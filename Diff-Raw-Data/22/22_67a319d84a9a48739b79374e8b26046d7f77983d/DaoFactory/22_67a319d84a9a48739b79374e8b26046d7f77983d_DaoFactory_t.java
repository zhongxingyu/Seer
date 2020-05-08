 package com.ambantis.magic.dao;
 
 import com.ambantis.magic.exception.DaoConfigurationException;
 import com.ambantis.magic.exception.DaoConnectionException;
 
 import java.sql.Connection;
 
 /**
  * User: Alexandros Bantis
  * Date: 6/15/13
  * Time: 3:02 PM
  */
 public abstract class DaoFactory {
 
     private static DaoFactory daoFactory;
 
 
     public static DaoFactory getInstance() {
         if (daoFactory == null)
             daoFactory = DaoFactoryMock.getInstance();
         return daoFactory;
     }
 
     public static void init() throws DaoConfigurationException, DaoConnectionException {
        DaoFactoryMock.init();
     }
 
     abstract Connection getConnection() throws DaoConfigurationException, DaoConnectionException;
 
     public AssignmentDao getBugDao() {
         return new AssignmentDaoMock();
     }
 
     public CommentDao getUserDao() {
         return new CommentDaoMock();
     }
 
     public DocumentDao getDocumentDao() {
         return new DocumentDaoMock();
     }
 
     public PeriodDao getPeriodDao() {
         return new PeriodDaoMock();
     }
 
     public RevisionDao getRevisionDao() {
         return new RevisionDaoMock();
     }
 
     public RollBookDao getRollBookDao() {
         return new RollBookDaoMock();
     }
 
     public StudentDao getStudentDao() {
         return new StudentDaoMock();
     }
 }
