 package html_output;
 
 import process.Event;
 import process.EventCalendar;
 
 import com.hp.gagawa.java.elements.Body;
import com.hp.gagawa.java.elements.Br;
 import com.hp.gagawa.java.elements.Html;
import com.hp.gagawa.java.elements.Text;
 
 /**
  *  @author Antares Yee
  */
 
 public class DetailPage extends HtmlPage {
     public static final String DETAIL_DIR_PATH = "DetailDir";
     
     public DetailPage(String path) {
         super(path);
         makeDirFromPath(DETAIL_DIR_PATH);
     }
     
     //Push this up, make override.
     /**
      * Creates a .html detail page for each event in myDetailDirPath.
      */
     @Override
     public String createHTMLpage(EventCalendar events) {
         for (Event e : events.getList()) {
             Html html = makeHtmlObject(e);
             makeFile(html, DETAIL_DIR_PATH + makeFileName(e));
         }
         return "Detail Pages Made Sucessfully";
     }
     
     /**
      * Returns Html object for detail page for one Event e
      */
     public Html makeHtmlObject(Event e) {
         Html html = new Html();
         Body body = new Body();
         
         addEventInfo(e, body);
         html.appendChild(body);
         return html;
     }
 
     @Override
     public String getMyFileName() {
         System.out.println("getMyFileName() shouldn't be called in DetailPage.");
         return null;
     }
 
     @Override
     public Html makeHtmlObject(EventCalendar events) {
         // TODO Auto-generated method stub
         return null;
     }
    
    @Override
    public boolean addEventDescription(Event e, Body body) {
        for (String key: e.getKeys()) {
        	if (key.equalsIgnoreCase("title")) continue;
        	body.appendChild(new Text(key+": " + e.getFeature(key)));
            body.appendChild(new Br());
        }
        return true;
    }
 }
