 package ie.dcu.cngl.summarizer.main;
 
 import ie.dcu.cngl.summarizer.Aggregator;
 import ie.dcu.cngl.summarizer.Summarizer;
 import ie.dcu.cngl.summarizer.Weighter;
 import ie.dcu.cngl.tokenizer.Structurer;
 
 import java.io.File;
 import java.util.ArrayList;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.NodeList;
 
 
 public class Test {
 	public static void main(String [] args) throws Exception {
 
 		//String text = FileUtils.readFileToString(new File(Test.class.getResource("/data/en/textExample.txt").getFile()), "UTF-8");
 
 		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
 		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
 
 		final File folder = new File("./corpus");
 		ArrayList<String> fileList = new ArrayList<String>();
 		ArrayList<String> texts = new ArrayList<String>();
 
 		for (final File fileEntry : folder.listFiles()) {
 			if (fileEntry.getName().startsWith("scrt")){
 				fileList.add(fileEntry.getAbsolutePath());
 			}
 		}
 
 		System.out.println(fileList.size() + " files to process");
 
 		int i =0;
 		for (String filename : fileList){
 			Document doc = dBuilder.parse(new File(filename));
 
 			NodeList textnodes = doc.getElementsByTagName("p");
 			String currentText = "";
 
 			for (int j = 0; j < textnodes.getLength(); j++){
				currentText += textnodes.item(j).getTextContent() + "\n";
 			}
 			texts.add(i, currentText);
 			i++;
 		}
 
 		System.out.println("Starting summarization");
 
 		long startTime = System.nanoTime();
 
 		Structurer structurer = new Structurer();
 		Weighter weighter = new Weighter();
 		weighter.addAllFeatures();
 		Aggregator aggregator = new Aggregator();
 
 		int count = 1;
 
 		for (String text : texts){
 			//System.out.println(count);
 
 			Summarizer summarizer = new Summarizer(structurer, weighter, aggregator);
 			summarizer.setNumSentences(5);
 
 			String summary = summarizer.summarize(text);
 			System.out.println("****** Print summary after ******");
 			System.out.println(summary);
 			count++;
 		}
 		long endTime = System.nanoTime();
 
 		long duration = endTime - startTime;
 		System.out.println(duration/1000000000);
 	}
 }
