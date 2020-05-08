 package com.nextcentury.TripletExtraction;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 
 import org.apache.log4j.Logger;
 
 import edu.stanford.nlp.ling.CoreLabel;
 import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
 import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
 import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
 import edu.stanford.nlp.pipeline.Annotation;
 import edu.stanford.nlp.pipeline.StanfordCoreNLP;
 import edu.stanford.nlp.util.CoreMap;
 
 public class CoreNlpPOSTagger{
 	
 	private static Logger log = Logger.getLogger(TestDriver.class);
 	Properties props;
 	StanfordCoreNLP pipeline;
 	
 	public CoreNlpPOSTagger() {
 		props = new Properties();
 		props.put("annotators", "tokenize, ssplit, pos");
 		pipeline = new StanfordCoreNLP(props);
 	}
 	
 	public List<TaggedToken> getTaggedText(String text) {
 		ArrayList<TaggedToken> result = new ArrayList<TaggedToken>();
 		
 		Annotation document1 = new Annotation(text);
 		pipeline.annotate(document1);
 		List<CoreMap> sentences = document1.get(SentencesAnnotation.class);
 		for(CoreMap sentence : sentences) {
 			for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
 				result.add(new TaggedToken(token.get(PartOfSpeechAnnotation.class), token.toString()));
 			}
 		}
 		
 		return result;
 	}
 	
 	public String getTaggedSentenceString(String text) {
 		List<TaggedToken> tagged = getTaggedText(text);
 		
 		String resultString = "";
 		
 		for(TaggedToken token : tagged) {
 			resultString = resultString + "[" + token.tag + "]" + token.token + " ";
 		}
 		resultString = resultString.trim();
 		return resultString;
 	}
 	
 	public List<TaggedToken[]> getTaggedSentences(String text) {
 		ArrayList<TaggedToken> result = new ArrayList<TaggedToken>();
 		ArrayList<TaggedToken[]> output = new ArrayList<TaggedToken[]>();
 		
 		Annotation document1 = new Annotation(text);
 		pipeline.annotate(document1);
 		List<CoreMap> sentences = document1.get(SentencesAnnotation.class);
 		for(CoreMap sentence : sentences) {
 			for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
 				result.add(new TaggedToken(token.get(PartOfSpeechAnnotation.class), token.toString()));
 			}
 			output.add((TaggedToken[]) result.toArray());
 			result.removeAll(result);
 		}
 		
 		return output;
 	}
 	
 	public List<String> getTaggedSentencesString(String text) {
 		ArrayList<String> result = new ArrayList<String>();
 		
 		Annotation document1 = new Annotation(text);
 		pipeline.annotate(document1);
 		List<CoreMap> sentences = document1.get(SentencesAnnotation.class);
 		
 		String resultString = "";
 		TaggedToken taggedToken;
 		
 		for(CoreMap sentence : sentences) {
 			
 			for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
 				taggedToken = new TaggedToken(token.get(PartOfSpeechAnnotation.class), token.toString());
				resultString += resultString + "[" + taggedToken.tag + "]" + taggedToken.token + " ";
 			}
 			
 			result.add(resultString);
 			resultString = "";
 		}
 		
 		return result ;
 	}
 }
