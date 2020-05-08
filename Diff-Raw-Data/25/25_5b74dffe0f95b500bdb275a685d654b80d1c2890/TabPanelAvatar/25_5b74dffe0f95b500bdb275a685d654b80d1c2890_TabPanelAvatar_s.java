 /*
  * Created On: October 6, 2006, 1:42 PM
  */
 package com.thinkparity.ophelia.browser.application.browser.display.avatar.tab;
 
 import java.awt.Component;
 import java.awt.GridBagConstraints;
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.swing.AbstractAction;
 import javax.swing.DefaultListModel;
 import javax.swing.JComponent;
 import javax.swing.KeyStroke;
 import javax.swing.SwingUtilities;
 import javax.swing.TransferHandler;
 import javax.swing.event.ListDataEvent;
 import javax.swing.event.ListDataListener;
 
 import com.thinkparity.ophelia.browser.Constants;
 import com.thinkparity.ophelia.browser.application.browser.display.avatar.AvatarId;
 import com.thinkparity.ophelia.browser.application.browser.display.avatar.Resizer;
 import com.thinkparity.ophelia.browser.application.browser.display.avatar.Resizer.ResizeEdges;
 import com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.TabButtonActionDelegate;
 import com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.TabPanel;
 
 /**
  * <b>Title:</b>thinkParity Tab Panel Avatar<br>
  * <b>Description:</b>An abstraction of a tab avatar. A tab avatar is the
  * visual representation of the avatar. It requires a model of a specific type
  * to feed it data and generally maintain it via a DefaultList model
  * intermediary.<br>
  * 
  * @author raymond@thinkparity.com
  * @version 1.1.2.1
  * @param <T> The model type.
  */
 public abstract class TabPanelAvatar<T extends TabPanelModel> extends TabAvatar<T> {
 
     /**
      * A client property key <code>String</code> mapping to the panels'
      * transfer handlers.
      */
     private static final String CPK_PANEL_TRANSFER_HANDLER;
 
     static {
         CPK_PANEL_TRANSFER_HANDLER = "TabPanelAvatar#panelTransferHandler";
     }
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private final javax.swing.JLabel fillJLabel = new javax.swing.JLabel();
     private final javax.swing.JPanel tabJPanel = new javax.swing.JPanel();
     private final javax.swing.JScrollPane tabJScrollPane = new javax.swing.JScrollPane();
     // End of variables declaration//GEN-END:variables
 
     /**
      * The set of <code>GridBagConstraints</code> used when adding a fill
      * component.
      */
     private final GridBagConstraints fillConstraints;
 
     /** A <code>TabAvatarFilterDelegate</code>. */
     private TabAvatarFilterDelegate filterDelegate;
 
     /** The set of <code>GridBagConstraints</code> used when adding a panel. */
     private final GridBagConstraints panelConstraints;
 
     /**
      * A <code>TransferHandler</code> which dispatches all panel import export
      * requests to the model.
      */
     private final TransferHandler panelTransferHandler;
 
     /** The map of tab panel mouse listeners. */
     private final Map<TabPanel, MouseAdapter> tabPanelMouseListeners;
 
     /** The map of tab panel mouse listeners. */
     private final Map<TabPanel, MouseAdapter> tabPanelMouseMotionListeners;
 
     /**
      * Creates TabPanelAvatar.
      * 
      */
     public TabPanelAvatar(final AvatarId id, final T model) {
     	super(id, model);
         this.tabPanelMouseListeners = new HashMap<TabPanel, MouseAdapter>();
         this.tabPanelMouseMotionListeners = new HashMap<TabPanel, MouseAdapter>();
         this.fillConstraints = new GridBagConstraints();
         this.fillConstraints.fill = GridBagConstraints.HORIZONTAL;
         this.fillConstraints.weightx = 1.0F;
         this.fillConstraints.weighty = 1.0F;
         this.fillConstraints.gridx = 0;
         this.fillConstraints.gridy = GridBagConstraints.RELATIVE;
         this.panelConstraints = new GridBagConstraints();
         this.panelConstraints.fill = GridBagConstraints.BOTH;
         this.panelConstraints.gridx = 0;
         this.panelTransferHandler = new TransferHandler() {
             @Override
             public boolean canImport(final JComponent comp,
                     final DataFlavor[] transferFlavors) {
                 logger.logApiId();
                 logger.logVariable("comp", comp);
                 logger.logVariable("transferFlavors", transferFlavors);
                 return model.canImportData((TabPanel) comp, transferFlavors);
             }
             @Override
             public boolean importData(final JComponent comp,
                     final Transferable transferable) {
                 logger.logApiId();
                 logger.logVariable("comp", comp);
                 logger.logVariable("transferable", transferable);
                 try {
                     model.importData((TabPanel) comp, transferable);
                     return true;
                 } catch (final Throwable t) {
                     logger.logError(t, "Could not import data {0} for {1}.",
                             transferable, comp);
                     return false;
                 }
             }
         };
     	installDataListener();
         initComponents();
         initScrollPane();
         installResizer();
         installRequestFocusListener();
         addRequestFocusListener(tabJPanel);
         installFocusListeners();
         setTransferHandler(new TransferHandler() {
             @Override
             public boolean canImport(final JComponent comp,
                     final DataFlavor[] transferFlavors) {
                 logger.logApiId();
                 logger.logVariable("comp", comp);
                 logger.logVariable("transferFlavors", transferFlavors);
                 return model.canImportData(transferFlavors);
             }
             @Override
             public boolean importData(final JComponent comp,
                     final Transferable transferable) {
                 logger.logApiId();
                 logger.logVariable("comp", comp);
                 logger.logVariable("transferable", transferable);
                 try {
                     model.importData(transferable);
                     return true;
                 } catch (final Throwable t) {
                     logger.logError(t, "Could not import data {0}.",
                             transferable);
                     return false;
                 }
             }
         });
         tabJScrollPane.getVerticalScrollBar().setUnitIncrement(Constants.ScrollBar.UNIT_INCREMENT);
        new Resizer(getController(), tabJScrollPane, Boolean.FALSE, Resizer.ResizeEdges.MIDDLE);
         bindKeys();
     }
 
     /**
      * @see com.thinkparity.codebase.swing.AbstractJPanel#debug()
      * 
      */
     @Override
     public void debug() {
         final Component[] components = tabJPanel.getComponents();
         logger.logDebug("{0} components.", components.length);
     }
 
     /**
      * Obtain the filter delegate.
      *
      * @return A <code>TabAvatarFilterDelegate</code>.
      */
     public TabAvatarFilterDelegate getFilterDelegate() {
         return filterDelegate;
     }
 
     /**
      * Obtain the tab button action delegate.
      *
      * @return A <code>TabButtonActionDelegate</code>.
      */
     public TabButtonActionDelegate getTabButtonActionDelegate() {
         return model.getTabButtonActionDelegate();
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.application.browser.display.avatar.tab.TabAvatar#reload()
      * 
      */
     @Override
 	public void reload() {
     	super.reload();
     }
 
     /**
      * Request focus in the tab.
      */
     public void requestFocusInTab() {
         requestFocusInWindow();
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.platform.application.display.avatar.Avatar#getResizeEdges()
      * 
      */
     @Override
     protected ResizeEdges getResizeEdges() {
         return Resizer.ResizeEdges.MIDDLE;
     }
 
     /**
      * Set the filter delegate.
      *
      * @param filterDelegate
      *            A <code>TabAvatarFilterDelegate</code>.
      */
     protected void setFilterDelegate(final TabAvatarFilterDelegate filterDelegate) {
         this.filterDelegate = filterDelegate;
     }
 
     /**
      * Add the fill component.
      * 
      * @param listSize
      *            The <code>int</code> size of the list.
      */
     private void addFill(final int listSize) {
         tabJPanel.add(fillJLabel, fillConstraints, listSize);
     }
 
     /**
      * Add a panel from a model at an index.
      * 
      * @param index
      *            An <code>int</code> index.
      * @param panel
      *            A <code>TabPanel</code>.
      */
     private void addPanel(final int index, final TabPanel panel) {
         final JComponent jComponent = (JComponent) panel;
         // property to protect against adding it twice
         final TransferHandler transferHandler =
             (TransferHandler) jComponent.getClientProperty(CPK_PANEL_TRANSFER_HANDLER);
         if (null == transferHandler) {
             // setup a transfer handler for each panel added
             jComponent.setTransferHandler(panelTransferHandler);
             jComponent.putClientProperty(CPK_PANEL_TRANSFER_HANDLER, panelTransferHandler);
         }
         panelConstraints.gridy = index;
         tabJPanel.add((Component) panel, panelConstraints.clone(), index);
         addTabPanelListeners(panel);
     }
     
     /**
      * Add listeners to a panel.
      * 
      * @param panel
      *            A <code>TabPanel</code>.  
      */
     private void addTabPanelListeners(final TabPanel panel) {
         addTabPanelMouseListener(panel, tabPanelMouseListeners);
         addTabPanelMouseMotionListener(panel, tabPanelMouseMotionListeners);
     }
     
     /**
      * Add a MouseListener to a panel.
      * 
      * @param panel
      *            A <code>TabPanel</code>.  
      * @param listeners
      *            A map of panels to listeners.
      */
     private void addTabPanelMouseListener(final TabPanel panel,
             final Map<TabPanel, MouseAdapter> listeners) {
         final java.awt.event.MouseAdapter mouseAdapter = new java.awt.event.MouseAdapter() {
             @Override
             public void mouseEntered(MouseEvent e) {
                 redispatch(e, TabPanelAvatar.this);
             }
             @Override
             public void mouseExited(MouseEvent e) {
                 redispatch(e, TabPanelAvatar.this);
             }
             @Override
             public void mousePressed(MouseEvent e) {
                 redispatch(e, TabPanelAvatar.this);
             }
             @Override
             public void mouseReleased(MouseEvent e) {
                 redispatch(e, TabPanelAvatar.this);
             }           
         };
         ((Component)panel).addMouseListener(mouseAdapter);
         listeners.put(panel, mouseAdapter);
     }
     
     /**
      * Add a MouseMotionListener to a panel.
      * 
      * @param panel
      *            A <code>TabPanel</code>.  
      * @param listeners
      *            A map of panels to listeners.
      */
     private void addTabPanelMouseMotionListener(final TabPanel panel,
             final Map<TabPanel, MouseAdapter> listeners) {
         final java.awt.event.MouseAdapter mouseAdapter = new java.awt.event.MouseAdapter() {
             @Override
             public void mouseDragged(MouseEvent e) {
                 redispatch(e, TabPanelAvatar.this);
             }
             @Override
             public void mouseMoved(MouseEvent e) {
                 redispatch(e, TabPanelAvatar.this);
             }          
         };
         ((Component)panel).addMouseMotionListener(mouseAdapter);
         tabPanelMouseMotionListeners.put(panel, mouseAdapter);
     }
 
     /**
      * Bind keys to the appropriate actions.
      */
     private void bindKeys() {
         // home: select the first panel
         bindKey(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0),
                 new AbstractAction() {
                     public void actionPerformed(final ActionEvent e) {
                         model.selectFirstPanel();
                     }
                 });
         // end: select the last panel
         bindKey(KeyStroke.getKeyStroke(KeyEvent.VK_END, 0),
                 new AbstractAction() {
                     public void actionPerformed(final ActionEvent e) {
                         model.selectLastPanel();
                     }
                 });
         // cursor down: select the next panel
         bindKey(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0),
                 new AbstractAction() {
                     public void actionPerformed(final ActionEvent e) {
                         model.selectNextPanel();
                     }
                 });
         // cursor up: select the previous panel
         bindKey(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0),
                 new AbstractAction() {
                     public void actionPerformed(final ActionEvent e) {
                         model.selectPreviousPanel();
                     }
                 });
         // delete: delete panel
         bindKey(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),
                 new AbstractAction() {
                     public void actionPerformed(final ActionEvent e) {
                         model.deleteSelectedPanel();
                     }
                 });
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
     private void initComponents() {
         java.awt.GridBagConstraints gridBagConstraints;
 
         final javax.swing.JPanel headerJPanel = new javax.swing.JPanel();
 
         setLayout(new java.awt.GridBagLayout());
 
         headerJPanel.setMaximumSize(new java.awt.Dimension(5000, 1));
         headerJPanel.setMinimumSize(new java.awt.Dimension(128, 1));
         headerJPanel.setOpaque(false);
         headerJPanel.setPreferredSize(new java.awt.Dimension(128, 1));
         org.jdesktop.layout.GroupLayout headerJPanelLayout = new org.jdesktop.layout.GroupLayout(headerJPanel);
         headerJPanel.setLayout(headerJPanelLayout);
         headerJPanelLayout.setHorizontalGroup(
             headerJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 481, Short.MAX_VALUE)
         );
         headerJPanelLayout.setVerticalGroup(
             headerJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 1, Short.MAX_VALUE)
         );
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
         add(headerJPanel, gridBagConstraints);
 
         tabJScrollPane.setBorder(null);
         tabJScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
         tabJScrollPane.setPreferredSize(new java.awt.Dimension(256, 128));
         tabJPanel.setLayout(new java.awt.GridBagLayout());
 
         tabJPanel.setBackground(new java.awt.Color(255, 255, 255));
         tabJPanel.add(fillJLabel, new java.awt.GridBagConstraints());
 
         tabJScrollPane.setViewportView(tabJPanel);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         add(tabJScrollPane, gridBagConstraints);
 
     }// </editor-fold>//GEN-END:initComponents
 
     /**
      * Initialize the scroll pane.
      */
     private void initScrollPane() {
         // uninstall keyboard actions since this is handled manually elsewhere
         SwingUtilities.replaceUIActionMap(tabJScrollPane, null);
         SwingUtilities.replaceUIInputMap(tabJScrollPane, JComponent.
                    WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, null);
     }
 
     /**
      * Install the a data listener on the list model. This will translate the
      * model's data events into UI component events.
      * 
      */
     private void installDataListener() {
         final DefaultListModel listModel = model.getListModel();
         listModel.addListDataListener(new ListDataListener() {
             /**
              * @see javax.swing.event.ListDataListener#contentsChanged(javax.swing.event.ListDataEvent)
              * 
              */
             public void contentsChanged(final ListDataEvent e) {
                 debug();
                 removeFill();
                 for (int i = e.getIndex0(); i <= e.getIndex1(); i++) {
                     removePanel(i);
                     addPanel(i, (TabPanel) listModel.get(i));
                 }
                 addFill(listModel.size());
                 reloadPanels();
             }
             /**
              * @see javax.swing.event.ListDataListener#intervalAdded(javax.swing.event.ListDataEvent)
              * 
              */
             public void intervalAdded(final ListDataEvent e) {
                 debug();
                 removeFill();
                 for (int i = e.getIndex0(); i <= e.getIndex1(); i++) {
                     addPanel(i, (TabPanel) listModel.get(i));
                 }
                 addFill(listModel.size());
                 reloadPanels();
             }
             /**
              * @see javax.swing.event.ListDataListener#intervalRemoved(javax.swing.event.ListDataEvent)
              * 
              */
             public void intervalRemoved(final ListDataEvent e) {
                 debug();
                 removeFill();
                 for (int i = e.getIndex0(); i <= e.getIndex1(); i++) {
                     removePanel(i);
                 }
                 addFill(listModel.size());
                 reloadPanels();
             } 
         });
     }
 
     /**
      * Install focus listeners.
      * 
      * Focus on the tab is forwarded to focus on the selected
      * panel if possible.
      */
     private void installFocusListeners() {
         final FocusListener focusListener = new FocusListener() {
             public void focusGained(final FocusEvent e) {
                 model.requestFocusInSelectedPanel();
             }
             public void focusLost(final FocusEvent e) {}
         };
         this.addFocusListener(focusListener);
         tabJPanel.addFocusListener(focusListener);
     }
 
     /**
      * Redispatch events to the destination component.
      * 
      * @param e
      *            A <code>MouseEvent</code>.
      * @param destination
      *            A destination <code>Component</code>.          
      */
     private void redispatch(final MouseEvent e, final Component destination) {
         Component source = e.getComponent(); 
         destination.dispatchEvent(SwingUtilities.convertMouseEvent(source, e, destination));
      }
 
     /**
      * Reload the panels display. The jpanel is revalidated.
      * 
      * NOTE validate() is used rather than revalidate() so validation
      * occurs immediately. From the perspective of the model, this means
      * after synchronize() it is possible to call getBounds() on panels.
      * 
      * It is also important to note that a revalidate() of tabJPanel
      * will cause tabJScrollPane to validate, but if validate() is used
      * on tabJPanel then the tabJScrollPane must also validate().
      */
     private void reloadPanels() {
         tabJPanel.validate();
         tabJScrollPane.validate();
         tabJPanel.repaint();        
     }
 
     /**
      * Remove the fill component.
      *
      */
     private void removeFill() {
         tabJPanel.remove(fillJLabel);
     }
 
     /**
      * Remove a panel at an index.
      * 
      * @param index
      *            An <code>int</code> index.
      */
     private void removePanel(final int index) {
         removeTabPanelListeners((TabPanel) tabJPanel.getComponent(index));
         tabJPanel.remove(index);
     }
     
     /**
      * Remove listeners from a panel.
      * 
      * @param panel
      *            A <code>TabPanel</code>.  
      */
     private void removeTabPanelListeners(final TabPanel panel) {
         removeTabPanelMouseListener(panel, tabPanelMouseListeners);
         removeTabPanelMouseMotionListener(panel, tabPanelMouseMotionListeners);
     }
     
     /**
      * Remove the MouseListener from a panel.
      * 
      * @param panel
      *            A <code>TabPanel</code>.  
      * @param listeners
      *            A map of panels to listeners.
      */
     private void removeTabPanelMouseListener(final TabPanel panel,
             final Map<TabPanel, MouseAdapter> listeners) {
         if (listeners.containsKey(panel)) {
             ((Component) panel).removeMouseListener(listeners.get(panel));
             listeners.remove(panel);
         }
     }
 
     /**
      * Remove the MouseMotionListener from a panel.
      * 
      * @param panel
      *            A <code>TabPanel</code>.  
      * @param listeners
      *            A map of panels to listeners.
      */
     private void removeTabPanelMouseMotionListener(final TabPanel panel,
             final Map<TabPanel, MouseAdapter> listeners) {
         if (listeners.containsKey(panel)) {
             ((Component) panel).removeMouseMotionListener(listeners.get(panel));
             listeners.remove(panel);
         }
     }
 }
