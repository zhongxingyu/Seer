 /*
  * Copyright 2004-2007 the Seasar Foundation and the Others.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
  * either express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package org.seasar.dao.pager;
 
 import junit.framework.TestCase;
 
 /**
  * @author jundu
  *
  */
 public class PagerResultSetFactoryRownumWrapperTest extends TestCase {
 
     MockResultSetFactory original;
 
     PagerResultSetFactoryRownumWrapper wrapper;
 
     /* (non-Javadoc)
      * @see junit.framework.TestCase#setUp()
      */
     protected void setUp() throws Exception {
         super.setUp();
         original = new MockResultSetFactory();
         wrapper = new PagerResultSetFactoryRownumWrapper(original, "Oracle");
         PagerContext.start();
         PagerContext.getContext().pushArgs(createNormalArgs());
     }
 
     protected void tearDown() throws Exception {
         PagerContext.getContext().popArgs();
         PagerContext.end();
         super.tearDown();
     }
 
     private Object[] createNormalArgs() {
         return new Object[] {};
     }
 
     /**
      * {@link org.seasar.dao.pager.PagerResultSetFactoryRownumWrapper#makeLimitOffsetSql(java.lang.String, int, int)} のためのテスト・メソッド。
      */
     public void testMakeLimitOffsetSql() {
         assertEquals(
                "SELECT * FROM (SELECT ROWNUM AS S2DAO_ROWNUMBER, S2DAO_ORIGINAL_DATA.* FROM (SELECT * FROM DEPARTMENT) S2DAO_ORIGINAL_DATA) WHERE S2DAO_ROWNUMBER BETWEEN 56 AND 65",
                 wrapper.makeLimitOffsetSql("SELECT * FROM DEPARTMENT", 10, 55));
     }
 
     /**
      * {@link org.seasar.dao.pager.PagerResultSetFactoryRownumWrapper#makeCountSql(java.lang.String)} のためのテスト・メソッド。
      */
     public void testMakeCountSql() {
         assertEquals("count(*)で全件数を取得するSQLを生成",
                 "SELECT count(*) FROM (SELECT * FROM DEPARTMENT)", wrapper
                         .makeCountSql("SELECT * FROM DEPARTMENT"));
         assertEquals("count(*)で全件数を取得するSQLを生成(order by 除去)",
                 "SELECT count(*) FROM (SELECT * FROM DEPARTMENT )", wrapper
                         .makeCountSql("SELECT * FROM DEPARTMENT order by id"));
         assertEquals("count(*)で全件数を取得するSQLを生成(ORDER BY 除去)",
                 "SELECT count(*) FROM (SELECT * FROM DEPARTMENT )", wrapper
                         .makeCountSql("SELECT * FROM DEPARTMENT ORDER BY id"));
         assertEquals(
                 "count(*)で全件数を取得するSQLを生成(whitespace付きorder by 除去)",
                 "SELECT count(*) FROM (SELECT * FROM DEPARTMENT\n)",
                 wrapper
                         .makeCountSql("SELECT * FROM DEPARTMENT\norder by\n    id"));
         assertEquals(
                 "count(*)で全件数を取得するSQLを生成(途中のorder byは除去しない)",
                 "SELECT count(*) FROM (SELECT * FROM DEPARTMENT WHERE name like '%order by%' )",
                 wrapper
                         .makeCountSql("SELECT * FROM DEPARTMENT WHERE name like '%order by%' order by id"));
         assertEquals(
                 "count(*)で全件数を取得するSQLを生成(途中のorder byは除去しない)",
                 "SELECT count(*) FROM (SELECT * FROM DEPARTMENT WHERE name='aaa'/*order by*/)",
                 wrapper
                         .makeCountSql("SELECT * FROM DEPARTMENT WHERE name='aaa'/*order by*/order by id"));
         assertEquals(
                 "count(*)で全件数を取得するSQLを生成(途中のorder byは除去しない)",
                 "SELECT count(*) FROM (SELECT * FROM DEPARTMENT WHERE\n--order by\nname=1\n)",
                 wrapper
                         .makeCountSql("SELECT * FROM DEPARTMENT WHERE\n--order by\nname=1\norder by id"));
         assertEquals("count(*)で全件数を取得するSQLを生成(order by除去 UNICODE)",
                 "SELECT count(*) FROM (SELECT * FROM DEPARTMENT )", wrapper
                         .makeCountSql("SELECT * FROM DEPARTMENT order by ＮＯ"));
         assertEquals(
                 "count(*)で全件数を取得するSQLを生成(order by除去 UNICODE)",
                 "SELECT count(*) FROM (SELECT * FROM DEPARTMENT )",
                 wrapper
                         .makeCountSql("SELECT * FROM DEPARTMENT order by 名前, 組織_ID"));
         assertEquals(
                 "count(*)で全件数を取得するSQLを生成(order by除去 ASC,DESC)",
                 "SELECT count(*) FROM (SELECT * FROM DEPARTMENT )",
                 wrapper
                         .makeCountSql("SELECT * FROM DEPARTMENT order by 名前 ASC\n, 組織_ID DESC"));
         assertEquals(
                 "count(*)で全件数を取得するSQLを生成(order by除去 ASC,DESC+空行)",
                 "SELECT count(*) FROM (SELECT * FROM DEPARTMENT )",
                 wrapper
                         .makeCountSql("SELECT * FROM DEPARTMENT order\n\tby\n\n 名前 \n\tASC \n\n\n, 組織_ID \n\tDESC \n"));
     }
 
 }
