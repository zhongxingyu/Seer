 package com.researchmobile.coretel.utility;
 
 import java.util.StringTokenizer;
 
 import alvarado.wuil.R;
 import android.content.Context;
 import android.graphics.drawable.Drawable;
 import android.util.Log;
 
 public class TokenizerUtility {
 	
 	private static int TITULO = 0;
 	private static int IDANOTACION = 1;
 	private static int IDCOMUNIDAD = 2;
 	private static int USUARIO_ANOTO = 3;
 	private static int TIPO_ANOTACION = 4;
 	private static int ICONO = 5;
 	
 	private static int DESCRIPCION = 0;
 	private static int FECHA_REGISTRO = 1;
 	private static int NOMBRE_USUARIO = 2;
 	private static int NOMBRE_COMUNIDAD = 3;
 	private static int IMAGEN = 4;
 
 	private static String SEPARADOR = "=+=";
 	
 	public String titulo(String anotacion){
 		return buscar(anotacion, TITULO);
 	}
 	
 	public String idAnotacion(String anotacion) {
 		return buscar(anotacion, IDANOTACION);
 	}
 	
 	public String idComunidad(String anotacion) {
 		return buscar(anotacion, IDCOMUNIDAD);
 	}
 	
 	public String usuarioAnoto(String anotacion){
 		return buscar(anotacion, USUARIO_ANOTO);
 	}
 	
 	public String tipoAnotacion(String anotacion){
 		return buscar(anotacion, TIPO_ANOTACION);
 	}
 	
 	public String icono(String anotacion){
 		return buscar(anotacion, ICONO);
 	}
 	
 	public String descripcion(String anotacion){
 		return buscar(anotacion, DESCRIPCION);
 	}
 	
 	public String fechaRegistro(String anotacion){
 		return buscar(anotacion, FECHA_REGISTRO);
 	}
 	
 	public String nombreUsuario(String anotacion){
 		return buscar(anotacion, NOMBRE_USUARIO);
 	}
 	
 	public String nombreComunidad(String anotacion){
 		return buscar(anotacion, NOMBRE_COMUNIDAD);
 	}
 	
 	public String imagen(String anotacion){
 		return buscar(anotacion, IMAGEN);
 	}
 	
 	public Drawable iconoResource(Context context, String titulo) {
 		String url = icono(titulo);
 		
 		if (url.equalsIgnoreCase("img/markers/2hand.png")){
 			return context.getResources().getDrawable(R.drawable.hand);
 		}
 		
 		if (url.equalsIgnoreCase("img/markers/360degrees.png")){
 			return context.getResources().getDrawable(R.drawable.degrees);
 		}
 		
 		if (url.equalsIgnoreCase("img/markers/abduction.png")){
 			return context.getResources().getDrawable(R.drawable.abduction);
 		}
 		
 		if (url.equalsIgnoreCase("img/markers/aboriginal.png")){
 			return context.getResources().getDrawable(R.drawable.aboriginal);
 		}
 		
 		if (url.equalsIgnoreCase("img/markers/accesdenied.png")){
 			return context.getResources().getDrawable(R.drawable.accesdenied);
 		}
 		
 		if (url.equalsIgnoreCase("img/markers/acupuncture.png")){
 			return context.getResources().getDrawable(R.drawable.acupuncture);
 		}
 		
 		if (url.equalsIgnoreCase("img/markers/administration.png")){
 			return context.getResources().getDrawable(R.drawable.administration);
 		}
 		
 		if (url.equalsIgnoreCase("img/markers/administrativeboundary.png")){
 			return context.getResources().getDrawable(R.drawable.administrativeboundary);
 		}
 		
 		if (url.equalsIgnoreCase("img/markers/aed-2.png")){
 			return context.getResources().getDrawable(R.drawable.aed);
 		}
 		
 		if (url.equalsIgnoreCase("img/markers/agritourism.png")){
 			return context.getResources().getDrawable(R.drawable.agritourism);
 		}
 		
 		if (url.equalsIgnoreCase("img/markers/air_fixwing.png")){
 			return context.getResources().getDrawable(R.drawable.air_fixwing);
 		}
 		
 		if (url.equalsIgnoreCase("img/markers/aircraftcarrier.png")){
 			return context.getResources().getDrawable(R.drawable.aircraftcarrier);
 		}
 		
 		if (url.equalsIgnoreCase("img/markers/aircraftsmall.png")){
 			return context.getResources().getDrawable(R.drawable.aircraftsmall);
 		}
 		
 		if (url.equalsIgnoreCase("img/markers/airport.png")){
 			return context.getResources().getDrawable(R.drawable.airport);
 		}
 		
 		if (url.equalsIgnoreCase("img/markers/airport_apron.png")){
 			return context.getResources().getDrawable(R.drawable.airport_apron);
 		}
 		
 		if (url.equalsIgnoreCase("img/markers/airport_runway.png")){
 			return context.getResources().getDrawable(R.drawable.airport_runway);
 		}
 		
 		if (url.equalsIgnoreCase("img/markers/airport_terminal.png")){
 			return context.getResources().getDrawable(R.drawable.airport_terminal);
 		}
 		
 		if (url.equalsIgnoreCase("img/markers/airshow-2.png")){
 			return context.getResources().getDrawable(R.drawable.airshow);
 		}
 		
 		if (url.equalsIgnoreCase("img/markers/algae.png")){
 			return context.getResources().getDrawable(R.drawable.algae);
 		}
 		
 		if (url.equalsIgnoreCase("img/markers/alien.png")){
 			return context.getResources().getDrawable(R.drawable.alien);
 		}
 		
 		if (url.equalsIgnoreCase("img/markers/alligator.png")){
 			return context.getResources().getDrawable(R.drawable.alligator);
 		}
 		
 		if (url.equalsIgnoreCase("img/markers/amphitheater.png")){
 			return context.getResources().getDrawable(R.drawable.amphitheat);
 		}
 		
 		if (url.equalsIgnoreCase("img/markers/amphitheater-2.png")){
 			return context.getResources().getDrawable(R.drawable.amphitheater);
 		}
 		
 		if (url.equalsIgnoreCase("img/markers/anchorpier.png")){
 			return context.getResources().getDrawable(R.drawable.anchorpier);
 		}
 		
 		if (url.equalsIgnoreCase("img/markers/anemometer_mono.png")){
 			return context.getResources().getDrawable(R.drawable.anemometer_mono);
 		}
 		
 		return context.getResources().getDrawable(R.drawable.algae);
 	}
 	
 	private String buscar(String anotacion, int posicion){
 		Log.e("TT", "tokenizer - string = " + anotacion + " posicion = " + posicion);
 		StringTokenizer tokenizer = new StringTokenizer(anotacion, SEPARADOR);
 		int i = 0;
 		Log.e("TT", "tamao tokenizer = " + tokenizer.countTokens());
 		if (tokenizer.countTokens() < 5){
 			return "";
 		}
 		String[] vector = new String[tokenizer.countTokens()];
 		while(tokenizer.hasMoreTokens()){
 			vector[i] = tokenizer.nextToken();
 			i++;
 		}
 		
 		return vector[posicion];
 	}
 
 }
