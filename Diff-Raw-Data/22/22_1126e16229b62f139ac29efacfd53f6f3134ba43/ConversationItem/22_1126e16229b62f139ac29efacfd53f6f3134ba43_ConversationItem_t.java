 package org.palaso.languageforge.client.lex.controls.conversation;
 
 
 import java.util.Date;
 
 import org.palaso.languageforge.client.lex.common.MessageFormat;
 import org.palaso.languageforge.client.lex.controls.ExtendedCheckBox;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.HasClickHandlers;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.ComplexPanel;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.RichTextArea;
 import com.google.gwt.user.client.ui.SimplePanel;
 
 public class ConversationItem extends ComplexPanel {
 
 	private boolean isRootComment = false;
 	private boolean isNewComment = false;
 	private boolean isMarked = false;
 	private String refGuid = "";
 	/*
 	 * a comment has follow parts - header - left check mark - comment container
 	 * - footer + buttons
 	 */
 
 	private SimplePanel panelHeader = null;
 	private SimplePanel panelLeftCheckMarker = null;
 	private SimplePanel panelCommentContainer = null;
 	private SimplePanel panelFooter = null;
 	private RichTextArea commentTextbox = null;
 	private Label footerLabel = null;
 	private Button btnExpendAllReplies = null;
 	private ExtendedCheckBox chkResolved = null;
	private Button btnAddComment = null;
 	private static String imgGreenChecked;
 	private ConversationControl conversationControl = null;
 	
 	
 	static {
 		imgGreenChecked =  GWT.getModuleBaseURL() +"/images/ConversationControlGreenChecked.png";
 	}
 
 	protected ConversationItem() {
 		this.isNewComment = true;
 		setElement(DOM.createElement("LI"));
 		setStyleName("conversation-sub-panel");
 		setStyleName("conversation-new", true);
 		buildStructure();
 	}
 
 	public ConversationItem(String guid, boolean isRootComment) {
 		this.isRootComment = isRootComment;
 		this.refGuid = guid;
 		setElement(DOM.createElement("LI"));
 		setStyleName("conversation-sub-panel");
 
 		if (this.isRootComment) {
 			setStyleName("conversation-root", true);
 		}
 		buildStructure();
 	}
 
	protected HasClickHandlers getAddCommentButtonClickHandlers() {
		return btnAddComment;
 	}
 
 	protected HasClickHandlers getExpendedClickHandlers() {
 		return btnExpendAllReplies;
 	}
 
 	public void setFooterText(String text) {
 		footerLabel.setText(text);
 	}
 
 	@SuppressWarnings("deprecation")
 	public void setFooterTextbyDatetimeAndUser(Date date, String userName) {
 		String timeString = "";
 		Date currentDatetime = new Date();
 
 		if (currentDatetime.getYear() > date.getYear()) {
 			int valueDiff = currentDatetime.getYear() - date.getYear();
 
 			String isPlural = valueDiff > 1 ? "years" : "year";
 
 			timeString = MessageFormat.format("{0} {1} ago", valueDiff,
 					isPlural);
 		} else if (currentDatetime.getMonth() > date.getMonth()) {
 			int valueDiff = currentDatetime.getMonth() - date.getMonth();
 
 			String isPlural = valueDiff > 1 ? "months" : "month";
 
 			timeString = MessageFormat.format("{0} {1} ago", valueDiff,
 					isPlural);
 		} else if (currentDatetime.getDate() > date.getDate()) {
 			int valueDiff = currentDatetime.getDate() - date.getDate();
 
 			String isPlural = valueDiff > 1 ? "days" : "day";
 
 			timeString = MessageFormat.format("{0} {1} ago", valueDiff,
 					isPlural);
 		} else if (currentDatetime.getHours() > date.getHours()) {
 			int valueDiff = currentDatetime.getHours() - date.getHours();
 
 			String isPlural = valueDiff > 1 ? "hours" : "hour";
 
 			timeString = MessageFormat.format("{0} {1} ago", valueDiff,
 					isPlural);
 		} else if (currentDatetime.getMinutes() > date.getMinutes()) {
 			int valueDiff = currentDatetime.getMinutes() - date.getMinutes();
 
 			String isPlural = valueDiff > 1 ? "minutes" : "minute";
 
 			timeString = MessageFormat.format("{0} {1} ago", valueDiff,
 					isPlural);
 		} else {
 			timeString = "just now";
 		}
 
 		footerLabel.setText(MessageFormat.format("By {0} {1}.", userName,
 				timeString));
 
 	}
 
 	public String getFooterText() {
 		return footerLabel.getText();
 	}
 
 	public void setCommentHtml(String html) {
 		panelCommentContainer.getElement().setInnerHTML(html);
 	}
 
 	public String getCommentHtml() {
 		return panelCommentContainer.getElement().getInnerHTML();
 	}
 
 	public boolean getIsResolvedCheck() {
 		return isMarked;
 	}
 
 	public String getReferencedGuid() {
 		return refGuid;
 	}
 
 	private void buildStructure() {
 		panelHeader = new SimplePanel();
 		panelLeftCheckMarker = new SimplePanel();
 		panelCommentContainer = new SimplePanel();
 		panelFooter = new SimplePanel();
 		commentTextbox = new RichTextArea();
 		footerLabel = new Label();
 		btnExpendAllReplies = new Button();
 		btnExpendAllReplies.setSize("20px", "20px");
 
 		if (isNewComment) {
 			panelHeader.setStyleName("conversation-sub-header-new");
 			panelCommentContainer
 					.setStyleName("conversation-sub-comment-container-new");
 			panelCommentContainer.setHeight("100px");
 			panelFooter.setStyleName("conversation-sub-footer-new");
 			commentTextbox.setStyleName("conversation-sub-comment-new");
 			commentTextbox.setHeight("100px");
 			panelCommentContainer.add(commentTextbox);
 
 			chkResolved = new ExtendedCheckBox("This conversation is resolved.");
 			chkResolved.setStyleName("conversation-checkBox-resolved");
 
			btnAddComment = new Button("Add comment");
			btnAddComment.setWidth("120px");
 			FlowPanel panelRow = new FlowPanel();
 			FlowPanel panelRowInner = new FlowPanel();
 			panelRow.add(panelRowInner);
 
 			FlowPanel panelLeftCell = new FlowPanel();
 			FlowPanel panelMidCell = new FlowPanel();
 			FlowPanel panelRightCell = new FlowPanel();
 
 			panelRowInner.add(panelLeftCell);
 			panelRowInner.add(panelMidCell);
 			panelRowInner.add(panelRightCell);
 
 			panelRow.setStyleName("item-footer-row");
 			panelRowInner.getElement().setId("item-footer-row-inner");
 
 			panelLeftCell.setStyleName("item-footer-row-cells");
 			panelMidCell.setStyleName("item-footer-row-cells");
 			panelMidCell.setWidth("300px");
 			panelRightCell.setStyleName("item-footer-row-cells");
 
 			panelLeftCell.getElement().setId("item-footer-row-left-cell");
 			panelMidCell.getElement().setId("item-footer-row-right-cell");
 			panelRightCell.getElement().setId("item-footer-row-right-cell");
 			panelFooter.add(panelRow);
 			panelMidCell.add(chkResolved);
			panelRightCell.add(btnAddComment);
 			super.add(panelHeader, getElement());
 			super.add(panelLeftCheckMarker, getElement());
 			super.add(panelCommentContainer, getElement());
 			super.add(panelFooter, getElement());
 			chkResolved
 					.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
 
 						@Override
 						public void onValueChange(
 								ValueChangeEvent<Boolean> event) {
 							isMarked = event.getValue();
 						}
 					});
 		} else {
 			panelHeader.setStyleName("conversation-sub-header");
 			panelLeftCheckMarker.setStyleName("conversation-sub-checkmarker");
 			panelCommentContainer
 					.setStyleName("conversation-sub-comment-container");
 			panelFooter.setStyleName("conversation-sub-footer");
 
 			footerLabel.setStyleName("conversation-sub-footer-label");
 			btnExpendAllReplies
 					.setStyleName("conversation-expend-btn-collapsed");
 
 			panelCommentContainer.getElement().setInnerHTML("");
 
 			if (isRootComment) {
 
 				panelCommentContainer
 						.setStyleName("conversation-sub-comment-container-root");
 				FlowPanel panelRow = new FlowPanel();
 				FlowPanel panelRowInner = new FlowPanel();
 				panelRow.add(panelRowInner);
 				FlowPanel panelLeftCell = new FlowPanel();
 				FlowPanel panelRightCell = new FlowPanel();
 
 				panelRowInner.add(panelLeftCell);
 				panelRowInner.add(panelRightCell);
 
 				panelRow.setStyleName("item-footer-row");
 				panelRowInner.getElement().setId("item-footer-row-inner");
 
 				panelLeftCell.setStyleName("item-footer-row-cells");
 				panelRightCell.setStyleName("item-footer-row-cells");
 
 				panelLeftCell.getElement().setId("item-footer-row-left-cell");
 				panelRightCell.getElement().setId("item-footer-row-right-cell");
 
 				panelFooter.add(panelRow);
 				panelLeftCell.add(footerLabel);
 				panelRightCell.add(btnExpendAllReplies);
 
 				super.add(panelHeader, getElement());
 				super.add(panelCommentContainer, getElement());
 				super.add(panelFooter, getElement());
 
 			} else {
 				panelFooter.setStyleName("conversation-sub-footer-reply", true);
 				panelFooter.add(footerLabel);
 
 				super.add(panelHeader, getElement());
 				super.add(panelLeftCheckMarker, getElement());
 				super.add(panelCommentContainer, getElement());
 				super.add(panelFooter, getElement());
 			}
 
 		}
 
 		// btnExpendAllReplies.addClickHandler(new ClickHandler() {
 		//
 		// @Override
 		// public void onClick(ClickEvent event) {
 		// Window.alert("Test");
 		//
 		// }
 		// });
 		//
 		// if (btnPost != null) {
 		// btnPost.addClickHandler(new ClickHandler() {
 		//
 		// @Override
 		// public void onClick(ClickEvent event) {
 		// Window.alert("Test");
 		//
 		// }
 		// });
 		// }
 	}
 
 	private void updateMarkerStatus() {
 		if (isMarked) {
 			panelLeftCheckMarker
 					.addStyleName("conversation-sub-checkmarker-checked");
 			panelLeftCheckMarker
 					.getElement()
 					.setInnerHTML(
 							"<img src="
 									+ imgGreenChecked
 									+ " alt=\"Resolved\" height=\"48\" width=\"48\" />");
 			if (isRootComment) {
 				panelHeader.addStyleName("conversation-root-resolved-header");
 				panelFooter.addStyleName("conversation-root-resolved-footer");
 			}
 
 		} else {
 			panelLeftCheckMarker.getElement().setInnerHTML("");
 			panelLeftCheckMarker
 					.removeStyleName("conversation-sub-checkmarker-checked");
 
 			if (isRootComment) {
 				panelHeader
 						.removeStyleName("conversation-root-resolved-header");
 				panelFooter
 						.removeStyleName("conversation-root-resolved-footer");
 			}
 		}
 	}
 
 	public boolean getIsRootComment() {
 		return isRootComment;
 	}
 
 	public void setIsMarked(boolean marked) {
 		isMarked = marked;
 		updateMarkerStatus();
 	}
 
 	public boolean getIsMarked() {
 		return isMarked;
 	}
 
 	public void setExpendedBtnDown(boolean isRepliesHidden) {
 		if (isRepliesHidden) {
 			btnExpendAllReplies
 					.setStyleName("conversation-expend-btn-collapsed");
 		} else {
 			btnExpendAllReplies
 					.setStyleName("conversation-expend-btn-expended");
 		}
 
 	}
 
 	public void isExpendedBtnVisible(boolean visible) {
 		btnExpendAllReplies.setVisible(visible);
 	}
 
 	public String getNewCommentHtml() {
 		return commentTextbox.getHTML();
 	}
 
 	public void showResolvedCheckBox(boolean visible) {
 			chkResolved.setVisible(visible);
 	}
 
 	public ConversationControl getConversationControl()
 	{
 		return this.conversationControl;
 	}
 	protected void setConversationControl(ConversationControl control)
 	{
 		this.conversationControl=control;
 	}
 
 	protected void setNewCommentHtml(String string) {
 		commentTextbox.setHTML("");
 	}
 }
