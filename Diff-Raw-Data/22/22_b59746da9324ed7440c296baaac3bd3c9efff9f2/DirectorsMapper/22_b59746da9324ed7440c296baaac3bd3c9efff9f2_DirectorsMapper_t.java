 package dk.kea.si.movies.persistence.mappers;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import dk.kea.si.movies.domain.Directors;
 import dk.kea.si.movies.domain.DomainObject;
 import dk.kea.si.movies.util.ApplicationException;
 
 public class DirectorsMapper extends AbstractMapper {
 	
 	public static final String COLUMNS = "Director.id, Director.name";
 
 	@Override
 	protected String findStatement() {
 		return "SELECT " + COLUMNS + " FROM Director AS Director"
 				+ " WHERE Director.id=?" + " LIMIT 1";
 	}
 	
 	protected String findByNameStatement() {
 		return "SELECT " + COLUMNS + " FROM Director AS Director"
				+ " WHERE Director.name=?" + " LIMIT 1;";
 	}
 
 	@Override
 	protected String findAllStatement() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	protected String updateStatement() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	protected String insertStatement() {
 		return "INSERT INTO `Director` (name) VALUES (?);";
 	}
 
 	@Override
 	protected String deleteStatement() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	protected DomainObject doLoad(Long id, ResultSet rs) throws SQLException {
 		Directors directors = new Directors();
 		directors.setId(rs.getLong("Director.id"));
 		directors.setName(rs.getString("Director.name"));
 		return directors;
 	}
 
 	@Override
 	protected void doUpdate(DomainObject o, PreparedStatement s)
 			throws SQLException {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	protected void doInsert(DomainObject o, PreparedStatement s)
 			throws SQLException {
 		Directors directors = (Directors) o;
 		s.setString(1, directors.getName());
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
 
 	public Directors findByName(String name) {
 		PreparedStatement statement = null;
 		try {
 			statement = getConnection().prepareStatement(findByNameStatement());
 			statement.setString(1, name);
			System.out.println(statement);
 			ResultSet rs = statement.executeQuery();
 			if(rs.next()) {
 				return (Directors) load(rs);
 			} else {
 				return null;
 			}
 		} catch (SQLException e) {
 			throw new ApplicationException(e);
 		} finally {
 			closeStatement(statement);
 		}
 	}
 
 }
