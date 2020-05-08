 package ch.admin.vbs.cube.dataowner;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.security.KeyStore;
 import java.security.KeyStore.Builder;
 import java.security.Security;
 import java.security.cert.CertificateEncodingException;
 import java.security.cert.CertificateException;
 import java.security.cert.CertificateFactory;
 import java.security.cert.X509Certificate;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashSet;
 import java.util.Properties;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 
 import javax.crypto.SecretKey;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import sun.security.pkcs11.SunPKCS11;
 import sun.security.provider.X509Factory;
 import ch.admin.vbs.cube.common.UuidGenerator;
 import ch.admin.vbs.cube.common.crypto.AESEncrypter;
 import ch.admin.vbs.cube.common.crypto.Base64;
 import ch.admin.vbs.cube.common.crypto.HashUtil;
 import ch.admin.vbs.cube.common.crypto.PemToolkit;
 import ch.admin.vbs.cube.common.crypto.RSAEncryptUtil;
 
 /**
  * Prototype Application to manage VM and prepare them to be published on the
  * server.
  * 
  * - this app scan its working directory (/opt/cube/cubedataowner) for VM
  * templates. A VM template is a directory with a file 'vm.cfg' some VDI files
  * and VPN certificates. if the working directory does not exists yet, an
  * example will be created the first time this program is run. VMs are then
  * encrypted for each user individually and packaged with configuration files.
  */
 public class DataOwner {
 	private static final Logger LOG = LoggerFactory.getLogger(DataOwner.class);
 	private File baseDir;
 	private File outDir;
 	private HashSet<String> uuidSet = new HashSet<String>();
 
 	public DataOwner() {
 		// base directory. eventually generate example VM
 		baseDir = new File("/opt/cube/cubedataowner");
 		if (!baseDir.exists()) {
 			baseDir.mkdirs();
 			// make example:
 			createExampleStructure();
 		}
 		// output dir
 		outDir = new File(baseDir, "output");
 		if (!outDir.exists()) {
 			outDir.mkdir();
 		}
 		// scan all sub directories for VM templates
 		try {
 			System.out.println("Working directory [" + baseDir.getAbsolutePath() + "]");
 			for (File vmDir : baseDir.listFiles()) {
 				processVm(vmDir);
 			}
 		} catch (Exception e) {
 			LOG.error("Failed to process all directories", e);
 		}
 	}
 
 	private void processVm(File vmDir) throws Exception {
 		// skip not-vm directories
 		if (vmDir.getName().equals("output")) {
 			return;
 		}
 		// a VM template MUST contains the vm.cfg file
 		Properties prop = new Properties();
 		File cfg = new File(vmDir, "vm.cfg");
 		if (cfg.exists()) {
 			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(cfg));
 			prop.load(bis);
 			bis.close();
 			// check unique name of this template
 			if (prop.containsKey("uuid")) {
 				String uuid = prop.getProperty("uuid");
 				if (uuidSet.contains(uuid)) {
 					LOG.error("uuid [" + uuid + "] is not unique.");
 					System.exit(1);
 				} else {
 					uuidSet.add(uuid);
 				}
 			} else {
 				LOG.error("missing propertie 'uuid' in [{}]", cfg.getAbsolutePath());
 			}
 		} else {
 			// skip this directory
 			LOG.debug("Skip [{}] due to missing vm.cfg", vmDir.getAbsolutePath());
 		}
 		LOG.debug("VM directory [{}]", vmDir.getAbsolutePath());
 		// list users certificates
 		for (File certFile : vmDir.listFiles(new FileFilter() {
 			@Override
 			public boolean accept(File pathname) {
 				return pathname.getName().endsWith(".pem");
 			}
 		})) {
 			File uuidFile = new File(certFile.getAbsoluteFile().getAbsolutePath().replaceAll("pem$", "uuid"));
 			if (!uuidFile.exists()) {
 				FileWriter fw = new FileWriter(uuidFile);
 				fw.write(UuidGenerator.generate());
 				fw.close();
 			}
 			processVm(vmDir, prop, certFile, uuidFile);
 		}
 	}
 
 	/**
 	 * prepare VM for the given user
 	 * 
 	 * @throws CertificateException
 	 * @throws IOException
 	 * @throws Exception
 	 */
 	private void processVm(File vmDir, Properties prop, File certFile, File uuidFile) throws CertificateException, IOException, Exception {
 		// load x509 certificate
 		X509Certificate x509 = PemToolkit.readPem(certFile);
 		// get uuid
 		BufferedReader br = new BufferedReader(new FileReader(uuidFile));
 		String uuid = br.readLine().trim();
 		br.close();
 		if (!UuidGenerator.validate(uuid)) {
 			throw new RuntimeException("Bad 'uuid' in [" + vmDir.getAbsolutePath() + "/vm.cfg] : [" + uuid + "]");
 		}
 		if (uuidSet.contains(uuid)) {
 			LOG.error("uuid [" + uuid + "] is not unique.");
 			System.exit(1);
 		} else {
 			uuidSet.add(uuid);
 		}
 		String uuidHash = HashUtil.sha512UrlInBase64(uuid);
 		// output files
 		String userHash = HashUtil.sha512UrlInBase64(x509.getSubjectDN().getName());
 		File userOutDir = new File(outDir, userHash);
 		userOutDir.mkdirs();
 		File outKey = new File(userOutDir, uuidHash + ".key.cube");
 		File outDesc = new File(userOutDir, uuidHash + ".desc.cube");
 		File outData = new File(userOutDir, uuidHash + ".data.cube");
 		if (outKey.exists()) {
 			System.out.println("[VM] [" + prop.getProperty("label") + "] for user [" + x509.getSubjectDN().getName() + "]  [" + uuid
 					+ "] already packaged. delete [" + outKey.getAbsolutePath() + "] to force repackaging it.");
 			return;
 		}
 		//
 		System.out.println("[VM] " + prop.getProperty("label") + "  [" + prop.getProperty("uuid") + "]");
 		// user output dir
 		File outUserDir = new File(outDir, userHash);
 		outUserDir.mkdirs();
 		// create file B : vdi file(s), config and additional files (VPN
 		// stuff..)
 		System.out.println(" * Build data file.");
 		ArrayList<File> files = new ArrayList<File>();
 		File tmpB = File.createTempFile("cubeowner", ".data.zip");
 		for (String filename : prop.getProperty("disks").split(",")) {
 			files.add(new File(vmDir, filename));
 		}
 		files.add(new File(vmDir, "vm.cfg"));
 		files.add(new File(vmDir, prop.getProperty("vpn.ca")));
 		files.add(new File(vmDir, prop.getProperty("vpn.crt")));
 		files.add(new File(vmDir, prop.getProperty("vpn.key")));
 		zip(tmpB, files.toArray(new File[files.size()]));
 		// create file A : short description
 		System.out.println(" * Build short description file.");
 		File tmpA = File.createTempFile("cubeowner", ".data");
 		String label = prop.getProperty("label");
 		if (label == null) {
 			throw new RuntimeException("Missing 'label' in [" + vmDir.getAbsolutePath() + "/vm.cfg]");
 		}
 		String description = prop.getProperty("description");
 		if (description == null) {
 			throw new RuntimeException("Missing 'description' in [" + vmDir.getAbsolutePath() + "/vm.cfg]");
 		}
 		String classification = prop.getProperty("classification");
 		if (classification == null) {
 			throw new RuntimeException("Missing 'classification' in [" + vmDir.getAbsolutePath() + "/vm.cfg]");
 		}
 		String domain = prop.getProperty("domain");
 		if (domain == null) {
 			throw new RuntimeException("Missing 'domain' in [" + vmDir.getAbsolutePath() + "/vm.cfg]");
 		}
 		Properties shortDesc = new Properties();
 		shortDesc.put("uuid", uuid);
 		shortDesc.put("label", label);
 		shortDesc.put("domain", domain);
 		shortDesc.put("description", description);
 		shortDesc.put("classification", classification);
 		FileOutputStream fos = new FileOutputStream(tmpA);
 		shortDesc.store(fos, "VM Short Description");
 		fos.close();
 		// prepare symmetrical key
 		System.out.println(" * Prepare keys.");
 		SecretKey symKey = AESEncrypter.generateKey(256);
 		FileOutputStream fw = new FileOutputStream(outKey);
 		fw.write(RSAEncryptUtil.encrypt(symKey.getEncoded(), x509.getPublicKey()));
 		fw.close();
 		// encrypt both files
 		System.out.println(" * Encrypt both files.");
 		AESEncrypter symEnv = new AESEncrypter(symKey);
 		BufferedInputStream in1 = new BufferedInputStream(new FileInputStream(tmpA));
 		BufferedOutputStream out1 = new BufferedOutputStream(new FileOutputStream(outDesc));
 		symEnv.encrypt(in1, out1);
 		in1.close();
 		out1.close();
 		BufferedInputStream in2 = new BufferedInputStream(new FileInputStream(tmpB));
 		BufferedOutputStream out2 = new BufferedOutputStream(new FileOutputStream(outData));
 		symEnv.encrypt(in2, out2);
 		in2.close();
 		out2.close();
 		System.out.println(" * Done.");
 		// delete temporary file
 		tmpA.delete();
 		tmpB.delete();
 	}
 
 	private void zip(File out, File... files) throws IOException {
 		byte[] buf = new byte[1024];
 		ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(out));
 		//
 		for (File filename : files) {
 			FileInputStream fis = new FileInputStream(filename);
 			zos.putNextEntry(new ZipEntry(filename.getName()));
 			int len;
 			while ((len = fis.read(buf)) > 0)
 				zos.write(buf, 0, len);
 			zos.closeEntry();
 		}
 		zos.close();
 	}
 
 	private void createExampleStructure() {
 		try {
 			File exDir = new File(baseDir, "example");
 			exDir.mkdirs();
 			System.out.println("Create example VM in : " + exDir.getAbsolutePath());
 			// disk
 			File vdi = new File(exDir, "my_disk.vdi");
 			System.out.println(" - virtual disk (unformatted, 50MB) [" + vdi.getAbsolutePath() + "]");
 			fillFile(vdi, 1024 * 1024 * 50);
 			// config
 			File cfg = new File(exDir, "vm.cfg");
 			System.out.println(" - config [" + cfg.getAbsolutePath() + "]");
 			BufferedWriter bw = new BufferedWriter(new FileWriter(cfg));
 			bw.write("## Cube example VM config File\n");
 			bw.write("# uuid MUST be unique (use command 'uuidgen' to get a new one)\n");
 			bw.write("uuid=87bbec94-c3c1-4f8a-a35f-2693c58183e6\n");
 			bw.write("# domain is an infromative FQDN (SHOULD be related to VPN connectivity)\n");
 			bw.write("domain=test.org\n");
 			bw.write("# label SHOULD be short because used in tabs\n");
 			bw.write("label=Example VM\n");
 			bw.write("# description may be longer (~300 chars)\n");
 			bw.write("description=Example VM in test domain\n");
 			bw.write("# classification is UNCLASSIFIED, RESTRICTED, CONFIDENTIAL or SECRET\n");
 			bw.write("classification=UNCLASSIFIED\n");
 			bw.write("# disks is a comma separated list of VDI files\n");
 			bw.write("disks=my_disk.vdi\n");
 			bw.write("# vpn.remote: <remote host>:<port>\n");
 			bw.write("vpn.remote=vpn.example.net:12345\n");
 			bw.write("# vpn files\n");
 			bw.write("vpn.key=client-example.key\n");
 			bw.write("vpn.crt=client-example.crt\n");
 			bw.write("vpn.ca=ca-example.crt\n");
 			bw.close();
 			// user keys
			File jksFile1 = new File(System.getProperty("user.home"), "cube-pki/client1.jks");
			File jksFile0 = new File(System.getProperty("user.home"), "cube-pki/client0.jks");
 			if ( !jksFile0.exists() || !jksFile1.exists()) {
 				System.err.println("No user certificate found. please look at README-server.txt to find out how to generate them (client0.jks and client1.jks).");
 			}
 			//
 			Builder builder = KeyStore.Builder.newInstance("JKS", null, jksFile0, new KeyStore.PasswordProtection("123456".toCharArray()));
 			KeyStore keystoreTmp = builder.getKeyStore();// <- slow part
 			X509Certificate cert = (X509Certificate) keystoreTmp.getCertificate("client0-enciph");
 			File userPemFile = new File(exDir, "user0.pem");
 			System.out.println(" - user0 x509 certificate [" + userPemFile.getAbsolutePath() + "]");
 			PemToolkit.writePem(cert,userPemFile);
 			//
 			builder = KeyStore.Builder.newInstance("JKS", null, jksFile1, new KeyStore.PasswordProtection("123456".toCharArray()));
 			keystoreTmp = builder.getKeyStore();// <- slow part
 			cert = (X509Certificate) keystoreTmp.getCertificate("client1-enciph");
 			userPemFile = new File(exDir, "user1.pem");
 			System.out.println(" - user1 x509 certificate [" + userPemFile.getAbsolutePath() + "]");
 			PemToolkit.writePem(cert,userPemFile);
 			// VPN
 			File ca = new File(exDir, "ca-example.crt");
 			File crt = new File(exDir, "client-example.crt");
 			File key = new File(exDir, "client-example.key");
 			System.out.println(" - VPN : CA certificate [" + ca.getAbsolutePath() + "]");
 			System.out.println(" - VPN : client certificate [" + crt.getAbsolutePath() + "]");
 			System.out.println(" - VPN : client private key [" + key.getAbsolutePath() + "]");
 			fillFile(ca, 32);
 			fillFile(crt, 32);
 			fillFile(key, 32);
 		} catch (Exception e) {
 			LOG.error("Failed to setup example.", e);
 		}
 		System.out.println("Example VM created.. continue with normal process.\n\n");
 	}
 
 	private void fillFile(File file, int size) throws IOException {
 		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
 		for (int i = 0; i < size; i++) {
 			bos.write('0');
 		}
 		bos.close();
 	}
 
 	public static void main(String[] args) {
 		new DataOwner();
 	}
 }
