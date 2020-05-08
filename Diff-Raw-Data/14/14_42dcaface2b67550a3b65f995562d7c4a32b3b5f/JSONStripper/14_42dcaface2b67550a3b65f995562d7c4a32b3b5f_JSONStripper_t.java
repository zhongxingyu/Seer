 package util;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 
 import com.google.gson.JsonElement;
 import com.google.gson.JsonParser;
 
 public class JSONStripper {
 	
 	private File sourceFile;
 	private File outputFile;
 	
 
 
 	public JSONStripper(File sourceFile, File outputFile) {
 		super();
 		this.sourceFile = sourceFile;
 		this.outputFile = outputFile;
 	}
 
 	/**
 	 * Strips out non json text from the begining and end of file, writes back pure json to the source file. 
 	 * @throws Exception if file format is not [text]json data[text]
 	 */
 	public void cleanFile() throws Exception{
 		JsonParser parser = new JsonParser();
 		try {
 			JsonElement metadata = parser.parse(new FileReader(sourceFile));
 			metadata.isJsonArray();
 		} catch (Exception e) {
 			stripFile();
 		}
 	}
 	
 	private void stripFile () throws Exception {
 		BufferedReader reader = new BufferedReader(new FileReader(sourceFile));
		StringBuilder contents = new StringBuilder("");
 		String line = reader.readLine();
 		String json;
		boolean jsonFound = true;
 		if (!line.startsWith("{")) {
 			while (line != null && !line.contains("{")){
 				line = reader.readLine();
 			}
 			if (line == null) {
				jsonFound = false;
			}else {
				line = line.substring(line.indexOf("{"));
 			}
 		}
 		while (line != null) {
 			contents.append(line);
 			line = reader.readLine();
 		}
		if (jsonFound && !contents.toString().endsWith("}")){
 			int lastMark = contents.lastIndexOf("}");
 			json = contents.substring(0, lastMark +1);
 		}else {
 			json = contents.toString();
 		}
 		try {
 			JsonParser parser = new JsonParser();
 			JsonElement metadata = parser.parse(json);
 			metadata.isJsonArray();
 			FileWriter writer = new FileWriter(outputFile);
 			writer.write(metadata.toString());
 			writer.close();
 		} catch (Exception e) {
 			throw new Exception ("Could not clean json file " + sourceFile);
 		}
 	}
 
 }
