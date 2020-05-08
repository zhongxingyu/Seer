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
 
 package com.orangeleap.tangerine.web.customization.tag.fields.handlers.impl.display;
 
 import com.orangeleap.tangerine.controller.TangerineForm;
 import com.orangeleap.tangerine.domain.customization.Picklist;
 import com.orangeleap.tangerine.domain.customization.PicklistItem;
 import com.orangeleap.tangerine.domain.customization.SectionDefinition;
 import com.orangeleap.tangerine.domain.customization.SectionField;
 import com.orangeleap.tangerine.web.customization.tag.fields.handlers.impl.picklists.PicklistHandler;
 import org.apache.commons.lang.StringEscapeUtils;
 import org.springframework.context.ApplicationContext;
 import org.springframework.util.StringUtils;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.jsp.PageContext;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 
 /**
  * User: alexlo
  * Date: Jul 9, 2009
  * Time: 6:02:38 PM
  */
 public class PicklistDisplayHandler extends PicklistHandler {
 
 	public PicklistDisplayHandler(ApplicationContext applicationContext) {
 		super(applicationContext);
 	}
 
 	@Override
 	protected void doHandler(HttpServletRequest request, HttpServletResponse response, PageContext pageContext,
 	                      SectionDefinition sectionDefinition, List<SectionField> sectionFields, SectionField currentField,
 	                      TangerineForm form, String formFieldName, Object fieldValue, StringBuilder sb) {
 		Picklist picklist = resolvePicklist(currentField, pageContext);
 		createPicklistBegin(currentField, picklist, formFieldName, sb);
 		String selectedRef = createPicklistOptions(pageContext, picklist, fieldValue, sb);
 		createPicklistEnd(sb);
 		createSelectedRef(formFieldName, selectedRef, sb);
 	}
 
 	protected void createPicklistBegin(SectionField currentField, Picklist picklist, String formFieldName, StringBuilder sb)  {
		sb.append("<div class=\"readOnlyField multiPicklist ").append(resolveEntityAttributes(currentField)).append("\" ");
 		sb.append("id=\"") .append(formFieldName).append("\" references=\"");
 
 		Set<String> refSb = new TreeSet<String>();
 		if (picklist != null) {
 			for (PicklistItem item : picklist.getActivePicklistItems()) {
 				if (StringUtils.hasText(item.getReferenceValue())) {
 					refSb.add(item.getReferenceValue());
 				}
 			}
 		}
 		sb.append(StringUtils.collectionToCommaDelimitedString(refSb));
 		sb.append("\">");
 	}
 
 	protected String createPicklistOptions(PageContext pageContext, Picklist picklist, Object fieldValue, StringBuilder sb) {
 		Set<String> selectedRefs = new TreeSet<String>();
 		if (picklist != null) {
 			for (PicklistItem item : picklist.getActivePicklistItems()) {
 				sb.append("<div class=\"multiPicklistOption multiOption\" style=\"");
 
 				Object[] fieldVals = splitValuesByCustomFieldSeparator(fieldValue);
 
 				boolean foundValue = false;
 				for (Object val : fieldVals) {
 					if (val != null) {
 						if (item.getItemName().equals(val.toString())) {
 							foundValue = true;
 							break;
 						}
 					}
 				}
 
 				if (foundValue) {
 					if (StringUtils.hasText(item.getReferenceValue())) {
 						selectedRefs.add(item.getReferenceValue());
 					}
 				}
 				else {
 					sb.append("display:none");
 				}
 				String itemName = StringEscapeUtils.escapeHtml(item.getItemName());
 				sb.append("\" id=\"option-").append(itemName).append("\" selectedId=\"").append(itemName).append("\" reference=\"").append(checkForNull(item.getReferenceValue())).append("\">");
 
 				String displayValue = resolvePicklistItemDisplayValue(item, pageContext);
 				sb.append(displayValue);
 				sb.append("</div>");
 			}
 		}
 		return StringUtils.collectionToCommaDelimitedString(selectedRefs);
 	}
 
 	protected void createPicklistEnd(StringBuilder sb) {
 		sb.append("</div>");
 	}
 
 	protected void createSelectedRef(String formFieldName, String selectedRef, StringBuilder sb) {
 		sb.append("<div class=\"noDisplay\" id=\"selectedRef-").append(formFieldName).append("\">").append(selectedRef).append("</div>");
 	}
 }
