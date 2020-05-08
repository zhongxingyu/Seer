 package lorian.graph;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JColorChooser;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JSeparator;
 import javax.swing.JTextField;
 import javax.swing.KeyStroke;
 import javax.swing.Spring;
 import javax.swing.SpringLayout;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 import javax.swing.filechooser.FileFilter;
 
 import lorian.graph.fileio.GraphFileReader;
 import lorian.graph.fileio.GraphFileWriter;
 import lorian.graph.fileio.JFileChooserWithConfirmation;
 import lorian.graph.function.Function;
 import lorian.graph.function.MathChars;
 import lorian.graph.function.Util;
 import lorian.graph.fileio.ExtensionFileFilter;
 
 public class GraphFunctionsFrame extends JFrame implements ActionListener, KeyListener, MouseListener, WindowListener {
 	private static final long serialVersionUID = -1090268654275240501L;
 	
 	public static final String version = "1.0 Beta";
 	public static final short major_version = 0x0001;
 	public static final short minor_version = 0x0000;
 	
 	public static int MaxFunctions = 20;
 	private final Dimension WindowSize = new Dimension(800, 800);
 	private final Dimension WindowSizeSmall = new Dimension(600, 600);
 	public static List<Function> functions;
 	
 	private List<JTextField> textfields;
 	private List<JLabel> labels;
 	private List<JCheckBox> checkboxes;
 	private List<ParseResultIcon> parseresults;
 	
 	private final String[] buttons = { "Draw", "Special characters"}; 
 	private final String[] calcMenuStrings = { "Value", "Zero", "Minimum", "Maximum", "Intersect", "dy/dx", MathChars.Integral.getCode() + "f(x)dx" };
 	private final Color[] defaultColors = { new Color(37, 119, 255), new Color(224,0,0).brighter(), new Color(211,0,224).brighter(), new Color(0,158,224).brighter(), new Color(0,255,90), new Color(221,224,0).brighter(), new Color(224,84,0).brighter() };  
 
 	public static boolean FileSaved = false;
 	private static boolean FilePathPresent = false;
 	private static String FileName = "Untitled";
 	private static String FilePath;
 	private static final String FileExt = "lgf";
 	private boolean empty = true;
 	
 	private static GraphFunctionsFrame funcframe;
 	public static GraphFrame gframe; 
 	private CalculateFrame calcframe;
 	private static SettingsFrame settingsframe;
 	private static SpecialCharsFrame charframe;
 	public static WindowSettings settings;
 
 	public JMenuBar menuBar;
 	public static boolean applet = false;
 	public JPanel MainPanel;
 	
 	public GraphFunctionsFrame(boolean applet)
 	{
 		super("Graph v" + version + " - " + FileName + " *");
 		textfields = new ArrayList<JTextField>();
 		labels = new ArrayList<JLabel>();
 		checkboxes = new ArrayList<JCheckBox>();
 		parseresults = new ArrayList<ParseResultIcon>();
 		functions = new ArrayList<Function>();
 		settings = new WindowSettings();
 		GraphFunctionsFrame.applet = applet;
 		initUI(false);
 		
 		if(!applet)
 		{
 			this.setVisible(true);
 		}
 
 	}
 	public GraphFunctionsFrame(boolean applet, boolean forceSmall)
 	{
 		super("Graph v" + version + " - " + FileName + " *");
 		textfields = new ArrayList<JTextField>();
 		labels = new ArrayList<JLabel>();
 		checkboxes = new ArrayList<JCheckBox>();
 		parseresults = new ArrayList<ParseResultIcon>();
 		functions = new ArrayList<Function>();
 		settings = new WindowSettings();
 		GraphFunctionsFrame.applet = applet;
 		initUI(forceSmall);
 		
 		
 		if(!applet)
 		{
 			this.setVisible(true);
 		}
 
 	}
 	private Color ChooseColor(Component component, String title, Color initialColor)
 	{
 		return JColorChooser.showDialog(component, title, initialColor);
 	}
 	
 	private static void UpdateGUIStyle()
 	{
 		try
 		{
 			SwingUtilities.updateComponentTreeUI(funcframe);
 		}
 		catch (NullPointerException e) { 
 			//System.out.println("Error changing GUI Style of function frame");
 		}
 		try
 		{
 			SwingUtilities.updateComponentTreeUI(gframe);
 		}
 		catch (NullPointerException e) {
 			//System.out.println("Error changing GUI Style of gframe");
 		}
 		try
 		{
 			SwingUtilities.updateComponentTreeUI(settingsframe);
 		}
 		catch (NullPointerException e) {
 			//System.out.println("Error changing GUI Style of settings frame");
 		}
 		try
 		{
 			SwingUtilities.updateComponentTreeUI(charframe);
 		}
 		catch (NullPointerException e) {
 			//System.out.println("Error changing GUI Style of char frame");
 		}
 		
 	}
 	public static void SetSystemLookAndFeel()
 	{		
 		try {
 			UIManager.setLookAndFeel(//"com.sun.java.swing.plaf.windows.WindowsLookAndFeel"
 					UIManager.getSystemLookAndFeelClassName()
 					);
 		} //catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) 
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			return;
 		} 		
 		UpdateGUIStyle();
 	}
 	public static void SetJavaLookAndFeel()
 	{
 		try
 		{
 			UIManager.setLookAndFeel(
 					UIManager.getCrossPlatformLookAndFeelClassName()
 					);
 		} //catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e)
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			return;
 		} 	
 		UpdateGUIStyle();
 	}
 	public static void UpdateWindowSettings()
 	{		
 		gframe.UpdateWindowSettings(settings);
 		if(gframe.windowerror)
 		{
 			JOptionPane.showMessageDialog(null, "Invalid window settings", "Error", JOptionPane.ERROR_MESSAGE);
 		}
 	}
 	public static void UpdateTitle()
 	{
		if(!applet)
			GraphFunctionsFrame.funcframe.setTitle("Graph v" + version + " - " + FileName + (FileSaved ? "" : " *"));
 	}
 	private void InitMenu()
 	{
 		JMenu fileMenu, calcMenu, helpMenu;
 		JMenuItem NewFileItem,OpenFileItem, SaveFileItem, SaveFileAsItem, settingsItem, exitItem;
 		JMenuItem aboutItem;
 		
 		menuBar = new JMenuBar();
 		fileMenu = new JMenu("File");
 		calcMenu = new JMenu("Calculate");
 		helpMenu = new JMenu("Help");
 		menuBar.add(fileMenu);
 		menuBar.add(calcMenu);
 		menuBar.add(helpMenu);
 		
 		NewFileItem = new JMenuItem("New");
 		NewFileItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK)); 
 		NewFileItem.addActionListener(this);
 		
 		OpenFileItem = new JMenuItem("Open");
 		OpenFileItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK)); 
 		OpenFileItem.addActionListener(this);
 		
 		SaveFileItem = new JMenuItem("Save");
 		SaveFileItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK)); 
 		SaveFileItem.addActionListener(this);
 		
 		SaveFileAsItem = new JMenuItem("Save as");
 		SaveFileAsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK)); 
 		SaveFileAsItem.addActionListener(this);
 		
 		settingsItem = new JMenuItem("Settings");
 		settingsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.ALT_MASK)); 
 		settingsItem.addActionListener(this);
 		
 		
 		fileMenu.add(NewFileItem);
 		fileMenu.add(OpenFileItem);
 		fileMenu.add(SaveFileItem);
 		fileMenu.add(SaveFileAsItem);
 		fileMenu.addSeparator();
 		fileMenu.add(settingsItem); 
 		
 		
 		if(!applet)
 		{
 			exitItem = new JMenuItem("Exit");
 			exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK));
 			exitItem.addActionListener(this);
 			fileMenu.addSeparator();
 			fileMenu.add(exitItem);
 		}
 		
 		for(int i=0;i<calcMenuStrings.length; i++)
 		{
 			JMenuItem item = new JMenuItem(calcMenuStrings[i]);
 			item.addActionListener(this);
 			calcMenu.add(item);
 		}
 		
 		
 		aboutItem = new JMenuItem("About");
 		aboutItem.addActionListener(this);
 		helpMenu.add(aboutItem);
 		if(!applet)
 			this.setJMenuBar(menuBar);
 	}
 	private void initUI(boolean forceSmall)
 	{
 		if(applet) MainPanel = new JPanel();
 		
 		boolean small;
 		if(!applet && !forceSmall)
 		{
 			if(Toolkit.getDefaultToolkit().getScreenSize().getHeight() <= 900) 
 				small = true;
 			else 
 				small = false;
 			}
 		else
 		{
 			System.out.println("Forcing small mode");
 			small = forceSmall;
 		}
 	
 		
 		SetSystemLookAndFeel();
 		if(small)
 		{
 			System.out.println("Setting window to smaller size");
 			gframe = new GraphFrame(functions, settings, WindowSizeSmall);
 			GraphFunctionsFrame.MaxFunctions = 18;
 		}
 		else
 			gframe = new GraphFrame(functions, settings, WindowSize);
 		
 		JPanel panel = new JPanel();
 		SpringLayout layout = new SpringLayout();
 		panel.setLayout(layout);
 		
 		int height = 0;
 		
 		for(int i=0;i<MaxFunctions; i++)
 		{
 			JPanel funcpanel = new JPanel();
 			SpringLayout funcpanellayout = new SpringLayout();
 			funcpanel.setLayout(funcpanellayout);
 			
 			JCheckBox checkBox = new JCheckBox();
 			checkBox.setSelected(true);
 			checkBox.setFocusable(false);
 			checkBox.addActionListener(this);
 			checkBox.setName("c" + i);
 			
 			JLabel label = new JLabel("Y" + (i+1) + " = ");
 			label.setFont(label.getFont().deriveFont(13.0f));
 			label.setOpaque(true);
 			label.setBackground(defaultColors[i % defaultColors.length]);
 			label.setForeground(Color.BLACK);
 			label.addMouseListener(this);
 			
 			
 			JTextField textField = new JTextField();
 				
 			textField.setPreferredSize(new Dimension(350, (int) textField.getPreferredSize().getHeight() + 5)); 
 			textField.setFont(textField.getFont().deriveFont(15.0f));
 			textField.setForeground(Color.BLACK);
 			textField.addKeyListener(this);
 			
 			//JTextPane textField = new JTextPane();
 			//textField.setContentType("text/html");
 			//textField.setText("<HTML>x<sup>2</sup></HTML> ");
 			//textField.setPreferredSize(new Dimension( 55 * (int) textField.getPreferredSize().getWidth(), (int) textField.getPreferredSize().getHeight()));
 			ParseResultIcon icon = new ParseResultIcon();
 			icon.setState(ParseResultIcon.State.EMPTY);
 			
 			
 			height = ((int) textField.getPreferredSize().getHeight() + 5);
 			funcpanel.setPreferredSize(new Dimension(450, height));
 			
 			funcpanel.add(checkBox);
 			funcpanel.add(label);
 			funcpanel.add(textField);
 			funcpanel.add(icon);
 			
 			SpringLayout.Constraints labelCons = funcpanellayout.getConstraints(label);
 			labelCons.setX(Spring.constant(25));
 			labelCons.setY(Spring.constant(3));
 			SpringLayout.Constraints textFieldCons = funcpanellayout.getConstraints(textField);
 			textFieldCons.setX(Spring.constant(70));
 			SpringLayout.Constraints iconCons = funcpanellayout.getConstraints(icon);
 			iconCons.setX(Spring.constant(423));
 			
 			panel.add(funcpanel);
 			SpringLayout.Constraints funcPanelCons = layout.getConstraints(funcpanel);
 			funcPanelCons.setX(Spring.constant(0));
 			funcPanelCons.setY(Spring.constant(8 + height * i));
 			
 			/*
 			panel.add(label);
 			panel.add(textField);
 			
 			SpringLayout.Constraints labelCons = layout.getConstraints(label);
 			labelCons.setX(Spring.constant(5));
 			labelCons.setY(Spring.constant(8 + height * i));
 			SpringLayout.Constraints textFieldCons = layout
 				        .getConstraints(textField);
 			textFieldCons.setX(Spring.constant(50));
 			textFieldCons.setY(Spring.constant(5 + height  * i));
 			*/
 			checkboxes.add(checkBox);
 			textfields.add(textField);
 			labels.add(label);
 			parseresults.add(icon);
 			
 			//funcpanel.requestFocusInWindow();
 
 			
 			
 		}
 		JPanel buttonpanel = new JPanel();
 		for(int i=0;i< buttons.length ;i++)
 		{
 			JButton button = new JButton(buttons[i]);
 			button.setFont(button.getFont().deriveFont(13.0f));
 			button.addActionListener(this);
 			buttonpanel.add(button);
 		}
 		SpringLayout.Constraints buttonPanelCons = layout.getConstraints(buttonpanel);
 		buttonPanelCons.setX(Spring.constant(50));
 		buttonPanelCons.setY(Spring.constant(10 + height *MaxFunctions));
 		panel.add(buttonpanel);
 		
 		if(applet) MainPanel.setLayout(new BorderLayout());
 		else this.setLayout(new BorderLayout());
 		
 		panel.setPreferredSize(new Dimension(450, 50 + height * (MaxFunctions+1)));
 		
 		if(applet)
 		{
 			MainPanel.add(panel, BorderLayout.WEST);
 			MainPanel.add(new JSeparator(JSeparator.VERTICAL));		
 			MainPanel.add((JPanel) gframe, BorderLayout.EAST);
 		}
 		else
 		{
 			this.add(panel, BorderLayout.WEST);
 			this.add(new JSeparator(JSeparator.VERTICAL));		
 			this.add((JPanel) gframe, BorderLayout.EAST);
 		}
 		InitMenu();
 		
 		Render();
 		if(!applet)
 			this.pack();
 		
 		textfields.get(0).requestFocusInWindow();
 		this.setBackground(Color.WHITE);
 		if(applet)
 		{
 			if(small)
 				MainPanel.setSize(450 + (int) WindowSizeSmall.getWidth() + 10, (int) WindowSizeSmall.getHeight() + 50);
 			else
 				MainPanel.setSize(450 + (int) WindowSize.getWidth() + 10, (int) WindowSize.getHeight() + 50);
 		}
 		else
 		{
 			if(small)
 				this.setSize(450 + (int) WindowSizeSmall.getWidth() + 10, (int) WindowSizeSmall.getHeight() + 50);
 			else
 				this.setSize(450 + (int) WindowSize.getWidth() + 10, (int) WindowSize.getHeight() + 50);
 			this.setResizable(false);
 			this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
 			this.addWindowListener(this);
 			this.setLocationRelativeTo(null);
 		}
 		
 	}
 	
 	private boolean SaveFile() 
 	{
 		System.out.println("Saving to " + FilePath);
 		GraphFileWriter fw = new GraphFileWriter(FilePath);
 		fw.setWindowSettings(settings);
 		int i=0;
 		for(Function f: functions)
 		{
 			if(!f.isEmpty())
 			{
 				fw.addFunction(f, i);
 			}
 			i++;
 		}
 		try
 		{
 			boolean result =  fw.write();
 			System.out.println("Done");
 			FileSaved = result;
 			empty = false;
 			return result;
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 			return false;
 		}
 		
 	}
 	
 	private boolean OpenFile(String filePath)
 	{
 		System.out.println("Opening " + filePath);
 		GraphFileReader fr = new GraphFileReader(filePath);
 		try
 		{
 			if(!fr.read()) return false;
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			JOptionPane.showMessageDialog(this, "Could not open " + (new File(filePath)).getName(), "Error", JOptionPane.ERROR_MESSAGE);
 			return false;
 		}
 
 		ClearAll();
 		
 		
 		String[] reconstructedfunctions = fr.getReconstructedFunctionStrings();
 		short[] indexes = fr.getFunctionIndexes();
 		Function[] functions = fr.getReconstructedFunctions();
 		
 		settings = fr.getWindowSettings();
 		for(int i=0;i<indexes.length; i++)
 		{
 			if(reconstructedfunctions[i].trim().equalsIgnoreCase("")) continue;
 			
 			int j = indexes[i];
 			JCheckBox checkbox = this.checkboxes.get(j);
 			JLabel label = this.labels.get(j);
 			JTextField textfield = this.textfields.get(j);
 			checkbox.setSelected(functions[i].drawOn());
 			label.setBackground(functions[i].getColor());
 			textfield.setText(reconstructedfunctions[i]);
 			this.checkboxes.set(j, checkbox);
 			this.labels.set(j, label);
 			this.textfields.set(j, textfield);
 		}
 		
 		UpdateWindowSettings();
 		Render();
 		FilePath = filePath;
 		FilePathPresent = true;
 		FileName = (new File(filePath)).getName();
 		FileSaved = true;
 		return true;
 	}
 	
 	private boolean ShowOpenFileDialog()
 	{
 		JFileChooserWithConfirmation openFile;
 		 if(!FilePathPresent)
 			 openFile = new JFileChooserWithConfirmation(System.getProperty("user.dir"));
 		 else
 			 openFile = new JFileChooserWithConfirmation(new File(FilePath));
 		 
 		 FileFilter openFilter = new ExtensionFileFilter("Graph files", new String[] { FileExt });
 		 openFile.setFileFilter(openFilter);
 		 
 		 int openOption = openFile.showOpenDialog(this);
 		 if(openOption == JFileChooser.APPROVE_OPTION)
 		 {
 			String filePath = openFile.getSelectedFile().getAbsolutePath();
 			if(!filePath.endsWith("." + FileExt))
 			{
 				filePath += "." + FileExt;
 			}
 			return OpenFile(filePath);
 		 }
 		 else return false;
 	}
 	private boolean SaveFileAs()
 	{
 		 JFileChooserWithConfirmation saveFile;
 		 if(!FilePathPresent)
 			 saveFile = new JFileChooserWithConfirmation(System.getProperty("user.dir"));
 		 else
 			 saveFile = new JFileChooserWithConfirmation(new File(FilePath));
 		 
 	
 		 FileFilter saveFilter = new ExtensionFileFilter("Graph files", new String[] { FileExt });
 		 saveFile.setFileFilter(saveFilter);
 		 
 		 int saveOption = saveFile.showSaveDialog(this);
 		 if(saveOption == JFileChooser.APPROVE_OPTION)
 		 {
 			FilePath = saveFile.getSelectedFile().getAbsolutePath();
 			if(!FilePath.endsWith("." + FileExt))
 			{
 				FilePath += "." + FileExt;
 			}
 			FilePathPresent = true;
 			FileName = (new File(FilePath)).getName();
 			return SaveFile();
 					
 		 }
 		 else return false;
 	}
 	private boolean ConfirmFileChanges()
 	{
 		
 		int n= JOptionPane.showConfirmDialog (this, String.format("Do you want to save changes to %s?", FileName), "Graph", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
 		
 		if(n == JOptionPane.YES_OPTION)
 		{
 			return SaveFileAs();
 		}
 		else if(n == JOptionPane.NO_OPTION)
 		{
 			return true;
 		}
 		else if(n == JOptionPane.CANCEL_OPTION)
 		{
 			return false;
 		}
 		return false;
 	}
 	
 	private void ClearAll()
 	{
 		for(JTextField txt: textfields)
 		{
 			txt.setText("");
 		}
 		for(JCheckBox check : checkboxes)
 		{
 			check.setSelected(true);
 		}
 		for(int i=0;i<MaxFunctions;i++)
 		{
 			JLabel label = this.labels.get(i);
 			label.setBackground(defaultColors[i % defaultColors.length]);
 		}
 		
 		settings = new WindowSettings();
 		FilePath = "";
 		FileSaved = false;
 		FilePathPresent = false;
 		FileName = "Untitled";
 		empty = true;
 		Render();
 	}
 
 	
 	private void Render()
 	{
 		System.out.println("Parsing functions...");
 		functions.clear();
 		for(int i=0;i<GraphFunctionsFrame.MaxFunctions;i++)
 		{
 			String text = Util.removeWhiteSpace(textfields.get(i).getText().trim());
 			Function f = new Function();
 			ParseResultIcon parseresult = parseresults.get(i);
 			if(text.isEmpty()) {
 				f.setDraw(false);
 				functions.add(f);
 				//System.out.println("Y" + (i+1) + " is empty. Skipping.");
 				parseresult.setState(ParseResultIcon.State.EMPTY);
 				parseresults.set(i, parseresult);
 				continue;
 			}
 			empty = false;
 			if(!f.Parse(text))
 			{
 				System.out.println("Error: Unable to parse function Y" + (i+1));
 				f.clear();
 				f.setDraw(false);
 				functions.add(f);
 				parseresult.setState(ParseResultIcon.State.ERROR);
 				parseresults.set(i, parseresult);
 				continue;
 				
 			}
 			Color c = labels.get(i).getBackground();
 			f.setColor(c);
 			f.setDraw(checkboxes.get(i).isSelected());
 			functions.add(f);
 			parseresult.setState(ParseResultIcon.State.OK);
 			parseresults.set(i, parseresult);
 			System.out.println("Added function Y" + (i+1) + " with color " + c.getRed() + "," + c.getGreen() + "," + c.getBlue());
 		}
 		gframe.Update(functions);
 		this.repaint();
 		if(calcframe != null)
 		{
 			calcframe.Update();
 		}
 		System.out.println("Done");		
 	}
 
 	public static void WriterTest()
 	{
 		GraphFileWriter fw = new GraphFileWriter("test.bin");
 		WindowSettings wsettings = new WindowSettings();
 		fw.setWindowSettings(wsettings);
 		Function f = new Function("sin(x)cos(x)");
 		Function g = new Function("4x^3+3x+2");
 		
 		fw.addFunction(f, 0);
 		fw.addFunction(g, 3);
 		try {
 			fw.write();
 		} catch (IOException e) {
 			e.printStackTrace();
 			return;
 		} 
 		System.out.println("Done writing");
 		
 	
 	}
 	private static void ReaderTest()
 	{
 		GraphFileReader fr = new GraphFileReader("test.lgf");
 		try {
 			fr.read();
 		} catch (IOException e) {
 			e.printStackTrace();
 			return;
 		}
 		System.out.println("Done reading");
 		String[] reconstructedfunctions = fr.getReconstructedFunctionStrings();
 		short[] indexes = fr.getFunctionIndexes();
 		int i = 0;
 		for(String fstr : reconstructedfunctions)
 		{
 			if(fstr.trim().equalsIgnoreCase(""))
 			{
 				i++;
 				continue;
 			}
 			System.out.println(indexes[i] + ": " + fstr);
 			i++;
 		}
 	}
 	public static void main(String[] args)
 	{
 		System.out.println("Graph v" + GraphFunctionsFrame.version);
 		
 		//WriterTest();
 		//ReaderTest();
 		
 		GraphFunctionsFrame.funcframe = new GraphFunctionsFrame(false);
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if(e.getSource() instanceof JButton || e.getSource() instanceof JMenuItem)
 		{
 			String buttonname = e.getActionCommand();
 			
 			if(buttonname.equalsIgnoreCase(buttons[0])) //render
 			{
 				Render();
 			}
 			else if(buttonname.equalsIgnoreCase(buttons[1])) //Special chars
 			{
 				if(charframe == null)
 					charframe = new SpecialCharsFrame(this.getLocation());
 				else 
 					charframe.Restore();
 			}
 			
 			else if(buttonname.equalsIgnoreCase("new"))
 			{
 				System.out.println("Creating new file...");
 				if(!FileSaved && !empty)
 				{
 					if(!ConfirmFileChanges())
 					{
 						System.out.println("New file canceled by user");
 						return;
 					}
 				}
 				ClearAll();
 				UpdateTitle();
 			}
 			else if(buttonname.equalsIgnoreCase("open"))
 			{
 				System.out.println("Opening file...");
 				if(!FileSaved && !empty)
 				{
 					if(!ConfirmFileChanges())
 					{
 						System.out.println("Canceled by user");
 						return;
 					}
 				}
 				ShowOpenFileDialog();
 				UpdateTitle();
 			}
 			else if(buttonname.equalsIgnoreCase("save"))
 			{
 				if(FilePathPresent)
 				{
 					SaveFile();
 				}
 				else
 				{
 					System.out.println("Saving file");
 					SaveFileAs();
 				}
 				UpdateTitle();
 			}
 			else if(buttonname.equalsIgnoreCase("save as"))
 			{
 				System.out.println("Saving file");
 				SaveFileAs();
 				
 				UpdateTitle();
 			}
 			
 			else if (buttonname.equalsIgnoreCase("settings")) 
 			{
 				if(settingsframe == null)
 				{
 					settingsframe = new SettingsFrame(this.getLocation());
 				}
 				else
 					settingsframe.Restore();
 			}
 			
 			else if (buttonname.equalsIgnoreCase("exit")) 
 			{
 				System.exit(0);
 			}	
 			else if(Util.StringArrayGetIndex(calcMenuStrings, buttonname) != -1)
 			{
 	
 				switch(Util.StringArrayGetIndex(calcMenuStrings, buttonname))
 				{
 					case 0: // value
 					{
 						calcframe = new CalculateFrame(CalculateFrame.Calculation.VALUE);
 						break;
 					}
 					case 1: // zero
 					{
 						calcframe = new CalculateFrame(CalculateFrame.Calculation.ZERO);
 						break;
 					}
 					case 2: // minimum
 					{
 						calcframe = new CalculateFrame(CalculateFrame.Calculation.MINIMUM);
 						break;
 					}
 					case 3: // maximum
 					{
 						calcframe = new CalculateFrame(CalculateFrame.Calculation.MAXIMUM);
 						break;
 					}
 					case 4: // intersect
 					{
 						calcframe = new CalculateFrame(CalculateFrame.Calculation.INTERSECT);
 						break;
 					}
 					case 5: //  dy/dx
 					{
 						calcframe = new CalculateFrame(CalculateFrame.Calculation.DYDX);
 						break;
 					}
 					case 6: // integral
 					{
 						calcframe = new CalculateFrame(CalculateFrame.Calculation.INTEGRAL);
 						break;
 					}
 					default:
 					{
 						return;
 					}
 				}
 				gframe.setCalcPanel(calcframe);
 				gframe.setCalcPanelVisible(true);
 			}
 		}
 		else if(e.getSource() instanceof JCheckBox)
 		{
 			JCheckBox source = (JCheckBox) e.getSource();
 			int index = Integer.parseInt(source.getName().substring(1));
 			if(!textfields.get(index).getText().trim().isEmpty()) 
 				Render();
 		}
 	}
 	
 	@Override
 	public void keyPressed(KeyEvent e) {
 		if(e.getKeyChar() == KeyEvent.VK_ENTER)
 		{
 			Render();
 		}
 		
 	}
 
 	@Override
 	 public void mouseReleased(MouseEvent e)  
 	    {  
 	        JLabel label = ((JLabel)e.getSource());
 	        //label.setBackground(JColorChooser.showDialog(null, "Choose function color", label.getBackground()));
 	        label.setBackground(ChooseColor(null, "Choose function color", label.getBackground()));
 	        int funcindex;
 	        String functext = label.getText().substring(1, label.getText().length()-3);
 	        funcindex = Integer.parseInt(functext.trim()) - 1;
 	        Color c = label.getBackground();
 			Function f = functions.get(funcindex);
 			f.setColor(c);
 			functions.set(funcindex, f);
 	        gframe.Update(funcindex, functions.get(funcindex));
 	    }  
 	
 	@Override
 	public void keyTyped(KeyEvent e) 
 	{
 		if((e.getKeyChar() >= 'a' && e.getKeyChar() <= 'z') ||(e.getKeyChar() >= 'A' && e.getKeyChar() <= 'Z') || (e.getKeyChar() >= '0' && e.getKeyChar() <= '9') || e.getKeyChar() == '(' || e.getKeyChar() == ')' || e.getKeyChar() == '^' || e.getKeyChar() == ' ' || e.getKeyChar() == 0x08)
 		{
 			FileSaved = false;
 			empty = false;
 			UpdateTitle();
 		}
 	}	
 	
 	@Override
 	public void windowClosing(WindowEvent e) {
 		if(!FileSaved && !empty)
 		{
 			if(ConfirmFileChanges()) System.exit(0);
 		}
 		else
 			System.exit(0);
 	}
 	
 	
 	//Unused
 	@Override
 	public void keyReleased(KeyEvent e) {}
 	@Override
 	public void mouseClicked(MouseEvent arg0) {}
 	@Override
 	public void mouseEntered(MouseEvent arg0) {}
 	@Override
 	public void mouseExited(MouseEvent arg0) {}
 	@Override
 	public void mousePressed(MouseEvent arg0) {}
 	@Override
 	public void windowActivated(WindowEvent arg0) {}
 	@Override
 	public void windowClosed(WindowEvent arg0){}
 	@Override
 	public void windowDeactivated(WindowEvent arg0) {}
 	@Override
 	public void windowDeiconified(WindowEvent arg0) {}
 	@Override
 	public void windowIconified(WindowEvent arg0) {}
 	@Override
 	public void windowOpened(WindowEvent arg0) {}
 	
 }
