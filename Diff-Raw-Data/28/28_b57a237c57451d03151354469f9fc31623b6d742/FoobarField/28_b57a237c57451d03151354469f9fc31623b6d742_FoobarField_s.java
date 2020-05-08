 package org.bruno.foobar;
 
 import java.awt.AWTKeyStroke;
 import java.awt.Dimension;
 import java.awt.KeyboardFocusManager;
 import java.util.Collections;
 
import org.bruno.frontend.PlaceholderTextField;
 
 
 /**
  * The FoobarField is the text field to search for Fooables. This is contained
  * within the Foobar panel.
  * 
  * @author Frank Goodman
  * 
  */
 public final class FoobarField extends PlaceholderTextField {
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * Create a new Foobar text field
 	 */
 	protected FoobarField() {
 		super();
 
 		// Listen for key presses
 		this.addKeyListener(new FoobarKeyListener());
 
 		// Prevent default tab behavior
 		this.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
 				Collections.<AWTKeyStroke> emptySet());
 
 		// Default size
 		this.setPreferredSize(new Dimension(220, 30));
 
 		this.setPlaceholderText("Foo...");
 
 		this.setVisible(true);
 	}
 }
