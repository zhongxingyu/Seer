 package com.mobyere.hapintehour;
 
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.app.NavUtils;
 import android.support.v4.view.PagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.telephony.TelephonyManager;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.mobyere.hapintehour.dao.FavorisDao;
 import com.mobyere.hapintehour.dao.FavorisMetier;
 
 public class DetailsBarActivity extends Activity {
 	private ImageView imgBarDetail;
 	private TextView txtNomBarDetail;
 	private TextView txtRueBarDetail;
 	private TextView txtCodePostalBarDetail;
 	private TextView txtTelephoneBar;
 	private ImageView imgTelephoneDetail;
 	private TextView txtVilleBarDetail;
 	private TextView txtPrixBiereHNDetail;
 	private TextView txtPrixBiereHHDetail;
 	private TextView txtEuroBiereHNDetail;
 	private TextView txtPrixVinHNDetail;
 	private TextView txtPrixVinHHDetail;
 	private TextView txtEuroVinHNDetail;
 	private ImageLoader imageLoader;
 	private TextView txtHHLundi;
 	private TextView txtHHMardi;
 	private TextView txtHHMercredi;
 	private TextView txtHHJeudi;
 	private TextView txtHHVendredi;
 	private TextView txtHHSamedi;
 	private TextView txtHHDimanche;
 	private TextView txtLundi;
 	private TextView txtMardi;
 	private TextView txtMercredi;
 	private TextView txtJeudi;
 	private TextView txtVendredi;
 	private TextView txtSamedi;
 	private TextView txtDimanche;
 	private TextView txtTitreDetail;
 	private TextView txtDetails;
	private ImageView imgCadreDetails;
 	private Bar bar;
 	private int positionBar;
 	private Intent intent;
 	private FavorisDao favorisDao;
 	private FavorisMetier barFavoris;
 	
 	// Pour le swipe
 	private LayoutInflater inflater;	//Used to create individual pages
 	private ViewPager vp;	//Reference to class to swipe views
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_details_bar);
 		
 		// Show the Up button in the action bar.
 		setupActionBar();
 		
 		// On récupère la position du bar cliqué
 		Bundle b = getIntent().getExtras();
 		positionBar = b.getInt("BarSelectionne");
 				
 		// Gestion du swipe
 		//get an inflater to be used to create single pages
 		inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		//Reference ViewPager defined in activity
 		vp=(ViewPager)findViewById(R.id.viewPager);
 		//set the adapter that will create the individual pages
 		vp.setAdapter(new MyPagesAdapter());
 		vp.setCurrentItem(positionBar);
 		
 		vp.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
 			
 			@Override
 			public void onPageSelected(int arg0) {
 			}
 
 			@Override
 			public void onPageScrollStateChanged(int arg0) {
 			}
 
 			@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 			@Override
 			public void onPageScrolled(int arg0, float arg1, int arg2) {
 				// On met à jour le menu à chaque scroll 
 				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 				  invalidateOptionsMenu();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Set up the {@link android.app.ActionBar}, if the API is available.
 	 */
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	private void setupActionBar() {
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 			getActionBar().setDisplayHomeAsUpEnabled(true);
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.menu_details_bar, menu);
 		// Modification du menu
 		MenuItem item = menu.findItem(R.id.action_favoris);
 		bar = Utils.getListeBarsHH().get(vp.getCurrentItem());
 		// On recherche si le bar est un favori
 		//new lectureBddBackgroung().execute();
 		barFavoris = favorisDao.getFavoriByBarId(bar.getBarID());
 		if (null != barFavoris) {
 			item.setIcon(R.drawable.ic_favoris_couleur);
 			bar.setBarFavori(true);
 		} else {
 			item.setIcon(R.drawable.ic_favoris_nb);
 			bar.setBarFavori(false);
 		}
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// This ID represents the Home or Up button. In the case of this
 			// activity, the Up button is shown. Use NavUtils to allow users
 			// to navigate up one level in the application structure. For
 			// more details, see the Navigation pattern on Android Design:
 			//
 			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
 			//
 			NavUtils.navigateUpFromSameTask(this);
 			//finish();
 			break;
 	    case R.id.action_signal_erreur:
 	    	// Proposer un bar : on ouvre la page avec le formulaire
 	    	//bar = Utils.getListeBarsHH().get(vp.getCurrentItem());
 	    	intent = new Intent(DetailsBarActivity.this, PropErreurActivity.class);
 	    	intent.putExtra("origine", Utils.getActivitySignalErreur());
 	    	intent.putExtra("nomBar", bar.getBarNom());
 	    	startActivity(intent);
 	    	break;
 	    case R.id.action_favoris:
 	    	// Ajout (ou suppression) d'un favori dans le fichier
 	    	bar = Utils.getListeBarsHH().get(vp.getCurrentItem());
 	    	if (bar.isBarFavori()) {
 	    		item.setIcon(R.drawable.ic_favoris_nb);
 	    		bar.setBarFavori(false);
 	    		// Suppression du bar de la bdd
 	    		favorisDao.supprimerBarId(bar.getBarID());
 	    	} else {
 	    		item.setIcon(R.drawable.ic_favoris_couleur);
 	    		bar.setBarFavori(true);
 	    		// Ajout du bar dans la bdd
 	    		favorisDao.ajouterBarId(bar.getBarID());
 	    	}
 	    	
 	    default:
 	      break;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	//Implement PagerAdapter Class to handle individual page creation
 	class MyPagesAdapter extends PagerAdapter {
 
 		@Override
 		public int getCount() {
 			if (null != Utils.getListeBarsHH()) {
 				return Utils.getListeBarsHH().size();
 			}
 			return 0;
 		}
 
 		//Create the given page (indicated by position)
 		@Override
 	    public Object instantiateItem(ViewGroup container, int position) {
 	        View page = inflater.inflate(R.layout.activity_swipe_details, null);
 	        // Récupération des champs de l'activité
 			imgBarDetail = (ImageView) page.findViewById(R.id.imgBarDetail);
 			txtNomBarDetail = (TextView) page.findViewById(R.id.txtNomBarDetail);
 			imageLoader = new ImageLoader(DetailsBarActivity.this);
 			txtRueBarDetail = (TextView) page.findViewById(R.id.txtRueBarDetail);
 		    txtCodePostalBarDetail = (TextView) page.findViewById(R.id.txtCodePostalBarDetail);
 		    txtVilleBarDetail = (TextView) page.findViewById(R.id.txtVilleBarDetail);
 		    txtTelephoneBar = (TextView) page.findViewById(R.id.txtTelephoneBar);
 		    imgTelephoneDetail = (ImageView) page.findViewById(R.id.imgTelephoneDetail);
 		    txtPrixBiereHHDetail = (TextView) page.findViewById(R.id.txtPrixBiereHHDetail);
 		    txtPrixBiereHNDetail = (TextView) page.findViewById(R.id.txtPrixBiereHNDetail);
 		    txtEuroBiereHNDetail = (TextView) page.findViewById(R.id.txtEuroBiereHNDetail);
 		    txtPrixVinHHDetail = (TextView) page.findViewById(R.id.txtPrixVinHHDetail);
 		    txtPrixVinHNDetail = (TextView) page.findViewById(R.id.txtPrixVinHNDetail);
 		    txtEuroVinHNDetail = (TextView) page.findViewById(R.id.txtEuroVinHNDetail);
 		    txtHHLundi = (TextView) page.findViewById(R.id.txtHHLundi);
 		    txtHHMardi = (TextView) page.findViewById(R.id.txtHHMardi);
 		    txtHHMercredi = (TextView) page.findViewById(R.id.txtHHMercredi);
 		    txtHHJeudi = (TextView) page.findViewById(R.id.txtHHJeudi);
 		    txtHHVendredi = (TextView) page.findViewById(R.id.txtHHVendredi);
 		    txtHHSamedi = (TextView) page.findViewById(R.id.txtHHSamedi);
 		    txtHHDimanche = (TextView) page.findViewById(R.id.txtHHDimanche);
 		    txtLundi = (TextView) page.findViewById(R.id.txtLundi);
 		    txtMardi = (TextView) page.findViewById(R.id.txtMardi);
 		    txtMercredi = (TextView) page.findViewById(R.id.txtMercredi);
 		    txtJeudi = (TextView) page.findViewById(R.id.txtJeudi);
 		    txtVendredi = (TextView) page.findViewById(R.id.txtVendredi);
 		    txtSamedi = (TextView) page.findViewById(R.id.txtSamedi);
 		    txtDimanche = (TextView) page.findViewById(R.id.txtDimanche);
 		    txtTitreDetail = (TextView) page.findViewById(R.id.txtTitreDetails);
 		    txtDetails = (TextView) page.findViewById(R.id.txtDetails);
		    imgCadreDetails = (ImageView) page.findViewById(R.id.imgCadreDetailsDetail);
 		    
 		    // Alimentation des champs
 		    bar = Utils.getListeBarsHH().get(position);
 		    imageLoader.DisplayImage(bar.getBarUrlImage(), imgBarDetail);
 		    txtNomBarDetail.setText(bar.getBarNom());
 		    txtRueBarDetail.setText(bar.getBarRue());
 		    txtCodePostalBarDetail.setText(bar.getBarCodePostal());
 		    txtVilleBarDetail.setText(bar.getBarVille());
 	        txtTelephoneBar.setText(bar.getBarTelephone());
 	        if ("".equalsIgnoreCase(bar.getBarTelephone())) {
 	        	imgTelephoneDetail.setVisibility(View.INVISIBLE);
 	        }
 	        
 	        // Listener sur le clic du téléphone pour appeler
 	        txtTelephoneBar.setOnClickListener(new View.OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					bar = Utils.getListeBarsHH().get(vp.getCurrentItem());
 				    TelephonyManager telManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
 					if (telManager.getPhoneType() != TelephonyManager.PHONE_TYPE_NONE) {
 						Intent appel = new Intent(Intent.ACTION_DIAL, 
 								//Uri.parse("tel:"+txtTelephoneBar.getText().toString()));
 								Uri.parse("tel:"+bar.getBarTelephone()));
 						startActivity(appel);
 					}
 				}
 			});
 	        
 	        // Listener sur le clic sur l'adresse (envoi vers maps pour navigation)
 	        txtRueBarDetail.setOnClickListener(new View.OnClickListener() {
 				
 				@Override
 				public void onClick(View arg0) {
 					appelIntentMaps();
 				}
 			});
 	        txtCodePostalBarDetail.setOnClickListener(new View.OnClickListener() {
 				
 				@Override
 				public void onClick(View v) {
 					appelIntentMaps();
 				}
 			});
 	        txtVilleBarDetail.setOnClickListener(new View.OnClickListener() {
 				
 				@Override
 				public void onClick(View v) {
 					appelIntentMaps();
 				}
 			});
 	        
 	        // On barre les prix normaux
 		    txtPrixBiereHNDetail.getPaint().setStrikeThruText(true);
 		    txtEuroBiereHNDetail.getPaint().setStrikeThruText(true);
 			txtPrixBiereHNDetail.setText(bar.getBarPrixBiereHN());
 			txtPrixBiereHHDetail.setText(bar.getBarPrixBiereHH());
 			txtPrixVinHNDetail.getPaint().setStrikeThruText(true);
 			txtPrixVinHNDetail.setText(bar.getBarPrixVinHN());
 			txtPrixVinHHDetail.setText(bar.getBarPrixVinHH());
 			txtEuroVinHNDetail.getPaint().setStrikeThruText(true);
 			
 			// Affichage des horaires des Happy hour
 			// On n'affiche que si le bar est en HH ce jour là
 			Resources resources = getResources();
 			if (bar.isBarHHLundi()) {
 				txtHHLundi.setText(String.format(resources.getString(R.string.heuresHH), 
 						bar.getBarDebutHHLundi(), bar.getBarFinHHLundi()));
 				txtLundi.setVisibility(View.VISIBLE);
 				txtHHLundi.setVisibility(View.VISIBLE);
 			} else {
 				txtLundi.setVisibility(View.GONE);
 				txtHHLundi.setVisibility(View.GONE);
 			}
 			if (bar.isBarHHMardi()) {
 				txtHHMardi.setText(String.format(resources.getString(R.string.heuresHH), 
 						bar.getBarDebutHHMardi(), bar.getBarFinHHMardi()));
 				txtMardi.setVisibility(View.VISIBLE);
 				txtHHMardi.setVisibility(View.VISIBLE);
 			} else {
 				txtMardi.setVisibility(View.GONE);
 				txtHHMardi.setVisibility(View.GONE);
 			}
 			if (bar.isBarHHMercredi()) { 
 				txtHHMercredi.setText(String.format(resources.getString(R.string.heuresHH), 
 						bar.getBarDebutHHMercredi(), bar.getBarFinHHMercredi()));
 				txtMercredi.setVisibility(View.VISIBLE);
 				txtHHMercredi.setVisibility(View.VISIBLE);
 			} else {
 				txtMercredi.setVisibility(View.GONE);
 				txtHHMercredi.setVisibility(View.GONE);
 			}
 			if (bar.isBarHHJeudi()) { 
 				txtHHJeudi.setText(String.format(resources.getString(R.string.heuresHH), 
 						bar.getBarDebutHHJeudi(), bar.getBarFinHHJeudi()));
 				txtJeudi.setVisibility(View.VISIBLE);
 				txtHHJeudi.setVisibility(View.VISIBLE);
 			} else {
 				txtJeudi.setVisibility(View.GONE);
 				txtHHJeudi.setVisibility(View.GONE);
 			}
 			if (bar.isBarHHVendredi()) { 
 				txtHHVendredi.setText(String.format(resources.getString(R.string.heuresHH), 
 						bar.getBarDebutHHVendredi(), bar.getBarFinHHVendredi()));
 				txtVendredi.setVisibility(View.VISIBLE);
 				txtHHVendredi.setVisibility(View.VISIBLE);
 			} else {
 				txtVendredi.setVisibility(View.GONE);
 				txtHHVendredi.setVisibility(View.GONE);
 			}
 			if (bar.isBarHHSamedi()) { 
 				txtHHSamedi.setText(String.format(resources.getString(R.string.heuresHH), 
 						bar.getBarDebutHHSamedi(), bar.getBarFinHHSamedi()));
 				txtSamedi.setVisibility(View.VISIBLE);
 				txtHHSamedi.setVisibility(View.VISIBLE);
 			} else {
 				txtSamedi.setVisibility(View.GONE);
 				txtHHSamedi.setVisibility(View.GONE);
 			}
 			if (bar.isBarHHDimanche()) { 
 				txtHHDimanche.setText(String.format(resources.getString(R.string.heuresHH), 
 						bar.getBarDebutHHDimanche(), bar.getBarFinHHDimanche()));
 				txtDimanche.setVisibility(View.VISIBLE);
 				txtHHDimanche.setVisibility(View.VISIBLE);
 			} else {
 				txtDimanche.setVisibility(View.INVISIBLE);
 				txtHHDimanche.setVisibility(View.INVISIBLE);
 			}
 			
 			// Details sur le bar (si existe)
 			if (!"NULL".equalsIgnoreCase(bar.getBarDetails())) {
 				txtTitreDetail.setVisibility(View.VISIBLE);
 				txtDetails.setVisibility(View.VISIBLE);
				imgCadreDetails.setVisibility(View.VISIBLE);
 				txtDetails.setText(bar.getBarDetails());
 			}
 			
 			//Add the page to the front of the queue
 			((ViewPager) container).addView(page, 0);
 			return page;
 		}
 				
 		@Override
 		public boolean isViewFromObject(View arg0, Object arg1) {
 			//See if object from instantiateItem is related to the given view
 			//required by API
 			return arg0==(View)arg1;
 		}
 		
 		@Override
 	    public void destroyItem(ViewGroup container, int position, Object object) {
 	      ((ViewPager) container).removeView((View) object);
 	      object=null;
 	    }
 		
 		
 		/**
 		 * Appel de Gmaps pour la navigation jusqu'au bar
 		 * @param bar
 		 */
 		private void appelIntentMaps() {
 			bar = Utils.getListeBarsHH().get(vp.getCurrentItem());
 		    intent = new Intent(android.content.Intent.ACTION_VIEW, 
 //	    			Uri.parse("geo:" + bar.getBarLatitude() + "," + 
 //	    					bar.getBarLongitude() + "?q=" + bar.getBarLatitude() + "," + 
 //	    					bar.getBarLongitude()));
 	    			Uri.parse("geo:" + bar.getBarLatitude() + "," + bar.getBarLongitude() + 
 	    					"?q=" + bar.getBarNom()));
 	    	startActivity(intent);
 		}
 	}
 	
 	private class accesBddBackgroung extends AsyncTask<Void, Void, Void> {
 
 		@Override
 		protected Void doInBackground(Void... params) {
 			// Ouverture bdd
 			favorisDao = new FavorisDao(DetailsBarActivity.this);
 			favorisDao.open();
 			return null;
 		}
 	}
 	
 	@Override
 	protected void onResume() {
 		// Ouverture de la bdd
 		new accesBddBackgroung().execute();
 		super.onResume();
 	}
 	
 	@Override
 	  protected void onPause() {
 	    favorisDao.close();
 	    super.onPause();
 	  }
 }
