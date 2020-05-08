 package gameBoy.cpu.opcodes.decrement;
 
 import gameBoy.cpu.Flag;
 import gameBoy.cpu.Register;
 import gameBoy.interfaces.IOpcode;
 import gameBoy.interfaces.IProcessor;
 
 public class DecAddrHL implements IOpcode {
 	private static int cycles = 12;
 	private IProcessor processor;
 	
 	public DecAddrHL(IProcessor processor) {
 		this.processor = processor;
 	}
 
 	@Override
 	public void execute() {
 		this.processor.getRegisters().setFlagTo(Flag.N, true);
 		short address = this.processor.getRegisters().getRegister( Register.HL );
		byte data = (byte) ( this.processor.getMemory().get8BitValue( address ) + 1 );
 		this.processor.getRegisters().setFlagTo(Flag.H, (data & 0x10) >= 0x10);
 		data--;
 		this.processor.getRegisters().setFlagTo(Flag.Z, data == 0);
 		this.processor.getMemory().set8BitValue( address, data );
 	}
 
 	@Override
 	public int getCycles() {
 		return cycles;
 	}
 
 }
