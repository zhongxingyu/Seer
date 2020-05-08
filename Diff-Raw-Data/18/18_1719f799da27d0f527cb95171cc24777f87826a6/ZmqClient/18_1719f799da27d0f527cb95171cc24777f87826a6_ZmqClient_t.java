 package org.flupes.ljf.grannyroomba.net;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.zeromq.ZMQ;
import org.zeromq.ZMQException;
 
 public class ZmqClient {
 
 	protected String m_server;
 	protected int m_port;
 	protected String m_url;
 	protected ZMQ.Context m_context;
 	protected ZMQ.Socket m_socket;
 	protected boolean m_connected;
 	
 	protected static Logger s_logger = LoggerFactory.getLogger("grannyroomba");
 	
 	public ZmqClient(String server, int port) {
 		m_server = server;
 		m_port = port;
 		m_url = "tcp://"+m_server+":"+Integer.toString(m_port);
 		m_connected = false;
 	}
 	
 	public boolean isConnected() {
 		return m_connected;
 	}
 	
 	public synchronized void connect() {
 		if ( ! m_connected ) {
 			m_context = ZMQ.context(1);
 			m_socket = m_context.socket(ZMQ.REQ);
 			m_socket.connect (m_url);
			m_socket.setSendTimeOut(500);
			m_socket.setReceiveTimeOut(1500);
 			m_connected = true;
 			s_logger.info("Client connected to ["+m_url+"]");
 		}
 	}
 	
 	public synchronized void disconnect() {
		m_socket.disconnect(m_url);
 		m_socket.close();
 		m_context.term();
 		m_connected = false;
 		s_logger.info("Client of [" + m_url + "] disconnected.");
 	}
 	
	protected byte[] reqrep(byte[] buffer) throws ZMQException {
		boolean req = m_socket.send(buffer);
		if ( ! req ) {
			throw new ZMQException(m_socket.base().errno());
		}
		byte[] rep = m_socket.recv(0);
		if ( null == rep ) {
			throw new ZMQException(m_socket.base().errno());
		}
		return rep;
	}
 }
