 /**
  * 
  */
 package model;
 
 import java.io.*;
 import java.util.Date;
 import java.text.*;
 
 public class Logging {
 
 	private static Logging instance = null;
 	static String logFileName = new String("RefereeSystemDefaultLog.log"); // default
 																			// file
 																			// name
 	private boolean competitionLoggingIsActivated = false;
 	private String competitionFileName = "";
 	File logFile;
 	File competitionLogFile;
 	DateFormat dateformat;
 	String fileParent = "";
 	private String teamName = "Unknown";
 	private int competitionNo = 0;
 
 	protected Logging() {
 		logFile = new File(logFileName);
 		BufferedWriter output;
 		boolean exists = logFile.exists();
 		try {
 			if (!exists) {
 				logFile.createNewFile();
 			} else {
 				output = new BufferedWriter(new FileWriter(logFile));
 				output.write("");
 				output.close();
 			}
 		} catch (IOException e) {
 			System.out.println("Exception caught in Logging constructor: " + e);
 		}
 	}
 
 	public static void setFileName(String fileName) {
 		logFileName = fileName;
 	}
 
 	public static Logging getInstance() {
 		if (instance == null) {
 			instance = new Logging();
 		}
 		return instance;
 	}
 
 	public void globalLogging(String logIdentifier, String args) {
 		dateformat = new SimpleDateFormat("HH:mm:ss");
 		try {
 			Date date = new Date();
 			BufferedWriter output;
 			output = new BufferedWriter(new FileWriter(logFile, true));
 			output.write("[" + dateformat.format(date.getTime()) + "] "
 					+ logIdentifier + ": " + args + "\n");
 			output.close();
 		} catch (IOException e) {
 			System.out.println("Exception caught in during File Logging: " + e);
 		}
 	}
 
 	// public void LoggingFileAndCompetitionFile(String logIdentifier, String
 	// args, boolean logToCompetitionFile) {
 	public void competitionLogging(String logIdentifier, String args) {
 		// LoggingFile(logIdentifier, args);
 		// if(logToCompetitionFile) {
 		if (competitionLoggingIsActivated) {
 			try {
 				BufferedWriter output;
 				output = new BufferedWriter(new FileWriter(competitionLogFile,
 						true));
 				output.write(logIdentifier + " " + args + "\n");
 				output.close();
 			} catch (IOException e) {
 			}
 		}
 		// }
 	}
 
 	public void setCompetitionLogging(boolean b) {
 		if (competitionLoggingIsActivated != b) {
 			if (b) {
 				DateFormat dateformat = new SimpleDateFormat(
 						"yyyy-MM-dd_HH-mm-ss");
 				Date date = new Date();
 				System.out.println(fileParent + File.separatorChar
 						+ competitionFileName + "_"
 						+ dateformat.format(date.getTime()) + ".log");
 				competitionLogFile = new File(fileParent + File.separatorChar
 						+ competitionFileName + "_"
 						+ dateformat.format(date.getTime()) + ".log");
 				boolean exists = competitionLogFile.exists();
 				BufferedWriter output;
 				try {
 					if (!exists) {
 						competitionLogFile.createNewFile();
 					} else {
 						output = new BufferedWriter(new FileWriter(
 								competitionLogFile));
 						output.write("");
 						output.close();
 					}
 				} catch (IOException e) {
 					System.out
 							.println("Exception caught in setCompetitionLogging: "
 									+ e);
 				}
 				competitionLoggingIsActivated = true;
 			} else {
 				boolean exists = competitionLogFile.exists();
 				if (exists) {
 					// competitionLogFile.delete();
 					// TODO reset all params
 					fileParent = "";
 					competitionNo = 0;
 					teamName = "Unknown";
 				}
 				competitionLoggingIsActivated = false;
 			}
 		}
 	}
 
 	public void setCompetitionFileName(String fileName) {
 		if (!competitionLoggingIsActivated) {
 			if (fileName != null && fileName != "") {
 				competitionFileName = fileName;
 			}
 		} else {
 			System.out
 					.println("It is not allowed to change the competition filename while the competition is still running");
 		}
 	}
 
 	public void setFilePath(File file) {
 		if (file != null) {
 			fileParent = file.getParent();
 		}
 	}
 
 	public void setTeamName(String name) {
		if (name == null || name.equals("")) {
 			teamName = "Unknown";
 		} else {
 			teamName = name;
 		}
 		System.out.println("Teamname: " + teamName);
 	}
 
 	public void renameCompetitionFile() {
 		DateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
 		Date date = new Date();
 		System.out.println("Rename file to: " + fileParent + File.separatorChar
 				+ teamName + "_Competition_" + competitionNo + "_"
 				+ dateformat.format(date.getTime()) + ".log");
 		competitionLogFile.renameTo(new File(fileParent + File.separatorChar
 				+ teamName + "_Competition_" + competitionNo + "_"
 				+ dateformat.format(date.getTime()) + ".log"));
 	}
 }
