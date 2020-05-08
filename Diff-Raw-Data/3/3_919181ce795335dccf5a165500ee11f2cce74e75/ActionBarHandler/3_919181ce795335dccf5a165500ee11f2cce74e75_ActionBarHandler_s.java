 package com.connectsy;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageView;
 
 import com.connectsy.events.EventNew;
 
 public class ActionBarHandler implements OnClickListener{
 	private Activity activity;
 	
 	public ActionBarHandler(Activity a){
 		activity = a;
 		
 		ImageView logo = (ImageView)a.findViewById(R.id.ab_logo);
		logo.setOnClickListener(this);
 	}
 	
 	public void onClick(View abAction) {
 		if (abAction.getId() == R.id.ab_new_event){
     		activity.startActivity(new Intent(activity, EventNew.class));
     	} else if (abAction.getId() == R.id.ab_logo) {
     		activity.startActivity(new Intent(activity, Dashboard.class));
     	}
 	}
 
 }
