 /*
 
 Riscrivere il thread Sommatore in modo che
 implementi l'interfaccia Runnable.
 
 */
 
 package esercitazione2;
 
 class Sommatore implements Runnable {
 	private int da;
 	private int a;
 	private int somma;
	private Thread t;
 
 	public Sommatore(int da, int a) {
 		this.da = da;
 		this.a = a;
		this.t = new Thread(this);
 	}
 	
	public int getSomma() throws InterruptedException {
		t.start();
		t.join();
		return somma;
	}
 
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
 		try {
 			System.out.println(s1.getSomma() + s2.getSomma());
 		} catch (InterruptedException e) {}
 	}
 }
