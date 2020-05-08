 package caesar.bcinterpreter.instructions;
 
 import caesar.bcinterpreter.CObject;
 import caesar.bcinterpreter.CaesarBCInterpreter;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Jan Hýbl
  * Date: 14.2.12
  * Time: 19:54
  */
 public class LoadField {
     public static final byte code = 0x05;
 
     public static void execute(CaesarBCInterpreter interpreter) {
         int field_id = interpreter.readIntInstructionParam();
         CObject obj = interpreter.getStack().popObject();
        int address = obj.getFieldAddress(field_id);
         CObject field = interpreter.getHeap().getFromAddress(address);
         interpreter.getStack().pushObject(field);
     }
 }
