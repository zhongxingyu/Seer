 package com.sk.service;
 
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import com.sk.domain.CreditCard;
 import com.sk.domain.CreditCardPaymentMethod;
 import com.sk.domain.Shopper;
 import com.sk.domain.dao.ShopperDao;
 import com.sk.service.encryption.EncryptionService;
 
 @Service
 public class ShopperService {
 
 	private EncryptionService encryptionService;
 	private ShopperDao shopperDao;
 
 	public ShopperService() {
 	}
 
 	@Autowired
 	public ShopperService(EncryptionService encryptionService, ShopperDao shopperDao) {
 		this.encryptionService = encryptionService;
 		this.shopperDao = shopperDao;
 	}
 
 	public void encryptAndsaveCardInfo(Shopper shopper, CreditCard card) {
 
 		CreditCard encryptedCard = encryptCreditCardInfo(card);
 
 		shopper.addCreditCard(encryptedCard);
 		shopperDao.persist(shopper);
 	}
 
 	public void encryptCreditCardInfo(CreditCardPaymentMethod payment) {
 		payment.setCreditCard(encryptCreditCardInfo(payment.getCreditCard()));
 	}
 
 	public CreditCard encryptCreditCardInfo(CreditCard card) {
 		CreditCard encryptedCard = new CreditCard();
 		encryptedCard.setId(card.getId());
 		encryptedCard.setOwner(encryptionService.encrypt(card.getOwner()));
 		encryptedCard.setCardNumber(encryptionService.encrypt(card.getCardNumber()));
 		encryptedCard.setCvc(encryptionService.encrypt(card.getCvc()));
 		encryptedCard.setMonth(encryptionService.encrypt(card.getMonth()));
 		encryptedCard.setYear(encryptionService.encrypt(card.getYear()));
 		encryptedCard.setCreditCardType(card.getCreditCardType());
 		return encryptedCard;
 	}
 
 	public CreditCard decryptCreditCardInfo(CreditCard encryptedCard) {
 		CreditCard card = new CreditCard();
 		card.setId(encryptedCard.getId());
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
 		if (stubShopper == null) {
 			Shopper shopper = new Shopper();
 			shopper.setEmail("default@default.com");
 			shopper.setName("Default Shopper");
 			stubShopper = shopperDao.persist(shopper);
 		}
 		return stubShopper;
 	}
 
 	public List<Shopper> getAllShoppers() {
 		return shopperDao.getAll();
 	}
 
 }
