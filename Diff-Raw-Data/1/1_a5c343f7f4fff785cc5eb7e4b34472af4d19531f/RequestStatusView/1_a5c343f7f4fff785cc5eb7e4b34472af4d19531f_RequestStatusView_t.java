 /**
  * Acceso Inteligente
  *
  * Copyright (C) 2010-2011 Fundación Ciudadano Inteligente
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.accesointeligente.client.views;
 
 import org.accesointeligente.client.presenters.RequestStatusPresenter;
 import org.accesointeligente.client.uihandlers.RequestStatusUiHandlers;
 import org.accesointeligente.model.RequestCategory;
 import org.accesointeligente.shared.RequestStatus;
 
 import com.gwtplatform.mvp.client.ViewWithUiHandlers;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.client.ui.*;
 
 import java.util.Date;
 import java.util.Iterator;
 import java.util.Set;
 
 public class RequestStatusView extends ViewWithUiHandlers<RequestStatusUiHandlers> implements RequestStatusPresenter.MyView {
 	private static RequestStatusViewUiBinder uiBinder = GWT.create(RequestStatusViewUiBinder.class);
 	interface RequestStatusViewUiBinder extends UiBinder<Widget, RequestStatusView> {}
 	private final Widget widget;
 
 	// UIFields
 	@UiField Image requestStatusHead;
 	@UiField Label requestTitleHead;
 	@UiField Label requestDate;
 	@UiField Label requestStatus;
 	@UiField Label institutionName;
 	@UiField Label requestInfo;
 	@UiField Label requestContext;
 	@UiField Label requestTitle;
 	@UiField FlowPanel requestCategoryPanel;
 	@UiField Button requestListLink;
 	@UiField Button editRequest;
 	@UiField Button deleteRequest;
 	@UiField Button requestListLinkBottom;
 	@UiField Button editRequestBottom;
 	@UiField Button deleteRequestBottom;
 
 	public RequestStatusView() {
 		widget = uiBinder.createAndBindUi(this);
 	}
 
 	@Override
 	public Widget asWidget() {
 		return widget;
 	}
 
 	@Override
 	public void setDate(Date date) {
 		if (date != null) {
 			requestDate.setText(DateTimeFormat.getFormat("dd/MM/yyyy HH:mm").format(date));
 		} else {
 			requestDate.setText("");
 		}
 	}
 
 	@Override
 	public void setStatus(RequestStatus status) {
 		requestStatus.setText(status.getName());
 		requestStatusHead.setUrl(status.getUrl());
 	}
 
 	@Override
 	public void setInstitutionName(String name) {
 		institutionName.setText(name);
 	}
 
 	@Override
 	public void setRequestInfo(String info) {
 		requestInfo.setText(info);
 	}
 
 	@Override
 	public void setRequestContext(String context) {
 		requestContext.setText(context);
 	}
 
 	@Override
 	public void setRequestTitle(String title) {
 		requestTitle.setText(title);
 		requestTitleHead.setText(title);
 	}
 
 	@Override
 	public void addRequestCategories(RequestCategory category) {
 		Label categoryLabel = new Label();
 		categoryLabel.setText(category.getName());
 		requestCategoryPanel.add(categoryLabel);
 	}
 
 	@Override
 	public void setRequestCategories(Set<RequestCategory> categories) {
		requestCategoryPanel.clear();
 		Iterator<RequestCategory> iterator = categories.iterator();
 
 		while (iterator.hasNext()) {
 			RequestCategory category = iterator.next();
 			addRequestCategories(category);
 		}
 	}
 
 	@UiHandler("editRequest")
 	public void onEditRequestClick(ClickEvent event) {
 		getUiHandlers().editRequest();
 	}
 
 	@UiHandler("deleteRequest")
 	public void onDeleteRequestClick(ClickEvent event) {
 		getUiHandlers().deleteRequest();
 	}
 
 	@UiHandler("requestListLink")
 	public void onRequestListLinkClick(ClickEvent event) {
 		getUiHandlers().confirmRequest();
 		getUiHandlers().gotoMyRequests();
 	}
 
 
 	@UiHandler("editRequestBottom")
 	public void onEditRequestBottomClick(ClickEvent event) {
 		getUiHandlers().editRequest();
 	}
 
 	@UiHandler("deleteRequestBottom")
 	public void onDeleteRequestBottomClick(ClickEvent event) {
 		getUiHandlers().deleteRequest();
 	}
 
 	@UiHandler("requestListLinkBottom")
 	public void onRequestListLinkBottomClick(ClickEvent event) {
 		getUiHandlers().confirmRequest();
 		getUiHandlers().gotoMyRequests();
 	}
 
 	@Override
 	public void editOptions(Boolean allowEdit) {
 		if (allowEdit == false) {
 			editRequest.setVisible(false);
 			deleteRequest.setVisible(false);
 			editRequestBottom.setVisible(false);
 			deleteRequestBottom.setVisible(false);
 		}
 	}
 }
