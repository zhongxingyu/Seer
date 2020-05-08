 package org.panda.mts;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.panda.mts.util.Config;
 import org.panda.mts.util.CsvTool;
 import org.panda.mts.util.ZipTool;
 
 public class ReportService {
 	private Logger log = Logger.getLogger(ReportService.class.getName());
 
 	private Config cf = new Config("config.property");
 	private final String zipFileFolder = cf.getValue("UPLOAD_ZIP_PATH");
 	private final String unzipFileFolder = cf.getValue("UPLOAD_UNZIP_PATH");
 
 	private final String downloadSourcePath = cf
 			.getValue("DOWNLOAD_SOURCE_PATH");
 	private final String downloadTargetPath = cf
 			.getValue("DOWNLOAD_TARGET_PATH");
 
 	public void unzipRequestFiles() {
 
		File folder = new File(zipFileFolder);
 		for (File file : folder.listFiles()) {
 			if (file.isFile()) {
 				processZipFile(file);
 			}
 		}
 
 	}
 
 	public void zipResponseFiles() {
 		File folder = new File(zipFileFolder);
 		for (File file : folder.listFiles()) {
 			if (file.isDirectory()) {
 				proccessResponseFiles(file);
 			}
 		}
 	}
 
 	private Results proccessResponseFiles(File file) {
 		Results results = new Results();
 		log.debug(file.getPath() + " & " + downloadTargetPath + "/"
 				+ file.getName());
 		ZipTool.zip(file.getPath(), downloadTargetPath + "/" + file.getName()
 				+ ".zip");
 		return results;
 	}
 
 	private Results processZipFile(File file) {
 		List<Branch> branchs = CsvTool.getBranchs();
 		Results results = new Results();
 
 		for (Branch branch : branchs) {
 			if (file.getName().length() < 10) {
 				continue;
 			}
 
 			/**
 			 * 4~10byte are bank code of branch file ext name should be .zip
 			 */
 			if (file.getName().substring(4, 10).equals(branch.getCode())
 					&& file.getName().endsWith("zip")) {
 
 				log.debug("branch no:" + branch.getNo() + " has file:"
 						+ file.getPath());
 
 				if (branch.getZipInd() == Branch.ZIP_IND_NEED_FOLDER) {
 					final String zipFileName = file.getName().substring(0,
 							file.getName().length() - 3);
 					/* using filename as folder name */
 					File f1 = new File(unzipFileFolder + "//" + zipFileName);
 					if (!f1.exists())
 						f1.mkdirs();
 
 					ZipTool.unzip(file.getPath(), unzipFileFolder + "//"
 							+ zipFileName);
 					log.debug("unzip file:" + file.getPath() + " to "
 							+ unzipFileFolder + "//" + zipFileName);
 				} else {
 					ZipTool.unzip(file.getPath(), unzipFileFolder);
 
 					log.debug("unzip file:" + file.getPath() + " to "
 							+ unzipFileFolder);
 				}
 
 			}
 		}
 		return results;
 	}
 
 }
