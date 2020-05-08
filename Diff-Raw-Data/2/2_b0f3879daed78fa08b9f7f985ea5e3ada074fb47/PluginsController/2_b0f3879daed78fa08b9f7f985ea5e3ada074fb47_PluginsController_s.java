 package devoxx.core.controllers;
 
 import devoxx.core.fwk.api.Controller;
 import devoxx.api.*;
 import devoxx.api.Lang.Language;
 import devoxx.core.util.F.Tuple;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import javax.activation.MimetypesFileTypeMap;
 import javax.enterprise.event.Observes;
 import javax.inject.Inject;
 import javax.ws.rs.*;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import org.jboss.weld.environment.osgi.api.Service;
 import org.jboss.weld.environment.osgi.api.annotation.OSGiService;
 import org.jboss.weld.environment.osgi.api.annotation.Required;
 import org.jboss.weld.environment.osgi.api.annotation.Specification;
 import org.jboss.weld.environment.osgi.api.events.ServiceEvents;
 
 @Path("plugins")
 public class PluginsController implements Controller {
     
     @Inject @Required Service<Plugin> plugins;
     
     @Inject @OSGiService @Lang(Language.EN) Plugin plugin;
     
     @GET
     @Produces(MediaType.APPLICATION_JSON)
     public List<String> getPluginIds() {
         List<String> ids = new ArrayList<String>();
         for (Plugin plugin : plugins) {
             ids.add(plugin.pluginId());
         }
         return ids;
     }
     
     @GET @Path("{pluginId}/apply")
    @Produces(MediaType.APPLICATION_JSON)
     public String apply(@PathParam("pluginId") String pluginid, @FormParam("content") String content) {
         for (Plugin plugin : plugins) {
             if (plugin.pluginId().equals(pluginid)) {
                 return plugin.apply(content);
             }
         }
         return "Error while processing content ...";
     }
     
     @GET @Path("messages")
     @Produces(MediaType.APPLICATION_JSON)
     public List<String> getPopupMessages(@QueryParam("since") Long since) {
         List<Tuple<Long, String>> messagesCopy = new ArrayList<Tuple<Long, String>>();
         messagesCopy.addAll(messages);
         List<String> values = new ArrayList<String>();
         for (Tuple<Long, String> t : messages) {
            if (since == null) {
                values.add("{\"last\":" + t._1 + ", \"message\":\""+ t._2 + "\"}");
            } else if (t._1 >= since) {
                values.add("{\"last\":" + t._1 + ", \"message\":\""+ t._2 + "\"}");
            }
         }
         return values;
     }
     
     @GET @Path("installed")
     @Produces(MediaType.APPLICATION_JSON)
     public List<String> getInstalledBundles() {
         List<String> names = new ArrayList<String>();
         for(Tuple<String, String> t : pluginNames.values()) {
             names.add(t._1);
         }
         return names;
     }
     
     @GET
     @Path("{pluginId}/{route}")
     public Response getRes(@PathParam("pluginId") String pluginid, @PathParam("route") String route) {
         for (Plugin plugin : plugins) {
             if (plugin.pluginId().equals(pluginid)) {
                 if (plugin.resources().containsKey(route)) {
                     String mt = new MimetypesFileTypeMap().getContentType(plugin.resources().get(route));
                     return Response.ok(plugin.resources().get(route), mt).build();
                 }
                 throw new WebApplicationException(404);
             }
         }
         throw new WebApplicationException(404);
     }
     
     private ConcurrentHashMap<String, Tuple<String, String>> pluginNames = 
             new ConcurrentHashMap<String, Tuple<String, String>>();
     
     private List<Tuple<Long, String>> messages = Collections.synchronizedList(new ArrayList<Tuple<Long, String>>());
     
     public void listenArrival(@Observes @Specification(Plugin.class) ServiceEvents.ServiceArrival evt) {
         Plugin p = evt.getService(Plugin.class);
         if (!pluginNames.containsKey(p.pluginId())) {
             pluginNames.putIfAbsent(p.pluginId(), new Tuple<String, String>(p.name(), p.desc()));
             messages.add(new Tuple<Long, String>(System.currentTimeMillis(), "Plugin " + p.name() + " is now available for use. Enjoy ;-)"));
         }
     }
     
     public void listenDeparture(@Observes @Specification(Plugin.class) ServiceEvents.ServiceDeparture evt) {
         Plugin p = evt.getService(Plugin.class);
         pluginNames.remove(p.pluginId());
     }
 }
