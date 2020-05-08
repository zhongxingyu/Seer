 package org.softwaresynthesis.mytalk.server.call.controller;
 
 import java.io.PrintWriter;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.Set;
 
 import org.softwaresynthesis.mytalk.server.AbstractController;
 import org.softwaresynthesis.mytalk.server.abook.IUserData;
 import org.softwaresynthesis.mytalk.server.call.ICallList;
 import org.softwaresynthesis.mytalk.server.dao.DataPersistanceManager;
 
 public class GetCallsController extends AbstractController{
 	/**
 	 * Recupera la
 	 * lista delle chiamate
 	 */
 	@Override
 	protected void doAction(HttpServletRequest request, HttpServletResponse response) throws IOException 
 	{
 		DataPersistanceManager dao = null;
 		String result = null;
 		PrintWriter writer = null;
 		String email= null;
 		IUserData user = null;
 		Set<ICallList> callsList = null;
 		Iterator<ICallList> callListIter = null;
 		ICallList callList = null;
 		
 		try
 		{
 			dao = getDAOFactory();
 			email = getUserMail();
 			user = dao.getUserData(email);
 			callsList = user.getCalls();
 			if (callsList != null){
 				callListIter = callsList.iterator();
 
 				result = "[";
 				while (callListIter.hasNext() == true)
 				{
 					callList = callListIter.next();
 					result += "{";
					result += "\"name\":\"" + callList.getUser().getName() + " " + callList.getUser().getSurname() + "\"";
 					result += ", \"start\":\"" + callList.getCall().getStart() + "\"";
 					result += ", \"caller\":\"" + callList.getCaller() + "\"";
 					result += "}";
 					if (callListIter.hasNext() == true)
 					{
 						result += ", ";
 					}
 				}
 				result += "]";
 			}
 			else
 			{
 				result = "[]";
 			}
 		}
 		catch (Exception ex)
 		{
 			result = "null";
 		}
 		finally
 		{
 			writer = response.getWriter();
 			writer.write(result);
 		}
 	}
 }
