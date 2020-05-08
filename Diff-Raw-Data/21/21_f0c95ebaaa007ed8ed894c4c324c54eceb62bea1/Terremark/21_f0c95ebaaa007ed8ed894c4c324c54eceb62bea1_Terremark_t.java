 package org.dasein.cloud.terremark;
 
 import java.io.UnsupportedEncodingException;
 import java.security.InvalidKeyException;
 import java.security.NoSuchAlgorithmException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.TimeZone;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import javax.crypto.Mac;
 import javax.crypto.spec.SecretKeySpec;
 
 import org.apache.commons.codec.binary.Base64;
 import org.apache.http.NameValuePair;
 import org.apache.log4j.Logger;
 import org.dasein.cloud.AbstractCloud;
 import org.dasein.cloud.CloudException;
 import org.dasein.cloud.InternalException;
 import org.dasein.cloud.ProviderContext;
 import org.dasein.cloud.dc.Region;
 import org.dasein.cloud.terremark.TerremarkMethod.HttpMethodName;
 import org.dasein.cloud.terremark.compute.Template;
 import org.dasein.cloud.terremark.compute.TerremarkComputeServices;
 import org.dasein.cloud.terremark.identity.TerremarkIdentityServices;
 import org.dasein.cloud.terremark.network.FirewallRule;
 import org.dasein.cloud.terremark.network.TerremarkNetworkServices;
 import org.dasein.cloud.terremark.network.TerremarkNetworkSupport;
 import org.dasein.util.CalendarWrapper;
 import org.w3c.dom.Document;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 public class Terremark  extends AbstractCloud {
 	public class Task {
 		public String taskId;
 		public String errorMessage;
 		public String status;
 	}
 
 	static private final Logger logger = Logger.getLogger(Terremark.class);
 
 	private transient volatile TerremarkProvider provider;
 
 	//Required Headers
 	static public final String AUTHORIZATION         = "Authorization";
 
 	static public final String DATE                  = "Date";
 
 	static public final String TMRK_AUTHORIZATION    = "x-tmrk-authorization";
 
 	static public final String TMRK_DATE             = "x-tmrk-date";
 
 	static public final String TMRK_VERSION          = "x-tmrk-version";
 
 	//Conditional Headers
 	static public final String ACCEPT                = "Accept";
 
 	static public final String CONTENT_LENGTH        = "Content-Length";
 
 	static public final String CONTENT_LOCATION      = "Content-Location";
 
 	static public final String CONTENT_RANGE         = "Content-Range";
 
 	static public final String CONTENT_TYPE          = "Content-Type";
 
 	static public final String LOCATION              = "Location";
 	static public final String GUEST_PASSWORD        = "X-Guest-Password";
 	static public final String GUEST_USER            = "X-Guest-User";
 	static public final String RESPONDING_HOST       = "X-Responding-Host";
 	static public final String TMRK_CONTENTHASH      = "x-tmrk-contenthash";
 
 	static public final String TMRK_TOKEN            = "x-tmrk-token";
 	//Response-Only Headers
 	static public final String TMRK_CURRENTUSER      = "x-tmrk-currentuser";
 	static public final String TMRK_DEPRECATED       = "x-tmrk-deprecated";
 	// Header Values
 	static public final String VERSION               = "2012-09-01";
 	static public final String ALGORITHM             = "HmacSha512";
 	public final static String RFC1123_PATTERN       = "EEE, dd MMM yyyy HH:mm:ss z";
 	public final static String ISO8601_PATTERN       = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
 	public final static String ISO8601_NO_MS_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";
 	public final static String TMRK_URI              = "https://services.enterprisecloud.terremark.com";
 	public final static String URI_PATH              = "/cloudapi/ecloud";
 	public final static String LIVE_SPEC_URI_PATH    = "/cloudapi/spec";
 	public final static String DEFAULT_URI_PATH      = URI_PATH;
 
 	public final static String JSON                  = "application/json";
 	public final static String XML                   = "application/xml";
 
 	// API Calls
 	public final static String ORGANZIATIONS         = "organizations";
 	public final static String ACTION                = "action";
 	public final static String ADMIN                 = "admin";
 	// Common Response Attributes
 	public final static String NAME                  = "name";
 	public final static String HREF                  = "href";
 	public final static String TYPE                  = "type";
 	public final static String ACCESSIBLE            = "accessible";
 	// Task Tags & Status
 	public final static String TASK_TAG              = "Task";
 	public final static String OPERATION_TAG         = "Operation";
 	public final static String STATUS_TAG            = "Status";
 	public final static String ERROR_MESSAGE_TAG     = "ErrorMessage";
 
 	public final static String TASK_COMPLETE         = "Complete";
 	public final static String TASK_QUEUED           = "Queued";
 	public final static String TASK_RUNNING          = "Running";
 
 	public final static String TASK_ERROR            = "Error";
 	public final static int TASK_ERROR_COUNT         = 5;
 	static private String getLastItem(String name) {
 		int idx = name.lastIndexOf('.');
 
 		if( idx < 0 ) {
 			return name;
 		}
 		else if( idx == (name.length()-1) ) {
 			return "";
 		}
 		return name.substring(idx+1);
 	}
 
 	static public Logger getLogger(Class<?> cls) {
 		String pkg = getLastItem(cls.getPackage().getName());
 
 		if( pkg.equals("terremark") ) {
 			pkg = "";
 		}
 		else {
 			pkg = pkg + ".";
 		}
 		return Logger.getLogger("dasein.cloud.terremark.std." + pkg + getLastItem(cls.getName()));
 	}
 	private static String getRfcDate() {
 		Date date = new Date();
 		SimpleDateFormat format = new SimpleDateFormat(RFC1123_PATTERN);
 		format.setTimeZone(TimeZone.getTimeZone("GMT"));
 		return format.format(date);
 	}
 	public static String getTaskHref(Document doc, String taskName) {
 		String href = null;
 		NodeList taskElements = doc.getElementsByTagName(Terremark.TASK_TAG);
 		for (int i=0; i<taskElements.getLength(); i++) {
 			Node taskElement = taskElements.item(i);
 			NodeList taskChildren = taskElement.getChildNodes();
 			for (int j=0; j<taskChildren.getLength(); j++) {
 				Node taskChild = taskChildren.item(j);
 				if (taskChild.getNodeName().equals(Terremark.OPERATION_TAG)) {
 					if (taskChild.getTextContent().equals(taskName)) { 
 						href = taskElement.getAttributes().getNamedItem(Terremark.HREF).getNodeValue();
 						break;
 					}
 				}
 			}
 			if (href != null) {
 				break;
 			}
 		}
 		return href;
 	}
	
 	public static String getTemplateIdFromHref(String templateHref){
 		String id = null;
 		String templateString = "/" + Template.TEMPLATES + "/";
 		String cpLowerString = "/" + EnvironmentsAndComputePools.COMPUTE_POOLS.toLowerCase() + "/";
 		String cpString = "/" + EnvironmentsAndComputePools.COMPUTE_POOLS + "/";
 		templateHref = templateHref.toLowerCase();
 		if (templateHref.contains(templateString) && templateHref.contains(cpLowerString)){
 			int startTemplateId = templateHref.indexOf(templateString) + templateString.length();
 			int startCPId = templateHref.indexOf(cpLowerString) + cpLowerString.length();
 			id = templateHref.substring(startTemplateId, templateHref.indexOf(cpLowerString)) + ":" + templateHref.substring(startCPId) + ":" + Template.ImageType.TEMPLATE.name();
 		}
 		else if (templateHref.contains(templateString) && templateHref.contains(cpString)){
 			int startTemplateId = templateHref.indexOf(templateString) + templateString.length();
 			int startCPId = templateHref.indexOf(cpString) + cpString.length();
 			id = templateHref.substring(startTemplateId, templateHref.indexOf(cpString)) + ":" + templateHref.substring(startCPId) + ":" + Template.ImageType.TEMPLATE.name();
 		}
 		else {
			id = null;
 			logger.warn("getTemplateIdFromHref(): Failed to parse template href " + templateHref);
 		}
 		return id;
 	}
	
 	static public Logger getWireLogger(Class<?> cls) {
 		return Logger.getLogger("dasein.cloud.terremark.wire." + getLastItem(cls.getPackage().getName()) + "." + getLastItem(cls.getName()));
 	}
	
 	/**
 	 * Converts a firewall acls href to the ID format ({custom | nodeServices}/{firewall rule identifier | node service identifier}).
 	 * @param href A FirewallAcl href
 	 * @return the firewall rule ID in the format {custom | nodeServices}/{firewall rule identifier | node service identifier}
 	 */
 	public static String hrefToFirewallRuleId (String href) {
 		final String uri = FirewallRule.FIREWALL_ACLS + "/";
 		final String lowerUri = FirewallRule.FIREWALL_ACLS.toLowerCase() + "/";
 		String firewallRuleId = "";
 		if (href.contains(lowerUri)) {
 			firewallRuleId = href.substring(href.lastIndexOf(lowerUri)+lowerUri.length());
 		}
 		else if (href.contains(uri)) {
 			firewallRuleId = href.substring(href.lastIndexOf(uri)+uri.length());
 		}
 		else {
 			firewallRuleId = href;
 			logger.warn("hrefToFirewallRuleId(): Failed to parse firewall rule href " + href);
 		}
 		return firewallRuleId;
 	}
 
 	public static String hrefToId(String href) {
 		return href.substring(href.lastIndexOf("/")+1);
 	}
 	/**
 	 * Converts a network or IP address href to the ID format (network_id or netowrk_id/ipv6 or network_id/ip_address or network_id/ipv6/ip_address).
 	 * @param href A network or IP address href
 	 * @return the network or IP address ID in the format network_id or netowrk_id/ipv6 or network_id/ip_address or network_id/ipv6/ip_address
 	 */
 	public static String hrefToNetworkId (String href) {
 		String networkId = null;
 		if (href.contains(TerremarkNetworkSupport.NETWORKS)) {
 			int beginIndex = href.indexOf(TerremarkNetworkSupport.NETWORKS + "/") + TerremarkNetworkSupport.NETWORKS.length() + 1;
 			networkId = href.substring(beginIndex, href.length());
 		}
 		else if (href.contains(TerremarkNetworkSupport.NETWORKS.toLowerCase())) {
 			int beginIndex = href.indexOf(TerremarkNetworkSupport.NETWORKS.toLowerCase() + "/") + TerremarkNetworkSupport.NETWORKS.length() + 1;
 			networkId = href.substring(beginIndex, href.length());
 		}
 		else {
 			networkId = href;
 			logger.warn("hrefToNetworkId(): Failed to parse network href " + href);
 		}
 		return networkId;
 	}
 
 	public static Date parseIsoDate(String isoDateString) {
 		java.text.DateFormat df = new SimpleDateFormat(ISO8601_PATTERN);
 		df.setTimeZone(TimeZone.getTimeZone("UTC"));
 		Date result = null;
 		try {
 			result = df.parse(isoDateString);
 		} catch (ParseException e) {
 			df = new SimpleDateFormat(ISO8601_NO_MS_PATTERN);
 			df.setTimeZone(TimeZone.getTimeZone("UTC"));
 			result = null;
 			try {
 				result = df.parse(isoDateString);
 			} catch (ParseException e2) {
 				e2.printStackTrace();
 			}
 		}
 		return result;
 	}
 
 	private static String sign(byte[] key, String authString, String algorithm) throws InternalException {
 		try {
 			Mac mac = Mac.getInstance(algorithm);
 
 			mac.init(new SecretKeySpec(key, algorithm));
 			return new String(Base64.encodeBase64(mac.doFinal(authString.getBytes("utf-8"))));
 		} 
 		catch( NoSuchAlgorithmException e ) {
 			logger.error(e);
 			e.printStackTrace();
 			throw new InternalException(e);
 		} 
 		catch( InvalidKeyException e ) {
 			logger.error(e);
 			e.printStackTrace();
 			throw new InternalException(e);
 		} 
 		catch( IllegalStateException e ) {
 			logger.error(e);
 			e.printStackTrace();
 			throw new InternalException(e);
 		} 
 		catch( UnsupportedEncodingException e ) {
 			logger.error(e);
 			e.printStackTrace();
 			throw new InternalException(e);
 		}
 	}
 
 	private transient volatile Organization currentOrg;
 
 	public Terremark() { }
 
 	@Override
 	public String getCloudName() {
 		String name = getContext().getCloudName();
 
 		return ((name == null ) ? getProviderName() : name);
 	}
 
 	@Override
 	public @Nonnull TerremarkComputeServices getComputeServices() {
 		return new TerremarkComputeServices(this);
 	}
 
 	@Override
 	public @Nonnull EnvironmentsAndComputePools getDataCenterServices() {
 		return new EnvironmentsAndComputePools(this);
 	}
 
 	public Map<String,String> getHeaders(ProviderContext ctx, HttpMethodName methodType, String url, NameValuePair[] queryParamters, String body) {
 		HashMap<String,String> headers = new HashMap<String,String>();
 
 		String accessKey = "";
 		try {
 			accessKey = new String(ctx.getAccessPublic(), "utf-8");
 		} catch (UnsupportedEncodingException e) {
 			logger.warn(e.getMessage());
 		}
 		String signatureType = ALGORITHM;
 		String signature = null;
 		String contentLength = "";
 		String contentType = "";
 		String formattedDate = getRfcDate();
 		String canonicalizedHeaders = "";
 		String canonicalizedResource = "";
 
 		headers.put(TMRK_DATE, formattedDate);
 		headers.put(TMRK_VERSION, VERSION);
 		headers.put(ACCEPT, XML);
 		if (body != null && (methodType.equals(HttpMethodName.PUT) || methodType.equals(HttpMethodName.POST) || methodType.equals(HttpMethodName.DELETE))){
 			contentLength = new String("" + body.getBytes().length);
 		}
 		if (body != null && !body.equals("")){
 			contentType = XML;
 			headers.put(CONTENT_TYPE, contentType);
 		}
 
 		List<String> canonicalizedHeadersList = new ArrayList<String>();
 		HashMap<String,String> tempHeadersLower = new HashMap<String,String>();
 		for( Map.Entry<String, String> entry : headers.entrySet() ) {
 			if (entry.getKey().toLowerCase().startsWith("x-tmrk-") && !entry.getKey().equals(TMRK_AUTHORIZATION))
 				canonicalizedHeadersList.add(entry.getKey().toLowerCase().trim());
 			tempHeadersLower.put(entry.getKey().toLowerCase().trim(), entry.getValue().trim());
 		}
 		Collections.sort(canonicalizedHeadersList);
 		for (String header : canonicalizedHeadersList){
 			String value = tempHeadersLower.get(header);
 			canonicalizedHeaders += header + ":" + value+"\n";
 		}
 
 		if (!url.startsWith("/")){
 			url = "/" + url;
 		}
 
 		canonicalizedResource += DEFAULT_URI_PATH + url.toLowerCase().trim() + "\n";
 		List<String> queryParameterList = new ArrayList<String>();
 		HashMap<String,String> tempQueryParamtersLower = new HashMap<String,String>();
 		if (queryParamters != null) {
 			for( NameValuePair parameter : queryParamters ) {
 				queryParameterList.add(parameter.getName().toLowerCase().trim());
 				tempQueryParamtersLower.put(parameter.getName().toLowerCase().trim(), parameter.getValue().trim());
 			}
 			Collections.sort(queryParameterList);
 		}
 
 		for (String paramter : queryParameterList){
 			String value = tempQueryParamtersLower.get(paramter);
 			canonicalizedResource += paramter+ ":" + value + "\n";
 		}
 
 		// use String constructor for UTF-8 encoding
 		String stringToSign = new String(methodType + "\n" + contentLength + "\n" + contentType + "\n" + formattedDate + "\n" + canonicalizedHeaders + canonicalizedResource);
 		try {
 			signature = sign(ctx.getAccessPrivate(), stringToSign, ALGORITHM);
 		} catch (InternalException e) {
 			logger.warn(e.getMessage());
 			e.printStackTrace();
 		}
 
 		String autorizationString = "CloudApi AccessKey=\"" + accessKey + "\" SignatureType=\"" + signatureType + "\" Signature=\"" + signature + "\"";
 		headers.put(TMRK_AUTHORIZATION, autorizationString);
 
 		return headers;
 	}
 
 	@Override
 	public TerremarkIdentityServices getIdentityServices() {
 		return new TerremarkIdentityServices(this);
 	}
 
 	@Override
 	public @Nonnull TerremarkNetworkServices getNetworkServices() {
 		return new TerremarkNetworkServices(this);
 	}
 
 	public Organization getOrganization() throws CloudException, InternalException {
 		if( currentOrg == null ) {
 			String url = "/" + ORGANZIATIONS + "/";
 			TerremarkMethod method = new TerremarkMethod(this, HttpMethodName.GET, url, null, null);
 			Document doc = method.invoke();
 			if (doc != null) {
 				currentOrg = toOrg(doc);
 			}
 		}
 		return currentOrg;
 	}
 
 	@Override
 	public String getProviderName() {
 		ProviderContext ctx = getContext();
 		String name = (ctx == null ? null : ctx.getProviderName());
 
 		return ((name == null) ? TerremarkProvider.ENTERPRISE_CLOUD.getName() : name);
 	}
 
 	public String getProxyHost() {
 		return getContext().getCustomProperties().getProperty("proxyHost");
 	}
 
 	public int getProxyPort() {
 		String port = getContext().getCustomProperties().getProperty("proxyPort");
 
 		if( port != null ) {
 			return Integer.parseInt(port);
 		}
 		return -1;
 	}
 
 	public String getTaskStatus(String taskHref) throws CloudException, InternalException {
 		String status = null;
 		TerremarkMethod method = new TerremarkMethod(this, HttpMethodName.GET, taskHref, null, null);
 		Document doc = method.invoke();
 		if (doc != null) {
 			status = doc.getElementsByTagName(STATUS_TAG).item(0).getTextContent();
 		}
 		return status;
 	}
 
 	public @Nonnull TerremarkProvider getTerremarkProvider() {
 		if( provider == null ) {
 			provider = TerremarkProvider.valueOf(getProviderName());
 		}
 		return provider;
 	}
 
 	@Override
 	public @Nullable String testContext() {
 		try {
 			ProviderContext ctx = getContext();
 
 			if( ctx == null ) {
 				System.out.println("context is null");
 				return null;
 			}
 			if (ctx.getRegionId() == null) {
 				Collection<Region> regions = getDataCenterServices().listRegions();
 				if (regions.size() > 0) {
 					ctx.setRegionId(regions.iterator().next().getProviderRegionId());
 				}
 			}
 
 			if( !getComputeServices().getVirtualMachineSupport().isSubscribed() ) {
 				System.out.println("isSubscribed is false");
 				return null;
 			}
 			return ctx.getAccountNumber();
 		}
 		catch( Throwable t ) {
 			logger.warn("Failed to test Terremark connection context: " + t.getMessage());
 			if( logger.isDebugEnabled() ) {
 				t.printStackTrace();
 			}
 			return null;
 		}
 	}
 
 	private Organization toOrg(Document doc) throws CloudException {
 		Logger logger = getLogger(Terremark.class);
 		Organization org = new Organization();
 		NodeList blocks = doc.getElementsByTagName("Organization");
 		if (blocks == null){
 			throw new CloudException("Did not find any organizations");
 		}
 		else if (blocks.getLength() > 1){
 			logger.warn("There is more than one organization in this account. This must be a reseller account. Only using the first organization.");
 		}
 
 		Node organization = blocks.item(0);
 		NamedNodeMap attributes = organization.getAttributes();
 		for (int i=0; i < attributes.getLength(); i++) {
 			Node node = attributes.item(i);
 			if (node.getNodeName().equals(Terremark.HREF)){
 				org.setId(hrefToId(node.getNodeValue()));
 			}
 			else if (node.getNodeName().equals(Terremark.NAME)){
 				org.setName(node.getNodeValue());
 			}
 		}
 
 		return org;
 	}
 
 	public Task toTask(Node node) throws CloudException, InternalException {
 		NodeList childNodes = node.getChildNodes();
 		Task task = new Task();
 
 		task.taskId = hrefToId(node.getAttributes().getNamedItem(Terremark.HREF).getNodeValue());
 
 		for( int i=0; i<childNodes.getLength(); i++ ) {
 			Node child = childNodes.item(i);
 			String name = childNodes.item(i).getNodeName();
 
 			if( name.equals(Terremark.STATUS_TAG) ) {
 				task.status = child.getTextContent();
 			}
 			else if( name.equals(Terremark.ERROR_MESSAGE_TAG) ) {
 				task.errorMessage = child.getTextContent();
 			}
 		}
 		return task;
 	}
 
 	public void waitForTask(String taskHref, long sleepTime, long timeout) throws CloudException, InternalException {
 		logger.debug("enter - waitForTask(): " + taskHref);
 		boolean complete = false;
 		long startTime = System.currentTimeMillis();
 		long failurePoint = -1L;
 		int failedCalls = 0;
 		while( !complete && failedCalls < TASK_ERROR_COUNT) {
 			TerremarkMethod method;
 			Document doc = null;
 
 			method = new TerremarkMethod(this, HttpMethodName.GET, taskHref, null, null);
 			try {
 				doc = method.invoke();
 			}
 			catch (CloudException e) {
 				logger.warn("waitForTask Error: " + e);
 				failedCalls++;
 			}
 			if (doc != null ) {
 				Node taskNode = doc.getElementsByTagName(Terremark.TASK_TAG).item(0);
 				Task lt = toTask(taskNode);
 
 				if( lt.status.equals(Terremark.TASK_COMPLETE) ) {
 					complete = true;
 				}
 				else if( lt.status.equals(Terremark.TASK_ERROR) ) {
 					String message = lt.errorMessage;
 
 					if( message == null ) {
 						if( failurePoint == -1L ) {
 							failurePoint = System.currentTimeMillis();
 						}
 						if( (System.currentTimeMillis() - failurePoint) > (CalendarWrapper.MINUTE * 2) ) {
 							message = "Task failed without further information.";
 						}
 					}
 					if( message != null ) {
 						throw new CloudException(message);
 					}
 				}
 				logger.debug("Time ellapsed since task start time " + (System.currentTimeMillis() - startTime));
 				if( !complete ) {
 					if ((System.currentTimeMillis() - startTime) > timeout) {
 						throw new InternalException("Timed out waiting for the task to complete.");
 					}
 					try { Thread.sleep(sleepTime); }
 					catch( InterruptedException e ) { }
 				}
 			}
 		}
 		if (failedCalls >= TASK_ERROR_COUNT) {
 			throw new CloudException("waitForTask(): Get task call failed " + failedCalls + " times. Giving up.");
 		}
 		logger.debug("exit - waitForTask(): " + taskHref);
 	}
 	
 	public static String removeCommas(String tag) {
 		Pattern p = Pattern.compile("[, ]");
 		Matcher m = p.matcher(tag);
 		String clean = m.replaceFirst("");
 		return clean;
 	}
 
 }
