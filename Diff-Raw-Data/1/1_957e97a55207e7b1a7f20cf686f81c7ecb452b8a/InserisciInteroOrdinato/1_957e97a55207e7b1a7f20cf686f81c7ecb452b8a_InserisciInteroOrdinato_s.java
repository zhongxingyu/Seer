 package poo.file;
 
 import java.io.*;
 import java.util.*;
 
 public class InserisciInteroOrdinato {
 	public static void main(String[]args) {
 		Scanner sc = new Scanner(System.in);
 		System.out.print("Nome file di interi ordinato in cui inserire: ");
 		String nomeFile = sc.nextLine();
 		System.out.print("Numero intero da inserire: ");
 		int x = sc.nextInt();
 		try {
 			inserisci(nomeFile, x);
 		} catch (Exception e) {
 			System.out.println("Errore in lettura/scrittura!");
 		}
 	} // main
 	public static void inserisci(String nome, int x) throws IOException {
 		RandomAccessFile raf = new RandomAccessFile(nome, "r");
 		DataOutputStream dos = new DataOutputStream(new FileOutputStream("tmp"));
 		long pos = 0; int y = 0; boolean inserito = false;
 		while (pos < raf.length()) {
 			y = raf.readInt();
 			if (y > x && !inserito) { dos.writeInt(x); inserito = true; }
 			dos.writeInt(y);
 			pos = raf.getFilePointer();
 		}
 		raf.close(); dos.close();
 		BufferedInputStream fin = new BufferedInputStream(new FileInputStream("tmp"));
 		BufferedOutputStream fout = new BufferedOutputStream(new FileOutputStream(nome));
 		int dato = 0;
 		for (;;) {
 			dato = fin.read();
 			if (dato == -1) break;
 			fout.write(dato);
 		}
 		fin.close(); fout.close();
 	} // inserisci
 } // InserisciInteroOrdinato
