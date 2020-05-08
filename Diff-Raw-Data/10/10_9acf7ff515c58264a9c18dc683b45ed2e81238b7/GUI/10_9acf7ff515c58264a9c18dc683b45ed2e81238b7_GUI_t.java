 package edu.gatech.statusquo.spacetrader.view;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.swing.JOptionPane;
 
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.List;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Spinner;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.custom.TableCursor;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.KeyListener;
 import org.eclipse.swt.events.MouseAdapter;
 import org.eclipse.swt.events.MouseEvent;
 
 import edu.gatech.statusquo.spacetrader.presenter.*;
 
 public class GUI {
 
 	protected Shell shell;
 	protected Display display;
 
 	/**
 	 * Launch the application.
 	 * 
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		try {
 			GUI UI = new GUI();
 			UI.openLoader();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Open the window.
 	 */
 	private void openLoader() {
 		display = Display.getDefault();
 		createContents();
 		shell.open();
 		shell.layout();
 		while (!shell.isDisposed()) {
 			if (!display.readAndDispatch()) {
 				display.sleep();
 			}
 		}
 	}
 
 	/**
 	 * Create contents of the window.
 	 */
 	private void createContents() {
 		shell = new Shell(display, SWT.TITLE | SWT.CLOSE);
 		shell.setSize(381, 253);
 		shell.setText("Space Trader - CS 2340");
 		shell.setLayout(null);
 
 		final Button btnStartGame_1 = new Button(shell, SWT.NONE);
 		final Button btnLoadGame = new Button(shell, SWT.NONE);
 		final Label lblWelcomeToSpace = new Label(shell, SWT.NONE);
 
 		btnStartGame_1.addMouseListener(new MouseAdapter() {
 			@Override
 			/*
 			 * (non-Javadoc)
 			 * 
 			 * @see
 			 * org.eclipse.swt.events.MouseAdapter#mouseDown(org.eclipse.swt
 			 * .events.MouseEvent) So this is where the mousedown event happens.
 			 * Notice I set the visibility of all the parts of the first window
 			 * to false, and then I ran the method that would create the
 			 * CharacterCreationScreen in the same shell. -Shawn
 			 */
 			public void mouseDown(MouseEvent e) {
 				btnStartGame_1.setVisible(false);
 				btnLoadGame.setVisible(false);
 				lblWelcomeToSpace.setVisible(false);
 				createCharacterCreationScreen();
 			}
 		});
 		btnStartGame_1.setBounds(10, 180, 75, 25);
 		btnStartGame_1.setText("Start Game");
 
 		btnLoadGame.setBounds(280, 180, 75, 25);
 		btnLoadGame.setText("Load Game");
 
 		lblWelcomeToSpace.setBounds(108, 35, 142, 15);
 		lblWelcomeToSpace.setText("Welcome to Space Trader");
 
 	}
 
 	/*
 	 * So I copied and pasted this from another WindowBuilderPro Window to make
 	 * it one class like Spencer wanted - Shawn
 	 */
 	private int pointsAvailable = 15;
 	private int traderUsed = 0;
 	private int engineerUsed = 0;
 	private int pilotUsed = 0;
 	private int fighterUsed = 0;
 	protected boolean status = true;
 	private Text text;
 
 	Spinner spinner;
 	Spinner spinner_1;
 	Spinner spinner_2;
 	Spinner spinner_3;
 
 	Label label;
 
 	String name;
 
 	/**
 	 * Create contents of the window.
 	 */
 	private void createCharacterCreationScreen() {
 
 		/*
 		 * Declare some stuff. - Shawn
 		 */
 		spinner = new Spinner(shell, SWT.BORDER);
 		spinner_1 = new Spinner(shell, SWT.BORDER);
 		spinner_2 = new Spinner(shell, SWT.BORDER);
 		spinner_3 = new Spinner(shell, SWT.BORDER);
 
 		label = new Label(shell, SWT.NONE);
 		label.setText(Integer.toString(pointsAvailable));
 		/*
 		 * Settings bounds for all the spinners. - Shawn
 		 */
 		spinner.setBounds(203, 109, 47, 22);
 		spinner_1.setBounds(203, 137, 47, 22);
 		spinner_2.setBounds(203, 192, 47, 22);
 		spinner_3.setBounds(203, 164, 47, 22);
 
 		/*
 		 * Set maximums for all the spinners.
 		 */
 		spinner.setMaximum(15);
 		spinner_1.setMaximum(15);
 		spinner_2.setMaximum(15);
 		spinner_3.setMaximum(15);
 
 		/*
 		 * Set the spinners to be un-editable - Shawn
 		 */
 
 		shell.setSize(400, 500);
 
 		final Label lblNewLabel = new Label(shell, SWT.NONE);
 		lblNewLabel.setBounds(143, 10, 104, 15);
 		lblNewLabel.setText("Create New Player");
 
 		text = new Text(shell, SWT.BORDER);
 		text.setBounds(203, 43, 104, 21);
 
 		final Label lblEnterAPlayer = new Label(shell, SWT.NONE);
 		lblEnterAPlayer.setBounds(70, 46, 116, 15);
 		lblEnterAPlayer.setText("Enter a Player Name");
 
 		/*
 		 * These are all the listeners - Shawn
 		 */
 		spinner.addMouseListener(new MouseAdapter() {
 			/*
 			 * (non-Javadoc)
 			 * 
 			 * @see
 			 * org.eclipse.swt.events.MouseAdapter#mouseDown(org.eclipse.swt
 			 * .events.MouseEvent) So incase you are wondering the reason this
 			 * is here is so that we can see the amount of points in the spinner
 			 * and then we can check to see with canAddMorePoints() if there can
 			 * be more points added. Afterwards it will set the spinners to
 			 * enabled or disabled depending on whether or not the status is
 			 * true or false. - Shawn
 			 * 
 			 * Edit: I changed to mouseUp due to a bug that would allow a player
 			 * to be created with over 15 points.
 			 */
 			@Override
 			public void mouseUp(MouseEvent e) {
 				traderUsed = Integer.parseInt(spinner.getText());
 				status = canAddMorePoints();
 				spinner.setEnabled(status);
 				spinner_1.setEnabled(status);
 				spinner_2.setEnabled(status);
 				spinner_3.setEnabled(status);
 				checkValidityOfPoints();
 			}
 		});
 
 		spinner.addKeyListener(new KeyListener() {
 
 			@Override
 			public void keyPressed(KeyEvent e) {
 				// TODO Auto-generated method stub
 
 			}
 
 			@Override
 			public void keyReleased(KeyEvent e) {
 				// TODO Auto-generated method stub
 				if (spinner.getText() == "") {
 
 				} else {
 					traderUsed = Integer.parseInt(spinner.getText());
 					status = canAddMorePoints();
 					spinner.setEnabled(status);
 					spinner_1.setEnabled(status);
 					spinner_2.setEnabled(status);
 					spinner_3.setEnabled(status);
 					checkValidityOfPoints();
 				}
 			}
 
 		});
 
 		spinner_1.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseUp(MouseEvent e) {
 				engineerUsed = Integer.parseInt(spinner_1.getText());
 				status = canAddMorePoints();
 				spinner.setEnabled(status);
 				spinner_1.setEnabled(status);
 				spinner_2.setEnabled(status);
 				spinner_3.setEnabled(status);
 				checkValidityOfPoints();
 			}
 		});
 
 		spinner_1.addKeyListener(new KeyListener() {
 
 			@Override
 			public void keyPressed(KeyEvent e) {
 				// TODO Auto-generated method stub
 
 			}
 
 			@Override
 			public void keyReleased(KeyEvent e) {
 				// TODO Auto-generated method stub
 				if (spinner_1.getText() == "") {
 
 				} else {
 					engineerUsed = Integer.parseInt(spinner_1.getText());
 					status = canAddMorePoints();
 					spinner.setEnabled(status);
 					spinner_1.setEnabled(status);
 					spinner_2.setEnabled(status);
 					spinner_3.setEnabled(status);
 					checkValidityOfPoints();
 				}
 			}
 
 		});
 
 		spinner_2.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseUp(MouseEvent e) {
 				pilotUsed = Integer.parseInt(spinner_2.getText());
 				status = canAddMorePoints();
 				spinner.setEnabled(status);
 				spinner_1.setEnabled(status);
 				spinner_2.setEnabled(status);
 				spinner_3.setEnabled(status);
 				checkValidityOfPoints();
 			}
 		});
 
 		spinner_2.addKeyListener(new KeyListener() {
 
 			@Override
 			public void keyPressed(KeyEvent e) {
 				// TODO Auto-generated method stub
 
 			}
 
 			@Override
 			public void keyReleased(KeyEvent e) {
 				// TODO Auto-generated method stub
 				if (spinner_2.getText() == "") {
 
 				} else {
 					pilotUsed = Integer.parseInt(spinner_2.getText());
 					status = canAddMorePoints();
 					spinner.setEnabled(status);
 					spinner_1.setEnabled(status);
 					spinner_2.setEnabled(status);
 					spinner_3.setEnabled(status);
 					checkValidityOfPoints();
 				}
 			}
 		});
 
 		spinner_3.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseUp(MouseEvent e) {
 				fighterUsed = Integer.parseInt(spinner_3.getText());
 				status = canAddMorePoints();
 				spinner.setEnabled(status);
 				spinner_1.setEnabled(status);
 				spinner_2.setEnabled(status);
 				spinner_3.setEnabled(status);
 				label.setText(Integer.toString(calculatePoints()));
 			}
 		});
 
 		spinner_3.addKeyListener(new KeyListener() {
 
 			@Override
 			public void keyPressed(KeyEvent e) {
 				// TODO Auto-generated method stub
 
 			}
 
 			@Override
 			public void keyReleased(KeyEvent e) {
 				// TODO Auto-generated method stub
 				if (spinner_3.getText() == "") {
 
 				} else {
 					fighterUsed = Integer.parseInt(spinner_3.getText());
 					status = canAddMorePoints();
 					spinner.setEnabled(status);
 					spinner_1.setEnabled(status);
 					spinner_2.setEnabled(status);
 					spinner_3.setEnabled(status);
 					checkValidityOfPoints();
 				}
 			}
 		});
 
 		final Label lblAllocateSkillPoints = new Label(shell, SWT.NONE);
 		lblAllocateSkillPoints.setBounds(143, 88, 104, 15);
 		lblAllocateSkillPoints.setText("Allocate Skill Points");
 
 		final Label lblFighter = new Label(shell, SWT.NONE);
 		lblFighter.setBounds(131, 195, 43, 15);
 		lblFighter.setText("Fighter");
 
 		final Label lblTrader = new Label(shell, SWT.NONE);
 		lblTrader.setBounds(131, 116, 55, 15);
 		lblTrader.setText("Trader");
 
 		final Label lblEngineer = new Label(shell, SWT.NONE);
 		lblEngineer.setBounds(131, 146, 55, 15);
 		lblEngineer.setText("Engineer");
 
 		final Label lblPilot = new Label(shell, SWT.NONE);
 		lblPilot.setBounds(131, 169, 24, 15);
 		lblPilot.setText("Pilot");
 
 		final Label lblSelectADifficulty = new Label(shell, SWT.NONE);
 		lblSelectADifficulty.setBounds(143, 280, 104, 15);
 		lblSelectADifficulty.setText("Select a Difficulty");
 
 		final Button btnBeginner = new Button(shell, SWT.RADIO);
 		btnBeginner.setBounds(143, 301, 90, 16);
 		btnBeginner.setText("Beginner");
 		btnBeginner.setSelection(true);
 
 		final Button btnEasy = new Button(shell, SWT.RADIO);
 		btnEasy.setBounds(143, 323, 90, 16);
 		btnEasy.setText("Easy");
 
 		final Button btnNormal = new Button(shell, SWT.RADIO);
 		btnNormal.setBounds(143, 345, 90, 16);
 		btnNormal.setText("Normal");
 
 		final Button btnHard = new Button(shell, SWT.RADIO);
 		btnHard.setBounds(143, 367, 90, 16);
 		btnHard.setText("Hard");
 
 		final Button btnImpossible = new Button(shell, SWT.RADIO);
 		btnImpossible.setBounds(143, 389, 90, 16);
 		btnImpossible.setText("Impossible");
 
 		final Button btnStartGame = new Button(shell, SWT.NONE);
 		btnStartGame.setBounds(10, 427, 75, 25);
 		btnStartGame.setText("Start Game");
 
 		final Button btnResetSkillPoints = new Button(shell, SWT.NONE);
 		btnResetSkillPoints.setBounds(270, 427, 104, 25);
 		btnResetSkillPoints.setText("Reset Skill Points");
 
 		btnResetSkillPoints.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseDown(MouseEvent e) {
 				pointsAvailable = 15;
 				traderUsed = 0;
 				engineerUsed = 0;
 				pilotUsed = 0;
 				fighterUsed = 0;
 				status = true;
 				spinner.setEnabled(status);
 				spinner_1.setEnabled(status);
 				spinner_2.setEnabled(status);
 				spinner_3.setEnabled(status);
 				spinner.setValues(0, 0, 15, 0, 1, 1);
 				spinner_1.setValues(0, 0, 15, 0, 1, 1);
 				spinner_2.setValues(0, 0, 15, 0, 1, 1);
 				spinner_3.setValues(0, 0, 15, 0, 1, 1);
 				label.setText(Integer.toString(calculatePoints()));
 			}
 		});
 
 		final Label lblSkillPointsLeft = new Label(shell, SWT.NONE);
 		lblSkillPointsLeft.setBounds(96, 230, 90, 15);
 		lblSkillPointsLeft.setText("Points Available:");
 
 		btnStartGame.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseDown(MouseEvent e) {
 				status = canAddMorePoints();
 				name = text.getText();
 				if (status) {
 					JOptionPane.showMessageDialog(null,
 							"You have not assigned all of your skill points",
 							"Unused Skill Points", 2);
 				} else if (name == "") {
 					JOptionPane.showMessageDialog(null,
 							"Please enter a player name.");
				} 
				else if(calculatePoints() < 0)
				{
				    checkValidityOfPoints();
				}
				else {
 
 					// ***********TRY CATCH**********
 					Driver driver = null;
 					try {
 						driver = new Driver();
 					} catch (IOException e2) {
 						// TODO Auto-generated catch block
 						e2.printStackTrace();
 					}
 					driver.createPlayer(name, pilotUsed, fighterUsed,
 							traderUsed, engineerUsed);
 					try {
 						driver.generateUniverse();
 					} catch (IOException e1) {
 						// TODO Auto-generated catch block
 						e1.printStackTrace();
 					}
 					// ********************************
 
 					/*
 					 * old code presenter.Driver driver = new
 					 * presenter.Driver(); driver.createPlayer(name, pilotUsed,
 					 * fighterUsed, traderUsed, engineerUsed);
 					 * driver.generateUniverse();
 					 */
 
 					text.setVisible(false);
 					spinner.setVisible(false);
 					spinner_1.setVisible(false);
 					spinner_2.setVisible(false);
 					spinner_3.setVisible(false);
 					label.setVisible(false);
 					lblEnterAPlayer.setVisible(false);
 					lblNewLabel.setVisible(false);
 					lblAllocateSkillPoints.setVisible(false);
 					lblFighter.setVisible(false);
 					lblTrader.setVisible(false);
 					lblEngineer.setVisible(false);
 					lblPilot.setVisible(false);
 					lblSelectADifficulty.setVisible(false);
 					btnBeginner.setVisible(false);
 					btnEasy.setVisible(false);
 					btnNormal.setVisible(false);
 					btnHard.setVisible(false);
 					btnImpossible.setVisible(false);
 					btnStartGame.setVisible(false);
 					btnResetSkillPoints.setVisible(false);
 					lblSkillPointsLeft.setVisible(false);
 					createMainApplicationWindow();
 				}
 			}
 		});
 
 		label.setBounds(205, 230, 55, 15);
 	}
 
 	/**
 	 * This method will tell the program whether or not all the skill points
 	 * have been used.
 	 * 
 	 * @return boolean which states whether all the points have been used or
 	 *         not.
 	 */
 	private boolean canAddMorePoints() {
 		// TODO Auto-generated method stub
 		int totalPoints = traderUsed + engineerUsed + pilotUsed + fighterUsed;
 		if (totalPoints <= 14) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * This method just takes the points from trader, explorer, pilot, and adds
 	 * them together.
 	 * 
 	 * @return int that is the number of points left to add to the skillset.
 	 */
 
 	private int calculatePoints() {
		int totalPoints = Integer.parseInt(spinner.getText()) + Integer.parseInt(spinner_1.getText()) + Integer.parseInt(spinner_2.getText()) + Integer.parseInt(spinner_3.getText());
 		return pointsAvailable - totalPoints;
 	}
 
 	/**
 	 * This will check the validity of the points and reset them if an
 	 * inconsistency is detected for example, if the points are over 15.
 	 */
 	private void checkValidityOfPoints() {
 		if (calculatePoints() < 0) {
 			JOptionPane.showMessageDialog(null,
 					"Invalid entries detected. Resetting all skill points.");
 			pointsAvailable = 15;
 			traderUsed = 0;
 			engineerUsed = 0;
 			pilotUsed = 0;
 			fighterUsed = 0;
 			status = true;
 			spinner.setEnabled(status);
 			spinner_1.setEnabled(status);
 			spinner_2.setEnabled(status);
 			spinner_3.setEnabled(status);
 			spinner.setValues(0, 0, 15, 0, 1, 1);
 			spinner_1.setValues(0, 0, 15, 0, 1, 1);
 			spinner_2.setValues(0, 0, 15, 0, 1, 1);
 			spinner_3.setValues(0, 0, 15, 0, 1, 1);
 			label.setText(Integer.toString(calculatePoints()));
 		} else {
 			label.setText(Integer.toString(calculatePoints()));
 		}
 	}
 
 	/*
 	 * Again, this is a big copy and paste. Sorry if it gets a little rough
 	 * here.
 	 */
 
 	private Table table;
 	private Table table_1;
 	private Table table_2;
 	private Table table_3;
 	private TableColumn tblclmnNewColumn;
 	private List list_2;
 	private TableColumn tblclmnAttribute;
 	private TableColumn tblclmnQuantity;
 	private TableColumn tblclmnCurrency;
 	private TableColumn tblclmnWeight;
 	private TableCursor tableCursor;
 	private TableCursor tableCursor_1;
 	private TableCursor tableCursor_2;
 	private TableCursor tableCursor_3;
 	private TableColumn tblclmnCargo;
 	private TableColumn tblclmnWeight_1;
 	private Label lblNotifications;
 	private Label lblLocalPlanets;
 	private Driver driver;
 	private TableItem tableItem;
 	private Table table_4;
 	private TableItem tableItem_1;
 	private TableItem tableItem_2;
 
 	/**
 	 * This method will create the main Application window.
 	 */
 	private void createMainApplicationWindow() {
 		shell.setSize(1024, 768);
 		shell.setText("Space Trader");
 		shell.setLayout(null);
 
 		Label lblShipStatistics = new Label(shell, SWT.NONE);
 		lblShipStatistics.setAlignment(SWT.CENTER);
 		lblShipStatistics.setBounds(53, 0, 77, 15);
 		lblShipStatistics.setText("Ship Statistics");
 
 		table = new Table(shell, SWT.BORDER | SWT.FULL_SELECTION);
 		table.setBounds(0, 21, 190, 198);
 		table.setHeaderVisible(true);
 		table.setLinesVisible(true);
 
 		tblclmnAttribute = new TableColumn(table, SWT.CENTER);
 		tblclmnAttribute.setWidth(89);
 		tblclmnAttribute.setText("Attribute");
 
 		tblclmnQuantity = new TableColumn(table, SWT.CENTER);
 		tblclmnQuantity.setWidth(97);
 		tblclmnQuantity.setText("Quantity");
 
 		tableCursor = new TableCursor(table, SWT.NONE);
 
 		Label lblTeamStatistics = new Label(shell, SWT.NONE);
 		lblTeamStatistics.setBounds(53, 231, 94, 15);
 		lblTeamStatistics.setText("Team Statistics");
 
 		table_4 = new Table(shell, SWT.BORDER | SWT.FULL_SELECTION);
 		table_4.setBounds(0, 252, 190, 165);
 		table_4.setHeaderVisible(true);
 		table_4.setLinesVisible(true);
 
 		TableColumn tblclmnAttribute_1 = new TableColumn(table_4, SWT.NONE);
 		tblclmnAttribute_1.setWidth(85);
 		tblclmnAttribute_1.setText("Attribute");
 
 		TableColumn tblclmnPoints = new TableColumn(table_4, SWT.NONE);
 		tblclmnPoints.setWidth(100);
 		tblclmnPoints.setText("Points");
 
 		/*
 		 * Get the player skills, the skills are in the order located inside the
 		 * player class.
 		 */
 
 		ArrayList<Integer> skills = Driver.getSkills();
 
 		tableItem_1 = new TableItem(table_4, SWT.NONE);
 		String[] traderArray = { "Trader", skills.get(2).toString() };
 		tableItem_1.setText(traderArray);
 
 		tableItem_2 = new TableItem(table_4, SWT.NONE);
 		String[] engineerArray = { "Engineer", skills.get(3).toString() };
 		tableItem_2.setText(engineerArray);
 
 		TableItem tableItem_3 = new TableItem(table_4, SWT.NONE);
 		String[] pilotArray = { "Pilot", skills.get(1).toString() };
 		tableItem_3.setText(pilotArray);
 
 		TableItem tableItem_4 = new TableItem(table_4, SWT.NONE);
 		String[] fighterArray = { "Fighter", skills.get(0).toString() };
 		tableItem_4.setText(fighterArray);
 
 		Label lblNewLabel = new Label(shell, SWT.NONE);
 		lblNewLabel.setBounds(53, 423, 94, 15);
 		lblNewLabel.setText("Solar System List");
 
 		List list = new List(shell, SWT.BORDER | SWT.V_SCROLL);
 		list.setBounds(0, 444, 190, 286);
 
 		Label label = new Label(shell, SWT.NONE);
 		label.setAlignment(SWT.CENTER);
 		label.setText("Trade Goods");
 		label.setBounds(485, 0, 77, 15);
 
 		table_1 = new Table(shell, SWT.BORDER | SWT.FULL_SELECTION);
 		table_1.setBounds(196, 21, 653, 579);
 		table_1.setHeaderVisible(true);
 		table_1.setLinesVisible(true);
 
 		TableColumn tblclmnItems = new TableColumn(table_1, SWT.NONE);
 		tblclmnItems.setWidth(521);
 		tblclmnItems.setText("Item");
 
 		TableColumn tblclmnPrice = new TableColumn(table_1, SWT.NONE);
 		tblclmnPrice.setWidth(64);
 		tblclmnPrice.setText("Price");
 
 		tblclmnNewColumn = new TableColumn(table_1, SWT.NONE);
 		tblclmnNewColumn.setWidth(63);
 		tblclmnNewColumn.setText("Quantity");
 
 		tableCursor_1 = new TableCursor(table_1, SWT.NONE);
 
 		Label lblVitals = new Label(shell, SWT.NONE);
 		lblVitals.setAlignment(SWT.CENTER);
 		lblVitals.setBounds(903, 0, 55, 15);
 		lblVitals.setText("Vitals");
 
 		table_2 = new Table(shell, SWT.BORDER | SWT.FULL_SELECTION);
 		table_2.setBounds(855, 21, 143, 90);
 		table_2.setHeaderVisible(true);
 		table_2.setLinesVisible(true);
 
 		tblclmnCurrency = new TableColumn(table_2, SWT.CENTER);
 		tblclmnCurrency.setWidth(70);
 		tblclmnCurrency.setText("Currency");
 
 		tblclmnWeight = new TableColumn(table_2, SWT.CENTER);
 		tblclmnWeight.setWidth(69);
 		tblclmnWeight.setText("Fuel");
 
 		tableCursor_2 = new TableCursor(table_2, SWT.NONE);
 
 		tableItem = new TableItem(table_2, SWT.NONE);
 		tableItem.setText(Integer.toString(Driver.getCurrency()));
 
 		table_3 = new Table(shell, SWT.BORDER | SWT.FULL_SELECTION);
 		table_3.setBounds(855, 119, 143, 480);
 		table_3.setHeaderVisible(true);
 		table_3.setLinesVisible(true);
 
 		tableCursor_3 = new TableCursor(table_3, SWT.NONE);
 
 		tblclmnCargo = new TableColumn(table_3, SWT.CENTER);
 		tblclmnCargo.setWidth(68);
 		tblclmnCargo.setText("Cargo");
 
 		tblclmnWeight_1 = new TableColumn(table_3, SWT.CENTER);
 		tblclmnWeight_1.setWidth(71);
 		tblclmnWeight_1.setText("Weight");
 
 		lblNotifications = new Label(shell, SWT.NONE);
 		lblNotifications.setAlignment(SWT.CENTER);
 		lblNotifications.setBounds(485, 604, 77, 15);
 		lblNotifications.setText("Notifications");
 
 		List list_1 = new List(shell, SWT.BORDER);
 		list_1.setBounds(196, 628, 653, 96);
 		list_1.add("Welcome to Space Trader " + name);
 
 		lblLocalPlanets = new Label(shell, SWT.NONE);
 		lblLocalPlanets.setBounds(891, 604, 77, 15);
 		lblLocalPlanets.setText("Local Planets");
 
 		list_2 = new List(shell, SWT.BORDER);
 		list_2.setBounds(855, 625, 143, 99);
 	}
 }
