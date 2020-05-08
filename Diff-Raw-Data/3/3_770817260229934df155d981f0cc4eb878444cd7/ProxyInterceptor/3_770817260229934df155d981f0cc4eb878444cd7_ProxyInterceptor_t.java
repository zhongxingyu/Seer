 package com.dianping.wizard.widget.interceptor.extensions;
 
 import com.dianping.wizard.widget.InvocationContext;
 import com.dianping.wizard.widget.Widget;
 import com.dianping.wizard.widget.interceptor.Interceptor;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import org.apache.commons.lang.StringUtils;
 import org.apache.http.client.fluent.Request;
 
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * @author ltebean
  */
 public class ProxyInterceptor implements Interceptor {
 
     private static final String WIZARD_COOKIE_NAME = "wizard";
 
     public ProxyInterceptor() {
     }
 
     @Override
     public String intercept(InvocationContext invocation) throws Exception {
 
         HttpServletRequest request = (HttpServletRequest) invocation.getParam().get("request");
         if (request == null) {
             return invocation.invoke();
         }
         WizardCookieConfig config = getWizardCookieConfig(request);
        if(config==null){
            return invocation.invoke();
        }
         if (!needsProxy(config, invocation.getWidget().name)) {
             return invocation.invoke();
         }
         Widget proxyWidget = getProxyWidget(config, invocation.getWidget().name);
         if (proxyWidget != null) {
             replaceWidget(invocation, proxyWidget);
         }
         return invocation.invoke();
     }
 
     private Widget getProxyWidget(WizardCookieConfig config, String widgetName) throws Exception {
         String url = "http://" + config.host + ":" + config.port + "/api/widget/" + widgetName;
         String result = Request.Get(url)
                 .connectTimeout(1000)
                 .socketTimeout(1000)
                 .execute().returnContent().asString();
         if (StringUtils.isEmpty(result)) {
             return null;
         }
         ObjectMapper mapper = new ObjectMapper();
         return mapper.readValue(result, Widget.class);
     }
 
     private boolean needsProxy(WizardCookieConfig config, String widgetName) {
         if (config.apps.contains("all")) {
             return true;
         }
         if (config.apps.contains(widgetName)) {
             return true;
         }
         return false;
     }
 
 
     private void replaceWidget(InvocationContext invocationContext, Widget widget) {
         invocationContext.getWidget().modes = widget.modes;
         invocationContext.getWidget().layoutName = widget.layoutName;
         invocationContext.getWidget().layoutRule = widget.layoutRule;
         invocationContext.getWidget().parentWidgetName = widget.parentWidgetName;
     }
 
     private WizardCookieConfig initFromCookieValue(String cookieValue) {
         if (StringUtils.isEmpty(cookieValue)) {
             return null;
         }
         WizardCookieConfig wizardCookieConfig = new WizardCookieConfig();
         for (String config : cookieValue.split("\\|")) {
             String[] configPair = config.split("~");
             String key = configPair[0];
             String value = configPair[1];
             if (key.equals("host")) {
                 wizardCookieConfig.host = value;
             } else if (key.equals("port")) {
                 wizardCookieConfig.port = value;
             } else if (key.equals("apps")) {
                 Set<String> apps = new HashSet<String>();
                 for (String app : value.split("&")) {
                     apps.add(app);
                 }
                 wizardCookieConfig.apps = apps;
             }
         }
         return wizardCookieConfig;
     }
 
     private WizardCookieConfig getWizardCookieConfig(HttpServletRequest request) {
         Cookie cookies[] = request.getCookies();
         if (cookies != null) {
             for (int i = 0; i < cookies.length; i++) {
                 if (WIZARD_COOKIE_NAME.equals(cookies[i].getName())) {
                     return initFromCookieValue(cookies[i].getValue());
                 }
             }
         }
         return null;
     }
 
     private static final class WizardCookieConfig {
 
         public String host;
 
         public String port = "3000";
 
         public Set<String> apps;
     }
 }
