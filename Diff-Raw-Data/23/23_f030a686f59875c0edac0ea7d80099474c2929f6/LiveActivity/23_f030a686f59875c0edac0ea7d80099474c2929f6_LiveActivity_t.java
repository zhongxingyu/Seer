 package de.boomboxbeilstein.android2.ui;
 
 import org.joda.time.DateTimeZone;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.TextView;
 import de.boomboxbeilstein.android2.InfoProvider;
 import de.boomboxbeilstein.android2.PlayerInfo;
 import de.boomboxbeilstein.android2.R;
 import de.boomboxbeilstein.android2.Show;
 import de.boomboxbeilstein.android2.ShowInfo;
 import de.boomboxbeilstein.android2.utils.Images;
 import de.boomboxbeilstein.android2.utils.Strings;
 import de.boomboxbeilstein.android2.utils.Web;
 import de.boomboxbeilstein.android2.views.MarqueeTextView;
 
public abstract class LiveActivity extends BaseActivity {
 	private String lastShowImagerURL = "";
 	
 	private static final DateTimeFormatter timeFormat =
 		DateTimeFormat.shortTime()
 			.withZone(DateTimeZone.getDefault());
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState, int layoutID) {
 		super.onCreate(savedInstanceState, layoutID);
 		// Do it before any call to Web class
 		Web.loadCookies(this);
 	}
 
 	public void onResume() {
 		super.onResume();
 		InfoProvider.setUpdatedHandler(new Runnable() {
 			public void run() {
 				runOnUiThread(new Runnable() {
 					public void run() {
 						updateUI();
 					}
 				});
 			}
 		});
 		updateUI();
 	}
 
 	public void onPause() {
 		super.onPause();
 		InfoProvider.clearUpdatedHandler();
 		Web.saveCookies(this);
 	}
 	
 	protected void updateUI() {
 		
 	}
 
 	protected void updateShowUI() {
 		PlayerInfo info = InfoProvider.getCurrentInfo();
 		if (info == null)
 			return;
 		
 		// Show
 		ShowInfo showInfo = info.getShowInfo();
 		Show show = showInfo != null ? showInfo.getShow() : null;
 		View showWrap = findViewById(R.id.show_wrap);
 		if (show != null) {
 			if (showWrap != null)
 				showWrap.setVisibility(View.VISIBLE);
 			MarqueeTextView showHeader = (MarqueeTextView)findViewById(R.id.show_header);
 			if (showInfo.isNext()) {
 				String time = show.getStartTime().toString(timeFormat);
 				showHeader.setTextLazily(getResources().getString(R.string.next_show, time));
 			} else
 				showHeader.setTextLazily(getResources().getString(R.string.current_show));
 			
 			MarqueeTextView showTitle = (MarqueeTextView) findViewById(R.id.show_title);
 			showTitle.setTextLazily(show.getTitle());	
 			TextView showDescription = (TextView) findViewById(R.id.show_description);
 			if (showDescription instanceof MarqueeTextView)
 				((MarqueeTextView)showDescription).setTextLazily(show.getText());
 			else
 				showDescription.setText(show.getText());
 			
 			if (lastShowImagerURL == null || !lastShowImagerURL.equals(show.getImageURL())) {
 				ImageView showImage = (ImageView) findViewById(R.id.show_image);
 				if (show.getImageURL() != null && !show.getImageURL().equals(""))
 					Images.loadImageAsynchronously(show.getImageURL(), showImage, this);
 				else
 					showImage.setImageBitmap(null);
 				lastShowImagerURL = show.getImageURL();
 			}
 			
 			MarqueeTextView showPreview = (MarqueeTextView)findViewById(R.id.show_preview);
 			if (!Strings.isEmpty(showInfo.getPreviewTitle())) {
 				showPreview.setTextLazily(getResources().getString(R.string.show_preview, showInfo.getPreviewTitle()));
 				showPreview.setVisibility(View.VISIBLE);
 			} else
 				showPreview.setVisibility(View.GONE);
 		} else {
 			if (showWrap != null)
 				showWrap.setVisibility(View.GONE);
 		}
 	}
 }
