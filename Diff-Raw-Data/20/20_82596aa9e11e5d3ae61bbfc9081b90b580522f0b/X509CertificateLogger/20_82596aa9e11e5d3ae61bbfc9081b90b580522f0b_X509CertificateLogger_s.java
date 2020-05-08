 package sslhoff;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.security.cert.CertificateEncodingException;
 import java.security.cert.X509Certificate;
 
 public class X509CertificateLogger
 {
 	private String folder;
 	FileOutputStream log;
 
 	public X509CertificateLogger(String folder, String logFileName)
 			throws IOException
 	{
 		this.log = new FileOutputStream(logFileName);
 		this.folder = folder;
 	}
 
 	private boolean store(String fileName, X509Certificate cert)
 			throws IOException, NoSuchAlgorithmException,
 			CertificateEncodingException
 	{
 		if (cert instanceof Serializable)
 		{
 			Serializable k = (Serializable) cert;
 			FileOutputStream certfile = new FileOutputStream(fileName);
 			ObjectOutputStream x = new ObjectOutputStream(certfile);
 			x.writeObject(k);
 			certfile.close();
 		} else
 		{
 			System.out.println("could not Serialize Cert");
 		}
 		return false;
 	}
 
 	synchronized void log(String identifier,X509Certificate cert) throws CertificateEncodingException,
 			NoSuchAlgorithmException, IOException, InterruptedException
 	{
 		
 		String fileName = "";
 		MessageDigest sha = MessageDigest.getInstance("SHA");
 		sha.digest(cert.getEncoded());
		fileName = folder + sha.toString() + ".ser";
 		String logMessage = identifier+";"+fileName+"\r";
 		log.write(logMessage.getBytes());
 		File test = new File(fileName);
 		System.out.println("Writing"+fileName);
 		if (!test.exists())
 		{
 			this.store(fileName, cert);
 		}
 	}
 
 }
