 package org.iplantc.de.client.desktop.widget;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.Style.Float;
 import com.google.gwt.event.logical.shared.ResizeEvent;
 import com.google.gwt.event.logical.shared.ResizeHandler;
 import com.google.gwt.resources.client.ClientBundle;
 import com.google.gwt.resources.client.CssResource;
 import com.google.gwt.resources.client.ImageResource;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.IsWidget;
 import com.sencha.gxt.core.client.Style.Side;
 import com.sencha.gxt.core.client.dom.XElement;
 import com.sencha.gxt.widget.core.client.Popup;
 import com.sencha.gxt.widget.core.client.button.IconButton.IconConfig;
 import com.sencha.gxt.widget.core.client.button.ToolButton;
 import com.sencha.gxt.widget.core.client.container.CssFloatLayoutContainer;
 import com.sencha.gxt.widget.core.client.container.CssFloatLayoutContainer.CssFloatData;
 import com.sencha.gxt.widget.core.client.container.SimpleContainer;
 import com.sencha.gxt.widget.core.client.event.SelectEvent;
 import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
 
 /**
  * Objects of this class display a message in the top center of the view port. The message may be a
  * widget, allowing a user to interact with the message to obtain more information. By default, the
  * message is closable by the user and will close automatically after 10 seconds. 
  * 
  * Only one message can be displayed at time. If a second message is displayed, the first one will
  * be replaced.
  * 
  * FIXME Due to time pressure, Announcer itself guarantees that there is only one Announcer
  * displayed at a time. This is not very MVP design.
  */
 public final class Announcer {
 
 	/**
 	 * TODO externalize this interface
 	 */
 	interface Resources extends ClientBundle {
 		
 		interface Style extends CssResource {
 		
 			String closeButton();
 		
 			String closeButtonOver();
 			
 			String panel();
 			
			@ClassName("multiple-panels")
			String multiplePanels();
			
 			String content();
 		
 		}		
 		
 		@Source("button_exit.png")
 		ImageResource closeButtonImg();
 		
 		@Source("button_exit_hover.png")
 		ImageResource closeButtonOverImg();
 		
 		@Source("Announcer.css")
 		Style style();
 		
 	}
 	
 	private static final Resources RESOURCES;
 	private static final Resources.Style STYLE;
 	private static final IconConfig CLOSER_CFG;
 	private static final int DEFAULT_TIMEOUT_ms;
 
 	static {
 		RESOURCES = GWT.create(Resources.class);
 		STYLE = RESOURCES.style();
 		STYLE.ensureInjected();
 		CLOSER_CFG = new IconConfig(STYLE.closeButton(), STYLE.closeButtonOver());
 		DEFAULT_TIMEOUT_ms = 10000;
 	}
 	
 	private static Announcer currentAnnouncer = null;
 	
 	private static void setCurrentAnnouncer(final Announcer current) {
 		if (currentAnnouncer != null) {
 			currentAnnouncer.hide();
 		}
 		
 		currentAnnouncer = current;		
 	}
 	
 	private final class CloseHandler implements SelectHandler {
 		
 		@Override
 		public void onSelect(final SelectEvent event) {
 			hide();
 		}
 		
 	}
 	
 	private final class CloseTimer extends Timer {
 
 		@Override
 		public void run() {
 			hide();
 		};
 		
 	}
 	
 	private static final Popup makePanel(final IsWidget content, final boolean closable, 
 			final CloseHandler closer) {
 		final SimpleContainer contentContainer = new SimpleContainer();
 		contentContainer.setWidget(content);
 		contentContainer.addStyleName(STYLE.content());
 
 		final CssFloatLayoutContainer layout = new CssFloatLayoutContainer();
 		layout.add(contentContainer, new CssFloatData(-1));
 		
 		if (closable) {
 			final ToolButton closeButton = new ToolButton(CLOSER_CFG, closer);
 			layout.add(closeButton, new CssFloatData(-1));
 			closeButton.getElement().getStyle().setFloat(Float.RIGHT);
 		}
 		
 		final Popup panel = new Popup();
 		panel.setWidget(layout);
 		panel.setAutoHide(false);
 		panel.addStyleName(STYLE.panel());
		panel.setShadow(true);
 		
 		return panel;
 	}
 	
 	
 	private final Popup panel;
 	private final Timer timer;
 	private final int timeout_ms;
 	
 	/**
 	 * Constructs a user closable announcer that will close automatically after 10 seconds.
 	 * 
 	 * @param content the message widget
 	 */
 	public Announcer(final IsWidget content) {
 		this(content, true, DEFAULT_TIMEOUT_ms);
 	}
 	
 	/**
 	 * Constructs an announcer that will close automatically after 10 seconds.
 	 * 
 	 * @param content the message widget
 	 * @param closable a flag indicating whether or not the message is user closable.
 	 */
 	public Announcer(final IsWidget content, final boolean closable) {
 		this(content, closable, DEFAULT_TIMEOUT_ms);
 	}
 	
 	/**
 	 * Constructs an announcer. Setting a timeout of 0 or less will cause the message to not close 
 	 * automatically. 
 	 * 
 	 * If the closable flag is set to false, the message must close automatically. In this case, if
 	 * the provided timeout is 0 or less, the message will close automatically after 10 seconds.
 	 * 
 	 * @param content the message widget
 	 * @param closable a flag indicating whether or not the message is closable.
 	 * @param timeout_ms the amount of time in milliseconds to wait before automatically closing the
 	 * message.
 	 */
 	public Announcer(final IsWidget content, final boolean closable, final int timeout_ms) {
 		this.panel = makePanel(content, closable, new CloseHandler());
 		this.timer = new CloseTimer();
 		this.timeout_ms = (!closable && timeout_ms <= 0) ? DEFAULT_TIMEOUT_ms : timeout_ms;
 		
 		Window.addResizeHandler(new ResizeHandler() {
 			@Override
 			public void onResize(final ResizeEvent event) {
 				positionPanel();
 			}});
 	}
 	
 	/**
 	 * closes the message
 	 */
 	public void hide() {
 		timer.cancel();
 		panel.hide();
 	}
 	
 	/**
 	 * shows the message
 	 */
 	public void show() {
 		setCurrentAnnouncer(this);
 		panel.show();
 		positionPanel();
 		if (timeout_ms > 0) {
 			timer.schedule(timeout_ms);
 		}
 	}
 	
 	private void positionPanel() {
 		final XElement panElmt = panel.getElement();
 		final int panelWid = panElmt.getMargins(Side.LEFT, Side.RIGHT) + panElmt.getOffsetWidth();
 		panel.getElement().setX((Window.getClientWidth() - panelWid)/2);
		panel.getElement().setY(0);		
 	}
 	
 }
