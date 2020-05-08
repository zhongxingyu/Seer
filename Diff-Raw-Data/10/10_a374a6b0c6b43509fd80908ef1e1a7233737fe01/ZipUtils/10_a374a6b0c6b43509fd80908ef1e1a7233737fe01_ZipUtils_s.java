 package framework.utils;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Enumeration;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 import java.util.zip.ZipInputStream;
 import java.util.zip.ZipOutputStream;
 
import com.j_spaces.kernel.PlatformVersion;
 import org.apache.commons.io.IOUtils;
 
 import framework.tools.SGTestHelper;
 
 public class ZipUtils {

     public static void unzipArchive(String testName, String suiteName) {
        String buildNumber = PlatformVersion.getBuildNumber();
        File buildFolder = new File(SGTestHelper.getSGTestRootDir()+"/deploy/local-builds/build_" + buildNumber);
         File testFolder = null;
         testFolder = new File(buildFolder +"/" + suiteName + "/" +testName);
         File[] children = testFolder.listFiles();
         if (children == null)
             return;
         for (int n = 0; n < children.length; n++) {
             File file = children[n];
             if (file.getName().contains(".zip")) {
                 unzipArchive(file, testFolder.getAbsoluteFile());
                 file.delete();
             }
         }
     }
 
     public static void unzipArchive(File archive, File outputDir) {
         try {
             ZipFile zipfile = new ZipFile(archive);
             for (Enumeration e = zipfile.entries(); e.hasMoreElements();) {
                 ZipEntry entry = (ZipEntry) e.nextElement();
                 unzipEntry(zipfile, entry, outputDir);
             }
         } catch (Exception e) {
             LogUtils.log(e.getMessage(), e);
         }
     }
 
     private static void unzipEntry(ZipFile zipfile, ZipEntry entry, File outputDir) throws IOException {
 
         if (entry.isDirectory()) {
             createDir(new File(outputDir, entry.getName()));
             return;
         }
 
         File outputFile = new File(outputDir, entry.getName());
         if (!outputFile.getParentFile().exists()) {
             createDir(outputFile.getParentFile());
         }
 
         BufferedInputStream inputStream = new BufferedInputStream(zipfile.getInputStream(entry));
         BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));
 
         try {
             IOUtils.copy(inputStream, outputStream);
         } finally {
             outputStream.close();
             inputStream.close();
         }
     }
 
     private static void createDir(File dir) {
         if (!dir.mkdirs()) throw new RuntimeException("Can not create dir " + dir);
     }
     
     public static boolean isZipIntact(File zip) {
     	boolean intact = false;
     	
     	try {
 	    	final ZipInputStream test = new ZipInputStream(new FileInputStream(zip));
 	    	try {
 	    		test.read();
 	    		intact = true;
 	    	}
 	    	finally {
 				test.close();
 	    	}
     	}
     	catch (IOException e) {
     		//ignore
     	}
     	
     	return intact;
     }
 
     /**
      * Compresses a directory (including subdirectories) into a zip file.
      * The directory's name is included in the zip, higher level directories are excluded
      * @param in - input directory to zip
      * @param out - output zip stream
      * @param excludeDirectories - directories to exclude
      * @throws IOException
      */
     public static void zipDirectory(File out, File in, File[] includeDirectories, File[] excludeDirectories, File[] emptyDirectories, String rootDirectoryName) throws IOException {
 		FileOutputStream fos = new FileOutputStream(out);
 		fos.getChannel().lock();
 		ZipOutputStream zos = new ZipOutputStream(fos);
 		
 		try {
 			
 	    	Set<String> includes = convertFileArrayToStringSet(includeDirectories);
 	    	Set<String> excludes = convertFileArrayToStringSet(excludeDirectories);
 	    	String prefixToRemove = in.getCanonicalPath();
 	    	if (rootDirectoryName == null || rootDirectoryName.length() == 0)  {
 	    		// same directory name as the current directory name.
 	    		rootDirectoryName = in.getCanonicalFile().getName();
 	    	}
 			zipDirectoryRecursive(zos,in,includes,excludes,prefixToRemove,rootDirectoryName);
 	    	
 			// add empty directories
 			for (File emptyDir : emptyDirectories) {
 				String filename = fixPath(emptyDir,prefixToRemove, rootDirectoryName);
 				String separator = System.getProperty("file.separator");    	
 				ZipEntry dirEntry = new ZipEntry(filename+separator);
 				zos.putNextEntry(dirEntry);
 	    	}
 		}
 		finally {
 			zos.close();
 		}
     }
 
 	private static String fixPath(File file, String prefixToRemove,String prefixToAdd)
 			throws IOException {
 		String filename = file.getCanonicalPath();
 		if (!filename.startsWith(prefixToRemove)) {
 			 throw new IllegalStateException(filename + " does not start with " + prefixToRemove);
 		}
 		filename = prefixToAdd + filename.substring(prefixToRemove.length());
 		if (filename.startsWith("/") || filename.startsWith("\\")) {
 		 	filename = filename.substring(1);
 		}
 		
 		return filename;
 	}
 
     private static Set<String> convertFileArrayToStringSet(File[] files) throws IOException {
     	final Set<String> set = new HashSet<String>();
     	for (final File file : files) {
     		set.add(file.getCanonicalPath());
     	}
     	return set;
     }
     
     // @see http://www.devx.com/tips/Tip/14049   
     private static void zipDirectoryRecursive(ZipOutputStream zos, File in, Set<String> includes, Set<String> excludes, String prefixToRemove, String prefixToAdd) throws IOException {
     	
     	boolean exclude = isExcluded(includes, excludes, in);
     	
          //get a listing of the directory content 
          String[] dirList = in.list(); 
          byte[] readBuffer = new byte[1000]; 
          int bytesIn = 0; 
          //loop through dirList, and zip the files 
          for(int i=0; i<dirList.length; i++) 
          { 
              File file = new File(in, dirList[i]); 
              if(file.isDirectory()) 
              { 
                  //recursive 
             	 zipDirectoryRecursive(zos, file, includes, excludes, prefixToRemove, prefixToAdd); 
              }
              else if (!exclude) {
                  FileInputStream fis = new FileInputStream(file); 
                  if (fis.getChannel().tryLock(0, Long.MAX_VALUE, true) == null) {
                 	 throw new IOException("Cannot obtain shared read lock on " + file);
                  }
                  try {
                 	 String filename = fixPath(file,prefixToRemove,prefixToAdd);
                      ZipEntry anEntry = new ZipEntry(filename); 
                      //place the zip entry in the ZipOutputStream object 
                      zos.putNextEntry(anEntry); 
                      //now write the content of the file to the ZipOutputStream 
                      while((bytesIn = fis.read(readBuffer)) != -1) 
                      { 
                          zos.write(readBuffer, 0, bytesIn); 
                      } 
                  } 
                  finally {
                     //close the Stream 
                     fis.close();
                  }
              }
          }
     }
 
 	private static boolean isExcluded(Set<String> includes,
 			Set<String> excludes, File path) throws IOException {
 		String canonicalPath = path.getCanonicalPath();
 		boolean exclude = false;
     	for (String excludePath : excludes) {
     		if (canonicalPath.startsWith(excludePath)) {
     			exclude = true;
     			break;
     		}
     	}
     	
     	for (String includePath : includes) {
     		if (canonicalPath.startsWith(includePath)) {
     			exclude = false;
     			break;
     		}
     	}
 		return exclude;
 	}
 
 }
