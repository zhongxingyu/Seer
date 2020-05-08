 package pl.edu.pjwstk.p2pp.superpeer;
 
 import java.math.BigInteger;
 import java.security.NoSuchAlgorithmException;
 import java.util.*;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.apache.log4j.Logger;
 
 import pl.edu.pjwstk.p2pp.ErrorInterface;
 import pl.edu.pjwstk.p2pp.GlobalConstants;
 import pl.edu.pjwstk.p2pp.P2PPManager;
 import pl.edu.pjwstk.p2pp.P2PPNodeCallback;
 import pl.edu.pjwstk.p2pp.entities.P2PPEntity;
 import pl.edu.pjwstk.p2pp.entities.Peer;
 import pl.edu.pjwstk.p2pp.messages.Acknowledgment;
 import pl.edu.pjwstk.p2pp.messages.Indication;
 import pl.edu.pjwstk.p2pp.messages.Message;
 import pl.edu.pjwstk.p2pp.messages.P2PPMessage;
 import pl.edu.pjwstk.p2pp.messages.indications.LeaveIndication;
 import pl.edu.pjwstk.p2pp.messages.requests.*;
 import pl.edu.pjwstk.p2pp.messages.responses.*;
 import pl.edu.pjwstk.p2pp.objects.*;
 import pl.edu.pjwstk.p2pp.resources.MessageResourceObject;
 import pl.edu.pjwstk.p2pp.superpeer.messages.indications.IndexPeerIndication;
 import pl.edu.pjwstk.p2pp.superpeer.messages.requests.IndexRequest;
 import pl.edu.pjwstk.p2pp.superpeer.messages.requests.LookupIndexRequest;
 import pl.edu.pjwstk.p2pp.superpeer.messages.requests.LookupPeerIndexRequest;
 import pl.edu.pjwstk.p2pp.superpeer.messages.responses.IndexResponse;
 import pl.edu.pjwstk.p2pp.superpeer.messages.responses.LookupIndexResponse;
 import pl.edu.pjwstk.p2pp.superpeer.messages.responses.LookupPeerIndexResponse;
 import pl.edu.pjwstk.p2pp.transactions.Transaction;
 import pl.edu.pjwstk.p2pp.transactions.TransactionListener;
 import pl.edu.pjwstk.p2pp.transactions.TransactionTable;
 import pl.edu.pjwstk.p2pp.util.Arrays;
 import pl.edu.pjwstk.p2pp.util.ByteUtils;
 import pl.edu.pjwstk.p2pp.util.NodeTimers;
 import pl.edu.pjwstk.p2pp.util.P2PPUtils;
 
 
 public class SuperPeerPeer extends Peer {
 
     private static Logger LOG = org.apache.log4j.Logger.getLogger(SuperPeerPeer.class);
 
     /**
 	 * Default time (seconds) of expiration of resource objects.
 	 */
 	public static final int DEFAULT_EXPIRES_SECONDS = 120;
 
     private AddressInfo bootstrapAddress;
     private boolean iAmSuperPeer = false;
 
     private Date lastPeerLookup = new Date();
     private final Map<PeerID, PeerInfo> peerIndex = new ConcurrentHashMap<PeerID,PeerInfo>();
     private final Map<ResourceID,List<PeerInfo>> resourceIndex = new ConcurrentHashMap<ResourceID,List<PeerInfo>>();
     private final Map<PeerInfo,Collection<ResourceID>> reverseResourceIndex = new ConcurrentHashMap<PeerInfo,Collection<ResourceID>>();
     private Date lastSuperPeerLookup = new Date();
 
     private final TransactionListener joinTransactionListener = new TransactionListener() {
 
         public void transactionEnded(byte[] transactionID, byte transactionState, byte TransactionType, Request request, Response response,
                                      TransactionTable transactionTable, P2PPEntity node) {
             try {
 
                 if (transactionState == Transaction.TERMINATED_STATE) {
 
                     if (response instanceof JoinResponse) {
 
                         int responseCode = response.getResponseCodeAsInt();
                         if (responseCode == Response.RESPONSE_CODE_OK) {
 
                             state = JOINED_NODE_STATE;
                             callback.joinCallback();
 
                         }
 
 
                     } else if (response instanceof NextHopResponse) {
                         if (LOG.isDebugEnabled()) LOG.debug("Received next hop response for lookup object");
                         NextHopResponse nextHopResponse = (NextHopResponse) response;
                         PeerInfo nextHop = nextHopResponse.getPeerInfo();
                         transactionTable.createTransactionAndFill(request, joinTransactionListener, nextHop.getAddressInfos(),
                                 sharedManager.getPeerIDAsBytes(), nextHop.getPeerID().getPeerIDBytes());
 
                     }
                 } // if transaction ended with an error
                 else {
                     ErrorInterface error = new ErrorInterface() {
                         private Object value;
                         public Object getValue() {return value;}
                         public void setValue(Object value) {this.value = value;}
                     };
                     error.setValue((JoinRequest) request);
                     callback.errorCallback(error, P2PPNodeCallback.JOIN_ERROR_CODE);
                 }
 
             } catch (Throwable e) {
                 StringBuilder strb = new StringBuilder("Error while processing transaction in lookupIndexTransactionListener transactionID=");
                 strb.append(ByteUtils.byteArrayToHexString(transactionID));
                 strb.append(" transactionState=").append(transactionState).append(" request=").append(request).append(" response=").append(response);
                 strb.append(" transactionTable=").append(transactionTable).append(" nodeState=").append(node.getState());
                 LOG.error(strb.toString(), e);
             }
         }
 
     };
 
     private final TransactionListener lookupIndexTransactionListener = new TransactionListener() {
 
 		public void transactionEnded(byte[] transactionID, byte transactionState, byte TransactionType, Request request, Response response,
                                      TransactionTable transactionTable, P2PPEntity node) {
             try {
 
                 if (transactionState == Transaction.TERMINATED_STATE) {
 
                     if (response instanceof LookupIndexResponse) {
                         LookupIndexRequest lookupIndexRequest = (LookupIndexRequest) request;
                         LookupIndexResponse lookupIndexResponse = (LookupIndexResponse) response;
 
                         int responseCode = response.getResponseCodeAsInt();
                         if (responseCode == Response.RESPONSE_CODE_OK) {
 
                             byte[] ownPeerID = sharedManager.getPeerIDAsBytes();
                             LookupObjectRequest lookupObjectRequest = new LookupObjectRequest(P2PPMessage.P2PP_PROTOCOL_VERSION_1, false, true,
                             false, (byte) 255, null, ownPeerID, GlobalConstants.isOverReliable, false, null, sharedManager.getPeerInfo(true, true),
                             lookupIndexRequest.getResourceLookup());
 
                             PeerInfo peerInfo = lookupIndexResponse.getPeerInfo();
                             transactionTable.createTransactionAndFill(lookupObjectRequest, lookupObjectTransactionListener, peerInfo.getAddressInfos(),
                                     ownPeerID, peerInfo.getPeerID().getPeerIDBytes());
 
                         } else if (responseCode == Response.RESPONSE_CODE_NOT_FOUND) {
 
                             callback.onDeliverRequest(request, new ArrayList<ResourceObject>());
 
                         }
                     } else if (response instanceof NextHopResponse) {
                         if (LOG.isDebugEnabled()) LOG.debug("Received next hop response for lookup index");
                         NextHopResponse nextHopResponse = (NextHopResponse) response;
                         PeerInfo nextHop = nextHopResponse.getPeerInfo();
                         transactionTable.createTransactionAndFill(request, lookupIndexTransactionListener, nextHop.getAddressInfos(),
                                 sharedManager.getPeerIDAsBytes(), nextHop.getPeerID().getPeerIDBytes());
 
                     }
                 } // if transaction ended with an error
                 else {
                     ErrorInterface error = new ErrorInterface() {
                         private Object value;
                         public Object getValue() {return value;}
                         public void setValue(Object value) {this.value = value;}
                     };
                     error.setValue(((LookupIndexRequest) request).getResourceLookup());
                     callback.errorCallback(error, P2PPNodeCallback.RESOURCE_LOOKUP_ERROR_CODE);
                 }
 
             } catch (Throwable e) {
                 StringBuilder strb = new StringBuilder("Error while processing transaction in lookupIndexTransactionListener transactionID=");
                 strb.append(ByteUtils.byteArrayToHexString(transactionID));
                 strb.append(" transactionState=").append(transactionState).append(" request=").append(request).append(" response=").append(response);
                 strb.append(" transactionTable=").append(transactionTable).append(" nodeState=").append(node.getState());
                 LOG.error(strb.toString(), e);
             }
 		}
 	};
 
     private final TransactionListener lookupObjectTransactionListener = new TransactionListener() {
 
 		public void transactionEnded(byte[] transactionID, byte transactionState, byte TransactionType, Request request, Response response,
                                      TransactionTable transactionTable, P2PPEntity node) {
             try {
 
                 if (transactionState == Transaction.TERMINATED_STATE) {
 
                     if (response instanceof LookupObjectResponse) {
                         LookupObjectResponse lookupResponse = (LookupObjectResponse) response;
 
                         int responseCode = response.getResponseCodeAsInt();
                         if (responseCode == Response.RESPONSE_CODE_OK) {
                             Vector<ResourceObject> foundResources = lookupResponse.getResourceObject();
 
                             if (foundResources.size() > 0 && foundResources.get(0) != null) {
                                 callback.lookupCallback(foundResources);
                             }
                         }  else if (responseCode == Response.RESPONSE_CODE_NOT_FOUND) {
 
                             ErrorInterface error = new ErrorInterface() {
                                 private Object value;
                                 public Object getValue() {return value;}
                                 public void setValue(Object value) {this.value = value;}
                             };
                             error.setValue(((LookupObjectRequest) request).getResourceLookup());
 
                             callback.errorCallback(error, P2PPNodeCallback.RESOURCE_LOOKUP_ERROR_CODE);
                         }
                     } else if (response instanceof NextHopResponse) {
                         if (LOG.isDebugEnabled()) LOG.debug("Received next hop response for lookup object");
                         NextHopResponse nextHopResponse = (NextHopResponse) response;
                         PeerInfo nextHop = nextHopResponse.getPeerInfo();
                         transactionTable.createTransactionAndFill(request, lookupObjectTransactionListener, nextHop.getAddressInfos(),
                                 sharedManager.getPeerIDAsBytes(), nextHop.getPeerID().getPeerIDBytes());
 
                     }
                 } // if transaction ended with an error
                 else {
                     ErrorInterface error = new ErrorInterface() {
                         private Object value;
                         public Object getValue() {return value;}
                         public void setValue(Object value) {this.value = value;}
                     };
                     error.setValue(((LookupObjectRequest) request).getResourceLookup());
                     callback.errorCallback(error, P2PPNodeCallback.RESOURCE_LOOKUP_ERROR_CODE);
                 }
 
             } catch (Throwable e) {
                 StringBuilder strb = new StringBuilder("Error while processing transaction in lookupIndexTransactionListener transactionID=");
                 strb.append(ByteUtils.byteArrayToHexString(transactionID));
                 strb.append(" transactionState=").append(transactionState).append(" request=").append(request).append(" response=").append(response);
                 strb.append(" transactionTable=").append(transactionTable).append(" nodeState=").append(node.getState());
                 LOG.error(strb.toString(), e);
             }
 		}
 
 	};
 
     private final TransactionListener removeObjectTransactionListener = new TransactionListener() {
 
 		public void transactionEnded(byte[] transactionID, byte transactionState, byte TransactionType, Request request, Response response,
                                      TransactionTable transactionTable, P2PPEntity node) {
 
 			if (transactionState == Transaction.TERMINATED_STATE) {
 
 				if (response instanceof RemoveObjectResponse) {
                     RemoveObjectRequest removeRequest = (RemoveObjectRequest) request;
                     ResourceObject removedResource = removeRequest.getResourceObject();
 
 					int responseCode = response.getResponseCodeAsInt();
 					if (responseCode == Response.RESPONSE_CODE_OK) {
 
                         callback.removeCallback(removedResource);
 
 					}  else if (responseCode == Response.RESPONSE_CODE_NOT_FOUND) {
 
 						ErrorInterface error = new ErrorInterface() {
 							private Object value;
 							public Object getValue() {return value;}
 							public void setValue(Object value) {this.value = value;}
 						};
 						error.setValue(removedResource);
 						callback.errorCallback(error, P2PPNodeCallback.RESOURCE_LOOKUP_ERROR_CODE);
 
 					}
 				} else if (response instanceof NextHopResponse) {
 					if (LOG.isDebugEnabled()) LOG.debug("Received next hop response for remove object");
 					NextHopResponse nextHopResponse = (NextHopResponse) response;
 					PeerInfo nextHop = nextHopResponse.getPeerInfo();
 					transactionTable.createTransactionAndFill(request, removeObjectTransactionListener, nextHop.getAddressInfos(),
                             sharedManager.getPeerIDAsBytes(), nextHop.getPeerID().getPeerIDBytes());
 
 				}
 			} // if transaction ended with an error
 			else {
 				ErrorInterface error = new ErrorInterface() {
 					private Object value;
 					public Object getValue() {return value;}
 					public void setValue(Object value) {this.value = value;}
 				};
 				error.setValue(((LookupObjectRequest) request).getResourceLookup());
 				callback.errorCallback(error, P2PPNodeCallback.RESOURCE_LOOKUP_ERROR_CODE);
 			}
 
 		}
 
 	};
 
     private final TransactionListener indexRequestTransactionListener = new TransactionListener() {
 
 		public void transactionEnded(byte[] transactionID, byte transactionState, byte TransactionType, Request request, Response response,
                                      TransactionTable transactionTable, P2PPEntity node) {
 
 			if (transactionState == Transaction.TERMINATED_STATE) {
 
 				if (response instanceof IndexResponse) {
                     IndexRequest indexRequest = (IndexRequest) request;
                     ResourceObject publishedResource = indexRequest.getResourceObject();
 
                     callback.publishCallback(publishedResource.getContentType(), publishedResource.getContentSubtype(), publishedResource.getUnhashedID(),
                             publishedResource.getValue().getValue());
 
 
 				} else if (response instanceof NextHopResponse) {
 					if (LOG.isDebugEnabled()) LOG.debug("Received next hop response for lookup object");
 					NextHopResponse nextHopResponse = (NextHopResponse) response;
 					PeerInfo nextHop = nextHopResponse.getPeerInfo();
 					transactionTable.createTransactionAndFill(request, indexRequestTransactionListener, nextHop.getAddressInfos(),
                             sharedManager.getPeerIDAsBytes(), nextHop.getPeerID().getPeerIDBytes());
 
 				}
 			} // if transaction ended with an error
 			else {
 				ErrorInterface error = new ErrorInterface() {
 					private Object value;
 					public Object getValue() {return value;}
 					public void setValue(Object value) {this.value = value;}
 				};
 				error.setValue(((IndexRequest) request).getResourceObject());
 				callback.errorCallback(error, P2PPNodeCallback.RESOURCE_LOOKUP_ERROR_CODE);
 			}
 
 		}
 
 	};
 
 
     public SuperPeerPeer() {
         routingTable = new SuperPeerRoutingTable();
         neighborTable = new SuperPeerNeighbourTable();
 
         resourceManager.setRoutingTable(routingTable);
         resourceManager.setNeighborTable(neighborTable);
     }
 
     private void addPeerToIndex(PeerID peerID, PeerInfo peerInfo) {
         if (LOG.isDebugEnabled()) {
             LOG.debug("Adding peerID " + peerID + " to peerIndex with " + peerInfo);
         }
         this.peerIndex.put(peerID, peerInfo);
     }
 
     private void addResourceToIndex(final ResourceID resourceID, PeerInfo peerInfo) {
 
         if (LOG.isDebugEnabled()) {
             LOG.debug("Adding resource " + resourceID + " to resourceIndex with " + peerInfo);
         }
 
         synchronized (this.resourceIndex) {
             List<PeerInfo> resourcePublishers;
             if (this.resourceIndex.containsKey(resourceID)) {
                 resourcePublishers = this.resourceIndex.get(resourceID);
                 if (!resourcePublishers.contains(peerInfo)) {
                     resourcePublishers.add(peerInfo);
                 }
             } else {
                 resourcePublishers = new Vector<PeerInfo>();
                 resourcePublishers.add(peerInfo);
                 this.resourceIndex.put(resourceID, resourcePublishers);
             }
         }
 
         Collection<ResourceID> resourceIDs = this.reverseResourceIndex.get(peerInfo);
         if (resourceIDs != null && !resourceIDs.contains(resourceID)) {
             resourceIDs.add(resourceID);
         } else {
             if (LOG.isDebugEnabled()) LOG.debug("Adding peerInfo to reverseResourceIndex " + peerInfo);
             this.reverseResourceIndex.put(peerInfo, new ArrayList<ResourceID>(){{add(resourceID);}});
             //LOG.debug(this.reverseResourceIndex);
         }
 
     }
 
     @Override
     public Byte getRoutingAlgorithm() {
         return P2PPUtils.SUPERPEER_P2P_ALGORITHM;
     }
 
     @Override
     public PeerInfo getClosestTo(byte[] id) {
         return null;
     }
 
     @Override
     public BigInteger getDistance(String unhashedKey1, String unhashedKey2) throws IllegalStateException {
         return BigInteger.ZERO;
     }
 
     @Override
     public BigInteger getDistanceBetweenHashed(byte[] hashedKey1, byte[] hashedKey2) throws IllegalStateException {
         return BigInteger.ZERO;
     }
 
     @Override
     public String getOverlayAlgorithm() {
         return "SuperPeer-SHA1-128";
     }
 
     @Override
     public String[] getRoutingTableToString() {
         return new String[0];
     }
 
     @Override
     public void join(byte[] overlayID, String overlayPeerAddress, int overlayPeerPort) {
         // TODO probably should be done in a thread-safe way... now this method is invoked in another thread, and
         // creates the transaction... shouldn't work this way...
 
         if (LOG.isDebugEnabled()) {
             LOG.debug("Method invoked with " + "overlayAddress=" + overlayPeerAddress + ":" + overlayPeerPort + " in " + state + " state.");
         }
 
         if (state == INITIAL_NODE_STATE) {
 
             /*byte[] ownNodeID = null;
             byte[] unhashedID = sharedManager.getUnhashedID();
             try {
                 ownNodeID = P2PPUtils.hash(unhashedID, sharedManager.getOptions().getHashAlgorithm());
             } catch (NoSuchAlgorithmException e) {
                 LOG.error("Error while generating hashed id from " + new String(unhashedID), e);
             }
 
             sharedManager.setPeerIDAsBytes(ownNodeID);*/
 
             // creates bootstrap request
             BootstrapRequest message = new BootstrapRequest(P2PPManager.CURRENT_PROTOCOL_VERSION, false, true, false,
                     (byte) 255, null, GlobalConstants.isOverReliable, GlobalConstants.isEncrypted, sharedManager.getPeerInfo(true, true));
 
             // creates AddressInfo object and adds it to a vector of addresses of bootstrap server
             bootstrapAddress = new AddressInfo((byte) 0, false, AddressInfo.IP_V4, (byte) 0, (byte) 0, 0,
                     (GlobalConstants.isOverReliable) ? AddressInfo.TCP_TRANSPORT_TYPE : AddressInfo.UDP_TRANSPORT_TYPE, AddressInfo.HOST_ADDRESS_TYPE,
                     overlayPeerPort, ByteUtils.stringIPAddressToBytes(overlayPeerAddress));
             Vector<AddressInfo> bootstrapAddressInfos = new Vector<AddressInfo>();
             bootstrapAddressInfos.add(bootstrapAddress);
 
             if (LOG.isDebugEnabled()) LOG.debug("Joining begins. Bootstrap server at " + overlayPeerAddress + ":" + overlayPeerPort);
             // creates transaction for bootstrap request TODO Decide who will be the listener of this transaction.
             transactionTable.createTransactionAndFill(message, bootstrapTransactionListener, bootstrapAddressInfos,
                     new byte[4], new byte[4]);
             state = BOOTSTRAPPING_NODE_STATE;
             if (LOG.isDebugEnabled()) LOG.debug("state changed to BOOTSTRAPPING");
 
         } else {
 
             LOG.error("Can't connect to an overlay because communication with one started already.");
             callback.errorCallback(null, P2PPNodeCallback.BOOTSTRAP_ERROR_CODE);
 
         }
 
     }
     /**
      * TransactionListener for bootstrapping.
      */
     private TransactionListener bootstrapTransactionListener = new TransactionListener() {
 
         public void transactionEnded(byte[] transactionID, byte transactionState, byte TransactionType,
                 Request request, Response response, TransactionTable transactionTable, P2PPEntity node) {
 
             if (transactionState == Transaction.TERMINATED_STATE) {
 
                 BootstrapResponse bootstrapResponse = (BootstrapResponse) response;
 
                 int responseCode = response.getResponseCodeAsInt();
                 if (responseCode == Response.RESPONSE_CODE_OK) {
 
                     // saves information about bootstrap candidates
                     bootstrapCandidates = bootstrapResponse.getPeersInOverlay();
                     if (bootstrapCandidates.size() == 0) {
                         iAmSuperPeer = true;
                     }
                     for (PeerInfo peerInfo : bootstrapCandidates) {
                         routingTable.addPeerInfo(peerInfo);
                     }
 
                     // gets PeerInfo describing originator of BootstrapRequest (it's this peer.. BootstrapServer filled this object)
                     PeerInfo originatorPeerInfo = bootstrapResponse.getOriginatorPeerInfo();
 
                     // saves new peerID
                     byte[] ownNodeID = originatorPeerInfo.getPeerID().getPeerIDBytes();
                     if (LOG.isDebugEnabled()) {
                         LOG.debug("Bootstrap response received. PeerID=" + ByteUtils.byteArrayToHexString(ownNodeID));
                     }
                     sharedManager.setPeerIDAsBytes(ownNodeID);
 
                     // saves p2p options from bootstrap response
                     P2POptions options = bootstrapResponse.getP2POptions();
                     sharedManager.setOptions(options);
 
                     // Creates Owner object that will be used for creating ResourceObjects.
                     Owner owner = new Owner(ownNodeID);
 
                     // informs objects about overlay options
                     resourceManager.setProperties(options.getHashAlgorithm(), options.getHashAlgorithmLength(), owner, DEFAULT_EXPIRES_SECONDS);
                     routingTable.setProperties(options.getBase(), options.getHashAlgorithmLength() * 8);
                     routingTable.setLocalPeerID(ownNodeID);
 
                     byte[] ownPeerID = sharedManager.getPeerIDAsBytes();
                     PeerInfo nextHop = routingTable.getClosestTo(ownPeerID);
                     if (nextHop != null) {
 
                         if (LOG.isDebugEnabled()) LOG.info("Indexing peer at the super peer");
 
                         PeerInfo ownPeerInfo = sharedManager.getPeerInfo(true, true);
 
                         JoinRequest joinRequest = new JoinRequest(P2PPMessage.P2PP_PROTOCOL_VERSION_1, true, false, (byte) 255, null, ownPeerID,
                                 GlobalConstants.isOverReliable, false, null, ownPeerInfo);
                         transactionTable.createTransactionAndFill(joinRequest, joinTransactionListener, nextHop.getAddressInfos(), ownPeerID,
                                 nextHop.getPeerID().getPeerIDBytes());
 
                     } else { // Super-peer doesn't have to join.
 
                         state = JOINED_NODE_STATE;
                         callback.joinCallback();
 
                     }
 
                 } else if (responseCode == Response.RESPONSE_CODE_REQUEST_REJECTED) {
 
                     LOG.warn("Could not bootstrap because there's already a node with a given id in the overlay.");
 
                 }
 
             } else if (transactionState == Transaction.FAILURE_STATE) { // TODO Do we need this?
 
                 // checks if there are more bootstrap candidates
                 if (currentBootstrapCandidateIndex < bootstrapCandidates.size()) {
 
                     try {
                         P2POptions options = sharedManager.getOptions();
 
                         if (LOG.isDebugEnabled()) {
                             LOG.debug("Communication with bootstrap candidate nr" + currentBootstrapCandidateIndex
                                 + " has failed. Communication with another one starts.");
                         }
 
                         // asks another bootstrap candidate for STUN server
                         ResourceID stunResourceID = new ResourceID(P2PPUtils.hash(P2PPUtils.STUN_SERVICE_ID, options.getHashAlgorithm()));
                         RLookup resourceLookup = new RLookup(P2PPUtils.STUN_CONTENT_TYPE, (byte) 0, stunResourceID,
                                 null);
                         LookupObjectRequest lookupRequest = new LookupObjectRequest(P2PPMessage.P2PP_PROTOCOL_VERSION_1, false, true, false, (byte) 255,
                                 null, sharedManager.getPeerIDAsBytes(), GlobalConstants.isOverReliable, false, null,
                                 sharedManager.getPeerInfo(true, true), resourceLookup);
                         PeerInfo chosenBootstrapCandidate = bootstrapCandidates.get(0);
                         Vector<AddressInfo> candidateAddresses = chosenBootstrapCandidate.getAddressInfos();
                         // TODO maybe inform someone about a fact that we are bootstrapped
                         transactionTable.createTransactionAndFill(lookupRequest, transactionListener,
                                 candidateAddresses, sharedManager.getPeerIDAsBytes(), chosenBootstrapCandidate.getPeerID().getPeerIDBytes());
                     } catch (NoSuchAlgorithmException e) {
                         // TODO probably can't happen
                         LOG.error("No algorithm!");
                     }
 
                 } // if there are no more bootstrap candidates
                 /*else {
                     if (LOG.isDebugEnabled()) LOG.debug("No response from any of bootstrap peers, so peer is now JOINED.");
 
                     BootstrapResponse bootstrapResponse = (BootstrapResponse) response;
                     PeerInfo originatorPeerInfo = bootstrapResponse.getOriginatorPeerInfo();
                     byte[] ownNodeID = originatorPeerInfo.getPeerID().getPeerIDBytes();
 
                     sharedManager.setPeerIDAsBytes(ownNodeID);
                     P2POptions options = bootstrapResponse.getP2POptions();
                     sharedManager.setOptions(options);
 
                     Owner owner = new Owner(ownNodeID);
 
                     resourceManager.setProperties(options.getHashAlgorithm(), options.getHashAlgorithmLength(), owner, DEFAULT_EXPIRES_SECONDS);
                     routingTable.setProperties(options.getBase(), options.getHashAlgorithmLength() * 8);
                     routingTable.setLocalPeerID(ownNodeID);
 
                     /*
                     // creates UserInfoResource and resource objects for services
                     UserInfoResourceObject userInfo = new UserInfoResourceObject(new Owner(ownNodeID), new Expires(120), null,
                     new Certificate(false, new byte[] { 2, 2, 2, 2 }), ownPeerInfo .getAddressInfos());
                     userInfo.setValue(new ResourceObjectValue(ownPeerInfo.asBytes()));
                     publish(userInfo.getUnhashedID(), userInfo);
 
                     // gets resource objects for local services
                     List<ResourceObject> listOfServiceResources = resourceManager.getResourceObjectsForServices(ownPeerInfo);
 
                     logger.debug("Publishes information about " + listOfServiceResources.size() + " services.");
 
                     // publishes objects describing services
                     for (ResourceObject current : listOfServiceResources) {
                     publish(current.getUnhashedID(), current);
                     }*/
                 //}
 
                 callback.errorCallback(null, P2PPNodeCallback.BOOTSTRAP_ERROR_CODE);
             }
         }
     };
 
     @Override
     public void leave() {
         try {
             byte[] ownPeerID = this.sharedManager.getPeerIDAsBytes();
             PeerInfo nextHop = this.routingTable.getClosestTo(ownPeerID);
             if (nextHop != null) {
                 LeaveIndication leaveIndication = new LeaveIndication(P2PPMessage.P2PP_PROTOCOL_VERSION_1, true, false, (byte) 255, null,
                         ownPeerID, GlobalConstants.isOverReliable, false, sharedManager.getPeerInfo(true, true), resourceManager.getAllResourceObjects());
                 this.transactionTable.createTransactionAndFill(leaveIndication, transactionListener, nextHop.getAddressInfos(), ownPeerID,
                         nextHop.getPeerID().getPeerIDBytes());
             }
             this.state = INITIAL_NODE_STATE;
             performLeaveTasks();
             this.callback.leaveCallback();
         } catch (Throwable e) {
             LOG.error("Error while leaving", e);
         }
     }
 
     @Override
 	public void lookup(byte contentType, byte contentSubtype, byte[] resourceIDBytes, Owner owner) {
 		try {
 			if (state == JOINED_NODE_STATE) {
 
 				byte[] hashedResourceID = P2PPUtils.hash(resourceIDBytes, sharedManager.getOptions().getHashAlgorithm());
                 ResourceID resourceID = new ResourceID(hashedResourceID);
 				RLookup resourceLookup = new RLookup(contentType, contentSubtype, resourceID, owner);
 				byte[] ownPeerID = sharedManager.getPeerIDAsBytes();
 				LookupIndexRequest request = new LookupIndexRequest(P2PPMessage.P2PP_PROTOCOL_VERSION_1, false, true, false, (byte) 255, null,
                         ownPeerID, GlobalConstants.isOverReliable, false, null, sharedManager.getPeerInfo(true, true), resourceLookup);
 
                 List<ResourceObject> resources = resourceManager.getResourceObject(resourceLookup);
                 if (resources == null) {
                    PeerInfo superPeerPI = this.bootstrapCandidates.get(0);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Resource " + ByteUtils.byteArrayToHexString(resourceLookup.getResourceID().getResourceID()) +
                                " not found. Looking up in index of " + superPeerPI);
                    }
                     if (!this.iAmSuperPeer) {
                         transactionTable.createTransactionAndFill(request, lookupIndexTransactionListener, superPeerPI.getAddressInfos(), ownPeerID,
                             superPeerPI.getPeerID().getPeerIDBytes());
                     } else {
                         onLookupIndexLocal(request);
                     }
                 } else {
                     if (LOG.isDebugEnabled()) {
                         LOG.debug("Resource " + resourceLookup.getResourceID().getResourceID().toString() + " found. Calling onDeliverRequest callback");
                     }
                     callback.onDeliverRequest(request, resources);
                 }
 
             } else {
 				if (LOG.isDebugEnabled()) LOG.debug("You can make lookups only if the peer has joined an overlay.");
 			}
 		} catch (NoSuchAlgorithmException e) {
 			LOG.error("Not known hash algorithm.");
 		}
 
 	}
 
     @Override
     public int onRequest(Request request, boolean isReceived) {
         int returnCode = 0;
 
         if (isReceived) {
             if (transactionTable.wasAlreadyConsumed(request)) {
                 // TODO is this the good code?
                 return 0;
             }
         }
 
         switch (state) {
             case INITIAL_NODE_STATE:
                 // probably ignore
                 break;
             case BOOTSTRAPPING_NODE_STATE:
                 // probably ignore
                 break;
             case SEARCHING_FOR_STUN_SERVER_NODE_STATE:
                 // probably ignore
                 break;
             case STUN_SERVER_COMMUNICATION_NODE_STATE:
                 // probably ignore
                 break;
             case JOIN_REQUEST_SENT_NODE_STATE:
                 // probably ignore
                 break;
             case JOINED_NODE_STATE:
                 if (request instanceof IndexRequest) {
                     onIndexRequest((IndexRequest) request);
                 } else if (request instanceof LookupIndexRequest) {
                     onLookupIndexRequest((LookupIndexRequest) request);
                 } else if (request instanceof LookupObjectRequest) {
                     onLookupObjectRequest((LookupObjectRequest) request);
                 } else if (request instanceof RemoveObjectRequest) {
                     onRemoveObjectRequest((RemoveObjectRequest) request);
                 } else if (request instanceof LookupPeerIndexRequest) {
                     onLookupPeerIndexRequest((LookupPeerIndexRequest) request);
                 } else if (request instanceof SendMessageRequest) {
                     onSendMessageRequest((SendMessageRequest) request);
                 } else if (request instanceof LookupPeerRequest) {
                     onLookupPeerRequest((LookupPeerRequest) request);
                 } else if (request instanceof JoinRequest) {
                     onJoinRequest((JoinRequest) request);
                 } else {
                     if (LOG.isDebugEnabled()) {
                         LOG.debug("Peer received " + request.getClass().getName() + " in JOINED state. It can't handle it now.");
                     }
                 }
                 break;
         }
 
 
 
         return returnCode;
     }
 
     @Override
     public void publish(byte[] unhashedID, final ResourceObject resourceObject) {
 
         if (LOG.isDebugEnabled()) {
             LOG.debug("Publishing id=\"" + new String(unhashedID) + "\" resource=" + resourceObject);
         }
 
         try {
 			if (state == JOINED_NODE_STATE) {
 				byte[] hashedResourceID = P2PPUtils.hash(unhashedID, sharedManager.getOptions().getHashAlgorithm());
 				resourceObject.setResourceID(new ResourceID(hashedResourceID));
 				resourceObject.setUnhashedID(unhashedID);
 				resourceObject.setOwner(new Owner(sharedManager.getPeerIDAsBytes()));
 
 				//resourceManager.addSelfPublishedResourceObject(resourceObject);
                 resourceManager.storeResourceObject(resourceObject);
 
                 PeerInfo ownPeerInfo = sharedManager.getPeerInfo(true, true);
                 IndexRequest indexRequest = new IndexRequest(P2PPMessage.P2PP_PROTOCOL_VERSION_1, false, true, false, (byte) 255, null,
                         sharedManager.getPeerIDAsBytes(), GlobalConstants.isOverReliable, false, null, ownPeerInfo, resourceObject);
                PeerInfo superPeerPI = this.bootstrapCandidates.get(0);
                 if (!this.iAmSuperPeer) {
                     transactionTable.createTransactionAndFill(indexRequest, indexRequestTransactionListener, superPeerPI.getAddressInfos(),
                         ownPeerInfo.getPeerID().getPeerIDBytes(), superPeerPI.getPeerID().getPeerIDBytes());
                 } else {
                     addResourceToIndex(resourceObject.getResourceID(), ownPeerInfo);
                     callback.onDeliverRequest(indexRequest, new ArrayList<ResourceObject>(){{add(resourceObject);}});
                 }
 
 			} else {
 				LOG.debug("You can publish only if the peer has joined an overlay.");
 				// TODO probably onError callback
 			}
 		} catch (Throwable e) {
 			LOG.error("Error while publishing resource", e);
 		}
     }
 
     @Override
     public void query() {
         // TODO Auto-generated method stub
     }
 
     @Override
     public void remove(byte contentType, byte contentSubtype, byte[] resourceIDBytes, Owner owner) {
 
         try {
 			if (state == JOINED_NODE_STATE) {
 
 				byte[] hashedResourceID = P2PPUtils.hash(resourceIDBytes, sharedManager.getOptions().getHashAlgorithm());
                 ResourceID resourceID = new ResourceID(hashedResourceID);
 				RLookup resourceLookup = new RLookup(contentType, contentSubtype, resourceID, owner);
                 PeerInfo ownPeerInfo = sharedManager.getPeerInfo(true, true);
                 byte[] ownPeerID = ownPeerInfo.getPeerID().getPeerIDBytes();
                 if (owner == null) {
                     owner = new Owner(ownPeerID);
                 }
 
 				/*LookupIndexRequest request = new LookupIndexRequest(P2PPMessage.P2PP_PROTOCOL_VERSION_1, false, true, false, (byte) 255, null,
                         ownPeerID, false, false, null, sharedManager.getPeerInfo(true, true), resourceLookup);*/
 
                 List<ResourceObject> resources = resourceManager.getResourceObject(resourceLookup);
                 /*if (resources == null) {
                     PeerInfo nextHop = routingTable.getNextHop(resourceLookup.getResourceID().getResourceID());
                     if (logger.isDebugEnabled()) {
                         logger.debug("Resource " + resourceLookup.getResourceID() + " not found. Looking up in index of " + nextHop);
                     }
                     if (nextHop != null) {
                         transactionTable.createTransactionAndFill(request, lookupIndexTransactionListener, nextHop.getAddressInfos(), ownPeerID,
                             nextHop.getPeerID().getPeerIDBytes());
                     } else {
                         ResourceObject removedResource = this.resourceManager.removeResourceObject(contentType, contentSubtype,resourceID, owner);
                         if (removedResource != null) {
                             if (logger.isDebugEnabled()) {
                                 logger.debug("Resource " + resourceLookup.getResourceID() + " found. Calling removeCallback");
                             }
                             callback.removeCallback(resources);
                         }
                     }
                 } else {*/
                 if (resources != null) {
                     if (LOG.isDebugEnabled()) {
                         LOG.debug("Resource " + resourceLookup.getResourceID() + " found. Calling removeCallback");
                     }
 
                     ResourceObject removedResource = this.resourceManager.removeResourceObject(contentType, contentSubtype,resourceID, owner);
                    PeerInfo superPeerPI = this.bootstrapCandidates.get(0);
 
                     if (!this.iAmSuperPeer) {
 
                         RemoveObjectRequest removeRequest = new RemoveObjectRequest(P2PPMessage.P2PP_PROTOCOL_VERSION_1, false, true, false,
                                 (byte) 255, null, sharedManager.getPeerIDAsBytes(), GlobalConstants.isOverReliable, false, null,
                                 sharedManager.getPeerInfo(true, true), removedResource);
                         transactionTable.createTransactionAndFill(removeRequest, removeObjectTransactionListener, superPeerPI.getAddressInfos(),
                                 ownPeerID, superPeerPI.getPeerID().getPeerIDBytes());
 
                     } else {
 
                         ResourceID removedRID = removedResource.getResourceID();
 
                         if (this.resourceIndex.containsKey(removedRID)) {
                             List<PeerInfo> resourceKeepers = this.resourceIndex.get(removedRID);
                             if (resourceKeepers != null && resourceKeepers.size() > 0) {
                                 resourceKeepers.remove(ownPeerInfo);
                             }
                         }
 
                         this.reverseResourceIndex.get(ownPeerInfo).remove(removedRID);
 
                         callback.removeCallback(removedResource);
 
                     }
 
                 } else {
                     if (LOG.isDebugEnabled()) {
                         LOG.debug("Peer doesn't own resource " + resourceLookup.getResourceID() + " or it doesn't exist");
                     }
                 }
 
             } else {
 				if (LOG.isDebugEnabled()) LOG.debug("You can make lookups only if the peer has joined an overlay.");
 			}
 		} catch (NoSuchAlgorithmException e) {
 			LOG.error("Not known hash algorithm.");
 		}
 
     }
 
     public void sendMessage(final byte[] unhashedID, final String protocol, final byte[] message) {
 
         byte [] hashedID = null;
 
         try {
             hashedID = P2PPUtils.hash(unhashedID, sharedManager.getOptions().getHashAlgorithm());
             //LOG.warn("--- unhashed ID: "+java.util.Arrays.toString(unhashedID)+", hashed to: "+java.util.Arrays.toString(hashedID)+", with hashAlgorithm: "+sharedManager.getOptions().getHashAlgorithm());
         } catch (NoSuchAlgorithmException e) {
             LOG.error("Error while generating hashed id from " + new String(unhashedID), e);
         }
 
         PeerID peerID = new PeerID(hashedID);
 
         if (LOG.isDebugEnabled()) {
             LOG.debug("Sending message protocol=" + protocol + " messageLength=" + message.length + " to peerID=" + peerID);
         }
 
         PeerInfo ownPeerInfo = sharedManager.getPeerInfo(true, true);
 
         final MessageResourceObject messageResourceObject = new MessageResourceObject();
         messageResourceObject.setMessageValue(message);
         messageResourceObject.setOwner(new Owner(peerID.getPeerIDBytes()));
         messageResourceObject.setResourceID(new ResourceID(peerID.getPeerIDBytes()));
         messageResourceObject.setUnhashedID(peerID.getPeerIDBytes());
 
         if (ownPeerInfo.getPeerID().equals(peerID)) {
 
             if (LOG.isTraceEnabled()) {
                 LOG.trace("Passing message protocol=" + protocol + " messageLength=" + message.length + " directly to callback (own id)");
             }
 
             SendMessageRequest sendMessageRequest = new SendMessageRequest(P2PPMessage.P2PP_PROTOCOL_VERSION_1, false, true, false,
                     (byte) 255, null, sharedManager.getPeerIDAsBytes(), GlobalConstants.isOverReliable, false, null, ownPeerInfo, ownPeerInfo.getPeerID(),
                     messageResourceObject, protocol);
             callback.onDeliverRequest(sendMessageRequest, new ArrayList<ResourceObject>(){{add(messageResourceObject);}});
 
             return;
         }
 
         PeerInfo superPeer = routingTable.getNextHop(unhashedID);
         if (superPeer != null) {
 
             if (LOG.isTraceEnabled()) {
                 LOG.trace("Sending message protocol=" + protocol + " messageLength=" + message.length + " directly to the super peer");
             }
 
             // TODO Special case for SuperPeer of SuperPeer in peerIndex ?
             if (superPeer.getPeerID().equals(peerID)) {
 
                 SendMessageRequest sendMessageRequest = new SendMessageRequest(P2PPMessage.P2PP_PROTOCOL_VERSION_1, false, true, false,
                         (byte) 255, null, sharedManager.getPeerIDAsBytes(), GlobalConstants.isOverReliable, false, null, ownPeerInfo,
                         superPeer.getPeerID(), messageResourceObject, protocol);
                 transactionTable.createTransactionAndFill(sendMessageRequest, transactionListener,
                         superPeer.getAddressInfos(), ownPeerInfo.getPeerID().getPeerIDBytes(),
                         superPeer.getPeerID().getPeerIDBytes());
 
             } else {
 
                 if (LOG.isTraceEnabled()) {
                     StringBuilder strb = new StringBuilder("Looking up peer id=");
                     strb.append(peerID).append(" before sending message protocol= ").append(protocol).append(" messageLength=").append(message.length);
                     LOG.trace(strb.toString());
                 }
 
                 LookupPeerIndexRequest request = new LookupPeerIndexRequest(P2PPMessage.P2PP_PROTOCOL_VERSION_1, false, true, false, (byte) 255, null,
                         sharedManager.getPeerIDAsBytes(), GlobalConstants.isOverReliable, false, null, ownPeerInfo, peerID);
 
                 TransactionListener lookupPeerTransactionListener = new TransactionListener() {
 
                     public void transactionEnded(byte[] transactionID, byte transactionState, byte TransactionType, Request request, Response response,
                                                  TransactionTable transactionTable, P2PPEntity node) {
 
                         if (transactionState == Transaction.TERMINATED_STATE) {
 
                             if (response instanceof LookupPeerIndexResponse) {
                                 LookupPeerIndexRequest lpiRequest = (LookupPeerIndexRequest) request;
                                 PeerID peerID = lpiRequest.getPeerID();
 
                                 int responseCode = response.getResponseCodeAsInt();
                                 if (responseCode == Response.RESPONSE_CODE_OK) {
 
                                     LookupPeerIndexResponse lpiResponse = (LookupPeerIndexResponse) response;
                                     PeerInfo lookedUpPeerInfo = lpiResponse.getPeerInfo();
                                     PeerInfo ownPeerInfo = sharedManager.getPeerInfo(true, true);
 
                                     if (LOG.isTraceEnabled()) {
                                         StringBuilder strb = new StringBuilder("Sending message protocol=");
                                         strb.append(protocol).append(" messageLength=").append(message.length).append(" to ").append(lookedUpPeerInfo);
                                         LOG.trace(strb.toString());
                                     }
 
                                     SendMessageRequest sendMessageRequest = new SendMessageRequest(P2PPMessage.P2PP_PROTOCOL_VERSION_1, false, true, false,
                                             (byte) 255, null, sharedManager.getPeerIDAsBytes(), GlobalConstants.isOverReliable, false, null, ownPeerInfo,
                                             lookedUpPeerInfo.getPeerID(), messageResourceObject, protocol);
                                     transactionTable.createTransactionAndFill(sendMessageRequest, transactionListener,
                                             lookedUpPeerInfo.getAddressInfos(), ownPeerInfo.getPeerID().getPeerIDBytes(),
                                             lookedUpPeerInfo.getPeerID().getPeerIDBytes());
 
                                 } else if (responseCode == Response.RESPONSE_CODE_NOT_FOUND) {
                                     ErrorInterface error = new ErrorInterface() {
                                         private Object value;
                                         public Object getValue() {return value;}
                                         public void setValue(Object value) {this.value = value;}
                                     };
                                     error.setValue(peerID);
                                     callback.errorCallback(error, P2PPNodeCallback.USER_LOOKUP_ERROR_CODE);
                                 }
                             } else if (response instanceof NextHopResponse) {
                                 if (LOG.isDebugEnabled()) LOG.debug("Received next hop response for lookup index request");
                                 NextHopResponse nextHopResponse = (NextHopResponse) response;
                                 PeerInfo nextHop = nextHopResponse.getPeerInfo();
                                 transactionTable.createTransactionAndFill(request, this, nextHop.getAddressInfos(),
                                         sharedManager.getPeerIDAsBytes(), nextHop.getPeerID().getPeerIDBytes());
 
                             }
                         } // if transaction ended with an error
                         else {
                             ErrorInterface error = new ErrorInterface() {
                                 private Object value;
                                 public Object getValue() { return value; }
                                 public void setValue(Object value) { this.value = value; }
                             };
                             error.setValue(((LookupObjectRequest) request).getResourceLookup());
                             callback.errorCallback(error, P2PPNodeCallback.USER_LOOKUP_ERROR_CODE);
                             
                         }
 
                     }
 
                 };
 
                 transactionTable.createTransactionAndFill(request, lookupPeerTransactionListener, superPeer.getAddressInfos(),
                         sharedManager.getPeerIDAsBytes(), superPeer.getPeerID().getPeerIDBytes());
 
             }
 
         } else { // I am a SuperPeer
 
             PeerInfo lookedUpPeerInfo = this.peerIndex.get(peerID); // And I have an index I can lookup given peerID in
             if (lookedUpPeerInfo != null) {
 
                 if (LOG.isTraceEnabled()) {
                     StringBuilder strb = new StringBuilder("Sending message protocol=");
                     strb.append(protocol).append(" messageLength=").append(message.length).append(" to ").append(lookedUpPeerInfo);
                     LOG.trace(strb.toString());
                 }
 
                 SendMessageRequest sendMessageRequest = new SendMessageRequest(P2PPMessage.P2PP_PROTOCOL_VERSION_1, false, true, false,
                         (byte) 255, null, sharedManager.getPeerIDAsBytes(), GlobalConstants.isOverReliable, false, null, ownPeerInfo,
                         ownPeerInfo.getPeerID(), messageResourceObject, protocol);
                 transactionTable.createTransactionAndFill(sendMessageRequest, transactionListener, lookedUpPeerInfo.getAddressInfos(),
                         ownPeerInfo.getPeerID().getPeerIDBytes(), lookedUpPeerInfo.getPeerID().getPeerIDBytes());
 
             } else {
                     ErrorInterface error = new ErrorInterface() {
                         private Object value;
                         public Object getValue() {return value;}
                         public void setValue(Object value) {this.value = value;}
                     };
                     error.setValue(peerID);
                     callback.errorCallback(error, P2PPNodeCallback.USER_LOOKUP_ERROR_CODE);
                     LOG.warn("PeerID: "+peerID+", PeerIndex values: "+this.peerIndex.toString());
             }
 
         }
 
     }
 
     @Override
     protected boolean canConsume(Message receivedMessage) {
         boolean canConsume = false;
         if (receivedMessage instanceof P2PPMessage) {
             P2PPMessage p2ppMessage = (P2PPMessage) receivedMessage;
             byte requestType = p2ppMessage.getRequestOrResponseType();
             boolean[] messageType = p2ppMessage.getMessageType();
             // TODO more handling for indications
             // handling for non-acknowledgment messages
             if (!p2ppMessage.isAcknowledgment()) {
                 if (Arrays.equals(messageType, P2PPMessage.REQUEST_MESSAGE_TYPE)) {
                     if (requestType == P2PPMessage.JOIN_MESSAGE_TYPE
                             || requestType == P2PPMessage.LEAVE_MESSAGE_TYPE
                             || requestType == P2PPMessage.KEEP_ALIVE_MESSAGE_TYPE
                             || requestType == P2PPMessage.LOOKUP_PEER_MESSAGE_TYPE
                             || requestType == P2PPMessage.EXCHANGE_TABLE_MESSAGE_TYPE
                             || requestType == P2PPMessage.QUERY_MESSAGE_TYPE
                             || requestType == P2PPMessage.PUBLISH_MESSAGE_TYPE
                             || requestType == P2PPMessage.LOOKUP_OBJECT_MESSAGE_TYPE
                             || requestType == P2PPMessage.REMOVE_OBJECT_MESSAGE_TYPE
                             || requestType == P2PPMessage.REPLICATE_MESSAGE_TYPE
                             || requestType == P2PPMessage.TRANSFER_MESSAGE_TYPE
                             || requestType == P2PPMessage.TUNNEL_MESSAGE_TYPE
                             || requestType == P2PPMessage.CONNECT_MESSAGE_TYPE
                             || requestType == P2PPMessage.GET_DIAGNOSTICS_MESSAGE_TYPE
                             || requestType == P2PPMessage.INDEX_MESSAGE_TYPE
                             || requestType == P2PPMessage.LOOKUP_INDEX_MESSAGE_TYPE
                             || requestType == P2PPMessage.LOOKUP_PEER_INDEX_MESSAGE_TYPE
                             || requestType == P2PPMessage.SEND_MESSAGE_MESSAGE_TYPE
                             || requestType == P2PPMessage.JOIN_MESSAGE_TYPE) {
                         canConsume = true;
                     }
                 } else if (Arrays.equals(messageType, P2PPMessage.RESPONSE_MESSAGE_TYPE)
                         || Arrays.equals(messageType, P2PPMessage.RESPONSE_ACK_MESSAGE_TYPE)) {
                     if (requestType == P2PPMessage.ENROLL_MESSAGE_TYPE
                             || requestType == P2PPMessage.AUTHENTICATE_MESSAGE_TYPE
                             || requestType == P2PPMessage.BOOTSTRAP_MESSAGE_TYPE
                             || requestType == P2PPMessage.JOIN_MESSAGE_TYPE
                             || requestType == P2PPMessage.LEAVE_MESSAGE_TYPE
                             || requestType == P2PPMessage.KEEP_ALIVE_MESSAGE_TYPE
                             || requestType == P2PPMessage.LOOKUP_PEER_MESSAGE_TYPE
                             || requestType == P2PPMessage.EXCHANGE_TABLE_MESSAGE_TYPE
                             || requestType == P2PPMessage.QUERY_MESSAGE_TYPE
                             || requestType == P2PPMessage.PUBLISH_MESSAGE_TYPE
                             || requestType == P2PPMessage.LOOKUP_OBJECT_MESSAGE_TYPE
                             || requestType == P2PPMessage.REMOVE_OBJECT_MESSAGE_TYPE
                             || requestType == P2PPMessage.REPLICATE_MESSAGE_TYPE
                             || requestType == P2PPMessage.TRANSFER_MESSAGE_TYPE
                             || requestType == P2PPMessage.TUNNEL_MESSAGE_TYPE
                             || requestType == P2PPMessage.CONNECT_MESSAGE_TYPE
                             || requestType == P2PPMessage.INDEX_MESSAGE_TYPE
                             || requestType == P2PPMessage.LOOKUP_INDEX_MESSAGE_TYPE
                             || requestType == P2PPMessage.LOOKUP_PEER_INDEX_MESSAGE_TYPE
                             || requestType == P2PPMessage.SEND_MESSAGE_MESSAGE_TYPE
                             || requestType == P2PPMessage.JOIN_MESSAGE_TYPE) {
                         canConsume = true;
                     }
                 } else if (Arrays.equals(messageType, P2PPMessage.INDICATION_MESSAGE_TYPE)) {
                     if (requestType == P2PPMessage.LEAVE_MESSAGE_TYPE
                             || requestType == P2PPMessage.INDEX_PEER_MESSAGE_TYPE) {
                         canConsume = true;
                     }
                 }
             } // end of if isn't Ack
             // if is ACK
             else {
                 if (Arrays.equals(messageType, P2PPMessage.REQUEST_MESSAGE_TYPE)) {
                     if (// TODO what about ACK for bootstrap response? probably only responseACK is generated, so
                         // there's no need for consuming received ACK for bootstrap response
                             requestType == P2PPMessage.JOIN_MESSAGE_TYPE
                                     || requestType == P2PPMessage.LEAVE_MESSAGE_TYPE
                                     || requestType == P2PPMessage.KEEP_ALIVE_MESSAGE_TYPE
                                     || requestType == P2PPMessage.LOOKUP_PEER_MESSAGE_TYPE
                                     || requestType == P2PPMessage.EXCHANGE_TABLE_MESSAGE_TYPE
                                     || requestType == P2PPMessage.QUERY_MESSAGE_TYPE
                                     || requestType == P2PPMessage.PUBLISH_MESSAGE_TYPE
                                     || requestType == P2PPMessage.LOOKUP_OBJECT_MESSAGE_TYPE
                                     || requestType == P2PPMessage.REMOVE_OBJECT_MESSAGE_TYPE
                                     || requestType == P2PPMessage.REPLICATE_MESSAGE_TYPE
                                     || requestType == P2PPMessage.TRANSFER_MESSAGE_TYPE
                                     || requestType == P2PPMessage.TUNNEL_MESSAGE_TYPE
                                     || requestType == P2PPMessage.CONNECT_MESSAGE_TYPE
                                     || requestType == P2PPMessage.GET_DIAGNOSTICS_MESSAGE_TYPE
                                     || requestType == P2PPMessage.INDEX_MESSAGE_TYPE
                                     || requestType == P2PPMessage.JOIN_MESSAGE_TYPE) {
                         canConsume = true;
                     }
                 } else if (Arrays.equals(messageType, P2PPMessage.RESPONSE_MESSAGE_TYPE)
                         || Arrays.equals(messageType, P2PPMessage.RESPONSE_ACK_MESSAGE_TYPE)) {
                     if (requestType == P2PPMessage.ENROLL_MESSAGE_TYPE
                             || requestType == P2PPMessage.AUTHENTICATE_MESSAGE_TYPE
                             || requestType == P2PPMessage.JOIN_MESSAGE_TYPE
                             || requestType == P2PPMessage.LEAVE_MESSAGE_TYPE
                             || requestType == P2PPMessage.KEEP_ALIVE_MESSAGE_TYPE
                             || requestType == P2PPMessage.LOOKUP_PEER_MESSAGE_TYPE
                             || requestType == P2PPMessage.EXCHANGE_TABLE_MESSAGE_TYPE
                             || requestType == P2PPMessage.QUERY_MESSAGE_TYPE
                             || requestType == P2PPMessage.PUBLISH_MESSAGE_TYPE
                             || requestType == P2PPMessage.LOOKUP_OBJECT_MESSAGE_TYPE
                             || requestType == P2PPMessage.REMOVE_OBJECT_MESSAGE_TYPE
                             || requestType == P2PPMessage.REPLICATE_MESSAGE_TYPE
                             || requestType == P2PPMessage.TRANSFER_MESSAGE_TYPE
                             || requestType == P2PPMessage.TUNNEL_MESSAGE_TYPE
                             || requestType == P2PPMessage.CONNECT_MESSAGE_TYPE
                             || requestType == P2PPMessage.GET_DIAGNOSTICS_MESSAGE_TYPE
                             || requestType == P2PPMessage.INDEX_MESSAGE_TYPE
                             || requestType == P2PPMessage.JOIN_MESSAGE_TYPE) {
                         canConsume = true;
                     }
                 } else if (Arrays.equals(messageType, P2PPMessage.INDICATION_MESSAGE_TYPE)) {
                     if (requestType == P2PPMessage.LEAVE_MESSAGE_TYPE
                             || requestType == P2PPMessage.INDEX_PEER_MESSAGE_TYPE) {
                         canConsume = true;
                     }
                 }
             } // end of is ACK
         } // end of if is P2PPMessage
         return canConsume;
     }
 
     @Override
     protected void consume(Message message) {
         try {
             if (LOG.isDebugEnabled()) {
                 LOG.debug("SuperPeerPeer starts consumption of a message of " + message.getClass().getName() + " type, state=" + state);
             }
             if (message instanceof P2PPMessage) {
                 if (message instanceof Request) {
 					onRequest((Request) message, true);
                 } else if (message instanceof Response) {
                     // TODO probably may be deleted, because transaction listeners are a better place to handle
                     // responses (they have direct access to request and transaction)
                     onResponseOrResponseACK((Response) message);
                 } else if (message instanceof Indication) {
                     onIndication((Indication) message);
                 } else if (message instanceof Acknowledgment) {
                     onAck((Acknowledgment) message);
                 }
             }
         } catch (NoSuchAlgorithmException e) {
             LOG.error("No algorithm!");
         }
         catch(Exception ex){
             LOG.error("An error occurred:", ex);
         }
     }
 
     @Override
     protected PeerInfo getNextHop(Request request) {
         return null;
     }
 
     @Override
     protected PeerInfo getNextHopForResourceID(byte[] id) {
         return routingTable.getNextHop(id);
     }
 
     @Override
     protected boolean isBestKnownPeer(Request request) {
 
         if (LOG.isDebugEnabled()) {
             LOG.debug("Received " + request.getClass().getSimpleName() + " is checked whether can be satisfied. sourceID="
                 + ByteUtils.byteArrayToHexString(request.getSourceID()));
         }
 
         if (request instanceof IndexRequest ||
                 request instanceof LookupIndexRequest ||
                 request instanceof RemoveObjectRequest ||
                 request instanceof LookupPeerIndexRequest ||
                 request instanceof SendMessageRequest ||
                 request instanceof JoinRequest) {
             PeerInfo nextHop = getNextHop(request);
             if (nextHop == null) {
                 return true;
             }
         } else if (request instanceof LookupObjectRequest ||
                 request instanceof LookupPeerRequest) {
             return true;
         }
 
         return false;
     }
 
     @Override
     public boolean isNodeAfterBootstrapping() {
         return state != INITIAL_NODE_STATE && state != BOOTSTRAPPING_NODE_STATE;
     }
 
     @Override
     protected void onForwardingRequest(Request request) {
         // TODO Auto-generated method stub
         if (LOG.isDebugEnabled()) LOG.debug("TODO: FORWARDING");
 
         PeerInfo nextHop = getNextHop(request);
         PeerInfo ownPeerInfo = sharedManager.getPeerInfo(true, true);
 
         request.setSenderID(ownPeerInfo.getPeerID().getPeerIDBytes());
         Response nextHopResponse = request.createNextHopResponse(ownPeerInfo, nextHop);
 
         transactionTable.createTransactionAndFill(nextHopResponse, transactionListener, request.getPeerInfo().getAddressInfos(),
                 sharedManager.getPeerIDAsBytes(), nextHop.getPeerID().getPeerIDBytes());
 
     }
 
     @Override
     public void onTimeSlot() {
         try {
             transactionTable.onTimeSlot(this);
 
             resourceManager.onTimeSlot(this);
 
             switch (state) {
                 /*
                case STUN_SERVER_COMMUNICATION_NODE_STATE: {
                // checks if server reflexive address was determined (TODO synchronize? because address is determined in
                // STUNService's thread [that's how it will look like when STUN implementation will be ready]).
                if (serverReflexiveAddress != null) {
                // adds determined address to shared manager
                sharedManager.addAddress(serverReflexiveAddress.getAddressAsString(), serverReflexiveAddress.getPort(),
                serverReflexiveAddress.getHt(), serverReflexiveAddress.getTt());
 
                // TODO is nullifying serverReflexiveAddress field needed?
                serverReflexiveAddress = null;
 
                // TODO in the future this should be changed to a state of determining relayed candidate
                state = JOIN_REQUEST_SENT_NODE_STATE;
 
                // gets a PeerInfo object describing remote Peer (closest to us) to which a Join request will be send
                byte[] ownPeerID = sharedManager.getPeerIDAsBytes();
                PeerInfo ownPeerInfo = sharedManager.getPeerInfo(true, true);
                PeerInfo closestInfo = routingTable.getClosestTo(ownPeerID);
 
                // TODO what about a case when closestInfo is null?
 
                logger.debug("Creating Join transaction and sending it to peer=" + closestInfo);
 
                // creates transaction for join request which will be send to
                JoinRequest joinRequest = new JoinRequest(P2PPMessage.P2PP_PROTOCOL_VERSION_1, true, false, (byte) 255,
                null, ownPeerID, false, false, null, ownPeerInfo);
                transactionTable.createTransactionAndFill(joinRequest, joinTransactionListener, closestInfo
                .getAddressInfos(), ownPeerID, closestInfo.getPeerID().getPeerIDBytes());
 
                }
                break;
                }
                 */
                 case JOINED_NODE_STATE: {
 
                     Date nowDate = new Date();
 
                     if (bootstrapCandidates.size() == 0) { // I am Supper-Peer
 
                         if ((nowDate.getTime() - this.lastPeerLookup.getTime()) / 1000 >= NodeTimers.PEER_LOOKUP_BOOTSTRAP_TIMER_SECONDS) {
 
                             for (Map.Entry<PeerID, PeerInfo> pEntry : this.peerIndex.entrySet()) {
 
                                 final PeerID pID = pEntry.getKey();
                                 final PeerInfo pPI = pEntry.getValue();
 
                                 if (LOG.isDebugEnabled()) {
                                     LOG.debug("Looking up peer " + pPI);
                                 }
 
                                 TransactionListener lookupPeerTransactionListener = new TransactionListener() {
 
                                     public void transactionEnded(byte[] transactionID, byte transactionState, byte TransactionType, Request request,
                                                                  Response response, TransactionTable transactionTable, P2PPEntity node) {
 
                                         if (LOG.isTraceEnabled()) {
                                             LOG.trace("transactionID=" + ByteUtils.byteArrayToHexString(transactionID) + "; transactionState=" + transactionState);
                                         }
 
                                         if (transactionState == Transaction.TERMINATED_STATE) {
 
                                             if (LOG.isTraceEnabled()) {
                                                 LOG.trace("Transaction terminated transactionID=" + ByteUtils.byteArrayToHexString(transactionID) +
                                                         "; transactionType=" + TransactionType + "; transactionState=" + transactionState);
                                             }
 
                                         } else {
 
                                             if (LOG.isDebugEnabled()) {
                                                 LOG.debug("Removing unresponsive peer and it's resources from indexes: " + pPI);
                                             }
 
                                             peerIndex.remove(pID);
 
                                             Collection<ResourceID> resourceIDs = reverseResourceIndex.remove(pPI);
                                             if (resourceIDs != null) {
                                                 for (ResourceID resourceID : resourceIDs) {
                                                     resourceIndex.get(resourceID).remove(pPI);
                                                 }
 
                                                 if (LOG.isTraceEnabled()) {
                                                     LOG.trace(resourceIDs.size() + " resources removed from indexes");
                                                 }
                                             }
 
                                             transactionTable.removeForPeerID(pID.getPeerIDBytes());
 
                                         }
 
                                     }
 
                                 };
 
                                 byte[] ownPeerId = sharedManager.getPeerIDAsBytes();
 
                                 PeerInfo ownPeerInfo = sharedManager.getPeerInfo(true, true);
 
                                 LookupPeerRequest request = new LookupPeerRequest(P2PPMessage.P2PP_PROTOCOL_VERSION_1, false, true, false, (byte) 255, null,
                                         ownPeerId, GlobalConstants.isOverReliable, false, null, ownPeerInfo);
 
                                 transactionTable.createTransactionAndFill(request, lookupPeerTransactionListener, pPI.getAddressInfos(), ownPeerId,
                                         pID.getPeerIDBytes());
 
                             }
 
                             this.lastPeerLookup = nowDate;
                         }
 
                     } else {
 
                         if ((nowDate.getTime() - this.lastSuperPeerLookup.getTime()) / 1000 >= NodeTimers.PEER_LOOKUP_BOOTSTRAP_TIMER_SECONDS) {
 
                             synchronized (this.resourceManager) {
                                 final List<ResourceObject> resources = this.resourceManager.getAllResourceObjects();
                                 if (resources != null && !resources.isEmpty()) {
                                     ResourceObject resource = resources.get(0);
 
                                     TransactionListener lookupIndexTransactionListener = new TransactionListener() {
                                         public void transactionEnded(byte[] transactionID, byte transactionState, byte TransactionType, Request request,
                                                                      Response response, TransactionTable transactionTable, P2PPEntity node) {
                                             try {
                                                 if (transactionState == Transaction.TERMINATED_STATE) {
                                                     if (response instanceof LookupIndexResponse) {
                                                         int responseCode = response.getResponseCodeAsInt();
                                                         if (responseCode == Response.RESPONSE_CODE_NOT_FOUND) {
                                                             if (LOG.isDebugEnabled()) LOG.debug("Super-Peer doesn't have my resources. Republishing");
                                                             for (ResourceObject r : resources) {
                                                                 publish(r.getUnhashedID(), r);
                                                             }
                                                         }
                                                     } else if (response instanceof NextHopResponse) {
                                                         if (LOG.isDebugEnabled()) LOG.debug("Received next hop response for lookup index");
                                                         NextHopResponse nextHopResponse = (NextHopResponse) response;
                                                         PeerInfo nextHop = nextHopResponse.getPeerInfo();
                                                         transactionTable.createTransactionAndFill(request, this, nextHop.getAddressInfos(),
                                                                 sharedManager.getPeerIDAsBytes(), nextHop.getPeerID().getPeerIDBytes());
                                                     }
                                                 } // if transaction ended with an error
                                                 else {
                                                     ErrorInterface error = new ErrorInterface() {
                                                         private Object value;
                                                         public Object getValue() {return value;}
                                                         public void setValue(Object value) {this.value = value;}
                                                     };
                                                     error.setValue(((LookupIndexRequest) request).getResourceLookup());
                                                     callback.errorCallback(error, P2PPNodeCallback.RESOURCE_LOOKUP_ERROR_CODE);
                                                 }
                                             } catch (Throwable e) {
                                                 StringBuilder strb = new StringBuilder("Error while processing transaction in lookupIndexTransactionListener transactionID=");
                                                 strb.append(ByteUtils.byteArrayToHexString(transactionID));
                                                 strb.append(" transactionState=").append(transactionState).append(" request=").append(request).append(" response=").append(response);
                                                 strb.append(" transactionTable=").append(transactionTable).append(" nodeState=").append(node.getState());
                                                 LOG.error(strb.toString(), e);
                                             }
                                         }
                                     };
 
                                     if (LOG.isDebugEnabled()) LOG.debug("Checking Super-Peer for my resources");
 
                                     RLookup resourceLookup = new RLookup(resource.getContentType(), resource.getContentSubtype(), resource.getResourceID(), resource.getOwner());
                                     PeerInfo ownPeerInfo = sharedManager.getPeerInfo(true, true);
                                     byte[] ownPeerID = ownPeerInfo.getPeerID().getPeerIDBytes();
                                     LookupIndexRequest request = new LookupIndexRequest(P2PPMessage.P2PP_PROTOCOL_VERSION_1, false, true, false, (byte) 255, null,
                                             ownPeerID, GlobalConstants.isOverReliable, false, null, sharedManager.getPeerInfo(true, true), resourceLookup);
                                     PeerInfo nextHop = routingTable.getNextHop(resourceLookup.getResourceID().getResourceID());
                                     if (nextHop != null) {
                                         transactionTable.createTransactionAndFill(request, lookupIndexTransactionListener, nextHop.getAddressInfos(), ownPeerID,
                                             nextHop.getPeerID().getPeerIDBytes());
                                     }
 
                                 }
                             }
                             this.lastSuperPeerLookup = nowDate;
                         }
 
                     }
 
                     break;
                 }
             }
         } catch (Throwable e) {
             LOG.error("Error while running onTimeSlot", e);
         }
     }
 
     @Override
     public void updateTables(PeerInfo peerInfo) {}
 
     @Override
     public void updateTables(Vector<PeerInfo> peerInfos) {}
 
     /**
      * Method invoked when Response or ResponseACK was received. Analyses given response (or responseACK).
      *
      * @param response
      * @throws NoSuchAlgorithmException
      *             Thrown when somewhere in the code the hash method was used with wrong non existing hash function.
      */
     private void onResponseOrResponseACK(Response response) throws NoSuchAlgorithmException {
 
         // Gets a transaction for given response.
         Transaction transaction = transactionTable.getTransaction(response);
 
         if (transaction == null) {
             // probably ignore, because generalAnalysis() should TODO change the info probably
             if (LOG.isDebugEnabled()) LOG.debug("No transaction matching to " + response.getTransactionID().toString());
         } else {
             // TODO probably remove transaction if reliable transport is used. or not (transaction table will do this)
             // transactionTable.removeMatchingTransaction(response);
 
             // passes received message to a transaction... it will know what to do
             transaction.setResponse(response);
             // next hop response doesn't have to be passed further
             if (response instanceof NextHopResponse) {
                 return;
             }
 
             // reaction depending on current and new state
             switch (state) {
                 case INITIAL_NODE_STATE:
                     break;
                 case BOOTSTRAPPING_NODE_STATE:
                     // If received response for previously sent bootstrap request
                     if (response instanceof BootstrapResponse) {
                         //onBootstrapResponse((BootstrapResponse) response);
                     } else {
                         if (LOG.isDebugEnabled()) {
                             LOG.debug("Peer received " + response.getClass().getName() + " in BOOTSTRAPPING state. It can't handle it now.");
                         }
                     }
                     break;
                 case SEARCHING_FOR_STUN_SERVER_NODE_STATE:
                     if (response instanceof LookupObjectResponse) {
 //					    onSTUNServerLookup((LookupObjectResponse) response);
                     } else {
                         if (LOG.isDebugEnabled()) {
                             LOG.debug("Peer received " + response.getClass().getName() + " in SEARCHING_FOR_STUN_SERVER state. It can't handle it now.");
                         }
                     }
                     break;
                 case STUN_SERVER_COMMUNICATION_NODE_STATE: {
                     LOG.debug("Peer received " + response.getClass().getName() + " in STUN_SERVER_COMMUNICATION state. It can't handle it now.");
                 }
                 break;
                 case JOIN_REQUEST_SENT_NODE_STATE:
                     if (response instanceof JoinResponse) {
 //					    onJoinResponse((JoinResponse) response);
                     } else {
                         if (LOG.isDebugEnabled()) {
                             LOG.debug("Peer received " + response.getClass().getName() + " in JOIN_REQUEST_SENT state. It can't handle it now.");
                         }
                     }
 
                     break;
                 case INFORMING_NEIGHBOURS_NODE_STATE: {
                     if (response instanceof PublishObjectResponse) {
 //					onPublishObjectRespnoseBeforeJoined((PublishObjectResponse) response);
                     } else {
                         if (LOG.isDebugEnabled()) {
                             LOG.debug("Peer received " + response.getClass().getName() + " in INFORMING_NEIGHBOURS state. "
                                 + "It can't handle it now.");
                         }
                     }
                 }
                 break;
                 case REQUESTING_NEIGHBOR_ROUTING_TABLES_NODE_STATE: {
                     if (response instanceof ExchangeTableResponse) {
 //					    onExchangeTableResponseBeforeJoined((ExchangeTableResponse) response);
                     } else {
                         if (LOG.isDebugEnabled()) {
                             LOG.debug("Peer received " + response.getClass().getName() + " in REQUESTING_NEIGHBOUR_ROUTING_TABLES state." +
                                 " It can't handle it now.");
                         }
                     }
                 }
                 break;
                 case JOINED_NODE_STATE:
                     if (LOG.isDebugEnabled()) LOG.debug("Peer received " + response.getClass().getName() + " in JOINED state.");
                     if (response instanceof PublishObjectResponse) {
 //					    onPublishObjectResponse((PublishObjectResponse) response);
                     } else if (response instanceof LookupObjectResponse) {
 //					    onLookupObjectResponse((LookupObjectResponse) response);
                     } else if (response instanceof TransferResponse) {
 //					    onTransferResponse((TransferResponse) response);
                     } else if (response instanceof SendMessageResponse ||
                             response instanceof LookupPeerIndexResponse) {
                         // Do nothing ...
                     } else {
                         if (LOG.isDebugEnabled()) {
                             LOG.debug("Peer received " + response.getClass().getName() + " in JOINED state. It can't handle it now.");
                         }
                     }
                     break;
             }
 
         }
 
     }
 
     /**
      * Method invoked when Indication was received. Analyses received indication.
      *
      * @param indication
      */
     private void onIndication(Indication indication) {
         
         PeerInfo peerInfo = indication.getPeerInfo();
         PeerID peerID = peerInfo.getPeerID();
 
         if (LOG.isDebugEnabled()) LOG.debug("Indication received");
 
         if (indication instanceof LeaveIndication) {
 
             Collection<ResourceID> resourceIDs = this.reverseResourceIndex.get(peerInfo);
 
             if (resourceIDs == null) {
                 LOG.warn("Received leave indication from already removed peer " + peerInfo);
             }
 
             if (LOG.isDebugEnabled()) {
                 LOG.debug("Peer received LeaveIndication from " + peerID + ". Removing it and it's " + resourceIDs.size() + " resources");
             }
 
             this.peerIndex.remove(peerInfo.getPeerID());
             for (ResourceID resourceID : resourceIDs) {
                 this.resourceIndex.remove(resourceID);
             }
             this.reverseResourceIndex.remove(peerInfo);
 
         } else if (indication instanceof IndexPeerIndication) {
 
             if (LOG.isDebugEnabled()) {
                 LOG.debug("Peer received IndexPeerIndication from bootstrap. Adding new peer to index: " + peerID);
             }
             addPeerToIndex(peerID, peerInfo);
 
         }
     }
 
     /**
      * Analyzes received acknowledgment. TODO I'm not sure if this is needed (probably
      * {@link P2PPEntity#generalAnalysis(P2PPMessage)} handles it and ACK is not passed here)... or not
      *
      * @param ack
      */
     private void onAck(Acknowledgment ack) {
 
         Transaction transaction = transactionTable.getTransaction(ack);
 
         // if there's a matching transaction for this ACK
         if (transaction != null) {
             transaction.setReceivedAck(ack);
         }
     }
 
     private void onLookupPeerRequest(LookupPeerRequest request) {
 
         PeerInfo ownPeerInfo = sharedManager.getPeerInfo(true, true);
         LookupPeerResponse response = request.createResponse(Response.RESPONSE_CODE_OK_BITS_ARRAY, ownPeerInfo);
         transactionTable.createTransaction(response, transactionListener, request.getPeerInfo().getAddressInfos(), sharedManager.getPeerIDAsBytes(),
                 request.getSourceID());
 
     }
 
     private void onIndexRequest(IndexRequest request) {
 
         try {
 
             final ResourceObject resourceObject = request.getResourceObject();
             PeerInfo peerInfo = request.getPeerInfo();
 
             addResourceToIndex(resourceObject.getResourceID(), peerInfo);
             callback.onDeliverRequest(request, new ArrayList<ResourceObject>(){{add(resourceObject);}});
 
             PeerInfo ownPeerInfo = sharedManager.getPeerInfo(true, true);
             IndexResponse response = request.createResponse(Response.RESPONSE_CODE_OK_BITS_ARRAY, ownPeerInfo, DEFAULT_EXPIRES_SECONDS);
             transactionTable.createTransaction(response, transactionListener, request.getPeerInfo().getAddressInfos(),
                     ownPeerInfo.getPeerID().getPeerIDBytes(), request.getPeerInfo().getPeerID().getPeerIDBytes());
 
         } catch (Throwable e) {
             LOG.error("Error while processing " + request, e);
         }
 
     }
 
     private void onLookupObjectRequest(LookupObjectRequest lookupRequest) {
         try {
 
             RLookup resourceLookup = lookupRequest.getResourceLookup();
             List<ResourceObject> resources = resourceManager.getResourceObject(resourceLookup);
 
             if (resources == null) {
                 if (LOG.isDebugEnabled()) LOG.debug("Resource " + resourceLookup.getResourceID() + " not found.");
                 resources = new ArrayList<ResourceObject>(1);
                 resources.add(null);
             }
 
             callback.onDeliverRequest(lookupRequest, resources);
 
             int resourcesSize = resources.size();
             boolean resourceFound = false;
             for (int i = 0; i < resourcesSize; i++) {
                 if (resources.get(i) != null) {
                     resourceFound = true;
                     break;
                 }
             }
 
             boolean[] responseCode = null;
             if (resourceFound) {
                 if (LOG.isDebugEnabled()) LOG.debug("Resource found (might have been added by callback).");
                 responseCode = Response.RESPONSE_CODE_OK_BITS_ARRAY;
             } else {
                 if (LOG.isDebugEnabled()) LOG.debug("Resource not found (might have been hidden by callback).");
                 responseCode = Response.RESPONSE_CODE_NOT_FOUND_BITS_ARRAY;
             }
 
             LookupObjectResponse response = lookupRequest.createResponse(responseCode, sharedManager.getPeerInfo(true, true), resources);
             transactionTable.createTransaction(response, transactionListener, lookupRequest.getPeerInfo().getAddressInfos(),
                             sharedManager.getPeerIDAsBytes(), response.getSourceID());
 
         } catch (Throwable e) {
             LOG.error("Error while processing " + lookupRequest, e);
         }
 	}
 
     /**
 	 * Analyses given {@link LookupIndexRequest}. Intended for usage in JOINED_NODE_STATE.
 	 *
 	 * @param lookupRequest
 	 *            Request to be analyzed.
 	 */
 	private void onLookupIndexRequest(LookupIndexRequest lookupRequest) {
 
         PeerInfo ownPeerInfo = sharedManager.getPeerInfo(true, true);
 
 		RLookup resourceLookup = lookupRequest.getResourceLookup();
         String resourceIDString = ByteUtils.byteArrayToHexString(resourceLookup.getResourceID().getResourceID());
 
         PeerInfo resourceKeeperPeerInfo = null;
 		List<PeerInfo> resourceKeepers = this.resourceIndex.get(resourceLookup.getResourceID());
         if (resourceKeepers != null && resourceKeepers.size() > 0) {
             resourceKeeperPeerInfo = resourceKeepers.get(0);
         }
 
 		boolean[] responseCode;
 		if (resourceKeeperPeerInfo != null) {
             if (LOG.isDebugEnabled()) LOG.debug("Keeper of resource " + resourceIDString + " found: " + resourceKeeperPeerInfo);
 			responseCode = Response.RESPONSE_CODE_OK_BITS_ARRAY;
 		} else {
 			if (LOG.isDebugEnabled()) LOG.debug("Keeper of resource " + resourceIDString + " not found.");
 			responseCode = Response.RESPONSE_CODE_NOT_FOUND_BITS_ARRAY;
             resourceKeeperPeerInfo = ownPeerInfo; //ignored
 		}
 
 		LookupIndexResponse response = lookupRequest.createResponse(responseCode, ownPeerInfo, resourceKeeperPeerInfo);
 		transactionTable.createTransaction(response, transactionListener, lookupRequest.getPeerInfo().getAddressInfos(),
 						sharedManager.getPeerIDAsBytes(), response.getSourceID());
 
 	}
 
     private void onLookupIndexLocal(LookupIndexRequest lookupRequest) {
 
         PeerInfo resourceKeeperPeerInfo = null;
         List<PeerInfo> resourceKeepers = this.resourceIndex.get(lookupRequest.getResourceLookup().getResourceID());
         if (resourceKeepers != null && resourceKeepers.size() > 0) {
             resourceKeeperPeerInfo = resourceKeepers.get(0);
         }
 
         if (LOG.isTraceEnabled()) {
             LOG.trace("onLookupIndexLocal keeper=" + resourceKeeperPeerInfo + " for request=" + lookupRequest);
         }
 
         PeerInfo ownPeerInfo = sharedManager.getPeerInfo(true, true);
 
         if (resourceKeeperPeerInfo != null) {
 
             byte[] ownPeerID = ownPeerInfo.getPeerID().getPeerIDBytes();
             LookupObjectRequest lookupObjectRequest = new LookupObjectRequest(P2PPMessage.P2PP_PROTOCOL_VERSION_1, false, true,
                     false, (byte) 255, null, ownPeerID, GlobalConstants.isOverReliable, false, null, ownPeerInfo, lookupRequest.getResourceLookup());
             transactionTable.createTransactionAndFill(lookupObjectRequest, lookupObjectTransactionListener, resourceKeeperPeerInfo.getAddressInfos(),
                     ownPeerID, resourceKeeperPeerInfo.getPeerID().getPeerIDBytes());
 
         } else {
             callback.onDeliverRequest(lookupRequest, new ArrayList<ResourceObject>());
         }
 
     }
 
     private void onRemoveObjectRequest(RemoveObjectRequest removeRequest) {
 
         ResourceObject resource = removeRequest.getResourceObject();
         PeerInfo removingPeer = removeRequest.getPeerInfo();
         PeerInfo ownPeerInfo = sharedManager.getPeerInfo(true, true);
 
         PeerInfo resourceKeeperPeerInfo = null;
 
 		List<PeerInfo> resourceKeepers = this.resourceIndex.get(resource.getResourceID());
         if (resourceKeepers != null && resourceKeepers.size() > 0) {
             if (resourceKeepers.remove(removingPeer)) {
                 resourceKeeperPeerInfo = removingPeer;
             }
         }
 
         boolean[] responseCode = Response.RESPONSE_CODE_NOT_FOUND_BITS_ARRAY;
         if (resourceKeeperPeerInfo != null) {
             responseCode = Response.RESPONSE_CODE_OK_BITS_ARRAY;
             this.reverseResourceIndex.remove(resourceKeeperPeerInfo);
         }
 
         RemoveObjectResponse removeResponse = removeRequest.createResponse(responseCode, ownPeerInfo);
         transactionTable.createTransaction(removeResponse, transactionListener, removeRequest.getPeerInfo().getAddressInfos(),
 						sharedManager.getPeerIDAsBytes(), removeResponse.getSourceID());
 
     }
 
     private void onLookupPeerIndexRequest(LookupPeerIndexRequest request) {
 
         PeerID seekedID = request.getPeerID();
         PeerInfo lookedUpPeerInfo = this.peerIndex.get(seekedID);
         PeerInfo ownPeerInfo = sharedManager.getPeerInfo(true, true);
 
         boolean[] responseCode;
         if (lookedUpPeerInfo != null) {
             if (LOG.isDebugEnabled()) LOG.debug("Peer of PeerID=" + seekedID + ":" + lookedUpPeerInfo);
             responseCode = Response.RESPONSE_CODE_OK_BITS_ARRAY;
         } else {
             if (LOG.isDebugEnabled()) LOG.debug("Peer of PeerID=" + seekedID + " not found");
             responseCode = Response.RESPONSE_CODE_NOT_FOUND_BITS_ARRAY;
             lookedUpPeerInfo = request.getPeerInfo(); //ignored
         }
 
         LookupPeerIndexResponse response = request.createResponse(responseCode, ownPeerInfo, lookedUpPeerInfo);
         transactionTable.createTransaction(response, transactionListener, request.getPeerInfo().getAddressInfos(), sharedManager.getPeerIDAsBytes(),
                 response.getSourceID());
 
     }
 
     private void onSendMessageRequest(final SendMessageRequest request) {
 
         callback.onDeliverRequest(request, new ArrayList<ResourceObject>(){{add(request.getMessageResourceObject());}});
 
         PeerInfo ownPeerInfo = sharedManager.getPeerInfo(true, true);
         SendMessageResponse response = request.createResponse(Response.RESPONSE_CODE_OK_BITS_ARRAY, ownPeerInfo);
         transactionTable.createTransaction(response, transactionListener, request.getPeerInfo().getAddressInfos(),
                 sharedManager.getPeerIDAsBytes(), response.getSourceID());
 
     }
 
     private void onJoinRequest(JoinRequest request) {
 
         PeerInfo peerInfo = request.getPeerInfo();
         PeerID peerID = peerInfo.getPeerID();
 
         if (LOG.isDebugEnabled()) {
             LOG.debug("Peer received IndexPeerIndication from bootstrap. Adding new peer to index: " + peerID);
         }
 
         addPeerToIndex(peerID, peerInfo);
 
         PeerInfo ownPeerInfo = sharedManager.getPeerInfo(true, true);
         byte[] ownPeerID = sharedManager.getPeerIDAsBytes();
 
         JoinResponse response = request.createResponse(Response.RESPONSE_CODE_OK_BITS_ARRAY, ownPeerInfo,
                 new Expires(NodeTimers.KEEP_ALIVE_TIMER_SECONDS), null, null);
         transactionTable.createTransaction(response, transactionListener, request.getPeerInfo().getAddressInfos(), ownPeerID, peerID.getPeerIDBytes());
 
     }
 
 }
