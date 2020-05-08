 /**
  * 
  */
 package net.mysocio.ui.executors.basic;
 
 import java.util.List;
 
 import net.mysocio.data.CorruptedDataException;
 import net.mysocio.data.IConnectionData;
 import net.mysocio.data.IDataManager;
 import net.mysocio.data.UserContact;
 import net.mysocio.data.management.DataManagerFactory;
 import net.mysocio.ui.data.objects.ContactLine;
 import net.mysocio.ui.management.CommandExecutionException;
 import net.mysocio.ui.management.ICommandExecutor;
 import net.mysocio.ui.managers.basic.AbstractUiManager;
 import net.mysocio.ui.managers.basic.DefaultUiManager;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author Aladdin
  *
  */
 public class GetContactsExecutor implements ICommandExecutor {
 	private static final Logger logger = LoggerFactory.getLogger(GetContactsExecutor.class);
 	/* (non-Javadoc)
 	 * @see net.mysocio.ui.management.ICommandExecutor#execute(net.mysocio.data.IConnectionData)
 	 */
 	@Override
 	public String execute(IConnectionData connectionData)
 			throws CommandExecutionException {
 		String page = "";
 		String userId = connectionData.getUserId();
 		IDataManager dataManager = DataManagerFactory.getDataManager();
 		List<UserContact> contacts = dataManager.getContacts(userId);
 		AbstractUiManager uiManager = new DefaultUiManager();
 		String contactHTML;
 		try {
 			contactHTML = uiManager.getPage(ContactLine.CATEGORY, ContactLine.NAME, userId);
 		} catch (CorruptedDataException e) {
 			logger.error("Failed showing contacts",e);
 			throw new CommandExecutionException(e);
 		}
 		for (UserContact contact : contacts) {
 			String currentContactHTML = contactHTML;
			currentContactHTML = currentContactHTML.replace("contact.userpic", "images/portrait.jpg");
 			currentContactHTML = currentContactHTML.replace("contact.username", contact.getContacts().get(0).getName());
 			page += currentContactHTML;
 		}
 		return page;
 	}
 }
