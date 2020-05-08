 /*******************************************************************************
  * Copyright (c) 2010 The Eclipse Foundation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     The Eclipse Foundation  - initial API and implementation
  *******************************************************************************/
 package org.eclipse.epp.mpc.tests.service.xml;
 
 import static junit.framework.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.eclipse.epp.internal.mpc.core.service.Catalog;
 import org.eclipse.epp.internal.mpc.core.service.CatalogBranding;
 import org.eclipse.epp.internal.mpc.core.service.Catalogs;
 import org.eclipse.epp.internal.mpc.core.service.Categories;
 import org.eclipse.epp.internal.mpc.core.service.Category;
 import org.eclipse.epp.internal.mpc.core.service.Favorites;
 import org.eclipse.epp.internal.mpc.core.service.Featured;
 import org.eclipse.epp.internal.mpc.core.service.Market;
 import org.eclipse.epp.internal.mpc.core.service.Marketplace;
 import org.eclipse.epp.internal.mpc.core.service.Node;
 import org.eclipse.epp.internal.mpc.core.service.Recent;
 import org.eclipse.epp.internal.mpc.core.service.Search;
 import org.eclipse.epp.internal.mpc.core.service.Tag;
 import org.eclipse.epp.internal.mpc.core.service.xml.Unmarshaller;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.BlockJUnit4ClassRunner;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.XMLReader;
 
 /**
  * @author David Green
  * @author Benjamin Muskalla
  */
 @RunWith(BlockJUnit4ClassRunner.class)
 public class UnmarshallerTest {
 
 	private Unmarshaller unmarshaller;
 
 	private XMLReader reader;
 
 	@Before
 	public void before() throws SAXException, ParserConfigurationException {
 		unmarshaller = new Unmarshaller();
 
 		SAXParserFactory parserFactory = SAXParserFactory.newInstance();
 		parserFactory.setNamespaceAware(true);
 		reader = parserFactory.newSAXParser().getXMLReader();
 		reader.setContentHandler(unmarshaller);
 	}
 
 	@Test
 	public void marketplaceRoot() throws IOException, SAXException {
 		// from http://www.eclipseplugincentral.net/xml
 		Object model = process("resources/marketplace-root.xml");
 		assertNotNull(model);
 		assertTrue(model instanceof Marketplace);
 		Marketplace marketplace = (Marketplace) model;
 
 		assertEquals(4, marketplace.getMarket().size());
 
 		Market market = marketplace.getMarket().get(0);
 
 		assertEquals("31", market.getId());
 		assertEquals("Tools", market.getName());
 		assertEquals("http://www.eclipseplugincentral.net/category/markets/tools", market.getUrl());
 
 		assertEquals(36, market.getCategory().size());
 		Category category = market.getCategory().get(10);
 		assertEquals("24", category.getId());
 		assertEquals("IDE", category.getName());
 		assertEquals("http://www.eclipseplugincentral.net/taxonomy/term/24%2C31", category.getUrl());
 	}
 
 	@Test
 	public void categoryTaxonomy() throws IOException, SAXException {
 		// from http://www.eclipseplugincentral.net/taxonomy/term/38,31/xml
 		Object model = process("resources/category-taxonomy.xml");
 		assertNotNull(model);
 		assertTrue(model instanceof Marketplace);
 		Marketplace marketplace = (Marketplace) model;
 
 		assertEquals(1, marketplace.getCategory().size());
 
 		Category category = marketplace.getCategory().get(0);
 		assertEquals("38,31", category.getId());
 		assertEquals("Mylyn Connectors", category.getName());
 		assertEquals("http://www.eclipseplugincentral.net/taxonomy/term/38%2C31", category.getUrl());
 
 		assertEquals(9, category.getNode().size());
 
 		Node node = category.getNode().get(0);
 		//		<node id="641" name="Tasktop Pro">
 		//        <url>http://www.eclipseplugincentral.net/content/tasktop-pro</url>
 		//        <favorited>3</favorited>
 		//      </node>
 		assertEquals("641", node.getId());
 		assertEquals("Tasktop Pro", node.getName());
 		assertEquals("http://www.eclipseplugincentral.net/content/tasktop-pro", node.getUrl());
 	}
 
 	@Test
 	public void node() throws IOException, SAXException {
 		// from http://www.eclipseplugincentral.net/content/mylyn-wikitext-lightweight-markup-editing-tools-and-framework/xml
 		Object model = process("resources/node.xml");
 		assertNotNull(model);
 		assertTrue(model instanceof Marketplace);
 		Marketplace marketplace = (Marketplace) model;
 
 		assertEquals(1, marketplace.getNode().size());
 
 		Node node = marketplace.getNode().get(0);
 		assertEquals("1065", node.getId());
 		assertEquals("Mylyn WikiText - Lightweight Markup Editing, Tools and Framework", node.getName());
 		assertEquals(
 				"http://www.eclipseplugincentral.net/content/mylyn-wikitext-lightweight-markup-editing-tools-and-framework",
 				node.getUrl());
 
 		assertNotNull(node.getBody());
 		assertTrue(node.getBody().startsWith("Mylyn WikiText is a"));
 		assertTrue(node.getBody().endsWith("FAQ</a>."));
 
 		assertNotNull(node.getCategories());
 		assertEquals(5, node.getCategories().getCategory().size());
 		Category category = node.getCategories().getCategory().get(1);
 		// <category name='Tools'>/taxonomy/term/17</category>
 		assertEquals("Tools", category.getName());
 		// FIXME category id.
 
 		assertNotNull(node.getCreated());
 		assertEquals(1259955243L, node.getCreated().getTime());
 		assertEquals(new Integer(3), node.getFavorited());
 		assertEquals(Boolean.TRUE, node.getFoundationmember());
 		assertNotNull(node.getChanged());
 		assertEquals(1259964722L, node.getChanged().getTime());
 		assertEquals("David Green", node.getOwner());
 		assertEquals("resource", node.getType());
 
 	}
 
 	@Test
 	public void featured() throws IOException, SAXException {
 		// from http://www.eclipseplugincentral.net/api/v2/featured
 		Object model = process("resources/featured.xml");
 		assertNotNull(model);
 		assertTrue(model instanceof Marketplace);
 		Marketplace marketplace = (Marketplace) model;
 
 		Featured featured = marketplace.getFeatured();
 		assertNotNull(featured);
 		assertEquals(Integer.valueOf(6), featured.getCount());
 
 		assertEquals(6, featured.getNode().size());
 		Node node = featured.getNode().get(0);
 		assertEquals("248", node.getId());
 		assertEquals("eUML2 free edition", node.getName());
 		assertEquals("http://www.eclipseplugincentral.net/content/euml2-free-edition", node.getUrl());
 		assertEquals("resource", node.getType());
 		Categories categories = node.getCategories();
 		assertNotNull(categories);
 		assertEquals(1, categories.getCategory().size());
 		Category category = categories.getCategory().get(0);
 		assertEquals("19", category.getId());
 		assertEquals("UML", category.getName());
 		assertEquals("http://www.eclipseplugincentral.net/taxonomy/term/19", category.getUrl());
 		assertEquals("Yves YANG", node.getOwner());
 		assertEquals(Integer.valueOf(0), node.getFavorited());
 		assertNotNull(node.getBody());
 		//bug 303149		assertTrue(node.getBody().startsWith("<P><STRONG>eUML2 for Java<"));
 		//bug 303149		assertTrue(node.getBody().endsWith("</LI></UL>"));
 		assertTrue(node.getFoundationmember());
 		assertEquals("http://www.soyatec.com/", node.getHomepageurl());
 		assertEquals("http://www.soyatec.com/euml2/images/product_euml2_110x80.png", node.getImage());
 		assertEquals("3.4", node.getVersion());
 		assertEquals("Free for non-commercial use", node.getLicense());
 		assertEquals("Soyatec", node.getCompanyname());
 		assertEquals("Mature", node.getStatus());
 		assertEquals("3.4.x/3.5.x", node.getEclipseversion());
 		assertEquals("http://www.soyatec.com/forum", node.getSupporturl());
 		assertEquals("http://www.soyatec.com/update", node.getUpdateurl());
 
 	}
 
 	@Test
 	public void search() throws IOException, SAXException {
 		// from http://www.eclipseplugincentral.net/api/v2/search/apachesolr_search/test?filters=tid:16%20tid:31
 		Object model = process("resources/search.xml");
 		assertNotNull(model);
 		assertTrue(model instanceof Marketplace);
 		Marketplace marketplace = (Marketplace) model;
 
 		Search search = marketplace.getSearch();
 		assertNotNull(search);
 
 		assertEquals("test", search.getTerm());
 		assertEquals("http://www.eclipseplugincentral.net/search/apachesolr/test?filters=tid%3A16%20tid%3A31",
 				search.getUrl());
 		assertEquals(Integer.valueOf(62), search.getCount());
 		assertEquals(7, search.getNode().size());
 		Node node = search.getNode().get(0);
 
 		assertEquals("983", node.getId());
 		assertEquals("Run All Tests", node.getName());
 		assertEquals("http://www.eclipseplugincentral.net/content/run-all-tests", node.getUrl());
 		assertEquals("resource", node.getType());
 		Categories categories = node.getCategories();
 		assertNotNull(categories);
 		assertEquals(1, categories.getCategory().size());
 		Category category = categories.getCategory().get(0);
 		assertEquals("16", category.getId());
 		assertEquals("Testing", category.getName());
 		assertEquals("http://www.eclipseplugincentral.net/taxonomy/term/16", category.getUrl());
 		assertEquals("ipreuss", node.getOwner());
 		assertEquals(Integer.valueOf(0), node.getFavorited());
 		assertNotNull(node.getBody());
 		assertEquals("Allows the execution of JUnit tests for several projects at once.", node.getBody());
 		assertTrue(!node.getFoundationmember());
 		assertEquals("https://sourceforge.net/projects/e-rat/", node.getHomepageurl());
 		assertNull(node.getImage());
 		assertEquals("1.0.1", node.getVersion());
 		assertEquals("Other", node.getLicense());
 		assertEquals("Ilja Preu\u00DF", node.getCompanyname());
 		assertEquals("Production/Stable", node.getStatus());
 		assertEquals("3.5", node.getEclipseversion());
 		assertEquals("https://sourceforge.net/projects/e-rat/support", node.getSupporturl());
 		assertEquals("http://e-rat.sf.net/updatesite", node.getUpdateurl());
 
 		Node lastNode = search.getNode().get(search.getNode().size() - 1);
 
 		assertEquals("1011", lastNode.getId());
 		assertEquals("JUnit Flux", lastNode.getName());
 	}
 
 	@Test
 	public void favorites() throws IOException, SAXException {
 		// from http://www.eclipseplugincentral.net/favorites/top/api/p
 
 		Object model = process("resources/favorites.xml");
 		assertNotNull(model);
 		assertTrue(model instanceof Marketplace);
 		Marketplace marketplace = (Marketplace) model;
 
 		Favorites favorites = marketplace.getFavorites();
 		assertNotNull(favorites);
 		assertEquals(Integer.valueOf(6), favorites.getCount());
 
 		assertEquals(6, favorites.getNode().size());
 
 		Node node = favorites.getNode().get(0);
 
 		assertEquals("206", node.getId());
 		assertEquals("Mylyn", node.getName());
 		assertEquals("http://www.eclipseplugincentral.net/content/mylyn", node.getUrl());
 		assertEquals("resource", node.getType());
 		Categories categories = node.getCategories();
 		assertNotNull(categories);
 		assertEquals(1, categories.getCategory().size());
 		Category category = categories.getCategory().get(0);
 		assertEquals("18", category.getId());
 		assertEquals("UI", category.getName());
 		assertEquals("http://www.eclipseplugincentral.net/taxonomy/term/18", category.getUrl());
 		assertEquals("Robert Elves", node.getOwner());
 		assertEquals(Integer.valueOf(16), node.getFavorited());
 		assertNotNull(node.getBody());
 		assertEquals(
 				"Mylyn is a task-focused interface for Eclipse that reduces information overload and makes multi-tasking easy. It does this by making tasks a first class part of Eclipse, and integrating rich and offline editing for repositories such as Bugzilla, Trac, and JIRA. Once your tasks are integrated, Mylyn monitors your work activity to identify information relevant to the task-at-hand, and uses this task context to focus the Eclipse UI on the interesting information, hide the uninteresting, and automatically find what&#039;s related. This puts the information you need to get work done at your fingertips and improves productivity by reducing searching, scrolling, and navigation. By making task context explicit Mylyn also facilitates multitasking, planning, reusing past efforts, and sharing expertise. ",
 				node.getBody());
 		assertTrue(node.getFoundationmember());
 		assertEquals("http://eclipse.org/mylyn", node.getHomepageurl());
 		assertEquals("http://www.eclipse.org/mylyn/images/image-epic.gif", node.getImage());
 		assertEquals("3.3", node.getVersion());
 		assertEquals("EPL", node.getLicense());
 		assertEquals("Eclipse.org", node.getCompanyname());
 		assertEquals("Production/Stable", node.getStatus());
 		assertEquals("3.5, 3.4 and 3.3", node.getEclipseversion());
 		assertEquals("http://eclipse.org/mylyn/community/", node.getSupporturl());
 		assertEquals("http://download.eclipse.org/tools/mylyn/update/e3.4", node.getUpdateurl());
 	}
 
 	@Test
 	public void recent() throws IOException, SAXException {
 		// from http://www.eclipseplugincentral.net/featured/top/api/p
 
 		Object model = process("resources/recent.xml");
 		assertNotNull(model);
 		assertTrue(model instanceof Marketplace);
 		Marketplace marketplace = (Marketplace) model;
 
 		Recent recent = marketplace.getRecent();
 		assertNotNull(recent);
 		assertEquals(Integer.valueOf(6), recent.getCount());
 
 		assertEquals(6, recent.getNode().size());
 
 		Node node = recent.getNode().get(0);
 
 		assertEquals("1091", node.getId());
 		assertEquals("API Demonstration Listing", node.getName());
 		assertEquals("http://www.eclipseplugincentral.net/content/api-demonstration-listing", node.getUrl());
 
 		assertEquals("resource", node.getType());
 		Categories categories = node.getCategories();
 		assertNotNull(categories);
 		assertEquals(6, categories.getCategory().size());
 		Category category = categories.getCategory().get(0);
 		assertEquals("3", category.getId());
 		assertEquals("Database", category.getName());
 		assertEquals("http://www.eclipseplugincentral.net/taxonomy/term/3", category.getUrl());
 		category = categories.getCategory().get(5);
 		assertEquals("38", category.getId());
 		assertEquals("Mylyn Connectors", category.getName());
 		assertEquals("http://www.eclipseplugincentral.net/category/categories/mylyn-connectors", category.getUrl());
 
 		assertEquals("admin", node.getOwner());
 		assertEquals(Integer.valueOf(0), node.getFavorited());
 		assertNotNull(node.getBody());
 		assertTrue(node.getBody().startsWith("Lorem ipsum dolor"));
 		assertTrue(node.getBody().endsWith("vitae aliquam lectus."));
 		assertTrue(node.getFoundationmember());
 		assertEquals("http://marketplace.eclipse.org/xmlapi", node.getHomepageurl());
 		assertEquals("http://marketplace.eclipse.org/sites/default/files/equinox.png", node.getImage());
 		assertEquals("1.0", node.getVersion());
 		assertEquals("EPL", node.getLicense());
 		assertEquals("Eclipse Foundation Inc.", node.getCompanyname());
 		assertEquals("Mature", node.getStatus());
 		assertEquals("3.5", node.getEclipseversion());
 		assertEquals("http://marketplace.eclipse.org/support", node.getSupporturl());
 		assertEquals("http://update.eclipse.org/marketplace", node.getUpdateurl());
 
 		{
 			String[] expectedIus = new String[] { "org.eclipse.one.one", "org.eclipse.one.two", "org.eclipse.two.one",
 					"org.eclipse.three.one", };
 			assertNotNull(node.getIus());
 			assertEquals(expectedIus.length, node.getIus().getIu().size());
 			for (int x = 0; x < expectedIus.length; ++x) {
 				assertEquals(expectedIus[x], node.getIus().getIu().get(x));
 			}
 		}
 		{
 			String[] expectedPlatforms = new String[] { "Windows", "Mac", "Linux/GTK", };
 			assertNotNull(node.getPlatforms());
 			assertEquals(expectedPlatforms.length, node.getPlatforms().getPlatform().size());
 			for (int x = 0; x < expectedPlatforms.length; ++x) {
 				assertEquals(expectedPlatforms[x], node.getPlatforms().getPlatform().get(x));
 			}
 		}
 
 	}
 
 	@Test
 	public void tags() throws Exception {
 		Object model = process("resources/node.xml");
 		Marketplace marketplace = (Marketplace) model;
 		Node node = marketplace.getNode().get(0);
 
 		assertNotNull(node.getTags());
 		assertEquals(5, node.getCategories().getCategory().size());
 		Tag tag = node.getTags().getTags().get(3);
 		assertEquals("mylyn", tag.getName());
 		assertEquals("88", tag.getId());
 		assertEquals("http://marketplace.eclipse.org/category/free-tagging/mylyn", tag.getUrl());
 	}
 
 	@Test
 	public void marketplaceCatalogs() throws IOException, SAXException {
 		Object model = process("resources/catalogs.xml");
 		assertNotNull(model);
 		assertTrue(model instanceof Catalogs);
 		Catalogs catalogs = (Catalogs) model;
 
 		assertEquals(2, catalogs.getCatalogs().size());
 
 		//	     <catalog id="35656" title="Marketplace Catalog" url="http://marketplace.eclipse.org" selfContained="1"  dependencyRepository="http://download.eclipse.org/releases/helios">
 		//	        <description>Here is a description</description>
 		//	        <icon>http://marketplace.eclipse.org/sites/default/files/jacket.jpg</icon>
 		//	        <wizard title="Eclipse Marketplace Catalog">
 		//	          <icon>http://marketplace.eclipse.org/sites/default/files/giant-rabbit2.jpg</icon>
 		//	          <searchtab enabled='1'>Search</searchtab>
 		//	          <populartab enabled='1'>Popular</populartab>
 		//	          <recenttab enabled='1'>Recent</recenttab>
 		//	        </wizard>
 		//	      </catalog>
 
 		Catalog catalog = catalogs.getCatalogs().get(0);
 		assertEquals("35656", catalog.getId());
 		assertEquals("Marketplace Catalog", catalog.getName());
 		assertEquals("http://marketplace.eclipse.org", catalog.getUrl());
 		assertEquals("Here is a description", catalog.getDescription());
 		assertTrue(catalog.isSelfContained());
		assertEquals("http://marketplace.eclipse.org/sites/default/files/marketplace32.png", catalog.getImageUrl());
 		assertEquals("http://download.eclipse.org/releases/helios", catalog.getDependencyRepository());
 
 		CatalogBranding branding = catalog.getBranding();
 		assertNotNull(branding);
 		assertEquals("Eclipse Marketplace Catalog", branding.getWizardTitle());
 		assertEquals("http://marketplace.eclipse.org/sites/default/files/giant-rabbit2.jpg", branding.getWizardIcon());
 		assertEquals("Search", branding.getSearchTabName());
 		assertEquals("Popular", branding.getPopularTabName());
 		assertEquals("Recent", branding.getRecentTabName());
 		assertTrue(branding.hasSearchTab());
 		assertFalse(branding.hasPopularTab());
 		assertTrue(branding.hasRecentTab());
 
 	}
 
 	private Object process(String resource) throws IOException, SAXException {
 		InputStream in = UnmarshallerTest.class.getResourceAsStream(resource);
 		if (in == null) {
 			throw new IllegalStateException(resource);
 		}
 		try {
 			reader.parse(new InputSource(in));
 		} finally {
 			in.close();
 		}
 		return unmarshaller.getModel();
 	}
 }
