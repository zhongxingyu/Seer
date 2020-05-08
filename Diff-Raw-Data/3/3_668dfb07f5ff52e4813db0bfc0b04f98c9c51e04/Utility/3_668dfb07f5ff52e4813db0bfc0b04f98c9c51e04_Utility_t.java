 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.PrintWriter;
 import java.util.HashMap;
 import java.util.Scanner;
 
 
 public class Utility {
 
 	static HashMap<String, File> files= new HashMap<String, File>();
 	static HashMap<String, Scanner> scanners=new HashMap<String, Scanner>();
 	static HashMap<String, PrintWriter> printers=new HashMap<String, PrintWriter>();
 	
 	public static File getFile(Stint s){
 		if(files.containsKey(s.toString())){
 			return files.get(s.toString());
 		}else{
 			File file=new File(s.toString());
 			files.put(s.toString(),file);
 			updateIO(file,s);
 			return file;
 		}
 	}
 	
 	public static Scanner getScanner(Stint s){
 		if(scanners.containsKey(s.toString())){
 			return scanners.get(s.toString());
 		}else{
 			exception("Stint: File Not Opened");
 			return null;
 		}
 	}
 	
 	public static PrintWriter getWriter(Stint s){
 		if(printers.containsKey(s.toString())){
 			return printers.get(s.toString());
 		}else{
 			exception("Stint: File Not Opened");
 			return null;
 		}
 	}
 	
 	public static boolean close(Stint s){
 		try{
 			if(files.containsKey(s.toString())){
 				files.remove(s.toString());
 				if(scanners.containsKey(s.toString())){
 					scanners.remove(s.toString());
 					if(printers.containsKey(s.toString())){
 						printers.remove(s.toString());
 						return true;
 					}
 				}
 			}
 			return false;
 		}catch(Exception e){
 			e.printStackTrace();
 			exception("Stint: IO Exception");
 		}
 		return false;
 	}
 	
 	private static void updateIO(File f, Stint s){
 		try {
 			PrintWriter pw=new PrintWriter(f);
			Scanner sc=new Scanner(f);
 			scanners.put(s.toString(),sc);
 			printers.put(s.toString(),pw);
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			exception("Stint: IO Exception");
 		}
 		
 	}
 	
 	private static void exception(String message){
 		throw new RuntimeException(message);
 	}
 	
 }
