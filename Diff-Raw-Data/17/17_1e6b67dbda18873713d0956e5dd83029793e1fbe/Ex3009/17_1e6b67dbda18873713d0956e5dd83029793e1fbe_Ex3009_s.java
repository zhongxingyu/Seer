 package it.d0ge01.exercises.ex3009;
 
 import java.io.IOException;
 
 import it.d0ge01.exercises.util.*;
 
 public class Ex3009 extends ExTemplate {
 
 	public Ex3009() throws IOException {
 		super(testo);
 		// TODO Auto-generated constructor stub
 	}
 
 	public static String[] testo = {"Dati 3 vettori di caratteri, quali hanno solo cifre?","prodotto vettore di reali",
 		"scrivi numero da vettore di interi","Conta elementi distinti in un vettore",
 		"Verifica se in un vettore di interi c' uno 0","ricerca binaria su vettore di caratteri :D"};
 	private static int sizeV = 3;
 	
 	public void esercizio1() throws IOException {
 		char[] v1 = new char[sizeV];
 		char[] v2 = new char[sizeV];
 		char[] v3 = new char[sizeV];
 		
 		Util.loadArrayChar("Inserisci un carattere (1v)", v1);
 		Util.loadArrayChar("Inserisci un carattere (2v)", v2);
 		Util.loadArrayChar("Inserisci un carattere (3v)", v3);
 		
 		System.out.println("Il vettore uno: " + checkOnlyN(v1));
 		System.out.println("Il vettore due: " + checkOnlyN(v2));
 		System.out.println("Il vettore tre: " + checkOnlyN(v3));
 	}
 	
 	private boolean checkOnlyN(char[] v) {
 		boolean found = true;
 		
 		for ( int i = 0 ; i < v.length && found; i++ )
 			if ( ( v[i] < '0') || ( v[i] > '9'))
 				found = false;
 		
 		return found;
 	}
 	
 	public void esercizio2() throws IOException {
 		double[] v = new double[sizeV];
 		Util.loadArrayDouble("Inserisci un reale: ", v);
 		double p = 1.0;
 		
 		
 		for ( int i = 0 ; i < v.length ; i++ )
 			p *= v[i];
 		
 		System.out.println("Il prodotto e' : " + p);
 	}
 
 	public void esercizio3() throws IOException {
 		int[] v = new int[sizeV];
		
 		Util.loadArrayInt("Inserisci un naturale tra 0 e 9: ", v);
 		
 		for ( int i = 0 ; i < v.length ; i++ )
 			if ( ( v[i] >= 0 ) && (v[i] <= 9))
				System.out.print(v[i]);
		System.out.println("");
 	}
 }
