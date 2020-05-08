 package com.brousalis;
 
 import android.app.Activity;
 import android.os.Bundle;
import android.widget.TextView;
 
 public class ItemDetails extends Activity {
 	
 	private Bundle _extras;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.setContentView(R.layout.item_details);
 		_extras = this.getIntent().getExtras();
 		_extras.get("title");
 		_extras.get("summary");
		//TextView changelog = (TextView)updateNeeded.findViewById(R.id.changelog);
		//changelog.setText(BetaChecker.getHTTPData(this.getString(R.string.beta_user_log_url)));
		TextView title = (TextView)this.findViewById(R.id.detail_title);
		TextView summary = (TextView)this.findViewById(R.id.detail_summary);
		//TextView condition = (TextView)this.findViewById(R.id.detail_condition);
		
		title.setText(_extras.get("title").toString());
		summary.setText(_extras.get("summary").toString());
		//TODO: Conditions aren't implemented for a trail scale in the XML yet.  Do that, then this
		// Line gets uncommented.
		//condition.setText(_extras.get("title").toString());
 	}
 }
