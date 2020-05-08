 package uk.ac.ebi.ae15.utils.search;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Search experiments based on full-text information stored in ExperimentText class
  */
 public class ExperimentSearch
 {
     // logging machinery
     private final Log log = LogFactory.getLog(getClass());
 
     // array of ExperimentText objects in the same order they exist in the document
     private List<ExperimentText> expText = new ArrayList<ExperimentText>();
 
     // mapping of experiment accession numbers to experiment text indices
     private Map<String, Integer> accessionIdx = new HashMap<String, Integer>();
 
     public boolean isEmpty()
     {
         return expText.isEmpty();
     }
 
     public void buildText( Document experiments )
     {
         if (null != experiments) {
             try {
                 if (experiments.hasChildNodes() && experiments.getDocumentElement().hasChildNodes()) {
                     NodeList expList = experiments.getDocumentElement().getChildNodes();
 
                     expText.clear();
                     accessionIdx.clear();
 
                     for ( int i = 0; i < expList.getLength(); ++i ) {
                         Element expElt = (Element) expList.item(i);
                         ExperimentText text = new ExperimentText().populateFromElement(expElt);
                         expText.add(text);
                         accessionIdx.put(text.accession, i);
                         expElt.setAttribute("textIdx", Integer.toString(i));
                     }
 
                 }
             } catch ( Throwable x ) {
                 log.error("Caught an exception:", x);
             }
         }
     }
 
     public boolean matchText( String textIdx, String keywords, boolean wholeWords )
     {
         int idx = Integer.parseInt(textIdx);
         return matchString(expText.get(idx).text, keywords, wholeWords);
     }
 
     public boolean matchSpecies( String textIdx, String species )
     {
         int idx = Integer.parseInt(textIdx);
         return (-1 != expText.get(idx).species.indexOf(species.trim().toLowerCase()));
     }
 
     public boolean matchArray( String textIdx, String array )
     {
         int idx = Integer.parseInt(textIdx);
         return (-1 != expText.get(idx).array.indexOf(array.trim().toLowerCase()));
     }
 
     public boolean matchExperimentType( String textIdx, String experimentType )
     {
         int idx = Integer.parseInt(textIdx);
         return (-1 != expText.get(idx).experimentType.indexOf(experimentType.trim().toLowerCase()));
     }
 
     public boolean isAccessible( String accession, String user )
     {
         Integer idx = accessionIdx.get(accession.toLowerCase());
        return (user.equals("0") || (null != idx && -1 != expText.get(idx).users.indexOf(" ".concat(user).concat(" "))));
     }
 
     public boolean doesPresent( String accession )
     {
         return accessionIdx.containsKey(accession.toLowerCase());
     }
 
     private boolean matchRegexp( String input, String pattern, String flags )
     {
         boolean result = false;
         try {
             int patternFlags = (flags.indexOf("i") >= 0 ? Pattern.CASE_INSENSITIVE : 0);
 
             String inputStr = (input == null ? "" : input);
             String patternStr = (pattern == null ? "" : pattern);
 
             Pattern p = Pattern.compile(patternStr, patternFlags);
             Matcher matcher = p.matcher(inputStr);
             result = matcher.find();
         } catch ( Throwable x ) {
             log.error("Caught an exception:", x);
         }
 
         return result;
     }
 
     private String keywordToPattern( String keyword, boolean wholeWord )
     {
         return (wholeWord ? "\\b\\Q" + keyword + "\\E\\b" : "\\Q" + keyword + "\\E");
     }
 
     private boolean matchString( String input, String keywords, boolean wholeWords )
     {
         // trim spaces on both sides
         keywords = keywords.trim();
 
         // by default (i.e. no keywords) it'll always match
         // otherwise any keyword fails -> no match :)
         if (0 < keywords.length()) {
             String[] kwdArray = keywords.split("\\s");
             for ( String keyword : kwdArray ) {
                 if (!matchRegexp(input, keywordToPattern(keyword, wholeWords), "i"))
                     return false;
             }
         }
         return true;
     }
 }
