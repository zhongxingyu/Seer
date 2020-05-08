 package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.distributor.impl;
 
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.Nodes;
 import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.PositionUpdateMsgs;
 import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.RelayServers;
 import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.Node;
 import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.PositionUpdateMsg;
 import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.RelayServer;
 import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.Sender;
 import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.distributor.DistributorWorker;
 import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.util.MyIPAddresses;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Required;
 import org.springframework.stereotype.Repository;
 import org.springframework.transaction.annotation.Transactional;
 
 @Repository
 public class DistributorWorkerImpl implements DistributorWorker {
 	protected Logger logger = Logger.getLogger(this.getClass().getName());
 
 	private int packetMaxSize;
 
 	/**
 	 * @param packetMaxSize
 	 *          the packetMaxSize to set
 	 */
 	@Required
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
 
 	/** the UDP port to distribute on */
 	private int uplinkUdpPort = -1;
 
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
 
 	public void init() throws IOException {
 		this.sock = new DatagramSocket(null);
 		this.sock.setReuseAddress(true);
 		this.sock.bind(new InetSocketAddress(this.uplinkUdpPort));
 	}
 
 	public void uninit() {
 		if (this.sock != null) {
 			this.sock.close();
 			this.sock = null;
 		}
 	}
 
 	/*
 	 * Distribution
 	 */
 
 	private long lastDistributionTime = -1;
 
 	private DatagramSocket sock;
 
 	private DatagramPacket toPacket(List<PositionUpdateMsg> positionUpdateMsgs, int positionUpdateMsgsByteCount) {
 		assert (positionUpdateMsgs != null);
 		assert (positionUpdateMsgs.size() > 0);
 		assert (positionUpdateMsgsByteCount > 0);
 		assert (positionUpdateMsgsByteCount <= this.packetMaxSize);
 
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
 			if ((packetPositionUpdateMsgsByteCount + positionUpdateMsgLength) > this.packetMaxSize) {
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
 
 	@Override
 	@Transactional(readOnly = true)
 	public void distribute(AtomicInteger signaledUpdates) {
 		while (signaledUpdates.getAndSet(0) > 0) {
 			long currentTime = System.currentTimeMillis();
 
 			if (this.logger.isDebugEnabled()) {
 				this.logger.debug("*** Have to distribute <" + this.lastDistributionTime + ", " + currentTime + "]");
 			}
 
 			/*
 			 * Distribute to other relay servers
 			 */
 
 			if (this.logger.isDebugEnabled()) {
 				this.logger.debug("*** relay servers");
 			}
 
 			List<RelayServer> otherRelayServers = this.relayServers.getOtherRelayServers();
 			if ((otherRelayServers != null) && (otherRelayServers.size() > 0)) {
 				List<PositionUpdateMsg> p4ds = this.positions.getPositionUpdateMsgForDistribution(this.lastDistributionTime,
 						currentTime, null);
 				if (this.logger.isDebugEnabled()) {
 					StringBuilder s = new StringBuilder();
 					s.append("p4ds(" + p4ds.size() + ")=");
 					for (PositionUpdateMsg p4d : p4ds) {
 						s.append(" " + p4d.getId());
 					}
 					this.logger.debug(s.toString());
 				}
 
 				List<DatagramPacket> packets = positionUpdateMsgsToPackets(p4ds);
 				if ((packets != null) && (packets.size() > 0)) {
 					StringBuilder s = new StringBuilder();
 					for (RelayServer otherRelayServer : otherRelayServers) {
 						InetAddress otherRelayServerIp = otherRelayServer.getIp();
 						int otherRelayServerPort = otherRelayServer.getPort().intValue();
 
 						if (this.logger.isDebugEnabled()) {
 							s.setLength(0);
 							s.append("tx " + packets.size() + " packet(s) to " + otherRelayServerIp.getHostAddress() + ":"
 									+ otherRelayServerPort + ", sizes=");
 						}
 						for (DatagramPacket packet : packets) {
 							if (this.logger.isDebugEnabled()) {
 								s.append(" " + packet.getLength());
 							}
 							packet.setAddress(otherRelayServerIp);
 							packet.setPort(otherRelayServerPort);
 							try {
 								this.sock.send(packet);
 							} catch (IOException e) {
 								if (this.logger.isDebugEnabled()) {
 									s.append(" ERROR: " + e.getLocalizedMessage());
 								}
 								this.logger.error("Could not send to relay server " + otherRelayServerIp + ":" + otherRelayServerPort
 										+ " : " + e.getLocalizedMessage());
 							}
 						}
 						if (this.logger.isDebugEnabled()) {
 							this.logger.debug(s.toString());
 						}
 					}
 				}
 			}
 
 			/*
 			 * Cluster Leaders
 			 */
 
 			List<Node> clusterLeaders = this.nodes.getClusterLeaders();
 			if ((clusterLeaders != null) && (clusterLeaders.size() > 0)) {
 				for (Node clusterLeader : clusterLeaders) {
 					InetAddress clusterLeaderMainIp = clusterLeader.getMainIp();
 
 					Sender clusterLeaderSender = clusterLeader.getSender();
 					if (clusterLeaderSender == null) {
 						Node substituteClusterLeader = this.nodes.getSubstituteClusterLeader(clusterLeader);
 						if (substituteClusterLeader == null) {
 							if (this.logger.isDebugEnabled()) {
 								this.logger.info("Cluster leader " + clusterLeaderMainIp.getHostAddress()
 										+ " has no sender and no substitute cluster leader is found: skipped");
 							}
 							continue;
 						}
 
						clusterLeaderSender = substituteClusterLeader.getSender();
 						if (this.logger.isDebugEnabled()) {
 							this.logger.info("Cluster leader " + clusterLeaderMainIp.getHostAddress()
									+ " has no sender: selected sender " + clusterLeaderSender.getIp().getHostAddress() + ":"
									+ clusterLeaderSender.getPort() + " of substitute cluster leader "
 									+ substituteClusterLeader.getMainIp().getHostAddress());
 						}
 					}
 
 					InetAddress clusterLeaderSenderIp = clusterLeaderSender.getIp();
 					Integer clusterLeaderSenderPort = clusterLeaderSender.getPort();
 
 					if (this.logger.isDebugEnabled()) {
 						this.logger.debug("*** cluster leader " + clusterLeaderMainIp.getHostAddress() + " (sender="
 								+ clusterLeaderSenderIp.getHostAddress() + ":" + clusterLeaderSenderPort + ")");
 					}
 
 					if ((this.myIPAddresses.isMe(clusterLeaderSenderIp) || this.myIPAddresses.isMe(clusterLeaderMainIp))
 							&& (clusterLeaderSenderPort.intValue() == this.uplinkUdpPort)) {
 						/* do not relay to ourselves */
 						if (this.logger.isDebugEnabled()) {
 							this.logger.debug("this is me: skipping");
 						}
 						continue;
 					}
 
 					List<PositionUpdateMsg> p4ds = this.positions.getPositionUpdateMsgForDistribution(this.lastDistributionTime,
 							currentTime, clusterLeader);
 					if ((p4ds == null) || (p4ds.size() == 0)) {
 						if (this.logger.isDebugEnabled()) {
 							this.logger.debug("p4ds EMPTY");
 						}
 						continue;
 					}
 
 					if (this.logger.isDebugEnabled()) {
 						StringBuilder s = new StringBuilder();
 						s.append("p4ds(" + p4ds.size() + ")=");
 						for (PositionUpdateMsg p4d : p4ds) {
 							s.append(" " + p4d.getId());
 						}
 						this.logger.debug(s.toString());
 					}
 
 					List<DatagramPacket> packets = positionUpdateMsgsToPackets(p4ds);
 					if ((packets != null) && (packets.size() > 0)) {
 						StringBuilder s = new StringBuilder();
 						if (this.logger.isDebugEnabled()) {
 							s.setLength(0);
 							s.append("tx " + packets.size() + " packet(s) to " + clusterLeaderMainIp.getHostAddress() + " (sender="
 									+ clusterLeaderSenderIp.getHostAddress() + ":" + clusterLeaderSenderPort + "), sizes=");
 						}
 
 						for (DatagramPacket packet : packets) {
 							if (this.logger.isDebugEnabled()) {
 								s.append(" " + packet.getLength());
 							}
 							packet.setAddress(clusterLeaderSenderIp);
 							packet.setPort(clusterLeaderSenderPort.intValue());
 							try {
 								this.sock.send(packet);
 							} catch (IOException e) {
 								if (this.logger.isDebugEnabled()) {
 									s.append(" ERROR:" + e.getLocalizedMessage());
 									this.logger.debug(s.toString());
 								}
 								this.logger.error("Could not send to cluster leader " + clusterLeaderMainIp + " (sender="
 										+ clusterLeaderSenderIp.getHostAddress() + ":" + clusterLeaderSenderPort + ") : "
 										+ e.getLocalizedMessage());
 							}
 						}
 						if (this.logger.isDebugEnabled()) {
 							this.logger.debug(s.toString());
 						}
 					}
 				}
 			}
 
 			this.lastDistributionTime = currentTime;
 		}
 	}
 }
