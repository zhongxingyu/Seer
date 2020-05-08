 package org.softwaresynthesis.mytalk.server.abook.controller;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Iterator;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.softwaresynthesis.mytalk.server.AbstractController;
 import org.softwaresynthesis.mytalk.server.abook.IAddressBookEntry;
 import org.softwaresynthesis.mytalk.server.abook.IUserData;
 import org.softwaresynthesis.mytalk.server.dao.DataPersistanceManager;
 
 public class DeleteContactController extends AbstractController{
 
 	/**
 	 * Elimina un contatto
 	 * dalla propria rubrica
 	 */
 	@Override
 	protected void doAction(HttpServletRequest request, HttpServletResponse response) throws IOException 
 	{
 		String result = null;
 		PrintWriter writer = null;
 		IUserData myUser = null;
 		IUserData friend = null;
 		DataPersistanceManager dao = null;
 		String email = null;
 		Long idFriend = null;
 		IAddressBookEntry entry = null;
 		Iterator<IAddressBookEntry> iterator = null;
 		Set<IAddressBookEntry> entrys = null;
 		
 		try
 		{
 			dao = getDAOFactory();
 			email = getUserMail();
 			idFriend = Long.parseLong(request.getParameter("contactId"));
 			friend = dao.getUserData(idFriend);
 			myUser = dao.getUserData(email);
			result = "null";
 			if (friend != null)
 			{
 				entrys = myUser.getAddressBook();
 				iterator = entrys.iterator();
 				while (iterator.hasNext() == true)
 				{
 					entry = iterator.next();
 					if (entry.getContact().equals(friend) == true)
 					{
 						myUser.removeAddressBookEntry(entry);
 						dao.delete(entry);
						result = "true";
 					}
 				}
 				dao.update(myUser);
 				entrys = friend.getAddressBook();
 				iterator = entrys.iterator();
 				while (iterator.hasNext() == true)
 				{
 					entry = iterator.next();
 					if (entry.getContact().equals(myUser) == true)
 					{
 						friend.removeAddressBookEntry(entry);
 						dao.delete(entry);
 					}
 				}
 				dao.update(friend);
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
