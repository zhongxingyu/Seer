 package view.window;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyListener;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 
 import view.board.AnimationPanel;
 import controller.DesignModeViewModel;
 import controller.GizmoballViewModel;
 import controller.GizmoballViewModel.UpdateReason;
 import controller.MagicKeyListener;
 import exceptions.BadFileException;
 
 /**
  * Main application window.
  */
 @SuppressWarnings("serial")
 public class ApplicationWindow extends JFrame implements Observer {
 	public static final int L = 20;
 
 	private GizmoballViewModel viewmodel;
 	private DesignModeViewModel designmodeViewmodel;
 
 	private JMenuItem newMenuItem, openMenuItem, saveMenuItem;
 	private AnimationPanel boardView;
 	private ToolbarButtonArea toolbar;
 	private JPanel contentPane;
 	private JLabel statusBar;
 
 	/**
 	 * Creates a new window for gizmoball. 
 	 */
 	public ApplicationWindow(GizmoballViewModel viewModel, DesignModeViewModel designModeViewModel) {
 		super("Gizmoball");
 		super.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		this.viewmodel = viewModel;
 		this.viewmodel.addObserver(this);
 
 		this.designmodeViewmodel = designModeViewModel;
 		this.designmodeViewmodel.addObserver(this);
 
 		initialiseComponents();
 		initialiseActionListeners();
 	}
 
 	/**
 	 * Initialise all the window components. 
 	 */
 	private void initialiseComponents() {
 		toolbar = new ToolbarButtonArea(viewmodel, designmodeViewmodel);
 		boardView = new AnimationPanel(viewmodel, designmodeViewmodel);
 
 		JMenuBar menubar = new JMenuBar();
 		super.setJMenuBar(menubar);
 
 		JMenu fileMenu = new JMenu("File");
 		menubar.add(fileMenu);
 
 		newMenuItem = new JMenuItem("New");
 		fileMenu.add(newMenuItem);
 
 		openMenuItem = new JMenuItem("Open");
 		fileMenu.add(openMenuItem);
 
 		saveMenuItem = new JMenuItem("Save");
 		fileMenu.add(saveMenuItem);
 
		statusBar = new JLabel();
 		statusBar.setBorder(new EmptyBorder(5, 5, 5, 5));
 
 		contentPane = new JPanel();
 		contentPane.setLayout(new BorderLayout());
 		contentPane.setPreferredSize(new Dimension(500, 410));
 		contentPane.add(boardView, BorderLayout.CENTER);
 		contentPane.add(toolbar, BorderLayout.EAST);
 		contentPane.add(statusBar, BorderLayout.SOUTH);
 
 		super.setContentPane(contentPane);
 		super.setMinimumSize(new Dimension(650, 580));
 
 		super.pack();
 		super.requestFocus();
 	}
 
 	/**
 	 * Sets action listeners. 
 	 */
 	private void initialiseActionListeners() {
 		final JFrame parent = this;
 
 		newMenuItem.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				viewmodel.newGame();
 			}
 		});
 
 		openMenuItem.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				JFileChooser chooser = new JFileChooser(System
 						.getProperty("user.dir"));
 
 				if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
 					File file = chooser.getSelectedFile();
 
 					try {
 						viewmodel.loadGame(file.getAbsolutePath());
 					} catch (FileNotFoundException ex) {
 						JOptionPane.showMessageDialog(parent,
 								"The file cannot be found.", "Load error",
 								JOptionPane.ERROR_MESSAGE);
 					} catch (IOException ex) {
 						JOptionPane.showMessageDialog(parent,
 								"Error reading file: " + ex.getMessage(),
 								"Load error", JOptionPane.ERROR_MESSAGE);
 					} catch (BadFileException ex) {
 						JOptionPane.showMessageDialog(
 								parent,
 								"The file format is incorrect: "
 										+ ex.getMessage(), "Load error",
 								JOptionPane.ERROR_MESSAGE);
 					}
 				}
 			}
 		});
 
 		saveMenuItem.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				JFileChooser chooser = new JFileChooser(System
 						.getProperty("user.dir"));
 
 				if (chooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
 					File file = chooser.getSelectedFile();
 
 					try {
 						viewmodel.saveGame(file.getAbsolutePath());
 					} catch (IOException ex) {
 						JOptionPane.showMessageDialog(parent,
 								"Error writing file: " + ex.getMessage(),
 								"Save error", JOptionPane.ERROR_MESSAGE);
 					}
 				}
 			}
 		});
 	}
 
 	@Override
 	public void update(Observable source, Object arg) {
 		UpdateReason reason = (UpdateReason) arg;
 
 		switch (reason) {
 		case RunStateChanged:
 			toolbar.setRunMode(viewmodel.getIsRunning());
 
 			if (viewmodel.getIsRunning())
 				contentPane.remove(statusBar);
 			else
 				contentPane.add(statusBar, BorderLayout.SOUTH);
 
 			break;
 
 		case StatusChanged:
 			statusBar.setText(designmodeViewmodel.getStatusMessage());
 			break;
 		}
 	}
 }
