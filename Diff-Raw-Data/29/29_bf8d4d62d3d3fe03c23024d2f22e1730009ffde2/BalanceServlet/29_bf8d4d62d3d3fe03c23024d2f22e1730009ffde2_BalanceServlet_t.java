 package com.tort.trade.journals;
 
import java.io.IOException;
import java.util.List;

 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 public class BalanceServlet  extends HttpServlet {
 
 	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		TransitionConversation conversation = (TransitionConversation) req.getSession().getAttribute("conversation");
		Action<List<GoodBalance>> action = new BalanceAction(conversation.getHibernateSession(), new JournalQueryFactoryImpl(), req.getParameterMap());
		
		JsonView<List<GoodBalance>> view = new JsonView<List<GoodBalance>>();
		resp.getOutputStream().write(view.render(action.act()));
 	}
 }
