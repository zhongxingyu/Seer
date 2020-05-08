 package suite.stm;
 
 import java.util.HashSet;
 import java.util.Set;
 import java.util.concurrent.atomic.AtomicInteger;
 import java.util.concurrent.atomic.AtomicReference;
 
 import suite.stm.Stm.AbortException;
 import suite.stm.Stm.DeadlockException;
 import suite.stm.Stm.Memory;
 import suite.stm.Stm.Transaction;
 import suite.stm.Stm.TransactionException;
 import suite.stm.Stm.TransactionManager;
 import suite.stm.Stm.TransactionStatus;
 
 /**
  * Implements software transactional memory by locking.
 * 
  * FIXME read after read, value might be changed.
 * 
  * FIXME read after write, confused versioning.
 * 
  * TEST T0 read A, T1 write A, T1 write B, T0 read B
 * 
  * @author ywsing
  */
 public class ObstructionFreeStm implements TransactionManager {
 
 	private static final int nullTimestamp = -1;
 
 	private AtomicInteger clock = new AtomicInteger();
 
 	private class ObstructionFreeTransaction implements Transaction {
 		private volatile TransactionStatus status = TransactionStatus.ACTIVE;
 		private ObstructionFreeTransaction waitingFor;
 		private int readTimestamp = nullTimestamp;
 		private int writeTimestamp = nullTimestamp;
 		private Set<ObstructionFreeMemory<?>> touchedMemories = new HashSet<>();
 
 		public void commit() throws AbortException {
 			for (ObstructionFreeMemory<?> memory : touchedMemories)
				if (readTimestamp < memory.timestamp0 && memory.timestamp0 < writeTimestamp)
 					throw new AbortException();
 
 			setStatus(TransactionStatus.COMMITTED);
 		}
 
 		public void rollback() {
 			setStatus(TransactionStatus.ABORTED);
 		}
 
 		private int readTimestamp() {
 			if (readTimestamp == nullTimestamp)
 				readTimestamp = clock.getAndIncrement();
 			return readTimestamp;
 		}
 
 		private int writeTimestamp() {
 			if (writeTimestamp == nullTimestamp)
 				writeTimestamp = clock.getAndIncrement();
 			return writeTimestamp;
 		}
 
 		private void waitForAnotherTransaction(ObstructionFreeTransaction target) throws InterruptedException, TransactionException {
 			synchronized (ObstructionFreeStm.class) {
 				ObstructionFreeTransaction t = target;
 
 				// Detects waiting cycles and abort if deadlock is happening
 				while (t != null)
 					if (t != this)
 						t = t.waitingFor;
 					else {
 						setStatus(TransactionStatus.ABORTED);
 						throw new DeadlockException();
 					}
 
 				waitingFor = target;
 			}
 
 			try {
 				target.waitStatus();
 			} finally {
 				waitingFor = null;
 			}
 		}
 
 		private void waitStatus() throws InterruptedException {
 			if (status == TransactionStatus.ACTIVE)
 				synchronized (this) {
 					while (status == TransactionStatus.ACTIVE)
 						wait();
 				}
 		}
 
 		private synchronized void setStatus(TransactionStatus status1) {
 			status = status1;
 			notifyAll();
 		}
 	}
 
 	private static class ObstructionFreeMemory<T> implements Memory<T> {
 		private AtomicReference<ObstructionFreeTransaction> owner = new AtomicReference<>();
 		private volatile int timestamp0;
 		private volatile T value0;
 		private volatile int timestamp1;
 		private volatile T value1;
 
 		public T read(Transaction transaction) throws AbortException {
 			ObstructionFreeTransaction ourTransaction = (ObstructionFreeTransaction) transaction;
 
 			while (true) {
 				ObstructionFreeTransaction theirTransaction = owner.get();
 
 				// Finishes other committed transactions;
 				// update value by being owner temporarily
 				if (theirTransaction != null && theirTransaction.status == TransactionStatus.COMMITTED)
 					if (owner.compareAndSet(theirTransaction, ourTransaction)) {
 						timestamp0 = timestamp1;
 						value0 = value1;
 						owner.set(null); // Loses the owner status
 					} else
 						continue;
 
 				if (timestamp0 <= ourTransaction.readTimestamp()) {
 					ourTransaction.touchedMemories.add(this);
 					return value0;
 				} else
 					throw new AbortException();
 			}
 		}
 
 		public void write(Transaction transaction, T t) throws InterruptedException, TransactionException {
 			ObstructionFreeTransaction ourTransaction = (ObstructionFreeTransaction) transaction;
 			ObstructionFreeTransaction theirTransaction;
 
 			// Makes ourself the owner
 			while ((theirTransaction = owner.get()) != ourTransaction)
 				if (theirTransaction != null) {
 
 					// Waits until previous owner complete
 					ourTransaction.waitForAnotherTransaction(theirTransaction);
 
 					if (owner.compareAndSet(theirTransaction, ourTransaction)) {
 						if (theirTransaction.status == TransactionStatus.COMMITTED) {
 							timestamp0 = timestamp1;
 							value0 = value1;
 						}
 
 						break;
 					}
 				} else if (owner.compareAndSet(theirTransaction, ourTransaction))
 					break;
 
 			int writeTimestamp = ourTransaction.writeTimestamp();
 
 			if (timestamp1 <= writeTimestamp) {
 				ourTransaction.touchedMemories.add(this);
 				timestamp1 = writeTimestamp;
 				value1 = t;
 			} else
 				throw new AbortException();
 		}
 	}
 
 	@Override
 	public Transaction createTransaction() {
 		return new ObstructionFreeTransaction();
 	}
 
 	@Override
 	public <T> Memory<T> createMemory(Class<T> clazz) {
 		return new ObstructionFreeMemory<>();
 	}
 
 }
