 package com.nip.wereport;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.graphics.Color;
 import android.location.Address;
 import android.location.Geocoder;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.view.ContextThemeWrapper;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.ExpandableListView;
 import android.widget.Spinner;
 import android.widget.Toast;
 
 import com.google.android.gms.common.GooglePlayServicesUtil;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
 import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.BitmapDescriptor;
 import com.google.android.gms.maps.model.BitmapDescriptorFactory;
 import com.google.android.gms.maps.model.CameraPosition;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.LatLngBounds;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 import com.google.android.gms.maps.model.Polyline;
 import com.google.android.gms.maps.model.PolylineOptions;
 import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
 import com.nip.wereport.R;
 
 public class MainActivity extends FragmentActivity implements ExpandableListView.OnChildClickListener{
 
 	/**
 	 * Constante para los lmites geogrficos de la cmara inicial; bogot.
 	 */
 	private LatLngBounds BOGOTA = new LatLngBounds(new LatLng(4.59354,-74.26964), new LatLng(4.79952,-73.98262));
 
 	private SlidingMenu slidingMenu;
 	
 	private ExpandableListView sectionListView;
 
 	//-----------------------------------
 	// Constructor del fragment
 	//-----------------------------------
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
 
 		//Sliding Menu INIT
 		slidingMenu = new SlidingMenu(this);
 		slidingMenu.setMode(SlidingMenu.LEFT);
 		slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
 		slidingMenu.setFadeDegree(0.35f);
 		slidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
 		slidingMenu.setMenu(R.layout.slidingmenu);
 		sectionListView = new ExpandableListView(getApplicationContext());
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 		List<Section> sectionList = createMenu();
 		SectionListAdapter sectionListAdapter = new SectionListAdapter(this, sectionList);
         sectionListView.setAdapter(sectionListAdapter);
         
         sectionListView.setOnChildClickListener(this);
         
         int count = sectionListAdapter.getGroupCount();
         for (int position = 0; position < count; position++) {
             this.sectionListView.expandGroup(position);
         }
 
 		final GoogleMap map = ((SupportMapFragment)  getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
 		//para que solo muestre a bogota
 		map.setMyLocationEnabled(true);
 		//Desactiva la rotacion del mapa
 		map.getUiSettings().setRotateGesturesEnabled(false);
 
 		//Toasts de bienvenida e instrucciones
 		Toast.makeText(this, "Bienvenido a WeReport",  Toast.LENGTH_LONG).show();
 		Toast bienvenida = Toast.makeText(this, "Toque una calle y luego la direccin para hacer un reporte",  Toast.LENGTH_LONG);
 		bienvenida.show();
 
 		//----------------------------------
 		// Setup del click listener
 		//----------------------------------
 		map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
 			Marker marker;
 			Polyline p;
 			@Override
 			public void onMapClick(LatLng point) {
 				//Remueve el marcador si ya existe, y crea otro nuevo
 				if(marker!=null)
 					marker.remove();
 				//TODO: Implementar Thread para que el mapa no se traba mientras encuentra la calle
 				Toast.makeText(getApplicationContext(), "Identificando calle...", Toast.LENGTH_LONG).show();
 				List<Address> a = getAddress(point); 
 				if (a != null){
 					BitmapDescriptor bmd = BitmapDescriptorFactory.fromResource(R.drawable.pointer);
 					marker= map.addMarker(new MarkerOptions().position(point)
 							.title(a.get(0).getAddressLine(0)).snippet(getAddress(point).get(0).getAddressLine(1)+" - "+getAddress(point).get(0).getAddressLine(2)).icon(bmd));
 					marker.showInfoWindow();
 					getAddress(point);
 					//
 					if (p!=null)
 						p.remove();
 					p = crearPolyline(a, map);
 				}
 				else
 				{
 					Toast.makeText(getApplicationContext(), "Direccin no disponible para este punto", Toast.LENGTH_LONG).show();
 				}
 
 			}
 		});
 
 
 		//----------------------------------
 		// Setup de la camara del mapa
 		//----------------------------------
 		map.setOnCameraChangeListener(new OnCameraChangeListener() {
 
 			@Override
 			public void onCameraChange(CameraPosition arg0) {
 				// Mostrar Bogota cuando inicia
 				map.moveCamera(CameraUpdateFactory.newLatLngBounds(BOGOTA, 10));
 				// Remove listener to prevent position reset on camera move.
 				map.setOnCameraChangeListener(null);
 			}
 		});
 
 
 		//-----------------------------------
 		// Creacin del dialogo para reportar
 		//-----------------------------------	
 		AlertDialog.Builder builder = new AlertDialog.Builder( new ContextThemeWrapper(this, android.R.style.Theme_Holo));
 		builder.setMessage("Qu pas?")
 		.setTitle("Reporte")	
 
 		.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 				Toast.makeText(getApplicationContext(), "Se report la calle!", Toast.LENGTH_LONG).show();
 			}
 		})
 		.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 
 				dialog.cancel();
 
 			}
 		});
 		//-------------------------------------
 		// Creacin del spinner para el dialogo
 		//-------------------------------------
 		Context mContext = this;
 		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
 		View layout = inflater.inflate(R.layout.spinner1,null);
 
 		Spinner s = (Spinner) layout.findViewById(R.id.spinner1);
 
 		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.report_arrays, R.layout.spinner_item);		
 		s.setAdapter(adapter);
 
 		builder.setView(layout);
 
 		final AlertDialog dialog = builder.create();
 
 		map.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
 
 			public void onInfoWindowClick(Marker marker) {
 				dialog.show();
 
 			}
 		});
 
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if ( keyCode == KeyEvent.KEYCODE_MENU ) {
 			this.slidingMenu.toggle();
 			return true;
 		}
 		return super.onKeyDown(keyCode, event);
 	}
 
 	@Override
 	public void onBackPressed() {
 		if ( slidingMenu.isMenuShowing()) {
 			slidingMenu.toggle();
 		}
 		else {
 			super.onBackPressed();
 		}
 	}
 
 	private List<Section> createMenu() {
 		List<Section> sectionList = new ArrayList<Section>();
 
 		Section oDemoSection = new Section("Options");
 		oDemoSection.addSectionItem(101,"Opciones", "slidingmenu_friends");
 		oDemoSection.addSectionItem(102, "Actualizar mapa", "slidingmenu_airport");
 
 		Section oGeneralSection = new Section("Acerca de");
 		oGeneralSection.addSectionItem(201, "Quines somos", "slidingmenu_settings");
 		oGeneralSection.addSectionItem(202, "Licencias", "slidingmenu_rating");
 
 		sectionList.add(oDemoSection);
 		sectionList.add(oGeneralSection);
 		return sectionList;
 	}
 
 	public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
 
 		switch ((int)id) {
 		case 101:
 			//TODO
 			break;
 		case 102:
 			//TODO
 			break;
 		case 201:
 			//TODO
 			break;
 		case 202:
 			//TODO
 			break;
 		}
 		return false;
 	}
 
 
 	private Polyline crearPolyline(List<Address> a2, GoogleMap map) {
 
 		Address ad = a2.get(0);
 		String address = ad.getAddressLine(0);
 		System.out.println(address);
 
 		if(address.contains("-"))
 		{
 
 		}
 
 		//Saca "numCalle1 a numCalle2"
 		String[] addressSplit = address.split("-");
 		String addressA = null, addressB = null;
 		Polyline p = null;
 
 
 		try {
 
 			//Saca "numCalle1" y "a numCalle2"
 			String[] addressSplit2 = addressSplit[1].split(" a ");
 
 			//Primera direccion para el polyline
 			addressA=addressSplit[0]+"-"+addressSplit2[0]+", Bogot";
 			LatLng a = getLatLongFromAddress(addressA);
 			addressB=addressSplit[0]+"-"+addressSplit[2]+", Bogot";
 			LatLng b = getLatLongFromAddress(addressB);
 
 
 			System.out.println(addressA);
 			System.out.println(addressB);
 			p = map.addPolyline(new PolylineOptions().add(a,b).color(Color.BLUE));
 
 		} catch (Exception e) {
 			Toast.makeText(getApplicationContext(), "Procure tocar calles rectas", Toast.LENGTH_LONG).show();
 		}
 
 		return p;
 	}
 
 	/**
 	 * Obtiene el LatLng de una direccin
 	 * @param address La direccin a convertir
 	 * @return Latitud y Longitud de la direccin
 	 */
 	private LatLng getLatLongFromAddress(String address)
 	{
 		LatLng p=null;
 		Geocoder geoCoder = new Geocoder(this, Locale.getDefault());    
 		try 
 		{
 			List<Address> addresses = geoCoder.getFromLocationName(address , 1);
 			if (addresses.size() > 0) 
 			{            
 				p = new LatLng((addresses.get(0).getLatitude()),(addresses.get(0).getLongitude()));
 				return p;
 			}
 		}
 		catch(Exception e)
 		{
 			Toast.makeText(getApplicationContext(), "El servicio de Google Maps no se encuentra disponible.\n"
 					+ "Intente ms tarde.", Toast.LENGTH_LONG).show();
 		}
 		return p;
 	}
 
 	/**
 	 * Obtiene la direccin para un LatLng
 	 * @param point Latitud y longitud el punto cuya direccin se quiere encontrar
 	 * @return La primera direccin encontrada para el punto
 	 */
 	public List<Address> getAddress(LatLng point) {
 		try {
 			Geocoder geocoder;
 			List<Address> addresses;
 			geocoder = new Geocoder(this);
 			if (point.latitude != 0 || point.longitude != 0) {
 				addresses = geocoder.getFromLocation(point.latitude ,
 						point.longitude, 1);
 				String address = addresses.get(0).getAddressLine(0);
 				String city = addresses.get(0).getAddressLine(1);
 				String country = addresses.get(0).getAddressLine(2);
 				System.out.println(address+" - "+city+" - "+country);
 
 				return addresses;
 
 			} else {
 				Toast.makeText(this, "latitude and longitude are null",
 						Toast.LENGTH_LONG).show();
 				return null;
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			this.slidingMenu.toggle();
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 }
