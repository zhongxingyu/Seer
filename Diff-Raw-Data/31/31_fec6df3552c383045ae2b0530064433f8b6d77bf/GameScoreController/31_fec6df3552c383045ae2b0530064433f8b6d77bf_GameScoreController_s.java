 package com.ctb.pilot.gamification.service;
 
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import com.ctb.pilot.gamification.model.GameLog;
 import com.ctb.pilot.gamification.model.Leaderboard;
 import com.ctb.pilot.user.model.User;
 
 @Controller
 public class GameScoreController {
 
 	@Autowired
 	private GameScoreService gameScoreService;
 
 	@RequestMapping("/services/game/score/leaderboard.do")
 	public String showLeaderboard(HttpServletRequest req, Model model) {
 		int gameSequence = Integer.parseInt(req.getParameter("game_sequence"));
 		Leaderboard leaderboard = gameScoreService.getLeaderboard(gameSequence);
 		model.addAttribute("entries", leaderboard.getEntries());
 		model.addAttribute("gameSequence", gameSequence);
 		return "/services/games/leaderboard";
 	}
 
 	@RequestMapping("/services/game/score/history.do")
 	public String showHistory(HttpServletRequest req, Model model) {
 		int gameSequence = Integer.parseInt(req.getParameter("game_sequence"));
 		int userSequence = Integer.parseInt(req.getParameter("user_sequence"));
		String nickname = req.getParameter("nickname");
 		List<GameLog> history = gameScoreService.getHistory(gameSequence,
 				userSequence);
 		model.addAttribute("nickname", nickname);
 		model.addAttribute("entries", history);
 		return "/services/games/history";
 	}
 
 	@RequestMapping(value = "/services/game/score/record.do", method = RequestMethod.POST)
 	public void recordScore(HttpServletRequest req, HttpServletResponse resp) {
 		HttpSession session = req.getSession();
 		int gameSequence = Integer.parseInt(req.getParameter("game_sequence"));
 		User user = (User) session.getAttribute("user");
 		int userSequence = user.getSequence();
 		int score = Integer.parseInt(req.getParameter("score"));
 		gameScoreService.recordScore(gameSequence, userSequence, score);
 	}
 
 }
