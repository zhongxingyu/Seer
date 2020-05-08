 package tcwi;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import tcwi.fileHandler.*;
 import tcwi.xml.*;
 
 /**
  * 
  * @author EifX
 * @version 0.0.0.3
  */
 public class TCWI {
 	private ArrayList<ErrorFile> files = new ArrayList<ErrorFile>();
 	private Check check = new Check();
 	private Parser parser;
 	
 	/**
 	 * Get all files from an given path
 	 * @param path
 	 * @return
 	 */
 	private String getAllFiles(String path){
 		File file = new File(path);
 		File[] fileList = file.listFiles();
 		try{
 			for(int i=0;i<fileList.length;i++){
 				if(fileList[i].isDirectory()){
 					getAllFiles(fileList[i].getAbsolutePath());
 				}else{
 					if(fileList[i].toString().substring(fileList[i].toString().length()-4, fileList[i].toString().length()).equals(".dbg")){
 						try{
 							ErrorFile errFile = new ErrorFile(fileList[i].getAbsolutePath(),check.failFlags(fileList[i].getAbsolutePath()));
 							files.add(errFile);
 						}catch (IOException e){
 							return fileList[i].getAbsolutePath();
 						}
 					}
 				}
 			}
 		}catch (Exception e){
 			return path;
 		}
 		return "";
 	}
 	
 	private void writeProjectFile(String path, String projectName){
 		try{
 			File f = new File(path);
 			f.delete();
			RandomAccessFile file = new RandomAccessFile(path+check.folderSeparator()+projectName+".project","rw");
 			for(int i=0;i<files.size();i++){
 				file.writeBytes(files.get(i)+"\r\n");
 			}
 			file.close();
 		}catch (IOException e){
 			System.out.println("ERROR during write the project-file at:\n"+path);
 		}
 	}
 	
 	public void initialisize(String path, String projectName, String settingFile){
 		System.out.println("Starting initialization from project "+projectName+"...");
 		
 		String str = getAllFiles(path);
 		if(str.equals("")){
 			System.out.println("Initialization DONE!");
 		}else{
 			System.out.println("Initialization FAILED! Maybe you do not have the rights for the path: \n"+str);
 			System.exit(-1);
 		}
 		
 		System.out.println("Sort files...");
 		
		ErrorFile[] errFiles = new ErrorFile[files.size()];
		for(int i=0;i<files.size();i++){
			errFiles[i] = files.get(i);
		}

 		Arrays.sort(errFiles);
 		files.clear();
 		for(int i=0;i<errFiles.length;i++){
 			files.add(errFiles[i]);
 		}
 
 		System.out.println("Sorting DONE!");
 		System.out.println("Writing down the project file...");
 		
 		parser = new Parser(settingFile);
 		String[] xpath1 = {"settings","global","webint","path"};
 		String[] xpath2 = {"settings","global","projects","path"};
 		
 		try {
 			writeProjectFile(parser.read_setting(xpath1)+parser.read_setting(xpath2),projectName);
 		} catch (Exception e) {
 			System.out.println("ERROR by reading the settings-file.");
 			System.exit(-1);
 		}
 		
 		System.out.println("Writing DONE!\nScript DONE!");
 	}
 }
