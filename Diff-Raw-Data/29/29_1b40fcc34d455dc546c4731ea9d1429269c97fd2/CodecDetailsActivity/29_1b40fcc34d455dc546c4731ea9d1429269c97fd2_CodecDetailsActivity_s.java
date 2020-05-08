 package com.crayonio.mediacodecquery;
 
 import java.util.ArrayList;
 
 import android.app.ActivityOptions;
 import android.app.ListActivity;
 import android.content.ClipData;
 import android.content.ClipboardManager;
 import android.content.Intent;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.widget.AdapterView;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class CodecDetailsActivity extends ListActivity implements
 		OnItemClickListener {
 
 	final static String CODEC_INDEX = "codecIndex";
 	final static String SELECTED_TYPE = "selectedType";
 
	private CodecInfo thisCodecInfo;
 	private int codecIndex = -1;
 	private String[] types;
 
 	private Typeface robotoCondensedLight;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_codec_details);
 
 		robotoCondensedLight = Typeface.createFromAsset(getAssets(), "RobotoCondensed-Light.ttf");
 
 		ListView myListView = getListView();
 		myListView.setOnItemClickListener(this);
 		registerForContextMenu(myListView);
 
 		if (savedInstanceState != null) {
 			int newCodecState = savedInstanceState.getInt(CODEC_INDEX, -1);
 			if (newCodecState >= 0)
 				codecIndex = newCodecState;
 		}
 
		if (codecIndex < 0) {
			codecIndex = getIntent().getIntExtra(CodecListActivity.CODEC_INDEX,
					-1);
		}
 
 		ArrayList<CodecInfo> codecInfoList = CodecInfoList.getCodecInfoList();
 
 		if (codecIndex >= 0 && codecIndex < codecInfoList.size()) {
			thisCodecInfo = codecInfoList.get(codecIndex);
 			types = thisCodecInfo.getSupportedTypes();
			setListAdapter(new ArrayAdapter<String>(this,
					R.layout.codec_detail_row, R.id.codecDetails, types){
 
 				@Override
 				public View getView(int position, View convertView, ViewGroup parent) {
 
 					// Must always return just a View.
 					View rowView = super.getView(position, convertView, parent);
 
 					if (rowView != null){
 						String entry = types[position];

						TextView details = (TextView) rowView
								.findViewById(R.id.codecDetails);
 						details.setTypeface(robotoCondensedLight);

 						details.setText(entry);
 					}
 
 					return rowView;
 				}
 
 			});
 		} else
 			Log.w("Codec Details Activity", "No codec Index ");
 
 	}
 
 	@Override
 	public void onBackPressed() {
 		super.onBackPressed();
 		// Log.d("Details Activity","Back pressed.");
 		overridePendingTransition(R.anim.details_activity_slide_enter,
 				R.anim.details_activity_slide_exit);
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		outState.putInt(CODEC_INDEX, codecIndex);
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
 
 		Intent intent = new Intent(this, CodecProfileActivity.class);
 		intent.putExtra(CODEC_INDEX, codecIndex);
 		intent.putExtra(SELECTED_TYPE, types[arg2]);
		Bundle bndlanimation = ActivityOptions.makeCustomAnimation(
				getApplicationContext(), R.anim.list_activity_slide_enter,
				R.anim.list_activity_slide_exit).toBundle();
 		startActivity(intent, bndlanimation);

 	}
 
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.settings_menu, menu);
 		return true;
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item) {
 
 		switch (item.getItemId()) {
 			case android.R.id.home:
 				finish();
 				overridePendingTransition(R.anim.details_activity_slide_enter,
 						R.anim.details_activity_slide_exit);
 				return true;
 
 			case R.id.action_email:
 
 				StringBuilder sb = new StringBuilder();
 
 				String codecName =  CodecInfoList.getCodecInfoList().get(codecIndex).getCodecName();
 				sb.append("Your device supports the following types within ").append(codecName).append(" codec.").append("\n\n");
 
 				for (String thisCodecType : CodecInfoList.getCodecInfoList().get(codecIndex).getSupportedTypes())
 					sb.append("- ").append(thisCodecType).append("\n\n");
 
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
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 		menu.setHeaderTitle("Options..");
 		menu.add(ContextMenu.NONE, ((AdapterContextMenuInfo)menuInfo).position, 0, getString(R.string.copy) + getString(R.string.copy_type));
 	}
 
 	public boolean onContextItemSelected(MenuItem item) {
 		if(item.getOrder() == 0)
 		{
 			ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
 			ArrayList<CodecInfo> codecInfoList = CodecInfoList.getCodecInfoList();
 			ClipData clip = ClipData.newPlainText("label", (codecInfoList.get(codecIndex).getSupportedTypes())[item.getItemId()]);
 			clipboard.setPrimaryClip(clip);
 			Toast.makeText(getApplicationContext(), getString(R.string.copy_type) + getString(R.string.copied), Toast.LENGTH_SHORT).show();
 			return true;
 		}
 		else
 			return super.onContextItemSelected(item);
 	}
 }
