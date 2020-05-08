 package com.k99k.khunter;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.HashMap;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.Logger;
 
 /**
  * 过滤器，用于过滤非法请求，并处理参数
  */
 public final class KFilter implements Filter {
 
     /**
      * Default constructor. 
      */
     public KFilter() {
     }
     
     private static boolean isStop = false;
 
 	static final Logger log = Logger.getLogger(KFilter.class);
 	
 	private static String ini;
 	
 	private static int rootNum = 0;
 	
 	private static String staticPrefix = "";
 	
 	public static String getPrefix(){
 		return staticPrefix;
 	}
     
 	/**
 	 * @see Filter#destroy()
 	 */
 	public void destroy() {
 		HTManager.exit();
 	}
 	
     public static final String getIni(){
     	return ini;
     }
 	
 	public static final void stop(){
 		isStop = true;
 	}
 
 	public static final void start(){
 		isStop = false;
 	}
 	
 	private static final HashMap<String,String> staticPath = new HashMap<String, String>();
 	/**
 	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
 	 */
 	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
 		HttpServletRequest req = (HttpServletRequest)request;
 		HttpServletResponse resp = (HttpServletResponse)response;
 		//全程使用utf-8
 		setCharset("utf-8",req,resp);
 		
 		String requrl = req.getRequestURI();
 		//requrl = (requrl == null) ? "" : requrl.substring(1);
 		String[] pathArr = requrl.split("\\/");
 		int rn = rootNum+1;
 		String actName = (pathArr.length <= rn) ? "" : pathArr[rn];
 		//TODO 过滤静态请求,此处如果配合前端的web server过滤可省去
 		if (staticPath.containsKey(actName)) {
 			chain.doFilter(request, response);
 			return;
 		}
 		if (isStop) {
 			//FIXME 穿透控制,注意这里一定要加ip控制
 			if (req.getParameter("keelcontrolsall") == null) {
 				resp.setStatus(500);
 				resp.getWriter().print("System under maintenance, please come back later.");
 				return;
 			}
 		}
 //		chain.doFilter(request, response);
 		
 
 		try {
 			//pathArr[0]为域名，pathArr[1]为第一个路径,后面继续为路径,最后面为参数
 			//String[] pathArr = requrl.substring(requrl.indexOf("//")+2).split("\\/");
 //			int rn = 1+rootNum;
 //			String actName = (pathArr.length <= rn) ? "" : pathArr[rn];
 			ActionMsg msg = new HttpActionMsg(actName, req, resp);
 			Action action = ActionManager.findAction(actName);
 			if (action == null) {
 				resp.setStatus(404);
 				resp.getWriter().print("404 - 2");
 				return;
 			}
 			msg.addData("[pathArr]", pathArr);
 			//msg.addData("[prefix]", staticPrefix);
 			//执行action
 			msg = action.act(msg);
 			//是否打印
 			if (msg.getData("[print]") != null) {
 				resp.getWriter().print(msg.getData("[print]"));
 				return;
 			}
 			//是否发向JSP
 			else if (msg.getData("[jsp]") != null) {
 				String to = (String) msg.getData("[jsp]");
 				Object o = msg.getData("[jspAttr]");
 				if (o != null) {
 					req.setAttribute("[jspAttr]", o);
 				}
 				RequestDispatcher rd = req.getRequestDispatcher(to);
 				rd.forward(req, resp);
 				return;
 			}
 			//是否跳转
 			else if (msg.getData("[redirect]") != null) {
 				String redirect = KFilter.getPrefix()+(String) msg.getData("[redirect]");
 				resp.sendRedirect(redirect);
 				return;
 			}else{
 				resp.setStatus(404);
 				resp.getWriter().print("404 - 3");
 				return;
 			}
 		} catch (Exception e) {
 			log.error("KFilter error!", e);
 			resp.setStatus(404);
 			resp.getWriter().print("500 - System error! please contact administrator.");
 			return;
 		}
 
 	}
 	
 	public static final boolean reStart(){
 		KFilter.stop();
 		HTManager.exit();
		boolean init = HTManager.init(ActionServlet.getIni());
 		KFilter.start();
 		return init;
 	}
 
 	/**
 	 * @see Filter#init(FilterConfig)
 	 */
 	public void init(FilterConfig fConfig) throws ServletException {
 		ini = fConfig.getInitParameter("ini");
 		rootNum = Integer.parseInt(fConfig.getInitParameter("rootNum"));
 		staticPrefix = fConfig.getInitParameter("staticPrefix");
 		//处理静态内容(非Action请求)的过滤
 		String[] statics = fConfig.getInitParameter("statics").split(",");
 		for (int i = 0; i < statics.length; i++) {
 			staticPath.put(statics[i], statics[i]);
 		}
 		boolean initOK = HTManager.init(ini);
 		if (!initOK) {
 			log.error("---------KHunter init failed!!!------------");
 		}
 	}
 	
 	/**
 	 * 设置输入输出的编码
 	 * @param charset
 	 * @param req
 	 * @param resp
 	 * @throws UnsupportedEncodingException
 	 */
 	public static final void setCharset(String charset,HttpServletRequest req, HttpServletResponse resp) throws UnsupportedEncodingException{
 		req.setCharacterEncoding(charset);
 		resp.setCharacterEncoding(charset);
 		resp.setHeader("Content-Encoding",charset);
 		resp.setHeader("content-type","text/html; charset="+charset);
 	}
 	
 	/**
 	 * 从msg的[pathArr]中定位子Action的actName
 	 * @param msg
 	 * @param pathNum
 	 * @param defaultStr
 	 * @return subact 子Action的actName
 	 */
 	public static final String actPath(ActionMsg msg,int pathNum,String defaultStr){
 		//FIXME 测试时多计算了Servlet
 		pathNum = pathNum+rootNum;
 		String[] pathArr = (String[]) msg.getData("[pathArr]");
 		String subact = (pathArr.length <= pathNum) ? defaultStr : pathArr[pathNum];
 		return subact;
 	}
 }
