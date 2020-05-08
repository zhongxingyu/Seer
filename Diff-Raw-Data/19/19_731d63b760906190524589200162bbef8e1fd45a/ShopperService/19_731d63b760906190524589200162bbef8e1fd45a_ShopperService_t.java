 package com.sk.service;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import com.sk.domain.CreditCard;
 import com.sk.domain.Shopper;
 import com.sk.domain.dao.ShopperDao;
 import com.sk.service.encryption.EncryptionService;
 
 @Service
 public class ShopperService {
 
 	private EncryptionService encryptionService;
 	private ShopperDao shopperDao;
 
	public ShopperService(){}
	
 	@Autowired
 	public ShopperService(EncryptionService encryptionService, ShopperDao shopperDao) {
 		this.encryptionService = encryptionService;
 		this.shopperDao = shopperDao;
 	}
 
 	public void encryptAndsaveCardInfo(Shopper shopper, CreditCard card) {
 		
 		CreditCard encryptedCard = new CreditCard();
 		encryptedCard.setOwner(encryptionService.encrypt(card.getOwner()));
 		encryptedCard.setCardNumber(encryptionService.encrypt(card.getCardNumber()));
 		encryptedCard.setCvc(encryptionService.encrypt(card.getCvc()));
 		encryptedCard.setMonth(encryptionService.encrypt(card.getMonth()));
 		encryptedCard.setYear(encryptionService.encrypt(card.getYear()));
 		encryptedCard.setCreditCardType(card.getCreditCardType());
 		
 		shopper.addCreditCard(encryptedCard);
 		shopperDao.persist(shopper);
 	}
 
 	public CreditCard decryptCreditCardInfo(CreditCard encryptedCard) {
 		CreditCard card = new CreditCard();
 		card.setOwner(encryptionService.decrypt(encryptedCard.getOwner()));
 		card.setCardNumber(encryptionService.decrypt(encryptedCard.getCardNumber()));
 		card.setCvc(encryptionService.decrypt(encryptedCard.getCvc()));
 		card.setMonth(encryptionService.decrypt(encryptedCard.getMonth()));
 		card.setYear(encryptionService.decrypt(encryptedCard.getYear()));
 		card.setCreditCardType(encryptedCard.getCreditCardType());
 		return card;
 	}
 	
 	public Shopper getStubShopper() {
 		Shopper stubShopper = shopperDao.findByEmail("default@default.com");
 		if(stubShopper == null){
 			Shopper shopper = new Shopper();
 			shopper.setEmail("default@default.com");
 			shopper.setName("Default Shopper");
 			stubShopper = shopperDao.persist(shopper);
 		}
 		return stubShopper;
 	}
 
 }
