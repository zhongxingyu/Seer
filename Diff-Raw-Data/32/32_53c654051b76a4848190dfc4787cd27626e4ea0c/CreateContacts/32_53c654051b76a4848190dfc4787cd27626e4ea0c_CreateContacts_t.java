 package com.capgemini.igor;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.provider.ContactsContract.RawContacts;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ProgressBar;
 
 import com.capgemini.igor.pusher.ContactPusher;
 
 public class CreateContacts extends Activity {
 	private static final String logTag = "Igor:CreateContacts";
 
	private ContactPusher contactPusher;
 	private ProgressBar progressBar;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.main);
 
 		setUpButtons();
 
 		setUpOtherWidgets();
 	}
 
 	private void setUpOtherWidgets() {
 		progressBar = (ProgressBar) findViewById(R.id.ProgressBar01);
 	}
 
 	private void setUpButtons() {
 		Button createButton = (Button) findViewById(R.id.CreateButton);
 		createButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				createContacts();
 			}
 		});
 
 		Button deleteButton = (Button) findViewById(R.id.DeleteButton);
 		deleteButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				deleteAllContacts();
 			}
 		});
 
 		Button exitButton = (Button) findViewById(R.id.ExitButton);
 		exitButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				exit();
 			}
 		});
 	}
 
 	private void createContacts() {
		killWorkers();

		contactPusher = new ContactPusher(this);
		contactPusher.execute();
	}

	private void killWorkers() {
		if (contactPusher != null && !contactPusher.isCancelled()) {
			contactPusher.cancel(true);
		}
 	}
 
 	private void deleteAllContacts() {
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				int numberOfContactsMarkedForDeletion = getContentResolver().delete(RawContacts.CONTENT_URI, null, null);
 
 				Log.i(logTag, "Marked " + numberOfContactsMarkedForDeletion + " raw contacts for deletion");
 			}
 		}).start();
 	}
 
 	private void exit() {
		killWorkers();
 
 		this.finish();
 	}
 
 	public void createContactsUpdateCallback(Integer progress) {
 		progressBar.setProgress(progress);
 	}
 
 	public void createContactsStartedCallback() {
 		progressBar.setIndeterminate(false);
 		progressBar.setMax(20);
 		progressBar.setProgress(0);
 		progressBar.setVisibility(ProgressBar.VISIBLE);
 	}
 
 	public void createContactsFinishedCallback() {
 		progressBar.setVisibility(ProgressBar.INVISIBLE);
 	}
 
 }
