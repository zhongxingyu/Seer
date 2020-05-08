 package nz.gen.wellington.guardian.android.activities;
 
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.TimeZone;
 
 import nz.gen.wellington.guardian.android.R;
 import nz.gen.wellington.guardian.android.model.AboutArticleSet;
 import nz.gen.wellington.guardian.android.model.ArticleSet;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class about extends ArticleListActivity {
 		
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);        
 		setContentView(R.layout.about);
 		setHeading("Guardian Lite - About");
 				
 		TextView description = (TextView) findViewById(R.id.About);		
 		description.setText("This unofficial application was developed by Tony McCrae of Eel Pie Consulting Limited.\n\n" +
 				"Articles are retreived from the Guardian's RSS feeds. Tag information is supplied by the Guardian Content API.\n\n" +
 				"For more information see:\nhttp://eelpieconsulting.co.uk/guardianlite\n\n" +
				"Application © 2010 Eel Pie Consulting Limited\n"
 				);
 				
 		ImageView poweredByTheGuardian = (ImageView) findViewById(R.id.PoweredByTheGuardian);
 		poweredByTheGuardian.setImageResource(R.drawable.poweredbyguardian);
 	}
 	
 	@Override
 	protected void onStart() {
 		super.onStart();
 		populateSplashImage();
 	}
 	
 	@Override
 	protected ArticleSet getArticleSet() {
 		return new AboutArticleSet();
 	}
 
 	@Override
 	protected String getRefinementDescription(String refinementType) {
 		return null;
 	}
 	
 	
 	public boolean onCreateOptionsMenu(Menu menu) {
 		menu.add(0, 1, 0, "Home");
 		menu.add(0, 5, 0, "Refresh");
 	    return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case 1:
 			switchToMain();
 			return true;
 		case 5:
 			refresh();
 			return true;
 		}
 		return false;
 	}
 	
 	private void populateSplashImage() {
 		ImageView slashImage = (ImageView) findViewById(R.id.SplashImage);
 		slashImage.setImageResource(R.drawable.kingsplace_night);
 		if (isDaylightInLondon()) {
 			slashImage.setImageResource(R.drawable.kingsplace);
 		} else {
 			slashImage.setImageResource(R.drawable.kingsplace_night);
 		}
 	}
 	
 	private boolean isDaylightInLondon() {
 		Calendar londonCal = GregorianCalendar.getInstance(TimeZone.getTimeZone("Europe/London"));
 		int londonHour = londonCal.get(Calendar.HOUR_OF_DAY);
 		return londonHour > 6 && londonHour < 21;
 	}
 	
 }
