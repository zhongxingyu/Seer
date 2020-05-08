 package org.techhouse.shirts.display.web;
 
 import java.net.URL;
 
 import org.apache.wicket.Application;
 import org.apache.wicket.IConverterLocator;
 import org.apache.wicket.Page;
 import org.apache.wicket.authentication.AuthenticatedWebApplication;
 import org.apache.wicket.authentication.AuthenticatedWebSession;
 import org.apache.wicket.markup.html.WebPage;
 import org.apache.wicket.protocol.http.WebRequestCycleProcessor;
 import org.apache.wicket.protocol.http.request.CryptedUrlWebRequestCodingStrategy;
 import org.apache.wicket.protocol.http.request.WebRequestCodingStrategy;
 import org.apache.wicket.request.IRequestCodingStrategy;
 import org.apache.wicket.request.IRequestCycleProcessor;
 import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
 import org.apache.wicket.util.convert.ConverterLocator;
 import org.apache.wicket.util.crypt.CachingSunJceCryptFactory;
 import org.joda.time.DateTimeZone;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.core.token.Sha512DigestUtils;
 import org.techhouse.shirts.display.web.converters.URLConverter;
 import org.techhouse.shirts.display.web.pages.BallotPage;
 import org.techhouse.shirts.display.web.pages.StatisticsPage;
 import org.techhouse.shirts.display.web.security.InterfaceAuthorizationStrategy;
 import org.techhouse.shirts.display.web.security.SignInPage;
 import org.techhouse.shirts.display.web.security.WicketSession;
 import org.techhouse.shirts.service.DeadlineService;
 
 public class WicketApplication extends AuthenticatedWebApplication {
 
 	@Autowired
 	private DeadlineService deadlineService;
 
 	@Override
 	public Class<? extends Page> getHomePage() {
 		if (deadlineService.hasDeadlinePassed()) {
 			return StatisticsPage.class;
 		} else {
 			return BallotPage.class;
 		}
 	}
 
 	@Override
 	protected IConverterLocator newConverterLocator() {
 		final ConverterLocator converterLocator = new ConverterLocator();
 
 		converterLocator.set(URL.class, new URLConverter());
 
 		return converterLocator;
 	}
 
 	@Override
 	public void init() {
 		addComponentInstantiationListener(new SpringComponentInjector(this));
 
 		getApplicationSettings().setPageExpiredErrorPage(getHomePage());
 
 		if (!isDevelopment()) {
 			getSecuritySettings().setAuthorizationStrategy(new InterfaceAuthorizationStrategy(deadlineService));
 		}
 
		if (/* /!isDevelopment()/ */true/**/) {
			byte[] secretKeyBytes = Sha512DigestUtils.shaHex("omarh4ste3th" + getClass().getName()).getBytes();
			String secretKey = new String(secretKeyBytes, secretKeyBytes.length - 17, 16);
			getSecuritySettings().setCryptFactory(new CachingSunJceCryptFactory(secretKey));
		}
 	}
 
 	@Override
 	protected IRequestCycleProcessor newRequestCycleProcessor() {
 		return new WebRequestCycleProcessor() {
 			protected IRequestCodingStrategy newRequestCodingStrategy() {
 				return new CryptedUrlWebRequestCodingStrategy(new WebRequestCodingStrategy());
 			}
 		};
 	}
 
 	public static WicketApplication get() {
 		return (WicketApplication) AuthenticatedWebApplication.get();
 	}
 
 	public boolean isDevelopment() {
 		return getConfigurationType().equals(Application.DEVELOPMENT);
 	}
 
 	@Override
 	protected Class<? extends AuthenticatedWebSession> getWebSessionClass() {
 		return WicketSession.class;
 	}
 
 	@Override
 	public Class<? extends WebPage> getSignInPageClass() {
 		return SignInPage.class;
 	}
 	
 	public DateTimeZone getDefaultDateTimeZone(){
 		return DateTimeZone.forID("America/New_York");
 	}
 }
