 package org.reprap.gui;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.IOException;
 import java.lang.System;
 import java.util.Arrays;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextField;
 
 /**
  * This reads in the preferences file and constructs a set of menus from it to allow entries
  * to be edited.
  * 
  * Preference keys either start with the string "Extruder" followed by a number
  * and an underscore (that is, they look like "Extruder3_temp(C)") in which case they
  * are assumed to be a characteristic of the extruder with that number; or they don't,
  * in which case they are assumed to be global characteristics of the entire machine.
  * 
  * The keys should end with their dimensions: "Extruder3_temp(C)", "Axis2Scale(steps/mm)", but
  * regretably can't contain unescaped space characters (see java.util.Properties).
  * 
  * Some weak type checking is done to prevent obvious crassness being put in the edit
  * boxes.  This is done at save time and prevents the junk being written, but doesn't give
  * a chance to correct it.
  * 
  * Extensively adapted from Simon's old version by Adrian to construct itself from
  * the preferences file.
  * 
  * TODO: make booleans use check boxes, not "true" or "false".
  */
 
 //Boxes must contain one of three types:
 
 enum Category
 {
 	number, string, bool;
 }
 
 public class Preferences extends javax.swing.JDialog {
 	
 	// Pixel dimensions of boxes and things
 	
 	static private final int gx = 10;    // Horizontal gap
 	static private final int gy = 5;     // Vertical gap
 	static private final int tx = 9;     // Character X width (average)
 	static private final int ty = 20;    // Character Y height
 	static private final int bx = 80;    // Button X width
 	static private final int by = 20;    // Button Y height
 	static private final int taby = 50;  // Tab Y height
 	static private final int maxy = 700; // Maximum Y height
 	
 	// Load of arrays for all the stuff...
 	
 	private int extruderCount;
 	JLabel[] globals;              // Array of JLabels for the general key names
 	JTextField[] globalValues;     // Array of JTextFields for the general variables
 	Category[] globalCats;         // What are they?
 	JLabel[][] extruders;          // Array of Arrays of JLabels for the extruders' key names
 	JTextField[][] extruderValues; // Array of Arrays of JTextFields for the extruders' variables
 	Category[][] extruderCats;     // What are they?
 	int longestGlobal;             // The longest string in the global list
 	int longestGlobalVal;          // The longest value in the global list
 	int longestExtruders[];        // The longest strings in the extuder lists
 	int longestExtruderVals[];     // The longest strings in the extuder lists
 	int columns;                   // The number of menu columns to use
 
 	// Get the show on the road...
 	
 	public static void main(String[] args) {
 		JFrame frame = new JFrame();
 		Preferences inst = new Preferences(frame);
 		inst.setVisible(true);
 	}
 	
 
 	/**
 	 * Get the value corresponding to name from the preferences file
 	 * @param name
 	 * @return String
 	 */
 	private String loadString(String name) throws IOException {
 		return org.reprap.Preferences.loadGlobalString(name);
 	}
 	
 	/**
 	 * Save the value corresponding to name to the preferences file
 	 * @param name
 	 * @param value
 	 */	
 	private void saveString(String name, String value) throws IOException {
 		org.reprap.Preferences.setGlobalString(name, value);
 	}
 	
 	/**
 	 * Save the lot to the preferences file
 	 *
 	 */
 	public void savePreferences() {
 		try {
 			for(int i = 0; i < globals.length; i++)
 			{
 				String s = globalValues[i].getText();
 				if(category(s) != globalCats[i])
 					System.err.println("Dud format for " + globals[i].getText() + ": " + s);
 				else
 					saveString(globals[i].getText(), s);
 			}
 			
 			for(int j = 0; j < extruderCount; j++)
 			{
 				JLabel[] enames = extruders[j];
 				JTextField[] evals = extruderValues[j];
 				Category[] cats = extruderCats[j];
 				for(int i = 0; i < enames.length; i++)
 				{
 					String s = evals[i].getText();
 					if(category(s) != cats[i])
 						System.err.println("Dud format for " + enames[i].getText() + ": " + s);
 					else
 						saveString(enames[i].getText(), s);
 				}
 			}
 			
 			org.reprap.Preferences.saveGlobal();
 		} catch (Exception ex) {
 			JOptionPane.showMessageDialog(null, "Saving preferences: " + ex);
 			ex.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Constructor loads all the information from the preferences file,
 	 * converts it into arrays of JPanels and JTextFields, then builds the
 	 * menus from them.
 	 * 
 	 * @param frame
 	 */
 	public Preferences(JFrame frame) 
 	{
 		super(frame);
 		
 		columns = 1; // Default
 		
 		// Start with everything that isn't an extruder value.
 		
 		try {
 			String[] g = org.reprap.Preferences.notStartsWith("Extruder");
 			Arrays.sort(g);
 			globals = makeLabels(g);
 			globalValues = makeValues(globals);
 			globalCats = categorise(globalValues);
 			longestGlobal = jLabelListLongest(globals);
 			longestGlobalVal = jTextFieldListLongest(globalValues);
 		}catch (Exception ex)
 		{
 			System.err.println("Preferences window: Can't load the globals!");
 			ex.printStackTrace();
 		}
 		
 		// Next we need to know how many extruders we've got.
 		
 		try{
 			extruderCount = Integer.parseInt(loadString("NumberOfExtruders"));
 		} catch (Exception ex)
 		{
 			System.err.println("Preferences window: Can't load the extruder count!");
 			ex.printStackTrace();
 		}
 		
 		// Now build a set of arrays for each extruder in turn.
 		
 		extruders= new JLabel[extruderCount][];
 		extruderValues= new JTextField[extruderCount][];
 		longestExtruders = new int[extruderCount];
 		longestExtruderVals = new int[extruderCount];
 		extruderCats = new Category[extruderCount][];
 		try {
 			for(int i = 0; i < extruderCount; i++)
 			{
 				String[] a = org.reprap.Preferences.startsWith("Extruder" + i);
 				Arrays.sort(a);
 				extruders[i] = makeLabels(a);
 				longestExtruders[i] = jLabelListLongest(extruders[i]);
 				extruderValues[i]= makeValues(extruders[i]);
 				extruderCats[i] = categorise(extruderValues[i]);
 				longestExtruderVals[i] = jTextFieldListLongest(extruderValues[i]);
 			}
 		}catch (Exception ex)
 		{
 			System.err.println("Preferences window: Can't load extruder(s)!");
 			ex.printStackTrace();
 		}
 		
 		// Paint the lot on the screen...
 		
 		initGUI();
         Utility.centerWindowOnParent(this, frame);
 	}
 	
 	/**
 	 * Set up the panels with all the right boxes in
 	 *
 	 */
 	private void initGUI() 
 	{
 		JButton jButtonOK;     // Save and exit
 		JButton jButtonCancel; // Exit without save
 
 		// Work out overall dimensions
 
 		int ypane = yAll();		
 		if(ypane > maxy)
 		{
 			columns = 1 + ypane/maxy;
 			ypane = yAll();
 		}
 		int xall = (xAll() + 2*gx)*columns + gx;
 		int yall = ypane + by + 3*gy + taby;
 
 		// Put it all together
 
 		try {
 			// Start with the buttons
 
 			jButtonOK = new JButton();
 			getContentPane().add(jButtonOK);
 			jButtonOK.setText("OK");
 			jButtonOK.setBounds(xall - (3*gx + bx), ypane + by + 2*gy, bx, by);
 			jButtonOK.addMouseListener(new MouseAdapter() 
 			{
 				public void mouseClicked(MouseEvent evt) 
 				{
 					jButtonOKMouseClicked(evt);
 				}
 			});
 
 
 			jButtonCancel = new JButton();
 			getContentPane().add(jButtonCancel);
 			jButtonCancel.setText("Cancel");
 			jButtonCancel.setBounds(3*gx, ypane + by + 2*gy, bx, by);
 			jButtonCancel.addMouseListener(new MouseAdapter() 
 			{
 				public void mouseClicked(MouseEvent evt) 
 				{
 					jButtonCancelMouseClicked(evt);
 				}
 			});
 
 			// We'll have a tab for the globals, then one 
 			// for each extruder
 
 			JTabbedPane jTabbedPane1 = new JTabbedPane();
 			getContentPane().add(jTabbedPane1);
 			jTabbedPane1.setBounds(gx, gy, xall, yall);
 
 
 			// Do the global panel
 
 			JPanel jPanelGeneral = new JPanel();
 			jTabbedPane1.addTab("Globals", null, jPanelGeneral, null);
 			jPanelGeneral.setPreferredSize(new java.awt.Dimension(xall, ypane));
 			jPanelGeneral.setLayout(null);
 
 			// Start top left
 
 			int x = 0;
 			int y = gy;
 			int xw;     // X coordinate of the edit boxes
 			xw = longestGlobal*tx + 2*gx;
 			int i = 0;
 			int lim;
 			
 			for(int c = 1; c <= columns; c++)
 			{
 				if(c == columns)
 					lim = globals.length%(1 + globals.length/columns);
 				else
 					lim = 1 + globals.length/columns;
 				y = gy;
 				for(int d = 0; d < lim; d++)
 				{
 					globals[i].setBounds(x + gx, y, longestGlobal*tx, ty);
 					jPanelGeneral.add(globals[i]);
 					globalValues[i].setBounds(x + xw, y, longestGlobalVal*tx, ty);
 					jPanelGeneral.add(globalValues[i]);
 					y = y + ty + gy;
 					i++;
 				}
 				x += (longestGlobal + longestGlobalVal)*tx + 3*gx;
 			}
 
 			// Do all the extruder panels
 
 			for(int j = 0; j < extruderCount; j++)
 			{
 				JLabel[] keys = extruders[j];
 				JTextField[] values = extruderValues[j];
 
 				JPanel jPanelExtruder = new JPanel();
 				jTabbedPane1.addTab("Extruder" + j, null, jPanelExtruder, null);
 				jPanelExtruder.setLayout(null);
 				jPanelExtruder.setPreferredSize(new java.awt.Dimension(xall, ypane));
 				
 				
 				x = 0;
 				y = gy;
 				xw = longestExtruders[j]*tx + 2*gx;
 				i = 0;
 				
 				for(int c = 1; c <= columns; c++)
 				{
 					if(c == columns)
 						lim = keys.length%(1 + keys.length/columns);
 					else
 						lim = 1 + keys.length/columns;
 					y = gy;
 					for(int d = 0; d < lim; d++)
 					{
 						keys[i].setBounds(x + gx, y, longestExtruders[j]*tx, ty);
 						jPanelExtruder.add(keys[i]);
 						values[i].setBounds(x + xw, y, longestExtruderVals[j]*tx, ty);
 						jPanelExtruder.add(values[i]);
 						y = y + ty + gy;
 						i++;
 					}
 					x += (longestExtruders[j] + longestExtruderVals[j])*tx + 3*gx;
 				}
 			}	
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
		
		// Wrap it all up
		getContentPane().setLayout(null);
		setTitle("RepRap Preferences");
		setSize(xall, yall);
 	}
 	
 	/**
 	 * What to do when OK is clicked
 	 * @param evt
 	 */
 	private void jButtonOKMouseClicked(MouseEvent evt) {
 		// Update all preferences
 		savePreferences();
 		dispose();
 	}
 	
 	/**
 	 * What to do when Cancel is clicked
 	 * @param evt
 	 */
 	private void jButtonCancelMouseClicked(MouseEvent evt) {
 		// Close without saving
 		dispose();
 	}
 	
 	/**
 	 * Find the character count of the longest string in a label (key)
 	 * @param a
 	 * @return
 	 */
 	private int jLabelListLongest(JLabel[] a)
 	{
 		int result = 0;
 		for(int i = 0; i < a.length; i++)
 		{
 			int len = a[i].getText().length();
 			if(len > result)
 				result = len;
 		}
 		return result;
 	}
 	
 	/**
 	 * Find the character count of the longest string in a value 
 	 * @param a
 	 * @return
 	 */
 	private int jTextFieldListLongest(JTextField[] a)
 	{
 		int result = 0;
 		for(int i = 0; i < a.length; i++)
 		{
 			int len = a[i].getText().length();
 			if(len > result)
 				result = len;
 		}
 		return result;
 	}
 
 	/**
 	 * Work out the pixel width of a key + value pair
 	 * @param longestLab
 	 * @param longestVal
 	 * @return
 	 */
 	private int xSize(int longestLab, int longestVal)
 	{	
 		return tx*(longestLab + longestVal) + 3*gx;
 	}
 	
 	/**
 	 * Work out the pixel height of an array of values and keys
 	 * @param listLen
 	 * @return
 	 */
 	private int ySize(int listLen)
 	{	
 		return (gy + ty)*listLen + gy;
 	}
 	
 	/**
 	 * Work out the widest width of all the lists
 	 * @return
 	 */
 	private int xAll()
 	{
 		int result = xSize(longestGlobal, longestGlobalVal);
 		for(int i = 0; i < extruderCount; i++)
 		{
 			int x = xSize(longestExtruders[i], longestExtruderVals[i]);
 			if(x > result) 
 				result = x;
 		}
 		return result;
 	}
 	
 	/**
 	 * Work out the heighest height of all the lists
 	 * @return
 	 */
 	private int yAll()
 	{
 		int result = ySize(1 + globals.length/columns);
 		for(int i = 0; i < extruderCount; i++)
 		{
 			int y = ySize(1 + extruders[i].length/columns);
 			if(y > result) 
 				result = y;
 		}		
 		return result;
 	}
 	
 	/**
 	 * Take an array of strings and turn them into labels (right justified).
 	 * @param a
 	 * @return
 	 */
 	private JLabel[] makeLabels(String[] a)
 	{
 		JLabel[] result = new JLabel[a.length];
 		for(int i = 0; i < a.length; i++)
 		{
 			result[i] = new JLabel();
 			result[i].setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
 			result[i].setText(a[i]);
 		}
 		return result;
 	}	
 	
 	/**
 	 * Take an array of labels and use their string values as keys to look up
 	 * the corresponding values.  Make those into an array of editable boxes.
 	 * @param a
 	 * @return
 	 */
 	private JTextField[] makeValues(JLabel[] a)
 	{
 		JTextField[] result = new JTextField[a.length];
 		for(int i = 0; i < a.length; i++)
 		{
 			try{
 				result[i] = new JTextField();
 				result[i].setText(loadString(a[i].getText()));
 			} catch (Exception ex)
 			{
 				ex.printStackTrace();
 			}
 		}
 		return result;
 	}
 	
 	/**
 	 * Is a string saying a boolean?
 	 * @param s
 	 * @return
 	 */
 	private boolean isBoolean(String s)
 	{
 		if(s.equalsIgnoreCase("true"))
 			return true;
 		if(s.equalsIgnoreCase("false"))
 			return true;
 		return false;
 	}
 	
 	/**
 	 * Is a string a number (int or double)?
 	 * 
 	 * There must be a better way to do this; also this doesn't allow
 	 * for 1.3e-5...
 	 * 
 	 * @param s
 	 * @return
 	 */
 	private boolean isNumber(String s)
 	{
 		// Bulletproofing.
 		if ((s==null)||(s.length()==0)) return false;
 		
 		int start = 0;
 		
 		while(Character.isSpaceChar(s.charAt(start)))
 			start++;
 		
 		if(s.charAt(start) == '-' || s.charAt(start) == '+')
 			start++;
 		
 		// Last we checked, only one decimal point allowed per number.
 		int dotCount = 0;
 		for(int i = start; i < s.length(); i++)
 		{
 			char c = s.charAt(i);
 			if(!Character.isDigit(c))
 			{
 				 if(c != '.')
 					return false;
 				 else
 				 {
 					 dotCount++;
 					 if(dotCount > 1)
 						 return false;
 				 }
 			}
 		}
 		return true;
 	}
 	
 	/**
 	 * Find if a string is a boolean, a number, or a string
 	 * @param s
 	 * @return
 	 */
 	private Category category(String s)
 	{
 		if(isBoolean(s))
 			return Category.bool;
 		
 		if(isNumber(s))
 			return Category.number;		
 		
 		return Category.string;
 	}
 	
 	/**
 	 * Generate an array of categories corresponsing to the text in 
 	 * an array of edit boxes so they can be checked later.
 	 * @param a
 	 * @return
 	 */
 	private Category[] categorise(JTextField[] a)
 	{
 		Category[] result = new Category[a.length];
 		for(int i = 0; i < a.length; i++)
 			result[i] = category(a[i].getText());
 		
 		return result;
 	}
 }
