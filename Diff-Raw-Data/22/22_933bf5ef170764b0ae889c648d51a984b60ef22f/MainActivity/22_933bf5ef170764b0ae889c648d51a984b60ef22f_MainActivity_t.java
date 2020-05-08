 package com.vidit;
 
 
 
 
 import java.io.File;
 import android.support.v4.app.Fragment;
 import android.os.Bundle;
 import android.os.Environment;
 import android.view.Menu;
 import android.view.MenuItem;
 
 import com.facebook.*;
 import com.vidit.R;
 
 public class MainActivity extends FacebookActivity {
 	
 	private Fragment loginFragment;
 	private boolean isResumed=false;
 	private Session session;
	private MenuItem logInOut;
 	private SessionState sessionState;
 	@Override
 	public void onCreate(Bundle savedInstanceState) 
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		if(savedInstanceState==null)
 		{
 			loginFragment=new LogFragment();
 			getSupportFragmentManager().beginTransaction()
 			.add(android.R.id.content, loginFragment).commit();
 		}
 		else
 		{
 			loginFragment=(LogFragment)getSupportFragmentManager()
 					.findFragmentById(android.R.id.content);
 			
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) 
 	{
 		// Inflate the menu; this adds items to the action bar if it is present.
		logInOut=(MenuItem)findViewById(R.id.log_out);
		/*if(this.session==null)
			logInOut.setVisible(false);
		else
			logInOut.setVisible(true);*/
 		getMenuInflater().inflate(R.menu.vidit_menu, menu);
 		return true;
 	}
 	
 	@Override
     public boolean onOptionsItemSelected(MenuItem item)
     {
  
         switch (item.getItemId())
         {
         	case R.id.log_out:
        		this.session.closeAndClearTokenInformation();
        		return true;
        		
        	case R.id.exit:
        		this.finish();
         		return true;
         		
         	default:
                 return super.onOptionsItemSelected(item);
         }
     }
 	
 	@Override
 	public void onResume()
 	{
 	    super.onResume();
 	    isResumed = true;
 	}
 
 	@Override
 	public void onPause() 
 	{
 	    super.onPause();
 	    isResumed = false;
 	}
 	
 	@Override
 	protected void onSessionStateChange(SessionState state, Exception exception) 
 	{
 	    sessionState=state;
 		super.onSessionStateChange(state, exception);
 	    if (isResumed) {
 	    	
 	    	((LogFragment) loginFragment).onSessionStateChange(state, exception,this.getSession());
 	    }
 	}
 	
 	@Override
 	protected void onResumeFragments() 
 	{
 	    super.onResumeFragments();
 
 	    session = Session.getActiveSession();
 	    if (session != null && (session.isOpened() || session.isClosed()) ) 
 	    {
 	        onSessionStateChange(session.getState(), null);
 	    }
 	}
 	
 	/*@Override
 	protected void onStop()
 	{
 		super.onStop();
 		File storagePath = new File(Environment.getExternalStorageDirectory().toString() + "/FidVids");
 		boolean delDirectory=deleteDirectory(storagePath);
     }
 	
 	
 	//Delete the directory where images are saved when the app is exited
 	static public boolean deleteDirectory(File path) {
 	    if( path.exists() ) {
 	      File[] files = path.listFiles();
 	      if (files == null) {
 	          return true;
 	      }
 	      for(int i=0; i<files.length; i++) {
 	         if(files[i].isDirectory()) {
 	           deleteDirectory(files[i]);
 	         }
 	         else {
 	           files[i].delete();
 	         }
 	      }
 	    }
 	    return( path.delete() );
 	  }*/
 }
 
 
