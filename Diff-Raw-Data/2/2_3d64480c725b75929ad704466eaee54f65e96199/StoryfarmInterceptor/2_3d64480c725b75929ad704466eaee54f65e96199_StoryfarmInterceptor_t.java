 package com.bgg.storyfarm.common;
 
 import java.util.Enumeration;
 import java.util.HashMap;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
 
 public class StoryfarmInterceptor extends HandlerInterceptorAdapter {
 
 	private Logger logger = LoggerFactory.getLogger(StoryfarmInterceptor.class);
 	
 	//로그인 세션 검사
 	//false = ON || true = OFF
 	private boolean loginCheckOff = true;
 
 	@Override
 	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
 		
 		try {
 			long startTime = System.currentTimeMillis();
 			request.setAttribute("startTime", startTime);
 
 			printRequestLog(request);
 			
 			if( loginCheckOff ) {
 				return true;
 			}
 			
 			if(	
 				//세션 체크 예외 URL 리스트
 				! request.getServletPath().contains( "/index.do" ) &&
 				! request.getServletPath().contains( "/login.do" ) &&
 				! request.getServletPath().contains( "/join.do" ) &&
 				! request.getServletPath().contains( "/getUser.ajax" ) &&
 				! request.getServletPath().contains( "/joinAction.do" )
 				
 			){
 				
 				HttpSession session = request.getSession(false);
 				
 				if ( session == null || session.getAttribute("user") == null){
 					response.sendRedirect("/index.do?result=6");
 					return false;
 				} else {
 					
 //					 ROLE처리 로직 구현 예정
 //					 자격 없을시 "접근권한이 없음" 문구 알림
 					@SuppressWarnings("unchecked")
 					HashMap<String, Object> map = (HashMap<String, Object>) session.getAttribute("user");
 					if(map.get("level_cd").equals("2")) {
 						response.sendRedirect("/index.do?result=5");
 						return false;
 					}
 				}
 			}
 				 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 				
 		return true;
 	}
 
 	@Override
 	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
 
 		printRequestProcessingTime(request);
 
 	}
 
 	private void printRequestProcessingTime(HttpServletRequest request) {
 		long startTime = (Long) request.getAttribute("startTime");
 		long endTime = System.currentTimeMillis();
 		long executeTime = endTime - startTime;
 		logger.info("[RES]_["+request.getServletPath()+"]_EXECUTETIME_[ " + executeTime + "ms ]");
 	}
 
 	private void printRequestLog(HttpServletRequest request) {
 		try {
 			StringBuffer sb = new StringBuffer();
 			sb.append("[REQ]");
 			sb.append("_IP_[" + request.getRemoteAddr() + "]");
 			sb.append("_REQURL_[" + request.getServletPath() + "]");
 			sb.append("_PARAM_[");
 
 			// parameter
			Enumeration<?> eNames = request.getParameterNames();
 			while (eNames.hasMoreElements()) {
 				String name = (String) eNames.nextElement();
 				String[] values = request.getParameterValues(name);
 				String paramIngo = "[" + name + " : ";
 				for (int x = 0; x < values.length; x++) {
 					if (x == 0) {
 						// paramIngo += URLEncoder.encode(values[x], "UTF-8");
 						paramIngo += values[x];
 					} else {
 						// paramIngo += ", "+URLEncoder.encode(values[x],
 						// "UTF-8");
 						paramIngo += ", " + values[x];
 					}
 				}
 
 				if (StringUtils.isNotEmpty(name)) {
 					if (name.equals("pwd")) {
 						paramIngo = "xxxx ]";
 					} else {
 						paramIngo += "]";
 					}
 				}
 
 				if (eNames.hasMoreElements()) {
 					sb.append(paramIngo + ",");
 				} else {
 					sb.append(paramIngo);
 				}
 			}
 			sb.append("]");
 			logger.info(sb.toString());
 		} catch (Exception e) {
 			logger.error("{}", e);
 		}
 	}
 }
