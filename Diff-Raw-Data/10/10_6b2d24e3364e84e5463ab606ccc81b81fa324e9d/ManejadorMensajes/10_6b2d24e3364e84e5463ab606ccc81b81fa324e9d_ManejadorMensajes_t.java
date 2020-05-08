 package com.gustavogenovese.servermensajeria.rest;
 
 import com.gustavogenovese.servermensajeria.core.ServicioMensajes;
 import com.gustavogenovese.servermensajeria.core.Sesiones;
 import com.gustavogenovese.servermensajeria.entidades.dtos.MensajeDTO;
 import java.util.Collections;
 import java.util.List;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 /**
  *
  * @author gus
  */
 @Component
 @Path("/mensajes")
 public class ManejadorMensajes {
 
     @Autowired
     private ServicioMensajes servicioMensajes;
 
     @POST
     @Path("/listamensajes")
     @Produces(MediaType.APPLICATION_JSON)
     public Response listaMensajes(@QueryParam("sesion") String sesion){
         String usuarioId = Sesiones.getInstance().getUsuarioId(sesion);
         if (usuarioId == null){
             return Response.status(Response.Status.UNAUTHORIZED).entity(Collections.emptyList()).build();
         }
 
         List<MensajeDTO> mensajes = servicioMensajes.listarMensajesPara(usuarioId);
         return Response.status(Response.Status.OK).entity(mensajes).build();
     }
 
     @POST
     @Path("/listamensajesenviados")
     @Produces(MediaType.APPLICATION_JSON)
     public Response listaMensajesEnviados(@QueryParam("sesion") String sesion){
         String usuarioId = Sesiones.getInstance().getUsuarioId(sesion);
         if (usuarioId == null){
             return Response.status(Response.Status.UNAUTHORIZED).entity(Collections.emptyList()).build();
         }
 
         List<MensajeDTO> mensajes = servicioMensajes.listarMensajesDe(usuarioId);
         return Response.status(Response.Status.OK).entity(mensajes).build();
     }
 
     @POST
     @Path("/nuevomensaje")
     @Produces(MediaType.APPLICATION_JSON)
     public Response enviarMensaje(@QueryParam("sesion") String sesion,
                                   @QueryParam("destinatario") String destinatario,
                                  @PathParam("mensaje") String mensaje) {
         String remitenteId = Sesiones.getInstance().getUsuarioId(sesion);
         if (remitenteId == null){
             return Response.status(Response.Status.UNAUTHORIZED).entity(false).build();
         }
 
         if (destinatario == null){
            return Response.status(Response.Status.NOT_ACCEPTABLE).entity(false).build();
         }
 
         boolean resultado = servicioMensajes.enviarMensaje(remitenteId, destinatario, mensaje);
         if (resultado){
             return Response.status(Response.Status.CREATED).entity(true).build();
         }else{
            return Response.status(Response.Status.NOT_ACCEPTABLE).entity(true).build();
         }
     }
 }
