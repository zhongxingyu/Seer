 package com.googlecode.mad_schuelerturnier.business.print;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import com.googlecode.mad_schuelerturnier.business.controller.leiter.converter.XHTMLOutputUtil;
 import com.googlecode.mad_schuelerturnier.model.spiel.Spiel;
 
 /**
  * Created with IntelliJ IDEA.
  * User: dama
  * Date: 04.01.13
  * Time: 23:57
  * To change this template use File | Settings | File Templates.
  */
 @Component
 public class SpielPrintManager {
 
 	private static Integer SPIELE_PER_PAGE = 15;
 
     private static final Logger LOG = Logger.getLogger(SpielPrintManager.class);
 
     private List<Spiel> aktuelleSpiele = new ArrayList<Spiel>();
     
     private Set<Spiel> spielSet = new HashSet<Spiel>();
     
     private Map<Integer, String> savedPages = new HashMap<Integer, String>();
 
     // idee war, dass beim admin gui ausgeschlatet wewrden kann
     private  boolean print = true;
 
     @Autowired
     private XHTMLOutputUtil cleaner;
     
     @Autowired
     private PrintAgent printAgent;
     
     public void saveSpiel(Spiel spiel){
     	
     	if(!spielSet.contains(spiel)){
     		aktuelleSpiele.add(spiel);
     	}
     	
     	if(aktuelleSpiele.size() >= SPIELE_PER_PAGE && print){
     		printPage();
 
     	}
     	spielSet.add(spiel);
     }
 
     public void printPage(){
     	StringBuffer buff = new StringBuffer();
     	buff.append("<table padding=\"4\" style=\"font-size: 16px\" border=\"1\">");
     	
 		buff.append("<tr>");
         buff.append("<td>");
 		buff.append("<b>Start</b>");
 		buff.append("</td><td>");
 		buff.append("<b>Platz</b>");
 		buff.append("</td><td colspan=\"2\">");
 		buff.append("<b>Mannschaften</b>");
 		buff.append("</td><td>");
 		buff.append("<b>Tore</b>");
 		buff.append("</td></tr>");
     	
     	for (Spiel spiel : aktuelleSpiele) {
     		buff.append("<tr><td>");
             SimpleDateFormat form = new SimpleDateFormat("E HH:mm");
     		buff.append(form.format(spiel.getStart()));
     		buff.append("</td><td>");
     		buff.append(""+ spiel.getPlatz());
     		buff.append("</td><td>");
     		buff.append(""+spiel.getMannschaftAName());
     		buff.append("</td><td>");
     		buff.append(""+spiel.getMannschaftBName());
     		buff.append("</td><td>");
     		buff.append(""+spiel.getToreABestaetigt()+" : "+spiel.getToreBBestaetigt());
     		buff.append("</td></tr>");
 		}
 
         buff.append("<tr><td align=\"right\" colspan=\"5\">");
 
        int pageN = savedPages.size();
        pageN = pageN ++;
 
        buff.append("Seite: " +pageN);
 
         buff.append("</td></tr>");
 
         buff.append("<table>");
 
     	String page = this.cleaner.cleanup(buff.toString(), false);
     	

     	this.savedPages.put(pageN, page);
 
         this.aktuelleSpiele.clear();
     	
     	printAgent.saveFileToPrint(""+pageN, page);
     	
     }
 
     public boolean isPrint() {
         return print;
     }
 
     public void setPrint(boolean print) {
         this.print = print;
     }
 }
