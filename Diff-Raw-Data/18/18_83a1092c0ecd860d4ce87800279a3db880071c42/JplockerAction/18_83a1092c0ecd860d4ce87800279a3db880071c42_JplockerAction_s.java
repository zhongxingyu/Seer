 package com.tp.action;
 
 import java.util.Collections;
 import java.util.List;
 
 import javax.servlet.http.HttpSession;
 
 import org.apache.struts2.convention.annotation.Namespace;
 import org.apache.struts2.convention.annotation.Result;
 import org.apache.struts2.convention.annotation.Results;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import com.opensymphony.xwork2.ActionSupport;
 import com.tp.entity.CategoryInfo;
 import com.tp.entity.FileMarketValue;
 import com.tp.entity.FileStoreInfo;
 import com.tp.entity.Market;
 import com.tp.entity.Shelf;
 import com.tp.entity.ThemeFile;
 import com.tp.orm.Page;
 import com.tp.service.CategoryManager;
 import com.tp.service.FileManager;
 import com.tp.service.MarketManager;
 import com.tp.utils.Constants;
 import com.tp.utils.Struts2Utils;
 
 @Namespace("/store")
 @Results({ @Result(name = "reload", location = "jplocker.action", type = "redirect") })
 public class JplockerAction extends ActionSupport {
 
 	private static final long serialVersionUID = 1L;
 
 	private CategoryManager categoryManager;
 
 	private FileManager fileManager;
 	private MarketManager marketManager;
 
 	private Page<FileStoreInfo> hottestPage = new Page<FileStoreInfo>();
 	private Page<FileStoreInfo> recommendPage = new Page<FileStoreInfo>();
 
 	private Page<FileStoreInfo> newestPage = new Page<FileStoreInfo>();
 	private Page<FileStoreInfo> catePage = new Page<FileStoreInfo>();
 
 	private FileStoreInfo info;
 
 	private List<CategoryInfo> cateInfos;
 	private Long categoryId;
 	private ThemeFile adFile;
 
 	private String categoryName;
 	private String language;
 
 	@Override
 	public String execute() throws Exception {
 
 		return list();
 	}
 
 	/**
 	 * 商店首页显示列表
 	 * @return
 	 * @throws Exception
 	 */
 	public String list() throws Exception {
 
 		HttpSession session = Struts2Utils.getSession();
 
 		language = (String) session.getAttribute(Constants.PARA_LANGUAGE);
 
 		Long storeId = chooseStoreId(session);
 
 		hottestPage.setPageSize(20);
 		hottestPage = fileManager.searchStoreInfoInShelf(hottestPage, Shelf.Type.HOTTEST, storeId, language);
 
 		newestPage = fileManager.searchStoreInfoInShelf(newestPage, Shelf.Type.NEWEST, storeId, language);
		Market market = this.getMarket(session);
		List<FileStoreInfo> newestList = newestPage.getResult();
		for (FileStoreInfo info : newestList) {
			setDownloadType(market, info);
		}
 		if (visitByBrowse(session)) {
 			recommendPage = fileManager.searchStoreInfoInShelf(recommendPage, Shelf.Type.RECOMMEND, storeId, language);
 			List<FileStoreInfo> recommendFiles = recommendPage.getResult();
 			if (recommendFiles.size() > 0) {
 				Collections.shuffle(recommendFiles);
 				adFile = recommendFiles.get(0).getTheme();
 			}
 		}
 		return SUCCESS;
 	}
 
 	private Long chooseStoreId(HttpSession session) {
 		String storeType = (String) session.getAttribute(Constants.PARA_STORE_TYPE);
 		Long storeId = (Long) session.getAttribute(Constants.ID_JPLOCKER);
 		if (storeType != null && storeId != null && storeType.equals(Constants.JP_LOCKER)) {
 			return storeId;
 		} else {
 			storeId = categoryManager.getStoreByValue(Constants.JP_LOCKER).getId();
 			session.setAttribute(Constants.ID_JPLOCKER, storeId);
 			return storeId;
 		}
 	}
 
 	private boolean visitByBrowse(HttpSession session) {
 
 		return (String) session.getAttribute(Constants.PARA_RESOLUTION) == null;
 	}
 
 	/**
 	 * 输出5个广告xml
 	 * @return
 	 * @throws Exception
 	 */
 	public String adXml() throws Exception {
 		Long storeId = categoryManager.getStoreByValue(Constants.JP_LOCKER).getId();
 		Page<ThemeFile> adPage = new Page<ThemeFile>();
 		adPage = fileManager.searchFileByShelf(adPage, Shelf.Type.RECOMMEND, storeId);
 		String domain = Constants.getDomain();
 		String xml = fileManager.jplockerAdXml(adPage.getResult(), domain);
 		Struts2Utils.renderXml(xml);
 		return null;
 	}
 
 	private Market getMarket(HttpSession session) {
 		String fromMarket = (String) session.getAttribute(Constants.PARA_FROM_MARKET);
 		if (fromMarket == null || fromMarket.isEmpty()) {
 			fromMarket = Constants.MARKET_GOOGLE;
 		}
 		return marketManager.findByPkName(fromMarket);
 	}
 
 	private void setDownloadType(Market market, FileStoreInfo info) {
 		List<ThemeFile> files = market.getThemes();
 		if (files.contains(info.getTheme())) {
 			String uri = market.getMarketKey() + info.getTheme().getMarketURL();
 			if (market.getPkName().equals(Constants.LENVOL_STORE)) {
 				uri += ("&versioncode=" + info.getTheme().getVersion());
 			}
 			if (market.getPkName().equals(Constants.OPPO_NEARME)) {
 				List<FileMarketValue> fvs = info.getTheme().getMarketValues();
 				for (FileMarketValue fm : fvs) {
 					uri += (fm.getKeyName() + "=" + fm.getKeyValue());
 				}
 			}
 			info.getTheme().setDownloadURL(uri);
 		} else if (info.getPrice() != null) {
 			info.getTheme().setDownloadURL("");
 		}
 	}
 
 	@Autowired
 	public void setFileManager(FileManager fileManager) {
 		this.fileManager = fileManager;
 	}
 
 	@Autowired
 	public void setCategoryManager(CategoryManager categoryManager) {
 		this.categoryManager = categoryManager;
 	}
 
 	@Autowired
 	public void setMarketManager(MarketManager marketManager) {
 		this.marketManager = marketManager;
 	}
 
 	public Page<FileStoreInfo> getHottestPage() {
 		return hottestPage;
 	}
 
 	public Page<FileStoreInfo> getNewestPage() {
 		return newestPage;
 	}
 
 	public Page<FileStoreInfo> getRecommendPage() {
 		return recommendPage;
 	}
 
 	public Page<FileStoreInfo> getCatePage() {
 		return catePage;
 	}
 
 	public FileStoreInfo getInfo() {
 		return info;
 	}
 
 	public void setInfo(FileStoreInfo info) {
 		this.info = info;
 	}
 
 	public List<CategoryInfo> getCateInfos() {
 		return cateInfos;
 	}
 
 	public Long getCategoryId() {
 		return categoryId;
 	}
 
 	public ThemeFile getAdFile() {
 		return adFile;
 	}
 
 	public String getCategoryName() {
 		return categoryName;
 	}
 
 	public String getLanguage() {
 		return language;
 	}
 }
