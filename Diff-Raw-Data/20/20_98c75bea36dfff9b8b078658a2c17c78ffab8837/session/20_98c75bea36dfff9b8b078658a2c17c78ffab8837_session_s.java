 package session;
 
 import java.net.Socket;
 import java.sql.SQLException;
 import java.util.Vector;
 
 import socket.packet.handlers.cont_connected_handler;
 
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
 	
 	public session(Thread thr, Socket sockt)
 	{
 		uid = 0;
 		connected = false;
 		status = 0;
 		name = "";
 		personnal_msg = "";
 		thr_associated = thr;
 		sess_linked = null;
 		sock = sockt;
 	}
 
 	public void connect_client() throws SQLException
 	{
 		connected = true;
 		SessionHandler.AddSession(this);
 		uid = DatabaseTransactions.IntegerQuery("account", "uid", "user = '" + name + "'");
 		personnal_msg = DatabaseTransactions.StringQuery("account", "phr_perso", "user = '" + name + "'");
 	}
 	
 	public void disconnect_client()
 	{
 		SessionHandler.DestroySession(this,thr_associated);
 	}
 
 	public void contact_connected(session sess)
 	{
 		sess_linked.add(sess);
 		cont_connected_handler pck = new cont_connected_handler(sess.getName(),
				sess.getStatus().toString(),sess.getPersonnalMsg());
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
 	public void contact_disconnected(session sess)
 	{
		/*
		 * TODO: contact client to say his contact was disconnected
		 */
 		for(int i=0;i<sess_linked.size();i++)
 			if(sess_linked.get(i).equals(sess))
 				sess_linked.remove(i);
 	}
 	
 	public boolean has_blocked(Integer _uid) 
 	{
 		if(DatabaseTransactions.IntegerQuery("acc_contact", "blocked"
 				, "contact = '" + _uid + "' AND uid = '" + uid + "'") == 1)
 			return true;
 		else
 			return false;
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
 
 	
 }
