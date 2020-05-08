 package br.com.expense.util;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.util.Scanner;
 
 public class FileUtil {
 	
 	public static String entryLocation() {
 		return System.getProperty("user.dir");
 	}
 
 	public static String loadFile(String path) {
 		System.out.println(">> Loading content from file: " + path);
 		StringBuilder content = new StringBuilder();
 		Scanner sc = null;
 		try {
 			sc = new Scanner(new FileInputStream(path), "UTF-8");
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 		while (sc.hasNext()) {
 			content.append(sc.nextLine() + "\r\n");
 		}
 		return content.toString();		
 	}
 	
 	public static String loadFile(File baseDir, String file) {
 		return loadFile(new File(baseDir, file).getPath());
 	}
 	
 	public static String loadFiles(File baseDir, String... files) {
 		StringBuilder content = new StringBuilder();
 		for (String file : files) {
 			content.append(loadFile(new File(baseDir, file).getPath()));
 		}
 		return content.toString();
 	}
 	
 	public static void writeFile(File file, String content, String encoding) {
 		BufferedWriter bw = null;
 		try {
 			 bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), encoding));
 			 bw.write(content);
 			 bw.flush();
 			 bw.close();
 		} catch (IOException e) {
 			if (bw != null) {
 				try {
 					bw.close();
 				} catch (IOException e1) {
 					e1.printStackTrace();
 				}
			} else {
				e.printStackTrace();
 			}
 		}		
 	}
 	
 	public static void writeFile(File file, String content) {
 		writeFile(file, content, "UTF-8");
 	}
 }
