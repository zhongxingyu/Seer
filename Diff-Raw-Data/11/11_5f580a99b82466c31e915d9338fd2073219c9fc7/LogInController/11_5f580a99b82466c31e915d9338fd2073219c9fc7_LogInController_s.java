 package org.shinyul.control.mobile;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import javax.inject.Inject;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.log4j.Logger;
 import org.shinyul.domain.MemberCommonVO;
import org.shinyul.service.MemberService;
 import org.shinyul.util.SessionUtil;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 @Controller
 @RequestMapping("/logIn")
 public class LogInController {
 	
 	private static final Logger logger = Logger.getLogger(LogInController.class);
 	
 	@Inject
	private MemberService member;
 	
 	///////////////////////////////////////////////////////////////////////////////////
 	//Login																			          //
 	//////////////////////////////////////////////////////////////////////////////////
 	@RequestMapping(value = "/", method = RequestMethod.POST)
 	public @ResponseBody List<Map<String, String>> login(HttpSession session, String memberId,String memberPw){
 		logger.info("mobile 로그인 하러 들어왓슴미다 : " + memberId + " : " + memberPw);
 		
 		SessionUtil sUtil = new SessionUtil();
 		MemberCommonVO vo = null;
 		Map<String, String> toMobile = new HashMap<String, String>();
 		
 		int chk = 0;
 		//아이디만 체크해서 값을 가져옴
 		vo = member.loginchk(memberId);
 		logger.info(vo);
 		if(vo == null){
 			//아이디 없음
 			logger.info("이푸 vo가 널이라면!!! 아이디나 패스워드가 없는거임...혹은 암것도 안쓴거...");
 //			session.setAttribute("idchk", 0);
 		}else{
 			//요기부터
 			//아이디가 있는 경우 비번 체크
 			chk = sUtil.pwChk(memberPw, vo);
 			if(chk==0){	//비번이 맞을경우 세션을 한번 비우고 셋팅
 				logger.info("if(chk==0)");
 				toMobile.put("chk", String.valueOf(0));
 				toMobile.put("memberIdx", String.valueOf(vo.getMemberIdx()));			
 				toMobile.put("memberId", vo.getMemberId());
 				toMobile.put("memberName", vo.getMemberName());
 				toMobile.put("memberLev", String.valueOf(vo.getMemberLev()));								
 				
 				session.setAttribute("loginchk", 0);				
 				session.setAttribute("memberIdx", vo.getMemberIdx());			
 				session.setAttribute("memberId", vo.getMemberId());
 				session.setAttribute("memberName", vo.getMemberName());
 				session.setAttribute("memberLev", vo.getMemberLev());
 				
 				
 				if(vo.getMemberLev() == 2){
 					//selIdx 등록해야됨...
 					toMobile.put("selIdx", String.valueOf(member.getSelIdx(vo.getMemberIdx())));
 					session.setAttribute("selIdx", member.getSelIdx(vo.getMemberIdx()));
 				}else{
 					toMobile.put("customerIdx", String.valueOf(member.getCustomerIdx(vo.getMemberIdx())));
 					session.setAttribute("customerIdx",  member.getCustomerIdx(vo.getMemberIdx()));
 				}
 				
 				logger.info("비번이 맞으므로 세팅 끝~~~~~~~~~~~~~~~~~~!!!!");
 			}else if(chk==1) {	//비번이 틀렸을 경우
 				logger.info("if(chk==1)  비번이 틀렸을 경우..");
 				toMobile.put("chk", String.valueOf(1));
 				session.setAttribute("loginchk", 1);
 			}else {	//그외 알수없는 경우?
 				logger.info("if(chk==???)  그외 경우??");
 				toMobile.put("chk", String.valueOf(2));
 				session.setAttribute("loginchk", 2);
 			} //요까징 end inner if~ else			
 		}//end if~ else		
 		List<Map<String, String>> list = new ArrayList<Map<String,String>>();
 		list.add(toMobile);
 		return list;
 	}
 }
