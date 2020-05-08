 import javax.xml.parsers.FactoryConfigurationError;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 import org.xml.sax.SAXException;
 import org.xml.sax.Attributes;
 import org.xml.sax.helpers.DefaultHandler;
 import org.xml.sax.InputSource;
 import java.io.BufferedWriter;
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.FilenameFilter;
 import java.io.InputStream;
 import java.io.FileInputStream;
 import java.io.Reader;
 import java.io.InputStreamReader;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.List;
 import java.util.ArrayList;
 
 import weka.core.Instances;
 import weka.core.converters.ConverterUtils.DataSource;
 
 public class Parser {
 
 	/*Suggested structure:
 	* - Parser class which performs initial streaming in of sgm files
 	* - Tokenizer class which performs stem removal/other text processing to give root words
 	* - Classes that then represent each text mining model
 	* - Classes that run classification algorithms on the document representations
 	* - (optional) implementation of the mapreduce framework via apache hadoop.
 	*/
 	
 	private static String dirpath;
 	private static String directory;
 	private static int sgmCount;
 	private static List<ReutersDocument> completedCollection;
 	
 	private static CustomModel model;  
 	
 	private static DataSource source; 
 	private static Instances data;	
 	private static Instances trainingData;
 	private static Instances testData;
 	
 	public Parser () {}
 	
 	public static void main (String[] args) {
 	
 		dirpath = "reuters21578/reut2-0";				//legacy 
 		directory = "reuters21578";
 		
 		//select model based on command line switch
 		if (args.length == 0) {
 			//some default action
 			System.out.println("Usage:");
 			System.out.println("Windows: java -Xmx256m -cp .;weka.jar Parser {1-3} [-f]");
 			System.out.println("*Nix   : java -Xmx256m -cp .:weka.jar Parser {1-3} [-f]");
 			System.out.println("1. MDM");
 			System.out.println("2. PLSA");
 			System.out.println("3. TF-IDF");
 			System.out.println("-f force regeneration");
 			System.exit(-1);
 		} else {
 			switch(Integer.parseInt(args[0])) {
 				case 1: 
 					model = new MDM(); 
 					break; 
 				case 2: 
 					break; 
 				case 3:
 					model = new TFIDF();
 					break; 
 				default: 
 					model = new SWM();
 					break;
 			}
 		
 		}	
				
		if (args[1].equals("-f")) {	
			File lm = new File("lastModified.dat");
			try{
				lm.delete();
			} catch (Exception e) {
				System.err.println("Error: try deleting lastModified.dat");
 			}
 		}
 		
 		//this main will eventually be abstracted out to a handler class
 			
 		//create a list of all of the sgm files
 		File dir = new File("reuters21578");
 		File[] files = dir.listFiles(new FilenameFilter() {
 			public boolean accept(File dir, String name) {
 				return name.toLowerCase().endsWith(".sgm");
 			}
 		});
 		
 		//regenerate the arff file if something has changed
 		if(filesChanged(files)) {
 			//stick all of the files into the sgm parser
 			loadSGMFiles(files);
 			
 			//pass to lemmatizer? (more accurate classification)
 			//better than stemming but more lookup involved (http://en.wikipedia.org/wiki/Lemmatisation)
 			//uses lucene.
 				
 			//pass to tokenizer for now
 			Tokenizer tk = new Tokenizer(completedCollection);
 			completedCollection = tk.tokenizeDocumentSet();
 			
 			System.out.println("(Tokenizer): Tokenization complete, written to ARFF");
 		} else {
 			System.out.println("(Parser): Files remain unchanged, skipping parsing process");
 		}
 		
 		//load arff data, ready to be passed to the various models 
 		try{	
 			//Get global arff file for training and test data
 			source = new DataSource(directory + "/arff/reut2-all.arff");
 			data = source.getDataSet();
 			//Get training data
 			source = new DataSource(directory + "/arff/reut2-train.arff");
 			trainingData = source.getDataSet();
 			//Get test data
 			source = new DataSource(directory + "/arff/reut2-test.arff");
 			testData = source.getDataSet();
 			
 		} catch (Exception e) {
 			//without the arff file in memory there is little else that we can do, kill the proggy with a helpful message 
 			System.err.println("Unable to open arff file, perhaps there are problems with permission?");
 			e.printStackTrace(); 
 			System.exit(-1);
 		}
 		
 		//perform data model (my view is that the preprocessor will be in its own class w/ instantiation in the model)
 		
 		ClassificationSuite.runJ48(model.runModel(trainingData), model.runModel(testData)); 		
 
 	}
 	
 	public static void parseXMLFile(String file) {
 	
 		//Loads the requested XML file and splits out the desired tags that we are
 		//interested in. This will eventually go to a tokenizer and is stored as
 		//in a document object.
 		
 		List<ReutersDocument> documentCollection = new ArrayList<ReutersDocument>();
 		SAXParserFactory factory = SAXParserFactory.newInstance();
 		factory.setNamespaceAware(true);
 		
 		//SAX Handler callback functions. This needs to be changed into a non anon class.
 		SAXDefaultHandler handler = new SAXDefaultHandler(documentCollection);
 	
 		try {
 	
 			SAXParser parser = factory.newSAXParser();
 			parser.parse(file, handler);
 			documentCollection = handler.getCompletedCollection();
 			System.out.println("(ReutersDocument): Last documented to be added");
 			System.out.println("(ReutersDocument): PROPERTIES:" + documentCollection.remove(documentCollection.size()-1).getTopics() + "/" + documentCollection.remove(documentCollection.size()-1).getLewis() + "/" + documentCollection.remove(documentCollection.size()-1).getCgi() + "/" + documentCollection.remove(documentCollection.size()-1).getoldid() + "/" + documentCollection.remove(documentCollection.size()-1).getnewid());
 			System.out.println("(ReutersDocument): TITLE: " + documentCollection.remove(documentCollection.size()-1).getTitle());
 			System.out.println("(ReutersDocument): DATELINE: " + documentCollection.remove(documentCollection.size()-1).getDateline());
 			System.out.println("(ReutersDocument): BODY: " + documentCollection.remove(documentCollection.size()-1).getText());
 			System.out.println("(PARSER): Parsing complete");
 			System.out.println("(PARSER): Documents in collection, " + documentCollection.size());
 			completedCollection = documentCollection;
 		
 		} catch (Exception e) {
 		
 			System.out.println(e.toString());
 		
 		}
 	
 	
 	}
 	
 	public static void loadSGMFiles(File[] f) {
 		
 		List<ReutersDocument> documentCollection = new ArrayList<ReutersDocument>();
 		SAXParserFactory factory = SAXParserFactory.newInstance();
 		factory.setNamespaceAware(true);
 		SAXDefaultHandler handler = new SAXDefaultHandler(documentCollection);
 		
 		try {
 			
 			//1) First add a root node as file isn't well formed.
 			String ln;
 		    SAXParser parser = factory.newSAXParser();
 			
 			//a)Temp file
 			File file = new File(directory + "/complete.xml");
 			file.createNewFile();
 			
 			
 			//c)Write to file
 			FileWriter fw = new FileWriter(file.getAbsoluteFile());
 			BufferedWriter bw = new BufferedWriter(fw);
 			
 			bw.write("<ReutersRoot>");
 			
 			//go over each file reading and writing into the new file 
 			for (File sgmFile : f) {
 			
 				BufferedReader br = new BufferedReader(new FileReader(sgmFile));
 				
 				while ((ln = br.readLine()) != null) {
 				if(!ln.contains("DOCTYPE")) {
 						bw.write(ln.replaceAll("&#","")+"\n");
 					} 
 				}
 			}
 			
 			bw.write("</ReutersRoot>");
 		
 			bw.flush();
 			bw.close();
 			
 			//Force input source to encode as UTF-8.
 			InputStream is = new FileInputStream(file);
 			Reader reader = new InputStreamReader(is,"UTF-8");
 			
 			InputSource source = new InputSource(reader);
 		    source.setEncoding("UTF-8");
 
 			parser.parse(source, handler);
 			completedCollection = handler.getCompletedCollection();
 
 			System.out.println("(PARSER): Parsing complete - " + completedCollection.size());
 			
 		
 		} catch (Exception e) {
 			
 			System.out.println(e.toString());
 		
 		}
 	}
 	
 	public static List<ReutersDocument> getCompletedCollection() {
 	
 		return completedCollection;
 	
 	}
 	
 	private static boolean filesChanged(File[] f ) {
 		long[] oldDates = new long[f.length]; 
 		long[] currentDates = new long[f.length];
 		boolean changed = false; 
 		
 		try{
 			//retrieve last modified array 
 			ObjectInputStream in = new ObjectInputStream(new FileInputStream("lastModified.dat"));
 			oldDates = (long[]) in.readObject();
 			in.close();
 		} catch (Exception e) { 
 			changed = true;
 		}
 		
 		// go over each file, retrieving the last modified date	
 		for(int i = 0; i < f.length; i++) {
 			currentDates[i] = f[i].lastModified();
 			
 			//if file has different date, then the file has been changed
 			if(currentDates[i] != oldDates[i]) {
 				changed = true; 
 			}
 		}
 		
 		//dump the array of changed values to file
 		try {
 			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("./lastModified.dat"));
 			out.writeObject(currentDates);
 			out.flush();
 			out.close();
 		} catch (Exception e) { System.out.println(":(");
 		}
 		
 		return changed; 
 	}
 
 }
 
 class SAXDefaultHandler extends DefaultHandler {
 
 	private List<ReutersDocument> collection;
 	private boolean title = false;
 	private boolean d = false; 
 	private boolean places = false; 
 	private boolean dateline = false;
 	private boolean topics = false;
 	private boolean body = false;
 	private boolean bodypresent = false;
 	private boolean endofdoc = false;
 	private boolean endoffile = false;
 	private String bodytext;
 			
 	public ReutersDocument document;
 
 	public SAXDefaultHandler(List<ReutersDocument> collection) {
 	
 		this.collection = collection;
 	
 	}
 	
 	public SAXDefaultHandler() {}
 	
 	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
 
 		if (qName.equalsIgnoreCase("reuters")) {
 		
 			//create new reuters document.
 			document = new ReutersDocument();
 			
 			//If split attribute is not-used, just whack into training set.
 			
 			if (attributes.getValue("LEWISSPLIT").compareTo("NOT-USED")!=0) {
 				document.setLewis(attributes.getValue("LEWISSPLIT"));
 			} else {
 				document.setLewis("train");	
 			}
 			
 			//set remaining attributes
 			document.setCgi(attributes.getValue("CGISPLIT"));
 			document.setoldid(attributes.getValue("OLDID"));
 			document.setnewid(attributes.getValue("NEWID"));
 			endofdoc = false;
 		}
 
 		if (qName.equalsIgnoreCase("title")) {
 			title = true;
 		}
 
 		if (qName.equalsIgnoreCase("dateline")) {
 			dateline = true;
 		}
 
 		if (qName.equalsIgnoreCase("body")) {
 			body = true;
 			bodypresent = true;
 		}
 		
 		if (qName.equalsIgnoreCase("topics")) {
 			topics = true;
 		}
 		
 		//"d" contains the names of topics
 		if (qName.equalsIgnoreCase("d")) {
 			d = true;
 		}
 		
 		//we do not care about places, but we are using this to prevent places from occurring in the topic list
 		if (qName.equalsIgnoreCase("places")) {
 			topics = false; 
 			places = true;
 		}
 
 	}	
 
 	public void characters(char ch[], int start, int length) throws SAXException {
 
 		if (title) {
 			document.setTitle(new String(ch, start, length));
 			title = false;
 		}
 
 		if (dateline) {
 			document.setDateline(new String(ch, start, length));
 			dateline = false;
 		}
 		
 		//we don't actually do anything here... this method can be removed
 		if (topics) {
 			//document.setTopicList(new String(ch, start, length));
 			//topics = false;
 		}
 		
 		//dispose of places
 		if (places) {
 			places = false;
 		}
 		
 		//"d" contains the names of topics 
 		if (d && topics) {
 			document.addTopic(new String(ch, start, length));
 			d = false; 
 		}
 
 		if (body) {
 			bodytext = bodytext + new String(ch, start, length);
 		}
 
 	}
 
 	public void endElement(String uri, String localName, String qName) throws SAXException {
 
 		if (qName.equalsIgnoreCase("reuters")) {
 			endofdoc = true;
 			collection.add(document);
 		}
 		
 		if (qName.equalsIgnoreCase("body")) {
 			document.setText(bodytext);
 			body = false;
 			bodytext="";
 		}
 
 		if (qName.equalsIgnoreCase("topics")) {
 			topics = false;
 		}
 		
 		if (qName.equalsIgnoreCase("title")) {
 			title = false;
 		}
 	}
 	
 	public List<ReutersDocument> getCompletedCollection() {
 	
 		return collection;
 	
 	}
 
 }
