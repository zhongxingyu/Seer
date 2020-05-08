 package org.walkingarchive.backend.model.trade;
 
 import java.util.Date;
 import java.util.List;
 
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 import org.walkingarchive.backend.DbHelper;
 import org.walkingarchive.backend.model.card.Card;
 import org.walkingarchive.backend.model.security.User;
 
 public class TradeDAO {
     private static TradeDAO instance = new TradeDAO();
 
     //Singleton
     public static TradeDAO getInstance() {
         return instance;
     }
     
     private TradeDAO() {}
 
     public List<Trade> getAllTrades() {
         return null;
     }
     
     public Trade getTradeForId(Integer id) {
         Session session = DbHelper.getSession();
         Trade trade = (Trade) session.get(Trade.class, id);
         session.close();
         return trade;
     }
     
    public List<Trade> getTradesForUser(Integer userId) {
         Session session = DbHelper.getSession();
        List trades = session.createQuery("from Trade where userid = :user")
             .setParameter("user", userId)
             .list();
         session.close();
         return trades;
     }
 
     public List<Trade> getTradesForDate(Date date) {
         return null;
     }
     
     public Trade createTrade(Trade trade) throws Exception {
         Session session = DbHelper.getSession();
         Transaction tx = null;
         try {
             tx = session.beginTransaction();
             Integer id = (Integer) session.save(trade);
             trade.setId(id);
             tx.commit();
         }
         catch (Exception e) {
             if (tx!=null) tx.rollback();
             throw new RuntimeException("Error creating new Trade", e);
         }
         finally {
             session.close();
         }
 
         return trade;
     }
     
     public Trade updateTrade(Trade trade) throws Exception {
         Session session = DbHelper.getSession();
         Transaction tx = null;
         try {
             tx = session.beginTransaction();
             session.update(trade);
             tx.commit();
         }
         catch (Exception e) {
             if (tx != null) tx.rollback();
             throw new RuntimeException("Error in updating Trade", e);
         }
         finally {
             session.close();
         }
         
         return null;
     }
 }
