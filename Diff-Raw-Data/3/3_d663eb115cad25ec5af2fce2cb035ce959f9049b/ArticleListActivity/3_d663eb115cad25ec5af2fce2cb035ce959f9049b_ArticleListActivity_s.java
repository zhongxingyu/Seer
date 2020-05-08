 package nz.gen.wellington.guardian.android.activities;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import nz.gen.wellington.guardian.android.R;
 import nz.gen.wellington.guardian.android.activities.ui.ArticleClicker;
 import nz.gen.wellington.guardian.android.activities.ui.ClickerPopulatingService;
 import nz.gen.wellington.guardian.android.activities.ui.TagListPopulatingService;
 import nz.gen.wellington.guardian.android.api.ArticleDAO;
 import nz.gen.wellington.guardian.android.api.ContentFetchType;
 import nz.gen.wellington.guardian.android.api.ImageDAO;
 import nz.gen.wellington.guardian.android.factories.ArticleSetFactory;
 import nz.gen.wellington.guardian.android.factories.SingletonFactory;
 import nz.gen.wellington.guardian.android.model.Article;
 import nz.gen.wellington.guardian.android.model.ArticleBundle;
 import nz.gen.wellington.guardian.android.model.ArticleSet;
 import nz.gen.wellington.guardian.android.model.ColourScheme;
 import nz.gen.wellington.guardian.android.model.Section;
 import nz.gen.wellington.guardian.android.model.SectionColourMap;
 import nz.gen.wellington.guardian.android.network.NetworkStatusService;
 import nz.gen.wellington.guardian.android.usersettings.PreferencesDAO;
 import nz.gen.wellington.guardian.android.utils.DateTimeHelper;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.util.TypedValue;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public abstract class ArticleListActivity extends DownloadProgressAwareActivity implements FontResizingActivity {
 	
 	private static final String TAG = "ArticleListActivity";
 	
 	protected ArticleDAO articleDAO;
 	protected ImageDAO imageDAO;
 	private NetworkStatusService networkStatusService;
 	private PreferencesDAO preferencesDAO;
 	
 	private UpdateArticlesHandler updateArticlesHandler;
 	private UpdateArticlesRunner updateArticlesRunner;
 	
 	private ArticleBundle bundle;
 	private Map<String, View> viewsWaitingForTrailImages;
 
 	boolean showSeperators = false;
 	boolean showMainImage = true;
 		
 	private Thread loader;
 	private Date loaded;
 	private int baseSize;
 	
 	protected String[] permittedRefinements = {"keyword"};
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		viewsWaitingForTrailImages = new HashMap<String, View>();
 		articleDAO = SingletonFactory.getDao(this.getApplicationContext());
 		imageDAO = SingletonFactory.getImageDao(this.getApplicationContext());		
 		networkStatusService = new NetworkStatusService(this);
 		preferencesDAO = SingletonFactory.getPreferencesDAO(this.getApplicationContext());
 	}
 	
 	
 	@Override
 	protected void onStart() {
 		super.onStart();	
 		LinearLayout mainPane = (LinearLayout) findViewById(R.id.MainPane);		
 		boolean mainPaneNeedsPopulating = shouldRefreshView(mainPane);
 		if (mainPaneNeedsPopulating) {
 			populateArticles(ContentFetchType.NORMAL, preferencesDAO.getBaseFontSize());
 		}
 	}
 	
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
 		baseSize = preferencesDAO.getBaseFontSize();
 		setFontSize(baseSize);
 		
 		View view =  findViewById(R.id.Main);
 		if (view != null) {
 			view.setBackgroundColor(ColourScheme.BACKGROUND);
 		}
 	}
 
 	
 	@Override
 	public void setFontSize(int baseSize) {
 		TextView description = (TextView) findViewById(R.id.Description);
 		if (description != null) {
 			description.setTextSize(TypedValue.COMPLEX_UNIT_PT, baseSize);
 			description.setTextColor(ColourScheme.BODYTEXT);
 		}
 	}
 
 
 	protected void refresh() {
 		populateArticles(ContentFetchType.CHECKSUM, preferencesDAO.getBaseFontSize());
 	}
 	
 	
 	protected int getPageSize() {
 		return preferencesDAO.getPageSizePreference();
 	}
 	
 	
 	private void populateArticles(ContentFetchType fetchType, int baseFontSize) {
 		Log.i(TAG, "Refresh requested");
 	
 		if (!networkStatusService.isConnectionAvailable() && ContentFetchType.CHECKSUM.equals(fetchType)) {	// TODO knowledge of connections requirements should be on the fetch type.
 			Log.i(TAG, "Not refreshing uncached as no connection is available");
 			return;
 		}
 		
 		LinearLayout mainPane = (LinearLayout) findViewById(R.id.MainPane);
 		if (loader == null || !loader.isAlive()) {
 			mainPane.removeAllViews();
 			
 			final ArticleSet articleSet = getArticleSet();
 			updateArticlesHandler = new UpdateArticlesHandler(this, articleSet, baseFontSize);
 			updateArticlesRunner = new UpdateArticlesRunner(articleDAO, imageDAO, networkStatusService, fetchType, articleSet);
 			updateArticlesHandler.init();
 			
 			loader = new Thread(updateArticlesRunner);
 			loader.start();
 		}
 	}
 	
 	
 	@Override
 	protected void onPause() {
 		super.onPause();
 		if (updateArticlesHandler != null) {
 			updateArticlesRunner.stop();
 		}
 	}
 	
 	
 	private boolean shouldRefreshView(LinearLayout mainPane) {
 		if (loaded == null || mainPane.getChildCount() == 0) {
 			return true;
 		}
 		Date modtime = SingletonFactory.getDao(this.getApplicationContext()).getModificationTime(getArticleSet());
 		return modtime != null && modtime.after(loaded);
 	}
 	
 	
 	private ArticleBundle loadArticles(ContentFetchType fetchType, ArticleSet articleSet) {
 		if (articleSet != null) {
 			return articleDAO.getArticleSetArticles(articleSet, fetchType);
 		}
 		return null;
 	}
 	
 	
 	protected abstract ArticleSet getArticleSet();
 	protected abstract String getRefinementDescription(String refinementType);	
 
 	
 	
 	class UpdateArticlesHandler extends Handler {		
 
 		private static final int ARTICLE_READY = 1;
 		private static final int TRAIL_IMAGE_IS_AVAILABLE_FOR_ARTICLE = 3;
 		private static final int DESCRIPTION_TEXT_READY = 6;
 		private static final int DRAW_REFINEMENTS = 4;
 		private static final int SHOW_ARTICLE_SET_OUT_OF_DATE_WARNING = 5;
 		public static final int NO_ARTICLES = 2;
 
 		
 		private Context context;
 		boolean first = true;
 		boolean isFirstOfSection;
 		Section currentSection;
 		private ArticleSet articleSet;
 		private boolean descriptionSet;
 		private int baseFontSize;
 		private ArticleSetFactory articleSetFactory;
 		
 		public UpdateArticlesHandler(Context context, ArticleSet articleSet, int baseFontSize) {
 			super();
 			this.articleSetFactory = SingletonFactory.getArticleSetFactory(context);
 			this.context = context;
 			this.articleSet = articleSet;
 			this.descriptionSet = false;
 			this.baseFontSize = baseFontSize;
 			init();
 		}
 				
 		public void init() {
 			first = true;
 			isFirstOfSection = false;
 			currentSection = null;
 			descriptionSet = false;
 		}
 		
 		public void handleMessage(Message msg) {
 			super.handleMessage(msg);
 			
 			switch (msg.what) {
 				
 			    case ARTICLE_READY: 		
 			    	Article article = (Article) msg.getData().getSerializable("article");				
 					LayoutInflater mInflater = LayoutInflater.from(context);
 					LinearLayout mainpane = (LinearLayout) findViewById(R.id.MainPane);					
 					if (showSeperators) {
 						
 						if (article.getSection() != null) {
 							if (currentSection == null || !currentSection.getId().equals(article.getSection().getId())) {
 								isFirstOfSection = true;
 							}
 						}
 
 						if (isFirstOfSection) {
 							addSeperator(mInflater, mainpane, article.getSection());
 							currentSection = article.getSection();
 							isFirstOfSection = false;
 						}
 					}
 					
 					// TODO should base this decision on the articles set's tags
 					boolean isContributorArticleSet = false; // TODO articleSet.getApiUrl().startsWith("profile");
 					boolean shouldUseFeatureTrail = showMainImage && first && !isContributorArticleSet && article.getMainImageUrl() != null && imageDAO.isAvailableLocally(article.getMainImageUrl());
 					View articleTrailView = chooseTrailView(mInflater, shouldUseFeatureTrail);
 					populateArticleListView(article, articleTrailView, shouldUseFeatureTrail);
 					mainpane.addView(articleTrailView);
 					first = false;
 					return;
 					
 					
 			    case TRAIL_IMAGE_IS_AVAILABLE_FOR_ARTICLE:
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
 			    	
 			    				    
 			    case DESCRIPTION_TEXT_READY:
 			    	mainpane = (LinearLayout) findViewById(R.id.MainPane);
 			    	Bundle descriptionData = msg.getData();
 			    	String descripton = descriptionData.getString("description");
 			    	if (descripton != null && !descriptionSet) {			    	
 			    		populateTagDescription(mainpane, descripton, baseFontSize);
 			    	}
 			    	return;
 			    	
 			    	
 			    case DRAW_REFINEMENTS:			    	
 			    	mainpane = (LinearLayout) findViewById(R.id.MainPane);
 			    	Map<String, List<ArticleSet>> refinements = bundle.getRefinements();
 			    	
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
 			    			
 			    	
 			    case SHOW_ARTICLE_SET_OUT_OF_DATE_WARNING: 
 					mainpane = (LinearLayout) findViewById(R.id.MainPane);
 					TextView message = new TextView(context);					
 					if (networkStatusService.isConnectionAvailable()) {
 						message.setText("This article set was last downloaded more than 2 hours ago. Refresh to check for updates.");
 					} else {
 						message.setText("This article set was last downloaded more than 2 hours ago and may be out of date.");
 					}
 					
 					message.setTextColor(ColourScheme.STATUS);
 					message.setPadding(2, 3, 2, 3);
 					mainpane.addView(message, 0);
 					return;
 					
 			    case NO_ARTICLES:
 			    	Log.i(TAG, "Displaying no articles available message");			    	
 			    	mainpane = (LinearLayout) findViewById(R.id.MainPane);
 					TextView noArticlesMessage = new TextView(context);
					noArticlesMessage.setTextSize(baseFontSize + 10, TypedValue.COMPLEX_UNIT_PT);
 					noArticlesMessage.setText("No articles available.");
 					
 					noArticlesMessage.setTextColor(ColourScheme.HEADLINE);
 					noArticlesMessage.setPadding(2, 3, 2, 3);					
 					mainpane.addView(noArticlesMessage, 0);
 			    	return;
 			}
 		}
 
 
 		private void populateTagDescription(LinearLayout mainpane, String descripton, int fontSize) {
 			// TODO move to the layout file
 			TextView descriptionView = new TextView(context);
 			descriptionView.setId(R.id.Description);
 			descriptionView.setText(descripton);
 			descriptionView.setPadding(2, 3, 2, 15);
 			mainpane.addView(descriptionView, 0);
 			descriptionView.setTextSize(TypedValue.COMPLEX_UNIT_PT, fontSize);	// TODO duplicated setting code
 			descriptionView.setLineSpacing(new Float(0), new Float(1.1));
 			
 			descriptionView.setTextColor(ColourScheme.BODYTEXT);
 			descriptionView.setPadding(2, 3, 2, 3);	
 			descriptionSet = true;
 		}
 
 		
 		private void populateRefinementType(LinearLayout mainpane, LayoutInflater inflater, String description, List<ArticleSet> typedRefinements) {
 			View refinementsHeadingView = inflater.inflate(R.layout.refinements, null);			
 			TextView descriptionView = (TextView) refinementsHeadingView.findViewById(R.id.RefinementsDescription);
 			descriptionView.setText(description);
 			descriptionView.setTextColor(ColourScheme.BODYTEXT);
 			descriptionView.setPadding(2, 3, 2, 3);
 			mainpane.addView(refinementsHeadingView);
 			
 			// TODO move to a layout
 			LinearLayout tagGroup = new LinearLayout(context);
 			tagGroup.setOrientation(LinearLayout.VERTICAL);
 			tagGroup.setPadding(2, 0, 2, 0);
 			
 			TagListPopulatingService.populateTags(inflater, true, tagGroup, typedRefinements, context);
 			mainpane.addView(tagGroup);
 		}
 
 		private void populateTrailImage(final String url, View view) {
 			ImageView trialImage = (ImageView) view.findViewById(R.id.TrailImage);
 			Bitmap image = imageDAO.getImage(url);
 			trialImage.setImageBitmap(image);
 			trialImage.setVisibility(View.VISIBLE);
 		}
 		
 
 		private void addSeperator(LayoutInflater mInflater, LinearLayout mainpane, Section section) {
 			View seperator = mInflater.inflate(R.layout.seperator, null);
 			
 			final String colourForSection = SectionColourMap.getColourForSection(section.getId());
 			if (colourForSection != null) {
 				seperator.setBackgroundColor(Color.parseColor(colourForSection));
 			
 				TextView heading = (TextView) seperator.findViewById(R.id.TagName);
 				heading.setText(section.getName());
 	
 				ArticleSet articleSetForSection = articleSetFactory.getArticleSetForSection(section);
 				boolean contentIsAvailable = articleDAO.isAvailable(articleSetForSection);	    	
 				ClickerPopulatingService.populateClicker(articleSetForSection, seperator, contentIsAvailable);
 				mainpane.addView(seperator);
 				
 			} else {
 				Log.w(TAG, "Could not find section colour for section: " + section.getId());
 			}
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
 			TextView pubDateText = (TextView) view.findViewById(R.id.Pubdate);
 			TextView standfirst = (TextView) view.findViewById(R.id.Standfirst);
 			TextView caption = (TextView) view.findViewById(R.id.Caption);
 			
 			titleText.setTextColor(ColourScheme.HEADLINE);
 			pubDateText.setTextColor(ColourScheme.BODYTEXT);			
 			standfirst.setTextColor(ColourScheme.BODYTEXT);
 			
 			titleText.setTextSize(TypedValue.COMPLEX_UNIT_PT, baseSize);
 			pubDateText.setTextSize(TypedValue.COMPLEX_UNIT_PT, baseSize -2);
 			standfirst.setTextSize(TypedValue.COMPLEX_UNIT_PT, new Float(baseSize - 0.75));
 
 			if (caption != null) {
 				caption.setTextColor(ColourScheme.BODYTEXT);
 			}
 			titleText.setText(article.getTitle());			
 			if (article.getPubDate() != null) {
 				pubDateText.setText(article.getPubDateString());
 			}
 			
 			if (article.getStandfirst() != null) {
 				standfirst.setText(article.getStandfirst());
 			}
 						
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
 		m.what = UpdateArticlesHandler.ARTICLE_READY;
 		Bundle bundle = new Bundle();
 		bundle.putSerializable("article", article);			
 		m.setData(bundle);		
 		updateArticlesHandler.sendMessage(m);
 	}
 	
 	
 	
 	private void sendDescriptionReadyMessage(String description) {
 		Message m = new Message();			
 		m.what = UpdateArticlesHandler.DESCRIPTION_TEXT_READY;
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
 		ContentFetchType fetchType;
 		private ArticleSet articleSet;
 		
 		public UpdateArticlesRunner(ArticleDAO articleDAO, ImageDAO imageDAO, NetworkStatusService networkStatusService, ContentFetchType fetchType, ArticleSet articleSet) {
 			this.articleDAO = articleDAO;
 			this.imageDAO = imageDAO;
 			this.running = true;
 			this.networkStatusService = networkStatusService;
 			articleDAO.setArticleReadyCallback(this);
 			this.fetchType = fetchType;
 			this.articleSet = articleSet;
 		}
 		
 		public void run() {
 
 			if (running) {
 				bundle = loadArticles(fetchType, articleSet);
 			}
 			
 			if (bundle == null) {
 				Log.i(TAG, "Article bundle was null");
 				Message m = new Message();
 				m.what = UpdateArticlesHandler.NO_ARTICLES;
 				updateArticlesHandler.sendMessage(m);
 				return;
 			}
 			
 			if (bundle.getDescription() != null) {
 				sendDescriptionReadyMessage(bundle.getDescription());
 			}
 			
 			Message m = new Message();
 			m.what = UpdateArticlesHandler.DRAW_REFINEMENTS;
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
 						m.what = UpdateArticlesHandler.TRAIL_IMAGE_IS_AVAILABLE_FOR_ARTICLE;						
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
 					imageDAO.getImage(article.getThumbnailUrl());
 					m = new Message();
 					m.what = UpdateArticlesHandler.TRAIL_IMAGE_IS_AVAILABLE_FOR_ARTICLE;
 					Bundle bundle = new Bundle();
 					bundle.putString("id", article.getId());
 					bundle.putString("url", article.getThumbnailUrl());
 					
 					m.setData(bundle);
 					updateArticlesHandler.sendMessage(m);
 				}		
 			}
 			
 			if (bundle != null) {
 				Date modificationTime = articleDAO.getModificationTime(articleSet);
 				if (modificationTime != null && DateTimeHelper.isMoreThanHoursOld(modificationTime, 2)) {
 					m = new Message();
 					m.what = UpdateArticlesHandler.SHOW_ARTICLE_SET_OUT_OF_DATE_WARNING;
 					Bundle bundle = new Bundle();
 					bundle.putString("modtime", modificationTime.toString());					
 					m.setData(bundle);
 					updateArticlesHandler.sendMessage(m);
 				}
 				
 			}
 			
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
