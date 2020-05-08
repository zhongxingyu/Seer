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
 	
 	private static final ClassLoader loader = AAConfig.class.getClassLoader();
 	
 	private static String baseDirName;
 	private static String graphicsSubDir;
 	private static String soundsSubDir;
 	private static String letterPropsName;
 	
 	private static Properties letterProps = null;
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
 			stream.close();
 		} catch (IOException ioe) {
 			ioe.printStackTrace();
 			System.exit(1);
 		}
 	}
 	
 	public static InputStream getGraphicsResource(String filename) {
 		InputStream is = null;
 		File currentdir = new File(".");
 		
 	//	is = loader.getResourceAsStream(baseDirName + "/" + graphicsSubDir + "/" + filename);
 		try {
 			String absPath = currentdir.getCanonicalPath() + "/" + baseDirName + "/" + graphicsSubDir + "/" + filename;
 			log.info("Loading resource=" + absPath);
 			File f = new File(absPath);
 			is = new FileInputStream(f);
 		} catch (Exception e) {
 		}
 		return is;
 	}
 	
 	public static String getGraphicsResourceDir() {
 		return baseDirName + "/" + graphicsSubDir + "/";
 	}
 
 	public static InputStream getSoundResource(String filename) {
 		
		return loader.getResourceAsStream(baseDirName + "/" + soundsSubDir + "/" + filename);
		/*
 		InputStream is = null;
 		File currentdir = new File(".");
 		
 		try {
 			String absPath = currentdir.getCanonicalPath() + "/" + baseDirName + "/" + soundsSubDir + "/" + filename;
			log.info("Loading resource=" + absPath);
 			File f = new File(absPath);
 			is = new FileInputStream(f);
 		} catch (Exception e) {
 		}
 		return is;
		*/
 	}
 	
 	public static String getSoundResourceDir() {
 		return baseDirName + "/" + soundsSubDir + "/";
 	}
 	
 
 	public static Properties getLetterProps() {
 		File currentdir = new File(".");
 		String absPath;
 		File f;
 		InputStream is;
 		
 		if (letterProps == null) {
 			try {
 				letterProps = new Properties();
 				absPath = currentdir.getCanonicalPath() + "/" + letterPropsName;
 				//InputStream is = loader.getResourceAsStream(letterPropsName);
 				log.info("Loading resource=" + absPath);
 				f = new File(absPath);
 				is = new FileInputStream(f);
 				letterProps.load(is);
 			} catch (IOException ioe) {
 				ioe.printStackTrace();
 				System.exit(1);
 			}
 		}
 		
 		return letterProps;
 	}
 	
 	/** copy file
 	 * @param srcFileName: Full path to source file
 	 * @param dstFileName : Full path to dest file
 	 */
 	public static int copy_file(String srcFileName, String destFileName)
 	{
 		InputStream inStream = null;
 		OutputStream outStream = null;
 		
 		File currentdir = new File(".");
 		try {
 			log.info("CWD is:" + currentdir.getCanonicalPath());
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 			log.info("copy: " + srcFileName + " to " + destFileName);
 		
     	try {
     		 
     		File sfile = new File(srcFileName);
     		File dfile = new File(currentdir.getCanonicalPath() + "/" + destFileName);
     		
     		/* Create dest file */
     		if (!dfile.exists()) {
     			log.info("Trying to create file:" + dfile.getPath());
     			if (!dfile.getParentFile().exists()) {
     				log.info("Parent dir does not exist. Creating:"+ dfile.getParentFile().getPath());
     				dfile.getParentFile().mkdirs();
     			} else 
     				log.info("Parent file (dir) exists");
     			dfile.createNewFile();
     		}
     		
     		inStream = new FileInputStream(sfile);
     		outStream = new FileOutputStream(dfile);
  
     	    byte[] buffer = new byte[1024];
  
     	    int length;
     	    
     	    //copy the file content in bytes 
     	    while ((length = inStream.read(buffer)) > 0) {
     	    	outStream.write(buffer, 0, length);
     	    }
  
     	    inStream.close();
     	    outStream.close();
  
     	} catch(IOException e) {
     		e.printStackTrace();
     	}
 		return 0;
 	}
 	
 	/** Add a sound resource file.
 	 * @param srcFileName: Full path name to source sound file
 	 * @param destFileName: file name (no path info) of dest file
 	 * @return
 	 */
 	public static int addSoundResource(String srcFileName, String destFileName) {
 		return copy_file(srcFileName, baseDirName + "/" + soundsSubDir + "/" + destFileName);
 	}
 	
 	/** Add a word image file.
 	 * @param srcFileName: Full path name to source image file
 	 * @param destFileName: file name (no path info) of dest file
 	 * @return
 	 */
 	public static int addImageResource(String srcFileName, String destFileName) {
 		return copy_file(srcFileName, baseDirName + "/" + graphicsSubDir + "/" + destFileName);
 	}
 
 	/** Add a word to letter.properties file
 	 * @param srcFileName: Full path name to source image file
 	 * @param destFileName: file name (no path info) of dest file
 	 * @return
 	 */
 	public static int addWordToIndex(char letter, String wordText, String Theme) {
 		int i = 1;
 	
 		try {
 			Properties props = getLetterProps();
 			File currentdir = new File(".");
 			File outputFile = new File(currentdir.getCanonicalPath() + "/" + letterPropsName + ".temp");
 			File destFile = new File(currentdir.getCanonicalPath() + "/" + letterPropsName);
 			OutputStream outStream;
 			
 			log.info("Temp index file is:" + outputFile);
 			log.info("Dest index file is:" + destFile);
 			if (!outputFile.exists()) {
 				outputFile.createNewFile();
 			}
 			
 			outStream = new FileOutputStream(outputFile);
 			
 			while (true) {
 				if (props.getProperty("letter." + letter + "." + i + ".word") == null)
 					break;
 				i++;
 			}
 			
 			props.setProperty("letter." + letter + "." + i + ".word", wordText);
 			props.setProperty("letter." + letter + "." + i + ".theme", Theme);
 			props.store(outStream, null);
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
 		File currentdir = new File(".");
 		
 		try {
 			File file = new File(currentdir.getCanonicalPath() + "/" + baseDirName + "/" + soundsSubDir + "/" + srcFileName);
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
 		File currentdir = new File(".");
 		
 		try {
 			File file = new File(currentdir.getCanonicalPath() + "/" + baseDirName + "/" + graphicsSubDir + "/" + srcFileName);
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
 			Properties props = getLetterProps();
 			File currentdir = new File(".");
 			File outputFile = new File(currentdir.getCanonicalPath() + "/" + letterPropsName + ".temp");
 			File destFile = new File(currentdir.getCanonicalPath() + "/" + letterPropsName);
 			OutputStream outStream;
 			int tablesize = props.size();
 			boolean found = false;
 			
 			
 			log.info("Temp index file is:" + outputFile);
 			log.info("Dest index file is:" + destFile);
 			
 			if (!outputFile.exists()) {
 				outputFile.createNewFile();
 			}
 			
 			outStream = new FileOutputStream(outputFile);
 			
 			while (true) {
 				if (props.getProperty("letter." + letter + "." + i + ".word") == wordText) {
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
 			
 			props.remove("letter." + letter + "." + i + ".word");
 			props.remove("letter." + letter + "." + i + ".theme");
 			
 			props.store(outStream, null);
 			outStream.close();
 			outputFile.renameTo(destFile);
 			outputFile.delete();
 		} catch(IOException e) {
 			e.printStackTrace();
 		}
 		return 0;
 	}
 }
