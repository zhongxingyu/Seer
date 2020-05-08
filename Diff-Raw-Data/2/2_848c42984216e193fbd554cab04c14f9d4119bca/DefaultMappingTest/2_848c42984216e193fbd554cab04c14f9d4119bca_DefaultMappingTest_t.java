 /**
  * Copyright 2010 CosmoCode GmbH
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
 
 package de.cosmocode.rendering;
 
 import java.io.Serializable;
 import java.util.AbstractCollection;
 import java.util.AbstractList;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.EnumSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.RandomAccess;
 
 import org.junit.Assert;
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import de.cosmocode.commons.reflect.Reflection;
 import de.cosmocode.junit.UnitProvider;
 
 /**
  * Tests {@link Mappings#defaultMapping()}.
  *
  * @since 1.2
  * @author Willi Schoenborn
  */
 public final class DefaultMappingTest implements UnitProvider<Mapping> {
 
     private static final Logger LOG = LoggerFactory.getLogger(DefaultMappingTest.class);
     
     @Override
     public Mapping unit() {
         return Mappings.defaultMapping();
     }
     
     /**
      * Tests all steps for {@link ArrayList}.
      */
     @Test
     public void arraylist() {
         final Iterable<Class<?>> all = Reflection.getAllSuperTypes(ArrayList.class);
         final Iterator<Class<?>> iterator = all.iterator();
         
         LOG.debug("{}", all);
         
         Assert.assertTrue(iterator.hasNext());
         Assert.assertSame(ArrayList.class, iterator.next());
         Assert.assertTrue(iterator.hasNext());
         Assert.assertSame(AbstractList.class, iterator.next());
         Assert.assertTrue(iterator.hasNext());
         Assert.assertSame(List.class, iterator.next());
         Assert.assertTrue(iterator.hasNext());
         Assert.assertSame(RandomAccess.class, iterator.next());
         Assert.assertTrue(iterator.hasNext());
         Assert.assertSame(Cloneable.class, iterator.next());
         Assert.assertTrue(iterator.hasNext());
         Assert.assertSame(Serializable.class, iterator.next());
         Assert.assertTrue(iterator.hasNext());
         Assert.assertSame(AbstractCollection.class, iterator.next());
         Assert.assertTrue(iterator.hasNext());
         Assert.assertSame(List.class, iterator.next());
         Assert.assertTrue(iterator.hasNext());
         Assert.assertSame(Collection.class, iterator.next());
         Assert.assertTrue(iterator.hasNext());
         Assert.assertSame(Object.class, iterator.next());
         Assert.assertTrue(iterator.hasNext());
         Assert.assertSame(Collection.class, iterator.next());
         Assert.assertTrue(iterator.hasNext());
         Assert.assertSame(Collection.class, iterator.next());
         Assert.assertTrue(iterator.hasNext());
         Assert.assertSame(Iterable.class, iterator.next());
         Assert.assertTrue(iterator.hasNext());
         Assert.assertSame(Iterable.class, iterator.next());
         Assert.assertTrue(iterator.hasNext());
         Assert.assertSame(Iterable.class, iterator.next());
         Assert.assertFalse(iterator.hasNext());
     }
     
     /**
      * Tests with a list class.
      */
     @Test
     public void list() {
         Assert.assertSame(IterableValueRenderer.INSTANCE, unit().find(Arrays.asList().getClass()));
     }
     
     /**
      * Tests with a set class.
      */
     @Test
     public void set() {
         Assert.assertSame(IterableValueRenderer.INSTANCE, unit().find(EnumSet.class));
     }
     
     /**
      * Tests with a byte array class.
      */
     @Test
     public void byteArray() {
         Assert.assertSame(ByteArrayValueRenderer.INSTANCE, unit().find(byte[].class));
     }
     
     /**
      * Tests with a boolean array class.
      *
      * @since
      */
     @Test
     public void booleanArray() {
        Assert.assertSame(ArrayValueRenderer.INSTANCE, unit().find(boolean[].class));
     }
 
 }
