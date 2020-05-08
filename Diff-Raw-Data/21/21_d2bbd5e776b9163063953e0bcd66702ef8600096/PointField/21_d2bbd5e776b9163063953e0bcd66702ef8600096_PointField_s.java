 package caesar.bcinterpreter.instructions;
 
 import caesar.bcinterpreter.CObject;
 import caesar.bcinterpreter.CaesarBCInterpreter;
 
 /**
  * Zjisti adresu ulozeni urciteho fieldu z objektu na stacku a da ji na stack.
  * User: Jan Hýbl
  * Date: 18.2.12
  * Time: 20:21
  */
 public class PointField {
     public static final byte code = 0x09;
 
     public static void execute(CaesarBCInterpreter interpreter) {
         int fieldId = interpreter.readIntInstructionParam();
         CObject object = interpreter.getStack().popObject();
        int address = object.getFieldAddress(fieldId);
         interpreter.getStack().pushInt(address);
     }
 }
