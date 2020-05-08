 package com.thoughtworks.twu.persistence;
 
 import com.thoughtworks.twu.domain.Offer;
import org.hibernate.SessionFactory;
import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.context.transaction.TransactionConfiguration;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertThat;
 
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"classpath:applicationContext.xml"})
 @TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
 @Transactional
 public class OfferDaoTest {
 
     @Autowired
     private OfferDao offerDao;
 
    @Autowired
    private SessionFactory sessionFactory;
 
     @Test
     public void shouldSaveAnimalOfferCorrectly(){
        String offerTitle = "My cat";
        String offerDescription = "Hey, feed my cat!";
        String offerCategory = "ANIMAL";
        String offerOwner = "Hermione";
 
        Offer offer = new Offer(offerTitle, offerDescription, offerCategory, offerOwner, new Date(), false);
        String generatedId = offerDao.saveOffer(offer);
 
        Offer offerFromDatabase = offerDao.getOfferById(generatedId);
 
        assertThat(offerFromDatabase, is(offer));
     }
 
     @Test
     public void shouldSaveHouseOffer(){
         String offerTitle = "House on England";
         String offerDescription = "Harrys house";
         String offerCategory = "HOUSE";
         String offerOwner = "Dumbledore";
 
         Offer offer = new Offer(offerTitle, offerDescription, offerCategory, offerOwner, new Date(), false);
         String generatedId = offerDao.saveOffer(offer);
 
         Offer offerFromDatabase = offerDao.getOfferById(generatedId);
 
         assertThat(offerFromDatabase, is(offer));
     }
 
     @Test
     public void shouldGetAllOffers(){
         String offerTitle = "House on England";
         String offerDescription = "Harrys house";
         String offerCategory = "HOUSE";
         String offerOwner = "Dumbledore";
 
         Offer offer = new Offer(offerTitle, offerDescription, offerCategory, offerOwner, new Date(), false);
 
         String secondOfferTitle = "My cat";
         String secondOfferDescription = "Hey, feed my cat!";
         String secondOfferCategory = "ANIMAL";
         String secondOfferOwner = "Hermoine";
 
         Offer secondOffer = new Offer(secondOfferTitle,secondOfferDescription,secondOfferCategory,secondOfferOwner, new Date(),false);
 
         offerDao.saveOffer(offer);
         offerDao.saveOffer(secondOffer);
 
         List<Offer> allOffers = offerDao.getAll();
         List<Offer> expectedOffers = new ArrayList<Offer>();
         expectedOffers.add(offer);
         expectedOffers.add(secondOffer);
         assertThat(expectedOffers.containsAll(allOffers), is(true));
         assertThat(allOffers.containsAll(expectedOffers), is(true));
 
     }
 
     @Test
     public void shouldTakeDownOffer() {
         String offerTitle = "House on England";
         String offerDescription = "Harrys house";
         String offerCategory = "HOUSE";
         String offerOwner = "Dumbledore";
 
         Offer offer = new Offer(offerTitle, offerDescription, offerCategory, offerOwner, new Date(), false);
 
         String offerId = offerDao.saveOffer(offer);
 
         offerDao.takeDown(offerId);
 
         Offer databaseOffer = offerDao.getOfferById(offerId);
         boolean isOfferHidden = databaseOffer.isHidden();
 
         assertThat(isOfferHidden, is(true));
 
     }
 
    @Before
    public void setUp(){
        sessionFactory.getCurrentSession().createQuery("delete from Offer").executeUpdate();
        sessionFactory.getCurrentSession().clear();
    }

 }
