 package esercitazione10;
 
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 import java.util.concurrent.locks.Condition;
 
 public class TavoloLC extends Tavolo {
 
 	private Lock l = new ReentrantLock();
 	private Condition tabaccaio = l.newCondition();
 	private Condition[] fumatori = new Condition[NUM_INGREDIENTI];
 	private boolean ciSonoIngredienti = false;
 
 	public TavoloLC() {
 		for (int i = 0; i < fumatori.length; i++)
 			fumatori[i] = l.newCondition();
 	}
 
 	public void metti(int[] ingr) throws InterruptedException {
 		l.lock();
 		try {
 			while (ciSonoIngredienti) tabaccaio.await();
 			for (int i = 0; i < ingr.length; i++)
 				ingredienti[i] = ingr[i];
 			ciSonoIngredienti = true;
 			int qualeFumatore = (ingredienti[ingredienti.length - 1] + 1) % NUM_INGREDIENTI;
 			fumatori[qualeFumatore].signal();
 		} finally { l.unlock(); }
 	}
 
 	public void prendi(int ingrediente) throws InterruptedException {
 		l.lock();
 		try {
 			while (!ingredientiGiusti(ingrediente)) fumatori[ingrediente].await();
 			for (int i = 0; i < ingredienti.length; i++)
 				ingredienti[i] = -1;
 			ciSonoIngredienti = false;
 			tabaccaio.signal();
 		} finally { l.unlock(); }
 	}
 
 	private boolean ingredientiGiusti(int i) {
 		return ciSonoIngredienti &&
			((ingredienti[ingredienti.length - 1] + 1) % NUM_INGREDIENTI == i);
 	}
 }
