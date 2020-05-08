 /*
  * Copyright (C) 2012 MineStar.de 
  * 
  * This file is part of Contao2.
  * 
  * Contao2 is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * Contao2 is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with Contao2.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.minestar.contao2.manager;
 
 import java.io.File;
 import java.sql.Blob;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 
 import de.minestar.contao2.core.Core;
 import de.minestar.contao2.units.ContaoGroup;
 import de.minestar.contao2.units.MCUser;
 import de.minestar.contao2.units.MCWarning;
 import de.minestar.contao2.units.PlayerWarnings;
 import de.minestar.contao2.units.Statistic;
 import de.minestar.minestarlibrary.database.AbstractMySQLHandler;
 import de.minestar.minestarlibrary.utils.ConsoleUtils;
 
 public class DatabaseManager extends AbstractMySQLHandler {
 
     private PlayerManager playerManager;
     private StatisticManager sManager;
 
     private PreparedStatement insertMCPay;
     private PreparedStatement updateExpireDate;
     private PreparedStatement updateGroup;
     private PreparedStatement selectMCPayByName;
     private PreparedStatement selectMCPayById;
     private PreparedStatement checkAccount;
     private PreparedStatement selectGroup;
     private PreparedStatement selectContaoId;
     private PreparedStatement checkContaoId;
     private PreparedStatement checkMCNick;
 
     private PreparedStatement getAccountDates;
     private PreparedStatement isInProbation;
     private PreparedStatement deleteProbeStatus;
     private PreparedStatement addProbeDate;
     private PreparedStatement isProbeMember;
     private PreparedStatement convertFreeToProbe;
 
     private PreparedStatement addWarning;
     private PreparedStatement deleteWarning;
 
     private PreparedStatement selectAllStatistics;
     private PreparedStatement selectAllWarnings;
     private PreparedStatement saveStatistics;
 
     private PreparedStatement canBeFree;
     private PreparedStatement hasUsedFreeWeek, setFreeWeekUsed;
 
     private final static int PROBE_TIME = 7;
 
     public DatabaseManager(String NAME, File SQLConfigFile) {
         super(NAME, SQLConfigFile);
     }
 
     @Override
     protected void createStructure(String NAME, Connection con) throws Exception {
         // Do nothing - structure is given
     }
 
     @Override
     protected void createStatements(String NAME, Connection con) throws Exception {
 
         insertMCPay = con.prepareStatement("INSERT INTO mc_pay (contao_user_id, minecraft_nick, expire_date, admin_nick, startDate, probeEndDate, usedFreePayWeek) VALUES (?,?,STR_TO_DATE(?,'%d.%m.%Y'),?, NOW(), ADDDATE(NOW(), INTERVAL " + PROBE_TIME + " DAY), ?)");
 
         updateExpireDate = con.prepareStatement("UPDATE mc_pay SET expire_date = STR_TO_DATE(?,'%d.%m.%Y') WHERE contao_user_id = ?");
 
         updateGroup = con.prepareStatement("UPDATE tl_member SET groups = ? WHERE ID = ?");
 
         selectMCPayByName = con.prepareStatement("SELECT minecraft_nick, contao_user_id, DATE_FORMAT(expire_date, '%d.%m.%Y') FROM mc_pay WHERE minecraft_nick = ? LIMIT 1");
 
         selectMCPayById = con.prepareStatement("SELECT minecraft_nick, contao_user_id, DATE_FORMAT(expire_date, '%d.%m.%Y') FROM mc_pay WHERE contao_user_id = ? LIMIT 1");
 
         checkAccount = con.prepareStatement("SELECT disable FROM tl_member WHERE id = ? LIMIT 1");
 
         selectGroup = con.prepareStatement("SELECT groups FROM tl_member WHERE id = ? LIMIT 1");
 
         selectContaoId = con.prepareStatement("SELECT id, username FROM tl_member WHERE username LIKE ?");
 
         checkContaoId = con.prepareStatement("SELECT 1 FROM mc_pay WHERE contao_user_id = ? LIMIT 1");
 
         checkMCNick = con.prepareStatement("SELECT 1 FROM mc_pay WHERE minecraft_nick = ? LIMIT 1");
 
         getAccountDates = con.prepareStatement("SELECT DATE_FORMAT(startDate, '%d.%m.%Y %H:%i:%s'), DATE_FORMAT(probeEndDate, '%d.%m.%Y %H:%i:%s'), DATE_FORMAT(expire_date, '%d.%m.%Y') FROM mc_pay WHERE minecraft_nick = ? LIMIT 1");
 
         deleteProbeStatus = con.prepareStatement("UPDATE mc_pay SET probeEndDate = NULL WHERE minecraft_nick = ?");
 
         convertFreeToProbe = con.prepareStatement("UPDATE mc_pay SET probeEndDate = ADDDATE(NOW(), INTERVAL " + PROBE_TIME + " DAY) WHERE minecraft_nick = ?");
 
         addProbeDate = con.prepareStatement("UPDATE mc_pay SET probeEndDate = ADDDATE(probeEndDate, INTERVAL ? DAY) WHERE minecraft_nick = ?");
 
         addWarning = con.prepareStatement("INSERT INTO mc_warning (mc_pay_id,reason,date,adminnickname) VALUES ((SELECT id FROM mc_pay WHERE minecraft_nick = ?), ?, STR_TO_DATE(?,'%d.%m.%Y %H:%i:%s'), ?)");
 
         selectAllWarnings = con.prepareStatement("SELECT minecraft_nick, mc_warning.reason, DATE_FORMAT(date, '%d.%m.%Y %H:%i:%s'),adminnickname FROM mc_warning,mc_pay WHERE mc_warning.mc_pay_id = mc_pay.id ORDER BY minecraft_nick,mc_warning.date");
 
         deleteWarning = con.prepareStatement("DELETE FROM mc_warning WHERE mc_pay_id = (SELECT id FROM mc_pay WHERE minecraft_nick = ?) AND DATE_FORMAT(date,'%d.%m.%Y %H:%i:%s') = ?");
 
         isProbeMember = con.prepareStatement("SELECT 1 FROM mc_pay WHERE minecraft_nick = ? AND probeEndDate IS NULL");
 
         isInProbation = con.prepareStatement("SELECT 1 FROM mc_pay WHERE minecraft_nick = ? AND DATEDIFF(NOW(),probeEndDate) < 0");
 
         selectAllStatistics = con.prepareStatement("SELECT minecraft_nick, totalBreak, totalPlaced FROM mc_pay ORDER BY minecraft_nick ASC");
 
         saveStatistics = con.prepareStatement("UPDATE mc_pay SET totalPlaced = ?, totalBreak = ? WHERE minecraft_nick = ?");
 
         canBeFree = con.prepareStatement("SELECT 1 FROM mc_pay WHERE contao_user_id = ? AND totalBreak + totalPlaced >= 10000 AND DATEDIFF(NOW(), probeEndDate) >= 7");
 
         hasUsedFreeWeek = con.prepareStatement("SELECT usedFreePayWeek FROM mc_pay WHERE minecraft_nick = ?");
 
         setFreeWeekUsed = con.prepareStatement("UPDATE mc_pay SET usedFreePayWeek = 1 WHERE minecraft_nick = ?");
     }
 
     public boolean hasUsedFreeWeek(String playerName) {
         // SELECT usedFreePayWeek FROM mc_pay WHERE minecraft_nick = ?
         try {
             hasUsedFreeWeek.setString(1, playerName);
             ResultSet result = hasUsedFreeWeek.executeQuery();
             if (result.next()) {
                 return result.getBoolean("usedFreePayWeek");
             }
             return true;
         } catch (Exception e) {
             ConsoleUtils.printException(e, Core.NAME, "Can't fetch results for hasUsedFreeWeek! PlayerName=" + playerName);
         }
         return true;
     }
 
     public void setFreeWeekUsed(String playerName) {
         // SELECT usedFreePayWeek FROM mc_pay WHERE minecraft_nick = ?
         try {
             setFreeWeekUsed.setString(1, playerName);
             setFreeWeekUsed.executeUpdate();
         } catch (Exception e) {
             ConsoleUtils.printException(e, Core.NAME, "Can't update hasUsedFreeWeek! PlayerName=" + playerName);
         }
     }
 
     public void addProbe(String playerName, int contaoID, String expDate, String modPlayer) {
 
         // INSERT INTO mc_pay (contao_user_id, minecraft_nick, expire_date,
         // admin_nick, startDate, probeEndDate) VALUES
         // (?,?,STR_TO_DATE(?,'%d.%m.%Y'),?, NOW(),
         // ADDDATE(NOW(), INTERVAL PROBE_TIME DAY))
         try {
             insertMCPay.setInt(1, contaoID);
             insertMCPay.setString(2, playerName);
             insertMCPay.setString(3, expDate);
             insertMCPay.setString(4, modPlayer);
             insertMCPay.setBoolean(5, false);
             insertMCPay.executeUpdate();
         } catch (Exception e) {
             ConsoleUtils.printException(e, Core.NAME, "Can't insert data in the mc_pay table! PlayerName=" + playerName + ",ContaoID=" + contaoID + ",ExpDate=" + expDate + ",modPlayer=" + modPlayer);
         }
     }
 
     public void setExpDateInMCTable(String date, int contaoID) {
 
         // UPDATE mc_pay SET expire_date = STR_TO_DATE(?,'%d.%m.%Y') WHERE
         // contao_user_id = ?
         try {
             updateExpireDate.setString(1, date);
             updateExpireDate.setInt(2, contaoID);
             updateExpireDate.executeUpdate();
         } catch (Exception e) {
             ConsoleUtils.printException(e, Core.NAME, "Can't update expire date! Date=" + date + ",ContaoID=" + contaoID);
         }
     }
 
     // SET CONTAO-GROUP IN 'tl_member'
     public void updateContaoGroup(ContaoGroup group, int contaoID) {
 
         // UPDATE tl_member SET groups = ? WHERE ID = ?
         try {
             updateGroup.setString(1, new String(group.getContaoString().getBytes("UTF-8")));
             updateGroup.setInt(2, contaoID);
             updateGroup.executeUpdate();
         } catch (Exception e) {
             ConsoleUtils.printException(e, Core.NAME, "Can't update Contao Member Group! GroupManagerGroup=" + group.getName() + ",ContaoGroupString=" + group.getContaoString() + ",ContaoID=" + contaoID);
         }
     }
 
     // GET INGAME DATA FROM DB
     public MCUser getIngameData(String player) {
 
         // SELECT minecraft_nick, contao_user_id, DATE_FORMAT(expire_date,
         // '%d.%m.%Y') FROM mc_pay WHERE minecraft_nick = ? LIMIT 1
         try {
             selectMCPayByName.setString(1, player);
             ResultSet result = selectMCPayByName.executeQuery();
             if (result.next())
                 return new MCUser(result.getString(1), result.getInt(2), result.getString(3));
         } catch (Exception e) {
             ConsoleUtils.printException(e, Core.NAME, "Can't select MCUser from mc_pay by name! PlayerName=" + player);
         }
 
         return null;
     }
 
     // GET INGAME DATA FROM DB
     public MCUser getIngameData(int id) {
 
         // SELECT minecraft_nick, contao_user_id, DATE_FORMAT(expire_date,
         // '%d.%m.%Y') FROM mc_pay WHERE contao_user_id = ? LIMIT 1
         try {
             selectMCPayById.setInt(1, id);
             ResultSet result = selectMCPayById.executeQuery();
             if (result.next())
                 return new MCUser(result.getString(1), result.getInt(2), result.getString(3));
         } catch (Exception e) {
             ConsoleUtils.printException(e, Core.NAME, "Can't select MCUser from mc_pay by id! ContaoID=" + id);
         }
 
         return null;
     }
 
     // IS ACCOUNT ACTIVATED
     public boolean isContaoAccountActive(int id) {
 
         // SELECT disable FROM tl_member WHERE id = ? LIMIT 1
         try {
             checkAccount.setInt(1, id);
             ResultSet result = checkAccount.executeQuery();
             return result.next() && !result.getBoolean("disable");
         } catch (Exception e) {
             ConsoleUtils.printException(e, Core.NAME, "Can't check whether ContaoAccount is active! ContaoID=" + id);
         }
 
         return false;
     }
 
     public String getContaoGroup(int id) {
 
         // SELECT groups FROM tl_member WHERE id = ? LIMIT 1
         try {
             selectGroup.setInt(1, id);
             ResultSet result = selectGroup.executeQuery();
 
             if (result.next()) {
                 Blob blob = result.getBlob("groups");
                 byte[] bdata = blob.getBytes(1, (int) blob.length());
                 String group = new String(bdata);
 
                 group = group.replace("a:2:{i:0;s:1:\"", "");
                 group = group.replace("\";i:1;s:1:\"2", "");
                 group = group.replace("a:1:{i:0;s:1:\"", "");
                 group = group.replace("\";}", "");
                 return getMCGroupName(group);
             }
         } catch (Exception e) {
             ConsoleUtils.printException(e, Core.NAME, "Can't get ContaoGroupName from tl_member! ContaoID=" + id);
         }
 
         return null;
     }
 
     // GET MC GROUP FROM CONTAO-GROUPID
     private String getMCGroupName(String id) {
         if (id.equalsIgnoreCase("1"))
             return ContaoGroup.FREE.getName();
         else if (id.equalsIgnoreCase("2"))
             return ContaoGroup.PAY.getName();
         else if (id.equalsIgnoreCase("3"))
             return ContaoGroup.ADMIN.getName();
         else if (id.equalsIgnoreCase("5"))
             return ContaoGroup.PROBE.getName();
         else if (id.equalsIgnoreCase("6"))
             return ContaoGroup.MOD.getName();
         return ContaoGroup.DEFAULT.getName();
     }
 
     // GET CONTAO ID
     public HashMap<Integer, String> getContaoID(String username) {
 
         // SELECT id, username FROM tl_member WHERE username LIKE %?%
         HashMap<Integer, String> list = new HashMap<Integer, String>();
 
         try {
             selectContaoId.setString(1, "%" + username + "%");
             ResultSet result = selectContaoId.executeQuery();
             while (result.next())
                 list.put(result.getInt("id"), result.getString("username"));
 
         } catch (Exception e) {
             ConsoleUtils.printException(e, Core.NAME, "Can't get ContaoID from tl_member! Username=" + username);
         }
 
         return list;
     }
 
     // IS CONTAO-ID IN MC-TABLE
     public boolean isContaoIDInMCTable(int id) {
 
         // SELECT 1 FROM mc_pay WHERE contao_user_id = ? LIMIT 1
         try {
             checkContaoId.setInt(1, id);
             return checkContaoId.executeQuery().next();
         } catch (Exception e) {
             ConsoleUtils.printException(e, Core.NAME, "Can't check wether contaoID is existing! ContaoID=" + id);
         }
 
         return false;
     }
 
     // IS MCNICK IN MC-TABLE
     public boolean isMCNickInMCTable(String name) {
 
         // SELECT 1 FROM mc_pay WHERE minecraft_nick = ? LIMIT 1
         try {
             checkMCNick.setString(1, name);
             return checkMCNick.executeQuery().next();
         } catch (Exception e) {
             ConsoleUtils.printException(e, Core.NAME, "Can't check whether playerName is in mc_pay! Name=" + name);
         }
 
         return false;
     }
 
     public String[] getAccountDates(String playerName) {
 
         // SELECT startDate, probeEndDate, expire_date FROM mc_pay WHERE
         // minecraft_nick = ?
         // LIMIT 1
         try {
             getAccountDates.setString(1, playerName);
             ResultSet rs = getAccountDates.executeQuery();
             if (rs.next()) {
                 return new String[]{rs.getString(1), rs.getString(2), rs.getString(3)};
             } else
                 return null;
         } catch (Exception e) {
             ConsoleUtils.printException(e, Core.NAME, "Can't select dates from mc_pay! PlayerName=" + playerName);
         }
 
         return null;
     }
 
     public boolean addProbeTime(int days, String playerName) {
 
         // UPDATE mc_pay SET probeEndDate = ADDDATE(probeEndDate, INTERVAL ?
         // DAY) WHERE minecraft_nick = ?
         try {
             addProbeDate.setInt(1, days);
             addProbeDate.setString(2, playerName);
             return addProbeDate.executeUpdate() == 1;
         } catch (Exception e) {
             ConsoleUtils.printException(e, Core.NAME, "Can't update probeEndDate in mc_pay! PlayerName=" + playerName + ",Days=" + days);
         }
 
         return false;
     }
 
     public void deleteProbeStatus(String playerName) {
 
         // UPDATE mc_pay SET probeEndDate = NULL WHERE minecraft_nick = ?
         try {
             deleteProbeStatus.setString(1, playerName);
             deleteProbeStatus.executeUpdate();
         } catch (Exception e) {
             ConsoleUtils.printException(e, Core.NAME, "Can't delete probe status by setting probeEndDate to NULL! PlayerName=" + playerName);
         }
     }
 
     public boolean isProbeMember(String playerName) {
 
         // SELECT 1 FROM mc_pay WHERE minecraft_nick = ? AND probeEndDate IS
         // NULL
         try {
             isProbeMember.setString(1, playerName);
             return !isProbeMember.executeQuery().next();
         } catch (Exception e) {
             ConsoleUtils.printException(e, Core.NAME, "Can't check whether player is probe member! PlayerName=" + playerName);
         }
 
         return false;
     }
 
     public boolean isInProbation(String playerName) {
 
         // SELECT 1 FROM mc_pay WHERE minecraft_nick = ? AND
         // DATEDIFF(NOW(),probeEndDate) < 0
         try {
             isInProbation.setString(1, playerName);
             return isInProbation.executeQuery().next();
         } catch (Exception e) {
             ConsoleUtils.printException(e, Core.NAME, "Can't check whether player is in probation time! PlayerName=" + playerName);
         }
         return false;
     }
 
     public boolean degradeFree(String playerName) {
 
         // UPDATE mc_pay SET probeEndDate = ADDDATE(NOW(), INTERVAL PROBE_TIME
         // DAY)
         // WHERE minecraft_nick = ?
         try {
             convertFreeToProbe.setString(1, playerName);
             return convertFreeToProbe.executeUpdate() == 1;
         } catch (Exception e) {
             ConsoleUtils.printException(e, Core.NAME, "Can't degrade a free user to probe user! PlayerName=" + playerName);
         }
 
         return false;
     }
 
     private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
 
     public boolean addWarning(String playerName, String reason, String adminName) {
 
         String date = dateFormat.format(new Date());
         sManager.addWarning(playerName, new MCWarning(reason, date, adminName));
 
         // INSERT INTO mc_warning (mc_pay_id,reason,date,adminnickname) VALUES
         // ((SELECT id
         // FROM mc_pay WHERE minecraft_nick = ?), ?, NOW(), ?)
         try {
             addWarning.setString(1, playerName);
             addWarning.setString(2, reason);
             addWarning.setString(3, date);
             addWarning.setString(4, adminName);
             return addWarning.executeUpdate() == 1;
         } catch (Exception e) {
             ConsoleUtils.printException(e, Core.NAME, "Can't add a warning to a player! PlayerName=" + playerName + ",adminName=" + adminName + ",text=" + reason);
         }
 
         return false;
     }
 
     public boolean removeWarning(String playerName, String date) {
         try {
             deleteWarning.setString(1, playerName);
             deleteWarning.setString(2, date);
             return deleteWarning.executeUpdate() == 1;
         } catch (Exception e) {
             ConsoleUtils.printException(e, Core.NAME, "Can't remove a warning from mc_pay! PlayerName=" + playerName + ",WarningDate=" + date);
         }
         return false;
     }
 
     public HashMap<String, Statistic> loadAllStatistics() {
         HashMap<String, Statistic> statistics = new HashMap<String, Statistic>();
         try {
             ResultSet result = selectAllStatistics.executeQuery();
             while (result.next()) {
                 statistics.put(result.getString("minecraft_nick").toLowerCase(), new Statistic(result.getInt("totalPlaced"), result.getInt("totalBreak")));
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
         return statistics;
     }
 
     public HashMap<String, PlayerWarnings> loadAllWarnings() {
         HashMap<String, PlayerWarnings> warnings = new HashMap<String, PlayerWarnings>();
         try {
             ResultSet result = selectAllWarnings.executeQuery();
             PlayerWarnings thisPlayer = null;
             String playerName = null;
             while (result.next()) {
                 playerName = result.getString("minecraft_nick").toLowerCase();
                 thisPlayer = warnings.get(playerName);
                 if (thisPlayer == null) {
                     thisPlayer = new PlayerWarnings();
                     warnings.put(playerName, thisPlayer);
                 }
                 thisPlayer.addWarning(new MCWarning(result.getString(2), result.getString(3), result.getString(4)));
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
         return warnings;
     }
 
     public void saveStatistics(String playerName, int totalPlaced, int totalBreak) {
         try {
             saveStatistics.setInt(1, totalPlaced);
             saveStatistics.setInt(2, totalBreak);
             saveStatistics.setString(3, playerName);
             saveStatistics.executeUpdate();
         } catch (Exception e) {
             ConsoleUtils.printException(e, Core.NAME, "Can't store statistics to database! PlayerName=" + playerName + ",totalPlaced=" + totalPlaced + ",totalBreak=" + totalBreak);
         }
     }
 
     public void performContaoCheck(String playerName, String group) {
         // GET INGAME-NAME AND CONTAO-ID
         MCUser user = getIngameData(playerName);
         if (user == null) {
             // NO NICK FOUND = return;
             return;
         }
 
         // GET CONTAO GROUP
         String contaoGroup = getContaoGroup(user.getContaoID());
         if (contaoGroup == null) {
             // NO CONTAOUSER FOUND = RETURN
             return;
         }
 
         // CONTAO GROUP DIFFERS = SET MC GROUP TO CONTAO GROUP
         if (!contaoGroup.equalsIgnoreCase(group)) {
             String oldGroup = group;
             group = playerManager.updateGroupManagerGroup(playerName, contaoGroup);
             ConsoleUtils.printWarning(Core.NAME, "Player '" + playerName + "'(MCNick is '" + user.getNickname() + "' ) has a different contao( " + contaoGroup + " ) and groupmanager( " + oldGroup + " )-group!");
         }
 
         // Check if paytime is expired
         if (group.equals(ContaoGroup.PAY.getName()))
             checkPayUser(playerName, user);
         // Check if probe user has ended probationtime with 10k changed
         // blocks and no warnings
         else if (group.equals(ContaoGroup.PROBE.getName()))
             checkProbeUser(playerName, user);
     }
 
     private void checkPayUser(String playerName, MCUser user) {
 
         if (user.getExpDate().equalsIgnoreCase("11.11.1111"))
             throw new RuntimeException(user.getNickname() + " is payUser, but has expireDate 11.11.1111!");
 
         // CHECK DATES
         try {
             DateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
             Date expDate = (Date) formatter.parse(user.getExpDate());
             expDate = getRelativeDate(expDate, 1);
             Date now = new Date();
 
             // STILL PAY-ACCOUNT
             if (expDate.after(now))
                 return;
 
             // MOVE PAY TO FREE
             playerManager.updateGroupManagerGroup(playerName, ContaoGroup.FREE);
 
             setExpDateInMCTable("11.11.1111", user.getContaoID());
             updateContaoGroup(ContaoGroup.FREE, user.getContaoID());
             ConsoleUtils.printInfo(Core.NAME, "Player '" + playerName + "'s Payaccount has expired! Moving to free member!");
         } catch (ParseException e) {
             ConsoleUtils.printException(e, Core.NAME, "Can't parse expire date! PlayerName=" + playerName + ",Date=" + user.getExpDate());
         }
     }
 
     private void checkProbeUser(String playerName, MCUser user) {
 
         try {
             // SELECT 1 FROM mc_pay WHERE minecraft_nick = ? AND totalBreak +
             // totalPlaced >= 10000 AND DATEDIFF(NOW(), probeEndDate) >= 7
             canBeFree.setInt(1, user.getContaoID());
             ResultSet rs = canBeFree.executeQuery();
 
             // Query return nothing or that the user doesn't accomblish the
             // conditions
             if (!(rs.next() && rs.getBoolean(1)))
                 return;
 
             // Check warning status
 
             // Returns an empty resultset if the user has no warnings
            if (this.sManager.getWarnings(playerName) != null && !this.sManager.getWarnings(playerName).isEmpty())
                 return;
 
             // ProbeUser did enough to be a free user
             playerManager.updateGroupManagerGroup(playerName, ContaoGroup.FREE);
             updateContaoGroup(ContaoGroup.FREE, user.getContaoID());
         } catch (Exception e) {
             ConsoleUtils.printException(e, Core.NAME, "Can't check probe user whether he can be a free member! PlayerName=" + playerName);
         }
     }
 
     // GET RELATIVE DAYS
     private Date getRelativeDate(Date thisDate, int days) {
         Calendar cal = Calendar.getInstance();
         cal.setTime(thisDate);
         cal.add(Calendar.DAY_OF_YEAR, days);
         return cal.getTime();
     }
 
     public void initManager(PlayerManager pManager, StatisticManager sManager) {
         this.playerManager = pManager;
         this.sManager = sManager;
     }
 
     /**
      * @return the pManager
      */
     public PlayerManager getpManager() {
         return playerManager;
     }
 
     /**
      * @return the sManager
      */
     public StatisticManager getsManager() {
         return sManager;
     }
 }
