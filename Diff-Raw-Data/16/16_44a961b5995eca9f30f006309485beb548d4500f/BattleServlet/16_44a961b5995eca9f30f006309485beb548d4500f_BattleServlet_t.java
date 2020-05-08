 package com.server.fickapets;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.List;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 public class BattleServlet extends HttpServlet {
 
 	private static final long serialVersionUID = 6227254987843138881L;
 
 	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String action = req.getRequestURI().toLowerCase();
		if (action.equals("/create")) {
 			createBattle(resp, req.getParameter("uid1"), req.getParameter("uid2"));
		} else if (action.equals("/sendmove")) {
 			doMove(resp, req.getParameter("uid"), req.getParameter("bid"), req.getParameter("move"), Double.parseDouble(req.getParameter("strength")));
		} else if (action.equals("/openbattles") || action.equals("outgoingchallenges") || action.equals("incomingchallenges")) {
 			handleBattleQuery(resp, action, req.getParameter("uid"));
		} else if (action.equals("/closebattle")) {
 			closeBattle(resp, req.getParameter("uid"), req.getParameter("bid"));
		} else if (action.equals("/battledata")) {
 			battleData(resp, req.getParameter("uid"), req.getParameter("bid"));
 		}
 	}
 
 	private void battleData(HttpServletResponse resp, String uid, String bid) throws IOException {
 		if (checkNonNullParams(resp, uid, bid)) return;
 		Battle b = Battle.getById(bid);
 		if (b == null) {
 			resp.sendError(400, "No battle with bid: " + bid);
 		} else {
 			String xml = b.getXMLDataForUser(uid);
 			if (xml == null) {
 				resp.sendError(400, "User with uid: " + uid + " is not a particpant in battle with bid: " + bid);
 			} else {
 				resp.setStatus(200);
 				PrintWriter wr = resp.getWriter();
 				wr.write(xml);
 				wr.close();
 			}
 		}
 	}
 
 	private void handleBattleQuery(HttpServletResponse resp, String action, String uid) throws IOException {
 		if (checkNonNullParams(resp, uid)) return;
 		List<Battle> battles;
 		if (action.equals("openbattles")) {
 			battles = Battle.getOpenBattles(uid);
 		} else if (action.equals("outgoingchallenges")) {
 			battles = Battle.getOutgoingChallenges(uid);
 		} else {
 			battles = Battle.getIncomingChallenges(uid);
 		}
 		resp.setStatus(200);
 		PrintWriter wr = resp.getWriter();
 		for (int i = 0; i < battles.size(); i++) {
 			wr.write(battles.get(i).getId());
 			if (i != battles.size() - 1) wr.write("\n");
 		}
 		wr.close();
 	}
 
 	private void closeBattle(HttpServletResponse resp, String uid, String bid) throws IOException {
 		if (checkNonNullParams(resp, uid, bid)) return;
 		Battle battle = Battle.getById(bid);
 		if (battle == null) {
 			resp.sendError(400, "No battle with bid: " + bid);
 		} else {
 			if (battle.close(uid)) {
 				if (!battle.deleteIfClosed()) battle.save();
 				resp.setStatus(200);
 			} else {
 				resp.sendError(400, "User with uid: " + uid + " is not a particpant in battle with bid: " + bid + ", or that player has already closed that battle.");
 			}
 		}
 	}
 
 
 	private void doMove(HttpServletResponse resp, String uid, String bid, String move, Double strength) throws IOException {
 		if (checkNonNullParams(resp, uid, bid, move, strength)) return;
 		Battle battle = Battle.getById(bid);
 		if (battle == null) {
 			resp.sendError(400, "No battle with bid: " + bid);
 		} else {
 			if (battle.setMove(uid, move, strength)) {
 				battle.save();
 				resp.setStatus(200);
 			} else {
 				resp.sendError(400, "User with uid: " + uid + " is not a particpant in battle with bid: " + bid + ", or that player has already made a move in that battle.");
 			}
 		}
 	}
 
 	private void createBattle(HttpServletResponse resp, String uid1, String uid2) throws IOException {
 		if (checkNonNullParams(resp, uid1, uid2)) return;
 		Battle b = new Battle(uid1, uid2);
 		b.save();
 		resp.setStatus(200);
 		resp.getWriter().write(b.getId());
 	}
 	
 	private boolean checkNonNullParams(HttpServletResponse resp, Object... params) throws IOException {
 		for (Object o : params) {
 			if (o == null) {
 				resp.sendError(400, "missing one or more parameters");
 				return true;
 			}
 		}
 		return false;
 	}
 }
