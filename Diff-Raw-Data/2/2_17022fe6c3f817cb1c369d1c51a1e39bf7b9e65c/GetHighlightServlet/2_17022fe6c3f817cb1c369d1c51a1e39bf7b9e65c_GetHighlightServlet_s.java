 package sara;
 import sara.SARADocument;
 import sara.Highlight;
 import sara.Selection;
 import sara.HighlightService;
 
 import java.io.IOException;
 import java.servlet.http.*;
 
 public class GetHighlightServlet extends HttpServlet {
   public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
     if(req.getParameter("document") != null) {
       HighlightService hs = new HighlightService();
       Iterator<Highlight> highlights = hs.listHighlights();
       
       while(highlights.hasNext()) {
        out.print(highlight.next().toJson());
       }
     }
 
   }
 }
