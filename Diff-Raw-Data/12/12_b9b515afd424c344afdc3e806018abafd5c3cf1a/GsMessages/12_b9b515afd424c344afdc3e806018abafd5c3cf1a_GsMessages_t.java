 package org.genericsystem.myadmin.util;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Objects;
 import javax.enterprise.context.SessionScoped;
 import javax.enterprise.event.Observes;
 import javax.faces.context.FacesContext;
 import javax.faces.event.PhaseEvent;
 import javax.inject.Inject;
import org.jboss.seam.faces.event.qualifier.After;
import org.jboss.seam.faces.event.qualifier.RestoreView;
 import org.jboss.seam.international.status.Message;
 import org.jboss.seam.international.status.MessageFactory;
 import org.jboss.seam.international.status.Messages;
 import org.jboss.seam.international.status.builder.BundleKey;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 @SessionScoped
 public class GsMessages implements Serializable {
 
 	private static final long serialVersionUID = 7666964394295933934L;
 	protected static Logger log = LoggerFactory.getLogger(GsMessages.class);
 
 	public static String toString(Throwable throwable) {
 		String s = "";
 		while (throwable != null) {
 			s += throwable + "\n";
 			for (Object object : throwable.getStackTrace())
 				s += Objects.toString(object) + "\n";
 			throwable = throwable.getCause();
 			s += "\n";
 		}
 		return s;
 	}
 
 	@Inject
 	FacesContext context;
 
 	@Inject
 	Messages messages;
 
 	@Inject
 	MessageFactory factory;
 
 	private static final String MESSAGES_BUNDLE_NAME = "/bundles/messages";
 
 	private static BundleKey bundleKey(String key) {
 		return new BundleKey(MESSAGES_BUNDLE_NAME, key);
 	}
 
 	private List<Message> messagesToRedirect = new ArrayList<>();
 
	public void restoreMessages(@Observes @RestoreView @After PhaseEvent e) {
		for (Message message : messagesToRedirect) {
 			messages.add(message);
			log.info("ZZZZZZZZZZ" + message.getText());
		}
 		messagesToRedirect.clear();
 	}
 
 	public void redirectError(String key, Object... params) {
 		Message message = factory.error(bundleKey(key), params).build();
 		log.error(message.getText());
 		messagesToRedirect.add(message);
 	}
 
 	public void redirectWarn(String key, Object... params) {
 		Message message = factory.warn(bundleKey(key), params).build();
 		log.warn(message.getText());
 		messagesToRedirect.add(message);
 	}
 
 	public void redirectInfo(String key, Object... params) {
 		Message message = factory.info(bundleKey(key), params).build();
 		log.info(message.getText());
 		messagesToRedirect.add(message);
 	}
 
 	// public void redirectStringError(String simpleMessage, Object... params) {
 	// Message message = factory.error(simpleMessage, params).build();
 	// log.info(message.getDetail());
 	// messagesToRedirect.add(message);
 	// }
 
 	public void redirectThrowable(Throwable t) {
 		Message message = factory.error(t.toString()).build();
 		log.error("\n" + toString(t));
 		messagesToRedirect.add(message);
 	}
 
 	public String getMessage(String key, Object... params) {
 		return factory.info(bundleKey(key), params).build().getText();
 	}
 
 	public void info(String key, Object... params) {
 		Message message = factory.info(bundleKey(key), params).build();
 		log.info(message.getText());
 		messages.add(message);
 	}
 
 	public void warn(String key, Object... params) {
 		Message message = factory.warn(bundleKey(key), params).build();
 		log.warn(message.getText());
 		messages.add(message);
 	}
 
 	public void error(String key, Object... params) {
 		Message message = factory.error(bundleKey(key), params).build();
 		log.error(message.getText());
 		messages.add(message);
 	}
 }
