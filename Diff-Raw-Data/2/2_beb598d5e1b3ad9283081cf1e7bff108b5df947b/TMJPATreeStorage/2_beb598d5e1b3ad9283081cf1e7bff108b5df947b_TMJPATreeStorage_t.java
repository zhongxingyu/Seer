 package controllers;
 
 import controllers.tree.JPATreeStorage;
 import models.tm.ProjectTreeNode;
 import tree.persistent.GenericTreeNode;
 
 /**
  * @author Manuel Bernhardt <bernhardt.manuel@gmail.com>
  */
 public class TMJPATreeStorage extends JPATreeStorage {
 
     public TMJPATreeStorage() {
         super(ProjectTreeNode.class);
     }
 
     @Override
     public GenericTreeNode getNewTreeNode() {
         // this is probably quite a hack
         // it also means that whenever we have a tree that isn't specific to a project (e.g. in the admin area)
         // we need to make sure the projectThreadLocal is set beforehand by that specific tree
         return new ProjectTreeNode(TMTreeController.projectThreadLocal.get());
     }
 
     @Override
     public GenericTreeNode persistTreeNode(GenericTreeNode node) {
         ProjectTreeNode treeNode = (ProjectTreeNode) node;
         treeNode.create();
         return node;
     }
 
     @Override
     public boolean exists(GenericTreeNode node) {
        return findJSTreeNodes("from ProjectTreeNode n where n.path = ? and n.type = ? and n.treeId = ? and n.project.id = ?", node.getPath(), node.getNodeType().getName(), node.getTreeId(), ((ProjectTreeNode)node).project.getId()).size() > 0;
     }
 }
