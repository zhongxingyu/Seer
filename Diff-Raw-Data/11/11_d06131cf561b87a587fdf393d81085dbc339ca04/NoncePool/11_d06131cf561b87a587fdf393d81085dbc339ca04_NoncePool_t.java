 package util;
 
 import java.util.HashMap;
 import java.util.Random;
 
 public class NoncePool<E> {
 	//first is the bound object, second is the creation time (used to clean up 'dead' nonces)
 	private HashMap<Long, Pair<E, Long>> ncs = new HashMap<Long, Pair<E, Long>>();
 	private Random r = new Random(System.nanoTime());
 	//private static final int DELETION_THRESHOLD = 1;//120*1000;	//2 minutes in milliseconds
	public synchronized long bind(E e) {
 		long nonce=createNonce();
 		ncs.put(nonce, new Pair<E, Long>(e, System.currentTimeMillis()));
 		return nonce;
 	}
	public synchronized E get(long nonce) {
 		if (ncs.containsKey(nonce))
 			return ncs.get(nonce).first;
 		return null;
 	}
	public synchronized void remove(long nonce) {
 		ncs.remove(nonce);
 	}
 	public synchronized void cleanup() {
 		//This might come back later.  I don't think it's really a problem so far as memory
 		//usage or whatever, and I don't think this actually works, so I'll just leave it
 		//out for now and see how it goes
 		/*
 		long begin = System.currentTimeMillis();
 		ArrayList<Long> toRemove = new ArrayList<Long>();
 		for (long l : ncs.keySet())
 			if (begin - ncs.get(l).second > DELETION_THRESHOLD)
 				toRemove.add(l);
 		for (long l : toRemove)
 			ncs.remove(l);
 		*/
 	}
	private synchronized long createNonce() {
 		long l;
 		do
 			l = Math.abs(r.nextLong());
 		while (ncs.containsKey(l));
 		return l;
 	}
 }
