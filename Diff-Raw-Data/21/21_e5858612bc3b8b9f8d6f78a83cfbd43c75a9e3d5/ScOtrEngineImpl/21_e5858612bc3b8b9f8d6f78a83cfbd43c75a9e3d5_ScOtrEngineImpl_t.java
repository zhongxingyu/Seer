 package net.java.sip.communicator.plugin.otr;
 
 import java.security.KeyFactory;
 import java.security.KeyPair;
 import java.security.KeyPairGenerator;
 import java.security.NoSuchAlgorithmException;
 import java.security.PrivateKey;
 import java.security.PublicKey;
 import java.security.spec.*;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 
 import org.bouncycastle.util.encoders.Base64;
 import org.osgi.framework.ServiceReference;
 
 import net.java.otr4j.OtrEngine;
 import net.java.otr4j.OtrEngineImpl;
 import net.java.otr4j.OtrEngineHost;
 import net.java.otr4j.OtrKeyManager;
 import net.java.otr4j.OtrPolicy;
 import net.java.otr4j.OtrPolicyImpl;
 import net.java.otr4j.crypto.OtrCryptoEngineImpl;
 import net.java.otr4j.crypto.OtrCryptoException;
 import net.java.otr4j.session.SessionID;
 import net.java.otr4j.session.SessionStatus;
 import net.java.sip.communicator.service.browserlauncher.BrowserLauncherService;
 import net.java.sip.communicator.service.gui.PopupDialog;
 import net.java.sip.communicator.service.protocol.AccountID;
 import net.java.sip.communicator.service.protocol.Contact;
 import net.java.sip.communicator.service.protocol.Message;
 import net.java.sip.communicator.service.protocol.OperationSetBasicInstantMessaging;
 
 public class ScOtrEngineImpl
     implements ScOtrEngine
 {
 
     private List<ScOtrEngineListener> listeners =
         new Vector<ScOtrEngineListener>();
 
     public void addListener(ScOtrEngineListener l)
     {
         listeners.add(l);
     }
 
     public void removeListener(ScOtrEngineListener l)
     {
         listeners.remove(l);
     }
 
     private List<String> injectedMessageUIDs = new Vector<String>();
 
     public boolean isMessageUIDInjected(String mUID)
     {
         return injectedMessageUIDs.contains(mUID);
     }
 
     class ScOtrKeyManager
         implements OtrKeyManager
     {
         public KeyPair getKeyPair(SessionID sessionID)
         {
             String accountID = sessionID.getAccountID();
             KeyPair keyPair = loadKeyPair(accountID);
             if (keyPair == null)
                 generateKeyPair(accountID);
 
             return loadKeyPair(accountID);
         }
     }
 
     class ScOtrEngineHost
         implements OtrEngineHost
     {
         public void showWarning(SessionID sessionID, String warn)
         {
             // TODO Dialog usage is not that great.
             OtrActivator.uiService.getPopupDialog().showMessagePopupDialog(
                 warn, "OTR warning", PopupDialog.WARNING_MESSAGE);
         }
 
         public void showError(SessionID sessionID, String err)
         {
             // TODO Dialog usage is not that great.
             OtrActivator.uiService.getPopupDialog().showMessagePopupDialog(err,
                 "OTR Error", PopupDialog.ERROR_MESSAGE);
         }
 
         public void injectMessage(SessionID sessionID, String messageText)
         {
             Contact contact = contactsMap.get(sessionID);
             OperationSetBasicInstantMessaging imOpSet =
                 (OperationSetBasicInstantMessaging) contact
                     .getProtocolProvider().getOperationSet(
                         OperationSetBasicInstantMessaging.class);
 
             Message message = imOpSet.createMessage(messageText);
             injectedMessageUIDs.add(message.getMessageUID());
             imOpSet.sendInstantMessage(contact, message);
         }
 
         public OtrPolicy getSessionPolicy(SessionID sessionID)
         {
             return getContactPolicy(contactsMap.get(sessionID));
         }
 
         public void sessionStatusChanged(SessionID sessionID)
         {
             Contact contact = contactsMap.get(sessionID);
             if (contact == null)
                 return;
 
             switch (otrEngine.getSessionStatus(sessionID))
             {
             case ENCRYPTED:
                 PublicKey remotePubKey =
                     otrEngine.getRemotePublicKey(sessionID);
 
                 PublicKey storedPubKey = loadPublicKey(sessionID.getUserID());
 
                 if (!remotePubKey.equals(storedPubKey))
                     savePublicKey(sessionID.getUserID(), remotePubKey);
                 break;
             }
 
             for (ScOtrEngineListener l : listeners)
             {
                 l.sessionStatusChanged(contact);
             }
         }
     }
 
     private OtrEngine otrEngine =
         new OtrEngineImpl(new ScOtrEngineHost(), new ScOtrKeyManager());
 
     public boolean isContactVerified(Contact contact)
     {
         return this.configurator.getPropertyBoolean(getSessionID(contact) + "publicKey.verified", false);
     }
 
     Map<SessionID, Contact> contactsMap = new Hashtable<SessionID, Contact>();
 
     public void endSession(Contact contact)
     {
         otrEngine.endSession(getSessionID(contact));
     }
 
     public SessionStatus getSessionStatus(Contact contact)
     {
         return otrEngine.getSessionStatus(getSessionID(contact));
     }
 
     public String transformReceiving(Contact contact, String msgText)
     {
         return otrEngine.transformReceiving(getSessionID(contact), msgText);
     }
 
     public String transformSending(Contact contact, String msgText)
     {
         return otrEngine.transformSending(getSessionID(contact), msgText);
     }
 
     public void refreshSession(Contact contact)
     {
         otrEngine.refreshSession(getSessionID(contact));
     }
 
     public void startSession(Contact contact)
     {
         otrEngine.startSession(getSessionID(contact));
     }
 
     private SessionID getSessionID(Contact contact)
     {
         SessionID sessionID =
             new SessionID(contact.getProtocolProvider().getAccountID()
                 .getAccountUniqueID(), contact.getAddress(), contact
                 .getProtocolProvider().getProtocolName());
 
         contactsMap.put(sessionID, contact);
         return sessionID;
     }
 
     public String getRemoteFingerprint(Contact contact)
     {
         PublicKey remotePublicKey = loadPublicKey(contact.getAddress());
         if (remotePublicKey == null)
             return null;
         try
         {
             return new OtrCryptoEngineImpl().getFingerprint(remotePublicKey);
         }
         catch (OtrCryptoException e)
         {
             e.printStackTrace();
             return null;
         }
     }
 
     public String getLocalFingerprint(AccountID account)
     {
         KeyPair keyPair = loadKeyPair(account.getAccountUniqueID());
 
         if (keyPair == null)
             return null;
 
         PublicKey pubKey = keyPair.getPublic();
 
         try
         {
             return new OtrCryptoEngineImpl().getFingerprint(pubKey);
         }
         catch (OtrCryptoException e)
         {
             e.printStackTrace();
             return null;
         }
     }
 
     private Configurator configurator = new Configurator();
     
     class Configurator { 
 	    
     	private String getXmlFriendlyString(String s){
    		if (s == null || s.length() < 1)
    			return s;
    		
    		// XML Tags are not allowed to start with digits, 
    		// insert a dummy "p" char.
    		if (Character.isDigit(s.charAt(0)))
    			s = "p" + s;
    		
     		char[] cId = new char[s.length()];
 	    	for (int i = 0; i < cId.length; i++) {
 	    		char c = s.charAt(i);
 	    		cId[i] = (Character.isLetterOrDigit(c)) ? c : '_';
 			}
 	    	
 	    	return new String(cId);
     	}
     	
 	    private String getID(String id){
 	    	return "net.java.sip.communicator.plugin.otr." + getXmlFriendlyString(id);
 	    }
 	    
 	    public byte[] getPropertyBytes(String id){
 	    	String value = (String)OtrActivator.configService.getProperty(this.getID(id));
 	    	if (value == null)
 	    		return null;
 	    	
 	    	return Base64.decode(value.getBytes());
 	    }
 	    
 	    public Boolean getPropertyBoolean(String id, boolean defaultValue){
 	    	return OtrActivator.configService.getBoolean(this.getID(id), defaultValue);
 	    }
 	    
 	    public void setProperty(String id, byte[] value){
 	    	String valueToStore = new String(Base64.encode(value));
 	    	
 	    	OtrActivator.configService.setProperty(this.getID(id), valueToStore);
 	    }
 	    
 	    public void setProperty(String id, boolean value){	    	
 	    	OtrActivator.configService.setProperty(this.getID(id), value);
 	    }
 	    
 	    public void setProperty(String id, Integer value){
 	    	OtrActivator.configService.setProperty(this.getID(id), value);
 	    }
 	    
 	    public void removeProperty(String id){
 	    	OtrActivator.configService.removeProperty(this.getID(id));
 	    }
 
 		public int getPropertyInt(String id, int defaultValue) {
 			return OtrActivator.configService.getInt(getID(id), defaultValue);
 		}
     }
     
     public void verifyContactFingerprint(Contact contact)
     {
         if (contact == null)
             return;
         
         this.configurator.setProperty(getSessionID(contact) + "publicKey.verified", true);
     }
 
     public void forgetContactFingerprint(Contact contact)
     {
         if (contact == null)
             return;
 
         this.configurator.removeProperty(getSessionID(contact) + "publicKey.verified");
 
     }
 
     public OtrPolicy getGlobalPolicy()
     {
         return new OtrPolicyImpl(this.configurator
         		.getPropertyInt("POLICY", OtrPolicy.OTRL_POLICY_DEFAULT));
     }
 
     public void setGlobalPolicy(OtrPolicy policy)
     {
         if (policy == null)
             this.configurator.removeProperty("POLICY");
         else
             this.configurator.setProperty("POLICY", policy.getPolicy());
 
         for (ScOtrEngineListener l : listeners)
             l.globalPolicyChanged();
     }
 
     public void launchHelp()
     {
     	ServiceReference ref = OtrActivator.bundleContext
         	.getServiceReference(BrowserLauncherService.class.getName());
 
 		if (ref == null)
 			return;
 		
 		BrowserLauncherService service = (BrowserLauncherService)
 		OtrActivator.bundleContext.getService(ref);
 		
 		service.openURL(OtrActivator.resourceService
 		        .getI18NString("plugin.otr.authbuddydialog.HELP_URI"));
     }
 
     public OtrPolicy getContactPolicy(Contact contact)
     {
         int policy = this.configurator.getPropertyInt(getSessionID(contact) + "policy", -1);
         if (policy < 0)
             return getGlobalPolicy();
         else
             return new OtrPolicyImpl(policy);
     }
 
     public void setContactPolicy(Contact contact, OtrPolicy policy)
     {
     	String propertyID = getSessionID(contact) + "policy";
         if (policy == null)
         	this.configurator.removeProperty(propertyID);
         else
         	this.configurator.setProperty(propertyID, policy.getPolicy());
 
         for (ScOtrEngineListener l : listeners)
             l.contactPolicyChanged(contact);
 
     }
 
     private KeyPair loadKeyPair(String accountID)
     {
         // Load Private Key.
         byte[] b64PrivKey = this.configurator.getPropertyBytes(accountID + ".privateKey");
         if (b64PrivKey == null)
             return null;
 
         PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(b64PrivKey);
 
         // Load Public Key.
         byte[] b64PubKey = this.configurator.getPropertyBytes(accountID + ".publicKey");
         if (b64PubKey == null)
             return null;
 
         X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(b64PubKey);
 
         PublicKey publicKey;
         PrivateKey privateKey;
 
         // Generate KeyPair.
         KeyFactory keyFactory;
         try
         {
             keyFactory = KeyFactory.getInstance("DSA");
             publicKey = keyFactory.generatePublic(publicKeySpec);
             privateKey = keyFactory.generatePrivate(privateKeySpec);
         }
         catch (NoSuchAlgorithmException e)
         {
             e.printStackTrace();
             return null;
         }
         catch (InvalidKeySpecException e)
         {
             e.printStackTrace();
             return null;
         }
 
         return new KeyPair(publicKey, privateKey);
     }
 
     public void generateKeyPair(String accountID)
     {
         KeyPair keyPair;
         try
         {
             keyPair = KeyPairGenerator.getInstance("DSA").genKeyPair();
         }
         catch (NoSuchAlgorithmException e)
         {
             e.printStackTrace();
             return;
         }
 
         // Store Public Key.
         PublicKey pubKey = keyPair.getPublic();
         X509EncodedKeySpec x509EncodedKeySpec =
             new X509EncodedKeySpec(pubKey.getEncoded());
         
         this.configurator.setProperty(accountID + ".publicKey", x509EncodedKeySpec.getEncoded());
 
         // Store Private Key.
         PrivateKey privKey = keyPair.getPrivate();
         PKCS8EncodedKeySpec pkcs8EncodedKeySpec =
             new PKCS8EncodedKeySpec(privKey.getEncoded());
         
         this.configurator.setProperty(accountID + ".privateKey", pkcs8EncodedKeySpec.getEncoded());
     }
 
     private void savePublicKey(String userID, PublicKey pubKey)
     {
         X509EncodedKeySpec x509EncodedKeySpec =
             new X509EncodedKeySpec(pubKey.getEncoded());
 
         this.configurator.setProperty(userID + ".publicKey", x509EncodedKeySpec.getEncoded());
 
         this.configurator.removeProperty(userID + ".publicKey.verified");
     }
 
     private PublicKey loadPublicKey(String userID)
     {
         byte[] b64PubKey = this.configurator.getPropertyBytes(userID + ".publicKey");
         if (b64PubKey == null)
             return null;
 
         X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(b64PubKey);
 
         // Generate KeyPair.
         KeyFactory keyFactory;
         try
         {
             keyFactory = KeyFactory.getInstance("DSA");
             return keyFactory.generatePublic(publicKeySpec);
         }
         catch (NoSuchAlgorithmException e)
         {
             e.printStackTrace();
             return null;
         }
         catch (InvalidKeySpecException e)
         {
             e.printStackTrace();
             return null;
         }
     }
 }
