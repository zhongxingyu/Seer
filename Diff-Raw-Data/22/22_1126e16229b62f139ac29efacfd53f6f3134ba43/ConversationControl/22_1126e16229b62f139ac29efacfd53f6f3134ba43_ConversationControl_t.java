 package org.palaso.languageforge.client.lex.controls.conversation;
 
 import com.google.gwt.dom.client.Document;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.user.client.ui.ComplexPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 public class ConversationControl extends ComplexPanel {
 
 	// W3C dateformat
 	public static DateTimeFormat POST_DATE_TIME_FORMAT = DateTimeFormat
 			.getFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
 
 	private ConversationItem newCommentItem = new ConversationItem();
 	private ConversationItem rootCommentItem = null;
 	private Element uListElementRoot = null;
 	private Element uListElementMessage = null;
 	private Element uListElementReplay = null;
 	private boolean isRepliesHidden = false;
 	private ConversationPostClickHandler conversationPostClickHandler;
 
 	private final ClickHandler expendedCollClickHandler = new ClickHandler() {
 		@Override
 		public void onClick(ClickEvent event) {
 			setExpended(isRepliesHidden);
 		}
 	};
 
 	public void setConversationPostClickHandler(
 			ConversationPostClickHandler handler) {
 		this.conversationPostClickHandler = handler;
 	}
 
 	public ConversationControl() {
 		uListElementRoot = Document.get().createULElement();
 		uListElementMessage = Document.get().createULElement();
 		uListElementReplay = Document.get().createElement("UL");
 
 		setElement(uListElementRoot);
 		// setStyleName("conversation-panel");
 		uListElementRoot.appendChild(uListElementMessage);
 		uListElementRoot.appendChild(uListElementReplay);
 
		newCommentItem.getAddCommentButtonClickHandlers().addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {
 				ConversationPostEvent newEvent = new ConversationPostEvent(
 						newCommentItem, rootCommentItem);
 				if (conversationPostClickHandler != null) {
 					conversationPostClickHandler.onNewPostClicked(newEvent);
 				}
 			}
 		});
 	}
 
 	@Override
 	public void add(Widget w) {
 
 		if (w instanceof ConversationItem) {
 			ConversationItem conversationItem = (ConversationItem) w;
 			conversationItem.setConversationControl(this);
 			if (conversationItem.getIsRootComment()) {
 				if (uListElementMessage.getChildCount() == 0) {
 					rootCommentItem = conversationItem;
 					rootCommentItem.setExpendedBtnDown(false);
 					conversationItem.getExpendedClickHandlers().addClickHandler(
 							expendedCollClickHandler);
 					uListElementMessage.appendChild(w.getElement());
 					super.add(
 							w,
 							(com.google.gwt.user.client.Element) uListElementMessage);
 				}
 			} else {
 				super.add(w,
 						(com.google.gwt.user.client.Element) uListElementReplay);
 			}
 
 			if (conversationItem.getIsResolvedCheck()
 					&& rootCommentItem != null) {
 				rootCommentItem.setIsMarked(true);
 			}
 
 		} else {
 			super.add(w,
 					(com.google.gwt.user.client.Element) uListElementReplay);
 		}
 		if (uListElementReplay.getChildCount() > 0 && rootCommentItem != null) {
 			rootCommentItem.isExpendedBtnVisible(true);
 		} else if (rootCommentItem != null) {
 			rootCommentItem.isExpendedBtnVisible(false);
 		}
 	}
 
 	public void addItem(ConversationItem item) {
 		item.setConversationControl(this);
 		if (item.getIsResolvedCheck() && rootCommentItem != null) {
 			rootCommentItem.setIsMarked(true);
 		}
 		if (item.getIsRootComment()) {
 			if (uListElementMessage.getChildCount() == 0) {
 				rootCommentItem = item;
 				rootCommentItem.setExpendedBtnDown(false);
 				item.getExpendedClickHandlers().addClickHandler(
 						expendedCollClickHandler);
 				uListElementMessage.appendChild(item.getElement());
 				super.add(
 						item,
 						(com.google.gwt.user.client.Element) uListElementMessage);
 			}
 		} else {
 			super.add(item,
 					(com.google.gwt.user.client.Element) uListElementReplay);
 		}
 		if (uListElementReplay.getChildCount() > 0 && rootCommentItem != null) {
 			rootCommentItem.isExpendedBtnVisible(true);
 		} else if (rootCommentItem != null) {
 			rootCommentItem.isExpendedBtnVisible(false);
 		}
 	}
 
 	public void showNewComment() {
 		showNewComment(true);
 	}
 
 	public void showNewComment(boolean showResolvedCheckBox) {
 		uListElementRoot.appendChild(newCommentItem.getElement());
 		super.add(newCommentItem,
 				(com.google.gwt.user.client.Element) uListElementRoot);
 		if (newCommentItem != null) {
 			if (showResolvedCheckBox) {
 				newCommentItem.setIsMarked(false);
 				newCommentItem.showResolvedCheckBox(true);
 			} else {
 				newCommentItem.setIsMarked(false);
 				newCommentItem.showResolvedCheckBox(false);
 			}
 		}
 	}
 
 	public void closeNewComment() {
 		if (newCommentItem.getElement().getParentElement() == uListElementRoot) {
 			uListElementRoot.removeChild(newCommentItem.getElement());
 			super.remove(newCommentItem);
 		}
 	}
 
 	public void setExpended(boolean isExpended) {
 		if (!isExpended) {
 			uListElementReplay.setClassName("conversation-sub-comment-hide");
 			isRepliesHidden = true;
 			rootCommentItem.setExpendedBtnDown(isRepliesHidden);
 		} else {
 			uListElementReplay.setClassName("conversation-sub-comment-show");
 			isRepliesHidden = false;
 			rootCommentItem.setExpendedBtnDown(isRepliesHidden);
 		}
 	}
 
 	public void resetNewCommentBox() {
 		newCommentItem.setIsMarked(false);
 		newCommentItem.setNewCommentHtml("");
 	}
 
 }
