 package it.d0ge01.exercises.ex2309;
 
 import it.d0ge01.exercises.util.*;
 
 import java.io.IOException;
 
 public class Ex2309 extends ExTemplate {
 	public Ex2309() throws IOException {
 		super(testo);
 		// TODO Auto-generated constructor stub
 	}
 
 	public static String[] testo = {"Media 3 Vettori reali","Controlla '*' in un vettore di caratteri",
 		"Controlla elementi pari in un vettore integer","Conta elementi diversi v1, v2 integer",
 		"Conta le cifre di un naturale :D","Calcola radice in un numero reale"};
 	private static int sizeV = 3;
 	
 	public void esercizio1() throws IOException {
 		double[] v1 = new double[sizeV];
 		double[] v2 = new double[sizeV];
 		double[] v3 = new double[sizeV];
 		
 		Util.loadArrayDouble("Inserisci l'elemento reale (1 v ): ", v1);
 		Util.loadArrayDouble("Inserisci l'elemento reale (2 v ): ", v2);
 		Util.loadArrayDouble("Inserisci l'elemento reale (3 v ): ", v3);
 		
 		System.out.println("La somma del primo vettore e' " + sumAllDouble(v1));
 		System.out.println("La somma del secondo vettore e' " + sumAllDouble(v2));
 		System.out.println("La somma del terzo vettore e' " + sumAllDouble(v3));
 		
 		
 	}
 	
 	private double sumAllDouble(double[] v) {
 		// TODO Auto-generated method stub
 		double s = 0;
 		for ( int i = 0 ; i < v.length ; i++ )
 			s += v[i];
 		return s;
 	}
 
 	public void esercizio2() throws IOException {
 		char[] v = new char[sizeV];
 		boolean c= false;
 		Util.loadArrayChar("Inserisci un carattere: ", v);
 
 		for ( int i = 0 ; i < v.length ; i++ )
 			if ( v[i] == '*')
 				c = true;
 		System.out.println(c);
 	}
 	
 	public void esercizio3() throws IOException {
 		int[] v = new int[sizeV];
 		boolean c = true;
 		int i = 0;
 		
 		
 		Util.loadArrayInt("Inserisci un intero: ", v);
 		while ( c && i < sizeV ) {
 			if ( v[i] % 2 != 0 )
 				c = false;
 			i++;
 		}
 		System.out.println(c);
 	}
 	
 	public void esercizio4() throws IOException {
 		int[] v1 = new int[sizeV];
 		int[] v2 = new int[sizeV];
 		int n = 0;
 		
 		Util.loadArrayInt("Inserisci un intero (1v): ", v1);
 		Util.loadArrayInt("Inserisci un intero (2v): ", v2);
 		
 		for ( int i = 0 ; i < sizeV ; i++ )
 			if ( !Util.checkIntIndex(v2, v1[i]) )
 				n += 1;
 		
 		System.out.println(n);
 	}
 	
 	public void esercizio5() throws IOException {
 		System.out.print("Inserisci un reale: ");
 		double n = Util.inputDouble();
 		System.out.println(contaCifre(n));
 	}
 	
 	private static int contaCifre(double n) {
 		// TODO Auto-generated method stub
 		if ( n <= 10 )
 			return 1;
 		else
 			return ( contaCifre(n / 10) + 1 );
 	}
 
 	public void esercizio6() throws IOException {
 		double n1, n2;
 		n1 = 0.0;
 		System.out.println("Inserisci un quadrato: ");
 		double n = Util.inputDouble();
 		n2 = n;
 		System.out.println("Inserisci l'errore: ");
 		double err = Util.inputDouble();
 		while ( ( n2 - n1 ) > err ) {
 			if ( Math.pow(n1, 2)  < n )
 				n1 += err;
 			if ( Math.pow(n2, 2) > n )
 				n2 -= err;
 		}
		System.out.println("La tua radice si trova tra " + n1 + " e "  + n2 + " :D");
 	}
 }
