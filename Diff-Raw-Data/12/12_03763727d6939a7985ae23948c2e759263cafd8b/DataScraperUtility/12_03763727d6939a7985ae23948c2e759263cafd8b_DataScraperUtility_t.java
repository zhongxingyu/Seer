 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package no.hials.muldvarpweb.utility;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import no.hials.muldvarpweb.domain.Programme;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 /**
  * This class scrapes the HIALS Nexus-provided site for Course/Programme data. (XML)
  * @author johan
  */
 public class DataScraperUtility {
     private static String URL = "http://www.hials.no/var/storage/cdmu_eksport.xml";
     private static String PROGRAMMETAG = "program";
     private static String PROGRAMMENAMETAG = "programName";
     private static String PROGRAMMEDESCRIPTIONTAG = "programDescription";
     private static String PROGRAMMESTRUCTURETAG = "programStructure";
     
     /**
      * Constructor
      */
     public DataScraperUtility(){
         
     }
     
     public List<Programme> getDataAndSetupProgrammes() throws IOException {
         ArrayList<Programme> programmeList = new ArrayList<Programme>();
         Document document = Jsoup.connect(URL).get();
         if (document == null) {
             System.out.println("Fikk ikke lest fra nettsiden.");
         } else {
             Elements programmeElements = document.select(PROGRAMMETAG);
             System.out.println("size of programme elements: " + programmeElements.size());
             for (Element thisElement : programmeElements) {
                 String programmeName = thisElement.getElementsByTag(PROGRAMMENAMETAG).first().text();
                 String programmeDescription = thisElement.getElementsByTag(PROGRAMMEDESCRIPTIONTAG).first().text();
                 String programmeStructure= thisElement.getElementsByTag(PROGRAMMESTRUCTURETAG).first().text();
                 //Trim away some unwanted data
                 programmeDescription = trimHTML(programmeDescription);
                 programmeStructure = trimHTML(programmeStructure);
                 Programme thisProgramme = new Programme(programmeName, null);
                 thisProgramme.setDescription(programmeDescription);
                 thisProgramme.setPstructure(programmeStructure);
                 programmeList.add(thisProgramme);
             }
         }
         return programmeList;
     }
     
     public String trimHTML(String html){
         return Jsoup.parse(html).text();
     }
     
 }
