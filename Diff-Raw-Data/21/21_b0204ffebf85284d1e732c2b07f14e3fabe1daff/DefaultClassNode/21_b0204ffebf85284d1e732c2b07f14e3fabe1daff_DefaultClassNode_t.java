 package net.sf.jlayercheck.util.modeltree;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.swing.tree.DefaultMutableTreeNode;
 
 import net.sf.jlayercheck.util.model.ClassDependency;
 
 /**
  * @see ClassNode
  * @author webmaster@earth3d.org
  *
  */
 public class DefaultClassNode extends DefaultMutableTreeNode implements ClassNode {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -478278521385757708L;
 
 	protected String classname;
 	
 	protected Set<ClassDependency> classDependencies = new HashSet<ClassDependency>();
 
 	public DefaultClassNode(String classname) {
 		this.classname = classname;
 		
 		setAllowsChildren(false);
 	}
 
 	public String getClassname() {
 		return classname;
 	}
 	
 	public String toString() {
 		return getClassname().replace("/", ".");
 	}
 
 	public String getName() {
 		return getClassname();
 	}
 
 	public void addClassDependency(ClassDependency cd) {
 		classDependencies.add(cd);
 	}
 
 	public Set<ClassDependency> getClassDependencies() {
 		return classDependencies;
 	}
 }
