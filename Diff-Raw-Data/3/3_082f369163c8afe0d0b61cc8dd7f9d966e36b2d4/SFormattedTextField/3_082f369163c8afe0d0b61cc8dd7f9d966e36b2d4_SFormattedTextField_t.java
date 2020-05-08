 /*
  * SFormattedTextField.java
  *
  * Created on 9. September 2003, 09:05
  */
 
 /*
  * Copyright 2000,2005 wingS development team.
  *
  * This file is part of wingS (http://wingsframework.org).
  *
  * wingS is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  *
  * Please see COPYING for the complete licence.
  */
 package org.wings;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import java.text.ParseException;
 import java.text.*;
 import org.wings.text.SDefaultFormatterFactory;
 import org.wings.text.SDateFormatter;
 import org.wings.text.SInternationalFormatter;
 import org.wings.text.SNumberFormatter;
 import java.util.Date;
 import org.wings.plaf.TextFieldCG;
 import org.wings.text.SAbstractFormatter;
 import org.wings.text.SDefaultFormatter;
 
 
 /**
  * Formats it content interactivly on the server side via DWR/AJAX.
  *
  * @author theresia
  */
 public class SFormattedTextField extends STextField {
 
     private final static Log log = LogFactory.getLog(SFormattedTextField.class);
 
     public final static int COMMIT = 0;
     public final static int COMMIT_OR_REVERT = 1;
 
     private int focusLostBehavior = COMMIT_OR_REVERT;
 
     /* The last valid value */
     private Object value = null;
 
     private SAbstractFormatter formatter = null;
 
     private SAbstractFormatterFactory factory = null;
 
     /**
      * Creates a SFormattedTextField
      */
     public SFormattedTextField() {
     }
 
     /**
      * Creates a SFormattedTextField with the given value
      * @param value
      */
     public SFormattedTextField( Object value ) {
         setValue( value );
     }
 
     /**
      * Creates a SFormattedTextField with the given SAbstractFormatter
      * @param formatter SAbstractFormatter
      */
     public SFormattedTextField( SAbstractFormatter formatter ) {
         setFormatter(formatter);
     }
 
     /**
      * Creates a SFormattedTextField with the given AbstractFormatterFactory
      * @param factory SAbstractFormatterFactory
      */
     public SFormattedTextField( SAbstractFormatterFactory factory ) {
         setFormatterFactory( factory );
     }
 
     /**
      * Sets the value
      * @param object value
      */
     public void setValue(Object object) {
         String string = null;
 
         try {
             string = getFormatter().valueToString(object);
             this.value = object;
             putClientProperty("lastValid", string );
         } catch (ParseException e) {
             log.info("Unable to parse object" + e);
         }
 
         super.setText( string );
 
     }
 
     /**
      * Returns the last valid value
      * @return the last valid value
      */
     public Object getValue() {
         Object returnValue;
 
         try {
             returnValue = getFormatter().stringToValue(this.getText());
             value = returnValue;
         } catch (ParseException e) {
             log.debug("Unable to parse string" + e);
             returnValue = value;
         }
 
         return returnValue;
     }
     public void processLowLevelEvent(String action, String[] values) {
         processKeyEvents(values);
         if (action.endsWith("_keystroke"))
             return;
 
         String orgText = getText() == null ? "" : getText();
         String newText = "";
         if (isEditable() && isEnabled()) {
             if ( !orgText.equals(values[0]) ) {
                 try {
                     SAbstractFormatter formatter = getFormatter();
                     newText = formatter.valueToString(formatter.stringToValue(values[0]));
                 } catch (ParseException e) {
                     switch( getFocusLostBehavior() ) {
                         case COMMIT_OR_REVERT :
                             newText = orgText;
                             break;
                         case COMMIT :
                             newText = values[0];
                             break;
                     }
                 }
                getDocument().setDelayEvents(true); 
                 setText(newText);
                getDocument().setDelayEvents(false);
                 if (newText == null)
                     newText = "";
                 if ( !newText.equals(values[0]) && orgText.equals( newText ) ) {
                     update( ((TextFieldCG)getCG()).getTextUpdate( this, getText() ) );
                 }
                 SForm.addArmedComponent(this);
                 
             }
         }
     }
 
     public boolean isEditValid() {
         boolean isEditValid = true;
         try {
             getFormatter().valueToString( getFormatter().stringToValue(getText()) );
         } catch ( ParseException e ) {
             isEditValid = false;
         }
         return isEditValid;
     }
 
     /**
      * Sets the focus lost behavior
      * <code>COMMIT</code>
      * <code>COMMIT_OR_REVERT</code>
      * @param behavior focus lost behavior
      */
     public void setFocusLostBehavior( int behavior ) {
         if ( behavior != COMMIT && behavior != COMMIT_OR_REVERT ) {
             throw new IllegalArgumentException("i don't know your behavior");
         }
         focusLostBehavior = behavior;
     }
 
     /**
      * Returns the focus lost behavior
      * @return focus lost behavior
      */
     public int getFocusLostBehavior () {
         return this.focusLostBehavior;
     }
 
     /**
      * Returns the SAbstractFormatter
      * @return SAbstractFormatter
      */
     public SAbstractFormatter getFormatter() {
         SAbstractFormatter formatter = this.formatter;
         if (formatter == null) {
             SAbstractFormatterFactory aff = getFormatterFactory();
             if ( aff == null ) {
                 aff = getDefaultFormatterFactory( value );
             }
             formatter = aff.getFormatter(this);
         }
         return formatter;
     }
 
     /**
      * Sets the SAbstractFormatter
      * @param formatter SAbstactFormatter
      */
     public void setFormatter(SAbstractFormatter formatter) {
         this.formatter = formatter;
     }
 
     /**
      * Sets the FormatterFactory
      * @param ff AbstractFormatterFactory
      */
     public void setFormatterFactory ( SAbstractFormatterFactory ff ) {
         this.factory = ff;
         this.formatter = null;
         setValue( value );
         
     }
     
     /**
      * Returns the FormatterFactory
      * @return SAbstractFormatterFactory
      */
     public SAbstractFormatterFactory getFormatterFactory () {
         return this.factory;
     }
     
     private SAbstractFormatterFactory getDefaultFormatterFactory(Object type) {
         
         SAbstractFormatterFactory factory = null;
         
         if (type instanceof DateFormat) {
             factory = new SDefaultFormatterFactory(new SDateFormatter( (DateFormat)type ) );
         }
         if (type instanceof NumberFormat) {
             factory = new SDefaultFormatterFactory(new SNumberFormatter( (NumberFormat)type ) );
         }
         if (type instanceof Format) {
             factory = new SDefaultFormatterFactory(new SInternationalFormatter( (Format)type ) );
         }
         if (type instanceof Date) {
             factory = new SDefaultFormatterFactory(new SDateFormatter( ) );
         }
         if (type instanceof Number) {
             SAbstractFormatter displayFormatter = new SNumberFormatter();
             SAbstractFormatter editFormatter = new SNumberFormatter( new DecimalFormat( "#.#" ) );
 
             factory = new SDefaultFormatterFactory( displayFormatter, displayFormatter, editFormatter );
         }
         if ( factory == null ) {
             factory = new SDefaultFormatterFactory(new SDefaultFormatter());
         }
         
         return factory;
         
     }
 
     
     public static abstract class SAbstractFormatterFactory {
         /**
          * Returns an AbstractFormatter
          *
          * @return AbstractFormatter
          * @param ftf SFormattedTextField
          */
         public abstract SAbstractFormatter getFormatter(SFormattedTextField ftf);
     }
     
 }
