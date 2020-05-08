 package esercitazione10;
 
 public class TavoloSync extends Tavolo {
 	
 	private boolean ciSonoIngredienti = false;
 
 	public synchronized void metti(int[] ingr) throws InterruptedException {
 		while (ciSonoIngredienti) wait();
 		for (int i = 0; i < ingr.length; i++)
 			ingredienti[i] = ingr[i];
 		ciSonoIngredienti = true;
 		int qualeFumatore = (ingredienti[ingredienti.length - 1] + 1) % NUM_INGREDIENTI;
 		notifyAll();
 	}
 
 	public synchronized void prendi(int ingrediente) throws InterruptedException {
 		while (!ingredientiGiusti(ingrediente)) wait();
 		for (int i = 0; i < ingredienti.length; i++)
 			ingredienti[i] = -1;
 		ciSonoIngredienti = false;
 		notifyAll();
 	}
 
 	private boolean ingredientiGiusti(int i) {
 		return ciSonoIngredienti &&
			((ingredienti[ingredienti.length - 1] + 1) % NUM_INGREDIENTI == i);
 	}
 }
