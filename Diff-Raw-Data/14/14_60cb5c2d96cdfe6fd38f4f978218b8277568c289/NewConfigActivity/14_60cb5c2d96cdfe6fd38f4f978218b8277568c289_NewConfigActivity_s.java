 package ceu.marten.activities;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.View;
 import android.view.Window;
 import android.view.inputmethod.EditorInfo;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.RadioButton;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 import android.widget.Toast;
 import ceu.marten.adapters.ActiveChannelsListAdapter;
 import ceu.marten.bplux.R;
 import ceu.marten.data.Configuration;
 
 public class NewConfigActivity extends Activity {
 
 	int freq;
 	int nbits;
 	Configuration config;
 	ListView lv;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.ly_new_config);
 
 		/* initialize view components */
 		SeekBar frequency = (SeekBar) findViewById(R.id.freq_seekbar);
 		frequency.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
 
 			public void onProgressChanged(SeekBar seekBar, int progress,
 					boolean fromUser) {
 				if (fromUser) {
 					((TextView) findViewById(R.id.freq_view)).setText(String
 							.valueOf(progress + 36));
 					freq = progress;
 				}
 			}
 
 			public void onStartTrackingTouch(SeekBar seekBar) {
 			}
 
 			public void onStopTrackingTouch(SeekBar seekBar) {
 			}
 		});
 
 		final EditText freqView = (EditText) findViewById(R.id.freq_view);
 		freqView.setOnEditorActionListener(new OnEditorActionListener() {
 			@Override
 			public boolean onEditorAction(TextView v, int actionId,
 					KeyEvent event) {
 				boolean handled = false;
 				if (actionId == EditorInfo.IME_ACTION_DONE) {
 					SeekBar frequency = (SeekBar) findViewById(R.id.freq_seekbar);
 					int new_frec = Integer.parseInt(v.getText().toString());
 					if(new_frec > 36 ){
						frequency.setProgress((new_frec+36));
 						freq = new_frec;
 					}
 					else{
 						frequency.setProgress(0);
 						freqView.setText("36");
 						freq = 36;
 					}
						
 					handled = true;
 				}
 				return handled;
 
 			}
 		});
 
 		/* initialize variables */
 		config = new Configuration();
 
 	}
 
 	private void setupActiveChannelsDialog() {
 
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setIcon(R.drawable.ic_launcher);
 		builder.setTitle("Select active channels");
 		final ArrayList<String> channels = new ArrayList<String>();
 		channels.add("channel 1");
 		channels.add("channel 2");
 		channels.add("channel 3");
 		channels.add("channel 4");
 		channels.add("channel 5");
 		channels.add("channel 6");
 		channels.add("channel 7");
 		channels.add("channel 8");
 		final ActiveChannelsListAdapter acla = new ActiveChannelsListAdapter(
 				this, channels);
 		builder.setView(getLayoutInflater().inflate(
 				R.layout.dialog_channels_listview, null));
 		builder.setPositiveButton("accept",
 				new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 
 						TextView ca = (TextView) findViewById(R.id.txt_canales_activos);
 						ca.setText("selected channels: \n\n");
 						String[] checked = acla.getChecked();
 						String si = "";
 						for (int i = 0; i < checked.length; i++) {
 							if (checked[i] != null)
 								si = si+"\t channel " + (i + 1) + " with sensor " + checked[i] + "\n";
 						}
 						ca.append(si);
 					}
 				});
 		builder.setNegativeButton("cancel",
 				new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						dialog.dismiss();
 					}
 				});
 
 		AlertDialog ad = builder.create();
 		ad.show();
 
 		lv = (ListView) ad.findViewById(R.id.lv_channelsSelection);
 		lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
 		lv.setItemsCanFocus(false);
 
 		lv.setAdapter(acla);
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.settings_menu, menu);
 		return true;
 	}
 
 	public void onClickedSubmit(View view) {
 
 		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
 		Date date = new Date();
 		config.setName(((EditText) findViewById(R.id.dev_name)).getText()
 				.toString());
 		config.setFreq(freq);
 		config.setnBits(nbits);
 		config.setCreateDate(dateFormat.format(date));
 		Intent returnIntent = new Intent();
 		returnIntent.putExtra("config", config);
 		setResult(RESULT_OK, returnIntent);
 		finish();
 
 		Toast t;
 		t = Toast.makeText(getApplicationContext(),
 				"config successfully created", Toast.LENGTH_SHORT);
 		t.setGravity(Gravity.BOTTOM | Gravity.CENTER, 0, 0);
 		t.show();
 	}
 
 	public void onClickedCancel(View view) {
 		finish();
 	}
 
 	public void onRadioButtonClicked(View view) {
 		boolean checked = ((RadioButton) view).isChecked();
 
 		switch (view.getId()) {
 		case R.id.radioBttn8:
 			if (checked)
 				nbits = 8;
 			break;
 		case R.id.radioBttn12:
 			if (checked)
 				nbits = 12;
 			break;
 		}
 	}
 
 	public void onClickedChannelPickDialog(View view) {
 		setupActiveChannelsDialog();
 	}
 
 }
