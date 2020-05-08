 package edu.depaul.snotg_android.Map;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.widget.Toast;
 
 import com.google.android.maps.MapView;
 import com.google.android.maps.OverlayItem;
 
 import edu.depaul.snotg_android.Activity.MyProfileActivity;
 import edu.depaul.snotg_android.Activity.OtherProfileActivity;
 import edu.depaul.snotg_android.Activity.Snotg_androidActivity;
 import edu.depaul.snotg_android.Profile.UserProfile;
 
 
 public class ProfileOverlay extends BalloonItemizedOverlay<OverlayItem>{
 	
 	private ArrayList<OverlayItem> profileList = new ArrayList<OverlayItem>();
 	private Context c;
 	
 
 	//Constructor for the ProfileOverlay class
 	public ProfileOverlay(Drawable image, MapView mapView) {
 		super(boundCenter(image), mapView);
 		c = mapView.getContext();
 	}
 	
 	public void drawTop(Drawable image){
 		return;
 	}
 	
 	public void addOverlay(OverlayItem overlay){
 		profileList.add(overlay);
 		populate();
 	}
 
 	//Creates an item into the profileList Overlay array
 	@Override
 	protected OverlayItem createItem(int i) {
 		return profileList.get(i);
 	}
 
 	//This returns the size of the profileList Overlays
 	@Override
 	public int size() {
 		return profileList.size();
 	}
 	
 	//remove an item if necessary
 	public void removeItem(int i){
 		profileList.remove(i);
 		populate();
 	}
 	
 	@Override
 	protected boolean onBalloonTap(int index, OverlayItem item) {
 		Toast.makeText(c, "Retrieving profile for index " + index,
 				Toast.LENGTH_LONG).show();
 		Intent other = new Intent( c, OtherProfileActivity.class);
 		UserProfile up = new UserProfile();
 		up.setShout("Help me with my SE450 homework!");
 		up.setDescription("I'm a grad student at DePaul in the school of Computing and Digital Media");
 		Bundle b = new Bundle();
 		b.putSerializable("profile", up);
 		other.putExtras(b);
		Intent m = new Intent(c,OtherProfileActivity.class);
		c.startActivity(m);
 		return true;
 	}
 	
 	
 }
