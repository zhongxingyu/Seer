 package org.nothing;
 
 import static junit.framework.Assert.*;
 import static org.nothing.JdbcCanBeNice.*;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Arrays;
 import java.util.List;
 
 import org.junit.Test;
 import static org.mockito.Mockito.*;
 
 public class JdbcCanBeNiceTest {
 
 	private ConnectionProvider createConnectionProvider() {
 		ConnectionProvider connectionProvider = mock(ConnectionProvider.class);
 		try {
 			Connection connection = mock(Connection.class);
 			when(connectionProvider.get()).thenReturn(connection);
 		} catch (SQLException e1) {
 			fail("Shouldn't happen");
 		}
 		return connectionProvider;
 	}
 
 	@Test
 	public void testCachingConnectionProvider() {
 		ConnectionProvider connectionProvider = createConnectionProvider();
 
 		ConnectionProvider cachingConnectionProvider = cachingConnectionProvider(connectionProvider);
 
 		try {
 			cachingConnectionProvider.get();
 			cachingConnectionProvider.get();
 			verify(connectionProvider, times(1)).get();
 		} catch (SQLException e) {
 			fail("Shouldn't happen");
 		}
 	}
 
 	@Test
 	public void testDoWithConnection() {
 		ConnectionProvider connectionProvider = createConnectionProvider();
 		ConnectionProvider cachingConnectionProvider = cachingConnectionProvider(connectionProvider);
 
 		JdbcAction<Object> jdbcAction = mock(JdbcAction.class);
 
 		try {
 			when(jdbcAction.doWithConnection(cachingConnectionProvider.get()))
 					.thenReturn("jdbc.can.be.nice");
 			Object res = doWithConnection(jdbcAction, cachingConnectionProvider);
 			verify(connectionProvider, times(1)).get();
 			verify(jdbcAction, times(1)).doWithConnection(
 					cachingConnectionProvider.get());
 			assertEquals("jdbc.can.be.nice", res);
 		} catch (SQLException e) {
 			fail("Shouldn't happen");
 		}
 	}
 
 	@Test
 	public void testSqlTxSuccess() {
 		final Connection connection = mock(Connection.class);
 		ConnectionProvider connectionProvider = new ConnectionProvider() {
 
 			public Connection get() throws SQLException {
 				return connection;
 			}
 		};
 
 		JdbcAction<Object> sucessfullJdbcAction = mock(JdbcAction.class);
 		try {
 			when(sucessfullJdbcAction.doWithConnection(connection)).thenReturn(
 					"jdbc.can.be.nice");
 		} catch (SQLException e) {
 			fail("Shouldn't happen");
 		}
 
 		try {
 			ChainableJdbcAction<Object> txJdbcAction = sqlTx(sucessfullJdbcAction);
 			Object res = doWithConnection(txJdbcAction, connectionProvider);
 			verify(sucessfullJdbcAction, times(1)).doWithConnection(connection);
 			verify(connection, atLeast(1)).setAutoCommit(false);
 			verify(connection).commit();
 			verify(connection, never()).rollback();
 			assertEquals("jdbc.can.be.nice", res);
 		} catch (SQLException e) {
 			fail("Shouldn't happen");
 		}
 	}
 
 	@Test
 	public void testSqlTxFailing() {
 		final Connection connection = mock(Connection.class);
 		ConnectionProvider connectionProvider = new ConnectionProvider() {
 
 			public Connection get() throws SQLException {
 				return connection;
 			}
 		};
 
 		JdbcAction<Object> failingJdbcAction = mock(JdbcAction.class);
 		try {
 			when(failingJdbcAction.doWithConnection(connection)).thenThrow(
 					new RuntimeException());
 		} catch (SQLException e) {
 			fail("Shouldn't happen");
 		}
 
 		try {
 			ChainableJdbcAction<Object> txJdbcAction = sqlTx(failingJdbcAction);
 			try {
 				doWithConnection(txJdbcAction, connectionProvider);
 				fail("Shouldn't happen");
 			} catch (RuntimeException e) {
 				// expected
 			}
 			verify(failingJdbcAction, times(1)).doWithConnection(connection);
 			verify(connection, atLeast(1)).setAutoCommit(false);
 			verify(connection, never()).commit();
 			verify(connection).rollback();
 		} catch (SQLException e) {
 			fail("Shouldn't happen");
 		}
 	}
 
 	@Test
 	public void testSqlUpdate() {
 		final Connection connection = mock(Connection.class);
 		ConnectionProvider connectionProvider = new ConnectionProvider() {
 
 			public Connection get() throws SQLException {
 				return connection;
 			}
 		};
 
 		PreparedStatement preparedStatement = mock(PreparedStatement.class);
 		String sql = "jdbc.can.be.nice";
 
 		try {
 			when(connection.prepareStatement(sql))
 					.thenReturn(preparedStatement);
 			ChainableJdbcAction<Integer> action = sqlUpdate(sql, 1, true,
 					"string");
 			doWithConnection(action, connectionProvider);
 
 			verify(connection).prepareStatement(sql);
 			verify(preparedStatement).setObject(1, 1);
 			verify(preparedStatement).setObject(2, true);
 			verify(preparedStatement).setObject(3, "string");
 			verify(preparedStatement).executeUpdate();
 		} catch (SQLException e) {
 			fail("Shouldn't happen");
 		}
 	}
 
 	@Test
 	public void testDontThrowSqlException() {
 		final Connection connection = mock(Connection.class);
 		ConnectionProvider connectionProvider = new ConnectionProvider() {
 
 			public Connection get() throws SQLException {
 				return connection;
 			}
 		};
 
 		JdbcAction<Object> action = mock(JdbcAction.class);
 		try {
 			when(action.doWithConnection(connection)).thenThrow(
 					new SQLException());
 		} catch (SQLException e) {
 			fail("Shouldn't happen");
 		}
 		ChainableJdbcAction<Object> dontThrowSqlExceptionAction = dontThrowSqlException(
 				action, "value");
 
 		// This hould not throw an SQL Exception
 		Object res = doWithConnection(dontThrowSqlExceptionAction,
 				connectionProvider);
 
 		try {
 			// check that the original action was called
 			verify(action).doWithConnection(connection);
 			assertEquals("value", res);
 		} catch (SQLException e) {
 			fail("Shouldn't happen");
 		}
 	}
 
 	@Test
 	public void testSqlUpdateAndReturnKey() {
 		final Connection connection = mock(Connection.class);
 		ConnectionProvider connectionProvider = new ConnectionProvider() {
 
 			public Connection get() throws SQLException {
 				return connection;
 			}
 		};
 
 		PreparedStatement preparedStatement = mock(PreparedStatement.class);
 		ResultSet resultSet = mock(ResultSet.class);
 		String sql = "jdbc.can.be.nice";
 
 		try {
 			when(resultSet.getObject(1)).thenReturn(82);
 			when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
 			when(
 					connection.prepareStatement(sql,
 							Statement.RETURN_GENERATED_KEYS)).thenReturn(
 					preparedStatement);
 			ChainableJdbcAction<Number> action = sqlUpdateAndReturnKey(sql, 1,
 					true, "string");
 			Number generatedKey = doWithConnection(action, connectionProvider);
 
 			verify(connection).prepareStatement(sql,
 					Statement.RETURN_GENERATED_KEYS);
 			verify(preparedStatement).setObject(1, 1);
 			verify(preparedStatement).setObject(2, true);
 			verify(preparedStatement).setObject(3, "string");
 			verify(preparedStatement).executeUpdate();
 			verify(preparedStatement).getGeneratedKeys();
 			verify(resultSet).next();
 			verify(resultSet).getObject(1);
 			assertEquals(82, generatedKey);
 		} catch (SQLException e) {
 			fail("Shouldn't happen");
 		}
 	}
 
 	@Test
 	public void testSqlQuery() {
 		class Person {
 			private Integer id;
 			private String name;
 
 			public Person(Integer id, String name) {
 				super();
 				this.id = id;
 				this.name = name;
 			}
 
 			@Override
 			public int hashCode() {
 				final int prime = 31;
 				int result = 1;
 				result = prime * result + ((id == null) ? 0 : id.hashCode());
 				result = prime * result
 						+ ((name == null) ? 0 : name.hashCode());
 				return result;
 			}
 
 			@Override
 			public boolean equals(Object obj) {
 				if (this == obj)
 					return true;
 				if (obj == null)
 					return false;
 				if (getClass() != obj.getClass())
 					return false;
 				Person other = (Person) obj;
 				if (id == null) {
 					if (other.id != null)
 						return false;
 				} else if (!id.equals(other.id))
 					return false;
 				if (name == null) {
 					if (other.name != null)
 						return false;
 				} else if (!name.equals(other.name))
 					return false;
 				return true;
 			}
 
 			@Override
 			public String toString() {
 				return "Person [id=" + id + ", name=" + name + "]";
 			}
 
 		}
 
 		final RowMapper<Person> personMapper = new RowMapper<Person>() {
 			public Person mapRow(ResultSet resultSet, int row)
 					throws SQLException {
 				return new Person(resultSet.getInt("id"), resultSet
 						.getString("name"));
 			}
 		};
 
 		final Connection connection = mock(Connection.class);
 		ConnectionProvider connectionProvider = new ConnectionProvider() {
 
 			public Connection get() throws SQLException {
 				return connection;
 			}
 		};
 
 		PreparedStatement preparedStatement = mock(PreparedStatement.class);
 		ResultSet resultSet = mock(ResultSet.class);
 		String sql = "jdbc.can.be.nice";
 		List<Person> expectedPersons = Arrays.asList(new Person(82, "a"),
 				new Person(1, "b"));
 
 		try {
 			when(resultSet.next()).thenReturn(true, true, false);
 			when(resultSet.getInt("id")).thenReturn(82, 1);
 			when(resultSet.getString("name")).thenReturn("a", "b");
 
 			when(preparedStatement.executeQuery()).thenReturn(resultSet);
 			when(connection.prepareStatement(sql))
 					.thenReturn(preparedStatement);
 			ChainableJdbcAction<List<Person>> action = sqlQuery(sql,
 					personMapper, 1, true, "string");
 			List<Person> persons = doWithConnection(action, connectionProvider);
 			verify(connection).prepareStatement(sql);
 			verify(preparedStatement).setObject(1, 1);
 			verify(preparedStatement).setObject(2, true);
 			verify(preparedStatement).setObject(3, "string");
 			verify(preparedStatement).executeQuery();
 			assertEquals(expectedPersons, persons);
 		} catch (SQLException e) {
 			fail("Shouldn't happen");
 		}
 	}
 
 	@Test
	public void testChainig1() {
 		final Connection connection = mock(Connection.class);
 		ConnectionProvider connectionProvider = new ConnectionProvider() {
 
 			public Connection get() throws SQLException {
 				return connection;
 			}
 		};
 
 		JdbcAction<Integer> action1 = mock(JdbcAction.class);
 		JdbcAction<Boolean> action2 = mock(JdbcAction.class);
 		JdbcAction<String> action3 = mock(JdbcAction.class);
 
 		ChainableJdbcAction<Integer> action = sqlMakeChainable(action1).then(
 				action2).then(action3);
 		try {
 			when(action1.doWithConnection(connection)).thenReturn(82);
 			when(action2.doWithConnection(connection)).thenReturn(true);
 			when(action3.doWithConnection(connection)).thenReturn("jdbc");
 		} catch (SQLException e) {
 			fail("Shouldn't happen");
 		}
 
 		try {
 			int res = doWithConnection(action, connectionProvider);
 
 			verify(action1).doWithConnection(connection);
 			verify(action2).doWithConnection(connection);
 			verify(action3).doWithConnection(connection);
 			assertEquals(82, res);
 		} catch (SQLException e) {
 			fail("Shouldn't happen");
 		}
 	}
 
 	@Test
	public void testChainig2() {
 		final Connection connection = mock(Connection.class);
 		ConnectionProvider connectionProvider = new ConnectionProvider() {
 
 			public Connection get() throws SQLException {
 				return connection;
 			}
 		};
 
 		JdbcAction<Integer> action1 = mock(JdbcAction.class);
 		JdbcAction<Boolean> action2 = mock(JdbcAction.class);
 		JdbcAction<String> action3 = mock(JdbcAction.class);
 
 		ChainableJdbcAction<Boolean> action = sqlMakeChainable(action1)
 				.thenReturn(action2).then(action3);
 		try {
 			when(action1.doWithConnection(connection)).thenReturn(82);
 			when(action2.doWithConnection(connection)).thenReturn(true);
 			when(action3.doWithConnection(connection)).thenReturn("jdbc");
 		} catch (SQLException e) {
 			fail("Shouldn't happen");
 		}
 
 		try {
 
 			Boolean res = doWithConnection(action, connectionProvider);
 
 			verify(action1).doWithConnection(connection);
 			verify(action2).doWithConnection(connection);
 			verify(action3).doWithConnection(connection);
 			assertTrue(res);
 		} catch (SQLException e) {
 			fail("Shouldn't happen");
 		}
 	}
 
 	@Test
	public void testChainig3() {
 		final Connection connection = mock(Connection.class);
 		ConnectionProvider connectionProvider = new ConnectionProvider() {
 
 			public Connection get() throws SQLException {
 				return connection;
 			}
 		};
 
 		JdbcAction<Integer> action1 = mock(JdbcAction.class);
 		JdbcAction<Boolean> action2 = mock(JdbcAction.class);
 		JdbcAction<String> action3 = mock(JdbcAction.class);
 
 		ChainableJdbcAction<String> action = sqlMakeChainable(action1).then(
 				action2).thenReturn(action3);
 		try {
 			when(action1.doWithConnection(connection)).thenReturn(82);
 			when(action2.doWithConnection(connection)).thenReturn(true);
 			when(action3.doWithConnection(connection)).thenReturn("jdbc");
 		} catch (SQLException e) {
 			fail("Shouldn't happen");
 		}
 
 		try {
 
 			String res = doWithConnection(action, connectionProvider);
 
 			verify(action1).doWithConnection(connection);
 			verify(action2).doWithConnection(connection);
 			verify(action3).doWithConnection(connection);
 			assertEquals("jdbc", res);
 		} catch (SQLException e) {
 			fail("Shouldn't happen");
 		}
 	}
	
 	@Test
	public void testChainig4() {
 		final Connection connection = mock(Connection.class);
 		ConnectionProvider connectionProvider = new ConnectionProvider() {
 
 			public Connection get() throws SQLException {
 				return connection;
 			}
 		};
 
 		JdbcAction<Integer> action1 = mock(JdbcAction.class);
 		JdbcAction<Boolean> action2 = mock(JdbcAction.class);
 		JdbcAction<String> action3 = mock(JdbcAction.class);
 
 		ChainableJdbcAction<String> action = sqlMakeChainable(action1)
 				.thenReturn(action2).thenReturn(action3);
 		try {
 			when(action1.doWithConnection(connection)).thenReturn(82);
 			when(action2.doWithConnection(connection)).thenReturn(true);
 			when(action3.doWithConnection(connection)).thenReturn("jdbc");
 		} catch (SQLException e) {
 			fail("Shouldn't happen");
 		}
 
 		try {
 
 			String res = doWithConnection(action, connectionProvider);
 
 			verify(action1).doWithConnection(connection);
 			verify(action2).doWithConnection(connection);
 			verify(action3).doWithConnection(connection);
 			assertEquals("jdbc", res);
 		} catch (SQLException e) {
 			fail("Shouldn't happen");
 		}
 	}

 }
