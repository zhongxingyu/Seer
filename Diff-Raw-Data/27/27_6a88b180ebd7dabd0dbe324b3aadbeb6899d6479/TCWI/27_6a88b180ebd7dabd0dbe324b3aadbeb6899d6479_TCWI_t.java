 package tcwi;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 import tcwi.fileHandler.*;
 import tcwi.xml.*;
 
 /**
  * 
  * @author EifX
 * @version 0.0.3.0
  */
 public class TCWI {
 	private ArrayList<ErrorFile> files = new ArrayList<ErrorFile>();
 	private Check check = new Check();
 	private Parser parser;
 	private boolean failureProject = false;
 	
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
 					if(fileList[i].toString().substring(fileList[i].toString().length()-2, fileList[i].toString().length()).equals(".c")){
 						try{
 							ErrorFile errFile = new ErrorFile(fileList[i].getAbsolutePath().substring(0,fileList[i].getAbsolutePath().length()-2),check.failFlags(fileList[i].getAbsolutePath().substring(0,fileList[i].getAbsolutePath().length()-2)));
 							files.add(errFile);
 						}catch (IOException e){
 							e.printStackTrace();
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
 	
 	/**
 	 * Write the .project file
 	 * @param path
 	 * @param projectName
 	 */
 	private void writeProjectFile(String path, String projectName){
 		try{
 			File f = new File(path+check.folderSeparator()+projectName+".project");
 			f.delete();
 			RandomAccessFile file = new RandomAccessFile(path+check.folderSeparator()+projectName+".project","rw");
 			for(int i=0;i<files.size();i++){
 				//Check if the project has errors. The result will be saved in the project.xml
 				if(failureProject==false){
 					if(files.get(i).haveErrors()){
 						failureProject = true;
 					}
 				}
 				file.writeBytes(files.get(i)+"\r\n");
 			}
 			file.close();
 		}catch (IOException e){
 			System.out.println("ERROR during write the project-file at:\n"+path);
 		}
 	}
 
 	/**
 	 * Returns a correct formatted number
 	 * @param str
 	 * @return
 	 */
 	private String correctCalendarForm(int i){
 		String str = i+"";
 		if(str.length()==1){
 			return "0"+str;
 		}else{
 			return str;
 		}
 	}
 	
 	/**
 	 * Write the .project.xml file
 	 * @param path
 	 * @param projectName
 	 * @param projectPath
 	 */
	private void writeProjectXMLFile(String path, String projectName, String projectFullName, String projectVersion, String projectAuthor, String projectPath){
 		try{
 			Calendar c = new GregorianCalendar();
 			String month = correctCalendarForm(c.get(GregorianCalendar.MONTH)+1);
 			String day = correctCalendarForm(c.get(GregorianCalendar.DAY_OF_MONTH));
 			String hour = correctCalendarForm(c.get(GregorianCalendar.HOUR_OF_DAY));
 			String minute = correctCalendarForm(c.get(GregorianCalendar.MINUTE));
 			String second = correctCalendarForm(c.get(GregorianCalendar.SECOND));
 
 			File f = new File(path+check.folderSeparator()+projectName+".project.xml");
 			f.delete();
 			RandomAccessFile file = new RandomAccessFile(path+check.folderSeparator()+projectName+".project.xml","rw");
 			file.writeBytes("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");//TODO: Datei wird nicht als UTF-8 abgespeichert
 			file.writeBytes("<settings>\r\n");
 			file.writeBytes("     <global>\r\n");
			file.writeBytes("          <project idname=\""+projectName+"\" fullname=\""+projectFullName+"\" version=\""+projectVersion+"\" path=\""+projectPath+"\" failureProject=\""+failureProject+"\" />\r\n");
 			file.writeBytes("          <init builder=\""+projectAuthor+"\" buildday=\""+c.get(GregorianCalendar.YEAR)+"-"+month+"-"+day+"\" buildtime=\""+hour+":"+minute+":"+second+"\" />\r\n");
 			file.writeBytes("     </global>\r\n");
 			file.writeBytes("</settings>\r\n");
 			file.close();
 		}catch (IOException e){
 			System.out.println("ERROR during write the project-file at:\n"+path);
 		}
 	}
 	
 	/**
 	 * Main initialization from a project 
 	 * @param path
 	 * @param projectName
 	 * @param settingFile
 	 */
	public void initialisize(String path, String projectName, String projectFullName, String projectVersion, String projectAuthor, String settingFile){
 		System.out.println("Starting initialization from project "+projectName+"...");
 		
 		//Removes an additional folder separator from an path end
 		//ex. /app/home/foo/bar/ --> /app/home/foo/bar
 		if(path.endsWith(check.folderSeparator()+"")){
 			path = path.substring(0,path.length()-1);
 		}
 		
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
			writeProjectXMLFile(parser.read_setting(xpath1)+parser.read_setting(xpath2),projectName,projectFullName,projectVersion,projectAuthor,path);
 		} catch (Exception e) {
 			System.out.println("ERROR by reading the settings-file.");
 			System.exit(-1);
 		}
 		
 		System.out.println("Writing DONE!\nScript DONE!");
 	}
 }
