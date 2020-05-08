 /*******************************************************************************
  * Copyright (c) 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.bpel.model.util;
 
 import javax.xml.namespace.QName;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.xsd.XSDElementDeclaration;
 import org.eclipse.xsd.XSDPackage;
 import org.eclipse.xsd.XSDSchema;
 import org.eclipse.xsd.XSDTypeDefinition;
 
 public class XSDUtil
 {
     public static final String XSD_TYPE_DEFINITION = XSDPackage.eINSTANCE.getXSDTypeDefinition().getName();
     
     public static final String XSD_ELEMENT_DECLARATION = XSDPackage.eINSTANCE.getXSDElementDeclaration().getName();
 
     /**
      * Tests if <code>typeName</code> is a recognized reference type.
      */
     public static boolean isSchemaType(String typeName)
     {
         return typeName == null ? false :
             XSD_TYPE_DEFINITION.equals(typeName)
             || XSD_ELEMENT_DECLARATION.equals(typeName);
     }
 
     /**
      * Resolve with the given schema.
      */
     public static EObject resolve(XSDSchema schema, QName qname, String name, String refType)
     {
         EObject resolvedObject = null;
         if (XSD_TYPE_DEFINITION.equals(refType))
         {
             resolvedObject = resolveTypeDefinition(schema, qname);
         }
         else if (XSD_ELEMENT_DECLARATION.equals(refType))
         {
             resolvedObject = resolveElementDeclaration(schema, qname);
         }
         else
         {
             System.err.println(XSDUtil.class.getName() + ": unrecognized refType: " + refType);
         }        
         return resolvedObject;
     }
 
     public static XSDElementDeclaration resolveElementDeclaration(XSDSchema schema, QName qname)
    {
        return schema.resolveElementDeclaration(qname.getNamespaceURI(), qname.getLocalPart());
     }
     
     public static XSDTypeDefinition resolveTypeDefinition(XSDSchema schema, QName qname)
     {
        return schema.resolveTypeDefinition(qname.getNamespaceURI(), qname.getLocalPart());
     }
 }
