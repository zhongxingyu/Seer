 package com.orangeleap.tangerine.web.customization.handler;
 
 import java.util.List;
 import java.util.Locale;
 
import org.apache.commons.lang.math.NumberUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.context.ApplicationContext;
 
 import com.orangeleap.tangerine.domain.Person;
 import com.orangeleap.tangerine.domain.customization.SectionField;
 import com.orangeleap.tangerine.domain.paymentInfo.Gift;
 import com.orangeleap.tangerine.domain.paymentInfo.Pledge;
 import com.orangeleap.tangerine.domain.paymentInfo.RecurringGift;
 import com.orangeleap.tangerine.service.GiftService;
 import com.orangeleap.tangerine.service.PledgeService;
 import com.orangeleap.tangerine.service.RecurringGiftService;
 import com.orangeleap.tangerine.type.ReferenceType;
 import com.orangeleap.tangerine.util.StringConstants;
 import com.orangeleap.tangerine.web.customization.FieldVO;
 
 public class LookupFieldHandler extends GenericFieldHandler {
 
     /** Logger for this class and subclasses */
     protected final Log logger = LogFactory.getLog(getClass());
     
     protected PledgeService pledgeService;
     protected RecurringGiftService recurringGiftService;
     protected GiftService giftService;
 
     public LookupFieldHandler(ApplicationContext appContext) {
         super(appContext);
         pledgeService = (PledgeService) appContext.getBean("pledgeService");
         recurringGiftService = (RecurringGiftService) appContext.getBean("recurringGiftService");
         giftService = (GiftService) appContext.getBean("giftService");
     }
 
     @Override
     public FieldVO handleField(List<SectionField> sectionFields, SectionField currentField, Locale locale, Object model) {
         FieldVO fieldVO = super.handleField(sectionFields, currentField, locale, model);
         boolean isCustom = currentField.getFieldDefinition().isCustom();
         Object propertyValue = super.getPropertyValue(model, fieldVO);
 
        if (propertyValue != null && isCustom && propertyValue instanceof String) {
             ReferenceType referenceType = currentField.getFieldDefinition().getReferenceType();
 
             String[] ids = ((String)propertyValue).split(",");
             StringBuffer sb = new StringBuffer();
             for (String id : ids) {
                 if (id.length() > 0) {
                     if (sb.length() > 0) {
                         sb.append(FieldVO.DISPLAY_VALUE_DELIMITER);
                     }
                    if (NumberUtils.isDigits(id)) {
                        Long longId = Long.valueOf(id);
                        sb.append(resolve(longId, referenceType));
                        fieldVO.addId(longId);
                    }
                 }
             }
             fieldVO.setDisplayValue(sb.toString());
             fieldVO.setReferenceType(referenceType);
         }
         return fieldVO;
     }
 
     protected String resolve(Long id, ReferenceType referenceType) {
         String val = new StringBuilder(id == null ? StringConstants.EMPTY : id.toString()).toString();
         if (referenceType == ReferenceType.person) {
             Person constituent = constituentService.readConstituentById(id);
             if (constituent == null) {
                 logger.warn("resolve: Could not find constituent for ID = " + id);
             }
             else {
                 val = constituent.getDisplayValue();
             }
         }
         else if (referenceType == ReferenceType.pledge) {
             Pledge pledge = pledgeService.readPledgeById(id);
             if (pledge == null) {
                 logger.warn("resolve: Could not find pledge for ID = " + id);
             }
             else {
                 val = pledge.getShortDescription();
             }
         }
         else if (referenceType == ReferenceType.recurringGift) {
             RecurringGift recurringGift = recurringGiftService.readRecurringGiftById(id);
             if (recurringGift == null) {
                 logger.warn("resolve: Could not find recurringGift for ID = " + id);
             }
             else {
                 val = recurringGift.getShortDescription();
             }
         }
         else if (referenceType == ReferenceType.gift) {
             Gift gift = giftService.readGiftById(id);
             if (gift == null) {
                 logger.warn("resolve: Could not find gift for ID = " + id);
             }
             else {
                 val = gift.getShortDescription();
             }
         }
         return val;
     }
 }
