 package edu.knowitall.tac2013.entitylinking.utils;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import scala.actors.threadpool.Executors;
 import scala.actors.threadpool.TimeUnit;
 import edu.knowitall.collection.immutable.Interval;
 import edu.stanford.nlp.dcoref.CorefChain;
 import edu.stanford.nlp.dcoref.CorefChain.CorefMention;
 import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
 import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefClusterIdAnnotation;
 import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
 import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
 import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
 import edu.stanford.nlp.ling.CoreLabel;
 import edu.stanford.nlp.pipeline.Annotation;
 import edu.stanford.nlp.pipeline.StanfordCoreNLP;
 import edu.stanford.nlp.util.CoreMap;
 
 
 
 
 
 public class StanfordAnnotatorHelperMethods {
 	
 	private StanfordCoreNLP corefPipeline = null;
 	private StanfordCoreNLP regularPipeline = null;
 	private String filePath = "/homes/gws/jgilme1/docs/";
 	
 	
 	public StanfordAnnotatorHelperMethods(Boolean usePipeline){
 		if(usePipeline){
 			Properties corefProps = new Properties();
 		    corefProps.put("annotators", "tokenize, cleanxml, ssplit, pos, lemma, ner, parse, dcoref");
 		    corefProps.put("clean.allowflawedxml", "true");
 		    corefProps.put("ner.useSUTime", "false");
 		    //clean all xml tags
 			this.corefPipeline = new StanfordCoreNLP(corefProps);
 					
 			
 			Properties regularProps = new Properties();
 			regularProps.put("annotators", "tokenize, cleanxml, ssplit, pos, lemma, ner");
 			regularProps.put("clean.allowflawedxml","true");
 			regularProps.put("ner.useSUTime", "false");
 			this.regularPipeline = new StanfordCoreNLP(regularProps);
 		}
 	}
 	
 	private List<CorefMention> getCorefMentions(Annotation document, Integer begOffset){
 		scala.actors.threadpool.ExecutorService executor = Executors.newSingleThreadExecutor();
 		try{
 		  executor.submit(new AnnotationRunnable(document,corefPipeline)).get(3, TimeUnit.MINUTES);
 		}
 		catch(Exception e){
 			return null;
 		}
 		finally{
 			executor.shutdown();
 		}	
 		
 		Map<Integer, CorefChain> graph = document.get(CorefChainAnnotation.class);
 		Integer corefClusterID = null;
 		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
 		
 	    for(CoreMap sentence: sentences){
 	    	for(CoreLabel token: sentence.get(TokensAnnotation.class)){
 	    		if(token.beginPosition() == begOffset){
 	    			corefClusterID = token.get(CorefClusterIdAnnotation.class);
 	    		}
 	    	}
 	    }
 	    if(corefClusterID != null){
 	    	return graph.get(corefClusterID).getMentionsInTextualOrder();
 	    }
 	    else{
 	        return null;	
 	    }
 	}
 
 	public List<Interval> getCorefIntervals(String xmlString, Integer begOffset) {
 		    
 		    Annotation document = new Annotation(xmlString);
 			List<CorefMention> listOfCorefMentions = getCorefMentions(document,begOffset);
 			if(listOfCorefMentions == null){
 			  return new ArrayList<Interval>();	
 			}
 			else{
 	    	List<Interval> offsets = new ArrayList<Interval>();
 	    	for(CorefMention m : listOfCorefMentions){
 	    		offsets.add(getCharIntervalFromCorefMention(document,m.sentNum,m.startIndex,m.endIndex));
 	    	}
 	    	return offsets;
 			}
 	}
 	
 	public String getCorefRepresentativeString(String xmlString, Integer begOffset) {
 	    Annotation document = new Annotation(xmlString);
 		List<CorefMention> listOfCorefMentions = getCorefMentions(document,begOffset);
 		if(listOfCorefMentions == null){
 		  return null;
 		}
 		else{
 			Map<Integer, CorefChain> graph = document.get(CorefChainAnnotation.class);
 			Integer corefClusterID = listOfCorefMentions.get(0).corefClusterID;
 			return graph.get(corefClusterID).getRepresentativeMention().mentionSpan;
 		}
 	}
 	
 	public List<String> getCorefStringMentions(String xmlString, Integer begOffset) {
 	    Annotation document = new Annotation(xmlString);
 		List<CorefMention> listOfCorefMentions = getCorefMentions(document,begOffset);
 		if(listOfCorefMentions == null){
 		  return new ArrayList<String>();	
 		}
 		else{
     	List<String> stringMentions = new ArrayList<String>();
     	for(CorefMention m : listOfCorefMentions){
     		stringMentions.add(m.mentionSpan);
     	}
     	return stringMentions;
 		}
 	}
 	
 	private List<List<CoreLabel>> getNamedEntityTokens(Annotation document){
 		scala.actors.threadpool.ExecutorService executor = Executors.newSingleThreadExecutor();
 		try{
 		  executor.submit(new AnnotationRunnable(document,regularPipeline)).get(3, TimeUnit.MINUTES);
 		}
 		catch(Exception e){
 			return null;
 		}
 		finally{
 			executor.shutdown();
 		}
 		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
     	List<List<CoreLabel>> allTokens = new ArrayList<List<CoreLabel>>();
     	List<CoreLabel> relevantTokens = new ArrayList<CoreLabel>();
     	int sentIndex =0;
 	    for(CoreMap sentence: sentences){
 	    	List<CoreLabel> sentenceTokenList = new ArrayList<CoreLabel>();
 	    	int tokenIndex =0;
 	    	for(CoreLabel token: sentence.get(TokensAnnotation.class)){
 	    			String net = token.get(NamedEntityTagAnnotation.class);
     				token.setIndex(tokenIndex);
     				token.setSentIndex(sentIndex);		    			
 	    			if( (net.equals("ORGANIZATION"))||
 	    				(net.equals("LOCATION")) ||
 	    				(net.equals("PERSON"))
 	    				){
 	    				relevantTokens.add(token);
 	    			}
 	    			sentenceTokenList.add(token);
 	    		tokenIndex +=1 ;
 	    	}
 	    	allTokens.add(sentenceTokenList);
 	    	sentIndex += 1;
 	    }
 	    
     	if(!relevantTokens.isEmpty()){
     		
 	    	List<List<CoreLabel>> matchingTypes = new ArrayList<List<CoreLabel>>();
 	    	List<CoreLabel> firstTokenList = new ArrayList<CoreLabel>();
 	    	firstTokenList.add(relevantTokens.get(0));
 	    	matchingTypes.add(firstTokenList);
 	    	relevantTokens.remove(0);
 	    	for(CoreLabel t : relevantTokens){
 	    		int currIndex = matchingTypes.size()-1;
 	    		List<CoreLabel> lastTokenList = matchingTypes.get(currIndex);
 	    		CoreLabel lastToken = lastTokenList.get(lastTokenList.size()-1);
 	    		
 	    		if((t.sentIndex() == lastToken.sentIndex()) 
 	    				&&  (t.index() == (1 + lastToken.index())) &&
 	    				(t.ner().equals(lastToken.ner()))){
 	    			matchingTypes.get(currIndex).add(t);
 	    		}
 	    		else if((t.ner().equals("LOCATION") && lastToken.ner().equals("LOCATION")) &&
 	    				(t.sentIndex()== lastToken.sentIndex()) && (t.index() == (2 + lastToken.index())) && 
	    				(allTokens.get(t.sentIndex()).get(t.index()-1).originalText().equals(","))){
 	    			matchingTypes.get(currIndex).add(allTokens.get(t.sentIndex()).get(t.index()-1));
 	    			matchingTypes.get(currIndex).add(t);
 	    		}
 	    		else{
 	    			List<CoreLabel> newTokenList = new ArrayList<CoreLabel>();
 	    			newTokenList.add(t);
 	    			matchingTypes.add(newTokenList);
 	    		}
 	    	}
 	    	return matchingTypes;
     	}
     	else{
     		return null;
     	}
 	}
 	
 	private List<String> getNamedEntityStringsByType(String type, List<List<CoreLabel>> namedEntityTokens){
     	//convert lists of tokens into strings
     	List<String> namedEntityList = new ArrayList<String>();
     	for(List<CoreLabel> namedEntity : namedEntityTokens){
     		if(namedEntity.get(0).ner().equals(type)){
 	    		StringBuilder sb = new StringBuilder();
 	    		for(CoreLabel t : namedEntity){
 	    			sb.append(" ");
 	    			sb.append(t.originalText());
 	    		}
 	    		namedEntityList.add(sb.toString().trim());
     		}
     	}
     	return namedEntityList;
 	}
 	
 	public List<String> getMatchingNamedEntities(String xmlString, Integer begOffset) {
 		Annotation document = new Annotation(xmlString);
 		List<List<CoreLabel>> namedEntityTokens = getNamedEntityTokens(document);
 		if(namedEntityTokens == null){
 			return new ArrayList<String>();
 		}
 
 		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
 		String ne = "";
 	    for(CoreMap sentence: sentences){
 	    	for(CoreLabel token: sentence.get(TokensAnnotation.class)){
 	    		if(token.beginPosition() == begOffset){
 	    			ne = token.get(NamedEntityTagAnnotation.class);
 	    		}
 	    	}
 	    }
 	    if((!ne.equals("ORGANIZATION")) && (!ne.equals("PERSON")) && (!ne.equals("LOCATION"))){
 	    	return new ArrayList<String>();
 	    }
 	    else{
 	    	List<String> matchingNamedEntities = getNamedEntityStringsByType(ne,namedEntityTokens);
 	    	List<String> typeAndMatchingNamedEntities = new ArrayList<String>();
 	    	typeAndMatchingNamedEntities.add(ne);
 	    	typeAndMatchingNamedEntities.addAll(matchingNamedEntities);
 	    	return typeAndMatchingNamedEntities;
 	    }
 	}
 	
 	public List<String> getNamedEntitiesByType(String namedEntityType, String xmlString){
 		Annotation document = new Annotation(xmlString);
 		List<List<CoreLabel>> namedEntityTokens = getNamedEntityTokens(document);
 		if(namedEntityTokens == null){
 			return new ArrayList<String>();
 		}
     	List<String> matchingNamedEntities = getNamedEntityStringsByType(namedEntityType,namedEntityTokens);
     	return matchingNamedEntities;
 	}
 	
 	/**
 	 * Given the information from a CorefMention determine the byte offsets
 	 * of the whole mention and return as a knowitall Interval.
 	 * @param document
 	 * @param sentNum
 	 * @param startIndex
 	 * @param endIndex
 	 * @return
 	 */
 	public Interval getCharIntervalFromCorefMention(Annotation document, Integer sentNum, Integer startIndex, Integer endIndex){
 		
 		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
 		CoreMap sentence = sentences.get(sentNum-1);
 		List<CoreLabel> tokens = sentence.get(TokensAnnotation.class);
 		List<CoreLabel> spanningTokens = new ArrayList<CoreLabel>();
 		for(int i = startIndex; i < endIndex; i++){
 			spanningTokens.add(tokens.get(i-1));
 		}
 		
 		return Interval.closed(spanningTokens.get(0).beginPosition(),spanningTokens.get(spanningTokens.size()-1).endPosition());
 		
 	}
 	
 	private class AnnotationRunnable implements Runnable {
 		
 		Annotation doc;
 		StanfordCoreNLP pipeline;
 		public AnnotationRunnable(Annotation document, StanfordCoreNLP pipeline){
 			doc = document;
 			this.pipeline = pipeline;
 		}
 		public void run(){
 			pipeline.annotate(doc);
 		}
 	}
 }
