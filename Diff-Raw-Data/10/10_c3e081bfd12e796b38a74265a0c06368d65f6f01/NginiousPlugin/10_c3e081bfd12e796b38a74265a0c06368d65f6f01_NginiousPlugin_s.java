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
 import java.net.URL;
 
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.QualifiedName;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.osgi.framework.BundleContext;
 
 public class NginiousPlugin extends AbstractUIPlugin {
 	
 	public static final String PLUGIN_ID = "nginious_plugin";
 	
 	public static final String NATURE_ID = "com.nginious.http.plugin.NginiousNature";
 	
 	private static NginiousPlugin plugin;
 	
 	static final int DEFAULT_LISTEN_PORT = 8080;
 	
 	static final String DEFAULT_PUBLISH_URL = "http://127.0.0.1/admin/application/";
 	
 	static final String DEFAULT_PUBLISH_USERNAME = "admin";
 	
 	static final String DEFAULT_PUBLISH_PASSWORD = "admin";
 	
 	static final QualifiedName LISTE_PORT_PROP_KEY = 
 			new QualifiedName("com.nginious.http.plugin.listePort", "Listen port for HTTP server");
 	
 	static final QualifiedName PUBLISH_URL_PROP_KEY = 
 			new QualifiedName("com.nginious.http.plugin.publishUrl", "Publish URL");
 	
 	static final QualifiedName PUBLISH_USERNAME_PROP_KEY = 
 			new QualifiedName("com.nginious.http.plugin.publishUsername", "Publish username");
 	
 	static final QualifiedName PUBLISH_PASSWORD_PROP_KEY = 
 			new QualifiedName("com.nginious.http.plugin.publishPassword", "Publish password");
 	
 	private ServerManager manager;
 	
 	public NginiousPlugin() {
 		super();
 	}
 
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
 		plugin = this;
 		this.manager = ServerManager.getInstance();
 	}
 
 	public void stop(BundleContext context) throws Exception {
 		plugin = null;
 		super.stop(context);
 	}
 	
 	ServerManager getInstance() {
 		return this.manager;
 	}
 	
 	public static URL getApiJar() throws IOException {
		URL url = FileLocator.find(plugin.getBundle(), new Path("lib/nginious-api.jar"), null);
 		
 		if(url != null) {
 			return FileLocator.toFileURL(url);
 		}
 		
 		return null;
 	}
 	
 	public static NginiousPlugin getDefault() {
 		return plugin;
 	}
 
 	public static void log(IStatus status) {
 		getDefault().getLog().log(status);
 	}
 
 	public static void log(Throwable e) {
 		log(new Status(IStatus.ERROR, PLUGIN_ID, 10001, "Internal error", e));
 	}
 }
