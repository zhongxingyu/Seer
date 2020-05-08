 package gameBoy.cpu.opcodes.decrement;
 
 import gameBoy.cpu.Flag;
 import gameBoy.cpu.Register;
 import gameBoy.cpu.opcodes.OneByteOpcode;
 import gameBoy.interfaces.IProcessor;
 
 public abstract class Decrement8 extends OneByteOpcode {
 	private static int cycles = 4;
 	private Register register;
 	private IProcessor processor;
 	
 	public Decrement8( IProcessor processor, Register register ) {
 		this.processor = processor;
 		this.register = register;
 	}
 	
 	public void setRegister(Register register) {
 		this.register = register;
 	}
 	
 	public Register getRegister(Register register) {
 		return this.register;
 	}
 	
 	public void execute() {
 		assert processor != null && register != null;
 		
 		this.processor.getRegisters().setFlagTo(Flag.N, true);
 		byte data = (byte) (this.processor.getRegisters().getRegister( this.register ));
 		this.processor.getRegisters().setFlagTo(Flag.H, (data & 0x10) >= 0x10);
 		data--;
 		this.processor.getRegisters().setFlagTo(Flag.Z, data == 0);
 		this.processor.getRegisters().setRegister( this.register, data);
 	}
 	
 	public int getCycles() {
 		return cycles;
 	}
 }
