 package com.github.thehilikus.jrobocom.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyEvent;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.*;
 import javax.swing.border.BevelBorder;
 import javax.swing.border.CompoundBorder;
 import javax.swing.border.EmptyBorder;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.table.DefaultTableModel;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.github.thehilikus.jrobocom.GameSettings;
 import com.github.thehilikus.jrobocom.Player;
 import com.github.thehilikus.jrobocom.Session;
 import com.github.thehilikus.jrobocom.events.GameListener;
 import com.github.thehilikus.jrobocom.events.LeaderChangedEvent;
 import com.github.thehilikus.jrobocom.events.PlayerEliminatedEvent;
 import com.github.thehilikus.jrobocom.events.ResultEvent;
 import com.github.thehilikus.jrobocom.events.ResultEvent.Result;
 import com.github.thehilikus.jrobocom.events.RobotAddedEvent;
 import com.github.thehilikus.jrobocom.events.RobotChangedEvent;
 import com.github.thehilikus.jrobocom.events.RobotMovedEvent;
 import com.github.thehilikus.jrobocom.events.RobotRemovedEvent;
 import com.github.thehilikus.jrobocom.gui.drawables.DrawableRobot;
 import com.github.thehilikus.jrobocom.gui.panels.BoardPanel;
 import com.github.thehilikus.jrobocom.gui.panels.NewGameDialog;
 import com.github.thehilikus.jrobocom.robot.Robot;
 import com.github.thehilikus.jrobocom.timing.MasterClock;
 
 /**
  * The composed UI
  * 
  * @author hilikus
  */
 public class GUI implements ColourInfoProvider {
 
     private JFrame frame;
 
     private Controller controller = new Controller();
 
     private JSlider speedSlider;
 
     private BoardPanel board;
 
     private Map<Integer, Color> teamsColours;
 
     private Session session;
 
     private static final Logger log = LoggerFactory.getLogger(GUI.class);
 
     private UIAction startAction;
 
     private UIAction stepAction;
 
     private UIAction reloadAction;
     private JTable table;
 
     private DefaultTableModel tableModel;
 
     private TeamsTableRenderer teamsTableRenderer;
 
     /**
      * Event handler in charge of updating the UI
      * 
      */
     public class Controller implements GameListener, ChangeListener {
 	private Map<Robot, Point> robots;
 
 	@Override
 	public void update(final RobotAddedEvent evt) {
 	    final Robot source = evt.getSource();
 	    SwingUtilities.invokeLater(new Runnable() {
 
 		@Override
 		public void run() {
 		    board.addItem(evt.getCoordinates(), new DrawableRobot(source));
 		    changeCount(source.getOwner(), 1);
 		}
 
 	    });
 
 	    if (robots == null) { // lazy init
 		robots = new HashMap<>();
 	    }
 	    robots.put(source, evt.getCoordinates());
 	}
 
 	@Override
 	public void update(final RobotRemovedEvent evt) {
 	    SwingUtilities.invokeLater(new Runnable() {
 
 		@Override
 		public void run() {
 		    board.removeItem(evt.getCoordinates());
 		    changeCount(evt.getSource().getOwner(), -1);
 		}
 	    });
 
 	    if (robots == null) { // lazy init
 		robots = new HashMap<>();
 	    }
 	    robots.remove(evt.getSource());
 	}
 
 	@Override
 	public void update(final RobotChangedEvent evt) {
 	    SwingUtilities.invokeLater(new Runnable() {
 
 		@Override
 		public void run() {
		    board.refresh(robots.get(evt.getSource()));
 		}
 	    });
 
 	}
 
 	@Override
 	public void update(final RobotMovedEvent mov) {
 	    SwingUtilities.invokeLater(new Runnable() {
 
 		@Override
 		public void run() {
 		    board.moveItem(mov.getOldPosition(), mov.getNewPosition());
 		}
 	    });
 
 	    robots.put(mov.getSource(), mov.getNewPosition());
 	}
 
 	@Override
 	public void update(ResultEvent result) {
 	    displayResult(result);
 
 	}
 
 	@Override
 	public void stateChanged(ChangeEvent e) {
 	    JSlider source = (JSlider) e.getSource();
 	    if (!source.getValueIsAdjusting()) {
 		session.setClockPeriod(source.getValue());
 	    }
 
 	}
 
 	@Override
 	public void update(LeaderChangedEvent event) {
 	    table.repaint(50);
 
 	}
 
 	@Override
 	public void update(PlayerEliminatedEvent event) {
 	    // TODO Auto-generated method stub
 	    
 	}
 
     }
 
     private class UIAction extends AbstractAction {
 
 	private static final long serialVersionUID = 2865727921724644884L;
 
 	public UIAction(String actionName) {
 	    super(actionName);
 	    putValue(ACTION_COMMAND_KEY, actionName);
 	    putValue(SHORT_DESCRIPTION, getDescription(actionName));
 	    putValue(MNEMONIC_KEY, getMnemonic(actionName));
 
 	    if (ActionNames.NEW_GAME.equals(actionName)) {
 		setEnabled(true);
 	    } else {
 		setEnabled(false);
 	    }
 	}
 
 	private int getMnemonic(String actionName) {
 	    switch (actionName) {
 		case ActionNames.NEW_GAME:
 		    return KeyEvent.VK_N;
 		case ActionNames.RELOAD:
 		    return KeyEvent.VK_R;
 		case ActionNames.STEP:
 		    return KeyEvent.VK_T;
 		case ActionNames.START:
 		    return KeyEvent.VK_S;
 	    }
 	    return -1;
 	}
 
 	private String getDescription(String actionName) {
 	    switch (actionName) {
 		case ActionNames.NEW_GAME:
 		    return "Configures a new session";
 		case ActionNames.RELOAD:
 		    return "Starts a new session with the same robots";
 		case ActionNames.STEP:
 		    return "Executes a single clock of the Master clock";
 		case ActionNames.START:
 		    return "Starts or stops the Master clock";
 	    }
 	    return "n/a";
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 	    switch (e.getActionCommand()) {
 		case ActionNames.NEW_GAME:
 		    createNewSession();
 		    break;
 		case ActionNames.RELOAD:
 		    reloadSession();
 		    break;
 		case ActionNames.STEP:
 		    singleStep();
 		    break;
 		case ActionNames.START:
 		    start(!((JToggleButton) e.getSource()).isSelected());
 
 	    }
 
 	}
 
     }
 
     private class ActionNames {
 	public static final String RELOAD = "Reload";
 	public static final String NEW_GAME = "New Game";
 	public static final String STEP = "Step";
 	public static final String START = "Start";
     }
 
     /**
      * @param args ignored
      */
     public static void main(String[] args) {
 
 	SwingUtilities.invokeLater(new Runnable() {
 	    @Override
 	    public void run() {
 		GUI view = new GUI("JRoboCom");
 		assertEDT();
 		view.setVisible(true);
 	    }
 	});
 
     }
 
     private void setVisible(boolean visible) {
 	frame.setVisible(visible);
 
     }
 
     /**
      * Create the application.
      * 
      * @param title the name of the frame
      */
     public GUI(String title) {
 	initialize(title);
     }
 
     private void singleStep() {
 	session.step();
     }
 
     private void start(boolean pause) {
 	if (pause) {
 	    assert session.isRunning();
 	    session.stop();
 	} else {
 	    assert !session.isRunning();
 	    session.start();
 	}
     }
 
     /**
      * Changes the UI depending on whether the session is ready or not
      * 
      * @param isReady true if session was configured correctly; false otherwise
      */
     public void sessionReady(boolean isReady) {
 	if (isReady) {
 	    speedSlider.setValue(session.getClockPeriod());
 	}
 	startAction.setEnabled(isReady);
 	speedSlider.setEnabled(isReady);
 	stepAction.setEnabled(isReady);
 	reloadAction.setEnabled(isReady);
 
     }
 
     /**
      * Initialize the contents of the frame.
      * 
      * @param title
      */
     private void initialize(String title) {
 	frame = new JFrame(title);
 	frame.setBounds(100, 100, 814, 669);
 	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	frame.getContentPane().setLayout(new BorderLayout(0, 0));
 
 	JToolBar toolBar = new JToolBar();
 	frame.getContentPane().add(toolBar, BorderLayout.NORTH);
 
 	UIAction newAction = new UIAction(ActionNames.NEW_GAME);
 	JButton btnNewGame = new JButton(newAction);
 	btnNewGame.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ctrl N"),
 		ActionNames.NEW_GAME);
 	btnNewGame.getActionMap().put(ActionNames.NEW_GAME, newAction);
 
 	toolBar.add(btnNewGame);
 
 	reloadAction = new UIAction(ActionNames.RELOAD);
 	JButton btnReload = new JButton(reloadAction);
 	btnReload.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F5"), ActionNames.RELOAD);
 	btnReload.getActionMap().put(ActionNames.RELOAD, reloadAction);
 
 	toolBar.add(btnReload);
 	toolBar.addSeparator();
 
 	startAction = new UIAction(ActionNames.START);
 	JToggleButton tglbtnStart = new JToggleButton(startAction);
 	toolBar.add(tglbtnStart);
 
 	speedSlider = new JSlider();
 	speedSlider.setInverted(true);
 	speedSlider.setMinimum(MasterClock.MIN_PERIOD);
 	speedSlider.setMaximum(700);
 	speedSlider.setEnabled(false);
 	speedSlider.setMaximumSize(new Dimension(100, 16));
 	speedSlider.addChangeListener(controller);
 	toolBar.add(speedSlider);
 
 	stepAction = new UIAction(ActionNames.STEP);
 	JButton btnStep = new JButton(stepAction);
 	btnStep.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F10"), ActionNames.STEP);
 	btnStep.getActionMap().put(ActionNames.STEP, stepAction);
 	toolBar.add(btnStep);
 
 	JPanel mainPanel = new JPanel();
 	frame.getContentPane().add(mainPanel, BorderLayout.CENTER);
 	mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));
 
 	board = new BoardPanel(this);
 	mainPanel.add(board);
 	board.setBorder(new CompoundBorder(new EmptyBorder(10, 10, 10, 0), new BevelBorder(BevelBorder.RAISED, null,
 		null, null, null)));
 
 	JPanel rightPanel = new JPanel();
 	rightPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
 	rightPanel.setPreferredSize(new Dimension(196, 32767));
 	rightPanel.setMinimumSize(new Dimension(300, 300));
 	rightPanel.setMaximumSize(new Dimension(500, 32767));
 	mainPanel.add(rightPanel);
 	rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.PAGE_AXIS));
 
 	JLabel lblTeams = new JLabel("Teams");
 	lblTeams.setAlignmentX(Component.CENTER_ALIGNMENT);
 	rightPanel.add(lblTeams);
 
 	table = new JTable();
 	teamsTableRenderer = new TeamsTableRenderer(this);
 	teamsTableRenderer.setDisableSelection(true);
 	table.setDefaultRenderer(Object.class, teamsTableRenderer);
 	tableModel = new DefaultTableModel(0, 2);
 	table.setModel(tableModel);
 	table.getColumnModel().getColumn(1).setMaxWidth(30);
 	table.setShowVerticalLines(false);
 	rightPanel.add(table);
     }
 
     private static void assertEDT() {
 	assert SwingUtilities.isEventDispatchThread() : "Not in EDT";
     }
 
     /**
      * Changes the title of the application
      * 
      * @param title new window title
      */
     public void setTitle(String title) {
 	assertEDT();
 	frame.setTitle(title);
     }
 
     /**
      * @return the event manager
      */
     public GameListener getController() {
 	return controller;
     }
 
     @Override
     public Color getTeamColour(int teamId) {
 	return teamsColours.get(teamId);
     }
 
     private void createNewSession() {
 	NewGameDialog newGameDialog = new NewGameDialog(frame);
 	newGameDialog.setVisible(true);
 	if (newGameDialog.getResult() == JOptionPane.OK_OPTION) {
 	    // user pressed ok
 	    assert newGameDialog.getSelectedTeams() != null;
 	    board.clear();
 	    List<Player> players = newGameDialog.getSelectedTeams();
 	    teamsColours = newGameDialog.getColourMappings();
 	    session = new Session(players, controller);
 
 	    sessionReady(newGameDialog.getSelectedTeams().size() > 0);
 	    // playersModel.clear();
 	    tableModel.setRowCount(0);
 	    for (Player player : players) {
 		// playersModel.addElement(player);
 		tableModel.addRow(new Object[] { player, 0 });
 	    }
 
 	}
     }
 
     private void reloadSession() {
 	assert session != null;
 	assert session.getPlayers() != null && session.getPlayers().size() > 0 : "Players undefined, UI should not allow this action yet";
 	SwingUtilities.invokeLater(new Runnable() {
 
 	    @Override
 	    public void run() {
 		board.clear();
 		for (Player player : session.getPlayers()) {
 		    changeCount(player, -GameSettings.getInstance().BOARD_SIZE*2);
 		}
 	    }
 	});
 
 	session.clean();
 	session = new Session(session.getPlayers(), controller);
     }
 
     private void displayResult(ResultEvent result) {
 	String title;
 	String msg;
 	Icon icon = null;
 	if (result.getResult() == Result.DRAW) {
 	    title = "Draw";
 	    msg = "There were no winners in this run";
 	    icon = loadIcon("/images/cup.jpg");
 	} else {
 	    title = "And the Winner is...";
 	    msg = "The winner is " + result.getWinner().getAuthor() + " with " + result.getWinner().getTeamName();
 	    icon = loadIcon("/images/cup.jpg");
 	}
 
 	JOptionPane.showMessageDialog(frame, msg, title, JOptionPane.INFORMATION_MESSAGE, icon);
     }
 
     private Icon loadIcon(String path) {
 	URL imgURL = getClass().getResource(path);
 	if (imgURL != null) {
 	    return new ImageIcon(imgURL);
 	} else {
 	    log.warn("[loadIcon] Could not load icon from {}", path);
 	    return null;
 	}
     }
 
     private void changeCount(Player team, int delta) {
 	for (int row = 0; row < tableModel.getRowCount(); row++) {
 	    if (tableModel.getValueAt(row, 0) == team) {
 		Integer currVal = (Integer) tableModel.getValueAt(row, 1);
 		tableModel.setValueAt(Math.max(currVal + delta, 0), row, 1);
 		break;
 	    }
 	}
     }
 
 }
