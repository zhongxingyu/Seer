 package session;
 
 import java.net.Socket;
 import java.util.Vector;
 
 import misc.Log;
 
 import socket.packet.handlers.senders.AddContactWithoutInvite_handler;
 import socket.packet.handlers.senders.MsgPersoToClient_handler;
 import socket.packet.handlers.senders.MsgToClient_Handler;
 import socket.packet.handlers.senders.PseudoToClient_handler;
 import socket.packet.handlers.senders.StatusToClient_Handler;
 import socket.packet.handlers.senders.cont_connected_handler;
 import socket.packet.handlers.senders.cont_disconct_handler;
 import socket.packet.objects.message;
 
 import database.DatabaseFunctions;
 import database.DatabaseTransactions;
 
 public class session {
 	
 	private boolean connected;
 	private Integer status;
 	private String name;
 	private String personnal_msg;
 	private Integer uid;
 	private Thread thr_associated;
 	private Vector<session> sess_linked;
 	private Socket sock;
 	private Vector<Integer> uid_blocked;
 	
 	public session(Thread thr, Socket sockt)
 	{
 		uid = 0;
 		connected = false;
 		status = 0;
 		name = "";
 		personnal_msg = "";
 		thr_associated = thr;
 		sess_linked = new Vector<session>();
 		sock = sockt;
 	}
 
 	public void connect_client()
 	{
 		connected = true;
 		SessionHandler.AddSession(this);
 		uid = DatabaseTransactions.IntegerQuery("account", "uid", "user = '" + name + "'");
 		personnal_msg = DatabaseTransactions.StringQuery("account", "phr_perso", "user = '" + name + "'");
 		LoadBlockedContacts();
		Log.outError(uid+"");
 		// TODO : send all invitations
 	}
 	
 	private void LoadBlockedContacts() 
 	{
 		uid_blocked = DatabaseTransactions.getIntegerList("acc_blocked", "contact", "uid = '" + uid + "'");
 	}
 
 	public void disconnect_client()
 	{
 		SessionHandler.DestroySession(this,thr_associated);
 	}
 
 	public void contact_connected(session sess, boolean block)
 	{
 		if(sess == null)
 			return;
 		
 		if(!block)
 			sess_linked.add(sess);
 		cont_connected_handler pck = new cont_connected_handler(sess.getName(),
 				sess.getStatus().toString(),sess.getPersonnalMsg(),sess.getUid());
 		if(pck != null)
 			pck.Send(sock);
 	}
 	
 	public boolean know_contact(Integer _uid)
 	{
 		if(DatabaseTransactions.DataExist("acc_contact", "uid", "contact = '" + _uid + "'" +
 				" AND uid = '" + uid + "'"))
 			return true;
 		else
 			return false;
 	}
 	public void contact_disconnected(session sess, boolean blocked)
 	{
 		if(sess == null)
 			return;
 		
 		cont_disconct_handler pck = new cont_disconct_handler(sess.getUid());
 		if(sock != null)
 			pck.Send(sock);
 		
 		if(!blocked)
 			for(int i=0;i<sess_linked.size();i++)
 				if(sess_linked.get(i).equals(sess))
 					sess_linked.remove(i);
 	}
 	
 	public boolean has_blocked(Integer _uid) 
 	{
 		if(DatabaseTransactions.IntegerQuery("acc_blocked", "blocked"
 				, "contact = '" + _uid + "' AND uid = '" + uid + "'") == 1)
 			return true;
 		else
 			return false;
 	}
 	
 	public void broadcast_SomethingChanged(int sth) 
 	{
 		for(int i=0;i<sess_linked.size();i++)
 		{
 			boolean blocked = false;
 			for(int j=0;j<uid_blocked.size();j++)
 				if(uid_blocked.get(i) == sess_linked.get(i).getUid())
 					blocked = true;
 			
 			if(!blocked)
 			{
 				switch(sth)
 				{
 					case 1:
 						sess_linked.get(i).SendStatusToMe(uid, status);
 						break;
 					case 2:
 						sess_linked.get(i).SendPseudoToMe(uid, name);
 						break;
 					case 3:
 						sess_linked.get(i).SendMsgPersoToMe(uid, personnal_msg);
 						break;
 				}
 			}
 		}
 	}
 	
 	private void SendMsgPersoToMe(Integer _uid, String pmsg) 
 	{
 		MsgPersoToClient_handler pck = new MsgPersoToClient_handler(_uid,pmsg);
 		pck.Send(sock);
 	}
 
 	private void SendPseudoToMe(Integer _uid, String _name) 
 	{
 		PseudoToClient_handler pck = new PseudoToClient_handler(_uid,_name);
 		pck.Send(sock);
 	}
 
 	public void block_contact(String c_uid, String method) 
 	{
 		if(method.equals("1"))
 		{
 			contact_disconnected(SessionHandler.getContactByUID(Integer.decode(c_uid)),true);
 			uid_blocked.add(Integer.decode(c_uid));
 		}
 		else
 		{
 			contact_connected(SessionHandler.getContactByUID(Integer.decode(c_uid)), true);
 			for(int i=0;i<uid_blocked.size();i++)
 				if(uid_blocked.get(i).equals(Integer.decode(c_uid)))
 					uid_blocked.remove(i);
 		}
 		if(DatabaseTransactions.DataExist("acc_blocked", "blocked", "uid = '" + 
 				uid + "' AND contact = '" + c_uid + "'"))
 			DatabaseTransactions.ExecuteQuery("UPDATE acc_blocked SET blocked = '" + method + 
 					"' where uid = '" + uid + "' AND contact = '" + c_uid + "'");
 		else
 			DatabaseTransactions.ExecuteQuery("INSERT INTO acc_blocked VALUES ('" + uid + "','" 
 					+ c_uid + "','" + method + "");
 				
 	}
 	
 	
 	
 	public void TransmitMsgTo(Object packet) {
 		if(!packet.getClass().equals((new message(null,null)).getClass()))
 		{
 			Log.outError("Malformed Msg to Transmit");
 			return;
 		}
 		
 		message msg = (message) packet;
 		Integer _uid = msg.getDest();
 		if(SessionHandler.getContactByUID(_uid) != null)
 			if(!SessionHandler.getContactByUID(_uid).has_blocked(uid))
 				SessionHandler.getContactByUID(_uid).SendMessageToMe(uid,msg.getMsg());
	
 	}
 	
 	private void SendMessageToMe(Integer _uid, String msg)
 	{
 		MsgToClient_Handler pck = new MsgToClient_Handler(_uid,msg);
 		pck.Send(sock);		
 	}
 
 	private void SendStatusToMe(Integer _uid, Integer status)
 	{
 		StatusToClient_Handler pck = new StatusToClient_Handler(_uid,status);
 		pck.Send(sock);
 	}
 	
 	public void ChangePseudo(Object packet) 
 	{
 		String pck = packet.toString();
 		SetName(pck);
 		DatabaseTransactions.ExecuteQuery("UPDATE account SET pseudo = '" + pck + 
 				"'	WHERE uid = '" + uid + "'");
 		broadcast_SomethingChanged(2);
 	}
 	
 	public void ChangeMsgPerso(Object packet)
 	{
 		String pck = packet.toString();
 		SetPersonnalMsg(pck);
 		DatabaseTransactions.ExecuteQuery("UPDATE account SET phr_perso = '" + pck +
 				"' WHERE uid = '" + uid + "'");
 		broadcast_SomethingChanged(3);
 	}
 	
 	public String AddContact(Object packet) 
 	{
 		String pck[] = packet.toString().split("||[]||");
 		if(pck.length != 2)
 			return "5";
 		
 		String username = pck[0].toString();
 		Integer group = Integer.decode(pck[1]);
 		String result = "1";
 		if(DatabaseTransactions.DataExist("account", "user", "user = '" + username + "'") &&
 				!username.equals(DatabaseFunctions.getAccountNameByUID(uid)))
 		{
 			Integer _uid = DatabaseFunctions.getAccountUIDByName(username);
 			if(!DatabaseTransactions.DataExist("acc_contact", "contact", "uid = '" + uid + "' AND"))
 			{
 				if(DatabaseTransactions.DataExist("acc_group", "gid", "uid = '" + uid + "' AND" +
 						" gid = '" + group + "'"))
 				{
 					
 					DatabaseTransactions.ExecuteQuery("INSERT INTO acc_contact VALUES ('" + uid + "','" + 
 							_uid + "','0','','" + group + "'");
 					
 					if(SessionHandler.isConnected(_uid))
 					{
 						Invitation invit = new Invitation(uid, _uid, true);
 						invit.Send(SessionHandler.getContactByUID(_uid).getSocket());
 					}
 					else
 						new Invitation(uid,_uid,false);
 					
 					result = (0 + "[)[)" + _uid);
 				}
 				else
 					result = (4 + "[)[)" + _uid);
 
 			}
 			else
 				result = (2 + "[)[)" + _uid);
 		}
 		
 		return result;
 	}
 	
 	
 
 	public void ManageInvitation(Integer _uid, Integer method) 
 	{
 		switch(method)
 		{
 			case 0:
 				// nothing, not accept the client
 				break;
 			case 1:
 				DatabaseTransactions.ExecuteQuery("DELETE FROM acc_invitation WHERE uid = '" + uid + "' AND" +
 						" contact = '" + _uid + "'");
 				break;
 			case 2:
 				DatabaseTransactions.ExecuteQuery("DELETE FROM acc_invitation WHERE uid = '" + uid + "' AND" +
 						" contact = '" + _uid + "'");
 				DatabaseTransactions.ExecuteQuery("INSERT INTO acc_contact VALUES ('" + _uid + "','" + 
 						uid + "','0','','0'");
 				AddContactToClientList(_uid);
 				break;
 			default:
 				Log.outError("Invalid response from ManageInvitation");
 				break;
 		}
 	}
 	
 	private void AddContactToClientList(Integer _uid) 
 	{
 		AddContactWithoutInvite_handler ACWI = new AddContactWithoutInvite_handler(this,(0 + "[)[)" + _uid));
 		ACWI.Send(sock);
 	}
 	
 	public void DelContact(Integer _uid, Integer blocked) 
 	{
 		DatabaseTransactions.ExecuteQuery("DELETE FROM acc_contact WHERE uid = '" + uid + "' AND " 
 				+ "contact = '" + _uid + "'");
 		DatabaseTransactions.ExecuteQuery("DELETE FROM acc_invitation WHERE contact = '" + uid + "' AND "
 				+ "uid = '" + _uid + "'");
 		if(SessionHandler.isConnected(_uid));
 			for(int i=0;i<sess_linked.size();i++)
 				if(sess_linked.get(i).getUid().equals(_uid))
 					sess_linked.remove(i);
 	}
 	
 	public Vector<session> getLinkedSessions() { return sess_linked; }
 	public Integer getUid() { return uid; }
 	public boolean IsConnected(){ return connected; }
 	public void SetStatus(Integer st){ status = st; }
 	public Integer getStatus(){	return status; }
 	public void SetConnected(boolean cn) { connected = cn; } 
 	public void SetName(String nm) { name = nm; }
 	public String getName() { return name; }
 	public String getPersonnalMsg() { return personnal_msg; }
 	public void SetPersonnalMsg(String msg) { personnal_msg = msg; }
 	public Socket getSocket() { return sock; }
 
 	
 
 	
 	
 
 
 
 	
 
 	
 
 	
 
 	
 }
