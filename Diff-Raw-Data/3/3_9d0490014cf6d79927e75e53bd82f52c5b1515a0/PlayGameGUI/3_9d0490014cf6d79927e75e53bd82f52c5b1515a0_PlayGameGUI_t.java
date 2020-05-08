 package herbstJennrichLehmannRitter.ui.GUI;
 
 import herbstJennrichLehmannRitter.engine.Globals;
 import herbstJennrichLehmannRitter.engine.model.Card;
 import herbstJennrichLehmannRitter.server.GameServer;
 import herbstJennrichLehmannRitter.ui.impl.ClientUserInterface;
 
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.MouseAdapter;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.PaintEvent;
 import org.eclipse.swt.events.PaintListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.widgets.Canvas;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.MessageBox;
 
 /**	Description of PlayMenuGUI Class
  * Implementation of the GameGUI
  */
 
 public class PlayGameGUI extends AbstractMagicGUIElement {
 
 	private DefenceBuildingFields enemyTower;
 	private DefenceBuildingFields enemyWall;
 	private DefenceBuildingFields playerWall;
 	private DefenceBuildingFields playerTower;
 	private RessourceFields playerDungeon;
 	private RessourceFields playerMagicLab;
 	private RessourceFields playerMine;
 	private RessourceFields enemyDungeon;
 	private RessourceFields enemyMagicLab;
 	private RessourceFields enemyMine;
 	private ArrayList<CardFields> playerCards;
 	private ArrayList<CardFields> enemyCards;
 	private CardFields playerChosenCard;
 	private CardFields enemyChosenCards;
 	
 	protected String text;
 	private GameMessage gameMessage;
 	private NameFields playerName;
 	private NameFields enemyName;
 	private boolean cardDetailIsOpen = false;
 	private boolean playerCanPlayCard = false;
 	
 	private ClientUserInterface clientUserInterface;
 	private GameServer gameServer;
 	
 	public PlayGameGUI(Display parent, ClientUserInterface clientUserInterface, GameServer gameServer) {
 		super(parent);
 		this.clientUserInterface = clientUserInterface;
 		this.gameServer = gameServer;
 		
 		initGUI();
 		
 		this.clientUserInterface.setPlayGameGUI(this);
 	}
 	
 	@Override
 	protected void onInitGUI() {
 		this.gameMessage = new GameMessage();
 		initMenuBar();
 		initPlayerName();
 		initPlayerDungeon();
 		initPlayerMagicLab();
 		initPlayerMine();
 		initPlayerWall();
 		initPlayerTower();
 		initEnemyDungeon();
 		initEnemyMagicLab();
 		initEnemyMine();
 		initEnemyWall();
 		initEnemyTower();
 		initPlayerCards();
 		initEnemyCards();
 		initPlayerChosenCards();
 		initEnemyChosenCards();
 		initEnemyName();
 		horizontalLine();
 	}
 	
 	@Override
 	protected Listener getOnCloseListener() {
 		return new Listener() {
 			@Override
 			public void handleEvent(Event event) {
 				try {
 					PlayGameGUI.this.gameServer.stop();
 					PlayGameGUI.this.gameServer.unregister();
 				} catch (RemoteException e1) {
 					e1.printStackTrace();
 				} catch (NullPointerException e2) {
 					e2.printStackTrace();
 				}
 			}
 		};
 	}
 	
 	@Override
 	protected void onInitShell() {
 		getShell().setText("Magic");
 		getShell().setLayout(new FormLayout());
 		getShell().layout();
 	}
 	
 	public void setCardDetailIsOpen(boolean bool) {
 		this.cardDetailIsOpen = bool;
 	}
 	
 	private void initMenuBar() {
 		Menu menuBar = new Menu(getShell(), SWT.BAR);
 		MenuItem fileMenuHead = new MenuItem(menuBar, SWT.CASCADE);
 		fileMenuHead.setText("Menü");
 		Menu menuMenu = new Menu(getShell(), SWT.DROP_DOWN);
 		fileMenuHead.setMenu(menuMenu);
 		MenuItem menuItemHowTo = new MenuItem(menuMenu, SWT.PUSH);
 		menuItemHowTo.setText("Spielregeln");
 		menuItemHowTo.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				howToButtonPressed(e);
 			}
 		});
 		MenuItem menuItemExit = new MenuItem(menuMenu, SWT.PUSH);
 		menuItemExit.setText("Beenden");
 		menuItemExit.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				getShell().close();
 			}
 		});
 		getShell().setMenuBar(menuBar);
 	}
 	
 	private void howToButtonPressed (SelectionEvent e){
 		MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_INFORMATION);
 		String messageString = "Jeder Spieler beginnt mit dem Folgenden: Turm 25 Punkte, Mauer 10 Punkte, " +
 				"Steinbruch, Zauberlabor, Verlies jeweils Stufe 1 und 15 Ressourcen. " +
 				"Und 6 zufälligen Spielkarten aus seinem Kartenstapel. \n" +
 				"Es gibt zwei verschiedene Modi: " +
 				"Turmbau und Sammelwut. " +
 				"Ein Spieler gewinnt, wenn der gegnerische Turm 0 Punkte hat (beide Modi), " +
 				"der eigene Turm 100 Punkte hat (Turmbau) oder von einer Sorte Ressourcen 400 Stück " +
 				"gesammelt worden sind (Sammelwut). \n" +
 				"Das Spiel läuft in Runden ab. " +
 				"Zum Anfang der Runde erhält der Spieler Ressourcen im Wert " +
 				"seiner aktuellen Stufen von Verlies, " +
 				"Zauberlabor und Steinbruch. Dann kann der Spieler eine Karte " +
 				"auswählen und spielen oder " +
 				"verwerfen. Gespielt werden kann die Karte nur, wenn er genügend " +
 				"Ressourcen besitzt, wenn ja, " +
 				"werden alle Aktionen gleichzeitig ausgeführt. Im Anschluss wird die Karte auf " +
 				"den Friedhof gelegt und der Spieler zieht Karten vom Deck, bis er wieder 6 Karten " +
 				"auf der Hand hat. Falls das " +
 				"Deck leer ist, wird der Friedhof neu gemischt. Der andere Spieler ist am Zug. \n" +
 				"Schaden wird erst von der Mauer, danach vom Turm abgezogen, Mauerschaden nur " +
 				"von der Mauer und Turmschaden immer vom Turm. Ressourcengebäude können " +
 				"Stufen gewinnen oder verlieren, dabei ist die niedrigste Stufe Stufe 1. " +
 				"Ressourcenbestände können nicht unter 0 fallen.";
 		messageBox.setMessage(messageString);
 		messageBox.open();
 		
 	}
 	
 	private void horizontalLine() {
 		FormData formData = new FormData();
 		formData.left =  new FormAttachment(0, 1000, 10);
 		formData.top =  new FormAttachment(0, 1000, 362);
 		formData.width = 1004;
 		Label line = new Label(getShell(), SWT.SEPARATOR | SWT.HORIZONTAL | SWT.SHADOW_OUT);
 		line.setLayoutData(formData);
 	}
 	
 	public boolean getPlayerCanPlayCard() {
 		return this.playerCanPlayCard;
 	}
 	
 	private void initPlayerDungeon() {
 		this.playerDungeon = new RessourceFields("Verlies", "Monster", 10, 642);
 	}
 	public void setPlayerDungeonLevel(int level) {
 		this.playerDungeon.setLevel(level);
 	}
 	public void setPlayerDungeonStock(int stock) {
 		this.playerDungeon.setStock(stock);
 	}
 	public int getPlayerDungeonStock() {
 		return this.playerDungeon.getStock();
 	}
 	
 	private void initPlayerMagicLab() {
 		this.playerMagicLab = new RessourceFields("Zauberlabor", "Kristall", 220, 642);
 	}
 	public void setPlayerMagicLabLevel(int level) {
 		this.playerMagicLab.setLevel(level);
 	}
 	public void setPlayerMagicLabStock(int stock) {
 		this.playerMagicLab.setStock(stock);
 	}
 	public int getPlayerMagicLabStock() {
 		return this.playerMagicLab.getStock(); 
 	}
 
 	private void initPlayerMine() {
 		this.playerMine = new RessourceFields("Steinbruch", "Ziegel", 430, 642);
 	}
 	public void setPlayerMineLevel(int level) {
 		this.playerMine.setLevel(level);
 	}
 	public void setPlayerMineStock(int stock) {
 		this.playerMine.setStock(stock);
 	}
 	public int getPlayerMineStock() {
 		return this.playerMine.getStock();
 	}
 	
 	private void initPlayerWall() {
 		this.playerWall = new DefenceBuildingFields("Mauer", 640, 642);
 	}
 	public void setPlayerWall(int level) {
 		this.playerWall.setLevel(level);
 	}
 	
 	public void setPlayerName(String name) {
 		this.playerName.setPlayerName(name);
 	}
 	public String getPlayerName() {
 		return this.playerName.getPlayerName();
 	}
 	
 	public void setEnemyName(String name) {
 		this.enemyName.setPlayerName(name);
 	}
 	public String getEnemyName() {
 		return this.enemyName.getPlayerName();
 	}
 	
 	private void initPlayerTower(){
 		this.playerTower = new DefenceBuildingFields("Turm", 830, 642);
 	}
 	public void setPlayerTower(int level) {
 		this.playerTower.setLevel(level);
 	}
 	
 	private void initPlayerCards() {
 		int x = 157;
 		int y = 515;
 		this.playerCards = new ArrayList<CardFields>();
 		for (int i=0; i<6; i++) {
 			this.playerCards.add(new CardFields((x+(120*i)), y, true, true));
 			this.playerCards.get(i).setCardName("");
 			this.playerCards.get(i).setVisible(false);
 		}
 	}
 	
 	public void setPlayerHandCards(Collection<Card> cards) {
 		PlayGameGUI.setHandCards(PlayGameGUI.this.playerCards, cards);
 	}
 	
 	private void initPlayerChosenCards() {
 		this.playerChosenCard = new CardFields(457, 388, false, false);
 	}
 	
 	public void setPlayerChosenCardName(String name) {
 		if (!getShell().isDisposed()) {
 			PlayGameGUI.setChosenCardName(PlayGameGUI.this.playerChosenCard, name);
 		}
 	}
 
 	public void playerPlayedCard(String name, boolean anotherTurn) {
 		this.playerCanPlayCard = false;
 		PlayGameGUI.removeCardFromDeck(this.playerCards, name);
 		this.playerChosenCard.setCardName(name);
 		this.playerChosenCard.setVisible(true);
 		if (!anotherTurn && this.playerCards.size() > 0) {
 			nextTurnEnemy();
 		}
 	}
 	
 	public void playerDiscardCard(String name) {
 		this.playerCanPlayCard = false;
 		PlayGameGUI.removeCardFromDeck(this.playerCards, name);
 		nextTurnEnemy();
 	}	
 	
 	private void initEnemyDungeon() {
 		this.enemyDungeon = new RessourceFields("Verlies", "Monster", 10, 25);
 	}
 	public void setEnemyDungeonLevel(int level) {
 		this.enemyDungeon.setLevel(level);
 	}
 	public void setEnemyDungeonStock(int stock) {
 		if (stock != Integer.MAX_VALUE) {
 			this.enemyDungeon.setStock(stock);
 		}
 	}
 
 	private void initEnemyMagicLab() {
 		this.enemyMagicLab = new RessourceFields("Zauberlabor", "Kristall", 220, 25);
 	}
 	public void setEnemyMagicLabLevel(int level) {
 		this.enemyMagicLab.setLevel(level);
 	}
 	public void setEnemyMagicLabStock(int stock) {
 		if (stock != Integer.MAX_VALUE) {
 			this.enemyMagicLab.setStock(stock);
 		}
 	}
 	
 	private void initEnemyMine() {
 		this.enemyMine = new RessourceFields("Steinbruch", "Ziegel", 430, 25);
 	}
 	public void setEnemyMineLevel(int level) {
 		this.enemyMine.setLevel(level);
 	}
 	public void setEnemyMineStock(int stock) {
 		if (stock != Integer.MAX_VALUE) {
 			this.enemyMine.setStock(stock);
 		}
 	}
 	
 	private void initEnemyWall() {
 		this.enemyWall = new DefenceBuildingFields("Mauer", 640, 25);
 	}
 	public void setEnemyWall(int level) {
 		this.enemyWall.setLevel(level);
 	}
 	
 	private void initEnemyTower(){
 		this.enemyTower = new DefenceBuildingFields("Turm", 830, 25);
 	}
 	public void setEnemyTower(int level) {
 		this.enemyTower.setLevel(level);
 	}
 
 	private void initEnemyCards() {
 		int x = 157;
 		int y = 90;
 		this.enemyCards = new ArrayList<CardFields>();
 		for (int i=0; i<6; i++) {
 			this.enemyCards.add(new CardFields((x+(120*i)), y, true, true));
 		}
 	}
 	
 	public void setEnemyHandCards(Collection<Card> cards) {
 		PlayGameGUI.setHandCards(PlayGameGUI.this.enemyCards, cards);
 	}
 	
 	private void initEnemyChosenCards() {
 		this.enemyChosenCards = new CardFields(457, 227, false, false);
 	}
 	
 	public void setEnemyChosenCardName(String name) {
 		PlayGameGUI.setChosenCardName(PlayGameGUI.this.enemyChosenCards, name);
 	}
 	
 	public void enemyPlayedCard(String name) {
 		PlayGameGUI.removeCardFromDeck(this.enemyCards, name);
 		this.enemyChosenCards.setCardName(name);
 		this.enemyChosenCards.setVisible(true);
 	}
 	
 	public void enemyDiscardCard(String name) {
 		PlayGameGUI.removeCardFromDeck(this.enemyCards, name);
 	}
 	
 	
 	private void initPlayerName() {
 		this.playerName = new NameFields("Spieler", 366);
 	}
 	
 	private void initEnemyName() {
 		this.enemyName = new NameFields("Gegner", 3);
 	}
 	
 	public void nextTurnPlayer() {
 		if (!getShell().isDisposed()) {
 			this.playerCanPlayCard = true;
 			this.playerName.setPlayerActive("ist am Zug");
 			this.enemyName.setPlayerInactive();
 		}
 	}
 
 	public void nextTurnEnemy() {
 		if (!getShell().isDisposed()) {
 			this.enemyName.setPlayerActive("ist am Zug");
 			this.playerName.setPlayerInactive();
 		}
 	}
 	
 	public void playAnotherCardPlayer() {
 		if (!getShell().isDisposed()) {
 			this.playerCanPlayCard = true;
 			this.playerName.setPlayerActive("hat noch einen Zug");
 			PlayGameGUI.playAnotherCard(this.playerCards);
 		}
 	}
 	
 	public void playAnotherCardEnemy() {
 		if (!getShell().isDisposed()) {
 			this.playerName.setPlayerActive("hat noch einen Zug");
 			PlayGameGUI.playAnotherCard(this.enemyCards);
 		}
 	}
 	
 	private static void playAnotherCard(ArrayList<CardFields> cardFields) {
 		for (CardFields cardField: cardFields) {
 			if (cardField.getCardName() == "") {
 				cardField.setVisible(false);
 			}
 		}
 	}
 
 	public void setGameStateToWon() {
 		this.gameMessage.setTitleToWon();
 	}
 
 	public void setGameStateToLoose() {
 		this.gameMessage.setTitleToLoose();
 	}
 	
 	public void setGameStateToAbort(String reason) {
 		if (getShell().isDisposed()) {
 			return;
 		}
 		
 		this.gameMessage.setTitleToAbort(reason);
 	}
 	
 	private class CardFields {
 		private Label nameLabel;
 		private Composite cardComp;
 		
 		private CardFields(int positionFromLeft, int positionFromTop, boolean isVisible, boolean isClickable) {
 			FormData cardData = new FormData();
 			cardData.left = new FormAttachment(0, 800, positionFromLeft);
 			cardData.top = new FormAttachment(0, 800, positionFromTop);
 			cardData.height = 120;
 			cardData.width = 110;
 
 			this.cardComp = new Composite(getShell(), SWT.BORDER);
 			this.cardComp.setVisible(isVisible);
 			this.cardComp.setLayoutData(cardData);
 			
 			this.nameLabel = new Label(this.cardComp, SWT.CENTER | SWT.WRAP);
 			this.nameLabel.setBounds(0, 5, 110, 35);
 			if (isClickable) {
 				this.cardComp.addMouseListener(new MouseAdapter() {
 					@Override
 				   public void mouseDown(MouseEvent e) {
 						mousePressed(e);
 				   }
 				});
 				this.nameLabel.addMouseListener(new MouseAdapter() {
 					@Override
 					   public void mouseDown(MouseEvent e) {
 							mousePressed(e);
 					   }
 				});
 			}
 		}
 		
 		private void mousePressed(MouseEvent e) {
 			if (!getCardName().isEmpty() && PlayGameGUI.this.cardDetailIsOpen == false) {
 				ShowCardDetailGUI showCardDetailGUI = new ShowCardDetailGUI(getDisplay(), 
 						PlayGameGUI.this, null, 
 						Globals.getGameCardFactory().createCard(getCardName()),
 						PlayGameGUI.this.gameServer);
 				showCardDetailGUI.open();
 				PlayGameGUI.this.cardDetailIsOpen = true;
 			}
 		}
 		
 		public void setCardName(String cardName) {
 			this.nameLabel.setText(cardName);
 		}
 		
 		private String getCardName() {
 			return this.nameLabel.getText();
 		}
 		
 		public void setVisible(boolean isVisible) {
 			this.cardComp.setVisible(isVisible);
 		}
 	}
 	
 	private static void setHandCards(ArrayList<CardFields> playerCards, Collection<Card> cards) {
 		ArrayList<String> cardFields = new ArrayList<String>();
 		for (CardFields cardField: playerCards) {
 			cardFields.add(cardField.getCardName());
 			cardField.setVisible(true);
 		}
 		
 		Iterator<Card> cardIterator = cards.iterator();
 		Card card;
 		do {
 			card = cardIterator.next();
 			if (cardFields.contains(card.getName())) {
 				continue;
 			}
 			for (CardFields cardField: playerCards) {
 				if (cardField.getCardName() == "") {
 					cardField.setCardName(card.getName());
 					break;
 				}
 			}
 		} while (cardIterator.hasNext());
 	}
 	
 	private static void removeCardFromDeck(ArrayList<CardFields> cardFields, String name) {
 		for (CardFields cardField: cardFields) {
 			if( cardField.getCardName() == name) {
 				cardField.setCardName("");
 				cardField.setVisible(false);
 				break;
 			}
 		}
 	}
 	
 	private static void setChosenCardName(CardFields cardField, String name) {
 		if (name != null) {
 			cardField.setCardName(name);
 			cardField.setVisible(true);
 		} else {
 			cardField.setVisible(false);
 		}
 		
 	}
 	
 	private class DefenceBuildingFields {
 		private Label levelValue;
 		
 		public DefenceBuildingFields(String headline,int positionFromLeft, int positionFromTop){
 			FormData canvasData = new FormData();
 			canvasData.left =  new FormAttachment(0, 1000, positionFromLeft);
 			canvasData.top =  new FormAttachment(0, 1000, positionFromTop);
 			canvasData.width = 180;
 			canvasData.height = 54;
 			
 			Composite defenceComp = new Composite(getShell(), SWT.BORDER);
 			defenceComp.setLayoutData(canvasData);
 			{
 				Label headlineLabel = new Label(defenceComp, SWT.CENTER);
 				headlineLabel.setText(headline);
 				headlineLabel.setBounds(10, 2, 160, 15);
 			}
 			{
 				Label levelLabel = new Label(defenceComp, SWT.NONE);
 				levelLabel.setText("Stufe");
 				levelLabel.setBounds(12, 20, 60, 15);
 			}
 			{	
 				this.levelValue = new Label(defenceComp, SWT.NONE);
 				this.levelValue.setText(String.valueOf(0));
 				this.levelValue.setBounds(80, 20, 60, 15);
 			}
 		}
 			
 		public void setLevel(int level) {
 			this.levelValue.setText(String.valueOf(level));
 		}
 	}
 	
 	private class RessourceFields {
 		private Label levelValue;
 		private Label stockValue;
 		
 		public RessourceFields(String headline, String resourceName, int positionFromLeft, int positionFromTop) {
 			FormData canvasData = new FormData();
 			canvasData.left =  new FormAttachment(0, 1000, positionFromLeft);
 			canvasData.top =  new FormAttachment(0, 1000, positionFromTop);
 			canvasData.width = 200;
 			canvasData.height = 54;
 			
 			Composite ressourceComp = new Composite(getShell(), SWT.BORDER);
 			ressourceComp.setLayoutData(canvasData);
 			{
 				Label headlineLabel = new Label(ressourceComp, SWT.CENTER);
 				headlineLabel.setText(headline);
 				headlineLabel.setBounds(10, 2, 180, 15);
 			}
 			{
 				Label levelLabel = new Label(ressourceComp, SWT.NONE);
 				levelLabel.setText("Stufe");
 				levelLabel.setBounds(12, 20, 60, 15);
 			}
 			{	
 				this.levelValue = new Label(ressourceComp, SWT.NONE);
 				this.levelValue.setText(String.valueOf(0));
 				this.levelValue.setBounds(80, 20, 60, 15);
 			}
 			{
 				Label stockLabel = new Label(ressourceComp, SWT.NONE);
 				stockLabel.setText(resourceName);
 				stockLabel.setSize(60, 30);
 				stockLabel.setBounds(12, 38, 60, 15);
 			}
 			{	
 				this.stockValue = new Label(ressourceComp, SWT.NONE);
 				this.stockValue.setText("unbekannt");
 				this.stockValue.setBounds(80, 38, 80, 15);
 			}
 		}
 		
 		public void setLevel(int level) {
 			this.levelValue.setText(String.valueOf(level));
 		}
 		
 		public void setStock(int stock) {
 			this.stockValue.setText(String.valueOf(stock));
 		}
 		
 		public int getStock() {
 			return Integer.valueOf(this.stockValue.getText().toString());
 		}
 	}
 	
 	private class NameFields {
 		private Label nameLabel;
 		private String playerName;
 		
 		public NameFields(String name, int positionFromTop) {
 			this.playerName = name;
 			
 			FormData nameData = new FormData();
 			nameData.left =  new FormAttachment(0, 1000, 10);
 			nameData.top =  new FormAttachment(0, 1000, positionFromTop);
 			nameData.width = 1004;
 			nameData.height = 20;
 			
 			this.nameLabel = new Label(getShell(), SWT.CENTER | SWT.BORDER_SOLID);
 			this.nameLabel.setText(NameFields.this.playerName);
 			this.nameLabel.setLayoutData(nameData);
 			this.nameLabel.pack();
 		}
 		
 		public void setPlayerActive(String text) {
 			this.nameLabel.setBackground(new Color(getDisplay(), 255, 200, 0));
 			this.nameLabel.setForeground(new Color(getDisplay(), 255, 255, 255));
 			this.nameLabel.setText(NameFields.this.playerName + ' ' + text);
 		}
 		
 		public void setPlayerInactive() {
 			this.nameLabel.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
 			this.nameLabel.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));
 			this.nameLabel.setText(NameFields.this.playerName);
 		}
 		
 		public void setPlayerName(String name) {
 			this.playerName = name;
 			this.nameLabel.setText(name);
 		}
 		
 		public String getPlayerName() {
 			return this.playerName;
 		}
 	}
 
 	private class GameMessage {
 		private Canvas gameMessageCanv;
 		private PaintEvent paintEvent;
 
 		public GameMessage() {
 			FormData gameMessageData = new FormData();
 			gameMessageData.left = new FormAttachment(0, 1, 100);
 			gameMessageData.top = new FormAttachment(0, 1, 150);
 			gameMessageData.height = 500;
 			gameMessageData.width = 800;
 			
 			this.gameMessageCanv = new Canvas(getShell(), SWT.BORDER);
 			this.gameMessageCanv.setLayoutData(gameMessageData);
 			this.gameMessageCanv.setVisible(false);
 			this.gameMessageCanv.setBackground(new Color(getDisplay(), 255, 255, 255));
 		
 			this.gameMessageCanv.addPaintListener(new PaintListener()  {
 				@Override
 				public void paintControl(PaintEvent e) {
 					GameMessage.this.paintEvent = e;
 					GameMessage.this.paintEvent.gc.setFont(new Font(getDisplay(), 
 												"Verdana", 28, SWT.BOLD));
 				}
 			});
 		}
 		
 		public void setTitleToWon() {
 			GameMessage.this.setGameMessageText(getPlayerName() + " hat gewonnen", 0, 200);
 		}
 		
 		public void setTitleToLoose() {
 			GameMessage.this.setGameMessageText(getPlayerName() + " hat verloren", 200, 0);
 		}
 		
 		public void setTitleToAbort(final String text) {
 			GameMessage.this.setGameMessageText(text, 0, 0);
 		}
 		
 		private void setGameMessageText(final String text, final int red, final int green) {
 			GameMessage.this.gameMessageCanv.addPaintListener(new PaintListener()  {
 				@Override
 				public void paintControl(PaintEvent e) {
 					GameMessage.this.paintEvent = e;
 					
					GameMessage.this.paintEvent.gc.setForeground(new Color
							(getDisplay(), red, green, 0));
 					Point textSize = GameMessage.this.paintEvent.gc.textExtent(text);
 					GameMessage.this.paintEvent.gc.drawText(text, 
 							(GameMessage.this.gameMessageCanv.getSize().x - textSize.x)/2, 
 							(GameMessage.this.gameMessageCanv.getSize().y - textSize.y)/2);
 				}
 			});
 			GameMessage.this.gameMessageCanv.setVisible(true);
 		}
 	}
 }
