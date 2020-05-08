 package uk.ac.ebi.ae15.utils.search;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import uk.ac.ebi.ae15.utils.RegExpHelper;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
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
 
     public boolean matchAccession( String textIdx, String accession )
     {
         int idx = Integer.parseInt(textIdx);
         return expText.get(idx).accessions.contains(" ".concat(accession).concat(" ").toLowerCase());
     }
 
     public boolean matchSpecies( String textIdx, String species )
     {
         int idx = Integer.parseInt(textIdx);
         return expText.get(idx).species.contains(species.trim().toLowerCase());
     }
 
     public boolean matchArray( String textIdx, String array )
     {
         int idx = Integer.parseInt(textIdx);
         return expText.get(idx).array.contains(array.trim().toLowerCase());
     }
 
     public boolean matchExperimentType( String textIdx, String experimentType )
     {
         int idx = Integer.parseInt(textIdx);
         return expText.get(idx).experimentType.contains(experimentType.trim().toLowerCase());
     }
 
     public boolean matchUser( String textIdx, String user )
     {
         int idx = Integer.parseInt(textIdx);
         return expText.get(idx).users.contains(" ".concat(user).concat(" "));
     }
 
     public boolean isAccessible( String accession, String user )
     {
         if (user.equals("0")) {
             return true;
         }
 
         Integer idx = accessionIdx.get(accession.toLowerCase());
         return (null != idx) && matchUser(String.valueOf(idx), user);
     }
 
     public boolean doesPresent( String accession )
     {
         return accessionIdx.containsKey(accession.toLowerCase());
     }
 
     private String keywordToPattern( String keyword, boolean wholeWord )
     {
         return (wholeWord ? "\\b\\Q" + keyword + "\\E\\b" : "\\Q" + keyword + "\\E");
     }
 
     private boolean matchString( String input, String keywords, boolean wholeWords )
     {
         // trim spaces on both sides
         keywords = keywords.trim();
 
         if (0 < keywords.length()) {
             // by default (i.e. no keywords) it'll always match
 
             if (2 < keywords.length() && '"' == keywords.charAt(0) && '"' == keywords.charAt(keywords.length() - 1)) {
                 // if keywords are adorned with double-quotes we do phrase matching
                 return new RegExpHelper(keywordToPattern(keywords.substring(1, keywords.length() - 1), true), "i").test(input);
             } else {
                 // any keyword fails -> no match :)
                String[] kwdArray = keywords.split("\\s");
                 for ( String keyword : kwdArray ) {
                     if (!new RegExpHelper(keywordToPattern(keyword, wholeWords), "i").test(input))
                         return false;
                 }
             }
         }
         return true;
     }
 }
