 package gameBoy.tests.opcodeTests;
 
 import junit.framework.Assert;
import gameBoy.cpu.opcodes.load.*;
 import gameBoy.cpu.GameBoyProcessor;
 import gameBoy.cpu.Register;
 import gameBoy.interfaces.IOpcode;
 import gameBoy.interfaces.IProcessor;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class LoadImmToRegTests {
 	private IProcessor processor;
 	private IOpcode op;
 	
 	@Before
 	public void setUp() throws Exception {
 		this.processor = new GameBoyProcessor();
 	}
 
 	@Test
 	public void testLoadImmToReg8() {
 		byte value = 30;
 		this.op = new LoadImmToB( processor , value );
 		
 		op.execute();
 		
 		Assert.assertEquals(this.processor.getRegisters().getRegister(Register.B), value);
 	}
 	
 	@Test
 	public void testLoadImmToReg16() {
 		short value = 300;
 		this.op = new LoadImmToBC( processor , value );
 		
 		op.execute();
 		
 		Assert.assertEquals(this.processor.getRegisters().getRegister(Register.BC), value);
 	}
 
 }
