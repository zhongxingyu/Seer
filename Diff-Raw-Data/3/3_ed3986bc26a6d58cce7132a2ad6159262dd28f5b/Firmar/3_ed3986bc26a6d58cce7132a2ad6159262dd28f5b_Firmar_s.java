 package cidc.certificaciones.signer;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.security.KeyStore;
 import java.security.KeyStoreException;
 import java.security.NoSuchAlgorithmException;
 import java.security.PrivateKey;
 import java.security.UnrecoverableKeyException;
 import java.security.cert.Certificate;
 import java.security.cert.CertificateException;
 import java.util.ResourceBundle;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import edu.logic.pki.KeyStoreTools;
 import edu.logic.pki.PDFSignVerifier;
 import edu.logic.pki.PDFSigner;
 
 public class Firmar {
 	public ResourceBundle rb;
 	
 	public String firmarPDF(File documento,String ruta) throws IOException{
 		rb=ResourceBundle.getBundle("cidc.general.conect");
 
 		KeyStoreTools kst = new KeyStoreTools("/usr/local/tomcat/webapps/siciud/CIDCks", rb.getString("clave2"));
         String alias = rb.getString("clave1");
         //Add pivate/certificate from PKCS#12 certificate (.pfx o . p12)
         try {
             
             kst.addCertificateP12(ruta, rb.getString("clave1"), alias);
         } catch (KeyStoreException ex) {
             Logger.getLogger(Firmar.class.getName()).log(Level.SEVERE, null, ex);
         } catch (FileNotFoundException ex) {
             Logger.getLogger(Firmar.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IOException ex) {
             Logger.getLogger(Firmar.class.getName()).log(Level.SEVERE, null, ex);
         } catch (NoSuchAlgorithmException ex) {
             Logger.getLogger(Firmar.class.getName()).log(Level.SEVERE, null, ex);
         } catch (CertificateException ex) {
             Logger.getLogger(Firmar.class.getName()).log(Level.SEVERE, null, ex);
         } catch (UnrecoverableKeyException ex) {
             Logger.getLogger(Firmar.class.getName()).log(Level.SEVERE, null, ex);
         }
 		
         File file = new File("/usr/local/tomcat/webapps/siciud/CIDCks");
         file.delete();
 		
         PrivateKey key =  (PrivateKey) kst.getKey(alias, rb.getString("clave1").toCharArray());
         Certificate[] certificate = kst.getCertificateChain(alias);
         Certificate c = certificate[0];
         
         //Sign PDF
         PDFSigner pdfs = new PDFSigner();
         File pdfSigned = pdfs.sign(documento, key, c);
         
         //Verify PDF signature
         PDFSignVerifier verifier = new PDFSignVerifier();
         boolean isValid = verifier.verify(pdfSigned);
         
         System.out.println(isValid+"nombre: "+pdfSigned.getName());
         return pdfSigned.getName();
 	}
 	
 //	public void newKeyStore(String ruta){
 //		KeyStoreTools kst = new KeyStoreTools(ruta, "parafrase");
 //		kst.addCertificate(arg0, arg1, arg2)
 //	}
 
 }
