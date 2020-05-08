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
 
 import static com.google.common.collect.Lists.newArrayList;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Iterator;
 import java.util.List;
 
 import br.com.objectos.comuns.relational.search.Page;
 import br.com.objectos.comuns.relational.search.ResultSetLoader;
 
 import com.google.common.base.Joiner;
 import com.google.common.base.Preconditions;
 
 /**
  * @author marcio.endo@objectos.com.br (Marcio Endo)
  */
 abstract class NativeSqlImpl implements NativeSql {
 
   private final JdbcNativeSqlExec exec;
 
   private final List<String> parts;
   private final List<ParamValue<?>> params;
 
   private ResultSetLoader<?> loader;
   private int index;
 
   private GeneratedKeyCallback keyCallback;
 
   public NativeSqlImpl(JdbcNativeSqlExec exec) {
     this.exec = exec;
     this.parts = newArrayList();
     this.params = newArrayList();
   }
 
   NativeSqlImpl(JdbcNativeSqlExec exec, NativeSqlBuilder<?> sql) {
     this.exec = exec;
     this.parts = sql.getParts();
     this.params = sql.getParams();
   }
 
   @Override
   public NativeSql add(String string) {
     Preconditions.checkNotNull(string, "SQL part cannot be null");
     parts.add(string);
 
     return this;
   }
 
   @Override
   public NativeSql add(String template, Object... args) {
     Preconditions.checkNotNull(template, "template cannot be null");
     String string = String.format(template, args);
     return add(string);
   }
 
   @Override
   public AddIf addIf(final String string) {
     return new AddIf() {
       @Override
       public NativeSql paramNotNull(Object param) {
         if (param != null && !"".equals(param)) {
           add(string);
           param(param);
         }
 
         return NativeSqlImpl.this;
       }
     };
   }
 
   @Override
   public AddIf addIf(String template, Object... args) {
     Preconditions.checkNotNull(template, "template cannot be null");
     String string = String.format(template, args);
     return addIf(string);
   }
 
   @Override
   public NativeSql param(Object value) {
     Preconditions.checkNotNull(value, "param cannot be null");
 
     ParamValue<?> param = ParamValue.valueOf(++index, value);
     params.add(param);
 
     return this;
   }
 
   @Override
   public NativeSql andLoadWith(ResultSetLoader<?> loader) {
     this.loader = loader;
 
     return this;
   }
 
   @Override
   public NativeSql onGeneratedKey(GeneratedKeyCallback keyCallback) {
     this.keyCallback = keyCallback;
 
     return this;
   }
 
   public NativeSqlImpl limit(Page page) {
     return this;
   }
 
   @Override
   public int execute() {
     return exec.execute(this);
   }
 
   @Override
   public void insert() {
     exec.insert(this);
   }
 
   @Override
   public <T> Iterator<T> iterate() {
     return exec.iterate(this);
   }
 
   @Override
   public <T> List<T> list() {
     return exec.list(this);
   }
 
   @Override
   public <T> List<T> listPage(Page page) {
     return exec.listPage(this, page);
   }
 
   @Override
   public <T> T single() {
     return exec.single(this);
   }
 
   public PreparedStatement addBatch(PreparedStatement stmt) throws SQLException {
     PreparedStatementWrapper wrapper = new PreparedStatementWrapper(stmt);
 
     for (ParamValue<?> param : params) {
       param.set(wrapper);
     }
 
     stmt.addBatch();
     return stmt;
   }
 
   public PreparedStatement prepare(Connection connection) throws SQLException {
     String sql = toString();
 
     PreparedStatement stmt = connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE);
     PreparedStatementWrapper wrapper = new PreparedStatementWrapper(stmt);
 
     for (ParamValue<?> param : params) {
       param.set(wrapper);
     }
 
     return stmt;
   }
 
   public PreparedStatement prepare(Connection connection, int mode) throws SQLException {
     String sql = toString();
 
     PreparedStatement stmt = connection.prepareStatement(sql, mode);
     PreparedStatementWrapper wrapper = new PreparedStatementWrapper(stmt);
 
     for (ParamValue<?> param : params) {
       param.set(wrapper);
     }
 
     return stmt;
   }
 
   public GeneratedKeyCallback getKeyCallback() {
     return keyCallback;
   }
 
   public ResultSetLoader<?> getLoader() {
     return loader;
   }
 
   @Override
   public String toString() {
     return Joiner.on("\n").join(parts);
   }
 
 }
