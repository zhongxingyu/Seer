 package sara;
 import sara.SARADocument;
 import sara.Highlight;
 import sara.Selection;
 import sara.HighlightService;
 
import java.util.Iterator;

 import java.io.IOException;
import javax.servlet.http.*;
 
 public class GetHighlightServlet extends HttpServlet {
   public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
     if(req.getParameter("document") != null) {
       HighlightService hs = new HighlightService();
      Iterator<Highlight> highlights = hs.getHighlights().iterator();
       
       while(highlights.hasNext()) {
        resp.getWriter().print(highlights.next().toJson());
       }
     }
 
   }
 }
