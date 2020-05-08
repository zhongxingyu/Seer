 package net.algoviz;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import com.google.gson.Gson;
 
 public class Avy {
 	private static int fileIndex = 0;
 	private static PrintWriter file = null;
 	
 	private static Gson gson = new Gson();
 	
 	public static void cmd(String name, Object ...args) {
 		if (name.equals("start")) {
 			if (file != null) {
 				file.close();
 			}
 			
 			try {
 				file = new PrintWriter(new FileWriter(String.format("%d.avy", fileIndex++)));
 			} catch (IOException e) {
 				// TODO: do something with the exception
 				return;
 			}
 			return;
 		}
 		
 		StringBuffer sb = new StringBuffer(name);
		for (Object obj : args) {
			sb.append(' ');
			sb.append(gson.toJson(obj));
 		}
 		file.println(sb.toString());
 	}
 	
 	public static void close() {
 		if (file != null) {
 			file.close();
 		}
 	}
 }
