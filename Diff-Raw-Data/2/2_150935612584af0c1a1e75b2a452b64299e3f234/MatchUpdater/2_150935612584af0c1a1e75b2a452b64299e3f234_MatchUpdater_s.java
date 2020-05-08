 // %4165694865:de.hattrickorganizer.logik%
 package de.hattrickorganizer.logik;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import plugins.IHOMiniModel;
 import plugins.IMatchDetails;
 import de.hattrickorganizer.model.MatchPosition;
 import de.hattrickorganizer.tools.HOLogger;
 
 
 /**
  * TODO Missing Class Documentation
  *
  * @author TODO Author Name
  */
 public class MatchUpdater {
     //~ Methods ------------------------------------------------------------------------------------
 
     /**
      * TODO Missing Method Documentation
      *
      * @param model TODO Missing Method Parameter Documentation
      * @param matchId TODO Missing Method Parameter Documentation
      */
     public static void updateMatch(IHOMiniModel model, int matchId) {
         final IMatchDetails matchDetail = model.getMatchDetails(matchId);
 
         if ((matchDetail.getHeimId() != model.getBasics().getTeamId())
             && (matchDetail.getGastId() != model.getBasics().getTeamId())) {
             return;
         }
 
         final MatchPlayerRetriever updater = new MatchPlayerRetriever(model);
         final List matchPlayers = updater.getMatchData(matchId);
 
         //HOLogger.instance().log(getClass(),matchPlayers);
         final String query =
             "select ROLEID, SPIELERID,PositionCode from MATCHLINEUPPLAYER where MATCHID = "
             + matchId + "  and TEAMID = " + model.getBasics().getTeamId();
         final ResultSet rs = model.getAdapter().executeQuery(query);
         final List updates = new ArrayList();
 
         try {
             while (rs.next()) {
                 try {
                     final int role = rs.getInt("ROLEID");
                     final int id = rs.getInt("SPIELERID");
                     final int pos = rs.getInt("PositionCode");
 
                     if (pos == -1) {
                         if (role < 12) {
                             final MatchPosition position = (MatchPosition) matchPlayers.get(role
                                                                                             - 1);
                             updates.add("UPDATE MATCHLINEUPPLAYER SET SPIELERID="
                                         + position.getPlayerID() + ", NAME='" + position.getName()
                                         + "', STATUS=1, FIELDPOS=" + position.getPosition()
                                         + " WHERE MATCHID=" + matchId + " AND ROLEID=" + role
                                         + " and TEAMID = " + model.getBasics().getTeamId());
                             continue;
                         }
 
                         if (role > 18) {
                             for (Iterator iter = matchPlayers.iterator(); iter.hasNext();) {
                                 final MatchPosition position = (MatchPosition) iter.next();
 
                                if (position.getPlayerID() == id) {
                                     updates.add("UPDATE MATCHLINEUPPLAYER SET SPIELERID="
                                                 + position.getPlayerID() + ", NAME='"
                                                 + position.getName() + "', STATUS=2, FIELDPOS="
                                                 + position.getPosition() + " WHERE MATCHID="
                                                 + matchId + " AND ROLEID=" + role
                                                 + " and TEAMID = " + model.getBasics().getTeamId());
                                     continue;
                                 }
                             }
                         }
                     }
                 } catch (SQLException e1) {
                     HOLogger.instance().log(MatchUpdater.class,e1);
                 }
             }
 
             for (Iterator iter = updates.iterator(); iter.hasNext();) {
                 final String queryUpdate = (String) iter.next();
                 model.getAdapter().executeUpdate(queryUpdate);
             }
         } catch (SQLException e) {
             HOLogger.instance().log(MatchUpdater.class,e);
         }
     }
 }
