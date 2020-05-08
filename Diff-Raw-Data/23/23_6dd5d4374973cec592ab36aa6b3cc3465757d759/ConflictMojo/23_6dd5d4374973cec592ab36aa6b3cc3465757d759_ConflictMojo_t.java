 package de.mytoys.maven.plugins.debug;
 
 import java.io.Writer;
 
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.shared.dependency.tree.DependencyNode;
 import org.apache.maven.shared.dependency.tree.traversal.BuildingDependencyNodeVisitor;
 
 import org.apache.maven.shared.dependency.tree.traversal.DependencyNodeVisitor;
 
 /**
  * Displays the dependency tree for conflicting artifacts without requiring conflicts to be resolved
  * first.
  *
  * @author <a href="mailto:leonard.ehrenfried@web.de">Leonard Ehrenfried</a>
  * @goal conflict
  */
 public class ConflictMojo extends AbstractDebugMojo {
 	
 	boolean errorFound = false;
 
 	public DependencyNodeVisitor getSerializingDependencyNodeVisitor(Writer writer) {
 	 return new ConflictDependencyNodeVisitor(writer);
 	}
 	
 	@Override
 	protected String serializeDependencyTree(DependencyNode rootNode) {
 		ConflictDependencyNodeVisitor conflictVisitor = new ConflictDependencyNodeVisitor(null);
 		DependencyNodeVisitor buildingVisitor = new BuildingDependencyNodeVisitor(conflictVisitor);
 		rootNode.accept(buildingVisitor);
 		
 		String conflictInfo = conflictVisitor.getConflictInfo();
 		if (errorFound == false) errorFound = conflictVisitor.conflictsFound;
 		return conflictInfo;
 	}
 
 	@Override
	protected void postprocessResult() throws MojoExecutionException {
	 	if (errorFound && failOnConflict) {
 		    String s = "Conflicts found in conflict resolution";
 		    throw new MojoExecutionException(this, s, s);
 	    }
 	}
 }
