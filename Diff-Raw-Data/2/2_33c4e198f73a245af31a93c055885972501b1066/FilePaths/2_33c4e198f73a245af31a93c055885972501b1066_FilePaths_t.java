 package xite;
 
 import java.io.File;
 import java.net.URI;
 
 public class FilePaths {
 
 	private FilePaths() {
 	}
 
	/*
 	 * trasforms relative paths in absolute,
 	 * ie. ../tests/docs is transformed in
 	 * D:\path\to\tests\docs
 	 * 
 	 * @param path
 	 * @return the normalized, absolute path
 	 *
 	public static String _absoluteNormalized(String path) {
 		File file = new File(path);
 		return absoluteNormalized(file);
 	}*/
 
 	public static String absoluteNormalized(File file) {
 		URI ud = file.toURI();
 		URI un = ud.normalize();
 		File f = new File(un);
 		String absolutePath = f.getAbsolutePath();
 		return absolutePath.replace(File.separatorChar, '/');
 	}	
 
 	/*
 	public static String _absolute(File file) {
 		URI ud = file.toURI();
 		URI un = ud.normalize();
 		File f = new File(un);
 		String absolutePath = f.getAbsolutePath();
 		return absolutePath;
 	}
 	*/
 
     /**
      * Replaces all backslashes with slash char. Throws NPE if the original path
      * is null.
      * 
      * @param original :
      *            the path to normalize.
      *
      *
     public static String _normalized(String original)
     {
         return original.replace(File.separatorChar, '/');
     }*/
     
 
     /**
      * Resolve the extension for the given filename. Throws NPE if filename is
      * null.
      * 
      * @param filename
      * @return the file extension (without dot) if any, or empty string if filename doesn't contain any dot.
      *
     public static String _extension(String filename)
     {
         String extension = "";
         String fn = filename.trim();
         int dotPos = fn.lastIndexOf(".");
         if (dotPos == -1)
         {
             return "";
         }
         extension = fn.substring(dotPos + 1);
         return extension;
     }
     */
 
 	/*
     public static String _stripExtension(String filename) {
         int extensionIndex = filename.lastIndexOf(".");
         if (extensionIndex == -1) {
             return filename;
         }
         return filename.substring(0, extensionIndex);
     }
 	*/
 
 }
