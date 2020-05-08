 package com.in6k.mypal.dao;
 
 import com.in6k.mypal.domain.User;
 import com.in6k.mypal.util.HibernateUtil;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 import org.hibernate.criterion.Expression;
 
 import javax.swing.*;
 import java.util.ArrayList;
 import java.util.List;
 
 public class UserDao {
 
     public static void save(User user) {
         Session session = HibernateUtil.getSessionFactory().openSession();
         session.beginTransaction();
         session.save(user);
         session.getTransaction().commit();
 
         if (session != null && session.isOpen()) {
             session.close();
         }
     }
 
     public static User getById(Integer id) {
         Session session = null;
         User user = null;
 
         try {
             session = HibernateUtil.getSessionFactory().openSession();
             user = (User) session.get(User.class, id);
 
         } catch (Exception e) {
             JOptionPane.showMessageDialog(null, e.getMessage(), "Error 'findById'", JOptionPane.OK_OPTION);
 
         } finally {
             if (session != null && session.isOpen()) {
                 session.close();
             }
         }
         return user;
     }
 
     public static User getByEmail(String email) {
         Session session = HibernateUtil.getSessionFactory().openSession();
         session.beginTransaction();
         List userList = session.createCriteria(User.class).add(Expression.like("email", email)).list();
         User result = null;
         if (userList != null && userList.size() > 0) {
             result = (User) userList.get(0);
         }
         session.getTransaction().commit();
 
         return result;
     }
 
     public static double getBalance(User user) {
        double result = 0;
         Session session = HibernateUtil.getSessionFactory().openSession();
         session.beginTransaction();
 
         Query query = session.createSQLQuery("SELECT sum(sum), (SELECT sum(sum) FROM transactions WHERE debit_id=?) FROM transactions WHERE credit_id=?;");
         query.setInteger(0, user.getId());
         query.setInteger(1, user.getId());
 
         double creditSum = 0;
         double debitSum = 0;
         Object[] doubles = (Object[]) query.list().get(0);
         if (doubles[0] != null) {
             creditSum = (Double) doubles[0];
         }
         if (doubles[1] != null) {
             debitSum = (Double) doubles[1];
         }
 
         result = debitSum - creditSum;
         result *= 1000;
         result = Math.round(result);
         result /= 1000;
         session.getTransaction().commit();
         session.close();
 
         return result;
     }
 
     public static ArrayList<User> list() {
         ArrayList<User> result = new ArrayList<User>();
 
         Session session = HibernateUtil.getSessionFactory().openSession();
         Transaction tx = session.beginTransaction();
 
         result.addAll(session.createCriteria(User.class).list());
 
         tx.commit();
         session.close();
 
         return result;
     }
 }
 
