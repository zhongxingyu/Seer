 package dbs.project.dao;
 
 import java.util.List;
 
import org.hibernate.criterion.Restrictions;

 import dbs.project.entity.Player;
import dbs.project.entity.Team;
 
 public class PlayerDao extends DaoBase {
 
 	/**
 	 * Creates or updates a player
 	 * 
 	 * @param player
 	 */
 	public static void save(Player player) {
 		session.beginTransaction();
 		session.saveOrUpdate(player);
 		session.getTransaction().commit();
 	}
 
 	/**
 	 * Creates or updates a list of players
 	 * 
 	 * @param players
 	 */
 	public static void saveAll(List<Player> players) {
 		for (Player player : players)
 			save(player);
 	}
 
 	/**
 	 * Deletes a player
 	 * 
 	 * @param Player
 	 */
 	public static void delete(Player player) {
 		session.beginTransaction();
 		session.delete(player);
 		session.getTransaction().commit();
 	}
 
 	/**
 	 * Tries to find a player
 	 * 
 	 * @param name
 	 * @return
 	 */
 	public static Player find(String name) {
 		return (Player) session.load(Player.class, name);
 	}
 
 	/**
 	 * Fetches all player entries
 	 * 
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	public static List<Player> fetchAll() {
 		return session.createQuery("From Player").list();
 	}
 
 }
