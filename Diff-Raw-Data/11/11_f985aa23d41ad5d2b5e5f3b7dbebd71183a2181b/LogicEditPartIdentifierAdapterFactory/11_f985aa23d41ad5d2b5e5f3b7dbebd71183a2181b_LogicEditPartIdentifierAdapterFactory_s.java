 package org.eclipse.jubula.examples.extension.rcp.gef.logic.identifier;
 
 import org.eclipse.core.runtime.IAdapterFactory;
 import org.eclipse.gef.examples.logicdesigner.edit.GateEditPart;
import org.eclipse.jubula.rc.rcp.gef.identifier.IEditPartIdentifier;
 
 
 /**
  * Provides identifiers for Edit Parts in the Logic application.
  * 
  * @author BREDEX GmbH
  */
 public class LogicEditPartIdentifierAdapterFactory implements IAdapterFactory {
 
     /** classes for which this factory provides adapters */
     private static final Class [] ADAPTABLE_TYPES = new Class [] {
         GateEditPart.class};
     
     /**
      * {@inheritDoc}
      */
     public Object getAdapter(Object adaptableObject, Class adapterType) {
         if (adapterType == IEditPartIdentifier.class) {
             if (adaptableObject instanceof GateEditPart) {
                 return new GateEditPartIdentifier(
                         (GateEditPart)adaptableObject);
             }
             
         }
         return null;
     }
 
     /**
      * {@inheritDoc}
      */
     public Class[] getAdapterList() {
         return ADAPTABLE_TYPES;
     }
 }
