 package edu.grinnell.sandb;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.NavUtils;
 import android.text.Html;
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 import com.actionbarsherlock.app.SherlockFragment;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 
 import edu.grinnell.sandb.data.Article;
 import edu.grinnell.sandb.data.ArticleTable;
 import edu.grinnell.sandb.img.ImageTable;
 import edu.grinnell.sandb.img.UniversalLoaderUtility;
 
 public class ArticleDetailFragment extends SherlockFragment {
 
 	private static final int SWIPE_MIN_DISTANCE = 200;
 	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
 	static GestureDetector gestureDetector;
 	View.OnTouchListener gestureListener;
 
 	public static final String ARTICLE_ID_KEY = "article_id";
 	private Article mArticle;
 	protected UniversalLoaderUtility mLoader;
 
 	public static final String TAG = "ArticleDetailFragment";
 
 	public ArticleDetailFragment() {
 		super();
 	}
 
 	@Override
	// public void onCreate(Bundle ofJoy) {
	// super.onCreate(ofJoy);
 	public void onCreate(Bundle ofJoy) {
 		super.onCreate(ofJoy);
 		setHasOptionsMenu(true);
 
 		gestureListener = new View.OnTouchListener() {
 			public boolean onTouch(View v, MotionEvent event) {
 				return gestureDetector.onTouchEvent(event);
 			}
 		};
 
 		gestureDetector = new GestureDetector(getSherlockActivity(),
 				new GestureDetector.SimpleOnGestureListener() {
 					@Override
 					public boolean onFling(MotionEvent e1, MotionEvent e2,
 							float velocityX, float velocityY) {
 						if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
 								&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
 							getSherlockActivity().onBackPressed();
 							return true;
 						} else
 							return false;
 					}
 				});
 
 		Bundle b = (ofJoy == null) ? getArguments() : ofJoy;
 
 		if (b != null) {
 			int id = b.getInt(ARTICLE_ID_KEY);
 			ArticleTable table = new ArticleTable(getSherlockActivity());
 			table.open();
 			Log.d(TAG, "Looking for article with id = " + id);
 			mArticle = table.findById(id);
 		}
 
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 
 		View rootView = inflater.inflate(R.layout.fragment_article_detail,
 				container, false);
 
 		// add the author to the article
 		((TextView) rootView.findViewById(R.id.article_author)).setText("By: "
 				+ mArticle.getAuthor());
 
 		// add the date to the article
 		((TextView) rootView.findViewById(R.id.article_date)).setText(mArticle
 				.getPubDate().toString());
 
 		// add the title to the article
 		((TextView) rootView.findViewById(R.id.article_title)).setText(mArticle
 				.getTitle());
 		
 		LinearLayout body = (LinearLayout) rootView.findViewById(R.id.article_body_group);
 		mLoader = new UniversalLoaderUtility();
 
 		String bodyHTML = mArticle.getBody();
 		
 		// make text more readable
 		bodyHTML = bodyHTML.replaceAll("<br />", "<br><br>");
 
 		// remove images
 		// bodyHTML = bodyHTML.replaceAll("<a.+?</a>", "");
 		// bodyHTML = bodyHTML.replaceAll("<div.+?</div>", "");
 		String imgtags="<img.+?>";
 		String[] sections = bodyHTML.split(imgtags);
 		
 		ImageTable imgTable = new ImageTable(getActivity());
 		imgTable.open();
 		String[] urls = imgTable.findUrlsByArticleId(mArticle.getId());
 		final int maxUrls = (urls == null) ? 0 : urls.length;
 		LayoutInflater i = getActivity().getLayoutInflater();
 		int cnt = 0;
 		for (String section : sections) {
 			String url = (cnt < maxUrls) ? urls[cnt++] : null;
 			addSectionViews(body, i, section, url);
 		}
 
 		body.setOnTouchListener(gestureListener);
 
 		Log.d(TAG, mArticle.getTitle());
 		return rootView;
 	}
 
 	private void addSectionViews(ViewGroup v, LayoutInflater li, String text, String img) {
 		
 		if (img != null) {
 			ImageView imgView = (ImageView) li.inflate(R.layout.img_section, v, false);
 			// open image pager if image is clicked
 			OnClickListener imgClick = new OnClickListener() {
 				public void onClick(View v) {
 
 					ImageTable imgTable = new ImageTable(getSherlockActivity());
 					imgTable.open();
 
 					int id = mArticle.getId();
 					String[] URLS = imgTable.findUrlsByArticleId(id);
 
 					for (int i = 0; i < URLS.length; i++) {
 						URLS[i] = mLoader.getHiResImage(URLS[i]);
 						// System.out.println(URLS[i]);
 					}
 
 					Intent intent = new Intent(getSherlockActivity(),
 							ImagePagerActivity.class);
 					intent.putExtra("ArticleImages", URLS);
 					intent.putExtra("ImageTitles",
 							imgTable.findTitlesbyArticleId(id));
 
 					imgTable.close();
 					startActivity(intent);
 				}
 			};
 
 			imgView.setOnTouchListener(gestureListener);
 			imgView.setOnClickListener(imgClick);
 
 			mLoader.loadHiResArticleImage(img, imgView, getActivity());
 			v.addView(imgView);
 		}
 		
 		if (text != null) {
 			TextView tv = (TextView) li.inflate(R.layout.text_section, v, false);
 			tv.setText(Html.fromHtml(text));
 			v.addView(tv);
 		}
 	}
 	
 	@Override
 	public void onResume() {
 		super.onResume();
 		if (mArticle == null) {
 			// Navigate Up..
 			Intent upIntent = new Intent(getSherlockActivity(),
 					MainActivity.class);
 			NavUtils.navigateUpTo(getSherlockActivity(), upIntent);
 		}
 
 	}
 
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		inflater.inflate(R.menu.article_detail_menu, menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 
 		switch (item.getItemId()) {
 		case R.id.menu_settings:
 			// startActivityForResult(new Intent(this, PrefActiv.class), PREFS);
 			break;
 		case R.id.menu_share:
 			share();
 			break;
 		case R.id.menu_share2:
 			share();
 			break;
 		default:
 			break;
 		}
 		return false;
 	}
 
 	public void share() {
 
 		Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
 		sharingIntent.setType("text/plain");
 
 		String shareBody = mArticle.getLink();
 		sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
 				"S&B Article Link");
 		sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
 
 		// Share to any compatible app on the device
 		startActivity(Intent.createChooser(sharingIntent, "Share via"));
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		outState.putInt(ARTICLE_ID_KEY, mArticle.getId());
 		super.onSaveInstanceState(outState);
 	}
 }
