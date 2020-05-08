 package com.madalla.service.cms;
 
 import java.io.IOException;
 import java.io.Serializable;
 
 import javax.jcr.Node;
 import javax.jcr.PathNotFoundException;
 import javax.jcr.Property;
 import javax.jcr.RepositoryException;
 import javax.jcr.Session;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springmodules.jcr.JcrCallback;
 import org.springmodules.jcr.JcrTemplate;
 
 import com.madalla.webapp.cms.Content;
 
 public class ContentServiceImpl implements IContentService, Serializable {
     
     private static final long serialVersionUID = 1L;
     private JcrTemplate template;
     private final Log log = LogFactory.getLog(this.getClass());
 
     public void loadContent(String className) {
         // TODO load all data for class
 
     }
 
     public String getContentData(final String className, final String id) {
         
         return (String) template.execute(new JcrCallback(){
 
             public Object doInJcr(Session session) throws IOException, RepositoryException {
                 String content = null;
                 try {
                     Node classNode = getClassNode(className, session.getRootNode());
                     content = getContent(classNode, id);
                 } catch (RepositoryException e) {
                     log.error("Exception getting content.", e);
                     content = "There has been a technical difficulty accessing the Content Repository. " + e.getMessage();
                 }
                 session.save();
                 return content;
             }
             
         });
     }
 
     public void setContent(final Content content) throws RepositoryException {
         template.execute(new JcrCallback(){
 
             public Object doInJcr(Session session) throws IOException, RepositoryException {
                 Node classNode = getClassNode(content.getClassName(),session.getRootNode());
                Node node;
                try {
                    node = classNode.getNode(content.getContentId());
                } catch (PathNotFoundException e){
                	log.debug("Path Content Not found, now adding it. contentId="+content.getContentId());
                	node = classNode.addNode(content.getContentId());
                }
                 node.setProperty(CONTENT, content.getText());
                 session.save();
                 return null;
             }
             
         });
     }
     
     /**
      *  returns the class name node -- creates it if its not there
      */
     private Node getClassNode(String className, Node root) throws RepositoryException{
         Node classNode = null;
         try {
             classNode = root.getNode(className);
         } catch (PathNotFoundException e){
             log.debug("Class node not found in repository, now adding. class="+className);
             classNode = root.addNode(className);
         }
         return classNode;
         
     }
     
     private String getContent(Node mainNode, String id) {
         String content = "Content placeholder : there is no content in the repository at present.";
         Node node;
         try {
             try {
                 node = mainNode.getNode(id);
             } catch (PathNotFoundException e) {
                 log.debug("Content ID not found in repository, now adding. id="+id);
                 node = mainNode.addNode(id);
             }
             try {
                 content = node.getProperty(CONTENT).getString();
             } catch (PathNotFoundException e) {
                 log.debug("No Content found in repository, now adding default value. id="+id);
                 Property property = node.setProperty(CONTENT,CONTENT_DEFAULT);
                 content = property.getString();
             }
         } catch (Exception e) {
             log.error("Exception while getting content from repository.", e);
             throw new RuntimeException(e);
         }
         
         
         return content;
     }
 
     public void setTemplate(JcrTemplate template) {
         this.template = template;
     }
 
     public JcrTemplate getTemplate() {
         return template;
     }
 
 }
