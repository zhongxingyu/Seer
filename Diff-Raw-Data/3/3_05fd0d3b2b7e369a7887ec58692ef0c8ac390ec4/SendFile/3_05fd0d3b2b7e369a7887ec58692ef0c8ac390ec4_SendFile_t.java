 // Time-stamp: <13 Oct 2009 at 13:29:48 by charpov on glinka.cs.unh.edu>
 
 // Example of parallelisation and synchronization
 
 package edu.unh.cs.files;
 
 import java.util.Random;
 import edu.unh.cs.tact.*;
 
 class File implements Comparable<File> {
 
 	static final int SPEED = 1;
 
 	private static final Random rand = new Random(44); // for determinism
 
 	final long duration;
 	final String name;
 
 	File (int i) {
 		name = String.format("File %d", i);
 		duration = (rand.nextInt(15000)+5000) / SPEED;
 	}
 	public String toString () {
 		return name;
 	}
 	public int compareTo (File f) {
 		return Long.signum(this.duration - f.duration);
 	}
 }
 
 public class SendFile {
 
 
 	public void sendFile (File f) {
 		System.err.printf("	Start sending %s by %s%n",
 			f, Thread.currentThread().getName());
 		long time = System.currentTimeMillis();
 		try {
 			Thread.sleep(f.duration);
 		} catch (InterruptedException e) {
 			return;
 		}
 		time = System.currentTimeMillis() - time;
 		System.err.printf("	 %s sent in %.2f seconds%n", f, time / 1000.0);
 	}
 
 	// Purely sequential
 	public void sendAllFiles (File[] files) {
 		for (File f : files) {
 			sendFile(f);
 		}
 	}
 
 	// Purely parallel, asynchronous
 	public void sendAllFilesPar (File[] files) {
 		for (final File f : files)
 			new Thread("Sender of "+f) {
 				public void run () {
 					sendFile(f);
 				}
 			}.start();
 	}
 
 	// Purely parallel, synchronous
 	public void sendAllFilesParWait (File[] files) {
 		final int n = files.length;
 		Thread[] senders = new Thread[n];
 		for (int i=0; i<n; i++) {
 			final File f = files[i];
 			Thread t = senders[i] = new Thread("Sender of "+f) {
 				public void run () {
 					sendFile(f);
 				}
 			};
 			t.start();
 		}
 		for (Thread t : senders)
 			try {
 				t.join();
 			} catch (InterruptedException e) {
 				continue;
 			}
 	}
 
 	// Bounded parallelism, new threads
 	public void sendAllFilesParWait (final File[] files, int k) {
 		Thread[] senders = new Thread[k];
 		class Dispatcher {
 			int next; 
 			synchronized File nextFile () {
 				if (next == files.length)
 					return null;
 				return files[next++];
 			}
 		}
 		final Dispatcher d = new Dispatcher();
 		for (int i=0; i<k; i++) {
 			Thread t = senders[i] = new Thread("Sender "+i) {
 				public void run () {
 					File f;
 					while ((f = d.nextFile()) != null)
 						sendFile(f);
 				}
 			};
 			t.start();
 		}
 		for (Thread t : senders)
 			try {
 				t.join();
 			} catch (InterruptedException e) {
 				continue;
 			}
 	}
 
 	// Bounded parallelism, reusing threads
 
 	private final Worker[] workers;
 	@GuardedBy("this") private int activeThreads;
 
 	public SendFile (int k) { // constructor takes the bound
 		workers = new Worker[k];
 		for (int i=0; i<k; i++) {
 			Thread t = workers[i] = new Worker("Worker "+i);
 			Checker.releaseAndStart(t);
 		}
 	}
 
 	public void terminate () { // to properly terminate workers
 		for (Worker w : workers)
 			w.interrupt();
 	}
 
 	private synchronized int activeCount () {
 		return activeThreads;
 	}
 
 	private synchronized void setActive () {
 		activeThreads++;
 	}
 
 	private synchronized void setInactive () {
 		activeThreads--;
 		if (activeThreads == 0)
 			notify();
 		System.err.printf(" %s is inactive%n", Thread.currentThread().getName());
 	}
 
 	private class Worker extends Thread {
 		public Worker (String name) {
 			super(name);
 		}
 		@GuardedBy("this") private Dispatcher disp;
 		public synchronized void setDispatcher (Dispatcher d) {
 			disp = d;
 			notify();
 			setActive();
 			System.err.printf(" %s is active%n", getName());
 		}
 		private synchronized void waitForWork () throws InterruptedException {
 			while (disp == null)
 				wait();
 		}
 		private synchronized void doneWorking () {
 			disp = null;
 			setInactive();
 		}
 		public void run () {
 			while (true) { // non terminating threads
 				try {
 					waitForWork();
 				} catch (InterruptedException e) {
 					return; // termination
 				}
 				while(true){
 					Dispatcher d = null;
 					synchronized(this){ d = disp; }
 					File f = d.nextFile();
 					if(f == null)
 						break;
 					sendFile(f);
 				}
 				doneWorking();
 			}
 		}
 	}
 
 	private static class Dispatcher {
 		@GuardedBy("this") private int next;
 		@GuardedBy("this") private File[] files;
 
 		public Dispatcher (File[] files) {
 			this.files = files;
			synchronized(this){ // TODO: this is unecessary in constructors
				Checker.guardBy(this.files, this);
			}
 		}
 		public synchronized File nextFile () {
 			if (next == files.length)
 				return null;
 			return files[next++];
 		}
 	}
 
 	public void sendAllFilesPoolWait (File[] files, int k) {
 		Dispatcher d = new Dispatcher(files);
 		for (int i=0, l=Math.min(k, workers.length); i<l; i++)
 			workers[i].setDispatcher(d);
 		synchronized (this) {
 			while (activeCount() > 0)
 				try {
 					wait();
 				} catch (InterruptedException e) {
 					return;
 				}
 		}
 	}
 
 	public void sendAllFilesPoolWait (File[] files) {
 		sendAllFilesPoolWait(files, Integer.MAX_VALUE);
 	}
 
 	public void sendAllFilesPoolWaitLargeFirst (File[] files, int k) {
 		java.util.Arrays.sort(files);
 		sendAllFilesPoolWait(files, k);
 	}
 
 	private static final Object lock = new Object();
 
 	public static void main (String[] args) throws Exception {
 		Checker.guardBy(System.out, lock);
 		Checker.guardBy(System.err, lock);
 
 		int p = Integer.parseInt(args[0]);
 		int n = Integer.parseInt(args[1]);
 		int k = Integer.parseInt(args[2]);
 		File[] files = new File[n];
 		for (int i=0, l=files.length; i<l; i++)
 			files[i] = new File(i);
 		SendFile s = new SendFile(p);
 		System.out.printf("Start sending %d files using %d thread%s%n",
 			n, k, k>1?"s":"");
 		System.gc();
 		long time = System.currentTimeMillis();
 		s.sendAllFilesPoolWait(files, k);
 		time = System.currentTimeMillis() - time;
 		System.out.println("All files sent");
 		System.out.printf("Sending took %.2f seconds%n", time / 1000.0);
 		s.terminate();
 	}
 }
