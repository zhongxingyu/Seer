 package com.szas.server.gwt.client.widgets;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.safehtml.shared.SafeHtmlUtils;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.Window.ClosingEvent;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.SimplePanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.szas.data.FilledQuestionnaireTuple;
 import com.szas.data.QuestionnaireTuple;
 import com.szas.server.gwt.client.router.LongRouteAction;
 import com.szas.server.gwt.client.router.RouteAction;
 import com.szas.server.gwt.client.router.Router;
 import com.szas.server.gwt.client.router.RouterImpl;
 import com.szas.server.gwt.client.sync.AutoSyncer;
 import com.szas.server.gwt.client.sync.StaticGWTSyncer;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.HTML;
 
 public class MainWidget extends Composite {
 
 	private static MainWidgetUiBinder uiBinder = GWT
 	.create(MainWidgetUiBinder.class);
 
 	@UiField SimplePanel simplePanel;
 	@UiField Button refreshButton;
 	@UiField Label syncStatusLabel;
 	@UiField(provided=true) HTML loginField = 
 		new HTML("Logged as: " +
 				SafeHtmlUtils.htmlEscape(StaticGWTSyncer.getEmail()) +
 				", <a href=\"" +
 				StaticGWTSyncer.getLogoutUrl()
 				+ "\">sign out</a>"
 				);
 	private Router<Widget> router = new RouterImpl<Widget>();
 
 	private AutoSyncer.AutoSyncerObserver autoSyncerObserver;
 
 	
 
 	private Widget widget;
 
 	interface MainWidgetUiBinder extends UiBinder<Widget, MainWidget> {
 	}
 	
 	private void addRoutes() {
 		RouteAction<Widget> questionnaireRouteAction = new RouteAction<Widget>() {
 			@Override
 			public Widget run(String command, String params) {
 				return new QuestionnariesList();
 			}
 		};
 		router.addRoute("", questionnaireRouteAction);
 		router.addRoute(QuestionnariesList.NAME, questionnaireRouteAction);
 		router.addRoute(QuestinnairesWidget.NAME, new LongRouteAction<Widget>() {
 
 			@Override
 			protected Widget run(String command, long param) {
 				QuestionnaireTuple questionnaireTuple =
 					StaticGWTSyncer.getQuestionnairedao().getById(param);
 				if (questionnaireTuple == null)
 					return null;
 				return new QuestinnairesWidget(questionnaireTuple);
 			}
 		});
 		router.addRoute(EditQuesionnaireWidget.NAME, new LongRouteAction<Widget>() {
 
 			@Override
 			protected Widget run(String command, long param) {
 				QuestionnaireTuple questionnaireTuple =
 					StaticGWTSyncer.getQuestionnairedao().getById(param);
 				if (questionnaireTuple == null)
 					return null;
 				return new EditQuesionnaireWidget(questionnaireTuple);
 			}
 		});
 		router.addRoute(EditQuesionnaireWidget.NAME, new RouteAction<Widget>() {
 
 			@Override
 			public Widget run(String command, String params) {
 				QuestionnaireTuple questionnaireTuple =
 					new QuestionnaireTuple();
 				return new EditQuesionnaireWidget(questionnaireTuple);
 			}
 		});
 		router.addRoute(EditFilledQuestionnaireWidget.NAME, new LongRouteAction<Widget>() {
 
 			@Override
 			protected Widget run(String command, long param) {
 				FilledQuestionnaireTuple filledQuestionnaireTuple =
 					StaticGWTSyncer.getFilledquestionnairedao().getById(param);
 				if (filledQuestionnaireTuple == null)
 					return null;
 				return new EditFilledQuestionnaireWidget(filledQuestionnaireTuple,false);
 			}
 		});
 		router.addRoute(EditFilledQuestionnaireWidget.NAME_EDIT, new LongRouteAction<Widget>() {
 
 			@Override
 			protected Widget run(String command, long param) {
 				FilledQuestionnaireTuple filledQuestionnaireTuple =
 					StaticGWTSyncer.getFilledquestionnairedao().getById(param);
 				if (filledQuestionnaireTuple == null)
 					return null;
 				return new EditFilledQuestionnaireWidget(filledQuestionnaireTuple,true);
 			}
 		});
 		router.addRoute(EditFilledQuestionnaireWidget.NAME_NEW, new LongRouteAction<Widget>() {
 
 			@Override
 			protected Widget run(String command, long param) {
 				QuestionnaireTuple questionnaireTuple =
 					StaticGWTSyncer.getQuestionnairedao().getById(param);
 				if (questionnaireTuple == null)
 					return null;
 				FilledQuestionnaireTuple filledQuestionnaireTuple = 
 					questionnaireTuple.getFilled();
				return new EditFilledQuestionnaireWidget(filledQuestionnaireTuple,true);
 			}
 		});
 	}
 	
 	private void showWidgets() {
 		ValueChangeHandler<String> valueChangeHandler =
 			new ValueChangeHandler<String>() {
 			@Override
 			public void onValueChange(ValueChangeEvent<String> event) {
 				String historyToken = event.getValue();
 				parseToken(historyToken);
 			}
 		};
 		History.addValueChangeHandler(valueChangeHandler);
 		History.fireCurrentHistoryState();
 		
 		Window.addWindowClosingHandler(new Window.ClosingHandler() {		
 			@Override
 			public void onWindowClosing(ClosingEvent event) {
 				if (StaticGWTSyncer.getAutosyncer().isSynced())
 					return;
 				event.setMessage("Tere are sync in progress - are you sure to exit?");
 			}
 		});
 	}
 	
 	private AutoSyncer.AutoSyncerObserver syncObserver = new AutoSyncer.AutoSyncerObserver() {
 		@Override
 		public void onSuccess() {
 			StaticGWTSyncer.getAutosyncer().removeAutoSyncerObserver(this);
 			showWidgets();
 		}
 		@Override
 		public void onWait(int waitTime) {}
 		@Override
 		public void onStarted() {}
 		@Override
 		public void onFail() {}
 	};
 
 	public MainWidget() {
 		initWidget(uiBinder.createAndBindUi(this));
 		
 		addRoutes();	
 		if (StaticGWTSyncer.getAutosyncer().isSynced()) {
 			showWidgets();
 		} else {
 			StaticGWTSyncer.getAutosyncer().addAutoSyncerObserver(syncObserver);
 			switchWidget(new WaitWidget());
 		}
 	}
 
 	protected void switchWidget(Widget newWidget) {
 		if (widget != null)
 			simplePanel.remove(widget);
 		widget = newWidget;
 		simplePanel.add(widget);
 	}
 	
 	@Override
 	protected void onAttach() {
 		super.onAttach();
 		autoSyncerObserver = new AutoSyncer.AutoSyncerObserver() {
 
 			@Override
 			public void onStarted() {
 				syncStatusLabel.setText("Syncing...");
 			}
 
 			@Override
 			public void onSuccess() {
 				syncStatusLabel.setText("");
 			}
 
 			@Override
 			public void onFail() {
 				syncStatusLabel.setText("FAIL");
 			}
 
 			@Override
 			public void onWait(int waitTime) {
 				syncStatusLabel.setText("Waiting: " + waitTime);
 			}
 			
 		};
 		StaticGWTSyncer.getAutosyncer().addAutoSyncerObserver(autoSyncerObserver);
 		
 	}
 	protected void parseToken(String historyToken) {
 		Widget newWidget = router.route(historyToken);
 		if (newWidget != null) {
 			switchWidget(newWidget);
 		} else {
 			switchWidget(new NotFoundWidget());
 		}
 	}
 
 	@Override
 	protected void onDetach() {
 		if (autoSyncerObserver != null)
 			StaticGWTSyncer.getAutosyncer().removeAutoSyncerObserver(autoSyncerObserver);
 		autoSyncerObserver = null;
 		super.onDetach();
 	}
 	@UiHandler("refreshButton")
 	void handleClick(ClickEvent e) {
 		StaticGWTSyncer.getAutosyncer().syncNow();
 	}
 
 }
