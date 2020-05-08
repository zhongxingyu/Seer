 package org.omo.jms;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Proxy;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
 
 import javax.jms.Destination;
 import javax.jms.JMSException;
 import javax.jms.Message;
 import javax.jms.MessageConsumer;
 import javax.jms.MessageListener;
 import javax.jms.MessageProducer;
 import javax.jms.ObjectMessage;
 import javax.jms.Session;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 // TODO: reuse more code between these classes...
 // TODO: support topic/multi-shot result aggregation
 // TODO: server should probably support a lifecycle ?
 // TODO: test multiple client/topic/multiple server scenarios...
 
 public class RemotingFactory<T> {
 
 	private final Session session;
 	private final Class<?> interfaze;
 	private final SimpleMethodMapper mapper;
 	private final Destination invocationDestination;
 	private final long timeout;
 	
 	public RemotingFactory(Session session, Class<?> interfaze, DestinationFactory factory, long timeout) throws JMSException {
 		this(session, interfaze, factory.create(session, interfaze.getCanonicalName()), timeout);
 	}
 	
 	public RemotingFactory(Session session, Class<?> interfaze, Destination invocationDestination, long timeout) throws JMSException {
 		this.session = session;
 		this.interfaze = interfaze;
 		this.mapper = new SimpleMethodMapper(interfaze);
 		this.invocationDestination = invocationDestination;
 		this.timeout = timeout;
 	}
 	
 	//--------------------------------------------------------------------
 	
 	public class Server implements MessageListener {
 
 		private final Log log;
 
 		private final T target;
 		private final MessageProducer producer;
 		private final MessageConsumer consumer;
 
 		public Server(T target, Destination invocationDestination) throws JMSException {
 			this.target = target;
 			log = LogFactory.getLog(Server.class);
 			producer = session.createProducer(null);
 			consumer = session.createConsumer(invocationDestination);
 			log.info("consuming messages on: " + invocationDestination);
 			consumer.setMessageListener(this);
 		}
 
		protected Executor executor = new ThreadPoolExecutor(10, 100, 600, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(100));
		
 		@Override
 		public void onMessage(final Message message) {
 			new Thread(new Runnable() { // TODO: use a thread pool
 				@Override
 				public void run() {
 					process(message);
 				}
 			}).start(); // TODO: use ThreadPool
 		}
 		
 		public void process(Message message) {
 			String correlationId = null;
 			Destination replyTo = null;
 			boolean isException = false;
 			Object result = null;
 			// input
 			try {
 				correlationId = message.getJMSCorrelationID();
 				replyTo = message.getJMSReplyTo();
 				ObjectMessage request = (ObjectMessage)message;
 				Client.setCurrentSession(session);
 				Invocation invocation = (Invocation)request.getObject();
 				int methodIndex = invocation.getMethodIndex();
 				Object args[] = invocation.getArgs();
 				Method method = mapper.getMethod(methodIndex);
 				log.trace("RECEIVING: " + method + " <- " + message.getJMSDestination());
 				result = method.invoke(Server.this.target, args);
 			} catch (JMSException e) {
 				isException = true;
 				result = e;
 			} catch (IllegalAccessException e) {
 				isException = true;
 				result = e;
 			} catch (InvocationTargetException e) {
 				isException = true;
 				result = e.getTargetException();
 			}
 
 			if (correlationId != null && replyTo != null) {
 				ObjectMessage response = null;
 				try {
 					response = session.createObjectMessage();
 					response.setJMSCorrelationID(correlationId);
 					Results results = new Results(isException, result);
 					response.setObject(results);
 					log.trace("SENDING: " + results + " -> " + replyTo);
 					producer.send(replyTo, response);
 				} catch (JMSException e) {
 					log.warn("problem replying to message: " + response, e);
 				}
 			}
 		}
 	}
 	
 	//----------------------------------------------------------------------------
 	
 	public T createServer(T target) throws JMSException {
 		new Server(target, invocationDestination);
 		return target;
 	}
 
 	public T createServer(T target, Destination invocationDestination) throws JMSException {
 		new Server(target, invocationDestination);
 		return target;
 	}
 	
 	public T createSynchronousClient() throws IllegalArgumentException, JMSException {
 		ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
 		return (T)Proxy.newProxyInstance(contextClassLoader, new Class[]{interfaze}, new SynchronousClient(session, invocationDestination, interfaze, timeout));
 	}
 	
 	public T createSynchronousClient(Destination invocationDestination) throws IllegalArgumentException, JMSException {
 		ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
 		return (T)Proxy.newProxyInstance(contextClassLoader, new Class[]{interfaze}, new SynchronousClient(session, invocationDestination, interfaze, timeout));
 	}
 	
 	public AsynchronousClient createAsynchronousClient() throws JMSException {
 		return new AsynchronousClient(session, invocationDestination, interfaze, timeout);
 	}
 }
