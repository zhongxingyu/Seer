 package dk.kea.si.movies.persistence.mappers;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import dk.kea.si.movies.domain.Cast;
 import dk.kea.si.movies.domain.DomainObject;
 import dk.kea.si.movies.util.ApplicationException;
 
 public class CastMapper extends AbstractMapper {
 
 	public static final String COLUMNS = "Cast.id, Cast.name";
 
 	@Override
 	protected String findStatement() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	protected String findAllStatement() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	protected String findByNameStatement() {
 		return "SELECT " + COLUMNS + " FROM Actor AS Cast"
				+ " WHERE Cast.name=?" + " LIMIT 1";
 	}
 
 	@Override
 	protected String updateStatement() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	protected String insertStatement() {
 		return "INSERT INTO `Actor` (id, name) VALUES (?, ?);";
 	}
 
 	@Override
 	protected String deleteStatement() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	protected DomainObject doLoad(Long id, ResultSet rs) throws SQLException {
 		Cast cast = new Cast();
 		cast.setId(rs.getLong("Cast.id"));
 		cast.setName(rs.getString("Cast.name"));
 		return cast;
 	}
 
 	@Override
 	protected void doUpdate(DomainObject o, PreparedStatement s)
 			throws SQLException {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	protected void doInsert(DomainObject o, PreparedStatement s)
 			throws SQLException {
 		Cast cast = (Cast) o;
 		s.setLong(1, cast.getId());
 		s.setString(2, cast.getName());
 	}
 
 	@Override
 	protected void doDelete(DomainObject o, PreparedStatement s)
 			throws SQLException {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public DomainObject find(long id) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public Cast findByName(String name) {
 		PreparedStatement statement = null;
 		try {
 			statement = getConnection().prepareStatement(findByNameStatement());
 			statement.setString(1, name);
 			ResultSet rs = statement.executeQuery();
 			if (rs.next()) {
 				return (Cast) load(rs);
 			} else {
 				return null;
 			}
 		} catch (SQLException e) {
 			throw new ApplicationException(e);
 		} finally {
 			closeStatement(statement);
 		}
 	}
 
 	@Override
 	public long insert(DomainObject obj) {
 		PreparedStatement insertStatement = null;
 		try {
 			insertStatement = getConnection().prepareStatement(
 					insertStatement());
 			doInsert(obj, insertStatement);
 			System.out.println(insertStatement);
 			insertStatement.execute();
 			Long lastInsertId = obj.getId();
 			loadedMap.put(lastInsertId, obj);
 			return lastInsertId;
 		} catch (SQLException e) {
 			throw new ApplicationException(e);
 		} finally {
 			closeStatement(insertStatement);
 		}
 	}
 
 }
