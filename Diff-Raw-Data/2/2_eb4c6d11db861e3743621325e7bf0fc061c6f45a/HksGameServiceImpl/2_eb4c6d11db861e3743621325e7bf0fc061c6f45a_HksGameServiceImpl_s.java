 package edu.harvard.med.hks.service.impl;
 
 import java.text.NumberFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import edu.harvard.med.hks.dao.HksGameDao;
 import edu.harvard.med.hks.dao.SlotDao;
 import edu.harvard.med.hks.model.Game;
 import edu.harvard.med.hks.model.Slot;
 import edu.harvard.med.hks.model.Slot.Status;
 import edu.harvard.med.hks.server.GeneralException;
 import edu.harvard.med.hks.service.HksGameService;
 
 @Service
 public class HksGameServiceImpl implements HksGameService {
 	@SuppressWarnings("unused")
 	private static Logger logger = Logger.getLogger(HksGameServiceImpl.class);
 	@Autowired HksGameDao hksGameDao;
 	@Autowired private SlotDao slotDao;
 	
 	private boolean isClientInfoOutput = false;
 	
 	private int roundsPlayed =0;
 
 	private void appendLog(Slot slot, String log) {
 		String gameLog = "";
 		if (slot.getLog() != null) {
 			gameLog = new String(slot.getLog());
 		}
 		gameLog += new Date().toString() + "\t"+log + "\n";
 		slot.setLog(gameLog.getBytes());
 	}
 	
 	private void appendClientInfo(Slot slot, HttpServletRequest req){
 		if (!isClientInfoOutput) {
 
 			StringBuilder sb = new StringBuilder();
 			sb.append("Remote addr. | Remote port | Remote Host | Remote user | Cookies | User Agent | OS    | Pixels \n");
 
 			sb.append(req.getRemoteAddr()).append("|");
 			sb.append(req.getRemotePort()).append("|");
 			sb.append(req.getRemoteHost()).append("|");
 			sb.append(req.getRemoteUser()).append("|");
 			sb.append(req.getHeader("Cookie")).append("|");
 			sb.append(req.getHeader("User-Agent")).append("|");
 			sb.append(req.getHeader("UA-OS")).append("|");
 			sb.append(req.getHeader("UA-Pixels")).append("|\n");
 
 			appendLog(slot, sb.toString());
 			isClientInfoOutput = true;
 		}
 	}
 	
 	private void outputSlotData(Slot slot){
 		StringBuilder sb = new StringBuilder();
 		
 		sb.append("Slot ID\tSTatus\tGame ID\tSlot Number\tWorker Number\tWorker ID\tBlack Mark Count\tBetray Payoff\n");
 		sb.append(slot.getSlotId()).append("\t").append(slot.getStatus()).append("\t").
 			append(slot.getGame().getId()).append("\t").append(slot.getSlotNumber()).append("\t").
 			append(slot.getWorkerNumber()).append("\t").append(slot.getWorkerId()).append("\t").
 			append(slot.getBlackMarkCount()).append("\t").append(slot.getCurrentBetrayPayoff()).
 			append("\n");
 		
 		appendLog(slot, sb.toString());
 	}
 
 	@Override
 	public Map<String, Object> betray(HttpServletRequest req) throws GeneralException {
 		Map<String, Object> result = new HashMap<String, Object>();
 		List<Slot> byProperty = slotDao.getByProperty("slotId", req.getParameter("slotId"));
 		if (byProperty.isEmpty()) {
 			return result;
 		}
 		Slot slot = byProperty.get(0);
 		appendClientInfo (slot, req);
 		if (!slot.getGame().getGameId().equals(req.getParameter("gameId"))) {
 			result.put("status", Status.DROPPED);
 			return result;
 		}
 		if (!slot.getStatus().equals(Status.PLAY.toString())) {
 			return result;
 		}
 		slot.setLastAction("Betray");
 
 		if (slot.getBetrayCaughtSampling() <= slot.getBetrayCaughtChance()) {
 			slot.setBlackMarkCount(slot.getBlackMarkCount() + 1);
 			slot.setBetrayCaught(true);
 			appendLog(slot, "Betray caught. Number of black marks: " + slot.getBlackMarkCount());
 		} else {
 			slot.setBetrayCaught(false);
 		}
 		slot.setSurvival(slot.getSurvivalSampling() 
 				<= slot.getTempteeSurvivalChance());
 		appendLog(slot, "Survive to next round: " + 
 				(slot.getSurvivalSampling() <= slot.getTempteeSurvivalChance()));
 		slot.setRewardCaughtAsBetrayal(false);
 		slot.setTempteeBonus(slot.getTempteeBonus() + slot.getCurrentBetrayPayoff());
 		appendLog(slot, "Betray and earn " + (slot.getCurrentBetrayPayoff()));
 		
 		slot.setStatus(Status.PAYOFF.toString());
 		slotDao.update(slot);
 		output(result, slot);
 
 		return result;
 	}
 
 	@Override
 	public void doneTutorial(HttpServletRequest req) throws GeneralException {
 		List<Slot> byProperty = slotDao.getByProperty("slotId",
 				req.getParameter("slotId"));
 		if (byProperty.isEmpty()) {
 			return;
 		}
 		Slot slot = byProperty.get(0);
 		if (!slot.getGame().getGameId().equals(req.getParameter("gameId"))) {
 			return;
 		}
 		slot.setStatus(Status.PLAY.toString());
 		slotDao.update(slot);
 	}
 
 	private String fillZero(double number) {
 		if (number == 1) {
 			number = 0.999;
 		}
 		String result = "" + ((int) (number * 1000));
 		while (result.length() < 3) {
 			result = "0" + result;
 		}
 		return result;
 	}
 
 	@Override
 	public String getEmptySlotId(String gameId, String workerId) throws GeneralException {
 		if (StringUtils.isEmpty(workerId)) {
 			return null;
 		}
 		List<Game> byProperty = hksGameDao.getByProperty("gameId", gameId);
 		if (byProperty.isEmpty()) {
 			return null;
 		}
 		Game game = byProperty.get(0);
 		if (!workerId.equals("admin7")) {
 			Map<String, Object> rect = new HashMap<String, Object>();
 			rect.put("game", game);
 			rect.put("workerId", workerId);
 			if (!slotDao.getByProperties(rect).isEmpty()) {
 				return null;
 			}
 		}
 
 		Map<String, Object> rect = new HashMap<String, Object>();
 		rect.put("game", game);
 		rect.put("status", Status.INIT.toString());
 		List<Slot> availableSlots = slotDao.getByProperties(rect,
 				"slotNumber", true);
 		if (!availableSlots.isEmpty()) {
 			Slot slot = availableSlots.get(0);
 			slot.setStatus(Status.OCCUPIED.toString());
 			slotDao.update(slot);
 			return slot.getSlotId();
 		}
 		return null;
 	}
 
 	private void output(Map<String, Object> result, Slot slot) {
 		NumberFormat formatter = NumberFormat.getInstance();
 		formatter.setMaximumFractionDigits(2);
 		result.put("status", slot.getStatus());
 		result.put("currentBetrayPayoff", formatter.format(slot.getCurrentBetrayPayoff()));
 		result.put("blackMarkCount", slot.getBlackMarkCount());
 		result.put("tempteeBonus", formatter.format(slot.getTempteeBonus()));
 		result.put("version", slot.getUpdated().getTime());
 		result.put("tutorialStep", slot.getTutorialStep());
 		result.put("survival", slot.isSurvival());
 		result.put("rewardCaughtAsBetrayal", slot.isRewardCaughtAsBetrayal());
 		result.put("lastAction", slot.getLastAction());
 		result.put("betrayCaught", slot.isBetrayCaught());
 		result.put("rewardPayoff", slot.getRewardPayoff());
 		result.put("betrayNotCaughtChance", 1 - slot.getBetrayCaughtChance());
 		result.put("rewardNotCaughtAsBetrayalChance", 1 - slot.getRewardCaughtAsBetrayalChance());
 		result.put("endChance", 1 - slot.getTempteeSurvivalChance());
 		result.put("blackMarkUpperLimit", slot.getBlackMarkUpperLimit());
 		result.put("betrayNotCaughtSampling", fillZero(1 - slot.getBetrayCaughtSampling()));
 		result.put("rewardNotCaughtAsBetrayalSampling", fillZero(1 - slot.getRewardCaughtAsBetrayalSampling()));
 		result.put("notContinueSampling", fillZero(1 - slot.getSurvivalSampling()));
 		result.put("currentRound", slot.getCurrentRound());
 	}
 
 	@Override
 	public Map<String, Object> payoffAck(HttpServletRequest req) {
 		Map<String, Object> result = new HashMap<String, Object>();
 		List<Slot> byProperty = slotDao.getByProperty("slotId",
 				req.getParameter("slotId"));
 		if (byProperty.isEmpty()) {
 			return result;
 		}
 		Slot slot = byProperty.get(0);
 		if (!slot.getGame().getGameId().equals(req.getParameter("gameId"))) {
 			result.put("status", Status.DROPPED);
 			return result;
 		}
 		if (slot.isSurvival()) {
 			slot.setCurrentRound(slot.getCurrentRound() + 1);
 			slot.setStatus(Status.PLAY.toString());
 			samplingNewRound(slot);
 		} else {
 			slot.setStatus(Status.FINISHED.toString());
 		}
 		output(result, slot);
 		return result;
 	}
 	
 	@Override
 	public Map<String, Object> endChanceAck(HttpServletRequest req) {
 		Map<String, Object> result = new HashMap<String, Object>();
 		List<Slot> byProperty = slotDao.getByProperty("slotId",
 				req.getParameter("slotId"));
 		if (byProperty.isEmpty()) {
 			return result;
 		}
 		Slot slot = byProperty.get(0);
 		if (!slot.getGame().getGameId().equals(req.getParameter("gameId"))) {
 			result.put("status", Status.DROPPED);
 			return result;
 		}
 		
 		/*slot.setSurvival(slot.getSurvivalSampling() 
 				<= slot.getTempteeSurvivalChance());*/
 		
 		if (roundsPlayed < slot.getGame().getMaxRoundsNum()){
 			slot.setSurvival(true);
 			roundsPlayed++;
 		} else {
 			slot.setSurvival(false);
 		}
 		
 		//appendLog(slot, "Survive to next round: " + 
 			//	(slot.getSurvivalSampling() <= slot.getTempteeSurvivalChance()));
 		
 		appendLog(slot, "Survive to next round: "+slot.isSurvival());
 		
 		if (slot.isSurvival()) {
 			slot.setCurrentRound(slot.getCurrentRound() + 1);
 			slot.setStatus(Status.PLAY.toString());
 			samplingNewRound(slot);
 		} else {
 			slot.setStatus(Status.FINISHED.toString());
 		}
 		output(result, slot);
 		return result;
 	}
 	
 	private void samplingNewRound(Slot slot) {
 		Random random = new Random();
 		slot.setCurrentBetrayPayoff(slot.getRewardPayoff() 
 				+ random.nextInt(slot.getMaxBetrayPayoff()));
 		slot.setSurvivalSampling(random.nextDouble());
 		slot.setBetrayCaughtSampling(random.nextDouble());
 		slot.setRewardCaughtAsBetrayalSampling(random.nextDouble());
 	}
 	
 	@Override
 	public Map<String, Object> reward(HttpServletRequest req)
 			throws GeneralException {
 		Map<String, Object> result = new HashMap<String, Object>();
 		List<Slot> byProperty = slotDao.getByProperty("slotId",
 				req.getParameter("slotId"));
 		if (byProperty.isEmpty()) {
 			result.put("status", Status.DROPPED);
 			return result;
 		}
 		Slot slot = byProperty.get(0);
 		if (!slot.getGame().getGameId().equals(req.getParameter("gameId"))) {
 			result.put("status", Status.DROPPED);
 			return result;
 		}
 		if (!slot.getStatus().equals(Status.PLAY.toString())) {
 			return result;
 		}
 		
 		slot.setLastAction("Reward");
 		appendLog(slot, "Reward");
 		if (slot.getRewardCaughtAsBetrayalSampling() 
 				<= slot.getRewardCaughtAsBetrayalChance()) {
 			slot.setBlackMarkCount(slot.getBlackMarkCount() + 1);
 			slot.setRewardCaughtAsBetrayal(true);
 			appendLog(slot, "Reward but caught as betrayal. Number of black marks: " + slot.getBlackMarkCount());
 		} else {
 			slot.setRewardCaughtAsBetrayal(false);
 		}
 		slot.setSurvival(slot.getSurvivalSampling() 
 				<= slot.getTempteeSurvivalChance());
 		appendLog(slot, "Survive to next round: " + 
 				(slot.getSurvivalSampling() <= slot.getTempteeSurvivalChance()));
 		slot.setBetrayCaught(false);
 		slot.setTempteeBonus(slot.getTempteeBonus() + slot.getRewardPayoff());
 		slot.setStatus(Status.PAYOFF.toString());
 		slotDao.update(slot);
 		output(result, slot);
 		return result;
 	}
 	@Override
 	public Map<String, Object> sendFeedback(HttpServletRequest req)
 			throws GeneralException {
 		Map<String, Object> result = new HashMap<String, Object>();
 		List<Slot> byProperty = slotDao.getByProperty("slotId",
 				req.getParameter("slotId"));
 		if (byProperty.isEmpty()) {
 			result.put("status", Status.DROPPED);
 			return result;
 		}
 		Slot slot = byProperty.get(0);
 		if (!slot.getGame().getGameId().equals(req.getParameter("gameId"))) {
 			result.put("status", Status.DROPPED);
 			return result;
 		}
 		String feedback = req.getParameter("feedback");
 		appendLog(slot, "Feedback from player:\n" + feedback);
 		slot.setStatus(Status.THANKS.toString());
 		slotDao.update(slot);
 		
 		outputSlotData(slot);
 		return result;
 	}
 
 	public void setHksGameDao(HksGameDao hksGameDao) {
 		this.hksGameDao = hksGameDao;
 	}
 
 	public void setSlotDao(SlotDao slotDao) {
 		this.slotDao = slotDao;
 	}
 
 	@Override
 	public Map<String, Object> update(HttpServletRequest req)throws GeneralException {
 		String slotId = req.getParameter("slotId") ;
 		System.out.println("slot id = " + slotId) ;
 		List<Slot> byProperty = slotDao.getByProperty("slotId", req.getParameter("slotId"));
 		Map<String, Object> result = new HashMap<String, Object>();
 		if (byProperty.isEmpty()) {
 			result.put("status", Status.DROPPED);
 			return result;
 		}
 		Slot slot = byProperty.get(0);
 		if(!StringUtils.isEmpty(req.getParameter("next"))){
 			roundsPlayed++;
 			if (roundsPlayed < slot.getGame().getMaxRoundsNum()){
 			slot.setStatus(Status.PLAY.toString());
 			slot.setSurvival(true);
 			slot.setLastAction("");
 			slot.setBetrayCaught(false);
 			slot.setCurrentRound(1);
 			slot.setRewardCaughtAsBetrayalSampling(466);
 			slot.setBetrayCaughtSampling(713);
 			slot.setBlackMarkCount(0);
 			slot.setCurrentBetrayPayoff(11);
 			samplingNewRound(slot);
 			slotDao.update(slot);
 			
 			output(result, slot);
 			return result;
 			} else {
 				reward(req);
 			}
 		}
 		
 		if (!slot.getGame().getGameId().equals(req.getParameter("gameId"))) {
 			result.put("status", Status.DROPPED);
 			return result;
 		}
 		if (slot.getStatus().equals(Status.INIT.toString())) {
 			slot.setStatus(Status.TUTORIAL.toString());
 			slot.setTempteeBonus(slot.getInitTempteeBonus());
 			samplingNewRound(slot);
 		}
 		if (!StringUtils.isEmpty(req.getParameter("tutorialStep"))) {
 			slot.setTutorialStep(Integer.parseInt(
 					req.getParameter("tutorialStep")));
 		}
 		if (StringUtils.isEmpty(slot.getAssignmentId())
 				&& !StringUtils.isEmpty(req.getParameter("assignmentId"))) {
 			slot.setAssignmentId(req.getParameter("assignmentId"));
 		}
 		if (StringUtils.isEmpty(slot.getWorkerId())
 				&& !StringUtils.isEmpty(req.getParameter("workerId"))) {
 			slot.setWorkerId(req.getParameter("workerId"));
 		}
 		if (StringUtils.isEmpty(slot.getWorkerNumber())
 				&& !StringUtils.isEmpty(req.getParameter("workerNum"))) {
 			slot.setWorkerId(req.getParameter("workerNum"));
 		}
 		if (StringUtils.isEmpty(slot.getTurkSubmitTo())
 				&& !StringUtils.isEmpty(req.getParameter("turkSubmitTo"))) {
 			slot.setTurkSubmitTo(req.getParameter("turkSubmitTo"));
 		}
 		if (StringUtils.isEmpty(slot.getHitId())
 				&& !StringUtils.isEmpty(req.getParameter("hitId"))) {
 			slot.setHitId(req.getParameter("hitId"));
 		}
 		slotDao.update(slot);
 		output(result, slot);
 		return result;
 	}
 
 	@Override
 	public void setRoundsPlayed(int roundsPlayed) {
 		this.roundsPlayed = roundsPlayed;
 	}
 
 	@Override
 	public int getRoundsPlayed() {
 		return this.roundsPlayed;
 	}
 
 	@Override
 	public HksGameDao getGameDao() {
 		return hksGameDao;
 	}
 
 	@Override
 	public SlotDao getSlotDao() {
 		return slotDao;
 	}
 	
 	
 	
 	
 }
