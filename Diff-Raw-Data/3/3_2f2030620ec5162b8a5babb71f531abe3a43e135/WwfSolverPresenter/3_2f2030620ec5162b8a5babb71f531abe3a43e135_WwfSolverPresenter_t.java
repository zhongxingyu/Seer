 package com.zylman.wwf.client;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.zylman.wwf.shared.Result;
 
 public class WwfSolverPresenter {
 	private final WwfSolveServiceAsync wwfSolveService = GWT.create(WwfSolveService.class);
 	private final WwfWordTestServiceAsync wwfWordTestService = GWT.create(WwfWordTestService.class);
 	
 	private final WwfSolverView view;
 	public WwfSolverPresenter(WwfSolverView view) {
 		this.view = view;
 		
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
         		view.test.setText(tokens.get(1));
         		testWord(tokens.get(1));
         	}
         } else if (tokens.get(0).equals("solve")) {
         	if(tokens.size() == 5) {
         		String rack = tokens.get(1);
         		String start = tokens.get(2).equals("!") ? "" : tokens.get(2);
         		String contains = tokens.get(3).equals("!") ? "" : tokens.get(3);
         		String end = tokens.get(4).equals("!") ? "" : tokens.get(4);
         		view.rack.setText(rack);
         		view.start.setText(start);
         		view.contains.setText(contains);
         		view.end.setText(end);
         		getAnagrams(rack, start, contains, end);
         	}
         }
       }
     };
 	}
 	
 	private void getAnagrams(String rack, String start, String contains, String end) {
 		// Set up the callback object.
 		AsyncCallback<Result> callback = new AsyncCallback<Result>() {
 			public void onFailure(Throwable caught) {
 				view.setError("failure!");
 			}
 
 			public void onSuccess(Result results) {
 				if (results.getError()) {
 					view.setError("Invalid query. You may only have letters and wildcard characters. "
 							+ "Wildcard characters may only be placed in the rack and there can be a maximum of 10 "
 							+ "characters.");
 				} else {
 					view.setError("");
 					view.setResults(results.getWords());
 				}
 			}
 		};
 		// Make the call to the solve service.
 		if (validateInput()) {
			view.setError("Loading...");
 			wwfSolveService.findAnagrams(rack, start, contains, end, callback);
 		}
 	}
 	
 	private boolean validateInput() {
 		return view.validateInput();
 	}
 	
 	private void testWord(final String word) {
 		AsyncCallback<Result> callback = new AsyncCallback<Result>() {
 			public void onFailure(Throwable caught) {
 				view.setError("failure!");
 			}
 
 			public void onSuccess(Result results) {
 				if (results.getError()) {
 					view.testResults.setText("That's not a word.");
 				} else {
 					view.testResults.setText(
 							word + " is worth " + results.getWords().get(0).getScore() + " points!");
 				}
 			}
 		};
 		wwfWordTestService.testWord(word, callback);
 	}
 	
 	public void setSolveHistory(String rack, String start, String contains, String end) {
 		History.newItem(
 				"solve/" + rack
 				+ "/" + (start.isEmpty() ? "!" : start)
 				+ "/" + (contains.isEmpty() ? "!" : contains)
 				+ "/" + (end.isEmpty() ? "!" : end));
 	}
 }
