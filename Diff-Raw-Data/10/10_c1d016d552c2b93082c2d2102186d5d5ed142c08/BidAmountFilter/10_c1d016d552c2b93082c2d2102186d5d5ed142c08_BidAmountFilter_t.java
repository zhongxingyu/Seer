 package com.epam.lab.buyit.controller.web.filter;
 
 import java.io.IOException;
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 public class BidAmountFilter implements Filter {
 
 	public void destroy() {
 	}
 
 	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
 		HttpServletRequest httpRequest = (HttpServletRequest) request;
 		HttpServletResponse httpResponse = (HttpServletResponse) response;
 		
 		if(httpRequest.getAttribute("bidAmountException") != null) {
 			httpRequest.setAttribute("message", "Sorry, your bid is to small.");
 			httpRequest.setAttribute("messageHeader", "Warning");
 			httpRequest.setAttribute("alert", "block");
 			httpRequest.getRequestDispatcher("message_page").forward(httpRequest, httpResponse);
 		}
		else chain.doFilter(request, response);		
 	}
 	
 	public void init(FilterConfig fConfig) throws ServletException {
 	}
 
 }
