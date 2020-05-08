 package org.soldomi.commons;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.Statement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import java.util.ListIterator;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Set;
 
 public class SqlRequest<I, O> extends DaoAction<I> {
     private interface ResultSetMapper<O> {
 	public void feed(O o, ResultSet resultSet) throws SQLException;
     }
 
     private interface StatementMapper<I> {
 	public void feed(I i, PreparedStatement statement) throws SQLException;
     }
 
     private interface SqlFunction<I,O> {
 	public O apply(I i) throws SQLException;
     }
 
     private final String m_sql;
     private final Function<I, Edge<I, O>> m_fctEdge;
     private final SqlFunction<Statement, ResultSet> m_fctResultSet;
     private final List<StatementMapper<I>> m_inputs = new ArrayList<StatementMapper<I>>();
     private final List<ResultSetMapper<O>> m_outputs = new ArrayList<ResultSetMapper<O>>();
 
     private SqlRequest(String sql,
 		       Function<I, Edge<I, O>> fctEdge,
 		       SqlFunction<Statement, ResultSet> fctResultSet) {
 	m_sql = sql;
 	m_fctEdge = fctEdge;
 	m_fctResultSet = fctResultSet;
     }
 
     public static <I, O> SqlRequest<I, O> select(String sql, Function<I, Edge<I, O>> fctEdge) {
 	return new SqlRequest(sql, fctEdge, new SqlFunction<Statement, ResultSet>() {
 		@Override public ResultSet apply(Statement statement) throws SQLException {
 		    return statement.getResultSet();
 		}
 	});
     }
 
     public static <I, O> SqlRequest<I, O> insert(String sql, Function<I, Edge<I, O>> fctEdge) {
 	return new SqlRequest(sql, fctEdge, new SqlFunction<Statement, ResultSet>() {
 		@Override public ResultSet apply(Statement statement) throws SQLException {
 		    return statement.getGeneratedKeys();
 		}
 	});
     }
 
     public <P> SqlRequest<I, O> addInput(final Function<I, Property<P>> metaProperty) {
 	final int index = m_inputs.size() + 1;
 	m_inputs.add(new StatementMapper<I>() {
 		@Override public void feed(I i, PreparedStatement statement) throws SQLException {
 		    metaProperty.apply(i).feedStatement(statement, index);
 		}
 	    });
 	return this;
     }
 
     public <P> SqlRequest<I, O> addOutput(final Function<O, Property<P>> metaProperty) {
 	final int index = m_outputs.size() + 1;
 	m_outputs.add(new ResultSetMapper<O>() {
 		@Override public void feed(O o, ResultSet resultSet) throws SQLException {
 		    metaProperty.apply(o).fromResultSet(resultSet, index);
 		}
 	    });
 	return this;
     }
 
     @Override public Result<Void> run(final Connection connection, final I i) {
 	try {
 	    PreparedStatement statement = connection.prepareStatement(m_sql);
 	    for (final StatementMapper<I> mapper : m_inputs) {
 		mapper.feed(i, statement);
 	    }
 	    System.out.println("Running SQL : " + statement);
 	    statement.execute();
 	    ResultSet resultSet = m_fctResultSet.apply(statement);
 	    EdgePopulator<O> edgePopulator = m_fctEdge.apply(i).populator();
	    while(resultSet.next()) {
 		final O o = edgePopulator.next();
 		for (final ResultSetMapper<O> mapper : m_outputs) {
 		    mapper.feed(o, resultSet);
 		}
 	    }
 	    return Result.<Void>success(null);
 	} catch (SQLException e) {
 	    return Result.<Void>failure(e.toString());
 	}
     }
 }
