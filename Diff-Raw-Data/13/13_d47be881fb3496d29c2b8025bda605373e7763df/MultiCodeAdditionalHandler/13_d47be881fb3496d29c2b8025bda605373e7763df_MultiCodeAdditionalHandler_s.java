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
 import org.springframework.context.ApplicationContext;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.jsp.PageContext;
 import java.util.List;
 
 /**
  * User: alexlo
  * Date: Jul 9, 2009
  * Time: 4:03:26 PM
  */
 public class MultiCodeAdditionalHandler extends CodeHandler {
 	
 	public MultiCodeAdditionalHandler(ApplicationContext applicationContext) {
 		super(applicationContext);
 	}
 
 	@Override
 	protected void doHandler(HttpServletRequest request, HttpServletResponse response, PageContext pageContext,
 	                      SectionDefinition sectionDefinition, List<SectionField> sectionFields, SectionField currentField,
 	                      TangerineForm form, String formFieldName, Object fieldValue, StringBuilder sb) {
 		createTop(request, pageContext, formFieldName, sb);
 		createContainerBegin(request, pageContext, formFieldName, sb);
 		createMultiCodeBegin(currentField, sb);
 		createLeft(sb);
 		createCodeOptions(currentField, fieldValue, sb);
 		createAdditionalOptions(currentField, form, formFieldName, sb);
 		createRight(sb);
 		createMultiCodeEnd(sb);
 		createHiddenInput(formFieldName, fieldValue, sb);
 		createClone(sb);
 		createContainerEnd(sb);
 		createBottom(request, pageContext, formFieldName, sb);
 		createLookupLink(currentField, sb);
 	}
 
 	@Override
 	protected String getSideCssClass(Object fieldValue) {
 		return "multiOptionLi";
 	}
 
 	protected void createTop(HttpServletRequest request, PageContext pageContext, String formFieldName, StringBuilder sb) {
 	    sb.append("<div class=\"lookupScrollTop ");
 	    writeErrorClass(pageContext, formFieldName, sb);
 	    sb.append("\"></div>");
 	}
 
 	protected void createContainerBegin(HttpServletRequest request, PageContext pageContext, String formFieldName, StringBuilder sb) {
 	    sb.append("<div class=\"lookupScrollContainer ");
 		writeErrorClass(pageContext, formFieldName, sb);
 		sb.append("\">");
 	}
 
 	protected void createMultiCodeBegin(SectionField currentField, StringBuilder sb) {
 	    sb.append("<div class=\"multiCode multiLookupField ");
 		sb.append(resolveEntityAttributes(currentField));
 		sb.append("\">");
 	}
 
 	protected void createLeft(StringBuilder sb) {
 	    sb.append("<div class=\"lookupScrollLeft\"></div>");
 	}
 
 	protected void createCodeOptions(SectionField currentField, Object fieldValue, StringBuilder sb) {
		if (fieldValue != null) {
 			Object[] fieldVals = splitValuesByCustomFieldSeparator(fieldValue);
 
//			String fieldPropertyName = resolveFieldPropertyName(currentField);

 			for (Object val : fieldVals) {
 				String displayValue = resolveCodeValue(currentField.getFieldPropertyName(), val.toString());
 				sb.append("<div class='multiCodeOption multiOption' id=\"option-").append(val).append("\" code=\"").append(val).append("\">");
 				sb.append("<span>").append(displayValue).append("</span>");
 
 				writeDeleteLink(sb, "Lookup.deleteCode(this)");
 
 				sb.append("</div>");
 			}
 		}
 	}
 
 	protected void createAdditionalOptions(SectionField currentField, TangerineForm form, String formFieldName, StringBuilder sb) {
 		sb.append("<div id=\"div-additional-").append(formFieldName).append("\" class=\"additionalOptions\">");
 
 		String additionalFormFieldName = resolveAdditionalFormFieldName(formFieldName);
 		Object additionalFieldValue = form.getFieldValueFromUnescapedFieldName(additionalFormFieldName);
 
		if (additionalFieldValue != null) {
 			Object[] additionalVals = splitValuesByCustomFieldSeparator(additionalFieldValue);
 
 			int x = 0;
 			for (Object additionalVal : additionalVals) {
				sb.append("<div class='multiCodeOption multiOption' id=\"additional-").append(x++).append("\" code=\"").append(additionalVal).append(">");
 				sb.append("<span>").append(additionalVal).append("</span>");
 
 				writeDeleteLink(sb, "Lookup.deleteCode(this)");
 
 				sb.append("</div>");
 			}
 		}
 		sb.append("</div>");
 	}
 
 	protected void createRight(StringBuilder sb) {
 	    sb.append("<div class=\"lookupScrollRight\"></div>");
 	}
 
 	protected void createMultiCodeEnd(StringBuilder sb) {
 		sb.append("</div>");
 	}
 
 	@Override
 	protected void createHiddenInput(String formFieldName, Object fieldValue, StringBuilder sb) {
 	    sb.append("<input type=\"hidden\" name=\"").append(formFieldName).append("\" id=\"").append(formFieldName).append("\" value=\"").append(checkForNull(fieldValue)).append("\" ");
 		sb.append("additionalFieldId=\"").append(resolveAdditionalFormFieldName(formFieldName)).append("\"");
 		sb.append("\"/>");
 	}
 
 	protected void createClone(StringBuilder sb) {
 		sb.append("<div class=\"multiCodeOption multiOption noDisplay clone\" id=\"\" code=\"\">");
 		sb.append("<span></span>");
 		writeDeleteLink(sb, "Lookup.deleteCode(this)");
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
 		sb.append("<a href=\"javascript:void(0)\" onclick=\"Lookup.loadCodeAdditionalPopup(this)\" class=\"multiLookupLink hideText\" ");
 		sb.append("lookup=\"").append(currentField.getFieldPropertyName()).append("\" ");
 		sb.append("alt=\"").append(lookupMsg).append("\" title=\"").append(lookupMsg).append("\">").append(lookupMsg).append("</a>");
 	}
 }
