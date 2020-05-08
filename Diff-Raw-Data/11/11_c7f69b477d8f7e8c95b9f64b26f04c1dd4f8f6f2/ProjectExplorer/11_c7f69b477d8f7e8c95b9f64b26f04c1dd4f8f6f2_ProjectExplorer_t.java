 package frontend;
 
 import java.awt.CardLayout;
 import java.awt.Font;
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.Transferable;
 import java.awt.datatransfer.UnsupportedFlavorException;
 import java.awt.dnd.DropTarget;
 import java.awt.dnd.DropTargetDragEvent;
 import java.awt.dnd.DropTargetDropEvent;
 import java.awt.dnd.DropTargetEvent;
 import java.awt.dnd.DropTargetListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTree;
 import javax.swing.SwingConstants;
 import javax.swing.tree.TreePath;
 
 import foobar.FileFooable;
 import frontend.FileSystemTreeModel.TreeFileObject;
 
 public class ProjectExplorer extends JPanel implements DropTargetListener {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -6000650682129841885L;
 
 	private final Bruno parentApp;
 	private final CardLayout layout;
 	// private final DropTarget dropTarget;
 	private final JTree fileTree;
 
 	public ProjectExplorer(final Bruno parentApp) {
 		this.parentApp = parentApp;
 
 		layout = new CardLayout();
 		setLayout(layout);
 
 		// Empty label
 		JLabel emptyLabel = new JLabel(
 				"<html><div style=\"text-align: center; color: #888888;\">Drag and drop folders<br /> here to get started</div></html>");
 		emptyLabel.setFont(new Font("Arial", Font.BOLD, 14));
 		emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
 		add(emptyLabel, "empty");
 
 		// File tree
 		fileTree = new JTree();
 
 		fileTree.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mousePressed(MouseEvent e) {
 				int selectedRow = fileTree.getRowForLocation(e.getX(), e.getY());
 				TreePath selectedPath = fileTree.getPathForLocation(e.getX(),
 						e.getY());
 				if (selectedRow != -1) {
 					if (e.getClickCount() == 1) {
 						// System.out.println("single click");
 					} else if (e.getClickCount() == 2) {
 						File file = ((TreeFileObject) selectedPath
 								.getLastPathComponent()).getFile();
 						if (file.isFile()) {
 							parentApp.openFile(file);
 						}
 					}
 				}
 			}
 		});
 
 		add(fileTree, "tree");
 
 		new DropTarget(this, this);
 	}
 
 	public void showFolder(File folder) {
 		fileTree.setModel(new FileSystemTreeModel(folder));
 		layout.show(this, "tree");
 		addFooables(folder);
 	}
 
 	private void addFooables(File f) {
 		if (f.isHidden()) {
 			return;
 		}
 		if (f.isFile()) {
			parentApp.getFoobar()
 					.addFooable(new FileFooable(parentApp, f));
 		} else {
 			for (File file : f.listFiles()) {
 				addFooables(file);
 			}
 		}
 	}
 
 	@Override
 	public void dragEnter(DropTargetDragEvent dtde) {
 
 	}
 
 	@Override
 	public void dragOver(DropTargetDragEvent dtde) {
 
 	}
 
 	@Override
 	public void dropActionChanged(DropTargetDragEvent dtde) {
 
 	}
 
 	@Override
 	public void dragExit(DropTargetEvent dte) {
 
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public void drop(DropTargetDropEvent dtde) {
 		int dropAction = dtde.getDropAction();
 		dtde.acceptDrop(dropAction);
 
 		try {
 			Transferable data = dtde.getTransferable();
 			if (data.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
 				List<File> files = (List<File>) data
 						.getTransferData(DataFlavor.javaFileListFlavor);
 				showFolder(files.get(0));
 			}
 		} catch (UnsupportedFlavorException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			dtde.dropComplete(true);
 			repaint();
 		}
 	}
 }
