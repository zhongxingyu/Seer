 package ch.admin.cube.ws.impl;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.security.PublicKey;
 import java.security.cert.CertificateEncodingException;
 import java.security.cert.CertificateException;
 import java.security.cert.CertificateFactory;
 import java.security.cert.X509Certificate;
 import java.security.spec.X509EncodedKeySpec;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.TimeZone;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import sun.security.provider.X509Factory;
 import sun.security.util.DerOutputStream;
 import sun.security.x509.X509Key;
 import ch.admin.cube.ws.CubeWsServiceProperties;
 import ch.admin.cube.ws.ICubeManageDAO;
 import ch.admin.vbs.cube.common.crypto.Base64;
 import ch.admin.vbs.cube.common.crypto.HashUtil;
 import ch.admin.vbs.cube.common.crypto.PemToolkit;
 
 public class CubeManageDAO implements ICubeManageDAO {
 	private static final Logger LOG = LoggerFactory.getLogger(CubeManageDAO.class);
 	private File baseDir;
 	private Lock lock = new ReentrantLock();
 
 	public CubeManageDAO() {
 		baseDir = new File(CubeWsServiceProperties.getProperty("cube.manage.datastore.path"));
 	}
 
 	private void log(X509Certificate x509, String message) throws IOException {
 		log(x509, message, System.currentTimeMillis());
 	}
 
 	private void log(X509Certificate x509, String message, long timestamp) throws IOException {
 		// log into user directory
 		File log = new File(getuserDirectory(x509), "report.log");
 		GregorianCalendar c = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
 		c.setTimeInMillis(timestamp);
 		String msg = String.format("[%1$tY.%1$tm.%1$td %1$tH:%1$tM:%1$tS.%1$tL] %2$s\n", c, message);
 		LOG.debug(msg);
 		BufferedWriter br = new BufferedWriter(new FileWriter(log, true));
 		br.write(msg);
 		br.close();
 	}
 
 	private void zip(File out, File... files) throws IOException {
 		byte[] buf = new byte[1024];
 		ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(out));
 		//
 		for (File file : files) {
 			LOG.debug("Add ["+file.getName()+"] to zip");
 			FileInputStream fis = new FileInputStream(file);
 			zos.putNextEntry(new ZipEntry(file.getName()));
 			int len;
 			while ((len = fis.read(buf)) > 0)
 				zos.write(buf, 0, len);
 			zos.closeEntry();
 		}
 		zos.close();
 	}
 
 	private File getuserDirectory(X509Certificate x509) throws IOException {
 		String dn = x509.getSubjectDN().getName();
 		String dnHash = HashUtil.sha512UrlInBase64(dn);
 		// ensure that user directory exists and contains user.txt
 		File userDir = new File(baseDir, dnHash);
 		if (!userDir.exists()) {
			if (!userDir.mkdirs()) {
				throw new IOException("Could not create directory ["+userDir.getAbsolutePath()+"]");
			}
 			File userCfg = new File(userDir, "user.txt");
 			FileWriter fw = new FileWriter(userCfg);
 			fw.write("DN=" + dn + "\nDN_hash=" + dnHash + "\n");
 			fw.close();
 		}
 		return userDir;
 	}
 
 	// ====================================
 	// ICubeManageDAO
 	// ====================================
 	public File listVMs(X509Certificate x509) {
 		lock.lock();
 		try {
 			log(x509, "(WebService) listVMs");
 			//
 			String dn = x509.getSubjectDN().getName();
 			String dnHash = HashUtil.sha512UrlInBase64(dn);
 			// check user directories
 			File userDir = new File(baseDir, dnHash);
 			File dataDir = new File(userDir, "data");
 			if (!dataDir.exists()) {
 				if (!dataDir.mkdirs()) {
 					throw new RuntimeException("Failed to init user directory ["+dataDir.getAbsolutePath()+"]");
 				}
 			}
 			// list files in ueser's data directory (only keys and description files)
 			ArrayList<File> fileToSend = new ArrayList<File>();
 			LOG.debug("List files from [" + dataDir.getAbsolutePath() + "]");
 			for (File file : dataDir.listFiles(new FilenameFilter() {
 				@Override
 				public boolean accept(File dir, String name) {
 					return name.endsWith(".key.cube") || name.endsWith(".desc.cube");
 				}
 			})) {
 				LOG.debug("append file [" + file.getAbsolutePath() + "]");
 				fileToSend.add(file);
 			}
 			// Pack all files to return to client un a ZIP file.
 			File tmp = File.createTempFile("CubeManagerDAO", ".data");
 			tmp.deleteOnExit();
 			if (fileToSend.size() == 0) {
 				LOG.debug("No file to send. Send an empty file.");
 				tmp.createNewFile();
 			} else {
 				LOG.debug("Files found [" + fileToSend.size() + "]. Zip them in ["+tmp.getAbsolutePath()+"].");
 				zip(tmp, fileToSend.toArray(new File[fileToSend.size()]));
 			}
 			return tmp;
 			// log into user directory
 		} catch (Exception e) {
 			throw new RuntimeException("Failed to list user VMs.", e);
 		} finally {
 			lock.unlock();
 		}
 	}
 
 	@Override
 	public void storePublicKey(X509Certificate x509) {
 		lock.lock();
 		try {
 			// save public key in two files (with serial number in file name and
 			// as 'current') so current always point to the last certificate
 			// along all certificate versions.
 			String dn = x509.getSubjectDN().getName();
 			String dnHash = HashUtil.sha512UrlInBase64(dn);
 			File userDir = getuserDirectory(x509);
 			File pemFile = new File(userDir, dnHash + "-" + x509.getSerialNumber() + ".pem");
 			if (!pemFile.exists()) {
 				// save certificate
 				PemToolkit.writePem(x509,pemFile);
 				// update current certificate (use symbolic link once migrated on Java7)
 				PemToolkit.writePem(x509,new File(userDir, dnHash + "-current.pem"));
 				LOG.info("Public key stored [" + pemFile.getAbsolutePath() + "]");
 			}
 		} catch (Exception e) {
 			LOG.error("Failed to store public key.", e);
 		} finally {
 			lock.unlock();
 		}
 	}
 
 	@Override
 	public void report(X509Certificate x509, String message, long timestamp) {
 		lock.lock();
 		try {
 			log(x509, message, timestamp);
 		} catch (Exception e) {
 			LOG.error("Failed to report message [" + message + "].", e);
 		} finally {
 			lock.unlock();
 		}
 	}
 }
