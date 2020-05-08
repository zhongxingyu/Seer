 /**
  * This file is part of the Paxle project.
  * Visit http://www.paxle.net for more information.
  * Copyright 2007-2008 the original author or authors.
  * 
  * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0"). 
  * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
  * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt 
  * or in the file LICENSE.txt in the root directory of the Paxle distribution.
  * 
  * Unless required by applicable law or agreed to in writing, this software is distributed
  * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  */
 
 package org.paxle.gui.impl.servlets;
 
 import java.util.Dictionary;
 import java.util.Hashtable;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.velocity.Template;
 import org.apache.velocity.context.Context;
 import org.osgi.framework.Constants;
 import org.osgi.service.cm.ConfigurationException;
 import org.osgi.service.cm.ManagedService;
 import org.paxle.gui.ALayoutServlet;
 
 public class RobotsTxt extends ALayoutServlet implements ManagedService {
 
 	private static final long serialVersionUID = 1L;
 	
 	/** The configuration data for this class */
 	private Dictionary<String, Object> config = null;
 	
 	/** The text of the robots.txt file */
	public static final String ROBOTSTXT = "robotstxt-txt";
 	
 	@Override
 	public Template handleRequest( 
 			HttpServletRequest request,
 			HttpServletResponse response,
 			Context context 
 	) {
 
 		Template template = null;
 		try {
 			template = this.getTemplate("/resources/templates/RobotsTxt.vm");
 		} catch (Exception e) {
 			this.logger.error("Error",e);
 		}
 
 		context.put("layout", "plain.vm");
 		
 		context.put("robotstxt", config.get(ROBOTSTXT));
 		
 		return template;
 	}
 
 	@SuppressWarnings("unchecked")
 	public void updated(Dictionary properties) throws ConfigurationException {
 		logger.info("Updating configuration");
 		try {
 			if ( properties == null ) {
 				logger.warn("Updated configuration is null. Using defaults.");
 				properties = this.getDefaults();
 			}
 			this.config = properties;
 		} catch (Throwable e) {
 			logger.error("Internal exception during configuring", e);
 		}
 	}
 
 	/**
 	 * @return the default configuration of this service
 	 */
 	public Hashtable<String,Object> getDefaults() {
 		Hashtable<String,Object> defaults = new Hashtable<String,Object>();
 
 		defaults.put(ROBOTSTXT, "User-agent: *\nDisallow: /");
		defaults.put(Constants.SERVICE_PID, RobotsTxt.class.getName());
 
 		return defaults;
 	}
 
 }
