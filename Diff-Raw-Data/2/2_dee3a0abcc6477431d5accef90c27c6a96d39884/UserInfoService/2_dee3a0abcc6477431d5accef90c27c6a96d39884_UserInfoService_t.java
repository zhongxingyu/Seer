 package edu.sysubbs.argoandroid.argoservices.user;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import edu.sysubbs.argoandroid.argoobject.ArgoBoard;
 import edu.sysubbs.argoandroid.argoobject.ArgoQueryUser;
 import edu.sysubbs.argoandroid.argoobject.ArgoSelfUser;
 import edu.sysubbs.argoandroid.argoobject.BaseObject;
 import edu.sysubbs.argoandroid.util.ErrorException;
 import edu.sysubbs.argoandroid.util.HttpManager;
 import edu.sysubbs.argoandroid.util.Site;
 
 public class UserInfoService {
 	public ArgoQueryUser queryOtherUser(String userid) throws ErrorException {
 		HttpManager manager = new HttpManager();
 		HashMap<String, Object> data = new HashMap<String, Object>();
 		data.put("userid", userid);
		ArgoQueryUser user = manager.getResponseAsObject(Site.QUERY_USER_INFO, null, data, ArgoQueryUser.class);
 		return user;
 	}
 	
 	public ArrayList<ArgoBoard> getFavBoards(String cookie) throws ErrorException {
 		HttpManager manager = new HttpManager();
 		ArrayList<ArgoBoard> boardList = manager.getResponseAsList(Site.QUERY_FAV_BOARD, cookie, null, ArgoBoard.class);
 		return boardList;
 	}
 	
 	public boolean addFavBoard(String cookie, String boardname) {
 		HttpManager manager = new HttpManager();
 		boolean success = false;
 		HashMap<String, Object> data = new HashMap<String, Object>();
 		data.put("boardname", boardname);
 		try {
 			manager.postDataByMapAndGetObject(Site.ADD_FAV_BORAD, cookie, data, BaseObject.class);
 			success = true;
 		} catch (ErrorException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			success = false;
 		}
 		return success;
 	}
 	
 	public boolean deleteFavBoard(String cookie, String boardname) {
 		HttpManager manager = new HttpManager();
 		boolean success = false;
 		HashMap<String, Object> data = new HashMap<String, Object>();
 		data.put("boardname", boardname);
 		try {
 			manager.postDataByMapAndGetObject(Site.DEL_FAV_BOARD, cookie, data, BaseObject.class);
 			success = true;
 		} catch (ErrorException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			success = false;
 		}
 		return success;
 	}
 	
 	public boolean updateUserInfo(String cookie, String username, String realname, 
 			String gender, String address, String email, String birthyear, String birthmonth, String birthday, String plan,
 			String signautre) {
 		HttpManager manager = new HttpManager();
 		boolean success = false;
 		HashMap<String, Object> data = new HashMap<String, Object>();
 		if (username != null) {
 			data.put("username", username);
 		}
 		if (realname != null) {
 			data.put("realname", realname);
 		}
 		if (gender != null) {
 			data.put("gender", gender);
 		}
 		if (address != null) {
 			data.put("address", address);
 		}
 		if (email != null) {
 			data.put("email", email);
 		}
 		if (birthyear != null) {
 			data.put("birthyear", birthyear);
 		}
 		if (birthmonth != null) {
 			data.put("birthmonth", birthmonth);
 		}
 		if (birthday != null) {
 			data.put("birthday", birthday);
 		}
 		if (plan != null) {
 			data.put("plan", plan);
 		}
 		if (signautre != null) {
 			data.put("signature", signautre);
 		}
 		try {
 			manager.postDataByMapAndGetObject(Site.UPDATE_USER_INFO, cookie, data, BaseObject.class);
 			success = true;
 		} catch (ErrorException e) {
 			success = false;
 		}
 		
 		return success;
 	}
 	
 	public ArgoSelfUser getSelfInfo(String cookie) throws ErrorException {
 		HttpManager manager = new HttpManager();
 		ArgoSelfUser argoSelfUser = manager.getResposneAsObject(Site.GET_SELF_INFO, cookie, null, ArgoSelfUser.class);
 		return argoSelfUser;
 	}
 	
 	
 }
