 package es.proof.restlet.standalone.application;
 
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 
 import org.restlet.Application;
 import org.restlet.Request;
 import org.restlet.Response;
 import org.restlet.Restlet;
 import org.restlet.data.MediaType;
 import org.restlet.representation.StringRepresentation;
 import org.restlet.routing.Router;
 
 import es.proof.restlet.standalone.base.resources.Item;
 import es.proof.restlet.standalone.base.resources.ItemResource;
 import es.proof.restlet.standalone.base.resources.ItemsResource;
 
 public class FirstResourceApplication extends Application {
 
     /** The list of items is persisted in memory. */
     private final ConcurrentMap<String, Item> items = new ConcurrentHashMap<String, Item>();
 
     /**
      * Creates a root Restlet that will receive all incoming calls.
      */
     @Override
     public Restlet createInboundRoot() {
         // Create a router Restlet that defines routes.
         Router router = new Router(getContext());
 
         // Defines a route for the resource "list of items"
         router.attach("/items", ItemsResource.class);
         // Defines a route for the resource "item"
         router.attach("/items/{itemName}", ItemResource.class);
 
         Restlet mainpage = new Restlet() {
             @Override
             public void handle(Request request, Response response) {
                 StringBuilder stringBuilder = new StringBuilder();
 
                 stringBuilder.append("<html>");
                 stringBuilder.append("<head><title>First Steps Application "
                         + "Servlet Page</title></head>");
                 stringBuilder.append("<body bgcolor=white>");
                 stringBuilder.append("<table border=\"0\">");
                 stringBuilder.append("<tr>");
                 stringBuilder.append("<td>");
                 stringBuilder.append("<h3>available REST calls</h3>");
                 stringBuilder
                        .append("<ol><li><a href=\"app/items\">show items</a> --> return items"
                                 + "and date string</li>");
 
                 stringBuilder.append("</ol>");
                 stringBuilder.append("</td>");
                 stringBuilder.append("</tr>");
                 stringBuilder.append("</table>");
                 stringBuilder.append("</body>");
                 stringBuilder.append("</html>");
 
                 response.setEntity(new StringRepresentation(stringBuilder
                         .toString(), MediaType.TEXT_HTML));
             }
         };
         router.attach("", mainpage);
 
         return router;
     }
 
     /**
      * Returns the list of registered items.
      * 
      * @return the list of registered items.
      */
     public ConcurrentMap<String, Item> getItems() {
         return items;
     }
 
 }
