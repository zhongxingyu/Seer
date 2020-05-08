 /**
  * 
  */
 package net.mysocio.ui.executors.basic;
 
 import net.mysocio.data.IConnectionData;
 import net.mysocio.data.IDataManager;
 import net.mysocio.data.SocioUser;
 import net.mysocio.data.accounts.Account;
 import net.mysocio.data.management.DataManagerFactory;
 import net.mysocio.data.messages.GeneralMessage;
 import net.mysocio.ui.management.CommandExecutionException;
 import net.mysocio.ui.management.ICommandExecutor;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 /**
  * @author Aladdin
  *
  */
 public class LikeMessageExecutor implements ICommandExecutor {
 	private static final Logger logger = LoggerFactory.getLogger(LikeMessageExecutor.class);
 	@Override
 	public String execute(IConnectionData connectionData)
 			throws CommandExecutionException {
 		IDataManager dataManager = DataManagerFactory.getDataManager();
 		String accounts = connectionData.getRequestParameter("accounts");
 		String messageId = connectionData.getRequestParameter("messageId");
 		SocioUser user = dataManager.getObject(SocioUser.class, connectionData.getUserId());
 		GeneralMessage message = dataManager.getObject(GeneralMessage.class, messageId);
 		Account account = user.getMainAccount();
 		try {
 			account.like(message);
 		} catch (Exception e) {
 			logger.error("Couldn't post to account with id " + account.getId(),e);
 			throw new CommandExecutionException(e);
 		}
 		return "";
 	}
 }
