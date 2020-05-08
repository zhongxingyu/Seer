 package com.bookspicker.client.view;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import com.bookspicker.client.view.Resources.Style;
 import com.bookspicker.client.view.SearchPage.SearchHandler;
 import com.bookspicker.client.view.widgets.BPPanel;
 import com.bookspicker.client.view.widgets.BpOracle;
 import com.bookspicker.client.view.widgets.BpSuggestBox;
 import com.bookspicker.client.view.widgets.SuggestionTextBox;
 import com.bookspicker.shared.Book;
 import com.bookspicker.shared.BpOracleSuggestion;
 import com.bookspicker.shared.ClassBook;
 import com.bookspicker.shared.Item;
 import com.google.gwt.event.logical.shared.SelectionEvent;
 import com.google.gwt.event.logical.shared.SelectionHandler;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.user.client.ui.Anchor;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.Grid;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineLabel;
 import com.google.gwt.user.client.ui.SimplePanel;
 import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
 
 /**
  * View that displays the list of ResultsBookView.
  * 
  * @author sinchan, Rodrigo Ipince
  */
 public class ResultsView extends BPPanel {
 	
 	private final Style STYLE = Resources.INSTANCE.style();
 	
 	Grid searchBox = new Grid(1,3);
 	FlowPanel results = new FlowPanel();
 	
 	/**
 	 * Maps the set of books currently visible to their actual Views.
 	 */
 	Map<Book, ResultsBookView> bookViewMap;
 	
 	/**
 	 * Set of Books that are already in the user's Bundle, and are
 	 * therefore unpickable if displayed.
 	 */
 	Set<Book> unPickables;
 	
 	FlowPanel contentPanel;
 	public static SuggestionTextBox searchTextBox;
 	private final static String classSuggestionString = "Search by course, title, keyword, or ISBN...";
 	private final static String genericSuggestionString = "Search by title, keyword, or ISBN...";
 	public final BpSuggestBox autoCompleteBox;
 	public Image loadingIcon;
 	
 	
 	/** This constructor uses the SearchHandler of the new SearchPageUnified. 
 	 * This will be the default constructor after development of the new feature is finished.
 	 * @param isUnified
 	 */
 	public ResultsView(boolean isUnified) {
 		super();
 		this.setStylePrimaryName(STYLE.resultsView());
 		contentPanel = getContentPanel();
 		bookViewMap = new HashMap<Book, ResultsBookView>();
 		unPickables = new HashSet<Book>();
 		
 		com.bookspicker.client.view.SearchPageUnified.SearchHandler searchHandler = new com.bookspicker.client.view.SearchPageUnified.SearchHandler();
 		
 		searchTextBox = new SuggestionTextBox(classSuggestionString);
 		searchTextBox.setVisibleLength(40);
 
 		autoCompleteBox = new BpSuggestBox(BpOracle.getInstance(), searchTextBox);
 		autoCompleteBox.addSelectionHandler(new SelectionHandler<Suggestion>() {
 
 		    @Override
 		    public void onSelection(
 		            SelectionEvent<Suggestion> event) {
 		        if (event.getSelectedItem() instanceof BpOracleSuggestion) {
 		            BpOracleSuggestion bpSuggestion = (BpOracleSuggestion) event.getSelectedItem();
 		            autoCompleteBox.setSavedQuery(bpSuggestion.getQueryString());
 		        }
 		    }
 		});
 		
 		autoCompleteBox.addValueChangeHandler(new ValueChangeHandler<String>() {
             
             @Override
             public void onValueChange(ValueChangeEvent<String> event) {
                 autoCompleteBox.cleanSavedQuery();
             }
         });
 		
 		searchTextBox.reset();
 		autoCompleteBox.setLimit(8);
 		autoCompleteBox.setWidth("25em");
 		autoCompleteBox.setAutoSelectEnabled(true);
 		
 		if (SearchPage.AUTO_COMPLETE) {
 			searchTextBox.addKeyPressHandler(searchHandler);
 			searchTextBox.addKeyDownHandler(searchHandler);
 			autoCompleteBox.addSelectionHandler(searchHandler);
 			searchBox.setWidget(0, 0, autoCompleteBox);
 		} else {
 			searchTextBox.addKeyPressHandler(searchHandler);
 			searchBox.setWidget(0, 0, searchTextBox);
 		}
 		searchBox.setStylePrimaryName(STYLE.searchBox());
 		
		InlineLabel searchButton = new InlineLabel("Search");
 		searchButton.setStylePrimaryName(STYLE.bpYellowButton());
 		searchButton.addStyleName(STYLE.searchButton());
 		searchButton.addStyleDependentName(STYLE.bpYellowButton());
 		searchButton.addClickHandler(searchHandler);
 		searchBox.setWidget(0, 1, searchButton);
 		
 		loadingIcon = new Image(Resources.INSTANCE.loadingIconSmall());
 		loadingIcon.setStylePrimaryName(STYLE.loadingIcon());
 		loadingIcon.addStyleName(STYLE.loadingIconHidden());
 		searchBox.setWidget(0, 2, loadingIcon);
 		
 		SimplePanel searchWrapper = new SimplePanel();
 		searchWrapper.setStylePrimaryName(STYLE.searchBoxWrapper());
 		searchWrapper.setWidget(searchBox);
 		contentPanel.add(searchWrapper);
 		
 		results.setStylePrimaryName(STYLE.resultsList());
 
 		contentPanel.add(results);
 	}
 	
 	
 	public ResultsView() {
 		super("Search for your books and pick the ones you want to add to your cart!");
 		this.setStylePrimaryName(STYLE.resultsView());
 		contentPanel = getContentPanel();
 		bookViewMap = new HashMap<Book, ResultsBookView>();
 		unPickables = new HashSet<Book>();
 		
 		SearchHandler searchHandler = new SearchHandler();
 		
 		searchTextBox = new SuggestionTextBox(classSuggestionString);
 		searchTextBox.setVisibleLength(40);
 
 		autoCompleteBox = new BpSuggestBox(BpOracle.getInstance(), searchTextBox);
 		autoCompleteBox.addSelectionHandler(new SelectionHandler<Suggestion>() {
 
 		    @Override
 		    public void onSelection(
 		            SelectionEvent<Suggestion> event) {
 		        if (event.getSelectedItem() instanceof BpOracleSuggestion) {
 		            BpOracleSuggestion bpSuggestion = (BpOracleSuggestion) event.getSelectedItem();
 		            autoCompleteBox.setSavedQuery(bpSuggestion.getQueryString());
 		        }
 		    }
 		});
 		
 		autoCompleteBox.addValueChangeHandler(new ValueChangeHandler<String>() {
             
             @Override
             public void onValueChange(ValueChangeEvent<String> event) {
                 autoCompleteBox.cleanSavedQuery();
             }
         });
 		
 		searchTextBox.reset();
 		autoCompleteBox.setLimit(8);
 		autoCompleteBox.setWidth("25em");
 		autoCompleteBox.setAutoSelectEnabled(true);
 		
 		if (SearchPage.AUTO_COMPLETE) {
 			searchTextBox.addKeyPressHandler(searchHandler);
 			searchTextBox.addKeyDownHandler(searchHandler);
 			autoCompleteBox.addSelectionHandler(searchHandler);
 			searchBox.setWidget(0, 0, autoCompleteBox);
 		} else {
 			searchTextBox.addKeyPressHandler(searchHandler);
 			searchBox.setWidget(0, 0, searchTextBox);
 		}
 		searchBox.setStylePrimaryName(STYLE.searchBox());
 		
 		Image searchButton = new Image(Resources.INSTANCE.searchButton());
 		searchButton.setStylePrimaryName(STYLE.bpButton());
 		searchButton.addClickHandler(searchHandler);
 		searchBox.setWidget(0, 1, searchButton);
 		
 		loadingIcon = new Image(Resources.INSTANCE.loadingIconSmall());
 		loadingIcon.setStylePrimaryName(STYLE.loadingIcon());
 		loadingIcon.addStyleName(STYLE.loadingIconHidden());
 		searchBox.setWidget(0, 2, loadingIcon);
 		
 		SimplePanel searchWrapper = new SimplePanel();
 		searchWrapper.setStylePrimaryName(STYLE.searchBoxWrapper());
 		searchWrapper.setWidget(searchBox);
 		contentPanel.add(searchWrapper);
 		
 		results.setStylePrimaryName(STYLE.resultsList());
 
 		contentPanel.add(results);
 	}
 	
 	static public void setGeneric(boolean isGeneric){
 		if(isGeneric){
 			searchTextBox.setSuggestionText(genericSuggestionString);
 		}
 		else{
 			searchTextBox.setSuggestionText(classSuggestionString);
 		}
 	}
 	
 	public void addErrorMessage(String message) {
 		HTML title = new HTML(message);
 		title.setStylePrimaryName(STYLE.resultsErrorMessage());
 		results.add(title);
 	}
 	
 	public void addInfoMessage(String message){
 		HTML title = new HTML(message);
 		title.setStylePrimaryName(STYLE.resultsInfoMessage());
 		results.add(title);
 	}
 
 
 	public void addClassBook(ClassBook classBook) {
 		ResultsBookView view = new ResultsBookView(classBook);
 		bookViewMap.put(classBook.getBook(), view);
 		if (unPickables.contains(classBook.getBook())) {
 			view.setPickable(false);
 		}
 		results.add(view);
 	}
 
 	public void addBook(Book book){
 		ResultsBookView view = new ResultsBookView(book);
 		bookViewMap.put(book, view);
 		if (unPickables.contains(book)) {
 			view.setPickable(false);
 		}
 		results.add(view);
 	}
 
 	/**
 	 * Sets the pickability of the given book.
 	 */
 	public void setBookPickability(Book book, boolean pickable) {
 		ResultsBookView view = bookViewMap.get(book);
 		if (view != null) {
 			view.setPickable(pickable);
 		}
 		
 		if (pickable && unPickables.contains(book)) {
 			unPickables.remove(book);
 		}
 		
 		if (!pickable && !unPickables.contains(book)) {
 			unPickables.add(book);
 		}
 	}
 	
 	public void setAllBooksPickable() {
 		for (ResultsBookView view : bookViewMap.values())
 			if (view != null)
 				view.setPickable(true);
 		unPickables.clear();
 	}
 
 	public void clearResults() {
 		results.clear();
 		bookViewMap.clear();
 	}
   
 	/**
 	 * Returns a list of all the books' ISBNs separated by comma.
 	 */
 	public String getIsbnList() {
 		StringBuilder sb = new StringBuilder();
 		for (Item item : bookViewMap.keySet()) {
 			sb.append(item.getBook().getIsbn());
 			sb.append(",");
 		}
 		return sb.toString();
 	}
 }
