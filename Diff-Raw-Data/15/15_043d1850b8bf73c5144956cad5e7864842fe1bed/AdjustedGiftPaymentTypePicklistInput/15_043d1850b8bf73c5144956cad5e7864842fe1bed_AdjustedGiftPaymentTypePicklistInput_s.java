 package com.orangeleap.tangerine.web.customization.tag.inputs.impl.picklists;
 
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.jsp.PageContext;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.stereotype.Component;
 import org.springframework.util.StringUtils;
 
 import com.orangeleap.tangerine.domain.PaymentSource;
 import com.orangeleap.tangerine.domain.paymentInfo.AdjustedGift;
 import com.orangeleap.tangerine.util.StringConstants;
 import com.orangeleap.tangerine.web.customization.FieldVO;
 
 @Component("adjustedGiftPaymentTypePicklistInput")
 public class AdjustedGiftPaymentTypePicklistInput extends PicklistInput {
 
     /** Logger for this class and subclasses */
     protected final Log logger = LogFactory.getLog(getClass());
 
     @Override
     public String handleField(HttpServletRequest request, HttpServletResponse response, PageContext pageContext, FieldVO fieldVO) {
         if (logger.isTraceEnabled()) {
             logger.trace("handleField: field.fieldName = " + fieldVO.getFieldName());
         }
         StringBuilder sb = new StringBuilder();
         createBeginSelect(request, fieldVO, sb, fieldVO.getUniqueReferenceValues());
         createNoneOption(request, fieldVO, sb);
         String selectedRef = createOptions(request, fieldVO, sb);
         createEndSelect(request, fieldVO, sb);
         createSelectedRef(request, fieldVO, sb, selectedRef);
         return sb.toString();
     }
     
     @Override
     protected String createOptions(HttpServletRequest request, FieldVO fieldVO, StringBuilder sb) {
         List<String> augmentedCodes = fieldVO.getAugmentedCodes();
         
         String selectedRef = null;
         if (augmentedCodes != null) {
             List<String> references = fieldVO.getReferenceValues();
             List<Object> displayValues = fieldVO.getDisplayValues();
             
             AdjustedGift adjustedGift = (AdjustedGift) fieldVO.getModel();
 
             for (int i = 0; i < augmentedCodes.size(); i++) {
                 String code = augmentedCodes.get(i);
                 
                 if (PaymentSource.CASH.equals(code) || PaymentSource.CHECK.equals(code) || 
                         (adjustedGift.getSelectedPaymentSource() != null && adjustedGift.getSelectedPaymentSource().getId() != null && 
                                 adjustedGift.getSelectedPaymentSource().getId() > 0 && adjustedGift.getSelectedPaymentSource().getPaymentType().equals(code))) {
                     sb.append("<option value='" + code + "'");
     
                    String reference = (references == null ? StringConstants.EMPTY : references.get(i));
                     if (StringUtils.hasText(reference)) {
                         sb.append(" reference='" + reference + "'");
                     }
                     if (code.equals(adjustedGift.getPaymentType())) {
                         sb.append(" selected='selected'");
                         selectedRef = reference;
                     }
                     sb.append(">");
                    sb.append(displayValues == null ? StringConstants.EMPTY : displayValues.get(i));
                     sb.append("</option>");
                 }
             }
         }
         return selectedRef;
     }
 }
