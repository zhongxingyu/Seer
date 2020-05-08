 package com.tort.trade.journals;
 
 import java.io.IOException;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 
 public class SaveAllServlet extends HttpServlet{
 		
	private final static JsonView<TransitionErrorTO> _view = new JsonView<TransitionErrorTO>();
 
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {				
 	
 		TransitionConversation conversation = (TransitionConversation) req.getSession().getAttribute(Constants.CONVERSATION);		
 		SaveAllAction action = new SaveAllAction(req.getParameterMap(), conversation.getHibernateSession(), new TransitionConverterLookupNoScoped());
 		
 		List<TransitionErrorTO> errors = action.act();
 		
 		resp.getOutputStream().write(_view.render(errors));
 	}
 }
