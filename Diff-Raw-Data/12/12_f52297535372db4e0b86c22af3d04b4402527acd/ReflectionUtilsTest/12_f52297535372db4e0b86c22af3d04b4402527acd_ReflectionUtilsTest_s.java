 /*
  * Copyright 2012 Robert Philipp
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 package org.freezedry.persistence.utils;
 
 import junit.framework.Assert;
 import org.freezedry.persistence.builders.DoubleNodeBuilder;
 import org.freezedry.persistence.tests.BadPerson;
 import org.freezedry.persistence.tests.Fconcrete;
 import org.freezedry.persistence.tests.circle.*;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.lang.reflect.Field;
 import java.util.Collection;
 import java.util.List;
 import java.util.concurrent.RunnableScheduledFuture;
 
 /**
  * Class hierarchy for the tests
  *            A
  *           /|\
  *          B | \
  *          |/ | \
  *          C /  Aprime
  *          |/
  *          D
  *          |
  *          Econcrete
  *          |     \
  *          |      \
  *          |   A   \
  *          |  / \  |
  *          | / Fconcrete
  *          |/
  *          Gconcrete
  */
 public class ReflectionUtilsTest {
 
 	@Before
 	public void setUp() throws Exception
 	{
 //		DOMConfigurator.configure( "log4j.xml" );
 //		Logger.getRootLogger().setLevel( Level.ERROR );
 	}
 
 	@Test
 	public void testIsClassOrSuperclass() throws Exception
 	{
 		Assert.assertTrue( ReflectionUtils.isClassOrSuperclass( A.class, A.class ) );
 		Assert.assertTrue( ReflectionUtils.isClassOrSuperclass( A.class, B.class ) );
 		Assert.assertTrue( ReflectionUtils.isClassOrSuperclass( A.class, Gconcrete.class ) );
 		Assert.assertTrue( ReflectionUtils.isClassOrSuperclass( Gconcrete.class, Gconcrete.class ) );
 		Assert.assertFalse( ReflectionUtils.isClassOrSuperclass( Gconcrete.class, Fconcrete.class ) );
 		Assert.assertFalse( ReflectionUtils.isClassOrSuperclass( Aprime.class, Fconcrete.class ) );
 	}
 
 	@Test
 	public void testIsSuperclass() throws Exception
 	{
 		Assert.assertFalse( ReflectionUtils.isSuperclass( A.class, A.class ) );
 		Assert.assertTrue( ReflectionUtils.isSuperclass( A.class, B.class ) );
 		Assert.assertTrue( ReflectionUtils.isSuperclass( A.class, Gconcrete.class ) );
 		Assert.assertFalse( ReflectionUtils.isSuperclass( Gconcrete.class, Gconcrete.class ) );
 		Assert.assertFalse( ReflectionUtils.isSuperclass( Gconcrete.class, Fconcrete.class ) );
 		Assert.assertFalse( ReflectionUtils.isSuperclass( Aprime.class, Fconcrete.class ) );
 	}
 
 	@Test
 	public void testCalculateClassDistance() throws Exception
 	{
 		Assert.assertEquals( 1, ReflectionUtils.calculateClassDistance( List.class, Collection.class, -1 ) );
 		Assert.assertEquals( 2, ReflectionUtils.calculateClassDistance( List.class, Iterable.class, -1 ) );
 		Assert.assertEquals( 3, ReflectionUtils.calculateClassDistance( RunnableScheduledFuture.class, Comparable.class, -1 ) );
 		Assert.assertEquals( 3,ReflectionUtils.calculateClassDistance( D.class, A.class, -1 ) );
 		Assert.assertEquals( 2, ReflectionUtils.calculateClassDistance( D.class, B.class, -1 ) );
 		Assert.assertEquals( -1, ReflectionUtils.calculateClassDistance( D.class, Aprime.class, -1 ) );
 		Assert.assertEquals( 4, ReflectionUtils.calculateClassDistance( Econcrete.class, A.class, -1 ) );
 		Assert.assertEquals( 3, ReflectionUtils.calculateClassDistance( Econcrete.class, B.class, -1 ) );
 		Assert.assertEquals( 1, ReflectionUtils.calculateClassDistance( Econcrete.class, D.class, -1 ) );
 		Assert.assertEquals( -1, ReflectionUtils.calculateClassDistance( Econcrete.class, Aprime.class, -1 ) );
 		Assert.assertEquals( 5, ReflectionUtils.calculateClassDistance( Fconcrete.class, A.class, -1 ) );
 		Assert.assertEquals( 0, ReflectionUtils.calculateClassDistance( Econcrete.class, Econcrete.class, -1 ) );
 		Assert.assertEquals( 1, ReflectionUtils.calculateClassDistance( Fconcrete.class, Econcrete.class, -1 ) );
 		Assert.assertEquals( 1, ReflectionUtils.calculateClassDistance( Gconcrete.class, Econcrete.class, -1 ) );
 	}
 
 	@Test
 	public void testGetPersistenceName() throws Exception
 	{

 	}
 
 	@Test
 	public void testGetFieldForPersistenceName() throws Exception
 	{
 
 	}
 
 	@Test
 	public void testGetFieldNameForPersistenceName() throws Exception
 	{
 
 	}
 
 	@Test
 	public void testGetMostSpecificClass() throws Exception
 	{
 
 	}
 
 	@Test
 	public void testGetNodeBuilderClass() throws Exception
 	{
 
 	}
 
 	@Test
 	public void testHasNodeBuilderAnnotation() throws Exception
 	{
 
 	}
 
 	@Test
 	public void testGetItemOrAncestorCopyable() throws Exception
 	{
 
 	}
 
 	@Test
 	public void testGetItemOrAncestor() throws Exception
 	{
 
 	}
 
 	@Test
 	public void testGetAllDeclaredFields() throws Exception
 	{
 
 	}
 
 	@Test
 	public void testGetDeclaredField() throws Exception
 	{
 
 	}
 
 	@Test
 	public void testCast() throws Exception
 	{
 
 	}
 }
