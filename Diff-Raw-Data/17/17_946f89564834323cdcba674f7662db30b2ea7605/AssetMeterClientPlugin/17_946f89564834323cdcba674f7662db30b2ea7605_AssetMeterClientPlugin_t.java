 /**
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
 package org.jdesktop.wonderland.modules.assetmeter.client;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.JMenuItem;
 import org.jdesktop.wonderland.client.BaseClientPlugin;
 import org.jdesktop.wonderland.client.jme.JmeClientMain;
 import org.jdesktop.wonderland.common.annotation.Plugin;
 
 /**
  * Client-side plugin to activate the asset meter on the Tools menu.
  * 
  * @author Jordan Slott <jslott@dev.java.net>
  */
 @Plugin
 public class AssetMeterClientPlugin extends BaseClientPlugin {
 
     private AssetMeterJFrame assetMeterJFrame;
     private final JMenuItem item;
 
     public AssetMeterClientPlugin() {
         item = new JMenuItem("Asset Meter");
     }
 
     @Override
     protected void activate() {
         // First create the asset meter frame and keep a weak reference to it
         // so that it gets garbage collected
         assetMeterJFrame = new AssetMeterJFrame();
         assetMeterJFrame.setSize(350, 200);
 
         // Add the Asset Meter as a checkbox menu item to the Tools menu as a
         // Checkbox menu item. If it is selected, then show it or hide it. Keep
         // the frame in a weak reference.
         item.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                if (assetMeterJFrame.isVisible() == false) {
                    assetMeterJFrame.setVisible(true);
                }
             }
         });
 
         // Add the item to the tools menu and make the Asset Meter visible
         // by default initially.
         JmeClientMain.getFrame().addToWindowMenu(item, 1);
         assetMeterJFrame.setVisible(true);
     }
 
     @Override
     protected void deactivate() {
         // remove items
         JmeClientMain.getFrame().removeFromWindowMenu(item);
         assetMeterJFrame.setVisible(false);
         assetMeterJFrame.deactivate();
         assetMeterJFrame = null;
     }
 }
