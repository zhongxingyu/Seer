 package clientQT;
 
 import games.GameType;
 import games.GameLogic;
 
 import java.util.Map;
 import java.util.HashMap;
 import java.util.ArrayList;
 import java.util.List;
 
 import action.Message;
 import action.UserAction;
 import action.UserActionLobbyMessage;
 import action.UserActionSetNick;
 import action.UserActionStartGame;
 
 import clientData.Client;
 import clientData.ServerListener;
 import clientData.GameSession;
 
 import com.trolltech.qt.core.Qt;
 import com.trolltech.qt.gui.QAction;
 import com.trolltech.qt.gui.QApplication;
 import com.trolltech.qt.gui.QCloseEvent;
 import com.trolltech.qt.gui.QLineEdit;
 import com.trolltech.qt.gui.QMenu;
 import com.trolltech.qt.gui.QMenuBar;
 import com.trolltech.qt.gui.QPushButton;
 import com.trolltech.qt.gui.QTextEdit;
 import com.trolltech.qt.gui.QSplitter;
 import com.trolltech.qt.gui.QVBoxLayout;
 import com.trolltech.qt.gui.QHBoxLayout;
 import com.trolltech.qt.gui.QInputDialog;
 import com.trolltech.qt.gui.QPixmap;
 import com.trolltech.qt.gui.QSizePolicy;
 import com.trolltech.qt.gui.QTableWidget;
 import com.trolltech.qt.gui.QTableWidgetItem;
 import com.trolltech.qt.gui.QTreeWidget;
 import com.trolltech.qt.gui.QTreeWidgetItem;
 import com.trolltech.qt.gui.QWidget;
 import com.trolltech.qt.gui.*; // Fuck it..
 import java.util.Arrays;
 
 public class LobbyWindow extends QWidget implements ServerListener
 	{
 	private QPushButton bNick;
 	
 	private QTreeWidget nickList;
 	private Map<Integer,QTreeWidgetItem> nicks = new HashMap<Integer, QTreeWidgetItem>();
 	private QTableWidget gameList;
 	private QTextEdit chatHistory;
 	private QLineEdit tfEditLine;
 	
 	private QMenuBar menuBar;
 	private QMenu miNewGame;
 	
 	private Client client;
 	
 	public LobbyWindow(Client client)
 		{
 		this.client=client;
 
 		QHBoxLayout lobbyLayout=new QHBoxLayout();
 		//QSplitter lobbyLayout=new QSplitter();
 		//lobbyLayout.setOrientation(Qt.Orientation.Horizontal);
 		QVBoxLayout chatWindowLayout=new QVBoxLayout();
 		QHBoxLayout chatInputLayout=new QHBoxLayout();
 		
 		QSplitter chatAndSidebar = new QSplitter();
 		QSplitter nickAndGame = new QSplitter();
 		
 		nickAndGame.setOrientation(Qt.Orientation.Vertical);
 		chatAndSidebar.setOrientation(Qt.Orientation.Horizontal);
 
 		bNick=new QPushButton(client.getNick()+":", this);
 		bNick.clicked.connect(this, "actionChangeNick()");
 		bNick.setSizePolicy(QSizePolicy.Policy.Minimum, QSizePolicy.Policy.Minimum);
 		chatHistory=new QTextEdit(this);
 		chatHistory.setReadOnly(true);
 		
 		tfEditLine=new QLineEdit(this);
 		tfEditLine.returnPressed.connect(this,"actionSendMessage()");
 
 		nickList=new QTreeWidget();
 		nickList.header().close();
 		
 		gameList=new QTableWidget();
 		gameList.setColumnCount(3);
 		gameList.setColumnWidth(0,30);
 		gameList.setShowGrid(false);
 		// TODO: Why the fuck wont this change the header...
		gameList.setHorizontalHeaderLabels(Arrays.asList("#","Type","Session"));
		//gameList.setHorizontalHeaderItem(0,new QTableWidgetItem("#"));
		//gameList.setHorizontalHeaderItem(1,new QTableWidgetItem("Type"));
		//gameList.setHorizontalHeaderItem(2,new QTableWidgetItem("Session"));
 		
 		// Lobby layout
 		setLayout(lobbyLayout);
 		//lobbyLayout.addLayout(chatWindowLayout);
 		//lobbyLayout.addWidget(nickAndGameLayout);
 		lobbyLayout.addWidget(chatAndSidebar);
 		QWidget temp = new QWidget();
 		temp.setLayout(chatWindowLayout); // This gives extra padding.
 		temp.setContentsMargins(0, 0, 0, 0);
 		
 		chatWindowLayout.setContentsMargins(0,0,0,0);
 		//lobbyLayout.setContentsMargins(0, 0,0,0);
 		//chatAndSidebar.setContentsMargins(0, 0, 0, 0);
 		
 		chatAndSidebar.addWidget(temp);
 		chatAndSidebar.addWidget(nickAndGame);
 		chatWindowLayout.addWidget(chatHistory);
 		chatWindowLayout.addLayout(chatInputLayout);
 		chatInputLayout.addWidget(bNick);
 		chatInputLayout.addWidget(tfEditLine);
 		nickAndGame.addWidget(nickList);
 		nickAndGame.addWidget(gameList);
 		
 		setWindowTitle("Lobby");
 		
 		menuBar=new QMenuBar();
 		lobbyLayout.setMenuBar(menuBar);
 		buildMenuBar();
 		}
 
 	public void buildMenuBar()
 		{
 		QAction miExit = new QAction(tr("E&xit"), this);
 		//miExit.setShortcuts(QKeySequence.Quit);
 		miExit.setStatusTip(tr("Exit the application"));
 		miExit.triggered.connect(this, "actionExit()");
 
 		QAction miConnect = new QAction(tr("Connect"), this);
 		miConnect.setStatusTip(tr("Connect to server"));
 		miConnect.triggered.connect(this, "actionConnect()");
 
 		QAction miDisconnect = new QAction(tr("Disconnect"), this);
 		miDisconnect.setStatusTip(tr("Disconnect to server"));
 		miDisconnect.triggered.connect(this, "actionDisconnect()");
 
 		QAction miAbout = new QAction(tr("About"), this);
 		miAbout.setStatusTip(tr("About this program"));
 		miAbout.triggered.connect(this, "actionAbout()");
 
 		QMenu mClient = menuBar.addMenu("&Client");
 		mClient.addAction(miConnect);
 		mClient.addAction(miDisconnect);
 		mClient.addAction(miExit);
 		miNewGame = menuBar.addMenu("&New");
 
 
 		QMenu helpMenu = menuBar.addMenu(tr("&Help"));
 		helpMenu.addAction(miAbout);
 		
 		}
 
 	public void eventServerMessage(Message msg)
 		{
 		for(UserAction action:msg.actions)
 			if(action instanceof UserActionLobbyMessage)
 				showLobbyMessage((UserActionLobbyMessage)action);
 		
 		}
 
 	
 	public void actionExit()
 		{
 		System.out.println("Exiting...");
 		client.disconnectFromServer();
 		System.exit(0);
 		}
 
 	public void actionAbout()
 		{
 		//TODO show about dialog.
 		}
 	
 	public void actionSendMessage()
 		{
 		String text=tfEditLine.text();
 		if(!text.equals(""))
 			{
 			tfEditLine.setText("");
 			client.send(new Message(new UserActionLobbyMessage(text)));
 			}
 		}
 	
 	public void actionChangeNick()
 		{
 		String text = QInputDialog.getText(this, "New nick", "Nick:", QLineEdit.EchoMode.Normal, client.getNick());
 		if (text != null)
 			client.send(new Message(new UserActionSetNick(text)));
 		}
 	
 	
 	public void actionConnect()
 		{
 		new ConnectToServerDialog(client).show();
 		}
 	
 	public void actionDisconnect()
 		{
 		client.disconnectFromServer();
 		}
 	
 	public void closeEvent(QCloseEvent e)
 		{
 		actionExit();
 		}
 	
 
 	public void showStatusMessage(final String str)
 		{
 		QApplication.invokeLater(new Runnable() {
 			public void run() {
 				chatHistory.append("## "+str);
 			}
 			});
 		}
 	
 	public void showLobbyMessage(final UserActionLobbyMessage lm)
 		{
 		QApplication.invokeLater(new Runnable() {
 			public void run() {
 				chatHistory.append(client.getNickFor(lm.fromClientID)+": "+lm.message);
 			}
 			});
 		}
 	
 	public void setNickList()
 		{
 		nickList.clear();
 		nicks.clear();
 		for(Map.Entry<Integer,String> u:client.mapClientIDtoNick.entrySet())
 			{
 			List<String> usertexts = new ArrayList<String>(1); // Just nick (perhaps nick + comment?)
 			usertexts.add(u.getValue());
 			QTreeWidgetItem user = new QTreeWidgetItem(usertexts);
 			nickList.insertTopLevelItem(0, user);
 			nicks.put(u.getKey(), user);
 			}
 		setGameSessions();
 		
 		}
 
 	public void setGameSessions()
 		{
 		gameList.clear();
 		gameList.setRowCount(client.gameSessions.size());
		gameList.setHorizontalHeaderLabels(Arrays.asList("#","Type","Session"));
 		System.out.println("game sessions: "+client.gameSessions);
 		
 		// Delete all old entries first. (Why is there no clear()?
 		for(QTreeWidgetItem w:nicks.values())
 			for(QTreeWidgetItem s:w.takeChildren())
 				w.removeChild(s);
 		
 		int i = 0;
 		for(GameSession g:client.gameSessions.values())
 			{
 			GameType gt=client.gameTypes.get(g.type);
 			if(gt==null)
 				System.out.println("Error: gametype does not exist "+gt); //This is a problem with the local connection
 			
 			// TODO: Store metadata in some way to allow sorting and such.. (perhaps use a tree view)
 			QTableWidgetItem newGameType=new QTableWidgetItem(gt.name);
 			QTableWidgetItem newGameUsersItem=new QTableWidgetItem( g.maxusers < 0 ? ""+g.joinedUsers.size() : ""+g.joinedUsers.size()+"/"+g.maxusers);
 			QTableWidgetItem newGameName=new QTableWidgetItem( g.sessionName );
 			gameList.setItem(i,0,newGameUsersItem);
 			gameList.setItem(i,1,newGameType);
 			gameList.setItem(i,2,newGameName);
 			i++;
 
 			// Maybe something like this?
 			List<String> temptext = new ArrayList<String>(1);
 			temptext.add(g.sessionName + " ("+gt.name+")"); // temptext.add(gt.name); and possible other data..
 			QTreeWidgetItem temp=new QTreeWidgetItem(temptext);
 			for(Integer u:g.joinedUsers)
 				nicks.get(u).addChild(temp);
 			}

 		}
 	
 	public void setGameTypes()
 		{
 		System.out.println("Filling in the list of available games. ("+client.gameTypes.size()+" in total).");
 		miNewGame.clear();
 		for(Map.Entry<Class<? extends GameLogic>, GameType> g:client.gameTypes.entrySet())
 			{
 			final Class<? extends GameLogic> cl=g.getKey();
 			final GameType gt=g.getValue();
 			QAction menuaction = new QAction(gt.name, miNewGame);
 			miNewGame.addAction(menuaction);
 			menuaction.triggered.connect(new Runnable()
 				{
 					public void run()
 						{
 						System.out.println("new game "+cl);
 						// TODO Auto-generated method stub
 						
 						UserActionStartGame a=new UserActionStartGame();
 						a.sessionName = QInputDialog.getText(null, "Session name", gt.description+"\nSession name:", QLineEdit.EchoMode.Normal, "My game");
 						if (a.sessionName == null)
 							return; // Then no new game!
 						a.game=cl;
 						client.send(new Message(a));
 						
 						
 						}
 				}, "run()");
 			menuaction.setIconVisibleInMenu(true); // TODO: Make some icons perhaps?
 			menuaction.setToolTip(gt.description); // TODO: Doesn't seem to show up?
 			menuaction.setIcon(QPixmap.fromImage(new QImage("./cards/playingcard_icon.png")));
 			}
 		}
 
 	
 	@Override
 	public void eventNewUserList()
 		{
 		System.out.println("list of nicks: "+client.mapClientIDtoNick);
 		QApplication.invokeLater(new Runnable() {
 			public void run() {
 				setNickList();
 			}
 		});
 
 		//Update our nick
 		final String ourNick=client.getNickFor(client.getClientID());
 		QApplication.invokeLater(new Runnable() {
 			public void run() {
 				bNick.setText(ourNick+":");
 			}
 		});
 
 		}
 
 	@Override
 	public void eventNewGameSessions()
 		{
 		System.out.println("New game list...");
 		QApplication.invokeLater(new Runnable() {
 			public void run() {
 				setGameTypes(); // TODO: This shouldn't be here.
 				setGameSessions();
 			}
 		});
 		}
 
 	
 	//TODO on exit, stop the thread and properly shut down the game
 	}
