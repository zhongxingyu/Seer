 package tempeval;
 
 import java.io.*;
 import java.util.*;
 
 import org.w3c.dom.*;
 import org.w3c.dom.Document;
 
 import annotationclasses.AuxTokenInfoAnnotation;
 import annotationclasses.EventAnnotation;
 import annotationclasses.SignalAnnotation;
 import annotationclasses.TimeAnnotation;
 
 import dataclasses.AuxTokenInfo;
 import dataclasses.EventInfo;
 import dataclasses.TimeInfo;
 
 import edu.stanford.nlp.ie.AbstractSequenceClassifier;
 import edu.stanford.nlp.ie.crf.CRFClassifier;
 import edu.stanford.nlp.ling.*;
 import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
 import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
 import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
 import edu.stanford.nlp.pipeline.*;
 import edu.stanford.nlp.time.TimeAnnotations.TimexAnnotation;
 import edu.stanford.nlp.time.Timex;
 import edu.stanford.nlp.trees.Tree;
 import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
 import edu.stanford.nlp.util.*;
 
 import edu.stanford.nlp.ie.crf.*;
 import edu.stanford.nlp.ie.AbstractSequenceClassifier;
 import edu.stanford.nlp.ling.CoreLabel;
 import edu.stanford.nlp.ling.CoreAnnotations.AnswerAnnotation;
 import edu.stanford.nlp.util.StringUtils;
 
 public class EventTagger {
 
 	private static final String CRF_CLASSIFIER_FILENAME = 
 			"classifiers/event-model.ser.gz";
 
 	private AbstractSequenceClassifier classifier = null;
 
 
 	/*
 	 * Helper method for testEventTagger
 	 */
 	public String getWordEventType(int index, String [] tagged_data_array){
 		String tag = tagged_data_array[index];
 		int start = tag.lastIndexOf("/");
 		return tag.substring(start + 1);
 	}
 
 	public void loadTestClassifier() {
 		classifier = 
 				CRFClassifier.getClassifierNoExceptions(CRF_CLASSIFIER_FILENAME);
 	}
 	
 	private static String getTense(String pos) {
 		if (pos.equals("VB"))
 			return "INFINITIVE";
 		if (pos.equals("VBD"))
 			return "PAST";
 		if (pos.equals("VBG"))
 			return "PRESPART";
 		if (pos.equals("VBN"))
 			return "PAST";
 		if (pos.equals("VBP"))
 			return "PRESENT";
 		if (pos.equals("VBZ"))
 			return "PRESENT";
 		return "UNKNOWN";
 	}
 
 	/*
 	 * Method to run at test that given our annotations will annotate tokens classified as Events with the appropriate EventInfo object
 	 * Must call loadTestClassifier first!
 	 */
 	public void test(Annotation annotation) {
 
 		String data = "";
 		List<CoreMap> sentences = 
 				annotation.get(CoreAnnotations.SentencesAnnotation.class);
 		for(CoreMap sentence: sentences) {
 			List<CoreLabel> tokens = sentence.get(TokensAnnotation.class);
 			for (CoreLabel token: tokens) {
 				String word = token.get(TextAnnotation.class);
 				data += word + " ";
 			}
 		}
 
 
 		String [] tagged_data_array = classifier.classifyToString(data).split("\\s+");
 		int nextEventID = 0;
 
 		//Perform a mapping from tagged_data_array event classifications to annotations, 
 		//all classifier output appears to tokenize identically to the coreNLP annotator
 		int count = 0;
 		int curTokenNum = 0;
 		sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
 		for(CoreMap sentence: sentences) {
 			
 			CoreLabel lastToken = null;
 			AuxTokenInfo lastTokenAux = null;
 			
 			int tokenIndex = 1;
 			Tree tree = sentence.get(TreeAnnotation.class);
 			tree.indexLeaves();
 			tree.percolateHeadIndices();
 			
 			List<CoreLabel> tokens = sentence.get(TokensAnnotation.class);
 			for (CoreLabel token: tokens) {
 				String word = token.get(TextAnnotation.class);
 				String pos = token.get(PartOfSpeechAnnotation.class);
 				String eventType = getWordEventType(count, tagged_data_array);
 				if(!eventType.equals("O")) {
 					
 					// No event ID needed - this will be handled by
 					// the AnnotationWriter.
 					int id = nextEventID++;
 					EventInfo info = new EventInfo(eventType, "e" + id);
 					info.currEiid = "ei" + id;
 					info.aspect = "NONE";	//TODO fix
 					info.polarity = "POS";	//TODO fix
 					info.pos = pos;
 					info.tense = getTense(pos);
 					token.set(EventAnnotation.class, info);
 				}
 				
 				// Handle timexes
 				Timex timex = token.get(TimexAnnotation.class);
 				if (timex != null) {
 					TimeInfo info = new TimeInfo(timex.tid(), timex.timexType(),
 							timex.value(), null, null);
 					token.set(TimeAnnotation.class, info);
 				}
 				
 				// Handle general token annotations
 				AuxTokenInfo aux = new AuxTokenInfo();
 				aux.tokenOffset = curTokenNum++;
 				aux.prev = lastToken;
 				aux.next = null;
 				aux.tree = tree;
 				aux.tree_idx = tokenIndex;
				tokenIndex++;
 				if (lastTokenAux != null)
 					lastTokenAux.next = token;
 
 				token.set(AuxTokenInfoAnnotation.class, aux);
 
 				lastToken = token;
 				lastTokenAux = aux;
 				
 				count++;
 				tokenIndex++;
 			}
 		}
 
 	}
 
 	/*
 	 * Prints event annotations in two-column format.
 	 */
 	
 	public static void printEventAnnotations(Annotation annotation, BufferedWriter out) 
 			throws IOException {
 		List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
 		for(CoreMap sentence: sentences) {
 			for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
 				String word = token.getString(TextAnnotation.class);
 				EventInfo eventInfo = token.get(EventAnnotation.class);
 				String event = "";
 				if (eventInfo != null) {
 					event = eventInfo.currEventType;
 				}
 
 				out.write(word + " ");
 				if(!event.equals(""))
 					out.write(event);
 				else
 					out.write("O");
 				out.write("\n");
 				out.flush();
 			}
 		}
 		
 	}
  
 }
