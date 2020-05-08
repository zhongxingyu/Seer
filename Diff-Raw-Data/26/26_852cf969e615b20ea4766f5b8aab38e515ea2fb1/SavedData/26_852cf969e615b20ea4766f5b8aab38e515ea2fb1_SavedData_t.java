 package framework;
 
 import java.io.File;
 import java.io.IOException;
 
 public class SavedData {
 	boolean createDir,createFile;
 	
 	File saveDir, saveFile;
 	
 	public static void main(String[] args) throws IOException {
 		SavedData config = new SavedData("Config", "Settings");
		SavedData saves = new SavedData("Save", "Saves");
 	}
 	
 	public SavedData(String dir, String baseFile) throws IOException {
 		
 		saveDir = new File(dir);
 		saveFile = new File(saveDir, (baseFile+".txt"));
 		
 		// if the directory doesn't exist, create it.
 		System.out.println("Checking directory: " + saveDir);
 		if (!saveDir.exists()) {
 			System.out.println("Creating directory: " + saveDir);
 			boolean result = saveDir.mkdir();  
 			
 			if(result) {    
 				System.out.println("Directory created.");
 			} else {
 				System.out.println("Directory could not be created.");
 			}
 		} else {
 			System.out.println("Directory already exists.");
 		}
 		
 		// if the file doesn't exist, create it.
 		System.out.println("Checking file: " + saveFile);
 		if (!saveFile.exists()) {
 			System.out.println("Creating file: " + saveFile);
 			boolean result = saveFile.createNewFile();  
 			
 			if(result) {    
 				System.out.println("File created.");
 			} else {
 				System.out.println("File could not be created.");
 			}
 		} else {
 			System.out.println("File already exists.");
			this.load(); //Load stfrom the file
 		}	
 
 		String fileName = saveFile.getAbsoluteFile().toString();
 		System.out.println("File located at: " + fileName + "\n");
 		
 	}
 	
	/*public void createSaveData(String saveName) {
 		saveFile = new File(saveDir, (saveName + ".txt"));
	}*/
 	
 	public void load() {
	
 		
 	}
 	
 	public void save() {
 		
 	}
 
 }
