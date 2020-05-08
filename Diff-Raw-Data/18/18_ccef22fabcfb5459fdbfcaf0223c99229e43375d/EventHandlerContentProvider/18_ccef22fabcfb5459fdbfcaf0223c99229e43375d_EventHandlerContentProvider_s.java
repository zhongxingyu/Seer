 /*******************************************************************************
  * Copyright (c) 2004, 2011 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.client.ui.provider.contentprovider;
 
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.Validate;
 import org.eclipse.jface.viewers.ITreeContentProvider;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jubula.client.core.model.ISpecTestCasePO;
 
 /**
  * @author BREDEX GmbH
  * @created 01.04.2011
  */
 public class EventHandlerContentProvider implements ITreeContentProvider {
 
     /**
      * 
      * {@inheritDoc}
      */
     public void dispose() {
         // no-op
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
         // only accept a Test Case
         Validate.isTrue(
                 newInput == null || newInput instanceof ISpecTestCasePO);
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     public Object[] getElements(Object inputElement) {
         ISpecTestCasePO testCase = (ISpecTestCasePO)inputElement;
        return testCase.getEventExecTcMap().values().toArray();
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     public Object[] getChildren(Object parentElement) {
         return ArrayUtils.EMPTY_OBJECT_ARRAY;
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     public Object getParent(Object element) {
         // since the tree is only 1 level deep, there's no need to worry about
         // proper expansion. so we can just return null here.
         return null;
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     public boolean hasChildren(Object element) {
         // since the tree is only 1 level deep, no element will have children.
         return false;
     }
 
 }
