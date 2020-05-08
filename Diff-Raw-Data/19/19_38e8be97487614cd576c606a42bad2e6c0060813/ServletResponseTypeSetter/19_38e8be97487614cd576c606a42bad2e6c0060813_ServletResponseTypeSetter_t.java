 package com.gentics.cr.util.response;
 
import java.io.IOException;

 import javax.servlet.http.HttpServletResponse;
 
import org.apache.commons.httpclient.HttpStatus;

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
 
	@Override
	public void setResponseCode(int responsteCode) {
		if (responsteCode == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
			try {
				this.response.sendError(responsteCode);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			this.response.setStatus(responsteCode);
		}
	}

 }
