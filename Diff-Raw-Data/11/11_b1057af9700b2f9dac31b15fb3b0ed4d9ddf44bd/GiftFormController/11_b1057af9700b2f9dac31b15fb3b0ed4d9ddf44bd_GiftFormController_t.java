 package com.orangeleap.tangerine.controller.gift;
 
 import java.util.List;
 import java.util.Map;
 
 import javax.annotation.Resource;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.lang.math.NumberUtils;
 import org.apache.commons.logging.Log;
 import org.springframework.validation.BindException;
 import org.springframework.validation.Errors;
 import org.springframework.web.bind.ServletRequestDataBinder;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.orangeleap.tangerine.domain.AbstractEntity;
 import com.orangeleap.tangerine.domain.paymentInfo.Commitment;
 import com.orangeleap.tangerine.domain.paymentInfo.Gift;
 import com.orangeleap.tangerine.domain.paymentInfo.Pledge;
 import com.orangeleap.tangerine.domain.paymentInfo.RecurringGift;
 import com.orangeleap.tangerine.service.PledgeService;
 import com.orangeleap.tangerine.service.RecurringGiftService;
import com.orangeleap.tangerine.type.PaymentType;
 import com.orangeleap.tangerine.util.OLLogger;
 import com.orangeleap.tangerine.util.StringConstants;
 
 public class GiftFormController extends AbstractGiftController {
 
     /** Logger for this class and subclasses */
     protected final Log logger = OLLogger.getLog(getClass());
 
     @Resource(name = "pledgeService")
     protected PledgeService pledgeService;
 
     @Resource(name = "recurringGiftService")
     protected RecurringGiftService recurringGiftService;
 
     @Override
     protected AbstractEntity findEntity(HttpServletRequest request) {
         return giftService.readGiftByIdCreateIfNull(getConstituent(request), request.getParameter(StringConstants.GIFT_ID));
     }
     
     private boolean isEnteredGift(Gift gift) {
     	return gift != null && gift.getId() != null && gift.getId() > 0;
     }
     
     private boolean canReprocessGift(Gift gift) {
     	return isEnteredGift(gift) && (Gift.STATUS_NOT_PAID.equals(gift.getGiftStatus()) || Commitment.STATUS_PENDING.equals(gift.getGiftStatus())) && 
 			(Gift.PAY_STATUS_DECLINED.equals(gift.getPaymentStatus()) || Gift.PAY_STATUS_ERROR.equals(gift.getPaymentStatus()));
     }
    
    private boolean showGiftView(Gift gift) {
    	return isEnteredGift(gift) && (PaymentType.CASH.getPaymentName().equals(gift.getPaymentType()) || PaymentType.CHECK.getPaymentName().equals(gift.getPaymentType()) || 
    			((PaymentType.ACH.getPaymentName().equals(gift.getPaymentType()) || PaymentType.CREDIT_CARD.getPaymentName().equals(gift.getPaymentType())) && 
    					Gift.STATUS_PAID.equals(gift.getGiftStatus()) && Gift.PAY_STATUS_APPROVED.equals(gift.getPaymentStatus())));
    }
 
 	@Override
 	protected ModelAndView showForm(HttpServletRequest request, HttpServletResponse response, BindException errors) throws Exception {
 		ModelAndView mav = super.showForm(request, response, errors);
         Gift gift = (Gift) formBackingObject(request);
 
        if (showGiftView(gift)) {
         	mav = new ModelAndView(appendGiftParameters(request, giftViewUrl, gift));
         }
 		return mav;
 	}
 
     @SuppressWarnings("unchecked")
     @Override
     protected Map referenceData(HttpServletRequest request, Object command, Errors errors) throws Exception {
         Map refMap = super.referenceData(request, command, errors);
         
         String selectedPledgeId = request.getParameter("selectedPledgeId");
         String selectedRecurringGiftId = request.getParameter("selectedRecurringGiftId");
         if (NumberUtils.isDigits(selectedPledgeId)) {
             Pledge pledge = pledgeService.readPledgeById(Long.parseLong(selectedPledgeId));
             refMap.put("associatedPledge", pledge);
         }
         else if (NumberUtils.isDigits(selectedRecurringGiftId)) {
             RecurringGift recurringGift = recurringGiftService.readRecurringGiftById(Long.parseLong(selectedRecurringGiftId));
             refMap.put("associatedRecurringGift", recurringGift);
         }
         
         Gift gift = (Gift) command;
         if (canReprocessGift(gift)) {
         	refMap.put("allowReprocess", Boolean.TRUE);
         }
         return refMap;
     }
 
     @Override
     protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
         super.initBinder(request, binder);
         binder.registerCustomEditor(List.class, "associatedPledgeIds", new AssociationEditor());
         binder.registerCustomEditor(List.class, "associatedRecurringGiftIds", new AssociationEditor());
     }
 
 	@Override
     protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
     	Gift gift = (Gift) command;
         checkAssociations(gift);
         gift.filterValidDistributionLines();
         
         boolean saved = true;
         try {
         	if ("true".equals(request.getParameter("doReprocess")) && canReprocessGift(gift)) {
         		gift = giftService.reprocessGift(gift);
         	}
         	else {
         		gift = giftService.maintainGift(gift);
         	}
         } 
         catch (BindException e) {
             saved = false;
             errors.addAllErrors(e);
         }
 
         ModelAndView mav = null;
         if (saved) {
             mav = new ModelAndView(super.appendSaved(appendGiftParameters(request, getSuccessView(), gift)));
         }
         else {
         	gift.removeEmptyMutableDistributionLines();
 			checkAssociations(gift);
 			mav = showForm(request, errors, getFormView());
         }
         return mav;
     }
 	
     protected void checkAssociations(Gift gift) {
         giftService.checkAssociatedPledgeIds(gift);
         giftService.checkAssociatedRecurringGiftIds(gift);
     }
 }
