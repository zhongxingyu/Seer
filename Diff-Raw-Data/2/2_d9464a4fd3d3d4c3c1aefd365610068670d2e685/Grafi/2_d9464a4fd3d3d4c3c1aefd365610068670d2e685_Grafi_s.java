 package grafi;
 
 import java.util.*;
 
 public class Grafi {
 
 	private Grafi() { }
 
 	public static boolean eConnesso(Grafo<? extends Arco> g) {
 		// Per grafi non orientati
 		// Complessità pari al costo di visita del grafo
 		List<Integer> l = g.depthFirstSearch(0);
 		return l.size() == g.getN();
 		/* Oppure:
 		return numComponentiConnesse(g) == 1;
 		*/
 	}
 
 	public static int numComponentiConnesse(Grafo<? extends Arco> g) {
 		// Per grafi non orientati
 		// Complessità pari al costo di visita del grafo
 		boolean[] visitati = new boolean[g.getN()];
 		for (int i = 0; i < g.getN(); i++) visitati[i] = false;
 		boolean grafoVisitato = false;
 		int nodoPartenza = 0, numComponenti = 0;
 		while (!grafoVisitato) {
 			List<Integer> l = g.depthFirstSearch(nodoPartenza); // oppure breadthFirstSearch
 			numComponenti++;
 			while (!l.isEmpty()) visitati[l.remove(0)] = true;
 			while (nodoPartenza < g.getN() && visitati[nodoPartenza]) nodoPartenza++;
 			if (nodoPartenza == g.getN()) grafoVisitato = true;
 		}
 		return numComponenti;
 	}
 
 	public static boolean connessoEAciclico(Grafo<? extends Arco> g) {
 		// Per grafi non orientati
 		// Complessità:
 		// 	Lista -> O(n)
 		// 	Matrice -> O(n^2)
 		return (g.getM() == g.getN() - 1) && eConnesso(g);
 	}
 
 	public static boolean eForesta(Grafo<? extends Arco> g) {
 		// Per grafi non orientati
 		// Complessità pari al costo di visita del grafo
 		return g.getM() == (g.getN() - numComponentiConnesse(g));
 	}
 
 	public static boolean eFortementeConnesso(Grafo<? extends Arco> g) {
 		// Per grafi orientati
 		// Complessità pari al costo di visita del grafo
 		return numComponentiFortementeConnesse(g) == 1;
 	}
 
 	public static int numComponentiFortementeConnesse(Grafo<Arco> g, Grafo<Arco> gc) { 
 		// Ipotesi: g.getN() == gc.getN() e gc non contiene archi
 		// Complessità pari al costo di chiusuraTransitiva
 		chiusuraTransitiva(g, gc);
 		int nComp = 0;
 		boolean[] visitati = new boolean[g.getN()];
 		for (int i = 0; i < g.getN(); i++)
 			if (!visitati[i]) {
 				nComp++;
 				visitati[i] = true;
 				Iterator<? extends Arco> itAd = gc.adiacenti(i);
 				while (itAd.hasNext()) {
 					int ad = itAd.next().getFin();
 					if (!visitati[ad] && gc.arco(ad, i)) visitati[ad] = true;
 				}
 			}
 		return nComp;
 	}
 
 	public static int numComponentiFortementeConnesse(Grafo<? extends Arco> g) {
 		// Per grafi orientati (Algoritmo di Tarjan)
 		// Complessità pari al costo di visita del grafo
 		int[] contatoreVisite = new int[1]; // Simula passaggio per riferimento
 		contatoreVisite[0] = 1;
 		int[] fuga = new int[g.getN()];
 		int[] indiceVisita = new int[g.getN()];
 		for (int i = 0; i < g.getN(); i++) fuga[i] = indiceVisita[i] = 0;
 		int numComponenti = 0;
 		Deque<Integer> pila = new LinkedList<Integer>();
 		for (int i = 0; i < g.getN(); i++)
 			if (indiceVisita[i] == 0)
 				numComponenti = visitaFortementeConnesse(g, i, numComponenti, contatoreVisite, fuga, indiceVisita, pila);
 		return numComponenti;
 	}
 
 	private static int visitaFortementeConnesse(Grafo<? extends Arco> g, int nodo, int numComponenti, int[] contatoreVisite, int[] fuga, int[] indiceVisita, Deque<Integer> pila) {
 		// Per grafi orientati
 		// Complessità pari al più al costo di visita del grafo
 		indiceVisita[nodo] = contatoreVisite[0]++;
 		fuga[nodo] = indiceVisita[nodo];
 		pila.addFirst(nodo);
 		Iterator<? extends Arco> itAd = g.adiacenti(nodo);
 		while (itAd.hasNext()) {
 			int ad = itAd.next().getFin();
 			if (indiceVisita[ad] == 0) {
 				numComponenti = visitaFortementeConnesse(g, ad, numComponenti, contatoreVisite, fuga, indiceVisita, pila);
 				if (fuga[ad] < fuga[nodo]) fuga[nodo] = fuga[ad];
 			} else if (pila.contains(ad)) {
 				if (indiceVisita[ad] < fuga[nodo]) fuga[nodo] = indiceVisita[ad];
 			}
 		}
 		if (fuga[nodo] == indiceVisita[nodo]) {
 			numComponenti++;
 			while (pila.removeFirst() != nodo) ;
 		}
 		return numComponenti;
 	}
 
 	public static boolean aciclico(Grafo<? extends Arco> g) {
 		// Per grafi orientati
 		// Complessità:
 		// 	Lista -> O(m)
 		// 	Matrice -> O(n^2)
 		int[] gradi = gradiDiEntrata(g);
 		boolean[] rimossi = new boolean[g.getN()];
 		LinkedList<Integer> daRimuovere = new LinkedList<Integer>();
 		for (int i = 0; i < gradi.length; i++)
 			if (gradi[i] == 0) daRimuovere.add(i);
 		while (!daRimuovere.isEmpty()) {
 			int nodo = daRimuovere.removeFirst();
 			rimossi[nodo] = true;
 			Iterator<? extends Arco> itAd = g.adiacenti(nodo);
 			while (itAd.hasNext()) {
 				int ad = itAd.next().getFin();
 				if (--gradi[ad] == 0 && !rimossi[ad]) daRimuovere.add(ad);
 			}
 		}
 		for (int i = 0; i < rimossi.length; i++)
 			if (!rimossi[i]) return false;
 		return true;
 	}
 
 	private static int[] gradiDiEntrata(Grafo<? extends Arco> g) {
 		// Complessità:
 		// 	Lista -> O(m)
 		// 	Matrice -> O(n^2)
 		int[] gradi = new int[g.getN()];
 		for (int i = 0; i < gradi.length; i++) gradi[i] = 0;
 		Iterator<? extends Arco> it = g.archi();
 		while (it.hasNext())
 			gradi[it.next().getFin()]++;
 		return gradi;
 	}
 
 	public static void chiusuraTransitiva(Grafo<Arco> g, Grafo<Arco> gc) {
 		// Per grafi orientati
 		// Ipotesi: g.getN() == gc.getN() e gc non contiene archi
 		// Complessità:
 		// 	Lista -> O(m * n)
 		// 	Matrice -> O(n^3)
 		List<Integer> visitati;
 		for (int i = 0; i < g.getN(); i++) {
 			visitati = g.depthFirstSearch(i); // oppure breadthFirstSearch
 			for (int v: visitati) gc.aggiungiArco(new Arco(i, v));
 		}
 	}
 
 	public static List<Double> prim(Grafo<ArcoPesato> g) {
 		// Complessità: O(n^2)
 		// Utilizzando un Heap di archi al posto dell'array distanze
 		// la complessità diventa O(m * log m) = O(m * log n)
 		// risparmiando le n ricerche del minimo a fronte di O(m) operazioni sull'Heap
 		Double[] distanze = new Double[g.getN()];
 		boolean[] raggiunti = new boolean[g.getN()];
 		for (int i = 0; i < g.getN(); i++) {
 			distanze[i] = Double.POSITIVE_INFINITY;
 			raggiunti[i] = false;
 		}
 		int[] padri = new int[g.getN()];
 		int nodoCorrente = 0;
 		distanze[nodoCorrente] = 0.0;
 		padri[nodoCorrente] = 0;
 		// Ciclo eseguito n volte
 		while (nodoCorrente != -1) {
 			raggiunti[nodoCorrente] = true;
 			Iterator<ArcoPesato> itAd = g.adiacenti(nodoCorrente);
 			// Costo iterazione adiacenti:
 			// 	Lista -> O(grado(nodoCorrente))
 			// 	Matrice -> O(n)
 			while (itAd.hasNext()) {
 				ArcoPesato a = itAd.next();
 				if (!raggiunti[a.getFin()]) {
 					double nuovaDist = a.getPeso();
 					if (nuovaDist < distanze[a.getFin()]) {
 						distanze[a.getFin()] = nuovaDist;
 						padri[a.getFin()] = nodoCorrente;
 					}
 				}
 			}
 			nodoCorrente = -1;
 			double minPeso = Double.POSITIVE_INFINITY;
 			// Ricerca del nodo più vicino al sottoalbero costruito: O(n)
 			for (int i = 0; i < g.getN(); i++)
 				if (!raggiunti[i] && distanze[i] < minPeso) {
 					nodoCorrente = i;
 					minPeso = distanze[i];
 				}
 		}
 		return Arrays.asList(distanze);
 	}
 
 	public static Grafo<ArcoPesato> kruskal(Grafo<ArcoPesato> g) {
 		// Complessità (nell'ipotesi che l'ordinamento sia eseguito con costo O(m * log n)):
 		// 	Lista -> O(m * n)
 		// 	Matrice -> O(m * n^2)
 		// Utilizzando una struttura dati Union-Find (di tipo Quickfind con bilanciamento sulle union)
		// al posto di eseguire una visita su "albero" è possibile verificare (con una find)
 		// se l'arco da inserire collega due alberi diversi oppure crea un ciclo, con costo O(1),
 		// e durante l'inserimento di un nuovo arco è sufficiente aggiornare la struttura
 		// con una union che ha complessità O(log n) ammortizzata sulle n-1 operazioni
 		// In questo caso la complessità sarebbe O(m * log n) [dovuta all'ordinamento]
 		ArcoPesato[] archi = generaArchiOrdinati(g);
 		int inseriti = 0;
 		GrafoLista<ArcoPesato> albero = new GrafoLista<ArcoPesato>(g.getN()); // Oppure GrafoMA<ArcoPesato>
 		// Ciclo eseguito al più m volte
 		for (int i = 0; (i < archi.length) && (inseriti < g.getN() - 1); i++) {
 			// Costo visita:
 			// 	Lista -> O(n) (Il numero di archi di "albero" è al più n-1)
 			// 	Matrice -> O(n^2)
 			List<Integer> lista = albero.depthFirstSearch(archi[i].getIn());
 			if (!lista.contains(archi[i].getFin())) {
 				albero.aggiungiArco(archi[i]);
 				inseriti++;
 			}
 		}
 		return albero;
 	}
 	
 	protected static ArcoPesato[] generaArchiOrdinati(Grafo<ArcoPesato> g) {
 		// Complessità ordinamento: O(m * log m) = O(m * log n)
 		// Con Quicksort O(m^2) nel caso peggiore
 		ArcoPesato[] archi = new ArcoPesato[g.getM()];
 		Iterator<ArcoPesato> it = g.archi();
 		for (int i = 0; it.hasNext(); i++) archi[i] = it.next();
 		quickSort(archi, 0, archi.length - 1);
 		return archi;
 	}
 
 	private static void quickSort(ArcoPesato[] a, int in, int fin) {
 		if (in >= fin) return;
 		int pivot = partizione(a, in, fin);
 		quickSort(a, in, pivot - 1);
 		quickSort(a, in, pivot + 1);
 	}
 
 	@SuppressWarnings("unchecked")
 	private static int partizione(ArcoPesato[] a, int in, int fin) {
 		int s = in + 1, d = fin;
 		while (s <= d) {
 			for (; s <= fin && a[s].getPeso() < a[fin].getPeso(); s++);
 			for (; a[d].getPeso() > a[in].getPeso(); d--);
 			if (s <= d) scambia(a, s, d);
 		}
 		scambia(a, in, s - 1);
 		return s - 1;
 	}
 
 	private static void scambia(ArcoPesato[] a, int i, int j) {
 		ArcoPesato tmp = a[i]; a[i] = a[j]; a[j] = tmp;
 	}
 
 	public static double[] dijkstra(Grafo<ArcoPesato> g, int nodoPartenza) {
 		// Ipotesi: g non contiene archi di peso negativo
 		// Complessità: O(n^2)
 		// Utilizzando un Heap di archi al posto dell'array distanze
 		// la complessità diventa O(m * log m) = O(m * log n)
 		// risparmiando le n ricerche del minimo a fronte di O(m) operazioni sull'Heap
 		double[] distanze = new double[g.getN()];
 		boolean[] raggiunti = new boolean[g.getN()];
 		for (int i = 0; i < g.getN(); i++) {
 			distanze[i] = Double.POSITIVE_INFINITY;
 			raggiunti[i] = false;
 		}
 		distanze[nodoPartenza] = 0.0;
 		int nodoCorrente = nodoPartenza;
 		// Ciclo eseguito n volte
 		while (nodoCorrente != -1) {
 			raggiunti[nodoCorrente] = true;
 			Iterator<ArcoPesato> itAd = g.adiacenti(nodoCorrente);
 			// Costo iterazione adiacenti:
 			// 	Lista -> O(grado(nodoCorrente))
 			// 	Matrice -> O(n)
 			while (itAd.hasNext()) {
 				ArcoPesato a = itAd.next();
 				if (!raggiunti[a.getFin()]) {
 					double nuovaDist = distanze[nodoCorrente] + a.getPeso();
 					if (nuovaDist < distanze[a.getFin()])
 						distanze[a.getFin()] = nuovaDist;
 				}
 			}
 			nodoCorrente = -1;
 			double minPeso = Double.POSITIVE_INFINITY;
 			// Ricerca del nodo più vicino al sottoalbero dei cammini minimi costruito: O(n)
 			for (int i = 0; i < g.getN(); i++)
 				if (!raggiunti[i] && distanze[i] < minPeso) {
 					nodoCorrente = i;
 					minPeso = distanze[i];
 				}
 		}
 		return distanze;
 	}
 
 	public static double[][] distanzeMinimeDijkstra(Grafo<ArcoPesato> g) {
 		// Ipotesi: g non contiene archi di peso negativo
 		// Complessità O(n^3)
 		double[][] distanze = distanzeIniziali(g);
 		for (int i = 0; i < g.getN(); i++)
 			distanze[i] = dijkstra(g, i);
 		return distanze;
 	}
 
 	public static double[][] distanzeMinimeFloydWarshall(Grafo<ArcoPesato> g) {
 		// Ipotesi: g non contiene cicli negativi (altrimenti non esiste un albero dei cammini minimi)
 		// Complessità O(n^3)
 		double[][] distanze = distanzeIniziali(g);
 		// Costruisco i cammini minimi k-vincolati sfruttando la tecnica della programmazione dinamica
 		for (int k = 0; k < g.getN(); k++)
 			for (int i = 0; i < g.getN(); i++)
 				for (int j = 0; j < g.getN(); j++)
 					if (distanze[i][j] > distanze[i][k] + distanze[k][j])
 						distanze[i][j] = distanze[i][k] + distanze[k][j];
 		return distanze;
 	}
 
 	private static double[][] distanzeIniziali(Grafo<ArcoPesato> g) {
 		// Complessità: O(n^2 + m) = O(n^2)
 		double[][] distanze = new double[g.getN()][g.getN()];
 		for (int i = 0; i < g.getN(); i++)
 			for (int j = 0; j < g.getN(); j++)
 				if (i == j) distanze[i][j] = 0;
 				else distanze[i][j] = Double.POSITIVE_INFINITY;
 		Iterator<ArcoPesato> it = g.archi();
 		while (it.hasNext()) {
 			ArcoPesato a = it.next();
 			distanze[a.getIn()][a.getFin()] = a.getPeso();
 		}
 		return distanze;
 	}
 
 	public static void chiusuraTransitivaFloyd(Grafo<Arco> g, Grafo<Arco> gc) {
 		// Complessità: O(n^3)
 		for (int i = 0; i < g.getN(); i++) gc.aggiungiArco(new Arco(i, i));
 		Iterator<Arco> it = g.archi();
 		while (it.hasNext()) gc.aggiungiArco(new Arco(it.next()));
 		for (int k = 0; k < g.getN(); k++)
 			for (int i = 0; i < g.getN(); i++)
 				for (int j = 0; j < g.getN(); j++)
 					if (gc.arco(i, k) && gc.arco(k, j))
 						gc.aggiungiArco(new Arco(i, j));
 	}
 
 	/*
 	
 	Dato un grafo g che rappresenta un insieme di operazioni
 	e un numero arbitrario di "esecutori", calcolare il tempo minimo
 	necessario ad eseguire tutte le operazioni rispettando le
 	relazioni di propedeuticità. Il tempo necessario ad eseguire ogni singola
 	operazione del grafo è memorizzato in un array passato come secondo parametro.
 
 	Il metodo tempoEsecuzione è ricavato dal metodo aciclico, con la differenza
 	che ad ogni passo vengono rimossi TUTTI i nodi con grado di entrata = 0,
 	non solo il primo, ottenendo un sottoinsieme di operazioni che possono
 	essere svolte simultaneamente.
 
 	*/
 	public static int tempoEsecuzione(Grafo<Arco> g, int[] executionTimes) {
 		int[] gradi = gradiDiEntrata(g);
 		boolean[] rimossi = new boolean[g.getN()];
 		int[] startTimes = new int[g.getN()];
 		LinkedList<Integer> daRimuovere = new LinkedList<Integer>();
 		for (int i = 0; i < startTimes.length; i++) startTimes[i] = 0;
 		for (int i = 0; i < gradi.length; i++)
 			if (gradi[i] == 0) daRimuovere.add(i);
 		while (!daRimuovere.isEmpty()) {
 			int curr = daRimuovere.removeFirst();
 			rimossi[curr] = true;
 			Iterator<Arco> it = g.adiacenti(curr);
 			while (it.hasNext()) {
 				int succ = it.next().getFin();
 				gradi[succ]--;
 				if (startTimes[succ] < startTimes[curr] + executionTimes[curr])
 					startTimes[succ] = startTimes[curr] + executionTimes[curr];
 				if (gradi[succ] == 0 && !rimossi[succ]) daRimuovere.add(succ);
 			}
 		}
 		for (int i = 0; i < rimossi.length; i++)
 			if (!rimossi[i]) return -1; // Grafo ciclico
 		int[] endTimes = new int[g.getN()];
 		for (int i = 0; i < endTimes.length; i++) endTimes[i] = startTimes[i] + executionTimes[i];
 		return max(endTimes);
 	}
 
 	private static int max(int[] v) {
 		int iMax = 0;
 		for (int i = 1; i < v.length; i++)
 			if (v[iMax] < v[i]) iMax = i;
 		return v[iMax];
 	}
 }
