 package com.google.codes.dryvalidator;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import junit.framework.Assert;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.validator.Field;
 import org.apache.commons.validator.Form;
 import org.apache.commons.validator.ValidatorResources;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.xml.sax.SAXException;
 
 import com.google.codes.dryvalidator.dto.FormItem;
 import com.google.codes.dryvalidator.dto.Validation;
 
 public class ValidationFromStruts {
 	protected static ValidationEngine validationEngine;
 	@BeforeClass
 	public static void initailizeValidationEngine() {
 		validationEngine = new ValidationEngine();
 		validationEngine.setup();
 	}
 
 	@AfterClass
 	public static void disposeValidationEngine() {
 		validationEngine.dispose();
 	}
 
 	@SuppressWarnings("unchecked")
 	@Test
 	public void test() throws IOException, SAXException {
 		InputStream in = this.getClass().getResourceAsStream("/validation.xml");
 		ValidatorResources resources = new ValidatorResources(in);
 		Form form = resources.getForm(Locale.JAPANESE, "MemberForm");
 		for(Field field : (List<Field>)form.getFields()) {
 			FormItem formItem = new FormItem();
 			formItem.setId(field.getProperty());
 			formItem.setLabel(field.getKey());
 			for(String depend : (List<String>)field.getDependencyList()) {
 				String value = null;
 				if(StringUtils.equals(depend, "required")) {
 					value="true";
 				} else {
 					value=field.getVarValue(depend);
 				}
 				if(StringUtils.equals(depend, "maxlength")) {
 					depend = "maxLength";
 				}
 				Validation validation = new Validation(depend, value);
 				formItem.getValidations().addValidation(validation);
 			}
 			System.out.println(formItem);
 			validationEngine.register(formItem);
 		}
 
 		Map<String, Object> formValues = new HashMap<String, Object>();
 		formValues.put("hasSpouse", true);
 		formValues.put("familyName", "01234567890");
 		formValues.put("childrenNum", "");
 		Map<String, List<String>> messages = validationEngine.exec(formValues);
 		List<String> childrenNumMessages = messages.get("childrenNum");
 		Assert.assertTrue("配偶者がありのときは、子供の人数が必須になる", childrenNumMessages != null && childrenNumMessages.size() == 1);
 		validationEngine.unregisterAll();
 
 	}
 }
