 package name.aristides.util;
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 
 
 import com.google.api.translate.Language;
 import com.google.api.translate.Translate;
 
 public class TranslatePropertiesFile {
 
 	/**
 	 * @param args
 	 */
 
 	private static final String translateFileName = "/home/aristides/develop/CobrandString_fr_FR";
 	private static final String originalFileName = "/home/aristides/develop/CobrandString";
 	private static final String extension = ".properties";
 	private static final String withOutEmptyLines = originalFileName + "2" + extension;
 	
 	public static void main(String[] args) throws Exception {	
 		SortedProperties tranlatedFile = new SortedProperties();
 		try{
 			deleteEmptyLines();
 			SortedProperties originalFile = loadFile();
 			translate(originalFile, tranlatedFile);
 			//storeFile(tranlatedFile);
 		} catch( java.net.UnknownHostException e){
 			System.out.println("error connecting with ajax.googleapis.com");
 			return;
 		} catch(FileNotFoundException e){
 			System.out.println("error reading the properties file, verify the file name. " + e.getMessage());
 			return;
 		} catch(IOException e){
 			e.printStackTrace();
 			return;
 		}
 		ArrayList<Comment> comments = readComments(withOutEmptyLines);
 		mergeTranslationWithComments(comments, tranlatedFile);
 		
 		System.out.println("Done.");
 	}
 	//this method is use for testing.
 	private static void storeFile(SortedProperties tranlatedFile) {
 		try {
 			tranlatedFile.store(new FileWriter(translateFileName + extension),"CobransStrings de Yodlee.");
 			System.out.println("File write well :)");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 	}
 
 	private static void translate(SortedProperties original, SortedProperties translated) throws java.net.UnknownHostException{
 		// Set the HTTP referrer to your website address.
 		Translate.setHttpReferrer("www.google.com");
 		System.out.println("Translating keys");
 		String translatedText;
 		try {
 			for(String key : original.propertyNames()) {
 				if(original.getProperty(key).isEmpty()){
 					translated.put(key, "");
 				}
 				else{
 					translatedText = Translate.execute(original.getProperty(key),Language.ENGLISH, Language.FRENCH);
 					translated.put(key, translatedText);
 				}
 			}
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		System.out.println("Translate has finish");
 	}
 
 	private static SortedProperties loadFile() throws IOException{
 		SortedProperties properties = new SortedProperties();
 	
 		properties.load(new FileReader(originalFileName + extension));
 		properties.propertyNames();
 		System.out.println("File load  :)");
 
 		return properties;
 	}
 
 	private static void deleteEmptyLines() throws IOException{
 	    String line = null;
 		// command line parameter
 	   
 	    FileInputStream fstream = new FileInputStream(originalFileName + extension);
 	    // Get the object of DataInputStream
 	    DataInputStream in = new DataInputStream(fstream);
 	    BufferedReader br = new BufferedReader(new InputStreamReader(in));
 	    
 	    PrintWriter pw = new PrintWriter(withOutEmptyLines);
 	    while(br.ready()){
 	    	line = br.readLine();
 	    	if( line != null && !"".equals(line.trim()) ) {
 		    	pw.println(line);
 		    }
 	    } 
 	    pw.close();
 	}
 	
 	private static ArrayList<Comment> readComments(String filename){
 		ArrayList<Comment> comments = new ArrayList<Comment>();
 		try {
 			FileInputStream file = new FileInputStream(filename);
 			BufferedReader d = new BufferedReader(new InputStreamReader(file));
 			int i=1;
 			while(d.ready()){
 				String line = d.readLine();
 				if( line.startsWith("#")){
 					comments.add(new Comment(i, line));
 				}
 				i++;
 			}
 		} catch (IOException e) {
 			System.out.println("Error sorry I can't read the comments");
 		}
 		
 		return comments;
 	}
 	
 	private static void mergeTranslationWithComments(ArrayList<Comment> comments, SortedProperties translatedFile){
 		System.out.println("merge comments with translation has begins.");
 		ArrayList<String> linesToWrite = new ArrayList<String>();
 		int writedLines = 1;
 		boolean writeKey = true;
 		ArrayList<String> keys = translatedFile.propertyNames();
 		while( !keys.isEmpty() || ! comments.isEmpty()){
 			
			if(writedLines == (comments.get(0).getLineNumber())){
 				linesToWrite.add(comments.get(0).getText());
 				comments.remove(0);
 				writeKey = false;
 			}
 				
 			while(writeKey){
 				try{
 				linesToWrite.add(keys.get(0) + "=" +translatedFile.getProperty(keys.get(0)));
 				}catch (IndexOutOfBoundsException e) {
 					e.printStackTrace();
 				}
 				keys.remove(0);
 				break;
 			}
 			writeKey = true;
 			writedLines++;
 		}
 		try {
 			PrintWriter pw = new PrintWriter(translateFileName + "2"+ extension);
 			for(String line : linesToWrite){
 				pw.println(line);
 			}
 			pw.close();
 			System.out.println("File write well :)");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 }
