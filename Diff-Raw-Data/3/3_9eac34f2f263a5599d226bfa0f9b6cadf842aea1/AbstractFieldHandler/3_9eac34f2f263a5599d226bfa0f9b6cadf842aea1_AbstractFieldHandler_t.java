 package com.orangeleap.tangerine.web.customization.tag.fields.handlers.impl;
 
 import com.orangeleap.tangerine.controller.TangerineForm;
 import com.orangeleap.tangerine.domain.customization.EntityDefault;
 import com.orangeleap.tangerine.domain.customization.SectionDefinition;
 import com.orangeleap.tangerine.domain.customization.SectionField;
 import com.orangeleap.tangerine.service.ConstituentService;
 import com.orangeleap.tangerine.service.RelationshipService;
 import com.orangeleap.tangerine.service.SiteService;
 import com.orangeleap.tangerine.service.customization.FieldService;
 import com.orangeleap.tangerine.service.customization.MessageService;
 import com.orangeleap.tangerine.type.LayoutType;
 import com.orangeleap.tangerine.type.MessageResourceType;
 import com.orangeleap.tangerine.util.OLLogger;
 import com.orangeleap.tangerine.util.StringConstants;
 import com.orangeleap.tangerine.util.TangerineMessageAccessor;
 import com.orangeleap.tangerine.web.customization.tag.fields.handlers.FieldHandler;
 import org.apache.commons.lang.StringEscapeUtils;
 import org.apache.commons.logging.Log;
 import org.springframework.beans.BeanWrapper;
 import org.springframework.beans.PropertyAccessorFactory;
 import org.springframework.context.ApplicationContext;
 import org.springframework.util.StringUtils;
 import org.springframework.validation.Errors;
 import org.springframework.web.servlet.support.RequestContext;
 import org.springframework.web.servlet.tags.RequestContextAwareTag;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.jsp.PageContext;
 import java.util.Collection;
 import java.util.List;
 
 /**
  * User: alex lo
  * Date: Jul 2, 2009
  * Time: 6:04:17 PM
  */
 public abstract class AbstractFieldHandler implements FieldHandler {
 	
 	protected final Log logger = OLLogger.getLog(getClass());
 	protected FieldService fieldService;
 	protected SiteService siteService;
 	protected MessageService messageService;
 	protected ConstituentService constituentService;
 	protected RelationshipService relationshipService;
 
 	protected AbstractFieldHandler(ApplicationContext applicationContext) {
 		constituentService = (ConstituentService) applicationContext.getBean("constituentService");
 		messageService = (MessageService) applicationContext.getBean("messageService");
 		fieldService = (FieldService) applicationContext.getBean("fieldService");
 		siteService = (SiteService) applicationContext.getBean("siteService");
 		relationshipService = (RelationshipService) applicationContext.getBean("relationshipService");
 	}
 
 	@Override
 	public void handleField(PageContext pageContext,
 	                     SectionDefinition sectionDefinition, List<SectionField> sectionFields,
 	                     SectionField currentField, TangerineForm form, StringBuilder sb) {
 
 		String unescapedFieldName = resolveUnescapedFieldName(sectionDefinition, currentField, form);
 		String formFieldName = TangerineForm.escapeFieldName(unescapedFieldName);
 		Object fieldValue = form.getFieldValue(formFieldName);
 
 		writeSideLiElementStart(currentField, fieldValue, sb);
 		writeLabel(currentField, pageContext, sb);
 
 		doHandler((HttpServletRequest) pageContext.getRequest(), (HttpServletResponse) pageContext.getResponse(),
 				pageContext, sectionDefinition, sectionFields, currentField, form, formFieldName, fieldValue, sb);
 		
 		writeSideLiElementEnd(sb);
 	}
 
 	@Override
 	public void handleField(PageContext pageContext,
 	                     SectionDefinition sectionDefinition, List<SectionField> sectionFields,
 	                     SectionField currentField, TangerineForm form, boolean showSideAndLabel,
 	                     boolean isDummy, int rowCounter, StringBuilder sb) {
 		String unescapedFieldName = resolveUnescapedFieldName(sectionDefinition, currentField, form, rowCounter, isDummy);
 		String formFieldName = TangerineForm.escapeFieldName(unescapedFieldName);
 
 		Object fieldValue = null;
 		if (isDummy) {
 			EntityDefault entityDefault = siteService.readEntityDefaultByTypeNameSectionDef(currentField);
 			if (entityDefault != null) {
 				fieldValue = siteService.getDefaultValue(entityDefault, PropertyAccessorFactory.forBeanPropertyAccess(form.getDomainObject()), null);
 			}
 		}
 		else {
 			fieldValue = form.getFieldValue(formFieldName);
 		}
 
 		if (showSideAndLabel) {
 			writeSideLiElementStart(currentField, fieldValue, sb);
 			writeLabel(currentField, pageContext, sb);
 		}
 
 		doHandler((HttpServletRequest) pageContext.getRequest(), (HttpServletResponse) pageContext.getResponse(),
 				pageContext, sectionDefinition, sectionFields, currentField, form, formFieldName, fieldValue, sb);
 
 		if (showSideAndLabel) {
 			writeSideLiElementEnd(sb);
 		}
 	}
 
 
 	protected abstract void doHandler(HttpServletRequest request, HttpServletResponse response, PageContext pageContext,
 	                                  SectionDefinition sectionDefinition, List<SectionField> sectionFields,
 	                     SectionField currentField, TangerineForm form, String formFieldName, Object fieldValue, StringBuilder sb);
 
 
 	protected String resolveUnescapedFieldName(SectionDefinition sectionDef, SectionField currentField, TangerineForm form) {
 		return resolveUnescapedFieldName(sectionDef, currentField, form, 0, false);
 	}
 
 	protected String resolveUnescapedFieldName(SectionDefinition sectionDef, SectionField currentField, TangerineForm form,
 	                                       int rowCounter, boolean isDummy) {
 		String unescapedFieldName = null;
 		if (LayoutType.isGridType(sectionDef.getLayoutType())) {
 			String collectionFieldName = currentField.getFieldDefinition().getFieldName();
 			BeanWrapper bean = PropertyAccessorFactory.forBeanPropertyAccess(form.getDomainObject());
 
 			if (bean.isReadableProperty(collectionFieldName) && bean.getPropertyValue(collectionFieldName) instanceof Collection) {
 				StringBuilder key = new StringBuilder();
 				if (isDummy) {
 					key.append("tangDummy-");
 				}
 				key.append(collectionFieldName);
 				key.append("[").append(rowCounter).append("]");
 
 				if (currentField.getSecondaryFieldDefinition() != null) {
 					key.append(".").append(currentField.getSecondaryFieldDefinition().getFieldName());
 				}
 				unescapedFieldName = key.toString();
 			}
 		}
 		if (unescapedFieldName == null) {
 			unescapedFieldName = currentField.getFieldPropertyName();
 		}
 		if ((currentField.isCompoundField() && currentField.getSecondaryFieldDefinition().isCustom()) ||
 		        (!currentField.isCompoundField() && currentField.getFieldDefinition().isCustom())) {
 		    unescapedFieldName = new StringBuilder(unescapedFieldName).append(StringConstants.DOT_VALUE).toString();
 		}
 		return unescapedFieldName;
 	}                                                                                                           
 
 	protected String getMessage(String code) {
 	    return getMessage(code, null);
 	}
 
 	protected String getMessage(String code, String[] args) {
 	    return TangerineMessageAccessor.getMessage(code, args);
 	}
 
 	protected String checkForNull(Object obj) {
 		String escVal = StringConstants.EMPTY;
 		if (obj != null) {
 			if (obj instanceof Collection) {
 				if (!((Collection) obj).isEmpty()) {
 					escVal = StringUtils.collectionToCommaDelimitedString((Collection) obj);
 				}
 			}
 			else {
 				escVal = obj.toString();
 			}
 		}
 	    return StringEscapeUtils.escapeHtml(escVal);
 	}
 	
 	public String resolvedPrefixedFieldName(String prefix, String aFieldName) {
 	    String prefixedFieldName = null;
 
 	    boolean endsInValue = false;
 		final String escapedDotValue = TangerineForm.escapeFieldName(StringConstants.DOT_VALUE);
 	    if (aFieldName.endsWith(escapedDotValue)) {
 	        aFieldName = aFieldName.substring(0, aFieldName.length() - escapedDotValue.length());
 	        endsInValue = true;
 	    }
 
 	    int startBracketIndex = aFieldName.lastIndexOf(TangerineForm.TANG_START_BRACKET);
 	    if (startBracketIndex > -1) {
 	        int periodIndex = aFieldName.indexOf(TangerineForm.TANG_DOT, startBracketIndex);
 	        if (periodIndex > -1) {
 	            prefixedFieldName = new StringBuilder(aFieldName.substring(0, periodIndex + TangerineForm.TANG_DOT.length())).
 			            append(prefix).append(aFieldName.substring(periodIndex + TangerineForm.TANG_DOT.length(), aFieldName.length())).toString();
 	        }
 	        else {
 	            prefixedFieldName = new StringBuilder(aFieldName.substring(0, startBracketIndex + TangerineForm.TANG_START_BRACKET.length())).
 			            append(prefix).append(aFieldName.substring(startBracketIndex + TangerineForm.TANG_START_BRACKET.length(), aFieldName.length())).toString();
 	        }
 	    }
 	    if (prefixedFieldName == null) {
 	        prefixedFieldName = new StringBuilder(prefix).append(aFieldName).toString();
 	    }
 	    if (endsInValue) {
 	        prefixedFieldName = prefixedFieldName.concat(escapedDotValue);
 	    }
 	    return prefixedFieldName;
 	}
 
 	public String resolveOtherFormFieldName(String formFieldName) {
 		return resolvedPrefixedFieldName(StringConstants.OTHER_PREFIX, formFieldName);
 	}
 
 	public String resolveAdditionalFormFieldName(String formFieldName) {
 		return resolvedPrefixedFieldName(StringConstants.ADDITIONAL_PREFIX, formFieldName);
 	}
 
 	public String resolveEntityAttributes(SectionField currentField) {
 		String entityAttributes = currentField.getFieldDefinition().getEntityAttributes();
 		StringBuilder entityAttributesStyle = new StringBuilder();
 		if (entityAttributes != null) {
 		    String[] entityAttributesArray = entityAttributes.split(",");
 		    for (String ea : entityAttributesArray) {
 			    if (StringUtils.hasText(ea)) {
 		            entityAttributesStyle.append(" ea-").append(ea);
 			    }
 		    }
 		}
 		return StringEscapeUtils.escapeHtml(entityAttributesStyle.toString());
 	}
 
 	@Override
 	public String resolveLabelText(PageContext pageContext, SectionField sectionField) {
 		String labelText = messageService.lookupMessage(MessageResourceType.FIELD_LABEL, sectionField.getFieldLabelName(), pageContext.getRequest().getLocale());
 		if ( ! StringUtils.hasText(labelText)) {
 			if (!sectionField.isCompoundField()) {
 				labelText = sectionField.getFieldDefinition().getDefaultLabel();
 			}
 			else {
 				labelText = sectionField.getSecondaryFieldDefinition().getDefaultLabel();
 			}
 		}
 		return labelText;
 	}
 
 	protected void writeErrorClass(PageContext pageContext, String formFieldName, StringBuilder sb) {
 		RequestContext requestContext = (RequestContext) pageContext.getAttribute(RequestContextAwareTag.REQUEST_CONTEXT_PAGE_ATTRIBUTE);
 		Errors errors = requestContext.getErrors(StringConstants.FORM);
 		if (errors != null && errors.hasErrors()) {
            if (formFieldName.endsWith(TangerineForm.TANG_DOT_VALUE)) {
                formFieldName = formFieldName.substring(0, formFieldName.length() - TangerineForm.TANG_DOT_VALUE.length());
            }
 			if (errors.hasFieldErrors(new StringBuilder(StringConstants.FIELD_MAP_START).append(formFieldName).append(StringConstants.FIELD_MAP_END).toString())) {
 				sb.append(" textError ");
 			}
 		}
 	}
 
 	protected void writeDisabled(SectionField currentField, TangerineForm form, StringBuilder sb) {
 		if (fieldService.isFieldDisabled(currentField, form.getDomainObject())) {
 		    sb.append("disabled=\"true\" ");
 		}
 	}
 
 	protected void writeSideLiElementStart(SectionField currentField, Object fieldValue, StringBuilder sb) {
 		sb.append("<li class=\"side ");
 		sb.append(getSideCssClass(fieldValue));
 		sb.append("\" id=\"").append(currentField.getFieldDefinition().getId().replace(".", "-").replace("[", "-").replace("]", "-")).append("\">");
 	}
 
 	protected void writeSideLiElementEnd(StringBuilder sb) {
 		sb.append("</li>");
 	}
 
 	protected void writeLabel(SectionField sectionField, PageContext pageContext, StringBuilder sb) {
 		sb.append("<label for=\"").append(TangerineForm.escapeFieldName(sectionField.getFieldPropertyName())).append("\" class=\"desc\">");
 		String helpText = messageService.lookupMessage(MessageResourceType.FIELD_HELP, sectionField.getFieldLabelName(), pageContext.getRequest().getLocale());
 		if (StringUtils.hasText(helpText)) {
 			sb.append("<a class=\"helpLink\"><img src=\"images/icons/questionGreyTransparent.gif\" /></a><span class=\"helpText\">");
 			sb.append(helpText);
 			sb.append("</span>");
 		}
 		if (isFieldRequired(sectionField)) {
 			sb.append("<span class=\"required\">*</span>&nbsp;");
 		}
 
 		sb.append(resolveLabelText(pageContext, sectionField));
 		sb.append("</label>");
 	}
 
 	protected void writeDeleteLink(StringBuilder sb, String deleteHandler) {
 		String removeOptionMsg = getMessage("removeThisOption");
 		sb.append("<a href=\"javascript:void(0)\" onclick=\"").append(deleteHandler).append("\" class=\"deleteOption\">");
 		sb.append("<img src=\"images/icons/deleteRow.png\" alt=\"").append(removeOptionMsg).append("\" title=\"").append(removeOptionMsg).append("\"/></a>");
 	}
 
 	protected String getSideCssClass(Object fieldValue) {
 		return StringConstants.EMPTY;
 	}
 
 	protected boolean isFieldRequired(SectionField currentField) {
 		return fieldService.isFieldRequired(currentField);
 	}
 
 	protected Object[] splitValuesByCustomFieldSeparator(Object fieldValue) {
 		Object[] fieldVals;
 		if (fieldValue == null) {
 			fieldVals = new Object[0];
 		}
 		else if (fieldValue instanceof String) {
 			fieldVals = ((String) fieldValue).split(StringConstants.CUSTOM_FIELD_SEPARATOR);
 		}
 		else {
 			fieldVals = new Object[] { fieldValue };
 		}
 		return fieldVals;
 	}
 }
