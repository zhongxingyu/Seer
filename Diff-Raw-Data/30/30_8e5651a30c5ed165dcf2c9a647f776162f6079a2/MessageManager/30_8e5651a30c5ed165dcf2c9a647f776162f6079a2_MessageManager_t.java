 package gm;
 
 import comm.CommunicationService;
 
 import basic.User;
 
 /**
  * Class used for sending and processing group-related messages. It needs to use the 
  * CommunicationService
  * @author adriana
  *
  */
 public class MessageManager {
 
 	CommunicationService commService;
 	/**
 	 * Creates an invitation object and sends it to the remote Messaging service. 
 	 * @param g the group for which we send the invitation
 	 * @param userEmail the invited member's email, it might not be registered in the system yet.
 	 */
 	public void sendInvite(Group g, String userEmail){}
 	
 	/**
 	 * Processes an invitation message the user received based on the user's input in the reply. 
 	 * @param msg
 	 */
 	protected void processInvite(InvitationMessage msg){}
 	/**
 	 * Processes an invitation message the user received based on the user's input in the reply.
 	 * @param msg
 	 */
 	protected void processOwnerTransfer(TransferMessage msg){}
 	
 	/**
 	 * Performs the necessary steps for completing the invitation process(adding the user or not)
 	 * @param msg
 	 */
 	public void processInvitationReply(InvitationMessage msg) {}
 	/**
 	 * Performs the necessary steps for completing the ownership transfer process
 	 * @param msg
 	 */
 	public void processTransferReply(TransferMessage msg) {}
 	/**
	 * Creates an TransferMessage object and sends it to the remote Messaging service.
	 * @param g the group
	 * @param newOwner the proposed owner, must be part of the group
 	 */
 	public void sendOwnerTransfer(Group g, User newOwner){}
 
 		
 	/**
 	 * Creates and sends a reply message for the given GroupMessage received, based on the user's
 	 * input. Based on this reply there might be other actions to perform (such as marking the user
 	 * as a 'pending owner' etc.
 	 * @param msg the message for which to send response. Contains information such as the sender, the type of message etc
 	 * @param choice the user's answer
 	 */
 	public void sendReply(GroupMessage msg, UserChoice choice){}
 }
