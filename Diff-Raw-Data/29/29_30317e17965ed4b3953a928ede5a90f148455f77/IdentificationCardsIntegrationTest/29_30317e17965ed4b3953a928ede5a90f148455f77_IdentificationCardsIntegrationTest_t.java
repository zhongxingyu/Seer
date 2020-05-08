 package com.orgsync.api.integration;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.List;
import java.util.UUID;
 
 import org.junit.AfterClass;
 import org.junit.Test;
 
 import com.orgsync.api.IdentificationCardsResource;
 import com.orgsync.api.Resources;
 import com.orgsync.api.model.identification_cards.IdentificationCard;
 import com.typesafe.config.Config;
 
 public class IdentificationCardsIntegrationTest extends BaseIntegrationTest<IdentificationCardsResource> {
 
     private static final List<? extends Config> cardsConfig = DbTemplate.getList("identification_cards");
     private static final Config firstCardConfig = cardsConfig.get(0);
 
     public IdentificationCardsIntegrationTest() {
         super(Resources.IDENTIFICATIONS_CARDS);
     }
 
     @AfterClass
     public static void cleanup() {
         BaseIntegrationTest.cleanup(Resources.IDENTIFICATIONS_CARDS);
     }
 
     @Test
     public void testGet() throws Exception {
         List<IdentificationCard> result = getResult(getResource().getIdentificationCards());
 
         testContainsIds(result, cardsConfig);
     }
 
     @Test
     public void testGetIdentificationCard() throws Exception {
         IdentificationCard card = getResult(getResource().getIdentificationCard(firstCardConfig.getInt("id")));
 
         assertEquals(firstCardConfig.getString("number"), card.getNumber());
         assertEquals(firstCardConfig.getInt("id"), card.getId());
         assertEquals(201, card.getAccountId());
     }
 
     @Test
     public void testGetCardByNumber() throws Exception {
         String number = firstCardConfig.getString("number");
 
         IdentificationCard card = getResult(getResource().getIdentificationCardByCardNumber(number));
 
         assertEquals(firstCardConfig.getInt("id"), card.getId());
     }
 
     @Test
     public void testGetCardByAccount() throws Exception {
         int accountId = 201;
 
         IdentificationCard card = getResult(getResource().getIdentificationCardByAccount(accountId));
 
         assertEquals(firstCardConfig.getInt("id"), card.getId());
     }
 
     @Test
     public void testAddAndRemove() throws Exception {
        Config userConfig = DbTemplate.getList("users").get(0);
        int userId = userConfig.getInt("id");
        String cardNumber = UUID.randomUUID().toString();
        IdentificationCard card = getResult(getResource().addIdentificationCardToAccount(userId, cardNumber));
 
        assertEquals(userId, card.getAccountId());
         assertEquals(cardNumber, card.getNumber());
 
        getResource().removeIdentificationCardFromAccount(userId);
     }
 
 }
