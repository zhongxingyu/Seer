 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package xmlparser;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 /**
  *
  * @author Jenhan Tao <jenhantao@gmail.com>
  */
 public class XMLParser {
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         //retrieve a document using the name
         Document BBa_J23119Doc = getPartDocument("BBa_J23119");
         //use the document to parse out the name
         System.out.println("Name: " + getName(BBa_J23119Doc));
         //use the document to parse out the sequence
         System.out.println("Sequence: " + getSequence(BBa_J23119Doc));
        //print out the entire document
        System.out.println(BBa_J23119Doc.toString());
         ArrayList<Integer> indexMatches = poojaMethod("", "");
         System.out.println(indexMatches);
     }
 
     //given a part name, create a document object corresponding to the DOM
     public static Document getPartDocument(String partName) {
         Document partDoc;
         try {
             //all part urls kind of look like this
             partDoc = Jsoup.connect("http://parts.igem.org/partsdb/part_info.cgi?part_name=" + partName).timeout(10000000).get();
             return partDoc;
         } catch (Exception e) {
             e.printStackTrace();
             return null;
         } finally {
         }
     }
 
     //given a document, find the part name associated with that document
     public static String getName(Document partDoc) {
         String partName;
         try {// parse the text of the title element
             //grab the title element
             Elements elements = partDoc.getElementsByTag("title");
 
             String titleString = elements.get(0).text();
             partName = titleString.split(":")[1];
             return partName;
         } catch (Exception e) {
             e.printStackTrace();
             return null;
         }
     }
 
     //get the sequence of a part given a document
     public static String getSequence(Document partDoc) {
         String sequence;
         try {// parse the text of the title element
             //grab all script elements
             Elements scriptElements = partDoc.getElementsByTag("script");
             Iterator<Element> iterator = scriptElements.iterator();
 
             while (iterator.hasNext()) {
                 Element current = iterator.next();
                 String scriptText = current.html();
                 if (scriptText.contains("var sequence")) {
                     //extract sequence using regex
                     Pattern p = Pattern.compile("'([ACTGRYNactgryn]+)'");
                     Matcher m = p.matcher("var sequence = new String('ttgacagctagctcagtcctaggtataatgctagc');        var seqFeatures = new Array( ['new_feature',1,0,'', 0]); var subParts = null; var Format = '_ruler_'; var PrimaryPartName = 'BBa_J23119'; var PrimaryPartID = '7309'; var Selection_Start = 0; var Selection_End = 0; showSeqFeatures(false);");
                     if (m.find()) {
                         //if there is a match return it
                         sequence = m.group(1);
                         return sequence;
                     }
                 }
             }
             return null;
         } catch (Exception e) {
             e.printStackTrace();
             return null;
         }
     }
     
     /** This is a practice regex exercise **/
     public static ArrayList<Integer> poojaMethod(String sequence, String regexStatement) {
         
         //Initiate all variables needed for this method
         ArrayList<Integer> matchLocations = new ArrayList<Integer>();
         
         
         return matchLocations;
     }
 }
