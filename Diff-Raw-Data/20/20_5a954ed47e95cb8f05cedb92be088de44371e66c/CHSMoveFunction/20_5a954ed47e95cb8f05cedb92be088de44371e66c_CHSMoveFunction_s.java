 /**********************************************************************************
  * $URL$
  * $Id$
  ***********************************************************************************
  *
  * Copyright (c) 2003, 2004, 2005, 2006, 2007 The Sakai Foundation.
  *
  * Licensed under the Educational Community License, Version 1.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.opensource.org/licenses/ecl1.php
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  **********************************************************************************/
 
 package org.sakaiproject.sdata.tool.functions;
 
 import java.io.IOException;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.sakaiproject.content.api.ContentEntity;
 import org.sakaiproject.exception.IdUnusedException;
 import org.sakaiproject.exception.IdUsedException;
 import org.sakaiproject.exception.InUseException;
 import org.sakaiproject.exception.InconsistentException;
 import org.sakaiproject.exception.OverQuotaException;
 import org.sakaiproject.exception.PermissionException;
 import org.sakaiproject.exception.ServerOverloadException;
 import org.sakaiproject.exception.TypeException;
 import org.sakaiproject.sdata.tool.model.CHSNodeMap;
 import org.sakaiproject.sdata.tool.api.Handler;
 import org.sakaiproject.sdata.tool.api.ResourceDefinition;
 import org.sakaiproject.sdata.tool.api.SDataException;
 
 /**
  * <p>
  * Move the request entity to a specified folder. The destination folder is
  * specified by the <b>to</b> parameter.
  * </p>
  * <p>
  * If there is any problem with the mode an appropriate SDataException will be
  * thrown.
  * </p>
  * 
  * @author ieb
  */
 public class CHSMoveFunction extends CHSSDataFunction
 {
 
 	public static final String TO = "to";
 
 	private static final Log log = LogFactory.getLog(CHSMoveFunction.class);
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.sakaiproject.sdata.tool.api.SDataFunction#call(org.sakaiproject.sdata.tool.api.Handler,
 	 *      javax.servlet.http.HttpServletRequest,
 	 *      javax.servlet.http.HttpServletResponse, java.lang.Object,
 	 *      org.sakaiproject.sdata.tool.api.ResourceDefinition)
 	 */
 	public void call(Handler handler, HttpServletRequest request,
 			HttpServletResponse response, Object target, ResourceDefinition rp)
 			throws SDataException
 	{
 		SDataFunctionUtil.checkMethod(request.getMethod(), "POST");
 		String targetPath = request.getParameter(TO);
 		if (targetPath == null || targetPath.trim().length() == 0)
 		{
 			throw new SDataException(HttpServletResponse.SC_BAD_REQUEST,
 					"No Target folder for the move specified ");
 		}
		String reposotoryTargetPath = targetPath;
 		String repositorySourcePath = rp.getRepositoryPath();

		log.info("Moving " + repositorySourcePath + " to " + reposotoryTargetPath
 				+ " specified by " + targetPath);
 
 		try
 		{
			contentHostingService.moveIntoFolder(repositorySourcePath,
					reposotoryTargetPath);
 			response.setStatus(HttpServletResponse.SC_OK);
 
			ContentEntity ce = getEntity(handler, reposotoryTargetPath);
 			CHSNodeMap nm = new CHSNodeMap(ce, rp.getDepth(), rp);
 			try
 			{
 				handler.sendMap(request, response, nm);
 			}
 			catch (IOException e)
 			{
 				throw new SDataException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
 						"IO Error " + e.getMessage());
 			}
 
 		}
 		catch (PermissionException e)
 		{
 			logException("Permission Denied ", e);
 			throw new SDataException(HttpServletResponse.SC_FORBIDDEN, "Forbidden ");
 		}
 		catch (IdUnusedException e)
 		{
 			logException("IdUnusedException ", e);
 			throw new SDataException(HttpServletResponse.SC_BAD_REQUEST,
 					"Source Not found ");
 		}
 		catch (TypeException e)
 		{
 			logException("TypeException ", e);
 			throw new SDataException(HttpServletResponse.SC_FORBIDDEN,
 					"Target must be a collection ");
 		}
 		catch (InUseException e)
 		{
 			logException("InUseException ", e);
 			throw new SDataException(HttpServletResponse.SC_FORBIDDEN,
 					"Target ID in use ");
 		}
 		catch (OverQuotaException e)
 		{
 			logException("OverQuotaException ", e);
 			throw new SDataException(HttpServletResponse.SC_FORBIDDEN, "Overquota ");
 		}
 		catch (IdUsedException e)
 		{
 			logException("IdUsedException ", e);
 			throw new SDataException(HttpServletResponse.SC_BAD_REQUEST,
 					"Target folder already contains source ");
 		}
 		catch (InconsistentException e)
 		{
 			logException("InconsistentException ", e);
 			throw new SDataException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
 					"Inconsistancy ");
 		}
 		catch (ServerOverloadException e)
 		{
 			logException("ServerOverloadException ", e);
 			throw new SDataException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
 					"Server Overload ");
 		}
 	}
 
 
 }
