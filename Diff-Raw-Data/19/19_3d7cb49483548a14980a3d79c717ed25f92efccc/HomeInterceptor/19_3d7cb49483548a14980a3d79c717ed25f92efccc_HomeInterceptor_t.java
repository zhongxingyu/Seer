 package com.tp.action;
 
 import static com.tp.utils.Constants.*;
 
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import org.apache.commons.lang3.StringUtils;
 import org.apache.struts2.ServletActionContext;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.opensymphony.xwork2.ActionInvocation;
 import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
 import com.tp.action.log.LogAction;
 import com.tp.cache.MemcachedObjectType;
 import com.tp.cache.SpyMemcachedClient;
 import com.tp.entity.DownloadType;
 import com.tp.entity.log.LogInHome;
 import com.tp.mapper.JsonMapper;
 import com.tp.service.LogService;
 import com.tp.utils.Constants;
 import com.tp.utils.Constants.Language;
 import com.tp.utils.DateFormatUtils;
 import com.tp.utils.ServletUtils;
 import com.tp.utils.Struts2Utils;
 
 public class HomeInterceptor extends AbstractInterceptor {
 
 	private static final long serialVersionUID = 1L;
 	private static Logger logger = LoggerFactory.getLogger(HomeInterceptor.class);
 
 	private LogService logService;
 
 	private SpyMemcachedClient memcachedClient;
 
 	private JsonMapper mapper = JsonMapper.buildNormalMapper();
 
 	@Override
 	public String intercept(ActionInvocation invocation) throws Exception {
 		Object action = invocation.getAction();
 		String method = invocation.getProxy().getMethod();
 		Map<String, Object> paramMap = invocation.getInvocationContext().getParameters();
 
		if (action instanceof HomeAction || action instanceof LockerAction || action instanceof JplockerAction) {
 			saveLog(method, paramMap);
 			setParamInSession(method);
			String language = (String) Struts2Utils.getSessionAttribute(Constants.PARA_LANGUAGE);
			if (language.equalsIgnoreCase("ja")) {
				Struts2Utils.getSession().setAttribute(Constants.PARA_LANGUAGE, Language.JP.getValue());
			}
 		}

 		if (action instanceof FileDownloadAction) {
 			if (method.equals(METHOD_GET_CLIENT)) {
 				String imei = Struts2Utils.getParameter(PARA_IMEI);
 				boolean save = true;
 				if (imei == null || imei.isEmpty()) {
 					save = isFirstDownload();
 				} else {
 					save = !isInMemcached(MemcachedObjectType.LOG_IMEI.getPrefix() + imei);
 				}
 				if (save) {
 					saveLog(method, paramMap);
 				}
 			}
 		}
 		if (action instanceof LogAction) {
 			if (method.equals("saveDownload")) {
 				String imei = Struts2Utils.getParameter(PARA_IMEI);
 				String id = Struts2Utils.getParameter("id");
 				if (imei != null && !imei.isEmpty()) {
 					boolean save = !isInMemcached(MemcachedObjectType.LOG_IMEI.getPrefix() + imei + id);
 					if (!save) {
 						Struts2Utils.renderText("do nothing");
 						return null;
 					}
 				}
 			}
 		}
 		return invocation.invoke();
 	}
 
 	private boolean isFirstDownload() {
 		Cookie[] cookies = Struts2Utils.getRequest().getCookies();
 		if (cookies != null) {
 			for (int i = 0; i < cookies.length; i++) {
 				if (cookies[i].getName().equals("downloadFlag")) {
 					return false;
 				}
 
 			}
 		}
 		return true;
 	}
 
 	private boolean isInMemcached(String key) {
 		String exitsValue = memcachedClient.get(key);
 		if (exitsValue != null) {
 			return true;
 		} else {
 			memcachedClient.set(key, MemcachedObjectType.LOG_IMEI.getExpiredTime(), "1");
 			return false;
 		}
 	}
 
 	private void saveLog(String requestMethod, Map<String, Object> requestParam) throws Exception {
 		if (requestMethod.equals(METHOD_AD_XML)) {
 			return;
 		}
 		if (requestMethod.equals("execute") && requestParam.get("f") == null) {
 			return;
 		}
 
 		StringBuilder buffer = new StringBuilder();
 		LogInHome log = new LogInHome();
 		log.setRequestMethod(requestMethod);
 		Set<Entry<String, Object>> keys = requestParam.entrySet();
 		for (Entry<String, Object> e : keys) {
 			String k = e.getKey();
 			String v = ((String[]) e.getValue())[0];
 			if (v != null && !v.isEmpty()) {
 				v = new String(v.getBytes("iso-8859-1"), Constants.ENCODE_UTF_8);
 			}
 			if (k.equals(PARA_IMEI)) {
 				log.setImei(v);
 			} else if (k.equals(PARA_IMSI)) {
 				log.setImsi(v);
 			} else if (k.equals(PARA_DOWNLOAD_METHOD)) {
 				log.setDownType(v);
 			} else if (k.equals(PARA_LANGUAGE)) {
 				log.setLanguage(v);
 			} else if (k.equals(PARA_RESOLUTION)) {
 				log.setResolution(v);
 			} else if (k.equals(PARA_STORE_TYPE)) {
 				log.setStoreType(v);
 			} else if (k.equals(PARA_CLIENT_VERSION)) {
 				log.setClientVersion(v);
 			} else if (k.equals(PARA_FROM_MARKET)) {
 				log.setFromMarket(v);
 			} else {
 				buffer.append(k).append(":").append(v).append(",");
 			}
 		}
 		String params = removeLastChara(buffer.toString());
 		log.setRequestParams(params);
 		log.setCreateTime(DateFormatUtils.convert(new Date()));
 		if (!isAvailable(log)) {
 			HttpServletRequest request = Struts2Utils.getRequest();
 			String userAgent = request.getHeader("User-Agent");
 			logger.warn("参数过长: " + mapper.toJson(log) + ",ip:" + ServletUtils.getIpAddr(request) + ",User-Agent:"
 					+ userAgent);
 			return;
 		}
 		logService.saveLogInHome(log);
 
 	}
 
 	private boolean isAvailable(LogInHome log) {
 		if (StringUtils.length(log.getImei()) > 50) {
 			return false;
 		}
 		if (StringUtils.length(log.getImei()) > 50) {
 			return false;
 		}
 		if (StringUtils.length(log.getClientVersion()) > 20) {
 			return false;
 		}
 		if (StringUtils.length(log.getDownType()) > 10) {
 			return false;
 		}
 		if (StringUtils.length(log.getFromMarket()) > 255) {
 			return false;
 		}
 		if (StringUtils.length(log.getLanguage()) > 20) {
 			return false;
 		}
 		if (StringUtils.length(log.getRequestMethod()) > 255) {
 			return false;
 		}
 		if (StringUtils.length(log.getRequestParams()) > 255) {
 			return false;
 		}
 		if (StringUtils.length(log.getResolution()) > 20) {
 			return false;
 		}
 		if (StringUtils.length(log.getStoreType()) > 20) {
 			return false;
 		}
 		return true;
 	}
 
 	private String removeLastChara(String str) {
 
 		return StringUtils.substring(str, 0, str.length() - 1);
 	}
 
 	private void setParamInSession(String method) {
 		HttpSession session = Struts2Utils.getSession();
 		String language = Struts2Utils.getParameter(PARA_LANGUAGE);
 		String fromMarket = Struts2Utils.getParameter(PARA_FROM_MARKET);
 		String downMethod = Struts2Utils.getParameter(PARA_DOWNLOAD_METHOD);
 		String imei = Struts2Utils.getParameter(PARA_IMEI);
 		String imsi = Struts2Utils.getParameter(PARA_IMSI);
 		String client_version = Struts2Utils.getParameter(PARA_CLIENT_VERSION);
 		String resolution = Struts2Utils.getParameter(PARA_RESOLUTION);
 		String store_type = Struts2Utils.getParameter(PARA_STORE_TYPE);
 		String fromClient = Struts2Utils.getParameter(PARA_FROM);
 		String op = Struts2Utils.getParameter(PARA_OPERATORS);
 		String ct = Struts2Utils.getParameter(PARA_CLIENT_TYPE);
 		Map<String, Object> requestParams = Maps.newHashMap();
 		if (fromClient != null) {
 			requestParams.put(PARA_FROM, fromClient);
 		}
 		if (op != null) {
 			requestParams.put(PARA_OPERATORS, op);
 		}
 		if (imei != null) {
 			session.setAttribute(PARA_IMEI, imei);
 			requestParams.put(PARA_IMEI, imei);
 		}
 		if (imsi != null) {
 			session.setAttribute(PARA_IMSI, imsi);
 			requestParams.put(PARA_IMSI, imsi);
 		}
 		if (client_version != null) {
 			session.setAttribute(PARA_CLIENT_VERSION, client_version);
 			requestParams.put(PARA_CLIENT_VERSION, client_version);
 		}
 		if (resolution != null) {
 			session.setAttribute(PARA_RESOLUTION, resolution);
 			requestParams.put(PARA_RESOLUTION, resolution);
 		}
 		if (store_type != null) {
 			session.setAttribute(PARA_STORE_TYPE, store_type);
 			requestParams.put(PARA_STORE_TYPE, store_type);
 		}
 		if (ct != null) {
 			requestParams.put(PARA_CLIENT_TYPE, ct);
 		}
 		if (language != null) {
 
 			if (defaultLanguage().contains(language.toLowerCase())) {
 				session.setAttribute(PARA_LANGUAGE, language.toLowerCase());
 			} else {
 				session.setAttribute(PARA_LANGUAGE, Language.EN.getValue());
 			}
 
 		} else {
 			language = Constants.getLocal();
 			session.setAttribute(PARA_LANGUAGE, language);
 		}
 		requestParams.put(PARA_LANGUAGE, language);
 
 		Locale local = new Locale((String) session.getAttribute(PARA_LANGUAGE));
 		ServletActionContext.getContext().setLocale(local);
 		if (fromMarket != null) {
 			session.setAttribute(PARA_FROM_MARKET, fromMarket);
 			requestParams.put(PARA_FROM_MARKET, fromMarket);
 		}
 		if (downMethod != null) {
 			session.setAttribute(PARA_DOWNLOAD_METHOD, downMethod);
 		} else if (downMethod == null && session.getAttribute(PARA_DOWNLOAD_METHOD) == null) {
 			session.setAttribute(PARA_DOWNLOAD_METHOD, DownloadType.HTTP.getValue());
 		}
 		requestParams.put(PARA_DOWNLOAD_METHOD, (String) session.getAttribute(PARA_DOWNLOAD_METHOD));
 
 		StringBuilder buffer = new StringBuilder();
 		for (Map.Entry<String, Object> entry : requestParams.entrySet()) {
 			buffer.append(entry.getKey() + "=" + entry.getValue()).append("&");
 		}
 
 		if (method.equals("execute")) {
 			session.setAttribute(QUERY_STRING, removeLastChara(buffer.toString()));
 		}
 	}
 
 	private static List<String> defaultLanguage() {
 
 		List<String> languages = Lists.newArrayList();
 		Language[] lans = Language.values();
 		for (Language l : lans) {
 			languages.add(l.getValue());
 		}
 		return languages;
 
 	}
 
 	@Autowired
 	public void setLogService(LogService logService) {
 		this.logService = logService;
 	}
 
 	@Autowired
 	public void setMemcachedClient(SpyMemcachedClient memcachedClient) {
 		this.memcachedClient = memcachedClient;
 	}
 }
