 package ws.wiklund.guides.activities;
 
 import java.util.Date;
 
 import ws.wiklund.guides.R;
 import ws.wiklund.guides.db.BeverageDatabaseHelper;
 import ws.wiklund.guides.model.Beverage;
 import ws.wiklund.guides.model.Country;
 import ws.wiklund.guides.model.Producer;
 import ws.wiklund.guides.model.Provider;
 import ws.wiklund.guides.model.Rating;
 import ws.wiklund.guides.util.ViewHelper;
 import ws.wiklund.guides.util.facebook.FacebookConnector;
 import ws.wiklund.guides.util.facebook.SessionEvents;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.RatingBar;
 import android.widget.TextView;
 
 public abstract class BeverageActivity extends BaseActivity {
 	private FacebookConnector connector;
 	private Beverage beverage;
 	private boolean isNew;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.beverage);
         
 		connector = new FacebookConnector(this);
 		
         beverage = (Beverage) getIntent().getSerializableExtra("ws.wiklund.guides.model.Beverage");
 		Log.d(BeverageActivity.class.getName(), "Beverage: " + beverage);
 
 		populateUI();
         isNew = true;
     }
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.beverage_menu, menu);
 
 		return true;
 	}
 
 	@Override
 	public void onResume() {
 		if(isNew) {
 			isNew = false;
 		} else {
 			//Update the beverage because the data might have changed
 			Beverage b = getDatabaseHelper().getBeverage(beverage.getId());
 			if(!b.equals(beverage)) {
 				beverage = b;
 				populateUI();
 			}
 		}
 		
 		connector.getFacebook().extendAccessTokenIfNeeded(this, null);
 
 		super.onResume();
 	}
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		connector.getFacebook().authorizeCallback(requestCode, resultCode, data);
		
		super.onActivityResult(requestCode, resultCode, data);
 	}	
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if (item.getItemId() == R.id.menuShareOnFacebook) {
 			if (connector.getFacebook().isSessionValid()) {
 				connector.postMessageOnWall(beverage);
 			} else {
 				SessionEvents.addAuthListener(new SessionEvents.AuthListener() {
 					@Override
 					public void onAuthSucceed() {
 						connector.postMessageOnWall(beverage);
 					}
 					
 					@Override
 					public void onAuthFail(String error) {
 						Log.d(BeverageActivity.class.getName(), "FacebookPostMessageTask onAuthFail: " + error);
 					}
 				});
 
 				connector.login();
 			}
 			return true;
 		} else if (item.getItemId() == R.id.menuRateBeverage) {
 			final Dialog viewDialog = new Dialog(this);
 			viewDialog.setTitle(R.string.rate);
 			LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			View dialogView = li.inflate(R.layout.rating, null);
 			viewDialog.setContentView(dialogView);
 			viewDialog.show();
 			final RatingBar rating = (RatingBar) dialogView.findViewById(R.id.dialogRatingBar);
 			if(beverage != null && beverage.getRating() != -1) {
 				rating.setRating(beverage.getRating());
 			}
 			final Button okBtn = (Button) dialogView.findViewById(R.id.ratingOk);
 			final Button cancelBtn = (Button) dialogView.findViewById(R.id.ratingCancel);
 			okBtn.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					Rating r = Rating.fromFloat(rating.getRating()); 
 					beverage.setRating(r.getRating());
 				   	getDatabaseHelper().addBeverage(beverage);
 
 					RatingBar rating = (RatingBar) findViewById(R.id.ratingBar);
 					rating.setRating(r.getRating());
 			    	viewDialog.dismiss();
 				}
 			});
 			cancelBtn.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 			    	viewDialog.dismiss();
 				}
 			});
 			return true;
 		} else if (item.getItemId() == R.id.menuUpdateBeverage) {
 			Intent intent = new Intent(getApplicationContext(), getModifyBeverageActivityClass());
 			intent.putExtra("ws.wiklund.guides.model.Beverage", beverage);
			startActivity(intent);
 			return true;
 		}
 		
 		return false;
 	}	
 
 	private void populateUI() {
 		setTitle(beverage.getName());
 		
 		ViewHelper.setThumbFromUrl((ImageView) findViewById(R.id.Image_thumbUrl), beverage.getThumb());
 		
 		if (beverage.getNo() != -1) {
 			TextView no = (TextView) findViewById(R.id.Text_no);
 			no.setText(String.valueOf(beverage.getNo()));
 		}
 		
 		TextView type = (TextView) findViewById(R.id.Text_type);
 		
 		ViewHelper.setText(type, beverage.getBeverageType().getName());
 
 		Country c = beverage.getCountry();
 		if (c != null) {
 			ViewHelper.setCountryThumbFromUrl((ImageView) findViewById(R.id.Image_country_thumbUrl), c);
 			TextView country = (TextView) findViewById(R.id.Text_country);
 			ViewHelper.setText(country, c.getName());
 		}
 
 		if (beverage.getYear() != -1) {
 			TextView year = (TextView) findViewById(R.id.Text_year);
 			ViewHelper.setText(year, String.valueOf(beverage.getYear()));
 		} else {
 			findViewById(R.id.lbl_year).setVisibility(View.GONE);
 			findViewById(R.id.Text_year).setVisibility(View.GONE);
 		}
 		
 		Producer p = beverage.getProducer();
 		if (p != null) {
 			TextView producer = (TextView) findViewById(R.id.Text_producer);
 			ViewHelper.setText(producer, p.getName());
 		}
 
 		if (beverage.getStrength() != -1) {
 			TextView strength = (TextView) findViewById(R.id.Text_strength);
 			ViewHelper.setText(strength, String.valueOf(beverage.getStrength()) + " %");
 		}
 		
 		if (beverage.hasPrice()) {
 			TextView label = (TextView) findViewById(R.id.label_price);
 			label.setVisibility(View.VISIBLE);
 
 			TextView price = (TextView) findViewById(R.id.Text_price);
 			ViewHelper.setText(price, ViewHelper.formatPrice(beverage.getPrice()));
 		} else {
 			TextView price = (TextView) findViewById(R.id.label_price);
 			price.setVisibility(View.GONE);
 		}
 		
 		if (beverage.hasBottlesInCellar()) {
 			TextView cellar = (TextView) findViewById(R.id.Text_cellar);
 			cellar.setText(String.format(getString(R.string.bottles_in_cellar), new Object[]{
 						String.valueOf(beverage.getBottlesInCellar()),
 						ViewHelper.formatPrice(beverage.getPrice() * beverage.getBottlesInCellar())}));
 		}
 		
 		TextView usage = (TextView) findViewById(R.id.Text_usage);
 		ViewHelper.setText(usage, beverage.getUsage());
 
 		TextView taste = (TextView) findViewById(R.id.Text_taste);
 		ViewHelper.setText(taste, beverage.getTaste());
 
 		Provider p1 = beverage.getProvider();
 		if (p1 != null) {
 			TextView provider = (TextView) findViewById(R.id.Text_provider);
 			ViewHelper.setText(provider, p1.getName());
 		}
 
 		TextView comment = (TextView) findViewById(R.id.Text_comment);
 		ViewHelper.setText(comment, beverage.getComment());
 		
 		RatingBar rating = (RatingBar) findViewById(R.id.ratingBar);
 		rating.setRating(beverage.getRating());
 
 		TextView added = (TextView) findViewById(R.id.Text_added);
 		
 		added.setText(ViewHelper.getDateAsString((beverage.getAdded() != null ? beverage.getAdded() : new Date())));
 	}
 	
 	protected abstract BeverageDatabaseHelper getDatabaseHelper();
 	protected abstract Class<?> getModifyBeverageActivityClass();
 
 }
