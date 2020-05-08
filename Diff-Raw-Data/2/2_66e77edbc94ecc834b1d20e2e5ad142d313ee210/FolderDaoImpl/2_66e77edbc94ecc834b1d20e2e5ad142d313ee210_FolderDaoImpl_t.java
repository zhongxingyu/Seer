 package org.otherobjects.cms.model;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.otherobjects.cms.jcr.GenericJcrDaoJackrabbit;
 import org.otherobjects.cms.site.TreeBuilder;
 import org.otherobjects.cms.site.TreeNode;
 
 /**
  * TODO Merge with NavigationSrevice 
  * @author rich
  */
 public class FolderDaoImpl extends GenericJcrDaoJackrabbit<Folder> implements FolderDao
 {
     public FolderDaoImpl()
     {
         super(Folder.class);
     }
 
     public List<Folder> getFolders()
     {
         // FIXME Need folder indicator
        return (List<Folder>)getAllByJcrExpression("/jcr:root//element(*) [jcr:like(@ooType,'%Folder')] order by @creationTimestamp");
     }
 
     public TreeNode getFolderTree(String rootPath)
     {
         TreeBuilder tb = new TreeBuilder();
         List<Folder> all = getFolders();
         List<TreeNode> flat = new ArrayList<TreeNode>();
         int count = 0;
         for (Folder b : all)
         {
             flat.add(new TreeNode(b.getJcrPath()+"/", b.getId(), b.getOoLabel(), count++, b)); 
         }
         Collections.sort(flat,new TreeNodeComparator());
         TreeNode tree = tb.buildTree(flat);
         return tree.getNode(rootPath);
     }
 }
