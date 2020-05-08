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
 package org.eclipse.jubula.client.ui.rcp.propertytester;
 
 import org.eclipse.jubula.client.ui.propertytester.AbstractBooleanPropertyTester;
import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.ui.IEditorPart;
 
 /**
  * @author BREDEX GmbH
  * @created 30.07.2009
  */
 public class EditorPartPropertyTester extends AbstractBooleanPropertyTester {
    /** the name space of this property tester */
    public static final String NS = "org.eclipse.jubula.client.ui.rcp.propertytester.EditorPart"; //$NON-NLS-1$
    /** the id of the "isDirty" property */
     public static final String IS_DIRTY = "isDirty"; //$NON-NLS-1$
    /** the fully qualified id of the "isDirty" property */
    public static final String FQN_IS_DIRTY = 
            NS + StringConstants.DOT + IS_DIRTY;
    
     /**
      * <code>PROPERTIES</code>
      */
     private static final String[] PROPERTIES = new String[] { IS_DIRTY };
 
     /** {@inheritDoc} */
     public boolean testImpl(Object receiver, String property, Object[] args) {
         IEditorPart ep = (IEditorPart)receiver;
         if (property.equals(IS_DIRTY)) {
             return ep.isDirty() ? true : false;
         }
         return false;
     }
 
     /** {@inheritDoc} */
     public Class<? extends Object> getType() {
         return IEditorPart.class;
     }
 
     /** {@inheritDoc} */
     public String[] getProperties() {
         return PROPERTIES;
     }
 }
