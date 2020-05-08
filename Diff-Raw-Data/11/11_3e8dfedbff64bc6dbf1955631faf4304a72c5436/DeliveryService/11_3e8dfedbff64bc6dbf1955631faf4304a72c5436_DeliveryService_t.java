 package common.network;
 
 import java.net.SocketAddress;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.PriorityQueue;
 import java.util.concurrent.RejectedExecutionException;
import java.util.Iterator;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import common.AbstractExecutorTask;
 import common.CommonPacketType;
 import common.GameException;
 import common.Util;
 
 public class DeliveryService implements PacketObserver {
 	private final PriorityQueue<Task> tasks;
 	private final Map<SocketAddress, UDPConnection> connections;
 	private final PacketSender ps;
 	private final Task compareSingleton; //used to remove Tasks
 
 	private static final long TIMEOUT = 500;
 
 	public DeliveryService(PacketSender ps) {
 		this.ps = ps;
 		tasks = new PriorityQueue<Task>(91);
 		connections = new HashMap<SocketAddress, UDPConnection>();
 		final Thread periodicResend = new PeriodicResend();
 		periodicResend.start();
 
 		compareSingleton = new Task(0, (byte) 0, null);
 	}
 
 	public void addDelivery(byte[] data, UDPConnection udpc) {
 		ps.exec.submit(new New(data, udpc));
 	}
 
 	public void acknowledge(byte seq, SocketAddress saddr) {
 		ps.exec.submit(new Ack(saddr, seq));
 	}
 
 	private class PeriodicResend extends Thread {
 		private long schedule;
 
 		public void start() {
 			schedule = System.currentTimeMillis();
 			setDaemon(true);
 			super.start();
 		}
 
 		public void run() {
 			try {
 				while (true) {
 					final long curt = System.currentTimeMillis();
 					final long diff = schedule - curt;
 					if (diff > 0) {
 						sleep(diff);
 					}
 
 					ps.exec.submit(new ResendTimedOut());
 
 					schedule += 50;
 				}
 			} catch (final RejectedExecutionException e) {
 				Logger.getLogger(getClass().getName()).log(Level.INFO, "Rejected execution of resend task: This is NOT a problem if you were shutting down.");
 			} catch (final InterruptedException e) {
 				Logger.getLogger(getClass().getName()).log(Level.WARNING,"Periodic resend thread interrupted!", e);
 			}
 		}
 	}
 
 	private static class Task implements Comparable<Task> {
 		long timeout;
 		byte seq;
 		UDPConnection udpc;
 		int left = 10;
 
 		Task(long time, byte seq, UDPConnection udpc) {
 			this.timeout = time;
 			this.seq = seq;
 			this.udpc = udpc;
 		}
 
 		public int compareTo(Task t) {
 			if (timeout == t.timeout) {
 				return 0;
 			}
 
 			return timeout > t.timeout ? 1 : -1;
 		}
 
 		public boolean equals(Object o) {
 			if (o instanceof Task) {
 				final Task t = (Task) o;
 				return (t.seq == seq && t.udpc.dgp.getSocketAddress().equals(
 						udpc.dgp.getSocketAddress()));
 
 			}
 			return false;
 		}
 	}
 
 	private void send(Task t) {
 		t.timeout = System.currentTimeMillis() + TIMEOUT;
 		t.left--;
 		if (t.left >= 0) {
 			ps.send(t.udpc.getData(t.seq), t.udpc.dgp);
 			tasks.add(t);
 		} else {
 			Logger.getLogger(getClass().getName()).log(Level.SEVERE,
 					"Maximum number of retries reached");
 			System.exit(1);
 			// TODO: inte exit! :D
 		}
 	}
 
 	private class New extends AbstractExecutorTask {
 
 		private final byte[] data;
 		private final UDPConnection udpc;
 
 		public New(byte[] data, UDPConnection udpc) {
 			this.data = data;
 			this.udpc = udpc;
 		}
 
 		public void execute() {
 			try {
 				final byte seq = udpc.addControlledPacket(data);
 				final Task t = new Task(System.currentTimeMillis() + TIMEOUT,
 						seq, udpc);
 				connections.put(udpc.dgp.getSocketAddress(), udpc);
 
 				send(t);
 			} catch (final SendWindowFullException e) {
 				Logger.getLogger(getClass().getName()).log(Level.SEVERE,
 						e.getMessage(), e);
 			}
 		}
 	}
 
 	private class ResendTimedOut extends AbstractExecutorTask {
 
 		public void execute() {
 			final long curt = System.currentTimeMillis();
 			Task t = tasks.peek();
 			while (t != null && t.timeout <= curt) {
 				Logger.getLogger(getClass().getName()).log(Level.INFO,
 						"Resending " + t.seq + " to " + t.udpc);
 				tasks.remove(t);
 				send(t);
 				t = tasks.peek();
 			}
 		}
 	}
 
 	private class Ack extends AbstractExecutorTask {
 		private final byte seq;
 		private final SocketAddress saddr;
 
 		Ack(SocketAddress saddr, byte seq) {
 			this.saddr = saddr;
 			this.seq = seq;
 		}
 
 		public void execute() {
 			final UDPConnection udpc = connections.get(saddr);
 			udpc.acknowledged(seq);
 			compareSingleton.seq = seq;
 			compareSingleton.udpc = udpc;

			for(Iterator<Task> it = tasks.iterator(); it.hasNext();) {
				Task t = it.next();
				if(t.equals(compareSingleton)) {
					it.remove();
					break;
				}
			}
 
 			if (udpc.getNumPending() == 0) {
 				connections.remove(saddr);
 			}
 		}
 	}
 
 	private class Notify extends AbstractExecutorTask {
 		public void execute() {
 			if (tasks.isEmpty()) {
 				synchronized (DeliveryService.this) {
 					DeliveryService.this.notifyAll();
 				}
 			} else {
 				ps.exec.submit(this);
 			}
 		}
 	}
 
 	public boolean packetReceived(byte[] data, SocketAddress addr) throws GameException {
 		if (Util.packetType(data) == CommonPacketType.ACK) {
 			final byte seq = data[1];
 			acknowledge(seq, addr);
 			return true;
 		}
 		return false;
 	}
 
 	public synchronized void waitForAllDelivered() {
 		try {
 			ps.exec.submit(new Notify());
 			while (!tasks.isEmpty()) {
 				wait();
 			}
 		} catch (final InterruptedException e) {
 		}
 	}
 }
