 package com.oakonell.chaotictactoe.ui.game;
 
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.CheckBox;
 
 import com.actionbarsherlock.app.SherlockDialogFragment;
 import com.oakonell.chaotictactoe.R;
 
 public class OnlineSettingsDialogFragment extends SherlockDialogFragment {
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View view = inflater.inflate(R.layout.online_settings_dialog,
 				container, false);
 		getDialog().setTitle(R.string.online_setting_title);
 		// TODO move the pref get/set off main thread...
 		final CheckBox sounds = (CheckBox) view
 				.findViewById(R.id.pref_sound_fx_key);
 		sounds.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				boolean useSounds = sounds.isChecked();
 
 				SharedPreferences preferences = PreferenceManager
 						.getDefaultSharedPreferences(getActivity());
 				final String soundFxPrefKey = getActivity().getString(
 						R.string.pref_sound_fx_key);
 				Editor edit = preferences.edit();
 				edit.putBoolean(soundFxPrefKey, useSounds);
 				edit.commit();
 			}
 		});
 
 		SharedPreferences preferences = PreferenceManager
 				.getDefaultSharedPreferences(getActivity());
 		final String soundFxPrefKey = getActivity().getString(
 				R.string.pref_sound_fx_key);
		sounds.setChecked(preferences.getBoolean(soundFxPrefKey, false));
 
 		Button okButton = (Button) view.findViewById(R.id.ok);
 		okButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				getDialog().dismiss();
 			}
 		});
 		return view;
 	}
 }
