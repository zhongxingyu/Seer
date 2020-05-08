 // ========================================================================
 // Copyright 2008-2009 NEXCOM Systems
 // ------------------------------------------------------------------------
 // Licensed under the Apache License, Version 2.0 (the "License");
 // you may not use this file except in compliance with the License.
 // You may obtain a copy of the License at 
 // http://www.apache.org/licenses/LICENSE-2.0
 // Unless required by applicable law or agreed to in writing, software
 // distributed under the License is distributed on an "AS IS" BASIS,
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 // See the License for the specific language governing permissions and
 // limitations under the License.
 // ========================================================================
 
 package org.cipango.server.session;
 
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.atomic.AtomicLong;
 import java.util.concurrent.locks.ReentrantLock;
 
 import javax.servlet.sip.SipSession;
 
 import org.cipango.log.event.Events;
 import org.cipango.server.ID;
 import org.cipango.server.Server;
 import org.cipango.server.SipRequest;
 import org.cipango.server.SipResponse;
 import org.cipango.server.transaction.ClientTransaction;
 import org.cipango.server.transaction.ServerTransaction;
 import org.cipango.server.transaction.Transaction;
 import org.cipango.sipapp.SipAppContext;
 import org.cipango.util.TimerList;
 import org.cipango.util.TimerQueue;
 import org.cipango.util.TimerTask;
 import org.eclipse.jetty.util.component.AbstractLifeCycle;
 import org.eclipse.jetty.util.log.Log;
 import org.eclipse.jetty.util.statistic.CounterStatistic;
 
 /**
  * Holds and manages all SIP related sessions.
  *  
  * SIP counterpart of HTTP {@link org.mortbay.jetty.SessionManager}. SIP related sessions consists of three different kinds 
  * of sessions, managed in a hierarchical structure: the root is the call session which contains SipApplicationSessions 
  * which contains in turn SipSessions. 
  * 
  * Call session is a container structure (not exposed in Sip Servlets API) used to group all data related to a SIP call.
  * Call sessions are processed in a pseudo-transactional manner to control concurrency and may be scheduled for execution.  
  */
 public class SessionManager extends AbstractLifeCycle
 {   
     protected Map<String, CSession> _callSessions = new HashMap<String, CSession>(1024);
     protected TimerQueue<CSession> _queue = new TimerQueue<CSession>(1024);
     
     private Thread _scheduler;
     private int _priorityOffset;
     
     private File _storeDir;
     private Server _server;
 	
     // statistics 
     private AtomicLong _statsStartedAt = new AtomicLong(-1);
     private CounterStatistic _sessionsStats = new CounterStatistic();
     private int _callsThreshold = 0;
     	
     public SessionManager()
     { 
     }
     
     @Override
     protected void doStart() throws Exception
     {
     	if (_storeDir != null)
         {
             if (!_storeDir.exists())
                 _storeDir.mkdir();
             
             restoreSessions();
         }
     	
         new Thread(new Scheduler()).start();
         super.doStart();
     }
     
     @Override
     protected void doStop() throws Exception
     {
     	super.doStop();
     	
     	if (_scheduler != null)
     		_scheduler.interrupt();
     	
     	_callSessions.clear();
     }
     
     public void setPriorityOffset(int priorityOffset)
     {
     	_priorityOffset = priorityOffset;
     }
     
     public void setStoreDir(File storeDir)
     {
     	_storeDir = storeDir;
     }
     
     public SessionScope openScope(String id)
     {
     	CSession callSession = null;
     	
     	synchronized (_callSessions)
     	{
     		callSession =  _callSessions.get(id);
     		if (callSession == null)
     		{
     			callSession = newCall(id);
     			
     			_callSessions.put(callSession.getId(), callSession);
     			
     			if (_statsStartedAt.get() > 0)
 					_sessionsStats.increment();
     			
     			if (_callsThreshold > 0 && getCalls() == _callsThreshold)
     				Events.fire(Events.CALLS_THRESHOLD_READCHED, "Calls threashlod reached: " + getCalls());
     		}
     	}
     	return new SessionScope(callSession._lock.tryLock() ? callSession : null);
     }
     
     public SessionScope openScope(CallSession callSession)
     {
     	CSession csession = (CSession) callSession;
     	csession._lock.lock();
     	return new SessionScope(csession);
     }
     
     public void close(CSession callSession)
     {
     	try
     	{
 	    	int holds = callSession._lock.getHoldCount();
 	    	
 	    	if (holds == 1)
 	    	{
 	    		callSession.invalidateSessionsIfReady();
 	    		
 	    		long time = callSession.nextExecutionTime();
 	
 	        	if (time > 0)
 	        	{
 	        		while (time < System.currentTimeMillis())
 	        		{
 	        			callSession.runTimers();
 	        			time = callSession.nextExecutionTime();
 	        			
 	        			if (time < 0)
 	        				break;
 	        		}
 	        		
 	        		if (time > 0)
 	        		{
 	        			synchronized (_queue)
 	        			{
 	        				//_queue.remove(callSession); // TODO O(n) ?
 	        				_queue.offer(callSession, time);
 	        				_queue.notifyAll();
 	        			}
 	        		}
 	        	}
 	        	if (callSession.isDone())
 	        	{
	        		boolean removed = removeSession(callSession);
	        		if (removed && _statsStartedAt.get() > 0)
 	        			_sessionsStats.decrement();
 	        	}
 	        	else
 	        	{
 	        		saveSession(callSession);
 	        	}
 	    	}
     	}
     	finally
     	{
     		callSession._lock.unlock();
     	}
     }
     
    /**
     * @return <code>true</code> if callSession contains the session.
     */
    protected boolean removeSession(CSession callSession)
     {
     	if (Log.isDebugEnabled())
 			Log.debug("CallSession " + callSession.getId() + " is done.");
 		
 		synchronized (_callSessions)
     	{
    		return _callSessions.remove(callSession.getId()) != null;
     	}
     }
     
     protected CSession newCall(String id)
     {
     	return new CSession(id);
     }
     
     public CallSession get(String callId)
     {
     	synchronized (_callSessions)
     	{
 			return (CallSession) _callSessions.get(callId);
 		}
     }
     
     private void runTimers(CSession csession)
 	{
     	csession._lock.lock();
 		try
 		{
 			csession.runTimers(); // TODO thread pool for app timers at least
 		}
 		finally
 		{
 			close(csession);
 		}
 	}
     
     public void saveSession(CSession session)
     {
     	if (_storeDir == null || !_storeDir.exists())
         {
             return;
         }
         
         if (!_storeDir.canWrite())
         {
             Log.warn ("Unable to save session. Session persistence storage directory " + _storeDir.getAbsolutePath() + " is not writeable");
             return;
         }
         
         try
         {
             File file = new File(_storeDir, session.getId());
             if (file.exists())
                 file.delete();
             file.createNewFile();
             FileOutputStream fos = new FileOutputStream(file);
             session.save(fos);
             fos.close();
         }
         catch (Exception e)
         {
             Log.warn("Problem persisting session " + session.getId(), e);
         }
     }
     
     public void restoreSessions() throws Exception
     {
     	if (_storeDir == null || !_storeDir.exists())
     	{
     		return;
     	}
     	
     	if (!_storeDir.canRead())
     	{
     		Log.warn("unable to restore sessions: cannot read from store directory " + _storeDir.getAbsolutePath());
     		return;
     	}
     	File[] files = _storeDir.listFiles();
     	for (int i = 0; files != null && i < files.length; i++)
     	{
     		try
     		{
     			FileInputStream in = new FileInputStream(files[i]);
     			CallSession session = restoreSession(in);
     			in.close();
     			files[i].delete();
     		}
     		catch (Exception e)
     		{
     			Log.warn("problem restoring session " + files[i].getName(), e);
     		}
     	}
     }
     
     public CallSession restoreSession(FileInputStream fis) throws Exception
     {
     	DataInputStream in = new DataInputStream(fis);
     	String id = in.readUTF();
     	int nbAppSessions = in.readInt();
     	
     	for (int i = 0; i < nbAppSessions; i++)
     	{
     		String appId = in.readUTF();
     		System.out.println("read call: " + id + " / " + appId);
     	}
     	    	
     	return null;
     }
     
     public void setServer(Server server)
 	{
 		_server = server;
 	}
 	
 	public Server getServer()
 	{
 		return _server;
 	}
 	
 	// ------ statistics --------
 	
     public int getCalls()
     {
         return (int) _sessionsStats.getCurrent();
     }
     
     public int getMaxCalls()
     {
         return (int) _sessionsStats.getMax();
     }
         
     public long getTotalCalls()
     {
         return _sessionsStats.getTotal();
     }
 
 	public int getCallsThreshold()
 	{
 		return _callsThreshold;
 	}
 
 	public void setCallsThreshold(int callsThreshold)
 	{
 		_callsThreshold = callsThreshold;
 	}
 	
     public void statsReset() 
     {
         _statsStartedAt.set(_statsStartedAt.get() == -1 ? -1l : System.currentTimeMillis());
         _sessionsStats.reset();
     }
     
     public void setStatsOn(boolean on) 
     {
         if (on && _statsStartedAt.get() != -1) 
             return;
         
         statsReset();
         _statsStartedAt.set(on ? System.currentTimeMillis() : -1);
     }
     
 	public boolean isStatsOn() 
 	{
 		return _statsStartedAt.get() != -1;
 	}
 	
 	/**
 	 * Pseudo-transactional scope for session processing. 
 	 */
 	public class SessionScope
 	{
 		private CSession _csession;
 		
 		public SessionScope(CSession csession)
 		{
 			_csession = csession;
 		}
 		
 		public CallSession getCallSession()
 		{
 			return _csession;
 		}
 		
 		public void close()
 		{
 			if (_csession != null)
 				SessionManager.this.close(_csession);
 		}
 	}
 	
     class Scheduler implements Runnable
     {
     	public void run()
     	{
     		_scheduler = Thread.currentThread();
     		String name = _scheduler.getName();
     		_scheduler.setName("session-scheduler");
     		int priority = _scheduler.getPriority();
     		
     		try
     		{
     			_scheduler.setPriority(priority + _priorityOffset);
     			do
     			{
     				try
     				{
     					CSession csession;
     					long timeout;
     					
 						synchronized (_queue)
 						{
 							csession = (CSession) _queue.peek();
 							timeout = (csession != null ? csession.nextExecutionTime() - System.currentTimeMillis() : Long.MAX_VALUE);
 							
 							if (timeout > 0)
 							{
 								if (Log.isDebugEnabled())
 									Log.debug("waiting {} ms for call session: {}", timeout, csession);
 								_queue.wait(timeout);
 							} 
 							else
 							{
 								_queue.poll();
 							}
 						}
 						if (timeout <= 0)
 						{
 							if (Log.isDebugEnabled())
 								Log.debug("running timers for call session: {}", csession);
 							runTimers(csession);
 						}
     				}
     				catch (InterruptedException e) { continue; }
     				catch (Throwable t) { Log.warn(t); }
     			}
     			while (isRunning()); 
     		}
     		finally
     		{
     			_scheduler.setName(name);
     			_scheduler.setPriority(priority);
     			_scheduler = null;
     			
     			String exit = "session-scheduler exited";
     			if (isStarted())
     				Log.warn(exit);
     			else
     				Log.debug(exit);
     		}
     	}
     }
     
     public class CSession extends TimerQueue.Node implements CallSession
     {
     	protected String _id;
     	
     	protected TimerList _timers = new TimerList();
     	
     	protected List<ServerTransaction> _serverTransactions = new ArrayList<ServerTransaction>(1);
     	protected List<ClientTransaction> _clientTransactions = new ArrayList<ClientTransaction>(1);
     	protected List<AppSession> _appSessions = new ArrayList<AppSession>(1);
     	
     	private ReentrantLock _lock = new ReentrantLock();
     	
     	public CSession(String id)
     	{
     		_id = id;
     	}
     	
     	public String getId()
     	{
     		return _id;
     	}
     	
     	public Server getServer()
     	{
     		return SessionManager.this.getServer();
     	}
     	
     	public TimerTask schedule(Runnable runnable, long delay)
     	{
     		assertLocked();
     		
     		TimerTask timer = new TimerTask(runnable, System.currentTimeMillis() + delay);
     		_timers.addTimer(timer);
     		
     		if (Log.isDebugEnabled())
     			Log.debug("scheduled timer {} for call session: {}", timer, _id);
     		
     		return timer;
     	}
     	
     	public void cancel(TimerTask timer) 
 		{
     		assertLocked();
     		
     		if (Log.isDebugEnabled())
     			Log.debug("canceled timer {} for call session: {}", timer, _id);
     		
     		if (timer != null)
     		{
     			timer.cancel();
     			_timers.remove(timer);
     		}
 		}
 
     	public void addServerTransaction(ServerTransaction transaction)
     	{
     		_serverTransactions.add(transaction);
     	}
     	
     	public ServerTransaction getServerTransaction(String id)
     	{
     		for (int i = 0; i < _serverTransactions.size(); i++)
     		{
     			ServerTransaction transaction = _serverTransactions.get(i);
     			if (transaction.getKey().equals(id))
     				return transaction;
     		}
     		return null;
     	}
     	
     	public void removeServerTransaction(ServerTransaction transaction)
     	{
     		_serverTransactions.remove(transaction);
     	}
     	
     	public void addClientTransaction(ClientTransaction transaction)
     	{
     		_clientTransactions.add(transaction);
     	}
     	
     	public ClientTransaction getClientTransaction(String id)
     	{
     		for (int i = 0; i <  _clientTransactions.size(); i++)
     		{
     			ClientTransaction transaction = _clientTransactions.get(i);
     			if (transaction.getKey().equals(id))
     				return transaction;
     		}
     		return null;
     	}
     	
     	public void removeClientTransaction(ClientTransaction transaction)
     	{
     		_clientTransactions.remove(transaction);
     	}
     	
     	public List<ClientTransaction> getClientTransactions(SipSession session) 
     	{
 			List<ClientTransaction> list = new ArrayList<ClientTransaction>(_clientTransactions.size());
 			for (int i = 0; i < _clientTransactions.size(); i++)
 			{
 				ClientTransaction transaction = _clientTransactions.get(i);
 				if (transaction.getRequest().session().equals(session))
 					list.add(transaction);
 			}
 			return list;
 		}
 
 		public List<ServerTransaction> getServerTransactions(SipSession session) 
 		{
 			List<ServerTransaction> list = new ArrayList<ServerTransaction>(_serverTransactions.size());
 			for (int i = 0; i < _serverTransactions.size(); i++)
 			{
 				ServerTransaction transaction = _serverTransactions.get(i);
 				if (transaction.getRequest().session().equals(session))
 					list.add(transaction);
 			}
 			return list;			
 		}
 
 		public boolean hasActiveTransactions(SipSession session) 
 		{
 			for (int i = 0; i < _clientTransactions.size(); i++)
 			{
 				ClientTransaction transaction = _clientTransactions.get(i);
 				if (transaction.getState() < Transaction.STATE_COMPLETED 
 						&& transaction.getRequest().session().equals(session))
 					return true;
 			}
 			for (int i = 0; i < _serverTransactions.size(); i++)
 			{
 				ServerTransaction transaction = _serverTransactions.get(i);
 				if (transaction.getState() < Transaction.STATE_COMPLETED
 						&& transaction.getRequest().session().equals(session))
 					return true;
 			}
 			return false;
 		}
 		
 		public AppSession createAppSession(SipAppContext context, String id) 
 		{
 			AppSession appSession = newAppSession(this, id);
 			appSession.setContext(context);
 			
 			_appSessions.add(appSession);
 			return appSession;
 		}
 		
 		public AppSession getAppSession(String id)
 		{
 			for (int i = 0; i < _appSessions.size(); i++)
 			{
 				AppSession appSession = _appSessions.get(i);
 				if (appSession.getAppId().equals(id))
 					return appSession;
 			}
 			return null;
 		}
     	
 		public void removeSession(AppSession appSession) 
 		{
 			_appSessions.remove(appSession);
 		}
 		
 		public Session findSession(SipRequest request) 
 		{
 			String appSessionId = request.getParameter(ID.APP_SESSION_ID_PARAMETER);
 			
 			if (appSessionId != null)
 			{
 				AppSession appSession = getAppSession(appSessionId);
 				return appSession == null ? null : appSession.getSession(request);
 			}
 			else
 			{
 				for (int i = 0; i < _appSessions.size(); i++)
 				{
 					AppSession appSession = _appSessions.get(i);
 					Session session = appSession.getSession(request);
 					if (session != null)
 						return session;
 				}
 			}
 			if (Log.isDebugEnabled())
 				Log.debug("could not find session for request {}", request.getRequestLine());
 			
 			return null;
 		}
 
 		public Session findSession(SipResponse response) 
 		{
 			for (int i = 0; i < _appSessions.size(); i++)
 			{
 				AppSession appSession = _appSessions.get(i);
 				Session session = appSession.getSession(response);
 				if (session != null)
 					return session;
 			}
 			
 			if (Log.isDebugEnabled())
 				Log.debug("could not find session for response {}", response.getRequestLine());
 			
 			return null;
 		}
 		
 		// ==================
 		
 		protected AppSession newAppSession(CallSession callSession, String id)
 		{
 			return new AppSession(callSession, id);
 		}
 		
 		protected boolean isDone()
 		{
 			// No check is done on transaction as 
 			//  - if tx is completed a timer exists
 			//  - else as there is no more timers, no messages are expected to be received and as 
 			//    there is no more sessions, no message could be sent.
 			return (_timers.isEmpty()) && (_appSessions.isEmpty());
 		}
 		
 		protected long nextExecutionTime()
 		{
 			TimerTask timer = _timers.peek();
 			return timer != null ? timer.getExecutionTime() : -1;
 		}
 		
 		protected void runTimers()
 		{
 			long now = System.currentTimeMillis();
 			TimerTask timer = null;
 			
 			while ((timer = _timers.getExpired(now)) != null)
 			{
 				if (!timer.isCancelled())
 				{
 					if (Log.isDebugEnabled())
 						Log.debug("running timer {} for call session {}", timer, _id);
 					try
 					{
 						timer.getRunnable().run();
 					}
 					catch(Throwable t)
 					{
 						Log.warn(t);
 					}
 				}
 			}
 		}
 		
 		protected void invalidateSessionsIfReady()
 		{
 			for (int i = _appSessions.size(); i-->0;)
 			{
 				_appSessions.get(i).invalidateIfReady();
 			}
 		}
 		
 		protected void save(FileOutputStream fos) throws IOException
 		{
 		}
 		
     	private void assertLocked()
     	{
     		if (!_lock.isHeldByCurrentThread())
     			throw new IllegalStateException("CallSession " + _id + " is not locked by thread " + Thread.currentThread());
     	}
     	
     	protected ReentrantLock getLock()
     	{
     		return _lock;
     	}
 
     	@SuppressWarnings("unchecked")
 		public String toString()
         {
         	StringBuffer sb = new StringBuffer();
         	sb.append(_id 
         		+ "[stxs= " + new ArrayList(_serverTransactions)
         		+ ", ctxs=" + new ArrayList(_clientTransactions) 
         		+ ", timers=" + new ArrayList(_timers) 
         		+ ", sessions=" + new ArrayList(_appSessions) + "]");
         	return sb.toString();
         }
     }
     
     /*
     public void setCallLog(CallLog callLog)
     {
     	try
     	{
     		if (_callLog != null)
     			_callLog.stop();
     	}
     	catch (Exception e)
     	{
     		Log.warn(e);
     	}
     	if (getServer() != null)
     		getServer().getContainer().update(this, _callLog, callLog, "calllog", true);
     	
     	_callLog = callLog;
     	
     	try
     	{
     		if (isStarted() && (_callLog != null))
     			_callLog.start();
     	}
     	catch (Exception e)
     	{
     		throw new RuntimeException(e);
     	}
     }
     	public void save(FileOutputStream fos)  throws IOException 
         {
         	System.out.println("saving " + getId());
 
     		DataOutputStream out = new DataOutputStream(fos);
     		out.writeUTF(_id);
     		
     		int nbAppSessions = LazyList.size(_appSessions);
     		out.writeInt(nbAppSessions);
         	System.out.println("appsessions " + nbAppSessions);
 
     		for (int i = 0; i < nbAppSessions; i++)
     		{
     			((AppSession) LazyList.get(_appSessions, i)).save(out);
     		}
     		out.close();
         }
     }*/
 }
