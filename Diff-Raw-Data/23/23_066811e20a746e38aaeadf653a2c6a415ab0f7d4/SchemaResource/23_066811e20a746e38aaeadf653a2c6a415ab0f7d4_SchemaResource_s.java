 package ru.oculus.database.resources;
 
 import java.io.IOException;
 
 import javax.ws.rs.DefaultValue;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 import javax.xml.bind.JAXBException;
 
 import org.apache.commons.lang3.ObjectUtils;
 import org.codehaus.jettison.json.JSONArray;
 import org.codehaus.jettison.json.JSONException;
 import org.codehaus.jettison.json.JSONObject;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import com.sun.jersey.api.NotFoundException;
 import com.sun.jersey.spi.resource.Singleton;
 
 import ru.oculus.database.service.ResourceUtils;
 import ru.oculus.database.service.scheme.SchemaInfo;
 import ru.oculus.database.service.scheme.SchemaService;
 import ru.oculus.database.service.sid.Sid;
 import ru.oculus.database.service.sid.SidService;
 
 @Path("/sid/{host}/{sid}/schema")
 @Singleton
 @Produces(MediaType.APPLICATION_JSON)
 public class SchemaResource {
 
     @Autowired
     private SchemaService schemeService;
 
     @Autowired
     private SidService sidService;
 
     @GET
     public JSONArray getAll(
             @PathParam(value = "host") String host,
             @PathParam(value = "sid") String sidName,
             @DefaultValue("0") @QueryParam(value = "minSize") String minSizeGbString,
             @DefaultValue("") @QueryParam(value = "withTable") String withTable
             ) throws JAXBException, IOException, JSONException {
         Sid sid = getSid(host, sidName);
 
         double minSizeGb = Double.parseDouble(minSizeGbString);
         JSONArray result = new JSONArray();
         for (SchemaInfo walker : schemeService.getAll(sid, withTable)) {
             if (minSizeGb == 0 || walker.getSize().doubleValue() > minSizeGb) {
                 JSONObject obj = new JSONObject();
                 obj.put("connectionCount", walker.getConnectionCount());
                 obj.put("name", walker.getName());
                 obj.put("size", walker.getSize());
                 obj.put("lastPatch", ObjectUtils.toString(walker.getLastPatch()));
                 result.put(obj);
             }
         }
 
         return result;
     }
 
     @GET
     @Path("{schemaName}")
     public Object getFullSchemeInfo(
     		@PathParam(value = "host") String host,
             @PathParam(value = "sid") String sidName,
             @PathParam(value = "schemaName") String schemaName,
             @QueryParam("action") String action
     		) throws JAXBException, IOException, InterruptedException {
     	Sid sid = getSid(host, sidName);
 
         if( action == null ) {
             return schemeService.getSchemaInfo(sid, schemaName);
         }
         if( action.equals("drop")) {
             schemeService.dropScheme(sid, schemaName);
             return OperationResult.OK;
         }
         if( action.equals("disconnectAll")) {
             schemeService.disconnectAll(sid, schemaName);
             return OperationResult.OK;
         }
         if( action.equals("truncate")) {
             schemeService.truncate(sid, schemaName);
             return OperationResult.OK;
         }
         throw new NotFoundException();
     }
 
     private Sid getSid(String host, String sidName) throws JAXBException, IOException {
         ResourceUtils.notNull(host, "Specify host");
         ResourceUtils.notNull(sidName, "Specify sid");
         Sid sid = sidService.getSid(host,  sidName);
         ResourceUtils.notNull(sid, "Unkown sid " + sid + "@" + host);
         return sid;
     }
 
 }
