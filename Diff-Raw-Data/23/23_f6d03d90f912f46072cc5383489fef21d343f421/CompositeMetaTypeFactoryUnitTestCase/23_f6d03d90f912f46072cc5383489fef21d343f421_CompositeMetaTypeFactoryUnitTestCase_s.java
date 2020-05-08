 /*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
 package org.jboss.test.metatype.types.factory.test;
 
 import java.util.Collections;
 import java.util.HashMap;
import java.util.Map;
 import java.util.Set;
 
 import org.jboss.metatype.api.types.CompositeMetaType;
 import org.jboss.metatype.api.types.MetaType;
 import org.jboss.metatype.api.types.SimpleMetaType;
 import org.jboss.metatype.api.values.CompositeValueSupport;
 import org.jboss.metatype.api.values.MetaValue;
 import org.jboss.metatype.api.values.SimpleValueSupport;
 import org.jboss.metatype.plugins.types.MutableCompositeMetaType;
 import org.jboss.test.metatype.types.factory.support.TestIgnoredCompositeItem;
 import org.jboss.test.metatype.types.factory.support.TestRecursiveComposite;
 import org.jboss.test.metatype.types.factory.support.TestRenamedCompositeItem;
 import org.jboss.test.metatype.types.factory.support.TestSimpleComposite;
 
import junit.framework.Test;

 /**
  * CompositeMetaTypeFactoryUnitTestCase.
  * 
  * @author <a href="adrian@jboss.com">Adrian Brock</a>
  * @version $Revision: 1.1 $
  */
 public class CompositeMetaTypeFactoryUnitTestCase extends AbstractMetaTypeFactoryTest
 {
    /**
     * Create a testsuite for this test
     * 
     * @return the testsuite
     */
    public static Test suite()
    {
       return suite(CompositeMetaTypeFactoryUnitTestCase.class);
    }
 
    /**
     * Create a new CompositeMetaTypeFactoryUnitTestCase.
     * 
     * @param name the test name
     */
    public CompositeMetaTypeFactoryUnitTestCase(String name)
    {
       super(name);
    }
 
    /**
     * Test the correct metatype is generated for a simple composite
     * 
     * @throws Exception for any problem
     */
    public void testSimpleComposite() throws Exception
    {
       MetaType<?> result = resolve(TestSimpleComposite.class);
       CompositeMetaType actual = assertInstanceOf(result, CompositeMetaType.class);
       
       MutableCompositeMetaType expected = new MutableCompositeMetaType(TestSimpleComposite.class.getName(), TestSimpleComposite.class.getName());
       expected.addItem("something", "something", SimpleMetaType.STRING);
       expected.freeze();
       
       testComposite(expected, actual);
    }
 
    /**
     * Test the correct metatype is generated for a recursive composite
     * 
     * @throws Exception for any problem
     */
    public void testRecursiveComposite() throws Exception
    {
       MetaType<?> result = resolve(TestRecursiveComposite.class);
       CompositeMetaType actual = assertInstanceOf(result, CompositeMetaType.class);
       
       MutableCompositeMetaType expected = new MutableCompositeMetaType(TestRecursiveComposite.class.getName(), TestRecursiveComposite.class.getName());
       expected.addItem("id", "id", SimpleMetaType.STRING);
       expected.addItem("other", "other", expected);
       Set<String> keys = Collections.singleton("id");
       expected.setKeys(keys);
       expected.freeze();
       
       testComposite(expected, actual);
    }
    
    public void testIgnoredItem() throws Exception
    {
       MetaType<?> result = resolve(TestIgnoredCompositeItem.class);
       CompositeMetaType actual = assertInstanceOf(result, CompositeMetaType.class);
 
       MutableCompositeMetaType expected = new MutableCompositeMetaType(TestIgnoredCompositeItem.class.getName(), TestIgnoredCompositeItem.class.getName());
       expected.addItem("id", "id", SimpleMetaType.STRING);
       Set<String> keys = Collections.singleton("id");
       expected.setKeys(keys);
       expected.freeze();
       
       testComposite(expected, actual);
    }
    
    public void testRenamedItem() throws Exception
    {
       MetaType<?> result = resolve(TestRenamedCompositeItem.class);
       CompositeMetaType actual = assertInstanceOf(result, CompositeMetaType.class);
       
       MutableCompositeMetaType expected = new MutableCompositeMetaType(TestRenamedCompositeItem.class.getName(), TestRenamedCompositeItem.class.getName());
       expected.addItem("id", "id", SimpleMetaType.STRING);
       expected.addItem("renamed", "renamed", SimpleMetaType.STRING);
       Set<String> keys = Collections.singleton("id");
       expected.setKeys(keys);
       expected.freeze();
       
       testComposite(expected, actual);
    }
 
    public void testMapComposite() throws Exception
    {
      Map<String, String> x = new HashMap<String, String>();
      CompositeMetaType result = (CompositeMetaType) resolve(x.getClass());
//      CompositeMetaType actual = assertInstanceOf(result, CompositeMetaType.class);
       MutableCompositeMetaType expected = new MutableCompositeMetaType(HashMap.class.getName(), "HashMap<String,String>");
       expected.addItem("key1", "key1", SimpleMetaType.STRING);
       expected.addItem("key2", "key2", SimpleMetaType.STRING);
       expected.addItem("key3", "key3", SimpleMetaType.STRING);
       expected.freeze();
       
       testComposite(expected, result);
 
       String[] itemNames = {"key1", "key2", "key3"};
       MetaValue[] itemValues = {SimpleValueSupport.wrap("value1"), SimpleValueSupport.wrap("value3"), SimpleValueSupport.wrap("value3")};
       CompositeValueSupport mapValue = new CompositeValueSupport(expected, itemNames, itemValues);
      MetaValue value1 = mapValue.get("key1");
    }
 
 }
