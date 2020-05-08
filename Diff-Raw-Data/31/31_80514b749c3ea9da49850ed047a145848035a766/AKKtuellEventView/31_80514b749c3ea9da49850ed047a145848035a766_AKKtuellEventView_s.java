 package org.akk.akktuell.Activity;
 
 import org.akk.akktuell.R;
 import org.akk.akktuell.toolkit.Tools;
 
 import android.app.Activity;
 import android.content.Intent;
import android.net.Uri;
 import android.os.Bundle;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class AKKtuellEventView extends Activity {
	
		private Tools tools;
	 @Override
	 public void onCreate(Bundle savedInstanceState) {
	     super.onCreate(savedInstanceState);
	     this.tools = Tools.getInstance(this);
	     setupView();
	 }
 
 	private void setupView() {
 		if (tools.isInLandscapeMode()) {
 			setContentView(R.layout.simple_event_view_landscape);
 		} else {
 			setContentView(R.layout.simple_event_view);
 		}
 		Intent intent = getIntent();
 		TextView eventName = (TextView) findViewById(R.id.simple_event_view_eventname);
 		TextView eventDate = (TextView) findViewById(R.id.simple_event_view_eventdate);
 		TextView eventDescription = (TextView) findViewById(R.id.simple_event_view_eventdescription);
 		ImageView eventImage = (ImageView) findViewById(R.id.simple_event_view_eventimage);
 		eventName.setText(intent.getStringExtra("EVENT_NAME"));
 		eventDate.setText(intent.getStringExtra("EVENT_DATE"));
 		eventDescription.setText(intent.getStringExtra("EVENT_DESCRIPTION"));
		//Uri eventPicUri = (Uri) intent.getSerializableExtra("EVENT_PICTURE_URI");
		//eventImage.setImageURI(eventPicUri);
 	}
 }
