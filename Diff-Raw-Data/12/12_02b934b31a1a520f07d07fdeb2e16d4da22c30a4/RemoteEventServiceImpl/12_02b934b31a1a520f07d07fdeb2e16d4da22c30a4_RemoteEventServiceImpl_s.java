 /**
  * Licensed to TOMOTON nv under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  TOMOTON nv licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package gwtx.event.remote.server;
 
 import gwtx.event.remote.client.RemoteEventService;
 import gwtx.event.remote.shared.BufferOverflowException;
 import gwtx.event.remote.shared.RemoteEventBusException;
 import gwtx.event.remote.shared.RemoteGwtEvent;
 import gwtx.event.remote.shared.RemoteGwtEvent.Type;
 import gwtx.event.remote.shared.RemoteSessionId;
 import gwtx.event.remote.shared.ServerId;
 import gwtx.event.remote.shared.SourceId;
 
 import java.security.SecureRandom;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ThreadFactory;
 import java.util.concurrent.TimeUnit;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 import com.lmax.disruptor.EventFactory;
 import com.lmax.disruptor.EventProcessor;
 import com.lmax.disruptor.NoOpEventProcessor;
 import com.lmax.disruptor.RingBuffer;
 import com.lmax.disruptor.SequenceBarrier;
 import com.lmax.disruptor.SingleThreadedClaimStrategy;
 import com.lmax.disruptor.SleepingWaitStrategy;
 
 
 /**
  * Implementation of the <code>RemoteEventService</code>, which relies on 
  * GWT RPC.
  * 
  * @author Dann Martens
  */
 public class RemoteEventServiceImpl extends RemoteServiceServlet implements RemoteEventService {
 
 	/* Managed UID. */
 	private static final long serialVersionUID = 1L;
 
 	private static final int DEFAULT_BUFFER_SIZE = 1024;
 	
 	private static final long DEFAULT_MINIMUM_WAITING_TIME = 1000L;
 	
 	private static final long DEFAULT_MAXIMUM_WAITING_TIME = 10000L;
 	
 	static final String DEFAULT_NAME = "DEFAULT";
 	
 	private static class Referer<T> {
 
 		private T referenced;
 
 		public T getReferenced() {
 			return referenced;
 		}
 
 		public void setReferenced(T referenced) {
 			this.referenced = referenced;
 		}
 		
 	}
 	
 	private final static EventFactory<Referer<RemoteGwtEvent<?>>> EVENT_FACTORY = new EventFactory<Referer<RemoteGwtEvent<?>>>() {
 		@SuppressWarnings({ "rawtypes", "unchecked" })
 		public Referer<RemoteGwtEvent<?>> newInstance() {
 			return (Referer<RemoteGwtEvent<?>>) new Referer();
 		}
 	};
 	
 	private String name = DEFAULT_NAME;
 	
 	private int bufferSize = DEFAULT_BUFFER_SIZE;
 	
 	private long minimumWaitingTime = DEFAULT_MINIMUM_WAITING_TIME;
 
 	private long maximumWaitingTime = DEFAULT_MAXIMUM_WAITING_TIME;
 	
 	private RingBuffer<Referer<RemoteGwtEvent<?>>> ringBuffer;
 		
 	private static SecureRandom random = new SecureRandom();
 		
 	private ServerId serverId = new ServerId(random.nextInt());
 
 	private SequenceBarrier barrier;
 	
 	private SessionManager sessionManager = SessionManagerFactory.newDefaultInstance();
 	
 	private ExecutorService executor;
 	
 	@Override
 	public void init(ServletConfig config) throws ServletException {
 		//? Mandatory invocation by contract.
 		super.init(config);
 		//? Read parameters, if any.
 		String candidateName = config.getInitParameter("name");
 		if(candidateName != null && candidateName.length() > 0) {
 			name = candidateName;
 		}
 		try {
 			String value = config.getInitParameter("bufferSize");
 			bufferSize = Integer.parseInt(value);
 		} catch (Exception ignore) {}
 		try {
 			String value = config.getInitParameter("minimumWaitingTime");
 			minimumWaitingTime = Integer.parseInt(value);
 		} catch (Exception ignore) {}
 		try {
 			String value = config.getInitParameter("maximumWaitingTime");
 			maximumWaitingTime = Integer.parseInt(value);
 		} catch (Exception ignore) {}				
 		//? Local initialization.
 		ringBuffer = new RingBuffer<Referer<RemoteGwtEvent<?>>>(EVENT_FACTORY, new SingleThreadedClaimStrategy(bufferSize), new SleepingWaitStrategy());
 		barrier = ringBuffer.newBarrier();
 		EventProcessor eventProcessor = new NoOpEventProcessor(ringBuffer);
 		ringBuffer.setGatingSequences(eventProcessor.getSequence());
 		//? Set up event firing thread.
         final SecurityManager securityManager = System.getSecurityManager();
         final ThreadGroup group = (securityManager != null)? securityManager.getThreadGroup() :  Thread.currentThread().getThreadGroup();
 		executor = Executors.newSingleThreadExecutor(new ThreadFactory() {
 			@Override
 			public Thread newThread(Runnable runnable) {
 	            Thread result = new Thread(group, runnable, RemoteEventServiceImpl.class.getSimpleName(), 0);
 				  if (!result.isDaemon())
 				      result.setDaemon(true);
 				  if (result.getPriority() != Thread.NORM_PRIORITY)
 				      result.setPriority(Thread.NORM_PRIORITY);
 				  return result;
 			}
 		});
 		//? Register this remote event service the server-side handler.
 		RemoteEventHandler.getInstance().register(name, this);
 	}
 
 	@Override
 	public void fireEvent(final RemoteGwtEvent<?> event) {
 		executor.execute(new Runnable() {
 			@Override
 			public void run() {
 				long sequence = ringBuffer.next();
 				Referer<RemoteGwtEvent<?>> referer = ringBuffer.get(sequence);
 				referer.setReferenced(event);
 				//? Make the event available to EventProcessors
 				ringBuffer.publish(sequence); 
 			}			
 		});
 	}
 	
 	public SessionManager getSessionManager() {
 		return sessionManager;
 	}
 	
 	@Override
 	public RemoteSessionId newSession() {
 		SourceId sourceId = sessionManager.newSession(serverId);
 		System.err.println("Created new session for source " + sourceId.asString());
 		return new RemoteSessionId(serverId, sourceId);
 	}
 
 	@Override
 	public void invalidateSession() {
 		SourceId sourceId = sessionManager.service(this.getThreadLocalRequest(), this.getThreadLocalResponse());
 		sessionManager.invalidate(sourceId);
 	}
 
 	@Override
 	public <H> boolean addSubscription(Type<H> type) {
 		SourceId sourceId = sessionManager.service(this.getThreadLocalRequest(), this.getThreadLocalResponse());
 		Session session = sessionManager.getSession(sourceId);
 		return session.subscribe(type);
 	}
 
 	@Override
 	public <H> boolean removeSubscription(Type<H> type) {
 		SourceId sourceId = sessionManager.service(this.getThreadLocalRequest(), this.getThreadLocalResponse());
 		Session session = sessionManager.getSession(sourceId);
 		return session.unsubscribe(type);
 	}
 
 	@Override
 	public List<RemoteGwtEvent<?>> getAvailableEvents() throws RemoteEventBusException {
 		//! System.err.println("Get events");
 		long startTime = System.nanoTime();
 		List<RemoteGwtEvent<?>> result = new ArrayList<RemoteGwtEvent<?>>();
 		SourceId sourceId = sessionManager.service(this.getThreadLocalRequest(), this.getThreadLocalResponse());
 		//! System.err.println("SourceId from Header (get) " + sourceId);
 		if(sourceId == null)
 			throw new RemoteEventBusException("Unknown source id!");
 		Session session = sessionManager.getSession(sourceId);
 		if(session == null) {
 			throw new RemoteEventBusException("No session for source!");
 		}
 		long lastSequence = session.getLastSequence();
 		long cursor = ringBuffer.getCursor();
 		if(lastSequence == -1) {
 			lastSequence = ringBuffer.getCursor() == -1 ? -1 : cursor;
 		}
		//! System.err.println("Cursor is at: " + cursor + ", lastSequence is at: " + lastSequence + " ,bufferSize is: " + ringBuffer.getBufferSize());
 		long waitSequence = lastSequence + 1;		
 		if((cursor - lastSequence) > ringBuffer.getBufferSize()) {
 			throw new BufferOverflowException();
 		}
 		if(!getAsManyAsPossibleDuringMinimumWaitingTime(waitSequence, startTime, result)) {
 			getAtLeastOneDuringMaximumWaitingTime(waitSequence, startTime, result);
 		}
 		session.updateSequence(lastSequence + result.size());
		//! System.err.println(">>> Returning after elapsed: " + ((double) (System.nanoTime() - startTime) / 1000000000.0));
 		return result;
 	}
 
 	private boolean getAsManyAsPossibleDuringMinimumWaitingTime(long sequence, long startTimeInNanos, List<RemoteGwtEvent<?>> eventList) {
 		long elapsedNanos = System.nanoTime() - startTimeInNanos;
 	    boolean result = false;
 	    long whatsLeftOfMinimumWaitingTime = minimumWaitingTime - (elapsedNanos / 1000000L);
 	    while(whatsLeftOfMinimumWaitingTime > 0) { 
 			try {
				//! System.err.println("Waiting for " + sequence + " (MIN)...");
 				long currentSequence = barrier.waitFor(sequence, whatsLeftOfMinimumWaitingTime, TimeUnit.MILLISECONDS);
 				if(currentSequence >= sequence) { //? Something happened.
 					Referer<RemoteGwtEvent<?>> referer = ringBuffer.get(sequence++);
 					RemoteGwtEvent<?> event = referer.getReferenced();
 					eventList.add(event);
 					result = true;
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
			whatsLeftOfMinimumWaitingTime -= ((System.nanoTime() - startTimeInNanos) / 1000000L);
 	    }
 		return result;
 	}
 	
 	private void getAtLeastOneDuringMaximumWaitingTime(long sequence, long startTimeInNanos, List<RemoteGwtEvent<?>> eventList) {
 		long elapsedNanos = System.nanoTime() - startTimeInNanos;
 		//! System.err.println("Elapsed " + (elapsedNanos / 1000000L));
 		long whatsLeftOfMaximumWaitingTime = maximumWaitingTime - (elapsedNanos / 1000000L);
 		try {
 			//! System.err.println("Waiting for " + sequence + " (MAX)... " + whatsLeftOfMaximumWaitingTime);
 			long currentSequence = barrier.waitFor(sequence, whatsLeftOfMaximumWaitingTime, TimeUnit.MILLISECONDS);
 			if(currentSequence >= sequence) { //? Something happened.
 				Referer<RemoteGwtEvent<?>> referer = ringBuffer.get(sequence++);
 				RemoteGwtEvent<?> event = referer.getReferenced();
 				eventList.add(event);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 }
