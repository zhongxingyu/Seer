 package plg.gr3.vm.instr;
 
 import java.io.IOException;
 
 import plg.gr3.data.Type;
 import plg.gr3.errors.runtime.IOError;
 import plg.gr3.vm.VirtualMachine;
 
 /**
  * Clase que implementa la instruccion <tt>in(type)</tt>.
  * 
  * @author PLg Grupo 03 2012/2013
  */
 public class InputInstruction extends Instruction {
     
     /** Tipo de la variable a la que se asigna el valor del buffer de entrada */
     private Type inputType;
     
     /**
      * La instrución de Input debe ir seguida de un Store ya que solo almacena el valor del BufferIN en la cima de la
      * pila
      * 
      * @param inputType Tipo del valor que se leerá
      */
     public InputInstruction (Type inputType) {
         this.inputType = inputType;
     }
     
     /** @return Tipo del valor que se leerá */
     public Type getInputType () {
         return inputType;
     }
     
     @Override
     public void execute (VirtualMachine vm) {
         try {
             // FIXME Qué
            vm.pushValue(vm.getInput(inputType)); // Cima <= BufferIN
         } catch (IOException e) {
             // TODO Error en tiempo de ejecución: error al leer de BufferIN
             vm.abort(new IOError(vm.getProgramCounter(), this));
         }
     }
 }
