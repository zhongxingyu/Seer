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
     super( name, defaultValue );
     this.bits = bits;
     this.min = 0;
     this.max = (( 1 << bits ) - 1 );
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
     if (( text == null ) || ( text.length() == 0 ))
       return null;
     Integer rc = Integer.valueOf( text, base );
     return rc;
   }
 
   public void setValue( Object value )
   {
     if ( value == null )
       tf.setText( "" );
     else
     {
       String temp = null;
      Class aClass = value.getClass();
      if (( aClass == Integer.class ) && base != 10 )
         temp = Integer.toHexString((( Integer )value ).intValue());
      else 
        temp = value.toString();
       tf.setText( temp );
     }
   }
 
   public String toString()
   {
     StringBuffer buff = new StringBuffer();
     buff.append( name );
     if (( base == 6 ) || ( bits != 8 ))
       buff.append( ':' );
     if ( base == 16 )
       buff.append( '$' );
     if ( bits != 8 )
       buff.append( bits );
     if ( defaultValue != null )
     {
       buff.append( '=' );
       buff.append( defaultValue );
     }
     return buff.toString();
   }
 
   private JTextField tf = null;
   private int bits = 8;
   private int min;
   private int max;
   private int base = 10;
   private IntVerifier verifier = null;
 }
