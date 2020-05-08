 package com.lonepulse.packrat.sql.builder;
 
 /*
  * #%L
  * Packrat
  * %%
  * Copyright (C) 2013 Lonepulse
  * %%
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * #L%
  */
 
 
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import android.text.TextUtils;
 
 /**
  * <p>This concrete implementation of {@link AbstractSQLBuilder} provides an implementation 
  * of {@link CreateTablePolicy}.</p>
  * 
 * <p>The operations on this template are not, please employ your own mechanisms 
  * for thread safety.</p>
  * 
  * @version 1.1.0
  * <br><br>
  * @author <a href="mailto:lahiru@lonepulse.com">Lahiru Sahan Jayasinghe</a>
  */
 public class DropTableTemplate extends AbstractSQLBuilder implements DropTablePolicy {
 
 
 	/**
 	 * <p>This flag indicates whether the table to be dropped has been specified.
 	 */
 	private AtomicBoolean tableInitialized;
 	
 	
 	/**
 	 * <p>Creates a new {@link DropTableTemplate} by initializing the switches.
 	 *
 	 * @since 1.1.0
 	 */
 	public DropTableTemplate() {
 	
 		tableInitialized = new AtomicBoolean(false);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public DropTablePolicy dropTable(String tableName) throws MalformedSQLException {
 		
 		throwIfImmutable();
 		
 		if(tableInitialized.get())
 			throw new MalformedSQLException("Cannot invoke dropTable() twice on the same template. ");
 		
 		if(TextUtils.isEmpty(tableName))
 			throw new SQLException("A table name is required to create a table definition.");
 		
 		sql().append("DROP TABLE ").append(tableName);
 		
 		setCorrupted(false);
 		return this;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public String build() throws SQLException {
 		
 		if(isCorrupted())
 			throw new MalformedSQLException("Invoke dropTable() before building. ");
 		
		throwIfImmutable();
 		setImmutable();
		
 		return sql().append(";").toString();
 	}
 	
 	/**
 	 * <p>Throws an exception if this template cannot be mutated any further. 
 	 *
 	 * @throws SQLException
 	 * 			if this template cannot be mutated any further
 	 * 
 	 * @since 1.1.0
 	 */
 	private void throwIfImmutable() throws SQLException {
 		
 		if(isImmutable())
 			throw new SQLException("This template is now immutable. Use getSQLStatement() to edit the SQL manually.");
 	}
 }
