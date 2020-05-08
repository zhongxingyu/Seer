 /*
  * Copyright (c) 2009, Julian Gosnell
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are
  * met:
  *
  *     * Redistributions of source code must retain the above copyright
  *     notice, this list of conditions and the following disclaimer.
  *
  *     * Redistributions in binary form must reproduce the above
  *     copyright notice, this list of conditions and the following
  *     disclaimer in the documentation and/or other materials provided
  *     with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
  * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.dada.jms;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.util.UUID;
 
 import javax.jms.Destination;
 import javax.jms.JMSException;
 import javax.jms.MessageConsumer;
 import javax.jms.MessageListener;
 import javax.jms.MessageProducer;
 import javax.jms.Queue;
 import javax.jms.Session;
 
 import org.dada.slf4j.Logger;
 import org.dada.slf4j.LoggerFactory;
 
 public abstract class AbstractClient implements MessageListener, Serializable {
 
 	protected static final ThreadLocal<Session> CURRENT_SESSION = new ThreadLocal<Session>(); // TODO: encapsulate
 
 	protected long timeout;
 	protected Class<?> interfaze;
 	protected Destination destination;
 	protected boolean trueAsync;
 
 	protected transient Logger logger;
 	protected transient UUID uuid;
 	protected transient Session session;
 	protected transient MessageProducer producer;
 	protected transient Queue resultsQueue;
 	protected transient MessageConsumer consumer;
 	protected /* final */ SimpleMethodMapper mapper;
 	protected transient int count; // used by subclasses
 
 	public AbstractClient(Session session, Destination destination, Class<?> interfaze, long timeout, boolean trueAsync) throws JMSException {
 		init(session, destination, interfaze, timeout, trueAsync);
 	}
 
 	protected void init(Session session, Destination destination, Class<?> interfaze, long timeout, boolean trueAsync) throws JMSException {
 		logger = LoggerFactory.getLogger(getClass());
 		this.interfaze = interfaze;
 		mapper = new SimpleMethodMapper(interfaze);
 		this.destination = destination;
 		this.trueAsync = trueAsync;
 		this.timeout = timeout;
 		this.uuid = UUID.randomUUID();
 
 		this.session = session;
 		this.producer = session.createProducer(destination);
 		this.resultsQueue = session.createQueue(interfaze.getCanonicalName() + "." + uuid);
 		this.consumer = session.createConsumer(resultsQueue);
 		this.consumer.setMessageListener(this);
 	}
 
 	//@Override
 	private void writeObject(ObjectOutputStream oos) throws IOException {
 		oos.writeObject(destination);
 		oos.writeObject(interfaze);
 		oos.writeLong(timeout);
 		oos.writeBoolean(trueAsync);
 	}
 
 	//@Override
 	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
 		Session session = getCurrentSession();
 		Destination destination = (Destination) ois.readObject();
 		Class<?> interfaze = (Class<?>) ois.readObject();
 		long timeout = ois.readLong();
 		boolean trueAsync = ois.readBoolean();
 		try {
 			init(session, destination, interfaze, timeout, trueAsync);
 		} catch (JMSException e) {
 			logger.error("unexpected problem reconstructing client proxy", e);
 		}
 	}
 
 	public static void setCurrentSession(Session session) {
 		CURRENT_SESSION.set(session);
 	}
 
 	public static Session getCurrentSession() {
 		return CURRENT_SESSION.get();
 	}
 
	public Destination getDestination() {
		return destination;
	}
 
 }
