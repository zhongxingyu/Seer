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
 
 import org.easymock.EasyMock;
 import org.junit.Assert;
 import org.junit.Test;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 
 import de.cosmocode.junit.UnitProvider;
 
 /**
  * Tests {@link Renderer#value(Object, ValueRenderer)}.
  *
  * @author Willi Schoenborn
  */
 public abstract class AbstractRendererObjectValueRendererTest implements UnitProvider<Renderer> {
 
     /**
      * Tests {@link Renderer#value(Object, ValueRenderer)} with a null value.
      */
     @Test
     public void valueObjectRendererObjectNull() {
         final Renderer unit = unit();
         final Object value = null;
         @SuppressWarnings("unchecked")
         final ValueRenderer<Object> renderer = EasyMock.createMock("renderer", ValueRenderer.class);
         renderer.render(value, unit);
         EasyMock.expectLastCall();
         EasyMock.replay(renderer);
         unit.list().value(value, renderer);
         EasyMock.verify(renderer);
     }
 
     /**
      * Tests {@link Renderer#value(Object, ValueRenderer)} with a null renderer.
      */
     @Test(expected = NullPointerException.class)
     public void valueObjectRendererRendererNull() {
         final ValueRenderer<Object> renderer = null;
         unit().list().value(new Object(), renderer);
     }
     
     /**
      * Tests {@link Renderer#value(Object, ValueRenderer)} with two nulls.
      */
     @Test(expected = NullPointerException.class)
     public void valueObjectRendererNull() {
         final Object value = null;
         final ValueRenderer<Object> renderer = null;
         unit().list().value(value, renderer);
     }
     
     /**
      * Tests {@link Renderer#value(Object, ValueRenderer)} to return this.
      */
     @Test
     public void valueObjectRendererThis() {
         final Renderer unit = unit();
         Assert.assertSame(unit, unit.list().value(new Object(), Rendering.defaultValueRenderer()));
     }
     
     /**
      * Tests {@link Renderer#value(Object, ValueRenderer)} with a simple (non complex) value as first call.
      */
     @Test(expected = RenderingException.class)
     public void valueObjectRendererSimpleInitial() {
         unit().value(new Object(), Rendering.defaultValueRenderer());
     }
     
     /**
      * Tests {@link Renderer#value(Object, ValueRenderer)} with a complex value as first call.
      */
     @Test
     public void valueObjectRendererComplexInitial() {
        final Object value = Maps.newHashMap();
        unit().value(value, Rendering.defaultValueRenderer());
     }
 
     /**
      * Tests {@link Renderer#value(Object, ValueRenderer)} after key.
      */
     @Test
     public void valueObjectRendererKey() {
         unit().map().key("k").value(Boolean.TRUE, Rendering.defaultValueRenderer());
     }
     
     /**
      * Tests {@link Renderer#value(Object, ValueRenderer)} after list.
      */
     @Test
     public void valueObjectRendererList() {
         final Object value = Lists.newArrayList();
         unit().list().value(value, Rendering.defaultValueRenderer());
     }
     
     /**
      * Tests {@link Renderer#value(Object, ValueRenderer)} after map.
      */
     @Test(expected = RenderingException.class)
     public void valueObjectRendererMap() {
         final Object value = Sets.newHashSet();
         unit().map().value(value, Rendering.defaultValueRenderer());
     }
     
     /**
      * Tests {@link Renderer#value(Object, ValueRenderer)} after done.
      */
     @Test(expected = RenderingException.class)
     public void valueObjectRendererDone() {
         unit().list().endList().value(new Object(), Rendering.defaultValueRenderer());
     }
     
 }
