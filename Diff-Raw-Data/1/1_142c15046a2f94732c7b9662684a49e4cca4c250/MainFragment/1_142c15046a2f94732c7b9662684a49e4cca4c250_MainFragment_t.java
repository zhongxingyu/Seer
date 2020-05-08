 package de.thm.hcia.twofactorlockscreen.fragments;
 
 import de.thm.hcia.twofactorlockscreen.io.SharedPreferenceIO;
 import android.content.Context;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.TableLayout;
 import android.widget.TextView;
 
 import com.actionbarsherlock.app.SherlockFragment;
 
 import de.thm.hcia.twofactorlockscreen.MainActivity;
 import de.thm.hcia.twofactorlockscreen.R;
 
 public class MainFragment extends SherlockFragment {
 
 	private static Context mContext;
 	private TextView mAppVersion;
 	private TextView mTvInstallSpeechExpl;
 	private TextView mTvInstallPatternExpl;
 	private LinearLayout quickStart;
 	private TableLayout dashboard;
 	private Button mBtnManualInput;
 	private Button mBtnStartAssistent;
 	
 	private TextView loginFails;
 	private TextView loginSuccessfull;
 	
 	private MainActivity mMainActivity;
 	
 	private SharedPreferenceIO sIo;
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		mContext = getActivity();
 		mMainActivity = (MainActivity) getActivity();
 		
 		View v = inflater.inflate(R.layout.main_fragment, null); 
 		quickStart = (LinearLayout) v.findViewById(R.id.quick_start);
 		dashboard = (TableLayout) v.findViewById(R.id.dashboard);	
 		
 		//get app version from AndroidManifest
 		StringBuffer versionName = new StringBuffer().append(mContext.getString(R.string.app_version));
 		versionName.append(" ").append(MainActivity.getAppVersion());
 
 		mAppVersion = (TextView) v.findViewById(R.id.tv_app_version);
 		mAppVersion.setText(versionName.toString());
 		mTvInstallSpeechExpl = (TextView) v.findViewById(R.id.tv_install_speech_explanation);
 		mTvInstallPatternExpl = (TextView) v.findViewById(R.id.tv_install_pattern_explanation);
 		mBtnManualInput = (Button) v.findViewById(R.id.btn_goto_manuel_input);
 		mBtnStartAssistent = (Button) v.findViewById(R.id.btn_start_assistent);
 		
 		sIo = new SharedPreferenceIO(mMainActivity.getBaseContext());
 		
 		setUpOnClickListeners();
 		
 		
 			
 		return v;
 	}
 	
 	public void onStart(){
 		super.onStart(); 
 		mMainActivity.checkInstallation();
 		if(!mMainActivity.isPatternInstalled() && !mMainActivity.isSpeechInstalled()){
 			//Nothing installed set prototype start invisible
 			quickStart.setVisibility(View.VISIBLE);
 		}else if(!mMainActivity.isPatternInstalled() && mMainActivity.isSpeechInstalled()){
 			//Pattern not installed
 			mTvInstallPatternExpl.setVisibility(View.VISIBLE);
 			mBtnManualInput.setVisibility(View.VISIBLE);
 		}else if(!mMainActivity.isSpeechInstalled() && mMainActivity.isPatternInstalled()){
 			//Speech not installed
 			mTvInstallSpeechExpl.setVisibility(View.VISIBLE);
 			mBtnManualInput.setVisibility(View.VISIBLE);
 		}else{
 			//Everything installed --> show start prototype
 			dashboard.setVisibility(View.VISIBLE);
 			
 			loginFails = (TextView) dashboard.findViewById(R.id.tv_logins_failed_count);
 			loginSuccessfull = (TextView) dashboard.findViewById(R.id.tv_logins_successfull_count);
 			
 			//Hier toDo
 			//loginFails.setText(sIo.getIncrementLoginsFailed());
			
 		}
 	}
 	
 	/**
 	 * Set up all on click listeners
 	 */
 	private void setUpOnClickListeners(){
 		mBtnStartAssistent.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				mMainActivity.switchContent(new AssistentFragment());					
 			}
 		});
 		
 		mBtnManualInput.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				mMainActivity.switchContent(new ManualInputFragment());
 			}
 		});
 	}
 	
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 	}
 	
 }
