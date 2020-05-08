 package cl.own.usi.dao.impl.memory;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.NavigableSet;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.ConcurrentSkipListSet;
 
 import org.springframework.stereotype.Repository;
 
 import cl.own.usi.dao.ScoreDAO;
 import cl.own.usi.model.User;
 
 @Repository
 public class ScoreDAOImpl implements ScoreDAO {
 
 	private ConcurrentSkipListSet<User> rankedUsers = new ConcurrentSkipListSet<User>();
 	private ConcurrentMap<User, Integer> userBonuses = new ConcurrentHashMap<User, Integer>();
 	
	public boolean updateScore(User user, int newScore) {
 		if (rankedUsers.contains(user)) {
 			rankedUsers.remove(user);
 		}
		user.setScore(newScore);
 		return rankedUsers.add(user);
 	}
 
 	public List<User> getTop(int limit) {
 		List<User> users = new ArrayList<User>(limit);
 		int i = 0;
 		for (User user : rankedUsers) {
 			if (i++ < limit) {
 				users.add(user);
 			} else {
 				break;
 			}
 		}
 		return users;
 	}
 
 	public List<User> getBefore(User user, int limit) {
 		List<User> users = new ArrayList<User>(limit);
 		NavigableSet<User> usersBefore = rankedUsers.headSet(user, false);
 		int i = 0;
 		for (User userBefore : usersBefore) {
 			if (i++ < limit) {
 				users.add(userBefore);
 			} else {
 				break;
 			}
 		}
 		return users;
 	}
 
 	public List<User> getAfter(User user, int limit) {
 		List<User> users = new ArrayList<User>(limit);
 		NavigableSet<User> usersAfter = rankedUsers.tailSet(user, false);
 		int i = 0;
 		for (User userAfter : usersAfter) {
 			if (i++ < limit) {
 				users.add(userAfter);
 			} else {
 				break;
 			}
 		}
 		return users;
 	}
 
 	public int getUserBonus(User user) {
 		Integer bonus = userBonuses.get(user);
 		if (bonus == null) {
 			return 0;
 		} else {
 			return bonus;
 		}
 	}
 
 	public void setUserBonus(User user, int newBonus) {
 		userBonuses.put(user, newBonus);
 	}
 
 	@Override
 	public void flushUsers() {
 		rankedUsers.clear();
 		userBonuses.clear();
 	}
 
 }
