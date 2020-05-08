 package gui.util;
 
 import java.awt.Component;
 import java.io.File;
 import java.io.FileFilter;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.NoSuchElementException;
 
 import javax.swing.Icon;
 import javax.swing.JLabel;
 import javax.swing.JScrollPane;
 import javax.swing.JTree;
 import javax.swing.event.TreeSelectionListener;
 import javax.swing.filechooser.FileSystemView;
 import javax.swing.tree.DefaultTreeCellRenderer;
 import javax.swing.tree.TreeNode;
 
 
 /**
  * JScrollpane das einen JTree beinhaltet der die 
  * Ordner des Dateisystems anzeigt.
  */
 public class FileTreePanel extends JScrollPane {
 	
 	/** SelectionListener des jTrees */
 	TreeSelectionListener treeListener;
 	
 	/** Dateisystemwurzeln (Laufwerke) */
 	File[] roots;
 
 	/** Wurzelknoten des Trees */
 	FileTreeNode rootTreeNode;
 
 	/** JTree der das Dateisystem zeigt */
 	private JTree tree;
 
 	protected static FileSystemView fsv = FileSystemView.getFileSystemView();
 	
 	
 	/** Konstruktor */
 	public FileTreePanel() {
 		init();
 	}
 	
 	/** @return Jtree dieses Panes */
 	public JTree getTree(){
 		return tree;
 	}
 
 	/** initialisiert Filetreepanel */
 	protected void init() {
 		roots = File.listRoots();
 		rootTreeNode = new FileTreeNode(roots);
 		tree = new JTree(rootTreeNode);
 		tree.setCellRenderer(new FileTreeCellRenderer());
 		tree.setName("My Computer");
 		tree.setRootVisible(true);
 		this.setViewportView(tree);
 	}
 	
 	/** laed den JTree neu */
 	public void reloadTree(){
 		roots = File.listRoots();
 		rootTreeNode = new FileTreeNode(roots);
 		tree = new JTree(rootTreeNode);
 		tree.setCellRenderer(new FileTreeCellRenderer());
 		tree.setName("My Computer");
 		tree.setRootVisible(true);
 		tree.addTreeSelectionListener(treeListener);
 		this.setViewportView(tree);
 		this.validate();
 	}
 	
 	
 	public void setTreeSelectionListener(TreeSelectionListener listener){
 		this.treeListener = listener;
 		tree.addTreeSelectionListener(listener);
 	}
 
 	/** CellRenderer fuer den JTree */
 	private static class FileTreeCellRenderer extends DefaultTreeCellRenderer {
 
 		private static Map<String, Icon> iconCache = new HashMap<String, Icon>();
 
 		private static Map<File, String> rootNameCache = new HashMap<File, String>();
 
 
 		public Component getTreeCellRendererComponent(JTree tree, Object value,
 				boolean sel, boolean expanded, boolean leaf, int row,
 				boolean hasFocus) {
 			FileTreeNode ftn = (FileTreeNode) value;
 			File file = ftn.file;
 			String filename = "";
 			if(file != null) {
 				if(ftn.isFileSystemRoot) {
 					filename = rootNameCache.get(file);
 					if(filename == null) {
 						filename = fsv.getSystemDisplayName(file);
 						rootNameCache.put(file, filename);
 					}
 				} else {
 					filename = file.getName();
 				}
 			}
 			JLabel result = (JLabel) super.getTreeCellRendererComponent(tree,
 					filename, sel, expanded, leaf, row, hasFocus);
 			if(file != null) {
 				Icon icon = iconCache.get(filename);
 				if(icon == null) {
 					icon = fsv.getSystemIcon(file);
 					iconCache.put(filename, icon);
 				}
 				result.setIcon(icon);
 			}
 			return result;
 		}
 	}
 
 	/** Klasse fuer einen Knoten im JTree */
 	public static class FileTreeNode implements TreeNode {
 
 		private File file;
 
 		private File[] children;
 
 		private FileTreeNode parent;
 
 		private boolean isFileSystemRoot;
 		
 		private static FileFilter fileFilter = new FileFilter() {
 			
 			@Override
 			public boolean accept(File file) {
 				return file.isDirectory() && !file.isHidden() && file.listFiles() != null;
 			}
 		};
 
 
 		public FileTreeNode(File file, boolean isFileSystemRoot, FileTreeNode parent) {
 			this.file = file;
 			this.isFileSystemRoot = isFileSystemRoot;
 			this.parent = parent;
 			this.children = this.file.listFiles(fileFilter);
 			if(this.children == null)
 				this.children = new File[0];
 		}
 

 		public FileTreeNode(File[] children) {
 			this.file = null;
 			this.parent = null;
 			this.children = children;
 		}
 
 
 		public Enumeration children() {
 			final int elementCount = this.children.length;
 			return new Enumeration() {
 
 				int count = 0;
 
 
 				public boolean hasMoreElements() {
 					return this.count < elementCount;
 				}
 
 
 				public File nextElement() {
 					if(this.count < elementCount) {
 						return FileTreeNode.this.children[this.count++];
 					}
 					throw new NoSuchElementException("Vector Enumeration");
 				}
 			};
 		}
 		
 
 		public boolean getAllowsChildren() {
 			return true;
 		}
 
 
 		public TreeNode getChildAt(int childIndex) {
 			return new FileTreeNode(this.children[childIndex],
 					this.parent == null, this);
 		}
 
 
 		public int getChildCount() {
 			return this.children.length;
 		}
 
 
 		public int getIndex(TreeNode node) {
 			FileTreeNode ftn = (FileTreeNode) node;
 			for(int i = 0; i < this.children.length; i++) {
 				if(ftn.file.equals(this.children[i]))
 					return i;
 			}
 			return -1;
 		}
 
 
 		public FileTreeNode getParent() {
 			return this.parent;
 		}
 
 
 		public boolean isLeaf() {
 			return (this.getChildCount() == 0);
 		}
 		
 		
 		public File getFile(){
 			return file;
 		}
 	}
 }
