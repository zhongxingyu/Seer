 package com.in6k.dao;
 
 import com.in6k.entity.User;
 import com.in6k.hibernate.HibernateUtil;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 
 import javax.swing.*;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 public class UserDao {
 
     public static void save(User user) {
         Transaction tx = null;
         Session session = HibernateUtil.getInstance().openSession();
         tx = session.beginTransaction();
         session.save(user);
         tx.commit();
     }
 
     public static List getAll() throws SQLException {
         Session session = null;
         List  list = new ArrayList();
         try {
             session = HibernateUtil.getInstance().openSession();
             list = session.createCriteria(User.class).list();
         } catch (Exception e) {
             JOptionPane.showMessageDialog(null, e.getMessage());
         } finally {
             if (session != null && session.isOpen()) {
                 session.close();
             }
         }
         return list;
     }
 
    public static User getTransactionById(int id) throws SQLException {
        Session session = null;
        User user = null;
        try {
            session = HibernateUtil.getInstance().openSession();
            user = (User) session.load(User.class, id);
        } catch (Exception e) {
            e.getMessage();
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return user;
    }

 }
