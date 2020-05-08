 /*
 Copyright (C) 2009 Gopalkrishna Sharma.
 Email: gopalkri@umich.edu / gopalkrishnaps@gmail.com
 
 This file is part of WakeUp!.
 
 Wake Up! is free software: you can redistribute it and/or modify
 it under the terms of the Lesser GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 Wake Up! is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 Lesser GNU General Public License for more details.
 
 You should have received a copy of the Lesser GNU General Public License
 along with Wake Up!.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package edu.umich.gopalkri.wakeup;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.Locale;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.location.Address;
 import android.location.Geocoder;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.Toast;
 
 import com.google.android.maps.GeoPoint;
 
 import edu.umich.gopalkri.wakeup.data.Alarm;
 import edu.umich.gopalkri.wakeup.data.AlarmAlreadyExistsException;
 import edu.umich.gopalkri.wakeup.data.Alarms;
 import edu.umich.gopalkri.wakeup.data.InvalidAlarmNameException;
 import edu.umich.gopalkri.wakeup.data.Alarm.InvalidAlarmStringException;
 
 public class EditAlarm extends Activity
 {
     public static final String ALARM_NAME = "ALARM_NAME";
 
     private static final int SELECT_DESTINATION = 1;
 
     private static final String RADIUS_COULD_NOT_BE_PARSED = "The proximity radius you entered could not be parsed as a number. You need to enter a number (can be a decimal). Please fix this and try again.";
     private static final String NO_RADIUS = "You have not entered a proximity radius. Please do so and try again.";
     private static final String INVALID_ALARM_NAME = "The alarm name you supplied is invalid. Alarm names cannot contain the character sequence: \""
             + Alarm.FIELD_SEPARATOR
             + "\" (without the \"). Please enter another name and try again.";
     private static final String NO_ALARM_NAME = "You have not entered a name for this alarm. Please do so first and try again.";
     private static final String ALARM_ALREADY_EXISTS = "An alarm with this name already exists. Please pick another name, or delete the existing alarm first and then try again.";
     private static final String LOCATION_NOT_SET = "You have not set a location for this alarm. Please do so and try again.";
     private static final String UNABLE_TO_GEOCODE = "Wake Up! was unable to geocode the street address you provided. Please try a different one, or pick a desination using the map.";
     private static final String LOCATION_SET = "The destination you picked is: ";
 
     /**
      * @see android.app.Activity#onCreate(android.os.Bundle)
      */
     @Override
     protected void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.edit_alarm);
 
         mAlarms = new Alarms(this);
         mSelectDestinationIntent = new Intent(this, SelectDestination.class);
 
         Bundle extras = getIntent().getExtras();
         if (extras != null)
         {
             String alarmName = extras.getString(ALARM_NAME);
             if (alarmName != null)
             {
                 mThisAlarm = mAlarms.getAlarm(alarmName);
                 mNewAlarm = false;
                 mLocationSet = true;
                 mOriginalAlarmName = alarmName;
             }
             else
             {
                 mThisAlarm = new Alarm();
                 mNewAlarm = true;
                 mLocationSet = false;
                 mOriginalAlarmName = null;
             }
         }
         else
         {
             mThisAlarm = new Alarm();
             mNewAlarm = true;
             mLocationSet = false;
             mOriginalAlarmName = null;
         }
 
         setupUI();
     }
 
     /**
      * @see android.app.Activity#onActivityResult(int, int,
      *      android.content.Intent)
      */
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data)
     {
         super.onActivityResult(requestCode, resultCode, data);
 
         if (data == null) // Destination was not selected.
         {
             return;
         }
         Bundle bundle = data.getExtras();
 
         switch (requestCode)
         {
         case SELECT_DESTINATION:
             String locStr = bundle.getString(SelectDestination.LOCATION_STRING);
             if (locStr != null)
             {
                 GeoPoint selectedLocation = Utilities.decodeLocationString(locStr);
                 Double latitude = Utilities.getLatitudeFromGeoPoint(selectedLocation);
                 Double longitude = Utilities.getLongitudeFromGeoPoint(selectedLocation);
                 mThisAlarm.setLatitude(latitude);
                 mThisAlarm.setLongitude(longitude);
                 mLocationSet = true;
                 Toast toast = Toast.makeText(this, LOCATION_SET + latitude.toString() + ", "
                         + longitude.toString() + ".", Toast.LENGTH_LONG);
                 toast.show();
             }
         }
     }
 
     private void setupUI()
     {
         // Setup all EditText fields.
         mETAlarmName = (EditText) findViewById(R.id.edit_alarm_alarm_name);
         mETAlarmRadius = (EditText) findViewById(R.id.edit_alarm_radius);
         mETAlarmSearch = (EditText) findViewById(R.id.edit_alarm_address_search);
 
         // Setup Units spinner.
         mUnitsSpinner = (Spinner) findViewById(R.id.edit_alarm_units_spinner);
         ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.units,
                 android.R.layout.simple_spinner_item);
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         mUnitsSpinner.setAdapter(adapter);
 
         // Setup Search For Address button.
         Button searchAddress = (Button) findViewById(R.id.edit_alarm_search_btn);
         searchAddress.setOnClickListener(new View.OnClickListener()
         {
             public void onClick(View v)
             {
                 GeoPoint loc = geocodeAddress(mETAlarmSearch.getText().toString().trim());
                 if (loc == null)
                 {
                     // FIXME CHeck that EditAlarm.this actually works.
                     Utilities.createAlertDialog(EditAlarm.this, Utilities.ERROR, UNABLE_TO_GEOCODE)
                             .show();
                     return;
                 }
                 String locStr = Utilities.encodeLocation(loc);
                 mSelectDestinationIntent.putExtra(SelectDestination.LOCATION_STRING, locStr);
                 startActivityForResult(mSelectDestinationIntent, SELECT_DESTINATION);
             }
         });
 
         // Setup Save button.
         Button save = (Button) findViewById(R.id.edit_alarm_save);
         save.setOnClickListener(new View.OnClickListener()
         {
             public void onClick(View v)
             {
                 if (!updateAlarm())
                 {
                     return;
                 }
                 try
                 {
                     if (mNewAlarm)
                     {
                         mAlarms.addAlarm(mThisAlarm);
                     }
                     else
                     {
                         mAlarms.updateAlarm(mOriginalAlarmName, mThisAlarm);
                     }
                 }
                 catch (AlarmAlreadyExistsException e)
                 {
                     // An alarm with this name already exists.
                     // FIXME Check that EditAlarm.this actually works.
                     Utilities.createAlertDialog(EditAlarm.this, Utilities.ERROR,
                             ALARM_ALREADY_EXISTS).show();
                     return;
                 }
                 setResult(RESULT_OK);
                 finish();
             }
         });
 
         // Setup Pick From Map button.
         Button pickFromMap = (Button) findViewById(R.id.edit_alarm_pick_from_map);
         pickFromMap.setOnClickListener(new View.OnClickListener()
         {
             public void onClick(View v)
             {
                 mETAlarmSearch.setText("");
                 if (mLocationSet)
                 {
                     String strLoc = Utilities.encodeLocation(mThisAlarm.getLatitude(), mThisAlarm.getLongitude());
                     mSelectDestinationIntent.putExtra(SelectDestination.LOCATION_STRING, strLoc);
                 }
                 startActivityForResult(mSelectDestinationIntent, SELECT_DESTINATION);
             }
         });
         if (mNewAlarm)
         {
             pickFromMap.setText(R.string.edit_alarm_pick_from_map);
         }
         else
         {
             pickFromMap.setText(R.string.edit_alarm_view_on_map);
             populateFields();
         }
     }
 
     private GeoPoint geocodeAddress(String address)
     {
         Geocoder gc = new Geocoder(this, Locale.getDefault());
         List<Address> locations = null;
         try
         {
             locations = gc.getFromLocationName(address, 1); // Only want the
                                                             // best address
                                                             // match.
         }
         catch (IOException e)
         {
             return null;
         }
         if (locations == null)
         {
             return null;
         }
         Address addr = locations.get(0);
         Double lat = addr.getLatitude() * 1E6;
         Double lon = addr.getLongitude() * 1E6;
         return new GeoPoint(lat.intValue(), lon.intValue());
     }
 
     private void populateFields()
     {
         mETAlarmName.setText(mThisAlarm.getName());
         mETAlarmRadius.setText(((Double) mThisAlarm.getRadius()).toString());
         mUnitsSpinner.setSelection(Alarm.UnitsToInt(mThisAlarm.getUnit()));
         mETAlarmSearch.setText("");
     }
 
     private boolean updateAlarm()
     {
         if (!mLocationSet)
         {
             Utilities.createAlertDialog(this, Utilities.ERROR, LOCATION_NOT_SET)
                     .show();
             return false;
         }
         String alarmName = mETAlarmName.getText().toString().trim();
         if (alarmName.compareTo("") == 0)
         {
             Utilities.createAlertDialog(this, Utilities.ERROR, NO_ALARM_NAME).show();
             return false;
         }
         try
         {
             mThisAlarm.setName(alarmName);
         }
         catch (InvalidAlarmNameException ex)
         {
             Utilities.createAlertDialog(this, Utilities.ERROR, INVALID_ALARM_NAME).show();
             return false;
         }
         String radiusStr = mETAlarmRadius.getText().toString().trim();
         if (radiusStr.compareTo("") == 0)
         {
            Utilities.createAlertDialog(this, Utilities.ERROR, NO_RADIUS);
             return false;
         }
         double radius;
         try
         {
             radius = Double.parseDouble(radiusStr);
         }
         catch (NumberFormatException ex)
         {
             Utilities.createAlertDialog(this, Utilities.ERROR, RADIUS_COULD_NOT_BE_PARSED).show();
             return false;
         }
         mThisAlarm.setRadius(radius);
         try
         {
             mThisAlarm.setUnit(Alarm.IntToUnits(mUnitsSpinner.getSelectedItemPosition()));
         }
         catch (InvalidAlarmStringException e)
         {
             // This should not happen.
             throw new RuntimeException("Units spinner and enum Units are out of sync.");
         }
         return true;
     }
 
     private EditText mETAlarmName;
     private EditText mETAlarmRadius;
     private EditText mETAlarmSearch;
     private Spinner mUnitsSpinner;
 
     private Alarms mAlarms;
 
     private Intent mSelectDestinationIntent;
 
     private Alarm mThisAlarm;
     private boolean mNewAlarm;
     private boolean mLocationSet;
     private String mOriginalAlarmName;
 }
