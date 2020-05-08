 package ie.ucd.asteroid;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.os.Handler;
 import android.widget.ProgressBar;
 
 
 public class MainActivity extends Activity {
 
 	private DownloadAdapter da = new DownloadAdapter(this); // Create a new object of DownloadAdapter
 	private DBAdapter db = new DBAdapter(this); // Create new object of DBAdapter
 	private Handler mHandler = new Handler();
 	
 	// UI Objects
 	private ProgressBar progressBar;
 	private ProgressBar spinningProgress;
 	private TextView progressText;
 	
 	// Thread variables
 	private boolean isParsingRunning = false;
 	public static int progressPercent;
 
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
        progressBar = (ProgressBar)findViewById(R.id.progress);
         progressText = (TextView)findViewById(R.id.progressText);
        //spinningProgress = (ProgressBar)findViewById(R.id.progressBarSpinning);
         final Intent startMenu = new Intent(MainActivity.this, MainMenu.class);
       
         // Download and parsing main thread
     	Thread downloadAndParse = new Thread(new Runnable() {
     		public void run() {    
     			try {
     				// Hide progressBar and display downloading text
     				mHandler.post(new Runnable() {
     	                public void run() {
     	                	progressBar.setVisibility(View.INVISIBLE);
     	                	progressText.setText(R.string.download_inprogress);
     	                }
     	            });
 
     				// Download NEO page from the web
     				String downloadStatus = da.downloadNEOPage();
     				
     				// If download was successful
     				if ( downloadStatus.equals("Success") ) {
     					// Update the UI
     					mHandler.post(new Runnable() {
         	                public void run() {
         	                	spinningProgress.setVisibility(View.INVISIBLE);
         	                	progressBar.setVisibility(View.VISIBLE);
         	                	progressBar.setProgress(0);
         	                	progressText.setText(R.string.parsing_inprogress);
         	                }
         	            });
         				
     					// Initialise the parsing in its own thread
         				isParsingRunning = true;
         				new Thread(new Runnable() {
         			        public void run() {
         			            String parsingStatus = da.parseDocument();
         			            // If parsing is successful, signify dependent threads to stop
         			            if ( parsingStatus.equals("Success") ) {
         			            	isParsingRunning = false;
         			            // Else display parsing error and launch main menu
         			            } else {
         			            	db.closeDB();
         			            	Toast.makeText(MainActivity.this, R.string.download_parsing_error_toast, Toast.LENGTH_LONG).show();
         			            	mHandler.postDelayed(mLaunchTask,1000);
         			            	isParsingRunning = false;
         			            }
         			        }
         			    }).start();
         				
         				// When parsing thread is running, update the UI
         			    while ( isParsingRunning == true ) {
         			    	Thread.sleep(150);
         			    	mHandler.post(new Runnable() {
             	                public void run() {
             	                	progressBar.setProgress(progressPercent);
             	                }
             	            });
         			    }
         			    
         			    // Download and parsing successful, launch the main menu
         			    db.closeDB();
         				startActivity(startMenu);
         				
         			// If downloading returns an HTTP Status Exception
     				} else if ( downloadStatus.equals("HttpStatusException") ) {
     					// Update the UI with relevant error and launch the main menu
     					mHandler.post(new Runnable() {
         	                public void run() {
         	                	spinningProgress.setVisibility(View.INVISIBLE);
         	                	progressText.setVisibility(View.INVISIBLE);
         	                	db.closeDB();
         	                	Toast.makeText(MainActivity.this, R.string.download_failed_toast, Toast.LENGTH_LONG).show();
         	                	mHandler.postDelayed(mLaunchTask,3000);
         	                }
         	            });
     				
     				// If downloading returns a Socket Timeout Exception
     				} else if ( downloadStatus.equals("SocketTimeoutException") ) {
     					// Update the UI with relevant error and launch the main menu
     					mHandler.post(new Runnable() {
         	                public void run() {
         	                	spinningProgress.setVisibility(View.INVISIBLE);
         	                	progressText.setVisibility(View.INVISIBLE);
         	                	db.closeDB();
         	                	Toast.makeText(MainActivity.this, R.string.download_timeout_error_toast, Toast.LENGTH_LONG).show();
         	                	mHandler.postDelayed(mLaunchTask,3000);
         	                }
         	            });
     					
     				// If downloading returns a Malformed URL Exception
     				} else if ( downloadStatus.equals("MalformedURLException") ) {
     					// Update the UI with relevant error and launch the main menu
     					mHandler.post(new Runnable() {
         	                public void run() {
         	                	spinningProgress.setVisibility(View.INVISIBLE);
         	                	progressText.setVisibility(View.INVISIBLE);
         	                	db.closeDB();
         	                	Toast.makeText(MainActivity.this, R.string.download_failed_toast, Toast.LENGTH_LONG).show();
         	                	mHandler.postDelayed(mLaunchTask,3000);
         	                }
         	            });
     					
     				// If downloading returns an IO Exception
     				} else if ( downloadStatus.equals("IOException") ) {
     					// Update the UI with relevant error and launch the main menu
     					mHandler.post(new Runnable() {
         	                public void run() {
         	                	spinningProgress.setVisibility(View.INVISIBLE);
         	                	progressText.setVisibility(View.INVISIBLE);
         	                	db.closeDB();
         	                	Toast.makeText(MainActivity.this, R.string.wifi_error_toast, Toast.LENGTH_LONG).show();
         	                	mHandler.postDelayed(mLaunchTask,3000);
         	                }
         	            });
     				}
     			}
     			catch (Throwable t) {
     				Log.w("THREADING", "Exception caught in MainActivity by thread");
     				// just end the background thread
     			}
     		}
     	});
     	
     	
     	
     	/** Actual activity initialisation starts here **/
         db.openDB();
         
         // Check if database update is needed
         if ( db.requireUpdate() == true || db.isDatabaseEmpty() == true ) {
         	// Start the thread
         	downloadAndParse.start();
         } else {
 			// Hide the progress bar
         	progressBar.setVisibility(View.INVISIBLE);
 			db.closeDB();
 			
 			// Delayed launch of main menu
 			mHandler.postDelayed(mLaunchTask,2000);
 		}
     }
 
     
     // Allows use of delayed intents in threads
     private Runnable mLaunchTask = new Runnable() {
         public void run() {
             Intent i = new Intent(getApplicationContext(),MainMenu.class);
             startActivity(i);
         }
      };
 
}
