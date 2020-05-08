 /**
  * 
  */
 package net.mysocio.authentication.facebook;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.mysocio.authentication.AbstractOauth2Manager;
 import net.mysocio.data.ContactsList;
 import net.mysocio.data.IDataManager;
 import net.mysocio.data.accounts.Account;
 import net.mysocio.data.accounts.facebook.FacebookAccount;
 import net.mysocio.data.accounts.facebook.FacebookFriendList;
 import net.mysocio.data.contacts.Contact;
 import net.mysocio.data.contacts.facebook.FacebookContact;
 import net.mysocio.data.management.DataManagerFactory;
 
 import org.scribe.builder.api.Api;
 import org.scribe.builder.api.FacebookApi;
 import org.scribe.model.Token;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import facebook4j.Facebook;
 import facebook4j.FacebookFactory;
 import facebook4j.Friend;
 import facebook4j.Friendlist;
 import facebook4j.ResponseList;
 import facebook4j.User;
 import facebook4j.auth.AccessToken;
 
 /**
  * @author Aladdin
  * 
  */
 public class FacebookAuthenticationManager extends AbstractOauth2Manager {
 	private static final Logger logger = LoggerFactory
 			.getLogger(FacebookAuthenticationManager.class);
 
 	protected Logger getLogger() {
 		return logger;
 	}
 
 	@Override
 	protected String getUserIdentifier() {
 		return "facebook";
 	}
 
 	@Override
 	protected Class<? extends Api> getApiClass() {
 		return FacebookApi.class;
 	}
 
 	@Override
 	protected Account getAccount(Token accessToken) throws Exception {
 		FacebookAccount account = initAccount(accessToken.getToken());
 		return account;
 	}
 
 	private FacebookAccount initAccount(String token) throws Exception {
 		logger.debug("starting authentication");
 		Facebook facebook = new FacebookFactory().getInstance();
 		facebook.setOAuthAccessToken(new AccessToken(token, null));
 		logger.debug("user data taken");
 		FacebookAccount account;
 		User user = facebook.getMe();
 		String email = user.getEmail();
 		IDataManager dataManager = DataManagerFactory.getDataManager();
 		checkUserInvitation(email, dataManager);
 		String id = user.getId();
 		account = (FacebookAccount) dataManager.getAccount(id);
 		if (account != null) {
 			logger.debug("Account found.");
 			account.setToken(token);
 			dataManager.saveObject(account);
 			return account;
 		}
 		account = new FacebookAccount();
 		account.setToken(token);
 		account.setAccountUniqueId(id);
 		account.setUserName(user.getName());
 		account.setUserpicUrl("http://graph.facebook.com/" + id + "/picture");
 		account.setEmail(email);
 		logger.debug("parsing friends list");
 		account.setContactsLists(parseFriendLists(facebook));
 		logger.debug("friends list parsed");
 		logger.debug("parsing friends");
 		List<Contact> friends = parseFriends(facebook);
 		dataManager.saveObjects(Contact.class, friends);
 		account.setContacts(friends);
 		logger.debug("friends parsed");
 		dataManager.saveObject(account);
 		return account;
 	}
 
 	private List<Contact> parseFriends(Facebook facebook) throws Exception {
 		ResponseList<Friend> facebookFriends = facebook.getFriends();
 		List<Contact> friends = new ArrayList<Contact>();
 		for (Friend facebookFriend : facebookFriends) {
 			FacebookContact friend = new FacebookContact();
 			friend.setFacebookId(facebookFriend.getId());
 			friend.setName(facebookFriend.getName());
 			friends.add(friend);
 		}
 		return friends;
 	}
 
 	private List<ContactsList> parseFriendLists(Facebook facebook) throws Exception {
 		ResponseList<Friendlist> friendlists = facebook.getFriendlists();
 		List<ContactsList> friendsLists = new ArrayList<ContactsList>();
 		for (Friendlist friendlist : friendlists) {
 			FacebookFriendList friendList = new FacebookFriendList();
 			friendList.setFacebookId(friendlist.getId());
 			friendList.setName(friendlist.getName());
 			try {
 				ResponseList<Friend> friendlistMembers = facebook.getFriendlistMembers(friendlist.getId());
 				for (Friend friend : friendlistMembers) {
 					friendList.getIds().add(friend.getId());
 				}
 			} catch (Exception e1) {
				logger.error("Failed to get friends list members " + friendlist.getName() + " for FB acc " + facebook.getEmail());
 			}
 			try {
 				DataManagerFactory.getDataManager().saveObject(friendList);
 			} catch (Exception e) {
				logger.error("Failed to save friends list " + friendlist.getName() + " for FB acc " + facebook.getEmail());
 			}
 			friendsLists.add(friendList);
 		}
 		return friendsLists;
 	}
 }
