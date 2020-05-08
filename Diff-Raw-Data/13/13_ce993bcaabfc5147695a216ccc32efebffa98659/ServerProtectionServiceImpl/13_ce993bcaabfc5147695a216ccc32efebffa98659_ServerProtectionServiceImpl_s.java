 package uk.co.tfd.sm.http;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.security.InvalidKeyException;
 import java.security.Key;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import javax.crypto.Mac;
 import javax.crypto.spec.SecretKeySpec;
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.codec.binary.Base64;
 import org.apache.commons.lang.StringUtils;
 import org.apache.felix.scr.annotations.Activate;
 import org.apache.felix.scr.annotations.Component;
 import org.apache.felix.scr.annotations.Modified;
 import org.apache.felix.scr.annotations.Property;
 import org.apache.felix.scr.annotations.Service;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.co.tfd.sm.api.http.CSRFProtectionService;
 import uk.co.tfd.sm.api.http.IdentityRedirectService;
 import uk.co.tfd.sm.api.http.ServerProtectionService;
 
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.ImmutableMap.Builder;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 
 /**
  * Implements both the ServerProtectionService and a CSRFProtectionService. How
  * each host is protected
  * 
  * @author ieb
  * 
  */
 @Component(immediate = false, metatype=true)
 @Service(value = { ServerProtectionService.class, CSRFProtectionService.class,
 		IdentityRedirectService.class })
 public class ServerProtectionServiceImpl implements ServerProtectionService,
 		CSRFProtectionService, IdentityRedirectService {
 
 	// only put urls that have to be excluded here.
 	@Property(value = {
 			"/system/console"
 	})
 	private static final String WHITELIST = "whitelist";
 	@Property(value = "secret, change in production")
 	private static final String SECRET = "secret";
 	private static final String UTF_8 = "UTF-8";
 	private static final String SHA_256 = "SHA-256";
 	private static final String HMAC_SHA256 = "HmacSHA256";
 	@Property(value = { "referer;localhost:8080;http://localhost:8080/",
 			"csrf;localhost:8080;", "usercontent;localhost:8082;",
 			"redirect;localhost:8080;http://localhost:8082" })
 	private static final String HOSTS = "hosts";
 
 	public interface HostType {
 
 		boolean requestSafe(HttpServletRequest request);
 
 	}
 
 	private static final Set<String> SAFE_METHODS = ImmutableSet.of("GET",
 			"HEAD", "OPTIONS", "PROPGET");
 
 	/**
 	 * Redirect host types redirect from one host to another.
 	 * 
 	 * @author ieb
 	 * 
 	 */
 	public class RedirectHostType implements HostType {
 
 		private String target;
 
 		public RedirectHostType(String host, String target) {
 			this.target = target;
 		}
 
 		@Override
 		public boolean requestSafe(HttpServletRequest request) {
 			return false;
 		}
 
 		public String getRedirectTarget(HttpServletRequest request,
 				String userId) {
 			// convert the request URL into a target url replacing the protocol,
 			// host, port part with the target, and add a user transfer
 			String requestURI = request.getRequestURI();
 			String queryString = request.getQueryString();
 			String hmac = getRequestHmac(requestURI, userId);
 			try {
 				if (queryString == null) {
 					queryString = "_hmac=" + URLEncoder.encode(hmac, UTF_8);
 				} else {
 					queryString = queryString + "&_hmac="
 							+ URLEncoder.encode(hmac, UTF_8);
 				}
 			} catch (UnsupportedEncodingException e) {
 				LOGGER.debug(" No UTF-8 Support, check you JVM, its broken");
 			}
 			return target + requestURI + "?" + queryString;
 		}
 
 	}
 
 	/**
 	 * User Content host types are only safe for GET operations.
 	 * 
 	 * @author ieb
 	 * 
 	 */
 	public class UserContentHostType implements HostType {
 
 
 		public UserContentHostType(String host) {
 		}
 
 		@Override
 		public boolean requestSafe(HttpServletRequest request) {
 			return SAFE_METHODS.contains(request.getMethod());
 		}
 
 
 		public String getTransferUserId(HttpServletRequest request) {
 			return checkRequestHmac(request);
 		}
 
 	}
 
 	public class CSRFHostType implements HostType {
 
 		private String host;
 
 		public CSRFHostType(String host) {
 			this.host = host;
 		}
 
 		@Override
 		public boolean requestSafe(HttpServletRequest request) {
 			if (SAFE_METHODS.contains(request.getMethod())) {
 				return true;
 			}
 			return checkCSRFToken(request, host);
 		}
 
 		public String getToken(HttpServletRequest request) {
 			return createCSRFToken(request, host);
 		}
 
 	}
 
 	public class RefererHostType implements HostType {
 
 		private String safeRefererStub;
 
 		public RefererHostType(String host, String safeRefererStub) {
 			this.safeRefererStub = safeRefererStub;
 		}
 
 		public boolean requestSafe(HttpServletRequest request) {
 			if (SAFE_METHODS.contains(request.getMethod())) {
 				return true;
 			}
 			if (safeRefererStub != null) {
 				String referer = getReferer(request);
 				if (referer != null) {
 					if (referer.startsWith("/")
 							|| referer.startsWith(safeRefererStub)) {
 						return true;
 					}
 				}
 			}
 			return false;
 		}
 
 		private String getReferer(HttpServletRequest request) {
 			@SuppressWarnings("unchecked")
 			Enumeration<String> referers = request.getHeaders("Referer");
 			String referer = null;
 			if (referers == null || !referers.hasMoreElements()) {
 				LOGGER.debug("No Referer header present ");
 				return null;
 			}
 			referer = referers.nextElement();
 			if (referer == null) {
 				LOGGER.debug("No Referer header present, was null ");
 			}
 			return referer;
 		}
 
 	}
 
 	private static final Logger LOGGER = LoggerFactory
 			.getLogger(ServerProtectionServiceImpl.class);
 	private Map<String, HostType[]> hosts;
 	private Key[] transferKeys;
 	private String[] whitelist;
 	
 	@Activate
 	public void activate(Map<String, Object> properties) throws NoSuchAlgorithmException, UnsupportedEncodingException {
 		modified(properties);
 	}
 
 	@Modified
 	public void modified(Map<String, Object> properties)
 			throws NoSuchAlgorithmException, UnsupportedEncodingException {
 		Map<String, List<HostType>> ht = Maps.newHashMap();
		String[] hostsConfig = (String[]) properties.get(HOSTS);
 		if (hostsConfig != null) {
 			for (String host : hostsConfig) {
 				String[] hostConfig = StringUtils.split(host, ";");
 				List<HostType> htl = ht.get(hostConfig[1]);
 				if (htl == null) {
 					htl = Lists.newArrayList();
 					ht.put(hostConfig[1], htl);
 				}
 				// format is type;host;config
 				if ("referer".equals(hostConfig[0])) {
 					htl.add(new RefererHostType(hostConfig[1], hostConfig[2]));
 				} else if ("csrf".equals(hostConfig[0])) {
 					htl.add(new CSRFHostType(hostConfig[1]));
 				} else if ("usercontent".equals(hostConfig[0])) {
 					htl.add(new UserContentHostType(hostConfig[1]));
 				} else if ("redirect".equals(hostConfig[0])) {
 					htl.add(new RedirectHostType(hostConfig[1], hostConfig[2]));
 				} else {
 					LOGGER.warn("Uknown host config type {} ", host);
 				}
 			}
 		}
 		Builder<String, HostType[]> b = ImmutableMap.builder();
 		for (Entry<String, List<HostType>> e : ht.entrySet()) {
 			if (e.getValue().size() > 0) {
 				b.put(e.getKey(),
 						e.getValue().toArray(new HostType[e.getValue().size()]));
 			}
 		}
 		hosts = b.build();
 
 		String transferSharedSecret = (String) properties.get(SECRET);
 		transferKeys = new Key[10];
 		MessageDigest md = MessageDigest.getInstance(SHA_256);
 		Base64 encoder = new Base64(true);
 		byte[] input = transferSharedSecret.getBytes(UTF_8);
 		// create a static ring of 10 keys by repeatedly hashing the last key
 		// seed
 		// starting with the transferSharedSecret
 		for (int i = 0; i < transferKeys.length; i++) {
 			md.reset();
 			byte[] data = md.digest(input);
 			transferKeys[i] = new SecretKeySpec(data, HMAC_SHA256);
 			input = encoder.encode(data);
 		}
 		
		whitelist = (String[]) properties.get(WHITELIST);
 
 	}
 
 	protected String getRequestHmac(String requestURI, String userId) {
 		long expires = System.currentTimeMillis() + 600000L;
 		return hash(requestURI, userId, expires);
 	}
 
 	protected String checkRequestHmac(HttpServletRequest request) {
 		String p = request.getParameter("_hmac");
 		if (p == null) {
 			LOGGER.debug("No token");
 			return null;
 		}
 		String[] parts = StringUtils.split(p, ";");
 		if (parts == null || parts.length < 3) {
 			LOGGER.debug("too short {} ",p);
 			return null;
 		}
 		long expires = Long.parseLong(parts[1]);
 		if (System.currentTimeMillis() > expires) {
 			LOGGER.debug("Expired {} ",p);
 			return null;
 		}
 		if (p.equals(hash(request.getRequestURI(), parts[2], expires))) {
 			LOGGER.debug("Expired, bad hash {} {} ",p,hash(request.getRequestURI(), "", expires));
 			return parts[2];
 		}
 		return null;
 	}
 
 	protected String createCSRFToken(HttpServletRequest request, String host) {
 		long expires = System.currentTimeMillis() + 36000000L;
 		return hash(host, "", expires);
 	}
 
 	protected boolean checkCSRFToken(HttpServletRequest request, String host) {
 		String p = request.getParameter("_csrft");
 		if (p == null) {
 			LOGGER.debug("No token");
 			return false;
 		}
 		String[] parts = StringUtils.split(p, ";");
 		if (parts == null || parts.length < 2) {
 			LOGGER.debug("Invalid Token, too short {} ",p);
 			return false;
 		}
 		long expires = Long.parseLong(parts[1]);
 		if (System.currentTimeMillis() > expires) {
 			LOGGER.debug("Expired, too short {} ",p);
 			return false;
 		}
 		
 		if ( ! p.equals(hash(host, "", expires) )) {
 			LOGGER.debug("Expired, bad hash {} {} ",p,hash(host, "", expires));
 			return false;
 		}
 		return true;
 	}
 
 	private String hash(String privatePayload, String publicPayload, long expires) {
 		try {
 			int keyIndex = (int) (expires - ((expires / 10) * 10));
 			Mac m = Mac.getInstance(HMAC_SHA256);
 			m.init(transferKeys[keyIndex]);
 
 			String message = privatePayload + publicPayload + expires;
 			m.update(message.getBytes(UTF_8));
 			StringBuilder sb = new StringBuilder();
 				sb.append(new String(Base64.encodeBase64URLSafe(m.doFinal())));
 			sb.append(";");
 			sb.append(expires);
 			sb.append(";");
 			sb.append(publicPayload);
 			LOGGER.debug("Hashing {} to {} ",message, sb.toString());
 			return sb.toString();
 		} catch (NoSuchAlgorithmException e) {
 			LOGGER.error("Unable to hash token, please check JVM for SHA256");
 		} catch (UnsupportedEncodingException e) {
 			LOGGER.error("No UTF-8 char set, check JVM");
 		} catch (InvalidKeyException e) {
 			LOGGER.error("Invlid Key used in hash");
 		}
 		return null;
 	}
 	
 	private boolean inWhitelist(HttpServletRequest request) {
 		String requestURI = request.getRequestURI();
 		for (String openUrl : whitelist) {
 			if ( requestURI.startsWith(openUrl)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 
 	private String buildTrustedHostHeader(HttpServletRequest request) {
 		// try the host header first
 		String host = request.getHeader("Host");
 		if (host != null && host.trim().length() > 0) {
 			LOGGER.debug("Host header taknen from http host header as [{}]",
 					host);
 			return host;
 		}
 		// if not suitable resort to letting jetty build the host header
 		int port = request.getServerPort();
 		String scheme = request.getScheme();
 		String serverName = request.getServerName();
 		// default ports are not added to the header.
 		if ((port == 80 && "http".equals(scheme))
 				|| (port == 443 && "https".equals(scheme))) {
 			LOGGER.debug(
 					"Host header not present, constructed assuming default port from request [{}]",
 					serverName);
 			return serverName;
 		} else {
 			LOGGER.debug("Host header not present, from request [{}:{}]",
 					serverName, port);
 			return serverName + ":" + port;
 		}
 	}
 
 	@Override
 	public Action checkAction(HttpServletRequest request) {
 		if ( inWhitelist(request)) {
 			return Action.OK;
 		}
 		// get the host
 		String hostHeader = buildTrustedHostHeader(request);
 		// check the host
 		HostType[] htl = hosts.get(hostHeader);
 		if (htl != null) {
 			for (HostType host : htl) {
 				if (host.requestSafe(request)) {
 					return Action.OK;
 				}
 			}
 		}
 		return Action.FORBID;
 	}
 
 
 	@Override
 	public String getCSRFToken(HttpServletRequest request) {
 		if ( inWhitelist(request)) {
 			return null;
 		}
 		String hostHeader = buildTrustedHostHeader(request);
 		HostType[] htl = hosts.get(hostHeader);
 		if (htl != null) {
 			for (HostType host : htl) {
 				if (host instanceof CSRFHostType) {
 					return ((CSRFHostType) host).getToken(request);
 				}
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public String getRedirectIdentityUrl(HttpServletRequest request,
 			String userId) {
 		if ("GET".equals(request.getMethod())) {
 			String hostHeader = buildTrustedHostHeader(request);
 			HostType[] htl = hosts.get(hostHeader);
 			if (htl != null) {
 				for (HostType host : htl) {
 					if (host instanceof RedirectHostType) {
 						return ((RedirectHostType) host).getRedirectTarget(
 								request, userId);
 					}
 				}
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public String getIdentity(HttpServletRequest request) {
 		if ("GET".equals(request.getMethod())) {
 			String hostHeader = buildTrustedHostHeader(request);
 			HostType[] htl = hosts.get(hostHeader);
 			if (htl != null) {
 				for (HostType host : htl) {
 					if (host instanceof UserContentHostType) {
 						return ((UserContentHostType) host)
 								.getTransferUserId(request);
 					}
 				}
 			}
 		}
 		return null;
 	}
 
 }
