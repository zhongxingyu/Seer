 /*
  * TagInputSuggestOracle.java
  * Copyright (C) 2011 Meyer Kizner
  * All rights reserved.
  */
 
 package com.prealpha.extempdb.client.taginput;
 
 import static com.google.common.base.Preconditions.*;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import com.google.gwt.user.client.ui.SuggestOracle;
 import com.google.inject.Inject;
 import com.prealpha.dispatch.shared.DispatcherAsync;
 import com.prealpha.extempdb.client.error.ManagedCallback;
 import com.prealpha.extempdb.shared.action.GetTagSuggestions;
 import com.prealpha.extempdb.shared.action.GetTagSuggestionsResult;
 
public final class TagInputSuggestOracle extends SuggestOracle {
 	private final DispatcherAsync dispatcher;
 
 	private String lastPrefix;
 
 	@Inject
	private TagInputSuggestOracle(DispatcherAsync dispatcher) {
 		this.dispatcher = dispatcher;
 	}
 
 	@Override
 	public void requestSuggestions(Request request, Callback callback) {
 		lastPrefix = request.getQuery();
 		GetTagSuggestions action = new GetTagSuggestions(lastPrefix,
 				request.getLimit());
 		dispatcher.execute(action, new SuggestionCallback(request, callback));
 	}
 
 	private class SuggestionCallback extends
 			ManagedCallback<GetTagSuggestionsResult> {
 		private final Request request;
 
 		private final Callback callback;
 
 		public SuggestionCallback(Request request, Callback callback) {
 			this.request = request;
 			this.callback = callback;
 		}
 
 		@Override
 		public void onSuccess(GetTagSuggestionsResult result) {
 			String requestPrefix = request.getQuery();
 			if (!requestPrefix.equals(lastPrefix)) {
 				// this result is already out of date
 				return;
 			}
 
 			Set<String> tagNames = result.getSuggestions();
 			Set<Suggestion> suggestions = new HashSet<Suggestion>();
 
 			for (String tagName : tagNames) {
 				suggestions.add(new TagSuggestion(tagName));
 			}
 
 			Response response = new Response(suggestions);
 			callback.onSuggestionsReady(request, response);
 		}
 	}
 
 	private static class TagSuggestion implements Suggestion {
 		private final String tagName;
 
 		public TagSuggestion(String tagName) {
 			checkNotNull(tagName);
 			this.tagName = tagName;
 		}
 
 		@Override
 		public String getDisplayString() {
 			return tagName;
 		}
 
 		@Override
 		public String getReplacementString() {
 			return tagName;
 		}
 	}
 }
