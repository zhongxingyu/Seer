 package com.mpower.service.impl;
 
 import java.lang.reflect.InvocationTargetException;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.Map;
 
 import javax.annotation.Resource;
 
 import org.apache.commons.beanutils.BeanUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.BeanWrapper;
 import org.springframework.beans.BeansException;
 import org.springframework.beans.PropertyAccessorFactory;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.mpower.dao.interfaces.GiftDao;
 import com.mpower.dao.interfaces.SiteDao;
 import com.mpower.domain.model.PaymentHistory;
 import com.mpower.domain.model.PaymentSource;
 import com.mpower.domain.model.Person;
 import com.mpower.domain.model.communication.Address;
 import com.mpower.domain.model.communication.Phone;
 import com.mpower.domain.model.customization.EntityDefault;
 import com.mpower.domain.model.paymentInfo.Commitment;
 import com.mpower.domain.model.paymentInfo.DistributionLine;
 import com.mpower.domain.model.paymentInfo.Gift;
 import com.mpower.integration.NewGift;
 import com.mpower.service.CommitmentService;
 import com.mpower.service.GiftService;
 import com.mpower.service.PaymentHistoryService;
 import com.mpower.type.EntityType;
 import com.mpower.type.GiftEntryType;
 import com.mpower.type.PaymentHistoryType;
 
 @Service("giftService")
 @Transactional(propagation = Propagation.REQUIRED)
 public class GiftServiceImpl extends AbstractPaymentService implements GiftService, ApplicationContextAware {
 
     /** Logger for this class and subclasses */
     protected final Log logger = LogFactory.getLog(getClass());
 
     @Resource(name = "paymentHistoryService")
     private PaymentHistoryService paymentHistoryService;
 
     @Resource(name = "commitmentService")
     private CommitmentService commitmentService;
 
     @Resource(name = "giftDAO")
     private GiftDao giftDao;
 
     @Resource(name = "siteDAO")
     private SiteDao siteDao;
 
     private ApplicationContext context;
 
     public void setApplicationContext(ApplicationContext applicationContext)
             throws BeansException {
         context = applicationContext;
     }
 
     @Override
     @Transactional(propagation = Propagation.REQUIRED)
     public Gift maintainGift(Gift gift) {
         if (logger.isDebugEnabled()) {
             logger.debug("maintainGift: gift = " + gift);
         }
         maintainEntityChildren(gift, gift.getPerson());
 
         routeGift(gift);
 
         setDefaultDates(gift);
         gift.filterValidDistributionLines();
         gift = giftDao.maintainGift(gift);
         paymentHistoryService.addPaymentHistory(createPaymentHistoryForGift(gift));
         
         auditService.auditObject(gift);
 
         return gift;
     }
     
     private void setDefaultDates(Gift gift) {
         if (gift.getId() == null) {
             Calendar transCal = Calendar.getInstance();
             gift.setTransactionDate(transCal.getTime());
             if (gift.getPostmarkDate() == null) {
                 Calendar postCal = new GregorianCalendar(transCal.get(Calendar.YEAR), transCal.get(Calendar.MONTH), transCal.get(Calendar.DAY_OF_MONTH));
                 gift.setPostmarkDate(postCal.getTime());
             }
         }
     }
 
     /*
      * this is needed for JMS
      */
     // @Resource(name = "creditGateway")
     // private MPowerCreditGateway creditGateway;
     @Override
     @Transactional(propagation = Propagation.REQUIRED)
     public Gift editGift(Gift gift) {
         if (logger.isDebugEnabled()) {
             logger.debug("editGift: giftId = " + gift.getId());
         }
         maintainEntityChildren(gift, gift.getPerson());
 
         gift = giftDao.maintainGift(gift);
 
         routeGift(gift);
         
         auditService.auditObject(gift);
 
         return gift;
     }
     
     private void routeGift(Gift gift) {
         try {
             NewGift newGift = (NewGift) context.getBean("newGift");
             newGift.routeGift(gift);
         } 
         catch (Exception ex) {
             logger.error(ex.getMessage(), ex);
         }
     }
 
 //    private void processMockTrans(Gift gift) {
      // this was a part of our JMS/MOM poc
      // creditGateway.sendGiftTransaction(gift);
 //    }
     
     private PaymentHistory createPaymentHistoryForGift(Gift gift) {
     	PaymentHistory paymentHistory = new PaymentHistory();
     	paymentHistory.setAmount(gift.getAmount());
     	paymentHistory.setCurrencyCode(gift.getCurrencyCode());
     	paymentHistory.setGift(gift);
     	paymentHistory.setPerson(gift.getPerson());
     	paymentHistory.setPaymentHistoryType(PaymentHistoryType.GIFT);
     	paymentHistory.setPaymentType(gift.getPaymentType());
     	paymentHistory.setTransactionDate(gift.getTransactionDate());
     	paymentHistory.setTransactionId("");
     	String desc = getGiftDescription(gift);
     	paymentHistory.setDescription(desc);
     	return paymentHistory;
     }
     
     private String getGiftDescription(Gift gift) {
     	
     	StringBuilder sb = new StringBuilder();
     	
     	if (PaymentSource.ACH.equals(gift.getPaymentType())) {
     	    sb.append("ACH Number: "+gift.getPaymentSource().getAchAccountNumberDisplay());
     	}
     	if (PaymentSource.CREDIT_CARD.equals(gift.getPaymentType())) {
     		sb.append("Credit Card Number: "+gift.getPaymentSource().getCreditCardType()+" "+gift.getPaymentSource().getCreditCardNumberDisplay());
     		sb.append(" ");
     		sb.append(gift.getPaymentSource().getCreditCardExpirationMonth());
     		sb.append(" / ");
     		sb.append(gift.getPaymentSource().getCreditCardExpirationYear());
     		sb.append(" ");
     		sb.append(gift.getPaymentSource().getCreditCardHolderName());
     	}
     	if (PaymentSource.CHECK.equals(gift.getPaymentType())) {
     		sb.append("\nCheck Number: ");
     		sb.append(gift.getCheckNumber());
     	}
     	Address address = gift.getAddress();
     	if (address != null) {
         	sb.append("\nAddress: ");
         	String state = StringUtils.trimToEmpty(address.getStateProvince());
     		sb.append(StringUtils.trimToEmpty(address.getAddressLine1()) 
     				+ " " + StringUtils.trimToEmpty(address.getAddressLine2()) 
     				+ " " + StringUtils.trimToEmpty(address.getAddressLine3()) 
     				+ " " + StringUtils.trimToEmpty(address.getCity()) 
     				+ (state.length() == 0  ? "" : (", " + state))
     				+ " " + StringUtils.trimToEmpty(address.getCountry()) 
     				+ " " + StringUtils.trimToEmpty(address.getPostalCode())
     				);
     	}
     	Phone phone = gift.getPhone();
     	if (phone != null) {
         	sb.append("\nPhone: ");
     		sb.append(StringUtils.trimToEmpty(phone.getNumber()));
     	}
     	    	
     	return sb.toString();
     	
     }
 
     @Override
     public Gift readGiftById(Long giftId) {
         if (logger.isDebugEnabled()) {
             logger.debug("readGiftById: giftId = " + giftId);
         }
 //        return normalize(giftDao.readGiftById(giftId));
         return giftDao.readGiftById(giftId);
     }
 
     @Override
     public Gift readGiftByIdCreateIfNull(String giftId, String commitmentId, Person constituent) {
         if (logger.isDebugEnabled()) {
             logger.debug("readGiftByIdCreateIfNull: giftId = " + giftId + " commitmentId = " + commitmentId + " constituentId = " + (constituent == null ? null : constituent.getId()));
         }
         Gift gift = null;
         if (giftId == null) {
             Commitment commitment = null;
             if (commitmentId != null) {
                 commitment = commitmentService.readCommitmentById(Long.valueOf(commitmentId));
                 if (commitment == null) {
                     logger.error("readGiftByIdCreateIfNull: commitment not found for commitmentId = " + commitmentId);
                     return gift;
                 }
                 gift = this.createGift(commitment, GiftEntryType.MANUAL);
                 gift.setPerson(commitment.getPerson());
             }
             if (gift == null) {
                 if (constituent != null) {
                     gift = this.createDefaultGift(constituent);
                     gift.setPerson(constituent);
                 }
             }
         }
         else {
             gift = this.readGiftById(Long.valueOf(giftId));
         }
         return gift;
     }
 
     // only needed for gifts not entered by the program and entered via sql.
 //    private Gift normalize(Gift gift) {
 //        if (gift.getAddress() == null) {
 //            gift.setAddress(new Address(gift.getPerson().getId()));
 //        }
 //        if (gift.getPhone() == null) {
 //            gift.setPhone(new Phone(gift.getPerson().getId()));
 //        }
 //        if (gift.getEmail() == null) {
 //            gift.setEmail(new Email(gift.getPerson().getId()));
 //        }
 //        if (gift.getPaymentSource() == null) {
 //            gift.setPaymentSource(new PaymentSource(gift.getPerson()));
 //        }
 //        gift.getPaymentSource().setPerson(gift.getPerson());
 //        return gift;
 //    }
 
     @Override
     public List<Gift> readGifts(Person constituent) {
         return readGifts(constituent.getId());
     }
 
     @Override
     public List<Gift> readGifts(Long constituentId) {
         if (logger.isDebugEnabled()) {
             logger.debug("readGifts: constituentId = " + constituentId);
         }
         return giftDao.readGiftsByConstituentId(constituentId);
     }
 
     @Override
     public List<Gift> readGifts(Map<String, Object> params) {
         if (logger.isDebugEnabled()) {
             logger.debug("readGifts: params = " + params);
         }
 //        return giftDao.readGifts(params);//TODO fix for Search
         return null;
     }
 
     @Override
     public Gift createDefaultGift(Person constituent) {
         if (logger.isDebugEnabled()) {
             logger.debug("createDefaultGift: constituent = " + (constituent == null ? null : constituent.getId()));
         }
         // get initial gift with built-in defaults
         Gift gift = new Gift();
         BeanWrapper giftBeanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(gift);
 
         List<EntityDefault> entityDefaults = siteDao.readEntityDefaults(Arrays.asList(new EntityType[] { EntityType.gift }));
         for (EntityDefault ed : entityDefaults) {
             giftBeanWrapper.setPropertyValue(ed.getEntityFieldName(), ed.getDefaultValue());
         }
 
         List<DistributionLine> lines = new ArrayList<DistributionLine>(1);
         DistributionLine line = new DistributionLine();
         line.setGiftId(gift.getId());
         lines.add(line);
         gift.setDistributionLines(lines);
 
         // TODO: consider caching techniques for the default Gift
         return gift;
     }
 
     @Override
     public Gift createGift(Commitment commitment, GiftEntryType giftEntryType) {
         if (logger.isDebugEnabled()) {
             logger.debug("createGift: commitment = " + commitment + " giftEntryType = " + giftEntryType);
         }
         Gift gift = new Gift();
         gift.setPerson(commitment.getPerson());
         gift.setCommitmentId(commitment.getId());
         gift.setComments(commitment.getComments());
         gift.setAmount(commitment.getAmountPerGift());
         gift.setPaymentType(commitment.getPaymentType());
         gift.setPaymentSource(commitment.getPaymentSource());
         gift.setEntryType(giftEntryType);
         if (commitment.getDistributionLines() != null) {
             List<DistributionLine> list = new ArrayList<DistributionLine>();
             for (DistributionLine oldLine : commitment.getDistributionLines()) {
                 boolean isGift = true;
                 DistributionLine newLine = new DistributionLine(oldLine, isGift, gift.getId());
                 list.add(newLine);
             }
             gift.setDistributionLines(list);
         }
         gift.setAddress(commitment.getAddress());
         gift.setPhone(commitment.getPhone());
         return gift;
     }
 
     @Override
     public double analyzeMajorDonor(Long constituentId, Date beginDate, Date currentDate) {
         if (logger.isDebugEnabled()) {
             logger.debug("analyzeMajorDonor: constituentId = " + constituentId + " beginDate = " + beginDate + " currentDate = " + currentDate);
         }
         return giftDao.analyzeMajorDonor(constituentId, beginDate, currentDate);
     }
 
     @Override
     @Transactional(propagation = Propagation.REQUIRED)
     public Gift refundGift(Long giftId) {
         if (logger.isDebugEnabled()) {
             logger.debug("refundGift: giftId = " + giftId);
         }
         Gift originalGift = readGiftById(giftId);
         try {
             Gift refundGift = (Gift) BeanUtils.cloneBean(originalGift);
             refundGift.resetIdToNull();
             refundGift.setTransactionDate(null);
             refundGift.getPaymentSource().setCreditCardExpiration(null);
             refundGift.setAmount(originalGift.getAmount().negate());
             refundGift.setOriginalGiftId(originalGift.getId());
             refundGift = maintainGift(refundGift);
             
             originalGift.filterValidDistributionLines();
             List<DistributionLine> lines = originalGift.getDistributionLines();
             List<DistributionLine> refundLines = new ArrayList<DistributionLine>();
             for (DistributionLine line : lines) {
                 BigDecimal negativeAmount = line.getAmount() == null ? null : line.getAmount().negate();
                 DistributionLine newLine = new DistributionLine(negativeAmount, line.getPercentage(), line.getProjectCode(), line.getMotivationCode(), line.getOther_motivationCode());
                 newLine.setGiftId(refundGift.getId());
                 refundLines.add(newLine);
             }
             refundGift.setDistributionLines(refundLines);
             
             originalGift.setRefundGiftId(refundGift.getId());
             originalGift.setRefundGiftTransactionDate(refundGift.getTransactionDate());
             maintainGift(originalGift);
             auditService.auditObject(refundGift);
             return refundGift;
         } catch (IllegalAccessException e) {
             throw new IllegalStateException();
         } catch (InstantiationException e) {
             throw new IllegalStateException();
         } catch (InvocationTargetException e) {
             throw new IllegalStateException();
         } catch (NoSuchMethodException e) {
             throw new IllegalStateException();
         }
     }
 
     @Override
     @Transactional(propagation = Propagation.REQUIRED)
     public List<Gift> readGiftsByConstituentId(Long constituentId) {
         if (logger.isDebugEnabled()) {
             logger.debug("readGiftsByConstituentId: constituentId = " + constituentId);
         }
         return giftDao.readGiftsByConstituentId(constituentId);
     }
 
 //    @Override
 //    @Transactional(propagation = Propagation.REQUIRED)
     // THIS METHOD IS NOT USED ANYWHERE TODO: remove?
 //    public List<Gift> readAllGifts() {
 //        return giftDao.readAllGifts(); 
 //    }
 
     // THIS METHOD IS NOT USED ANYWHERE TODO: remove?
     @Override
     public List<Gift> readGiftsByCommitment(Commitment commitment) {
         if (logger.isDebugEnabled()) {
             logger.debug("readGiftsByCommitment: commitment = " + commitment);
         }
         return giftDao.readGiftsByCommitmentId(commitment.getId());
     }
 
 	@Override
 	public List<Gift> readAllGiftsBySiteName() {
         if (logger.isDebugEnabled()) {
             logger.debug("readAllGiftsBySiteName:");
         }
         return giftDao.readAllGiftsBySite();
 	}
 }
