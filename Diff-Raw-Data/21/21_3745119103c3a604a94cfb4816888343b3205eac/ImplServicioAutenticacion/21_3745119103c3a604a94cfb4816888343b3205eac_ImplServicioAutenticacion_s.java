 package com.geored.servicios.impl;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
 
 import javax.ejb.EJB;
 import javax.ejb.Local;
 import javax.ejb.Stateless;
 import javax.ws.rs.core.Response;
 
 import negocios.GestionUsuarios;
 import persistencia.Usuario;
 
 import com.fasterxml.jackson.core.type.TypeReference;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.geored.servicios.ServicioAutenticacion;
 import com.geored.servicios.impl.auth.GestionTokens;
 import com.geored.servicios.json.FacebookUserJSON;
 
 @Local
 @Stateless
 public class ImplServicioAutenticacion implements ServicioAutenticacion {
 	
 	final private static String FACEBOOK_CHECK_URL = "https://graph.facebook.com/me?access_token=";
 	
 	@EJB
 	GestionUsuarios gestionUsuarios;
 	
 	@EJB
 	GestionTokens gestionTokens;
 
 	@Override
 	public Response login(final String usuario, final String password) {
 		int idUsuario = gestionUsuarios.checkLogin(usuario, password);
 		if (idUsuario >= 0) {
			gestionTokens.obtenerToken(idUsuario);
 			return Response.status(Response.Status.OK).entity(gestionTokens.obtenerToken(idUsuario) + ":" + idUsuario).build();
 		}
 		else {
 			return Response.status(Response.Status.UNAUTHORIZED).build();
 		}
 	}
 
 	@Override
 	public Response loginFacebook(String accessToken) {
 		FacebookUserJSON userFacebook;
 		try {
 			URL url = new URL(FACEBOOK_CHECK_URL + accessToken);	
 			URLConnection urlConnection = url.openConnection();
 			BufferedReader rd = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
 			StringBuffer sb = new StringBuffer();
 			String line;
 			while ((line = rd.readLine()) != null)
 			{
 				sb.append(line);
 			}
 			rd.close();
 			String json = sb.toString();
 			ObjectMapper mapper = new ObjectMapper();
 			userFacebook = mapper.readValue(json, new TypeReference<FacebookUserJSON>(){});
 		} catch (Exception e) {
 			return Response.status(Response.Status.UNAUTHORIZED).build();
 		}
 		if (userFacebook.isVerified()) {
 			int idUsuario;
 			if ((idUsuario = gestionUsuarios.checkLoginUsuarioFacebook(userFacebook.getUsername())) >= 0) {
 				return Response.status(Response.Status.OK).entity(gestionTokens.obtenerToken(idUsuario) + ":" + idUsuario).build();
 			}
 			else {
 				Usuario user = new Usuario();
 				user.setNombre(userFacebook.getUsername());
 				user.setFacebookUser(true);
 				gestionUsuarios.registrarUsuario(user);
 				idUsuario = gestionUsuarios.checkLogin(userFacebook.getUsername(), userFacebook.getUsername());
 				return Response.status(Response.Status.OK).entity(gestionTokens.obtenerToken(idUsuario) + ":" + idUsuario).build();
 			}			
 		}
 		else {
 			return Response.status(Response.Status.UNAUTHORIZED).build();
 		}
 	}
 
 	@Override
 	public Response logout(final String userToken) {
 		if (gestionTokens.validarToken(userToken)) {
 			gestionTokens.removeToken(userToken);
 			return Response.status(Response.Status.OK).build();
 		}
 		else {
 			return Response.status(Response.Status.NOT_FOUND).build();
 		}
 	}
 
 }
