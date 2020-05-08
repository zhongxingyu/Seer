 package ziraja.client;
 
 import ziraja.client.event.CancelledEvent;
 import ziraja.client.event.CancelledEventHandler;
 import ziraja.client.event.DoAnswerEvaluationEvent;
 import ziraja.client.event.DoAnswerEvaluationEventHandler;
 import ziraja.client.event.UpdateAnswerEvent;
 import ziraja.client.event.UpdateAnswerEventHandler;
 import ziraja.client.event.UpdateLetterEvent;
 import ziraja.client.event.UpdateLetterEventHandler;
 import ziraja.client.event.UpdateTotalEvent;
 import ziraja.client.event.UpdateTotalEventHandler;
 import ziraja.client.presenter.MainPresenter;
 import ziraja.client.presenter.Presenter;
 import ziraja.client.presenter.QuestionPresenter;
 import ziraja.client.presenter.ReportsPresenter;
 import ziraja.client.service.DoesItMeanService;
 import ziraja.client.service.DoesItMeanServiceAsync;
 import ziraja.client.service.PersistenceService;
 import ziraja.client.service.PersistenceServiceAsync;
 import ziraja.client.view.MainView;
 import ziraja.client.view.QuestionView;
 import ziraja.client.view.ReportsView;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.logical.shared.SelectionEvent;
 import com.google.gwt.event.logical.shared.SelectionHandler;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.event.shared.HandlerManager;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.ui.HasWidgets;
 import com.google.gwt.user.client.ui.TabPanel;
 
 public class AppController implements Presenter, ValueChangeHandler<String> {
     private final HandlerManager eventBus;
     private HasWidgets container;
 
     private final DoesItMeanServiceAsync doesItMeanService = GWT.create(DoesItMeanService.class);
     private final PersistenceServiceAsync persistenceService = GWT.create(PersistenceService.class);
 
     private final TabPanel tabPanel = new TabPanel();
     private final MainView mainView = new MainView();
     private QuestionView questionView;
     private final ReportsView reportsView = new ReportsView();
 
     private Presenter mainPresenter;
     private QuestionPresenter questionPresenter;
     private Presenter reportsPresenter;
 
     public AppController(final HandlerManager eventBus) {
         this.eventBus = eventBus;
         questionView = new QuestionView(eventBus, persistenceService, doesItMeanService);
         mainPresenter = new MainPresenter(eventBus, tabPanel, mainView);
         questionPresenter = new QuestionPresenter(eventBus, tabPanel, questionView, persistenceService);
         reportsPresenter = new ReportsPresenter(eventBus, tabPanel, reportsView, persistenceService);
         prepareTabs();
         bind();
     }
 
     private void prepareTabs() {
         tabPanel.addSelectionHandler(new SelectionHandler<Integer>() {
             @Override
             public void onSelection(final SelectionEvent<Integer> event) {
                 int selection = event.getSelectedItem().intValue();
                 if (selection == 0) {
                     if (!History.getToken().startsWith("main")) {
                         History.newItem("main/");
                     }
                 }
                 if (selection == 1) {
                     if (!History.getToken().startsWith("question")) {
                         History.newItem("question/" + questionView.getQuestionTextBox().getValue());
                     }
                 }
                 if (selection == 2) {
                     if (!History.getToken().startsWith("reports")) {
                         History.newItem("reports/");
                     }
                 }
             }
         });
        //tabPanel.setAnimationEnabled(true); //may cause problems in chrome.
         tabPanel.add(mainView.asWidget(), "Main");
         tabPanel.add(questionView.asWidget(), "Question");
         tabPanel.add(reportsView.asWidget(), "Reports");
     }
 
     private void bind() {
         History.addValueChangeHandler(this);
         eventBus.addHandler(CancelledEvent.TYPE, new CancelledEventHandler() {
             public void onCancelled(final CancelledEvent event) {
                 doCancelled();
             }
         });
         eventBus.addHandler(UpdateLetterEvent.TYPE, new UpdateLetterEventHandler() {
             public void onUpdated(final UpdateLetterEvent event) {
                 questionView.doUpdateLetter();
             }
         });
         eventBus.addHandler(UpdateTotalEvent.TYPE, new UpdateTotalEventHandler() {
             public void onUpdated(final UpdateTotalEvent event) {
                 questionView.doUpdateTotal();
             }
         });
         eventBus.addHandler(UpdateAnswerEvent.TYPE, new UpdateAnswerEventHandler() {
             public void onUpdated(final UpdateAnswerEvent event) {
                 questionView.doUpdateAnswer();
             }
         });
         eventBus.addHandler(DoAnswerEvaluationEvent.TYPE, new DoAnswerEvaluationEventHandler() {
             public void onEvaluate(final DoAnswerEvaluationEvent event) {
                 questionView.doAnswerEvaluation();
             }
         });
     }
 
     private void doCancelled() {
         History.newItem("question/" + questionView.getQuestionTextBox().getValue());
     }
 
     public final void go(final HasWidgets container) {
         this.container = container;
         if ("".equals(History.getToken())) {
             History.newItem("main/");
         } else {
             History.fireCurrentHistoryState();
         }
     }
 
     public final void onValueChange(final ValueChangeEvent<String> event) {
         final String token = event.getValue();
         if (token.equals("main/")) {
             tabPanel.selectTab(0);
             mainPresenter.go(container);
         } else if (token.equals("reports/")) {
             tabPanel.selectTab(2);
             reportsPresenter.go(container);
         } else {
             tabPanel.selectTab(1);
             String question = "";
             if (token.split("/").length > 1) {
                 question = token.split("/")[1];
                 questionView.setQuestion(question);
                 questionPresenter.fillTables();
             }
             questionPresenter.go(container);
             questionView.resetFocus();
         }
     }
 
 }
