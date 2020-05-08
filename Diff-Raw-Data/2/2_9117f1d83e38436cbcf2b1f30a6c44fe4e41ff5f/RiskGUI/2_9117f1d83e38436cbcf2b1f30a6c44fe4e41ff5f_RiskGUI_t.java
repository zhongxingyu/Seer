 package gui.risk;
 
 import gui.ActionDialog;
 import gui.AppClient;
 import gui.DialogBox;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseListener;
 import org.eclipse.swt.events.ShellEvent;
 import org.eclipse.swt.events.ShellListener;
 import org.eclipse.swt.graphics.Device;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 
 import server.GameMethodsImpl.Phase;
 import valueobjects.BonusCard;
 import valueobjects.Player;
 import valueobjects.PlayerCollection;
 import valueobjects.Territory;
 
 import commons.GameMethods;
 
 /**
  * @author Hendrik
  */
 public class RiskGUI {
 
 	private final int defaultSizeX = 800;
 	private final int defaultSizeY = 600;
 
 	private AppClient app;
 	private Territory targetTerritory;
 	private Player attackedPlayer;
 	private Territory sourceTerritory;
 	private Image[] bonusImage = new Image[4];
 	private Label[] bonusLabelStack;
 	private HashMap<String, Button> buttons = new HashMap<String,Button>();
 	private Button[] buttonArray = new Button[42];
 	private Composite cardWindow;
 	private Player currentPlayer;
 	private Device dev;
 	private Display display;
 	private String events = "";
 	private Text eventWindow = null;
 	private GameMethods game;
 	private Player guiPlayer;
 	private int imgWidth;
 	private int imgHeight;
 	private Composite mainWindow;
 	private Image map;
 	private final int maxSizeX = 1920;
 	private final int maxSizeY = 1080;
 	private Button nextPhaseButton;
 	private Phase phase;
 	private Button[] playerButtons;
 	private PlayerCollection players;
 	private Image[] roundImage = new Image[3];
 	private Button roundButton;
 	private Shell shell;
 	private HashMap<String, Territory> territories;
 	private Image[] unitImage = new Image[6];
 
 
 	/**
 	 * creates a new GUI and Game
 	 * 
 	 * @param display
 	 *            the Display on which the shell is shown
 	 */
 	public RiskGUI(Display display, AppClient app, final GameMethods game) {
 		this.game = game;
 		this.app = app;
 		this.display = display;
 		this.guiPlayer = app.getClient();
 	}
 
 	public void prepare(){
 		territories = game.getTerritories();
 		players = game.getPlayers();
 
 		currentPlayer = game.getActivePlayer();
 
 		System.out.println("CP in prepare(); " + currentPlayer);
 
 		// Create a new Shell with Title
 		shell = new Shell(display);
 		shell.setText(AppClient.name + " | " + guiPlayer.getName());
 
 		// Set size to default
 		shell.setSize(defaultSizeX, defaultSizeY);
 
 		// Create a new Composite make it default size adjust the Shell
 		mainWindow = new Composite(shell, SWT.NONE);
 		mainWindow.setSize(defaultSizeX, defaultSizeY);
 		shell.pack();
 
 		// Shell set minimum size to default size
 		shell.setMinimumSize(shell.getBounds().width, shell.getBounds().height);
 
 		// Composite set size to max size
 		mainWindow.setSize(maxSizeX, maxSizeY);
 
 		dev = shell.getDisplay();
 
 		try {
 			map = new Image(dev, "assets/riskClean.png");
 		} catch (Exception e) {
 			System.out.println("Cannot load image");
 			System.out.println(e.getMessage());
 			System.exit(1);
 		}
 
 		imgWidth = map.getBounds().width;
 		imgHeight = map.getBounds().height;
 
 		mainWindow.setBackgroundImage(map);
 
 		createButtons();
 
 		createEventWindow();
 
 		createCardWindow();
 
 		createRoundWindow();
 
 		// resize listener which auto centers the game
 		shell.addListener(SWT.Resize, new Listener() {
 			public void handleEvent(Event e) {
 				centerImage(mainWindow);
 			}
 		});
 
 		// defines all Actions if Shell is closed, deactivated, deinconified or
 		// iconified
 		shell.addShellListener(new ShellListener() {
 			@Override
 			public void shellActivated(ShellEvent e) {
 				centerImage(mainWindow);
 			}
 
 			@Override
 			public void shellClosed(ShellEvent e) {
 
 			}
 
 			@Override
 			public void shellDeactivated(ShellEvent e) {
 				centerImage(mainWindow);
 				;
 			}
 
 			@Override
 			public void shellDeiconified(ShellEvent e) {
 				centerImage(mainWindow);
 			}
 
 			@Override
 			public void shellIconified(ShellEvent e) {
 				centerImage(mainWindow);
 			}
 		});
 
 		shell.open();
 	}
 
 	private void createRoundWindow() {
 		roundButton = new Button(mainWindow, SWT.PUSH);
 		roundImage[0] = new Image(dev, "assets/roundSUPPLY.png");
 		roundImage[1] = new Image(dev, "assets/roundATTACK.png");
 		roundImage[2] = new Image(dev, "assets/roundMOVEMENT.png");
 	}
 
 	/**
 	 * Starts the game loop
 	 */
 	public void start() {
 		while (!shell.isDisposed()) {
 			if (!display.readAndDispatch()) {
 				display.sleep();
 			}
 		}
 	}
 
 	/**
 	 * add a new String to the Event Window
 	 * 
 	 * @param string
 	 *            String which should be added
 	 */
 	private void eventWindowAppendText(String string) {
 		events = string + "\n" + events;
 		eventWindow.setText(events);
 	}
 
 	/**
 	 * creates an eventWindow which contains all Messages which could be usefull
 	 * for the User
 	 */
 	private void createEventWindow() {
 		mainWindow.setBackgroundMode(SWT.INHERIT_DEFAULT);
 		eventWindow = new Text(mainWindow, SWT.NONE | SWT.MULTI | SWT.WRAP
 				| SWT.RESIZE | SWT.V_SCROLL);
 		eventWindow.setBounds(0, 0, 250, 50);
 		eventWindow.setText(events);
 		eventWindow.setLocation(new Point(
 				((imgWidth - shell.getClientArea().width) / 2
 						+ shell.getClientArea().width - 250),
 						((imgHeight - shell.getClientArea().height) / 2
 								+ shell.getClientArea().height - 50)));
 	}
 
 	/**
 	 * creates a Button on every Territory and adds a Tooltip and lable to it
 	 */
 	private void createButtons() {
 
 		// load images for Button
 		try {
 			// load unit pictures
 			// BLACK
 			unitImage[0] = new Image(dev, "assets/unitsBLACK.png");
 			// BLUE
 			unitImage[1] = new Image(dev, "assets/unitsGREEN.png");
 			// RED
 			unitImage[2] = new Image(dev, "assets/unitsRED.png");
 			// YELLOW
 			unitImage[3] = new Image(dev, "assets/unitsYELLOW.png");
 			// PINK
 			unitImage[4] = new Image(dev, "assets/unitsPINK.png");
 			// GREEN
 			unitImage[5] = new Image(dev, "assets/unitsBLUE.png");
 
 			// rescale unit icons for Buttons to 16x16px
 			for (int i = 0; i < unitImage.length; i++) {
 				unitImage[i] = new Image(dev, unitImage[i].getImageData()
 						.scaledTo(16,
 								16));
 			}
 		} catch (Exception e) {
 			System.out.println("Cannot load image");
 			System.out.println(e.getMessage());
 			System.exit(1);
 		}
 
 		// clear composite
 		for (Control kid : mainWindow.getChildren()) {
 			kid.dispose();
 		}
 
 		// size of the Buttons
 		int buttonSizeW = 45;
 		int buttonSizeH = 20;
 
 		Territory territory;
 
 		// NORD-AMERIKA
 
 		// Alaska
 		territory = territories.get("Alaska");
 		buttonArray[0] = new Button(mainWindow, SWT.PUSH);
 		buttonArray[0].setData("name", "Alaska");
 		buttonArray[0].setImage(unitImage[territory.getOwner().getColor()]);
 		buttonArray[0].setText(String.valueOf(territory.getUnits()));
 		buttonArray[0].setBounds(615 - buttonSizeW / 2, 332 - buttonSizeH / 2,
 				buttonSizeW, buttonSizeH);
 		buttonArray[0].setToolTipText(territory.getName() + " gehört "
 				+ territory.getOwner().getName());
 
 		buttons.put("Alaska", buttonArray[0]);
 
 		// Nordwest-Territorium
 		territory = territories.get("Nordwest-Territorium");
 		buttonArray[1] = new Button(mainWindow, SWT.PUSH);
 		buttonArray[1].setData("name", "Nordwest-Territorium");
 		buttonArray[1].setImage(unitImage[territory.getOwner().getColor()]);
 		buttonArray[1].setText(String.valueOf(territory.getUnits()));
 		buttonArray[1].setBounds(690 - buttonSizeW / 2, 342 - buttonSizeH / 2,
 				buttonSizeW, buttonSizeH);
 		buttonArray[1].setToolTipText(territory.getName() + " gehört "
 				+ territory.getOwner().getName());
 
 		buttons.put("Nordwest-Territorium", buttonArray[1]);
 
 		// Alberta
 		territory = territories.get("Alberta");
 		buttonArray[2] = new Button(mainWindow, SWT.PUSH);
 		buttonArray[2].setData("name", "Alberta");
 		buttonArray[2].setImage(unitImage[territory.getOwner().getColor()]);
 		buttonArray[2].setText(String.valueOf(territory.getUnits()));
 		buttonArray[2].setBounds(682 - buttonSizeW / 2, 382 - buttonSizeH / 2,
 				buttonSizeW, buttonSizeH);
 		buttonArray[2].setToolTipText(territory.getName() + " gehört "
 				+ territory.getOwner().getName());
 
 		buttons.put("Alberta", buttonArray[2]);
 
 		// Ontario
 		territory = territories.get("Ontario");
 		buttonArray[3] = new Button(mainWindow, SWT.PUSH);
 		buttonArray[3].setData("name", "Ontario");
 		buttonArray[3].setImage(unitImage[territory.getOwner().getColor()]);
 		buttonArray[3].setText(String.valueOf(territory.getUnits()));
 		buttonArray[3].setBounds(736 - buttonSizeW / 2, 400 - buttonSizeH / 2,
 				buttonSizeW, buttonSizeH);
 		buttonArray[3].setToolTipText(territory.getName() + " gehört "
 				+ territory.getOwner().getName());
 
 		buttons.put("Ontario", buttonArray[3]);
 
 		// Quebec
 		territory = territories.get("Quebec");
 		buttonArray[4] = new Button(mainWindow, SWT.PUSH);
 		buttonArray[4].setData("name", "Quebec");
 		buttonArray[4].setImage(unitImage[territory.getOwner().getColor()]);
 		buttonArray[4].setText(String.valueOf(territory.getUnits()));
 		buttonArray[4].setBounds(790 - buttonSizeW / 2, 404 - buttonSizeH / 2,
 				buttonSizeW, buttonSizeH);
 		buttonArray[4].setToolTipText(territory.getName() + " gehört "
 				+ territory.getOwner().getName());
 
 		buttons.put("Quebec", buttonArray[4]);
 
 		// Weststaaten
 		territory = territories.get("Weststaaten");
 		buttonArray[5] = new Button(mainWindow, SWT.PUSH);
 		buttonArray[5].setData("name", "Weststaaten");
 		buttonArray[5].setImage(unitImage[territory.getOwner().getColor()]);
 		buttonArray[5].setText(String.valueOf(territory.getUnits()));
 		buttonArray[5].setBounds(687 - buttonSizeW / 2, 445 - buttonSizeH / 2,
 				buttonSizeW, buttonSizeH);
 		buttonArray[5].setToolTipText(territory.getName() + " gehört "
 				+ territory.getOwner().getName());
 
 		buttons.put("Weststaaten", buttonArray[5]);
 
 		// Oststaaten
 		territory = territories.get("Oststaaten");
 		buttonArray[6] = new Button(mainWindow, SWT.PUSH);
 		buttonArray[6].setData("name", "Oststaaten");
 		buttonArray[6].setImage(unitImage[territory.getOwner().getColor()]);
 		buttonArray[6].setText(String.valueOf(territory.getUnits()));
 		buttonArray[6].setBounds(750 - buttonSizeW / 2, 465 - buttonSizeH / 2,
 				buttonSizeW, buttonSizeH);
 		buttonArray[6].setToolTipText(territory.getName() + " gehört "
 				+ territory.getOwner().getName());
 
 		buttons.put("Oststaaten", buttonArray[6]);
 
 		// Mittel-Amerika
 		territory = territories.get("Mittelamerika");
 		buttonArray[7] = new Button(mainWindow, SWT.PUSH);
 		buttonArray[7].setData("name", "Mittelamerika");
 		buttonArray[7].setImage(unitImage[territory.getOwner().getColor()]);
 		buttonArray[7].setText(String.valueOf(territory.getUnits()));
 		buttonArray[7].setBounds(690 - buttonSizeW / 2, 512 - buttonSizeH / 2,
 				buttonSizeW, buttonSizeH);
 		buttonArray[7].setToolTipText(territory.getName() + " gehört "
 				+ territory.getOwner().getName());
 
 		buttons.put("Mittelamerika", buttonArray[7]);
 
 		// Grönland
 		territory = territories.get("Grönland");
 		buttonArray[8] = new Button(mainWindow, SWT.PUSH);
 		buttonArray[8].setData("name", "Grönland");
 		buttonArray[8].setImage(unitImage[territory.getOwner().getColor()]);
 		buttonArray[8].setText(String.valueOf(territory.getUnits()));
 		buttonArray[8].setBounds(843 - buttonSizeW / 2, 320 - buttonSizeH / 2,
 				buttonSizeW, buttonSizeH);
 		buttonArray[8].setToolTipText(territory.getName() + " gehört "
 				+ territory.getOwner().getName());
 
 		buttons.put("Grönland", buttonArray[8]);
 
 		// SÜDAMERIKA
 
 		// Venezuela
 		territory = territories.get("Venezuela");
 		buttonArray[9] = new Button(mainWindow, SWT.PUSH);
 		buttonArray[9].setData("name", "Venezuela");
 		buttonArray[9].setImage(unitImage[territory.getOwner().getColor()]);
 		buttonArray[9].setText(String.valueOf(territory.getUnits()));
 		buttonArray[9].setBounds(748 - buttonSizeW / 2, 571 - buttonSizeH / 2,
 				buttonSizeW, buttonSizeH);
 		buttonArray[9].setToolTipText(territory.getName() + " gehört "
 				+ territory.getOwner().getName());
 
 		buttons.put("Venezuela", buttonArray[9]);
 
 		// Peru
 		territory = territories.get("Peru");
 		buttonArray[10] = new Button(mainWindow, SWT.PUSH);
 		buttonArray[10].setData("name", "Peru");
 		buttonArray[10].setImage(unitImage[territory.getOwner().getColor()]);
 		buttonArray[10].setText(String.valueOf(territory.getUnits()));
 		buttonArray[10].setBounds(757 - buttonSizeW / 2, 641 - buttonSizeH / 2,
 				buttonSizeW, buttonSizeH);
 		buttonArray[10].setToolTipText(territory.getName() + " gehört "
 				+ territory.getOwner().getName());
 
 		buttons.put("Peru", buttonArray[10]);
 
 		// Brasilien
 		territory = territories.get("Brasilien");
 		buttonArray[11] = new Button(mainWindow, SWT.PUSH);
 		buttonArray[11].setData("name", "Brasilien");
 		buttonArray[11].setImage(unitImage[territory.getOwner().getColor()]);
 		buttonArray[11].setText(String.valueOf(territory.getUnits()));
 		buttonArray[11].setBounds(810 - buttonSizeW / 2, 623 - buttonSizeH / 2,
 				buttonSizeW, buttonSizeH);
 		buttonArray[11].setToolTipText(territory.getName() + " gehört "
 				+ territory.getOwner().getName());
 
 		buttons.put("Brasilien", buttonArray[11]);
 
 		// Argentinien
 		territory = territories.get("Argentinien");
 		buttonArray[12] = new Button(mainWindow, SWT.PUSH);
 		buttonArray[12].setData("name", "Argentinien");
 		buttonArray[12].setImage(unitImage[territory.getOwner().getColor()]);
 		buttonArray[12].setText(String.valueOf(territory.getUnits()));
 		buttonArray[12].setBounds(765 - buttonSizeW / 2, 710 - buttonSizeH / 2,
 				buttonSizeW, buttonSizeH);
 		buttonArray[12].setToolTipText(territory.getName() + " gehört "
 				+ territory.getOwner().getName());
 
 		buttons.put("Argentinien", buttonArray[12]);
 
 		// EUROPA
 
 		// Island
 		territory = territories.get("Island");
 		buttonArray[13] = new Button(mainWindow, SWT.PUSH);
 		buttonArray[13].setData("name", "Island");
 		buttonArray[13].setImage(unitImage[territory.getOwner().getColor()]);
 		buttonArray[13].setText(String.valueOf(territory.getUnits()));
 		buttonArray[13].setBounds(910 - buttonSizeW / 2, 378 - buttonSizeH / 2,
 				buttonSizeW, buttonSizeH);
 		buttonArray[13].setToolTipText(territory.getName() + " gehört "
 				+ territory.getOwner().getName());
 
 		buttons.put("Island", buttonArray[13]);
 
 		// Skandinavien
 		territory = territories.get("Skandinavien");
 		buttonArray[14] = new Button(mainWindow, SWT.PUSH);
 		buttonArray[14].setData("name", "Skandinavien");
 		buttonArray[14].setImage(unitImage[territory.getOwner().getColor()]);
 		buttonArray[14].setText(String.valueOf(territory.getUnits()));
 		buttonArray[14].setBounds(968 - buttonSizeW / 2, 383 - buttonSizeH / 2,
 				buttonSizeW, buttonSizeH);
 		buttonArray[14].setToolTipText(territory.getName() + " gehört "
 				+ territory.getOwner().getName());
 
 		buttons.put("Skandinavien", buttonArray[14]);
 
 		// Großbritannien
 		territory = territories.get("Großbritannien");
 		buttonArray[15] = new Button(mainWindow, SWT.PUSH);
 		buttonArray[15].setData("name", "Großbritannien");
 		buttonArray[15].setImage(unitImage[territory.getOwner().getColor()]);
 		buttonArray[15].setText(String.valueOf(territory.getUnits()));
 		buttonArray[15].setBounds(892 - buttonSizeW / 2, 444 - buttonSizeH / 2,
 				buttonSizeW, buttonSizeH);
 		buttonArray[15].setToolTipText(territory.getName() + " gehört "
 				+ territory.getOwner().getName());
 
 		buttons.put("Großbritannien", buttonArray[15]);
 
 		// MittelEuropa
 		territory = territories.get("Mitteleuropa");
 		buttonArray[16] = new Button(mainWindow, SWT.PUSH);
 		buttonArray[16].setData("name", "Mitteleuropa");
 		buttonArray[16].setImage(unitImage[territory.getOwner().getColor()]);
 		buttonArray[16].setText(String.valueOf(territory.getUnits()));
 		buttonArray[16].setBounds(967 - buttonSizeW / 2, 454 - buttonSizeH / 2,
 				buttonSizeW, buttonSizeH);
 		buttonArray[16].setToolTipText(territory.getName() + " gehört "
 				+ territory.getOwner().getName());
 
 		buttons.put("Mitteleuropa", buttonArray[16]);
 
 		// SüdEuropa
 		territory = territories.get("Südeuropa");
 		buttonArray[17] = new Button(mainWindow, SWT.PUSH);
 		buttonArray[17].setData("name", "Südeuropa");
 		buttonArray[17].setImage(unitImage[territory.getOwner().getColor()]);
 		buttonArray[17].setText(String.valueOf(territory.getUnits()));
 		buttonArray[17].setBounds(976 - buttonSizeW / 2, 503 - buttonSizeH / 2,
 				buttonSizeW, buttonSizeH);
 		buttonArray[17].setToolTipText(territory.getName() + " gehört "
 				+ territory.getOwner().getName());
 
 		buttons.put("Südeuropa", buttonArray[17]);
 
 		// WestEuropa
 		territory = territories.get("Westeuropa");
 		buttonArray[18] = new Button(mainWindow, SWT.PUSH);
 		buttonArray[18].setData("name", "Westeuropa");
 		buttonArray[18].setImage(unitImage[territory.getOwner().getColor()]);
 		buttonArray[18].setText(String.valueOf(territory.getUnits()));
 		buttonArray[18].setBounds(915 - buttonSizeW / 2, 508 - buttonSizeH / 2,
 				buttonSizeW, buttonSizeH);
 		buttonArray[18].setToolTipText(territory.getName() + " gehört "
 				+ territory.getOwner().getName());
 
 		buttons.put("Westeuropa", buttonArray[18]);
 
 		// Ukraine
 		territory = territories.get("Ukraine");
 		buttonArray[19] = new Button(mainWindow, SWT.PUSH);
 		buttonArray[19].setData("name", "Ukraine");
 		buttonArray[19].setImage(unitImage[territory.getOwner().getColor()]);
 		buttonArray[19].setText(String.valueOf(territory.getUnits()));
 		buttonArray[19].setBounds(1042 - buttonSizeW / 2, 415 - buttonSizeH / 2,
 				buttonSizeW, buttonSizeH);
 		buttonArray[19].setToolTipText(territory.getName() + " gehört "
 				+ territory.getOwner().getName());
 
 		buttons.put("Ukraine", buttonArray[19]);
 
 		// AFRIKA
 
 		// Nordwestafrika
 		territory = territories.get("Nordwestafrika");
 		buttonArray[20] = new Button(mainWindow, SWT.PUSH);
 		buttonArray[20].setData("name", "Nordwestafrika");
 		buttonArray[20].setImage(unitImage[territory.getOwner().getColor()]);
 		buttonArray[20].setText(String.valueOf(territory.getUnits()));
 		buttonArray[20].setBounds(938 - buttonSizeW / 2, 606 - buttonSizeH / 2,
 				buttonSizeW, buttonSizeH);
 		buttonArray[20].setToolTipText(territory.getName() + " gehört "
 				+ territory.getOwner().getName());
 
 		buttons.put("Nordwestafrika", buttonArray[20]);
 
 		// Ägypten
 		territory = territories.get("Ägypten");
 		buttonArray[21] = new Button(mainWindow, SWT.PUSH);
 		buttonArray[21].setData("name", "Ägypten");
 		buttonArray[21].setImage(unitImage[territory.getOwner().getColor()]);
 		buttonArray[21].setText(String.valueOf(territory.getUnits()));
 		buttonArray[21].setBounds(998 - buttonSizeW / 2, 577 - buttonSizeH / 2,
 				buttonSizeW, buttonSizeH);
 		buttonArray[21].setToolTipText(territory.getName() + " gehört "
 				+ territory.getOwner().getName());
 
 		buttons.put("Ägypten", buttonArray[21]);
 
 		// Ostafrika
 		territory = territories.get("Ostafrika");
 		buttonArray[22] = new Button(mainWindow, SWT.PUSH);
 		buttonArray[22].setData("name", "Ostafrika");
 		buttonArray[22].setImage(unitImage[territory.getOwner().getColor()]);
 		buttonArray[22].setText(String.valueOf(territory.getUnits()));
 		buttonArray[22].setBounds(1037 - buttonSizeW / 2, 649 - buttonSizeH / 2,
 				buttonSizeW, buttonSizeH);
 		buttonArray[22].setToolTipText(territory.getName() + " gehört "
 				+ territory.getOwner().getName());
 
 		buttons.put("Ostafrika", buttonArray[22]);
 
 		// Kongo
 		territory = territories.get("Kongo");
 		buttonArray[23] = new Button(mainWindow, SWT.PUSH);
 		buttonArray[23].setData("name", "Kongo");
 		buttonArray[23].setImage(unitImage[territory.getOwner().getColor()]);
 		buttonArray[23].setText(String.valueOf(territory.getUnits()));
 		buttonArray[23].setBounds(998 - buttonSizeW / 2, 678 - buttonSizeH / 2,
 				buttonSizeW, buttonSizeH);
 		buttonArray[23].setToolTipText(territory.getName() + " gehört "
 				+ territory.getOwner().getName());
 
 		buttons.put("Kongo", buttonArray[23]);
 
 		// Südafrika
 		territory = territories.get("Südafrika");
 		buttonArray[24] = new Button(mainWindow, SWT.PUSH);
 		buttonArray[24].setData("name", "Südafrika");
 		buttonArray[24].setImage(unitImage[territory.getOwner().getColor()]);
 		buttonArray[24].setText(String.valueOf(territory.getUnits()));
 		buttonArray[24].setBounds(999 - buttonSizeW / 2, 743 - buttonSizeH / 2,
 				buttonSizeW, buttonSizeH);
 		buttonArray[24].setToolTipText(territory.getName() + " gehört "
 				+ territory.getOwner().getName());
 
 		buttons.put("Südafrika", buttonArray[24]);
 
 		// Madagaskar
 		territory = territories.get("Madagaskar");
 		buttonArray[25] = new Button(mainWindow, SWT.PUSH);
 		buttonArray[25].setData("name", "Madagaskar");
 		buttonArray[25].setImage(unitImage[territory.getOwner().getColor()]);
 		buttonArray[25].setText(String.valueOf(territory.getUnits()));
 		buttonArray[25].setBounds(1075 - buttonSizeW / 2, 745 - buttonSizeH / 2,
 				buttonSizeW, buttonSizeH);
 		buttonArray[25].setToolTipText(territory.getName() + " gehört "
 				+ territory.getOwner().getName());
 
 		buttons.put("Madagaskar", buttonArray[25]);
 
 		// ASIEN
 
 		// Ural
 		territory = territories.get("Ural");
 		buttonArray[26] = new Button(mainWindow, SWT.PUSH);
 		buttonArray[26].setData("name", "Ural");
 		buttonArray[26].setImage(unitImage[territory.getOwner().getColor()]);
 		buttonArray[26].setText(String.valueOf(territory.getUnits()));
 		buttonArray[26].setBounds(1121 - buttonSizeW / 2, 404 - buttonSizeH / 2,
 				buttonSizeW, buttonSizeH);
 		buttonArray[26].setToolTipText(territory.getName() + " gehört "
 				+ territory.getOwner().getName());
 
 		buttons.put("Ural", buttonArray[26]);
 
 		// Sibirien
 		territory = territories.get("Sibirien");
 		buttonArray[27] = new Button(mainWindow, SWT.PUSH);
 		buttonArray[27].setData("name", "Sibirien");
 		buttonArray[27].setImage(unitImage[territory.getOwner().getColor()]);
 		buttonArray[27].setText(String.valueOf(territory.getUnits()));
 		buttonArray[27].setBounds(1165 - buttonSizeW / 2, 365 - buttonSizeH / 2,
 				buttonSizeW, buttonSizeH);
 		buttonArray[27].setToolTipText(territory.getName() + " gehört "
 				+ territory.getOwner().getName());
 
 		buttons.put("Sibirien", buttonArray[27]);
 
 		// Jakutien
 		territory = territories.get("Jakutien");
 		buttonArray[28] = new Button(mainWindow, SWT.PUSH);
 		buttonArray[28].setData("name", "Jakutien");
 		buttonArray[28].setImage(unitImage[territory.getOwner().getColor()]);
 		buttonArray[28].setText(String.valueOf(territory.getUnits()));
 		buttonArray[28].setBounds(1219 - buttonSizeW / 2, 344 - buttonSizeH / 2,
 				buttonSizeW, buttonSizeH);
 		buttonArray[28].setToolTipText(territory.getName() + " gehört "
 				+ territory.getOwner().getName());
 
 		buttons.put("Jakutien", buttonArray[28]);
 
 		// Kamtschatka
 		territory = territories.get("Kamtschatka");
 		buttonArray[29] = new Button(mainWindow, SWT.PUSH);
 		buttonArray[29].setData("name", "Kamtschatka");
 		buttonArray[29].setImage(unitImage[territory.getOwner().getColor()]);
 		buttonArray[29].setText(String.valueOf(territory.getUnits()));
 		buttonArray[29].setBounds(1285 - buttonSizeW / 2, 346 - buttonSizeH / 2,
 				buttonSizeW, buttonSizeH);
 		buttonArray[29].setToolTipText(territory.getName() + " gehört "
 				+ territory.getOwner().getName());
 
 		buttons.put("Kamtschatka", buttonArray[29]);
 
 		// Irkutsk
 		territory = territories.get("Irkutsk");
 		buttonArray[30] = new Button(mainWindow, SWT.PUSH);
 		buttonArray[30].setData("name", "Irkutsk");
 		buttonArray[30].setImage(unitImage[territory.getOwner().getColor()]);
 		buttonArray[30].setText(String.valueOf(territory.getUnits()));
 		buttonArray[30].setBounds(1200 - buttonSizeW / 2, 416 - buttonSizeH / 2,
 				buttonSizeW, buttonSizeH);
 		buttonArray[30].setToolTipText(territory.getName() + " gehört "
 				+ territory.getOwner().getName());
 
 		buttons.put("Irkutsk", buttonArray[30]);
 
 		// Mongolei
 		territory = territories.get("Mongolei");
 		buttonArray[31] = new Button(mainWindow, SWT.PUSH);
 		buttonArray[31].setData("name", "Mongolei");
 		buttonArray[31].setImage(unitImage[territory.getOwner().getColor()]);
 		buttonArray[31].setText(String.valueOf(territory.getUnits()));
 		buttonArray[31].setBounds(1218 - buttonSizeW / 2, 460 - buttonSizeH / 2,
 				buttonSizeW, buttonSizeH);
 		buttonArray[31].setToolTipText(territory.getName() + " gehört "
 				+ territory.getOwner().getName());
 
 		buttons.put("Mongolei", buttonArray[31]);
 
 		// China
 		territory = territories.get("China");
 		buttonArray[32] = new Button(mainWindow, SWT.PUSH);
 		buttonArray[32].setData("name", "China");
 		buttonArray[32].setImage(unitImage[territory.getOwner().getColor()]);
 		buttonArray[32].setText(String.valueOf(territory.getUnits()));
 		buttonArray[32].setBounds(1195 - buttonSizeW / 2, 509 - buttonSizeH / 2,
 				buttonSizeW, buttonSizeH);
 		buttonArray[32].setToolTipText(territory.getName() + " gehört "
 				+ territory.getOwner().getName());
 
 		buttons.put("China", buttonArray[32]);
 
 		// Japan
 		territory = territories.get("Japan");
 		buttonArray[33] = new Button(mainWindow, SWT.PUSH);
 		buttonArray[33].setData("name", "Japan");
 		buttonArray[33].setImage(unitImage[territory.getOwner().getColor()]);
 		buttonArray[33].setText(String.valueOf(territory.getUnits()));
 		buttonArray[33].setBounds(1299 - buttonSizeW / 2, 464 - buttonSizeH / 2,
 				buttonSizeW, buttonSizeH);
 		buttonArray[33].setToolTipText(territory.getName() + " gehört "
 				+ territory.getOwner().getName());
 
 		buttons.put("Japan", buttonArray[33]);
 
 		// Afghanistan
 		territory = territories.get("Afghanistan");
 		buttonArray[34] = new Button(mainWindow, SWT.PUSH);
 		buttonArray[34].setData("name", "Afghanistan");
 		buttonArray[34].setImage(unitImage[territory.getOwner().getColor()]);
 		buttonArray[34].setText(String.valueOf(territory.getUnits()));
 		buttonArray[34].setBounds(1105 - buttonSizeW / 2, 472 - buttonSizeH / 2,
 				buttonSizeW, buttonSizeH);
 		buttonArray[34].setToolTipText(territory.getName() + " gehört "
 				+ territory.getOwner().getName());
 
 		buttons.put("Afghanistan", buttonArray[34]);
 
 		// Mittlerer Osten
 		territory = territories.get("Mittlerer Osten");
 		buttonArray[35] = new Button(mainWindow, SWT.PUSH);
 		buttonArray[35].setData("name", "Mittlerer Osten");
 		buttonArray[35].setImage(unitImage[territory.getOwner().getColor()]);
 		buttonArray[35].setText(String.valueOf(territory.getUnits()));
 		buttonArray[35].setBounds(1054 - buttonSizeW / 2, 558 - buttonSizeH / 2,
 				buttonSizeW, buttonSizeH);
 		buttonArray[35].setToolTipText(territory.getName() + " gehört "
 				+ territory.getOwner().getName());
 
 		buttons.put("Mittlerer Osten", buttonArray[35]);
 
 		// Indien
 		territory = territories.get("Indien");
 		buttonArray[36] = new Button(mainWindow, SWT.PUSH);
 		buttonArray[36].setData("name", "Indien");
 		buttonArray[36].setImage(unitImage[territory.getOwner().getColor()]);
 		buttonArray[36].setText(String.valueOf(territory.getUnits()));
 		buttonArray[36].setBounds(1153 - buttonSizeW / 2, 550 - buttonSizeH / 2,
 				buttonSizeW, buttonSizeH);
 		buttonArray[36].setToolTipText(territory.getName() + " gehört "
 				+ territory.getOwner().getName());
 
 		buttons.put("Indien", buttonArray[36]);
 
 		// Siam
 		territory = territories.get("Siam");
 		buttonArray[37] = new Button(mainWindow, SWT.PUSH);
 		buttonArray[37].setData("name", "Siam");
 		buttonArray[37].setImage(unitImage[territory.getOwner().getColor()]);
 		buttonArray[37].setText(String.valueOf(territory.getUnits()));
 		buttonArray[37].setBounds(1214 - buttonSizeW / 2, 576 - buttonSizeH / 2,
 				buttonSizeW, buttonSizeH);
 		buttonArray[37].setToolTipText(territory.getName() + " gehört "
 				+ territory.getOwner().getName());
 
 		buttons.put("Siam", buttonArray[37]);
 
 		// AUSTRALIEN
 
 		// Indonesien
 		territory = territories.get("Indonesien");
 		buttonArray[38] = new Button(mainWindow, SWT.PUSH);
 		buttonArray[38].setData("name", "Indonesien");
 		buttonArray[38].setImage(unitImage[territory.getOwner().getColor()]);
 		buttonArray[38].setText(String.valueOf(territory.getUnits()));
 		buttonArray[38].setBounds(1226 - buttonSizeW / 2, 664 - buttonSizeH / 2,
 				buttonSizeW, buttonSizeH);
 		buttonArray[38].setToolTipText(territory.getName() + " gehört "
 				+ territory.getOwner().getName());
 
 		buttons.put("Indonesien", buttonArray[38]);
 
 		// Neu-Guinea
 		territory = territories.get("Neu-Guinea");
 		buttonArray[39] = new Button(mainWindow, SWT.PUSH);
 		buttonArray[39].setData("name", "Neu-Guinea");
 		buttonArray[39].setImage(unitImage[territory.getOwner().getColor()]);
 		buttonArray[39].setText(String.valueOf(territory.getUnits()));
 		buttonArray[39].setBounds(1292 - buttonSizeW / 2, 638 - buttonSizeH / 2,
 				buttonSizeW, buttonSizeH);
 		buttonArray[39].setToolTipText(territory.getName() + " gehört "
 				+ territory.getOwner().getName());
 
 		buttons.put("Neu-Guinea", buttonArray[39]);
 
 		// Westaustralien
 		territory = territories.get("West-Australien");
 		buttonArray[40] = new Button(mainWindow, SWT.PUSH);
 		buttonArray[40].setData("name", "West-Australien");
 		buttonArray[40].setImage(unitImage[territory.getOwner().getColor()]);
 		buttonArray[40].setText(String.valueOf(territory.getUnits()));
 		buttonArray[40].setBounds(1258 - buttonSizeW / 2, 740 - buttonSizeH / 2,
 				buttonSizeW, buttonSizeH);
 		buttonArray[40].setToolTipText(territory.getName() + " gehört "
 				+ territory.getOwner().getName());
 
 		buttons.put("West-Australien", buttonArray[40]);
 
 		// Ostaustralien
 		territory = territories.get("Ost-Australien");
 		buttonArray[41] = new Button(mainWindow, SWT.PUSH);
 		buttonArray[41].setData("name", "Ost-Australien");
 		buttonArray[41].setImage(unitImage[territory.getOwner().getColor()]);
 		buttonArray[41].setText(String.valueOf(territory.getUnits()));
 		buttonArray[41].setBounds(1306 - buttonSizeW / 2, 712 - buttonSizeH / 2,
 				buttonSizeW, buttonSizeH);
 		buttonArray[41].setToolTipText(territory.getName() + " gehört "
 				+ territory.getOwner().getName());
 
 		buttons.put("Ost-Australien", buttonArray[41]);
 
 		// add to all buttons a Mouselistener
 		for (int i = 0; i < buttonArray.length; i++) {
 			buttonArray[i].addMouseListener(new MouseListener() {
 				@Override
 				public void mouseDoubleClick(MouseEvent e) {
 
 				}
 
 				@Override
 				public void mouseDown(MouseEvent e) {
 
 				}
 
 				@Override
 				public void mouseUp(MouseEvent e) {
 					performAction(e);
 				}
 			});
 		}
 
 		// Create some Buttons and Display the Player names Colors and current
 		// Unitammount
 
 		playerButtons = new Button[players.size()];
 		int biggestButton = 0;
 
 		for (int i = 0; i < players.size(); i++) {
 			playerButtons[i] = new Button(mainWindow, SWT.PUSH | SWT.LEFT);
 			playerButtons[i].setImage(unitImage[players.get(i).getColor()]);
 			playerButtons[i].setText(players.get(i).getName() + "("
 					+ players.get(i).getAllUnits() + ")");
 			playerButtons[i].setAlignment(SWT.LEFT);
 			playerButtons[i].pack();
 			if (playerButtons[i].getBounds().width > biggestButton) {
 				biggestButton = playerButtons[i].getBounds().width;
 				playerButtons[i].setSize(biggestButton, 20);
 			}
 			playerButtons[i].setLocation(new Point(((imgWidth - shell
 					.getClientArea().width) / 2 + 10), ((imgHeight - shell
 							.getClientArea().height)
 							/ 2
 							+ shell.getClientArea().height
 							- 10 - players.size() * 20 + (i * 20))));
 		}
 
 		// make all Buttons same size
 		for (Button button : playerButtons) {
 			if (button.getBounds().width > biggestButton) {
 				biggestButton = button.getBounds().width;
 				button.setSize(biggestButton, 20);
 			} else if (button.getBounds().width < biggestButton) {
 				button.setSize(biggestButton, 20);
 			}
 		}
 	}
 
 	/**
 	 * updates all Buttons and their Values
 	 */
 	public void updateTerritory(Territory territory) {
 		for (Button button : buttonArray) {
 			if (button.getData("name").equals(territory.getName())) {
 				button.setText(String.valueOf(territory.getUnits()));
 				button.setImage(unitImage[territory.getOwner().getColor()]);
 				button.setText(String.valueOf(territory.getUnits()));
 				button.setToolTipText(territory.getName() + " gehört "+ territory.getOwner().getName());
 
 				System.out.println("Button Inhale von: "+ territory.getName() +" geändert");
 
 				shell.update();
 			}
 
 		}
		eventWindowAppendText("Auf " + territory.getName() + " stehen nun (" + territory.getUnits() + ") Einheiten.");
 		shell.update();
 	}
 
 	public void openEventBox(Player player, String message) {
 
 		if(player.equals(guiPlayer)){
 			new DialogBox(shell, SWT.ICON_INFORMATION, player.getName(), message);
 		}
 	}
 
 	/**
 	 * opens a Dialog after MouseClick according to the Phase
 	 * 
 	 * @param e
 	 *            calling Object
 	 */
 	private void performAction(MouseEvent e) {
 		Button clickedButton = (Button) e.widget;
 
 		if (phase == Phase.PLACEMENT) {
 			game.placeUnits(game.getTerritories().get(clickedButton.getData("name")).getName(), 1);
 		} else if (phase == Phase.ATTACK1) {
 			// SOURCE TERRITORY
 			sourceTerritory = game.getTerritories().get(clickedButton.getData("name"));
 			game.nextPhase();
 
 		} else if (phase == Phase.ATTACK2) {
 			// TARGET TERRITORY
 			targetTerritory = game.getTerritories().get(clickedButton.getData("name"));
 
 			// AMOUNT
 			ActionDialog ad = new ActionDialog(shell, SWT.NONE, phase,
 					sourceTerritory);
 			int units = (Integer) ad.open();
 
 			game.attack(sourceTerritory, targetTerritory, units);
 		} else if (phase == Phase.MOVEMENT1) {
 			sourceTerritory = game.getTerritories().get(clickedButton.getData("name"));
 			game.nextPhase();			
 		} else if(phase == Phase.MOVEMENT2) {
 			targetTerritory = game.getTerritories().get(clickedButton.getData("name"));
 			ActionDialog ad = new ActionDialog(shell, SWT.NONE, phase,
 					sourceTerritory);
 			int units = (Integer) ad.open();
 
 			game.move(sourceTerritory, targetTerritory, units);
 			game.nextPhase();
 		}
 	}
 
 	private void createCardWindow() {
 		cardWindow = new Composite(mainWindow, SWT.NONE);
 		RowLayout rowLayout = new RowLayout();
 		cardWindow.setLayout(rowLayout);
 
 		// load images for Bonus cards
 		try {
 
 			// load bonus pictures
 
 			// Infantary
 			bonusImage[0] = new Image(dev, "assets/bonusINF.png");
 
 			// Calvary
 			bonusImage[1] = new Image(dev, "assets/bonusCAL.png");
 
 			// Artillery
 			bonusImage[2] = new Image(dev, "assets/bonusART.png");
 
 			// Joker alias Wildcard
 			bonusImage[3] = new Image(dev, "assets/bonusJOK.png");
 		} catch (Exception e) {
 			System.out.println("Cannot load image");
 			System.out.println(e.getMessage());
 			System.exit(1);
 		}
 
 		ArrayList<BonusCard> bonuscards = guiPlayer.getBonusCards();
 
 		bonusLabelStack = new Label[bonuscards.size()];
 
 		for (BonusCard bonusCard : bonuscards) {
 			Label label = new Label(cardWindow, SWT.NONE);
 			int type = 0;
 
 			if (bonusCard.getType().equals("Infantry")) {
 				type = 0;
 			}
 			if (bonusCard.getType().equals("Cavalry")) {
 				type = 1;
 			}
 			if (bonusCard.getType().equals("Artillery")) {
 				type = 2;
 			}
 			if (bonusCard.getType().equals("WildCard")) {
 				type = 3;
 			}
 			label.setSize(22, 32);
 			label.setImage(bonusImage[type]);
 			label.pack();
 			cardWindow.pack();
 		}
 
 		cardWindow
 		.setLocation(new Point(
 				((imgWidth - shell.getClientArea().width) / 2
 						+ shell.getClientArea().width - cardWindow
 						.getBounds().width),
 						((imgHeight - shell.getClientArea().height) / 2
 								+ 5)));
 
 		shell.update();
 	}
 
 	public void updateBonusCard(Player player) {
 		
 		if(currentPlayer.equals(guiPlayer)){
 			cardWindow = new Composite(mainWindow, SWT.NONE);
 			RowLayout rowLayout = new RowLayout();
 			cardWindow.setLayout(rowLayout);
 
 			ArrayList<BonusCard> bonuscards = player.getBonusCards();
 			
 			System.out.println(" Bonuskarten auf GUI : " + bonuscards);
 
 			bonusLabelStack = new Label[bonuscards.size()];
 
 			for (BonusCard bonusCard : bonuscards) {
 				Label label = new Label(cardWindow, SWT.NONE);
 
 				int type = 0;
 
 				if (bonusCard.getType() == BonusCard.BonusCardType.Infantry) {
 					type = 0;
 				}
 				if (bonusCard.getType() == BonusCard.BonusCardType.Cavalry) {
 					type = 1;
 				}
 				if (bonusCard.getType() == BonusCard.BonusCardType.Artillery) {
 					type = 2;
 				}
 				if (bonusCard.getType() == BonusCard.BonusCardType.Wildcard) {
 					type = 3;
 				}
 
 				label.setSize(22, 32);
 				label.setImage(bonusImage[type]);
 				label.pack();
 				cardWindow.pack();
 			}
 
 			cardWindow
 			.setLocation(new Point(
 					((imgWidth - shell.getClientArea().width) / 2
 							+ shell.getClientArea().width - cardWindow
 							.getBounds().width),
 							((imgHeight - shell.getClientArea().height) / 2
 									+ 5)));
 
 			shell.update();
 		}
 
 	}
 
 	/**
 	 * Method to find out which territory the Button belongs to
 	 * 
 	 * @param toolTipText
 	 *            of the calling Button
 	 * @return the name of the Territory of the calling Button
 
 	 */
 	/*
 	private String cutTooltip(String toolTipText) {
 		String cutted = "";
 
 		char[] toolTipChar = toolTipText.toCharArray();
 
 		for (int i = 0; i < toolTipChar.length; i++) {
 			if (toolTipChar[i] == ' ') {
 				if (cutted.equals("Mittlerer")) {
 					cutted = "Mittlerer Osten";
 				}
 				return cutted;
 			}
 			cutted += toolTipChar[i];
 		}
 
 		return cutted;
 	}
 	 */
 
 	/**
 	 * Centers an image in the middle of the Shell
 	 */
 	private void centerImage(Composite window) {
 		// center Composition
 		int shellClientWidth = shell.getClientArea().width;
 		int shellClientHeight = shell.getClientArea().height;
 
 		Rectangle bds = window.getBounds();
 		Point p = window.getSize();
 
 		int nLeft = (bds.width - p.x) / 2;
 		int nTop = (bds.height - p.y) / 2;
 
 		window.setLocation(-((imgWidth - shellClientWidth) / 2) + nLeft,
 				-((imgHeight - shellClientHeight) / 2) + nTop);
 	}
 
 	@Override
 	public void finalize() {
 		shell.dispose();
 	}
 
 	/**
 	 * Updates the current player after a NextPlayerAction was received.
 	 */
 	public void updateCurrentPlayer(Player player) {
 		currentPlayer = player;
 		System.out.println("AktiveSpieler in RISKGUI ist: "+ currentPlayer.getName());
 
 		if(!(eventWindow == null)){
 			// Check whether the player equals my player
 			if (player.equals(guiPlayer)) {
 				eventWindowAppendText("Du bist dran");
 			} else {
 				eventWindowAppendText(currentPlayer.getName() + " ist dran.");
 			}
 		}
 	}
 
 	public void defend(Territory attackedTerritory2) {	
 		this.targetTerritory = attackedTerritory2;
 		attackedPlayer = targetTerritory.getOwner();
 
 		//This sould only becalled ONCE!
 		if(guiPlayer.equals(attackedPlayer)){
 			game.nextPhase();
 		}
 	}
 
 	public void updatePhase(Phase phase) {
 
 		PlayerCollection players = game.getPlayers();
 		int zahl = 0;
 
 		for(Player player:players){
 			++zahl;
 			System.out.println(zahl + player.getName());
 		}
 
 		this.phase = phase;
 
 		System.out.println("CurrentPlayer || " + currentPlayer);
 		System.out.println("PHASE || " + phase);
 
 		//change the state of the roundButton to visualize the round
 		if (phase == Phase.PLACEMENT || phase == Phase.TURNINCARDS){
 			roundButton.setImage(roundImage[0]);
 			roundButton.setToolTipText("Bitte platziere deine Verstärkung");
 			roundButton.setLocation(new Point(
 					((imgWidth - shell.getClientArea().width) / 2 + 10),
 					((imgHeight - shell.getClientArea().height) / 2 + 10)));
 
 			roundButton.pack();
 			shell.update();
 		}
 		if (phase == Phase.ATTACK1 || phase == Phase.ATTACK2 || phase == Phase.ATTACK3){
 			roundButton.setImage(roundImage[1]);
 			roundButton.setToolTipText("Bitte führe deine Angriffe durch");
 			roundButton.setLocation(new Point(
 					((imgWidth - shell.getClientArea().width) / 2 + 10),
 					((imgHeight - shell.getClientArea().height) / 2 + 10)));
 
 			roundButton.pack();
 			shell.update();
 		}
 
 		if (phase == Phase.MOVEMENT1 || phase == Phase.MOVEMENT2){
 			roundButton.setImage(roundImage[2]);
 			roundButton.setToolTipText("Verschiebe deine Armeen");
 			roundButton.setLocation(new Point(
 					((imgWidth - shell.getClientArea().width) / 2 + 10),
 					((imgHeight - shell.getClientArea().height) / 2 + 10)));
 
 			roundButton.pack();
 			shell.update();
 		}
 
 		if(currentPlayer.equals(guiPlayer)){
 			roundButton.setEnabled(true);
 			shell.update();
 		} else {
 			roundButton.setEnabled(false);
 			shell.update();
 		}
 
 		if (phase == Phase.PLACEMENT) {
 			for (Button button : buttonArray) {
 
 				if (currentPlayer.equals(guiPlayer)) {
 					Territory territory = game.getTerritories().get(button.getData("name"));
 
 					if (territory.getOwner().equals(guiPlayer)) {
 						button.setEnabled(true);
 					} else {
 						button.setEnabled(false);
 					}
 				} else {
 					for (Button button2 : buttonArray) {
 						button2.setEnabled(false);
 					}
 				}
 			}
 		} else if (phase == Phase.ATTACK1) {
 
 			if (currentPlayer.equals(guiPlayer)) {
 
 				List<Territory> attackingTerritories = game.getMyTerritoriesForAttacking(currentPlayer);
 
 				for (Button button : buttonArray) {
 					button.setEnabled(false);
 				}
 
 				for (Button button : buttonArray) {
 					for(int i = 0 ; i  < attackingTerritories.size(); i++){						
 						if(game.getTerritories().get(button.getData("name")).getName().equals(attackingTerritories.get(i).getName())){
 							button.setEnabled(true);
 						}
 					}
 				}
 
 				nextPhaseButton = new Button(mainWindow, SWT.PUSH);
 				nextPhaseButton.setText("nächste Phase");
 				nextPhaseButton.setLocation(new Point(
 						((imgWidth - shell.getClientArea().width) / 2 + 10 + 50),
 						((imgHeight - shell.getClientArea().height) / 2 + 10)));
 
 				nextPhaseButton.pack();
 				shell.update();
 
 				nextPhaseButton.addMouseListener(new MouseListener() {
 
 					@Override
 					public void mouseUp(MouseEvent e) {
 						game.endAttackPhase();
 						nextPhaseButton.dispose();
 						System.out.println("NEXT PHASE BUTTON IS DISPOSING");
 					}
 
 					@Override
 					public void mouseDown(MouseEvent e) {
 						// TODO Auto-generated method stub
 
 					}
 
 					@Override
 					public void mouseDoubleClick(MouseEvent e) {
 						// TODO Auto-generated method stub
 
 					}
 				});
 			}
 		} else if (phase == Phase.ATTACK2) {
 
 			if (currentPlayer.equals(guiPlayer)) {
 				//meine Länder anzeigen von den ich angreifen kann (mehr als 1 Einheit + feindliches Land)
 
 				List<Territory> attackableTerritories = game.getOpposingNeighborsOf(sourceTerritory);
 
 
 				for (Button button : buttonArray) {
 					button.setEnabled(false);
 				}
 
 				for (Button button : buttonArray) {
 
 					Territory territory = game.getTerritories().get(button.getData("name"));
 
 					for(int i = 0 ; i  < attackableTerritories.size(); i++){
 						if(territory.getName().equals(attackableTerritories.get(i).getName())){
 							button.setEnabled(true);
 						}
 					}
 
 				}
 			}
 		} else if (phase == Phase.ATTACK3) {
 
 			for (Button button : buttonArray) {
 				button.setEnabled(false);
 			}
 
 			if (currentPlayer.equals(guiPlayer)) {
 				nextPhaseButton.dispose();
 			}
 
 			if (attackedPlayer.equals(guiPlayer)){
 
 				ActionDialog ad2 = new ActionDialog(shell, SWT.NONE, phase,
 						targetTerritory);
 
 				int units = (Integer) ad2.open();
 
 				game.defend(targetTerritory, units);
 			}
 		}
 
 		if (phase.equals(Phase.MOVEMENT1)) {
 			if (currentPlayer.equals(guiPlayer)) {
 
 				List<Territory> movingTerritories = game.getMyTerritoriesForMoving(currentPlayer);
 
 				for (Button button : buttonArray) {
 					button.setEnabled(false);
 				}
 
 				for (Button button : buttonArray) {
 					Territory territory = game.getTerritories().get(button.getData("name"));
 
 					for(int i = 0 ; i  < movingTerritories.size(); i++){						
 						if(territory.getName().equals(movingTerritories.get(i).getName())){
 							button.setEnabled(true);
 						}
 					}
 				}
 
 
 				nextPhaseButton = new Button(mainWindow, SWT.PUSH);
 				nextPhaseButton.setText("Runde beenden.");
 				nextPhaseButton.setLocation(new Point(
 						((imgWidth - shell.getClientArea().width) / 2 + 10 + 50),
 						((imgHeight - shell.getClientArea().height) / 2 + 10)));
 
 				nextPhaseButton.pack();
 				shell.update();
 
 				nextPhaseButton.addMouseListener(new MouseListener() {
 
 					@Override
 					public void mouseUp(MouseEvent e) {
 						game.endMovementPhase();
 						nextPhaseButton.dispose();
 					}
 
 					@Override
 					public void mouseDown(MouseEvent e) {
 						// TODO Auto-generated method stub
 
 					}
 
 					@Override
 					public void mouseDoubleClick(MouseEvent e) {
 						// TODO Auto-generated method stub
 
 					}
 				});
 			}
 		}
 		if (phase.equals(Phase.MOVEMENT2)) {
 			if (currentPlayer.equals(guiPlayer)) {
 
 				List<Territory> movingTerritories = game.getSimilarNeighborsOf(sourceTerritory);
 
 				for (Button button : buttonArray) {
 					button.setEnabled(false);
 				}
 
 				for (Button button : buttonArray) {
 					Territory territory = game.getTerritories().get(button.getData("name"));
 
 					for(int i = 0 ; i  < movingTerritories.size(); i++){						
 						if(territory.getName().equals(movingTerritories.get(i).getName())){
 							button.setEnabled(true);
 						}
 					}
 				}
 			}
 		}
 		if (phase.equals(Phase.MOVEMENT3)) {
 			for (Button button : buttonArray) {
 				button.setEnabled(false);
 			}
 		}
 	}
 }
