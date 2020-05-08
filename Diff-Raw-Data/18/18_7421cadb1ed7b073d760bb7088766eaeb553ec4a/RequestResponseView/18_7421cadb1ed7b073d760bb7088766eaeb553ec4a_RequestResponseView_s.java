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
 
 import org.accesointeligente.client.presenters.RequestResponsePresenter;
 import org.accesointeligente.client.presenters.RequestResponsePresenterIface;
 import org.accesointeligente.client.widgets.CommentWidget;
 import org.accesointeligente.client.widgets.ResponseWidget;
 import org.accesointeligente.client.widgets.UserResponseWidget;
 import org.accesointeligente.model.RequestComment;
 import org.accesointeligente.model.Response;
 import org.accesointeligente.model.UserResponse;
 import org.accesointeligente.shared.AppPlace;
 import org.accesointeligente.shared.RequestStatus;
 
 import org.cobogw.gwt.user.client.ui.Rating;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.uibinder.client.*;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.ui.*;
 
 import java.util.Date;
 import java.util.List;
 
 public class RequestResponseView extends Composite implements RequestResponsePresenter.Display {
 	private static RequestResponseViewUiBinder uiBinder = GWT.create(RequestResponseViewUiBinder.class);
 	interface RequestResponseViewUiBinder extends UiBinder<Widget, RequestResponseView> {}
 
 	// Fields
 	@UiField HTMLPanel navigationPanel;
 	@UiField Anchor requestListLink;
 	@UiField Image requestStatus;
 	@UiField Label requestTitle;
 	@UiField Label requestDate;
 	@UiField Label commentCount;
 	@UiField Label institutionName;
 	@UiField Rating requestRate;
 	@UiField Label requestInfo;
 	@UiField Label requestContext;
 	@UiField FlowPanel responsePanel;
 	@UiField FlowPanel commentsPanel;
 	@UiField HTMLPanel newCommentPanel;
 	@UiField TextArea newCommentText;
 	@UiField Button newCommentSubmit;
 
 
 	private RequestResponsePresenterIface presenter;
 
 	public RequestResponseView() {
 		initWidget(uiBinder.createAndBindUi(this));
 	}
 
 	@Override
 	public void setPresenter(RequestResponsePresenterIface presenter) {
 		this.presenter = presenter;
 	}
 
 	@Override
 	public void setStatus(RequestStatus status) {
 		requestStatus.setUrl(status.getUrl());
 		requestStatus.setTitle(status.getName());
 	}
 
 	@Override
 	public void setRequestTitle(String title) {
 		requestTitle.setText(title);
 	}
 
 	@Override
 	public void setRequestDate(Date date) {
 		requestDate.setText(DateTimeFormat.getFormat("dd/MM/yyyy HH:mm").format(date));
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
 	public void setResponses(List<Response> responses) {
 		responsePanel.clear();
 
		for (Response response : responses) {
			ResponseWidget responseWidget = new ResponseWidget();
			responseWidget.setInfo(response.getInformation());
			responseWidget.setDate(response.getDate());
			presenter.loadAttachments(response, responseWidget);
			presenter.getUserResponse(response, responseWidget);
			responsePanel.add(responseWidget);
 		}
 	}
 
 	@Override
 	public void setComments(List<RequestComment> comments) {
 		commentsPanel.clear();
 		for (RequestComment comment : comments) {
 			CommentWidget commentWidget = new CommentWidget();
 			commentWidget.setImage("");
 			commentWidget.setAuthor(comment.getUser().getFirstName());
 			commentWidget.setDate(comment.getDate());
 			commentWidget.setContent(comment.getText());
 			commentsPanel.add(commentWidget);
 		}
 		commentCount.setText(new Integer(comments.size()).toString());
 	}
 
 	@Override
 	public void showNewCommentPanel() {
 		newCommentPanel.setVisible(true);
 	}
 
 	@Override
 	public void cleanNewCommentText() {
 		newCommentText.setText("");
 	}
 
 	@Override
 	public void setRatingValue(Integer rate) {
 		requestRate.setValue(rate);
 	}
 
 	@Override
 	public void setRatingReadOnly(Boolean readOnly) {
 		requestRate.setReadOnly(readOnly);
 	}
 
 	@Override
 	public void clearResponseWidget(ResponseWidget widget) {
 		widget.clearUserResponsePanel();
 	}
 
 	@Override
 	public void setUserResponse(UserResponse userResponse, ResponseWidget widget) {
 		UserResponseWidget userResponseWidget = new UserResponseWidget();
 		userResponseWidget.setInformation(userResponse.getInformation());
 		userResponseWidget.setDate(userResponse.getDate());
 		widget.add(userResponseWidget);
 	}
 
 	@Override
 	public void newUserResponse(final Response response, final ResponseWidget widget) {
 		FlowPanel userResponsePanel = new FlowPanel();
 		final TextArea userResponseTextBox = new TextArea();
 		Button userResponseButton = new Button("Responder", new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				presenter.saveUserResponse(userResponseTextBox.getText(), response, widget);
 			}
 		});
 
 		userResponsePanel.addStyleName("userResponsePanel");
 		userResponseTextBox.addStyleName("newUserResponse");
 		userResponseButton.addStyleName("userResponseButton");
 
 		userResponsePanel.add(userResponseTextBox);
 		userResponsePanel.add(userResponseButton);
 		widget.add(userResponsePanel);
 	}
 
 	@UiHandler("requestListLink")
 	public void onRequestListLinkClick(ClickEvent event) {
 		String link = presenter.getListLink();
 		if (link == null) {
 			link = AppPlace.HOME.getToken();
 		}
 
 		History.newItem(link);
 	}
 
 	@UiHandler("newCommentSubmit")
 	public void onNewCommentClick(ClickEvent event) {
 		presenter.saveComment(newCommentText.getText());
 	}
 
 	@UiHandler("requestRate")
 	public void onRequestRateClick(ClickEvent event) {
 		presenter.saveQualification(requestRate.getValue());
 	}
 
 	@UiFactory
 	public Rating rateBuilder() {
 		return new Rating(0,5);
 	}
 }
