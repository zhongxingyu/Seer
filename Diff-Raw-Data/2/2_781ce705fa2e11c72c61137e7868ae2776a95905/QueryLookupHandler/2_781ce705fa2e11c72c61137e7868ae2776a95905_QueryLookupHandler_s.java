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
 
 package com.orangeleap.tangerine.web.customization.tag.fields.handlers.impl.lookups;
 
 import com.orangeleap.tangerine.controller.TangerineForm;
 import com.orangeleap.tangerine.domain.Constituent;
 import com.orangeleap.tangerine.domain.customization.SectionDefinition;
 import com.orangeleap.tangerine.domain.customization.SectionField;
 import com.orangeleap.tangerine.domain.paymentInfo.Gift;
 import com.orangeleap.tangerine.domain.paymentInfo.Pledge;
 import com.orangeleap.tangerine.domain.paymentInfo.RecurringGift;
 import com.orangeleap.tangerine.service.GiftService;
 import com.orangeleap.tangerine.service.PledgeService;
 import com.orangeleap.tangerine.service.RecurringGiftService;
 import com.orangeleap.tangerine.type.FieldType;
 import com.orangeleap.tangerine.type.ReferenceType;
 import com.orangeleap.tangerine.util.OLLogger;
 import com.orangeleap.tangerine.util.StringConstants;
 import com.orangeleap.tangerine.web.customization.tag.fields.handlers.impl.AbstractFieldHandler;
 import org.apache.commons.lang.StringEscapeUtils;
 import org.apache.commons.lang.math.NumberUtils;
 import org.apache.commons.logging.Log;
 import org.springframework.context.ApplicationContext;
 import org.springframework.util.StringUtils;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.jsp.PageContext;
 import java.util.List;
 
 public class QueryLookupHandler extends AbstractFieldHandler {
 
     /** Logger for this class and subclasses */
     protected final Log logger = OLLogger.getLog(getClass());
 	protected final PledgeService pledgeService;
 	protected final RecurringGiftService recurringGiftService;
 	protected final GiftService giftService;
 
 	public QueryLookupHandler(ApplicationContext applicationContext) {
 		super(applicationContext);
 		pledgeService = (PledgeService) applicationContext.getBean("pledgeService");
 		recurringGiftService = (RecurringGiftService) applicationContext.getBean("recurringGiftService");
 		giftService = (GiftService) applicationContext.getBean("giftService");
 	}
 
 	protected String resolve(Long id, ReferenceType referenceType) {
 	    String val = new StringBuilder(id == null ? StringConstants.EMPTY : id.toString()).toString();
 	    if (referenceType == ReferenceType.constituent) {
 	        Constituent constituent = constituentService.readConstituentById(id);
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
 
 	@Override
 	protected void doHandler(HttpServletRequest request, HttpServletResponse response, PageContext pageContext, SectionDefinition sectionDefinition,
 	                      List<SectionField> sectionFields, SectionField currentField, TangerineForm form, String formFieldName,
 	                      Object fieldValue, StringBuilder sb) {
 		createLookupWrapperBegin(sb);
 		createLookupFieldBegin(currentField, sb);
 		createLookupOptionBegin(formFieldName, fieldValue, sb);
 		String displayValue = createOptionText(request, currentField, form, formFieldName, fieldValue, sb);
 
 		if (!FieldType.ASSOCIATION_DISPLAY.equals(currentField.getFieldType())) {
 			createDeleteOption(sb, displayValue);
 		}
 
 		createLookupOptionEnd(sb);
 
		if (!FieldType.ASSOCIATION.equals(currentField.getFieldType())) {
 			createLookupLink(currentField, formFieldName, sb);
 		}
 
 		createLookupFieldEnd(sb);
 
 		if (!FieldType.ASSOCIATION_DISPLAY.equals(currentField.getFieldType())) {
 			createHiddenField(currentField, formFieldName, fieldValue, sb);
 			createCloneable(sb);
 		}
 		
 		createHiearchy(request, currentField, formFieldName, sb);
 		createLookupWrapperEnd(sb);
 	}
 
 	@Override
 	protected String getSideCssClass(Object fieldValue) {
 		return new StringBuilder(super.getSideCssClass(fieldValue)).append(" queryLookupLi ").toString();
 	}
 
 	protected void createLookupWrapperBegin(StringBuilder sb) {
         sb.append("<div class=\"lookupWrapper\">");
     }
 
     protected void createLookupFieldBegin(SectionField currentField, StringBuilder sb) {
         sb.append("<div class=\"lookupField ").append(resolveEntityAttributes(currentField)).append("\">");
     }
 
     protected void createLookupOptionBegin(String formFieldName, Object fieldValue, StringBuilder sb) {
         sb.append("<div id=\"lookup-").append(formFieldName).append("\" class=\"queryLookupOption\" selectedId=\"").append(checkForNull(fieldValue)).append("\">");
     }
 
     protected String createOptionText(HttpServletRequest request, SectionField currentField, TangerineForm form, String formFieldName,
 	                      Object fieldValue, StringBuilder sb) {
 		ReferenceType referenceType = currentField.getFieldDefinition().getReferenceType();
 
 	    String displayValue = null;
 		sb.append("<span>");
 		if (fieldValue != null) {
 			if (NumberUtils.isDigits(fieldValue.toString()) && Long.valueOf(fieldValue.toString()) > 0 && referenceType != null) {
 				Long longId = Long.valueOf(fieldValue.toString());
 				displayValue = resolve(longId, referenceType);
 
 				StringBuilder linkSb = new StringBuilder();
 				linkSb.append(referenceType).append(".htm?");
 				linkSb.append(referenceType).append("Id=").append(checkForNull(fieldValue));
 				if ( ! ReferenceType.constituent.equals(referenceType)) {
 					linkSb.append("&").append(StringConstants.CONSTITUENT_ID).append("=").append(request.getParameter(StringConstants.CONSTITUENT_ID));
 				}
 				String linkMsg = getMessage("gotoLink");
 				
 				sb.append("<a href=\"").append(linkSb.toString()).append("\" target=\"_blank\" alt=\"").append(linkMsg).append("\" title=\"").append(linkMsg).append("\">");
 				sb.append(displayValue);
 				sb.append("</a>");
 			}
 			else {
 				sb.append(fieldValue);
 			}
 		}
 		sb.append("</span>");
 	    return displayValue;
     }
 
     protected void createDeleteOption(StringBuilder sb, String displayValue) {
         if (StringUtils.hasText(displayValue)) {
 	        writeDeleteLink(sb, getDeleteClickHandler());
         }
     }
 
     protected String getDeleteClickHandler() {
         return "Lookup.deleteOption(this)";
     }
 
     protected void createLookupOptionEnd(StringBuilder sb) {
         sb.append("</div>");
     }
 
     protected void createLookupLink(SectionField currentField, String formFieldName, StringBuilder sb) {
         sb.append("<a href=\"javascript:void(0)\" onclick=\"").append(getQueryClickHandler()).append("\" fieldDef=\"");
 	    sb.append(StringEscapeUtils.escapeHtml(currentField.getFieldDefinition().getId()));
 	    
         String lookupMsg = getMessage("lookup");
         sb.append("\" class=\"hideText\" alt=\"").append(lookupMsg).append("\" title=\"").append(lookupMsg).append("\">").append(lookupMsg).append("</a>");
     }
 
     protected String getQueryClickHandler() {
         return "Lookup.loadQueryLookup(this)";
     }
 
     protected void createLookupFieldEnd(StringBuilder sb) {
         sb.append("</div>");
     }
 
     protected void createHiddenField(SectionField currentField, String formFieldName, Object fieldValue, StringBuilder sb) {
         sb.append("<input type=\"hidden\" name=\"").append(formFieldName).append("\" value=\"").append(checkForNull(fieldValue)).append("\" id=\"").append(formFieldName).append("\"/>");
     }
 
     protected void createCloneable(StringBuilder sb) {
         String removeMsg = getMessage("removeThisOption");
         sb.append("<div class=\"queryLookupOption noDisplay clone\">");
         sb.append("<span><a href=\"\" target=\"_blank\"></a></span>");
         sb.append("<a href=\"javascript:void(0)\" onclick=\"").append(getDeleteClickHandler()).append("\" class=\"deleteOption\">");
         sb.append("<img src=\"images/icons/deleteRow.png\" alt=\"").append(removeMsg).append("\" title=\"").append(removeMsg).append("\"/>");
         sb.append("</a>");
         sb.append("</div>");
     }
 
     protected void createHiearchy(HttpServletRequest request, SectionField currentField, String formFieldName, StringBuilder sb) {
         if (relationshipService.isHierarchy(currentField.getFieldDefinition())) {
             sb.append("<a href=\"javascript:void(0)\" onclick=\"Lookup.loadTreeView(this)\" divid=\"treeview-").append(formFieldName).append("\" ");
             sb.append("constituentid=\"").append(request.getParameter(StringConstants.CONSTITUENT_ID)).append("\" fieldDef=\"").append(formFieldName).append("\">");
             sb.append(getMessage("viewHierarchy"));
             sb.append("</a>");
             sb.append("<div id=\"treeview-").append(formFieldName).append("\"></div>");
         }
     }
 
 	protected void createLookupWrapperEnd(StringBuilder sb) {
 	    sb.append("</div>");
 	}
 
 }
