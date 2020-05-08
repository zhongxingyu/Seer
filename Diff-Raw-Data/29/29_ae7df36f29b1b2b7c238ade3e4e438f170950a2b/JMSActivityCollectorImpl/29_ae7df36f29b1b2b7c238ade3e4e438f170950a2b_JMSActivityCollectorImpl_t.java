 /*
  * JBoss, Home of Professional Open Source
  * Copyright 2008-11, Red Hat Middleware LLC, and others contributors as indicated
  * by the @authors tag. All rights reserved.
  * See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  * This copyrighted material is made available to anyone wishing to use,
  * modify, copy, or redistribute it subject to the terms and conditions
  * of the GNU Lesser General Public License, v. 2.1.
  * This program is distributed in the hope that it will be useful, but WITHOUT A
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
  * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public License,
  * v.2.1 along with this distribution; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  * MA  02110-1301, USA.
  */
 package org.savara.sam.internal.collector;
 
 import java.util.TimerTask;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.annotation.PostConstruct;
 import javax.annotation.PreDestroy;
 import javax.jms.Connection;
 import javax.jms.ConnectionFactory;
 import javax.jms.MessageProducer;
 import javax.jms.Queue;
 import javax.jms.Session;
 
 import org.hornetq.api.core.TransportConfiguration;
 import org.hornetq.api.jms.HornetQJMSClient;
 import org.hornetq.api.jms.JMSFactoryType;
 import org.hornetq.core.remoting.impl.netty.NettyConnectorFactory;
 import org.savara.sam.activity.ActivityModel.Activity;
 import org.savara.sam.collector.ActivityCollector;
 
 public class JMSActivityCollectorImpl implements ActivityCollector {
 
 	private static final String ACTIVITY_MONITOR_SERVER = "ActivityMonitorServer";
 
 	private static final Logger LOG=Logger.getLogger(JMSActivityCollectorImpl.class.getName());
 	
 	private Connection _connection;
 	private Session _session;
 	private MessageProducer _producer;
 	private javax.jms.BytesMessage _currentMessage;
 	private int _messageCounter=0;
 	private java.util.Timer _timer;
 	private java.util.TimerTask _timerTask;
 	
 	@PostConstruct
 	public void init() {
 		try {
 			Queue queue = HornetQJMSClient.createQueue(ACTIVITY_MONITOR_SERVER);
 
 			TransportConfiguration transportConfiguration = new TransportConfiguration(NettyConnectorFactory.class.getName());
 
 			ConnectionFactory cf = (ConnectionFactory) HornetQJMSClient.createConnectionFactoryWithoutHA(JMSFactoryType.CF, transportConfiguration);
 
 			_connection = cf.createConnection();
 
 			_session = _connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
 
 			_producer = _session.createProducer(queue);
 		} catch (Exception e) {
 			LOG.log(Level.SEVERE, "Failed to setup JMS connection", e);
 		}
 		
 		_timer = new java.util.Timer();
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	public void process(Activity act) {
 		
 		try {
 			 byte[] mesg=act.toByteArray();
 	
 			 synchronized(this) {
 				 
 				 // Cancel any current scheduled task
				 if (_timerTask == null) {
					 _timerTask = new TimerTask() {
							public void run() {
								sendMessage();
							}						 
						 };
						 
					 // Schedule send
					 _timer.schedule(_timerTask, 500);
 				 }
 				 
 				 if (_currentMessage == null) {
 					 _currentMessage = _session.createBytesMessage();
 				 }
 		
 				 _currentMessage.writeInt(mesg.length);
 				 _currentMessage.writeBytes(mesg);
 				 _messageCounter++;
 
 				 if (_messageCounter > 1000) {
 					 sendMessage();
 				 }
 			 }
 			 
 		} catch (Exception e) {
 			LOG.log(Level.SEVERE, "Failed to send activity event", e);
 		}
 	}
 	
 	protected synchronized void sendMessage() {
 		if (_currentMessage != null) {
 			try {
 				 // Send message
 				 _currentMessage.writeInt(0);
 				 _producer.send(_currentMessage);
 				 
 				 if (LOG.isLoggable(Level.FINEST)) {
 					 LOG.finest("Sent "+_messageCounter+" activity events");
 				 }
 				 
 				 _currentMessage = null;
 				 _messageCounter = 0;
 				 
 				 if (_timerTask != null) {
 					 _timerTask.cancel();
 					 _timerTask = null;
 				 }
 				 
 			} catch (Exception e) {
 				LOG.log(Level.SEVERE, "Failed to send activity event", e);
 			}	
 		}
 	}
 
 	@PreDestroy
 	public void close() {
 		try {
 			_session.close();
 	        _connection.close();
 		} catch (Exception e) {
 			LOG.log(Level.SEVERE, "Failed to close JMS connection", e);
 		}
 		
 		_timer.cancel();
 	}
 }
