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
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Locale;
 
 import javax.servlet.ServletOutputStream;
 import javax.servlet.ServletResponse;
 import javax.servlet.sip.Address;
 import javax.servlet.sip.Proxy;
 import javax.servlet.sip.ProxyBranch;
 import javax.servlet.sip.Rel100Exception;
 import javax.servlet.sip.SipServletRequest;
 import javax.servlet.sip.SipServletResponse;
 
 import org.cipango.SessionManager.SessionTransaction;
 import org.cipango.security.Authenticate;
 import org.cipango.sip.ServerTransaction;
 import org.mortbay.log.Log;
 
 public class SipResponse extends SipMessage implements SipServletResponse
 {
     private SipRequest _request;
     private int _status;
     private String _reason;
     private SipProxy _proxy;
     private ProxyBranch _proxyBranch;
     private boolean _branchResponse;
     
     /** 
      * Need to send multiple 200 to a request. 
      * See {@link B2bHelper#createResponseToOriginalRequest(javax.servlet.sip.SipSession, int, String)} 
      */
     private boolean _sendOutsideTx = false;
     
 	public SipResponse() { }
 	
 	public SipResponse(SipRequest request, int status, String reason) 
     {
 		_request = request;
 		if (status >= SC_OK) 
             _request.setCommitted(true);
 		setStatus(status, reason);
 		
 		setCallSession(request.getCallSession());
 		setTransaction(request.getTransaction());
 		setSession(request.session());
 		
 		SipFields reqFields = request.getFields();
 		_fields.copy(reqFields, SipHeaders.VIA_BUFFER);
 		_fields.copy(reqFields, SipHeaders.FROM_BUFFER);
 		_fields.copy(reqFields, SipHeaders.TO_BUFFER);
 		_fields.copy(reqFields, SipHeaders.CALL_ID_BUFFER);
 		_fields.copy(reqFields, SipHeaders.CSEQ_BUFFER);
 		if (status < 300)
 			_fields.copy(reqFields, SipHeaders.RECORD_ROUTE_BUFFER);
         
         // TODO Server
 	}
 	
 	/**
 	 * @see SipServletResponse#createAck()
 	 */
 	public SipServletRequest createAck() 
     {
 		if (!SipMethods.INVITE.equalsIgnoreCase(getMethod())) 
 			throw new IllegalStateException("Not INVITE method");
         
         if (_status > 100 && _status < 200)
         {   // For Sip servlet 1.0 compliance
             try
 			{
 				return createPrack();
 			}
 			catch (Rel100Exception e)
 			{
 				throw new IllegalStateException(e.getMessage(), e);
 			}
         }
         else if (_status >= 200 && _status < 300)
         {
         	setCommitted(true);
             return _session.createRequest(SipMethods.ACK, getCSeq().getNumber());
         }
         else 
             throw new IllegalStateException("non 2xx or 1xx response");
 	}
 	
 	public SipServletRequest createPrack() throws Rel100Exception
 	{
 		if (isCommitted())
 			throw new IllegalStateException("Already committed");
 		
 		if (!SipMethods.INVITE.equalsIgnoreCase(getMethod())) 
 			throw new Rel100Exception(Rel100Exception.NOT_INVITE);
 		
 		if (_status > 100 && _status < 200)
         {
             int rseq = getRSeq();
             if (!isReliable1xx()) 
         		throw new Rel100Exception(Rel100Exception.NOT_100rel);
         	
             SipRequest request = (SipRequest) _session.createRequest(SipMethods.PRACK); // TODO do not use API method
             request.getFields().setString(SipHeaders.RACK_BUFFER, rseq + " " + getCSeq());
             setCommitted(true);
             return request;
         }
 		else 
             throw new Rel100Exception(Rel100Exception.NOT_1XX);
 	}
 
 	
 	/**
 	 * @see SipServletResponse#getOutputStream()
 	 */
 	public ServletOutputStream getOutputStream() 
     {
 		return null;
 	}
 	
 	/**
 	 * @see SipServletResponse#getProxy()
 	 */
 	public Proxy getProxy() 
     {
 		if (_proxy == null) 
             _proxy = getTransaction().getRequest().getProxyImpl();
         
         return _proxy;
 	}
 	
 	/**
 	 * @see SipServletResponse#getReasonPhrase()
 	 */
 	public String getReasonPhrase() 
     {
 		return _reason;
 	}
 	
 	/**
 	 * @see SipServletResponse#getRequest()
 	 */
 	public SipServletRequest getRequest() 
     {
         if (_request == null && getTransaction() != null)
             _request = getTransaction().getRequest();
 		return _request;
 	}
 
 	/**
 	 * @see SipServletResponse#getStatus()
 	 */
 	public int getStatus() 
     {
 		return _status;
 	}
 	
 	/**
 	 * @see SipServletResponse#getWriter()
 	 */
 	public PrintWriter getWriter() 
     {
 		return null;
 	}
 	
 	/**
 	 * @see SipServletResponse#send()
 	 */
 	public void send() throws IOException 
     {
         send(false);
     }
 	
 	/**
 	 * @see SipServletResponse#sendReliably()
 	 */
 	public void sendReliably() throws Rel100Exception
     {
 	    SipRequest request = (SipRequest) getRequest();
         if (!request.isInvite())
             throw new Rel100Exception(Rel100Exception.NOT_INVITE);
         if (_status < 101 || _status > 199)
             throw new Rel100Exception(Rel100Exception.NOT_1XX);
     
         Iterator it = _request.getHeaders(SipHeaders.SUPPORTED);
         boolean supports100rel = false;
         
         while (it.hasNext() && !supports100rel)
         {
             String s = (String) it.next();
             if (s.equals(SipParams.REL_100))
                 supports100rel = true;
         }
         
         if (!supports100rel)
         {
             it = _request.getHeaders(SipHeaders.REQUIRE);
             while (it.hasNext() && !supports100rel)
             {
                 String s = (String) it.next();
                 if (s.equals(SipParams.REL_100))
                     supports100rel = true;
             }
         }
         
         if (!supports100rel)
             throw new Rel100Exception(Rel100Exception.NO_REQ_SUPPORT);
         
         try 
         {
             send(true);
         } 
         catch (IOException e)
         {
             Log.debug(e);
         }
 	}
 	
 	/**
 	 * @see ServletResponse#setCharacterEncoding(String)
 	 */
 	public void setCharacterEncoding(String encoding)
 	{
 		/*
 		 * Because of a change in Servlet spec 2.4 the setCharacterEncoding() 
 		 * does NOT throw the java.io.UnsupportedEncodingException as derived 
 		 * from SipServletMessage.setCharacterEncoding(String) but inherits 
 		 * a more generic setCharacterEncoding() method from the 
 		 * javax.servlet.ServletResponse. 
 		 */
 		_characterEncoding = encoding;
 	}
 	
 	/**
 	 * @see SipServletResponse#setStatus(int)
 	 */
 	public void setStatus(int status) 
 	{
 		_status = status;
 	}
 	
 	/**
 	 * @see SipServletResponse#setStatus(int, java.lang.String)
 	 */
 	public void setStatus(int status, String reason) 
 	{
 		if (status < 100 || status >= 700) 
     		throw new IllegalArgumentException("Invalid status-code: " + status);
     	    	
 		_status = status;
 		_reason = reason;
 	}
 	
 	/**
 	 * @see javax.servlet.ServletResponse#flushBuffer()
 	 */
 	public void flushBuffer() 
 	{
 	}
 	
 	/**
 	 * @see javax.servlet.ServletResponse#getBufferSize()
 	 */
 	public int getBufferSize() 
 	{
 		return 0;
 	}
 	
 	/**
 	 * @see javax.servlet.ServletResponse#getLocale()
 	 */
 	public Locale getLocale() 
 	{
 		return getContentLanguage();
 	}
 	
 	/**
 	 * @see javax.servlet.ServletResponse#reset()
 	 */
 	public void reset() { }
 	
 	/**
 	 * @see javax.servlet.ServletResponse#resetBuffer()
 	 */
 	public void resetBuffer() { }
 	
 	/**
 	 * @see javax.servlet.ServletResponse#setBufferSize(int)
 	 */
 	public void setBufferSize(int size) { }
 	
 	/**
 	 * @see javax.servlet.ServletResponse#setLocale(Locale)
 	 */
 	public void setLocale(Locale locale) 
 	{
 		setContentLanguage(locale);
 	}
 	
 	//
 	
 	public String getMethod() 
 	{
 		return getCSeq().getMethod();
 	}
 	
 	public boolean isRequest() 
 	{
 		return false;
 	}
 	
 	public boolean needsContact() 
 	{
 		return (isInvite() || isSubscribe() || isNotify() || isRefer() || isUpdate())
 				&& (getStatus() < 300);
 	}
 	
 	protected boolean canSetContact() 
 	{
         return isRegister() 
         		||(getStatus() >= 300 && getStatus() < 400) 
         		|| getStatus() == 485
         		|| (getStatus() == 200 && isOptions());
     }
 	
 	/*
 	public Transaction getTransaction() {
 		return request.getTransaction();
 	}
 	*/
     
     public void setRSeq(int rseq)
     {
         getFields().setString(SipHeaders.RSEQ_BUFFER, Integer.toString(rseq));
     }
     
     public int getRSeq()
     {
         return (int) getFields().getLong(SipHeaders.RSEQ_BUFFER);
     }
     
     private void send(boolean reliable) throws IOException
     {
         if (isCommitted())
             throw new IllegalStateException("Response is commited");
         
         SessionManager csm = getCallSession().getServer().getSessionManager();
         
         SessionTransaction workUnit = csm.begin(getCallSession());
         
         try 
         {
         	_session.sendResponse(this, (ServerTransaction) getTransaction(), reliable);
             setCommitted(true);
         } 
         catch (Exception e) 
         {
             Log.warn(e);
             if (e instanceof IOException)
                 throw (IOException) e; 
             throw new IllegalStateException(e.getMessage());
         }
         finally
         {
         	workUnit.done();
         }
     }
 	
 	public Via removeTopVia() 
 	{
 		Via via = _fields.getVia();
 		_fields.removeFirst(SipHeaders.VIA_BUFFER);
 		return via;
 	}
 	
 	public void setContact(Address contact) 
 	{
         _fields.setAddress(SipHeaders.CONTACT, (NameAddr) contact);
     }
 	
 	public boolean isReliable1xx()
 	{
 		if (_status > 100 && _status < 200)
 		{
 			if (getRSeq() == -1)
 				return false;
 			Iterator<String> it = _fields.getValues(SipHeaders.REQUIRE_BUFFER);
 			while (it.hasNext()) {
 				String val = it.next();
 				if (SipParams.REL_100.equalsIgnoreCase(val)) {
 					return true;
 				}
 			}
             return false;
 		}
 		return false;
 	}
 	
 	public Iterator<String> getChallengeRealms()
 	{
 		List<String> list = new ArrayList<String>();
 		ListIterator<String > it = _fields.getValues(SipHeaders.WWW_AUTHENTICATE_BUFFER);
 		while (it.hasNext())
 		{
 			Authenticate authenticate = new Authenticate(it.next());
 			list.add(authenticate.getParameter(Authenticate.REALM_BUFFER));
 		}
 		it = _fields.getValues(SipHeaders.PROXY_AUTHENTICATE_BUFFER);
 		while (it.hasNext())
 		{
 			Authenticate authenticate = new Authenticate(it.next());
 			list.add(authenticate.getParameter(Authenticate.REALM_BUFFER));
 		}
 		return list.iterator();
 	}
 
 	public ProxyBranch getProxyBranch()
 	{
 		return _proxyBranch;
 	}
 	
 	public void setProxyBranch(ProxyBranch branch)
 	{
 		_proxyBranch = branch;
 	}
 
 	public boolean isBranchResponse()
 	{
 		return _branchResponse;
 	}
 	
 	public void setBranchResponse(boolean branch)
 	{
 		_branchResponse = branch;
 	}
 	
 	public void setSendOutsideTx(boolean outside)
 	{
 		_sendOutsideTx = outside;
 	}
 	
 	public boolean isSendOutsideTx()
 	{
 		return _sendOutsideTx;
 	}
 	
 	public String getRequestLine()
 	{
 		return _status + " " + _status;
 	}
 
 	public Object clone()
 	{
 		SipResponse clone = (SipResponse) super.clone();
 		clone._request = null;
 		clone._proxy = null;
 		return clone;
 	}
 }
