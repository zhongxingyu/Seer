 package com.hifiremote.jp1;
 
 import javax.swing.JComponent;
 import javax.swing.JComboBox;
 
 public class ChoiceDeviceParm
   extends DeviceParameter
 {
   public ChoiceDeviceParm( String name, DefaultValue defaultValue, String[] choices )
   {
     super( name, defaultValue );
     comboBox = new JComboBox( choices );
    // JSF28may03 Questionable design decision: ChoiceDeviceParm doesn't dynamically correct default in ToolTip (compare vs. NumberDeviceParm)
     // JSF28may03 Questionable design decision: DeviceParameter always has non null defaultValue
     String helpText = "Select a value from the list.  The default value is "
      + choices[ ((Integer)getDefaultValue()).intValue() + 1 ] + '.';
     comboBox.setToolTipText( helpText );
   }
 
   public JComponent getComponent(){ return comboBox; }
   public Object getValue()
   {
     Object rc = null;
     int index = comboBox.getSelectedIndex();
     if ( index != 0 )
       rc = new Integer( index - 1 );
     return rc;
   }
 
   public void setValue( Object val )
   {
     int index = 0;
     if ( val != null )
     {
       Class c = val.getClass();
       if ( c == Integer.class )
        comboBox.setSelectedIndex((( Integer )val ).intValue() + 1 );
       else if ( c == String.class )
         comboBox.setSelectedItem( val );
     }
     else
       comboBox.setSelectedIndex( index );
   }
 
   private JComboBox comboBox = null;
 }
