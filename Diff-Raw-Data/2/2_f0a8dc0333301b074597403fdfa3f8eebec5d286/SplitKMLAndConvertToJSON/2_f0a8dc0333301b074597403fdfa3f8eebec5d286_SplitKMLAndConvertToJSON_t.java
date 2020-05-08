 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.StringWriter;
 import java.util.ArrayList;
 
 import org.apache.commons.io.IOUtils;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.XML;
 
 public class SplitKMLAndConvertToJSON { 
 	public static int PRETTY_PRINT_INDENT_FACTOR = 4;
 	public static String TEST_XML_STRING =
 			"<?xml version=\"1.0\" ?><test attrib=\"moretest\">Turn this to JSON</test>";
 
 	public static ArrayList<String> getFilesForFolder(final File folder) {
 		ArrayList<String> filesInFolder = new ArrayList<String>();
 
 		for (final File fileEntry : folder.listFiles()) {
 			if (fileEntry.isDirectory()) {
 				filesInFolder.addAll(getFilesForFolder(fileEntry));
 			} else {
 				filesInFolder.add(fileEntry.getName());
 			}
 		}
 
 		return filesInFolder;
 	}
 
 	public static void main(String[] args) {
 		String fileFolder = "data/trees/kml/";
 		String outputFolder = "data/trees/json/";
 		final File folder = new File(fileFolder);
 		ArrayList<String> fileList = getFilesForFolder(folder);
 
 		try {
 			for (int i = 0; i < fileList.size(); i++) {
 				String fileName = fileList.get(i);
 				String fileNameMinusExtension = fileName.split("\\.")[0];
 				FileInputStream fisTargetFile;
 				fisTargetFile = new FileInputStream(new File(fileFolder + fileName));
 
 				StringWriter writer = new StringWriter();
				IOUtils.copy(fisTargetFile, writer);
 				String xmlString = writer.toString();
 
 				JSONObject xmlJSONObj = XML.toJSONObject(xmlString);
 				String jsonPrettyPrintString = xmlJSONObj.toString(PRETTY_PRINT_INDENT_FACTOR);
 				
 				FileWriter treeSpeciesJSONFile = new FileWriter(outputFolder + fileNameMinusExtension);
 				BufferedWriter treeSpeciesJSONWriter = new BufferedWriter(treeSpeciesJSONFile);
 				treeSpeciesJSONWriter.write("tree_data(");
 				treeSpeciesJSONWriter.write(jsonPrettyPrintString);
 				treeSpeciesJSONWriter.write(");");
 				treeSpeciesJSONWriter.close();
 				//System.out.println(jsonPrettyPrintString);
 				
 				System.out.print("'" + fileNameMinusExtension + "', ");
 			}
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (JSONException je) {
 			System.out.println(je.toString());
 		}
 		
 		System.out.println("done");
 	}
 }
