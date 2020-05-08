 package edu.bu.cs673.AwesomeAlphabet.main;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Properties;
 
 import org.apache.log4j.Logger;
 
 public class AAConfig {
 
 	private static final String CONFIG_PROPS = "config.properties";
 	private static final String BASE_DIR = "dir.location";
 	private static final String GRAPHICS_DIR = "dir.graphics";
 	private static final String SOUNDS_DIR = "dir.sounds";
 	private static final String DEFAULT_LETTERS = "prop.letters";
 	private static final String PERSISTENT_RES = "dir.persistent_resources";
 	
 	private static final ClassLoader loader = AAConfig.class.getClassLoader();
 	
 	private static String baseDirName;
 	private static String graphicsSubDir;
 	private static String soundsSubDir;
 	private static String letterPropsName;
 	private static String persistentResDir;
 	private static String cwd = System.getProperty("user.dir") + "/";
 	
 	private static Properties letterProps = null;
 	private static Properties letterPropsPersistent = null;
 	protected static Logger log = Logger.getLogger(AAConfig.class);
 	
 	static {
 		InputStream stream = loader.getResourceAsStream(CONFIG_PROPS);
 		Properties prop = new Properties();
 		try {
 			prop.load(stream);
 			baseDirName = prop.getProperty(BASE_DIR);
 			graphicsSubDir = prop.getProperty(GRAPHICS_DIR);
 			soundsSubDir = prop.getProperty(SOUNDS_DIR);
 			letterPropsName = prop.getProperty(DEFAULT_LETTERS);
 			persistentResDir = prop.getProperty(PERSISTENT_RES); 
 			stream.close();
 		} catch (IOException ioe) {
 			ioe.printStackTrace();
 			System.exit(1);
 		}
 	}
 	
 	public static String getLetterPropFileName() {
 		return letterPropsName;
 	}
 	
 	public static InputStream getGraphicsResource(String filename) {
 		InputStream is = null;
 		
 		is = loader.getResourceAsStream(baseDirName + "/" + graphicsSubDir + "/" + filename);
 		return is;
 	}
 	
 	public static InputStream getGraphicsResourcePersistent(String filename) {
 		InputStream is = null;
 		
 		try {
			String absPath = getGraphicsResourceDirAbs() + filename;
 			log.info("Loading resource=" + absPath);
 			File f = new File(absPath);
 			is = new FileInputStream(f);
 		} catch (Exception e) {
 		}
 		return is;
 	}
 	
 	public static String getResourceDirAbs() {
 		return cwd;
 	}
 	
 	public static String getResourceDirPersistentAbs() {
 		return cwd + persistentResDir + "/";
 	}
 	
 	public static String getGraphicsResourceDir() {
 		return baseDirName + "/" + graphicsSubDir + "/";
 	}
 	
 	public static String getGraphicsResourceDirAbs() {
 		return cwd + baseDirName + "/" + graphicsSubDir + "/";
 	}
 	
 	public static String getGraphicsResourceDirPersistentAbs() {
 		return cwd + persistentResDir + "/" + graphicsSubDir + "/";
 	}
 
 	public static InputStream getSoundResource(String filename) {
 		
 		return loader.getResourceAsStream(baseDirName + "/" + soundsSubDir + "/" + filename);
 	}
 	
 	public static InputStream getSoundResourcePersistent(String filename) {
 		InputStream is = null;
 		
 		try {
 			String absPath = getSoundResourceDirPersistentAbs() + filename;
 			log.info("Loading sound resource=" + absPath);
 			File f = new File(absPath);
 			is = new FileInputStream(f);
 		} catch (Exception e) {
 		}
 		return is;
 	}
 	
 	public static String getSoundResourceDir() {
 		return baseDirName + "/" + soundsSubDir + "/";
 	}
 	
 	public static String getSoundResourceDirAbs() {
 		return cwd + baseDirName + "/" + soundsSubDir + "/";
 	}
 	
 	public static String getSoundResourceDirPersistentAbs() {
 		return cwd + persistentResDir + "/" + soundsSubDir + "/";
 	}
 
 	public static Properties getLetterProps() {
 		InputStream is;
 		
 		if (letterProps == null) {
 			try {
 				letterProps = new Properties();
 				is = loader.getResourceAsStream(letterPropsName);
 				letterProps.load(is);
 			} catch (IOException ioe) {
 				ioe.printStackTrace();
 				System.exit(1);
 			}
 		}
 		
 		return letterProps;
 	}
 	
 	public static Properties getLetterPropsPersistent() {
 		InputStream is;
 		
 		if (letterPropsPersistent == null) {
 			try {
 				letterPropsPersistent = new Properties();
 				is = new FileInputStream(new File(getResourceDirPersistentAbs() + letterPropsName));
 				letterPropsPersistent.load(is);
 			} catch (IOException ioe) {
 				ioe.printStackTrace();
 				System.exit(1);
 			}
 		}
 		
 		return letterPropsPersistent;
 	}
 	
 	public static int copy_stream(InputStream inStream, OutputStream outStream)
 	{
 		byte[] buffer = new byte[1024];
 	    int length;
 	  
 	    try {
 	    	//copy the file content in bytes
 	    	while ((length = inStream.read(buffer)) > 0) {
 	    		outStream.write(buffer, 0, length);
 	    	}
 	    } catch (Exception e) {
 	    	e.printStackTrace();
 	    }
 	    
 	    return 0;
 	}
 	
 	/** copy file
 	 * @param srcFileName: Full path to source file
 	 * @param dstFileName : Full path to dest file
 	 */
 	public static int copy_file(String srcFileName, String destFileName)
 	{
 		InputStream inStream = null;
 		OutputStream outStream = null;
 		
 		log.info("copy: " + srcFileName + " to " + destFileName);
 		
     	try {
     		 
     		File sfile = new File(srcFileName);
     		File dfile = new File(destFileName);
     		
     		/* Create dest file */
     		if (!dfile.exists()) {
     			//log.info("Trying to create file:" + dfile.getPath());
     			if (!dfile.getParentFile().exists()) {
     			//	log.info("Parent dir does not exist. Creating:"+ dfile.getParentFile().getPath());
     				dfile.getParentFile().mkdirs();
     			} else 
     				//log.info("Parent file (dir) exists");
     			dfile.createNewFile();
     		}
     		
     		inStream = new FileInputStream(sfile);
     		outStream = new FileOutputStream(dfile);
     		copy_stream(inStream, outStream);
  
     	    inStream.close();
     	    outStream.close();
  
     	} catch(IOException e) {
     		e.printStackTrace();
     	}
 		return 0;
 	}
 
 	/** copy resource to a file
 	 * @param resource: resource file name
 	 * @param dstFileName : Full path to dest file
 	 * @param type: type of resource. 0 - top level dir resource, 1 - graphics resource, 2- sound resource
 	 */
 	public static boolean copy_res_to_file(String resource, String destFileName, int type)
 	{
 		InputStream is;
 		OutputStream os;
 		String resource_path;
 		
 		//log.info("copy_res_to_file: " + resource + " to " + destFileName + " type=" + type);
 		
 		if (type == 0)
 			resource_path = resource;
 		else if (type == 1)
 			resource_path = baseDirName + "/" + graphicsSubDir + "/" + resource;
 		else if (type == 2)
 			resource_path = baseDirName + "/" + soundsSubDir + "/" + resource;
 		else
 			return false;
 		
 		try {
 			is = loader.getResourceAsStream(resource_path);
 			if (is == null) {
 				log.error("Failed to get resource as stream");
 				return false;
 			}
 			File dfile = new File(destFileName);
     		if (!dfile.exists()) {
     			if (!dfile.getParentFile().exists())
     				dfile.getParentFile().mkdirs();
     			dfile.createNewFile();
     		}
     		
     		os = new FileOutputStream(dfile);
     		copy_stream(is, os);
  
     	    is.close();
     	    os.close(); 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return true;
 	}
 	
 	/** copy dir
 	 * @param srcDirName: Absolute path to source Dir
 	 * @param dstDirName : Absolute path to dest Dir
 	 */
 	public static int copy_dir(String srcDirName, String destDirName)
 	{
 		File src = new File(srcDirName);
 		File dest = new File(destDirName);
 		
 		if (!src.exists()) {
 			log.error("src=" + srcDirName + "does not exist");
 			return 1;
 		}
 		
 		if (!src.isDirectory()) {
 			log.error("src=" + srcDirName + "is not a direcotyr");
 			return 1;
 		}
 		
 		if (!dest.exists()) {
 			log.error("dest=" + destDirName + "does not exist");
 			return 1;
 		}
 		
 		if (!dest.isDirectory()) {
 			log.error("dest=" + destDirName + "is not a direcotyr");
 			return 1;
 		}
 		
 		String files[] = src.list();
 		
 		for (String file : files) {
  		   String srcFile = srcDirName + "/" + file;
  		   String destFile = destDirName + "/" + file;
  		   
  		   copy_file(srcFile, destFile);
  		}
 		
 		return 0;
 	}
 	
 	/** Add a sound resource file.
 	 * @param srcFileName: Full path name to source sound file
 	 * @param destFileName: file name (no path info) of dest file
 	 * @return
 	 */
 	public static int addSoundResource(String srcFileName, String destFileName) {
 		return copy_file(srcFileName, getSoundResourceDirPersistentAbs() + destFileName);
 	}
 	
 	/** Add a word image file.
 	 * @param srcFileName: Full path name to source image file
 	 * @param destFileName: file name (no path info) of dest file
 	 * @return
 	 */
 	public static int addImageResource(String srcFileName, String destFileName) {
 		return copy_file(srcFileName, getGraphicsResourceDirPersistentAbs() + destFileName);
 	}
 
 	/** Add a word to letter.properties file
 	 * @param srcFileName: Full path name to source image file
 	 * @param destFileName: file name (no path info) of dest file
 	 * @return
 	 */
 	public static int addWordToIndex(char letter, String wordText, String Theme) {
 		int i = 1;
 	
 		try {
 			Properties propsP = getLetterPropsPersistent();
 			File outputFile = new File(getResourceDirPersistentAbs() + letterPropsName + ".temp");
 			File destFile = new File(getResourceDirPersistentAbs() + letterPropsName);
 			OutputStream outStream;
 			
 			log.info("Temp index file is:" + outputFile);
 			log.info("Dest index file is:" + destFile);
 			if (!outputFile.exists()) {
 				outputFile.createNewFile();
 			}
 			
 			outStream = new FileOutputStream(outputFile);
 			
 			while (true) {
 				if (propsP.getProperty("letter." + letter + "." + i + ".word") == null)
 					break;
 				i++;
 			}
 			
 			propsP.setProperty("letter." + letter + "." + i + ".word", wordText);
 			propsP.setProperty("letter." + letter + "." + i + ".theme", Theme);
 			propsP.store(outStream, null);
 			outStream.close();
 			outputFile.renameTo(destFile);
 			outputFile.delete();
 		} catch(IOException e) {
 			e.printStackTrace();
 		}
 		return 0;
 	}
 	
 	/** Remove a sound resource file.
 	 * @param srcFileName: file name to source sound file (no dir)
 	 * @return 0 on success.
 	 */
 	public static int removeSoundResource(String srcFileName) {
 		int ret = 0;
 		boolean rc;
 		
 		try {
 			File file = new File(getSoundResourceDirPersistentAbs() + srcFileName);
 			rc = file.delete();
 			if (!rc)
 				ret = 1;
 		} catch(Exception e) {
 			e.printStackTrace();
 		}
 		
 		return ret;
 	}
 	
 	/** Remove a image resource file.
 	 * @param srcFileName: file name to source sound file (no dir)
 	 * @return 0 on success.
 	 */
 	public static int removeImageResource(String srcFileName) {
 		int ret = 0;
 		boolean rc;
 		
 		try {
 			File file = new File(getGraphicsResourceDirPersistentAbs() + srcFileName);
 			rc = file.delete();
 			if (!rc)
 				ret = 1;
 		} catch(Exception e) {
 			e.printStackTrace();
 		}
 		
 		return ret;
 	}
 	
 	/** Remove a word from letter.properties file
 	 * @param letter char word belongs to
 	 * @param actual word string
 	 * @return 0 on success
 	 */
 	public static int removeWordFromIndex(char letter, String wordText) {
 		int i = 1;
 	
 		try {
 			Properties propsP = getLetterPropsPersistent();
 			File outputFile = new File(getResourceDirPersistentAbs() + letterPropsName + ".temp");
 			File destFile = new File(getResourceDirPersistentAbs() + "/" + letterPropsName);
 			OutputStream outStream;
 			int tablesize = propsP.size();
 			boolean found = false;
 			
 			
 			log.info("Temp index file is:" + outputFile);
 			log.info("Dest index file is:" + destFile);
 			
 			if (!outputFile.exists()) {
 				outputFile.createNewFile();
 			}
 			
 			outStream = new FileOutputStream(outputFile);
 			
 			while (true) {
 				if (propsP.getProperty("letter." + letter + "." + i + ".word") == wordText) {
 					found = true;
 					break;
 				}
 				
 				i++;
 				if (i > tablesize)
 					break;
 			}
 			
 			if (!found) {
 				outStream.close();
 				return 1;
 			}
 			
 			propsP.remove("letter." + letter + "." + i + ".word");
 			propsP.remove("letter." + letter + "." + i + ".theme");
 			
 			propsP.store(outStream, null);
 			outStream.close();
 			outputFile.renameTo(destFile);
 			outputFile.delete();
 		} catch(IOException e) {
 			e.printStackTrace();
 		}
 		return 0;
 	}
 }
