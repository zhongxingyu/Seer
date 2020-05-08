 package cl.own.usi.dao;
 
 import java.util.List;
 
 import cl.own.usi.model.User;
 
 public interface ScoreDAO {
 
 	/**
 	 * For a given User, update its score with the given delta.
 	 * 
 	 * @param user
 	 * @return
 	 */
	boolean updateScore(User user);
 	
 	/**
 	 * Return the Users having the higher score.
 	 * 
 	 * @param limit
 	 * @return
 	 */
 	List<User> getTop(int limit);
 	
 	/**
 	 * Return the Users having a score just better than the given User.
 	 * 
 	 * @param user
 	 * @param limit
 	 * @return
 	 */
 	List<User> getBefore(User user, int limit);
 	
 	/**
 	 * Return the Users having a score just worst than the given User.
 	 * @param user
 	 * @param limit
 	 * @return
 	 */
 	List<User> getAfter(User user, int limit);
 	
 	
 	int getUserBonus(User user);
 	
 	void setUserBonus(User user, int newBonus);
 	
 	void flushUsers();
 	
 }
