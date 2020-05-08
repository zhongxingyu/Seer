 package uk.ac.ebi.ae15.utils.search;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 /**
  * A transitional class holding text that will be matched when searching for experiments.
  * To be replaced by Lucene engine in 1.2.
  */
 
 public class ExperimentText
 {
     // logging machinery
     private final Log log = LogFactory.getLog(getClass());
 
     // all text, concatenated
     public String text;
 
     public String accession;
 
     // all species, concatenated
     public String species;
 
     // all array info, concatenated
     public String array;
 
     // experiment type info, concatenated
     public String experimentType;
 
     // user ids, concatenated
     public String users;
 
     public ExperimentText populateFromElement( Element elt )
     {
 
         text = concatAll(elt);
         try {
             accession = elt.getElementsByTagName("accession").item(0).getFirstChild().getNodeValue().toLowerCase();
         } catch ( Throwable x ) {
             log.debug("Caught an exception:", x);
         }
         species = concatAll(elt.getElementsByTagName("species")).toLowerCase();
         array = concatAll(elt.getElementsByTagName("arraydesign")).toLowerCase();
         experimentType = concatAll(elt.getElementsByTagName("experimenttype")).toLowerCase();
         users = " ".concat(concatAll(elt.getElementsByTagName("user")));
 
         return this;
     }
 
     private String concatAll( Element elt )
     {
         if (elt.hasChildNodes()) {
             return concatAll(elt.getChildNodes());
         } else {
             return "";
         }
 
     }
 
     private String concatAll( NodeList nl )
     {
         StringBuilder buf = new StringBuilder();
 
         try {
             for ( int i = 0; i < nl.getLength(); i++ ) {
                 Node elt = nl.item(i);
 
                 if (null != elt.getNodeValue())
                     buf.append(elt.getNodeValue()).append(' ');
 
                 if (elt.hasAttributes()) {
                     NamedNodeMap attrs = elt.getAttributes();
                     for ( int j = 0; j < attrs.getLength(); j++ ) {
                         buf.append(attrs.item(j).getNodeValue()).append(' ');
                     }
                 }
 
                 if (elt.hasChildNodes())
                     buf.append(concatAll(elt.getChildNodes())).append(' ');
             }
         } catch ( Throwable x ) {
             log.error("Caught an exception:", x);
         }
 
        return buf.toString().trim();
     }
 }
