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
 
 import java.awt.Component;
 import java.awt.Rectangle;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.ComboBoxEditor;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JDialog;
 import javax.swing.JEditorPane;
 import javax.swing.JFrame;
 import javax.swing.JList;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPopupMenu;
 import javax.swing.JTabbedPane;
 import javax.swing.JTable;
 import javax.swing.JTextArea;
 import javax.swing.JTextPane;
 import javax.swing.JTree;
 import javax.swing.text.JTextComponent;
 import javax.swing.tree.TreePath;
 
 import org.apache.commons.lang.StringUtils;
 import org.eclipse.jubula.communication.message.CAPRecordedMessage;
 import org.eclipse.jubula.communication.message.MessageCap;
 import org.eclipse.jubula.communication.message.MessageParam;
 import org.eclipse.jubula.communication.message.RecordActionMessage;
 import org.eclipse.jubula.communication.message.ShowObservInfoMessage;
 import org.eclipse.jubula.rc.common.AUTServer;
 import org.eclipse.jubula.rc.common.Constants;
 import org.eclipse.jubula.rc.common.exception.NoIdentifierForComponentException;
 import org.eclipse.jubula.rc.common.logger.AutServerLogger;
 import org.eclipse.jubula.rc.common.util.MatchUtil;
 import org.eclipse.jubula.tools.constants.CharacterConstants;
 import org.eclipse.jubula.tools.constants.InputConstants;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.jubula.tools.exception.CommunicationException;
 import org.eclipse.jubula.tools.objects.IComponentIdentifier;
 import org.eclipse.jubula.tools.utils.StringParsing;
 import org.eclipse.jubula.tools.xml.businessmodell.Action;
 import org.eclipse.jubula.tools.xml.businessmodell.Param;
 
 
 
 
 /**
  * @author BREDEX GmbH
  * @created 23.08.2004
  */
 public class RecordActions {    
     
     /** the logger */
     private static AutServerLogger log = new AutServerLogger(
         RecordListener.class);
     
     /** parent of component */
     private Component m_parent;
     
     /** set to true if Event is Selection */
     private boolean m_isSelEvent;
     
     /** row and column of Table */
     private int[] m_tableRowColumn = {0, 0};
     
     /** map to store contents of TextComponents */
     private Map m_map = new HashMap();
     
     /** map to store logical Names */
     private Map m_logNameMap = new HashMap();
     
     /** map to store technical Names */
     private Map m_techNameMap = new HashMap();
     
     /** mouse button for popups */
     private int m_popupMouseBtn = MouseEvent.BUTTON3;
 
     /** The RecordHelper */
     private RecordHelper m_recordHelper = new RecordHelper();
     
     /** 
      * default constructor
      */
     public RecordActions() {
         // do nothing
     }
     
     /**
      * contents of TextComponents
      * @return map 
      */
     public Map getTextCompContent() {
         return m_map;
     }
     /**
      * contents of TextComponents
      * @param map The message data
      */
     public void setTextCompContent(Map map) {
         m_map = map;
     }    
     /**
      * stores contents of TextComponents
      * @param source TextComponent
      * @param content content to store
      */
     public void addTextCompContent(Component source, String content) {
         m_map.put(source, content);
     }
     
     /**
      * @return parent of Component 
      */
     public Component getComponentParent() {
         return m_parent;
     }
     /**
      * set to true if event is selection, false otherwise
      * @param isSelEvent boolean
      */
     public void setSelectionState(boolean isSelEvent) {
         m_isSelEvent = isSelEvent;
     }
     
     /**
      * @return true if event is selection, false otherwise
      */
     public boolean isSelectionEvent() {
         return m_isSelEvent;
     }
     /**
      * set parent of Component
      * @param parent Component
      */
     public void setComponentParent(Component parent) {
         m_parent = parent;
     } 
     
     /**
      * @return row and column of Component 
      */
     public int[] getTableRowColumn() {
         return m_tableRowColumn;
     }
     /**
      * row and column of Component
      * @param row int
      * @param column int
      */
     public void setTableRowColumn(int row, int column) {
         m_tableRowColumn[0] = row;
         m_tableRowColumn[1] = column;
     }
     
     /**
      * popup mouse button
      * @return map 
      */
     public int getPopupMouseButton() {
         return m_popupMouseBtn;
     }
     /**
      * set popup mouse button
      * @param popupMouseBtn popup mouse button
      */
     public void setPopupMouseButton(int popupMouseBtn) {
         m_popupMouseBtn = popupMouseBtn;
     } 
 
     /**
      * select item
      * @param jlst JList
      * @param id IComponentIdentifier
      * @param a Action
      */
     protected void selectListValues(JList jlst, IComponentIdentifier id,
             Action a) {
         String[] entries = m_recordHelper.getRenderedListValues(jlst);
         String entr = StringConstants.EMPTY;
         for (int i = 0; i < entries.length; i++) {
             String item = StringParsing.maskAndSingleQuoteText(
                     entries[i], StringParsing.LISTCOMBOMASK);
             entr = entr.concat(item);
             if (i < (entries.length - 1)) {
                 entr = entr.concat(","); //$NON-NLS-1$
             }
         }
         List lstValues = new LinkedList();
         lstValues.add(entr);
         lstValues.add(Constants.REC_OPERATOR);
         lstValues.add(Constants.REC_SEARCH_MODE);
         lstValues.add(Constants.REC_EXT_SELECTION);
         
         String logName = createLogicalName(jlst, id);
 
         createCAP(a, id, lstValues, logName);
     }
     
     /**
      * select node by textpath
      * @param jtre JTree
      * @param path TreePath
      * @param id IComponentIdentifier
      * @param clickcount int
      * @param a Action
      */
     protected void selectNode(JTree jtre, TreePath path,
             IComponentIdentifier id, Action a, int clickcount) {
         String nodepath = m_recordHelper.treepathToTextpath(jtre, path);
         int count = clickcount;
         if (count < 1) {
             count = 1;
         }
         String clCount = String.valueOf(count);
         List treValues = new LinkedList();
         treValues.add(Constants.REC_SEARCH_MODE);
         treValues.add("0"); //$NON-NLS-1$
         treValues.add(nodepath);                
         treValues.add(Constants.REC_OPERATOR);
         treValues.add(clCount);
         treValues.add(String.valueOf(InputConstants.MOUSE_BUTTON_LEFT));
         treValues.add(Constants.REC_EXT_SELECTION);
         
         String logName = createLogicalName(jtre, id);
         
         createCAP(a, id, treValues, logName);
     }
     
     /**
      * collapse or Expand Tree
      * @param jtre JTree
      * @param path Treepath
      * @param id IComponentIdentifier
      * @param collOrExp String
      */
     protected void collExpTree(JTree jtre, TreePath path,
             IComponentIdentifier id, String collOrExp) {
         String nodepath = null;
         nodepath = m_recordHelper.treepathToTextpath(jtre, path);
         Action a = m_recordHelper.compSysToAction(id, collOrExp);
         java.util.List treValues = new LinkedList();
         treValues.add(Constants.REC_SEARCH_MODE);
         treValues.add("0"); //$NON-NLS-1$
         treValues.add(nodepath);                
         treValues.add(Constants.REC_OPERATOR);
         
         String logName = createLogicalName(jtre, id);
         
         createCAP(a, id, treValues, logName); 
     }
     
     /**
      * select tab
      * @param jtpn JTabbedPane
      * @param id IComponentIdentifier
      * @param a Action
      */
     protected void selectTab(JTabbedPane jtpn, IComponentIdentifier id,
             Action a) {
         List tpnValues = new LinkedList();
         String tpnTitle = StringParsing.singleQuoteText(jtpn
                 .getTitleAt(jtpn.getSelectedIndex()));
         tpnValues.add(tpnTitle);
         tpnValues.add(Constants.REC_OPERATOR);
         
         String logName = createLogicalName(jtpn, id);
         
         createCAP(a, id, tpnValues, logName);
     }
     
     /**
      * select value
      * @param jcbx JComboBox
      * @param id IComponentIdentifier
      * @param a Action
      */
     protected void selectCbxValue(JComboBox jcbx, IComponentIdentifier id,
             Action a) {
         String cbxText = StringParsing.singleQuoteText(m_recordHelper
                 .getRenderedComboText(jcbx));
         if (cbxText.equals(StringConstants.EMPTY) || cbxText == null) {
             cbxText = Constants.EMPTY_ITEM;
         }
         List cbxValues = new LinkedList();
         cbxValues.add(cbxText);
         cbxValues.add(Constants.REC_OPERATOR);
         cbxValues.add(Constants.REC_SEARCH_MODE);
         
         String logName = createLogicalName(jcbx, id);
         
         createCAP(a, id, cbxValues, logName);
     }
     /**
      * select cell
      * @param jtbl JTable
      * @param id IComponentIdentifier
      * @param clickcount int
      * @param a Action
     * @param mouseButton the mouse button used
      */
     protected void selectTableCell(JTable jtbl, IComponentIdentifier id,
            int clickcount, Action a, int mouseButton) {
         int row = jtbl.getSelectedRow();
         int column = jtbl.getSelectedColumn();
         int count = clickcount;
         if (count < 1) {
             count = 1;
         }
         String clCount = (new Integer(count)
             .toString());
         String rowStr = (new Integer(row + 1)).toString();
         String columnStr = (new Integer(column + 1)).toString();
         List tblValues = new LinkedList();
         tblValues.add(rowStr);
         tblValues.add(MatchUtil.EQUALS);
         tblValues.add(columnStr);
         tblValues.add(MatchUtil.EQUALS);
         tblValues.add(clCount);
         tblValues.add("50"); //$NON-NLS-1$
         tblValues.add("percent"); //$NON-NLS-1$
         tblValues.add("50"); //$NON-NLS-1$
         tblValues.add("percent"); //$NON-NLS-1$
         tblValues.add(Constants.REC_EXT_SELECTION);
        tblValues.add(String.valueOf(mouseButton));
         
         String logName = createLogicalName(jtbl, id);
         
         createCAP(a, id, tblValues, logName); 
     }
     
     
     /**
      * select MenuItem
      * @param mi JMenuItem
      */
     protected void selectMenuItem(JMenuItem mi) {
         Component comp = mi;
         boolean isMenuBarItem = false;
         String logName = null;
         while (comp.getParent() != null) {
             if (comp.getParent() instanceof JPopupMenu) {
                 JPopupMenu jpm = (JPopupMenu)comp.getParent();
                 comp = jpm.getInvoker();
             } else {
                 comp = comp.getParent();
             }
 
             if (comp instanceof JMenuBar) {
                 isMenuBarItem = true;
                 break;
             }
             if (comp instanceof JComponent 
                     && !(comp instanceof JMenu)) {
                 break;
             }
         }
         IComponentIdentifier id = null;
         Action a = new Action();                    
         if (comp instanceof JComponent) {
             String pth = m_recordHelper.getPath(mi);            
             List parValues = new LinkedList();
             parValues.add(pth);
             parValues.add(Constants.REC_OPERATOR);
             
             if (isMenuBarItem) {
                 id = m_recordHelper.getMenuCompID();
                 a = m_recordHelper.compSysToAction(id, "CompSystem.SelectMenuItem"); //$NON-NLS-1$
             } else {
                 try {
                     id = ComponentHandler
                         .getIdentifier(comp);
                     a = m_recordHelper.compSysToAction(id, "CompSystem.PopupSelectByTextPathNew"); //$NON-NLS-1$
                     logName = createLogicalName(comp, id);
                     parValues.add((new Integer(m_popupMouseBtn)).toString());
                 } catch (NoIdentifierForComponentException
                         nifce) {
                     // no identifier for the component, log this as an error
                     log.error("no identifier for '" + comp); //$NON-NLS-1$
                 }
             }
             
             if (logName != null) {
                 createCAP(a, id, parValues, logName); 
             } else {
                 createCAP(a, id, parValues);
             }
         }
     }
     
     /**
      * creates CAP for KeyCominations like ENTER, BACKSPACE, SHIFT+TAB etc
      * @param id IComponentIdentifier
      * @param ke KeyEvent
      * @param keycode int
      */
     protected void keyComboApp(IComponentIdentifier id,
             KeyEvent ke, int keycode) {
         Action a = new Action();
         a = m_recordHelper.compSysToAction(id, "CompSystem.KeyStroke"); //$NON-NLS-1$
         
         List parameterValues = new LinkedList();            
         String modifierKey = null;
         if (ke.getModifiers() == 0) {
             modifierKey = "none"; //$NON-NLS-1$
         } else {
             modifierKey = RecordHelper.getModifierName(ke.getModifiers());
             if (modifierKey != null) {
                 modifierKey = modifierKey.toLowerCase().replace('+', ' ');
             }            
         }        
         
         String baseKey = null;
         baseKey = RecordHelper.getKeyName(keycode);
         if (baseKey == null) {
             baseKey = KeyEvent.getKeyText(keycode).toUpperCase();
         }
         
         if (baseKey != null && modifierKey != null) {
             parameterValues.add(modifierKey);
             parameterValues.add(baseKey);
             
             createCAP(a, id, parameterValues);  
         }    
     }
     
     /**
      * creates CAP for Click on Graphics Component
      * @param id IComponentIdentifier
      * @param me MouseEvent
      * @param source Component
      */
     protected void clickGraphComp(IComponentIdentifier id, MouseEvent me,
             Component source) {
         if ((source instanceof JTable
                 || source instanceof JList
                 || source instanceof JTree
                 || source instanceof JTabbedPane)) {
             clickInComponent(id, me, source);
         } else {
             int clickcount = me.getClickCount();
             if (clickcount < 1) {
                 clickcount = 1;
             }
             String clCount = (new Integer(clickcount)
                 .toString());
             String mbutton = (new Integer(me.getButton())
                 .toString());
             Action a = m_recordHelper.compSysToAction(id, "CompSystem.Click"); //$NON-NLS-1$
             List parValues = new LinkedList();
             parValues.add(clCount);
             parValues.add(mbutton);
             
             String logName = createLogicalName(source, id);
     
             createCAP(a, id, parValues, logName);
         }
     }
     
     /**
      * creates CAP for Click in Component
      * @param id IComponentIdentifier
      * @param me MouseEvent
      * @param source Component
      */
     protected void clickInComponent(IComponentIdentifier id, MouseEvent me,
             Component source) {
         int clickcount = me.getClickCount();
         if (clickcount < 1) {
             clickcount = 1;
         }
         String clCount = (new Integer(clickcount)
             .toString());
         String mbutton = (new Integer(me.getButton())
             .toString());
         Action a = m_recordHelper.compSysToAction(id, "CompSystem.ClickDirect"); //$NON-NLS-1$          
         Rectangle bounds = me.getComponent().getBounds();
         int percentX = (int)(me.getX() / bounds.getWidth() * 100);
         String percentXString = new Integer(percentX).toString();
         int percentY = (int)(me.getY() / bounds.getHeight() * 100);
         String percentYString = new Integer(percentY).toString();
         String units = Constants.REC_UNITS;
         List parValues = new LinkedList();
         parValues.add(clCount);
         parValues.add(mbutton);
         parValues.add(percentXString);
         parValues.add(units);
         parValues.add(percentYString);
         parValues.add(units);        
         
         String logName = createLogicalName(source, id);  
 
         createCAP(a, id, parValues, logName); 
     } 
 
     
     /**
      * creates CAP for Actions Replace Text
      * @param source Component
      */
     protected void replaceText(Component source) {
         Component src = source;
         Component parent = getComponentParent() != null 
             ? getComponentParent() : src.getParent();
         if (parent instanceof JComboBox) {
             src = parent;
         }
         String text = null;
         boolean isEditable = false;
         boolean isCbxItem = false;
         boolean isSupported = true;
         if (src instanceof JTextComponent) {
             JTextComponent jtf = (JTextComponent)src;
             text = jtf.getText();
             isEditable = jtf.isEditable();
             if ((source instanceof JTextArea || source instanceof JTextPane
                 || source instanceof JEditorPane)
                     && (text.indexOf(CharacterConstants.LINEFEED) != -1
                             || text.indexOf(CharacterConstants.RETURN) != -1)) {
                 isSupported = false;
                 sendInfoMessage(Constants.REC_MULTILINE_MSG);
             }
             if (parent instanceof JTable) {
                 JTable tbl = (JTable)parent;
                 replaceTableText(src, tbl, text);
                 return;
             }
         }
         if (src instanceof JComboBox) {
             JComboBox cbx = (JComboBox)src;
             isEditable = cbx.isEditable();
             if (isEditable) {
                 ComboBoxEditor cbxEditor = cbx.getEditor();
                 text = cbxEditor.getItem().toString();
                 String[] cbxItems = m_recordHelper.getRenderedComboItems(cbx);
                 for (int i = 0; i < cbxItems.length; i++) {
                     String item = cbxItems[i];
                     if (item.equals(text)) {                        
                         isCbxItem = true;
                     }
                 }
             } else {
                 return;
             }
         }
         if (text.length() > Constants.REC_MAX_STRING_LENGTH) {
             ShowObservInfoMessage infoMsg =
                 new ShowObservInfoMessage(Constants.REC_MAX_STRING_MSG);
             try {
                 AUTServer.getInstance().getServerCommunicator().send(infoMsg);
             } catch (CommunicationException e) { 
                 // no log available here
             }
             return;            
         }
         if (m_map.get(source) != null
                 && !(text.equals(m_map.get(source).toString()))
                 && isSupported && isEditable && !isCbxItem) {
             m_map.put(src, text);
             IComponentIdentifier id = null;
             try {
                 id = ComponentHandler.getIdentifier(src);
                 Action a = new Action();
                 a = m_recordHelper.compSysToAction(id, "CompSystem.InputText"); //$NON-NLS-1$        
                 List parameterValues = new LinkedList();
                 text = StringParsing.singleQuoteText(text);                
                 parameterValues.add(text);                
                 String logName = createLogicalName(src, id);                 
                 createCAP(a, id, parameterValues, logName);
             } catch (NoIdentifierForComponentException nifce) {
                 // no identifier for the component, log this as an error
                 log.error("no identifier for '" + src); //$NON-NLS-1$
             }
         }
     }
     
     /**
      * creates CAP for Actions Replace Text (Specified by Cell) on Table
      * @param src Component 
      * @param tbl JTable
      * @param text String
      */
     private void replaceTableText(Component src, JTable tbl, String text) {
         String txt = StringParsing.singleQuoteText(text);
         if (!(txt.equals(m_map.get(src).toString()))) {
             IComponentIdentifier id = null;
             try {
                 id = ComponentHandler.getIdentifier(tbl);
                 Action a = new Action();
                 a = m_recordHelper.compSysToAction(id, "CompSystem.ReplaceTextInTableCellNew"); //$NON-NLS-1$        
 
                 //int row = tbl.getSelectedRow();
                 //int column = tbl.getSelectedColumn();
                 int row = getTableRowColumn()[0];
                 int column = getTableRowColumn()[1];
                 String rowStr = (new Integer(row + 1)).toString();
                 String columnStr = (new Integer(column + 1)).toString();
                 List parameterValues = new LinkedList();
                 if (txt.equals(StringConstants.EMPTY) || txt == null) {
                     txt = "''"; //$NON-NLS-1$
                 }
                 parameterValues.add(txt);
                 parameterValues.add(rowStr);
                 parameterValues.add(columnStr);
                 
                 String logName = createLogicalName(tbl, id);
                 
                 createCAP(a, id, parameterValues, logName);            
                 m_map.put(src, txt);  
             } catch (NoIdentifierForComponentException nifce) {
                 // no identifier for the component, log this as an error
                 log.error("no identifier for '" + tbl); //$NON-NLS-1$
             }
         }      
     }
 
     /**
      * select item
      * @param window Component
      * @param id IComponentIdentifier
      * @param a Action
      */
     protected void waitForWindow(Component window, IComponentIdentifier id,
             Action a) {
         String title = null;
         if (window instanceof JFrame) {
             JFrame jf = (JFrame)window;
             title = StringParsing.singleQuoteText(jf.getTitle());
         }
         if (window instanceof JDialog) {
             JDialog jd = (JDialog)window;
             title = StringParsing.singleQuoteText(jd.getTitle());
         }
         if (title != null && !(title.equals(StringConstants.EMPTY))) {
             String operator = Constants.REC_OPERATOR;
             String delay = new Integer(Constants.REC_WAIT_DELAY).toString();
             
             String timeout = null;
             long timestamp = AUTServer.getInstance().getObservTimestamp();
 
             if (timestamp == 0) {
                 timeout = new Integer(Constants.REC_WAIT_TIMEOUT).toString();
             } else {
                 long timeoutLong = (System.currentTimeMillis() - timestamp) 
                     + 10000;
                 double timeoutDouble = (
                         Math.ceil(timeoutLong / 5000.0)) * 5000.0;
                 int timeoutInt = (int)timeoutDouble;
                 timeout = new Integer(timeoutInt).toString();
             }
             
             java.util.List winValues = new LinkedList();
             winValues.add(title);
             winValues.add(operator);
             winValues.add(timeout);
             winValues.add(delay);
             createCAP(a, id, winValues);
         }
     }
     
     /**
      * records and executes a cap
      * @param a Action
      * @param id IComponentIdentifier
      * @param parValues List of values
      */
     private void createCAP(Action a,
             IComponentIdentifier id, List parValues) {
         String defaultName = "default"; //$NON-NLS-1$
         createCAP(a, id, parValues, defaultName);
     }
     
     /**
      * records and executes a cap
      * @param a Action
      * @param id IComponentIdentifier
      * @param parValues List of values
      * @param logName Logical Name
      */
     private void createCAP(Action a,
             IComponentIdentifier id, List parValues, String logName) {
         MessageCap messageCap = new MessageCap();        
         
         // setup Action in MessageCap
         messageCap.setMethod(a.getMethod());
         
         messageCap.setAction(a);
         
         // setup ComponentIdentifier in MessageCap
         messageCap.setCi(id);
         
         // setup parameters in MessageCap
         List parameterValues = parValues;
         List params = a.getParams();
 
         for (int i = 0; i < params.size(); i++) {
             Param param = (Param) params.get(i);
             MessageParam messageParam = new MessageParam();
             messageParam.setType(param.getType());
             String emptyString = StringConstants.EMPTY;
             String value = 
                 emptyString.equals(parameterValues.get(i)) ? null 
                     : (String)parameterValues.get(i);            
             messageParam.setValue(value);
             messageCap.addMessageParam(messageParam);
             
         }
         if (StringUtils.isEmpty(id.getComponentName())) {
             messageCap.setLogicalName(logName);
             messageCap.sethasDefaultMapping(true);
         }
         CAPRecordedMessage capRecMessage = new CAPRecordedMessage(messageCap);
                 
         try {            
             RecordActionMessage message =
                 new RecordActionMessage(capRecMessage);            
             AUTServer.getInstance().getServerCommunicator().send(message);
         } catch (CommunicationException e) { 
             // no log available here
         }
 
         AUTServer.getInstance().setObservTimestamp(System.currentTimeMillis());
     }
     
     /**
      * creates logical name for component
      * @param c Component
      * @param id IComponentIdentifier
      * @return logical Name
      */
     private String createLogicalName(Component c, IComponentIdentifier id) {
         
         String logName = (String)m_techNameMap.get(c);
         
         if (logName == null) {
             logName = m_recordHelper.generateLogicalName(c, id);
             
             if (logName != null) {
                 IComponentIdentifier id2 = 
                     (IComponentIdentifier)m_logNameMap.get(logName);
                 if (m_logNameMap.containsKey(logName)) {
                     if (!(m_recordHelper.isCiEqual(id, id2))) {
                         Collection col = m_techNameMap.values();
                         Iterator it = col.iterator();
                         int counter = 0;
                         while (it.hasNext()) {
                             String name = (String)it.next();
                             if (name.equals(logName)
                                     || name.equals(
                                             logName + "_" + (counter + 1))) { //$NON-NLS-1$
                                 counter++;
                             }
                         }
                         logName = logName + "_" + counter; //$NON-NLS-1$
                     }
                 }
                 m_logNameMap.put(logName, id);
                 m_techNameMap.put(c, logName);
             }
         }   
         
         return logName;
     }
     
     /**
      * send info Message to Observation Console
      * @param info String info Message
      */
     private void sendInfoMessage(String info) {
         ShowObservInfoMessage infoMsg =
             new ShowObservInfoMessage(info);
         try {
             AUTServer.getInstance().getServerCommunicator().send(infoMsg);
         } catch (CommunicationException e) { 
             // no log available here
         }        
     }
 } 
