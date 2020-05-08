 package controllers;
 
 
 import play.Logger;
 import play.libs.Akka;
 import play.libs.F.*;
 import play.libs.Json;
 import play.mvc.Http.MultipartFormData;
 import play.mvc.Http.MultipartFormData.FilePart;
 
 import ij.ImagePlus;
 import ij.io.FileSaver;
 import ij.plugin.ContrastEnhancer;
 import ij.plugin.JpegWriter;
 import ij.process.ImageProcessor;
 
 import java.io.*;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.*;
 import java.util.Map.Entry;
 import java.util.concurrent.Callable;
 
 import org.apache.commons.io.FileUtils;
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.node.ObjectNode;
 
 import colonyMeasure.ColonyGrid;
 import colonyMeasure.ImageMeasurer;
 import colonyMeasure.ImageMeasurerOptions;
 
 import com.google.common.io.Files;
 
 import models.*;
 import play.data.Form;
 import play.mvc.Controller;
 import play.mvc.Result;
 import play.mvc.Http.MultipartFormData.FilePart;
 import views.html.*;
 import views.html.ia.iasummary;
 import views.html.ns.*;
 
 
 import java.io.*;
 import java.nio.charset.Charset;
 
 public class NScontroller extends Controller {
 	final static Form<NSjob> nsForm = form(NSjob.class);
 	
 	public static Result initNSform(){
 		Form<NSjob> newform = nsForm.fill(new NSjob());
 		return ok(nsform.render(newform, "", ""));
 	}
 	
 	public static Result redirectPreloadedNSform(String jobid){
 		IAjob iajob = IAcontroller.getJobFromJsonFile(jobid);
 		Form<NSjob> newform = nsForm.fill(new NSjob());
 		return ok(nsform.render(newform, iajob.jobid, iajob.jsonSelectedForNS));
 	}
 	
 	public static Result initPreloadedNSform(){
 		//Get form
 		Form<NSjob> filledForm = nsForm.bindFromRequest();
 		NSjob job = filledForm.get();
 		
 		Logger.info("sJOB ID:"+job.jobid);
 		Logger.info("selected for NS JSON:"+job.jsonSelectedForNS);
 		
 		IAjob iajob = IAcontroller.getJobFromJsonFile(job.jobid);
 		iajob.jsonSelectedForNS = job.jsonSelectedForNS;
 		IAcontroller.writeJsonJobToFile(iajob, job.jobid);
 		
 		return redirect("/preloaded-normalizationscoring/"+job.jobid);
 	}
 	
 	
 	public static Result submit(){
 		//Get form
 		Form<NSjob> filledForm = nsForm.bindFromRequest();
 		
 		Logger.info(filledForm.data().toString());
 		
 		//Process submission
 		long submissionStartTime = new Date().getTime(); //start time
 
 		//Get job
 		NSjob job = filledForm.get();
 		
 		Map<String, File> plateFilesMap = new HashMap<String, File>();
 		
 		//Get file(s) uploaded
 		MultipartFormData body = request().body().asMultipartFormData();
 
 		// Check if we have an array definition file
 		FilePart arrayDefCustomFile = body.getFile("arrayDefCustomFile");
 		
 		Logger.error(body.toString());
 		Logger.info("JOBid = "+job.jobid);
 		
 		if(job.jobid == null | job.jobid.isEmpty()){
 			Logger.info("Files uploaded, populating plate file map:");
 			List<FilePart> plateFiles = body.getFiles();
 	        for(FilePart fp : plateFiles){
 	        	File f = fp.getFile();
 	        	Logger.info("--> "+f.getPath() + "\t"+f.exists());
 				plateFilesMap.put(fp.getFilename(), fp.getFile());
 	        }
 		}else{
 			Logger.info("Files preloaded from IA, populating plate file map:");
 			IAjob iajob = IAcontroller.getJobFromJsonFile(job.jobid);
 			Logger.info("selectedForNS = "+iajob.jsonSelectedForNS);
 			List<String> preloadedList = Json.fromJson(Json.parse(iajob.jsonSelectedForNS), List.class);
 			for(String fileName: preloadedList){
 				File file = new File(Constants.JOB_OUTPUT_DIR + File.separator + 
 									job.jobid+"/ia/output_files/"+fileName);
 				Logger.info("\t"+fileName+" -> " + file.getPath());
 				plateFilesMap.put(fileName, file);
 			}
 		}
 		
         //Generate 
         List<String> adList = ComboboxOpts.arrayDef();
         StringBuilder adfiles = new StringBuilder();
         String summaryAD = "Not applied";
         if(job.doArrayDef){
 			//Custom file -ERROR HERE FILE NOT UPLOADING
 			if(adList.indexOf(job.arrayDefPredefined) == adList.size()-1){
 				Logger.info("Found array definition file");
 				//Remove extra non plate files
 	        	plateFilesMap.remove(arrayDefCustomFile.getFilename());
 				adfiles.append(arrayDefCustomFile.getFile().getPath());
 				summaryAD = "Custom upload: " + arrayDefCustomFile.getFilename();
 			}
 			//Predefined array def
 			else if(job.selectedArrayDefPlate != null){
 				String arrayDefDir = Constants.ARRAY_DEF_PATH + "/" + job.arrayDefPredefined;
 				summaryAD = job.arrayDefPredefined + ": ";
 				List<String> adPlatesList = ComboboxOpts.arrayDefPlates(job.arrayDefPredefined);
 				if(adPlatesList.indexOf(job.selectedArrayDefPlate) == 0){
 					//All plates
 					for(int i=1; i<adPlatesList.size(); i++){
 						adfiles.append(arrayDefDir + File.separator + adPlatesList.get(i) );
 						if( i < adPlatesList.size()-1 ){ adfiles.append(':'); }
 					}
 					summaryAD += "All plates";
 				}else{
 					//One plate
 					adfiles.append(arrayDefDir + File.separator + job.selectedArrayDefPlate);
 					summaryAD += job.selectedArrayDefPlate;
 				}
 				
 			}
 		}
         
         //Make directories for output
         if(job.jobid.isEmpty() || job.jobid == null){
         	job.jobid = UUID.randomUUID().toString();
         	Logger.info("Generated new UUID: "+job.jobid);
         }
         
         File outputDir = new File(Constants.JOB_OUTPUT_DIR + File.separator + job.jobid + File.separator);
         File nsDir = new File( outputDir.getPath() + File.separator + "ns" + File.separator);
         File outputFilesDir = new File(nsDir + "output_files"+	File.separator);
         File inputFilesDir = new File(nsDir + "input_files"+	File.separator);
         
         //Remove any ns directory if it exists
         if(nsDir.exists()){
         	nsDir.delete();
         }
         
         boolean res1 = outputDir.mkdir();
         boolean res2 = nsDir.mkdir();
         boolean res3 = outputFilesDir.mkdir();
         boolean res4 = inputFilesDir.mkdir();
         
         StringBuilder inputfiles = new StringBuilder();
         StringBuilder savenames = new StringBuilder();
         List<String> savenamesList = new ArrayList();
         for(Entry<String,File> e: plateFilesMap.entrySet()){
         	inputfiles.append(e.getValue().getPath() + ":");
         	savenames.append(e.getKey() + ":");
         	savenamesList.add(e.getKey());
         }
         String ninputfiles =  inputfiles.substring(0, inputfiles.lastIndexOf(":")) ;
         String nsavenames = savenames.substring(0, savenames.lastIndexOf(":"));
         
         // Prepare arguments
         List<String> c = new ArrayList();
         c.add("Rscript"); 			c.add(Constants.RSCRIPT_PATH);
         c.add("--inputfiles"); 		c.add(ninputfiles);
         c.add("--savenames"); 		c.add(nsavenames);
         c.add("--outputdir"); 		c.add(outputFilesDir.getPath());
         c.add("--replicates"); 		c.add(job.replicates.toString());
         c.add("--linkagecutoff"); 	c.add(job.linkageCutoff.toString());
         
         if(!adfiles.toString().isEmpty()){ 
         	c.add("--adfiles"); c.add(adfiles.toString()); 
         }
         if(job.doScoring){ 
         	c.add("--score"); 
         	c.add("--sfunction"); c.add(job.scoringFunction); 
         }
         Logger.info("#####"+c );
         
         String[] cmd = new String[c.size()];
         c.toArray(cmd);
         
         StringBuilder t = new StringBuilder();
         for(String s: cmd){
         	t.append(s+ " ");
         }
 
         String zipFilePath = nsDir.getPath()+ "normalizationscoring-sgatools-"+job.jobid+".zip";
         StringBuilder shell_output = new StringBuilder();
         StringBuilder shell_output_error = new StringBuilder();
         try{
 	        //Try execute and read
 	        Process p = Runtime.getRuntime().exec(cmd);
 	        
 	        
 	        //Read in output returned from shell
 	        //#########FOR DEBUG ONLY######
 	        BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()) );
 	        String line;
 	        while ((line = in.readLine()) != null) {
 	        	shell_output.append(line+"\n");
 	        }
 	        
 	        //Read in error input stream (if we have some error)
 	        BufferedReader in_error = new BufferedReader(new InputStreamReader(p.getErrorStream()));
 	        String line_error;
 	        while ((line_error = in_error.readLine()) != null) {
 	        	shell_output_error.append(line_error+"\n");
 	        }
 	        in_error.close();
 	        //#############################
 	        Logger.debug(shell_output.toString());
 	        Logger.error(shell_output_error.toString());
 	        // Zip files 
 	        Zipper.zipDir(zipFilePath, outputFilesDir.getPath());
         }catch(Exception e){
         	Logger.error(shell_output.toString());
         	Logger.error("===============================");
         	Logger.error(shell_output_error.toString());
         	//Fatal error
         	filledForm.reject("plateFiles", "Fatal error, please contact developers "+ e.getMessage());
         	return badRequest(nsform.render(filledForm, "", ""));
         }        
         
         //Record time elapsed
         long submissionEndTime = new Date().getTime(); //end time
         long milliseconds = submissionEndTime - submissionStartTime; //check different
         int seconds = (int) (milliseconds / 1000) % 60 ;
         int minutes = (int) ((milliseconds / (1000*60)) % 60);
         
         Map<String, String> outputFilesMap = new HashMap();
         for(File of: outputFilesDir.listFiles()){
        	if(!of.getName().startsWith("combined"))
         		outputFilesMap.put(of.getName(), of.getPath());
         }
         
         //Save some objects
         job.timeElapsed =  minutes + " mins "+ seconds + " secs";
         job.outputFilesMap = outputFilesMap;
         job.summaryAD = summaryAD;
         
         // Save bound data to file
         job.plateFilesMap = plateFilesMap;
         job.downloadZipPath = zipFilePath.replace(Constants.BASE_PUBLIC_DIR, "");
         writeJsonJobToFile(job, job.jobid);
         
         //Direct to summary page
       	return redirect("/normalizationscoring/"+job.jobid);
 	}
 	
 	@SuppressWarnings("unchecked")
 	public static Result showJob(String jobid){
 		NSjob nsJob  = NScontroller.getJobFromJsonFile( jobid );
 		
 		if(nsJob == null) { return ok(errorpage.render("The job you have requested was not found, please check to make your job id is correct", "404")); }
 		return ok(nssummary.render(nsJob));
 	}
 	
 	public static NSjob getJobFromJsonFile(String jobid){
 		try{
 			String jsonString = Help.readFile(Constants.JOB_OUTPUT_DIR + File.separator + 
 					jobid +File.separator +"ns"+ File.separator +
 					jobid + ".json");
 			NSjob nsJob = Json.fromJson(Json.parse(jsonString), NSjob.class);
 			
 			return nsJob;
 		}catch(Exception e){
 			Logger.error(e.getMessage());
 			return null;
 		}
 	}
 	
 	public static boolean writeJsonJobToFile(NSjob job, String jobid){
 		try{
 			JsonNode jn = Json.toJson(job);
 			Help.writeFile(jn.toString(), 
 					Constants.JOB_OUTPUT_DIR + File.separator + 
 					jobid +File.separator +"ns"+ File.separator +
 					jobid + ".json");
 			return true;
 		}catch(Exception e){
 			return false;
 		}
 	}
 	
 }
 
