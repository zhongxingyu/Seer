 package com.orangeleap.tangerine.web.customization.tag.inputs.impl.picklists;
 
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.jsp.PageContext;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.stereotype.Component;
 import org.springframework.util.StringUtils;
 
 import com.orangeleap.tangerine.util.StringConstants;
 import com.orangeleap.tangerine.web.customization.FieldVO;
 
 @Component("picklistInput")
 public class PicklistInput extends AbstractSingleValuedPicklistInput {
 
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
     protected void createBeginSelect(HttpServletRequest request, FieldVO fieldVO, StringBuilder sb, String references) {
         sb.append("<select name='" + fieldVO.getFieldName() + "' id='" + fieldVO.getFieldId() + "' class='");
         if (fieldVO.isCascading()) {
             sb.append("picklist ");
         }
         sb.append(fieldVO.getEntityAttributes() + "' references='" + references + "'>");
     }
     
     protected String createOptions(HttpServletRequest request, FieldVO fieldVO, StringBuilder sb) {
         List<String> augmentedCodes = fieldVO.getAugmentedCodes();
         
         String selectedRef = null;
         if (augmentedCodes != null) {
             List<String> references = fieldVO.getReferenceValues();
             List<Object> displayValues = fieldVO.getDisplayValues();
             for (int i = 0; i < augmentedCodes.size(); i++) {
                 String code = augmentedCodes.get(i);
                 sb.append("<option value='" + code + "'");
 
                String reference = (references == null ? StringConstants.EMPTY : references.get(i));
                 if (StringUtils.hasText(reference)) {
                     sb.append(" reference='" + reference + "'");
                 }
                 if (fieldVO.getFieldValue() != null) {
                     if (code.equals(fieldVO.getFieldValue().toString())) {
                         sb.append(" selected='selected'");
                         selectedRef = reference;
                     }
                 }
                 sb.append(">");
                sb.append(displayValues == null ? StringConstants.EMPTY : displayValues.get(i));
                 sb.append("</option>");
             }
         }
         return selectedRef;
     }
     
     protected void createSelectedRef(HttpServletRequest request, FieldVO fieldVO, StringBuilder sb, String selectedRef) {
         if (fieldVO.isRequired() && StringUtils.hasText(selectedRef) == false) {
             selectedRef = fieldVO.getReferenceValues() == null ? StringConstants.EMPTY : fieldVO.getReferenceValues().get(0);
         }
         if (selectedRef == null) {
             selectedRef = StringConstants.EMPTY;
         }
         selectedRef = selectedRef.trim();
         sb.append("<div style='display:none' id='selectedRef-" + fieldVO.getFieldId() + "'>" + selectedRef + "</div>");
     }
 }
