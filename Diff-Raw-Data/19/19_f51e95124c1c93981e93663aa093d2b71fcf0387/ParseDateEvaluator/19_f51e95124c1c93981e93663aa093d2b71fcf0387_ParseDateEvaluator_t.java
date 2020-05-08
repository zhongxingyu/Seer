 /**
  * 
  */
 package org.eclipse.jubula.client.core.functions;
 
 import java.text.ParseException;
 import java.util.Date;
 
 import org.apache.commons.lang.time.DateUtils;
 import org.eclipse.jubula.tools.exception.InvalidDataException;
 import org.eclipse.jubula.tools.messagehandling.MessageIDs;
 
 /**
  * @author al
  * 
  */
 public final class ParseDateEvaluator extends AbstractFunctionEvaluator {
 
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
             Date result = DateUtils.parseDate(arguments[0],
                     new String[] { arguments[1] });
             return String.valueOf(result.getTime());
         } catch (ParseException e) {
             throw new InvalidDataException("parsing failed, reason: "
                     + e.getMessage(), MessageIDs.E_WRONG_PARAMETER_VALUE);
         }
     }
 
 }
