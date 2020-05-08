 package com.openappengine.security;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
 
 public class WebstoreLoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
 	@Override
 	protected String determineTargetUrl(HttpServletRequest request,
 			HttpServletResponse response) {
 		if (request.getParameter("checkout_flow") != null 
 				&& request.getParameter("checkout_flow").equals("T")) {
			String shoppingCartId = request.getParameter("shoppingCartId");
			return checkoutSuccessUrl + "?shoppingCartId=" + shoppingCartId;
 		} else {
 			return super.determineTargetUrl(request, response);
 		}
 	}
 
 	private String checkoutSuccessUrl;
 
 	public void setCheckoutSuccessUrl(String checkoutSuccessUrl) {
 		this.checkoutSuccessUrl = checkoutSuccessUrl;
 	}
 }
