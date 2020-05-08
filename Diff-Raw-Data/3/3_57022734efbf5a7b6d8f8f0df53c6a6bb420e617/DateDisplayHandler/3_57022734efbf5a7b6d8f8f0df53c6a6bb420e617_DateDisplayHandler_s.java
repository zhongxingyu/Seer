 package com.orangeleap.tangerine.web.customization.tag.fields.handlers.impl.display;
 
 import com.orangeleap.tangerine.controller.TangerineForm;
 import com.orangeleap.tangerine.domain.customization.SectionDefinition;
 import com.orangeleap.tangerine.domain.customization.SectionField;
 import com.orangeleap.tangerine.util.OLLogger;
 import com.orangeleap.tangerine.util.StringConstants;
 import com.orangeleap.tangerine.web.customization.tag.fields.handlers.impl.form.DateHandler;
 import org.apache.commons.logging.Log;
 import org.springframework.beans.BeanWrapper;
 import org.springframework.context.ApplicationContext;
 import org.springframework.util.StringUtils;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.jsp.PageContext;
 import java.util.List;
 
 public class DateDisplayHandler extends DateHandler {
 
     /** Logger for this class and subclasses */
     protected final Log logger = OLLogger.getLog(getClass());
 
     public String getDateFormat() {
         return "MM / dd / yyyy";
     }
 
 	public DateDisplayHandler(ApplicationContext applicationContext) {
 		super(applicationContext);
 	}
 
 	@Override
 	protected void doHandler(HttpServletRequest request, HttpServletResponse response, PageContext pageContext,
 	                      SectionDefinition sectionDefinition, List<SectionField> sectionFields, SectionField currentField,
 	                      TangerineForm form, String formFieldName, Object fieldValue, StringBuilder sb) {
 		sb.append("<div id=\"").append(formFieldName).append("\" class=\"readOnlyField ").append(resolveEntityAttributes(currentField)).append("\">");
 
         String formattedDate = formatDate(fieldValue, getDateFormat());
 		if ( ! StringUtils.hasText(formattedDate)) {
 		    sb.append("&nbsp;");
 		}
 		sb.append("</div>");
 	}
 
     @Override
     public Object resolveDisplayValue(HttpServletRequest request, BeanWrapper beanWrapper, SectionField currentField, Object fieldValue) {
         return formatDate(fieldValue, StringConstants.EXT_DATE_FORMAT);
     }
 }
