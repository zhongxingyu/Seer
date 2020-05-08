 package org.kisst.jms;
 
 import java.util.Hashtable;
 
import javax.jms.ConnectionFactory;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 
 import org.kisst.cfg4j.Props;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class MqSeriesJmsSystem extends JmsSystem {
 	private final static Logger logger=LoggerFactory.getLogger(MqSeriesJmsSystem.class);
 
 	public MqSeriesJmsSystem(Props props) { super(props); }
 
	@Override protected ConnectionFactory createConnectionFactory() {
 		return jantje(props.getString("jndiQueuemanager"));
 	}
 	/*
 	protected ConnectionFactory createConnectionFactory2() {
 //MQSESSION t=null;
 		MQQueueConnectionFactory cf=new MQQueueConnectionFactory();
 		//cf.setHostName("localhost");
 		try {
 			cf.setPort(1414);
 			cf.setHostName("172.30.247.38"); //??
 			cf.setTransportType(JMSC.MQJMS_TP_CLIENT_MQ_TCPIP);
 			//cf.setTransportType(JMSC.MQJMS_TP_DIRECT_TCPIP);
 			cf.setQueueManager("WINONT");
 			cf.setChannel("ONT.ESBCLIENT.CH"); //SYSTEM.DEF.SVRCONN
 		} catch (JMSException e) { throw new RuntimeException(e); }
 		return cf;
 	}
 	*/
 	
	public ConnectionFactory jantje(String name) {
         Hashtable<String, String> env= new Hashtable<String,String>();
         env.put( "java.naming.factory.initial", "com.sun.jndi.fscontext.RefFSContextFactory" );
         env.put( "java.naming.provider.url", "file:///c:/tmp/preont");
         env.put( "java.naming.security.authentication", "none" );
         if( !"none".equals( env.get("java.naming.security.authentication"))) {
             env.put( "java.naming.security.principal", props.getString("username",null));
             env.put( "java.naming.security.credentials", props.getString("password",null));
         }
         
 		InitialContext jndiContext;
 		try {
 			jndiContext = new InitialContext( env);
 			logger.debug("Looking up {}",name);
			return (ConnectionFactory) jndiContext.lookup( name );
 		} catch (NamingException e) { throw new RuntimeException(e); }
 
 	}
 }
