 /*
  * Copyright (c) 2006, 2007 Borland Software Corporation
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Artem Tikhomirov (Borland) - initial API and implementation
  */
 package org.eclipse.gmf.internal.graphdef.codegen;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import org.eclipse.core.runtime.Plugin;
 import org.eclipse.gmf.graphdef.codegen.MapModeCodeGenStrategy;
 import org.eclipse.gmf.internal.xpand.ResourceManager;
 import org.eclipse.gmf.internal.xpand.util.BundleResourceManager;
 import org.osgi.framework.BundleContext;
 
 public class Activator extends Plugin {
 	private static Activator instance;
 
 	public Activator() {
 		instance = this;
 	}
 
 	@Override
 	public void stop(BundleContext context) throws Exception {
 		instance = null;
 		super.stop(context);
 	}
 
 	public static ResourceManager createResourceEngine(MapModeCodeGenStrategy strategy, URL... dynamicTemplates) {
 		try {
 			URL baseURL = instance.getBundle().getEntry("/templates.migrated/");
 			ArrayList<URL> urls = new ArrayList<URL>(3);
 			if (dynamicTemplates != null) {
 				// XXX perhaps, add strategy token to each url
 				// to keep dynamic template structure similar to those bundled?
 				urls.addAll(Arrays.asList(dynamicTemplates));
 			}
 			if (strategy.getToken().length() > 0) {
 				urls.add(new URL(baseURL, strategy.getToken() + '/'));
 			}
 			urls.add(baseURL);
 			return new BundleResourceManager(urls.toArray(new URL[urls.size()]));
 		} catch (MalformedURLException ex) {
			throw new Error();
 		}
 	}
 }
