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
 import com.orangeleap.tangerine.domain.customization.SectionDefinition;
 import com.orangeleap.tangerine.domain.customization.SectionField;
 import com.orangeleap.tangerine.domain.paymentInfo.AdjustedGift;
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
 import org.apache.commons.logging.Log;
 import org.springframework.context.ApplicationContext;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.jsp.PageContext;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * User: alexlo
  * Date: Jul 9, 2009
  * Time: 4:03:26 PM
  */
 public class SelectionHandler extends AbstractFieldHandler {
 
 	protected final Log logger = OLLogger.getLog(getClass());
 	protected final PledgeService pledgeService;
 	protected final RecurringGiftService recurringGiftService;
 	protected final GiftService giftService;
 
 	public SelectionHandler(ApplicationContext applicationContext) {
 		super(applicationContext);
 		pledgeService = (PledgeService) applicationContext.getBean("pledgeService");
 		recurringGiftService = (RecurringGiftService) applicationContext.getBean("recurringGiftService");
 		giftService = (GiftService) applicationContext.getBean("giftService");
 	}
 
 	@Override
 	protected void doHandler(HttpServletRequest request, HttpServletResponse response, PageContext pageContext,
 	                      SectionDefinition sectionDefinition, List<SectionField> sectionFields, SectionField currentField,
 	                      TangerineForm form, String formFieldName, Object fieldValue, StringBuilder sb) {
 		createTop(request, pageContext, sb);
 		createContainerBegin(request, pageContext, sb);
 		createSelectionBegin(currentField, sb);
 		createLeft(sb);
 		createSelectionOptions(request, currentField, fieldValue, sb);
 		createRight(sb);
 		createSelectionEnd(sb);
 
 		if ( ! FieldType.SELECTION_DISPLAY.equals(currentField.getFieldType())) {
 			createHiddenInput(formFieldName, fieldValue, sb);
 			createClone(sb);
 		}
 		createContainerEnd(sb);
 		createBottom(request, pageContext, sb);
 
 		if ( ! FieldType.SELECTION_DISPLAY.equals(currentField.getFieldType())) {
 			createLookupLink(currentField, sb);
 		}
 	}
 
 	@Override
 	protected String getSideCssClass(Object fieldValue) {
 		return new StringBuilder(super.getSideCssClass(fieldValue)).append(" multiOptionLi").toString();
 	}
 
 	protected void createTop(HttpServletRequest request, PageContext pageContext, StringBuilder sb) {
 	    sb.append("<div class=\"lookupScrollTop ");
 	    writeErrorClass(request, pageContext, sb);
 	    sb.append("\"></div>");
 	}
 
 	protected void createContainerBegin(HttpServletRequest request, PageContext pageContext, StringBuilder sb) {
 	    sb.append("<div class=\"lookupScrollContainer ").append(getContainerCssClass());
 		writeErrorClass(request, pageContext, sb);
 		sb.append("\">");
 	}
 
 	protected String getContainerCssClass() {
 		return StringConstants.EMPTY;
 	}
 
 	protected void createSelectionBegin(SectionField currentField, StringBuilder sb) {
 	    sb.append("<div class=\"multiLookupField ");
 		sb.append(resolveEntityAttributes(currentField));
 		sb.append("\">");
 	}
 
 	protected void createLeft(StringBuilder sb) {
 	    sb.append("<div class=\"lookupScrollLeft\"></div>");
 	}
 
 	@SuppressWarnings("unchecked")
 	protected void createSelectionOptions(HttpServletRequest request, SectionField currentField, Object fieldValue, StringBuilder sb) {
 		if (fieldValue != null) {
 			final List<String> displayValues = new ArrayList<String>();
 			final List<Long> ids = new ArrayList<Long>();
 			final List<String> links = new ArrayList<String>();
 
 			ReferenceType referenceType = currentField.getFieldDefinition().getReferenceType();
 			if (ReferenceType.adjustedGift.equals(referenceType)) {
 			    List<AdjustedGift> adjustedGifts = (List<AdjustedGift>) fieldValue;
 			    for (AdjustedGift aAdjGift : adjustedGifts) {
 			        displayValues.add(aAdjGift.getShortDescription());
 			        ids.add(aAdjGift.getId());
 
 				    links.add(new StringBuilder("giftAdjustmentView.htm?").append(referenceType).append("Id=").append(aAdjGift.getId()).append("&").
 						    append(StringConstants.CONSTITUENT_ID).append("=").append(request.getParameter(StringConstants.CONSTITUENT_ID)).toString());
 			    }
 			}
 			else {
 			    final List<Long> refIds = (List<Long>) fieldValue;
 			    for (Long refId : refIds) {
 			        if (ReferenceType.pledge.equals(referenceType)) {
 			            Pledge pledge = pledgeService.readPledgeById(refId);
 			            displayValues.add(pledge.getShortDescription());
 			        } 
 			        else if (ReferenceType.recurringGift.equals(referenceType)) {
 			            RecurringGift recurringGift = recurringGiftService.readRecurringGiftById(refId);
 			            displayValues.add(recurringGift.getShortDescription());
 			        }
 			        else if (ReferenceType.gift.equals(referenceType)) {
 			            Gift gift = giftService.readGiftById(refId);
 			            displayValues.add(gift.getShortDescription());
 			        }
 			        ids.add(refId);
 				    
 				    links.add(new StringBuilder(referenceType.toString()).append(".htm?").append(referenceType).append("Id=").append(refId).append("&").
 						    append(StringConstants.CONSTITUENT_ID).append("=").append(request.getParameter(StringConstants.CONSTITUENT_ID)).toString());
 			    }
 			}
 			String gotoMsg = getMessage("gotoLink");
 
 			for (int i = 0; i < ids.size(); i++) {
 				Long thisId = ids.get(i);
 				String displayVal = displayValues.get(i);
 				String link = links.get(i);
 
 				sb.append("<div class='multiQueryLookupOption multiOption' id=\"lookup-").append(thisId).append("\" selectedId=\"").append(thisId).append("\">");
 				sb.append("<a href=\"").append(link).append("\" target=\"_blank\" alt=\"").append(gotoMsg).append("\" title=\"").append(gotoMsg).append("\">").append(displayVal).append("</a>");
 
 				if ( ! FieldType.SELECTION_DISPLAY.equals(currentField.getFieldType())) {
 					writeDeleteLink(sb, "PledgeRecurringGiftSelector.deleteThis(this)");
 				}
 
 				sb.append("</div>");
 			}
 		}
 	}
 
 	protected void createRight(StringBuilder sb) {
 	    sb.append("<div class=\"lookupScrollRight\"></div>");
 	}
 
 	protected void createSelectionEnd(StringBuilder sb) {
 		sb.append("</div>");
 	}
 
 	protected void createHiddenInput(String formFieldName, Object fieldValue, StringBuilder sb) {
 	    sb.append("<input type=\"hidden\" name=\"").append(formFieldName).append("\" id=\"").append(formFieldName).append("\" value=\"").append(checkForNull(fieldValue)).append("\"/>");
 	}
 
 	protected void createClone(StringBuilder sb) {
 		sb.append("<div class=\"multiQueryLookupOption multiOption noDisplay clone\" selectedId=\"\">");
 		sb.append("<a href=\"\" target=\"_blank\"></a>");
 		writeDeleteLink(sb, "PledgeRecurringGiftSelector.deleteThis(this)");
 		sb.append("</div>");
 	}
 
 	protected void createContainerEnd(StringBuilder sb) {
 	    sb.append("</div>");
 	}
 
 	protected void createBottom(HttpServletRequest request, PageContext pageContext, StringBuilder sb) {
 	    sb.append("<div class=\"lookupScrollBottom ");
 		writeErrorClass(request, pageContext, sb);
 	    sb.append("\"></div>");
 	}
 
 	protected void createLookupLink(SectionField currentField, StringBuilder sb) {
 		String lookupMsg = getMessage("lookup");
		sb.append("<a href=\"javascript:void(0)\" fieldDef=\"").append(currentField.getFieldPropertyName()).append("\" class=\"multiLookupLink hideText selectorLookup\" ");
 		sb.append("alt=\"").append(lookupMsg).append("\" title=\"").append(lookupMsg).append("\">").append(lookupMsg).append("</a>");
 	}
 }
