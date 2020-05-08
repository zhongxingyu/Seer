 package de.kp.ames.web.function.user;
 /**
  * This Java module is part of the
  *  Application Developer Framework
  *
  *  Project: AMES-Web-Service
  *  Package: de.kp.ames.web.function.user
  *  Module: UserServiceImpl
  *  @author krusche@dr-kruscheundpartner.de
  *
  * Add your semantic annotations within the SemanticAssist tags and
  * mark them with a leading hashtag #:
  *
  * <SemanticAssist>
  *     #function #service #user #web
  * </SemanticAssist>
  *
  */
 
 /**
  *	Copyright 2012 Dr. Krusche & Partner PartG
  *
  *	AMES-Web-Service is free software: you can redistribute it and/or 
  *	modify it under the terms of the GNU General Public License 
  *	as published by the Free Software Foundation, either version 3 of 
  *	the License, or (at your option) any later version.
  *
  *	AMES- Web-Service is distributed in the hope that it will be useful,
  *	but WITHOUT ANY WARRANTY; without even the implied warranty of
  *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
  * 
  *  See the GNU General Public License for more details. 
  *
  *	You should have received a copy of the GNU General Public License
  *	along with this software. If not, see <http://www.gnu.org/licenses/>.
  *
  */
 
 import org.json.JSONArray;
 
 import de.kp.ames.web.core.regrep.JaxrClient;
 import de.kp.ames.web.function.BusinessImpl;
 import de.kp.ames.web.function.FncConstants;
 import de.kp.ames.web.http.RequestContext;
 import de.kp.ames.web.shared.constants.FormatConstants;
 import de.kp.ames.web.shared.constants.MethodConstants;
 
 public class UserServiceImpl extends BusinessImpl {
 
 	public UserServiceImpl() {
 		super();
 	}
 	
 	/* (non-Javadoc)
 	 * @see de.kp.ames.web.core.service.ServiceImpl#processRequest(de.kp.ames.web.core.RequestContext)
 	 */
 	public void processRequest(RequestContext ctx) {	
 
 		String methodName = this.method.getName();
 		if (methodName.equals(MethodConstants.METH_GET)) {
 			/*
 			 * Call get method
 			 */
 			doGetRequest(ctx);
 			
 		} else if (methodName.equals(MethodConstants.METH_SUBMIT)) {
 			/*
 			 * Call submit method
 			 */
 			doSubmitRequest(ctx);
 			
 		}
 
 	}
 
 	/* (non-Javadoc)
 	 * @see de.kp.ames.web.core.service.ServiceImpl#doGetRequest(de.kp.ames.web.http.RequestContext)
 	 */
 	public void doGetRequest(RequestContext ctx) {
 
 		String format = this.method.getAttribute(MethodConstants.ATTR_FORMAT);	
 		if (format == null) {
 			this.sendNotImplemented(ctx);
 			
 		} else {
 
 			try {
 
 				String item = this.method.getAttribute(MethodConstants.ATTR_ITEM);
 
 				String start = this.method.getAttribute(FncConstants.ATTR_START);
 				String limit = this.method.getAttribute(FncConstants.ATTR_LIMIT);
 
 				String content = getJSONResponse(format, item, start, limit);
 				sendJSONResponse(content, ctx.getResponse());
 
 			} catch (Exception e) {
 				this.sendBadRequest(ctx, e);
 
 			}
 			
 		}
 		
 	}
 	
 	/* (non-Javadoc)
 	 * @see de.kp.ames.web.core.service.ServiceImpl#doSubmitRequest(de.kp.ames.web.http.RequestContext)
 	 */
 	public void doSubmitRequest(RequestContext ctx) {
 
 		String data = this.getRequestData(ctx);
 		if (data == null) {
 			this.sendNotImplemented(ctx);
 			
 		} else {
 
 			try {
 				/*
 				 * JSON response
 				 */
 				String content = submit(data);
 				sendJSONResponse(content, ctx.getResponse());
 
 			} catch (Exception e) {
 				this.sendBadRequest(ctx, e);
 
 			}
 			
 		}
 		
 	}
 	
 	/**
 	 * @param community
 	 * @param format
 	 * @return
 	 * @throws Exception
 	 */
 	private String getJSONResponse(String format, String item, String start, String limit) throws Exception {
 
 		String content = null;
 		
 		/*
 		 * Login
 		 */		
 		JaxrClient.getInstance().logon(jaxrHandle);
 
 		if (format.equals(FormatConstants.FNC_FORMAT_ID_Object)) {
 			/*
 			 * Retrieve a certain user identified
 			 * by its unique identifier
 			 */
 			UserDQM dqm = new UserDQM(jaxrHandle);
 			JSONArray jArray = dqm.getUser(item);
 
 			/*
 			 * Render result
 			 */
 			content = render(jArray, format);
 	
 			
 		} else {
 			/*
 			 * Retrieve all users that are affiliated to 
 			 * a certain community of interest
 			 */ 
 			String community = item;
 			
 			UserDQM dqm = new UserDQM(jaxrHandle);
 			JSONArray jArray = dqm.getUsers(community);
 	
 			/*
 			 * Render result
 			 */
 			if ((start == null) || (limit == null)) {
 				content = render(jArray, format);
 	
 			} else {
 				content = render(jArray, start, limit, format);

 			}
 		
 		}
 		
 		/*
 		 * Logoff
 		 */
 		JaxrClient.getInstance().logoff(jaxrHandle);
 		return content;
 		
 	}
 
 	/**
 	 * Submit user information
 	 * 
 	 * @param data
 	 * @return
 	 * @throws Exception
 	 */
 	private String submit(String data) throws Exception {
 
 		String content = null;
 		
 		/*
 		 * Login
 		 */		
 		JaxrClient.getInstance().logon(jaxrHandle);
 		
 		UserLCM lcm = new UserLCM(jaxrHandle);
 		content = lcm.submitUser(data);
 		
 		/*
 		 * Logoff
 		 */
 		JaxrClient.getInstance().logoff(jaxrHandle);
 		return content;
 
 	}
 }
