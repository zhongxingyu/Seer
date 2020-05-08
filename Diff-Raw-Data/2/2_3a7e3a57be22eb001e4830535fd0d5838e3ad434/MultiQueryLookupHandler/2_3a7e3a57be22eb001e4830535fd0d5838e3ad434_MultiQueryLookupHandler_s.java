 package com.orangeleap.tangerine.web.customization.tag.fields.handlers.impl.lookups;
 
 import com.orangeleap.tangerine.controller.TangerineForm;
 import com.orangeleap.tangerine.domain.customization.SectionDefinition;
 import com.orangeleap.tangerine.domain.customization.SectionField;
 import com.orangeleap.tangerine.type.FieldType;
 import com.orangeleap.tangerine.type.ReferenceType;
 import com.orangeleap.tangerine.util.OLLogger;
 import com.orangeleap.tangerine.util.StringConstants;
 import org.apache.commons.lang.StringEscapeUtils;
 import org.apache.commons.lang.math.NumberUtils;
 import org.apache.commons.logging.Log;
 import org.springframework.beans.BeanWrapper;
 import org.springframework.context.ApplicationContext;
 import org.springframework.util.StringUtils;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.jsp.PageContext;
 import java.util.ArrayList;
 import java.util.List;
 
 public class MultiQueryLookupHandler extends QueryLookupHandler {
 
     /** Logger for this class and subclasses */
     protected final Log logger = OLLogger.getLog(getClass());
 
 	public MultiQueryLookupHandler(ApplicationContext applicationContext) {
 		super(applicationContext);
 	}
 
 	@Override
 	protected void doHandler(HttpServletRequest request, HttpServletResponse response, PageContext pageContext,
 	                      SectionDefinition sectionDefinition, List<SectionField> sectionFields, SectionField currentField,
 	                      TangerineForm form, String formFieldName, Object fieldValue, StringBuilder sb) {
 		createTop(request, pageContext, formFieldName, sb);
 		createContainerBegin(request, pageContext, formFieldName, sb);
 		createMultiLookupBegin(currentField, sb);
 		createLeft(sb);
 		createMultiLookupOptions(pageContext, currentField, form, formFieldName, fieldValue, sb);
 		createRight(sb);
 		createMultiLookupEnd(sb);
 		createHiddenInput(formFieldName, fieldValue, sb);
 
 		if (!FieldType.QUERY_LOOKUP_DISPLAY.equals(currentField.getFieldType())) {
 			createClone(sb);
 		}
 		
 		createContainerEnd(sb);
 		createBottom(request, pageContext, formFieldName, sb);
 
 		if (!FieldType.QUERY_LOOKUP_DISPLAY.equals(currentField.getFieldType())) {
 			createLookupLink(currentField, sb);
 		}
 	}
 
     protected void createTop(HttpServletRequest request, PageContext pageContext, String formFieldName, StringBuilder sb) {
         sb.append("<div class=\"lookupScrollTop ");
         writeErrorClass(pageContext, formFieldName, sb);
         sb.append("\"></div>");
     }
 
     protected void createContainerBegin(HttpServletRequest request, PageContext pageContext, String formFieldName, StringBuilder sb) {
         sb.append("<div class=\"lookupScrollContainer ");
 	    sb.append(getContainerCssClass());
 	    writeErrorClass(pageContext, formFieldName, sb);
 	    sb.append("\">");
     }
 
 	protected String getContainerCssClass() {
 		return StringConstants.EMPTY;
 	}
 
     protected void createMultiLookupBegin(SectionField currentField, StringBuilder sb) {
         sb.append("<div class=\"multiLookupField ");
 	    sb.append(resolveEntityAttributes(currentField));
 	    sb.append("\">");
     }
 
     protected void createLeft(StringBuilder sb) {
         sb.append("<div class=\"lookupScrollLeft\"></div>");
     }
 
     protected void createMultiLookupOptions(PageContext pageContext, SectionField currentField, TangerineForm form, String formFieldName,
                                             Object fieldValue, StringBuilder sb) {
 	    if (fieldValue != null) {
 		    Object[] fieldVals = splitValuesByCustomFieldSeparator(fieldValue);
 		    for (Object val : fieldVals) {
 			    String displayVal;
 			    StringBuilder linkSb = new StringBuilder();
 			    String linkMsg = getMessage("gotoLink");
 
 			    ReferenceType referenceType = currentField.getFieldDefinition().getReferenceType();
 			    if (NumberUtils.isDigits(val.toString()) && Long.valueOf(val.toString()) > 0 && referenceType != null) {
 				    displayVal = resolve(Long.parseLong(val.toString()), referenceType);
 
 				    linkSb.append(referenceType).append(".htm?");
				    linkSb.append(referenceType).append("Id=").append(checkForNull(fieldValue));
 				    if ( ! ReferenceType.constituent.equals(referenceType)) {
 					    linkSb.append("&").append(StringConstants.CONSTITUENT_ID).append("=").append(pageContext.getRequest().getParameter(StringConstants.CONSTITUENT_ID));
 				    }
 			    }
 			    else {
 				    displayVal = val.toString();
 			    }
 			    sb.append("<div class=\"multiQueryLookupOption multiOption\" id=\"lookup-");
 			    sb.append(StringEscapeUtils.escapeHtml(displayVal)).append("\" selectedId=\"").append(val).append("\">");
 
 			    if (linkSb.length() > 0) {
 			        sb.append("<a href=\"").append(linkSb.toString()).append("\" target=\"_blank\" alt=\"").append(linkMsg).append("\" title=\"").append(linkMsg).append("\">");
 				    sb.append(displayVal);
 				    sb.append("</a>");
 
 				    if (!FieldType.QUERY_LOOKUP_DISPLAY.equals(currentField.getFieldType())) {
 				        writeDeleteLink(sb, "Lookup.deleteOption(this)");
 				    }
 			    }
 			    else {
 				    sb.append("<span>").append(displayVal).append("</span>");
 			    }
 
 			    sb.append("</div>");
 		    }
 	    }
     }
 
     protected void createRight(StringBuilder sb) {
         sb.append("<div class=\"lookupScrollRight\"></div>");
     }
 
     protected void createMultiLookupEnd(StringBuilder sb) {
         sb.append("</div>");
     }
 
     protected void createHiddenInput(String formFieldName, Object fieldValue, StringBuilder sb) {
         sb.append("<input type=\"hidden\" name=\"").append(formFieldName).append("\" id=\"").append(formFieldName).append("\" value=\"").append(checkForNull(fieldValue));
 	    sb.append("\"/>");
     }
 
 	protected void createClone(StringBuilder sb) {
 		sb.append("<div class=\"multiQueryLookupOption multiOption noDisplay clone\" selectedId=\"\">");
 		sb.append("<a href=\"\" target=\"_blank\"></a>");
 		writeDeleteLink(sb, "Lookup.deleteOption(this)");
 		sb.append("</div>");
 	}
 
     protected void createContainerEnd(StringBuilder sb) {
         sb.append("</div>");
     }
 
     protected void createBottom(HttpServletRequest request, PageContext pageContext, String formFieldName, StringBuilder sb) {
         sb.append("<div class=\"lookupScrollBottom ");
 	    writeErrorClass(pageContext, formFieldName, sb);
         sb.append("\"></div>");
     }
 
 	protected void createLookupLink(SectionField currentField, StringBuilder sb) {
 		String lookupMsg = getMessage("lookup");
 		sb.append("<a href=\"javascript:void(0)\" onclick=\"").append(getLookupClickHandler()).append("\" class=\"multiLookupLink hideText\" ");
 		sb.append("fieldDef=\"").append(StringEscapeUtils.escapeHtml(currentField.getFieldDefinition().getId())).append("\" ");
 		sb.append("alt=\"").append(lookupMsg).append("\" title=\"").append(lookupMsg).append("\">").append(lookupMsg).append("</a>");
 	}
 
 	protected String getLookupClickHandler() {
 		return "Lookup.loadMultiQueryLookup(this)";
 	}
 
 	@Override
 	protected String getSideCssClass(Object fieldValue) {
 		return new StringBuilder(super.getSideCssClass(fieldValue)).append(" multiOptionLi queryLookupLi").toString();
 	}
 
     @Override
     public Object resolveDisplayValue(HttpServletRequest request, BeanWrapper beanWrapper, SectionField currentField, Object fieldValue) {
         List<String> displayValues = new ArrayList<String>();
         if (fieldValue != null) {
             Object[] fieldVals = splitValuesByCustomFieldSeparator(fieldValue);
             for (Object val : fieldVals) {
                 String displayVal;
                 ReferenceType referenceType = currentField.getFieldDefinition().getReferenceType();
                 if (NumberUtils.isDigits(val.toString()) && Long.valueOf(val.toString()) > 0 && referenceType != null) {
                     displayVal = resolve(Long.parseLong(val.toString()), referenceType);
                 }
                 else {
                     displayVal = val.toString();
                 }
                 displayValues.add(displayVal);
             }
         }
         return StringUtils.collectionToDelimitedString(displayValues, StringConstants.COMMA_SPACE);  
     }
 }
