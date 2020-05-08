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
 
 import javax.annotation.PostConstruct;
 import javax.inject.Inject;
 import javax.servlet.ServletContext;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.validation.Valid;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.icemobile.jsp.tags.TagUtil;
 import org.icepush.PushContext;
 import org.icepush.PushNotification;
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
 @SessionAttributes(value = {"admin","desktop", "voterId", "sx", "sxThumbnail", "email", "enhanced",
 		TagUtil.USER_AGENT_COOKIE, TagUtil.CLOUD_PUSH_KEY},
 		types=ContestForm.class)
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
 
 	private String currentLeaderEmail;
 	private int currentLeaderVotes = 0;
 
 	private static final String SX_UPLOAD_KEY = "sxUpload";
 
 	@PostConstruct
 	public void init(){
 		ensureUploadDirExists();
 	}
 
 	@Autowired
 	public ContestController(ServletContext servletContext){
 		this.servletContext = servletContext;		
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
 		if( !model.containsAttribute("sx") || sx){
 			model.addAttribute("sx", sx);
 		}
 	}
 	
 	@ModelAttribute
 	public void putAttributeEnhanced(HttpServletRequest request, Model model){
 		boolean enhanced = Utils.isEnhanced(request);
 		if( !model.containsAttribute("enhanced") || enhanced){
 			model.addAttribute("enhanced", enhanced);
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
 			@RequestParam(value=TagUtil.CLOUD_PUSH_KEY) String cloudKey, 
 			Model model)  {
 		log.info("setting ICEmobile-SX");
 		String userAgent = request.getHeader(TagUtil.USER_AGENT);
 		if (userAgent.contains("ICEmobile-SX"))  {
 			model.addAttribute("sx", true);
 			model.addAttribute(TagUtil.USER_AGENT_COOKIE,
 					"HyperBrowser-ICEmobile-SX/1.0");
 			if (null != cloudKey)  {
 				model.addAttribute(TagUtil.CLOUD_PUSH_KEY, cloudKey);
 			}
 		}
 		return "contest";
 	}
 
 	@RequestMapping(value="/contest", method = RequestMethod.GET)
 	public String get(
 			HttpServletResponse response,
 			ContestForm form,
 			@CookieValue(value="votes", required=false) String cookieVotes,
 			Model model, 
 			@ModelAttribute("msg") String msg) {
 
 		log.debug("form="+form+" ,cookies="+cookieVotes);
 
 		String view = null;
 		if( TABLET.equals(form.getL()) ){
 			form.setP(PAGE_ALL);
 		}
 		String voterId = getVoterIdFromCookie(cookieVotes);
 		if( cookieVotes == null || cookieVotes.length() == 0){
 			voterId = newId();
 			List<String> votes = Collections.emptyList();
 			addVotesCookie(response, voterId, votes);
 		}
 
 		addCommonModel(model,form.getL());
 		model.addAttribute("voterId",voterId);
 
 		if( PAGE_UPLOAD.equals(form.getP()) ){
 			addUploadViewModel(PAGE_UPLOAD, form.getL(), model);
 			view = "contest-upload";
 		}
 		else if( PAGE_GALLERY.equals(form.getP()) ){
 			view = "contest-gallery";
 		}
 		else if( PAGE_VIEWER.equals(form.getP())){
 			addViewerViewModel(form.getPhotoId(),form.getL(), model, false, false);
 			view = "contest-viewer";
 		}
 		else if( PAGE_ALL.equals(form.getP())){
 			addUploadViewModel(PAGE_ALL, form.getL(), model);
 			addViewerViewModel(form.getPhotoId(), form.getL(), model, false, false);
 			view = "contest-tablet";
 		}
 		log.debug("forwarding to " + view);
 
 		return view;
 	}
 
 	@RequestMapping(value="/contest-carousel", method = RequestMethod.GET)
 	public String getCarouselContent(ContestForm form, Model model) {
 		model.addAttribute("carouselItems", mediaService
 				.getContestMediaImageMarkup(form.getL()));
 		model.addAttribute("layout",form.getL());
 		return "contest-carousel";
 	}
 
 	@RequestMapping(value="/contest-viewer", method = RequestMethod.GET)
 	public String getContestViewerContent(@RequestParam(value="action", required=false) String action, 
 			ContestForm form, Model model) {
 		log.info(form);
 		String actionParam = form.cleanParam(action);
 		boolean back = "back".equals(actionParam);
 		boolean forward = "forward".equals(actionParam);
 		addCommonModel(model, form.getL());
 		addViewerViewModel(form.getPhotoId(), form.getL(), model, back, forward);
 		return "contest-viewer-panel";
 	}
 
 	@RequestMapping(value="/contest-photo-list", method=RequestMethod.GET)
 	public String getPhotoListContent(ContestForm form, Model model,
 			@CookieValue(value="votes", required=false) String cookieVotes){
 		addCommonModel(model, form.getL());
 		model.addAttribute("voterId",getVoterIdFromCookie(cookieVotes));
 		return "contest-photo-list";
 	}
 
 	@RequestMapping(value="/contest-vote-tally", method=RequestMethod.GET)
 	public String getVoteTallyContent( ContestForm form, Model model){
 		MediaMessage msg = mediaService.getMediaMessage(form.getPhotoId());
 		if( msg == null ){
 			log.warn("could not find msg = "+ form.getPhotoId());
 		}
 
 		model.addAttribute("media", msg);
 		return "contest-vote-tally";
 	}
 
 	@RequestMapping(value="/contest-photo-list-json", method=RequestMethod.GET, produces="application/json")
 	public @ResponseBody List<MediaMessageTransfer> getPhotoListJSON(
 			@RequestParam(value="since") long since,
 			@RequestParam(value="_", required=false) String jqTimestamp,
 			@CookieValue(value="votes", required=false) String cookieVotes){
 
 		List<MediaMessage> list = mediaService.getMediaCopy();
 		List<MediaMessageTransfer> results = new ArrayList<MediaMessageTransfer>();
 		String voterId = getVoterIdFromCookie(cookieVotes);
 		log.debug("voterId="+voterId);
 		log.debug("cookie="+cookieVotes);
 
 		if( list != null && list.size() > 0 ){
 			Iterator<MediaMessage> iter = list.iterator();
 			while( iter.hasNext() ){
 				MediaMessage msg = iter.next();
 				if( msg.getLastVote() > since || msg.getCreated() > since || since == 0){
 					log.debug(msg);
 					boolean canVote = true;
 					if( voterId != null ){
 						canVote = msg.getVotes().isEmpty() || !msg.getVotes().contains(voterId);
 					}
 					log.debug("votes="+msg.getVotesAsString());
 					MediaMessageTransfer transfer = new MediaMessageTransfer(msg, canVote);
 					log.debug(transfer);
 					results.add(transfer);
 
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
 			}
 		}
 
 		return "contest-admin";
 	}
 
 	@RequestMapping(value="/contest-admin", method = RequestMethod.GET)
	public String getAdminPage() throws IOException {
 		return "contest-admin";
 	}
 
 	private File getSXThumbnailFile(String sessionId){
 		return new File(servletContext.getRealPath("/resources/uploads")
 				+File.separator+"sx-"+sessionId+"-small.png");
 	}
 
 
 	@RequestMapping(value="/contest", method = RequestMethod.POST, consumes="multipart/form-data")
 	public String postUpload(
 			HttpServletRequest request,
 			HttpServletResponse response,
 			@RequestParam(value = "upload", required = false) MultipartFile multiPart,
 			@RequestParam(value="fullPost", defaultValue="true") String fullPost,
 			@RequestParam(value="operation", required=false) String operation,
 			@Valid ContestForm form, BindingResult result,
 			@ModelAttribute("msg") String msg,
 			@ModelAttribute("desktop") boolean desktop,
 			@ModelAttribute("sx") boolean sx,
 			@CookieValue(value="votes", required=false) String cookieVotes,
 			Model model)
 					throws IOException {
 
 		log.info("user-agent="+request.getHeader("User-Agent"));
 		log.info(form);
 		
 		if( multiPart != null ){
 			log.info("incoming upload " + multiPart.getContentType() + multiPart.getSize() );
 		}
 		
 		if( operation != null && "cancel".equals(operation)){
 			request.getSession().removeAttribute(SX_UPLOAD_KEY);
 			return postUploadFormResponseView(isAjaxRequest(request),false,form.getL());
 		}
 
 		//SX Image upload before full form post
 		if( multiPart != null && !"true".equals(fullPost) && sx){
 			log.info("SX upload");
 			String sessionId = request.getSession().getId();
 			File sxMasterFile = getOriginalFile(sessionId);
 			multiPart.transferTo(sxMasterFile);
 			request.getSession().setAttribute(SX_UPLOAD_KEY, sxMasterFile);
 			File sxThumbnail = mediaHelper.processSmallImage(sxMasterFile, "sx-"+sessionId);
 			model.addAttribute("sxThumbnail",sxThumbnail);
 			model.addAttribute("email",form.getEmail());
 			return postUploadFormResponseView(false, false, form.getL());
 		}
 
 		boolean success = false;
 
 		if (result.hasErrors() ) {
 			log.info("form has errors " + result);
 			if( form.getEmail() != null && form.getEmail().length() > 0 ){
 				model.addAttribute("msg","Is that your correct email?");
 				log.warn("invalid email");
 			}
 			else{
 				model.addAttribute("msg","Sorry, I think you missed something.");
 				log.warn("missing required form values");
 			}
 
 		}
 		else{
 			log.info("form has no errors");
 			File sxUpload = (File)request.getSession().getAttribute(SX_UPLOAD_KEY);
 			if( sxUpload == null && (multiPart == null || multiPart.isEmpty()) ){
 				model.addAttribute("msg","Sorry, I think you missed the photo.");
 			}
 			if( multiPart != null || sxUpload != null ){
 				String id = newId();
 				File origFile = getOriginalFile(id);
 				if( sxUpload != null ){
 					sxUpload.renameTo(origFile);
 					File sxThumbnail = getSXThumbnailFile(request.getSession().getId());
 					if( sxThumbnail.exists() ){
 						sxThumbnail.delete();
 					}
 					request.getSession().removeAttribute(SX_UPLOAD_KEY);
 				}
 				else{
 					saveMultipartUploadToFile(multiPart,id);
 				}
 				File[] processedFiles = mediaHelper.processSmallAndLargeImages(origFile, id);
 				if( processedFiles != null ){
 					MediaMessage media = new MediaMessage();
 					media.setId(id);
 					media.setDescription(form.getDescription());
 					media.setEmail(form.getEmail());
 					media.setCreated(System.currentTimeMillis());
 					media.setSmallPhoto(processedFiles[0]);
 					media.setLargePhoto(processedFiles[1]);
 					mediaService.addMedia(media);
 					log.info("created new media: "+media);
 
 					model.addAttribute("msg","Thank you, your file was uploaded successfully.");
 					model.addAttribute("email", form.getEmail());
 
 					PushContext pc = PushContext.getInstance(servletContext);
 					String pushId = pc.createPushId(request, response);
 					pc.addGroupMember(form.getEmail(), pushId);
 					pc.push("photos");	
 
 					success = true;
 				}
 
 			}
 		}
 		addCommonModel(model, form.getL());
 		model.addAttribute("voterId",getVoterIdFromCookie(cookieVotes));
 		return postUploadFormResponseView(isAjaxRequest(request),success,form.getL());
 	}
 
 	@RequestMapping(value="/contest-vote", method = RequestMethod.POST, consumes="multipart/form-data")
 	public String postVote(
 			HttpServletRequest request,
 			HttpServletResponse response,
 			ContestForm form, 
 			BindingResult result,
 			@ModelAttribute("msg") String msg,
 			@CookieValue(value="votes", required=false) String cookieVotes,
 			Model model)
 					throws IOException {
 
 		log.info("user-agent="+request.getHeader("User-Agent"));
 		log.info(form);
 
 		doVote(request, response, form.getPhotoId(), cookieVotes, model);
 		model.addAttribute("mediaService", mediaService);
 		model.addAttribute("voterId",getVoterIdFromCookie(cookieVotes));
 		model.addAttribute("layout",form.getL());
 		model.addAttribute("updated",System.currentTimeMillis());
 		return "contest-photo-list";
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
 
 	private void addCommonModel(Model model, String layout){
 		model.addAttribute("mediaService", mediaService);
 		model.addAttribute("layout",layout);
 		model.addAttribute("updated",System.currentTimeMillis());
 	}
 
 	private void addUploadViewModel(String page, String layout, Model model){
 		if( PAGE_UPLOAD.equals(page) ){
 			page = PAGE_VIEWER;
 		}
 		model.addAttribute("carouselItems", mediaService
 				.getContestMediaImageMarkup(layout));
 	}
 
 	private void addViewerViewModel(String photoId, String layout, Model model, boolean back, boolean forward){
 
 		MediaMessage msg = null;
 		if( photoId != null ){
 			if( back || forward ){
 				List<MediaMessage> list = mediaService.getMediaSortedByVotes();
 				if( list != null ){
 					for(int i = 0 ; i < list.size() ; i++ ){
 						String pid = list.get(i).getId();
 						if( pid != null && pid.equals(photoId) ){
 							int target = (back ? i-1 : i+1);
 							if( target == -1 ){
 								target = list.size()-1;
 							}
 							else if( target == list.size() ){
 								target = 0;
 							}
 							msg = list.get(target);
 						}
 					}
 				}
 			}
 			else{
 				msg = mediaService.getMediaMessage(photoId);
 			}
 
 		}
 		else{
 			List<MediaMessage> list = mediaService.getMediaCopy();
 			if( list.size() > 0 ){
 				msg = list.get(0);
 			}
 		}
 		model.addAttribute("media", msg);
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
 
 	private void doVote(HttpServletRequest request, HttpServletResponse response, String id, String cookieVotes, Model model){
 		log.debug("id="+id+", cookie="+cookieVotes);
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
 			PushContext pc = PushContext.getInstance(servletContext);
 			pc.push("photos");
 			pc.push("votes-"+msg.getId());
 			checkLeader(msg, request);
 			log.debug("recorded vote");
 		} 
 	}
 
 	private void checkLeader(MediaMessage msg, HttpServletRequest request){
 		String url = request.getRequestURL().toString();
 		String imageURL = url.substring(0, url.lastIndexOf('/'))+"/resources/uploads/"+msg.getLargePhoto().getName();
 		log.info("checkLeader url="+imageURL);
 		if( msg.getVotes().size() > currentLeaderVotes ){
 			if( currentLeaderEmail != null && !currentLeaderEmail.equals(msg.getEmail())){
 				PushContext pc = PushContext.getInstance(servletContext);
 				PushNotification pn = new PushNotification("ICEmobile JavaOne Contest Outvoted!",
 					"Our sincerest apologies, your photo has now been outvoted, previously you had " 
 					+ currentLeaderVotes+ " votes. The new leader has "+msg.getVotes().size()+" <a href='"+imageURL+"'>New Leading Photo</a>");
 				pc.push(currentLeaderEmail, pn);
 			}
 			currentLeaderVotes = msg.getVotes().size();
 			currentLeaderEmail = msg.getEmail();
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
 		String fileName = id + "-orig.jpg";
 		String path = getUploadsDir() + File.separator + fileName;
 		return new File(path);
 	}
 
 	private File getUploadsDir(){
 		return new File(servletContext.getRealPath("/resources/uploads"));
 	}
 
 	private void ensureUploadDirExists(){
 		File uploadsDir = getUploadsDir();
 		boolean exists = uploadsDir.exists();
 		if( !exists){
 			exists = uploadsDir.mkdir();
 		}
 		log.info("attempting checking upload dir " + exists + ", "+uploadsDir.getAbsolutePath());
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
         String editKeyPrefix = System.getProperty("org.icemobile.j1.password");
         if ((null == editKeyPrefix) || "".equals(editKeyPrefix))  {
             editKeyPrefix = EDIT_KEY_PREFIX;
         }
 		boolean result = false;
 		if( key != null && key.length() > editKeyPrefix.length() 
 				&& key.startsWith(editKeyPrefix)){
 			String token = key.substring(editKeyPrefix.length());
 			try{
 				int val = Integer.parseInt(token);
 				int min = currentMinute();
 				if( val == min ){
 					result = true;
 				}
 			}
 			catch(NumberFormatException nfe){
 				//do nothing
 			}
 		}
 		return result;
 	}
 
 	private int currentMinute(){
 		return new GregorianCalendar().get(Calendar.MINUTE);
 	}
 
 }
