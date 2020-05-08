 /*  Copyright (C) 2010 - 2011  Fabian Neundorf, Philip Caroli,
  *  Maximilian Madlung,	Usman Ghani Ahmed, Jeremias Mechler
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *  
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *  
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.ojim.client.gui;
 
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.UIManager;
 import javax.swing.UIManager.LookAndFeelInfo;
 
 import org.ojim.client.ClientBase;
 import org.ojim.client.gui.CardBar.CardWindow;
 import org.ojim.client.gui.GameField.GameField;
 import org.ojim.client.gui.OLabel.FontLayout;
 import org.ojim.client.gui.PopUpFrames.*;
 import org.ojim.client.gui.RightBar.ChatMessage;
 import org.ojim.client.gui.RightBar.ChatWindow;
 import org.ojim.client.gui.RightBar.PlayerInfoWindow;
 import org.ojim.language.Localizer;
 import org.ojim.language.LanguageDefinition;
 import org.ojim.logic.state.GameState;
 import org.ojim.logic.state.Player;
 import org.ojim.logic.state.fields.BuyableField;
 import org.ojim.logic.state.fields.Street;
 import org.ojim.server.OjimServer;
 
 public class GUIClient extends ClientBase {
 
 	GUISettings settings;
 	GameField gameField;
 	ChatWindow chatWindow;
 	PlayerInfoWindow playerInfoWindow;
 	CardWindow cardWindow = new CardWindow();
 
 	CreateGameFrame createGameFrame;
 	JoinGameFrame joinGameFrame;
 	SettingsFrame settingsFrame;
 	HelpFrame helpFrame;
 	AboutFrame aboutFrame;
 
 	String name;
 
 	MenuBar menubar;
 	JPanel window = new JPanel();
 	JPanel rightWindow1 = new JPanel();
 	JPanel downWindow = new JPanel();
 	JPanel downRight = new JPanel();
 	JButton buyButton = new JButton();
 	JButton rollButton = new JButton();
 
 	JFrame GUIFrame;
 
 	JPanel pane = new JPanel(new OJIMLayout());
 	Localizer language;
 	
 	boolean haveIalreadyRolled = false;
 
 	private MenuState menuState;
 
 	public GUIClient() {
 
 		// Nur zu Debugzwecken auf game
 		setMenuState(MenuState.mainMenu);
 		// setMenuState(MenuState.game);
 
 		language = new Localizer();
 
 		LanguageDefinition[] langs = language.getLanguages();
 		if (langs.length == 0) {
 			System.out.println("No languagefile found.");
 		}
 
 		if (langs.length > 0)
 			language.setLanguage(langs[0]);
 
 		createGameFrame = new CreateGameFrame(language, this);
 		joinGameFrame = new JoinGameFrame(language);
 		settingsFrame = new SettingsFrame(language);
 		helpFrame = new HelpFrame(language);
 		aboutFrame = new AboutFrame(language);
 
 		createGameFrame.setTitle(language.getText("create game"));
 		joinGameFrame.setTitle(language.getText("join game"));
 		settingsFrame.setTitle(language.getText("settings"));
 		helpFrame.setTitle(language.getText("help"));
 		aboutFrame.setTitle(language.getText("about"));
 
 		gameField = new GameField();
 
 		GUIFrame = new JFrame(language.getText("ojim"));
 
 		playerInfoWindow = new PlayerInfoWindow(language);
 		chatWindow = new ChatWindow(language);
 
 		menubar = new MenuBar(language, this);
 		GUIFrame.setJMenuBar(menubar);
 
 		GUIFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		GUIFrame.setMinimumSize(new Dimension(550, 450));
 
 		// LookAndFeel lookAndFeel = UIManager.getLookAndFeel();
 
 		draw();
 
 	}
 
 	private void draw() {
 
 		// GUIFrame = new JFrame(language.getText("ojim"));
 
 		try {
 			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
 				if ("Windows".equals(info.getName())) {
 					UIManager.setLookAndFeel(info.getClassName());
 					break;
 				} else if ("Nimbus".equals(info.getName())) {
 					UIManager.setLookAndFeel(info.getClassName());
 					break;
 				} else if ("Metal".equals(info.getName())) {
 					UIManager.setLookAndFeel(info.getClassName());
 					break;
 				}
 			}
 			// Keines der Standarddesigns vorhanden. Nimm das was du hast.
 		} catch (Exception e) {
 			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
 				try {
 					UIManager.setLookAndFeel(info.getClassName());
 				} catch (Exception e1) {
 					// Kein Look and Feel
 					e1.printStackTrace();
 				}
 			}
 		}
 
 		switch (menuState) {
 
 		case mainMenu:
 
 			// TODO Ein schönes Bild, oder ein Vorschauspiel vielleicht?
 
 			break;
 
 		case waitRoom:
 
 			setName("Max");
 
 			OjimServer server = new OjimServer("Philip");
 
 			server.initGame(8, 7);
 
 			connect(server);
 
 			JPanel leftWindow = new JPanel();
 			JPanel rightWindow = new JPanel();
 
 			window.setLayout(new GridLayout(1, 0));
 			rightWindow.setLayout(new GridLayout(0, 1));
 
 			playerInfoWindow = new PlayerInfoWindow(language);
 
 			rightWindow.add(playerInfoWindow);
 			leftWindow.add(chatWindow);
 
 			JButton button;
 			button = new JButton(language.getText("ready"));
 
 			ActionListener clickedOnReady = new ActionListener() {
 
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					ready();
 
 				}
 			};
 			;
 			;
 
 			button.addActionListener(clickedOnReady);
 
 			rightWindow.add(button);
 
 			window.add(leftWindow);
 			window.add(rightWindow);
 
 			GUIFrame.add(window);
 			break;
 
 		case game:
 			System.out.println("Male das Spielfeld");
 
 			GUIFrame.remove(window);
 			GUIFrame.remove(pane);
 
 			pane.remove(gameField);
 			pane.remove(rightWindow1);
 			pane.remove(downWindow);
 			pane.remove(downRight);
 
 			rightWindow1.remove(playerInfoWindow);
 			rightWindow1.remove(chatWindow);
 
 			downWindow.remove(cardWindow);
 
 			downRight.remove(buyButton);
 			downRight.remove(rollButton);
 
 			gameField = new GameField();
 
 			gameField.init(GameState.FIELDS_AMOUNT, this.getGameState());
 
 			pane.add(gameField);
 
 			rightWindow1.setLayout(new GridLayout(0, 1));
 
 			playerInfoWindow = new PlayerInfoWindow(language);
 			chatWindow = new ChatWindow(language);
 
 			System.out.println("Es gibt "
 					+ this.getGameState().getPlayers().length + " Spieler.");
 			for (int i = 0; this.getGameState().getPlayers().length > i; i++) {
 				// System.out.println(this.getGameState().getPlayers()[i].getName()+" wurde hinzugefügt mit "+this.getGameState().getPlayers()[i].getBalance()+" Kohle.");
 				this.playerInfoWindow.addPlayer(this.getGameState()
 						.getPlayers()[i], this.getGameState().getPlayers()[i]
 						.getBalance());
 			}
 
 			rightWindow1.add(playerInfoWindow);
 			rightWindow1.add(chatWindow);
 
 			pane.add(rightWindow1);
 
 			downWindow.setLayout(new GridLayout(1, 0));
 
 			cardWindow = new CardWindow();
 
 			downWindow.add(cardWindow);
 
 			pane.add(downWindow);
 
 			downRight.remove(buyButton);
 
 			downRight.setLayout(new GridLayout(1, 0));
 			try {
 				if (((BuyableField) (getGameState().getFieldAt(getMe()
 						.getPosition()))).getPrice() <= getMe().getBalance()) {
 					buyButton = new JButton(language.getText("buy"));
 
 					ActionListener buyListener = new ActionListener() {
 
 						@Override
 						public void actionPerformed(ActionEvent e) {
 							accept();
 							System.out
 									.println("BUYYYYYYYY THISSSSSS!!!! I NEEEED IT SOOOO MUCH");
 
 						}
 					};
 
 					buyButton.addActionListener(buyListener);
 
 					buyButton.setLayout(new FontLayout());
 					downRight.add(buyButton);
 				}
 			} catch (Exception e) {
 				System.out.println("Kein buyablefield");
 			}
 
 			downRight.remove(rollButton);
 			try {
 				if (!haveIalreadyRolled &&
 						//getGameState().getActivePlayer().equals(getMe()) && 
 						this.getGameState().getActivePlayerNeedsToRoll()) {
 
 					ActionListener rollListener = new ActionListener() {
 
 						@Override
 						public void actionPerformed(ActionEvent arg0) {
 							rollDice();
 							System.out.println("Rolly Rolly");
 							haveIalreadyRolled = true;
 							draw();
 
 						}
 					};
 
 					rollButton = new JButton(language.getText("roll"));
 					rollButton.addActionListener(rollListener);
 					downRight.add(rollButton);
 
 				} else // if (getGameState().getActivePlayer().equals(getMe()))
 				{
 					ActionListener endTurnListener = new ActionListener() {
 
 						@Override
 						public void actionPerformed(ActionEvent arg0) {
 							System.out.println("Turn is ENDED!!!");
 							haveIalreadyRolled = false;
							endTurn();
 
 						}
 					};
 
 					rollButton = new JButton(language.getText("endturn"));
 					rollButton.addActionListener(endTurnListener);
 					downRight.add(rollButton);
 				}
 			} catch (NullPointerException e) {
 				System.out.println("Jemand anderes verschwendet unsere Zeit, Meister.");
 			}
 			rollButton.setLayout(new FontLayout());
 
 			pane.add(downRight);
 
 			GUIFrame.add(pane);
 			GUIFrame.repaint();
 			GUIFrame.setVisible(true);
 			System.out.println("Spielfeld gemalt.");
 			break;
 
 		}
 		if (GUIFrame != null) {
 			GUIFrame.setVisible(true);
 		}
 	}
 
 	public static void main(String[] args) {
 		new GUIClient();
 	}
 
 	public void setMenuState(MenuState menuState) {
 		this.menuState = menuState;
 		draw();
 	}
 
 	@Override
 	public void onBuy(Player player, BuyableField field) {
 		gameField.playerBuysField(player, field);
 
 		// TODO if player = gui player => feld and cardBar schicken zum
 		// aufnehmen
 		// Wo finde ich heraus ob ich der GUI Player bin?
 
 	}
 
 	@Override
 	public void onCashChange(Player player, int cashChange) {
 		playerInfoWindow.changeCash(player, cashChange);
 	}
 
 	@Override
 	public void onConstruct(Street street) {
 		gameField.buildOnStreet(street);
 	}
 
 	@Override
 	public void onDestruct(Street street) {
 		gameField.destroyOnStreet(street);
 	}
 
 	@Override
 	public void onMessage(String text, Player sender, boolean privateMessage) {
 		chatWindow.write(new ChatMessage(sender, privateMessage, text));
 	}
 
 	@Override
 	public void onMortgageToogle(BuyableField street) {
 		cardWindow.switchCardStatus(street);
 		gameField.switchFieldStatus(street);
 	}
 
 	@Override
 	public void onMove(Player player, int position) {
 		// TODO: (v. xZise) position kann negativ sein (z.B. Gefängnis)
 		// this.menuState = MenuState.game;
 		gameField.playerMoves(this.getGameState().getFieldAt(position), player);
 		System.out.println("MOVE!");
 	}
 
 	@Override
 	public void onTrade(Player actingPlayer, Player partnerPlayer) {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void onBankruptcy() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onCardPull(String text, boolean communityCard) {
 		// TODO Auto-generated method stub
 		// Mittelfeld
 	}
 
 	@Override
 	public void onDiceValues(int[] diceValues) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onStartGame(Player[] players) {
 		this.menuState = MenuState.game;
 		draw();
 	}
 
 	@Override
 	public void onTurn(Player player) {
 		playerInfoWindow.turnOn(player);
 	}
 
 	@Override
 	public void setName(String name) {
 		super.setName(name);
 	}
 
 	public MenuState getMenuState() {
 		return menuState;
 	}
 
 	public GameField getGameField() {
 		return gameField;
 	}
 
 	public ChatWindow getChatWindow() {
 		return chatWindow;
 	}
 
 	public PlayerInfoWindow getPlayerInfoWindow() {
 		return playerInfoWindow;
 	}
 
 	public CardWindow getCardWindow() {
 		return cardWindow;
 	}
 
 	public void openCreateGameWindow() {
 		createGameFrame.setVisible(true);
 
 	}
 
 	public void leaveGame() {
 		// TODO Game beenden
 
 		menuState = MenuState.mainMenu;
 
 	}
 
 	public void openJoinGameWindow() {
 		joinGameFrame.showJoin();
 		joinGameFrame.setVisible(true);
 
 	}
 
 	public void openServerListWindow() {
 		joinGameFrame.showServerList();
 		joinGameFrame.setVisible(true);
 
 	}
 
 	public void openDirectConnectionWindow() {
 		joinGameFrame.showDirectConnection();
 		joinGameFrame.setVisible(true);
 
 	}
 
 	public void openAboutWindow() {
 		aboutFrame.draw();
 		aboutFrame.setVisible(true);
 
 	}
 
 	public void openHelpWindow() {
 		helpFrame.draw();
 		helpFrame.setVisible(true);
 
 	}
 
 	public void changeLanguage(String languageName) {
 		for (int i = 0; i < language.getLanguages().length; i++) {
 			if (language.getLanguages()[i].name.equals(languageName)) {
 				language.setLanguage(language.getLanguages()[i]);
 				resetLanguage();
 			}
 		}
 
 	}
 
 	private void resetLanguage() {
 		GUIFrame.setTitle(language.getText("ojim"));
 		createGameFrame.setTitle(language.getText("create game"));
 		createGameFrame.setLanguage(language);
 		joinGameFrame.setTitle(language.getText("join game"));
 		joinGameFrame.setLanguage(language);
 		settingsFrame.setTitle(language.getText("settings"));
 		settingsFrame.setLanguage(language);
 		helpFrame.setTitle(language.getText("help"));
 		helpFrame.setLanguage(language);
 		aboutFrame.setTitle(language.getText("about"));
 		aboutFrame.setLanguage(language);
 		menubar.language(language);
 
 	}
 
 	@Override
 	public void onAuction(int auctionState) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void openSettingsWindow() {
 		settingsFrame.draw();
 		settingsFrame.setVisible(true);
 
 	}
 
 	public void startServer() {
 		menuState = MenuState.waitRoom;
 		createGameFrame.setVisible(false);
 		draw();
 
 	}
 
 	@Override
 	public void onNewPlayer(Player player) {
 		// draw();
 
 	}
 
 	@Override
 	public void onPlayerLeft(Player player) {
 		// draw();
 
 	}
 
 }
