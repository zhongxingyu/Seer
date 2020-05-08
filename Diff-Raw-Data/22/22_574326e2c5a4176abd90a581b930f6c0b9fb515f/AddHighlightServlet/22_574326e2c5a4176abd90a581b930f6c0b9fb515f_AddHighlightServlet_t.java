 package sara;
 
 import com.google.gson.Gson;
 import java.io.IOException;
 import javax.servlet.http.*;
 import sara.SARADocument;
 import sara.Highlight;
 import sara.Selection;
 import java.util.Arrays;
 import sara.HighlightService;
 import com.googlecode.objectify.*;
 import com.googlecode.objectify.annotation.*;
 
 
 public class AddHighlightServlet extends HttpServlet {
   public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
     System.out.println(req.getParameter("selections_startoffsets"));
 
     if(req.getParameter("selections_startoffsets") != null) { //all we actually need is a selection to create a highlight.
       String document = req.getParameter("document");
       String comment = "" + req.getParameter("comment");
       String difficulty = "" + req.getParameter("difficulty");
       String usefulness = "" + req.getParameter("usefulness");
      
       Gson gson = new Gson();
      String[] nodeIDs = gson.fromJson(req.getParameter("nodeIDs"), String[].class);
       int[] startOffsets = gson.fromJson(req.getParameter("selections_startoffsets"), int[].class);
       int[] endOffsets = gson.fromJson(req.getParameter("selections_endoffsets"), int[].class);
 
       Key<SARADocument> dockey = new Key<SARADocument>(SARADocument.class, new Long(document));
      Highlight highlight = new Highlight(dockey, comment, difficulty, usefulness, startOffsets, endOffsets, nodeIDs);
 
       HighlightService hs = new HighlightService();
       hs.addHighlight(highlight);
 
     }
 
   }
 }
