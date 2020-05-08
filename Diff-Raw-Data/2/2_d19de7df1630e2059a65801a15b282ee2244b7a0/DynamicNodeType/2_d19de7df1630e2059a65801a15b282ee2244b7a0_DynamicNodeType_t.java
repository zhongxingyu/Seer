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
 
 package org.amanzi.neo.services.internal;
 
 import org.amanzi.neo.services.enums.INodeType;
 
 
 /**
  * <p>
  *Dynamic node type provide work with user-defined node types
  * </p>
  * @author TsAr
  * @since 1.0.0
  */
 public class DynamicNodeType implements INodeType {
 
     private final String type;
 
     /**
      * @param type
      */
     public DynamicNodeType(String type) {
         this.type = type;
     }
 
     @Override
     public String getId() {
         return type;
     }
 
     @Override
     public int hashCode() {
         final int prime = 31;
         int result = 1;
         result = prime * result + ((type == null) ? 0 : type.hashCode());
         return result;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (this == obj)
             return true;
         if (obj == null)
             return false;
        if (!(obj instanceof INodeType))
             return false;
         DynamicNodeType other = (DynamicNodeType)obj;
         if (type == null) {
             if (other.type != null)
                 return false;
         } else if (!type.equals(other.type))
             return false;
         return true;
     }
 
 
 
 }
