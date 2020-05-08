 package kpk.dev.d3app.ui.activities;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 
 import kpk.dev.d3app.R;
 import kpk.dev.d3app.adapters.ProfilesAdapter;
 import kpk.dev.d3app.listeners.BaseDataListener;
 import kpk.dev.d3app.listeners.DataReadyListener;
 import kpk.dev.d3app.models.accountmodels.ProfileModel;
 import kpk.dev.d3app.models.accountmodels.interfaces.IProfileModel;
 import kpk.dev.d3app.tasks.BaseJSONAsyncTask.TaskType;
 import kpk.dev.d3app.tasks.BaseJSONAsyncTask;
 import kpk.dev.d3app.tasks.CareerAsyncTask;
 import kpk.dev.d3app.tasks.ModelType;
 import kpk.dev.d3app.tasks.ReadRecordsTask;
 import kpk.dev.d3app.ui.fragments.BaseDialog;
 import kpk.dev.d3app.ui.fragments.BaseDialog.DialogType;
 import kpk.dev.d3app.ui.fragments.AddProfileDialogFragment;
 import kpk.dev.d3app.ui.fragments.ProfileOptionsDialog;
 import kpk.dev.d3app.ui.fragments.WarningDialogFragment;
 import kpk.dev.d3app.ui.fragments.ProfileOptionsDialog.ProfileOptions;
 import kpk.dev.d3app.ui.fragments.ProgressDialogFragment;
 import kpk.dev.d3app.ui.interfaces.IDialogWatcher;
 import kpk.dev.d3app.util.Utils;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.support.v4.app.DialogFragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.ListView;
 
 public class ProfilesListActivity extends AbstractActivity implements IDialogWatcher  {
 	private static final String ADD_PROFILE_DIALOG_TAG = "add_profile_dialog";
 	private static final String PROGRESS_DIALOG_TAG = "progress_dialog";
 	private List<IProfileModel> mProfiles = new ArrayList<IProfileModel>();
 	private ProfilesAdapter mAdapter;
 	private ListView mProfileList;
 	public static final String SELECTED_PROFILE_ID_KEY = "selected_profile_id";
 	public static final String SELECTED_PROFILE_SERVER = "selected_profile_server";
 	public static final String SELECTED_PROFILE_BATTLE_TAG = "selected_profile_battleTag";
 	private Handler mHandler;
 	private static final String WARNING_DIALOG_TAG = "warning_dialog";
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.d3_list_layout);
 		mHandler = new Handler();
 	}
 	
 	private void checkIfDialogIsVisible(){
 		DialogFragment openDialogFragment = (DialogFragment)getSupportFragmentManager().findFragmentByTag(ADD_PROFILE_DIALOG_TAG);
 		if(openDialogFragment != null) {
 			((BaseDialog)openDialogFragment).setDialogWatcher(this);
 		}
 	}
 	
 	private View getHeaderView() {
 		final LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		return inflater.inflate(R.layout.profiles_list_header, null, false);
 	}
 	
 	@Override
 	protected void initComponents() {
 		checkIfDialogIsVisible();
 		mProfileList = (ListView)findViewById(R.id.d3list);
 		final View headerView = getHeaderView();
 		Utils.setFont((ViewGroup)headerView);
 		mProfileList.addHeaderView(headerView);
 		final Button addButton = (Button)headerView.findViewById(R.id.add_profile_button);
 		addButton.setOnClickListener(addProfileListener);
 		queryDatabaseForProfiles();
 		mAdapter = new ProfilesAdapter(this, android.R.layout.simple_list_item_1, mProfiles);
 		mProfileList.setAdapter(mAdapter);
 	}
 	
 	private void queryDatabaseForProfiles() {
 		new ReadRecordsTask(getDatabase(), mDataReadFromDBListener).execute(ModelType.profiles);
 	}
 	
 	private DataReadyListener mDataReadFromDBListener = new DataReadyListener() {
 		@Override
 		public void dataReadyListener(List<IProfileModel> models) {
 			mProfiles.addAll(models);
 			mAdapter.notifyDataSetChanged();
 			mProfileList.setOnItemClickListener(mProfileListItemClickListener);
 			mProfileList.setOnItemLongClickListener(mProfileLongClickListener);
 		}
 
 		@Override
 		public void dataReady(IProfileModel model, boolean newObject,
 				String[] returnedArgs) {
 			
 		}
 	};
 
 	private View.OnClickListener addProfileListener = new View.OnClickListener() {
 		@Override
 		public void onClick(View v) {
 			showBattleTagDialog();
 		}
 	};
 	
 	private void showBattleTagDialog() {
 		final Bundle dialogData = new Bundle();
 		dialogData.putString(BaseDialog.TITLE_KEY, getString(R.string.add_battletag_dialog_title));
 		dialogData.putInt(BaseDialog.DIALOG_LAYOUT_KEY, R.layout.add_battletag_dialog);
 		DialogFragment addProfileDialogFragment = AddProfileDialogFragment.getInstance(dialogData);
 		((BaseDialog)addProfileDialogFragment).setDialogWatcher(this);
 		addProfileDialogFragment.show(getSupportFragmentManager(), ADD_PROFILE_DIALOG_TAG);
 	}
 	
 	private void showProfileOptionsDialog(String title, int position) {
 		final Bundle dialogData = new Bundle();
 		dialogData.putString(BaseDialog.TITLE_KEY, title);
 		dialogData.putInt(BaseDialog.DIALOG_LAYOUT_KEY, R.layout.d3profile_option_layout);
 		dialogData.putInt(ProfileOptionsDialog.PROFILE_POSITION_KEY, position);
 		DialogFragment addProfileDialogFragment = ProfileOptionsDialog.getInstance(dialogData);
 		((BaseDialog)addProfileDialogFragment).setDialogWatcher(this);
 		addProfileDialogFragment.show(getSupportFragmentManager(), ADD_PROFILE_DIALOG_TAG);
 	}
 
 	@Override
 	public void closeDialogs(String tag) {
 		DialogFragment openDialogFragment = (DialogFragment)getSupportFragmentManager().findFragmentByTag(tag);
 		openDialogFragment.dismiss();
 	}
 
 	@Override
 	public void closeDialogsWithData(DialogType type, String tag, Bundle dialogData) {
 		DialogFragment openDialogFragment = (DialogFragment)getSupportFragmentManager().findFragmentByTag(tag);
 		openDialogFragment.dismiss();
 		if(type.name().equalsIgnoreCase(DialogType.addProfileDialog.name())) {
 			StringBuilder battleTag = new StringBuilder(dialogData.getString(AddProfileDialogFragment.BATTLE_TAG_KEY));
 			String regionString = dialogData.getString(AddProfileDialogFragment.SELECTED_REGION_KEY);
 			if(battleTag.length() > 0){
 				battleTag.replace(0, 1, battleTag.substring(0, 1).toUpperCase(new Locale("en-US")));
 			}
 			constructAndExecuteQuery(QueryTypes.loadData, regionString, battleTag.toString());
 		}else if(type.name().equalsIgnoreCase(DialogType.profileOptionsDialog.name())) {
 			final String regionString = dialogData.getString(ProfileOptionsDialog.SELECTED_OPTION_KEY);
 			final int selectedProfile = dialogData.getInt(ProfileOptionsDialog.PROFILE_POSITION_KEY);
 			final ProfileModel profileModel = (ProfileModel)mProfiles.get(selectedProfile);
 			if(regionString.equalsIgnoreCase(ProfileOptions.delete.name())) {
 				constructAndExecuteQuery(QueryTypes.deleteData, profileModel.getServer(), profileModel.getBattleTag());
 			}else if(regionString.equalsIgnoreCase(ProfileOptions.refresh.name())) {
 				constructAndExecuteQuery(QueryTypes.loadData, profileModel.getServer(), profileModel.getBattleTag());
 			}
 		}
 	}
 	
 	private void showProblemDialog(int strId) {
 		final Bundle dialogData = new Bundle();
 		dialogData.putString(BaseDialog.TITLE_KEY, getString(R.string.connectivity_alert_dialog_title));
 		dialogData.putString(WarningDialogFragment.MESSAGE_KEY, getString(strId));
 		dialogData.putInt(BaseDialog.DIALOG_LAYOUT_KEY, R.layout.warning_dialog_background);
 		DialogFragment warningDialogFragment = WarningDialogFragment.getInstance(dialogData);
 		((BaseDialog)warningDialogFragment).setDialogWatcher(this);
 		warningDialogFragment.show(getSupportFragmentManager(), WARNING_DIALOG_TAG);
 	}
 	
 	private void constructAndExecuteQuery(QueryTypes queryType, String region, String battleTag) {
		if(queryType.equals(QueryTypes.loadData)){
			if(!battleTag.contains("#")) {
				showProblemDialog(R.string.battletag_error);
				return;
			}else if(!Utils.isConnectedToInternet(getApplicationContext())){
				showProblemDialog(R.string.data_download_error);
				return;
			}
 		}
 		showProgressDialog();
 		final Bundle bundle = new Bundle();
 		bundle.putString(CareerAsyncTask.REGION_BUNDLE_KEY, region);
 		bundle.putString(CareerAsyncTask.BATTLE_TAG_BUNDLE_KEY, battleTag);
 		BaseJSONAsyncTask task;
 		if(queryType.name().equalsIgnoreCase(QueryTypes.loadData.name())) {
 			task = BaseJSONAsyncTask.getTask(TaskType.CAREER, mDataReadyListener, getDatabase());
 		}else{
 			task = BaseJSONAsyncTask.getTask(TaskType.DELETE, mDataReadyListener, getDatabase());
 		}
 		 
 		task.execute(bundle);
 	}
 	
 	private void showProgressDialog() {
 		final Bundle dialogData = new Bundle();
 		dialogData.putInt(BaseDialog.DIALOG_LAYOUT_KEY, R.layout.progress_dialog_layout);
 		DialogFragment progressDialog = ProgressDialogFragment.getInstance(dialogData);
 		((BaseDialog)progressDialog).setDialogWatcher(this);
 		progressDialog.setCancelable(false);
 		progressDialog.show(getSupportFragmentManager(), PROGRESS_DIALOG_TAG);
 	}
 
 	private BaseDataListener mDataReadyListener = new BaseDataListener() {
 		@Override
 		public void dataReady(IProfileModel model, final boolean newObject, String[] returnedArgs) {
 			final ProfileModel profile = (ProfileModel)model;
 			//KPKLog.d("before " + mProfiles.size());
 			if(profile != null){
 				if(newObject){
 					mProfiles.add(profile);
 				}else{
 					replaceProfile(profile);
 				}
 			}else{
				
 				if(returnedArgs != null) {
 					deleteProfile(returnedArgs);
				}else{
					showProblemDialog(R.string.data_download_error);
 				}
 			}
 			mHandler.post(new Runnable() {
 				@Override
 				public void run() {
 					mAdapter.notifyDataSetChanged();
 					DialogFragment openDialogFragment = (DialogFragment)getSupportFragmentManager().findFragmentByTag(PROGRESS_DIALOG_TAG);
 					if(openDialogFragment != null){
 						openDialogFragment.dismiss();
 					}
 				}
 			});
 		}
 	};
 	
 	private AdapterView.OnItemClickListener mProfileListItemClickListener = new AdapterView.OnItemClickListener() {
 		@Override
 		public void onItemClick(AdapterView<?> list, View item, int position, long id) {
 			if(position - 1 < 0) return;
 			final Intent intent = new Intent(ProfilesListActivity.this, ProfileDetailsActivity.class);
 			intent.putExtra(SELECTED_PROFILE_ID_KEY, position - 1);
 			intent.putExtra(SELECTED_PROFILE_SERVER, ((ProfileModel)mProfiles.get(position - 1)).getServer());
 			intent.putExtra(SELECTED_PROFILE_BATTLE_TAG, ((ProfileModel)mProfiles.get(position - 1)).getBattleTag());
 			startActivity(intent);
 		}
 	};
 	
 	private AdapterView.OnItemLongClickListener mProfileLongClickListener = new AdapterView.OnItemLongClickListener(){
 		public boolean onItemLongClick(android.widget.AdapterView<?> list, View view, int position, long id) {
 			if(position - 1 < 0) return false;
 			showProfileOptionsDialog(((ProfileModel)mProfiles.get(position - 1)).getBattleTag() + ", " + ((ProfileModel)mProfiles.get(position - 1)).getServer(), position - 1);
 			return true;
 		};
 	};
 	
 	private void replaceProfile(ProfileModel profile) {
 		for(int i = 0; i < mProfiles.size(); i++) {
 			if(((ProfileModel)mProfiles.get(i)).getBattleTag().equalsIgnoreCase(profile.getBattleTag()) && ((ProfileModel)mProfiles.get(i)).getServer().equalsIgnoreCase(profile.getServer())){
 				mProfiles.set(i, profile);
 				break;
 			}
 		}
 	}
 	
 	private void deleteProfile(String[] args) {
 		for(int i = 0; i < mProfiles.size(); i++) {
 			if(((ProfileModel)mProfiles.get(i)).getBattleTag().equalsIgnoreCase(args[0]) && ((ProfileModel)mProfiles.get(i)).getServer().equalsIgnoreCase(args[1])){
 				mProfiles.remove(i);
 				break;
 			}
 		}
 	}
 }
