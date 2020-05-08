 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.OutputStream;
 import java.nio.CharBuffer;
 import java.nio.charset.Charset;
 import java.security.MessageDigest;
 import java.security.spec.KeySpec;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Iterator;
 import java.io.Serializable;
 
 import javax.crypto.Cipher;
 import javax.crypto.CipherInputStream;
 import javax.crypto.CipherOutputStream;
 import javax.crypto.NoSuchPaddingException;
 import javax.crypto.SecretKey;
 import javax.crypto.SecretKeyFactory;
 import javax.crypto.spec.IvParameterSpec;
 import javax.crypto.spec.PBEKeySpec;
 import javax.crypto.spec.PBEParameterSpec;
 import javax.crypto.spec.SecretKeySpec;
 import javax.crypto.SealedObject;
 
 public class FileStorage {
 
 	public static final File ROOTDIR = new File(
 			System.getProperty("user.home"), "CMS_Files");
 
 
 	private PBEKeySpec keySpec;
 
 	private ObjectFileStrategy ofStrategy;
 	private StreamFileStrategy sfStrategy;
 
 	// Time format
 
 	public FileStorage(char[] password) {
 		try {
 			//aesCipher = Cipher.getInstance("AES");
 			//keySetup(password);
 			keySpecInit(password);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		ofStrategy = new CryptoObjectStrategy(); //Determine if objects should be encrypted when stored.
 		//ofStrategy = new PlainObjectStrategy();
 
 		sfStrategy = new CryptoStreamStrategy();
 	}
 
 	private Cipher createCipher(int mode) throws Exception {
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5andDES");
 		SecretKey key = keyFactory.generateSecret(keySpec);
 		MessageDigest md = MessageDigest.getInstance("MD5");
 		md.update("input".getBytes());
 		byte[] digest = md.digest();
 		byte[] salt = new byte[8];
 		for (int i = 0; i < 8; ++i)
 			salt[i] = digest[i];
 		PBEParameterSpec paramSpec = new PBEParameterSpec(salt, 20);
		Cipher cipher = Cipher.getInstance("PBEWithMD5andDES");
 		cipher.init(mode, key, paramSpec);
 		return cipher;
 	}
 
 	private void keySpecInit(char[] password) throws Exception{
 		this.keySpec = new PBEKeySpec(password);
 	}
 
 
 
 	private static void copy(InputStream input, OutputStream output, int size)
 		throws IOException {
 		byte[] buffer = new byte[1024];
 		int countdown = size;
 		int n = 0;
 		while (countdown > 20) {
 			n = input.read(buffer, 0, 1024);
 			output.write(buffer, 0, n);
 			countdown -= n;
 		}
 		output.flush();
 	}
 
 	public void saveFile(InputStream is, String filename, int filesize,
 			String username) {
 		//Initialize Cipher
 		//This looks dirty. try putting most of the method in the same try or letting this throw.
 		Cipher cipher=null;
 		try {
 			//aesCipher.init(Cipher.ENCRYPT_MODE, secret);
 			//iv = aesCipher.getParameters()
 			//	.getParameterSpec(IvParameterSpec.class).getIV();
 			cipher = createCipher(Cipher.ENCRYPT_MODE);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		//Keep file in user directory
 		File userhome = new File(ROOTDIR, username);
 		//if the user home doesn't exist, something is wrong. get out.
 		if (!userhome.exists())
 			return;
 
 
 		try {
 			//file is just the filename in the user's home directory.
 			File outputFile = new File(userhome, new File(filename).getName());
 
 			//Set up streams
 			FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
 			CipherOutputStream os = new CipherOutputStream(fileOutputStream, cipher);
 			//InputStream cis = is;
 
 			//Save the file
 			copy(is, os, filesize);
 
 
 			//Close the streams
 			fileOutputStream.close();
 			os.close();
 
 			this.decrypt(new File(filename).getName(),new File(filename).getName()+".decrypt",username);
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	public int getFileSize(String courseName, String filename){
 		return (int)  new File(ROOTDIR,courseName+"/"+filename).length();
 	}
 
 	public InputStream getStreamFromFile(String courseName, String filename)
 		throws Exception{
 		return sfStrategy.getStreamFromFile(courseName, filename);
 	}
 
 	public void saveStreamToFile(String coursename, String filename,
 			InputStream is, int filesize) throws Exception{
 		sfStrategy.saveStreamToFile(coursename, filename, is, filesize);
 	}
 
 	private interface StreamFileStrategy{
 		public InputStream getStreamFromFile(String courseName, String filename) throws Exception;
 		public void saveStreamToFile (String courseName, String filename, 
 				InputStream is, int filesize) throws Exception;
 
 	}
 
 	private class CryptoStreamStrategy implements StreamFileStrategy{
 
 		public InputStream getStreamFromFile(String courseName, String filename)
 			throws Exception{
 			CipherInputStream cis = null;
 
 			Cipher cipher = createCipher(Cipher.DECRYPT_MODE);
 			File in = new File(ROOTDIR,
 					courseName+"/"+filename);
 			cis = new CipherInputStream(
 					new FileInputStream(in),
 					cipher);
 
 			return cis;
 
 		}
 
 		public void saveStreamToFile(String courseName, String filename,
 				InputStream is, int filesize) throws Exception{
 			//Initialize Cipher
 			Cipher cipher = createCipher(Cipher.ENCRYPT_MODE);
 
 			//file is just the filename in the user's home directory.
 			File courseRoot = new File(ROOTDIR,courseName);
 			File outputFile = new File(courseRoot,filename);
 
 			//Set up streams
 			FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
 			CipherOutputStream os = new CipherOutputStream(fileOutputStream, cipher);
 			//InputStream cis = is;
 
 			//Save the file
 			copy(is, os, filesize);
 
 			//Close the streams
 			fileOutputStream.close();
 			os.close();
 
 		}
 	}
 
 	public void makeDir(String courseName, String subdir){
 		File courseRoot = new File(ROOTDIR,courseName);
 		new File(courseRoot,subdir).mkdirs();
 	}
 
 	public void saveObjectToFile(Object obj, String filename){
 		ofStrategy.saveObjectToFile(obj, filename);
 	}
 
 	public Object readObjectFromFile(String filename){
 		return ofStrategy.readObjectFromFile(filename);
 	}
 
 	private interface ObjectFileStrategy{
 		//TODO readObject should probably throw an exception
 		public void saveObjectToFile(Object obj, String filename);
 		public Object readObjectFromFile(String filename);
 	}
 
 	private class CryptoObjectStrategy implements ObjectFileStrategy{
 		public void saveObjectToFile(Object obj, String filename){
 			//Initialize Cipher
 			try {
 				Cipher cipher = createCipher(Cipher.ENCRYPT_MODE);
 				//file should use whole path.
 				File outputFile = new File(filename);
 
 				//Set up streams	
 				FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
 				//CipherOutputStream os = new CipherOutputStream(fileOutputStream, cipher);
 				ObjectOutputStream oos = new ObjectOutputStream(fileOutputStream);
 				SealedObject sealed = new SealedObject((Serializable) obj, cipher);
 
 				//Save the file
 				oos.writeObject(sealed);
 
 				//Close the streams
 				fileOutputStream.close();
 				oos.close();
 
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 
 		}
 
 		public Object readObjectFromFile(String filename){
 			SealedObject sealed = null;
 			Object obj = null;
 			try {
 
 				Cipher cipher = createCipher(Cipher.DECRYPT_MODE);
 
 				//Setup Streams.
 				//CipherInputStream cis = new CipherInputStream(
 				//		new FileInputStream(filename), cipher);
 				FileInputStream is = new FileInputStream(filename);
 				ObjectInputStream ois = new ObjectInputStream(is);
 
 				//Read the object.
 				sealed = (SealedObject) ois.readObject();
 				obj = sealed.getObject(cipher);
 
 				//Close the streams.
 				ois.close();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			return obj;
 
 		}
 	}
 
 	private class PlainObjectStrategy implements ObjectFileStrategy{
 		public void saveObjectToFile(Object obj, String filename){
 
 			try{	
 				ObjectOutputStream oos = new ObjectOutputStream(
 						new FileOutputStream(filename));
 				oos.writeObject(obj);
 				oos.close();
 			} catch (Exception e){
 				e.printStackTrace();
 			}
 		}
 
 		public Object readObjectFromFile(String filename){
 			Object obj = null;
 			try{
 				ObjectInputStream ois = new ObjectInputStream(
 						new FileInputStream(filename));
 				obj = ois.readObject();
 				ois.close();
 			} catch (Exception e){
 				e.printStackTrace();
 			}
 			return obj;
 		}
 	}
 
 	//TODO
 	//For Debugging. Remove when safe.
 	private void decrypt(String infilename, String outfilename, String username) {
 		try {
 			Cipher cipher = createCipher(Cipher.DECRYPT_MODE);
 
 			File userHome = new File(ROOTDIR,username);			
 			File out = new File(userHome,outfilename);
 			File in = new File(userHome,infilename);
 			CipherOutputStream os = new CipherOutputStream(
 					new FileOutputStream(out), cipher);
 			FileInputStream is = new FileInputStream(in);
 
 			copy(is, os, (int)in.length());
 
 			os.close();
 			is.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	//TODO
 	//For debugging. Remove when safe.
 	private void decrypt(String infilename, String outfilename){
 		try {
 			Cipher cipher = createCipher(Cipher.DECRYPT_MODE);
 
 			File out = new File(outfilename);
 			File in = new File(infilename);
 			CipherOutputStream os = new CipherOutputStream(
 					new FileOutputStream(out), cipher);
 			FileInputStream is = new FileInputStream(in);
 
 			copy(is, os, (int)in.length());
 
 			os.close();
 			is.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	public void updateCourse(Course course){
 		File courseRoot = new File(ROOTDIR,course.name());
 		courseRoot.mkdirs();
 		this.saveObjectToFile(course,
 				new File(courseRoot.getAbsolutePath(), course.name())
 				.getAbsolutePath()+".dat");
 
 
 	}
 
 	public Course getCourse(String courseName){
 		File courseRoot = new File(ROOTDIR,courseName);
 		String courseFileName = new File(courseRoot,
 				courseName+".dat").getAbsolutePath();
 		return (Course) this.readObjectFromFile(courseFileName);
 
 	}
 
 	private static void testStreamFile(FileStorage fs)
 		throws Exception{
 		File in = new File("inputfile");
 		FileInputStream fis = new FileInputStream(in);
 		fs.saveStreamToFile("ECE4750","inputfile",fis,(int) in.length());
 		if( (int)in.length() != fs.getFileSize("ECE4750","inputfile"))
 			System.out.println("File sizes differ");
 
 		InputStream is = fs.getStreamFromFile("ECE4750","inputfile");
 		FileOutputStream fos = new FileOutputStream("outfile");
 
 		byte[] buf = new byte[1024];
 		int bytesRead;
 		while( (bytesRead = is.read(buf)) != -1){
 			fos.write(buf, 0, bytesRead);
 		}
 		fos.close();
 
 	}
 
 
 	public static void main(String[] args) {
 		FileStorage fs = new FileStorage("testpass".toCharArray());
 // 		try{
 // 			testStreamFile(fs);
 // 		} catch (Exception e){
 // 			e.printStackTrace();
 // 		}
 
 	   try{
 	   int a = 123;
 	   fs.saveObjectToFile(a,"a.dat");
 	   Object obj = fs.readObjectFromFile("a.dat");
 	   int b = (int) obj;
 	   int c = b+2;
 	   System.out.println(c);
 	   } catch (Exception e){
 	   e.printStackTrace();
 	   }
 
 		/*
 		   Course course = new Course("Alice","ECE4750");
 
 		   course.addUser("Bob","TA");
 		   course.addUser("StinkyTom","Student");
 
 		   course.addHandout("Syllabus.pdf");
 		   course.addHandout("Hennessy_Patterson.pdf");
 
 		   course.createAssignment("SuperHardLab.pdf");
 		   course.createAssignment("BusyWork1.pdf");
 
 		   fs.updateCourse(course);
 
 
 		   Course courseLoaded=fs.getCourse("ECE4750");
 
 
 		   System.out.println(courseLoaded.name());
 		   System.out.println("--------------------------------------------------");
 
 		   System.out.println("StinkyTom's Assignments: ");
 		   Iterator<Assignment> tomsAssignments = courseLoaded.getAssignmentsFor("StinkyTom").iterator();
 		   while(tomsAssignments.hasNext()){
 		   System.out.println(tomsAssignments.next().assignmentName);
 		   }
 		   System.out.println("--------------------------------------------------");
 
 		   System.out.println("Checking StinkyTom's BusyWork");
 		   HashMap<String,Assignment> busyWorkSubmissions = courseLoaded.getSubmissions("BusyWork1.pdf");
 		   Assignment tomsSubmission = busyWorkSubmissions.get("StinkyTom");
 		   if(!tomsSubmission.submitted)
 		   System.out.println("Lazy fool hasn't done it yet.");
 		   System.out.println("--------------------------------------------------");
 
 		   System.out.println("Get handouts: ");
 		   Iterator<String> handouts = courseLoaded.getHandouts().iterator();
 		   while(handouts.hasNext())
 		   System.out.println(handouts.next());
 		   System.out.println("--------------------------------------------------");
 		   */
 
 	}
 
 }
