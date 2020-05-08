 package util;
 
 import java.io.File;
 
 
 /**
  * This is a class that can switch between absolute and relative file paths,
  * and convert a filepath to a fully qualified java class name,
  * which can be used for reflection
  * 
  * @author Will Nance
  * 
  */
 public class FilePathFormatter {
     public static final String SOURCE = "src";
     private String projectDir = System.getProperty("user.dir");
 
     public FilePathFormatter () {
     }
 
     public String relativeToAbsolute (String relativePath) {
         // this should be someone else's problem. deal with your own null pointer exceptions
         if (relativePath == null) return null;
         return projectDir + SOURCE + relativePath;
     }
 
     public String absoluteToRelative (String absolutePath) {
         if (absolutePath == null) return null;
         return makePathRelativeToSrc(absolutePath);
     }
 
     public String formatClassFilePath (String path) {
        if (path == null || path.equals("")) return null;
         return replaceFileSeparatorWithDots(makePathRelativeToSrc(removeFileExtension(path)));
     }
 
     private String removeFileExtension (String filepath) {
        System.out.println("this is the filepath" + filepath);
        System.out.println(filepath.indexOf('.'));
            return filepath.substring(0, filepath.indexOf("."));
     }
 
     private String makePathRelativeToSrc (String path) {
         while (path.contains(SOURCE)) {
             path = path.substring(path.indexOf(SOURCE) + SOURCE.length() + 1, path.length());
         }
         return path;
     }
 
     private String replaceFileSeparatorWithDots (String path) {
         char[] letters = path.toCharArray();
         String ret = "";
         for (int i = 0; i < letters.length; i++) {
             char c = letters[i];
             if (c == File.separatorChar) {
                 letters[i] = '.';
             }
             ret += letters[i];
         }
         return ret;
     }
 
 }
