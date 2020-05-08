 package com.dougkoellmer.server.thirdparty.servlets;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 public class RedirectServlet extends HttpServlet
 {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	
 	private final String[] m_redirects = 
 	{
 		"/portfolio",
 		"/resume",
 		"/games/feed_the_bear",
 		"/games/pressure",
 		"/games/pressure_and_heat",
 		"/games/radioactive_decay",
		"/games/coriolis",
		"/games/atmospheric_layers",
 		"/games"
 	};
 
 	@Override
 	public void init()
 	{
 	}
 	
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
 	{
 		for( int i = 0; i < m_redirects.length; i++ )
 		{
 			if( req.getRequestURI().equals(m_redirects[i]) )
 			{
 				resp.sendRedirect(m_redirects[i] + "/");
 			}
 		}
 		
 		resp.sendRedirect("/");
 	}
 	
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
 	{
 		resp.sendRedirect("/");
 	}
 }
