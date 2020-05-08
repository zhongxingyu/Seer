 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package no.ntnu.webintelligence.parsers;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import no.ntnu.webintelligence.models.NLHChapter;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 /**
  *
  * @author Kristin
  */
 public class NLHChapterParser {
 
     private ArrayList<NLHChapter> chapters;
 
     public NLHChapterParser() throws IOException {
         chapters = new ArrayList<NLHChapter>();
         addChapters();
     }
 
     public void addChapters() throws IOException {
         File dir = new File("src/main/resources/nlh");
         File[] files = dir.listFiles();
         for (int i = 0; i < files.length; i++) {
             //System.out.println(files[i].getName().replaceAll(".htm", ""));
             NLHChapter chapter = readHTMFile(files[i]);
             chapters.add(chapter);
         }
     }
 
     public NLHChapter readHTMFile(File file) throws IOException {
        Document doc = Jsoup.parse(file, "iso-8859-1");
         Elements body = doc.getElementsByClass("seksjon2");
         String bodyString = body.toString();
         String bodyResult = Jsoup.parse(bodyString).text();
         String id = file.getName().replaceAll(".htm", "");
         //System.out.println(bodyResult);
         for (Element e : body) {
             //System.out.println(e.getAllElements());
         }
         String title = "";
         Elements docs;
         if (doc.select("h2") != null) {
             docs = doc.select("h2");
         } else {
             docs = doc.select("h1");
         }
 
         for (Element e : docs) {
             int length = id.length();
             // int ind = e.text().indexOf("Publisert");
             //if (ind > 0) {
             //  title = e.text().substring(length + 1, ind);
           //  System.out.println(e.text());
             // }
 
         }
         return new NLHChapter(id, title, bodyResult);
 
     }
 
     public ArrayList<NLHChapter> getChapters() {
         return chapters;
     }
 
     public static void main(String[] args) throws IOException {
         NLHChapterParser parser = new NLHChapterParser();
     }
 }
