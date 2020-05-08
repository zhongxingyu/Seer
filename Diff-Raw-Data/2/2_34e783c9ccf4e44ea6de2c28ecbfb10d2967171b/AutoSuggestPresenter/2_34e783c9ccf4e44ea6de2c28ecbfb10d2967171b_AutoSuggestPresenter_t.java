 /**
  * A presenter for auto suggest box to search the word easily from the Dictionary
  * @author Kandasamy
  * @version 0.2
  * @since August 2011
  */
 
 package org.palaso.languageforge.client.lex.browse.edit.presenter;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.SortedMap;
 
 import org.palaso.languageforge.client.lex.browse.edit.BrowseAndEditEventBus;
 import org.palaso.languageforge.client.lex.browse.edit.view.AutoSuggestView;
 import org.palaso.languageforge.client.lex.common.AutoSuggestPresenterOption;
 import org.palaso.languageforge.client.lex.common.AutoSuggestPresenterOptionResultSet;
 import org.palaso.languageforge.client.lex.common.Constants;
 import org.palaso.languageforge.client.lex.common.I18nConstants;
 import org.palaso.languageforge.client.lex.main.service.ILexService;
 
 import com.github.gwtbootstrap.client.ui.base.TextBox;
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ChangeHandler;
 import com.google.gwt.event.dom.client.FocusEvent;
 import com.google.gwt.event.dom.client.FocusHandler;
 import com.google.gwt.event.logical.shared.SelectionEvent;
 import com.google.gwt.event.logical.shared.SelectionHandler;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.SuggestBox;
 import com.google.gwt.user.client.ui.SuggestOracle;
 import com.google.gwt.user.client.ui.SuggestOracle.Callback;
 import com.google.gwt.user.client.ui.SuggestOracle.Request;
 import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
 import com.google.inject.Inject;
 import com.mvp4g.client.annotation.Presenter;
 import com.mvp4g.client.presenter.BasePresenter;
 
 @Presenter(view = AutoSuggestView.class)
 public class AutoSuggestPresenter
 		extends
 		BasePresenter<AutoSuggestPresenter.ISuggestBoxView, BrowseAndEditEventBus>
 		implements SelectionHandler<Suggestion>, ChangeHandler {
 
 	@Inject
 	public ILexService LexService;
 	private SuggestBox suggestBox;
 	private Map<String, String> valueMap;
 	private int indexFrom = 0;
 	private static final String PRE_TEXT = I18nConstants.STRINGS.AutoSuggestPresenter_Start_your_search_here();
 
 	public interface ISuggestBoxView {
 		void setSuggestBox(SuggestBox suggestBox);
 	}
 
 	public void onStart() {
 		TextBox textfield = new TextBox();
 		SuggestOracle oracle = new RestSuggestOracle();
 		suggestBox = new SuggestBox(oracle, textfield);
 		suggestBox.addSelectionHandler(this);
 		suggestBox.getTextBox().addChangeHandler(this);
		suggestBox.setWidth("225px");
 		suggestBox.setText(PRE_TEXT);
 		view.setSuggestBox(suggestBox);
 		valueMap = new HashMap<String, String>();
 		resetPageIndices();
 		suggestBox.getTextBox().addFocusHandler(new FocusHandler() {
 
 			@Override
 			public void onFocus(FocusEvent event) {
 				if (suggestBox.getText().equals(PRE_TEXT)) {
 					suggestBox.setText("");
 				}
 			}
 		});
 	}
 
 	/**
 	 * This will be called when user click inside the suggest box
 	 * 
 	 */
 	@Override
 	public void onChange(ChangeEvent event) {
 		if (suggestBox.getText().equals(PRE_TEXT))
 			suggestBox.setText("");
 
 	}
 
 	/**
 	 * This will be called when user select am item from suggestbox dropdown
 	 * list
 	 * 
 	 */
 	@Override
 	public void onSelection(SelectionEvent<Suggestion> event) {
 		Suggestion suggestion = event.getSelectedItem();
 		if (suggestion instanceof OptionSuggestion) {
 			OptionSuggestion osugg = (OptionSuggestion) suggestion;
 			// if NEXT or PREVIOUS were selected, requery but bypass the timer
 			String value = osugg.getValue();
 			if (OptionSuggestion.NEXT_VALUE.equals(value)) {
 				indexFrom += Constants.SUGGEST_BOX_PAGE_SIZE;
 
 				RestSuggestOracle oracle = (RestSuggestOracle) suggestBox
 						.getSuggestOracle();
 				oracle.getSuggestions();
 
 			} else if (OptionSuggestion.PREVIOUS_VALUE.equals(value)) {
 				indexFrom -= Constants.SUGGEST_BOX_PAGE_SIZE;
 
 				RestSuggestOracle oracle = (RestSuggestOracle) suggestBox
 						.getSuggestOracle();
 				oracle.getSuggestions();
 
 			} else {
 
 				// add the option's value to the value map
 				putValue(osugg.getName(), value);
 				suggestBox.setText(osugg.getName());
 				// put the focus back into the textfield so user
 				// can enter more
 				suggestBox.setFocus(true);
 			}
 		}
 		// to load the entry values in rhs
 		String guid = null;
 		for (Entry<String, String> entry : valueMap.entrySet()) {
 			String s1 = entry.getKey();
 			String s2 = getValue();
 			if (s2.equals(s1))
 				guid = entry.getValue();
 		}
 		if (guid != null) {
 			eventBus.wordSelected(guid);
 		}
 	}
 
 	private void resetPageIndices() {
 		indexFrom = 0;
 	}
 
 	/**
 	 * Put the key(guid), value(word) as a String in map.
 	 * 
 	 */
 	private void putValue(String key, String value) {
 		valueMap.put(key, value);
 	}
 
 	/**
 	 * Get the value as a String from textbox.
 	 * 
 	 * @return value as a String
 	 */
 	public String getValue() {
 		return suggestBox.getText();
 	}
 
 	/**
 	 * Get the value map
 	 * 
 	 * @return value map
 	 */
 	public Map<String, String> getValueMap() {
 		return valueMap;
 	}
 
 	/**
 	 * Retrieve Options (name-value pairs) that are suggested from the JSON-RPC server
 	 * 
 	 * @param query
 	 *            - the String search term
 	 * @param from
 	 *            - the 0-based begin index int
 	 * @param limit
 	 *            - the end index inclusive int
 	 * @param callback
 	 *            - the OptionQueryCallback to handle the response
 	 */
 	private void queryOptions(final String query, final int from, final int limit, final OptionQueryCallback callback) {
 		LexService.getWordsForAutoSuggest(query, from, limit, new AsyncCallback<SortedMap<String, String>>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				eventBus.handleError(caught);
 			}
 	
 			@Override
 			public void onSuccess(SortedMap<String, String> result) {
 	
 				AutoSuggestPresenterOptionResultSet options = new AutoSuggestPresenterOptionResultSet(
 					result.size()
 				);
 				if (result.size() > 0) {
 					Iterator<String> iterator = result.keySet().iterator();
 					while (iterator.hasNext()) {
 						Object key = iterator.next();
 						if (result.get(key) == null || result.get(key).isEmpty()) {
 							continue;
 						}
 						AutoSuggestPresenterOption option = new AutoSuggestPresenterOption();
 						option.setName(result.get(key));
 						option.setValue(key.toString());
 						options.addOption(option);
 					}
 				}
 				callback.success(options);
 			}
 		});
 	}
 
 	/**
 	 * A custom Suggest Oracle
 	 */
 	private class RestSuggestOracle extends SuggestOracle {
 		private SuggestOracle.Request request;
 		private SuggestOracle.Callback callback;
 		private Timer timer;
 
 		public RestSuggestOracle() {
 			timer = new Timer() {
 
 				@Override
 				public void run() {
 
 					if (!suggestBox.getText().trim().isEmpty() 
 						&& suggestBox.getText().trim().length() > Constants.SUGGEST_BOX_MINIMUM_CHAR - 1
 					) {
 						getSuggestions();
 					}
 				}
 			};
 		}
 
 		@Override
 		public void requestSuggestions(SuggestOracle.Request request, SuggestOracle.Callback callback) {
 			// This is the method that gets called by the SuggestBox whenever
 			// some types into the text field
 			this.request = request;
 			this.callback = callback;
 
 			// reset the indexes (b/c NEXT and PREV call getSuggestions
 			// directly)
 			resetPageIndices();
 
 			// If the user keeps triggering this event (e.g., keeps typing),
 			// cancel and restart the timer
 			timer.cancel();
 			timer.schedule(Constants.SUGGEST_BOX_DELAY);
 		}
 
 		public void getSuggestions() {
 			String query = request.getQuery();
 
 			query = query.trim();
 			if (query.length() > 0 && valueMap.get(query) == null) {
 
 				queryOptions(
 					query, indexFrom, Constants.SUGGEST_BOX_PAGE_SIZE,
 					new RestSuggestCallback(request, callback, query)
 				);
 			}
 		}
 
 		@Override
 		public boolean isDisplayStringHTML() {
 			return true;
 		}
 	}
 
 	/**
 	 * A custom callback that has the original SuggestOracle.Request and
 	 * SuggestOracle.Callback
 	 */
 	private class RestSuggestCallback extends OptionQueryCallback {
 		private SuggestOracle.Request request;
 		private SuggestOracle.Callback callback;
 		private String query;
 
 		RestSuggestCallback(Request request, Callback callback, String query) {
 			this.request = request;
 			this.callback = callback;
 			this.query = query;
 		}
 
 		public void success(AutoSuggestPresenterOptionResultSet optResults) {
 			SuggestOracle.Response resp = new SuggestOracle.Response();
 			List<OptionSuggestion> suggestions = new ArrayList<OptionSuggestion>();
 			int totalSize = optResults.getTotalSize();
 
 			if (totalSize == 1) {
 				// it's an exact match, so do not bother with showing
 				// suggestions,
 				AutoSuggestPresenterOption o = optResults.getOptions()[0];
 				String displ = o.getName();
 
 				// set the value into the valueMap
 				putValue(displ, String.valueOf(o.getValue()));
 				OptionSuggestion suggestion = new OptionSuggestion(
 					o.getName(), String.valueOf(o.getValue()), request.getQuery(), query
 				);
 				suggestions.add(suggestion);
 
 			} else {
 
 				// if not at the first page, show PREVIOUS
 				if (indexFrom > 0) {
 					OptionSuggestion prev = new OptionSuggestion(OptionSuggestion.PREVIOUS_VALUE, request.getQuery());
 					suggestions.add(prev);
 				}
 
 				// show the suggestions
 				for (AutoSuggestPresenterOption o : optResults.getOptions()) {
 					OptionSuggestion sugg = new OptionSuggestion(o.getName(),
 							String.valueOf(o.getValue()), request.getQuery(), query);
 					suggestions.add(sugg);
 				}
 
 				// if there are more pages, show NEXT
 				if (indexFrom + Constants.SUGGEST_BOX_PAGE_SIZE < totalSize) {
 					OptionSuggestion next = new OptionSuggestion(OptionSuggestion.NEXT_VALUE, request.getQuery());
 					suggestions.add(next);
 				}
 
 			}
 
 			// it's ok (and good) to pass an empty suggestion list back to the
 			// suggest box's callback method
 			// the list is not shown at all if the list is empty.
 			resp.setSuggestions(suggestions);
 			callback.onSuggestionsReady(request, resp);
 		}
 
 		@Override
 		public void error(Throwable excOptionResultSeteption) {
 
 		}
 
 	}
 
 	/**
 	 * A bean to serve as a custom suggestion so that the value is available and
 	 * the replace will look like it is supporting multivalues
 	 */
 	private class OptionSuggestion implements SuggestOracle.Suggestion {
 		private String display;
 		private String replace;
 		private String value;
 		private String name;
 
 		public static final String NEXT_VALUE = "NEXT";
 		static final String PREVIOUS_VALUE = "PREVIOUS";
 
 		
 		OptionSuggestion(String nav, String currentTextValue) {
 			if (NEXT_VALUE.equals(nav)) {
 				display = "<div class=\"autocompleterNext\" title=\"" + I18nConstants.STRINGS.AutoSuggestPresenter_Next() + "\"></div>";
 			} else {
 				display = "<div class=\"autocompleterPrev\" title=\"" + I18nConstants.STRINGS.AutoSuggestPresenter_Previous() + "\"></div>";
 			}
 			replace = currentTextValue;
 			value = nav;
 		}
 
 		OptionSuggestion(String displ, String val, String replacePre,
 				String query) {
 			name = displ;
 			int begin = displ.toLowerCase().indexOf(query.toLowerCase());
 			if (begin >= 0) {
 				int end = begin + query.length();
 				String match = displ.substring(begin, end);
 				display = displ.replaceFirst(match, "<b>" + match + "</b>");
 			} else {
 				// may not necessarily be a part of the query, for example if
 				// "*" was typed.
 				display = displ;
 			}
 			replace = replacePre;
 			value = val;
 		}
 
 		@Override
 		public String getDisplayString() {
 			return display;
 		}
 
 		@Override
 		public String getReplacementString() {
 			return replace;
 		}
 
 		/**
 		 * Get the value of the option
 		 * 
 		 * @return value
 		 */
 		public String getValue() {
 			return value;
 		}
 
 		/**
 		 * Get the name of the option. (when not multivalued, this will be the
 		 * same as getReplacementString)
 		 * 
 		 * @return name
 		 */
 		public String getName() {
 			return name;
 		}
 	}
 
 	/**
 	 * An abstract class that handles success and error conditions from the REST
 	 * call
 	 */
 	public abstract class OptionQueryCallback {
 		abstract void success(AutoSuggestPresenterOptionResultSet optResults);
 
 		abstract void error(Throwable exception);
 	}
 }
