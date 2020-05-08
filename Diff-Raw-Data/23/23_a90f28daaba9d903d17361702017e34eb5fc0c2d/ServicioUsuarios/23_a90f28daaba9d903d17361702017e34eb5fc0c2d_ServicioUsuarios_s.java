 package com.geored.servicios;
 
 import java.util.List;
 
 import javax.servlet.http.HttpServletResponse;
 import javax.ws.rs.GET;
 import javax.ws.rs.HeaderParam;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Context;
 
 import com.geored.servicios.json.InvitacionJSON;
 import com.geored.servicios.json.UsuarioJSON;
 
 @Path("usuarios")
 public interface ServicioUsuarios {
 
 	@GET
 	@Path("contactos")
 	@Produces("application/json")
 	public List<UsuarioJSON> getContactos(
 			@HeaderParam("Security-Token") final String userToken,
 			@Context final HttpServletResponse response);
 
 	@GET
 	@Path("contactos/{idContacto}")
 	@Produces("application/json")
 	public UsuarioJSON getContacto(
 			@HeaderParam("Security-Token") final String userToken,
 			@Context final HttpServletResponse response,
 			@PathParam("idContacto") final int idContacto);
 
 	@GET
 	@Path("invitaciones")
 	@Produces("application/json")
 	public List<InvitacionJSON> getInvitaciones(
 			@HeaderParam("Security-Token") final String userToken,
 			@Context final HttpServletResponse response);
 
 	@PUT
 	@Path("invitaciones/{idContacto}")
 	public void invitarContacto(
 			@HeaderParam("Security-Token") final String userToken,
 			@Context final HttpServletResponse response,
 			@PathParam("idContacto") final int idContacto);
 	
 	@POST
 	@Path("invitaciones/{idContacto}")
 	public void aceptarInvitacion(
 			@HeaderParam("Security-Token") final String userToken,
 			@Context final HttpServletResponse response,
 			@PathParam("idContacto") final int idContacto);
 
 	@PUT
 	@Path("usuario/{password}")
	void registrarUsuario(@HeaderParam("Security-Token") final String userToken, @Context final HttpServletResponse response,
 			@PathParam("password") final String password, final UsuarioJSON usuarioJSON);
 
 	@POST
 	@Path("usuario/{password}")
 	void modificarUsuario(@HeaderParam("Security-Token") final String userToken, @Context final HttpServletResponse response,
 			@PathParam("password") final String password, final UsuarioJSON usuarioJSON);
 
 }
