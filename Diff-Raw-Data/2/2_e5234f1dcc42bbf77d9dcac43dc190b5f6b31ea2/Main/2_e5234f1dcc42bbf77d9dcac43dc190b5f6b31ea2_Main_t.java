 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.UnsupportedEncodingException;
 import java.util.Scanner;
 import edu.stanford.nlp.tagger.maxent.MaxentTagger;
 
 public class Main {
 	public static void main(String[] args) throws IOException, ClassNotFoundException {
 		File folder = new File("./reviews");
 		if (folder.isDirectory()) {
 			filesPOSTagger(folder);
 		}
 		else {
 			System.out.println("Not a folder...");
 		}
 		//filesPOSTagger(listOfFiles);
 		//MaxentTagger tagger = new MaxentTagger("models/english-left3words-distsim.tagger");
 		//String docText = "This airline is cheap and comfortable. I don't think it is safe though.";
 		//System.out.println(tagger.tagString(docText));
 	}
 	
 	public static void filesPOSTagger(File folder) throws IOException {
 		MaxentTagger tagger = new MaxentTagger("models/english-left3words-distsim.tagger");
 		File[] listOfFiles = folder.listFiles();
 		for (File fileEntry : listOfFiles) {
 			if (fileEntry.isDirectory()) {
 				filesPOSTagger(fileEntry);
 			} else {
 				System.out.println("Tagging " + fileEntry.getName() + "...");
 				tagReviews(fileEntry, tagger);
 			}
 		}
 	}
 	
 	public static void tagReviews(File fileEntry, MaxentTagger tagger) throws IOException {
 		Scanner in = new Scanner(new FileReader(fileEntry));
 		String docText = "";
 		//get the review text
 		while(in.hasNext())
			docText += " " + in.next();
 		
 		int startIndex = docText.indexOf("<text>") + "<text>".length();
 		int endIndex = docText.indexOf("</text>");
 		String review = docText.substring(startIndex, endIndex);
 		
 		//tag the review text
 		String taggedReview = tagger.tagString(review);
 		
 		//output to file
 		String taggedDoc = docText.substring(0, startIndex) + taggedReview + docText.substring(endIndex);
 		File outputDir = new File("./output/");
 		if (!(outputDir.exists() && outputDir.isDirectory())) {
 			outputDir.mkdir();
 			System.out.println("Creating output dir ...");
 		}
 		String filename = fileEntry.getName();
 		filename = filename.substring(0, filename.indexOf(".")) + "[tagged]" + filename.substring(filename.indexOf("."));
 		PrintWriter writer = new PrintWriter("./output/" + filename, "UTF-8");
 		writer.print(taggedDoc);
 		writer.flush();
 		writer.close();
 	}
 }
