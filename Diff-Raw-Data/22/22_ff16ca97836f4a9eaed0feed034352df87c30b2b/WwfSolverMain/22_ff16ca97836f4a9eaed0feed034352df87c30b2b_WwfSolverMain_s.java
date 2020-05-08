 package com.zylman.wwf.client;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.List;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.event.dom.client.KeyUpEvent;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.cellview.client.CellTable;
 import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
 import com.google.gwt.user.cellview.client.TextColumn;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.RootLayoutPanel;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.view.client.ListDataProvider;
 import com.zylman.wwf.shared.InputValidator;
 import com.zylman.wwf.shared.Result;
 import com.zylman.wwf.shared.SolveResult;
 
 /**
  * Entry point classes define <code>onModuleLoad()</code>.
  */
 public class WwfSolverMain implements EntryPoint {
 	interface Binder extends UiBinder<Widget, WwfSolverMain> { }
 	private static final Binder binder = GWT.create(Binder.class);
 
 	@UiField TextBox rack;
 	@UiField TextBox start;
 	@UiField TextBox contains;
 	@UiField TextBox end;
 	@UiField Button sendButton;
 	@UiField Label errorLabel;
 	@UiField CellTable<SolveResult> results;
 	@UiField TextBox test;
 	@UiField Label testResults;
 
 	private final WwfSolveServiceAsync wwfSolveService = GWT.create(WwfSolveService.class);
 	private final WwfWordTestServiceAsync wwfWordTestService = GWT.create(WwfWordTestService.class);
 	private final ListDataProvider<SolveResult> dataProvider = new ListDataProvider<SolveResult>();
 	private final List<SolveResult> resultList = dataProvider.getList();
 
 	/**
 	 * This is the entry point method.
 	 */
 	public void onModuleLoad() {
 		Widget ui = binder.createAndBindUi(this);
 		
 		// Focus the cursor on the name field when the app loads
 		rack.setFocus(true);
 		sendButton.setWidth("100px");
 
 		RootLayoutPanel.get().add(ui);
 		initializeColumns();
 		
     History.addValueChangeHandler(getHistoryHandler());
     
     History.fireCurrentHistoryState();
 	}
 	
 	private ValueChangeHandler<String> getHistoryHandler() {
 		return new ValueChangeHandler<String>() {
       public void onValueChange(ValueChangeEvent<String> event) {
         String historyToken = event.getValue();
         ArrayList<String> tokens = new ArrayList<String>(Arrays.asList(historyToken.split("/")));
         if (tokens.get(0).equals("test")) {
         	if(tokens.size() == 2) {
         		test.setText(tokens.get(1));
         		testWord(test.getText());
         	}
         } else if (tokens.get(0).equals("solve")) {
         	if(tokens.size() == 5) {
         		rack.setText(tokens.get(1));
         		if (!tokens.get(2).equals("!")) {
         			start.setText(tokens.get(2));
         		}
         		if (!tokens.get(3).equals("!")) {
         			contains.setText(tokens.get(3));
         		}
         		if (!tokens.get(4).equals("!")) {
         			end.setText(tokens.get(4));
         		}
         		getAnagrams(rack.getText(), start.getText(), contains.getText(), end.getText());
         	}
         }
       }
     };
 	}
 	
 	private void initializeColumns() {
 		TextColumn<SolveResult> wordColumn = new TextColumn<SolveResult>() {
 			@Override public String getValue(SolveResult object) {
 				return object.getWord();
 			}
 		};
 		TextColumn<SolveResult> scoreColumn = new TextColumn<SolveResult>() {
 			@Override public String getValue(SolveResult object) {
 				return object.getScore().toString();
 			}
 		};
 		TextColumn<SolveResult> lengthColumn = new TextColumn<SolveResult>() {
 			@Override public String getValue(SolveResult object) {
 				return object.getLength().toString();
 			}
 		};
 		wordColumn.setSortable(true);
 		scoreColumn.setSortable(true);
 		lengthColumn.setSortable(true);
 		results.addColumn(wordColumn, "Word");
 		results.addColumn(lengthColumn, "Length");
 		results.addColumn(scoreColumn, "Score");
 		// Add a ColumnSortEvent.ListHandler to connect sorting to the
 		// java.util.List.
 		ListHandler<SolveResult> wordColumnSortHandler = new ListHandler<SolveResult>(resultList);
 		wordColumnSortHandler.setComparator(wordColumn, new Comparator<SolveResult>() {
 			public int compare(SolveResult o1, SolveResult o2) {
 				if (o1 == o2) {
 					return 0;
 				}
 				// Compare the name columns.
 				if (o1 != null) {
 					return (o2 != null) ? o1.getWord().compareTo(o2.getWord()) : 1;
 				}
 				return -1;
 			}
 		});
 		ListHandler<SolveResult> lengthColumnSortHandler = new ListHandler<SolveResult>(resultList);
 		lengthColumnSortHandler.setComparator(lengthColumn, new Comparator<SolveResult>() {
 			public int compare(SolveResult o1, SolveResult o2) {
 				if (o1 == o2) {
 					return 0;
 				}
 				// Compare the name columns.
 				if (o1 != null) {
 					return (o2 != null) ? o1.getLength().compareTo(o2.getLength()) : 1;
 				}
 				return -1;
 			}
 		});
 		ListHandler<SolveResult> scoreColumnSortHandler = new ListHandler<SolveResult>(resultList);
 		scoreColumnSortHandler.setComparator(scoreColumn, new Comparator<SolveResult>() {
 			public int compare(SolveResult o1, SolveResult o2) {
 				if (o1 == o2) {
 					return 0;
 				}
 				// Compare the name columns.
 				if (o1 != null) {
 					return (o2 != null) ? o1.getScore().compareTo(o2.getScore()) : 1;
 				}
 				return -1;
 			}
 		});
 
 		results.addColumnSortHandler(wordColumnSortHandler);
 		results.addColumnSortHandler(lengthColumnSortHandler);
 		results.addColumnSortHandler(scoreColumnSortHandler);
 		results.getColumnSortList().push(scoreColumn);
 		results.setPageSize(999999);
 		
 		dataProvider.addDataDisplay(results);
 	}
 
 	private void setSolveHistory() {
 		History.newItem(
 				"solve/" + rack.getText()
 				+ "/" + (start.getText().isEmpty() ? "!" : start.getText())
 				+ "/" + (contains.getText().isEmpty() ? "!" : contains.getText())
 				+ "/" + (end.getText().isEmpty() ? "!" : end.getText()));
 	}
 	
 	@UiHandler("sendButton")
 	void handleClick(ClickEvent e) {
 		setSolveHistory();
 	}
 
 	@UiHandler("rack")
 	void handleRackKeyUp(KeyUpEvent event) {
		if (validateInput() && event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
 			setSolveHistory();
 		}
 	}
 	
 	@UiHandler("start")
 	void handleStartKeyUp(KeyUpEvent event) {
		if (validateInput() && event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
 			setSolveHistory();
 		}
 	}
 	
 	@UiHandler("contains")
 	void handleContainsKeyUp(KeyUpEvent event) {
		if (validateInput() && event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
 			setSolveHistory();
 		}
 	}
 	
 	@UiHandler("end")
 	void handleEndKeyUp(KeyUpEvent event) {
		if (validateInput() && event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
 			setSolveHistory();
 		}
 	}
 
 	@UiHandler("test")
 	void handleTestKeyUp(KeyUpEvent event) {
 		if (test.getText().isEmpty()) {
 			testResults.setText("");
 		} else {
 			errorLabel.setText("");
 			testResults.setText("");
 		}
 		History.newItem("test/" + test.getText());
 	}
 	
 	private boolean validateInput() {
 		if (!InputValidator.validateRack(rack.getText())) {
 			errorLabel.setText("Invalid query. The rack may only contain letters and up to two wildcards,"
 					+ " to a maximum of 10 characters.");
 			return false;
 		} else if (
 					!InputValidator.validateOther(start.getText())
 					|| !InputValidator.validateOther(contains.getText())
 					|| !InputValidator.validateOther(end.getText())) {
 			errorLabel.setText("Invalid query. The requirement fields may only contain letters.");
 			return false;
 		}
 		errorLabel.setText("");
 		return true;
 	}
 
 	private void getAnagrams(
 			final String rack, final String start, final String contains, final String end) {
 		// Set up the callback object.
 		errorLabel.setText("Loading...");
 		AsyncCallback<Result> callback = new AsyncCallback<Result>() {
 			public void onFailure(Throwable caught) {
 				errorLabel.setText("failure!");
 			}
 
 			public void onSuccess(Result results) {
 				if (results.getError()) {
 					errorLabel.setText("Invalid query. You may only have letters and wildcard characters. "
 							+ "Wildcard characters may only be placed in the rack and there can be a maximum of 10 "
 							+ "characters.");
 				} else {
 					errorLabel.setText("");
 					resultList.clear();
 					resultList.addAll(results.getWords());
 				}
 			}
 		};
 		// Make the call to the solve service.
		if (validateInput() && rack.length() > 1) {
 			wwfSolveService.findAnagrams(rack, start, contains, end, callback);
 		}
 	}
 
 	private void testWord(final String word) {
 		AsyncCallback<Result> callback = new AsyncCallback<Result>() {
 			public void onFailure(Throwable caught) {
 				errorLabel.setText("failure!");
 			}
 
 			public void onSuccess(Result results) {
 				if (results.getError()) {
 					testResults.setText("That's not a word.");
 				} else {
 					testResults.setText(
 							word + " is worth " + results.getWords().get(0).getScore() + " points!");
 				}
 			}
 		};
 		wwfWordTestService.testWord(word, callback);
 	}
 }
