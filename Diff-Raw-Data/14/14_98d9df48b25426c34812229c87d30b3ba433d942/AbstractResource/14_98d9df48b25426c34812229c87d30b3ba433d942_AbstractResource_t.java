 package resources;
 
 import com.google.common.base.Charsets;
 import com.google.common.base.Throwables;
 import com.google.common.io.Files;
 import com.google.gson.Gson;
 import com.sun.jersey.api.NotFoundException;
 import templating.ContentWithVariables;
 import templating.Layout;
 import templating.Template;
 import templating.YamlFrontMatter;
 
 import javax.ws.rs.core.Response;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URI;
 import java.util.Date;
 import java.util.Map;
 
 abstract class AbstractResource {
   private static final long ONE_MONTH = 1000L * 3600 * 24 * 30;
 
   protected Response seeOther(URI uri) {
     return Response.seeOther(uri).build();
   }
 
   protected Response seeOther(String url) {
     return seeOther(URI.create(url));
   }
 
   protected Response ok(Object entity, long modified) {
     return Response.ok(entity).lastModified(new Date(modified)).expires(new Date(modified + ONE_MONTH)).build();
   }
 
   protected Response ok(Object entity) {
     return Response.ok(entity).build();
   }
 
   protected String jsonp(Object entity, String callback) {
     String json = (entity instanceof String) ? (String) entity : new Gson().toJson(entity);
 
     if (callback != null) {
       return callback + "(" + json + ")";
     }
 
     return json;
   }
 
   protected Response concat(String... paths) {
     StringBuilder body = new StringBuilder();
     for (String path : paths) {
       body.append(read(path));
     }
     return ok(body.toString(), file(paths[paths.length - 1]).lastModified());
   }
 
  protected String templatize(String text) {
     ContentWithVariables yaml = new YamlFrontMatter().parse(text);
     String content = yaml.getContent();
     Map<String, String> variables = yaml.getVariables();
 
     String layout = variables.get("layout");
     if (layout != null) {
       content = new Layout(read(layout)).apply(content);
     }
 
    return new Template().apply(content, variables);
   }
 
   protected String read(String path) {
     return read(file(path));
   }
 
   protected String read(File file) {
     try {
       return Files.toString(file, Charsets.UTF_8);
     } catch (IOException e) {
       throw Throwables.propagate(e);
     }
   }
 
   protected File file(String path) {
     if (path.endsWith("/")) {
       throw new NotFoundException();
     }
 
     try {
       File root = new File("web");
       File file = new File(root, path);
       if (!file.exists() || !file.getCanonicalPath().startsWith(root.getCanonicalPath())) {
         throw new NotFoundException();
       }
 
       return file;
     } catch (IOException e) {
       throw Throwables.propagate(e);
     }
   }
 }
