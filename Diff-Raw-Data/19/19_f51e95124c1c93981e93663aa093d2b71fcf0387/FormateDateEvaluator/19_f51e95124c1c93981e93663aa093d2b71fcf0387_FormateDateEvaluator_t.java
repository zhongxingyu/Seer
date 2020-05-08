 /**
  * 
  */
 package org.eclipse.jubula.client.core.functions;
 
 import java.util.Date;
 
 import org.apache.commons.lang.time.DateFormatUtils;
 import org.eclipse.jubula.tools.exception.InvalidDataException;
 import org.eclipse.jubula.tools.messagehandling.MessageIDs;
 
 /**
  * @author al
  * 
  */
 public final class FormateDateEvaluator extends AbstractFunctionEvaluator {
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * org.eclipse.jubula.client.core.functions.IFunctionEvaluator#evaluate(
      * java.lang.String[])
      */
     public String evaluate(String[] arguments) throws InvalidDataException {
         validateParamCount(arguments, 2);
         try {
             Long dateTime = Long.valueOf(arguments[0]);
             if (dateTime < 0) {
                 throw new InvalidDataException("value to small: " + dateTime,
                         MessageIDs.E_TOO_SMALL_VALUE);
             }
             Date date = new Date(dateTime);
 
             return DateFormatUtils.format(date, arguments[1]);
         } catch (NumberFormatException e) {
             throw new InvalidDataException("not an integer: " + arguments[0],
                     MessageIDs.E_BAD_INT);
 
         }
     }
 
 }
