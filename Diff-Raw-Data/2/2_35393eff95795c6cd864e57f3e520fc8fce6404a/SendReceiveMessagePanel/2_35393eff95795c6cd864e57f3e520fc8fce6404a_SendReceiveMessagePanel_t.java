 package devhood.im.sim.ui;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Desktop;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JEditorPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextArea;
 import javax.swing.JViewport;
 import javax.swing.SwingUtilities;
 import javax.swing.SwingWorker;
 import javax.swing.JComboBox.KeySelectionManager;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.HyperlinkEvent;
 import javax.swing.event.HyperlinkListener;
 import javax.swing.text.DefaultCaret;
 import javax.swing.text.JTextComponent;
 
 import org.apache.commons.lang3.StringEscapeUtils;
 
 import devhood.im.sim.Sim;
 import devhood.im.sim.event.EventDispatcher;
 import devhood.im.sim.event.EventObserver;
 import devhood.im.sim.event.Events;
 import devhood.im.sim.model.Message;
 import devhood.im.sim.model.MessageType;
 import devhood.im.sim.model.MessagingError;
 import devhood.im.sim.model.User;
 import devhood.im.sim.model.UserStatus;
 import devhood.im.sim.service.ServiceLocator;
 import devhood.im.sim.ui.util.ComponentProvider;
 
 /**
  * Panel to send and receive messages.
  * 
  * @author flo
  * 
  */
 public class SendReceiveMessagePanel extends JPanel implements EventObserver {
 
 	/**
 	 * Name des Tabs, in dem die Nachrichten an alle gesendet, empfangen werden.
 	 */
 	private String streamTabName = Sim.streamTabName;
 
 	/**
 	 * Tabbed pane der einzelnen Konversationen. Eine Konv. pro Tab.
 	 */
 	private JTabbedPane tabbedPane;
 
 	/**
 	 * Map von Name Des Tabs -> Texarea des Msg (Timeline).
 	 */
 	private Map<String, JEditorPane> nameTextAreaMap = new HashMap<String, JEditorPane>();
 
 	/**
 	 * Map von Name Des Tabs -> Texarea der Inputzeile (Text sendne).
 	 */
 	private Map<String, JTextArea> inputTextAreaMap = new HashMap<String, JTextArea>();
 
 	/**
 	 * DateFormat.
 	 */
 	private DateFormat df = new SimpleDateFormat("HH:mm:ss");
 
 	/**
 	 * List mit ungelesenen Tabs.
 	 */
 	private List<String> unreadTabsList = new ArrayList<String>();
 
 	public SendReceiveMessagePanel() {
 		super();
 		setLayout(new BorderLayout());
 
 		tabbedPane = new JTabbedPane();
 
 		addToTabPane(
 				streamTabName,
 				Sim.applicationName
 						+ "<br /> "
 						+ "<i>Achtung: alles im Stream Tab wird an alle Teilnehmer geschickt!</i>");
 
 		// Lay out the buttons from left to right.
 		JPanel buttonPane = new JPanel(new BorderLayout());
 
 		JButton sendButton = new JButton("Send");
 		sendButton.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (tabbedPane.getSelectedIndex() != 0) {
 					String toUser = getCurrentSelectedTabTitle();
 					sendMessage(toUser);
 				} else {
 					sendMessage(streamTabName);
 				}
 			}
 		});
 
 		buttonPane.add(sendButton, BorderLayout.CENTER);
 
 		JPanel buttonsRight = new JPanel();
 		JButton clearButton = new JButton("Clear");
 
 		clearButton.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				clearText(nameTextAreaMap.get(getCurrentSelectedTabTitle()));
 			}
 		});
 
 		// Schlie�t das aktuelle Tab.
 		JButton closeButton = new JButton("Close");
 		closeButton.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 
 				int index = tabbedPane.getSelectedIndex();
 				if (index == 0) {
 					return;
 				}
 				String title = getCurrentSelectedTabTitle();
 				nameTextAreaMap.remove(title);
 				inputTextAreaMap.remove(title);
 
 				tabbedPane.remove(index);
 
 				addKeyboardShortcuts(tabbedPane);
 			}
 		});
 
 		JComboBox statusComboBox = new JComboBox(
 				new UserStatus[] { UserStatus.AVAILABLE, UserStatus.BUSY,
 						UserStatus.NOT_AVAILABLE });
 
 		// statusComboBox.setEditable(true);
 		statusComboBox.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				JComboBox box = (JComboBox) e.getSource();
 				UserStatus status = (UserStatus) box.getSelectedItem();
 
 				Sim.getCurrentUser().setStatusType(status);
 				Message statusMessage = new Message();
 				statusMessage.setMessageType(MessageType.USER_STATUS);
 				statusMessage.setUserStatus(status);
 				statusMessage.setSender(Sim.getCurrentUser().getName());
 
 				List<User> users = ServiceLocator.getInstance()
 						.getRegistryService().getUsers();
 				for (User user : users) {
 					statusMessage.getReceiver().add(user.getName());
 				}
 
 				EventDispatcher.fireEvent(Events.MESSAGE_SENT, statusMessage);
 			}
 		});
 
 		buttonsRight.add(statusComboBox);
 		buttonsRight.add(clearButton);
 		buttonsRight.add(closeButton);
 
 		buttonPane.add(buttonsRight, BorderLayout.EAST);
 
 		this.add(tabbedPane, BorderLayout.CENTER);
 		this.add(buttonPane, BorderLayout.PAGE_END);
 
 		createUnreadMessagesTimer();
 
 		EventDispatcher.add(this);
 	}
 
 	/**
 	 * Loescht den Text aus der Textarea.
 	 * 
 	 * @param area
 	 *            textarea.
 	 */
 	public void clearText(JTextComponent area) {
 		area.setText(null);
 	}
 
 	/**
 	 * Erzeugt den Timer, der regelm��ig checkt ob es ungelesene Nachrichten
 	 * gibt.
 	 */
 	public void createUnreadMessagesTimer() {
 		Timer t = new Timer("Unread Messages Timer");
 		TimerTask task = new TimerTask() {
 
 			@Override
 			public void run() {
 
 				if (unreadTabsList.size() > 0) {
 					EventDispatcher.fireEvent(Events.UNREAD_MESSAGES,
 							unreadTabsList);
 				}
 			}
 		};
 
 		t.schedule(task, 0, 20000);
 	}
 
 	/**
 	 * Gibt den Titel des aktuellen Tabs zurueck.
 	 * 
 	 * @return Titel.
 	 */
 	protected String getCurrentSelectedTabTitle() {
 		int index = tabbedPane.getSelectedIndex();
 
 		Component c = tabbedPane.getComponentAt(index);
 
 		String title = tabbedPane.getTitleAt(index);
 
 		return title;
 	}
 
 	/**
 	 * Versendet die Nachricht, aktualisiert die UI.
 	 */
 	protected void sendMessage(String toUser) {
 		JPanel p = (JPanel) tabbedPane.getSelectedComponent();
 
 		// TODO fragil - haengt damit direkt mit der ui struktur zusammen
 		JTextComponent timeline = (JTextComponent) ((JViewport) ((JScrollPane) p
 				.getComponent(0)).getComponent(0)).getComponent(0);
 		JTextComponent input = (JTextComponent) ((JViewport) ((JScrollPane) p
 				.getComponent(1)).getComponent(0)).getComponent(0);
 
 		final Message newMessage = new Message();
 
 		List<String> usernames = new ArrayList<String>();
 		usernames.add(toUser);
 		newMessage.setReceiver(usernames);
 		newMessage.setSender(Sim.getCurrentUser().getName());
 		newMessage.setText(input.getText());
 
 		if (toUser.equals(streamTabName)) { // Ist dies im Stremtab - nachricht
 											// an alle
 			newMessage.setMessageType(MessageType.ALL);
 			newMessage.getReceiver().clear();
 			List<User> users = ServiceLocator.getInstance()
 					.getRegistryService().getUsers();
 			for (User user : users) {
 				newMessage.getReceiver().add(user.getName());
 			}
 			System.out.println("Nachricht an alle");
 		}
 
 		String title = tabbedPane.getTitleAt(tabbedPane.getSelectedIndex());
 
 		if (!toUser.equals(streamTabName)) { // Ist dies im Stremtab - nachricht
 												// an alle
 
 			if (isUserOnline(toUser)) {
 				timeline.setText(getFormattedMessage(newMessage,
 						timeline.getText()));
 			} else {
 				outputStatusMessage("Tut mir leid, " + toUser + " ist offline",
 						timeline);
 			}
 
 		}
 
 		input.setText(null);
 
 		moveCaretDown((JEditorPane) timeline);
 
 		input.requestFocusInWindow();
 
 		// Versand wird asynchron ausgefuehrt, da potentiell langsam und droht
 		// die ui zu blocken.
 		// TODO Fehlerhandling, wenn versand fehlschlaegt?
 		// z.b. in einer liste Worker speichern und via Timer den return type
 		// checken (wenn worker.isDone() == true, isdone ist auch true, wenn
 		// gecancelt, falls einer false ist, eine Warnung in systray erzeugen.
 		// evtl den betroffenen User rot markieren (letzter Versand nicht
 		// erfolgreich)
 		SwingWorker<Void, Message> worker = new SwingWorker<Void, Message>() {
 
 			@Override
 			protected Void doInBackground() throws Exception {
 				EventDispatcher.fireEvent(Events.MESSAGE_SENT, newMessage);
 				return null;
 			}
 
 			@Override
 			protected void done() {
 				// TODO Auto-generated method stub
 				super.done();
 			}
 		};
 
 		worker.execute();
 	}
 
 	/**
 	 * Gibt zurueck ob user online ist oder nicht.
 	 * 
 	 * @param user
 	 *            username
 	 * @return return
 	 */
 	public boolean isUserOnline(String user) {
 		return ComponentProvider.getInstance().getUserPanel().getCurrentUsers()
 				.contains(user);
 	}
 
 	/**
 	 * Empfaengt Events.
 	 */
 	@Override
 	public void eventReceived(Events event, Object o) {
 		// Ein user wurde per UserPanel ausgeawehlt, nun wird das Tab
 		// fokussiert.
 		if (Events.USER_SELECTED.equals(event)) {
 			String text = o.toString();
 
 			int index = tabbedPane.indexOfTab(text);
 			if (index == -1) {
 				addToTabPane(text, null);
 			}
 			focusTabPane(text);
 			// Neue Nachricht verarbeiten.
 		} else if (Events.MESSAGE_RECEIVED.equals(event)) {
 			if (o instanceof Message) {
 				Message m = (Message) o;
 				processNewMessage(m);
 			} else {
 				throw new IllegalArgumentException("Fuer Event "
 						+ Events.MESSAGE_RECEIVED
 						+ " muss eine Message als Object kkommen! ist aber: "
 						+ o.getClass());
 			}
 			// Zeigt die Tabbed pane mit dem entsprechenden Namen.
 		} else if (Events.SHOW_MSG_TABBED_PANE.equals(event)) {
 			String name = (String) o;
 			focusTabPane(name);
 			// Meldung dass Benutzer offline oder online ist
 		} else if (Events.USER_OFFLINE_NOTICE.equals(event)
 				|| Events.USER_ONLINE_NOTICE.equals(event)) {
 			String message = "User "
 					+ o
 					+ " ist jetzt "
 					+ (Events.USER_OFFLINE_NOTICE.equals(event) ? "offline"
 							: "online");
 			outputStatusMessage(message, nameTextAreaMap.get(streamTabName));
 
 			outputStatusMessage(message, (List<String>) o);
 
 		} else if (Events.MESSAGE_SEND_FAILED.equals(event)) {
 
 			final MessagingError error = (MessagingError) o;
 			if (MessageType.SINGLE.equals(error.getMessage().getMessageType())) {
 				// Muss per invokeLater auf dem Swing Event Dispatcher Thread
 				// ausgef�hrt werden siehe
 				// http://docs.oracle.com/javase/tutorial/uiswing/concurrency/dispatch.html
 				// http://tips4java.wordpress.com/2008/10/22/text-area-scrolling/
 				SwingUtilities.invokeLater(new Runnable() {
 
 					@Override
 					public void run() {
 
 						outputStatusMessage(
 								"Fehler: Message konnte nicht gesendet werden: "
 										+ error.getException().getMessage(),
 								nameTextAreaMap.get(error.getMessage()
 										.getReceiver().get(0)));
 					}
 				});
 			}
 		}
 	}
 
 	/**
 	 * Z Verarbeitung einer neuen Nachricht.
 	 * 
 	 * @param m
 	 *            Message
 	 */
 	protected void processNewMessage(Message m) {
 		String sender = m.getSender();
 
 		if (MessageType.ALL.equals(m.getMessageType())) {
 			sender = streamTabName;
 		}
 		if (MessageType.USER_STATUS.equals(m.getMessageType())) {
 			// Wenn eine Statusnachricht von sich selbst kommt, nicht
 			// verarbeiten
 			if (Sim.getCurrentUser().getName().equals(m.getSender())) {
 				return;
 			}
 
 			List<String> tabs = new ArrayList<String>();
 			tabs.add(streamTabName);
 			tabs.add(sender);
 
 			String statusMessage = getFormattedUserStatusMessage(m);
 			outputStatusMessage(statusMessage, tabs);
 		}
 		if (!MessageType.USER_STATUS.equals(m.getMessageType())) {
 			final JEditorPane textArea = nameTextAreaMap.get(sender);
 
 			if (textArea != null) {
 				String oldText = textArea.getText();
 				textArea.setText(getFormattedMessage(m, oldText));
 				moveCaretDown(textArea);
 			} else {
 				String textline = getFormattedMessage(m);
 				addToTabPane(sender, textline);
 			}
 
 			// Icon auf ungelesen setzten, falls tab derzeit nicht ausgewaehlt.
 			if (!isTabSelected(sender)) {
 				setIconToUnreadMessages(sender);
 			}
 		}
 
 		System.out.println("Message: " + m + ", received.");
 
 	}
 
 	/**
 	 * Gibt zurueck ob der Tab mit dem Namen derzeit ausgewaehlt ist oder nicht.
 	 * 
 	 * @param name
 	 *            Name des Tabs
 	 * @return true/false
 	 */
 	public boolean isTabSelected(String name) {
 		int selected = tabbedPane.getSelectedIndex();
 		int index = tabbedPane.indexOfTab(name);
 
 		return selected == index;
 	}
 
 	/**
 	 * Gibt eine Statusmessage aus
 	 * 
 	 * @param statusMessage
 	 *            beliebige Statusmeldung
 	 * @param textArea
 	 *            Zieltab f�r Statusmeldung
 	 */
 	protected void outputStatusMessage(String statusMessage,
 			JTextComponent textArea) {
 		String oldText = textArea.getText();
 		textArea.setText(getFormattedMessage("<i>" + statusMessage + "</i>",
 				oldText));
 	}
 
 	/**
 	 * GIbt die Nachricht in den Tabs aus.
 	 * 
 	 * @param statusMessage
 	 *            Nachricht
 	 * @param tabs
 	 *            Liste von Tabs
 	 */
 	protected void outputStatusMessage(String statusMessage, List<String> tabs) {
 		for (String tab : tabs) {
 			JEditorPane textArea = nameTextAreaMap.get(tab);
 			if (textArea != null) {
 				outputStatusMessage(statusMessage, textArea);
 			}
 		}
 	}
 
 	/**
 	 * Gibt die Message formatiert aus.
 	 * 
 	 * @param username
 	 *            Username
 	 * @param msg
 	 *            Nachricht
 	 * @param oldText
 	 *            vorhandener text in der text area
 	 * @return formatierter String
 	 */
 	protected String getFormattedMessage(Message m, String oldText) {
 		String message = "";
 		if (m != null) {
 			message += getFormattedMessage(m);
 		}
 		return getFormattedMessage(message, oldText);
 	}
 
 	/**
 	 * Gibt einen beliebigen Text formatiert aus.
 	 * 
 	 * @param message
 	 *            Nachricht als Text
 	 * @param oldText
 	 *            vorhandener Text in der Textarea
 	 * @return formatierter String
 	 */
 	protected String getFormattedMessage(String message, String oldText) {
 		StringBuffer newMsg = new StringBuffer(oldText);
 
 		if (oldText.contains("</body>")) {
 			int index = oldText.indexOf("</body>");
 			newMsg.insert(index, "<br />" + message);
 		}
 
 		return newMsg.toString();
 	}
 
 	/**
 	 * Gibt die Message formatiert aus.
 	 * 
 	 * @param username
 	 *            Username
 	 * @param msg
 	 *            Nachricht
 	 * @param oldText
 	 *            vorhandener text in der text area
 	 * @return formatierter String
 	 */
 	protected String getFormattedMessage(Message m) {
 		String text = StringEscapeUtils.escapeHtml4(m.getText());
 		String sender = m.getSender();
 		String colorHexValue = "#000000";
 		String[] chunks = text.split("\\s");
 		String linkPattern = "((file\\:|mailto\\:|(news|(ht|f)tp(s?))\\://){1}\\S+)";
 		StringBuffer msg = new StringBuffer();
 
 		/* Farbe des Benutzers ermitteln. */
 		colorHexValue = ComponentProvider.getInstance().getUserColorFactory()
 				.getOrReserveUserColor(sender).getHexValue();
 
 		for (String c : chunks) {
 			if (c.matches(linkPattern)) {
 				c = "<a href=\"" + c + "\">" + c + "</a>";
 			} else {
 				c = ComponentProvider.getInstance().getSmileyFactory()
 						.applySmiles(c);
 			}
 			msg.append(" ");
 			msg.append(c);
 		}
 
 		return "<span style=\"color:" + colorHexValue + "\">["
 				+ df.format(new Date()) + "] " + m.getSender() + "></span> "
 				+ msg.toString();
 	}
 
 	/**
 	 * Gibt die USerstatus message zurueck.
 	 * 
 	 * @param m
 	 *            Message
 	 * @return string msg.
 	 */
 	protected String getFormattedUserStatusMessage(Message m) {
 		String msg = m.getSender() + " ist jetzt ";
 		msg = msg + " " + m.getUserStatus().getText();
 		msg = "<i>" + msg + "</i>";
 
 		return msg;
 	}
 
 	/**
 	 * Fuegt ein SendReceiveMessage Tab fuer diese Konversation ein.
 	 * 
 	 * @param label
 	 *            Label des Tabs
 	 * @param text
 	 *            Text der in die TextArea geschrieben wird.
 	 */
 	protected void addToTabPane(final String label, String text) {
 		JPanel textPanel = new JPanel(new BorderLayout());
 
 		JScrollPane timelineScrollPane = createTimelineScrollpane(label, text);
 
 		textPanel.add(timelineScrollPane, BorderLayout.CENTER);
 
 		final JTextArea messageTextArea = new JTextArea(3, 70);
 		messageTextArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
 		messageTextArea.setWrapStyleWord(true);
 		messageTextArea.setLineWrap(true);
 
 		// Schickt die Nachricht auf Enter.
 		messageTextArea.addKeyListener(new KeyAdapter() {
 
 			@Override
 			public void keyPressed(KeyEvent e) {
 				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
 					sendMessage(label);
 				}
 			}
 		});
 
 		JScrollPane messageScrollPane = new JScrollPane(messageTextArea);
 
 		textPanel.add(messageScrollPane, BorderLayout.SOUTH);
 
 		tabbedPane.addTab(label, Sim.readIcon, textPanel);
 
 		tabbedPane.addChangeListener(new ChangeListener() {
 
 			/**
 			 * Beim Tabwechsel wird der Fokus auf das Eingabefeld gelegt.
 			 */
 			@Override
 			public void stateChanged(ChangeEvent e) {
 				JTabbedPane src = (JTabbedPane) e.getSource();
 				int index = src.getSelectedIndex();
 				String title = src.getTitleAt(index);
 
 				focusMessageTextArea(title);
 
 				setIconToReadMessages(title);
 			}
 		});
 
 		addKeyboardShortcuts(tabbedPane);
 
 		inputTextAreaMap.put(label, messageTextArea);
 	}
 
 	/**
 	 * Fuegt die Shortcuts ALT+1-9 zum tabpane hinzu. Damit kann man per alt+1-9
 	 * zwischen den tabs umschalten.
 	 * 
 	 * @param tabbedPane
 	 *            tabbedpane
 	 */
 	public void addKeyboardShortcuts(JTabbedPane tabbedPane) {
 		if (tabbedPane.getTabCount() >= 1) {
 			tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
 		}
 		if (tabbedPane.getTabCount() >= 2) {
 			tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);
 		}
 		if (tabbedPane.getTabCount() >= 3) {
 			tabbedPane.setMnemonicAt(2, KeyEvent.VK_3);
 		}
 		if (tabbedPane.getTabCount() >= 4) {
 			tabbedPane.setMnemonicAt(3, KeyEvent.VK_4);
 		}
 		if (tabbedPane.getTabCount() >= 5) {
 			tabbedPane.setMnemonicAt(4, KeyEvent.VK_5);
 		}
 		if (tabbedPane.getTabCount() >= 6) {
 			tabbedPane.setMnemonicAt(6, KeyEvent.VK_6);
 		}
 		if (tabbedPane.getTabCount() >= 7) {
 			tabbedPane.setMnemonicAt(7, KeyEvent.VK_7);
 		}
 		if (tabbedPane.getTabCount() >= 8) {
 			tabbedPane.setMnemonicAt(8, KeyEvent.VK_8);
 		}
 		if (tabbedPane.getTabCount() >= 9) {
 			tabbedPane.setMnemonicAt(9, KeyEvent.VK_9);
 		}
 
 	}
 
 	/**
 	 * Schiebt den Cursor ans Ende des Documents.
 	 * 
 	 * @param textArea
 	 *            textArea.
 	 */
 	public void moveCaretDown(final JEditorPane textArea) {
		textArea.setCaretPosition(textArea.getDocument().getLength());
 	}
 
 	/**
 	 * Erzeugt das timeline Scrollpane incl inhalt.
 	 * 
 	 * @param label
 	 *            Label des Tabs.
 	 * @param text
 	 *            Textinhalt.
 	 * @return
 	 */
 	protected JScrollPane createTimelineScrollpane(String label, String text) {
 		final JEditorPane timelineTextArea = new JEditorPane("text/html", text);
 		DefaultCaret caret = (DefaultCaret) timelineTextArea.getCaret();
 		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
 
 		timelineTextArea.setEditable(false);
 		timelineTextArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
 
 		timelineTextArea.addHyperlinkListener(new HyperlinkListener() {
 
 			/**
 			 * Bei Klick auf einen Link in der Timeline, wird der Browser
 			 * ge�ffnet.
 			 * 
 			 * @param e
 			 *            Event.
 			 */
 			@Override
 			public void hyperlinkUpdate(final HyperlinkEvent e) {
 
 				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
 
 					// Fenster asynchron �ffnen, sonst blockt ui.
 					SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
 
 						@Override
 						protected Void doInBackground() throws Exception {
 
 							try {
 								Desktop.getDesktop().browse(e.getURL().toURI());
 							} catch (Exception uri) {
 								// TODO Fehlerhandling, wen URI / Browser nicht
 								// geoeffnet werden konnte. Warnung anzeigen.
 								uri.printStackTrace();
 							}
 
 							return null;
 						}
 
 						@Override
 						protected void done() {
 							// TODO Auto-generated method stub
 							super.done();
 						}
 					};
 
 					worker.execute();
 
 				}
 			}
 		});
 		nameTextAreaMap.put(label, timelineTextArea);
 
 		JScrollPane timelineScrollPane = new JScrollPane(timelineTextArea);
 
 		return timelineScrollPane;
 	}
 
 	/**
 	 * fokussiert das tabpane mit dem Namen.
 	 * 
 	 * @param name
 	 *            Name des Panes.
 	 */
 	protected void focusTabPane(String name) {
 		int index = tabbedPane.indexOfTab(name);
 		if (index != -1) {
 			tabbedPane.setSelectedIndex(index);
 		}
 	}
 
 	/**
 	 * Fokussiert das Feld im Tab mit dem Namen.
 	 * 
 	 * @param name
 	 *            Name des Tabs.
 	 */
 	protected void focusMessageTextArea(String name) {
 		inputTextAreaMap.get(name).requestFocusInWindow();
 	}
 
 	/**
 	 * Setzt das Icon im Tab, so dass es auf ungelesene Nachrichten aufmerksam
 	 * macht.
 	 * 
 	 * @param name
 	 *            Name des Tabs.
 	 */
 	protected void setIconToUnreadMessages(String name) {
 		if (!unreadTabsList.contains(name)) {
 			unreadTabsList.add(name);
 		}
 		;
 		int index = tabbedPane.indexOfTab(name);
 		tabbedPane.setIconAt(index, Sim.unreadIcon);
 
 	}
 
 	/**
 	 * Entfernt das Icon vom Tab und setzt das standardicon readicon.
 	 * 
 	 * @param name
 	 *            Name des Tabs.
 	 */
 	protected void setIconToReadMessages(String name) {
 		unreadTabsList.remove(name);
 		int index = tabbedPane.indexOfTab(name);
 		tabbedPane.setIconAt(index, Sim.readIcon);
 	}
 }
