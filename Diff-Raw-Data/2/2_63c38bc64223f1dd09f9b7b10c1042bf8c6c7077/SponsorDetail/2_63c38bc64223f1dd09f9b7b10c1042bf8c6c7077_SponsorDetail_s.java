 package org.windycityrails.activities;
 
 import org.windycityrails.Constants;
 import org.windycityrails.R;
 import org.windycityrails.WindyCityRailsApplication;
 import org.windycityrails.model.Sponsor;
 import org.windycityrails.ui.WebImageView;
 import org.windycityrails.util.Network;
 
 import roboguice.activity.RoboActivity;
 import roboguice.inject.InjectView;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class SponsorDetail extends RoboActivity {
 
 	private static final String CLASSTAG = SponsorDetail.class.getSimpleName();
 	@InjectView(R.id.sponsor_image_detail) private WebImageView logo;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		Log.v(Constants.LOGTAG, " " + SponsorDetail.CLASSTAG + " onCreate");
 
 		WindyCityRailsApplication app = (WindyCityRailsApplication) getApplication();
 		final Sponsor sponsor = app.getCurrentSponsor();
 
 		setContentView(R.layout.sponsor_detail);
 
 		TextView name = (TextView) findViewById(R.id.sponsor_name_detail);
 		name.setText("About " + sponsor.name);
 
 		TextView description = (TextView) findViewById(R.id.sponsor_description_detail);
 		description.setText(sponsor.description);
 
		Button url = (Button) findViewById(R.id.sponsor_url_detail);
 		url.setOnClickListener(new TextView.OnClickListener() {
 
 			public void onClick(View v) {
 				Intent i = new Intent(Intent.ACTION_VIEW, Uri
 						.parse(sponsor.url));
 				startActivity(i);
 			}
 		});
 
 		if (Network.isNetworkAvailable(this) && sponsor.logo != null
 				&& sponsor.logo != "") {
 			logo.setImageUrl(sponsor.logo);
 			logo.loadImage();
 		}
 	}
 }
