 
 
 
 
 
 
 
 
 
 
 /*
  * The following code does nothing except for rigging up a graphical window with a text 
  * box for output. See programEntry for details of implementing your own code
  */
 
 
 
 package boot;
 
 import gui.HelpFrame;
 import gui.results;
 
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 
 import javax.swing.*;
 
 
 /**
 * This code was edited or generated using CloudGarden's Jigloo
 * SWT/Swing GUI Builder, which is free for non-commercial
 * use. If Jigloo is being used commercially (ie, by a corporation,
 * company or business for any purpose whatever) then you
 * should purchase a license for each developer using Jigloo.
 * Please visit www.cloudgarden.com for details.
 * Use of Jigloo implies acceptance of these licensing terms.
 * A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
 * THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
 * LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
 */
 public class GuiEntry extends javax.swing.JFrame implements WindowListener,ActionListener, ComponentListener {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private JMenuItem helpMenuItem;
 	private JMenu jMenu5;
 	private JMenuItem clearMenuItem;
 	private JSeparator jSeparator1;
 	private JMenuItem exitMenuItem;
 
 	private JMenu jMenu3;
 	private JMenuBar jMenuBar1;
 	private JToolBar jToolBar1;
 	
 	public static results myResults;
 	private programEntry parent;
 
 	
 	public GuiEntry(programEntry myParent) {
 		super();
 		parent = myParent;
 		initGUI();
 	}
 	
 	private void initGUI() {
 		try {
 			this.setTitle(std.string.title);
 			//Set Look & Feel
			//javax.swing.UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel");
 			this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Will call the window listeners for shutdown
 			/// See std.calls.shutdown for custom shutdown.
 //			BoxLayout thisLayout = new BoxLayout(getContentPane(), javax.swing.BoxLayout.Y_AXIS);
 //			getContentPane().setLayout(thisLayout);
 			this.setIconImage(new ImageIcon(std.objects.iconFile).getImage());
 			this.addComponentListener(this);
 
 			GridBagLayout myResultsLayout = new GridBagLayout();
 			getContentPane().setLayout(myResultsLayout);
 			GridBagConstraints c = new GridBagConstraints();
 			c.weightx = 1.0;
 			c.weighty = 1.0;
 
 			
 			// Create subdialogs
 			myResults = new results();
 			
 			addWindowListener(this);
 			setSize(500, 400);
 			{
 				jMenuBar1 = new JMenuBar();
 				setJMenuBar(jMenuBar1);
 				{
 					jMenu3 = new JMenu();
 					jMenu3.setMnemonic(KeyEvent.VK_F);
 //					jMenu3.setAccelerator(KeyStroke.getKeyStroke(
 //					        KeyEvent.VK_X, ActionEvent.ALT_MASK));
 					jMenu3.getAccessibleContext().setAccessibleDescription(
 					        "FileMenu");
 					jMenuBar1.add(jMenu3);
 					jMenu3.setText("File");
 					{
 						clearMenuItem = new JMenuItem();
 						clearMenuItem.setMnemonic(KeyEvent.VK_C);
 						clearMenuItem.setAccelerator(KeyStroke.getKeyStroke(
 						        KeyEvent.VK_C, ActionEvent.CTRL_MASK));
 						clearMenuItem.getAccessibleContext().setAccessibleDescription(
 						        "Exit TextSeer");
 						clearMenuItem.setActionCommand("clear");
 						clearMenuItem.addActionListener(this);
 						jMenu3.add(clearMenuItem);
 						clearMenuItem.setText("Clear Results");
 						
 						jSeparator1 = new JSeparator();
 						jMenu3.add(jSeparator1);
 						
 						exitMenuItem = new JMenuItem();
 						exitMenuItem.setMnemonic(KeyEvent.VK_X);
 						exitMenuItem.setAccelerator(KeyStroke.getKeyStroke(
 						        KeyEvent.VK_X, ActionEvent.CTRL_MASK));
 						exitMenuItem.getAccessibleContext().setAccessibleDescription(
 						        "Exit TextSeer");
 						exitMenuItem.setActionCommand("exit");
 						exitMenuItem.addActionListener(this);
 						jMenu3.add(exitMenuItem);
 						exitMenuItem.setText("Exit");
 					}
 				}
 //				{
 //					jMenu4 = new JMenu();
 //					jMenuBar1.add(jMenu4);
 //					jMenu4.setText("Edit");
 //					{
 //						cutMenuItem = new JMenuItem();
 //						jMenu4.add(cutMenuItem);
 //						cutMenuItem.setText("Cut");
 //					}
 //					{
 //						copyMenuItem = new JMenuItem();
 //						jMenu4.add(copyMenuItem);
 //						copyMenuItem.setText("Copy");
 //					}
 //					{
 //						pasteMenuItem = new JMenuItem();
 //						jMenu4.add(pasteMenuItem);
 //						pasteMenuItem.setText("Paste");
 //					}
 //					{
 //						jSeparator1 = new JSeparator();
 //						jMenu4.add(jSeparator1);
 //					}
 //					{
 //						deleteMenuItem = new JMenuItem();
 //						jMenu4.add(deleteMenuItem);
 //						deleteMenuItem.setText("Delete");
 //					}
 //				}
 				{
 					jMenu5 = new JMenu();
 					jMenuBar1.add(jMenu5);
 					jMenu5.setText("Help");
 					{
 						helpMenuItem = new JMenuItem();
 						jMenu5.add(helpMenuItem);
 						helpMenuItem.setText("Help");
 						helpMenuItem.setActionCommand("showHelper");
 						helpMenuItem.addActionListener(this);
 					}
 				}
 				
 			}
 			Box toolBox =  Box.createVerticalBox();
 			jToolBar1 = new JToolBar(JToolBar.VERTICAL);
 			jToolBar1.setFloatable(false);
 			
 			JButton clearButton = new JButton("Clear");
 			clearButton.setActionCommand("clear");
 			clearButton.addActionListener(this);
 			jToolBar1.add(clearButton);
 			
 			JButton runProver = new JButton("Prover");
 			runProver.setActionCommand("RunProver");
 			runProver.addActionListener(this);
 			jToolBar1.add(runProver);
 			
 			JButton reStart = new JButton("Restart");
 			reStart.setActionCommand("Restart");
 			reStart.addActionListener(this);
 			jToolBar1.add(reStart);
 			
 			toolBox.add(jToolBar1);
 			add(toolBox, c);
 			add(myResults, c);
 			
 			pack();
 			this.setMinimumSize(getSize());
 //			pack();
 //			myResults.fixDimensions(this.getSize());
 //			setSize(500, 400);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void windowActivated(WindowEvent arg0) {
 		
 		
 	}
 
 	@Override
 	public void windowClosed(WindowEvent arg0) {
 		//std.calls.display("Window Closed");
 		std.calls.shutdown();
 	}
 
 	@Override
 	public void windowClosing(WindowEvent arg0) {
 		//std.calls.display("Window Closing");
 		std.calls.shutdown();
 	}
 
 	@Override
 	public void windowDeactivated(WindowEvent arg0) {
 		
 		
 	}
 
 	@Override
 	public void windowDeiconified(WindowEvent arg0) {
 		
 		
 	}
 
 	@Override
 	public void windowIconified(WindowEvent arg0) {
 		
 		
 	}
 
 	@Override
 	public void windowOpened(WindowEvent arg0) {
 		
 		
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if ("exit".equals(e.getActionCommand())) {
 			std.calls.shutdown();
 		}
 		if ("clear".equals(e.getActionCommand())) {
 			gui.results.resultsArea.setText("");
 		}
 		if ("showHelper".equals(e.getActionCommand())) {
 			HelpFrame myHelper = new HelpFrame(this);
 			myHelper.setVisible(true);
 			//std.calls.display("Showing Helper?");
 		}
 		if ("linkToDSL".equals(e.getActionCommand())) {
 			std.calls.display("Problem Loading Site");
 			//std.calls.showResult("Problem loading website");
 //			try {
 //				// Runtime.getRuntime().exec("iexplore.exe " + std.string.homeWeb);
 //				
 //			} catch (IOException e1) {
 //				// TODO Auto-generated catch block
 //				e1.printStackTrace();
 //			}
 		}
 		if ("Restart".equals(e.getActionCommand())) {
 			parent.TextSeer();
 		}
 		
 		if ("RunProver".equals(e.getActionCommand())) {
 			/*
 			 * Need to automate the following example
 			 */
 			String backup = std.string.prover9Input;
             std.calls.showResult("Running Prover 9 within TextSeer" + std.string.endl);
             std.calls.showResult("example KB: c -> ~(a ^ b)" + std.string.endl);
             std.calls.showResult("first check: (a ^ b) is consistent" + std.string.endl);
             std.prover.Run.exec();
             //std.calls.display("Done 1 ;" + std.string.prover9Input);
             
             std.calls.showResult("second check: (a ^ b) ^ c is consistent" + std.string.endl);
             std.string.prover9Input = std.string.prover9Path + "in2.txt";
             std.string.prover9ExecString = std.string.prover9Binary + " -f " + std.string.prover9Input;
             std.Timer t = new std.Timer();
             t.reset(); t.start();
             std.prover.Run.exec();
             t.end();
             std.calls.showResult("Prover9 completed run in: " + t.toString() + std.string.endl);
             
             std.string.prover9Input = backup;
             std.string.prover9ExecString = std.string.prover9Binary + " -f " + std.string.prover9Input;
 			
 		}
 		
 		
 			
 			
 		
 	}
 
 	@Override
 	public void componentHidden(ComponentEvent arg0) {
 		
 	}
 
 	@Override
 	public void componentMoved(ComponentEvent arg0) {
 		
 	}
 
 	@Override
 	public void componentResized(ComponentEvent arg0) {
 //		myResults.fixDimensions(new Dimension(this.getWidth() - 10, this.getHeight() - 60));
 //		this.repaint();
 		// I stuffed this up, need to add in window growing in the future
 
 	}
 
 	@Override
 	public void componentShown(ComponentEvent arg0) {
 		
 	}
 
 }
