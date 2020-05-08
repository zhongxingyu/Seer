 package com.gentics.cr.util.response;
 
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * 
  * Last changed: $Date: 2010-04-01 15:25:30 +0200 (Do, 01 Apr 2010) $
  * @version $Revision: 544 $
  * @author $Author: supnig@constantinopel.at $
  *
  */
 public class ServletResponseTypeSetter implements IResponseTypeSetter {
 
 	private HttpServletResponse response;
 
 	/**
 	 * Create new instance of ServletResponseTypeSetter
 	 * @param response
 	 */
 	public ServletResponseTypeSetter(HttpServletResponse response) {
 		this.response = response;
 	}
 
 	/**
 	 * @param type - content type to be set
 	 * 
 	 */
 	public void setContentType(String type) {
 		this.response.setContentType(type);
 	}
 
 }
