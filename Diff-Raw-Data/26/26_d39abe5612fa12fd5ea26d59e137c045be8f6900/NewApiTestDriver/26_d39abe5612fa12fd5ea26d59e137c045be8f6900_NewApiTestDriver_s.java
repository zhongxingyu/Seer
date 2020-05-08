 /**
  * Copyright (c) 2009-2011 Zauber S.A. <http://www.zaubersoftware.com/>
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
 package com.zaubersoftware.leviathan.api.engine;
 
 import java.util.List;
 
 import org.junit.Test;
 
 import ar.com.zauber.commons.dao.exception.DuplicatedEntityException;
 import ar.com.zauber.leviathan.api.URIFetcherResponse;
 
 
 /**
  * TODO: Description of the class, Comments in english by default
  *
  *
  * @author Guido Marucci Blas
  * @since Jul 22, 2011
  */
 public class NewApiTestDriver {
 
     @Test
     public void testname() throws Exception {
         final Engine engine = Leviathan.newEngine().build();
 
         engine.forUri("http://google.com/").then(new ContextAwareClosure<URIFetcherResponse>() {
             @Override
             public void execute(final URIFetcherResponse response) {
                 // ok! nos llego la pagina.
             }
         });
 
 
         engine.forUri("http://www.dmoz.org/").sanitizeHTML().transformXML("raiz.xsl")
                  .transformXML("otramas.xml")
                  .toJavaObject(Categories.class)
                  .then(new Action<Categories, List<Category>>() {
                     @Override
                     public List<Category> execute(final Categories t) {
                         get("ContextVariable");
                         put("ContextObject", "VALUE");
                         return t.getCategories();
                     }
 
                 }).then(new ContextAwareClosure<List<Category>>() {
                     @Override
                     public void execute(final List<Category> categories) {
                         get("ContextVariable");
                     }
                 });
 
         engine.afterFetch().sanitizeHTML().transformXML("raiz.xsl")
             .transformXML("otramas.xml")
             .toJavaObject(Categories.class)
             .then(new Action<Categories, List<Category>>() {
                 @Override
                 public List<Category> execute(final Categories t) {
                     get("ContextVariable");
                     put("ContextObject", "VALUE");
                     return t.getCategories();
                 }
 
            }).then(new ContextAwareClosure<List<Category>>() {
                @Override
                public void execute(final List<Category> categories) {
                    get("ContextVariable");
                }
            });
 
 
         engine.forUri("http://www.dmoz.org/")
             .sanitizeHTML()
             .transformXML("raiz.xsl")
             .transformXML("otramas.xml")
             .toJavaObject(Categories.class)
             .then(new Action<Categories, Categories>() {
                 @Override
                 public Categories execute(final Categories t) {
                     // DO something with the categories
                     return t;
                 }
             })
            .forEach(Category.class).in("categories")
                .thenFetch("http://www.dmoz.org/#[root.getName()]")
                    .then(new ContextAwareClosure<URIFetcherResponse>() {
                        @Override
                        public void execute(final URIFetcherResponse arg0) {
                        }
                    })
                    .on(DuplicatedEntityException.class).handleWith(new ExceptionHandler() {
                        @Override
                        public void handle(final Throwable trowable) {
                            // Log error
                        }
                    });
 
         engine.forUri("http://www.dmoz.org/").sanitizeHTML().transformXML("raiz.xsl")
             .transformXML("otramas.xml")
             .toJavaObject(Categories.class)
             .then(new ActionAndThenFetch<Categories>() {
                 @Override
                 public FetchRequest execute(final Categories t) {
                     return null;
                 }
            });
 
 
         engine.onAnyExceptionDo(new ExceptionHandler() {
             @Override
             public void handle(final Throwable trowable) {
                 // Log error
             }
         });
 //        engine.forUri(...);
 //
 //
 //        FetchingProcessor processor = engine.getFetchingProcessor();
 //        processor.fetch("http://www.dmoz.org/", model);
 
     }
 }
