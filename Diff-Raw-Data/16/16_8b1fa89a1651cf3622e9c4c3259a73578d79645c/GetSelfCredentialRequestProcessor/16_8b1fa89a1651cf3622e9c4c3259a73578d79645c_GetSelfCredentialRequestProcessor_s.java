 package org.fiteagle.interactors.sfa.getSelfCredential;
 
 import java.io.ByteArrayOutputStream;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.util.GregorianCalendar;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBElement;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 import javax.xml.datatype.DatatypeConfigurationException;
 import javax.xml.datatype.DatatypeFactory;
 import javax.xml.datatype.XMLGregorianCalendar;
 import javax.xml.namespace.QName;
 
 import org.fiteagle.core.aaa.CertificateAuthority;
 import org.fiteagle.core.aaa.SignatureCreator;
 import org.fiteagle.interactors.sfa.common.AMResult;
 import org.fiteagle.interactors.sfa.common.ListCredentials;
 import org.fiteagle.interactors.sfa.common.SFAv3RequestProcessor;
 import org.fiteagle.interactors.sfa.getSelfCredential.jaxbClasses.Credential;
 import org.fiteagle.interactors.sfa.getSelfCredential.jaxbClasses.Privilege;
 import org.fiteagle.interactors.sfa.getSelfCredential.jaxbClasses.Privileges;
 import org.fiteagle.interactors.sfa.getSelfCredential.jaxbClasses.Signatures;
 import org.fiteagle.interactors.sfa.getSelfCredential.jaxbClasses.SignedCredential;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.xml.sax.InputSource;
 
 public class GetSelfCredentialRequestProcessor extends SFAv3RequestProcessor{
 
   Logger log = LoggerFactory.getLogger(getClass());
 	public String processRequest(String cert, String xrn, String type) {
 		if (!Type.contains(type)) {
 			throw new IllegalArgumentException();
 		}
 		
 		return getSelfCredential(cert, xrn, type);
 	}
 
 	private String getSelfCredential(String cert, String xrn, String type) {
 		// TODO implement the get self credentials. check access rights etc.
 	  
 	  String signedCredentialString = "";
 	  SignedCredential signedCredential = new SignedCredential();
 
     Credential credential = new Credential();
     credential.setId("testCredential");
     credential.setType("privilege");
    credential.setOwnerGid(getOwnerGID(cert));
     credential.setOwnerURN(getOwnerURN(xrn));
     credential.setTargetGid(credential.getOwnerGid());
    credential.setTargetURN(getOwnerURN(xrn));
     GregorianCalendar gregCalendar = new GregorianCalendar();
     gregCalendar
         .setTimeInMillis(java.lang.System.currentTimeMillis() + 10000);
     XMLGregorianCalendar expirationDate = null;
     try {
       expirationDate = DatatypeFactory.newInstance()
           .newXMLGregorianCalendar(gregCalendar);
     } catch (DatatypeConfigurationException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     }
     credential.setExpires(expirationDate);
     Privileges privileges = new Privileges();
     Privilege userPriv = new Privilege();
     userPriv.setCanDelegate(false);
     userPriv.setName("*");
     privileges.getPrivilege().add(userPriv);
     credential.setPrivileges(privileges);
   
     signedCredential.setCredential(credential);
     
     Signatures signatures = new Signatures();
     signedCredential.setSignatures(signatures);
     SignatureCreator signer = new SignatureCreator();
     try {
       String signedcredentialString = getJAXBString(signedCredential);
       InputSource is = new InputSource(new StringReader(signedcredentialString));
       ByteArrayOutputStream bout = signer.signContent(is, credential.getId());
       signedCredentialString = new String(bout.toByteArray());
       
     
     } catch (JAXBException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     } catch (Exception e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     }
     signedCredentialString =  "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + signedCredentialString;
     return signedCredentialString;
 	}
 
 
   private String getTargetGID(String xrn) {
     // TODO Auto-generated method stub
     return null;
   }
 
   private String getOwnerURN(String xrn) {
     
     String[] split = xrn.split("\\.");
     String user = split[split.length-1];
     String returnString = "urn:publicid:IDN"; 
     String domain = "";
     for(int i = 0; i< split.length-1;i++){
       if(i>0){
         domain+= ":"+split[i];
       }
       else{
         domain+=split[i];
       }
     }
     
     String plus = domain.length()>0 ? "+" :"";
     returnString = returnString + plus + domain + "+user+" +user;
     return returnString;
   }
 
   private String getOwnerGID(String cert) {
    CertificateAuthority ca = new CertificateAuthority();
     String owner_gid = ca.getUserCertificateAsString(cert);
     return owner_gid;
   }
 
   private String getSignedCredentialString(SignedCredential signedCredential) {
     String signedCredentialSTR="";
     QName _SignedCredential_QNAME = new QName("", "signed-credential");
 		JAXBElement<SignedCredential> signedCredJAXB = new JAXBElement<SignedCredential>(_SignedCredential_QNAME, SignedCredential.class, null, signedCredential);
 
     try {
       signedCredentialSTR = getJAXBString(signedCredJAXB);
     } catch (JAXBException e) {
       System.out.println("EXCEPTION!!!!!!!!1"+e);
     }
     
     return signedCredentialSTR;
   }
 	
 	private static String getJAXBString(Object jaxbObject) throws JAXBException {
     JAXBContext context = JAXBContext
         .newInstance("org.fiteagle.interactors.sfa.getSelfCredential.jaxbClasses");
     Marshaller marshaller = context.createMarshaller();
     StringWriter stringWriter = new StringWriter();
     marshaller.marshal(jaxbObject, stringWriter);
 
     return stringWriter.toString();
 
   }
 
 	@Override
 	public AMResult processRequest(ListCredentials credentials,
 			Object... specificArgs) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 }
