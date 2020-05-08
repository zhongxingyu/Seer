 package grisu.control.util;
 
 import grisu.control.GrisuUserDetails;
 import grisu.jcommons.utils.VariousStringHelpers;
 import grisu.jcommons.utils.tid.TidGenerator;
 import grisu.model.dto.DtoStringList;
 import grisu.settings.ServerPropertiesManager;
 
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import javax.xml.ws.WebServiceContext;
 import javax.xml.ws.handler.MessageContext;
 
 import org.aopalliance.intercept.MethodInterceptor;
 import org.aopalliance.intercept.MethodInvocation;
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.slf4j.MDC;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.context.SecurityContext;
 import org.springframework.security.core.context.SecurityContextHolder;
 
 import com.google.common.collect.Lists;
 
 public class AuditAdvice implements MethodInterceptor {
 
 	private final WebServiceContext wsContext = new org.apache.cxf.jaxws.context.WebServiceContextImpl();;
 
 	public static AtomicInteger numberOfOpenMethodCalls = new AtomicInteger(0);
 
 	static final Logger myLogger = LoggerFactory.getLogger(AuditAdvice.class
 			.getName());
 
 	static final TidGenerator tidGenerator = ServerPropertiesManager
 			.getTidGenerator();
 
 	public Object invoke(MethodInvocation methodInvocation) throws Throwable {
 
 		final String method = methodInvocation.getMethod().getName();
 		String dn = null;
 		final Object[] argOs = methodInvocation.getArguments();
 		String argList = "NO_ARGS";
 		if ((argOs != null) && (argOs.length > 0) && !"login".equals(method)) {
 			final String[] args = new String[argOs.length];
 			for (int i = 0; i < args.length; i++) {
 				try {
 					if (argOs[i] == null) {
 						args[i] = "null_arg";
 					} else {
 						args[i] = (argOs[i]).toString();
 					}
 				} catch (final Exception e) {
 					args[i] = "Error serializing object";
 				}
 			}
 			argList = StringUtils.join(args, ";");
 			argList = argList.replace("\n", " ");
 		}
 
 		MessageContext mContext = wsContext.getMessageContext();
 
 		Map o = (Map) mContext.get(MessageContext.HTTP_REQUEST_HEADERS);
 
 		List session_id = (List) o.get("X-client-session-id");
 		List client = (List) o.get("X-grisu-client");
 		List command_id = (List) o.get("X-command-id");
 
 		if ((session_id == null) || (session_id.size() == 0)) {
 			session_id = Lists.newArrayList("n/a");
 		}
 
 		if ((client == null) || (client.size() == 0)) {
 			client = Lists.newArrayList("n/a");
 		}
 
 		if ((command_id == null) || (command_id.size() == 0)) {
 			command_id = Lists.newArrayList("n/a");
 		}
 
 		final SecurityContext securityContext = SecurityContextHolder
 				.getContext();
 		final Authentication authentication = securityContext
 				.getAuthentication();
 
 		if (authentication != null) {
 			final Object principal = authentication.getPrincipal();
 			if (principal instanceof GrisuUserDetails) {
 				final GrisuUserDetails gud = (GrisuUserDetails) principal;
 				dn = gud.fetchCredential().getDn();
 				MDC.put("dn", dn);
 			}
 		}
 
 
 		final Date start = new Date();
 
 		final String tid = tidGenerator.getTid();

 		int number = numberOfOpenMethodCalls.incrementAndGet();
 
 		String un = VariousStringHelpers.getCN(dn);
		MDC.put("tid", tid);
 		if (StringUtils.isBlank(un)) {
 			MDC.put("user", "n/a");
 		} else {
 			MDC.put("user", un);
 		}
 
		MDC.put("csid", (String) session_id.get(0));
		MDC.put("cmdid", (String) command_id.get(0));
		MDC.put("client", (String) client.get(0));
 
 		myLogger.info(
 				"Entering method: method=[{}] arguments=[{}] time=[{}] open_calls=[{}]",
 				new Object[] { method, argList,
 						start.getTime(), number });
 
 
 		Object result = null;
 		try {
 			result = methodInvocation.proceed();
 		} catch (final Throwable t) {
 			number = numberOfOpenMethodCalls.decrementAndGet();
 			final Date end = new Date();
 			final long duration = end.getTime() - start.getTime();
 			myLogger.info(
 					"Finishing method: method=[{}] [failed] arguments=[{}] Exception=[{}] Message=[{}] time=[{}] duration=[{}] open_calls=[{}]",
 					new Object[] { method, argList,
 							t.getClass().getSimpleName(),
 							t.getLocalizedMessage(), end.getTime(), duration,
 							number });
 			throw t;
 		}
 
 		number = numberOfOpenMethodCalls.decrementAndGet();
 
 		final Date end = new Date();
 
 		final long duration = end.getTime() - start.getTime();
 
 		String resultString = "n/a";
 		if (result instanceof String) {
 			resultString = (String) result;
 		} else if (result instanceof Integer) {
 			resultString = ((Integer) result).toString();
 		} else if (result instanceof Boolean) {
 			resultString = ((Boolean) result).toString();
 		} else if (result instanceof Long) {
 			resultString = ((Long) result).toString();
 		} else if (result instanceof DtoStringList) {
 			resultString = StringUtils.join(((DtoStringList) result).asArray(),
 					";");
 		}
 
 		resultString = resultString.replace("\n", " ");
 
 		myLogger.info(
 				"Finishing method: method=[{}] arguments=[{}] result=[{}] time=[{}] duration=[{}] open_calls=[{}]",
 				new Object[] { method, argList, resultString, end.getTime(),
 						duration, number });
 
 		MDC.remove("tid");
 		MDC.remove("user");
 
 		return result;
 	}
 
 }
