 package txtfnnl.utils;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 import java.io.UnsupportedEncodingException;
 
 /**
  * A compact version of the Apache Commons IOUtils.
  * 
  * Might be removed if more IOUtils methods are needed.
  * 
  * @author Florian Leitner
  * 
  */
 public class IOUtils {
 
 	public static int BUFFER_SIZE = 0x2000; // 8 KB default
 
 	private IOUtils() {
 		throw new AssertionError("non-instantiable");
 	}
 
 	public static String read(InputStream in, String encoding)
 	        throws UnsupportedEncodingException, IOException {
		InputStreamReader stream = new InputStreamReader(in, "UTF-8");
 		StringBuilder out = new StringBuilder();
 		final char[] buffer = new char[BUFFER_SIZE];
 		int read;
 
 		try {
 			while ((read = stream.read(buffer, 0, buffer.length)) != -1) {
 				out.append(buffer, 0, read);
 			}
 		} finally {
 			in.close();
 		}
 
 		return out.toString();
 	}
 
	public static boolean isMacOSX() {
		return System.getProperty("os.name").toLowerCase().startsWith("mac");
	}

 	public static String getLocaleEncoding() {
 		String encoding = System.getenv("LANG");
 
 		if (encoding != null) {
 			if (encoding.lastIndexOf('.') > -1)
 				encoding = encoding.substring(encoding.lastIndexOf('.') + 1);
 			else
 				encoding = null;
 		}
 
 		return encoding;
 	}
 
 	public static void setOutputEncoding(String encoding)
 	        throws UnsupportedEncodingException {
 		System.setOut(new PrintStream(System.out, true, encoding));
 	}
 
 	public static void setUTF8OutputEncoding()
 	        throws UnsupportedEncodingException {
 		if (!System.getProperty("file.encoding").toLowerCase().equals("utf-8"))
 			setOutputEncoding("UTF-8");
 	}
 
 }
