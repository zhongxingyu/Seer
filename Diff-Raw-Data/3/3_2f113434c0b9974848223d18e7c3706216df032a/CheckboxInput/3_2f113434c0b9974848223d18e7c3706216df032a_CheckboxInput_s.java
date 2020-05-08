 package com.orangeleap.tangerine.web.customization.tag.inputs.impl.fields;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.jsp.PageContext;
 
 import org.apache.commons.lang.StringEscapeUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.stereotype.Component;
 
 import com.orangeleap.tangerine.web.customization.FieldVO;
 import com.orangeleap.tangerine.web.customization.tag.inputs.AbstractInput;
 
 @Component("checkboxInput")
 public class CheckboxInput extends AbstractInput {
 
     /** Logger for this class and subclasses */
     protected final Log logger = LogFactory.getLog(getClass());
 
     @Override
     public String handleField(HttpServletRequest request, HttpServletResponse response, PageContext pageContext, FieldVO fieldVO) {
         StringBuilder sb = new StringBuilder();
         sb.append("<input type=\"hidden\" name=\"_" + StringEscapeUtils.escapeHtml(fieldVO.getFieldName()) + "\"/>");
         sb.append("<input type=\"checkbox\" value=\"true\" class=\"checkbox " + checkForNull(fieldVO.getEntityAttributes()) + "\" name=\"" + StringEscapeUtils.escapeHtml(fieldVO.getFieldName()) + "\" ");
         sb.append("id=\"" + StringEscapeUtils.escapeHtml(fieldVO.getFieldId()) + "\" ");
        if ("true".equals(fieldVO.getFieldValue())) {
             sb.append("checked=\"true\" ");
         }
         if (fieldVO.isDisabled()) {
             sb.append("disabled=\"true\" ");
         }
         sb.append("/>");
         return sb.toString();
     }
 }
