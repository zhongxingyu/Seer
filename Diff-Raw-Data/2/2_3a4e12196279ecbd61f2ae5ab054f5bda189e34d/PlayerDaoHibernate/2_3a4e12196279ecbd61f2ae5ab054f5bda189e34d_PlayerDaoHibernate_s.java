 package com.zzvc.mmps.dao.hibernate;
 
 import java.util.Date;
 import java.util.List;
 
 import org.appfuse.dao.hibernate.GenericDaoHibernate;
 import org.hibernate.criterion.Restrictions;
 import org.springframework.orm.ObjectRetrievalFailureException;
 import org.springframework.stereotype.Repository;
 
 import com.zzvc.mmps.dao.PlayerDao;
 import com.zzvc.mmps.model.Player;
 
 @Repository("playerDao")
 public class PlayerDaoHibernate extends GenericDaoHibernate<Player, Long> implements PlayerDao {
 
 	public PlayerDaoHibernate() {
 		super(Player.class);
 	}
 	
 	@Override
 	@SuppressWarnings("unchecked")
 	public Player findByAddress(String address) {
 		List<Player> players = getSession().createCriteria(Player.class).add(Restrictions.eq("address", address)).list();
 		if (players.isEmpty()) {
			new ObjectRetrievalFailureException("Cannot find player with address '" + address + "'", null);
 		}
 		return players.get(0);
 	}
 
 	@Override
 	@SuppressWarnings("unchecked")
 	public List<Player> findByAddresses(List<String> addresses) {
 		return getSession().createCriteria(Player.class).add(Restrictions.in("address", addresses)).list();
 	}
 
 	@Override
 	@SuppressWarnings("unchecked")
 	public List<Player> findByHeartbeatBefore(Date time) {
 		return getSession().createCriteria(Player.class).add(Restrictions.or(Restrictions.isNull("lastHeartbeat"), Restrictions.le("lastHeartbeat", time))).add(Restrictions.eq("enabled", true)).list();
 	}
 
 	@Override
 	@SuppressWarnings("unchecked")
 	public List<Player> findByHeartbeatAfter(Date time) {
 		return getSession().createCriteria(Player.class).add(Restrictions.gt("lastHeartbeat", time)).add(Restrictions.eq("enabled", true)).list();
 	}
 	
 	@Override
 	@SuppressWarnings("unchecked")
 	public List<Player> findAll() {
 		return getSession().createCriteria(Player.class).add(Restrictions.eq("enabled", true)).list();
 	}
 
 }
