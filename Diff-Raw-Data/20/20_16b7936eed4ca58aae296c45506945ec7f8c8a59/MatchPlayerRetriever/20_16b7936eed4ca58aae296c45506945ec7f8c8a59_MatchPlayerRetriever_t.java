 // %471978769:de.hattrickorganizer.logik%
 package de.hattrickorganizer.logik;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import plugins.IHOMiniModel;
 import plugins.IMatchDetails;
import plugins.ISpielerPosition;
 import plugins.ITeamLineup;
 import de.hattrickorganizer.model.MatchPosition;
 import de.hattrickorganizer.model.TeamLineup;
 import de.hattrickorganizer.tools.HOLogger;
 
 
 /**
  * Class that extract the list of players that played in each match with the position field where
  * he played. He consider also sent off and injured players
  *
  * @author Draghetto HO
  */
 public class MatchPlayerRetriever {
     //~ Instance fields ----------------------------------------------------------------------------
 
     private IHOMiniModel model;
 
     // key MatchId -- Value Hashtable
     private Map<Integer,List<MatchPosition>> matches;
 
     //~ Constructors -------------------------------------------------------------------------------
 
     /**
      * Creates a new MatchManager object.
      *
      * @param miniModel the HO model
      */
     public MatchPlayerRetriever(IHOMiniModel miniModel) {
         matches = new Hashtable<Integer,List<MatchPosition>>();
         model = miniModel;
     }
 
     //~ Methods ------------------------------------------------------------------------------------
 
     /**
      * Return the right HRF to be used for the match
      *
      * @param matchId id of selected match
      *
      * @return the hrf number that is right after the match being played
      */
     public final int getHrf(int matchId) {
         final IMatchDetails matchDetails = model.getMatchDetails(matchId);
         final Date date = matchDetails.getSpielDatum();
         Timestamp time = new Timestamp(date.getTime());
 		int hrfID = 0;
 
 		// Try to get HRF after match date
         String query = "SELECT HRF_ID FROM HRF WHERE Datum>'" + time.toString() + "' ORDER BY Datum asc";
         ResultSet rs = model.getAdapter().executeQuery(query);
 
 		try {
 			if (rs != null) {
 				if (rs.first()) {
 					hrfID = rs.getInt("HRF_ID");
 				}
 				//if not hrf before get first before
 				else {
 					query = "SELECT HRF_ID FROM HRF WHERE Datum<='" + time.toString() + "' ORDER BY Datum DESC";
 					rs = model.getAdapter().executeQuery(query);
 
 					if (rs.first()) {
 						hrfID = rs.getInt("HRF_ID");
 					}
 				}
 			}
 		} catch (Exception e) {
 			HOLogger.instance().log(getClass(),e);
         } finally {
             try {
             	if (rs != null) rs.close();
             } catch (SQLException e1) {
             }
         }
 		return hrfID;
 	}
 
     /**
      * Method that returns the MatchData, if not present it calculates  it before returing it
      *
      * @param matchId id of selected match
      *
      * @return The Hashtable of playerID/position
      */
     public final List<MatchPosition> getMatchData(int matchId) {
         final List<MatchPosition> p = matches.get(new Integer(matchId));
 
         if (p == null) {
             return loadMatch(matchId);
         }
 
         return p;
     }
 
     /**
      * Clean the matches hashtable where previous calculation are stored
      */
     public final void clear() {
         matches = new Hashtable<Integer,List<MatchPosition>>();
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param matchLineup TODO Missing Method Parameter Documentation
      * @param playerId TODO Missing Method Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     private int getAreaOfPlayer(ITeamLineup matchLineup, int playerId) {
         // Area of the injured player is obtained from the spot where he is
         int area = 0;
 
         for (int ar = 0; ar < 4; ar++) {
             final List<String> areaList = matchLineup.getArea(ar);
 
             if (areaList.contains("" + playerId)) {
                 area = ar;
                 break;
             }
         }
 
         return area;
     }
 
     /**
      * Method that returns the area of the field where a certain position belongs
      *
      * @param position position code
      *
      * @return the area int code
      */
     private int getAreaOfPosition(int position) {
         if (position < 2) {
             return ITeamLineup.KEEPER;
         }
 
         if (position < 6) {
             return ITeamLineup.DEFENCE;
         }
 
         if (position < 10) {
             return ITeamLineup.MIDFIELD;
         }
 
         return ITeamLineup.ATTACK;
     }
 
     /**
      * Method that returns the starting lineup  object for the matchid, with PlayerID instead of
      * player names
      *
      * @param matchId id of selected match
      * @param playerIds Collection of PlayerID and playerName relations
      *
      * @return Lineup object
      */
     private ITeamLineup getLineup(int matchId, Map<String,String> playerIds) {
         final TeamLineup lineup = new TeamLineup();
         final IMatchDetails match = model.getMatchDetails(matchId);
         boolean isHome = false;
 
         if (match.getHeimId() == model.getBasics().getTeamId()) {
             isHome = true;
         }
 
         final ITeamLineup temp = match.getTeamLineup(isHome);
 
         for (int i = 0; i < 4; i++) {
             final List<String> l2 = temp.getArea(i);
 
             for (Iterator<String> iter = l2.iterator(); iter.hasNext();) {
                 final String name = iter.next();
                 lineup.add("" + getPlayerId(name, playerIds), i);
             }
         }
 
         return lineup;
     }
 
     /**
      * Method that returns the match data from HO Database
      *
      * @param matchId id of selected match
      *
      * @return Array of Position object recovered from DB
      */
     private MatchPosition[] getMatchPosition(int matchId) {
         final MatchPosition[] pos = new MatchPosition[21];
         final String query =
             "select ROLEID, SPIELERID,HOPOSCODE, NAME from MATCHLINEUPPLAYER where MATCHID = "
             + matchId + "  and TEAMID = " + model.getBasics().getTeamId();
         final ResultSet rs = model.getAdapter().executeQuery(query);
 
         try {
             while (rs.next()) {
                 MatchPosition position = new MatchPosition();
                 int rid = -2;
                 try {
                 	rid = rs.getInt("ROLEID");
                	if((rid > 0 && rid < 22) || (rid >= ISpielerPosition.startLineup && rid <= ISpielerPosition.substForward)) {
                 		pos[rid - 1] = position;
                 		position.setPlayerID(rs.getInt("SPIELERID"));
                 		position.setName(rs.getString("NAME"));
                 		position.setPosition(model.getHelper().getPosition(rs.getInt("HOPOSCODE")));
                 	} else {
                 		HOLogger.instance().debug(getClass(), "Problem in match: " + matchId + ", unknown RoleID " + rid);
                 	}
                 } catch (Exception e1) {
                     HOLogger.instance().log(getClass(), "Error. MatchID: " + matchId);
                     HOLogger.instance().log(getClass(), e1);
                 }
             }
         } catch (Exception e) {
         	HOLogger.instance().log(getClass(), "Error! MatchID: " + matchId);
             HOLogger.instance().log(getClass(),e);
         } finally {
             try {
                 rs.close();
             } catch (SQLException e1) {
             }
         }
 
         return pos;
     }
 
     /**
      * If a sure match is found returns the playerid of the given lastName if the match is not sure
      * a 0 is returned
      *
      * @param lastName name of player
      * @param ids Map of match between playerId and PlayerNames
      *
      * @return the playerId
      */
     private int getPlayerId(String lastName, Map<String,String> ids) {
         int count = 0;
         int id = 0;
 
         for (Iterator<String> iter = ids.keySet().iterator(); iter.hasNext();) {
             final String key = iter.next();
             final String playerName = ids.get(key);
 
             if (playerName.indexOf(lastName) > -1) {
                 count++;
                 id = Integer.parseInt(key);
             }
         }
 
         if (count != 1) {
             return 0;
         }
 
         return id;
     }
 
     /**
      * Method that try to recover the sentoff player
      *
      * @param matchId id of selected match
      * @param lineup The empty matchPosition as loaded from HO Database
      * @param position TODO Missing Constructuor Parameter Documentation
      * @param startingLineup Starting Lineup object
      * @param playerIds TODO Missing Constructuor Parameter Documentation
      *
      * @return updated position object
      */
     private MatchPosition getSentOffPLayer(int matchId, MatchPosition[] lineup, int position,
                                            ITeamLineup startingLineup, Map<String,String> playerIds) {
         final MatchPosition matchPosition = lineup[position];
 
         if (matchPosition == null) return null;
 
         // Get list of playerID from comment
         final String query = "select SPIELERID from MATCHHIGHLIGHTS WHERE MATCHID = " + matchId
                              + " and TEAMID = " + model.getBasics().getTeamId()
                              + " AND TYP=5 AND SUBTYP > 11";
         final ResultSet rs = model.getAdapter().executeQuery(query);
 
         try {
             while (rs.next()) {
                 try {
                     final int id = rs.getInt("SPIELERID");
                     boolean found = false;
 
                     for (int i = 0; i < lineup.length; i++) {
                         final MatchPosition pos = lineup[i];
 
                         if ((pos != null) && (id == pos.getPlayerID())) {
                             found = true;
                             break;
                         }
                     }
 
                     if (found) {
                         continue;
                     }
 
                     final int area = getAreaOfPlayer(startingLineup, id);
                     final Iterator<String> it = startingLineup.getArea(area).iterator();
 
                     while (it.hasNext()) {
                         final String tmpId = it.next();
 
                         if (tmpId.equalsIgnoreCase("" + id)) {
                             matchPosition.setPlayerID(id);
 
                             final String name = playerIds.get("" + id);
 
                             matchPosition.setName(name);
 
                             final int areaPos = getAreaOfPosition(matchPosition.getPosition());
 
                             if (area != areaPos) {
                                 switch (area) {
                                     case 1:
                                         matchPosition.setPosition(3);
                                         break;
 
                                     case 2:
                                         matchPosition.setPosition(7);
                                         break;
 
                                     case 3:
                                         matchPosition.setPosition(10);
                                         break;
 
                                     default:
                                         break;
                                 }
                             }
 
                             return matchPosition;
                         }
                     }
                 } catch (SQLException e1) {
                     HOLogger.instance().log(getClass(),e1);
                 }
             }
         } catch (SQLException e) {
             // DO NOTHING
         } finally {
             try {
                 rs.close();
             } catch (SQLException e1) {
             }
         }
 
         return matchPosition;
     }
 
     /**
      * Method the returns an hashtable of all player on roster at the specified HRF
      *
      * @param hrf irf number
      *
      * @return Map of playersId
      */
     private Map<String,String> getTeamPlayers(int hrf) {
         final Map<String,String> list = new Hashtable<String,String>();
         final String query = "select SPIELERID,NAME from SPIELER where HRF_ID=" + hrf;
         final ResultSet rs = model.getAdapter().executeQuery(query);
 
         try {
             while (rs.next()) {
                 list.put(rs.getString("SPIELERID"), rs.getString("NAME"));
             }
         } catch (SQLException e) {
             //HOLogger.instance().log(getClass(),e);
         } finally {
             try {
                 rs.close();
             } catch (SQLException e1) {
             }
         }
 
         return list;
     }
 
     /**
      * Method that process the match and store it in the cache
      *
      * @param matchId id of selected match
      *
      * @return Hashtable of PlayerID/PositionCode
      */
     private List<MatchPosition> loadMatch(int matchId) {
         final List<MatchPosition> players = process(matchId);
         matches.put(new Integer(matchId), players);
         return players;
     }
 
     /**
      * Method that extracts the Match Data
      *
      * @param matchId id of selected match
      *
      * @return Hashtable of PlayerID/PositionCode
      */
     private List<MatchPosition> process(int matchId) {
         final int hrf = getHrf(matchId);
         final Map<String,String> playerIds = getTeamPlayers(hrf);
         final List<MatchPosition> players = new ArrayList<MatchPosition>();
 
         final ITeamLineup matchLineup = getLineup(matchId, playerIds);
 
         //int[] startingLineup = getLineup(matchId, matchPosition);
         final MatchPosition[] matchPosition = getMatchPosition(matchId);
 
         for (int i = 0; i < 11; i++) {
             // If playerID==0 red card
             if (matchPosition[i] == null || matchPosition[i].getPlayerID() == 0) {
                 // get updated position with sent off data
                 MatchPosition posTmp = getSentOffPLayer(matchId, matchPosition, i, matchLineup,
                                                         playerIds);
 
                 // update array with the new data
                 matchPosition[i] = posTmp;
             }
 
             players.add(matchPosition[i]);
         }
 
         for (int i = 18; i < 21; i++) {
             // Injured player
             if ((matchPosition[i] != null) && (matchPosition[i].getPlayerID() > 0)) {
                 // PlayerId of the Injured player
                 final int subsid = matchPosition[i].getPlayerID();
 
                 final int area = getAreaOfPlayer(matchLineup, subsid);
 
                 // Get starting players in that area
                 final List<String> startingLineup = matchLineup.getArea(area);
 
                 // Cycle thru all the players that ended the match / + red cards already included
                 for (int index = 0; index < 11; index++) {
                 	// if player played in the same area as the injured player
                 	if (matchPosition[index] != null && area == getAreaOfPosition(matchPosition[index].getPosition())) {
                 		boolean isFound = false;
                 		final Iterator<String> it = startingLineup.iterator();
 
                 		// search for him in the starting lineup
                 		while (it.hasNext()) {
                 			final String tmpId = it.next();
 
                 			if (tmpId.equalsIgnoreCase("" + matchPosition[index].getPlayerID())) {
                 				isFound = true;
                 			}
                 		}
 
                 		// if not found he is the one who replaced the injured
                 		if (!isFound) {
                 			// Set the injured player position to the same as the one who replaced him
                 			matchPosition[i].setPosition(matchPosition[index].getPosition());
 
                 			// update players and stop searching
                 			players.add(matchPosition[i]);
 
                 			break;
                 		}
 //                	} else if (matchPosition[index] != null) {
 //                		HOLogger.instance().log(getClass(), "matchPosition["+index+"] is null, matchId:" + matchId + " i: " + i + ", subsid: " + subsid + ", area: " + area);
 //                		HOLogger.instance().log(getClass(), "------------");
 //                		for (int m=0; m<matchPosition.length; m++) {
 //                			MatchPosition mp = matchPosition[m];
 //                			HOLogger.instance().log(getClass(), (mp == null ? "MP Null!" : "MP posi: " + mp.getPosition() + ", pid: " + mp.getPlayerID() + ", " + mp.getName()));
 //                		}
 //                		HOLogger.instance().log(getClass(), "------------");
 //                		for (Iterator it=players.iterator(); it.hasNext(); ) {
 //                			MatchPosition mp = (MatchPosition)it.next();
 //                			HOLogger.instance().log(getClass(), (mp == null ? "PlayerMP Null!" : "Players posi: " +mp.getPosition() + ", pid: " + mp.getPlayerID() + ", " + mp.getName()));
 //                		}
 //                		HOLogger.instance().log(getClass(), "------------");
 //                		List pls = matchLineup.getArea(ITeamLineup.KEEPER);
 //                		for (Iterator it=pls.iterator(); it.hasNext(); ) {
 //                			HOLogger.instance().log(getClass(), "Keeper ids: " + it.next());
 //                		}
 //                		pls = matchLineup.getArea(ITeamLineup.DEFENCE);
 //                		for (Iterator it=pls.iterator(); it.hasNext(); ) {
 //                			HOLogger.instance().log(getClass(), "Defender ids: " + it.next());
 //                		}
 //                		pls = matchLineup.getArea(ITeamLineup.MIDFIELD);
 //                		for (Iterator it=pls.iterator(); it.hasNext(); ) {
 //                			HOLogger.instance().log(getClass(), "Midfielder ids: " + it.next());
 //                		}
 //                		pls = matchLineup.getArea(ITeamLineup.ATTACK);
 //                		for (Iterator it=pls.iterator(); it.hasNext(); ) {
 //                			HOLogger.instance().log(getClass(), "Striker ids: " + it.next());
 //                		}
                 	}
                 }
             }
         }
 
         return players;
     }
 }
