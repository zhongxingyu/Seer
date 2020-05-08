 package com.gourmet6;
 
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.app.NavUtils;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.RatingBar;
 import android.widget.RatingBar.OnRatingBarChangeListener;
 import android.widget.TextView;
 
 
 public class RestaurantActivity extends Activity
 {
 	
 	private Gourmet g;
 	private Restaurant currentRest;
	private String currentTown;
 	
 	private LinearLayout listImg;
 	
 	private RatingBar ratingBar;
 	private RatingBar toRateBar;
 	
 	private TextView description;
 	private TextView horaire;
 	private TextView localisation;
 	private TextView tvRate;
 	private TextView tvTel;
 	private TextView tvMail;
 	private TextView tvWeb;
 	private TextView tvCatPrice;
 	private TextView tvSeats;
 	private TextView tvInfoRate;
 	
 	private Button order;
 	private Button reserve;
 	private Button menu;
 	
 	private ImageView imgDesc;
 	
 	private boolean extended;
 	private boolean hasRated;
 	
 	private Dialog dialog;
 	
 	private DBHandler dbh;
 	
 	private Context context;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_restaurant);
 		MainGourmet.isDone = false;
		currentTown = MainGourmet.currentTown;
 		overridePendingTransition(0, R.anim.commetuveux);
 		// Show the Up button in the action bar.
 		setupActionBar();
 		
 		g = (Gourmet)getApplication();
 		context = this;
 		dbh = new DBHandler(context);
 		
 		hasRated = false;
 		
 		currentRest = g.getRest();
 		setTitle(this.currentRest.getName().split("_")[0]);
 		
 		//Image
 		listImg = (LinearLayout) findViewById(R.id.listImgRest);
 		addImage();
 		
 		//rating bar
 		ratingBar = (RatingBar) findViewById(R.id.ratingRest);
 		ratingBar.setIsIndicator(true);
 		if(g.getClient()!=null){
 			ratingBar.setOnTouchListener(new View.OnTouchListener() {
 				@Override
 				public boolean onTouch(View v, MotionEvent event) {
 					if(!hasRated)
 					{
 						createDialogToRate();
 					}
 					return false;
 				}
 			});
 		}
 		
 		imgDesc = (ImageView) findViewById(R.id.imageViewDesc);
 		imgDesc.setClickable(true);
 		imgDesc.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				checkSizeDesc();
 			}
 		});
 		
 		//TextView
 		tvInfoRate = (TextView) findViewById(R.id.tvInfoRate);
 		actualizeInfoRate();
 		
 		description = (TextView) findViewById(R.id.descriptionRest);
 		description.setText(currentRest.getDescription());
 		description.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				checkSizeDesc();
 			}
 		});
 		
 		horaire = (TextView) findViewById(R.id.horaireRest);
 		setHorair();
 		
 		localisation = (TextView) findViewById(R.id.localisationRest);
 		setLocalisation();
 		
 		if(currentRest.getTel()!=null){
 			tvTel = (TextView) findViewById(R.id.tvTelRest);
 			tvTel.setText(currentRest.getTel());
 		}
 		if(currentRest.getMail()!=null){
 			tvMail = (TextView) findViewById(R.id.tvMailRest);
 			tvMail.setText(currentRest.getMail());
 		}
 		if(currentRest.getWeb()!=null){
 			tvWeb = (TextView) findViewById(R.id.tvWebRest);
 			tvWeb.setText(currentRest.getWeb());
 		}
 		if(currentRest.getPriceCat()!=0){
 			tvCatPrice = (TextView) findViewById(R.id.tvPriceCat);
			tvCatPrice.setText(Dish.myRound(currentRest.getPriceCat())+" �");
 		}
 		if(currentRest.getSeats()!=0){
 			tvSeats = (TextView) findViewById(R.id.tvPlaceRest);
 			tvSeats.setText(currentRest.getSeats()+"");
 		}
 		
 		//Button
 		//Reaction du bouton de commande
 		order = (Button) findViewById(R.id.commandeInRest);
 		order.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				currentRest.createListDishes(new DBHandler(RestaurantActivity.this));
 				Intent commande = new Intent(RestaurantActivity.this, OrderActivity.class);
 				commande.putExtra("from", true);
 				startActivity(commande);
 			}
 		});
 		
 		//Reaction du bouton de reservation
 		reserve = (Button) findViewById(R.id.reserveInRest);
 		reserve.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Intent reserv = new Intent(RestaurantActivity.this, ReservationActivity.class);
 				reserv.putExtra("from", false);
 				startActivity(reserv);
 			}
 		});
 		
 		//Reaction du bouton menu
 		menu = (Button) findViewById(R.id.menuInRest);
 		menu.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				currentRest.createListDishes(new DBHandler(RestaurantActivity.this));
 				Intent dishes = new Intent(RestaurantActivity.this, DishMenuActivity.class);
 				startActivity(dishes);
 			}
 		});
 		if(g.getClient()==null)
 		{
 			order.setEnabled(false);
 			reserve.setEnabled(false);
 		}
 	}
 	
 	private void actualizeInfoRate()
 	{
 		ratingBar.setRating((float) currentRest.getRating());
 		tvInfoRate.setText(Dish.myRound(currentRest.getRating())+"/5 by "
 				+currentRest.getNbrPrsHasVoted()+" people");
 	}
 
 	public void checkSizeDesc()
 	{
 		LayoutParams params = description.getLayoutParams();
 		if(extended){
 			params.height = 77;
 			imgDesc.setImageResource(android.R.drawable.arrow_down_float);
 			extended = false;
 		}
 		else{
 			params.height = (currentRest.getDescription().length()*params.width)+20;
 			imgDesc.setImageResource(android.R.drawable.arrow_up_float);
 			extended = true;
 		}
 		description.setLayoutParams(params);
 	}
 	
 	private void addImage()
 	{
 		String nameImg = Restaurant.getNameImg(currentRest.getName().split("_")[0]);
 		ImageView img;
 		for(int i=0; i<currentRest.getNbrImage(); i++)
 		{
 			img = new ImageView(context);
 			img.setImageResource(getResources().getIdentifier(nameImg+i, "drawable", getPackageName()));
 			listImg.addView(img);
 		}
 		
 	}
 
 	private void setLocalisation()
 	{
 		String res = "";
 		if(currentRest.getAdress()!=null){
 			res = res+currentRest.getAdress()+"\n";
 		}
 		if(currentRest.getZip()!=0){
 			res = res+currentRest.getZip()+" ";
 		}
 		if(currentRest.getTown()!=null){
 			res = res+ currentRest.getTown();
 		}
 		if(res.length()!=0){
 			localisation.setText(res);
 		}
 	}
 
 	private void setHorair()
 	{
 		if(currentRest.getSemaine()==null) return;
 		String s = currentRest.getHoraireInString();
 		if(s.length()!=0)horaire.setText(s);
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
 	
 	private void createDialogToRate()
 	{
 		dialog = new Dialog(context);
 		dialog.setContentView(R.layout.rating_dialog);
 		dialog.setTitle("Rate restaurant");
 		
 		tvRate = (TextView) dialog.findViewById(R.id.typeCotation);
 		lookForType((int)ratingBar.getRating());
 		
 		toRateBar = (RatingBar) dialog.findViewById(R.id.ratingBarToRate);
 		toRateBar.setRating(ratingBar.getRating());
 		toRateBar.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {
 			@Override
 			public void onRatingChanged(RatingBar ratingBar, float rating,
 					boolean fromUser) {
 				System.out.println(rating);
 				lookForType((int)rating);
 			}
 		});
 		
 		Button okButtonRate = (Button) dialog.findViewById(R.id.okButtonRate);
 		okButtonRate.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				currentRest.rateRestaurant(toRateBar.getRating(), dbh);
 				hasRated = true;
 				actualizeInfoRate();
 				dialog.dismiss();
 			}
 		});
 		
 		Button cancelButtonRate = (Button) dialog.findViewById(R.id.cancelButtonRate);
 		cancelButtonRate.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				dialog.cancel();
 			}
 		});
 		
 		dialog.show();
 	}
 	
 	public void lookForType(int rating)
 	{
 		switch(rating)
 		{
 		case 0: 
 			tvRate.setText("Mediocre"); 
 			break;
 		case 1: 
 			tvRate.setText("Mauvais"); 
 			break;
 		case 2: 
 			tvRate.setText("Moyen");
 			break;
 		case 3: 
 			tvRate.setText("Bon");
 			break;
 		case 4: 
 			tvRate.setText("Tres bon");
 			break;
 		case 5: 
 			tvRate.setText("Excellent");
 			break;
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
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
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 	
 	@Override
 	public void onBackPressed()
 	{
 		MainGourmet.showResto = true;
		MainGourmet.currentTown = currentTown;
 		super.onBackPressed();
	}	
 }
