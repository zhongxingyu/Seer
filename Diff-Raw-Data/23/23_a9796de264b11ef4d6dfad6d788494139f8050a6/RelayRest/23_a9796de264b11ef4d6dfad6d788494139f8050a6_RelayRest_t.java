 /**
  * 
  */
 package ar.com.cuyum.cnc.service.rest;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import javax.enterprise.context.RequestScoped;
 import javax.inject.Inject;
 import javax.ws.rs.FormParam;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 
 import org.apache.log4j.Logger;
 import org.primefaces.json.JSONException;
 import org.primefaces.json.JSONObject;
 
 import ar.com.cuyum.cnc.service.RelayService;
import ar.com.cuyum.cnc.utils.FormRenderProperties;
 
 /**
  * @author Jorge Morando
  *
  */
 @Path("/service")
 @RequestScoped
 public class RelayRest {
 
 	private Logger log = Logger.getLogger(RelayRest.class);
 
 	@Inject
 	private RelayService relay;
 	
	@Inject
	private FormRenderProperties frp;
	
 	@POST
 	@Path("/relay")
 	@Produces("application/json")
 	public String relay(@FormParam("remoteUrl") String remoteUrl, @FormParam("fkey") String fkey) {
 		log.info("Ingreso en relay de "+remoteUrl+(fkey!=null?" con el siguiente fkey "+fkey:""));
 		
 		JSONObject response;
 		try {
			URL url = new URL(frp.getRemoteListHost()+remoteUrl);
 			String remoteResponse = relay.request(url, fkey);
 			response = new JSONObject(remoteResponse);
 			return response.toString();
 			
 		}catch (MalformedURLException e){
 			String msg = "No se pudo generar la petici&oacute;n con relay service ya que la URL suministrada es inv&aacute;lida";
 			log.error(msg,e);
 			return "{\"success\":false,\"msg\": \""+msg+"\"}";
 		} catch (JSONException e) {
 			String msg = "No se pudo generar la respuesta json en relay service ya que el servicio ha respondido en un objeto JSON inv&aacute;lido";
 			log.error(msg,e);
 			return "{\"success\":false,\"msg\": \""+msg+"\"}";
 		}
 	}
 	
 }
