 /*******************************************************************************
  * Copyright (c) 2004, 2010 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.rc.swt.driver;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Properties;
 import java.util.StringTokenizer;
 
 import org.eclipse.jubula.rc.common.exception.RobotException;
 import org.eclipse.jubula.rc.common.logger.AutServerLogger;
 import org.eclipse.swt.SWT;
 
 
 /**
  * @author BREDEX GmbH
  * @created 12.07.2007
  */
 public class KeyboardHelper {
     
     /** the logger */
     private static AutServerLogger log = new AutServerLogger(
         KeyboardHelper.class);
 
     /**
      * The keyboard mapping file prefix (keyboardmapping_)
      */
     private static final String KEYBOARD_MAPPING_FILE_PREFIX = 
            "keyboardmapping_"; //$NON-NLS-1$
     
     /**
      * The keyboard mapping file postfix (.properties)
      */
     private static final String KEYBOARD_MAPPING_FILE_POSTFIX = ".properties"; //$NON-NLS-1$
     
         
     /**
      * The delimiter (+).
      */
     private static final String DELIMITER = "+"; //$NON-NLS-1$
     
     /**
      * shift
      */
     private static final String SHIFT = "shift"; //$NON-NLS-1$
     
     /**
      * ctrl
      */
     private static final String CTRL = "ctrl"; //$NON-NLS-1$
     
     /**
      * alt
      */
     private static final String ALT = "alt"; //$NON-NLS-1$
     
     /** 
      * The keyboard mapping.
      * Key = character
      * Value = KeyStroke
      */
     private Map m_mapping = new HashMap();
 
     /** The Locale of this KeyboardHelper */
     private Locale m_locale; 
     
     /**
      * Constructor
      * @param locale the locale
      */
     public KeyboardHelper(Locale locale) {
         m_locale = locale;
         initKeyboardMapping(locale);
     }
     
    
    
     /**
      * Inits keyboard mapping
      * @param locale the Locale
      */
     private void initKeyboardMapping(Locale locale) {
         final String filename = createFileName(locale);
         final InputStream stream = this.getClass().getClassLoader()
             .getResourceAsStream(filename);
         final Properties prop = new Properties();
         try {
             prop.load(stream);
         } catch (IOException e) {
             log.error("Could not read file: " + filename, e); //$NON-NLS-1$
             throw new RobotException(e);
         }
         final Iterator charsIter = prop.keySet().iterator();
         while (charsIter.hasNext()) {
             final String origCharStr = (String)charsIter.next();
             if (origCharStr.length() < 1) {
                 final String msg = "Could not parse file: " + filename; //$NON-NLS-1$
                 log.error(msg, new RobotException(new IOException(msg)));
             }
             final char origChar = origCharStr.charAt(0);
             final String mapping = ((String)prop.get(origCharStr))
                 .toLowerCase();
             final StringTokenizer tok = new StringTokenizer(mapping, DELIMITER);
             final char nativeChar = mapping.charAt(mapping.length() - 1);
             final KeyStroke keyStroke = new KeyStroke(nativeChar);
             while (tok.hasMoreTokens()) {
                 final String modifier = tok.nextToken();
                 final int mod = getModifier(modifier);
                 if (mod != 0) {
                     keyStroke.addModifier(mod);
                 }
             }
             m_mapping.put(new Character(origChar), keyStroke);
         }
     }
     
     /**
      * 
      * @param modifier the modifier
      * @return the SWT constant of the given modifier or 0 if no SWT-modifier.
      */
     private int getModifier(String modifier) {
         int mod = 0;
         if (CTRL.equals(modifier)) {
             mod = SWT.CTRL;
         } else if (SHIFT.equals(modifier)) {
             mod = SWT.SHIFT;
         } else if (ALT.equals(modifier)) {
             mod = SWT.ALT;
         }
         return mod;
     }
     
     /**
      * 
      * @param character the charater
      * @return the KeyStroke for the given character.
      */
     public KeyStroke getKeyStroke(char character) {
         KeyStroke keyStroke = null;
         
         if (Character.isUpperCase(character)) {
             final char lowChar = Character.toLowerCase(character);
             keyStroke = new KeyStroke(lowChar);
             keyStroke.addModifier(SWT.SHIFT);
             return keyStroke;            
         }
         if (isSingleKey(character)) {
             return new KeyStroke(character);
         }
         keyStroke = (KeyStroke)m_mapping.get(new Character(character));
         
         // if no KeyStroke was found, return a KeyStroke with the given 
         // character and log in debug.
         // It is not necessarily an error, see method isSingleKey(char)!
         if (keyStroke == null && log.isDebugEnabled()) {
             log.debug("No keyboard-mapping found for character '"  //$NON-NLS-1$
                 + String.valueOf(character) + "'!"); //$NON-NLS-1$
         }
         return keyStroke != null ? keyStroke : new KeyStroke(character);
     }
 
 
 
     /**
      * @param character a char value
      * @return true if the given character can be entered via single keyboard
      * key (without modifiers), false otherwise.
      */
     private boolean isSingleKey(char character) {
         // Some characters e.g. ,.-+# on de_DE-layout do not match here.
         // Is there a way to find it out?
         return Character.isLowerCase(character) 
             || Character.isWhitespace(character)
             || (Character.isDigit(character));
     }
     
     
     /**
      * @param locale the Locale.
      * @return the file name of the keyboard mapping file.
      */
     private String createFileName(Locale locale) {
        final String fileName = KEYBOARD_MAPPING_FILE_PREFIX 
            + locale.getLanguage() + "_" + locale.getCountry()  //$NON-NLS-1$
            + KEYBOARD_MAPPING_FILE_POSTFIX;
         return fileName;
     }
     
     /**
      * KeyStroke
      * @author BREDEX GmbH
      * @created 13.07.2007
      */
     public static class KeyStroke {
         
         /** The character */
         private char m_char = '0';
         
         /** The List of modifiers */
         private List m_modifiers = new ArrayList(3);
 
         /**
          * Constructor.
          * @param character the character without the modifiers.
          */
         public KeyStroke(char character) {
             m_char = character;
         }
         
         /**
          * 
          * @return the character without modifiers.
          */
         public char getChar() {
             return m_char;
         }
         
         /**
          * 
          * @return the modifier
          */
         public Integer[] getModifiers() {
             return (Integer[])m_modifiers.toArray(
                 new Integer[m_modifiers.size()]);
         }
         
         /**
          * Adds the given modofier to the KeyStroke.
          * @param modifier the modoifier.
          */
         public void addModifier(int modifier) {
             m_modifiers.add(new Integer(modifier));
         }
         
         
     }
 
     /**
      * @return The Locale of tihs KeyboardHelper
      */
     public Locale getLocale() {
         return m_locale;
     }
     
     
 }
