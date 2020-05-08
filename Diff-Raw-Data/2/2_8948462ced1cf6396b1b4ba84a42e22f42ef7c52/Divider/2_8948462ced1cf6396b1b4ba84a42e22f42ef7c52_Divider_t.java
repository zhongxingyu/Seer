 package de.uni.leipzig.asv.zitationsgraph.preprocessing;
 
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Class to split scientific papers into the three parts. We assume that we have only the plain text with line
  * breaks. So we only can use this inherent informations. Any splitter taking into account additional meta data
  * such as layout command within a PDF file would by of a higher order.
  * As of now, we only support a splitting by a brute force algorithm looking for "Introduction" and "References"
  * or "Bibliography" as exact string matches in the full text.
  * Future work: Take into account, that these parts are captions and therefore occurring in a single line, possibly
  * with a heading numeration.
  * 
  * @version 0.1
  * @author Klaus Lyko
  *
  */
 public class Divider {
 	
 	Logger logger = Logger.getLogger("ZitGraph");
 	public static final boolean debug = false;
 	
 	//public static String[] refBoundaries = {"Notes", "Note", "Appendix"};
 	
 	/**
 	 * Holding full text of a scientific paper to be divided.
 	 */
 	private String fullText;
 
 	String head, body, tail;
 	String introName, extroName;
 	
 	
 	public Divider(String fullText) {
 		super();
 		this.fullText = fullText;
 	}
 
 	public void setFullText(String fullText) {
 		this.fullText = fullText;
 	}
 
 	public String getFullText() {
 		return fullText;
 	}
 	
 	
 	/**
 	 * Determines quality of brute force method.
 	 * @return
 	 */
 	public int determineBruteForceMethod() {
 		int quality = 0;
 		int headCount = countOccurrenceOfHeading(fullText, "Introduction");
 		int referenceCount = countOccurrenceOfHeading(fullText, "References");
 		int bibliographyCount = countOccurrenceOfHeading(fullText, "Bibliography");
 		introName = "Introduction";
 			if(referenceCount == 0 && bibliographyCount == 0) {
 				if(debug)
 					logger.warning("Wasn't able to find either 'References' or 'Bibliography' to mark tail.");
 			}
 			if(referenceCount >= 1 && bibliographyCount == 0) {
 				if(debug)
 					logger.info("Using 'Introduction' and 'References'");
 				extroName = "References";		
 			}
 			if(referenceCount == 0 && bibliographyCount >= 1) {
 				if(debug)
 					logger.info("Using 'Introduction' and 'Bibliography'");
 				extroName = "Bibliography";
 			}
 			if(referenceCount > 0 && bibliographyCount > 0) {
 				if(debug)
 					logger.info("Both appearing 'References' and 'Bibliography' atleast once.");
 				if(referenceCount <= bibliographyCount) {
 					if(debug)
 						logger.info("Using 'Introduction' and 'References'");
 					extroName = "References";
 				}
 				else {
 					if(debug)
 						logger.info("Using 'Introduction' and 'Bibliography'");
 					extroName = "Bibliography";
 				}					
 			}		
 		quality += headCount + referenceCount + bibliographyCount;
 		return quality;
 	}
 	
 	/**
 	 * Splits by brute force method.
 	 */
 	public void splitByBruteForce() {
 		if(introName==null && extroName==null)
 			determineBruteForceMethod();
 		splitBy(introName, extroName);
 	}
 	/**
 	 * Splits fullText into head, body and tail using exact string with the specified words. 
 	 * @param intro String dividing head and body, taking first occurrence.
 	 * @param extro String dividing body and tail, taking last occurrence.
 	 */
 	public void splitBy(String intro, String extro) {
 		//defaults
 		body = fullText;
 		head = fullText;
 		tail = fullText;
 		
 		//split references
 		splitTail(extro);
 		
 		//try to get head
 		splitHead(intro);		
 	}
 	
 	private void splitHead(String intro) {		
 		Pattern pattern = Pattern.compile("\\s[0-9]*"+intro+"\\n");
 		Matcher matcher = pattern.matcher(fullText);
 		if(matcher.find()) {
 			if(debug)
 				logger.info("Found "+intro+" at "+matcher.end());
 			head = fullText.substring(0, matcher.start());
 		}else {
 			// try "...." after Abstract
 			if(debug)
 				logger.info("Trying to find abstract");
 			Pattern abstractPattern = Pattern.compile("\\s[0-9]*Abstract\\s");
 			Matcher abstractMatcher = abstractPattern.matcher(fullText);
 			int abstractOffSet = 0;
 			if(abstractMatcher.find()) {
 				if(debug)
 					logger.info("Found Abstract");
 				abstractOffSet = abstractMatcher.end();
 				Pattern pointPattern = Pattern.compile("\\.{4,}");
 				Matcher pointMatcher = pointPattern.matcher(fullText);
 				while(pointMatcher.find()) {
 					if(pointMatcher.end()>abstractOffSet){
 						head=fullText.substring(0, pointMatcher.end());
 						body=fullText.substring(pointMatcher.end());
 						return;
 					}
 				}
 				head = fullText.substring(0, abstractMatcher.start());
 				body = fullText.substring(abstractMatcher.start());
 			}
 			// Apparently abstract wasn't divided by points
 			splitByHeading();
 		}
 			
 	}
 	
 	private void splitTail(String extro)  {
 		//first try to find references
 		
		Pattern pattern = Pattern.compile("^(References|Bibliography).{0,5}$", Pattern.MULTILINE);
 		Matcher matcher = pattern.matcher(fullText);		
 		if(matcher.find())  {
 			matcher.reset();
 			while(matcher.find()){
 				tail = fullText.substring(matcher.end());
 				body = fullText.substring(0, matcher.start());
 			}
 			//limit 
 			int limitOffSet = -1;
 			// for each possible heading of the limit
 			//create patter, get first occurrence in tail
 			Pattern limitPattern = Pattern.compile("^(Note|Notes|Appendix ).{0,5}$", Pattern.MULTILINE);
 			Matcher limitMatcher = limitPattern.matcher(tail);
 			while(limitMatcher.find()) {
 				if(limitOffSet == -1)
 					limitOffSet = limitMatcher.start();
 				if(limitOffSet > limitMatcher.start())
 					limitOffSet = limitMatcher.start();
 			}
 			if(limitOffSet > -1) {
 				if(debug)
 					logger.info("Limiting Reference part until "+limitOffSet+" that is "+tail.substring(limitOffSet, limitOffSet+12));
 				tail = tail.substring(0, limitOffSet);
 			}
 		}else {
 			if(debug)
 				logger.info("Wasn't able to find '"+extro+"' to split tail and body. So we set reference to null.");
 			tail=null;
 		/*	
 			Pattern headingPattern = Pattern.compile("^[0-9]*[A-Z][a-zA-Z].{0,5}$", Pattern.MULTILINE);
 			Matcher headingMatcher = headingPattern.matcher(fullText);
 			while(headingMatcher.find()) {
 				if(debug)
 					logger.info("Heading found to split tail: "+headingMatcher.group());
 				tail = fullText.substring(headingMatcher.end()+1);
 				body = fullText.substring(0, headingMatcher.start());
 			}
 		*/	
 		}
 	}
 
 	/**
 	 * Method to split a text by headings.
 	 * As of now we assume a Heading has a leading number followed by a whitespace character 
 	 * and some text beginning with upper case letters, such as "3 Related Work"
 	 */
 	private void splitByHeading() {
 		Pattern pattern = Pattern.compile("^[0-9]+\\s[A-Z].*", Pattern.MULTILINE);
 		Matcher matcher;
 		int add = 0;
 		// try to find headings after abstract
 		Pattern abstractPattern = Pattern.compile("^(Abstract).{0,5}$", Pattern.MULTILINE);
 		Matcher abstractMatcher = abstractPattern.matcher(body);
 		if(abstractMatcher.find()) {
 			add = abstractMatcher.end();
 			matcher = pattern.matcher(body.substring(abstractMatcher.end()));
 		} else {
 			matcher = pattern.matcher(body);
 		}		
 		
 		if(matcher.find()) {
 			// found at least once
 			if(debug)
 				logger.info("Splitting by heading at "+add+matcher.start()+ " which is the heading: "+matcher.group());
 			head = body.substring(0, add+matcher.start());
 			body = body.substring(add+matcher.start());
 		}
 	}
 	
 	private int countOccurrenceOfHeading(String text, String heading) {
 		int count = 0;
 		Pattern pattern = Pattern.compile("\\s[0-9]*"+heading+"\\s");
 		Matcher matcher = pattern.matcher(text);
 		while(matcher.find())
 			count++;
 		return count;
 	}
 }
