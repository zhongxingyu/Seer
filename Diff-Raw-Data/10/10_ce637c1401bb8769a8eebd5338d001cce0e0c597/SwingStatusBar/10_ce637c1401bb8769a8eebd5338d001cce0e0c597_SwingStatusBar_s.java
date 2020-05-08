 /*
  * Copyright (c) 2006-2014 DMDirc Developers
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
 
 import com.dmdirc.events.StatusBarComponentAddedEvent;
 import com.dmdirc.events.StatusBarComponentRemovedEvent;
 import com.dmdirc.interfaces.ui.StatusBar;
 import com.dmdirc.interfaces.ui.StatusBarComponent;
 import com.dmdirc.ui.StatusMessage;
 
 import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
 
 import java.awt.Component;
 import java.util.Arrays;
 
 import javax.swing.JPanel;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 
 import net.miginfocom.layout.PlatformDefaults;
 import net.miginfocom.swing.MigLayout;
 
 import static com.dmdirc.addons.ui_swing.SwingPreconditions.checkOnEDT;
 
 /** Status bar, shows message and info on the GUI. */
 public class SwingStatusBar extends JPanel implements StatusBar {
 
     /** A version number for this class. */
     private static final long serialVersionUID = 5;
     /** Mig layout component restraints. */
     private final String componentConstraints;
     /** error panel. */
     private final ErrorPanel errorLabel;
     /** update label. */
     private final UpdaterLabel updaterLabel;
     /** Invite label. */
     private final InviteLabel inviteLabel;
 
     /**
      * Creates a new instance of SwingStatusBar.
      *
      * @param inviteLabel  The invite label to add to the status bar.
      * @param updaterLabel The updater label to add to the status bar.
      * @param errorLabel   The error label to add to the status bar.
      * @param messageLabel The message label to add to the status bar.
      */
     public SwingStatusBar(
             final InviteLabel inviteLabel,
             final UpdaterLabel updaterLabel,
             final ErrorPanel errorLabel,
             final MessageLabel messageLabel) {
         checkOnEDT();
 
         final int height = getFontMetrics(UIManager.getFont("Table.font")).getHeight()
                 + (int) PlatformDefaults.getUnitValueX("related").getValue()
                 + (int) PlatformDefaults.getUnitValueX("related").getValue();
         componentConstraints = "sgy components, hmax " + height + ", hmin " + height
                 + ", wmin 20, shrink 0";
 
         this.errorLabel = errorLabel;
         this.updaterLabel = updaterLabel;
         this.inviteLabel = inviteLabel;
 
         setLayout(new MigLayout("fill, ins 0, hidemode 3"));
 
         add(messageLabel, "grow, push, sgy components, hmax " + height
                 + ", hmin " + height);
         add(updaterLabel, componentConstraints);
         add(errorLabel, componentConstraints);
         add(inviteLabel, componentConstraints);
     }
 
    @Subscribe
     public void addComponent(final StatusBarComponentAddedEvent event) {
         Preconditions.checkArgument(event.getComponent() instanceof Component,
                 "Error adding status bar component, " +
                         "component must be an instance of java.awt.component");
         if (!Arrays.asList(getComponents()).contains(event.getComponent())) {
             SwingUtilities.invokeLater(new Runnable() {
 
                 @Override
                 public void run() {
                     remove(updaterLabel);
                     remove(errorLabel);
                     remove(inviteLabel);
                     add((Component) event.getComponent(), componentConstraints);
                     add(updaterLabel, componentConstraints);
                     add(inviteLabel, componentConstraints);
                     add(errorLabel, componentConstraints);
                     validate();
                 }
             });
         }
     }
 
    @Subscribe
     public void removeComponent(final StatusBarComponentRemovedEvent event) {
         Preconditions.checkArgument(event.getComponent() instanceof Component,
                 "Error removing status bar component" +
                         "component must be an instance of java.awt.component");
         SwingUtilities.invokeLater(new Runnable() {
 
             @Override
             public void run() {
                 remove((Component) event.getComponent());
                 validate();
             }
         });
     }
 
     @Override
     public void setMessage(final StatusMessage message) {
         //Do nothing
     }
 
     @Override
     public void clearMessage() {
         //Do nothing
     }
 
     @Override
     public void addComponent(final StatusBarComponent component) {
         //Do nothing
     }
 
     @Override
     public void removeComponent(final StatusBarComponent component) {
         //Do nothing
     }
 }
