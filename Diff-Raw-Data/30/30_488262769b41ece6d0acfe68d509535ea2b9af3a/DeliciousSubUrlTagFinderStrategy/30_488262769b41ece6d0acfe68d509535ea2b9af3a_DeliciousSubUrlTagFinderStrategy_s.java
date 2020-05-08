 package model.usermodel.tags;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import util.LogHandler;
 
 
 public class DeliciousSubUrlTagFinderStrategy extends
 		MD5SubUrlTagFinderStrategy {
 	
 	private static final String DELICIOUS_URL_STARTSWITH = "http://del.icio.us/url/";
 	private static final String DELICIOUS_URL_ENDSWITH = "?settagview=list";
 	
 	public DeliciousSubUrlTagFinderStrategy() {
 		super(DELICIOUS_URL_STARTSWITH,DELICIOUS_URL_ENDSWITH);
 	}
 	
 	
 	//FIXME: BROKEN!!
 
 	protected Map<String, Integer> extractTags(String pageContent) {
 		
 		Logger logger = LogHandler.getLogger(this.getClass().getName());
 		
 		//result of the method
 		Map<String,Integer> extractedTags = new HashMap<String,Integer> ();
 		
 		//first relevant character
 		int startIndex = pageContent.indexOf("top-tags");
 		//logger.info("occorrenza di 'top-tags': " + startIndex);
 		if(startIndex>=0) {
 			startIndex = pageContent.indexOf("<li", startIndex);
 			//logger.info("occorrenza della lista di tags: " + startIndex);
 		}
 		//last relevant character
 		int endIndex = pageContent.indexOf("</ul>",startIndex);
 		
 		//if page is empty skip
 		if(startIndex==-1 || endIndex==-1)
 			return extractedTags;
 		
 		//page content relevant substring
 		pageContent = pageContent.substring(startIndex,endIndex);
 //		logger.info("contenuto rilevante della pagina di delicious: " + pageContent);
 		
 		while(pageContent.indexOf("</li>")!=-1) {
 			int tagValuesStart = pageContent.indexOf("<li") + ("<li").length();
 			int tagValuesEnd = pageContent.indexOf("</li>");
 			String tagValues = pageContent.substring(tagValuesStart,tagValuesEnd);
 			//logger.info("contenuto rilevante per il tag: " + tagValues);
 			
 			/*
 			int frequencyStart = tagValues.indexOf("<span>") + ("<span>").length();
 			int frequencyEnd = tagValues.indexOf("</span>");
 			String tagNameValues = tagValues.substring(frequencyEnd+("</span>").length());
 			//logger.info("contenuto rilevante per il nome del tag: " + tagNameValues);
 			int nameStart = tagNameValues.indexOf("\">") + ("\">").length();
 			int nameEnd = tagNameValues.indexOf("</a>");
 			*/
 			/* sono sbagliati: 6/4/2009 */
 			/* nello span class ora c': 
 			 * <span class="m" title="3 (2450)">
 			 * anche il posto in classifica e la frequenza */
 			
 			/* ancora sbagliati: 03/6/2009 */
 			
			String stringBeforeTag = "<span class=\"m\">";///!!!!!!!!!!!!!!!!!!
 			int nameStart = tagValues.indexOf(stringBeforeTag) + stringBeforeTag.length();
 			int nameEnd = tagValues.indexOf("<em>");
 			
 			String tagFrequencyValues = tagValues.substring(nameEnd);
 			
 			int frequencyStart = tagFrequencyValues.indexOf("<em>") + ("<em>").length();
 			int frequencyEnd = tagFrequencyValues.indexOf("</em>");
 			
 			String frequencyString = tagFrequencyValues.substring(frequencyStart,frequencyEnd);
 			int frequency = Integer.parseInt(frequencyString);
			String name = tagValues.substring(nameStart,nameEnd);
 			logger.info("tag: " + name + "(frequenza: " + frequencyString + ")");
 			extractedTags.put(name, frequency);
 			pageContent = pageContent.substring(tagValuesEnd + ("</li>").length());
 		}
 		return extractedTags;
 	}
 
 }
