 /**
  * FetionAccountControl.java
  * 
  * Copyright : (C) 2008 by Honnix
  * Email     : hxliang1982@gmail.com
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 package com.honnix.jfetion;
 
 import java.util.List;
 
 import com.honnix.jfetion.impl.data.FetionAccount;
 import com.honnix.jfetion.impl.data.FetionBlacklistItem;
 import com.honnix.jfetion.impl.data.FetionGang;
 import com.honnix.jfetion.impl.data.FetionGroup;
 import com.honnix.jfetion.impl.data.FetionPersonalInfo;
 
 /**
  * 
  * This interface declares all account related operations.
  * 
  */
 public interface FetionAccountControl
 {
 
     int asyncAddBuddyById(String id, String localName, int groupId,
             String description, EventListener eventListener, Object... args);
 
     int asyncAddBuddyByMobileNumber(String mobileNumber, String localName,
             int groupId, String description, EventListener eventListener,
             Object... args);
 
     int asyncAddBuddyList(String name, EventListener eventListener,
             Object... args);
 
     int asyncAddToBlacklistByAccount(FetionAccount account,
             EventListener eventListener, Object... args);
 
     int asyncAddToBlacklistById(long id, EventListener eventListener,
             Object... args);
 
     int asyncAddToBlacklistByUri(String uri, EventListener eventListener,
             Object... args);
 
     int asyncDeleteBuddyByAccount(FetionAccount account,
             EventListener eventListener, Object... args);
 
     int asyncDeleteBuddyById(long id, EventListener eventListener,
             Object... args);
 
     int asyncDeleteBuddyList(int id, EventListener eventListener,
             Object... args);
 
     int asyncMoveGroupBuddyByAccount(FetionAccount account, int groupId,
             EventListener eventListener, Object... args);
 
     int asyncMoveGroupBuddyById(long id, int groupId,
             EventListener eventListener, Object... args);
 
     int asyncRemoveFromBlacklistByAccount(FetionAccount account,
             EventListener eventListener, Object... args);
 
     int asyncRemoveFromBlacklistById(long id, EventListener eventListener,
             Object... args);
 
     int asyncRemoveFromBlacklistByUri(String uri, EventListener eventListener,
             Object... args);
 
     int asyncRenameBuddyList(int id, String name, EventListener eventListener,
             Object... args);
 
     int asyncSetBuddyInfo(long id, String localName,
             EventListener eventListener, Object... args);
 
     int asyncSetUerNickname(String nickname, EventListener eventListener,
             Object... args);
 
     int asyncSetUserImpresa(String impresa, EventListener eventListener,
             Object... args);
 
     int asyncSetUserRefuseSmsDayCount(int dayCount,
             EventListener eventListener, Object... args);
 
     int asyncSetUserStatus(int status, String description,
             EventListener eventListener, Object... args);
 
     boolean deleteBuddyList(int id);
 
     FetionAccount getAccountById(long id);
 
     int getAccountGroupId(FetionAccount account);
 
     String getAccountShowName(FetionAccount account, boolean needImpresa);
 
     List<FetionBlacklistItem> getBlacklist();
 
     FetionAccount getFirstAccount();
 
     FetionGang getGangById(long id);
 
     List<FetionGang> getGangList();
 
     String getGangShowName(FetionGang gang);
 
     List<FetionGroup> getGroupList();
 
    FetionAccount getNextAccount(FetionAccount currentAccount);
 
     int getOnlineStatusByAccount(FetionAccount account);
 
     int getOnlineStatusById(long id);
 
     String getOriginalId(long id);
 
     FetionPersonalInfo getPersonalInfo();
 
     String getUserId();
 
     String getUserMobileNumber();
 
     String getUserPassword();
 
     int getUserRefuseSmsDayCount();
 
     int getUserRefuseSmsDayCountByAccount(FetionAccount account);
 
     int getUserScore();
 
     String getUserShowName();
 
     int getUserStatus();
 
     String getUserUri();
 
     int handleContactRequest(String uri, int isPassed, int groupId,
             String localName);
 
     int isAuthorizedByAccount(FetionAccount account);
 
     int isAuthorizedById(long id);
 
     boolean isGangById(long id);
 
     boolean isInBlacklistByAccount(FetionAccount account);
 
     boolean isInBlacklistById(long id);
 
     boolean isLoginByMobile(FetionAccount account);
 
     boolean isOnlineByAccount(FetionAccount account);
 
     boolean isOnlineById(long id);
 
     boolean isPCUserByAccount(FetionAccount account);
 
     boolean isPCUserById(long id);
 
     boolean renameBuddyList(int id, String name);
 
     boolean setBuddyInfo(long id, String localName);
 
     void updateAccountInfoById(long id);
 
 }
