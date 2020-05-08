 // %2443578200:hoplugins.teamplanner.manager%
 package hoplugins.teamplanner.manager;
 
 import hoplugins.Commons;
 import hoplugins.TeamPlanner;
 import hoplugins.teamplanner.ui.tabs.WeekHeader;
 import hoplugins.teamplanner.vo.HTWeek;
 import hoplugins.teamplanner.vo.PlayerData;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 
 import plugins.IFuturePlayer;
 import plugins.IFutureTrainingManager;
 import plugins.IFutureTrainingWeek;
 import plugins.ISkillup;
 import plugins.ISpieler;
 
 
 /**
  * Missing Class Documentation
  *
  * @author Draghetto
  */
 public class TeamPlayerManager {
     //~ Static fields/initializers -----------------------------------------------------------------
 
     private static Map<String,TeamPlayerData> teamPlayers;
     private static TeamPlayerManager _me;
     private static int newPlayerId = -1000;
     private static int[] salary = new int[IFutureTrainingManager.FUTUREWEEKS];
 
     //~ Constructors -------------------------------------------------------------------------------
 
     /**
      * Creates a new TeamPlayerManager object.
      */
     private TeamPlayerManager() {
         teamPlayers = new HashMap<String, TeamPlayerData>();
         loadTrainees();
         initSalary();
     }
 
     //~ Methods ------------------------------------------------------------------------------------
 
     /**
      * Missing Method Documentation
      *
      * @param week Missing Method Parameter Documentation
      *
      * @return Missing Return Method Documentation
      */
     public static int getSalary(HTWeek week) {
         int index = WeekHeader.instance().getColumnIndex(week);
         return salary[index];
     }
 
     /**
      * Missing Method Documentation
      *
      * @return Missing Return Method Documentation
      */
     public static TeamPlayerManager instance() {
         if (_me == null) {
             _me = new TeamPlayerManager();
         }
 
         return _me;
     }
 
     /**
      * Missing Method Documentation
      *
      * @param week Missing Method Parameter Documentation
      *
      * @return Missing Return Method Documentation
      */
     public List<ISpieler> getBoughtPlayersForWeek(HTWeek week) {
         List<ISpieler> l = new ArrayList<ISpieler>();
 
         for (Iterator<TeamPlayerData> iter = teamPlayers.values().iterator(); iter.hasNext();) {
             TeamPlayerData element = iter.next();
 
             if (week.equals(element.getStartWeek())) {
                 l.add(element.getData());
             }
         }
 
         return l;
     }
 
     /**
      *
      */
     public int getNewPlayerId() {
         newPlayerId--;
         return newPlayerId;
     }
 
     /**
      * Missing Method Documentation
      *
      * @param week Missing Method Parameter Documentation
      *
      * @return Missing Return Method Documentation
      */
     public List<ISpieler> getPlayersAvailableAtWeek(HTWeek week) {
         List<ISpieler> l = new ArrayList<ISpieler>();
 
         for (Iterator<TeamPlayerData> iter = teamPlayers.values().iterator(); iter.hasNext();) {
             TeamPlayerData element = iter.next();
 
             if ((element.getStartWeek().compareTo(week) <= 0)
                 && (element.getFinalWeek().compareTo(week) > 0)) {
                 l.add(element.getData());
             }
         }
 
         return l;
     }
 
     /**
      * Missing Method Documentation
      *
      * @param spielerId Missing Method Parameter Documentation
      * @param week Missing Method Parameter Documentation
      *
      * @return Missing Return Method Documentation
      */
     public ISpieler getPredictedPlayerAtWeek(int spielerId, HTWeek week) {
     	TeamPlayerData data = getTeamPlayer(spielerId);
     	ISpieler spieler = data.getData();
     	int coTrainer = Commons.getModel().getVerein().getCoTrainer();
    	//int keeperTrainer = Commons.getModel().getVerein().getTorwartTrainer(); // not used any longer
     	ISpieler t = Commons.getModel().getTrainer();
     	int trainer = 0;
     	if (t != null) {
     		trainer = t.getTrainer();
     	}
     	List<IFutureTrainingWeek> futures = Commons.getModel().getFutureTrainingWeeks();
 
		IFutureTrainingManager ftm = Commons.getModel().getFutureTrainingManager(spieler, futures, coTrainer, trainer);
     	int act = TeamPlanner.ACTUALWEEK.getWeekNumber();
     	int weeks = week.getWeekNumber() - act;
 
     	if (weeks > IFutureTrainingManager.FUTUREWEEKS) {
     		weeks = IFutureTrainingManager.FUTUREWEEKS;
     	}
 
     	if (ftm != null) {
     		IFuturePlayer fp = ftm.previewPlayer(data.getStartWeek().getWeekNumber() - act, weeks);
 
     		PlayerData pd = new PlayerData();
     		pd.setAge(fp.getAge());
     		pd.setAttack(fp.getAttack());
     		pd.setDefense(fp.getDefense());
     		pd.setExperience(spieler.getErfahrung());
     		pd.setForm(spieler.getForm());
     		pd.setGoalKeeping(fp.getGoalkeeping());
     		pd.setLeadership(spieler.getFuehrung());
     		pd.setPassing(fp.getPassing());
     		pd.setPlayerId(fp.getPlayerId());
     		pd.setPlayerName(spieler.getName());
     		pd.setPlayMaking(fp.getPlaymaking());
     		pd.setSetPieces(fp.getSetpieces());
     		pd.setSpeciality(spieler.getSpezialitaet());
     		pd.setStamina(fp.getStamina());
     		pd.setTSI(spieler.getTSI());
     		pd.setWing(fp.getCross());
     		return Commons.getModel().createPlayer(pd);
     	}
     	return null;
     }
 
     /**
      * Returns a list of list of skillups, one each week
      *
      * @return list
      */
     public List<List<String>> getPredictedSkillup() {
     	List<List<String>> sk = new ArrayList<List<String>>();
 
     	for (int i = 0; i < IFutureTrainingManager.FUTUREWEEKS; i++) {
     		sk.add(new ArrayList<String>());
     	}
 
     	int coTrainer = Commons.getModel().getVerein().getCoTrainer();
    	//int kepperTrainer = Commons.getModel().getVerein().getTorwartTrainer(); //deprecated
     	ISpieler t = Commons.getModel().getTrainer();
     	int trainer = 0;
     	if (t != null) {
     		trainer = t.getTrainer();
     	}
     	List<IFutureTrainingWeek> futures = Commons.getModel().getFutureTrainingWeeks();
 
     	for (Iterator<TeamPlayerData> iter = teamPlayers.values().iterator(); iter.hasNext();) {
     		TeamPlayerData player = iter.next();
			IFutureTrainingManager ftm = Commons.getModel().getFutureTrainingManager(player.getData(), futures, coTrainer, trainer);
     		int act = TeamPlanner.ACTUALWEEK.getWeekNumber();
     		int weeks = player.getFinalWeek().getWeekNumber() - act - 1;
 
     		if (weeks > IFutureTrainingManager.FUTUREWEEKS) {
     			weeks = IFutureTrainingManager.FUTUREWEEKS;
     		}
 
     		ftm.previewPlayer(player.getStartWeek().getWeekNumber() - act, weeks);
 
     		List<ISkillup> futureSkillups = ftm.getFutureSkillups();
 
     		for (Iterator<ISkillup> iterator = futureSkillups.iterator(); iterator.hasNext();) {
     			ISkillup skillup = iterator.next();
     			HTWeek week = new HTWeek(skillup.getHtSeason(), skillup.getHtWeek());
     			int col = week.getWeekNumber() - act;
 
     			List<String> l = sk.get(col - 1);
     			l.add(player.getData().getSpielerID() + "");
     		}
     	}
 
     	return sk;
     }
 
     /**
      * Missing Method Documentation
      *
      * @param playerId Missing Method Parameter Documentation
      *
      * @return Missing Return Method Documentation
      */
     public HTWeek getSellingWeek(int playerId) {
         TeamPlayerData tp = getTeamPlayer(playerId);
 
         if ((tp.getFinalWeek().getWeekNumber() - TeamPlanner.ACTUALWEEK.getWeekNumber()) > 50) {
             return null;
         }
 
         return tp.getFinalWeek();
     }
 
     /**
      * Missing Method Documentation
      *
      * @param week Missing Method Parameter Documentation
      *
      * @return Missing Return Method Documentation
      */
     public List<ISpieler> getSoldPlayersForWeek(HTWeek week) {
         List<ISpieler> l = new ArrayList<ISpieler>();
 
         for (Iterator<TeamPlayerData> iter = teamPlayers.values().iterator(); iter.hasNext();) {
             TeamPlayerData element = iter.next();
             HTWeek pWeek = getSellingWeek(element.getData().getSpielerID());
 
             if ((pWeek != null) && (week.equals(pWeek))) {
                 l.add(element.getData());
             }
         }
 
         return l;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param week
      * @param player
      */
     public void buyPlayer(HTWeek week, ISpieler player) {
         TeamPlayerData tp = getTeamPlayer(player.getSpielerID());
         tp.setData(player);
         tp.setStartWeek(week);
 
         int actualSalary = PlayerSalaryCalculator.getEstimatedSalary(player);
         player.setGehalt(actualSalary);
         updateSalaries(week, player, false);
     }
 
     /**
      * Cancel a Transfer
      *
      * @param id playerId to cancel transfer for
      * @param sell Missing Constructuor Parameter Documentation
      */
     public void cancelTransfer(int id, boolean sell) {
         TeamPlayerData tp = getTeamPlayer(id);
 
         if (sell) {
             updateSalaries(tp.getFinalWeek(), tp.getData(), false);
 
             HTWeek end = TeamPlanner.ACTUALWEEK.copy();
             end.addWeek(100);
             tp.setFinalWeek(end);
         } else {
             updateSalaries(tp.getStartWeek(), tp.getData(), true);
             teamPlayers.remove("" + id);
         }
     }
 
     /**
      * Missing Method Documentation
      *
      * @param week Missing Method Parameter Documentation
      * @param playerId Missing Method Parameter Documentation
      */
     public void sellPlayer(HTWeek week, int playerId) {
         TeamPlayerData tp = getTeamPlayer(playerId);
         tp.setFinalWeek(week);
         updateSalaries(week, tp.getData(), true);
     }
 
     /**
      * Missing Method Documentation
      *
      * @param id Missing Method Parameter Documentation
      *
      * @return Missing Return Method Documentation
      */
     private TeamPlayerData getTeamPlayer(int id) {
         TeamPlayerData tp = (TeamPlayerData) teamPlayers.get("" + id);
 
         if (tp == null) {
             tp = new TeamPlayerData();
 
             HTWeek start = TeamPlanner.ACTUALWEEK.copy();
             start.addWeek(1);
             tp.setStartWeek(start);
 
             HTWeek end = TeamPlanner.ACTUALWEEK.copy();
             end.addWeek(100);
             tp.setFinalWeek(end);
             teamPlayers.put("" + id, tp);
         }
 
         return tp;
     }
 
     /**
      * Missing Method Documentation
      */
     private void initSalary() {
         for (Iterator<TeamPlayerData> iter = teamPlayers.values().iterator(); iter.hasNext();) {
             TeamPlayerData tpd = iter.next();
             updateSalaries(tpd.getStartWeek(), tpd.getData(), false);
         }
     }
 
     /**
      * Missing Method Documentation
      */
     private void loadTrainees() {
         Vector<ISpieler> players = Commons.getModel().getAllSpieler();
 
         for (Iterator<ISpieler> iter = players.iterator(); iter.hasNext();) {
             ISpieler player = iter.next();
             TeamPlayerData tp = getTeamPlayer(player.getSpielerID());
             tp.setData(player);
         }
     }
 
     /**
      * Missing Method Documentation
      *
      * @param week Missing Method Parameter Documentation
      * @param player Missing Method Parameter Documentation
      * @param sell Missing Method Parameter Documentation
      */
     private void updateSalaries(HTWeek week, ISpieler player, boolean sell) {
         int sal = (int) (player.getGehalt() / Commons.getModel().getXtraDaten().getCurrencyRate());
 
         int index = WeekHeader.instance().getColumnIndex(week);
         int actualSeason = TeamPlanner.ACTUALWEEK.getSeason();
 
         for (int i = index; i < IFutureTrainingManager.FUTUREWEEKS; i++) {
             HTWeek actualWeek = WeekHeader.instance().getColumnWeek(i);
 
             if (actualWeek.getSeason() != actualSeason) {
                 actualSeason = actualWeek.getSeason();
 
                 ISpieler fp = getPredictedPlayerAtWeek(player.getSpielerID(), actualWeek);
                 int newSalary = PlayerSalaryCalculator.getEstimatedSalary(fp);
 
                 if (player.getLaenderspiele() != Commons.getModel().getBasics().getLand()) {
                     newSalary = (int) (newSalary * 1.2);
                 }
 
                 if (newSalary < sal) {
                     newSalary = sal;
                 }
 
                 sal = newSalary;
             }
 
             if (sell) {
                 salary[i] -= sal;
             } else {
                 salary[i] += sal;
             }
         }
     }
 }
