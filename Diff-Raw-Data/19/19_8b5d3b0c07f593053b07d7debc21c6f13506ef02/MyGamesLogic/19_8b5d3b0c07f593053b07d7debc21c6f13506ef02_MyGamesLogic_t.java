 package funktionalitet;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import connector.Connector;
 import daoimpl.MySQLGameDAO;
 import daoimpl.MySQLRoleDAO;
 import daoimpl.MySQLUsersDAO;
 import daoimpl.MySQLUsersGamesDAO;
 import daointerfaces.DALException;
 import daointerfaces.GameIDAO;
 import daointerfaces.RoleIDAO;
 import daointerfaces.UsersGamesIDAO;
 import daointerfaces.UsersIDAO;
 import daointerfaces.UsersLangIDAO;
 import dto.GameDTO;
 import dto.UsersDTO;
 import dto.UsersGamesDTO;
 
 public class MyGamesLogic implements IMyGamesLogic {
 
 	private UsersGamesIDAO o;
 	private GameIDAO g;
 	
 	public MyGamesLogic() throws DALException {
 		try {
 			new Connector();
 		} catch (InstantiationException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		o = new MySQLUsersGamesDAO();
 		g = new MySQLGameDAO();
 	}
 	@Override
 	public void addGame(UsersGamesDTO game) throws DALException {
 		o.create(game);
 	}
 
 	@Override
 	public List<GameDTO> getMyGames(String email) throws DALException {
		List<UsersGamesDTO> myGames = new ArrayList<UsersGamesDTO>();
 		List<GameDTO> games = new ArrayList<GameDTO>();
		try 
		{
			myGames = o.getListbyEmail(email);
 		for (int i= 0; i< myGames.size(); i++)
 		{
 			int a = myGames.get(i).getGid();
 			games.add(g.get(a));
 		}
 		return games;
		} catch (DALException e){ throw new DALException("You haven't registrered any games yet. Please do so.");}
 	}
 
 }
