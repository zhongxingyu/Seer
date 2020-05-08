 package tempeval;
 
 import helperclasses.AnnotationWriter;
 import helperclasses.XMLParser;
 
 import java.io.*;
 import java.util.*;
 
 import org.w3c.dom.*;
 
 import annotationclasses.AuxTokenInfoAnnotation;
 import annotationclasses.DocInfoAnnotation;
 import annotationclasses.EventAnnotation;
 import annotationclasses.SignalAnnotation;
 import annotationclasses.TimeAnnotation;
 
 import dataclasses.AuxTokenInfo;
 import dataclasses.DocInfo;
 import dataclasses.EventInfo;
 import dataclasses.TimeInfo;
 
 
 import edu.stanford.nlp.io.IOUtils;
 import edu.stanford.nlp.ling.CoreAnnotations;
 import edu.stanford.nlp.ling.CoreAnnotations.*;
 import edu.stanford.nlp.ling.CoreLabel;
 import edu.stanford.nlp.pipeline.Annotation;
 import edu.stanford.nlp.pipeline.StanfordCoreNLP;
 import edu.stanford.nlp.trees.Tree;
 import edu.stanford.nlp.trees.TreeCoreAnnotations;
 import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
 import edu.stanford.nlp.time.Timex;
 import edu.stanford.nlp.time.TimeAnnotations;
 import edu.stanford.nlp.time.TimeAnnotations.TimexAnnotation;
 import edu.stanford.nlp.util.CoreMap;
 import edu.stanford.nlp.util.StringUtils;
 
 public class Runner {
 
 	private static final String TRAIN_DIR = 
 			"data/TBAQ-cleaned/AQUAINT";
 	private static final String TEST_DIR =
 			"data/TBAQ-cleaned/TimeBank";
 	private static final String OUTPUT_DIR =
 			"output/TBAQ-cleaned/AQUAINT";
 
 	private static final String EVENT_TRAIN_FILE = 
 			"classifiers/training/event.out";
 	
 	private static final boolean TRAINING = true;
 	private static final boolean NOT_TRAINING = false;
 
 	private static StanfordCoreNLP pipeline;
 	private static EventTagger eventTagger;
 	private static TimexEventTagger timexEventTagger;
 	private static DCTEventTagger dctEventTagger;
 	private static SameSentenceEventTagger sameSentenceEventTagger;
 	private static ConsecutiveEventTagger consecutiveEventTagger;
 	
 	private static int numTraining, numTesting;
 
 	/*
 	 * Gets entire contents of training file.
 	 */
 	private static String getRawText(File child) throws IOException {
 		String fileText = "";
 		String currLine;
 		BufferedReader br = new BufferedReader(new FileReader(child));
 
 		while ((currLine = br.readLine()) != null) {
 			fileText += currLine;
 		}
 
 		br.close();
 
 		return fileText;
 	}
 
 	/*
 	 * Gets training text. This is everything in the <TEXT> tag INCLUDING
 	 * EVENT, TIMEX, and SIGNAL tags themselves.
 	 */
 	private static String getTagText(File child) throws IOException {
 		return XMLParser.getRawTextByTagName(getRawText(child), "<TEXT>", "</TEXT>");
 	}
 
 	/*
 	 * Gets test text. This is everything in the <TEXT> tag EXCLUDING
 	 * EVENT, TIMEX, and SIGNAL tags themselves.
 	 */
 	private static String getPlainText(Document doc) {
 		return XMLParser.getElementTextByTagNameNR(doc.getDocumentElement(), "TEXT");
 	}
 
 	/*
 	 * Builds annotation using raw text as companion.
 	 */
 	private static Annotation getAnnotation(File file, Document doc, boolean train) 
 			throws IOException {
 
 		// Build main annotation
 		String plainText = getPlainText(doc);
 		Annotation annotation = new Annotation(plainText);
 		NodeList dcts = doc.getElementsByTagName("DCT");
 		if (dcts != null) {
 			String dct = dcts.item(0).getTextContent();
 			System.out.println("Got document date: " + dct);
 			annotation.set(CoreAnnotations.DocDateAnnotation.class, dct);
 		}
 		
 		pipeline.annotate(annotation);
 		
 		
 		// Return early if we're training. Everything below this takes
 		// tags from the training file and adds their information to the
 		// annotation, which we should only do in training mode.
 		if (!train)
 			return annotation;
 		
 		// Get raw text
 		String rawText = getTagText(file);
 		int rawIndex = 0;
 		int nextEvent = rawText.indexOf("<EVENT");
 		int nextEventEnd = rawText.indexOf("</EVENT>", nextEvent);
 		int nextTime = rawText.indexOf("<TIMEX3");
 		int nextTimeEnd = rawText.indexOf("</TIMEX3>", nextTime);
 		int nextSignal = rawText.indexOf("<SIGNAL");
 		int nextSignalEnd = rawText.indexOf("</SIGNAL>", nextSignal);
 
 		// Iterate through companion and main sentences in parallel
 		List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);
 		for (CoreMap sentence: sentences) {
 			
 			// Keep state about what we've read so far
 			int mainTokenIndex = 0;
 			EventInfo currEvent = null;
 			TimeInfo currTime = null;
 			CoreLabel lastToken = null;
 			AuxTokenInfo lastTokenAux = null;
 			
 			Tree tree = sentence.get(TreeAnnotation.class);
 			tree.indexLeaves();
 			tree.percolateHeadIndices();
 			
 			List<CoreLabel> tokens = sentence.get(TokensAnnotation.class);
 			for (CoreLabel token: tokens) {
 				String word = token.get(TextAnnotation.class);
 				
 				int wordIndex = rawText.indexOf(word, rawIndex);
 				rawIndex = wordIndex + word.length();
 				
 				// END TAGS
 				
 				// If we just finished an event
 				if (nextEventEnd > 0 && wordIndex > nextEventEnd) {
 					nextEvent = rawText.indexOf("<EVENT", rawIndex);
 					nextEventEnd = rawText.indexOf("</EVENT>", rawIndex);
 					currEvent = null;
 				}
 				
 				// If we just finished a timex
 				if (nextTimeEnd > 0 && wordIndex > nextTimeEnd) {
 					nextTime = rawText.indexOf("<TIMEX3", rawIndex);
 					nextTimeEnd = rawText.indexOf("</TIMEX3>", rawIndex);
 					currTime = null;
 				}
 				
 				// If we just finished a signal
 				if (nextSignalEnd > 0 && wordIndex > nextSignalEnd) {
 					nextSignal = rawText.indexOf("<SIGNAL", rawIndex);
 					nextSignalEnd = rawText.indexOf("</SIGNAL>", rawIndex);	
 				}
 
 				// START TAGS
 				
 				// If we're in an event
 				if (nextEvent > 0 && wordIndex > nextEvent && wordIndex < nextEventEnd) {
 					String eventString = rawText.substring(nextEvent, rawText.indexOf(">", nextEvent));
 					if (currEvent == null)
 						currEvent = new EventInfo(eventString, doc);
 					token.set(EventAnnotation.class, currEvent);
 					currEvent.numTokens++;
 				}
 				
 				// If we're in a timex
 				if (nextTime > 0 && wordIndex > nextTime && wordIndex < nextTimeEnd) {
 					String timeString = rawText.substring(nextTime, rawText.indexOf(">", nextTime));
 					if (currTime == null)
 						currTime = new TimeInfo(timeString);
 					token.set(TimeAnnotation.class, currTime);
 					currTime.numTokens++;
 				}
 				
 				// If we're in a signal
 				if (nextSignal > 0 && wordIndex > nextSignal && wordIndex < nextSignalEnd) {
 					token.set(SignalAnnotation.class, true);
 				}
 				
 				// Handle general token annotations
 				AuxTokenInfo aux = new AuxTokenInfo();
 				aux.tokenOffset = mainTokenIndex++;
 				aux.prev = lastToken;
 				aux.next = null;
 				aux.tree = tree;
 				aux.tree_idx = aux.tokenOffset + 1;
 				if (lastTokenAux != null)
 					lastTokenAux.next = token;
 
 				token.set(AuxTokenInfoAnnotation.class, aux);
 
 				lastToken = token;
 				lastTokenAux = aux;
 			}
 		}
 
 		return annotation;
 	}
 
	
 	private static void findIntervalStrings(Document doc, HashSet<String> interval_strings) throws IOException{
 		Element root = doc.getDocumentElement();
 
 		Element [] signals = XMLParser.getElementsByTagNameNR(XMLParser.getElementByTagNameNR(root, "TEXT"), "SIGNAL");
 		for(Element e : signals) {
 				interval_strings.add(e.getTextContent());
 		}
 	}
 	
 	private static void printIntervalStrings(HashSet<String> interval_strings) throws IOException{
		
 		BufferedWriter out = new BufferedWriter(new FileWriter("interval_strings.txt"));
 		for(String s : interval_strings){
 			out.write(s);
 			out.write('\n');
 			out.flush();
 		}
 		out.close();
 	}
 	
 	/*
 	 * Builds up annotation object with built in CoreNLP annotations as
 	 * well as events.
 	 */
 	private static void train() throws Exception {
 
 		BufferedWriter eventTrainOut = new BufferedWriter(new FileWriter(EVENT_TRAIN_FILE));
		HashSet<String> interval_strings = new HashSet<String>();
 
 		// Read each training file in training directory
 		int numFiles = 0;
 		File directory = new File(TRAIN_DIR);
 		for (File child : directory.listFiles()) {
 			if (child.getName().startsWith("."))
 				continue;
 			
 			numFiles++;
 			if (numFiles > numTraining)
 				break;
 
 			System.out.println("Training on file " + child.getName());
 
 			// Parse XML
 			Document doc = XMLParser.parse(child);
 			
			//find all tokens inbetween SIGNAL tags to use for interval feature
 			//findIntervalStrings(doc, interval_strings);
			
 			Annotation annotation = getAnnotation(child, doc, TRAINING);
 
 			//Need doc info for dctEventTagger
 			String rawText = getRawText(child);
 			addDocumentInfo(annotation, doc, rawText, child.getName());
 			
 			//Print out file to train classifier upon
 			EventTagger.printEventAnnotations(annotation, eventTrainOut);
 
 			// Annotate with same-sentence event-timex pairs
 			timexEventTagger.train(annotation, doc);
 
 			//Annotate with document creation time-event pairs
 			dctEventTagger.train(annotation, doc);
 
 			//Annotate with same-sentence event-event pairs
 			sameSentenceEventTagger.train(annotation, doc);
 
 			//Annotate with consecutive-sentence main event pairs
 			consecutiveEventTagger.train(annotation, doc);
 		}
 		eventTrainOut.close();
 		timexEventTagger.doneClassifying();
 		dctEventTagger.doneClassifying();
 		sameSentenceEventTagger.doneClassifying();
 		consecutiveEventTagger.doneClassifying();
 		
		printIntervalStrings(interval_strings);
 	}
 
 	private static void addDocumentInfo(Annotation annotation, Document doc, 
 			String rawText, String filename) {
 		Element root = doc.getDocumentElement();
 		String id = XMLParser.getElementTextByTagNameNR(root, "DOCID");
 		String dct = XMLParser.getRawTextByTagName(rawText, "<DCT>", "</DCT>");
 		String title = XMLParser.getElementTextByTagNameNR(root, "TITLE");
 		String extra = XMLParser.getElementTextByTagNameNR(root, "EXTRAINFO");
 		DocInfo info = new DocInfo(filename, id, dct, title, extra);
 		annotation.set(DocInfoAnnotation.class, info);
 	}
 
 	private static void test() throws Exception {
 		eventTagger.loadTestClassifier();
 		timexEventTagger.loadTestClassifier();
 		dctEventTagger.loadTestClassifier();
 		sameSentenceEventTagger.loadTestClassifier();
 		consecutiveEventTagger.loadTestClassifier();
 
 		// Test
 		int numFiles = 0;
 		File directory = new File(TEST_DIR);
 		for (File child : directory.listFiles()) {
 			if (child.getName().startsWith("."))
 				continue;
 			
 			numFiles++;
 			if (numFiles > numTesting)
 				break;
 
 			System.out.println("Testing on file " + child.getName());
 
 			// Parse XML
 			Document doc = XMLParser.parse(child);
 			String rawText = getRawText(child);
 
 			// Do initial annotation
 			Annotation annotation = getAnnotation(child, doc, NOT_TRAINING);
 			Annotation goldenAnnotation = getAnnotation(child, doc, TRAINING);
 
 			// Add document information
 			addDocumentInfo(annotation, doc, rawText, child.getName());
 
 			// Annotate with events
 			eventTagger.test(annotation);
 
 			// Annotate with same-sentence event-timex pairs
 			timexEventTagger.test(annotation, doc);
 
 			//Annotate with document creation time-event pairs
 			dctEventTagger.test(annotation, doc);
 
 			//Annotate with same-sentence event-event pairs
 			sameSentenceEventTagger.test(annotation, goldenAnnotation, doc);
 
 			//Annotate with consecutive-sentence main event pairs
 			consecutiveEventTagger.test(annotation, doc);
 
 			// Write this annotation
 			BufferedWriter out = new BufferedWriter(new FileWriter(OUTPUT_DIR
 					+ "/" + child.getName()));
 			AnnotationWriter.writeAnnotation(annotation, out);
 			out.close();
 		}
 		
 		sameSentenceEventTagger.printStats();
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) throws Exception {
 		
 		// Args
 		if (args.length != 2) {
 			System.out.println("Usage: java Runner [num_train_files] [num_test_files]");
 			System.exit(-1);
 		}
 		
 		numTraining = Integer.parseInt(args[0]);
 		numTesting = Integer.parseInt(args[1]);
 
 		// Create full pipeline
 		Properties fullProps = new Properties();
 		fullProps.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse");
 		//props.put("annotators", "tokenize, ssplit");
 		pipeline = new StanfordCoreNLP(fullProps);
 
 		// Create classifiers
 		eventTagger = new EventTagger();
 		timexEventTagger = new TimexEventTagger();
 		dctEventTagger = new DCTEventTagger();
 		sameSentenceEventTagger = new SameSentenceEventTagger();
 		consecutiveEventTagger = new ConsecutiveEventTagger();
 
 		train();
 		test();
 	}
 }
