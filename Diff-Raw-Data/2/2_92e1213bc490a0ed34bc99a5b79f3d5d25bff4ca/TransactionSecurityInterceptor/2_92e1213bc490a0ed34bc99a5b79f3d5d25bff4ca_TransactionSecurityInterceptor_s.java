 package org.nju.artemis.aejb.component.interceptors;
 
 import org.jboss.invocation.Interceptor;
 import org.jboss.invocation.InterceptorContext;
 import org.jboss.logging.Logger;
 import org.nju.artemis.aejb.component.AcContainer;
 import org.nju.artemis.aejb.deployment.processors.TransactionManager;
 import org.nju.artemis.aejb.evolution.DuService;
 import org.nju.artemis.aejb.evolution.protocols.Protocol;
 
 /**
  * This interceptor checks transaction security using specified protocol.<p>Instance level
  * 
  * @author <a href="mailto:wangjue1199@gmail.com">Jason</a>
  */
 public class TransactionSecurityInterceptor implements Interceptor {
 	Logger log = Logger.getLogger(TransactionSecurityInterceptor.class);
 	
 	private final AcContainer container;
 	
 	public TransactionSecurityInterceptor(AcContainer container) {
 		this.container = container;
 	}
 	
 	@Override
 	public Object processInvocation(InterceptorContext context) throws Exception {
 		log.debug("TransactionSecurityInterceptor: start process invocation");
 		String param0 = (String) context.getParameters()[0];
 		String[] splits = param0.split("/");
 		if(splits.length != 2) {
			context.proceed();
 		}
 		final String transactionName = splits[0];
 		final String objectId = splits[1];
 		final String targetName = (String) context.getContextData().get("aejbName");
 		final String protocol = container.getEvolutionStatistics().getProtocolByBeanName(targetName);
 		log.debug("transactionName = " + transactionName + ",objectId = " + objectId + ",protocol = " + protocol);
 		if(protocol == null || isValidProtocolName(protocol) == false)
 			return context.proceed();
 		boolean ts = checkTransactionSecurity(targetName, objectId, container.getTransactionManager(transactionName), protocol);
 		log.debug("checkTransactionSecurity = " + ts);
 		context.putPrivateData(Boolean.class, ts);
 		log.debug("TransactionSecurityInterceptor: stop process invocation");
 		return context.proceed();
 	}
 
 	private boolean checkTransactionSecurity(String targetName,	String objectId, TransactionManager transactionManager,	String protocolName) {
 		Protocol protocol = DuService.getProtocol(protocolName);
 		if(protocol != null) {
 			return protocol.checkTransactionSecurity(targetName, objectId, transactionManager);
 		}
 		return false;
 	}
 
 	// more protocol names
 	private boolean isValidProtocolName(String protocol) {
 		return protocol.equals("tranquility") || protocol.equals("quiescence");
 	}
 }
