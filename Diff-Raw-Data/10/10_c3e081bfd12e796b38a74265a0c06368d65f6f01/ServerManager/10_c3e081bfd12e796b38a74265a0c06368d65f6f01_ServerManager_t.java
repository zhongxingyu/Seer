 /**
  * Copyright 2012 NetDigital Sweden AB
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
 */
 
 package com.nginious.http.plugin;
 
 import java.io.IOException;
 import java.util.Collection;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.core.resources.IResourceChangeListener;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.jface.text.Document;
 
 import com.nginious.http.application.ApplicationException;
 import com.nginious.http.application.ApplicationManager;
 import com.nginious.http.server.HttpServer;
 import com.nginious.http.server.HttpServerConfiguration;
 import com.nginious.http.server.HttpServerFactory;
 
 class ServerManager implements IResourceChangeListener {
 	
 	private static ServerManager manager = null;
 	
 	private static Object lock = new Object();
 	
 	private ConcurrentHashMap<String, HttpServerEnvironment> servers;
 	
 	private ServerManager() {
 		super();
 		this.servers = new ConcurrentHashMap<String, HttpServerEnvironment>();
 		int eventMask = IResourceChangeEvent.POST_CHANGE | IResourceChangeEvent.PRE_CLOSE | IResourceChangeEvent.PRE_DELETE;
 		ResourcesPlugin.getWorkspace().addResourceChangeListener(this, eventMask);
 	}
 	
 	static ServerManager getInstance() {
 		synchronized(lock) {
 			if(manager == null) {
 				manager = new ServerManager();
 			}
 			
 			return manager;
 		}
 		
 	}
 	
 	public void resourceChanged(IResourceChangeEvent event) {
 		int type = event.getType();
 		
 		switch(type) {
 		case IResourceChangeEvent.PRE_CLOSE:
 		case IResourceChangeEvent.PRE_DELETE:
 			IProject project = (IProject)event.getResource();
 			stopServer(project);
 			break;
 			
 		case IResourceChangeEvent.POST_CHANGE:
 			IResourceDelta delta = event.getDelta();
 			IResourceDelta[] children = delta.getAffectedChildren();
 			
 			if(children != null && children.length > 0) {
 				for(IResourceDelta child : children) {
 					IResource resource = child.getResource();
 					
 					if(resource instanceof IProject) {
 						IProject changeProject = (IProject)resource;
 						
 						if(projectHasChanged(changeProject)) {
 							stopServer(changeProject);
 							startServer(changeProject);
 						}
 					}
 				}
 			}
 			break;
 		}
 	}
 	
 	private boolean projectHasChanged(IProject project) {
 		HttpServerEnvironment env = servers.get(project.getName());
 		
 		if(env != null) {
 			return env.hasChanged();
 		}
 		
 		// Verify that project has been fully created
 		IFolder folder = project.getFolder("WebContent");
 		
 		if(!folder.exists()) {
 			return false;
 		}
 		
 		return project.isOpen();
 	}
 	
 	void stopServer(IProject project) {
 		try {
 			HttpServerEnvironment env = servers.remove(project.getName());
 			
 			if(env != null) {
 				HttpServer server = env.getServer();
 				server.stop();
 			}
 		} catch(IOException e) {
 			// Do nothing
 		}
 	}
 	
 	void restartServer(IProject project) {
 		stopServer(project);
 		startServer(project);
 	}
 	
 	void startServer(IProject project) {
 		int listenPort = NginiousPlugin.DEFAULT_LISTEN_PORT;
 		String publishUrl = NginiousPlugin.DEFAULT_PUBLISH_URL;
 		String publishUsername = NginiousPlugin.DEFAULT_PUBLISH_USERNAME;
 		String publishPassword = NginiousPlugin.DEFAULT_PUBLISH_PASSWORD;
 		
 		try {
 			String listenPortStr = project.getPersistentProperty(NginiousPlugin.LISTE_PORT_PROP_KEY);
 			
 			if(listenPortStr != null) {
 				listenPort = Integer.parseInt(listenPortStr);
 			}
 			
 			publishUrl = project.getPersistentProperty(NginiousPlugin.PUBLISH_URL_PROP_KEY);
 			publishUsername = project.getPersistentProperty(NginiousPlugin.PUBLISH_USERNAME_PROP_KEY);
 			publishPassword = project.getPersistentProperty(NginiousPlugin.PUBLISH_PASSWORD_PROP_KEY);
 		} catch(NumberFormatException e) {
 			String title = Messages.ServerManager_listen_port_error_title;
 			String message = Messages.ServerManager_listen_port_error_message + " " + project.getName();
 			MessagesUtils.displayMessageDialog(e.getMessage(), null, title, message);
 		} catch(CoreException e) {
 			String title = Messages.ServerManager_properties_error_title;
 			String message = Messages.ServerManager_listen_port_error_message + " " + project.getName();
 			MessagesUtils.perform(e, null, title, message);
 		}
 		
 		try {
 			HttpServerConfiguration config = new HttpServerConfiguration();
 			config.setPort(listenPort);
 			config.setAdminPwd(publishPassword);
 			config.setWebappsDir(null);
 			IPath projectPath = project.getLocation();
 			IPath webappsPath = projectPath.append("WebContent");
			HttpServerFactory factory = HttpServerFactory.getInstance();
			HttpServer server = factory.create(config);
 			LogViewConsumer accessLogConsumer = new LogViewConsumer(new Document());
 			LogViewConsumer messageLogConsumer = new LogViewConsumer(new Document());
 			server.setAccessLogConsumer(accessLogConsumer);
 			server.setMessageLogConsumer(messageLogConsumer);
 			server.start();
 			ApplicationManager manager = server.getApplicationManager();
 			manager.publish(project.getName(), webappsPath.toFile());
 			
 			HttpServerEnvironment env = new HttpServerEnvironment(project, server, accessLogConsumer, messageLogConsumer);
 			env.setPort(listenPort);
 			env.setPublishUrl(publishUrl);
 			env.setPublishUsername(publishUsername);
 			env.setPublishPassword(publishPassword);
 			servers.put(project.getName(), env);
 		} catch(ApplicationException e) {
 			String title = Messages.ServerManager_server_error_title;
 			String message = Messages.ServerManager_server_error_message + " " + project.getName();
 			MessagesUtils.displayMessageDialog(e.getMessage(), null, title, message);			
 		} catch(IOException e) {
 			String title = Messages.ServerManager_server_error_title;
 			String message = Messages.ServerManager_server_error_message + " " + project.getName();
 			MessagesUtils.displayMessageDialog(e.getMessage(), null, title, message);
 		}
 	}
 	
 	IProject checkListenPortUsage(int listenPort) {
 		Collection<HttpServerEnvironment> envs = servers.values();
 		
 		for(HttpServerEnvironment env : envs) {
 			if(env.getPort() == listenPort) {
 				return env.getProject();
 			}
 		}
 		
 		return null;
 	}
 	
 	Document getMessageLogDocument(IProject project) {
 		HttpServerEnvironment env = servers.get(project.getName());
 		
 		if(env != null) {
 			LogViewConsumer consumer = env.getMessageLogConsumer();
 			
 			if(consumer != null) {
 				return consumer.getDocument();
 			}
 		}
 		
 		return null;
 	}
 	
 	Document getAccessLogDocument(IProject project) {
 		HttpServerEnvironment env = servers.get(project.getName());
 		
 		if(env != null) {
 			LogViewConsumer consumer = env.getAccessLogConsumer();
 			
 			if(consumer != null) {
 				return consumer.getDocument();
 			}
 		}
 		
 		return null;
 	}
 	
 	private class HttpServerEnvironment {
 		
 		private IProject project;
 		
 		private int port;
 		
 		@SuppressWarnings("unused")
 		private String publishUrl;
 		
 		@SuppressWarnings("unused")
 		private String publishUsername;
 		
 		private String publishPassword;
 		
 		private HttpServer server;
 		
 		private LogViewConsumer accessLogConsumer;
 		
 		private LogViewConsumer messageLogConsumer;
 		
 		private HttpServerEnvironment(IProject project, HttpServer server, LogViewConsumer accessLogConsumer, LogViewConsumer messageLogConsumer) {
 			super();
 			this.project = project;
 			this.server = server;
 			this.accessLogConsumer = accessLogConsumer;
 			this.messageLogConsumer = messageLogConsumer;
 		}
 		
 		private IProject getProject() {
 			return this.project;
 		}
 		
 		private void setPort(int port) {
 			this.port = port;
 		}
 		
 		private int getPort() {
 			return this.port;
 		}
 		
 		private void setPublishUrl(String publishUrl) {
 			this.publishUrl = publishUrl;
 		}
 
 		private void setPublishUsername(String publishUsername) {
 			this.publishUsername = publishUsername;
 		}
 
 		private void setPublishPassword(String publishPassword) {
 			this.publishPassword = publishPassword;
 		}
 		
 		private HttpServer getServer() {
 			return this.server;
 		}
 		
 		private LogViewConsumer getMessageLogConsumer() {
 			return this.messageLogConsumer;
 		}
 		
 		private LogViewConsumer getAccessLogConsumer() {
 			return this.accessLogConsumer;
 		}
 		
 		private boolean hasChanged() {
 			try {
 				String newPort = project.getPersistentProperty(NginiousPlugin.LISTE_PORT_PROP_KEY);
 				String newPublishPassword = project.getPersistentProperty(NginiousPlugin.PUBLISH_PASSWORD_PROP_KEY);
 				
 				if(!Integer.toString(this.port).equals(newPort)) {
 					return true;
 				}
 				
 				if(!publishPassword.equals(newPublishPassword)) {
 					return true;
 				}
 				
 				return false;
 			} catch(CoreException e) {
 				return false;
 			}
 		}
 	}	
 }
