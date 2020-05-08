 package cz.muni.fi.pv243.sportleaguesystem.service;
 
 import java.util.List;
 
 import javax.enterprise.context.ApplicationScoped;
 import javax.inject.Inject;
 
 import org.apache.log4j.Logger;
 
 import cz.muni.fi.pv243.sportleaguesystem.dao.interfaces.LeagueDAO;
 import cz.muni.fi.pv243.sportleaguesystem.dao.interfaces.MatchDAO;
 import cz.muni.fi.pv243.sportleaguesystem.dao.interfaces.PrincipalDAO;
 import cz.muni.fi.pv243.sportleaguesystem.dao.interfaces.UserDAO;
 import cz.muni.fi.pv243.sportleaguesystem.entities.League;
 import cz.muni.fi.pv243.sportleaguesystem.entities.Match;
 import cz.muni.fi.pv243.sportleaguesystem.entities.User;
 import cz.muni.fi.pv243.sportleaguesystem.service.interfaces.UserService;
 
 @ApplicationScoped
 public class UserServiceImpl implements UserService {
 
 	@Inject
 	private Logger logger;
 	
 	@Inject
 	private UserDAO userDAO;
 	
 	@Inject 
 	private MatchDAO matchDAO;
 	
 	@Inject
 	private LeagueDAO leagueDAO;
 	
 	@Inject
 	private PrincipalDAO principalDAO;
 	
 	@Override
 	public void createUser(User user) {
 		if (user == null) {
 			logger.error("Creating null user.");
 			throw new IllegalArgumentException("null user");
 		}		
 		if (user.getId() != null) {
 			logger.error("Creating user with assigned id.");
 			throw new IllegalArgumentException("user id assigned");
 		}
 		userDAO.create(user);
		logger.info("Created user:" + user.toString());
 	}
 
 	@Override
 	public void updateUser(User user) {
 		if (user == null) {
 			logger.error("Updating null user.");
 			throw new IllegalArgumentException("null user");
 		}
 		
 		if (user.getId() == null || userDAO.get(user.getId()) == null) {
 			logger.error("Updating nonexistent user.");
 			throw new IllegalArgumentException("nonexistent user");
 		}
 		userDAO.update(user);
		logger.info("Updated user. Id: " + user.toString());
 	}
 
 	@Override
 	public User getById(Long id) {
 		if (id == null) {
 			logger.error("Getting a user by null id.");
 			throw new IllegalArgumentException("null id");
 		}
		logger.info("Returning user with id: " + id);
 		return userDAO.get(id);
 	}
 
 	@Override
 	public List<User> getAll() {
 		logger.info("Returning all users.");
 		return userDAO.findAll();
 	}
 
 	@Override
 	public void deleteUser(User user) {
 		if (user == null) {
 			logger.error("Deleting null user.");
 			throw new IllegalArgumentException("null user");
 		}
 		
 		
 		List<Match> userMatches = matchDAO.findMatchesByUser(user);
 		for(Match match : userMatches){
 			matchDAO.delete(match);
 		}
 		logger.info("Deleted all matches of user.");
 		user = getById(user.getId());
 		List<League> registeredLeagues = user.getLeagues();
 		for(League league : registeredLeagues){			
 			league.getPlayers().remove(user);
 			leagueDAO.update(league);
 		}
 		logger.info("Removed user from all leagues.");
 		principalDAO.delete(principalDAO.findPrincipalByUser(user));
		logger.info("Deleted user.");
 		// Object principal cascaduje delete a preto vymaze zaroven aj usera
 		//userDAO.delete(user);
 	}
 
 	@Override
 	public List<User> findByName(String name) {
 		if (name == null) {
 			logger.error("Trying to find a user by null name.");
 			throw new IllegalArgumentException("null name");
 		}
		logger.info("Returning users with name: " + name);
 		return userDAO.findUsersByName(name);
 	}
 	
 	@Override
 	public void registerToLeague(User user, League league){
 		
 		if (league == null ){
 			logger.error("Registering user to null league.");
 			throw new IllegalArgumentException("null league");
 		}
 		if (league.getId() == null ){
 			logger.error("Registering user to league with null id");
 			throw new IllegalArgumentException("null league id");
 		}
 		if (user == null) {
 			logger.error("Registering null user to league.");
 			throw new IllegalArgumentException("null user");
 		}
 		if (user.getId() == null) {
 			logger.error("Registering user with null id to league.");
 			throw new IllegalArgumentException("null user id");
 		}
 		user = getById(user.getId());
 		league = leagueDAO.get(league.getId());
 		if(! league.getPlayers().contains(user)) {
 			league.getPlayers().add(user);
 		}
 		leagueDAO.update(league);
		logger.info("Registered user:\n " + user.toString() + "\nto league: \n" + league.toString());
 	}
 
 }
