 package com.christophdietze.jack.client.view;
 
 import com.christophdietze.jack.client.event.MatchEndedEvent;
 import com.christophdietze.jack.client.event.MatchEndedEventHandler;
 import com.christophdietze.jack.client.event.MatchStartedEvent;
 import com.christophdietze.jack.client.event.MatchStartedEventHandler;
 import com.christophdietze.jack.client.event.SignedInEvent;
 import com.christophdietze.jack.client.event.SignedInEventHandler;
 import com.christophdietze.jack.client.event.UncaughtExceptionEvent;
 import com.christophdietze.jack.client.event.UncaughtExceptionEventHandler;
 import com.christophdietze.jack.client.presenter.MatchInfo;
 import com.christophdietze.jack.client.util.GlobalEventBus;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.resources.client.CssResource;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ScrollPanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.inject.Inject;
 
 public class MessagePanel extends Composite {
 
 	private static MessagePanelUiBinder uiBinder = GWT.create(MessagePanelUiBinder.class);
 
 	interface MessagePanelUiBinder extends UiBinder<Widget, MessagePanel> {
 	}
 
 	interface MyStyle extends CssResource {
 		String messageLabel();
 	}
 
 	private GlobalEventBus eventBus;
 
 	@UiField
 	MyStyle style;
 	@UiField
 	ScrollPanel scrollPanel;
 	@UiField
 	FlowPanel messagePanel;
 
 	@Inject
	public MessagePanel(GlobalEventBus eventBus) {
 		this.eventBus = eventBus;
 		initWidget(uiBinder.createAndBindUi(this));
 		// somehow, adding css style to the scrollpanel in the ui binder xml doesnt work, so do that here.
 		DOM.setStyleAttribute(scrollPanel.getElement(), "overflowY", "scroll");
 		DOM.setStyleAttribute(scrollPanel.getElement(), "background", "#eeeeee");
 		initListeners();
 	}
 
 	private void addMessage(String message) {
 		Label label = new Label(message);
 		label.setStyleName(style.messageLabel());
 		messagePanel.add(label);
 		scrollPanel.scrollToBottom();
 	}
 
 	private void initListeners() {
 		eventBus.addHandler(MatchStartedEvent.TYPE, new MatchStartedEventHandler() {
 			@Override
 			public void onMatchStarted(MatchStartedEvent event) {
 				MatchInfo matchInfo = event.getMatchInfo();
 				StringBuilder sb = new StringBuilder();
 				sb.append("New Match: You play as ");
 				sb.append(matchInfo.isPlayerWhite() ? "white" : "black");
 				sb.append(" against Guest" + matchInfo.getOpponentId() + ".");
 				addMessage(sb.toString());
 			}
 		});
 		eventBus.addHandler(MatchEndedEvent.TYPE, new MatchEndedEventHandler() {
 			@Override
 			public void onMatchEnded(MatchEndedEvent event) {
 				switch (event.getReason()) {
 				case YOU_ABORTED:
 					addMessage("You aborted the match.");
 					break;
 				case OPPONENT_ABORTED:
 					addMessage("Your opponent aborted the match.");
 					break;
 				}
 			}
 		});
 		eventBus.addHandler(SignedInEvent.TYPE, new SignedInEventHandler() {
 			@Override
 			public void onSignIn(SignedInEvent event) {
 				addMessage("You are signed in as Guest" + event.getLocationId() + ".");
 			}
 		});
 		eventBus.addHandler(UncaughtExceptionEvent.TYPE, new UncaughtExceptionEventHandler() {
 			@Override
 			public void onException(UncaughtExceptionEvent event) {
 				addMessage("Oops, an unexpected error occurred: " + event.getMessage());
 			}
 		});
 	}
 }
