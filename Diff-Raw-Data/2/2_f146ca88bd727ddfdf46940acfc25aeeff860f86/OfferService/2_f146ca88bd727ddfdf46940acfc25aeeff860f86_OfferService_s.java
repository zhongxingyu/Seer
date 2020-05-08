 package com.thoughtworks.twu.service;
 
 import com.thoughtworks.twu.domain.Offer;
 import com.thoughtworks.twu.persistence.OfferDao;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.util.ArrayList;
 import java.util.List;
 
 @Service
 @Transactional
 public class OfferService implements OfferServiceInterface {
 
     @Autowired
     private OfferDao offerDao;
 
     @Override
     public Offer getOfferById(String offerId) {
         return offerDao.getOfferById(offerId);
     }
 
     @Override
     public String saveOffer(Offer offer) {
         return offerDao.saveOffer(offer);
     }
 
     @Override
     public List<Offer> getAll() {
         List<Offer> tempOrderedList = offerDao.getAll();
        List<Offer> reverseList = new ArrayList<>();
         for (int index = tempOrderedList.size()-1; index >= 0; index--) {
               reverseList.add(tempOrderedList.get(index));
         }
 
         return reverseList;
     }
 }
