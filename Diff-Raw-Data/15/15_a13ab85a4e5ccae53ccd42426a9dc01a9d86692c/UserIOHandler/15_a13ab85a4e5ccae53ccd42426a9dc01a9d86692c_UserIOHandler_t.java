 /*
  * Apr 20, 2006
  */
 package com.thinkparity.model.parity.model.io.db.hsqldb.handler;
 
 import com.thinkparity.model.parity.model.io.db.hsqldb.HypersonicException;
 import com.thinkparity.model.parity.model.io.db.hsqldb.Session;
 import com.thinkparity.model.xmpp.JabberId;
 import com.thinkparity.model.xmpp.user.User;
 
 
 /**
  * @author raykroeker@gmail.com
  * @version 1.1
  */
 public class UserIOHandler extends AbstractIOHandler implements
         com.thinkparity.model.parity.model.io.handler.UserIOHandler {
 
     private static final String SQL_CREATE =
         new StringBuffer("insert into USER ")
         .append("(JABBER_ID) ")
         .append("values (?)")
         .toString();
 
     private static final String SQL_CREATE_INFO =
         new StringBuffer("insert into USER_INFO ")
         .append("(USER_ID,FIRST_NAME,LAST_NAME,ORGANIZATION) ")
         .append("values (?,?,?,?)")
         .toString();
 
     private static final String SQL_READ_BY_JABBER_ID =
         new StringBuffer("select U.USER_ID,U.JABBER_ID,UI.FIRST_NAME,")
         .append("UI.LAST_NAME,UI.ORGANIZATION ")
        .append("from USER U inner join USER_INFO UI ")
         .append("on U.USER_ID=UI.USER_ID ")
         .append("where U.JABBER_ID=?")
         .toString();
 
     private static final String SQL_READ_BY_USER_ID =
         new StringBuffer("select U.USER_ID,U.JABBER_ID,UI.FIRST_NAME,")
         .append("UI.LAST_NAME,UI.ORGANIZATION ")
        .append("from USER U inner join USER_INFO UI ")
         .append("on U.USER_ID=UI.USER_ID ")
         .append("where U.USER_ID=?")
         .toString();
 
     /**
      * Create a UserIOHandler.
      */
     public UserIOHandler() { super(); }
 
     /**
      * @see com.thinkparity.model.parity.model.io.handler.UserIOHandler#create(com.thinkparity.model.xmpp.user.User)
      */
     public User create(final User user) {
         final Session session = openSession();
         try {
             session.prepareStatement(SQL_CREATE);
             session.setQualifiedUsername(1, user.getId());
             if(1 != session.executeUpdate())
                 throw new HypersonicException("");
 
             final Long userId = session.getIdentity();
             session.prepareStatement(SQL_CREATE_INFO);
             session.setLong(1, userId);
             session.setString(2, user.getFirstName());
             session.setString(3, user.getLastName());
             session.setString(4, user.getOrganization());
             if(1 != session.executeUpdate())
                 throw new HypersonicException("");
 
             user.setLocalId(userId);
             return user;
         }
         catch(final HypersonicException hx) {
             session.rollback();
             throw hx;
         }
         finally { session.close(); }
     }
 
     /**
      * @see com.thinkparity.model.parity.model.io.handler.UserIOHandler#read(com.thinkparity.model.xmpp.JabberId)
      */
     public User read(final JabberId jabberId) {
         final Session session = openSession();
         try {
             session.prepareStatement(SQL_READ_BY_JABBER_ID);
             session.setQualifiedUsername(1, jabberId);
             session.executeQuery();
 
             if(session.nextResult()) { return extractUser(session); }
             else { return null; }
         }
         catch(final HypersonicException hx) {
             session.rollback();
             throw hx;
         }
         finally { session.close(); }
     }
 
     /**
      * @see com.thinkparity.model.parity.model.io.handler.UserIOHandler#read(java.lang.Long)
      */
     public User read(final Long userId) {
         final Session session = openSession();
         try {
             session.prepareStatement(SQL_READ_BY_USER_ID);
             session.setLong(1, userId);
             session.executeQuery();
 
             if(session.nextResult()) { return extractUser(session); }
             else { return null; }
         }
         catch(final HypersonicException hx) {
             session.rollback();
             throw hx;
         }
         finally { session.close(); }
     }
 
     private User extractUser(final Session session) {
         final User u = new User();
         u.setFirstName(session.getString("FIRST_NAME"));
         u.setId(session.getQualifiedUsername("JABBER_ID"));
         u.setLastName(session.getString("LAST_NAME"));
         u.setLocalId(session.getLong("USER_ID"));
         u.setOrganization(session.getString("ORGANIZATION"));
         return u;
     }
 
 }
