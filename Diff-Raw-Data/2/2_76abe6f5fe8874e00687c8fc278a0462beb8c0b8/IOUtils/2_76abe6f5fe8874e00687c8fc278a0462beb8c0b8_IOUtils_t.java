 package iTests.framework.utils;
 
 import com.google.common.io.Files;
 import com.google.common.io.InputSupplier;
 import org.apache.commons.io.FileUtils;
 import org.testng.Assert;
 
 import java.io.*;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Properties;
 
 /**
  * A set of utility methods for IO manipulation
  * @author elip, sagi
  *
  */
 public class IOUtils {
 	
 	/**
 	 * 
 	 * @param oldFile
 	 * @param newFile
 	 * @throws java.io.IOException
 	 */
 	public static void replaceFile(String oldFile, String newFile) throws IOException {
 		File old = new File(oldFile);
 		old.delete();
 		Files.copy(new File(newFile), new File(oldFile));
 	}
 
     public static String backupFile(String filePath) throws IOException {
 
         String backupFilePath = filePath + ".tmp";
         FileUtils.copyFile(new File(filePath), new File(backupFilePath));
         return backupFilePath;
     }
 
 	public static void replaceTextInFile(String fileName, String oldText, String newText) throws IOException {
 		HashMap<String, String> toPass = new HashMap<String, String>();
 		toPass.put(oldText, newText);
 		replaceTextInFile(fileName, toPass);
 	}
 
 	public static void replaceTextInFile(String filePath, Map<String, String> map) throws IOException {
         LogUtils.log("replacing props in file : " + filePath);
         File file = new File(filePath);
         BufferedReader reader = new BufferedReader(new FileReader(file));
         String line = "", oldtext = "";
         String newLine;
         if(System.getProperty("os.name").toLowerCase().startsWith("win")){
         	newLine = "\r\n";
         }
         else {
         	newLine = "\n";
         }
         while((line = reader.readLine()) != null)
             {
             oldtext += line + newLine;
         }
         reader.close();
         String newtext = new String(oldtext);
         for (String toReplace : map.keySet()) {
             LogUtils.log("replacing " + toReplace + " with " + map.get(toReplace));
             newtext = newtext.replaceAll(toReplace, map.get(toReplace));
         }
 
         FileWriter writer = new FileWriter(filePath);
         writer.write(newtext);
         writer.close();
 	}
 
 	public static void replaceTextInFile(File file, Map<String,String> map) throws IOException {
 		String originalFileContents = FileUtils.readFileToString(file);
 		String modified = originalFileContents;
 		for (String s : map.keySet()) {
 			modified = modified.replace(s, map.get(s));
 		}
 		FileUtils.write(file, modified);
 	}
 
 	public static void copyFile(String oldFile, String newFile) throws IOException {
 		Files.copy(new File(oldFile), new File(newFile));
 	}
 
 	public static void createFileFromResource(String resourcePath, File destination) throws IOException {
 	    final URL resource = Thread.currentThread().getContextClassLoader().getResource(resourcePath);
 	    if (resource == null) {
 	        Assert.fail("Failed finding resource: " + resourcePath + " in classpath");
 	    }
 
 	    final InputStream is = resource.openStream();
 
 	    Files.copy(new InputSupplier<InputStream>() {
             public InputStream getInput() throws IOException {
                 return is;
             }
         }, destination);
 	}
 
     public static File writePropertiesToFile(final Properties props , final File destinationFile) throws IOException {
 
         String existingProps = null;
         if (destinationFile.exists()) {
             existingProps = FileUtils.readFileToString(destinationFile);
         }
 
         Properties properties = new Properties();
         for (Entry<Object, Object> entry : props.entrySet()) {
 
             Object value = entry.getValue();
             String key = entry.getKey().toString();
             String actualValue = null;
             if (value instanceof String) {
                 actualValue = '"' + value.toString() + '"';
             } else {
                 actualValue = value.toString();
             }
             properties.setProperty(key, actualValue);
         }
         FileOutputStream fileOut = new FileOutputStream(destinationFile);
         properties.store(fileOut,null);
         fileOut.close();
 
         // this part is needed to replace the illegal comment sign '#' in the file
         String readFileToString = FileUtils.readFileToString(destinationFile);
         if (existingProps != null) {
             FileUtils.writeStringToFile(destinationFile, existingProps + "\n" + readFileToString.replaceAll("#", "//").replaceAll("\\\\:", ":"));
         } else {
            FileUtils.writeStringToFile(destinationFile, readFileToString.replaceAll("#", "//").replaceAll("\\\\:", ":"));
         }
         return destinationFile;
 
     }
 
     public static File writePropertiesToFile(final Map<String, Object> props , final File destinationFile) throws IOException {
 
         Properties properties = new Properties();
         for (Entry<String, Object> entry : props.entrySet()) {
             properties.put(entry.getKey(), entry.getValue());
         }
         return writePropertiesToFile(properties, destinationFile);
     }
 
     public static void replaceFile(final File originalFile, final File replaceToReplaceWith) throws IOException {
 
         File parentFolder = originalFile.getParentFile();
 
         // delete the old file
         if (originalFile.exists()) {
             originalFile.delete();
         }
         // copy the new file and use the name of the old file
         FileUtils.copyFileToDirectory(replaceToReplaceWith, parentFolder);
 
     }
 
     public static void replaceFileWithMove(final File originalFile, final File ReplaceWith) throws IOException {
 
         // delete the old file
         if (originalFile.exists()) {
             originalFile.delete();
         }
         // copy the new file and use the name of the old file
         FileUtils.moveFile(ReplaceWith, originalFile);
 
     }
 
     public static File createTempOverridesFile(Map<String, Object> overrides) throws IOException {
         File createTempFile = File.createTempFile("__sgtest_cloudify", ".overrides");
         File overridePropsFile = writePropertiesToFile(overrides, createTempFile);
         return overridePropsFile;
 
     }
 
     public static Properties readPropertiesFromFile(File file) throws FileNotFoundException, IOException {
         Properties props = new Properties();
         props.load(new FileInputStream(file));
         return props;
     }
 }
