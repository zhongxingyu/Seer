 package org.openxdata.designer.dnd;
 
 import java.io.IOException;
 
 import org.apache.pivot.collections.List;
 import org.apache.pivot.collections.Sequence.Tree.Path;
 import org.apache.pivot.wtk.Component;
 import org.apache.pivot.wtk.Display;
 import org.apache.pivot.wtk.DropAction;
 import org.apache.pivot.wtk.LocalManifest;
 import org.apache.pivot.wtk.Manifest;
 import org.apache.pivot.wtk.Point;
 import org.apache.pivot.wtk.TreeView;
 import org.openxdata.designer.DesignerApp;
 
 public class DropTarget implements org.apache.pivot.wtk.DropTarget {
 
 	private DesignerApp application;
 	private TreeView designTree;
 
 	public void setApplication(DesignerApp application) {
 		this.application = application;
 	}
 
 	@Override
 	public DropAction dragEnter(Component component, Manifest dragContent,
 			int supportedDropActions, DropAction userDropAction) {
		designTree = (TreeView) component;
 		DropAction action = null;
 		if (dragContent.containsFileList()
 				&& DropAction.COPY.isSelected(supportedDropActions)) {
 			action = DropAction.COPY;
 		} else if (dragContent.containsValue("node")) {
 			action = DropAction.MOVE;
 		}
 		return action;
 	}
 
 	@Override
 	public void dragExit(Component component) {
 		designTree = null;
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public DropAction dragMove(Component component, Manifest dragContent,
 			int supportedDropActions, int x, int y, DropAction userDropAction) {
 		if (dragContent.containsValue("node")) {
 			List<Object> treeData = (List<Object>) designTree.getTreeData();
 			Path sourcePath = null;
 			try {
 				sourcePath = (Path) dragContent.getValue("path");
 			} catch (IOException e) {
 				e.printStackTrace();
 				return null;
 			}
 			Path targetPath = designTree.getNodeAt(y);
 			if (DropPolicy.allowDrop(treeData, sourcePath, targetPath))
 				return DropAction.MOVE;
 		} else if (dragContent.containsFileList())
 			return DropAction.COPY;
 		return null;
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public DropAction userDropActionChange(Component component,
 			Manifest dragContent, int supportedDropActions, int x, int y,
 			DropAction userDropAction) {
 		if (dragContent.containsValue("node")) {
 			List<Object> treeData = (List<Object>) designTree.getTreeData();
 			Path sourcePath = null;
 			try {
 				sourcePath = (Path) dragContent.getValue("path");
 			} catch (IOException e) {
 				e.printStackTrace();
 				return null;
 			}
 			Path targetPath = designTree.getNodeAt(y);
 			if (DropPolicy.allowDrop(treeData, sourcePath, targetPath))
 				return DropAction.MOVE;
 		} else if (dragContent.containsFileList())
 			return DropAction.COPY;
 
 		return null;
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public DropAction drop(Component component, Manifest dragContent,
 			int supportedDropActions, int x, int y, DropAction userDropAction) {
 
 		// Oddly, in this method x,y are display relative
 
 		Display display = component.getDisplay();
 		Point dropLocation = component.mapPointFromAncestor(display, x, y);
 		DropAction dropAction = null;
 		if (dragContent.containsFileList()) {
 			return application.drop(dragContent);
 		} else if (dragContent.containsValue("node")) {
 			List<Object> treeData = (List<Object>) designTree.getTreeData();
 			Path sourcePath;
 			try {
 				sourcePath = (Path) dragContent.getValue("path");
 			} catch (IOException e) {
 				e.printStackTrace();
 				return null;
 			}
 			Path targetPath = designTree.getNodeAt(dropLocation.y);
 			if (DropPolicy.allowDrop(treeData, sourcePath, targetPath)) {
 				((LocalManifest) dragContent)
 						.putValue("targetPath", targetPath);
 				return application.drop(dragContent);
 			}
 		}
 
 		this.dragExit(component);
 
 		return dropAction;
 	}
 }
