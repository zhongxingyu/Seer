 package node.facebook;
 import java.util.Hashtable;
 import java.util.Vector;
 import node.rpc.IFacebookServer;
 import node.rpc.IFacebookServerReply;
 import node.rpc.RPCException;
 import node.rpc.RPCStub;
 
 
 public class FacebookFrontendSystem extends BaseFacebookSystem implements IFacebookServerReply
 {
 	// PendingAcceptFriendInfo stores info about ongoing accept_friend requests.
 	// Once we get acknowledgement that the receiver accepted the adder's request
 	// we send another accept_friend request to make the adder add the receiver 
 	// as well.
 	private class PendingAcceptFriendInfo
 	{
 		String loginAdder;
 		String loginReceiver;
 		IFacebookServer shardAdder;
 	};
 	
 	private Hashtable<Integer, IFacebookServer> m_stubs = new Hashtable<Integer, IFacebookServer>();
 	private Hashtable<Integer, PendingAcceptFriendInfo> m_pendingAcceptFriend = new Hashtable<Integer, PendingAcceptFriendInfo>();
 	
 	/**
 	 * FacebookFrontendSystem()
 	 * @param node
 	 */
 	public FacebookFrontendSystem(FacebookRPCNode node) 
 	{
 		super(node);
 	}
 
 	/**
 	 * onCommand()
 	 * Invoked when this node receives a command string from the console
 	 * 
 	 * @param command
 	 */
 	public boolean onCommand(String command)
 	{	
 		String[] parts = command.split("\\s+");
 		
 		String methodName = parts[0];
 		
 		if (!canHandle(methodName)) {
 			return false;
 		}
 		
 		callMethodOnShards(command, parts);
 		
 		return true;
 	}
 
 	private boolean canHandle(String methodName) {
 		return methodName.equals("login") || methodName.equals("logout") ||
 				methodName.equals("create_user") || methodName.equals("add_friend") ||
 				methodName.equals("accept_friend") || methodName.equals("read_message_all") ||
 				methodName.equals("write_message_all");
 	}
 
 	private void callMethodOnShards(String command, String[] parts) {
 		String methodName = parts[0];
 		
 		String login = extractLoginFromCommand(methodName, parts).toLowerCase();
 		
 		try
 		{
 			String op = parts[0].toLowerCase();
 			switch(op) {
 				case "login":
 				{
 					IFacebookServer shard = getShardFromLogin(login);
 					String pwd = parts[2];
 					
 					shard.login(login, pwd);
 					break;
 				}
 				case "logout":
 				{
 					IFacebookServer shard = getShardFromLogin(login);
 					String token = parts[1];
 					
 					shard.logout(token);
 					break;
 				}
 				case "create_user":
 				{
 					IFacebookServer shard = getShardFromLogin(login);
 					String pwd = parts[2];
 					
 					shard.createUser(login, pwd);
 					break;
 				}
 				case "add_friend":
 				{
 					String adderLogin = login;
 					String receiverLogin = parts[2];
 					
 					IFacebookServer shardReceiver = getShardFromLogin(receiverLogin);
 					shardReceiver.addFriendReceiver(adderLogin, receiverLogin);
 					break;
 				}
 				case "accept_friend":
 				{
 					String receiverLogin = login;
 					String adderLogin = parts[2];
 					IFacebookServer shardReceiver = getShardFromLogin(login);
 					IFacebookServer shardAdder = getShardFromLogin(adderLogin);
 					
 					// Send the accept_friend command
 					shardReceiver.acceptFriendReceiver(adderLogin, receiverLogin);
 					
 					// Store this info so that we can process the 2nd part once the
 					// 1st part succeeds
 					PendingAcceptFriendInfo info = new PendingAcceptFriendInfo();
 					info.loginAdder = adderLogin;
 					info.loginReceiver = receiverLogin;
 					info.shardAdder = shardAdder;
 					m_pendingAcceptFriend.put(RPCStub.getCurrentReplyId(), info);
 					break;
 				}
 				case "write_message_all":
 					// TODO: write_message_all should only contact shards that actually
 					// contain friends of the user.
 				
 					String message = getMessageBody(command);
 	
 					for (int shardId : FacebookRPCNode.getShardAddresses()) {
 						// TODO: add 2pc here
 						IFacebookServer shard = getShardFromShardAddress(shardId);
 						shard.writeMessageAll(login, message);
 					}
 					break;
 				case "read_message_all":
 				{
 					IFacebookServer shard = getShardFromLogin(login);
 					shard.readMessageAll(parts[1]);
 					break;
 				}
 					
 				default:
 					assert false;
 			}
 		}
 		catch (RPCException ex)
 		{
 			// Stubs shouldn't throw, but java forces the try/catch block
 			// because remote implementations can throw.
 		}
 	}
 
 	private String getMessageBody(String command) {
 		String message = "";
 		int idx = command.indexOf(' ');
 		int nextIdx = command.indexOf(' ', idx+1);
 		if (idx != -1 && nextIdx != -1) {
 			message = command.substring(nextIdx, command.length()).trim();
 		}
 		return message;
 	}
 	
 	private String extractLoginFromCommand(String methodName, String[] parts) {
 		// login and create_user receive the username directly
 		// the other commands receive the session instead
 		if (methodName.equals("login") || methodName.equals("create_user")) {
 			return parts[1];
 		} else {
 			SessionToken token = SessionToken.createFromString(parts[1]);
 			return token.getUser();
 		}
 	}
 
 	private IFacebookServer getShardFromLogin(String user) {
 		
 		int shardAddress = getShardAddress(user);
 		
 		return getShardFromShardAddress(shardAddress);
 	}
 	
 	private IFacebookServer getShardFromShardAddress(int shardAddress) {		
 		IFacebookServer stub;
 		// Get the proper stub for communication with the server
 		if (!m_stubs.containsKey(shardAddress))
 		{
 			// NOTE: we create a different stub per server connection,
 			// but in the end all replies are handled by the same
 			// object ("this" one).
 			stub = m_node.connectToFacebookServer(shardAddress, this);
 			m_stubs.put(shardAddress, stub);
 		}
 		else
 		{
 			stub = m_stubs.get(shardAddress);
 		}
 		return stub;
 	}
 	
 	public int getShardAddress(String user) {		
 		// Use a simplified division based on the first letter
 		int shardCount = FacebookRPCNode.getShardAddresses().size();
		int hash = (int)Character.toLowerCase(user.charAt(0)) - 'a';
 		return 1 + (hash*shardCount)/26;
 	}
 	
 	@Override
 	public void reply_login(int replyId, int sender, int result, String reply)
 	{
 		if (result == 0)
 		{
 			// RPC call succeeded
 			user_info("User logged in. Token=" + reply);
 		}
 		else
 		{
 			// RPC call failed
 			onMethodFailed(sender, "login", result);
 		}
 	}
 
 	@Override
 	public void reply_logout(int replyId, int sender, int result, String reply)
 	{
 		if (result == 0)
 		{
 			// RPC call succeeded
 			user_info("User logged out.");
 		}
 		else
 		{
 			// RPC call failed
 			onMethodFailed(sender, "logout", result);
 		}
 	}
 
 	@Override
 	public void reply_createUser(int replyId, int sender, int result, String reply)
 	{
 		if (result == 0)
 		{
 			// RPC call succeeded
 			user_info("create_user: Server returned ok. returnValue=" + reply);
 		}
 		else
 		{
 			// RPC call failed
 			onMethodFailed(sender, "create_user", result);
 		}
 	}
 
 	@Override
 	public void reply_addFriend_receiver(int replyId, int sender, int result, String reply)
 	{
 		if (result == 0)
 		{
 			// RPC call succeeded
 			user_info("add_friend_receiver: Server returned ok. returnValue=" + reply);
 		}
 		else
 		{
 			// RPC call failed
 			onMethodFailed(sender, "add_friend", result);
 		}
 	}
 
 	@Override
 	public void reply_acceptFriend_adder(int replyId, int sender, int result, String reply)
 	{
 		if (result == 0)
 		{
 			// RPC call succeeded
 			user_info("accept_friend_adder: Server returned ok. returnValue=" + reply);
 		}
 		else
 		{
 			// RPC call failed
 			onMethodFailed(sender, "accept_friend", result);
 		}
 	}
 
 	@Override
 	public void reply_acceptFriend_receiver(int replyId, int sender, int result, String reply)
 	{
 		if (result == 0)
 		{
 			// RPC call succeeded
 			user_info("accept_friend_receiver: Server returned ok. returnValue=" + reply);
 			
 			// Now that the receiver accepted the friend request from the adder,
 			// make it so the adder also adds the receiver as friend.
 			if (m_pendingAcceptFriend.containsKey(replyId))
 			{
 				try 
 				{
 					PendingAcceptFriendInfo info = m_pendingAcceptFriend.get(replyId);
 					info.shardAdder.acceptFriendAdder(info.loginAdder, info.loginReceiver);
 					m_pendingAcceptFriend.remove(replyId);
 				} 
 				catch (RPCException e) 
 				{
 					// Should not happen
 				}
 			}
 		}
 		else
 		{
 			// RPC call failed
 			onMethodFailed(sender, "accept_friend", result);
 		}
 	}
 	
 	@Override
 	public void reply_writeMessageAll(int replyId, int sender, int result, String reply)
 	{
 		if (result == 0)
 		{
 			// RPC call succeeded
 			user_info("write_message_all: Server returned ok. returnValue=" + reply);
 		}
 		else
 		{
 			// RPC call failed
 			onMethodFailed(sender, "write_message_all", result);
 		}
 	}
 
 	@Override
 	public void reply_readMessageAll(int replyId, int sender, int result, String reply)
 	{
 		if (result == 0)
 		{
 			// RPC call succeeded
 			user_info("read_message_all: Returned content:"); 
 			user_info(reply);
 		}
 		else
 		{
 			// RPC call failed
 			onMethodFailed(sender, "read_message_all", result);
 		}
 	}
 
 	private void onMethodFailed(int from, String methodName, int result)
 	{
 		String errorMsg = String.format(FacebookRPCNode.ERROR_MESSAGE_FORMAT, methodName, from, result);
 		user_info(String.format("NODE %d: %s", m_node.addr, errorMsg));
 	}
 }
