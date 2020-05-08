 package poo.data;
 
 import java.util.*;
 
 public class Data {
 	private int g, m, a;
 	public enum Elemento {GIORNO, MESE, ANNO};
 	public Data() {
 		GregorianCalendar gc = new GregorianCalendar();
 		this.g = gc.get(gc.DAY_OF_MONTH);
 		this.m = gc.get(gc.MONTH)+1;
 		this.a = gc.get(gc.YEAR);
 	} // Costruttore di default: data corrente
 	public Data(int g, int m, int a) {
 		if (	g < 1 || g > durataMese(m, a) ||
 			m < 1 || m > 12 || a < 0	)
 			throw new IllegalArgumentException();
 		this.g = g; this.m = m; this.a = a;
 	} // Costruttore normale
 	public Data(Data d) {
 		g = d.g; m = d.m; a = d.a;
 	} // Costruttore di copia
 	public int get(Elemento val) {
 		switch(val) {
 			case GIORNO: return g;
 			case MESE: return m;
 			default: return a;
 		}
 	} // get
 	public static int durataMese(int m, int a) {
 		if (m < 1 || m > 12 || a < 0) throw new IllegalArgumentException();
 		switch(m) {
 			case 4: case 6: case 9: case 11: return 30;
 			case 2:
 				if (bisestile(a)) return 29;
 				else return 28;
 			default: return 31;
 		}
 	} // durataMese
 	private static int durataAnno(int a) {
 		return (bisestile(a) ? 366 : 365);
 	} // durataAnno
 	public static boolean bisestile(int a) {
 		if (a < 0) throw new IllegalArgumentException();
 		return (a % 4 == 0 && (a % 100 != 0 || a % 400 == 0));
 	} // bisestile
 	public Data giornoDopo() {
 		int gg = g + 1, mm = m, aa = a;
 		if (g == durataMese(m, a)) {
 			gg = 1;
 			if (m == 12) { mm = 1; aa++; }
 			else mm++;
 		}
 		return new Data(gg, mm, aa);
 	} // giornoDopo
 	public Data giornoPrima() {
 		int gg = g - 1, mm = m, aa = a;
 		if (g == 1) {
 			if (m == 1) {
 				if (a == 0) return null;
 				mm = 12; aa--;
			} else m--;
 			gg = durataMese(mm, aa);
 		}
 		return new Data(gg, mm, aa);
 	} // giornoPrima
 	public int distanza(Data d) {
 		Data d1, d2; int a2 = d.get(Elemento.ANNO), m2 = d.get(Elemento.MESE), g2 = d.get(Elemento.GIORNO);
 		if (a < a2 || (a == a2 && m < m2) || (a == a2 && m == m2 && g < g2)) return distanzaGiorni(this, d);
 		else if (a == a2 && m == m2 && g == g2) return 0;
 		else return -distanzaGiorni(d, this);
 	} // distanza
 	private static int distanzaGiorni(Data d1, Data d2) {
 		// Presupposto: d1 precede d2
 		int giorni = 0, a1 = d1.get(Elemento.ANNO), a2 = d2.get(Elemento.ANNO), deltaAnni = a2 - a1,
 		    m1 = d1.get(Elemento.MESE), m2 = d2.get(Elemento.MESE), g1 = d1.get(Elemento.GIORNO), g2 = d2.get(Elemento.GIORNO);
 		if (deltaAnni > 0) {
 			for (int anno = a1 + 1; anno < a2; anno++)
 				giorni += durataAnno(anno); // Sommo i giorni relativi agli anni compresi tra d1 e d2 (esclusi)
 			for (int mese = m1 + 1; mese <= 12; mese++)
 				giorni += durataMese(mese, a1); // Aggiungo i mesi compresi tra il mese di d1 e l'ultimo mese dell'anno di d1
 			giorni += durataMese(m1, a1) - g1; // Aggiungo i giorni compresi tra il giorno di d1 e l'ultimo giorno del mese di d1
 			for (int mese = m2 - 1; mese > 0; mese--)
 				giorni += durataMese(mese, a2); // Aggiungo i mesi compresi tra l'inizio dell'anno di d2 e il suo mese (escluso)
 			giorni += g2; // Aggiungo i giorni del mese di d2 (quello escluso prima)
 		} else { // deltaAnni = 0
 			int deltaMesi = m2 - m1;
 			if (deltaMesi > 0) {
 				for (int mese = m1 + 1; mese < m2; mese++)
 					giorni += durataMese(mese, a1); // Sommo i giorni dei mesi compresi tra il mese di d1 e quello di d2 (esclusi)
 				giorni += durataMese(m1, a1) - g1; // Aggiungo i giorni compresi tra il giorno di d1 e l'ultimo giorno del mese di d1
 				giorni += g2; // Aggiungo i giorni di d2
 			} else { // deltaMesi = 0
 				giorni += g2 - g1;
 			}
 		}
 		return giorni;
 	} // distanzaGiorni
 	public String toString() {
 		return String.format("%02d/%02d/%04d", g, m, a);
 	} // toString
 } // Data
