 package src;
 
 import java.io.*;
 import java.text.DateFormat;
 import java.util.Date;
 
 public class MainLoader {
 
 	public static void main(String[] agrs) {
 
 		FilesLoader filesLoader = new FilesLoader();
 		LogLoader logLoader = new LogLoader();
 		String sourcePath = "/Users/apple/github.com/iSuper-FIS-SEA/FileLoader/tmp";
 		String distPath = "/Users/apple/github.com/iSuper-FIS-SEA/FileLoader";
 		String startTime = DateFormat.getTimeInstance().format(new Date());
 		String endTime;
 
 		filesLoader.searchFile(sourcePath);
		filesLoader.moveFile(distPath, 0);
 		
 		//filesLoader.moveAllFile(distPath);
 		endTime = DateFormat.getTimeInstance().format(new Date());
 		logLoader.generateLog(sourcePath, filesLoader.getFilename(0),
 				startTime, endTime, logLoader.diffTime(startTime, endTime));
 	}
 }
