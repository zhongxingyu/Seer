 package com.udb.mad.shinmen.benja.guana.anuncios.adapters;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.graphics.Color;
 import android.view.View;
 import android.widget.ListView;
 
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.model.Circle;
 import com.google.android.gms.maps.model.CircleOptions;
 import com.google.android.gms.maps.model.LatLng;
 import com.udb.mad.shinmen.benja.guana.anuncios.AnunciosCercanosActivity;
 import com.udb.mad.shinmen.benja.guana.anuncios.R;
 import com.udb.mad.shinmen.benja.guana.anuncios.utilidades.JSONDownloaderTask;
 
 public class GestionAnunciosImpl implements GestionAnuncios,
 		JSONDownloaderTask.OnFinishDownload<JSONArray> {
 
 	public static final String LATITUD = "latitud";
 	public static final String ALTITUD = "altitud";
 	public static final String TOKEN = "token";
 	public static final String USUARIO = "usuario";
 	public static final String TITULO = "titulo";
 	public static final String DESCRIPCION = "descripcion";
 	public static final String CODIGO = "codigo";
 	public static final String ID = "id";
 
 	private JSONDownloaderTask<JSONArray> jsonTask;
 	private AnunciosCercanosActivity activity;
 	private AnunciosCercanosCustomAdapter adapter;
 	private ListView listview;
 	private GoogleMap mMap;
 	public boolean notifyDataSetChanged = false;
 
 	ArrayList<HashMap<String, Object>> anunciosList;
 
 	@Override
 	public ArrayList<HashMap<String, Object>> obtenerAnunciosCercanos(
 			Double latitud, Double altitud, String usuario, String token,
 			String url, AnunciosCercanosActivity activity, ListView listview,
 			GoogleMap mMap) {
 
 		this.activity = activity;
 		this.listview = listview;
 		this.mMap = mMap;
 
 		/* Lista de parametros a enviar en el POST */
 		List<NameValuePair> parametros = new ArrayList<NameValuePair>(1);
 		parametros.add(new BasicNameValuePair(USUARIO, usuario));
 		parametros.add(new BasicNameValuePair(TOKEN, token));
 		parametros.add(new BasicNameValuePair(LATITUD, latitud.toString()));
 		parametros.add(new BasicNameValuePair(ALTITUD, altitud.toString()));
 
 		if(anunciosList == null || anunciosList.size()==0){
 			anunciosList = new ArrayList<HashMap<String, Object>>();
 		}
 
 		/* Tarea asincrona que recupera los datos */
 		jsonTask = new JSONDownloaderTask<JSONArray>(url,
 				JSONDownloaderTask.METODO_POST, parametros);
 		jsonTask.setOnFinishDownload(this);
 		jsonTask.setJsonArray(true);
 		jsonTask.execute();
 		
 		ArrayList<HashMap<String, Object>> anunciosList = new ArrayList<HashMap<String, Object>>();
 
 		/*
 		 * Dummy data* HashMap<String, Object> anuncio = new HashMap<String,
 		 * Object>(); anuncio.put("titulo", "Mouse inalambrico");
 		 * anuncio.put("alias", "malaCara1"); anuncio.put("descripcion",
 		 * "El veloz murcilago hind coma feliz cardillo y kiwi. La cigea tocaba el saxofn detrs del palenque de paja."
 		 * ); anuncio.put("imagen", ""); anuncio.put("latitud",
 		 * "13.690340434877442"); anuncio.put("longitud", "-89.22508481559748");
 		 * anunciosList.add(anuncio);
 		 * 
 		 * anuncio = new HashMap<String, Object>(); anuncio.put("titulo",
 		 * "Galaxy S5"); anuncio.put("alias", "ratero01");
 		 * anuncio.put("descripcion",
 		 * "El veloz murcilago hind coma feliz cardillo y kiwi. La cigea tocaba el saxofn detrs del palenque de paja."
 		 * ); anuncio.put("imagen", ""); anuncio.put("latitud",
 		 * "13.690903331255466"); anuncio.put("longitud", "-89.2275739055633");
 		 * anunciosList.add(anuncio);
 		 * 
 		 * anuncio = new HashMap<String, Object>(); anuncio.put("titulo",
 		 * "Adaptadores USB"); anuncio.put("alias", "timorato");
 		 * anuncio.put("descripcion",
 		 * "El veloz murcilago hind coma feliz cardillo y kiwi. La cigea tocaba el saxofn detrs del palenque de paja."
 		 * ); anuncio.put("imagen", ""); anuncio.put("latitud",
 		 * "13.687817808638007"); anuncio.put("longitud", "-89.22544959602351");
 		 * anunciosList.add(anuncio);
 		 * 
 		 * anuncio = new HashMap<String, Object>(); anuncio.put("titulo",
 		 * "Tarjetas de red"); anuncio.put("alias", "cachada");
 		 * anuncio.put("descripcion",
 		 * "El veloz murcilago hind coma feliz cardillo y kiwi. La cigea tocaba el saxofn detrs del palenque de paja."
 		 * ); anuncio.put("imagen", ""); anuncio.put("latitud",
 		 * "13.689172941798928"); anuncio.put("longitud", "-89.2257714611053");
 		 * anunciosList.add(anuncio);
 		 * 
 		 * anuncio = new HashMap<String, Object>(); anuncio.put("titulo",
 		 * "Monitor LED"); anuncio.put("alias", "descuentazo");
 		 * anuncio.put("descripcion",
 		 * "El veloz murcilago hind coma feliz cardillo y kiwi. La cigea tocaba el saxofn detrs del palenque de paja."
 		 * ); anuncio.put("imagen", ""); anuncio.put("latitud",
 		 * "13.684419517304311"); anuncio.put("longitud", "-89.22313216743464");
 		 * anunciosList.add(anuncio);
 		 */
 
 		return anunciosList;
 	}
 
 	@Override
 	public void onFinishDownload(JSONArray jsonArray) {
 
 		HashMap<String, Object> anuncio;
 
 		try {
 			/*
 			 * Obtenemos la lista de anuncios recibidos para pasarlo
 			 * al adapter
 			 * */
 			for (int i = 0; i < jsonArray.length(); i++) {
 
 				JSONObject jsonObject = jsonArray.getJSONObject(i);
 
 				anuncio = new HashMap<String, Object>();
 				anuncio.put(TITULO, jsonObject.getString(TITULO));
 				anuncio.put(DESCRIPCION, jsonObject.getString(DESCRIPCION));
 				anuncio.put(CODIGO, jsonObject.getString(ID));
 				anuncio.put(LATITUD, jsonObject.getString(LATITUD));
 				anuncio.put(ALTITUD, jsonObject.getString(ALTITUD));
 				
				String url = activity.findViewById(R.string.imagenAnuncioService).toString();
 		        url = url.replace("{id}", jsonObject.getString(ID));
 		        
 		        anuncio.put(ALTITUD, jsonObject.getString(ALTITUD));
 		        
 				anunciosList.add(anuncio);
 
 			}
 
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		if(!notifyDataSetChanged){
 			/*
 			 * Se pasa la lista de anuncios al adapter para que los cargue en el
 			 * listview
 			 * */
 			adapter = new AnunciosCercanosCustomAdapter(activity, anunciosList);
 			listview.setAdapter(adapter);
 		}else{
 			adapter.notifyDataSetChanged();
 		}
 		
 		activity.findViewById(R.id.progressBarAnuncios).setVisibility(View.GONE);
 		
 		/*
 		 * dibujamos los puntos en el mapa correspondientes a cada anuncio
 		 * */
 		dibujarPuntosAnuncios();
 		
 	}
 
 	private void dibujarPuntosAnuncios() {
 
 		if (anunciosList != null && anunciosList.size() > 0) {
 
 			mMap.clear();
 
 			for (HashMap<String, Object> anuncio : anunciosList) {
 
 				CircleOptions circleOptions = new CircleOptions();
 				Double lat = Double.valueOf(anuncio.get(LATITUD).toString());
 				Double lon = Double.valueOf(anuncio.get(ALTITUD).toString());
 				circleOptions.center(new LatLng(lat, lon));
 				circleOptions.fillColor(Color.RED);
 				circleOptions.radius(50);
 				circleOptions.strokeWidth(1);
 				@SuppressWarnings("unused")
 				Circle circle = mMap.addCircle(circleOptions);
 
 			}
 		}
 		
 		/*para indicar que ya se cargo la data en el listview*/
 		activity.flag_loading = false;
 	}
 
 	@Override
 	public void loadError() {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
