 package com.orangeleap.tangerine.controller.gift;
 
 import java.util.List;
 import java.util.Map;
 
 import javax.annotation.Resource;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.lang.math.NumberUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.validation.BindException;
 import org.springframework.validation.Errors;
 import org.springframework.web.bind.ServletRequestDataBinder;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.orangeleap.tangerine.controller.TangerineConstituentAttributesFormController;
 import com.orangeleap.tangerine.domain.AbstractEntity;
 import com.orangeleap.tangerine.domain.paymentInfo.Gift;
 import com.orangeleap.tangerine.domain.paymentInfo.Pledge;
 import com.orangeleap.tangerine.domain.paymentInfo.RecurringGift;
 import com.orangeleap.tangerine.service.GiftService;
 import com.orangeleap.tangerine.service.PledgeService;
 import com.orangeleap.tangerine.service.RecurringGiftService;
 import com.orangeleap.tangerine.util.StringConstants;
 
 public class GiftFormController extends TangerineConstituentAttributesFormController {
 
     /** Logger for this class and subclasses */
     protected final Log logger = LogFactory.getLog(getClass());
 
     @Resource(name="giftService")
     private GiftService giftService;
 
     @Resource(name = "pledgeService")
     private PledgeService pledgeService;
 
     @Resource(name = "recurringGiftService")
     private RecurringGiftService recurringGiftService;
 
     @Override
     protected AbstractEntity findEntity(HttpServletRequest request) {
         return giftService.createDefaultGift(super.getConstituent(request));
     }
 
     @SuppressWarnings("unchecked")
     @Override
     protected Map referenceData(HttpServletRequest request, Object command, Errors errors) throws Exception {
         Map returnMap = super.referenceData(request, command, errors);
         
         String selectedPledgeId = request.getParameter("selectedPledgeId");
         String selectedRecurringGiftId = request.getParameter("selectedRecurringGiftId");
         if (NumberUtils.isDigits(selectedPledgeId)) {
             Pledge pledge = pledgeService.readPledgeById(Long.parseLong(selectedPledgeId));
             returnMap.put("associatedPledge", pledge);
         }
         else if (NumberUtils.isDigits(selectedRecurringGiftId)) {
             RecurringGift recurringGift = recurringGiftService.readRecurringGiftById(Long.parseLong(selectedRecurringGiftId));
             returnMap.put("associatedRecurringGift", recurringGift);
         }
         return returnMap;
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
         Gift current = null;
         try {
         	current = giftService.maintainGift(gift);
         } catch (BindException e) {
             saved = false;
             current = gift;
             errors.addAllErrors(e);
         }
 
         ModelAndView mav = null;
         if (saved) {
             mav = new ModelAndView(super.appendSaved(getSuccessView() + "?" + StringConstants.GIFT_ID + "=" + current.getId() + "&" + StringConstants.PERSON_ID + "=" + super.getConstituentId(request)));
         }
         else {
			current.removeEmptyMutableDistributionLines();
			checkAssociations(current);
			mav = showForm(request, errors, getFormView());
         }
         return mav;
         
     }
     
     protected void checkAssociations(Gift gift) {
         giftService.checkAssociatedPledgeIds(gift);
         giftService.checkAssociatedRecurringGiftIds(gift);
     }
 }
