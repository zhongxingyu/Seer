 /*******************************************************************************
  * Copyright (c) 2004, 2010 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.client.ui.propertytester;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.eclipse.core.expressions.PropertyTester;
 import org.eclipse.jubula.client.core.model.IObjectMappingCategoryPO;
 import org.eclipse.jubula.client.ui.i18n.Messages;
 import org.eclipse.osgi.util.NLS;
 
 
 /**
  * Property tester for Object Mapping Category.
  * 
  * @author BREDEX GmbH
  * @created Mar 4, 2009
  */
 public class ObjectMappingCategoryPropertyTester extends PropertyTester {
 
     /** the id of the "isTopLevel" property */
     public static final String IS_TOP_LEVEL = "isTopLevel"; //$NON-NLS-1$
 
    /** the id of the "isEmpty" property */
    public static final String IS_EMPTY = "isEmpty"; //$NON-NLS-1$

     /** the logger */
     private static final Log LOG = 
         LogFactory.getLog(ObjectMappingCategoryPropertyTester.class);
 
     /**
      * 
      * {@inheritDoc}
      */
     public boolean test(Object receiver, String property, Object[] args,
             Object expectedValue) {
 
         if (receiver instanceof IObjectMappingCategoryPO) {
             IObjectMappingCategoryPO category = 
                 (IObjectMappingCategoryPO)receiver;
             if (property.equals(IS_TOP_LEVEL)) {
                 boolean areSameType = testIsTopLevel(category);
                 boolean expectedBoolean = expectedValue instanceof Boolean 
                     ? ((Boolean)expectedValue).booleanValue() : true;
                 return areSameType == expectedBoolean;
            } else if (property.equals(IS_EMPTY)) {
                boolean isEmpty = testEmpty(category);
                boolean expectedBoolean = expectedValue instanceof Boolean 
                    ? ((Boolean)expectedValue).booleanValue() : true;
                return isEmpty == expectedBoolean;
             }
 
             LOG.warn(NLS.bind(Messages.PropertyTesterPropertyNotSupported,
                     new String [] {property}));
             return false;
         }
 
         String receiverClass = 
             receiver != null ? receiver.getClass().getName() : "null"; //$NON-NLS-1$
         LOG.warn(NLS.bind(Messages.PropertyTesterTypeNotSupported,
                 new String [] {receiverClass}));
         return false;
     }
 
     /**
      * 
      * @param category The Object Mapping Category to test.
      * @return <code>true</code> if the given category is a top-level category. 
      *         Otherwise <code>false</code>.
      */
     private boolean testIsTopLevel(
             IObjectMappingCategoryPO category) {
 
         return category.getParent() == null;
     }
 
    /**
     * 
     * @param category The Object Mapping Category to test.
     * @return <code>true</code> if the given category is empty. 
     *         Otherwise <code>false</code>.
     */
    private boolean testEmpty(IObjectMappingCategoryPO category) {

        return category.getUnmodifiableAssociationList().isEmpty() 
            && category.getUnmodifiableCategoryList().isEmpty();
    }
 }
