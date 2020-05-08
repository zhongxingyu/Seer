 package daoimpl;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import connector.Connector;
 
 import dto.GameDTO;
 import daointerfaces.GameIDAO;
 import daointerfaces.DALException;
 
 
 public class MySQLGameDAO extends GameIDAO
 {
 
 	public void create(GameDTO row) throws DALException
 	{
 		String create = "INSERT INTO Game(Gid, Gname, Released, Url) VALUES (" + row.getGid() + ", '" + row.getGname() + "', '" + row.getReleased() + "', '" + row.getUrl() + "');";
 		Connector.doUpdate(create);
 	}
 	public void delete(int Gid) throws DALException
 	{
 		String delete = "DELETE FROM Game WHERE Gid = " + Gid + ";";
 		Connector.doUpdate(delete);
 	}
 	public void update(GameDTO row) throws DALException
 	{
 		String update = "UPDATE Game SET Gid = " + row.getGid() + ", Gname = '" + row.getGname() + "', Released = '" + row.getReleased() + "', Url = '" + row.getUrl() + "' WHERE Gid = " + row.getGid() + ";";
 		Connector.doUpdate(update);
 	}

 	public GameDTO getById(int Gid) throws DALException
 	{
 		try
 		{
 			ResultSet rs = Connector.doQuery("SELECT * FROM Game WHERE Gid = " + Gid + ";");
 			if(!rs.next()) throw new DALException("Missing entry.");
 			return new GameDTO(rs.getInt("Gid"), rs.getString("Gname"), rs.getString("Released"), rs.getString("Url"));
 		}
 		catch(SQLException e){throw new DALException(e);}
 	}
 	public GameDTO getByTitle(String Title) throws DALException
 	{
 		try
 		{
 			ResultSet rs = Connector.doQuery("SELECT * FROM Game WHERE Gname = '" + Title + "';");
 			if(!rs.next()) throw new DALException("Missing entry.");
 			return new GameDTO(rs.getInt("Gid"), rs.getString("Gname"), rs.getString("Released"), rs.getString("Url"));
 		}
 		catch(SQLException e){throw new DALException(e);}
 	}
 
 	public List<GameDTO> getList() throws DALException
 	{
 		try
 		{
 			List<GameDTO> results = new ArrayList<GameDTO>();
 			ResultSet rs = Connector.doQuery("SELECT * FROM Game ORDER BY Gname;");
 			if(!rs.next()) throw new DALException("Missing table: Game");
 			do results.add(new GameDTO(rs.getInt("Gid"), rs.getString("Gname"), rs.getString("Released"), rs.getString("Url")));
 			while(rs.next());
 			return results;
 		}
 		catch(SQLException e){throw new DALException(e);}
 	}
 }
