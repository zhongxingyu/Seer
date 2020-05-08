 /**
  * Copyright (C) 2012 Ness Computing, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.nesscomputing.migratory.jdbi;
 
 import java.util.Map;
 
 import org.skife.jdbi.v2.Handle;
 import org.skife.jdbi.v2.IDBI;
 import org.skife.jdbi.v2.TransactionCallback;
 import org.skife.jdbi.v2.TransactionStatus;
 import org.skife.jdbi.v2.exceptions.CallbackFailedException;
 import org.skife.jdbi.v2.tweak.HandleCallback;
 
 import com.google.common.collect.Maps;
 
 public class MigratoryDBI implements IDBI
 {
     private final IDBI delegate;
 
     private final MigratoryStatementLocator statementLocator = new MigratoryStatementLocator();
     private final MigratoryStatementRewriter statementRewriter = new MigratoryStatementRewriter();
 
     private final Map<String, Object> defines = Maps.newHashMap();
 
     public MigratoryDBI(final IDBI delegate)
     {
         this.delegate = delegate;
     }
 
     @Override
     public void define(String key, Object value)
     {
         delegate.define(key, value);
     }
 
     @Override
     public Handle open()
     {
         return augmentHandle(delegate.open());
     }
 
     public void addTemplate(final String identifier, final String rawSql)
     {
         statementLocator.addTemplate(identifier, rawSql);
     }
 
     public void addDefine(final String key, final Object value)
     {
         defines.put(key, value);
     }
 
     @Override
     public <ReturnType> ReturnType inTransaction(final TransactionCallback<ReturnType> callback) throws CallbackFailedException
     {
         return delegate.inTransaction(new TransactionCallback<ReturnType>() {
                 @Override
                 public ReturnType inTransaction(final Handle handle, final TransactionStatus transactionStatus) throws Exception
                 {
                     return callback.inTransaction(augmentHandle(handle), transactionStatus);
                 }
             });
     }
 
     @Override
     public <ReturnType> ReturnType withHandle(final HandleCallback<ReturnType> callback) throws CallbackFailedException
     {
         return delegate.withHandle(new HandleCallback<ReturnType>() {
                 @Override
                 public ReturnType withHandle(final Handle handle) throws Exception
                 {
                     return callback.withHandle(augmentHandle(handle));
                 }
             });
     }
 
     protected Handle augmentHandle(final Handle handle)
     {
         handle.setStatementLocator(statementLocator);
         handle.setStatementRewriter(statementRewriter);
 
         for (Map.Entry<String, Object> entry : defines.entrySet()) {
             handle.define(entry.getKey(), entry.getValue());
         }
 
         return handle;
     }
 
     @Override
     public <SqlObjectType> SqlObjectType open(Class<SqlObjectType> sqlObjectType) {
         throw new UnsupportedOperationException("Migratory does not use the SqlObject functionality currently, so this is not implemented");
     }
 
     @Override
     public <SqlObjectType> SqlObjectType onDemand(Class<SqlObjectType> sqlObjectType) {
         throw new UnsupportedOperationException("Migratory does not use the SqlObject functionality currently, so this is not implemented");
     }
 
     @Override
     public void close(Object sqlObject) {
         delegate.close(sqlObject);
     }
 }
