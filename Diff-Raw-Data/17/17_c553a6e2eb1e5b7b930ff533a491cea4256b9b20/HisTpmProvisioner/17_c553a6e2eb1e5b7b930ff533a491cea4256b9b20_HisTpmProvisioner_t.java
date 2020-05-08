 /*
  * 2012, U.S. Government, National Security Agency, National Information Assurance Research Laboratory
  * 
  * This is a work of the UNITED STATES GOVERNMENT and is not subject to copyright protection in the United States. Foreign copyrights may apply.
  * 
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
  * �Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer. 
  * �Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution. 
  * �Neither the name of the NATIONAL SECURITY AGENCY/NATIONAL INFORMATION ASSURANCE RESEARCH LABORATORY nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package gov.niarl.his.privacyca;
 
 import gov.niarl.his.privacyca.TpmModule.TpmModuleException;
 import gov.niarl.his.webservices.hisPrivacyCAWebService2.IHisPrivacyCAWebService2;
 import gov.niarl.his.webservices.hisPrivacyCAWebService2.client.HisPrivacyCAWebServices2ClientInvoker;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.File;
 import java.io.FileOutputStream;
 
 import java.security.NoSuchAlgorithmException;
 import java.security.Security;
 import java.security.PublicKey;
 import java.security.cert.CertificateEncodingException;
 import java.security.cert.X509Certificate;
 import java.util.Properties;
 
 import javax.crypto.Cipher;
 import javax.crypto.KeyGenerator;
 import javax.crypto.NoSuchPaddingException;
 import javax.crypto.SecretKey;
 import javax.security.cert.CertificateException;
 import java.security.interfaces.RSAPublicKey;  
 
 import org.bouncycastle.jce.provider.BouncyCastleProvider;
 
 /**
  * <p>This is part 1 of 3 for fully provisioning HIS on a Windows client. This class does the initial provisioning of the TPM.</p>
  * This provisioning includes:
  * <ul>
  * <li>Taking ownership of the TPM</li>
  * <li>Creating an Endorsement Certificate</li>
  * <li>Storing the Endorsement Certificate in the TPM's NVRAM</li>
  * </ul>
  * 
  * <p>This class utilizes a properties file. It looks for a file by the name of "HISprovisioner.properties" in the directory from which Java was invoked.</p>
  * The following values must be in the properties file:<br>
  * <ul>
  * <li><b>TpmEndorsmentP12</b></li>
  * <li><b>EndorsementP12Pass</b></li>
  * <li><b>EcValidityDays</b></li>
  * <li><b>TpmOwnerAuth</b> This must be a 40 digit (20 byte) hex code representing the owner auth data to be assigned.</li>
  * </ul>
  * 
  * @author schawki
  *
  */
 public class HisTpmProvisioner {
 
 	/**
 	 * Entry point into the program
 	 */
 	public static void main(String[] args){// throws InvalidKeyException, CertificateEncodingException, UnrecoverableKeyException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, NoSuchProviderException, KeyStoreException, CertificateException, IOException, javax.security.cert.CertificateException {
 		//get properties file info
 		final String EC_VALIDITY = "EcValidityDays";
 		final String OWNER_AUTH = "TpmOwnerAuth";
 		final String PRIVACY_CA_URL = "PrivacyCaUrl";
 		final String TRUST_STORE = "TrustStore";
         final String EC_STORAGE = "ecStorage";
         final String PRIVACY_CA_CERT = "PrivacyCaCertFile";
  
 		String PrivacyCaUrl = "";
 		int EcValidityDays = 0;
 		String TrustStore = "";
         String ecStorage = "";
         String PrivacyCaCertFile = "";
 		
 		byte [] TpmOwnerAuth = null;
 		byte[] encryptCert = null;
 		byte [] pubEkMod = null;
 		X509Certificate pcaCert = null;
 		PublicKey publicKey = null;
 
 		String propertiesFileName = "./OATprovisioner.properties";
                  String ecStorageFileName = "./EC.cer";
 
 		FileInputStream PropertyFile = null;
 		try {
 			PropertyFile = new FileInputStream(propertiesFileName);
 			Properties HisProvisionerProperties = new Properties();
 			HisProvisionerProperties.load(PropertyFile);
 			
 			EcValidityDays = Integer.parseInt(HisProvisionerProperties.getProperty(EC_VALIDITY, ""));
 			TpmOwnerAuth = TpmUtils.hexStringToByteArray(HisProvisionerProperties.getProperty(OWNER_AUTH, ""));
 			PrivacyCaUrl = HisProvisionerProperties.getProperty(PRIVACY_CA_URL, "");
 			PrivacyCaCertFile = HisProvisionerProperties.getProperty(PRIVACY_CA_CERT, "");
 			TrustStore = HisProvisionerProperties.getProperty(TRUST_STORE, "TrustStore.jks");
             ecStorage = HisProvisionerProperties.getProperty(EC_STORAGE, "NVRAM");
             System.out.println("### ecStorage = "  + ecStorage + "###");
 		} catch (FileNotFoundException e) {
 			System.out.println("Error finding HIS Provisioner properties file (HISprovisionier.properties)");
 		} catch (IOException e) {
 			System.out.println("Error loading HIS Provisioner properties file (HISprovisionier.properties)");
 		}
 		catch (NumberFormatException e) {
 			e.printStackTrace();
 		}
 		finally{
 			if (PropertyFile != null){
 				try {
 					PropertyFile.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		String errorString = "Properties file \"" + propertiesFileName + "\" contains errors:\n";
 		boolean hasErrors = false;
 		if(EcValidityDays == 0){
 			errorString += " - \"EcValidityDays\" value must be the number of validity days for the Endorsement Credential\n";
 			hasErrors = true;
 		}
 		if(TpmOwnerAuth ==null || TpmOwnerAuth.length != 20){
 			errorString += " - \"TpmOwnerAuth\" value must be a 40 hexidecimal digit (20 byte) value representing the TPM owner auth\n";
 			hasErrors = true;
 		}
 		if(PrivacyCaCertFile.length() == 0){
 			errorString += "There is a improper configuration within properties file, please check the file first\n";
 			hasErrors = true;
 		}
 		if(hasErrors){
 			System.out.println(errorString);
 			System.exit(99);
 			return;
 		}
 		//Provision the TPM
 		System.out.print("Performing TPM provisioning...");
 		
 		
 		/*
 		 * The following actions must be performed during the TPM Provisioning process:
 		 * 1. Take ownership of the TPM
 		 * 		- owner auth
 		 * 2. Create an Endorsement Certificate (EC)
 		 * 		- public EK
 		 * 			- owner auth (should already have from above)
 		 * 		- private key and cert for CA to create new cert
 		 * 		- validity period of EC cert 
 		 * 3. Store the newly created EC in the TPM's NV-RAM
 		 */
 		SecretKey deskey = null;
 		KeyGenerator keygen;
 		Cipher c;
 		Security.addProvider(new BouncyCastleProvider());
 		// Take Ownership
 		byte [] nonce = null;
 		try {
 			nonce = TpmUtils.createRandomBytes(20);
 			TpmModule.takeOwnership(TpmOwnerAuth, nonce);
 		} catch (TpmModuleException e){
 			//System.out.println("Error taking ownership: " + e.toString());
 		} catch (IOException e) {
 			//e.printStackTrace();
 		}
 		
 		// Generate security key via 3DES algorithm
 		try {
 			keygen = KeyGenerator.getInstance("DESede"); 
 			deskey = keygen.generateKey();  
 		    c = Cipher.getInstance("DESede");
 		} catch (NoSuchPaddingException e) {
 			System.out.println("Exception message is found, detail info is: " + e.getMessage());
 		} catch (NoSuchAlgorithmException e) {
 			e.printStackTrace();
 		} 
 		
 		// Create Endorsement Certificate
 		try {
 			nonce = TpmUtils.createRandomBytes(20);
 			pubEkMod = TpmModule.getEndorsementKeyModulus(TpmOwnerAuth, nonce);
 		} catch (TpmModuleException e){
 			System.out.println("Error getting PubEK: " + e.toString());
 		} catch (Exception e){
 			System.out.println("Error getting PubEK: " + e.toString());
 		}
 		
 		try {
 			pcaCert = TpmUtils.certFromFile(PrivacyCaCertFile);
 			if (pcaCert != null){
 			publicKey = (RSAPublicKey)pcaCert.getPublicKey();
 			}
 		} catch (Exception e){
 			System.out.println("print out error message: " + e.toString());
 			e.printStackTrace();
 		}
 	
 		
 		System.setProperty("javax.net.ssl.trustStore", "./" + TrustStore);
 		try {					
 			IHisPrivacyCAWebService2 hisPrivacyCAWebService2 = HisPrivacyCAWebServices2ClientInvoker.getHisPrivacyCAWebService2(PrivacyCaUrl);
 			encryptCert = hisPrivacyCAWebService2.requestGetEC(encryptDES(pubEkMod, deskey), encryptRSA(deskey.getEncoded(), publicKey), EcValidityDays);	
 		} catch (Exception e){
 			System.out.println("FAILED");
 			e.printStackTrace();
 			System.exit(1);
 		}
 		
 		//Decrypt and generate endorsement certificate 
 		X509Certificate ekCert = null;		
 		try {
 			if (encryptCert != null){
 				ekCert = TpmUtils.certFromBytes(decryptDES(encryptCert, deskey));
 			}
 		} catch (java.security.cert.CertificateException e) {
 			e.printStackTrace();
 		} catch (CertificateException e) {
 			e.printStackTrace();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 			
 		// Store the new EC in NV-RAM
        if (ecStorage.equalsIgnoreCase("file")) {
     	   System.out.println("--store EC in file--");
     	   try{
                 File ecFile = new File(ecStorageFileName);
                 FileOutputStream ecFileOut = new FileOutputStream(ecFile);
                 ecFileOut.write(ekCert.getEncoded());
                 ecFileOut.flush();
                 ecFileOut.close();
                 }catch(Exception e){
                    System.out.println("FAILED");
                    e.printStackTrace();
                    System.exit(1);
                }
        } else {
  		try{
  			TpmModule.setCredential(TpmOwnerAuth, "EC", ekCert.getEncoded());
                           System.out.println( ekCert.getEncoded().length);
  		} catch (TpmModuleException e){
  			System.out.println("Error getting PubEK: " + e.toString());
  		} catch (CertificateEncodingException e) {
  			e.printStackTrace();
  		} catch (IOException e) {
  			e.printStackTrace();
  		}
         }
 		System.out.println("DONE");
 		System.exit(0);
 		return;
 	}
 
     private static byte[] encryptDES(byte[] text, SecretKey key) throws Exception {
     	Cipher c = Cipher.getInstance("DESede");  
 		c.init(Cipher.ENCRYPT_MODE, key);  
 		return c.doFinal(text);
     }
     
     private static byte[] encryptRSA(byte[] text, PublicKey pubRSA) throws Exception {
     	Cipher cipher = Cipher.getInstance("RSA", new BouncyCastleProvider());
         cipher.init(Cipher.ENCRYPT_MODE, pubRSA);
         return cipher.doFinal(text);
     }
     
     private static byte[] decryptDES(byte[] text, SecretKey key) throws Exception {
     	Cipher cipher = Cipher.getInstance("DESede");
         cipher.init(Cipher.DECRYPT_MODE, key);
         return cipher.doFinal(text);
     }
 }
