 /*
  * LoadingStatusWidget.java
  * Copyright (C) 2011 Meyer Kizner
  * All rights reserved.
  */
 
 package com.prealpha.extempdb.client.taginput;
 
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.HasValue;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.inject.Inject;
 
final class LoadingStatusWidget extends Composite implements
 		HasValue<LoadingStatus> {
 	public static interface LoadingStatusUiBinder extends
 			UiBinder<Widget, LoadingStatusWidget> {
 	}
 
 	@UiField
 	Image image;
 
 	private LoadingStatus loadingStatus;
 
 	@Inject
	public LoadingStatusWidget(LoadingStatusUiBinder uiBinder) {
 		setValue(LoadingStatus.NONE);
 		initWidget(uiBinder.createAndBindUi(this));
 	}
 
 	@Override
 	public LoadingStatus getValue() {
 		return loadingStatus;
 	}
 
 	@Override
 	public void setValue(LoadingStatus value) {
 		setValue(value, false);
 	}
 
 	@Override
 	public void setValue(LoadingStatus value, boolean fireEvents) {
 		LoadingStatus oldValue = loadingStatus;
 		loadingStatus = value;
 		if (fireEvents) {
 			ValueChangeEvent.fireIfNotEqual(this, oldValue, value);
 		}
 	}
 
 	@Override
 	public HandlerRegistration addValueChangeHandler(
 			ValueChangeHandler<LoadingStatus> handler) {
 		return addHandler(handler, ValueChangeEvent.getType());
 	}
 }
