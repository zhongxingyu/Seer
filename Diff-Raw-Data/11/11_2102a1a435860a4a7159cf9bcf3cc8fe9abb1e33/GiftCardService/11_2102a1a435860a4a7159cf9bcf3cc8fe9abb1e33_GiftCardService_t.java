 package is.idega.idegaweb.pheidippides.business;
 
 import is.idega.idegaweb.pheidippides.dao.PheidippidesDao;
 import is.idega.idegaweb.pheidippides.data.GiftCard;
 import is.idega.idegaweb.pheidippides.data.GiftCardUsage;
 import is.idega.idegaweb.pheidippides.data.RegistrationHeader;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Service;
 
 @Scope("singleton")
 @Service("giftCardService")
 public class GiftCardService {
 	@Autowired
 	private PheidippidesDao dao;
 
 	public GiftCardStatus getGiftCardStatus(String code) {
 		return getGiftCardStatus(dao.getGiftCard(code));
 	}
 
 	public GiftCardStatus getGiftCardStatus(GiftCard card) {
 		if (card == null) {
 			return null;
 		}
 		
 		if (card.getHeader().getStatus() == GiftCardHeaderStatus.Cancelled || card.getHeader().getStatus() == GiftCardHeaderStatus.UserDidntFinishPayment || card.getHeader().getStatus() == GiftCardHeaderStatus.WaitingForPayment) {
 			return new GiftCardStatus(false, 0, 0);
 		}
 		
 		return new GiftCardStatus(true, card.getAmount(), dao.getGiftCardUsageSum(card));
 	}
 	
 	public GiftCardUsage reserveGiftCard(String code, int amount, RegistrationHeader header) {
 		return reserveGiftCard(dao.getGiftCard(code), amount, header);
 	}
 	
 	public GiftCardUsage reserveGiftCard(GiftCard card, int amount, RegistrationHeader header) {
 		if (card == null || amount <= 0) {
 			System.out.println(amount);
 			return null;
 		}
 		
 		GiftCardStatus status = getGiftCardStatus(card);
 		if (!status.isValid()) {
 			System.out.println(status.isValid());
 			return null;
 		}
 		
		if ((status.getOriginalAmount() - status.getUsedAmount()) <= 0) {
 			System.out.println(status.getOriginalAmount() + " - " + status.getUsedAmount());
 			return null;
 		}
 		
 		//amount less then what's left on card
 		if ((status.getOriginalAmount() - status.getUsedAmount()) >= amount) {
 			return dao.storeGiftCardUsage(card, header, amount, GiftCardUsageStatus.Reservation);
 		} else {
 			return dao.storeGiftCardUsage(card, header, status.getOriginalAmount() - status.getUsedAmount(), GiftCardUsageStatus.Reservation);
 		}
 	}
 	
 	public boolean releaseGiftCardReservation(GiftCardUsage usage) {
 		return dao.removeGiftCardUsage(usage);
 	}
 	
 	public GiftCardUsage confirmGiftCardReservation(GiftCardUsage usage, RegistrationHeader header) {
 		return dao.updateGiftCardUsage(usage, header, GiftCardUsageStatus.Confirmed);
 	}
 }
