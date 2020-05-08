 package ch.zhaw.inf3.emulator.test;
 
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 
 import ch.zhaw.inf3.emulator.Instruction;
 import ch.zhaw.inf3.emulator.InstructionSetArchitecture;
 import ch.zhaw.inf3.emulator.TransformNumbersAndFormats;
 
 @RunWith(Parameterized.class)
 public class TestISA {
 	private InstructionSetArchitecture isa;
 	private Instruction testInstruction;
 	private String testMnemonic;
 
 	public TestISA(Instruction ins) {
 		testInstruction = ins;
 		testMnemonic = ins.mnemonic;
 	}
 
 	@Before
 	public void setUp() {
 		isa = new InstructionSetArchitecture();
 	}
 
 	@Parameterized.Parameters
 	public static Collection<?> instructionsUnderTest() {
 		InstructionSetArchitecture isa = new InstructionSetArchitecture();
 		ArrayList<Object[]> list = new ArrayList<Object[]>();
 		for (Instruction instruction : isa.instructions) {
 			list.add(new Object[] { instruction });
 		}
 		return list;
 	}
 
 	@Test
 	public void testEncodeDecode() {
 		short[] ops = { 1, 500 };
 		short word = testInstruction.encodeOperandValues(ops);
 		Instruction decodedIns = isa.decodeWord(word);
 		assertEquals(testMnemonic, decodedIns.mnemonic);
 	}
 	
 	@Test
 	public void testOperandsMaxValue() {
 		short[] ops = new short[2];
 		for (int i=0; i < testInstruction.num_operands; i++) {
 			ops[i] = (short)(testInstruction.operand_bitmasks[i] >> testInstruction.operand_offsets[i]);
			//System.out.println(ops[i] +" "+ TransformNumbersAndFormats.decToBinString(ops[i]));
 		}
 		short word = testInstruction.encodeOperandValues(ops);
 		Instruction decodedIns = isa.decodeWord(word);
 		for (int i=0; i < testInstruction.num_operands; i++) {
 			assertEquals(ops[i], decodedIns.operands[i]);
 		}
 	}
 	
 	@Test
 	public void testOperandsMinValue() {
 		short[] ops = {0,0};
 		short word = testInstruction.encodeOperandValues(ops);
 		Instruction decodedIns = isa.decodeWord(word);
 		for (int i=0; i < testInstruction.num_operands; i++) {
 			assertEquals(ops[i], decodedIns.operands[i]);
 		}
 	}
 }
