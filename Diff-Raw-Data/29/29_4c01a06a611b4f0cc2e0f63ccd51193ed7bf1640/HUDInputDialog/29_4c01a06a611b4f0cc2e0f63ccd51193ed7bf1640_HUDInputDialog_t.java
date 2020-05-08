 /*
  * Project Wonderland
  * 
  * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
  * 
  * Redistributions in source code form must reproduce the above
  * copyright and this condition.
  * 
  * The contents of this file are subject to the GNU General Public
  * License, Version 2 (the "License"); you may not use this file
  * except in compliance with the License. A copy of the License is
  * available at http://www.opensource.org/licenses/gpl-license.php.
  * 
  * Sun designates this particular file as subject to the "Classpath"
  * exception as provided by Sun in the License file that accompanied
  * this code.
  */
 package org.jdesktop.wonderland.modules.hud.client;
 
 import com.jme.math.Vector2f;
 import java.beans.PropertyChangeListener;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponentEvent.ComponentEventType;
 import org.jdesktop.wonderland.modules.appbase.client.App;
 import org.jdesktop.wonderland.modules.appbase.client.swing.WindowSwing;
 
 /**
  * A simple dialog for requesting a text value from the user.
  *
  * @author nsimpson
  */
 public class HUDInputDialog extends HUDComponent2D {
 
     private static final Logger logger = Logger.getLogger(HUDInputDialog.class.getName());
     private HUDInputDialogImpl dialogImpl;
     private WindowSwing window;
     private App app;
 
     public HUDInputDialog(App app) {
         super();
         this.app = app;
         initializeDialog();
     }
 
     public HUDInputDialog(App app, String label) {
         this(app);
         dialogImpl.setLabelText(label);
     }
 
     public HUDInputDialog(App app, String label, String value) {
         this(app, label);
         dialogImpl.setValueText(value);
     }
 
     /**
      * Create the dialog components
      */
     private void initializeDialog() {
         if (dialogImpl == null) {
             dialogImpl = new HUDInputDialogImpl(null, false);
 
             try {
                 logger.log(Level.FINE, "creating WindowSwing: " + dialogImpl.getWidth() + "x" + dialogImpl.getHeight());
                 window = new WindowSwing(app, dialogImpl.getWidth(), dialogImpl.getHeight(),
                         false, new Vector2f(0.02f, 0.02f));
                 window.setComponent(dialogImpl.getContentPane());
                 view = window.getPrimaryView();
                 HUD mainHUD = WonderlandHUDManager.getHUDManager().getHUD("main");
                 mainHUD.addComponent(this);
                 this.setLocation(500, 300);
                 this.setSize(dialogImpl.getWidth(), dialogImpl.getHeight() - 20);
                 window.setVisible(true);
                 this.setVisible(true);
             } catch (Exception e) {
                 logger.log(Level.WARNING, "failed to create HUD dialog: " + e);
             }
         }
     }
 
     /**
     * {@inheritDoc}
     */
    @Override
    public void setVisible(boolean visible) {
        if (visible == false) {
            // hack to make the view go away
            view.setVisible(false);
            view.setOnHUD(false);
            window.setVisible(false);
        } else {
            super.setVisible(visible);
        }
    }

    /**
      * Sets the string to be displayed on the text field label
      * @param text the string to display
      */
     public void setLabelText(String text) {
         dialogImpl.setLabelText(text);
     }
 
     /**
      * Gets the string displayed on the text field label
      * @return the text field label
      */
     public String getLabelText() {
         return dialogImpl.getLabelText();
     }
 
     /**
      * Sets the string displayed in the text field
      * @param text the string to display
      */
     public void setValueText(String text) {
         dialogImpl.setValueText(text);
     }
 
     /**
      * Gets the string entered by the user in the text field
      * @return the text field string
      */
     public String getValueText() {
         return dialogImpl.getValueText();
     }
 
     /**
      * Adds a bound property listener to the dialog
      * @param listener a listener for dialog events
      */
     public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
         dialogImpl.addPropertyChangeListener(listener);
     }
 
     /**
      * Removes a bound property listener from the dialog
      * @param listener the listener to remove
      */
     public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
         dialogImpl.removePropertyChangeListener(listener);
     }
 }
