 package jp.ikikko.bti.gdata;
 
 import java.util.Collection;
 
 import jp.ikikko.bti.backlogapi.BacklogApiClient;
 import jp.ikikko.bti.entity.Project;
 import jp.ikikko.bti.entity.User;
 
 import org.apache.xmlrpc.XmlRpcException;
 
 public class GdataValidator {
 
 	private Collection<User> registeredUsers;
 
 	/** Backlog に登録されているデータを取得してキャッシュしておく */
 	public void setRegisteredData(BacklogApiClient client, String key)
 			throws XmlRpcException {
 		Project project = client.getProject(key);
 
 		registeredUsers = client.getUsers(project.getId());
 	}
 
 	/** userId と一致するユーザが登録されているか否か */
 	public boolean isRegisteredUser(int userId) {
 		if (registeredUsers == null) {
 			throw new IllegalStateException(
 					"set data 'registeredUsers' before you call.");
 		}
 
		for (User user : registeredUsers) {
			if (user.getId() == userId) {
				return true;
			}
		}

		return false;
 	}
 
 	Collection<User> getRegisteredUsers() {
 		return registeredUsers;
 	}
 
 }
