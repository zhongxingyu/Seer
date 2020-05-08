 package org.bh.gui.swing;
 
 import javax.swing.JTextField;
 
 /**
  * BHTextField to display simple input fields at screen.
  * 
  * <p>
  * This class extends the Swing <code>JTextField</code> to display simple input
  * fields in Business Horizon.
  * 
  * @author Thiele.Klaus
  * @version 0.1, 2009/12/13
  * 
  */
 public class BHTextField extends JTextField implements IBHComponent {
     /**
      * unique id to identify Label.
      */
     private String id;
 
     /**
      * Constructor to create new <code>BHTextField</code>.
      * 
      * @param identifier
      *            unique id
      * @param value
      *            default value
      */
     public BHTextField(String identifier, String value) {
 	super(value);
 	this.id = identifier;
     }
 
     /**
      * Returns the unique ID of the <code>BHTextField</code>.
      * 
      * @return id unique identifier.
      */
     public String getID() {
 	return id;
     }
 }
