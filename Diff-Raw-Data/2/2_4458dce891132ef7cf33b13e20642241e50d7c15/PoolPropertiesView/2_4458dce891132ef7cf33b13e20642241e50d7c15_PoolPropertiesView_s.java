 /**********************************************************************************
  * $URL$
  * $Id$
  ***********************************************************************************
  *
  * Copyright (c) 2008 Etudes, Inc.
  * 
  * Portions completed before September 1, 2008
  * Copyright (c) 2007, 2008 The Regents of the University of Michigan & Foothill College, ETUDES Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  **********************************************************************************/
 
 package org.etudes.mneme.tool;
 
 import java.io.IOException;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.etudes.ambrosia.api.Context;
 import org.etudes.ambrosia.util.ControllerImpl;
 import org.etudes.mneme.api.AssessmentPermissionException;
 import org.etudes.mneme.api.Pool;
 import org.etudes.mneme.api.PoolService;
 import org.sakaiproject.tool.api.ToolManager;
 import org.sakaiproject.util.Web;
 
 /**
  * The /pool_properties view for the mneme tool.
  */
 public class PoolPropertiesView extends ControllerImpl
 {
 	/** Our log. */
 	private static Log M_log = LogFactory.getLog(PoolPropertiesView.class);
 
 	/** Pool Service */
 	protected PoolService poolService = null;
 
 	/** tool manager reference. */
 	protected ToolManager toolManager = null;
 
 	/**
 	 * Shutdown.
 	 */
 	public void destroy()
 	{
 		M_log.info("destroy()");
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void get(HttpServletRequest req, HttpServletResponse res, Context context, String[] params) throws IOException
 	{
 		// pools sort, pool id
 		if (params.length != 4)
 		{
 			throw new IllegalArgumentException();
 		}
 
 		String poolsSort = params[2];
 		String pid = params[3];
 
 		// get the pool
 		Pool pool = this.poolService.getPool(pid);
 		if (pool == null)
 		{
 			// redirect to error
 			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.invalid)));
 			return;
 		}
 
 		// check that the user can manage this pool
 		if (!this.poolService.allowManagePools(pool.getContext()))
 		{
 			// redirect to error
 			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
 			return;
 		}
 
 		context.put("poolsSortCode", poolsSort);
 		context.put("pool", pool);
 
 		// render
 		uiService.render(ui, context);
 	}
 
 	/**
 	 * Final initialization, once all dependencies are set.
 	 */
 	public void init()
 	{
 		super.init();
 		M_log.info("init()");
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void post(HttpServletRequest req, HttpServletResponse res, Context context, String[] params) throws IOException
 	{
 		// pools sort, pool id
 		if (params.length != 4) throw new IllegalArgumentException();
 		String poolsSort = params[2];
 		String pid = params[3];
 
 		// get the pool
 		Pool pool = this.poolService.getPool(pid);
 		if (pool == null)
 		{
 			// redirect to error
 			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.invalid)));
 			return;
 		}
 
 		// if we start out, we are coming from an add
 		boolean mint = pool.getMint();
 
 		// check that the user can manage this pool
 		if (!this.poolService.allowManagePools(pool.getContext()))
 		{
 			// redirect to error
 			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
 			return;
 		}
 
 		// read the form
 		context.put("pool", pool);
 		String destination = uiService.decode(req, context);
 
 		try
 		{
 			this.poolService.savePool(pool);
 		}
 		catch (AssessmentPermissionException e)
 		{
 			// redirect to error
 			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
 			return;
 		}
 
 		// change destination to edit if this is new
 		if (mint)
 		{
 			// make sure we were not deleted
 			pool = this.poolService.getPool(pid);
 			if (pool != null)
 			{
 				// send them to edit pool
				destination = "/pool_edit/" + poolsSort + "/" + pool.getId();
 			}
 		}
 
 		res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, destination)));
 	}
 
 	/**
 	 * @param poolService
 	 *        the poolService to set
 	 */
 	public void setPoolService(PoolService poolService)
 	{
 		this.poolService = poolService;
 	}
 
 	/**
 	 * @param toolManager
 	 *        the toolManager to set
 	 */
 	public void setToolManager(ToolManager toolManager)
 	{
 		this.toolManager = toolManager;
 	}
 }
