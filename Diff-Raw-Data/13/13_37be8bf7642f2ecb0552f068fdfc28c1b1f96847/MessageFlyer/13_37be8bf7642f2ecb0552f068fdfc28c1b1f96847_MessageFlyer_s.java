 package com.marthym.oikonomos.client.components;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.Document;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.dom.client.LIElement;
 import com.google.gwt.dom.client.UListElement;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.HTMLPanel;
 import com.google.gwt.user.client.ui.PopupPanel;
 import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.InlineHyperlink;
 import com.marthym.oikonomos.client.i18n.OikonomosConstants;
 import com.marthym.oikonomos.client.resources.MessageFlyerResource;
 
 public class MessageFlyer extends PopupPanel {
 	private static enum Type {INFO, ERROR};
 	private static int INFO_AUTOCLOSE_DELAY = 2 * 1000;
 	private static MessageFlyer instance;
 	
 	private static MessageFlyerUiBinder uiBinder = GWT
 			.create(MessageFlyerUiBinder.class);
 	
 	@UiField HTMLPanel flyer;
	@UiField InlineHyperlink linkClose;
 	@UiField UListElement messages;
 	@UiField Element title;
 	
 	Timer closeTimer = new Timer() {
 		@Override
 		public void run() {
 			instance.hide();
 			cancel();
 		}
 	};
 
 	private final static MessageFlyerResource res = MessageFlyerResource.INSTANCE;
 	private final static OikonomosConstants translations = GWT.create(OikonomosConstants.class);
 	
 	interface MessageFlyerUiBinder extends
 		UiBinder<Widget, MessageFlyer> {
 	}
 
 	private MessageFlyer() {
 		super(true);
 		res.style().ensureInjected();
 		
 		setWidget(uiBinder.createAndBindUi(this));
 	}
 	
 	@UiHandler("linkClose")
 	void onClickClose(ClickEvent e) {
 		this.hide();
 	}
 	
 	private static void display(Type type, List<String> htmlMessages) {
 		if (instance != null) {
 			instance.hide();
 			instance.removeFromParent();
 		}
 		instance = new MessageFlyer();
 		switch (type) {
 		case ERROR:
 			instance.flyer.addStyleName(res.style().error());
 			instance.title.setInnerText(translations.error()); break;
 		case INFO:
 			instance.flyer.addStyleName(res.style().info());
 			instance.title.setInnerText(translations.info());
 			instance.closeTimer.schedule(INFO_AUTOCLOSE_DELAY);
 			break;
 		}
 		instance.populateMessagesList(htmlMessages);
 		instance.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
 			public void setPosition(int offsetWidth, int offsetHeight) {
 				int left = (Window.getClientWidth() - offsetWidth) / 100 * 10;
 				int top = (Window.getClientHeight() - offsetHeight) / 100 * 10;
 				instance.setPopupPosition(left, top);
 			}
 		});
 	}
 	
 	public static void info(List<String> htmlMessages) {
 		display(Type.INFO, htmlMessages);
 	}
 	
 	public static void error(List<String> htmlMessages) {
 		display(Type.ERROR, htmlMessages);
 	}
 	
 	public static void info(String htmlMessage) {
 		List<String> htmlMessages = new LinkedList<String>();
 		htmlMessages.add(htmlMessage);
 		info(htmlMessages);
 	}
 	
 	public static void error(String htmlMessage) {
 		List<String> htmlMessages = new LinkedList<String>();
 		htmlMessages.add(htmlMessage);
 		error(htmlMessages);
 	}
 	
 	private void populateMessagesList(List<String> htmlMessages) {
 		for (String message : htmlMessages) {
 			LIElement liElement = Document.get().createLIElement();
 			liElement.setInnerText(message);
 			messages.appendChild(liElement);
 		}
 	}
 }
