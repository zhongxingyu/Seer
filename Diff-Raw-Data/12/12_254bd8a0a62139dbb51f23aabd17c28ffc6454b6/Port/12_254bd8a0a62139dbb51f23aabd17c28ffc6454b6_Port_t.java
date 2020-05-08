 // Copyright (C) 2012 jOVAL.org.  All rights reserved.
 // This software is licensed under the AGPL 3.0 license available at http://www.joval.org/agpl_v3.txt
 
 package jwsmv.wsman;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.HttpURLConnection;
 import java.net.Proxy;
 import java.net.PasswordAuthentication;
 import java.net.URL;
 import java.math.BigInteger;
 import java.security.PrivilegedActionException;
 import java.util.Date;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Properties;
 import java.util.UUID;
 import javax.net.ssl.HttpsURLConnection;
 import javax.net.ssl.SSLContext;
 import javax.security.auth.login.LoginException;
 import javax.security.auth.login.FailedLoginException;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBElement;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 import javax.xml.bind.Unmarshaller;
 import javax.xml.transform.dom.DOMSource;
 import org.w3c.dom.Node;
 
 import org.slf4j.cal10n.LocLogger;
 
 import org.dmtf.wsman.Locale;
 import org.xmlsoap.ws.addressing.AttributedURI;
 import org.xmlsoap.ws.addressing.EndpointReferenceType;
 import org.xmlsoap.ws.transfer.AnyXmlOptionalType;
 import org.xmlsoap.ws.transfer.AnyXmlType;
 import org.w3c.soap.envelope.Body;
 import org.w3c.soap.envelope.Envelope;
 import org.w3c.soap.envelope.Fault;
 import org.w3c.soap.envelope.Header;
 
 import jwsmv.Constants;
 import jwsmv.Message;
 import jwsmv.http.HttpSocketConnection;
 import jwsmv.http.KerberosHttpURLConnection;
 import jwsmv.http.NtlmHttpURLConnection;
 import jwsmv.util.Base64;
 
 /**
  * A Web-Services port implementation for MS-WSMV (Microsoft Web Services Management for Vista).
  *
  * Since MS-WSMV is implemented on top of WS-Management, including the non-BP/1.0 compliant WS-Transfer specification,
  * it is necessary to implement a custom SOAP client to perform the various operations therein entailed.  This is the
  * implementation for those operations, which should support both NTLM and Kerberos authentication, and HTTP
  * encryption.
  *
  * @author David A. Solin
  * @version %I% %G%
  */
 public class Port implements Constants {
     private static final String RESOURCE = "ws-man.properties";
     private static final JAXBContext JAXB;
     static {
 	ClassLoader cl = Port.class.getClassLoader();
 	Properties props = new Properties();
 	InputStream rsc = cl.getResourceAsStream(RESOURCE);
 	if (rsc == null) {
 	    throw new RuntimeException(Message.getMessage(Message.ERROR_MISSING_RESOURCE, RESOURCE));
 	} else {
 	    try {
 		props.load(rsc);
 		JAXB = JAXBContext.newInstance(props.getProperty("ws-man.packages"), cl);
 	    } catch (Exception e) {
 		throw new RuntimeException(e);
 	    }
 	}
     }
 
     /**
      * Enumeration of supported authentication schemes.
      */
     public enum AuthScheme {
 	NONE, BASIC, NTLM, KERBEROS;
     }
 
     private AuthScheme scheme;
     private String url;
     private Proxy proxy;
     private PasswordAuthentication cred, proxyCred = null;
     private Marshaller marshaller;
     private Unmarshaller unmarshaller;
     private boolean encrypt;
     private OutputStream debug;
     private LocLogger logger;
 
     /**
      * Create a SOAP Web-Services port.
      */
     public Port(String url, PasswordAuthentication cred) throws JAXBException {
 	marshaller = JAXB.createMarshaller();
 	marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
 	marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
 	unmarshaller = JAXB.createUnmarshaller();
 	scheme = AuthScheme.NTLM;
 	logger = Message.getLogger();
 	proxy = Proxy.NO_PROXY;
 	this.url = url;
 	this.cred = cred;
 	this.encrypt = true;
 	this.debug = null;
     }
 
     /**
      * Set a proxy and proxy credentials.
      */
     public void setProxy(Proxy proxy, PasswordAuthentication proxyCred) {
 	this.proxy = proxy;
 	this.proxyCred = proxyCred;
     }
 
     /**
      * Enable/disable SOAP encryption.
      */
     public void setEncryption(boolean encrypt) {
 	this.encrypt = encrypt;
     }
 
     /**
      * Enable debug logging by setting an OutputStream to which to log SOAP traffic (or null to disable).
      */
     public void setDebug(OutputStream debug) {
 	this.debug = debug;
     }
 
     public void setLogger(LocLogger logger) {
 	this.logger = logger;
     }
 
     public LocLogger getLogger() {
 	return logger;
     }
 
     public synchronized void marshal(Object obj, Node node) throws JAXBException {
 	marshaller.marshal(obj, node);
     }
 
     public synchronized Object unmarshal(Node node) throws JAXBException {
 	return unmarshaller.unmarshal(new DOMSource(node));
     }
 
     public Object dispatch(String action, List<Object> headers, Object input)
 		throws IOException, JAXBException, FaultException, FailedLoginException {
 
 	Header header = Factories.SOAP.createHeader();
 
 	AttributedURI to = Factories.ADDRESS.createAttributedURI();
 	to.setValue(url);
 	to.getOtherAttributes().put(MUST_UNDERSTAND, "true");
 	header.getAny().add(Factories.ADDRESS.createTo(to));
 
 	EndpointReferenceType endpointRef = Factories.ADDRESS.createEndpointReferenceType();
 	AttributedURI address = Factories.ADDRESS.createAttributedURI();
 	address.setValue(REPLY_TO);
 	address.getOtherAttributes().put(MUST_UNDERSTAND, "true");
 	endpointRef.setAddress(address);
 	header.getAny().add(Factories.ADDRESS.createReplyTo(endpointRef));
 
 	AttributedURI soapAction = Factories.ADDRESS.createAttributedURI();
 	soapAction.setValue(action);
 	soapAction.getOtherAttributes().put(MUST_UNDERSTAND, "true");
 	header.getAny().add(Factories.ADDRESS.createAction(soapAction));
 
 	AttributedURI messageId = Factories.ADDRESS.createAttributedURI();
 	messageId.setValue("uuid:" + UUID.randomUUID().toString().toUpperCase());
 	header.getAny().add(Factories.ADDRESS.createMessageID(messageId));
 
 	for (Object obj : headers) {
 	    header.getAny().add(obj);
 	}
 
 	Locale locale = Factories.WSMAN.createLocale();
 	locale.setLang("en-US");
 	locale.getOtherAttributes().put(MUST_UNDERSTAND, "false");
 	header.getAny().add(locale);
 
 	//
 	// Convert any non-directly-serializable WS-Transfer input types
 	//
 	if (input instanceof AnyXmlType) {
 	    input = ((AnyXmlType)input).getAny();
 	} else if (input instanceof AnyXmlOptionalType) {
 	    input = ((AnyXmlOptionalType)input).getAny();
 	}
 
 	Body body = Factories.SOAP.createBody();
 	if (input != null) {
 	    body.getAny().add(input);
 	}
 
 	Envelope request = Factories.SOAP.createEnvelope();
 	request.setHeader(header);
 	request.setBody(body);
 
 	URL u = new URL(url);
 	boolean retry = false;
 	Object result = null;
 	HttpURLConnection conn = null;
 	do {
 	    try {
 		if (conn != null) {
 		    conn.disconnect();
 		}
 
 		logger.trace(Message.STATUS_CONNECT, url, scheme);
 		switch(scheme) {
 		  case NONE:
 		    switch(proxy.type()) {
 		      case DIRECT:
 			conn = (HttpURLConnection)u.openConnection();
 			break;
 		      default:
 			conn = (HttpURLConnection)u.openConnection(proxy);
 			break;
 		    }
 		    break;
 
 		  case KERBEROS:
 		    conn = KerberosHttpURLConnection.openConnection(u, cred, encrypt);
 		    ((KerberosHttpURLConnection)conn).setProxy(proxy, proxyCred);
 		    break;
 
 		  case NTLM:
 		    conn = NtlmHttpURLConnection.openConnection(u, cred, encrypt);
 		    ((NtlmHttpURLConnection)conn).setProxy(proxy, proxyCred);
 		    break;
 
 		  case BASIC:
 		    switch(proxy.type()) {
 		      case DIRECT:
 			conn = (HttpURLConnection)u.openConnection();
 			break;
 		      default:
 			conn = (HttpURLConnection)u.openConnection(proxy);
 			if (proxyCred != null) {
 			    String clear = proxyCred.getUserName() + ":" + new String(proxyCred.getPassword());
 			    String auth = "Basic " + Base64.encodeBytes(clear.getBytes());
			    conn.setRequestProperty("Proxy-Authorization", auth);
 			}
 			break;
 		    }
 		    break;
 		}
 
 		conn.setDoInput(true);
 		conn.setDoOutput(true);
 		conn.setRequestMethod("POST");
 		conn.setRequestProperty("Content-Type", "application/soap+xml;charset=UTF-8");
 
 		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
 		synchronized(marshaller) {
 		    marshaller.marshal(Factories.SOAP.createEnvelope(request), buffer);
 		}
 		byte[] bytes = buffer.toByteArray();
 		conn.setFixedLengthStreamingMode(bytes.length);
 
 		conn.connect();
 		OutputStream out = conn.getOutputStream();
 		out.write(bytes);
 		out.flush();
 		logger.debug(Message.STATUS_REQUEST, action);
 		if (debug != null) {
 		    StringBuffer sb = new StringBuffer("[").append(new Date().toString()).append("] - SOAP Request:\r\n");
 		    debug.write(sb.toString().getBytes());
 		    debug.write(bytes);
 		    debug.write("\r\n".getBytes());
 		    debug.flush();
 		}
 
 		retry = false;
 		int code = conn.getResponseCode();
 		switch(code) {
 		  case HttpURLConnection.HTTP_BAD_REQUEST:
 		  case HttpURLConnection.HTTP_INTERNAL_ERROR:
 		    result = getSOAPBodyContents(conn.getErrorStream(), conn.getContentType());
 		    break;
 
 		  case HttpURLConnection.HTTP_OK:
 		    result = getSOAPBodyContents(conn.getInputStream(), conn.getContentType());
 		    break;
 
 		  case HttpURLConnection.HTTP_UNAUTHORIZED:
 		    retry = true;
 		    break;
 
 		  default:
 		    logger.warn(Message.ERROR_RESPONSE, code);
 		    debug(conn);
 		    break;
 		}
 	    } finally {
 		if (conn != null) {
 		    conn.disconnect();
 		}
 	    }
 	} while (retry && nextAuthScheme(conn));
 
 	if (result instanceof JAXBElement) {
 	    result = ((JAXBElement)result).getValue();
 	}
 	logger.debug(Message.STATUS_RESPONSE, result == null ? "null" : result.getClass().getName());
 	if (result instanceof Fault) {
 	    throw new FaultException((Fault)result);
 	} else {
 	    return result;
 	}
     }
 
     // Private
 
     /**
      * Read a SOAP envelope and return the unmarshalled object contents of the body.
      */
     private synchronized Object getSOAPBodyContents(InputStream in, String contentType) throws JAXBException, IOException {
 	Object result = unmarshaller.unmarshal(in);
 	in.close();
 	if (debug != null) {
 	    StringBuffer sb = new StringBuffer("[").append(new Date().toString()).append("] - SOAP Reply:\r\n");
 	    debug.write(sb.toString().getBytes());
 	    synchronized(marshaller) {
 		marshaller.marshal(result, debug);
 	    }
 	    debug.write("\r\n".getBytes());
 	    debug.flush();
 	}
 	if (result instanceof JAXBElement) {
 	    JAXBElement elt = (JAXBElement)result;
 	    if (elt.getValue() instanceof Envelope) {
 		List<Object> list = ((Envelope)elt.getValue()).getBody().getAny();
 		switch(list.size()) {
 		  case 0:
 		    return null;
 		  case 1:
 		    return list.get(0);
 		  default:
 		    return list;
 		}
 	    } else {
 		System.out.println("Unsupported element contents: " + elt.getValue().getClass().getName());
 	    }
 	} else {
 	    System.out.println("Unsupported class: " + result.getClass().getName());
 	}
 	return null;
     }
 
     /**
      * Write information about a non-HTTP_OK reply to the debug log, if applicable.
      */
     private void debug(HttpURLConnection conn) throws IOException {
 	if (debug != null) {
 	    StringBuffer sb = new StringBuffer("[").append(new Date().toString()).append("] - HTTP Reply:\r\n");
 	    debug.write(sb.toString().getBytes());
 	    int len = conn.getHeaderFields().size();
 	    if (len > 0) {
 		sb = new StringBuffer("  ").append("  ").append(conn.getHeaderField(0)).append("\r\n");
 		debug.write(sb.toString().getBytes());
 		for (int i=1; i < len; i++) {
 		    sb = new StringBuffer("  ");
 		    sb.append(conn.getHeaderFieldKey(i)).append(": ").append(conn.getHeaderField(i));
 		    debug.write(sb.toString().getBytes());
 		}
 	    }
 	    len = conn.getContentLength();
 	    if (len > 0) {
 		byte[] buff = new byte[len];
 		for (int offset=0; offset < len; ) {
 		    offset += conn.getErrorStream().read(buff, offset, len - offset);
 		}
 		debug.write(buff);
 		debug.write("\r\n".getBytes());
 	    }
 	    debug.flush();
 	}
     }
 
     /**
      * Set the authentication scheme for the retry, or return false if there are no more options.
      *
      * @throws FailedLoginException if the specified login scheme was already attempted.
      */
     private boolean nextAuthScheme(HttpURLConnection conn) throws FailedLoginException {
 	if (conn == null) {
 	    return false;
 	}
 
 	List<String> authFields = conn.getHeaderFields().get("WWW-Authenticate");
 	if (authFields == null || authFields.size() == 0) {
 	    switch(scheme) {
 	      case NONE:
 		return false;
 	      default:
 		scheme = AuthScheme.NONE;
 		return true;
 	    }
 	}
 
 	boolean basic = false;
 	boolean ntlm = false;
 	boolean kerberos = false;
 	boolean negotiate = false;
 
 	for (String val : authFields) {
 	    if (val.toLowerCase().startsWith("basic")) {
 		basic = true;
 	    } else if (val.equalsIgnoreCase("Negotiate")) {
 		negotiate = true;
 	    } else if (val.equalsIgnoreCase("Kerberos")) {
 		kerberos = true;
 	    } else if (val.equalsIgnoreCase("NTLM")) {
 		ntlm = true;
 	    }
 	}
 
 	if (basic) {
 	    switch(scheme) {
 	      case BASIC:
 		throw new FailedLoginException();
 	      default:
 		scheme = AuthScheme.BASIC;
 		return true;
 	    }
 	}
 	if (negotiate) {
 	    switch(scheme) {
 	      case NONE:
 	      case BASIC:
 		scheme = AuthScheme.NTLM;
 		return true;
 	      case NTLM:
 		scheme = AuthScheme.KERBEROS;
 		return true;
 	      case KERBEROS:
 		throw new FailedLoginException();
 	    }
 	}
 	if (ntlm) {
 	    switch(scheme) {
 	      case NTLM:
 		throw new FailedLoginException();
 	      default:
 		scheme = AuthScheme.NTLM;
 		return true;
 	    }
 	}
 	if (kerberos) {
 	    switch(scheme) {
 	      case KERBEROS:
 		throw new FailedLoginException();
 	      default:
 		scheme = AuthScheme.KERBEROS;
 		return true;
 	    }
 	}
 	return false;
     }
 }
