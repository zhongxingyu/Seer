 package org.xbrlapi.data.bdbxml.utilities.tests;
 
 import java.net.URI;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.Vector;
 
import org.xbrlapi.XML;
 import org.xbrlapi.data.bdbxml.tests.BaseTestCase;
 import org.xbrlapi.loader.Loader;
 
 /**
  * Use this unit test to find XLink locators with multiple targets.
  * @author Geoff Shuetrim (geoff@galexy.net)
  */
 public abstract class AnalyseDuplicateLocatorTargets extends BaseTestCase {
     
     public AnalyseDuplicateLocatorTargets(String arg0) {
         super(arg0);
     }
 
     private List<URI> resources = new Vector<URI>();
     
     Loader secondLoader = null;
     
     protected void setUp() throws Exception {
         super.setUp();
 
     }
 
     protected void tearDown() throws Exception {
         // Close but DO NOT delete the data stores.
         for (int i=0; i<stores.size(); i++) {
             stores.get(i).close();
         }
     }   
     
     public void testAnalyseDuplicateLocatorTargets() {
         try {
 
             String query = "for $locator in #roots#,$target in #roots#/xbrlapi:xptr where ($locator/@uri='http://www.sec.gov/Archives/edgar/data/1160330/000129281407002220/bbd-20070618_pre.xml' and $locator/*/*/@xlink:type='locator' and $target/../@uri=$locator/@targetDocumentURI and $locator/@targetPointerValue=$target/@value) return concat($target/../@uri,' ',$target/@value,' ',$target/../@index)";
             Set<String> results = store.queryForStrings(query);
             logger.info(results.size());
             Set<String> sortedResults = new TreeSet<String>();
             sortedResults.addAll(results);
             for (String result: sortedResults) {
                 logger.info(result);
             }
             
            store.<XML>getXMLResource("uqMF4V_1511").serialize();
             
 /*            Map<String,String> map = new HashMap<String,String>();
             Set<String> pairs = store.queryForStrings("for $root in #roots#[@parentIndex=''] return concat($root/@uri,' ',$root/@index)");
             int count = 0;
             sortedResults = new TreeSet<String>();
             for (String pair: pairs) {
                 int split = pair.indexOf(" ");
                 String uri = pair.substring(0,split);
                 String index = pair.substring(split+1);
                 String targetURI = "http://www.xbrl.org/us/fr/common/pte/2005-02-28/usfr-pte-2005-02-28.xsd";
                 String targetIndex = "uqMF4V_1";
                 if (uri.equals(targetURI)) logger.info("Matched URI " + pair);
                 if (index.equals(targetIndex)) logger.info("Matched index " + pair);
                 if (map.containsKey(uri)) {
                     logger.info(uri + " stored under index " + index + " and " + map.get(uri));
                     count++;
                 } else {
                     map.put(uri,index);
                 }
             }
             logger.info("# of duplicated documents = " + count);
             for (String result: sortedResults) {
                 logger.info(result);
             }
 */            
         } catch (Exception e) {
             e.printStackTrace();
             fail("An unexpected exception was thrown.");
         }
     }
     
 }
