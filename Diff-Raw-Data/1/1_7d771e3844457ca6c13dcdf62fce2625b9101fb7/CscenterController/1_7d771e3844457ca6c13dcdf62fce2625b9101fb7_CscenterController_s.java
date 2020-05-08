 package com.bgg.storyfarm.controller;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.net.URLEncoder;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import org.json.simple.JSONObject;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.bgg.storyfarm.common.BreadcrumbUtil;
 import com.bgg.storyfarm.common.PageUtil;
 import com.bgg.storyfarm.common.StoryfarmConstants;
 import com.bgg.storyfarm.service.BoardService;
 
 
 @Controller
 @RequestMapping(value = "cscenter")
 public class CscenterController {
 	
 	private Logger logger = LoggerFactory.getLogger(CscenterController.class);
 	
 	//고객센터 사이드바 활성화 좌표
 	private final String LM_SEQ = "lmSeq";
 	private final int LNB_NOTI = 0; //공지사항
 	private final int LNB_EVENT = 1; //이벤트
 	private final int LNB_FAQ = 2; //자주하는 질문
 	private final int LNB_QUESTION = 3; //문의하기
 	
 	
 	//게시판 아이디
 	private final int NOTI_BOARD_ID = 1; //공지사항
 	private final int EVENT_BOARD_ID = 2; //이벤트
 	private final int WINNER_BOARD_ID = 4; //당첨자발표
 	private final int FAQ_USER_BOARD_ID = 26; //자주하는질문 회원/가입안내
 	private final int FAQ_PAYMENT_BOARD_ID = 27; //자주하는질문 결제안내
 	private final int FAQ_SERVICE_BOARD_ID = 28; //자주하는질문 서비스안내
 	private final int FAQ_ERROR_BOARD_ID = 29; //자주하는질문 이용장애안내
 	
 	
 	@Autowired
 	private BreadcrumbUtil breadcrumbUtil;
 
 	@Autowired
 	private BoardService boardService;
 	
 	@Autowired
 	private PageUtil pageUtil;
 	
 	@RequestMapping(value = "notice.do", method = RequestMethod.GET)
 	public ModelAndView notice(@RequestParam Map<String, Object> paramsMap) throws UnsupportedEncodingException {
 		ModelAndView mav = new ModelAndView();
 		mav.setViewName("side-cscenter/notice");
 		mav.addObject(LM_SEQ, LNB_NOTI);
 		mav.addObject(StoryfarmConstants.BREADCRUMBS, breadcrumbUtil.getBreadcrumbs(
 				StoryfarmConstants.BREADCRUMB_HOME, 
 				StoryfarmConstants.BREADCRUMB_CSCENTER, 
 				StoryfarmConstants.BREADCRUMB_CSCENTER_NOTI));
 		
 		Map<String, Object> boardMap = new HashMap<String, Object>();
 		boardMap.put(StoryfarmConstants.BOARD_ID, NOTI_BOARD_ID);
 		boardMap.put("search", paramsMap.get("search"));
 		
 		try {
 			logger.info("search : {}", paramsMap.get("search"));
 			byte[] b = paramsMap.get("search").toString().getBytes("iso-8859-1");
 			
 			logger.info("search euc : {}", new String(b, "euc-kr"));
 			logger.info("search utf : {}", new String(b, "UTF-8"));
 			
 			String isoname = URLEncoder.encode(paramsMap.get("search").toString(),"iso-8859-1");
 			logger.info("isoname  : {}", isoname);
 			logger.info("isoname  : {}", URLDecoder.decode(isoname));
 			
 		} catch (NullPointerException e) {
 			//do nothing
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		
 		//페이징 로직
 		int totalCnt = boardService.totalCount(boardMap);
 		int pageNum = setPage(paramsMap, boardMap);
 		mav.addObject("pageLink", pageUtil.getPageLinkMap(totalCnt, pageNum));
 		//페이징 로직
 		mav.addObject("paramsMap", paramsMap);
 		
 		mav.addObject("list", boardService.list(boardMap));
 		
 		
 		return mav;
 	}
 
 	@RequestMapping(value = "noticeView.do", method = RequestMethod.GET)
 	public ModelAndView noticeView(@RequestParam Map<String, Object> paramsMap) {
 		ModelAndView mav = new ModelAndView();
 		mav.setViewName("side-cscenter/noticeView");
 		mav.addObject(LM_SEQ, LNB_NOTI);
 		mav.addObject(StoryfarmConstants.BREADCRUMBS, breadcrumbUtil.getBreadcrumbs(
 				StoryfarmConstants.BREADCRUMB_HOME, 
 				StoryfarmConstants.BREADCRUMB_CSCENTER, 
 				StoryfarmConstants.BREADCRUMB_CSCENTER_NOTI));
 		
 		String contentId = paramsMap.get("contentsId").toString();
 		boardService.hits(Integer.valueOf(contentId));
 		Map<String, Object> writing = boardService.detail(Integer.valueOf(contentId));
 		mav.addObject("writing", writing);
 		
 		return mav;
 	}
 	
 	@RequestMapping(value = "faq.do", method = RequestMethod.GET)
 	public ModelAndView faq(Model model) {
 		ModelAndView mav = new ModelAndView();
 		mav.setViewName("side-cscenter/faq");
 		mav.addObject(LM_SEQ, LNB_FAQ);
 		mav.addObject(StoryfarmConstants.BREADCRUMBS, breadcrumbUtil.getBreadcrumbs(
 				StoryfarmConstants.BREADCRUMB_HOME, 
 				StoryfarmConstants.BREADCRUMB_CSCENTER, 
 				StoryfarmConstants.BREADCRUMB_CSCENTER_FAQ));
 		return mav;
 	}
 	
 	@RequestMapping(value = "event.do", method = RequestMethod.GET)
 	public ModelAndView event(@RequestParam Map<String, Object> paramsMap) {
 		ModelAndView mav = new ModelAndView();
 		mav.setViewName("side-cscenter/event");
 		mav.addObject(LM_SEQ, LNB_EVENT);
 		mav.addObject(StoryfarmConstants.BREADCRUMBS, breadcrumbUtil.getBreadcrumbs(
 				StoryfarmConstants.BREADCRUMB_HOME, 
 				StoryfarmConstants.BREADCRUMB_CSCENTER, 
 				StoryfarmConstants.BREADCRUMB_CSCENTER_EVENT));
 		
 		Map<String, Object> boardMap = new HashMap<String, Object>();
 		boardMap.put(StoryfarmConstants.BOARD_ID, EVENT_BOARD_ID);
 		
 		//페이징 로직
 		int totalCnt = boardService.totalCount(boardMap);
 		int pageNum = setPage(paramsMap, boardMap);
 		mav.addObject("pageLink", pageUtil.getPageLinkMap(totalCnt, pageNum));
 		//페이징 로직
 		
 		mav.addObject("list", boardService.list(boardMap));
 		
 		return mav;
 	}
 	
 	@RequestMapping(value = "eventView.do", method = RequestMethod.GET)
 	public ModelAndView eventView(@RequestParam Map<String, Object> paramsMap) {
 		ModelAndView mav = new ModelAndView();
 		mav.setViewName("side-cscenter/eventView");
 		mav.addObject(LM_SEQ, LNB_EVENT);
 		mav.addObject(StoryfarmConstants.BREADCRUMBS, breadcrumbUtil.getBreadcrumbs(
 				StoryfarmConstants.BREADCRUMB_HOME, 
 				StoryfarmConstants.BREADCRUMB_CSCENTER, 
 				StoryfarmConstants.BREADCRUMB_CSCENTER_EVENT));
 		
 		String contentId = paramsMap.get("contentsId").toString();
 		Map<String, Object> writing = boardService.detail(Integer.valueOf(contentId));
 		mav.addObject("writing", writing);
 
 		//댓글 목록 view
 		mav.addObject("detailComments", boardService.detailComments(paramsMap));
 		mav.addObject("contentsId", contentId);
 		
 		return mav;
 	}
 	
 	@RequestMapping(value = "commentCreate.do", method = RequestMethod.POST)
 	public String commentCreate(@RequestParam Map<String, Object> paramsMap, HttpSession session) {
 		ModelAndView mav = new ModelAndView();
 
 		String fLocation = paramsMap.get("fLocation").toString();
 		String sLocation = paramsMap.get("sLocation").toString();
 		
 		if(session.getAttribute("userInfoSession") == null){
 			mav.addObject("msg", "login_fail");
 			return "redirect:/" + fLocation + "/" + sLocation + ".do?contentsId="+paramsMap.get("contents_id");
 		}
 		
 		mav.addObject("commentCreate", boardService.commentCreate(paramsMap));
 		return "redirect:/" + fLocation + "/" + sLocation + ".do?contentsId="+paramsMap.get("contents_id");
 	}
 	
 	@RequestMapping(value = "commentDelete.do", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
 	public @ResponseBody String commentDelete(@RequestParam Map<String, Object> paramsMap) {
 		
 		boardService.commentDelete(paramsMap);
 		
 		JSONObject jsonObj=new JSONObject();
 		jsonObj.put("code", "200");
 		
 		return jsonObj.toJSONString();
 	}
 
 	@RequestMapping(value = "commentModify.do", method = RequestMethod.POST)
 	public String commentModify(@RequestParam Map<String, Object> paramsMap) {
 		ModelAndView mav = new ModelAndView();
 		
 		String fLocation = paramsMap.get("fLocation").toString();
 		String sLocation = paramsMap.get("sLocation").toString();
 
 		mav.addObject("commentModify", boardService.commentModify(paramsMap));
 		
 		return "redirect:/" + fLocation + "/" + sLocation + ".do?contentsId="+paramsMap.get("contents_id") + "&comment_id=" + paramsMap.get("comment_id");
 	}
 	
 	@RequestMapping(value = "ask.do", method = RequestMethod.GET)
 	public ModelAndView ask(Model model) {
 		ModelAndView mav = new ModelAndView();
 		mav.setViewName("side-cscenter/ask");
 		mav.addObject(LM_SEQ, LNB_QUESTION);
 		mav.addObject(StoryfarmConstants.BREADCRUMBS, breadcrumbUtil.getBreadcrumbs(
 				StoryfarmConstants.BREADCRUMB_HOME, 
 				StoryfarmConstants.BREADCRUMB_CSCENTER, 
 				StoryfarmConstants.BREADCRUMB_CSCENTER_ASK));
 		return mav;
 	}
 	
 	/** 문의메일 보내기
 	 * @param model
 	 * @return
 	 */
 	@RequestMapping(value = "email.do", method = RequestMethod.GET)
 	public String email(Model model, HttpServletRequest request, HttpSession session) {
 		ModelAndView mav = new ModelAndView();
 		mav.setViewName("side-cscenter/email");
 		mav.addObject(LM_SEQ, LNB_QUESTION);
 		mav.addObject(StoryfarmConstants.BREADCRUMBS, breadcrumbUtil.getBreadcrumbs(
 				StoryfarmConstants.BREADCRUMB_HOME, 
 				StoryfarmConstants.BREADCRUMB_CSCENTER, 
 				StoryfarmConstants.BREADCRUMB_CSCENTER_ASK));
 		
 		if( session.getAttribute("userInfoSession") != null){
 			mav.setViewName("side-mypage/question");
 			return "redirect:/mypage/question.do";
 		}else{
 			mav.setViewName("side-cscenter/email");
 			return "side-cscenter/email";
 		}
 	}
 
 	@RequestMapping(value = "winner.do", method = RequestMethod.GET)
 	public ModelAndView winner(@RequestParam Map<String, Object> paramsMap) {
 		ModelAndView mav = new ModelAndView();
 		mav.setViewName("side-cscenter/winner");
 		mav.addObject(StoryfarmConstants.BREADCRUMBS, breadcrumbUtil.getBreadcrumbs(
 				StoryfarmConstants.BREADCRUMB_HOME, 
 				StoryfarmConstants.BREADCRUMB_CSCENTER, 
 				StoryfarmConstants.BREADCRUMB_CSCENTER_WINNER));
 		
 		Map<String, Object> boardMap = new HashMap<String, Object>();
 		boardMap.put(StoryfarmConstants.BOARD_ID, WINNER_BOARD_ID);
 		boardMap.put("search", paramsMap.get("search"));
 		
 		//페이징 로직
 		int totalCnt = boardService.totalCount(boardMap);
 		int pageNum = setPage(paramsMap, boardMap);
 		mav.addObject("pageLink", pageUtil.getPageLinkMap(totalCnt, pageNum));
 		//페이징 로직
 		mav.addObject("searchList", paramsMap);
 		
 		mav.addObject("list", boardService.list(boardMap));
 		
 		
 		return mav;
 	}
 
 	@RequestMapping(value = "winnerView.do", method = RequestMethod.GET)
 	public ModelAndView winnerView(@RequestParam Map<String, Object> paramsMap) {
 		ModelAndView mav = new ModelAndView();
 		mav.setViewName("side-cscenter/winnerView");
 		mav.addObject(StoryfarmConstants.BREADCRUMBS, breadcrumbUtil.getBreadcrumbs(
 				StoryfarmConstants.BREADCRUMB_HOME, 
 				StoryfarmConstants.BREADCRUMB_CSCENTER, 
 				StoryfarmConstants.BREADCRUMB_CSCENTER_NOTI));
 		
 		String contentId = paramsMap.get("contentsId").toString();
 		boardService.hits(Integer.valueOf(contentId));
 		Map<String, Object> writing = boardService.detail(Integer.valueOf(contentId));
 		mav.addObject("writing", writing);
 		
 		return mav;
 	}
 	/** 페이지 처리
 	 * @param paramsMap
 	 * @param boardMap
 	 * @return
 	 */
 	private int setPage(Map<String, Object> paramsMap, Map<String, Object> boardMap) {
 		int pageNum = 1; //시작글 번호
 		Object obj = paramsMap.get("pageNum");
 		if(obj != null){
 			try {
 				pageNum = Integer.valueOf(obj.toString());
 			} catch (NumberFormatException nfe) {
 				nfe.printStackTrace();
 			}
 		}
 		boardMap.put(PageUtil.ROWNUM_KEY, (pageNum - 1) * PageUtil.PER_PAGE);
 		boardMap.put(PageUtil.PER_KEY, PageUtil.PER_PAGE);
 		return pageNum;
 	}
 	
 }
