 /*
  * File:         P2PNetworkGraph.java
  * Project:		 Spiderweb Network Graph Visualizer
  * Created:      01/06/2011
  * Last Changed: Date: 21/07/2011 
  * Author:       <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
  * 
  * This code was produced at Carleton University 2011
  */
 package spiderweb.graph;
 
 import java.util.Collection;
 import java.util.Set;
 import java.util.TreeSet;
 
 import edu.uci.ics.jung.graph.DelegateForest;
 import edu.uci.ics.jung.graph.DirectedSparseGraph;
 import edu.uci.ics.jung.graph.Forest;
 import edu.uci.ics.jung.graph.Graph;
 
 public class P2PNetworkGraph extends DirectedSparseGraph<P2PVertex, P2PConnection> implements Graph<P2PVertex, P2PConnection> {
 
 	private static final long serialVersionUID = -5569894854823217541L;
 
 	private int edgecounter = 0;
 
 	public P2PNetworkGraph() {
 		super();
 	}
 
 	protected void addPeer(int peernumber){
 		P2PVertex v1 = new PeerVertex(peernumber);
 		super.addVertex(v1);
 	}
 
 	protected void removePeer(int peernum){
 		P2PVertex peer = new PeerVertex(peernum);
 		Collection<P2PConnection> edgeset = getIncidentEdges(peer);
 		Set<PeerDocumentVertex> docsToRemove = new TreeSet<PeerDocumentVertex>();
 		//TODO use an iterator?
 		//TODO use removeDocument
 		for (P2PConnection e: edgeset){
 			P2PVertex node = getOpposite(peer, e); //node is what the edge connects the peer to  
 			if (node instanceof PeerDocumentVertex)//if that node is a document
 				docsToRemove.add((PeerDocumentVertex)node);
 		}
 
 		for (PeerDocumentVertex n :docsToRemove){
 			removeDocument(n.getDocumentNumber(),n.getPeerNumber());
 		}
 		removeVertex(peer);
 	}
 
 
 	protected void addDocument(int docnumber, int peer){
 		addDocument(docnumber, peer, new Integer(++edgecounter));
 		addPeerDocument(docnumber,peer,new Integer(++edgecounter),new Integer(++edgecounter));
 	}
 
 	protected void addPeerDocument(int docnumber, int peer, Integer peerEdgeKey, Integer docEdgeKey) {
 		P2PVertex pDocV = new PeerDocumentVertex(peer, docnumber);
 		addVertex(pDocV);
 
 		P2PVertex vpeer = new PeerVertex(peer);
 		P2PVertex vdoc = new DocumentVertex(docnumber);
 		addEdge(new P2PConnection(P2PConnection.P2PDOC,peerEdgeKey), vpeer, pDocV);
 		//System.out.println("addEdge: "+pDocV+" to "+vdoc);
 		addEdge(new P2PConnection(P2PConnection.DOC2PDOC,docEdgeKey), pDocV,vdoc);
 
 		//System.out.println("crash");
 	}
 
 	protected void addDocument(int docnumber, int peer, Integer edgeKey) {
 		P2PVertex vdoc = getDocument(docnumber);
 		
 		if(vdoc==null) {
 			vdoc = new DocumentVertex(docnumber);
 			addVertex(vdoc);
 		}
 		//create a vertex that we can compare with the ones in the graph to find the peer vertex
 		P2PVertex vpeer = getVertexInGraph(new PeerVertex(peer));
 		addEdge(new P2PConnection(P2PConnection.P2DOC,edgeKey), vpeer, vdoc);
 	}
 
 
 	protected void removeDocument(int docnumber, int peer) {
 		P2PVertex peerDoc = getVertexInGraph(new PeerDocumentVertex(peer, docnumber));
 		P2PVertex publisher = getVertexInGraph(new PeerVertex(peer));
 		P2PVertex document = getVertexInGraph(new DocumentVertex(docnumber));
 		P2PConnection peerToPeerDocEdge = findEdge(publisher,peerDoc);
 		P2PConnection peerToDocEdge = findEdge(publisher, document);
 		P2PConnection docToPeerDocEdge = findEdge(document,peerDoc);
 
 
 		removeEdge(peerToDocEdge);
 		removeEdge(peerToPeerDocEdge);
 		removeEdge(docToPeerDocEdge);
 		removeVertex(peerDoc);
 		if(document != null) {
 			if(getIncidentEdges(document).size() == 0) {
 				removeVertex(document);
 			}
 		}
 	}
 
 	protected void connectDocuments(int doc1, int doc2) {
 		connectDocuments(doc1, doc2, new Integer(++edgecounter));
 	}
 
 	protected void connectDocuments(int doc1, int doc2, Integer edgeKey) {
 		P2PVertex documentOne = getVertexInGraph(new DocumentVertex(doc1));
 		P2PVertex documentTwo = getVertexInGraph(new DocumentVertex(doc2));
 
 		addEdge(new P2PConnection(P2PConnection.DOC2DOC,edgeKey), documentOne, documentTwo);
 	}
 
 	protected void disconnectDocuments(int from, int to) {
 		P2PConnection edge = findDocumentToDocumentConnection(from, to);
 
 		super.removeEdge(edge);
 	}
 
 	protected void connectPeers(int from, int to) {
 		connectPeers(from,to,new Integer(++edgecounter));
 	}
 	/**
 	 * add an edge to the graph
 	 * @param from	peer 1 (vertex 1)
 	 * @param to	peer 2 (vertex 2)
 	 */
 	protected void connectPeers(int from, int to, Integer key) {
 		P2PConnection edge = new P2PConnection(P2PConnection.P2P,key);
 		if(!containsEdge(edge)) {
 			addEdge(edge, getVertexInGraph(new PeerVertex(from)), getVertexInGraph(new PeerVertex(to)));
 		}
 
 	}
 	protected void disconnectPeers(int from, int to) {
 		P2PConnection edge = findPeerConnection(from, to);
 
 		super.removeEdge(edge);
 	}
 
 	public P2PVertex getVertexInGraph(P2PVertex input){
 		for (P2PVertex v : vertices.keySet()){
 			if (v.equals(input))
 				return v;
 		}
 		return null;
 	}
 
 	public PeerVertex getPeer(int peerNumber) {
 		return (PeerVertex)getVertexInGraph(new PeerVertex(peerNumber));
 	}
 
 	public DocumentVertex getDocument(int documentNumber) {
 		return (DocumentVertex)getVertexInGraph(new DocumentVertex(documentNumber));
 	}
 
 	public PeerDocumentVertex getPeerDocument(int publisherNumber, int documentNumber) {
 		return (PeerDocumentVertex)getVertexInGraph(new PeerDocumentVertex(publisherNumber,documentNumber));
 	}
 
 	public boolean isPeerOnline(int peerNumber) {
 		return getPeer(peerNumber) != null;
 	}
 
 	public boolean isDocumentPublished(int documentNumber) {
 		return getDocument(documentNumber) != null;
 	}
 
 	public P2PConnection findPeerConnection(int peerFrom, int peerTo){
 		P2PConnection connection = findEdge(new PeerVertex(peerFrom), new PeerVertex(peerTo));
 		return connection;
 	}
 
 	public boolean arePeersConnected(int peer1, int peer2) {
 		return (findPeerConnection(peer1, peer2) != null) && (findPeerConnection(peer2, peer1) != null);
 	}
 
 	public P2PConnection findDocumentToDocumentConnection(int docFrom, int docTo){
 		P2PConnection connection = findEdge(new DocumentVertex(docFrom), new DocumentVertex(docTo));
 		return connection;
 	}
 
 	public boolean areDocumentsConnected(int docFrom, int docTo) {
 		return (findDocumentToDocumentConnection(docFrom, docTo) != null);
 	}
 
 	public P2PConnection findPeerToDocConnection(int peer, int docnumber){
 		P2PVertex p = getVertexInGraph(new PeerVertex(peer));
 		P2PVertex doc = getVertexInGraph(new DocumentVertex(docnumber));
 		P2PConnection connection = findEdge(p, doc);
 		return connection;
 	}
 
 	/**
 	 * Returns the edge that connects the vertex which the peer number peerFrom represents to the vertex which document number docnumber represents.
 	 * @param peerFrom	Peer that the edge emerges from (vertex 1)
 	 * @param docnumber	Document that the edge terminates at (vertex 2)
 	 * @return			The edge that connects the two peers.
 	 */
 	public P2PConnection findPeerToPeerDocConnection(int peer, int docnumber){
 		return findEdge(new PeerVertex(peer), new PeerDocumentVertex(peer, docnumber));
 	}
 
 	/**
 	 * Returns the edge that connects the vertex which the peer number peerFrom represents to the vertex which document number docnumber represents.
 	 * @param peerFrom	Peer that the edge emerges from (vertex 1)
 	 * @param docnumber	Document that the edge terminates at (vertex 2)
 	 * @return			The edge that connects the two peers.
 	 */
 	public P2PConnection findDocToPeerDocConnection(int peer, int docnumber){
 		return findEdge(new PeerDocumentVertex(peer, docnumber), new DocumentVertex(docnumber));
 	}
 
 	//override these methods so the underlying collection is not unmodifiable
 	@Override
 	public Collection<P2PConnection> getEdges()
 	{
 		return edges.keySet();
 	}
 
 	@Override
 	public Collection<P2PVertex> getVertices()
 	{
 		return vertices.keySet();
 	}
 
 	/**
 	 * Returns a tree graph of documents and the peers which host them.
 	 * @param graph	The source which the tree Graph will be made from
 	 * @return	The Document Tree Graph
 	 */
 	public static Forest<P2PVertex, P2PConnection> makeTreeGraph(P2PNetworkGraph graph) {
 		Forest<P2PVertex, P2PConnection> tree = new DelegateForest<P2PVertex, P2PConnection>();
 		for(P2PVertex documentVertex : graph.getVertices()) { //iterate over all vertices in the graph
 			if(documentVertex.getClass().equals(DocumentVertex.class)) { //a document represents the root of a tree in the graph
 				//DocumentVertex docVertex = new DocumentVertex(p2pV);
 				tree.addVertex(documentVertex);
 				for(P2PConnection edge : graph.getOutEdges(documentVertex)) { //get all the document's edges to find all the peers connected to it
 					P2PVertex opposite =  graph.getOpposite(documentVertex, edge);
 					if(opposite.getClass().equals(DocumentVertex.class)) {
 						tree.addEdge(edge, documentVertex, opposite);
 					}
 				}
 				for(P2PConnection edge : graph.getInEdges(documentVertex)) {
 					P2PVertex opposite =  graph.getOpposite(documentVertex, edge);
 					if(opposite.getClass().equals(PeerDocumentVertex.class)) {
 						tree.addEdge(edge, documentVertex, opposite);
 					}
 				}
 			}
 		}
 		return tree;
 	}
 
 	/**
 	 * Limited version of graphEvent for construction a graph for layout purposes
 	 * @param gev	The Log event which needs to be handled.
 	 */
 	public void graphConstructionEvent(LogEvent gev) {
 		if (gev.getType().equals("online")){
 			int peerNumber = gev.getParam(1);
 			//check to make sure peer is not online already
 			if(!isPeerOnline(peerNumber)) {
 				addPeer(peerNumber);
 			}
 		} else if(gev.getType().equals("connect")){
 			int peer1 = gev.getParam(1);
 			int peer2 = gev.getParam(2);
 			//check peers are online and that they are not already connected
 			//if peer is not online, come online
 			if(!isPeerOnline(peer1)) {
 				addPeer(peer1);
 			}
 			if(!isPeerOnline(peer2)) {
 				addPeer(peer2);
 			}
 			if(!arePeersConnected(peer1, peer2)) {
 				connectPeers(peer1, peer2);
 				connectPeers(peer2, peer1);
 			}
 		} else if(gev.getType().equals("publish")){
 			int peer = gev.getParam(1);
 			int document = gev.getParam(2);
 			//check peer is online, check peer has not already published document
 			//if peer is not online, come online
 			if(!isPeerOnline(peer)) {
 				addPeer(peer);
 			}
 			addDocument(document, peer);
 		} else if(gev.getType().equals("linkdocument")) {
 			int doc1 = gev.getParam(1);
 			int doc2 = gev.getParam(2);
 			//check documents are both published
 			if(isDocumentPublished(doc1) && isDocumentPublished(doc2)) {
 				connectDocuments(doc1, doc2);
 			}
 		} 
 	}
 
 	/**
 	 * Handles the Log Events which affect the structure of the graph.
 	 * @param gev				The Log event which needs to be handled.
 	 * @param forward			<code>true</code> if play-back is playing forward.
 	 * @param referenceGraph	The Graph to get edge numbers from.
 	 */
 	public void graphEvent(LogEvent gev, boolean forward, P2PNetworkGraph referenceGraph) {
 		if(forward) {
 			if (gev.getType().equals("online")){
 				addPeer(gev.getParam(1));
 			} else if (gev.getType().equals("offline")){
 				removePeer(gev.getParam(1));
 			} else if(gev.getType().equals("connect")){
 				connectPeers(gev.getParam(1), gev.getParam(2), referenceGraph.findPeerConnection(gev.getParam(1), gev.getParam(2)).getKey());
 				connectPeers(gev.getParam(2), gev.getParam(1), referenceGraph.findPeerConnection(gev.getParam(2), gev.getParam(1)).getKey());
 			} else if(gev.getType().equals("disconnect")){
 				disconnectPeers(gev.getParam(1), gev.getParam(2));
 				disconnectPeers(gev.getParam(2), gev.getParam(1));
 			} else if(gev.getType().equals("publish")){
 				addDocument(gev.getParam(2), gev.getParam(1), referenceGraph.findPeerToDocConnection(gev.getParam(1), gev.getParam(2)).getKey());
 				addPeerDocument(gev.getParam(2), gev.getParam(1), referenceGraph.findPeerToPeerDocConnection(gev.getParam(1), gev.getParam(2)).getKey(),
 						referenceGraph.findDocToPeerDocConnection(gev.getParam(1), gev.getParam(2)).getKey());
			} else if(gev.getType().equals("depublish")){
 				removeDocument(gev.getParam(2), gev.getParam(1));
 			} else if(gev.getType().equals("linkdocument")) {
 				connectDocuments(gev.getParam(1), gev.getParam(2),referenceGraph.findDocumentToDocumentConnection(gev.getParam(1), gev.getParam(2)).getKey());
 			} else if(gev.getType().equals("delinkdocument")) {
 				disconnectDocuments(gev.getParam(1), gev.getParam(2));
 			}
 		} else {
 			if (gev.getType().equals("online")){
 				removePeer(gev.getParam(1));
 			} else if (gev.getType().equals("offline")){
 				addPeer(gev.getParam(1));
 			} else if(gev.getType().equals("connect")){
 				disconnectPeers(gev.getParam(1), gev.getParam(2));
 				disconnectPeers(gev.getParam(2), gev.getParam(1));
 			} else if(gev.getType().equals("disconnect")){
 				connectPeers(gev.getParam(1), gev.getParam(2), referenceGraph.findPeerConnection(gev.getParam(1), gev.getParam(2)).getKey());
 				connectPeers(gev.getParam(2), gev.getParam(1), referenceGraph.findPeerConnection(gev.getParam(2), gev.getParam(1)).getKey());
 			} else if(gev.getType().equals("publish")){
 				removeDocument(gev.getParam(2), gev.getParam(1));
			} else if(gev.getType().equals("depublish")){
 				addDocument(gev.getParam(2), gev.getParam(1), referenceGraph.findPeerToDocConnection(gev.getParam(1), gev.getParam(2)).getKey());
 				addPeerDocument(gev.getParam(2), gev.getParam(1), referenceGraph.findPeerToPeerDocConnection(gev.getParam(1), gev.getParam(2)).getKey(),
 						referenceGraph.findDocToPeerDocConnection(gev.getParam(1), gev.getParam(2)).getKey());
 			} else if(gev.getType().equals("linkdocument")) {
 				disconnectDocuments(gev.getParam(1), gev.getParam(2));
 			} else if(gev.getType().equals("delinkdocument")) {
 				connectDocuments(gev.getParam(1), gev.getParam(2),referenceGraph.findDocumentToDocumentConnection(gev.getParam(1), gev.getParam(2)).getKey());
 			}
 		}
 	}
 	
 	/**
 	 * Handles the Log Events which affect the structure of the graph.
 	 * Primarily used for online as the edges are keyed similar to how edges are keyed when a graph is constructed.
 	 * @param gev				The Log event which needs to be handled.
 	 */
 	public void robustGraphEvent(LogEvent gev) {
 
 		if (gev.getType().equals("online")){
 			int peerNumber = gev.getParam(1);
 			//check to make sure peer is not online already
 			if(!isPeerOnline(peerNumber)) {
 				addPeer(peerNumber);
 			}
 		} else if (gev.getType().equals("offline")){
 			int peerNumber = gev.getParam(1);
 			//check to make sure peer is online
 			if(isPeerOnline(peerNumber)) {
 				removePeer(peerNumber);
 			}
 		} else if(gev.getType().equals("connect")){
 			int peer1 = gev.getParam(1);
 			int peer2 = gev.getParam(2);
 			//check peers are online and that they are not already connected
 			//if peer is not online, come online
 			if(!isPeerOnline(peer1)) {
 				addPeer(peer1);
 			}
 			if(!isPeerOnline(peer2)) {
 				addPeer(peer2);
 			}
 			if(!arePeersConnected(peer1, peer2)) {
 				connectPeers(peer1, peer2);
 				connectPeers(peer2, peer1);
 			}
 		} else if(gev.getType().equals("disconnect")){
 			int peer1 = gev.getParam(1);
 			int peer2 = gev.getParam(2);
 			//check peers are online and that they are already connected
 			//if peer is not online, come online
 			if(!isPeerOnline(peer1)) {
 				addPeer(peer1);
 			}
 			if(!isPeerOnline(peer2)) {
 				addPeer(peer2);
 			}
 			if(arePeersConnected(peer1, peer2)) {
 				disconnectPeers(peer1, peer2);
 				disconnectPeers(peer2, peer1);
 			}
 		} else if(gev.getType().equals("publish")){		
 			int peer = gev.getParam(1);
 			int document = gev.getParam(2);
 			//check peer is online, check peer has not already published document
 			//if peer is not online, come online
 			if(!isPeerOnline(peer)) {
 				addPeer(peer);
 			}
 			if(!isDocumentPublished(document)) {
 				addDocument(document, peer);
 			}
 		} else if(gev.getType().equals("remove")){
 			int peer = gev.getParam(1);
 			int document = gev.getParam(2);
 			//check peer is online, check peer has published document
 			//if peer is not online, come online
 			if(!isPeerOnline(peer)) {
 				addPeer(peer);
 			}
 			if(isDocumentPublished(document)) {
 				removeDocument(document, peer);
 			}
 		} else if(gev.getType().equals("linkdocument")) {
 			int doc1 = gev.getParam(1);
 			int doc2 = gev.getParam(2);
 			//check documents are both published
 			if(isDocumentPublished(doc1) && isDocumentPublished(doc2)) {
 				connectDocuments(doc1, doc2);
 			}
 		} else if(gev.getType().equals("delinkdocument")) {
 			int doc1 = gev.getParam(1);
 			int doc2 = gev.getParam(2);
 			//check documents are both published
 			if(isDocumentPublished(doc1) && isDocumentPublished(doc2)) {
 				disconnectDocuments(doc1, doc2);
 			}
 		}
 	}
 
 }
