 package com.madalla.cms.bo.impl.ocm;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.jcr.Node;
 import javax.jcr.RepositoryException;
 import javax.jcr.Session;
 
 import org.apache.commons.lang.RandomStringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.jackrabbit.ocm.manager.ObjectContentManager;
 import org.springmodules.jcr.JcrCallback;
 import org.springmodules.jcr.JcrTemplate;
 
 import com.madalla.AbstractSpringWicketTester;
 import com.madalla.cms.jcr.JcrUtils;
 import com.madalla.cms.service.ocm.RepositoryInfo;
 import com.madalla.cms.service.ocm.RepositoryInfo.RepositoryType;
 import com.madalla.cms.service.ocm.util.JcrOcmUtils;
 
 public abstract class AbstractContentOcmTest extends AbstractSpringWicketTester {
 
 	private static final String NS = "ec:";
 	private static final String NS_APP = NS+"apps";
 	protected static final String NS_TEST = "test";
 	
 	Log log = LogFactory.getLog(this.getClass());
 	protected JcrTemplate jcrTemplate;
 	protected ObjectContentManager ocm;
 	
 	@Override
 	protected void onSetUp() throws Exception {
 		super.onSetUp();
 		
 		Session session = jcrTemplate.getSessionFactory().getSession();
 		ocm =  JcrOcmUtils.getObjectContentManager(session);
 	}
 	
 	protected Node getTestNode(Session session) throws RepositoryException{
 		Node root = session.getRootNode();
 		Node app = JcrUtils.getCreateNode(NS_APP, root);
 		return JcrUtils.getCreateNode(NS_TEST, app);
 	}
 
 	@Override
 	protected List<String> getTestConfigLocations() {
 		List<String> configLocations = new ArrayList<String>();
 		configLocations
 				.add("classpath:com/madalla/cms/service/ocm/applicationContext-cms.xml");
 		configLocations
 				.add("classpath:com/madalla/cms/jcr/applicationContext-jcr.xml");
 		configLocations.add("classpath:com/madalla/email/applicationContext-email.xml");
		configLocations.add("classpath:com/madalla/db/dao/applicationContext-dao.xml");
 		return configLocations;
 	}
 	
 	public void setJcrTemplate(JcrTemplate jcrTemplate) {
 		this.jcrTemplate = jcrTemplate;
 	}
 
 	public JcrTemplate getJcrTemplate() {
 		return jcrTemplate;
 	}
 	
 	protected String getRandomName(String post){
 	    return RandomStringUtils.randomAlphabetic(5)+post;
 	}
 	
     protected String getCreateParentNode(final RepositoryType type){
         return (String) jcrTemplate.execute(new JcrCallback(){
 
             public Object doInJcr(Session session) throws IOException,
                     RepositoryException {
                 
                 Node parent = RepositoryInfo.getGroupNode(session, NS_TEST, type);
                 session.save();
                 return parent.getPath();
             }
             
         });
     }
     
     protected String getApplicationNode(){
     	return (String) jcrTemplate.execute(new JcrCallback(){
     		
     		public Object doInJcr(Session session) throws IOException,
             RepositoryException {
     			Node node = RepositoryInfo.getApplicationNode(session);
     			session.save();
     			return node.getPath();
     		}
     	});
     }
 
 
 
 }
