 package ca.idrc.tagin.cloud;
 
 /**
  * Komodo Lab: Tagin! Project: 3D Tag Cloud
  * Google Summer of Code 2011
  * @authors Reza Shiftehfar, Sara Khosravinasr and Jorge Silva
  */
 
 import java.io.IOException;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Display;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import ca.idrc.tagin.cloud.tag.Tag;
 import ca.idrc.tagin.cloud.tag.TagCloudView;
 import ca.idrc.tagin.cloud.util.TagAdderDialog;
 import ca.idrc.tagin.lib.TaginManager;
 import ca.idrc.tagin.lib.TaginService;
 import ca.idrc.tagin.lib.tags.GetLabelTask;
 import ca.idrc.tagin.lib.tags.GetLabelTaskListener;
 import ca.idrc.tagin.lib.tags.SetLabelTask;
 import ca.idrc.tagin.lib.tags.SetLabelTaskListener;
 
 import com.google.api.client.json.gson.GsonFactory;
 import com.google.api.services.tagin.model.URN;
 import com.google.api.services.tagin.model.URNCollection;
 
 public class CloudActivity extends Activity implements GetLabelTaskListener, SetLabelTaskListener {
 	
 	private final String MAX_NEIGHBOURS = "10";
 	private Integer mNeighboursCounter;
 	private String mInitialURN;
 	
 	private boolean isInitializing;
 	
 	private CloudActivity mInstance;
 	private Map<String,Tag> mTags;
 	private TaginManager mTaginManager;
 	private TagCloudView mTagCloudView;
 	private TagAdderDialog mTagAdderDialog;
 
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.splash);
 		isInitializing = true;
 		mInstance = this;
 		mTaginManager = new TaginManager(this);
 		mTagAdderDialog = new TagAdderDialog(this);
 		mTags = new LinkedHashMap<String,Tag>();
 		mTaginManager.apiRequest(TaginService.REQUEST_URN);
 	}
 
 	private void createTagCloud() {
 		isInitializing = false;
 		Display display = getWindowManager().getDefaultDisplay();
 		mTagCloudView = new TagCloudView(this, display.getWidth(), display.getHeight(), mTags);
 		setContentView(mTagCloudView);
 		mTagCloudView.requestFocus();
 		updateTagCloud();
 	}
 	
 	public void submitTag(Tag tag) {
 		SetLabelTask<CloudActivity> task = new SetLabelTask<CloudActivity>(mInstance, tag.getID(), tag.getText());
 		task.execute();
 		addTagToCloud(tag);
 	}
 	
 	public void addTagToCloud(Tag tag) {
 		if (tag != null) {
 			mTagCloudView.addTag(tag);
 			mTags.put(tag.getID(), tag);
 			updateTagCloud();
 		}
 	}
 	
 	private void updateTagCloud() {
 		for (Tag tag : mTags.values()) {
 			mTagCloudView.setTagRGBT(tag);
 		}
 	}
 	
 	public void onGetURNClick(View view) {
 		mTaginManager.apiRequest(TaginService.REQUEST_URN);
 		mTagAdderDialog.getURNTextView().setText("Fetching URN...");
 	}
 	
 	@Override
 	protected void onPause() {
 		super.onPause();
 		unregisterReceiver(mReceiver);
 	}
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
         registerReceiver(mReceiver, new IntentFilter(TaginService.ACTION_URN_READY));
         registerReceiver(mReceiver, new IntentFilter(TaginService.ACTION_NEIGHBOURS_READY));
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.options_menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		super.onOptionsItemSelected(item);
 		switch (item.getItemId()) {
 		case R.id.add_tag:
 			mTagAdderDialog.showDialog();
 			break;
 		case R.id.exit_app:
 			finish();
 			break;
 		}
 		return true;
 	}
 	
 	public void handleNeighboursReady(String result) {
 		URNCollection urns = null;
 		
 		if (result != null) {
 			try {
 				urns = new GsonFactory().fromString(result, URNCollection.class);
 			} catch (IOException e) {
 				Log.e("tagin", "Deserialization error: " + e.getMessage());
 			}
 		}
 
 		if (urns != null && urns.getItems() != null && urns.getItems().size() > 0) {
 			mNeighboursCounter = urns.getItems().size();
 			for (URN urn : urns.getItems()) {
 				GetLabelTask<CloudActivity> task = new GetLabelTask<CloudActivity>(this, urn.getValue());
 				task.execute();
 			}
 		} else {
 			// No neighbours found, start cloud
 			createTagCloud();
 		}
 	}
 	
 	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			if (intent.getAction().equals(TaginService.ACTION_URN_READY)) {
 				String urn = intent.getStringExtra(TaginService.EXTRA_QUERY_RESULT);
 				if (isInitializing) {
 					if (urn != null) {
 						mTaginManager.apiRequest(TaginService.REQUEST_NEIGHBOURS, urn, MAX_NEIGHBOURS);
 						mInitialURN = urn;
 						GetLabelTask<CloudActivity> task = new GetLabelTask<CloudActivity>(mInstance, urn);
 						task.execute();
 					} else {
 						Log.d("tagin", "Could not submit fingerprint");
 						// TODO show error dialog
 						createTagCloud();
 					}
 				} else {
 					mTagAdderDialog.getURNTextView().setText(urn);
 					GetLabelTask<CloudActivity> task = new GetLabelTask<CloudActivity>(mInstance, urn);
 					task.execute();
 				}
 			} else if (intent.getAction().equals(TaginService.ACTION_NEIGHBOURS_READY)) {
 				String result = intent.getStringExtra(TaginService.EXTRA_QUERY_RESULT);
 				handleNeighboursReady(result);
 			}
 		}
 	};
 
 	@Override
 	public void onGetLabelTaskComplete(String urn, String label) {
 		if (isInitializing) {
 			if (urn.equals(mInitialURN)) {
 				Tag tag = new Tag(urn, label, 20);
 				mTags.put(urn, tag);
 			} else {
 				synchronized(mNeighboursCounter) {
 					mNeighboursCounter--;
 					if (label != null) {
						Tag tag = new Tag(urn, label, 18);
 						mTags.put(urn, tag);
 					}
 					if (mNeighboursCounter == 0) {
 						createTagCloud();
 					}
 				}
 			}
 		} else {
 			if (label != null) {
 				mTagAdderDialog.getLabelTextView().setText(label);
 			}
 		}
 	}
 
 	@Override
 	public void onSetLabelTaskComplete(Boolean isSuccessful) {
 		
 	}
 
 }
