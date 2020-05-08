 package com.researchmobile.coretel.ws;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.mime.HttpMultipartMode;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.FileBody;
 import org.apache.http.entity.mime.content.StringBody;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.protocol.HttpContext;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.os.Environment;
 import android.util.Log;
 
 import com.researchmobile.coretel.entity.Anotacion;
 import com.researchmobile.coretel.entity.CatalogoAnotacion;
 import com.researchmobile.coretel.entity.CatalogoComunidad;
 import com.researchmobile.coretel.entity.CatalogoInvitacion;
 import com.researchmobile.coretel.entity.CatalogoMiembro;
 import com.researchmobile.coretel.entity.CatalogoSolicitud;
 import com.researchmobile.coretel.entity.CatalogoTipoAnotacion;
 import com.researchmobile.coretel.entity.DetalleComunidad;
 import com.researchmobile.coretel.entity.Invitacion;
 import com.researchmobile.coretel.entity.Miembro;
 import com.researchmobile.coretel.entity.RespuestaWS;
 import com.researchmobile.coretel.entity.Solicitud;
 import com.researchmobile.coretel.entity.TipoAnotacion;
 import com.researchmobile.coretel.entity.User;
 import com.researchmobile.coretel.entity.Usuario;
 
 public class RequestWS {
 	private static final String WS_LOGIN = "ws_login.php?usuario=";
 	private static final String WS_COMUNIDADES = "ws_comunidad.php?id=";
 	private static final String WS_ANOTACIONES = "ws_anotacion.php?usuario=";
 	private static final String WS_DATOSUSUARIO = "ws_usuario.php?id=";
 	private static final String WS_CREAUSUARIO = "login.actions.php?client=1&a=add&email=";
 	private static final String WS_MISCOMUNIDADES = "ws_comunidad2.php?usuario=";
 	private static final String WS_DETALLECOMUNIDAD = "ws_comunidad.php?id=";
 	private static final String WS_CATALOGOMIEMBRO = "ws_miembro.php?comunidad=";
 	private static final String WS_MANDAREVENTO = "dashboard.anotaciones.actions.php?idusuario=";
 	private static final String WS_CREACOMUNIDAD = "ws_crear_comunidad.php?usuario=";
 	private static final String WS_CAMBIARCLAVE = "ws_update_usuario.php?action=clave&id=";
 	private static final String WS_VERINVITACIONES = "ws_invitacion.php?&usuario=";
 	private static final String WS_ENVIARINVITACION = "ws_invitacion.php?&invita=";
 	private static final String WS_VERINVITACIONESENVIADAS = "ws_invitacion.php?&usuario=";
 	private static final String WS_VERSOLICITUDESRECIBIDAS = "ws_invitacion.php?&usuario=";
 	private static final String WS_VERSOLICITUDESENVIADAS = "ws_invitacion.php?&usuario=";
 	private static final String WS_RESPUESTAINVITACON = "ws_invitacion.php?&id=";
 	private static final String WS_CHAT = "envio?usuario=Luis&mensaje=";
 	private static final String WS_NUEVOTIPOANOTACION = "ws_crear_tipo_anotacion.php?comunidad=";
 	private static final String WS_LISTATIPOANOTACION = "ws_tipo_anotacion.php?comunidad=";
 	private static final String WS_ELIMINATIPOANOTACION = "ws_delete_tipo_anotacion.php?id=";
 	private static final String WS_ELIMINAANOTACION = "ws_delete_annotation.php?id=";
 	private static final String WS_ELIMINACOMUNIDAD = "ws_delete_comunidad.php?id=";
 	private static final String WS_ELIMINAMIEMBRO = "ws_sacarmiembro_comunidad.php?id=";
 	private static final String WS_EDITARPERFIL = "ws_update_usuario.php?action=modificar&id=";
 	private static final String WS_EDITARCOMUNIDAD = "ws_update_comunidad.php?id=";
 	private static final String WS_RECUPERARCLAVE = "ws_forgot.php?email=";
 	private static final String WS_COMUNIDADESTODAS = "ws_comunidades.php?usuario=";
 	private static final String WS_ENVIARSOLICITUD = "ws_solicitud.php?comunidad=";
 	
 	private ConnectWS connectWS = new ConnectWS();
 	
 	public RespuestaWS Login(User user){
 		JSONObject jsonObject = null;
 		String finalURL = WS_LOGIN + user.getUsername() + "&clave=" + user.getPassword();
 		RespuestaWS respuesta = new RespuestaWS();
 		try {
 			jsonObject = connectWS.Login(finalURL);
 			if (jsonObject != null){
 				User user_t = new User();
 				respuesta.setResultado(Boolean.parseBoolean(jsonObject.getString("resultado")));
 				respuesta.setMensaje(jsonObject.getString("mensaje"));
 				if (respuesta.isResultado()){
 					JSONArray datosUsuario = jsonObject.getJSONArray("usuario");
 					JSONObject id = (JSONObject) datosUsuario.get(0);
 					user_t.setUserId(id.getString("id"));
 					User.setFechaRegistro(id.getString("fecha_registro"));
 					User.setIdTipoUsuario(id.getString("id_tipo_usuario"));
 					User.setActivo(id.getString("activo"));
 					User.setNombre(id.getString("nombre"));
 					user_t.setEmail(id.getString("email"));
 					User.setTelefono(id.getString("telefono"));
 					User.setAvatar(id.getString("avatar"));
 					User.setIdPadre(id.getString("id_padre"));
 					User.setNivel(id.getString("nivel"));
 					User.setSuperUsuario(id.getString("super_usuario"));
 					User.setSupervisorUsuario(id.getString("supervisor_usuario"));
 				}
 				return respuesta;
 			}else{
 				respuesta.setResultado(false);
 				respuesta.setMensaje("Intente nuevamente");
 				return respuesta;
 			}
 		} catch (JSONException e) {
 			respuesta.setResultado(false);
 			respuesta.setMensaje("Problemas de conexion");
 			return respuesta;
 		}
 	}
 	
 	public RespuestaWS EnviarMensajeChat(String miMensaje) {
 		JSONObject jsonObject = null;
 
 
 		String finalURL = WS_CHAT + miMensaje;
 		String url = finalURL.replace(" ", "%20");
 		RespuestaWS respuesta = new RespuestaWS();
 		try{
 			jsonObject = connectWS.Chat(url);
 			if (jsonObject != null){
 				respuesta.setResultado(jsonObject.getBoolean("resultado"));
 				respuesta.setMensaje(jsonObject.getString("mensaje"));
 				System.out.println(respuesta.getMensaje());
 				return respuesta;
 			}
 		}catch(Exception exception){
 			return null;
 		}
 		return null;
 	}
 	
 	public RespuestaWS NuevoTipoEvento(String idComunidad, String nombre, String descripcion, String incidente, String icono) {
 		JSONObject jsonObject = null;
 		
 		String finalURL = WS_NUEVOTIPOANOTACION + idComunidad + "&usuario=" + User.getUserId() + "&incidente=" + incidente + "&administrador=1&nombre=" + nombre + "&descripcion=" + descripcion + "&icono=" + icono;
 		String url = finalURL.replace(" ", "%20");
 		RespuestaWS respuesta = new RespuestaWS();
 		try{
 			jsonObject = connectWS.NuevoTipoEvento(url);
 			if (jsonObject != null){
 				respuesta.setResultado(jsonObject.getBoolean("resultado"));
 				respuesta.setMensaje(jsonObject.getString("mensaje"));
 				return respuesta;
 			}
 		}catch(Exception exception){
 			return respuesta;
 		}
 		return null;
 	}
 	
 	public RespuestaWS CrearUsuario(String nombre, String usuario, String email, String telefono) {
 		JSONObject jsonObject = null;
 
 		String urlTemp = WS_CREAUSUARIO + email + "&nombre=" + nombre + "&telefono=" + telefono + "&usuario=" + usuario;
 		String finalURL = urlTemp.replace(" ", "%20");
 		RespuestaWS respuesta = new RespuestaWS();
 		try{
 			jsonObject = connectWS.CreaUsuario(finalURL);
 			if (jsonObject != null){
 				respuesta.setResultado(jsonObject.getBoolean("respuesta"));
 				respuesta.setMensaje(jsonObject.getString("mensaje"));
 				return respuesta;
 			}
 		}catch (Exception exception){
 			return respuesta;
 		}
 		return null;
 	}
 	
 	public CatalogoComunidad CargarComunidadesTodas(String id) {
 		Log.e("TT", "buscando anotaciones, idusuario = " + id);
 		JSONObject jsonObject = null;
 		String finalURL = WS_COMUNIDADESTODAS + User.getUserId();
 		RespuestaWS respuesta = new RespuestaWS();
 		CatalogoComunidad catalogo = new CatalogoComunidad();
 		try{
 			jsonObject = connectWS.ComunidadesTodas(finalURL);
 			if (jsonObject != null){
 				respuesta.setResultado(jsonObject.getBoolean("resultado"));
 				respuesta.setMensaje(jsonObject.getString("mensaje"));
 				if (jsonObject.has("comunidad")){
 					JSONArray jsonArray = jsonObject.getJSONArray("comunidad");
 					int tamano = jsonArray.length();
 					DetalleComunidad[] comunidad = new DetalleComunidad[tamano];
 					for (int i = 0; i < tamano; i++){
 						JSONObject json = jsonArray.getJSONObject(i);
 						DetalleComunidad com = new DetalleComunidad();
 						com.setId(json.getString("id"));
 						com.setNombre(json.getString("nombre"));
 						comunidad[i] = com;
 					}
 					catalogo.setRespuestaWS(respuesta);
 					catalogo.setComunidad(comunidad);
 					return catalogo;
 				}else{
 					return catalogo;
 				}
 			}else{
 				respuesta.setResultado(false);
 				respuesta.setMensaje("No se pudieron cargar las comunidades");
 				catalogo.setRespuestaWS(respuesta);
 				return catalogo;
 			}
 		}catch (Exception exception){
 			respuesta.setResultado(false);
 			respuesta.setMensaje("No se pudieron cargar las comunidades");
 			catalogo.setRespuestaWS(respuesta);
 			return catalogo;
 		}
 	}
 	
 	public CatalogoComunidad CargarComunidades(String id) {
 		JSONObject jsonObject = null;
 		JSONArray jsonArray = null;
 		String finalURL = WS_MISCOMUNIDADES + id;
 		RespuestaWS respuesta = new RespuestaWS();
 		CatalogoComunidad catalogo = new CatalogoComunidad();
 		try{
 			jsonObject = connectWS.MisComunidades(finalURL);
 			if (jsonObject != null){
 				respuesta.setResultado(jsonObject.getBoolean("resultado"));
 				respuesta.setMensaje(jsonObject.getString("mensaje"));
 				if (jsonObject.has("comunidad")){
 					jsonArray = jsonObject.getJSONArray("comunidad");
 					int tamano = jsonArray.length();
 					DetalleComunidad[] comunidad = new DetalleComunidad[tamano];
 					for (int i = 0; i < tamano; i++){
 						JSONObject json = jsonArray.getJSONObject(i);
 						DetalleComunidad com = new DetalleComunidad();
 						com.setId(json.getString("id"));
 						com.setNombre(json.getString("nombre"));
 						comunidad[i] = com;
 					}
 					catalogo.setRespuestaWS(respuesta);
 					catalogo.setComunidad(comunidad);
 				}
 				return catalogo;
 			}
 		}catch (Exception exception){
 			respuesta.setResultado(false);
 			respuesta.setMensaje("Problemas al cargar las comunidades, intente nuevamente");
 			catalogo.setRespuestaWS(respuesta);
 			return catalogo;
 		}
 		return null;
 	}
 	
 	public CatalogoComunidad CargarComunidadesFilter(String id) {
 		Log.e("TT", "buscando anotaciones, idusuario = " + id);
 		JSONObject jsonObject = null;
 		JSONArray jsonArray = null;
 		String finalURL = WS_MISCOMUNIDADES + id;
 		RespuestaWS respuesta = new RespuestaWS();
 		CatalogoComunidad catalogo = new CatalogoComunidad();
 		try{
 			jsonObject = connectWS.MisComunidades(finalURL);
 			if (jsonObject != null){
 				respuesta.setResultado(jsonObject.getBoolean("resultado"));
 				respuesta.setMensaje(jsonObject.getString("mensaje"));
 				if (jsonObject.has("comunidad")){
 					jsonArray = jsonObject.getJSONArray("comunidad");
 					int tamano = jsonArray.length();
 					DetalleComunidad[] comunidad = new DetalleComunidad[tamano + 1];
 					for (int i = 0; i < tamano; i++){
 						JSONObject json = jsonArray.getJSONObject(i);
 						DetalleComunidad com = new DetalleComunidad();
 						com.setId(json.getString("id"));
 						com.setNombre(json.getString("nombre"));
 						comunidad[i] = com;
 					}
 					
 					if (tamano > 0){
 						DetalleComunidad com = new DetalleComunidad();
 						com.setId("100000");
 						com.setNombre("Todos");
 						comunidad[tamano] = com;
 				}
 				catalogo.setComunidad(comunidad);
 			}
 				catalogo.setRespuestaWS(respuesta);
 				return catalogo;
 			}
 		}catch (Exception exception){
 			respuesta.setResultado(false);
 			respuesta.setMensaje("No se pudieron cargar las comunidades, intente nuevamente");
 			catalogo.setRespuestaWS(respuesta);
 			return catalogo;
 		}
 		return null;
 	}
 	
 	public RespuestaWS CreaComunidad(String nombre, String descripcion, String tipo) {
 		JSONObject jsonObject = null;
 		String finalURL = WS_CREACOMUNIDAD + User.getUserId() + "&tipo_comunidad=" + tipo + "&publica=" + "1" +"&nombre=" + nombre + "&descripcion=" + descripcion;
 		RespuestaWS respuesta = new RespuestaWS();
 		try{
 			jsonObject = connectWS.CrearComunidad(finalURL);
 			if (jsonObject != null){
 				respuesta.setResultado(jsonObject.getBoolean("resultado"));
 				respuesta.setMensaje(jsonObject.getString("mensaje"));
 				return respuesta;
 			}else{
 				return respuesta;
 			}
 		}catch(Exception exception){
 			return null;
 		}
 	}
 	
 	public CatalogoTipoAnotacion BuscarTiposEventos(String id) {
 		CatalogoTipoAnotacion catalogo = new CatalogoTipoAnotacion();
 		JSONObject jsonObject = null;
 		String finalURL = WS_LISTATIPOANOTACION + id + "&activo=1";
 		RespuestaWS respuesta = new RespuestaWS();
 		try{
 			jsonObject = connectWS.ListaTipoEventos(finalURL);
 			if (jsonObject != null){
 				System.out.println("json != null");
 				respuesta.setResultado(jsonObject.getBoolean("resultado"));
 				respuesta.setMensaje(jsonObject.getString("mensaje"));
 				catalogo.setRespuestaWS(respuesta);
 				if(respuesta.isResultado()){
 					System.out.println("respuesta verdadera");
 					JSONArray jsonArray = jsonObject.getJSONArray("tipo_anotacion");
 					int tamano = jsonArray.length();
 					TipoAnotacion[] tipoAnotacion = new TipoAnotacion[tamano];
 					for (int i = 0; i < tamano; i++){
 						JSONObject jsonTemp = jsonArray.getJSONObject(i);
 						TipoAnotacion tipoTemp = new TipoAnotacion();
 						tipoTemp.setId(jsonTemp.getString("id"));
 						tipoTemp.setActivo(jsonTemp.getString("activo"));
 						tipoTemp.setNombre(jsonTemp.getString("nombre"));
 						tipoTemp.setComunidad(jsonTemp.getString("comunidad"));
 						tipoTemp.setIcono(jsonTemp.getString("icono"));
 						tipoTemp.setAdministrador(jsonTemp.getString("es_del_administrador"));
 						tipoTemp.setIncidente(jsonTemp.getString("es_incidente"));
 						tipoTemp.setDescripcion(jsonTemp.getString("descripcion"));
 						tipoTemp.setNombreComunidad(jsonTemp.getString("nombrecomunidad"));
 						tipoAnotacion[i] = tipoTemp;
 					}
 					catalogo.setTipoAnotacion(tipoAnotacion);
 					System.out.println("retorno catalogo tipos de anotacion");
 					return catalogo;
 				}
 				return catalogo;
 			}
 		}catch(Exception exception){
 			return null;
 		}
 		return null;
 	}
 	
 	public CatalogoMiembro CatalogoMiembro(String id) {
 		JSONObject jsonObject = null;
 		String finalURL = WS_CATALOGOMIEMBRO + id;
 		RespuestaWS respuesta = new RespuestaWS();
 		CatalogoMiembro catalogo = new CatalogoMiembro();
 		try{
 			jsonObject = connectWS.CatalogoMiembro(finalURL);
 			if (jsonObject != null){
 				System.out.println("CatalogoMiembro - != null");
 				respuesta.setResultado(jsonObject.getBoolean("resultado"));
 				respuesta.setMensaje(jsonObject.getString("mensaje"));
 				catalogo.setRespuestaWS(respuesta);
 				if(respuesta.isResultado()){
 					System.out.println("CatalogoMiembro - respuesta true");
 					JSONArray jsonArray = jsonObject.getJSONArray("miembro");
 					
 					int tamano = jsonArray.length();
 					Miembro[] miembro = new Miembro[tamano];
 					for (int i = 0; i < tamano; i++){
 						System.out.println("CatalogoMiembro - cargando miembro");
 						JSONObject mJson = jsonArray.getJSONObject(i);
 						Miembro miembroTemp = new Miembro();
 						miembroTemp.setId(mJson.getString("id"));
 						miembroTemp.setId_comunidad(mJson.getString("id_comunidad"));
 						miembroTemp.setId_usuario(mJson.getString("id_usuario"));
 						miembroTemp.setNombreUsuario(mJson.getString("nombreUsuario"));
 						miembroTemp.setTelefono(mJson.getString("telefonoUsuario"));
 						miembroTemp.setFecha_registro(mJson.getString("fecha_registro"));
 						miembroTemp.setNombreComunidad(mJson.getString("nombreComunidad"));
 						miembroTemp.setNombreUsuarioInvito(mJson.getString("nombreUsuarioInvito"));
 						miembroTemp.setEmail(mJson.getString("emailUsuario"));
 						miembroTemp.setAvatar(mJson.getString("avatar"));
 						miembro[i] = miembroTemp;
 						System.out.println("CatalogoMiembro - miembro cargado");
 					}
 					catalogo.setMiembro(miembro);
 					return catalogo;
 				}
 			}
 		}catch(Exception exception){
 			
 		}
 		return null;
 	}
 	
 	public RespuestaWS solicitarComunidad(String idComunidad){
 		JSONObject jsonObject = null;
 		String finalURL = WS_ENVIARSOLICITUD + idComunidad + "&usuario=" + User.getUserId();
 		RespuestaWS respuesta = new RespuestaWS();
 		try{
 			jsonObject = connectWS.solicitarComunidad(finalURL);
 			if (jsonObject != null){
 				respuesta.setResultado(jsonObject.getBoolean("resultado"));
 				respuesta.setMensaje(jsonObject.getString("mensaje"));
 				
 				return respuesta;
 			}
 		}catch(Exception exception){
 			return respuesta;
 		}
 		return null;
 	}
 	
 	public DetalleComunidad DetalleComunidad(String idComunidad) {
 		JSONObject jsonObject = null;
 		String finalURL = WS_DETALLECOMUNIDAD + idComunidad;
 		RespuestaWS respuesta = new RespuestaWS();
 		DetalleComunidad detalleComunidad = new DetalleComunidad();
 		try{
 			jsonObject = connectWS.DetalleComunidad(finalURL);
 			if (jsonObject != null){
 				respuesta.setResultado(jsonObject.getBoolean("resultado"));
 				respuesta.setMensaje(jsonObject.getString("mensaje"));
 				detalleComunidad.setRespuestaWS(respuesta);
 				if(respuesta.isResultado()){
 					detalleComunidad.setId(jsonObject.getString("id"));
 					detalleComunidad.setNombre(jsonObject.getString("nombre"));
 					detalleComunidad.setDescripcion(jsonObject.getString("descripcion"));
 					detalleComunidad.setActivo(jsonObject.getString("activo"));
 					try{
 					JSONArray usuarioCreo = new JSONArray();
 					usuarioCreo = jsonObject.getJSONArray("usuario_creo");
 					JSONObject usuarioCreoJSObject = usuarioCreo.getJSONObject(0);
 					detalleComunidad.setIdDuenno(usuarioCreoJSObject.getString("id"));
 					}catch(Exception e){
 						Log.v("pio", "No se pudo obtener el dato del dueo de la comunidad");
 					}
 					
 					return detalleComunidad;
 				}else{
 					return detalleComunidad;
 				}
 			}else{
 				return null;
 			}
 		}catch(Exception exception){
 			
 		}
 		return null;
 	}
 
 public void post(String url, List<NameValuePair> nameValuePairs) {
     HttpClient httpClient = new DefaultHttpClient();
     HttpContext localContext = new BasicHttpContext();
     HttpPost httpPost = new HttpPost(url);
 
     try {
         MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
         Log.e("LOG", "Prueba envio foto 1");
 
         for(int index=0; index < nameValuePairs.size(); index++) {
             if(nameValuePairs.get(index).getName().equalsIgnoreCase("Filedata")) {
             	Log.e("LOG", "Prueba envio foto 2");
                 // If the key equals to "image", we use FileBody to transfer the data
                 entity.addPart(nameValuePairs.get(index).getName(), new FileBody(new File (nameValuePairs.get(index).getValue())));
                 Log.e("LOG", "Prueba envio foto 2");
             } else {
                 // Normal string data
                 entity.addPart(nameValuePairs.get(index).getName(), new StringBody(nameValuePairs.get(index).getValue()));
                 Log.e("LOG", "Prueba envio foto 3");
             }
         }
 
         Log.e("LOG", "Prueba envio foto 4");
         httpPost.setEntity(entity);
 
         Log.e("LOG", "Prueba envio foto 5");
         HttpResponse response = httpClient.execute(httpPost, localContext);
         Log.e("LOG", "Prueba envio foto 6, response = " + response.getParams());
         Log.e("LOG", "Prueba envio foto 6, response = " + response.hashCode());
         Log.e("LOG", "Prueba envio foto 6, response = " + response.getStatusLine());
         
     } catch (IOException e) {
         e.printStackTrace();
     }
 }
 	
 	public RespuestaWS MandarEvento(String latitud, String longitud, String idUsuario, String comunidad, String tipoAnotacion, String descripcion, String imagen) {
 		Log.v("pio", "imagen = " + imagen);
 		final List<NameValuePair> nombresArchivos = new ArrayList<NameValuePair>(2);
 		nombresArchivos.add(new BasicNameValuePair("usuario", idUsuario));
 		nombresArchivos.add(new BasicNameValuePair("comunidad", comunidad));
 		nombresArchivos.add(new BasicNameValuePair("tipo_anotacion", tipoAnotacion));
 		nombresArchivos.add(new BasicNameValuePair("descripcion", descripcion));
 		nombresArchivos.add(new BasicNameValuePair("lat", latitud));
 		nombresArchivos.add(new BasicNameValuePair("lon", longitud));
 		nombresArchivos.add(new BasicNameValuePair("Filedata",Environment.getExternalStorageDirectory() + imagen) );
 		post("http://23.23.1.2/WS/ws_crear_anotacion.php?", nombresArchivos);
 		
 		
 		return null;
 		
 	}
 	
 	public void cambiarAvatar(String imagen) {
 		Log.v("pio", "imagen = " + imagen);
 		final List<NameValuePair> nombresArchivos = new ArrayList<NameValuePair>(2);
 		nombresArchivos.add(new BasicNameValuePair("id", User.getUserId()));
 		nombresArchivos.add(new BasicNameValuePair("Filedata",Environment.getExternalStorageDirectory() + imagen) );
 		post("http://23.23.1.2/WS/upload.avatar.php?", nombresArchivos);
 		
 	}
 	
 	public RespuestaWS enviarRespuestaInvitacion(String idInvitacion, String respuesta) {
 		JSONObject jsonObject = null;
 		String finalURL = WS_RESPUESTAINVITACON + idInvitacion + "&estado=" + respuesta; 
 		RespuestaWS res = new RespuestaWS();
 		try{
 			jsonObject = connectWS.enviarRespuestaInvitacion(finalURL);
 			if (jsonObject != null){
 				res.setResultado(jsonObject.getBoolean("resultado"));
 				res.setMensaje(jsonObject.getString("mensaje"));
 				return res;
 			}else{
 				return null;
 			}
 		}catch(Exception exception){
 			return null;
 		}
 	}
 	
 	public RespuestaWS CambiarClave(String claveactual, String clavenueva1) {
 		JSONObject jsonObject = null;
 		String finalURL = WS_CAMBIARCLAVE + User.getUserId() + "&clave=" + claveactual + "&nclave=" + clavenueva1;
 		String url = finalURL.replace(" ", "%20");
 		RespuestaWS respuesta = new RespuestaWS();
 		try{
 			jsonObject = connectWS.CambiarClave(url);
 			if (jsonObject != null){
 				respuesta.setResultado(jsonObject.getBoolean("resultado"));
 				respuesta.setMensaje(jsonObject.getString("mensaje"));
 				return respuesta;
 			}else{
 				return null;
 			}
 		}catch(Exception exception){
 			return null;
 		}
 	}
 	
 	public RespuestaWS editarPerfil(String nombre, String email, String usuario, String telefono) {
 		JSONObject jsonObject = null;
 //		http://174.129.97.85/WS/ws_update_usuario.php?action=modificar&id=2&nombre=kevin&email=kevintech@outlook.copm&telefono=56305815
 
 		String finalURL = WS_EDITARPERFIL + User.getUserId() + "&nombre=" + nombre + "&email=" + email + "&telefono=" + telefono;
 		String url = finalURL.replace(" ", "%20");
 		RespuestaWS respuesta = new RespuestaWS();
 		try{
 			jsonObject = connectWS.editarPerfil(url);
 			if (jsonObject != null){
 				respuesta.setResultado(jsonObject.getBoolean("resultado"));
 				respuesta.setMensaje(jsonObject.getString("mensaje"));
 				return respuesta;
 			}else{
 				return null;
 			}
 		}catch(Exception exception){
 			return null;
 		}
 	}
 	
 	public RespuestaWS editarComunidad(DetalleComunidad comunidad, String nombre, String descripcion, String espublica, String esreasignable) {
 		JSONObject jsonObject = null;
 //		ws_update_comunidad.php?id=idcomunidad&activo=[siempre es 1]&tipo_comunidad=1&usuario=[mi iddd usuario]&nombre=&descripcion&publica=1
 
		String finalURL = WS_EDITARCOMUNIDAD + comunidad.getId() + "&activo=1&tipo_comunidad=1&usuario=" + User.getUserId() + "&nombre=" + nombre + "&descripcion=" + descripcion + "&publica=" + espublica + "&reasignar=" + esreasignable ;
 		String url = finalURL.replace(" ", "%20");
 		RespuestaWS respuesta = new RespuestaWS();
 		try{
 			jsonObject = connectWS.editarPerfil(url);
 			if (jsonObject != null){
 				respuesta.setResultado(jsonObject.getBoolean("resultado"));
 				respuesta.setMensaje(jsonObject.getString("mensaje"));
 				return respuesta;
 			}else{
 				return null;
 			}
 		}catch(Exception exception){
 			return null;
 		}
 	}
 	
 	public RespuestaWS eliminaTipoEvento(String idUsuario, String idTipoAnotacion) {
 		JSONObject jsonObject = null;
 		String finalURL = WS_ELIMINATIPOANOTACION + idTipoAnotacion + "&usuario=" + idUsuario;
 		String url = finalURL.replace(" ", "%20");
 		RespuestaWS respuesta = new RespuestaWS();
 		try{
 			jsonObject = connectWS.eliminarTipoEvento(url);
 			if (jsonObject != null){
 				respuesta.setResultado(jsonObject.getBoolean("resultado"));
 				respuesta.setMensaje(jsonObject.getString("mensaje"));
 				return respuesta;
 			}else{
 				return null;
 			}
 		}catch(Exception exception){
 			return null;
 		}
 	}
 	
 	public RespuestaWS eliminaComunidad(String id, String userId) {
 		JSONObject jsonObject = null;
 		String finalURL = WS_ELIMINACOMUNIDAD + id + "&usuario=" + userId;
 		String url = finalURL.replace(" ", "%20");
 		RespuestaWS respuesta = new RespuestaWS();
 		try{
 			jsonObject = connectWS.eliminaComunidad(url);
 			if (jsonObject != null){
 				respuesta.setResultado(jsonObject.getBoolean("resultado"));
 				respuesta.setMensaje(jsonObject.getString("mensaje"));
 				return respuesta;
 			}
 		}catch(Exception exception){
 			return null;
 		}
 		return null;
 	}
 	
 	public RespuestaWS eliminarMiembro(Miembro miembro) {
 		JSONObject jsonObject = null;
 //		ws_sacarmiembro_comunidad.php?id=  USUARIOMIO&comunidad=IDCOMUNIDAD&idexpulsar=IDAEXPULSAR
 		String finalURL = WS_ELIMINAMIEMBRO + User.getUserId() + "&comunidad=" + miembro.getId_comunidad() + "&idexpulsar=" + miembro.getId();
 		String url = finalURL.replace(" ", "%20");
 		RespuestaWS respuesta = new RespuestaWS();
 		try{
 			jsonObject = connectWS.eliminaMiembro(url);
 			if (jsonObject != null){
 				respuesta.setResultado(jsonObject.getBoolean("resultado"));
 				respuesta.setMensaje(jsonObject.getString("mensaje"));
 				return respuesta;
 			}
 		}catch(Exception exception){
 			return null;
 		}
 		return null;
 	}
 	
 	public RespuestaWS eliminaEvento(String idAnotacion, String userId) {
 		JSONObject jsonObject = null;
 		String finalURL = WS_ELIMINAANOTACION + idAnotacion + "&usuario=" + userId;
 		String url = finalURL.replace(" ", "%20");
 		RespuestaWS respuesta = new RespuestaWS();
 		try{
 			jsonObject = connectWS.eliminaEvento(url);
 			if (jsonObject != null){
 				respuesta.setResultado(jsonObject.getBoolean("resultado"));
 				respuesta.setMensaje(jsonObject.getString("mensaje"));
 				return respuesta;
 			}
 		}catch(Exception exception){
 			return null;
 		}
 		return null;
 	}
 	
 	public RespuestaWS enviarInvitacion(String email, String idComunidad) {
 		RespuestaWS respuesta = new RespuestaWS();
 		String finalURL = WS_ENVIARINVITACION + User.getUserId() + "&comunidad=" + idComunidad + "&email=" + email;
 		JSONObject jsonObject = null;
 		try{
 			jsonObject = connectWS.enviarInvitacion(finalURL);
 			if (jsonObject != null){
 				respuesta.setResultado(jsonObject.getBoolean("resultado"));
 				respuesta.setMensaje(jsonObject.getString("mensaje"));
 			}else{
 				respuesta.setResultado(false);
 				respuesta.setMensaje("En este momento no se puede enviar la invitacion");
 			}
 			return respuesta;
 		}catch(Exception exception){
 			respuesta.setResultado(false);
 			respuesta.setMensaje("En este momento no se puede enviar la invitacion");
 			return respuesta;
 		}
 	}
 	
 	public RespuestaWS recuperaClave(String email) {
 		RespuestaWS respuesta = new RespuestaWS();
 		String finalURL = WS_RECUPERARCLAVE + email;
 		JSONObject jsonObject = null;
 		try{
 			jsonObject = connectWS.recuperarClave(finalURL);
 			if (jsonObject != null){
 				respuesta.setResultado((jsonObject.getInt("resultado") != 0));
 				respuesta.setMensaje(jsonObject.getString("mensaje"));
 				Log.e("piio", "resultado = " + respuesta.isResultado());
 			}else{
 				respuesta.setResultado(false);
 				respuesta.setMensaje("En este momento no se puede enviar la invitacion");
 			}
 			return respuesta;
 		}catch(Exception exception){
 			respuesta.setResultado(false);
 			respuesta.setMensaje("En este momento no se puede enviar la invitacion");
 			return respuesta;
 		}
 	}
 	
 	public CatalogoInvitacion buscarInvitaciones() {
 		CatalogoInvitacion invitaciones = new CatalogoInvitacion();
 		JSONObject jsonObject = null;
 		String finalURL = WS_VERINVITACIONES + User.getUserId();
 		RespuestaWS respuesta = new RespuestaWS();
 		try{
 			jsonObject = connectWS.buscarInvitaciones(finalURL);
 			if (jsonObject != null){
 				respuesta.setResultado(jsonObject.getBoolean("resultado"));
 				respuesta.setMensaje(jsonObject.getString("mensaje"));
 				invitaciones.setRespuestaWS(respuesta);
 				
 				JSONArray jsonArray = jsonObject.getJSONArray("invitacion_recibida");
 				
 				int tamano = jsonArray.length();
 				if (tamano > 0){
 					Invitacion[] invitacion = new Invitacion[tamano];
 					for (int i = 0; i < tamano; i++){
 						JSONObject temp = jsonArray.getJSONObject(i);
 						Invitacion invitacionTemp = new Invitacion();
 						invitacionTemp.setId(temp.getString("id"));
 						invitacionTemp.setFechaIntento(temp.getString("fecha_ultimo_intento"));
 						invitacionTemp.setFechaRegistro((temp.getString("fecha_registro")));
 						invitacionTemp.setIdUsuario((temp.getString("id_usuario")));
 						invitacionTemp.setIdComunicad((temp.getString("id_comunidad")));
 						invitacionTemp.setUsuarioInvito((temp.getString("usuario_invito")));
 						invitacionTemp.setCodigoInvitacion((temp.getString("codigo_invitacion")));
 						invitacionTemp.setEmail(temp.getString("email"));
 						invitacionTemp.setTelefono(temp.getString("telefono"));
 						invitacionTemp.setEstado(temp.getString("estado"));
 						invitacionTemp.setIntentos(temp.getString("intentos"));
 						invitacionTemp.setNombreUsuario(temp.getString("nombreUsuario"));
 						invitacionTemp.setNombreComunidad(temp.getString("nombreComunidad"));
 						invitacionTemp.setUsuarioInvita((temp.getString("usuarioInvita")));
 						invitacionTemp.setMiembros((temp.getString("miembros")));
 						invitacion[i] = invitacionTemp;
 						System.out.println("invitacion " + invitacionTemp.getId());
 					}
 					invitaciones.setInvitacion(invitacion);
 					System.out.println("invitacion asignada "+ invitaciones.getInvitacion()[0].getId());
 					
 				}
 				return invitaciones;
 			}else{
 				System.out.println("No se retornarn las invitaciones, json null");
 				return null;				
 			}
 		}catch(Exception exception){
 			return null;
 		}
 	}
 	
 	public CatalogoInvitacion buscaInvitacionesEnviadas() {
 		CatalogoInvitacion invitaciones = new CatalogoInvitacion();
 		JSONObject jsonObject = null;
 		String finalURL = WS_VERINVITACIONESENVIADAS + User.getUserId();
 		RespuestaWS respuesta = new RespuestaWS();
 		try{
 			jsonObject = connectWS.buscarInvitaciones(finalURL);
 			if (jsonObject != null){
 				respuesta.setResultado(jsonObject.getBoolean("resultado"));
 				respuesta.setMensaje(jsonObject.getString("mensaje"));
 				invitaciones.setRespuestaWS(respuesta);
 				
 				JSONArray jsonArray = jsonObject.getJSONArray("invitacion_enviada");
 				
 				int tamano = jsonArray.length();
 				if (tamano > 0){
 					Invitacion[] invitacion = new Invitacion[tamano];
 					for (int i = 0; i < tamano; i++){
 						JSONObject temp = jsonArray.getJSONObject(i);
 						Invitacion invitacionTemp = new Invitacion();
 						invitacionTemp.setId(temp.getString("id"));
 						invitacionTemp.setFechaIntento(temp.getString("fecha_ultimo_intento"));
 						invitacionTemp.setFechaRegistro((temp.getString("fecha_registro")));
 						invitacionTemp.setIdUsuario((temp.getString("id_usuario")));
 						invitacionTemp.setIdComunicad((temp.getString("id_comunidad")));
 						invitacionTemp.setUsuarioInvito((temp.getString("usuario_invito")));
 						invitacionTemp.setCodigoInvitacion((temp.getString("codigo_invitacion")));
 						invitacionTemp.setEmail(temp.getString("email"));
 						invitacionTemp.setTelefono(temp.getString("telefono"));
 						invitacionTemp.setEstado(temp.getString("estado"));
 						invitacionTemp.setIntentos(temp.getString("intentos"));
 						invitacionTemp.setNombreUsuario(temp.getString("nombreUsuario"));
 						invitacionTemp.setNombreComunidad(temp.getString("nombreComunidad"));
 						invitacionTemp.setUsuarioInvita((temp.getString("usuarioInvita")));
 						invitacionTemp.setMiembros((temp.getString("miembros")));
 						invitacion[i] = invitacionTemp;
 					}
 					invitaciones.setInvitacion(invitacion);
 					
 				}
 				return invitaciones;
 			}else{
 				return null;
 			}
 		}catch(Exception exception){
 			return null;
 		}
 	}
 	
 	
 	public CatalogoSolicitud buscarSolicitudesRecibidas() {
 		CatalogoSolicitud solicitudes = new CatalogoSolicitud();
 		JSONObject jsonObject = null;
 		String finalURL = WS_VERSOLICITUDESRECIBIDAS + User.getUserId();
 		RespuestaWS respuesta = new RespuestaWS();
 		try{
 			jsonObject = connectWS.buscarInvitaciones(finalURL);
 			if (jsonObject != null){
 				System.out.println("ENCONTR EL JSON DE INVITACIONES RECIBIDAS");
 				
 				respuesta.setResultado(jsonObject.getBoolean("resultado"));
 				respuesta.setMensaje(jsonObject.getString("mensaje"));
 				solicitudes.setRespuestaWS(respuesta);
 				
 				JSONArray jsonArray = jsonObject.getJSONArray("solicitud_recibida");
 				
 				int tamano = jsonArray.length();
 				System.out.println(jsonArray.length());
 				if (tamano > 0){
 					System.out.println("ENTRA AL IF PORQUE EL TAMAO DEL ARRAY ES MAYOR");
 					Solicitud[] solicitud  = new Solicitud[tamano];
 					for (int i = 0; i < tamano; i++){
 						JSONObject temp = jsonArray.getJSONObject(i);
 						Solicitud solicitudTemp = new Solicitud();
 						solicitudTemp.setId(temp.getString("id"));
 						solicitudTemp.setFechaEnviada(temp.getString("fecha_enviada"));	
 						solicitudTemp.setEstado(temp.getString("estado"));			
 						solicitudTemp.setNombreUsuario(temp.getString("nombreUsuario"));			
 						solicitudTemp.setNombreComunidad(temp.getString("nombreComunidad"));
 						solicitud[i] = solicitudTemp;
 						
 						System.out.println("SOLICITUD RECIBIDA........SOLICITUD RECIBIDA");
 						System.out.println(solicitud[i].getNombreUsuario());
 					}
 					solicitudes.setSolicitud(solicitud);
 					
 				}
 				return solicitudes;
 			}else{
 				return null;
 			}
 		}catch(Exception exception){
 			return null;
 		}
 	}
 	
 	public CatalogoSolicitud buscarSolicitudesEnviadas() {
 		CatalogoSolicitud solicitudes = new CatalogoSolicitud();
 		JSONObject jsonObject = null;
 		String finalURL = WS_VERSOLICITUDESENVIADAS + User.getUserId();
 		RespuestaWS respuesta = new RespuestaWS();
 		try{
 			jsonObject = connectWS.buscarInvitaciones(finalURL);
 			if (jsonObject != null){
 				System.out.println("ENCONTR EL JSON DE INVITACIONES ENVIADAS");
 				
 				respuesta.setResultado(jsonObject.getBoolean("resultado"));
 				respuesta.setMensaje(jsonObject.getString("mensaje"));
 				solicitudes.setRespuestaWS(respuesta);
 				
 				JSONArray jsonArray = jsonObject.getJSONArray("solicitud_enviada");
 				
 				int tamano = jsonArray.length();
 				System.out.println(jsonArray.length());
 				if (tamano > 0){
 					System.out.println("ENTRA AL IF PORQUE EL TAMAO DEL ARRAY ES MAYOR");
 					Solicitud[] solicitud  = new Solicitud[tamano];
 					for (int i = 0; i < tamano; i++){
 						JSONObject temp = jsonArray.getJSONObject(i);
 						Solicitud solicitudTemp = new Solicitud();
 						solicitudTemp.setId(temp.getString("id"));
 						System.out.println("Id de la solicitud " + temp.getString("id"));
 						solicitudTemp.setFechaEnviada(temp.getString("fecha_enviada"));
 						System.out.println("1");
 						solicitudTemp.setIdUsuario(temp.getString("id_usuario"));
 						System.out.println("1");
 						solicitudTemp.setIdComunidad(temp.getString("id_comunidad"));
 						System.out.println("1");
 						solicitudTemp.setEstado(temp.getString("estado"));
 						System.out.println("1");
 						solicitudTemp.setNombreUsuario(temp.getString("nombreUsuario"));
 						System.out.println("1");
 						solicitudTemp.setNombreComunidad(temp.getString("nombreComunidad"));
 						System.out.println("1");
 						solicitudTemp.setUsuarioCreo(temp.getString("usuarioCreo"));
 						System.out.println("1");
 						solicitudTemp.setMiembros(temp.getString("miembros"));
 						
 						System.out.println("asigno los parametros");
 						solicitud[i] = solicitudTemp;
 						
 						System.out.println("SOLICITUD RECIBIDA........SOLICITUD RECIBIDA");
 						System.out.println(solicitud[i].getNombreUsuario());
 					}
 					solicitudes.setSolicitud(solicitud);
 					
 				}
 				return solicitudes;
 			}else{
 				return null;
 			}
 		}catch(Exception exception){
 			return null;
 		}
 	}
 	
 	
 	public Usuario CargarPerfil(String userId) {
 		System.out.println(userId);
 		JSONObject jsonObject = null;
 		String finalURL = WS_DATOSUSUARIO + userId;
 		RespuestaWS respuesta = new RespuestaWS();
 		Usuario usuario = new Usuario();
 		try{
 			jsonObject = connectWS.DatosUsuario(finalURL);
 			if(jsonObject != null){
 				respuesta.setResultado(jsonObject.getBoolean("resultado"));
 				respuesta.setMensaje(jsonObject.getString("mensaje"));
 				usuario.setRespuestaWS(respuesta);
 				if (respuesta.isResultado()){
 					System.out.println("CargarPerfil - correcto");
 					usuario.setNombre(jsonObject.getString("nombre"));
 					usuario.setEmail(jsonObject.getString("email"));
 					usuario.setTelefono(jsonObject.getString("telefono"));
 					usuario.setFechaRegistro(jsonObject.getString("fecha_registro"));
 					
 					
 					return usuario;
 				}else{
 					return usuario;
 				}
 			}else{
 				return null;
 			}
 		}catch (Exception exception){
 			return null;
 		}
 	}
 	
 	public CatalogoComunidad CargarComunidad(String idUsuario) {
 		CatalogoComunidad catalogo = new CatalogoComunidad();
 		RespuestaWS respuesta = new RespuestaWS();
 		JSONObject jsonObject = null;
 		String finalURL = WS_COMUNIDADES + idUsuario;
 		try{
 			jsonObject = connectWS.CatalogoComunidad(finalURL);
 			if (jsonObject != null){
 				System.out.println("json != null");
 				respuesta.setResultado(jsonObject.getBoolean("resultado"));
 				respuesta.setMensaje(jsonObject.getString("mensaje"));
 				Log.d("WA", "mensaje capturado");
 				catalogo.setRespuestaWS((RespuestaWS)respuesta);
 				Log.d("WA", "respuesta en catalogo");
 				System.out.println("RequestWS" + catalogo.getRespuestaWS().getMensaje());
 				JSONArray comunidades = jsonObject.getJSONArray("comunidades");
 				DetalleComunidad[] comunidad = new DetalleComunidad[comunidades.length()]; 
 				for (int i = 0; i < comunidades.length(); i++){
 					JSONObject jsonTemp = comunidades.getJSONObject(i);
 					DetalleComunidad com = new DetalleComunidad();
 					com.setId(jsonTemp.getString("id"));
 					com.setNombre(jsonTemp.getString("nombre"));
 					comunidad[i] = com;
 				}
 				catalogo.setComunidad(comunidad);
 				return catalogo;
 			}else{
 				respuesta.setResultado(false);
 				respuesta.setMensaje("Error de conexin");
 				catalogo.setRespuestaWS(respuesta);
 				return catalogo;
 			}
 		}catch(Exception exception){
 			respuesta.setResultado(false);
 			respuesta.setMensaje("Error de conexin");
 			catalogo.setRespuestaWS(respuesta);
 			System.out.println(exception);
 			return catalogo;
 		}
 	}
 
 	public CatalogoAnotacion CargarAnotaciones() {
 		CatalogoAnotacion catalogo = new CatalogoAnotacion();
 		RespuestaWS respuesta = new RespuestaWS();
 		JSONObject jsonObject = null;
 			String finalURL = WS_ANOTACIONES + User.getUserId();
 			Log.e("TT", "anotaciones = " + finalURL);
 			try{
 				jsonObject = connectWS.CatalogoAnotacion(finalURL);
 				if (jsonObject != null){
 					respuesta.setResultado(jsonObject.getBoolean("resultado"));
 					respuesta.setMensaje(jsonObject.getString("mensaje"));
 					catalogo.setRespuesta((RespuestaWS)respuesta);
 					if (respuesta.isResultado()){
 						JSONArray anotacionesa = jsonObject.getJSONArray("anotacion");
 						Anotacion[] anotacion = new Anotacion[anotacionesa.length()];
 						for (int i = 0; i < anotacionesa.length(); i++){
 							JSONObject jsonTemp = anotacionesa.getJSONObject(i);
 							Anotacion anota = new Anotacion();
 							anota.setIdAnotacion(jsonTemp.getString("id"));
 							anota.setDescripcion(jsonTemp.getString("descripcion"));
 							anota.setNombreTipoAnotacion(jsonTemp.getString("nombreTipoAnotacion"));
 							anota.setLatitud(Float.parseFloat(jsonTemp.getString("latitud")));
 							anota.setLongitud(Float.parseFloat(jsonTemp.getString("longitud")));
 							anota.setNombreTipoAnotacion(jsonTemp.getString("nombreTipoAnotacion"));
 							anota.setFecha_registro(jsonTemp.getString("fecha_registro"));
 							anota.setActivo(Integer.parseInt(jsonTemp.getString("activo")));
 							anota.setNombreUsuario(jsonTemp.getString("nombreUsuario"));
 							anota.setIdcomunidad(jsonTemp.getString("id_comunidad"));
 							anota.setNombreUsuario(jsonTemp.getString("nombreUsuario"));
 							anota.setNombreComunidad(jsonTemp.getString("nombreComunidad"));
 							anota.setIcono(jsonTemp.getString("icono"));
 							anota.setImagen(jsonTemp.getString("archivo"));
 							anotacion[i] = anota;
 						}
 						catalogo.setAnotacion(anotacion);
 					}
 					return catalogo;
 				}else{
 					respuesta.setResultado(false);
 					respuesta.setMensaje("no se puedieron cargar las anotaciones");
 					catalogo.setRespuesta(respuesta);
 					return catalogo;
 				}
 			}catch(Exception exception){
 				respuesta.setResultado(false);
 				respuesta.setMensaje("Ocurrio un error en la carga de anotaciones");
 				catalogo.setRespuesta(respuesta);
 				return catalogo;
 			}
 	}
 	
 	public CatalogoAnotacion CargarAnotacionesComunidad(String idComunidad) {
 		CatalogoAnotacion catalogo = new CatalogoAnotacion();
 		RespuestaWS respuesta = new RespuestaWS();
 		JSONObject jsonObject = null;
 			String finalURL = WS_ANOTACIONES + User.getUserId() + "&comunidad=" + idComunidad;
 			Log.e("TT", "anotaciones = " + finalURL);
 			try{
 				jsonObject = connectWS.CatalogoAnotacion(finalURL);
 				if (jsonObject != null){
 					respuesta.setResultado(jsonObject.getBoolean("resultado"));
 					respuesta.setMensaje(jsonObject.getString("mensaje"));
 					catalogo.setRespuesta((RespuestaWS)respuesta);
 					if (respuesta.isResultado()){
 						JSONArray anotacionesa = jsonObject.getJSONArray("anotacion");
 						Anotacion[] anotacion = new Anotacion[anotacionesa.length()];
 						for (int i = 0; i < anotacionesa.length(); i++){
 							JSONObject jsonTemp = anotacionesa.getJSONObject(i);
 							Anotacion anota = new Anotacion();
 							anota.setIdAnotacion(jsonTemp.getString("id"));
 							anota.setDescripcion(jsonTemp.getString("descripcion"));
 							anota.setNombreTipoAnotacion(jsonTemp.getString("nombreTipoAnotacion"));
 							anota.setLatitud(Float.parseFloat(jsonTemp.getString("latitud")));
 							anota.setLongitud(Float.parseFloat(jsonTemp.getString("longitud")));
 							anota.setNombreTipoAnotacion(jsonTemp.getString("nombreTipoAnotacion"));
 							anota.setFecha_registro(jsonTemp.getString("fecha_registro"));
 							anota.setActivo(Integer.parseInt(jsonTemp.getString("activo")));
 							anota.setNombreUsuario(jsonTemp.getString("nombreUsuario"));
 							anota.setIdcomunidad(jsonTemp.getString("id_comunidad"));
 							anota.setNombreUsuario(jsonTemp.getString("nombreUsuario"));
 							anota.setNombreComunidad(jsonTemp.getString("nombreComunidad"));
 							anota.setIcono(jsonTemp.getString("icono"));
 							anota.setImagen(jsonTemp.getString("archivo"));
 							anotacion[i] = anota;
 						}
 						catalogo.setAnotacion(anotacion);
 					}
 					return catalogo;
 				}else{
 					respuesta.setResultado(false);
 					respuesta.setMensaje("no se puedieron cargar las anotaciones");
 					catalogo.setRespuesta(respuesta);
 					return catalogo;
 				}
 			}catch(Exception exception){
 				respuesta.setResultado(false);
 				respuesta.setMensaje("Ocurrio un error en la carga de anotaciones");
 				catalogo.setRespuesta(respuesta);
 				return catalogo;
 			}
 	}
 
 	
 
 }
