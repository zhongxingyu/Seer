 /**
  * License Agreement.
  *
  * Rich Faces - Natural Ajax for Java Server Faces (JSF)
  *
  * Copyright (C) 2007 Exadel, Inc.
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License version 2.1 as published by the Free Software Foundation.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
  */
 package org.richfaces.validator;
 
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.ResourceBundle;
 
 import org.ajax4jsf.el.ELContextWrapper;
 import org.ajax4jsf.tests.AbstractAjax4JsfTestCase;
 import org.hibernate.validator.ClassValidator;
 import org.hibernate.validator.InvalidValue;
 import org.richfaces.validator.ObjectValidator.ValidationResolver;
 
 public class HibernateValidatorTest extends AbstractAjax4JsfTestCase {
 
 	public HibernateValidatorTest(String name) {
 		super(name);
 	}
 
 	public void testValidate() {
 		
 	}
 	
 	public void setUp() throws Exception {
     	super.setUp();
     }
 
     public void tearDown() throws Exception {
     	super.tearDown();
     }
 	
 	public void testGetValidator() throws Exception {
 		HibernateValidator beanValidator = new HibernateValidator();
 		ClassValidator<? extends Object> validator = beanValidator.getValidator(facesContext,ValidableBean.class);
 		assertNotNull(validator);
 		assertTrue(validator.hasValidationRules());
 		validator = beanValidator.getValidator(facesContext,String.class);
 		assertNotNull(validator);
 		assertFalse(validator.hasValidationRules());
 	}
 
 	public void testValidateClass() throws Exception {
 		HibernateValidator beanValidator = new HibernateValidator();
 		InvalidValue[] invalidValues = beanValidator.validateClass(facesContext, ValidableBean.class, "integerProperty",new Integer(3));
 		assertNotNull(invalidValues);
 		assertEquals(0, invalidValues.length);
 		invalidValues = beanValidator.validateClass(facesContext, ValidableBean.class, "integerProperty",new Integer(-1));
 		assertNotNull(invalidValues);
 		assertEquals(1, invalidValues.length);
 		invalidValues = beanValidator.validateClass(facesContext, UnValidableBean.class, "integerProperty",new Integer(-1));
 		assertNotNull(invalidValues);
 		assertEquals(0, invalidValues.length);
 		invalidValues = beanValidator.validateClass(facesContext, ValidableBean.class, "nonExistentProperty",new Integer(-1));
 		assertNotNull(invalidValues);
 		assertEquals(0, invalidValues.length);
 
 	}
 	
 	public void testValidateBean() throws Exception {
 		HibernateValidator beanValidator = new HibernateValidator();
 		InvalidValue[] invalidValues = beanValidator.validateBean(facesContext, new ValidableBean(), "integerProperty",new Integer(-1));
 		assertNotNull(invalidValues);
 		assertEquals(1, invalidValues.length);
 	}
 	
 	public void testValidateArray() throws Exception {
 		HibernateValidator beanValidator = new HibernateValidator();
 		InvalidValue[] invalidValues = beanValidator.validateBean(facesContext, new ValidableBean(), "array","");
 		assertNotNull(invalidValues);
 		assertEquals(2, invalidValues.length);
 		System.out.println(invalidValues[0].getMessage());
 		System.out.println(invalidValues[1].getMessage());
 	}
 
 	public void testValidateList() throws Exception {
 		HibernateValidator beanValidator = new HibernateValidator();
 		InvalidValue[] invalidValues = beanValidator.validateBean(facesContext, new ValidableBean(), "list","");
 		assertNotNull(invalidValues);
 		assertEquals(1, invalidValues.length);
 		System.out.println(invalidValues[0].getMessage());
 	}
 	public void testValidateMap() throws Exception {
 		HibernateValidator beanValidator = new HibernateValidator();
 		InvalidValue[] invalidValues = beanValidator.validateBean(facesContext, new ValidableBean(), "map","");
 		assertNotNull(invalidValues);
 		assertEquals(1, invalidValues.length);
 		System.out.println(invalidValues[0].getMessage());
 	}
 	
 	public void testValidationResolver() throws Exception {
 		ValidableBean bean = new ValidableBean();
 		HibernateValidator beanValidator = new HibernateValidator();
 		ValidationResolver validationResolver = beanValidator.createValidationResolver(facesContext, facesContext.getELContext().getELResolver(),null);
 		Object list = validationResolver.getValue(elContext, bean, "list");
 		assertNotNull(list);
 		assertTrue(list instanceof List);
 		validationResolver.setValue(elContext, list, new Integer(0), "");
 		assertFalse(validationResolver.isValid());
 		assertEquals(1, validationResolver.getValidationMessages().length);
 	}
 	public void testValidationResolverMap() throws Exception {
 		ValidableBean bean = new ValidableBean();
 		HibernateValidator beanValidator = new HibernateValidator();
 		ValidationResolver validationResolver = beanValidator.createValidationResolver(facesContext, facesContext.getELContext().getELResolver(),null);
 		Object list = validationResolver.getValue(elContext, bean, "map");
 		assertNotNull(list);
 		assertTrue(list instanceof Map);
 		validationResolver.setValue(elContext, list, new Integer(0), "");
 		assertFalse(validationResolver.isValid());
 		assertEquals(1, validationResolver.getValidationMessages().length);
 	}
 	
 	public void testHibernateMessages() throws Exception {
 		HibernateValidator beanValidator = new HibernateValidator();
 		facesContext.getViewRoot().setLocale(Locale.FRANCE);
 		ResourceBundle hibernateMessages = beanValidator.createHibernateMessages(facesContext);
 		assertEquals(Locale.FRANCE.getLanguage(), hibernateMessages.getLocale().getLanguage());
 		String message = hibernateMessages.getString("validator.pattern");
 		assertEquals("doit suivre \"{regex}\"", message);
 
 	}
 	
 	public void testLocalization() throws Exception {
 		HibernateValidator beanValidator = new HibernateValidator();
 		InvalidValue[] invalidValues = beanValidator.validateClass(facesContext, ValidableBean.class, "integerProperty",new Integer(-1));
 		assertNotNull(invalidValues);
 		assertEquals(1, invalidValues.length);
 		System.out.println(invalidValues[0]);
 		assertTrue(invalidValues[0].getMessage().contains("must be greater than or equal"));
 		facesContext.getViewRoot().setLocale(Locale.FRANCE);
 		invalidValues = beanValidator.validateClass(facesContext, ValidableBean.class, "integerProperty",new Integer(-1));
 		assertNotNull(invalidValues);
 		assertEquals(1, invalidValues.length);
 		System.out.println(invalidValues[0]);
 		assertTrue(invalidValues[0].getMessage().contains("rieur ou"));
 		facesContext.getViewRoot().setLocale(new Locale("ru","RU"));
 		invalidValues = beanValidator.validateClass(facesContext, ValidableBean.class, "integerProperty",new Integer(-1));
 		assertNotNull(invalidValues);
 		assertEquals(1, invalidValues.length);
 		System.out.println(invalidValues[0]);
 		assertTrue(invalidValues[0].getMessage().contains("i18n Test"));
 	}
 
 }
