 package kkckkc.jsourcepad.ui;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Rectangle;
 import java.awt.Toolkit;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.io.File;
 import java.io.FileFilter;
 import java.util.Arrays;
 import java.util.Comparator;
 
 import javax.annotation.PostConstruct;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JComponent;
 import javax.swing.JPopupMenu;
 import javax.swing.JTree;
 import javax.swing.UIDefaults;
 import javax.swing.UIManager;
 import javax.swing.border.EmptyBorder;
 import javax.swing.event.EventListenerList;
 import javax.swing.event.TreeModelEvent;
 import javax.swing.event.TreeModelListener;
 import javax.swing.plaf.TreeUI;
 import javax.swing.tree.DefaultTreeCellRenderer;
 import javax.swing.tree.TreeModel;
 import javax.swing.tree.TreePath;
 
 import kkckkc.jsourcepad.model.DocList;
 import kkckkc.jsourcepad.model.Project;
 import kkckkc.jsourcepad.model.Window;
 import kkckkc.jsourcepad.util.action.ActionGroup;
 import kkckkc.jsourcepad.util.action.MenuFactory;
 import kkckkc.jsourcepad.util.ui.PopupUtils;
 
 import org.springframework.beans.factory.annotation.Autowired;
 
 
 
 public class ProjectViewImpl extends JTree implements ProjectView, MouseListener {
 	private static final long serialVersionUID = 1L;
 	private DocList docList;
 	private Project project;
 	private Window window;
 	private FileTreeModel model;
 	
 	@Autowired
 	public void setProject(Project project) {
 	    this.project = project;
     }
 	
 	@Autowired
 	public void setWindow(Window window) {
 	    this.window = window;
     }
 	
 	@Autowired
 	public void setDocList(DocList docList) {
 	    this.docList = docList;
     }
 	
 	@PostConstruct
 	public void init() {
 		model = new FileTreeModel(project.getProjectDir()); 
         
         setModel(model);
 		setCellRenderer(new FileTreeCellRenderer());
         setShowsRootHandles(true);
 
//        try {
//			setUI((TreeUI) Class.forName("kkckkc.jsourcepad.theme.TigerTreeUI").newInstance());
//		} catch (InstantiationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IllegalAccessException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (ClassNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
 
         setFont(getFont().deriveFont(11f));
 
 		addMouseListener(this);
 		
 		ActionGroup actionGroup = window.getActionManager().getActionGroup("project-context-menu");
 		JPopupMenu jpm = new MenuFactory().buildPopup(actionGroup, null);
 		PopupUtils.bind(jpm, this, false);
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		int selRow = getRowForLocation(e.getX(), e.getY());
 		TreePath selPath = getPathForLocation(e.getX(), e.getY());
 		if (selRow != -1) {
 			if (e.getClickCount() == 2) {
 				File f = (File) selPath.getLastPathComponent();
 				if (f.isFile()) {
 					docList.open(f);
 				}
 			}
 		}
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent e) { }
 
 	@Override
 	public void mouseExited(MouseEvent e) { }
 
 	@Override
 	public void mousePressed(MouseEvent e) { }
 
 	@Override
 	public void mouseReleased(MouseEvent e) { }
 
 	@Override
     public void insertFile(File file) {
 		model.insertFile(file);
     }
 
 	@Override
     public void refresh(File file) {
 		model.refresh(file);
     }
 
 	@Override
     public void refresh() {
 		model.refresh();
     }
 
 	
 	public static class FileTreeModel implements TreeModel {
 
 		public static final Comparator<File> COMPARATOR = new Comparator<File>() {
 			public int compare(File f1, File f2) {
 				if (f1.isDirectory() && ! f2.isDirectory()) return -1;
 				if (! f1.isDirectory() && f2.isDirectory()) return 1;
 				return f1.getName().compareTo(f2.getName());
 			}
 		};
 
 		private final EventListenerList listeners = new EventListenerList();
 
 		private final File root;
 		private final FileFilter filter;
 
 		public FileTreeModel(final File root) {
 			this(root, null);
 		}
 
 		public FileTreeModel(final File root, final FileFilter filter) {
 			this.root = root;
 			this.filter = filter;
 		}
 
 		public Object getRoot() {
 			return root;
 		}
 
 		public Object getChild(Object parent, int index) {
 			File[] children = getChildren((File) parent);
 			if (children == null) return null;
 			return children[index];
 		}
 
 		public int getChildCount(Object parent) {
 			File[] children = getChildren((File) parent);
 			if (children == null) return 0;
 			return children.length;
 		}
 
 		public boolean isLeaf(Object node) {
 			return !((File) node).isDirectory();
 		}
 
 		public void valueForPathChanged(TreePath path, Object newValue) {}
 
 		public int getIndexOfChild(Object parent, Object child) {
 			if (parent == null || child == null) return -1;
 
 			File[] children = getChildren((File) parent);
 			if (children == null) return -1;
 			
 			for (int i = 0; i < children.length; i++) {
 				if (children[i].equals(child)) return i;
 			}
 			
 			return -1;
 		}
 
 		public void addTreeModelListener(TreeModelListener l) {
 			listeners.add(TreeModelListener.class, l);
 		}
 
 		public void removeTreeModelListener(TreeModelListener l) {
 			listeners.remove(TreeModelListener.class, l);
 		}
 
 		
 		public void insertFile(final File node) {
 			File parent = node.getParentFile();
 			int index = getIndexOfChild(parent, node); 
 			if (index == -1) return;
 
 			TreePath p = createTreePath(parent);
 			TreeModelEvent ev = new TreeModelEvent(this, p, new int[] { index }, new File[] { node });
 			fireTreeNodesInserted(ev);
 		}
 
 		public void removeFile(final File node) {
 			File parent = node.getParentFile();
 			int index = getIndexOfChild(parent, node); 
 			if (index == -1) return;
 
 			fireTreeNodesRemoved(
 					new TreeModelEvent(this, createTreePath(parent), new int[] { index }, new File[] { node }));
 		}
 
 		public void refresh() {
 			refresh(root);
 		}
 
 		public void refresh(File node) {
 			fireTreeStructureChanged(new TreeModelEvent(this, createTreePath(node), null, null));
 		}
 
 		private void fireTreeNodesInserted(final TreeModelEvent evt) {
 			for (TreeModelListener l : listeners.getListeners(TreeModelListener.class)) {
 				l.treeNodesInserted(evt);
 			}
 		}
 		
 		private File[] getChildren(final File parent) {
 			File[] children = parent.listFiles(filter);
 			if (children != null) Arrays.sort(children, COMPARATOR);
 			return children;
 		}
 
 		private TreePath createTreePath(File node) {
 			File[] elements = createPath(node, 1);
 			if (elements == null) return null;
 			return new TreePath(elements);
 		}
 
 		private File[] createPath(final File node, int level) {
 			File[] path;
 			if (root.equals(node)) {
 				path = new File[level];
 				path[0] = root;
 			} else if (node != null) {
 				path = createPath(node.getParentFile(), level + 1);
 				if (path != null) path[path.length - level] = node;
 			} else {
 				path = null;
 			}
 			return path;
 		}
 
 		private void fireTreeNodesRemoved(final TreeModelEvent evt) {
 			for (TreeModelListener l : listeners.getListeners(TreeModelListener.class)) {
 				l.treeNodesRemoved(evt);
 			}
 		}
 
 		private void fireTreeStructureChanged(final TreeModelEvent evt) {
 			for (TreeModelListener l : listeners.getListeners(TreeModelListener.class)) {
 				l.treeStructureChanged(evt);
 			}
 		}
 	}
 
 	
 	public static class FileTreeCellRenderer extends DefaultTreeCellRenderer {
 
 		public FileTreeCellRenderer() {
 			 setBackgroundNonSelectionColor(null); 
 			 setBackgroundSelectionColor(null);
 			 setBorderSelectionColor(null);
 		}
 		
 	    public Component getTreeCellRendererComponent(
 	            final JTree tree,
 	            final Object value,
 	            final boolean selected,
 	            final boolean expanded,
 	            final boolean leaf,
 	            final int row,
 	            final boolean hasFocus) {
 	        super.getTreeCellRendererComponent(
 	                tree, ((File) value).getName(), selected, expanded, leaf, row, hasFocus);
 	        setOpaque(false);
 	        setBorder(new EmptyBorder(1, 0, 1, 0));
 	        setIcon(getNodeIcon((java.io.File) value));
 	        return this;
 	    }
 	    
 	    
 
 		public Color getBackground() {
 	    	return null;
 	    }
 	    
 		private Icon getNodeIcon(File file) {
 		    if (file.isDirectory()) {
 		    	return UIManager.getDefaults().getIcon("FileChooser.newFolderIcon");
 				// return new ImageIcon("/usr/share/icons/Human/16x16/places/folder.png");
 			} else {
 		    	return UIManager.getDefaults().getIcon("FileView.fileIcon");
 //				return new ImageIcon("/usr/share/icons/gnome/16x16/mimetypes/text-x-generic.png");
 			}
 	    }
 
 		
 	}
 
 	@Override
     public JComponent getJComponent() {
 		return this;
     }
 }
 
 
