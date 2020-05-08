 package com.alphabetbloc.accessmrs.ui.user;
 
 import android.content.Context;
 import android.content.SyncStatusObserver;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.view.GestureDetector;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.alphabetbloc.accessmrs.data.Patient;
 import com.alphabetbloc.accessmrs.providers.DataModel;
 import com.alphabetbloc.accessmrs.providers.Db;
 import com.alphabetbloc.accessmrs.R;
 
 /**
  * Displays ArrayList<Form> in a Time-Separated List with patient header
  * 
  * @author Louis Fazen (louis.fazen@gmail.com) (All Methods except where noted
  *         otherwise)
  * @author Yaw Anokwa (getPatient() taken from ODK Clinic)
  * 
  */
 
 public abstract class BasePatientActivity extends BaseUserListActivity implements SyncStatusObserver {
 
 	// intent extras
 	public static final String KEY_PATIENT_ID = "PATIENT_ID";
 	public static final String KEY_OBSERVATION_FIELD_NAME = "KEY_OBSERVATION_FIELD_NAME";
 	private OnTouchListener mSwipeListener;
 	private GestureDetector mSwipeDetector;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		mSwipeDetector = new GestureDetector(new myGestureListener());
 		mSwipeListener = new OnTouchListener() {
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				return mSwipeDetector.onTouchEvent(event);
 			}
 		};
 	}
 
 	protected void createPatientHeader(Integer patientId) {
 
 		Patient focusPt = getPatient(patientId);
 
 		View v = (View) findViewById(R.id.client_header);
 		if (v != null) {
 			v.setOnTouchListener(mSwipeListener);
 		}
 
 		TextView textView = (TextView) findViewById(R.id.identifier_text);
 		if (textView != null) {
 			textView.setText(focusPt.getIdentifier());
 		}
 
 		textView = (TextView) findViewById(R.id.name_text);
 		if (textView != null) {
 			StringBuilder nameBuilder = new StringBuilder();
 			nameBuilder.append(focusPt.getGivenName());
 			nameBuilder.append(' ');
 			nameBuilder.append(focusPt.getMiddleName());
 			nameBuilder.append(' ');
 			nameBuilder.append(focusPt.getFamilyName());
 			textView.setText(nameBuilder.toString());
 		}
 
 		textView = (TextView) findViewById(R.id.birthdate_text);
 		if (textView != null) {
 			textView.setText(focusPt.getBirthdate());
 		}
 
 		ImageView imageView = (ImageView) findViewById(R.id.gender_image);
 		if (imageView != null) {
 			if (focusPt.getGender().equals("M")) {
 				imageView.setImageResource(R.drawable.male_gray);
 			} else if (focusPt.getGender().equals("F")) {
 				imageView.setImageResource(R.drawable.female_gray);
 			}
 		}
 
 	}
 
 	protected View buildSectionLabel(String section, boolean icon) {
 		View v;
 		LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		v = vi.inflate(R.layout.section_label, null);
 		v.setBackgroundResource(R.color.medium_gray);
 		TextView textView = (TextView) v.findViewById(R.id.name_text);
 		ImageView sectionImage = (ImageView) v.findViewById(R.id.section_image);
 		if (icon)
 			sectionImage.setVisibility(View.VISIBLE);
 		else
 			sectionImage.setVisibility(View.GONE);
 		textView.setText(section);
 		v.setOnTouchListener(mSwipeListener);
 		return (v);
 	}
 
 	// TODO better resource management if Patient were Parceable object?
 	protected Patient getPatient(Integer patientId) {
 
 		Patient p = null;
 
 		Cursor c = Db.open().fetchPatient(patientId);
 
 		if (c != null) {
 			if (c.moveToFirst()) {
 
 				int patientIdIndex = c.getColumnIndex(DataModel.KEY_PATIENT_ID);
 				int identifierIndex = c.getColumnIndex(DataModel.KEY_IDENTIFIER);
 				int givenNameIndex = c.getColumnIndex(DataModel.KEY_GIVEN_NAME);
 				int familyNameIndex = c.getColumnIndex(DataModel.KEY_FAMILY_NAME);
 				int middleNameIndex = c.getColumnIndex(DataModel.KEY_MIDDLE_NAME);
 				int birthDateIndex = c.getColumnIndex(DataModel.KEY_BIRTH_DATE);
 				int genderIndex = c.getColumnIndex(DataModel.KEY_GENDER);
 				int priorityIndex = c.getColumnIndexOrThrow(DataModel.KEY_PRIORITY_FORM_NUMBER);
				int uuidIndex = c.getColumnIndex(DataModel.KEY_UUID);
				
 				p = new Patient();
 				p.setPatientId(c.getInt(patientIdIndex));
 				p.setIdentifier(c.getString(identifierIndex));
 				p.setGivenName(c.getString(givenNameIndex));
 				p.setFamilyName(c.getString(familyNameIndex));
 				p.setMiddleName(c.getString(middleNameIndex));
 				p.setBirthDate(c.getString(birthDateIndex));
 				p.setGender(c.getString(genderIndex));
 				p.setPriorityNumber(c.getInt(priorityIndex));
				p.setUuid(c.getString(uuidIndex));
				
 				if (c.getInt(priorityIndex) > 0) {
 					p.setPriority(true);
 				}
 			}
 			
 			c.close();
 		}
 
 		return p;
 	}
 
 	// BUTTONS: Dont show the menu
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		return false;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		return false;
 	}
 
 	// LIFECYCLE
 	@Override
 	protected void onPause() {
 		super.onPause();
 	}
 }
