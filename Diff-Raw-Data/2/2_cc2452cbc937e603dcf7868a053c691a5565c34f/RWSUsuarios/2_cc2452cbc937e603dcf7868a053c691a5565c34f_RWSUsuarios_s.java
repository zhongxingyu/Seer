 package tecinf.servicios.rest;
 
 import java.util.List;
 
 import javax.naming.NamingException;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 
 import org.jboss.logging.Logger;
 
 import tecinf.negocio.NegocioUsuario;
 import tecinf.negocio.dtos.GenericJsonResponse;
 import tecinf.negocio.dtos.LoginDataType;
 import tecinf.negocio.dtos.LoginRespDataType;
 import tecinf.negocio.dtos.MensajeCertificado;
 import tecinf.negocio.dtos.UsuarioClienteDataType;
 import tecinf.negocio.dtos.UsuarioDataType;
 import tecinf.negocio.dtos.UsuarioProveedorDataType;
 import tecinf.negocio.utiles.EnumRespuestas;
 import tecinf.negocio.utiles.NegocioFactory;
 import tecinf.servicios.utiles.JSonUtils;
 
 
 @Path("/usuarios")
 public class RWSUsuarios {
 	
 	private static Logger logger = Logger.getLogger(RWSUsuarios.class);
 	
 	private NegocioUsuario negocioUsuario = null;
 	
 	public RWSUsuarios(){
 		
 		try {
 			negocioUsuario = NegocioFactory.getNegocioUsuario();
 		} catch (NamingException e) {
 			logger.error(e.getMessage() , e); 
 		}
 		
 	}
 	
 	@GET
 	@Path("/listarUsuarios")
 	@Produces("application/json")
 	public List<UsuarioClienteDataType> obtenerTodos() {
 		List<UsuarioClienteDataType> listaUsuarios = null;
 		try {
 			negocioUsuario = NegocioFactory.getNegocioUsuario();
 			listaUsuarios = negocioUsuario.obtenerTodosClientes();
 		} catch (Exception e) {
 			logger.error(e.getMessage() , e);
 		} 
 		return listaUsuarios;
  
 	}
 	
 	@PUT
 	@Path("/loginCliente")
 	@Produces("application/json")
 	@Consumes("application/json")
 	public LoginRespDataType login(LoginDataType dt) {
 		LoginRespDataType resp = new LoginRespDataType();
 		try {
 			negocioUsuario = NegocioFactory.getNegocioUsuario();
 			resp = negocioUsuario.loginUsuario(dt.getUsuario(), dt.getContrasenia());
 		} catch (NamingException e) {
 			logger.error(e.getMessage(), e);
 			resp.setRespuesta(EnumRespuestas.RESPUESTA_FALLA);
 		}			
 		return resp;
 	}
 	
 	@PUT
 	@Path("/logout")
 	@Produces("application/json")
 	@Consumes("application/json")
 	public GenericJsonResponse logout(MensajeCertificado dt) { 
 		GenericJsonResponse resp = new GenericJsonResponse();	
 		NegocioUsuario negocioUsuario;
 		try {
 			negocioUsuario = NegocioFactory.getNegocioUsuario();
 			if (negocioUsuario.logout(dt.getUsuario(), dt.getToken()))
 				resp.setResultadoOperacion(EnumRespuestas.RESPUESTA_OK);
 			else
 				resp.setResultadoOperacion(EnumRespuestas.RESPUESTA_FALLA);
 		} catch (NamingException e) {
 			resp.setResultadoOperacion(EnumRespuestas.RESPUESTA_FALLA);
 			logger.error(e.getMessage() , e); 
 		}			
 		return resp;
 	}
 	
 	@GET
 	@Path("/existeUsuario")
 	@Produces("application/json")
 	@Consumes("application/json")
 	public GenericJsonResponse existeUsuario(String usr) {
 		GenericJsonResponse resp = new GenericJsonResponse();
 		try {		
 			negocioUsuario = NegocioFactory.getNegocioUsuario();
 			if (negocioUsuario.existeUsuario(usr))
 				resp.setResultadoOperacion(JSonUtils.RESULTADO_OPERACION_EXITO);
 			else
 				resp.setResultadoOperacion(JSonUtils.RESULTADO_OPERACION_FALLA);
 		} catch (Exception e){
 			logger.error(e.getMessage() , e);
 		}
 		return resp;
 	}
 	
 	@GET
 	@Path("/existeUsuarioPorMail")
 	@Produces("application/json")
 	@Consumes("application/json")
 	public GenericJsonResponse existeUsuarioPorMail(String param) { 
 		GenericJsonResponse resp = new GenericJsonResponse();
 		try {
 			negocioUsuario = NegocioFactory.getNegocioUsuario();
 			if (negocioUsuario.existeUsuarioPorMail(""))
 				resp.setResultadoOperacion(JSonUtils.RESULTADO_OPERACION_EXITO);
 			else
 				resp.setResultadoOperacion(JSonUtils.RESULTADO_OPERACION_FALLA);
 		} catch (Exception e){
 			logger.error(e.getMessage() , e);
 		}
 		return resp;
 	}
 	
 	@PUT
 	@Path("/modificarUsuario")
 	@Produces("application/json")
 	@Consumes("application/json")
 	public GenericJsonResponse modificarUsuario(UsuarioDataType ud){
 		GenericJsonResponse resp = new GenericJsonResponse();
 		try {
 			
 			negocioUsuario = NegocioFactory.getNegocioUsuario();
 			negocioUsuario.modificarUsuario(ud); 
 			
 			resp.setResultadoOperacion(JSonUtils.RESULTADO_OPERACION_EXITO);
 		} catch (Exception e) {
 			logger.error(e.getMessage() , e); 
 			resp.setResultadoOperacion(JSonUtils.RESULTADO_OPERACION_FALLA);
 			resp.setMensageOperacion(e.getMessage());
 		}				
 		return resp;
 	}
 	
 	@POST
 	@Path("/registrarUsuario")
 	@Produces("application/json")
 	@Consumes("application/json")
 	public LoginRespDataType registrarUsuario(UsuarioClienteDataType ud) {
 		LoginRespDataType resp = new LoginRespDataType();
 		try {
 			
 			negocioUsuario = NegocioFactory.getNegocioUsuario();
 			resp = negocioUsuario.registroUsuarioCliente(ud); 
 			
 			resp.setRespuesta(JSonUtils.RESULTADO_OPERACION_EXITO);
 		} catch (Exception e) {
 			logger.error(e.getMessage() , e); 
 			resp.setRespuesta(JSonUtils.RESULTADO_OPERACION_FALLA);
 		}				
 		return resp;
 	}
 	
 	@POST
 	@Path("/registrarProveedor")
 	@Produces("application/json")
 	@Consumes("application/json")
 	public LoginRespDataType registrarProveedor(UsuarioProveedorDataType ud) {
 		LoginRespDataType resp = new LoginRespDataType();
 		try {
 			
 			negocioUsuario = NegocioFactory.getNegocioUsuario();
 			resp = negocioUsuario.registroUsuarioProveedor(ud); 
 			
 			resp.setRespuesta(JSonUtils.RESULTADO_OPERACION_EXITO);
 		} catch (Exception e) {
 			logger.error(e.getMessage() , e); 
			resp.setRespuesta(JSonUtils.RESULTADO_OPERACION_FALLA);
 		}				
 		return resp;
 	}
 	
 }
