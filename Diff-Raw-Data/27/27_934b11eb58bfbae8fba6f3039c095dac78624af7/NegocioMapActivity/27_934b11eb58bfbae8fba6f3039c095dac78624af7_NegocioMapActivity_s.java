 package com.mawape.aimant.activities;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Color;
 import android.location.Address;
 import android.location.Geocoder;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.provider.Settings;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GooglePlayServicesUtil;
 import com.google.android.gms.maps.CameraUpdate;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.UiSettings;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.MarkerOptions;
 import com.mawape.aimant.R;
 import com.mawape.aimant.constants.AppConstants;
 import com.mawape.aimant.entities.Categoria;
 import com.mawape.aimant.entities.Negocio;
 import com.mawape.aimant.utilities.ApacheStringUtils;
 
 public class NegocioMapActivity extends BaseActivity {
 	private GoogleMap googleMap;
 	private Negocio negocioSeleccionado;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.map_layout);
 		Toast.makeText(getApplicationContext(), "Cargando Mapa...",
 				Toast.LENGTH_SHORT).show();
 		
 		new Runnable() {
 			
 			@Override
 			public void run() {
 				// TODO Auto-generated method stub
 				new AsyncTask<Void, Void, String>() {
 
 					@Override
 					protected String doInBackground(Void... params) {
 						return isMapInitialized();
 					}
 
 					protected void onPostExecute(String resultMessage) {
 						if (!ApacheStringUtils.isEmpty(resultMessage)) {
 							showGMapNotFoundDialog(resultMessage);
 						} else {
 							initBusiness();
 							postMapConfig();
 						}
 					}
 
 				}.execute();
 			}
 		}.run();
 		
 
 	}
 
 	// Simulating something timeconsuming
 	private void doFakeWork() {
 		Log.d("fake-work..","fakeWorking");
 		try {
 			Thread.sleep(500);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 
 	protected String isMapInitialized() {
 		String resultMessage = null;
 		// googleMap initialization
 		// Getting Google Play availability status
 		int status = GooglePlayServicesUtil
 				.isGooglePlayServicesAvailable(getBaseContext());
 
 		// Google Play Services are not available
 		if (status != ConnectionResult.SUCCESS) {
 			resultMessage = getResources().getString(
 					R.string.google_play_services_outdated);
 		}
 
 		googleMap = ((SupportMapFragment) getSupportFragmentManager()
 				.findFragmentById(R.id.map)).getMap();
 		if (googleMap == null) {
 			resultMessage = getResources().getString(
 					R.string.google_map_not_found);
 		}
 
 		return resultMessage;
 
 	}
 
 	private void showGMapNotFoundDialog(String message) {
 		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
 		dialog.setMessage(message);
		dialog.setPositiveButton(R.string.open_location_settings,
 				new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface paramDialogInterface,
 							int paramInt) {
						openSecuritySettings();
 					}
 				});
 		dialog.show();
 	}
 
	private void openSecuritySettings() {
		Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		getApplicationContext().startActivity(intent);
	}

 	private void postMapConfig() {
 		mapSettings();
 		StringBuilder direccion = new StringBuilder();
 		direccion.append(negocioSeleccionado.getDireccion());
 		direccion.append(getString(R.string.default_localidad));
 		markLocation(negocioSeleccionado.getNombre(), direccion.toString());
 	}
 
 	private void mapSettings() {
 		UiSettings gmapSettings = googleMap.getUiSettings();
 		gmapSettings.setMyLocationButtonEnabled(false);
 		gmapSettings.setZoomControlsEnabled(false);
 		gmapSettings.setCompassEnabled(false);
 	}
 
 	private void markLocation(String title, String direccion) {
 		try {
 			Geocoder geocoder = new Geocoder(getApplicationContext());
 			List<Address> addresses = new ArrayList<Address>();
 			addresses = geocoder.getFromLocationName(direccion, 1);
 			if (addresses.size() > 0) {
 				double latitude = addresses.get(0).getLatitude();
 				double longitude = addresses.get(0).getLongitude();
 				LatLng data = new LatLng(latitude, longitude);
 				CameraUpdate center = CameraUpdateFactory.newLatLng(data);
 				CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);
 				googleMap.addMarker(new MarkerOptions().position(data).title(
 						title));
 				googleMap.moveCamera(center);
 				googleMap.animateCamera(zoom);
 			}
 		} catch (IOException ex) {
 			Log.d(getClass().getName(),
 					"ioexception geocoder:" + ex.getMessage());
 		}
 
 	}
 
 	private void initBusiness() {
 		// categoria visual init.
 		Bundle bundle = getIntent().getExtras();
 		Categoria categoriaSeleccionada = (Categoria) bundle
 				.get(AppConstants.CATEGORIA_SELECCIONADA_KEY);
 		negocioSeleccionado = (Negocio) bundle
 				.get(AppConstants.NEGOCIO_SELECCIONADO_KEY);
 		if (categoriaSeleccionada != null) {
 			configureMenuBar(negocioSeleccionado.getNombre(),
 					Color.parseColor("#" + categoriaSeleccionada.getColor()),
 					true, null);
 		}
 
 		final String primPhone = negocioSeleccionado.getTelefonoPrimario();
 		final String secPhone = negocioSeleccionado.getTelefonoSecundario();
 		TextView txtPrimPhone = (TextView) findViewById(R.id.detailSecondaryPhoneText);
 		txtPrimPhone.setText(primPhone);
 
 		ImageView icPrimPhone = (ImageView) findViewById(R.id.detailSecondaryPhoneIcon);
 		icPrimPhone.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				makePhoneCall(primPhone);
 			}
 		});
 
 		// if secPhone actually is there
 		if (secPhone != null && secPhone.length() > 0) {
 			RelativeLayout layout = (RelativeLayout) findViewById(R.id.detailTopPhoneLayout);
 			layout.setVisibility(View.VISIBLE);
 			TextView txtSecPhone = (TextView) findViewById(R.id.detailPrimaryPhoneText);
 			txtSecPhone.setText(secPhone);
 
 			ImageView icSecPhone = (ImageView) findViewById(R.id.detailPrimaryPhoneIcon);
 			icSecPhone.setOnClickListener(new OnClickListener() {
 
 				@Override
 				public void onClick(View v) {
 					makePhoneCall(secPhone);
 				}
 			});
 		}
 
 		setHorarioValue(negocioSeleccionado.getHorario());
 		setWebValue(negocioSeleccionado.getWeb());
 		setFacebookValue(negocioSeleccionado.getFacebook());
 		setEmailValue(negocioSeleccionado.getEmail());
 	}
 
 	private void setHorarioValue(String text) {
 		setTxtValue(text, R.id.detailHorarioLayout, R.id.detailHorarioText);
 	}
 
 	private void setWebValue(String text) {
 		setTxtValue(text, R.id.detailWebLayout, R.id.detailWebText);
 	}
 
 	private void setFacebookValue(String text) {
 		setTxtValue(text, R.id.detailFacebookLayout, R.id.detailFacebookText);
 	}
 
 	private void setEmailValue(String text) {
 		setTxtValue(text, R.id.detailEmailLayout, R.id.detailEmailText);
 	}
 
 	private void setTxtValue(String textValue, int containerId, int txtId) {
 		TextView textView = (TextView) findViewById(txtId);
 		if (textView == null) {
 			String name = getResources().getResourceEntryName(txtId);
 			throw new RuntimeException("View whose id attribute is 'R.id."
 					+ name + "' could not be found");
 		}
 		if (ApacheStringUtils.isEmpty(textValue)) {
 			RelativeLayout container = (RelativeLayout) findViewById(containerId);
 			if (container != null) {
 				container.setVisibility(View.GONE);
 			}
 
 		} else {
 			textView.setText(textValue);
 		}
 
 	}
 }
