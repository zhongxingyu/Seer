 /**
  * 
  */
 package ecologylab.appframework.types.prefs;
 
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.Rectangle;
 import java.util.HashMap;
 
 import javax.swing.Icon;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JTextField;
 import javax.swing.ButtonGroup;
 import javax.swing.SwingConstants;
 
 import ecologylab.appframework.ObjectRegistry;
 import ecologylab.xml.ElementState;
 import ecologylab.xml.xml_inherit;
 import ecologylab.xml.types.element.ArrayListState;
 
 /**
  * Metadata about a Preference.
  * Defines information to enable editing the Preference.
  * 
  * @author andruid
  *
  */
 
 @xml_inherit
 public abstract class MetaPref<T> extends ElementState
 {
 	/**
 	 * Unique identifier for Preference name with convenient lookup in automatically generated HashMap.
 	 */
 	@xml_attribute 	String		id;
 	
 	/**
 	 * This is the short text that appears in the Swing panel for editing the value.
 	 */
 	@xml_attribute 	String		description;
 	
 	/**
 	 * This is longer text about that describes what the Preference does and more about the implications
 	 * of its value.
 	 */
 	@xml_attribute 	String		helpText;
 	
 	/**
 	 * Type of graphical user interface component used to interact with it.
 	 * Must be a constant defined in the interface WidgetTypes
 	 * If this value is left as null, it should default to TEXT_FIELD.
 	 */
 	@xml_attribute	String		widget;
 	
 	/**
 	 * Categories enable tabbed panes of preferences to be edited.
 	 */
 	@xml_attribute 	String		category;
     
     // have to call getWidget() for each panel;
     // if we try to do so here, everything ends up null
     public JPanel               jPanel;
     
     ObjectRegistry<JComponent>  jComponentsMap;
     
    @xml_nested ArrayListState<Choice<T>> choices = null;
 	
 //	@xml_attribute	T			defaultValue;
 	
 //	@xml_nested		RangeState<T>	range;
 	/**
 	 * 
 	 */
 	public MetaPref()
 	{
 		super();
 	}
 	
 	public abstract T getDefaultValue();
 
     public String getCategory()
     {
         return category;
     }
     
     public String getID()
     {
         return id;
     }
 
     public abstract JPanel getWidget();
     
     public void print()
     {
         println(this.id + '\n' +
                 this.description + '\n' +
                 this.category + '\n' +
                 this.helpText + '\n' +
                 this.widget);
         println("" + this.getDefaultValue());
         if (choices != null)
         {
             for (Choice choice : choices)
             {
                 println("Choice: " + choice.name + ", " + choice.label);
             }
         }
         println("\n");
     }
 
     public boolean widgetIsRadio()
     {
         if ("RADIO".equals(widget))
             return true;
         return false;
     }
     
     public boolean widgetIsTextField()
     {
         if ("TEXT_FIELD".equals(widget))
             return true;
         return false;
     }
 	
 /*
 	public boolean isWithinRange(T newValue)
 	{
 		return (range == null) ? true :  range.isWithinRange(newValue);
 	}
 	*/
     
     public abstract void revertToDefault();
     public abstract void setWidgetToPrefValue(T prefValue);
     
     private ObjectRegistry<JComponent> jComponentsMap()
     {
         ObjectRegistry<JComponent> result   = this.jComponentsMap;
         if (result == null)
         {
             result                          = new ObjectRegistry<JComponent>();
             this.jComponentsMap             = result;
         }
         return result;
     }
 
     protected void registerComponent(String labelAndName, JComponent jComponent)
     {
         //println("Registering: " + this.id+labelAndName);
         jComponentsMap().registerObject(this.id+labelAndName,jComponent);
     }
     
     protected JComponent lookupComponent(String labelAndName)
     {
         //println("Trying to fetch: " + labelAndName);
         JComponent jComponent = jComponentsMap().lookupObject(labelAndName);
         return jComponent;
     }
     
     protected JRadioButton createRadio(JPanel panel, ButtonGroup buttonGroup, boolean initialValue, String label, String name, int x)
     {
         JRadioButton radioButton = new JRadioButton();
         // get font metrics info so we can properly determine length
         FontMetrics fontMetrics = panel.getFontMetrics(radioButton.getFont());
         // get actual width of string (as per gui, not as number of chars)
         double strWidth = fontMetrics.getStringBounds(label, panel.getGraphics()).getWidth();
         // also add width of icon TODO guessed to be 30 because icon.getIconWidth is not working
         radioButton.setBounds(new Rectangle(x, 7, (int)strWidth+30, 32));
         radioButton.setSelected(initialValue);
         radioButton.setName(name);
         radioButton.setText(label);
         
         buttonGroup.add(radioButton);
         
         panel.add(radioButton);
         registerComponent(name, radioButton);
         
         return radioButton;
     }
     
     protected JRadioButton createRadio(JPanel panel, ButtonGroup buttonGroup, boolean initialValue, String label, String name, int x, int y)
     {
         // get font metrics info so we can properly determine length
         JRadioButton radioButton = new JRadioButton();
         FontMetrics fontMetrics = panel.getFontMetrics(radioButton.getFont());
         // get string width
         double strWidth = fontMetrics.getStringBounds(label, panel.getGraphics()).getWidth();
         // also add width of icon TODO guessed to be 30 because icon.getIconWidth is not working
         radioButton.setBounds(new Rectangle(x, y, (int)strWidth + 30, 32));
         radioButton.setSelected(initialValue);
         radioButton.setName(name);
         radioButton.setText(label);
         
         buttonGroup.add(radioButton);
         
         panel.add(radioButton);
         registerComponent(name, radioButton);
         
         return radioButton;
     }
     
     protected JTextField createTextField(JPanel panel, String initialValue, String labelAndName)
     {
         JTextField textField = new JTextField();
         textField.setBounds(new Rectangle(410, 17, 115, 20));
         textField.setHorizontalAlignment(JTextField.CENTER);
         textField.setText(initialValue);
         textField.setName(labelAndName);
         
         panel.add(textField);
         registerComponent(labelAndName, textField);
         
         return textField;
     }
     
     protected JLabel createLabel(JPanel panel)
     {
         JLabel label = new JLabel();
         label.setBounds(new Rectangle(0, 10, 340, 32));
         String wrapText = "<html>" + this.description + "</html>";
         label.setText(wrapText);
         label.setToolTipText(this.helpText);
         label.setHorizontalTextPosition(SwingConstants.LEADING);
         
         panel.add(label);
         
         return label;
     }
 
     public abstract T getPrefValue();
 }
