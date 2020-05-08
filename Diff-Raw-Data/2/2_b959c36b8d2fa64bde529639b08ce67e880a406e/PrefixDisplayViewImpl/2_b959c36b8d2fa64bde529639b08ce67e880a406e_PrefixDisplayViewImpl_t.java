 /*
  *  Copyright 2012 Axel Winkler, Daniel Dunér
  * 
  *  This file is part of Daxplore Presenter.
  *
  *  Daxplore Presenter is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU Lesser General Public License as published by
  *  the Free Software Foundation, either version 2.1 of the License, or
  *  (at your option) any later version.
  *
  *  Daxplore Presenter is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Lesser General Public License for more details.
  *
  *  You should have received a copy of the GNU Lesser General Public License
  *  along with Daxplore Presenter.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.daxplore.presenter.admin.view;
 
 import javax.servlet.http.HttpServletResponse;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.uibinder.client.UiTemplate;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Anchor;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.DialogBox;
 import com.google.gwt.user.client.ui.FileUpload;
 import com.google.gwt.user.client.ui.FormPanel;
 import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
 import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
 import com.google.gwt.user.client.ui.Hidden;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.Widget;
 
 public class PrefixDisplayViewImpl extends Composite implements PrefixDisplayView {
 	@UiTemplate("PrefixDisplayViewImpl.ui.xml")
 	interface PrefixDisplayViewPanel extends UiBinder<Widget, PrefixDisplayViewImpl> {}
 	private static PrefixDisplayViewPanel uiBinder = GWT.create(PrefixDisplayViewPanel.class);
 	
 	@UiField protected Anchor prefixHeader;
 	@UiField protected Anchor mainlink;
 	@UiField protected Label something; //TODO add relevant statistics
 	
 	@UiField protected FormPanel settingsForm;
 	@UiField protected Hidden settingsPrefix;
 	@UiField protected TextBox gaIDField;
 	@UiField protected Button settingsButton;
 	
 	@UiField protected FormPanel uploadForm;
 	@UiField protected FileUpload uploadWidget;
 	@UiField protected Hidden uploadPrefix;
 	@UiField protected Button uploadButton;
 	@UiField protected DialogBox uploadDialog;
 	@UiField protected Label uploadDialogText;
 	@UiField protected Button uploadDialogButton;
 	
 	@UiField protected Label deletePrefixLabel;
 	@UiField protected Button deleteButton;
 
 	public PrefixDisplayViewImpl() {
 		initWidget(uiBinder.createAndBindUi(this));
 		
 		mainlink.setTarget("_blank");
 		
 		settingsForm.setAction("/admin/settings");
 		settingsForm.setEncoding(FormPanel.ENCODING_URLENCODED);
 		settingsForm.setMethod(FormPanel.METHOD_POST);
 		
 		uploadForm.setAction("/admin/upload");
 		uploadForm.setEncoding(FormPanel.ENCODING_MULTIPART);
 		uploadForm.setMethod(FormPanel.METHOD_POST);
 		
 		uploadDialog.setVisible(false);
 		uploadDialog.setModal(true);
 		uploadDialog.setGlassEnabled(true);
 	}
 	
 	@UiHandler("settingsButton")
 	protected void settingsClick(ClickEvent e) {
 		settingsForm.submit();
 	}
 
 	@UiHandler("uploadButton")
 	protected void uploadClick(ClickEvent e) {
 		boolean doUpload = Window.confirm("Are you sure you want to upload a new file, replacing all old data?");
 		if (doUpload) {
 			uploadForm.submit();
 		}
 	}
 
 	@UiHandler("uploadForm")
 	protected void uploadSubmit(SubmitEvent event) {
 	}
 
 	@UiHandler("uploadForm")
 	protected void uploadSubmitComplete(SubmitCompleteEvent event) {
 		int status = -1;
 		if(event.getResults() != null) {
 			status = Integer.parseInt(event.getResults());
 		}
 		
 		if(status!=HttpServletResponse.SC_OK){
 			uploadDialogText.setText("Error code given by server: " + status + ". Please try again.");
 			uploadDialog.center();
 		}
 	}
 	
 	@UiHandler("uploadDialogButton")
 	protected void onClick(ClickEvent event) {
		uploadDialog.hide();
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void setPrefix(String prefix) {
 		prefixHeader.setText(prefix);
 		deletePrefixLabel.setText("Delete prefix " + prefix + ":");
 		deleteButton.setText("Delete " + prefix);
 		uploadPrefix.setValue(prefix);
 		settingsPrefix.setValue(prefix);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void setPrefixHref(String href) {
 		prefixHeader.setHref(href);
 		mainlink.setText(href);
 		mainlink.setHref(href);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void setStatDataItemCount(String something) {
 		this.something.setText(something);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void addDeletePrefixClickHandler(ClickHandler handler) {
 		deleteButton.addClickHandler(handler);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean promptDeleteConfirmation(String prefix) {
 		return Window.confirm("Are you sure you want to delete the prefix '"
 				+ prefix + "'.\n\nThis will also remove all data and information that belongs to it.");
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void setGoogleAnalyticsID(String gaID) {
 		gaIDField.setText(gaID);
 	}
 }
