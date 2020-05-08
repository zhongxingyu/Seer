 package gov.nih.ncgc.bardplugin;
 
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.node.ArrayNode;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import org.apache.catalina.*;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 
 
 /**
  * This plugin is represents the plugin registry resource.
  * <p/>
  * Its basic task is to list the available plugins. This is an example of
  * a plugin that does not use Jersey at all, and instead is a basic servlet.
  * <p/>
  * Note that this plugin will not show itself in the list of plugins and will
  * not pass the BARD Plugin Validation tests.
  * <p/>
  * The plugin somewhat different from other plugins in that it implements
  * {@link ContainerServlet} which must run in a privileged context. As a result
  * do not deploy this plugin in the same context as other plugins.
  *
  * @author Rajarshi Guha
  */
 public class Registry extends HttpServlet implements ContainerServlet {
 
     Wrapper wrapper;
     Host host;
     Context context;
 
     public Wrapper getWrapper() {
         return wrapper;
     }
 
     public void setWrapper(Wrapper wrapper) {
         this.wrapper = wrapper;
         if (wrapper == null) {
             context = null;
             host = null;
         } else {
             context = (org.apache.catalina.Context) wrapper.getParent();
             host = (Host) context.getParent();
         }
     }
 
     public void doGet(HttpServletRequest request,
                       HttpServletResponse response)
             throws IOException, ServletException {
 
         String command = request.getRequestURI();
         response.setContentType("text/plain; charset=UTF-8");
         PrintWriter writer = response.getWriter();
         if (command.endsWith("/registry/list")) {
             response.setContentType("application/json");
             listPlugins(writer, request);
         } else if (command.endsWith("/registry")) {
             response.setContentType("text/plain");
             writer.println("BARD Plugin Registry");
         } else response.sendError(HttpServletResponse.SC_NOT_FOUND);
         writer.flush();
         writer.close();
     }
 
     public void listPlugins(PrintWriter writer, HttpServletRequest request) throws IOException {
         ObjectMapper mapper = new ObjectMapper();
         ArrayNode anode = mapper.createArrayNode();
 
         Container[] containers = host.findChildren();
         for (Container container : containers) {
             org.apache.catalina.Context context = (org.apache.catalina.Context) container;
 
             String path = context.getPath();
             String docBase = context.getDocBase();
             boolean isRunning = context != null && context.getAvailable();
 
             if (!path.startsWith("/bardplugin_")) continue;
             if (path.equals("/bardplugin_registry")) continue;
 
             String pluginRoot = path.split("_")[1];
 
             HttpClient client = new DefaultHttpClient();
            String url = getInitParameter("tomcatHost") + path + "/" + pluginRoot + "/_version";
             HttpGet get = new HttpGet(url);
             HttpResponse response = client.execute(get);
            String version = null;
            if (response.getStatusLine().getStatusCode() == 200) {
                version = "";
                BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                String line = "";
                while ((line = rd.readLine()) != null) {
                    version += line;
                }
            } else {
                isRunning = false;
             }
 
             ObjectNode node = mapper.createObjectNode();
             node.put("path", "/plugins/" + pluginRoot);
             node.put("version", version);
             node.put("available", isRunning);
             anode.add(node);
         }
         String json = mapper.writeValueAsString(anode);
         writer.print(json);
     }
 
 }
