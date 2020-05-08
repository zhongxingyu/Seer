 package alberi;
 
 public class AVL<T extends Comparable<? super T>> extends ABR<T> {
 
 	public AVL() { }
 
 	public void inserisci(T x) {
 		AlberoBin<T> padre = cercaNodo(x);
 		if (padre == null) {
 			// inserisco la radice
 			coll = new AlberoBinLFB<T>();
 			coll.setVal(x);
 		} else if (!padre.val().equals(x)) {
 			AlberoBin<T> figlio = new AlberoBinLFB<T>();
 			figlio.setVal(x);
 			if (x.compareTo(padre.val()) < 0) padre.setSin(figlio);
 			else padre.setDes(figlio);
 			int pos = figlio.pos(), var = 1;
 			aggiustaInserimento((AlberoBinLFB<T>)padre, pos);
 		}
 	}
 
 	protected void aggiustaInserimento(AlberoBinLFB<T> a, int pos) {
 		int var = 1;
 		while (a != null && var != 0) {
 			if (pos == 0) {
 				switch (a.bil()) {
 					case -1:
 						a.setBil(0);
 						var = 0;
 						break;
 					case 0:
 						a.setBil(1);
 						break;
 					case 1:
 						a.setBil(2);
 						a = ruota(a);
 						var = 0;
						break;
 				}
 			} else {
 				switch (a.bil()) {
 					case -1:
 						a.setBil(-2);
 						a = ruota(a);
 						var = 0;
 						break;
 					case 0:
 						a.setBil(-1);
 						break;
 					case 1:
 						a.setBil(0);
 						var = 0;
						break;
 				}
 			}
 			pos = a.pos();
 			// Aggiorno la radice nel caso fosse stata ruotata
 			if (a.padre() == null) coll = a;
 			a = (AlberoBinLFB<T>)a.padre();
 		}
 	}
 
 	public void rimuovi(T x) {
 		AlberoBin<T> curr = cercaNodo(x);
 		if (curr == null || !curr.val().equals(x)) return;
 		AlberoBin<T> padre; int pos;
 		if (curr.sin() == null || curr.des() == null) {
 			pos = curr.pos();
 			padre = curr.padreBin();
 			rimuoviNodo(curr);
 		} else {
 			AlberoBin<T> curr2 = max(curr.sin());
 			curr.setVal(curr2.val());
 			pos = curr2.pos();
 			padre = curr2.padreBin();
 			rimuoviNodo(curr2);
 		}
 		aggiustaRimozione((AlberoBinLFB<T>)padre, pos);
 	}
 
 	protected void aggiustaRimozione(AlberoBinLFB<T> a, int pos) {
 		int var = -1;
 		while (a != null && var != 0) {
 			if (pos == 0) {
 				switch (a.bil()) {
 					case -1:
 						a.setBil(-2);
 						a = ruota(a);
 						break;
 					case 0:
 						a.setBil(-1);
 						var = 0;
 						break;
 					case 1:
 						a.setBil(0);
 				}
 			} else {
 				switch (a.bil()) {
 					case -1:
 						a.setBil(0);
 						break;
 					case 0:
 						a.setBil(1);
 						var = 0;
 						break;
 					case 1:
 						a.setBil(2);
 						a = ruota(a);
 				}
 			}
 			pos = a.pos();
 			// Aggiorno la radice nel caso fosse stata ruotata
 			if (a.padre() == null) coll = a;
 			a = (AlberoBinLFB<T>)a.padre();
 		}
 	}
 
 	protected AlberoBinLFB<T> ruota(AlberoBinLFB<T> a) {
 		AlberoBinLFB<T> padre, b, c, res;
 		padre = (AlberoBinLFB<T>)a.padre();
 		int pos = a.pos();
 		a.pota();
 		if (a.bil() == 2) {
 			b = (AlberoBinLFB<T>)a.sin();
 			b.pota();
 			if (b.bil() == -1) {
 				// Rotazione sinistra-destra
 				c = (AlberoBinLFB<T>)b.des();
 				c.pota();
 				a.setSin(c.des());
 				b.setDes(c.sin());
 				c.setSin(b);
 				c.setDes(a);
 				switch (c.bil()) {
 					case -1:
 						a.setBil(0);
 						b.setBil(1);
 						break;
 					case 0:
 						a.setBil(0);
 						b.setBil(0);
 						break;
 					case 1:
 						a.setBil(-1);
 						b.setBil(0);
 				}
 				c.setBil(0);
 				res = c;
 			} else {
 				// Rotazione sinistra-sinistra
 				a.setSin(b.des());
 				b.setDes(a);
 				if (b.bil() == 0) {
 					a.setBil(1);
 					b.setBil(-1);
 				} else { // b.bil() = 1
 					a.setBil(0);
 					b.setBil(0);
 				}
 				res = b;
 			}
 		} else { // a.bil() = -2
 			b = (AlberoBinLFB<T>)a.des();
 			b.pota();
 			if (b.bil() == 1) {
 				// Rotazione destra-sinistra
 				c = (AlberoBinLFB<T>)b.sin();
 				c.pota();
 				a.setDes(c.sin());
 				b.setSin(c.des());
 				c.setSin(a);
 				c.setDes(b);	
 				switch (c.bil()) {
 					case -1:
 						a.setBil(1);
 						b.setBil(0);
 						break;
 					case 0:
 						a.setBil(0);
 						b.setBil(0);
 						break;
 					case 1:
 						a.setBil(0);
 						b.setBil(-1);
 				}
 				c.setBil(0);
 				res = c;
 			} else {
 				// Rotazione destra-destra
 				a.setDes(b.sin());
 				b.setSin(a);
 				if (b.bil() == 0) {
 					a.setBil(-1);
 					b.setBil(1);
 				} else { // b.bil() = -1
 					a.setBil(0);
 					b.setBil(0);
 				}
 				res = b;
 			}
 		}
 		if (padre != null) padre.setFiglio(res, pos);
 		return res;
 	}
 }
