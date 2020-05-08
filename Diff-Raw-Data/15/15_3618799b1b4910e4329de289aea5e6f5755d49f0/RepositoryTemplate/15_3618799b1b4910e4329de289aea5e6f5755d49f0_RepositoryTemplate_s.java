 package com.madalla.cms.service.ocm.template;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Collection;
 import java.util.Collections;
 
 import javax.jcr.Node;
 import javax.jcr.Property;
 import javax.jcr.RepositoryException;
 import javax.jcr.Session;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.jackrabbit.ocm.manager.ObjectContentManager;
 import org.apache.jackrabbit.ocm.query.Filter;
 import org.apache.jackrabbit.ocm.query.Query;
 import org.apache.jackrabbit.ocm.query.QueryManager;
 import org.springmodules.jcr.JcrCallback;
 import org.springmodules.jcr.JcrTemplate;
 
 import com.madalla.bo.AbstractData;
 import com.madalla.cms.service.ocm.RepositoryInfo;
 import com.madalla.cms.service.ocm.RepositoryInfo.RepositoryType;
 
 /**
  * Template for executing common OCM code.
  * 
  * @author Eugene Malan
  *
  */
 public class RepositoryTemplate{
 
 	private JcrTemplate template;
 	private ObjectContentManager ocm;
 	private String site;
 	
 	public RepositoryTemplate(JcrTemplate template, ObjectContentManager ocm, String site){
 		this.template = template;
 		this.ocm = ocm;
 		this.site = site;
 	}
 	
 	public boolean checkExists(final RepositoryType type, final String name){
 		return (Boolean)template.execute(new JcrCallback(){
 
 			public Object doInJcr(Session session) throws IOException,
 					RepositoryException {
 				Node parent = RepositoryInfo.getGroupNode(session, site, type);
 				String nodePath = parent.getPath() + (StringUtils.isEmpty(name)?"" : "/" + name);
 				return ocm.objectExists(nodePath);
 			}
 			
 		});
 	}
 	
 	public InputStream getNodePropertyStream(final String path, final String property){
 	    return (InputStream) template.execute(new JcrCallback(){
 
             public Object doInJcr(Session session) throws IOException, RepositoryException {
                 Node node = (Node) session.getItem(path);
                 Property prop = node.getProperty(property);
                 return prop.getStream();
             }
 	        
 	    });
 	}
 	
 	public void saveNodePropertyStream(final String path, final String property, final InputStream stream){
 	    template.execute(new JcrCallback(){
 
             public Object doInJcr(Session session) throws IOException, RepositoryException {
                 Node node = (Node) session.getItem(path);
                 if (node.hasProperty(property)){
                     Property prop = node.getProperty(property);
                     prop.setValue(stream);
                 } else {
                 	node.setProperty(property, stream);
                 }
                 session.save();
                 stream.close();
                 return null;
             }
 	       
 	        
 	    });
 	}
 	
 	public Object getOcmObject(final RepositoryType type, final AbstractData parent, final String name, 
 			final RepositoryTemplateCallback callback){
 		return getCreateOcmObject(type, parent.getId(), name, callback);
 	}
 	
 	public Object getParentObject(final RepositoryType type, final String name, final RepositoryTemplateCallback callback){
 		String parentPath = (String)template.execute(new JcrCallback(){
 			public Object doInJcr(Session session) throws IOException,
 					RepositoryException {
 				Node node = RepositoryInfo.getGroupNode(session, site, type);
 				session.save();
 				return node.getPath();
 			}
 		});
 		return getCreateOcmObject(type, parentPath, name, callback);
 	}
 	
 	public Collection<? extends AbstractData> getAll(final RepositoryType type){
 		if(!type.parent){
 			return Collections.emptyList();
 		}
 		String parentPath = (String)template.execute(new JcrCallback(){
 			public Object doInJcr(Session session) throws IOException,
 					RepositoryException {
 	    		Node parent = RepositoryInfo.getGroupNode(session, site, type);
 	    		return parent.getPath();
 			}
 		});
		Collection<? extends AbstractData> result = getQueryData(type.typeClass, parentPath);
         return result;
 	}
 	
 	public Collection<? extends AbstractData> getAll(final RepositoryType type, AbstractData parent) {
		return getQueryData(type.typeClass, parent.getId());
 	}
 	
 	private Object getCreateOcmObject(final RepositoryType type, final String parentPath, final String name, final RepositoryTemplateCallback callback){
 		if (type == null || StringUtils.isEmpty(parentPath) || StringUtils.isEmpty(name) || callback == null){
 			throw new RuntimeException("getOcmObject method requires that all parameters are supplied.");
 		}
 		final String path = parentPath + "/" + name;
 		if (!ocm.objectExists(path)){
 			Object obj = callback.createNew(parentPath, name);
 			ocm.insert(obj);
 			ocm.save();
 		}
 		return ocm.getObject(type.typeClass, path);
 	}
 	
     @SuppressWarnings("unchecked") //filter will guarantee that only 'extends AbstractData' are returned
	private Collection<? extends AbstractData> getQueryData(Class<? extends AbstractData> data, String path){
         QueryManager queryManager = ocm.getQueryManager();
         Filter filter = queryManager.createFilter(data);
		filter.setScope(path+"//");
         Query query = queryManager.createQuery(filter);
         return  ocm.getObjects(query);
     }
 	
 	
 	
 }
