 package net.thesocialos.client.chat;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.thesocialos.client.TheSocialOS;
 import net.thesocialos.client.app.IApplication;
 import net.thesocialos.client.chat.events.ChatCloseConversation;
 import net.thesocialos.client.chat.events.ChatHideConversation;
 import net.thesocialos.client.chat.events.ChatSendMessage;
 import net.thesocialos.client.chat.events.ChatTopConversations;
 import net.thesocialos.client.chat.view.ChatConversationView;
 import net.thesocialos.client.desktop.DesktopEventOnClose;
 import net.thesocialos.client.desktop.DesktopEventOnTop;
 import net.thesocialos.client.desktop.DesktopEventonEndDrag;
 import net.thesocialos.client.desktop.DesktopUnit;
 import net.thesocialos.client.desktop.window.events.WindowCloseEvent;
 import net.thesocialos.client.desktop.window.events.WindowDisplay;
 import net.thesocialos.client.desktop.window.events.WindowEndDragEvent;
 import net.thesocialos.client.desktop.window.events.WindowEventHandler;
 import net.thesocialos.client.desktop.window.events.WindowMaximizeEvent;
 import net.thesocialos.client.desktop.window.events.WindowMinimizeEvent;
 import net.thesocialos.client.desktop.window.events.WindowOnTopEvent;
 import net.thesocialos.client.desktop.window.events.WindowResizeEvent;
 import net.thesocialos.shared.model.Lines;
 import net.thesocialos.shared.model.User;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.event.dom.client.KeyDownEvent;
 import com.google.gwt.event.dom.client.KeyDownHandler;
 import com.google.gwt.user.cellview.client.CellList;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.ui.AbsolutePanel;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ScrollPanel;
 import com.google.gwt.user.client.ui.TextArea;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.view.client.ListDataProvider;
 import com.googlecode.objectify.Key;
 
 public class ChatConversationPresenter extends DesktopUnit implements IApplication {
 	
 	public interface Display {
 		
 		ChatConversationView getChatPanel();
 		
 		Button getSendButton();
 		
 		TextArea getSendText();
 		
 		CellList<Lines> getConversation();
 		
 		Label lblCharacters();
 		
 		ScrollPanel getScrollPanel();
 		
 		Widget asWidget();
 	}
 	
 	private String name;
 	private String image;
 	
 	private Display display;
 	private Key<User> userWithChat;
 	
 	/*
 	 * Los modelos de la cajas de seleccion de los contactos
 	 */
 	
 	List<Lines> linesList = new ArrayList<Lines>();
 	ListDataProvider<Lines> linesDataProvider = new ListDataProvider<Lines>(linesList);
 	
 	public ChatConversationPresenter(int programID, String appName, String appImageURL, Key<User> userWithChat,
 			WindowDisplay windoDisplay, Display display) {
 		super(programID, "Chat Conversation", windoDisplay, TypeUnit.WINDOW, true);
 		setName(appName);
 		setImage(appImageURL);
 		
 		this.display = display;
 		this.userWithChat = userWithChat;
 		
 		init();
 	}
 	
 	private void init() {
 		bindHandlers();
 		initWindow();
 		linesDataProvider.addDataDisplay(display.getConversation());
 		
 	}
 	
 	private void bindHandlers() {
 		display.getSendButton().addClickHandler(new ClickHandler() {
 			
 			@Override
 			public void onClick(ClickEvent event) {
				if (!display.getSendText().getText().trim().isEmpty()) {
					TheSocialOS.getEventBus().fireEvent(
							new ChatSendMessage(userWithChat, display.getSendText().getText()));
					display.getSendText().setValue(null);
					event.preventDefault();
				} else
					event.preventDefault();
				
				if ((150 - display.getSendText().getText().length()) < 0)
					display.getSendText().setText(display.getSendText().getText().substring(0, 149));
				display.lblCharacters().setText(String.valueOf((150 - display.getSendText().getText().length())));
 				
 			}
 		});
 		
 		display.getSendText().addKeyDownHandler(new KeyDownHandler() {
 			
 			@Override
 			public void onKeyDown(KeyDownEvent event) {
 				
 				if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER)
 					if (!display.getSendText().getText().trim().isEmpty()) {
 						TheSocialOS.getEventBus().fireEvent(
 								new ChatSendMessage(userWithChat, display.getSendText().getText()));
 						display.getSendText().setValue(null);
 						event.preventDefault();
 					} else
 						event.preventDefault();
 				
 				if ((150 - display.getSendText().getText().length()) < 0)
 					display.getSendText().setText(display.getSendText().getText().substring(0, 149));
 				display.lblCharacters().setText(String.valueOf((150 - display.getSendText().getText().length())));
 				
 			}
 		});
 		
 		windowDisplay.addWindowEvents(new WindowEventHandler() {
 			
 			@Override
 			public void onTop(WindowOnTopEvent event) {
 				TheSocialOS.getEventBus().fireEvent(new ChatTopConversations(userWithChat));
 				TheSocialOS.getEventBus().fireEvent(new DesktopEventOnTop(ChatConversationPresenter.this));
 				
 			}
 			
 			@Override
 			public void onMinimize(WindowMinimizeEvent windowMinimizeEvent) {
 				
 				TheSocialOS.getEventBus().fireEvent(new ChatHideConversation(userWithChat));
 				
 			}
 			
 			@Override
 			public void onMaximize(WindowMaximizeEvent windowMaximizeEvent) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void onEndDrag(WindowEndDragEvent event) {
 				// TODO Auto-generated method stub
 				TheSocialOS.getEventBus().fireEvent(new DesktopEventonEndDrag(ChatConversationPresenter.this));
 			}
 			
 			@Override
 			public void onClose(WindowCloseEvent event) {
 				TheSocialOS.getEventBus().fireEvent(new ChatCloseConversation(userWithChat));
 				TheSocialOS.getEventBus().fireEvent(new DesktopEventOnClose(ChatConversationPresenter.this));
 				
 			}
 			
 			@Override
 			public void onResize(WindowResizeEvent event) {
 				// TODO Auto-generated method stub
 				
 			}
 		});
 	}
 	
 	/**
 	 * Write a messageChat in the window
 	 * 
 	 * @param message
 	 *            to write in the chat window
 	 */
 	public void writeMessage(Lines line) {
 		
 		linesList.add(line);
 		linesDataProvider.flush();
 		linesDataProvider.refresh();
 		// display.getScrollPanel().scrollToBottom();
 		// display.getScrollPanel().setVerticalScrollPosition(
 		// display.getScrollPanel().getMaximumVerticalScrollPosition() - 20);
 		scrollTobottom();
 		
 	}
 	
 	private void scrollTobottom() {
 		Timer timer = new Timer() {
 			
 			@Override
 			public void run() {
 				display.getScrollPanel().scrollToBottom();
 				cancel();
 				
 			}
 		};
 		timer.schedule(300);
 		/*
 		 * Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
 		 * @Override public void execute() { // your commands here } });
 		 */
 	}
 	
 	/**
 	 * Make the window have go on top of the desktop
 	 */
 	public void setOnTop() {
 		TheSocialOS.getEventBus().fireEvent(new DesktopEventOnTop(this));
 	}
 	
 	@Override
 	public String getImage() {
 		// TODO Auto-generated method stub
 		return image;
 	}
 	
 	@Override
 	public String getName() {
 		// TODO Auto-generated method stub
 		return name;
 	}
 	
 	@Override
 	public void setImage(String image) {
 		this.image = image;
 		
 	}
 	
 	@Override
 	public void setName(String name) {
 		this.name = name;
 		
 	}
 	
 	@Override
 	public void close(AbsolutePanel absolutePanel) {
 		absolutePanel.remove(windowDisplay.getWindow());
 		
 	}
 	
 	@Override
 	public int getZposition() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 	
 	@Override
 	public void open(AbsolutePanel absolutePanel) {
 		
 		absolutePanel.add(windowDisplay.getWindow(), 20, 50);
 		windowDisplay.getWindow().setVisible(true);
 		scrollTobottom();
 		// absolutePanel.add(display.asWidget(), 20, 50);
 		
 	}
 	
 	private void initWindow() {
 		
 		windowDisplay.setResizable(false);
 		windowDisplay.setWindowTitle(userWithChat.getName());
 		display.asWidget().setSize("447px", "323px");
 		windowDisplay.getWindow().add(display.asWidget());
 		
 	}
 	
 	@Override
 	public void toZPosition(int position) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 }
