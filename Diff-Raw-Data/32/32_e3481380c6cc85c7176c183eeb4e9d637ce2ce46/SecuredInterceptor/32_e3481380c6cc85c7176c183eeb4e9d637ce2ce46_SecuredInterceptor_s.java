 /*
  * Copyright 2013 Roy F. Donasco.
  * File Created on: 07-Mar-2013
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
  */
 package com.rdonasco.security.authorization.interceptors;
 
 import com.rdonasco.config.services.ConfigDataManagerVODecoratorRemote;
 import com.rdonasco.security.authentication.services.LoggedOnSession;
 import com.rdonasco.security.exceptions.SecurityAuthorizationException;
 import com.rdonasco.security.exceptions.SecurityManagerException;
 import com.rdonasco.security.authentication.services.LoggedOnSessionProvider;
 import com.rdonasco.security.services.SystemSecurityManagerRemote;
 import com.rdonasco.security.vo.AccessRightsVO;
 import com.rdonasco.security.vo.AccessRightsVOBuilder;
 import com.rdonasco.security.vo.UserSecurityProfileVO;
 import java.lang.annotation.Annotation;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.ejb.EJB;
 import javax.inject.Inject;
 import javax.interceptor.AroundInvoke;
 import javax.interceptor.Interceptor;
 import javax.interceptor.InvocationContext;
 
 /**
  *
  * @author Roy F. Donasco
  */
 @Secured
 @Interceptor
 public class SecuredInterceptor
 {
 
 	private static final Logger LOG = Logger.getLogger(SecuredInterceptor.class.getName());
 
 	@EJB
 	private SystemSecurityManagerRemote systemSecurityManager;
 
 	@EJB
 	private ConfigDataManagerVODecoratorRemote configDataManager;
 
 	private LoggedOnSessionProvider loggedOnSessionProvider;
 
 	private SecurityExceptionHandler securityExceptionHandler;
 
 	public SecuredInterceptor()
 	{
 		LOG.info("SecuredInterceptor instantiated");
 	}
 
 	@AroundInvoke
 	public Object interceptSecuredMethodCall(InvocationContext joinPoint) throws
 			Exception
 	{
 		Object returnValue;
 		if (isMethodAnnotatedBy(joinPoint, SecuredCapability.class))
 		{
 			returnValue = checkSecuredInvocation(joinPoint);
 		}
 		else
 		{
 			returnValue = joinPoint.proceed();
 		}
 		return returnValue;
 	}
 
 	private Object checkSecuredInvocation(InvocationContext joinPoint) throws
 			Exception
 	{
 		String action;
 		String resource = getResourceFromType(joinPoint);
 
 		if (resource.isEmpty())
 		{
 			resource = getResourceFromMethod(joinPoint);
 			action = getActionFromMethod(joinPoint);
 		}
 		else
 		{
 			action = getActionFromType(joinPoint);
 		}
 		Object returnValue = null;
 		InvocationEventType invocationEventType = getInvocationEventTypeFromMethod(joinPoint);
 		try
 		{
 			if (InvocationEventType.BEFORE == invocationEventType)
 			{
 				if (isEnabled())
 				{
 					LOG.log(Level.INFO, "executing doTheInvocationCheck before method {0}", joinPoint.getMethod().getName());
 					doTheInvocationCheck(resource, action);
 				}
 				returnValue = joinPoint.proceed();
 			}
 			else
 			{
 				returnValue = joinPoint.proceed();
 				if (isEnabled())
 				{
 					LOG.log(Level.INFO, "executing doTheInvocationCheck after method {0}", joinPoint.getMethod().getName());
 					doTheInvocationCheck(resource, action);
 				}
 			}
 		}
 		catch (Exception e)
 		{
 			boolean useExceptionHandler = true;
 			SecuredCapability securedCapabilityMethodAnnotation = joinPoint.getMethod().getAnnotation(SecuredCapability.class);
 			if (securedCapabilityMethodAnnotation != null)
 			{
 				useExceptionHandler = securedCapabilityMethodAnnotation.useExceptionHandler();
 			}
 
 			Throwable cause = e.getCause();
 			if (useExceptionHandler && null != securityExceptionHandler)
 			{
 				if (null != cause)
 				{
 					securityExceptionHandler.handleSecurityException(cause);
 				}
 				else
 				{
 					securityExceptionHandler.handleSecurityException(e);
 				}
 			}
 			else
 			{
 				if (null != cause && cause instanceof Exception)
 				{
 					throw (Exception) cause;
 				}
 				else
 				{
 					throw e;
 				}
 			}
 		}
 		return returnValue;
 	}
 
 	String getResourceFromType(InvocationContext joinPoint)
 	{
 		// get resource from the annotation
 		String resource = "";
 		SecuredCapability typeAnnotation = joinPoint.getTarget().getClass().getAnnotation(SecuredCapability.class);
 		if (null != typeAnnotation)
 		{
 			resource = typeAnnotation.resource();
 		}
 		return resource;
 	}
 
 	private <T extends Annotation> boolean isMethodAnnotatedBy(
 			InvocationContext joinPoint, Class<T> annotation)
 	{
 		return joinPoint.getMethod().getAnnotation(annotation) != null;
 	}
 
 	private String getResourceFromMethod(InvocationContext joinPoint)
 	{
 		String resource = "";
 		SecuredCapability methodAnnotation = joinPoint.getMethod().getAnnotation(SecuredCapability.class);
 		if (null != methodAnnotation)
 		{
 			resource = methodAnnotation.resource();
 		}
 		return resource;
 	}
 
 	private InvocationEventType getInvocationEventTypeFromMethod(
 			InvocationContext joinPoint)
 	{
 		InvocationEventType invocationEventType = InvocationEventType.BEFORE;
 		SecuredCapability methodAnnotation = joinPoint.getMethod().getAnnotation(SecuredCapability.class);
 		if (null != methodAnnotation)
 		{
 			invocationEventType = methodAnnotation.invocationEventType();
 		}
 		return invocationEventType;
 	}
 
 	private String getActionFromMethod(InvocationContext joinPoint)
 	{
 		String action = "";
 		SecuredCapability methodAnnotation = joinPoint.getMethod().getAnnotation(SecuredCapability.class);
 		if (null != methodAnnotation)
 		{
 			action = methodAnnotation.action();
 		}
 		return action;
 	}
 
 	private String getActionFromType(InvocationContext joinPoint)
 	{
 		String action = "";
 		SecuredCapability typeAnnotation = joinPoint.getTarget().getClass().getAnnotation(SecuredCapability.class);
 		if (null != typeAnnotation)
 		{
 			action = typeAnnotation.action();
 		}
 		return action;
 	}
 
 	private void doTheInvocationCheck(String resource, String action) throws
 			SecurityManagerException, SecurityAuthorizationException
 	{
 		LOG.log(Level.INFO, "executing doTheInvocationCheck resource = {0}, action= {1}", new Object[]
 		{
 			resource, action
 		});
 		if (!resource.isEmpty())
 		{
 			if (loggedOnSessionProvider.getLoggedOnSession() == null || !loggedOnSessionProvider.getLoggedOnSession().isLoggedOn())
 			{
 				throw new SecurityAuthorizationException("User is not logged on");
 			}
 			final LoggedOnSession loggedOnSession = loggedOnSessionProvider.getLoggedOnSession();
 			UserSecurityProfileVO userSecurityProfileVO = loggedOnSession.getLoggedOnUser();
 			AccessRightsVO accessRights = new AccessRightsVOBuilder()
 					.setActionAsString(action)
 					.setResourceAsString(resource)
 					.setUserProfileVO(userSecurityProfileVO)
 					.setApplicationID(loggedOnSession.getApplicationID())
 					.setApplicationToken(loggedOnSession.getApplicationToken())
 					.createAccessRightsVO();
 			systemSecurityManager.checkAccessRights(accessRights);
 		}
 		else
 		{
 			throw new SecurityAuthorizationException("Resource is null, please provide a resource");
 		}
 	}
 
 	@Inject
 	void setLoggedOnSession(LoggedOnSessionProvider loggedOnSessionProvider)
 	{
 		this.loggedOnSessionProvider = loggedOnSessionProvider;
 	}
 
 	@Inject
 	void setSecurityExceptionHandler(SecurityExceptionHandler exceptionHandler)
 	{
 		this.securityExceptionHandler = exceptionHandler;
 	}
 
 	void setSystemSecurityManager(
 			SystemSecurityManagerRemote systemSecurityManager)
 	{
 		this.systemSecurityManager = systemSecurityManager;
 	}
 
 	void setConfigDataManager(
 			ConfigDataManagerVODecoratorRemote configDataManager)
 	{
 		this.configDataManager = configDataManager;
 	}
 
 	// TODO: Get rid of this duplicate and use SystemSecurityManagerDecorator
 	boolean isEnabled()
 	{
 		Boolean isEnabled = Boolean.TRUE;
 		try
 		{
 			isEnabled = configDataManager.loadValue("/security/interceptor/enabled", Boolean.class, Boolean.FALSE);
 			if (!isEnabled)
 			{
 				LOG.log(Level.WARNING, "/security/interceptor/enabled = {0}", isEnabled);
 			}
 		}
 		catch (Exception e)
 		{
 			LOG.log(Level.WARNING, e.getMessage(), e);
 		}
 
 		return isEnabled;
 	}
 }
