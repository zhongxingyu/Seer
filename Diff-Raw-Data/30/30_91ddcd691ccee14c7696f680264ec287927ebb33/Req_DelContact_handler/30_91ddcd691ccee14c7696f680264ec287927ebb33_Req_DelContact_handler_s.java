 package socket.packet.handlers.listened;
 
 import session.session;
 import socket.packet.handlers.Listen_handler;
import socket.packet.handlers.senders.contact_handlers.ContactDeleted_handler;
 import socket.packet.objects.IdAndData;
 
 public class Req_DelContact_handler extends Listen_handler {
 
 	public Req_DelContact_handler(session sess, Object packet) 
 	{
 		IdAndData pck = (IdAndData)packet;
 		sess.DelContact(pck.getUid(),Integer.decode(pck.getDat()));
		ContactDeleted_handler pack = new ContactDeleted_handler(pck.getUid());
		pack.Send(sess.getSocket());
 	}
 
 }
