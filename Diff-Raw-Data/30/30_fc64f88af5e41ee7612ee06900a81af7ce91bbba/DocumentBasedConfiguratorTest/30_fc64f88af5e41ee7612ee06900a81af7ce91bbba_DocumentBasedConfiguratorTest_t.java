 package net.ech.config;
 
 import java.io.*;
 import java.util.*;
 import net.ech.doc.ChildDocumentResolver;
 import net.ech.doc.Document;
 import net.ech.util.*;
 import org.junit.*;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 public class DocumentBasedConfiguratorTest
 {
 	@Test
 	public void testNotFoundCase() throws Exception
 	{
 		try {
 			configure("thing", Object.class, new Hash());
 		}
 		catch (IOException e) {
 			assertEquals("thing: no such key", e.getMessage());
 		}
 	}
 
 	@Test
 	public void testInstantiateBeanWithExplicitType() throws Exception
 	{
 		Object bean = configure("thing", Object.class, new Hash()
 			.addEntry("thing", new Hash()
 				.addEntry("__type", "net.ech.config.SimpleConfigurable")));
 		assertNotNull(bean);
 		assertTrue(bean instanceof SimpleConfigurable);
 	}
 
 	@Test
 	public void testInstantiateBeanWithTypeHint() throws Exception
 	{
 		SimpleConfigurable bean = configure("thing", SimpleConfigurable.class, new Hash("thing", new Hash()));
 		assertNotNull(bean);
 		assertNull(bean.getProperty());
 	}
 
 	@Test
 	public void testSetBeanProp() throws Exception
 	{
 		SimpleConfigurable bean = configure("thing", SimpleConfigurable.class,
 			new Hash()
 				.addEntry("thing", new Hash()
 					.addEntry("property", "manilla")));
 		assertNotNull(bean);
 		assertEquals("manilla", bean.getProperty());
 	}
 
 	@Test
	public void testBeanConstructorSingleArg() throws Exception
 	{
 		Object bean = configure("thing", Object.class,
 			new Hash()
 				.addEntry("thing", new Hash()
 					.addEntry("__type", "net.ech.config.SimpleConfigurable")
 					.addEntry("__args", new Hash()
 						.addEntry("__type", "net.ech.config.SimpleConfigurable")
 						.addEntry("property", "manilla"))));
 
 		assertNotNull(bean);
 		assertTrue(bean instanceof SimpleConfigurable);
 		assertEquals("manilla", ((SimpleConfigurable) bean).getProperty());
 	}
 
 	@Test
	public void testBeanConstructorListArg() throws Exception
	{
		Object bean = configure("thing", Object.class,
			new Hash()
				.addEntry("thing", new Hash()
					.addEntry("__type", "net.ech.config.Bean")
					.addEntry("__args", Arrays.asList(new Object[] { "hi", 256 }))));

		assertNotNull(bean);
		assertTrue(bean instanceof Bean);
		assertEquals("hi", ((Bean) bean).getProperty());
	}

	@Test
 	public void testPositiveGeneric() throws Exception
 	{
 		Object bean = configure("thing",
 			new Hash()
 				.addEntry("thing", new Hash()
 					.addEntry("property", "manilla")));
 		assertNotNull(bean);
 		assertTrue(bean instanceof Map);
 		assertEquals("manilla", ((Map<String,Object>)bean).get("property"));
 	}
 
 	@Test
 	public void testPositiveGenericAsObject() throws Exception
 	{
 		Object bean = configure("thing", Object.class,
 			new Hash()
 				.addEntry("thing", new Hash()
 					.addEntry("property", "manilla")));
 		assertNotNull(bean);
 		assertTrue(bean instanceof Map);
 		assertEquals("manilla", ((Map<String,Object>)bean).get("property"));
 	}
 
 	@Test
 	public void testPositiveGenericAsMap() throws Exception
 	{
 		Map<String,Object> bean = configure("thing", Map.class,
 			new Hash()
 				.addEntry("thing", new Hash()
 					.addEntry("property", "manilla")));
 		assertNotNull(bean);
 		assertEquals("manilla", bean.get("property"));
 	}
 
 	@Test
 	public void testPositiveBean() throws Exception
 	{
 		Bean bean = configure("thing", Bean.class,
 			new Hash()
 				.addEntry("thing", new Hash()
 					.addEntry("property", "manilla")));
 		assertNotNull(bean);
 		assertEquals("manilla", bean.getProperty());
 	}
 
 	@Test
 	public void testPositiveBeanAsSubtype() throws Exception
 	{
 		IBean bean = configure("thing", IBean.class,
 			new Hash()
 				.addEntry("thing", new Hash()
 					.addEntry("property", "manilla")));
 		assertNotNull(bean);
 		assertTrue(bean instanceof Bean);
 		assertEquals("manilla", ((Bean)bean).getProperty());
 	}
 
 	@Test
 	public void testNegativeBeanAsUnknownSubtype() throws Exception
 	{
 		try {
 			configure("thing", IBean.class,
 				new Hash()
 					.addEntry("thing", new Hash()
 						.addEntry("mopperty", "manilla")));
 			fail("should not be reached");
 		}
 		catch (IOException e) {
 			assertEquals("thing: does not appear to configure a subtype of interface net.ech.config.IBean", e.getMessage());
 			assertNotNull(e.getCause());
 		}
 	}
 
 	@Test
 	public void testNegativeBeanAsAmbiguousSubtype() throws Exception
 	{
 		try {
 			configure("thing", IBean.class, 
 				new Hash()
 					.addEntry("thing", new Hash()
 						.addEntry("property", "manilla")
 						.addEntry("properly", "manilla")));
 			fail("should not be reached");
 		}
 		catch (IOException e) {
 			assertEquals("thing: ambiguous subtype", e.getMessage());
 			assertNotNull(e.getCause());
 		}
 	}
 
 	@Test
 	public void testPositiveArrayAsObject() throws Exception
 	{
 		Object obj = configure("thing", 
 			new Hash()
 				.addEntry("thing", Arrays.asList(new Object[] {
 					new Hash("property", "manilla"),
 					new Hash("property", "vanilla")
 				})));
 		assertNotNull(obj);
 		assertTrue(obj instanceof List);
 		assertEquals(2, ((List<?>) obj).size());
 		assertTrue(((List<?>) obj).get(0) instanceof Map);
 		assertTrue(((List<?>) obj).get(1) instanceof Map);
 	}
 
 	@Test
 	public void testPositiveArrayAsList() throws Exception
 	{
 		Object obj = configure("thing", List.class,
 			new Hash()
 				.addEntry("thing", Arrays.asList(new Object[] {
 					new Hash("property", "manilla"),
 					new Hash("property", "vanilla")
 				})));
 		assertNotNull(obj);
 		assertTrue(obj instanceof List);
 		assertEquals(2, ((List<?>) obj).size());
 		assertTrue(((List<?>) obj).get(0) instanceof Map);
 		assertTrue(((List<?>) obj).get(1) instanceof Map);
 	}
 
 	@Test
 	public void testPositiveListArrayAsArray() throws Exception
 	{
 		Object obj = configure("thing", Bean[].class,
 			new Hash()
 				.addEntry("thing", Arrays.asList(new Object[] {
 					new Hash("property", "manilla"),
 					new Hash("property", "vanilla")
 				})));
 		assertNotNull(obj);
 		assertTrue(obj instanceof Bean[]);
 		assertEquals(2, ((Bean[]) obj).length);
 		assertTrue(((Bean[]) obj)[0] instanceof Bean);
 		assertTrue(((Bean[]) obj)[1] instanceof Bean);
 	}
 
 	@Test
 	public void testNegativeListArrayAsBean() throws Exception
 	{
 		try {
 			configure("thing", Bean.class,
 				new Hash()
 					.addEntry("thing", Arrays.asList(new Object[] {
 						new Hash("property", "manilla"),
 						new Hash("property", "vanilla")
 					})));
 			fail("should not be reached");
 		}
 		catch (IOException e) {
 			assertEquals("thing: cannot coerce [{property=manilla}, {property=vanilla}] to type net.ech.config.Bean", e.getMessage());
 			assertNotNull(e.getCause());
 		}
 	}
 
 	@Test
 	public void testNegativeInterface() throws Exception
 	{
 		try {
 			configure("thing", List.class,
 				new Hash()
 					.addEntry("thing", new Hash()
 						.addEntry("property", "manilla")));
 			fail("should not be reached");
 		}
 		catch (IOException e) {
 			assertEquals("thing: does not appear to configure a subtype of interface java.util.List", e.getMessage());
 			assertNotNull(e.getCause());
 		}
 	}
 
 	@Test
 	public void testExtends() throws Exception
 	{
 		Map<String,String> bean = configure("thing2", Map.class,
 			new Hash()
 				.addEntry("thing1", new Hash()
 					.addEntry("prop1", "a")
 					.addEntry("prop2", "b"))
 				.addEntry("thing2", new Hash()
 					.addEntry("__extends", "thing1")
 					.addEntry("prop2", "bee")
 					.addEntry("prop3", "c")));
 		assertEquals("a", bean.get("prop1"));
 		assertEquals("bee", bean.get("prop2"));
 		assertEquals("c", bean.get("prop3"));
 	}
 
 	@Test
 	public void testRecursiveExtends() throws Exception
 	{
 		Map<String,Object> bean = configure("thing2", Map.class,
 			new Hash()
 				.addEntry("thing1", new Hash()
 					.addEntry("hash", new Hash("x", 1).addEntry("y", 2)))
 				.addEntry("thing2", new Hash()
 					.addEntry("__extends", "thing1")
 					.addEntry("hash", new Hash("x", 0))));
 		assertEquals(new Integer(0), ((Map<String,Object>)bean.get("hash")).get("x"));
 	}
 
 	@Test
 	public void testMultiExtends() throws Exception
 	{
 		Map<String,Object> bean = configure("thing", Map.class,
 			new Hash()
 				.addEntry("thing1", new Hash()
 					.addEntry("h1", new Hash("x", 1).addEntry("y", 2)))
 				.addEntry("thing2", new Hash()
 					.addEntry("h2", new Hash("x", 1).addEntry("y", 2)))
 				.addEntry("thing", new Hash("__extends", new String[] { "thing1", "thing2" })));
 		assertTrue(bean.containsKey("h1"));
 		assertTrue(bean.containsKey("h2"));
 	}
 
 	@Test
 	public void testContextReference() throws Exception
 	{
 		Map<String,Object> bean = configure("thing", Map.class,
 			new Hash()
 				.addEntry("thing", new Hash()
 					.addEntry("__context", new Hash("x", 1))
 					.addEntry("y", new Hash("__ref", "x"))));
 		assertTrue(bean.containsKey("y"));
 		assertEquals(new Integer(1), bean.get("y"));
 	}
 
 	@Test
 	public void testNestedLocalConfigReference() throws Exception
 	{
 		Object bean = configure(new Hash()
 			.addEntry("level", 0)
 			.addEntry("local", new Hash("__ref", "$config.local"))
 			.addEntry("child", new Hash()
 				.addEntry("level", 1)
 				.addEntry("local", new Hash("__ref", "$config.local"))
 				.addEntry("child", new Hash()
 					.addEntry("level", 2)
 					.addEntry("local", new Hash("__ref", "$config.local")))));
 		Object obj = bean;
 		while (obj != null) {
 			assertTrue(obj instanceof Map);
 			Map<String,Object> map = (Map<String,Object>) obj;
 			assertTrue(map.containsKey("level"));
 			int level = (Integer) map.get("level");
 			assertTrue(map.containsKey("local"));
 			assertTrue(map.get("local") instanceof Document);
 			assertEquals(level, (((Document) map.get("local")).find("level").get(Integer.class)).intValue());
 			if (map.containsKey("child")) {
 				obj = map.get("child");
 			}
 			else {
 				obj = null;
 			}
 		}
 	}
 
 	@Test
 	public void testNestedLocalConfigReferenceAsConstructorArg() throws Exception
 	{
 		Object configured = configure(new Hash()
 			.addEntry("__ID", 0)
 			.addEntry("bean", new Hash()
 				.addEntry("__ID", 1)
 				.addEntry("__type", "net.ech.config.DocumentBean")
 				.addEntry("__args", new Hash()
 					.addEntry("__ID", 2)
 					.addEntry("__ref", "$config.local"))));
 		assertTrue("got a container", configured instanceof Map);
 		assertTrue("map contains bean", ((Map<String,Object>)configured).containsKey("bean"));
 		assertTrue("bean is of the correct type", ((Map<String,Object>)configured).get("bean") instanceof DocumentBean);
 		Document doc = ((DocumentBean)(((Map<String,Object>)configured).get("bean"))).getProperty();
 		assertNotNull("bean's property is set", doc);
 		assertEquals("bean's property is set to the correct document", new Integer(1), doc.find("__ID").get());
 	}
 
 	@Test
 	public void testImplicitDocumentMap() throws Exception
 	{
 		Object configured = configure(new Hash()
 			.addEntry("__type", "net.ech.config.DocumentBean")
 			.addEntry("property", new Hash()
 				.addEntry("_ID", 1)));
 		assertTrue("bean is of the correct type", configured instanceof DocumentBean);
 		Document doc = ((DocumentBean)configured).getProperty();
 		assertNotNull("bean's property is set", doc);
 		assertEquals("bean's property is set to the correct document", new Integer(1), doc.find("_ID").get());
 	}
 
 	@Test
 	public void testImplicitDocumentList() throws Exception
 	{
 		Object configured = configure(new Hash()
 			.addEntry("__ID", 0)
 			.addEntry("__type", "net.ech.config.DocumentBean")
 			.addEntry("property", Arrays.asList(new String[] { "abc", "123" })));
 		assertTrue("bean is of the correct type", configured instanceof DocumentBean);
 		Document doc = ((DocumentBean)configured).getProperty();
 		assertNotNull("bean's property is set", doc);
 		assertEquals("bean's property is set to the correct document", 2, doc.get(List.class).size());
 	}
 
 	@Test
 	public void testImplicitDocumentScalar() throws Exception
 	{
 		Object configured = configure(new Hash()
 			.addEntry("__type", "net.ech.config.DocumentBean")
 			.addEntry("property", 123));
 		assertTrue("bean is of the correct type", configured instanceof DocumentBean);
 		Document doc = ((DocumentBean)configured).getProperty();
 		assertNotNull("bean's property is set", doc);
 		assertEquals("bean's property is set to the correct document", new Integer(123), doc.get(Integer.class));
 	}
 
 	@Test
 	public void testListToSetCoercion() throws Exception
 	{
 		Document doc = new Document(Arrays.asList(new String[] { "abc", "def" }));
 		DocumentBasedConfigurator w = new DocumentBasedConfigurator(doc);
 		Object configured = w.configure(Set.class);
 		assertTrue("bean is of the correct type", configured instanceof Set);
 		assertEquals(2, ((Set<Object>) configured).size());
 	}
 
 	@Test
 	public void testListWithNoCoercion() throws Exception
 	{
 		Document doc = new Document(Arrays.asList(new String[] { "abc", "def" }));
 		DocumentBasedConfigurator w = new DocumentBasedConfigurator(doc);
 		Object configured = w.configure();
 		assertTrue("bean is of the correct type", configured instanceof List);
 		assertEquals(2, ((List<Object>) configured).size());
 	}
 
 	private Object configure(Hash hash) throws Exception
 	{
 		Document doc = new Document(hash);
 		DocumentBasedConfigurator w = new DocumentBasedConfigurator(doc);
 		return w.configure(Object.class);
 	}
 
 	private <T> T configure(String key, Class<T> clazz, Hash hash) throws Exception
 	{
 		Document doc = new Document(hash);
 		DocumentBasedConfigurator w = new DocumentBasedConfigurator(doc.find(key), new ChildDocumentResolver(doc));
 		return w.configure(clazz);
 	}
 
 	private Object configure(String key, Hash hash) throws Exception
 	{
 		Document doc = new Document(hash);
 		DocumentBasedConfigurator w = new DocumentBasedConfigurator(doc.find(key), new ChildDocumentResolver(doc));
 		return w.configure(Object.class);
 	}
 }
