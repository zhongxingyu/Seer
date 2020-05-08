 package org.andrill.conop4j.mutation;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.MulticastSocket;
 import java.util.List;
 import java.util.Map;
 
 import org.andrill.conop4j.Event;
 import org.andrill.conop4j.Run;
 import org.andrill.conop4j.Solution;
 import org.andrill.conop4j.listeners.Listener;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.common.io.Closeables;
 
 /**
  * A shared mutator for multiple CONOP processes running on separate machines.
  * 
  * @author Josh Reed (jareed@andrill.org)
  */
 public class MulticastSharedMutator implements MutationStrategy, Listener {
 	private class MulticastThread extends Thread {
 		Map<Integer, Event> events = Maps.newHashMap();
 		InetAddress group;
 		Run run;
 		MulticastSocket socket;
 		boolean stopped = false;
 
 		public MulticastThread(final Run run) {
 			// mark ourselves as a daemon thread
 			setDaemon(true);
 
 			// save our run and make an event map
 			this.run = run;
 			for (Event e : run.getEvents()) {
 				events.put(e.getInternalId(), e);
 			}
 
 			try {
 				// setup our multicast socket
 				group = InetAddress.getByName("224.0.0.17");
 				socket = new MulticastSocket(new InetSocketAddress(4499));
 				socket.joinGroup(group);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 
 		}
 
 		public void broadcast(final Solution solution) {
 			lastBroadcast = 0;
 			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
 			DataOutputStream dos = null;
 			try {
 				dos = new DataOutputStream(buffer);
 				dos.writeDouble(solution.getScore());
 				for (int i = 0; i < events.size(); i++) {
 					dos.writeInt(solution.getEvent(i).getInternalId());
 				}
 				dos.flush();
 				byte[] bytes = buffer.toByteArray();
 				socket.send(new DatagramPacket(bytes, bytes.length, group, 4499));
 			} catch (IOException e) {
 				e.printStackTrace();
 			} finally {
 				Closeables.closeQuietly(dos);
 			}
 		}
 
 		@Override
 		public void run() {
 			byte[] buffer = new byte[4 * run.getEvents().size() + 8];
 			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
 			while (!stopped) {
 				DataInputStream dis = null;
 				try {
 					socket.receive(packet);
 					dis = new DataInputStream(new ByteArrayInputStream(buffer));
 					double score = dis.readDouble();
 					if (score < localBest) {
 						List<Event> list = Lists.newArrayList();
 						for (int i = 0; i < events.size(); i++) {
 							list.add(events.get(dis.readInt()));
 						}
 						Solution solution = new Solution(run, list);
 						solution.setScore(score);
 						remote = solution;
 					}
 				} catch (IOException e) {
 					e.printStackTrace();
 				} finally {
 					Closeables.closeQuietly(dis);
 				}
 			}
 		}
 	}
 
 	protected final double factor;
 	protected long lastBroadcast = 0;
 	protected double localBest = Double.MAX_VALUE;
	protected final MulticastThread multicast;
 	protected final MutationStrategy mutator;
 	protected Solution remote;
 
 	/**
 	 * Create a new MulticastSharedMutator.
 	 * 
 	 * @param run
 	 *            the run.
 	 * @param mutator
 	 *            the base mutator.
 	 * @param factor
 	 *            the acceptance factor.
 	 */
 	public MulticastSharedMutator(final Run run, final MutationStrategy mutator, final double factor) {
 		this.mutator = mutator;
 		this.factor = factor;
 		multicast = new MulticastThread(run);
		multicast.start();
 	}
 
 	@Override
 	public Solution mutate(final Solution solution) {
 		if ((remote != null) && (remote.getScore() < factor * solution.getScore())) {
 			System.out.println("Teleported to " + remote.getScore());
 			Solution next = new Solution(remote.getRun(), remote.getEvents());
 			remote = null;
 			return next;
 		} else {
 			return mutator.mutate(solution);
 		}
 	}
 
 	@Override
 	public String toString() {
 		return "Multicast [" + mutator + "]";
 	}
 
 	@Override
 	public void tried(final double temp, final Solution current, final Solution best) {
 		lastBroadcast++;
 		if (best.getScore() < localBest) {
 			localBest = best.getScore();
 			multicast.broadcast(best);
 		} else if (lastBroadcast > 10000) {
 			multicast.broadcast(best);
 		}
 	}
 }
