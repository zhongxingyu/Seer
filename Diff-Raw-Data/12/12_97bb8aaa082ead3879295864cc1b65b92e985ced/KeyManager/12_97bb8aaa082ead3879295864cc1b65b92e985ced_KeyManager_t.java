 package org.certificatesmanager.security;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.ObjectInputStream.GetField;
 import java.security.Key;
 import java.security.KeyStore;
 import java.security.KeyStoreException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.security.PrivateKey;
 import java.security.PublicKey;
 import java.security.UnrecoverableKeyException;
 import java.security.cert.CertificateException;
 import java.security.cert.X509Certificate;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.List;
 
 import javax.security.auth.x500.X500Principal;
 
 import org.certificatesmanager.utils.StringUtils;
 
 /**
  * @author Ivan Gualandri
  *
  */
 public class KeyManager {
 	
 	private String keyPath;
 	private String password;
 	private int size;
 	
 	private List<KeyObject> keyList = new ArrayList<KeyObject>();
 
 	public KeyManager(String keyPath){
 		this.keyPath = keyPath;
 		this.password="";
 		size = -1;
 	}
 	
 	public KeyManager(String keyPath, String password){
 		this(keyPath);
 		this.password=password;
 	}
 	
 	/**
 	 * @see #load()
 	 * @return The list of available keys.
 	 */
 	public List<KeyObject> getKeyList() {
 		return keyList;
 	}
 	
 	/**
 	 * @return Number of entries into keystore
 	 */
 	public int getKeystoreSize(){
 		if(size==-1){
 			try {
 				FileInputStream is = new FileInputStream(keyPath);
 				KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
 				size = keystore.size();
 				is.close();
 			} catch (Exception e) {				
 				e.printStackTrace();
 			}			
 		}
 		return this.size;
 	}
 	
 	
 	/**
 	 * Load all availables keys into a List. You can obtain the list with getKeyList() method.	  
 	 * @see #getKeyList()
 	 * @throws Exception
 	 */
 	public void load() throws Exception {
 		try {
 			FileInputStream is = new FileInputStream(keyPath);
 			KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
 			
 			keystore.load(is, password.toCharArray());		
 			Enumeration<String> e = keystore.aliases();			
 		    for (; e.hasMoreElements(); ) {
 		        String alias = (String)e.nextElement();
 		        System.out.println(alias);		       
 		        // Does alias refer to a private key?
 		        boolean b = keystore.isKeyEntry(alias);
 		        System.out.println("Key: " + b);		        
 		        if(b){
 		        	Key key = keystore.getKey(alias, password.toCharArray());
 		        	System.out.println("Creation: " + keystore.getCreationDate(alias));
 		        	System.out.println("Key: " + key.getAlgorithm());
 		        	if (key instanceof PrivateKey) {
 		                // Get certificate of public key
 		        		String issuer;
 		        		Date startDate;
 		        		Date endDate; 
 		        		String algorithm;
 		                java.security.cert.Certificate cert = keystore.getCertificate(alias);		                
 		                X509Certificate xcert = (X509Certificate)cert;
 		                getFingerPrint(xcert.getEncoded());
 		                X500Principal principal = xcert.getSubjectX500Principal();		                
 		                System.out.println("X500Name: " + principal.getName());
 		                issuer = principal.getName();
 		                principal = xcert.getIssuerX500Principal();
 		                startDate = xcert.getNotBefore();
 		                endDate = xcert.getNotAfter();
 		                algorithm = xcert.getSigAlgName();
 		                KeyObject entry = new KeyObject(alias, issuer, startDate, endDate, algorithm);
 		                keyList.add(entry);
 		                System.out.println("NotAfter: " + xcert.getNotAfter());
 		                System.out.println("Not Before: " + xcert.getNotBefore());
 		                System.out.println("X500Issuer: " + principal.getName());
 		                System.out.println("Encoded Fingerprint: ");		                
 		                // Get public key
 		                PublicKey publicKey = cert.getPublicKey();
 		                getFingerPrint(publicKey.getEncoded());
 		                System.out.println("");
 		        	}
 		        }
 		        // Does alias refer to a trusted certificate?
 		        b = keystore.isCertificateEntry(alias);
 		        System.out.println("Cert: " + b);
 		        if(b){
 		        	java.security.cert.Certificate cert = keystore.getCertificate(alias);
 		        	System.out.println(cert.toString());
 		        }
 		      
 		    }
 		    is.close();
 		    
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (KeyStoreException e) {
 			e.printStackTrace();
 			throw e;
 		} catch (NoSuchAlgorithmException e) {
 			e.printStackTrace();
 			throw e;
 		} catch (CertificateException e) {
 			e.printStackTrace();
 			throw e;
 		} catch (IOException e) {
 			e.printStackTrace();
 			throw e;
 		} catch (UnrecoverableKeyException e) {
 			e.printStackTrace();
 			throw e;
 		}		       
 	}
 	
 	public String getFingerPrint(byte[] encoded){
 		try {			
 			MessageDigest md = MessageDigest.getInstance("MD5");
 			byte[] digest = md.digest(encoded);
 			int i=0;
             System.out.println("Encoded: ");
             System.out.println(StringUtils.toHexString(digest)); 
             System.out.println("");
 			return null;			
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 }
