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
 package org.jdesktop.wonderland.modules.palette.client;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.lang.ref.WeakReference;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JComponent;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import org.jdesktop.mtgame.Entity;
 import org.jdesktop.wonderland.client.ClientPlugin;
 import org.jdesktop.wonderland.client.cell.Cell;
 import org.jdesktop.wonderland.client.cell.CellEditChannelConnection;
 import org.jdesktop.wonderland.client.comms.WonderlandSession;
 import org.jdesktop.wonderland.client.contextmenu.ContextMenu;
 import org.jdesktop.wonderland.client.contextmenu.ContextMenuEvent;
 import org.jdesktop.wonderland.client.contextmenu.ContextMenuListener;
 import org.jdesktop.wonderland.client.jme.JmeClientMain;
 import org.jdesktop.wonderland.client.login.LoginManager;
 import org.jdesktop.wonderland.client.login.ServerSessionManager;
 import org.jdesktop.wonderland.client.scenemanager.SceneManager;
 import org.jdesktop.wonderland.common.annotation.Plugin;
 import org.jdesktop.wonderland.common.cell.CellEditConnectionType;
 import org.jdesktop.wonderland.common.cell.messages.CellCreateMessage;
 import org.jdesktop.wonderland.common.cell.messages.CellDeleteMessage;
 
 /**
  * Client-size plugin for the cell palette.
  * 
  * @author Jordan Slott <jslott@dev.java.net>
  */
 @Plugin
 public class PaletteClientPlugin implements ClientPlugin {
 
     private static Logger logger = Logger.getLogger(PaletteClientPlugin.class.getName());
 
     /* The single instance of the cell palette dialog */
     private WeakReference<CellPalette> cellPaletteFrameRef = null;
 
     public void initialize(ServerSessionManager loginInfo) {
         // Add the Palette menu and the Cell submenu and dialog that lets users
         // create new cells.
         JMenu paletteMenu = new JMenu("Palettes");
         JMenuItem item = new JMenuItem("Cell Palette");
         item.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 CellPalette cellPaletteFrame;
                 if (cellPaletteFrameRef == null || cellPaletteFrameRef.get() == null) {
                     cellPaletteFrame = new CellPalette();
                     cellPaletteFrameRef = new WeakReference(cellPaletteFrame);
                 }
                 else {
                     cellPaletteFrame = cellPaletteFrameRef.get();
                 }
                 
                 if (cellPaletteFrame.isVisible() == false) {
                     cellPaletteFrame.setVisible(true);
                 }
             }
         });
         paletteMenu.add(item);
         JmeClientMain.getFrame().addToToolMenu(paletteMenu);
 
         // Add the "Properties" item to the context menu. Display a CellEditFrame
         // when selected.
         ContextMenu contextMenu = ContextMenu.getContextMenu();
         contextMenu.addContextMenuItem("Properties", new ContextMenuListener() {
             public void entityContextPerformed(ContextMenuEvent event) {
                 // Fetch the cell from the context event, using the entity
                 // given in the event
                 Entity entity = event.getEntityList().get(0);
                 if (entity == null) {
                     logger.warning("Unable to find Entity for context event");
                     return;
                 }
                 Cell cell = SceneManager.getCellForEntity(entity);
                 if (cell == null) {
                     logger.warning("Unable to find Cell from Entity for context event");
                     return;
                 }
 
                 // Create a new cell edit frame passing in the Cell and make
                 // it visible
                 try {
                     CellEditFrame frame = new CellEditFrame(cell);
                     frame.setVisible(true);
                 } catch (IllegalStateException excp) {
                     Logger.getLogger(PaletteClientPlugin.class.getName()).log(Level.WARNING, null, excp);
                 }
             }
         });
 
         // Add the "Delete" item to the context menu. When selected, display
         // a confirmation dialog and then delete the Cell
         contextMenu.addContextMenuItem("Delete", new ContextMenuListener() {
             public void entityContextPerformed(ContextMenuEvent event) {
                // Fetch the cell from the context event, using the entity
                 // given in the event
                 Entity entity = event.getEntityList().get(0);
                 if (entity == null) {
                     logger.warning("Unable to find Entity for context event");
                     return;
                 }
                 Cell cell = SceneManager.getCellForEntity(entity);
                 if (cell == null) {
                     logger.warning("Unable to find Cell from Entity for context event");
                     return;
                 }
 
                 // Display a confirmation dialog to make sure we really want to
                 // delete the cell.
                 int result = JOptionPane.showConfirmDialog(
                         JmeClientMain.getFrame().getFrame(),
                         "Are you sure you wish to delete cell " + cell.getName(),
                         "Confirm Delete",
                         JOptionPane.YES_NO_OPTION);
                 if (result == JOptionPane.NO_OPTION) {
                     return;
                 }
 
                 // If we want to delete, send a message to the server as such
                 WonderlandSession session = LoginManager.getPrimary().getPrimarySession();
                 CellEditChannelConnection connection = (CellEditChannelConnection) session.getConnection(CellEditConnectionType.CLIENT_TYPE);
                 CellDeleteMessage msg = new CellDeleteMessage(cell.getCellID());
                 connection.send(msg);
 
                 // Really should receive an OK/Error response from the server!
             }
         });
     }
 }
