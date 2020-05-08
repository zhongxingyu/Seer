 package com.pk.addits.fragment;
 
 import java.util.List;
 
 import android.app.ActionBar;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Typeface;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.text.Html;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.LinearLayout;
 import android.widget.ShareActionProvider;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.google.analytics.tracking.android.EasyTracker;
 import com.google.analytics.tracking.android.Fields;
 import com.google.analytics.tracking.android.MapBuilder;
 import com.google.analytics.tracking.android.Tracker;
 import com.pk.addits.R;
 import com.pk.addits.activity.ActivityMain;
 import com.pk.addits.adapter.ArticleContentAdapter;
 import com.pk.addits.data.Data;
 import com.pk.addits.misc.CustomMovementMethod;
 import com.pk.addits.misc.URLImageParser;
 import com.pk.addits.model.Article;
 import com.pk.addits.model.ArticleContent;
 import com.pk.addits.view.PkListView;
 import com.pk.addits.view.ZoomImageView;
 import com.squareup.picasso.Picasso;
 
 public class FragmentArticle extends Fragment
 {
 	private SharedPreferences prefs;
 	private boolean adsEnabled;
 	private LinearLayout ad;
 	
 	LoadContentAsyncTask loadContentTask;
 	
 	ActionBar actionBar;
 	static ShareActionProvider mShareActionProvider;
 	static Article Article;
 	//private Thread markReadThread;
 	// private Thread loadCommentsThread;
 	//private Handler mHandler;
 	static MenuItem shareItem;
 	static Menu optionsMenu;
 	private URLImageParser p;
 	
 	ZoomImageView imgHeader;
 	TextView txtTitle;
 	TextView txtAuthor;
 	TextView txtDate;
 	TextView txtContent;
 	PkListView lstContent;
 	private List<ArticleContent> contentList;
 	public static ArticleContentAdapter contentAdapter;
 	private LinearLayout loading;
 	
 	/** Comments have been disabled until it's fixed on the server side! **/
 	// FrameLayout commentCard;
 	// TextView txtLoadComments;
 	// ProgressBar progressBar;
 	// ListView comments;
 	// private List<CommentFeed> commentList;
 	// private CommentsAdapter commentAdapter;
 	
 	private Typeface fontRegular;
 	private Typeface fontBold;
 	private Typeface fontLight;
 	
 	private boolean parseContent;
 	
 	public static FragmentArticle newInstance(Article article)
 	{
 		FragmentArticle f = new FragmentArticle();
 		Bundle bundle = new Bundle();
 		
 		bundle.putInt("ID", article.getID());
 		bundle.putString("Title", article.getTitle());
 		bundle.putString("Description", article.getDescription());
 		bundle.putString("Content", article.getContent());
 		bundle.putString("Comment Feed", article.getCommentFeed());
 		bundle.putString("Author", article.getAuthor());
 		bundle.putString("Date", article.getDate());
 		bundle.putString("Category", article.getCategory());
 		bundle.putString("Image", article.getImage());
 		bundle.putString("URL", article.getURL());
 		bundle.putBoolean("Favorite", article.isFavorite());
 		bundle.putBoolean("Read", article.isRead());
 		
 		f.setArguments(bundle);
 		
 		return f;
 	}
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
 	{
 		View view = inflater.inflate(R.layout.fragment_article, container, false);
 		setHasOptionsMenu(true);
 		
 		imgHeader = (ZoomImageView) view.findViewById(R.id.Image);
 		txtTitle = (TextView) view.findViewById(R.id.txtTitle);
 		txtAuthor = (TextView) view.findViewById(R.id.txtAuthor);
 		txtDate = (TextView) view.findViewById(R.id.txtDate);
 		txtContent = (TextView) view.findViewById(R.id.txtContent);
 		lstContent = (PkListView) view.findViewById(R.id.ArticleContent);
 		loading = (LinearLayout) view.findViewById(R.id.Loading);
 		ad = (LinearLayout) view.findViewById(R.id.ad);
 		p = new URLImageParser(txtContent, getActivity());
 		
 		// commentCard = (FrameLayout) view.findViewById(R.id.commentCard);
 		// txtLoadComments = (TextView) view.findViewById(R.id.txtLoadComments);
 		// progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
 		// comments = (ListView) view.findViewById(R.id.ListView);
 		
 		fontRegular = Typeface.createFromAsset(getActivity().getAssets(), "RobotoSlab-Regular.ttf");
 		fontBold = Typeface.createFromAsset(getActivity().getAssets(), "RobotoSlab-Bold.ttf");
 		fontLight = Typeface.createFromAsset(getActivity().getAssets(), "RobotoSlab-Light.ttf");
 		
 		txtTitle.setTypeface(fontBold);
 		txtAuthor.setTypeface(fontLight);
 		txtDate.setTypeface(fontLight);
 		txtContent.setTypeface(fontRegular);
 		
 		return view;
 	}
 	
 	@Override
 	public void onStart()
 	{
 		super.onStart();
 		
 		Tracker easyTracker = EasyTracker.getInstance(getActivity());
 		
 		// This screen name value will remain set on the tracker and sent with
 		// hits until it is set to a new value or to null.
 		
 		prefs = getActivity().getSharedPreferences(Data.PREFS_TAG, 0);
 		adsEnabled = prefs.getBoolean(Data.PREF_TAG_ADS_ENABLED, true);
 		parseContent = prefs.getBoolean(Data.PREF_TAG_PARSE_ARTICLE_CONTENT, false);
 		actionBar = getActivity().getActionBar();
 		retrieveArguments();
 		//mHandler = new Handler();
 		
 		actionBar.setTitle(Article.getTitle());
 		if (Article.getImage().length() > 0)
 		{
 			Picasso.with(getActivity()).load(Article.getImage()).fit().skipCache().into(imgHeader);
 			imgHeader.setOnClickListener(new View.OnClickListener()
 			{
 				@Override
 				public void onClick(View view)
 				{
 					Data.zoomImageFromThumb(imgHeader, Article.getImage(), getActivity());
 				}
 			});
 		}
 		else
 			imgHeader.setVisibility(View.GONE);
 		
 		txtTitle.setText(Article.getTitle());
 		txtAuthor.setText("Posted by " + Article.getAuthor());
 		txtDate.setText(Data.parseRelativeDate(Article.getDate()));
 		
 		easyTracker.set(Fields.SCREEN_NAME, "Article Screen : " + Article.getTitle());
 		
 		easyTracker.send(MapBuilder.createAppView().build());
 		
 		if (parseContent)
 		{
 			loadContentTask = new LoadContentAsyncTask();
 			loadContentTask.execute();
 		}
 		else
 		{
 			loading.setVisibility(View.GONE);
 			txtContent.setVisibility(View.VISIBLE);
 			
 			if(Data.isNetworkConnected(getActivity()))
 				txtContent.setText(Html.fromHtml(Article.getContent(), p, null));
 			else
 				txtContent.setText(Html.fromHtml(Article.getContent()));
 			txtContent.setMovementMethod(new CustomMovementMethod());
 		}
 		
 		/*
 		 * commentCard.setOnClickListener(new View.OnClickListener() {
 		 * 
 		 * @Override public void onClick(View v) { progressBar.setVisibility(View.VISIBLE); txtLoadComments.setText("Loading Comments..."); commentCard.setClickable(false);
 		 * 
 		 * if (loadCommentsThread == null) { initializeLoadCommentsThread(); loadCommentsThread.start(); } else if (!loadCommentsThread.isAlive()) { initializeLoadCommentsThread();
 		 * loadCommentsThread.start(); } } });
 		 */
 		
 		//if (!Article.isRead())
 		//	markRead();
 		if (!adsEnabled)
 			ad.setVisibility(View.GONE);
 		
 		configureShare();
 	}
 	
 	@Override
 	public void onPause()
 	{
 		super.onPause();
		
		if(loadContentTask != null)
			loadContentTask.cancel(true);
 	}
 	
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
 	{
 		menu.clear();
 		inflater.inflate(R.menu.article, menu);
 		optionsMenu = menu;
 		
 		shareItem = menu.findItem(R.id.Share_Label);
 		mShareActionProvider = (ShareActionProvider) shareItem.getActionProvider();
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item)
 	{
 		switch (item.getItemId())
 		{
 			case R.id.Browser_Label:
 				Intent i = new Intent(Intent.ACTION_VIEW);
 				i.setData(Uri.parse(Article.getURL()));
 				startActivity(i);
 				return true;
 			case R.id.Settings_Label:
 				ActivityMain.callSettings();
 				return true;
 			default:
 				return super.onOptionsItemSelected(item);
 		}
 	}
 	
 	public static void menuVisibility(boolean drawerOpen)
 	{
 		if (shareItem != null && optionsMenu != null)
 			shareItem.setVisible(!drawerOpen);
 		if (!drawerOpen && Article != null)
 			configureShare();
 	}
 	
 	public void retrieveArguments()
 	{
 		Bundle args = getArguments();
 		
 		int ID = args.getInt("ID");
 		String Title = args.getString("Title");
 		String Description = args.getString("Description");
 		String Content = args.getString("Content");
 		String CommentFeed = args.getString("Comment Feed");
 		String Author = args.getString("Author");
 		String Date = args.getString("Date");
 		String Category = args.getString("Category");
 		String Image = args.getString("Image");
 		String URL = args.getString("URL");
 		boolean Favorite = args.getBoolean("Favorite");
 		boolean Read = args.getBoolean("Read");
 		
 		Article = new Article(ID, Title, Description, Content, CommentFeed, Author, Date, Category, Image, URL, Favorite, Read);
 	}
 	
 	public static void configureShare()
 	{
 		String shareBody = Article.getTitle() + "\n\n" + Article.getURL();
 		Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
 		shareIntent.setType("text/plain");
 		shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
 		if (mShareActionProvider != null)
 			mShareActionProvider.setShareIntent(shareIntent);
 	}
 	
 	/*
 	 * private void markRead() { if (markReadThread == null) { initializeMarkReadThread(); markReadThread.start(); } else if (!markReadThread.isAlive()) { initializeMarkReadThread();
 	 * markReadThread.start(); } }
 	 */
 	
 	/*
 	 * private void initializeMarkReadThread() { markReadThread = new Thread() { public void run() { // ActivityMain.NewsFeed[Article.getID()].setRead(true); // ActivityMain.overwriteFeedXML();
 	 * 
 	 * stopThread(this); } }; }
 	 */
 	
 	Runnable loadFail = new Runnable()
 	{
 		public void run()
 		{
 			Toast.makeText(getActivity(), "FAIL...", Toast.LENGTH_SHORT).show();
 		}
 	};
 	
 	private class LoadContentAsyncTask extends AsyncTask<Void, Void, Void>
 	{
 		@Override
 		protected Void doInBackground(Void... params)
 		{
 			contentList = Data.generateArticleContent(Article.getContent());
 			contentAdapter = new ArticleContentAdapter(getActivity(), contentList);
 			try
 			{
 				Thread.sleep(600);
 			}
 			catch (InterruptedException e)
 			{
 				e.printStackTrace();
 			}
 			
 			return null;
 		}
 		
 		@Override
 		protected void onPostExecute(Void p)
 		{
 			loading.setVisibility(View.GONE);
 			lstContent.setVisibility(View.VISIBLE);
 			lstContent.setAdapter(contentAdapter);
 			contentAdapter.notifyDataSetChanged();
 			lstContent.setExpanded(true);
 			lstContent.setDividerHeight(0);
 		}
 	}
 	
 	/*
 	 * private void initializeLoadCommentsThread() { loadCommentsThread = new Thread() { public void run() { try { Data.downloadCommentFeed(Article.getCommentFeed()); commentList =
 	 * Data.retrieveCommentFeed(getActivity()); mHandler.postDelayed(loadComments, 250); } catch (Exception e) { Log.v("Download Comments", "ERROR: " + e.getMessage()); }
 	 * 
 	 * stopThread(this); } }; }
 	 */
 	
 	/*
 	 * Runnable loadComments = new Runnable() { public void run() { commentAdapter = new CommentsAdapter(getActivity(), commentList); comments.setAdapter(commentAdapter);
 	 * commentAdapter.notifyDataSetChanged();
 	 * 
 	 * txtLoadComments.setVisibility(View.GONE); progressBar.setVisibility(View.GONE); } };
 	 */
 }
