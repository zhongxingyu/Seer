 package com.audiolab.areago;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.location.Location;
 import android.location.LocationManager;
 import android.net.wifi.ScanResult;
 import android.os.Vibrator;
 import android.util.Log;
 import android.widget.ImageView;
 
 public class Paseo {
 	
 	private int id ;
 	private String titulo;
 	private String idioma;
 	private String descripcion;
 	private String excerpt;
 	private String imagen = "none";
 	private Bitmap img = null;
 	private int grabaciones;
 	private int tamano;
 	private SoundPoint pref = new SoundPoint("paseo_reference"); // Punto de referencia de inicio / lat-lon
 	private String hash = "";
 	private boolean update=false; // ya está descargado pero necesita ser actualizado porque el hash es diferente
 	private boolean downlad=false; // necesita ser descargado?
 	private String JsonPoints; // Listado de puntos del mapa en Json Array
 	private ArrayList<SoundPoint> puntos = new ArrayList<SoundPoint>();
 	private Vibrator vibrator;
 	
 	// Creadoras
 	public Paseo(int id) {
 		this.id = id;
 	}
 	
 	// Modificadoras
 	public void setTitle(String t) {
 		this.titulo = t;
 	}
 	
 	public void setDescription(String d) {
 		this.descripcion = d;
 	}
 	
 	public void setExcerpt(String e) {
 		this.excerpt = e;
 	}
 	
 	public void setVibrator(Vibrator v) {
 		this.vibrator = v;
 	}
 	
 	public void setIdioma(String lan) {
 		this.idioma=lan;
 	}
 	
 	public void setGrabaciones(int grabaciones) {
 		this.grabaciones = grabaciones;
 	}
 	
 	public void setHash(String hash) {
 		this.hash = hash;
 	}
 	
 	public void setUpdated() {
 		this.update=true;
 	}
 	
 	public void setOutdated() {
 		this.update=false;
 	}
 	
 	public void setDownloaded() {
 		this.downlad=true;
 	}
 	
 	public void setNotDownloaded() {
 		this.downlad=false;
 	}
 	
 	public void setPoints(String points) {
 		this.JsonPoints = points;
 	}
 	
 	public void setImage(String uri) {
 		this.imagen = uri;
 	}
 	
 	public void setBitmap(String file) {
 		//Crea un bitmap desde un path
 		File imgFile = new  File(file);
 		if(imgFile.exists()){
 		    this.img = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
 		} // Gestionar si no hay icono.jpg??
 	}
 	
 	public void setBitmap(Bitmap bitmap) {
 		this.img = bitmap;
 	}
 	
 	// Consultoras
 	
 	//TODO: Buscar gestión de la imagen para no hacer dos tipología distintas...
 	public Bitmap getBitmap() {
 		return this.img;
 	}
 	public String getImage() {
 		return this.imagen;
 	}
 	
 	public String getTitle() {
 		return this.titulo; 
 	}
 	
 	public int getId() {
 		return this.id;
 	}
 	
 	public String getHash() {
 		return this.hash;
 	}
 	
 	public String getPoints() {
 		return this.JsonPoints;
 	}
 	
 	public String getDescription() {
 		return this.descripcion;
 	}
 	
 	public String getExcerpt() {
 		return this.excerpt;
 	}
 	
 	public String getIdioma() {
 		return this.idioma;
 	}
 	
 	public boolean isUpdate() { //esta actualizado? false no está actualizado 
 		return this.update;
 	}
 	
 	public boolean isDownload() { // esta descargado?
 		return this.downlad;
 	}
 	
 	public boolean hasImage() {
 		if (this.imagen!="none") return true;
 		if (this.img!=null) return true;
 		else { return false; }
 	}
 	
	public String getPathImage() {
		return "/sdcard/Areago/"+this.id+"/icono.jpg";
	}
	
 	// Acciones
 	public void stop() {
 		for (int i = 0; i<this.puntos.size(); i++) {
 			this.puntos.get(i).stopSoundFile();
 		}
 	}
 	
 	public void create_points(String str) {
 		// Crear los puntos a partir del Objeto Points del GeoJSON
 		//
 		
 		JSONObject points;
 		try {
 			
 			points = new JSONObject(str);
 			//String type = points.getString("type"); no lo uso
 			JSONArray features = points.getJSONArray("features"); // aquí van los puntos
 			
 		
 			for (int i = 0; i<features.length();i++) {
 				// Cargamos cada uno de los obj con los puntos
 				JSONObject jO = features.getJSONObject(i);
 				JSONObject properties = jO.getJSONObject("properties");
 				JSONObject geometry = jO.getJSONObject("geometry");
 				//JSONObject geo_prop = geometry.getJSONObject("properties"); // No usamos para nada??
 				JSONArray geo_coord = geometry.getJSONArray("coordinates");
 
 				//Paseo walk = new Paseo(jObject.getInt("id"),jObject.getString("name"),jObject.getString("description"));
 				// Me esto inventado el provider...
 				SoundPoint p = new SoundPoint(LocationManager.GPS_PROVIDER);
 				if (!geo_coord.isNull(1)) p.setLatitude(geo_coord.getDouble(1)); // TODO: No estoy seguro si el primer valor es LAT o LON...
 				if (!geo_coord.isNull(0)) p.setLongitude(geo_coord.getDouble(0));
 				if (properties.has("radius")) p.setRadius((float) geometry.getDouble("radius"));
 				if (properties.has("file")) p.setSoundFile(properties.getString("file"));
 				if (properties.has("type")) p.setType(properties.getInt("type")); // Dentro de properties
 				if (properties.has("essid")) p.setEssid(properties.getString("essid")); // TODO: Será obligatorio tener ESSID?? aunque sea ""
 				if (properties.has("autofade")) {p.setAutofade(properties.getBoolean("autofade"));} else {p.setAutofade(true);} // por defecto le dejo activado el autofade.. 
 				// TODO: Hacer una variable global en sharedPreferences para guarda el lugar de descarga..
 				p.setFolder("/mnt/sdcard/Areago/"+this.getId()); // En JSON no nos define el ID del paseo? si
 				this.puntos.add(p);
 			} 
 		
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			//e.printStackTrace();
 			Log.d("AREAGO","Error al cargar los puntos del paseo: "+this.getTitle()+" @ "+str);
 		}
 	}
 	
 	public void addRefPoint(double lat, double lon) {
 		this.pref.setLatitude(lat);
 		this.pref.setLongitude(lon);
 	}
 	
 	public void check_collisions(Location l) {
 		// Recorre los puntos del mapa y revisa si estamos dentro del radio de uno de ellos
 		String p = "";
 		for (int i = 0; i<this.puntos.size(); i++){
 			if (this.puntos.get(i).getType() != SoundPoint.TYPE_WIFI_PLAY_LOOP) this.puntos.get(i).checkColision(l);
 			p = p + " | "+this.puntos.get(i).getFolder()+"/"+this.puntos.get(i).getSoundFile();
 		}
 		Log.d("AREAGO",p);
 	}
 	
 	public void check_collisions(List<ScanResult> wifis) {
 		for (int i=0; i<this.puntos.size(); i++) {
 			if (this.puntos.get(i).getType()==SoundPoint.TYPE_WIFI_PLAY_LOOP) this.puntos.get(i).checkColision(wifis);
 			//Log.d("AREAGO","ID: "+this.puntos.get(i).getId()+"tipo:"+this.puntos.get(i).getType());
 			
 		}
 	}
 	
 	public boolean exist(HashMap<Integer,Paseo> walks) {
 		if (walks.get(this.id) != null) { // ya está mapeado un paseo con ese ID
 			Paseo p = (Paseo) walks.get(this.id);
 			if (this.getHash() != "") { // si tiene hash...
 					if (!p.getHash().equals(this.getHash())) { // se debe actualizar
 						p.setOutdated(); // marcamos como no actualizado
 						walks.put(this.getId(), p); // actualizamos el paseo en el hash
 					}
 					
 				}
 			return true;
 		} else { return false; }
 	}
 	
 }
