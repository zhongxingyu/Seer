 package ch.bfh.monopoly.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.util.List;
 import java.util.ResourceBundle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.util.ArrayList;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSpinner;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.Timer;
 
 import ch.bfh.monopoly.common.BoardController;
 import ch.bfh.monopoly.common.Dice;
 import ch.bfh.monopoly.common.GameController;
 import ch.bfh.monopoly.common.Token;
 import ch.bfh.monopoly.observer.PlayerListener;
 import ch.bfh.monopoly.observer.PlayerStateEvent;
 import ch.bfh.monopoly.observer.TileSubject;
 import ch.bfh.monopoly.observer.WindowListener;
 import ch.bfh.monopoly.observer.WindowMessage;
 import ch.bfh.monopoly.observer.WindowStateEvent;
 import ch.bfh.monopoly.tile.TileInfo;
 
 /**
  * This class is used to show the 
  * window with the all the graphical components 
  * of the game after the welcome panel
  * @author snake
  *
  */
 public class MonopolyGUI extends JFrame {
 
 	private static final long serialVersionUID = -3409398396221480650L;
 
 	/**
 	 * Space and counters constants
 	 */
 	public static final int TILE_NUMBER = 40;
 	private final int LEFT_SPACER_HEIGHT = 10;
 	private final int DICE_MOVEMENT_DELAY = 850;
 
 	/**
 	 * Graphical elements
 	 */
 	private JTextArea eventTextArea, chat, history;
 	private JPanel tab1;
 	private List<BoardTile> tiles = new ArrayList<BoardTile>();
 	private JTabbedPane tabPane = new JTabbedPane();
 	private JButton throwDice, useCard, community, chance, endTurn, trade, sendTradeRequest;
 	private JCheckBox terrainCheck, cardCheck, moneyCheck, rcvrTerrainCheck, rcvrCardCheck, rcvrMoneyCheck;
 	private JComboBox usersBox, myTerrainBox, hisTerrainBox;
 	private JSpinner moneySpinner, rcvrMoneySpinner;
 	private JLabel jailCardLbl, rcvrJailCardLbl;
 
 	/**
 	 * Counter for dice throw
 	 */
 	private int step = 0;
 
 
 	/**
 	 * Other instance variable
 	 */
 	private int playerNumber;
 	private BoardController bc;
 	private GameController gc;
 	private ResourceBundle res;
 	private Dice dice = new Dice(6,6);
 	private List<PlayerStateEvent> pse;
 	private boolean tokenPlaced = false;
 	private int[][] tileGroupMember = { {-1,-1},{3,-1}, {-1,-1}, {1,-1}, {-1,-1},{-1,-1}, {8,9},
 			//14 Via nassa
 			{-1,-1}, {6,9}, {6,8}, {-1,-1}, {13,14}, {-1,-1}, {11,14}, {11,13}, {-1,-1}, {18,19}, {-1,-1}, {16,19}, {16,18},
 			//20 free park											//26								//30
 			{-1,-1}, {23,24}, {-1,-1}, {21,24}, {21,23}, {-1,-1}, {27,29}, {26,29}, {-1,-1}, {26,27}, {-1,-1}, 
 
 			{32,34},{31,34}, {-1,-1},{31,32},{-1,-1},{-1,-1},{39,-1},{-1,-1},{37,-1}
 	};
 
 	/**
 	 * Construct a MonopolyGUI
 	 * @param bc the board controller used to query the board
 	 */
 	public MonopolyGUI(BoardController bc, GameController gc){
 		this.bc = bc;
 		this.gc = gc;
 
 		this.res = ResourceBundle.getBundle("ch.bfh.monopoly.resources.gui", gc.getLocale());
 		System.out.println(this.res);
 
 		System.out.println("INSIDE MONOPOLY FRAME");
 
 		//initialize the buttons with the action listener
 		initializeButtons();
 
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setTitle(res.getString("title"));
 		setLayout(new BorderLayout());
 
 		System.out.println("BEFORE WRAPPER INIT");
 		//initialize the element of the GUI
 		wrapperInit();
 
 		pack();
 	}
 
 	/**
 	 * Initialize the list of tiles, tokens and player listener
 	 * !!! RESPECT THE ORDER OF METHOD'S CALL IN ORDER TO 
 	 * AVOID PROBLEMS !!!
 	 */
 	private void wrapperInit(){
 		/**
 		 * This inner class is used to 
 		 * implement the PlayerListener used
 		 * to draw the token on the board
 		 */
 		class TokenDraw implements PlayerListener{
 			@Override
 			public void updatePlayer(ArrayList<PlayerStateEvent> playerStates) {
 				pse = playerStates;
 
 				for(PlayerStateEvent singlePlayer : pse){
 					//used to place the token on the first tile for the first time
 					if(!tokenPlaced){
 						System.out.println("DRAING TOKEN ON POSITION" + singlePlayer.getName());
 						Token t = singlePlayer.getT();
 						int position = singlePlayer.getPosition();
 						tiles.get(position).addToken(t);
 					}
 					//used to move the token 
 					else if(tokenPlaced){
 						Token t = singlePlayer.getT();
 						int throwValue = singlePlayer.getPosition()-singlePlayer.getPreviousPosition();
 						int previousPosition = singlePlayer.getPreviousPosition();
 
 						System.out.println("==== TOKEN / DICE VALUES ====");
 						System.out.println("TOKEN COLOR: " + t.getColor());
 						System.out.println("PLAYER NAME: " + singlePlayer.getName());
 						System.out.println("THROW VALUE: " + (singlePlayer.getPosition()-singlePlayer.getPreviousPosition()));
 						System.out.println("START POSITION: " + singlePlayer.getPosition());
 						System.out.println("PREVIOUS POSITION: " + singlePlayer.getPreviousPosition());
 
 						//move the token only when the user has thrown the dice
 						if(throwValue > 1 && throwValue < 13){
 							Timer timerAnimation = new Timer(DICE_MOVEMENT_DELAY, moveToken(throwDice, t, throwValue, previousPosition));
 							timerAnimation.start();
 						}
 
 					}
 				}
 
 				//we have placed the tokenserve
 				tokenPlaced = true;
 			}
 		}
 
 		System.out.println("AFTER INNER CLASS");
 
 		//add the listener to the subject
 		TokenDraw td = new TokenDraw();
 		bc.getSubjectForPlayer().addListener(td);
 
 		//get the playerNumber
 		this.playerNumber = bc.getPlayerCount();
 
 		add(leftPanel(), BorderLayout.WEST);
 
 		System.out.println("AFTER LEFT PANEL ADD");
 
 		//Initialize all the tiles with the information 
 		for(int j = 0 ; j < TILE_NUMBER ; j++){
 			TileInfo t = bc.getTileInfoById(j);
 
 			BoardTile bt = new BoardTile(t, tab1, this.bc,this.gc, this.res);
 
 			//System.out.println("AFTER BOARD TILE CREATION " + j);
 
 			//System.out.println(tileGroupMember[j][0]);
 
 			TileSubject s = this.bc.getTileSubjectAtIndex(j);
 			this.tiles.add(bt);
 			s.addListener(bt.getTileListener());
 		}
 
 		//after we have initialized the tiles, 
 		//add the tile of the same group to each single tile board
 		//so each tile knows the member of the same group
 		for(int i = 0 ; i < TILE_NUMBER ; i++){
 			BoardTile[] member = new BoardTile[2];
 
 			//get the neighborhood
 			int[] neighborhood = tileGroupMember[i];
 
 			if(neighborhood[0] != -1 && neighborhood[1] != -1){
 				member[0] = this.tiles.get(neighborhood[0]);
 				member[1] = this.tiles.get(neighborhood[1]);
 			}
 			else if(neighborhood[0] != -1 && neighborhood[1] == -1){
 				member[0] = this.tiles.get(neighborhood[0]);
 				member[1] = null;
 			}
 			else{
 				member[0] = null;
 				member[1] = null;
 			}
 
 			this.tiles.get(i).setGroupMember(member);
 		}
 
 		System.out.println("AFTER TILE INIT");
 
 		add(drawBoard(), BorderLayout.CENTER);
 		
 		
 		//!!! leave this here !!!
 		this.bc.initGUI();
 	}
 
 	/**
 	 * Panel for the left container
 	 * @return the left jpanel
 	 */
 	private JPanel leftPanel(){
 		JPanel left = new JPanel();
 
 		Dimension spacer = new Dimension(0,LEFT_SPACER_HEIGHT);
 
 		left.setLayout(new BoxLayout(left, BoxLayout.PAGE_AXIS));
 		left.setBorder(BorderFactory.createMatteBorder(0, 3, 0, 2, Color.decode("0xEEEEEE")));
 
 		left.add(cardPanel());
 		left.add(Box.createRigidArea(spacer));
 		left.add(infoPanel());
 		left.add(Box.createRigidArea(spacer));
 		left.add(historyChatPanel());
 
 		return left;
 	}
 
 	/**
 	 * Draw the info panel
 	 * @return JPanel with the information
 	 */
 	private JPanel infoPanel(){
 		JPanel info = new JPanel();
 		info.setLayout(new BoxLayout(info, BoxLayout.PAGE_AXIS));
 
 		//for each player create the panel 
 		//with his info
 		for(int j = 0 ; j < playerNumber ; j++){
 
 			PlayerInfo plInfo = new PlayerInfo(j, this.bc);
 
 			bc.getSubjectForPlayer().addListener(plInfo.getPlayerListener());
 
 			//TODO hard to do because, we have the pse arraylist after 
 			//the call to this method, so at this point, pse is empty.
 
 			//if is the local player, show the terrain panel by default
 			//			if(gc.getLocalPlayerName().equals(this.pse.get(j).getName()))
 			//					plInfo.showTerrains();
 
 			info.add(plInfo);
 		}
 
 		return info;
 	}
 
 	/**
 	 * Draw the tabbed panel for the card
 	 * @return JTabbedPane 
 	 */
 	private JTabbedPane cardPanel(){
 		JTabbedPane card = new JTabbedPane();
 		tab1 = new JPanel();
 		tab1.setLayout(new BoxLayout(tab1, BoxLayout.PAGE_AXIS));
 
 		card.addTab(res.getString("tab-card"), tab1);
 		return card;
 	}
 
 	/**
 	 * Draw the tabbed panel for the chat and history
 	 * @return JTabbedPane
 	 */
 	private JTabbedPane historyChatPanel(){
 		JTabbedPane pane = new JTabbedPane();
 
 		//create history text area
 		history = new JTextArea(5,20);
 		history.setWrapStyleWord(true);
 		history.setLineWrap(true);
 		history.setEditable(false);
 
 		//add to the history text area a scroll pane
 		JScrollPane historyScroll = new JScrollPane(history);
 		historyScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 		historyScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
 
 		//create chat text area
 		chat = new JTextArea(7,20);
 		chat.setWrapStyleWord(true);
 		chat.setLineWrap(true);
 		chat.setEditable(false);
 
 		class TextUpdate implements WindowListener{
 
 			@Override
 			public void updateWindow(WindowStateEvent wse) {
 				if (wse.getType()==WindowMessage.MSG_FOR_CHAT){
 					chat.append(wse.getEventDescription());
 					chat.setCaretPosition(chat.getDocument().getLength());
 				}
 			}
 		}
 
 		TextUpdate tu = new TextUpdate();
 
 		gc.getWindowSubject().addListener(tu);
 
 		//add to the chat text area a scroll pane
 		JScrollPane scrollChat = new JScrollPane(chat);
 		scrollChat.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 		scrollChat.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
 
 		final JTextField input = new JTextField(20);
 
 		//when we press enter, we send a message
 		input.addKeyListener(new KeyListener() {
 			@Override
 			public void keyTyped(KeyEvent e) {}
 
 			@Override
 			public void keyReleased(KeyEvent e) {}
 
 			@Override
 			public void keyPressed(KeyEvent e) {
 				//if we press the enter key
 				System.out.println("Key pressed" + e.getKeyCode());
 				if(e.getKeyCode() == 10 && input.getText().length() != 0){
 					String text = input.getText();
 					chat.append(gc.getLocalPlayerName() + ": " + text + "\n");
 					chat.setCaretPosition(chat.getDocument().getLength());
 					gc.sendChatMessage(text);
 					input.setText("");
 				}
 			}
 		});
 
 		//add to the input text area a scroll pane
 		JScrollPane scrollInput = new JScrollPane(input);
 		scrollInput.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 		scrollInput.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
 		scrollInput.setMaximumSize(new Dimension(250,65));
 
 		//the chat panel, with the input and chat text area
 		JPanel chatArea = new JPanel();
 		chatArea.setLayout(new BoxLayout(chatArea, BoxLayout.PAGE_AXIS));
 		chatArea.add(scrollChat);
 
 		JPanel inputContainer = new JPanel();
 		inputContainer.setLayout(new BoxLayout(inputContainer, BoxLayout.PAGE_AXIS));
 		inputContainer.add(scrollInput);
 
 		chatArea.add(inputContainer);
 
 		//create the two tab
 		pane.addTab(res.getString("tab-chat"), chatArea);
 		pane.addTab(res.getString("tab-history"), historyScroll);
 		return pane;
 	}
 
 	/**
 	 * Draw the board
 	 * @return a JPanel containing the board's elements
 	 */
 	private JPanel drawBoard(){
 		ArrayList<JButton> guiButtons = new ArrayList<JButton>();
 		guiButtons.add(throwDice);
 		guiButtons.add(useCard);
 		guiButtons.add(trade);
 		guiButtons.add(endTurn);
 
 		//set the parameters for the event window
 		this.eventTextArea = new JTextArea(13,23);
 		eventTextArea.setWrapStyleWord(true);
 		eventTextArea.setLineWrap(true);
 		eventTextArea.setEditable(false);
 
 		System.out.println("DRAWBOARD METHOD");
 		JPanel board = new BoardBuilder(this.eventTextArea, this.tabPane, this.tiles, guiButtons, community, chance);
 
 		return board;
 	}
 
 	/**
 	 * Move the token
 	 * @param diceButton
 	 * 			the JButton to enable/disable when we throw the dice
 	 * 			set this value to null if you don't need the button but only the movement
 	 * 			this because we don't know when the timer will stops outside this method
 	 * @param t
 	 * 			the Token to move on the board
 	 * @param val
 	 * 			the value of the throw
 	 * @return Action
 	 * 				the abstract action used to move the token
 	 */
 	private Action moveToken(final JButton diceButton, final Token t, final int val, final int startPosition){
 		Action moveToken = new AbstractAction() {
 
 			private static final long serialVersionUID = 9219941791909195711L;
 
 			@Override
 			public void actionPerformed(ActionEvent e) {		
 				if(diceButton != null)
 					throwDice.setEnabled(false);	
 
 				System.out.println("==== MOVE TOKEN VALUES ====");
 
 				//move the token for "step" times
 				if(step < val){
 					step++;
 
 					System.out.println("GET TOKEN TO REMOVE ON POSITION: " + (startPosition+step-1)%TILE_NUMBER);
 					System.out.println("GET TOKEN TO ADD ON POSITION: " + (startPosition+step)%TILE_NUMBER);
 
 					//removing the token at the previous tile
 					tiles.get((startPosition+step-1)%TILE_NUMBER).removeToken(t);
 
 					//add the token to the tile we are on
 					tiles.get((startPosition+step)%TILE_NUMBER).addToken(t);
 
 					repaint();
 
 				}
 				else if(step == val){
 					((Timer)e.getSource()).stop();
 
 					//show tile's information in the card box
 					tiles.get((startPosition+val)%TILE_NUMBER).showCard();
 					System.out.println("LANDED ON TILE: " + (startPosition+val)%TILE_NUMBER);
 
 					step = 0;	
 
 					//TODO only for test
 					if(diceButton != null)
 						diceButton.setEnabled(true);
 				}	
 			}
 		};
 
 		return moveToken;
 
 	}
 
 	private void initializeButtons(){
 		this.useCard = new JButton(res.getString("button-jailcard"));
 		useCard.setEnabled(false);
 
 		this.throwDice = new JButton(res.getString("button-throwdice"));
 
 		//action listeners
 		throwDice.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				int localPlayerthrowValue = dice.throwDice();		
 
 				//TODO only for test
 				tabPane.addTab(res.getString("tab-trade"), tradeTab());
 
 				eventTextArea.setText(res.getString("text-throwindice") + "\n");
 				eventTextArea.append(res.getString("text-diceresult") + " " + dice.getDiceValues() + " =>" + localPlayerthrowValue + "\n");
 
 				//move the player of throwValue positions, and communicate to the other player the new position
 				gc.advancePlayerNSpaces(localPlayerthrowValue);
 			}
 		});
 
 		this.community = new JButton(res.getString("button-communitychest"));
 		this.community.setEnabled(false);
 
 		this.chance = new JButton(res.getString("button-chance"));
 		this.chance.setEnabled(false);
 
 		this.endTurn = new JButton(res.getString("button-endturn"));
 		this.endTurn.setEnabled(false);
 		
 		endTurn.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				gc.endTurn();
 			}
 		});
 
 		this.trade = new JButton(res.getString("button-trade"));
 		this.trade.setEnabled(false);
 
 				class ButtonManager implements PlayerListener{
 		
 					@Override
 					public void updatePlayer(ArrayList<PlayerStateEvent> playerStates) {
 						for(PlayerStateEvent playerState : playerStates){
 							//if the localplayer has the token enable buttons
 							if(playerState.getName().equals(gc.getLocalPlayerName())){
								if(playerState.hasToken()){
 									throwDice.setEnabled(true);
 								}
 							}
 						}
 					}	
 				}
 				
 				ButtonManager bl = new ButtonManager();
 
 				bc.getSubjectForPlayer().addListener(bl);
 
 	}
 
 	/**
 	 * This method is used to draw the JPanel used 
 	 * to trade properties / card / money with another player
 	 * @return
 	 */
 	private JScrollPane tradeTab(){
 		JPanel yourOffer = new JPanel();
 		yourOffer.setLayout(new GridLayout(9, 2));
 
 		this.terrainCheck = new JCheckBox();
 		this.cardCheck = new JCheckBox();
 		this.moneyCheck = new JCheckBox();
 		this.rcvrTerrainCheck = new JCheckBox();
 		this.rcvrCardCheck = new JCheckBox();
 		this.rcvrMoneyCheck = new JCheckBox();
 
 		this.sendTradeRequest = new JButton(res.getString("button-sendoffer"));
 
 		this.myTerrainBox = new JComboBox();
 		this.moneySpinner = new JSpinner();
 		this.rcvrMoneySpinner = new JSpinner();
 		this.jailCardLbl = new JLabel(res.getString("label-jailcard"));
 		this.rcvrJailCardLbl = new JLabel(res.getString("label-jailcard"));
 
 		this.usersBox = new JComboBox();
 
 		//build the array with the user name
 		for(int i = 0 ; i < playerNumber ; i++){
 			this.usersBox.addItem(pse.get(i).getName());
 		}
 
 		this.hisTerrainBox = new JComboBox();
 
 		//change 10 to selectedPlayer.getTerrain.size
 		for(int i = 0 ; i < 10 ; i++){
 			this.hisTerrainBox.addItem(i);
 		}
 
 		myTerrainBox.setEnabled(false);
 		moneySpinner.setEnabled(false);
 		jailCardLbl.setEnabled(false);
 
 		hisTerrainBox.setEnabled(false);
 		rcvrMoneySpinner.setEnabled(false);
 		rcvrJailCardLbl.setEnabled(false);
 
 		JLabel offerLbl = new JLabel(res.getString("label-youroffer"));
 		JLabel wantedRes = new JLabel(res.getString("label-requestfrompl"));
 
 		//change 10 to localPlayer.getTerrains
 		for(int i = 0 ; i < 10 ; i++){
 			myTerrainBox.addItem(i);
 		}
 
 		//money spinner
 		int startMoney = 0;
 		//TODO adjust JSpinner money range
 		moneySpinner.setModel(new SpinnerNumberModel(startMoney, startMoney,startMoney + 15000, 1));
 		rcvrMoneySpinner.setModel(new SpinnerNumberModel(startMoney, startMoney,startMoney + 15000, 1));
 
 		//add the panels to the container
 		yourOffer.add(offerLbl,0);
 		yourOffer.add(new JLabel(" "), 1);
 		yourOffer.add(terrainCheck, 2);
 		yourOffer.add(myTerrainBox, 3);
 		yourOffer.add(cardCheck, 4);
 		yourOffer.add(jailCardLbl, 5);
 		yourOffer.add(moneyCheck, 6);
 		yourOffer.add(moneySpinner, 7);
 		yourOffer.add(wantedRes, 8);
 		yourOffer.add(usersBox, 9);
 		yourOffer.add(rcvrTerrainCheck, 10);
 		yourOffer.add(hisTerrainBox, 11);
 		yourOffer.add(rcvrCardCheck, 12);
 		yourOffer.add(rcvrJailCardLbl, 13);
 		yourOffer.add(rcvrMoneyCheck, 14);
 		yourOffer.add(rcvrMoneySpinner, 15);
 		yourOffer.add(new JLabel(" "), 16);
 		yourOffer.add(sendTradeRequest, 17);
 
 		JScrollPane scrollInput = new JScrollPane(yourOffer);
 		scrollInput.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 		scrollInput.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
 
 		return scrollInput;
 
 	}
 }
