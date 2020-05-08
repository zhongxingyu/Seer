 /**
  * Copyright (C) 2013, Moss Computing Inc.
  *
  * This file is part of rpcutil.
  *
  * rpcutil is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2, or (at your option)
  * any later version.
  *
  * rpcutil is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with rpcutil; see the file COPYING.  If not, write to the
  * Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
  * 02110-1301 USA.
  *
  * Linking this library statically or dynamically with other modules is
  * making a combined work based on this library.  Thus, the terms and
  * conditions of the GNU General Public License cover the whole
  * combination.
  *
  * As a special exception, the copyright holders of this library give you
  * permission to link this library with independent modules to produce an
  * executable, regardless of the license terms of these independent
  * modules, and to copy and distribute the resulting executable under
  * terms of your choice, provided that you also meet, for each linked
  * independent module, the terms and conditions of the license of that
  * module.  An independent module is a module which is not derived from
  * or based on this library.  If you modify this library, you may extend
  * this exception to your version of the library, but you are not
  * obligated to do so.  If you do not wish to do so, delete this
  * exception statement from your version.
  */
 package com.moss.rpcutil.proxy.jaxws;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.xml.namespace.QName;
 import javax.xml.soap.SOAPMessage;
 import javax.xml.ws.Service;
 import javax.xml.ws.handler.Handler;
 import javax.xml.ws.handler.HandlerResolver;
 import javax.xml.ws.handler.MessageContext;
 import javax.xml.ws.handler.PortInfo;
 import javax.xml.ws.handler.soap.SOAPHandler;
 import javax.xml.ws.handler.soap.SOAPMessageContext;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
import com.cmaxinc.jaxbhelper.JAXBHelper;
 import com.moss.rpcutil.proxy.ProxyProvider;
 
 public final class JAXWSProxyProvider implements ProxyProvider {
 	
 	private final Log log;
 	private final Map<Class, QName> registry;
 	private final Map<String, Object> cache;
 	
 	public JAXWSProxyProvider() {
 		this.log = LogFactory.getLog(this.getClass());
 		registry = new HashMap<Class, QName>();
 		cache = new HashMap<String, Object>();
 	}
 	
 	public synchronized JAXWSProxyProvider register(Class iface, QName type) {
 		registry.put(iface, type);
 		return this;
 	}
 
 	public synchronized <T> T getProxy(Class<T> iface, String url) {
 		
 		QName type = registry.get(iface);
 		
 		if (type == null) {
 			throw new RuntimeException("Service type not registered: " + iface);
 		}
 		
 		T proxy = (T) cache.get(url);
 		if (proxy == null) {
 			
 			Service service; 
 			try {
 				String wsdlLocation = url;
 				if (!wsdlLocation.endsWith("?wsdl")) {
 					wsdlLocation += "?wsdl";
 				}
 				service = Service.create(new URL(wsdlLocation), type);
 			}
 			catch (Exception ex) {
 				throw new RuntimeException(ex);
 			}
 			
 			if(log.isDebugEnabled()){
 				// SOAP LOGGING STUFF
 				service.setHandlerResolver(new HandlerResolver(){
 					public List<Handler> getHandlerChain(PortInfo arg0) {
 						List<Handler> handlers = new LinkedList<Handler>();
 						handlers.add(new SOAPLoggingHandler(log));
 						return handlers;
 					}
 				});
 			}
 			
 			proxy = service.getPort(iface);
 			
 //			BindingProvider bp = (BindingProvider) proxy;
 //			
 //			bp.getRequestContext().put(
 //				MessageContext.HTTP_REQUEST_HEADERS, 
 //				Collections.singletonMap("Content-Type", Arrays.asList(new String[]{"application/soap+xml", "charset=utf-8"}))
 //			);
 			
 			cache.put(url, proxy);
 		}
 		
 		return proxy;
 	}
 	
 	/*
 	 * This simple SOAPHandler will output the contents of incoming
 	 * and outgoing messages.
 	 */
 	public class SOAPLoggingHandler implements SOAPHandler<SOAPMessageContext> {
 
 		private Log log;
 
 		public SOAPLoggingHandler(Log log) {
 			super();
 			this.log = log;
 		}
 
 		public Set<QName> getHeaders() {
 			return null;
 		}
 
 		public boolean handleMessage(SOAPMessageContext smc) {
 			logToSystemOut(smc);
 			return true;
 		}
 
 		public boolean handleFault(SOAPMessageContext smc) {
 			logToSystemOut(smc);
 			return true;
 		}
 
 		// nothing to clean up
 		public void close(MessageContext messageContext) {
 		}
 
 		/*
 		 * Check the MESSAGE_OUTBOUND_PROPERTY in the context
 		 * to see if this is an outgoing or incoming message.
 		 * Write a brief message to the print stream and
 		 * output the message. The writeTo() method can throw
 		 * SOAPException or IOException
 		 */
 		private void logToSystemOut(SOAPMessageContext smc) {
 			if(!log.isDebugEnabled()) return;
 
 
 			try {
 				final Boolean outboundProperty = (Boolean) smc.get (MessageContext.MESSAGE_OUTBOUND_PROPERTY);
 				final ByteArrayOutputStream messageBytes = new ByteArrayOutputStream();
 				SOAPMessage message = smc.getMessage();
 				message.writeTo(messageBytes);
 				messageBytes.close();
 				
 
 				new Thread(){
 					@Override
 					public void run() {
 						try {
 							
 							if (outboundProperty.booleanValue()) {
 								log.debug("\nOutbound message:");
 							} else {
 								log.debug("\nInbound message:");
 							}
 							
 							ByteArrayOutputStream formattedBytes = new ByteArrayOutputStream();
 							setPriority(MIN_PRIORITY);
 							JAXBHelper.beautify(new ByteArrayInputStream(messageBytes.toByteArray()), formattedBytes);
 							formattedBytes.close();
 							log.debug(new String(formattedBytes.toByteArray()));
 							log.debug(""); 
 						} catch (Exception e) {
 							throw new RuntimeException(e);
 						}
 					}
 				}.start();
 
 				// just to add a newline
 			} catch (Exception e) {
 				log.debug("Exception in handler: " + e);
 			}
 		}
 	}
 }
