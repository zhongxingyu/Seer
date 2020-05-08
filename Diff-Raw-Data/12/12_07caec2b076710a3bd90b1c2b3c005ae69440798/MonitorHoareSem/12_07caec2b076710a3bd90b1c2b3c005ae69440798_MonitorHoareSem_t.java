 /*
 
 Scrivere una classe che, utilizzando i semafori,
 implementi un monitor di Hoare con un'unica condition,
 e che esponga i seguenti metodi:
 
 	void lock() throws InterruptedException
 	void unlock()
 	void await() throws InterruptedException
 	void signal()
 	void signalAll()
 
 */
 
 package esercitazione6;
 
 import java.util.concurrent.Semaphore;
 import java.util.concurrent.TimeUnit;
 import java.util.Random;
 import java.util.Stack;
 
 public class MonitorHoareSem {
 	
 	private Semaphore lock = new Semaphore(1);
 	private Semaphore mutex = new Semaphore(1); // Per proteggere le variabili threadCorrente, threadInAttesa e threadSegnalanti
 	private Semaphore condition = new Semaphore(0);
 	private Stack<Semaphore> segnalati = new Stack<Semaphore>();
 	private Stack<Semaphore> segnalanti = new Stack<Semaphore>();
 
 	private Thread threadCorrente = null;
 	private int threadInAttesa = 0, threadSegnalanti = 0;
 
 	public void lock() throws InterruptedException {
 		lock.acquire();
 		mutex.acquire();
 		threadCorrente = Thread.currentThread();
 		mutex.release();
 		System.out.println("Thread " + Thread.currentThread().getId() + " entra nel monitor");
 	}
 
 	public void unlock() {
 		try {
 			mutex.acquire();
 			if (threadCorrente != Thread.currentThread()) throw new IllegalMonitorStateException();
 			System.out.println("Thread " + Thread.currentThread().getId() + " lascia il monitor");
 			threadCorrente = null;
 			if (threadSegnalanti > 0) segnalanti.pop().release();
 			else {
 				threadCorrente = null;
 				lock.release();
 			}
 			mutex.release();
 		} catch(InterruptedException e) {}
 	}
 
 	public void await() throws InterruptedException {
 		mutex.acquire();
 		if (threadCorrente != Thread.currentThread()) throw new IllegalMonitorStateException();
 		threadInAttesa++;
 		if (threadSegnalanti > 0) segnalanti.pop().release();
 		else {
 			threadCorrente = null;
 			lock.release();
 		}
 		System.out.println("Thread " + Thread.currentThread().getId() + " attende nella condition");
 		mutex.release();
 		condition.acquire();
 		segnalati.peek().acquire();
 		mutex.acquire();
 		System.out.println("Thread " + Thread.currentThread().getId() + " esce dalla condition");
 		threadCorrente = Thread.currentThread();
 		mutex.release();
 	}
 
 	public void signal() {
 		try {
 			mutex.acquire();
 			if (threadCorrente != Thread.currentThread()) throw new IllegalMonitorStateException();
 			if (threadInAttesa > 0) {
 				condition.release();
				segnalati.push(new Semaphore(1));
 				threadInAttesa--;
 				threadCorrente = null;
 				threadSegnalanti++;
 				Semaphore s = new Semaphore(0);
 				segnalanti.push(s);
 				System.out.println("Thread " + Thread.currentThread().getId() + " esegue una signal");
 				mutex.release();
 				s.acquire();
 				mutex.acquire();
 				System.out.println("Thread " + Thread.currentThread().getId() + " ritorna dopo la signal");
 				threadCorrente = Thread.currentThread();
 				threadSegnalanti--;
 				segnalati.pop();
 			}
 			mutex.release();
 		} catch(InterruptedException e) {}
 	}
 
 	public void signalAll() {
 		try {
 			mutex.acquire();
 			if (threadCorrente != Thread.currentThread()) throw new IllegalMonitorStateException();
 			if (threadInAttesa > 0) {
 				condition.release(threadInAttesa);
 				int threadInAttesaAttuale = threadInAttesa;
 				threadInAttesa = 0;
 				segnalati.push(new Semaphore(0));
 				while (threadInAttesaAttuale > 0) {
 					segnalati.peek().release();
 					threadInAttesaAttuale--;
 					threadCorrente = null;
 					threadSegnalanti++;
 					Semaphore s = new Semaphore(0);
 					segnalanti.push(s);
 					System.out.println("Thread " + Thread.currentThread().getId() + " esegue una signal di signalAll");
 					mutex.release();
 					s.acquire();
 					mutex.acquire();
 					System.out.println("Thread " + Thread.currentThread().getId() + " ritorna dopo una signal di signalAll");
 					threadCorrente = Thread.currentThread();
 					threadSegnalanti--;
 				}
 				segnalati.pop();
 			}
 			mutex.release();
 		} catch(InterruptedException e) {}
 	}
 
 	public void test(int threads) {
 		ThreadUtente[] t = new ThreadUtente[threads];
 		for (int i = 0; i < t.length; i++)
 			t[i] = new ThreadUtente(this);
 		for (int i = 0; i < t.length; i++)
 			t[i].start();
 	}
 
 	public static void main(String[]args) {
		MonitorHoareSem m = new MonitorHoareSem();
 		m.test(10);
 	}
 }
 
 class ThreadUtente extends Thread {
 	
	private MonitorHoareSem m;
 	private Random r = new Random();
 
	public ThreadUtente(MonitorHoareSem m) {
 		this.m = m;
 	}
 
 	public void run() {
 		while (true) {
 			try {
 				m.lock();
 				System.out.println("Inizio operazioni thread " + Thread.currentThread().getId());
 				attesaCasuale();
 				while (condizioneCasuale()) m.await();
 				attesaCasuale();
 				if (condizioneCasuale() || condizioneCasuale()) m.signal();
 				else m.signalAll();
 				attesaCasuale();
 				System.out.println("Fine operazioni thread " + Thread.currentThread().getId());
 			} catch(InterruptedException e) {
 			} finally {
 				m.unlock();
 			}
 		}
 	}
 
 	private boolean condizioneCasuale() {
 		return r.nextInt(2) == 0;
 	}
 
 	private void attesaCasuale() throws InterruptedException {
 		TimeUnit.MILLISECONDS.sleep(r.nextInt(101));
 	}
 }
