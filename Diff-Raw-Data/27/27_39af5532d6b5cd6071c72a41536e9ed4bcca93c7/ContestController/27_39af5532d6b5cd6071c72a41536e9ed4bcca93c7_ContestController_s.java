 package org.icemobile.samples.spring.mediacast;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.GregorianCalendar;
 import java.util.Iterator;
 import java.util.List;
 import java.util.UUID;
 
 import javax.inject.Inject;
 import javax.servlet.ServletContext;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.validation.Valid;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.icepush.PushContext;
 import org.icemobile.jsp.tags.TagUtil;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.CookieValue;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.bind.annotation.SessionAttributes;
 import org.springframework.web.context.ServletContextAware;
 import org.springframework.web.context.request.WebRequest;
 import org.springframework.web.multipart.MultipartFile;
 
 @Controller
@SessionAttributes({"uploadModel","msg","admin","desktop", "sx", TagUtil.USER_AGENT_COOKIE})
 public class ContestController implements ServletContextAware {
 
 	@Inject
 	private MediaService mediaService;
 	
 	@Inject
 	private MediaHelper mediaHelper;
 
 	String currentFileName = null;
 
 	private ServletContext servletContext;
 	
 	private final static String EDIT_KEY_PREFIX = "ice";
 
 	private static final Log log = LogFactory
 			.getLog(ContestController.class);
 	
 	private static final String PAGE_UPLOAD = "upload";
 	private static final String PAGE_GALLERY = "gallery";
 	private static final String PAGE_VIEWER = "viewer";
 	private static final String PAGE_ALL = "all";
 	
 	
 	private static final String DESKTOP = "d";
 	private static final String MOBILE = "m";
 	private static final String TABLET = "t";
 	
 	private static final String ACTION_VOTE = "v";
 	
 	private static final String UPLOADDIR = "/resources/uploads/";
 
 	@Autowired
 	public ContestController(ServletContext servletContext){
 		this.servletContext = servletContext;		
 	}
 
 	@ModelAttribute("uploadModel")
 	public MediaMessage putAttributeUploadModel(){
 		MediaMessage uploadModel = new MediaMessage();
 		uploadModel.setId(newId());
 		log.debug("returning new uploadModel="+uploadModel);
 		return uploadModel;
 	}
 	
 	@ModelAttribute("msg")
 	public String putAttributeMsg(){
 		return "";
 	}
 	
 	@ModelAttribute
 	public void putAttributeAjax(WebRequest request, Model model) {
 		model.addAttribute("ajaxRequest", isAjaxRequest(request));
 	}
 	
 	@ModelAttribute
 	public void putAttributeDesktop(WebRequest request, Model model){
 		model.addAttribute("desktop", Utils.isDesktop(request.getHeader("User-Agent")));
 	}
 	
 	@ModelAttribute
 	public void putAttributeSX(WebRequest request, Model model){
 		boolean sx = Utils.isSX(request.getHeader("User-Agent"));
 		log.info("sx="+sx);
 		if( !model.containsAttribute("sx") || sx){
 			model.addAttribute("sx", sx);
 		}
 	}
 	
 	@ModelAttribute
 	public void putAttributeView(WebRequest request, Model model) {
 		String view = DESKTOP;
 		if( Utils.isMobileBrowser(request.getHeader("User-Agent"))){
 			view = MOBILE;
 		}
 		else if( Utils.isTabletBrowser(request.getHeader("User-Agent"))){
 			view = TABLET;
 		}
 		model.addAttribute("view", view);
 	}
 
 	@RequestMapping(value="/icemobile", method = RequestMethod.POST)
 	public String postICEmobileSX(HttpServletRequest request,
             Model model)  {
 		log.info("setting ICEmobile-SX");
             String userAgent = request.getHeader(TagUtil.USER_AGENT);
             if (userAgent.contains("ICEmobile-SX"))  {
                 model.addAttribute(TagUtil.USER_AGENT_COOKIE,
                         "HyperBrowser-ICEmobile-SX/1.0");
             }
             return "contest";
         }
 
 	@RequestMapping(value="/contest", method = RequestMethod.GET)
 	public String get(
 			HttpServletResponse response,
 			@RequestParam(value="l", defaultValue=MOBILE) String layout, 
 			@RequestParam(value="p", defaultValue=PAGE_UPLOAD) String page,
 			@RequestParam(value="id", required=false) String id,
 			@RequestParam(value="a", required=false) String action,
 			@CookieValue(value="votes", required=false) String cookieVotes,
 			Model model, 
 			@ModelAttribute("uploadModel") MediaMessage uploadModel,
 			@ModelAttribute("msg") String msg) {
 		
 		log.warn("main contest controller l="+layout+", p="+page+", a="+action +", cookies="+cookieVotes);
 		
 		//clean params
 		layout = cleanSingleRequestParam(layout);
 		
 		String view = null;
 		if( layout.equals(TABLET) ){
 			page = PAGE_ALL;
 		}
 		String voterId = getVoterIdFromCookie(cookieVotes);
 		if( cookieVotes == null || cookieVotes.length() == 0){
 			voterId = newId();
 			List<String> votes = Collections.emptyList();
 			addVotesCookie(response, voterId, votes);
 		}
 		
 		addCommonModel(model,uploadModel,layout);
 		model.addAttribute("voterId",voterId);
 		
 		if (ACTION_VOTE.equals(action) ){
 			doVote(response,id,cookieVotes, model);
 		}
 		
 		if( page.equals(PAGE_UPLOAD) ){
 			addUploadViewModel(page, layout, model);
 			view = "contest-upload";
 		}
 		else if( page.equals(PAGE_GALLERY) ){
 			view = "contest-gallery";
 		}
 		else if( page.equals(PAGE_VIEWER)){
 			addViewerViewModel(id,layout,model);
 			view = "contest-viewer";
 		}
 		else if( page.equals(PAGE_ALL)){
 			addUploadViewModel(page, layout, model);
 			addViewerViewModel(id, layout, model);
 			view = "contest-tablet";
 		}
 		log.warn("forwarding to " + view);
 		
 		return view;
 	}
 	
 	@RequestMapping(value="/contest-carousel", method = RequestMethod.GET)
 	public String getCarouselContent(@RequestParam(value="l", defaultValue=MOBILE) String layout, 
 			Model model) {
 		layout = cleanSingleRequestParam(layout);
 		model.addAttribute("carouselItems", mediaService
 				.getContestMediaImageMarkup(layout));
 		return "contest-carousel";
 	}
 	
 	@RequestMapping(value="/contest-viewer", method = RequestMethod.GET)
 	public String getContestViewerContent(
 			@RequestParam(value="l", defaultValue=MOBILE) String layout, 
 			@RequestParam(value="id", required=false) String id,
 			Model model, 
 			@ModelAttribute("uploadModel") MediaMessage uploadModel) {
 		addCommonModel(model, uploadModel, layout);
 		addViewerViewModel(id, layout, model);
 		return "contest-viewer-panel";
 	}
 	
 	@RequestMapping(value="/contest-photo-list", method=RequestMethod.GET)
 	public String getPhotoListContent(
 			@RequestParam(value="l", defaultValue=MOBILE) String layout, 
 			Model model, 
 			@ModelAttribute("uploadModel") MediaMessage uploadModel,
 			@CookieValue(value="votes", required=false) String cookieVotes){
 		addCommonModel(model, uploadModel, layout);
 		model.addAttribute("voterId",getVoterIdFromCookie(cookieVotes));
 		return "contest-photo-list";
 	}
 	
 	@RequestMapping(value="/contest-vote-tally", method=RequestMethod.GET)
 	public String getVoteTallyContent(
 			@RequestParam(value="id") String id, 
 			Model model){
 		id = cleanSingleRequestParam(id);
 		MediaMessage msg = mediaService.getMediaMessage(id);
 		if( msg == null ){
 			log.warn("could not find msg = "+ id);
 		}
 		
 		model.addAttribute("media", msg);
 		return "contest-vote-tally";
 	}
 	
 	@RequestMapping(value="/contest-photo-list-json", method=RequestMethod.GET, produces="application/json")
 	public @ResponseBody List<MediaMessageTransfer> getPhotoListJSON(
 			@RequestParam(value="since") long since,
 			@CookieValue(value="votes", required=false) String cookieVotes){
 		List<MediaMessage> list = mediaService.getMediaCopy();
 		List<MediaMessageTransfer> results = new ArrayList<MediaMessageTransfer>();
 		if( list != null && list.size() > 0 ){
 			Iterator<MediaMessage> iter = list.iterator();
 			while( iter.hasNext() ){
 				MediaMessage msg = iter.next();
 				String voterId = getVoterIdFromCookie(cookieVotes);
 				boolean canVote = false;
 				if( voterId != null ){
 					canVote = !msg.getVotes().contains(voterId);
 				}
 				if( msg.getLastVote() > since || msg.getCreated() > since || since == 0){
 					results.add(new MediaMessageTransfer(msg, canVote));
 				}
 			}
 		}
 		return results;
 	}
 
 	
 
 	@RequestMapping(value="/contest-admin", method = RequestMethod.POST)
 	public String postAdminPage(HttpServletRequest request, String password, 
 			String[] delete, Model model)
 					throws IOException {
 		log.info("password="+password+", delete="+delete);
 		if( password != null ){
 			boolean canEdit = canEdit(password);
 			log.info("canEdit="+canEdit);
 			if( canEdit ){
 				model.addAttribute("admin",Boolean.valueOf(canEdit));
 				model.addAttribute("mediaService", mediaService);
 			}
 		}
 		else{
 			boolean isAdmin = (Boolean)model.asMap().get("admin");
 			if( delete != null && delete.length > 0 && isAdmin ){
 				for( String id : delete ){
 					mediaService.removeMessage(id);
 				}
 				model.addAttribute("mediaService", mediaService);
 				PushContext.getInstance(servletContext).push("photos");		
 				PushContext.getInstance(servletContext).push("carousel");
 			}
 		}
 		
 		return "contest-admin";
 	}
 	
 	@RequestMapping(value="/contest-admin", method = RequestMethod.GET)
 	public String getAdminPage()
 					throws IOException {
 		return "contest-admin";
 	}
 	
 	
 	@RequestMapping(value="/contest", method = RequestMethod.POST, consumes="multipart/form-data")
 	public String post(
 			HttpServletRequest request,
 			HttpServletResponse response,
 			@RequestParam(value = "uploadId", required=false) String uploadId,
 			@RequestParam(value = "upload", required = false) MultipartFile file,
 			@RequestParam(value="l", defaultValue=MOBILE) String layout,
 			@RequestParam(value="fullPost", defaultValue="true") String fullPost,
 			@Valid ContestForm form, BindingResult result,
 			@ModelAttribute("uploadModel") MediaMessage uploadModel,
 			@ModelAttribute("msg") String msg,
 			@CookieValue(value="votes", required=false) String cookieVotes,
 			Model model)
 					throws IOException {
 		
 		log.info("upload id=" + uploadId + ", fullPost="+fullPost);
 		log.info("user-agent="+request.getHeader("User-Agent"));
 		
 		layout = cleanSingleRequestParam(layout);
 		if( uploadId == null || uploadId.length() == 0){
 			uploadId = newId();
 			uploadModel.setId(uploadId);
 		}
 		
 		log.info(form);
 		
 		if( file != null ){
 			log.info("incoming upload " + file.getContentType() + ", " + file.getContentType() + ", " + file.getSize() );
 		}
 		
 		//SX Image upload before full form post
 		if( file != null && !"true".equals(fullPost)){
 			log.info("SX upload");
 			File photo = saveMultipartUploadToFile(file,uploadId);
 			Media media = new Media();
 			media.setFile(photo);
 			uploadModel.setPhoto(media);
 			mediaHelper.processImage(uploadModel,uploadId);
 			return postUploadFormResponseView(false, false, layout);
 		}
 		
 		boolean success = false;
 		
 		if( "gallery".equals(form.getForm())){
 			doVote(response, form.getPhotoId(), cookieVotes, model);
 			addCommonModel(model, uploadModel, form.getLayout());
 			model.addAttribute("voterId",getVoterIdFromCookie(cookieVotes));
 			return "contest-photo-list";
 		}
 
 		if (result.hasErrors() ) {
 			log.info("form has errors " + result);
 			if( form.getEmail() != null && form.getEmail().length() > 0 ){
 				//uploadModel.setUploadMsg("Is that your correct email?");
 				model.addAttribute("msg","Is that your correct email?");
 				log.warn("invalid email");
 			}
 			else{
 				//uploadModel.setUploadMsg("Sorry, I think you missed something.");
 				model.addAttribute("msg","Sorry, I think you missed something.");
 				log.warn("missing required form values");
 			}
 			
 		}
 		else{
 			log.info("form has no errors");
 			File originalPhoto = getOriginalFile(uploadId);
 			if( !originalPhoto.exists() && (file == null || file.isEmpty()) ){
 				//uploadModel.setUploadMsg("Sorry, I think you missed the photo.");
 				model.addAttribute("msg","Sorry, I think you missed the photo.");
 			}
 			else{
 				if( !originalPhoto.exists() && file != null && !file.isEmpty() ){
 					saveMultipartUploadToFile(file, uploadId);
 					Media media = new Media();
 					media.setFile(originalPhoto);
 					uploadModel.setPhoto(media);
 					mediaHelper.processImage(uploadModel,uploadId);
 				}
 				
 				
 				uploadModel.setDescription(form.getDescription());
 				uploadModel.setEmail(form.getEmail());
 				uploadModel.setCreated(System.currentTimeMillis());
 				mediaService.addMedia(uploadModel);
 				log.debug("successfully added message to mediaService, uploadModel="
 						+ uploadModel);
 				//uploadModel.setUploadMsg("Thank you, your file was uploaded successfully.");
 				model.addAttribute("msg","Thank you, your file was uploaded successfully.");
				uploadModel.clear();
 				uploadModel.setId(newId());
 				PushContext.getInstance(servletContext).push("photos");		
 				PushContext.getInstance(servletContext).push("carousel");		
 				
 				success = true;
 			}
 		}
 		addCommonModel(model, uploadModel, form.getLayout());
 		model.addAttribute("voterId",getVoterIdFromCookie(cookieVotes));
 		return postUploadFormResponseView(isAjaxRequest(request),success,form.getLayout());
 	}
 	
 	private String postUploadFormResponseView(boolean ajax, boolean redirect, String layout){
 		String view = null;
 		if( ajax ){
 			redirect = false;
 		}
 		if( redirect ){
 			view = "redirect:/" + "contest?l=" + layout
 					+(MOBILE.equals(layout)?"p=upload":"");
 		}
 		else if( MOBILE.equals(layout)){
 			view = "contest-upload";
 		}
 		else {
 			view = "contest-upload-form";
 		}
 		return view;
 	}
 
 	private void addCommonModel(Model model, MediaMessage uploadModel, String layout){
 		model.addAttribute("mediaService", mediaService);
 		//if( !model.containsAttribute("uploadModel")){
 			model.addAttribute("uploadModel", uploadModel);
 		//}
 		if( layout != null && layout.length() > 0){
 			model.addAttribute("layout",layout.substring(0,1));
 		}
 		model.addAttribute("updated",System.currentTimeMillis());
 	}
 	
 	private void addUploadViewModel(String page, String layout, Model model){
 		if( PAGE_UPLOAD.equals(page) ){
 			page = PAGE_VIEWER;
 		}
 		model.addAttribute("carouselItems", mediaService
 				.getContestMediaImageMarkup(layout));
 	}
 	
 	private void addViewerViewModel(String id, String layout, Model model){
 		MediaMessage msg = mediaService.getMediaMessage(id);
 		model.addAttribute("media", msg);
 		if( TABLET.equals(layout)){
 			model.addAttribute("tab", PAGE_VIEWER);
 		}
 	}
 	
 	private String getVoterIdFromCookie(String cookie){
 		String voterId = null;
 		if( cookie != null && cookie.length() > 13){
 			voterId = cookie.substring(0,13);
 		}
 		else{
 			return newId();
 		}
 		return voterId;
 	}
 	
 	private List<String> getVotesFromCookie(String cookie){
 		List<String> votes = new ArrayList<String>();
 		if( cookie != null && cookie.length() > 14 ){
 			String votesString = cookie.substring(14);
 			votes.addAll(Arrays.asList(votesString.split(",")));
 		}
 		return votes;
 	}
 	
 	private void doVote(HttpServletResponse response, String id, String cookieVotes, Model model){
 		MediaMessage msg = mediaService.getMediaMessage(id);
 		if (msg != null ){
 			String voterId = getVoterIdFromCookie(cookieVotes);
 			List<String> votesList = getVotesFromCookie(cookieVotes);
 			if( votesList.contains(id)){
 				log.debug("attempted duplicate vote!!!");
 				model.addAttribute("msg","Looks like you already voted on this one...try another");
 				return;
 			}
 			votesList.add(id);
 			msg.getVotes().add(voterId);
 			msg.setLastVote(System.currentTimeMillis());
 			model.addAttribute("msg","Awesome, thanks for the vote!");
 			addVotesCookie(response, voterId, votesList);
 			PushContext.getInstance(servletContext).push("photos");
 			PushContext.getInstance(servletContext).push("votes-"+msg.getId());
 			log.debug("recorded vote");
 		} 
 	}
 	
 	private void addVotesCookie(HttpServletResponse response, String voterId, List<String> votes){
 		String votesString = "";
 		if( !votes.isEmpty() ){
 			Iterator<String> iter = votes.iterator();
 			while( iter.hasNext() ){
 				votesString += iter.next();
 				if( iter.hasNext() ){
 					votesString += ",";
 				}
 			}
 		}
 		String cookieVal = voterId+":"+ votesString;
 		Cookie cookie = new Cookie("votes", cookieVal);
 		cookie.setHttpOnly(true);
 		cookie.setPath("/");
 		response.addCookie(cookie);
 	}
 
 	private String newId() {
 		return Long.toString(
 				Math.abs(UUID.randomUUID().getMostSignificantBits()), 32);
 	}
 
 	private File getOriginalFile(String id){
 		String fileName = "img-" + id + "-orig.jpg";
 		String path = UPLOADDIR + fileName;
 		return new File(servletContext.getRealPath(path));
 	}
 	
 	private void ensureUploadDirExists(){
 		File uploadDir = new File(servletContext.getRealPath(UPLOADDIR));
 		if( uploadDir.exists()){
 			uploadDir.mkdir();
 		}
 	}
 	
 	private File saveMultipartUploadToFile(MultipartFile upload, String id){
 		File file = null;
 		if( upload != null && !upload.isEmpty()){
 			file = getOriginalFile(id);
 			ensureUploadDirExists();
 			log.info("writing new image file " + file.getAbsolutePath());
 			try {
 				upload.transferTo(file);
 			} catch( Exception e) {
 				log.warn("could not write file ", e);
 				e.printStackTrace();
 			} 
 		}
 		return file;
 	}
 
 	private static boolean isAjaxRequest(WebRequest webRequest) {
 		String requestedWith = webRequest.getHeader("Faces-Request");
 		if ("partial/ajax".equals(requestedWith))  {
 			return true;
 		}
 
 		requestedWith = webRequest.getHeader("X-Requested-With");
 		return requestedWith != null ? "XMLHttpRequest".equals(requestedWith) : false;
 	}
 	
 	private static boolean isAjaxRequest(HttpServletRequest webRequest) {
 		String requestedWith = webRequest.getHeader("Faces-Request");
 		if ("partial/ajax".equals(requestedWith))  {
 			return true;
 		}
 
 		requestedWith = webRequest.getHeader("X-Requested-With");
 		return requestedWith != null ? "XMLHttpRequest".equals(requestedWith) : false;
 	}
 
 	private static boolean isAjaxUploadRequest(WebRequest webRequest) {
 		return webRequest.getParameter("ajaxUpload") != null;
 	}
 
 	public void setServletContext(ServletContext sc) {
 		servletContext = sc; 
 	}
 	
 	private boolean canEdit(String key){
 		boolean result = false;
 		log.warn("key="+key);
 		if( key != null && key.length() > EDIT_KEY_PREFIX.length() 
 				&& key.startsWith(EDIT_KEY_PREFIX)){
 			String token = key.substring(EDIT_KEY_PREFIX.length());
 			try{
 				int val = Integer.parseInt(token);
 				int min = currentMinute();
 				log.warn("val="+val+", min="+min);
 				if( val == min ){
 					result = true;
 				}
 			}
 			catch(NumberFormatException nfe){
 				//do nothing
 			}
 		}
 		log.warn("edit="+result);
 		return result;
 	}
 	
 	private int currentMinute(){
 		return new GregorianCalendar().get(Calendar.MINUTE);
 	}
 	
 	private String cleanSingleRequestParam(String param){
 		if( param != null && param.indexOf(",") > 0 ){
 			param = param.substring(0,param.indexOf(","));
 		}
 		return param;
 	}
 
 }
