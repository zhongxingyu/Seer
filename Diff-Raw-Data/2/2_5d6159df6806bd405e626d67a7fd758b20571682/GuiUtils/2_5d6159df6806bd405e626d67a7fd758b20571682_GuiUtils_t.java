 package emcshop.util;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 
 import javax.swing.JComponent;
 import javax.swing.JDialog;
 import javax.swing.KeyStroke;
 
 /**
 * Contains GUI utility methods.
  * @author Michael Angstadt
  */
 public class GuiUtils {
 	/**
 	 * Builds a standardized tooltip string.
 	 * @param text the tooltip text
 	 * @return the standardized tooltip string
 	 */
 	public static String toolTipText(String text) {
 		text = text.replace("\n", "<br>");
 		return "<html><div width=300>" + text + "</div></html>";
 	}
 
 	/**
 	 * Configures a dialog to close when the escape key is pressed.
 	 * @param dialog the dialog
 	 */
 	public static void closeOnEscapeKeyPress(final JDialog dialog) {
 		onEscapeKeyPress(dialog, new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				dialog.dispose();
 			}
 		});
 	}
 
 	/**
 	 * Adds a listener to a dialog, which will fire when the escape key is
 	 * pressed.
 	 * @param dialog the dialog
 	 * @param listener the listener
 	 */
 	public static void onEscapeKeyPress(JDialog dialog, ActionListener listener) {
 		onKeyPress(dialog, KeyEvent.VK_ESCAPE, listener);
 	}
 
 	/**
 	 * Adds a listener to a dialog, which will fire when a key is pressed.
 	 * @param dialog the dialog
 	 * @param key the key (see constants in {@link KeyEvent})
 	 * @param listener the listener
 	 */
 	public static void onKeyPress(JDialog dialog, int key, ActionListener listener) {
 		dialog.getRootPane().registerKeyboardAction(listener, KeyStroke.getKeyStroke(key, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
 	}
 }
