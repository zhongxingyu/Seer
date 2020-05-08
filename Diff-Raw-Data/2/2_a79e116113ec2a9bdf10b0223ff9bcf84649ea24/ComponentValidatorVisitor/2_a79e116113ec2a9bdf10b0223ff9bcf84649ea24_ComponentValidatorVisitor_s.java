 /*******************************************************************************
  * Copyright (c) 2001, 2006 Oracle Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Oracle Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.jsf.validation.internal.appconfig;
 
 import java.util.List;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.jst.jsf.facesconfig.emf.ComponentClassType;
 import org.eclipse.jst.jsf.facesconfig.emf.FacesConfigPackage;
 import org.eclipse.jst.jsf.facesconfig.emf.FacetNameType;
 
 /**
  * Validator for a <component>
  * 
  * @author cbateman
  *
  */
 public class ComponentValidatorVisitor extends EObjectValidationVisitor 
 {
     /**
      * @param version
      */
     public ComponentValidatorVisitor(final String version)
     {
         super(FacesConfigPackage.eINSTANCE.getFacesConfigType_Component(),
                 version);
     }
     
     protected void doValidate(EObject object, List messages, IFile file) {
         // nothing in the tag to validate
     }
 
     protected EObjectValidationVisitor[] getChildNodeValidators() {
         return new EObjectValidationVisitor[]
         {
             new ComponentClassValidationVisitor(getVersion()),
             new AttributeValidationVisitor(FacesConfigPackage.eINSTANCE.getComponentType_Attribute(), getVersion()),
             new PropertyValidationVisitor
                 (FacesConfigPackage.eINSTANCE.getComponentType_Property()
                 ,FacesConfigPackage.eINSTANCE.getComponentType_ComponentClass()
                 ,getVersion()),
             new ComponentFacetValidationVisitor(getVersion())
         };
     }
 
     private static class ComponentClassValidationVisitor extends ClassNameEObjectValidationVisitor
     {
         ComponentClassValidationVisitor(final String version)
         {
             super(FacesConfigPackage.eINSTANCE.getComponentType_ComponentClass(),
                     version);
         }
         
         protected String getFullyQualifiedName(EObject eobj) 
         {
             return ((ComponentClassType)eobj).getTextContent();
         }
 
         protected String getInstanceOf() {
             return "javax.faces.component.UIComponent";
         }
 
         protected EObjectValidationVisitor[] getChildNodeValidators() {
             return NO_CHILDREN;
         }
     }
     
     private static class ComponentFacetValidationVisitor extends EObjectValidationVisitor
     {
         /**
          * @param version
          */
         public ComponentFacetValidationVisitor(String version) 
         {
             super(FacesConfigPackage.eINSTANCE.getComponentType_Facet(), version);
         }
 
         protected void doValidate(EObject object, List messages, IFile file) {
             // do nothing
         }
 
         protected EObjectValidationVisitor[] getChildNodeValidators() {
             return new EObjectValidationVisitor[]
             {
                     new FacetNameValidationVisitor(getVersion())
             };
         }
         
         private static class FacetNameValidationVisitor extends EObjectValidationVisitor
         {
             /**
              * @param version
              */
             public FacetNameValidationVisitor(String version) {
                 super(FacesConfigPackage.eINSTANCE.getFacetType_FacetName(),
                         version);
             }
 
             protected void doValidate(EObject object, List messages, IFile file) {
                 FacetNameType name = (FacetNameType) object;
                 boolean   isValid = false;
                 
                 if (name.getTextContent() != null
                         || "".equals(name.getTextContent().trim()))
                 {
                     String nameValue = name.getTextContent().trim();
 
                     // TODO: isJavaIdentifierStart seems broken...
                    if (Character.isJavaIdentifierStart(nameValue.charAt(0)));
                     {
                         isValid = true;
                         for (int i = 1; i < nameValue.length(); i++)
                         {
                             if (!Character.isJavaIdentifierPart(nameValue.charAt(i)))
                             {
                                 isValid = false;
                             }
                         }
                     }
                 }
                 
                 if (!isValid)
                 {
                     addMessageInfo(messages, 
                             DiagnosticFactory.create_MUST_BE_A_VALID_JAVA_IDENT("Facet name")
                             , object, file);
                 }
             }
 
             protected EObjectValidationVisitor[] getChildNodeValidators() {
                 return NO_CHILDREN;
             }
             
         }
     }
 }
