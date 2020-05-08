 package evymind.vapor.app;
 
 import java.io.IOException;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import evyframework.common.io.Resource;
 import evymind.vapor.service.ServiceHandler;
 
 public class AppConfigConfiguration extends AbstractConfiguration {
 
 	private static final Logger log = LoggerFactory.getLogger(AppConfigConfiguration.class);
 
 	@Override
 	public void preConfigure(AppContext context) throws Exception {
 		Resource appConfig = findAppDescriptorResource(context);
 		if (appConfig != null) {
 			context.getMetadata().setAppDescriptorContext(appConfig);
 		}
 	}
 
 	protected Resource findAppDescriptorResource(AppContext context) throws IOException {
 		String descriptor = context.getDescriptor();
 		if (descriptor != null) {
			Resource res = context.newResource(descriptor);
			if (res.exists() && !res.getFile().isDirectory())
				return res;
 		}
 
 		Resource appres = context.getBaseResource();
 		if (appres != null && appres.exists() && appres.getFile().isDirectory()) {
 			// do app config file
 			Resource appconfig = appres.createRelative("/app.yml");
 			if (appconfig.exists())
 				return appconfig;
 			if (log.isDebugEnabled())
				log.debug("No app.yml ("+appconfig.toString()+") in " + context.getSar() + ". Serving default/dynamic services only");
 		}
 		return null;
 	}
 
 	@Override
 	public void configure(AppContext context) throws Exception {
 		// cannot configure if the context is already started
 		if (context.isStarted()) {
 			log.debug("Cannot configure app after it is started");
 			return;
 		}
 		context.getMetadata().addDescriptorProcessor(new AppConfigDescriptorProcessor());
 	}
 
 	@Override
 	public void deconfigure(AppContext context) throws Exception {
 		ServiceHandler serviceHandler = context.getServiceHandler();
 		serviceHandler.setServices(null);
 
 		context.removeAllListeners();
 	}
 
 }
