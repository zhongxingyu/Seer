 /**
  * Copyright 2013 Simon Curd <simoncurd@gmail.com>
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *     http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.hula.web.servlet;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.inject.Inject;
 import com.google.inject.Injector;
 import com.google.inject.Singleton;
 import com.hula.lang.runtime.HulaPlayer;
 import com.hula.lang.runtime.HulaPlayerImpl;
 import com.hula.lang.runtime.ServiceProxy;
 import com.hula.web.WebConstants;
 import com.hula.web.model.HulaWebContext;
 import com.hula.web.model.RunResult;
 import com.hula.web.model.runresult.ForwardRunResult;
 import com.hula.web.response.ResponseProcessor;
 import com.hula.web.runtime.GuiceServiceProxy;
 import com.hula.web.service.script.ScriptService;
 import com.hula.web.util.ParameterUtils;
 
 /**
  * Servlet responsible for executing Hula scripts and processing the
  * response.
  */
 @Singleton
 public class HulaWebServlet extends HttpServlet
 {
 	private static Logger logger = LoggerFactory.getLogger(HulaWebServlet.class);
 	private ScriptService scriptService = null;
 	private ServiceProxy serviceProxy = null;
 
 	// alternative strategies for processing responses
	private Map<String,ResponseProcessor> responseProcessors;
 
 	@Inject
	public HulaWebServlet(ScriptService scriptService, Map<String,ResponseProcessor> responseProcessors, Injector injector)
 	{
 		this.scriptService = scriptService;
 		this.responseProcessors = responseProcessors;
 		this.serviceProxy = new GuiceServiceProxy(injector);
 	}
 
 	/**
 	 * Handle the incoming request
 	 * 
 	 * @param request The request to handle
 	 * @param response The response object
 	 * @throws IOException
 	 * @throws ServletException
 	 */
 	public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
 	{
 		// lookup the script to run
 		String scriptName = (String) request.getAttribute(WebConstants.ScriptName);
 
 		if (scriptName == null || scriptName.trim().equals(""))
 		{
 			scriptName = "default";
 		}
 
 		logger.info("execute [" + scriptName + "]");
 
 		// setup context
 		HulaWebContext hctx = new HulaWebContext();
 		hctx.setRequest(request);
 		hctx.setResponse(response);
 
 		Map<String, Object> requestParameters = ParameterUtils.convertParameterMap(request.getParameterMap());
 		hctx.getParameters().putAll(requestParameters);
 		hctx.setParameter(WebConstants.ScriptName, scriptName);
 
 		// build a list of scripts to execute
 		List<String> scripts = new ArrayList<String>();
 		if (scriptService.hasScript("common"))
 		{
 			scripts.add("common");
 		}
 		scripts.add(scriptName);
 
 		// execute scripts
 		for (String scriptItem : scripts)
 		{
			RunResult runResult = runScript(scriptItem, hctx, request, response);
 			logger.info("script [" + scriptItem + "] finished");
 
 			if (runResult != null)
 			{
 				if (runResult instanceof ForwardRunResult)
 				{
					ForwardRunResult fwd = (ForwardRunResult) hctx.getRunResult();
					this.runScript(fwd.getScriptName(), hctx, request, response);
 					return;
 				}
 				else
 				{
 					ResponseProcessor processor = responseProcessors.get(runResult.getClass().getSimpleName());
					processor.process(hctx, request, response);
 					return;
 				}
 			}
 		}

 	}
 
 	/**
 	 * Run the Hula script and return the {@link com.hula.web.model.RunResult}
 	 * 
 	 * @param scriptName The script to execute
 	 * @param hulaWebContext The context for the script
 	 * @param request The request
 	 * @param response The response
 	 * @return The RunResult from the script
 	 * @throws IOException
 	 */
 	protected RunResult runScript(String scriptName, HulaWebContext hulaWebContext, HttpServletRequest request, HttpServletResponse response) throws IOException
 	{
 		try
 		{
 			HulaPlayer player = new HulaPlayerImpl(scriptService, serviceProxy);
 			player.run(scriptName, hulaWebContext);
 		}
 		catch (Throwable e)
 		{
 			logger.error("error running script", e);
 			throw new RuntimeException(e);
 		}
 
 		return hulaWebContext.getRunResult();
 	}
 
 	@Override
 	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
 	{
 		handleRequest(request, response);
 	}
 
 	@Override
 	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
 	{
 		handleRequest(request, response);
 	}
 }
