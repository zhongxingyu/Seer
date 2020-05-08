 package com.crayonio.mediacodecquery;
 
import java.util.ArrayList;
 import android.app.ActivityOptions;
 import android.app.ListActivity;
 import android.content.ClipData;
 import android.content.ClipboardManager;
 import android.content.Intent;
import android.content.pm.ActivityInfo;
 import android.content.pm.PackageManager;
 import android.os.Bundle;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import com.bugsense.trace.BugSenseHandler;
 
 public class CodecListActivity extends ListActivity implements
 		OnItemClickListener {
 
 	final static String CODEC_INDEX = "codecIndex";
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		/*Setup Bugsense*/
 		try {
			ActivityInfo app = getPackageManager().getActivityInfo(this.getComponentName(),PackageManager.GET_ACTIVITIES|PackageManager.GET_META_DATA);
			BugSenseHandler.initAndStartSession(CodecListActivity.this,(String)app.metaData.get("com.crayonio.mediacodecquery.BUGSENSE_API_KEY"));
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		setContentView(R.layout.activity_codec_list);
 		this.setTitle(getString(R.string.title_activity_codec_list));
 
 		ListView myListView = getListView();
 		myListView.setOnItemClickListener(this);
 		registerForContextMenu(myListView);
 
 		final ArrayList<CodecInfo> codecInfoList = CodecInfoList.getCodecInfoList();
 
 		setListAdapter(new ArrayAdapter<CodecInfo>(this,
 				R.layout.codec_list_row, R.id.codecName, codecInfoList) {
 
 			@Override
 			public View getView(int position, View convertView, ViewGroup parent) {
 
 				// Must always return just a View.
 				View rowView = super.getView(position, convertView, parent);
 
 				if (rowView != null) {
 					CodecInfo entry = codecInfoList.get(position);
 
 					TextView name = (TextView) rowView.findViewById(R.id.codecName);
 					TextView fullName = (TextView) rowView.findViewById(R.id.codecFullName);
 
 					name.setText(entry.getCodecName());
 					fullName.setText(entry.getFullName());
 
 					// Log.d("GetView", "Getting rowView " + position + " : " +
 					// rowView.getId());
 
 					ImageView dirImg = (ImageView) rowView.findViewById(R.id.directionImage);
 
 					if (entry.isDecoder() && entry.isEncoder()) {
 						dirImg.setImageDrawable(getResources().getDrawable(R.drawable.codec_both));
 						// Log.d("GetView", "Setting Image in " + position);
 					} else if (entry.isDecoder()) {
 						dirImg.setImageDrawable(getResources().getDrawable(R.drawable.codec_decoder));
 					} else if (entry.isEncoder()) {
 						dirImg.setImageDrawable(getResources().getDrawable(R.drawable.codec_encoder));
 					} else {
 						dirImg.setImageDrawable(null);
 					}
 				}
 
 				return rowView;
 			}
 		});
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
 		// Log.d("OnClick", "Clicked " + arg2 + " " + codecInfoList.get(arg2));
 		Intent intent = new Intent(this, CodecDetailsActivity.class);
 		intent.putExtra(CODEC_INDEX, arg2);
 		Bundle bundleAnimation = ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.list_activity_slide_enter,R.anim.list_activity_slide_exit).toBundle();
 		startActivity(intent, bundleAnimation);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.settings_menu, menu);
 		return true;
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if (item.getItemId() == R.id.action_email) {
 
 			StringBuilder sb = new StringBuilder();
 
 			sb.append("Your device supports the following Media Codecs.");
 			sb.append("\n\n");
 
 			for (CodecInfo thisCodec : CodecInfoList.getCodecInfoList()) {
 				if (thisCodec.isDecoder() && thisCodec.isEncoder()) sb.append("- Encoder+Decoder : ");
 				else if (thisCodec.isDecoder()) sb.append("- Decoder : ");
 				else if (thisCodec.isEncoder()) sb.append("- Encoder : ");
 				sb.append(thisCodec.getCodecName()).append("  : ");
 				sb.append(thisCodec.getFullName()).append("\n\n");
 			}
 
 			sb.append("\n\nThanks for using Media Codec Query.");
 
 			final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
 
 			emailIntent.setType("plain/text");
 			emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Your Device's Media Codec Support");
 			emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, sb.toString());
 			emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 			Intent i = Intent.createChooser(emailIntent, "Send mail...");
 			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 			getApplicationContext().startActivity(i);
 			return true;
 		} else
 			return super.onOptionsItemSelected(item);
 	}
 
 	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 		menu.setHeaderTitle("Options..");
 		menu.add(ContextMenu.NONE, ((AdapterContextMenuInfo) menuInfo).position, 0, getString(R.string.copy) + getString(R.string.copy_short_name));
 		menu.add(ContextMenu.NONE, ((AdapterContextMenuInfo) menuInfo).position, 1, getString(R.string.copy) + getString(R.string.copy_full_name));
 	}
 
 	public boolean onContextItemSelected(MenuItem item) {
 		if (item.getOrder() == 0) {
 			ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
 			ArrayList<CodecInfo> codecInfoList = CodecInfoList.getCodecInfoList();
 			ClipData clip = ClipData.newPlainText("label", codecInfoList.get(item.getItemId()).getCodecName());
 			clipboard.setPrimaryClip(clip);
 			Toast.makeText(getApplicationContext(), getString(R.string.copy_short_name) + getString(R.string.copied), Toast.LENGTH_SHORT).show();
 			return true;
 		} else if (item.getOrder() == 1) {
 			ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
 			ArrayList<CodecInfo> codecInfoList = CodecInfoList.getCodecInfoList();
 			ClipData clip = ClipData.newPlainText("label", codecInfoList.get(item.getItemId()).getFullName());
 			clipboard.setPrimaryClip(clip);
 			Toast.makeText(getApplicationContext(), getString(R.string.copy_full_name) + getString(R.string.copied), Toast.LENGTH_SHORT).show();
 			return true;
 		} else
 			return super.onContextItemSelected(item);
 
 	}
 
 	@Override
 	protected void onDestroy() {
 		BugSenseHandler.closeSession(CodecListActivity.this);
 		super.onDestroy();
 	}
 }
