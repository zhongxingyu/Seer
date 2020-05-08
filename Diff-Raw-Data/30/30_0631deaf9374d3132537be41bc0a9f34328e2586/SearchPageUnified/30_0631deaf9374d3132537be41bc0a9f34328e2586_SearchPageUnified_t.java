 package com.bookspicker.client.view;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.bookspicker.client.BooksPicker;
 import com.bookspicker.client.HistoryToken;
 import com.bookspicker.client.service.QueryService;
 import com.bookspicker.client.service.QueryServiceAsync;
 import com.bookspicker.client.service.StatService;
 import com.bookspicker.client.service.StatServiceAsync;
 import com.bookspicker.client.view.widgets.buttons.BuyOfferButton;
 import com.bookspicker.client.view.widgets.buttons.PickButton;
 import com.bookspicker.shared.Book;
 import com.bookspicker.shared.ClassBook;
 import com.bookspicker.shared.Constants;
 import com.bookspicker.shared.Item;
 import com.bookspicker.shared.Offer;
 import com.bookspicker.shared.School;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.event.dom.client.KeyDownEvent;
 import com.google.gwt.event.dom.client.KeyDownHandler;
 import com.google.gwt.event.dom.client.KeyPressEvent;
 import com.google.gwt.event.dom.client.KeyPressHandler;
 import com.google.gwt.event.logical.shared.SelectionEvent;
 import com.google.gwt.event.logical.shared.SelectionHandler;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.Command;
 import com.google.gwt.user.client.Cookies;
 import com.google.gwt.user.client.DeferredCommand;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.SimplePanel;
 import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * 
  * Controller for all the different views under the Search Page Panel.
  * 
  * @author Sinchan Banerjee, Jonathan Goldberg, Rodrigo Ipince
  * 
  */
 public class SearchPageUnified extends Composite implements HasHeader {
 
     private static SearchPageUnifiedUiBinder uiBinder = GWT
             .create(SearchPageUnifiedUiBinder.class);
 
     interface SearchPageUnifiedUiBinder extends UiBinder<Widget, SearchPageUnified> {
     }
 
     public static final boolean AUTO_COMPLETE = true;
     private static final String NO_BOOKS_FOUND_MSG = "We didn't find any books";
     private static final String CLASS_NOT_FOUND_MSG = "We didn't find a class";
 
     // Services
     private static final QueryServiceAsync queryService = GWT.create(QueryService.class);
     private static final StatServiceAsync statService = GWT.create(StatService.class);
 
     @UiField
     static SimplePanel headerViewWrapper;
     private static final HeaderView headerView = HeaderView.getHeaderView();
 
     @UiField
     static SimplePanel centralViewWrapper;
 
     static ResultsView resultsView;
 
     // State
     private static String currentQuery;
     private static String currentToken;
 
     // Used to keep track of which results to render
     // on the page
     private static int offerRequestCounter = 0;
 
     private static Map<Book, List<Offer>> bookOffers = new HashMap<Book, List<Offer>>();
 
     public SearchPageUnified() {
         initWidget(uiBinder.createAndBindUi(this));
 
         headerView.setSelectedMenuItem(Page.SEARCH);
         headerViewWrapper.setWidget(headerView);
         centralViewWrapper.setStylePrimaryName(Resources.INSTANCE.style()
                 .centralViewWrapper());
         resultsView = new ResultsView(true);
         centralViewWrapper.setWidget(resultsView);
     }
 
     public void setState(String queryParam, String bundleParam,
             String offersParam, String displayedQuery) {
 
         String newToken = buildToken(queryParam, displayedQuery);//resultsView.autoCompleteBox.getDisplayedQuery());
 
         GWT.log("current token: " + (currentToken == null ? "null" : currentToken), null);
         GWT.log("new token: " + newToken, null);
 
         if (newToken.equals(currentToken)) { // do nothing
             GWT.log("No change in state - doing nothing", null);
             return;
         }
 
         // If no bundle params, load what's in the cookie
         // Note: Cookies actually make this really complicated,
         // so I'm removing them for now
         //		if (bundleParam == null) {
         //			GWT.log("SearchPage - Using cookie: " + Cookies.getCookie("bundle"));
         //			bundleParam = Cookies.getCookie("bundle");
         //		}
 
         setSearchState(queryParam, displayedQuery);
     }
 
     private void setSearchState(String query, String displayedQuery) {
         if (query == null) {
             clearSearch();
         } else {
 
             if (query.equals(currentQuery))
                 return; // nothing to do
 
             // Prepare request
             String[] ids = query.split(",");
             List<String> queries = new ArrayList<String>();
             for (String id : ids) {
                 queries.add(id.trim());
             }
 
             // Set actual state
             resultsView.autoCompleteBox.setText(displayedQuery);
             currentQuery = query;
             setHistoryToken(false);
 
             // Send the request
             resultsView.loadingIcon.removeStyleName(Resources.INSTANCE.style().loadingIconHidden());
             queryService.search(BooksPicker.getSchool(), queries, new SearchCallback(displayedQuery));
         }
     }
 
 
     // ============= Handlers =============
 
     public static ClickHandler checkPricesHandler = new ClickHandler() {
         @Override
         public void onClick(ClickEvent event) {
             PickButton button = (PickButton) event.getSource();
             if (button.isEnabled()) {
                 displayOffers((ResultsBookView) button.getParent());
                 setHistoryToken(true);
             }
         }
     };
 
     public static class SearchHandler implements ClickHandler, KeyPressHandler, KeyDownHandler, SelectionHandler<Suggestion> {
 
         @Override
         public void onClick(ClickEvent event) {
             findBooks(resultsView.autoCompleteBox.getText(), resultsView.autoCompleteBox.getDisplayedQuery());
         }
 
         @Override
         public void onKeyPress(KeyPressEvent event) {
             if (event.getCharCode() == KeyCodes.KEY_ENTER) {
                 DeferredCommand.addCommand(new Command() {
                     @Override
                     public void execute() {
                         findBooks(resultsView.autoCompleteBox.getText(), resultsView.autoCompleteBox.getDisplayedQuery());
                     }
                 });
             }
         }
 
         @Override
         public void onKeyDown(KeyDownEvent event) {
             if (event.getNativeKeyCode() == KeyCodes.KEY_ESCAPE) {
                 if (resultsView.autoCompleteBox.isSuggestionListShowing())
                     resultsView.autoCompleteBox.hideSuggestionList();
             }
         }
 
         @Override
         public void onSelection(SelectionEvent<Suggestion> event) {
             findBooks(resultsView.autoCompleteBox.getText(), resultsView.autoCompleteBox.getDisplayedQuery());
         }
 
         private void findBooks(String query, final String displayedQuery) {
             // Set history stuff
             currentQuery = query;
             setHistoryToken(true);
 
             if (query.isEmpty()) {
                 resultsView.clearResults();
             } else {
                 // Build request
                 String[] raw = query.split(",");
                 List<String> queries = new ArrayList<String>();
                 for (int i = 0; i < raw.length; i++)
                     queries.add(raw[i].trim());
 
                 resultsView.loadingIcon.removeStyleName(Resources.INSTANCE.style().loadingIconHidden());
                 queryService.search(BooksPicker.getSchool(), queries, new SearchCallback(displayedQuery));
             }
         }
     }
 
 
     public static ClickHandler buyHandler = new ClickHandler() {
         @Override
         public void onClick(ClickEvent event) {
             BuyOfferButton button = (BuyOfferButton) event.getSource();
 
             Book book = button.getBook();
             Offer clickedOffer = button.getOffer();
             List<Offer> competingOffers = bookOffers.get(book);
 
             // Save stat
             statService.logBuyClick(book.getIsbn(),
                     clickedOffer, competingOffers,
                     emptyCallback);
 
             // Open window with offer
             Window.open(button.getOffer().getUrl(), "_blank", "");
         }
     };
 
 
     // ============= Callbacks =============
 
     /**
      * Returns an AsyncCallback to be used in QueryService.getBookInfo().
      * 
      * Depending on insertInBundle, the callback will either insert the corresponding
      * books (returned by the QueryService) into the bundle or into the
      * search result area.
      */
 
     public static AsyncCallback<List<Offer>> getOfferCallback(final ResultsBookView bookView) {
         return new AsyncCallback<List<Offer>>() {
             @Override
             public void onSuccess(List<Offer> result) {
                 bookView.showOffers(result);
                 SearchPageUnified.bookOffers.put(bookView.getBook(), result);
             }
 
             @Override
             public void onFailure(Throwable caught) {
                 bookView.reset();
                 GWT.log(caught.getMessage(), caught);
             }
         };
     }
 
     private static AsyncCallback<Void> emptyCallback = new AsyncCallback<Void>() {
         @Override
         public void onSuccess(Void result) {
             GWT.log("SearchPage - onSuccess at empty callback", null);
         }
         @Override
         public void onFailure(Throwable caught) {
             GWT.log("SearchPage - onFailure at empty callback", null);
         }
     };
 
     // ============= Convenience methods =============
 
 
     private static void displayOffers(ResultsBookView bookView) {
         bookView.showLoading();
         offerRequestCounter++;
         GWT.log("SearchPage - Sending request #" + offerRequestCounter + " for offers", null);
         //TODO: Figure out why things are crashing with null pointer exceptions for non-MIT schools.
         queryService.getOffersForBook(BooksPicker.getSchool(), bookView.getBook(), true, getOfferCallback(bookView));
     }
 
     private static void clearSearch() {
         currentQuery = "";
         resultsView.searchTextBox.reset();
         resultsView.autoCompleteBox.cleanSavedQuery();
         resultsView.clearResults();
     }
 
     public static void reset(){
         clearSearch();
     }
 
     private static void populateSearchResults(Map<String, List<Item>> results, String displayedQuery) {
 
         List<Item> result;
         // used to determine whether to show a header for each query or not
         boolean multipleQueries = results.keySet().size() > 1;
         for (String query : results.keySet()) {
             result = results.get(query);
 
             if (result == null) {
                 // class not found
                 resultsView.addInfoMessage(CLASS_NOT_FOUND_MSG + " matching \"" + displayedQuery + "\"");
             } else {
                 if (result.isEmpty()) {
                     // No books found -> display differently depending on query type
                    if (query.matches(Constants.CLASS_REGEX_STR)
                            || query.matches(Constants.DB_ID_STR)) {
                         resultsView.addInfoMessage(NO_BOOKS_FOUND_MSG + " for " + displayedQuery + "." +
                                "<br /><br />This class probably requires no books, but we cannot guarantee it. " +
                                "You might need to double-check on your own. Sorry! :(");
                        //                                "<br /><br />Let us know if we're missing any of " + displayedQuery + "'s books through the feedback tab on the right.");
                     } else {
                         resultsView.addInfoMessage("<br />"+NO_BOOKS_FOUND_MSG + " matching \"" + displayedQuery + "\"");
                     }
                 } else {
 
                     // Show ClassBooks first
                     ClassBook cb;
                     boolean headerShown = false;
                     List<ClassBook> classbooksToAdd = new ArrayList<ClassBook>();
                     boolean noBooksForClass = false;
                     for (Item item : result) {
                         //TODO (Sinchan) - can you please change the info message to an error message. See how it looks when
                         // you query for multiple books and one of the ISBN is broken. (e.g: 9780262033844, 6.003)
 
                         if (item == null) {
                             resultsView.addErrorMessage(NO_BOOKS_FOUND_MSG + " matching \"" + query + "\"<br />");
                             headerShown = true;
                         }
 
                         if (item instanceof ClassBook) {
                             cb = (ClassBook) item;
                             // Only display the class title for the first book!
                             if (!headerShown) {
                                 String fullTitle = cb.getSchoolClass().getCode();
                                 if (cb.getSchoolClass().getTitle() != null) {
                                     fullTitle += " - " + cb.getSchoolClass().getTitle();
                                 }
                                 if (cb.getSchoolClass().getSchool().equals(School.MIT)
                                         && cb.getSchoolClass().getClassInfoUrl() != null) {
                                     String linkToCatalog = "<a style='font-weight: normal' href='"
                                             + cb.getSchoolClass().getClassInfoUrl()
                                             + "' target='_blank'>" + fullTitle + "</a>";
                                     resultsView.addInfoMessage("Books for " + linkToCatalog);
                                 } else {
                                     resultsView.addInfoMessage("Books for " + fullTitle);
                                 }
                                 headerShown = true;
                             }
                             // IF we see a classbook with hasBook == false => no books for this class
                             if (!cb.getHasBook()) {
                                 noBooksForClass = true;
                                 String msg = "It appears no books are needed for your class";
                                 if (cb.getUrl() != null) {
                                     msg += " (doublecheck <a style='font-weight: normal' href='"
                                             + cb.getUrl() + "' target='_blank'> here</a>).";
                                 }
                                 resultsView.addInfoMessage(msg);
                             }
                             classbooksToAdd.add(cb);
                         } else if (item instanceof Book) {
                             // Only show header for regular title search if many queries were made (to distinguish)
                             if (multipleQueries && !headerShown) {
                                 resultsView.addInfoMessage("Books matching \"" + query + "\":");
                                 headerShown = true;
                             }
                             resultsView.addBook((Book) item);
                         }
                     }
                     if (!noBooksForClass) {
                         for (ClassBook classbook : classbooksToAdd) {
                             resultsView.addClassBook(classbook);
                         }
                     }
                 }
             }
         }
     }
 
     // ============= History stuff =============
 
     private static void setHistoryToken(boolean addToStack) {
         currentToken = buildToken();
         Cookies.setCookie("searchState", currentToken);
         if (addToStack)
             History.newItem(currentToken, false);
 
     }
 
     private static String buildToken() {
         return buildToken(currentQuery, resultsView.autoCompleteBox.getDisplayedQuery());
     }
 
     public static String buildToken(String queryParam, String displayedQuery) {
         StringBuilder sb = new StringBuilder();
         sb.append(HistoryToken.SEARCH_UNIFIED);
         if ((queryParam != null && !queryParam.isEmpty())) {
             sb.append(HistoryToken.PARAM_STARTER);
         }
 
         String delimeter = "";
 
         if (queryParam != null && !queryParam.isEmpty()) {
             sb.append(delimeter);
             delimeter = HistoryToken.PARAM_DELIMETER;
             sb.append(HistoryToken.PARAM_QUERY);
             sb.append("=");
             sb.append(queryParam);
             sb.append(HistoryToken.PARAM_DELIMETER);
             sb.append(HistoryToken.DISPLAYED_QUERY);
             sb.append("=");
             sb.append(displayedQuery);
         }
 
         return sb.toString();
     }
 
     @Override
     public void updateLoginContainer() {
         headerView.updateLoginContainer();
     }
 
     @Override
     protected void onLoad() {
         super.onLoad();
         headerViewWrapper.setWidget(headerView);
         headerView.setSelectedMenuItem(Page.SEARCH);
     }
 
     public static class SearchCallback implements AsyncCallback<Map<String, List<Item>>> {
         private final String  _displayedQuery;
 
         public SearchCallback(String displayedQuery) {
             _displayedQuery = displayedQuery;
         }
 
         @Override
         public void onFailure(Throwable caught) {
             resultsView.clearResults();
             resultsView.loadingIcon.addStyleName(Resources.INSTANCE.style().loadingIconHidden());
             // Do nothing....
             GWT.log("There was an error retrieving the books", null);
             resultsView.autoCompleteBox.cleanSavedQuery();
 
         }
 
         @Override
         public void onSuccess(Map<String, List<Item>> result) {
             resultsView.clearResults();
             populateSearchResults(result, _displayedQuery);
             resultsView.loadingIcon.addStyleName(Resources.INSTANCE.style().loadingIconHidden());
             resultsView.autoCompleteBox.cleanSavedQuery();
         }
     }
 
 }
