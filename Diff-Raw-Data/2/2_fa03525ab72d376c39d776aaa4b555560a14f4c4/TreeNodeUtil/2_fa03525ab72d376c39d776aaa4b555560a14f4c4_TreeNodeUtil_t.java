 package com.ted.common.support.extjs4.tree;
 
 import java.util.List;
 
 /**
  * TreeNodeUtil
  *
  */
 public abstract class TreeNodeUtil {
     public static final void setLeaf(TreeNode node , boolean leaf){
     	if(null == node){
     		return;
     	}
         node.setLeaf(leaf);
     }
     
     public static final void setLeaf(List<? extends TreeNode> nodes , boolean leaf){
     	if(null == nodes){
     		return;
     	}
         for(TreeNode node: nodes){
             setLeaf(node, leaf);
         }
     }
     
     /**
      * 级联设置TreeNodeWithChildren2Check 的children2为leaf
      */
     public static final void setChildren2LeafCascade(List<CheckTreeNodeWithChildren2> treeNodeList){
     	if(null == treeNodeList){
     		return;
     	}
         for(CheckTreeNodeWithChildren2 treeNode:treeNodeList){
             List<CheckTreeNodeWithChildren2> children2 = treeNode.getChildren2();
             TreeNodeUtil.setLeaf(children2, true);
             
             setChildren2LeafCascade(treeNode.getChildren());
         }
     }
     
     /**
      * 级联设置TreeNodeWithChildrenCheck 的children为leaf=false
      */
     public static final void setChildrenNotLeafCascade(List<CheckTreeNodeWithChildren2> treeNodeList){
     	if(null == treeNodeList){
     		return;
     	}
         for(CheckTreeNodeWithChildren2 treeNode:treeNodeList){
             List<CheckTreeNodeWithChildren2> children = treeNode.getChildren();
             TreeNodeUtil.setLeaf(children, false);
             
            setChildrenNotLeafCascade(treeNode.getChildren());
         }
     }
     
     /**
      * 级联把childre2 append 到children
      */
     public static final void moveChildren2ToChildrenCascade(List<CheckTreeNodeWithChildren2> treeNodeList){
     	if(null == treeNodeList){
     		return;
     	}
         for(CheckTreeNodeWithChildren2 treeNode:treeNodeList){
             moveChildren2ToChildrenCascade(treeNode.getChildren());
             
             List<CheckTreeNodeWithChildren2> children =(List<CheckTreeNodeWithChildren2>) treeNode.getChildren();
             List<CheckTreeNodeWithChildren2> children2 = treeNode.getChildren2();
             for(CheckTreeNodeWithChildren2 node :children2){ //20120830 hack to see Constants.js 
                 //node.setId(Constants.NODEIDSTART + node.getId());
                 node.setId(null);//20120906 modified
             }
             children.addAll(children2);
             treeNode.setChildren2(null);
         }
     }
 }
