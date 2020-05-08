 package uk.ac.ebi.fgpt.sampletab;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.ebi.arrayexpress2.magetab.exception.ParseException;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.SampleData;
 import uk.ac.ebi.arrayexpress2.sampletab.parser.SampleTabParser;
 import uk.ac.ebi.arrayexpress2.sampletab.parser.SampleTabSaferParser;
 import uk.ac.ebi.fgpt.sampletab.utils.SampleTabUtils;
 
 public class SampleTabStatusRunnable implements Runnable {
 	
 	private static final Date now = new Date(); 
 	
 	public static final List<File> toCopyToFTP = Collections.synchronizedList(new ArrayList<File>());
 	public static final List<File> toRemoveFromFTP = Collections.synchronizedList(new ArrayList<File>());
 
 	public static final List<File> toAddToDatabase = Collections.synchronizedList(new ArrayList<File>());
 	public static final List<File> toRemoveFromDatabase = Collections.synchronizedList(new ArrayList<File>());
 	
 	private final SampleTabSaferParser stparser = new SampleTabSaferParser();
 	
 	private final File inputFile;
 	private final File ftpDir;
 	
 	public Boolean shouldBePublic = null;
 	public Boolean isLoaded = null;
 	public Boolean isPublic = null;
 
     private Logger log = LoggerFactory.getLogger(getClass());
     
 
 	public SampleTabStatusRunnable(File inputFile, File ftpDir){
 		if (inputFile == null){
 			throw new IllegalArgumentException("inputFile cannot be null");
 		}
 		if (!inputFile.exists()){
 			throw new IllegalArgumentException("inputFile must exist ("+inputFile+")");
 		}
 		this.inputFile = inputFile;
 
 		if (ftpDir == null){
 			throw new IllegalArgumentException("ftpDir cannot be null");
 		}
 		if (!ftpDir.exists()){
 			throw new IllegalArgumentException("ftpDir must exist ("+ftpDir+")");
 		}
 		this.ftpDir = ftpDir;
 	}
 
 	public SampleTabStatusRunnable(File inputFile, String ftpDirFilename){
 		this(inputFile, new File(ftpDirFilename));
 	}
 	
 	public void run() {
 		
     	//each input can in one the following states:
     	//  currently public and should stay public <- do nothing
     	//  currently public and should be private <- the bad state
     	//  currently private and should be public
     	//  currently private and should stay private <- do nothing
     	//  currently not up to date and should be public
     	//  currently not up to date and should be private
 		
     	SampleData sd = null;
         try {
            sd = stparser.parse(inputFile);
         } catch (ParseException e){
             log.error("Unable to parse file "+inputFile);
             e.printStackTrace();
         	return;
         }
     	
         //calculate shouldBePublic
         //use release date inside file
     	if (sd != null){
     		if (sd.msi.submissionReleaseDate == null || sd.msi.submissionReleaseDate.before(now)){
         		//should be public
     			shouldBePublic = true;
     		} else if (sd.msi.submissionReleaseDate.after(now)){
         		//should be private
     			shouldBePublic = false;
     		}
     	}
     	
     	//calculate isLoaded
     	File loadDir = new File(inputFile, "load");
     	File sucessFile = new File(loadDir, inputFile.getName()+".SUCCESS");
         File ageDir = new File(inputFile, "age");
         File ageFile = new File(ageDir, inputFile.getName()+".age.txt");
     	//this is not a perfect check - ideally this should be an API query for last load date
         if (!sucessFile.exists() || ageFile.lastModified() > sucessFile.lastModified()){
             isLoaded = false;
         } else {
             isLoaded = true;
         }
     	
     	
         //calculate isPublic
     	File ftpSubDir = new File(ftpDir, SampleTabUtils.getPathPrefix(inputFile.getName())); 
 		File ftpSubSubDir = new File(ftpSubDir, inputFile.getName());
 		File ftpFile = new File(ftpSubSubDir, "sampletab.txt");
     	if (ftpFile.exists()){
     		isPublic = true;
     	} else {
     		isPublic = false;
     	}
     	
     	log.info(inputFile.getName()+" "+shouldBePublic+" "+isPublic+" "+isLoaded);
     	
     	
     	//now we have the information, determine what we need to do
     	if (shouldBePublic && isPublic && isLoaded){
     		//do nothing
     	} else if (shouldBePublic && isPublic && !isLoaded){
     		//load
     		toAddToDatabase.add(inputFile);
     	} else if (shouldBePublic && !isPublic && isLoaded){
     		//add to ftp site
     		toCopyToFTP.add(inputFile);
     	} else if (shouldBePublic && !isPublic && !isLoaded){
     		//load
     		//add to ftp site
     		toCopyToFTP.add(inputFile);
     		toAddToDatabase.add(inputFile);
     	} else if (!shouldBePublic && isPublic && isLoaded){
     		//remove from ftp site
     		//remove from database
     		toRemoveFromFTP.add(inputFile);
     		toRemoveFromDatabase.add(inputFile);
     	} else if (!shouldBePublic && isPublic && !isLoaded){
     		//remove from ftp site
     		toRemoveFromFTP.add(inputFile);
     	} else if (!shouldBePublic && !isPublic && isLoaded){
     		//remove from database
     		toRemoveFromDatabase.add(inputFile);
     	} else if (!shouldBePublic && !isPublic && !isLoaded){
     		//do nothing
     	}
 
 	}
 
 }
