 package com.socialblackjack.game.dao.impl;
 
 import java.util.List;
 
 import org.hibernate.SessionFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Repository;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.socialblackjack.game.dao.GameTableDao;
 import com.socialblackjack.game.entities.GameTable;
 import com.socialblackjack.game.entities.Player;
 
 @Repository
 @Transactional
 public class GameTableDaoImpl implements GameTableDao {
 
 	@Autowired
     private SessionFactory sessionFactory;
 	
 	public void createTable(GameTable table) {
 	}
 
 	public GameTable getTable(String table) {
 		return (GameTable) sessionFactory.getCurrentSession().createQuery("From GameTable where name = '" + table + "'").uniqueResult();
 	}
 
 	public List<Player> getPlayers(String table) {
		String hql = "Select p From GameTablePlayer gtu join gtu.gameTable as g join gtu.player as p where g.name = '"+table+"'";
 		return sessionFactory.getCurrentSession().createQuery(hql).list();
 	}
 
 	public void removePlayersFromTable(String table) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void sitPlayers(String table) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void addPlayerToWait(String playerToken, String table) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void addPlayerToPlay(String playerToken, String table, Integer seat) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void sitOutPlayer(String playerToken, String table) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void cancelSitoutPlayer(String playerToken, String table) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void cancelWaitingPlayer(String playerToken, String table) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void removePlayerFromTable(String playerToken, String table) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<GameTable> getTables() {
 		return sessionFactory.getCurrentSession().createQuery("FROM GameTable").list();
 	}
 
 	@Override
 	public GameTable getTableById(Integer tableId) {
 		return (GameTable) sessionFactory.getCurrentSession().get(GameTable.class, tableId);
 	}
 
 }
