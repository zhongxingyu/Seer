 /**
  * This file is part of the Paxle project.
  * Visit http://www.paxle.net for more information.
  * Copyright 2007-2009 the original author or authors.
  *
  * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
  * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
  * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
  * or in the file LICENSE.txt in the root directory of the Paxle distribution.
  *
  * Unless required by applicable law or agreed to in writing, this software is distributed
  * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  */
 package org.paxle.se.provider.rsssearch.impl;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.Constants;
 import org.osgi.framework.ServiceRegistration;
 import org.osgi.service.component.ComponentContext;
 import org.paxle.se.provider.rsssearch.IRssSearchProviderManager;
 import org.paxle.se.search.ISearchProvider;
 
 /**
  * @scr.component 
  * @scr.service interface="org.paxle.se.provider.rsssearch.IRssSearchProviderManager"
  */
 public class RssSearchProviderManager implements IRssSearchProviderManager {	
 	/**
 	 * All currently registered RSS-search providers
 	 */
 	public static List<ServiceRegistration> providers = new ArrayList<ServiceRegistration>();
 	
 	/**
 	 * For logging
 	 */
 	private final Log logger = LogFactory.getLog(this.getClass());
 	
 	/**
 	 * The bundle-context used to register new {@link RssSearchProvider providers}
 	 */
 	private BundleContext bc;
 	
 	/**
 	 * The data-file containing all known provider-URLs
 	 */
 	private File providerFile;
 	
 	protected void activate(ComponentContext context) throws IOException {
 		// getting the data directory to use
 		File providerDir = new File(System.getProperty("paxle.data") + File.separatorChar + "rssSearch");
 		if (!providerDir.exists()) providerDir.mkdirs();
 		
 		// creating the data file
 		this.providerFile = new File(providerDir, "rssProviders.txt");
 		
 		// getting the bundle context
 		this.bc = context.getBundleContext();
 		
 		// registering all currently known searchers to the framework
 		ArrayList<String> urls = this.getUrls();
 		this.registerSearchers(urls);
 	}		
 	
 	protected void deactivate(ComponentContext context) throws Exception {
 		this.unregisterSearchers();
 	}	
 	
 	/* (non-Javadoc)
 	 * @see org.paxle.se.provider.rsssearch.impl.IRssSearchProviderManager#getUrls()
 	 */
 	public ArrayList<String> getUrls() throws IOException{
 		/*
 		 * Creating the default provider list
 		 */
 		if(!this.providerFile.exists()){
 			InputStream defaultIn = this.getClass().getResourceAsStream("/resources/defaultProviders.txt");
 			ArrayList<String> defaults = this.loadUrls(defaultIn);
 			setUrls(defaults);
 		}
 		
 		// reading the providers from file
 		return this.loadUrls(this.providerFile);
 	}
 	
 	private ArrayList<String> loadUrls(File file) throws IOException {
 		return this.loadUrls(new FileInputStream(file));
 	}
 	
 	private ArrayList<String> loadUrls(InputStream in) throws IOException {
 		ArrayList<String> list=new ArrayList<String>();
 		BufferedReader is = null;
 		try {
 			is = new BufferedReader(new InputStreamReader(in));
 			
 			String line;			
 			while((line=is.readLine())!=null) {
 				line = line.trim();
 				if (line.length() == 0) continue;
 				else if (line.startsWith("#")) continue;
 				list.add(line);
 			}
 		} finally {
 			if (is != null) try { is.close(); } catch (Exception e) {/* ignore this */}
 		}
 		return list;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.paxle.se.provider.rsssearch.impl.IRssSearchProviderManager#setUrls(java.util.ArrayList)
 	 */
 	public void setUrls(ArrayList<String> urls) throws IOException{
 		if(!this.providerFile.exists()){
 			this.providerFile.createNewFile();
 		}
 		
 		PrintWriter pr = null;
 		try {
 			pr = new PrintWriter(this.providerFile);
 			for (String url : urls) {
 				pr.println(url);
 			}
 		} finally {
 			if (pr != null) pr.close();
 		}
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.paxle.se.provider.rsssearch.impl.IRssSearchProviderManager#registerSearchers(java.util.ArrayList)
 	 */
 	@SuppressWarnings("serial")
 	public void registerSearchers(ArrayList<String> urls){
 		this.unregisterSearchers();
 		
 		for (String url : urls) {
 			// create a new provider
 			final RssSearchProvider provider = new RssSearchProvider(url);
 			
 			// the provider ID to use
			final String providerID = "org.paxle.se.provider.rsssearch." + provider.getFeedUrlHost(); //Using the host makes using two different feeds on the same site impossible!
 			
 			// register as a service to the framework
 			ServiceRegistration registration = this.bc.registerService(
 					ISearchProvider.class.getName(),
 					provider,
 					new Hashtable<String,String>() {{
 						put(Constants.SERVICE_PID, providerID);
 					}}
 			);
 			
 			// remember it in the internal list
 			providers.add(registration);
 		}
 	}
 	
 	private void unregisterSearchers() {
 		Iterator<ServiceRegistration> regs = providers.iterator();
 		while (regs.hasNext()) {
 			ServiceRegistration sr = regs.next();
 			try{
 				sr.unregister();				
 			} catch(IllegalStateException e) {
 				this.logger.error(e);
 			}
 			regs.remove();
 		}
 	}
 }
