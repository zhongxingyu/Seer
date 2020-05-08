 package com.cee.news.client;
 
 import com.cee.news.client.async.NotificationCallback;
 import com.cee.news.client.content.ArticleUtil;
 import com.cee.news.client.content.NewSiteWizard;
 import com.cee.news.client.content.NewSiteWizardView;
 import com.cee.news.client.content.NewsListContentModel;
 import com.cee.news.client.content.SiteListContentModel;
 import com.cee.news.client.content.SiteUpdateService;
 import com.cee.news.client.content.SiteUpdateServiceAsync;
 import com.cee.news.client.error.ErrorDialog;
 import com.cee.news.client.error.ErrorEvent;
 import com.cee.news.client.error.ErrorHandler;
 import com.cee.news.client.list.ListPresenter;
 import com.cee.news.client.list.SelectionChangedEvent;
 import com.cee.news.client.list.SelectionChangedHandler;
 import com.cee.news.client.paging.PagingPresenter;
 import com.cee.news.client.progress.ProgressModel;
 import com.cee.news.client.progress.ProgressPresenter;
 import com.cee.news.client.workingset.WorkingSetEditor;
 import com.cee.news.client.workingset.WorkingSetListModel;
 import com.cee.news.client.workingset.WorkingSetSelectionPresenter;
 import com.cee.news.client.workingset.WorkingSetSelectionView;
 import com.cee.news.client.workingset.WorkingSetWorkflow;
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.shared.SimpleEventBus;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.DeckPanel;
 import com.google.gwt.user.client.ui.LayoutPanel;
 import com.google.gwt.user.client.ui.RootPanel;
 
 /**
  * Entry point classes define <code>onModuleLoad()</code>.
  */
 public class NewsReader implements EntryPoint {
 	
 	private static final int NEWS_PANEL_INDEX = 1;
 
 	private static final int START_PANEL_INDEX = 0;
 
 	/**
 	 * Application event bus. The following events are routed:
 	 * 
 	 * {@link ErrorEvent} Event raised for all unknown exceptions
 	 * {@link SelectionChangedEvent} Event raised if the current working set is changed
 	 */
 	private final SimpleEventBus appEventBus = new SimpleEventBus();
 	
 	private final ErrorHandler globalErrorHandler = new ErrorHandler() {
 		@Override
 		public void onError(ErrorEvent event) {
 			appEventBus.fireEvent(event);
 		}
 	};
 	
 	private String workingSet;
 	
 	public void onModuleLoad() {
 		RootPanel rootPanel = RootPanel.get();
 		
 		ErrorDialog errorDialog = new ErrorDialog();
 		appEventBus.addHandler(ErrorEvent.TYPE, errorDialog);
 		
 		LayoutPanel layoutPanel = new LayoutPanel();
 		rootPanel.add(layoutPanel, 0, 0);
 		layoutPanel.setSize("813px", "600px");
 		
 		final DeckPanel deckPanel = new DeckPanel();
 		layoutPanel.add(deckPanel);
 		layoutPanel.setWidgetLeftRight(deckPanel, 0.0, Unit.PX, 0.0, Unit.PX);
 		layoutPanel.setWidgetTopBottom(deckPanel, 0.0, Unit.PX, 0.0, Unit.PX);
 		
 		StartPanel startPanel = new StartPanel();
 		deckPanel.add(startPanel);
 		startPanel.setSize("100%", "100%");
 		
 		final NewsPanel newsPanel = new NewsPanel();
 		deckPanel.add(newsPanel);
 		newsPanel.setSize("100%", "100%");
 		deckPanel.showWidget(START_PANEL_INDEX);
 		
 		//Working Set Selection
 		final WorkingSetListModel workingSetListModel = new WorkingSetListModel();
 		workingSetListModel.addErrorHandler(globalErrorHandler);
 		workingSetListModel.addSelectionChangedhandler(new SelectionChangedHandler() {
 			@Override
 			public void onSelectionChange(SelectionChangedEvent event) {
 				workingSet = event.getKey();
 				appEventBus.fireEvent(event);
 			}
 		});
 		final WorkingSetSelectionView workingSetSelectionView = startPanel.getWorkingSetSelectionView();
 		
 		//Site Update Service
 		final SiteUpdateServiceAsync siteUpdateService = SiteUpdateService.Util.getInstance();
 		final ProgressModel progressModel = new ProgressModel();
 		new ProgressPresenter(progressModel, startPanel.getProgressView());
 		appEventBus.addHandler(SelectionChangedEvent.TYPE, new SelectionChangedHandler() {
 			@Override
 			public void onSelectionChange(SelectionChangedEvent event) {
 				siteUpdateService.addSitesOfWorkingSetToUpdateQueue(event.getKey(), new AsyncCallback<Integer>() {
 					@Override
 					public void onSuccess(Integer result) {
 						progressModel.startMonitor();
 					}
 					@Override
 					public void onFailure(Throwable caught) {
 						appEventBus.fireEvent(new ErrorEvent(caught, "Could not queue commands for working set update"));
 					}
 				});
 			}
 		});
 		
 		//Latest Article List
 		final NewsListContentModel latestArticlesOfWorkingSet = new NewsListContentModel();
 		new ListPresenter(latestArticlesOfWorkingSet, latestArticlesOfWorkingSet, startPanel.getLatestArticlesListView());
 		appEventBus.addHandler(SelectionChangedEvent.TYPE, new SelectionChangedHandler() {
 			@Override
 			public void onSelectionChange(SelectionChangedEvent event) {
 				latestArticlesOfWorkingSet.updateFromWorkingSet(event.getKey());
 			}
 		});
 		final ProgressUpdateHandler progressUpdateHandler = new ProgressUpdateHandler(latestArticlesOfWorkingSet);
 		appEventBus.addHandler(SelectionChangedEvent.TYPE, progressUpdateHandler);
 		progressModel.addProgressHandler(progressUpdateHandler);
 		
 		//Site List
 		final SiteListContentModel sitesOfWorkingSetModel = new SiteListContentModel();
 		sitesOfWorkingSetModel.addErrorHandler(globalErrorHandler);
 		new ListPresenter(sitesOfWorkingSetModel, sitesOfWorkingSetModel, startPanel.getSitesListView());
 		appEventBus.addHandler(SelectionChangedEvent.TYPE, new SelectionChangedHandler() {
 			@Override
 			public void onSelectionChange(SelectionChangedEvent event) {
 				sitesOfWorkingSetModel.update(null, event.getKey());
 			}
 		});
 
 		//New & Edit Working Set Workflow
 		final NewSiteWizardView newSiteWizard = new NewSiteWizard();
 		final SiteListContentModel siteAddRemoveListModel = new SiteListContentModel();
 		siteAddRemoveListModel.addErrorHandler(globalErrorHandler);
 		final WorkingSetEditor workingSetEditor = new WorkingSetEditor(siteAddRemoveListModel, siteAddRemoveListModel);
 		final WorkingSetWorkflow workingSetWorkflow = new WorkingSetWorkflow(workingSetListModel, siteAddRemoveListModel, workingSetEditor, newSiteWizard);
 		workingSetWorkflow.addErrorHandler(globalErrorHandler);
 		new WorkingSetSelectionPresenter(workingSetListModel, workingSetSelectionView);
 		workingSetSelectionView.getNewButton().addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				workingSetWorkflow.newWorkingSet();
 			}
 		});
 		workingSetSelectionView.getEditButton().addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				workingSetWorkflow.editWorkingSet(workingSetListModel.getSelectedKey());
 			}
 		});
 		
 		//News Paging View
 		final NewsListContentModel pagingNewsList = new NewsListContentModel();
 		new PagingPresenter(pagingNewsList, pagingNewsList, newsPanel.getPagingView());
 		sitesOfWorkingSetModel.addSelectionChangedhandler(new SelectionChangedHandler() {
 			@Override
 			public void onSelectionChange(SelectionChangedEvent event) {
 				if (event.isUserAction()) {
 					pagingNewsList.updateFromSite(event.getKey(), new NotificationCallback() {
 
 						@Override
 						public void finished() {
 							deckPanel.showWidget(NEWS_PANEL_INDEX);
 						}
 					});
 				}
 			}
 		});
 		latestArticlesOfWorkingSet.addSelectionChangedhandler(new SelectionChangedHandler() {
 			@Override
 			public void onSelectionChange(SelectionChangedEvent event) {
 				if (event.isUserAction()) {
 					final String articleKey = event.getKey();
 					final String siteKey = ArticleUtil.getSiteKeyFromArticleKey(articleKey);
 					newsPanel.getSiteNameLabel().setText(siteKey);
 					pagingNewsList.updateFromSite(siteKey, new NotificationCallback() {
 						
 						@Override
 						public void finished() {
 							pagingNewsList.setSelectedKey(articleKey);
 							deckPanel.showWidget(NEWS_PANEL_INDEX);
 						}
 					});
 				}
 			}
 		});
 		newsPanel.getButtonGoToStart().addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				deckPanel.showWidget(START_PANEL_INDEX);
 			}
 		});
 		
 		//What others say view
 		final NewsListContentModel whatOthersSay = new NewsListContentModel();
 		new ListPresenter(whatOthersSay, whatOthersSay, newsPanel.getWhatOthersSayListView());
 		pagingNewsList.addSelectionChangedhandler(new SelectionChangedHandler() {
 			
 			@Override
 			public void onSelectionChange(SelectionChangedEvent event) {
 				whatOthersSay.updateFromArticle(event.getKey(), workingSet);
 			}
 		});
 		whatOthersSay.addSelectionChangedhandler(new SelectionChangedHandler() {
 			
 			@Override
 			public void onSelectionChange(SelectionChangedEvent event) {
 				if (event.isUserAction()) {
 					final String articleKey = event.getKey();
 					final String siteKey = ArticleUtil.getSiteKeyFromArticleKey(articleKey);
 					newsPanel.getSiteNameLabel().setText(siteKey);
 					pagingNewsList.updateFromSite(siteKey, new NotificationCallback() {
 						
 						@Override
 						public void finished() {
 							pagingNewsList.setSelectedKey(articleKey);
 						}
 					});
 				}
 			}
 		});
 		
 		//trigger update
 		workingSetListModel.update(null);
 		siteAddRemoveListModel.update(null);
 		
 		//start the background scheduler
		siteUpdateService.startUpdateScheduler(null);
 	}
 }
