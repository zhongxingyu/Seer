 import java.io.File;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.util.ArrayList;
 
 import tcwi.TCWIFile.CompareFile;
 import tcwi.TCWIFile.ErrorFile;
 import tcwi.exception.Exceptions;
 import tcwi.fileHandler.Check;
 import tcwi.xml.Parser;
 
 public class Web_ProjectCompare {
	private static final String VERSION = "0.0.0.1";
 	private static final String AUTHORS = "EifX & hulllemann";
 	private static Parser parser;
 	private static Check check = new Check();
 	private static Exceptions exception = new Exceptions();
 	
 	/**
 	 * Write the .project.compare file
 	 * @param path
 	 * @param projectName
 	 */
 	private static void writeCompareFile(String path, String projectName, ArrayList<CompareFile> files){
 		try{
 			File f = new File(path+check.folderSeparator()+projectName+".project.compare");
 			f.delete();
 			RandomAccessFile file = new RandomAccessFile(path+check.folderSeparator()+projectName+".project.compare","rw");
 			for(int i=0;i<files.size();i++){
 				//Check if the project has errors. The result will be saved in the project.compare.xml
 //				if(failureProject==false){
 //					if(files.get(i).haveErrors()){
 //						failureProject = true;
 //					}
 //				}
 				file.writeBytes(files.get(i)+"\r\n");
 			}
 			file.close();
 		}catch (IOException e){
 			exception.throwException(4, e, true, path);
 		}
 	}
 	
 	public static void main(String[] args) {
 		if(args.length!=3){
 			System.out.println("Help - Web_ProjectCompare "+VERSION+" by "+AUTHORS);
 			System.out.println("----------------------------------------------------");
 			System.out.println("\nUsage: Web_ProjectCompare [MAINPROJECT] [COMPAREPROJECT] [GLOBAL_SETTINGS]");
 			System.out.println("\n[MAINPROJECT]");
 			System.out.println("     Base-Project\n");
 			System.out.println("[COMPAREPROJECT]");
 			System.out.println("     Compare-Project\n");
 			System.out.println("[GLOBAL_SETTINGS]");
 			System.out.println("     Absolute Path for the global_settings.xml\n     (include the name of the settings file)\n");
 		}else{
 			String mainProject = args[0];
 			String compareProject = args[1];
 			String globalSettings = args[2];
 			
 			System.out.println("Starting compare "+mainProject+" with "+compareProject);
 
 			parser = new Parser(globalSettings);
 			String[] xpath = {"settings","global","projects","path"};
 			
 			String projectPath = "";
 			
 			try{
 				projectPath = parser.read_setting(xpath);
 			} catch (IOException e) {
 				exception.throwException(1, e, true, "");
 			} catch (Exception e){
 				String path = "";
 				for(int i=0;i<xpath.length;i++){
 					path += xpath[i]+" ";
 				}
 				exception.throwException(2, e, true, path);
 			}
 			
 			String path = "";
 			ArrayList<ErrorFile> mainProjectErrArr = null;
 			ArrayList<ErrorFile> compareProjectErrArr = null;
 			try{
 				path = projectPath + check.folderSeparator() + mainProject + ".project";
 				mainProjectErrArr = ErrorFile.createErrorFileArray(path);
 				path = projectPath + check.folderSeparator() + compareProject + ".project";
 				compareProjectErrArr = ErrorFile.createErrorFileArray(path);
 			}catch (IOException e){
 				exception.throwException(8, e, true, path);
 			}
 			
 			ArrayList<CompareFile> compFileArr = new ArrayList<CompareFile>();
 			
 			if(mainProjectErrArr.size()==compareProjectErrArr.size()){
 				for(int i = 0;i<mainProjectErrArr.size();i++){
					if(mainProjectErrArr.get(i).getPath()==compareProjectErrArr.get(i).getPath()){
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
						CompareFile compFile = new CompareFile(mainProjectErrArr.get(i).getPath(),haveNoDBG,isEmptyFile,isNotTrueSucceeded,haveTypeErrors);
						compFileArr.add(compFile);
 					}else{
 						exception.throwException(9, null, true, "");
 					}
 				}
 			}else{
 				exception.throwException(9, null, true, "");
 			}
 			
 			writeCompareFile(projectPath,mainProject,compFileArr);
 			
 		}
 	}
 
 }
