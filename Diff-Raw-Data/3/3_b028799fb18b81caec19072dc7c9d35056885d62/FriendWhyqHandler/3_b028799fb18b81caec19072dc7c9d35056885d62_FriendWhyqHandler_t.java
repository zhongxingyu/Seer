 package whyq.handler;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 
 import whyq.model.FriendWhyq;
 import whyq.model.StatusUser;
 
 public class FriendWhyqHandler extends BaseHandler {
 	private static final String TAG_ID = "id";
 	private static final String TAG_EMAIL = "email";
 	private static final String TAG_ROLE_ID = "role_id";
 	private static final String TAG_IS_ACTIVE = "is_active";
 	private static final String TAG_TOTAL_MONEY = "total_money";
 	private static final String TAG_TOTAL_SAVING_MONEY = "total_saving_money";
 	private static final String TAG_TOTAL_COMMENT = "total_comment";
 	private static final String TAG_TOTAL_COMMENT_LIKE = "total_comment_like";
 	private static final String TAG_TOTAL_FRIEND = "total_friend";
 	private static final String TAG_TOTAL_FAVOURITE = "total_favourite";
 	private static final String TAG_TOTAL_CHECK_BILL = "total_check_bill";
 	private static final String TAG_TWITTER_ID = "twitter_id";
 	private static final String TAG_FACEBOOK_ID = "facebook_id";
 	private static final String TAG_TOKEN = "token";
 	private static final String TAG_STATUS = "status";
 	private static final String TAG_CREATEDATE = "createdate";
 	private static final String TAG_UPDATEDATE = "updatedate";
 	private static final String TAG_FIRST_NAME = "first_name";
 	private static final String TAG_LAST_NAME = "last_name";
 	private static final String TAG_GENDER = "gender";
 	private static final String TAG_AVATAR = "avatar";
 	private static final String TAG_STATUS_USER = "status_user";
 	private static final String ITEM = "obj";
 	private static final String ITEM_SEARCH = "data";
 	private static final String TAG_IS_FRIEND = "is_friend";
 	private static final String TAG_WERE_FRIEND = "were_friend";
 
 	private List<FriendWhyq> friends;
 	private FriendWhyq currentFriend;
 	private StatusUser currentStatusUser = new StatusUser();
 
 	public List<FriendWhyq> getFriends() {
 		return friends;
 	}
 
 	@Override
 	public void endElement(String uri, String localName, String name)
 			throws SAXException {
 		super.endElement(uri, localName, name);
 		if (this.currentFriend != null) {
 			if (localName.equalsIgnoreCase(TAG_AVATAR)) {
 				currentFriend.setAvatar(getString());
 			} else if (localName.equalsIgnoreCase(TAG_CREATEDATE)) {
 				currentFriend.setCreatedate(getString());
 			} else if (localName.equalsIgnoreCase(TAG_EMAIL)) {
 				currentFriend.setEmail(getString());
 			} else if (localName.equalsIgnoreCase(TAG_FACEBOOK_ID)) {
 				currentFriend.setFacebook_id(getString());
 			} else if (localName.equalsIgnoreCase(TAG_FIRST_NAME)) {
 				currentFriend.setFirst_name(getString());
 			}else if (localName.equalsIgnoreCase(TAG_LAST_NAME)) {
 				currentFriend.setLast_name(getString());
 			}  else if (localName.equalsIgnoreCase(TAG_GENDER)) {
 				currentFriend.setGender(getString());
 			} else if (localName.equalsIgnoreCase(TAG_ID)) {
 				currentFriend.setId(getString());
 			} else if (localName.equalsIgnoreCase(TAG_IS_ACTIVE)) {
 				currentFriend.setIs_active(getInt());
 			} else if (localName.equalsIgnoreCase(TAG_LAST_NAME)) {
 				currentFriend.setLast_name(getString());
 			} else if (localName.equalsIgnoreCase(TAG_ROLE_ID)) {
 				currentFriend.setRole_id(getInt());
 			} else if (localName.equalsIgnoreCase(TAG_STATUS)) {
 				currentFriend.setStatus(getString());
 			} else if (localName.equalsIgnoreCase(TAG_IS_FRIEND)) {
 				currentStatusUser.setIs_friend(getInt());
 			} else if (localName.equalsIgnoreCase(TAG_WERE_FRIEND)) {
 				currentStatusUser.setWere_friend(getInt());
 			} else if (localName.equalsIgnoreCase(TAG_STATUS_USER)) {
 				currentFriend.setStatus_user(currentStatusUser);
 			} else if (localName.equalsIgnoreCase(TAG_TOKEN)) {
 				currentFriend.setToken(getString());
 			} else if (localName.equalsIgnoreCase(TAG_TOTAL_CHECK_BILL)) {
 				currentFriend.setTotal_check_bill(getInt());
 			} else if (localName.equalsIgnoreCase(TAG_TOTAL_COMMENT)) {
 				currentFriend.setTotal_comment(getInt());
 			} else if (localName.equalsIgnoreCase(TAG_TOTAL_COMMENT_LIKE)) {
 				currentFriend.setTotal_comment_like(getInt());
 			} else if (localName.equalsIgnoreCase(TAG_TOTAL_FAVOURITE)) {
 				currentFriend.setTotal_favourite(getInt());
 			} else if (localName.equalsIgnoreCase(TAG_TOTAL_FRIEND)) {
 				currentFriend.setTotal_friend(getInt());
 			} else if (localName.equalsIgnoreCase(TAG_TOTAL_MONEY)) {
 				currentFriend.setTotal_money(getFloat());
 			} else if (localName.equalsIgnoreCase(TAG_TOTAL_SAVING_MONEY)) {
 				currentFriend.setTotal_saving_money(getFloat());
 			} else if (localName.equalsIgnoreCase(TAG_TWITTER_ID)) {
 				currentFriend.setTwitter_id(getString());
 			} else if (localName.equalsIgnoreCase(TAG_UPDATEDATE)) {
 				currentFriend.setUpdatedate(getString());
 			} else if (localName.equalsIgnoreCase(TAG_IS_FRIEND)) {
 				currentFriend.setIsFriend(getInt());
 			} else if (localName.equalsIgnoreCase(ITEM_SEARCH)) {
 				friends.add(currentFriend);
 			} else if (localName.equalsIgnoreCase(ITEM)) {
 				friends.add(currentFriend);
 			} 
 			builder.setLength(0);
 		}
 	}
 	
 	@Override
 	public void startDocument() throws SAXException {
 		super.startDocument();
 		friends = new ArrayList<FriendWhyq>();
 	}
 
 	@Override
 	public void startElement(String uri, String localName, String name,
 			Attributes attributes) throws SAXException {
 		super.startElement(uri, localName, name, attributes);
 		if (localName.equalsIgnoreCase(ITEM)||localName.equalsIgnoreCase(ITEM_SEARCH)) {
			builder.setLength(0);
 			this.currentFriend = new FriendWhyq();
 		} else if (localName.equalsIgnoreCase(TAG_STATUS_USER)) {
			builder.setLength(0);
 			this.currentStatusUser = new StatusUser();
 		}
 	}
 
 }
