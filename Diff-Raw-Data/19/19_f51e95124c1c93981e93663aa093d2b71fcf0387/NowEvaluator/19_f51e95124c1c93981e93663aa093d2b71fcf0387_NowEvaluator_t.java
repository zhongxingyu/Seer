 /**
  * 
  */
 package org.eclipse.jubula.client.core.functions;
 
 import java.util.Date;
 
 import org.eclipse.jubula.tools.exception.InvalidDataException;
 
 /**
  * @author al
  *
  */
 public final class NowEvaluator implements IFunctionEvaluator {
 
     /* (non-Javadoc)
      * @see org.eclipse.jubula.client.core.functions.IFunctionEvaluator#evaluate(java.lang.String[])
      */
     public String evaluate(String[] arguments) throws InvalidDataException {
         Date now = new Date();
         return String.valueOf(now.getTime());
     }
 
 }
