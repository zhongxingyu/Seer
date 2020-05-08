 package org.bh.gui.swing;
 
 import javax.swing.JMenuItem;
 import javax.swing.KeyStroke;
 
 import org.bh.platform.i18n.BHTranslator;
 
 /**
  * BHMenuItem to create and display new menu items in the menu bar.
  * 
  * <p>
  * This class extends the Swing <code>JMenuItem</code> to display menu items
  * in the menu bar
  * 
  * @author Tietze.Patrick
  * @version 0.1, 2009/12/16
  * 
  */
 
 public class BHMenuItem extends JMenuItem implements IBHComponent{
 
 	private String key;
     private int[] validateRules;
     BHTranslator translator = BHTranslator.getInstance();
     
     /**
      * create the new menu item
      * @param key
      * @param eventKey
      * @param eventAction
      * @param actionCommand
      */
 
     public BHMenuItem(String key, int eventKey, int eventAction, String actionCommand){
     	super();
     	this.key = key;
    	this.setText(key);//translator.translate(key));
     	this.setMnemonic(eventKey);
     	this.setAccelerator(KeyStroke.getKeyStroke(eventKey, eventAction));
     	this.setActionCommand(actionCommand);
     	//this.addActionListener(this);
     	
     	
     }
 	
 	@Override
 	public String getKey() {
 		// TODO Auto-generated method stub
 		throw new UnsupportedOperationException("This method has not been implemented");
 	}
 
 	@Override
 	public int[] getValidateRules() {
 		// TODO Auto-generated method stub
 		throw new UnsupportedOperationException("This method has not been implemented");
 	}
 
 	@Override
 	public boolean isTypeValid() {
 		// TODO Auto-generated method stub
 		throw new UnsupportedOperationException("This method has not been implemented");
 	}
 
 	@Override
 	public void setValidateRules(int[] validateRules) {
 		// TODO Auto-generated method stub
 		throw new UnsupportedOperationException("This method has not been implemented");
 	}
 
 }
