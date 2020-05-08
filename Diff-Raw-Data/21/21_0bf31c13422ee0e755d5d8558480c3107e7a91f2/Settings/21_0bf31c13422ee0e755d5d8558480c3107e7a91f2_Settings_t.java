 package com.chess.genesis;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnLongClickListener;
 import android.view.View.OnTouchListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.ImageView;
 import android.widget.Spinner;
 import java.util.HashMap;
 import java.util.Map;
 
 public class Settings extends Activity implements OnCheckedChangeListener, OnItemSelectedListener, OnLongClickListener, OnTouchListener
 {
 	private static final Map<Integer,Integer> FreqMap = createMap();
 
 	private static Map<Integer, Integer> createMap()
 	{
 		final Map<Integer, Integer> map = new HashMap<Integer, Integer>();
 		map.put(5, 0);		map.put(6, 1);
 		map.put(10, 2);		map.put(12, 3);
 		map.put(15, 4);		map.put(20, 5);
		map.put(30, 6);		map.put(60, 7);
 		map.put(120, 8);	map.put(180, 9);
 		map.put(240, 10);	map.put(360, 11);
 		map.put(480, 12);	map.put(720, 13);
 		return map;
 	}
 
 	@Override
 	public void onCreate(final Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 
 		// Set only portrait
 		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 		setContentView(R.layout.settings);
 
 		final ImageView button = (ImageView) findViewById(R.id.topbar);
 		button.setOnTouchListener(this);
 		button.setOnLongClickListener(this);
 
 		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
 
 		final CheckBox check = (CheckBox) findViewById(R.id.note_enable);
 		check.setOnCheckedChangeListener(this);
 		check.setChecked(pref.getBoolean("noteEnabled", true));
 
 		final AdapterItem[] list = new AdapterItem[]
 			{new AdapterItem("5 Minutes", 5), new AdapterItem("6 Minutes", 6),
 			new AdapterItem("10 Minutes", 10), new AdapterItem("12 Minutes", 12),
 			new AdapterItem("15 Minutes", 15), new AdapterItem("20 Minutes", 20),
 			new AdapterItem("30 Minutes", 30), new AdapterItem("1 Hours", 60),
 			new AdapterItem("2 Hours", 120), new AdapterItem("3 Hours", 180),
 			new AdapterItem("4 Hours", 240), new AdapterItem("6 Hours", 360),
 			new AdapterItem("8 Hours", 480), new AdapterItem("12 Hours", 720) };
 
 		final ArrayAdapter<AdapterItem> adapter = new ArrayAdapter<AdapterItem>(this, android.R.layout.simple_spinner_item, list);
 		adapter.setDropDownViewResource(R.layout.spinner_dropdown);
 
 		final Spinner spinner = (Spinner) findViewById(R.id.poll_freq);
 		spinner.setOnItemSelectedListener(this);
 		spinner.setAdapter(adapter);
 		spinner.setSelection(FreqMap.get(pref.getInt("notifierPolling", 60)));
 	}
 
 	@Override
 	public void onNothingSelected(AdapterView<?> parent)
 	{
 	}
 
 	@Override
 	public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id)
 	{
 		final AdapterItem item = (AdapterItem) parent.getSelectedItem();
 		final Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
 		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
 
 		if (pref.getInt("notifierPolling", 60) == item.id)
 			return;
 		editor.putInt("notifierPolling", item.id);
 		editor.commit();
 
 		startService(new Intent(this, GenesisNotifier.class));
 	}
 
 	@Override
 	public void onCheckedChanged(final CompoundButton v, final boolean isChecked)
 	{
 		switch (v.getId()) {
 		case R.id.note_enable:
 			final Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
 			final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
 
 			if (pref.getBoolean("noteEnabled", true) == isChecked)
 				return;
 			editor.putBoolean("noteEnabled", v.isChecked());
 			editor.commit();
 
 			if (isChecked)
 				startService(new Intent(this, GenesisNotifier.class));
 			break;
 		}
 	}
 
 	public boolean onTouch(final View v, final MotionEvent event)
 	{
 		switch (v.getId()) {
 		case R.id.topbar:
 			if (event.getAction() == MotionEvent.ACTION_DOWN)
 				((ImageView) v).setImageResource(R.drawable.topbar_pressed);
 			else if (event.getAction() == MotionEvent.ACTION_UP)
 				((ImageView) v).setImageResource(R.drawable.topbar);
 			break;
 		}
 		return false;
 	}
 
 	public boolean onLongClick(final View v)
 	{
 		switch (v.getId()) {
 		case R.id.topbar:
 			finish();
 			return true;
 		default:
 			return false;
 		}
 	}
 }
