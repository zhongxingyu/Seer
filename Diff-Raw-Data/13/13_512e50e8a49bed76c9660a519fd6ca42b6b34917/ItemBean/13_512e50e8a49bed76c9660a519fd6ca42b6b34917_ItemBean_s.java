 package net.latroquette.web.beans.item;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ManagedProperty;
 import javax.faces.bean.ViewScoped;
 import javax.faces.context.FacesContext;
 
 import net.latroquette.common.database.data.file.File;
 import net.latroquette.common.database.data.file.FilesService;
 import net.latroquette.common.database.data.file.GarbageFileStatus;
 import net.latroquette.common.database.data.item.Item;
 import net.latroquette.common.database.data.item.ItemStatus;
 import net.latroquette.common.database.data.item.ItemsService;
 import net.latroquette.common.database.data.keyword.ExternalKeyword;
 import net.latroquette.common.database.data.keyword.Keyword;
 import net.latroquette.common.database.data.keyword.KeywordType;
 import net.latroquette.common.database.data.keyword.KeywordsService;
 import net.latroquette.common.database.data.keyword.MainKeyword;
 import net.latroquette.common.util.Services;
 import net.latroquette.web.beans.commons.NavigationBean;
 import net.latroquette.web.beans.profile.UserBean;
 import net.latroquette.web.security.SecurityUtil;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.myfaces.custom.fileupload.UploadedFile;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.adi3000.common.database.hibernate.DatabaseOperation;
 import com.adi3000.common.util.CommonUtil;
 import com.adi3000.common.util.optimizer.CommonValues;
 import com.adi3000.common.web.faces.FacesUtil;
 
 @ManagedBean
 @ViewScoped
 public class ItemBean implements Serializable {
 	private static final Logger logger = LoggerFactory.getLogger(ItemBean.class);
 	
 	@ManagedProperty(value=Services.ITEMS_SERVICE_JSF)
 	private transient ItemsService itemsService;
 	@ManagedProperty(value=Services.FILES_SERVICE_JSF)
 	private transient FilesService filesService;
 	@ManagedProperty(value=Services.KEYWORDS_SERVICE_JSF)
 	private transient KeywordsService keywordsService;
 	@ManagedProperty(value="#{request.requestURI}")
 	private String navigationPath;
 	
 	private static final String DRAFTS_VALIDATION_URL="/index/item?s="+ItemStatus.CREATED.getValue();
 	/**
 	 * 
 	 */
 	@ManagedProperty(value="#{userBean}")
 	private UserBean userBean;
 	private static final long serialVersionUID = 8400549541055176853L;
 	private Item item;
 	private UploadedFile newFile;
 	private List<String> wishies;
 	private List<String> keywords;
 	private List<File> fileList;
 	private String itemId;
 	private String keywordIds;
 
 	private boolean creating;
 
 	/**
 	 * @return the title
 	 */
 	public String getTitle() {
 		return item.getTitle();
 	}
 	/**
 	 * @param title the title to set
 	 */
 	public void setTitle(String title) {
 		this.item.setTitle(title);
 	}
 	/**
 	 * @return the description
 	 */
 	public String getDescription() {
 		return item.getDescription();
 	}
 	/**
 	 * @param description the description to set
 	 */
 	public void setDescription(String description) {
 		this.item.setDescription(description);
 	}
 	/**
 	 * @return the file1
 	 */
 	public UploadedFile getFile() {
 		return newFile;
 	}
 	/**
 	 * @param file1 the file1 to set
 	 */
 	public void setNewFile(UploadedFile file) {
 		this.newFile = file;
 	}
 	
 	public Item getItem(){
 		return item;
 	}
 	public void setItem(Item item){
 		this.item = item;
 	}
 
 	public String createItem(){
 		return registerItem(DatabaseOperation.INSERT);
 	}
 	public String updateItem(){
 		return registerItem(DatabaseOperation.UPDATE);
 	}
 	
 	private String registerItem(DatabaseOperation operation){
 		
 		for(File file : fileList){
 			file.setGarbageStatus(GarbageFileStatus.VALIDATE);
 		}
 		keywords = CommonUtil.parseStringToList(keywordIds);
 		String[] keywordInfo = null;
		List<MainKeyword> mainKeywordList = new ArrayList<>();
		List<ExternalKeyword> externalKeywordList = new ArrayList<>();
 		for(String keywordTypeAndId : keywords){
 			keywordInfo = keywordTypeAndId.split(CommonValues.INNER_SEPARATOR);
 			switch (KeywordType.get(Integer.valueOf(keywordInfo[0]))) {
 			case MAIN_KEYWORD:
 				mainKeywordList.add(keywordsService.getKeywordById(Integer.valueOf(keywordInfo[1])));
 				break;
 			case EXTERNAL_KEYWORD:
 				externalKeywordList.add(keywordsService.getExternalKeywordById(Integer.valueOf(keywordInfo[1])));
 				break;
 			default:
 				break;
 			}
 		}
 		item.setImageList(fileList);
 		item.setStatusId(ItemStatus.DRAFT);
 		item.setDatabaseOperation(operation);
		item.setKeywordList(mainKeywordList);
		item.setExternalKeywordList(externalKeywordList);
 		item = itemsService.modifyItem(item, userBean.getUser());
 		return FacesUtil.prepareRedirect("/item/viewItem?item="+item.getId());
 	}
 	
 	public String uploadPic(){
 		File file = filesService.uploadNewPicFile(newFile, userBean.getUser());
 		if(file == null){
 			FacesContext fc = FacesContext.getCurrentInstance();
 			FacesMessage msg = new FacesMessage("Uploading error", 
 					"Adding file : upload failed, please try later or ask for support");
 			msg.setSeverity(FacesMessage.SEVERITY_ERROR);
 			fc.addMessage(null, msg);
 			fc.validationFailed();
 		}else{
 			getFileList().add(file);
 		}
 		newFile = null;
 		return null;
 	}
 	
 	public String removePic(String imageId){
 		File image = (File) CommonUtil.findById(item.getImageList(), imageId);
 		if(item.getId() != null){
 			image.setGarbageStatus(GarbageFileStatus.NOT_LINKED);
 			filesService.modifyFile(image, userBean.getUser());
 		}else{
 			filesService.removeFile(image);
 		}
 		fileList.remove(image);
 		return null;
 	}
 	
 	public void setWishiesListString(String wishiesListString){
 		wishies = CommonUtil.parseStringToList(wishiesListString);
 	}
 	
 	public String getWishiesListString(){
 		return CommonUtil.formatListToString(wishies);
 	}
 	public UserBean getUserBean() {
 		return userBean;
 	}
 	public void setUserBean(UserBean userBean) {
 		this.userBean = userBean;
 	}
 	
 	public List<File> getFileList(){
 		if(CollectionUtils.isEmpty(fileList) && item != null){
 			this.fileList = item.getImageList();
 		}
 		if(this.fileList == null){
 			this.fileList = new ArrayList<File>();
 		}
 		return this.fileList;
 	}
 	
 	public void setFileList(List<File> fileList){
 		this.fileList = fileList;
 	}
 	
 	public UploadedFile getNewFile(){
 		return newFile;
 	}
 	/**
 	 * @return the itemId
 	 */
 	public String getItemId() {
 		return itemId;
 	}
 	/**
 	 * @param itemId the itemId to set
 	 */
 	public void setItemId(String itemId) {
 		this.itemId = itemId;
 	}
 	/**
 	 * @return the navigationPath
 	 */
 	public String getNavigationPath() {
 		return navigationPath;
 	}
 	/**
 	 * @param navigationPath the navigationPath to set
 	 */
 	public void setNavigationPath(String navigationPath) {
 		this.navigationPath = navigationPath;
 	}
 	/**
 	 * @param keywordsService the keywordsService to set
 	 */
 	public void setKeywordsService(KeywordsService keywordsService) {
 		this.keywordsService = keywordsService;
 	}
 	/**
 	 * @param filesService the filesService to set
 	 */
 	public void setFilesService(FilesService filesService) {
 		this.filesService = filesService;
 	}
 	/**
 	 * @param itemsService the itemsService to set
 	 */
 	public void setItemsService(ItemsService itemsService) {
 		this.itemsService = itemsService;
 	}
 
 	/**
 	 * @return the keywordIds
 	 */
 	public String getKeywordIds() {
 		return keywordIds;
 	}
 	/**
 	 * @param keywordIds the keywordIds to set
 	 */
 	public void setKeywordIds(String keywordIds) {
 		this.keywordIds = keywordIds;
 	}
 	public void loadItem(){
 		if(StringUtils.isNotEmpty(itemId) ){
 			item = itemsService.getItemById(Integer.valueOf(itemId));
 			keywords = new ArrayList<>();
 			//Loading keywords
 			if(item.getKeywordList() != null){
 				for(Keyword keyword : item.getKeywordList()){
 					keywords.add(keyword.getKeywordTypeId() + CommonValues.INNER_SEPARATOR + keyword.getId());
 				}
 			}
 			if(item.getExternalKeywordList() != null){
 				for(Keyword keyword : item.getExternalKeywordList()){
 					keywords.add(keyword.getKeywordTypeId() + CommonValues.INNER_SEPARATOR + keyword.getId());
 				}
 			}
 			keywordIds = CommonUtil.formatListToString(keywords);
 		}else if (!creating){
 			item = new Item();
 			creating =true;
 		}
 	}
 
 	public void checkItemAndUser(){
 		//First check if user is logged
 		boolean userCheck = userBean.isLoggedIn();
 		if(isEditing()){
 			userCheck = SecurityUtil.checkUserLogged(userBean);
 		}
 		//Next get item and check if user is available to modify this item
 		loadItem();
 		if (item.isIdSet() &&
 				userCheck &&
 				!userBean.getId().equals(item.getUser().getId())){
 				if(isEditing()){
 					//Pass to viewItem only if user is logged for this check
 					if(ItemStatus.APPROUVED.getValue().equals(item.getStatusId()) ||
 							userBean.isValidateItems()){
 						FacesUtil.navigationRedirect("/item/viewItem.xhtml?item=".concat(item.getId().toString()));
 					}else{
 						FacesUtil.navigationForward("/error404");
 					}
 				}else if(isViewing()){
 					if(!ItemStatus.APPROUVED.getValue().equals(item.getStatusId()) &&
 							!userBean.isValidateItems()){
 						FacesUtil.navigationForward("/error404");
 					}
 				}
 		//Redirect request with no item to view
 		}else if(!item.isIdSet() && isViewing()){
 			FacesUtil.navigationForward("/error404");
 		}
 		//Otherwise accept owner of item to edit and view item
 	}
 	
 	public boolean isOwner(){
 		return userBean.isLoggedIn() &&
 				item.isIdSet() &&  userBean.getId().equals(item.getUser().getId());
 	}
 	
 	private String modifyStatus(ItemStatus status, boolean needPrivileges){
 		if(needPrivileges && !userBean.isValidateItems()){
 			logger.warn("User " + userBean.getId() + " try to set status "+status+ " to item " + itemId + " without sufficient privilieges");
 			return userBean.logoutUser();
 		}else{
 			item.setStatusId(status.getValue());
 			item.setDatabaseOperation(DatabaseOperation.UPDATE);
 			itemsService.modifyItem(item, item.getUser());
 			//We redirect to draft list if we are using our privilieges
 			if(needPrivileges){
 				return DRAFTS_VALIDATION_URL;
 			}else{
 				return null;
 			}
 		}
 	}
 	public String validate(){
 		if(isOwner()){
 			return modifyStatus(ItemStatus.CREATED, false);
 		}else{
 			logger.warn("User " + userBean.getId() + " try to set status "+ItemStatus.CREATED+ " to item " + itemId + " without beeing owner");
 			return null;
 		}
 	}
 	public String end(){
 		if(isOwner()){
 			return modifyStatus(ItemStatus.FINISHED, false);
 		}else{
 			logger.warn("User " + userBean.getId() + " try to set status "+ItemStatus.FINISHED+ " to item " + itemId + " without beeing owner");
 			return null;
 		}
 	}
 	public String approuve(){
 		return modifyStatus(ItemStatus.APPROUVED, true);
 	}
 	public String block(){
 		return modifyStatus(ItemStatus.BLOCKED, true);
 	}
 	public String disapprouve(){
 		return modifyStatus(ItemStatus.DESAPPROUVED, true);
 	}
 	public boolean isDraft(){
 		return 
 				ItemStatus.DRAFT.getValue().equals(item.getStatusId());
 	}
 	public boolean isValidated(){
 		return 
 				ItemStatus.DESAPPROUVED.getValue().equals(item.getStatusId()) ||
 				ItemStatus.BLOCKED.getValue().equals(item.getStatusId()) ||
 				ItemStatus.APPROUVED.getValue().equals(item.getStatusId());
 	}
 	public boolean isEditing(){
 		return navigationPath.startsWith(NavigationBean.EDIT_ITEM_VIEW);
 	}
 	public boolean isViewing(){
 		return navigationPath.startsWith(NavigationBean.VIEW_ITEM_VIEW);
 	}
 }
