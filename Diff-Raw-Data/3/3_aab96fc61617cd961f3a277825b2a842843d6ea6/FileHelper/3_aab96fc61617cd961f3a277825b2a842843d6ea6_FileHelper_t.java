 package com.evertvdbruel.helpers;
 
 import java.io.BufferedInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipInputStream;
 
 import android.content.Context;
 import android.os.Environment;
 
 public class FileHelper {
 
 	public static boolean isExternalStorageWritable() {
 		String state = Environment.getExternalStorageState();
 		if (Environment.MEDIA_MOUNTED.equals(state)) {
 			return true;
 		}
 		return false;
 	}
 
 	public static boolean isExternalStorageReadable() {
 		String state = Environment.getExternalStorageState();
 		if (Environment.MEDIA_MOUNTED.equals(state)
 				|| Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
 			return true;
 		}
 		return false;
 	}
 
 	public static String getAbsolutePath(Context context, String fileName) {
 		File file = FileHelper.getFileFromFileName(context, fileName);
 		return file.getAbsolutePath();
 	}
 
 	public static Boolean writeByteArrayToFile(Context context,
 			String fileName, byte[] data) throws IOException {
 		File file = FileHelper.getFileFromFileName(context, fileName);
 		return FileHelper.writeByteArrayToFile(file, data);
 	}
 
 	public static Boolean writeByteArrayToFile(File file, byte[] data)
 			throws IOException {
 		if (!file.getParentFile().exists()) {
 			if (!file.getParentFile().mkdirs()) {
 				return false;
 			}
 		}
 		OutputStream outputStream = new FileOutputStream(file);
 		outputStream.write(data);
 		outputStream.close();
 		return true;
 	}
 
 	public static void unzipFile(Context context, BufferedInputStream is)
 			throws IOException {
 		ZipInputStream zis = new ZipInputStream(is);
 		ZipEntry ze;
 
 		while ((ze = zis.getNextEntry()) != null) {
 			if (!ze.isDirectory()) {
 				String filename = ze.getName();
 				FileHelper.writeByteArrayToFile(context, filename,
 						FileHelper.inputStreamToByteArray(zis));
 			}
 		}
 	}
 
 	public static void unzipFile(Context context, String fileName)
 			throws FileNotFoundException, IOException {
 		File file = FileHelper.getFileFromFileName(context, fileName);
 		FileHelper.unzipFile(context, new BufferedInputStream(
 				new FileInputStream(file)));
 	}
 
 	public static byte[] getByteArrayFromFile(File file)
 			throws FileNotFoundException, IOException {
 		return inputStreamToByteArray(new FileInputStream(file));
 	}
 
 	public static byte[] inputStreamToByteArray(InputStream is)
 			throws IOException {
 		ByteArrayOutputStream bos = new ByteArrayOutputStream();
 		while (is.available() > 0) {
 			bos.write(is.read());
 		}
 		return bos.toByteArray();
 	}
 
 	public static Boolean deleteFile(Context context, String fileName) {
		File file = FileHelper.getFileFromFileName(context, fileName);
		return file.delete();
 	}
 
 	public static Boolean fileExists(Context context, String fileName) {
 		File file = FileHelper.getFileFromFileName(context, fileName);
 		return file.exists();
 	}
 
 	public static File getFileFromFileName(Context context, String fileName) {
 		return new File(context.getExternalFilesDir(null), fileName);
 	}
 
 }
