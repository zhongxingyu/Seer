 package no.ntnu.tdt4215.group7.service;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 import no.ntnu.tdt4215.group7.utils.Paths;
 
 public class FileServiceImpl implements FileService {
 	
 	static Logger logger = Logger.getLogger(FileServiceImpl.class);
 
 	@Override
 	public List<String> getBookFiles() {
 
 		List<String> masterList = new ArrayList<String>();
 
 		String path = null;
 
		String simonLoc = "C:\\Users\\Simon\\Dropbox\\NTNU\\WEB_INTELLIGENCE\\PROJECT\\NLH-html-20130123-01";
 		String martinLoc = "C:\\Users\\hengsti\\Dropbox\\uni\\a related stuff\\ausland\\A TDT4215 Web intelligence\\Project\\NLH-html-20130123-01";
 		String alessioLoc = "/home/alessio/Scrivania/NLH-html-20130123-01";
 		if (new File(simonLoc).exists()) {
 			path = simonLoc;
 		}else if(new File(alessioLoc).exists()){
 			path = alessioLoc;
 		}
 		else if (new File(martinLoc).exists()) {
 			path = martinLoc;
 		} else if (new File(Paths.LMHB_DIR).exists()) {
 			path = Paths.LMHB_DIR;
 		} else if (new File("data/NLH-html-20130123-01").exists()) {
 			path = "data/NLH-html-20130123-01";
 		} else if (new File("data/book/NLH-html-20130123-01").exists()) {
 			path = "data/book/NLH-html-20130123-01";
 		} else {
 			throw new RuntimeException("Book files not found in any of known locations.");
 		}
 
 		File[] fileListL = new File(path + "/L/").listFiles();
 
 		logger.info(fileListL.length + " Files found at " + path);
 
 		int cnt = 0;
 		for (File file : fileListL) {
 			if (file.getName().matches("^[T|L]\\d.*") && file.isFile()) {
 				cnt++;
 				masterList.add(path + "/L/" + file.getName());
 			}
 		}
 
 		logger.info(cnt + " Files loaded from " + path);
 
 		File[] fileListT = new File(path + "/T/").listFiles();
 
 		logger.info(fileListT.length + " Files found at " + path);
 
 		cnt = 0;
 		for (File file : fileListT) {
 			if (file.getName().matches("^[T|L]\\d.*") && file.isFile()) {
 				cnt++;
 				masterList.add(path + "/T/" + file.getName());
 			}
 		}
 
 		logger.info(cnt + " Files loaded from " + path);
 		return masterList;
 
 	}
 
 	@Override
 	public List<String> getPatientFiles() {
 
 		List<String> results = new ArrayList<String>();
 
 		File[] fileListL = new File(Paths.PATIENT_DATA_DIR).listFiles();
 
 		for (File file : fileListL) {
 			if (file.getName().matches(".*\\.xml$") && file.isFile()) {
 				results.add(Paths.PATIENT_DATA_DIR + "/" + file.getName());
 			}
 		}
 		
 		return results;
 	}
 }
