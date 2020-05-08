 package com.intalker.borrow.cloud;
 
 import java.security.MessageDigest;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 import org.json.JSONObject;
 
 import android.content.Context;
 
 import com.intalker.borrow.cloud.CloudAPIAsyncTask.ICloudAPITaskListener;
 import com.intalker.borrow.data.AppData;
 import com.intalker.borrow.data.UserInfo;
 import com.intalker.borrow.util.DBUtil;
 import com.intalker.borrow.util.JSONUtil;
 
 public class CloudAPI {
	public final static String API_BaseURL = "http://services.sketchbook.cn/test/openshelf/api/index.php?op=";
 
 	// API operations
 	public final static String API_Login = "Login";
 	public final static String API_SignUp = "SignUp";
 	public final static String API_GetUserInfo = "GetUserInfo";
 	public final static String API_UploadBooks = "UploadBooks";
 	public final static String API_GetOwnedBooks = "GetOwnedBooks";
 	public final static String API_SynchronizeOwnedBooks = "SynchronizeOwnedBooks";
 	public final static String API_GetFollowings = "GetFollowings";
 	public final static String API_DeleteBook = "DeleteBook";
 	public final static String API_GetAllUsers = "GetAllUsers";
 	public final static String API_Follow = "Follow";
 	public final static String API_UnFollow = "UnFollow";
 	public final static String API_GetBooksByOwner = "GetBooksByOwner";
 	
 	// API params
 	public final static String API_Email = "&email=";
 	public final static String API_LocalPwd = "&localpwd=";
 	public final static String API_NickName = "&nickname=";
 	public final static String API_TOKEN = "&sessionid=";
 	public final static String API_ISBN = "&isbn=";
 	public final static String API_FriendId = "&friendid=";
 	public final static String API_OwnerId = "&ownerid=";
 	
 	public final static String API_POST_BookInfoList = "bookinfolist";
 	
 	// DB keys
 	// Book
 	public final static String DB_Book_OwnerId = "ownerid";
 	public final static String DB_Book_ISBN = "isbn";
 	public final static String DB_Book_Quantity = "quantity";
 	public final static String DB_Book_Description = "description";
 	public final static String DB_Book_PublicLevel = "publiclevel";
 	public final static String DB_Book_Status = "status";
 	
 	// User
 	public final static String DB_User_Id = "id";
 	public final static String DB_User_NickName = "nickname";
 	public final static String DB_User_Email = "email";
 	public final static String DB_User_RegTime = "registertime";
 	public final static String DB_User_Permission = "permission";
 	
 	// Friend
 	public final static String DB_Friend_Alias = "alias";
 	public final static String DB_Friend_Group = "group";
 	public final static String DB_Friend_Status = "status";
 	public final static String DB_Friend_ConnectTime = "connecttime";
 	
 	// Server Return code
 	public final static String ServerReturnCode_Successful = "Successful";
 	public final static String ServerReturnCode_NoSuchUser = "NoSuchUser";
 	public final static String ServerReturnCode_BadSession = "BadSession";
 	public final static String ServerReturnCode_WrongUserNameOrPwd = "WrongUserNameOrPwd";
 	public final static String ServerReturnCode_UserNameOccupied = "UserNameOccupied";
 	public final static String ServerReturnCode_EmptyResult = "EmptyResult";
 
 	// API return code
 	public final static int Return_Unset = -1;
 	public final static int Return_OK = 0;
 	public final static int Return_TimeOut = 1;
 	public final static int Return_NoNetworkConnection = 2;
 	public final static int Return_NoSuchUser = 3;
 	public final static int Return_WrongUserNameOrPassword = 4;
 	public final static int Return_UserNameOccupied = 5;
 	public final static int Return_BadToken = 6;
 	public final static int Return_NetworkError = 7;
 	public final static int Return_UnknownError = 100;
 
 	// Token
 	public static String CloudToken = "";
 
 	public static String md5(String val) {
 		MessageDigest messageDigest = null;
 		try {
 			messageDigest = MessageDigest.getInstance("MD5");
 			messageDigest.reset();
 			messageDigest.update(val.getBytes("UTF-8"));
 		} catch (Exception e) {
 		}
 		byte[] byteArray = messageDigest.digest();
 		StringBuffer md5StrBuff = new StringBuffer();
 		for (int i = 0; i < byteArray.length; i++) {
 			if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)
 				md5StrBuff.append("0").append(
 						Integer.toHexString(0xFF & byteArray[i]));
 			else
 				md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
 		}
 		return md5StrBuff.toString();
 	}
 
 	public static UserInfo getUserInfoFromJSON(String str) {
 		try {
 			JSONObject jsonObject = new JSONObject(str);
 
 			String id = jsonObject.getString(DB_User_Id);
 			String nickName = jsonObject.getString(DB_User_NickName);
 			String email = jsonObject.getString(DB_User_Email);
 			String regTime = jsonObject.getString(DB_User_RegTime);
 			String permission = jsonObject.getString(DB_User_Permission);
 
 			UserInfo userInfo = new UserInfo(id, nickName, email, regTime,
 					permission);
 
 			return userInfo;
 		} catch (Exception ex) {
 		}
 		return null;
 	}
 
 	public static int _login(String url) {
 		HttpGet getReq = new HttpGet(url);
 		try {
 			HttpResponse httpResponse = new DefaultHttpClient().execute(getReq);
 			if (httpResponse.getStatusLine().getStatusCode() == 200) {
 				String strResult = EntityUtils.toString(httpResponse
 						.getEntity());
 				if (setAccessToken(strResult)) {
 					return CloudAPI.Return_OK;
 				} else if (strResult
 						.compareTo(CloudAPI.ServerReturnCode_WrongUserNameOrPwd) == 0) {
 					return CloudAPI.Return_WrongUserNameOrPassword;
 				} else {
 					return CloudAPI.Return_NetworkError;
 				}
 			} else {
 				return CloudAPI.Return_NetworkError;
 			}
 		} catch (Exception e) {
 			return CloudAPI.Return_NetworkError;
 		}
 	}
 
 	public static boolean isLoggedIn() {
 		return null != UserInfo.getCurLoggedinUser();
 	}
 
 	public static boolean setAccessToken(String token) {
 		if (token.length() == 36) {
 			if (token.compareTo(CloudToken) != 0) {
 				CloudToken = token;
 				DBUtil.saveToken(token);
 			}
 			return true;
 		}
 		return false;
 	}
 
 	public static int _signUp(String url) {
 		HttpGet getReq = new HttpGet(url);
 		try {
 			HttpResponse httpResponse = new DefaultHttpClient().execute(getReq);
 			if (httpResponse.getStatusLine().getStatusCode() == 200) {
 				String strResult = EntityUtils.toString(httpResponse
 						.getEntity());
 
 				if (setAccessToken(strResult)) {
 					return CloudAPI.Return_OK;
 				} else {
 					if (strResult
 							.compareTo(CloudAPI.ServerReturnCode_UserNameOccupied) == 0) {
 						return CloudAPI.Return_UserNameOccupied;
 					} else {
 						return CloudAPI.Return_UnknownError;
 					}
 				}
 			} else {
 				return CloudAPI.Return_NetworkError;
 			}
 		} catch (Exception e) {
 			return CloudAPI.Return_UnknownError;
 		}
 	}
 
 	public static int _getLoggedInUserInfo() {
 		String url = CloudAPI.API_BaseURL + CloudAPI.API_GetUserInfo
 				+ CloudAPI.API_TOKEN + CloudAPI.CloudToken;
 		HttpGet getReq = new HttpGet(url);
 		try {
 			HttpResponse httpResponse = new DefaultHttpClient().execute(getReq);
 			if (httpResponse.getStatusLine().getStatusCode() == 200) {
 				String strResult = EntityUtils.toString(httpResponse
 						.getEntity());
 
 				UserInfo userInfo = getUserInfoFromJSON(strResult);
 
 				if (null != userInfo) {
 					UserInfo.setCurLoginUser(userInfo);
 					return CloudAPI.Return_OK;
 				} else {
 					if (strResult
 							.compareTo(CloudAPI.ServerReturnCode_NoSuchUser) == 0) {
 						return CloudAPI.Return_NoSuchUser;
 					} else if (strResult
 							.compareTo(CloudAPI.ServerReturnCode_BadSession) == 0) {
 						return CloudAPI.Return_BadToken;
 					} else {
 						return CloudAPI.Return_UnknownError;
 					}
 				}
 			} else {
 				return CloudAPI.Return_NetworkError;
 			}
 		} catch (Exception e) {
 			return CloudAPI.Return_NetworkError;
 		}
 	}
 	
 	public static int _uploadBooks()
 	{
 		String url = API_BaseURL + API_UploadBooks + CloudAPI.API_TOKEN + CloudAPI.CloudToken;;
 		String strResult;
 		HttpPost httpRequest = new HttpPost(url);
 		try {
 			JSONObject data = JSONUtil.makeBookInfoListUploadData();
 			String dataStr = data.toString();
 			StringEntity se = new StringEntity(dataStr);
 			httpRequest.setEntity(se);
 			HttpResponse httpResponse = new DefaultHttpClient()
 					.execute(httpRequest);
 			if (httpResponse.getStatusLine().getStatusCode() == 200) {
 				strResult = EntityUtils.toString(httpResponse.getEntity());
 				if(strResult.compareTo(CloudAPI.ServerReturnCode_Successful) == 0)
 				{
 					return CloudAPI.Return_OK;
 				}
 				else if(strResult.compareTo(CloudAPI.ServerReturnCode_BadSession) == 0)
 				{
 					return CloudAPI.Return_BadToken;
 				}
 				else
 				{
 					return CloudAPI.Return_UnknownError;
 				}
 			} else {
 				return CloudAPI.Return_NetworkError;
 			}
 		} catch (Exception ex) {
 			return CloudAPI.Return_NetworkError;
 		}
 	}
 	
 	public static int _getOwnedBooks()
 	{
 		String url = API_BaseURL + API_GetOwnedBooks + CloudAPI.API_TOKEN + CloudAPI.CloudToken;;
 		HttpGet getReq = new HttpGet(url);
 		try {
 			HttpResponse httpResponse = new DefaultHttpClient().execute(getReq);
 			if (httpResponse.getStatusLine().getStatusCode() == 200) {
 				String strResult = EntityUtils.toString(httpResponse
 						.getEntity());
 				if (null != strResult && strResult.length() > 0) {
 					if (strResult.compareTo(CloudAPI.ServerReturnCode_EmptyResult) == 0)
 					{
 						return CloudAPI.Return_OK;
 					}
 					else if (strResult.compareTo(CloudAPI.ServerReturnCode_BadSession) == 0)
 					{
 						return CloudAPI.Return_BadToken;
 					}
 					else
 					{
 						JSONUtil.parseOwnedBooksInfo(strResult);
 					}
 					return CloudAPI.Return_OK;
 				} else {
 					return CloudAPI.Return_UnknownError;
 				}
 			} else {
 				return CloudAPI.Return_NetworkError;
 			}
 		} catch (Exception e) {
 			return CloudAPI.Return_NetworkError;
 		}
 	}
 
 	public static int _getFriends()
 	{
 		AppData.getInstance().clearFriends();
 		String url = API_BaseURL + API_GetFollowings + CloudAPI.API_TOKEN + CloudAPI.CloudToken;;
 		HttpGet getReq = new HttpGet(url);
 		try {
 			HttpResponse httpResponse = new DefaultHttpClient().execute(getReq);
 			if (httpResponse.getStatusLine().getStatusCode() == 200) {
 				String strResult = EntityUtils.toString(httpResponse
 						.getEntity());
 				if (null != strResult && strResult.length() > 0) {
 					if (strResult.compareTo(CloudAPI.ServerReturnCode_EmptyResult) == 0)
 					{
 						return CloudAPI.Return_OK;
 					}
 					else if (strResult.compareTo(CloudAPI.ServerReturnCode_BadSession) == 0)
 					{
 						return CloudAPI.Return_BadToken;
 					}
 					else
 					{
 						JSONUtil.parseFriendsInfo(strResult);
 					}
 					return CloudAPI.Return_OK;
 				} else {
 					return CloudAPI.Return_UnknownError;
 				}
 			} else {
 				return CloudAPI.Return_NetworkError;
 			}
 		} catch (Exception e) {
 			return CloudAPI.Return_NetworkError;
 		}
 	}
 	
 	public static int _deleteBook(String url) {
 		HttpGet getReq = new HttpGet(url);
 		try {
 			HttpResponse httpResponse = new DefaultHttpClient().execute(getReq);
 			if (httpResponse.getStatusLine().getStatusCode() == 200) {
 				String strResult = EntityUtils.toString(httpResponse
 						.getEntity());
 				if (strResult.compareTo(CloudAPI.ServerReturnCode_Successful) == 0) {
 					return CloudAPI.Return_OK;
 				} else if (strResult
 						.compareTo(CloudAPI.ServerReturnCode_BadSession) == 0) {
 					return CloudAPI.Return_BadToken;
 				} else {
 					return CloudAPI.Return_NetworkError;
 				}
 			} else {
 				return CloudAPI.Return_NetworkError;
 			}
 		} catch (Exception e) {
 			return CloudAPI.Return_NetworkError;
 		}
 	}
 	
 	public static int _getAllUsers()
 	{
 		AppData.getInstance().clearAllUsers();
 		String url = API_BaseURL + API_GetAllUsers + CloudAPI.API_TOKEN + CloudAPI.CloudToken;;
 		HttpGet getReq = new HttpGet(url);
 		try {
 			HttpResponse httpResponse = new DefaultHttpClient().execute(getReq);
 			if (httpResponse.getStatusLine().getStatusCode() == 200) {
 				String strResult = EntityUtils.toString(httpResponse
 						.getEntity());
 				if (null != strResult && strResult.length() > 0) {
 					if (strResult.compareTo(CloudAPI.ServerReturnCode_EmptyResult) == 0)
 					{
 						return CloudAPI.Return_OK;
 					}
 					else if (strResult.compareTo(CloudAPI.ServerReturnCode_BadSession) == 0)
 					{
 						return CloudAPI.Return_BadToken;
 					}
 					else
 					{
 						JSONUtil.parseAllUsersInfo(strResult);
 					}
 					return CloudAPI.Return_OK;
 				} else {
 					return CloudAPI.Return_UnknownError;
 				}
 			} else {
 				return CloudAPI.Return_NetworkError;
 			}
 		} catch (Exception e) {
 			return CloudAPI.Return_NetworkError;
 		}
 	}
 	
 	public static int _follow(String url) {
 		HttpGet getReq = new HttpGet(url);
 		try {
 			HttpResponse httpResponse = new DefaultHttpClient().execute(getReq);
 			if (httpResponse.getStatusLine().getStatusCode() == 200) {
 				String strResult = EntityUtils.toString(httpResponse
 						.getEntity());
 				if (strResult.compareTo(CloudAPI.ServerReturnCode_Successful) == 0) {
 					return CloudAPI.Return_OK;
 				} else if (strResult
 						.compareTo(CloudAPI.ServerReturnCode_BadSession) == 0) {
 					return CloudAPI.Return_BadToken;
 				} else {
 					return CloudAPI.Return_NetworkError;
 				}
 			} else {
 				return CloudAPI.Return_NetworkError;
 			}
 		} catch (Exception e) {
 			return CloudAPI.Return_NetworkError;
 		}
 	}
 	
 	public static int _unFollow(String url) {
 		HttpGet getReq = new HttpGet(url);
 		try {
 			HttpResponse httpResponse = new DefaultHttpClient().execute(getReq);
 			if (httpResponse.getStatusLine().getStatusCode() == 200) {
 				String strResult = EntityUtils.toString(httpResponse
 						.getEntity());
 				if (strResult.compareTo(CloudAPI.ServerReturnCode_Successful) == 0) {
 					return CloudAPI.Return_OK;
 				} else if (strResult
 						.compareTo(CloudAPI.ServerReturnCode_BadSession) == 0) {
 					return CloudAPI.Return_BadToken;
 				} else {
 					return CloudAPI.Return_NetworkError;
 				}
 			} else {
 				return CloudAPI.Return_NetworkError;
 			}
 		} catch (Exception e) {
 			return CloudAPI.Return_NetworkError;
 		}
 	}
 	
 	public static int _getBooksByOwner(String url)
 	{
 		HttpGet getReq = new HttpGet(url);
 		try {
 			HttpResponse httpResponse = new DefaultHttpClient().execute(getReq);
 			if (httpResponse.getStatusLine().getStatusCode() == 200) {
 				String strResult = EntityUtils.toString(httpResponse
 						.getEntity());
 				if (null != strResult && strResult.length() > 0) {
 					if (strResult.compareTo(CloudAPI.ServerReturnCode_EmptyResult) == 0)
 					{
 						return CloudAPI.Return_OK;
 					}
 					else if (strResult.compareTo(CloudAPI.ServerReturnCode_BadSession) == 0)
 					{
 						return CloudAPI.Return_BadToken;
 					}
 					else
 					{
 						JSONUtil.parseOthersBooksInfo(strResult);
 					}
 					return CloudAPI.Return_OK;
 				} else {
 					return CloudAPI.Return_UnknownError;
 				}
 			} else {
 				return CloudAPI.Return_NetworkError;
 			}
 		} catch (Exception e) {
 			return CloudAPI.Return_NetworkError;
 		}
 	}
 	// Client should not call any of the methods above.
 	public static void login(Context context, String email, String pwd,
 			ICloudAPITaskListener apiListener) {
 		String encryptedPwd = md5(pwd);
 		String url = API_BaseURL + API_Login + API_Email + email + API_LocalPwd
 				+ encryptedPwd;
 
 		CloudAPIAsyncTask task = new CloudAPIAsyncTask(context, url, API_Login,
 				apiListener);
 		task.execute();
 	}
 
 	public static void signUp(Context context, String email, String pwd,
 			String nickName, ICloudAPITaskListener apiListener) {
 		String encryptedPwd = md5(pwd);
 		String url = API_BaseURL + API_SignUp + API_Email + email
 				+ API_LocalPwd + encryptedPwd + API_NickName + nickName;
 
 		CloudAPIAsyncTask task = new CloudAPIAsyncTask(context, url,
 				API_SignUp, apiListener);
 		task.execute();
 	}
 
 	public static void getLoggedInUserInfo(Context context,
 			ICloudAPITaskListener apiListener) {
 		CloudAPIAsyncTask task = new CloudAPIAsyncTask(context, "",
 				API_GetUserInfo, apiListener);
 		task.execute();
 	}
 
 	public static void uploadBooks(Context context,
 			ICloudAPITaskListener apiListener) {
 		CloudAPIAsyncTask task = new CloudAPIAsyncTask(context, "",
 				API_UploadBooks, apiListener);
 		task.execute();
 	}
 	
 	public static void getOwnedBooks(Context context,
 			ICloudAPITaskListener apiListener) {
 		CloudAPIAsyncTask task = new CloudAPIAsyncTask(context, "",
 				API_GetOwnedBooks, apiListener);
 		task.execute();
 	}
 	
 	public static void sychronizeOwnedBooks(Context context,
 			ICloudAPITaskListener apiListener) {
 		CloudAPIAsyncTask task = new CloudAPIAsyncTask(context, "",
 				API_SynchronizeOwnedBooks, apiListener);
 		task.execute();
 	}
 	
 	public static void getFollowings(Context context,
 			ICloudAPITaskListener apiListener) {
 		CloudAPIAsyncTask task = new CloudAPIAsyncTask(context, "",
 				API_GetFollowings, apiListener);
 		task.execute();
 	}
 	
 	public static void deleteBook(Context context, String isbn,
 			ICloudAPITaskListener apiListener) {
 		String url = API_BaseURL + API_DeleteBook + CloudAPI.API_TOKEN + CloudAPI.CloudToken + CloudAPI.API_ISBN + isbn;
 		CloudAPIAsyncTask task = new CloudAPIAsyncTask(context, url,
 				API_DeleteBook, apiListener);
 		task.execute();
 	}
 	
 	public static void getAllUsers(Context context,
 			ICloudAPITaskListener apiListener) {
 		CloudAPIAsyncTask task = new CloudAPIAsyncTask(context, "",
 				API_GetAllUsers, apiListener);
 		task.execute();
 	}
 	
 	public static void follow(Context context, String friendId,
 			ICloudAPITaskListener apiListener) {
 		String url = API_BaseURL + API_Follow + CloudAPI.API_TOKEN + CloudAPI.CloudToken + CloudAPI.API_FriendId + friendId;
 		CloudAPIAsyncTask task = new CloudAPIAsyncTask(context, url,
 				API_Follow, apiListener);
 		task.execute();
 	}
 	
 	public static void unFollow(Context context, String friendId,
 			ICloudAPITaskListener apiListener) {
 		String url = API_BaseURL + API_UnFollow + CloudAPI.API_TOKEN + CloudAPI.CloudToken + CloudAPI.API_FriendId + friendId;
 		CloudAPIAsyncTask task = new CloudAPIAsyncTask(context, url,
 				API_UnFollow, apiListener);
 		task.execute();
 	}
 	
 	public static void getBooksByOwner(Context context, String ownerId,
 			ICloudAPITaskListener apiListener) {
 		String url = API_BaseURL + API_GetBooksByOwner + CloudAPI.API_TOKEN + CloudAPI.CloudToken + CloudAPI.API_OwnerId + ownerId;
 		CloudAPIAsyncTask task = new CloudAPIAsyncTask(context, url,
 				API_GetBooksByOwner, apiListener);
 		task.execute();
 	}
 }
