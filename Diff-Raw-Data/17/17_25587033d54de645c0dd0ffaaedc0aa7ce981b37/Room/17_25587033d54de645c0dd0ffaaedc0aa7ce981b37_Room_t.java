 /*
  * @author Kyle Kemp
  */
 package ui.room;
 
 import java.io.BufferedWriter;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 
 import lombok.Data;
 import lombok.EqualsAndHashCode;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.ExtendedModifyEvent;
 import org.eclipse.swt.custom.ExtendedModifyListener;
 import org.eclipse.swt.custom.MovementEvent;
 import org.eclipse.swt.custom.MovementListener;
 import org.eclipse.swt.custom.StyleRange;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.custom.VerifyKeyListener;
 import org.eclipse.swt.events.KeyAdapter;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.events.VerifyEvent;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.RGB;
 import org.eclipse.swt.program.Program;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.swt.widgets.TreeItem;
 import org.eclipse.wb.swt.layout.grouplayout.GroupLayout;
 import org.eclipse.wb.swt.layout.grouplayout.GroupLayout.ParallelGroup;
 import org.eclipse.wb.swt.layout.grouplayout.GroupLayout.SequentialGroup;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Attributes;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.parser.Tag;
 import org.pircbotx.Channel;
 import org.pircbotx.Colors;
 import org.pircbotx.User;
 
 import scripting.Script;
 import scripting.ScriptManager;
 import shared.ControlCodeParser;
 import shared.Customs;
 import shared.Message;
 import shared.Quicklinks;
 import shared.RoomManager;
 import shared.SWTResourceManager;
 import connection.Connection;
 import connection.KEllyBot;
 import connection.Settings;
 
 /* (non-Javadoc)
  * @see java.lang.Object#hashCode()
  */
 @EqualsAndHashCode(callSuper = false)
 @Data
 public class Room extends Composite {
 
 	/** The current listener. */
 	private WhoListener curListener;
 
 	/**
 	 * The listener interface for receiving who events. The class that is interested in processing a
 	 * who event implements this interface, and the object created with that class is registered
 	 * with a component using the component's <code>addWhoListener<code> method. When
 	 * the who event occurs, that object's appropriate
 	 * method is invoked.
 	 * 
 	 * @see WhoEvent
 	 */
 	private class WhoListener implements Listener {
 
 		/*
 		 * (non-Javadoc)
 		 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
 		 */
 		public void handleEvent(Event event) {
 			Point point = new Point(event.x, event.y);
 			final TreeItem item = who.getItem(point);
 
 			if (item != null && item.getData() != null
 					&& item.getData() instanceof User) {
 				Menu m = new Menu(item.getParent().getShell(), SWT.POP_UP);
 				basicItems(item, m);
 				ctcpItems(item, m);
 				operatorItems(item, m);
 				customItems(item, m);
 				item.getParent().setMenu(m);
 			}
 		}
 
 		/**
 		 * Custom items.
 		 * 
 		 * @param item
 		 *            the item
 		 * @param m
 		 *            the m
 		 */
 		private void customItems(final TreeItem item, Menu m) {
 			final Script[] contextScripts = findContextScript();
 			if (contextScripts == null || contextScripts.length == 0)
 				return;
 			for (final Script contextScript : contextScripts) {
 				Object[] arr = contextScript.invoke("getContextCommands");
 				for (final Object s : arr) {
 					MenuItem mitem = new MenuItem(m, SWT.PUSH);
 					mitem.setText((String) s);
 					mitem.addSelectionListener(new SelectionListener() {
 
 						@Override
 						public void widgetDefaultSelected(SelectionEvent arg0) {
 						}
 
 						@Override
 						public void widgetSelected(SelectionEvent arg0) {
 							contextScript.invoke((String) s, getBot(),
 									item.getData(), cChannel.getChannel());
 						}
 					});
 				}
 			}
 		}
 
 		/**
 		 * Find context script.
 		 * 
 		 * @return the script[]
 		 */
 		private Script[] findContextScript() {
 			ArrayList<Script> scripts = new ArrayList<Script>();
 			for (Script s : ScriptManager.scripts) {
 				if (s.getFunctions().contains("getContextCommands")) {
 					scripts.add(s);
 				}
 			}
 			return scripts.toArray(new Script[0]);
 		}
 
 		/**
 		 * Ctcp items.
 		 * 
 		 * @param item
 		 *            the item
 		 * @param m
 		 *            the m
 		 */
 		private void ctcpItems(TreeItem item, Menu m) {
 			final User tUser = (User) item.getData();
 
 			Menu parent = new Menu(m);
 			MenuItem mitem = new MenuItem(m, SWT.CASCADE);
 			mitem.setText("CTCP");
 			mitem.setMenu(parent);
 			createCTCP(tUser, parent, "CLIENTINFO");
 			createCTCP(tUser, parent, "FINGER");
 			createCTCP(tUser, parent, "PING");
 			createCTCP(tUser, parent, "TIME");
 			createCTCP(tUser, parent, "USERINFO");
 			createCTCP(tUser, parent, "VERSION");
 		}
 
 		/**
 		 * Creates the ctcp.
 		 * 
 		 * @param tUser
 		 *            the t user
 		 * @param parent
 		 *            the parent
 		 * @param command
 		 *            the command
 		 */
 		private void createCTCP(final User tUser, Menu parent,
 				final String command) {
 			MenuItem mitem = new MenuItem(parent, SWT.PUSH);
 			mitem.setText(command);
 			mitem.addSelectionListener(new SelectionListener() {
 
 				@Override
 				public void widgetDefaultSelected(SelectionEvent arg0) {
 				}
 
 				@Override
 				public void widgetSelected(SelectionEvent arg0) {
 					bot.sendCTCPCommand(tUser, command);
 				}
 			});
 		}
 
 		/**
 		 * Basic items.
 		 * 
 		 * @param item
 		 *            the item
 		 * @param m
 		 *            the m
 		 */
 		private void basicItems(final TreeItem item, Menu m) {
 			MenuItem mitem = new MenuItem(m, SWT.PUSH);
 			mitem.setText("Query");
 			mitem.addSelectionListener(new SelectionListener() {
 
 				@Override
 				public void widgetDefaultSelected(SelectionEvent arg0) {
 				}
 
 				@Override
 				public void widgetSelected(SelectionEvent arg0) {
 					serverConnection.createRoom(item.getText(), IO);
 
 				}
 			});
 		}
 
 		/**
 		 * Operator items.
 		 * 
 		 * @param item
 		 *            the item
 		 * @param m
 		 *            the m
 		 */
 		private void operatorItems(final TreeItem item, Menu m) {
 			final Channel thisChan = cChannel.getChannel();
 			final User tUser = (User) item.getData();
 			if (bot.getUserBot().getChannelsOpIn().contains(thisChan)) {
 				if (!tUser.getChannelsOpIn().contains(thisChan)) {
 					MenuItem mitem = new MenuItem(m, SWT.PUSH);
 					mitem.setText("Op");
 					mitem.addSelectionListener(new SelectionListener() {
 
 						@Override
 						public void widgetDefaultSelected(SelectionEvent arg0) {
 						}
 
 						@Override
 						public void widgetSelected(SelectionEvent arg0) {
 							bot.op(thisChan, tUser);
 
 						}
 					});
 				}
 				if (!tUser.getChannelsVoiceIn().contains(thisChan)) {
 					MenuItem mitem = new MenuItem(m, SWT.PUSH);
 					mitem.setText("Voice");
 					mitem.addSelectionListener(new SelectionListener() {
 
 						@Override
 						public void widgetDefaultSelected(SelectionEvent arg0) {
 						}
 
 						@Override
 						public void widgetSelected(SelectionEvent arg0) {
 							bot.voice(thisChan, tUser);
 
 						}
 					});
 				}
 			}
 		}
 	}
 
 	// TODO: fake tooltips
 	// (http://dev.eclipse.org/viewcvs/viewvc.cgi/org.eclipse.swt.snippets/src/org/eclipse/swt/snippets/Snippet125.java?view=co)
 
 	// TODO: change to booleans
 	/** The constants that dictate what a room gets for data. */
 	public static final int WHO = 1, TOPIC = 2, IO = 4;
	
	boolean hasLoaded;
 
 	/** The c channel. */
 	private CustomChannel cChannel;
 
 	/** The server connection. */
 	private Connection serverConnection;
 
 	/** The bot. */
 	private KEllyBot bot;
 
 	/** The channel list item. */
 	private TreeItem chanListItem;
 
 	// make clickable links by changing the style and the data of the individual
 	// messages
 	/** The topic box. */
 	private StyledText output, input, topicBox;
 
 	/** The who list. */
 	private Tree who;
 
 	/** The room layout. */
 	private int roomLayout;
 
 	/** The customs. */
 	private Customs customs;
 
 	// determines color of TreeItem in the channel list
 	/** The status. */
 	private int status;
 
 	/** The Constant NAME_CALLED. */
 	public static final int NORMAL = 0, NEW_IRC_EVENT = 1, NEW_MESSAGE = 2,
 			NAME_CALLED = 3;
 
 	// first item is the newest
 	/** The last messages. */
 	private LinkedList<String> lastMessages;
 
 	/** The list index. */
 	private int listIndex;
 
 	// variables used with the tool tip
 	/** The user count. */
 	private int userCount;
 
 	/** The last message. */
 	private String lastMessage;
 
 	/** The channel name. */
 	private String channelName;
 
 	// TODO extract logging logic into separate class
 	/** The session. */
 	private long session = 0;
 
 	/** The alternate message. */
 	private boolean alternateMessage = false;
 
 	/**
 	 * Instantiates a new room.
 	 * 
 	 * @param c
 	 *            the c
 	 * @param style
 	 *            the style
 	 * @param layout
 	 *            the layout
 	 * @param tree
 	 *            the tree
 	 * @param channelstr
 	 *            the channelstr
 	 * @param newConnection
 	 *            the new connection
 	 * @param channel
 	 *            the channel
 	 */
 	public Room(Composite c, int style, int layout, Tree tree,
 			String channelstr, Connection newConnection, Channel channel) {
 		super(c, style);
 		this.channelName = channelstr;
 		setServerConnection(newConnection);
 		this.setBot(newConnection.getBot());
 		this.cChannel = new CustomChannel(tree, channelstr, newConnection,
 				channel, this);
 		customs = new Customs();
 		roomLayout = layout;
 		lastMessages = new LinkedList<String>();
 		listIndex = -1;
 		this.logMessage("");
 
 		for (TreeItem i : tree.getItems()) {
 			if (i.getData() == this) {
 				chanListItem = i;
 				break;
 			}
 		}
 
 		changeStatus(NORMAL);
 		instantiate(layout);
		hasLoaded=true;
 	}
 
 	/**
 	 * Instantiate.
 	 * 
 	 * @param layout
 	 *            the layout
 	 */
 	public void instantiate(int layout) {
 		MovementListener linkClickListener = new MovementListener() {
 			@Override
 			public void getNextOffset(MovementEvent arg0) {
 				String[] message = arg0.lineText.split(" ");
 				int offset = arg0.offset - arg0.lineOffset;
 				for (String s : message) {
 					if (arg0.lineText.indexOf(s) > offset
 							|| arg0.lineText.indexOf(s) + s.length() < offset)
 						continue;
 
 					if (s.contains("://")) {
 						Program.launch(s);
 					} else if (Quicklinks.hasQuicklink(s)) {
 						Program.launch(Quicklinks.getLink(s));
 					}
 				}
 			}
 
 			@Override
 			public void getPreviousOffset(MovementEvent arg0) {
 
 			}
 		};
 
 		if ((layout & TOPIC) != 0) {
 			topicBox = new StyledText(this, SWT.BORDER | SWT.WRAP);
 			topicBox.setEditable(true);
 			topicBox.setWordWrap(false);
 			topicBox.addVerifyKeyListener(new VerifyKeyListener() {
 
 				@Override
 				public void verifyKey(VerifyEvent e) {
 					// on pressing Enter, attempt to set the topic
 					if (e.character == SWT.CR) {
 						bot.setTopic(Room.this.getCChannel().getChannel(),
 								topicBox.getText());
 						e.doit = false;
 					}
 					// or add control codes
 					if (e.stateMask == SWT.CTRL) {
 						switch (e.keyCode) {
 						// Key combinations
 						case 'O':
 						case 'o': // Insert Normal (kills all formatting)
 							insertCode("\u000f");
 							break;
 						case 'B':
 						case 'b': // Insert Bold
 							insertCode("\u0002");
 							break;
 						case 'U':
 						case 'u': // Insert Underlin
 							insertCode("\u001f");
 							break;
 						case 'I':
 						case 'i': // Insert Italic
 							insertCode("\u0016");
 							break;
 						case 'K':
 						case 'k': // Insert Color
 							insertCode("\u0003");
 							break;
 
 						case 'A':
 						case 'a': // Select all
 							input.selectAll();
 							break;
 						}
 					}
 				}
 
 				private void insertCode(String insertCode) {
 					int insertPos = topicBox.getCaretOffset();
 					topicBox.replaceTextRange(insertPos, 0, insertCode);
 					topicBox.setCaretOffset(insertPos + 1);
 				}
 			});
 			topicBox.addWordMovementListener(linkClickListener);
 		}
 		if ((layout & IO) != 0) {
 			// set up the output window
 			output = new StyledText(this, SWT.BORDER | SWT.V_SCROLL | SWT.WRAP
 					| SWT.MULTI);
 			output.setFont(SWTResourceManager.getFont("Courier New", 9,
 					SWT.NORMAL));
 			output.setForeground(customs.colors.get(Settings.getSettings()
 					.getOutputColors().get(Message.MSG)));
 			output.setBackground(customs.colors.get(Settings.getSettings()
 					.getOutputColors().get(Settings.BACKGROUND)));
 			output.setEditable(false);
 
 			output.addWordMovementListener(linkClickListener);
 			// if key pressed while output box selected, move to input box
 			output.addKeyListener(new KeyAdapter() {
 				public void keyPressed(KeyEvent e) {
 					// TODO: Make this accept more than letters and digits,
 					// without letting it put weird characters in
 					if (e.stateMask != SWT.CTRL && e.stateMask != SWT.ALT
 							&& e.character != 0 && e.character != SWT.CR) {
 						input.append("" + e.character);
 						input.setSelection(input.getText().length());
 						input.setFocus();
 
 					}
 				}
 			});
 
 			// set up the input box and it's enter-key listener
 			input = new StyledText(this, SWT.BORDER);
 			input.setFont(SWTResourceManager.getFont("Courier New", 9,
 					SWT.NORMAL));
 			input.setForeground(customs.colors.get(Settings.getSettings()
 					.getOutputColors().get(Message.MSG)));
 			input.setBackground(customs.colors.get(Settings.getSettings()
 					.getOutputColors().get(Settings.BACKGROUND)));
 			input.addVerifyKeyListener(new VerifyKeyListener() {
 				@Override
 				public void verifyKey(VerifyEvent e) {
 					if (e.stateMask == SWT.CTRL && // Ignore adding an indent when hitting
 							(e.keyCode == 'I' || e.keyCode == 'i')) // Ctrl + I
 						e.doit = false;
 				}
 			});
 
 			input.addKeyListener(new KeyAdapter() {
 				@Override
 				public void keyPressed(KeyEvent e) {
 					// CR == Carriage Return == Enter
 					if (e.character == SWT.CR) {
 						if (input.getText().equals(""))
 							return;
 						if (cChannel != null && serverConnection != null) {
 							serverConnection.getBot().sendMessage(
 									channelName,
 									input.getText().replaceAll(
 											Message.NEW_LINE, ""));
 							lastMessages.addFirst(input.getText());
 							listIndex = -1;
 						}
 						input.setText("");
 
 					}
 					if (e.keyCode == SWT.ARROW_UP && !lastMessage.isEmpty()) {
 						listIndex++;
 						if (listIndex >= lastMessages.size())
 							listIndex = 0;
 						input.setText(lastMessages.get(listIndex));
 					}
 					if (e.keyCode == SWT.ARROW_DOWN && !lastMessage.isEmpty()) {
 						listIndex--;
 						if (listIndex < 0)
 							listIndex = lastMessages.size() - 1;
 						input.setText(lastMessages.get(listIndex));
 
 					}
 
 					if (e.stateMask == SWT.CTRL) {
 						switch (e.keyCode) {
 						// Key combinations
 						case 'O':
 						case 'o': // Insert Normal (kills all formatting)
 							insertCode("\u000f");
 							break;
 						case 'B':
 						case 'b': // Insert Bold
 							insertCode("\u0002");
 							break;
 						case 'U':
 						case 'u': // Insert Underlin
 							insertCode("\u001f");
 							break;
 						case 'I':
 						case 'i': // Insert Italic
 							insertCode("\u0016");
 							break;
 						case 'K':
 						case 'k': // Insert Color
 							insertCode("\u0003");
 							break;
 
 						case 'A':
 						case 'a': // Select all
 							input.selectAll();
 							break;
 						}
 					}
 				}
 
 				private void insertCode(String insertCode) {
 					int insertPos = input.getCaretOffset();
 					input.replaceTextRange(insertPos, 0, insertCode);
 					input.setCaretOffset(insertPos + 1);
 				}
 			});
 			// prevent selected text from disappearing when you hit enter
 			input.addExtendedModifyListener(new ExtendedModifyListener() {
 
 				public void modifyText(ExtendedModifyEvent e) {
 					String text = ((StyledText) e.widget).getText();
 					if (text.contains("" + SWT.CR))
 						((StyledText) e.widget).setText((text.substring(0,
 								e.start) + e.replacedText + text
 								.substring(e.start)).replaceAll(
 								Message.NEW_LINE, ""));
 
 				}
 			});
 		}
 
 		// attempt to pass on the Ctrl+Tab and Ctrl+Shift+Tab traversal to the channel list
 		// BUH THIS DOESN'T WORK SOMEONE ELSE TRY IT.
 		// TraverseListener tabSwitcher = new TraverseListener(){
 		// public void keyTraversed(TraverseEvent e) {
 		// if(e.detail == SWT.TRAVERSE_TAB_NEXT)
 		// {
 		// Tree chanList = serverConnection.getChanList();
 		// chanList.traverse(SWT.TRAVERSE_ARROW_NEXT);
 		// }
 		// else if(e.detail == SWT.TRAVERSE_TAB_PREVIOUS)
 		// {
 		// Tree chanList = serverConnection.getChanList();
 		// chanList.traverse(SWT.TRAVERSE_ARROW_PREVIOUS);
 		// }
 		// }}};
 		// input.addTraverseListener(tabSwitcher);
 		// output.addTraverseListener(tabSwitcher);
 
 		if ((layout & WHO) != 0) {
 			who = new Tree(this, SWT.BORDER | SWT.V_SCROLL);
 			updateWhoListener();
 			who.addListener(SWT.MouseDoubleClick, new Listener() {
 
 				@Override
 				public void handleEvent(Event event) {
 					Point point = new Point(event.x, event.y);
 					TreeItem item = who.getItem(point);
 					if (item != null) {
 						serverConnection.createRoom(item.getText(), IO);
 					}
 				}
 			});
 
 		}
 
 		// generate the anchors for the windows
 		GroupLayout gl_composite = new GroupLayout(this);
 
 		SequentialGroup ss = gl_composite.createSequentialGroup();
 		ParallelGroup p2 = gl_composite
 				.createParallelGroup(GroupLayout.LEADING);
 
 		if ((layout & TOPIC) != 0) {
 			p2.add(topicBox, GroupLayout.DEFAULT_SIZE, 496, Short.MAX_VALUE);
 		}
 		if ((layout & IO) != 0) {
 			p2.add(output, GroupLayout.DEFAULT_SIZE, 496, Short.MAX_VALUE);
 			p2.add(input, GroupLayout.DEFAULT_SIZE, 496, Short.MAX_VALUE);
 		}
 		ss.add(p2);
 		if ((layout & WHO) != 0) {
 			ss.add(who, 150, 150, 150);
 		}
 		gl_composite.setHorizontalGroup(gl_composite.createParallelGroup(
 				GroupLayout.LEADING).add(ss));
 
 		ParallelGroup p = gl_composite.createParallelGroup(GroupLayout.LEADING);
 		SequentialGroup s = gl_composite.createSequentialGroup();
 
 		if ((layout & TOPIC) != 0) {
 			s.add(topicBox, GroupLayout.DEFAULT_SIZE, 15, 18);
 		}
 		if ((layout & IO) != 0) {
 			if ((layout & TOPIC) == 0) {
 				s.add(output, GroupLayout.DEFAULT_SIZE, 323, Short.MAX_VALUE);
 			} else {
 				s.add(output, GroupLayout.DEFAULT_SIZE, 308, Short.MAX_VALUE);
 			}
 			s.add(input);
 		}
 		p.add(s);
 		if ((layout & WHO) != 0) {
 			p.add(who, GroupLayout.DEFAULT_SIZE, 344, Short.MAX_VALUE);
 		}
 		gl_composite.setVerticalGroup(p);
 
 		this.setLayout(gl_composite);
 
 		// initial tab tool tip text setting
 		userCount = cChannel.getChannel() != null ? cChannel.getChannel()
 				.getUsers().size() : 0;
 		lastMessage = "N/A";
 		channelName = cChannel.getChannelString();
 
 		serverConnection.switchComposite(this);
 	}
 
 	/**
 	 * Update who listener.
 	 */
 	private void updateWhoListener() {
 		if (curListener != null) {
 			who.removeListener(SWT.MouseDown, curListener);
 		}
 		curListener = new WhoListener();
 		who.addListener(SWT.MouseDown, curListener);
 	}
 
 	/**
 	 * Update topic.
 	 */
 	public void updateTopic() {
 		if (topicBox == null)
 			return;
 		final String topic = this.getCChannel().getChannel().getTopic();
 		final String strippedTopic = Colors.removeFormattingAndColors(topic);
 		RoomManager.getMain().getDisplay().asyncExec(new Runnable() {
 			public void run() {
 				topicBox.setText(strippedTopic);
 
 				List<StyleRange> styleRanges = ControlCodeParser
 						.parseControlCodes(topic, topicBox.getText().length()
 								- strippedTopic.length());
 
 				for (StyleRange styleRange : styleRanges
 						.toArray(new StyleRange[styleRanges.size()]))
 					topicBox.setStyleRange(styleRange);
 
 				for (String s : strippedTopic.split(" ")) {
 					if (s.contains("://") || Quicklinks.hasQuicklink(s)) {
 						linkify(strippedTopic, s);
 					}
 				}
 
 				topicBox.setToolTipText(topic);
 			}
 
 			private void linkify(final String strippedTopic, String s) {
 				Color blue = new Color(topicBox.getDisplay(), 0, 0, 255);
 				StyleRange styleRange = new StyleRange();
 				styleRange.start = topicBox.getCharCount()
 						- strippedTopic.length() + strippedTopic.indexOf(s);
 				styleRange.length = s.length();
 				styleRange.foreground = blue;
 				styleRange.data = s;
 				styleRange.underline = true;
 				styleRange.underlineStyle = SWT.UNDERLINE_LINK;
 				topicBox.setStyleRange(styleRange);
 			}
 		});
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.swt.widgets.Control#getToolTipText()
 	 */
 	public String getToolTipText() {
 		String output = "";
 		output += "Room: " + channelName;
 		if ((roomLayout & Room.WHO) != 0)
 			output += "\nCurrent Users: " + userCount;
 		output += "\nLast Message: " + lastMessage;
 		return output;
 	}
 
 	/**
 	 * Update user count.
 	 */
 	private void updateUserCount() {
 		userCount = cChannel.getChannel().getUsers().size();
 	}
 
 	/**
 	 * Update last message.
 	 * 
 	 * @param lastMessage
 	 *            the last message
 	 */
 	public void updateLastMessage(String lastMessage) {
 		this.lastMessage = lastMessage;
 	}
 
 	/**
 	 * Update who.
 	 */
 	public void updateWho() {
 		if (who == null)
 			return;
 		final Room c = this;
 		RoomManager.getMain().getDisplay().asyncExec(new Runnable() {
 			public void run() {
 				updateUserCount();
 
 				getWho().removeAll();
 
 				Channel chan = c.getCChannel().getChannel();
 
 				Comparator<User> nickOrder = new Comparator<User>() {
 					@Override
 					public int compare(User u1, User u2) {
 						;
 						return u1.getNick().compareTo(u2.getNick());
 					}
 				};
 
 				if (chan.getOwners().size() > 0) {
 					TreeItem t = new TreeItem(c.getWho(), SWT.NONE);
 					t.setText("Owners");
 					List<User> users = new LinkedList<User>(chan.getOwners());
 					Collections.sort(users, nickOrder);
 					for (User u : users) {
 						UserTreeItem i = new UserTreeItem(t, SWT.NONE, u, c
 								.getCChannel());
 						i.getTree().setText(u.getNick());
 					}
 					t.setExpanded(true);
 				}
 				if (chan.getSuperOps().size() > 0) {
 					TreeItem t = new TreeItem(c.getWho(), SWT.NONE);
 					t.setText("Super-Ops");
 					List<User> users = new LinkedList<User>(chan.getSuperOps());
 					Collections.sort(users, nickOrder);
 					for (User u : users) {
 						UserTreeItem i = new UserTreeItem(t, SWT.NONE, u, c
 								.getCChannel());
 						i.getTree().setText(u.getNick());
 					}
 					t.setExpanded(true);
 				}
 				if (chan.getOps().size() > 0) {
 					TreeItem t = new TreeItem(c.getWho(), SWT.NONE);
 					t.setText("Ops");
 					List<User> users = new LinkedList<User>(chan.getOps());
 					Collections.sort(users, nickOrder);
 					for (User u : users) {
 						UserTreeItem i = new UserTreeItem(t, SWT.NONE, u, c
 								.getCChannel());
 						i.getTree().setText(u.getNick());
 					}
 					t.setExpanded(true);
 				}
 				if (chan.getHalfOps().size() > 0) {
 					TreeItem t = new TreeItem(c.getWho(), SWT.NONE);
 					t.setText("Half-Ops");
 					List<User> users = new LinkedList<User>(chan.getHalfOps());
 					Collections.sort(users, nickOrder);
 					for (User u : users) {
 						UserTreeItem i = new UserTreeItem(t, SWT.NONE, u, c
 								.getCChannel());
 						i.getTree().setText(u.getNick());
 					}
 					t.setExpanded(true);
 				}
 				if (chan.getVoices().size() > 0) {
 					TreeItem t = new TreeItem(c.getWho(), SWT.NONE);
 					t.setText("Voices");
 					List<User> users = new LinkedList<User>(chan.getVoices());
 					Collections.sort(users, nickOrder);
 					for (User u : users) {
 						UserTreeItem i = new UserTreeItem(t, SWT.NONE, u, c
 								.getCChannel());
 						i.getTree().setText(u.getNick());
 					}
 					t.setExpanded(true);
 				}
 
 				if (chan.getNormalUsers().size() > 0) {
 					TreeItem t = new TreeItem(c.getWho(), SWT.NONE);
 					t.setText("Normal");
 					List<User> users = new LinkedList<User>(chan
 							.getNormalUsers());
 					Collections.sort(users, nickOrder);
 					for (User u : users) {
 						UserTreeItem i = new UserTreeItem(t, SWT.NONE, u, c
 								.getCChannel());
 						i.getTree().setText(u.getNick());
 					}
 					t.setExpanded(true);
 				}
 			}
 		});
 	}
 
 	/**
 	 * Change status.
 	 * 
 	 * @param status
 	 *            the status
 	 */
 	public void changeStatus(int status) {
 		HashMap<Integer, RGB> roomColors = Settings.getSettings()
 				.getRoomStatusColors();
 		// if currently in focus, change back to normal;
 		if (serverConnection.getScrolledComposite().getContent() == this)
 			status = NORMAL;
 		switch (status) {
 		case NORMAL:
 			chanListItem.setForeground(SWTResourceManager.getColor(roomColors
 					.get(NORMAL)));
 			break;
 		case NEW_IRC_EVENT:
 			if (this.status < status) // if the new status is higher priority
 				chanListItem.setForeground(SWTResourceManager
 						.getColor(roomColors.get(NEW_IRC_EVENT)));
 			break;
 		case NEW_MESSAGE:
 			if (this.status < status)
 				chanListItem.setForeground(SWTResourceManager
 						.getColor(roomColors.get(NEW_MESSAGE)));
 			break;
 		case NAME_CALLED:
 			if (this.status < status)
 				chanListItem.setForeground(SWTResourceManager
 						.getColor(roomColors.get(NAME_CALLED)));
 			break;
 		}
 		// if(this.status < status)
 		this.status = status;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.swt.widgets.Widget#toString()
 	 */
 	public String toString() {
 		return channelName;
 
 	}
 
 	/**
 	 * New message.
 	 * 
 	 * @param s
 	 *            the s
 	 * @param update
 	 *            the update
 	 */
 	public void newMessage(String s, boolean update) {
 		output.append(s);
 		logMessage(s);
 		if (update) {
 			updateLastMessage(s);
 		}
 	}
 
 	/**
 	 * New message.
 	 * 
 	 * @param s
 	 *            the s
 	 */
 	public void newMessage(String s) {
 		newMessage(s, false);
 	}
 
 	/**
 	 * Log message.
 	 * 
 	 * @param s
 	 *            the s
 	 */
 	private void logMessage(String s) {
 		if (!Settings.getSettings().isChatLogs())
 			return;
 
 		if (getChannelName().equals(Connection.CONSOLE_ROOM))
 			return;
 
 		new File("logs/").mkdir();
 		new File("logs/" + getServerConnection().getBot().getServer() + "/")
 				.mkdir();
 
 		logMessageTxt(s);
 		logMessageHtml(s);
 	}
 
 	/**
 	 * Log message txt.
 	 * 
 	 * @param s
 	 *            the s
 	 */
 	private void logMessageTxt(String s) {
 		File file = new File(getDefaultLogPath() + ".txt");
 
 		createIfNotExist(file);
 
 		try (BufferedWriter writer = new BufferedWriter(new FileWriter(file,
 				true))) {
 			writer.write("[" + new java.util.Date() + "] " + s);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Gets the default log path.
 	 * 
 	 * @return the default log path
 	 */
 	private String getDefaultLogPath() {
 		return "logs/" + getServerConnection().getBot().getServer() + "/"
 				+ getChannelName();
 	}
 
 	/**
 	 * Log message html.
 	 * 
 	 * @param s
 	 *            the s
 	 */
 	private void logMessageHtml(String s) {
 
 		File file = new File(getDefaultLogPath() + ".html");
 
 		boolean needsIntro = false;
 		if (createIfNotExist(file)) {
 			needsIntro = true;
 		}
 
 		if (needsIntro) {
 			try (BufferedWriter writer = new BufferedWriter(new FileWriter(
 					file, true))) {
 				writer.write("<html><head><title>"
 						+ bot.getServer()
 						+ " - "
 						+ cChannel.getChannelString()
 						+ "</title><link rel=\"stylesheet\" type=\"text/css\" href=\"http://kellyirc.googlecode.com/svn/trunk/logstyle.css\" /</head><body></body></html>");
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		appendToHtml(s);
 
 	}
 
 	/**
 	 * Creates the if not exist.
 	 * 
 	 * @param logs
 	 *            the logs
 	 * @return true, if successful
 	 */
 	private boolean createIfNotExist(File logs) {
 		if (!logs.exists()) {
 			try {
 				logs.createNewFile();
 				return true;
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Append to html.
 	 * 
 	 * @param s
 	 *            the s
 	 */
 	public void appendToHtml(String s) {
 		Document doc = null;
 		try {
 			doc = Jsoup.parse(new File(getDefaultLogPath() + ".html"), "UTF-8");
 		} catch (IOException e) {
 		}
 		Element body = doc.select("body").first();
 		if (session == 0) {
 			session = System.currentTimeMillis();
 			body.appendElement("br");
 			Element sess = new Element(Tag.valueOf("div"), "", new Attributes());
 			sess.addClass("session");
 			sess.attr("id", String.valueOf(session));
 			sess.append("Session started on " + new java.util.Date());
 			body.appendChild(sess);
 			body.appendElement("br");
 			if (s.equals("")) {
 				saveLogFile(doc);
 				return;
 			}
 		}
 		Element cursession = doc.select("#" + session).first();
 		if (cursession == null)
 			return;
 
 		Element timestamp = new Element(Tag.valueOf("span"), "",
 				new Attributes());
 		timestamp.addClass("timestamp");
 		if (!s.equals(""))
 			timestamp.append(new java.util.Date().toString());
 
 		String userNick = s.trim().split(" ")[0];
 
 		if (s.startsWith("<" + this.getChannelName() + ">")
 				|| userNick.contains(".") || s.startsWith("<SYSTEM>")) {
 			Element system = new Element(Tag.valueOf("div"), "",
 					new Attributes());
 			system.addClass("system");
 
 			Element systemmsg = new Element(Tag.valueOf("span"), "",
 					new Attributes());
 			systemmsg.addClass("system-msg");
 			systemmsg.append(s.replaceAll("<", "&lt;"));
 
 			system.appendChild(timestamp);
 			system.appendChild(systemmsg);
 
 			body.appendChild(system);
 
 		} else {
 			// TODO use default user timestamp format
 			// TODO format links and quicklinks in html log
 			Element message = new Element(Tag.valueOf("div"), "",
 					new Attributes());
 			message.addClass("message" + (alternateMessage ? "-alt" : ""));
 
 			Element nick = new Element(Tag.valueOf("span"), "",
 					new Attributes());
 			nick.addClass("nick"
 					+ (userNick.contains(getBot().getNick()) ? "-me" : ""));
 			nick.append(userNick.replaceAll("<", "&lt;"));
 
 			message.appendChild(timestamp);
 			message.appendChild(nick);
 			message.append(Message.parseForLinks(s.substring(userNick.length()))
 					+ " ");
 
 			body.appendChild(message);
 		}
 		alternateMessage = !alternateMessage;
 
 		saveLogFile(doc);
 
 	}
 
 	/**
 	 * Save log file.
 	 * 
 	 * @param doc
 	 *            the doc
 	 */
 	private void saveLogFile(Document doc) {
 		File swap = new File(getDefaultLogPath() + ".html.swap");
 		try (FileWriter out = new FileWriter(swap)) {
 			out.write(doc.toString());
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		copy(swap, new File(getDefaultLogPath() + ".html"));
 		swap.delete();
 	}
 
 	/**
 	 * Copy.
 	 * 
 	 * @param file
 	 *            the file
 	 * @param output
 	 *            the output
 	 */
 	private void copy(File file, File output) {
 		try (FileInputStream from = new FileInputStream(file);
 				FileOutputStream to = new FileOutputStream(output)) {
 			byte[] buffer = new byte[4096];
 			int bytesRead;
 
 			while ((bytesRead = from.read(buffer)) != -1)
 				to.write(buffer, 0, bytesRead);
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 }
