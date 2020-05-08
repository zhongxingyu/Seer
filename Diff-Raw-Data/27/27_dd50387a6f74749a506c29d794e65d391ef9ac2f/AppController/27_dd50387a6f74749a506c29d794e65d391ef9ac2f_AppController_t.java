 package arithmea.client;
 
 import arithmea.client.event.AddTermEvent;
 import arithmea.client.event.AddTermEventHandler;
 import arithmea.client.event.EditTermEvent;
 import arithmea.client.event.EditTermEventHandler;
 import arithmea.client.event.ParseTextEvent;
 import arithmea.client.event.ParseTextEventHandler;
 import arithmea.client.event.ShowInfoEvent;
 import arithmea.client.event.ShowInfoEventHandler;
 import arithmea.client.event.ShowListEvent;
 import arithmea.client.event.ShowListEventHandler;
 import arithmea.client.event.ShowNumberEvent;
 import arithmea.client.event.ShowNumberEventHandler;
 import arithmea.client.presenter.EditTermPresenter;
 import arithmea.client.presenter.InfoPresenter;
 import arithmea.client.presenter.NumberPresenter;
 import arithmea.client.presenter.ParseTextPresenter;
 import arithmea.client.presenter.Presenter;
 import arithmea.client.presenter.TermsPresenter;
 import arithmea.client.service.ArithmeaServiceAsync;
 import arithmea.client.view.EditTermView;
 import arithmea.client.view.InfoView;
 import arithmea.client.view.NumberView;
 import arithmea.client.view.ParseTextView;
 import arithmea.client.view.TermsView;
 
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.event.shared.HandlerManager;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.HasWidgets;
 
 /**
  * Controls the views and the presenters in relation to history.
  */
 public class AppController implements Presenter, ValueChangeHandler<String> {
     private final HandlerManager eventBus;
     private final ArithmeaServiceAsync rpcService;
     private HasWidgets container;
 
     public AppController(final ArithmeaServiceAsync rpcService,
             final HandlerManager eventBus) {
         this.eventBus = eventBus;
         this.rpcService = rpcService;
         bind();
     }
 
     /**
      * Binds and handles the global events.
      */
     private void bind() {
         History.addValueChangeHandler(this);
         eventBus.addHandler(AddTermEvent.TYPE, new AddTermEventHandler() {
             public void onAddTerm(final AddTermEvent event) {
                 doAddNewTerm(event.getWord());
             }
         });
         eventBus.addHandler(EditTermEvent.TYPE, new EditTermEventHandler() {
             public void onEditTerm(final EditTermEvent event) {
                 doEditTerm(event.getId());
             }
         });
         eventBus.addHandler(ShowListEvent.TYPE,
                 new ShowListEventHandler() {
                     public void onShowList(final ShowListEvent event) {
                         doShowList(event.getLetter(), event.getOffset());
                     }
                 });
         eventBus.addHandler(ShowNumberEvent.TYPE,
                 new ShowNumberEventHandler() {
                     public void onShowNumber(final ShowNumberEvent event) {
                         doShowNumber(event.getMethod(), event.getNumber());
                     }
                 });
         eventBus.addHandler(ParseTextEvent.TYPE,
                 new ParseTextEventHandler() {
                     public void onParseText(final ParseTextEvent event) {
                         doParseText();
                     }
                 });
         eventBus.addHandler(ShowInfoEvent.TYPE, new ShowInfoEventHandler() {
             @Override
             public void onShowInfo(final ShowInfoEvent event) {
                 doShowInfo();
             }
         });
     }
 
     /**
      * Fires history event to show the page that adds new words.
      * @param word
      */
     private void doAddNewTerm(final String word) {
         History.newItem("add/" + word);
     }
 
     /**
      * Prepares and runs the presenter to edit words.
      * @param id
      */
     private void doEditTerm(final String id) {
         History.newItem("edit/", false);
         Presenter presenter = new EditTermPresenter(rpcService, eventBus, new EditTermView(eventBus, ""), id);
         presenter.go(container);
     }
 
     /**
      * Fires history event to show the list for the provided letter at the provided offset.
      * @param letter
      * @param offset
      */
     private void doShowList(final String letter, final int offset) {
         if (letter.equals("All") && offset == 0) {
             History.newItem("list/");
         } else {
             History.newItem("list/" + letter + "/" + offset);
         }
     }
 
     /**
      * Fires history event to show word for the provided number and method.
      * @param method
      * @param number
      */
     private void doShowNumber(final String method, final String number) {
         if (method != null && method.length() > 0 && number != null && number.length() > 0) {
             History.newItem("show/" + method + "/" + number);
         } else {
             History.newItem("show/");
         }
     }
 
     /**
      * Fires history event to show the info page.
      */
     private void doShowInfo() {
         History.newItem("info/");
     }
 
     /**
      * Fires history event to show the page to parse texts.
      */
     private void doParseText() {
         History.newItem("parse/");
     }
 
     /**
      * Evaluates the history token and fires the related state.
      */
     public final void go(final HasWidgets container) {
         this.container = container;
         if ("".equals(History.getToken())) {
             History.newItem("list/");
         } else {
             History.fireCurrentHistoryState();
         }
     }
 
     /**
      * Maps changed tokens to the related presenters and creates them.
      */
     public final void onValueChange(final ValueChangeEvent<String> event) {
         final String token = event.getValue();
         if (token != null) {
             Presenter presenter = null;
             if (token.startsWith("list/")) {
                 String letterString = "All";
                 if (token.split("/").length > 1) {
                     letterString = token.split("/")[1];
                 }
                 String offsetString = "0";
                 if (token.split("/").length > 2) {
                     offsetString = token.split("/")[2];
                 }
                presenter = new TermsPresenter(rpcService, eventBus, new TermsView(eventBus, letterString, Integer.valueOf(offsetString)));
             } else if (token.startsWith("add") || token.startsWith("edit")) {
                 String wordString = "";
                 if (token.split("/").length > 1) {
                     wordString = token.split("/")[1];
                 }
                presenter = new EditTermPresenter(rpcService, eventBus, new EditTermView(eventBus, wordString));
             } else if (token.startsWith("show/")) {
                 String methodString = "All";
                 if (token.split("/").length > 1) {
                     methodString = token.split("/")[1];
                 }
                 String numberString = "0";
                 if (token.split("/").length > 2) {
                     numberString = token.split("/")[2];
                 }
                presenter = new NumberPresenter(rpcService, eventBus, new NumberView(methodString, numberString));
             } else if (token.equals("parse/")) {
                presenter = new ParseTextPresenter(rpcService, eventBus, new ParseTextView());
             } else if (token.equals("info/")) {
                presenter = new InfoPresenter(rpcService, eventBus, new InfoView());
             } else if (token.equals("deleteAll/")) {
                 rpcService.deleteAllTerms(new AsyncCallback<String>() {
                     public void onSuccess(final String result) {
                         Window.alert("deleted " + result + " terms.");
                     }
                     public void onFailure(final Throwable caught) {
                         Window.alert("Fail deleting terms: " + caught);
                     }
                 });
            } else { //default case
                presenter = new TermsPresenter(rpcService, eventBus, new TermsView(eventBus, "All", 0));
            }
            if (presenter != null) {
                presenter.go(container);
             }
         }
     }
 }
