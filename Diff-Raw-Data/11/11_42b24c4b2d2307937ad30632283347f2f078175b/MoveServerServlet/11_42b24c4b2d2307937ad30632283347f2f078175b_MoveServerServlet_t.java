 package edu.colorado.csci3308.inventory.servlet;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import edu.colorado.csci3308.inventory.Server;
 import edu.colorado.csci3308.inventory.ServerDB;
 import edu.colorado.csci3308.inventory.ServerList;
 
 /**
  * Servlet implementation class MoveServerServlet
  */
 @WebServlet("/MoveServerServlet")
 public class MoveServerServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		ServerDB.setPath(request.getServletContext().getRealPath("WEB-INF/db/servers.db"));
 		Server s = ServerDB.getServerById(Integer.valueOf(request.getParameter("BoxID")));
 		s.setLocationId(Integer.valueOf(request.getParameter("Location_id")));
 		s.setRackId(Integer.valueOf(request.getParameter("rack_id")));
 		
		Integer maxHeight = Integer.valueOf(request.getParameter("MaxHeight"));
 		List<Integer> available = new ArrayList<Integer>();
 		List<Server> servers = new ArrayList<Server>();
 	   	ServerList sevs = ServerDB.getAllServers();
 	   	Iterator<Server> it = sevs.iterator();
 	   	while(it.hasNext())
 	   		servers.add(it.next());
 	   	
 	   	for(int i = 0; i < maxHeight; ++i)
 	   		available.add(new Integer(i));
 	   	for(Server sev: servers){
 	   		if(sev.getServerId() == (Integer)request.getSession(true).getAttribute("BoxID"))
 	   			continue;
 	   		else
 	   			if(sev.getLocationId().equals(s.getLocationId()) && sev.getRackId().equals(s.getRackId()))
 	   				for(Integer j = 0; j < sev.getChassisModel().getHeight(); ++j)
 	   					available.remove(new Integer(sev.getTopHeight() - j));
 	   	}
 	   	Integer height = Integer.valueOf(request.getParameter("HeightOnRack"));
	
		if(!available.contains(height)){
 			response.sendRedirect("Box.jsp");
 		} else {
 			s.setTopHeight(height);
 			ServerDB.updateServer(s);
 			response.sendRedirect("ServerList.jsp");
 		}
 	}
 
 }
