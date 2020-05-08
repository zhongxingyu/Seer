 package org.otherobjects.cms.model;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.otherobjects.cms.jcr.GenericJcrDaoJackrabbit;
 import org.otherobjects.cms.site.TreeBuilder;
 import org.otherobjects.cms.site.TreeNode;
 
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
         return getAllByJcrExpression("/jcr:root//element(*) [jcr:like(@ooType,'%Folder')]");
     }
 
     public TreeNode getFolderTree()
     {
         TreeBuilder tb = new TreeBuilder();
         List<BaseNode> all = getFolders();
         List<TreeNode> flat = new ArrayList<TreeNode>();
         for (BaseNode b : all)
         {
            flat.add(new TreeNode(b.getJcrPath()+"/", b.getId(), b.getOoLabel(), b)); 
         }
 
         return tb.buildTree(flat);
     }
 }
