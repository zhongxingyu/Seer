 package org.otherobjects.cms.test;
 
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 
 import javax.jcr.Node;
 import javax.jcr.NodeIterator;
 import javax.jcr.RepositoryException;
 import javax.jcr.Session;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.jackrabbit.ocm.spring.JcrMappingTemplate;
 import org.otherobjects.cms.SingletonBeanLocator;
 import org.springframework.beans.BeanUtils;
 import org.springframework.test.AbstractTransactionalSpringContextTests;
 import org.springmodules.jcr.JcrCallback;
 
 /**
  * Base class for running Jcr tests. This class extends {@code AbstractTransactionalSpringContextTests} so
  * that dependencies are injected by Spring. Transactions are automatically created and rolled back
  * before each test.
  * 
  * @author rich
  */
 public abstract class BaseJcrTestCase extends AbstractTransactionalSpringContextTests
 {
     protected final Log log = LogFactory.getLog(getClass());
     protected ResourceBundle rb;
     protected JcrMappingTemplate jcrMappingTemplate;
 
     @Override
     protected String[] getConfigLocations()
     {
         setAutowireMode(AUTOWIRE_BY_TYPE);
         return new String[]{"file:src/test/resources/applicationContext-resources.xml", "file:src/main/resources/applicationContext-repository.xml",};
     }
 
     public BaseJcrTestCase()
     {
         // Since a ResourceBundle is not required for each class, just
         // do a simple check to see if one exists
         String className = this.getClass().getName();
 
         try
         {
             rb = ResourceBundle.getBundle(className);
         }
         catch (MissingResourceException mre)
         {
             //log.warn("No resource bundle found for: " + className);
         }
     }
 
     @Override
     protected void onSetUpInTransaction() throws Exception
     {
         //cleanUpRepository();
 
         SingletonBeanLocator.setStaticBeanFactory(applicationContext.getBeanFactory());
         super.onSetUpBeforeTransaction();
     }
 
     /**
      * Utility method to populate a javabean-style object with values
      * from a Properties file
      * @param obj the model object to populate
      * @return Object populated object
      * @throws Exception if BeanUtils fails to copy properly
      */
     protected Object populate(Object obj) throws Exception
     {
         // loop through all the beans methods and set its properties from
         // its .properties file
         Map<String, String> map = new HashMap<String, String>();
 
         for (Enumeration<String> keys = rb.getKeys(); keys.hasMoreElements();)
         {
             String key = keys.nextElement();
             map.put(key, rb.getString(key));
         }
 
         BeanUtils.copyProperties(map, obj);
 
         return obj;
     }
 
     /**
      * FIXME Shouldn't need this if we use transaction via Spring?
      */
     protected void cleanUpRepository()
     {
         jcrMappingTemplate.execute((new JcrCallback()
         {
             public Object doInJcr(Session session) throws RepositoryException
             {
 
                 NodeIterator nodeIterator = session.getRootNode().getNodes();
                 while (nodeIterator.hasNext())
                 {
                     Node node = nodeIterator.nextNode();
                     if (!node.getName().startsWith("jcr:"))
                     {
                         logger.debug("tearDown - remove : " + node.getPath());
                         node.remove();
                     }
                 }
                 session.save();
                 return null;
             }
         }));
     }
 
  
 
     public void setJcrMappingTemplate(JcrMappingTemplate jcrMappingTemplate)
     {
         this.jcrMappingTemplate = jcrMappingTemplate;
     }
 
 //    public void exportDocument(String filePath, String nodePath, boolean skipBinary, boolean noRecurse)
 //    {
 //        try
 //        {
 //            BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(filePath));
 //            ContentHandler handler = null;// new org.apache.xml.serialize.XMLSerializer(os, null).asContentHandler();
 //            session.exportDocumentView(nodePath, handler, skipBinary, noRecurse);
 //            os.flush();
 //            os.close();
 //        }
 //        catch (Exception e)
 //        {
 //            System.out.println("Impossible to export the content from : " + nodePath);
 //            e.printStackTrace();
 //        }
 //    }
 //
 //    public void importDocument(String filePath, String nodePath)
 //    {
 //        try
 //        {
 //            BufferedInputStream is = new BufferedInputStream(new FileInputStream(filePath));
 //            session.importXML(nodePath, is, ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
 //            session.save();
 //            is.close();
 //        }
 //        catch (Exception e)
 //        {
 //            System.out.println("Impossible to import the content from : " + nodePath);
 //            e.printStackTrace();
 //        }
 //
 //    }
 
 }
