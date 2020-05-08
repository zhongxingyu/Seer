 /*******************************************************************************
  * Copyright (c) 2010 The Eclipse Foundation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * 	The Eclipse Foundation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.epp.mpc.tests.service;
 
 import static junit.framework.Assert.assertFalse;
 import static junit.framework.Assert.assertNotNull;
 import static org.junit.Assert.assertEquals;
 
 import java.util.Collections;
 import java.util.List;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.epp.internal.mpc.core.ServiceLocator;
 import org.eclipse.epp.internal.mpc.core.service.Catalog;
 import org.eclipse.epp.internal.mpc.core.service.CatalogService;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.BlockJUnit4ClassRunner;
 
 /**
  * @author Benjamin Muskalla
  */
 @RunWith(BlockJUnit4ClassRunner.class)
 public class CatalogServiceTest {
 
 	private final class MockCatalogService implements CatalogService {
 		public List<Catalog> listCatalogs(IProgressMonitor monitor) throws CoreException {
 			Catalog catalog = new Catalog();
 			catalog.setId("mock");
 			return Collections.singletonList(catalog);
 		}
 	}
 
 	private CatalogService catalogService;
 
 	@Before
 	public void setUp() throws Exception {
		catalogService = ServiceLocator.getInstance().getCatalogService();
 	}
 
 	@Test
 	public void listCatalogs() throws CoreException {
 		List<Catalog> catalogs = catalogService.listCatalogs(new NullProgressMonitor());
 		assertNotNull(catalogs);
 		assertFalse(catalogs.isEmpty());
 
 		for (Catalog catalog : catalogs) {
 			assertNotNull(catalog.getId());
 			assertNotNull(catalog.getUrl());
 			assertNotNull(catalog.getName());
 		}
 	}
 
 	@Test
 	public void testSampleCatalog() throws Exception {
 		ServiceLocator.setInstance(new ServiceLocator() {
 			@Override
 			public CatalogService getCatalogService() {
 				return new MockCatalogService();
 			}
 		});
 		catalogService = ServiceLocator.getInstance().getCatalogService();
 		List<Catalog> catalogs = catalogService.listCatalogs(null);
 		assertEquals(1, catalogs.size());
 		assertEquals("mock", catalogs.get(0).getId());
 	}
 }
