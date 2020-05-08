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
 
 package org.cipango;
 
 import java.io.Serializable;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.NoSuchElementException;
 
 import javax.servlet.sip.Address;
 import javax.servlet.sip.Proxy;
 import javax.servlet.sip.ProxyBranch;
 import javax.servlet.sip.ServletParseException;
 import javax.servlet.sip.SipServletRequest;
 import javax.servlet.sip.SipServletResponse;
 import javax.servlet.sip.SipURI;
 import javax.servlet.sip.TooManyHopsException;
 import javax.servlet.sip.URI;
 import javax.servlet.sip.ar.SipApplicationRoutingDirective;
 
 import org.cipango.Call.TimerTask;
 import org.cipango.servlet.AppSession;
 import org.cipango.sip.ClientTransaction;
 import org.cipango.sip.ClientTransactionListener;
 import org.cipango.sip.ServerTransaction;
 import org.cipango.sip.ServerTransactionListener;
 import org.cipango.sip.SipConnector;
 import org.cipango.sip.SipEndpoint;
 import org.cipango.util.ID;
 import org.mortbay.log.Log;
 import org.mortbay.util.LazyList;
 
 public class ProxyImpl implements Proxy, ServerTransactionListener, Serializable
 {    
 	private static final long serialVersionUID = 1L;
 
 	public static final int DEFAULT_TIMEOUT = 15;
 	public static final int DEFAULT_TIMER_C = 180;
 	
     public static int __maxForwards = 70;
     public static int __timerC = DEFAULT_TIMER_C;
     
     private boolean _started;
     
     private boolean _parallel = true;
     private boolean _recurse = true;
     private boolean _supervised = true;
     private boolean _noCancel = false;
     
     private boolean _initial;
     
     private int _proxyTimeout = DEFAULT_TIMEOUT;
     
     private SipURI _rrUri;
     private SipURI _pathUri;
     
     private ServerTransaction _tx;
     private SipResponse _best;
     
     private int _actives;
     
     private Object _branches;
     private Object _targets;
 
 	public ProxyImpl(ServerTransaction tx) throws TooManyHopsException
     {
 		_tx = tx;
         _tx.setListener(this);
         
         _initial = _tx.getRequest().isInitial();
         
         validate(_tx.getRequest());
         
         if (Log.isDebugEnabled()) 
         	Log.debug("Created proxy for tx {}", tx, null);
     }
 	
 	/**
 	 * @see Proxy#cancel()
 	 */
 	public void cancel() 
 	{
 		cancel(null, null, null);
     }
 	
 	/**
 	 * @see Proxy#cancel(String[], int[], String[])
 	 */
 	public void cancel(String[] protocol, int[] reasonCode, String[] reasonText)
 	{                
 		if (_tx.isCompleted())
 			throw new IllegalStateException("Transaction has completed");
 		
 		doCancel(protocol, reasonCode, reasonText);
 	}
 	
 	protected void doCancel(String[] protocol, int[] reasonCode, String[] reasonText)
 	{
         for (int i = LazyList.size(_branches); i-->0; )
         {
         	Branch branch = (Branch) LazyList.get(_branches, i);
         	branch.cancel(protocol, reasonCode, reasonText);
         }
 	}
 	
 	/**
 	 * @see Proxy#createProxyBranches(List)
 	 */
 	public List<ProxyBranch> createProxyBranches(List<? extends URI> list)
 	{
 		List<ProxyBranch> branches = new ArrayList<ProxyBranch>();
 		
 		for (URI uri : list)
 		{
 			Branch branch = addBranch(uri);
 			if (branch != null)
 				branches.add(branch);
 		}
 		return branches;
 	}
 	
 	/**
 	 * @see Proxy#getAddToPath()
 	 */
 	public boolean getAddToPath()
 	{
 		return _pathUri != null;
 	}
 
 	/**
 	 * @see Proxy#getNoCancel
 	 */
 	public boolean getNoCancel()
 	{
 		return _noCancel;
 	}
 	
 	/**
 	 * @see Proxy#getOriginalRequest()
 	 */
 	public SipServletRequest getOriginalRequest()
     {
 		return _tx.getRequest();
 	}
 	
 	/**
 	 * @see Proxy#getParallel()
 	 */
 	public boolean getParallel()
     {
 		return _parallel;
 	}
 	
 	/**
 	 * @see Proxy#getPathURI()
 	 */
 	public SipURI getPathURI()
 	{
 		if (_pathUri == null)
 			throw new IllegalStateException("addToPath is not enabled");
 		return _pathUri;
 	}
 
 	/**
 	 * @see Proxy#getProxyBranch(URI)
 	 */
 	public ProxyBranch getProxyBranch(URI uri)
 	{
 		Iterator<Branch> it = new BranchIterator(_branches);
 		while (it.hasNext())
 		{
 			Branch branch = it.next();
 			if (branch.getUri().equals(uri))
 				return branch;
 		}
 		return null;
 	}
 	
 	/**
 	 * @see Proxy#getProxyBranches()
 	 */
 	@SuppressWarnings("unchecked")
 	public List<ProxyBranch> getProxyBranches()
 	{
 		return LazyList.getList(_branches);
 	}
 	
 	/**
 	 * @see Proxy#getProxyTimeout()
 	 */
 	public int getProxyTimeout()
 	{
 		return _proxyTimeout;
 	}
 	
 	/**
 	 * @see Proxy#getRecordRoute()
 	 */
 	public boolean getRecordRoute()
     {
 		return _rrUri != null;
 	}
 	
 	/**
 	 * @see Proxy#getRecordRouteURI()
 	 */
 	public SipURI getRecordRouteURI()
     {
 		if (_rrUri == null) 
 			throw new IllegalStateException("Record-Routing is not enabled");
         
 		return _rrUri;
 	}
 	
 	/**
 	 * @see Proxy#getRecurse()
 	 */
 	public boolean getRecurse()
     {
 		return _recurse;
 	}
 	
 	/**
 	 * @see Proxy#getSequentialSearchTimeout()
 	 * @deprecated
 	 */
 	public int getSequentialSearchTimeout() 
     {
 		return getProxyTimeout();
 	}
 	
 	/**
 	 * @see Proxy#getStateful()
 	 * @deprecated
 	 */
 	public boolean getStateful() 
     {
 		return true;
 	}
 	
 	/**
 	 * @see Proxy#getSupervised()
 	 */
 	public boolean getSupervised() 
     {
 		return _supervised;
 	}
 	
 	/**
 	 * @see Proxy#proxyTo(List)
 	 */
 	public void proxyTo(List<? extends URI> targets) 
     {
 		for (URI uri : targets) 
 		{
 			addBranch(uri);
 		}
 		startProxy();
 	}
 	
 	/**
 	 * @see Proxy#proxyTo(javax.servlet.sip.URI)
 	 */
 	public void proxyTo(URI uri) 
     {
 		addBranch(uri);
 		startProxy();
 	}
 	
 	/**
 	 * @see Proxy#setAddToPath(boolean)
 	 */
 	public void setAddToPath(boolean addToPath)
 	{
 		if (!addToPath)
 			_pathUri = null;
 		else if (_pathUri == null)
 			_pathUri = newProxyURI(false);
 	}
 	
 	/**
 	 * @see Proxy#setNoCancel(boolean)
 	 */
 	public void setNoCancel(boolean b)
 	{
 		_noCancel = b;
 	}
 
 	/**
 	 * @see Proxy#setOutboundInterface(InetAddress)
 	 */
 	public void setOutboundInterface(InetAddress address)
 	{
 		if (address == null)
 			throw new NullPointerException("Null address");
 		
 	}
 
 	/**
 	 * @see Proxy#setOutboundInterface(InetSocketAddress)
 	 */
 	public void setOutboundInterface(InetSocketAddress address)
 	{
 		if (address == null)
 			throw new NullPointerException("Null address");
 	}
 
 	/**
 	 * @see Proxy#setParallel(boolean)
 	 */
 	public void setParallel(boolean parallel) 
     {
 		_parallel = parallel;
 	}
 	
 	/**
 	 * @see Proxy#setProxyTimeout(int)
 	 */
 	public void setProxyTimeout(int seconds)
 	{
 		if (seconds <= 0)
 			throw new IllegalArgumentException("Proxy timeout too low: " + seconds);
 		_proxyTimeout = seconds;
 	}
 
 	/**
 	 * @see Proxy#setRecordRoute(boolean)
 	 */
 	public void setRecordRoute(boolean recordRoute) 
     {
 		if (_started)
 			throw new IllegalStateException("Proxy has already been started");
 		
 		if (!recordRoute)
 			_rrUri = null;
 		else if (_rrUri == null)
 			_rrUri = newProxyURI(true);
 	}
 	
 	/**
 	 * @see Proxy#setRecurse(boolean)
 	 */
 	public void setRecurse(boolean recurse) 
     {
 		_recurse = recurse;
 	}
 	
 	/**
 	 * @see Proxy#setSequentialSearchTimeout(int)
 	 * @deprecated
 	 */
 	public void setSequentialSearchTimeout(int seconds) 
     {
         setProxyTimeout(seconds);
 	}
 	
 	/**
 	 * @see Proxy#setStateful(boolean)
 	 * @deprecated
 	 */
 	public void setStateful(boolean b) { }
 	
 	/**
 	 * @see Proxy#setSupervised(boolean)
 	 */
 	public void setSupervised(boolean supervised) 
     {
 		_supervised = supervised;
 	}
 	
 	/**
 	 * @see Proxy#startProxy()
 	 */
 	public void startProxy() 
     {
 		_started = true;
 		
 		if (_tx.isCompleted())
         	throw new IllegalStateException("Transaction has completed");
 		
 		if (!_parallel && _actives > 0)
 			return;
 		
 		while (LazyList.size(_targets) > 0) 
         {
 			Branch branch = (Branch) LazyList.get(_targets, 0);
 			_targets = LazyList.remove(_targets, 0);
             
             if (Log.isDebugEnabled()) 
             	Log.debug("Proxying to {} ", branch.getUri(), null);
             
             branch.start();
 
 			if (!_parallel) break;
 		}
 	}
 	
 	// ----------------------------------------------------------------
 	
 	private SipURI newProxyURI(boolean applicationId)
 	{
 		SipConnector connector = null;
 		
 		SipEndpoint ep = _tx.getRequest().getEndpoint();
         if (ep != null)
         {
 	       connector = ep.getConnector();
         }
         else
         {
         	int transport = _tx.getRequest().transport();
         	InetAddress addr = _tx.getRequest().remoteAddress();
         	
         	connector = _tx.getCall().getServer().getTransportManager().findConnector(transport, addr);
         }
         
 		SipURI rrUri = (SipURI) connector.getSipUri().clone();
 		rrUri.setParameter("lr", "");
 		
 		if (applicationId)
 		{
 			AppSession appSession = _tx.getRequest().appSession();
 			rrUri.setUser("aid!" + appSession.getAid());
 		}
 
 		return rrUri;
 	}
 	
 	private boolean isInTargetSet(URI uri)
 	{
 		if (_branches == null)
 			return false;
 		
 		Iterator<Branch> it = new BranchIterator(_branches);
 		while (it.hasNext())
 		{
 			if (it.next().getUri().equals(uri))
 				return true;
 		}
 		return false;
 	}
 	
 	protected Branch addTarget(URI uri)
 	{
 		URI target = uri.clone();
 		if (target.isSipURI())
 		{
 			SipURI sipUri = (SipURI) target;
 			sipUri.removeParameter("method");
 			Iterator<String> it = sipUri.getHeaderNames();
 			while (it.hasNext())
 			{
 				it.remove();
 			}
 		}
 		if (isInTargetSet(target))
 		{
 			if (Log.isDebugEnabled())
 				Log.debug("target {} is already in target set", target);
 			return null;
 		}
 		else
 		{
 			if (Log.isDebugEnabled())
 				Log.debug("adding target {} to target set", target);
 			
 			Branch branch = new Branch(target);
 			_targets = LazyList.add(_targets, branch);
 			return branch;
 		}
 	}
 	
 	protected Branch addBranch(URI uri)
 	{
 		if (_tx.isCompleted())
 			throw new IllegalStateException("transaction completed");
 		
 		if (!uri.isSipURI() && getOriginalRequest().getHeader(SipHeaders.ROUTE) == null)
 			throw new IllegalArgumentException("Cannot route " + uri);
 		
 		Branch branch = addTarget(uri);
 		if (branch != null)
 		{
 			branch.setRecurse(getRecurse());
 			branch.setRecordRoute(getRecordRoute());
 			branch.setAddToPath(getAddToPath());
 			branch.setProxyBranchTimeout(getProxyTimeout());
 			
 			_branches = LazyList.add(_branches, branch);
 		}
 		return branch;
 	}
 	
     private void validate(SipRequest request) throws TooManyHopsException
     {
         int maxForwards = request.getMaxForwards();
         if (maxForwards == 0) 
             throw new TooManyHopsException();
         else if (maxForwards == -1) 
             request.setMaxForwards(70);
     } 
    
     public void handleCancel(ServerTransaction tx, SipRequest cancel)
     {
         cancel.setSession(_tx.getRequest().session());
         cancel();
         try 
         {
             cancel.session().invokeServlet(cancel);
         } 
         catch (Exception e)
         {
             Log.debug(e);
         }
     }
 	
     /**
      * 
      * @return <code>true</code> if there is remains active branches.
      */
 	private boolean tryFinal()
     {
 	    if (Log.isDebugEnabled()) 
 	    	Log.debug("tryFinal, branches: {}, untried: {}", _branches, _targets);
 
 	    if (_actives > 0)
 	    	return true;
         
     	if (_best == null)
     	{
 			_best = (SipResponse) getOriginalRequest().createResponse(SipServletResponse.SC_REQUEST_TIMEOUT);
 			_best.setToTag(ID.newTag());
     	}
     	
         if (Log.isDebugEnabled()) 
         	Log.debug("final response is {}", _best, null);
         
         _best.setBranchResponse(false);
         invokeServlet(_best);
        
         if (_actives > 0)
         {
             if (Log.isDebugEnabled()) 
             	Log.debug("new branch(es) created in callback {}", _branches, null);
             return true;
         }
 		forward(_best);
 		return false;
 	}
 	
     private void invokeServlet(SipResponse response)
     {
         if (_supervised)    
             response.session().invokeServlet(response);
     }
     
 	private void forward(SipResponse response)
     {	
         if (_tx.getState() >= ServerTransaction.STATE_COMPLETED) 
         {
             if (Log.isDebugEnabled()) 
             	Log.debug("Server transaction completed {}", _tx, null);
             return;
         }
         if (_tx.getRequest().getTo().getParameter("tag") == null)
         {
             if (response.getStatus() < 300 && (response.isInvite() || response.isSubscribe()))
             {
                 response.session().registerProxy(response);
             }
         }
         _tx.send(response);
 		response.setCommitted(true); 
 	}    
 	
 	class TimeoutC implements Runnable, Serializable
 	{
 		private static final long serialVersionUID = 1L;
 		
 		private Branch _branch;
 		
 		public TimeoutC(Branch branch)
 		{
 			_branch = branch;
 		}
 		
 		public void run()
 		{
 			_branch.timeoutTimerC();
 		}
 	}
 	
     class Branch implements ProxyBranch, ClientTransactionListener, Serializable
     {	
 		private static final long serialVersionUID = 1L;
 	
 		private URI _uri;
     	private SipRequest _request;
     	private SipResponse _response;
     	
         private ClientTransaction _ctx;
         private boolean _provisional = false;
         private TimerTask _timerC;
 
         private boolean _branchRecurse;
         
         private SipURI _branchPathUri;
         private SipURI _branchRRUri;
         
         private int _branchTimeout;
         
         private Object _recursedBranches;
         
         public Branch(URI uri)
         {
         	_uri = uri;
         	_request = (SipRequest) ((SipRequest) getOriginalRequest()).clone();
         	_request.setProxyImpl(((SipRequest) getOriginalRequest()).getProxyImpl());
         	if (getOriginalRequest().isInitial())
         		_request.setRoutingDirective(SipApplicationRoutingDirective.CONTINUE, getOriginalRequest());
         }
         
         /**
          * @see ProxyBranch#cancel()
          */
         public void cancel()
         {
         	cancel(null, null, null);
         }
         
         /**
          * @see ProxyBranch#cancel(String[], int[], String[])
          */
         public void cancel(String[] protocol, int[] reasonCode, String[] reasonText) 
         {
         	if (!_ctx.isCompleted())
         	{
 	        	stopTimerC();
 	            
 	            SipRequest cancel = (SipRequest) _ctx.getRequest().createCancel();
 	        	if (protocol != null)
 	        	{
 	        		for (int i = 0; i < protocol.length; i++)
 	        		{
 	        			String reason = protocol[i] 
 	        			                + ";cause=" + reasonCode[i] 
 	        			                + ";reason=\"" + SipGrammar.escapeQuoted(reasonText[i]) + "\"";
 	        			cancel.addHeader(SipHeaders.REASON, reason);
 	        		}
 	        	}
 	        	_ctx.cancel(cancel);
         	}
         	
         	for (int i = LazyList.size(_recursedBranches); i-->0;)
         	{
         		((Branch) LazyList.get(_recursedBranches, i)).cancel(protocol, reasonCode, reasonText);
         	}
 		}
         
         /**
          * @see ProxyBranch#getAddToPath()
          */
         public boolean getAddToPath() 
         {
 			return  _branchPathUri != null;
 		}
         
         /**
          * @see ProxyBranch#getPathURI()
          */
         public SipURI getPathURI() 
 		{
 			return _branchPathUri;
 		}
         
         /**
          * @see ProxyBranch#getProxy()
          */
         public Proxy getProxy() 
 		{
 			return ProxyImpl.this;
 		}
         
         /**
          * @see ProxyBranch#getProxyBranchTimeout()
          */
         public int getProxyBranchTimeout() 
 		{
 			return _branchTimeout;
 		}
         
         /**
          * @see ProxyBranch#getRecordRoute()
          */
         public boolean getRecordRoute() 
 		{
 			return _branchRRUri != null;
 		}
 
         /**
          * @see ProxyBranch#getRecordRouteURI()
          */
 		public SipURI getRecordRouteURI() 
 		{
 			if (_branchRRUri == null)
 				throw new IllegalStateException("Record-Routing is not enabled");
 			
 			return _branchRRUri;
 		}
         
 		/**
 		 * @see ProxyBranch#getRecurse()
 		 */
 		public boolean getRecurse() 
 		{
 			return _branchRecurse;
 		}
 		
 		/**
 		 * @see ProxyBranch#getRecursedProxyBranches()
 		 */
 		@SuppressWarnings("unchecked")
 		public List<ProxyBranch> getRecursedProxyBranches() 
 		{
 			return LazyList.getList(_recursedBranches);
 		}
 		
 		/**
 		 * @see ProxyBranch#getRequest()
 		 */
 		public SipServletRequest getRequest() 
 		{
 			return _request;
 		}
 
 		/**
 		 * @see ProxyBranch#getResponse()
 		 */
 		public SipServletResponse getResponse() 
 		{
 			return _response;
 		}
 		
 		/**
 		 * @see ProxyBranch#isStarted()
 		 */
 		public boolean isStarted() 
 		{
 			return _ctx != null;
 		}
 		
 		/**
 		 * @see ProxyBranch#setAddToPath(boolean)
 		 */
 		public void setAddToPath(boolean b) 
 	    {
 			if (!b)
 				_branchPathUri = null;
 			else if (_branchPathUri == null)
 				_branchPathUri = (SipURI) (_pathUri != null ? _pathUri.clone() : newProxyURI(false));
 		}
 		
 		/**
 		 * @see ProxyBranch#setOutboundInterface(InetAddress)
 		 */
 		public void setOutboundInterface(InetAddress address) 
 		{
 			if (!_tx.getRequest().session().isValid())
 				throw new IllegalStateException("Session not valid");
 			if (address == null)
 				throw new NullPointerException("Null address");
 		}
 		
 		/**
 		 * @see ProxyBranch#setOutboundInterface(InetSocketAddress)
 		 */
 		public void setOutboundInterface(InetSocketAddress address) 
 		{
 			if (!_tx.getRequest().session().isValid())
 				throw new IllegalStateException("Session not valid");
 			if (address == null)
 				throw new NullPointerException("Null address");
 		}
 
 		/**
 		 * @see ProxyBranch#setProxyBranchTimeout(int)
 		 */
 		public void setProxyBranchTimeout(int seconds) 
 		{
 			if (seconds <= 0 || seconds > _proxyTimeout)
 				throw new IllegalArgumentException("Invalid branch timeout: " + seconds);
 			_branchTimeout = seconds;
 		}
 		
 		/**
 		 * @see ProxyBranch#setRecordRoute(boolean)
 		 */
 		public void setRecordRoute(boolean b) 
 		{
 			if (isStarted())
 				throw new IllegalStateException("Proxy branch is started");
 			
 			if (!b)
 				_branchRRUri = null;
 			else if (_branchRRUri == null) 
 				_branchRRUri = (SipURI) (_rrUri != null ? _rrUri.clone() : newProxyURI(true));
 		}
 		
 		/**
 		 * @see ProxyBranch#setRecurse(boolean)
 		 */
 		public void setRecurse(boolean recurse) 
 		{
 			_branchRecurse = recurse;	
 		}
 
         public URI getUri()
         {
         	return _uri;
         }
         
 		protected void start()
 		{
             int mf = _request.getMaxForwards();
             if (mf == -1)
                 mf = __maxForwards;
             else
                 mf--;
             
 			_request.setMaxForwards(mf);
 			_request.setRequestURI(_uri);
 			
 			if (_branchRRUri != null) 
 				_request.addRecordRoute(new NameAddr(_branchRRUri));
 			
 			if (_branchPathUri != null && _request.isRegister())
 				_request.addAddressHeader(SipHeaders.PATH, new NameAddr(_branchPathUri), true);
 			
 			if (!getRecordRoute() && _request.isInitial())
 				_request.session().setMayReadyToInvalidate(true);
 				
 			_ctx = _request.getCall().getServer().sendRequest(_request, this);
 			
 			if (_request.isInvite())
 				startTimerC();
 			
 			_actives++;
 		}
         
         public void startTimerC()
         {
         	_timerC = _tx.getCall().schedule(new TimeoutC(this), __timerC * 1000);
         }
         
         public void updateTimerC()
         {
         	if (_timerC == null)
         		return; 
         	
         	_provisional = true;
         
         	_tx.getCall().cancel(_timerC);
         	_timerC = _tx.getCall().schedule(new TimeoutC(this), __timerC * 1000);
         }
         
         public void stopTimerC()
         {
         	_tx.getCall().cancel(_timerC);
         	_timerC = null;
         }
         
         public void timeoutTimerC()
         {
         	_timerC = null;
         	Log.debug("Timer C timed out for branch {}", _ctx.getBranch(), null);
         	if (_provisional)
         	{
         		cancel();
         	}
         	else 
         	{
         		SipResponse timeout = _ctx.create408();
         		handleResponse(timeout);
         	}
         }
 
 		public void handleResponse(SipResponse response) 
 	    {   
 			_response = response;
 			
 			int status = response.getStatus();
 			
 			if (status == 100)
 				return;
 			
 	        if (Log.isDebugEnabled()) 
 	        	Log.debug("Got response {}", response, null);
 	        
 	        /*
 	        if (_tx.getRequest().isInitial() && (_tx.get) )
 	        String tag = response.to().getParameter(SipParams.TAG);
 	        if (tag != null && _tx.getRequest().session().getRemoteTag())
 	        	*/
 	        
 	        response.setSession(_tx.getRequest().session());
 	        // TODO fork
 			
 			response.removeTopVia();
 			response.setProxyBranch(this);
 			
 			if (status < 200)
 	        {
 				if (response.isInvite())
 					updateTimerC();
 	                
 				// TODO session register
 	            invokeServlet(response);
 	            
 				forward(response);
 			} 
 	        else 
 	        {
 	        	_actives--;
 	        	
 	        	stopTimerC();
 	            
 				if ((300 <= status && status < 400) && _branchRecurse) 
 	            {
 					try 
 	                {
 						Iterator it = response.getAddressHeaders(SipHeaders.CONTACT);
 						while (it.hasNext()) 
 	                    {
 							Address contact = (Address) it.next();
 							if (contact.getURI().isSipURI()) 
 							{
 								Branch branch = addTarget(contact.getURI());
 								if (branch != null)
 								{
 									_recursedBranches = LazyList.add(_recursedBranches, branch);
 									branch.setRecurse(_branchRecurse);
 								}
 							}
 						}
 					} 
 	                catch (ServletParseException e)
 	                {
 						Log.ignore(e);
 					}
 	            }
 				
 				if (_best == null || 
 						(_best.getStatus() < 600 && (status < _best.getStatus() || status >= 600))) 
 	            {
 					_best = response;
 				}
 				
 				if (status >= 600) 
 	            {
 					ProxyImpl.this.doCancel(null, null, null);
 				}
 				
 				if (status < 300) 
 	            {
 					// TODO session register
 	                invokeServlet(response);
 					forward(response);
 					
 					ProxyImpl.this.doCancel(null, null, null);
 				}
 	            else 
 	            {
	                startProxy();
 	                if (tryFinal())
 	                {
 	                	response.setBranchResponse(true);
 	                	invokeServlet(response);
 	                }
 	            }
 			}
 		}
     }
 	
 	class BranchIterator implements Iterator<Branch>
 	{
 		private Iterator<Branch> _it;
 		
 		private Object _branches;
 		private Branch _next;
 		private int _index;
 		
 		public BranchIterator(Object branches)
 		{
 			_branches = branches;
 			_index = 0;
 		}
 
 		public boolean hasNext()
 		{
 			if (_next == null)
 			{
 				if (_it != null && _it.hasNext())
 				{
 					_next = _it.next();
 				}
 				else
 				{
 					if (_index < LazyList.size(_branches))
 					{
 						Branch branch = (Branch) LazyList.get(_branches, _index++);
 						_next = branch;
 						if (LazyList.size(branch._recursedBranches) > 0)
 						{
 							_it = new BranchIterator(branch._recursedBranches);
 						}
 					}
 				}
 			}
 			return _next != null;
 		}
 
 		public Branch next() 
 		{
 			if (hasNext())
 			{
 				Branch next = _next;
 				_next = null;
 				return next;
 			}
 			throw new NoSuchElementException();
 		}
 
 		public void remove() 
 		{	
 		}
 	}
 }
