 package frontend;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import javax.swing.event.TreeModelListener;
 import javax.swing.tree.TreeModel;
 import javax.swing.tree.TreePath;
 
 /**
  * A filesystem version of the Swing Tree model.
  * 
  * @author samuelainsworth
  * 
  */
 public class FileSystemTreeModel implements TreeModel {
 
 	private final TreeFileObject root;
 
 	public FileSystemTreeModel(File root) {
 		this.root = new TreeFileObject(root);
 	}
 
 	@Override
 	public Object getRoot() {
 		return root;
 	}
 
 	@Override
 	public Object getChild(Object parent, int index) {
 		return ((TreeFileObject) parent).getChildren().get(index);
 	}
 
 	@Override
 	public int getChildCount(Object parent) {
 		return ((TreeFileObject) parent).getChildren().size();
 	}
 
 	@Override
 	public boolean isLeaf(Object node) {
 		File nodeFile = ((TreeFileObject) node).getFile();
 		return nodeFile.isFile();
 	}
 
 	@Override
 	public void valueForPathChanged(TreePath path, Object newValue) {
 
 	}
 
 	@Override
 	public int getIndexOfChild(Object parent, Object child) {
 		List<TreeFileObject> children = ((TreeFileObject) parent).getChildren();
 		File file = ((TreeFileObject) child).getFile();
 
 		for (int i = 0; i < children.size(); i++) {
 			if (children.get(i).equals(file)) {
 				return i;
 			}
 		}
 
 		return -1;
 	}
 
 	@Override
 	public void addTreeModelListener(TreeModelListener l) {
 
 	}
 
 	@Override
 	public void removeTreeModelListener(TreeModelListener l) {
 
 	}
 
 	/**
 	 * An internal data structure for representing the file structure. Consists
 	 * of a file and TreeFileObjects of its children. The children are
 	 * lazily-initialized in order to avoid the slowdown of recursively
 	 * searching and building out the entire directory structure. Ignores custom
 	 * hidden files.
 	 * 
 	 * @author samuelainsworth
 	 * 
 	 */
 	public static class TreeFileObject {
 		private final File file;
 		private List<TreeFileObject> children;
 
 		public TreeFileObject(File file) {
 			this.file = file;
 		}
 
 		@Override
 		public String toString() {
 			return getFile().getName();
 		}
 
 		public File getFile() {
 			return file;
 		}
 
 		public List<TreeFileObject> getChildren() {
 			if (children == null) {
 				// Plain files have an empty children list
 				children = new ArrayList<>();
 
 				// Directories have a list of their children
 				if (file.isDirectory()) {
 					File[] files = file.listFiles();
 					List<File> childrenFiles = Arrays.asList(files);
 					for (File f : childrenFiles) {
						if (ProjectExplorer.belongsInProjectExplorer(f)) {
 							children.add(new TreeFileObject(f));
 						}
 					}
 				}
 			}
 			return children;
 		}
 
 		@Override
 		public int hashCode() {
 			final int prime = 31;
 			int result = 1;
 			result = prime * result + ((file == null) ? 0 : file.hashCode());
 			return result;
 		}
 
 		@Override
 		public boolean equals(Object obj) {
 			if (this == obj)
 				return true;
 			if (obj == null)
 				return false;
 			if (getClass() != obj.getClass())
 				return false;
 			TreeFileObject other = (TreeFileObject) obj;
 			if (file == null) {
 				if (other.file != null)
 					return false;
 			} else if (!file.equals(other.file))
 				return false;
 			return true;
 		}
 
 	}
 
 }
