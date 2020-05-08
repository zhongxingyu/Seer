 package com.viamep.richard.jgopherd;
 
 import java.io.File;
 import java.util.Arrays;
 
 public class Util {
 	public static String FormatBytes(long bytes) {
 		if (bytes > 1073741824) return (long)bytes/1073741824+" GB";
 		if (bytes > 1048576) return (long)bytes/1048576+" MB";
 		if (bytes > 1024) return (long)bytes/1024+" KB";
 		return bytes+" bytes";
 	}
 	
 	public static String GetArray(String[] Arr, String Delimiter, int IndexFrom, int IndexTo) {
 		String Temp = "";
 		int TmpIndex = 0;
 		if (IndexTo == 0) {
 			TmpIndex = Arr.length;
 		} else {
 			TmpIndex = IndexTo;
 		}
 		for (int i = IndexFrom; i < TmpIndex; i++) {
 			Temp += Delimiter;
 			Temp += Arr[i];
 		}
 		return Temp.replaceFirst(Delimiter,"");
 	}
 	
 	public static String GetArray(String[] Arr, int IndexFrom, int IndexTo) {
 		return GetArray(Arr," ",IndexFrom,IndexTo);
 	}
 	
 	public static <T> T[] ConcatArrays(T[] first, T[]... rest) {
 		  int totalLength = first.length;
 		  for (T[] array : rest) {
 		    totalLength += array.length;
 		  }
 		  T[] result = Arrays.copyOf(first, totalLength);
 		  int offset = first.length;
 		  for (T[] array : rest) {
 		    System.arraycopy(array, 0, result, offset, array.length);
 		    offset += array.length;
 		  }
 		  return result;
 	}
 	
 	public static boolean IsExecutable(String file) {
 		if (new File(file).isDirectory()) return false;
 		if ((System.getProperty("os.name").indexOf("Windows") == -1) && (System.getProperty("os.name").indexOf("Mac") == -1)) {
 			try {
 				if (new File(file).canExecute()) return true;
 			} catch (Throwable e) {
 				return false;
 			}
 		}
 		if (file.endsWith(".mol")) return true;
 		if ((System.getProperty("os.name").indexOf("Windows") != -1) && (file.endsWith(".exe"))) return true;
 		if ((System.getProperty("os.name").indexOf("Windows") != -1) && (file.endsWith(".bat"))) return true;
 		if ((System.getProperty("os.name").indexOf("Windows") != -1) && (file.endsWith(".cmd"))) return true;
 		if ((System.getProperty("os.name").indexOf("Windows") == -1) && (file.endsWith(".sh"))) return true;
 		if ((System.getProperty("os.name").indexOf("Windows") == -1) && (file.endsWith(".command"))) return true;
 		return false;
 	}
 	
 	public static boolean IsExecutable(File file) {
 		return IsExecutable(file.getAbsolutePath());
 	}
 	
 	public static char GetKind(String filename) {
 		if (new File(filename).isDirectory()) return '1';
 		String fn = filename.toLowerCase();
 		if (fn.endsWith(".txt")||fn.endsWith(".log")||fn.endsWith(".sql")) return '0';
 		if (fn.endsWith(".gif")) return 'g';
		if (fn.endsWith(".jpg")||fn.endsWith(".jpeg")||fn.endsWith(".pict")||fn.endsWith(".svg")||fn.endsWith(".ico")||fn.endsWith(".icon")||fn.endsWith(".icns")||fn.endsWith(".tiff")||fn.endsWith(".tif")||fn.endsWith(".bmp")) return 'I';
 		if (fn.endsWith(".png")) return 'p';
 		if (fn.endsWith(".pdf")) return 'd';
 		if (fn.endsWith(".htm")||fn.endsWith(".html")) return 'h';
 		if (fn.endsWith(".wav")||fn.endsWith(".mp3")||fn.endsWith(".snd")||fn.endsWith(".aiff")||fn.endsWith(".au")||fn.endsWith(".flac")||fn.endsWith(".ogg")) return 's';
 		if (fn.endsWith(".mov")||fn.endsWith(".mpg")||fn.endsWith(".mpeg")||fn.endsWith(".wmv")||fn.endsWith(".avi")||fn.endsWith(".wm")||fn.endsWith(".mp4")||fn.endsWith(".flv")||fn.endsWith(".ogv")) return ';';
 		return '9';
 	}
 	
 	public static String HTMLEscape(String text) {
 		return text.replaceAll("\\<","&lt;").replaceAll("\\>","&gt;").replaceAll("\\&","&amp;").replaceAll("\"","&quot;").replaceAll("\\|","&#124;");
 	}
 	
 	public static String GetFullKind(char kind) {
 		if (kind=='i') return "";
 		if (kind=='0') return "[TXT]";
 		if (kind=='1') return "[DIR]";
 		if (kind=='2') return "[CSO]";
 		if (kind=='3') return "";
 		if (kind==';') return "[MOV]";
 		if (kind=='4'||kind=='5'||kind=='6'||kind=='9') return "[BIN]";
 		if (kind=='8'||kind=='T') return "[TLN]";
 		if (kind=='d') return "[PDF]";
 		if (kind=='g'||kind=='I'||kind=='p') return "[IMG]";
 		if (kind=='h') return "[HTM]";
 		if (kind=='U') return "[URL]";
 		if (kind=='s') return "[SND]";
 		return "[GEN]";
 	}
 	
 	public static String GetContentTypeForKind(char kind) {
 		if (kind=='0') return "text/plain";
 		if (kind=='1'||kind=='h') return "text/html";
 		if (kind==';') return "video/*";
 		if (kind=='4'||kind=='5'||kind=='6'||kind=='9') return "application/octet-stream";
 		if (kind=='d') return "text/pdf";
 		if (kind=='g') return "image/gif";
 		if (kind=='p') return "image/png";
 		if (kind=='I') return "image/*";
 		if (kind=='s') return "audio/*";
 		return "text/html";
 	}
 	
 	public static String GetEnv(String name, String def) {
 		try {
 			String env = System.getenv(name);
 			if ((env == "") || (env == null)) return def;
 			return env;
 		} catch (Throwable e) {
 			// do nothing
 		}
 		return def;
 	}
 }
