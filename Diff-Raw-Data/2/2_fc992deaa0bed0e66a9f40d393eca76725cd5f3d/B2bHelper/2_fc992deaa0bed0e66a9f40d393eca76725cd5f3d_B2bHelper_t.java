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
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map;
 
 import javax.servlet.sip.Address;
 import javax.servlet.sip.B2buaHelper;
 import javax.servlet.sip.ServletParseException;
 import javax.servlet.sip.SipServletMessage;
 import javax.servlet.sip.SipServletRequest;
 import javax.servlet.sip.SipServletResponse;
 import javax.servlet.sip.SipSession;
 import javax.servlet.sip.SipURI;
 import javax.servlet.sip.TooManyHopsException;
 import javax.servlet.sip.UAMode;
 import javax.servlet.sip.SipSession.State;
 import javax.servlet.sip.ar.SipApplicationRoutingDirective;
 
 import org.cipango.servlet.AppSession;
 import org.cipango.servlet.Session;
 import org.cipango.servlet.SessionIf;
 import org.cipango.sip.ClientTransaction;
 import org.cipango.sip.ServerTransaction;
 import org.cipango.sip.Transaction;
 import org.cipango.util.ContactAddress;
 import org.cipango.util.ID;
 import org.mortbay.io.BufferCache.CachedBuffer;
 
 public class B2bHelper implements B2buaHelper
 {
 	
 	private SipRequest _request;
 	private SipRequest _linkedRequest;
 	
 	public B2bHelper(SipRequest request)
 	{
 		_request = request;
 	}
 
 	public SipServletRequest createCancel(SipSession sipSession)
 	{
 		Session session = ((SessionIf) sipSession).getSession();
 		Iterator<ClientTransaction> it = session.getCall().getClientTransactions(session).iterator();
 		while (it.hasNext())
 		{
 			ClientTransaction tx = (ClientTransaction) it.next();
 			if (tx.getRequest().isInitial())
 			{
 				return tx.getRequest().createCancel();
 			}
 		}
 		return null;
 	}
 	
 	public SipServletRequest createRequest(SipServletRequest origRequest)
 	{
 		try {
 			// FIXME max-forward should be decremented ?, should be linked ?
 			return createRequest(origRequest, false, null);
 		} catch (TooManyHopsException e) {
 			throw new IllegalStateException(e);
 		}
 	}
 
 	public SipServletRequest createRequest(SipServletRequest origRequest, boolean linked,
 			Map<String, List<String>> headerMap) throws IllegalArgumentException, TooManyHopsException
 	{
 		if (origRequest == null)
 			throw new NullPointerException("origRequest is null");
 		
 		if (!origRequest.isInitial())
 			throw new IllegalArgumentException("origRequest is not initial");
 		
 		AppSession appsession = _request.appSession();
         SipRequest request = (SipRequest) ((SipRequest) origRequest).clone();
                                 
         SipFields fields = request.getFields();
         
         fields.remove(SipHeaders.RECORD_ROUTE_BUFFER);
         fields.remove(SipHeaders.VIA);
         
         fields.remove(SipHeaders.FROM_BUFFER);
         fields.remove(SipHeaders.TO_BUFFER);
         
         int mf = request.getMaxForwards();
         if (mf == -1)
             mf = ProxyImpl.__maxForwards;
         else if (mf == 0)
             throw new TooManyHopsException();
         else
             mf--;
 		request.setMaxForwards(mf);
           
         if (!request.isRegister())
             fields.remove(SipHeaders.CONTACT_BUFFER);
         
        Server server = appsession.getContext().getSipServer();
         String callId = server.getIdManager().newCallId(request.getCallId());
         
         fields.setString(SipHeaders.CALL_ID, callId);
         
         request.setInitial(true);
                 
         List<String> contacts = processHeaderMap(headerMap, fields, false);
         
         // From and to may have been set by headerMap
         NameAddr from = (NameAddr) request.from();
         if (from == null)
         {
         	from = (NameAddr) origRequest.getFrom().clone();
         	fields.setAddress(SipHeaders.FROM, from);
         }
         NameAddr to = (NameAddr) request.to();
         if (to == null)
         {
         	to = (NameAddr) origRequest.getTo().clone();
         	fields.setAddress(SipHeaders.TO, to);
         }	
            
         from.setParameter(SipParams.TAG, ID.newTag());
         to.removeParameter(SipParams.TAG);
         
         Session session = appsession.newUacSession(callId, from, to);
         session.setLocalCSeq(request.getCSeq().getNumber() + 1);
         session.setHandler(appsession.getContext().getSipServletHandler().getDefaultServlet());
         
         request.setSession(session);
         
         if (request.needsContact())
         {
         	NameAddr contact = (NameAddr) session.getContact().clone();
         	if (contacts != null && contacts.size() > 1)
         		throw new IllegalStateException("Found multiple contacts in haederMap");
         	else if (contacts != null && contacts.size() == 1)
         	{
         		mergeContact(contacts.get(0), contact);
         	}
             fields.setAddress(SipHeaders.CONTACT, contact);
         }
         
         if (linked)
         	linkRequest(request);
         
         request.setRoutingDirective(SipApplicationRoutingDirective.CONTINUE, origRequest);
         
 		return request;
 	}
 	
 	protected void mergeContact(String sSource, Address destination)
 	{
 		try
 		{
 			Address source = new NameAddr(sSource);
 			SipURI uri = (SipURI) source.getURI();
 			SipURI destUri = (SipURI) destination.getURI();
 			destUri.setUser(uri.getUser());
 			Iterator<String> it = uri.getHeaderNames();
 			while (it.hasNext())
 			{
 				String name = (String) it.next();
 				destUri.setHeader(name, uri.getHeader(name));
 			}
 			it = uri.getParameterNames();
 			while (it.hasNext())
 			{
 				String name = (String) it.next();
 				if (!ContactAddress.isReservedUriParam(name))
 					destUri.setParameter(name, uri.getParameter(name));
 			}
 			destination.setDisplayName(source.getDisplayName());
 			it = source.getParameterNames();
 			while (it.hasNext())
 			{
 				String name = (String) it.next();
 				destination.setParameter(name, source.getParameter(name));
 			}
 			
 		}
 		catch (ServletParseException e)
 		{
 			throw new IllegalArgumentException("Invalid contact: " + sSource, e);
 		}
 		catch (ClassCastException e) 
 		{
 			throw new IllegalArgumentException("Invalid contact: " + sSource, e);
 		}
 	}
 	
 	protected void linkRequest(SipRequest request)
 	{
     	_linkedRequest = request;
     	B2bHelper linkedB2bHelper = new B2bHelper(_linkedRequest);
     	linkedB2bHelper._linkedRequest = _request;
     	_linkedRequest.setB2bHelper(linkedB2bHelper);
     	Session origSession = _request.session();
     	Session session = request.session();
     	origSession.setLinkedSession(session);
     	session.setLinkedSession(origSession);
 	}
 	
 	protected List<String> processHeaderMap(Map<String, List<String>> headerMap, SipFields fields, boolean subsequest)
 	{
 		List<String> contacts = null;
         if (headerMap != null)
         {
         	Iterator<String> it = headerMap.keySet().iterator();
         	while (it.hasNext())
 			{
 				String name = it.next();
 				checkSystemHeader(name, subsequest);
 				
 				if (name.length() == 1)
 				{
 					CachedBuffer buffer = SipHeaders.getCompact(name.charAt(0));
 					name = buffer.toString();
 				}
 				
 				List<String> values = headerMap.get(name);
 				if (name.equalsIgnoreCase(SipHeaders.CONTACT))
 				{
 					contacts = values;
 				}
 				else if (subsequest && 
 						(name.equalsIgnoreCase(SipHeaders.FROM) || name.equalsIgnoreCase(SipHeaders.TO)))
 				{
 					// As RFC 4916 is not supported ignore FROM and TO headers, see Sip servlet spec 1.1 4.1.2
 				}
 				else
 				{
 					boolean first = true;
 					Iterator<String> it2 = values.iterator();
 					while (it2.hasNext())
 					{
 						String value = (String) it2.next();
 						if (first)
 						{
 							fields.setString(name, value);
 							first = false;
 						}
 						else
 						{
 							fields.addString(name, value);
 						}
 					}
 				}
 			}
         }
         return contacts;
 	}
 
 	protected void checkSystemHeader(String name, boolean subsequest) throws IllegalArgumentException
 	{	
 		if (!_request.isSystemHeader(name))
 			return;
 	
 		if (name.equalsIgnoreCase(SipHeaders.FROM)
 				|| name.equalsIgnoreCase(SipHeaders.TO)
 				|| name.equalsIgnoreCase(SipHeaders.CONTACT))
 			return;
 		
 		if (!subsequest && name.equalsIgnoreCase(SipHeaders.ROUTE))
 			return;
 			
 		throw new IllegalArgumentException("System header: " + name);
 	}
 	
 	public SipServletRequest createRequest(SipSession session, SipServletRequest origRequest,
 			Map<String, List<String>> headerMap) throws IllegalArgumentException
 	{
 		//if (!origRequest.isInitial())
 		//	throw new IllegalArgumentException("origRequest is not initial");
 		
 		if (!session.getApplicationSession().equals(_request.appSession()))
 			throw new IllegalArgumentException("Not same application session");
 		
 		SipSession linkedSession =  getLinkedSession(origRequest.getSession());
 		if (linkedSession != null && linkedSession != session)
 			throw new IllegalArgumentException("Already link to another session");
 		
 		if (_linkedRequest != null)
 			throw new IllegalArgumentException("Already link to another request");
 		
 		SipRequest request = (SipRequest) session.createRequest(origRequest.getMethod());
 
 		SipFields fields = request.getFields();
 		
 		// Copy origRequest Headers
 		Iterator<String> it = origRequest.getHeaderNames();
 		while (it.hasNext())
 		{
 			String name = (String) it.next();
 			if (!_request.isSystemHeader(name))
 			{
 				ListIterator<String> values = origRequest.getHeaders(name);
 				// ensure headers are copied in right order
 				while (values.hasNext())
 					values.next();
 				while (values.hasPrevious())
 				{
 					String value = (String) values.previous();
 					request.addHeader(name, value);
 				}
 			}
 		}
 		request.setMaxForwards(origRequest.getMaxForwards() == -1 ? ProxyImpl.__maxForwards : origRequest.getMaxForwards() - 1);
 		// Copy headerMaps headers
 		List<String> contacts = processHeaderMap(headerMap, fields, false);
 		// TODO Merge contact with the one set by headerMap
 		
 		linkRequest(request);
 		
 		return request;
 	}
 
 	public SipServletResponse createResponseToOriginalRequest(SipSession sipSession, int status, String reason)
 	{
 		if (!sipSession.isValid())
 			throw new IllegalArgumentException("session invalid");
 		
 		Session session = ((SessionIf) sipSession).getSession();
 		Iterator<ServerTransaction> it = session.getCall().getServerTransactions(session).iterator();
 		while (it.hasNext())
 		{
 			ServerTransaction tx = (ServerTransaction) it.next();
 			SipRequest request = tx.getRequest();
 			if (request.isInitial())
 			{
 				if (tx.isCompleted()) // Pseudo forking
 				{
 					if (status >= 300)
 						throw new IllegalStateException("A final response has been already sent");
 					SipResponse response = new SipResponse(request, status, reason);
 					
 					response.setSession(session.appSession().newSession(session));
 					linkSipSessions(response.getSession(), _request.getSession());
 					response.setSendOutsideTx(true);
 					return response;
 				}
 				else
 				{
 					return request.createResponse(status, reason);
 				}
 			}
 		}
 		return null;
 	}
 
 	public SipSession getLinkedSession(SipSession session)
 	{
 		if (!session.isValid())
 			throw new IllegalArgumentException("Invalid");
 		return ((SessionIf) session).getSession().getLinkedSession();
 	}
 
 	public SipServletRequest getLinkedSipServletRequest(SipServletRequest request)
 	{
 		return _linkedRequest;
 	}
 
 	public List<SipServletMessage> getPendingMessages(SipSession sipSession, UAMode mode)
 	{
 		if (!sipSession.isValid())
 			throw new IllegalArgumentException("session invalid");
 		
 		Session session = ((SessionIf) sipSession).getSession();
 		List<SipServletMessage> list = new ArrayList<SipServletMessage>();
 
 		if (mode == UAMode.UAS)
 		{
 			Iterator<ServerTransaction> it = session.getCall().getServerTransactions(session).iterator();
 			while (it.hasNext())
 			{
 				ServerTransaction tx = (ServerTransaction) it.next();
 				if (!tx.getRequest().isCommitted())
 					list.add(tx.getRequest());
 				
 			}
 		}
 		else
 		{
 			Iterator<ClientTransaction> it = session.getCall().getClientTransactions(session).iterator();
 			while (it.hasNext())
 			{
 				ClientTransaction tx = (ClientTransaction) it.next();
 				if (tx.getState() < Transaction.STATE_COMPLETED)
 					list.add(tx.getRequest());
 				
 			}
 
 		}
 		List<SipServletResponse> invite200 = session.getUncommitted200(mode);
 		if (invite200 != null)
 			list.addAll(invite200);
 		Comparator<SipServletMessage> comparator = new Comparator<SipServletMessage>() {
 
 			public int compare(SipServletMessage o1, SipServletMessage o2)
 			{
 				return (int) (((SipMessage) o1).getCSeq().getNumber() - ((SipMessage) o2).getCSeq().getNumber());
 			}
 			
 		};
 		Collections.sort(list, comparator);
 		return list;
 	}
 
 	public void linkSipSessions(SipSession sipSession1, SipSession sipSession2)
 	{
 		Session session1 = ((SessionIf) sipSession1).getSession();
 		Session session2 = ((SessionIf) sipSession2).getSession();
 		
 		if (!session1.isValid() || !session2.isValid())
 			throw new IllegalArgumentException ("session invalid");
 		if (session1.appSession() != session2.appSession())
 			throw new IllegalArgumentException ("Different SipApplicationSession");
 		if (session1.getLinkedSession() != null || session2.getLinkedSession() != null)
 			throw new IllegalArgumentException ("Session already linked");
 		session1.getSession().setLinkedSession(session2);
     	session2.getSession().setLinkedSession(session1);
 	}
 
 	public void unlinkSipSessions(SipSession sipSession)
 	{
 		Session session = ((SessionIf) sipSession).getSession();
 		if (!session.isValid() || session.getState() == State.TERMINATED)
 			throw new IllegalArgumentException ("session invalid");
 		SessionIf linkedSession = session.getLinkedSession();
 		if (linkedSession == null)
 			throw new IllegalArgumentException ("No linked session");
 		
 		linkedSession.getSession().setLinkedSession(null);
 		session.getSession().setLinkedSession(null);
 	}
 
 }
