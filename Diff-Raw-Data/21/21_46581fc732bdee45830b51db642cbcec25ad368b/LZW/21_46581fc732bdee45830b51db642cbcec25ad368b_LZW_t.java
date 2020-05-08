 package com.lzwcompressor;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 public class LZW {
 	private File file;
 	private FileInputStream file_input;
 	private DataInputStream data_input;
 	private Dictionary dicoCompression;
 	private Dictionary dicoDecompression;
 	private int numBits;
 
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
 		String filename = "test.txt";
 		DataOutputStream os = new DataOutputStream(new FileOutputStream(
 				filename));
 		while (i < s.length()) {
 			c = s.charAt(i);
 			if (dicoCompression.containsValue(w + c)) {
 				w = w + c;
 			} else {
 				dicoCompression.put(255 + (i - ((w + c).length() - 2)), w + c);
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
 		Short code = null;
 		String c = null, w = null, entree = null;
 
 		file = new File(filename);
		
		file_input = new FileInputStream(file);

 		data_input = new DataInputStream(file_input);
 
		code = data_input.readShort();
 
 		c = dicoCompression.getValue(code);
 
 		int i = 1;
 		w = c;
 		dicoDecompression = new Dictionary();
 		while (code != -1) {
 			if (code > 255 && dicoDecompression.containsKey(code)) {
 				entree = w;
 			} else if (code > 255 && !dicoDecompression.containsKey(code)) {
 				entree = w + w.charAt(0);
 			} else {
 				entree = c;
 			}
 			System.out.println(entree);
 			dicoDecompression.put(255 + i, w + entree.charAt(0));
 			w = entree;
 			i++;
 			c = dicoCompression.getValue(code);
			
			code = data_input.readShort();
 		}
 	}
 
 	void writeCode(DataOutputStream os, int code) throws IOException {
 		// System.out.println("BIT TO WRITE "
 		// + (32 - Integer.numberOfLeadingZeros(code)));
 		for (int i = 0; i < (32 - Integer.numberOfLeadingZeros(code)); i++) {
 			os.write(code & 1);
 			code /= 2;
 		}
 	}
 
 }
