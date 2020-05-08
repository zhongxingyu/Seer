 package com.example.auditpermissionexample;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.Queue;
 
 import android.net.LocalSocket;
 import android.net.LocalSocketAddress;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.View;
 import android.widget.TextView;
 import android.widget.ToggleButton;
 
 public class MainActivity extends Activity {
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
     
     public void btnGetID_onClick(View view) {
     	Process getIds = null;
     	
     	// Create our ID process, run it, and print the output.
     	try {
     		getIds = new ProcessBuilder()
     					.command("id")
     					.redirectErrorStream(true)
     					.start();
     		
     		BufferedReader in = new BufferedReader(new InputStreamReader(getIds.getInputStream()));
    		 
     		String line = "";
     		
     		while((line = in.readLine()) != null) {
     			TextView txtOutput = (TextView) findViewById(R.id.txtViewOutput);
     			txtOutput.setText(line + "\n");
     		}
     		
     	}
     	catch (IOException e) {
     		e.printStackTrace();
     		getIds.destroy();
     	}    
     }
     
     public void toggleBtnAuditStream_onClick(View view) {
     	// Check our button state.
     	ToggleButton toggleBtnAudit = ((ToggleButton) view);
    	Thread auditstream;
    	auditstream = new Thread(new AuditStreamReader(view));
     	
     	//The click turned the button on
     	if(toggleBtnAudit.isChecked()) {
     		auditstream.start();
     	}
     	else { // The click turned the button off
     		auditstream.interrupt();
    		System.out.println("Thread Interrupted: " + auditstream.isInterrupted() + "\n");
     	}
     	
     	
     }
     
     /** An extra thread that deals with connecting to and reading the audit stream without
      * interrupting the original UI thread.
      */
     private class AuditStreamReader implements Runnable {
     	
     	private Queue<String> audit_record;
     	private static final String AUDIT_DEVICE = "/dev/audit";
     	private LocalSocket auditStream;
     	private View parent_view; // The view of this thread's parent UI
     	
     	public AuditStreamReader(View view) {
     		this.parent_view = view;
     	}
     	
 				@Override
 		public void run() {
     		// Set up the local socket address to our audit stream
 	    	LocalSocketAddress auditAddress = new LocalSocketAddress(AUDIT_DEVICE, LocalSocketAddress.Namespace.FILESYSTEM);
 	    	
 	    	// Open audit stream device with Local Socket
 	    	auditStream = new LocalSocket();
 
 			BufferedReader in;
 	    	
 	    	try {
 				auditStream.connect(auditAddress);
 				
 				in = new BufferedReader(new InputStreamReader(auditStream.getInputStream()));
 
 				try {
					// Read records until we hit a null
 					while(!Thread.currentThread().isInterrupted()) {
 						// Read line
 						final String line = in.readLine();
 
 						if(line != null) {
 							parent_view.post(new Runnable() {
 								public void run() {
 									TextView txtOutput = (TextView) findViewById(R.id.txtViewOutput);		
 									txtOutput.append(line + "\n");
 								}
 							});
 						}
 
 						Thread.sleep(20);
 					}
 
 				}catch (InterruptedException e) {
 					android.util.Log.i("threads", "Audit Stream Thread Interrupted, closing stream.");
 					auditStream.close();
 					return;
 				}
 				
 				// Loop is done, we were interrupted, close stream
 				auditStream.close();
 				return;
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} 
 		}
     	
     }
 }
