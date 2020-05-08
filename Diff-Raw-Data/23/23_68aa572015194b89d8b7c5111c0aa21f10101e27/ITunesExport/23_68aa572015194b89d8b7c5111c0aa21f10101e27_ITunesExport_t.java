 package work;
 
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 
 import au.com.bytecode.opencsv.CSVReader;
 
 public class ITunesExport {
 
 	static String[] fieldHeaders;
 	static HashMap<String, Integer> fieldMap = new HashMap<String, Integer>();
 	String[] fields;
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) throws Exception {
 		String inputFile = "input/Jam.txt";
 
 		List<ITunesExport> tracks = readExportFile(inputFile);
 //		System.out.println(Arrays.toString(fieldHeaders));
 
 
//		for (ITunesExport track : tracks) {
//			System.out.println(track.getField("Name") + ":" + track.getField("Location"));
//		}
//
//		System.out.println(Arrays.toString(fieldHeaders));
 
 		// // set field separator to tab
 		// CSVReader reader = new CSVReader(new FileReader(inputFile), '\t');
 
 		// getHeader(reader);
 		// System.out.println(Arrays.toString(fieldHeaders));
 		//
 		// String[] nextLine;
 
 		// nextLine = reader.readNext();
 		// System.out.println(Arrays.toString(nextLine));
 
 		// while ((nextLine = reader.readNext()) != null) {
 		// // nextLine[] is an array of values from the line
 		// System.out.println(nextLine[0] + nextLine[1] + "etc...");
 		// }
 
 	}
 
 	public ITunesExport(String[] fields) {
 		this.fields = fields;
 	}
 
 	public String getField(String fieldName) {
		if (fieldMap.containsKey(fieldName)){
			int index = fieldMap.get(fieldName);
			return fields[index];
		}
		else return "INVALID FIELD NAME";
 	}
 
 	static void getHeader(CSVReader reader) throws IOException {
 		if (fieldHeaders == null){
 			fieldHeaders = reader.readNext();
 			int i =0;
 			for (String f : fieldHeaders){
 				fieldMap.put(f, i++);
 			}
 		}
 	}
 
 	static List<ITunesExport> readExportFile(String filePath)
 			throws IOException {
 		ArrayList<ITunesExport> results = new ArrayList<ITunesExport>();
 
 		// get the field header
 		// set field separator to tab
		CSVReader reader = new CSVReader(new FileReader(filePath), '\t', '\0' );
 		getHeader(reader);
 
 		// get the tracks
 		String[] track;
 		while ((track = reader.readNext()) != null) {
 			// System.out.println(track[0] + track[1] + "etc...");
			System.out.println(Arrays.toString(track));
 			results.add(new ITunesExport(track));
 		}
 
 		return results;
 	}
 
 	class Track {
 
 	}
 
 }
