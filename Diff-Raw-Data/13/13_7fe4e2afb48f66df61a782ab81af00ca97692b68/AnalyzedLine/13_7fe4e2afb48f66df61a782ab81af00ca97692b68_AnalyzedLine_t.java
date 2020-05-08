 package br.usp.cata.model;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.commons.collections.map.HashedMap;
 import org.arabidopsis.ahocorasick.SearchResult;
 
 import br.usp.cata.component.Tree;
 import br.usp.pcs.lta.cogroo.entity.Sentence;
 import br.usp.pcs.lta.cogroo.entity.Token;
 import br.usp.pcs.lta.cogroo.tools.sentencedetector.SentenceDetectorME;
 import br.usp.pcs.lta.cogroo.tools.tokenizer.CogrooTokenizer;
 
 
 public class AnalyzedLine {
 	
 	private ArrayList<AnalyzedSegment> analyzedLine;
 	private int errorsFound;
 	private HashedMap starts, ends;
 	private SentenceDetectorME sentenceDetector;
 	private CogrooTokenizer tokenizer;
 	
 	public AnalyzedLine(String inputLine, SentenceDetectorME sentenceDetector, CogrooTokenizer tokenizer) {	
 		this.sentenceDetector = sentenceDetector;
 		this.tokenizer = tokenizer;
 		analyze(inputLine);
 	}
 	
 	private void buildAnalyzedLine(String inputLine, byte[] tokensBytes, int length, Iterator<BrokenRule> errorsIterator) {
 		analyzedLine = new ArrayList<AnalyzedSegment>();
 		
 		int i = 0;		
 		while(errorsIterator.hasNext()) {
 			BrokenRule occurrence = errorsIterator.next();
 			int start = (Integer) starts.get(occurrence.getFirstIndex());
 			int end = (Integer) ends.get(occurrence.getLastIndex() - 1);
 			if(i < start)
 				analyzedLine.add(new AnalyzedSegment(
 						inputLine.substring(i, start), null));
 			
 			analyzedLine.add(new AnalyzedSegment(
 					inputLine.substring(start, end), occurrence.getSuggestion()));
 			
 			i = end;
 		}
 		
 		if(i < inputLine.length())
 			analyzedLine.add(new AnalyzedSegment(
 					inputLine.substring(i, inputLine.length()), null));
 	}
 	
 	private void analyze(String inputLine) {
 		List<Sentence> sentences;
 		ArrayList<BrokenRule> errors;
 		byte[] tokenBytes, tokensBytes;
 		int tokensLength, start, startToken, end, endToken;
 		
 		errorsFound = 0;
 		errors = new ArrayList<BrokenRule>();
 		
 		tokensBytes = new byte[2 * inputLine.length() + 1];
 		starts = new HashedMap();
 		ends = new HashedMap();
 		
 		sentences = sentenceDetector.detectSentences(inputLine);
 		for(Sentence sentence : sentences)
 			tokenizer.tokenizeSentence(sentence);
 		
 		tokensLength = 0;
 		for(Sentence sentence : sentences) {
 			for(Token token : sentence.getTokens()) {
 				int offSet = sentence.getOffset();
 				start = offSet + token.getSpan().getStart();
 				end = offSet + token.getSpan().getEnd();
 				startToken = tokensLength;
 				tokenBytes = (" " + inputLine.substring(
 						offSet + token.getSpan().getStart(), 
 						offSet + token.getSpan().getEnd())).toLowerCase().getBytes();
 				for(int i = 0; i < tokenBytes.length; i++, tokensLength++)
 					tokensBytes[tokensLength] = tokenBytes[i];
 				endToken = tokensLength;
 				
 				starts.put(startToken, start);
 				ends.put(endToken, end);
 			}
 		}		
 		tokenBytes = " ".getBytes();
 		for(int i = 0; i < tokenBytes.length; i++, tokensLength++)
 			tokensBytes[tokensLength] = tokenBytes[i];
 		
 		Iterator<?> searcher = Tree.getTree().search(tokensBytes);
 	    while(searcher != null && searcher.hasNext()) {
 	    	errorsFound++;
 	        SearchResult searchResult = (SearchResult) searcher.next();
 	        errors.add(new BrokenRule(
 	        	(Rule) (searchResult.getOutputs().iterator().next()), searchResult.getLastIndex()));
 	    }
 	    
	    BrokenRule brokenRule = null;
	    for(int i = errors.size() - 1; i >= 0; i--) {
	    	if(brokenRule != null && errors.get(i).getFirstIndex() == brokenRule.getFirstIndex()) {
	    		if(errors.get(i).getLastIndex() < brokenRule.getLastIndex()) {
	    			errors.remove(errors.get(i));
	    			continue;
	    		}
	    		else
	    			errors.remove(brokenRule);
	    	}
	    	brokenRule = errors.get(i);
	    }
	    
 	    buildAnalyzedLine(inputLine, tokensBytes, tokensLength, errors.iterator());
 	}
 	
 	public ArrayList<AnalyzedSegment> getAnalyzedLine() {
 		return analyzedLine;
 	}
 	
 	public int getErrorsFound() {
 		return errorsFound;
 	}
 
 }
