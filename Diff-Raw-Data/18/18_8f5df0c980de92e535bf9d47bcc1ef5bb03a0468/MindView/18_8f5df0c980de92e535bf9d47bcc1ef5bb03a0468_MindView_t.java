 package excitedmind;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.util.logging.Logger;
 
 import javax.swing.*;
 import javax.swing.undo.AbstractUndoableEdit;
 import javax.swing.undo.UndoManager;
 
 import com.orientechnologies.orient.core.db.ODatabaseRecordThreadLocal;
 import com.orientechnologies.orient.core.db.record.ODatabaseRecord;
 import excitedmind.operators.EditAction;
 import excitedmind.operators.SimpleMindTreeAction;
 import prefuse.Display;
 import prefuse.Visualization;
 
 import prefuse.controls.ControlAdapter;
 import prefuse.controls.PanControl;
 import prefuse.controls.WheelZoomControl;
 import prefuse.controls.ZoomControl;
 import prefuse.controls.ZoomToFitControl;
 import prefuse.visual.NodeItem;
 import prefuse.visual.VisualItem;
 import prefuse.visual.sort.TreeDepthItemSorter;
 
 
 /**
  * Demonstration of a node-link tree viewer
  * 
  * @version 1.0
  * @author <a href="http://jheer.org">jeffrey heer</a>
  */
 public class MindView extends Display {
 
     Logger m_logger = Logger.getLogger(this.getClass().getName());
 
 	private VisualMindTree m_visMindTree;
 
 	MindTreeRenderEngine m_renderEngine;
     NodeItem m_clickedNode;
 
     static enum State {NORMAL, LINKING, MOVING};
 
     EditAction m_editAction = new EditAction(this);
 
     final static String sm_editActionName = "edit";
     final static String sm_removeActionName = "remove";
     final static String sm_undoActionName = "undo";
     final static String sm_redoActionName = "redo";
     final static String sm_addChildActionName = "addChild";
     final static String sm_addLinkActionName = "addLink";
     final static String sm_moveActionName = "move";
     final static String sm_prepareLinkActionName = "prepareLink";
     final static String sm_prepareMoveActionName = "prepareMove";
     final static String sm_toNormalActionAction = "toNormal";
 
     AbstractAction m_removeAction = new SimpleMindTreeAction(this) {
         @Override
         public AbstractUndoableEdit operateMindTree(ActionEvent e) {
             return m_visMindTree.removeCursorNode();
         }
     };
 
     AbstractAction m_undoAction = new AbstractAction() {
         @Override
         public void actionPerformed(ActionEvent e) {
             if (m_undoManager.canUndo()) {
                 m_undoManager.undo();
                 renderTree();
             }
         }
     };
 
     AbstractAction m_redoAction = new AbstractAction() {
 
         @Override
         public void actionPerformed(ActionEvent e) {
             if (m_undoManager.canRedo()) {
                 m_undoManager.redo();
                 renderTree();
             }
         }
     };
 
     AbstractAction m_addChildAction = new SimpleMindTreeAction(this) {
         @Override
         public AbstractUndoableEdit operateMindTree(ActionEvent e) {
             return m_visMindTree.addChild();
         }
     };
 
     AbstractAction m_prepareLinkAction = new AbstractAction() {
         @Override
         public void actionPerformed(ActionEvent actionEvent) {
             translate (State.LINKING);
         }
     };
 
     AbstractAction m_prepareMoveAction = new AbstractAction() {
         @Override
         public void actionPerformed(ActionEvent actionEvent) {
             translate (State.MOVING);
         }
     };
 
     AbstractAction m_toNormalAction = new AbstractAction() {
         @Override
         public void actionPerformed(ActionEvent actionEvent) {
             translate (State.NORMAL);
         }
     };
 
     ActionMap m_mindActionMap = new ActionMap();
 
     private void setEnabledAllMindActions(boolean enabled)
     {
         Object [] keys = m_mindActionMap.keys();
         for (Object key : keys) {
             m_mindActionMap.get(key).setEnabled(enabled);
         }
     }
 
     private void setEnabledMindActions(String keys[], boolean enabled)
     {
         for (Object key : keys) {
             m_mindActionMap.get(key).setEnabled(enabled);
         }
     }
 
     State m_state = State.NORMAL;
 
     void translate(State newState)
     {
         if (m_state == newState) {
             return;
         }
 
         m_state = newState;
 
         switch (newState) {
             case NORMAL:
                 setEnabledAllMindActions(true);
                 break;
             case LINKING:
                 setEnabledAllMindActions(false);
                 setEnabledMindActions(new String []{sm_toNormalActionAction}, true);
                 break;
             case MOVING:
                 setEnabledAllMindActions(false);
                 setEnabledMindActions(new String []{sm_toNormalActionAction}, true);
                 break;
         }
 
     }
 
 	public MindView(String path, Object rootId) {
 		super(new Visualization());
 		setSize(700, 600);
 
 		setHighQuality(true);
 
 		m_visMindTree = new VisualMindTree(path, rootId, m_vis);
 
 		setItemSorter(new TreeDepthItemSorter());
 
 		m_renderEngine = new MindTreeRenderEngine(this, VisualMindTree.sm_treeGroupName);
 
 		setMouseControlListener();
 		setKeyControlListener();
 
 		renderTree();
 	}
 
 	public void renderTree() {
 		m_renderEngine.run();
 	}
 
 	private void setMouseControlListener() {
 		addControlListener(new ZoomToFitControl());
 		addControlListener(new ZoomControl());
 		addControlListener(new WheelZoomControl());
 		addControlListener(new PanControl());
 		addControlListener(new ControlAdapter() {
 
 			public void itemEntered(VisualItem item, MouseEvent e) {
 				if (m_visMindTree.isNode(item)) {
                     if (m_state == State.NORMAL) {
                         m_visMindTree.setCursor((NodeItem)item);
                         renderTree();
                     }
 				}
 
 			}
 
 			public void itemClicked(VisualItem item, MouseEvent e) {
 				m_logger.info("mouse Clicked");
 
 				if (m_visMindTree.isNode(item)) {
                     m_clickedNode = (NodeItem)item;
 
 					m_renderEngine.holdItem(item);
 
                     AbstractUndoableEdit undoer;
 
 
                     ODatabaseRecordThreadLocal.INSTANCE.set(m_visMindTree.m_dbTree.m_graph.getRawGraph());
                     switch (m_state) {
                         case NORMAL:
                             undoer = m_visMindTree.ToggleFoldNode();
                             break;
                         case LINKING:
                             undoer = m_visMindTree.addReference(m_clickedNode);
                             break;
                         case MOVING:
                             undoer = m_visMindTree.resetParent(m_clickedNode);
                             break;
                         default:
                             assert(false);
                             undoer = null;
                     }
                     m_undoManager.addEdit(undoer);
 
 					renderTree();
 				}
 			}
 		});
 	}
 
 	public void setKeyControlListener() {
         m_mindActionMap.put(sm_editActionName, m_editAction);
         m_mindActionMap.put(sm_removeActionName, m_removeAction);
         m_mindActionMap.put(sm_addChildActionName, m_addChildAction);
 
         m_mindActionMap.put(sm_undoActionName, m_undoAction);
         m_mindActionMap.put(sm_redoActionName, m_redoAction);
 
         m_mindActionMap.put(sm_prepareLinkActionName, m_prepareLinkAction);
         m_mindActionMap.put(sm_prepareMoveActionName, m_prepareMoveAction);
         m_mindActionMap.put(sm_toNormalActionAction, m_toNormalAction);
 
 
         ActionMap defaultActionMap = getActionMap();
         m_mindActionMap.setParent(defaultActionMap);
         setActionMap(m_mindActionMap);
 
         InputMap inputMap = getInputMap();
         inputMap.put(KeyStroke.getKeyStroke("F2"), sm_editActionName);
         inputMap.put(KeyStroke.getKeyStroke('d'), sm_removeActionName);
         inputMap.put(KeyStroke.getKeyStroke('i'), sm_addChildActionName);
 
         inputMap.put(KeyStroke.getKeyStroke('u'), sm_undoActionName);
         inputMap.put(KeyStroke.getKeyStroke('r'), sm_redoActionName);
         inputMap.put(KeyStroke.getKeyStroke('l'), sm_prepareLinkActionName);
         inputMap.put(KeyStroke.getKeyStroke('m'), sm_prepareMoveActionName);
         inputMap.put(KeyStroke.getKeyStroke("ESCAPE"), sm_toNormalActionAction);
 	}
 
 	private UndoManager m_undoManager = new UndoManager();
 
 	public UndoManager getUndoManager() {
 		return m_undoManager;
 	}
 
 	public VisualMindTree getVisMindTree() {
 		return m_visMindTree;
 	}
 
 } // end of class TreeMap
