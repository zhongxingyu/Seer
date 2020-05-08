 package com.tactilapp.operadorapp;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.client.methods.HttpGet;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.http.AndroidHttpClient;
 import android.util.Log;
 import android.view.animation.AnimationUtils;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class Utils {
 
 	public static boolean hayConexionAInternet(final Context contexto) {
 		return hayConexionAInternet(contexto, R.string.sin_conexion_a_internet);
 	}
 
 	public static boolean hayConexionAInternet(final Context contexto,
 			final Integer mensajeDeError) {
 		Boolean hayConexion = true;
 		final ConnectivityManager conMgr = (ConnectivityManager) contexto
 				.getSystemService(Context.CONNECTIVITY_SERVICE);
 		final NetworkInfo i = conMgr.getActiveNetworkInfo();
 		if (i == null) {
 			hayConexion = false;
 		} else if (!i.isConnected()) {
 			hayConexion = false;
 		} else if (!i.isAvailable()) {
 			hayConexion = false;
 		}
 		if (!hayConexion && mensajeDeError != null) {
 			Toast.makeText(contexto, mensajeDeError, Toast.LENGTH_LONG).show();
 		}
 		return hayConexion;
 	}
 
 	public static boolean haMetidoAlgoValidoEnElCampo(final Context contexto,
 			final EditText campo, final Integer numeroMinimoDeCaracteres,
 			final int errorCampoCorto, final int errorCampoVacio) {
 		boolean haMetidoAlgoConMasCaracteresDelMinimo = false;
 
 		if (haRellenadoElCampo(campo)) {
 			final String cadenaIntroducida = campo.getText().toString();
 
 			haMetidoAlgoConMasCaracteresDelMinimo = haMetidoMasCaracteresDelMinimo(
					cadenaIntroducida, 8);
 
 			if (!haMetidoAlgoConMasCaracteresDelMinimo) {
 				campo.startAnimation(AnimationUtils.loadAnimation(contexto,
 						R.anim.shake));
 				Toast.makeText(contexto, errorCampoCorto, Toast.LENGTH_LONG)
 						.show();
 			}
 		} else {
 			campo.startAnimation(AnimationUtils.loadAnimation(contexto,
 					R.anim.shake));
 			Toast.makeText(contexto, errorCampoVacio, Toast.LENGTH_LONG).show();
 		}
 
 		return haMetidoAlgoConMasCaracteresDelMinimo;
 	}
 
 	public static boolean haRellenadoElCampo(final EditText campo) {
 		return campo != null && campo.getText() != null
 				&& !"".equals(campo.getText().toString());
 	}
 
 	public static boolean haMetidoMasCaracteresDelMinimo(final String cadena,
 			final Integer numeroMinimoDeCaracteres) {
 		return cadena != null
 				&& cadena.trim().length() > numeroMinimoDeCaracteres;
 	}
 
 	public static Bitmap descargarImagen(final String url) {
 		final AndroidHttpClient cliente = AndroidHttpClient
 				.newInstance("Android");
 		final HttpGet peticion = new HttpGet(url);
 
 		try {
 			final HttpResponse respuesta = cliente.execute(peticion);
 			final int codigoDeRespuesta = respuesta.getStatusLine()
 					.getStatusCode();
 			if (codigoDeRespuesta != HttpStatus.SC_OK) {
 				return null;
 			}
 
 			final HttpEntity entidad = respuesta.getEntity();
 			if (entidad != null) {
 				InputStream flujoDeEntrada = null;
 				try {
 					flujoDeEntrada = entidad.getContent();
 					final Bitmap imagen = BitmapFactory
 							.decodeStream(flujoDeEntrada);
 					return imagen;
 				} finally {
 					if (flujoDeEntrada != null) {
 						flujoDeEntrada.close();
 					}
 					entidad.consumeContent();
 				}
 			}
 		} catch (final Exception e) {
 			peticion.abort();
 		} finally {
 			if (cliente != null) {
 				cliente.close();
 			}
 		}
 		return null;
 	}
 
 	public static String obtenerElContenido(
 			final HttpEntity contenidoDeLaRespuesta) {
 		BufferedReader lector = null;
 		final StringBuilder contenido = new StringBuilder();
 
 		try {
 
 			lector = new BufferedReader(new InputStreamReader(
 					contenidoDeLaRespuesta.getContent()));
 			String linea;
 			while ((linea = lector.readLine()) != null) {
 				contenido.append(linea);
 			}
 
 		} catch (final IOException excepcion) {
 			Log.e("Utils",
 					"Error para obtener el contenido de la entidad de respuesta ",
 					excepcion);
 		} finally {
 			if (lector != null) {
 				try {
 					lector.close();
 				} catch (final IOException excepcion) {
 					excepcion.printStackTrace();
 				}
 			}
 		}
 		return contenido.toString();
 	}
 
 }
