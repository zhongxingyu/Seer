 /**
  * 
  */
 package com.google.gwt.user.client.ui;
 
 import org.cotrix.web.common.client.resources.CommonResources;
 
 import com.allen_sauer.gwt.log.client.Log;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.logical.shared.HasSelectionHandlers;
 import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
 import com.google.gwt.event.logical.shared.SelectionHandler;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.user.client.TakesValue;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.SuggestBox;
 import com.google.gwt.user.client.ui.SuggestOracle;
 import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
 import com.google.gwt.user.client.ui.ValueBoxBase;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public class SuggestListBox extends Composite implements HasValueChangeHandlers<String>, HasSelectionHandlers<SuggestOracle.Suggestion>, TakesValue<String> {
 	
 	private HorizontalPanel mainPanel;
 	private SuggestBox suggestBox;
 	private Button suggestButton;
 	
 	public SuggestListBox(SuggestOracle oracle) {
 		mainPanel = new HorizontalPanel();
 		mainPanel.setWidth("100%");
 		mainPanel.setStyleName(CommonResources.INSTANCE.css().listBox());
 		
 		/*
 		 * border-radius: 5px;
 background-color: #fafafa;
 box-shadow: 0 0 0 0;
 border: 1px solid #999;
 		 */
 		
 		
 		suggestBox = new SuggestBox(new SuggestOracleProxy(oracle));
 		suggestBox.setWidth("100%");
 		suggestBox.setStyleName(CommonResources.INSTANCE.css().sugestionListBoxTextBox());
 		mainPanel.add(suggestBox);
 		
 		/*
 		 * border: none;
 		 */
 		
 		suggestButton = new Button();
 		suggestButton.setStyleName(CommonResources.INSTANCE.css().sugestionListBoxButton());
 		mainPanel.add(suggestButton);
 		//mainPanel.setCellWidth(suggestButton, "20px");
 		
 		/*
 		 * width: 19px;
 height: 27px;
 border: none;
 background: white;
 -webkit-appearance: menulist;
 vertical-align: middle;
 		 */
 		
 		suggestButton.addClickHandler(new ClickHandler() {
 			
 			@Override
 			public void onClick(ClickEvent event) {
 				Log.trace("show suggestions");
 				suggestBox.showSuggestions("");
 				
 			}
 		});
 		initWidget(mainPanel);
 	}
 	
 	public void setBoxStyle(String name) {
 		suggestBox.setStyleName(name);
 	}
 	
 	private class SuggestOracleProxy extends SuggestOracle {
 		
 		private SuggestOracle proxed;
 
 		/**
 		 * @param proxed
 		 */
 		public SuggestOracleProxy(SuggestOracle proxed) {
 			this.proxed = proxed;
 		}
 
 		/** 
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void requestDefaultSuggestions(Request request, Callback callback) {
 			requestSuggestions(new Request("", request.getLimit()), callback);
 		}
 
 		@Override
 		public void requestSuggestions(Request request, Callback callback) {
 			proxed.requestSuggestions(request, callback);
 		}
 		
 	}
 
 	@Override
 	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
 		return suggestBox.addValueChangeHandler(handler);
 	}
 
 	@Override
 	public HandlerRegistration addSelectionHandler(SelectionHandler<Suggestion> handler) {
 		return suggestBox.addSelectionHandler(handler);
 	}
 
 	public ValueBoxBase<String> getValueBox() {
 		return suggestBox.getValueBox();
 	}
 
 	@Override
 	public void setValue(String value) {
 		suggestBox.setValue(value);
 	}
 
 	@Override
 	public String getValue() {
 		return suggestBox.getValue();
 	}
 	
 
 }
