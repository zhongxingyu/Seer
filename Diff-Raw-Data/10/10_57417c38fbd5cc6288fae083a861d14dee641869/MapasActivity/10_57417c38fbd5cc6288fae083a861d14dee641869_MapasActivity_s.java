 /**
  *  TiempoBus - Informacion sobre tiempos de paso de autobuses en Alicante
  *  Copyright (C) 2012 Alberto Montiel
  * 
  *  
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package alberapps.android.tiempobus.mapas;
 
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import alberapps.android.tiempobus.MainActivity;
 import alberapps.android.tiempobus.R;
 import alberapps.android.tiempobus.actionbar.ActionBarMapaActivity;
 import alberapps.android.tiempobus.data.BusAdapter;
 import alberapps.android.tiempobus.infolineas.InfoLineasTabsPager;
 import alberapps.android.tiempobus.tasks.LoadDatosLineasAsyncTask;
 import alberapps.android.tiempobus.tasks.LoadDatosLineasAsyncTask.LoadDatosLineasAsyncTaskResponder;
 import alberapps.android.tiempobus.tasks.LoadDatosMapaAsyncTask;
 import alberapps.android.tiempobus.tasks.LoadDatosMapaAsyncTask.LoadDatosMapaAsyncTaskResponder;
 import alberapps.android.tiempobus.tasks.LoadVehiculosMapaAsyncTask;
 import alberapps.android.tiempobus.tasks.LoadVehiculosMapaAsyncTask.LoadVehiculosMapaAsyncTaskResponder;
 import alberapps.java.tam.BusLinea;
 import alberapps.java.tam.UtilidadesTAM;
 import alberapps.java.tam.mapas.DatosMapa;
 import alberapps.java.tam.mapas.DatosRuta;
 import alberapps.java.tam.mapas.PlaceMark;
 import alberapps.java.tram.UtilidadesTRAM;
 import alberapps.java.util.Utilidades;
 import android.app.ActionBar;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Resources;
 import android.graphics.Color;
 import android.graphics.drawable.Drawable;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 import android.os.AsyncTask.Status;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Handler;
 import android.preference.PreferenceManager;
 import android.provider.Settings;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.Spinner;
 import android.widget.Toast;
 import android.widget.ToggleButton;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.MyLocationOverlay;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.OverlayItem;
 
 /**
  * Actividad para mapas
  * 
  * 
  */
 public class MapasActivity extends ActionBarMapaActivity {
 
 	protected static final int SUB_ACTIVITY_REQUEST_LINEAS = 1000;
 	public static final int SUB_ACTIVITY_RESULT_OK = 1002;
 
 	String distancia = ParadasCercanas.DISTACIA_CERCANA;
 
 	LinearLayout linearLayout;
 	MapView mapView;
 
 	List<Overlay> mapOverlays;
 	Drawable drawableIda;
 	Drawable drawableVuelta;
 	Drawable drawableMedio;
 	Drawable drawableVehiculo;
 	MapasItemizedOverlay itemizedOverlayIda;
 	MapasItemizedOverlay itemizedOverlayVuelta;
 	MapasItemizedOverlay itemizedOverlayMedio;
 	VehiculosItemizedOverlay itemizedOverlayVehiculos;
 
 	DatosMapa datosMapaCargadosIda;
 	DatosMapa datosMapaCargadosVuelta;
 
 	DatosMapa datosMapaCargadosIdaAux;
 	DatosMapa datosMapaCargadosVueltaAux;
 
 	String paradaSeleccionada;
 
 	String lineaSeleccionada;
 	String lineaSeleccionadaDesc;
 	String lineaSeleccionadaNum;
 
 	boolean primeraCarga = true;
 
 	MyLocationOverlay mMyLocationOverlay;
 
 	private ProgressDialog dialog;
 
 	SharedPreferences preferencias = null;
 
 	int modoRed = InfoLineasTabsPager.MODO_RED_SUBUS_ONLINE;
 
 	MapasOffline mapasOffline;
 
 	ParadasCercanas paradasCercanas;
 
 	AsyncTask<String, Void, DatosMapa> taskDatosMapa = null;
 	AsyncTask<String, Void, DatosMapa> taskDatosMapaVuelta = null;
 	AsyncTask<String, Void, ArrayList<BusLinea>> taskBuses = null;
 	AsyncTask<String, Void, DatosMapa> taskVehiculosMapa = null;
 
 	Timer timer = null;
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		return false;
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.mapas);
 
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 			ActionBar actionBar = getActionBar();
 			actionBar.setDisplayHomeAsUpEnabled(true);
 		}
 
 		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
 		preferencias = PreferenceManager.getDefaultSharedPreferences(this);
 
 		mapasOffline = new MapasOffline(this, preferencias);
 		paradasCercanas = new ParadasCercanas(this, preferencias);
 
 		// Control de modo de red
 		modoRed = this.getIntent().getIntExtra("MODO_RED", 0);
 
 		if (this.getIntent().getExtras() == null || (this.getIntent().getExtras() != null && !this.getIntent().getExtras().containsKey("MODO_RED"))) {
 
 			modoRed = preferencias.getInt("infolinea_modo", 0);
 
 		}
 
 		primeraCarga = true;
 
 		mapView = (MapView) findViewById(R.id.mapview);
 
 		mapView.setBuiltInZoomControls(true);
 
 		mMyLocationOverlay = new MyLocationOverlay(this, mapView);
 
 		MapController mapController = mapView.getController();
 
 		GeoPoint point = null;
 
 		point = new GeoPoint(38337176, -491892);
 
 		mapController.animateTo(point);
 		mapController.setZoom(10);
 
 		// Si viene de la seleccion de la lista
 		if (this.getIntent().getExtras() != null && this.getIntent().getExtras().containsKey("LINEA_MAPA")) {
 
 			int lineaPos = UtilidadesTAM.getIdLinea(this.getIntent().getExtras().getString("LINEA_MAPA"));
 
 			Log.d("mapas", "linea: " + lineaPos + "l: " + this.getIntent().getExtras().getString("LINEA_MAPA"));
 
 			if (lineaPos > -1) {
 
 				lineaSeleccionada = UtilidadesTAM.LINEAS_CODIGO_KML[lineaPos];
 				lineaSeleccionadaDesc = UtilidadesTAM.LINEAS_DESCRIPCION[lineaPos];
 
 				lineaSeleccionadaNum = UtilidadesTAM.LINEAS_NUM[lineaPos];
 
 				dialog = ProgressDialog.show(MapasActivity.this, "", getString(R.string.dialogo_espera), true);
 
 				// loadDatosMapa();
 
 				mapasOffline.loadDatosMapaOffline();
 
 				if (modoRed != InfoLineasTabsPager.MODO_RED_TRAM_OFFLINE) {
 					loadDatosVehiculos();
 				}
 
 			} else {
 				// launchBuses();
 				Toast.makeText(this, getResources().getText(R.string.aviso_error_datos) + " www.subus.es", Toast.LENGTH_LONG).show();
 
 			}
 
 		} else if (this.getIntent().getExtras() != null && this.getIntent().getExtras().containsKey("LINEA_MAPA_FICHA")) {
 
 			String lineaPos = this.getIntent().getExtras().getString("LINEA_MAPA_FICHA");
 
 			lineaSeleccionada = this.getIntent().getExtras().getString("LINEA_MAPA_FICHA_KML");
 			lineaSeleccionadaDesc = this.getIntent().getExtras().getString("LINEA_MAPA_FICHA_DESC");
 
 			lineaSeleccionadaNum = lineaPos;
 
 			dialog = ProgressDialog.show(MapasActivity.this, "", getString(R.string.dialogo_espera), true);
 
 			if (this.getIntent().getExtras().containsKey("LINEA_MAPA_FICHA_ONLINE")) {
 				loadDatosMapa();
 			} else {
 				mapasOffline.loadDatosMapaOffline();
 			}
 
 		}
 
 		else {
 			// Si se entra desde el menu
 			// launchBuses();
 			// miLocalizacion();
 
 		}
 
 		// Combo de seleccion de datos
 		final Spinner spinner = (Spinner) findViewById(R.id.spinner_datos);
 
 		ArrayAdapter<CharSequence> adapter = null;
 
 		if (UtilidadesTRAM.ACTIVADO_TRAM) {
 			adapter = ArrayAdapter.createFromResource(this, R.array.spinner_datos, android.R.layout.simple_spinner_item);
 		} else {
 			adapter = ArrayAdapter.createFromResource(this, R.array.spinner_datos_b, android.R.layout.simple_spinner_item);
 		}
 
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 
 		spinner.setAdapter(adapter);
 
 		// Seleccion inicial
 		int infolineaModo = preferencias.getInt("infolinea_modo", 0);
 		spinner.setSelection(infolineaModo);
 
 		// Seleccion
 		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
 
 			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
 
 				// Solo en caso de haber cambiado
 				if (preferencias.getInt("infolinea_modo", 0) != arg2) {
 
 					// Guarda la nueva seleciccion
 					SharedPreferences.Editor editor = preferencias.edit();
 					editor.putInt("infolinea_modo", arg2);
 					editor.commit();
 
 					// cambiar el modo de la actividad
 					if (arg2 == 0) {
 
 						Intent intent2 = getIntent();
 						intent2.putExtra("MODO_RED", InfoLineasTabsPager.MODO_RED_SUBUS_ONLINE);
 						finish();
 						startActivity(intent2);
 
 					} else if (arg2 == 1) {
 
 						Intent intent2 = getIntent();
 						intent2.putExtra("MODO_RED", InfoLineasTabsPager.MODO_RED_SUBUS_OFFLINE);
 						finish();
 						startActivity(intent2);
 
 					} else if (arg2 == 2) {
 
 						Intent intent2 = getIntent();
 						intent2.putExtra("MODO_RED", InfoLineasTabsPager.MODO_RED_TRAM_OFFLINE);
 						finish();
 						startActivity(intent2);
 
 					}
 
 				}
 
 			}
 
 			public void onNothingSelected(AdapterView<?> arg0) {
 				// TODO Auto-generated method stub
 
 			}
 
 		});
 
 		// Control de boton vehiculos
 		final ToggleButton botonVehiculos = (ToggleButton) findViewById(R.id.mapasVehiculosButton);
 
 		if (modoRed != InfoLineasTabsPager.MODO_RED_TRAM_OFFLINE) {
 
 			boolean vehiculosPref = preferencias.getBoolean("mapas_vehiculos", true);
 
 			if (vehiculosPref) {
 				botonVehiculos.setChecked(true);
 			}
 
 			botonVehiculos.setOnClickListener(new OnClickListener() {
 
 				public void onClick(View v) {
 
 					if (botonVehiculos.isChecked()) {
 
 						SharedPreferences.Editor editor = preferencias.edit();
 						editor.putBoolean("mapas_vehiculos", true);
 						editor.commit();
 
 						loadDatosVehiculos();
 					} else {
 
 						SharedPreferences.Editor editor = preferencias.edit();
 						editor.putBoolean("mapas_vehiculos", false);
 						editor.commit();
 
 						if (itemizedOverlayVehiculos != null) {
 							mapOverlays.remove(itemizedOverlayVehiculos);
 							itemizedOverlayVehiculos = null;
 
 							if (timer != null) {
 								timer.cancel();
 							}
 
 							mapView.invalidate();
 
 						}
 					}
 
 				}
 			});
 
 		} else {
 
 			botonVehiculos.setVisibility(View.INVISIBLE);
 
 		}
 
 	}
 
 	@Override
 	protected void onDestroy() {
 
 		detenerTareas();
 
 		super.onDestroy();
 	}
 
 	public void detenerTareas() {
 
 		if (taskDatosMapa != null && taskDatosMapa.getStatus() == Status.RUNNING) {
 
 			taskDatosMapa.cancel(true);
 
 			Log.d("MAPAS", "Cancelada task datos mapa");
 
 		}
 
 		if (taskDatosMapaVuelta != null && taskDatosMapaVuelta.getStatus() == Status.RUNNING) {
 
 			taskDatosMapaVuelta.cancel(true);
 
 			Log.d("MAPAS", "Cancelada task datos mapa vuelta");
 
 		}
 
 		if (taskBuses != null && taskBuses.getStatus() == Status.RUNNING) {
 
 			taskBuses.cancel(true);
 
 			Log.d("MAPAS", "Cancelada task taskBuses");
 
 		}
 
 		if (taskVehiculosMapa != null && taskVehiculosMapa.getStatus() == Status.RUNNING) {
 
 			taskVehiculosMapa.cancel(true);
 
 			Log.d("MAPAS", "Cancelada task vehiculos");
 
 		}
 
 		if (timer != null) {
 
 			timer.cancel();
 
 		}
 	}
 
 	private void enableLocationSettings() {
 		Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
 		startActivity(settingsIntent);
 	}
 
 	/**
 	 * Control de posicion
 	 * 
 	 * @param cercanas
 	 */
 	public void miLocalizacion(final boolean cercanas) {
 
 		if (!mMyLocationOverlay.isMyLocationEnabled()) {
 
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setMessage(getString(R.string.gps_on)).setCancelable(false).setPositiveButton(getString(R.string.barcode_si), new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int id) {
 
 					enableLocationSettings();
 
 				}
 			}).setNegativeButton(getString(R.string.barcode_no), new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int id) {
 					dialog.cancel();
 
 				}
 			});
 			AlertDialog alert = builder.create();
 
 			alert.show();
 
 		} else {
 
 			if (cercanas) {
 				setTitle(getString(R.string.cercanas));
 			}
 
 			if (primeraCarga) {
 
 				Toast.makeText(this, getString(R.string.gps_recuperando), Toast.LENGTH_SHORT).show();
 
 				mMyLocationOverlay.runOnFirstFix(new Runnable() {
 					public void run() {
 
 						mapView.getController().animateTo(mMyLocationOverlay.getMyLocation());
 
 						if (cercanas) {
 							paradasCercanas.cargarParadasCercanas(mMyLocationOverlay.getMyLocation().getLatitudeE6(), mMyLocationOverlay.getMyLocation().getLongitudeE6());
 						}
 
 						primeraCarga = false;
 
 					}
 				});
 
 				mapView.getOverlays().add(mMyLocationOverlay);
 
 				mapView.getController().setZoom(18);
 				mapView.setClickable(true);
 				mapView.setEnabled(true);
 
 			} else {
 				miLocalizacionRecarga(cercanas);
 			}
 
 		}
 
 	}
 
 	/**
 	 * Recarga de la posicion
 	 * 
 	 * @param cercanas
 	 */
 	private void miLocalizacionRecarga(boolean cercanas) {
 
 		Toast.makeText(this, getString(R.string.gps_recuperando), Toast.LENGTH_SHORT).show();
 
 		if (cercanas) {
 			paradasCercanas.cargarParadasCercanas(mMyLocationOverlay.getMyLocation().getLatitudeE6(), mMyLocationOverlay.getMyLocation().getLongitudeE6());
 
 		}
 
 		mapView.getController().animateTo(mMyLocationOverlay.getMyLocation());
 
 		mapView.getController().setZoom(18);
 		mapView.setClickable(true);
 		mapView.setEnabled(true);
 
 	}
 
 	/**
 	 * kml de carga
 	 */
 	private void loadDatosMapa() {
 
 		String url = UtilidadesTAM.getKMLParadasIda(lineaSeleccionada);
 
 		String urlRecorrido = UtilidadesTAM.getKMLRecorridoIda(lineaSeleccionada);
 
 		// Control de disponibilidad de conexion
 		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
 		if (networkInfo != null && networkInfo.isConnected()) {
 			taskDatosMapa = new LoadDatosMapaAsyncTask(loadDatosMapaAsyncTaskResponderIda).execute(url, urlRecorrido);
 		} else {
 			Toast.makeText(getApplicationContext(), getString(R.string.error_red), Toast.LENGTH_LONG).show();
 			if (dialog != null && dialog.isShowing()) {
 				dialog.dismiss();
 			}
 		}
 
 	}
 
 	/**
 	 * Se llama cuando las paradas hayan sido cargadas
 	 */
 	LoadDatosMapaAsyncTaskResponder loadDatosMapaAsyncTaskResponderIda = new LoadDatosMapaAsyncTaskResponder() {
 		public void datosMapaLoaded(DatosMapa datos) {
 
 			if (datos != null) {
 				datosMapaCargadosIda = datos;
 
 				loadDatosMapaVuelta();
 
 			} else {
 
 				Toast toast = Toast.makeText(getApplicationContext(), getResources().getText(R.string.aviso_error_datos) + " www.subus.es", Toast.LENGTH_SHORT);
 				toast.show();
 				finish();
 
 				dialog.dismiss();
 
 			}
 
 		}
 	};
 
 	private void loadDatosMapaVuelta() {
 
 		String url = UtilidadesTAM.getKMLParadasVuelta(lineaSeleccionada);
 
 		String urlVuelta = UtilidadesTAM.getKMLRecorridoVuelta(lineaSeleccionada);
 
 		// Control de disponibilidad de conexion
 		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
 		if (networkInfo != null && networkInfo.isConnected()) {
 			taskDatosMapaVuelta = new LoadDatosMapaAsyncTask(loadDatosMapaAsyncTaskResponderVuelta).execute(url, urlVuelta);
 		} else {
 			Toast.makeText(getApplicationContext(), getString(R.string.error_red), Toast.LENGTH_LONG).show();
 			if (dialog != null && dialog.isShowing()) {
 				dialog.dismiss();
 			}
 		}
 
 	}
 
 	/**
 	 * Se llama cuando las paradas hayan sido cargadas
 	 */
 	LoadDatosMapaAsyncTaskResponder loadDatosMapaAsyncTaskResponderVuelta = new LoadDatosMapaAsyncTaskResponder() {
 		public void datosMapaLoaded(DatosMapa datos) {
 
 			if (datos != null) {
 				try {
 					datosMapaCargadosVuelta = datos;
 					cargarMapa();
 
 					if (modoRed != InfoLineasTabsPager.MODO_RED_TRAM_OFFLINE) {
 						loadDatosVehiculos();
 					}
 
 				} catch (Exception e) {
 
 					Toast toast = Toast.makeText(getApplicationContext(), getResources().getText(R.string.aviso_error_datos) + " www.subus.es", Toast.LENGTH_SHORT);
 					toast.show();
 
 				}
 
 				dialog.dismiss();
 
 			} else {
 
 				Toast toast = Toast.makeText(getApplicationContext(), getResources().getText(R.string.aviso_error_datos) + " www.subus.es", Toast.LENGTH_SHORT);
 				toast.show();
 
 				dialog.dismiss();
 
 				finish();
 
 			}
 
 		}
 	};
 
 	/**
 	 * Cargar el mapa con las paradas de la linea
 	 * 
 	 */
 	public void cargarMapa() {
 
 		// Cargar datos cabecera
 		String cabdatos = lineaSeleccionadaDesc;
 		setTitle(cabdatos);
 
 		mapView = (MapView) findViewById(R.id.mapview);
 		mapView.setBuiltInZoomControls(true);
 
 		mapOverlays = mapView.getOverlays();
 
 		if (modoRed == InfoLineasTabsPager.MODO_RED_TRAM_OFFLINE) {
 			drawableIda = this.getResources().getDrawable(R.drawable.tramway);
 		} else {
 			drawableIda = this.getResources().getDrawable(R.drawable.busstop_blue);
 		}
 
 		drawableVuelta = this.getResources().getDrawable(R.drawable.busstop_green);
 		drawableMedio = this.getResources().getDrawable(R.drawable.busstop_medio);
 		itemizedOverlayIda = new MapasItemizedOverlay(drawableIda, this);
 		itemizedOverlayVuelta = new MapasItemizedOverlay(drawableVuelta, this);
 		itemizedOverlayMedio = new MapasItemizedOverlay(drawableMedio, this);
 
 		// -0.510017579,38.386057662,0
 		// 38.386057662,-0.510017579
 
 		/**
 		 * 38.344820, -0.483320‎ +38° 20' 41.35", -0° 28' 59.95"
 		 * 38.34482,-0.48332
 		 * 
 		 * long: -0,510018 lati: 38,386058 PRUEBAS‎
 		 * 
 		 */
 
 		// Carga de puntos del mapa
 
 		GeoPoint point = null;
 
 		// Recorrido ida
		if (datosMapaCargadosIda != null && datosMapaCargadosIda.getRecorrido() != null && !datosMapaCargadosIda.getRecorrido().isEmpty()) {
 
 			// Recorrido
 			drawPath(datosMapaCargadosIda, Color.parseColor("#157087"), mapView);
 
 		}
 
 		// Datos IDA
 		if (datosMapaCargadosIda != null && !datosMapaCargadosIda.getPlacemarks().isEmpty()) {
 
 			// Recorrido
 			drawPath(datosMapaCargadosIda, Color.parseColor("#157087"), mapView);
 
 			for (int i = 0; i < datosMapaCargadosIda.getPlacemarks().size(); i++) {
 
 				String[] coordenadas = datosMapaCargadosIda.getPlacemarks().get(i).getCoordinates().split(",");
 
 				double lat = Double.parseDouble(coordenadas[1]); // 38.386058;
 				double lng = Double.parseDouble(coordenadas[0]); // -0.510018;
 				int glat = (int) (lat * 1E6);
 				int glng = (int) (lng * 1E6);
 
 				// 19240000,-99120000
 				// 38337176
 				// -491890
 
 				point = new GeoPoint(glat, glng);
 				// GeoPoint point = new GeoPoint(19240000,-99120000);
 
 				String descripcionAlert = getResources().getText(R.string.share_2) + " ";
 
 				if (datosMapaCargadosIda.getPlacemarks().get(i).getSentido() != null && !datosMapaCargadosIda.getPlacemarks().get(i).getSentido().trim().equals("")) {
 					descripcionAlert += datosMapaCargadosIda.getPlacemarks().get(i).getSentido().trim();
 				} else {
 					descripcionAlert += "Ida";
 				}
 
 				descripcionAlert += "\n" + getResources().getText(R.string.lineas) + " ";
 
 				if (datosMapaCargadosIda.getPlacemarks().get(i).getLineas() != null) {
 					descripcionAlert += datosMapaCargadosIda.getPlacemarks().get(i).getLineas().trim();
 				}
 
 				descripcionAlert += "\n" + getResources().getText(R.string.observaciones) + " ";
 
 				if (datosMapaCargadosIda.getPlacemarks().get(i).getObservaciones() != null) {
 					descripcionAlert += datosMapaCargadosIda.getPlacemarks().get(i).getObservaciones().trim();
 				}
 
 				OverlayItem overlayitem = new OverlayItem(point, "[" + datosMapaCargadosIda.getPlacemarks().get(i).getCodigoParada().trim() + "] " + datosMapaCargadosIda.getPlacemarks().get(i).getTitle().trim(),
 						descripcionAlert);
 
 				itemizedOverlayIda.addOverlay(overlayitem);
 
 			}
 
 			if (itemizedOverlayIda != null && itemizedOverlayIda.size() > 0) {
 				mapOverlays.add(itemizedOverlayIda);
 			} else {
 				avisoPosibleError();
 			}
 
 		}
 
 		boolean coincide = false;
 
 		// Recorrido vuelta
		if (datosMapaCargadosVuelta != null && datosMapaCargadosVuelta.getRecorrido() != null && !datosMapaCargadosVuelta.getRecorrido().isEmpty()) {
 
 			// Recorrido
 			drawPath(datosMapaCargadosVuelta, Color.parseColor("#6C8715"), mapView);
 
 		}
 
 		// Datos VUELTA
 		if (datosMapaCargadosVuelta != null && !datosMapaCargadosVuelta.getPlacemarks().isEmpty()) {
 
 			for (int i = 0; i < datosMapaCargadosVuelta.getPlacemarks().size(); i++) {
 
 				String[] coordenadas = datosMapaCargadosVuelta.getPlacemarks().get(i).getCoordinates().split(",");
 
 				double lat = Double.parseDouble(coordenadas[1]); // 38.386058;
 				double lng = Double.parseDouble(coordenadas[0]); // -0.510018;
 				int glat = (int) (lat * 1E6);
 				int glng = (int) (lng * 1E6);
 
 				// 19240000,-99120000
 
 				point = new GeoPoint(glat, glng);
 				// GeoPoint point = new GeoPoint(19240000,-99120000);
 
 				String direc = "";
 
 				coincide = false;
 
 				if (datosMapaCargadosIda.getPlacemarks().contains(datosMapaCargadosVuelta.getPlacemarks().get(i))) {
 
 					String ida = datosMapaCargadosIda.getCurrentPlacemark().getSentido().trim();
 
 					if (ida.equals("")) {
 						ida = "Ida";
 					}
 
 					direc = ida + " " + getResources().getText(R.string.tiempo_m_3) + " " + datosMapaCargadosVuelta.getPlacemarks().get(i).getSentido();
 
 					coincide = true;
 
 				} else {
 					direc = datosMapaCargadosVuelta.getCurrentPlacemark().getSentido().trim();
 
 					coincide = false;
 				}
 
 				if (direc == null || (direc != null && direc.trim().equals(""))) {
 					direc = "Vuelta";
 				}
 
 				String descripcionAlert = getResources().getText(R.string.share_2) + " ";
 
 				if (direc != null) {
 					descripcionAlert += direc;
 				}
 
 				descripcionAlert += "\n" + getResources().getText(R.string.lineas) + " ";
 
 				if (datosMapaCargadosVuelta.getPlacemarks().get(i).getLineas() != null) {
 					descripcionAlert += datosMapaCargadosVuelta.getPlacemarks().get(i).getLineas().trim();
 				}
 
 				descripcionAlert += "\n" + getResources().getText(R.string.observaciones) + " ";
 
 				if (datosMapaCargadosVuelta.getPlacemarks().get(i).getObservaciones() != null) {
 					descripcionAlert += datosMapaCargadosVuelta.getPlacemarks().get(i).getObservaciones().trim();
 				}
 
 				OverlayItem overlayitem = new OverlayItem(point, "[" + datosMapaCargadosVuelta.getPlacemarks().get(i).getCodigoParada().trim() + "] " + datosMapaCargadosVuelta.getPlacemarks().get(i).getTitle().trim(),
 						descripcionAlert);
 
 				if (coincide) {
 					itemizedOverlayMedio.addOverlay(overlayitem);
 				} else {
 					itemizedOverlayVuelta.addOverlay(overlayitem);
 				}
 
 			}
 
 			if (itemizedOverlayMedio != null && itemizedOverlayMedio.size() > 0 && datosMapaCargadosIda != null && !datosMapaCargadosIda.getPlacemarks().isEmpty()) {
 				mapOverlays.add(itemizedOverlayMedio);
 			}
 
 			if (itemizedOverlayVuelta.size() > 0) {
 				mapOverlays.add(itemizedOverlayVuelta);
 			} else {
 				avisoPosibleError();
 			}
 
 		}
 
 		if (!primeraCarga) {
 			mapView.getOverlays().add(mMyLocationOverlay);
 			mapView.getController().animateTo(mMyLocationOverlay.getMyLocation());
 
 		} else if (point != null) {
 
 			MapController mapController = mapView.getController();
 
 			mapController.animateTo(point);
 			mapController.setZoom(15);
 		}
 
 	}
 
 	private void avisoPosibleError() {
 
 		Toast.makeText(this, getString(R.string.mapa_posible_error), Toast.LENGTH_LONG).show();
 
 	}
 
 	/**
 	 * Ir a la parada seleccionada y cerra mapa
 	 * 
 	 */
 	public void irParadaSeleccionada() {
 
 		int codigo;
 
 		if (paradaSeleccionada != null) {
 			try {
 				codigo = Integer.parseInt(paradaSeleccionada.substring(1, 5));
 			} catch (Exception e) {
 
 				// Por si es tram
 				int c1 = paradaSeleccionada.indexOf("[");
 				int c2 = paradaSeleccionada.indexOf("]");
 
 				codigo = Integer.parseInt(paradaSeleccionada.substring(c1 + 1, c2));
 
 				Log.d("", "mapa:" + codigo);
 
 			}
 
 			Intent intent = new Intent(this, MainActivity.class);
 			Bundle b = new Bundle();
 			b.putInt("poste", codigo);
 			intent.putExtras(b);
 
 			SharedPreferences.Editor editor = preferencias.edit();
 			editor.putInt("parada_inicio", codigo);
 			editor.commit();
 
 			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 
 			startActivity(intent);
 
 		}
 		super.finish();
 
 	}
 
 	public String getParadaSeleccionada() {
 		return paradaSeleccionada;
 	}
 
 	public void setParadaSeleccionada(String paradaSeleccionada) {
 		this.paradaSeleccionada = paradaSeleccionada;
 	}
 
 	private void launchBuses() {
 
 		// flagOffline = false;
 
 		cargarDatosLineas();
 
 	}
 
 	private void launchBusesOffline() {
 
 		// flagOffline = true;
 
 		cargarDatosLineas();
 
 	}
 
 	ArrayList<BusLinea> lineasBus;
 	BusAdapter lineasAdapter;
 
 	Dialog dialogoLineas = null;
 
 	private void cargarDatosLineas() {
 
 		lineasAdapter = new BusAdapter(this, android.R.layout.simple_list_item_1);
 
 		loadBuses();
 
 	}
 
 	private void abrirDialogoLineas() {
 
 		dialogoLineas = new Dialog(this);
 
 		dialogoLineas.setTitle(R.string.tit_buses);
 
 		LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		View vista = li.inflate(R.layout.buses, null, false);
 
 		dialogoLineas.setContentView(vista);
 
 		ListView lista = (ListView) dialogoLineas.findViewById(android.R.id.list);
 
 		lista.setAdapter(lineasAdapter);
 
 		lista.setOnItemClickListener(lineasClickedHandler);
 
 		dialogoLineas.show();
 
 	}
 
 	/**
 	 * Listener encargado de gestionar las pulsaciones sobre los items
 	 */
 	private OnItemClickListener lineasClickedHandler = new OnItemClickListener() {
 
 		/**
 		 * @param l
 		 *            The ListView where the click happened
 		 * @param v
 		 *            The view that was clicked within the ListView
 		 * @param position
 		 *            The position of the view in the list
 		 * @param id
 		 *            The row id of the item that was clicked
 		 */
 		public void onItemClick(AdapterView<?> l, View v, int position, long id) {
 
 			dialogoLineas.dismiss();
 
 			lineaSeleccionada(position);
 
 		}
 	};
 
 	private void lineaSeleccionada(int posicion) {
 
 		detenerTareas();
 
 		// Limpiar lista anterior para nuevas busquedas
 		if (mapView != null && mapView.getOverlays() != null) {
 			mapView.getOverlays().clear();
 		}
 
 		lineaSeleccionada = lineasBus.get(posicion).getIdlinea();
 		lineaSeleccionadaDesc = lineasBus.get(posicion).getLinea();
 
 		lineaSeleccionadaNum = lineasBus.get(posicion).getNumLinea();
 
 		dialog = ProgressDialog.show(MapasActivity.this, "", getString(R.string.dialogo_espera), true);
 
 		if (modoRed == InfoLineasTabsPager.MODO_RED_SUBUS_ONLINE) {
 			loadDatosMapa();
 		} else if (modoRed == InfoLineasTabsPager.MODO_RED_SUBUS_OFFLINE) {
 			mapasOffline.loadDatosMapaOffline();
 
 			loadDatosVehiculos();
 
 		} else if (modoRed == InfoLineasTabsPager.MODO_RED_TRAM_OFFLINE) {
 			mapasOffline.loadDatosMapaTRAMOffline();
 		}
 
 	}
 
 	/**
 	 * Carga las lineas de bus
 	 */
 	private void loadBuses() {
 
 		dialog = ProgressDialog.show(MapasActivity.this, "", getString(R.string.dialogo_espera), true);
 
 		// Carga local de lineas
 		String datosOffline = null;
 		if (modoRed == InfoLineasTabsPager.MODO_RED_SUBUS_OFFLINE) {
 
 			Resources resources = getResources();
 			InputStream inputStream = resources.openRawResource(R.raw.lineasoffline);
 
 			datosOffline = Utilidades.obtenerStringDeStream(inputStream);
 
 		} else if (modoRed == InfoLineasTabsPager.MODO_RED_TRAM_OFFLINE) {
 
 			Resources resources = getResources();
 			InputStream inputStream = resources.openRawResource(R.raw.lineasoffline_tram);
 
 			datosOffline = Utilidades.obtenerStringDeStream(inputStream);
 
 		}
 
 		// Control de disponibilidad de conexion
 		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
 		if (networkInfo != null && networkInfo.isConnected()) {
 			taskBuses = new LoadDatosLineasAsyncTask(loadBusesAsyncTaskResponder).execute(datosOffline);
 		} else {
 			Toast.makeText(getApplicationContext(), getString(R.string.error_red), Toast.LENGTH_LONG).show();
 			if (dialog != null && dialog.isShowing()) {
 				dialog.dismiss();
 			}
 		}
 
 	}
 
 	LoadDatosLineasAsyncTaskResponder loadBusesAsyncTaskResponder = new LoadDatosLineasAsyncTaskResponder() {
 		public void busesLoaded(ArrayList<BusLinea> buses) {
 			if (buses != null) {
 				lineasBus = buses;
 				lineasAdapter.clear();
 				lineasAdapter.addAll(lineasBus);
 				lineasAdapter.notifyDataSetChanged();
 
 				dialog.dismiss();
 
 				abrirDialogoLineas();
 
 			} else {
 
 				dialog.dismiss();
 
 			}
 
 		}
 	};
 
 	/**
 	 * Mostrar y ocultar el recorrido de ida
 	 */
 	private void cargarOcultarIda() {
 
 		// if (datosMapaCargadosVuelta != null &&
 		// datosMapaCargadosVuelta.getPlacemarks().isEmpty()) {
 
 		// Toast.makeText(this, getString(R.string.mapa_aviso_vacio),
 		// Toast.LENGTH_SHORT).show();
 
 		// } else
 
 		if (datosMapaCargadosIda != null) {
 
 			if (!datosMapaCargadosIda.getPlacemarks().isEmpty()) {
 				datosMapaCargadosIdaAux = new DatosMapa();
 				datosMapaCargadosIdaAux.setPlacemarks(datosMapaCargadosIda.getPlacemarks());
 				datosMapaCargadosIda.setPlacemarks(new ArrayList<PlaceMark>());
 			} else if (datosMapaCargadosIdaAux != null) {
 				datosMapaCargadosIda.setPlacemarks(datosMapaCargadosIdaAux.getPlacemarks());
 			}
 
 			// Limpiar lista anterior para nuevas busquedas
 			if (mapView != null && mapView.getOverlays() != null)
 				mapView.getOverlays().clear();
 
 			cargarMapa();
 
 			loadDatosVehiculos();
 		}
 	}
 
 	/**
 	 * Mostrar y ocultar el recorrido de vuelta
 	 */
 	private void cargarOcultarVuelta() {
 
 		// if (datosMapaCargadosIda != null &&
 		// datosMapaCargadosIda.getPlacemarks().isEmpty()) {
 
 		// Toast.makeText(this, getString(R.string.mapa_aviso_vacio),
 		// Toast.LENGTH_SHORT).show();
 
 		// } else
 
 		if (datosMapaCargadosVuelta != null) {
 
 			if (!datosMapaCargadosVuelta.getPlacemarks().isEmpty()) {
 				datosMapaCargadosVueltaAux = new DatosMapa();
 				datosMapaCargadosVueltaAux.setPlacemarks(datosMapaCargadosVuelta.getPlacemarks());
 				datosMapaCargadosVuelta.setPlacemarks(new ArrayList<PlaceMark>());
 			} else if (datosMapaCargadosVueltaAux != null) {
 				datosMapaCargadosVuelta.setPlacemarks(datosMapaCargadosVueltaAux.getPlacemarks());
 			}
 
 			// Limpiar lista anterior para nuevas busquedas
 			if (mapView != null && mapView.getOverlays() != null)
 				mapView.getOverlays().clear();
 
 			cargarMapa();
 
 			loadDatosVehiculos();
 
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater menuInflater = getMenuInflater();
 		menuInflater.inflate(R.menu.mapa, menu);
 
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		/*
 		 * case R.id.menu_search: onSearchRequested(); break;
 		 */
 		case R.id.menu_satelite:
 
 			if (mapView.isSatellite()) {
 				mapView.setSatellite(false);
 			} else {
 				mapView.setSatellite(true);
 			}
 
 			break;
 		case R.id.menu_ida:
 			if (modoRed != InfoLineasTabsPager.MODO_RED_TRAM_OFFLINE) {
 				cargarOcultarIda();
 			}
 
 			// Prueba
 			// loadDatosVehiculos();
 
 			break;
 		case R.id.menu_vuelta:
 			if (modoRed != InfoLineasTabsPager.MODO_RED_TRAM_OFFLINE) {
 				cargarOcultarVuelta();
 			}
 			break;
 		case R.id.menu_cercanas:
 
 			paradasCercanas.seleccionarProximidad();
 
 			break;
 		case R.id.menu_posicion:
 			miLocalizacion(false);
 			break;
 		case R.id.menu_search_online:
 			launchBuses();
 			break;
 		/*
 		 * case R.id.menu_search_offline: launchBusesOffline(); break;
 		 */
 		case android.R.id.home:
 			Intent intent = new Intent(this, MainActivity.class);
 			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			startActivity(intent);
 			break;
 
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	@Override
 	public void finish() {
 
 		Intent intent = new Intent();
 		setResult(MainActivity.SUB_ACTIVITY_RESULT_OK, intent);
 		super.finish();
 
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		mMyLocationOverlay.enableMyLocation();
 		mMyLocationOverlay.enableCompass();
 	}
 
 	@Override
 	protected void onStop() {
 		mMyLocationOverlay.disableMyLocation();
 		mMyLocationOverlay.disableCompass();
 		super.onStop();
 	}
 
 	public void drawPath(DatosMapa navSet, int color, MapView mMapView01) {
 
 		// color correction for dining, make it darker
 		if (color == Color.parseColor("#add331"))
 			color = Color.parseColor("#6C8715");
 
 		/*
 		 * Collection overlaysToAddAgain = new ArrayList(); for (Iterator iter =
 		 * mMapView01.getOverlays().iterator(); iter.hasNext();) { Object o =
 		 * iter.next();
 		 * 
 		 * if (!DatosRuta.class.getName().equals(o.getClass().getName())) { //
 		 * mMapView01.getOverlays().remove(o); overlaysToAddAgain.add(o); } }
 		 * mMapView01.getOverlays().clear();
 		 * mMapView01.getOverlays().addAll(overlaysToAddAgain);
 		 */
 		String path = navSet.getRecorrido();
 
 		if (path != null && path.trim().length() > 0) {
 			String[] pairs = path.trim().split(" ");
 
 			String[] lngLat = pairs[0].split(","); // lngLat[0]=longitude
 													// lngLat[1]=latitude
 													// lngLat[2]=height
 
 			if (lngLat.length < 3)
 				lngLat = pairs[1].split(","); // if first pair is not
 												// transferred completely, take
 												// seconds pair //TODO
 
 			try {
 				GeoPoint startGP = new GeoPoint((int) (Double.parseDouble(lngLat[1]) * 1E6), (int) (Double.parseDouble(lngLat[0]) * 1E6));
 				mMapView01.getOverlays().add(new DatosRuta(startGP, startGP, 1));
 				GeoPoint gp1;
 				GeoPoint gp2 = startGP;
 
 				for (int i = 1; i < pairs.length; i++) // the last one would be
 														// crash
 				{
 					lngLat = pairs[i].split(",");
 
 					gp1 = gp2;
 
 					// if (lngLat.length >= 2 && gp1.getLatitudeE6() > 0 &&
 					// gp1.getLongitudeE6() > 0
 					// && gp2.getLatitudeE6() > 0 && gp2.getLongitudeE6() > 0) {
 					if (lngLat.length >= 2) {
 						// for GeoPoint, first:latitude, second:longitude
 						gp2 = new GeoPoint((int) (Double.parseDouble(lngLat[1]) * 1E6), (int) (Double.parseDouble(lngLat[0]) * 1E6));
 
 						// if (gp2.getLatitudeE6() != 22200000) {
 						mMapView01.getOverlays().add(new DatosRuta(gp1, gp2, 2, color));
 
 						// }
 					}
 
 				}
 				// DatosRutas.add(new DatosRuta(gp2,gp2, 3));
 				mMapView01.getOverlays().add(new DatosRuta(gp2, gp2, 3));
 			} catch (NumberFormatException e) {
 
 			}
 		}
 
 	}
 
 	public ProgressDialog getDialog() {
 		return dialog;
 	}
 
 	/*
 	 * VEHICULOS
 	 */
 
 	/**
 	 * Carga de vehiculos de la linea
 	 */
 	private void loadDatosVehiculos() {
 
 		ToggleButton toogleButton = (ToggleButton) findViewById(R.id.mapasVehiculosButton);
 
 		if (!toogleButton.isChecked() || lineaSeleccionadaNum == null || lineaSeleccionadaNum.equals("")) {
 			return;
 		}
 
 		// dialog = ProgressDialog.show(MapasActivity.this, "",
 		// getString(R.string.dialogo_espera), true);
 
 		// Control de disponibilidad de conexion
 		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
 		if (networkInfo != null && networkInfo.isConnected()) {
 
 			if (timer != null) {
 				timer.cancel();
 			}
 
 			final Handler handler = new Handler();
 			timer = new Timer();
 			TimerTask timerTask = new TimerTask() {
 
 				@Override
 				public void run() {
 					handler.post(new Runnable() {
 
 						public void run() {
 
 							taskVehiculosMapa = new LoadVehiculosMapaAsyncTask(loadVehiculosMapaAsyncTaskResponder).execute(lineaSeleccionadaNum);
 
 						}
 					});
 
 				}
 			};
 
 			timer.schedule(timerTask, 0, 30000);
 
 		} else {
 			Toast.makeText(getApplicationContext(), getString(R.string.error_red), Toast.LENGTH_LONG).show();
 			if (dialog != null && dialog.isShowing()) {
 				dialog.dismiss();
 			}
 		}
 
 	}
 
 	/**
 	 * Carga de vehiculos de la linea
 	 */
 	LoadVehiculosMapaAsyncTaskResponder loadVehiculosMapaAsyncTaskResponder = new LoadVehiculosMapaAsyncTaskResponder() {
 
 		public void vehiculosMapaLoaded(DatosMapa datosMapa) {
 
			if (datosMapa != null && datosMapa.getVehiculosList() != null) {
 				datosMapaCargadosIda.setVehiculosList(datosMapa.getVehiculosList());
 
 				cargarVehiculosMapa();
 
 				dialog.dismiss();
 
 			} else {
 
 				Toast toast = Toast.makeText(getApplicationContext(), getResources().getText(R.string.aviso_error_datos) + " www.subus.es", Toast.LENGTH_SHORT);
 				toast.show();
 				finish();
 
 				dialog.dismiss();
 
 			}
 
 		}
 	};
 
 	/**
 	 * Cargar el mapa con las paradas de la linea
 	 * 
 	 */
 	public void cargarVehiculosMapa() {
 
 		if (itemizedOverlayVehiculos != null) {
 			mapOverlays.remove(itemizedOverlayVehiculos);
 		}
 
 		drawableVehiculo = this.getResources().getDrawable(R.drawable.bus);
 
 		itemizedOverlayVehiculos = new VehiculosItemizedOverlay(drawableVehiculo, this);
 
 		GeoPoint point = null;
 
 		// Datos IDA
 		if (datosMapaCargadosIda != null && !datosMapaCargadosIda.getVehiculosList().isEmpty()) {
 
 			for (int i = 0; i < datosMapaCargadosIda.getVehiculosList().size(); i++) {
 
 				// if(!datosMapaCargadosIda.getVehiculosList().get(i).getEstado().equals("512")){
 				// continue;
 				// }
 
 				double y = Double.parseDouble(datosMapaCargadosIda.getVehiculosList().get(i).getYcoord());
 				double x = Double.parseDouble(datosMapaCargadosIda.getVehiculosList().get(i).getXcoord());
 
 				String coord = UtilidadesGeo.getLatLongUTMBus(y, x);
 
 				String[] coordenadas = coord.split(",");
 
 				double lat = Double.parseDouble(coordenadas[1]); // 38.386058;
 				double lng = Double.parseDouble(coordenadas[0]); // -0.510018;
 
 				// Desvio en el calculo
 				lat = lat - 0.001517;
 				lng = lng - 0.001517;
 
 				/*
 				 * prueba no: 0,002185
 				 * 
 				 * Erronea: 38.337297,-0.492949 correcta: 38.337625,-0.492155
 				 * desvio: +0,000328, +0,000794
 				 * 
 				 * Erronea: 38.338631,-0.490427 Correcta: 38.337114,-0.491668
 				 * desvio: 0,001517, −0,001241
 				 */
 
 				int glat = (int) (lat * 1E6);
 				int glng = (int) (lng * 1E6);
 
 				point = new GeoPoint(glat, glng);
 
 				String descripcionAlert = "";
 
 				OverlayItem overlayitem = new OverlayItem(point, datosMapaCargadosIda.getVehiculosList().get(i).getVehiculo().trim(), descripcionAlert);
 
 				itemizedOverlayVehiculos.addOverlay(overlayitem);
 
 				/*
 				 * // 19240000,-99120000
 				 * 
 				 * //UTM a geograficas geotools jcoord
 				 * 
 				 * long x: 715923 lat y: 4253901 30N 715923 4253901 -> 38.40728
 				 * -0.52710 geographiclib //lat:38337176 //long:-491890
 				 */
 
 			}
 
 			if (itemizedOverlayVehiculos != null && itemizedOverlayVehiculos.size() > 0) {
 				mapOverlays.add(itemizedOverlayVehiculos);
 			} else {
 				avisoPosibleError();
 			}
 
 			mapView.invalidate();
 
 		}
 
		
 	}
 
 }
