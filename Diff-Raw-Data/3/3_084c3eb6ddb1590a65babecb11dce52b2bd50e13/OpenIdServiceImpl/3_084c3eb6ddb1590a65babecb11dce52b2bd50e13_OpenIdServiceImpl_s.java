 package uk.co.tfd.sm.authn.openid;
 
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.felix.scr.annotations.Activate;
 import org.apache.felix.scr.annotations.Component;
 import org.apache.felix.scr.annotations.Modified;
 import org.apache.felix.scr.annotations.Reference;
 import org.apache.felix.scr.annotations.Service;
 import org.openid4java.association.AssociationException;
 import org.openid4java.consumer.ConsumerException;
 import org.openid4java.consumer.ConsumerManager;
 import org.openid4java.consumer.VerificationResult;
 import org.openid4java.discovery.DiscoveryException;
 import org.openid4java.discovery.DiscoveryInformation;
 import org.openid4java.discovery.Identifier;
 import org.openid4java.message.AuthRequest;
 import org.openid4java.message.MessageException;
 import org.openid4java.message.ParameterList;
 import org.sakaiproject.nakamura.api.lite.StorageClientUtils;
 import org.sakaiproject.nakamura.api.memory.Cache;
 import org.sakaiproject.nakamura.api.memory.CacheManagerService;
 import org.sakaiproject.nakamura.api.memory.CacheScope;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 @Component(immediate = true, metatype = true)
 @Service(value = OpenIdService.class)
 public class OpenIdServiceImpl implements OpenIdService {
 
 	private static final Logger LOGGER = LoggerFactory
 			.getLogger(OpenIdServiceImpl.class);
 
 	private ConsumerManager consumerManager;
 
 	@Reference
 	protected CacheManagerService cacheManagerService;
 
 	private Cache<DiscoveryInformation> discoveryCache;
 
 	
 	@Activate
 	protected void activate(Map<String, Object> properties)
 			throws ConsumerException {
 		modified(properties);
 	}
 
 	@Modified
 	protected void modified(Map<String, Object> properties) throws ConsumerException {
 		consumerManager = new ConsumerManager();
 		discoveryCache = cacheManagerService.getCache(
 				this.getClass().getName(), CacheScope.INSTANCE);
 		
 	}
 
 	@Override
 	public String getIdentity(HttpServletRequest request) {
 		// only consider OpenId if the request is a get and there is a _oidk
 		// parameter in the url.
 		if (request.getMethod().indexOf("_GET_POST_") > 0) {
 			String key = request.getParameter("_oidk");
 			if (key != null && discoveryCache.containsKey(key)) {
 				// NB OpenID redirects must come back to the same server.
 				// When configuring a Load balancer make certain that happens.
 				// The discovery information may only be used once.
 				DiscoveryInformation discoveryInformation = discoveryCache
 						.get(key);
 				discoveryCache.remove(key);
 
 				ParameterList response = new ParameterList(
 						request.getParameterMap());
 				StringBuffer receivingURL = request.getRequestURL();
 				String queryString = request.getQueryString();
 				if (queryString != null && queryString.length() > 0)
 					receivingURL.append("?").append(request.getQueryString());
 
 				VerificationResult verification;
 				try {
 					verification = consumerManager.verify(
 							receivingURL.toString(), response,
 							discoveryInformation);
 					Identifier verified = verification.getVerifiedId();
 					if (verified != null) {
 						// could extract more information at this point, like an
 						// OAuth token
 						// to be stored on the users authorizable, or somewhere
 						// else.
 						return verified.getIdentifier();
 					}
 				} catch (MessageException e) {
 					LOGGER.error(e.getMessage(), e);
 				} catch (DiscoveryException e) {
 					LOGGER.error(e.getMessage(), e);
 				} catch (AssociationException e) {
 					LOGGER.error(e.getMessage(), e);
 				}
 			}
 		}
 		return null;
 	}
 
 	public String getAuthRedirectUrl(String userSuppliedString,
 			String returnToUrl) {
 		try {
 			List<?> discoveries = consumerManager.discover(userSuppliedString);
 			DiscoveryInformation discoveryInformation = consumerManager
 					.associate(discoveries);
 
 			String key = StorageClientUtils.getUuid();
 			discoveryCache.put(key, discoveryInformation);
 
 			if (returnToUrl.contains("?")) {
 				returnToUrl = returnToUrl + "&_oidk" + key;
 			} else {
 				returnToUrl = returnToUrl + "?_oidk" + key;
 			}
 			AuthRequest authReq = consumerManager.authenticate(
 					discoveryInformation, returnToUrl);
 			// Add extensions here, eg email or request for OAuth token.
 			return authReq.getDestinationUrl(true);
 		} catch (DiscoveryException e) {
 			LOGGER.error(e.getMessage(), e);
 		} catch (MessageException e) {
 			LOGGER.error(e.getMessage(), e);
 		} catch (ConsumerException e) {
 			LOGGER.error(e.getMessage(), e);
 		}
 		return null;
 	}
 
 }
