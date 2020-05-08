 /**
  * The contents of this file are subject to the OpenMRS Public License
  * Version 1.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://license.openmrs.org
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
  * License for the specific language governing rights and limitations
  * under the License.
  *
  * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
  */
 package org.openmrs.module.registrationapp.form;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang.StringUtils;
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.node.ArrayNode;
 import org.codehaus.jackson.node.ObjectNode;
 import org.openmrs.Person;
 import org.openmrs.PersonAttribute;
 import org.openmrs.PersonAttributeType;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.appframework.domain.AppDescriptor;
 import org.openmrs.module.registrationapp.model.Field;
 import org.openmrs.module.registrationapp.model.NavigableFormStructure;
 import org.openmrs.module.registrationapp.model.Question;
 import org.openmrs.module.registrationapp.model.Section;
 import org.openmrs.ui.framework.fragment.FragmentRequest;
 
 /**
  * Builds a registration form structure from the app configuration.
  */
 public class RegisterPatientFormBuilder {
 	
 	public static NavigableFormStructure buildFormStructure(AppDescriptor app) throws IOException {
 		NavigableFormStructure formStructure = new NavigableFormStructure();
 		
 		ArrayNode sections = (ArrayNode) app.getConfig().get("sections");
 		for (JsonNode i : sections) {
 			ObjectNode config = (ObjectNode) i;
 			
 			ObjectMapper objectMapper = new ObjectMapper();
 			Section section = objectMapper.convertValue(config, Section.class);
 			
 			if (section.getQuestions() != null) {
 				for (Question question : section.getQuestions()) {
 					if (question.getFields() != null) {
 						for (Field field : question.getFields()) {
 							ObjectNode widget = field.getWidget();
 							String providerName = (String) widget.get("providerName").getTextValue();
 							String fragmentId = (String) widget.get("fragmentId").getTextValue();
 							FragmentRequest fragmentRequest = new FragmentRequest(providerName, fragmentId);
 							field.setFragmentRequest(fragmentRequest);
 						}
 					}
 				}
 			}
 			
 			formStructure.addSection(section);
 		}
 		
 		return formStructure;
 	}
 	
 	public static void resolvePersonAttributeFields(NavigableFormStructure form, Person person,
 	                                                Map<String, String[]> personAttributes) {
 		List<Field> fields = form.getFields();
 		if (fields != null && fields.size() > 0) {
 			for (Field field : fields) {
 				String[] parameterValues = personAttributes.get(field.getFormFieldName());
				for (String parameterValue : parameterValues) {
					if (StringUtils.isNotBlank(parameterValue)) {
						if (StringUtils.equals(field.getType(), "personAttribute")) {
							PersonAttributeType personAttributeByUuid = Context.getPersonService()
							        .getPersonAttributeTypeByUuid(field.getUuid());
							if (personAttributeByUuid != null) {
								PersonAttribute attribute = new PersonAttribute(personAttributeByUuid, parameterValue);
								person.addAttribute(attribute);
 							}
 						}
 					}
 				}
				
 			}
 		}
 	}
 }
