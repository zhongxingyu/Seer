 /*******************************************************************************
  * Copyright (c) 2000, 2004 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.update.tests.regularInstall;
import java.util.Arrays;

 import java.io.File;
 import java.net.URL;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.update.core.*;
 import org.eclipse.update.internal.core.*;
 import org.eclipse.update.tests.UpdateManagerTestCase;
 
 public class TestExecutablePackagedInstall extends UpdateManagerTestCase {
 	/**
 	 * Constructor for Test1
 	 */
 	public TestExecutablePackagedInstall(String arg0) {
 		super(arg0);
 	}
 
 	/**
 	 * With site.xml
 	 */
 	public void testFilePackageExecutableFeatureSite() throws Exception {
 
 		//cleanup target  
 		File target = new File(TARGET_FILE_SITE.getFile());
 		UpdateManagerUtils.removeFromFileSystem(target);
 		
 		URL newURL = new File(dataPath + "ExecutableFeaturePackagedSite/data2/site.xml").toURL();
 		ISite remoteSite = SiteManager.getSite(newURL,null);
 		IFeatureReference[] featuresRef = remoteSite.getFeatureReferences();
 		ISite localSite = SiteManager.getSite(TARGET_FILE_SITE,null);
 		IFeature remoteFeature = null;
 		
 		// at least one executable feature and on packaged
 		boolean execFeature = false;
 		boolean packFeature = false;
 
 		if (featuresRef.length==0) fail ("no feature found");
 	
 		for (int i = 0; i < featuresRef.length; i++) {
 			remoteFeature = featuresRef[i].getFeature(null);
 			remove(remoteFeature,localSite);			
 			localSite.install(remoteFeature, null,null);
 			
 			if (remoteFeature.getFeatureContentProvider() instanceof FeaturePackagedContentProvider) packFeature = true;
 			if (remoteFeature.getFeatureContentProvider() instanceof FeatureExecutableContentProvider) execFeature = true;
 
 			// verify
 			String site = localSite.getURL().getFile();
 			IPluginEntry[] entries = remoteFeature.getRawPluginEntries();
 			assertTrue("no plugins entry", (entries != null && entries.length != 0));
 			String pluginName = entries[0].getVersionedIdentifier().toString();
 			File pluginFile = new File(site, Site.DEFAULT_PLUGIN_PATH + pluginName);
			if (!pluginFile.exists()) {
				//print out more failure details - see bug 271196
				File[] existing = pluginFile.getParentFile().listFiles();
				String detail = existing == null ? "no children" : String.valueOf(Arrays.asList(existing));
				assertTrue("plugin files not installed locally:"+pluginFile + "children found: " + detail, pluginFile.exists());
			}
 
 			File featureFile = new File(site, Site.DEFAULT_INSTALLED_FEATURE_PATH + remoteFeature.getVersionedIdentifier().toString());
 			assertTrue("feature info not installed locally:"+featureFile, featureFile.exists());
 
 			File featureFileXML = new File(site, Site.DEFAULT_INSTALLED_FEATURE_PATH + remoteFeature.getVersionedIdentifier().toString() + File.separator + "feature.xml");
 			assertTrue("feature info not installed locally: no feature.xml", featureFileXML.exists());
 		}
 
 		if (!execFeature && !packFeature){
 			fail("cannot find one executable and one package feature on teh site");
 		}
 
 		//cleanup target 
 		UpdateManagerUtils.removeFromFileSystem(target);
 
 
 	}
 	
 	
 	/**
 	 * Without site.xml
 	 */
 	public void testFileNoSiteXMLSite() throws Exception {
 
 		//cleanup target 
 		File target = new File(TARGET_FILE_SITE.getFile());
 		UpdateManagerUtils.removeFromFileSystem(target);
 		
 		URL newURL = new File(dataPath + "ExecutableFeaturePackagedSite/data/").toURL();
 		ISite remoteSite = SiteManager.getSite(newURL,null);
 		IFeatureReference[] featuresRef = remoteSite.getFeatureReferences();
 		ISite localSite = SiteManager.getSite(TARGET_FILE_SITE,null);
 		IFeature remoteFeature = null;
 		
 		// at least one executable feature and on packaged
 		boolean execFeature = false;
 		boolean packFeature = false;
 
 		if (featuresRef.length==0) fail ("no feature found");
 	
 		for (int i = 0; i < featuresRef.length; i++) {
 			try {
 				remoteFeature = featuresRef[i].getFeature(null);
 			} catch (CoreException e){
 				Throwable e1 = e.getStatus().getException();
 				String msg = e1.getMessage().replace(File.separatorChar,'/');
 				if (msg.indexOf("CVS/feature.xml")==-1){
 					throw e;
 				}				
 			}
 			if (remoteFeature!=null){
 				remove(remoteFeature,localSite);
 				localSite.install(remoteFeature,null, null);
 				
 				if (remoteFeature.getFeatureContentProvider() instanceof FeaturePackagedContentProvider) packFeature = true;
 				if (remoteFeature.getFeatureContentProvider() instanceof FeatureExecutableContentProvider) execFeature = true;
 	
 				// verify
 				String site = localSite.getURL().getFile();
 				IPluginEntry[] entries = remoteFeature.getRawPluginEntries();
 				assertTrue("no plugins entry", (entries != null && entries.length != 0));
 				String pluginName = entries[0].getVersionedIdentifier().toString();
 				File pluginFile = new File(site, Site.DEFAULT_PLUGIN_PATH + pluginName);
 				assertTrue("plugin files not installed locally:"+pluginFile, pluginFile.exists());
 	
 				File featureFile = new File(site, Site.DEFAULT_INSTALLED_FEATURE_PATH + remoteFeature.getVersionedIdentifier().toString());
 				assertTrue("feature info not installed locally:"+featureFile, featureFile.exists());
 	
 				File featureFileXML = new File(site, Site.DEFAULT_INSTALLED_FEATURE_PATH + remoteFeature.getVersionedIdentifier().toString() + File.separator + "feature.xml");
 				assertTrue("feature info not installed locally: no feature.xml", featureFileXML.exists());
 			}
 		}
 
 		if (!execFeature && !packFeature){
 			fail("cannot find one executable and one package feature on teh site");
 		}
 
 		//cleanup target 
 		UpdateManagerUtils.removeFromFileSystem(target);
 
 
 	}
 	
 }
