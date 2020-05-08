 package warborn.view;
 
 import java.awt.Color;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 
 import warborn.model.Warborn;
 import warborn.sound.Sound;
 
 public class MenuView extends JPanel {
 	private static final long serialVersionUID = 1L;
 	private JButton btNewGame, btCredits, btExit, btQuickStart, btStart, btBack, btBack2;
 	private SelectionPanel pSelection;
 	private CreditsPanel pCredits;
 	private Sound sounds;
 	private JTextArea tARaceDescription, tAGodDescription, tAMapDescription;
 	
 	/**
 	 * Create the panel.
 	 */
 	public MenuView(Warborn model) {
 		sounds = new Sound();
 		model.addObserver(sounds);
 		sounds.start();
 		setLayout(null);
 		setSize(model.getWidth(), model.getHeight());
 		btNewGame = new GenericButton("New Game");
 		btNewGame.setLocation(model.getWidth()-200, 50);
 		btNewGame.setSize(150, 150);
 		add(btNewGame, 0);
 
 		btCredits = new GenericButton("Credits");
 		btCredits.setLocation(model.getWidth()-200, 250);
 		btCredits.setSize(150, 150);
 		add(btCredits, 0);
 		
 		btExit = new GenericButton("Exit Game");
 		btExit.setLocation(model.getWidth()-200, 450);
 		btExit.setSize(150, 150);
 		add(btExit, 0);
 		
 		btQuickStart = new GenericButton("Quick Start");
 		btQuickStart.setLocation(100, model.getHeight()-250);
 		btQuickStart.setSize(150, 150);
 		btQuickStart.setVisible(false);
 		add(btQuickStart, 0);
 		
 		btStart = new GenericButton("Start Game");
 		btStart.setLocation(300, model.getHeight()-250);
 		btStart.setSize(150, 150);
 		btStart.setVisible(false);
 		add(btStart, 0);
 		
 		btBack = new GenericButton("Back");
 		btBack.setLocation(500, model.getHeight()-250);
 		btBack.setSize(150, 150);
 		btBack.setVisible(false);
 		add(btBack, 0);
 		
 		btBack2 = new GenericButton("Back");
 		btBack2.setLocation(100, model.getHeight()-250);
 		btBack2.setSize(150, 150);
 		btBack2.setVisible(false);
 		add(btBack2, 0);
 
 		pSelection = new SelectionPanel(model);
 		pSelection.setLocation((int)(this.getWidth()*0.5), 0);
 		pSelection.setVisible(false);
 		add(pSelection);
 		
 		pCredits = new CreditsPanel();
 		pCredits.setLocation((int)(this.getWidth()*0.25), (int)(this.getHeight()*0.10));
 		pCredits.setVisible(false);
 		add(pCredits);
 		
 		tARaceDescription = new JTextArea();
 		tARaceDescription.setSize((int)(model.getHeight()*0.5), (int)(model.getWidth()*0.3));
 		tARaceDescription.setLocation((int)(model.getHeight()*0.8), (int)(model.getWidth()*0.625));
 		//tARaceDescription.setEditable(false);
 		tARaceDescription.setVisible(false);
 		add(tARaceDescription);
 		tARaceDescription.setText("HELL YEAH!");
 		
 		
 	}
 	
 	public JButton[] getButtonArray(){
 		return new JButton[]{
 			btNewGame,
 			btCredits,
 			btExit,
 			btQuickStart,
 			btStart,
 			btBack,
 			btBack2,
 			pSelection.getAddPlayerButton(),
 			pSelection.getRemovePlayerButton(),
 			pSelection.getStartButton(),
 		};
 	}
 	
 	public JButton[] getColorButtons(){
 		return pSelection.getColorButtons();
 	}
 	
 	public String[] getNames(){
 		return pSelection.getNames();
 	}
 	
 	public Color[] getColors(){
 		return pSelection.getColors();
 	}
 	
 	public int[] getRaces(){
 		return pSelection.getRaces();
 	}
 	
 	public int[] getBackgrounds(){
 		return pSelection.getBackgrounds();
 	}
 	
 	@SuppressWarnings("rawtypes")
 	public JComboBox getMapComboBox(){
 		return pSelection.getMapComboBox();
 	}
 	
 	public int getSelectedMapIndex(){
 		return pSelection.getSelectedMapIndex();
 	}
 	
 	public void showCredits(){
 		btBack.setVisible(true);
 		btCredits.setVisible(true);
 		pCredits.setVisible(true);
 		
 		btNewGame.setVisible(false);
 		btCredits.setEnabled(false);
 		btExit.setVisible(false);
 	}
 	
 	public void openNewGameMenu(){
 		btQuickStart.setVisible(true);
 		btStart.setVisible(true);
 		btBack.setVisible(true);
 		btNewGame.setVisible(true);
 		
 		btNewGame.setEnabled(false);
 		btCredits.setVisible(false);
 		btExit.setVisible(false);
 		btBack2.setVisible(false);
 		pSelection.setVisible(false);
 		tARaceDescription.setVisible(false);
 	}
 	
 	public void openSelectionMenu(){
 		btQuickStart.setVisible(false);
 		btStart.setVisible(false);
 		btBack.setVisible(false);
 		btNewGame.setVisible(false);
 		btCredits.setVisible(false);
 		btExit.setVisible(false);
 		
 		btBack2.setVisible(true);
 		pSelection.setVisible(true);
 		tARaceDescription.setVisible(true);
 	}
 	
 	public void closeNewGameMenu(){
 		btQuickStart.setVisible(false);
 		btStart.setVisible(false);
 		btBack.setVisible(false);
 		pCredits.setVisible(false);
 
 		btNewGame.setEnabled(true);
 		btNewGame.setVisible(true);
 		btCredits.setEnabled(true);
 		btCredits.setVisible(true);
 		btExit.setVisible(true);
 	}
 	
 	public void updateMap(){
 		pSelection.updateMap();
 	}
 	
 	public void addPlayer(){
 		pSelection.addPlayer();
 	}
 	
 	public void removePlayer(){
 		pSelection.removePlayer();
 	}
 
 }
