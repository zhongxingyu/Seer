 package com.orangeleap.tangerine.web.customization.tag.fields.handlers.impl.form;
 
 import com.orangeleap.tangerine.controller.TangerineForm;
 import com.orangeleap.tangerine.domain.customization.SectionDefinition;
 import com.orangeleap.tangerine.domain.customization.SectionField;
 import com.orangeleap.tangerine.util.StringConstants;
 import com.orangeleap.tangerine.web.customization.tag.fields.handlers.impl.AbstractFieldHandler;
 import org.springframework.beans.BeanWrapper;
 import org.springframework.context.ApplicationContext;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.jsp.PageContext;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 import java.util.regex.Pattern;
 
 /**
  * User: alexlo
  * Date: Jul 7, 2009
  * Time: 5:09:39 PM
  */
 public class DateHandler extends AbstractFieldHandler {
 
 	public DateHandler(ApplicationContext applicationContext) {
 		super(applicationContext);
 	}
 
 	@Override
 	protected void doHandler(HttpServletRequest request, HttpServletResponse response, PageContext pageContext,
 	                      SectionDefinition sectionDefinition, List<SectionField> sectionFields, SectionField currentField,
 	                      TangerineForm form, String formFieldName, Object fieldValue, StringBuilder sb) {
 		sb.append("<div class=\"lookupWrapper\">");
 
 		sb.append("<input id=\"").append(formFieldName).append("\" ");
 		sb.append("class=\"text ");
 		writeErrorClass(pageContext, formFieldName, sb);
 		sb.append(resolveEntityAttributes(currentField)).append("\" ");
 
		sb.append("type=\"text\" size=\"16\" value=\"");
         sb.append(formatDate(fieldValue, StringConstants.MM_DD_YYYY_FORMAT));
 		sb.append("\" name=\"").append(formFieldName).append("\"/>");
 
 		createScript(formFieldName, sb);
 		
 		sb.append("</div>");
 	}
 
 	protected void createScript(String formFieldName, StringBuilder sb) {
 		sb.append("<script type=\"text/javascript\">");
 		sb.append("//<![CDATA[\n");
 		sb.append("var name = '").append(formFieldName).append("';\n");
 		sb.append("var seasonal = (name.indexOf('seasonal') > -1);\n");
 		sb.append("name = name.replace('[','').replace(']','');\n");
 		sb.append("new Ext.form.DateField({\n");
 		sb.append("applyTo: name,\n");
 		sb.append("id: name + \"-wrapper\",\n");
 		sb.append("format: (seasonal ? 'F-j' : 'm/d/Y'),\n");
 		sb.append("width: 250\n");
 		sb.append("});\n");
 		sb.append("//]]>\n");
 		sb.append("</script>");
 	}
 
     @Override
     public Object resolveDisplayValue(HttpServletRequest request, BeanWrapper beanWrapper, SectionField currentField, Object fieldValue) {
         return formatDate(fieldValue, StringConstants.EXT_DATE_FORMAT);
     }
 
     protected String formatDate(final Object fieldValue, final String dateFormat) {
         String formattedDate = StringConstants.EMPTY;
         if (fieldValue != null) {
             final SimpleDateFormat toDateFormat = new SimpleDateFormat(dateFormat);
             final SimpleDateFormat mmDdYyyyFormat = new SimpleDateFormat(StringConstants.MM_DD_YYYY_FORMAT);
             final SimpleDateFormat YyyyMmDdFormat = new SimpleDateFormat(StringConstants.YYYY_MM_DD_FORMAT);
             if (fieldValue instanceof Date) {
                 formattedDate = toDateFormat.format(fieldValue);
             }
             else if (fieldValue instanceof String) {
                 String dateFieldValue = (String) fieldValue;
                 try {
                     if (Pattern.matches("\\d{2}/\\d{2}/\\d{4}", dateFieldValue)) {
                         Date parsedDate = mmDdYyyyFormat.parse((String) fieldValue);
                         formattedDate = toDateFormat.format(parsedDate);
                     }
                     else if (Pattern.matches("\\d{4}-\\d{2}-\\d{2}", dateFieldValue)) {
                         Date parsedDate = YyyyMmDdFormat.parse(dateFieldValue);
                         formattedDate = toDateFormat.format(parsedDate);
                     }
                     else {
                         toDateFormat.parse(dateFieldValue);
                         formattedDate = dateFieldValue;
                     }
                 }
                 catch (Exception e) {
                     logger.warn("formatDate: could not format date = " + fieldValue);
                 }
             }
         }
         return formattedDate;
     }
 }
