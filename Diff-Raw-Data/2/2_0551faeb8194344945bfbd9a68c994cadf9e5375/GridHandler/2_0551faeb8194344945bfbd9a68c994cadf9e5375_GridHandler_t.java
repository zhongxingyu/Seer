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
 
 package com.orangeleap.tangerine.web.customization.tag.fields.handlers.impl.grid;
 
 import com.orangeleap.tangerine.controller.TangerineForm;
 import com.orangeleap.tangerine.domain.customization.SectionDefinition;
 import com.orangeleap.tangerine.domain.customization.SectionField;
 import com.orangeleap.tangerine.service.customization.FieldService;
 import com.orangeleap.tangerine.type.FieldType;
 import com.orangeleap.tangerine.type.LayoutType;
 import com.orangeleap.tangerine.util.StringConstants;
 import com.orangeleap.tangerine.util.TangerineMessageAccessor;
 import com.orangeleap.tangerine.web.customization.tag.fields.handlers.FieldHandler;
 import com.orangeleap.tangerine.web.customization.tag.fields.handlers.FieldHandlerHelper;
 import org.springframework.beans.BeanWrapper;
 import org.springframework.beans.BeansException;
 import org.springframework.beans.PropertyAccessorFactory;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 import org.springframework.stereotype.Component;
 
 import javax.servlet.jsp.PageContext;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 
 /**
  * User: alexlo
  * Date: Jul 14, 2009
  * Time: 10:39:46 AM
  */
 @Component("gridHandler")
 public class GridHandler implements ApplicationContextAware {
 	private FieldService fieldService;
 	private FieldHandlerHelper fieldHandlerHelper;
 
 	@Override
 	public void setApplicationContext(ApplicationContext appContext) throws BeansException {
 		fieldService = (FieldService) appContext.getBean("fieldService");
 		fieldHandlerHelper = (FieldHandlerHelper) appContext.getBean("fieldHandlerHelper");
 	}
 
 	public void writeDistributionLinesGridBegin(String pageName, StringBuilder sb) {
 		sb.append("<div id=\"").append(pageName).append("DistributionLinesDiv\">");
 	}
 
 	public void writeGridTableBegin(SectionDefinition sectionDef, String gridTableCssClass, StringBuilder sb) {
 		sb.append("<table class=\"tablesorter ").append(gridTableCssClass).append("\" id=\"").append(sectionDef.getSectionHtmlName()).append("\" cellspacing=\"0\">");
 	}
 
 	public void writeGridCols(List<SectionField> sectionFields, boolean hasHiddenGridRow, StringBuilder sb) {
 		if (hasHiddenGridRow) {
 			sb.append("<col class=\"node\"/>");
 		}
 		for (SectionField field : sectionFields) {
 			if (FieldType.NUMBER.equals(field.getFieldType())) {
 				sb.append("<col class=\"number\"/>");
 			}
 			else if (FieldType.PERCENTAGE.equals(field.getFieldType())) {
 				sb.append("<col class=\"pct\"/>");
 			}
 			else if (FieldType.CODE.equals(field.getFieldType()) || FieldType.CODE_OTHER.equals(field.getFieldType())) {
 				sb.append("<col class=\"code\"/>");
 			}
 			else if (FieldType.QUERY_LOOKUP.equals(field.getFieldType()) || FieldType.QUERY_LOOKUP_OTHER.equals(field.getFieldType())) {
 				sb.append("<col class=\"reference\"/>");
 			}
 			else if (FieldType.TEXT.equals(field.getFieldType())) {
 				sb.append("<col class=\"text\"/>");
 			}
 			else if (FieldType.CHECKBOX.equals(field.getFieldType())) {
 				sb.append("<col class=\"text\"/>");
 			}
 		}
 		sb.append("<col class=\"button\"/>");
 	}
 
 	// TODO: grid sorting
 	public void writeGridHeader(PageContext pageContext, List<SectionField> sectionFields, boolean hasHiddenGridRow, StringBuilder sb) {
 		sb.append("<thead><tr>");
 		if (hasHiddenGridRow) {
 			sb.append("<th class=\"actionColumn\">&nbsp;</th>");
 		}
 		for (SectionField field : sectionFields) {
 			if (FieldType.HIDDEN.equals(field.getFieldType())) {
 				sb.append("<th class=\"noDisplay\">&nbsp;</th>");
 			}
 			else {
 				sb.append("<th>");
 				if (fieldService.isFieldRequired(field)) {
 					sb.append("<span class=\"required\">*</span>&nbsp;");
 				}
 				String label = field.getFieldDefinition().getDefaultLabel();
 				FieldHandler fieldHandler = fieldHandlerHelper.lookupFieldHandler(field.getFieldType());
 				if (fieldHandler != null) {
 					label = fieldHandler.resolveLabelText(pageContext, field);
 				}
 				sb.append("<a>").append(label).append("</a>");
 				sb.append("</th>");
 			}
 		}
 		sb.append("<th></th>");
 		sb.append("</tr></thead>");
 	}
 
 	public void writeGridTableBody(PageContext pageContext, SectionDefinition gridSectionDef, List<SectionField> gridSectionFields,
 	                               SectionDefinition hiddenSectionDef, List<SectionField> hiddenSectionFields,
 	                               TangerineForm form, boolean hasHiddenGridRow, boolean isDummy, StringBuilder sb) {
 		if ( ! gridSectionFields.isEmpty()) {
 			String collectionFieldName = gridSectionFields.get(0).getFieldDefinition().getFieldName();
 	        BeanWrapper bean = PropertyAccessorFactory.forBeanPropertyAccess(form.getDomainObject());
 
 			int totalRows = 1;
 			if ( ! isDummy && bean.isReadableProperty(collectionFieldName) && bean.getPropertyValue(collectionFieldName) instanceof Collection) {
 				Collection coll = (Collection) bean.getPropertyValue(collectionFieldName);
 				totalRows = coll.size();
 			}
 
 			for (int rowCounter = 0; rowCounter < totalRows; rowCounter++) {
 				sb.append("<tbody class=\"");
 				if (hasHiddenGridRow) {
 					sb.append("expandable ");
 				}
 				if (isDummy) {
 					sb.append("noDisplay ");
 				}
 				else {
 					sb.append("gridRow ");
 				}
 				sb.append("\" id=\"");
 				if (isDummy) {
 					sb.append("gridCloneRow");
 				}
 				else {
 					sb.append("gridRow").append(rowCounter);
 				}
 				sb.append("\">");
 				writeLineRow(pageContext, gridSectionDef, gridSectionFields, form, hasHiddenGridRow, isDummy, totalRows, rowCounter, sb);
 				writeHiddenRow(pageContext, hiddenSectionDef, hiddenSectionFields, form, isDummy, rowCounter, gridSectionFields.size(), sb);
 				sb.append("</tbody>");
 			}
 		}
 	}
 
 	public void writeLineRow(PageContext pageContext, SectionDefinition sectionDef, List<SectionField> sectionFields, TangerineForm form,
 	                            boolean hasHiddenGridRow, boolean isDummy, int totalSize,
 	                            int rowCounter, StringBuilder sb) {
 		sb.append("<tr class=\"lineRow");
 		if (hasHiddenGridRow) {
 			sb.append(" expandableRow collapsed");
 		}
 		sb.append("\">");
 		String clickMsg = TangerineMessageAccessor.getMessage("clickShowHideExtended");
 		if (hasHiddenGridRow) {
 			sb.append("<td class=\"nodeLink\"><a href=\"#\" class=\"treeNodeLink plus\" title=\"").append(clickMsg);
 			sb.append("\" rowIndex=\"").append(rowCounter).append("\">").append(clickMsg).append("</a></td>");
 		}
 
 		for (SectionField field : sectionFields) {
 			FieldHandler fieldHandler = fieldHandlerHelper.lookupFieldHandler(field.getFieldType());
 			if (fieldHandler != null) {
 				sb.append("<td");
 				if (FieldType.HIDDEN.equals(field.getFieldType())) {
 					sb.append(" class=\"noDisplay\"");
 				}
 				sb.append(">");
 
 				boolean showSideAndLabel = false;
 
 				fieldHandler.handleField(pageContext, sectionDef, sectionFields, field, form, showSideAndLabel,
 						isDummy, rowCounter, sb);
 				sb.append("</td>");
 			}
 		}
 
 		String removeMsg = TangerineMessageAccessor.getMessage("removeThisOption");
 		sb.append("<td><a href=\"#\" class=\"deleteButton");
		if (rowCounter == 0 && totalSize == 1) {
 			sb.append(" noDisplay");
 		}
 		sb.append("\"><img src=\"images/icons/deleteRow.png\" alt=\"").append(removeMsg).append("\" title=\"").append(removeMsg).append("\"/></a></td>");
 		sb.append("</tr>");
 	}
 
 	public void writeHiddenRow(PageContext pageContext, SectionDefinition hiddenSectionDef,
 	                           List<SectionField> hiddenSectionFields, TangerineForm form,
 	                           boolean isDummy, int rowCounter, int colspan, StringBuilder sb) {
 		sb.append("<tr class=\"hiddenRow noDisplay\">");
 		sb.append("<td colspan=\"").append(colspan + 2).append("\">");
 		sb.append("<table>");
 		sb.append("<tr>");
 		sb.append("<td>");
 		sb.append("<div class=\"columns\">");
 		sb.append("<div class=\"column\">");
 		sb.append("<ul class=\"formFields width350\">");
 		writeHiddenRowColumn(pageContext, hiddenSectionDef, hiddenSectionFields, form, true, isDummy, rowCounter, sb);
 		sb.append("<li class=\"clear\"></li>");
 		sb.append("</ul>");
 		sb.append("</div>"); // end first column
 		sb.append("<div class=\"column\">");
 		sb.append("<ul class=\"formFields width350\">");
 		writeHiddenRowColumn(pageContext, hiddenSectionDef, hiddenSectionFields, form, false, isDummy, rowCounter, sb);
 		sb.append("<li class=\"clear\"></li>");
 		sb.append("</ul>");
 		sb.append("</div>"); // end second column
 		sb.append("</div>"); // end columns
 		sb.append("</td>");
 		sb.append("</tr>");
 		sb.append("</table>");
 		sb.append("</td>");
 		sb.append("</tr>");
 	}
 
 	public void writeHiddenRowColumn(PageContext pageContext, SectionDefinition hiddenRowSectionDef,
 	                                 List<SectionField> hiddenRowSectionFields, TangerineForm form,
 	                                 boolean firstColumn, boolean isDummy, int rowCounter, StringBuilder sb) {
 		boolean showSideAndLabel = true;
 
 		Map<String, List<SectionField>> groupedSectionFields = fieldService.groupSectionFields(hiddenRowSectionFields);
 		List<SectionField> hiddenFields = groupedSectionFields.get(StringConstants.HIDDEN);
 
 		/* Display the hidden fields in the first column ONLY */
 		if (firstColumn) {
 			for (SectionField hiddenFld : hiddenFields) {
 				FieldHandler fieldHandler = fieldHandlerHelper.lookupFieldHandler(hiddenFld.getFieldType());
 				if (fieldHandler != null) {
 					fieldHandler.handleField(pageContext, hiddenRowSectionDef, hiddenFields, hiddenFld, form,
 							showSideAndLabel, isDummy, rowCounter, sb);
 				}
 			}
 		}
 
 		List<SectionField> displayedFields = groupedSectionFields.get(StringConstants.DISPLAYED);
 
 		int begin = 0;
 		int end = displayedFields.size();
 		int split = (int) Math.ceil(((float)displayedFields.size()) / ((float)2));
 		if (firstColumn) {
 			end = split;
 		}
 		else {
 			begin = split;
 		}
 
 		for (int x = begin; x < end; x++) {
 			SectionField displayedFld = displayedFields.get(x);
 			FieldHandler fieldHandler = fieldHandlerHelper.lookupFieldHandler(displayedFld.getFieldType());
 			if (fieldHandler != null) {
 				fieldHandler.handleField(pageContext, hiddenRowSectionDef, displayedFields, displayedFld, form,
 						showSideAndLabel, isDummy, rowCounter, sb);
 			}
 		}
 	}
 
 	public void writeGridTableEnd(StringBuilder sb) {
 		sb.append("</table>");
 	}
 
 	public void writeGridActions(LayoutType layoutType, StringBuilder sb) {
 		sb.append("<div class=\"gridActions\">");
 		sb.append("<div id=\"totalText\">");
 		sb.append(TangerineMessageAccessor.getMessage("total")).append("&nbsp;");
 		sb.append("<span class=\"warningText\" id=\"");
 
 		String msg = StringConstants.EMPTY;
 		if (LayoutType.DISTRIBUTION_LINE_GRID.equals(layoutType)) {
 			sb.append("amountsErrorSpan");
 			msg = TangerineMessageAccessor.getMessage("mustMatchGiftValue");
 		}
 		else if (LayoutType.GIFT_IN_KIND_GRID.equals(layoutType)) {
 			sb.append("valueErrorSpan");
 			msg = TangerineMessageAccessor.getMessage("mustMatchFairMarketValue");
 		}
 		sb.append("\">").append(msg).append("</span>");
 		sb.append("</div>");
 		sb.append("<div class=\"value\" id=\"subTotal\">0</div>");
 
 		if (LayoutType.DISTRIBUTION_LINE_GRID.equals(layoutType)) {
 			sb.append("<span id=\"totalContributionInfo\">");
 			sb.append("<div id=\"totalContributionText\">").append(TangerineMessageAccessor.getMessage("totalContribution")).append("</div>");
 			sb.append("<div class=\"value\" id=\"totalContribution\">0</div>");
 			sb.append("</span>");
 			sb.append("<script type=\"text/javascript\">Ext.fly('totalContributionInfo').hide();</script>");// TODO: why not just hide the element?
 		}
 		sb.append("</div>");
 	}
 
 	public void writeDistributionLinesGridEnd(StringBuilder sb) {
 		sb.append("</div>");
 	}
 
 }
