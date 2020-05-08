 package coucou;
 
 import java.io.*;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import org.codehaus.jettison.json.JSONObject;
 import org.codehaus.jettison.json.JSONException;
 
 
 
 @Path("/coucou")
 public class Coucou {
 
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response repondre() {
         JSONObject data=null;
         String chaine="";
        String fichier ="./src/main/ressources/mock_data.json";
         
         //lecture du fichier texte
         try{
             InputStream ips=new FileInputStream(fichier);
             InputStreamReader ipsr=new InputStreamReader(ips);
             BufferedReader br=new BufferedReader(ipsr);
             String ligne;
             while ((ligne=br.readLine())!=null){
                 chaine+=ligne+"\n";
             }
             br.close();
         }
         catch (Exception e){
             System.out.println(e.toString());
         }
         try{
             data = new JSONObject(chaine);
 
         }
         catch(JSONException JSe){
             System.out.println("pbs JSON file");
         }
         return Response.ok(data.toString()).build();
 
 	}
 	
 	
 }
 
