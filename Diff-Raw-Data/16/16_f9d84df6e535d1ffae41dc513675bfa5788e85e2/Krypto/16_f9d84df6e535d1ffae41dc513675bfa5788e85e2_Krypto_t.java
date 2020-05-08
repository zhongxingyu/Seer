 package itsp;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class Krypto {
 
 	public static void main(String[] args) {
 
 		// System.out.println(caesar("Hallo du da", 4, true));
 		System.out.println(caesar("wie geht es dir zzwzz a", 4, true));
 		System.out.println(caesar(" midkilxdiwdhmvdcc ccde", 4, false));
 
 		System.out.println(skytale("hallo wie geht es dir", 4, true));
 		System.out.println(skytale("hwhdaitile rl e ogs  e  ", 4, false));
 
 		analyse("Hallo wie geht es dir eigentlich so");
 	}
 
 	public static String caesar(String text, int key, boolean encrypt) {
 		char[] eingabe = text.toCharArray();
 		char[] ausgabe = new char[eingabe.length];
 
 		if (encrypt) {
 			for (int i = 0; i < eingabe.length; i++) {
				ausgabe[i] = (char) ((eingabe[i] + key) % 256);
 			}
 		} else {
 			for (int i = 0; i < eingabe.length; i++) {
                ausgabe[i] = (char) ((256 + eingabe[i] - key) % 256);
 			}
 		}
 
 		return new String(ausgabe);
 	}
 
 	public static String skytale(String text, int key, boolean encrypt) {
 		int streifen_lange = 6;
 		char[] eingabe = text.toCharArray();
 		char[][] zwischen = new char[key][streifen_lange];
 
 		for (int i = 0; i < zwischen.length; i++) {
 			for (int j = 0; j < zwischen[0].length; j++) {
 				zwischen[i][j] = ' ';
 			}
 		}
 
 		StringBuffer ausgabe = new StringBuffer();
 		if (encrypt) {
 			int k = 0;
 			int l = 0;
 			for (int i = 0; i < eingabe.length; i++) {
 				if (l == streifen_lange) {
 					l = 0;
 					k++;
 				}
 				zwischen[k][l] = eingabe[i];
 				l++;
 			}
 
 			for (int i = 0; i < zwischen.length; i++) {
 				for (int j = 0; j < zwischen[0].length; j++) {
 					System.out.print(zwischen[i][j]);
 				}
 				System.out.println();
 			}
 
 			for (int l1 = 0; l1 < zwischen[0].length; l1++) {
 				for (int k1 = 0; k1 < zwischen.length; k1++) {
 					ausgabe.append(zwischen[k1][l1]);
 				}
 			}
 		} else {
 			int k = 0;
 			int l = 0;
 			for (int i = 0; i < eingabe.length; i++) {
 				if (k == key) {
 					k = 0;
 					l++;
 				}
 				zwischen[k][l] = eingabe[i];
 				k++;
 			}
 
 			for (int i = 0; i < zwischen.length; i++) {
 				for (int j = 0; j < zwischen[0].length; j++) {
 					System.out.print(zwischen[i][j]);
 				}
 				System.out.println();
 			}
 
 			for (int k1 = 0; k1 < zwischen.length; k1++) {
 				for (int l1 = 0; l1 < zwischen[0].length; l1++) {
 					ausgabe.append(zwischen[k1][l1]);
 				}
 			}
 		}
 
 		return "" + ausgabe;
 	}
 	
 	public static void analyse(String text){
 		char[] eingabe = text.toCharArray();
 		Map<String, Integer> haufigkeit = new HashMap<String, Integer>();
 		
 		for(int i=0;i<eingabe.length;i++){
 			if(haufigkeit.containsKey(""+eingabe[i])){
 				System.out.println("Yes:"+eingabe[i]);
 				haufigkeit.put(""+eingabe[i], haufigkeit.get(eingabe[i])+1);
 			} else {
 				System.out.println("No:"+eingabe[i]);
 				haufigkeit.put(""+eingabe[i], 1);
 			}
 		}
 		
 		for(String s : haufigkeit.keySet()){
 			System.out.println(s+" -> "+ haufigkeit.get(s));
 		}
 	}
 }
