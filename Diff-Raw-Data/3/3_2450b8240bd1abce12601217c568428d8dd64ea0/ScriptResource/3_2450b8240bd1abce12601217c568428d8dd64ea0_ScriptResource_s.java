 package cue.hegemon.example;
 
 import cue.hegemon.LoadError;
 import cue.hegemon.LoadPath;
 import cue.hegemon.Script;
 import cue.hegemon.ScriptCache;
 import com.google.common.io.ByteStreams;
 
 import javax.script.ScriptException;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.List;
 import java.util.Map;
 
 /**
  * This class is an example of making javascript scripts available as http endpoints.
  */
 @Path("/script/{name}")
 @Produces(MediaType.APPLICATION_JSON)
 public class ScriptResource {
   /**
    * The ScriptCache takes a LoadPath object that it uses to locate scripts. By default, it will load
    * javascript in the resources/javascript/ directory, but additional paths can be passed as well.
    * Custom loading schemes can be made available by implementing the ScriptLocator interface.
    */
   private static ScriptCache scriptCache = new ScriptCache(new LoadPath());
 
 
   // post and get just pass along their name, query parameters, and any post data.
   @POST
   public Object post(@PathParam("name") String name, @Context UriInfo uriInfo, InputStream request)
       throws IOException {
     return run(name, "POST", uriInfo.getQueryParameters(), ByteStreams.toByteArray(request));
   }
 
   @GET
   public Object get(@PathParam("name") String name, @Context UriInfo uriInfo) {
     return run(name, "GET", uriInfo.getQueryParameters(), null);
   }
 
 
   /**
    * run loads and runs anything in "script/" from the cache.
    */
   private Object run(String name, String method, Map<String, List<String>> params, final byte[] postData) {
     try {
       final Script script = scriptCache.get("script/" + name + ".js", params.containsKey("reload"));
       // We load "common.js" into the script by hand so we can call the js defined
       // getParamsAsObject function to coerce 'params' into a js object.
       script.load("common");
       Object paramsObject = script.run("getParamsAsObject", params);
       return script.run(method, paramsObject, postData);
 
     } catch (LoadError loadError) {
       throw new WebApplicationException(Response.Status.NOT_FOUND);
 
     } catch (ScriptException e) {
      // TODO(kevinclark): Return proper status code.
      return e.getMessage() + "(" + e.getLineNumber() + ":" + e.getColumnNumber() + ")";
     }
   }
 }
