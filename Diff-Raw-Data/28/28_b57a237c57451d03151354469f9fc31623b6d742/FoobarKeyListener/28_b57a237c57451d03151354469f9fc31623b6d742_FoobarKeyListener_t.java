 package org.bruno.foobar;
 
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 
 import javax.swing.JTextField;
 
 /**
  * The FoobarKeyListener listens for key release events on the FoobarField.
  * 
  * @author Frank Goodman
  * 
  */
 public final class FoobarKeyListener implements KeyListener {
 	/**
 	 * Listen for the release of keys on the Foobar's text field. The enter key
 	 * will execute the Fooable currently selected. The tab key will
 	 * autocomplete the current search query. The up key will select the
 	 * previously indexed Fooable. The down key will select the next indexed
 	 * Fooable. Other non-modifying keys events will trigger a search for
 	 * Fooables.
 	 * 
 	 * @param e
 	 *            A key event
 	 */
 	@Override
 	public void keyReleased(KeyEvent e) {
 		// Get the source text field
 		JTextField field = (JTextField) e.getSource();
 
 		// Get the parent Foobar
 		Foobar foobar = (Foobar) field.getParent();
 
 		// Get the value of the source text field
 		String value = field.getText();
 
 		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
 
 			// Execute the selected Fooable when 'enter' is released
 			foobar.executeFooable();
 
 		} else if (e.getKeyCode() == KeyEvent.VK_TAB) {
 
 			// Autocomplete the input when 'tab' is released
 			String completed = foobar.completeFooable(value);
 			field.setText(completed);
 			foobar.showSuggestions(completed);
 
 		} else if (e.getKeyCode() == KeyEvent.VK_UP) {
 
 			// Select the previous indexed Fooable when 'up' is released
			if (foobar.getPopupManager().getSuggestions() != null) {
				foobar.getPopupManager().getSuggestions().setSelectedIndexPrevious();
			}
 			field.setCaretPosition(value.length());
 
 		} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
 
 			// Select the next indexed Fooable when 'down' is released
			if (foobar.getPopupManager().getSuggestions() != null) {
				foobar.getPopupManager().getSuggestions().setSelectedIndexNext();
			}
 			field.setCaretPosition(value.length());
 
 		} else if (e.getKeyCode() != KeyEvent.VK_CONTROL
 				&& e.getKeyCode() != KeyEvent.VK_META
 				&& e.getKeyCode() != KeyEvent.VK_LEFT
 				&& e.getKeyCode() != KeyEvent.VK_RIGHT) {
 
 			if (value.equals("")) {
 				// If no input is present, hide the suggestions popup
 				foobar.getPopupManager().destroyPopup();
 			} else {
 				// If input is present, display the suggestions popup
 				foobar.showSuggestions(value);
 			}
 
 		}
 	}
 
 	@Override
 	public void keyTyped(KeyEvent e) {
 	}
 
 	@Override
 	public void keyPressed(KeyEvent e) {
 	}
 }
