 /*
  * Copyright 2011 Objectos, Fábrica de Software LTDA.
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package br.com.objectos.comuns.relational.jdbc;
 
 import java.util.Iterator;
 import java.util.List;
 
 import br.com.objectos.comuns.relational.search.Page;
 import br.com.objectos.comuns.relational.search.ResultSetLoader;
 
 /**
  * @author marcio.endo@objectos.com.br (Marcio Endo)
  */
 public interface NativeSql {
 
   interface AddIf {
    NativeSql isTrue(boolean param);
     NativeSql paramNotNull(Object param);
   }
 
   NativeSql add(String string);
   NativeSql add(String template, Object... args);
 
   AddIf addIf(String string);
   AddIf addIf(String template, Object... args);
 
   NativeSql andLoadWith(ResultSetLoader<?> loader);
 
   NativeSql onGeneratedKey(GeneratedKeyCallback callback);
 
   NativeSql param(Object value);
 
   int execute();
 
   void insert();
 
   <T> Iterator<T> iterate();
 
   <T> List<T> list();
 
   <T> List<T> listPage(Page page);
 
   <T> T single();
 
 }
