 package gui;
 
 import gnu.mapping.OutPort;
 import icons.IconManager;
 
 import javax.swing.*;
 import javax.swing.text.BadLocationException;
 
 import wombat.DocumentManager;
 import wombat.Options;
 import wombat.Wombat;
 import util.KawaWrap;
 import util.errors.ErrorListener;
 import util.errors.ErrorManager;
 
 import java.awt.*;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.util.concurrent.CancellationException;
 import java.util.concurrent.ExecutionException;
 import java.util.*;
 
 import net.infonode.docking.*;
 import net.infonode.docking.util.*;
 
 /**
  * Create a main frame.
  */
 public class MainFrame extends JFrame {
 	private static final long serialVersionUID = 2574330949324570164L;
 
 	// Keep track of execution workers.
 	Queue<SwingWorker<String, Void>> workers = new LinkedList<SwingWorker<String, Void>>();
 	
 	// Display components.
 	RootWindow Root;
     KawaWrap Kawa;
     StringViewMap ViewMap;
     
     // Toolbar.
     JToolBar ToolBar;
     JButton ToolBarRun;
     JButton ToolBarStop;
     public static JLabel RowColumn;
     boolean Running = false;
 
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
             	
             	stopAllThreads();
             	DocumentManager.CloseAll();
             	
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
         
         // Put everything together into the actual dockable display.
         SplitWindow fullSplit = new SplitWindow(false, 0.6f, documents, replSplit);
         Root.setWindow(fullSplit);
         add(Root);
         
         // Connect to Kawa.
         OutPort.setOutDefault(new SchemePrinter("Display", Display));
         OutPort.setErrDefault(new SchemePrinter("Display", Display));
         Kawa = new KawaWrap();
         
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
         		MenuManager.itemForName("Redo").getAction()})
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
         
         final SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
 			@Override
 			protected String doInBackground() throws Exception {
 				return Kawa.eval(cmd);
 			}
 			
 			@Override
 			protected void done() {
 				try {
 					
 					String result = get();
 					if (result != null)
 			        	History.append(result + "\n");
 					
 					MenuManager.itemForName("Run").setEnabled(true);
 			    	MenuManager.itemForName("Stop").setEnabled(false);
 					
 			    	ToolBarRun.setEnabled(true);
 			    	ToolBarStop.setEnabled(false);
 			    	
 			    	Running = false;
 			    	
 					workers.remove(this);
 					
 				} catch (CancellationException e) {
 				} catch (InterruptedException e) {
 				} catch (ExecutionException e) {
 				}
 			}
         };
         worker.execute();
         workers.add(worker);
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
 	public void resetKawa() {
 		Kawa.reset();
		History.append("\n>>> Environment reset <<<\n");
 	}
 
 	/**
 	 * Show the debug view.
 	 */
 	public void showDebug() {
 		View view = ViewMap.getView("Debug");
 		
 		if (!view.isShowing()) {
 			if (view.getSize().width == 0 || view.getSize().height == 0)
 				view.setSize(400, 400);
 			
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
 				view.setSize(200, 200);
 			
 			FloatingWindow win = Root.createFloatingWindow(getLocation(), view.getSize(), view);
 			win.getTopLevelAncestor().setVisible(true);
 		}
 	}
 	
 	/**
 	 * Stop all running worker threads.
 	 */
 	public void stopAllThreads() {
 		while (!workers.isEmpty())
 		{
 			workers.peek().cancel(true);
 			workers.poll();
 		}
 		
 		MenuManager.itemForName("Run").setEnabled(true);
     	MenuManager.itemForName("Stop").setEnabled(false);
 		
     	ToolBarRun.setEnabled(true);
     	ToolBarStop.setEnabled(false);
     	
     	Running = false;
     	
     	History.append("\n>>> Execution halted <<<<\n");
 	}
 }
