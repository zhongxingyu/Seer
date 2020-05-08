 /*
  * Copyright (c) 2006-2012 DMDirc Developers
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package com.dmdirc.addons.ui_swing.components.statusbar;
 
 import com.dmdirc.addons.ui_swing.SwingController;
 import com.dmdirc.updater.UpdateChecker;
 import com.dmdirc.updater.UpdateComponent;
 import com.dmdirc.updater.manager.UpdateStatus;
 
 import java.awt.Window;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 /**
  * Information popup for the updater label.
  *
  * @since 0.6.3
  */
 public class UpdaterPopup extends StatusbarPopupWindow {
 
     /**
      * A version number for this class. It should be changed whenever the class
      * structure is changed (or anything else that would prevent serialized
      * objects being unserialized with the new class).
      */
     private static final long serialVersionUID = 1;
 
     /**
      * Creates a new popup window for the specified panel and window.
      *
      * @param controller Swing controller
      * @param parent The panel that owns this popup
      * @param parentWindow The Window that owns this popup
      */
     public UpdaterPopup(final SwingController controller, final JPanel parent,
             final Window parentWindow) {
         super(controller, parent, parentWindow);
     }
 
     /** {@inheritDoc} */
     @Override
     protected void initContent(final JPanel panel) {
         for (UpdateComponent component : UpdateChecker.getManager().getComponents()) {
             final UpdateStatus status = UpdateChecker.getManager().getStatus(component);
 
             if (status != UpdateStatus.IDLE && status != UpdateStatus.CHECKING_NOT_PERMITTED) {
                 panel.add(new JLabel(component.getFriendlyName()),
                         "growx, pushx");
                 panel.add(new JLabel(status.getDescription()),
                        "growx, pushx");
             }
         }
     }
 
 }
