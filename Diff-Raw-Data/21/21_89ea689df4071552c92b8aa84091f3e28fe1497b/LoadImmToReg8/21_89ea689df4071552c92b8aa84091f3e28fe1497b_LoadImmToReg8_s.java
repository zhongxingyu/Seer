 package gameBoy.cpu.opcodes.load.loadImmToReg;
 
 import gameBoy.cpu.Register;
 import gameBoy.interfaces.IOpcode;
 import gameBoy.interfaces.IProcessor;
 
public abstract class LoadImmToReg8 implements IOpcode{
 	private static int cycles = 8;
 	private IProcessor processor;
 	private Register register;
 	private byte immediate;
 	
 	public LoadImmToReg8( IProcessor processor , Register register, byte immediate ) {
 		this.processor = processor;
 		this.register = register;
 		this.immediate = immediate;
 	}
 	
 	public void execute() {
 		this.processor.getRegisters().setRegister( register, immediate );
 	}
 	
 	public int getCycles() {
 		return cycles;
 	}
 }
