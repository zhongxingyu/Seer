 package edu.ualr.bittorrent.impl.core;
 
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.joda.time.Instant;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Lists;
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.sun.tools.javac.util.Pair;
 
 import edu.ualr.bittorrent.impl.core.messages.HandshakeImpl;
 import edu.ualr.bittorrent.impl.core.messages.MessagesModule;
 import edu.ualr.bittorrent.interfaces.Message;
 import edu.ualr.bittorrent.interfaces.Metainfo;
 import edu.ualr.bittorrent.interfaces.Peer;
 import edu.ualr.bittorrent.interfaces.PeerBrains;
 import edu.ualr.bittorrent.interfaces.PeerState;
 import edu.ualr.bittorrent.interfaces.PeerState.ChokeStatus;
 import edu.ualr.bittorrent.interfaces.PeerState.InterestLevel;
 import edu.ualr.bittorrent.interfaces.PeerState.PieceDeclaration;
 import edu.ualr.bittorrent.interfaces.PeerState.PieceRequest;
 import edu.ualr.bittorrent.interfaces.messages.CancelFactory;
 import edu.ualr.bittorrent.interfaces.messages.ChokeFactory;
 import edu.ualr.bittorrent.interfaces.messages.HandshakeFactory;
 import edu.ualr.bittorrent.interfaces.messages.HaveFactory;
 import edu.ualr.bittorrent.interfaces.messages.InterestedFactory;
 import edu.ualr.bittorrent.interfaces.messages.KeepAliveFactory;
 import edu.ualr.bittorrent.interfaces.messages.NotInterestedFactory;
 import edu.ualr.bittorrent.interfaces.messages.PieceFactory;
 import edu.ualr.bittorrent.interfaces.messages.RequestFactory;
 import edu.ualr.bittorrent.interfaces.messages.UnchokeFactory;
 
 /**
  * Default implementation of the decision making portion of a peer, the
  * {@link PeerBrains}.
  */
 public class PeerBrainsImpl implements PeerBrains {
   private Map<Peer, PeerState> activePeers;
   private Peer localPeer;
   private Metainfo metainfo;
   private Map<Integer, byte[]> data;
   private static final Integer UNCHOKED_PEER_LIMIT = 100;
   private final Injector injector;
 
   public PeerBrainsImpl() {
     this.injector = Guice.createInjector(new MessagesModule());
   }
 
   /**
    * {@inheritDoc}
    */
   public void setLocalPeer(Peer local) {
     this.localPeer = Preconditions.checkNotNull(local);
   }
 
   /**
    * {@inheritDoc}
    */
   public void setActivePeers(Map<Peer, PeerState> activePeers) {
     this.activePeers = Preconditions.checkNotNull(activePeers);
   }
 
   /**
    * {@inheritDoc}
    */
   public void setMetainfo(Metainfo metainfo) {
     this.metainfo = Preconditions.checkNotNull(metainfo);
   }
 
   /**
    * {@inheritDoc}
    */
   public void setData(Map<Integer, byte[]> data) {
     this.data = Preconditions.checkNotNull(data);
   }
 
   private PeerState returnStateIfAvailable(Peer p) {
     if (p.equals(localPeer)) {
       return null;
     }
 
     PeerState state = null;
     synchronized (activePeers) {
       if (activePeers.containsKey(p)) {
         state = activePeers.get(p);
       }
     }
 
     return state;
   }
 
   private boolean handleCommunicationInitalization(Peer p, PeerState state,
       List<Pair<Peer, Message>> messages) {
     /* If the local client hasn't sent a handshake to this peer yet, send one */
     if (sendHandshake(p, state, messages)) {
       return true;
     }
 
     /* If the remote peer hasn't sent a handshake yet, send them another */
     if (!remoteHasSentHandshake(p, state, messages)) {
       return true;
     }
 
     /*
      * If we haven't started communicating with this peer yet, then the choke
      * status will be null. If this is the case, go ahead and send a choke
      * message to make the initial choke state official.
      */
     if (sendInitialChoke(p, state, messages)) {
       return true;
     }
 
     return false;
   }
 
   private boolean askForSomethingFromPeer(Peer p, PeerState state,
       List<Pair<Peer, Message>> messages) {
     /*
      * Let peers that are choking and that have pieces that we want know that we
      * are interested
      */
     if (expressInterest(p, state, messages)) {
       return true;
     } else if (makeRequests(p, state, messages)) {
       return true;
     }
 
     return false;
   }
 
   private boolean respondToPeerRequests(Peer p, PeerState state,
       List<Pair<Peer, Message>> messages) {
     /* Unchoke some peers if there are any that seem worthy */
     if (unchoke(p, state, messages)) {
       return true;
     } else if (sendPieces(p, state, messages)
         || cancelPieceRequests(p, state, messages)) {
       return true;
     }
 
     return false;
   }
 
   /**
    * After communication between peers is initialized, they can actually start
    * sending meaningful messages back-and-fourth. This method sees if any
    * messages need to be sent, and if so, returns true. Otherwise, it returns
    * false.
    *
    * @param p
    * @param state
    * @param messages
    * @return
    */
   private boolean sendMeaningfuleMessageToPeer(Peer p, PeerState state,
       List<Pair<Peer, Message>> messages) {
     boolean messageSent = false;
     messageSent = askForSomethingFromPeer(p, state, messages);
     messageSent &= respondToPeerRequests(p, state, messages);
     return messageSent;
   }
 
   /**
    * {@inheritDoc}
    */
   public List<Pair<Peer, Message>> getMessagesToDispatch() {
     Preconditions.checkNotNull(localPeer);
     Preconditions.checkNotNull(activePeers);
     Preconditions.checkNotNull(metainfo);
     Preconditions.checkNotNull(data);
 
     List<Pair<Peer, Message>> messages = Lists.newArrayList();
 
     Set<Peer> peers;
 
     synchronized (activePeers) {
       peers = activePeers.keySet();
     }
 
     for (Peer p : peers) {
       PeerState state = returnStateIfAvailable(p);
       if (state == null) {
         continue;
       }
 
       if (handleCommunicationInitalization(p, state, messages)) {
         continue;
       }
 
       /* Let the remote peer know about any new pieces that we might have */
       if (letPeerKnowAboutNewPieces(p, state, messages)) {
         continue;
       }
 
       if (sendMeaningfuleMessageToPeer(p, state, messages)) {
         continue;
       }
 
       /* If no other messages are necessary, just send a keep alive */
       sendKeepAlive(p, state, messages);
     }
 
     return messages;
   }
 
   /**
    * Determine if a handshake message should be sent and if so, add it to the
    * message queue.
    *
    * @param remotePeer
    * @param state
    * @param messages
    * @return
    */
   private boolean sendHandshake(Peer remotePeer, PeerState state,
       List<Pair<Peer, Message>> messages) {
     Instant localSentHandshakeAt = null;
     synchronized (state) {
       localSentHandshakeAt = state.whenDidLocalSendHandshake();
     }
     if (localSentHandshakeAt == null) {
       messages.add(new Pair<Peer, Message>(remotePeer, injector.getInstance(
           HandshakeFactory.class).create(localPeer, remotePeer,
           HandshakeImpl.DEFAULT_PROTOCOL_IDENTIFIER, metainfo.getInfoHash(),
           HandshakeImpl.DEFAULT_RESERVED_BYTES)));
 
       return true;
     }
     return false;
   }
 
   /**
    * Determine if the remote has sent a handshake, if not, trying sending
    * another to the remote.
    *
    * @param remotePeer
    * @param state
    * @param messages
    * @return
    */
   private boolean remoteHasSentHandshake(Peer remotePeer, PeerState state,
       List<Pair<Peer, Message>> messages) {
     Instant remoteSentHandshakeAt = null;
     synchronized (state) {
       remoteSentHandshakeAt = state.whenDidRemoteSendHandshake();
     }
 
     if (remoteSentHandshakeAt == null) {
       // shake again just to be sure that the remote got ours
/*      messages.add(new Pair<Peer, Message>(remotePeer, injector.getInstance(
           HandshakeFactory.class).create(localPeer, remotePeer,
           HandshakeImpl.DEFAULT_PROTOCOL_IDENTIFIER, metainfo.getInfoHash(),
           HandshakeImpl.DEFAULT_RESERVED_BYTES)));
*/
       return false;
     }
     return true;
   }
 
   /**
    * Initially the remote should be choked according to the specification.
    *
    * @param remotePeer
    * @param state
    * @param messages
    * @return
    */
   private boolean sendInitialChoke(Peer remotePeer, PeerState state,
       List<Pair<Peer, Message>> messages) {
     Pair<ChokeStatus, Instant> choked = null;
 
     synchronized (state) {
       choked = state.isRemoteChoked();
     }
 
     if (choked == null) {
       messages.add(new Pair<Peer, Message>(remotePeer, injector.getInstance(
           ChokeFactory.class).create(localPeer, remotePeer)));
 
       return true;
     }
     return false;
   }
 
   /**
    * As the local peer collects pieces, it should let its neighbors know.
    *
    * @param remotePeer
    * @param state
    * @param messages
    * @return
    */
   private boolean letPeerKnowAboutNewPieces(Peer remotePeer, PeerState state,
       List<Pair<Peer, Message>> messages) {
 
     Set<Integer> downloadedPieces = null;
 
     synchronized (data) {
       downloadedPieces = data.keySet();
     }
 
     if (downloadedPieces == null) {
       return false;
     }
 
     List<PieceDeclaration> declaredPieces = null;
     synchronized (state) {
       declaredPieces = state.localHasPieces();
     }
 
     if (declaredPieces == null) {
       return false;
     }
 
     boolean pieceDeclared = false;
 
     for (Integer pieceIndex : downloadedPieces) {
       boolean declared = false;
       for (PieceDeclaration piece : declaredPieces) {
         if (pieceIndex.equals(piece.getPieceIndex())) {
           declared = true;
           break;
         }
       }
       if (!declared) {
         pieceDeclared = true;
         messages.add(new Pair<Peer, Message>(remotePeer, injector.getInstance(
             HaveFactory.class).create(localPeer, remotePeer, pieceIndex)));
       }
     }
 
     return pieceDeclared;
   }
 
   /**
    * If a neighbor has a piece that we are interested in and if we are choked,
    * then we should let the neighbor know that we'd like to request a piece of
    * data from it, hoping that we will soon be unchoked. If the we are choked
    * and our neighbor isn't interesting, let the know. Otherwise, don't send any
    * messages about our interest.
    *
    * @param remotePeer
    * @param state
    * @param messages
    * @return
    */
   private boolean expressInterest(Peer remotePeer, PeerState state,
       List<Pair<Peer, Message>> messages) {
 
     Set<Integer> downloadedPieces = null;
 
     synchronized (data) {
       downloadedPieces = data.keySet();
     }
 
     List<PieceDeclaration> remotePieces = null;
     Pair<ChokeStatus, Instant> choked = null;
 
     synchronized (state) {
       choked = state.isLocalChoked();
       remotePieces = state.remoteHasPieces();
     }
 
     if (choked != null && ChokeStatus.UNCHOKED.equals(choked.fst)) {
       return false; // if we are already unchoked, there is no reason to express
       // interest
     }
 
     if (remotePieces != null) {
       for (PieceDeclaration pieceDeclaration : remotePieces) {
         if (!downloadedPieces.contains(pieceDeclaration.getPieceIndex())) {
           messages.add(new Pair<Peer, Message>(remotePeer, injector
               .getInstance(InterestedFactory.class).create(localPeer,
                   remotePeer)));
           return true;
         }
       }
     }
 
     messages.add(new Pair<Peer, Message>(remotePeer, injector.getInstance(
         NotInterestedFactory.class).create(localPeer, remotePeer)));
 
     return true;
   }
 
   /**
    * Unchoke peers that are interested.
    *
    * @param remotePeer
    * @param state
    * @param messages
    * @return
    */
   private boolean unchoke(Peer remotePeer, PeerState state,
       List<Pair<Peer, Message>> messages) {
 
     Pair<ChokeStatus, Instant> choked = null;
     Pair<InterestLevel, Instant> interest = null;
 
     synchronized (state) {
       choked = state.isRemoteChoked();
       interest = state.getRemoteInterestLevelInLocal();
     }
 
     if (interest == null || InterestLevel.NOT_INTERESTED.equals(interest.fst)) {
       return false; // the peer has no desire to be unchoked
     }
 
     if (choked != null && ChokeStatus.UNCHOKED.equals(choked.fst)) {
       return false; // already unchoked
     }
 
     Set<Peer> peers = null;
 
     synchronized (activePeers) {
       peers = activePeers.keySet();
     }
 
     int unchokedPeerCount = 0;
     for (Peer peer : peers) {
       PeerState peerState = null;
       synchronized (activePeers) {
         peerState = activePeers.get(peer);
       }
       // TODO: add some element of optimistic unchoking here... now this is too
       // optimistic
       if (peerState.isRemoteChoked() != null
           && ChokeStatus.UNCHOKED.equals(peerState.isRemoteChoked().fst)) {
         unchokedPeerCount++;
       }
     }
 
     if (unchokedPeerCount < UNCHOKED_PEER_LIMIT) {
       messages.add(new Pair<Peer, Message>(remotePeer, injector.getInstance(
           UnchokeFactory.class).create(localPeer, remotePeer)));
     }
 
     return true;
   }
 
   /**
    * Make data requests to peers.
    *
    * @param remotePeer
    * @param state
    * @param messages
    * @return
    */
   private boolean makeRequests(Peer remotePeer, PeerState state,
       List<Pair<Peer, Message>> messages) {
     // TODO: be more intelligent about requests (by intelligent, i really be
     // true to spec)
 
     Pair<ChokeStatus, Instant> choked = null;
     List<PieceDeclaration> remotePieces = null;
 
     synchronized (state) {
       choked = state.isLocalChoked();
       remotePieces = state.remoteHasPieces();
     }
 
     if (choked == null || ChokeStatus.CHOKED.equals(choked.fst)) {
       return false; // bummer, choked
     }
 
     Set<Integer> downloadedPieces = null;
 
     synchronized (data) {
       downloadedPieces = data.keySet();
     }
 
     ImmutableList<PieceRequest> alreadyRequestedPieces = null;
     synchronized (state) {
       alreadyRequestedPieces = state.getLocalRequestedPieces();
     }
 
     for (PieceDeclaration piece : remotePieces) {
       if (!downloadedPieces.contains(piece.getPieceIndex())) {
         boolean okayToRequest = true;
         if (alreadyRequestedPieces != null) {
           for (PieceRequest request : alreadyRequestedPieces) {
             if (request.getPieceIndex().equals(piece.getPieceIndex())
                 && request.getRequestTime().isAfter(
                     new Instant().minus(100000L))) {
               okayToRequest = false;
               break;
             }
           }
         }
 
         if (okayToRequest) {
           messages.add(new Pair<Peer, Message>(remotePeer, injector
               .getInstance(RequestFactory.class).create(localPeer, remotePeer,
                   piece.getPieceIndex(), 0, metainfo.getPieceLength())));
         }
 
         return true;
       }
     }
 
     return false;
   }
 
   /**
    * Send pieces to peers.
    *
    * @param remotePeer
    * @param state
    * @param messages
    * @return
    */
   private boolean sendPieces(Peer remotePeer, PeerState state,
       List<Pair<Peer, Message>> messages) {
 
     Pair<ChokeStatus, Instant> choked = null;
     List<PieceRequest> requestedPieces = null;
 
     synchronized (state) {
       choked = state.isRemoteChoked();
       requestedPieces = state.getRemoteRequestedPieces();
     }
 
     if (requestedPieces == null || requestedPieces.size() == 0) {
       return false;
     }
 
     if (choked == null || ChokeStatus.CHOKED.equals(choked.fst)) {
       return false; // remote is choked
     }
 
     byte[] bytes = null;
     int requestedIndex = requestedPieces.get(0).getPieceIndex();
     int requestedOffset = requestedPieces.get(0).getBlockOffset();
 
     synchronized (data) {
       if (data.containsKey(requestedIndex)) {
         bytes = data.get(requestedIndex);
       }
     }
 
     if (bytes == null) {
       return false;
     }
 
     // TODO: make the bytes match the requested size
     messages.add(new Pair<Peer, Message>(remotePeer, injector.getInstance(
         PieceFactory.class).create(localPeer, remotePeer, requestedIndex,
         requestedOffset, bytes)));
 
     return true;
   }
 
   /**
    * Cancel any requests that have been made, but fulfilled by other peers.
    *
    * @param remotePeer
    * @param state
    * @param messages
    * @return
    */
   private boolean cancelPieceRequests(Peer remotePeer, PeerState state,
       List<Pair<Peer, Message>> messages) {
 
     Set<Integer> downloadedPieces;
     ImmutableList<PieceRequest> requestedPieces;
 
     synchronized (data) {
       downloadedPieces = data.keySet();
     }
 
     synchronized (state) {
       requestedPieces = state.getLocalRequestedPieces();
     }
 
     for (PieceRequest request : requestedPieces) {
       if (downloadedPieces.contains(request.getPieceIndex())) {
         messages.add(new Pair<Peer, Message>(remotePeer, injector.getInstance(
             CancelFactory.class).create(localPeer, remotePeer,
             request.getPieceIndex(), request.getBlockOffset(),
             request.getBlockSize())));
       }
     }
 
     return true;
   }
 
   /**
    * Let the peer know we are here.
    *
    * @param remotePeer
    * @param state
    * @param messages
    * @return
    */
   private boolean sendKeepAlive(Peer remotePeer, PeerState state,
       List<Pair<Peer, Message>> messages) {
     messages.add(new Pair<Peer, Message>(remotePeer, injector.getInstance(
         KeepAliveFactory.class).create(localPeer, remotePeer)));
     return true;
   }
 }
