 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.util.ArrayList;
 
 import tcwi.fileHandler.Check;
 import tcwi.xml.Parser;
 import tcwi.exception.Exceptions;
 
 public class Web_WriteFileList {
 	private static Parser parser;
 	private static String project_settings_path;
 	private static String project_settings_xml_path;
 	private static String project_settings_lst_path;
 	private static Exceptions exception = new Exceptions();
 
	private static final String VERSION = "0.0.3.0";
 	private static final String AUTHORS = "EifX & hulllemann";
 	
 	public static void main(String args[]){
 		//Help - Shown if not the right count of parameters are given to the programm
 		if(args.length!=3){
 			System.out.println("Help - web_WriteFileList "+VERSION+" by "+AUTHORS);
 			System.out.println("----------------------------------------------------");
 			System.out.println("\nUsage: web_WriteFileList [PROJECTNAME] [CHECKBOX_STRING] [GLOBAL_SETTINGS]");
 			System.out.println("\n[PROJECTNAME]");
 			System.out.println("\n     Project name\n");
 			System.out.println("\n[CHECKBOX_STRING]");
 			System.out.println("\n     The checkbox string from the website. If the string is marked as empty\n");
 			System.out.println("     with the word 'EMPTY', the whole list will be written\n");
 			System.out.println("\n[GLOBAL_SETTINGS]");
 			System.out.println("\n     Absolute Path for the global_settings.xml\n     (include the name of the settings file)\n");
 		}else{
 			//Read the project dir and settings
 			Parser xmlParser = new Parser(args[2]);
 			
 			String WebIntProjectsPath = "";
 			String[] xpathWebIntProjectsPath = {"settings","global","projects","path"};
 			
 			try{
 				WebIntProjectsPath = xmlParser.read_setting(xpathWebIntProjectsPath);
 			}catch (IOException e){
 				exception.throwException(1, e, true, "");
 			}catch (Exception e){
 				String path = "";
 				for(int i=0;i<xpathWebIntProjectsPath.length;i++){
 					path += xpathWebIntProjectsPath[i]+" ";
 				}
 				exception.throwException(2, e, true, path);
 			}
 			
 			Check check = new Check();
 			String path = WebIntProjectsPath+check.folderSeparator()+args[0];
 
 			project_settings_path = path+".project";
 			project_settings_xml_path = path+".project.xml";
 			project_settings_lst_path = path+".lst";
 			
 			parser = new Parser(project_settings_xml_path);
 			String[] xpath = {"settings","global","project","path"};
 			
 			//If no checkbox-string is given to the program, all files
 			//will be add to the .lst-file
 			if(!args[1].equals("EMPTY")){
 				//Read all needed variables
 				args[1] = args[1].substring(0,args[1].length()-1);
 				String[] strArr = args[1].split("_");
 	
 				int[] intArr = new int[strArr.length];
 				
 				for(int i=0;i<strArr.length;i++){
 					intArr[i] = Integer.valueOf(strArr[i].substring(6));
 				}
 	
 				RandomAccessFile file;
 				File f = new File("");
 				ArrayList<String> writeInFile = new ArrayList<String>();
 				int j = 0,i = 0;
 				
 				try {
 					//Read from .project-file
 					file = new RandomAccessFile(project_settings_path,"r");
 					String projectPath = parser.read_setting(xpath);
 					
 					String str = file.readLine();
 					while(str!=null){
 						if(intArr[i]==j){
 							String[] tmpArr = str.split("\t");
 							writeInFile.add(tmpArr[0].substring(projectPath.length()+1,tmpArr[0].length()));
 							i++;
 						}
 						if(i>=intArr.length){
 							break;
 						}
 						str = file.readLine();
 						j++;
 					}
 					file.close();
 					
 					//Write to .lst-file
 					f = new File(project_settings_lst_path);
 					f.delete();
 					file = new RandomAccessFile(project_settings_lst_path,"rw");
 					for(int k=0;k<writeInFile.size();k++){
 						file.writeBytes(writeInFile.get(k)+"\r\n");
 					}
 					file.close();
 					
 				} catch (FileNotFoundException e) {
 					exception.throwException(7, e, true, f.getAbsolutePath());
 				} catch (IOException e) {
 					exception.throwException(8, e, true, f.getAbsolutePath());
 				} catch (Exception e) {
 					String path_ = "";
 					for(int k=0;k<xpath.length;k++){
 						path_ += xpath[k]+" ";
 					}
 					exception.throwException(2, e, true, path_);
 				}
 			}else{
 				File f = new File("");
 				try{
 					//If no checkbox-string is given, all files from .project will
 					//be add to the .lst-file
 					String projectPath = parser.read_setting(xpath);
 	
 					RandomAccessFile fileRead = new RandomAccessFile(project_settings_path,"r");
 					f = new File(project_settings_lst_path);
 					f.delete();
 					RandomAccessFile fileWrite = new RandomAccessFile(project_settings_lst_path,"rw");
 					
 					String str = fileRead.readLine();
 					while(str!=null){
 						String[] tmpArr = str.split("\t");
 						fileWrite.writeBytes(tmpArr[0].substring(projectPath.length()+1,tmpArr[0].length())+"\r\n");
 						str = fileRead.readLine();
 					}
 					fileRead.close();
 					fileWrite.close();
 					
 				} catch (FileNotFoundException e) {
 					exception.throwException(7, e, true, f.getAbsolutePath());
 				} catch (IOException e) {
 					exception.throwException(8, e, true, f.getAbsolutePath());
 				} catch (Exception e) {
 					String path_ = "";
 					for(int k=0;k<xpath.length;k++){
 						path_ += xpath[k]+" ";
 					}
 					exception.throwException(2, e, true, path_);
 				}
 			}
 		}
 	}
 }
