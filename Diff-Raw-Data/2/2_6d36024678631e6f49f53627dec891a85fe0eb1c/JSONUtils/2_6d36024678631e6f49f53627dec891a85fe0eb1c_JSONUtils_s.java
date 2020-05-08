 package admin.data;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Scanner;
 
 import data.me.json.JSONException;
 import data.me.json.JSONObject;
 
 
 /**
  * Use this class to get the keys, to write to a file, and getting values.
  * This differs from justing using the JSON classes directly as we keep
  * references to the object centralized. We can take care of any additional
  * code that is needed to read/write to files rather.
  */
 public class JSONUtils{
 	
 	public static String seasonFile = "res/data/Settings.dat";
 	
 	public static JSONObject readFile(String path) throws FileNotFoundException{
 		File f = new File(path);
 		if(!f.exists())
 			throw new FileNotFoundException();
 		try{
 			String jString = fileToString(path);
 			JSONObject obj = new JSONObject(jString);
 			return obj;
 			
 		}catch (IOException e) {
 			e.printStackTrace();
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}		
 		return null;
 	}
 	
 	private static String fileToString(String pathname) throws IOException {
 
 	    File file = new File(pathname);
 	    StringBuilder fileContents = new StringBuilder((int)file.length());
 	    Scanner scanner = new Scanner(file);
 	    String lineSeparator = System.getProperty("line.separator");
 
 	    try {
 	        while(scanner.hasNextLine()) {        
 	            fileContents.append(scanner.nextLine() + lineSeparator);
 	        }
 	        return fileContents.toString();
 	    } finally {
 	        scanner.close();
 	    }
 	}
 	
 	/**
 	 * Writes to file using a json object
 	 * @param filePath The file path to write to
 	 * @param json A json object that has the keys and teh values
 	 */
 	public static void writeJSON(String filePath, JSONObject json){
 		try {
 			File f = new File(filePath);
 			
 			// if the directory above the file doesn't exist, make it. :)
 			File dir = f.getParentFile();
 			if (!dir.exists()) {
				dir.createNewFile();
 			}
 			
 			// delete the file if it exists already to completely overwrite
 			if (f.exists()) {
 				f.delete();
 				f.createNewFile();
 			}
 			
 			f.setWritable(true);
 			
 			FileWriter fileWrite = new FileWriter(f);
 			fileWrite.write(json.toString());
 			fileWrite.close();
 			
 		} catch (IOException e) {
 			System.out.println("JSONObject: writeJson: could not write to file");
 			e.printStackTrace();
 		}
 	}
 	
 	
 	/**
 	 * Deletes the season file, returns true if successful.
 	 * @return True if sucessfully reset season.
 	 */
 	public static boolean resetSeason(){
 		File f = new File(seasonFile);
 		f.delete(); 
 		return f.exists();
 	}
 
 }
