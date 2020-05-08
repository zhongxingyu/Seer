 package org.genericsystem.myadmin.beans;
 
 import java.util.ArrayList;
 import java.util.List;

 import javax.enterprise.context.RequestScoped;
 import javax.enterprise.event.Event;
 import javax.enterprise.event.Observes;
 import javax.faces.event.PhaseEvent;
 import javax.inject.Inject;
 import javax.inject.Named;

 import org.jboss.seam.faces.event.qualifier.After;
 import org.jboss.seam.faces.event.qualifier.InvokeApplication;
 import org.richfaces.component.UITree;
 import org.richfaces.event.TreeSelectionChangeEvent;
 
 @Named
 @RequestScoped
 public class TreeBean {
 
 	@Inject
 	// @TreeSelection
 	private Event<TreeSelectionEvent> launcher;
 
 	private TreeSelectionEvent event;
 
 	public void change(TreeSelectionChangeEvent selectionChangeEvent) {
 		List<Object> selection = new ArrayList<Object>(selectionChangeEvent.getNewSelection());
 		if (!selection.isEmpty()) {
 			Object currentSelectionKey = selection.get(0);
 			UITree tree = (UITree) selectionChangeEvent.getSource();
 			Object storedKey = tree.getRowKey();
 			tree.setRowKey(currentSelectionKey);
 			event = new TreeSelectionEvent(tree.getId(), tree.getRowData());
 			tree.setRowKey(storedKey);
 		}
 	}
 
 	public void fire(@Observes @InvokeApplication @After PhaseEvent e) {
 		if (event != null) {
 			launcher.fire(event);
 			event = null;
 		}
 	}
 
 	public static class TreeSelectionEvent {
 		private final String id;
 		private final Object object;
 
 		public TreeSelectionEvent(String id, Object object) {
 			this.id = id;
 			this.object = object;
 		}
 
 		public String getId() {
 			return id;
 		}
 
 		public Object getObject() {
 			return object;
 		}
 
 	}
 
 }
