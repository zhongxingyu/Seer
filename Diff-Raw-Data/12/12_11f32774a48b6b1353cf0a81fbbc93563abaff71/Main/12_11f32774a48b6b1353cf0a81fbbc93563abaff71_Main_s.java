 package ch.ethz.mlmq.log_analizer;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.io.FileUtils;
 
 public class Main {
 	public static void main(String[] args) throws FileNotFoundException {
 		LogAnalizer l = new LogAnalizer();
 
		if (args.length != 2 && args.length != 3)
			System.out.println("usage: java -jar target.jar <directory_to_log_files> <message_type> (out_file optional)\\n");
 
 		String directoryToLogFiles = args[0];
 		String messageType = args[1];
 
 		PrintStream out;
 		if (args.length == 3) {
 			out = new PrintStream(FileUtils.getFile(args[2])); // Should close the printstream after usage...
 		} else {
 			out = System.out;
 		}
 
 		List<File> files = getPerformanceLogFiles(directoryToLogFiles);
 		for (File file : files) {
 			l.addFile(file);
 		}
 
 		ArrayList<Bucket> buckets = l.getBuckets(messageType, 1000 * 60 * 2);
 
 		CSVPrinter p = new CSVPrinter(buckets, out);
 		p.print();
 	}
 
 	private static List<File> getPerformanceLogFiles(String directoryToLogFiles) {
 		File parent = FileUtils.getFile(directoryToLogFiles);
 
 		List<File> files = new ArrayList<>();
 
 		for (File file : FileUtils.listFiles(parent, new String[] { "log" }, true))
 			if (file.getAbsolutePath().contains("performance_log"))
 				files.add(file);
 
 		return files;
 	}
 }
