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
 
 import javax.sql.DataSource;
 
 import org.seasar.extension.jdbc.ResultSetFactory;
 
 /**
  * @author jundu
  *
  */
 public class PagerResultSetFactoryRownumWrapper extends
         AbstractPagerResultSetFactory {
 
     /**
      * コンストラクタ(test only)
      * 
      * @param resultSetFactory
      *            オリジナルのResultSetFactory
      * @param productName RDBMSを表す文字列
      */
     PagerResultSetFactoryRownumWrapper(ResultSetFactory resultSetFactory,
             String productName) {
         super(resultSetFactory, productName);
     }
 
     /**
      * コンストラクタ
      * 
      * @param resultSetFactory
      *            オリジナルのResultSetFactory
      * @param dataSource 接続対象のデータソース
      */
     public PagerResultSetFactoryRownumWrapper(
             ResultSetFactory resultSetFactory, DataSource dataSource) {
         super(resultSetFactory, dataSource);
     }
 
     String makeLimitOffsetSql(String baseSQL, int limit, int offset) {
         if (offset < 0) {
             throw new IllegalArgumentException("offset is must ");
         }
         StringBuffer sqlBuf = new StringBuffer(baseSQL);
         sqlBuf
                 .insert(0,
                         "SELECT * FROM (SELECT ROWNUM AS S2DAO_ROWNUMBER, S2DAO_ORIGINAL_DATA.* FROM (");
         sqlBuf.append(") S2DAO_ORIGINAL_DATA) WHERE S2DAO_ROWNUMBER BETWEEN ");
         sqlBuf.append(offset + 1);
         sqlBuf.append(" AND ");
         sqlBuf.append(offset + limit);
        sqlBuf.append(" AND ROWNUM <= ");
        sqlBuf.append(limit);
        sqlBuf.append(" ORDER BY S2DAO_ROWNUMBER");
         return sqlBuf.toString();
     }
 
     String makeCountSql(String baseSQL) {
         StringBuffer sqlBuf = new StringBuffer("SELECT count(*) FROM (");
         if (isChopOrderBy()) {
             sqlBuf.append(chopOrderBy(baseSQL));
         } else {
             sqlBuf.append(baseSQL);
         }
         sqlBuf.append(")");
         return sqlBuf.toString();
     }
 
 }
