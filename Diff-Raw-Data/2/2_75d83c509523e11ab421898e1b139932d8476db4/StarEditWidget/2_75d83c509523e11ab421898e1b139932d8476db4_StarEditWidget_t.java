 package com.scurab.web.drifmaps.client.widget;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.TextArea;
 import com.google.gwt.user.client.ui.Widget;
 import com.scurab.web.drifmaps.client.DataService;
 import com.scurab.web.drifmaps.client.DataServiceAsync;
 import com.scurab.web.drifmaps.client.DrifMaps;
 import com.scurab.web.drifmaps.client.dialog.NotificationDialog;
 import com.scurab.web.drifmaps.shared.datamodel.Star;
 
 public class StarEditWidget extends Composite
 {
 
 	private static StarEditWidgetUiBinder uiBinder = GWT.create(StarEditWidgetUiBinder.class);
 
 	interface StarEditWidgetUiBinder extends UiBinder<Widget, StarEditWidget>
 	{
 	}
 	
 	public interface OnSavedListener
 	{
 		void onSave();
 		void onDelete(Star s);
 	}
 	
 	@UiField TextArea txtNote;
 	@UiField Button btnSave;
 	@UiField Button btnDelete;
 	private Star mStar = null;
 	private OnSavedListener listener = null;
 	
 	private DataServiceAsync mDataService = null;
 	
 	public StarEditWidget(Star s, DataServiceAsync dse,OnSavedListener listener)
 	{
 		initWidget(uiBinder.createAndBindUi(this));
 		this.listener = listener;
 		mStar = s;
 		mDataService = dse;
 		txtNote.setText(s.getNote());
 		btnSave.addClickHandler(mUpdateClickHandler);
 		btnDelete.addClickHandler(mDeleteClickHandler);
 	}
 	
 	private ClickHandler mUpdateClickHandler = new ClickHandler()
 	{
 		@Override
 		public void onClick(ClickEvent event)
 		{		
 			String t = txtNote.getText();
 			setButtonsEnabled(false);
 			btnSave.setText(DrifMaps.Words.Saving());
 			mStar.setNote(t);
 			mDataService.processStar(mStar, DataService.UPDATE, new AsyncCallback<Star>()
 			{
 				@Override
 				public void onSuccess(Star result)
 				{
 					setButtonsEnabled(true);
 					btnSave.setText(DrifMaps.Words.Save());
 					listener.onSave();
 				}
 				
 				@Override
 				public void onFailure(Throwable caught)
 				{
 					setButtonsEnabled(true);
 					NotificationDialog.show(caught);
 				}
 			});
 		}
 	};
 	
 	private void setButtonsEnabled(boolean enabled)
 	{
 		btnSave.setEnabled(enabled);
 		btnDelete.setEnabled(enabled);
 	}
 	
 	private ClickHandler mDeleteClickHandler = new ClickHandler()
 	{
 		@Override
 		public void onClick(ClickEvent event)
 		{
 			setButtonsEnabled(false);
 			btnDelete.setText(DrifMaps.Words.Deleting());
 			mDataService.processStar(mStar, DataService.DELETE, new AsyncCallback<Star>()
 			{
 				@Override
 				public void onSuccess(Star result)
 				{
 					setButtonsEnabled(true);
 					btnDelete.setText(DrifMaps.Words.Delete());
 					listener.onDelete(mStar);
 				}
 				
 				@Override
 				public void onFailure(Throwable caught)
 				{
 					setButtonsEnabled(true);
 					NotificationDialog.show(caught);
 				}
 			});
 		}
 	};
 }
