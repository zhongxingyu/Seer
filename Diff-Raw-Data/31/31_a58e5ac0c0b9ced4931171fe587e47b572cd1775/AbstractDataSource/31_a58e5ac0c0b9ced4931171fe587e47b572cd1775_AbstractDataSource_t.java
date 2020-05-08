 /*
  * Copyright 2009 zaichu xiao
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
 package zcu.xutil.sql;
 
 import java.io.PrintWriter;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 
 import javax.sql.CommonDataSource;
 import javax.sql.DataSource;
 
 import zcu.xutil.XutilRuntimeException;
 
 public abstract class AbstractDataSource implements DataSource {
 	private String testQuery;
 
 	protected abstract CommonDataSource getBase();
 
 	@Override
 	public boolean isWrapperFor(Class<?> arg0) throws SQLException {
 		CommonDataSource cds = getBase();
 		return cds instanceof DataSource ? ((DataSource) cds).isWrapperFor(arg0) : false;
 	}
 
 	@Override
 	public <T> T unwrap(Class<T> arg0) throws SQLException {
 		CommonDataSource cds = getBase();
 		return cds instanceof DataSource ? ((DataSource) cds).unwrap(arg0) : null;
 	}
 
 	@Override
 	public PrintWriter getLogWriter() throws SQLException {
 		return getBase().getLogWriter();
 	}
 
 	@Override
 	public int getLoginTimeout() throws SQLException {
 		return getBase().getLoginTimeout();
 	}
 
 	@Override
 	public void setLogWriter(PrintWriter arg0) throws SQLException {
 		getBase().setLogWriter(arg0);
 
 	}
 
 	@Override
 	public void setLoginTimeout(int arg0) throws SQLException {
 		getBase().setLoginTimeout(arg0);
 
 	}
 
 	@Override
 	public Connection getConnection(String username, String password) throws SQLException {
 		throw new UnsupportedOperationException("Cannot specify username/password for connections");
 	}
 
 	/**
 	 * 设置连接检验SQL
 	 * 
 	 */
 	public void setTestQuery(String query) {
 		testQuery = query;
 	}
 
 	public void testConnection(Connection conn) {
 		try {
			if (testQuery != null) {
				PreparedStatement stmt = conn.prepareStatement(testQuery);
				try {
					stmt.setQueryTimeout(6);
					if (stmt.executeQuery().next())
						return;
				} finally {
					stmt.close();
				}
			} else if (conn.isValid(6))
				return;
			throw new IllegalStateException("connection is invalid.");
 		} catch (SQLException e) {
 			throw new XutilRuntimeException(e);
 		}
 	}
 }
