 package com.bus.stripes.actionbean;
 
 import java.util.HashMap;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 
 import com.bus.dto.Account;
 
 import net.sourceforge.stripes.action.ActionBeanContext;
 import net.sourceforge.stripes.action.ForwardResolution;
 import net.sourceforge.stripes.action.Resolution;
 import net.sourceforge.stripes.action.StreamingResolution;
 import net.sourceforge.stripes.util.CryptoUtil;
 
 public class MyActionBeanContext extends ActionBeanContext {
 
 	public String getLocalFileLocation(){
		return "/var/www/html/bms-id-files/";
//		return "D:/www/bms-id-files/";
 	}
 	
 	public String getHrhostidfile(){
 		return "http://172.168.4.200/bms-id-files/";
 //		return "http://172.168.4.95/bms-id-files/";
 	}
 	
     public void setAccount(Account acc) {
         getRequest().getSession().setAttribute("account", acc);
     }
 
     public Account getUser() {
         return (Account) getRequest().getSession().getAttribute("account");
     }
     
     public void removeUser(){
     	getRequest().getSession().removeAttribute("account");
     	getRequest().getSession().removeAttribute("roles");
     }
     
     public void setRoles(List<String> roles){
     	getRequest().getSession().setAttribute("roles", roles);
     }
     
     public List<String> getRoles(){
     	return (List<String>)(getRequest().getSession().getAttribute("roles"));
     }
     
     public Resolution errorResolution(String error,String detail){
     	return new ForwardResolution("/actionbean/Error.action").addParameter("error", error).addParameter("description", detail);
     }
     
     public Resolution errorResolutionAjax(String error,String detail){
     	return new StreamingResolution("text/charset=utf-8;",error+"-"+detail);
     }
     
     public String getFullURL() {
     	HttpServletRequest request = this.getRequest();
         StringBuffer requestURL = request.getRequestURL();
         String queryString = request.getQueryString();
 
         if (queryString == null) {
             return requestURL.toString();
         } else {
             return requestURL.append('?').append(queryString).toString();
         }
     }
 }
