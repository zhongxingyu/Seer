 /**
  * 
  */
 package org.zmonitor.web;
 
 import java.io.IOException;
 
 import javax.servlet.FilterChain;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.zmonitor.AlreadyStartedException;
 import org.zmonitor.MarkerFactory;
 import org.zmonitor.MonitorMeta;
 import org.zmonitor.TrackingContext;
 import org.zmonitor.ZMonitor;
 import org.zmonitor.ZMonitorManager;
 import org.zmonitor.config.ConfigSource;
 import org.zmonitor.config.ConfigSources;
 import org.zmonitor.impl.MonitorMetaBase;
 import org.zmonitor.impl.TrackingContextBase;
 import org.zmonitor.impl.ZMLog;
 import org.zmonitor.spi.MonitorLifecycle;
 
 /**
  * @author Ian YT Tsai (Zanyking)
  *
  */
 public class ZMonitorFilterHelper {
 	private boolean isIgnitBySelf;
 	private HttpRequestMonitorLifecycleManager hReqMSLfManager;
 
 	private static final String TRACKER_NAME = ZMonitorFilter.TRACKER_NAME;
 	
 	
 	/**
 	 */
 	public static ZMonitorFilterHelper getInstance(ServletContext servletCtx) {
 		ZMonitorFilterHelper helper = (ZMonitorFilterHelper) servletCtx
 				.getAttribute(ZMonitorFilterHelper.class.getName());
 		if (helper == null) {
 			synchronized (servletCtx) {
 				if (helper == null) {
 					servletCtx.setAttribute(
 							ZMonitorFilterHelper.class.getName(),
 							helper = new ZMonitorFilterHelper());
 				}
 			}
 		}
 		return helper;
 	}
 	/**
 	 * 
 	 * @param servletCtx
 	 */
 	public void init(ServletContext servletCtx) {
 		// init ProfilingManager...
 		if(ZMonitorManager.isInitialized())return;
 		
 		ZMonitorManager aZMonitorManager = new ZMonitorManager();
 		
 		ConfigSource configSource = null;
 		try {
 			configSource = ConfigSources.loadForJavaEEWebApp(servletCtx);
 		} catch (IOException e) {
 			ZMLog.warn(e, "Got some problem while reading: ",
 					ConfigSource.WEB_INF_ZMONITOR_XML, ", please make sure it is exist!");
 		}
 		if(configSource==null){
 			ZMLog.warn("cannot find Configuration:[",
 					ConfigSource.ZMONITOR_XML,
 					"] from current application context: ",ZMonitorFilter.class);
 			ZMLog.warn("There's no configuration file loaded, the ZMonitorManager will be configured manually by developer himself.");
 			ZMLog.warn("If you want to do configuration by config file, " ,
 					"please give your own \"",ConfigSource.ZMONITOR_XML,"\" under /WEB-INF/");
 		}
 		
 		aZMonitorManager.performConfiguration(configSource);
 		
 		hReqMSLfManager = new HttpRequestMonitorLifecycleManager();
 		aZMonitorManager.setLifecycleManager(hReqMSLfManager);
 		try {
 			ZMonitorManager.init(aZMonitorManager);
 			isIgnitBySelf = true;
 		} catch (AlreadyStartedException e) {
 			ZMLog.info("already initialized by other place");
 		}
 		ZMLog.info(">> Ignit ZMonitor in: ",ZMonitorFilter.class.getCanonicalName());
 	}
 	/**
 	 * 
 	 */
 	public void destroy() {
 		if(isIgnitBySelf){
 			ZMonitorManager.dispose();
 		}
 	}
 	
 	/**
 	 * need to be thread safe, multiple request may call this method at the same time.
 	 * @param req
 	 * @param res
 	 */
 	public void doFilterStart(HttpServletRequest req, HttpServletResponse res){
 		int stackLv = ReqStack.getInstance(req).push();
 		
 		if (isIgnitBySelf && stackLv <= 1) {// need to identify if this is the first 
 			HttpRequestContexts.init(new StantardHttpRequestContext(), req, res);	
 			hReqMSLfManager.initRequest(req);
 		}
 		ZMonitor.push(newPushTrackingCtx(req));
 	}
 	
 	/**
 	 * need to be thread safe, multiple request may call this method at the same time.
 	 * @param req
 	 */
 	public void doFilterEnd(HttpServletRequest req){
 		try {
 			ZMonitor.pop(newPopTrackingCtx());
 		} finally {
 			int stackLv = ReqStack.getInstance(req).pop();
 			//
 			if (isIgnitBySelf && stackLv<=0) {
 				hReqMSLfManager.finishRequestIfAny(req);
 				HttpRequestContexts.dispose();
 			}
 		}
 	}
 	
 	
 	/**
 	 * 
 	 * @author Ian YT Tsai (Zanyking)
 	 *
 	 */
 	private static class ReqStack{
 		private static final String KEY =  ReqStack.class.getName();
 		/**
 		 * 
 		 * @param req
 		 * @return
 		 */
 		public static ReqStack getInstance(HttpServletRequest req){
 			ReqStack rs = (ReqStack) req.getAttribute(KEY);
 			if(rs==null){
 				req.setAttribute(KEY, rs = new ReqStack());
 			}
 			return rs;
 		}
 		
 		private int stackLv;
 		
 		public int push(){
 			stackLv++;
 			return stackLv;
 		}
 
 		public int pop(){
 			stackLv--;
 			return stackLv;
 		}
 	}//end of class...
 	
 	/**
 	 * 
 	 * @param req
 	 * @return
 	 */
 	private TrackingContext newPushTrackingCtx(HttpServletRequest req){
 		
 		WebConfigurator webConf = ZMonitorManager.getInstance().getBeanIfAny(
 				WebConfigurator.class);
		MonitorMeta mm = webConf.newMonitorMeta("request-start",req);
 		
 		WebTrackingContextBase webCtx = new WebTrackingContextBase(null, mm);
 		webCtx.setMessage(WebUtils.toURLString(req));
 		return webCtx;
 	}
 	/**
 	 * 
 	 * @return
 	 */
 	private TrackingContext newPopTrackingCtx(){
 		MonitorMeta mm = new MonitorMetaBase(
 				MarkerFactory.getMarker("request-end"), 
 				TRACKER_NAME, null, 
 				Thread.currentThread().getName());
 		
 		WebTrackingContextBase webCtx = new WebTrackingContextBase( null, mm);
 		webCtx.setMessage("<- END");
 		return webCtx;
 	}
 
 	/**
 	 * 
 	 * @author Ian YT Tsai (Zanyking)
 	 *
 	 */
 	private class WebTrackingContextBase extends TrackingContextBase{
 		private MonitorMeta fMonitorMeta;
 
 		public WebTrackingContextBase(StackTraceElement[] stackTraceElements, 
 				MonitorMeta fMonitorMeta) {
 			super(TRACKER_NAME, stackTraceElements);
 			this.fMonitorMeta = fMonitorMeta;
 		}
 
 		@Override
 		public MonitorMeta newMonitorMeta() {
 			return fMonitorMeta;
 		}
 		
 		public MonitorLifecycle getLifeCycle() {
 			if(isIgnitBySelf)
 				return hReqMSLfManager.getLifecycle();
 			return ZMonitorManager.getInstance().getMonitorLifecycle();
 		}
 	}//end of class...
 	
 	
 	
 	
 }
