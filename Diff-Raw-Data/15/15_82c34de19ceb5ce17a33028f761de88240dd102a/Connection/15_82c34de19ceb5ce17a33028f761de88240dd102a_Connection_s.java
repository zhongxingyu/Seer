 package connection;
 
 import java.io.IOException;
 import java.util.LinkedList;
 
 import javax.net.ssl.SSLSocketFactory;
 
 import listeners.DisconnectListener;
 import listeners.MessageListener;
 import listeners.RoomListener;
 import listeners.ScriptListener;
 import listeners.ServerListener;
 import listeners.UserListener;
 import lombok.Getter;
 
 import org.apache.log4j.Logger;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CTabFolder;
 import org.eclipse.swt.custom.CTabItem;
 import org.eclipse.swt.custom.ScrolledComposite;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.TableItem;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.swt.widgets.TreeItem;
 import org.eclipse.wb.swt.layout.grouplayout.GroupLayout;
 import org.eclipse.wb.swt.layout.grouplayout.LayoutStyle;
 import org.pircbotx.Channel;
 import org.pircbotx.PircBotX;
 import org.pircbotx.exception.IrcException;
 import org.pircbotx.exception.NickAlreadyInUseException;
 import org.pircbotx.hooks.ListenerAdapter;
 import org.pircbotx.hooks.managers.ListenerManager;
 
 import scripting.ScriptVars;
 import shared.NSAlertBox;
 import shared.RoomManager;
 import ui.room.Room;
 
 public class Connection extends Composite {
 	
 	private class ConnectionData extends ListenerAdapter<KEllyBot> {
 		
 		@Getter
 		private KEllyBot bot;
 		
 		@SuppressWarnings("unchecked")
 		public ConnectionData(KEllyBot bot, ConnectionSettings cs, Connection nc){
 		
 			this.bot = bot;
 
 			bot.setVersion(KEllyBot.VERSION);
 			bot.setVerbose(true);
 			bot.changeNick(cs.getNickname());
 			bot.setAutoNickChange(true);
 			bot.setName(cs.getNickname());
 			bot.setLogin(cs.getIdent());
 			
 			ListenerManager<PircBotX> l = bot.getListenerManager();
 			l.addListener(this);
 			l.addListener(new RoomListener(nc));
 			l.addListener(new ServerListener(nc));
 			l.addListener(new UserListener(nc));
 			l.addListener(new MessageListener(nc));
 			l.addListener(new ScriptListener(nc));
 			l.addListener(new DisconnectListener(nc));
 
 			//connecting to server
 			try {
 				attemptToConnect(cs);
 			} catch (Exception e){
 				Logger dLog = Logger.getLogger("log.error");
 				dLog.error("Error during the connection process.", e);
 			}
 			//identify nick
 			if(!cs.getNickPassword().equals(""))
 			{
 				bot.identify(cs.getNickPassword());
 			}
 			
 			//auto joining channels
 			for(String channel:cs.getAutoJoin())
 			{
 				bot.joinChannel(channel);
 			}
 		}
 		
 		private void attemptToConnect(ConnectionSettings cs)
 		{
 			final ConnectionSettings CS = cs;
 			new Thread(new Runnable(){
 
 				public void run() {
 					boolean connected = false;
 					int tries = 0;
 					while(!connected && tries < 3)
 					try {
 						tries++;
 						if(!CS.getServerPassword().equals(""))
 							bot.connect(CS.getServer(), Integer.parseInt(CS.getPort()),CS.getServerPassword());
 						else if(CS.isSsl())
 							bot.connect(CS.getServer(), Integer.parseInt(CS.getPort()),SSLSocketFactory.getDefault());
 						else
 						bot.connect(CS.getServer(), Integer.parseInt(CS.getPort()));
 						connected = true;
 					} catch (NumberFormatException e) {
 						Logger dLog = Logger.getLogger("log.error");
 						dLog.error("Improper port, not a number", e);
 						NSAlertBox alert = new NSAlertBox("Connection failed", "The port was not a number. Please use a number 0-65535", SWT.ICON_ERROR, SWT.OK);
 					} catch (NickAlreadyInUseException e) {
 					} catch (IOException e) {
 						Logger dLog = Logger.getLogger("log.error");
 						dLog.error("IOException while trying to connect", e);
 					} catch (IrcException e) {
 						Logger dLog = Logger.getLogger("log.error");
 						dLog.error("IrcException while trying to connect", e);
 					}
 				}
 				
 			}).run();
 			
 		}
 	}
 	
 	@Getter 
 	private ScrolledComposite scrolledComposite;
 	@Getter 
 	private KEllyBot bot;
 	@Getter 
 	private ConnectionData data;
 	@Getter 
 	private ConnectionSettings cs;
 	private Tree chanList;
 	private LinkedList<Room> rooms = new LinkedList<Room>();
 	
 	/**
 	 * Create the composite.
 	 * @param parent
 	 * @param style
 	 */
 	public Connection(Composite parent, int style, ConnectionSettings cs) {
 		super(parent, style);
 
 		
 		CTabItem c = new CTabItem((CTabFolder) parent, SWT.NONE);
 		c.setText(cs.getConnectionName());
 		c.setControl(this);
 		
 		chanList = new Tree(this, SWT.BORDER);
 		
 		scrolledComposite = new ScrolledComposite(this, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
 		scrolledComposite.setExpandHorizontal(true);
 		scrolledComposite.setExpandVertical(true);
 		GroupLayout groupLayout = new GroupLayout(this);
 		groupLayout.setHorizontalGroup(
 			groupLayout.createParallelGroup(GroupLayout.LEADING)
 				.add(groupLayout.createSequentialGroup()
 					.add(chanList, 150, 150, 150)
 					.addPreferredGap(LayoutStyle.RELATED)
 					.add(scrolledComposite, GroupLayout.DEFAULT_SIZE, 359, Short.MAX_VALUE))
 		);
 		groupLayout.setVerticalGroup(
 			groupLayout.createParallelGroup(GroupLayout.LEADING)
 				.add(chanList, GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
 				.add(scrolledComposite, GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
 		);
 		setLayout(groupLayout);
 		
 		chanList.addListener(SWT.MouseDown, new Listener () {
 			@Override
 			public void handleEvent (org.eclipse.swt.widgets.Event event) {
 				Point point = new Point (event.x, event.y);
 				final TreeItem item = chanList.getItem (point);
 
 				if (item != null && item.getData()!=null) {
 					switchComposite((Room) item.getData());
 				}
 			}
 		});	
 		//FAKE TOOLTIP STUFF
 		final Listener labelListener = new Listener () {
 			public void handleEvent (Event event) {
 				Label label = (Label)event.widget;
 				Shell shell = label.getShell ();
 				switch (event.type) {
 					case SWT.MouseDown:
						Event e = new Event ();
						e.item = (TreeItem) label.getData ("_TREEITEM");
						chanList.setSelection((TreeItem)e.item);
//						chanList.notifyListeners (SWT.MouseDown, e);
						if (e.item != null && e.item.getData()!=null) 
							switchComposite((Room) e.item.getData());
 						shell.dispose ();
						chanList.setFocus();
 						break;
 					case SWT.MouseExit:
 						shell.dispose ();
 						break;
 				}
 			}
 		};
 		
 		Listener treeListener = new Listener () {
 			Shell tip = null;
 			Label label = null;
 			public void handleEvent (Event event) {
 				switch (event.type) {
 					case SWT.Dispose:
 					case SWT.KeyDown:
 					case SWT.MouseMove: {
 						if (tip == null) break;
 						tip.dispose ();
 						tip = null;
 						label = null;
 						break;
 					}
 					case SWT.MouseHover: {
 						TreeItem item = chanList.getItem (new Point (event.x, event.y));
 						if (item != null) {
 							if (tip != null  && !tip.isDisposed ()) tip.dispose ();
 							tip = new Shell (RoomManager.getMain().getShell(), SWT.ON_TOP | SWT.NO_FOCUS | SWT.TOOL);
 							tip.setBackground (RoomManager.getMain().getDisplay().getSystemColor (SWT.COLOR_INFO_BACKGROUND));
 							FillLayout layout = new FillLayout ();
 							layout.marginWidth = 2;
 							tip.setLayout (layout);
 							label = new Label (tip, SWT.NONE);
 							label.setForeground (RoomManager.getMain().getDisplay().getSystemColor (SWT.COLOR_INFO_FOREGROUND));
 							label.setBackground (RoomManager.getMain().getDisplay().getSystemColor (SWT.COLOR_INFO_BACKGROUND));
 							label.setData ("_TREEITEM", item);
 							label.setText (findRoom(item.getText()).getToolTipText());
 							label.addListener (SWT.MouseExit, labelListener);
 							label.addListener (SWT.MouseDown, labelListener);
 							Point size = tip.computeSize (SWT.DEFAULT, SWT.DEFAULT);
 							Rectangle rect = item.getBounds (0);
 							Point pt = chanList.toDisplay (rect.x, rect.y);
 							tip.setBounds (pt.x, pt.y, size.x, size.y);
 							tip.setVisible (true);
 						}
 					}
 				}
 			}
 		};
 		chanList.addListener (SWT.Dispose, treeListener);
 		chanList.addListener (SWT.KeyDown, treeListener);
 		chanList.addListener (SWT.MouseMove, treeListener);
 		chanList.addListener (SWT.MouseHover, treeListener);
 		
 		
 		//END OF FAKE TOOLTIP STUFF
 		this.bot = new KEllyBot(this);
 		this.data = new ConnectionData(this.bot, cs, this);
 		this.cs = cs;
 		
 		createRoom("Console", Room.IO);
 		RoomManager.getMain().getContainer().setSelection(c);
 	}
 
 	@Override
 	protected void checkSubclass() {
 		// Disable the check that prevents subclassing of SWT components
 	}
 
 	public void switchComposite(Room c){
 		scrolledComposite.setContent(c);
 		ScriptVars.curChannel = c.getCChannel().getChannel();
 		ScriptVars.curConnection = c.getBot();
 		c.updateTopic();
 		c.updateWho();
 	}
 	
 	public void createRoom(String s, int layout) {
 		createRoom(s,layout,null);
 	}
 
 	/**Create a new room based on a channel name. This does NOT need to lead to an actual channel, 
 	 * as it just handles creation of the window itself. 
 	 * 
 	 * @param channel the name of the ctabitem that will be created
 	 */
 	public void createRoom(String chanstr, int layout, Channel chan) {
 		RoomManager.createRoom((Composite)scrolledComposite,chanList,SWT.NONE,chanstr,this,layout,chan);
 	}
 	
 	public void addRoom(Room r){
 		rooms.add(r);
 		
 	}
 	
 	public void updateWho(String channel) {
 		Room r = findRoom(channel);
 		if(r!=null){
 			r.updateWho();
 		}
 	}
 	
 	public void updateTopic(String channel) {
 		Room r = findRoom(channel);
 		if(r!=null){
 			r.updateTopic();
 		}
 	}
 	
 	public Room findRoom(String channel) {
 		for(Room r : rooms){
 			if(r.getCChannel().getChannelString().equals(channel)) {
 				return r;
 			}
 		}
 		return null;
 	}
 	
 	public boolean canAddRoom(String s){
 		for(Room r : rooms){
 			if(r.getCChannel().
 					getChannelString().
 					equals(s)){
 				return false;
 			}
 		}
 		return true;
 	}
 }
