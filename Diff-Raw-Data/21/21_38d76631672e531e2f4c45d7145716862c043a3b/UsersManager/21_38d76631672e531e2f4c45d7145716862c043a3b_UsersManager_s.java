 /*
  * The MIT License
  * 
  * Copyright (c) 2010 Petar Petrov
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package net.vexelon.myglob.users;
 
 import java.util.ArrayList;
 
 import net.vexelon.myglob.actions.ActionResult;
 import net.vexelon.myglob.configuration.Defs;
 import net.vexelon.myglob.crypto.PasswordEngine;
 import net.vexelon.myglob.crypto.PasswordEngineImpl2;
 
 import android.content.SharedPreferences;
 import android.text.TextUtils;
 
 public class UsersManager {
 	
 	private static UsersManager _INSTANCE = null;
 	
 	public static UsersManager getInstance() {
 		if (_INSTANCE == null)
 			_INSTANCE = new UsersManager();
 		
 		return _INSTANCE;
 	}
 	
 	private ArrayList<User> _users = new ArrayList<User>();
 	private PasswordEngine _passwordEngine = PasswordEngineImpl2.getInstance();
 
 	protected UsersManager() {
 		// empty
 	}
 	
 	public int size() {
 		return _users.size();
 	}
 	
 	public void reloadUsers(SharedPreferences prefs) {
 		// clear previous list
 		if (_users != null) {
 			_users.clear();
 			//_users = null;
 		}
 
 		// read num of users and load in list
 		int totalUsers = prefs.getInt(Defs.PREFS_USER_COUNT, 0);
 		for(int i = 0; i < totalUsers; i++) {
 			addUser(new User().load(i, prefs));
 		}
 	}
 	
 	public void save(SharedPreferences prefs) {
 		SharedPreferences.Editor editor = prefs.edit();
 		editor.clear(); // clean all previously saved user data
 		editor.putInt(Defs.PREFS_USER_COUNT, _users.size());
 		for(int i = 0; i < _users.size(); i++) {
 			_users.get(i).save(i, editor);
 		}
 		editor.commit();
 	}
 	
 	public User createUser(String accountName, String phoneNumber, AccountType accountType) {
 		User user = new User();
 		user.setAccountName(accountName);
 		user.setPhoneNumber(phoneNumber);
 		user.setAccountType(accountType);
 		return user;
 	}
 	
 	public void addUser(User user) {
 		_users.add(user);
 	}
 	
 	public boolean isUserExists(String phoneNumber) {
 		return getUserByPhoneNumber(phoneNumber) != null;
 	}
 	
 	/**
 	 * 
 	 * @param phoneNumber
 	 * @return {@link User} object or <code>null</code>
 	 */
 	public User getUserByPhoneNumber(String phoneNumber) {
 		for (User user : _users) {
 			if (user.getPhoneNumber().equals(phoneNumber)) {
 				return user;
 			}
 		}
 		
 		return null;
 	}
 	
 	public String[] getUsersPhoneNumbersList() {
 		String items[] = null;
 		
 		if (_users.size() > 0) {
 			items = new String[_users.size()];
 			for(int i = 0; i < _users.size(); i++)
 				items[i] = _users.get(i).getPhoneNumber();
 		}
 		
 		return items;
 	}
 	
	public void removeUser(String phoneNumber) {
		_users.remove(getUserByPhoneNumber(phoneNumber));
 	}
 	
 	public int getUsersCount() {
 		return _users.size();
 	}
 	
 	/**
 	 * 
 	 * @param user
 	 * @return Password string or <code>null</code>
 	 * @throws Exception
 	 */
 	public String getUserPassword(User user) throws Exception {
 		if (!TextUtils.isEmpty(user.getEncodedPassword())) {
 			return _passwordEngine.decodeAndDecrypt(user.getEncodedPassword());
 		}
 
 		return null;
 	}
 	
 	public void setUserPassword(User user, String rawPassword) throws Exception {
 		user.setEncodedPassword(_passwordEngine.encryptAndEncode(rawPassword));
 	}
 	
 	/**
 	 * 
 	 * @param user
 	 * @param result
 	 */
 	public void setUserResult(User user, ActionResult result) {
 		user.updateChecks(result.getCheckedOn(), 1);
 		user.updateTraffic(result.getCheckedOn(), result.getBytesCount());
 		user.setLastCheckDateTime(result.getCheckedOn().getTime());
 		
 		if (result.getString() != null && result.getString().length() > 0)
 			user.setLastCheckData(result.getString());
 	}
 	
 //	private byte[] loadKey() throws Exception {
 //		
 //		byte[] ret = null;
 ////		String savedKey = prefs.getString(Defs.PREFS_KEY_TEMPLATE + _id, "");
 ////		
 ////		if (TextUtils.isEmpty(savedKey)) {
 ////			// generate new key
 ////			Crypto crypto = CryptAESImpl.getInstance();
 ////			try {
 ////				// line
 ////				ret = crypto.createSecretKey();
 ////			} catch (Exception e) {
 ////				// Log.e(Defs.LOG_TAG, "Key could not be created!");
 ////				// AlertDialog alert = Utils.createAlertDialog(this,
 ////				// R.string.dlg_error_msg_create_key_failed,
 ////				// R.string.dlg_error_msg_title);
 ////				// alert.show();
 ////			}
 ////		}
 ////		else {
 ////			ret = Base64.decode(savedKey);
 ////		}
 //		
 //		return ret;
 //	}		
 
 }
