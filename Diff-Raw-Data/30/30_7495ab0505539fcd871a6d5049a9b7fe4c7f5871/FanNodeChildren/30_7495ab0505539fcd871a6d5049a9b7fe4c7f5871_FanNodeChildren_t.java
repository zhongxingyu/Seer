 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.colar.netbeans.fan.project;
 
 import org.netbeans.api.project.Project;
 import org.openide.filesystems.FileObject;
 import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
 import org.openide.nodes.FilterNode;
 import org.openide.nodes.Node;
 
 /**
  *
  * @author tcolar
  */
 public class FanNodeChildren extends FilterNode.Children
 {
 
     private final Project project;
 
     FanNodeChildren(Project project, Node projectNode)
     {
 	super(projectNode);
 	this.project = project;
     }
 
     @Override
     protected Node[] createNodes(Node object)
     {
 	Node origChild = (Node) object;
 	DataObject dob = (DataObject) origChild.getLookup().lookup(DataObject.class);
 
 	if (dob != null)
 	{
 	    FileObject fob = dob.getPrimaryFile();
 	    System.err.println("Creating nodes for: "+fob.getPath());
 	    Node[] nds = new Node[1];
	    Children children=FilterNode.Children.LEAF;
	    if(fob.isFolder())
		children=new FanNodeChildren(project, origChild);
 	    FanNode nd = new FanNode(project,
 		    origChild,
 		    children,
 		    fob);
 	    nds[0] = nd;
 	    return nds;
 	}
 	return new Node[0];
     }
 }
