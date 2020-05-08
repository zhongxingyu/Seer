 /**
 Copyright (c) 2012 Delcyon, Inc.
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 package com.delcyon.capo.resourcemanager;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 /**
  * @author jeremiah
  *
  */
 public class ResourceURITest
 {
 
 	private static final String testURI = "jdbc:hsqldb:file:testdb/testdb?user=user\\?&password&\\&\\!systems=\\=!systems?id=:242";
 	private static final String testSemiColonURI = "jdbc:hsqldb:file:testdb/testdb?user=user\\?;password;\\;\\!systems=\\=!systems?id=:242";
 	private static final String testMixedURI = "jdbc:hsqldb:file:testdb/testdb?user=user\\?&password;\\&\\;semicolon=\\;&ampersand=\\&&\\!systems=\\=!systems?id=:242";
 	private static final String opaqueTestURI = "scheme:/schemespecific";
 	private static final String testURL = "foo://username:password@example.com:8042/over/there/index.dtb?type=animal&name=narwhal#nose";
 	private static final String testURNPath = "urn:example:animal:ferret:nose";
 	private static final String testMailToURN = "mailto:username@example.com?subject=Topic";
	private static final String testMultiColonPathURI = "file:/home/jeremiah/java-work/delcyon-capo/capo/server/clients/by-MAC/cc:52:af:3e:61:6a";
 	
 	/**
 	 * Test method for {@link com.delcyon.capo.resourcemanager.ResourceURI#ResourceURI(java.lang.String)}.
 	 */
 	@Test
 	public void testResourceURI()
 	{
 		ResourceURI resourceURI = new ResourceURI(testURI);
 		Assert.assertEquals(null, resourceURI.getAuthority());
 		Assert.assertEquals("jdbc:hsqldb:file:testdb/testdb?user=user\\?&password&\\&!systems=\\=", resourceURI.getBaseURI());		
 		Assert.assertEquals(null, resourceURI.getHierarchy());
 		Assert.assertEquals(null, resourceURI.getHostname());		
 		Assert.assertEquals("testdb/testdb", resourceURI.getPath());
 		Assert.assertEquals(null, resourceURI.getPort());
 		Assert.assertEquals("user=user?&password&\\&!systems=\\=", resourceURI.getQuery());
 		Assert.assertEquals(null, resourceURI.getFragment());
 		Assert.assertEquals("jdbc:hsqldb:file:testdb/testdb?user=user\\?&password&\\&\\!systems=\\=!systems?id=:242", resourceURI.getResourceURIString());
 		Assert.assertEquals("jdbc", resourceURI.getScheme());
 		Assert.assertEquals("hsqldb:file:testdb/testdb?user=user\\?&password&\\&\\!systems=\\=!systems?id=:242", resourceURI.getSchemeSpecificPart());
 		Assert.assertEquals(null, resourceURI.getUserInfo());
 		Assert.assertEquals(true, resourceURI.isOpaque());
 		Assert.assertEquals(3, resourceURI.getParameterMap().size());
 		Assert.assertEquals("user?", resourceURI.getParameterMap().get("user"));
 		Assert.assertEquals("", resourceURI.getParameterMap().get("password"));
 		Assert.assertEquals("=", resourceURI.getParameterMap().get("&!systems"));
 		Assert.assertNotNull(resourceURI.getChildResourceURI());
 		Assert.assertEquals(1, resourceURI.getChildResourceURI().getParameterMap().size());
 		Assert.assertEquals(":242", resourceURI.getChildResourceURI().getParameterMap().get("id"));
 		
 		
 		resourceURI = new ResourceURI(testSemiColonURI);
 		Assert.assertEquals(null, resourceURI.getAuthority());
 		Assert.assertEquals("jdbc:hsqldb:file:testdb/testdb?user=user\\?;password;\\;!systems=\\=", resourceURI.getBaseURI());		
 		Assert.assertEquals(null, resourceURI.getHierarchy());
 		Assert.assertEquals(null, resourceURI.getHostname());		
 		Assert.assertEquals("testdb/testdb", resourceURI.getPath());
 		Assert.assertEquals(null, resourceURI.getPort());
 		Assert.assertEquals("user=user?;password;\\;!systems=\\=", resourceURI.getQuery());
 		Assert.assertEquals(null, resourceURI.getFragment());
 		Assert.assertEquals("jdbc:hsqldb:file:testdb/testdb?user=user\\?;password;\\;\\!systems=\\=!systems?id=:242", resourceURI.getResourceURIString());
 		Assert.assertEquals("jdbc", resourceURI.getScheme());
 		Assert.assertEquals("hsqldb:file:testdb/testdb?user=user\\?;password;\\;\\!systems=\\=!systems?id=:242", resourceURI.getSchemeSpecificPart());
 		Assert.assertEquals(null, resourceURI.getUserInfo());
 		Assert.assertEquals(true, resourceURI.isOpaque());
 		Assert.assertEquals(3, resourceURI.getParameterMap().size());
 		Assert.assertEquals("user?", resourceURI.getParameterMap().get("user"));
 		Assert.assertEquals("", resourceURI.getParameterMap().get("password"));
 		Assert.assertEquals("=", resourceURI.getParameterMap().get(";!systems"));
 		Assert.assertNotNull(resourceURI.getChildResourceURI());
 		Assert.assertEquals(1, resourceURI.getChildResourceURI().getParameterMap().size());
 		Assert.assertEquals(":242", resourceURI.getChildResourceURI().getParameterMap().get("id"));
 		
 		resourceURI = new ResourceURI(testMixedURI);
 		Assert.assertEquals(null, resourceURI.getAuthority());
 		Assert.assertEquals("jdbc:hsqldb:file:testdb/testdb?user=user\\?&password;\\&\\;semicolon=\\;&ampersand=\\&&!systems=\\=", resourceURI.getBaseURI());		
 		Assert.assertEquals(null, resourceURI.getHierarchy());
 		Assert.assertEquals(null, resourceURI.getHostname());		
 		Assert.assertEquals("testdb/testdb", resourceURI.getPath());
 		Assert.assertEquals(null, resourceURI.getPort());
 		Assert.assertEquals("user=user?&password;\\&\\;semicolon=\\;&ampersand=\\&&!systems=\\=", resourceURI.getQuery());
 		Assert.assertEquals(null, resourceURI.getFragment());
 		Assert.assertEquals("jdbc:hsqldb:file:testdb/testdb?user=user\\?&password;\\&\\;semicolon=\\;&ampersand=\\&&\\!systems=\\=!systems?id=:242", resourceURI.getResourceURIString());
 		Assert.assertEquals("jdbc", resourceURI.getScheme());
 		Assert.assertEquals("hsqldb:file:testdb/testdb?user=user\\?&password;\\&\\;semicolon=\\;&ampersand=\\&&\\!systems=\\=!systems?id=:242", resourceURI.getSchemeSpecificPart());
 		Assert.assertEquals(null, resourceURI.getUserInfo());
 		Assert.assertEquals(true, resourceURI.isOpaque());
 		Assert.assertEquals(5, resourceURI.getParameterMap().size());
 		System.out.println(resourceURI.getParameterMap());
 		Assert.assertEquals("user?", resourceURI.getParameterMap().get("user"));
 		Assert.assertEquals("", resourceURI.getParameterMap().get("password"));
 		Assert.assertEquals("&", resourceURI.getParameterMap().get("ampersand"));
 		Assert.assertEquals(";", resourceURI.getParameterMap().get("&;semicolon"));
 		Assert.assertEquals("=", resourceURI.getParameterMap().get("!systems"));
 		Assert.assertNotNull(resourceURI.getChildResourceURI());
 		Assert.assertEquals(1, resourceURI.getChildResourceURI().getParameterMap().size());
 		Assert.assertEquals(":242", resourceURI.getChildResourceURI().getParameterMap().get("id"));
 		
 		resourceURI = new ResourceURI(testURL);
 		Assert.assertEquals("username:password@example.com:8042", resourceURI.getAuthority());
 		Assert.assertEquals("foo://username:password@example.com:8042/over/there/index.dtb?type=animal&name=narwhal#nose", resourceURI.getBaseURI());		
 		Assert.assertEquals("username:password@example.com:8042/over/there/index.dtb", resourceURI.getHierarchy());
 		Assert.assertEquals("example.com", resourceURI.getHostname());		
 		Assert.assertEquals("/over/there/index.dtb", resourceURI.getPath());
 		Assert.assertEquals(8042l, (long)resourceURI.getPort());
 		Assert.assertEquals("type=animal&name=narwhal", resourceURI.getQuery());
 		Assert.assertEquals("nose", resourceURI.getFragment());
 		Assert.assertEquals("foo://username:password@example.com:8042/over/there/index.dtb?type=animal&name=narwhal#nose", resourceURI.getResourceURIString());
 		Assert.assertEquals("foo", resourceURI.getScheme());
 		Assert.assertEquals("//username:password@example.com:8042/over/there/index.dtb?type=animal&name=narwhal#nose", resourceURI.getSchemeSpecificPart());
 		Assert.assertEquals("username:password", resourceURI.getUserInfo());
 		Assert.assertEquals(false, resourceURI.isOpaque());
 		Assert.assertEquals(2, resourceURI.getParameterMap().size());
 		Assert.assertEquals("animal", resourceURI.getParameterMap().get("type"));
 		Assert.assertEquals("narwhal", resourceURI.getParameterMap().get("name"));		
 		Assert.assertNull(resourceURI.getChildResourceURI());
 		
 		
 	}
 
 	/**
 	 * Test method for {@link com.delcyon.capo.resourcemanager.ResourceURI#getScheme(java.lang.String)}.
 	 */
 	@Test
 	public void testGetScheme()
 	{
 		Assert.assertEquals("jdbc",ResourceURI.getScheme(testURI));
 		Assert.assertEquals("jdbc",ResourceURI.getScheme(testSemiColonURI));
 		Assert.assertEquals("foo",ResourceURI.getScheme(testURL));
 		Assert.assertEquals("scheme",ResourceURI.getScheme(opaqueTestURI));
 	}
 
 	/**
 	 * Test method for {@link com.delcyon.capo.resourcemanager.ResourceURI#getSchemeSpecificPart(java.lang.String)}.
 	 */
 	@Test
 	public void testGetSchemeSpecificPart()
 	{
 		Assert.assertEquals("hsqldb:file:testdb/testdb?user=user\\?&password&\\&\\!systems=\\=!systems?id=:242",ResourceURI.getSchemeSpecificPart(testURI));
 		Assert.assertEquals("//username:password@example.com:8042/over/there/index.dtb?type=animal&name=narwhal#nose",ResourceURI.getSchemeSpecificPart(testURL));
 	}
 
 	/**
 	 * Test method for {@link com.delcyon.capo.resourcemanager.ResourceURI#isOpaque(java.lang.String)}.
 	 */
 	@Test
 	public void testIsOpaque()
 	{
 		Assert.assertEquals(true,ResourceURI.isOpaque(testURI));
 		Assert.assertEquals(false,ResourceURI.isOpaque(opaqueTestURI));
 		Assert.assertEquals(false,ResourceURI.isOpaque(testURL));
 	}
 
 	/**
 	 * Test method for {@link com.delcyon.capo.resourcemanager.ResourceURI#getAuthroity(java.lang.String)}.
 	 */
 	@Test
 	public void testGetAuthroity()
 	{
 		Assert.assertEquals("username:password@example.com:8042",ResourceURI.getAuthroity(testURL));
 		Assert.assertEquals(null,ResourceURI.getAuthroity(testMailToURN));
 		Assert.assertEquals(null,ResourceURI.getAuthroity(testURI));
 		Assert.assertEquals(null,ResourceURI.getAuthroity(testURNPath));
 	}
 
 	@Test
 	public void testGetUserInfo()
 	{
 		Assert.assertEquals("username:password",ResourceURI.getUserInfo(testURL));
 	}
 	
 	@Test
 	public void testGetHostname()
 	{
 		Assert.assertEquals("example.com",ResourceURI.getHostname(testURL));
 		Assert.assertEquals(null,ResourceURI.getHostname(testURI));
 		Assert.assertEquals(null,ResourceURI.getHostname(opaqueTestURI));
 	}
 	
 	@Test
 	public void testGetPort()
 	{
 		Assert.assertEquals(8042l,(long)ResourceURI.getPort(testURL));
 		Assert.assertEquals(null,ResourceURI.getPort(testURI));
 		Assert.assertEquals(null,ResourceURI.getPort(opaqueTestURI));
 	}
 	
 	@Test
 	public void testGetBaseURI()
 	{		
 		Assert.assertEquals("foo://username:password@example.com:8042/over/there/index.dtb?type=animal&name=narwhal#nose",ResourceURI.getBaseURI(testURL));
 		Assert.assertEquals("jdbc:hsqldb:file:testdb/testdb?user=user\\?&password&\\&!systems=\\=",ResourceURI.getBaseURI(testURI));
 		Assert.assertEquals("scheme:/schemespecific",ResourceURI.getBaseURI(opaqueTestURI));
 	}
 	
 	@Test
 	public void testHasHierarchy()
 	{
 		Assert.assertEquals(true,ResourceURI.hasHierarchy(testURL));
 		Assert.assertEquals(false,ResourceURI.hasHierarchy(testURI));
 		Assert.assertEquals(false,ResourceURI.hasHierarchy(opaqueTestURI));
 		Assert.assertEquals(false,ResourceURI.hasHierarchy(testURNPath));
 		Assert.assertEquals(false,ResourceURI.hasHierarchy(testMailToURN));
 
 	}
 	
 	@Test
 	public void testGetHierarchy()
 	{
 		Assert.assertEquals("username:password@example.com:8042/over/there/index.dtb",ResourceURI.getHierarchy(testURL));
 		Assert.assertEquals(null,ResourceURI.getHierarchy(testURI));
 		Assert.assertEquals(null,ResourceURI.getHierarchy(opaqueTestURI));
 		Assert.assertEquals(null,ResourceURI.getHierarchy(testURNPath));
 		Assert.assertEquals(null,ResourceURI.getHierarchy(testMailToURN));
 
 	}
 	
 	@Test
 	public void testGetPath()
 	{
 		Assert.assertEquals("/over/there/index.dtb",ResourceURI.getPath(testURL));
 		Assert.assertEquals("testdb/testdb",ResourceURI.getPath(testSemiColonURI));
 		Assert.assertEquals("testdb/testdb",ResourceURI.getPath(testURI));
 		Assert.assertEquals("/schemespecific",ResourceURI.getPath(opaqueTestURI));
 		Assert.assertEquals("example:animal:ferret:nose",ResourceURI.getPath(testURNPath));
		Assert.assertEquals("username@example.com",ResourceURI.getPath(testMailToURN));
		Assert.assertEquals("/home/jeremiah/java-work/delcyon-capo/capo/server/clients/by-MAC/cc:52:af:3e:61:6a",ResourceURI.getPath(testMultiColonPathURI));
 	}
 	
 	@Test
 	public void testGetQuery()
 	{
 		Assert.assertEquals("type=animal&name=narwhal",ResourceURI.getQuery(testURL));
 		Assert.assertEquals("user=user?&password&\\&!systems=\\=",ResourceURI.getQuery(testURI));
 		Assert.assertEquals("user=user?;password;\\;!systems=\\=",ResourceURI.getQuery(testSemiColonURI));
 		Assert.assertEquals(null,ResourceURI.getQuery(opaqueTestURI));
 		Assert.assertEquals(null,ResourceURI.getQuery(testURNPath));
 		Assert.assertEquals("subject=Topic",ResourceURI.getQuery(testMailToURN));
 	}
 	
 	@Test
 	public void testGetFragment()
 	{
 		Assert.assertEquals("nose",ResourceURI.getFragment(testURL));
 		Assert.assertEquals(null,ResourceURI.getFragment(testURI));
 		Assert.assertEquals(null,ResourceURI.getFragment(opaqueTestURI));
 		Assert.assertEquals(null,ResourceURI.getFragment(testURNPath));
 		Assert.assertEquals(null,ResourceURI.getFragment(testMailToURN));
 
 	}
 	
 	/**
 	 * Test method for {@link com.delcyon.capo.resourcemanager.ResourceURI#removeURN(java.lang.String)}.
 	 */
 	@Test
 	public void testRemoveURN()
 	{
 		Assert.assertEquals("//username:password@example.com:8042/over/there/index.dtb?type=animal&name=narwhal#nose",ResourceURI.removeURN(testURL));
 		Assert.assertEquals("testdb/testdb?user=user\\?&password&\\&\\!systems=\\=!systems?id=:242",ResourceURI.removeURN(testURI));
 		Assert.assertEquals("/schemespecific",ResourceURI.removeURN(opaqueTestURI));
 	}
 
 	@Test
 	public void testGetParts()
 	{
 	    Assert.assertEquals(3, ResourceURI.getParts(testMailToURN).length);
 	    Assert.assertEquals(13, ResourceURI.getParts(testMixedURI).length);
 	    Assert.assertEquals(11, ResourceURI.getParts(testSemiColonURI).length);
 	    Assert.assertEquals(11, ResourceURI.getParts(testURI).length);
 	    Assert.assertEquals(12, ResourceURI.getParts(testURL).length);
 	    Assert.assertEquals(5, ResourceURI.getParts(testURNPath).length);	    
 	}
 	
 }
