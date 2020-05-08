 package angular.callbacks;
 
 import java.io.IOException;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import appengine.daos.Recipe;
 import appengine.servlets.BaseServlet;
 import appengine.utils.Util;
 
 import com.google.appengine.api.datastore.Entity;
 
 @SuppressWarnings("serial")
 public class SlideServlet extends BaseServlet {
 	
     @Override
     protected void doGet(HttpServletRequest req, HttpServletResponse resp)
             throws ServletException, IOException {
         super.doGet(req, resp);
         Entity type;
         Integer sx = null, dx = null, pos = Integer.valueOf(req.getParameter("id"));
         String blob = new String(""), description = new String("");
         final List<Entity> items =  Util.listSlideItems("Item", req.getParameter("id_type"));
         if(0 == pos){
         	Entity recipe = Recipe.getRecipe(req.getParameter("id_type"));
         	if(recipe != null){
         		blob = String.valueOf(recipe.getProperty("blobKey"));
         	}
         	if(!items.isEmpty())
         		dx = 1;
         } else {
         	type = (Entity) items.get(pos-1); 
         	blob = String.valueOf(type.getProperty("blobKey"));
         	description = String.valueOf(type.getProperty("item_description"));
         	sx = pos-1;
         	if(pos < items.size())
         		dx = pos+1;
         }
         
         resp.getWriter().println(req.getParameter("callback")+"(" +
         		"{\"sx\": "+sx+", " +
        		"\"description\": \""+description+"\", " +
         		"\"success\": true, " +
        		"\"recipe_id\": \""+req.getParameter("id_type")+"\", " +
        		"\"image\": \""+blob+"\", " +
         		"\"dx\": "+dx+"})");
         
         //'http://localhost:8888/images/view.json?callback=angular.callbacks._1&id=0&id_type=bOhxUNQl8rcD5E13rw-evQ&lan=en&type=recipe'
     }
 }
