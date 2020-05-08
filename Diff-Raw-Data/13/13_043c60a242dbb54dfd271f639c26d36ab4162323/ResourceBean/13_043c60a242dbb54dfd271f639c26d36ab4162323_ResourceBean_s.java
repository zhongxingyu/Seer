 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.avangarde.gnosis.presentation.controller;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.GregorianCalendar;
 import java.util.List;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ManagedProperty;
 import javax.faces.bean.ViewScoped;
 import javax.faces.model.SelectItem;
 import org.avangarde.gnosis.businesslogic.facade.FacadeFactory;
 import org.avangarde.gnosis.businesslogic.facade.PublicationFacade;
 import org.avangarde.gnosis.vo.PublicationVo;
 import org.primefaces.model.DefaultTreeNode;
 import org.primefaces.model.TreeNode;
 
 /**
  *
  * @author andres
  */
 @ManagedBean
 @ViewScoped
 public class ResourceBean implements Serializable {
 
     private TreeNode root;
     private TreeNode selectedNode;
     private int id;
     private String title;
     private String topic;
     private String type;
     private String url;
     private String sharedBy;
     private List<SelectItem> topics;
     @ManagedProperty(value = "#{userBean}")
     private UserBean user;
     @ManagedProperty(value = "#{subjectBean}")
     private SubjectBean subject;
 
     public ResourceBean() {
     }
 
     public void saveResource() {
 
         PublicationFacade facade = FacadeFactory.getInstance().getPublicationFacade();
 
         PublicationVo vo = new PublicationVo();
         vo.setTitle(getTitle());
         vo.setTopic(getTopic());
         vo.setType(getType());
         vo.setDate(new GregorianCalendar().getTime());
         vo.setStudentId(getUser().getId());
         vo.setSubjectCode(getSubject().getCode());
 
         vo.setUrl(getUrl());
         
         facade.create(vo);
         
        setId(16);

         TreeNode nodeCurrentTopic = null;
         for (TreeNode node : root.getChildren()) {
             if (((PublicationVo) (node).getData()).getTitle().equals(vo.getTopic())) {
                 nodeCurrentTopic = node;
             }
         }
         if (nodeCurrentTopic == null) {
             PublicationVo newVo = new PublicationVo();
             newVo.setTitle(vo.getTopic());
             nodeCurrentTopic = new DefaultTreeNode(newVo, root);
         }
 
         new DefaultTreeNode(vo, nodeCurrentTopic);
 
 //        return "success";
 
     }
 
     public void preRenderView() {
         root = new DefaultTreeNode("root", null);
         List<String> topics = FacadeFactory.getInstance().getPublicationFacade().getTopicsBySubject(subject.getCode());
         for (String topic : topics) {
             PublicationVo vo = new PublicationVo();
             vo.setTitle(topic);
             new DefaultTreeNode(vo, root);
         }
 
         for (TreeNode node : root.getChildren()) {
             List<PublicationVo> publications = FacadeFactory.getInstance().getPublicationFacade().getPublicationsByTopic(((PublicationVo) node.getData()).getTitle());
             for (PublicationVo publication : publications) {
                 new DefaultTreeNode(publication, node);
             }
         }
     }
 
     public List<SelectItem> getTopics() {
         if (topics == null) {
             topics = new ArrayList<SelectItem>();
             List<String> topicList = FacadeFactory.getInstance().getPublicationFacade().getTopicsBySubject(subject.getCode());
             if (topicList != null) {
                 for (String topic : topicList) {
                     topics.add(new SelectItem(topic));
                 }
             }
         }
         return topics;
     }
 
     public void update() {
     }
 
     public TreeNode getRoot() {
         return root;
     }
 
     public void setRoot(TreeNode root) {
         this.root = root;
     }
 
     public TreeNode getSelectedNode() {
         return selectedNode;
     }
 
     public void setSelectedNode(TreeNode selectedNode) {
         this.selectedNode = selectedNode;
     }
 
     public int getId() {
         if (selectedNode != null) {
             return ((PublicationVo) (selectedNode.getData())).getId();
         } else {
             return id;
         }
     }
 
     public void setId(int id) {
         this.id = id;
     }
 
     public String getTitle() {
         return title;
     }
 
     public void setTitle(String title) {
         this.title = title;
     }
 
     public String getTopic() {
         return topic;
     }
 
     public void setTopic(String topic) {
         this.topic = topic;
     }
 
     public String getType() {
         return type;
     }
 
     public void setType(String type) {
         this.type = type;
     }
 
     public String getUrl() {
         return url;
     }
 
     public void setUrl(String url) {
         this.url = url;
     }
 
     public UserBean getUser() {
         return user;
     }
 
     public void setUser(UserBean user) {
         this.user = user;
     }
 
     public SubjectBean getSubject() {
         return subject;
     }
 
     public void setSubject(SubjectBean subject) {
         this.subject = subject;
     }
 
     public String getSharedBy() {
         return sharedBy;
     }
 
     public void setSharedBy(String sharedBy) {
         this.sharedBy = sharedBy;
     }
 }
