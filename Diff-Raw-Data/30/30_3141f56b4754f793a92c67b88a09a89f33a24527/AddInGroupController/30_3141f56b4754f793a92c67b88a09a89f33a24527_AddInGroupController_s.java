 package org.softwaresynthesis.mytalk.server.abook.controller;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.softwaresynthesis.mytalk.server.AbstractController;
 import org.softwaresynthesis.mytalk.server.abook.AddressBookEntry;
 import org.softwaresynthesis.mytalk.server.abook.IAddressBookEntry;
 import org.softwaresynthesis.mytalk.server.abook.IGroup;
 import org.softwaresynthesis.mytalk.server.abook.IUserData;
 import org.softwaresynthesis.mytalk.server.dao.DataPersistanceManager;
 
 public class AddInGroupController extends AbstractController{
 	/**
 	 * Aggiunge un contatto in un gruppo
 	 * della propria rubrica
 	 */
 	@Override
 	protected void doAction(HttpServletRequest request, HttpServletResponse response) throws IOException 
 	{
 		DataPersistanceManager dao = null;
 		String result = null;
 		PrintWriter writer = null;
 		Long contactId = null;
 		Long groupId = null;
 		String email = null;
 		IUserData myUser = null;
 		IUserData friend = null;
 		IGroup group = null;
 		IAddressBookEntry entry = null;
 		
 		try
 		{
 			dao = getDAOFactory();
 			contactId = Long.parseLong(request.getParameter("contactId"));
 			groupId = Long.parseLong(request.getParameter("groupId"));
 			email = getUserMail();
 			myUser = dao.getUserData(email);
 			friend = dao.getUserData(contactId);
 			group = dao.getGroup(groupId);
 			if (group != null)
 			{
 				entry = new AddressBookEntry();
 				entry.setBlocked(false);
 				entry.setOwner(myUser);
 				entry.setContact(friend);
 				entry.setGroup(group);
 				myUser.addAddressBookEntry(entry);
				dao.insert(entry);
				dao.update(myUser);
				result = "true";
			}
			else
			{
				result = "null";
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
