 package cs585_hw3.team33.manage;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import cs585_hw3.team33.R;
 import cs585_hw3.team33.lib.ProgressRunnable;
 
 public class ManageActivity extends Activity {
 	public void onCreate(Bundle savedInstanceState) {
     	super.onCreate(savedInstanceState);
         setContentView(R.layout.manage);
         
         ((Button)findViewById(R.id.CreateButton))
         	.setOnClickListener( createListener );
         ((Button)findViewById(R.id.PopulateButton))
 	    	.setOnClickListener( popListener );
         ((Button)findViewById(R.id.DropButton))
 	    	.setOnClickListener( dropListener );
         
 	}
 	
 	public void createDB() {
 		// fill in
 		try{
             Thread.sleep(1000); // We need to remove this before we submit.
 		 } catch (Exception e) { 
         }
 	}
 	public void populateDB() {
 		// fill in
 		try{
             Thread.sleep(1000); // We need to remove this before we submit.
 		 } catch (Exception e) { 
         }
 	}
 	public void dropDB() {
 		// fill in
 		try{
             Thread.sleep(1000); // We need to remove this before we submit.
 		 } catch (Exception e) { 
         }
 	}
 	
 	
 
 	Activity me = this;
 	private OnClickListener createListener = new OnClickListener() {
 		public void onClick(View v) {
 			ProgressRunnable getResults = 
 				new ProgressRunnable("Please wait...", "Creating database ...") {
 					public void onGo() {
 						createDB();
 					}
 					public void onEnd() {			            					
 					}
 			};
 			getResults.startThread(me,"BackgroundManageDB");
 		}
 	};
 	private OnClickListener popListener = new OnClickListener() {
 		public void onClick(View v) {
 			ProgressRunnable getResults = 
 				new ProgressRunnable("Please wait...", "Populating database ...") {
 					public void onGo() {
 						populateDB();
 					}
 					public void onEnd() {			            					
 					}
 			};
 			getResults.startThread(me,"BackgroundManageDB");
 		}
 	};		
 	private OnClickListener dropListener = new OnClickListener() {
 		public void onClick(View v) {
 			ProgressRunnable getResults = 
 				new ProgressRunnable("Please wait...", "Dropping database ...") {
 					public void onGo() {
 						dropDB();
 					}
 					public void onEnd() {			            					
 					}
 			};
 			getResults.startThread(me,"BackgroundManageDB");
 		}
 	};	
 	
 }
