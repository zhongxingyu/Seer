 /**
  * Copyright (C) 2008 Ivan S. Dubrov
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *         http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.google.code.nanorm.test.introspect;
 
 import java.lang.reflect.Type;
 
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.google.code.nanorm.internal.introspect.Getter;
 import com.google.code.nanorm.internal.introspect.IntrospectionFactory;
 import com.google.code.nanorm.internal.introspect.Setter;
 import com.google.code.nanorm.test.beans.Article;
 import com.google.code.nanorm.test.beans.Label;
 import com.google.code.nanorm.test.beans.Publication;
 
 /**
 * 
  * @author Ivan Dubrov
  * @version 1.0 19.06.2008
  */
 public abstract class TestGetterBase {
 
 	/**
 	 * Introspection factory.
 	 */
     protected IntrospectionFactory factory;
     
     /**
      * Method which will create concrete introspection factory.
      * @return introspection factory
      */
     protected abstract IntrospectionFactory provideIntrospectionFactory();
 
     /**
      * Create introspection factory.
      */
     @Before
     public void setUp() {
         factory = provideIntrospectionFactory();
     }
 
     /**
      * Test getter generation.
      */
     @Test
     public void testGetter() {
         Article article = new Article();
         Publication publication = new Publication();
         publication.setArticle(article);
         Getter getter = factory.buildGetter(Publication.class, "article.subject");
 
         article.setSubject("World Domination");
         Assert.assertEquals("World Domination", getter.getValue(publication));
         Assert.assertEquals(String.class, getter.getType());
     }
     
     /**
      * Test setter generation.
      */
     @Test
     public void testSetter() {
         Article article = new Article();
         Publication publication = new Publication();
         publication.setArticle(article);
         Setter setter = factory.buildSetter(Publication.class, "article.subject");
 
         setter.setValue(publication, "Domination");
         Assert.assertEquals("Domination", publication.getArticle().getSubject());
     }
     
     /**
      * Test parameter getter generation.
      */
     @Test
     public void testParameterGetter() {
         Article article = new Article();
         article.setSubject("World");
         Publication publication = new Publication();
         publication.setArticle(article);
         Publication[] arr = new Publication[4];
         arr[1] = publication;
         Type[] types = new Type[] { Publication.class, Publication.class, Publication.class, Publication.class }; 
         Getter getter = factory.buildParameterGetter(types, "2.article.subject");
 
         Assert.assertEquals("World", getter.getValue(arr));
         Assert.assertEquals(String.class, getter.getType());
     }
     
     /**
      * Test parameter getter generation.
      */
     @Test
     public void testParameterGetter2() {
         String[] arr = new String[] { "one", "two", "three", "four" };
         Type[] types = new Type[] { String.class, String.class, String.class, String.class }; 
         Getter getter = factory.buildParameterGetter(types, "2");
         
         Assert.assertEquals("two", getter.getValue(arr));
         Assert.assertEquals(String.class, getter.getType());
     }
     
     /**
      * Test getter with arrays.
      */
     @Test
     public void testGetterArray() {
         Article article = new Article();
         Label label = new Label();
         label.setLabel("world");
         Label[] labels = new Label[10];
         labels[3] = label;
         article.setLabels(labels);
         Publication publication = new Publication();
         publication.setArticle(article);
             
         Getter getter = factory.buildGetter(Publication.class, "article.labels[3].label");
 
         Assert.assertEquals("world", getter.getValue(publication));
         Assert.assertEquals(String.class, getter.getType());
     }
     
     /**
      * Test getter with arrays.
      */
     @Test
     public void testGetterArray2() {
         Article article = new Article();
         int[] updates = new int[10];
         updates[2] = 2006;
         article.setUpdates(updates);
         Publication publication = new Publication();
         publication.setArticle(article);
             
         Getter getter = factory.buildGetter(Publication.class, "article.updates[2]");
 
         Assert.assertEquals(2006, getter.getValue(publication));
     }
     
     /**
      * Test setter with arrays. 
      */
     @Test
     public void testSetterArray() {
         Article article = new Article();
         Label label = new Label();
         Label[] labels = new Label[10];
         labels[3] = label;
         article.setLabels(labels);
         Publication publication = new Publication();
         publication.setArticle(article);
             
         Setter setter = factory.buildSetter(Publication.class, "article.labels[3].label");
 
         setter.setValue(publication, "domination");
         Assert.assertEquals("domination", publication.getArticle().getLabels()[3].getLabel());
     }
     
     /**
      * Test setter with arrays.
      */
     @Test
     public void testSetterArray2() {
         Article article = new Article();
         int[] updates = new int[10];
         article.setUpdates(updates);
         Publication publication = new Publication();
         publication.setArticle(article);
             
         Setter setter = factory.buildSetter(Publication.class, "article.updates[2]");
 
         setter.setValue(publication, 2002);
         Assert.assertEquals(2002, publication.getArticle().getUpdates()[2]);
     }
     
     /**
      * Test getter with arrays.
      */
     @Test
     public void testGetterArray3() {
         Publication publication = new Publication();
         publication.setYear(2008);
         Publication[] publications = new Publication[5];
         publications[3] = publication;
             
         Getter getter = factory.buildGetter(Publication[].class, "[3].year");
 
         Assert.assertEquals(2008, getter.getValue(publications));
     }
 }
 
