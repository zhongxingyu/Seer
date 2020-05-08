 package com.yanchuanli.games.pokr.model;
 
 import com.google.code.tempusfugit.temporal.Duration;
 import com.yanchuanli.games.pokr.basic.Hand;
 import com.yanchuanli.games.pokr.dao.PlayerDao;
 import com.yanchuanli.games.pokr.util.Config;
 import com.yanchuanli.games.pokr.util.NotificationCenter;
 import com.yanchuanli.games.pokr.util.Util;
 
 import org.apache.log4j.Logger;
 import org.apache.mina.core.session.IoSession;
 
 import java.util.Objects;
 import java.util.Scanner;
 import java.util.Set;
 
 /**
  * Copyright Candou.com
  * Author: Yanchuan Li
  * Email: mail@yanchuanli.com
  * Date: 12-5-31
  */
 
 public class Player {
 
     private static Logger log = Logger.getLogger(Player.class);
 
     private String udid;
     private String name;
     private IoSession session;
     private Hand hand;
     private Hand bestHand;
     private int bestHandRank;
     private boolean online;
     //进这个房间buy in的筹码
     private int moneyInGame;
     private int betThisTime;   //这一次的投注
     private int betThisRound;  //这一轮的投注
     private int betThisGame;   //这次游戏的投注
     private int money;
     private String input;
     private String nameOfBestHand;
     private int exp;
     private int winCount;
     private int loseCount;
     private int historicalBestHandRank;
     private String historicalBestHand;
     private int maxWin;
     private String avatar;
     private int customAvatar;
     private boolean smallBlind;
     private boolean bigBlind;
     private int roomId;
     private int sex;
     private String address;
     private int level;
     private int lastOnlineTime;
     private int elapsedTimeToday;    //当日游戏时间
     private int timeLevelToday;      //当日已经加过经验值的level
     private int seatIndex;    //座位下标, 不持久化
     private String roomName;
     private boolean stopNow;
 
     public Player() {
         hand = new Hand();
         this.bestHandRank = Integer.MIN_VALUE;
         this.online = false;
     }
 
     public Player(String id, String name) {
         this.udid = id;
         this.name = name;
         hand = new Hand();
         this.bestHandRank = Integer.MIN_VALUE;
         this.online = false;
     }
 
     public String getUdid() {
         return udid;
     }
 
     public void setUdid(String udid) {
         this.udid = udid;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public IoSession getSession() {
         return session;
     }
 
     public void setSession(IoSession session) {
         this.session = session;
     }
 
     public Hand getHand() {
         return hand;
     }
 
     public void setHand(Hand hand) {
         this.hand = hand;
     }
 
     public Hand getBestHand() {
         return bestHand;
     }
 
     public void setBestHand(Hand bestHand) {
         this.bestHand = bestHand;
     }
 
     public int getBestHandRank() {
         return bestHandRank;
     }
 
     public void setBestHandRank(int bestHandRank) {
         this.bestHandRank = bestHandRank;
     }
 
     public boolean isOnline() {
         return online;
     }
 
     public void setOnline(boolean online) {
         this.online = online;
     }
 
     public int getMoneyInGame() {
         return moneyInGame;
     }
 
     public void setMoneyInGame(int moneyInGame) {
         this.moneyInGame = moneyInGame;
     }
 
     public int getExp() {
         return exp;
     }
 
     public void setExp(int exp) {
         this.exp = exp;
     }
 
     public int getWinCount() {
         return winCount;
     }
 
     public void setWinCount(int winCount) {
         this.winCount = winCount;
     }
 
     public int getLoseCount() {
         return loseCount;
     }
 
     public void setLoseCount(int loseCount) {
         this.loseCount = loseCount;
     }
 
     public int getHistoricalBestHandRank() {
         return historicalBestHandRank;
     }
 
     public void setHistoricalBestHandRank(int historicalBestHandRank) {
         this.historicalBestHandRank = historicalBestHandRank;
     }
 
     public String getHistoricalBestHand() {
         return historicalBestHand;
     }
 
     public void setHistoricalBestHand(String historicalBestHand) {
         this.historicalBestHand = historicalBestHand;
     }
 
     public int getMaxWin() {
         return maxWin;
     }
 
     public void setMaxWin(int maxWin) {
         this.maxWin = maxWin;
     }
 
     public String getAvatar() {
         return avatar;
     }
 
     public void setAvatar(String avatar) {
         this.avatar = avatar;
     }
 
     public int getSex() {
         return sex;
     }
 
     public void setSex(int sex) {
         this.sex = sex;
     }
 
     public String getAddress() {
         return address;
     }
 
     public void setAddress(String address) {
         this.address = address;
     }
 
     /*
      * mode: 0 - 正常，1 - 小盲，2 - 大盲
      * param blindamount 盲注数量
      *
      */
 
     public Action act(Set<Action> actions, int currentBet, int moneyOnTable, Duration bettingDuration, Duration inactivityCheckInterval, int mode, int blindamount) {
         Action result;
         stopNow = false;
         if (mode == 0) {
             int counter = 0;
             int sleepCount = (int) (bettingDuration.inMillis() / inactivityCheckInterval.inMillis());
 
             String actionStr = "";
 
 
             for (Action action : actions) {
                 actionStr = actionStr + action.getVerb() + "_";
             }
             log.debug("allowed actions for " + getUdid() + ":" + getName() + " :" + actionStr);
             log.info(String.format("%s 允许的操作是 %s", getName(), Util.parseCmdsInGame(actionStr)));
 
             // notify this user for allowed actions
             if (Config.offlineDebug) {
                 Scanner scanner = new Scanner(System.in);
                 input = scanner.nextLine();
             } else {
                 if (actions.size() == 1 && actionStr.equals("con_")) {
                     input = "co";
                 } else {
                     NotificationCenter.act(this.getSession(), this.getUdid() + "," + this.getName() + "," + actionStr + "," + moneyOnTable + "," + currentBet);
                 }
                 while (getInput() == null && counter < sleepCount && isOnline() && !stopNow) {
                     try {
                         Thread.sleep(inactivityCheckInterval.inMillis());
                         counter++;
                         //                    log.debug("waiting for " + name + " ...");
                     } catch (InterruptedException e) {
                         log.error(e);
                     }
                 }
             }
         } else if (mode == 1) {
             input = "sb";
         } else if (mode == 2) {
             input = "bb";
         } else {
             input = "continue";
         }
 
 
         if (input == null) {
             result = Action.FOLD;
         } else {
             log.debug(name + " input:[" + input + "]");
             log.info(String.format("%s 刚刚的操作是(%s)", name, input));
             if (input.startsWith("co")) {
                 setBetThisTime(0);
                 result = Action.CONTINUE;
             } else if (input.startsWith("ca")) {
 
                 int diff = currentBet - betThisRound;
 
                 setBetThisTime(diff);
                 betThisRound += betThisTime;
                 betThisGame += betThisTime;
                 result = Action.CALL;
             } else if (input.startsWith("c")) {
                 result = Action.CHECK;
             } else if (input.startsWith("f")) {
                 setBetThisTime(0);
                 result = Action.FOLD;
             } else if (input.startsWith("r")) {
                 String[] inputs = input.split(":");
                 setBetThisTime(Integer.parseInt(inputs[1]));
                 betThisRound += betThisTime;
                 betThisGame += betThisTime;
                 result = Action.RAISE;
             } else if (input.startsWith("sb")) {
                 setBetThisTime(blindamount);
                 betThisRound += betThisTime;
                 betThisGame += betThisTime;
                 result = Action.SMALL_BLIND;
             } else if (input.startsWith("bb")) {
                 setBetThisTime(blindamount);
                 betThisRound += betThisTime;
                 betThisGame += betThisTime;
                 result = Action.BIG_BLIND;
             } else if (input.startsWith("a")) {
                 setBetThisTime(moneyInGame);
                 betThisRound += betThisTime;
                 betThisGame += betThisTime;
                 result = Action.ALLIN;
             } else {
                 String[] inputs = input.split(":");
                 setBetThisTime(Integer.parseInt(inputs[1]));
                 betThisRound += betThisTime;
                 betThisGame += betThisTime;
                 result = Action.BET;
             }
             moneyInGame -= betThisTime;
             PlayerDao.cashBack(this, -betThisTime);
         }
 
 
         input = null;
         stopNow = false;
         return result;
 
 
     }
 
     public void stopNow() {
         stopNow = true;
         input = "co";
     }
 
     public int getBetThisTime() {
         return betThisTime;
     }
 
     public void setBetThisTime(int betThisTime) {
         this.betThisTime = betThisTime;
     }
 
     public void win(int bet) {
         this.moneyInGame += bet;
     }
 
     public String getInput() {
         return input;
     }
 
     public void setInput(String input) {
         this.input = input;
     }
 
     public String getNameOfBestHand() {
         return nameOfBestHand;
     }
 
     public void setNameOfBestHand(String nameOfBestHand) {
         this.nameOfBestHand = nameOfBestHand;
     }
 
 
     public int getCustomAvatar() {
         return customAvatar;
     }
 
     public void setCustomAvatar(int customAvatar) {
         this.customAvatar = customAvatar;
     }
 
     public int getLevel() {
         return level;
     }
 
     public void setLevel(int level) {
         this.level = level;
     }
 
     @Override
     public String toString() {
         return "Player [udid=" + udid + ", name="
                 + name + ", session=" + session + ", hand=" + hand
                 + ", bestHand=" + bestHand + ", bestHandRank=" + bestHandRank
                 + ", online=" + online + ", moneyInGame=" + moneyInGame + ", betThisTime="
                 + betThisTime + ", betThisRound=" + betThisRound
                 + ", money=" + money + ", input=" + input
                 + ", nameOfBestHand=" + nameOfBestHand + ", exp=" + exp
                 + ", winCount=" + winCount + ", loseCount=" + loseCount
                 + ", historicalBestHandRank=" + historicalBestHandRank
                 + ", historicalBestHand=" + historicalBestHand + ", maxWin="
                 + maxWin + ", avatar=" + avatar + ", customAvatar="
                 + customAvatar + ", smallBlind=" + smallBlind + ", bigBlind="
                 + bigBlind + ", roomId=" + roomId + ", sex=" + sex
                 + ", address=" + address + ", level=" + level
                 + "]";
     }
 
     public void reset() {
         hand.makeEmpty();
         bestHand = null;
         bestHandRank = Integer.MIN_VALUE;
         smallBlind = false;
         bigBlind = false;
         betThisGame = 0;
         betThisRound = 0;
         betThisTime = 0;
 
     }
 
     public boolean isSmallBlind() {
         return smallBlind;
     }
 
     public void setSmallBlind(boolean smallBlind) {
         this.smallBlind = smallBlind;
     }
 
     public boolean isBigBlind() {
         return bigBlind;
     }
 
     public void setBigBlind(boolean bigBlind) {
         this.bigBlind = bigBlind;
     }
 
     public int getRoomId() {
         return roomId;
     }
 
     public void setRoomId(int roomId) {
         this.roomId = roomId;
     }
 
     public boolean inRoom(int roomID) {
         return roomId == roomID;
     }
 
     public int getMoney() {
         return money;
     }
 
     public void setMoney(int money) {
         this.money = money;
     }
 
     public int getBetThisRound() {
         return betThisRound;
     }
 
     public void setBetThisRound(int betThisRound) {
         this.betThisRound = betThisRound;
     }
 
     public void addMoney(int reward) {
         this.moneyInGame += reward;
     }
 
     public int getGIndexesForOwnCardsUsedInBestFive() {
         int result = 0;
         int[] ownCards = hand.getCardArray();
        int firstCard = ownCards[0];
        int secondCard = ownCards[1];
         boolean firstCardIncluded = false;
         boolean secondCardIncluded = false;
         int[] bestHandCardArray = bestHand.getCardArray();
         for (int i = 0; i < bestHandCardArray.length; i++) {
             if (bestHandCardArray[i] == firstCard) {
                 firstCardIncluded = true;
             }
             if (bestHandCardArray[i] == secondCard) {
                 secondCardIncluded = true;
             }
         }
 
         if (firstCardIncluded && secondCardIncluded) {
             result = 3;
         } else if (firstCardIncluded) {
             result = 1;
         } else if (secondCardIncluded) {
             result = 2;
         } else {
             result = 0;
         }
         return result;
     }
 
     public String getIndexesForUsedCommunityCardsInBestFive(int[] communityCards) {
         String result = "";
         int[] bestCardsArray = bestHand.getCardArray();
         for (int i = 0; i < communityCards.length; i++) {
             for (int j = 0; j < bestCardsArray.length; j++) {
                 if (communityCards[i] == bestCardsArray[j]) {
                     result = result + String.valueOf(i) + "_";
                     break;
                 }
             }
         }
         return result;
     }
 
     public int getLastOnlineTime() {
         return lastOnlineTime;
     }
 
     public void setLastOnlineTime(int lastOnlineTime) {
         this.lastOnlineTime = lastOnlineTime;
     }
 
     public void addExp(int amount) {
         this.exp += amount;
     }
 
     public int getElapsedTimeToday() {
         return elapsedTimeToday;
     }
 
     public void setElapsedTimeToday(int elapsedTimeToday) {
         this.elapsedTimeToday = elapsedTimeToday;
     }
 
     public int getTimeLevelToday() {
         return timeLevelToday;
     }
 
     public void setTimeLevelToday(int timeLevelToday) {
         this.timeLevelToday = timeLevelToday;
     }
 
     public int getSeatIndex() {
         return seatIndex;
     }
 
     public void setSeatIndex(int seatIndex) {
         this.seatIndex = seatIndex;
     }
 
     @Override
     public int hashCode() {
         return Objects.hashCode(udid);
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null)
             return false;
         if (obj == this)
             return true;
         if (obj.getClass() != getClass())
             return false;
 
         Player player = (Player) obj;
         return this.getUdid().equals(player.getUdid());
     }
 
     public int getBetThisGame() {
         return betThisGame;
     }
 
     public void setBetThisGame(int betThisGame) {
         this.betThisGame = betThisGame;
     }
 
     public String getRoomName() {
         return roomName;
     }
 
     public void setRoomName(String roomName) {
         this.roomName = roomName;
     }
 }
