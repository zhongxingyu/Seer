 /**
  * 
  */
 package ecologylab.appframework.types.prefs;
 
 import javax.swing.ButtonGroup;
 import javax.swing.ButtonModel;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JTextField;
 
 import ecologylab.xml.xml_inherit;
 
 /**
  * Metadata about an Integer Preference.
  * Defines information to enable editing the Preference.
  * 
  * @author andruid
  *
  */
 @xml_inherit
 public class MetaPrefInt extends MetaPref<Integer>
 {
 	@xml_attribute	int		defaultValue;
 	
 	/**
 	 * 
 	 */
 	public MetaPrefInt()
 	{
 		super();
 	}
 	
     public Integer getDefaultValue()
 	{
 		return defaultValue;
 	}
 
     public @Override
     void revertToDefault()
     {
         if (widgetIsTextField())
         {
             JTextField textField = (JTextField)lookupComponent(this.id+"textField");
             textField.setText(this.getDefaultValue().toString());
         }
         else if (widgetIsRadio())
         {
             /* This is an ugly way to do this, but we can't trust
              * choices to be in the right order (0-n), and we can't
              * trust the choice values to be number 0-n either.
              * We also can't get the index without the object.
              */
             // get default choice
             Integer defValue = this.getDefaultValue();
             for(Choice choice1 : choices)
             {
                 if (defValue.equals(choice1.getValue()))
                 {
                     // registered name
                     String regName = this.id + choice1.name;
                     //println("we think the name is: " + regName);
                     JRadioButton defaultButton = (JRadioButton)lookupComponent(regName);
                     ButtonModel buttonModel = defaultButton.getModel();
                     buttonModel.setSelected(true);
                     break;
                 }
             }
         }
     }
 
     public @Override
     JPanel getWidget()
     {
         JPanel panel = new JPanel();
         panel.setName(this.id);
         
         this.createLabel(panel);
         if (widgetIsTextField())
         {
             this.createTextField(panel,this.getDefaultValue().toString(),"textField");
         }
         else if (widgetIsRadio())
         {
             // we know here that if we are a radio, we are a mutex of 3 or more
             // because otherwise we would be a bool.
             if (choices != null)
             {
                 ButtonGroup buttonGroup = new ButtonGroup();
                 int start_y = 7;
                 for (Choice choice : choices)
                 {
                     boolean isDefault = getDefaultValue().equals(choice.getValue());
                     //println("this is default?: " + thisIsDefault);
                     //println("getDefaultValue: " + getDefaultValue().toString());
                     //println("choice.getValue: " + choice.getValue().toString());
                     this.createRadio(panel, buttonGroup, isDefault, choice.getLabel(), choice.getName(), 405, start_y);
                     start_y += 30;
                 }
             }
         }
         // TODO: drop-down list
         if (choices != null)
         {
             panel.setSize(new java.awt.Dimension(586,35*choices.size()));
         }
         else
         {
             panel.setSize(new java.awt.Dimension(586,35));
         }
         panel.setLayout(null);
         panel.setVisible(true);
         
         return panel;
     }
 
     @Override
     public void setWidgetToPrefValue(Integer prefValue)
     {
         if (widgetIsTextField())
         {
             JTextField textField = (JTextField)lookupComponent(this.id+"textField");
             textField.setText(prefValue.toString());
         }
         else if (widgetIsRadio())
         {
             /* This is an ugly way to do this, but we can't trust
              * choices to be in the right order (0-n), and we can't
              * trust the choice values to be number 0-n either.
              * We also can't get the index without the object.
              */
             // find default choice
            Integer defValue = this.getDefaultValue();
             for(Choice choice1 : choices)
             {
                 if (defValue.equals(choice1.getValue()))
                 {
                     // registered name
                     String regName = this.id + choice1.name;
                     //println("we think the name is: " + regName);
                     JRadioButton defaultButton = (JRadioButton) lookupComponent(regName);
                     ButtonModel buttonModel = defaultButton.getModel();
                     buttonModel.setSelected(true);
                     break;
                 }
             }
         }
     }
 
     @Override
     public Integer getPrefValue()
     {
         if (widgetIsTextField())
         {
             JTextField textField = (JTextField)lookupComponent(this.id+"textField");
             return new Integer(textField.getText());
         }
         else if (widgetIsRadio())
         {
             // find the selected one and return it
             for (Choice choice: choices)
             {
                 String regName = this.id + choice.name;
                 JRadioButton choiceButton = (JRadioButton) lookupComponent(regName);
                 if (choiceButton.isSelected())
                 {
                     return (Integer)choice.getValue();
                 }
             }
         }
         // if by some miracle we managed to deselect something that
         // automatically has a selected value, return the default value
         return (Integer)this.getDefaultValue();
     }
 	
 /*
 	public boolean isWithinRange(Integer newValue)
 	{
 		return (range == null) ? true :  range.isWithinRange(newValue);
 	}
 	*/
 }
