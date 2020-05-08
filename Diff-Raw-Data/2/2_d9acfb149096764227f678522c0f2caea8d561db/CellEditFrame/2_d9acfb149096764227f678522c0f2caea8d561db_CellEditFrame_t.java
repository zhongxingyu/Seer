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
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.DefaultListModel;
 import javax.swing.JPanel;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import org.jdesktop.wonderland.client.cell.Cell;
 import org.jdesktop.wonderland.client.cell.properties.CellComponentPropertiesManager;
 import org.jdesktop.wonderland.client.cell.properties.CellPropertiesEditor;
 import org.jdesktop.wonderland.client.cell.properties.CellPropertiesManager;
 import org.jdesktop.wonderland.client.cell.properties.spi.CellComponentPropertiesSPI;
 import org.jdesktop.wonderland.client.cell.properties.spi.CellPropertiesSPI;
 import org.jdesktop.wonderland.client.cell.registry.CellComponentRegistry;
 import org.jdesktop.wonderland.client.cell.registry.spi.CellComponentFactorySPI;
 import org.jdesktop.wonderland.common.cell.CellID;
 import org.jdesktop.wonderland.common.cell.messages.CellServerComponentMessage;
 import org.jdesktop.wonderland.common.cell.messages.CellServerStateMessage;
 import org.jdesktop.wonderland.common.cell.messages.CellServerStateResponseMessage;
 import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
 import org.jdesktop.wonderland.common.cell.state.CellServerState;
 import org.jdesktop.wonderland.common.cell.state.PositionComponentServerState;
 import org.jdesktop.wonderland.common.messages.ErrorMessage;
 import org.jdesktop.wonderland.common.messages.OKMessage;
 import org.jdesktop.wonderland.common.messages.ResponseMessage;
 
 /**
  * A frame to allow the editing of properties for the cell.
  * * 
  * @author Jordan Slott <jslott@dev.java.net>
  */
 public class CellEditFrame extends javax.swing.JFrame implements CellPropertiesEditor {
 
     /* The error logger */
     private static Logger logger = Logger.getLogger(CellEditFrame.class.getName());
 
     /* The cell the frame is currently editing */
     private Cell cell = null;
 
     /* The property sheet for the cell if it exists */
     private CellPropertiesSPI cellProperties = null;
 
     /* A list model for the list of capabilities */
     private DefaultListModel listModel = null;
 
     /* The cell server state fetched from the server-side cell */
     private CellServerState cellServerState = null;
 
     /* The panel to hold the basic cell properties */
     private BasicJPanel basicPanel = null;
 
     /* The panel to hold the cell transform */
     private PositionJPanel positionPanel = null;
 
     /* A Map of component property sheet display names and their panels */
     private Map<String, CellComponentPropertiesSPI> componentPropertiesMap = new HashMap();
 
     /* A set of panel Class object that are "dirty" */
     private Set<Class> dirtyPanelSet = new HashSet();
 
     /**
      * Creates new form CellEditFrame, takes the Cell to be edited as an
      * argument
      *
      * @param cell The Cell to be edited
      * @throw IlegalStateException If the dialog cannot be created sucessfully
      */
     public CellEditFrame(Cell cell) throws IllegalStateException {
         this.cell = cell;
 
         // Check to see that the cell is non-null. If so, flag an error and
         // return
         if (cell == null) {
             logger.warning("Cell passed to the cell properties frame is null");
             throw new IllegalStateException("Invalid Cell passed to Dialog");
         }
 
         // Fetch the initial server state of the cell. If we cannot fetch it,
         // then flag and error and return -- since there is nothing more we
         // can do
         cellServerState = fetchCellServerState();
         if (cellServerState == null) {
             logger.warning("Unable to fetch cell server state for " + cell.getName());
             throw new IllegalStateException("Unable to fetch cell server state");
         }
 
         // Look through the registry of cell property objects and check to see
         // if a panel exists for the cell. Add it if so
         Class clazz = cellServerState.getClass();
         CellPropertiesManager manager = CellPropertiesManager.getCellPropertiesManager();
         cellProperties = manager.getCellPropertiesByClass(clazz);
 
         // Initialize the GUI components
         initComponents();
 
         // Add a list model for the list of capabilities. Also, listen for
         // selections on the list to display the appropriate panel
         listModel = new DefaultListModel();
         capabilityList.setModel(listModel);
         capabilityList.addListSelectionListener(new CapabilityListSelectionListener());
 
         // Create and add a basic panel for all cells as a special case.
         basicPanel = new BasicJPanel(cell);
         listModel.add(0, "Basic");
 
         // Create and add a position panel for all cells as a special case.
         positionPanel = new PositionJPanel(cell);
         listModel.add(1, "Position");
 
         // If the cell properties panel exists, add an entry for it
         if (cellProperties != null && cellProperties.getPropertiesJPanel(this) != null) {
             listModel.add(2, cellProperties.getDisplayName());
         }
 
         // Loop through and add the components that are in the cell server
         // state and have an entry in the registry of components with property
         // panels
         updatePanelSet(cellServerState);
 
         // Update the GUI to reflect the values in the cell
         updateGUI();
     }
     
     /**
      * Returns a set of display names for each component that already exists
      * on the properties panel.
      *
      * @param A Set of property panel display names
      */
     public Set<String> getComponentDisplayNames() {
         return componentPropertiesMap.keySet();
     }
     
     /**
      * @inheritDoc()
      */
     public void setPanelDirty(Class clazz, boolean isDirty) {
         // Either add or remove the Class depending upon whether it is dirty
         if (isDirty == true) {
             dirtyPanelSet.add(clazz);
         }
         else {
             dirtyPanelSet.remove(clazz);
         }
 
         // Enable/disable the Ok/Apply buttons depending upon whether the set
         // of dirty panels is empty or not
         okButton.setEnabled(dirtyPanelSet.isEmpty() == false);
         applyButton.setEnabled(dirtyPanelSet.isEmpty() == false);
     }
 
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         mainPanel = new javax.swing.JPanel();
         jLabel1 = new javax.swing.JLabel();
         mainSplitPane = new javax.swing.JSplitPane();
         capabilityListScrollPane = new javax.swing.JScrollPane();
         capabilityList = new javax.swing.JList();
         propertyPanel = new javax.swing.JPanel();
         addCapabilityButton = new javax.swing.JButton();
         removeCapabilityButton = new javax.swing.JButton();
         buttonsPanel = new javax.swing.JPanel();
         okButton = new javax.swing.JButton();
         applyButton = new javax.swing.JButton();
         cancelButton = new javax.swing.JButton();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
         setTitle("Cell Property Editor");
 
         jLabel1.setText("Capabilities:");
 
         mainSplitPane.setDividerLocation(200);
         mainSplitPane.setDividerSize(15);
 
         capabilityList.setBackground(new java.awt.Color(204, 204, 255));
         capabilityList.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4));
         capabilityList.setFont(new java.awt.Font("Lucida Grande", 1, 12));
         capabilityList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
         capabilityListScrollPane.setViewportView(capabilityList);
 
         mainSplitPane.setLeftComponent(capabilityListScrollPane);
 
         propertyPanel.setBackground(new java.awt.Color(255, 255, 255));
         propertyPanel.setLayout(new java.awt.GridLayout(1, 1));
         mainSplitPane.setRightComponent(propertyPanel);
 
         addCapabilityButton.setFont(new java.awt.Font("Lucida Grande", 1, 14));
         addCapabilityButton.setText("+");
         addCapabilityButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
         addCapabilityButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 addCapabilityButtonActionPerformed(evt);
             }
         });
 
         removeCapabilityButton.setFont(new java.awt.Font("Lucida Grande", 1, 14));
         removeCapabilityButton.setEnabled(false);
         removeCapabilityButton.setLabel("-");
         removeCapabilityButton.setMargin(new java.awt.Insets(2, 4, 2, 4));
         removeCapabilityButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 removeCapabilityButtonActionPerformed(evt);
             }
         });
 
         buttonsPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
 
         okButton.setText("OK");
         okButton.setEnabled(false);
         okButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 okButtonActionPerformed(evt);
             }
         });
         buttonsPanel.add(okButton);
 
         applyButton.setText("Apply");
         applyButton.setEnabled(false);
         applyButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 applyButtonActionPerformed(evt);
             }
         });
         buttonsPanel.add(applyButton);
 
         cancelButton.setText("Cancel");
         cancelButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cancelButtonActionPerformed(evt);
             }
         });
         buttonsPanel.add(cancelButton);
 
         org.jdesktop.layout.GroupLayout mainPanelLayout = new org.jdesktop.layout.GroupLayout(mainPanel);
         mainPanel.setLayout(mainPanelLayout);
         mainPanelLayout.setHorizontalGroup(
             mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(mainPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(jLabel1)
                     .add(mainPanelLayout.createSequentialGroup()
                         .add(addCapabilityButton)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                         .add(removeCapabilityButton))
                     .add(mainSplitPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 683, Short.MAX_VALUE)
                     .add(buttonsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 683, Short.MAX_VALUE))
                 .addContainerGap())
         );
         mainPanelLayout.setVerticalGroup(
             mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(mainPanelLayout.createSequentialGroup()
                 .addContainerGap(20, Short.MAX_VALUE)
                 .add(jLabel1)
                 .add(6, 6, 6)
                 .add(mainSplitPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 441, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(addCapabilityButton)
                     .add(removeCapabilityButton))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(buttonsPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap())
         );
 
         org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(mainPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(mainPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
         // Apply the values from the GUI to the cell and close the window
         applyValues();
         this.setVisible(false);
         this.dispose();
     }//GEN-LAST:event_okButtonActionPerformed
 
     private void applyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_applyButtonActionPerformed
         // Apply the values from the GUI to the cell
         applyValues();
     }//GEN-LAST:event_applyButtonActionPerformed
 
     private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
         // Simply close the window
         this.setVisible(false);
         this.dispose();
     }//GEN-LAST:event_cancelButtonActionPerformed
 
     private void addCapabilityButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addCapabilityButtonActionPerformed
         // Create a new AddComponentDialog and display. Wait for the dialog
         // to close
         AddComponentDialog dialog = new AddComponentDialog(this, true, cell);
         dialog.setVisible(true);
 
         // If the OK button was pressed on the dialog and we can fetch a valid
         // cell component factory, then try to add it on the server.
         CellComponentFactorySPI spi = dialog.getCellComponentFactorySPI();
         if (dialog.getReturnStatus() == AddComponentDialog.RET_OK && spi != null) {
             addComponent(spi);
         }
     }//GEN-LAST:event_addCapabilityButtonActionPerformed
 
     private void removeCapabilityButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeCapabilityButtonActionPerformed
         // Find out which component is selected and remove it
         String selection = (String)capabilityList.getSelectedValue();
         CellComponentPropertiesSPI spi = componentPropertiesMap.get(selection);
         removeComponent(spi);
     }//GEN-LAST:event_removeCapabilityButtonActionPerformed
 
     /**
      * Inner class to deal with selection on the capability list
      */
     class CapabilityListSelectionListener implements ListSelectionListener {
 
         public void valueChanged(ListSelectionEvent e) {
             // Check to see if the value is still changing and if so, ignore
             if (e.getValueIsAdjusting() == true) {
                 return;
             }
             
             // Handles when an item has been selected in the list of capabilities
             // Display the proper panel in such an instance. We also enable
             // or disable the '-' sign to remove components depending upon
             // what is selected
             String selection = (String) capabilityList.getSelectedValue();
             if (selection == null) {
                 propertyPanel.removeAll();
                 removeCapabilityButton.setEnabled(false);
             }
             else if (selection.equals("Basic") == true) {
                 propertyPanel.removeAll();
                 propertyPanel.add(basicPanel.getPropertiesJPanel(CellEditFrame.this));
                 removeCapabilityButton.setEnabled(false);
             }
             else if (selection.equals("Position") == true) {
                 propertyPanel.removeAll();
                 propertyPanel.add(positionPanel.getPropertiesJPanel(CellEditFrame.this));
                 removeCapabilityButton.setEnabled(false);
             }
             else if (cellProperties != null && cellProperties.getDisplayName().equals(selection) == true) {
                 propertyPanel.removeAll();
                 propertyPanel.add(cellProperties.getPropertiesJPanel(CellEditFrame.this));
                 removeCapabilityButton.setEnabled(false);
             }
             else if (componentPropertiesMap.containsKey(selection) == true) {
                 propertyPanel.removeAll();
                 propertyPanel.add(componentPropertiesMap.get(selection).getPropertiesJPanel(CellEditFrame.this));
                 removeCapabilityButton.setEnabled(true);
             }
             else {
                 propertyPanel.removeAll();
                 removeCapabilityButton.setEnabled(false);
             }
 
             // Invalidate the layout and repaint
             propertyPanel.revalidate();
             propertyPanel.repaint();
         }
     }
 
     /**
      * Applies the values stored in the GUI to the cell
      */
     private void applyValues() {
         // If there is a cell-specific panel, fetch the items from the GUI
         // and put into the cell server state class
         if (cellProperties != null) {
             cellProperties.getCellServerState(cellServerState);
         }
 
         // Ask the basic panel to set its values as a special case.
         basicPanel.getCellServerState(cellServerState);
 
         // Loop through all of the component property panels and ask them to
         // fill in their server state
         Iterator<Map.Entry<String, CellComponentPropertiesSPI>> it = componentPropertiesMap.entrySet().iterator();
         while (it.hasNext() == true) {
             Map.Entry<String, CellComponentPropertiesSPI> entry = it.next();
             CellComponentPropertiesSPI spi = entry.getValue();
             spi.getCellServerState(cellServerState);
         }
         
         // Tell the server-side cell to update itself. Send the message over
         // the cell channel, so fetch the channel component first.
         cell.sendCellMessage(CellServerStateMessage.newSetMessage(cell.getCellID(), cellServerState));
 
         // XXX Probably should get a success/failed here!
 
         // Tell each of the panels to update their GUIs. This will have the
         // effect of resetting their original values so they know whether they
         // are dirty or not.
         updateGUI();
     }
 
     public CellServerState getCellServerState() {
         return cellServerState;
     }
     
     /**
      * Asks the server for the server state of the cell; returns null upon
      * error
      */
     private CellServerState fetchCellServerState() {
         // Fetch the setup object from the Cell object. We send a message on
         // the cell channel, so we must fetch that first.
         ResponseMessage response = cell.sendCellMessageAndWait(CellServerStateMessage.newGetMessage(cell.getCellID()));
         if (response == null) {
             return null;
         }
 
         // We need to remove the position component first as a special
         // case since we do not want to update it after the cell is
         // create
         CellServerStateResponseMessage cssrm = (CellServerStateResponseMessage)response;
         CellServerState state = cssrm.getCellServerState();
         if (state != null) {
             state.removeComponentServerState(PositionComponentServerState.class);
         }
         return state;
     }
 
     /**
      * Updates the GUI with values currently set in the cell 
      */
     private void updateGUI() {
         // If there is a cell-specific panel to display, then tell it to update
         // its UI based upon values in the server cell state
         if (cellProperties != null) {
             cellProperties.updateGUI(cellServerState);
         }
 
         // Update the GUI of the basic panel as a special case
         basicPanel.updateGUI(cellServerState);
 
         // Update the GUI of the position panel as a special case
         positionPanel.updateGUI();
         
         // Iterate through all of the other panels and tell them to update
         // themselves.
         Iterator<Map.Entry<String, CellComponentPropertiesSPI>> it = componentPropertiesMap.entrySet().iterator();
         while (it.hasNext() == true) {
             Map.Entry<String, CellComponentPropertiesSPI> entry = it.next();
             entry.getValue().updateGUI(cellServerState);
         }
     }
     
     /**
      * For the current Cell object, fetch which CellComponents are currently
      * associated with the cell and creates/deletes any panels on the GUI
      * edit frame as necessary. 
      */
     private void updatePanelSet(CellServerState cellSetup) {
         // Loop through all of the cell components in the server state and for
         // each see if there is a properties sheet registered for it. If so,
         // then add it.
         CellComponentPropertiesManager manager = CellComponentPropertiesManager.getCellComponentPropertiesManager();
         for (Map.Entry<Class, CellComponentServerState> e : cellSetup.getComponentServerStates().entrySet()) {
             CellComponentServerState state = e.getValue();
             CellComponentPropertiesSPI spi = manager.getCellComponentPropertiesByClass(state.getClass());
             if (spi != null) {
                 JPanel panel = spi.getPropertiesJPanel(this);
                 if (panel != null) {
                     String displayName = spi.getDisplayName();
                     componentPropertiesMap.put(displayName, spi);
                     listModel.addElement(displayName);
                 }
             }
         }
     }
 
     /**
      * Adds an individual component panel to the set of panels, given the
      * cell component factory and the component server state.
      */
     private void addComponentToPanelSet(CellComponentFactorySPI spi, CellComponentServerState state) {
         // First, since this is a new panel since the server state was fetched,
         // add the component server state to the cell server state.
         cellServerState.addComponentServerState(state);
 
         // Next, add the component display name to the list and to the maps
         // of properties panels. We look up the properties in the manager of
         // all component properties given the class name of the component
         // server state.
         CellComponentPropertiesManager manager = CellComponentPropertiesManager.getCellComponentPropertiesManager();
         CellComponentPropertiesSPI propertiesSPI = manager.getCellComponentPropertiesByClass(state.getClass());
         if (propertiesSPI != null) {
             JPanel panel = propertiesSPI.getPropertiesJPanel(this);
             if (panel != null) {
                 String displayName = propertiesSPI.getDisplayName();
                 componentPropertiesMap.put(displayName, propertiesSPI);
                 listModel.addElement(displayName);
             }
         }
     }
 
     /**
      * Removes an individual component panel from the set of panels, given the
      * display name of the component in the list
      */
     private void removeComponentFromPanelSet(String displayName) {
         componentPropertiesMap.remove(displayName);
         listModel.removeElement(displayName);
     }
 
     /**
      * Given a component factory, adds the component to the server and upates
      * the GUI to indicate its presence
      */
     private void addComponent(CellComponentFactorySPI spi) {
         // Fetch the default server state for the factory, and cell id. Make
         // sure we make it dynamically added
         CellComponentServerState state = spi.getDefaultCellComponentServerState();
        //state.setStatic(false);
         CellID cellID = cell.getCellID();
 
         // Send a ADD component message on the cell channel. Wait for a
         // response. If OK, then update the GUI with the new component.
         // Otherwise, display an error dialog box.
         CellServerComponentMessage message = CellServerComponentMessage.newAddMessage(cellID, state);
         ResponseMessage response = cell.sendCellMessageAndWait(message);
         if (response == null) {
             // log and error and post a dialog box
             logger.warning("Received a null reply from cell with id " +
                     cell.getCellID() + " with name " + cell.getName() +
                     " adding component.");
             return;
         }
 
         if (response instanceof OKMessage) {
             // If successful, add the component to the GUI
             addComponentToPanelSet(spi, state);
         }
         else if (response instanceof ErrorMessage) {
             // Log an error. Eventually we should display a dialog
             logger.log(Level.WARNING, "Unable to add component to the server",
                     ((ErrorMessage) response).getErrorCause());
         }
     }
 
     /**
      * Given the component properties SPI, removes the component from the server
      * and updates the GUI to indicate its absense
      */
     private void removeComponent(CellComponentPropertiesSPI spi) {
         // Fetch the default server state for the factory and the various info
         // we need to communicate with the server. We need to get to the class
         // name of the server-side component. We first need to fetch the
         // component factory using the properties object, and from that, get
         // the class name. This can be cleaned up I think! XXX
         CellComponentRegistry registry = CellComponentRegistry.getCellComponentRegistry();
         Class clazz = spi.getServerCellComponentClass();
         CellComponentFactorySPI factory = registry.getCellFactoryByStateClass(clazz);
         CellID cellID = cell.getCellID();
         String className = factory.getDefaultCellComponentServerState().getServerComponentClassName();
 
         // Send a message to the server with the cell id and class name and
         // wait for a response
         CellServerComponentMessage cscm = CellServerComponentMessage.newRemoveMessage(cellID, className);
         ResponseMessage response = cell.sendCellMessageAndWait(cscm);
         if (response == null) {
             logger.warning("Received a null reply from cell with id " +
                     cell.getCellID() + " with name " + cell.getName() +
                     " removing component.");
             return;
         }
 
 
         // Send the message to the server. Wait for a response. If OK, then
         // update the GUI with the new component. Otherwise, display an error
         // dialog box.
         if (response instanceof OKMessage) {
             // If successful, add the component to the GUI
             removeComponentFromPanelSet(spi.getDisplayName());
         }
         else if (response instanceof ErrorMessage) {
             // Log an error. Eventually we should display a dialog
             logger.log(Level.WARNING, "Unable to add component to the server",
                     ((ErrorMessage) response).getErrorCause());
         }
     }
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton addCapabilityButton;
     private javax.swing.JButton applyButton;
     private javax.swing.JPanel buttonsPanel;
     private javax.swing.JButton cancelButton;
     private javax.swing.JList capabilityList;
     private javax.swing.JScrollPane capabilityListScrollPane;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JPanel mainPanel;
     private javax.swing.JSplitPane mainSplitPane;
     private javax.swing.JButton okButton;
     private javax.swing.JPanel propertyPanel;
     private javax.swing.JButton removeCapabilityButton;
     // End of variables declaration//GEN-END:variables
 }
