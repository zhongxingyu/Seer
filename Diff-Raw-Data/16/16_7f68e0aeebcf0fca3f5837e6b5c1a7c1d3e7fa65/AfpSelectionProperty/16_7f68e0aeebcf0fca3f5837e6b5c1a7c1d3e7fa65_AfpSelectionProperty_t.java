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
 
 package org.amanzi.awe.afp;
 
 import java.util.Arrays;
 import java.util.List;
 
 import org.amanzi.neo.services.enums.INodeType;
 import org.amanzi.neo.services.enums.NodeTypes;
 import org.amanzi.neo.services.ui.ISelectionPropertyImplementation;
 
 /**
  * TODO Purpose of 
  * <p>
  *
  * </p>
  * @author Kasnitskij_V
  * @since 1.0.0
  */
 public class AfpSelectionProperty implements ISelectionPropertyImplementation {
 
     private List<INodeType> networkStructure;
     
     public AfpSelectionProperty() {
        /* 
         * need correct structure.
         * Correct structure is         
         * 
         * INodeType[] structure = new INodeType[] { NodeTypes.NETWORK, 
                 NodeTypes.BSC, NodeTypes.SITE, NodeTypes.SECTOR, NodeTypes.TRX };
         */
        
        INodeType[] structure = new INodeType[] { NodeTypes.NETWORK, 
                NodeTypes.SITE, NodeTypes.SECTOR };
         
          networkStructure = Arrays.asList(structure);    
     }
     
     @Override
     public String getName() {
         return "Afp selection property";
     }
 
     @Override
     public boolean checkImplementation(String property, List<INodeType> networkStructure) {
        if (!networkStructure.containsAll(this.networkStructure) )
                //|| this.networkStructure.size() != networkStructure.size())
             return true;
         
         String[] notVisibleProperties = new String[] {"Id", "Type", "Sector_ID"};
         
         for (String notVisibleProp : notVisibleProperties) {
             if (property.toLowerCase().equals(notVisibleProp.toLowerCase())) {
                 return false;
             }
         }
         return true;
     }
 
 }
