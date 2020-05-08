 package de.htwg.sudoku.aview.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
 import java.util.Set;
 
import javax.swing.JFileChooser;
 import javax.swing.JFrame;
import javax.swing.JMenu;
 import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
 
 import com.google.inject.Inject;
 
 import de.htwg.sudoku.controller.ISudokuController;
 import de.htwg.sudoku.controller.SizeChangedEvent;
 import de.htwg.sudoku.solverplugin.SolverPlugin;
 import de.htwg.util.observer.Event;
 import de.htwg.util.observer.IObserver;
 
 public class SudokuFrame extends JFrame implements IObserver {
 
 	private static final int DEFAULT_Y = 630;
 	private static final int DEFAULT_X = 528;
 	private Container pane;
 	private GridPanel gridPanel;
 	private HighlightButtonPanel digitPanel;
 	private StatusPanel statusPanel;
 	private ISudokuController controller;
 
 	private static final long serialVersionUID = 1L;
 
 	@Inject
 	public SudokuFrame(final ISudokuController controller, Set<SolverPlugin> plugins) {
 		this.controller = controller;
 		controller.addObserver(this);
 		
 		setTitle("HTWG Sudoku");
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setSize(DEFAULT_X, DEFAULT_Y);
 		pane = getContentPane();
 		pane.setLayout(new BorderLayout());
 
 		JMenuBar menuBar = new SudokuMenu(this, controller).buildMenuBar();
 
 
 		setJMenuBar(menuBar);
 		constructSudokuPane(controller);
 	}
 
 	public final void constructSudokuPane(ISudokuController controller) {
 		if (digitPanel != null) {
 			pane.remove(digitPanel);
 		}
 		digitPanel = new HighlightButtonPanel(controller);
 		pane.add(digitPanel, BorderLayout.NORTH);
 
 		if (gridPanel != null) {
 			pane.remove(gridPanel);
 		}
 		gridPanel = new GridPanel(controller);
 		pane.add(gridPanel, BorderLayout.CENTER);
 
 		if (statusPanel != null) {
 			pane.remove(statusPanel);
 		}
 		statusPanel = new StatusPanel();
 		pane.add(statusPanel, BorderLayout.SOUTH);
 		setVisible(true);
 		repaint();
 	}
 
 	public void update(Event e) {
 		statusPanel.setText(controller.getStatus());
 		if (e instanceof SizeChangedEvent) {
 			constructSudokuPane(controller);
 		}
 		repaint();
 	}
 
 
 
 }
