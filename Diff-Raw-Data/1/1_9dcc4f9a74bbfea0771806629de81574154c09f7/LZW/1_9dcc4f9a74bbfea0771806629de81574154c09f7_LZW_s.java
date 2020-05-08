 package com.lzwcompressor;
 
import java.io.BufferedInputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.EOFException;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 
 public class LZW {
 	private File file;
 	private FileInputStream file_input;
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
 
 		char symbol8 = 0x0;
 		int symbol = 0xFFFF;
 		int offset = 8;
 
 		while (i < s.length()) {
 			c = s.charAt(i);
 			if (dicoCompression.containsValue(w + c)) {
 				w = w + c;
 			} else {
 				dicoCompression.put(dicoCompression.getIndex(), w + c);
 				if (dicoCompression.getKey(w) <= 255) {
 					System.out.println(w);
 					os.write(w.charAt(0));
 				} else {
 					if (((dicoCompression.getIndex() - 1) > limit)
 							&& (symbol8 == 0x0)) {
 						writeCode(os, symbol8);
 						symbol8 = 0x01;
 						offset = 9;
 						limit = 511;
 					} else if (((dicoCompression.getIndex() - 1) > limit)) {
 						writeCode(os, symbol & offset);
 						symbol = 0xFFFF;
 						limit = limit * 2 + 1;
 						offset++;
 					}
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
 		int limit = 255;
 		int mask = 0x00ff;
 		Boolean test = false;
 
 		ArrayList<Integer> compressed = new ArrayList<Integer>();
 
 		dicoDecompression = new Dictionary();
 		dicoDecompression.init();
 
 		file = new File(filename);
 
 		file_input = new FileInputStream(file);
 
 		compressed = readCompressedFile(file_input);
 		code = compressed.get(index) >> 24;
 		code &= mask;
 		c = dicoDecompression.getValue(code);
 		System.out.println(c);
 
 		w = c;
 
 		currentByte++;
 
 		while (code != -1) {
 
 			if ((32 - ((8 + offset) + startBit)) >= 0) {
 				code = compressed.get(index) >> (32 - ((8 + offset) + startBit));
 				code = code & mask;
 				
 				c = dicoDecompression.getValue(code);
 				
 				System.out.println("offset "+offset+" index "+index+" byte "+" code "+code+" char "+c);	
 				
 				if (32 - ((8 + offset) + startBit) == 0) {
 					index++;
 					startBit += 8 + offset;
 					startBit %= 32;
 				} else {
 					startBit += 8 + offset;
 					startBit %= 32;
 				}
 			} else {
 				startBit = Math.abs(32 - (8 + offset) + startBit);
 				
 				System.out.println("offset "+offset+" index "+index+" code "+code+" char "+c);	
 				System.out.println("startBit "+startBit);
 				
 				code = compressed.get(index) >> (32 - ((32 - 8 + offset - startBit)));
 				code = code & startBit;
 				index++;
 				code |= compressed.get(index) >> (32 - startBit);
 
 				c = dicoDecompression.getValue(code);
 				
 				startBit += 8 + offset;
 				startBit %= 32;
 			}
 
 			if (code == 0x0 && !test) {
 				offset += 1;
 				mask += 0x0001;
 				limit = limit * 2 + 1;
 				test = true;
 				continue;
 			} else if (code == limit) {
 				offset += 1;
 				mask += 0x0001;
 				limit = limit * 2 + 1;
 				continue;
 			}
 
 			if (code > 255 && dicoDecompression.containsKey(code)) {
 				entree = dicoDecompression.getValue(code);
 			} else if (code > 255 && !dicoDecompression.containsKey(code)) {
 				entree = w + w.charAt(0);
 			} else {
 				entree = c;
 			}
 
 			System.out.println(entree);
 
 			dicoDecompression.put(dicoDecompression.getIndex(),
 					w + entree.charAt(0));
 
 			// System.out.println(code);
 
 			w = entree;
 
 			// System.out.println(c);
 		}
 	}
 
 	private int read_char(FileInputStream file) throws IOException {
 		int return_value;
 		int input_bit_count = 0;
 		int input_bit_buffer = 0;
 
 		while (input_bit_count <= 24) {
 			input_bit_buffer |= (int) file_input.read() << (24 - input_bit_count);
 			input_bit_count += 8;
 		}
 		// return_value = (int) (input_bit_buffer >> (32 - numBits));
 		return_value = input_bit_buffer;
 		// input_bit_buffer <<= numBits;
 		input_bit_count -= numBits;
 
 		return (return_value);
 	}
 
 	private ArrayList<Integer> readCompressedFile(FileInputStream file)
 			throws IOException {
 		DataInputStream dsi = new DataInputStream(file);
 		ArrayList<Integer> result = new ArrayList<Integer>();
 		int code = 0;
 		try {
 			while (dsi.available() >= 4) {
 				code = dsi.readInt();
 				result.add(code);
 			}
 			int off= 0;
 			code = 0x0;
 			while((code != -1) && (off <= 16)){
 				System.out.println("coucou");
 				code |= (((int)dsi.readByte()) << (16 - off));
 				System.out.println(code);
 				dsi.readByte();
 				off += 8;
 			}
 			
 			System.out.println("hello");
 		} catch (EOFException e) {
 			result.add(code);
 			dsi.close();
 			System.out.println("End of stream encountered");
 		}
 //		for(int i = 0; i< result.size(); i++){
 //			System.out.print(result.get(i)+ " ");
 //		}
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
