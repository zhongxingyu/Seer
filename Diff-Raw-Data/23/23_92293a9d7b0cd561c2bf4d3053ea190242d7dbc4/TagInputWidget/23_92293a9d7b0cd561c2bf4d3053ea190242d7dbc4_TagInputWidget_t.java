 /*
  * TagInputWidget.java
  * Copyright (C) 2011 Meyer Kizner
  * All rights reserved.
  */
 
 package com.prealpha.extempdb.client.taginput;
 
 import com.google.gwt.event.dom.client.KeyPressEvent;
 import com.google.gwt.event.logical.shared.SelectionEvent;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.SuggestBox;
 import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.inject.Inject;
 import com.prealpha.dispatch.shared.DispatcherAsync;
 import com.prealpha.extempdb.client.error.ManagedCallback;
 import com.prealpha.extempdb.shared.action.GetTag;
 import com.prealpha.extempdb.shared.action.GetTagResult;
 import com.prealpha.extempdb.shared.dto.TagDto;
 
 public final class TagInputWidget extends Composite implements TagSelector {
	public static interface TagInputUiBinder extends
			UiBinder<Widget, TagInputWidget> {
 	}
 
 	@UiField(provided = true)
 	final SuggestBox nameBox;
 
 	@UiField(provided = true)
 	final LoadingStatusWidget statusWidget;
 
 	private final DispatcherAsync dispatcher;
 
 	private TagDto tag;
 
 	@Inject
 	private TagInputWidget(TagInputUiBinder uiBinder,
 			DispatcherAsync dispatcher, SuggestBox nameBox,
 			LoadingStatusWidget statusWidget) {
 		this.dispatcher = dispatcher;
 		this.nameBox = nameBox;
 		this.statusWidget = statusWidget;
 		initWidget(uiBinder.createAndBindUi(this));
 	}
 
 	@Override
 	public TagDto getValue() {
 		return tag;
 	}
 
 	@Override
 	public void setValue(TagDto value) {
 		setValue(value, false);
 	}
 
 	@Override
 	public void setValue(TagDto value, boolean fireEvents) {
 		TagDto oldValue = tag;
 		tag = value;
 		nameBox.setText((tag == null) ? null : tag.getName());
 		if (fireEvents) {
 			ValueChangeEvent.fireIfNotEqual(this, oldValue, value);
 		}
 	}
 
 	@Override
 	public HandlerRegistration addValueChangeHandler(
 			ValueChangeHandler<TagDto> handler) {
 		return addHandler(handler, ValueChangeEvent.getType());
 	}
 
 	@Override
 	public String getSelectedName() {
 		return nameBox.getValue();
 	}
 
 	@UiHandler("nameBox")
 	void onKeyPress(KeyPressEvent event) {
 		requestTag(nameBox.getValue());
 	}
 
 	@UiHandler("nameBox")
 	void onSelection(SelectionEvent<Suggestion> event) {
 		requestTag(event.getSelectedItem().getReplacementString());
 	}
 
 	private void requestTag(String tagName) {
 		GetTag action = new GetTag(tagName);
 		dispatcher.execute(action, new TagCallback(tagName));
 		statusWidget.setValue(LoadingStatus.PENDING);
 		setValue(null, true);
 	}
 
 	private final class TagCallback extends ManagedCallback<GetTagResult> {
 		private final String expectedName;
 
 		private TagCallback(String expectedName) {
 			if (expectedName == null || expectedName.isEmpty()) {
 				this.expectedName = null;
 			} else {
 				this.expectedName = expectedName;
 			}
 		}
 
 		@Override
 		public void onSuccess(GetTagResult result) {
 			// this result may be obsolete already
 			if (nameBox.getValue() == null) {
 				if (expectedName != null) {
 					return;
 				}
 			} else if (!nameBox.getValue().equals(expectedName)) {
 				return;
 			}
 
 			TagDto tag = result.getTag();
 			if (tag == null) {
 				if (expectedName == null) {
 					statusWidget.setValue(LoadingStatus.NONE);
 				} else {
 					statusWidget.setValue(LoadingStatus.NOT_FOUND);
 				}
 			} else {
 				statusWidget.setValue(LoadingStatus.LOADED, true);
 			}
 			setValue(tag, true);
 		}
 	}
 }
