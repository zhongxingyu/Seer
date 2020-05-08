 package com.ese2013.mensaunibe.settings;
 
 
 
 import java.util.ArrayList;
 
 import com.ese2013.mensaunibe.R;
import com.ese2013.mensaunibe.mensa.MensaActivity;
 import com.ese2013.mensaunibe.model.utils.SystemLanguage;
 
 import android.support.v4.app.Fragment;
import android.app.ProgressDialog;
 import android.content.Intent;
 import android.os.Bundle;
import android.os.Handler;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.Spinner;
 import android.widget.Toast;
 
 /**
  * @author group7
  * @author Andreas Hohler
  */
 
 public class SettingsFragment extends Fragment implements OnItemSelectedListener {
 	private static final String TAG = SettingsFragment.class.getName();
 	
 	private View view;
 	private final ArrayList<LanguageItem> data = new ArrayList<LanguageItem>();
 	private boolean spinnerAdapterInit = false;
 	
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		this.view = inflater.inflate(R.layout.activity_settings, container, false);	
 		return this.view;
 	}
 	
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		updateAdapter();
 	}
 	
 	private void updateAdapter() {
 		data.add( new LanguageItem("de", getString(R.string.language_german)) );
 		data.add( new LanguageItem("en", getString(R.string.language_english)) );
 		
 		Spinner sp = (Spinner) view.findViewById(R.id.language_spinner);
 		sp.setAdapter(new LanguageSpinnerAdapter(getActivity(), data));
 		String saved = SystemLanguage.getLanguage();
 		for(LanguageItem l : data) {
 			if(l.getLangCode().equals(saved)) {
 				sp.setSelection( data.indexOf(l) );
 				break;
 			}
 		}
 		
 		sp.setOnItemSelectedListener(this);
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState){
         super.onCreate(savedInstanceState);
         Log.d(TAG,"SettingsFragment created");
     }
 	
 	@Override
 	public void onPause() {
 		super.onPause();
 	}
 	
 	@Override
 	public void onStop() {
 		super.onStop();
 	}
 	
 	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
 		if(!spinnerAdapterInit)	{
 			spinnerAdapterInit = true;
 		} else {
 			LanguageItem item = data.get(position);
 			SystemLanguage.context = getActivity();
 			SystemLanguage.changeLanguage(item.getLangCode());
 			Toast.makeText(getActivity(), getString(R.string.language_changed_to)+" "+item.toString(), Toast.LENGTH_LONG).show();
			/*Intent intent = getActivity().getIntent();
 			getActivity().finish();
			startActivity(intent);*/
			// TODO delete or change it
			ProgressDialog.show(getActivity(), getString(R.string.language), getString(R.string.changing_language), true);
			Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				public void run() {
					restartApp();
				}
			}, 2000);
 		}
 	}
	
	private void restartApp() {
		System.exit(0);
		getActivity().startActivity(new Intent(getActivity().getApplicationContext(), MensaActivity.class));
	}
 
 	@Override
 	public void onNothingSelected(AdapterView<?> arg0) {
 	}
 }
