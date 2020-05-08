 package com.mpower.service;
 
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.jaspersoft.jasperserver.api.metadata.xml.domain.impl.ResourceDescriptor;
 import com.jaspersoft.jasperserver.irplugin.JServer;
 import com.orangeleap.common.security.CasUtil;
 
 //@Service("jasperServerService")
 public class JasperServerServiceImpl implements JasperServerService {
     protected final Log logger = LogFactory.getLog(getClass());
     private String userName;
     private String password;
     private String baseUri;
     private String repositoryUri;
 
     @Override
     public String getPassword() {
 	return password;
     }
 
 
     @Override
     public String getUserName() {
 	return userName;
     }
 
     @Override
     public List list(String dir) throws Exception {
 	logger.info("list(" + dir + ")");
 	JServer jserver = new JServer();
 	jserver.setUsername(userName);
 	jserver.setPassword(password);
 	jserver.setUrl(baseUri + repositoryUri);
 	ResourceDescriptor rd = new ResourceDescriptor();
 	rd.setWsType(ResourceDescriptor.TYPE_FOLDER);
 	rd.setUriString(dir);
 
	CasUtil.populateJserverWithCasCredentials(jserver, getBaseUri());
 	return jserver.getWSClient().list(rd);
     }
 
     @Override
     public ResourceDescriptor getDatasource(String datasourceName) throws Exception {
     	ResourceDescriptor result = null;
 
     	logger.info("getDatasource(" + datasourceName + ")");
     	JServer jserver = new JServer();
 		jserver.setUsername(userName);
 		jserver.setPassword(password);
 		jserver.setUrl(baseUri + repositoryUri);
 
    	CasUtil.populateJserverWithCasCredentials(jserver, getBaseUri());
 
 		List datasources = jserver.getWSClient().listDatasources();
 		java.util.Iterator itDatasources = datasources.iterator();
 		while (itDatasources.hasNext()) {
 			ResourceDescriptor resource = (ResourceDescriptor) itDatasources.next();
 			if (resource.getUriString().equals(datasourceName)) {
 				return resource;
 			}
 		}
 		return result;
     }
 
     @Override
     public void setPassword(String pass) {
 	logger.info("setPassword(" + pass + ")");
 	password = pass;
 
     }
 
 
     @Override
     public void setUserName(String username) {
 	logger.info("setUserName(" + username + ")");
 	userName = username;
 
     }
 
 
 	public void setBaseUri(String baseUri) {
 		this.baseUri = baseUri;
 	}
 
 
 	public String getBaseUri() {
 		return baseUri;
 	}
 
 
 	public void setRepositoryUri(String repositoryUri) {
 		this.repositoryUri = repositoryUri;
 	}
 
 
 	public String getRepositoryUri() {
 		return repositoryUri;
 	}
 
 
 	
 }
