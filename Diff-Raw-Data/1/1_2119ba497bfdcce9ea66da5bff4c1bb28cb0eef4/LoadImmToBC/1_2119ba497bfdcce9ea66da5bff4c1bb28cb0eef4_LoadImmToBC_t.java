 package gameBoy.cpu.opcodes.load.loadImmToReg;
 
import gameBoy.cpu.Register;
 import gameBoy.interfaces.IProcessor;
 
 public class LoadImmToBC extends LoadImmToReg16 {
 
 	public LoadImmToBC(IProcessor processor, short immediate) {
 		super(processor, Register.BC, immediate);
 	}
 
 }
