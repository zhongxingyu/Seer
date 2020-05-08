 package com.quanleimu.util;
 
 import java.util.ArrayList;
 
 public class TrackConfig {
 	
 	
 	private  String response;
 	private boolean isLogging = true;//default config
 	private boolean hasResponseFromApi = false;
 	
 	private static TrackConfig instance = null;
 	public static TrackConfig getInstance() {
 		if (instance==null) {
 			instance = new TrackConfig();
 		}
 		return instance;
 	}
 	//constructor
 	private TrackConfig() {
 		
 	}
 
 	public boolean getLoggingFlag() {
 		return isLogging;
 	}
 	
 	public void getConfig() {
 		if (hasResponseFromApi == false) {
 			hasResponseFromApi = true;
 			new Thread(new ConfigRunnable()).start();
 		}
 	}
 	
 	class ConfigRunnable implements Runnable {
 
 		@Override
 		public void run() {
 			String apiName = "mobile_config";
 			String url = Communication.getApiUrl(apiName, new ArrayList<String>());
 			try {
 				response = Communication.getDataByUrl(url, true);
 			} catch (Exception e) {
 
 			} finally {
 				if (response != null && response.equals("\"false\"")) {
 					isLogging = false;
 				}else if (response != null && response.equals("\"true\"")){
 					isLogging = true;
 				}
 			}
 		}
 	}
 	
 	public interface TrackMobile {
 		enum Key implements TrackMobile {//每条记录可能的key
 			TRACKTYPE("tracktype","tracktype"),
 			TIMESTAMP("timestamp","timestamp"),
 			
 			URL("url","页面URL"),
 			FIRSTCATENAME("firstCateName","一级类目名"),
 			SECONDCATENAME("secondCateName","二级类目名"),
 			SEARCHKEYWORD("searchKeyword","搜索关键字"),
 			LISTINGFILTER("listingFilter","listing页面筛选条件"),
 			ADID("adId","adId"),
 			USERID("userId","userId"),
 			ISLOGIN("isLogin","已登录/未登录"),
 			ADSCOUNT("adsCount","信息条数"),
 			ADSENDERID("adSenderId","ad的发布者的userId"),
 			ADSTATUS("adStatus","正常/审核未通过"),
 //			LOGIN_STATUS("loginStatus","已登录/未登录"),
 			
 			EVENT("event","事件名"),
 			CITY("city","城市"),
 			BLOCK("block","区块（GPS定位、热门城市、其他城市、搜索）"),
 			GPS_RESULT("GPS_result","GPS定位城市结果成功失败"),
 			CATEGORYCOUNT("categoryCount","类目数"),
 			MAXCATE_ADSCOUNT("maxCate_adsCount","最大类目条数"),
 //			TOTAL_ADSCOUNT("total_adsCount","总条数"),
 			FILTER("filter","筛选条件"),
 			RESULTCATESCOUNT("resultCatesCount","结果类目数"),
 			TOTAL_ADSCOUNT("total_adsCount","总信息数"),
 			SELECTEDROWINDEX("selectedRowIndex","点进去看的所在行（从0开始）"),
 			DIALOG("dialogCount","对话数"),
 			DIALOG_BUYER("dialog_buyer","作为买家对话数"),
 			DIALOG_SELLER("dialog_seller","作为卖家对话数"),
 			POSTSTATUS("postStatus","状态（客户端验证失败，server端机器规则失败，成功）"),
 			POSTFAILREASON("postFailReason","发布失败原因"),
 			POSTPICSCOUNT("postPicsCount","图片数"),
 			POSTDESCRIPTIONTEXTCOUNT("postDescriptionTextCount","描述文字数"),
 			POSTDESCRIPTIONLINECOUNT("postDescriptionLineCount","描述文字行数"),
 			POSTCONTACTTEXTCOUNT("postContactTextCount","联系方式字数"),
 			POSTDETAILPOSITIONAUTO("postDetailPositionAuto","具体地点是否自动定位"),
 			POSTEDSECONDS("postedSeconds","已发布秒数"),
 			LOGIN_RESULT_STATUS("loginResultStatus","Login结果（成功、出错）"),
 			LOGIN_RESULT_FAIL_REASON("loginResultFailReason","login出错原因"),
             EDIT_PROFILE_STATUS("editProfileStatus", "修改用户信息成功失败状态"),
             EDIT_RPOFILE_FAIL_REASON("editProfileFileReason", "修改用户信息失败原因"),
 			POSTCOUNT_BEFORELOGIN("postCountBeforeLogin","如果登录成功登录前发帖数"),
 			REGISTER_RESULT_STATUS("registerResultStatus","注册结果（成功、出错）"),
 			REGISTER_RESULT_FAIL_REASON("registerResultFailReason","注册失败原因"),
 			POSTCOUNT_BEFOREREGISTER("postCountBeforeRegister","如果注册成功注册前发帖数"),
 			FORGETPASSWORD_SENDCODE_RESULT_STATUS("forgetPasswordSendCodeResultStatus","发送验证码成功/失败状态"),
 			FORGETPASSWORD_SENDCODE_RESULT_FAIL_REASON("forgetPasswordSendCodeResultFailReason","发送验证码失败原因"),
 			FORGETPASSWORD_RESETPASSWORD_RESULT("forgetPasswordResetPasswordResult","重设密码成功/失败状态"),
 			FORGETPASSWORD_RESETPASSWORD_RESULT_FAIL_REASON("forgetPasswordResetPasswordResultFailReason","重设密码失败原因"),
 			MENU_SHOW_PAGEURL("menuShowInPageURL","菜单出现的页面URL"),
 			MENU_ACTION_TYPE("menuActionType","菜单动作类型"),
 			MENU_ACTION_TYPE_CHANGE_CITY("menuActionType_changeCity","菜单动作切换城市");
 			
 			
 			private String name;
 			private String description;
 			private Key(String keyName, String keyDescription) {
 				this.name = keyName;
 				this.description = keyDescription;
 			}
 			public String getName() {
 				return name;
 			}
 			public String getDescription() {
 				return description;
 			}
 			
 		}
 		
 		enum PV implements TrackMobile {//pageview相关的value
 			//页面
 			SELECTCITY("/selectCity","切换城市"),
 			HOME("/home","首页"),
 			CATEGORIES("/categories","一级类目页"),
 			SEARCH("/search","header搜索页"),
 			SEARCHRESULTCATEGORY("/searchResultCategory","header搜索类目结果页"),
 			SEARCHRESULT("/searchResult","header搜索结果页"),
 			LISTING("/listing","Listing页"),
 			LISTINGFILTER("/listingFilter","更多筛选页"),
 			VIEWAD("/viewAd","Viewad页"),
 			VIEWADMAP("/viewAdMap","Viewad地图页"),
 			BUZZ("/buzz","私信"),
 			VIEWADPIC("/viewAdPic","图"),
 			POSTCATE1("/post/cate1","发布选择一级类目页"),
 			POSTCATE2("/post/cate2","发布选择二级类目页"),
 			POST("/post","发布界面"),
 			EDITPOST("/editPost","编辑界面"),
 			MY("/my","我的百姓网"),
 			MYADS_SENT("/myAds_sent","已发布信息"),
 			MYADS_APPROVING("/myAds_approving","审核未通过"),
 			MYADS_DELETED("/myAds_deleted","已删除"),
 			MYVIEWAD("/myViewad","自己查看的viewad"),
 			FAVADS("/favAds","收藏"),
 			BUZZLISTING("/buzzListing","私信列表页"),
 			HISTORYADS("/historyAds","最近浏览"),
 			SETTINGS("/settings","设置"),
 			LOGIN("/login","登录"),
 			REGISTER("/register","注册"),
 			FORGETPASSWORD("/forgetPassword","忘记密码");			
 			
 			private String name;
 			private String description;
 
 			private PV(String url, String description) {
 				this.name = url;
 				this.description = description;
 			}
 			public String getName() {
 				return name;
 			}
 			public String getDescription() {
 				return description;
 			}
 			
 		}
 		
 		enum BxEvent implements TrackMobile {//event相关的value
 			CITY_SELECT("City_Select","City_Select"),
 			CITY_SEARCH("City_Search","City_Search"),
 			HEADERSEARCHRESULT("HeaderSearchResult","HeaderSearchResult"),
 			LISTING("Listing","Listing"),
 			LISTING_SELECTEDROWINDEX("Listing_SelectedRowIndex","Listing_SelectedRowIndex"),
 			LISTING_MORE("Listing_More","Listing_More"),
 			VIEWAD_MOBILENUMBERCLICK("Viewad_MobileNumberClick","点击电话号码"),
 			VIEWAD_MOBILECALLCLICK("Viewad_MobileCallClick","点击拨打按钮"),
 			VIEWAD_FAV("Viewad_Fav","Viewad_Fav"),
 			VIEWAD_UNFAV("Viewad_Unfav","Viewad_Unfav"),
 			VIEWAD_SMS("Viewad_SMS","Viewad_SMS"),
 			BUZZLIST("BuzzList","BuzzList"),
 			POST_POSTBTNHEADERCLICKED("Post_PostBtnHeaderClicked","Post_PostBtnHeaderClicked"),
 			POST_POSTBTNCONTENTCLICKED("Post_PostBtnContentClicked","Post_PostBtnContentClicked"),
 			POST_POSTWITHLOGIN("Post_PostWithLogin","Post_登录Post"),
 			POST_POSTWITHOUTLOGIN("Post_PostWithoutLogin","Post_未登录Post"),
 			POST_POSTRESULT("Post_PostResult","Post_PostResult"),
 			POST_GPSFAIL("Post_GpsFail","Post_GPS失败"),
 			EDITPOST_POSTBTNHEADERCLICKED("EditPost_PostBtnHeaderClicked","EditPost_PostBtnHeaderClicked"),
 			EDITPOST_POSTBTNCONTENTCLICKED("EditPost_PostBtnContentClicked","EditPost_PostBtnContentClicked"),
 			EDITPOST_POSTWITHLOGIN("EditPost_PostWithLogin","EditPost_登录Post"),
 			EDITPOST_POSTWITHOUTLOGIN("EditPost_PostWithoutLogin","EditPost_未登录Post"),
 			EDITPOST_POSTRESULT("EditPost_PostResult","EditPost_PostResult"),
 			EDITPOST_GPSFAIL("EditPost_GpsFail","EditPost_GPS失败"),
 			SENT_RESULT("Sent_Result","Sent_Result"),
 			SENT_MANAGE("Sent_Manage","Sent_Manage"),
 			SENT_REFRESH("Sent_Refresh","Sent_Refresh"),
 			SENT_EDIT("Sent_Edit","Sent_Edit"),
 			SENT_DELETE("Sent_Delete","Sent_Delete"),
 			APPROVING_RESULT("Approving_Result","Approving_Result"),
 			APPROVING_MANAGE("Approving_Manage","Approving_Manage"),
 			APPROVING_APPEAL("Approving_Appeal","Approving_Appeal"),
             APPROVING_DELETE("Approving_Delete", "Approving_Delete"),
 			DELETED_RESULT("Deleted_Result","Deleted_Result"),
 			DELETED_MANAGE("Deleted_Manage","Deleted_Manage"),
 			DELETED_RECOVER("Deleted_Recover","Deleted_Recover"),
 			DELETED_DELETE("Deleted_Delete","Deleted_Delete"),
 			MYVIEWAD_EDIT("MyViewad_Edit","MyViewad_Edit"),
 			MYVIEWAD_REFRESH("MyViewad_Refresh","MyViewad_Refresh"),
 			MYVIEWAD_DELETE("MyViewad_Delete","MyViewad_Delete"),
 			MYVIEWAD_APPEAL("MyViewad_Appeal","MyViewad_Appeal"),
 			FAV_MANAGE("Fav_Manage","Fav_Manage"),
 			FAV_DELETE("Fav_Delete","Fav_Delete"),
 			BUZZLIST_MANAGE("BuzzList_Manage","BuzzList_Manage"),
 			BUZZLIST_DELETE("BuzzList_Delete","BuzzList_Delete"),
 			HISTORY_MANAGE("History_Manage","History_Manage"),
 			HISTORY_DELETE("History_Delete","History_Delete"),
 			SETTINGS_CHECKUPDATE("Settings_CheckUpdate","Settings_CheckUpdate"),
 			SETTINGS_ABOUT("Settings_About","Settings_About"),
 			SETTINGS_FEEDBACK("Settings_Feedback","Settings_Feedback"),
 			SETTINGS_PICMODE("Settings_PicMode","Settings_PicMode"),
 			SETTINGS_LOGOUT("Settings_Logout","Settings_Logout"),
 			SETTINGS_LOGOUT_CONFIRM("Settings_Logout_Confirm","Settings_Logout_Confirm"),
 			SETTINGS_LOGOUT_CANCEL("Settings_Logout_Cancel","Settings_Logout_Cancel"),
 			SETTINGS_LOGIN("Settings_Login","Settings_Login"),
 			EDITPROFILE_SAVE("EditProfile_Save","EditProfile_Save"),
 			EDITPROFILE_CANCEL("EditProfile_Cancel","EditProfile_Cancel"),
 			LOGIN_BACK("Login_Back","Login_Back"),
 			LOGIN_REGISTER("Login_Register","Login_Register"),
 			LOGIN_SUBMIT("Login_Submit","Login_Submit"),
 			LOGIN_FORGETPASSWORD("Login_ForgetPassword","Login_ForgetPassword"),
 			REGISTER_BACK("Register_Back","Register_Back"),
 			REGISTER_REGISTER("Register_Register","Register_Register"),
 			REGISTER_SUBMIT("Register_Submit","Register_Submit"),
 			FORGETPASSWORD_SENDCODE_RESULT("ForgetPassword_sendCode_Result","ForgetPassword_sendCode_Result"),
 			FORGETPASSWORD_RESETPASSWORD_RESULT("ForgetPassword_resetPassword_Result","ForgetPassword_resetPassword_Result"),
 			MENU_SHOW("Menu_Show","Menu_Show"),
 			MENU_CANCEL("Menu_Cancel","Menu_Cancel"),
 			MENU_ACTION("Menu_Action","Menu_Action"),
 			APP_START("App_Start","App_Start"),
 			APP_STOP("App_Stop","App_Stop"),
 			APP_PAUSE("App_Pause","App_Pause"),
 			APP_RESUME("App_Resume","App_Resume"),
 			GPS("GPS","GPS");
 			
 			private String name;
 			private String description;
 			private BxEvent(String name, String description) {
 				this.name = name;
 				this.description = description;
 			}
 			public String getName() {
 				return name;
 			}
 			public String getDescription() {
 				return description;
 			}
 			
 		}
 	}
 }
