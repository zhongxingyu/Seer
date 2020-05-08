 package ch.zhaw.inf3.emulator;
 
 public class Instruction {
 	public String mnemonic;
 	public String opcode_pattern;
 	public short opcode_bitmask;
 	public int num_operands;
 	public short[] operands; // stores the operands value once decoded
 	public short[] operand_bitmasks;
 	public int[] operand_offsets; // distance to LSb
 
 	public Instruction(String mnemonic, String opcode_pattern, int num_operands){
 		this.mnemonic = mnemonic;
 		this.opcode_pattern = opcode_pattern;
 		this.num_operands = num_operands;
 		operands = new short[num_operands];
 		operand_bitmasks = new short[num_operands];
 		operand_offsets = new int[num_operands];
 		opcode_bitmask = genBitmaskFromOpcode(opcode_pattern);
 		parseOperandMasksAndOffsets();
 	}
 	
 	public short encodeOperandValues(short[] ops) {
 		short s = opcode_bitmask;
 		operands = ops;
 		for (int i = 0; i < num_operands; i++) {
 			s |= (ops[i] << operand_offsets[i]);
 			// s = s | (ops[i]<<operand_offsets[i]);
 		}
 		return s;
 
 	}
 
 	public void decodeOperandValues(short word){
 		for (int i = 0; i < num_operands; i++) {
 			operands[i] = (short)((word & operand_bitmasks[i])  >>> operand_offsets[i]); // logical right shift
 		}
 	}
 	
 	private short genBitmaskFromOpcode(String op_pattern){
 		short bm = 0;
 		for (int i = 0; i < op_pattern.length(); i++) {
 			Character c = op_pattern.charAt(i);
 			int n = Character.getNumericValue(c);
 			if (n == 1) {
 				bm += (short)( 1 << (15-i) );				
 			}
 			//System.out.printf("%s:%s %s %s\n",i, c, n, bm);
 		}
 		return bm;
 	}
 	
 	private void parseOperandMasksAndOffsets(){
 		int found = 0;
 		
 		// parse all two-digit xx Register operands
 		int index = -2;
 		for (int i = 0; i < num_operands && index != -1; i++) {
 			index = opcode_pattern.indexOf("xx", index+2);
 			if (index >= 0) {
				int offset = 16 - (index+2);
 				operand_offsets[i] = offset;
 				operand_bitmasks[i]= (short)(0x03 << offset);
 				found++;
 			}
 		}
 		
 		if (found < num_operands) {
 			// parse the remaining <num> operand, if any
 			int start = opcode_pattern.indexOf('<',0);
 			if (start >= 0) {
 				operand_offsets[found] = 0;
				operand_bitmasks[found] = (short)(0xffff >>> start) ;
 			}
 		}
 	}
 
 }
