 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package entitysuggester.client.servlets;
 
 import com.google.common.collect.Iterables;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.URISyntaxException;
 import java.util.List;
 import javax.naming.NamingException;
 import javax.servlet.ServletException;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import net.myrrix.client.translating.TranslatedRecommendedItem;
 import org.apache.mahout.cf.taste.common.TasteException;
 
 /**
  *
  * @author nilesh
  */
 public class EntitySuggesterServlet extends AbstractEntitySuggesterServlet {
 
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
         super.doGet(request, response);
 
         String pathInfo = request.getPathInfo();
         String[] pathComponents = Iterables.toArray(SLASH.split(pathInfo), String.class);
 
         if (pathComponents.length == 0) {
             response.sendError(HttpServletResponse.SC_BAD_REQUEST);
             return;
         }
 
         try {
             List<TranslatedRecommendedItem> recommended = getClientRecommender().recommendAnonymous(getPropFilePath("proplist").toURI(), request.getParameter("type"), getHowMany(request), pathComponents);
             output(request, response, recommended);
         } catch (URISyntaxException use) {
             response.sendError(HttpServletResponse.SC_BAD_REQUEST, use.toString());
         } catch (NamingException ne) {
             response.sendError(HttpServletResponse.SC_BAD_REQUEST, ne.toString());
         } catch (TasteException te) {
             response.sendError(HttpServletResponse.SC_BAD_REQUEST, te.toString());
         }
     }
 
     protected final void output(HttpServletRequest request,
             ServletResponse response,
             List<TranslatedRecommendedItem> items) throws IOException {
 
         PrintWriter writer = response.getWriter();
         // Always print JSON
         writer.write('[');
         boolean first = true;
         for (TranslatedRecommendedItem item : items) {
             if (first) {
                 first = false;
             } else {
                 writer.write(',');
             }
            writer.write("[\"");
             writer.write(item.getItemID());
            writer.write("\",");
             writer.write(Float.toString(item.getValue()));
             writer.write(']');
         }
         writer.write(']');
     }
 }
