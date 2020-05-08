 package cs585_hw3.team33.post;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 import com.google.android.maps.GeoPoint;
 
 import cs585_hw3.team33.MainActivity;
 import cs585_hw3.team33.R;
 import cs585_hw3.team33.lib.ProgressRunnable;
 import cs585_hw3.team33.lib.ObservableLocationListener.LocationObserver;
 
 public class PostActivity extends Activity implements LocationObserver {
 
 	public void onCreate(Bundle savedInstanceState) {
     	super.onCreate(savedInstanceState);
         setContentView(R.layout.post);
         
         Button button = (Button)findViewById(R.id.PostButton);
         button.setOnClickListener( submitListener );
         ((MainActivity)me.getParent()).locationListener.subscribe(this);
 	}
 	
	public void OnResume() {
 		((MainActivity)me.getParent()).locationListener.subscribe(this);
 	}
 	
 	public void OnPause() {
 		((MainActivity)me.getParent()).locationListener.unsubscribe(this);
 	}
 	@Override
 	public void locationChanged(GeoPoint p) {
 		((TextView)findViewById(R.id.CoordText))
			.setText("( "+p.getLatitudeE6()+", "+p.getLongitudeE6()+" )");
 	}
 	
 	public void submitPost(ProgressRunnable pr) {
 
         String s = ((EditText)findViewById(R.id.PostText)).getText().toString();
 		System.out.println(s);
 		int x=0,y=0;
 		
 		MainActivity m = ((MainActivity)this.getParent());
 		
 		if (m.current_loc != null) {
 			x = m.current_loc.getLatitudeE6();
 			y = m.current_loc.getLongitudeE6();
 		} else {
 			pr.reportToast("No location found; using 0,0.");
 		}
 		 
 		if (m.dh.isOpen()) 
 			m.dh.insert(x,y,s);			
 		
 			
 		
 		try{
             Thread.sleep(1000); // We need to remove this before we submit.
 		 } catch (Exception e) { 
         }
 	}
 	
 	Activity me = this;
 	private OnClickListener submitListener = new OnClickListener() {
 		public void onClick(View v) {
 			ProgressRunnable getResults = 
 				new ProgressRunnable("Please wait...", "Submitting blog post ...") {
 					public void onGo() {
 						submitPost(this);
 					}
 					public void onEnd() {	
 						((EditText)findViewById(R.id.PostText))
 							.setText("");
 					}
 			};
 			getResults.startThread(me,"BackgroundManageDB");
 		}
 	};
 }
