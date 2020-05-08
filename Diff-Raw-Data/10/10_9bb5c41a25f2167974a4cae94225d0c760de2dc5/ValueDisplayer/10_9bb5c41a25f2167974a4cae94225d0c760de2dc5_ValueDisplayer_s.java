 //----------------------------------------------------------------------------
 // $Revision$
 // $Date$
 // $Author$
 //----------------------------------------------------------------------------
 package cytoscape.visual;
 //----------------------------------------------------------------------------
 import javax.swing.JButton;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.text.*;
 import javax.swing.event.*;
 import java.util.HashMap;
 import y.view.*;
 
 import cytoscape.visual.ui.*;
 //----------------------------------------------------------------------------
 /**
  * Given an Object, figures out the class of the object and creates a JButton
  * suitable for displaying the value of that object. When the button is
  * pressed, it should pop up a user interface to change the value.
  *
  * The class interested in the selection of the ValueDisplayer should add
  * an ItemListener to the button. The ItemListener is triggered when the user's
  * selection changes.
  */
 public class ValueDisplayer extends JButton {
     /**
      *  Formatting for numeric types.
      */
     public static DecimalFormat formatter = new DecimalFormat("0.0####");    
 
     /**
      *	Display and get input for a color
      */
     public static final byte COLOR = 0;
     
     /**
      *	Display and get input for a linetype
      */
     public static final byte LINETYPE = 1;
 
     /**
      *	Display and get input for an arrowhead
      */
     public static final byte ARROW = 2;
     
     /**
      *	Display and get input for a string
      */
     public static final byte STRING = 3;
 
     /**
      *	Display and get input for a double
      */
     public static final byte DOUBLE = 4;
 
     /**
      *	Display and get input for node shape
      */
     public static final byte NODESHAPE = 5;
 
     /**
      *	Display and get input for an int
      */
     public static final byte INT = 6;
 
     /**
      *  Display and get input for a font
      */
     public static final byte FONT = 7;
 
     /**
      *	Holds the type of UI this ValueDisplayer will pop up.
      */
     protected byte dispType;
 
     /**
      *	Holds the object inputted by the user.
      */
     private Object inputObj = null;
 
     /**
      *	Input dialog title
      */
     private String title;
 
     /**
      *	Parent dialog
      */
     private JDialog parent;
 
     /**
      *	ActionListener that triggers input UI
      */
     private ActionListener inputListener;
 
     /**
      *	Enable/disable mouse listeners
      */
     private boolean enabled;
 
     /**
      *	Provided for convenience.
      *  @see #getValue
      *  @return User-selected object displayed by this ValueDisplayer
      */
     public Object getSelectedItem() {
 	return this.getValue();
     }
 
     /**
      *	Returns an object representing the user input. The return value is always
      *	an object type. It may be a String, Number, Arrow, LineType, or Byte
      *	depending on what type the ValueDisplayer was initialized with.
      *
      *  @return User-selected object displayed by this ValueDisplayer
      */
     public Object getValue() {
 	return inputObj;
     }
 
     /**
      *	Returns the ActionListener that will pop up the input UI when triggered.
      *	Attach this to a component to trigger input on a click.
      */
     public ActionListener getInputListener() {
 	return inputListener;
     }
     
     /**
      *	Returns the type of input this ValueDisplayer displays/gets input for
      */
     public byte getType() {
 	return dispType;
     }
 
     /**
      *  Set the ValueDisplayer active/inactive.
      *
      *  @param b true to enable, false to disable
      */
     public void setEnabled(boolean b) {
 	this.enabled = b;
 	super.setEnabled(b);
     }
     
     /**
      *	This private constructor is used to create all ValueDisplayers.
      *	Use the static method getDisplayFor (@link #getDisplayFor) to
      *	get a new ValueDisplayer.
      */
     private ValueDisplayer(JDialog parent, String labelText, String title,
 			   byte dispType) {
 	super(labelText);
 	setBorderPainted(false);
 
 	this.parent = parent;
 	this.dispType = dispType;
 	this.title = title;
     }
     private ValueDisplayer(JDialog parent, String title, byte dispType) {
 	// can't find proper icon/label until later, so set label to null for now
 	this(parent, null, title, dispType);
     }
     
     public static ValueDisplayer getDisplayForColor(JDialog parent, String title,
 						    Color c) {
 	String dispString = "   "; // just display the color
         ValueDisplayer v = new ValueDisplayer(parent, dispString, title, COLOR);
 	if (c != null) {
 	    v.setOpaque(true);
 	    v.setBackground(c);
 	    v.inputObj = c;
 	}
 	v.setInputColorListener();
 	return v;
     }
 
     private void setInputColorListener() {
 	this.inputListener = new ColorListener(this);
 	addActionListener(this.inputListener);
     }
 
     /**
      * This method fires the itemListeners. Item listeners are notified only when
      * a new selection of the underlying value of the ValueDisplayer is made.
      *
      * Typically this should only be called by listeners that underlie the
      * internal structure of the ValueDisplayer
      */
     protected void fireItemSelected() {
 	this.fireItemStateChanged(new ItemEvent(this,
 						ItemEvent.ITEM_STATE_CHANGED,
 						inputObj,
 						ItemEvent.SELECTED));
     }
 
     /**
      * Set the object displayed. Ensure that the class is the same.
      * Fires an itemSelected event.
      *
      * @throws ClassCastException if caller attempts to set an object
      *	   different from what was being represented.
      */
     public void setObject(Object o) throws ClassCastException {
 	inputObj = o;
 	if (o instanceof Icon) {
 	    setIcon((Icon) o);
 	}
 	else if (o instanceof Color) {
 	    setBackground((Color) o);
 	}
 	else if (o instanceof Font) {
 	    Font f = (Font) o;
 	    setFont(f);
 	    setText(f.getFontName());
 	}
 	else { // anything else must be a Double, Integer, or String
 	    setText(o.toString());
 	}
 	fireItemSelected();
     }
 
     // internal class ColorListener
     private class ColorListener extends AbstractAction {
 	ValueDisplayer parent;
 
 	ColorListener (ValueDisplayer parent) {
 	    super("ValueDisplayer ColorListener");
 	    this.parent = parent;
 	}
 
 	public void actionPerformed(ActionEvent e) {
 	    if (enabled) {
 		Color tempColor = JColorChooser.showDialog(parent.parent,
 							   parent.title,
 							   (Color) parent.inputObj);
 		if (tempColor != null) {
 		    parent.inputObj = tempColor;
 		    parent.setBackground(tempColor);
 		    parent.fireItemSelected();
 		}
 	    }
 	}
     }
 
     private static ValueDisplayer getDisplayForFont(JDialog parent, String title,
 						    Font startFont) {
 	ValueDisplayer v = new ValueDisplayer(parent, title, FONT);
 	v.setSelectedFont(startFont);
 	v.setInputFontListener();
 	return v;
     }
 
     private void setSelectedFont(Font f) {
 	this.inputObj = f;
 	String dispFontName = f.getFontName();
 	setFont(f.deriveFont(12F));
 	setText(dispFontName);
     }
     
     private void setInputFontListener() {
 	this.inputListener = new FontListener(this);
 	addActionListener(this.inputListener);
     }
     // internal class FontListener
     private class FontListener extends AbstractAction {
 	ValueDisplayer parent;
 	JDialog popup;
 
 	FontListener (ValueDisplayer parent) {
 	    super("ValueDisplayer FontListener");
 	    this.parent = parent;
 	}
 	
 	public void actionPerformed(ActionEvent e) {
 	    if (enabled) {
 		FontChooser chooser = new FontChooser(((Font) inputObj).deriveFont(1F));
 		this.popup = new JDialog(parent.parent,
 					    parent.title,
 					    true);
 		JComboBox face = chooser.getFaceComboBox();
 		face.setSelectedItem(parent.inputObj);
 		
 		JPanel butPanel = new JPanel(false);
 		
 		// buttons - OK/Cancel
 		JButton okBut = new JButton("OK");
 		okBut.addActionListener(new OKListener(chooser));
 
 		JButton cancelBut = new JButton("Cancel");
 		cancelBut.addActionListener(new CancelListener());
 
 		butPanel.add(okBut);
 		butPanel.add(cancelBut);
 
 		Container content = popup.getContentPane();
 		content.setLayout(new BorderLayout());
 		
 		content.add(chooser, BorderLayout.CENTER);
 		content.add(butPanel, BorderLayout.SOUTH);
 		popup.pack();
 		popup.show();
 	    }
 	}
 
 	private class OKListener extends AbstractAction {
 	    FontChooser chooser;
 	    private OKListener (FontChooser chooser) {
 		this.chooser = chooser;
 	    }
 	    public void actionPerformed(ActionEvent e) {
 		setSelectedFont(chooser.getSelectedFont().deriveFont(12F));
 		//System.out.println("Set selected font to " + inputObj);
 		fireItemSelected();
 		popup.dispose();
 	    }
 	}
 
 	private class CancelListener extends AbstractAction {
 	    private CancelListener() {};
 
 	    public void actionPerformed(ActionEvent e) {
 		popup.dispose();
 	    }
 	}
     }
 
     private static ValueDisplayer getDisplayForIcons(JDialog parent,
 						     String title,
 						     Object startObj,
 						     byte type) {
 	ValueDisplayer v = new ValueDisplayer(parent, title, type);
 	// has to be done this way because of static call
 	v.setInputIconListener(title, title, startObj, parent, type);
 	return v;
     }
 
     private void setInputIconListener(String title, String objectName,
 				      Object startObject,
 				      JDialog parentDialog, byte type) {
 	// set up button to display icon only
 	this.setContentAreaFilled(false);
 
 	// get icons - cannot be done from a static context
 	ImageIcon[] icons = null;
 	HashMap iToS = null;
 	HashMap sToI = null;
 	switch (type) {
 	case ARROW:
 	    icons = MiscDialog.getArrowIcons();
 	    iToS = MiscDialog.getArrowToStringHashMap(25);
 	    sToI = MiscDialog.getStringToArrowHashMap(25);
 	    break;
 	case NODESHAPE:
 	    icons = MiscDialog.getShapeIcons();
 	    iToS = MiscDialog.getShapeByteToStringHashMap();
 	    sToI = MiscDialog.getStringToShapeByteHashMap();
 	    break;
 	case LINETYPE:
 	    icons = MiscDialog.getLineTypeIcons();
 	    iToS = MiscDialog.getLineTypeToStringHashMap();
 	    sToI = MiscDialog.getStringToLineTypeHashMap();
 	    break;
 	}
 
 	ImageIcon currentIcon;
 	if (startObject == null) {
 	    currentIcon = icons[0];
 	}
 	else {
 	    // find the right icon
 	    String ltName = (String)iToS.get(startObject);
 	    int iconIndex = 0;
 	    for ( ; icons[iconIndex].getDescription() != ltName; iconIndex++) {
 	    }
 	    currentIcon = icons[iconIndex];
	    if (iconIndex == icons.length) { // didn't find the icon
		System.err.println("Icon for object " + startObject + " not found!");
		currentIcon = icons[0];
	    }
 	}
 	// set currentIcon
 	this.setIcon(currentIcon);
 	this.inputObj = sToI.get(currentIcon.getDescription());
 
 	this.inputListener = new IconListener(title, objectName, icons, sToI,
 					      currentIcon, parentDialog, this);
 	addActionListener(this.inputListener);
     }
 
     
     // internal class IconListener. Calls PopupIconChooser to get an icon from
     // the user.
     private class IconListener extends AbstractAction {
 	private PopupIconChooser chooser;
 	private ValueDisplayer parent;
 	private HashMap sToI; // map from the image icon description to type	
 
 	IconListener(String title, String objectName, ImageIcon[] icons,
 		     HashMap sToI, ImageIcon startIconObject, JDialog parentDialog,
 		     ValueDisplayer parent) {
 	    super("ValueDisplayer IconListener");
 	    this.chooser = new PopupIconChooser(title,
 						objectName,
 						icons,
 						startIconObject,
 						parentDialog);
 	    this.parent = parent;
 	    this.sToI = sToI;
 	}
 	public void actionPerformed(ActionEvent e) {
 	    if (enabled) {
 		ImageIcon icon = chooser.showDialog();
 		if (icon != null) {
 		    // set the new icon to be displayed
 		    parent.setIcon(icon);
 
 		    // convert from ImageIcon description to expected type
 		    // (see MiscDialog)
 		    parent.inputObj = sToI.get(icon.getDescription());
 		    parent.fireItemSelected();
 		}
 	    }
 	}
     }
 
     private void addStringListener(String prompt, byte type) {
 	this.inputListener = new StringListener(prompt, type);
 	addActionListener(this.inputListener);
     }
 	
     private static ValueDisplayer getDisplayForString(JDialog parent,
 						      String title,
 						      String init) {
 	ValueDisplayer v = new ValueDisplayer(parent, init, title, STRING);
 	v.addStringListener("Input a string:", STRING);
 	return v;
     }
 
     private static ValueDisplayer getDisplayForDouble(JDialog parent,
 						      String title,
 						      double init) {
 	ValueDisplayer v = new ValueDisplayer(parent, formatter.format(init), title,
 					      DOUBLE);
 	v.addStringListener("Input a double:", DOUBLE);
 	return v;
     }
 
     private static ValueDisplayer getDisplayForInt(JDialog parent,
 						   String title,
 						   int init) {
 	ValueDisplayer v = new ValueDisplayer(parent, Integer.toString(init),
 					      title, INT);
 	v.addStringListener("Input an integer:", INT);
 	return v;
     }
 
     // StringListener for String, Double, Int types
     private class StringListener extends AbstractAction {
 	private byte type;
 	private String prompt;
 
 	StringListener (String prompt, byte type) {
 	    super("ValueDisplayer StringListener");
 	    this.prompt = prompt;
 	    this.type = type;
 	}
 
 	public void actionPerformed (ActionEvent e) {
 	    if (enabled) {
 		// keep prompting for input until a valid input is received
 		input:
 		while (true) {
 		    String ret = (String) JOptionPane.showInputDialog(parent, prompt, title, JOptionPane.QUESTION_MESSAGE, null, null, inputObj);
 		    if (ret == null) {
 			return;
 		    }
 		    else {
 			switch (type) {
 			case DOUBLE:
 			    try {
 				inputObj = new Double(Double.parseDouble(ret));
 				break input;
 			    }
 			    catch (NumberFormatException exc) {
 				showErrorDialog("That is not a valid double");
 				continue input;
 			    }
 			case INT:
 			    try {
 				inputObj = new Integer(Integer.parseInt(ret));
 				break input;
 			    }
 			    catch (NumberFormatException exc) {
 				showErrorDialog("That is not a valid integer");
 				continue input;
 			    }
 			default: // simple string assignment
 			    inputObj = ret;
 			    break input;
 			}
 		    }
 		}
 		setText(inputObj.toString());
 		fireItemSelected();
 	    }
 	}
     }
 
     private void showErrorDialog(String errorMsg) {
 	JOptionPane.showMessageDialog(parent, errorMsg, "Bad Input",
 				      JOptionPane.ERROR_MESSAGE);
     }
     
     /**
      *	Get a blank or default display/input pair for a given type of input.
      *
      *	@param	parent
      *		The parent dialog for the returned ValueDisplayer
      *	@param	title
      *		Title to display for input dialog
      *	@param	type
      *		Type of input, one of {@link #COLOR}, {@link #LINETYPE},
      *		{@link #NODESHAPE}, {@link #ARROW}, {@link #STRING},
      *		{@link #DOUBLE}, {@link #INT}, {@link FONT}
      *
      *	@return	ValueDisplayer initialized for given input
      *	@throws ClassCastException if you didn't pass in a known type
      */
     public static ValueDisplayer getBlankDisplayFor(JDialog parent, String title,
 						    byte type) {
 	switch (type) {
 	case COLOR:
 	    return getDisplayForColor(parent, title, null);
 	case LINETYPE:
 	    return getDisplayForIcons(parent, title, null, LINETYPE);
 	case NODESHAPE:
 	    return getDisplayForIcons(parent, title,
 				      new Byte(ShapeNodeRealizer.ELLIPSE),
 				      NODESHAPE);
 	case ARROW:
 	    return getDisplayForIcons(parent, title, Arrow.STANDARD, ARROW);
 	case STRING:
 	    return getDisplayForString(parent, title, null);
 	case DOUBLE:
 	    return getDisplayForDouble(parent, title, 0);
 	case INT:
 	    return getDisplayForInt(parent, title, 0);
 	case FONT:
 	    return getDisplayForFont(parent, title, new Font(null, Font.PLAIN, 1));
 	default:
 	    throw new ClassCastException("ValueDisplayer didn't understand type flag " + type);
 	}
     }
 
     /**
      *	Get a display/input pair initialized to a given type of input. If sending
      *	fonts, must send fonts as gotten from {@link GraphicsEnvrionment#getAllFonts}
      *
      *	@param	parent
      *		The parent dialog for the returned ValueDisplayer
      *	@param	o
      *		Object to represent. Should be a {@link java.awt.Color Color},
      *		{@link y.view.LineType LineType}, node shape (byte), arrow,
      *		string, or number
      *	@return	ValueDisplayer displaying the given object and accepting
      *		input for given object
      *	@throws	ClassCastException if you didn't pass in a known type
      */
     public static ValueDisplayer getDisplayFor(JDialog parent, String title,
 					       Object o) throws ClassCastException {
         if (o instanceof Color) {
             return getDisplayForColor(parent, title, (Color)o );
         } else if (o instanceof LineType) {
             return getDisplayForIcons(parent, title, o, LINETYPE);
         } else if (o instanceof Byte) {
             return getDisplayForIcons(parent, title, o, NODESHAPE);
         } else if (o instanceof Arrow) {
             return getDisplayForIcons(parent, title, o, ARROW);
         } else if (o instanceof String) {
             return getDisplayForString(parent, title, (String)o );
         } else if (o instanceof Number) {
             if ( o instanceof Float || o instanceof Double ) {
                 return getDisplayForDouble(parent, title, ((Number)o).doubleValue() );
             } else {
                 return getDisplayForInt(parent, title, ((Number)o).intValue() );
             }	    
         } else if (o instanceof Font) {
 	    return getDisplayForFont(parent, title, (Font) o);
 	} else {//don't know what to do this this
             throw new ClassCastException("ValueDisplayer doesn't know how to display type " + o.getClass().getName());
         }
     }
 }
