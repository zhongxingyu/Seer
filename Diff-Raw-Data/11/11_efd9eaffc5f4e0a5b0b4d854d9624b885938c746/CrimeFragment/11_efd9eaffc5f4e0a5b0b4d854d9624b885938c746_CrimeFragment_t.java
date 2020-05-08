 package com.bignerdranch.android.criminalintent;
 
 import java.util.Date;
 import java.util.UUID;
 
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.graphics.drawable.BitmapDrawable;
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.NavUtils;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 
 public class CrimeFragment extends Fragment {
 
 	private static final String TAG = "CrimeFragment";
 	
 	public static final String EXTRA_CRIME_ID = "com.bignerdranch.android.criminalintent.crime_id";
 	public static final String DIALOG_DATE = "date";
 	public static final String DIALOG_TIME = "time";
 
 	private static final int REQUEST_DATE = 0;
 	private static final int REQUEST_PHOTO = 1;
 
 	private Crime mCrime;
 	private EditText mTitleField;
 	private Button mDateButton;
 	private Button mTimeButton;
 	private CheckBox mSolvedCheckBox;
 
 	private AlertDialog mAlertDialog;
 
 	private ImageButton mPhotoButton;
 	private ImageView mPhotoView;
 
 	public static CrimeFragment newInstance(UUID crimeId) {
 		Bundle args = new Bundle();
 		args.putSerializable(EXTRA_CRIME_ID, crimeId);
 
 		CrimeFragment fragment = new CrimeFragment();
 		fragment.setArguments(args);
 
 		return fragment;
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		UUID crimeId = (UUID) getArguments().getSerializable(EXTRA_CRIME_ID);
 		setRetainInstance(true); //retain fragment data in between rotations
 		setHasOptionsMenu(true); //Tells this fragment it has an options menu (Or in this case, an up button in the action bar)
 
 		mCrime = CrimeLab.getInstance(getActivity()).getCrime(crimeId);
 	}
 
 	@TargetApi(11)
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle SavedInstanceState) {
 		View v = inflater.inflate(R.layout.fragment_crime, parent, false);
 
 		//If API11 or higher, allow the home icon to act as an "Up" button
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 			if (NavUtils.getParentActivityName(getActivity()) != null) { //Check if there is no parent activity (If there isn't, don't display the "Up" button/caret)
 				getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);	
 			}
 		}
 
 
 
 		mTitleField = (EditText) v.findViewById(R.id.crime_title);
 		mTitleField.setText(mCrime.getTitle());
 		mTitleField.addTextChangedListener(new TextWatcher() {
 
 			@Override
 			public void onTextChanged(CharSequence c, int start, int before, int count) {
 				mCrime.setTitle(c.toString());	
 			}
 
 			@Override
 			public void beforeTextChanged(CharSequence c, int start, int count,
 					int after) {
 			}
 
 			@Override
 			public void afterTextChanged(Editable s) {
 				// Do nothing
 			}
 		});
 
 		mDateButton = (Button) v.findViewById(R.id.crime_date);
 		updateButtonData();
 		mDateButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				FragmentManager fm = getActivity().getSupportFragmentManager();
 				DatePickerFragment dialog = DatePickerFragment.newInstance(mCrime.getDate());
 				dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
 				dialog.show(fm, DIALOG_DATE);
 			}
 
 		});
 
 		mTimeButton = (Button) v.findViewById(R.id.crime_time);
 		mTimeButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				openTimePickerDialog();
 			}
 
 		});
 
 
 		mSolvedCheckBox = (CheckBox) v.findViewById(R.id.crime_solved);
 		mSolvedCheckBox.setChecked(mCrime.isSolved());
 		mSolvedCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 
 			@Override
 			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 				mCrime.setSolved(isChecked);
 			}
 
 		});
 
 		mPhotoButton = (ImageButton) v.findViewById(R.id.crime_imagebutton);
 		mPhotoButton.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				Intent intent = new Intent(getActivity(), CrimeCameraActivity.class);
 				startActivityForResult(intent, REQUEST_PHOTO);
 			}
 		});
 
 		//If camera is not available, disable camera functionality by greying out button and disabling it
 		PackageManager packageManager = getActivity().getPackageManager();
 		if (!packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA) && 
 				!packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
 			mPhotoButton.setEnabled(false);
 		}
 
 		mPhotoView = (ImageView) v.findViewById(R.id.crime_imageView);
 		
 		return v;
 	}
 	
 	/**
 	 * Sets a scaled image on the camera preview ImageView.
 	 */
 	private void showPhoto() {
 		//Sets or resets the image button's image based on the photo taken.
 		Photo photo = mCrime.getPhoto();
 		BitmapDrawable bitmapDrawable = null;
		if (photo != null) {
 			String path = getActivity().getFileStreamPath(photo.getFilename()).getAbsolutePath();
 			bitmapDrawable = PictureUtils.getScaledDrawable(getActivity(), path);
 		}
 		mPhotoView.setImageDrawable(bitmapDrawable);
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 		showPhoto();
 	}
 	
 	@Override
 	public void onStop() {
 		super.onStop();
 		PictureUtils.cleanImageView(mPhotoView); //Clean up imageView memory when activity is stopped
 	}
 	
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		super.onCreateOptionsMenu(menu, inflater);
 		inflater.inflate(R.menu.fragment_crime, menu);
 
 	}
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == REQUEST_DATE) {
 			Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
 			mCrime.setDate(date);
 			updateButtonData();
 			if (resultCode != Activity.RESULT_OK) { 
 				openTimePickerDialog();
 			}
 		} else if (requestCode == REQUEST_PHOTO) {
 			//Create a new photo object and attach it to the crime
 			String filename = data.getStringExtra(CrimeCameraFragment.EXTRA_PHOTO_FILENAME);
 			if (filename != null) {
 				Photo photo = new Photo(filename);
 				mCrime.setPhoto(photo);
 
 				Log.i(TAG, "Crime : " + mCrime.getTitle() + " has a photo with filename: " + filename);
 				showPhoto();
 			}
 		}
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			if (NavUtils.getParentActivityIntent(getActivity()) != null) {
 				NavUtils.navigateUpFromSameTask(getActivity());
 				return true;
 			}
 		case R.id.menu_item_delete_crime:
 			DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
 
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					switch (which) {
 					case DialogInterface.BUTTON_POSITIVE:
 						CrimeLab.getInstance(getActivity()).deleteCrime(mCrime);
 						getActivity().finish(); //Kill this fragment and activity
 						break;
 					case DialogInterface.BUTTON_NEGATIVE:
 						//No button clicked
 						break;
 					}
 				}
 			};
 
 			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 			builder
 			.setMessage("Are you sure?")
 			.setPositiveButton("Yes", dialogClickListener)
 			.setNegativeButton("No", dialogClickListener);
 			mAlertDialog = builder.create();
 			mAlertDialog.show();
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	@Override
 	public void onDestroy() {
 		if (mAlertDialog != null) {
 			mAlertDialog.dismiss();
 		}
 		super.onDestroy();
 	}
 
 
 
 	@Override
 	public void onPause() {
 		if (mAlertDialog != null) {
 			mAlertDialog.dismiss();
 		}
 
 		super.onPause();
 		CrimeLab.getInstance(getActivity()).saveCrimes();
 	}
 
 	private void updateButtonData() {
 		mDateButton.setText(mCrime.getDate().toString());
 	}
 
 	private void openTimePickerDialog() {
 		FragmentManager fm = getActivity().getSupportFragmentManager();
 		TimePickerFragment dialog = TimePickerFragment.newInstance(mCrime.getDate());
 		dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
 		dialog.show(fm, DIALOG_TIME);
 
 	}
 	
 }
