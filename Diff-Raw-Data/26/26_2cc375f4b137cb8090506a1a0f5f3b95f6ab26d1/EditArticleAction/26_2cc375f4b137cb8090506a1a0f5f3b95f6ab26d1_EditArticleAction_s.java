 package tw.com.dsc.web.action;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.servlet.ServletContext;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.struts2.util.ServletContextAware;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Component;
 
 import tw.com.dsc.domain.ActionType;
 import tw.com.dsc.domain.Article;
 import tw.com.dsc.domain.ArticleId;
 import tw.com.dsc.domain.ArticleLog;
 import tw.com.dsc.domain.ArticleType;
 import tw.com.dsc.domain.Attachment;
 import tw.com.dsc.domain.Language;
 import tw.com.dsc.domain.ProductSeries;
 import tw.com.dsc.domain.Project;
 import tw.com.dsc.domain.Source;
 import tw.com.dsc.domain.Technology;
 import tw.com.dsc.domain.support.BetweenCondition;
 import tw.com.dsc.domain.support.Page;
 import tw.com.dsc.service.ArticleLogService;
 import tw.com.dsc.service.ArticleService;
 import tw.com.dsc.service.AttachmentService;
 import tw.com.dsc.service.LanguageService;
 import tw.com.dsc.service.SystemService;
 import tw.com.dsc.to.JsonMsg;
 import tw.com.dsc.to.User;
 import tw.com.dsc.util.DateUtils;
 import tw.com.dsc.util.ThreadLocalHolder;
 
 import com.opensymphony.xwork2.ModelDriven;
 import com.opensymphony.xwork2.Preparable;
 
 @Component("editAction")
 @Scope("prototype")
 public class EditArticleAction extends BaseAction implements Preparable, ModelDriven<Article>, ServletContextAware {
 	private static final Logger logger = LoggerFactory.getLogger(EditArticleAction.class);
 	private static final long serialVersionUID = 626334753779135892L;
 
 	private Long oid;
 	private Long sourceOid; //Copy Source Article Oid
 	private Article article;
 	private Article sarticle; //Copy Source
 	private Article example;
 	private Article example1;
 	private Article example2;
 	private Article example3;
 	private Page<Article> page;
 	private Page<Article> unpublishedArticles;
 	private Page<Article> draftArticles;
 	private Page<Article> expiredArticles;
 	
 	private String statusAction;
 	
 	private File upload;
 	private String uploadFileName;
 	private String uploadContentType;
 	
 	private List<Language> languages;
 	private List<ArticleLog> articleLogs;
 	private List<Technology> technologies;
 	private List<Project> projects;
 	private List<ProductSeries> products;
 	private List<Language> copyLanguages;
 	@Autowired
 	private LanguageService languageService;
 	
 	@Autowired
 	private ArticleService articleService;
 	@Autowired
 	private AttachmentService attachmentService;
 	@Autowired
 	private SystemService systemService;
 	@Autowired
 	private ArticleLogService articleLogService;
 	
 	private ServletContext context;
 	private String message;
 	private String rejectReason;
 	private String articleIdOid;
 	private String languageOid;
 	
 	private Integer ratingNumber;
 	private String suggestion;
 	private JsonMsg jsonMsg;
 	private Integer pageNo;
 	
 	private Language lan;
 	private Boolean copySourceFirmware;
 	private String targetFirmware;
 	@Override
 	public void prepare() throws Exception {
 		if (null != this.oid) {
 			this.article = this.articleService.findByOid(oid);
 		}
 		
 		if (StringUtils.isNotEmpty(this.languageOid)) {
 			this.lan = this.languageService.findByOid(this.languageOid);
 		}
 		this.example = new Article();
 		this.example1 = new Article();
 		this.example2 = new Article();
 		this.example3 = new Article();
 		
 		this.example.setArticleId(new ArticleId());
 		this.example1.setArticleId(new ArticleId());
 		this.example2.setArticleId(new ArticleId());
 		this.example3.setArticleId(new ArticleId());
 		
 		if (null == this.article) {
 			this.article = new Article();
 			this.article.setArticleId(new ArticleId());
 			this.article.setLanguage(new Language());
 			this.article.setNews(Boolean.FALSE);
 		}
 		
 		page = new Page<Article>(example);
 		
 		this.languages = this.languageService.listAll();
 		this.technologies = this.systemService.listAllTech();
 		this.projects = this.systemService.listAllProject();
 		User op = ThreadLocalHolder.getOperator();
 		logger.debug("[{}]", op);
 		if (op.isL3()) {
 			this.products = this.systemService.listAllSeries();
 		} else {
 			this.products = this.systemService.listSeries(op.getCurrentUserRole().getBranchCode());
 		}
 	}
 	
 	public String list() {
 		this.unpublishedArticles = this.articleService.searchUnpublishedPage(new Page<Article>(example));
 		this.draftArticles = this.articleService.searchDraftPage(new Page<Article>(example));
 		this.expiredArticles = this.articleService.searchExpiredPage(new Page<Article>(example));
 		
 		return "list";
 	}
 	
 	public String searchUnpublished() {
 		page = new Page<Article>(example1);
 		if (null != pageNo) {
 			page.setPageNo(pageNo);
 		} else {
 			page.setPageNo(1);
 		}
 		if (null != example1.getEntryDate()) {
 			page.getConditions().add(new BetweenCondition("entryDate", DateUtils.begin(example1.getEntryDate()), DateUtils.end(example1.getEntryDate())));
 			example1.setEntryDate(null);
 		}
 		this.unpublishedArticles = this.articleService.searchUnpublishedPage(page);
 		return "unpublished";
 	}
 	
 	public String searchDraft() {
 		page = new Page<Article>(example2);
 		if (null != pageNo) {
 			page.setPageNo(pageNo);
 		} else {
 			page.setPageNo(1);
 		}
 		if (null != example2.getEntryDate()) {
 			page.getConditions().add(new BetweenCondition("entryDate", DateUtils.begin(example2.getEntryDate()), DateUtils.end(example2.getEntryDate())));
 			example2.setEntryDate(null);
 		}
 		this.draftArticles = this.articleService.searchDraftPage(page);
 		return "draft";
 	}
 	
 	public String searchExpired() {
 		page = new Page<Article>(example3);
 		if (null != pageNo) {
 			page.setPageNo(pageNo);
 		} else {
 			page.setPageNo(1);
 		}
 		if (null != example3.getEntryDate()) {
 			page.getConditions().add(new BetweenCondition("entryDate", DateUtils.begin(example3.getEntryDate()), DateUtils.end(example3.getEntryDate())));
 			example3.setEntryDate(null);
 		}
 		this.expiredArticles = this.articleService.searchExpiredPage(page);
 		return "expired";
 	}
 	
 	public String load() {
 		if (article.isReadonly()) {
 			return "readOnly";
 		}
 		return "edit";
 	}
 	
 	public String preCreate() {
 		String articleId = this.articleService.getNextArticleId();
 		logger.debug("Get new ArticleId[{}]", articleId);
 		article.setArticleId(new ArticleId(articleId));
 		article.setType(ArticleType.GeneralInfo);
 		article.setLanguage(this.languageService.findDefaultLanguage());
 		article.setHitCount(0);
 		article.setEntryUser(ThreadLocalHolder.getOperator().getAccount());
 		article.setEntryDate(new Date());
 //		article.setStatus(Status.Draft);
 		article.setNews(Boolean.FALSE);
 		article.setSource(Source.OBM);
 		return "create";
 	}
 	
 	public String create() {
 		if (null != this.upload) {
 			Attachment attachment = this.attachmentService.uploadFirmware(this.context.getContextPath(), upload, uploadFileName, uploadContentType);
 			this.article.setFirmware(attachment);
 		}
 		if (null != this.lan) {
 			this.article.setLanguage(lan);
 		}
 //		this.article.setArticleId(new ArticleId(articleIdOid));
 		//For Copy Action
 		if(null != this.sourceOid) {
 			this.sarticle = this.articleService.findByOid(sourceOid);
 			this.article.setArticleId(this.sarticle.getArticleId());
 			if (Boolean.TRUE.equals(this.copySourceFirmware)) {
 				this.article.setFirmware(this.sarticle.getFirmware());
 			}
 			
 		}
 		
 		if ("Draft".equals(statusAction)) {
 			this.articleService.draftNewArticle(article);
 		} else if ("WaitForApproving".equals(statusAction)) {
 			this.articleService.finalNewArticle(article);
 		} else if ("Published".equals(statusAction) ) {
 			this.articleService.publishNewL2Article(article);
 		} else if ("WaitForProofRead".equals(statusAction)) {
 			this.articleService.publishNewL3Article(article);
 		} 
 		
 		return this.list();
 	}
 	
 	public String previewSave() {
 		return preview();
 	}
 	
 	public String preview() {
 		/*
 		if (null != this.article && null != this.article.getLanguage()) {
 			Language lan = this.article.getLanguage();
 			if (StringUtils.isNotEmpty(lan.getOid()) && StringUtils.isEmpty(lan.getName())) {
 				String loid = lan.getOid();
 				lan = this.languageService.findByOid(loid);
 				if (null != lan) {
 					this.article.setLanguage(lan);
 				} else {
 					logger.error("Please check Language options named[{}]", loid);
 				}
 			}
 		}
 		*/
 		if (null != this.lan) {
 			this.article.setLanguage(lan);
 		}
 		if(null != this.sourceOid) {
 			this.sarticle = this.articleService.findByOid(sourceOid);
 			if (Boolean.TRUE.equals(this.copySourceFirmware)) {
 				this.article.setFirmware(this.sarticle.getFirmware());
 			}
 			if (null != this.article.getLanguage() && StringUtils.isNotEmpty(this.article.getLanguage().getOid())) {
 				String lanOid = this.article.getLanguage().getOid();
 				Language lan = this.languageService.findByOid(lanOid);
 				if (null != lan) {
 					this.article.setLanguage(lan);
 				}
 			}
  		}
 		if (StringUtils.isEmpty(this.targetFirmware) && (null != this.article.getFirmware())) {
 			this.targetFirmware = this.article.getFirmware().getName();
 		}
 		return "preview";
 	}
 	
 	public String preCopy() {
 		this.copyLanguages = new ArrayList<Language>(this.languages);
 		
 		if(null != this.sourceOid) {
 			this.sarticle = this.articleService.findByOid(sourceOid);
 			this.copyLanguages.remove(this.sarticle.getLanguage());
 		}
 		
 		return "copy";
 	}
 	
 	public String save() {
 		if (null != this.upload) {
 			Attachment attachment = this.attachmentService.uploadFirmware(this.context.getContextPath(), upload, uploadFileName, uploadContentType);
 			this.article.setFirmware(attachment);
 		}
 		
 		if (null != this.lan && !this.lan.getOid().equals(this.article.getLanguage().getOid())) {
 			this.article.setLanguage(this.lan);
 		}
 //		Language lang = this.languageService.findByOid(this.article.getLanguage().getOid());
 //		this.article.setLanguage(lang);
 		this.article.setUpdateDate(new Date());
 				
 		if (StringUtils.isEmpty(this.statusAction)) {
 			this.articleService.saveOrUpdate(article);
 		} else if ("WaitForProofRead".equals(this.statusAction)) {
 			this.articleService.approve(article);
 		} else if ("Draft".equals(this.statusAction)) {
 			this.articleService.reject(article, this.rejectReason);
 		} else if ("Published".equals(this.statusAction)) {
 			this.articleService.publish(article);
 		} else if ("WaitForApproving".equals(this.statusAction)) {
 			this.articleService.finalArticle(article);
 		} else if ("ReadyToUpdate".equals(this.statusAction)) {
 			this.articleService.readyUpdate(article);
 		} else if ("ReadyToPublish".equals(this.statusAction)) {
 			this.articleService.readyPublish(article);
 		} else if ("Archived".equals(this.statusAction)) {
 			this.articleService.archive(article);
 		}
 		this.addActionMessage("Save Success!");
 		return this.list();
 	}
 	
 	public String updateStatus() {
 		if (StringUtils.isEmpty(this.statusAction)) {
 			this.articleService.saveOrUpdate(article);
 		} else if ("WaitForProofRead".equals(this.statusAction)) {
 			this.articleService.approve(article);
 		} else if ("Draft".equals(this.statusAction)) {
 			this.articleService.reject(article, this.rejectReason);
 		} else if ("Published".equals(this.statusAction)) {
 			this.articleService.publish(article);
 		} else if ("WaitForApproving".equals(this.statusAction)) {
 			this.articleService.finalArticle(article);
 		} else if ("ReadyToUpdate".equals(this.statusAction)) {
 			this.articleService.readyUpdate(article);
 		} else if ("ReadyToPublish".equals(this.statusAction)) {
 			this.articleService.readyPublish(article);
 		} else if ("Archived".equals(this.statusAction)) {
 			this.articleService.archive(article);
 		}
 		this.addActionMessage("Update Status Success!");
 		return this.list();
 	}
 	public String disable() {
 		logger.debug("Disable has been invoked");
 		this.articleService.disable(article);
 		return this.list();
 	}
 	public String viewLogs() {
 		ArticleLog exam = new ArticleLog();
 		exam.setArticleOid(oid);
 		this.articleLogs = this.articleLogService.listByExample(exam);
 		return "log";
 	}
 	
 	public String viewRejectLogs() {
 		ArticleLog exam = new ArticleLog();
 		exam.setArticleOid(oid);
 		exam.setAction(ActionType.Reject);
 		this.articleLogs = this.articleLogService.listByExample(exam);
 		return "log";
 	}
 	
 	public String rating() {
 		this.articleService.rate(article, ratingNumber);
 		this.jsonMsg = new JsonMsg("Thanks for your rating for articleId");
 		return "rating";
 	}
 	
 	public String suggest() {
 		this.articleService.comment(article, suggestion);
 		this.jsonMsg = new JsonMsg("Thanks for your suggestion, ["+this.suggestion+"]");
 		return "rating";
 	}
 	
 	public String quickEdit() {
 		return "quickEdit";
 	}
 	
 	public List<ProductSeries> getSeries() {
 		return systemService.listAllSeries();
 	}
 	
 	public Long getOid() {
 		return oid;
 	}
 	public void setOid(Long oid) {
 		this.oid = oid;
 	}
 	public Article getArticle() {
 		return article;
 	}
 	public void setArticle(Article article) {
 		this.article = article;
 	}
 
 	public Page<Article> getPage() {
 		return page;
 	}
 
 	public void setPage(Page<Article> page) {
 		this.page = page;
 	}
 
 	public Page<Article> getUnpublishedArticles() {
 		return unpublishedArticles;
 	}
 
 	public void setUnpublishedArticles(Page<Article> unpublishedArticles) {
 		this.unpublishedArticles = unpublishedArticles;
 	}
 
 	public Page<Article> getDraftArticles() {
 		return draftArticles;
 	}
 
 	public void setDraftArticles(Page<Article> draftArticles) {
 		this.draftArticles = draftArticles;
 	}
 
 	public Page<Article> getExpiredArticles() {
 		return expiredArticles;
 	}
 
 	public void setExpiredArticles(Page<Article> expiredArticles) {
 		this.expiredArticles = expiredArticles;
 	}
 
 	public AttachmentService getAttachmentService() {
 		return attachmentService;
 	}
 
 	public void setAttachmentService(AttachmentService attachmentService) {
 		this.attachmentService = attachmentService;
 	}
 
 	public ServletContext getContext() {
 		return context;
 	}
 
 	public void setContext(ServletContext context) {
 		this.context = context;
 	}
 
 	public Article getExample() {
 		return example;
 	}
 
 	public void setExample(Article example) {
 		this.example = example;
 	}
 	
 	public Article getExample1() {
 		return example1;
 	}
 
 	public void setExample1(Article example1) {
 		this.example1 = example1;
 	}
 
 	public Article getExample2() {
 		return example2;
 	}
 
 	public void setExample2(Article example2) {
 		this.example2 = example2;
 	}
 
 	public Article getExample3() {
 		return example3;
 	}
 
 	public void setExample3(Article example3) {
 		this.example3 = example3;
 	}
 
 	public String getMessage() {
 		return message;
 	}
 
 	public void setMessage(String message) {
 		this.message = message;
 	}
 	public User getCurrentUser() {
 		return ThreadLocalHolder.getUser();
 	}
 
 	@Override
 	public Article getModel() {
 		return this.article;
 	}
 
 	public List<Language> getLanguages() {
 		return languages;
 	}
 
 	public void setLanguages(List<Language> languages) {
 		this.languages = languages;
 	}
 
 	public LanguageService getLanguageService() {
 		return languageService;
 	}
 
 	public void setLanguageService(LanguageService languageService) {
 		this.languageService = languageService;
 	}
 
 	public ArticleLogService getArticleLogService() {
 		return articleLogService;
 	}
 
 	public void setArticleLogService(ArticleLogService articleLogService) {
 		this.articleLogService = articleLogService;
 	}
 
 	public File getUpload() {
 		return upload;
 	}
 
 	public void setUpload(File upload) {
 		this.upload = upload;
 	}
 
 	public String getUploadFileName() {
 		return uploadFileName;
 	}
 
 	public void setUploadFileName(String uploadFileName) {
 		this.uploadFileName = uploadFileName;
 	}
 
 	public String getUploadContentType() {
 		return uploadContentType;
 	}
 
 	public void setUploadContentType(String uploadContentType) {
 		this.uploadContentType = uploadContentType;
 	}
 
 	public ArticleService getArticleService() {
 		return articleService;
 	}
 
 	public void setArticleService(ArticleService articleService) {
 		this.articleService = articleService;
 	}
 	
 	public String getStatusAction() {
 		return statusAction;
 	}
 
 	public void setStatusAction(String statusAction) {
 		this.statusAction = statusAction;
 	}
 	
 	public String getRejectReason() {
 		return rejectReason;
 	}
 
 	public void setRejectReason(String rejectReason) {
 		this.rejectReason = rejectReason;
 	}
 
 	
 	public String getArticleIdOid() {
 		return articleIdOid;
 	}
 
 	public void setArticleIdOid(String articleIdOid) {
 		this.articleIdOid = articleIdOid;
 	}
 
 	public List<ArticleLog> getArticleLogs() {
 		return articleLogs;
 	}
 
 	public void setArticleLogs(List<ArticleLog> articleLogs) {
 		this.articleLogs = articleLogs;
 	}
 
 	public Long getSourceOid() {
 		return sourceOid;
 	}
 
 	public void setSourceOid(Long sourceOid) {
 		this.sourceOid = sourceOid;
 	}
 
 	public Article getSarticle() {
 		return sarticle;
 	}
 
 	public void setSarticle(Article sarticle) {
 		this.sarticle = sarticle;
 	}
 	
 	public Integer getRatingNumber() {
 		return ratingNumber;
 	}
 
 	public void setRatingNumber(Integer ratingNumber) {
 		this.ratingNumber = ratingNumber;
 	}
 
 	public String getSuggestion() {
 		return suggestion;
 	}
 
 	public void setSuggestion(String suggestion) {
 		this.suggestion = suggestion;
 	}
 
 	public JsonMsg getJsonMsg() {
 		return jsonMsg;
 	}
 
 	public void setJsonMsg(JsonMsg jsonMsg) {
 		this.jsonMsg = jsonMsg;
 	}
 
 	@Override
 	public void setServletContext(ServletContext context) {
 		this.context = context;
 	}
 
 	public String getLanguageOid() {
 		return languageOid;
 	}
 
 	public void setLanguageOid(String languageOid) {
 		this.languageOid = languageOid;
 	}
 
 	public List<Technology> getTechnologies() {
 		return technologies;
 	}
 
 	public void setTechnologies(List<Technology> technologies) {
 		this.technologies = technologies;
 	}
 
 	public List<Project> getProjects() {
 		return projects;
 	}
 
 	public void setProjects(List<Project> projects) {
 		this.projects = projects;
 	}
 
 	public List<ProductSeries> getProducts() {
 		return products;
 	}
 
 	public void setProducts(List<ProductSeries> products) {
 		this.products = products;
 	}
 
 	public List<Language> getCopyLanguages() {
 		return copyLanguages;
 	}
 
 	public void setCopyLanguages(List<Language> copyLanguages) {
 		this.copyLanguages = copyLanguages;
 	}
 
 	public Boolean getCopySourceFirmware() {
 		return copySourceFirmware;
 	}
 
 	public void setCopySourceFirmware(Boolean copySourceFirmware) {
 		this.copySourceFirmware = copySourceFirmware;
 	}
 
 	public String getTargetFirmware() {
 		return targetFirmware;
 	}
 
 	public void setTargetFirmware(String targetFirmware) {
 		this.targetFirmware = targetFirmware;
 	}
 
 	public Integer getPageNo() {
 		return pageNo;
 	}
 
 	public void setPageNo(Integer pageNo) {
 		this.pageNo = pageNo;
 	}
 	
 }
