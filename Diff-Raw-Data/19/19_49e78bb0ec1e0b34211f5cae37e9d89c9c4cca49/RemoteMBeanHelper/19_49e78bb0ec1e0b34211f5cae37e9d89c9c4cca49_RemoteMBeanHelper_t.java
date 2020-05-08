 package gestv;
 
 import java.util.HashMap;
 import java.util.List;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.FormParam;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 
 import mbean.DynamicMBeanMirrorFactory;
 
 @Path("/")
 public class RemoteMBeanHelper {
 
 	// Registra un MR para su gestion
 	//curl -d "ip=192.168.119.35&port=10001&domain=broadcaster&type=Webservices" http://192.168.119.35:9999/mbs/register
 	@POST
 	@Path("/register")
 	@Produces(MediaType.TEXT_PLAIN)
 	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
 	public String register(
 			@FormParam("ip") String ip,
 			@FormParam("port") String port,
 			@FormParam("domain") String domain, 
 			@FormParam("type") String type) {
 		DynamicMBeanMirrorFactory.register(ip, port, domain, type);
 		return "";
 	}
 
 	// Remueve el registro de un MR
 	//curl -d "ip=192.168.119.35&port=10001" -X DELETE http://192.168.119.35:9999/mbs/broadcaster/Webservices
 	@DELETE
 	@Path("/{domain}/{type}")
 	@Produces(MediaType.TEXT_PLAIN)
 	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
 	public String remove(
 			@FormParam("ip") String ip,
 			@FormParam("port") String port){
 		DynamicMBeanMirrorFactory.removeAll(ip, port);
 		return "";
 	}
 	
 	// Modifica el valor de un atributo dado
 	//curl -d "value=1111111" -X PUT http://192.168.119.35:9999/mbs/broadcaster/Webservices/ga1/voto
 	@PUT
 	@Path("/{domain}/{type}/{name}/{attribute}")
 	  @Produces(MediaType.TEXT_PLAIN)
 	  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
 	  public String setAttribute(
 			  @PathParam("domain") String domain,
 			  @PathParam("name") String name,
 			  @PathParam("type") String type, 
 			  @PathParam("attribute") String attribute,
 			  @FormParam("value") String value) {
 	    return DynamicMBeanMirrorFactory.setAttribute(domain, name, type, attribute, value);
 	  }
 	
 	// Obtiene el valor de un atributo dado
 	@GET
 	@Path("/{domain}/{type}/{name}/{attribute}")
 	  @Produces(MediaType.TEXT_PLAIN)
 	  public String getAttribute(
 			  @PathParam("domain") String domain,
 			  @PathParam("name") String name,
 			  @PathParam("type") String type, 
 			  @PathParam("attribute") String attribute) {
 	    return DynamicMBeanMirrorFactory.getAttribute(domain, name, type, attribute);
 	  }
 
 	// Modifica los valores los atributos de un grupo de atributos
 	@PUT
 	@Path("/{domain}/{type}/{name}/attributes")
 	  @Produces(MediaType.TEXT_PLAIN)
 	  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
 	  public String setAttributes(
 			  @PathParam("domain") String domain, 
 			  @PathParam("name") String name, 
 			  @PathParam("type") String type, 
 			  @FormParam("name") List<String> names, 
 			  @FormParam("value") List<String> values) {
 		HashMap<String, String> attrhm = new HashMap<String, String>();
 		for(int i=0;i<names.size();i++){
 			attrhm.put(names.get(i), values.get(i));
 		}
 		return DynamicMBeanMirrorFactory.setAttributes(domain, name, type, attrhm);
 	  }
 	
 	// Obtiene la descripción XML de un MR dado
 	@GET
 	@Path("/{domain}/{type}")
 	@Produces(MediaType.TEXT_PLAIN)
 	public String getMRInfo(
 			@PathParam("domain") String domain,
 			@PathParam("type") String type) {
 		return DynamicMBeanMirrorFactory.getMRInfo(domain, type);
 	}
 	
 	// Obtiene la descripción XML de un MA dado
 	@GET
 	@Path("/{domain}/{type}/{name}")
 	@Produces(MediaType.TEXT_PLAIN)
 	public String getMAInfo(
 			@PathParam("domain") String domain,
 			@PathParam("name") String name,
 			@PathParam("type") String type) {
 		return DynamicMBeanMirrorFactory.getMAInfo(domain, type, name);
 	}
 	
 	// Cambia el estado de un monitor dado
	// curl -X PUT http://192.168.119.35:9999/mbs/broadcaster/Webservices/ga1/518bbbb58a3d1ed2aa000083/qos/act
 	@PUT
 	@Path("/{domain}/{type}/{name}/{attribute}/{monitor}/{value}")
 	@Produces(MediaType.TEXT_PLAIN)
 	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
 	public String setMonitor(
 			@PathParam("domain") String domain,
 			@PathParam("type") String type,
 			@PathParam("name") String name,
  			@PathParam("attribute") String attribute,
 			@PathParam("monitor") String monitor,
 			@PathParam("value") String value) {
	   return DynamicMBeanMirrorFactory.setMonitor(domain, type, name, attribute, monitor, value);
 	}
 	
 	// Cambia el estado de alerta de un MR dado
	// curl -X PUT http://192.168.119.35:9999/mbs/broadcaster/Webservices/alerts/act
 	@PUT
 	@Path("/{domain}/{type}/alerts/{value}")
 	@Produces(MediaType.TEXT_PLAIN)
 	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
 	public String setAlertable(
 			@PathParam("domain") String domain,
 			@PathParam("type") String type,
 			@PathParam("value") String value) {
 	   return DynamicMBeanMirrorFactory.setAlertable(domain, type, value);
 	}
 	
 }
