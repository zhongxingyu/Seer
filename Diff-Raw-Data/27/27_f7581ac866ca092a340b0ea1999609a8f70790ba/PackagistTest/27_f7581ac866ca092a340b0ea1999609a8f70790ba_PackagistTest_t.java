 /*******************************************************************************
  * Copyright (c) 2012 The PDT Extension Group (https://github.com/pdt-eg)
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  ******************************************************************************/
 package org.getcomposer.test;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.List;
 
 import junit.framework.TestCase;
 
 import org.getcomposer.core.PHPPackage;
 import org.getcomposer.core.PackageInterface;
 import org.getcomposer.core.packagist.PackageDownloader;
 import org.getcomposer.core.packagist.PharDownloader;
 import org.getcomposer.core.packagist.SearchResultDownloader;
 import org.junit.Test;
 
 
 public class PackagistTest extends TestCase {
 
 	public void testComposerDownload() {
 		
 		try {
 			PharDownloader downloader = new PharDownloader();
 			InputStream resource = downloader.downloadResource();
 			assertNotNull(resource);
 		} catch (IOException e) {
 			e.printStackTrace();
 			fail();
 		}
 	}
 	
 	@Test
 	public void testPackage() {
 		
 		try {
 			PackageDownloader downloader = new PackageDownloader("https://packagist.org/packages/react/react.json");
 			PHPPackage resource = downloader.getPackage();
 			
 			assertTrue(resource != null);
 			assertEquals("react/react", resource.name);
 			assertEquals("Nuclear Reactor written in PHP.", resource.description);
 			assertNotNull(resource.versions);
 			assertTrue(resource.versions.size() > 1);
 			
			
			downloader = new PackageDownloader("https://packagist.org/packages/zendframework/zendframework.json");
			resource = downloader.getPackage();
			
 		} catch (Exception e) {
 			e.printStackTrace();
 			fail();
 		}
 	}
 	
 	@Test
 	public void testSearch() {
 		
 		try {
 			assertSearchResult("html");
 			assertSearchResult("react");
 		} catch (Exception e) {
 			e.printStackTrace();
 			fail();
 		}
 	}
 	
 	protected void assertSearchResult(String query) throws IOException {
 		
 		SearchResultDownloader downloader = new SearchResultDownloader();
 		List<PackageInterface> packages = downloader.searchPackages(query);
 
 		assertNotNull(packages);
 		assertTrue(packages.size() > 0);
 		
 		for (PackageInterface phpPackage : packages) {
 			assertNotNull(phpPackage);
 			assertNotNull(phpPackage.getName());
 			assertNotNull(phpPackage.getDescription());
 		}
 	}
 }
