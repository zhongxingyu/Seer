 /****************************************************
  * 
  * Universidad Francisco de Paula Santander UFPS
  * Ccuta, Colombia
  * (c) 2014 by UFPS. All rights reserved.
  * 
  ****************************************************/
 
 package classmodeler.domain.uml.types;
 
import org.eclipse.uml2.uml.CallConcurrencyKind;
 import org.eclipse.uml2.uml.Type;
 import org.eclipse.uml2.uml.internal.impl.OperationImpl;
 
 /**
  * 
  *
  * @author Gabriel Leonardo Diaz, 01.04.2014.
  */
 public class OperationCustom extends OperationImpl {
   
   private Type returnType;
   
   public Type getReturnType() {
     return returnType;
   }
   
   public void setReturnType(Type returnType) {
     this.returnType = returnType;
   }
   
  public boolean isSynchronized () {
    return getConcurrency() == CallConcurrencyKind.GUARDED_LITERAL;
  }
  
 }
