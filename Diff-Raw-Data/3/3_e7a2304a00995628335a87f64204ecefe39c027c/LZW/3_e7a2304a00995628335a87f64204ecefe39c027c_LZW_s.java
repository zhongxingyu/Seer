 package com.lzwcompressor;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 
 public class LZW {
 	private File file;
 	private FileInputStream file_input;
 	private DataInputStream data_input;
 	private Dictionary dicoCompression;
 	private Dictionary dicoDecompression;
 	private int numBits;
 	private int output_bit_buffer = 0;
 	private int output_bit_count = 0;
 
 	public LZW() {
 		numBits = 12;
 	}
 
 	public LZW(int numBits) {
 		this.numBits = numBits;
 	}
 	
 
 	public void compression(String s) throws IOException {
 		dicoCompression = new Dictionary(1 << numBits);
 		dicoCompression.init();
 		String w = "";
 		char c;
 		int i = 0;
 		int limit = 255;
 		String filename = "test.txt";
 		DataOutputStream os = new DataOutputStream(new FileOutputStream(
 				filename));
 		
 		int symbol8 = 0x0;
 		int symbol = 0xFFFF;
 		int offset = 8;
 		
 		while (i < s.length()) {
 			c = s.charAt(i);
 			if (dicoCompression.containsValue(w + c)) {
 				w = w + c;
 			} else {
 				dicoCompression.put(dicoCompression.getIndex(), w + c);
 				if(((dicoCompression.getIndex() - 1) > limit) && (symbol8 == 0x0)){
 					writeCode(os, symbol8);
 					symbol8 = 0x0001;
 					offset = 9;
 					limit = 511;
 				} else if(((dicoCompression.getIndex() - 1) > limit)){
 					writeCode(os, symbol & offset);
 					symbol = 0xFFFF;
					limit *= 2;	
 				}
 				if (dicoCompression.getKey(w) <= 255) {
 					System.out.println(w);
 					os.write(w.charAt(0));
 				} else {
 					System.out.println(dicoCompression.getKey(w));
 					// System.out.println(w);
 					writeCode(os, dicoCompression.getKey(w));
 				}
 				w = String.valueOf(c);
 			}
 			i++;
 		}
 		System.out.println(dicoCompression.getKey(w));
 		// System.out.println(w);
 		writeCode(os, dicoCompression.getKey(w));
 		os.flush();
 		os.close();
 	}
 
 	public void decompression(String filename) throws IOException {
 		int code;
 		String c = null, w = null, entree = null;
 		int index = 0;
 		int offset = 0;
 		int currentByte = 1;
 		int startBit = 0;
 
 		ArrayList<Integer> compressed = new ArrayList<Integer>();
 
 		dicoDecompression = new Dictionary();
 		dicoDecompression.init();
 
 		file = new File(filename);
 
 		file_input = new FileInputStream(file);
 
 		compressed = readCompressedFile(file_input);
 		code = compressed.get(index) >> (32 - ((8 + offset) * currentByte + startBit));
 		c = dicoDecompression.getValue(code);
 
 		w = c;
 
 		int mask = 0x000f;
 
 		while (code != -1) {
 
 			if ((32 - ((8 + offset) * currentByte + startBit)) >= 0) {
 				code = compressed.get(index) >> (32 - ((8 + offset)
 						* currentByte + startBit));
 				code = code & mask;
 
 				c = dicoDecompression.getValue(code);
 
 				if (32 - ((8 + offset) * currentByte + startBit) == 0) {
 					index++;
 					currentByte = 1;
 				} else {
 					currentByte++;
 				}
 			} else {
 				startBit = Math.abs(32 - (8 + offset) * currentByte);
 				code = compressed.get(index) >> (32 - ((8 + offset)
 						* currentByte - 2 * startBit));
 				code = code & startBit;
 				currentByte = 1;
 				index++;
 				code |= compressed.get(index) >> (32 - ((8 + offset)
 						* currentByte + startBit));
 
 				c = dicoDecompression.getValue(code);
 			}
 
 			if (code == 0x0) {
 				offset += 1;
 			}
 
 			if (code > 255 && dicoDecompression.containsKey(code)) {
 				entree = dicoDecompression.getValue(code);
 			} else if (code > 255 && !dicoDecompression.containsKey(code)) {
 				entree = w + w.charAt(0);
 			} else {
 				entree = c;
 			}
 			System.out.println(entree);
 			
 			dicoDecompression.put(dicoDecompression.getIndex(), w+entree.charAt(0));
 			
 			w = entree;
 
 			System.out.println(code);
 		}
 	}
 
 	private int read_char(FileInputStream file) throws IOException {
 		int return_value;
 		int input_bit_count = 0;
 		long input_bit_buffer = 0;
 
 		while (input_bit_count <= 24) {
 			input_bit_buffer |= (long) file_input.read() << (24 - input_bit_count);
 			input_bit_count += 8;
 		}
 		return_value = (int) (input_bit_buffer >> (32 - numBits));
 		input_bit_buffer <<= numBits;
 		input_bit_count -= numBits;
 
 		return (return_value);
 	}
 
 	private ArrayList<Integer> readCompressedFile(FileInputStream file)
 			throws IOException {
 		ArrayList<Integer> result = new ArrayList<Integer>();
 		int code = 0;
 		while (code != -1) {
 			code = read_char(file);
 			result.add(code);
 		}
 		return result;
 	}
 
 	private void writeCode(DataOutputStream os, int code) {
 		output_bit_buffer |= code << (32 - numBits - output_bit_count);
 		output_bit_count += numBits;
 
 		while (output_bit_count >= 8) {
 			try {
 				os.write(output_bit_buffer >> 24);
 			} catch (IOException e) {
 				System.out
 						.println("IOException while writing the output file !");
 				System.exit(1);
 			}
 			output_bit_buffer <<= 8;
 			output_bit_count -= 8;
 		}
 	}
 
 }
