 /*
  * Copyright (c) 2009 Hidenori Sugiyama
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
 
 /**
  * 
  */
 package org.madogiwa.plaintable.impl;
 
 import org.madogiwa.plaintable.DatabaseManager;
 import org.madogiwa.plaintable.SchemaManager;
 import org.madogiwa.plaintable.Session;
 import org.madogiwa.plaintable.dialect.Dialect;
 
 import javax.sql.DataSource;
 
 /**
  * @author Hidenori Sugiyama
  * 
  */
 public class DatabaseManagerImpl implements DatabaseManager {
 
 	private DataSource dataSource;
 
 	private Dialect dialect;
 
 	private DatabaseSchemaImpl databaseSchema;
 
 	private SchemaManagerImpl schemaManager;
 
 	private boolean delayedOpen = false;
 
 	private boolean readOnly = false;
 
     private boolean autoCommit = false;
 
 	private Session.TransactionMode transactionMode = Session.TransactionMode.READ_COMMITTED;
 
 	private String prefix;
 
 	/**
 	 * @param dataSource
 	 * @param dialect
 	 */
 	public DatabaseManagerImpl(DataSource dataSource, Dialect dialect, String prefix) {
 		this.dataSource = dataSource;
 		this.dialect = dialect;
 		this.prefix = prefix;
 
 		databaseSchema = new DatabaseSchemaImpl(dataSource, dialect, prefix);
 		schemaManager = new SchemaManagerImpl(databaseSchema, prefix);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.madogiwa.plaintable.DatabaseManager#getSchemaManager()
 	 */
 	public SchemaManager getSchemaManager() {
 		return schemaManager;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.madogiwa.plaintable.DatabaseManager#newSession()
 	 */
 	public Session newSession() {
 		Session session = new SessionImpl(this, dataSource, dialect);
 		session.setDelayedOpen(delayedOpen);
 		session.setReadOnly(readOnly);
        session.setAutoCommit(autoCommit);
 		session.setTransactionMode(transactionMode);
 		return session;
 	}
 
 	public boolean getDefaultReadOnly() {
 		return readOnly;
 	}
 
 	public void setDefaultReadOnly(boolean readOnly) {
 		this.readOnly = readOnly;
 	}
 
 	public boolean getDefaultDelayedOpen() {
 		return delayedOpen;
 	}
 
 	public void setDefaultDelayedOpen(boolean delayedOpen) {
 		this.delayedOpen = delayedOpen;
 	}
 
 	public Session.TransactionMode getDefaultTransactionMode() {
 		return transactionMode;
 	}
 
 	public void setDefaultTransactionMode(Session.TransactionMode mode) {
 		this.transactionMode = mode;
 	}
 
     public boolean getDefaultAutoCommit() {
         return autoCommit;
     }
 
     public void setDefaultAutoCommit(boolean autoCommit) {
         this.autoCommit = autoCommit;
     }
 
     public StatementBuilder createStatementBuilder() {
 		return new StatementBuilder(dialect);
 	}
 
 }
