 /*
  * Copyright 2013 Marcus Behrendt, Gerrit Appeler & Robert Friedrichs
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
 
 package de.behrfriedapp.webshop.server.data;
 
 import de.behrfriedapp.webshop.server.web.ImageEnrichmentFacade;
 import de.behrfriedapp.webshop.shared.data.DetailedProductInfo;
 import de.behrfriedapp.webshop.shared.data.ShortProductInfo;
 import de.behrfriedapp.webshop.shared.data.WProductGroupInfo;
 import junit.framework.Assert;
 import org.easymock.EasyMock;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.util.List;
 
 /**
  * @author marcus
  */
 public class DefaultServerDataAccessTest {
 
 	private DefaultServerDataAccess serverDataAccess;
 	private ImageEnrichmentFacade imageEnrichmentFacade;
 
 	@Before
 	public void setUp() throws Exception {
 		this.imageEnrichmentFacade = EasyMock.createMock(ImageEnrichmentFacade.class);
 		this.serverDataAccess = new DefaultServerDataAccess(this.imageEnrichmentFacade);
 
 	}
 
 	@After
 	public void tearDown() throws Exception {
 
 	}
 
 	@Test
 	public void testGetAllCategories() throws Exception {
 		final List<WProductGroupInfo> result = this.serverDataAccess.getAllProductGroups();
 
 		Assert.assertNotNull(result);
 		Assert.assertFalse(result.size() == 0);
 	}
 
 	@Test
 	public void testGetAllCategories_limit() throws Exception {
 		final List<WProductGroupInfo> result = this.serverDataAccess.getAllProductGroups(10);
 
 		Assert.assertNotNull(result);
 		Assert.assertEquals(10, result.size());;
 	}
 
 	@Test
 	public void testGetAllProducts() throws Exception {
 		final List<String> result = this.serverDataAccess.getAllProducts();
 
 		Assert.assertNotNull(result);
 		Assert.assertFalse(result.size() == 0);
 	}
 
 	@Test
 	public void testGetAllProducts_limit() throws Exception {
 		final List<ShortProductInfo> result = this.serverDataAccess.getAllProducts(10);
 
 		Assert.assertNotNull(result);
 		Assert.assertEquals(10, result.size());
 	}
 
 	@Test
 	public void testGetAllProducts_wCategory() throws Exception {
 		final List<ShortProductInfo> result = this.serverDataAccess.getAllProducts(new WProductGroupInfo(11, 12, "Wurst", "Schinken"));
 
 		Assert.assertNotNull(result);
 		Assert.assertFalse(result.size() == 0);
 	}
 
 	@Test
 	public void testGetAllProducts_wCategory_limit() throws Exception {
 		final List<ShortProductInfo> result = this.serverDataAccess.getAllProducts(new WProductGroupInfo(11, 12, "Wurst", "Schinken"), 4);
 
 		Assert.assertNotNull(result);
 		Assert.assertEquals(4, result.size());
 	}
 
 	@Test
 	public void testGetDetailedProductInfo() throws Exception {
		final DetailedProductInfo result = this.serverDataAccess.getDetailedProductInfo(1);
 		Assert.assertNotNull(result);
 		Assert.assertEquals("BBB Best Apple Jelly", result.getName());
 		Assert.assertEquals(1.48, result.getPrice());
 		Assert.assertEquals("Unilever", result.getManufactor());
 		Assert.assertEquals(285, result.getStock());
 	}
 }
