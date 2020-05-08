 /*******************************************************************************
  * Copyright (c) 2005 Oracle Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Ian Trimble - initial API and implementation
  *******************************************************************************/ 
 package org.eclipse.jst.jsf.core.jsfappconfig;
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.jst.jsf.facesconfig.emf.ComponentClassType;
 import org.eclipse.jst.jsf.facesconfig.emf.ComponentType;
 import org.eclipse.jst.jsf.facesconfig.emf.ComponentTypeType;
 import org.eclipse.jst.jsf.facesconfig.emf.ConverterClassType;
 import org.eclipse.jst.jsf.facesconfig.emf.ConverterIdType;
 import org.eclipse.jst.jsf.facesconfig.emf.ConverterType;
 import org.eclipse.jst.jsf.facesconfig.emf.FacesConfigFactory;
 import org.eclipse.jst.jsf.facesconfig.emf.FacesConfigType;
 import org.eclipse.jst.jsf.facesconfig.emf.ValidatorClassType;
 import org.eclipse.jst.jsf.facesconfig.emf.ValidatorIdType;
 import org.eclipse.jst.jsf.facesconfig.emf.ValidatorType;
 
 /**
  * ImplicitRuntimeJSFAppConfigProvider provides an application configuration
  * model that contains implicit configuration objects provided by a JSF
  * implementation at runtime.
  * 
  * <p><b>Provisional API - subject to change</b></p>
  * 
  * @author Ian Trimble - Oracle
  */
 public class ImplicitRuntimeJSFAppConfigProvider extends AbstractJSFAppConfigProvider {
 
 	/**
 	 * Cached {@link FacesConfigType} instance.
 	 */
 	protected FacesConfigType facesConfig = null;
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jst.jsf.core.jsfappconfig.IJSFAppConfigProvider#getFacesConfigModel()
 	 */
 	public FacesConfigType getFacesConfigModel() {
 		if (facesConfig == null) {
 			createModel();
 			if (facesConfig != null) {
 				jsfAppConfigLocater.getJSFAppConfigManager().addFacesConfigChangeAdapter(facesConfig);
 			}
 		}
 		return facesConfig;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jst.jsf.core.jsfappconfig.IJSFAppConfigProvider#releaseFacesConfigModel()
 	 */
 	public void releaseFacesConfigModel() {
 		jsfAppConfigLocater.getJSFAppConfigManager().removeFacesConfigChangeAdapter(facesConfig);
 		facesConfig = null;
 	}
 
 	/**
 	 * Creates the application configuration model and assigns it to the
 	 * facesConfig property.
 	 */
 	protected void createModel() {
 		facesConfig = FacesConfigFactory.eINSTANCE.createFacesConfigType();
 		//create and add converters
 		EList converters = facesConfig.getConverter();
 		converters.add(createConverter("BigDecimal")); //$NON-NLS-1$
 		converters.add(createConverter("BigInteger")); //$NON-NLS-1$
 		converters.add(createConverter("Boolean")); //$NON-NLS-1$
 		converters.add(createConverter("Byte")); //$NON-NLS-1$
 		converters.add(createConverter("Character")); //$NON-NLS-1$
 		converters.add(createConverter("DateTime")); //$NON-NLS-1$
 		converters.add(createConverter("Double")); //$NON-NLS-1$
 		converters.add(createConverter("Float")); //$NON-NLS-1$
 		converters.add(createConverter("Integer")); //$NON-NLS-1$
 		converters.add(createConverter("Long")); //$NON-NLS-1$
 		converters.add(createConverter("Number")); //$NON-NLS-1$
 		converters.add(createConverter("Short")); //$NON-NLS-1$
 		//create and add validators
 		EList validators = facesConfig.getValidator();
 		validators.add(createValidator("DoubleRange")); //$NON-NLS-1$
 		validators.add(createValidator("Length")); //$NON-NLS-1$
 		validators.add(createValidator("LongRange")); //$NON-NLS-1$
 		//create and add UI components
 		EList components = facesConfig.getComponent();
 		components.add(createUIComponent("Column")); //$NON-NLS-1$
 		components.add(createUIComponent("Command")); //$NON-NLS-1$
 		components.add(createUIComponent("Data")); //$NON-NLS-1$
 		components.add(createUIComponent("Form")); //$NON-NLS-1$
 		components.add(createUIComponent("Graphic")); //$NON-NLS-1$
 		components.add(createUIComponent("Input")); //$NON-NLS-1$
 		components.add(createUIComponent("Message")); //$NON-NLS-1$
 		components.add(createUIComponent("Messages")); //$NON-NLS-1$
 		components.add(createUIComponent("Output")); //$NON-NLS-1$
 		components.add(createUIComponent("Panel")); //$NON-NLS-1$
 		components.add(createUIComponent("Parameter")); //$NON-NLS-1$
 		components.add(createUIComponent("SelectBoolean")); //$NON-NLS-1$
 		components.add(createUIComponent("SelectItem")); //$NON-NLS-1$
 		components.add(createUIComponent("SelectItems")); //$NON-NLS-1$
 		components.add(createUIComponent("SelectMany")); //$NON-NLS-1$
 		components.add(createUIComponent("SelectOne")); //$NON-NLS-1$
 		components.add(createUIComponent("ViewRoot")); //$NON-NLS-1$
 		//create and add HTML components
 		components.add(createHTMLComponent("HtmlCommandButton")); //$NON-NLS-1$
 		components.add(createHTMLComponent("HtmlCommandLink")); //$NON-NLS-1$
 		components.add(createHTMLComponent("HtmlDataTable")); //$NON-NLS-1$
 		components.add(createHTMLComponent("HtmlForm")); //$NON-NLS-1$
 		components.add(createHTMLComponent("HtmlGraphicImage")); //$NON-NLS-1$
 		components.add(createHTMLComponent("HtmlInputHidden")); //$NON-NLS-1$
 		components.add(createHTMLComponent("HtmlInputSecret")); //$NON-NLS-1$
 		components.add(createHTMLComponent("HtmlInputText")); //$NON-NLS-1$
 		components.add(createHTMLComponent("HtmlInputTextarea")); //$NON-NLS-1$
 		components.add(createHTMLComponent("HtmlMessage")); //$NON-NLS-1$
 		components.add(createHTMLComponent("HtmlMessages")); //$NON-NLS-1$
 		components.add(createHTMLComponent("HtmlOutputFormat")); //$NON-NLS-1$
 		components.add(createHTMLComponent("HtmlOutputLabel")); //$NON-NLS-1$
 		components.add(createHTMLComponent("HtmlOutputLink")); //$NON-NLS-1$
 		components.add(createHTMLComponent("HtmlOutputText")); //$NON-NLS-1$
 		components.add(createHTMLComponent("HtmlPanelGrid")); //$NON-NLS-1$
 		components.add(createHTMLComponent("HtmlPanelGroup")); //$NON-NLS-1$
 		components.add(createHTMLComponent("HtmlSelectBooleanCheckbox")); //$NON-NLS-1$
 		components.add(createHTMLComponent("HtmlSelectManyCheckbox")); //$NON-NLS-1$
 		components.add(createHTMLComponent("HtmlSelectManyListbox")); //$NON-NLS-1$
 		components.add(createHTMLComponent("HtmlSelectManyMenu")); //$NON-NLS-1$
 		components.add(createHTMLComponent("HtmlSelectOneListbox")); //$NON-NLS-1$
 		components.add(createHTMLComponent("HtmlSelectOneMenu")); //$NON-NLS-1$
 		components.add(createHTMLComponent("HtmlSelectOneRadio")); //$NON-NLS-1$
 	}
 
 	/**
 	 * Creates a {@link ConverterType} instance.
 	 *
 	 * @param name Base name of converter from which converter-id and
 	 * converter-class are formed.
 	 * @return {@link ConverterType} instance.
 	 */
 	protected ConverterType createConverter(String name) {
 		ConverterType converterType = FacesConfigFactory.eINSTANCE.createConverterType();
 		//set converter-id
 		ConverterIdType converterIdType = FacesConfigFactory.eINSTANCE.createConverterIdType();
 		StringBuffer sb = new StringBuffer();
 		sb.append("javax.faces."); //$NON-NLS-1$
 		sb.append(name);
 		converterIdType.setTextContent(sb.toString());
 		converterType.setConverterId(converterIdType);
 		//set converter-class
 		ConverterClassType converterClassType = FacesConfigFactory.eINSTANCE.createConverterClassType();
 		sb = new StringBuffer();
 		sb.append("javax.faces.convert."); //$NON-NLS-1$
 		sb.append(name);
 		sb.append("Converter"); //$NON-NLS-1$
 		converterClassType.setTextContent(sb.toString());
 		converterType.setConverterClass(converterClassType);
 		return converterType;
 	}
 
 	/**
 	 * Creates a {@link ValidatorType} instance.
 	 * 
 	 * @param name Base name of validator from which validator-id and
 	 * validator-class are formed.
 	 * @return {@link ValidatorType} instance.
 	 */
 	protected ValidatorType createValidator(String name) {
 		ValidatorType validatorType = FacesConfigFactory.eINSTANCE.createValidatorType();
 		//set validator-id
 		ValidatorIdType validatorIdType = FacesConfigFactory.eINSTANCE.createValidatorIdType();
 		StringBuffer sb = new StringBuffer();
 		sb.append("javax.faces."); //$NON-NLS-1$
 		sb.append(name);
 		validatorIdType.setTextContent(sb.toString());
 		validatorType.setValidatorId(validatorIdType);
 		//set validator-class
 		ValidatorClassType validatorClassType = FacesConfigFactory.eINSTANCE.createValidatorClassType();
 		sb = new StringBuffer();
 		sb.append("javax.faces.validator."); //$NON-NLS-1$
 		sb.append(name);
 		sb.append("Validator"); //$NON-NLS-1$
 		validatorClassType.setTextContent(sb.toString());
 		validatorType.setValidatorClass(validatorClassType);
 		return validatorType;
 	}
 
 	/**
 	 * Creates a {@link ComponentType} instance to represent a standard UI
 	 * component.
 	 * 
 	 * @param name Base name of component from which component-type and
 	 * component-class are formed.
 	 * @return {@link ComponentType} instance.
 	 */
 	protected ComponentType createUIComponent(String name) {
 		ComponentType componentType = FacesConfigFactory.eINSTANCE.createComponentType();
 		//set component-type
 		ComponentTypeType componentTypeType = FacesConfigFactory.eINSTANCE.createComponentTypeType();
 		StringBuffer sb = new StringBuffer();
 		sb.append("javax.faces."); //$NON-NLS-1$
 		sb.append(name);
 		componentTypeType.setTextContent(sb.toString());
 		componentType.setComponentType(componentTypeType);
 		//set component-class
 		ComponentClassType componentClassType = FacesConfigFactory.eINSTANCE.createComponentClassType();
 		sb = new StringBuffer();
 		sb.append("javax.faces.component.UI"); //$NON-NLS-1$
 		sb.append(name);
 		componentClassType.setTextContent(sb.toString());
 		componentType.setComponentClass(componentClassType);
 		return componentType;
 	}
 
 	/**
 	 * Creates a {@link ComponentType} instance to represent a concrete HTML
 	 * component.
 	 * 
 	 * @param name Base name of component from which component-type and
 	 * component-class are formed.
 	 * @return {@link ComponentType} instance.
 	 */
 	protected ComponentType createHTMLComponent(String name) {
 		ComponentType componentType = FacesConfigFactory.eINSTANCE.createComponentType();
 		//set component-type
 		ComponentTypeType componentTypeType = FacesConfigFactory.eINSTANCE.createComponentTypeType();
 		StringBuffer sb = new StringBuffer();
 		sb.append("javax.faces."); //$NON-NLS-1$
 		sb.append(name);
 		componentTypeType.setTextContent(sb.toString());
 		componentType.setComponentType(componentTypeType);
 		//set component-class
 		ComponentClassType componentClassType = FacesConfigFactory.eINSTANCE.createComponentClassType();
 		sb = new StringBuffer();
 		sb.append("javax.faces.component.html."); //$NON-NLS-1$
 		sb.append(name);
 		componentClassType.setTextContent(sb.toString());
 		componentType.setComponentClass(componentClassType);
 		return componentType;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	public boolean equals(Object otherObject) {
 		boolean equals = false;
 		if (otherObject instanceof ImplicitRuntimeJSFAppConfigProvider) {
 			equals = true;
 		}
 		return equals;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see java.lang.Object#hashCode()
 	 */
 	public int hashCode() {
 		return ImplicitRuntimeJSFAppConfigProvider.class.getName().hashCode();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see java.lang.Object#toString()
 	 */
 	public String toString() {
 		return "ImplicitRuntimeJSFAppConfigProvider[]"; //$NON-NLS-1$
 	}
 
 }
