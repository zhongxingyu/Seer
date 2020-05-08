 package net.sourceforge.jfacets.impl;
 
 import net.sourceforge.jfacets.IFacetContext;
 import net.sourceforge.jfacets.IProfile;
 import net.sourceforge.jfacets.simpleprofiles.SimpleProfile;
 import junit.framework.TestCase;
 
 public class DefaultFacetContextFactoryTest extends TestCase {
 
 	public void testCreate() {
 		DefaultFacetContextFactory dfcf = new DefaultFacetContextFactory();
 		String name = "test";
 		IProfile profile = new SimpleProfile("demo_user");
 		String target = new String("this is a test");
        IFacetContext c = dfcf.create(name, profile, target, null);
 		assertNotNull(c);
 		assertEquals(c.getFacetName(), name);
 		assertEquals(c.getProfile(), profile);
 		assertEquals(c.getTargetObject(), target);
 	}
 
 }
