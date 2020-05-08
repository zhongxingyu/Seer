 package net.nakama.duckdroid.ui;
 
 import net.nakama.duckdroid.R;
 import net.nakama.duckdroid.R.layout;
 import net.nakama.duckdroid.R.menu;
 import net.nakama.duckdroid.ui.listeners.EventState;
 import net.nakama.duckdroid.ui.listeners.ListSelectedListener;
 import net.nakama.duckdroid.ui.listeners.ThreadCompletedListener;
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.view.Menu;
 
 public class Duckdroid extends FragmentActivity implements ListSelectedListener, ThreadCompletedListener {
 
 	private static final String TAG = "Duckdroid";
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_duckdroid);
         
         
         /*
          if (findViewById(R.id.fragment_container) != null) {
         	
         	Fragment mf = new MyListFrament(false);
         	mf.setArguments(getIntent().getExtras());
         	
         	getFragmentManager().beginTransaction().add(R.id.fragment_container, mf).commit();	
         } 
          */
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_duckdroid, menu);
         return true;
     }
 
     
 	/* (non-Javadoc)
 	 * @see net.nakama.duckdroid.ui.listeners.ListSelectedListener#onListSelected(int)
 	 */
 	@Override
 	public void onListSelected(int position) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/* (non-Javadoc)
 	 * @see net.nakama.duckdroid.ui.listeners.ThreadCompletedListener#onThreadCompleted(net.nakama.duckdroid.ui.listeners.EventState, java.lang.Object)
 	 */
 	@Override
 	public void onThreadCompleted(EventState state, Object result) {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
