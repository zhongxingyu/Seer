 package org.yaoqiang.bpmn.editor.swing;
 
 import java.awt.Component;
 import java.awt.IllegalComponentStateException;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.EventObject;
 
 import javax.swing.JPopupMenu;
 import javax.swing.JSplitPane;
 import javax.swing.JTabbedPane;
 import javax.swing.SwingUtilities;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import org.yaoqiang.bpmn.editor.BPMNEditor;
 import org.yaoqiang.bpmn.editor.action.EditorActions;
 import org.yaoqiang.bpmn.editor.action.ModelActions;
 import org.yaoqiang.graph.swing.GraphComponent;
 import org.yaoqiang.graph.view.Graph;
 import org.yaoqiang.util.Resources;
 
 import com.mxgraph.swing.mxGraphComponent;
 
 /**
  * TabbedPane
  * 
  * @author Shi Yaoqiang(shi_yaoqiang@yahoo.com)
  */
 public class TabbedPane extends JTabbedPane implements MouseListener, ChangeListener, ActionListener {
 
 	private static final long serialVersionUID = 1L;
 
 	private boolean maximized = false;
 
 	public TabbedPane(final BaseEditor editor) {
 		addMouseListener(this);
 		addChangeListener(this);
 	}
 
 	public void actionPerformed(ActionEvent e) {
 
 	}
 
 	public void mouseClicked(MouseEvent e) {
 		if (e.getID() == MouseEvent.MOUSE_CLICKED && e.getClickCount() == 2) {
 			BaseEditor editor = getEditor(e);
 			JSplitPane mainPane = editor.getMainPane();
 			if (maximized) {
 				mainPane.setDividerLocation(mainPane.getLastDividerLocation());
 				maximized = false;
 			} else {
 				mainPane.setDividerLocation(0);
 				maximized = true;
 			}
 		}
 	}
 
 	public void mousePressed(MouseEvent e) {
 		mouseReleased(e);
 	}
 
 	public void mouseReleased(MouseEvent e) {
 		if (e.isPopupTrigger()) {
 			TabbedPane centerPane = (TabbedPane) e.getSource();
 			Component c = centerPane.getSelectedComponent();
 			if (c instanceof mxGraphComponent) {
 				Point pt = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), c);
 				JPopupMenu popup = new JPopupMenu();
 				if (getEditor(e).getGraphComponent() == c) {
 					popup.add(getEditor(e).bind(Resources.get("editDiagramName"), ModelActions.getAction(ModelActions.DIAGRAM_NAME),
 							"/org/yaoqiang/bpmn/editor/images/edit.png"));
 				} else if (getEditor(e).getGraphComponents().containsValue(c)) {
 					popup.add(getEditor(e).bind(Resources.get("close"), EditorActions.getCloseCalledProcessAction(c.getName()),
 							"/org/yaoqiang/bpmn/editor/images/delete.png"));
 				}
 				try {
 					popup.show(c, pt.x, pt.y);
 				} catch (IllegalComponentStateException ex) {
 				}
 			}
 			e.consume();
 		}
 	}
 
 	public void mouseEntered(MouseEvent e) {
 	}
 
 	public void mouseExited(MouseEvent e) {
 	}
 
 	public void stateChanged(ChangeEvent e) {
 		BPMNEditor editor = (BPMNEditor) getEditor(e);
 		Component c = ((JTabbedPane) e.getSource()).getSelectedComponent();
		if (c != null && c.getName() != null && c.getName().equals("BPMNView")) {
 			editor.getBpmnView().refreshView(editor.getGraphComponent().getGraph());
		} else if (editor != null && c!=null && c instanceof GraphComponent) {
 			boolean reset = false;
 			GraphComponent graphComponent = (GraphComponent) c;
 			if (editor.getCurrentGraphComponent() != c) {
 				reset = true;
 			}
 			editor.setCurrentGraphComponent(graphComponent);
 			Graph graph = graphComponent.getGraph();
 			graph.getView().setCurrentRoot(graphComponent.getLastViewRoot());
 			graph.clearSelection();
 			graph.refresh();
 			editor.refreshGraphOverview();
 			if (reset) {
 				editor.getUndoManager().clear();
 			}
 
 		}
 	}
 
 	public void setTabTitle(String name, String title) {
 		if (name == null) {
 			setTitleAt(0, title);
 			return;
 		}
 		for (int i = 0; i < getComponentCount(); i++) {
 			Component com = getComponentAt(i);
 			if (name.equals(com.getName())) {
 				setTitleAt(i, title);
 				return;
 			}
 		}
 	}
 
 	public String getTabTitle(String name, String defaultTitle) {
 		if (name == null) {
 			return getTitleAt(0);
 		}
 		for (int i = 0; i < getComponentCount(); i++) {
 			Component com = getComponentAt(i);
 			if (name.equals(com.getName())) {
 				return getTitleAt(i);
 			}
 		}
 		return defaultTitle;
 	}
 
 	public BaseEditor getEditor(EventObject e) {
 		if (e.getSource() instanceof Component) {
 			Component component = (Component) e.getSource();
 
 			while (component != null && !(component instanceof BaseEditor)) {
 				component = component.getParent();
 			}
 
 			return (BaseEditor) component;
 		}
 
 		return null;
 	}
 
 }
