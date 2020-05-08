 package com.alexrnl.commons.gui.swing;
 
 import java.awt.Component;
 import java.awt.Frame;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.util.Collection;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.KeyStroke;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 import javax.swing.UIManager.LookAndFeelInfo;
 import javax.swing.UnsupportedLookAndFeelException;
 
 import com.alexrnl.commons.error.ExceptionUtils;
 import com.alexrnl.commons.translation.AbstractDialog;
 import com.alexrnl.commons.translation.GUIElement;
 import com.alexrnl.commons.translation.Translator;
 import com.alexrnl.commons.utils.StringUtils;
 
 /**
  * Utility class for Swing related methods.<br />
  * @author Alex
  */
 public final class SwingUtils {
 	/** Logger */
 	private static Logger	lg	= Logger.getLogger(SwingUtils.class.getName());
 	
 	/**
 	 * Constructor #1.<br />
 	 * Default private constructor.
 	 */
 	private SwingUtils () {
 		super();
 	}
 	
 	/**
 	 * Sets the look and feel of the application.<br />
 	 * Update all frames and pack them to update
 	 * @param lookAndFeelName
 	 *        the name of the look and feel to set.
 	 * @return <code>true</code> if the new look and feel was successfully set.
 	 */
 	public static boolean setLookAndFeel (final String lookAndFeelName) {
 		if (UIManager.getLookAndFeel().getName().equals(lookAndFeelName)) {
 			if (lg.isLoggable(Level.INFO)) {
				lg.info("Look and Feel " + lookAndFeelName + " is alredy set, nothing to do");
 			}
 			return true;
 		}
 		boolean lookAndFeelApplied = false;
 		for (final LookAndFeelInfo laf : UIManager.getInstalledLookAndFeels()) {
 			if (lg.isLoggable(Level.FINE)) {
 				lg.fine(laf.getName());
 			}
 			if (laf.getName().equals(lookAndFeelName)) {
 				try {
 					UIManager.setLookAndFeel(laf.getClassName());
 					if (lg.isLoggable(Level.INFO)) {
 						lg.info("Look and feel properly setted (" + laf.getName() + ").");
 					}
 					lookAndFeelApplied = true;
 				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
 						| UnsupportedLookAndFeelException e) {
 					lg.warning("Could not set the look and feel " + laf.getName() + ": "
 						+ ExceptionUtils.display(e));
 					lookAndFeelApplied = false;
 				}
 			}
 		}
 		// Applying changes to application
 		if (lookAndFeelApplied) {
 			for (final Frame frame : Frame.getFrames()) {
 				SwingUtilities.updateComponentTreeUI(frame);
 			}
 		} else {
 			lg.warning("Could not find (or set) the look and feel " + lookAndFeelName
 					+ ". Using default look and feel.");
 		}
 		return lookAndFeelApplied;
 	}
 	
 	/**
 	 * Return the message of a dialog with the parameters included, if the message contained some.
 	 * @param translator
 	 *        the translator to use for displaying the text.
 	 * @param dialog
 	 *        the dialog to use.
 	 * @param maxLine
 	 *        the maximum length allowed on a line.
 	 * @return the message to display.
 	 */
 	static String getMessage (final Translator translator, final AbstractDialog dialog, final int maxLine) {
 		return StringUtils.splitInLinesHTML(translator.get(dialog.message(), dialog.getParameters()), maxLine);
 	}
 	
 	/**
 	 * Show the dialog with translation picked from the dialog specified.
 	 * @param parent
 	 *        the parent window (may be <code>null</code>).
 	 * @param translator
 	 *        the translator to use for displaying the text.
 	 * @param dialog
 	 *        the key to the translations to use.
 	 * @param type
 	 *        the type of the dialog. Generally, {@link JOptionPane#ERROR_MESSAGE error},
 	 *        {@link JOptionPane#WARNING_MESSAGE warning}, {@link JOptionPane#INFORMATION_MESSAGE
 	 *        information}, {@link JOptionPane#QUESTION_MESSAGE question} or
 	 *        {@link JOptionPane#PLAIN_MESSAGE plain} message.
 	 * @param maxLine
 	 *        the maximum length allowed on a line.
 	 * @see JOptionPane#showMessageDialog(Component, Object, String, int, javax.swing.Icon)
 	 */
 	public static void showMessageDialog (final Component parent, final Translator translator,
 			final AbstractDialog dialog, final int type, final int maxLine) {
 		JOptionPane.showMessageDialog(parent, getMessage(translator, dialog, maxLine), translator.get(dialog.title()), type);
 	}
 	
 	/**
 	 * Show a confirmation dialog to the user.<br/>
 	 * @param parent
 	 *        the parent component.
 	 * @param translator
 	 *        the translator to use for displaying the text.
 	 * @param dialog
 	 *        the dialog to display.
 	 * @param maxLine
 	 *        the maximum length allowed on a line.
 	 * @return <code>true</code> if the use confirmed the dialog (clicked 'yes').
 	 */
 	public static boolean askConfirmation (final Component parent, final Translator translator,
 			final AbstractDialog dialog, final int maxLine) {
 		final int choice = JOptionPane.showConfirmDialog(parent, getMessage(translator, dialog, maxLine),
 				translator.get(dialog.title()), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
 		return choice == JOptionPane.YES_OPTION;
 	}
 	
 	/**
 	 * Ask the user to choose an element from a list.<br />
 	 * The elements of the list will be translated using their {@link Object#toString()} method.<br />
 	 * The collection provided should not contain identical object or identical text translations.
 	 * @param <T>
 	 *        the type of element the dialog offers.
 	 * @param parent
 	 *        the parent component.
 	 * @param translator
 	 *        the translator to use for displaying the text.
 	 * @param dialog
 	 *        the dialog to display.
 	 * @param elements
 	 *        the elements to display in the list.
 	 * @param maxLine
 	 *        the maximum length allowed on a line.
 	 * @return the selected element, or <code>null</code> if user canceled.
 	 */
 	public static <T> T askChoice (final Component parent, final Translator translator,
 			final AbstractDialog dialog, final Collection<T> elements, final int maxLine) {
 		if (elements == null || elements.isEmpty()) {
 			lg.warning("Cannot display a dialog for choices with an null or empty list");
 			throw new IllegalArgumentException("Cannot display input dialog with null or empty list");
 		}
 		
 		// Map element and their translation,
 		final Map<String, T> translationMap = new LinkedHashMap<>(elements.size());
 		for (final T t : elements) {
 			translationMap.put(translator.get(t.toString()), t);
 		}
 		
 		final String choice = (String) JOptionPane.showInputDialog(parent, getMessage(translator, dialog, maxLine),
 				translator.get(dialog.title()), JOptionPane.QUESTION_MESSAGE, null,
 				translationMap.keySet().toArray(new Object[0]), translationMap.keySet().iterator().next());
 		
 		if (lg.isLoggable(Level.FINE)) {
 			lg.fine("The user has choose " + choice);
 		}
 		
 		return translationMap.get(choice);
 	}
 	
 	/**
 	 * Creates a {@link JMenuItem} based on the parameters provided.<br />
 	 * <ul>
 	 * <li>Set a mnemonic (using the character following the {@link Translator#MNEMONIC_MARK} defined).</li>
 	 * <li>Set the shortcut define in the translation file.</li>
 	 * <li>Set the listener specified.</li>
 	 * <li>Set the tool tip.</li>
 	 * </ul>
 	 * @param translator
 	 *        the translator to use for displaying the text.
 	 * @param element
 	 *        the element to use to build the JMenuItem (use text and accelerator).
 	 * @param actionListener
 	 *        the listener to add on the menu item.
 	 * @return the menu item created.
 	 * @see #getMenu(Translator, String)
 	 */
 	public static JMenuItem getMenuItem (final Translator translator, final GUIElement element,
 			final ActionListener actionListener) {
 		final JMenuItem item = new JMenuItem();
 		
 		installMnemonics(translator, item, element.getText());
 		
 		// Set shortcut
 		if (translator.has(element.getShortcut())) {
 			final String shortcut = translator.get(element.getShortcut());
 			item.setAccelerator(KeyStroke.getKeyStroke(shortcut));
 		}
 		// Set listener
 		if (actionListener != null) {
 			item.addActionListener(actionListener);
 		}
 		// Set tool tip
 		if (translator.has(element.getToolTip())) {
 			final String toolTip = translator.get(element.getToolTip());
 			item.setToolTipText(toolTip);
 		}
 		return item;
 	}
 	
 	/**
 	 * Creates a {@link JMenu} based on the text provided.<br />
 	 * @param translator
 	 *        the translator to use for displaying the text.
 	 * @param element
 	 *        the translation key to use.
 	 * @return a menu parsed to retrieve the Mnemonic, if set.
 	 */
 	public static JMenu getMenu (final Translator translator, final String element) {
 		final JMenu menu = new JMenu();
 		installMnemonics(translator, menu, element);
 		return menu;
 	}
 	
 	/**
 	 * Install the text and the mnemonics on the specified menu component.<br />
 	 * @param translator
 	 *        the translator to use for displaying the text.
 	 * @param menu
 	 *        the menu item to initialize.
 	 * @param key
 	 *        the translation key to use.
 	 */
 	private static void installMnemonics (final Translator translator, final JMenuItem menu, final String key) {
 		String text = translator.get(key);
 		final int mnemonicIndex = text.indexOf(Translator.MNEMONIC_MARK);
 		if (mnemonicIndex == text.length() - 1) {
 			lg.warning("Mnemonic mark at the end of the translation, cannot set mnemonic");
 			menu.setText(text.substring(0, mnemonicIndex));
 		} else if (mnemonicIndex > -1) {
 			text = text.substring(0, mnemonicIndex) + text.substring(mnemonicIndex + 1);
 			menu.setText(text);
 			menu.setMnemonic(KeyEvent.getExtendedKeyCodeForChar(text.charAt(mnemonicIndex)));
 		} else {
 			menu.setText(text);
 		}
 	}
 }
