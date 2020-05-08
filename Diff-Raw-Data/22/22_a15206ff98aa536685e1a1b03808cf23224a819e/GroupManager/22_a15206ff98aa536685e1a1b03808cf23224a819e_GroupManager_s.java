 package edu.uc.cs.distsys.init;
 
 import java.io.IOException;
 import java.net.UnknownHostException;
 import java.util.Random;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.Executors;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.TimeUnit;
 
 import edu.uc.cs.distsys.Logger;
 import edu.uc.cs.distsys.Node;
 import edu.uc.cs.distsys.comms.CommsWrapper;
 import edu.uc.cs.distsys.comms.MessageDroppedException;
 import edu.uc.cs.distsys.comms.MessageHandler;
 import edu.uc.cs.distsys.comms.MulticastWrapper;
 import edu.uc.cs.distsys.comms.NotifyThread;
 
 public class GroupManager {
 
 	private class GroupRequestListener extends MessageHandler<GroupRequest> {
 		public GroupRequestListener(Logger logger) {
 			super(GroupRequest.class, logger, false); // Only start the handler once we've joined a group
 		}
 		
 		@Override
 		public void handleMessage(GroupRequest message) {	
 			GroupManager.this.logger.log("Received group request from " + message.getSenderId() + " cookie - " + message.getGroupCookie());
 			if (GroupManager.this.myNode.isLeader()) {
 				GroupInvitation invite = null;
 				if (GroupManager.this.cookieMappings.containsKey(message.getSenderId())) {
 					if (GroupManager.this.cookieMappings.get(message.getSenderId()).equals(message.getGroupCookie())) {
 						GroupManager.this.logger.log("Node rejoining the group " + message.getSenderId() + " cookie - " + message.getGroupCookie());
 						invite = new GroupInvitation(GroupManager.this.myNode.getId(), message.getSenderId(), 0, message.getGroupCookie());
 					} else {
 						GroupManager.this.logger.log("Rejecting group request from " + message.getSenderId() + " cookie - " + message.getGroupCookie());
 						invite = new GroupInvitation(GroupManager.this.myNode.getId(), message.getSenderId(), 0, Cookie.INVALID_COOKIE);
 					}
 				} else {
 					Random r = new Random();
 					Cookie newCookie = new Cookie(r.nextLong());
 					//TODO: there's a chance we could generate duplicate cookies (oops)
 //					while (GroupManager.this.cookieMappings.containsKey(newCookie)) {
 //						newCookie = new Cookie(r.nextLong());
 //					}
 					GroupManager.this.cookieMappings.put(message.getSenderId(), newCookie);
 					GroupManager.this.logger.log("Inviting id=" + message.getSenderId() + " to our group");
 					invite = new GroupInvitation(GroupManager.this.myNode.getId(), message.getSenderId(), 
 							GroupManager.this.myNode.getGroupId(), newCookie);
 				}
 				
 				int sendAttemptsLeft = 5;
 				while (sendAttemptsLeft > 0) {
 					try {
 						GroupManager.this.groupInvitor.send(invite);
 						break;
 					} catch (IOException e) {
 						GroupManager.this.logger.error("Failed to send invite to new node: " + e.getMessage());
 						sendAttemptsLeft--;
 					}
 				}
 				if (sendAttemptsLeft == 0) {
 					GroupManager.this.logger.error("Unable to send invite, this is not good!");
 				}
 			}
 		}
 	}
 	
 	private class GroupInvitationListener extends MessageHandler<GroupInvitation> {
 		public GroupInvitationListener(Logger logger) {
 			super(GroupInvitation.class, logger);
 		}
 		
 		@Override
 		public void handleMessage(GroupInvitation message) {
 			GroupManager.this.logger.log("RECEIVED INVITE");
 			
 			if (message.getInviteeId() == GroupManager.this.myNode.getId()) {
 				GroupManager.this.myInvitations.add(message);
 			} else {
 				if (!message.getCookie().equals(Cookie.INVALID_COOKIE)) {
 					GroupManager.this.cookieMappings.put(message.getSenderId(), message.getCookie());
 				}
 			}
 		}
 	}
 	
 	private static final int REQUEST_PORT = 10000;
 	private static final int INVITATION_PORT = 10001;
	private static final long INVITATION_TIMEOUT_MS = 3000;
 	
 	private Node myNode;
 	private ConcurrentMap<Integer, Cookie> cookieMappings;
 	
 	private Logger logger;
 	private CommsWrapper<GroupRequest> groupRequestor;
 	private CommsWrapper<GroupInvitation> groupInvitor;
 	private MessageHandler<GroupRequest> requestHandler;
 	private MessageHandler<GroupInvitation> inviteHandler;
 	private Thread requestThread;
 	private Thread inviteThread;
 
 	private BlockingQueue<GroupInvitation> myInvitations;
 	
 	public GroupManager(Node myNode, Logger logger) throws UnknownHostException {
 		this.myNode = myNode;
 		this.logger = logger;
 		this.cookieMappings = new ConcurrentHashMap<Integer, Cookie>();
 		this.myInvitations = new LinkedBlockingQueue<GroupInvitation>();
 		
 		this.groupRequestor = new MulticastWrapper<GroupRequest>(REQUEST_PORT, myNode.getId(), new GroupRequest.GroupRequestFactory(), logger);
 		this.groupInvitor = new MulticastWrapper<GroupInvitation>(INVITATION_PORT, myNode.getId(), new GroupInvitation.GroupInvitationFactory(), logger);
 		this.requestHandler = new GroupRequestListener(logger);
 		this.inviteHandler = new GroupInvitationListener(logger);
 		
 		this.requestThread = Executors.defaultThreadFactory().newThread(
 				new NotifyThread<GroupRequest>(myNode.getId(), groupRequestor, requestHandler, GroupRequest.class, logger));
 		this.inviteThread = Executors.defaultThreadFactory().newThread(
 				new NotifyThread<GroupInvitation>(myNode.getId(), groupInvitor, inviteHandler, GroupInvitation.class, logger));
 		this.requestThread.start();
 		this.inviteThread.start();
 //		this.requestHandler.start();
 //		this.inviteHandler.start();
 	}
 
 	public void shutdown() {
 		this.groupRequestor.close();
 		this.groupInvitor.close();
 		this.requestHandler.stop();
 		this.inviteHandler.stop();
 	}
 	
 	public void locateAndJoinGroup() throws GroupJoinException {
 		myInvitations.clear();
 		GroupRequest request = null;
 		if (myNode.getGroupCookie().equals(Cookie.INVALID_COOKIE)) {
 			request = new GroupRequest(myNode.getId());
 		} else {
 			request = new GroupRequest(myNode.getId(), myNode.getGroupCookie());
 		}
 		
 		try {
			logger.log("Sending group join request");
			// Send the group join request
			this.groupRequestor.send(request);
			
			// wait for a group invitation
			GroupInvitation invite = myInvitations.poll(INVITATION_TIMEOUT_MS, TimeUnit.MILLISECONDS);
 			
 			if (invite == null) {	// No invite :-(
 				logger.log("Didn't receive an invitation :-(");
 				
 				// If we were the leader previously, we must have gone away and come back before 
 				// any other process noticed. Let's just join up and start an election.
 				if (myNode.isLeader() && 
 						!myNode.getGroupCookie().equals(Cookie.INVALID_COOKIE)) {
 					//TODO
 					//TODO
 					//TODO
 					cookieMappings.put(myNode.getId(), myNode.getGroupCookie());
 					logger.log("Leader rejoining group (id=" + myNode.getGroupId() + ", cookie="+myNode.getGroupCookie()+")");
 					//TODO
 					//TODO
 					//TODO
 				} else { // We must be the first process, start a new group
 					Random r = new Random(System.currentTimeMillis());
 					int newGroupId = r.nextInt(1000);
 					myNode.setGroupId(newGroupId);
 					if (myNode.getGroupCookie().equals(Cookie.INVALID_COOKIE)) {
 						Cookie newCookie = new Cookie(r.nextLong());
 						cookieMappings.put(myNode.getId(), newCookie);
 						myNode.setGroupCookie(newCookie);
 					} else {
 						cookieMappings.put(myNode.getId(), myNode.getGroupCookie());
 					}
 					logger.log("Starting new group (id=" + newGroupId + ")");
 				}
 			} else {
 				// Make sure our invite wasn't rejected
 				if (invite.getCookie().equals(Cookie.INVALID_COOKIE)) {
 					throw new GroupJoinException("Failed to join group " + invite.getGroupId() + " (node conflict detected)");
 				}
 				cookieMappings.put(myNode.getId(), invite.getCookie());
 				myNode.setGroupCookie(invite.getCookie());
 				myNode.setGroupId(invite.getGroupId());
 				logger.log("Joined group " + invite.getGroupId() + ", my cookie is " + invite.getCookie());
 			}
 			
 			// If we get here, we've actually joined a group, so we should start listening
 			// for group messages
 			this.requestHandler.start();
 			
 		} catch (MessageDroppedException mde) {
 			throw new GroupJoinException("Failed to join group (message dropped)", mde);
 		} catch (IOException e) {
 			throw new GroupJoinException("Failed to join group (I/O error)", e);
 		} catch (InterruptedException e) {
 			throw new GroupJoinException("Group-join operation interrupted");
 		}
 	}
 	
 }
