 /*******************************************************************************
  * Copyright (c) 2004, 2010 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.rc.swing.listener;
 
 import java.awt.AWTEvent;
 import java.awt.Component;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentEvent;
 import java.awt.event.FocusEvent;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import java.awt.event.WindowEvent;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JDialog;
 import javax.swing.JEditorPane;
 import javax.swing.JFrame;
 import javax.swing.JList;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JPopupMenu;
 import javax.swing.JTabbedPane;
 import javax.swing.JTable;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.JTextPane;
 import javax.swing.JTree;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.event.TreeExpansionEvent;
 import javax.swing.event.TreeExpansionListener;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 import javax.swing.plaf.basic.ComboPopup;
 import javax.swing.text.JTextComponent;
 import javax.swing.tree.TreePath;
 
 import org.eclipse.jubula.communication.message.ChangeAUTModeMessage;
 import org.eclipse.jubula.rc.common.AUTServer;
 import org.eclipse.jubula.rc.common.exception.NoIdentifierForComponentException;
 import org.eclipse.jubula.rc.common.logger.AutServerLogger;
 import org.eclipse.jubula.tools.objects.IComponentIdentifier;
 import org.eclipse.jubula.tools.xml.businessmodell.Action;
 
 
 
 /**
  * The AWTEventListener for mode OBSERVATION. <br>
  * 
  * The component is marked by calling the methods
  * highLight() and lowLight() respectively of the corresponding implementation
  * class. <br>
  * 
  * The key events are tapped for selecting the <code>m_currentComponent</code>
  * to be used for the object mapping. The method <code>accept(KeyEvent)</code>
  * from the <code>MappingAcceptor</code> is queried to decide, whether the
  * event suits the active configuration. <br>
  * 
  * A <code>ComponentHandler</code> is used to determine the identifaction of
  * the component. See the <code>ComponentHandler</code> for details.
  * 
  * @author BREDEX GmbH
  * @created 23.08.2004
  */
 public class RecordListener extends AbstractAutSwingEventListener {    
     /** the logger */
     private static AutServerLogger log = new AutServerLogger(
         RecordListener.class);
     
     /** clicked or pressed Component */
     private Component m_selectedComponent;
     
     /** clicked or pressed ComponentParent */
     private Component m_selectedComponentParent;
     
     /** The RecordHelper */
     private RecordHelper m_recordHelper = new RecordHelper();
     
     /** The RecordActions */
     private RecordActions m_recordActions = new RecordActions();
     
     /** The RecordActions */
     private int m_popupMouseBtn = MouseEvent.BUTTON3;
     
     /** ListSelectionListener */
     private ListSelectionListener m_listSelListener =
         new ListSelectionListener() {
             public void valueChanged(ListSelectionEvent e) {
                 if (!e.getValueIsAdjusting()
                         && AUTServer.getInstance().getMode() 
                             == ChangeAUTModeMessage.RECORD_MODE) {
                     JList jlst = (JList)e.getSource();
                     IComponentIdentifier id = null;
                     try {
                         id = ComponentHandler.getIdentifier(jlst);
                         if (jlst.getSelectedIndices().length != 0
                                 && (getSelectedComponent() == jlst
                                         || getSelectedComponentParent() == jlst
                                         || getLastEvent().getSource() 
                                             == jlst)) {
                             Action a = m_recordHelper.compSysToAction(
                                     id, "CompSystem.SelectValues"); //$NON-NLS-1$
                             m_recordActions.selectListValues(jlst, id, a);
                             //m_recordActions.setSelectionState(true);
                         }
                     } catch (NoIdentifierForComponentException nifce) {
                         // no identifier for the component, log this as an error
                         log.error("no identifier for '" + getCurrentComponent()); //$NON-NLS-1$
                     }
                 }
             }
         };
 
     /** TreeExpansionListener */
     private TreeExpansionListener m_treExpListener = 
         new TreeExpansionListener() {
             public void treeCollapsed(TreeExpansionEvent event) {
                 JTree jtre = (JTree)event.getSource();
                 IComponentIdentifier id = null;
                 TreePath path = event.getPath();
                 if (path != null
                         && AUTServer.getInstance().getMode() 
                             == ChangeAUTModeMessage.RECORD_MODE
                         && (getSelectedComponent() == jtre
                                 || getSelectedComponentParent() == jtre
                                 || getLastEvent().getSource() == jtre)) {
                     try {
                         id = ComponentHandler.getIdentifier(jtre);
                         m_recordActions.collExpTree(jtre, path, id, "CompSystem.CollapseByTextPath"); //$NON-NLS-1$
                         //m_recordActions.setSelectionState(true);
                     } catch (NoIdentifierForComponentException nifce) {
                         // no identifier for the component, log this as an error
                         log.error("no identifier for '" + getCurrentComponent()); //$NON-NLS-1$
                     }
                 }                
             }
             public void treeExpanded(TreeExpansionEvent event) {
                 JTree jtre = (JTree)event.getSource();
                 IComponentIdentifier id = null;
                 TreePath path = event.getPath();
                 if (path != null
                         && AUTServer.getInstance().getMode() 
                             == ChangeAUTModeMessage.RECORD_MODE
                         && (getSelectedComponent() == jtre
                                 || getSelectedComponentParent() == jtre
                                 || getLastEvent().getSource() == jtre)) {
                     try {
                         id = ComponentHandler.getIdentifier(jtre);
                         m_recordActions.collExpTree(jtre, path, id, "CompSystem.ExpandByTextPath"); //$NON-NLS-1$
                         //m_recordActions.setSelectionState(true);
                     } catch (NoIdentifierForComponentException nifce) {
                         // no identifier for the component, log this as an error
                         log.error("no identifier for '" + getCurrentComponent()); //$NON-NLS-1$
                     }
                 }             
             }
         };
 
     /** TreeSelectionListener */
     private TreeSelectionListener m_treSelListener =
         new TreeSelectionListener() {
             public void valueChanged(TreeSelectionEvent e) {
                 JTree jtre = (JTree)e.getSource();
                 IComponentIdentifier id = null;
                 TreePath path = e.getPath();
                 try {
                     id = ComponentHandler.getIdentifier(jtre);
                     if (path != null
                             && AUTServer.getInstance().getMode() 
                                 == ChangeAUTModeMessage.RECORD_MODE
                             && (getSelectedComponent() == jtre
                                     || getSelectedComponentParent() == jtre
                                     || getLastEvent().getSource() == jtre)) {
                         Action a = m_recordHelper.compSysToAction(id, "CompSystem.SelectByTextPath"); //$NON-NLS-1$
                         int clickcount = 1; //me.getClickCount();
                         m_recordActions.selectNode(
                                 jtre, path, id, a, clickcount);
                         //m_recordActions.setSelectionState(true);
                     }
                 } catch (NoIdentifierForComponentException nifce) {
                     // no identifier for the component, log this as an error
                     log.error("no identifier for '" + getCurrentComponent()); //$NON-NLS-1$
                 }
             }
         };        
         
     /** ActionListener */   
     private ActionListener m_comboListener = 
         new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 if (e.getActionCommand().equals("comboBoxChanged") //$NON-NLS-1$
                         && AUTServer.getInstance().getMode()
                             == ChangeAUTModeMessage.RECORD_MODE) { 
                     Component popComp = null;
                     Component lastSrc = getEventSource(getLastEvent());
                     if (lastSrc instanceof JList) {
                         Component parent = lastSrc.getParent();
                         while (parent != null) {
                             if (parent instanceof JPopupMenu) {
                                 JPopupMenu popUp = (JPopupMenu)parent;
                                 popComp = popUp.getInvoker();
                                 if (popComp != null 
                                         && popComp instanceof JComboBox) {
                                     lastSrc = popComp;
                                 }
                                 break;
                             }
                             parent = parent.getParent();
                         }                    
                     }
                     if (lastSrc instanceof JTextComponent) {
                         Component parent = lastSrc.getParent();
                         if (parent instanceof JComboBox) {
                             lastSrc = parent;
                         }
                     }
                     if (e.getSource() instanceof JComboBox) {
                         JComboBox jcbx = (JComboBox)e.getSource();
                         if (jcbx.getSelectedIndex() != -1
                                 && (getSelectedComponent() == jcbx
                                         || getSelectedComponentParent() == jcbx
                                         || lastSrc == jcbx)) {
                             IComponentIdentifier id = null;
                             try {
                                 id = ComponentHandler.getIdentifier(jcbx);
                                 Action a = m_recordHelper.compSysToAction(id, "CompSystem.SelectValue"); //$NON-NLS-1$
                                 m_recordActions.selectCbxValue(jcbx, id, a);
                                 m_recordActions.setSelectionState(true);
                             } catch (
                                     NoIdentifierForComponentException 
                                     nifce) {
                                 // no identifier for the component, log this as an error
                                 log.error("no identifier for '" + getCurrentComponent()); //$NON-NLS-1$
                             }
                         }
                     }
                 }
             }
         };
       
       
     /**
      * method for event-handling
      * @param event AWTEvent
      */
     protected void handleEvent(AWTEvent event) {
         if (event.equals(getLastEvent())) {
             return;
         }
         if (getEventSource(event) == null 
                 //|| isComboPopup(getEventSource(event))
                 || event.getID() == MouseEvent.MOUSE_EXITED) {
             return;
         }
         if (!(m_recordHelper.isSupportedComponent(getEventSource(event)))
                 && !(event instanceof KeyEvent)) {
             return;
         }
         
         if ((event instanceof WindowEvent)
                 || (event.getID() == ComponentEvent.COMPONENT_HIDDEN)
                 || (event.getID() == ComponentEvent.COMPONENT_SHOWN)) {
             handleWindowEvent(event);
         }
         
         if ((event instanceof FocusEvent)) {
             handleFocusEvent(event);
         }
      
         if (event instanceof MouseEvent
                 && getEventSource(event) instanceof JComponent) {
             handleMouseEvent(event, getEventSource(event));
         }
         if ((event instanceof KeyEvent)) {
             setSelectedComponent(getEventSource(event));
             setSelectedComponentParent(getEventSource(event).getParent());
             KeyEvent keyEvent = (KeyEvent)event;
             handleKeyEvent(keyEvent);      
         }
         if ((event.getID() == MouseEvent.MOUSE_ENTERED)) {
             setSelectedComponent(getEventSource(event));
             setSelectedComponentParent(getEventSource(event).getParent());
             handleMouseEnterEvent(getEventSource(event));      
         }
         if (event.getID() != MouseEvent.MOUSE_EXITED) {
             setLastEvent(event);
         }        
     }
     
     /**
      * method for handling Mouse-Events
      * @param source Component
      */
     protected void handleMouseEnterEvent(Component source) {
         if (source instanceof JList && !(isComboPopup(source))) {
             final JList jlst = (JList)source;
             boolean containsListener = false;
             ListSelectionListener[] lsls = jlst.getListSelectionListeners();
             for (int i = 0; i < lsls.length; i++) {
                 ListSelectionListener listener = lsls[i];
                 if (listener == m_listSelListener) {
                     containsListener = true;
                 }
             }
             if (!containsListener) {
                 jlst.addListSelectionListener(m_listSelListener);
             }
 
         } else if (source instanceof JTree) {
             final JTree jtre = (JTree)source;
             boolean containsSelListener = false;
             TreeSelectionListener[] tsl = jtre.getTreeSelectionListeners();
             for (int i = 0; i < tsl.length; i++) {
                 TreeSelectionListener listener = tsl[i];
                 if (listener == m_treSelListener) {
                     containsSelListener = true;
                 }
             }
             if (!containsSelListener) {
                 jtre.addTreeSelectionListener(m_treSelListener);
             }
             boolean containsExpListener = false;
             TreeExpansionListener[] tel = jtre.getTreeExpansionListeners();
             for (int i = 0; i < tel.length; i++) {
                 TreeExpansionListener listener = tel[i];
                 if (listener == m_treExpListener) {
                     containsExpListener = true;
                 }
             }
             if (!containsExpListener) {
                 jtre.addTreeExpansionListener(m_treExpListener);
             }
 
         } else if (source instanceof JComboBox
                 || source.getParent() instanceof JComboBox) {
             Component src = source;
             if (src.getParent() instanceof JComboBox) {
                 src = source.getParent();
             }
             final JComboBox jcbx = (JComboBox)src;            
             boolean containsActionListener = false;
             ActionListener[] actL = jcbx.getActionListeners();
             for (int i = 0; i < actL.length; i++) {
                 ActionListener listener = actL[i];
                 if (listener == m_comboListener) {
                     containsActionListener = true;
                 }
             }
             if (!containsActionListener) {
                 jcbx.addActionListener(m_comboListener);
             }
         }
     }
 
     
     /**
      * method for handling Mouse-Events
      * @param event AWTEvent
      * @param source Component
      */
     protected void handleMouseEvent(AWTEvent event,
             Component source) {
         MouseEvent me = (MouseEvent)event;
         if (me.isPopupTrigger()) {
             m_popupMouseBtn = me.getButton();
             m_recordActions.setPopupMouseButton(m_popupMouseBtn);
         }
         IComponentIdentifier id = null;        
         try {
             id = ComponentHandler.getIdentifier(source);
             //don't capture right-click before ContextMenu (for Windows)
             if (getLastEvent() == null) {
                 return;
             }
             if (getLastEvent().getID() == MouseEvent.MOUSE_RELEASED
                     && !(me.getSource() instanceof JPopupMenu)
                     && me.getButton() == m_popupMouseBtn) {
                 m_recordActions.clickGraphComp(id, me, source);
             }
             if ((me.getID() == MouseEvent.MOUSE_RELEASED)) {
                 //don't capture right-click before ContextMenu (for Linux)
                 if (me.getButton() == m_popupMouseBtn
                         && getLastEvent().getSource() instanceof JPopupMenu) {
                     return;
                 }
                 if (source.getParent() instanceof JComboBox
                         && source instanceof JButton) {
                     return;
                 }                
                 if (source.getParent() instanceof JComboBox
                         && !(source instanceof JComboBox)) {
                     id = ComponentHandler.getIdentifier(source.getParent());
                 }
                 Action a = new Action();
                 if (source instanceof JTabbedPane) {
                     JTabbedPane jtpn = (JTabbedPane)source;
                     a = m_recordHelper.compSysToAction(id, "CompSystem.SelectTab"); //$NON-NLS-1$
                     m_recordActions.selectTab(jtpn, id, a);
                 } else if (source instanceof JTable) {
                     JTable jtbl = (JTable)source;
                     int clickcount = me.getClickCount();
                     a = m_recordHelper.compSysToAction(id, "CompSystem.SelectCellNew"); //$NON-NLS-1$
                    m_recordActions.selectTableCell(jtbl, id, clickcount, a);
                 } else if (source instanceof JMenuItem
                         && !(source instanceof JMenu)) {
                     JMenuItem jmi = (JMenuItem)source;
                     m_recordActions.selectMenuItem(jmi);
                 } else if (source != null && !(source instanceof JMenu)
                         && !(source instanceof JTree)
                         && !(source instanceof JList)
                         && !(source instanceof JTextComponent
                                 && ((JTextComponent)source).isEditable())
                         && !(source.getParent() instanceof JTable)
                         && me.getButton() != m_popupMouseBtn) {
                     m_recordActions.clickGraphComp(id, me, source);
                 }
             }
         } catch (NoIdentifierForComponentException nifce) {
             // no identifier for the component, log this as an error
             log.error("no identifier for '" + getCurrentComponent()); //$NON-NLS-1$
         }
     }
     
     
     /**
      * method for handling Key-Events
      * @param keyEvent AWTEvent
      */
     private void handleKeyEvent(KeyEvent keyEvent) {
         synchronized (getComponentLock()) {
             if (keyEvent.getKeyCode() != KeyEvent.VK_UNDEFINED
                     && keyEvent.getID() == KeyEvent.KEY_PRESSED) {
                 final int accepted = getAcceptor().accept(keyEvent);
                 final boolean isSingleTrigger =
                     getAcceptor().isSingleLineTrigger(keyEvent);
                 final boolean isMultiTrigger =
                     getAcceptor().isMultiLineTrigger(keyEvent);
                 IComponentIdentifier id = null;
                 Component source = getEventSource(keyEvent);
                 id = m_recordHelper.getApplicationCompID();
                 int keycode = keyEvent.getKeyCode();
                 if (accepted != KeyAcceptor.CHECKMODE_KEY_COMB
                         && accepted != KeyAcceptor.CHECKCOMP_KEY_COMB) {
                     if ((!(source instanceof JTextComponent)
                             && !(source instanceof JComboBox)
                             && keycode != KeyEvent.VK_ALT
                             && keycode != KeyEvent.VK_CONTROL
                             && keycode != KeyEvent.VK_SHIFT)) {
                         m_recordActions.keyComboApp(id, keyEvent, keycode);
                     }
                     if (((source instanceof JTextField
                             || source instanceof JComboBox)
                                     && isSingleTrigger) 
                         || ((source instanceof JTextArea
                                 || source instanceof JTextPane
                                 || source instanceof JEditorPane)
                             && isMultiTrigger)) {
                         m_recordActions.replaceText(source);
                         m_recordActions.keyComboApp(id, keyEvent, keycode);
                     }
                 }
                 
                 // activates/deactivates "CheckMode" (Highlighting)
                 if (accepted == KeyAcceptor.CHECKMODE_KEY_COMB) {
                     if (source instanceof JTextComponent
                             || source instanceof JComboBox) {
                         m_recordActions.replaceText(source);
                     }
                     changeCheckModeState(ChangeAUTModeMessage.CHECK_MODE);
                 }
             } 
         }
     }
     
     /**
      * method for handling Focus-Events
      * @param event AWTEvent
      */
     protected void handleFocusEvent(AWTEvent event) { 
         Component source = getEventSource(event);
         FocusEvent fe = (FocusEvent)event;
         if (fe.getID() == FocusEvent.FOCUS_GAINED
                 && (source instanceof JTextComponent
                         || source instanceof JComboBox)) {
             String content = null;
             if (source instanceof JTextComponent) {
                 JTextComponent textComp = (JTextComponent)source;
                 content = textComp.getText();
             }
             if (source instanceof JComboBox) {
                 JComboBox cbx = (JComboBox)source;
                 content = m_recordHelper.getRenderedComboText(cbx);
             }
             m_recordActions.addTextCompContent(source, content);
             m_recordActions.setComponentParent(source.getParent());
             if (source.getParent() instanceof JTable) {
                 JTable tbl = (JTable)source.getParent();
                 m_recordActions.setTableRowColumn(tbl.getSelectedRow(),
                         tbl.getSelectedColumn());
             }
         }
         if (fe.getID() == FocusEvent.FOCUS_LOST
                 && (source instanceof JTextComponent
                         || source instanceof JComboBox)) {
             m_recordActions.replaceText(source);
         }
     }
     
     /**
      * method for handling Focus-Events
      * @param event AWTEvent
      */
     protected void handleWindowEvent(AWTEvent event) {
         Component window = getEventSource(event);
         IComponentIdentifier id = m_recordHelper.getApplicationCompID();        
         Action wfw = m_recordHelper.compSysToAction(id, "CompSystem.WaitForWindow"); //$NON-NLS-1$
         Action wfwtc = m_recordHelper.compSysToAction(id, "CompSystem.WaitForWindowToClose"); //$NON-NLS-1$
         if (window instanceof JFrame || window instanceof JDialog) {
             if ((event.getID() == WindowEvent.WINDOW_OPENED 
                     && getLastEvent().getID() != ComponentEvent.COMPONENT_SHOWN)
                     || event.getID() == ComponentEvent.COMPONENT_SHOWN) {
                 m_recordActions.waitForWindow(window, id, wfw);
             }
             if ((event.getID() == WindowEvent.WINDOW_CLOSED
                     && getLastEvent().getID() != ComponentEvent
                         .COMPONENT_HIDDEN)
                     || event.getID() == ComponentEvent.COMPONENT_HIDDEN) {
                 m_recordActions.waitForWindow(window, id, wfwtc);
             }  
         }      
     }
     
     /**
      * special handling for the JList-object in a JComboBox
      * @param source the source to check
      * @return true, if a parent of the given source is a ComboPopup
      */
     private boolean isComboPopup(Component source) {
         if (source instanceof ComboPopup) {
             return true;
         } else if (source instanceof JList) {
             Component parent = source.getParent();
             while (parent != null) {
                 if (parent instanceof ComboPopup) {
                     return true;
                 }
                 parent = parent.getParent();
             }
         }
         return false;
     }
     
     /**
      * @return Returns the selectedComponent.
      */
     protected Component getSelectedComponent() {
         return m_selectedComponent;
     }
 
     /**
      * @param selectedComponent The clicked/pressed Component to set.
      */
     protected void setSelectedComponent(Component selectedComponent) {
         m_selectedComponent = selectedComponent;
     }
     
     /**
      * @return Returns the selectedComponentParent.
      */
     protected Component getSelectedComponentParent() {
         return m_selectedComponentParent;
     }
 
     /**
      * @param selectedComponentParent The clicked/pressed ComponentParent to set.
      */
     protected void setSelectedComponentParent(
             Component selectedComponentParent) {
         m_selectedComponentParent = selectedComponentParent;
     }
         
     /**
      * {@inheritDoc}
      */
     public void update() {
         // do nothing
     }    
     /**
      * {@inheritDoc}
      */
     public boolean highlightComponent(IComponentIdentifier comp) {
         // do nothing
         return true;
     }
 } 
