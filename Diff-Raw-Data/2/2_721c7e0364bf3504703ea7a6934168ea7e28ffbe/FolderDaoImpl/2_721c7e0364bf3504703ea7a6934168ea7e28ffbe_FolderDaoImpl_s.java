 package org.otherobjects.cms.model;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.otherobjects.cms.jcr.GenericJcrDaoJackrabbit;
 import org.otherobjects.cms.site.TreeBuilder;
 import org.otherobjects.cms.site.TreeNode;
 
 import edu.emory.mathcs.backport.java.util.Collections;
 
 /**
  * TODO Merge with NavigationSrevice 
  * @author rich
  */
 public class FolderDaoImpl extends GenericJcrDaoJackrabbit<BaseNode> implements FolderDao
 {
     public FolderDaoImpl()
     {
         super(BaseNode.class);
     }
 
     public List<BaseNode> getFolders()
     {
         // FIXME Need folder indicator
        return getAllByJcrExpression("/jcr:root//element(*) [jcr:like(@ooType,'%Folder')] order by creationTimestamp");
     }
 
     public TreeNode getFolderTree(String rootPath)
     {
         TreeBuilder tb = new TreeBuilder();
         List<BaseNode> all = getFolders();
         List<TreeNode> flat = new ArrayList<TreeNode>();
         int count = 0;
         for (BaseNode b : all)
         {
             flat.add(new TreeNode(b.getJcrPath()+"/", b.getId(), b.getOoLabel(), count++, b)); 
         }
         Collections.sort(flat,new TreeNodeComparator());
         TreeNode tree = tb.buildTree(flat);
         return tree.getNode(rootPath);
     }
 }
