 package kuusisto.finn.phi;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 public class RecordID {
 	
 	private byte[] bytes;
 	private List<Citation> citations;
 	
 	public RecordID(byte[] bytes) {
 		this.bytes = bytes;
 		this.citations = new ArrayList<Citation>();
 		this.parse();
 	}
 	
 	public RecordID(List<Byte> bytes) {
 		this.bytes = new byte[bytes.size()];
 		for (int i = 0; i < bytes.size(); i++) {
 			this.bytes[i] = bytes.get(i);
 		}
 		this.citations = new ArrayList<Citation>();
 		this.parse();
 	}
 	
 	private byte leftNibble(int index) {
 		return (byte)(this.bytes[index] & RecordID.LEFT_NIBBLE);
 	}
 	
 	private byte rightNibble(int index) {
 		return (byte)(this.bytes[index] & RecordID.RIGHT_NIBBLE);
 	}
 	
 	private int indexOfLeftNibble(byte nibble, int from) {
 		for (int i = from; i < this.bytes.length; i++) {
 			byte left = this.leftNibble(i);
 			if (left == nibble) {
 				return i;
 			}
 		}
 		return -1;
 	}
 	
 	private int indexOfLeftNibble(byte nibble) {
 		return this.indexOfLeftNibble(nibble, 0);
 	}
 	
 	private int indexOfByte(byte val, int from) {
 		for (int i = from; i < this.bytes.length; i++) {
 			if (this.bytes[i] == val) {
 				return i;
 			}
 		}
 		return -1;
 	}
 	
 	private int indexOfByte(byte val) {
 		return this.indexOfByte(val, 0);
 	}
 	
 	@Override
 	public String toString() {
 		//TODO this is pretty useless so far
 		StringBuilder str = new StringBuilder();
 		str.append("\n[");
 		for (int i = 0; i < this.bytes.length; i++) {
 			str.append(Utils.bitString(this.bytes[i]));
 			if (i < this.bytes.length - 1) {
 				str.append(",");
 			}
 		}
 		str.append("]\n");
 		byte[] text = Utils.unsetBit(this.bytes, 7);
 		str.append(Utils.convertToASCII(text));
 		str.append("\n");
 		str.append(this.citations.toString());
 		return str.toString();
 	}
 	
 	///////////
 	//Parsing//
 	///////////
 	
 	private void parse() {
 		boolean foundDescriptor = false;
		for (int i = 0; i < this.bytes.length && !foundDescriptor;) {
 			Citation toAdd = new Citation();
 			boolean add = true;
 			//first identify the left nibble
 			byte curr = this.bytes[i];
 			byte left = this.leftNibble(i);
 			//and process the citation (can't use switch w/ my masks)
 			if (left == RecordID.L_Z_LEVEL) {
 				toAdd.Z = true;
 				i = this.completeParse(toAdd, i, (i + 1));
 			}
 			else if (left == RecordID.L_Y_LEVEL) {
 				toAdd.Y = true;
 				i = this.completeParse(toAdd, i, (i + 1));
 			}
 			else if (left == RecordID.L_X_LEVEL) {
 				toAdd.X = true;
 				i = this.completeParse(toAdd, i, (i + 1));
 			}
 			else if (left == RecordID.L_W_LEVEL) {
 				toAdd.W = true;
 				i = this.completeParse(toAdd, i, (i + 1));
 			}
 			else if (left == RecordID.L_V_LEVEL) {
 				toAdd.V = true;
 				i = this.completeParse(toAdd, i, (i + 1));
 			}
 			else if (left == RecordID.L_N_LEVEL) {
 				toAdd.N = true;
 				i = this.completeParse(toAdd, i, (i + 1));
 			}
 			else if (left == RecordID.L_ESCAPE) {
 				//make sure we're not at the end
 				if ((i + 1) < this.bytes.length) {
 					//sign bit is extra
 					byte next = Utils.unsetBit(this.bytes[i + 1], 7);
 					//first check for citation A, B, C or D
 					if (next == RecordID.R_A_LEVEL) {
 						toAdd.A = true;
 						i = this.completeParse(toAdd, i, (i + 2));
 					}
 					else if (next == RecordID.R_B_LEVEL) {
 						toAdd.B = true;
 						i = this.completeParse(toAdd, i, (i + 2));
 					}
 					else if (next == RecordID.R_C_LEVEL) {
 						toAdd.C = true;
 						i = this.completeParse(toAdd, i, (i + 2));
 					}
 					else if (next == RecordID.R_D_LEVEL){
 						toAdd.D = true;
 						i = this.completeParse(toAdd, i, (i + 2));
 					} //otherwise check for descriptor
 					else if (next >= 97 && next <= 122) {
 						//found a descriptor
 						add = false;
 						foundDescriptor = true;
						
 					}
 					else { //this shouldn't happen
 						add = false;
 						System.err.println("Unexpected Escape byte!1");
 						System.err.println(Utils.bitString(next));
 					}
 				}
 				else {
 					add = false;
 					System.err.println("Unexpected Escape byte!2");
 				}
 			}
 			else if (left == RecordID.L_SPECIAL) {
 				//it will only be one of these obviously
 				toAdd.EOB = (curr == RecordID.EOB);
 				toAdd.EOF = (curr == RecordID.EOF);
 				toAdd.EXCEPTION_START = (curr == RecordID.EXCEPTION_START);
 				toAdd.EXCEPTION_END = (curr == RecordID.EXCEPTION_END);
 				//shouldn't find this unless something went wrong
 				if (curr == RecordID.EOS) {
 					add = false;
 					System.err.println("Started parse at EOS byte!");
 				}
				else {
					i++;
				}
 			}
 			else {
 				add = false;
 				System.err.println("Unrecognizable left nibble" +
 						Utils.bitString(left) + "!");
 			}
 			//add the citation if it should be
 			if (add) {
 				this.citations.add(toAdd);
 			}
 		}
 	}
 	
 	private int completeParse(Citation citation, int byteIndex,
 			int dataIndex) {
 		int retIndex = -1;
 		//identify the right nibble
 		byte right = this.rightNibble(byteIndex);
 		if (right == RecordID.R_INCREMENT) {
 			citation.INCRMENT = true;
 			retIndex = dataIndex; //didn't process any data
 		}
 		else if (right == RecordID.R_7BIN) {
 			citation.BIN = true;
 			citation.bin = this.bin7Bit(dataIndex);
 			retIndex = (dataIndex + 1);
 		}
 		else if (right == RecordID.R_7BIN_1CHAR) {
 			citation.BIN_ASCII = true;
 			citation.bin = this.bin7Bit(dataIndex);
 			citation.ascii = this.asciiChar(dataIndex + 1);
 			retIndex = (dataIndex + 2);
 		}
 		else if (right == RecordID.R_7BIN_ASCII) {
 			citation.BIN_ASCII = true;
 			citation.bin = this.bin7Bit(dataIndex);
 			int strFrom = (dataIndex + 1);
 			int strTo = this.indexOfByte(RecordID.EOS, strFrom);
 			citation.ascii = this.asciiString(strFrom, strTo);
 			retIndex = (strTo + 1);
 		}
 		else if (right == RecordID.R_14BIN) {
 			citation.BIN = true;
 			citation.bin = this.bin14Bit(dataIndex, (dataIndex + 1));
 			retIndex = (dataIndex + 2);
 		}
 		else if (right == RecordID.R_14BIN_1CHAR) {
 			citation.BIN_ASCII = true;
 			citation.bin = this.bin14Bit(dataIndex, (dataIndex + 1));
 			citation.ascii = this.asciiChar(dataIndex + 2);
 			retIndex = (dataIndex + 3);
 		}
 		else if (right == RecordID.R_14BIN_ASCII) {
 			citation.BIN_ASCII = true;
 			citation.bin = this.bin14Bit(dataIndex, (dataIndex + 1));
 			int strFrom = (dataIndex + 2);
 			int strTo = this.indexOfByte(RecordID.EOS, strFrom);
 			citation.ascii = this.asciiString(strFrom, strTo);
 			retIndex = (strTo + 1);
 		}
 		else if (right == RecordID.R_SAME_1CHAR) {
 			//TODO debug
 			System.err.println("****Found same_1char!****");
 			System.err.println(this.toString());
 			retIndex = (dataIndex + 1);
 		}
 		else if (right == RecordID.R_ASCII) {
 			citation.ASCII = true;
 			int strFrom = dataIndex;
 			int strTo = this.indexOfByte(RecordID.EOS, strFrom);
 			citation.ascii = this.asciiString(strFrom, strTo);
 			retIndex = (strTo + 1);
 		}
 		else {
 			//must be a literal value
 			citation.BIN = true;
 			citation.bin = right;
 			retIndex = dataIndex; //no processing needed
 		}
 		return retIndex;
 	}
 	
 	private String asciiChar(int index) {
 		return this.asciiString(index, (index + 1));
 	}
 	
 	private String asciiString(int from, int to) {
 		byte[] chars = Arrays.copyOfRange(this.bytes, from, to);
 		chars = Utils.unsetBit(chars, 7);
 		return Utils.convertToASCII(chars);
 	}
 	
 	private int bin7Bit(int index) {
 		return Utils.unsetBit(this.bytes[index], 7);
 	}
 	
 	private int bin14Bit(int index1, int index2) {
 		return ((this.bin7Bit(index1) << 7) + this.bin7Bit(index2));
 	}
 	
 	/////////////
 	//Bit-masks//
 	/////////////
 	
 	private static final byte LEFT_NIBBLE =  Utils.mask("11110000");
 	private static final byte RIGHT_NIBBLE = Utils.mask("00001111");
 	
 	private static final byte L_Z_LEVEL = Utils.mask("10000000");
 	private static final byte L_Y_LEVEL = Utils.mask("10010000");
 	private static final byte L_X_LEVEL = Utils.mask("10100000");
 	private static final byte L_W_LEVEL = Utils.mask("10110000");
 	private static final byte L_V_LEVEL = Utils.mask("11000000");
 	private static final byte L_N_LEVEL = Utils.mask("11010000");
 	private static final byte L_ESCAPE =  Utils.mask("11100000");
 	private static final byte L_SPECIAL = Utils.mask("11110000");
 
 	private static final byte R_A_LEVEL =     Utils.mask("00000000");
 	private static final byte R_B_LEVEL =     Utils.mask("00000001");
 	private static final byte R_C_LEVEL =     Utils.mask("00000010");
 	private static final byte R_D_LEVEL =     Utils.mask("00000011");	
 	private static final byte R_INCREMENT =   Utils.mask("00000000");
 	private static final byte R_7BIN =        Utils.mask("00001000");
 	private static final byte R_7BIN_1CHAR =  Utils.mask("00001001");
 	private static final byte R_7BIN_ASCII =  Utils.mask("00001010");
 	private static final byte R_14BIN =       Utils.mask("00001011");
 	private static final byte R_14BIN_1CHAR = Utils.mask("00001100");
 	private static final byte R_14BIN_ASCII = Utils.mask("00001101");
 	private static final byte R_SAME_1CHAR =  Utils.mask("00001110");
 	private static final byte R_ASCII =       Utils.mask("00001111");
 	
 	private static final byte EOS = Utils.mask("11111111");
 	private static final byte EOB = Utils.mask("11111110");
 	private static final byte EOF = Utils.mask("11110000");
 	
 	private static final byte EXCEPTION_START = Utils.mask("11111000");
 	private static final byte EXCEPTION_END =   Utils.mask("11111001");
 
 }
