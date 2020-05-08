 package server;
 
 import java.io.*;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import org.codehaus.jettison.json.JSONObject;
 import org.codehaus.jettison.json.JSONException;
 import java.nio.MappedByteBuffer;
 import java.nio.channels.FileChannel;
 import java.nio.charset.Charset;
 
 @Path("/server")
 public class Server {
 
     @GET
     @Produces(MediaType.APPLICATION_JSON)
     public Response repondre() throws FileNotFoundException, IOException {
         JSONObject data = null;
         JSONObject dataStruct = null;
         JSONObject dataConst = null;
         String chaine = "";
         String path = "./src/main/ressources/g5kMock.json";
 
         FileInputStream stream = new FileInputStream(new File(path));
         try {
             FileChannel fc = stream.getChannel();
             MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
             chaine = Charset.defaultCharset().decode(bb).toString();
             fc.close();
         } finally {
             stream.close();
         }
 
         try {
             data = new JSONObject(chaine);
             String keys[] = {"struct"};
             String keys2[] = {"const"};
             dataStruct = new JSONObject(data, keys);
             dataConst = new JSONObject(data, keys2);
         } catch (JSONException JSe) {
             System.out.println("pbs JSON file");
         }
         
         
         
         
        return Response.ok(dataStruct.toString()).build();
 
     }
 }
