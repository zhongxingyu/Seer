 package org.vpac.grisu.frontend.view.swing.files.virtual;
 
 import java.awt.Point;
 import java.awt.dnd.DnDConstants;
 import java.awt.dnd.DropTarget;
 import java.awt.dnd.DropTargetDragEvent;
 import java.awt.dnd.DropTargetDropEvent;
 import java.awt.dnd.DropTargetEvent;
 import java.awt.dnd.DropTargetListener;
 import java.util.Set;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.swing.JFrame;
 import javax.swing.SwingUtilities;
 import javax.swing.WindowConstants;
 import javax.swing.tree.TreePath;
 
 import org.apache.log4j.Logger;
 import org.netbeans.swing.outline.Outline;
 import org.vpac.grisu.X;
 import org.vpac.grisu.control.ServiceInterface;
 import org.vpac.grisu.frontend.control.fileTransfers.FileTransaction;
 import org.vpac.grisu.frontend.control.fileTransfers.FileTransactionManager;
 import org.vpac.grisu.frontend.view.swing.files.FileTransactionStatusDialog;
 import org.vpac.grisu.frontend.view.swing.files.GridFileTransferHandler;
 import org.vpac.grisu.model.FileManager;
 import org.vpac.grisu.model.GrisuRegistryManager;
 import org.vpac.grisu.model.dto.GridFile;
 
 public class GridFileTreeDropTarget implements DropTargetListener {
 
 	class OpenFolderTask extends TimerTask {
 
 		private final TreePath path;
 		private final Outline outline;
 
 		public OpenFolderTask(Outline outline, TreePath path) {
 			this.outline = outline;
 			this.path = path;
 		}
 
 		@Override
 		public void run() {
 			outline.expandPath(path);
 		}
 	}
 
 	static final Logger myLogger = Logger
 			.getLogger(GridFileTreeDropTarget.class.getName());
 
 	public static int WAIT_DRAG_UNTIL_OPEN_FOLDER = 2000;
 
 	private final ServiceInterface si;
 	private final FileManager fm;
 
 	private final DropTarget target;
 	private final Outline targetOutline;
 
 	private int[] lastSelectedRows = null;
 
 	private TreePath currentlySelectedPath = null;
 	private TimerTask openFolderTask;
 
 	private final Timer timer = new Timer();
 
 	public GridFileTreeDropTarget(ServiceInterface si, Outline tree) {
 		this.si = si;
 		this.fm = GrisuRegistryManager.getDefault(si).getFileManager();
 		targetOutline = tree;
 		target = new DropTarget(targetOutline, this);
 
 	}
 
 	public void dragEnter(DropTargetDragEvent dtde) {
 
 		Outline outline = (Outline) ((dtde.getDropTargetContext()
 				.getComponent()));
 
 		lastSelectedRows = outline.getSelectedRows();
 
 		// TreeNode node = getNodeForEvent(dtde);
 		// if (node.isLeaf()) {
 		// dtde.rejectDrag();
 		// } else {
 		// // start by supporting move operations
 		// // dtde.acceptDrag(DnDConstants.ACTION_MOVE);
 		// dtde.acceptDrag(dtde.getDropAction());
 		// }
 	}
 
 	public void dragExit(DropTargetEvent dte) {
 
 		if (openFolderTask != null) {
 			openFolderTask.cancel();
 		}
 
 		Outline outline = (Outline) ((dte.getDropTargetContext().getComponent()));
 
 		selectLastSelectedItems(outline);
 	}
 
 	public void dragOver(DropTargetDragEvent dtde) {
 
 		Outline outline = (Outline) ((dtde.getDropTargetContext()
 				.getComponent()));
 
 		int x = new Double(dtde.getLocation().getX()).intValue();
 		int y = new Double(dtde.getLocation().getY()).intValue();
 		TreePath p = outline.getClosestPathForLocation(x, y);
 
 		if (p.getLastPathComponent() instanceof GridFileTreeNode) {
 			GridFile dropTargetFile = ((GridFileTreeNode) p
 					.getLastPathComponent()).getGridFile();
 
 			Point point = new Point(x, y);
 			selectFolderAndChildren(outline, p, point);
 
 			if (dropTargetFile.isFolder()) {
 
 				if (!outline.getOutlineModel().getTreePathSupport()
 						.hasBeenExpanded(p)) {
 					if ((currentlySelectedPath == null)
 							|| !currentlySelectedPath.equals(p)) {
 						currentlySelectedPath = p;
 						if (openFolderTask != null) {
 							openFolderTask.cancel();
 						}
 						openFolderTask = new OpenFolderTask(outline, p);
 						timer.schedule(openFolderTask,
 								WAIT_DRAG_UNTIL_OPEN_FOLDER);
 					}
 				} else {
 					if (openFolderTask != null) {
 						openFolderTask.cancel();
 					}
 				}
 
 				if (dropTargetFile.isVirtual()) {
 
 					if (dropTargetFile.getUrls().size() == 1) {
 
 						// if (!dropTargetFile.getUrl().startsWith(
 						// ServiceInterface.VIRTUAL_GRID_PROTOCOL_NAME)) {
 						// dtde.acceptDrag(DnDConstants.ACTION_COPY);
 
 						// for now
 						dtde.rejectDrag();
 					} else {
 						dtde.rejectDrag();
 					}
 				} else {
 					dtde.acceptDrag(DnDConstants.ACTION_COPY);
 				}
 
 			} else {
 				if (openFolderTask != null) {
 					openFolderTask.cancel();
 				}
 				GridFile parent = ((GridFileTreeNode) p.getParentPath()
 						.getLastPathComponent()).getGridFile();
 
 				if (parent.isVirtual()) {
 					dtde.rejectDrag();
 				} else {
 					dtde.acceptDrag(DnDConstants.ACTION_COPY);
 				}
 
 			}
 
 		}
 
 	}
 
 	public void drop(DropTargetDropEvent dtde) {
 		if (openFolderTask != null) {
 			openFolderTask.cancel();
 		}
 
 		final Outline outline = (Outline) ((dtde.getDropTargetContext()
 				.getComponent()));
 
 		int x = new Double(dtde.getLocation().getX()).intValue();
 		int y = new Double(dtde.getLocation().getY()).intValue();
 		TreePath p = outline.getClosestPathForLocation(x, y);
 
 		GridFile target = null;
 
 		if (p.getLastPathComponent() instanceof GridFileTreeNode) {
 
 			GridFile dropTargetFile = ((GridFileTreeNode) p
 					.getLastPathComponent()).getGridFile();
 
 			if (dropTargetFile.isFolder()) {
 				// put file into that folder
 				dtde.acceptDrop(DnDConstants.ACTION_COPY);
 				target = dropTargetFile;
 			} else {
 				// get parent folder of selected file
 				openFolderTask.cancel();
 				GridFile parent = ((GridFileTreeNode) p.getParentPath()
 						.getLastPathComponent()).getGridFile();
 
 				if (parent.isVirtual()) {
 					// don't drop
 					dtde.rejectDrop();
 					return;
 				} else {
 					dtde.acceptDrop(DnDConstants.ACTION_COPY);
 					target = parent;
 				}
 			}
 
 		} else {
 			dtde.rejectDrop();
 			return;
 		}
 
 		X.p("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX Copying files.....");
 
 		Set<GridFile> files = null;
 		try {
 			files = (Set<GridFile>) dtde.getTransferable().getTransferData(
 					GridFileTransferHandler.SET_DATA_FLAVOR);
 		} catch (Exception e) {
 			myLogger.equals(e);
 			dtde.dropComplete(false);
 			return;
 		}
 
 		final FileTransaction ft = new FileTransaction(fm, files, target, true);
 		FileTransactionManager.getDefault(si).addFileTransfer(ft);
 
 		SwingUtilities.invokeLater(new Thread() {
 
 			@Override
 			public void run() {
 				final JFrame frame = (JFrame) SwingUtilities.getRoot(outline);
 				final FileTransactionStatusDialog ftd = new FileTransactionStatusDialog(
 						frame, ft);
 
 				ftd.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
 				ftd.setVisible(true);
 			}
 
 		});
 
 		dtde.dropComplete(true);
 		selectLastSelectedItems(outline);
 
 		// Point pt = dtde.getLocation();
 		// DropTargetContext dtc = dtde.getDropTargetContext();
 		// JTree tree = (JTree) dtc.getComponent();
 		// TreePath parentpath = tree.getClosestPathForLocation(pt.x, pt.y);
 		// DefaultMutableTreeNode parent = (DefaultMutableTreeNode) parentpath
 		// .getLastPathComponent();
 		// if (parent.isLeaf()) {
 		// dtde.rejectDrop();
 		// return;
 		// }
 		//
 		// try {
 		// Transferable tr = dtde.getTransferable();
 		// DataFlavor[] flavors = tr.getTransferDataFlavors();
 		// for (DataFlavor flavor : flavors) {
 		// if (tr.isDataFlavorSupported(flavor)) {
 		// dtde.acceptDrop(dtde.getDropAction());
 		// TreePath p = (TreePath) tr.getTransferData(flavor);
 		// DefaultMutableTreeNode node = (DefaultMutableTreeNode) p
 		// .getLastPathComponent();
 		// DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
 		// model.insertNodeInto(node, parent, 0);
 		// dtde.dropComplete(true);
 		// return;
 		// }
 		// }
 		// dtde.rejectDrop();
 		// } catch (Exception e) {
 		// e.printStackTrace();
 		// dtde.rejectDrop();
 		// }
 	}
 
 	public void dropActionChanged(DropTargetDragEvent dtde) {
 		// X.p("Drop action changed");
 	}
 
 	private GridFile getSelectedTargetFolder(TreePath path) {
 		GridFile dropTargetFile = ((GridFileTreeNode) path
 				.getLastPathComponent()).getGridFile();
 
 		if (dropTargetFile.isFolder()) {
 			return dropTargetFile;
 		} else {
 			TreePath parentPath = path.getParentPath();
 			GridFileTreeNode parentNode = (GridFileTreeNode) parentPath
 					.getLastPathComponent();
 
 			GridFile file = (GridFile) parentNode.getUserObject();
 
 			return file;
 		}
 	}
 
 	private void selectFolderAndChildren(Outline outline, TreePath path,
 			Point point) {
 
 		outline.getSelectionModel().clearSelection();
 
 		GridFile folder = getSelectedTargetFolder(path);
 		// if (dropTargetFile.isFolder()) {
 
 		int row = outline.rowAtPoint(point);
 
 		if (outline.getOutlineModel().getTreePathSupport()
 				.hasBeenExpanded(path)) {
 			int childs = folder.getChildren().size();
 
 			for (int i = 0; i < outline.getRowCount(); i++) {
 				try {
 					GridFileTreeNode node = ((GridFileTreeNode) (outline
 							.getOutlineModel().getValueAt(i, 0)));
 
 					GridFile gf = (GridFile) (node.getUserObject());
 
 					if (gf.getUrl().startsWith(folder.getUrl())) {
 						outline.addRowSelectionInterval(i, i);
 					}
 				} catch (Exception e) {
 					myLogger.error(e);
 				}
 			}
 
 			// outline.setRowSelectionInterval(row, row + childs);
 		} else {
 			outline.setRowSelectionInterval(row, row);
 		}
 
 		// } else {
 		// TreePath parentPath = path.getParentPath();
 		// GridFileTreeNode parentNode = (GridFileTreeNode) parentPath
 		// .getLastPathComponent();
 		//
 		// int row = -1;
 		// for (int i = 0; i < outline.getRowCount(); i++) {
 		//
 		// GridFileTreeNode node = (GridFileTreeNode) outline.getValueAt(
 		// i, 0);
 		// if (parentNode.equals(node)) {
 		// row = i;
 		// break;
 		// }
 		// }
 		// Object o = outline.getOutlineModel().getChild(
 		// parentPath.getLastPathComponent(), 0);
 		//
 		// GridFile dropTargetFileParent = ((GridFileTreeNode) parentPath
 		// .getLastPathComponent()).getGridFile();
 		//
 		// if (outline.getOutlineModel().getTreePathSupport()
 		// .hasBeenExpanded(parentPath)) {
 		// int childs = dropTargetFileParent.getChildren().size();
 		//
 		// outline.setRowSelectionInterval(row, row + childs);
 		// } else {
 		// outline.setRowSelectionInterval(row, row);
 		// }
 		//
 		// }
 
 	}
 
 	private void selectLastSelectedItems(Outline outline) {
 
 		outline.getSelectionModel().clearSelection();
 
 		if (lastSelectedRows == null) {
 			return;
 		}
 
 		for (int lastSelectedRow : lastSelectedRows) {
 			outline.addRowSelectionInterval(lastSelectedRow, lastSelectedRow);
 		}
 		lastSelectedRows = null;
 	}
 }
