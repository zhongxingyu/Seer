 package poo.file;
 
 import java.io.*;
 import java.util.*;
 
 public class NaturalMergeSort {
 	public static void main(String[]args) {
 		Scanner sc = new Scanner(System.in);
 		System.out.print("Nome file di interi da ordinare: ");
 		String nomeFile = sc.nextLine();
 		while (!(new File(nomeFile)).exists()) {
 			System.out.print(nomeFile + " non esiste! Ridare nome file: ");
 			nomeFile = sc.nextLine();
 		}
 		risolvi(nomeFile);
 	} // main
 	static void risolvi(String nomeFile) {
 		ObjectFile<Integer> f = null, tmp1 = null, tmp2 = null;
 		try {
 			f = new ObjectFile<Integer>(nomeFile, ObjectFile.Modo.LETTURA);
 			tmp1 = new ObjectFile<Integer>("tmp1", ObjectFile.Modo.SCRITTURA);
 			tmp2 = new ObjectFile<Integer>("tmp2", ObjectFile.Modo.SCRITTURA);
 			int n = 0, x = 0, y = 0;
 			while ((n = numeroSegmenti(f)) > 1) {
 				f.close(); f = new ObjectFile<Integer>(nomeFile, ObjectFile.Modo.LETTURA);
 				for (int i = 0; i < n; i++)
 					copiaSegmento(f, (i % 2 == 0 ? tmp1 : tmp2));
 				f.close(); tmp1.close(); tmp2.close();
 				f = new ObjectFile<Integer>(nomeFile, ObjectFile.Modo.SCRITTURA);
 				tmp1 = new ObjectFile<Integer>("tmp1", ObjectFile.Modo.LETTURA);
 				tmp2 = new ObjectFile<Integer>("tmp2", ObjectFile.Modo.LETTURA);
 				for (int i = 0; i < n / 2 + 1; i++)
 					fondiSegmenti(f, tmp1, tmp2);
 				f.close(); tmp1.close(); tmp2.close();
 				f = new ObjectFile<Integer>(nomeFile, ObjectFile.Modo.LETTURA);
 				tmp1 = new ObjectFile<Integer>("tmp1", ObjectFile.Modo.SCRITTURA);
 				tmp2 = new ObjectFile<Integer>("tmp2", ObjectFile.Modo.SCRITTURA);
 			}
 			(new File("tmp1")).delete();
 			(new File("tmp2")).delete();
 		} catch (Exception e) {
 			System.out.println("Errore di lettura/scrittura!");
 			e.printStackTrace();
 		} finally {
 			try {
 				if (f != null) f.close();
 				if (tmp1 != null) tmp1.close();
 				if (tmp2 != null) tmp2.close();
 			} catch (IOException e) {
 				System.out.println("Errore di lettura/scrittura!");
 			}
 		}
 	} // risolvi
 	private static int numeroSegmenti(ObjectFile<Integer> f) throws IOException {
 		if (f.eof()) return 0;
 		int n = 1, prev = f.peek(), next = 0;
 		f.get();
 		while (!f.eof()) {
 			next = f.peek();
 			if (next < prev) n++;
 			prev = next;
 			f.get();
 		}
 		return n;
 	} // numeroSegmenti
 	private static void copiaSegmento(ObjectFile<Integer> f, ObjectFile<Integer> tmp) throws IOException {
 		int prev = f.peek(), next = 0;
 		tmp.put(prev); f.get();
 		while (!f.eof()) {
 			next = f.peek();
 			if (prev <= next) { tmp.put(next); prev = next; }
 			else break;
 			f.get();
 		}
 	} // copiaSegmento
 	private static void fondiSegmenti(ObjectFile<Integer> f, ObjectFile<Integer> tmp1, ObjectFile<Integer> tmp2) throws IOException {
 		int x1 = 0, x2 = 0;
 		if (!tmp1.eof() && !tmp2.eof()) {
 			for (;;) {
 				x1 = tmp1.peek(); x2 = tmp2.peek();
 				if (x1 < x2) {
 					f.put(x1);
 					tmp1.get();
 					if (tmp1.eof() || tmp1.peek() < x1) break;
 				} else {
 					f.put(x2);
 					tmp2.get();
 					if (tmp2.eof() || tmp2.peek() < x2) break;
 				}
 			}
 		}
 		else if (!tmp1.eof()) x1 = tmp1.peek();
 		else if (!tmp2.eof()) x2 = tmp2.peek();
 		while (!tmp1.eof() && tmp1.peek() >= x1) { f.put(x1 = tmp1.peek()); tmp1.get(); }
 		while (!tmp2.eof() && tmp2.peek() >= x2) { f.put(x2 = tmp2.peek()); tmp2.get(); }
 	} // fondiSegmenti
 } // NaturalMergeSort
