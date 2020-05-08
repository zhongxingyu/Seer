 package com.sharkmob.rest;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 public class RestServer implements Filter
 {
 	public static enum RequestMethod
 	{
 		GET, POST, PUT, DELETE;
 	}
 
 	private RequestMethod requestMethod;
 	private HttpServletRequest request;
 	private HttpServletResponse response;
 	private IRoutes routes;
 	private RestRouter router = RestRouter.getInstance();
 	private IResource resource;
 	private String resourceId;
 
 	@Override
 	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException
 	{
 		request = (HttpServletRequest) req;
 		response = (HttpServletResponse) res;
 
 		if (request.getParameter("method") != null)
 		{
 			requestMethod = RequestMethod.valueOf(request.getParameter("method"));
 		}
 		else
 		{
 			requestMethod = RequestMethod.valueOf(request.getMethod());
 		}
 
 		String path = request.getRequestURI();
 
		if (isValidPath(path))
 		{
 			try
 			{
 				resource = router.getResource(filterPath(path));
 
 				if (resource != null)
 				{
 					resource.setParams(new HashMap<String, String>());
 					resource.getParams().putAll(getRequestParams(request));
 					if (resourceId != null) resource.getParams().put("id", resourceId);
 
 					switch (requestMethod)
 					{
 					case GET:
 						resource.doGet(request, response);
 						response.setStatus(HttpServletResponse.SC_OK);
 						break;
 					case POST:
 						resource.doPost(request, response);
 						response.setStatus(HttpServletResponse.SC_ACCEPTED);
 						break;
 					case PUT:
 						resource.doPut(request, response);
 						response.setStatus(HttpServletResponse.SC_CREATED);
 						break;
 					case DELETE:
 						resource.doDelete(request, response);
 						response.setStatus(HttpServletResponse.SC_GONE);
 						break;
 					}
 				}
 				else
 				{
 					// this is not a valid resource or we had trouble
 					// instantiating it!
 					((HttpServletResponse) res).sendError(HttpServletResponse.SC_NOT_FOUND);
 				}
 
 			}
 			catch (RestException rex)
 			{
 				response.setStatus(rex.statusCode);
 			}
 			catch (Exception ex)
 			{
 				// chain.doFilter(req, res);
 				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
 			}
 		}
 
 		else
 		{
 			// move on to the next request
 			// chain.doFilter(req, res);
 			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
 		}
 
 	}
 
 	@Override
 	public void destroy()
 	{
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public void init(FilterConfig config) throws ServletException
 	{
 		String routesParameter = config.getInitParameter("Routes");
 		Class routesClazz = null;
 		try
 		{
 			routesClazz = Class.forName(routesParameter);
 			routes = (IRoutes) routesClazz.newInstance();
 			routes.setupRoutes(router);
 		}
 		catch (InstantiationException e)
 		{
 			e.printStackTrace();
 		}
 		catch (IllegalAccessException e)
 		{
 			e.printStackTrace();
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 
 	}
 
 	/***
 	 * takes the path passed in the url and replaces any numbers with pound (#)
 	 * signs
 	 * 
 	 * @param path
 	 * @return filtered path
 	 */
 	public String filterPath(String path)
 	{
		resourceId = null;
 		for (String partial : path.split("/"))
 		{
 			if (partial.matches("[0-9]+"))
 			{
 				resourceId = partial;
 			}
 		}
 
 		return path.replaceAll("/[0-9]+", "/#");
 	}
 
 	@SuppressWarnings("unchecked")
 	private HashMap<String, String> getRequestParams(HttpServletRequest request)
 	{
 		HashMap<String, String> params = new HashMap<String, String>();
 		for (Iterator iterator = request.getParameterMap().entrySet().iterator(); iterator.hasNext();)
 		{
 			Map.Entry entry = (Map.Entry) iterator.next();
 			params.put(entry.getKey().toString(), ((String[])entry.getValue())[0]);
 		}
 		return params;
 	}
	
	private boolean isValidPath(String path)
	{
		return (path != null 
				&& !("/favicon.ico").equals(path)
				&& !("").equals(path)
				);
	}
 }
