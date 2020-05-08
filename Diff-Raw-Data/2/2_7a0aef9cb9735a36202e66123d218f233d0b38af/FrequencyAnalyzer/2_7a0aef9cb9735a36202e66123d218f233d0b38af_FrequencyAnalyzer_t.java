 package com.assignment2;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Arrays;
 
 public class FrequencyAnalyzer {
 	
 	FrequencyAnalyzer() {
 		this.makeAlphabetArray();
 				
 	}
 
 	public void printMostCommonLetter(String strCiphertext) {
 		
 		char[] chrArrCiphertext = strCiphertext.toCharArray();
 		int[] intLetterFrequency;
 							
 		intLetterFrequency = new int[chrArrLetters.length];
 		
 		for(int j=0;j<chrArrLetters.length;j++) {
 			for(int i=0;i<strCiphertext.length();i++) {
 				if(chrArrCiphertext[i] == chrArrLetters[j]) {
 					intLetterFrequency[j] = intLetterFrequency[j] + 1;
 				}
 			}
 		}
 		
 		for(int k=0;k<intLetterFrequency.length;k++) {
 			System.out.println(intLetterFrequency[k] + "\t" + chrArrLetters[k] + "\t" + 100*((double)intLetterFrequency[k]/ (double)chrArrCiphertext.length));
 		}		
 	}
 	
 	public void printMostCommonBigram(String strCiphertext) {
 		
 		char[] chrArrCiphertext = strCiphertext.toCharArray();
 		int[] intBigramFrequency;
 		
 		String[] strArrBigramLetters;
 		String[] strArrBigramCipher;
 		
 		strArrBigramLetters = new String[chrArrLetters.length*chrArrLetters.length];
 		strArrBigramCipher = new String[strCiphertext.length()-1]; //-1 because the first letter cannot form a bigram with the letter before it
 		intBigramFrequency = new int[chrArrLetters.length*chrArrLetters.length];
 		
 		// Make 26x26 bigram array
 		int intIndexCount = 0;
 		for(int bi1=0;bi1<chrArrLetters.length;bi1++) {
 			for(int bi2=0;bi2<chrArrLetters.length;bi2++) {
 				strArrBigramLetters[intIndexCount] = new StringBuilder().append(chrArrLetters[bi1]).append(chrArrLetters[bi2]).toString();
 				intIndexCount++;
 			}
 		}
 		
 //		for(int bi1=0;bi1<(chrArrLetters.length*chrArrLetters.length);bi1++) {
 //			System.out.println(bi1 + " : " + strArrBigramLetters[bi1]);
 //		}
 		
 		// Make cipher bigram
 		for(int index=1;index<chrArrCiphertext.length;index++) {
 			strArrBigramCipher[index-1] = new StringBuilder().append(chrArrCiphertext[index-1]).append(chrArrCiphertext[index]).toString();
 		}
 				
 		for(int j=0;j<strArrBigramLetters.length;j++) {
 			for(int i=0;i<strArrBigramCipher.length;i++) {
 				if(strArrBigramLetters[j].equals(strArrBigramCipher[i])) {
 					intBigramFrequency[j] = intBigramFrequency[j] + 1;
 				}
 			}
 		}
 		
 		// Display bigram frequency chart
 		for(int k=0;k<intBigramFrequency.length;k++) {
 			System.out.println(intBigramFrequency[k] + "\t" + strArrBigramLetters[k] + "\t" + 100*((double) intBigramFrequency[k] / (double)strArrBigramCipher.length));
 		}
 	}
 	
 	public void printMostCommonTrigram(String strCiphertext) throws IOException {
 		
 		char[] chrArrCiphertext = strCiphertext.toCharArray();
 		int[] intTrigramFrequency;
 		
 		String[] strArrTrigramLetters;
 		String[] strArrTrigramCipher;
 		
 		strArrTrigramLetters = new String[chrArrLetters.length*chrArrLetters.length*chrArrLetters.length];
 		strArrTrigramCipher = new String[strCiphertext.length()-2]; //-1 because the first letter cannot form a bigram with the letter before it
 		intTrigramFrequency = new int[chrArrLetters.length*chrArrLetters.length*chrArrLetters.length];
 		
 		// Make (26*26)x26 trigram array
 		int intIndexCount = 0;
 		for(int bi1=0;bi1<chrArrLetters.length;bi1++) {
 			for(int bi2=0;bi2<chrArrLetters.length;bi2++) {
 				for(int bi3=0;bi3<chrArrLetters.length;bi3++) {
 					strArrTrigramLetters[intIndexCount] = new StringBuilder().append(chrArrLetters[bi1]).append(chrArrLetters[bi2]).append(chrArrLetters[bi3]).toString();
 					intIndexCount++;
 				}
 			}
 		}
 		
 //		for(int bi1=0;bi1<(chrArrLetters.length*chrArrLetters.length);bi1++) {
 //			System.out.println(bi1 + " : " + strArrBigramLetters[bi1]);
 //		}
 		
 		// Make cipher trigram
 		for(int index=2;index<chrArrCiphertext.length;index++) {
 			strArrTrigramCipher[index-2] = new StringBuilder().append(chrArrCiphertext[index-2]).append(chrArrCiphertext[index-1]).append(chrArrCiphertext[index]).toString();
 		}
 				
 		for(int j=0;j<strArrTrigramLetters.length;j++) {
 			for(int i=0;i<strArrTrigramCipher.length;i++) {
 				if(strArrTrigramLetters[j].equals(strArrTrigramCipher[i])) {
 					intTrigramFrequency[j] = intTrigramFrequency[j] + 1;
 				}
 			}
 		}
 		
 		// Create file 
 		FileWriter fstream = new FileWriter("assig2-part1-monoalpha.txt");
 		BufferedWriter out = new BufferedWriter(fstream);
 				
 		// Write to file bigram frequency chart
 		for(int k=0;k<intTrigramFrequency.length;k++) {
 			out.write(intTrigramFrequency[k] + "\t" + strArrTrigramLetters[k] + "\t" + 100*((double) intTrigramFrequency[k] / (double)strArrTrigramCipher.length) + "\n");
 		}
 		
 		//Close the output stream
 		out.close();
 	}
 	
 	public void printMostCommonDigraphs(String strCiphertext) {
 		
 		char[] chrArrCiphertext = strCiphertext.toCharArray();
 		int[] intDigraphFrequency;
 		
 		String[] strArrBigramLetters;
 		String[] strArrDigraphCipher;
 		
 		strArrBigramLetters = new String[chrArrLetters.length*chrArrLetters.length];
 		strArrDigraphCipher = new String[strCiphertext.length()/2]; // length/2 because there are exactly n/2 digraphs for a text of n length
 		intDigraphFrequency = new int[chrArrLetters.length*chrArrLetters.length];
 		
 		// Make 26x26 bigram array
 		int intIndexCount = 0;
 		for(int bi1=0;bi1<chrArrLetters.length;bi1++) {
 			for(int bi2=0;bi2<chrArrLetters.length;bi2++) {
 				strArrBigramLetters[intIndexCount] = new StringBuilder().append(chrArrLetters[bi1]).append(chrArrLetters[bi2]).toString();
 				intIndexCount++;
 			}
 		}
 		
 //		for(int bi1=0;bi1<(chrArrLetters.length*chrArrLetters.length);bi1++) {
 //			System.out.println(bi1 + " : " + strArrBigramLetters[bi1]);
 //		}
 		
 		// Make cipher digraph
 //		int intDigraphIndexCount = 0;
 		for(int index=0;index<(chrArrCiphertext.length-1);index++) {
 			if(index % 2 == 0) {
 				strArrDigraphCipher[index/2] = new StringBuilder().append(chrArrCiphertext[index]).append(chrArrCiphertext[index+1]).toString();
 //				intDigraphIndexCount++;
 			}
 		}
 				
 		for(int j=0;j<strArrBigramLetters.length;j++) {
 			for(int i=0;i<strArrDigraphCipher.length;i++) {
 				if(strArrBigramLetters[j].equals(strArrDigraphCipher[i])) {
 					intDigraphFrequency[j] = intDigraphFrequency[j] + 1;
 				}
 			}
 		}
 		
 		// Display digraph frequency chart
 		for(int k=0;k<intDigraphFrequency.length;k++) {
 			System.out.println(intDigraphFrequency[k] + "\t" + strArrBigramLetters[k] + "\t" + 100*((double) intDigraphFrequency[k] / (double)strArrDigraphCipher.length));
 		}
 	}
 
 	public void printMostCommonQuadrigraph(String strCiphertext) throws IOException {
 		
 //		char[] chrArrCiphertext = strCiphertext.toCharArray();
 		int[] intQuadrigraphFrequency;
 		
 		String[] strArrQuadrigramLetters = new String[chrArrLetters.length*chrArrLetters.length*chrArrLetters.length*chrArrLetters.length];
 		String[] strArrQuadrigraphCipher = new String[strCiphertext.length()/4];
 		
 		strArrQuadrigramLetters = this.makeQuadrigraphArray();
 		strArrQuadrigraphCipher = this.makeCipherQuadrigraph(strCiphertext);
 				
 		intQuadrigraphFrequency = new int[chrArrLetters.length*chrArrLetters.length*chrArrLetters.length*chrArrLetters.length];
 				
 //		for(int bi1=0;bi1<(chrArrLetters.length*chrArrLetters.length);bi1++) {
 //			System.out.println(bi1 + " : " + strArrBigramLetters[bi1]);
 //		}
 				
 		for(int j=0;j<strArrQuadrigramLetters.length;j++) {
 			for(int i=0;i<strArrQuadrigraphCipher.length;i++) {
 				if(strArrQuadrigramLetters[j].equals(strArrQuadrigraphCipher[i])) {
 					intQuadrigraphFrequency[j] = intQuadrigraphFrequency[j] + 1;
 				}
 			}
 		}
 		
 //		// Display quadrigraph frequency chart
 //		for(int k=0;k<intQuadrigraphFrequency.length;k++) {
 //			System.out.println(intQuadrigraphFrequency[k] + "\t" + strArrQuadrigramLetters[k] + "\t" + 100*((double) intQuadrigraphFrequency[k] / (double)strArrQuadrigraphCipher.length));
 //		}
 		
 		// Write quadrigraph frequency chart to file -- too big to print
 		// Create file 
 		FileWriter fstream = new FileWriter("assig2-part2-playfair-quadrigraphs.txt");
 		BufferedWriter out = new BufferedWriter(fstream);
 				
 		// Write to file bigram frequency chart
 		for(int k=0;k<intQuadrigraphFrequency.length;k++) {
 			out.write(intQuadrigraphFrequency[k] + "\t" + strArrQuadrigramLetters[k] + "\t" + 100*((double) intQuadrigraphFrequency[k] / (double)strArrQuadrigraphCipher.length) + "\n");
 		}
 		
 		//Close the output stream
 		out.close();
 	
 	}
 	
 	public void printMostCommonSextagraph(String strCiphertext) throws IOException {
 		
 //		char[] chrArrCiphertext = strCiphertext.toCharArray();
 		int[] intSextagraphFrequency;
 		
 		String[] strArrSextagramLetters = new String[chrArrLetters.length*chrArrLetters.length*chrArrLetters.length*chrArrLetters.length*chrArrLetters.length*chrArrLetters.length];
 		String[] strArrSextagraphCipher = new String[strCiphertext.length()/6];
 		
 		strArrSextagramLetters = this.makeSextagraphArray();
 		strArrSextagraphCipher = this.makeCipherSextagraph(strCiphertext);
 				
 		intSextagraphFrequency = new int[chrArrLetters.length*chrArrLetters.length*chrArrLetters.length*chrArrLetters.length*chrArrLetters.length*chrArrLetters.length];
 				
 //		for(int bi1=0;bi1<(chrArrLetters.length*chrArrLetters.length);bi1++) {
 //			System.out.println(bi1 + " : " + strArrBigramLetters[bi1]);
 //		}
 				
 		for(int j=0;j<strArrSextagramLetters.length;j++) {
 			for(int i=0;i<strArrSextagraphCipher.length;i++) {
 				if(strArrSextagramLetters[j].equals(strArrSextagraphCipher[i])) {
 					intSextagraphFrequency[j] = intSextagraphFrequency[j] + 1;
 				}
 			}
 		}
 		
 //		// Display quadrigraph frequency chart
 //		for(int k=0;k<intQuadrigraphFrequency.length;k++) {
 //			System.out.println(intQuadrigraphFrequency[k] + "\t" + strArrQuadrigramLetters[k] + "\t" + 100*((double) intQuadrigraphFrequency[k] / (double)strArrQuadrigraphCipher.length));
 //		}
 		
 		// Write quadrigraph frequency chart to file -- too big to print
 		// Create file 
 		FileWriter fstream = new FileWriter("assig2-part2-playfair-sextagraphs.txt");
 		BufferedWriter out = new BufferedWriter(fstream);
 				
 		// Write to file bigram frequency chart
 		for(int k=0;k<intSextagraphFrequency.length;k++) {
 			out.write(intSextagraphFrequency[k] + "\t" + strArrSextagramLetters[k] + "\t" + 100*((double) intSextagraphFrequency[k] / (double)strArrSextagraphCipher.length) + "\n");
 		}
 		
 		//Close the output stream
 		out.close();
 	
 	}
 	
 	private void makeAlphabetArray() {
 		//chrArrLetters = new char[26];
 		
 		chrArrLetters[0] = 'A';
 		chrArrLetters[1] = 'B';
 		chrArrLetters[2] = 'C';
 		chrArrLetters[3] = 'D';
 		chrArrLetters[4] = 'E';
 		chrArrLetters[5] = 'F';
 		chrArrLetters[6] = 'G';
 		chrArrLetters[7] = 'H';
 		chrArrLetters[8] = 'I';
 		chrArrLetters[9] = 'J';
 		chrArrLetters[10] = 'K';
 		chrArrLetters[11] = 'L';
 		chrArrLetters[12] = 'M';
 		chrArrLetters[13] = 'N';
 		chrArrLetters[14] = 'O';
 		chrArrLetters[15] = 'P';
 		chrArrLetters[16] = 'Q';
 		chrArrLetters[17] = 'R';
 		chrArrLetters[18] = 'S';
 		chrArrLetters[19] = 'T';
 		chrArrLetters[20] = 'U';
 		chrArrLetters[21] = 'V';
 		chrArrLetters[22] = 'W';
 		chrArrLetters[23] = 'X';
 		chrArrLetters[24] = 'Y';
 		chrArrLetters[25] = 'Z';
 		
 	}
 	
 	private String[] makeQuadrigraphArray() {
 		
 		String[] strArrQuadrigramLetters = new String[chrArrLetters.length*chrArrLetters.length*chrArrLetters.length*chrArrLetters.length];
 		
 		// Make (26*26)x(26*26) quadrigram array
 		int intIndexCount = 0;
 		for(int bi1=0;bi1<chrArrLetters.length;bi1++) {
 			for(int bi2=0;bi2<chrArrLetters.length;bi2++) {
 				for(int bi3=0;bi3<chrArrLetters.length;bi3++) {
 					for(int bi4=0;bi4<chrArrLetters.length;bi4++) {
 						strArrQuadrigramLetters[intIndexCount] = new StringBuilder().append(chrArrLetters[bi1]).append(chrArrLetters[bi2]).append(chrArrLetters[bi3]).append(chrArrLetters[bi4]).toString();
 						intIndexCount++;
 					}
 				}
 			}
 		}
 		
 		return strArrQuadrigramLetters;
 	}
 	
 	private String[] makeSextagraphArray() {
 		
		String[] strArrSextagramLetters = new String[chrArrLetters.length*chrArrLetters.length*chrArrLetters.length*chrArrLetters.length*chrArrLetters.length*chrArrLetters.length];
 		
 		// Make (26*26)*(26*26)x(26*26) sextagram array
 		int intIndexCount = 0;
 		for(int bi1=0;bi1<chrArrLetters.length;bi1++) {
 			for(int bi2=0;bi2<chrArrLetters.length;bi2++) {
 				for(int bi3=0;bi3<chrArrLetters.length;bi3++) {
 					for(int bi4=0;bi4<chrArrLetters.length;bi4++) {
 						for(int bi5=0;bi5<chrArrLetters.length;bi5++) {
 							for(int bi6=0;bi6<chrArrLetters.length;bi6++) {
 								strArrSextagramLetters[intIndexCount] = new StringBuilder().append(chrArrLetters[bi1]).append(chrArrLetters[bi2]).append(chrArrLetters[bi3]).append(chrArrLetters[bi4]).append(chrArrLetters[bi5]).append(chrArrLetters[bi6]).toString();
 								intIndexCount++;
 							}
 						}
 					}
 				}
 			}
 		}
 		
 		return strArrSextagramLetters;
 	}
 		
 	private String[] makeCipherDigraph(String strCiphertext) {
 		
 		char[] chrArrCiphertext = strCiphertext.toCharArray();
 		String[] strArrDigraphCipher;
 		
 		strArrDigraphCipher = new String[strCiphertext.length()/2]; // length/2 because there are exactly n/2 digraphs for a text of n length
 		
 		// Make cipher digraph
 //		int intDigraphIndexCount = 0;
 		for(int index=0;index<(chrArrCiphertext.length-1);index++) {
 			if(index % 2 == 0) {
 				strArrDigraphCipher[index/2] = new StringBuilder().append(chrArrCiphertext[index]).append(chrArrCiphertext[index+1]).toString();
 //				intDigraphIndexCount++;
 			}
 		}
 		
 		return strArrDigraphCipher;
 	}
 	
 	private String[] makeCipherQuadrigraph(String strCiphertext) {
 		
 		char[] chrArrCiphertext = strCiphertext.toCharArray();
 		String[] strArrQuadrigraphCipher;
 		
 		strArrQuadrigraphCipher = new String[strCiphertext.length()/4]; // length/4 because there are exactly n/4 quadrigraphs for a text of n length
 		
 		// Make cipher quadrigraph
 		for(int index=0;index<(chrArrCiphertext.length-3);index++) {
 			if(index % 4 == 0) {
 				strArrQuadrigraphCipher[index/4] = new StringBuilder().append(chrArrCiphertext[index]).append(chrArrCiphertext[index+1]).append(chrArrCiphertext[index+2]).append(chrArrCiphertext[index+3]).toString();
 			}
 		}
 		
 		return strArrQuadrigraphCipher;
 	}
 	
 	private String[] makeCipherSextagraph(String strCiphertext) {
 		
 		char[] chrArrCiphertext = strCiphertext.toCharArray();
 		String[] strArrSextagraphCipher;
 		
 		strArrSextagraphCipher = new String[strCiphertext.length()/6]; // length/6 because there are exactly n/6 sextagraphs for a text of n length
 		
 		// Make cipher sextagraph
 		for(int index=0;index<(chrArrCiphertext.length-5);index++) {
 			if(index % 6 == 0) {
 				strArrSextagraphCipher[index/6] = new StringBuilder().append(chrArrCiphertext[index]).append(chrArrCiphertext[index+1]).append(chrArrCiphertext[index+2]).append(chrArrCiphertext[index+3]).append(chrArrCiphertext[index+4]).append(chrArrCiphertext[index+5]).toString();
 			}
 		}
 		
 		return strArrSextagraphCipher;
 	}
 	
 	private String[] makeCipherOctagraph(String strCiphertext) {
 		
 		char[] chrArrCiphertext = strCiphertext.toCharArray();
 		String[] strArrOctagraphCipher;
 		
 		strArrOctagraphCipher = new String[strCiphertext.length()/8]; // length/8 because there are exactly n/8 octagraphs for a text of n length
 		
 		// Make cipher octagraph
 		for(int index=0;index<(chrArrCiphertext.length-7);index++) {
 			if(index % 6 == 0) {
 				strArrOctagraphCipher[index/6] = new StringBuilder().append(chrArrCiphertext[index]).append(chrArrCiphertext[index+1]).append(chrArrCiphertext[index+2]).append(chrArrCiphertext[index+3]).append(chrArrCiphertext[index+4]).append(chrArrCiphertext[index+5]).append(chrArrCiphertext[index+6]).append(chrArrCiphertext[index+7]).toString();
 			}
 		}
 		
 		return strArrOctagraphCipher;
 	}
 	
 	static char[] chrArrLetters = new char[26];
 }
