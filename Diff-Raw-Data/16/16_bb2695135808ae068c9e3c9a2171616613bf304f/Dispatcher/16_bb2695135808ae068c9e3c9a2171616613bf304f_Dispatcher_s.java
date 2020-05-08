 package edu.unsw.triangle.controller;
 
 import java.io.IOException;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import edu.unsw.triangle.view.ViewResolver;
 
 
 public class Dispatcher 
 {
 	private final static Logger logger = Logger.getLogger(FrontController.class.getName());
 	private HttpServletRequest request;
 	private HttpServletResponse response;
 	
 	private Dispatcher(HttpServletRequest request, HttpServletResponse response)
 	{
 		this.request = request;
 		this.response = response;
 	}
 	
 	public static Dispatcher create(HttpServletRequest request, HttpServletResponse response) 
 	{
 		return new Dispatcher(request, response);
 	}
 	
 	public void doDispatch(ModelView modelView) throws ServletException, IOException
 	{
 		ViewResolver viewResolver = new ViewResolver();
 		String url = viewResolver.resolve(modelView.getViewName());
 		
 		// Add parameter to url request
 		Set<String> parameters = modelView.getParameterSet();
 		int count = 0;
 		for(String parameter : parameters)
 		{
 			if (count == 0)
 				url = url + "?";
 			else
 				url = url + "&";
			url = url + parameter + modelView.getParameter(parameter);
 		}
 		
 		if (url == null)
 		{
 			throw new ServletException("Cannot resolve view " + modelView.getViewName());
 		}
 		logger.info("Resolved view name " + modelView.getViewName() + " as " + url);
 		switch (modelView.getAction())
 		{
 		case FORWARD:
 			logger.info("Forwarding " + url);
 			request.getRequestDispatcher(url).forward(request, response);
 			break;
 		case REDIRECT:
 			logger.info("Redirecting " + url);
 			response.sendRedirect(url);
 			break;
 		default:
 			throw new ServletException("Model view for " + url + " does not contain valid action " + modelView.getAction() );
 		}	
 	}
 
 }
