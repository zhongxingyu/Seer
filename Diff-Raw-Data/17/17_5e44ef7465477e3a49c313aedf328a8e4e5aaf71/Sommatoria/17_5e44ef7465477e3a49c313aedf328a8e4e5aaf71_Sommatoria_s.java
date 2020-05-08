 /*
 
 Riscrivere il thread Sommatore in modo che
 implementi l'interfaccia Runnable.
 
 */
 
 package esercitazione2;
 
 class Sommatore implements Runnable {
 	private int da;
 	private int a;
 	private int somma;
 
 	public Sommatore(int da, int a) {
 		this.da = da;
 		this.a = a;
 	}
 	
	public int getSomma() { return somma; }
 
 	public void run() {
 		somma = 0;
 		for (int i = da; i <= a; i++)
 			somma += i;
 	}
 }
 
 public class Sommatoria {
 	public static void main(String[] args) {
 		int primo = 1;
 		int ultimo = 100;
 		int intermedio = (ultimo + primo) / 2;
 		Sommatore s1 = new Sommatore(primo, intermedio);
 		Sommatore s2 = new Sommatore(intermedio + 1, ultimo);
		Thread t1 = new Thread(s1);
		Thread t2 = new Thread(s2);
		t1.start();
		t2.start();
 		try {
			t1.join(); t2.join();
 			System.out.println(s1.getSomma() + s2.getSomma());
 		} catch (InterruptedException e) {}
 	}
 }
