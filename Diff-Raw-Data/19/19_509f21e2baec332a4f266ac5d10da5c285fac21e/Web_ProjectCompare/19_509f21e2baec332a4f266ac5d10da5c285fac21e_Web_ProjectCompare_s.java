 import java.io.File;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 import tcwi.TCWIFile.ErrorFile;
 import tcwi.TCWIFile.ParserError;
 import tcwi.TCWIFile.TypeError;
 import tcwi.enumFiles.ErrorState;
 import tcwi.exception.Exceptions;
 import tcwi.fileHandler.Check;
 import tcwi.tools.Tools;
 import tcwi.xml.Parser;
 import tcwi.xml.ProjectFile;
 
 public class Web_ProjectCompare {
	private static final String VERSION = "0.1.2.3";
 	private static final String AUTHORS = "EifX & hulllemann";
 	private static Parser parser;
 	private static String folderSeparator = Check.folderSeparator();
 
 	/**
 	 * This method compare two files and returns a compare-summary
 	 * @param main
 	 * @param compare
 	 * @return
 	 */
 	private static ErrorField getCompareStatus(ErrorFile main, ErrorFile compare){
 		if(main==null){
 			main = new ErrorFile();
 		}
 		if(compare==null){
 			compare = new ErrorFile();
 		}
 		
 		ErrorField err = new ErrorField();
 
 		ListDifferences<ParserError> pLD = new ListDifferences<ParserError>();
 		ArrayList<ParserError> pDeletedMain = pLD.getListDiffs(main.getParserError(),compare.getParserError()).k.k;
 		ArrayList<ParserError> pDeletedCompare = pLD.getListDiffs(main.getParserError(),compare.getParserError()).v.k;
 		err.setDelParserErrorsMain(pDeletedMain);
 		err.setDelParserErrorsCompare(pDeletedCompare);
 
 		ListDifferences<TypeError> tLD = new ListDifferences<TypeError>();
 		ArrayList<TypeError> tDeletedMain = tLD.getListDiffs(main.getTypeError(),compare.getTypeError()).k.k;
 		ArrayList<TypeError> tDeletedCompare = tLD.getListDiffs(main.getTypeError(),compare.getTypeError()).v.k;
 		err.setDelTypeErrorsMain(tDeletedMain);
 		err.setDelTypeErrorsCompare(tDeletedCompare);
 		
 		if(main.isCompileError()&&compare.isCompileError())
 			err.setCompileerror(ErrorState.COMPILEERROR);
 		if(!main.isCompileError()&&compare.isCompileError())
 			err.setCompileerror(ErrorState.NOWCOMPILEERROR);
 		if(main.isCompileError()&&!compare.isCompileError())
 			err.setCompileerror(ErrorState.NOWNOCOMPILEERROR);
 		if(!main.isCompileError()&&!compare.isCompileError())
 			err.setCompileerror(ErrorState.NOTHING);
 		
 		if(main.isExcluded()&&compare.isExcluded())
 			err.setExcluded(ErrorState.EXCLUDED);
 		if(!main.isExcluded()&&compare.isExcluded())
 			err.setExcluded(ErrorState.NOWEXCLUDED);
 		if(main.isExcluded()&&!compare.isExcluded())
 			err.setExcluded(ErrorState.NOWNOEXCLUDED);
 		if(!main.isExcluded()&&!compare.isExcluded())
 			err.setExcluded(ErrorState.NOTHING);
 		
 		err.setFilestat(ErrorState.NOCHANGE);
 		return err;
 	}
 	
 	/**
 	 * This Method is the main-analyse method. It gets all file differences and returns an project-compare-summary
 	 * @param main
 	 * @param compare
 	 * @return
 	 */
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	private static ArrayList<Tuple<String,ErrorField>> analyseFiles(ProjectFile main, ProjectFile compare){
 		System.out.println("Check for differences...");
 		//Get all Differences
 		ListDifferences<ErrorFile> ld = new ListDifferences<ErrorFile>();
 		ArrayList<ErrorFile> deletedMain = ld.getListDiffs(main.getFiles(),compare.getFiles()).k.k;
 		ArrayList<ErrorFile> withoutDeletedMain = ld.getListDiffs(main.getFiles(),compare.getFiles()).k.v;
 		ArrayList<ErrorFile> deletedCompare = ld.getListDiffs(main.getFiles(),compare.getFiles()).v.k;
 		ArrayList<ErrorFile> withoutDeletedCompare = ld.getListDiffs(main.getFiles(),compare.getFiles()).v.v;
 		
 		ArrayList<Tuple<String,ErrorField>> result = new ArrayList<Tuple<String,ErrorField>>();
 		
 		//All files without deleted or new created files
 		for(int i=0;i<withoutDeletedMain.size();i++){
 			Tuple<String,ErrorField> t = new Tuple<String,ErrorField>(withoutDeletedMain.get(i).getPath(),getCompareStatus(withoutDeletedMain.get(i), withoutDeletedCompare.get(i)));
 			result.add(t);
 		}
 		//All deleted files
 		for(int i=0;i<deletedMain.size();i++){
 			ErrorField e = getCompareStatus(deletedMain.get(i),null);
 			e.setFilestat(ErrorState.DELETED);
 			Tuple<String,ErrorField> t = new Tuple<String,ErrorField>(deletedMain.get(i).getPath(),e);
 			result.add(t);
 		}
 		//All new created files
 		for(int i=0;i<deletedCompare.size();i++){
 			ErrorField e = getCompareStatus(null,deletedCompare.get(i));
 			e.setFilestat(ErrorState.CREATED);
 			Tuple<String,ErrorField> t = new Tuple<String,ErrorField>(deletedCompare.get(i).getPath(),e);
 			result.add(t);
 		}
 		
 		System.out.println("Sort files...");
 		//Sort files
 		Tuple[] tuple = new Tuple[result.size()];
 		for(int i=0;i<result.size();i++){
 			tuple[i] = result.get(i);
 		}
 
 		Arrays.sort(tuple);
 		result.clear();
 		for(int i=0;i<tuple.length;i++){
 			result.add(tuple[i]);
 		}
 		
 		return result;
 	}
 	
 	/**
 	 * This Method returns a count about all parser errors
 	 * @param res
 	 * @return
 	 */
 	private static int getPErrsCount(ArrayList<Tuple<String,ErrorField>> res){
 		int val=0;
 		for(int i=0;i<res.size();i++){
 			val += res.get(i).v.getParsererrors();
 		}
 		return val;
 	}
 	
 	/**
 	 * This Method returns a count about all type errors
 	 * @param res
 	 * @return
 	 */
 	private static int getTErrsCount(ArrayList<Tuple<String,ErrorField>> res){
 		int val=0;
 		for(int i=0;i<res.size();i++){
 			val += res.get(i).v.getTypeerrors();
 		}
 		return val;
 	}
 	
 	/**
 	 * This Method returns a count about all files who are new created or deleted
 	 * @param res
 	 * @return
 	 */
 	private static int getExFilesCount(ArrayList<Tuple<String,ErrorField>> res){
 		int val=0;
 		for(int i=0;i<res.size();i++){
 			if(res.get(i).v.getExcluded()==ErrorState.NOWEXCLUDED||res.get(i).v.getExcluded()==ErrorState.NOWNOEXCLUDED)
 				val++;
 		}
 		return val;
 	}
 	
 	/**
 	 * This Method returns a count about all compile errors
 	 * @param res
 	 * @return
 	 */
 	private static int getCompFilesCount(ArrayList<Tuple<String,ErrorField>> res){
 		int val=0;
 		for(int i=0;i<res.size();i++){
 			if(res.get(i).v.getCompileerror()==ErrorState.NOWCOMPILEERROR||res.get(i).v.getCompileerror()==ErrorState.NOWNOCOMPILEERROR)
 				val++;
 		}
 		return val;
 	}	
 	
 	/**
 	 * This Method returns a count about all new files
 	 * @param res
 	 * @return
 	 */
 	private static int getNewFilesCount(ArrayList<Tuple<String,ErrorField>> res){
 		int val=0;
 		for(int i=0;i<res.size();i++){
 			if(res.get(i).v.getFilestat()==ErrorState.CREATED)
 				val++;
 		}
 		return val;
 	}	
 	
 	/**
 	 * This Method returns a count about all new files
 	 * @param res
 	 * @return
 	 */
 	private static int getDeletedFilesCount(ArrayList<Tuple<String,ErrorField>> res){
 		int val=0;
 		for(int i=0;i<res.size();i++){
 			if(res.get(i).v.getFilestat()==ErrorState.DELETED)
 				val++;
 		}
 		return val;
 	}
 	
 	private static void writeOutputFile(ProjectFile main, ProjectFile compare, String projectAuthor, String path){
 		try{
 			ArrayList<Tuple<String,ErrorField>> analyse = analyseFiles(main, compare);
 			
 			Calendar c = new GregorianCalendar();
 			String month = Tools.correctCalendarForm(c.get(GregorianCalendar.MONTH)+1);
 			String day = Tools.correctCalendarForm(c.get(GregorianCalendar.DAY_OF_MONTH));
 			String hour = Tools.correctCalendarForm(c.get(GregorianCalendar.HOUR_OF_DAY));
 			String minute = Tools.correctCalendarForm(c.get(GregorianCalendar.MINUTE));
 			String second = Tools.correctCalendarForm(c.get(GregorianCalendar.SECOND));
 
 			System.out.println("Write compare-file...");
 	
 			String mainProjectType ="";
 			String compareProjectType ="";
 			if(main.getIsDelta().equals("true"))
 				mainProjectType = "D";
 			else
 				mainProjectType = "P";
 			if(compare.getIsDelta().equals("true"))
 				compareProjectType = "D";
 			else
 				compareProjectType = "P";
 			
 			File f = new File(path+folderSeparator+main.getIdname()+mainProjectType+"_"+compare.getIdname()+compareProjectType+".compare.xml");
 			f.delete();
 			RandomAccessFile file = new RandomAccessFile(path+folderSeparator+main.getIdname()+mainProjectType+"_"+compare.getIdname()+compareProjectType+".compare.xml","rw");
 			file.writeBytes("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\r\n");
 			file.writeBytes("<project>\r\n");
 			file.writeBytes("	<header>\r\n");
 			file.writeBytes("		<project>\r\n");
 			file.writeBytes("			<idname>"+main.getIdname()+mainProjectType+"_"+compare.getIdname()+compareProjectType+"</idname>\r\n");
 			file.writeBytes("			<fullname>"+main.getFullname()+" "+main.getVersion()+"_"+compare.getFullname()+" "+compare.getVersion()+"</fullname>\r\n");
 			file.writeBytes("			<path>"+main.getPath()+"</path>\r\n");
 			file.writeBytes("		</project>\r\n");
 			file.writeBytes("		<build>\r\n");
 			file.writeBytes("			<builder>"+projectAuthor+"</builder>\r\n");
 			file.writeBytes("			<date>"+c.get(GregorianCalendar.YEAR)+"-"+month+"-"+day+"</date>\r\n");
 			file.writeBytes("			<time>"+hour+":"+minute+":"+second+"</time>\r\n");
 			file.writeBytes("		</build>\r\n");
 			file.writeBytes("		<stats>\r\n");
 			file.writeBytes("			<parsererrors>"+getPErrsCount(analyse)+"</parsererrors>\r\n");
 			file.writeBytes("			<typeerrors>"+getTErrsCount(analyse)+"</typeerrors>\r\n");
 			file.writeBytes("			<excludedfiles>"+getExFilesCount(analyse)+"</excludedfiles>\r\n");
 			file.writeBytes("			<compileerrors>"+getCompFilesCount(analyse)+"</compileerrors>\r\n");
 			file.writeBytes("			<newfiles>"+getNewFilesCount(analyse)+"</newfiles>\r\n");
 			file.writeBytes("			<deletedfiles>"+getDeletedFilesCount(analyse)+"</deletedfiles>\r\n");
 			file.writeBytes("		</stats>\r\n");
 			file.writeBytes("	</header>\r\n");
 			file.writeBytes("	<errors>\r\n");
 
 
 			for(int i=0;i<analyse.size();i++){
 				Tuple<String,ErrorField> act = analyse.get(i);
 				
 				file.writeBytes("		<file>\r\n");
 				file.writeBytes("			<path>"+act.k+"</path>\r\n");
 				file.writeBytes("			<excluded>"+act.v.getExcluded().getString()+"</excluded>\r\n");
 				file.writeBytes("			<compileerror>"+act.v.getCompileerror().getString()+"</compileerror>\r\n");
 				file.writeBytes("			<filestate>"+act.v.getFilestat().getString()+"</filestate>\r\n");
 				file.writeBytes("			<summary>\r\n");
 				file.writeBytes("				<parsererrors>"+act.v.getParsererrors()+"</parsererrors>\r\n");
 				file.writeBytes("				<typeerrors>"+act.v.getTypeerrors()+"</typeerrors>\r\n");
 				file.writeBytes("			</summary>\r\n");
 				file.writeBytes("			<errorlist>\r\n");
 				for(int j=0;j<act.v.getDelParserErrorsMain().size();j++){
 					file.writeBytes("				<oldparsererror>\r\n");
 					file.writeBytes("					<featurestr>"+act.v.getDelParserErrorsMain().get(j).getFeaturestr()+"</featurestr>\r\n");
 					file.writeBytes("					<msg>"+act.v.getDelParserErrorsMain().get(j).getMsg()+"</msg>\r\n");
 					file.writeBytes("					<position>\r\n");
 					file.writeBytes("						<file>"+act.v.getDelParserErrorsMain().get(j).getFile()+"</file>\r\n");
 					file.writeBytes("						<line>"+act.v.getDelParserErrorsMain().get(j).getLine()+"</line>\r\n");
 					file.writeBytes("						<col>"+act.v.getDelParserErrorsMain().get(j).getCol()+"</col>\r\n");
 					file.writeBytes("					</position>\r\n");
 					file.writeBytes("				</oldparsererror>\r\n");
 				}
 				for(int j=0;j<act.v.getDelParserErrorsCompare().size();j++){
 					file.writeBytes("				<newparsererror>\r\n");
 					file.writeBytes("					<featurestr>"+act.v.getDelParserErrorsCompare().get(j).getFeaturestr()+"</featurestr>\r\n");
 					file.writeBytes("					<msg>"+act.v.getDelParserErrorsCompare().get(j).getMsg()+"</msg>\r\n");
 					file.writeBytes("					<position>\r\n");
 					file.writeBytes("						<file>"+act.v.getDelParserErrorsCompare().get(j).getFile()+"</file>\r\n");
 					file.writeBytes("						<line>"+act.v.getDelParserErrorsCompare().get(j).getLine()+"</line>\r\n");
 					file.writeBytes("						<col>"+act.v.getDelParserErrorsCompare().get(j).getCol()+"</col>\r\n");
 					file.writeBytes("					</position>\r\n");
 					file.writeBytes("				</newparsererror>\r\n");
 				}
 				for(int j=0;j<act.v.getDelTypeErrorsMain().size();j++){
 					file.writeBytes("				<oldtypeerror>\r\n");
 					file.writeBytes("					<featurestr>"+act.v.getDelTypeErrorsMain().get(j).getFeaturestr()+"</featurestr>\r\n");
 					file.writeBytes("					<severity>"+act.v.getDelTypeErrorsMain().get(j).getSeverity()+"</severity>\r\n");
 					file.writeBytes("					<msg>"+act.v.getDelTypeErrorsMain().get(j).getMsg()+"</msg>\r\n");
 					file.writeBytes("					<position>\r\n");
 					file.writeBytes("						<file>"+act.v.getDelTypeErrorsMain().get(j).getFromFile()+"</file>\r\n");
 					file.writeBytes("						<line>"+act.v.getDelTypeErrorsMain().get(j).getFromLine()+"</line>\r\n");
 					file.writeBytes("						<col>"+act.v.getDelTypeErrorsMain().get(j).getFromCol()+"</col>\r\n");
 					file.writeBytes("					</position>\r\n");
 					file.writeBytes("					<position>\r\n");
 					file.writeBytes("						<file>"+act.v.getDelTypeErrorsMain().get(j).getToFile()+"</file>\r\n");
 					file.writeBytes("						<line>"+act.v.getDelTypeErrorsMain().get(j).getToLine()+"</line>\r\n");
 					file.writeBytes("						<col>"+act.v.getDelTypeErrorsMain().get(j).getToCol()+"</col>\r\n");
 					file.writeBytes("					</position>\r\n");
 					file.writeBytes("				</oldtypeerror>\r\n");
 				}
 				for(int j=0;j<act.v.getDelTypeErrorsCompare().size();j++){
 					file.writeBytes("				<newtypeerror>\r\n");
 					file.writeBytes("					<featurestr>"+act.v.getDelTypeErrorsCompare().get(j).getFeaturestr()+"</featurestr>\r\n");
 					file.writeBytes("					<severity>"+act.v.getDelTypeErrorsCompare().get(j).getSeverity()+"</severity>\r\n");
 					file.writeBytes("					<msg>"+act.v.getDelTypeErrorsCompare().get(j).getMsg()+"</msg>\r\n");
 					file.writeBytes("					<position>\r\n");
 					file.writeBytes("						<file>"+act.v.getDelTypeErrorsCompare().get(j).getFromFile()+"</file>\r\n");
 					file.writeBytes("						<line>"+act.v.getDelTypeErrorsCompare().get(j).getFromLine()+"</line>\r\n");
 					file.writeBytes("						<col>"+act.v.getDelTypeErrorsCompare().get(j).getFromCol()+"</col>\r\n");
 					file.writeBytes("					</position>\r\n");
 					file.writeBytes("					<position>\r\n");
 					file.writeBytes("						<file>"+act.v.getDelTypeErrorsCompare().get(j).getToFile()+"</file>\r\n");
 					file.writeBytes("						<line>"+act.v.getDelTypeErrorsCompare().get(j).getToLine()+"</line>\r\n");
 					file.writeBytes("						<col>"+act.v.getDelTypeErrorsCompare().get(j).getToCol()+"</col>\r\n");
 					file.writeBytes("					</position>\r\n");
 					file.writeBytes("				</newtypeerror>\r\n");
 				}
 				file.writeBytes("			</errorlist>\r\n");
 				file.writeBytes("		</file>\r\n");
 			}
 			file.writeBytes("	</errors>\r\n");
 			file.writeBytes("</project>\r\n");
 			file.close();
 		
 		}catch (IOException e){
 			Exceptions.throwException(4, e, true, main.getIdname()+"_"+compare.getIdname()+".compare.xml");
 		}
 	}
 	
 	public static void main(String[] args) {
 		if(args.length!=4){
 			System.out.println("Help - Web_ProjectCompare "+VERSION+" by "+AUTHORS);
 			System.out.println("----------------------------------------------------");
 			System.out.println("\nUsage: Web_ProjectCompare [MAINPROJECT] [COMPAREPROJECT] [GLOBAL_SETTINGS]");
 			System.out.println("\n[MAINPROJECT]");
 			System.out.println("     Base-Project + project-type\n     Ex:example.project OR example.deltaproject\n");
 			System.out.println("[COMPAREPROJECT]");
 			System.out.println("     Compare-Project + project-type\n     Ex:example.project OR example.deltaproject\n");
 			System.out.println("[PROJECTAUTHOR]");
 			System.out.println("     Project author\n");
 			System.out.println("[GLOBAL_SETTINGS]");
 			System.out.println("     Absolute Path for the global_settings.xml\n     (include the name of the settings file)\n");
 		}else{
 			String mainProjectName = args[0];
 			String compareProjectName = args[1];
 			String projectAuthor = args[2];
 			String globalSettings = args[3];
 			
 			System.out.println("Starting compare "+mainProjectName+" with "+compareProjectName);
 
 			parser = new Parser(globalSettings);
 			String projectPath = parser.getSetting_ProjectPath();
 			
 			System.out.println("Load Projects...");
 
 			ProjectFile mainProject = null;
 			ProjectFile compareProject = null;
 			
 			if(mainProjectName.endsWith("compare")||compareProjectName.endsWith("compare"))
 				Exceptions.throwException(15, null, true, "");
 			
 			try {
 				//Try to find the correct projectnames
 				
 				File mainP = new File(projectPath+folderSeparator+mainProjectName+".project.xml");
 				File mainD = new File(projectPath+folderSeparator+mainProjectName+".deltaproject.xml");
 				File compareP = new File(projectPath+folderSeparator+compareProjectName+".project.xml");
 				File compareD = new File(projectPath+folderSeparator+compareProjectName+".deltaproject.xml");
 				
 				if(mainP.exists()){
 					mainProject = Parser.getProject(projectPath+folderSeparator+mainProjectName+".project.xml");
 				}else if(mainD.exists()){
 					mainProject = Parser.getProject(projectPath+folderSeparator+mainProjectName+".deltaproject.xml");
 				}else{
 					Exceptions.throwException(1, null, true, "");
 				}
 				
 				if(compareP.exists()){
 					compareProject = Parser.getProject(projectPath+folderSeparator+compareProjectName+".project.xml");
 				}else if(compareD.exists()){
 					compareProject = Parser.getProject(projectPath+folderSeparator+compareProjectName+".deltaproject.xml");
 				}else{
 					Exceptions.throwException(1, null, true, "");
 				}
 			} catch (IOException e) {
 				Exceptions.throwException(1, e, true, "");
 			} catch (Exception e) {
 				Exceptions.throwException(2, e, true, "");
 			}
 			
 			if(mainProject==null||compareProject==null)
 				Exceptions.throwException(2, null, true, "");
 			
 			if(!mainProject.getPath().equals(compareProject.getPath())){
 				Exceptions.throwException(9, null, true, "");
 			}
 			
 			writeOutputFile(mainProject, compareProject, projectAuthor, projectPath);
 
 			System.out.println("Done!");
 			
 		}
 	}
 
 }
