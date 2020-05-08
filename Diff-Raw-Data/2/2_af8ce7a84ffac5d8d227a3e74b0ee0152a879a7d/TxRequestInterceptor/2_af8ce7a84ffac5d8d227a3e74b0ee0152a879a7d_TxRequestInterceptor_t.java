 package org.jboss.blacktie.jatmibroker.core.tx;
 
 import java.util.Properties;
 
 import org.apache.log4j.LogManager;
 import org.apache.log4j.Logger;
 import org.jboss.blacktie.jatmibroker.core.conf.AtmiBrokerClientXML;
 import org.jboss.blacktie.jatmibroker.core.transport.OrbManagement;
 import org.jboss.blacktie.jatmibroker.jab.JABTransaction;
 import org.omg.CORBA.BAD_PARAM;
 import org.omg.CORBA.LocalObject;
 import org.omg.CORBA.SystemException;
 import org.omg.CosTransactions.Control;
 import org.omg.IOP.Codec;
 import org.omg.IOP.ServiceContext;
 import org.omg.IOP.TaggedComponent;
 import org.omg.PortableInterceptor.ClientRequestInfo;
 import org.omg.PortableInterceptor.ClientRequestInterceptor;
 import org.omg.PortableInterceptor.ServerRequestInfo;
 import org.omg.PortableInterceptor.ServerRequestInterceptor;
 
 public class TxRequestInterceptor extends LocalObject implements
 		ClientRequestInterceptor, ServerRequestInterceptor {
 	private static final Logger log = LogManager
 			.getLogger(TxRequestInterceptor.class);
 
 	// ORB request service context id for tagging service contexts (as
 	// transactional)
 	// must match up with C++ implementation (see TxInterceptor.h)
 	private static final int tx_context_id = 0xabcd;
 
 	private Codec codec;
 	private String name;
 
 	private OrbManagement orbManagement;
 
 	public TxRequestInterceptor(OrbManagement orbManagement, String name,
 			Codec codec) {
 		this.orbManagement = orbManagement;
 		this.codec = codec;
 		this.name = name;
 		try {
 			AtmiBrokerClientXML xml = new AtmiBrokerClientXML();
 			Properties properties = null;
 			properties = xml.getProperties(null);
 
 			// orbManagement = new OrbManagement(properties, false);
 		} catch (Exception e) {
 			throw new RuntimeException("Could not load properties", e);
 		}
 	}
 
 	//
 	// Interceptor operations
 	//
 
 	public String name() {
 		return name;
 	}
 
 	public void destroy() {
 	}
 
 	private String extract_ior_from_context(ServerRequestInfo ri) {
 		return null;
 	}
 
 	private String extract_ior_from_context(ClientRequestInfo ri) {
 		return null;
 	}
 
 	public void send_request(ClientRequestInfo ri) {
 		if (isTransactional(ri)) {
 			log.trace("send_request: " + ri.operation());
 			JABTransaction curr = JABTransaction.current();
 
 			if (curr != null) {
 				log.trace("adding tx");
 				Control control = curr.getControl();
				CORBA::String_var ior = orbManagement.getOrb().object_to_string(control);
 				log.trace("tx ior: " + ior);
 				ri.add_request_service_context(new ServiceContext(
 						tx_context_id, ior.getBytes()), false);
 			}
 		}
 	}
 
 	public void send_poll(ClientRequestInfo ri) {
 		log.trace("send_poll: " + ri.operation());
 	}
 
 	public void receive_reply(ClientRequestInfo ri) {
 		log.trace("receive_reply: " + ri.operation());
 	}
 
 	public void receive_exception(ClientRequestInfo ri) {
 		log.trace("receive_exception: " + ri.operation());
 	}
 
 	public void receive_other(ClientRequestInfo ri) {
 		log.trace("receive_other: " + ri.operation());
 	}
 
 	//
 	// ServerRequestInterceptor operations
 	//
 
 	public void receive_request_service_contexts(ServerRequestInfo ri) {
 		log.trace("receive_request_service_contexts: " + ri.operation());
 
 		try {
 			ServiceContext serviceContext = ri
 					.get_request_service_context(tx_context_id);
 			byte[] data = serviceContext.context_data;
 			log.trace("receive_request_service_contexts: data: " + data);
 		} catch (BAD_PARAM e) {
 			log.debug("receive_request_service_contexts: not transactional");
 			// not a transactional request - ignore
 		} catch (SystemException e) {
 			log.error("receive_request_service_contexts error: ", e);
 		}
 	}
 
 	public void receive_request(ServerRequestInfo ri) {
 		log.trace("receive_request: " + ri.operation());
 	}
 
 	public void send_reply(ServerRequestInfo ri) {
 		log.trace("send_reply: " + ri.operation());
 	}
 
 	public void send_exception(ServerRequestInfo ri) {
 		log.trace("send_exception " + ri.operation() + " exception: "
 				+ ri.sending_exception());
 	}
 
 	public void send_other(ServerRequestInfo ri) {
 		log.trace("send_other: " + ri.operation());
 	}
 
 	private boolean isTransactional(ClientRequestInfo ri) {
 		try {
 			TaggedComponent comp = ri
 					.get_effective_component(TxIORInterceptor.TAG_OTS_POLICY);
 			log.debug("isTransactional: " + ri.operation());
 
 			return true;
 		} catch (SystemException e) {
 			// should be BAD_PARAM, minor code 25
 		} catch (Throwable t) {
 			log.warn("isTransactional: unexpected ex: " + t.getMessage());
 		}
 
 		return false;
 	}
 }
