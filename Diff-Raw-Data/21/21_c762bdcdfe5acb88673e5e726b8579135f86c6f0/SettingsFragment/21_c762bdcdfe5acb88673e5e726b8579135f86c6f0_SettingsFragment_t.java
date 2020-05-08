 package com.quizz.places.fragments;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.ToggleButton;
 
 import com.quizz.core.activities.BaseQuizzActivity;
import com.quizz.core.application.BaseQuizzApplication;
 import com.quizz.core.fragments.BaseSettingsFragment;
 import com.quizz.core.managers.DataManager;
 import com.quizz.core.utils.PreferencesUtils;
 import com.quizz.core.widgets.QuizzActionBar;
 import com.quizz.places.R;
import com.quizz.places.application.QuizzPlacesApplication;
 import com.quizz.places.db.PlacesDAO;
 
 public class SettingsFragment extends BaseSettingsFragment {
 	private boolean mHideActionBarOnDestroyView = true;
 
 	private ToggleButton mAudioOption;
 	private ToggleButton mVibrationOption;
 	private ToggleButton mExitPopupOption;
 	private Button mResetButton;
 //	private Button clearCacheButton;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		super.onCreateView(inflater, container, savedInstanceState);
 		
 		View view = inflater.inflate(R.layout.fragment_settings, container, false);
 
 		initActionbarView();		
 		initSettingViewsFromPreferences(view);
 		
 		return view;
 	}
 
 	private void initSettingViewsFromPreferences(View pView) {
 
 		mAudioOption = (ToggleButton) pView.findViewById(R.id.AudioOption);
 		mVibrationOption = (ToggleButton) pView.findViewById(R.id.VibrationOption);
 		mExitPopupOption = (ToggleButton) pView.findViewById(R.id.ExitPopupOption);
 		mResetButton = (Button) pView.findViewById(R.id.ResetButton);
 
 		mAudioOption.setOnClickListener(mAudioListener);
 		mVibrationOption.setOnClickListener(mVibrationListener);
 		mExitPopupOption.setOnClickListener(mExitPopupListener);
 		mResetButton.setOnClickListener(mResetListener);
 
 		// init Audio
 		if (!PreferencesUtils.containsAudioPreference(this.getActivity())) {
 			PreferencesUtils.setAudioEnabled(this.getActivity(), false);
 		}
 		mAudioOption.setChecked(PreferencesUtils.isAudioEnabled(this.getActivity()));
 		// init Vibrations
 		if (!PreferencesUtils.containsVibrationPreference(this.getActivity())) {
 			PreferencesUtils.setVibrationEnabled(this.getActivity(), false);
 		}
 		mVibrationOption.setChecked(PreferencesUtils.isVibrationEnabled(this.getActivity()));
 		// init Exit popup
 		if (!PreferencesUtils.containsExitPopupPreference(this.getActivity())) {
 			PreferencesUtils.setExitPopupEnabled(this.getActivity(), true);
 		}
 		mExitPopupOption.setChecked(PreferencesUtils.isExitPopupEnabled(this.getActivity()));
 	}
 	
 	DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
 	    @Override
 	    public void onClick(DialogInterface dialog, int which) {
 	        switch (which){
 	        case DialogInterface.BUTTON_POSITIVE:
 	            //Yes button clicked
 	        	new PlacesDAO(getActivity()).resetDB();
 	        	DataManager.resetGame();
	    		PreferencesUtils.setHintsAvailable(getActivity(),
	    				BaseQuizzApplication.PREF_DEFAULT_UNLOCKED_HINTS_COUNT_VALUE);
 	            break;
 	        case DialogInterface.BUTTON_NEGATIVE:
 	            //No button clicked
 	        	break;
 	        }
 	    }
 	};
 
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 	}
 
 	@Override
 	public void onDestroyView() {
 		super.onDestroyView();
 		if (mHideActionBarOnDestroyView) {
 			if (getActivity() instanceof BaseQuizzActivity) {
 				((BaseQuizzActivity) getActivity()).getQuizzActionBar().hide(
 						QuizzActionBar.MOVE_NORMAL);
 			}
 		}
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		mHideActionBarOnDestroyView = true;
 		if (getActivity() instanceof BaseQuizzActivity) {
 			((BaseQuizzActivity) getActivity()).getQuizzActionBar()
 					.showIfNecessary(QuizzActionBar.MOVE_NORMAL);
 		}
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 	}
 	
 	private void initActionbarView() {
 		QuizzActionBar actionBar = ((BaseQuizzActivity) getActivity()).getQuizzActionBar();
 		actionBar.setCustomView(R.layout.ab_view_settings);
 		View customView = actionBar.getCustomViewContainer();
 		((TextView) customView.findViewById(R.id.ab_settings_middle_text))
 				.setText(R.string.ab_settings_title);
 	}
 
 	// ===========================================================
 	// Listeners
 	// ===========================================================
 	
 	private View.OnClickListener mAudioListener = new OnClickListener() {
 		@Override
 		public void onClick(View v) {
 			PreferencesUtils.setAudioEnabled(getActivity(), mAudioOption.isChecked());
 		}
 	};
 
 	private View.OnClickListener mVibrationListener = new OnClickListener() {
 		@Override
 		public void onClick(View v) {
 			PreferencesUtils.setVibrationEnabled(getActivity(), mVibrationOption.isChecked());
 		}
 	};
 	
 	private View.OnClickListener mExitPopupListener = new OnClickListener() {
 		@Override
 		public void onClick(View v) {
 			PreferencesUtils.setExitPopupEnabled(getActivity(), mExitPopupOption.isChecked());
 		}
 	};
 
 	private View.OnClickListener mResetListener = new OnClickListener() {
 		@Override
 		public void onClick(View v) {
 			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 			builder.setMessage(R.string.confirm_reset)
 				.setPositiveButton(R.string.yes, dialogClickListener)
 			    .setNegativeButton(R.string.no, dialogClickListener).show();
 		}
 	};	
 	
 }
