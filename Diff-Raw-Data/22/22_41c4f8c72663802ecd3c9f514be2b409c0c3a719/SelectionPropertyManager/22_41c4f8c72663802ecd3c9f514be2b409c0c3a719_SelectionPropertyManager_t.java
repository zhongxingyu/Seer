 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 
 package org.amanzi.neo.services.ui;
 
 import java.util.ArrayList;
import java.util.List;

import org.amanzi.neo.services.enums.INodeType;
 
 /**
  * TODO Purpose of 
  * <p>
  *
  * </p>
  * @author Kasnitskij_V
  * @since 1.0.0
  */
 public class SelectionPropertyManager {
     /**
      * List of implementations
      */
     private ArrayList<ISelectionPropertyImplementation> listOfImplementations;
     private static SelectionPropertyManager selectionPropertyManager;
     
     public static SelectionPropertyManager getInstanse() {
         if (selectionPropertyManager == null)
             selectionPropertyManager = new SelectionPropertyManager();
         
         return selectionPropertyManager;
     }
     
     /**
      * Constructor
      */
     private SelectionPropertyManager() {
         listOfImplementations = new ArrayList<ISelectionPropertyImplementation>();
     }
     
     /**
      * Add implementation to list of implementations
      *
      * @param selectionPropertyImplementation specific implementation
      */
     public void addSelectionPropertyImplementation(ISelectionPropertyImplementation selectionPropertyImplementation) {
         if (!listOfImplementations.contains(selectionPropertyImplementation))
             listOfImplementations.add(selectionPropertyImplementation);
     }
     
     /**
      * Remove implementation from list of implementations
      *
      * @param name name of implementation
      * @return true if successful
      */
     public boolean removeFromSelectionPropertyImplementation(String name) {
         boolean isDeleted = false;
         int indexOfImplementation = -1;
         for (ISelectionPropertyImplementation implementation : listOfImplementations) {
             indexOfImplementation++;
             if (name.equals(implementation.getName())) {
                 break;
             }
         }
         if (indexOfImplementation != -1) {
             listOfImplementations.remove(indexOfImplementation);
             isDeleted = true;
         }
         
         return isDeleted;
     }
     
     /**
      * Check that property not visible
      *
      * @param property checking property
      * @return false - if property not visible, true - if property visible
      */
    public boolean checkVisibility(String property, List<INodeType> networkStructure) {
         boolean isSuccess = true;
         for (ISelectionPropertyImplementation implementation : listOfImplementations) {
            isSuccess = implementation.checkImplementation(property, networkStructure);
         }
         
         return isSuccess;
     }
 }
