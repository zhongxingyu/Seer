 package com.robonobo.midas;
 
 import javax.servlet.ServletContext;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.web.context.ServletContextAware;
 
 import com.robonobo.common.concurrent.SafetyNet;
 import com.robonobo.common.util.ExceptionEvent;
 import com.robonobo.common.util.ExceptionListener;
 import com.robonobo.remote.service.MailService;
 import com.robonobo.remote.service.MailServiceImpl;
 
 @Configuration
 public class AppConfig implements ServletContextAware, InitializingBean {
 	private ServletContext sc;
 	Log log = LogFactory.getLog(getClass());
 
 	@Bean
 	public RemoteMidasService remoteMidas() throws Exception {
 		String url = sc.getInitParameter("remoteMidasListenUrl");
 		String sekrit = sc.getInitParameter("remoteMidasSecret");
 		return new RemoteMidasService(url, sekrit);
 	}
 
 	@Bean
 	public MailService mail() {
 		String smtpServer = sc.getInitParameter("smtpServer");
		return new MailServiceImpl(smtpServer, "robonobo.com");
 	}
 
 	public String getInitParam(String name) {
 		return sc.getInitParameter(name);
 	}
 
 	@Override
 	public void setServletContext(ServletContext servC) {
 		sc = servC;
 	}
 	
 	@Override
 	public void afterPropertiesSet() throws Exception {
 		// Make sure all exceptions from catchingrunnables get logged
 		SafetyNet.addListener(new ExceptionListener() {
 			public void onException(ExceptionEvent e) {
 				log.error("Uncaught exception", e.getException());
 			}
 		});
 	}
 }
