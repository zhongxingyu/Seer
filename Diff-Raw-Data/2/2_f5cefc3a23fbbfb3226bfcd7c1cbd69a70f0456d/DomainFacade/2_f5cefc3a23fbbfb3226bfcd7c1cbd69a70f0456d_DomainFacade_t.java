 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package server.domain;
 
 import contract.domain.*;
 import java.util.*;
 import org.hibernate.*;
 import org.hibernate.criterion.Restrictions;
 import server.domain.classes.*;
 import server.utils.HibernateUtil;
 
 /**
  *
  * @author Markus Mohanty <markus.mo at gmx.net>
  */
 public class DomainFacade
 {
     private static DomainFacade instance;
     private static Session session;
 
     private DomainFacade()
     {
         session = HibernateUtil.getSessionFactory().openSession();
     }
 
     public static DomainFacade getInstance()
     {
         if (instance == null)
         {
             instance = new DomainFacade();
         }
         return instance;
     }
 
     /**
      * returns all competitions in a time span
      *
      * @param From start of time span
      * @param to end of time span
      * @return all competitions between a timespan given
      */
     public ArrayList<Competition> getCompetitionsByDate(Date From, Date to)
     {
         session.beginTransaction();
         Query query = session.createQuery("From Competition where dateFrom >= :From and dateTo <= :to");
         query.setParameter("From", From);
         query.setParameter("to", to);
         return (ArrayList<Competition>) query.list();
     }
 
     public Member getMemberByUsername(String username) throws CouldNotFetchException
     {
         try
         {
             session.beginTransaction();
            Query q = session.createQuery("From ClubMember where Username = :Username");
             q.setParameter("Username", username);
             return (Member) q.uniqueResult();
         }
         catch (HibernateException ex)
         {
             throw new CouldNotFetchException(ex.getMessage());
         }
     }
 
     /**
      * returns a the department of a type of sport
      *
      * @param sport the sport the department belongs to
      * @return the department of the sport
      */
     public Department getDepartmentsBySport(TypeOfSport sport)
     {
         session.beginTransaction();
         Query q = session.createQuery("From Department where typeOfSports = :sport");
         q.setParameter("sport", sport);
         return (Department) q.uniqueResult();
     }
 
     /**
      * returns all matches of a competition
      *
      * @param competition the competition the matches are in
      * @return all matches of the competition given
      */
     public ArrayList<Match> getMatchesByCompetition(Competition competition)
     {
         session.beginTransaction();
         Query query = session.createQuery("From Match where competition = :competition");
         query.setParameter("competition", competition);
         return (ArrayList<Match>) query.list();
     }
 
     /**
      * returns a member with the firstname and lastname
      *
      * @param firstname the firstname of the member
      * @param lastname the lastname of the member
      * @return a member with the firstname and lastname given
      */
     public Member getMemberByName(String firstname, String lastname)
     {
         session.beginTransaction();
         Query query = session.createQuery("From Member1 where prename = :firstname and lastname = :lastname");
         return (Member) query.uniqueResult();
     }
 
     /**
      * returns a object out of the database by its class with a specific name
      *
      * @param <T> the type of class
      * @param clazz the class
      * @param name name of the object
      * @return a object with a name given
      */
     public <T> T getByName(Class<T> clazz, String name)
     {
         session.beginTransaction();
         return (T) session.createCriteria(clazz).add(Restrictions.eq("name", name)).uniqueResult();
     }
 
     public <T> T getByID(Class<T> clazz, Integer id) throws CouldNotFetchException
     {
         try
         {
             session.beginTransaction();
             return (T) session.createCriteria(clazz).add(Restrictions.eq("id", id)).uniqueResult();
         }
         catch (HibernateException ex)
         {
             throw new CouldNotFetchException(ex.getMessage());
         }
     }
 
     /**
      * Saves a domain object
      *
      * @param <T> the class to be saved
      * @param expected the class instance to be saved
      * @return the id of the saved object
      * @throws CouldNotSaveException
      */
     public <T extends IDomain> Integer set(T expected)
             throws CouldNotSaveException
     {
         try
         {
             Transaction t = session.beginTransaction();
             session.saveOrUpdate(expected);
             t.commit();
 
             return expected.getId();
         }
         catch (HibernateException ex)
         {
             throw new CouldNotSaveException(ex);
         }
     }
 
     /**
      * deletes a line in the database
      *
      * @param <T> the table to be deleted From
      * @param expected the object to be deleted
      * @throws CouldNotDeleteException
      */
     public <T> void delete(T expected)
             throws CouldNotDeleteException
     {
         try
         {
             Transaction t = session.beginTransaction();
             session.delete(expected);
             t.commit();
         }
         catch (HibernateException ex)
         {
             throw new CouldNotDeleteException(ex);
         }
     }
 
     /**
      * gets all entries of a table
      *
      * @param <T> the class of the table
      * @param clazz the class instance of the table, i.e. Table.class
      * @return a list of entries
      */
     public <T extends IDomain> List<T> getAll(Class<T> clazz) throws CouldNotFetchException
     {
         try
         {
             session.beginTransaction();
             return (List<T>) session.createCriteria(clazz).list();
         }
         catch (HibernateException ex)
         {
             throw new CouldNotFetchException(ex.getMessage());
         }
     }
 
     public League getLeageByNameAndTypeOfSport(TypeOfSport t, String leaguename) throws CouldNotFetchException
     {
         try
         {
             session.beginTransaction();
             Query q = session.createQuery("From League where sport = :t and name = :leaguename");
             q.setParameter("t", t);
             q.setParameter("leaguename", leaguename);
             return (League) q.uniqueResult();
         }
         catch (HibernateException ex)
         {
             throw new CouldNotFetchException(ex.getMessage());
         }
     }
     
     public List<ClubTeam> getClubTeamsByTypeOfSport(ITypeOfSport sport) throws CouldNotFetchException{
         try {
             session.beginTransaction();
             Query q = session.createQuery("From ClubTeam where League.typeOfSport =:sport");
             q.setParameter("sport", sport);
             return q.list();
         } 
         catch (HibernateException e) 
         {
             throw new CouldNotFetchException(e.getMessage());
         }
     }
 }
