 package edu.uc.cs.distsys.init;
 
 import edu.uc.cs.distsys.comms.Message;
 import edu.uc.cs.distsys.comms.MessageFactory;
 
 
 public class GroupInvitation extends Message {
 
 	public static class GroupInvitationFactory implements MessageFactory<GroupInvitation> {
 		public GroupInvitationFactory() {}
 		@Override
 		public GroupInvitation create(byte[] rawMsg) {
 			return (GroupInvitation) Message.deserialize(rawMsg);
 		}
 	}
 
 	private static final long serialVersionUID = 6876939369742287913L;
 
 	private final int groupId;
 	private final Cookie cookie;
 	
	public GroupInvitation(int senderId, int groupId, Cookie cookie) {
 		super(senderId);
 		this.groupId = groupId;
 		this.cookie = cookie;
 	}
 
 	public int getGroupId() {
 		return groupId;
 	}
 	
 	public Cookie getCookie() {
 		return cookie;
 	}
 	
 }
