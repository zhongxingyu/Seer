 /*******************************************************************************
  * Copyright (c) 2013 Rudy D'hauwe @ Whizu
  * Licensed under the EUPL V.1.1
  *   
  * This Software is provided to You under the terms of the European 
  * Union Public License (the EUPL) version 1.1 as published by the 
  * European Union. Any use of this Software, other than as authorized 
  * under this License is strictly prohibited (to the extent such use 
  * is covered by a right of the copyright holder of this Software).
  *
  * This Software is provided under the License on an AS IS basis and 
  * without warranties of any kind concerning the Software, including 
  * without limitation merchantability, fitness for a particular purpose, 
  * absence of defects or errors, accuracy, and non-infringement of 
  * intellectual property rights other than copyright. This disclaimer 
  * of warranty is an essential part of the License and a condition for 
  * the grant of any rights to this Software.
  *   
  * For more  details, see <http://joinup.ec.europa.eu/software/page/eupl>.
  *
  * Contributors:
  *     2013 - Rudy D'hauwe @ Whizu - initial API and implementation
  *******************************************************************************/
 package org.whizu.server;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.whizu.jquery.EventHandler;
 import org.whizu.jquery.Input;
 import org.whizu.jquery.Request;
 import org.whizu.jquery.RequestContext;
 import org.whizu.jquery.Session;
 import org.whizu.ui.Application;
 import org.whizu.ui.WhizuUI;
 
 /**
  * @author Rudy D'hauwe
  */
 public class WhizuServlet extends HttpServlet {
 
 	private Log log = LogFactory.getLog(WhizuServlet.class);
 
 	/**
	 * The class name of the Application implementation class.
 	 */
 	public static final String INIT_PARAM_APPLICATION = "application";
 
 	/**
 	 * The class name of the Configuration implementation class.
 	 */
 	public static final String INIT_PARAM_CONFIG = "config";
 
 	private static final long serialVersionUID = 520182899630886403L;
 
 	private static final String WHIZU_SESSION = "whizu-session";
 
 	private static final Configuration DEFAULT_CONFIG = new Configuration() {
 		
 		@Override
 		protected void init() {
 		}
 	};
 	
 	private static String fromStream(InputStream in) throws IOException {
 		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
 		StringBuilder out = new StringBuilder();
 		String line;
 		while ((line = reader.readLine()) != null) {
 			out.append(line);
 		}
 		in.close();
 		return out.toString();
 	}
 
 	// not in session (shared across users)
 	@Deprecated
 	private Application application;
 
 	private Configuration config;
 
 	private Session assureUserSession(HttpSession session) {
 		Session userSession = (Session) session.getAttribute(WHIZU_SESSION);
 		if (userSession == null) {
 			userSession = new SessionImpl();
 			session.setAttribute(WHIZU_SESSION, userSession);
 		}
 		RequestImpl.get().setSession(userSession);
 		return userSession;
 	}
 
 	private void debug(String message) {
 		log.debug(message);
 		System.out.println(message);
 	}
 
 	@Override
 	public void init(ServletConfig config) throws ServletException {
 		RequestContext.init(new RequestContext() {
 
 			@Override
 			protected final Request getRequestImpl() {
 				return RequestImpl.get();
 			}
 		});
 
 		this.application = newInstance(config, INIT_PARAM_APPLICATION, null);
 		this.config = newInstance(config, INIT_PARAM_CONFIG, DEFAULT_CONFIG);
 	}
 
 	@SuppressWarnings("unchecked")
 	private <T> T newInstance(ServletConfig config, String param, T defaultValue) {
 		try {
 			String className = config.getInitParameter(param);
 			if (!StringUtils.isEmpty(className)) {
 				Class<T> clazz = (Class<T>) Class.forName(className);
 				return clazz.newInstance();
 			} else {
 				return defaultValue;
 			}
 		} catch (ClassNotFoundException e) {
 			throw new IllegalArgumentException(e);
 		} catch (InstantiationException e) {
 			throw new IllegalArgumentException(e);
 		} catch (IllegalAccessException e) {
 			throw new IllegalArgumentException(e);
 		} finally {
 		}
 	}
 
 	@Override
 	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException,
 			IOException {
 		long start = System.currentTimeMillis();
 		String content = "--";
 		try {
 			Session session = startRequest(request);
 			String id = request.getParameter("id");
 			if (id == null) {
 				debug("running setup:" + request.getRequestURI());
 				content = this.setup(request);
 			} else {
 				session.handleEvent(id);
 				content = RequestImpl.get().finish();
 			}
 		} finally {
 			try {
 				long end = System.currentTimeMillis();
 				debug("Server side completed in " + (end - start) + "ms");
 				debug("Sending script- " + content);
 				response.getWriter().print(content);
 				response.getWriter().close();
 			} catch (Exception exc) {
 				exc.printStackTrace();
 			}
 		}
 	}
 
 	private String setup(HttpServletRequest request) {
 		String uri = request.getRequestURI();
 		debug("uri:" + uri);
 		final Application app = config.getApplication(uri);
 		if (app != null) {
 			debug("Application to setup found:" + app);
 			// String content = app.create();
 			InputStream in = getClass().getResourceAsStream("/org/whizu/jquery/mobile/page.html");
 			// request.getServletContext().getResourceAsStream("/org/whizu/jquery/mobile/page.html");
 			debug("inputstream " + in);
 			try {
 				String content = fromStream(in);
 				final String id = app.getClass().getName();
 				content = content.replace("${id}", id);
 				RequestImpl.get().getSession().addClickListener(new EventHandler() {
 
 					@Override
 					public String getId() {
 						return id;
 					}
 
 					@Override
 					public void handleEvent() {
 						app.init(new WhizuUI());
 					}
 				});
 				return content;
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		application.init(new WhizuUI());
 		return RequestImpl.get().finish();
 	}
 
 	private Session startRequest(HttpServletRequest request) {
 		HttpSession session = request.getSession();
 		Session userSession = assureUserSession(session);
 		Map<String, String[]> parameterMap = request.getParameterMap();
 		Set<String> keys = parameterMap.keySet();
 		for (String key : keys) {
 			debug("P:" + key + "=" + parameterMap.get(key)[0]);
 			Input editable = userSession.getInput(key);
 			if (editable != null) {
 				debug("updating editable server-side " + editable);
 				editable.parseString(parameterMap.get(key)[0]);
 			}
 		}
 		return userSession;
 	}
 }
