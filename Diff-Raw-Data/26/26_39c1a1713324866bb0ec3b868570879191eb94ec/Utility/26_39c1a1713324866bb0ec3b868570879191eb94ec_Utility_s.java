 import java.io.File;
 import java.io.IOException;
 import java.io.FileWriter;
 import java.util.HashMap;
 import java.util.Scanner;
 
 
 public class Utility {
 
 	static HashMap<String, File> files= new HashMap<String, File>();
 	static HashMap<String, Scanner> scanners=new HashMap<String, Scanner>();
 	static HashMap<String, FileWriter> printers=new HashMap<String, FileWriter>();
 	
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
 	
 	public static FileWriter getWriter(Stint s){
 		if(printers.containsKey(s.toString())){
 			return printers.get(s.toString());
 		}else{
 			exception("Stint: File Not Opened");
 			return null;
 		}
 	}
 	
 	public static boolean read(Stint filename, Stint dest){
 		try{
 			Scanner sc=getScanner(filename);
 			if(sc.hasNextLine()){
				dest=new Stint(sc.nextLine());
 				return true;
 			}else
 				return false;
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 		return false;
 	}
 	
 	public static boolean read(Stint dest){
 		try{
 			Scanner sc=new Scanner(System.in);
 			if(sc.hasNextLine()){
				dest=new Stint(sc.nextLine());
 				return true;
 			}else
 				return false;
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 		return false;
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
 			FileWriter fw=new FileWriter(f,true);
 			Scanner sc=new Scanner(f);
 			scanners.put(s.toString(),sc);
 			printers.put(s.toString(),fw);
 		} catch (IOException e) {
 			e.printStackTrace();
 			exception("Stint: IO Exception");
 		}
 		
 	}
 	
 	private static void exception(String message){
 		throw new RuntimeException(message);
 	}
 	
 }
