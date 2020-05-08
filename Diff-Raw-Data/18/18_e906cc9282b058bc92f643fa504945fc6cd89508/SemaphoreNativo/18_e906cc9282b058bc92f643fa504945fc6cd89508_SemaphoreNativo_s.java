 /*
 
 Scrivere una classe che, utilizzando i monitor nativi di Java,
 implementi un semaforo contatore che garantisca il risveglio in
 ordine LIFO dei thread sospesi, e che implementi la seguente interfaccia:
 
 public interface Semaphore {
 	public void acquire() throws InterruptedException;
 	public void acquire(int p) throws InterruptedException;
 	public void release();
 	public void release(int p);
 }
 
 */
 
 package esercitazione8;
 
 import java.util.Random;
 import java.util.Deque;
 import java.util.Queue;
 import java.util.LinkedList;
 
 public class SemaphoreNativo implements Semaphore {
 	
 	Deque<Request> stackAttesa = new LinkedList<Request>();
 	Queue<Thread> codaPronti = new LinkedList<Thread>();
 	private int permessi;
 
 	private static class Request {
 		Thread thread;
 		int permessi;
 		public Request(Thread t, int p) {
 			thread = t; permessi = p;
 		}
 	}
 
 	public SemaphoreNativo(int p) {
 		permessi = p;
 	}
 
 	public synchronized void acquire() throws InterruptedException {
 		acquire(1);
 	}
 
 	public synchronized void acquire(int p) throws InterruptedException {
 		System.out.println("Thread #" + Thread.currentThread().getId() + " ne richiede " + p);
 		if (!codaPronti.isEmpty() || permessi < p) {
 			stackAttesa.addFirst(new Request(Thread.currentThread(), p));
			while (codaPronti.isEmpty() || codaPronti.peek() != Thread.currentThread()) wait();
			codaPronti.poll();
 			if (codaPronti.isEmpty()) rilasciaPermessi();
		} else permessi -= p;
		System.out.println("Thread #" + Thread.currentThread().getId() + " ne ottiene " + p);
 	}
 
 	public synchronized void release() { release(1); }
 
 	public synchronized void release(int p) {
 		permessi += p;
		rilasciaPermessi();
 		System.out.println("Thread #" + Thread.currentThread().getId() + " ne rilascia " + p);
 	}
 
 	private synchronized void rilasciaPermessi() {
 		while (!stackAttesa.isEmpty() && permessi >= stackAttesa.peekFirst().permessi) {
 			permessi -= stackAttesa.peekFirst().permessi;
 			codaPronti.offer(stackAttesa.removeFirst().thread);
 		}
 		notifyAll();
 	}
 
 	public static void main(String[]args) {
 		ThreadUser[] t = new ThreadUser[20];
 		Semaphore s = new SemaphoreNativo(10);
 		for (int i = 0; i < t.length; i++) t[i] = new ThreadUser(s);
 		for (ThreadUser u: t) u.start();
 	}
 }
 
 // Classe di test
 class ThreadUser extends Thread {
 
 	private Semaphore s;
 	private Random r = new Random();
 
 	public static final int MIN_PERMESSI = 2;
 	public static final int MAX_PERMESSI = 5;
 
 	public ThreadUser(Semaphore s) { this.s = s; }
 	public void run() {
 		try {
 			while(true) {
 				if (condizioneCasuale() && condizioneCasuale()) {
 					if (condizioneCasuale()) s.release(permessoCasuale());
 					else s.release();
 				} else {
 					if (condizioneCasuale()) s.acquire(permessoCasuale());
 					else s.acquire();
 				}
 			}
 		} catch (InterruptedException e) {}
 	}
 	private boolean condizioneCasuale() {
 		return r.nextInt(2) == 0;
 	}
 
 	private int permessoCasuale() {
 		return r.nextInt(MAX_PERMESSI - MIN_PERMESSI + 1) + MIN_PERMESSI;
 	}
 }
