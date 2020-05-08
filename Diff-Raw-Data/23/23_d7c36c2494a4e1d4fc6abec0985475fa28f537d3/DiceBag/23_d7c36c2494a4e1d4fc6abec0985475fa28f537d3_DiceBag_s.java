 /*
 	Title: DiceBag
 	Author: Matthew Boyette
 	Date: 3/31/2012
 	
 	This application simulates dice rolls for table top games like Dungeons & Dragons. It accepts input in the form of a string.
 	The string should be formatted such that it contains two positive integers separated by the character 'd'. The 'd' is not case
 	sensitive. Additionally, it has a new combat tracking system built into it that allows it to keep track of initiative, HP, the
 	number of rounds, etc.
 	
 	Examples: 3d6, 2d8, 1d20, 15D6, 10D10, 4D4, etc.
 */
 
 import api.gui.*;
 import api.util.*;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.KeyStroke;
 
 public class DiceBag
 {
 	private final static Font	textFont				= new Font("Lucida Console", Font.PLAIN, 14);
 	private final static String	inputExceptionString	= "Incorrect input format! Provide two non-negative integers separated by the character 'd'." +
 														  "\nThe 'd' is not case sensitve." +
 														  "\nExamples: 3d6, 2d8, 1d20, 15D6, 10D10, 4D4, etc.";
 	
 	public final static void main(final String[] args)
 	{
 		new DiceBag(true);
 	}
 	
 	private boolean				debugMode	= false;
 	private JComboBox<String>	input		= null;
 	private RichTextPane		output		= null;
 	private ApplicationWindow	window		= null;
 	
 	public final static class UniqueComboBoxModel extends DefaultComboBoxModel<String>
 	{
 		private static final long	serialVersionUID	= 1L;
 		
 		public void addElement(String s)
 		{  
 			if (this.getIndexOf(s) == -1)
 			{
 				super.addElement(s);
 			}
 	    }
 	}
 	
 	public DiceBag(final boolean showWindow)
 	{
 		int choice = Support.promptDebugMode(this.getWindow());
 		this.setDebugging((choice == JOptionPane.YES_OPTION));
 		
 		// Define a self-contained ActionListener event handler.
 		EventHandler myActionPerformed = new EventHandler(this)
 		{
 			@Override
 			public final void run(final Object... arguments) throws IllegalArgumentException, RuntimeException
 			{
 				if ((arguments.length <= 1) || (arguments.length > 2))
 				{
 					throw new IllegalArgumentException("myActionPerformed Error : incorrect number of arguments.");
 				}
 				else if (!(arguments[0] instanceof ActionEvent))
 				{
 					throw new IllegalArgumentException("myActionPerformed Error : argument[0] is of incorrect type.");
 				}
 				
 				ActionEvent event = (ActionEvent)arguments[0];
 				
 				if ((((DiceBag)this.parent).getOutput() != null) && (((DiceBag)this.parent).getInput() != null))
 				{
 					/*
 						JDK 7 allows string objects as the expression in a switch statement.
 						This generally produces more efficient byte code compared to a chain of if statements.
 						http://docs.oracle.com/javase/7/docs/technotes/guides/language/strings-switch.html
 					*/
 					switch (event.getActionCommand())
 					{
 						case "Clear":
 							
 							((DiceBag)this.parent).clearLog();
 							break;
 							
 						case "Combat Tracker":
 							
 							new CombatTracker(((DiceBag)this.parent), ((DiceBag)this.parent).isDebugging());
 							break;
 							
 						case "Open":
 							
 							((DiceBag)this.parent).openLog();
 							break;
 							
 						case "Save":
 							
 							((DiceBag)this.parent).saveLog();
 							break;
 							
 						case "Throw":
 							
 							Object obj = ((DiceBag)this.parent).getInput().getSelectedItem();
 							
 							if ((obj != null) && (((String)obj).isEmpty() == false))
 							{
 								((DiceBag)this.parent).processInput(((String)((DiceBag)this.parent).getInput().getSelectedItem()).toLowerCase());
 							}
 							break;
 							
 						default:
 							
 							break;
 					}
 				}
 			}
 		};
 		
 		// Define a self-contained interface construction event handler.
 		EventHandler myDrawGUI = new EventHandler(this)
 		{
 			@Override
 			public final void run(final Object... arguments) throws IllegalArgumentException
 			{
 				if (arguments.length <= 0)
 				{
 					throw new IllegalArgumentException("myDrawGUI Error : incorrect number of arguments.");
 				}
 				else if (!(arguments[0] instanceof ApplicationWindow))
 				{
 					throw new IllegalArgumentException("myDrawGUI Error : argument[0] is of incorrect type.");
 				}
 				
 				ApplicationWindow window = (ApplicationWindow)arguments[0];
 				Container contentPane = window.getContentPane();
 				JMenuBar menuBar = new JMenuBar();
 				JMenu toolsMenu = new JMenu("Tools");
 				JMenuItem ctOption = new JMenuItem("Combat Tracker");
 				JMenu fileMenu = new JMenu("File");
 				JMenuItem clearOption = new JMenuItem("Clear");
 				JMenuItem openOption = new JMenuItem("Open");
 				JMenuItem saveOption = new JMenuItem("Save");
 				RichTextPane outputBox = new RichTextPane(window, true, window.isDebugging(), DiceBag.textFont);
 				JComboBox<String> inputBox = new JComboBox<String>();
 				JButton inputBtn = new JButton("Throw");
 				
 				menuBar.setFont(DiceBag.textFont);
 				toolsMenu.setFont(DiceBag.textFont);
 				ctOption.setFont(DiceBag.textFont);
 				fileMenu.setFont(DiceBag.textFont);
 				clearOption.setFont(DiceBag.textFont);
 				openOption.setFont(DiceBag.textFont);
 				saveOption.setFont(DiceBag.textFont);
 				contentPane.setLayout(new BorderLayout());
 				clearOption.addActionListener(window);
 				fileMenu.add(clearOption);
 				openOption.addActionListener(window);
 				fileMenu.add(openOption);
 				saveOption.addActionListener(window);
 				fileMenu.add(saveOption);
 				menuBar.add(fileMenu);
 				ctOption.addActionListener(window);
 				toolsMenu.add(ctOption);
 				menuBar.add(toolsMenu);
 				window.setJMenuBar(menuBar);
 				
 				toolsMenu.setMnemonic('T');
 				fileMenu.setMnemonic('F');
 				openOption.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.Event.CTRL_MASK));
 				openOption.setMnemonic('O');
 				saveOption.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.Event.CTRL_MASK));
 				saveOption.setMnemonic('S');
 				clearOption.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, java.awt.Event.CTRL_MASK));
 				clearOption.setMnemonic('C');
 				ctOption.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_M, java.awt.Event.CTRL_MASK));
 				ctOption.setMnemonic('M');
 				
 				JScrollPane outputPanel = new JScrollPane(outputBox);
 				JPanel inputPanel = new JPanel();
 				
 				outputBox.setBackground(Color.WHITE);
 				inputBox.setEditable(true);
 				inputBox.setFont(DiceBag.textFont);
 				inputBox.setModel(new UniqueComboBoxModel());
 				inputBtn.setFont(DiceBag.textFont);
 				inputBtn.addActionListener(window);
 				inputPanel.setLayout(new FlowLayout());
 				inputPanel.add(inputBox);
 				inputPanel.add(inputBtn);
 				contentPane.add(outputPanel, BorderLayout.CENTER);
 				contentPane.add(inputPanel, BorderLayout.SOUTH);
 				((DiceBag)this.parent).setInput(inputBox);
 				((DiceBag)this.parent).setOutput(outputBox);
 				
 				window.getRootPane().setDefaultButton(inputBtn);
 			}
 		};
 		
 		this.setWindow(new ApplicationWindow(null, "Dice Bag", new Dimension(1000, 580), this.isDebugging(), false, myActionPerformed, myDrawGUI));
 		this.getWindow().setIconImageByResourceName("icon.png");
 		
 		if (!showWindow)
 		{
 			this.getWindow().toBack();
 			this.getWindow().setVisible(false);
 		}
 	}
 	
 	public final void clearLog()
 	{
 		this.getOutput().clear();
 		this.getInput().grabFocus();
 	}
 	
 	public final JComboBox<String> getInput()
 	{
 		return this.input;
 	}
 	
 	public final RichTextPane getOutput()
 	{
 		return this.output;
 	}
 	
 	public final ApplicationWindow getWindow()
 	{
 		return this.window;
 	}
 	
 	public final boolean isDebugging()
 	{
 		return this.debugMode;
 	}
 	
 	public final void openLog()
 	{
 		this.getOutput().openFile();
 		this.getInput().grabFocus();
 	}
 	
 	public int processInput(final String inputString)
 	{
 		boolean isInputBad = true;
 		int retVal = -1;
 		
 		if ((inputString != null) && (inputString.isEmpty() == false))
 		{
 			if (inputString.matches("[0-9]+d[0-9]+"))
 			{
 				if (this.getWindow().isDebugging())
 				{
 					Support.displayDebugMessage(this.getWindow(), "Input: " + inputString + "\n");
 				}
 				
 				String[] paramArray = inputString.split("d");
 				int[] resultsArray = Games.throwDice(Integer.parseInt(paramArray[0]), Integer.parseInt(paramArray[1]));
 				int upperBound = (resultsArray.length - 1);
 				
 				StringBuilder outputStringBuilder = new StringBuilder();
 				
 				for (int i = 0; i < upperBound; i++)
 				{
 					outputStringBuilder.append(resultsArray[i] + " ");
 				}
 				
				this.getOutput().append(Color.BLACK, Color.WHITE,
					"[" + Support.getDateTimeStamp() + "]: ", Color.BLUE,
					Color.WHITE, "Input\t\t", Color.BLACK, Color.WHITE,
					inputString + "\n");
				this.getOutput().append(Color.BLACK, Color.WHITE,
					"[" + Support.getDateTimeStamp() + "]: ", Color.RED,
					Color.WHITE, "Results\t", Color.BLACK, Color.WHITE,
					outputStringBuilder.toString() + "\n");
				this.getOutput().append(Color.BLACK, Color.WHITE,
					"[" + Support.getDateTimeStamp() + "]: ",
					Color.MAGENTA, Color.WHITE, "Sum\t\t", Color.BLACK,
					Color.WHITE, resultsArray[upperBound] + "\n\n");
 				
 				isInputBad = false;
 				retVal = resultsArray[upperBound];
 				this.getInput().addItem(inputString);
 			}
 		}
 		
 		if (isInputBad)
 		{
 			Support.displayException(this.getWindow(), new IllegalArgumentException(DiceBag.inputExceptionString), false);
 		}
 		
 		this.getInput().setSelectedIndex(-1);
 		this.getInput().grabFocus();
 		return retVal;
 	}
 	
 	public final void saveLog()
 	{
 		this.getOutput().saveFile();
 		this.getInput().grabFocus();
 	}
 	
 	public final void setDebugging(final boolean debugMode)
 	{
 		this.debugMode = debugMode;
 	}
 	
 	public final void setInput(final JComboBox<String> input)
 	{
 		this.input = input;
 	}
 	
 	public final void setOutput(final RichTextPane output)
 	{
 		this.output = output;
 	}
 	
 	public final void setWindow(final ApplicationWindow window)
 	{
 		this.window = window;
 	}
 }
