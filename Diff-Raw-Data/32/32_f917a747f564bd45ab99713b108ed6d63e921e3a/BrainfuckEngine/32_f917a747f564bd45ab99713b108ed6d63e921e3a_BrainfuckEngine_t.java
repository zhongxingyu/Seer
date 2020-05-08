 package com.dmillerw.brainfuckInterpreter;
 
 public class BrainfuckEngine {
 
 	public byte[] data;
 	
 	public int cells;
 	
 	public int dataPointer;
 	
 	public int charPointer;
 	
 	public BrainfuckEngine(int cells) {
 		initate(cells);
 	}
 	
 	public void initate(int cells) {
 		data = new byte[cells];
 		dataPointer = 0;
 		charPointer = 0;
 		this.cells = cells;
 	}
 	
 	public void clear() {
 		data = new byte[cells];
 		dataPointer = 0;
 		charPointer = 0;
 	}
 	
 	public void interpret(char token) {
 		if (token == Token.DATA_INC) {
 			if (dataPointer + 1 <= data.length) {
 				dataPointer++;
 			}
 		} else if (token == Token.DATA_DEC) {
 			if (dataPointer - 1 >= 0) {
 				dataPointer--;
 			}
 		} else if (token == Token.BYTE_INC) {
 			if (data[dataPointer] + 1 <= Byte.MAX_VALUE) {
 				data[dataPointer]++;
 			}
 		} else if (token == Token.BYTE_DEC) {
 			if (data[dataPointer] - 1 >= 0) {
 				data[dataPointer]--;
 			}
		} else if (token == Token.BYTE_IN) {
			//TODO Uhhh, how do
		} else if (token == Token.BYTE_OUT) {
			//Temporary
			System.out.println("Byte out");
			System.out.println(data[dataPointer]);
 		}
 	}
 	
 }
