 package org.softwaresynthesis.mytalk.server.call.controller;
 
 import java.io.PrintWriter;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.Set;
 
 import org.softwaresynthesis.mytalk.server.AbstractController;
 import org.softwaresynthesis.mytalk.server.abook.IUserData;
 import org.softwaresynthesis.mytalk.server.call.ICall;
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
 		ICall call = null;
 		ICallList callsMy = null;
 		ICallList callsOther = null;
 		Iterator<ICallList> callsMyIterator;
 		Iterator<ICallList> callsOtherIterator;
 		IUserData user = null;
 		PrintWriter writer = null;
 		Set<ICallList> setMy = null;
 		Set<ICallList> setOther = null;
 		String mail = null;
 		String result = null;
 		try
 		{
 			dao = this.getDAOFactory();
 			mail = this.getUserMail();
 			user = dao.getUserData(mail);
 			setMy = user.getCalls();
 			if (setMy != null)
 			{
 				callsMyIterator = setMy.iterator();
 				result = "[";
 				while (callsMyIterator.hasNext() == true)
 				{
 					callsMy = callsMyIterator.next();
 					call = dao.getCall(callsMy.getCall().getId());
 					if (call != null)
 					{
 						setOther = call.getCalls();
 						if (setOther != null)
 						{
 							callsOtherIterator = setOther.iterator();
 							while (callsOtherIterator.hasNext() == true)
 							{
 								callsOther = callsOtherIterator.next();
 								if (callsOther.getUser().equals(user) == false)
 								{
 									//Costruisco risultato
 									result += "{";
 									result += "\"id\":\"" + callsOther.getUser().getId() + "\"";
									result += ", \"email\":\"" + callsOther.getUser().getMail() + "\"";
 									result += ", \"start\":\"" + call.getStart() + "\"";
 									result += ", \"caller\":" + callsOther.getCaller();
 									result += "}";
 								}
 							}
 						}
 					}
 					if (callsMyIterator.hasNext() == true)
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
