 package com.ezhang.pop.ui;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 
 import android.widget.*;
 import com.ezhang.pop.R;
 import com.ezhang.pop.core.LocationSpliter;
 import com.ezhang.pop.core.NotEmptyValidator;
 import com.ezhang.pop.settings.AppSettings;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.KeyEvent;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnKeyListener;
 import android.view.View.OnTouchListener;
 
 public class CustomLocationActivity extends Activity {
 	private static final int MAX_HISTORY_LOCATION = 4;
 
 	RadioButton m_btnGPS = null;
 
 	int m_selectedHistoryLocation = 0;
 
 	List<String> m_locations = null;
 
 	AppSettings m_settings = null;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_custom_location);
 		
 		m_settings = new AppSettings(this);
 		
 		final RadioGroup group = (RadioGroup) this
 				.findViewById(R.id.locationRadioGroup);
 
         Spinner suburbSelector = (Spinner) findViewById(R.id.suburbAddress);
         ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
             this, R.array.suburbs, android.R.layout.simple_spinner_item);
         adapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
         suburbSelector.setAdapter(adapter);
 
 		suburbSelector.setOnTouchListener(new OnTouchListener() {
             @Override
             public boolean onTouch(View v, MotionEvent event) {
                 AutoSelectCustomRadioBtn(group);
                 return false;
             }
         });
 
 		suburbSelector.setOnKeyListener(new OnKeyListener() {
             public boolean onKey(View v, int keyCode, KeyEvent event) {
                 AutoSelectCustomRadioBtn(group);
                 return false;
             }
         });
 
 		EditText streetAddr = (EditText) this.findViewById(R.id.streetAddress);
 		streetAddr.setOnTouchListener(new OnTouchListener() {
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				AutoSelectCustomRadioBtn(group);
 				return false;
 			}
 		});
 
 		streetAddr.addTextChangedListener(new TextWatcher() {
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before,
 					int count) {
 				AutoSelectCustomRadioBtn(group);
 			}
 
 			@Override
 			public void afterTextChanged(Editable arg0) {
 			}
 
 			@Override
 			public void beforeTextChanged(CharSequence s, int start, int count,
 					int after) {
 			}
 		});
 
 		m_btnGPS = (RadioButton) this.findViewById(R.id.radioBtnGPSLocation);
 
 		boolean gpsAsLocation = m_settings.UseGPSAsLocation();
 		if (gpsAsLocation) {
 			m_btnGPS.setChecked(true);
 		}
 
 		m_locations = m_settings.GetHistoryLocations();
 
 		RadioButton firstLocationRadioBtn = null;
 		for (int i = 0; i < m_locations.size(); ++i) {
 			RadioButton locationRadioBtn = new RadioButton(this);
 			final int index = i;
 			locationRadioBtn.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					m_selectedHistoryLocation = index;
 				}
 			});
 			locationRadioBtn.setText(m_locations.get(i));
 			group.addView(locationRadioBtn, i + 1);// +1 for GPS radio button
 			if (i == 0) {
 				firstLocationRadioBtn = locationRadioBtn;
 			}
 		}
 		if (!gpsAsLocation && firstLocationRadioBtn != null) {
 			firstLocationRadioBtn.setChecked(true);
 		}
 
         String lastSuburb = m_settings.GetLastSuburb();
        if(!lastSuburb.equals("")){
             int pos = adapter.getPosition(lastSuburb);
             suburbSelector.setSelection(pos);
         }
 	}
 
 	public void OnOKClicked(View v) {
 
 		RadioGroup group = (RadioGroup) this
 				.findViewById(R.id.locationRadioGroup);
 		int selectedID = group.getCheckedRadioButtonId();
 
 		boolean addNewLocation = selectedID == R.id.radioBtnCustomLocation;
 		if (addNewLocation) {
             String suburb = ((Spinner) findViewById(R.id.suburbAddress)).getSelectedItem().toString();
             EditText streetAddrEditText = (EditText) this.findViewById(R.id.streetAddress);
             String streetAddr = streetAddrEditText.getText().toString();
             boolean notEmpty = NotEmptyValidator.NotEmpty(this, streetAddr, "Please input your address.");
             if(!notEmpty){
                 streetAddrEditText.requestFocus();
                 return;
             }
             String newLocation = GenerateNewLocationString(streetAddr, suburb);
 			AddNewLocation(newLocation);
             m_settings.SetLastSuburb(suburb);
 		}
 
         boolean useGPS = selectedID == R.id.radioBtnGPSLocation;
         this.m_settings.UseGPSAsLocation(useGPS);
 		if (!addNewLocation && !useGPS) {
 			ReorderHistoryLocations();
 		}
 
 		SaveLocations();
 		this.finish();
 	}
 
 	private void SaveLocations() {
 		this.m_settings.SaveLocations(m_locations);
 	}
 
 	private void AddNewLocation(String newLocation) {
 		boolean alreadyExisting = false;
 		if (m_locations == null) {
 			m_locations = new ArrayList<String>();
 		}
 		for (String l : this.m_locations) {
 			if (l.toLowerCase(Locale.ENGLISH).equals(
 					newLocation.toLowerCase(Locale.ENGLISH))) {
 				alreadyExisting = true;
 				break;
 			}
 		}
 		if (!alreadyExisting) {
 			this.m_locations.add(0, newLocation);
 		}
 		if (m_locations.size() > MAX_HISTORY_LOCATION) {
             m_locations = m_locations.subList(0, MAX_HISTORY_LOCATION);
 		}
 	}
 
 	private String GenerateNewLocationString(String streetAddr, String suburb) {
 		String newLocation = LocationSpliter.Combine(streetAddr, suburb);
 		return newLocation;
 	}
 
 	private void ReorderHistoryLocations() {
 		if (this.m_selectedHistoryLocation != 0) {
 			String selected = m_locations.get(this.m_selectedHistoryLocation);
 			m_locations.remove(m_selectedHistoryLocation);
 			m_locations.add(0, selected);
 		}
 	}
 
 	public void OnCancelClicked(View v) {
 		this.finish();
 	}
 
 	private void AutoSelectCustomRadioBtn(final RadioGroup group) {
 		group.check(R.id.radioBtnCustomLocation);
 	}
 }
