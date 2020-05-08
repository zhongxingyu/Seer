 package controllers;
 
 import play.*;
 import play.mvc.*;
 
 //For forms
 import play.data.*;
 import play.data.Form.*;
 
 //Get scala templates
 import views.html.*;
 //Get objects
 import models.*;
 
 //for testing/file
 import java.nio.file.Files;
 import java.io.File;
 import java.util.*;
 
 //For entering data to mysql
 import play.db.*;
 
 //For regex
 import java.util.regex.*;
 
 
 public class NewRun extends Controller{
 	/**
 	 * This function creates the form for adding a new run
 	 */
 	public static Result newRun() {
 		Form<models.NewRun> runForm = Form.form(models.NewRun.class);
         return ok(
             newRun.render(runForm)
         );
 	}
 	
 	
 	public static Result addNewRun(){
 		Form<models.NewRun> runForm = Form.form(models.NewRun.class).bindFromRequest();//Get from info from POST
 		
 		if(runForm.hasErrors()) { //doesn't do much...
 			System.out.println(runForm.errors());
             runForm.discardErrors();
 			System.out.println("Errors in addNewRun");
         }
 		
 		String[] fileFormats = runForm.get().formats.split(",");
 		
 		String commands="";
 		
 		//If multiple fileFormats....
 		for(String format : fileFormats){
 			commands += generateCommand(
 				Platform.getByID(runForm.get().platform.id).name,
 				runForm.get().name,
 				runForm.get().compDir,
 				runForm.get().inputDir,
 				FileFormat.getByName(format).name
 			);
 		}
 		
 		return ok(
 				viewBatch.render(
 					generateBatchFile(commands)
 					)
 			);
 		
 		
 	}
 	/** This method takes a string command and generates a command
 	 * @param Dtype The platform of the run
 	 * @param Dname The name of the run
 	 * @param Dref Reference directory for output
 	 * @param Dinputdir Input directory 
 	 * @param format Format of this particular run
 	 * @return String of command to run the regression
 	 * Example output :
 	 call ant all -Dtype=java -Dname=Idaho.13.4-PDF -Dref=13.4vsNothing-PDF -Dinput.dir=D:\Geoff\PDFs.Nightly
 	 
 	 rmdir D:\Regression\java\Trunk\Idaho.13.4-PDF
 	 
 	 */
 	public static String generateCommand(String Dtype, String Dname, String Dref, String Dinputdir, String format){
 		//Replace one \ with 4 so output is correct.
 		Dinputdir=Dinputdir.replaceAll("\\\\","\\\\\\\\"); //replace one \ for 2 \s
 		return "\n"+
 			"ECHO %time% "+format.toUpperCase()+
 			"\n"+
 			"call ant all -Dtype="+Dtype.toLowerCase()+" -Dname="+Dname+"-"+format+" -Dref="+Dref+" -Dinput.dir="+Dinputdir+
 			"\n"+
 			"rmdir D:\\\\Regression\\\\java\\\\Trunk\\\\"+Dname+
 			"\n";
 	}
 	/** This method takes a string command and enters it into the batch file template
 	 * @param command A string of the command that should be inserted into the template
 	 * @return String of complete batch file
 	 */
 	public static String generateBatchFile(String command){
 		String template = "H:\\QA-Internship\\db_product\\regressionsite\\app\\batchRunScript.bat";//Path to template file
 		String contents = "";
 		try{
 			contents=readFile(template,java.nio.charset.Charset.forName("UTF-8"));
 		}
 		catch (java.io.IOException e){
 			e.printStackTrace();
 		}
 		
 		//Insert command
 		String insertHere="//COMMANDS GO HERE//"; //This is the line to switch out for the command
 		contents=contents.replaceAll(insertHere,command);
 		
 		return contents;
 	}
 	
 	//Set contents of file to a String
 	public static String readFile(String path, java.nio.charset.Charset encoding) throws java.io.IOException{
 		byte[] encoded = Files.readAllBytes(java.nio.file.Paths.get(path));
 		return encoding.decode(java.nio.ByteBuffer.wrap(encoded)).toString();
 	}
 	
	//List all folders available to compare with
 	public static List<String> listCompDirs(){
 		String pathToCompDirs = "";
 		File file = new File(pathToCompDirs);
 		
 		File[] files = file.listFiles();
 		
 		List<String> validDirs = new ArrayList();
 		
 		for(File folder : files){
 			if(folder.isDirectory()){//if it is a directory, add it to the list
 				validDirs.add(folder.toString());
 			}
 		}
 		
 		return validDirs;
 	}
 }
