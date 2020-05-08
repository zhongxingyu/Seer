 package gameBoy.tests.opcodeTests;
 
 import gameBoy.cpu.Flag;
 import gameBoy.cpu.GameBoyProcessor;
 import gameBoy.cpu.Register;
 import gameBoy.cpu.opcodes.decrement.*;
 import gameBoy.interfaces.IOpcode;
 import gameBoy.interfaces.IProcessor;
 
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 
 public class DecrementTests {
 	private IProcessor processor;
 	private IOpcode op;
 	
 	@Before
 	public void setUp() {
 		this.processor = new GameBoyProcessor();
 	}
 	
 	@Test
 	public void testDec8() {
 		byte value = 13;
 		this.processor.getRegisters().setRegister(Register.A, value);
 		this.op = new DecA( this.processor );
 		
 		op.execute();
 		value--;
 		
 		Assert.assertEquals(value, this.processor.getRegisters().getRegister(Register.A));
 	}
 	
 	@Test
 	public void testDec16() {
 		byte value = 13;
 		this.processor.getRegisters().setRegister(Register.BC, value);
 		this.op = new DecBC( this.processor );
 		
 		op.execute();
 		value--;
 		
 		Assert.assertEquals(value, this.processor.getRegisters().getRegister(Register.BC));
 	}
 	
 	@Test
 	public void testDecAddrHL() {
		short address = (short) 0xC000;
 		byte data = 20;
		System.out.println(this.processor.getMemory());
 		this.processor.getMemory().set8BitValue(address, data);
 		this.processor.getRegisters().setRegister( Register.HL , address);
 		this.op = new DecAddrHL( this.processor );
 		
 		op.execute();
 		data--;
 		
 		Assert.assertEquals(data, this.processor.getMemory().get8BitValue(address));
 	}
 	
 	@Test
 	public void testGetCycles() {
 		this.op = new DecBC( this.processor );
 		int dec16Cycles = 8;
 		Assert.assertEquals(dec16Cycles, this.op.getCycles());
 		
 		this.op = new DecD ( this.processor );
 		int dec8Cycles = 4;
 		Assert.assertEquals(dec8Cycles, this.op.getCycles());
 	}
 	
 	@Test
 	public void testZFlag() {
 		byte value = 1;
 		this.processor.getRegisters().setRegister(Register.A, value);
 		this.op = new DecA( this.processor );
 		
 		op.execute();
 		
 		Assert.assertEquals((byte) 1, this.processor.getRegisters().getFlag(Flag.Z));
 	}
 	
 	@Test
 	public void testHFlag() {
 		byte value = (byte) 0x10;
 		this.processor.getRegisters().setRegister(Register.D, value);
 		this.op = new DecD( this.processor );
 		
 		op.execute();
 		
 		Assert.assertEquals((byte) 1, this.processor.getRegisters().getFlag(Flag.H));
 		
 		value = (byte) 0x30;
 		this.processor.getRegisters().setRegister(Register.D, value);
 		this.op = new DecD( this.processor );
 		
 		op.execute();
 		
 		Assert.assertEquals((byte) 1, this.processor.getRegisters().getFlag(Flag.H));
 	}
 	
 	@Test
 	public void testNFlag() {
 		
 		byte value = 13;
 		this.processor.getRegisters().setRegister(Register.A, value);
 		this.op = new DecA( this.processor );
 		
 		op.execute();
 		
 		Assert.assertEquals((byte) 1, this.processor.getRegisters().getFlag(Flag.N));
 	}
 }
