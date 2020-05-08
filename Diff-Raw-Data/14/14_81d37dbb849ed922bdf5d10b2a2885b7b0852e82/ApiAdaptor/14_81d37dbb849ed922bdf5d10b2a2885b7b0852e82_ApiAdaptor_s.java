 /**
  * 
  */
 package com.pintu.facade;
 
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.http.HttpServletResponse;
 
 import net.sf.json.JSONArray;
 import net.sf.json.JSONObject;
 
 import org.apache.commons.fileupload.FileItem;
 import org.apache.log4j.Logger;
 
 import com.pintu.ads.beans.Ads;
 import com.pintu.ads.beans.Vender;
 import com.pintu.ads.facade.AdsServiceInterface;
 import com.pintu.beans.Applicant;
 import com.pintu.beans.Event;
 import com.pintu.beans.Favorite;
 import com.pintu.beans.Gift;
 import com.pintu.beans.Message;
 import com.pintu.beans.Story;
 import com.pintu.beans.StoryDetails;
 import com.pintu.beans.TPicDesc;
 import com.pintu.beans.TPicDetails;
 import com.pintu.beans.TPicItem;
 import com.pintu.beans.TPicReview;
 import com.pintu.beans.Tag;
 import com.pintu.beans.TastePic;
 import com.pintu.beans.User;
 import com.pintu.beans.UserDetail;
 import com.pintu.beans.Vote;
 import com.pintu.utils.PintuUtils;
 
 /**
  * Servlet调用服务的参数转换器，用来封装客户端参数并实现服务调用；
  * 
  * @author lwz
  * 
  */
 
 public class ApiAdaptor {
 
 	// 由Spring注入
 	private PintuServiceInterface pintuService;
 	
 	private AdsServiceInterface adService;
 
 	private Logger log = Logger.getLogger(ApiAdaptor.class);
 	
 	public ApiAdaptor() {
 
 	}
 
 	public PintuServiceInterface getPintuService() {
 		return pintuService;
 	}
 
 	public void setPintuService(PintuServiceInterface pintuService) {
 		this.pintuService = pintuService;
 	}
 
 	public AdsServiceInterface getAdService() {
 		return adService;
 	}
 
 	public void setAdService(AdsServiceInterface adService) {
 		this.adService = adService;
 	}
 
 	// 由AppStarter调用
 	public void setImagePath(String filePath) {
 		this.pintuService.saveImagePathToProcessor(filePath);
 	}
 
 	public void createTastePic(List<FileItem> fileItems) {
 		log.debug("2 Analyse pic obj: apiadaptor createTastePic");
 		TastePic pic = new TastePic();
 		
 		//取图片的具体内部数据
 		for (int i = 0; i < fileItems.size(); i++) {
 			FileItem item = fileItems.get(i);
 			if (!item.isFormField()) {
 				// 图片数据
 				pic.setRawImageData(item);
 			}
 
 			if (item.isFormField()) {
 				// 取描述
 				if (item.getFieldName().equals("description")) {
 					try {
 						pic.setDescription(item.getString("UTF-8"));
 					} catch (UnsupportedEncodingException e) {
 						e.printStackTrace();
 					}
 				}
 				// 标签
 				if (item.getFieldName().equals("tags")) {
 					try {
 						pic.setTags(item.getString("UTF-8"));
 					} catch (UnsupportedEncodingException e) {
 						e.printStackTrace();
 					}
 				}
 				// 取用户
 				if (item.getFieldName().equals("userId")) {
 					pic.setUser(item.getString());
 				}
 				// 取来源
 				if (item.getFieldName().equals("source")) {
 					pic.setSource(item.getString());
 				}
 				// 是否原创
 				if (item.getFieldName().equals("isOriginal")) {
 					pic.setIsOriginal(Integer.parseInt(item.getString()));
 				}
 			}
 		} // 参数解析完成
 
 		// 将新发送的贴图对象放入服务处理
 		this.pintuService.createTastePic(pic, pic.getUser());
 		// 贴图处理完成OYEAR!
 	}
 
 	/**
 	 * 获取社区长廊的缓存图片信息
 	 * 
 	 * @param startTime
 	 * @param endTime
 	 * @return
 	 */
 	public String getGalleryByTime(String startTime, String endTime) {
 		long queryTimeSpan = Long.valueOf(endTime) - Long.valueOf(startTime);
 		log.debug(">>> query time span:" + queryTimeSpan / (60 * 1000)
 				+ " minutes;");
 		log.debug("startTime:"+PintuUtils.formatLong(Long.parseLong(startTime))
 				+" endTime:"+PintuUtils.formatLong(Long.parseLong(endTime)));
 		
 		long oneDayMiliSeconds = 24 * 60 * 60 * 1000;
 		if (queryTimeSpan > oneDayMiliSeconds) {
 			// 如果跨越了1天，就只给返回一天的数据
 			startTime = String.valueOf(Long.valueOf(endTime)
 					- oneDayMiliSeconds);
 		}
 		List<TPicDesc> list = pintuService.getTpicsByTime(startTime, endTime);
 
 		return JSONArray.fromCollection(list).toString();
 	}
 
 	/**
 	 * 获取特定的图片
 	 * 
 	 * @param picId
 	 * @param res
 	 */
 	public void getImageFile(String picId, HttpServletResponse res) {
 		pintuService.getImageFile(picId, res);
 	}
 
 	/**
 	 * 根据图片文件路径返回图片
 	 * 
 	 * @param path
 	 * @param res
 	 */
 	public void getImageByPath(String path, HttpServletResponse res) {
 		pintuService.getImageByPath(path, res);
 	}
 
 	/**
 	 * 根据图片id获得详情
 	 * 
 	 * @param tpId
 	 * @return
 	 */
 	public String getTPicDetailsById(String tpId) {
 		TPicDetails tpicDetails = pintuService.getTPicDetailsById(tpId);
 		JSONObject json = JSONObject.fromBean(tpicDetails);
 		json.remove("mobImgPath");
 		json.remove("rawImgPath");
 		json.remove("mobImgSize");
 		json.remove("rawImgSize");
 		return json.toString();
 	}
 
 	/**
 	 * 得到品图故事
 	 * 
 	 * @param tpID
 	 * @return
 	 */
 	public String getStoryDetailsOfPic(String tpId) {
 		return JSONArray
 				.fromCollection(pintuService.getStoryDetailsOfPic(tpId))
 				.toString();
 	}
 
 	private Story createStory(String follow, String owner, String content,
 			String source) {
 		Story story = new Story();
 		story.setId(PintuUtils.generateUID());
 		story.setFollow(follow);
 		story.setOwner(owner);
 		story.setPublishTime(PintuUtils.getFormatNowTime());
 		story.setContent(content);
 		story.setClassical(0);
 		return story;
 	}
 
 	/**
 	 * 为一个品图添加故事
 	 * 
 	 * @param source
 	 * 
 	 * @param story
 	 */
 	public void addStoryToPicture(String follow, String owner, String content,
 			String source) {
 		Story story = this.createStory(follow, owner, content, source);
 		pintuService.addStoryToPintu(story);
 	}
 
 	private Vote createVote(String follow, String type, String amount,
 			String voter, String receiver) {
 		Vote vote = new Vote();
 		vote.setId(PintuUtils.generateUID());
 		vote.setFollow(follow);
 		vote.setType(type);
 		vote.setAmount(Integer.parseInt(amount));
 		vote.setVoter(voter);
 		vote.setReceiver(receiver);
 		return vote;
 	}
 
 	/**
 	 * 为品图故事投票
 	 * 
 	 * @param voter
 	 * @param receiver
 	 * @param vote
 	 */
 	public void addVoteToPic(String follow, String type, String amount,
 			String voter, String receiver) {
 		Vote vote = this.createVote(follow, type, amount, voter, receiver);
 		pintuService.addVoteToPic(vote);
 	}
 
 	/**
 	 * 获取用户详细信息
 	 * 
 	 * @param userId
 	 * @return
 	 */
 	public String getUserDetail(String userId) {
 		User user = pintuService.getUserInfo(userId);
 		return JSONObject.fromObject(user).toString();
 	}
 
 	private Message createMessage(String sender, String receiver,
 			String content, String source) {
 		Message msg = new Message();
 		msg.setId(PintuUtils.generateUID());
 		msg.setSender(sender);
 		msg.setReceiver(receiver);
 		msg.setContent(content);
 		msg.setReference("");
 		msg.setWriteTime(PintuUtils.getFormatNowTime());
 		msg.setRead(0);
 		return msg;
 	}
 
 	/**
 	 * 发消息
 	 * 
 	 * @param sender
 	 * @param receiver
 	 * @param content
 	 * @param source
 	 */
 	public String sendMessage(String sender, String receiver, String content,
 			String source) {
 		Message msg = this.createMessage(sender, receiver, content, source);
 		return pintuService.sendMessage(msg);
 	}
 
 	/**
 	 * 得到该用户的所有消息信息
 	 * 
 	 * @param userId
 	 * @return
 	 */
 	public String getUserMsg(String userId) {
 		List<Message> msgList = pintuService.getUserMessages(userId);
 		return JSONArray.fromCollection(msgList).toString();
 	}
 
 	/**
 	 * 改变消息状态
 	 * 
 	 * @param read
 	 */
 	public String changeMsgState(String msgIds) {
 		List<String> msgIdList = new ArrayList<String>();
 		String[] idArray = msgIds.split(",");
 		for (int i = 0; i < idArray.length; i++) {
 			msgIdList.add(idArray[i]);
 		}
 		return pintuService.changeMsgState(msgIdList);
 	}
 
 	/**
 	 * 得到经典品图信息
 	 * 
 	 * @return
 	 */
 	public String getClassicalStory() {
 		List<StoryDetails> classicalList = pintuService.getClassicalPintu();
 		return JSONArray.fromCollection(classicalList).toString();
 	}
 
 	public String getUserEstate(String userId) {
 		UserDetail userDetail = pintuService.getUserEstate(userId);
 		return JSONObject.fromBean(userDetail).toString();
 	}
 
 	private Favorite createFavorite(String userId, String picId) {
 		Favorite fav = new Favorite();
 		fav.setId(PintuUtils.generateUID());
 		fav.setCollectTime(PintuUtils.getFormatNowTime());
 		fav.setOwner(userId);
 		fav.setPicture(picId);
 		return fav;
 	}
 
 	public String markFavoritePic(String userId, String picId) {
 		boolean flag = pintuService.checkExistFavorite(userId, picId);
 		if (flag) {// 图片已收藏，禁止重复收藏
 			return "alerady collect";
 		} else {
 			Favorite fav = this.createFavorite(userId, picId);
 			return pintuService.markFavoritePic(fav);
 		}
 	}
 
 	public String deleteOneFavorite(String fId) {
 		return pintuService.deleteOneFavorite(fId);
 	}
 
 	private void removeJsonKey(JSONArray jsonArray) {
 		for (int i = 0; i < jsonArray.length(); i++) {
 			jsonArray.getJSONObject(i).remove("mobImgPath");
 			jsonArray.getJSONObject(i).remove("rawImgPath");
 			jsonArray.getJSONObject(i).remove("mobImgSize");
 			jsonArray.getJSONObject(i).remove("rawImgSize");
 		}
 	}
 
 	/**
 	 * 得到热图列表
 	 * 
 	 * @return
 	 */
 	public String getHotPicture() {
 		List<TPicDetails> hotList = pintuService.getHotPicture();
 		JSONArray jsonArray = JSONArray.fromCollection(hotList);
 		removeJsonKey(jsonArray);
 		return jsonArray.toString();
 	}
 
 	public String getFavorTpics(String userId, int pageNum) {
 		List<TPicItem> favorList = pintuService.getFavoriteTpics(userId,
 				pageNum);
 		JSONArray jsonArray = JSONArray.fromCollection(favorList);
 		removeJsonKey(jsonArray);
 		return jsonArray.toString();
 	}
 
 	public String getTpicsByUser(String userId, int pageNum) {
 		List<TPicItem> userPicList = pintuService.getTpicsByUser(userId,
 				pageNum);
 		JSONArray jsonArray = JSONArray.fromCollection(userPicList);
 		removeJsonKey(jsonArray);
 		return jsonArray.toString();
 	}
 
 	public String getStoryiesByUser(String userId, int pageNum) {
 		List<StoryDetails> userStoryList = pintuService.getStroiesByUser(
 				userId, pageNum);
 		return JSONArray.fromCollection(userStoryList).toString();
 	}
 
 	public String getExchangeableGifts() {
 		List<Gift> list = pintuService.getExchangeableGifts();
 		return JSONArray.fromCollection(list).toString();
 	}
 
 	public String getCommunityEvents() {
 		List<Event> list = pintuService.getCommunityEvents();
 		return JSONArray.fromCollection(list).toString();
 	}
 
 	public String publishCommunityEvent(String title, String detail,
 			String time) {
 		Event eve = new Event();
 		eve.setId(PintuUtils.generateUID());
 		eve.setTitle(title);
 		eve.setDetail(detail);
 		eve.setEventTime(time);
 		return pintuService.publishCommunityEvent(eve);
 	}
 
 	public boolean publishExchangeableGift() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	public String getLatestTPicDesc() {
 		List<TPicReview> list = pintuService.getLatestTPicDesc();
 		return JSONArray.fromCollection(list).toString();
 	}
 
 	public String getExistUser(String account, String pwd) {
 		String result = pintuService.getExistUser(account, pwd);
 		return result;
 	}
 
 	public String registerUser(String account, String pwd, String code, String nick) {
 		String prompt = pintuService.registerUser(account, pwd, code,nick);
 		return prompt;
 	}
 
 	public int validateAccount(String account) {
 		int result = pintuService.validateAccount(account);
 		return result;
 	}
 
 	public int examineNickname(String nickName) {
 		int result = pintuService.examineNickname(nickName);
 		return result;
 	}
 	
 	public String sendApply(String account, String reason) {
 		String prompt = pintuService.sendApply(account, reason);
 		return prompt;
 	}
 
 	public String acceptApply( String account, String url, String opt) {
 		String prompt = pintuService.acceptApply(account, url, opt);
 		return prompt;
 	}
 
 	public String getApplicant() {
 		List<Applicant> list = pintuService.getApplicant();
 		return JSONArray.fromCollection(list).toString();
 	}
 
 	public boolean examineUser(String userId) {
 		boolean flag = pintuService.examineUser(userId);
 		return flag;
 	}
 
 	public String collectStatistics() {
 		List<TPicDetails> list = pintuService.collectStatistics();
 		JSONArray jsonArray = JSONArray.fromCollection(list);
 		removeJsonKey(jsonArray);
 		return jsonArray.toString();
 	}
 
 	public String classicalStatistics() {
 		List<TPicDetails> list = pintuService.classicalStatistics();
 		JSONArray jsonArray = JSONArray.fromCollection(list);
 		removeJsonKey(jsonArray);
 		return jsonArray.toString();
 	}
 
 	public String getGalleryForWeb(int pageNum) {
 		List<TPicDetails> list = pintuService.getGalleryForWeb(pageNum);
 		JSONArray jsonArray = JSONArray.fromCollection(list);
 		removeJsonKey(jsonArray);
 		return jsonArray.toString();
 	}
 
 	public String searchByTag(String tags) {
 		List<TPicDetails> list = pintuService.searchByTag(tags);
 		JSONArray jsonArray = JSONArray.fromCollection(list);
 		removeJsonKey(jsonArray);
 		return jsonArray.toString();
 	}
 
 	public String getHotTags() {
 		List<Tag> tagList = pintuService.getHotTags();
 		return JSONArray.fromCollection(tagList).toString();
 	}
 
 	public String geSystemTags() {
 		List<Tag> tagList = pintuService.geSystemTags();
 		return JSONArray.fromCollection(tagList).toString();
 	}
 
 	public String deleteOneCmt(String sId) {
 		return pintuService.deleteOneComment(sId);
 	}
 
 	public String deleteOnePic(String pId) {
 		return pintuService.deleteOnePicture(pId);
 	}
 
 	public String getThumbnailsByTag(String tagId, int pageNum) {
 		List<TPicDesc> list = pintuService.getThumbnailsByTag(tagId, pageNum);
 		return JSONArray.fromCollection(list).toString();
 	}
 
 	public String getPicDaren() {
 		List<User> list = pintuService.getPicDaren();
 		return JSONArray.fromCollection(list).toString();
 	}
 	
 
 	public String getCmtDaren() {
 		List<User> list = pintuService.getCmtDaren();
 		return JSONArray.fromCollection(list).toString();
 	}
 
 	public String getPicCoolCount(String picId) {
 		int result = pintuService.getPicCoolCount(picId);
 		return String.valueOf(result);
 	}
 
 	public String modifyPasswordById(String userId, String newPwd) {
 		
 		return pintuService.modifyPasswordById(userId,newPwd);
 	}
 
 	public int confirmPassword(String userId, String password) {
 		int result = pintuService.confirmPassword(userId,password);
 		return result;
 	}
 
 	public void createAvatar(List<FileItem> fileItems) {
 		FileItem avatarData = null;
 		String userId = "";
 		String nickName = "";
 		for (int i = 0; i < fileItems.size(); i++) {
 			FileItem item = fileItems.get(i);
 			if (!item.isFormField()) {
 				//头像图片数据
 				avatarData = item;
 			}
 			
 			if (item.isFormField()) {
 				// 取传头像的人
 				if (item.getFieldName().equals("userId")) {
 					userId = item.getString();
 				}
 				//要修改的昵称
 				if (item.getFieldName().equals("nickName")) {
 					try {
 						nickName = item.getString("UTF-8");
 					} catch (UnsupportedEncodingException e) {
 						e.printStackTrace();
 					}
 				}
 			}
 		}
 	
 		pintuService.createAvatarImg(avatarData,userId,nickName);
 		
 	}
 
 	public String getRandGallery() {
 		List<TPicDesc> list = pintuService.getRandGallery();
 		return JSONArray.fromCollection(list).toString();
 	}
 
 	public String getActiveUserRanking() {
 		List<User> activeList = pintuService.getActiveUserRandking();
 		return  JSONArray.fromCollection(activeList).toString();
 	}
 
 	public String reviewPictureById(String picId, String creationTime) {
 		String result = pintuService.reviewPictureById(picId, creationTime);
 		return result;
 	}
 
 	public String createInviteCode() {
 		String result = pintuService.createInviteCode();
 		return result;
 	}
 
 	public int checkApplicant(String account) {
 		int resut = pintuService.checkApplicant(account);
 		return resut;
 	}
 
 	public String retrievePwd(String account) {
 		String result = pintuService.retrievePwd(account);
 		return result;
 	}
 	
 	public boolean isProcessed(String account) {
 		//先判断申请表里是否还有此用户
 		int live = pintuService.checkApplicant(account);
 		if(live == 1){
 			//如果有，再看一下是否被审批过
 			int result = pintuService.checkAcceptApplicant(account);
 			if(result == 1){
 				//已被审批过
 				return true;
 			}
 		}else{
 			return true;
 		} 
 		return false;
 	}
 	
 
 	//爱品图微广告
 	public String getTodayAds(String userId) {
 		String today = PintuUtils.getFormatNowTime();
 		 List<Ads> adList = adService.getTodayAds(today,userId);
 		 JSONArray jsonArray = JSONArray.fromCollection(adList);
 		 processPath(jsonArray);
 		 return jsonArray.toString();
 	}
 
 	private void processPath(JSONArray jsonArray) {
 		for (int i = 0; i < jsonArray.length(); i++) {
 			String absolutePath = jsonArray.getJSONObject(i).get("imgPath").toString();
 			if(!"".equals(absolutePath)){
 				//先从字符串中找到文件夹uploadFile的位置，再加上uploadFile的长度10，即可截取到下属文件路径
 				int position = absolutePath.lastIndexOf("uploadFile");
 				String relativePath = absolutePath.substring(position+10);
 				jsonArray.getJSONObject(i).set("imgPath", relativePath);
 			}
 		}
 	}
 
 	public String searchAds(String keys, String time, String venderId) {
 		 List<Ads> adList = adService.searchAds(keys ,time ,venderId);
 		 JSONArray jsonArray = JSONArray.fromCollection(adList);
 		 processPath(jsonArray);
 		 return jsonArray.toString();
 	}
 	
 	
 
 	public String deleteAdsById(String adId) {
 		String res = adService.deleteAdsById(adId);
 		return res;
 	}
 
 	public String createAds(String publisher, String type, String priority, String startTime, String endTime, String content, String link) {
 		String res = adService.createAds(publisher,type,adImgPath,priority,startTime,endTime,content,link);
 		return res;
 	}
 
 	public String getAdsById(String adId) {
 		Ads ad = adService.getAdsById(adId);
 		return JSONObject.fromObject(ad).toString();
 	}
 
	public String updateAdsById(String adId, String publisher, String type, String priority, String startTime, String endTime, String content, String link) {
		String imgPath = "";
		if(type.equals("image")){
			imgPath = adImgPath;
		}
 		String res = adService.updateAdsById(adId,publisher,type,imgPath,priority,startTime,endTime,content,link);
 		return res;
 	}
 	
 	public String adImgPath = "";
 
 	public String createAdImg(List<FileItem> fileItems) {
 		FileItem adData = null;
 		for (int i = 0; i < fileItems.size(); i++) {
 			FileItem item = fileItems.get(i);
 			if (!item.isFormField()) {
 				//图片数据
 				adData = item;
 			}
 		}
 		adImgPath = pintuService.createAdImg(adData);
 		return adImgPath;
 	}
 
 	public void getImgByRelativePath(String relativePath, HttpServletResponse res) {
 		pintuService.getImgByRelativePath(relativePath, res);
 	}
 
 	public String getExistVender(String email, String pwd) {
 		String result = adService.getExistVender(email,pwd);
 		return result;
 	}
 
 	public int changePwd(String newPwd, String venderId) {
 		int result = adService.changePwd(newPwd,venderId);
 		return result;
 	}
 
 	public String searchVenders(String keys) {
 		 List<Vender> adList = adService.searchVenders(keys);
 		 return JSONArray.fromCollection(adList).toString();
 	}
 
 	public String getVendersById(String venderId) {
 		Vender vender = adService.getVendersById(venderId);
 		return JSONObject.fromObject(vender).toString();
 	}
 
 	public String updateVendersById(String venderId, String name, String email,
 			String serviceLevel, String effectiveTime, String deadTime,
 			String deployDNS, String enable) {
 		String res = adService.updateVendersById(venderId,name,email,serviceLevel,effectiveTime,deadTime,deployDNS,enable);
 		return res;
 	}
 
 	public String createVenders(String name, String email, String serviceLevel,
 			String effectiveTime, String deadTime, String deployDNS, String enable) {
 		String res = adService.createVenders(name,email,serviceLevel,effectiveTime,deadTime,deployDNS,enable);
 		return res;
 	}
 
 	public String validateVender(String venderId) {
 		String flag = adService.validateVender(venderId);
 		return flag;
 	}
 
 	public String registVenders(String name, String email, String pwd,
 			String deployDNS) {
 		String res = adService.registVenders(name,email,pwd,deployDNS);
 		return res;
 	}
 
 	public int checkoutRegister(String email) {
 		int res = adService.checkoutRegiser(email); 
 		return res;
 	}
 
 
 } // end of class
