 package org.bh.gui.swing;
 
 import javax.swing.JLabel;
 
 /**
  * BHLabel to display Labels at screen.
  * 
  * <p>
  * This class extends the Swing <code>JLabel</code> to display labels in
  * Business Horizon.
  * 
  * @author Thiele.Klaus
  * @version 0.1, 2009/12/13
  * 
  */
 public class BHLabel extends JLabel implements IBHComponent {
     /**
      * unique id to identify Label.
      */
     private String id;
 
     /**
      * Constructor to create new <code>BHLabel</code>.
      * 
      * @param identifier
      *            unique id
      * @param value
      *            default value
      */
     public BHLabel(String identifier, String value) {
 	super(value);
 	this.id = identifier;
     }
 
     /**
      * Returns the unique ID of the <code>BHLabel</code>.
      * 
      * @return id unique identifier.
      */
     public String getID() {
 	return id;
     }
 
 }
