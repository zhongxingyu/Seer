 package org.eweb4j.util;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URL;
 import java.util.Collection;
 import java.util.Enumeration;
 import java.util.HashSet;
 
 /**
  * 文件操作工具类
  * 
  * @author CFuture.aw
  * 
  */
 public class FileUtil {
 	
 	public static boolean exists(String filePath){
 		File dir = new File(StringUtil.uriDecoding(filePath));
 		return dir.exists();
 	}
 	
 	/**
 	 * 返回某目录下所有文件对象
 	 * 
 	 * @param str
 	 * @return
 	 */
 	public static File[] getFiles(String str) {
 		File dir = new File(StringUtil.uriDecoding(str));
 		File[] result = null;
 		if (dir.isDirectory()) {
 			result = dir.listFiles();
 		}
 
 		return result;
 	}
 
 	/**
 	 * 返回某个类所在包最顶层文件夹
 	 * 
 	 * @param clazz
 	 * @return
 	 */
 	public static String getTopClassPath(Class<?> clazz) {
 		String path = StringUtil.uriDecoding(clazz.getResource("/").getPath());
 
 		return path;
 	}
 
 	public static void main(String[] args) {
 		System.out.println(getCurrPath(FileUtil.class));
 	}
 
 	/**
 	 * get the jars path
 	 * 
 	 * @return
 	 */
 	public static String getJarPath() {
 		return FileUtil.getParent(FileUtil.getTopClassPath(FileUtil.class), 1) + File.separator + "lib";
 	}
 	
 	public static String[] getChildrenPath(File parent){
 		File[] files = parent.listFiles();
 		String[] result = new String[files.length];
 		for (int i = 0; i < files.length; i++)
 			result[i] = StringUtil.uriDecoding(files[i].getAbsolutePath());
 		
 		return result;
 	}
 	
 	public static Collection<String> getClassPath(){
 		Collection<String> classpath = new HashSet<String>();
 		Enumeration<URL> urls;
 		try {
 			urls = FileUtil.class.getClassLoader().getResources("META-INF/MANIFEST.MF");
 			classpath.add(getTopClassPath(FileUtil.class));
 			while (urls.hasMoreElements()) {
 	            URL url = (URL) urls.nextElement();
 	            String path = url.getFile().replace("file:/", "").replace("!/META-INF/MANIFEST.MF", "");
 	            classpath.add(StringUtil.uriDecoding(path));
 	        }
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		return classpath;
 	}
 
 	public static String getClassPath(String folderName) {
 		return getParent(getTopClassPath(FileUtil.class), 1) + folderName;
 	}
 
 	/**
 	 * 获得类所在文件路径
 	 * 
 	 * @param clazz
 	 * @return
 	 */
 	public static String getCurrPath(Class<?> clazz) {
 		return StringUtil.uriDecoding(clazz.getResource("/").getPath()
 				+ clazz.getName().replace(".", File.separator));
 	}
 
 	/**
 	 * 创建一个文件夹
 	 * 
 	 * @param path
 	 * @return
 	 */
 	public static boolean createDir(String path) {
 		boolean flag = false;
 		File file = new File(StringUtil.uriDecoding(path));
 		if (!file.exists()) {
 			if (!file.isDirectory()) {
 				flag = file.mkdir();
 			}
 		}
 		return flag;
 	}
 
 	/**
 	 * 创建一个文件
 	 * 
 	 * @param path
 	 * @return
 	 * @throws IOException
 	 */
 	public static boolean createFile(String path) throws IOException {
 		return createFile(path, false);
 	}
 
 	/**
 	 * 
 	 * @param file
 	 * @param isDelete
 	 * @return
 	 * @throws IOException
 	 */
 	public static boolean createFile(File file, boolean isDelete) throws IOException{
 		boolean flag = true;
 		if (file.exists()) {
 			if (isDelete) {
 				file.delete();
 				file.createNewFile();
 			} else {
 				flag = false;
 			}
 		} else {
 			file.createNewFile();
 		}
 		
 		return flag;
 	}
 	
 	/**
 	 * 
 	 * @param path
 	 * @param isDelete
 	 * @return
 	 * @throws IOException
 	 */
 	public static boolean createFile(String path, boolean isDelete)
 			throws IOException {
 		File file = new File(StringUtil.uriDecoding(path));
 
 		return createFile(file, isDelete);
 	}
 
 	/**
 	 * 将oldFile移动到指定目录
 	 * 
 	 * @param oldFile
 	 * @param newDir
 	 * @return
 	 */
 	public static boolean moveFileTo(File oldFile, String newDir) {
 		StringBuilder sb = new StringBuilder(newDir);
 		sb.append(File.separator).append(oldFile.getName());
 		File toDir = new File(StringUtil.uriDecoding(sb.toString()));
 		boolean flag = false;
 		if (!toDir.exists()) {
 			flag = oldFile.renameTo(toDir);
 		}
 		return flag;
 	}
 
 	/**
 	 * 返回当前文件的上层文件夹路径（第几层由参数floor决定）
 	 * 
 	 * @param f
 	 * @param floor
 	 * @return
 	 */
 	public static String getParent(File f, int floor) {
 		String result = "";
 		if (f != null && f.exists()) {
 			for (int i = 0; i < floor; ++i) {
 				f = f.getParentFile();
 			}
 
 			if (f != null && f.exists()) {
 				result = f.getPath();
 			}
 		}
 
 		return StringUtil.uriDecoding(result) + File.separator;
 	}
 
 	public static String getParent(String path, int floor) {
 		return getParent(new File(path), floor);
 	}
 
 	/**
 	 * 删除文件
 	 * 
 	 * @param file
 	 * @return
 	 */
 	public static boolean deleteFile(File file) {
 		boolean flag = false;
 		if (file != null && file.exists()) {
 			if (file.isDirectory()) {
 				for (File f : file.listFiles()) {
 					deleteFile(f);
 				}
 			}
 			flag = file.delete();
 		}
 
 		return flag;
 	}
 
 	/**
 	 * 检查文件名是否合法
 	 * 
 	 * @param fileName
 	 * @return
 	 */
 	public static boolean isValidFileName(String fileName) {
 		if (fileName == null || fileName.length() > 255)
 			return false;
 		else {
 			return fileName
 					.matches("[^\\s\\\\/:\\*\\?\\\"<>\\|](\\x20|[^\\s\\\\/:\\*\\?\\\"<>\\|])*[^\\s\\\\/:\\*\\?\\\"<>\\|\\.]$");
 		}
 	}
 
 	/**
 	 * 复制文件
 	 * 
 	 * @param src
 	 * @param dst
 	 */
 	public static void copy(File src, File dst) {
 		InputStream in = null;
 		OutputStream out = null;
 		try {
 			in = new FileInputStream(src);
 			out = new FileOutputStream(dst);
 
 			// Transfer bytes from in to out
 			byte[] buf = new byte[1024];
 			int len = -1;
 			while ((len = in.read(buf)) > 0) {
 				out.write(buf, 0, len);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				if (in != null) {
 					in.close();
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			} finally {
 				try {
 					if (out != null) {
 						out.close();
 					}
 				} catch (Exception e2) {
 					e2.printStackTrace();
 				}
 			}
 		}
 		return;
 	}
 
 }
