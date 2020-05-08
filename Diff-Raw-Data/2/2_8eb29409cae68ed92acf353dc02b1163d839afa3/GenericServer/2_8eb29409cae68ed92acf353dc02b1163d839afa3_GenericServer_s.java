 /***************************************************************************************************
  * Copyright (c) 2005 Eteration A.S. and Gorkem Ercan. All rights reserved. This program and the
  * accompanying materials are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors: Gorkem Ercan - initial API and implementation
  *               
  **************************************************************************************************/
 package org.eclipse.jst.server.generic.core.internal;
 
 import java.io.File;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jst.server.core.FacetUtil;
 import org.eclipse.jst.server.core.IEnterpriseApplication;
 import org.eclipse.jst.server.core.IWebModule;
 import org.eclipse.jst.server.generic.servertype.definition.Module;
 import org.eclipse.jst.server.generic.servertype.definition.Port;
 import org.eclipse.jst.server.generic.servertype.definition.Property;
 import org.eclipse.jst.server.generic.servertype.definition.ServerRuntime;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.wst.server.core.IModule;
 import org.eclipse.wst.server.core.IModuleType;
 import org.eclipse.wst.server.core.IServer;
 import org.eclipse.wst.server.core.ServerPort;
 import org.eclipse.wst.server.core.ServerUtil;
 import org.eclipse.wst.server.core.model.IURLProvider;
 import org.eclipse.wst.server.core.model.ServerDelegate;
 
 /**
  * Generic XML based server implementation.
  * 
  * @author Gorkem Ercan
  */
 public class GenericServer extends ServerDelegate implements IURLProvider {
 
     private static final String ATTR_GENERIC_SERVER_MODULES = "Generic_Server_Modules_List"; //$NON-NLS-1$
 
 	public IStatus canModifyModules(IModule[] add, IModule[] remove) {
 		for ( int i = 0; i < add.length; i++ ) {         
 			if( !isSupportedModule( add[i] ) ){
                 return new Status( IStatus.ERROR, CorePlugin.PLUGIN_ID, 0,
                         GenericServerCoreMessages.moduleNotCompatible, null );
             }
             if ( add[i].getProject() != null ) {
                 IStatus status = FacetUtil.verifyFacets(add[i].getProject(), getServer());
                 if (status != null && !status.isOK())
                     return status;
             }
         }
 		return new Status(IStatus.OK, CorePlugin.PLUGIN_ID, 0, "CanModifyModules", null); //$NON-NLS-1$ 
 	}
 	
     private boolean isSupportedModule(IModule module){
         if( module == null )
             return false;
         List moduleTypes = getServerDefinition().getModule();
         for( int j=0; j<moduleTypes.size(); j++ ){
              Module moduleDefinition = (Module)moduleTypes.get(j);
              if( module.getModuleType() != null && module.getModuleType().getId().equals(moduleDefinition.getType()) ){
                 return true;
               }
         }
          return false;
     }
     
     /* (non-Javadoc)
      * @see org.eclipse.wst.server.core.model.ServerDelegate#modifyModules(org.eclipse.wst.server.core.IModule[], org.eclipse.wst.server.core.IModule[], org.eclipse.core.runtime.IProgressMonitor)
      */
     public void modifyModules(IModule[] add, IModule[] remove, IProgressMonitor monitor) throws CoreException {
       
         List modules = this.getAttribute(ATTR_GENERIC_SERVER_MODULES,(List)null);
         
         if(add!=null&& add.length>0)
         {
             if(modules==null) {
                modules=new ArrayList(add.length);
             }
             for (int i = 0; i < add.length; i++) {
                
                if(modules.contains(add[i].getId())==false)
                     modules.add(add[i].getId());
             }
         }
         if(remove!=null && remove.length>0 && modules!=null)
         {
             for (int i = 0; i < remove.length; i++) {
                 modules.remove(remove[i].getId());
              }
         }
         if(modules!=null)    
             setAttribute(ATTR_GENERIC_SERVER_MODULES,modules);
         
     }
 
  	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.wst.server.core.model.IServerDelegate#getChildModules(org.eclipse.wst.server.core.model.IModule[])
 	 */
 	public IModule[] getChildModules(IModule[] module) {
 		if (module[0] != null && module[0].getModuleType() != null) {
 			if (module.length == 1) {
 				IModuleType moduleType = module[0].getModuleType();
 				if (moduleType != null && "jst.ear".equals(moduleType.getId())) { //$NON-NLS-1$
 					IEnterpriseApplication enterpriseApplication = (IEnterpriseApplication) module[0]
 							.loadAdapter(IEnterpriseApplication.class, null);
 					if (enterpriseApplication != null) {
 						IModule[] earModules = enterpriseApplication.getModules(); 
 						if ( earModules != null) {
 							return earModules;
 						}
 					}
 				}
 				else if (moduleType != null && "jst.web".equals(moduleType.getId())) { //$NON-NLS-1$
 					IWebModule webModule = (IWebModule) module[0].loadAdapter(IWebModule.class, null);
 					if (webModule != null) {
 						IModule[] modules = webModule.getModules();
 						return modules;
 					}
 				}
 			}
 		}
 		return new IModule[0];
 	}
 
 	/**
 	 * Returns the server instance properties including runtime properties. 
 	 * 
 	 * @return server instance properties.
 	 */
 	private Map getInstanceProperties() {
 		Map runtimeProperties =getRuntimeDelegate().getServerInstanceProperties();
 		Map serverProperties = getServerInstanceProperties();
 		Map instanceProperties = new HashMap(runtimeProperties.size()+serverProperties.size());
 		instanceProperties.putAll(runtimeProperties);
 		instanceProperties.putAll(serverProperties);
 		return instanceProperties;
 	}
 	
  	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.wst.server.core.model.IMonitorableServer#getServerPorts()
 	 */
 	public org.eclipse.wst.server.core.ServerPort[] getServerPorts() {
 		List ports = new ArrayList();
 		Iterator pIter = this.getServerDefinition().getPort().iterator();
 		while (pIter.hasNext()) {
 			Port element = (Port) pIter.next();
 			int port = Integer.parseInt(getServerDefinition().getResolver().resolveProperties(element.getNo()));
 			ports.add(new ServerPort("server", element.getName(), port, element.getProtocol()));		 //$NON-NLS-1$
 		}
 	
 		return (org.eclipse.wst.server.core.ServerPort[])ports.toArray(new org.eclipse.wst.server.core.ServerPort[ports.size()]);
 	}
 
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.wtp.server.core.model.IURLProvider#getModuleRootURL(org.eclipse.wtp.server.core.model.IModule)
 	 */
 	public URL getModuleRootURL(IModule module) {
 
 		try {
             if (module == null || module.loadAdapter(IWebModule.class,null)==null )
 				return null;
             
             IWebModule webModule =(IWebModule)module.loadAdapter(IWebModule.class,null);
             String host = getServer().getHost();
 			String url = "http://"+host; //$NON-NLS-1$
 			int port = 0;
 			
 			port = getHttpPort();
 			port =ServerUtil.getMonitoredPort(getServer(), port, "web"); //$NON-NLS-1$
 			if (port != 80)
				url += ':' + port;
 
 			url += '/'+webModule.getContextRoot();
 
 			if (!url.endsWith("/")) //$NON-NLS-1$
 				url += "/"; //$NON-NLS-1$
 
 			return new URL(url);
 		} catch (Exception e) {
 			Trace.trace("Could not get root URL", e); //$NON-NLS-1$
 			return null;
 		}
 
 	}
 
 	/**
 	 * Return http port
 	 * @return port
 	 */
 	protected int getHttpPort() {
 		int port=-1;
 		Iterator pIter = this.getServerDefinition().getPort().iterator();
 		while (pIter.hasNext()) {
 			Port aPort = (Port) pIter.next();
 			if(port== -1)
 				port = Integer.parseInt(getServerDefinition().getResolver().resolveProperties(aPort.getNo()));
 			else if( "http".equals(aPort.getProtocol() ) ) //$NON-NLS-1$
 				port = Integer.parseInt(aPort.getNo());	
 		}
 		if( port == -1)
 			port = 8080;
 		return port;
 	}
 
 	/**
 	 * Returns the ServerRuntime that represents the .serverdef
 	 * file for this server. 
 	 * @return server runtime
 	 */
     public ServerRuntime getServerDefinition(){
         IServer server = getServer();
 		String rtTypeId = server.getRuntime().getRuntimeType().getId();
         String serverTypeId = server.getServerType().getId();
         /**
          * Pass both the serverType id and runtimeType id and ServerTypeDefinitionManager 
          * will figure out how to give us back the correct ServerRuntime.
          */
 		return CorePlugin.getDefault().getServerTypeDefinitionManager().getServerRuntimeDefinition(
                 serverTypeId, rtTypeId, getInstanceProperties());
 	}
 
     private GenericServerRuntime getRuntimeDelegate(){
     	return (GenericServerRuntime)getServer().getRuntime().loadAdapter(GenericServerRuntime.class,null);
      }
 
 
     /*
      * (non-Javadoc)
      * 
      * @see org.eclipse.wst.server.core.model.ServerDelegate#getRootModules(org.eclipse.wst.server.core.IModule)
      */
     public IModule[] getRootModules(IModule module) throws CoreException {
      	if ( !isSupportedModule( module ) )
             return null;
         IStatus status = canModifyModules(new IModule[] { module }, null);
         if (status != null && !status.isOK())
             throw  new CoreException(status);;
         IModule[] parents = doGetParentModules(module);
         if(parents.length>0)
         	return parents;
         return new IModule[] { module };
     }
 
 
 	private IModule[] doGetParentModules(IModule module) {
 		IModule[] ears = ServerUtil.getModules("jst.ear"); //$NON-NLS-1$
 		ArrayList list = new ArrayList();
 		for (int i = 0; i < ears.length; i++) {
 			IEnterpriseApplication ear = (IEnterpriseApplication)ears[i].loadAdapter(IEnterpriseApplication.class,null);
 			IModule[] childs = ear.getModules();
 			for (int j = 0; j < childs.length; j++) {
 				if(childs[j].equals(module))
 					list.add(ears[i]);
 			}
 		}
 		return (IModule[])list.toArray(new IModule[list.size()]);
 	}
 	/**
 	 * Returns the server properties.
 	 * @return Map of properties.
 	 */
     public Map getServerInstanceProperties() {
  		return getAttribute(GenericServerRuntime.SERVER_INSTANCE_PROPERTIES, new HashMap());
  	}
  	/**
  	 * Change the server instance properties.
  	 * 
  	 * @param map
  	 */
  	public void setServerInstanceProperties(Map map) {
  		setAttribute(GenericServerRuntime.SERVER_INSTANCE_PROPERTIES, map);
  	}
  	/**
  	 * Checks if the properties set for this server is valid. 
  	 * @return status
  	 */
  	public IStatus validate() {
  		List props = this.getServerDefinition().getProperty();
  		for(int i=0;i<props.size();i++)
  		{
  			Property property =(Property)props.get(i);
  			if(property.getType().equals(Property.TYPE_DIRECTORY) || property.getType().equals(Property.TYPE_FILE))
  			{
  				String path= (String)getInstanceProperties().get(property.getId());
  				if(path!=null && !pathExist(path))
  					return  new Status(IStatus.ERROR, CorePlugin.PLUGIN_ID, 0, NLS.bind(GenericServerCoreMessages.invalidPath,path), null);
  			}
  		}
  		return new Status(IStatus.OK, CorePlugin.PLUGIN_ID, 0, "", null); //$NON-NLS-1$
  	}
 	private boolean pathExist(String path){
 		File f = new File(path);
 		return f.exists();
 	}
  
 	public void setDefaults(IProgressMonitor monitor) {
  		List props = this.getServerDefinition().getProperty();
  		Map instancePropsMap = new HashMap();
  		for (Iterator iter = props.iterator(); iter.hasNext();) {
 			Property element = (Property) iter.next();
 			if(Property.CONTEXT_SERVER.equalsIgnoreCase(element.getContext()))
 				instancePropsMap.put(element.getId(), element.getDefault());
 		}
  		setServerInstanceProperties(instancePropsMap);
  	}
  	
 }
