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
 package com.google.code.nanorm.test.resultmap;
 
 import java.util.List;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 import com.google.code.nanorm.annotations.Mapping;
 import com.google.code.nanorm.annotations.ResultMap;
 import com.google.code.nanorm.annotations.ResultMapList;
 import com.google.code.nanorm.annotations.ResultMapRef;
 import com.google.code.nanorm.annotations.Select;
 import com.google.code.nanorm.test.beans.Article;
 import com.google.code.nanorm.test.beans.Category;
 import com.google.code.nanorm.test.beans.Publication;
 import com.google.code.nanorm.test.common.MapperTestBase;
 
 /**
  * 
  * @author Ivan Dubrov
  * @version 1.0 27.05.2008
  */
 @SuppressWarnings("all")
 public class NestedResultMapTest extends MapperTestBase {
     @ResultMapList({
         @ResultMap(id = "article", mappings = {
         	@Mapping(property = "id"),
             @Mapping(property = "subject"),
             @Mapping(property = "body")
         }),
         @ResultMap(id = "article2", mappings = {
         	@Mapping(property = "id", column = "article_id"),
             @Mapping(property = "subject"),
             @Mapping(property = "body")
         }),
         @ResultMap(id = "comment", mappings = {
         	@Mapping(property = "id", column = "id"),
         	@Mapping(property = "comment", column = "comment"),
             @Mapping(property = "year", column = "year")
         }),
         @ResultMap(id = "comment2", mappings = {
         	@Mapping(property = "id", column = "comment_id"),
         	@Mapping(property = "comment", column = "comment"),
             @Mapping(property = "year", column = "year")
         }),
         // groupBy is the name of the property
         @ResultMap(id = "article3", groupBy = "id", mappings = {
             @Mapping(property = "id", column = "article_id"),
             @Mapping(property = "subject"),
             @Mapping(property = "body"),
             @Mapping(property = "comments", nestedMap = @ResultMapRef("comment2"))
         }),
         @ResultMap(id = "label", groupBy = "id", mappings = {
         	@Mapping(property = "id", column = "label_id"),
         	@Mapping(property = "label")
         }),
         @ResultMap(id = "comment3", groupBy = "id", mappings = {
             @Mapping(property = "id", column = "comment_id"),
             @Mapping(property = "comment", column = "comment"),
             @Mapping(property = "year", column = "year")
        }),
     })
     public interface Mapper {
         
         // Test 1-1 mapping with nested result map
         @ResultMap(mappings = {
             @Mapping(property = "id"),
             @Mapping(property = "title"),
             @Mapping(property = "year"),
             @Mapping(property = "article", nestedMap = @ResultMapRef("article")) 
         })
         @Select("SELECT id, subject as title, subject, body, year FROM articles WHERE ID = ${1}")
         Publication getPublicationById(int id);
         
         // Test 1-1 mapping with nested result map, the property type is List
         @ResultMap(mappings = {
             @Mapping(property = "id"),
             @Mapping(property = "title"),
             @Mapping(property = "year"),
             @Mapping(property = "articles", nestedMap = @ResultMapRef("article")) 
         })
         @Select("SELECT id, title, year, 'Dummy Subject' as subject, 'Dummy Body' as body " +
         		"FROM categories WHERE ID = ${1}")
         Category getCategoryById2(int id);
         
         // Test 1-N mapping with nested result map, the property type is List
         @ResultMap(groupBy = "id", mappings = {
             @Mapping(property = "id"),
             @Mapping(property = "title"),
             @Mapping(property = "year"),
             @Mapping(property = "articles", nestedMap = @ResultMapRef("article2")) 
         })
         @Select("SELECT c.id, c.title, c.year, " +
         		"a.id as article_id, a.subject, a.body " +
         		"FROM categories c " +
                 "INNER JOIN articles a ON c.id = a.category_id WHERE c.id = ${1}" +
                 "ORDER BY c.id, a.id")
         Category getPublicationById3(int id);
         
         // Test 1-N-M mapping with two nested result map, the property type is List
         @ResultMap(groupBy = "id", mappings = {
             @Mapping(property = "id"),
             @Mapping(property = "title"),
             @Mapping(property = "year"),
             @Mapping(property = "articles", nestedMap = @ResultMapRef("article3")) 
         })
         @Select("SELECT c.id, c.title, c.year, " +
                 "a.id as article_id, a.subject, a.body, " +
                 "cm.id as comment_id, cm.comment " +
                 "FROM categories c " +
                 "INNER JOIN articles a ON c.id = a.category_id " +
                 "INNER JOIN comments cm ON a.id = cm.article_id " +
                 "WHERE c.id = ${1} " +
                 "ORDER BY c.id, a.id, cm.id")
         Category getCategoriesById4(int id);
         
         // Test 1-N mapping, the property type is array
         @ResultMap(groupBy = "id", mappings = {
         	@Mapping(property = "id"),
             @Mapping(property = "subject"),
             @Mapping(property = "body"),
             @Mapping(property = "labels", nestedMap = @ResultMapRef("label"))
         })
         @Select("SELECT a.id, a.subject, a.body, l.id as label_id, l.label " +
         		"FROM articles a " +
         		"INNER JOIN labels l ON l.article_id = a.id " +
         		"ORDER BY a.id, l.id")
         List<Article> listArticles();
         
         // Test two 1-N mapping, one nested property is array and other is list
         @ResultMap(groupBy = "id", mappings = {
         	@Mapping(property = "id"),
             @Mapping(property = "subject"),
             @Mapping(property = "body"),
             @Mapping(property = "labels", nestedMap = @ResultMapRef("label")),
             @Mapping(property = "comments", nestedMap = @ResultMapRef("comment3"))
         })
         @Select("SELECT a.id, a.subject, a.body, l.id as label_id, l.label, " +
         		"c.id as comment_id, c.comment, c.year " +
         		"FROM articles a " +
         		"INNER JOIN labels l ON l.article_id = a.id " +
         		"INNER JOIN comments c ON c.article_id = a.id " +
         		"ORDER BY a.id, l.id, c.id")
         List<Article> listArticles2();
     }
     
     /**
      * Refer to nested map from different class.
      * @author Ivan Dubrov
      */
     public interface Mapper2 {
     	// Test 1-1 mapping with nested result map
         @ResultMap(mappings = {
             @Mapping(property = "id"),
             @Mapping(property = "title"),
             @Mapping(property = "year"),
             @Mapping(property = "article", nestedMap = @ResultMapRef(value = "article", declaringClass = Mapper.class)) 
         })
         @Select("SELECT id, subject as title, subject, body, year FROM articles WHERE ID = ${1}")
         Publication getPublicationById(int id);
     }
 
     @Test
     public void testNestedOneToOne() throws Exception {
         Mapper mapper = factory.createMapper(Mapper.class);
         Publication publication = mapper.getPublicationById(1);
         Assert.assertEquals(1, publication.getId());
         Assert.assertEquals("World Domination", publication.getArticle().getSubject());
         Assert.assertEquals(2007, publication.getYear());
     }
     
     @Test
     public void testNestedOneToOneExternal() throws Exception {
     	factory.createMapper(Mapper.class); // Force it to be configured
         Mapper2 mapper = factory.createMapper(Mapper2.class);
         Publication publication = mapper.getPublicationById(1);
         Assert.assertEquals(1, publication.getId());
         Assert.assertEquals("World Domination", publication.getArticle().getSubject());
         Assert.assertEquals(2007, publication.getYear());
     }
     
     @Test
     public void testNestedOneToOne2() throws Exception {
         Mapper mapper = factory.createMapper(Mapper.class);
         Category car = mapper.getCategoryById2(1);
         Assert.assertEquals(1, car.getId());
         Assert.assertEquals(1, car.getArticles().size());
         Assert.assertEquals("Dummy Subject", car.getArticles().get(0).getSubject());
         Assert.assertEquals("Dummy Body", car.getArticles().get(0).getBody());
         Assert.assertEquals("World", car.getTitle());
         Assert.assertEquals(2006, car.getYear());
     }
     
     @Test
     public void testNestedOneToMany() throws Exception {
         Mapper mapper = factory.createMapper(Mapper.class);
         Category cat = mapper.getPublicationById3(1);
         Assert.assertEquals(1, cat.getId());
         Assert.assertEquals(2006, cat.getYear());
         Assert.assertEquals(2, cat.getArticles().size());
         
         Assert.assertEquals(1, cat.getArticles().get(0).getId());
         Assert.assertEquals("World Domination", cat.getArticles().get(0).getSubject());
         Assert.assertEquals("Everybody thinks of world domination.", cat.getArticles().get(0).getBody());
         
         Assert.assertEquals(2, cat.getArticles().get(1).getId());
         Assert.assertEquals("Saving the Earth", cat.getArticles().get(1).getSubject());
         Assert.assertEquals("To save the earth you need...", cat.getArticles().get(1).getBody());
         
         
     }
     
     @Test
     public void testNestedOneToMany2() throws Exception {
         Mapper mapper = factory.createMapper(Mapper.class);
         Category cat = mapper.getCategoriesById4(1);
         Assert.assertEquals(1, cat.getId());
         Assert.assertEquals(2006, cat.getYear());
         
         // Second is not selected (we use INNER JOIN)
         Assert.assertEquals(1, cat.getArticles().size());
         
         Assert.assertEquals(1, cat.getArticles().get(0).getId());
         Assert.assertEquals("World Domination", cat.getArticles().get(0).getSubject());
         Assert.assertEquals("Everybody thinks of world domination.", cat.getArticles().get(0).getBody());
         Assert.assertEquals(2, cat.getArticles().get(0).getComments().size());
         
         Assert.assertEquals(101, cat.getArticles().get(0).getComments().get(0).getId());
         Assert.assertEquals("Great!", cat.getArticles().get(0).getComments().get(0).getComment());
         
         Assert.assertEquals(102, cat.getArticles().get(0).getComments().get(1).getId());
         Assert.assertEquals("Always wanted to world-dominate!", cat.getArticles().get(0).getComments().get(1).getComment());
         
         // Second owner
         /*
         Assert.assertEquals(2, car.getArticles().get(1).getId());
         Assert.assertEquals("Jimmy", car.getArticles().get(1).getFirstName());
         Assert.assertEquals("Green", car.getArticles().get(1).getLastName());
         Assert.assertEquals(0, car.getArticles().get(1).getCrashes().size());
         */
     }
     
     /**
      * Test 1-N mapping with nested maps where collection type is array.
      * @throws Exception
      */
     @Test
     public void testNestedOneToManyArray() throws Exception {
         Mapper mapper = factory.createMapper(Mapper.class);
         List<Article> articles = mapper.listArticles();
         
         // One article, because we use inner join
         Assert.assertEquals(1, articles.size());
         
         Assert.assertEquals(1, articles.get(0).getId());
         Assert.assertEquals("World Domination", articles.get(0).getSubject());
         Assert.assertEquals("Everybody thinks of world domination.", articles.get(0).getBody());
         Assert.assertEquals(2, articles.get(0).getLabels().length);
         
         Assert.assertEquals(1231, articles.get(0).getLabels()[0].getId());
         Assert.assertEquals("World", articles.get(0).getLabels()[0].getLabel());
         
         Assert.assertEquals(1232, articles.get(0).getLabels()[1].getId());
         Assert.assertEquals("Dominate", articles.get(0).getLabels()[1].getLabel());
     }
     
     /**
      * est two 1-N mapping with nested maps where one collection type is list and other is array.
      * @throws Exception
      */
     @Test
     public void testNestedOneToManyArrayList() throws Exception {
         Mapper mapper = factory.createMapper(Mapper.class);
         List<Article> articles = mapper.listArticles2();
         
         // One article, because we use inner join
         Assert.assertEquals(1, articles.size());
         
         Assert.assertEquals(1, articles.get(0).getId());
         Assert.assertEquals("World Domination", articles.get(0).getSubject());
         Assert.assertEquals("Everybody thinks of world domination.", articles.get(0).getBody());
         Assert.assertEquals(2, articles.get(0).getLabels().length);
         Assert.assertEquals(2, articles.get(0).getComments().size());
         
         Assert.assertEquals(1231, articles.get(0).getLabels()[0].getId());
         Assert.assertEquals("World", articles.get(0).getLabels()[0].getLabel());
         
         Assert.assertEquals(1232, articles.get(0).getLabels()[1].getId());
         Assert.assertEquals("Dominate", articles.get(0).getLabels()[1].getLabel());
         
         Assert.assertEquals(101, articles.get(0).getComments().get(0).getId());
         Assert.assertEquals("Great!", articles.get(0).getComments().get(0).getComment());
         
         Assert.assertEquals(102, articles.get(0).getComments().get(1).getId());
         Assert.assertEquals("Always wanted to world-dominate!", articles.get(0).getComments().get(1).getComment());
     }
 }
