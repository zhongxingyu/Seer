 package ceid.netcins.exo;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.Serializable;
 import java.io.StringReader;
 import java.net.URL;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.Vector;
 
 import rice.Continuation;
 import rice.Continuation.MultiContinuation;
 import rice.Continuation.NamedContinuation;
 import rice.Continuation.StandardContinuation;
 import rice.environment.logging.Logger;
 import rice.p2p.commonapi.Id;
 import rice.p2p.commonapi.IdFactory;
 import rice.p2p.commonapi.IdRange;
 import rice.p2p.commonapi.IdSet;
 import rice.p2p.commonapi.Message;
 import rice.p2p.commonapi.Node;
 import rice.p2p.commonapi.NodeHandle;
 import rice.p2p.commonapi.NodeHandleSet;
 import rice.p2p.commonapi.rawserialization.InputBuffer;
 import rice.p2p.commonapi.rawserialization.MessageDeserializer;
 import rice.p2p.past.PastContent;
 import rice.p2p.past.PastContentHandle;
 import rice.p2p.past.PastException;
 import rice.p2p.past.PastImpl;
 import rice.p2p.past.messaging.CacheMessage;
 import rice.p2p.past.messaging.ContinuationMessage;
 import rice.p2p.past.messaging.FetchHandleMessage;
 import rice.p2p.past.messaging.FetchMessage;
 import rice.p2p.past.messaging.InsertMessage;
 import rice.p2p.past.messaging.LookupHandlesMessage;
 import rice.p2p.past.messaging.LookupMessage;
 import rice.p2p.past.messaging.PastMessage;
 import rice.p2p.util.rawserialization.SimpleOutputBuffer;
 import rice.persistence.StorageManager;
 import ceid.netcins.exo.catalog.Catalog;
 import ceid.netcins.exo.catalog.CatalogEntry;
 import ceid.netcins.exo.catalog.ContentCatalogEntry;
 import ceid.netcins.exo.catalog.ScoreBoard;
 import ceid.netcins.exo.catalog.SocialCatalog;
 import ceid.netcins.exo.catalog.URLCatalogEntry;
 import ceid.netcins.exo.catalog.UserCatalogEntry;
 import ceid.netcins.exo.content.ContentField;
 import ceid.netcins.exo.content.ContentProfile;
 import ceid.netcins.exo.content.ContentProfileFactory;
 import ceid.netcins.exo.content.StoredField;
 import ceid.netcins.exo.content.TermField;
 import ceid.netcins.exo.content.TokenizedField;
 import ceid.netcins.exo.messages.FriendAcceptMessage;
 import ceid.netcins.exo.messages.FriendQueryMessage;
 import ceid.netcins.exo.messages.FriendRejectMessage;
 import ceid.netcins.exo.messages.FriendReqMessage;
 import ceid.netcins.exo.messages.FriendReqPDU;
 import ceid.netcins.exo.messages.GetUserProfileMessage;
 import ceid.netcins.exo.messages.InsertPDU;
 import ceid.netcins.exo.messages.MessageType;
 import ceid.netcins.exo.messages.QueryMessage;
 import ceid.netcins.exo.messages.QueryPDU;
 import ceid.netcins.exo.messages.ResponsePDU;
 import ceid.netcins.exo.messages.RetrieveContIDsMessage;
 import ceid.netcins.exo.messages.RetrieveContMessage;
 import ceid.netcins.exo.messages.RetrieveContPDU;
 import ceid.netcins.exo.messages.RetrieveContTagsMessage;
 import ceid.netcins.exo.messages.SocialQueryMessage;
 import ceid.netcins.exo.messages.SocialQueryPDU;
 import ceid.netcins.exo.messages.TagContentMessage;
 import ceid.netcins.exo.messages.TagPDU;
 import ceid.netcins.exo.messages.TagUserMessage;
 import ceid.netcins.exo.similarity.Scorer;
 import ceid.netcins.exo.similarity.SimilarityRequest;
 import ceid.netcins.exo.social.SocService;
 import ceid.netcins.exo.social.SocialBookMark;
 import ceid.netcins.exo.social.TagCloud;
 import ceid.netcins.exo.social.URLBookMark;
 import ceid.netcins.exo.user.Friend;
 import ceid.netcins.exo.user.FriendRequest;
 import ceid.netcins.exo.user.SharedContentInfo;
 import ceid.netcins.exo.user.User;
 import ceid.netcins.exo.utils.JavaSerializer;
 
 /**
  * 
  * @author <a href="mailto:loupasak@ceid.upatras.gr">Andreas Loupasakis</a>
  * @author <a href="mailto:ntarmos@cs.uoi.gr">Nikos Ntarmos</a>
  * @author <a href="mailto:peter@ceid.upatras.gr">Peter Triantafillou</a>
  * 
  * "eXO: Decentralized Autonomous Scalable Social Networking"
  * Proc. 5th Biennial Conf. on Innovative Data Systems Research (CIDR),
  * January 9-12, 2011, Asilomar, California, USA.
  */
 public class CatalogService extends PastImpl implements SocService {
 
 	// Factory which used to obtain the content profile data
 	private ContentProfileFactory cpf;
 
 	// User data for the specific node
 	private User user;
 
 	// Types of printing results
 	public static final int CONTENT = 0;
 	public static final int USER = 1;
 	public static final int HYBRID = 2;
 
 	// Result statuses
 	public static final int SUCCESS = 0;
 	public static final int FAILURE = 1;
 	public static final int EXCEPTION = 2;
 
 	// Our Scorer thread functionality
 	private Scorer scorer;
 	private Thread scorerThread;
 
 	// Load counting variable
 	//private int hits;
 
 	protected class CatalogServiceDeserializer implements MessageDeserializer {
 		public Message deserialize(InputBuffer buf, short type, int priority,
 				NodeHandle sender) throws IOException {
 			try {
 				switch (type) {
 				case MessageType.Cache:
 					return CacheMessage.build(buf, endpoint,
 							contentDeserializer);
 				case MessageType.FetchHandle:
 					return FetchHandleMessage.build(buf, endpoint,
 							contentHandleDeserializer);
 				case MessageType.Fetch:
 					return FetchMessage.build(buf, endpoint,
 							contentDeserializer, contentHandleDeserializer);
 				case MessageType.Insert:
 					return InsertMessage.build(buf, endpoint,
 							contentDeserializer);
 				case MessageType.LookupHandles:
 					return LookupHandlesMessage.build(buf, endpoint);
 				case MessageType.Lookup:
 					return LookupMessage.build(buf, endpoint,
 							contentDeserializer);
 				case MessageType.Query:
 					return QueryMessage.build(buf, endpoint,
 							contentDeserializer);
 				case MessageType.FriendRequest:
 					return FriendReqMessage.build(buf, endpoint,
 							contentDeserializer);
 				case MessageType.FriendAccept:
 					return FriendAcceptMessage.build(buf, endpoint,
 							contentDeserializer);
 				case MessageType.FriendReject:
 					return FriendRejectMessage.build(buf, endpoint,
 							contentDeserializer);
 				case MessageType.TagContent:
 					return TagContentMessage.build(buf, endpoint,
 							contentDeserializer);
 				case MessageType.TagUser:
 					return TagUserMessage.build(buf, endpoint,
 							contentDeserializer);
 				case MessageType.SocialQuery:
 					return SocialQueryMessage.build(buf, endpoint,
 							contentDeserializer);
 				case MessageType.RetrieveContent:
 					return RetrieveContMessage.build(buf, endpoint,
 							contentDeserializer);
 				case MessageType.RetrieveContentTags:
 					return RetrieveContTagsMessage.build(buf, endpoint,
 							contentDeserializer);
 				case MessageType.RetrieveContentIDs:
 					return RetrieveContIDsMessage.build(buf, endpoint,
 							contentDeserializer);
 				case MessageType.FriendQuery:
 					return FriendQueryMessage.build(buf, endpoint,
 							contentDeserializer);
 				case MessageType.GetUserProfile:
 					return GetUserProfileMessage.build(buf, endpoint,
 							contentDeserializer);
 				}
 			} catch (IOException e) {
 				if (logger.level <= Logger.SEVERE)
 					logger.log("Exception in deserializer in "
 							+ CatalogService.this.endpoint.toString() + ":"
 							+ instance + " " + e);
 				throw e;
 			}
 			throw new IllegalArgumentException("Unknown type:" + type + " in "
 					+ CatalogService.this.toString());
 		}
 	}
 
 	/**
 	 * Constructor pushes the appropriate arguments to PastImpl and
 	 * initializes the Service.
 	 * 
 	 */
 	public CatalogService(Node node, StorageManager manager, int replicas,
 			String instance, User user) {
 		super(node, manager, replicas, instance);
 		this.endpoint.setDeserializer(new CatalogServiceDeserializer());
 
 		cpf = new ContentProfileFactory();
 		this.user = user;
 		scorer = new Scorer();
 
 		// Start the "Scorer" thread to be waiting!
 		// One thread per node!
 		scorerThread = new Thread(new Runnable() {
 
 			public void run() {
 				// Begin the wait-serving loop
 				scorer.startScorer();
 			}
 
 		}, "Scorer");
 	}
 
 	public void start() {
 		scorerThread.start();
 		doStartIndexUser(user.getPublicUserProfile());
 	}
 
 	private void doStartIndexUser(final ContentProfile profile) {
 		indexUser(profile, null, new Continuation<Object, Exception>() {
 			public void receiveResult(Object result) {
 				if (logger.level <= Logger.INFO)
 					logger.log("User : " + user.getUID()
 							+ " indexed successfully");
 				// TODO : Check the replicas if are updated correctly!
 				// run replica maintenance
 				// runReplicaMaintence();
 				if (!(result instanceof Boolean[])) {
 					throw new RuntimeException("Unable to index user!");
 				}
 				Boolean[] results = (Boolean[]) result;
 				int indexedNum = 0;
 				if (results != null)
 					for (Boolean isIndexedTerm : results) {
 						if (isIndexedTerm)
 							indexedNum++;
 					}
 				if (logger.level <= Logger.INFO)
 					logger.log("Total " + indexedNum + " terms indexed out of " + results.length);
 				if (indexedNum < results.length)
 					receiveException(new Exception());
 			}
 
 			public void receiveException(Exception result) {
 				System.out.println("User : " + user.getUID()
 						+ ", indexed with errors : "
 						+ result.getMessage() + " Retrying...");
 				doStartIndexUser(profile);
 			}
 		});
 	}
 
 	/**
 	 * This is used to create a new user profile. In order to update the user
 	 * profile a update function is better to be used.
 	 * 
 	 * @param m
 	 *            A map with the fields and the corresponding terms
 	 * @throws java.io.IOException
 	 */
 	public void createUserProfile(Map<String, String> m) throws IOException {
 		createUserProfile(m, ContentProfileFactory.DEFAULT_DELIMITER);
 	}
 
 	/**
 	 * This is used to create a new user profile. In order to update the user
 	 * profile a update function is better to be used.
 	 * 
 	 * @param m
 	 *            A map with the fields and the corresponding terms
 	 * @param The
 	 *            delimiter used to split the terms
 	 * @throws java.io.IOException
 	 */
 	public void createUserProfile(Map<String, String> m, String delimiter)
 			throws IOException {
 		user.setUserProfile(cpf.buildProfile(m, delimiter));
 	}
 
 	/**
 	 * Tag a user profile with the given uid.
 	 * 
 	 * @param uid The destination node id.
 	 * @param nodeHandle Hint to use for the first hop routing.
 	 * @return The requested user profile
 	 * @throws Exception
 	 */
 	public void tagUser(final Id uid, final ContentProfile tags,
 			final NodeHandle nodeHandle, final Continuation<Object, Exception> command) {
 
 		if (this.user == null) {
 			command.receiveException(new RuntimeException("User has not be registered yet!"));
 			return;
 		}
 
 		// Fill in the extra arguments (non-standard ones) we want to pass to
 		// the lookup DHT wrapper.
 		HashMap<String, Object> extra_args = new HashMap<String, Object>();
 		extra_args.put("taggedId", uid);
 		extra_args.put("PDU", new TagPDU(uid, tags));
 
 		// Issue a lookup request to the underline DHT service
 		lookup(uid, TagUserMessage.TYPE, extra_args,
 				new StandardContinuation<Object, Exception>(command) {
 
 			public void receiveResult(Object result) {
 				// We expect a UserCatalogEntry to be returned
 				if (result instanceof UserCatalogEntry) {
 					UserCatalogEntry cce = (UserCatalogEntry) result;
 
 					Vector<String> tagArray = new Vector<String>();
 					if (tags != null)
 						for (ContentField cftag : tags.getAllFields())
 								tagArray.add(((TermField)cftag).getFieldName());
 
 					// First, remove tags not there any more
 					Map<String, SocialCatalog> invMap = user.getTagContentList();
 					for (SocialCatalog sc : invMap.values()) {
 						Set<UserCatalogEntry> uce = null;
 						if ((uce = sc.getUserCatalogEntries()) != null) {
 							Iterator<UserCatalogEntry> uceIter = uce.iterator();
 							while (uceIter.hasNext()) {
 								UserCatalogEntry uc = uceIter.next();
 								if (uc.getUID().equals(uid) && tagArray.contains(sc.getTag()))
 									uceIter.remove();
 							}
 						}
 					}
 
 					// Now we can add the ContentCatalogEntry to each
 					// SocialCatalog (for each tag)
 					// Tagers inverted list.
 					if (tags != null) {
 						for (String tag: tagArray) {
 							SocialCatalog scat = user.getTagContentList().get(tag);
 							if (scat == null)
 								scat = new SocialCatalog(tag);
 							Set<?> ccatEntries = scat.getUserCatalogEntries();
 							if (!ccatEntries.contains(cce))
 								scat.addUserCatalogEntry(cce);
 							user.addTagContentList(tag, scat);
 						}
 					}
 					// Wrap the result in a HashMap object together with a
 					// notification message
 					parent.receiveResult(wrapToResponse(
 							"Tagged user " + uid, SUCCESS, cce));
 				} else {
 					parent.receiveResult(wrapToResponse(
 							"Failed to tag user " + uid, FAILURE, result));
 				}
 			}
 			public void receiveException(Exception result) {
 				parent.receiveException(result);
 			}
 		});
 	}
 
 	public void setUserProfile(ContentProfile profile, Continuation<Object, Exception> command) {
 		ContentProfile oldProfile = new ContentProfile(user.getCompleteUserProfile());
 
 		// The public part has changed. We should reindex the user profile in the network
 		if (!oldProfile.equalsPublic(profile)) {
 			ContentProfile additions = profile.minus(oldProfile);
 			ContentProfile deletions = oldProfile.minus(profile);
 			indexUser(additions, deletions,
 					(command != null) ? command : new Continuation<Object, Exception>() {
 				@Override
 				public void receiveResult(Object result) {
 					if (logger.level <= Logger.INFO)
 						logger.log("User : " + user.getUID()
 								+ " indexed successfully");
 
 					// TODO : Check the replicas if are updated correctly!
 					// run replica maintenance
 					// runReplicaMaintence();
 					if (result instanceof Boolean[]) {
 						Boolean[] results = (Boolean[]) result;
 						int indexedNum = 0;
 						if (results != null)
 							for (Boolean isIndexedTerm : results) {
 								if (isIndexedTerm)
 									indexedNum++;
 							}
 						if (logger.level <= Logger.INFO)
 							logger.log("Total " + indexedNum + " terms indexed out of " + results.length);
 					}
 				}
 
 				@Override
 				public void receiveException(Exception result) {
 					System.out.println("User : " + user.getUID()
 							+ ", indexed with errors : " + result.getMessage());
 				}
 			});
 		}
 	}
 
 
 	public void getUserProfile(Friend friend, Continuation<Object, Exception> command) throws Exception {
 		this.getUserProfile(friend.getUID(), friend.getNodeHandle(),
 				command);
 	}
 
 	public void getUserProfile(Id uid, Continuation<Object, Exception> command) throws Exception {
 		this.getUserProfile(uid, null, command);
 	}
 
 	/**
 	 * Getter for a user profile which belongs to the user with the given uid.
 	 * 
 	 * @param uid The destination node id.
 	 * @param nodeHandle Hint to use for the first hop routing.
 	 * @return The requested user profile
 	 * @throws Exception
 	 */
 	public void getUserProfile(final Id uid,
 			final NodeHandle nodeHandle, final Continuation<Object, Exception> command) throws Exception {
 
 		if (this.user == null) {
 			command.receiveException(new RuntimeException("User has not be registered yet!"));
 			return;
 		}
 
 		// Fill in the extra arguments (non-standard ones) we want to pass to
 		// the lookup DHT wrapper.
 		HashMap<String, Object> extra_args = new HashMap<String, Object>();
 		extra_args.put("nodeHandle", nodeHandle);
 
 		// Issue a lookup request to the underline DHT service
 		lookup(uid, GetUserProfileMessage.TYPE, extra_args,
 				new StandardContinuation<Object, Exception>(command) {
 
 			public void receiveResult(Object result) {
 				// We expect a ContentProfile object with the user profile data.
 				if (result instanceof ContentProfile) {
 					parent.receiveResult(wrapToResponse(
 							"Got user profile of "+uid,	SUCCESS,
 							(ContentProfile)result));
 				} else {
 					parent.receiveResult(wrapToResponse(
 							"Failed to get user profile of "+uid, FAILURE,
 							result));
 				}
 			}
 			public void receiveException(Exception result) {
 				parent.receiveResult(wrapToResponse(
 						"Failed to get user profile of "+uid,
 						EXCEPTION,
 						result));
 			}
 		});
 	}
 
 	/**
 	 * This is a local user profile getter.
 	 * 
 	 * @return The profile object.
 	 */
 	public ContentProfile getUserProfile() {
 		return user.getCompleteUserProfile();
 	}
 
 	/**
 	 * This is used to create a url profile.
 	 * 
 	 * @param m
 	 *            A map with the fields and the corresponding terms
 	 * @throws java.io.IOException
 	 */
 	public ContentProfile createURLProfile(Map<String, String> m)
 			throws IOException {
 		return cpf.buildProfile(m);
 	}
 
 	/**
 	 * This is used to create the tag tokens and profile. This is used to filter
 	 * the tags within the tokenizer (StopWords etc.)
 	 * 
 	 * @param m
 	 *            A string with all tags we want to apply on some object
 	 * @throws java.io.IOException
 	 */
 	public String[] tokenizeTags(String tags) throws IOException {
 		TreeSet<String> ts = cpf.termSet(new StringReader(tags));
 		return ts.toArray(new String[ts.size()]);
 	}
 
 	/**
 	 * Sends a request for friendship to an other user of the network specified
 	 * by the uid.
 	 * 
 	 * @param uid Destination user UID
 	 * @param message A custom string formed by the source user.
 	 * @param command The response callback.
 	 * @throws Exception 
 	 */
 	public void friendRequest(final Id uid, final String message,
 			final Continuation<Object, Exception> command) {
 
 		if (this.user == null) {
 			command.receiveException(new RuntimeException("User has not be registered yet!"));
 			return;
 		}
 
 		// Fill in the extra arguments (non-standard ones) we want to pass to
 		// the lookup DHT wrapper.
 		HashMap<String, Object> extra_args = new HashMap<String, Object>();
 		extra_args.put("PDU", new FriendReqPDU(message, user.getScreenName()));
 
 		// Issue a lookup request to the uderline DHT service
 		lookup(uid, FriendReqMessage.TYPE, extra_args,
 				new StandardContinuation<Object, Exception>(command) {
 
 			public void receiveResult(Object result) {
 				if (result instanceof Boolean) {
 					// Now we can add the UID to the queue for pending
 					// outgoing requests!
 					user.addPendingOutgoingFReq(uid);
 
 					parent.receiveResult(wrapToResponse(
 							"Sent friend request to user " + uid,	SUCCESS,
 							(Boolean)result));
 				} else {
 					parent.receiveResult(wrapToResponse(
 							"Failed to send friend request to user " + uid,
 							FAILURE, result));
 				}
 			}
 			public void receiveException(Exception result) {
 				parent.receiveResult(wrapToResponse(
 						"Failed to send friend request to user " + uid,
 						EXCEPTION, result));
 			}
 		});
 
 	}
 
 	/*
 	 * Wrapper for the generic form of this function.
 	 */
 	public void acceptFriend(final FriendRequest freq, final Continuation<Object, Exception> command) {
 		acceptFriend(freq, "", command);
 	}
 
 	/**
 	 * This method sends an approval message for friendship to an other user of
 	 * the network specified by the uid. Message is an empty message formed by
 	 * the source user to say "I approve your friendship request". Warning! In
 	 * order to operate properly nodes must be assigned nodeIDs created by the
 	 * user unique names (e.g. email address).
 	 * 
 	 * @param freq The friend request for approval
 	 * @param message An optional message to send to the dest user.
 	 * @param command The callback that must be executed when we return
 	 * @throws Exception
 	 */
 	public void acceptFriend(final FriendRequest freq, String message,
 			final Continuation<Object, Exception> command) {
 
 		if (this.user == null) {
 			command.receiveException(new RuntimeException("User has not be registered yet!"));
 			return;
 		}
 
 		final Id uid = freq.getUID();
 		// Fill in the extra arguments (non-standard ones) we want to pass to
 		// the lookup DHT wrapper.
 		HashMap<String, Object> extra_args = new HashMap<String, Object>();
 		extra_args.put("PDU", new FriendReqPDU(message, user.getScreenName()));
 
 		// Issue a lookup request to the uderline DHT service
 		lookup(uid, FriendAcceptMessage.TYPE, extra_args,
 			   new StandardContinuation<Object, Exception>(command) {
 
 			public void receiveResult(Object result) {
 				if (result instanceof Boolean) {
 					// Now we know that message of approval has been sent
 					// successfully and we can remove the freq and add the
 					// Friend object to the friend list.
 					user.removePendingIncomingFReq(freq);
 					user.addFriend(new Friend(freq.getUID(), freq
 							.getFriendReqPDU().getScreenName(),
 							freq.getSourceHandle()));
 
 					parent.receiveResult(wrapToResponse(
 							"Accepted friend request of user " + uid,	SUCCESS,
 							(Boolean)result));
 				} else {
 					parent.receiveResult(wrapToResponse(
 							"Failed to accept friend request of user " + uid,
 							FAILURE, result));
 				}
 			}
 			public void receiveException(Exception result) {
 				parent.receiveResult(wrapToResponse(
 						"Failed to accept friend request of user " + uid,
 						EXCEPTION, result));
 			}
 		});
 	}
 
 	/*
 	 * Wrapper for the generic form of this function.
 	 */
 	public void rejectFriend(final FriendRequest freq, final Continuation<Object, Exception> command) {
 		rejectFriend(freq, "", command);
 	}
 
 	/**
 	 * This method sends an reject friendship request message to another user of
 	 * the network specified by the uid. Message is an empty message formed by
 	 * the source user to say "I reject your friendship request". Warning! In
 	 * order to operate properly nodes must be assigned nodeIDs created by the
 	 * user unique names (e.g. email address).
 	 * 
 	 * @param freq The friend request for approval
 	 * @param message An optional message to send to the dest user.
 	 * @param command The callback that must be executed when we return
 	 * @throws Exception
 	 */
 	public void rejectFriend(final FriendRequest freq, String message,
 			final Continuation<Object, Exception> command) {
 
 		if (this.user == null) {
 			command.receiveException(new RuntimeException("User has not be registered yet!"));
 			return;
 		}
 
 		final Id uid = freq.getUID();
 		// Fill in the extra arguments (non-standard ones) we want to pass to
 		// the lookup DHT wrapper.
 		HashMap<String, Object> extra_args = new HashMap<String, Object>();
 		extra_args.put("PDU", new FriendReqPDU(message, user.getScreenName()));
 
 		// Issue a lookup request to the uderline DHT service
 		lookup(uid, FriendRejectMessage.TYPE, extra_args,
 			   new StandardContinuation<Object, Exception>(command) {
 
 			public void receiveResult(Object result) {
 				if (result instanceof Boolean) {
 					// Now we know that message has sent
 					// successfully and we can remove the freq!
 					user.removePendingIncomingFReq(freq);
 
 					parent.receiveResult(wrapToResponse(
 							"Rejected friend request of user " + uid,	SUCCESS,
 							(Boolean)result));
 				} else {
 					parent.receiveResult(wrapToResponse(
 							"Failed to reject friend request of user " + uid,
 							FAILURE, result));
 				}
 			}
 			public void receiveException(Exception result) {
 				parent.receiveResult(wrapToResponse(
 						"Failed to reject friend request of user " + uid,
 						EXCEPTION, result));
 			}
 		});
 	}
 
 	/**
 	 * This method is used to retrieve the original (or pseudodata for
 	 * simulation) and maybe some tagclouds from a user node. In order to know
 	 * where we should travel to fetch the data a nodeId and a data checksum
 	 * must have been obtained previously. This can be done for exmple by using
 	 * a search request.
 	 * 
 	 * @param uid
 	 *            User unique id (destination node).
 	 * @param contentId
 	 *            The Id of the taging content (checksum, SHA-1 of synonyms
 	 *            etc.).
 	 * @param clouds
 	 *            This handles whether tag clouds will be returned or not.
 	 * @param command
 	 *            An asynchronous command which will be executed on return.
 	 */
 	public void retrieveContent(Id uid, Id contentId, final boolean clouds,
 			final Continuation<Object, Exception> command) {
 
 		if (this.user == null) {
 			command.receiveException(new RuntimeException("User has not be registered yet!"));
 			return;
 		}
 
 		final Id destuid = uid;
 
 		RetrieveContPDU retPDU = new RetrieveContPDU(contentId);
 		// send the request across the wire, and see if the result is null or not
 		sendRequest(destuid,
 				new RetrieveContMessage(getUID(), retPDU.getContentId(), getLocalNodeHandle(), destuid, retPDU),
 				new NamedContinuation("RetrieveContMessage for " + destuid, command) {
 					public void receiveResult(final Object o) {
 						// if we have an object, we return it
 						// otherwise, we must check all replicas in order to make sure
 						// that the object doesn't exist anywhere
 						if (o != null) {
 							command.receiveResult(o);
 						} else {
 							// TODO : examine if the tager's arrays need to be replicated to the leafset
 							// If so then here we should put the lookupHandles code as above!!!
 							command.receiveResult(null); // o is NULL
 						}
 					}
 
 					public void receiveException(Exception e) {
 						// If the lookup message failed , we then try to fetch all of
 						// the handles, just
 						// in case. This may fail too, but at least we tried.
 						receiveResult(null);
 					}
 				}
 		);
 	}
 
 	/**
 	 * This method is used to retrieve the original (or pseudodata for
 	 * simulation) and maybe some tagclouds from a user node. In order to know
 	 * where we should travel to fetch the data a nodeId and a data checksum
 	 * must have been obtained previously. This can be done for exmple by using
 	 * a search request.
 	 * 
 	 * @param uid
 	 *            User unique id (destination node).
 	 * @param contentId
 	 *            The Id of the taging content (checksum, SHA-1 of synonyms
 	 *            etc.).
 	 * @param clouds
 	 *            This handles whether tag clouds will be returned or not.
 	 * @param command
 	 *            An asynchronous command which will be executed on return.
 	 */
 	@SuppressWarnings("unchecked")
 	public void retrieveContentTags(Id uid, Id contentId,
 			final Continuation<Object, Exception> command) {
 
 		if (this.user == null) {
 			command.receiveException(new RuntimeException("User has not be registered yet!"));
 			return;
 		}
 
 		final Id destuid = uid;
 		HashMap<String, Object> extra_args = new HashMap<String, Object>();
 		extra_args.put("ContentId", contentId);
 		extra_args.put("PDU", new RetrieveContPDU(contentId));
 
 		// Issue a lookup request to the underline DHT service
 		lookup(uid, RetrieveContTagsMessage.TYPE, extra_args,
 				new NamedContinuation(
 						"RetrieveContTags(" + destuid + ")",
 						command) {
 
 					public void receiveResult(Object result) {
 						if (result instanceof ContentProfile) {
 							parent.receiveResult(result);
 						} else {
 							parent.receiveException(new PastException("Result was of wrong type"));
 						}
 					}
 
 					public void receiveException(Exception exception) {
 						parent.receiveException(exception);
 					}
 				});
 
 	}
 
 	@SuppressWarnings("unchecked")
 	public void retrieveContentIDs(Id uid, final Continuation<Object, Exception> command) {
 
 		if (this.user == null) {
 			command.receiveException(new RuntimeException("User has not be registered yet!"));
 			return;
 		}
 
 		final Id destuid = uid;
 		HashMap<String, Object> extra_args = new HashMap<String, Object>();
 
 		// Issue a lookup request to the underline DHT service
 		lookup(uid, RetrieveContIDsMessage.TYPE, extra_args,
 				new NamedContinuation(
 						"RetrieveContIDs(" + destuid + ")",
 						command) {
 
 					public void receiveResult(Object result) {
 						if (result instanceof Map) {
 							parent.receiveResult(result);
 						} else {
 							parent.receiveException(new PastException("Result was of wrong type"));
 						}
 					}
 
 					public void receiveException(Exception exception) {
 						parent.receiveException(exception);
 					}
 				});
 	}
 
 	/**
 	 * Sends a set of tags for a specific content object (contentId)
 	 * to its owner's node specified by the uid.
 	 * 
 	 * @param uid User unique id (destination node).
 	 * @param contentId The Id of the content which is tagged (checksum,
 	 * SHA-1 etc.).
 	 * @param tags An array of the tags which will be applied to the content
 	 * object.
 	 * @param command An asynchronous command which will be executed on return.
 	 * @throws Exception
 	 */
 	public void tagContent(final Id uid, final Id contentId, final ContentProfile tags,
 			final Continuation<Object, Exception> command) {
 
 		if (this.user == null) {
 			command.receiveException(new RuntimeException("User has not be registered yet!"));
 			return;
 		}
 
 		ContentProfile cp = new ContentProfile();
 		for (ContentField cf : tags.getAllFields()) {
 			if (!(cf instanceof StoredField))
 				cp.add(cf);
 		}
 
 		// Fill in the extra arguments (non-standard ones) we want to pass to
 		// the lookup DHT wrapper.
 		HashMap<String, Object> extra_args = new HashMap<String, Object>();
 		extra_args.put("contentId", contentId);
 		extra_args.put("PDU", new TagPDU(contentId, cp));
 
 		// Issue a lookup request to the underline DHT service
 		lookup(uid, TagContentMessage.TYPE, extra_args,
 				new StandardContinuation<Object, Exception>(command) {
 
 			public void receiveResult(Object result) {
 
 				// We expect a ContentCatalogEntry to be returned
 				if (result instanceof ContentCatalogEntry) {
 					ContentCatalogEntry cce = (ContentCatalogEntry) result;
 
 					// Now we can add the ContentCatalogEntry to each
 					// SocialCatalog (for each tag)
 					// Tagers inverted list.
 					if (tags != null)
 						for (ContentField cftag : tags.getAllFields()) {
 							String tag = cftag.getFieldName();
 							SocialCatalog scat = user.getTagContentList().get(tag);
 							if (scat == null)
 								scat = new SocialCatalog(tag);
 							Set<?> ccatEntries = scat.getContentCatalogEntries();
 							if (!ccatEntries.contains(cce))
 								scat.addContentCatalogEntry(cce);
 							user.addTagContentList(tag, scat);
 						}
 					// Wrap the result in a HashMap object together with a
 					// notification message
 					parent.receiveResult(wrapToResponse(
 							"Tagged content with checksum " + contentId +
 							" of " + uid, SUCCESS, cce));
 				} else {
 					parent.receiveResult(wrapToResponse(
 							"Failed to tag content with checksum " + contentId +
 							" of " + uid, FAILURE, result));
 				}
 			}
 			public void receiveException(Exception result) {
 				parent.receiveResult(wrapToResponse(
 						"Failed to tag content with checksum " + contentId +
 						" of " + uid, EXCEPTION, result));
 			}
 		});
 	}
 
 	/**
 	 * Index fuction for the specified URL profile (set of tags). It is
 	 * translated into an insert request to the underline network. An
 	 * URLCatalogEntry is added to the catalog node for the URL's TID.
 	 * 
 	 * @param url
 	 *            The destination address obtained by the URL's TID
 	 * @param tags
 	 *            The description keywords in a ContentProfile object
 	 * @param command
 	 *            Asynchronous commands to be executed after return
 	 */
 	public void indexURL(URL url, ContentProfile tags,
 			final Continuation<Object, Exception> command) {
 
 		if (this.user == null) {
 			command.receiveException(new RuntimeException("User has not be registered yet!"));
 			return;
 		}
 
 		final Id tid = factory.buildId(url.toString());
 
 		// Check the social bookmarks in the local user space first.
 		ContentProfile oldtags = tags;
 		Map<Id, SocialBookMark> bookMarks = user.getBookMarks();
 		if (bookMarks.containsKey(tid)
 				&& bookMarks.get(tid) instanceof URLBookMark) {
 			// 1.if there exist, update it
 
 			SocialBookMark ubm = bookMarks.get(tid);
 			oldtags = ubm.getTags();
 
 			for (Iterator<ContentField> it1 = oldtags.getAllFields().iterator(); it1.hasNext(); ) {
 				ContentField cf1 = it1.next();
 				for (Iterator<ContentField> it2 = tags.getAllFields().iterator(); it2.hasNext(); ) {
 					ContentField cf2 = it2.next();
 					if (cf1.getFieldName().equals(cf2.getFieldName())) {
 						if (cf1 instanceof TokenizedField
 								&& cf2 instanceof TokenizedField) {
 							// merge the tags and add up the frequencies
 							((TokenizedField)cf1).merge((TokenizedField)cf2);
 						} else {
 							String termA = null, termB = null;
 							if (!(cf1 instanceof TokenizedField)) {
 								termA = ((TermField)cf1).getFieldData();
 							}
 							if (!(cf2 instanceof TokenizedField)) {
 								termB = ((TermField)cf2).getFieldData();
 							}
 							if (termA != null && termB != null)
 								cf1 = new TermField(cf2.getFieldName(), termB);
 							else if (termA == null) {
 								((TokenizedField)cf1).addTerm(termB);
 							} else {
 								((TokenizedField)cf2).addTerm(termA);
 								cf1 = cf2;
 							}
 						}
 					} else {
 						oldtags.add(cf2);
 					}
 				}
 			}
 		} else {
 			// 2.if there is not, create it
 
 			user.addBookMark(tid, new URLBookMark(url, tags));
 		}
 
 		// Our data which will travel through the network!!!
 		URLCatalogEntry ue = new URLCatalogEntry(user.getUID(), oldtags, user
 				.getPublicUserProfile(), url);
 		PastContent pdu = new InsertPDU(tid, ue, null);
 		// Here is the message post
 		// Issue an insert request to the uderline DHT service
 		insert(pdu, new NamedContinuation(
 				"InsertMessage (InsertPDU - URLCatalogEntry) for " + tid,
 				command) {
 
 			@SuppressWarnings("unchecked")
 			public void receiveResult(Object result) {
 				if (result instanceof Boolean[]) {
 					System.out
 							.println("\n\nBookmark URL index process, result code : "
 									+ result);
 
 					parent.receiveResult(result);
 				} else {
 					System.out
 							.println("\n\nBookmark URL index process, result : something went wrong to the storing process");
 				}
 			}
 
 			@SuppressWarnings("unchecked")
 			public void receiveException(Exception result) {
 				System.out
 						.println("\n\nBookmark URL index process, result (exception) code : "
 								+ result.getMessage());
 				parent.receiveException(result);
 			}
 		});
 
 	}
 
 	/**
 	 * This method uses a ContentProfileFactory functions to form a set of
 	 * indexing terms for the specific File and inserts a ContentCatalogEntry to
 	 * every node which is responsible for the specific TID.
 	 * 
 	 * @param file
 	 */
 	@SuppressWarnings("rawtypes")
 	public void indexContent(final File file, final Continuation<Object, Exception> command) {
 
 		if (this.user == null) {
 			command.receiveException(new RuntimeException("User has not be registered yet!"));
 			return;
 		}
 
 		try {
 			// Metadata extraction, Field creation, Analysis, Tokenization, TF
 			// computation
 			final ContentProfile cp = cpf.buildContentProfile(file);
 			if (cp == null) {
 				command.receiveException(new RuntimeException("Empty profile!"));
 				return;
 			}
 
 			// Check in the user's sharedContent map to see if we have already
 			// indexed it!
 			List<ContentField> fields = cp.getPublicFields();
 			Iterator<ContentField> it = fields.listIterator();
 			ContentField cf;
 			Id checksum = null;
 			while (it.hasNext()) {
 				cf = it.next();
 				if (cf.getFieldName().equals("SHA-1")) {
 					checksum = factory.buildIdFromToString(((StoredField) cf)
 							.getFieldData());
 					System.out.println((user.getSharedContent().containsKey(checksum) ? "Rei" : "I") + "ndexing " + file.toString());
 					break;
 				}
 			}
 			if (checksum == null) {
 				// No checksum in file tags. Create an ID just from the file name
 				logger.log("No SHA-1 checksum! Computing an ID based on the file name alone.");
 				checksum = factory.buildId(file.getAbsolutePath());
 			}
 			// Convenience for use in MultiContinuation inner Class
 			final Id chsum = checksum;
 
 			// Our data which will travel through the network!!!
 			ContentCatalogEntry cce = new ContentCatalogEntry(user.getUID(),
 					cp, user.getPublicUserProfile());
 
 			// Create MultiContinuation
 			Set<String> indexingTerms = new HashSet<String>(); // holds the
 																	// indexing
 																	// terms
 																	// (Strings)
 			it = fields.listIterator();
 			while (it.hasNext()) {
 				cf = it.next();
 				if (cf instanceof TermField) { // These fields are indexed as
 												// whole
 					TermField termf = (TermField) cf;
 					indexingTerms.add(termf.getFieldData());
 
 				} else if (cf instanceof TokenizedField) { // These fields are
 															// indexed
 					TokenizedField tokf = (TokenizedField) cf;
 					String[] terms = tokf.getTerms();
 					for (int i = 0; i < terms.length; i++) {
 						indexingTerms.add(terms[i]);
 					}
 				}
 			}
 
 			int termscount = indexingTerms.size();
 			if (termscount == 0) {
 				command.receiveException(new RuntimeException("No terms to index!"));
 				return;
 			} else {
 
 				MultiContinuation multi = new MultiContinuation(command,
 						termscount) {
 
 					public boolean isDone() throws Exception {
 						int numSuccess = 0;
 						for (int i = 0; i < haveResult.length; i++)
 							if ((haveResult[i])
 									&& (result[i] instanceof Boolean[]))
 								numSuccess++;
 
 						if (numSuccess >= (SUCCESSFUL_INSERT_THRESHOLD * haveResult.length))
 							return true;
 
 						if (super.isDone()) {
 							for (int i = 0; i < result.length; i++)
 								if (result[i] instanceof Exception)
 									if (logger.level <= Logger.WARNING)
 										logger.logException("result[" + i
 												+ "]:", (Exception) result[i]);
 							throw new PastException("Had only " + numSuccess
 									+ " successful inserted indices out of "
 									+ result.length + " - aborting.");
 						}
 
 						return false;
 					}
 
 					// This is called once we have sent all the messages
 					// (signaling Success).
 					public Object getResult() {
 						Boolean[] b = new Boolean[result.length];
 						for (int i = 0; i < b.length; i++)
 							b[i] = Boolean.valueOf((result[i] == null)
 									|| result[i] instanceof Boolean[]);
 
 						// As we have sent all the necessary data, add the file
 						// to the user Map
 						if (chsum != null) {
 							user.addSharedContent(chsum, file);
 							user.addSharedContentProfile(chsum, null, cp);
 						}
 
 						return b;
 					}
 				};
 
 				int index = 0;
 				Id tid;
 				String term;
 				Iterator<String> iter = indexingTerms.iterator();
 				while (iter.hasNext()) {
 					term = iter.next();
 					tid = factory.buildId(term);
 					PastContent pdu = new InsertPDU(tid, cce, null);
 					Continuation c = new NamedContinuation(
 							"InsertMessage (InsertPDU) for " + tid, multi
 									.getSubContinuation(index));
 					index++;
 					insert(pdu, c); // Here is the message post
 					iter.remove();
 				}
 				// End of Multicontinuation
 			}
 		} catch (IOException e) {
 			command.receiveException(e);
 		}
 	}
 
 	private void catalogToTermVector(Vector<String> v, CatalogEntry ce) {
 		if (v == null || ce == null)
 			return;
 		if (ce instanceof UserCatalogEntry) {
 			ContentProfile cp = ((UserCatalogEntry)ce).getUserProfile();
 			if (cp != null && cp.getPublicFields().size() > 0) {
 				for (ContentField cf : cp.getPublicFields()) {
 					if (cf instanceof TermField) {
 						v.add(((TermField)cf).getFieldData());
 					} else if (cf instanceof TokenizedField) {
 						for (String s : ((TokenizedField)cf).getTerms())
 							v.add(s);
 					}
 				}
 			}
 		}
 		if (ce instanceof ContentCatalogEntry) {
 			ContentProfile cp = ((ContentCatalogEntry)ce).getContentProfile();
 			if (cp != null && cp.getPublicFields().size() > 0) {
 				for (ContentField cf : cp.getPublicFields()) {
 					if (cf instanceof TermField) {
 						v.add(((TermField)cf).getFieldData());
 					} else if (cf instanceof TokenizedField) {
 						for (String s : ((TokenizedField)cf).getTerms())
 							v.add(s);
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * This function indexes the user's profile keywords around the network.
 	 * 
 	 * @param command
 	 */
 	@SuppressWarnings("rawtypes")
 	public void indexUser(ContentProfile additions, ContentProfile deletions, final Continuation<Object, Exception> command) {
 
 		if (this.user == null) {
 			command.receiveException(new RuntimeException("User has not be registered yet!"));
 			return;
 		}
 
 		if (additions == null && deletions == null) {
 			command.receiveException(new RuntimeException("Empty profile!"));
 			return;
 		}
 
 		// Our data which will travel through the network!!!
 		UserCatalogEntry uceAdd = null, uceDel = null;
 		ContentProfile cpDel = null;
 		if (additions != null) {
 			uceAdd = new UserCatalogEntry(user.getUID(), additions);
 		}
 		if (deletions != null) {
 			cpDel = deletions.getPublicPart();
 			uceDel = new UserCatalogEntry(user.getUID(), deletions);
 		}
 
 		if (uceAdd != null && uceDel != null)
 			uceAdd.subtract(uceDel);
 
 		UserCatalogEntry uce = new UserCatalogEntry(user.getUID(), user.getCompleteUserProfile());
 		uce.add(uceAdd);
 		uce.subtract(uceDel);
 		user.setUserProfile(uce.getUserProfile());
 		uceAdd = new UserCatalogEntry(user.getUID(), uce.getUserProfile().getPublicPart());
 		uceDel = new UserCatalogEntry(user.getUID(), cpDel);
 
 		// Vector of indexing terms (Strings)
 		Vector<String> indexingTerms = new Vector<String>();
 		catalogToTermVector(indexingTerms, uce);
 		catalogToTermVector(indexingTerms, uceDel);
 
 		// Also add all terms from shared content profiles
 		Iterator<ContentProfile> itv = user.getSharedContentProfiles().values().iterator();
 		Iterator<Id> itk = user.getSharedContentProfiles().keySet().iterator();
 		while (itv.hasNext()) {
 			Id key = itk.next();
 			ContentProfile cp = itv.next().getPublicPart();
			cp.add(new StoredField("SHA-1", key.toStringFull()));
			catalogToTermVector(indexingTerms, new ContentCatalogEntry(user.getUID(), cp, null));
 		}
 
 		if (indexingTerms.size() == 0) {
 			command.receiveException(new RuntimeException("No terms to index!"));
 			return;
 		}
 
 		MultiContinuation multi = new MultiContinuation(command, indexingTerms.size()) {
 			public boolean isDone() throws Exception {
 				int numSuccess = 0;
 				for (int i = 0; i < haveResult.length; i++)
 					if ((haveResult[i]) && (result[i] instanceof Boolean[]))
 						numSuccess++;
 
 				if (numSuccess >= haveResult.length)
 					return true;
 
 				if (super.isDone()) {
 					for (int i = 0; i < result.length; i++)
 						if (result[i] instanceof Exception)
 							if (logger.level <= Logger.WARNING)
 								logger.logException("result[" + i + "]:",
 										(Exception) result[i]);
 					throw new PastException("Had only " + numSuccess
 							+ " successful inserted indices out of "
 							+ result.length + " - aborting.");
 				}
 
 				return false;
 			}
 
 			public Object getResult() {
 				Boolean[] b = new Boolean[result.length];
 
 				for (int i = 0; i < b.length; i++) {
 					if (result[i] != null && result[i] instanceof Boolean[]) {
 						// Check the replicas' return values for true to
 						// return true or false
 						Boolean temp[] = (Boolean[]) result[i];
 						b[i] = true;
 						// We want all the replicas to have been indexed (so all true)
 						for (int j = 0; j < temp.length && b[i]; j++) {
 							b[i] = temp[j];
 						}
 					} else { // If it is null or it is not of type Boolean[]
 						b[i] = false;
 					}
 				}
 				return b;
 			}
 		};
 
 		int index = 0;
 		for (String term : indexingTerms) {
 			Id tid = factory.buildId(term);
 			PastContent pdu = new InsertPDU(tid, uceAdd, uceDel);
 			Continuation c = new NamedContinuation(
 					"InsertMessage (InsertPDU) for " + tid + "(" + term + ")",
 					multi.getSubContinuation(index));
 			index++;
 			insert(pdu, c); // Here is the message post
 		}
 	}
 
 	/**
 	 * This function indexes the content profile keywords around the network. It
 	 * is used when pseudo content is going to be indexed.
 	 * 
 	 * @param command
 	 */
 	@SuppressWarnings("rawtypes")
 	public void indexPseudoContent(Id cid, String identifier,
 			final ContentProfile additions,
 			final ContentProfile deletions,
 			final Continuation<Object, Exception> command) {
 
 		if (this.user == null) {
 			command.receiveException(new RuntimeException("User has not be registered yet!"));
 			return;
 		}
 
 		// Handle the content profile, check if it is already indexed
 		if (additions == null && deletions == null) {
 			command.receiveException(new RuntimeException("Empty content profile!"));
 			return;
 		}
 
 		// Check in the user's sharedContent map to see if we have already
 		// indexed it!
 		final Id chsum = cid;
 		if (chsum == null) {
 			// No checksum in file tags. Create an ID just from the file name
 			logger.log("No SHA-1 checksum! Bailing out...");
 			command.receiveException(new PastException("No SHA-1 checksum!"));
 			return;
 		}
 
 		ContentCatalogEntry uceAdd = null, uceDel = null;
 		ContentProfile cpDel = null;
 		if (additions != null)
 			uceAdd = new ContentCatalogEntry(user.getUID(), additions, user.getPublicUserProfile());
 		if (deletions != null) {
 			cpDel = deletions.getPublicPart();
 			uceDel = new ContentCatalogEntry(user.getUID(), deletions, null);
 		}
 
 		if (uceAdd != null && uceDel != null)
 			uceAdd.subtract(uceDel);
 
 		ContentProfile cp = user.getSharedContentProfile(chsum);
 		if (cp == null)
 			cp = new ContentProfile(uceAdd.getContentProfile());
		ContentCatalogEntry cce = new ContentCatalogEntry(user.getUID(), cp, null);
 		cce.add(uceAdd);
 		cce.subtract(uceDel);
 		user.addSharedContentProfile(chsum, identifier, cce.getContentProfile());
		uceAdd = new ContentCatalogEntry(user.getUID(), cp.getPublicPart(), user.getPublicUserProfile());
		uceDel = (cpDel != null) ? new ContentCatalogEntry(user.getUID(), cpDel.getPublicPart(), null) : null;
 
 		// Vector of indexing terms (Strings)
 		Vector<String> indexingTerms = new Vector<String>();
 		catalogToTermVector(indexingTerms, cce);
 		catalogToTermVector(indexingTerms, uceDel);
 
 		int termscount = indexingTerms.size();
 		if (termscount == 0) {
 			command.receiveException(new RuntimeException("No terms to index!"));
 			return;
 		}
 
 		MultiContinuation multi = new MultiContinuation(command, termscount) {
 
 			public boolean isDone() throws Exception {
 				int numSuccess = 0;
 				for (int i = 0; i < haveResult.length; i++)
 					if ((haveResult[i]) && (result[i] instanceof Boolean[]))
 						numSuccess++;
 
 				if (numSuccess >= (1.0 * haveResult.length))
 					return true;
 
 				if (super.isDone()) {
 					for (int i = 0; i < result.length; i++)
 						if (result[i] instanceof Exception)
 							if (logger.level <= Logger.WARNING)
 								logger.logException("result[" + i + "]:",
 										(Exception) result[i]);
 					throw new PastException("Had only " + numSuccess
 							+ " successful inserted indices out of "
 							+ result.length + " - aborting.");
 				}
 
 				return false;
 			}
 
 			// This is called once we have sent all the messages (signaling
 			// Success).
 			public Object getResult() {
 				Boolean[] b = new Boolean[result.length];
 
 				for (int i = 0; i < b.length; i++) {
 					if (result[i] != null && result[i] instanceof Boolean[]) {
 						// Check the replicas' return values for true to
 						// return true or false
 						Boolean temp[] = (Boolean[]) result[i];
 						b[i] = Boolean.valueOf(false);
 						// We want all the replicas to have been indexed (so
 						// all true)
 						int j;
 						for (j = 0; j < temp.length; j++) {
 							if (temp[j] == false)
 								break;
 						}
 						// HERE WE WANT 100% SUCCESS IN REPLICATION INDEXING
 						if (j == temp.length)
 							b[i] = Boolean.valueOf(true);
 					} else { // If it is null or it is not of type Boolean[]
 						b[i] = Boolean.valueOf(false);
 					}
 				}
 				return b;
 			}
 		};
 
 
 		int index = 0;
 		for (String term : indexingTerms) {
 			Id tid = factory.buildId(term);
 			PastContent pdu = new InsertPDU(tid, uceAdd, uceDel);
 			Continuation c = new NamedContinuation(
 					"InsertMessage (InsertPDU) for " + tid + "(" + term + ")",
 					multi.getSubContinuation(index));
 			index++;
 			insert(pdu, c); // Here is the message post
 		}
 	}
 
 
 	/**
 	 * Wrapper for searchQuery to help searching for candidate friends.
 	 * 
 	 * The enhanced user query uses the user profile to find similar profiles
 	 * all over the network. This way a user can discover other users with
 	 * similar profiles which could be friend candidates.
 	 * Returns the top k catalog entries.
 	 */
 	public void discoverFriend(final String rawQuery, final int k,
 			final Continuation<Object, Exception> command) {
 		searchQuery(QueryPDU.USER_ENHANCEDQUERY, rawQuery, k,
 				ContentProfileFactory.DEFAULT_DELIMITER, command);
 	}
 
 	/**
 	 * Wrapper for searchQuery to help searching only for users.
 	 */
 	public void searchContent(final String rawQuery, final int k,
 			final Continuation<Object, Exception> command) {
 		searchQuery(QueryPDU.CONTENTQUERY, rawQuery, k, command);
 	}
 
 	/**
 	 * Wrapper for searchQuery to help searching only for users.
 	 */
 	public void searchUser(final String rawQuery, final int k,
 			final Continuation<Object, Exception> command) {
 		searchQuery(QueryPDU.USERQUERY, rawQuery, k, command);
 	}
 
 	/**
 	 * Wrapper for searchQuery
 	 */
 	public void searchQuery(final int queryType, final String rawQuery,
 			final int k, final Continuation<Object, Exception> command) {
 		searchQuery(queryType, rawQuery, k,
 				ContentProfileFactory.DEFAULT_DELIMITER, command);
 	}
 
 	/**
 	 * Wrapper for searchQuery
 	 */
 	public void searchQuery(final int queryType, final String rawQuery,
 			final int k, final String delimiter, final Continuation<Object, Exception> command) {
 		if (rawQuery == null || rawQuery.trim().equals(""))
 			command.receiveException(new Exception("Empty query"));
 
 		searchQuery(queryType, rawQueryTokenizer(rawQuery, delimiter), k, command);
 	}
 
 	private String[] rawQueryTokenizer(String rawQuery, String delimiter) {
 		// Elementary Query Parsing
 		// TODO : Examine doing this with JavaCC
 		Vector<String> tempQterms = new Vector<String>();
 		String query = rawQuery.trim();
 
 		boolean inQuotes = false;
 		int nextIndex, lastIndex = 0;
 		while ((nextIndex = query.indexOf('"', lastIndex)) != -1) {
 			String curTerm = query.substring(lastIndex, nextIndex).trim();
 			if (curTerm.length() > 0) {
 				if (inQuotes) {
 					tempQterms.add(curTerm);
 				} else {
 					tempQterms.addAll(Arrays.asList(curTerm.split(delimiter)));
 				}
 			}
 			inQuotes = !inQuotes;
 			lastIndex = nextIndex + 1;
 		}
 		if (lastIndex < query.length()) {
 			String curTerm = query.substring(lastIndex, query.length()).trim();
 			if (curTerm.length() > 0) {
 				if (inQuotes) {
 					tempQterms.add(curTerm);
 				} else {
 					tempQterms.addAll(Arrays.asList(curTerm.split(delimiter)));
 				}
 			}
 		}
 		if (tempQterms.size() > 0) {
 			return tempQterms.toArray(new String[1]);
 		}
 		return null;
 	}
 
 	private String[] termsToArray(int queryType, String[] queryTerms, int k) {
 		Set<String> terms = new HashSet<String>();
 		if (queryTerms != null)
 			for (int i = 0; i < queryTerms.length; i++)
 				if (queryTerms[i] != null && !queryTerms[i].equals("")) {
 					terms.add(queryTerms[i]);
 				}
 		String[] termsArray = null;
 
 		if (terms.size() == 0) {
 			if (queryType == QueryPDU.CONTENTQUERY)
 				queryType = QueryPDU.CONTENT_ENHANCEDQUERY;
 			else if (queryType == QueryPDU.USERQUERY)
 				queryType = QueryPDU.USER_ENHANCEDQUERY;
 			else if (queryType == QueryPDU.HYBRIDQUERY)
 				queryType = QueryPDU.HYBRID_ENHANCEDQUERY;
 		} else {
 			termsArray = new String[terms.size()];
 			termsArray = terms.toArray(termsArray);
 		}
 
 		if (queryType == QueryPDU.CONTENT_ENHANCEDQUERY
 				|| queryType == QueryPDU.USER_ENHANCEDQUERY
 				|| queryType == QueryPDU.HYBRID_ENHANCEDQUERY) {
 			if (terms.size() == 0) {
 				Vector<String> enhanced;
 				if (termsArray != null)
 					enhanced = new Vector<String>(Arrays.asList(termsArray));
 				else
 					enhanced = new Vector<String>();
 				termsArray = new String[1];
 				for (ContentField cf : user.getPublicUserProfile().getPublicFields()) {
 					if (cf instanceof TermField)
 						enhanced.add(((TermField)cf).getFieldData());
 					else if (cf instanceof TokenizedField) {
 						enhanced.addAll(Arrays.asList(((TokenizedField)cf).getTerms()));
 					}
 				}
 				termsArray = (enhanced.size() > 0) ? enhanced.toArray(termsArray) : null;
 			}
 		}
 		return termsArray;
 	}
 
 	/**
 	 * This method uses a query to search in the overlay network for occurrences
 	 * of the specific query terms. These terms may refer to a Content object
 	 * indexed in the network or some User or even both of these types.
 	 * Respectively, we have three query types : CONTENTQUERY, USERQUERY,
 	 * HYBRIDQUERY. New Feature: URLQUERY type is also offered now
 	 * 
 	 * @param queryType Specify the type of entities we search for.
 	 * @param queryTerns The terms given which will be searched.
 	 * @param k The number of results which are going to be returned as a
 	 *            list.
 	 * @param delimiter The delimiter for query terms
 	 * @param command A callback
 	 */
 	public void searchQuery(int queryType, final String[] queryTerms,
 			final int k, final Continuation<Object, Exception> command) {
 
 		if (this.user == null) {
 			command.receiveException(new RuntimeException("User has not been registered yet!"));
 			return;
 		}
 
 		String[] termsArray = termsToArray(queryType, queryTerms, k);
 
 		// Iterate to lookup for every term in query!
 		if (termsArray != null && termsArray.length > 0) {
 			final QueryPDU qPDU = new QueryPDU(termsArray, queryType, k,
 					(queryType == QueryPDU.CONTENT_ENHANCEDQUERY ||
 							queryType == QueryPDU.USER_ENHANCEDQUERY ||
 							queryType == QueryPDU.HYBRID_ENHANCEDQUERY) ?
 					this.user.getPublicUserProfile() : null);
 			for (int i = 0; i < termsArray.length; i++) {
 				// Compute each terms TID
 				final Id querytid = factory.buildId(termsArray[i]);
 
 				storage.getObject(querytid, new StandardContinuation<Object, Exception>(command) {
 					@SuppressWarnings("rawtypes")
 					public void receiveResult(Object o) {
 						if (o != null) {
 							// Do the similarity computation and scoring of terms and
 							// return a mini ScoredCatalog (PastContent)
 							if (o instanceof Catalog) {
 								int type = qPDU.getType();
 								Hashtable entries = ((Catalog)o).getCatalogEntriesForQueryType(type);
 								// Leave the job to be done asynchronously by the
 								// Scorer thread
 								scorer.addRequest(new SimilarityRequest(
 										entries.values(), qPDU.getData(), type,
 										qPDU.getK(),
 										qPDU.getSourceUserProfile(), parent, 0));
 								scorer.doNotify();
 							} else {
 								// debugging only
 								if (logger.level <= Logger.INFO)
 									logger.log("Error: o is not Catalog (in deliver)");
 								// send result back
 								parent.receiveResult(new ResponsePDU(0));
 							}
 						} else {
 							// send the request across the wire, and see if the result
 							// is null or not
 							sendRequest(querytid, new QueryMessage(getUID(), querytid,
 									getLocalNodeHandle(), querytid, qPDU),
 									new NamedContinuation("QueryMessage for " + querytid, this) {
 								public void receiveResult(final Object o) {
 									// if we have an object, we return it otherwise, we must check all replicas in
 									// order to make sure that the object doesn't exist anywhere
 									if (o != null) {
 										command.receiveResult(o);
 									} else {
 										lookupHandles(querytid, replicationFactor + 1,
 												new Continuation<Object, Exception>() {
 											@SuppressWarnings("unchecked")
 											public void receiveResult(Object o) {
 												PastContentHandle[] handles = (PastContentHandle[]) o;
 
 												for (int i = 0; i < handles.length; i++) {
 													if (handles[i] != null) {
 														fetch(handles[i],
 																new StandardContinuation<Object, Exception>(parent) {
 															public void receiveResult(final Object o) {
 																command.receiveResult(o);
 															}
 														});
 														return;
 													}
 												}
 
 												// there were no replicas of the object
 												command.receiveResult(null);
 											}
 
 											public void receiveException(Exception e) {
 												command.receiveException(e);
 											}
 										});
 									}
 								}
 
 								public void receiveException(Exception e) {
 									// If the lookup message failed , we then
 									// try to fetch all of the handles, just
 									// in case. This may fail too, but at least
 									// we tried.
 									receiveResult(null);
 								}
 							});
 						}
 					}
 				});
 			}
 		} else {
 			command.receiveException(new Exception("Empty query"));
 		}
 	}
 
 	public void searchFriendsNetwork(final int queryType, final String rawQuery,
 			final int k, final Continuation<Object, Exception> command) {
 		if (rawQuery == null || rawQuery.trim().equals(""))
 			command.receiveException(new Exception("Empty query"));
 
 		searchFriendsNetwork(queryType, rawQueryTokenizer(rawQuery, ContentProfileFactory.DEFAULT_DELIMITER), k, command);
 	}
 
 	/**
 	 * This method uses a query to search in every friend node.
 	 * 
 	 * Three query types are supported: CONTENTQUERY, USERQUERY,
 	 * HYBRIDQUERY.
 	 * 
 	 * @param queryType The type of the query.
 	 * @param queryTerms The set of terms to search for
 	 * @param command The callback which will be called on response.
 	 */
 	public void searchFriendsNetwork(final int queryType,
 			final String[] queryTerms, int topk, final Continuation<Object, Exception> command) {
 
 		if (this.user == null) {
 			command.receiveException(new RuntimeException("User has not be registered yet!"));
 			return;
 		}
 
 		final String[] termsArray = termsToArray(queryType, queryTerms, topk);
 
 		Hashtable<Id, Friend> friends = user.getFriends();
 		Id destId = null;
 		NodeHandle nodeHandle = null;
 		int nQueriesSent = 0;
 
 		// Iterate to lookup for every node we want to visit!
 		for (Friend friend : friends.values()) {
 
 			// Get the UID of the specific friend
 			destId = friend.getUID();
 			NodeHandleSet nhset = this.getEndpoint().localLookup(friend.getUID(), 1, true);
 			if (nhset == null || nhset.size() < 1)
 				continue;
 			NodeHandle nh = nhset.getHandle(0);
 			if (nh == null || nh.checkLiveness() == false)
 			continue;
 			nodeHandle = nh;
 			final Id uid = destId;
 			final QueryPDU qPDU;
 
 			qPDU = new QueryPDU(termsArray, queryType, topk, 
 					(queryType == QueryPDU.CONTENT_ENHANCEDQUERY ||
 							queryType == QueryPDU.USER_ENHANCEDQUERY ||
 							queryType == QueryPDU.HYBRID_ENHANCEDQUERY) ? user.getCompleteUserProfile() : null);
 
 			// send the request across the wire, and see if the result is null or not
 			// Use nodeHandle as first hop hint!
 			sendRequest(uid,
 					new FriendQueryMessage(getUID(), getLocalNodeHandle(), uid, qPDU),
 					nodeHandle,
 					new NamedContinuation("FriendQueryMessage for " + uid, command) {
 				public void receiveResult(final Object o) {
 					// if we have an object, we return it otherwise, we must check all replicas in order to make sure
 					// that the object doesn't exist anywhere
 					if (o != null) {
 						command.receiveResult(o);
 					} else {
 						// TODO : examine if the tager's arrays need to be replicated to the leafset
 						// If so then here we should put the lookupHandles code as above!!!
 						command.receiveResult(null); // o is NULL
 					}
 				}
 
 				public void receiveException(Exception e) {
 					// If the lookup message failed , we then try to fetch all of
 					// the handles, just in case. This may fail too, but at least we tried.
 					receiveResult(null);
 				}
 			});
 			nQueriesSent++;
 		}
 		if (nQueriesSent == 0)
 			command.receiveResult(null);
 	}
 
 	/**
 	 * This method uses a query of terms to search in every visited node. Social
 	 * Tags are the target. Specifically, in the set of nodes we visit (e.g.
 	 * friends, neighbors etc.), Social Catalogs of every tag term are checked
 	 * for Content or User entries, and the most relevant in some manner are
 	 * returned.
 	 * 
 	 * 
 	 * Respectively, we have three query types : CONTENTQUERY, USERQUERY,
 	 * HYBRIDQUERY. New Feature: URLQUERY type is also offered now
 	 * 
 	 * @param queryType
 	 * @param queryOld
 	 * @param command
 	 */
 	public void searchSocialTagsQuery(final int queryType, final String[] tags,
 			final String[] userIds, final Continuation<Object, Exception> command) {
 
 		if (this.user == null) {
 			command.receiveException(new RuntimeException("User has not be registered yet!"));
 			return;
 		}
 
 		Id destId = null;
 		// Iterate to lookup for every node we want to visit!
 		for (int i = 0; i < userIds.length; i++) {
 			// Compute each terms TID
 			destId = factory.buildIdFromToString(userIds[i]);
 
 			final Id uid = destId;
 			final SocialQueryPDU sqPDU;
 			if (queryType == QueryPDU.CONTENT_ENHANCEDQUERY
 					|| queryType == QueryPDU.USER_ENHANCEDQUERY
 					|| queryType == QueryPDU.HYBRID_ENHANCEDQUERY) {
 				sqPDU = new SocialQueryPDU(tags, queryType, this.user
 						.getCompleteUserProfile());
 			} else {
 				sqPDU = new SocialQueryPDU(tags, queryType);
 			}
 
 			storage.getObject(uid, new StandardContinuation<Object, Exception>(command) {
 				public void receiveResult(Object o) {
 					if (o != null) {
 						// TODO : Maybe we want to SCORE this local Catalog
 						command.receiveResult(o); // If we find here then we execute command
 					} else {
 						// send the request across the wire, and see if the result is null or not
 						sendRequest(uid, new SocialQueryMessage(getUID(), uid,
 								getLocalNodeHandle(), uid, sqPDU),
 								new NamedContinuation("SocialQueryMessage for " + uid, this) {
 							public void receiveResult(final Object o) {
 								// if we have an object, we return it otherwise, we must check all replicas in
 								// order to make sure that the object doesn't exist anywhere
 								if (o != null) {
 									// lastly, try and cache object locally for future use
 									command.receiveResult(o);
 								} else {
 									lookupHandles(uid, replicationFactor + 1, new Continuation<Object, Exception>() {
 										@SuppressWarnings("unchecked")
 										public void receiveResult(Object o) {
 											PastContentHandle[] handles = (PastContentHandle[]) o;
 
 											for (int i = 0; i < handles.length; i++) {
 												if (handles[i] != null) {
 													fetch(handles[i],new StandardContinuation<Object, Exception>(parent) {
 														public void receiveResult(final Object o) {
 															command.receiveResult(o);
 														}
 													});
 													return;
 												}
 											}
 
 											// there were no replicas of the object
 											command.receiveResult(null);
 										}
 
 										public void receiveException(Exception e) {
 											command.receiveException(e);
 										}
 									});
 								}
 							}
 
 							public void receiveException(Exception e) {
 								// If the lookup message failed , we then try to fetch all of the handles, just
 								// in case. This may fail too, but at least we tried.
 								receiveResult(null);
 							}
 						});
 					}
 				}
 			});
 		}
 	}
 
 	/**
 	 * Utility function to print the results returned by the search query.
 	 * 
 	 * @param result
 	 *            The array of results, result[i] could be null or ScoreBoard
 	 *            instance.
 	 * @param type
 	 *            0=CONTENT, 1=USER, 2=HYBRID
 	 * @param k
 	 *            The number of results to be returned.
 	 * @return
 	 * @throws PastException
 	 */
 	public static String printTopKQueryResults(Object[] result, int type, int k) throws PastException {
 
 		StringBuffer buffer = new StringBuffer();
 		ScoreBoard sc = null;
 		boolean done = false;
 		ContentProfile cprof;
 		Set<ContentField> listFields;
 		Iterator<ContentField> itf;
 		ContentField cf;
 
 		Random random = new Random(System.currentTimeMillis());
 
 		if (type == CONTENT) {
 			float max = 0, current_max = 0;
 			int point_vector = 0, resnum = 1;
 			// To include only the discreet results (not the duplicates)
 			Vector<ContentCatalogEntry> printed = new Vector<ContentCatalogEntry>();
 			// Corresponding Scores to be printed
 			Vector<Float> scoresPrinted = new Vector<Float>();
 			// Here we put the CCEs which will be chosen in a random manner
 			Vector<ContentCatalogEntry> randomSet = new Vector<ContentCatalogEntry>();
 			while (!done) {
 				point_vector = -1;
 				max = 0;
 				for (int i = 0; i < result.length; i++) {
 					if (result[i] != null && result[i] instanceof ScoreBoard) {
 						sc = (ScoreBoard) result[i];
 						if (sc.getScores() != null
 								&& !sc.getScores().isEmpty()
 								&& !sc.getCatalogEntries().isEmpty()
 								&& sc.getScores().firstElement().floatValue() >= max) {
 							max = sc.getScores().firstElement().floatValue();
 							point_vector = i;
 						}
 					} else {
 						throw new PastException("Unexpected class " + result[i].getClass().getName());
 					}
 					// Now remove the topmost as it is the maximum and check if
 					// we finished
 					if (i == (result.length - 1)) {
 						// We reach the end of the results
 						if (point_vector == -1) {
 							// FIll in the printed vector and break the loop
 							if (k != QueryPDU.RETURN_ALL
 									&& printed.size() + randomSet.size() >= k) {
 								// How many to print from the randomSet?
 								int answer = k - printed.size();
 								int choice;
 								// Pick radomly a CCE and put it in the printed
 								for (int l = 0; l < answer; l++) {
 									choice = random.nextInt(randomSet.size());
 									printed.add(randomSet.get(choice));
 									randomSet.remove(choice);
 									scoresPrinted.add(current_max);
 								}
 								done = true;
 								break;
 							} else {
 								// Add the pending results
 								printed.addAll(randomSet);
 								// Put the corresponding score values
 								for (int l = 0; l < randomSet.size(); l++) {
 									scoresPrinted.add(current_max);
 								}
 							}
 
 							done = true;
 							break;
 						}
 						sc = (ScoreBoard) result[point_vector];
 						// Only if the ContentCatalogEntries are non null, non
 						// empty
 						if (sc.getCatalogEntries() != null
 								&& !sc.getCatalogEntries().isEmpty()) {
 
 							ContentCatalogEntry cce = (ContentCatalogEntry) sc
 									.getCatalogEntries().firstElement();
 
 							// If we have a different score then we reset
 							// randomSet
 							// and check if we reach the end of the top k list
 							if (max != current_max) {
 								// Finish our list and dont include the new CCE
 								// as it is out of the important top k!
 								if (k != QueryPDU.RETURN_ALL
 										&& printed.size() + randomSet.size() >= k) {
 									// How many to print from the randomSet?
 									int answer = k - printed.size();
 									int choice;
 									// Pick radomly a CCE and put it in the
 									// printed
 									for (int l = 0; l < answer; l++) {
 										choice = random.nextInt(randomSet
 												.size());
 										printed.add(randomSet.get(choice));
 										randomSet.remove(choice);
 										// Put the corresponding score values
 										scoresPrinted.add(current_max);
 									}
 									done = true;
 									break;
 								} else {
 									// Add the pending results
 									printed.addAll(randomSet);
 									// Put the corresponding score values
 									for (int l = 0; l < randomSet.size(); l++) {
 										scoresPrinted.add(current_max);
 									}
 									current_max = max;
 									// and reset the randomSet
 									randomSet.clear();
 									// Now add the new CCE
 									randomSet.add(cce);
 								}
 								// If we have the same score add the cce to the
 								// randomSet
 							} else {
 								// Except for duplicates!
 								if (!randomSet.contains(cce))
 									randomSet.add(cce);
 							}
 
 							// Remove the touched entry
 							sc.getScores().remove(0);
 							sc.getCatalogEntries().remove(0);
 						}
 					}
 				}
 			}
 
 			// Print (append) the printed Vector
 			// ///////////////////////////////////////////////////////
 			Iterator<ContentCatalogEntry> it = printed.iterator();
 			Iterator<Float> it2 = scoresPrinted.iterator();
 			ContentCatalogEntry tempCCE;
 			while (it.hasNext()) {
 				tempCCE = it.next();
 				buffer.append("\n\n" + resnum++ + ". ");
 				buffer.append("User : " + tempCCE.getUID().toStringFull()
 						+ "\n");
 				buffer.append("   Content Profile : {");
 				cprof = tempCCE.getContentProfile();
 				listFields = cprof.getAllFields();
 				itf = listFields.iterator();
 				// Fore every ContentField
 				while (itf.hasNext()) {
 					cf = itf.next();
 					if (cf instanceof TermField) {
 						buffer.append(((TermField)cf).toString());
 					} else if (cf instanceof StoredField) {
 						buffer.append(((StoredField)cf).toString());
 					} else {
 						buffer.append(((TokenizedField)cf).toString());
 					}
 				}
 				buffer.append(" }\n");
 				// The corresponding score value
 				buffer.append("   Score : " + it2.next());
 			}
 			// ///////////////////////////////////////////////////////////////////
 
 		} else if (type == USER) {
 			float max = 0, current_max = 0;
 			int point_vector = 0, resnum = 1;
 			// To include only the discreet results (not the duplicates)
 			Vector<UserCatalogEntry> printed = new Vector<UserCatalogEntry>();
 			// Corresponding Scores to be printed
 			Vector<Float> scoresPrinted = new Vector<Float>();
 			// Here we put the CCEs which will be chosen in a random manner
 			Vector<UserCatalogEntry> randomSet = new Vector<UserCatalogEntry>();
 			while (!done) {
 				point_vector = -1;
 				max = 0;
 				for (int i = 0; i < result.length; i++) {
 					if (result[i] != null && result[i] instanceof ScoreBoard) {
 						sc = (ScoreBoard) result[i];
 						if (sc.getScores() != null
 								&& !sc.getScores().isEmpty()
 								&& !sc.getCatalogEntries().isEmpty()
 								&& sc.getScores().firstElement().floatValue() >= max) {
 							max = sc.getScores().firstElement().floatValue();
 							point_vector = i;
 						}
 					} else {
 						throw new PastException("Unexpected class " + result[i].getClass().getName());
 					}
 					// Now remove the topmost as it is the maximum and check if
 					// we finished
 					if (i == (result.length - 1)) {
 						// We reach the end of the results
 						if (point_vector == -1) {
 							// FIll in the printed vector and break the loop
 							if (k != QueryPDU.RETURN_ALL
 									&& printed.size() + randomSet.size() >= k) {
 								// How many to print from the randomSet?
 								int answer = k - printed.size();
 								int choice;
 								// Pick radomly a CCE and put it in the printed
 								for (int l = 0; l < answer; l++) {
 									choice = random.nextInt(randomSet.size());
 									printed.add(randomSet.get(choice));
 									randomSet.remove(choice);
 									scoresPrinted.add(current_max);
 								}
 								done = true;
 								break;
 							} else {
 								// Add the pending results
 								printed.addAll(randomSet);
 								// Put the corresponding score values
 								for (int l = 0; l < randomSet.size(); l++) {
 									scoresPrinted.add(current_max);
 								}
 							}
 
 							done = true;
 							break;
 						}
 						sc = (ScoreBoard) result[point_vector];
 						// Only if the ContentCatalogEntries are non null, non
 						// empty
 						if (sc.getCatalogEntries() != null
 								&& !sc.getCatalogEntries().isEmpty()) {
 
 							UserCatalogEntry uce = (UserCatalogEntry)sc
 								.getCatalogEntries().firstElement();
 
 							// If we have a different score then we reset
 							// randomSet
 							// and check if we reach the end of the top k list
 							if (max != current_max) {
 								// Finish our list and dont include the new CCE
 								// as it is out of the important top k!
 								if (k != QueryPDU.RETURN_ALL
 										&& printed.size() + randomSet.size() >= k) {
 									// How many to print from the randomSet?
 									int answer = k - printed.size();
 									int choice;
 									// Pick radomly a UCE and put it in the
 									// printed
 									for (int l = 0; l < answer; l++) {
 										choice = random.nextInt(randomSet
 												.size());
 										printed.add(randomSet.get(choice));
 										randomSet.remove(choice);
 										// Put the corresponding score values
 										scoresPrinted.add(current_max);
 									}
 									done = true;
 									break;
 								} else {
 									// Add the pending results
 									printed.addAll(randomSet);
 									// Put the corresponding score values
 									for (int l = 0; l < randomSet.size(); l++) {
 										scoresPrinted.add(current_max);
 									}
 									current_max = max;
 									// and reset the randomSet
 									randomSet.clear();
 									// Now add the new CCE
 									randomSet.add(uce);
 								}
 								// If we have the same score add the cce to the
 								// randomSet
 							} else {
 								// Except for duplicates!
 								if (!randomSet.contains(uce))
 									randomSet.add(uce);
 							}
 
 							// Remove the touched entry
 							sc.getScores().remove(0);
 							sc.getCatalogEntries().remove(0);
 						}
 					}
 				}
 			}
 
 			// Print (append) the printed Vector
 			// ///////////////////////////////////////////////////////
 			Iterator<UserCatalogEntry> it = printed.iterator();
 			Iterator<Float> it2 = scoresPrinted.iterator();
 			UserCatalogEntry tempUCE;
 			while (it.hasNext()) {
 				tempUCE = it.next();
 				buffer.append("\n\n" + resnum++ + ". ");
 				buffer.append("User : " + tempUCE.getUID().toStringFull()
 						+ "\n");
 				buffer.append("   User Profile : {");
 				cprof = tempUCE.getUserProfile();
 				listFields = cprof.getAllFields();
 				itf = listFields.iterator();
 				// Fore every ContentField
 				while (itf.hasNext()) {
 					cf = itf.next();
 					if (cf instanceof TermField) {
 						buffer.append(((TermField)cf).toString());
 					} else if (cf instanceof StoredField) {
 						buffer.append(((StoredField)cf).toString());
 					} else {
 						buffer.append(((TokenizedField)cf).toString());
 					}
 				}
 				buffer.append(" }\n");
 				// The corresponding score value
 				buffer.append("   Score : " + it2.next());
 			}
 			// ///////////////////////////////////////////////////////////////////
 
 		} else if (type == HYBRID) {
 			// TODO: Implement this
 		}
 		return buffer.toString();
 	}
 
 	/**
 	 * Getter for the user private object. Caution! This should be checked for
 	 * security holes in a real deployment.
 	 * 
 	 * @return
 	 */
 	public User getUser() {
 		return user;
 	}
 
 	public IdFactory getFactory() {
 		return this.factory;
 	}
 
 	/**
 	 * Utility method to form a common response HashMap for the next processing
 	 * level.
 	 * 
 	 * @param message
 	 * @param status
 	 * @param data
 	 */
 	HashMap<String,Object> wrapToResponse(String message, int status,
 			Object data){
 		HashMap<String,Object> response = new HashMap<String,Object>();
 		response.put("message", message);
 		response.put("status", status);
 		response.put("data", data);
 		return response;
 	}
 
 	/**
 	 * This method is called on the application at the destination node for the
 	 * given id.
 	 * 
 	 * @param id
 	 *            The destination id of the message
 	 * @param message
 	 *            The message being sent
 	 */
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	@Override
 	public void deliver(Id id, Message message) {
 			if (logger.level <= Logger.INFO)
 				logger.log("Received message " + message + " with destination "
 						+ id);
 
 			if (message instanceof InsertMessage ||
 					message instanceof LookupMessage ||
 					message instanceof LookupHandlesMessage ||
 					message instanceof FetchMessage ||
 					message instanceof FetchHandleMessage ||
 					message instanceof CacheMessage ||
 					((PastMessage)message).isResponse()) {
 				super.deliver(id, message);
 				return;
 			}
 
 			final PastMessage msg = (PastMessage) message;
 			if (logger.level <= Logger.FINER)
 				logger.log("Node " + this.getUser().getUID() + " (" + getUser().getScreenName() +
 						") got a " + message.getClass().getName() +
 						" destined to " + msg.getDestination());
 			if (msg instanceof QueryMessage) {
 				final QueryMessage qmsg = (QueryMessage) msg;
 				lookups++;
 
 				// if the data is here, we send the reply.
 				// TODO: we may want to  push a cached copy back to the previous node
 				storage.getObject(qmsg.getId(), new StandardContinuation<Object, Exception>(
 						getResponseContinuation(qmsg)) {
 					public void receiveResult(Object o) {
 						if (logger.level <= Logger.FINE)
 							logger.log("Received object " + o + " for id "
 									+ qmsg.getId());
 
 						// Do the similarity computation and scoring of terms
 						// and return a mini ScoredCatalog (PastContent)
 						if (o instanceof Catalog) {
 							int type = qmsg.getQueryPDU().getType();
 							Hashtable entries = ((Catalog) o).getCatalogEntriesForQueryType(type);
 							// Leave the job to be done asynchronously by the
 							// Scorer thread
 							scorer.addRequest(new SimilarityRequest(
 									entries.values(), qmsg.getQueryPDU().getData(),
 									type, qmsg.getQueryPDU().getK(),
 									qmsg.getQueryPDU().getSourceUserProfile(),
 									parent,	qmsg.getHops()));
 							scorer.doNotify();
 						} else {
 							// debugging only
 							if (logger.level <= Logger.INFO)
 								System.out.println("Error: o is not Catalog (in deliver)");
 							// send result back
 							parent
 									.receiveResult(new ResponsePDU(qmsg
 											.getHops()));
 						}
 
 						// // if possible, push copy into previous hop cache
 						// if ((lmsg.getPreviousNodeHandle() != null) &&
 						// (o != null) &&
 						// (! ((PastContent) o).isMutable())) {
 						// NodeHandle handle = lmsg.getPreviousNodeHandle();
 						// if (logger.level <= Logger.FINE)
 						// logger.log("Pushing cached copy of " + ((PastContent)
 						// o).getId() + " to " + handle);
 						//
 						// CacheMessage cmsg = new CacheMessage(getUID(),
 						// (PastContent) o, getLocalNodeHandle(),
 						// handle.getId());
 						// //endpoint.route(null, cmsg, handle);
 						// }
 					}
 				});
 
 			} else if (msg instanceof FriendQueryMessage) {
 				final FriendQueryMessage qmsg = (FriendQueryMessage) msg;
 				lookups++;
 
 				int type = qmsg.getQueryPDU().getType();
 				Vector<?> entries = this.user.
 					getCatalogEntriesForQueryType(type,qmsg.getSource().getId());
 				// Leave the job to be done asynchronously by the
 				// Scorer thread
 				scorer.addRequest(new SimilarityRequest(
 						entries, qmsg.getQueryPDU().getData(),
 						type, qmsg.getQueryPDU().getK(),
 						qmsg.getQueryPDU().getSourceUserProfile(),
 						getResponseContinuation(qmsg),	qmsg.getHops()));
 				scorer.doNotify();
 
 			} else if (msg instanceof GetUserProfileMessage) {
 				final GetUserProfileMessage gupmsg = (GetUserProfileMessage) msg;
 				lookups++;
 
 				if (logger.level <= Logger.FINER)
 					logger.log("Returning response for get user profile message "
 							+ gupmsg.getId() + " from " + endpoint.getId());
 
 				// Get and return a version of the user profile.
 				getResponseContinuation(msg).receiveResult(
 						user.isFriend(msg.getSource().getId())?
 								user.getCompleteUserProfile():
 									user.getPublicUserProfile());
 
 			} else if (msg instanceof SocialQueryMessage) {
 				final SocialQueryMessage sqmsg = (SocialQueryMessage) msg;
 				lookups++;
 
 				// Here we are in Tager's side.
 				SocialQueryPDU sqpdu = sqmsg.getQueryPDU();
 				String[] qtags = sqpdu.getData();
 
 				// The Vector we are going to fill in
 				Vector<SocialCatalog> vec = new Vector<SocialCatalog>();
 
 				// Search Social Catalogs corresponding to our query terms.
 				Map<String, SocialCatalog> invertedMap = user
 						.getTagContentList();
 				// SocialCatalog scat=null;
 				if (qtags != null)
 					for (String term : qtags) {
 						if (invertedMap.containsKey(term)) {
 							vec.add(invertedMap.get(term));
 						}
 					}
 
 				if (logger.level <= Logger.FINER)
 					logger
 							.log("Returning response for social content query message "
 									+ sqmsg.getId()
 									+ " from "
 									+ endpoint.getId());
 
 				// All was right! Now let's return the ContentCatalogEntry
 				getResponseContinuation(msg).receiveResult(vec);
 
 			} else if (msg instanceof FriendAcceptMessage) {
 				final FriendAcceptMessage famsg = (FriendAcceptMessage) msg;
 				lookups++;
 
 				Id fid = famsg.getSource().getId();
 				user.removePendingOutgoingFReq(fid);
 
 				// Now they are FRIENDS!
 				user.addFriend(new Friend(fid,
 						famsg.getFriendReqPDU().getScreenName(),
 						famsg.getSource()));
 
 				if (logger.level <= Logger.FINER)
 					logger.log("Returning response for FriendAcceptMessage "
 							+ fid + " from " + endpoint.getId());
 
 				// Return the response now.
 				getResponseContinuation(msg).receiveResult(Boolean.valueOf(true));
 			} else if (msg instanceof FriendRejectMessage) {
 				final FriendRejectMessage frmsg = (FriendRejectMessage) msg;
 				lookups++;
 
 				Id fid = frmsg.getSource().getId();
 				user.removePendingOutgoingFReq(fid);
 
 				if (logger.level <= Logger.FINER)
 					logger.log("Returning response for FriendAcceptMessage "
 							+ fid + " from " + endpoint.getId());
 
 				// Return the response now.
 				getResponseContinuation(msg).receiveResult(Boolean.valueOf(true));
 			} else if (msg instanceof FriendReqMessage) {
 				final FriendReqMessage frmsg = (FriendReqMessage) msg;
 				lookups++;
 
 				user.addPendingIncomingFReq(new FriendRequest(frmsg.getFriendReqPDU(),
 						frmsg.getSource().getId(),
 						frmsg.getSource()));
 				if (logger.level <= Logger.FINER)
 					logger.log("Returning response for friendrequest message "
 							+ frmsg.getSource().getId() + " from " + endpoint.getId());
 
 				// All was right!
 				getResponseContinuation(msg).receiveResult(Boolean.valueOf(true));
 			} else if (msg instanceof TagContentMessage) {
 				final TagContentMessage tcmsg = (TagContentMessage) msg;
 				lookups++;
 
 				// Now update the TagCloud +1 term freqs.
 				TagPDU tpdu = tcmsg.getTagContentPDU();
 				Id contentId = tpdu.getTaggedId();
 				Map<Id, TagCloud> mapCloud = user.getContentTagClouds();
 				TagCloud cloud;
 				if (mapCloud.containsKey(contentId)) {
 					cloud = mapCloud.get(contentId);
 				} else { // If the TagCloud does not exist, we create it
 					cloud = new TagCloud();
 					mapCloud.put(contentId, cloud);
 				}
 
 				// TODO : Implement association with User-tagers in the TagCloud
 				ContentProfile tags = tpdu.getTags();
 				if (tags != null)
 					for (ContentField tag : tags.getAllFields()) {
 						cloud.addTagTFMap(tag);
 					}
 
 				if (logger.level <= Logger.FINER)
 					logger.log("Returning response for tagcontent message "
 							+ tcmsg.getId() + " from " + endpoint.getId());
 
 				// All was right! Now let's return the ContentCatalogEntry
 				getResponseContinuation(msg).receiveResult(
 						new ContentCatalogEntry(user.getUID(),
 								user.getSharedContentProfile(tpdu.getTaggedId()),
 								user.getPublicUserProfile()));
 
 			} else if (msg instanceof TagUserMessage) {
 				final TagUserMessage tcmsg = (TagUserMessage) msg;
 				lookups++;
 
 				TagPDU tpdu = tcmsg.getTagUserPDU();
 				Id taggerId = endpoint.getId();
 				Map<Id, TagCloud> mapCloud = user.getUserTagClouds();
 				TagCloud cloud;
 				if (mapCloud.containsKey(taggerId)) {
 					cloud = mapCloud.get(taggerId);
 					cloud.getTagTFMap().clear();
 				} else { // If the TagCloud does not exist, we create it
 					cloud = new TagCloud();
 					mapCloud.put(taggerId, cloud);
 				}
 				// +1
 				// TODO : Implement association with User-tagers in the TagCloud
 				ContentProfile tags = tpdu.getTags();
 				if (tags != null)
 					for (ContentField tag : tags.getAllFields()) {
 						cloud.addTagTFMap(tag);
 					}
 
 				ContentProfile cp = new ContentProfile();
 				for (ContentField tag : cloud.getTagTFMap().keySet()) {
 					cp.add(tag);
 				}
 
 				if (logger.level <= Logger.FINER)
 					logger.log("Returning response for tagcontent message "
 							+ tcmsg.getUID() + " from " + taggerId);
 
 				// All was right! Now let's return the ContentCatalogEntry
 				getResponseContinuation(msg).receiveResult(
 						new UserCatalogEntry(user.getUID(), cp));
 
 			} else if (msg instanceof RetrieveContMessage) {
 				final RetrieveContMessage rcmsg = (RetrieveContMessage) msg;
 				lookups++;
 
 				final Vector<Object> ret = new Vector<Object>();
 
 				// Now get the tagclouds if requested
 				final RetrieveContPDU rcpdu = rcmsg.getRetrieveContPDU();
 				final Id contentId = rcpdu.getContentId();
 
 				// TODO : Here we must handle the downloading of the content
 				storage.getObject(contentId, new StandardContinuation<Object, Exception>(
 						getResponseContinuation(msg)) {
 					public void receiveResult(Object o) {
 						ret.add(Boolean.TRUE);
 						if (o instanceof Serializable) {
 							ret.add(o);
 						} else {
 							SimpleOutputBuffer sob = new SimpleOutputBuffer();
 							try {
 								JavaSerializer.serialize(sob, o);
 							} catch (IOException e) {
 								logger.logException("Error serializing content", e);
 								sob = new SimpleOutputBuffer();
 							}
 							ret.add(sob.getBytes());
 						}
 						if (rcpdu.getCloudFlag()) {
 							Map<Id, TagCloud> mapCloud = user.getContentTagClouds();
 							TagCloud cloud;
 							cloud = mapCloud.get(contentId);
 							ret.add(cloud);
 						}
 
 						if (logger.level <= Logger.FINER)
 							logger
 									.log("Returning response for retrieve content message "
 											+ rcmsg.getId()
 											+ " from "
 											+ endpoint.getId());
 
 						// All was right! Now return the TagCloud or the success
 						// indicator Boolean
 						parent.receiveResult(ret);
 					}
 
 					public void receiveException(Exception e) {
 						ret.add(Boolean.FALSE);
 						parent.receiveResult(ret);
 					}
 				});
 
 
 			} else if (msg instanceof RetrieveContTagsMessage) {
 				final RetrieveContTagsMessage rcmsg = (RetrieveContTagsMessage)msg;
 				lookups++;
 
 				// Now get the tagclouds if requested
 				RetrieveContPDU rcpdu = rcmsg.getRetrieveContPDU();
 				Id contentId = rcpdu.getContentId();
 				ContentProfile cp = user.getSharedContentProfile(contentId).getPublicPart();
 
 				if (logger.level <= Logger.FINER)
 					logger.log("Returning response for retrieve content tags message "
 									+ rcmsg.getContentId() + " from " + endpoint.getId());
 
 				getResponseContinuation(msg).receiveResult(cp);
 			}  else if (msg instanceof RetrieveContIDsMessage) {
 				lookups++;
 				Map<Id, String> ret = new HashMap<Id, String>();
 				Map<Id, SharedContentInfo> map = user.getSharedContent();
 				Iterator<Id> itid = map.keySet().iterator();
 				Iterator<SharedContentInfo> itsci = map.values().iterator();
 				while (itid.hasNext()) {
 					ret.put(itid.next(), itsci.next().getFilename());
 				}
 
 				if (logger.level <= Logger.FINER)
 					logger.log("Returning response for retrieve content ids message from " + endpoint.getId());
 
 				getResponseContinuation(msg).receiveResult(ret);
 			} else {
 				if (logger.level <= Logger.SEVERE)
 					logger.log("ERROR - Received message " + msg
 							+ "of unknown type.");
 			}
 	}
 
 	/**
 	 * Wrapper for lookup process.
 	 * Hint: A message can be routed using a NodeHandle as a first hop target!
 	 * 
 	 * @param id The key to be queried.
 	 * @param type The type of message to route.
 	 * @param extra_args A dictionary of extra arguments to be passed.
 	 * @param command Command to be performed when the result is received.
 	 */
 	public void lookup(final Id id, final short type,
 			final HashMap<String, Object> extra_args,
 			final Continuation<Object, Exception> command) {
 		if (logger.level <= Logger.FINER)
 			logger.log(" Performing lookup on " + id.toStringFull());
 
 		NodeHandle destNodeHandle = extra_args.containsKey("nodeHandle") ? ((NodeHandle) extra_args.get("nodeHandle")) : null;
 		ContinuationMessage message = null;
 		switch (type) {
 			case MessageType.GetUserProfile:
 				message = new GetUserProfileMessage(getUID(), id, getLocalNodeHandle(), id);
 				break;
 			case MessageType.FriendRequest:
 				message = new FriendReqMessage(getUID(), getLocalNodeHandle(), id, (FriendReqPDU)extra_args.get("PDU"));
 				break;
 			case MessageType.FriendReject:
 				message = new FriendRejectMessage(getUID(), id, getLocalNodeHandle(), id, (FriendReqPDU)extra_args.get("PDU"));
 				break;
 			case MessageType.FriendAccept:
 				message = new FriendAcceptMessage(getUID(), id, getLocalNodeHandle(), id, (FriendReqPDU)extra_args.get("PDU"));
 				break;
 			case MessageType.TagContent:
 				message = new TagContentMessage(getUID(), id, getLocalNodeHandle(), id,	(TagPDU)extra_args.get("PDU"));
 				break;
 			case MessageType.TagUser:
 				message = new TagUserMessage(getUID(), id, getLocalNodeHandle(), id,	(TagPDU)extra_args.get("PDU"));
 				break;
 			case MessageType.RetrieveContentTags:
 				message = new RetrieveContTagsMessage(getUID(), (Id)extra_args.get("ContentId"), getLocalNodeHandle(),
 						id,	 (RetrieveContPDU)extra_args.get("PDU"));
 				break;
 			case MessageType.RetrieveContentIDs:
 				message = new RetrieveContIDsMessage(getUID(), getLocalNodeHandle(), id);
 				break;
 			default:
 				logger.log("Unknown message type. Bailing out...");
 				return;
 		}
 
 		// send the request across the wire, and see if the result is null or not
 		sendRequest(id, message, destNodeHandle,
 				new NamedContinuation(message.getClass().getSimpleName() + " for " + id, command) {
 			public void receiveResult(final Object o) {
 				// if we have an object, we return it
 				// otherwise, we may want to check all replicas in order to make
 				// sure the object doesn't exist anywhere
 				command.receiveResult(o);
 			}
 			public void receiveException(Exception e) {
 				receiveResult(null);
 			}
 		});
 	}
 
 	@Override
 	public void update(NodeHandle handle, boolean joined) {
 		if (!joined)
 			return;
 		IdRange range = endpoint.range(handle, 0, null, true);
 		if (range == null)
 			return;
 		IdSet togo = storage.getStorage().scan(range);
 		if (togo == null || togo.numElements() == 0)
 			return;
 		for (Id next : togo.asArray()) {
 			final Id curId = next;
 			storage.getStorage().getObject(next,
 					new Continuation<Object, Exception>() {
 				@Override
 				public void receiveResult(Object result) {
 					if (result instanceof PastContent) {
 						insert((PastContent)result, new SimpleContinuation(){
 							@Override
 							public void receiveResult(Object result) {
 								storage.getStorage().unstore(curId, new SimpleContinuation() {
 									@Override
 									public void receiveResult(Object result) {
 										if (logger.level <= Logger.INFO)
 											logger.log("Moved item " + curId + " to new node");
 									}
 								});
 							}
 						});
 					}
 				}
 
 				@Override
 				public void receiveException(Exception exception) {
 					logger.logException("Error copying data to new node", exception);
 				}
 			});
 		}
 	}
 }
