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
 
 import java.io.UnsupportedEncodingException;
 import java.net.InetAddress;
 import java.security.Principal;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Locale;
 import java.util.Map;
 
 import javax.servlet.sip.Address;
 import javax.servlet.sip.Parameterable;
 import javax.servlet.sip.ServletParseException;
 import javax.servlet.sip.SipApplicationSession;
 import javax.servlet.sip.SipServletMessage;
 import javax.servlet.sip.SipSession;
 
 import org.cipango.SipHeaders.HeaderInfo;
 import org.cipango.servlet.AppSession;
 import org.cipango.servlet.Session;
 
 import org.cipango.sip.SipConnection;
 import org.cipango.sip.SipConnectors;
 import org.cipango.sip.Transaction;
 import org.cipango.util.ContactAddress;
 import org.cipango.util.ListIteratorProxy;
 import org.cipango.util.ReadOnlyAddress;
 import org.cipango.util.ReadOnlyParameterable;
 import org.cipango.util.concurrent.SessionLockProxy;
 import org.mortbay.io.Buffer;
 import org.mortbay.io.ByteArrayBuffer;
 import org.mortbay.io.BufferCache.CachedBuffer;
 import org.mortbay.jetty.HttpFields;
 import org.mortbay.util.LazyList;
 import org.mortbay.util.QuotedStringTokenizer;
 import org.mortbay.util.StringUtil;
 
 public abstract class SipMessage implements SipServletMessage, Cloneable
 {
     private static final Collection<Locale> __defaultLocale = Collections.singleton(Locale.getDefault());
     protected String _characterEncoding;
     
     protected SipFields _fields = new SipFields();
 	private byte[] _content;
 	
 	private SipConnection _connection;
 	
 	private int _initialTransport;
 	private InetAddress _initialRemoteAddr;
 	private int _initialRemotePort = -1;
 	
 	protected CallSession _callSession;
 	private Transaction _tx;
 	
 	protected Session _session;
 	
 	private boolean _committed = false;
 	
 	private Map<String, Object> _attributes;
 	
 	private HeaderForm _headerForm = HeaderForm.DEFAULT;
 
 	public SipMessage() 
 	{
 	}
 	
 	/**
 	 * @see SipServletMessage#addAcceptLanguage(java.util.Locale)
 	 */
 	public void addAcceptLanguage(Locale locale) 
 	{
 		addHeader(SipHeaders.ACCEPT_LANGUAGE, locale.toString().replace('_','-')); // TODO
 	}
 	
 	/**
 	 * @see SipServletMessage#addAddressHeader(java.lang.String, javax.servlet.sip.Address, boolean)
 	 */
 	public void addAddressHeader(String name, Address address, boolean first) 
 	{	
 		if (isCommitted())
 			throw new IllegalStateException("Message is committed");
 		
 		Buffer buffer = SipHeaders.CACHE.lookup(name);
 		HeaderInfo hi = SipHeaders.getType(buffer);
 		
 		if (hi.getType() != HeaderInfo.ADDRESS && hi.getOrdinal() != -1)
 			throw new IllegalArgumentException("Header: " + name + " is not of address type");
 
 		if (isSystemHeader(hi))
 			throw new IllegalArgumentException(name + " is a system header");
 		
 		if (address == null || name == null) 
 			throw new NullPointerException("name or address is null");
 		
 		_fields.addAddress(buffer, address, first);
 	}
 	
 	/*
 	public void addAddressHeader(Buffer buffer, Address address, boolean first) 
 	{
 		_fields.addAddress(buffer, (NameAddr) address, first);
 	}
 	*/
 	
 	/**
 	 * @see SipServletMessage#addHeader(java.lang.String, java.lang.String)
 	 */
 	public void addHeader(String name, String value) 
 	{
 		if (isCommitted())
 			throw new IllegalStateException("Message is committed");
 		
 		Buffer buffer = SipHeaders.CACHE.lookup(name);
 		HeaderInfo hi = SipHeaders.getType(buffer);
 		
 		if (isSystemHeader(hi)) 
 			throw new IllegalArgumentException(name + " is a system header");
 		
 		if (value == null || name == null) 
 			throw new NullPointerException("name or value is null");
 
 		_fields.addString(buffer, value); 
 	}
 	
 	/**
 	 * @see SipServletMessage#getAcceptLanguage()
 	 */
 	public Locale getAcceptLanguage() 
     {
         Iterator it = getFields().getValues(SipHeaders.ACCEPT_LANGUAGE_BUFFER);
         if (!it.hasNext())
             return Locale.getDefault();
         
         List acceptLanguage = SipFields.qualityList(it);
         if (acceptLanguage.size() == 0)
             return Locale.getDefault();
         
         int size = acceptLanguage.size();
         
         for (int i=0; i<size; i++)
         {
             String language = (String)acceptLanguage.get(i);
             language=HttpFields.valueParameters(language, null);
             String country = "";
             int dash = language.indexOf('-');
             if (dash > -1)
             {
                 country = language.substring(dash + 1).trim();
                 language = language.substring(0, dash).trim();
             }
             return new Locale(language, country);
         }
         
         return Locale.getDefault();
     }
 	
 	/**
 	 * @see SipServletMessage#getAcceptLanguages()
 	 */
 	public Iterator<Locale> getAcceptLanguages() 
     {
         Iterator<String> it = getFields().getValues(SipHeaders.ACCEPT_LANGUAGE_BUFFER);
 
         if (!it.hasNext())
             return __defaultLocale.iterator();
 
         List acceptLanguage = SipFields.qualityList(it);
         
         if (acceptLanguage.size() == 0)
             return __defaultLocale.iterator();
         
         Object langs = null;
         int size = acceptLanguage.size();
         
         for (int i = 0; i < size; i++)
         {
             String language = (String) acceptLanguage.get(i);
             language = HttpFields.valueParameters(language, null);
             String country = "";
             int dash = language.indexOf('-');
             if (dash > -1)
             {
                 country = language.substring(dash + 1).trim();
                 language = language.substring(0, dash).trim();
             }
             langs = LazyList.ensureSize(langs, size);
             langs = LazyList.add(langs, new Locale(language, country));
         }
         
         if (LazyList.size(langs) == 0)
             return __defaultLocale.iterator();
         
         return LazyList.getList(langs).iterator();
 	}
 	
 	/**
 	 * @see SipServletMessage#getAddressHeader(java.lang.String)
 	 */
 	public Address getAddressHeader(String name) throws ServletParseException 
 	{
 		Buffer buffer = SipHeaders.CACHE.lookup(name);
 		HeaderInfo hi = SipHeaders.getType(buffer);
 		
 		if (hi.getType() != HeaderInfo.ADDRESS && hi.getOrdinal() != -1)
 			throw new ServletParseException("Header: " + name + " is not of address type");
 		
 		Address address;
 		try
 		{
 			address =  _fields.getAddress(buffer);
 		}
 		catch (LazyParsingException e)
 		{
 			throw new ServletParseException(e);
 		}
 		
 		if (buffer == SipHeaders.CONTACT_BUFFER && isSystemHeader(hi) && !isCommitted() && address != null)
 			return new ContactAddress(address);
 		else if ((isSystemHeader(hi) || isCommitted()) && address != null)
 			return new ReadOnlyAddress(address); 
 		else 
 			return address;
 	}
 	
 	/**
 	 * @see SipServletMessage#getAddressHeaders(String)
 	 */
 	public ListIterator<Address> getAddressHeaders(String name) throws ServletParseException 
 	{
 		Buffer buffer = SipHeaders.CACHE.lookup(name);
 		HeaderInfo hi = SipHeaders.getType(buffer);
 		
 		if (hi.getType() != HeaderInfo.ADDRESS && hi.getOrdinal() != -1)
 			throw new ServletParseException("Header: " + name + " is not of address type");
 		
 		ListIterator<Address> it = _fields.getAddressValues(buffer);
 		try
 		{
 			while (it.hasNext())
 				it.next();
 			while (it.hasPrevious())
 				it.previous();
 		}
 		catch (LazyParsingException e) 
 		{
 			throw new ServletParseException(e);
 		}
 		if (isSystemHeader(hi) || isCommitted())
 		{
 			return new ListIteratorProxy<Address>(it)
 			{
 				@Override
 				public Address next() { return new ReadOnlyAddress(super.next()); }
 				@Override
 				public Address previous() { return new ReadOnlyAddress(super.previous()); }
 			};
 		}
 		return it;
 	}
 	
 	/**
 	 * @see SipServletMessage#getApplicationSession()
 	 */
 	public SipApplicationSession getApplicationSession() 
 	{
 		return _session.getApplicationSession();
 	}
 	
 	/**
 	 * @see SipServletMessage#getApplicationSession(boolean)
 	 */
 	public SipApplicationSession getApplicationSession(boolean create) 
 	{
 		return getApplicationSession();
 	}
 	
 	/**
 	 * @see SipServletMessage#getAttribute(java.lang.String)
 	 */
 	public Object getAttribute(String name) 
 	{
 		if (_attributes != null) 
 			return _attributes.get(name);
 		return null;
 	}
 	
 	/**
 	 * @see SipServletMessage#removeAttribute(String)
 	 */
 	public void removeAttribute(String name)
 	{
 		if (_attributes == null)
 			return;
 		_attributes.remove(name);
 	}
 	
 	/**
 	 * @see SipServletMessage#getAttributeNames()
 	 */
 	@SuppressWarnings("unchecked")
 	public Enumeration<String> getAttributeNames() 
 	{
 		if (_attributes != null) 
 			return Collections.enumeration(_attributes.keySet());
 		
 		return Collections.enumeration(Collections.EMPTY_LIST);
 	}
 	
 	/**
 	 * @see SipServletMessage#getCallId()
 	 */
 	public String getCallId() 
 	{
 		return _fields.getString(SipHeaders.CALL_ID_BUFFER);
 	}
 	
 	/**
 	 * @see SipServletMessage#getCharacterEncoding()
 	 */
 	public String getCharacterEncoding() 
     {
         if (_characterEncoding != null)
             return _characterEncoding;
         String contentType = getContentType();
         
         if (contentType != null)
         {
             int i0 = contentType.indexOf(';');
             if (i0 > 0)
             {
                 int i1 = contentType.indexOf("charset=", i0 + 1);
                 if (i1 >= 0)
                 {
                     int i8 = i1+8;
                     int i2 = contentType.indexOf(';',i8);
                     
                     if (i2 > 0)
                         _characterEncoding = QuotedStringTokenizer.unquote(contentType.substring(i8, i2));
                     else 
                         _characterEncoding = QuotedStringTokenizer.unquote(contentType.substring(i8));
                 }
             }
         }
         return _characterEncoding; // TODO contentlanguage ?
 	}
 	
 	/**
 	 * @see SipServletMessage#getContent()
 	 */
 	public Object getContent() throws UnsupportedEncodingException
     {
         String contentType = getContentType();
         if (_content != null && contentType != null
         		&& (StringUtil.startsWithIgnoreCase(contentType, "text") || contentType.equalsIgnoreCase("application/sdp")))
         {
             String charset = getCharacterEncoding();
             if (charset == null)
                 charset = StringUtil.__UTF8;
             
             return new String(_content, charset);
         }
         else 
         {
             return _content;
         }
 	}
 	
 	/**
 	 * @see SipServletMessage#getContentLanguage()
 	 */
 	public Locale getContentLanguage() 
 	{ 
 		String s = getHeader(SipHeaders.CONTENT_LANGUAGE);
 		if (s == null) 
 			return null;
 		
 		return new Locale(s);
 	}
 	
 	/**
 	 * @see SipServletMessage#getContentLength()
 	 */
 	public int getContentLength() 
 	{
 		int length = (int) _fields.getLong(SipHeaders.CONTENT_LENGTH_BUFFER);
 		if (length == -1) 
 		{
 			if (_content == null) 
 				return 0;
 			else 
 				return _content.length;
 		} 
 		else 
 		{
 			return length;
 		}
 	}
 	
 	/**
 	 * @see SipServletMessage#getContentType()
 	 */
 	public String getContentType() 
 	{
 		return getHeader(SipHeaders.CONTENT_TYPE); // TODO parse
 	}
 	
 	/**
 	 * @see SipServletMessage#getExpires()
 	 */
 	public int getExpires() 
 	{
 		return (int) _fields.getLong(SipHeaders.EXPIRES_BUFFER);
 	}
 	
 	/**
 	 * @see SipServletMessage#getFrom()
 	 */
 	public Address getFrom() 
 	{
 		return new ReadOnlyAddress(_fields.getAddress(SipHeaders.FROM_BUFFER));
 	}
 	
 	/**
 	 * @see SipServletMessage#getHeader(java.lang.String)
 	 */
 	public String getHeader(String name) 
 	{
 		if (name == null)
 			throw new NullPointerException("Null name");
 		return _fields.getString(name);
 	}
 	
 	/**
 	 * @see SipServletMessage#getHeaders(java.lang.String)
 	 */
 	public ListIterator<String> getHeaders(String name) 
 	{
 		if (name == null)
 			throw new NullPointerException("Null name");
 		return _fields.getValues(name);
 	}
 	
 	/**
 	 * @see SipServletMessage#getHeaderNames()
 	 */
 	public Iterator<String> getHeaderNames() 
 	{
 		return _fields.getNames();
 	}
 	
 	/**
 	 * @see SipServletMessage#getLocalAddr()
 	 */
 	public String getLocalAddr() 
 	{
 		return _connection != null ?_connection.getLocalAddress().getHostAddress() : null;
 	}
 	
 	/**
 	 * @see SipServletMessage#getLocalPort()
 	 */
 	public int getLocalPort() 
 	{
 		
 		return _connection != null ? _connection.getLocalPort() : -1;
 	}
 	
 	/**
 	 * @see SipServletMessage#getProtocol()
 	 */
 	public String getProtocol() 
 	{
 		return SipVersions.SIP_2_0;
 	}
 	
 	/**
 	 * @see SipServletMessage#getRawContent()
 	 */
 	public byte[] getRawContent() 
 	{
 		return _content;
 	}
 	
 	/**
 	 * @see SipServletMessage#getRemoteAddr()
 	 */
 	public String getRemoteAddr()
 	{
 		return _connection != null ? _connection.getRemoteAddress().getHostAddress() : null;
 	}
 	
 	/**
 	 * @see SipServletMessage#getRemotePort()
 	 */
 	public int getRemotePort() 
 	{
 		return _connection != null ? _connection.getRemotePort() : null;
 	}
 	
 	/**
 	 * @see SipServletMessage#getRemoteUser()
 	 */
 	public String getRemoteUser() 
 	{
 		return null;
 	}
 	
 	/**
 	 * @see SipServletMessage#getSession()
 	 */
 	public SipSession getSession() 
 	{
 		return new SessionLockProxy(_session);
 	}
 	
 	/**
 	 * @see SipServletMessage#getSession(boolean)
 	 */
 	public SipSession getSession(boolean create) 
 	{
 		return getSession();
 	}
 
 	/**
 	 * @see SipServletMessage#getTo()
 	 */
 	public Address getTo() 
 	{
 		return new ReadOnlyAddress(_fields.getAddress(SipHeaders.TO_BUFFER));
 	}
 	
 	/**
 	 * @see SipServletMessage#getTransport()
 	 */
 	public String getTransport() 
 	{
 		if (_connection == null)
 			return null;
 		return _connection.getConnector().getTransport();
 	}
 	
 	/**
 	 * @see SipServletMessage#getUserPrincipal()
 	 */
 	public Principal getUserPrincipal()
 	{
 		return null;
 	}
 	
 	/**
 	 * @see SipServletMessage#isCommitted()
 	 */
 	public boolean isCommitted()
 	{
 		return _committed;
 	}
 	
 	/**
 	 * @see SipServletMessage#isSecure()
 	 */
 	public boolean isSecure()
 	{
 		return false; // TODO
 	}
 	
 	/**
 	 * @see SipServletMessage#isUserInRole(java.lang.String)
 	 */
 	public boolean isUserInRole(String role)
 	{
 		return false;
 	}
 	
 	/**
 	 * @see SipServletMessage#removeHeader(java.lang.String)
 	 */
 	public void removeHeader(String name)
 	{
 		if (isCommitted())
 			throw new IllegalStateException("Message is committed");
 		
 		Buffer buffer = SipHeaders.CACHE.lookup(name);
 		HeaderInfo hi = SipHeaders.getType(buffer);
 		
 		if (isSystemHeader(hi)) 
 			throw new IllegalArgumentException(name + " is a system header");
 
 		_fields.remove(buffer);
 	}
 	
 	/**
 	 * @see SipServletMessage#setAcceptLanguage(java.util.Locale)
 	 */
 	public void setAcceptLanguage(Locale locale) 
 	{
 		setHeader(SipHeaders.ACCEPT_LANGUAGE, locale.toString().replace('_', '-'));
 	}
 	
 	/**
 	 * @see SipServletMessage#setAddressHeader(java.lang.String, javax.servlet.sip.Address)
 	 */
 	public void setAddressHeader(String name, Address addr) 
 	{
 		if (isCommitted())
 			throw new IllegalStateException("Message is committed");
 		
 		Buffer buffer = SipHeaders.CACHE.lookup(name);
 		HeaderInfo hi = SipHeaders.getType(buffer);
 		
 		if (isSystemHeader(hi)) 
 			throw new IllegalArgumentException(name + " is a system header");
 
		_fields.setAddress(buffer, addr);
 	}
 	
 	/**
 	 * @see SipServletMessage#setAttribute(java.lang.String, java.lang.Object)
 	 */
 	public void setAttribute(String name, Object o) 
 	{
 		if (o == null || name == null) 
 			throw new NullPointerException("name or value is null");
 		
 		if (_attributes == null) 
 			_attributes = new HashMap<String, Object>();
 
 		_attributes.put(name, o);
 	}
 	
 	/**
 	 * @see SipServletMessage#setCharacterEncoding(java.lang.String)
 	 */
 	public void setCharacterEncoding(String encoding) throws UnsupportedEncodingException
 	{
 		"".getBytes(encoding);
 		_characterEncoding = encoding;
 	}
 	
 	/**
 	 * @see SipServletMessage#setContent(java.lang.Object, java.lang.String)
 	 */
 	public void setContent(Object o, String type) throws UnsupportedEncodingException 
 	{
 		// TODO add sdp, ...
 		if (isCommitted())
 			throw new IllegalStateException("Is committed");
 		
 		if (o == null)
 		{
 			_content = null;
 			setContentLength(0);
 			setContentType(type);
 		}
 		else if (o instanceof byte[])
 		{
 			_content = (byte[]) o;
 			setContentLength(_content.length);
 			setContentType(type);
 		} 
 		else if (o instanceof String && type.startsWith("text/"))
 		{
 			String s = (String) o;
 			setContentType(type);
 			String charset = getCharacterEncoding();
 			if (charset == null)
 				charset = StringUtil.__UTF8;
 			
 			_content = s.getBytes(charset);
 			setContentLength(_content.length);
 		} 
 		else
 		{
 			throw new IllegalArgumentException("Unsupported object type");
 		}
 	}
 	
 	/**
 	 * @see SipServletMessage#setContentLanguage(java.util.Locale)
 	 */
 	public void setContentLanguage(Locale locale) 
     {
         if (locale == null)
         {
             getFields().remove(SipHeaders.CONTENT_LANGUAGE_BUFFER);
         }
         else 
         {
             getFields().setString(SipHeaders.CONTENT_LANGUAGE_BUFFER, locale.toString().replace('_','-'));
             if (_characterEncoding == null)
                 _characterEncoding = _session.appSession().getContext().getLocaleEncoding(locale);
         }
 	}
 	
 	/**
 	 * @see SipServletMessage#setContentLength(int)
 	 */
 	public void setContentLength(int length) 
 	{
 		if (isCommitted())
 			throw new IllegalStateException("Message is committed");
 		
 		setHeader(SipHeaders.CONTENT_LENGTH, Integer.toString(length));
 	}
 	
 	/**
 	 * @see SipServletMessage#setContentType(java.lang.String)
 	 */
 	public void setContentType(String contentType) 
     {
 		if (isCommitted())
 			throw new IllegalStateException("Message is committed");
         if (contentType == null)
             getFields().remove(SipHeaders.CONTENT_TYPE_BUFFER);      
         else
         {
             int i0 = contentType.indexOf(';');
             if (i0 > 0)
             {
                 int i1 = contentType.indexOf("charset=", i0 + 1);
                 if (i1 >= 0)
                 {
                     int i8 = i1+8;
                     int i2 = contentType.indexOf(';',i8);
                     
                     if (i2 > 0)
                         _characterEncoding = QuotedStringTokenizer.unquote(contentType.substring(i8, i2));
                     else 
                         _characterEncoding = QuotedStringTokenizer.unquote(contentType.substring(i8));
                 }
             }
             getFields().setString(SipHeaders.CONTENT_TYPE_BUFFER, contentType);
         }
 	}
 	
 	/**
 	 * @see SipServletMessage#setExpires(int)
 	 */
 	public void setExpires(int seconds) 
 	{
 		if (seconds < 0) 
 			removeHeader(SipHeaders.EXPIRES);
 		else 
 			setHeader(SipHeaders.EXPIRES, Long.toString(seconds));
 	}
 	
 	/**
 	 * @see SipServletMessage#setHeader(java.lang.String, java.lang.String)
 	 */
 	public void setHeader(String name, String value) 
 	{
 		if (isCommitted())
 			throw new IllegalStateException("Message is committed");
 		
 		if (name == null || value == null)
 			throw new NullPointerException("Null value or name");
 		
 		Buffer buffer = SipHeaders.CACHE.lookup(name);
 		HeaderInfo hi = SipHeaders.getType(buffer);
 		
 		if (isSystemHeader(hi)) 
 			throw new IllegalArgumentException(name + " is a system header");
 
 		getFields().setString(buffer, value); 
 	}
 	
 	/**
 	 * @see SipServletMessage#addParameterableHeader(String, Parameterable, boolean)
 	 */
 	public void addParameterableHeader(String name, Parameterable value, boolean first)
 	{
 		if (isCommitted())
 			throw new IllegalStateException("Message is committed");
 		
 		Buffer buffer = SipHeaders.CACHE.lookup(name);
 		HeaderInfo hi = SipHeaders.getType(buffer);
 		
 		if (isSystemHeader(hi)) 
 			throw new IllegalArgumentException(name + " is a system header");
 		
 		if (hi.getType() != HeaderInfo.PARAMETERABLE && hi.getOrdinal() != -1)
 			throw new IllegalArgumentException("Header " + name + " is not of parameterable type");
 		
 		getFields().addParameterable(buffer, (ParameterableImpl) value, first);
 	}
 	
 	/**
 	 * @see SipServletMessage#getParameterableHeader(String)
 	 */
 	public Parameterable getParameterableHeader(String name) throws ServletParseException
 	{
 		Buffer buffer = SipHeaders.CACHE.lookup(name);
 		HeaderInfo hi = SipHeaders.getType(buffer);
 		
 		if (hi.getType() != HeaderInfo.PARAMETERABLE && hi.getType() != HeaderInfo.ADDRESS && hi.getOrdinal() != -1)
 			throw new ServletParseException("Header: " + name + " is not of parameterable type");
 		
 		Parameterable p = getFields().getParameterable(buffer);
 		
 		if ((isSystemHeader(hi) || isCommitted()) && p != null) 
 			return new ReadOnlyParameterable(p);
 		else 
 			return p;
 	}
 
 	/**
 	 * @see SipServletMessage#getParameterableHeaders(String)
 	 */
 	public ListIterator<? extends Parameterable> getParameterableHeaders(String name) throws ServletParseException
 	{
 		Buffer buffer = SipHeaders.CACHE.lookup(name);
 		HeaderInfo hi = SipHeaders.getType(buffer);
 		
 		if (hi.getType() != HeaderInfo.PARAMETERABLE && hi.getType() != HeaderInfo.ADDRESS && hi.getOrdinal() != -1)
 			throw new ServletParseException("Header: " + name + " is not of parametrable type");
 		
 		ListIterator<Parameterable> it = getFields().getParameterableValues(buffer);
 		
 		if (isSystemHeader(hi) || isCommitted())
 		{
 			return new ListIteratorProxy<Parameterable>(it)
 			{
 				@Override
 				public Parameterable next() { return new ReadOnlyParameterable(super.next()); }
 				@Override
 				public Parameterable previous() { return new ReadOnlyParameterable(super.previous()); }
 			};
 		}
 		
 		return it;
 	}
 	
 	/**
 	 * @see SipServletMessage#setParameterableHeader(String, Parameterable)
 	 */
 	public void setParameterableHeader(String name, Parameterable value)
 	{
 		if (isCommitted())
 			throw new IllegalStateException("Message is committed");
 		
 		Buffer buffer = SipHeaders.CACHE.lookup(name);
 		HeaderInfo hi = SipHeaders.getType(buffer);
 		
 		if (isSystemHeader(hi)) 
 			throw new IllegalArgumentException(name + " is a system header");
 		
 		if (hi.getType() != HeaderInfo.PARAMETERABLE && hi.getOrdinal() != -1)
 			throw new IllegalArgumentException("Header " + name + " is not of parameterable type");
 		
 		getFields().setParameterable(buffer, (ParameterableImpl) value);
 	}
 
 	/**
 	 * @see SipServletMessage#getHeaderForm()
 	 */
 	public HeaderForm getHeaderForm()
 	{
 		return _headerForm;
 	}
 	
 	/**
 	 * @see SipServletMessage#setHeaderForm(javax.servlet.sip.SipServletMessage.HeaderForm)
 	 */
 	public void setHeaderForm(HeaderForm form)
 	{
 		if (form == null)
 			throw new NullPointerException("Null form");
 		_headerForm = form;
 	}
 	
 	/**
 	 * @see SipServletMessage#@ialRemoteAddr()
 	 */
 	public String getInitialRemoteAddr()
 	{
 		if (_initialRemoteAddr == null)
 			return null;
 		
 		return _initialRemoteAddr.getHostAddress();
 	}
 
 	/**
 	 * @see SipServletMessage#getInitialRemotePort()
 	 */
 	public int getInitialRemotePort()
 	{
 		return _initialRemotePort;
 	}
 
 	/**
 	 * @see SipServletMessage#getInitialTransport()
 	 */
 	public String getInitialTransport()
 	{
 		return SipConnectors.getName(_initialTransport);
 	}
 
 	// --
 	
 	protected abstract boolean canSetContact();
 	
 	public void setConnection(SipConnection connection)
 	{
 		_connection = connection;
 	}
 	
 	public SipConnection getConnection()
 	{
 		return _connection;
 	}
 	
 	public Address from() 
 	{
 		return _fields.getAddress(SipHeaders.FROM_BUFFER);
 	}
 	
 	public Address to() 
 	{
 		return _fields.getAddress(SipHeaders.TO_BUFFER);
 	}
 	
 	public Object clone() 
 	{
 		try 
 		{
 			SipMessage clone = (SipMessage) super.clone(); 
 			clone._fields = (SipFields) _fields.clone();
 			clone._committed = false;
 			clone._tx = null;
 			clone._attributes = null;
 			clone._connection = null;
 			//clone._session = null;
 			return clone;
 		} 
 		catch (CloneNotSupportedException _) 
 		{
 			throw new RuntimeException("!cloneable");
 		}
 	}
 	
 	public boolean isAck() 
 	{
 		return SipMethods.ACK.equalsIgnoreCase(getMethod());
 	}
 	
 	public boolean isBye() 
 	{
 		return SipMethods.BYE.equalsIgnoreCase(getMethod());
 	}
 	
 	public boolean isCancel() 
 	{
 		return SipMethods.CANCEL.equalsIgnoreCase(getMethod());
 	}
 	
 	public boolean isInvite() 
 	{
 		return SipMethods.INVITE.equalsIgnoreCase(getMethod());
 	}
 	
 	public boolean isNotify() 
 	{
 		return SipMethods.NOTIFY.equalsIgnoreCase(getMethod());
 	}
 	
     public boolean isPrack()
     {
         return SipMethods.PRACK.equalsIgnoreCase(getMethod());
     }
     
 	public boolean isRefer() 
 	{
 		return SipMethods.REFER.equalsIgnoreCase(getMethod());
 	}
 	
 	public boolean isRegister() 
 	{
 		return SipMethods.REGISTER.equalsIgnoreCase(getMethod());
 	}
 	
 	public boolean isSubscribe() 
 	{
 		return SipMethods.SUBSCRIBE.equalsIgnoreCase(getMethod());
 	}
 	
 	public boolean isOptions()
 	{
 		return SipMethods.OPTIONS.equalsIgnoreCase(getMethod());
 	}
 	
 	public boolean isUpdate() 
 	{
 		return SipMethods.UPDATE.equalsIgnoreCase(getMethod());
 	}
 	
 	protected boolean isSystemHeader(String name) 
 	{
 		CachedBuffer buffer = SipHeaders.CACHE.get(name);
 		if (buffer == null) 
 			return false;
 		
 		SipHeaders.HeaderInfo type = SipHeaders.__types[buffer.getOrdinal()];
 		return (type.isSystem() || 
 				(buffer.getOrdinal() == SipHeaders.CONTACT_ORDINAL && !canSetContact()));
 	}
 	
 	protected boolean isSystemHeader(HeaderInfo type)
 	{
 		return (type.isSystem() || 
 				(type.getOrdinal() == SipHeaders.CONTACT_ORDINAL && !canSetContact()));
 	}
 	
 	public abstract boolean isRequest();
 	public abstract boolean needsContact();
 	
 	public CSeq getCSeq() 
 	{
 		try 
 		{
 			return _fields.getCSeq();
 		} 
 		catch (ServletParseException e) 
 		{
 			return null;
 		}
 	}
 	
 	public String getDialogId() 
 	{
 		return getDialogId(true);
 	}
 	
 	public String getDialogId(boolean fromFirst) 
 	{	
         String fromTag = getFrom().getParameter("tag");
         String toTag = getTo().getParameter("tag");
         
         if (fromFirst) 
         {
             return getCallId() + "|" +
                   (fromTag == null ? "?" : fromTag) + "|" +
                   (toTag == null ? "?" : toTag);
         } 
         else 
         {
             return getCallId() + "|" +
                   (toTag == null ? "?" : toTag) + "|" +
                   (fromTag == null ? "?" : fromTag);
         }
     }
 	
 	public Via getTopVia()
 	{
 		return _fields.getVia();
 	}
 	
 	public SipFields getFields()
 	{
 		return _fields;
 	}
 	
 	public void setCallSession(CallSession callSession)
 	{
 		_callSession = callSession;
 	}
 	
 	public CallSession getCallSession()
 	{
         return _callSession;
 	}
 	
 	public void setCommitted(boolean b) 
 	{
 		_committed = b;
 	}
 	
 	public void setRawContent(byte[] content)
 	{
 		_content = content;
 	}
 	
 	public void setSession(Session session)
 	{
 		_session = session;
 	}
 	
 	public void setTransaction(Transaction tx)
 	{
         _tx = tx;
     }
 	
 	public void setToTag(String tag)
 	{
 		Address to = _fields.getAddress(SipHeaders.TO_BUFFER);
 		to.setParameter(SipParams.TAG, tag);
 	}
 
     public Transaction getTransaction()
     {
         return _tx;
     }
     
     public Session session()
     {
     	return _session;
     }
     
     public AppSession appSession()
     {
     	return _session.appSession();
     }
     
     public abstract String getRequestLine();
     
     public String toString() 
     {
     	Buffer buffer = new ByteArrayBuffer(64000); 
     	new SipGenerator().generate(buffer, this);
     	return buffer.toString();
     }
 }
