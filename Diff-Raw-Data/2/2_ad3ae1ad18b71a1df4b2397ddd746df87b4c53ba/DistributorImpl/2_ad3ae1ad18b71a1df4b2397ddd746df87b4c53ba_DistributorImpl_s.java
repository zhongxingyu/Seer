 package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.distributor.impl;
 
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.Nodes;
 import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.PositionUpdateMsgs;
 import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.RelayServers;
 import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.Gateway;
 import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.Node;
 import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.PositionUpdateMsg;
 import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.RelayServer;
 import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.distributor.Distributor;
 import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.util.MyIPAddresses;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Required;
 
 public class DistributorImpl extends Thread implements Distributor {
 	private Logger logger = Logger.getLogger(this.getClass().getName());
 
 	private int packetMaxSize = 1450;
 
 	/**
 	 * @param packetMaxSize
 	 *          the packetMaxSize to set
 	 */
 	public final void setPacketMaxSize(int packetMaxSize) {
 		this.packetMaxSize = packetMaxSize;
 	}
 
 	private MyIPAddresses myIPAddresses;
 
 	/**
 	 * @param myIPAddresses
 	 *          the myIPAddresses to set
 	 */
 	@Required
 	public final void setMyIPAddresses(MyIPAddresses myIPAddresses) {
 		this.myIPAddresses = myIPAddresses;
 	}
 
 	/** the UDP port to listen on for uplink messages */
 	private Integer uplinkUdpPort = null;
 
 	/**
 	 * @param uplinkUdpPort
 	 *          the uplinkUdpPort to set
 	 */
 	@Required
 	public final void setUplinkUdpPort(int uplinkUdpPort) {
 		this.uplinkUdpPort = uplinkUdpPort;
 	}
 
 	/** the Node handler */
 	private Nodes nodes;
 
 	/**
 	 * @param nodes
 	 *          the nodes to set
 	 */
 	@Required
 	public final void setNodes(Nodes nodes) {
 		this.nodes = nodes;
 	}
 
 	/** the PositionUpdateMsgs handler */
 	private PositionUpdateMsgs positions;
 
 	/**
 	 * @param positions
 	 *          the positions to set
 	 */
 	@Required
 	public final void setPositions(PositionUpdateMsgs positions) {
 		this.positions = positions;
 	}
 
 	private RelayServers relayServers;
 
 	/**
 	 * @param relayServers
 	 *          the relayServers to set
 	 */
 	@Required
 	public final void setRelayServers(RelayServers relayServers) {
 		this.relayServers = relayServers;
 	}
 
 	private long distributionDelay = 1000;
 
 	/**
 	 * @param distributionDelay
 	 *          the distributionDelay to set
 	 */
 	public final void setDistributionDelay(long distributionDelay) {
 		this.distributionDelay = distributionDelay;
 	}
 
 	public void init() throws SocketException, UnknownHostException {
 		this.setName(this.getClass().getSimpleName());
 		timer = new Timer(this.getClass().getSimpleName() + "-Timer");
 		sock = new DatagramSocket();
 		this.start();
 	}
 
 	public void destroy() {
 		run.set(false);
 		if (timer != null) {
 			timer.cancel();
 			timer = null;
 		}
 		synchronized (runWaiter) {
 			runWaiter.notifyAll();
 		}
 	}
 
 	private AtomicBoolean run = new AtomicBoolean(true);
 	private Object runWaiter = new Object();
 	private AtomicBoolean distribute = new AtomicBoolean(false);
 
 	@Override
 	public void run() {
 		while (run.get()) {
 			boolean distributeNow = distribute.getAndSet(false);
 			synchronized (runWaiter) {
 				try {
 					if (!distributeNow) {
 						runWaiter.wait();
 						distributeNow = distribute.getAndSet(false);
 					}
 				} catch (InterruptedException e) {
 					/* swallow */
 				}
 			}
 			if (distributeNow) {
 				try {
 					distribute();
 				} catch (Throwable e) {
 					logger.error("error during distribution", e);
 				}
 			}
 		}
 	}
 
 	/*
 	 * Distribution
 	 */
 
 	private Timer timer = null;
 	private AtomicBoolean signaledUpdates = new AtomicBoolean(false);
 
 	private long lastDistributionTime = -1;
 
 	public void signalUpdate() {
 		boolean previousSignaledUpdates = signaledUpdates.getAndSet(true);
 		if (!previousSignaledUpdates) {
 			timer.schedule(new TimerTask() {
 				@Override
 				public void run() {
 					distribute.set(true);
 					synchronized (runWaiter) {
 						runWaiter.notifyAll();
 					}
 				}
 			}, distributionDelay);
 		}
 	}
 
 	private DatagramSocket sock;
 
 	private DatagramPacket toPacket(List<PositionUpdateMsg> positionUpdateMsgs, int positionUpdateMsgsByteCount) {
 		assert (positionUpdateMsgs != null);
 		assert (positionUpdateMsgs.size() > 0);
 		assert (positionUpdateMsgsByteCount > 0);
 		assert (positionUpdateMsgsByteCount <= packetMaxSize);
 
 		DatagramPacket packet = new DatagramPacket(new byte[positionUpdateMsgsByteCount], positionUpdateMsgsByteCount);
 		byte[] packetData = packet.getData();
 		int packetDataIndex = 0;
 		for (PositionUpdateMsg positionUpdateMsg : positionUpdateMsgs) {
 			byte[] positionUpdateMsgData = positionUpdateMsg.getPositionUpdateMsg().getData();
 			int positionUpdateMsgDataLength = positionUpdateMsgData.length;
 			System.arraycopy(positionUpdateMsgData, 0, packetData, packetDataIndex, positionUpdateMsgDataLength);
 			packetDataIndex += positionUpdateMsgDataLength;
 		}
 
 		return packet;
 	}
 
 	private List<DatagramPacket> positionUpdateMsgsToPackets(List<PositionUpdateMsg> positionUpdateMsgsToDistribute) {
 		if ((positionUpdateMsgsToDistribute == null) || (positionUpdateMsgsToDistribute.size() == 0)) {
 			return null;
 		}
 
 		List<DatagramPacket> result = new LinkedList<DatagramPacket>();
 
 		List<PositionUpdateMsg> packetPositionUpdateMsgs = new LinkedList<PositionUpdateMsg>();
 		int packetPositionUpdateMsgsByteCount = 0;
 		for (PositionUpdateMsg positionUpdateMsgToDistribute : positionUpdateMsgsToDistribute) {
 			int positionUpdateMsgLength = positionUpdateMsgToDistribute.getPositionUpdateMsg().getData().length;
 			if ((packetPositionUpdateMsgsByteCount + positionUpdateMsgLength) > packetMaxSize) {
 				result.add(toPacket(packetPositionUpdateMsgs, packetPositionUpdateMsgsByteCount));
 				packetPositionUpdateMsgs.clear();
 				packetPositionUpdateMsgsByteCount = 0;
 			}
 
 			packetPositionUpdateMsgs.add(positionUpdateMsgToDistribute);
 			packetPositionUpdateMsgsByteCount += positionUpdateMsgLength;
 		}
 		if (packetPositionUpdateMsgs.size() != 0) {
 			result.add(toPacket(packetPositionUpdateMsgs, packetPositionUpdateMsgsByteCount));
 		}
 
 		return result;
 	}
 
 	private void distribute() {
 		while (signaledUpdates.getAndSet(false)) {
 			long currentTime = System.currentTimeMillis();
 
 			if (logger.isDebugEnabled()) {
 				logger.debug("*** Have to distribute <" + lastDistributionTime + ", " + currentTime + "]");
 			}
 
 			/*
 			 * Distribute to other relay servers
 			 */
 
 			if (logger.isDebugEnabled()) {
 				logger.debug("*** relay servers");
 			}
 
 			List<RelayServer> otherRelayServers = relayServers.getOtherRelayServers();
 			if ((otherRelayServers != null) && (otherRelayServers.size() > 0)) {
 				List<PositionUpdateMsg> p4ds = positions.getPositionUpdateMsgForDistribution(lastDistributionTime, currentTime,
 						null);
 				if (logger.isDebugEnabled()) {
 					StringBuilder s = new StringBuilder();
 					s.append("p4ds(" + p4ds.size() + ")=");
 					for (PositionUpdateMsg p4d : p4ds) {
 						s.append(" " + p4d.getId());
 					}
 					logger.debug(s.toString());
 				}
 
 				List<DatagramPacket> packets = positionUpdateMsgsToPackets(p4ds);
 				if ((packets != null) && (packets.size() > 0)) {
 					StringBuilder s = new StringBuilder();
 					for (RelayServer otherRelayServer : otherRelayServers) {
 						InetAddress otherRelayServerIp = otherRelayServer.getIp();
 						int otherRelayServerPort = otherRelayServer.getPort();
 
 						if (logger.isDebugEnabled()) {
 							s.setLength(0);
 							s.append("tx " + packets.size() + " packet(s) to " + otherRelayServerIp.getHostAddress() + ":"
 									+ otherRelayServerPort + ", sizes=");
 						}
 						for (DatagramPacket packet : packets) {
 							if (logger.isDebugEnabled()) {
 								s.append(" " + packet.getLength());
 							}
 							packet.setAddress(otherRelayServerIp);
 							packet.setPort(otherRelayServerPort);
 							try {
 								sock.send(packet);
 							} catch (IOException e) {
 								if (logger.isDebugEnabled()) {
 									s.append(" ERROR: " + e.getLocalizedMessage());
 								}
 								logger.error("Could not send to relay server " + otherRelayServerIp + ":" + otherRelayServerPort
 										+ " : " + e.getLocalizedMessage());
 							}
 						}
 						if (logger.isDebugEnabled()) {
 							logger.debug(s.toString());
 						}
 					}
 				}
 			}
 
 			/*
 			 * Cluster Leaders
 			 */
 
 			List<Node> clusterLeaders = nodes.getClusterLeaders();
 			if ((clusterLeaders != null) && (clusterLeaders.size() > 0)) {
 				for (Node clusterLeader : clusterLeaders) {
 					InetAddress clusterLeaderMainIp = clusterLeader.getMainIp();
 
 					Gateway clusterLeaderGateway = clusterLeader.getGateway();
 					if (clusterLeaderGateway == null) {
 						Node substituteClusterLeader = nodes.getSubstituteClusterLeader(clusterLeader);
 						if (substituteClusterLeader == null) {
 							if (logger.isDebugEnabled()) {
 								logger.info("Cluster leader " + clusterLeaderMainIp.getHostAddress()
 										+ " has no gateway and no substitute cluster leader is found: skipped");
 							}
 							continue;
 						}
 
 						clusterLeaderGateway = substituteClusterLeader.getGateway();
 						if (logger.isDebugEnabled()) {
 							logger.info("Cluster leader " + clusterLeaderMainIp.getHostAddress()
 									+ " has no gateway: selected gateway " + clusterLeaderGateway.getIp().getHostAddress() + ":"
 									+ clusterLeaderGateway.getPort() + " of substitute cluster leader "
 									+ substituteClusterLeader.getMainIp().getHostAddress());
 						}
 					}
 
 					InetAddress clusterLeaderGatewayIp = clusterLeaderGateway.getIp();
 					Integer clusterLeaderGatewayPort = clusterLeaderGateway.getPort();
 
 					if (logger.isDebugEnabled()) {
 						logger.debug("*** cluster leader " + clusterLeaderMainIp.getHostAddress() + " (gateway="
 								+ clusterLeaderGatewayIp.getHostAddress() + ":" + clusterLeaderGatewayPort + ")");
 					}
 
 					if ((myIPAddresses.isMe(clusterLeaderGatewayIp) || myIPAddresses.isMe(clusterLeaderMainIp))
							&& (clusterLeaderGatewayPort == uplinkUdpPort)) {
 						/* do not relay to ourselves */
 						if (logger.isDebugEnabled()) {
 							logger.debug("this is me: skipping");
 						}
 						continue;
 					}
 
 					List<PositionUpdateMsg> p4ds = positions.getPositionUpdateMsgForDistribution(lastDistributionTime,
 							currentTime, clusterLeader);
 					if ((p4ds == null) || (p4ds.size() == 0)) {
 						if (logger.isDebugEnabled()) {
 							logger.debug("p4ds EMPTY");
 						}
 						continue;
 					}
 
 					if (logger.isDebugEnabled()) {
 						StringBuilder s = new StringBuilder();
 						s.append("p4ds(" + p4ds.size() + ")=");
 						for (PositionUpdateMsg p4d : p4ds) {
 							s.append(" " + p4d.getId());
 						}
 						logger.debug(s.toString());
 					}
 
 					List<DatagramPacket> packets = positionUpdateMsgsToPackets(p4ds);
 					if ((packets != null) && (packets.size() > 0)) {
 						StringBuilder s = new StringBuilder();
 						if (logger.isDebugEnabled()) {
 							s.setLength(0);
 							s.append("tx " + packets.size() + " packet(s) to " + clusterLeaderMainIp.getHostAddress() + " (gateway="
 									+ clusterLeaderGatewayIp.getHostAddress() + ":" + clusterLeaderGatewayPort + "), sizes=");
 						}
 
 						for (DatagramPacket packet : packets) {
 							if (logger.isDebugEnabled()) {
 								s.append(" " + packet.getLength());
 							}
 							packet.setAddress(clusterLeaderGatewayIp);
 							packet.setPort(clusterLeaderGatewayPort);
 							try {
 								sock.send(packet);
 							} catch (IOException e) {
 								if (logger.isDebugEnabled()) {
 									s.append(" ERROR:" + e.getLocalizedMessage());
 									logger.debug(s.toString());
 								}
 								logger.error("Could not send to cluster leader " + clusterLeaderMainIp + " (gateway="
 										+ clusterLeaderGatewayIp.getHostAddress() + ":" + clusterLeaderGatewayPort + ") : "
 										+ e.getLocalizedMessage());
 							}
 						}
 						if (logger.isDebugEnabled()) {
 							logger.debug(s.toString());
 						}
 					}
 				}
 			}
 
 			lastDistributionTime = currentTime;
 		}
 	}
 }
