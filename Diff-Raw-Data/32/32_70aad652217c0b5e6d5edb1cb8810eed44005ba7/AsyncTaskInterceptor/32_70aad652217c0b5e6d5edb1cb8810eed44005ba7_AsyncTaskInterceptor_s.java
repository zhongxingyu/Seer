 /**
  * 
  */
 package org.cotrix.web.common.server.async;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Method;
 import java.util.concurrent.Callable;
 
 import javax.inject.Inject;
 import javax.interceptor.AroundInvoke;
 import javax.interceptor.Interceptor;
 import javax.interceptor.InvocationContext;
 
 import org.cotrix.common.async.ExecutionService;
 import org.cotrix.common.async.ReportingFuture;
 import org.cotrix.web.common.shared.async.AsyncOutput;
 import org.cotrix.web.common.shared.async.AsyncTask;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  * 
  */
 @ContainsAsyncTask
 @Interceptor
 public class AsyncTaskInterceptor {
 
 	protected Logger logger = LoggerFactory.getLogger(AsyncTaskInterceptor.class);
 	
 	@Inject
 	private ExecutionService service;
 	
 	@Inject
 	private ProgressService progressService;
 
 	@AroundInvoke
 	public Object manageTask(final InvocationContext ctx) throws Exception {
 		
 		Method method = ctx.getMethod();
 		
 		logger.trace("manageAsyncTask method: {}", method.getName());
 
 		Annotation asyncTaskAnnotation = getAsyncTaskAnnotation(method);
 		
 		logger.trace("asyncTaskAnnotation: {}", asyncTaskAnnotation);
 
 		if (asyncTaskAnnotation ==null)
 			return ctx.proceed();
 
 		//TODO check the return type
 		
		logger.trace("executing aync");
 		ReportingFuture<AsyncOutput<?>> future = service.execute(new Callable<AsyncOutput<?>>() {
 			@Override
 			public AsyncOutput<?> call() throws Exception {
 				return (AsyncOutput<?>) ctx.proceed();
 			}
 		});
 		
 		String progressToken = progressService.monitorize(future);
 		
 		return new AsyncTask<>(progressToken);
 		
 	}
 
 	//helper
 	
 	private Annotation getAsyncTaskAnnotation(Method m) {
 		
 		for (Annotation a : m.getAnnotations())
 			if (a.annotationType().isAssignableFrom(Async.class))
 			return a;
 		
 		return null;
 	}
 }
