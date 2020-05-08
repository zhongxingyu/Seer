 package rps.client.ui;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Container;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.io.File;
 import java.io.IOException;
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.Collections;
 
 import javax.imageio.ImageIO;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 
 import rps.game.Game;
 import rps.game.data.Figure;
 import rps.game.data.FigureKind;
 import rps.game.data.Player;
 
 public class GamePane {
 
 	private final JPanel gamePane = new JPanel();
 	private final JTextField chatInput = new JTextField();
 	private final JTextArea chat = new JTextArea(4, 30);
 	private final JTextArea log = new JTextArea(4, 30);
 	private final JScrollPane scrollPane = new JScrollPane(chat);
 	private final JScrollPane logPane = new JScrollPane(log);
 	private final JPanel boardBackground = new JPanel();
 	private final JPanel boardFigures = new JPanel();
 	private final JPanel boardArrows = new JPanel();
 	private final JPanel boardDiscovered = new JPanel();
 	private final JPanel boardButtons = new JPanel();
 	
 	private final JFrame frame = new JFrame();
 	private final JFrame frame2 = new JFrame();
 
 	private Game game;
 	private Player player;
 
 	private ImageIcon iconWhite;
 	private ImageIcon iconBlack;
 	private ImageIcon emptyIcon;
 	private ImageIcon discoveredIcon;
 	private ImageIcon unknownIcon;
 	private ImageIcon boarderIcon;
 	private ImageIcon arrowUp;
 	private ImageIcon arrowDown;
 	private ImageIcon arrowLeft;
 	private ImageIcon arrowRight;
 	
 	private ImageIcon redTrap;
 	private ImageIcon redFlag;
 	private ImageIcon redRock;
 	private ImageIcon redPaper;
 	private ImageIcon redScissors;
 	
 	private ImageIcon blueTrap;
 	private ImageIcon blueFlag;
 	private ImageIcon blueRock;
 	private ImageIcon bluePaper;
 	private ImageIcon blueScissors;
 	
 	private Figure[] board = new Figure[42];
 	private FigureKind[] initialAssignment = new FigureKind[42];
 	
 	public String themePath = "img/fancy/";
 	
 	public String gamePhase = "initFlag";
 	public boolean pick = false;
 	public int pickedPosition;
 	public int positionFlag;
 	public int positionTrap;
 
 
 	public boolean choosen = false;
 	public int choosenPosition;
 	
 	private GridBagConstraints gbcBackground = new GridBagConstraints();
 	private GridBagConstraints gbcFigures = new GridBagConstraints();
 	private GridBagConstraints gbcArrows = new GridBagConstraints();
 	private GridBagConstraints gbcDiscovered = new GridBagConstraints();
 	private GridBagConstraints gbcButtons = new GridBagConstraints();
 	private JLabel[] backgroundTiles = new JLabel[42];
 	private JLabel[] arrows = new JLabel[42];
 	private JLabel[] figures = new JLabel[42];
 	private JLabel[] discovered = new JLabel[42];
 	private JButton[] fieldButtons = new JButton[42];
 	private JButton acceptLineUp = new JButton("Aufstellung akzeptieren");
 	private JButton mixLineUp = new JButton("Neu mischen");
 
 	public GamePane(Container parent) {
 		gamePane.setLayout(null);
 
 		boardBackground.setBounds(20, 15, 700, 600);
 		boardFigures.setBounds(20, 15, 700, 600);
 		boardDiscovered.setBounds(20, 15, 700, 600);
 		boardArrows.setBounds(20, 15, 700, 600);
 		boardButtons.setBounds(20, 15, 700, 600);
 		
 		logPane.setBounds(740, 15, 225, 600);
 		
 		scrollPane.setBounds(20, 630, 700, 80);
 		chatInput.setBounds(20, 710, 700, 20);
 
 		acceptLineUp.setBounds(740, 630, 225, 40);
 		mixLineUp.setBounds(740, 690, 225, 40);
 		
 		gamePane.add(boardArrows);
 		gamePane.add(boardDiscovered);
 		gamePane.add(boardFigures);
 		gamePane.add(boardBackground);
 		gamePane.add(boardButtons);
 
 		gamePane.add(logPane);
 		gamePane.add(scrollPane);
 		gamePane.add(chatInput);
 		gamePane.add(acceptLineUp);
 		gamePane.add(mixLineUp);
 
 		
 		log.setLineWrap(true);
 		log.setEditable(false);
 		chat.setLineWrap(true);
 		chat.setEditable(false);
 		acceptLineUp.setVisible(false);
 		mixLineUp.setVisible(false);
 		
 		mixLineUp.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent ae) {
 				halbmanuellShuffle();
 				redrawInitialAssignment();
 			}
 		});
 		
 		acceptLineUp.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent ae) {
 				acceptLineUp.setVisible(false);
 				mixLineUp.setVisible(false);
 				try{
 					game.setInitialAssignment(player, initialAssignment);
 				}
 				catch (RemoteException re){
 					//TODO
 				}
 				gamePhase = "gamePhase";
 				redraw();
 			}
 		});
 		
 		this.loadPictures();
 		
 		this.drawBackground();
 		this.drawFigures();
 		this.drawArrows();
 		this.drawDiscovered();
 		this.drawButtons();
 
 		gamePane.setVisible(false);
 
 		parent.add(gamePane);
 		bindButtons();
 	}
 
 	private void drawDiscovered() {
 		GridBagLayout gbl = new GridBagLayout();
 		this.boardDiscovered.setLayout(gbl);
 		this.boardDiscovered.setOpaque(false);
 		this.gbcDiscovered.fill = GridBagConstraints.HORIZONTAL;
 
 		for (int i = 0; i < 42; i++) {
 			this.discovered[i] = new JLabel(this.emptyIcon);
 			this.discovered[i].setOpaque(false);
 			
 			this.gbcDiscovered.gridy = Math.round(i / 7);
 			this.gbcDiscovered.gridx = i % 7;
 			this.gbcDiscovered.gridheight = 1;
 			
 			gbl.setConstraints(this.discovered[i], this.gbcDiscovered);
 			this.boardDiscovered.add(this.discovered[i]);
 		}
 		
 	}
 
 	public void askInitial() {
 		this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		Object[] options = {"Schere",
                 			"Stein",
 							"Papier"};
 		int n = JOptionPane.showOptionDialog(frame,
 				"Kampf um den Start. "
 				+ "Womit kaempfst du?",
 				"Startkampf",
 				JOptionPane.YES_NO_CANCEL_OPTION,
 				JOptionPane.QUESTION_MESSAGE,
 				null,
 				options,
 				options[1]);
 		switch(n){
 			case 0:
 				try{
 					this.game.setInitialChoice(this.player, FigureKind.SCISSORS);
 				}
 				catch (RemoteException e){
 					//TODO
 				}
 				break;
 			case 1:
 				try{
 					this.game.setInitialChoice(this.player, FigureKind.ROCK);
 				}
 				catch (RemoteException e){
 					//TODO
 				}
 				break;
 			case 2:
 				try{
 					this.game.setInitialChoice(this.player, FigureKind.PAPER);
 				}
 				catch (RemoteException e){
 					//TODO
 				}
 				break;
 			default:
 				try{
 					this.game.setInitialChoice(this.player, FigureKind.ROCK);
 				}
 				catch (RemoteException e){
 					//TODO
 				}
 				break;
 		}
 		
 	}
 	
 	public void askAfterDraw() {
 		this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		Object[] options = {"Schere",
                 			"Stein",
 							"Papier"};
 		int n = JOptionPane.showOptionDialog(frame,
 				"Unentschieden!"
 				+ "Womit kaempfst du?",
 				"Unentschieden",
 				JOptionPane.YES_NO_CANCEL_OPTION,
 				JOptionPane.QUESTION_MESSAGE,
 				null,
 				options,
 				options[1]);
 		switch(n){
 			case 0:
 				try{
 					this.game.setUpdatedKindAfterDraw(this.player, FigureKind.SCISSORS);
 				}
 				catch (RemoteException e){
 					//TODO
 				}
 				break;
 			case 1:
 				try{
 					this.game.setUpdatedKindAfterDraw(this.player, FigureKind.ROCK);
 				}
 				catch (RemoteException e){
 					//TODO
 				}
 				break;
 			case 2:
 				try{
 					this.game.setUpdatedKindAfterDraw(this.player, FigureKind.PAPER);
 				}
 				catch (RemoteException e){
 					//TODO
 				}
 				break;
 			default:
 				try{
 					this.game.setUpdatedKindAfterDraw(this.player, FigureKind.ROCK);
 				}
 				catch (RemoteException e){
 					//TODO
 				}
 				break;
 		}
 		
 	}
 	public void askLineup() {
 		this.frame2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		Object[] options = {"Manuell",
 							"Zufaellig",
                 			"Nur Flagge/Falle manuell"};
 		int n = JOptionPane.showOptionDialog(frame,
 				"Kampf um den Start. "
 				+ "Womit kaempfst du?",
 				"Startkampf",
 				JOptionPane.YES_NO_CANCEL_OPTION,
 				JOptionPane.QUESTION_MESSAGE,
 				null,
 				options,
 				options[1]);
 		
 		switch(n){
 			case 0:
 				this.printLog("Manuell");
 				
 				break;
 			case 1:
 				this.printLog("Zufaellig");
 				this.createRandomLineup();
 				redrawInitialAssignment();
 				int i=0;
 				while(i == 0){
 					this.createRandomLineup();
 					redrawInitialAssignment();
 					Object[] options2 = {"Neu generieren",
 							"Aufstellung akzeptieren"};
 					i = JOptionPane.showOptionDialog(frame,
 							"Kampf um den Start. "
 							+ "Womit kaempfst du?",
 							"Startkampf",
 							JOptionPane.YES_NO_OPTION,
 							JOptionPane.QUESTION_MESSAGE,
 							null,
 							options2,
 							options2[1]);
 				}	
 				try{
 					this.game.setInitialAssignment(this.player, initialAssignment);
 				}
 				catch (RemoteException e){
 					//TODO
 				}
 				gamePhase = "gamePhase";
 				this.redraw();
 				break;
 			case 2:
 				this.printLog("Halb-Manuell");
 				printLog("Setze die Flagge");
 				break;
 			default:
 				this.printLog("Zufaellig");
 				this.createRandomLineup();
 				redrawInitialAssignment();
 				try{
 					this.game.setInitialAssignment(this.player, initialAssignment);
 				}
 				catch (RemoteException e){
 					//TODO
 				}
 				gamePhase = "gamePhase";
 				this.redraw();
 				break;
 		}
 		
 	}
 	
 	private void createRandomLineup(){
 		ArrayList<FigureKind> list = new ArrayList<FigureKind>();
 		
 		list.add(FigureKind.TRAP);
 		list.add(FigureKind.FLAG);
 		
 		for(int i =0; i<4; i++) {
 			list.add(FigureKind.PAPER);
 			list.add(FigureKind.ROCK);
 			list.add(FigureKind.SCISSORS);
 		}
 		
 		Collections.shuffle(list); // Liste mischen -> zufällige Anordnung
 				
 		for(int i = 0; i<list.size(); i++) {
 			initialAssignment[i+28] = list.get(i);
 		}
 		
 	}
 
 	private void redrawInitialAssignment(){
 		for(int i=28; i<42;i++){
 			if(this.initialAssignment[i] == null) continue;
 			switch(this.initialAssignment[i]){
 			case TRAP:
 				this.figures[i].setIcon(blueTrap);
 				break;
 			case FLAG:
 				this.figures[i].setIcon(blueFlag);
 				break;
 			case ROCK:
 				this.figures[i].setIcon(blueRock);
 				break;
 			case PAPER:
 				this.figures[i].setIcon(bluePaper);
 				break;
 			case SCISSORS:
 				this.figures[i].setIcon(blueScissors);
 				break;
 			case HIDDEN:
 				this.figures[i].setIcon(unknownIcon);
 				break;
 			default:
 				this.figures[i].setIcon(unknownIcon);
 				break;
 			}
 		}
 	}
 
 	private void loadPictures(){
 		try {
 			this.iconWhite = new ImageIcon(ImageIO.read(new File("img/field_white.png")));
 			this.iconBlack = new ImageIcon(ImageIO.read(new File("img/field_black.png")));
 			this.emptyIcon = new ImageIcon(ImageIO.read(new File("img/empty.png")));
 			this.discoveredIcon = new ImageIcon(ImageIO.read(new File("img/discovered.png")));
 			this.boarderIcon = new ImageIcon(ImageIO.read(new File("img/boarder.png")));
 			
 			this.arrowUp = new ImageIcon(ImageIO.read(new File("img/arrow_up.png")));
 			this.arrowDown = new ImageIcon(ImageIO.read(new File("img/arrow_down.png")));
 			this.arrowLeft = new ImageIcon(ImageIO.read(new File("img/arrow_left.png")));
 			this.arrowRight = new ImageIcon(ImageIO.read(new File("img/arrow_right.png")));
 			
 			this.unknownIcon = new ImageIcon(ImageIO.read(new File(this.themePath + "unknown.png")));
 			this.redTrap = new ImageIcon(ImageIO.read(new File(this.themePath + "red_trap.png")));
 			this.redFlag = new ImageIcon(ImageIO.read(new File(this.themePath + "red_flag.png")));
 			this.redRock = new ImageIcon(ImageIO.read(new File(this.themePath + "red_rock.png")));
 			this.redPaper = new ImageIcon(ImageIO.read(new File(this.themePath + "red_paper.png")));
 			this.redScissors = new ImageIcon(ImageIO.read(new File(this.themePath + "red_scissor.png")));
 			this.blueTrap = new ImageIcon(ImageIO.read(new File(this.themePath + "blue_trap.png")));
 			this.blueFlag = new ImageIcon(ImageIO.read(new File(this.themePath + "blue_flag.png")));
 			this.blueRock = new ImageIcon(ImageIO.read(new File(this.themePath + "blue_rock.png")));
 			this.bluePaper = new ImageIcon(ImageIO.read(new File(this.themePath + "blue_paper.png")));
 			this.blueScissors = new ImageIcon(ImageIO.read(new File(this.themePath + "blue_scissor.png")));
 		
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	private void drawBackground() {
 		GridBagLayout gbl = new GridBagLayout();
 		this.boardBackground.setLayout(gbl);
 		this.boardBackground.setOpaque(false);
 		gbcBackground.fill = GridBagConstraints.HORIZONTAL;
 
 		for (int i = 0; i < 42; i++) {
 			this.gbcBackground.gridy = Math.round(i / 7);
 			this.gbcBackground.gridx = i % 7;
 			this.gbcBackground.gridheight = 1;
 			
 			if (i % 2 == 0) {
 				this.backgroundTiles[i] = new JLabel(this.iconWhite);
 				gbl.setConstraints(this.backgroundTiles[i], this.gbcBackground);
 				this.boardBackground.add(this.backgroundTiles[i]);
 			} else {
 				this.backgroundTiles[i] = new JLabel(this.iconBlack);
 				gbl.setConstraints(this.backgroundTiles[i], this.gbcBackground);
 				this.boardBackground.add(this.backgroundTiles[i]);
 			}
 		}
 	}
 
 	
 	private void drawArrows() {
 		GridBagLayout gbl = new GridBagLayout();
 		this.boardArrows.setLayout(gbl);
 		this.boardArrows.setOpaque(false);
 		this.gbcArrows.fill = GridBagConstraints.HORIZONTAL;
 
 		for (int i = 0; i < 42; i++) {
 			this.arrows[i] = new JLabel(this.emptyIcon);
 			this.arrows[i].setOpaque(false);
 			
 			this.gbcArrows.gridy = Math.round(i / 7);
 			this.gbcArrows.gridx = i % 7;
 			this.gbcArrows.gridheight = 1;
 			
 			gbl.setConstraints(this.arrows[i], this.gbcArrows);
 			this.boardArrows.add(this.arrows[i]);
 		}
 	}
 	
 	private void drawFigures(){		
 		GridBagLayout gbl = new GridBagLayout();
 		this.boardFigures.setLayout(gbl);
 		this.boardFigures.setOpaque(false);
 		this.gbcFigures.fill = GridBagConstraints.HORIZONTAL;
 		
 		for (int i=0; i < 42; i++){
 			this.figures[i] = new JLabel(this.emptyIcon);
 			this.figures[i].setOpaque(false);
 			
 			this.gbcFigures.gridy = Math.round(i / 7);
 			this.gbcFigures.gridx = i % 7;
 			this.gbcFigures.gridheight = 1;
 
 			gbl.setConstraints(this.figures[i], gbcFigures);
 			this.boardFigures.add(this.figures[i]);
 		}
 	}
 	
 	private void drawButtons() {
 		GridBagLayout gbl = new GridBagLayout();
 		this.boardButtons.setLayout(gbl);
 		this.boardButtons.setOpaque(false);
 		gbcButtons.fill = GridBagConstraints.HORIZONTAL;
 
 		for (int i = 0; i < 42; i++) {
 			this.fieldButtons[i] = new JButton();
 			this.fieldButtons[i].setActionCommand(Integer.toString(i));
 			this.fieldButtons[i].addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent ae) {
 					int position = Integer.parseInt(ae.getActionCommand());
 					//printLog("Position: " + (position % 7) + " / " + (position / 7) + " | Array: " + position);
 					
 					switch(gamePhase){
 					case "initFlag":
 						if(position > 27){
 							printLog("Setze die Falle");
 							initialAssignment[position] = FigureKind.FLAG;
 							redrawInitialAssignment();
 							positionFlag = position;
 							gamePhase = "initTrap";
 							printLog("Setze die Falle");
 						}
 						else
 							printLog("Nicht im Startgebiet");
 						break;
 					case "initTrap":
 						if((position > 27) && (position != positionFlag)){
 							positionTrap = position;
 							halbmanuellShuffle();
 							redrawInitialAssignment();
 							acceptLineUp.setVisible(true);
 							mixLineUp.setVisible(true);
 							gamePhase = "initLineUpChange";
 							printLog("Passe die Startaufstellung an");
 						}
 						else
 							if(position == positionFlag)
 								printLog("Nicht auf die Flagge setzbar");
 							else
 								printLog("Nicht im Startgebiet");
 							break;						
 					case "initLineUpChange":
 						lineUpChange(position, pickedPosition);
 						break;
 					case "gamePhase":
 						moveFigure(position,choosenPosition);
 						break;
 					default:
 						printLog("Button broken");
 						System.err.println("Button broken");
 					}
 				}
 			});
 			this.fieldButtons[i].setOpaque(false);
 			this.fieldButtons[i].setContentAreaFilled(false);
 			this.fieldButtons[i].setBorderPainted(false);
 			this.fieldButtons[i].setRolloverEnabled(false);
 			this.fieldButtons[i].setPreferredSize(new Dimension(100, 100));
 			this.gbcButtons.gridy = Math.round(i / 7);
 			this.gbcButtons.gridx = i % 7;
 			this.gbcButtons.gridheight = 1;
 
 			gbl.setConstraints(this.fieldButtons[i], gbcButtons);
 			this.boardButtons.add(this.fieldButtons[i]);
 
 		}
 
 	}
 	
 	private void moveFigure(int pos1, int pos2){
 		try{
 			this.board = this.game.getField();
 		}
 		catch (RemoteException re){
 			//TODO
 		}
 		
 		if(choosen){
 			//printLog("From: "+pos2+" to: "+pos1);
 			try{
 				game.move(this.player, pos2, pos1);
 			}
 			catch (RemoteException re){
 				// TODO
 			}
 			finally{
 				for (int i=0; i < 42; i++){
 					this.arrows[i].setIcon(emptyIcon);
 				}
 				this.redraw();
 				choosen = false;
 			}
 		}
 		else{
 			if(this.board[pos1].belongsTo(this.player)){
 				int counter=0;
 				try{
					if (((this.board[pos1+1] == null) || !this.board[pos1+1].belongsTo(this.player)) && ((pos1+1) % 7 != 0)){
 						this.arrows[pos1+1].setIcon(arrowRight);
 						counter++;
 					}
 					if (((this.board[pos1-1] == null) || !this.board[pos1-1].belongsTo(this.player)) && (pos1 % 7 != 0)){
 						this.arrows[pos1-1].setIcon(arrowLeft);
 						counter++;
 					}
 					if (((this.board[pos1+7] == null) || !this.board[pos1+7].belongsTo(this.player)) && (pos1 <= 34)){
 						this.arrows[pos1+7].setIcon(arrowDown);
 						counter++;
 					}
 					if (((this.board[pos1-7] == null) || !this.board[pos1-7].belongsTo(this.player)) && (pos1 >= 7)){
 						this.arrows[pos1-7].setIcon(arrowUp);
 						counter++;
 					}
 				}
 				catch(IndexOutOfBoundsException ioobe){
 					//TODO
 				}
 				if(counter>0) this.arrows[pos1].setIcon(boarderIcon);
 				else printLog("Keine Zuege fuer dieses Feld.");
 				
 			}
 			else{
 				printLog("Nicht moeglich");
 			}
 			
 			choosenPosition = pos1;
 			choosen = true;
 		}
 	}
 		
 		
 
 	private void halbmanuellShuffle(){
 		createRandomLineup();
 		int flagBuffer=0;
 		int trapBuffer=1;
 		for(int i=28; i<42;i++){
 			if(this.initialAssignment[i] == FigureKind.FLAG)
 				flagBuffer = i;
 			if(this.initialAssignment[i] == FigureKind.TRAP)
 				trapBuffer = i;
 		}
 		// TODO Bug: Sometimes the trap get switched
 		switchField(positionFlag, flagBuffer);
 		if(positionFlag != trapBuffer) switchField(positionTrap, trapBuffer);		
 	}
 	
 	private void lineUpChange(int pos1, int pos2){
 
 		if((this.initialAssignment[pos1]!= null)){
 			if(pick){
 				if(this.initialAssignment[pos1] == FigureKind.FLAG)
 					positionFlag = pos2;
 				if(this.initialAssignment[pos2] == FigureKind.FLAG)
 					positionFlag = pos1;
 				if(this.initialAssignment[pos1] == FigureKind.TRAP)
 					positionTrap = pos2;
 				if(this.initialAssignment[pos2] == FigureKind.TRAP)
 					positionTrap = pos1;
 				switchField(pos1, pos2);
 				pick = false;
 			}
 			else{
 				pickedPosition = pos1;
 				pick = true;
 				printLog("Field picked");
 			}
 		}
 		else
 			printLog("Not your figure");
 		
 	}
 	
 	public void switchField(int pos1, int pos2){
 		FigureKind figureBuffer = this.initialAssignment[pos1];
 		this.initialAssignment[pos1] = this.initialAssignment[pos2];
 		this.initialAssignment[pos2] = figureBuffer;
 		redrawInitialAssignment();
 		printLog("Field switched");
 	}
 	
 	private void bindButtons() {
 		chatInput.addKeyListener(new KeyListener() {
 			@Override
 			public void keyPressed(KeyEvent e) {
 				boolean isEnter = e.getKeyCode() == KeyEvent.VK_ENTER;
 				if (isEnter) {
 					addToChat();
 				}
 			}
 
 			@Override
 			public void keyTyped(KeyEvent e) {
 			}
 
 			@Override
 			public void keyReleased(KeyEvent e) {
 			}
 		});
 	}
 
 	private void addToChat() {
 		String message = chatInput.getText().trim();
 		if (message.length() > 0) {
 			try {
 				game.sendMessage(player, message);
 				chatInput.setText("");
 			} catch (RemoteException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public void hide() {
 		gamePane.setVisible(false);
 	}
 
 	public void startGame(Player player, Game game) {
 		this.player = player;
 		this.game = game;
 		reset();
 		gamePane.setVisible(true);
 		this.askLineup();
 
 	}
 
 	public void receivedMessage(Player sender, String message) {
 
 		if (chat.getText().length() != 0) {
 			chat.append("\n");
 		}
 		String formatted = sender.getNick() + ": " + message;
 		chat.append(formatted);
 		chat.setCaretPosition(chat.getDocument().getLength());
 	}
 
 	public void printLog(String message) {
 		log.append("> " + message + "\n");
 		log.setCaretPosition(log.getDocument().getLength());
 	}
 	
 	private void reset() {
 		chat.setText(null);
		for (int i=0; i < 42; i++){
			this.discovered[i].setIcon(emptyIcon);
			this.arrows[i].setIcon(emptyIcon);
			this.figures[i].setIcon(emptyIcon);	
			initialAssignment[i] = null;
		}
		this.gamePhase = "initFlag";
 	}
 
 	public void redraw() {
 		this.loadPictures();
 		try{
 			board = this.game.getField();
 		}
 		catch (RemoteException e){
 			//TODO
 		}
 		for (int i=0; i < 42; i++){
 			if(this.board[i] != null && this.board[i].belongsTo(this.player) && !this.board[i].isDiscovered()){
 				this.discovered[i].setIcon(discoveredIcon);
 			}
 			else{
 				this.discovered[i].setIcon(emptyIcon);
 			}
 		}
 		
 		for (int i=0; i < 42; i++){
 			if(this.board[i] != null){
 				if(this.board[i].belongsTo(this.player)){
 					switch(this.board[i].getKind()){
 						case TRAP:
 							this.figures[i].setIcon(blueTrap);
 							break;
 						case FLAG:
 							this.figures[i].setIcon(blueFlag);
 							break;
 						case ROCK:
 							this.figures[i].setIcon(blueRock);
 							break;
 						case PAPER:
 							this.figures[i].setIcon(bluePaper);
 							break;
 						case SCISSORS:
 							this.figures[i].setIcon(blueScissors);
 							break;
 						case HIDDEN:
 							this.figures[i].setIcon(unknownIcon);
 							break;
 						default:
 							this.figures[i].setIcon(unknownIcon);
 							break;
 					}
 				}
 				else{
 					switch(this.board[i].getKind()){
 					case TRAP:
 						this.figures[i].setIcon(redTrap);
 						break;
 					case FLAG:
 						this.figures[i].setIcon(redFlag);
 						break;
 					case ROCK:
 						this.figures[i].setIcon(redRock);
 						break;
 					case PAPER:
 						this.figures[i].setIcon(redPaper);
 						break;
 					case SCISSORS:
 						this.figures[i].setIcon(redScissors);
 						break;
 					case HIDDEN:
 						this.figures[i].setIcon(unknownIcon);
 						break;
 					default:
 						this.figures[i].setIcon(unknownIcon);
 						break;
 					}
 				}
 			}
 			else{
 				this.figures[i].setIcon(emptyIcon);
 			}
 				
 		}
 	}
 }
