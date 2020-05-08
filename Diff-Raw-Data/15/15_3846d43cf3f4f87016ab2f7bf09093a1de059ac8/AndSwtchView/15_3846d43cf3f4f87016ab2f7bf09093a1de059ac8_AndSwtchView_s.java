 package sta.andswtch.gui;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import sta.andswtch.R;
 import sta.andswtch.extensionLead.ExtensionLead;
 import sta.andswtch.extensionLead.ExtensionLeadManager;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.text.format.Time;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ToggleButton;
 
 public class AndSwtchView extends OptionsMenu implements IAndSwtchViews {
 
 	private final int TAG_ALL_ON = 9;
 	private final int TAG_ALL_OFF = 10;
 	
 	private ExtensionLeadManager extLeadManager;
 	private ExtensionLead extLead;
 	private List<LinearLayout> LL;
 	private List<TextView> names;
 	private List<ToggleButton> buttons;
 	private Time time;
 	
 	private Handler handlerEvent = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			switch (msg.what) {
 				case 1: {
 					for (int i = 1; i <= ExtensionLead.POWERPOINTCNT; i++) {
 						checkState(buttons.get(i - 1));
 					}
 					break;
 				}
 				case 2: {
 					Context context = getApplicationContext();
 					CharSequence text = (String) msg.obj;
 					int duration = Toast.LENGTH_SHORT;
 					Toast toast = Toast.makeText(context, text, duration);
 					toast.show();
 				}
 				default: {
 					super.handleMessage(msg);
 					break;
 				}
 			}
 			// has to be done HERE (from outa space) cos of different threads
 			availabilityChecker();
 			// change the refresh time
 			setRefreshTime();
 			setName();
 		}
 	};
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		// create the extension lead
 		this.extLeadManager = ExtensionLeadManager.getInstance(this);
 		this.extLead = this.extLeadManager.getExtLeadFromView(this);
 		
 		super.onCreate(savedInstanceState);
 		this.setContentView(R.layout.andswtch);
 		
 		this.init();
 	}
 
 	private void init() {
 		this.time = new Time();
 		
 		this.names = new ArrayList<TextView>();
 		this.names.add((TextView) findViewById(R.id.TextView01));
 		this.names.add((TextView) findViewById(R.id.TextView02));
 		this.names.add((TextView) findViewById(R.id.TextView03));
 		this.names.add((TextView) findViewById(R.id.TextView04));
 		this.names.add((TextView) findViewById(R.id.TextView05));
 		this.names.add((TextView) findViewById(R.id.TextView06));
 		this.names.add((TextView) findViewById(R.id.TextView07));
 		this.names.add((TextView) findViewById(R.id.TextView08));
 		
 		this.LL = new ArrayList<LinearLayout>();
 		this.LL.add((LinearLayout) findViewById(R.id.LL00));
 		this.LL.add((LinearLayout) findViewById(R.id.LL01));
 		this.LL.add((LinearLayout) findViewById(R.id.LL02));
 		this.LL.add((LinearLayout) findViewById(R.id.LL03));
 		this.LL.add((LinearLayout) findViewById(R.id.LL04));
 		this.LL.add((LinearLayout) findViewById(R.id.LL05));
 		this.LL.add((LinearLayout) findViewById(R.id.LL06));
 		this.LL.add((LinearLayout) findViewById(R.id.LL07));
 		this.LL.add((LinearLayout) findViewById(R.id.LL08));
 		
 		this.buttons = new ArrayList<ToggleButton>();
 		this.buttons.add((ToggleButton) findViewById(R.id.Button01));
 		this.buttons.add((ToggleButton) findViewById(R.id.Button02));
 		this.buttons.add((ToggleButton) findViewById(R.id.Button03));
 		this.buttons.add((ToggleButton) findViewById(R.id.Button04));
 		this.buttons.add((ToggleButton) findViewById(R.id.Button05));
 		this.buttons.add((ToggleButton) findViewById(R.id.Button06));
 		this.buttons.add((ToggleButton) findViewById(R.id.Button07));
 		this.buttons.add((ToggleButton) findViewById(R.id.Button08));
 		
 		this.availabilityChecker();
 	}
 	
 	@Override
 	public void onStart() {
 		super.onStart();
 		this.extLead.sendUpdateMessage();
 	}
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
 		this.extLead = this.extLeadManager.getExtLeadFromView(this);
 		if(this.extLead.isAutoRefreshRunning())
 			this.extLead.sendUpdateMessage();
 		this.extLead.startAutoRefreshRunning();
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		menu.add(0, 1, 0, R.string.om_refresh);
 		return super.onCreateOptionsMenu(menu);
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 			case 1: {
 				this.extLead.sendUpdateMessage();
 				return true;
 			}
 			default: {
 				return super.onOptionsItemSelected(item);
 			}
 		}
 	}
 	
 	@Override
	public void onStop() {
 		this.extLead.stopAutoRefreshRunning();
		super.onStop();
 	}
 	
 	public Context getAppContext() {
 		return this.getApplicationContext();
 	}
 	
 	public void updateActivity() {
 		Message msg = new Message();
 		msg.what = 1;
 		this.handlerEvent.sendMessage(msg);
 	}
 
 	public void showErrorMessage(String message) {
 		Message msg = new Message();
 		msg.what = 2;
 		msg.obj = message;
 		this.handlerEvent.sendMessage(msg);
 	}
 
 	private void availabilityChecker() {
 		int cntUnavailables = 0;
 		// Checks if the powerpoints are enabled
 		// Makes the powerpoints visible if enabled
 		for(int i = 1; i <= ExtensionLead.POWERPOINTCNT; i++) {
 			if(this.extLead.isPowerPointEnable(i)) {
 				this.LL.get(i).setVisibility(View.VISIBLE);
 				this.names.get(i - 1).setText(this.extLead.getPowerPointName(i));
 			}
 			else {
 				this.LL.get(i).setVisibility(View.GONE);
 				cntUnavailables++;
 			}
 		}
 		// Makes the progressbar invisible and switcherbuttons visible
 		if(cntUnavailables == ExtensionLead.POWERPOINTCNT ) {
 			this.LL.get(0).setVisibility(View.VISIBLE);
 			findViewById(R.id.allOn).setVisibility(View.INVISIBLE);
 			findViewById(R.id.allOff).setVisibility(View.INVISIBLE);
 		}
 		else {
 			this.LL.get(0).setVisibility(View.GONE);
 			findViewById(R.id.allOn).setVisibility(View.VISIBLE);
 			findViewById(R.id.allOff).setVisibility(View.VISIBLE);
 		}
 	}
 	
 	private void setRefreshTime() {
 		time.setToNow();
 		TextView refreshtime = (TextView) findViewById(R.id.refreshtime);
 		refreshtime.setText(time.format("%H:%M:%S"));
 	}
 	
 	private void setName() {
 		if(this.extLead.getName() != "") 
 			this.setTitle("AndSwtch - " + this.extLead.getName());
 	}
 	
 	public void onOff(View v) {
 		// tag 1 - 8 for each power point
 		// tag 9 for all on and tag 10 for all off
 		String tagString = (String) v.getTag();
 		if (tagString != null) {
 			int tag = Integer.parseInt(tagString);
 			if (tag == TAG_ALL_ON) {
 				this.extLead.sendStateAll(true);
 			} else if (tag == TAG_ALL_OFF) {
 				this.extLead.sendStateAll(false);
 			} else {
 				this.extLead.switchState(tag);
 			}
 		}
 	}
 
 	public void switchToPowerPoint(View v) {
 		Intent toLaunch = new Intent(v.getContext(), PowerPointView.class);
 		toLaunch.putExtra("powerPoint", (String) v.getTag());
 		if(this.extLead.getName() != "")
 			toLaunch.putExtra("name", this.extLead.getName());
 		startActivity(toLaunch);
 	}
 	
 	private void checkState(ToggleButton btn) {
 		String tagString = (String) btn.getTag();
 		if (tagString != null) {
 			int tag = Integer.parseInt(tagString);
 			if (this.extLead.isPowerPointOn(tag)) {
 				this.setOn(btn);
 			} else {
 				this.setOff(btn);
 			}
 		}
 	}
 
 	private void setOn(ToggleButton btn) {
 		btn.setChecked(true);
 		//btn.setBackgroundColor(Color.GREEN);
 		//btn.setText("ON");
 	}
 
 	private void setOff(ToggleButton btn) {
 		btn.setChecked(false);
 		//btn.setBackgroundColor(Color.RED);
 		//btn.setText("OFF");
 	}
 	
 }
