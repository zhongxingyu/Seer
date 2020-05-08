 // %554748293:hoplugins.teamplanner.ui.tabs.stadium.listener%
 package hoplugins.teamplanner.ui.tabs.stadium.listener;
 
 import hoplugins.TeamPlanner;
 
 import hoplugins.teamplanner.ui.controller.calculator.Calculator;
 import hoplugins.teamplanner.ui.model.OperationCell;
 import hoplugins.teamplanner.ui.tabs.WeekHeader;
 import hoplugins.teamplanner.util.Util;
 import hoplugins.teamplanner.vo.HTWeek;
 
 import plugins.IFutureTrainingManager;
 
 import javax.swing.table.TableModel;
 
 
 /**
  * Missing Class Documentation
  *
  * @author Draghetto
  */
 public class StadiumIncomeCalculator extends Calculator {
     //~ Methods ------------------------------------------------------------------------------------
 
     /**
      * Missing Method Documentation
      *
      * @param week Missing Method Parameter Documentation
      *
      * @return Missing Return Method Documentation
      */
     public int getAttendanceAt(HTWeek week) {
         int fans = TeamPlanner.getFansPane().getFansAt(week);
         int seasonIndex = 0;
         int columnIndex = WeekHeader.instance().getColumnIndex(week);
         int weekNumber = TeamPlanner.ACTUALWEEK.getWeek();
 
         for (int futureWeek = 0; futureWeek < columnIndex; futureWeek++) {
             weekNumber++;
 
             if (weekNumber == 17) {
                 weekNumber = 1;
                 seasonIndex++;
             }
         }
 
         int fanMorale = (int) TeamPlanner.getFansPane().getFansMoraleAt(week);
         int att = 0;
 
         switch (fanMorale) {
             case 1:
                 att = 7;
                 break;
 
             case 2:
                 att = 9;
                 break;
 
             case 3:
                 att = 11;
                 break;
 
             case 4:
                 att = 15;
                 break;
 
             case 5:
                 att = 18;
                 break;
 
             case 6:
                 att = 21;
                 break;
 
             case 7:
                 att = 25;
                 break;
         }
 
         return att * fans;
     }
 
     /**
      * Missing Method Documentation
      *
      * @param row Missing Method Parameter Documentation
      * @param model Missing Method Parameter Documentation
      */
     @Override
 	public void doCalculate(int row, TableModel model) {
         for (int i = 0; i < IFutureTrainingManager.FUTUREWEEKS; i++) {
             int income = 0;
             HTWeek week = WeekHeader.instance().getColumnWeek(i);
             int size = TeamPlanner.getStadiumPane().getStadiumSize(week).getSize();
 
             // League income
             if (((week.getWeek() % 2) == 0) && (i > 0) && (week.getWeek() < 16)) {
                 HTWeek pWeek = WeekHeader.instance().getColumnWeek(i - 1);
                 int pSize = TeamPlanner.getStadiumPane().getStadiumSize(pWeek).getSize();
                 income += getIncome(pWeek, pSize, false);
                 income += getIncome(week, size, false);
             }
 
             // Qualifier Income
             if (week.getWeek() == 16) {
                 int event = TeamPlanner.getFuturePane().getYearSetting(week.getSeason())
                                        .getSeasonEventType();
 
                 // Promotion Qualifier, away
                 if (event == 1) {
                     income += (getIncome(week, size, true) / 2d);
                 }
 
                 // Relegation Qualifier, home
                 if (event == -1) {
                     income += (getIncome(week, size, true) / 2d);
                 }
             }
 
             // Cup Income
             int round = TeamPlanner.getFuturePane().getYearSetting(week.getSeason()).getCupRound();
             int teamRank = loadTeamRank(week);
 
             // Still in cup
             if (week.getWeek() <= round) {
                 if (isHome(week.getWeek(), teamRank)) {
                     income += ((getIncome(week, size, true) * 2) / 3);
                 } else {
                     int stadiumSize = getStadiumSize(TeamPlanner.getFuturePane()
                                                                 .getYearSetting(week.getSeason())
                                                                 .getSerie() + 1);
                    int teams = (int) Math.pow(2, getCupRounds() - week.getWeek() + 1);
                    int opponentRanking = teams - teamRank;
                    int opponentSerie = getSerie(opponentRanking);

                     // TODO Income depends on serie of the opponent?
                     income += ((getIncome(week, stadiumSize, true) * 1) / 3);
                 }
             } // Friendly 
             else {
                 income += 3000;
             }
 
             OperationCell cell = Util.getOperationCell(model, row, i);
             cell.getOperation().getInner().setMoney(income);
         }
     }
 
     /**
      * Missing Method Documentation
      *
      * @return Missing Return Method Documentation
      */
     private int getCupRounds() {
         return 16;
     }
 
     /**
      * Missing Method Documentation
      *
      * @param round Missing Method Parameter Documentation
      * @param teamRank Missing Method Parameter Documentation
      *
      * @return Missing Return Method Documentation
      */
     private boolean isHome(int round, int teamRank) {
         int teams = (int) Math.pow(2, getCupRounds() - round + 1);
 
         if (teamRank > (teams / 2)) {
             return true;
         }
 
         return false;
     }
 
     /**
      * Missing Method Documentation
      *
      * @param week Missing Method Parameter Documentation
      * @param size Missing Method Parameter Documentation
      * @param isCupMatch TODO Missing Constructuor Parameter Documentation
      *
      * @return Missing Return Method Documentation
      */
     private int getIncome(HTWeek week, int size, boolean isCupMatch) {
         double attendance = getAttendanceAt(week);
 
         if (isCupMatch) {
             // TODO Is this ok to approx cup extra income?
             attendance = attendance * 1.2d;
         }
 
         if (attendance > size) {
             attendance = size;
         }
 
         double perc = (attendance * 1.0d) / (size * 1.0d);
 
         double multiplier = 7 + (perc * 0.4);
         int money = (int) (attendance * multiplier);
         return money;
     }

    /**
     * DOCUMENT ME!
     *
     * @param opponentRanking
     *
     * @return
     */
    private int getSerie(int opponentRanking) {
        int rank = opponentRanking;
        int count = 0;
        int serie = 0;

        while (opponentRanking > count) {
            serie++;
            count += getSeriesSize(serie);
        }

        return serie;
    }

    /**
      * Missing Method Documentation
      *
      * @param level Missing Method Parameter Documentation
      *
      * @return Missing Return Method Documentation
      */
     private int getSeriesSize(int level) {
         int size = 8 * (int) Math.pow(4, level - 1);
 
         if (size > 8192) {
             size = 8192;
         }
 
         return size;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param serie
      *
      * @return
      */
     private int getStadiumSize(int serie) {
         // TODO Estimate stadium size baded on serie
         return 12000;
     }
 
     /**
      * Missing Method Documentation
      *
      * @param week Missing Method Parameter Documentation
      *
      * @return Missing Return Method Documentation
      */
     private int loadTeamRank(HTWeek week) {
         int serie = -1;
         int position = -1;
 
         if (week.getSeason() == TeamPlanner.ACTUALWEEK.getSeason()) {
             serie = TeamPlanner.getFuturePane().getYearSetting(week.getSeason()).getSerie();
             position = TeamPlanner.getFuturePane().getYearSetting(week.getSeason())
                                   .getLeaguePosition();
         } else {
             serie = TeamPlanner.getFuturePane().getYearSetting(week.getSeason() - 1).getSerie();
             position = TeamPlanner.getFuturePane().getYearSetting(week.getSeason() - 1)
                                   .getLeaguePosition();
         }
 
         int rank = 0;
 
         for (int i = 1; i < serie; i++) {
             rank += getSeriesSize(i);
         }
 
         int diff = getSeriesSize(serie) / 8;
         rank += (diff * (position - 1));
         return rank;
     }
 }
