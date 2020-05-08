 package sara;
 import sara.SARADocument;
 import sara.Highlight;
 import sara.Selection;
 import sara.HighlightService;
 import com.google.gson.Gson;
 import java.util.Iterator;
 import java.util.Collection;
 import java.util.ArrayList;
 import java.io.IOException;
 import javax.servlet.http.*;
 import com.googlecode.objectify.*;
 import com.googlecode.objectify.annotation.*;
 import com.google.appengine.api.users.UserService;
 import com.google.appengine.api.users.UserServiceFactory;
 
 public class GetHighlightServlet extends HttpServlet {
   public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
     HighlightService hs = new HighlightService();
 
     if(req.getParameter("document") != null) {
       Long id = new Long(req.getParameter("document"));
       SARADocument doc = hs.getDocumentById(id);
      /*Iterator<Highlight> highlights;
      if(req.getParameter("timestamp") != null) {
        String timestamp = req.getParameter("timestamp");
        highlights = hs.getNewHighlightsOfDocument(doc, timestamp).iterator();
      } else {
        highlights = hs.getHighlightsOfDocument(doc).iterator();
      }*/


 
       Key<SARADocument> dockey = new Key<SARADocument>(SARADocument.class, doc.getId());
       Objectify objectify = ObjectifyService.begin();
       //Get all public highlights related to document
      Iterator<Highlight> highlights =  objectify.query(Highlight.class).ancestor(dockey).filter("privacy !=", 1).iterator();
 
       Collection<Highlight> highlightar = new ArrayList();
 
       while(highlights.hasNext()) {
         Highlight h = highlights.next();
         highlightar.add(h);
       }
 
       UserService userService = UserServiceFactory.getUserService();
       if(req.getUserPrincipal() != null) {
         String userid = req.getUserPrincipal().getName();
         highlights = objectify.query(Highlight.class).ancestor(dockey).filter("privacy ==", 1).filter("user ==",userid).iterator();
 
         while(highlights.hasNext()) {
           Highlight h = highlights.next();
           highlightar.add(h);
         }
       }
 
       
 
 
       Gson gson = new Gson();
       resp.getWriter().print(gson.toJson(highlightar));
 
       //while(highlights.hasNext()) {
         //Highlight h = highlights.next();
         //resp.getWriter().print(h.toJson());
         //Iterator<Selection> selections = hs.getSelectionsOfHighlight(h).iterator();
           //while(selections.hasNext()) {
             //resp.getWriter().print(selections.next().toJson());
           //}
       //}
     }
   }
 }
