 package edu.ualr.bittorrent.impl.core;
 
 import java.util.List;
 import java.util.Map;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Lists;
 import com.google.inject.Inject;
 
 import edu.ualr.bittorrent.interfaces.Metainfo;
 import edu.ualr.bittorrent.interfaces.Peer;
 import edu.ualr.bittorrent.interfaces.PeerProvider;
 
 /**
  * Default implementation of the {@link PeerProvider} interface.
  */
 public class PeerProviderImpl implements PeerProvider {
   private boolean alreadyCalled = false;
   private final Metainfo metainfo;
   private final Map<Integer, byte[]> pieces;
 
   /**
    * Create a new peer provider.
    *
    * @param metainfo
    * @param pieces
    */
   @Inject
   public PeerProviderImpl(Metainfo metainfo, Map<Integer, byte[]> pieces) {
     this.metainfo = Preconditions.checkNotNull(metainfo);
     this.pieces = Preconditions.checkNotNull(pieces);
   }
 
   /**
    * {@inheritDoc}
    */
   public ImmutableList<Peer> addPeers() {
     if (alreadyCalled) {
       return null;
     }
     alreadyCalled = true;
     List<Peer> peers = Lists.newArrayList();
    for (int i = 0; i < 10; i++) {
       Peer peer = new PeerImpl(pieces);
       peer.setMetainfo(metainfo);
       peers.add(peer);
     }
     for (int i = 0; i < 10; i++) {
       Peer peer = new PeerImpl();
       peer.setMetainfo(metainfo);
       peers.add(peer);
     }
     return ImmutableList.copyOf(peers);
   }
 
 }
