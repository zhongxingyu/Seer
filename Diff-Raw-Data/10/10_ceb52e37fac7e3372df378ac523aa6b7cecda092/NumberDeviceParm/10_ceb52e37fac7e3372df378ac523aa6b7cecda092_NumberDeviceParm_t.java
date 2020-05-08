 package com.hifiremote.jp1;
 
 import java.text.*;
 import javax.swing.*;
 import javax.swing.event.*;
 import java.util.*;
 import java.awt.event.*;
 
 public class NumberDeviceParm
   extends DeviceParameter
 {
   public class JTextFieldDefault
     extends JTextField
   {
     JTextFieldDefault( String str, DefaultValue defaultValue, int base )
     {
       this.defaultValue = defaultValue;
       this.base = base;
       setToolTipText( str );
     }
     public String getToolTipText(MouseEvent event)
     {
       return super.getToolTipText() + Integer.toString((( Integer )defaultValue.value()).intValue(), base ) + '.';
     }
     DefaultValue defaultValue;
     int base;
   }
 
   public NumberDeviceParm( String name, DefaultValue defaultValue, int base )
   {
     this( name, defaultValue, base, 8 );
   }
 
   public NumberDeviceParm( String name, DefaultValue defaultValue, int base, int bits )
   {
     this( name, defaultValue, base, 0, (( 1 << bits ) - 1 ));
   }
 
   public NumberDeviceParm( String name, DefaultValue defaultValue, int base, int min, int max  )
   {
    
     super( name, defaultValue );
     this.min = min;
     this.max = max;
    this.base = base;
     verifier = new IntVerifier( min, max, true );
     verifier.setBase(base);
     // JSF28may03 Questionable design decision: DeviceParameter always has non null defaultValue
     String numType = "";
     if ( base == 16 )
       numType = "hex ";
     String helpText = "Enter a " + numType
       + "number in the range " + min + ".." + max + ".  The default is ";
     tf = new JTextFieldDefault( helpText, defaultValue, base);
     tf.setInputVerifier( verifier );
   }
 
   public JComponent getComponent()
   {
     return tf;
   }
 
   public void addListener( EventListener l )
   {
     tf.addActionListener(( ActionListener )l );
     tf.addFocusListener(( FocusListener)l );
     tf.getDocument().addDocumentListener(( DocumentListener )l );
   }
 
   public void removeListener( EventListener l )
   {
     tf.removeActionListener(( ActionListener )l );
     tf.removeFocusListener(( FocusListener )l );
     tf.getDocument().removeDocumentListener(( DocumentListener )l );
   }
 
   public Object getValue()
   {
     String text = tf.getText();
    System.err.println( "NumberDeviceParm.getValue(): name=" + getName() + ", text=" + text + " and base=" + base );
     if (( text == null ) || ( text.length() == 0 ))
       return null;
    Integer rc = Integer.valueOf( text, base );
    System.err.println( "Returning " + rc );
    return rc;
   }
 
   public void setValue( Object value )
   {
     if ( value == null )
       tf.setText( "" );
     else
     {
       String temp = null;
       if ( base == 10 )
         temp = value.toString();
       else
         temp = Integer.toHexString((( Integer )value ).intValue());
       tf.setText( temp );
     }
   }
 
   private JTextField tf = null;
   private int min;
   private int max;
   private int base = 10;
   private IntVerifier verifier = null;
 }
