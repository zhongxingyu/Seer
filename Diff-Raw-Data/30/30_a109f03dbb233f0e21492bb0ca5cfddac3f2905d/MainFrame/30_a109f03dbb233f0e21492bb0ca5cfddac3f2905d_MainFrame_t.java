 package wombat.gui.frames;
 
 
 import javax.swing.*;
 import javax.swing.event.*;
 import javax.swing.text.BadLocationException;
 
 import wombat.*;
 
 import java.awt.*;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 
 import net.infonode.docking.*;
 import net.infonode.docking.util.*;
 
 import wombat.gui.icons.IconManager;
 import wombat.gui.text.NonEditableTextArea;
 import wombat.gui.text.REPLTextArea;
 import wombat.gui.text.SchemeDocument;
 import wombat.gui.text.SchemeTextArea;
 import wombat.scheme.*;
 import wombat.util.Options;
 import wombat.util.errors.*;
 import wombat.util.files.DocumentManager;
 
 /**
  * Create a main frame.
  */
 public class MainFrame extends JFrame {
 	private static final long serialVersionUID = 2574330949324570164L;
 
 	// Display components.
 	RootWindow Root;
 	Petite Petite;
     StringViewMap ViewMap;
     
     // Toolbar.
     JToolBar ToolBar;
     JButton ToolBarRun;
     JButton ToolBarStop;
     JButton UpdateButton;
     public static JLabel RowColumn;
    public boolean Running = false;
 
     // Unique code components.
     NonEditableTextArea History;
     NonEditableTextArea Display;
     NonEditableTextArea Debug;
     REPLTextArea REPL;
 
     /**
      * Don't directly create this, use me().
      * Use this method to set it up though.
      */
     public MainFrame() {
         // Set frame options.
         setTitle("Wombat - Build " + Wombat.VERSION);
         setSize(Options.DisplayWidth, Options.DisplayHeight);
         setLocation(Options.DisplayLeft, Options.DisplayTop);
         setLayout(new BorderLayout(5, 5));
         setDefaultCloseOperation(EXIT_ON_CLOSE);
         try {
         	setIconImage(IconManager.icon("Wombat.png").getImage());
         } catch(NullPointerException ex) {
         	
         }
         
         // Wait for the program to end.
         final MainFrame me = this;
         addWindowListener(new WindowAdapter() {
             public void windowClosing(WindowEvent e) {
         		Options.DisplayTop = Math.max(0, e.getWindow().getLocation().y);
             	Options.DisplayLeft = Math.max(0, e.getWindow().getLocation().x);
             	Options.DisplayWidth = Math.max(400, e.getWindow().getWidth());
             	Options.DisplayHeight = Math.max(400, e.getWindow().getHeight());
             	Options.save();
             	
             	DocumentManager.CloseAll();
             	stopAllThreads(true, false);
             	
             	me.dispose();
             }
         });
 
         // Set up the menus using the above definitions.
         MenuManager.init(this);
         setJMenuBar(MenuManager.getMenu());
         
         // Create a display for any open documents.
         Root = DockingUtil.createRootWindow(new ViewMap(), true);
         TabWindow documents = new TabWindow();
         ViewMap = new StringViewMap();
         DocumentManager.init(this, Root, ViewMap, documents);
         DocumentManager.New();
          
         // Create displays for a split REPL.
         History = new NonEditableTextArea(this);
         REPL = new REPLTextArea(this);
         ViewMap.addView("REPL - Execute", new View("REPL - Execute", null, REPL));
         ViewMap.addView("REPL - History", new View("REPL - History", null, History));
         SplitWindow replSplit = new SplitWindow(true, 0.5f, ViewMap.getView("REPL - Execute"), ViewMap.getView("REPL - History"));
         
         ViewMap.getView("REPL - Execute").getWindowProperties().setCloseEnabled(false);
         ViewMap.getView("REPL - History").getWindowProperties().setCloseEnabled(false);
         
         ViewMap.getView("REPL - Execute").getWindowProperties().setUndockEnabled(false);
         ViewMap.getView("REPL - History").getWindowProperties().setUndockEnabled(false);
         
         // Create the error/debug/display views.
         Display = new NonEditableTextArea(this);
         Debug = new NonEditableTextArea(this);
         ViewMap.addView("Display", new View("Display", null, Display));
         ViewMap.addView("Debug", new View("Debug", null, Debug));
         ErrorManager.addErrorListener(new ErrorListener() {
 			@Override
 			public void logError(String msg) {
 				Debug.append(msg + "\n");
 			}
         });
         Display.addAncestorListener(new AncestorListener() {
 			@Override
 			public void ancestorRemoved(AncestorEvent event) {
 				Display.setText("");
 			}
 			
 			@Override
 			public void ancestorMoved(AncestorEvent event) {}
 			
 			@Override
 			public void ancestorAdded(AncestorEvent event) {}
 		});
         
         // Put everything together into the actual dockable display.
         SplitWindow fullSplit = new SplitWindow(false, 0.6f, documents, replSplit);
         Root.setWindow(fullSplit);
         add(Root);
         
         // Add a toolbar.
         ToolBar = new JToolBar();
         ToolBarRun = new JButton(MenuManager.itemForName("Run").getAction());
         ToolBarStop = new JButton(MenuManager.itemForName("Stop").getAction());
         
         ToolBar.setFloatable(false);
         for (Action a : new Action[]{
         		MenuManager.itemForName("New").getAction(),
         		MenuManager.itemForName("Open").getAction(),
         		MenuManager.itemForName("Save").getAction(),
         		MenuManager.itemForName("Close").getAction()})
         	ToolBar.add(new JButton(a));
         
         ToolBar.addSeparator();
         for (Action a : new Action[]{
         		MenuManager.itemForName("Cut").getAction(),
         		MenuManager.itemForName("Copy").getAction(),
         		MenuManager.itemForName("Paste").getAction(),
         		MenuManager.itemForName("Undo").getAction(),
         		MenuManager.itemForName("Redo").getAction(),
         		MenuManager.itemForName("Find/Replace").getAction()})
         	ToolBar.add(new JButton(a));
         	
         ToolBar.addSeparator();
         ToolBar.add(ToolBarRun);
         ToolBar.add(ToolBarStop);
         for (Action a : new Action[]{
         		MenuManager.itemForName("Format").getAction(),
         		MenuManager.itemForName("Reset").getAction()})
         	ToolBar.add(new JButton(a));
         
         /*
         ToolBar.addSeparator();
         ToolBar.add(new JButton(MenuManager.itemForName("Share").getAction()));
         */
         
         add(ToolBar, BorderLayout.PAGE_START);
         ToolBar.setVisible(Options.DisplayToolbar);
         
         // Disable items by default.
         MenuManager.itemForName("Stop").setEnabled(false);
 		ToolBarStop.setEnabled(false);
 		
 		// Remove text on toolbar buttons.
 		for (Component c : ToolBar.getComponents())
 			if (c instanceof JButton)
 				((JButton) c).setText("");
 				
 		// Add a tool to show the current row and column.
 		RowColumn = new JLabel("row:column");
 		ToolBar.addSeparator();
         ToolBar.add(RowColumn);
         
         // Connect to Petite.
         try {
 			Petite = new Petite();
 		} catch (Exception e1) {
 			ErrorManager.logError(e1.getMessage());
 		}
         Thread petiteOutputThread = new Thread(new Runnable() {
         	public void run() {
         		while (true) {
         			if (Petite.hasOutput()) {
         				String output = Petite.getOutput();
         				History.append(output);
         				History.goToEnd();
         			}
         			
         			if ((Running && Petite.isReady())
         					|| (!Running && !Petite.isReady())) {
         				Running = !Petite.isReady();
             			
             			MenuManager.itemForName("Run").setEnabled(!Running);
             			ToolBarRun.setEnabled(!Running);
             	    	
             			MenuManager.itemForName("Stop").setEnabled(Running);
             			ToolBarStop.setEnabled(Running);
         			}
     				
         			try { Thread.sleep(20); } catch (InterruptedException e) { }
         		}
         	}
         });
         petiteOutputThread.setDaemon(true);
         petiteOutputThread.start();
     }
 
 	/**
      * Run a command.
      *
      * @param command The command to run.
      */
     public void doCommand(String command) {
     	MenuManager.itemForName("Run").setEnabled(false);
     	MenuManager.itemForName("Stop").setEnabled(true);
     	
     	ToolBarRun.setEnabled(false);
     	ToolBarStop.setEnabled(true);
     	
     	Running = true;
     	
         final String cmd = command.trim();
         if (cmd.length() == 0)
             return;
 
         History.append("\n~ " + cmd.replace("\n", "\n  ") + "\n");
         History.goToEnd();
         
         Petite.sendCommand(cmd);
     }
 
     /**
      * Update the display.
      */
 	public boolean updateDisplay() {
 		boolean reloaded = true;
 		for (SchemeTextArea ss : new SchemeTextArea[]{History, Display, Debug, REPL}) {
 			try {
 				((SchemeDocument) ss.code.getDocument()).processChangedLines(0, ss.getText().length());
 				ss.updateUI();
 	    	}  catch (BadLocationException e) {
 	    		reloaded = false;
 	    		ErrorManager.logError("Unable to format view: " + e.getMessage());
 			}
 		}
 		return reloaded;
 	}
 
 	/**
 	 * Focus the REPL.
 	 */
 	public void focusREPL() {
 		REPL.code.requestFocusInWindow();
 	}
 
 	/**
 	 * Set the toolbar's display mode.
 	 * @param displayToolbar If the toolbar should be visible.
 	 */
 	public void toggleToolbar(boolean displayToolbar) {
 		ToolBar.setVisible(displayToolbar);
 	}
 
 	/**
 	 * Reset Kawa.
 	 */
 	public void resetScheme() {
 		Petite.reset();
 		History.setText("");
 		History.append(">>> Environment reset <<<\n");
 	}
 
 	/**
 	 * Show the debug view.
 	 */
 	public void showDebug() {
 		View view = ViewMap.getView("Debug");
 		
 		if (!view.isShowing()) {
 			if (view.getSize().width == 0 || view.getSize().height == 0)
 				view.setSize(500, 500);
 			
 			FloatingWindow win = Root.createFloatingWindow(getLocation(), view.getSize(), view);
 			win.getTopLevelAncestor().setVisible(true);
 		}
 	}
 
 	/**
 	 * Show the given view.
 	 * @param which Which display we are writing to.
 	 */
 	public void showView(String which) {
 		View view = ViewMap.getView(which);
 		
 		if (!view.isShowing()) {
 			if (view.getSize().width == 0 || view.getSize().height == 0)
 				view.setSize(500, 500);
 			
 			FloatingWindow win = Root.createFloatingWindow(getLocation(), view.getSize(), view);
 			win.getTopLevelAncestor().setVisible(true);
 		}
 	}
 	
 	/**
 	 * Stop all running worker threads.
 	 */
 	public void stopAllThreads(boolean silent, boolean andRestart) {
 		if (silent || JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(
 				this, 
 				"Stopping will reset the current Petite process.\n" + 
 						"Are you sure you want to do this?", 
 				"Confirm Stop", JOptionPane.YES_NO_OPTION)) {
 			
 			try {
 				if (andRestart) {
 					Petite.restart();
 					
 					while (!Petite.isReady()) {
 						try { Thread.sleep(50); } catch (InterruptedException e) {}
 					}
 					
 					History.setText(">>> Execution halted <<<<\n\n");
 			    	History.goToEnd();
 				} else {
 					Petite.stop();
 				}
 			} catch (Exception e) {
 				ErrorManager.logError("Unable to reconnect to Petite:\n" + e.getMessage());
 			}
 		}
 	}
 
 	/**
 	 * Set if the update dialog should be visible or not.
 	 * @param b The new value.
 	 */
 	public void setUpdateVisible(boolean b) {
 		UpdateButton.setVisible(b);
 	}
 }
