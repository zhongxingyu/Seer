 /**
  * 
  */
 package org.zmonitor.web;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.zmonitor.CustomConfigurable;
 import org.zmonitor.MarkerFactory;
 import org.zmonitor.MonitorMeta;
 import org.zmonitor.bean.ZMBeanBase;
 import org.zmonitor.config.ConfigContext;
 import org.zmonitor.config.ConfigContext.Visitor;
 import org.zmonitor.config.Configs;
 import org.zmonitor.spi.Configurator;
 import org.zmonitor.util.PropertySetter;
 import org.zmonitor.web.filter.Condition;
 import org.zmonitor.web.filter.DefaultUrlFilter;
 import org.zmonitor.web.filter.UrlFilter;
 import org.zmonitor.web.test.TestConfig;
 
 /**
  * @author Ian YT Tsai(Zanyking)
  *
  */
 public class WebConfigurator extends ZMBeanBase implements Configurator {
 	
 	private static final String REL_WEB_CONF = "web-conf";
 	private static final String REL_TEST_CONF = "test-conf";
 	private static final String REL_URL_FILTER = "url-filer";
 	private static final String REL_CONDITION = "condition";
 	private static final String ID_ZMBEAN_WEB_CONF = REL_WEB_CONF;
 	
 	public WebConfigurator(){
 		this.setId(ID_ZMBEAN_WEB_CONF);
 	}
 	
 
 	private UrlFilter filter;
 	private TestConfig fTestConfig = new TestConfig();
 	/**
 	 * 
 	 * @param urlStr
 	 * @return
 	 */
 	public boolean shouldAccept(HttpServletRequest req){
 		if(filter==null)return true;
 		return filter.shouldAccept(WebUtils.toURL(req));
 	}
 	
 	public void configure(ConfigContext monitorMgmt) {
 		ConfigContext webConf = monitorMgmt.toNode(REL_WEB_CONF);
 		
 		if(webConf.getNode()==null)return;//use default...
 		
 		initUrlFilter(webConf);
 		
 		ConfigContext testConfCtx = webConf.toNode(REL_TEST_CONF);
 		if(testConfCtx.getNode()==null)return;//use default...
 		initWebTestConf(testConfCtx);
 		
 	}
 	
 	private void initWebTestConf(ConfigContext testConfCtx) {
 		testConfCtx.applyAttributes(new PropertySetter(fTestConfig));
 		fTestConfig.init(testConfCtx);
 	}
 
 	private void initUrlFilter(ConfigContext webConf){
 		ConfigContext urlFilterCtx = webConf.toNode(REL_URL_FILTER);
 		if(urlFilterCtx.getNode()==null)return;
 		
 		filter = urlFilterCtx.newBean(DefaultUrlFilter.class, false);
 		
 		urlFilterCtx.applyAttributes(new PropertySetter(filter), "class");
 		
 		if(filter instanceof DefaultUrlFilter){
 			initUrlFilterByDefault(urlFilterCtx, (DefaultUrlFilter)filter);
 			
 		} else if(filter instanceof CustomConfigurable){
 			Configs.initCustomConfigurable(urlFilterCtx, 
 					(CustomConfigurable) filter, 
 					true);
 		}
 	}
 	
 	private static UrlFilter initUrlFilterByDefault(
 			final ConfigContext urlFilterCtx, 
 			final DefaultUrlFilter filter){
 
 		urlFilterCtx.forEach( REL_CONDITION, new Visitor(){
 			public boolean visit(int idx, ConfigContext nodeCtx) {
 				
 				Condition cond = new Condition();
 				PropertySetter setter = new PropertySetter(cond);
 				
 				urlFilterCtx.applyPropertyTags(setter);
 				nodeCtx.applyAttributes(setter);
 				
 				if(cond.getPattern()==null){
 					cond.setPattern(nodeCtx.getAttribute("pattern"));
 				}
 				filter.add(cond);
 				return true;
 			}});
 		
 		return filter;
 	}
 	
	public MonitorMeta newMonitorMeta(String markerStr, HttpServletRequest req, String threadName){
 		//  append additional info for zmonitor-webtest purpose.
 		WebMonitorMeta meta = new WebMonitorMeta(
				MarkerFactory.getMarker(markerStr), req, threadName);
 		if(fTestConfig.isActivate()){
 			meta.setUuid(req.getParameter(fTestConfig.getRequestUuidParam()));
 		}
 		return meta;
 	}
 }
