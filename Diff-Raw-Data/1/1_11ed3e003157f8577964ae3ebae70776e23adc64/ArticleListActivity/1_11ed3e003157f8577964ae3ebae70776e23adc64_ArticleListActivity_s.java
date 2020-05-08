 package nz.gen.wellington.guardian.android.activities;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import nz.gen.wellington.guardian.android.R;
 import nz.gen.wellington.guardian.android.activities.ui.ArticleClicker;
 import nz.gen.wellington.guardian.android.activities.ui.TagListPopulatingService;
 import nz.gen.wellington.guardian.android.api.ArticleDAO;
 import nz.gen.wellington.guardian.android.api.ArticleDAOFactory;
 import nz.gen.wellington.guardian.android.api.ImageDAO;
 import nz.gen.wellington.guardian.android.dates.DateTimeHelper;
 import nz.gen.wellington.guardian.android.model.Article;
 import nz.gen.wellington.guardian.android.model.ArticleBundle;
 import nz.gen.wellington.guardian.android.model.ArticleSet;
 import nz.gen.wellington.guardian.android.model.Section;
 import nz.gen.wellington.guardian.android.model.SectionColourMap;
 import nz.gen.wellington.guardian.android.model.Tag;
 import nz.gen.wellington.guardian.android.network.HttpFetcher;
 import nz.gen.wellington.guardian.android.network.NetworkStatusService;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.IntentFilter;
 import android.graphics.Bitmap;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.Window;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public abstract class ArticleListActivity extends DownloadProgressAwareActivity {
 	
 	private static final String TAG = "ArticleListActivity";
 	
 	UpdateArticlesHandler updateArticlesHandler;
 	UpdateArticlesRunner updateArticlesRunner;
 	ArticleBundle bundle;
 	Map<String, View> viewsWaitingForTrailImages;
 	protected ArticleDAO articleDAO;
 	protected ImageDAO imageDAO;
 
 	boolean showSeperators = false;
 	boolean showMainImage = true;
 	NetworkStatusService networkStatusService;
 	
 	protected BroadcastReceiver downloadProgressReceiver;
 
 	private Thread loader;
 	private Date loaded;
 	
 	protected String[] permittedRefinements = {"keyword"};
 	
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);        
         requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.main);
 		viewsWaitingForTrailImages = new HashMap<String, View>();
 		articleDAO = ArticleDAOFactory.getDao(this.getApplicationContext());
 		imageDAO = ArticleDAOFactory.getImageDao(this.getApplicationContext());
 
 		downloadProgressReceiver = new DownloadProgressReceiver();
 		
 		networkStatusService = new NetworkStatusService(this);		
 		updateArticlesRunner = new UpdateArticlesRunner(articleDAO, imageDAO, networkStatusService);		
 	}
 	
 	
 	@Override
 	protected void onStart() {
 		super.onStart();	
 		LinearLayout mainPane = (LinearLayout) findViewById(R.id.MainPane);		
 		boolean mainPaneNeedsPopulating = shouldRefreshView(mainPane);
 		if (mainPaneNeedsPopulating) {
 			refresh(false);
 		}
 	}
 
 	
 	protected void refresh(boolean unCached) {
 		//Log.i(TAG, "Refresh requested");
 	
 		if (!networkStatusService.isConnectionAvailable() && unCached) {
 			//Log.i(TAG, "Not refreshing uncached as no connection is available");
 			return;
 		}
 				
 		LinearLayout mainPane = (LinearLayout) findViewById(R.id.MainPane);
 		if (loader == null || !loader.isAlive()) {
 			mainPane.removeAllViews();
 			updateArticlesRunner = new UpdateArticlesRunner(articleDAO, imageDAO, networkStatusService);
 			updateArticlesRunner.setUncached(unCached);
 
 			updateArticlesHandler.init();
 			
 			loader = new Thread(updateArticlesRunner);
 			loader.start();
 		}
 	}
 	
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
 		registerReceiver(downloadProgressReceiver, new IntentFilter(HttpFetcher.DOWNLOAD_PROGRESS));
 	}
 
 		
 	@Override
 	protected void onPause() {
 		super.onPause();
 		updateArticlesRunner.stop();
 		unregisterReceiver(downloadProgressReceiver);
 		hideDownloadProgress();
 	}
 	
 	
 	protected boolean shouldRefreshView(LinearLayout mainPane) {
 		if (loaded == null || mainPane.getChildCount() == 0) {
 			return true;
 		}
 		Date modtime = ArticleDAOFactory.getDao(this.getApplicationContext()).getModificationTime(getArticleSet());
 		return modtime != null && modtime.after(loaded);
 	}
 	
 	
 	private ArticleBundle loadArticles(boolean uncached) {
 		ArticleSet articleSet = getArticleSet();
 		if (articleSet != null) {
 			return articleDAO.getArticleSetArticles(articleSet, uncached);
 		}
 		return null;
 	}
 	
 	protected abstract ArticleSet getArticleSet();
 	protected abstract String getRefinementDescription(String refinementType);	
 	
 	class UpdateArticlesHandler extends Handler {		
 
 		private Context context;
 		boolean first = true;
 		boolean isFirstOfSection;
 		Section currentSection;
 		private ArticleSet articleSet;
 		private boolean descriptionSet;
 		
 		public UpdateArticlesHandler(Context context, ArticleSet articleSet) {
 			super();
 			this.context = context;
 			this.articleSet = articleSet;
 			this.descriptionSet = false;
 			init();
 		}
 		
 		
 		public void init() {
 			first = true;
 			isFirstOfSection = true;
 			currentSection = null;
 		}
 		
 		public void handleMessage(Message msg) {
 			super.handleMessage(msg);
 			
 			switch (msg.what) {	   
 			    case 1: 		
 			    	Article article = (Article) msg.getData().getSerializable("article");				
 					LayoutInflater mInflater = LayoutInflater.from(context);
 					LinearLayout mainpane = (LinearLayout) findViewById(R.id.MainPane);					
 					if (showSeperators) {
 						if (currentSection == null || !currentSection.getId().equals(article.getSection().getId())) {
 							isFirstOfSection = true;
 						}
 
 						if (isFirstOfSection) {
 							addSeperator(mInflater, mainpane, article.getSection());
 							currentSection = article.getSection();
 							isFirstOfSection = false;
 						}
 					}
 					
 				boolean isContributorArticleSet = articleSet.getApiUrl().startsWith("profile");
 				boolean shouldUseFeatureTrail = showMainImage && first && !isContributorArticleSet && article.getMainImageUrl() != null && imageDAO.isAvailableLocally(article.getMainImageUrl());
 					View articleTrailView = chooseTrailView(mInflater, shouldUseFeatureTrail);
 					populateArticleListView(article, articleTrailView, shouldUseFeatureTrail);
 					mainpane.addView(articleTrailView);
 					first = false;
 					return;
 			    			    
 			    case 2: 
 			    	// TODO on screen test
 			    	return;
 			    			    
 			    case 3: 
 
 			    	Bundle data = msg.getData();
 			    	if (data.containsKey("id")) {
 			    		final String id = data.getString("id");
 			    		final String url = data.getString("url");
 			    		if( viewsWaitingForTrailImages.containsKey(id)) {
 			    			View view = viewsWaitingForTrailImages.get(id);
 			    			populateTrailImage(url, view);
 			    			viewsWaitingForTrailImages.remove(id);
 			    		}
 			    	}			    
 			    	return;
 			    	
 			    	
 			    case 6:
 			    	mainpane = (LinearLayout) findViewById(R.id.MainPane);
 			    	Bundle descriptionData = msg.getData();
 			    	String descripton = descriptionData.getString("description");
 			    	if (descripton != null && !descriptionSet) {
 			    		populateTagDescription(mainpane, descripton);
 			    	}
 			    	return;
 			    	
 			    case 4:			    	
 			    	mainpane = (LinearLayout) findViewById(R.id.MainPane);
 			    	Map<String, List<Tag>> refinements = bundle.getRefinements();
 			    	
 			    	if (refinements != null && !refinements.isEmpty()) {
 			    		LayoutInflater inflater = LayoutInflater.from(context);
 			    		
 			    		for (String refinementType : articleSet.getPermittedRefinements()) {
 			    			if (articleSet.getPermittedRefinements().contains(refinementType) && refinements.keySet().contains(refinementType)) {
 			    				String description = getRefinementDescription(refinementType);
 			    				populateRefinementType(mainpane, inflater, description, refinements.get(refinementType));
 			    			}
 						}
 			    		
 			    	}
 			    	return;
 			    				    	
 			    case 5: 
 					mainpane = (LinearLayout) findViewById(R.id.MainPane);
 					TextView message = new TextView(context);
 					msg.getData().getString("modtime");
 					message.setText("Updates to this article set are available (Refresh to view)");
 					mainpane.addView(message, 0);
 					return;
 			}
 		}
 
 
 		private void populateTagDescription(LinearLayout mainpane, String descripton) {
 			TextView descriptionView = new TextView(context);
 			descriptionView.setText(descripton);
 			descriptionView.setPadding(3, 3, 3, 15);
 			mainpane.addView(descriptionView, 0);
 			descriptionSet = true;
 		}
 
 		
 		private void populateRefinementType(LinearLayout mainpane, LayoutInflater inflater, String description, List<Tag> typedRefinements) {
 			View refinementsHeadingView = inflater.inflate(R.layout.refinements, null);
 			TextView descriptionView = (TextView) refinementsHeadingView.findViewById(R.id.RefinementsDescription);			    		
 			descriptionView.setText(description);
 			mainpane.addView(refinementsHeadingView);
 			TagListPopulatingService.populateTags(inflater, true, mainpane, typedRefinements, context);
 		}
 
 		private void populateTrailImage(final String url, View view) {
 			ImageView trialImage = (ImageView) view.findViewById(R.id.TrailImage);
 			Bitmap image = imageDAO.getImage(url);
 			trialImage.setImageBitmap(image);
 			trialImage.setVisibility(View.VISIBLE);
 		}
 		
 
 		private void addSeperator(LayoutInflater mInflater, LinearLayout mainpane, Section section) {
 			View seperator = mInflater.inflate(R.layout.seperator, null);
 			seperator.setBackgroundColor(Color.parseColor(SectionColourMap.getColourForSection(section.getId())));
 			TextView heading = (TextView) seperator.findViewById(R.id.TagName);
 			heading.setText(section.getName());
 			
 			TagListPopulatingService.populateSectionClicker(section, seperator, context);			
 			mainpane.addView(seperator);
 		}
 
 		private View chooseTrailView(LayoutInflater mInflater, boolean shouldUseFeatureTrail) {
 			View view;
 			if (shouldUseFeatureTrail) {
 				view = mInflater.inflate(R.layout.featurelist, null);
 			} else {				
 				view = mInflater.inflate(R.layout.list, null);
 			}
 			return view;
 		}
 
 		private void populateArticleListView(Article article, View view, boolean shouldUseFeatureTrail) {
 			TextView titleText = (TextView) view.findViewById(R.id.Headline);
 			titleText.setText(article.getTitle());
 			
 			TextView pubDateText = (TextView) view.findViewById(R.id.Pubdate);
 			if (article.getPubDate() != null) {
 				pubDateText.setText(article.getPubDateString());
 			}
 			
 			TextView standfirst = (TextView) view.findViewById(R.id.Standfirst);
 			if (article.getStandfirst() != null) {
 				standfirst.setText(article.getStandfirst());
 			}
 						
 			TextView caption = (TextView) view.findViewById(R.id.Caption);
 			if (caption != null && article.getCaption() != null) {
 				caption.setText(article.getCaption());
 				caption.setVisibility(View.VISIBLE);
 			}
 			
 			String trailImageUrl = article.getThumbnailUrl();
 			if (shouldUseFeatureTrail) {
 				trailImageUrl = article.getMainImageUrl();
 			}
 			
 			if (trailImageUrl != null) {
 				if (imageDAO.isAvailableLocally(trailImageUrl)) {
 					populateTrailImage(trailImageUrl, view);
 				} else {
 					viewsWaitingForTrailImages.put(article.getId(), view);
 				}
 			}
 			
 			ArticleClicker urlListener = new ArticleClicker(article);
 			view.setOnClickListener(urlListener);
 		}
 		
 	}
 	
 	
 	private void sendArticleReadyMessage(Article article) {
 		Message m = new Message();			
 		m.what = 1;
 		Bundle bundle = new Bundle();
 		bundle.putSerializable("article", article);			
 		m.setData(bundle);		
 		updateArticlesHandler.sendMessage(m);
 	}
 	
 	
 	
 	private void sendDescriptionReadyMessage(String description) {
 		Message m = new Message();			
 		m.what = 6;
 		Bundle bundle = new Bundle();
 		bundle.putString("description", description);			
 		m.setData(bundle);		
 		updateArticlesHandler.sendMessage(m);
 	}
 	
 	
 	class UpdateArticlesRunner implements Runnable, ArticleCallback {		
 		boolean running;
 		ArticleDAO articleDAO;
 		ImageDAO imageDAO;
 		NetworkStatusService networkStatusService;
 		boolean uncached;
 		
 		public UpdateArticlesRunner(ArticleDAO articleDAO, ImageDAO imageDAO, NetworkStatusService networkStatusService) {
 			this.articleDAO = articleDAO;
 			this.imageDAO = imageDAO;
 			this.running = true;
 			this.networkStatusService = networkStatusService;
 			articleDAO.setArticleReadyCallback(this);
 			this.uncached = false;
 		}
 		
 		public void setUncached(boolean uncached) {
 			this.uncached = uncached;		
 		}
 
 		public void run() {
 
 			if (running) {
 				bundle = loadArticles(uncached);
 			}
 			
 			if (bundle == null) {
 				Message m = new Message();
 				m.what = 2;
 				updateArticlesHandler.sendMessage(m);
 				return;
 			}
 			
 			if (bundle.getDescription() != null) {
 				sendDescriptionReadyMessage(bundle.getDescription());
 			}
 			
 			Message m = new Message();
 			m.what = 4;
 			updateArticlesHandler.sendMessage(m);
 			
 			
 			List<Article> downloadTrailImages = new LinkedList<Article>();
 			boolean first = true;
 			for (Article article : bundle.getArticles()) {
 				
 				String imageUrl;
 				boolean mainImageIsAvailableLocally = article.getMainImageUrl() != null && imageDAO.isAvailableLocally(article.getMainImageUrl());
 				if (first && mainImageIsAvailableLocally) {						
 						imageUrl = article.getMainImageUrl();
 				} else {
 					imageUrl = article.getThumbnailUrl();
 				}
 				
 				if (imageUrl != null) {
 					if (imageDAO.isAvailableLocally(imageUrl)) {
 						m = new Message();
 						m.what = 3;						
 						Bundle bundle = new Bundle();
 						bundle.putString("id", article.getId());
 						bundle.putString("url", imageUrl);
 						
 						m.setData(bundle);
 						updateArticlesHandler.sendMessage(m);
 						
 					} else {
 						downloadTrailImages.add(article);
 					}					
 				}
 				
 				first = false;
 			}
 			
 			
 			if (running) {
 				for (Article article : downloadTrailImages) {
 					imageDAO.fetchLiveImage(article.getThumbnailUrl());
 					m = new Message();
 					m.what = 3;
 					Bundle bundle = new Bundle();
 					bundle.putString("id", article.getId());
 					bundle.putString("url", article.getThumbnailUrl());
 					
 					m.setData(bundle);
 					updateArticlesHandler.sendMessage(m);
 				}		
 			}
 			
 			//if (bundle != null) {
 				//Date modificationTime = bundle.getTimestamp();
 				//Log.i(TAG, "Article bundle timestamp is: " + bundle.getTimestamp() + " / " + modificationTime);			
 				
 				/*
 				if (modificationTime != null) {
 					
 					if (networkStatusService.isConnectionAvailable() && modificationTime.isBefore(new DateTime().minusMinutes(10))) {
 						Log.i(TAG, "Checking remote checksum local copy is older than 10 minutes and network is available");
 					
 						String localChecksum = bundle.getChecksum();
 						String remoteChecksum = articleDAO.getArticleSetRemoteChecksum(getArticleSet());
 						if (remoteChecksum != null) {
 							if (localChecksum != null && !localChecksum.equals(remoteChecksum)) {
 								Log.i(TAG, "Remote content checksum is different: " + localChecksum + ":" + remoteChecksum);
 								m = new Message();
 								m.what = 5;
 								Bundle bundle = new Bundle();
 								bundle.putString("modtime", modificationTime.toString());
 								bundle.putString("localChecksum", localChecksum);
 								bundle.putString("remoteChecksum", remoteChecksum);
 								m.setData(bundle);
 								updateArticlesHandler.sendMessage(m);
 							
 							} else {
 								Log.i(TAG, "No remote content change detected: " + localChecksum + ":" + remoteChecksum);
 								articleDAO.touchFile(getArticleSet());
 							}
 							
 						} else {
 							Log.e(TAG, "Remote checksum was null");							
 						}
 					}
 					
 				}
 				*/
 				
 			//}
 			
 			loaded = DateTimeHelper.now();
 			return;				
 		}
 
 		public void stop() {
 			this.running = false;
 			articleDAO.stopLoading();
 		}
 
 		@Override
 		public void articleReady(Article article) {
 			sendArticleReadyMessage(article);
 		}
 
 		@Override
 		public void descriptionReady(String description) {
 			sendDescriptionReadyMessage(description);			
 		}
 			
 	}
 	
 }
