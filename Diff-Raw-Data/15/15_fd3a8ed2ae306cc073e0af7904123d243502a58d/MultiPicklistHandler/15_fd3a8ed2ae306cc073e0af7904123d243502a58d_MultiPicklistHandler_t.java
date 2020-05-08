 /*
  * Copyright (c) 2009. Orange Leap Inc. Active Constituent
  * Relationship Management Platform.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.orangeleap.tangerine.web.customization.tag.fields.handlers.impl.picklists.multi;
 
 import com.orangeleap.tangerine.controller.TangerineForm;
 import com.orangeleap.tangerine.domain.customization.Picklist;
 import com.orangeleap.tangerine.domain.customization.PicklistItem;
 import com.orangeleap.tangerine.domain.customization.SectionDefinition;
 import com.orangeleap.tangerine.domain.customization.SectionField;
 import com.orangeleap.tangerine.util.OLLogger;
 import com.orangeleap.tangerine.util.StringConstants;
 import com.orangeleap.tangerine.web.customization.tag.fields.handlers.impl.picklists.AbstractPicklistHandler;
 import org.apache.commons.lang.StringEscapeUtils;
 import org.apache.commons.logging.Log;
 import org.springframework.context.ApplicationContext;
 import org.springframework.util.StringUtils;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.jsp.PageContext;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 
 public class MultiPicklistHandler extends AbstractPicklistHandler {
 
     /** Logger for this class and subclasses */
     protected final Log logger = OLLogger.getLog(getClass());
 
 	public MultiPicklistHandler(ApplicationContext applicationContext) {
 		super(applicationContext);
 	}
 
 	@Override
 	protected void doHandler(HttpServletRequest request, HttpServletResponse response, PageContext pageContext,
 	                      SectionDefinition sectionDefinition, List<SectionField> sectionFields, SectionField currentField,
 	                      TangerineForm form, String formFieldName, Object fieldValue, StringBuilder sb) {
 		Picklist picklist = resolvePicklist(currentField, pageContext);
 		createTop(request, pageContext, sb);
 		createContainerBegin(request, pageContext, sb);
 		createMultiPicklistBegin(currentField, formFieldName, picklist, sb);
 		createLeft(sb);
 		String selectedRefs = createMultiPicklistOptions(pageContext, picklist, fieldValue, sb);
 		createLabelTextInput(pageContext, currentField, formFieldName, sb);
 		createRight(sb);
 		createMultiPicklistEnd(sb);
 		createHiddenInput(currentField, formFieldName, fieldValue, sb);
 		createContainerEnd(sb);
 		createBottom(request, pageContext, sb);
 		createSelectedRefs(formFieldName, selectedRefs, sb);
 		createLookupLink(sb);
 	}
 
     protected void createTop(HttpServletRequest request, PageContext pageContext, StringBuilder sb) {
         sb.append("<div class=\"lookupScrollTop ");
         writeErrorClass(request, pageContext, sb);
         sb.append("\"></div>");
     }
 
     protected void createContainerBegin(HttpServletRequest request, PageContext pageContext, StringBuilder sb) {
         sb.append("<div class=\"lookupScrollContainer ");
 	    sb.append(getContainerCssClass());
 	    writeErrorClass(request, pageContext, sb);
 	    sb.append("\">");
     }
 
 	protected String getContainerCssClass() {
 		return StringConstants.EMPTY;
 	}
 
     protected void createMultiPicklistBegin(SectionField currentField, String formFieldName, Picklist picklist, StringBuilder sb) {
         sb.append("<div class=\"multiPicklist multiLookupField ");
 	    sb.append(resolveEntityAttributes(currentField));
 	    //sb.append("\" id=\"").append(formFieldName);                       // TODO: check if ID is messed up
 	    sb.append("\" references=\"").append(resolveReferenceValues(currentField, picklist)).append("\">");
     }
 
     protected void createLeft(StringBuilder sb) {
         sb.append("<div class=\"lookupScrollLeft\"></div>");
     }
 
     protected String createMultiPicklistOptions(PageContext pageContext, Picklist picklist, Object fieldValue, StringBuilder sb) {
 	    Set<String> selectedRefs = new TreeSet<String>();
 	    if (picklist != null) {
		    Object[] fieldVals = splitValuesByCustomFieldSeparator(fieldValue);

 		    for (PicklistItem item : picklist.getActivePicklistItems()) {
 			    sb.append("<div class=\"multiPicklistOption multiOption\" style=\"");
 
 			    boolean foundValue = false;
 			    for (Object val : fieldVals) {
 				    if (item.getItemName().equals(val)) {
 					    foundValue = true;
 					    break;
 				    }
 			    }
 
				if (!foundValue) {
				    sb.append("display:none");
			    }
			    else if (StringUtils.hasText(item.getReferenceValue())) {
 					selectedRefs.add(item.getReferenceValue());
 				}
 			    
 			    String escapedItemName = StringEscapeUtils.escapeHtml(item.getItemName());
 			    sb.append("\" id=\"option-").append(escapedItemName);
 			    sb.append("\" selectedId=\"").append(escapedItemName).append("\" reference=\"").append(checkForNull(item.getReferenceValue())).append("\">");
 
 			    String displayValue = resolvePicklistItemDisplayValue(item, pageContext);
 			    if (StringUtils.hasText(displayValue)) {
 					sb.append(displayValue);
 					writeDeleteLink(sb, "Lookup.deleteOption(this)");
 			    }
 
 			    sb.append("</div>");
 		    }
 	    }
 	    return StringUtils.collectionToCommaDelimitedString(selectedRefs);
     }
 
     protected void createLabelTextInput(PageContext pageContext, SectionField currentField, String formFieldName, StringBuilder sb) {
         sb.append("<input type=\"hidden\" name=\"labelText\" id=\"");
 	    sb.append(formFieldName).append("-labelText\" value=\"").append(resolveLabelText(currentField, pageContext)).append("\"/>");
     }
 
     protected void createRight(StringBuilder sb) {
         sb.append("<div class=\"lookupScrollRight\"></div>");
     }
 
     protected void createMultiPicklistEnd(StringBuilder sb) {
         sb.append("</div>");
     }
 
     protected void createHiddenInput(SectionField currentField, String formFieldName, Object fieldValue, StringBuilder sb) {
         sb.append("<input type=\"hidden\" name=\"").append(formFieldName).append("\" id=\"").append(formFieldName).append("\" value=\"").append(checkForNull(fieldValue));
 	    sb.append("\" additionalFieldId=\"").append(resolveAdditionalFormFieldName(formFieldName)).append("\"/>");
     }
 
     protected void createContainerEnd(StringBuilder sb) {
         sb.append("</div>");
     }
 
     protected void createBottom(HttpServletRequest request, PageContext pageContext, StringBuilder sb) {
         sb.append("<div class=\"lookupScrollBottom ");
 	    writeErrorClass(request, pageContext, sb);
         sb.append("\"></div>");
     }
 
 	protected void createSelectedRefs(String formFieldName, String selectedRefs, StringBuilder sb) {
 		sb.append("<div style=\"display:none\" id=\"selectedRef-").append(formFieldName).append("\">").append(selectedRefs).append("</div>");
 	}
 
 	protected void createLookupLink(StringBuilder sb) {
 		String lookupMsg = getMessage("lookup");
 		sb.append("<a href=\"javascript:void(0)\" onclick=\"").append(getLookupClickHandler()).append("\" class=\"multiLookupLink hideText\" ");
 		sb.append("alt=\"").append(lookupMsg).append("\" title=\"").append(lookupMsg).append("\">").append(lookupMsg).append("</a>");
 	}
 
 	protected String getLookupClickHandler() {
 		return "Lookup.loadMultiPicklist(this)";
 	}
 
 	@Override
 	protected String getSideCssClass(Object fieldValue) {
 		return new StringBuilder(super.getSideCssClass(fieldValue)).append(" multiOptionLi").toString();
 	}
 }
