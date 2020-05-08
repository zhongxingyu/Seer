 package org.imirsel.nema.meandre.util;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Date;
 import java.util.NoSuchElementException;
 import java.util.Scanner;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.imirsel.nema.model.Job;
 
 public class ConsoleUtil {
 
 	static protected Log logger = LogFactory.getLog(ConsoleUtil.class);
 
 	private MeandreConsoleDao dao;
 	private String folder;
 
 	public void setDao(MeandreConsoleDao dao) {
 		this.dao = dao;
 	}
 
 	public void setFolder(String folder) {
 		this.folder = folder;
 	}
 
 	public String getFolder() {
 		return folder;
 	}
 
 	public void dumpConsoleToFile(Job job) {
 		if (job.isDone()&&hasEffectiveId(job)) {
 			try {
 
 				String filename = generateFileName(job);
 				File file = new File(filename);
 				if (!file.exists()) {
                                         logger.debug("start to dump job" + job.getName() + " into file "
 							+ filename);
 					FileWriter fstream = new FileWriter(file);
 					BufferedWriter out = new BufferedWriter(fstream);
 
 					out.append("Job ").append(job.getName());
 					out.newLine();
 					out.append("ID:").append(job.getId().toString());
 					out.newLine();
 					out.append("Description:").append(job.getDescription());
 					out.newLine();
 					out.append("URI:").append(job.getExecutionInstanceId());
 					out.newLine();
 					out.append("Token:").append(job.getToken());
 					out.newLine();
 					out.append("Transferred from database at:").append(
 							(new Date()).toString());
 					out.newLine();
 					out.append("*******************************");
 					out.newLine();
 
                                         ConsoleRowHandler handler=new ConsoleRowHandler();
 					handler.setWriter(out);
 
                                         dao.handleEntry(job, handler);
 
 					out.close();
 
 					dao.clearConsole(job);
 					logger.info("Dump the console of Job ("
 							+ job.getName() + ") into file "
 							+ filename);
 				} else {
 					logger.error("job " + job.getFlow().getUri()
 							+ " has already been dumped to " + filename);
 					throw new IllegalArgumentException("job "
 							+ job.getName()
 							+ " has already been dumped to " + filename);
 
 				}
 			} catch (IOException e) {
 				logger.error(e, e);
 			}
 		}
 	}
 
 	public String findConsole(Job job) throws FileNotFoundException {
 		if (job.isDone()) {
 			String filename = generateFileName(job);
 			logger.debug("start to read console of Job:"+job.getName());
 			StringBuilder text = new StringBuilder();
 			String NL = System.getProperty("line.separator");
 			Scanner scanner;
			File file = new File(filename);
			if (!file.exists() || !file.canRead()) {
				text.append("File: " + filename
						+ " does not exist or is not readable.");
				return text.toString();
			}
 			try {
 
				scanner = new Scanner(file);
 				try {
 					while (scanner.hasNextLine()) {
 						text.append(scanner.nextLine() + NL);
 					}
 				} catch (NoSuchElementException e) {
 					text.append("ERROR WHILE READING CONSOLE FILES" + NL);
 					text.append(e.toString());
 					logger.error(e, e);
 				} finally {
 					scanner.close();
 				}
 				logger.debug("finish reading console of Job:"+job.getName());
 			} catch (FileNotFoundException e) {
 				text.append("file " + filename + " does not exist");
 				logger.error(e, e);
 			}
 			return text.toString();
 		} else {
 			return null;
 		}
 	}
 	
 	public void deleteConsole(Job job){
 		String filename = generateFileName(job);
 		if (job.isDone()){
 			File file = new File(filename);
 			if (file.exists()) {
 				file.delete();
 				logger.info("delete the console file of job "+job.getName());
 			}
 			
 		}else{
 			throw new IllegalArgumentException("console of job "
 					+ job.getName()
 					+ " is not done and not dumped" );
 		}
 	}
 
 	private String generateFileName(Job job) {
 		
 //		String uniqueStr = extractJobStr(job.getExecutionInstanceId());
 		String uniqueStr=job.getToken();
 		return folder + File.separator + uniqueStr + ".log";
 
 	}
 
 	private String extractJobStr(String fullUri) {
 		String[] subStrs = fullUri.split("/");
 		if ((subStrs != null) && (subStrs.length > 0)) {
 			return subStrs[subStrs.length-1];
 		} else {
 			return null;
 
 		}
 	}
	private boolean hasEffectiveId(Job job){
 		return (job.getExecutionInstanceId()!=null)&&(job.getExecutionInstanceId()!="");
 	}
 
 }
