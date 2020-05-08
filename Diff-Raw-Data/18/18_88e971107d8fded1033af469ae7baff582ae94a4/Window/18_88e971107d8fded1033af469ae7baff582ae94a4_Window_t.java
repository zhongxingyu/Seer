// Copyright (C),2005-2008 HandCoded Software Ltd.
 // All rights reserved.
 //
 // This software is licensed in accordance with the terms of the 'Open Source
 // License (OSL) Version 3.0'. Please see 'license.txt' for the details.
 //
 // HANDCODED SOFTWARE LTD MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE
 // SUITABILITY OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT
 // LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 // PARTICULAR PURPOSE, OR NON-INFRINGEMENT. HANDCODED SOFTWARE LTD SHALL NOT BE
 // LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 // OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 
 package com.handcoded.framework.swing;
 
 import java.awt.Image;
 import java.util.MissingResourceException;
 import java.util.PropertyResourceBundle;
 import java.util.ResourceBundle;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JInternalFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JRadioButton;
 import javax.swing.KeyStroke;
 
 /**
  * The base class of a GUI Window heirarchy which is independent of the
  * actual Swing/AWT structure.
  *
  * @author	BitWise
  * @version	$Id$
  * @since	TFP 1.0
  */
 public abstract class Window
 {
 	/**
 	 * Causes the <CODE>Window</CODE> to be made visible on the users screen.
 	 * @since	TFP 1.0
 	 */
 	public abstract void show ();
 
 	/**
 	 * Causes the <CODE>Window</CODE> to be removed from the screen it it
 	 * is visible.
 	 * @since	TFP 1.0
 	 */
 	public abstract void hide ();
 
 	/**
 	 * The <CODE>ResourceBundle</CODE> used to obtain localise string values.
 	 * @since	TFP 1.0
 	 */
 	protected ResourceBundle	bundle;
 
 	/**
 	 * Constructs a <CODE>Window</CODE> instance and locates a localised
 	 * <CODE>ResourceBundle</CODE> using the base filename.
 	 * 
 	 * @param	filename		The base portion of the resource filename.
 	 * @since	TFP 1.0
 	 */
 	protected Window (
 	final String 		filename)
 	{
 		try {
 			bundle = PropertyResourceBundle.getBundle (filename);
 		}
 		catch (MissingResourceException error) {
 			JOptionPane.showMessageDialog (new JFrame (),
 				"The GUI property file '" + filename + "' for the current\n" +
 				"locale is not installed on this system.",
 				"Error", JOptionPane.ERROR_MESSAGE);
 
 			System.exit (1);
 		}
 	}
 
 	/**
 	 * Creates a <CODE>JButton</CODE> instance using the localised resources
 	 * to provide the text and icon for the button.
 	 * 
 	 * @param	key				The base key for the control. 
 	 * @return	A constructed and initialised <CODE>JButton</CODE> instance.
 	 * @since	TFP 1.0
 	 */
 	protected final JButton createButton (final String key)
 	{
 		String	text	= getString (key + "/text");
 		String	icon	= getString (key + "/icon");
 		
 		JButton button = new JButton ();
 		
 		if (text != null) button.setText (text);
 		if (icon != null) button.setIcon (loadImageIcon (icon));
 		
 		return (button);
 	}
 	
 	/**
 	 * Creates a <CODE>JDialog</CODE> instance using the localised resources
 	 * to provide the title for the dialog.
 	 * 
 	 * @param	owner			The owning <CODE>JFrame</CODE> control.
 	 * @param	key				The base key for the control. 
 	 * @return	A constructed and initialised <CODE>JDialog</CODE> instance.
 	 * @since	TFP 1.0
 	 */
 	protected final JDialog createDialog (final java.awt.Frame owner, final String key, boolean modal)
 	{
 		String title	= getString (key + "/title", key);
 	
 		JDialog dialog = new JDialog (owner, title, modal);
 		
 		return (dialog);
 	}
 
 	/**
 	 * Creates a <CODE>JDialog</CODE> instance using the localised resources
 	 * to provide the title for the dialog.
 	 * 
 	 * @param	owner			The owning <CODE>Dialog</CODE> control.
 	 * @param	key				The base key for the control. 
 	 * @return	A constructed and initialised <CODE>JDialog</CODE> instance.
 	 * @since	TFP 1.0
 	 */
 	protected final JDialog createDialog (final java.awt.Dialog owner, final String key, boolean modal)
 	{
 		return (new JDialog (owner, getString (key + "/title", key), modal)); 
 	}
 
 	/**
 	 * Creates a <CODE>JFrame</CODE> instance using the localised resources
 	 * to provide the title and icon for the frame.
 	 * 
 	 * @param	key				The base key for the control. 
 	 * @return	A constructed and initialised <CODE>JFrame</CODE> instance.
 	 * @since	TFP 1.0
 	 */
 	protected final JFrame createFrame (final String key)
 	{
 		String title	= getString (key + "/title", key);
 		String icon		= getString (key + "/icon");
 
 		JFrame frame = new JFrame (title);
 
 		if (icon != null)
 			frame.setIconImage (loadImage (icon));
 		
 		return (frame);
 	}
 
 	/**
 	 * Creates a <CODE>JInternalFrame</CODE> instance using the localised resources
 	 * to provide the title and icon for the frame.
 	 * 
 	 * @param	key				The base key for the control. 
 	 * @return	A constructed and initialised <CODE>JInternalFrame</CODE> instance.
 	 * @since	TFP 1.0
 	 */
 	protected final JInternalFrame createInternalFrame (
 	final String		key)
 	{
 		String title	= getString (key + "/title", key);
 		String icon		= getString (key + "/icon");
 
 		JInternalFrame frame = new JInternalFrame (title);
 		
 		if (icon != null)
 			frame.setFrameIcon (loadImageIcon (icon));
 
 		return (frame);
 	}
 	
 	/**
 	 * Creates a <CODE>JMenuItem</CODE> instance using the localised resources
 	 * to provide the title, mnemonic, accelerator and icon for the item.
 	 * 
 	 * @param	key				The base key for the control. 
 	 * @return	A constructed and initialised <CODE>JMenuItem</CODE> instance.
 	 * @since	TFP 1.0
 	 */
 	protected final JMenuItem createItem (
 	final String		key)
 	{
 		String title 	= getString (key + "/title", key);
 		String mnemonic = getString (key + "/mnemonic");
 		String accel	= getString (key + "/accel");
 		String icon		= getString (key + "/icon");
 		
 		JMenuItem item;
 
 		if (mnemonic != null)
 		 	item = new JMenuItem (title, mnemonic.charAt (0));
 		else
 			item = new JMenuItem (title);
 
 		if (accel != null)
 			item.setAccelerator (KeyStroke.getKeyStroke (accel));
 
 		if (icon != null)
 			item.setIcon (loadImageIcon (icon));
 		
 		return (item);
 	}
 	
 	/**
 	 * Creates a <CODE>JLabel</CODE> instance using the localised resources
 	 * to provide the text for the label.
 	 * 
 	 * @param	key				The base key for the control. 
 	 * @return	A constructed and initialised <CODE>JLabel</CODE> instance.
 	 * @since	TFP 1.0
 	 */
 	protected final JLabel createLabel (final String key)
 	{
 		String		text = getString (key + "/text");
 		
 		JLabel label = new JLabel (text);
 		
 		return (label);
 	}
 	
 	/**
 	 * Creates a <CODE>JMenu</CODE> instance using the localised resources
 	 * to provide the title, mnemonic and icon for the menu.
 	 * 
 	 * @param	key				The base key for the control. 
 	 * @return	A constructed and initialised <CODE>JMenu</CODE> instance.
 	 * @since	TFP 1.0
 	 */
 	protected final JMenu createMenu (
 	final String		key)
 	{
 		String title 	= getString (key + "/title", key);
 		String mnemonic = getString (key + "/mnemonic");
 		String icon		= getString (key + "/icon");
 
 		JMenu menu = new JMenu (title);
 
 		if (mnemonic != null)
 			menu.setMnemonic (mnemonic.charAt (0));
 
 		if (icon != null)
 			menu.setIcon (loadImageIcon (icon));
 		
 		return (menu);
 	}
 
 	/**
 	 * Creates a <CODE>JRadioButton</CODE> instance using the localised resources
 	 * to provide the text for the button.
 	 * 
 	 * @param	key				The base key for the control. 
 	 * @return	A constructed and initialised <CODE>JRadioButton</CODE> instance.
 	 * @since	TFP 1.0
 	 */
 	protected final JRadioButton createRadioButton (final String key)
 	{
 		String		text = getString (key + "/text");
 		
 		JRadioButton button = new JRadioButton (text);
 		
 		return (button);
 	}
 	
 	/**
 	 * Loads an <CODE>ImageIcon</CODE> based on the filename associated with
 	 * a resource key.
 	 * 
 	 * @param 	key				The key for the resource.
 	 * @return	An <CODE>ImageIcon</CODE> instance or <CODE>null</CODE> if one
 	 * 			could not be found.
 	 * @since	TFP 1.0
 	 */
 	protected final ImageIcon getIcon (final String key)
 	{
 		return (loadImageIcon (getString (key)));
 	}
 	
 	/**
 	 * Recovers the value of the specified key from the <CODE>ResourceBundle</CODE>,
 	 * or returns the default value if it is not present.
 	 *
 	 * @param	key				The key of the required resource
 	 * @param	defaultValue	A default value if the key was not found.
 	 * @return	The value corresponding to the key or the default.
 	 * @since	TFP 1.0
 	 */
 	protected final String getString (
 	final String		key,
 	final String		defaultValue)
 	{
 		try {
 			return (bundle.getString (key));
 		}
 		catch (MissingResourceException error) {
 			return (defaultValue);
 		}
 	}
 
 	/**
 	 * Recovers the value of the specified key from the <CODE>ResourceBundle
 	 * </CODE>, or returns the default value if it is not present.
 	 *
 	 * @param	key				The key of the required resource
 	 *
 	 * @return	The value corresponding to the key or the default.
 	 * @since	TFP 1.0
 	 */
 	protected final String getString (
 	final String		key)
 	{
 		return (getString (key, null));
 	}
 
 	/**
 	 * Returns the <CODE>Application</CODE> instance.
 	 *
 	 * @return	The <CODE>Application</CODE> instance.
 	 * @since	TFP 1.0
 	 */
 	protected static Application getApplication ()
 	{
 		return ((Application) Application.getApplication ());
 	}
 
 	/**
 	 * Loads an <CODE>Image</CODE> from the indicated <CODE>filename</CODE>.
 	 * 
 	 * @param 	filename		The filename for the required image.
 	 * @return	The requested <CODE>Image</CODE> or <CODE>null</CODE> if
 	 * 			it could not be found.
 	 * @since	TFP 1.0
 	 */
 	protected static Image loadImage (final String filename)
 	{
		try {
			return (loadImageIcon (filename).getImage ());
		}
		catch (NullPointerException error) {
			return (null);
		}
 	}
 
 	/**
 	 * Loads an <CODE>ImageIcon</CODE> from the indicated <CODE>filename
 	 * </CODE>.
 	 * 
 	 * @param 	filename		The filename for the required image.
 	 * @return	The requested <CODE>ImageIcon</CODE> or <CODE>null</CODE> if
 	 * 			it could not be found.
 	 * @since	TFP 1.0
 	 */
 	protected static ImageIcon loadImageIcon (final String filename)
 	{
		try {
			return (new ImageIcon (Window.class.getClassLoader().getResource (filename)));
		}
		catch (NullPointerException error) {
			return (null);
		}
 	}
 }
