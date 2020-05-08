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
 import com.google.appengine.api.users.UserService;
 import com.google.appengine.api.users.UserServiceFactory;
 
 
 public class AddHighlightServlet extends HttpServlet {
   public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
     System.out.println(req.getParameter("nodeID"));
 
     if(req.getParameter("nodeID") != null) { //all we actually need is a selection to create a highlight.
       String document = req.getParameter("document");
       System.out.println(document);
       String comment = "" + req.getParameter("comment");
       System.out.println(comment);
       //String difficulty = "" + req.getParameter("difficulty");
       int difficulty = 0;
       //String usefulness = "" + req.getParameter("usefulness");
 
       String userid = "";
 
       UserService userService = UserServiceFactory.getUserService();
       if(req.getUserPrincipal() != null) {
         System.out.println(req.getUserPrincipal().getName());
         userid = req.getUserPrincipal().getName();
       }
 
 
 
       String nodeID = "" + req.getParameter("nodeID");
       System.out.println(nodeID);
       int startOffset = (new Integer(req.getParameter("startOffset"))).intValue();
       System.out.println(startOffset);
       int offsetDelta = (new Integer(req.getParameter("offsetDelta"))).intValue();
       int privacy = (new Integer(req.getParameter("privacy"))).intValue();
       System.out.println(offsetDelta);
       
       //chang
 
       Key<SARADocument> dockey = new Key<SARADocument>(SARADocument.class, new Long(document));
 
      Highlight highlight = new Highlight(dockey, comment, difficulty, 50, userid, nodeID, startOffset, offsetDelta, privacy);
 
       HighlightService hs = new HighlightService();      
       //chang
         hs.addHighlight(highlight);
 
       System.out.println(highlight.toJson());
 
 
     }
 
   }
 }
