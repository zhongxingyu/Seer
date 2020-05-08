 package persiantools;
 
 import java.io.*;
 
 import org.apache.commons.io.*;
 
 public class HTMLCharacterHelper {
 	public static void removeAll_NBSP_ZWNJ_QUOT(File file) throws IOException{
 		String content;
 		FileInputStream fis = new FileInputStream(file);
 		content = IOUtils.toString(fis, "UTF8");
 		fis.close();
 		content = content.replaceAll("&nbsp;", " ");
		content = content.replaceAll("&rlm;", "‌");
 		content = content.replaceAll("&zwnj;", "‌");
 		content = content.replaceAll("&quot;", "\"");
 		FileOutputStream fos = new FileOutputStream(file);
 		IOUtils.write(content, fos, "UTF8");
 		fos.close();
 	}
 }
