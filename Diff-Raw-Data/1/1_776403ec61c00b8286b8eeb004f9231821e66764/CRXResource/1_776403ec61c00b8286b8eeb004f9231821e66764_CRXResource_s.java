 package de.devsurf.chrome.extensions.jaxrs;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.security.PrivateKey;
 import java.security.PublicKey;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 
 import org.bouncycastle.jce.provider.JCERSAPrivateCrtKey;
 
 import de.devsurf.chrome.extensions.BouncyCastleUtil;
 import de.devsurf.chrome.extensions.CrxWriter;
 
 @Path("/")
 public class CRXResource {
 	public static final URL PEM_FILE = CRXResource.class.getResource("/sample.pem");
 		
 	@GET
 	@Path("sample.crx")
 	@Produces("application/x-chrome-extension")
 	public InputStream get() throws Exception{
 		ByteArrayOutputStream out = new ByteArrayOutputStream();
 		ZipOutputStream zip = new ZipOutputStream(out);
 		byte[] bytes = getBytes("/background.html");
 		writeZipEntry("background.html", bytes, zip);
 		
 		bytes = getBytes("/manifest.json");
 		writeZipEntry("/manifest.json", bytes, zip);
 		zip.flush();
 		zip.close();
 		
 		byte[] crxContent = out.toByteArray();
 		out.reset();
 		
 		JCERSAPrivateCrtKey certificate = BouncyCastleUtil.readCertificate(new File(PEM_FILE.toURI()));
 		PrivateKey privateKey = BouncyCastleUtil.createPrivateKey(certificate);
 		PublicKey publicKey = BouncyCastleUtil.createPublicKey(certificate);
 		
 		new CrxWriter().create(out, new ByteArrayInputStream(crxContent), privateKey, publicKey);
 		
 		return new ByteArrayInputStream(out.toByteArray());
 	}
 	
 	public void writeZipEntry(String filename, byte[] bytes, ZipOutputStream zip) throws IOException{
 		ZipEntry entry = new ZipEntry(filename);
 		zip.putNextEntry(entry);
 		
 		for(byte b : bytes){
 			zip.write(b);
 		}
 		zip.closeEntry();
 	}
 	
 	public byte[] getBytes(String filename) throws Exception {
 		URL file = CRXResource.class.getResource(filename);
 		File f = new File(file.toURI());
 		FileInputStream fis = new FileInputStream(f);
 		DataInputStream dis = new DataInputStream(fis);
 		byte[] keyBytes = new byte[(int) f.length()];
 		dis.readFully(keyBytes);
 		dis.close();
 
 		return keyBytes;
 	}

 }
