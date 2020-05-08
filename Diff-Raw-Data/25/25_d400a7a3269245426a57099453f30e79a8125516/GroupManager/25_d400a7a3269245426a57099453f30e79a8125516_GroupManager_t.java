 package edu.uc.cs.distsys.init;
 
 import java.io.IOException;
 import java.net.UnknownHostException;
 import java.util.Random;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.TimeUnit;
 
 import edu.uc.cs.distsys.Logger;
 import edu.uc.cs.distsys.Node;
 import edu.uc.cs.distsys.comms.CommsWrapper;
 import edu.uc.cs.distsys.comms.MessageDroppedException;
 import edu.uc.cs.distsys.comms.MessageHandler;
 import edu.uc.cs.distsys.comms.MulticastWrapper;
 
 public class GroupManager {
 
 	private class GroupRequestListener extends MessageHandler<GroupRequest> {
 		public GroupRequestListener(Logger logger) {
 			super(logger, false); // Only start the handler once we've joined a group
 		}
 		
 		@Override
 		public void handleMessage(GroupRequest message) {
 			// TODO Auto-generated method stub
 		}
 	}
 	
 	private class GroupInvitationListener extends MessageHandler<GroupInvitation> {
 		public GroupInvitationListener(Logger logger) {
 			super(logger);
 		}
 		
 		@Override
 		public void handleMessage(GroupInvitation message) {
			if (message.getInviteeId() == GroupManager.this.myNode.getId()) {
				GroupManager.this.myInvitations.add(message);
 			} else {
				if (!message.getCookie().equals(Cookie.INVALID_COOKIE)) {
					GroupManager.this.cookieMappings.put(message.getCookie(), message.getSenderId());
				}
 			}
 		}
 	}
 	
 	private static final int REQUEST_PORT = 10000;
 	private static final int INVITATION_PORT = 10001;
 	private static final long INVITATION_TIMEOUT_MS = 3000;
 	
 	private Node myNode;
 	private ConcurrentMap<Cookie, Integer> cookieMappings;
 	
 	private Logger logger;
 	private CommsWrapper<GroupRequest> groupRequestor;
 	private CommsWrapper<GroupInvitation> groupInvitor;
 	private MessageHandler<GroupRequest> requestHandler;
 	private MessageHandler<GroupInvitation> inviteHandler;
 
 	private BlockingQueue<GroupInvitation> myInvitations;
 	
 	public GroupManager(Node myNode, Logger logger) throws UnknownHostException {
 		this.myNode = myNode;
 		this.logger = logger;
 		this.cookieMappings = new ConcurrentHashMap<Cookie, Integer>();
 		this.myInvitations = new LinkedBlockingQueue<GroupInvitation>();
 		
 		this.groupRequestor = new MulticastWrapper<GroupRequest>(REQUEST_PORT, myNode.getId(), new GroupRequest.GroupRequestFactory(), logger);
 		this.groupInvitor = new MulticastWrapper<>(INVITATION_PORT, myNode.getId(), new GroupInvitation.GroupInvitationFactory(), logger);
 		this.requestHandler = new GroupRequestListener(logger);
 		this.inviteHandler = new GroupInvitationListener(logger);
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
 			// Send the group join request
 			this.groupRequestor.send(request);
 			
 			// wait for a group invitation
 			GroupInvitation invite = myInvitations.poll(INVITATION_TIMEOUT_MS, TimeUnit.MILLISECONDS);
 			
 			if (invite == null) {	// No invite :-(
 				// If we were the leader previously, we must have gone away and come back before 
 				// any other process noticed. Let's just join up and start an election.
 				if (myNode.getLeaderId() == myNode.getId() && 
 						!myNode.getGroupCookie().equals(Cookie.INVALID_COOKIE)) {
 					//TODO
 					//TODO
 					//TODO
 				} else { // We must be the first process, start a new group
 					Random r = new Random(System.currentTimeMillis());
 					int newGroupId = r.nextInt(1000);
 					Cookie newCookie = new Cookie(r.nextLong());
 					cookieMappings.put(newCookie, myNode.getId());
 					myNode.setGroupId(newGroupId);
 					myNode.setGroupCookie(newCookie);
 				}
 			} else {
 				// Make sure our invite wasn't rejected
 				if (invite.getCookie().equals(Cookie.INVALID_COOKIE)) {
 					throw new GroupJoinException("Failed to join group " + invite.getGroupId());
 				}
 				cookieMappings.put(invite.getCookie(), myNode.getId());
 				myNode.setGroupCookie(invite.getCookie());
 				myNode.setGroupId(invite.getGroupId());
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
