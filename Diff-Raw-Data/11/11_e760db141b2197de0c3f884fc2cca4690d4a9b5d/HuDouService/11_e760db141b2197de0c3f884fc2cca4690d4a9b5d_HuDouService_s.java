 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.hudoumiao.service;
 
 import com.hudoumiao.entity.Book;
 import com.hudoumiao.entity.BookCollection;
 import com.hudoumiao.entity.Customer;
 import javax.ejb.EJB;
 import javax.ejb.Stateless;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 
 /**
  *
  * @author HUXIAOFENG
  */
 @Stateless
 public class HuDouService {
 
     @EJB
     private DaoBean daoBean;
     @EJB
     private DaoTxBean daoTxBean;
     @PersistenceContext(unitName = "HuDouMiaoPU")
     private EntityManager em;
     //
     private CacheManger cacheMgr = CacheManger.getInstance();
 
     public Book findBook(Long bookId, boolean create) {
         Book book = cacheMgr.getBook(bookId);
         if (book != null) {
             return book;
         }
         //
         book = daoBean.findBook(bookId);
         if (book == null) {
             if (create) {
                 return daoTxBean.createBook();
             } else {
                 return null;
             }
         }
         book.setCollectionCount(daoBean.findCollectionTotolCount(book));
         book.setScore1Count(daoBean.findCollectionScoreCount(book, 1));
         book.setScore2Count(daoBean.findCollectionScoreCount(book, 2));
         book.setScore3Count(daoBean.findCollectionScoreCount(book, 3));
         book.setScore4Count(daoBean.findCollectionScoreCount(book, 4));
         book.setScore5Count(daoBean.findCollectionScoreCount(book, 5));
         book.setRecentCollectionList(daoBean.findRecentCollections(book));
         book.setHotTagList(daoBean.findHotTags(book));
         book.setRelatedBooks(daoBean.findRelatedBooks(book));
         //
         cacheMgr.setBook(book);
         return book;
     }
 
     public Customer findCustomer(String name, boolean create) {
         Customer customer = daoBean.findCustomer(name);
         if (customer == null && create) {
             customer = daoTxBean.createCustomer(name);
         }
         return customer;
     }
 
     public Customer findCustomer(Long customerId) {
         Customer customer = cacheMgr.getCustomer(customerId);
         if (customer != null) {
             return customer;
         }
         customer = daoBean.findCustomer(customerId);
         cacheMgr.setCustomer(customer);
         return customer;
     }
 
     public BookCollection findBookCollection(Book book, Customer customer) {
         BookCollection collection = cacheMgr.getBookCollection(book, customer);
         if (collection != null) {
            return collection;
        }
        Long id = collection.getId();
        if (id == null || id < 1) {
            return null;
         }
         //
         collection = daoBean.findCollection(book, customer);
         if (collection == null) {
             collection = new BookCollection();
         } else {
             collection.setTagNames(daoBean.findTagNamesByBookCollection(collection));
         }
         cacheMgr.setBookCollection(book, customer, collection);
         return collection;
     }
 
     public void saveCollection(Long bookId, Long customerId, int score, String comment, String tags) {
         Customer customer = daoBean.findCustomer(customerId);
         if (customer == null) {
             return;
         }
         Book book = daoBean.findBook(bookId);
         if (book == null) {
             return;
         }
         daoBean.saveCollection(book, customer, score, comment, tags);
         //
         cacheMgr.deleteBook(book);
         cacheMgr.deleteCustomer(customer);
         cacheMgr.deleteBookCollection(book, customer);
     }
 
     public void removeCollection(Long bookId, Long customerId) {
         Customer customer = daoBean.findCustomer(customerId);
         if (customer == null) {
             return;
         }
         Book book = daoBean.findBook(bookId);
         if (book == null) {
             return;
         }
         BookCollection bookCollection = daoBean.findCollection(book, customer);
         if (bookCollection == null) {
             return;
         }
         daoBean.removeCollectionTags(bookCollection);
         em.remove(bookCollection);
         //
         cacheMgr.deleteBook(book);
         cacheMgr.deleteCustomer(customer);
         cacheMgr.deleteBookCollection(book, customer);
     }
 }
