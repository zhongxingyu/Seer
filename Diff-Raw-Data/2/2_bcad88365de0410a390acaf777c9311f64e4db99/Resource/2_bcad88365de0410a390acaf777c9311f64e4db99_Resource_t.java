 package org.mock.resource;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map.Entry;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 import javax.ws.rs.core.UriInfo;
 
 import org.codehaus.jettison.json.JSONArray;
 import org.codehaus.jettison.json.JSONException;
 import org.codehaus.jettison.json.JSONObject;
 
 @Path("/{entityName}")
 public class Resource {
 
     public Resource() {
     }
 
     private JSONObject getConfig() throws Exception {
         String fileName = getClass().getClassLoader().getResource(".")
                 .getPath()
                 + "../MockedStore/MockedRest!.config.json";
         String str = readFile(fileName);
         return new JSONObject(str);
 
     }
 
     private String getStoreFileName(String entityName) {
         String folderName = getClass().getClassLoader().getResource(".")
                 .getPath()
                 + "../MockedStore/";
         File folder = new File(folderName);
         if (!folder.exists())
             folder.mkdir();
         return folderName + entityName + ".json";
 
     }
 
     private JSONObject readStoreAction(String entityName) throws Exception {
         String fileName = getStoreFileName(entityName + ".action");
         File f = new File(fileName);
         if (f.exists()) {
             String str = readFile(fileName);
             if (!str.equals(""))
                 return new JSONObject(str);
         }
         return new JSONObject();
     }
 
     private JSONObject readStoreConfig(String entityName) throws Exception {
         String fileName = getStoreFileName(entityName + ".config");
         File f = new File(fileName);
         if (f.exists()) {
             String str = readFile(fileName);
             if (!str.equals(""))
                 return new JSONObject(str);
         }
         return new JSONObject();
     }
 
     private String readFile(String fileName) throws Exception {
         BufferedReader br = new BufferedReader(new FileReader(fileName));
         try {
             StringBuilder sb = new StringBuilder();
             String line = br.readLine();
 
             while (line != null) {
                 sb.append(line);
                 sb.append("\n");
                 line = br.readLine();
             }
             return (sb.toString());
         } finally {
             br.close();
         }
 
     }
 
     private JSONArray readStore(String entityName, Boolean createFile)
             throws Exception {
         JSONArray result = null;
         String fileName = getStoreFileName(entityName);
         File f = new File(fileName);
         if (f.exists()) {
             String str = readFile(fileName);
             if (str.equals(""))
                 result = new JSONArray();
             else
                 result = new JSONArray(str);
         } else {
             if (createFile)
                 f.createNewFile();
             result = new JSONArray();
         }
         return result;
     }
 
     private void saveStore(String entityName, Object store) throws Exception {
 
         BufferedWriter bw = new BufferedWriter(new FileWriter(
                 getStoreFileName(entityName)));
         try {
             String s = "";
             if (store instanceof JSONArray)
                 s = ((JSONArray) store).toString(2);
             else if (store instanceof JSONObject)
                 s = ((JSONObject) store).toString(2);
             else
                 throw new Exception("not supported");
             bw.write(s);
         } finally {
             bw.close();
         }
 
     }
 
     private JSONObject find(JSONArray store, String id) throws JSONException {
         for (int i = 0; i < store.length(); i++) {
             if (store.getJSONObject(i).optString("id").equals(id))
                 return store.getJSONObject(i);
         }
         return new JSONObject();
     }
 
     private Integer findIndex(JSONArray store, String id) throws JSONException {
         for (int i = 0; i < store.length(); i++) {
             if (store.getJSONObject(i).optString("id").equals(id))
                 return i;
         }
         return -1;
     }
 
     private Response processError(String entityName, String verbName)
             throws Exception {
         JSONObject config = readStoreConfig(entityName);
         if (config.has("error")) {
             JSONObject error = config.getJSONObject("error");
             if (error.has(verbName)) {
                 JSONObject verb = error.getJSONObject(verbName);
                 if (verb.getInt("interval") > 0)
                     if (verb.getInt("interval") == verb.getInt("last")) {
                         verb.put("last", 0);
                         saveStore(entityName + ".config", config);
                         return Response.ok()
                                 .status(Status.INTERNAL_SERVER_ERROR).build();
                     } else {
                         verb.put("last", verb.getInt("last") + 1);
                         saveStore(entityName + ".config", config);
                     }
             }
         }
         return null;
     }
 
     private void processDelay(String entityName, String verbName)
             throws Exception {
         JSONObject config = readStoreConfig(entityName);
         if (config.has("delay")) {
             JSONObject error = config.getJSONObject("delay");
             if (error.has(verbName)) {
                 JSONObject verb = error.getJSONObject(verbName);
                 if (verb.getInt("interval") > 0)
                     if (verb.getInt("interval") == verb.getInt("last")) {
                         verb.put("last", 0);
                         saveStore(entityName + ".config", config);
                         Thread.sleep(verb.getInt("value"));
                     } else {
                         verb.put("last", verb.getInt("last") + 1);
                         saveStore(entityName + ".config", config);
                     }
             }
         }
     }
 
     @POST
     @Path("")
     @Consumes({ MediaType.APPLICATION_JSON })
     @Produces({ MediaType.APPLICATION_JSON })
     public Response create(String string,
             @PathParam("entityName") String entityName, @Context UriInfo uriInfo)
             throws Exception {
         processDelay(entityName, "post");
         Response response = processError(entityName, "post");
         if (response != null)
             return response;
         JSONArray store = readStore(entityName, true);
         Integer id = 1;
         for (int i = 0; i < store.length(); i++) {
             if (store.getJSONObject(i).optInt("id") >= id)
                 id = store.getJSONObject(i).getInt("id") + 1;
         }
 
         JSONObject jo = new JSONObject(string);
         jo.put("id", id);
 
         store.put(jo);
 
         saveStore(entityName, store);
 
         return Response.ok(jo.toString(2))
                 .type(MediaType.APPLICATION_JSON_TYPE).build();
     }
 
     @POST
     @Path("/{entityId}/{subEntityName}")
     @Consumes({ MediaType.APPLICATION_JSON })
     @Produces({ MediaType.APPLICATION_JSON })
     public Response createEx(String string, @PathParam("entityId") String entityId,
             @PathParam("entityName") String entityName,
             @PathParam("subEntityName") String subEntityName,
             @Context UriInfo uriInfo) throws Exception {
         JSONArray store = readStore(entityName + "_" + entityId + "_"
                 + subEntityName, true);
 
         Integer id = 1;
         for (int i = 0; i < store.length(); i++) {
             if (store.getJSONObject(i).optInt("id") >= id)
                 id = store.getJSONObject(i).getInt("id") + 1;
         }
 
         JSONObject jo = new JSONObject(string);
         jo.put("id", id);
 
         store.put(jo);
 
         saveStore(entityName + "_" + entityId + "_" + subEntityName, store);
 
         return Response.ok(jo.toString(2))
                 .type(MediaType.APPLICATION_JSON_TYPE).build();
     }
 
     @GET
     @Produces({ MediaType.APPLICATION_JSON })
     public Response readAll(@PathParam("entityName") String entityName,
             @Context UriInfo uriInfo) throws Exception {
         processDelay(entityName, "get");
         Response response = processError(entityName, "get");
         if (response != null)
             return response;
         JSONArray store = readStore(entityName, false);
         JSONArray result = new JSONArray();
 
         for (int i = 0; i < store.length(); i++) {
             boolean add = true;
             JSONObject item = (JSONObject) store.get(i);
             for (Entry<String, List<String>> param : uriInfo
                     .getQueryParameters().entrySet()) {
                 if (item.has(param.getKey())
                         && param.getValue().size() == 1
                         && !item.getString(param.getKey()).equals(
                                 param.getValue().get(0))) {
                     add = false;
                     break;
                 }
             }
             if (add)
                 result.put(item);
         }
         return Response.ok(result.toString(2))
                 .type(MediaType.APPLICATION_JSON_TYPE).build();
     }
 
     @GET
     @Path("/{id}")
     @Produces({ MediaType.APPLICATION_JSON })
     public Response read(@PathParam("entityName") String entityName,
             @PathParam("id") String id) throws Exception {
         processDelay(entityName, "get");
         Response response = processError(entityName, "get");
         if (response != null)
             return response;
         return Response.ok(find(readStore(entityName, false), id).toString(2))
                 .type(MediaType.APPLICATION_JSON_TYPE).build();
     }
 
     @GET
     @Path("/{entityId}/{subEntityName}")
     @Produces({ MediaType.APPLICATION_JSON })
     public Response readEx(@PathParam("entityName") String entityName,
             @PathParam("entityId") String entityId,
             @PathParam("subEntityName") String subEntityName,
             @Context UriInfo uriInfo) throws Exception {
 
        JSONObject ex = getConfig().optJSONObject(entityName);
         if (ex != null && ex.getString("path").equals(subEntityName)) {
             processDelay(entityName, "get");
             Response response = processError(entityName, "get");
             if (response != null)
                 return response;
             ExtendedResource er = (ExtendedResource) Class.forName(
                     ex.getJSONObject("get").getString("class")).newInstance();
             return er.process(uriInfo);
         } else {
             JSONArray store = readStore(entityName + "_" + entityId + "_"
                     + subEntityName, false);
             if (store.length() > 0)
                 return Response.ok(store.toString(2)).build();
         }
         return Response.status(Status.NOT_FOUND).build();
     }
 
     @GET
     @Path("/{entityId}/{subEntityName}/{id}")
     @Produces({ MediaType.APPLICATION_JSON })
     public Response readExt(@PathParam("entityName") String entityName,
             @PathParam("entityId") String entityId,
             @PathParam("subEntityName") String subEntityName,
             @PathParam("id") String id) throws Exception {
 
         return Response.ok(find(readStore(entityName + "_" + entityId + "_" + subEntityName, false), id).toString(2))
                 .type(MediaType.APPLICATION_JSON_TYPE).build();
     }
 
     @PUT
     @Path("/{id}")
     @Consumes({ MediaType.APPLICATION_JSON })
     @Produces({ MediaType.APPLICATION_JSON })
     public Response update(String string,
             @PathParam("entityName") String entityName,
             @PathParam("id") String id) throws Exception {
         processDelay(entityName, "put");
         Response response = processError(entityName, "put");
         if (response != null)
             return response;
         JSONObject jo = new JSONObject(string);
         jo.put("id", id);
 
         JSONArray store = readStore(entityName, true);
         JSONObject action = readStoreAction(entityName);
         Integer index = findIndex(store, id);
         JSONObject currentRow = store.getJSONObject(index);
 
         JSONArray putAction = action.optJSONArray("put");
         if (putAction != null) {
             for (int i = 0; i < putAction.length(); i++) {
                 String mapField = putAction.optJSONObject(i).optString(
                         "mapField");
                 if (mapField != null && jo.has(mapField)) {
                     String fieldValue = jo.getString(mapField);
                     jo.remove(mapField);
                     JSONArray actions = putAction.getJSONObject(i)
                             .getJSONArray("actions");
                     for (int j = 0; j < actions.length(); j++) {
                         if (actions.getJSONObject(j).getString("value")
                                 .equals(fieldValue)) {
                             JSONObject data = actions.getJSONObject(j)
                                     .getJSONObject("data");
                             Iterator keys = data.keys();
                             while (keys.hasNext()) {
                                 String key = (String) keys.next();
                                 currentRow.put(key, data.get(key));
                             }
                             store.put(index, currentRow);
                             saveStore(entityName, store);
                             return Response.ok()
                                     .type(MediaType.APPLICATION_JSON).build();
                         }
                     }
                 }
             }
 
         }
         Iterator keys = jo.keys();
         while (keys.hasNext()) {
             String key = (String) keys.next();
             currentRow.put(key, jo.get(key));
         }
         store.put(index, currentRow);
         saveStore(entityName, store);
         return Response.ok(currentRow.toString(2))
                 .type(MediaType.APPLICATION_JSON).build();
     }
 
     @PUT
     @Path("/{entityId}/{subEntityName}/{id}")
     @Consumes({ MediaType.APPLICATION_JSON })
     @Produces({ MediaType.APPLICATION_JSON })
     public Response updateEx(String string,
             @PathParam("entityName") String entityName,
             @PathParam("entityId") String entityId,
             @PathParam("subEntityName") String subEntityName,
             @PathParam("id") String id) throws Exception {
         JSONObject jo = new JSONObject(string);
         JSONArray store = readStore(entityName + "_" + entityId + "_"
                 + subEntityName, true);
         Integer index = findIndex(store, id);
         JSONObject currentRow = store.getJSONObject(index);
 
         Iterator keys = jo.keys();
         while (keys.hasNext()) {
             String key = (String) keys.next();
             currentRow.put(key, jo.get(key));
         }
         store.put(index, currentRow);
         saveStore(entityName + "_" + entityId + "_" + subEntityName, store);
         return Response.ok(currentRow.toString(2))
                 .type(MediaType.APPLICATION_JSON).build();
     }
 
     @DELETE
     @Path("/{id}")
     public Response delete(@PathParam("entityName") String entityName,
             @PathParam("id") String id) throws Exception {
         processDelay(entityName, "delete");
         Response response = processError(entityName, "delete");
         if (response != null)
             return response;
         JSONArray store = readStore(entityName, false);
         JSONObject joid = find(store, id);
         if (joid.length() == 0)
             return Response.ok("invalid id").status(Status.NOT_FOUND).build();
         store.remove(joid);
         // TODO remove subEntities
         saveStore(entityName, store);
         return Response.ok().build();
     }
 
     @DELETE
     @Path("/{entityId}/{subEntityName}/{id}")
     public Response deleteEx(@PathParam("entityName") String entityName,
             @PathParam("entityId") String entityId,
             @PathParam("subEntityName") String subEntityName,
             @PathParam("id") String id) throws Exception {
         JSONArray store = readStore(entityName + "_" + entityId + "_"
                 + subEntityName, false);
         JSONObject joid = find(store, id);
         if (joid.length() == 0)
             return Response.ok("invalid id").status(Status.NOT_FOUND).build();
         store.remove(joid);
         saveStore(entityName + "_" + entityId + "_"
                 + subEntityName, store);
         return Response.ok().build();
     }
 }
