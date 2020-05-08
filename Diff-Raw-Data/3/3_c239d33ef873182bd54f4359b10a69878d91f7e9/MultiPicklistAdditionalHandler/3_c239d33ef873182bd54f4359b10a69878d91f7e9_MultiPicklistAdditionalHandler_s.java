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
 import com.orangeleap.tangerine.domain.customization.SectionDefinition;
 import com.orangeleap.tangerine.domain.customization.SectionField;
 import org.springframework.context.ApplicationContext;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.jsp.PageContext;
 import java.util.List;
 
 /**
  * User: alexlo
  * Date: Jul 8, 2009
  * Time: 12:55:40 PM
  */
 public class MultiPicklistAdditionalHandler extends MultiPicklistHandler {
 
 	public MultiPicklistAdditionalHandler(ApplicationContext applicationContext) {
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
 
 		/* Add the additional elements - only difference between this and MultiPicklistHandler */
 		createAdditionalFields(currentField, form, formFieldName, sb);
 		createAdditionalFieldClone(sb);
 
 		createHiddenInput(currentField, formFieldName, fieldValue, sb);
 		createContainerEnd(sb);
 		createBottom(request, pageContext, sb);
 		createSelectedRefs(formFieldName, selectedRefs, sb);
 		createLookupLink(sb);
 	}
 
 	protected void createAdditionalFields(SectionField currentField, TangerineForm form, String formFieldName, StringBuilder sb) {
 		sb.append("<div id=\"div-additional-").append(formFieldName).append("\" class=\"additionalOptions\">");
 
 		String additionalFormFieldName = resolveAdditionalFormFieldName(formFieldName);
 		Object additionalFieldValue = form.getFieldValue(additionalFormFieldName);
 
 		if (additionalFieldValue != null) {
 			Object[] additionalVals = splitValuesByCustomFieldSeparator(additionalFieldValue);
 
 			for (Object additionalVal : additionalVals) {
 				sb.append("<div class='multiPicklistOption multiOption' id=\"\">");
 				sb.append("<span>").append(additionalVal).append("</span>");
 
 				writeDeleteLink(sb, "Lookup.deleteAdditionalOption(this)");
 
 				sb.append("</div>");
 			}
 		}
 		sb.append("</div>");
 	}
 
 	protected void createAdditionalFieldClone(StringBuilder sb) {
 		sb.append("<div class='multiPicklistOption multiOption noDisplay clone' id=\"\">");
 		sb.append("<span></span>");
 		
 		writeDeleteLink(sb, "Lookup.deleteAdditionalOption(this)");
 
 		sb.append("</div>");
 	}
 
 	protected String getLookupClickHandler() {
 		return "Lookup.loadMultiPicklist(this, true)";
 	}
 }
