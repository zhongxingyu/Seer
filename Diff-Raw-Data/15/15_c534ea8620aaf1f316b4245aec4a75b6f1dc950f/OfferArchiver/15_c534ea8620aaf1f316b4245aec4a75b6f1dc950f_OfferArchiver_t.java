 package com.zibea.parser.core.workers;
 
 import com.zibea.parser.core.exception.BatchException;
 import com.zibea.parser.dao.RealtyDao;
 import com.zibea.parser.model.domain.Offer;
 
 import java.sql.*;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.concurrent.atomic.AtomicLong;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReadWriteLock;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 
 /**
  * @author: Mikhail Bragin
  */
 public class OfferArchiver implements Runnable {
 
     private Set<Offer> batch;
     private Set<Long> savedOffers;
 
     private AtomicLong offersSaved = new AtomicLong();
 
     private RealtyDao dao;
 
     ReadWriteLock readWriteLock = new ReentrantReadWriteLock(true);
     Lock r = readWriteLock.readLock();
     Lock w = readWriteLock.writeLock();
 
 
     public OfferArchiver(RealtyDao dao) {
         this.dao = dao;
         batch = Collections.synchronizedSet(new HashSet<Offer>());
         savedOffers = Collections.synchronizedSet(new HashSet<Long>());
     }
 
     @Override
     public void run() {
         try {
             w.lock();
             flush();
         } finally {
             w.unlock();
         }
     }
 
     private boolean saveOffer(Offer offer) {
         Set<Offer> offers = new HashSet<>();
         offers.add(offer);
 
         try {
             dao.saveBatch(offers);
         } catch (SQLException e) {
             return false;
         }
 
         return true;
     }
 
     private void flush() {
         try {
             if (!batch.isEmpty()) {
                 savedOffers.addAll(dao.saveBatch(batch));
                 offersSaved.getAndSet(offersSaved.get() + batch.size());
                 batch.clear();
             }
         } catch (SQLException e) {
 
             if (e.getMessage().contains("Violation of PRIMARY KEY constraint")) {
                 System.out.println("Constraint violation");
 
                 String stringId = e.getMessage().split("\\(")[1].split("\\)")[0];
                 long duplicateId = Long.parseLong(stringId);
                 batch.remove(new Offer(duplicateId));
 
                 for (Offer offer : batch) {
                     if (saveOffer(offer)) {
                         savedOffers.add(offer.getId());
                         offersSaved.incrementAndGet();
                     }
                 }
 
                 batch.clear();
             } else {
                 e.printStackTrace();
             }
 
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     public void addToBatch(Offer offer) {
         try {
             r.lock();
             batch.add(offer);
         } finally {
             r.unlock();
         }
     }
 
     public AtomicLong getOffersSaved() {
         return offersSaved;
     }
 }
