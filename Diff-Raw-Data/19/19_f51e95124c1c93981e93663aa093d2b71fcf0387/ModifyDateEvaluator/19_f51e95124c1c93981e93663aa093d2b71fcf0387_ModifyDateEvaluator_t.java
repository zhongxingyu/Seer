 /**
  * 
  */
 package org.eclipse.jubula.client.core.functions;
 
 import java.util.Date;
 
 import org.apache.commons.lang.time.DateUtils;
 import org.eclipse.jubula.tools.exception.InvalidDataException;
 import org.eclipse.jubula.tools.messagehandling.MessageIDs;
 
 /**
  * @author al
  * 
  */
 public final class ModifyDateEvaluator extends AbstractFunctionEvaluator {
 
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
             long dateTime = Long.parseLong(arguments[0]);
             if (dateTime < 0) {
                 throw new InvalidDataException("value to small: " + dateTime,
                         MessageIDs.E_TOO_SMALL_VALUE);
             }
             String opString = arguments[1];
             int opStringLength = opString.length();
             if (opStringLength < 2) {
                 throw new InvalidDataException("illegal value: " + opString,
                         MessageIDs.E_WRONG_PARAMETER_VALUE);
             }
             String op = opString.substring(opStringLength - 1, opStringLength);
             String offsetString = op.substring(0, opStringLength - 1);
             try {
                 int offset = Integer.parseInt(offsetString);
                 Date date = new Date(dateTime);
                 Date result = null;
                 if (op.equalsIgnoreCase("d")) {
                     result = DateUtils.addDays(date, offset);
                 } else if (op.equalsIgnoreCase("m")) {
                     result = DateUtils.addMonths(date, offset);
                 } else if (op.equalsIgnoreCase("y")) {
                     result = DateUtils.addYears(date, offset);
                 } else {
                     throw new InvalidDataException("illegal offset format: "
                             + arguments[1], MessageIDs.E_WRONG_PARAMETER_VALUE);
                 }
                 return String.valueOf(result.getTime());
             } catch (NumberFormatException e) {
                 throw new InvalidDataException("illegal offset format: "
                         + arguments[1], MessageIDs.E_WRONG_PARAMETER_VALUE);
             }
 
         } catch (NumberFormatException e) {
             throw new InvalidDataException("not an integer: " + arguments[0],
                     MessageIDs.E_BAD_INT);
         }
 
     }
 
 }
