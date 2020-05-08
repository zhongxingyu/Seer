 package com.sfeir.common.gwt.client.mvp.historian;
 
 import static com.google.common.base.Strings.isNullOrEmpty;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.event.shared.GwtEvent;
 import com.google.gwt.event.shared.HandlerManager;
 import com.google.gwt.http.client.URL;
 
 public class Html5Historian extends ParameterHistorian implements
 // allows the use of ValueChangeEvent.fire()
 		HasValueChangeHandlers<String> {
 
 	private HandlerManager handlers = new HandlerManager(null);
 	private String currentToken = "";
 
 	public Html5Historian() {
 		initEvent();
 	}
 
 	@Override
 	public com.google.gwt.event.shared.HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> valueChangeHandler) {
 //		final com.google.gwt.event.shared.HandlerRegistration historyHandler = History.addValueChangeHandler(valueChangeHandler);
 		final com.google.gwt.event.shared.HandlerRegistration html5Handler = this.handlers.addHandler(ValueChangeEvent.getType(), valueChangeHandler);
 		return new com.google.gwt.event.shared.HandlerRegistration() {
 			
 			@Override
 			public void removeHandler() {
 //				historyHandler.removeHandler();
 				html5Handler.removeHandler();
 			}
 		};
 	}
 
 	@Override
 	public void newItem(String token, boolean issueEvent) {
 		String newUri = getTokenPath(token);
 		if (currentToken.equals(newUri)) { // not sure if this is needed, but just in case
 			return;
 		}
		currentToken = token;
 		pushState(token, newUri);
 		GWT.log("newItem " + token + " " + newUri);
 		if (issueEvent) {
 			ValueChangeEvent.fire(this, currentToken);
 		}
 	}
 
 	@Override
 	public void fireEvent(GwtEvent<?> event) {
 		this.handlers.fireEvent(event);
 	}
 
 	private native void initEvent() /*-{
 		var that = this;
 		var oldHandler = $wnd.onpopstate;
 		$wnd.onpopstate = $entry(function(e) {
 			if (e.state && e.state.historyToken) {
 				that.@com.sfeir.common.gwt.client.mvp.historian.Html5Historian::onPopState(Ljava/lang/String;)(e.state.historyToken);
 			}
 			else {
 				that.@com.sfeir.common.gwt.client.mvp.historian.Html5Historian::onPopState(Ljava/lang/String;)(null)
 			}
 			if (oldHandler) {
 				oldHandler(e);
 			}
 		});
 	}-*/;
 
 	private void onPopState(String historyToken) {
 		if (isNullOrEmpty(historyToken)) {
 			historyToken = getToken();
 		}
		GWT.log("onPopState " + historyToken);
 		if (!currentToken.equals(historyToken)) {
 			ValueChangeEvent.fire(this, historyToken);
 			currentToken = historyToken;
 		}
 	}
 
 	private native void pushState(String token, String url) /*-{
 		var state = {
 	      historyToken : token
 	    };
 		$wnd.history.pushState(state, $doc.title, url);
 	}-*/;
 
 	private native void replaceState(String token, String url) /*-{
 		var state = {
 	      historyToken : token
 	    };
 		$wnd.history.replaceState(state, $doc.title, url);
 	}-*/;
 
 	@Override
 	public void replaceToken(String token, boolean issueEvent) {
 		String newUri = getTokenPath(token);
		currentToken = token;
 		replaceState(token, newUri);
 		GWT.log("replaceToken " + token + " " + newUri);
 		if (issueEvent) {
 			ValueChangeEvent.fire(this, currentToken);
 		}
 	}
 
 	protected String getTokenPath(String token) {
 		return URL.encode(formater.getTokenPath(token));
 	}
 	
 	@Override
 	public String getToken() {
 		String token = "";
 		if (currentToken.isEmpty()) {
 			return super.getToken();
 		}
 		else {
 			token = currentToken;
 		}
 		GWT.log("getToken " + token);
 		return token;
 	}
 }
