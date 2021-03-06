 import java.io.File;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 import tcwi.TCWIFile.CompareFile;
 import tcwi.TCWIFile.ErrorFile;
 import tcwi.exception.Exceptions;
 import tcwi.fileHandler.Check;
 import tcwi.tools.Tools;
 import tcwi.xml.Parser;
 
 public class Web_ProjectCompare {
	private static final String VERSION = "0.0.1.0";
 	private static final String AUTHORS = "EifX & hulllemann";
 	private static Parser parser;
 	private static Check check = new Check();
 	private static Exceptions exception = new Exceptions();
 	private static String newProjectName;
 	private static boolean projectWithChanges;
	
 	/**
 	 * Write the .project.compare file
 	 * @param path
 	 * @param projectName
 	 */
 	private static void writeCompareFile(String path, String projectName, String compareName, ArrayList<CompareFile> files){
 		//Try to find a free name
 		newProjectName = projectName + "_" + compareName + "_compare";
 		if(!check.uniqueCheck(newProjectName, path)){
 			int i = 0;
 			while(!check.uniqueCheck(newProjectName, path)){
 				newProjectName = projectName + "_" + compareName + "_compare"+i;
 				i++;
 				if(i > 1000000){
 					exception.throwException(10, null, true, newProjectName);
 				}
 			}
 		}
 		
 		//Save the .project file
 		projectWithChanges = false;
 		try{
 			File f = new File(path+check.folderSeparator()+projectName+".project.compare");
 			f.delete();
 			RandomAccessFile file = new RandomAccessFile(path+check.folderSeparator()+projectName+".project","rw");
 			for(int i=0;i<files.size();i++){
 				//Check if the project has changes. The result will be saved in the project.xml
 				if(projectWithChanges==false){
 					if(files.get(i).haveChanges()){
 						projectWithChanges = true;
 					}
 				}
 				file.writeBytes(files.get(i)+"\r\n");
 			}
 			file.close();
 		}catch (IOException e){
 			exception.throwException(4, e, true, path);
 		}
 	}
 	
 	/**
 	 * Write the .project.xml file
 	 * @param path
 	 * @param projectName
 	 * @param projectPath
 	 */
 	private static void writeProjectXMLFile(String path, String projectVersion, String projectAuthor, String projectPath){
 		try{
 			Calendar c = new GregorianCalendar();
 			String month = Tools.correctCalendarForm(c.get(GregorianCalendar.MONTH)+1);
 			String day = Tools.correctCalendarForm(c.get(GregorianCalendar.DAY_OF_MONTH));
 			String hour = Tools.correctCalendarForm(c.get(GregorianCalendar.HOUR_OF_DAY));
 			String minute = Tools.correctCalendarForm(c.get(GregorianCalendar.MINUTE));
 			String second = Tools.correctCalendarForm(c.get(GregorianCalendar.SECOND));
 
 			File f = new File(path+check.folderSeparator()+newProjectName+".project.xml");
 			f.delete();
 			RandomAccessFile file = new RandomAccessFile(path+check.folderSeparator()+newProjectName+".project.xml","rw");
 			file.writeBytes("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");//TODO: Datei wird nicht als UTF-8 abgespeichert
 			file.writeBytes("<settings>\r\n");
 			file.writeBytes("     <global>\r\n");
			file.writeBytes("          <project idname=\""+newProjectName+"\" fullname=\""+newProjectName+"\" version=\""+projectVersion+"\" path=\""+projectPath+"\" failureProject=\""+projectWithChanges+"\" type=compare />\r\n");
 			file.writeBytes("          <init builder=\""+projectAuthor+"\" buildday=\""+c.get(GregorianCalendar.YEAR)+"-"+month+"-"+day+"\" buildtime=\""+hour+":"+minute+":"+second+"\" />\r\n");
 			file.writeBytes("     </global>\r\n");
 			file.writeBytes("</settings>\r\n");
 			file.close();
 		}catch (IOException e){
 			exception.throwException(4, e, true, path);
 		}
 	}
 	
 	public static void main(String[] args) {
 		if(args.length!=4){
 			System.out.println("Help - Web_ProjectCompare "+VERSION+" by "+AUTHORS);
 			System.out.println("----------------------------------------------------");
 			System.out.println("\nUsage: Web_ProjectCompare [MAINPROJECT] [COMPAREPROJECT] [GLOBAL_SETTINGS]");
 			System.out.println("\n[MAINPROJECT]");
 			System.out.println("     Base-Project\n");
 			System.out.println("[COMPAREPROJECT]");
 			System.out.println("     Compare-Project\n");
 			System.out.println("[PROJECTAUTHOR]");
 			System.out.println("     Project author\n");
 			System.out.println("[GLOBAL_SETTINGS]");
 			System.out.println("     Absolute Path for the global_settings.xml\n     (include the name of the settings file)\n");
 		}else{
 			String mainProject = args[0];
 			String compareProject = args[1];
 			String projectAuthor = args[2];
 			String globalSettings = args[3];
 			
 			System.out.println("Starting compare "+mainProject+" with "+compareProject);
 
 			parser = new Parser(globalSettings);
 			String projectPath = parser.getSetting_ProjectPath();
 			
 			System.out.println("Load Projects...");
 			String mainProjectVersion = "";
 			String compareProjectVersion = "";
 			String mainProjectPath = "";
 			
 			String path = "";
 			ArrayList<ErrorFile> mainProjectErrArr = null;
 			ArrayList<ErrorFile> compareProjectErrArr = null;
 			try{
 				path = projectPath + check.folderSeparator() + mainProject + ".project";
 				parser = new Parser(path);
 				mainProjectVersion = parser.getSetting_ProjectVersion();
 				mainProjectPath = parser.getSetting_ProjectBasePath();
 				mainProjectErrArr = ErrorFile.createErrorFileArray(path);
 				
 				path = projectPath + check.folderSeparator() + compareProject + ".project";
 				parser = new Parser(path);
 				compareProjectVersion = parser.getSetting_ProjectVersion();
 				compareProjectErrArr = ErrorFile.createErrorFileArray(path);
 			}catch (IOException e){
 				exception.throwException(8, e, true, path);
 			}
 			
 			System.out.println("Compare Projects...");
 			
 			ArrayList<CompareFile> compFileArr = new ArrayList<CompareFile>();
 			
 			if(mainProjectErrArr.size()==compareProjectErrArr.size()){
 				for(int i = 0;i<mainProjectErrArr.size();i++){
 					if(mainProjectErrArr.get(i).getPath().equals(compareProjectErrArr.get(i).getPath())){
 						String haveNoDBG ="";
 						String isEmptyFile = "";
 						String isNotTrueSucceeded ="";
 						String haveTypeErrors ="";
 						if(mainProjectErrArr.get(i).isHaveNoDBG()!=compareProjectErrArr.get(i).isHaveNoDBG()){
 							haveNoDBG = mainProjectErrArr.get(i).isHaveNoDBG()+"|"+compareProjectErrArr.get(i).isHaveNoDBG();
 						}
 						if(mainProjectErrArr.get(i).isEmptyFile()!=compareProjectErrArr.get(i).isEmptyFile()){
 							isEmptyFile = mainProjectErrArr.get(i).isEmptyFile()+"|"+compareProjectErrArr.get(i).isEmptyFile();
 						}
 						if(mainProjectErrArr.get(i).isNotTrueSucceeded()!=compareProjectErrArr.get(i).isNotTrueSucceeded()){
 							isNotTrueSucceeded = mainProjectErrArr.get(i).isNotTrueSucceeded()+"|"+compareProjectErrArr.get(i).isNotTrueSucceeded();
 						}
 						if(mainProjectErrArr.get(i).isHaveTypeErrors()!=compareProjectErrArr.get(i).isHaveTypeErrors()){
 							haveTypeErrors = mainProjectErrArr.get(i).isHaveTypeErrors()+"|"+compareProjectErrArr.get(i).isHaveTypeErrors();
 						}
 						if(haveNoDBG.equals("") && isEmptyFile.equals("") && isNotTrueSucceeded.equals("") && haveTypeErrors.equals("")){
 							
 						}else{
 							CompareFile compFile = new CompareFile(mainProjectErrArr.get(i).getPath(),haveNoDBG,isEmptyFile,isNotTrueSucceeded,haveTypeErrors);
 							compFileArr.add(compFile);
 						}
 					}else{
 						exception.throwException(9, null, true, "");
 					}
 				}
 			}else{
 				exception.throwException(9, null, true, "");
 			}
 			
 			writeCompareFile(projectPath,mainProject,compareProject,compFileArr);
 			writeProjectXMLFile(projectPath,mainProjectVersion+"|"+compareProjectVersion,projectAuthor,mainProjectPath);
 			System.out.println("Done!");
 			
 		}
 	}
 
 }
