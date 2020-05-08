 package com.steto.diaw.activity;
 
 import java.io.Serializable;
 import java.sql.SQLException;
 import java.util.List;
 
 import roboguice.activity.RoboExpandableListActivity;
 import roboguice.inject.ContentView;
 import roboguice.util.Ln;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.ResultReceiver;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.google.inject.Inject;
 import com.steto.diaw.adapter.SeasonWithEpisodesExpandableAdapter;
 import com.steto.diaw.dao.DatabaseHelper;
 import com.steto.diaw.dao.EpisodeDao;
 import com.steto.diaw.dao.ShowDao;
 import com.steto.diaw.helper.TranslucideActionBarHelper;
 import com.steto.diaw.model.Episode;
 import com.steto.diaw.model.Season;
 import com.steto.diaw.model.Show;
 import com.steto.diaw.service.BannerService;
 import com.steto.diaw.service.ParseGetEpisodesService;
 import com.steto.diaw.service.TVDBService;
 import com.steto.projectdiaw.R;
 
 @ContentView(R.layout.activity_show_detail)
 public class ShowDetailActivity extends RoboExpandableListActivity {
 
 	private static final int SHORT_SUMMARY_LENGHT = 100;
 	private static final String HINT_TO_BE_CONTINUED = "...";
 	public static final String EXTRA_SHOW = "EXTRA_SHOW";
 	public static final String EXTRA_SHOW_NAME = "EXTRA_SHOW_NAME";
 	private static final int REQUEST_RESOLVE_AMBIGUITY = 1000;
 
 	private Show mShow;
 	private List<Season> mListSeasons;
 	private boolean mBannerIsDownloading = false;
 
 	@Inject
 	private DatabaseHelper mDatabaseHelper;
 
 	private View mHeaderContainer;
 	private TranslucideActionBarHelper mActionBarTranslucideHelper;
 	private ResultReceiver mShowResultReceiver = new ShowResultReceiver();
 	private ResultReceiver mBannerResultReceiver = new BannerReceiverExtension();
 
 	public void onCreate(Bundle savedInstanceState) {
 		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
 		super.onCreate(savedInstanceState);
 		mActionBarTranslucideHelper = new TranslucideActionBarHelper(getActionBar());
 
 		if (!processExtras()) {
 			finish();
 			return;
 		}
 
 		readDatabase();
 		mActionBarTranslucideHelper.initActionBar(this, mShow.getShowName(), "", true, R.drawable.ab_solid_dark_holo);
 
 		processDataInLayout();
 		mActionBarTranslucideHelper.setOnScrollChangedListener(getExpandableListView());
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 
 		if (requestCode == REQUEST_RESOLVE_AMBIGUITY && resultCode == RESULT_OK) {
 			if (!manageResultResolveAmbiguity(data)) {
 				finish();
 			}
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.show_detail, menu);
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 			case android.R.id.home:
 				onBackPressed();
 				return true;
 			case R.id.menu_rename:
 				renameShow();
 				return true;
 
 			default:
 				return super.onOptionsItemSelected(item);
 		}
 	}
 
 	private void searchSerieWithAnotherName() {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		LayoutInflater inflater = getLayoutInflater();
 		View viewInDialog = inflater.inflate(R.layout.dialog_rename, null);
 		final EditText nameShowEditText = (EditText) viewInDialog.findViewById(R.id.dialog_rename_episode_name);
 		nameShowEditText.setHint(R.string.dialog_search_show_name);
 		nameShowEditText.setText(mShow.getShowName());
 		builder.setCancelable(false);
 		builder.setView(viewInDialog)
 				// Add action buttons
 				.setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int id) {
 						String showNameBeforeRename = mShow.getShowName();
 						mShow.setShowName(nameShowEditText.getText().toString());
 						setProgressBarIndeterminateVisibility(true);
 						launchSerieService(showNameBeforeRename);
 					}
 				})
 				.setNegativeButton(R.string.btn_annuler, new DialogInterface.OnClickListener() {
 
 					public void onClick(DialogInterface dialog, int id) {
 						dialog.dismiss();
 					}
 				})
 				.setTitle(R.string.search_show);
 		AlertDialog dialog = builder.create();
 		dialog.show();
 	}
 
 	private void renameShow() {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		LayoutInflater inflater = getLayoutInflater();
 		View viewInDialog = inflater.inflate(R.layout.dialog_rename, null);
 		final EditText nameShowEditText = (EditText) viewInDialog.findViewById(R.id.dialog_rename_episode_name);
 		nameShowEditText.setHint(R.string.dialog_rename_show_name);
 		nameShowEditText.setText(mShow.getShowName());
 
 		builder.setView(viewInDialog)
 				// Add action buttons
 				.setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int id) {
 						setProgressBarIndeterminate(true);
 						String newName = nameShowEditText.getText().toString();
 						// rename all tv shows in this activity
 						try {
 							EpisodeDao episodeDao = mDatabaseHelper.getDao(Episode.class);
 							ShowDao showDao = mDatabaseHelper.getDao(Show.class);
 							for (Season season : mListSeasons) {
 								for (Episode episode : season.getEpisodes()) {
 									episodeDao.delete(episode);
 									episode.setShowName(newName); // change id : TODO in DAO method...
 									episodeDao.create(episode);
 								}
 							}
 							Show newShow = new Show(newName);
 							showDao.createIfNotExists(newShow);
 							showDao.delete(mShow);
 						} catch (SQLException e) {
 							Ln.e(e);
 						}
 						// new ShowDetailActivity
 						Intent intentDetail = new Intent(ShowDetailActivity.this, ShowDetailActivity.class);
 						intentDetail.putExtra(ShowDetailActivity.EXTRA_SHOW_NAME, newName);
 						startActivity(intentDetail);
 						finish();
 					}
 				})
 				.setNegativeButton(R.string.btn_annuler, new DialogInterface.OnClickListener() {
 
 					public void onClick(DialogInterface dialog, int id) {
 						dialog.dismiss();
 					}
 				})
 				.setTitle(R.string.rename_show);
 		AlertDialog dialog = builder.create();
 		dialog.show();
 	}
 
 	private void resolveAmbiguity(List<Show> ambiguous) {
 		Intent intent = new Intent(this, AmbiguityShow.class);
 		intent.putExtra(AmbiguityShow.INPUT_POTENTIAL_SHOW, (Serializable) ambiguous);
 		intent.putExtra(AmbiguityShow.INPUT_AMBIGUOUS_SHOW, (Serializable) mShow);
 		startActivityForResult(intent, REQUEST_RESOLVE_AMBIGUITY);
 	}
 
 	private boolean manageResultResolveAmbiguity(Intent data) {
 		String showName = data.getExtras().getString(ShowDetailActivity.EXTRA_SHOW_NAME);
 		try {
 			mShow = ((ShowDao) mDatabaseHelper.getDao(Show.class)).queryFromName(showName);
 			setProgressBarIndeterminateVisibility(true);
 			launchSerieService();
 			launchBannerService();
 			return true;
 		} catch (SQLException e) {
 			Ln.e(e);
 			return false;
 		}
 	}
 
 	private boolean processExtras() {
 		if (getIntent().getExtras() != null) {
 			mShow = (Show) (getIntent().getExtras().get(EXTRA_SHOW));
 			if (mShow != null) {
 				return true;
 			} else {
 				String showName = getIntent().getExtras().getString(ShowDetailActivity.EXTRA_SHOW_NAME);
 				try {
 					mShow = ((ShowDao) mDatabaseHelper.getDao(Show.class)).queryFromName(showName);
 					return true;
 				} catch (SQLException e) {
 					Ln.e(e);
 					return false;
 				}
 			}
 		}
 		return false;
 	}
 
 	private void readDatabase() {
 		try {
 			ShowDao showDao = mDatabaseHelper.getDao(Show.class);
 			mListSeasons = showDao.getSeasonsFromShow(mShow);
 		} catch (SQLException e) {
 			Ln.e(e);
 			Toast.makeText(this, "Erreur lors de la récupération des episodes de la serie", Toast.LENGTH_SHORT).show();
 		}
 
 		setProgressBarIndeterminateVisibility(true);
 		launchSerieService();
 		launchBannerService();
 	}
 
 	private void launchSerieService() {
 		launchSerieService(null);
 	}
 
 	private void launchSerieService(String explicitShowName) {
 		Intent intent = new Intent(this, TVDBService.class);
 		intent.putExtra(TVDBService.INPUT_SERIE, mShow);
		intent.putExtra(TVDBService.INPUT_REAL_NAME, explicitShowName != null ? explicitShowName : null);
 		intent.putExtra(TVDBService.INPUT_RESULTRECEIVER, mShowResultReceiver);
 		startService(intent);
 	}
 
 	private void launchBannerService() {
 		if (mShow.getBannerURL() != null && mShow.getBanner() == null && !mBannerIsDownloading) {
 			mBannerIsDownloading = true;
 			Intent intent = new Intent(this, BannerService.class);
 			intent.putExtra(BannerService.INPUT_SERIE, mShow);
 			intent.putExtra(BannerService.INPUT_RECEIVER, mBannerResultReceiver);
 			startService(intent);
 		}
 	}
 
 	private void processDataInLayout() {
 		// List
 		SeasonWithEpisodesExpandableAdapter adapter = new SeasonWithEpisodesExpandableAdapter(this, mListSeasons);
 		mHeaderContainer = getLayoutInflater().inflate(R.layout.header_show, null);
 		getExpandableListView().addHeaderView(mHeaderContainer);
 		setListAdapter(adapter);
 
 		// ActionBar
 		mActionBarTranslucideHelper.setHeaderContainer(mHeaderContainer);
 
 	}
 
 	private void refreshLayout() {
 
 		if (mShow != null) {
 			findViewById(R.id.activity_show_detail_info_layout).setVisibility(View.VISIBLE);
 			// TVDBContainerData
 			TextView title = (TextView) mHeaderContainer.findViewById(R.id.activity_show_detail_title);
 			title.setText(mShow.getShowName());
 			// TVDBContainerData
 			TextView genre = (TextView) mHeaderContainer.findViewById(R.id.activity_show_detail_genre);
 			genre.setText(mShow.getGenre());
 			// TVDBContainerData
 			TextView onAir = (TextView) mHeaderContainer.findViewById(R.id.activity_show_detail_on_air);
 			onAir.setText(mShow.getDateDebut() != null ? mShow.getDateDebut().toString() : "loading");
 			// TVDBContainerData
 			TextView statut = (TextView) mHeaderContainer.findViewById(R.id.activity_show_detail_statut);
 			statut.setText(mShow.getStatus());
 			// TVDBContainerData
 			final TextView summary = (TextView) mHeaderContainer.findViewById(R.id.activity_show_detail_summary);
 			if (mShow.getResume().length() > SHORT_SUMMARY_LENGHT) {
 				summary.setText(mShow.getResume().substring(0, SHORT_SUMMARY_LENGHT) + HINT_TO_BE_CONTINUED);
 				summary.setOnClickListener(new OnSummaryClickListener(summary));
 			} else {
 				summary.setText(mShow.getResume());
 			}
 
 			if (mShow.getBanner() != null) {
 				// TVDBContainerData
 				ImageView bannerView = (ImageView) mHeaderContainer.findViewById(R.id.activity_show_detail_image);
 				bannerView.setImageBitmap(mShow.getBannerAsBitmap());
 			}
 		}
 	}
 
 	private final class OnSummaryClickListener implements OnClickListener {
 
 		private final TextView summary;
 
 		private OnSummaryClickListener(TextView summary) {
 			this.summary = summary;
 		}
 
 		@Override
 		public void onClick(View v) {
 			int nbChar = summary.getText().length();
 			if (nbChar == SHORT_SUMMARY_LENGHT + HINT_TO_BE_CONTINUED.length()) {
 				summary.setText(mShow.getResume());
 			} else {
 				summary.setText(mShow.getResume().substring(0, SHORT_SUMMARY_LENGHT) + HINT_TO_BE_CONTINUED);
 			}
 
 		}
 	}
 
 	private final class BannerReceiverExtension extends ResultReceiver {
 
 		private BannerReceiverExtension() {
 			super(new Handler());
 		}
 
 		@Override
 		protected void onReceiveResult(int resultCode, Bundle resultData) {
 			super.onReceiveResult(resultCode, resultData);
 			Ln.i("onResult");
 			setProgressBarIndeterminateVisibility(false);
 			mBannerIsDownloading = false;
 			if (resultCode == ParseGetEpisodesService.RESULT_CODE_OK) {
 				Bitmap banner = (Bitmap) resultData.getParcelable(BannerService.OUTPUT_BITMAP);
 				mShow.setBanner(banner);
 				refreshLayout();
 			} else {
 				Toast.makeText(ShowDetailActivity.this, getString(R.string.msg_erreur_reseau), Toast.LENGTH_SHORT).show();
 			}
 		}
 	}
 
 	private final class ShowResultReceiver extends ResultReceiver {
 
 		private ShowResultReceiver() {
 			super(new Handler());
 		}
 
 		@SuppressWarnings("unchecked")
 		@Override
 		protected void onReceiveResult(int resultCode, Bundle resultData) {
 			super.onReceiveResult(resultCode, resultData);
 			Ln.i("onResult");
 			setProgressBarIndeterminateVisibility(false);
 			if (resultCode == ParseGetEpisodesService.RESULT_CODE_OK) {
 				List<Show> response = (List<Show>) resultData.get(TVDBService.OUTPUT_DATA);
 				if (response != null && !response.isEmpty()) {
 					mShow = response.get(0);
 					refreshLayout();
 					launchBannerService();
 				}
 			} else if (resultCode == TVDBService.RESULT_CODE_AMBIGUITY) {
 				List<Show> response = (List<Show>) resultData.get(TVDBService.OUTPUT_DATA);
 				if (response != null && !response.isEmpty()) {
 					resolveAmbiguity(response);
 				} else {
 					Toast.makeText(ShowDetailActivity.this, "Aucune serie ne correspond à ce nom.", Toast.LENGTH_SHORT).show();
 					searchSerieWithAnotherName();
 				}
 			} else {
 				Toast.makeText(ShowDetailActivity.this, "Unable to get result from service", Toast.LENGTH_SHORT).show();
 			}
 		}
 	}
 }
