 /*
  *  Copyright (C) 2012 Royal Dutch Army
  *  
  *  This program is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU General Public License
  *  as published by the Free Software Foundation; either version 2
  *  of the License, or (at your option) any later version.
  *  
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *  
  *  You should have received a copy of the GNU General Public License
  *  along with this program; if not, write to the Free Software
  *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  *  MA  02110-1301, USA.
  */
 package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.logger.impl;
 
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 import java.nio.channels.FileChannel;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.Formatter;
 import java.util.FormatterClosedException;
 import java.util.IllegalFormatException;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.constants.Constants;
 import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.ClusterLeaderMsgs;
 import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.Nodes;
 import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.PositionUpdateMsgs;
 import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.RelayServers;
 import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.Senders;
 import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.ClusterLeaderMsg;
 import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.Node;
 import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.PositionUpdateMsg;
 import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.Sender;
 import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.util.TxChecker;
 import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.logger.DatabaseLogger;
 import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.reportonce.ReportOnce;
 import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.reportonce.ReportSubject;
 
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.olsr.plugin.pud.PositionUpdate;
 import org.springframework.beans.factory.annotation.Required;
 import org.springframework.stereotype.Repository;
 import org.springframework.transaction.annotation.Transactional;
 
 @Repository
 public class DatabaseLoggerImpl implements DatabaseLogger {
 	protected Logger logger = Logger.getLogger(this.getClass().getName());
 
 	private String databaseLogFile = null;
 
 	/**
 	 * @param databaseLogFile
 	 *          the databaseLogFile to set
 	 */
 	@Required
 	public void setDatabaseLogFile(String databaseLogFile) {
 		this.databaseLogFile = databaseLogFile;
 	}
 
 	private String dotSimpleFile = null;
 
 	/**
 	 * @param dotSimpleFile
 	 *          the dotFullFile to set
 	 */
 	@Required
 	public void setDotSimpleFile(String dotSimpleFile) {
 		this.dotSimpleFile = dotSimpleFile;
 	}
 
 	private String dotFullFile = null;
 
 	/**
 	 * @param dotFullFile
 	 *          the dotFullFile to set
 	 */
 	@Required
 	public void setDotFullFile(String dotFullFile) {
 		this.dotFullFile = dotFullFile;
 	}
 
 	private boolean detectDuplicateNames = true;
 
 	/**
 	 * @param detectDuplicateNames
 	 *          the detectDuplicateNames to set
 	 */
 	@Required
 	public final void setDetectDuplicateNames(boolean detectDuplicateNames) {
 		this.detectDuplicateNames = detectDuplicateNames;
 	}
 
 	private boolean generateSVG = false;
 
 	/**
 	 * @param generateSVG
 	 *          the generateSVG to set
 	 */
 	@Required
 	public void setGenerateSVG(boolean generateSVG) {
 		this.generateSVG = generateSVG;
 	}
 
 	private String svgSimpleFile = null;
 
 	/**
 	 * @param svgSimpleFile
 	 *          the svgSimpleFile to set
 	 */
 	@Required
 	public void setSvgSimpleFile(String svgSimpleFile) {
 		this.svgSimpleFile = svgSimpleFile;
 	}
 
 	private String svgFullFile = null;
 
 	/**
 	 * @param svgFullFile
 	 *          the svgFullFile to set
 	 */
 	@Required
 	public void setSvgFullFile(String svgFullFile) {
 		this.svgFullFile = svgFullFile;
 	}
 
 	/** the Node handler */
 	private Nodes nodes;
 
 	/**
 	 * @param nodes
 	 *          the nodes to set
 	 */
 	@Required
 	public void setNodes(Nodes nodes) {
 		this.nodes = nodes;
 	}
 
 	/** the PositionUpdateMsgs handler */
 	private PositionUpdateMsgs positionUpdateMsgs;
 
 	/**
 	 * @param positionUpdateMsgs
 	 *          the positionUpdateMsgs to set
 	 */
 	@Required
 	public void setPositions(PositionUpdateMsgs positionUpdateMsgs) {
 		this.positionUpdateMsgs = positionUpdateMsgs;
 	}
 
 	/** the ClusterLeaderMsgs handler */
 	private ClusterLeaderMsgs clusterLeaderMsgs;
 
 	/**
 	 * @param clusterLeaderMsgs
 	 *          the clusterLeaderMsgs to set
 	 */
 	@Required
 	public void setClusterLeaderMsgs(ClusterLeaderMsgs clusterLeaderMsgs) {
 		this.clusterLeaderMsgs = clusterLeaderMsgs;
 	}
 
 	private RelayServers relayServers;
 
 	/**
 	 * @param relayServers
 	 *          the relayServers to set
 	 */
 	@Required
 	public void setRelayServers(RelayServers relayServers) {
 		this.relayServers = relayServers;
 	}
 
 	private Senders senders;
 
 	/**
 	 * @param senders
 	 *          the senders to set
 	 */
 	@Required
 	public void setSenders(Senders senders) {
 		this.senders = senders;
 	}
 
 	private ReportOnce reportOnce;
 
 	/**
 	 * @param reportOnce
 	 *          the reportOnce to set
 	 */
 	@Required
 	public final void setReportOnce(ReportOnce reportOnce) {
 		this.reportOnce = reportOnce;
 	}
 
 	private TxChecker txChecker;
 
 	/**
 	 * @param txChecker
 	 *          the txChecker to set
 	 */
 	@Required
 	public final void setTxChecker(TxChecker txChecker) {
 		this.txChecker = txChecker;
 	}
 
 	/*
 	 * Main
 	 */
 
 	private static final String dotNodeTemplateSimple = "%n%s%s [shape=%s, style=filled, fillcolor=%s, label=\"%s\"%s]%n";
 
 	private static final String dotNodeTemplateFullIp = "%n%s%s [shape=box, margin=0, label=<%n"
 			+ "    <table border=\"0\" cellborder=\"1\" cellspacing=\"2\" cellpadding=\"4\">%n"
 			+ "      <tr><td bgcolor=\"%s\">%s</td></tr>%n" + "      <tr><td bgcolor=\"%s\">%s</td></tr>%n"
 			+ "    </table>>%s];%n";
 
 	private static final String dotNodeTemplateFull = "%n%s%s [shape=box, margin=0, label=<%n"
 			+ "    <table border=\"0\" cellborder=\"1\" cellspacing=\"2\" cellpadding=\"4\">%n"
 			+ "      <tr><td bgcolor=\"%s\">%s</td></tr>%n" + "      <tr><td bgcolor=\"%s\">%s</td></tr>%n"
 			+ "      <tr><td bgcolor=\"%s\">%s</td></tr>%n" + "    </table>>%s];%n";
 
 	private static final String shapeNormal = "ellipse";
 	private static final String shapeClusterLeader = "box";
 	private static final String colorSimpleOk = "white";
 	private static final String colorSimpleNotOk = "red";
 	private static final String colorFullOk = colorSimpleOk;
 	private static final String colorFullNotOk = colorSimpleNotOk;
 
 	protected static String getNodeNameOrIp(Node node) {
 		PositionUpdateMsg puMsg = node.getPositionUpdateMsg();
 		if (puMsg == null) {
 			/* use IP variant */
 			return node.getMainIp().getHostAddress().toString();
 		}
 
 		/* use named variant */
 		return puMsg.getPositionUpdateMsg().getPositionUpdateNodeId();
 	}
 
 	private static void writeDotNode(OutputStream gvoss, OutputStream gvos, Node node, String indent)
 			throws IllegalFormatException, FormatterClosedException, IOException {
 		StringBuilder sbSimple = new StringBuilder();
 		StringBuilder sbFull = new StringBuilder();
 		Formatter formatterSimple = new Formatter(sbSimple);
 		Formatter formatterFull = new Formatter(sbFull);
 
 		Sender sender = node.getSender();
 		String senderIP = (sender == null) ? "" : "" + sender.getIp().getHostAddress() + ":" + sender.getPort();
 		String senderColor = (sender == null) ? colorFullNotOk : colorFullOk;
 
 		Long nodeId = node.getId();
 		String nodeIP = node.getMainIp().getHostAddress().toString();
 
 		PositionUpdateMsg nodePU = node.getPositionUpdateMsg();
 		PositionUpdate nodePUMsg = (nodePU == null) ? null : nodePU.getPositionUpdateMsg();
 
 		String nodeSimpleColor = (nodePUMsg == null) ? colorSimpleNotOk : colorSimpleOk;
 		String nodeColor = (nodePUMsg == null) ? colorFullNotOk : colorFullOk;
 		String nodeShape = node.getClusterNodes().isEmpty() ? shapeNormal : shapeClusterLeader;
 
 		String nodeName = getNodeNameOrIp(node);
 
 		String url = (sender == null) ? "" : ", URL=\"http://" + sender.getIp().getHostAddress() + "\"";
 		formatterSimple.format(dotNodeTemplateSimple, indent, nodeId, nodeShape, nodeSimpleColor, nodeName, url);
 		if ((nodePUMsg == null) || (nodePUMsg.getPositionUpdateNodeIdType() == 4)
 				|| (nodePUMsg.getPositionUpdateNodeIdType() == 6)) {
 			/* use IP variant */
 			formatterFull.format(dotNodeTemplateFullIp, indent, nodeId, nodeColor, nodeName, senderColor, senderIP, url);
 		} else {
 			/* use named variant */
 			formatterFull.format(dotNodeTemplateFull, indent, nodeId, nodeColor, nodeName, colorFullOk, nodeIP, senderColor,
 					senderIP, url);
 		}
 
 		/* now write graph */
 		ClusterLeaderMsg nodeCL = node.getClusterLeaderMsg();
 		if (nodeCL != null) {
 			formatterSimple.format("%s%s -> %s%n", indent, nodeId, nodeCL.getClusterLeaderNode().getId());
 			formatterFull.format("%s%s -> %s%n", indent, nodeId, nodeCL.getClusterLeaderNode().getId());
 		}
 
 		gvoss.write(sbSimple.toString().getBytes(Constants.CHARSET_DEFAULT));
 		gvos.write(sbFull.toString().getBytes(Constants.CHARSET_DEFAULT));
 	}
 
	protected static class NodeNameComparatorOnIp implements Comparator<Node> {
 		@Override
 		public int compare(Node o1, Node o2) {
 			return o1.getMainIp().getHostAddress().toString().compareTo(o2.getMainIp().getHostAddress().toString());
 		}
 	}
 
 	private static void addNode2NameMap(Map<String, List<Node>> nodeName2Nodes, Node node) {
 		assert (nodeName2Nodes != null);
 		assert (node != null);
 
 		String nodeName = getNodeNameOrIp(node);
 		List<Node> mapping = nodeName2Nodes.get(nodeName);
 		if (mapping == null) {
 			mapping = new LinkedList<Node>();
 			mapping.add(node);
 			nodeName2Nodes.put(nodeName, mapping);
 		} else {
 			if (!mapping.contains(node)) {
 				mapping.add(node);
 			}
 		}
 	}
 
 	private void warnOnDuplicateNames(Map<String, List<Node>> nodeName2Nodes) {
 		if (nodeName2Nodes == null) {
 			return;
 		}
 
 		for (Map.Entry<String, List<Node>> entry : nodeName2Nodes.entrySet()) {
 			List<Node> mapping = entry.getValue();
 			if (mapping.size() <= 1) {
 				continue;
 			}
 
 			Collections.sort(mapping, new NodeNameComparatorOnIp());
 
 			boolean warn = false;
 			StringBuilder sb = new StringBuilder();
 			for (Node node : mapping) {
 				if (this.reportOnce.add(ReportSubject.DUPLICATE_NODE_NAME, entry.getKey(), node.getMainIp().getHostAddress()
 						.toString())) {
 					warn = true;
 					sb.append("  ");
 					sb.append(node.getMainIp().getHostAddress().toString());
 					Sender sender = node.getSender();
 					if (sender != null) {
 						sb.append(", received from ");
 						sb.append(sender.getIp().getHostAddress().toString());
 						sb.append(":");
 						sb.append(sender.getPort());
 					}
 					sb.append("\n");
 				}
 			}
 			if (warn) {
 				sb.insert(0, "\nDetected node(s) using existing name \"" + entry.getKey() + "\":\n");
 				this.logger.warn(sb.toString());
 			}
 		}
 	}
 
 	private void checkDuplicateNames(List<List<Node>> clusters) {
 		if (clusters == null) {
 			return;
 		}
 
 		this.logger.debug("Checking for duplicate node names");
 
 		try {
 			Map<String, List<Node>> nodeName2Nodes = new TreeMap<String, List<Node>>();
 
 			for (List<Node> cluster : clusters) {
 				for (Node clusterNode : cluster) {
 					addNode2NameMap(nodeName2Nodes, clusterNode);
 				}
 			}
 
 			warnOnDuplicateNames(nodeName2Nodes);
 		} catch (Exception e) {
 			this.logger.error("Error while checking for duplicate names", e);
 		}
 	}
 
 	private void generateDotAndSVG(List<List<Node>> clusters) throws IOException {
 		this.logger.debug("Creating SVG files");
 
 		Map<String, List<Node>> nodeName2Nodes = null;
 
 		this.dotSimpleFileOSChannel.position(0);
 		this.dotFullFileOSChannel.position(0);
 		String header = "digraph SmartGatewayMap {\n" + "  label=\"OLSR SmartGateway Clusters of " + new Date() + "\"\n"
 				+ "  labelloc=t\n" + "  labeljust=l\n";
 		this.dotSimpleFileOS.write(header.getBytes(Constants.CHARSET_DEFAULT));
 		this.dotFullFileOS.write(header.getBytes(Constants.CHARSET_DEFAULT));
 		try {
 			if (clusters != null) {
 				if (this.detectDuplicateNames) {
 					nodeName2Nodes = new TreeMap<String, List<Node>>();
 					this.logger.debug("  and also checking for duplicate node names");
 				}
 
 				int clusterIndex = 1;
 				for (List<Node> cluster : clusters) {
 					String s = ((clusterIndex == 1) ? "" : "\n") + "  subgraph cluster" + clusterIndex++ + " {\n"
 							+ "    label=\"\";\n" + "    style=filled;\n" + "    fillcolor=lightgrey;\n" + "    color=black;\n";
 					this.dotSimpleFileOS.write(s.getBytes(Constants.CHARSET_DEFAULT));
 					this.dotFullFileOS.write(s.getBytes(Constants.CHARSET_DEFAULT));
 					try {
 						for (Node node : cluster) {
 							writeDotNode(this.dotSimpleFileOS, this.dotFullFileOS, node, "    ");
 							if (this.detectDuplicateNames) {
 								addNode2NameMap(nodeName2Nodes, node);
 							}
 						}
 					} finally {
 						this.dotSimpleFileOS.write("  }\n".getBytes(Constants.CHARSET_DEFAULT));
 						this.dotFullFileOS.write("  }\n".getBytes(Constants.CHARSET_DEFAULT));
 					}
 				}
 			}
 		} catch (Exception e) {
 			this.logger.error("Error while generating the dot file", e);
 		} finally {
 			this.dotSimpleFileOS.write("}\n".getBytes(Constants.CHARSET_DEFAULT));
 			this.dotFullFileOS.write("}\n".getBytes(Constants.CHARSET_DEFAULT));
 		}
 
 		this.dotSimpleFileOS.flush();
 		this.dotFullFileOS.flush();
 		this.dotSimpleFileOSChannel.truncate(this.dotSimpleFileOSChannel.position());
 		this.dotFullFileOSChannel.truncate(this.dotFullFileOSChannel.position());
 
 		this.logger.debug("Generating SVG file");
 		Runtime.getRuntime().exec("fdp -Tsvg " + this.dotSimpleFile + " -o " + this.svgSimpleFile);
 		Runtime.getRuntime().exec("fdp -Tsvg " + this.dotFullFile + " -o " + this.svgFullFile);
 
 		warnOnDuplicateNames(nodeName2Nodes);
 	}
 
 	@Override
 	@Transactional(readOnly = true)
 	public void logit() throws IOException {
 		try {
 			this.txChecker.checkInTx("DatabaseLogger::logit");
 		} catch (Throwable e) {
 			e.printStackTrace();
 		}
 
 		this.databaseLogFileOSChannel.position(0);
 
 		this.logger.debug("Writing database logfile");
 
 		this.relayServers.print(this.databaseLogFileOS);
 		this.databaseLogFileOS.write(eol);
 		this.senders.print(this.databaseLogFileOS);
 		this.databaseLogFileOS.write(eol);
 		this.nodes.print(this.databaseLogFileOS);
 		this.databaseLogFileOS.write(eol);
 		this.positionUpdateMsgs.print(this.databaseLogFileOS);
 		this.databaseLogFileOS.write(eol);
 		this.clusterLeaderMsgs.print(this.databaseLogFileOS);
 
 		this.databaseLogFileOSChannel.truncate(this.databaseLogFileOSChannel.position());
 		this.databaseLogFileOS.flush();
 
 		if (this.generateSVG || this.detectDuplicateNames) {
 			List<List<Node>> clusters = this.nodes.getClusters(null);
 			if (this.generateSVG) {
 				generateDotAndSVG(clusters);
 			} else if (this.detectDuplicateNames) {
 				checkDuplicateNames(clusters);
 			}
 		}
 	}
 
 	private FileOutputStream databaseLogFileOS = null;
 	private FileChannel databaseLogFileOSChannel = null;
 	private FileOutputStream dotSimpleFileOS = null;
 	private FileChannel dotSimpleFileOSChannel = null;
 	private FileOutputStream dotFullFileOS = null;
 	private FileChannel dotFullFileOSChannel = null;
 	private static final byte[] eol;
 
 	static {
 		try {
 			eol = "\n".getBytes(Constants.CHARSET_DEFAULT);
 		} catch (UnsupportedEncodingException e) {
 			throw new ExceptionInInitializerError(e);
 		}
 	}
 
 	@Override
 	public void init() throws FileNotFoundException {
 		this.databaseLogFileOS = new FileOutputStream(this.databaseLogFile, false);
 		this.databaseLogFileOSChannel = this.databaseLogFileOS.getChannel();
 
 		if (this.generateSVG) {
 			this.dotSimpleFileOS = new FileOutputStream(this.dotSimpleFile, false);
 			this.dotSimpleFileOSChannel = this.dotSimpleFileOS.getChannel();
 			this.dotFullFileOS = new FileOutputStream(this.dotFullFile, false);
 			this.dotFullFileOSChannel = this.dotFullFileOS.getChannel();
 		}
 	}
 
 	public void uninit() {
 		if (this.databaseLogFileOSChannel != null) {
 			try {
 				this.databaseLogFileOSChannel.close();
 			} catch (IOException e) {
 				/* ignore */
 			}
 			this.databaseLogFileOSChannel = null;
 		}
 		if (this.dotSimpleFileOS != null) {
 			try {
 				this.dotSimpleFileOS.close();
 			} catch (IOException e) {
 				/* ignore */
 			}
 			this.dotSimpleFileOS = null;
 		}
 		if (this.dotFullFileOS != null) {
 			try {
 				this.dotFullFileOS.close();
 			} catch (IOException e) {
 				/* ignore */
 			}
 			this.dotFullFileOS = null;
 		}
 		if (this.databaseLogFileOS != null) {
 			try {
 				this.databaseLogFileOS.close();
 			} catch (IOException e) {
 				/* ignore */
 			}
 			this.databaseLogFileOS = null;
 		}
 	}
 
 	@Override
 	@Transactional(readOnly = true)
 	public void log(Logger log, Level level) {
 		try {
 			this.txChecker.checkInTx("DatabaseLogger::log");
 		} catch (Throwable e) {
 			e.printStackTrace();
 		}
 
 		try {
 			this.relayServers.log(log, level);
 			this.senders.log(log, level);
 			this.nodes.log(log, level);
 			this.positionUpdateMsgs.log(log, level);
 			this.clusterLeaderMsgs.log(log, level);
 		} catch (Throwable t) {
 			this.logger.error("Error while logging database", t);
 		}
 	}
 }
