 package poo.banca;
 
 public class Banca {
 	private ContoBancario[] clientela; private int size;
 	public Banca(int n) {
 		if (n <= 0) throw new IllegalArgumentException();
 		clientela = new ContoBancario[n];
 	} // Costruttore
 	public void report() {
 		if (size == 0) System.out.println("Nessun cliente presente!");
 		for (int i = 0; i < size; i++)
 			System.out.println(clientela[i]);
 	} // report
 	public boolean addConto(ContoBancario cb) {
 		if (size == clientela.length) {
 			System.out.println("Limite clienti raggiunto!");
 			return false;
 		}
 		clientela[size++] = cb;
 		return true;
 	} // addConto
	public boolean removeConto(ContoBancario cb) {
 		for (int i = 0; i < size; i++)
			if (clientela[i].equals(cb)) {
 				clientela[i] = clientela[size-1]; // Trovato, sovrascrivo il suo riferimento con l'ultimo presente (garbage)
 				clientela[--size] = null; // Decremento size e tolgo il riferimento dell'ultimo conto, che è ora presente in posizione 'i'
 				return true;
 			}
 		System.out.println("Conto non presente!");
 		return false;
 	} // removeConto
 	public ContoBancario getConto(String numero) {
 		for (int i = 0; i < size; i++)
 			if (clientela[i].conto().equals(numero))
 				return clientela[i];
 		return null;
 	} // getConto
 	public void regalo() {
 		for (int i = 0; i < size; i++)
 			if (clientela[i] instanceof ContoConFido) {
 				double d = clientela[i].saldo() * 0.05;
 				if (d > 0) clientela[i].deposita(d);
 			} else { // ContoBancario
 				double d = clientela[i].saldo() * 0.01;
 				if (d > 0) clientela[i].deposita(d);
 			}
 	} // regalo
 } // Banca
