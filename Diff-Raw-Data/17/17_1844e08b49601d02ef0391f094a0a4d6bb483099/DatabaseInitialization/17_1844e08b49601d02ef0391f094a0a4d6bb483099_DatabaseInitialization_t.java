 /*
  * Copyright 2012 the original author or authors.
  *
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
  */
 package org.nebulae2us.stardust.example;
 
import java.sql.Connection;
 import java.util.List;
 
import javax.sql.DataSource;

 import org.nebulae2us.stardust.DaoManager;
 import org.nebulae2us.stardust.dao.JdbcExecutor;
 import org.nebulae2us.stardust.ddl.domain.DDLGenerator;
 import org.springframework.beans.factory.InitializingBean;
 
 /**
  * @author Trung Phan
  *
  */
 public class DatabaseInitialization implements InitializingBean {
 
 	private DaoManager daoManager;
 	private JdbcExecutor jdbcExecutor;
	private DataSource dataSource;
	
	private Connection tempConnection;
 	
 	/* (non-Javadoc)
 	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
 	 */
 	public void afterPropertiesSet() throws Exception {
 		DDLGenerator ddlGenerator = new DDLGenerator(daoManager.getDialect(), daoManager.getEntityRepository());
 		List<String> ddls = ddlGenerator.generateCreateSchemaObjectsDDL();
 		for (String ddl : ddls) {
 			jdbcExecutor.execute(ddl);
 		}
		
		// keep one connection so that in-memory does not shutdown
		this.tempConnection = dataSource.getConnection();
 	}
 
 	public final void setDaoManager(DaoManager daoManager) {
 		this.daoManager = daoManager;
 	}
 	
 	public final void setJdbcExecutor(JdbcExecutor jdbcExecutor) {
 		this.jdbcExecutor = jdbcExecutor;
 	}
 
	public final void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

 	
 }
