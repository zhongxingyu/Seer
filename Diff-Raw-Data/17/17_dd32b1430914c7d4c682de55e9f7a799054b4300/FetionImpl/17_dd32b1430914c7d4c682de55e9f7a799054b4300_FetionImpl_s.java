 /**
  * FetionImpl.java
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
 package com.honnix.jfetion.impl;
 
 import java.util.List;
 
 import com.honnix.jfetion.EventListener;
 import com.honnix.jfetion.FetionAccountControl;
 import com.honnix.jfetion.FetionMessageControl;
 import com.honnix.jfetion.FetionSessionControl;
 import com.honnix.jfetion.impl.data.FetionAccount;
 import com.honnix.jfetion.impl.data.FetionBlacklistItem;
 import com.honnix.jfetion.impl.data.FetionGang;
 import com.honnix.jfetion.impl.data.FetionGroup;
 import com.honnix.jfetion.impl.data.FetionMessage;
 import com.honnix.jfetion.impl.data.FetionPersonalInfo;
 import com.honnix.jfetion.impl.data.FetionProxyInfo;
 import com.honnix.jfetion.impl.data.FetionScheduledSMS;
 
 /**
  * 
  * This class loads JNI library when loaded by certain classloader.
  * <p>
  * All methos are native ones. Refer to native C implementation.
  * 
  */
 public class FetionImpl
     implements FetionSessionControl, FetionAccountControl, FetionMessageControl
 {
 
     static
     {
         System.loadLibrary("jfetion");
     }
 
    public FetionImpl()
     {
         super();
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.honnix.jfetion.FetionAccountControl#asyncAddBuddyById(java.lang
      * .String, java.lang.String, int, java.lang.String,
      * com.honnix.jfetion.EventListener, java.lang.Object[])
      */
     public native int asyncAddBuddyById(String id, String localName,
             int groupId, String description, EventListener eventListener,
             Object... args);
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.honnix.jfetion.FetionAccountControl#asyncAddBuddyByMobileNumber(java
      * .lang.String, java.lang.String, int, java.lang.String,
      * com.honnix.jfetion.EventListener, java.lang.Object[])
      */
     public native int asyncAddBuddyByMobileNumber(String mobileNumber,
             String localName, int groupId, String description,
             EventListener eventListener, Object... args);
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.honnix.jfetion.FetionAccountControl#asyncAddBuddyList(java.lang.String
      * , com.honnix.jfetion.EventListener, java.lang.Object[])
      */
     public native int asyncAddBuddyList(String name,
             EventListener eventListener, Object... args);
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.honnix.jfetion.FetionAccountControl#asyncAddToBlacklistByAccount(
      * com.honnix.jfetion.impl.data.FetionAccount,
      * com.honnix.jfetion.EventListener, java.lang.Object[])
      */
     public native int asyncAddToBlacklistByAccount(FetionAccount account,
             EventListener eventListener, Object... args);
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.honnix.jfetion.FetionAccountControl#asyncAddToBlacklistById(long,
      * com.honnix.jfetion.EventListener, java.lang.Object[])
      */
     public native int asyncAddToBlacklistById(long id,
             EventListener eventListener, Object... args);
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.honnix.jfetion.FetionAccountControl#asyncAddToBlacklistByUri(java
      * .lang.String, com.honnix.jfetion.EventListener, java.lang.Object[])
      */
     public native int asyncAddToBlacklistByUri(String uri,
             EventListener eventListener, Object... args);
 
     /*
      * (non-Javadoc)
      * 
      * @see com.honnix.jfetion.FetionMessageControl#asyncBeginDialog(long,
      * com.honnix.jfetion.EventListener, java.lang.Object[])
      */
     public native int asyncBeginDialog(long who, EventListener eventListener,
             Object... args);
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.honnix.jfetion.FetionAccountControl#asyncDeleteBuddyByAccount(com
      * .honnix.jfetion.impl.data.FetionAccount,
      * com.honnix.jfetion.EventListener, java.lang.Object[])
      */
     public native int asyncDeleteBuddyByAccount(FetionAccount account,
             EventListener eventListener, Object... args);
 
     /*
      * (non-Javadoc)
      * 
      * @see com.honnix.jfetion.FetionAccountControl#asyncDeleteBuddyById(long,
      * com.honnix.jfetion.EventListener, java.lang.Object[])
      */
     public native int asyncDeleteBuddyById(long id,
             EventListener eventListener, Object... args);
 
     /*
      * (non-Javadoc)
      * 
      * @see com.honnix.jfetion.FetionAccountControl#asyncDeleteBuddyList(int,
      * com.honnix.jfetion.EventListener, java.lang.Object[])
      */
     public native int asyncDeleteBuddyList(int id, EventListener eventListener,
             Object... args);
 
     /*
      * (non-Javadoc)
      * 
      * @see com.honnix.jfetion.FetionMessageControl#asyncDeleteScheduledSMS(int,
      * com.honnix.jfetion.EventListener, java.lang.Object[])
      */
     public native int asyncDeleteScheduledSMS(int id,
             EventListener eventListener, Object... args);
 
     /*
      * (non-Javadoc)
      * 
      * @see com.honnix.jfetion.FetionMessageControl#asyncDialogSend(long,
      * java.lang.String, com.honnix.jfetion.EventListener, java.lang.Object[])
      */
     public native int asyncDialogSend(long who, String message,
             EventListener eventListener, Object... args);
 
     /*
      * (non-Javadoc)
      * 
      * @see com.honnix.jfetion.FetionMessageControl#asyncEndDialog(long)
      */
     public native void asyncEndDialog(long who);
 
     /*
      * (non-Javadoc)
      * 
      * @see com.honnix.jfetion.FetionSessionControl#asyncLogin(java.lang.String,
      * java.lang.String, com.honnix.jfetion.EventListener, java.lang.Object[])
      */
     public native int asyncLogin(String id, String password,
             EventListener eventListener, Object... args);
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.honnix.jfetion.FetionAccountControl#asyncMoveGroupBuddyByAccount(
      * com.honnix.jfetion.impl.data.FetionAccount, int,
      * com.honnix.jfetion.EventListener, java.lang.Object[])
      */
     public native int asyncMoveGroupBuddyByAccount(FetionAccount account,
             int groupId, EventListener eventListener, Object... args);
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.honnix.jfetion.FetionAccountControl#asyncMoveGroupBuddyById(long,
      * int, com.honnix.jfetion.EventListener, java.lang.Object[])
      */
     public native int asyncMoveGroupBuddyById(long id, int groupId,
             EventListener eventListener, Object... args);
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.honnix.jfetion.FetionSessionControl#asyncReLogin(com.honnix.jfetion
      * .EventListener, java.lang.Object[])
      */
     public native int asyncReLogin(EventListener eventListener, Object... args);
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.honnix.jfetion.FetionAccountControl#asyncRemoveFromBlacklistByAccount
      * (com.honnix.jfetion.impl.data.FetionAccount,
      * com.honnix.jfetion.EventListener, java.lang.Object[])
      */
     public native int asyncRemoveFromBlacklistByAccount(FetionAccount account,
             EventListener eventListener, Object... args);
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.honnix.jfetion.FetionAccountControl#asyncRemoveFromBlacklistById(
      * long, com.honnix.jfetion.EventListener, java.lang.Object[])
      */
     public native int asyncRemoveFromBlacklistById(long id,
             EventListener eventListener, Object... args);
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.honnix.jfetion.FetionAccountControl#asyncRemoveFromBlacklistByUri
      * (java.lang.String, com.honnix.jfetion.EventListener, java.lang.Object[])
      */
     public native int asyncRemoveFromBlacklistByUri(String uri,
             EventListener eventListener, Object... args);
 
     /*
      * (non-Javadoc)
      * 
      * @see com.honnix.jfetion.FetionAccountControl#asyncRenameBuddyList(int,
      * java.lang.String, com.honnix.jfetion.EventListener, java.lang.Object[])
      */
     public native int asyncRenameBuddyList(int id, String name,
             EventListener eventListener, Object... args);
 
     /*
      * (non-Javadoc)
      * 
      * @see com.honnix.jfetion.FetionMessageControl#asyncSendSms(long,
      * java.lang.String, com.honnix.jfetion.EventListener, java.lang.Object[])
      */
     public native int asyncSendSms(long who, String message,
             EventListener eventListener, Object... args);
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.honnix.jfetion.FetionMessageControl#asyncSendSmsByMobileNumber(java
      * .lang.String, java.lang.String, com.honnix.jfetion.EventListener,
      * java.lang.Object[])
      */
     public native int asyncSendSmsByMobileNumber(String mobileNumber,
             String message, EventListener eventListener, Object... args);
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.honnix.jfetion.FetionMessageControl#asyncSendSmsToSelf(java.lang.
      * String, com.honnix.jfetion.EventListener, java.lang.Object[])
      */
     public native int asyncSendSmsToSelf(String message,
             EventListener eventListener, Object... args);
 
     /*
      * (non-Javadoc)
      * 
      * @see com.honnix.jfetion.FetionAccountControl#asyncSetBuddyInfo(long,
      * java.lang.String, com.honnix.jfetion.EventListener, java.lang.Object[])
      */
     public native int asyncSetBuddyInfo(long id, String localName,
             EventListener eventListener, Object... args);
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.honnix.jfetion.FetionMessageControl#asyncSetScheduledSMS(java.util
      * .List, java.lang.String, java.lang.String,
      * com.honnix.jfetion.EventListener, java.lang.Object[])
      */
     public native int asyncSetScheduledSMS(List<Integer> receiverList,
             String message, String sendTime, EventListener eventListener,
             Object... args);
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.honnix.jfetion.FetionAccountControl#asyncSetUerNickname(java.lang
      * .String, com.honnix.jfetion.EventListener, java.lang.Object[])
      */
     public native int asyncSetUerNickname(String nickname,
             EventListener eventListener, Object... args);
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.honnix.jfetion.FetionAccountControl#asyncSetUserImpresa(java.lang
      * .String, com.honnix.jfetion.EventListener, java.lang.Object[])
      */
     public native int asyncSetUserImpresa(String impresa,
             EventListener eventListener, Object... args);
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.honnix.jfetion.FetionAccountControl#asyncSetUserRefuseSmsDayCount
      * (int, com.honnix.jfetion.EventListener, java.lang.Object[])
      */
     public native int asyncSetUserRefuseSmsDayCount(int dayCount,
             EventListener eventListener, Object... args);
 
     /*
      * (non-Javadoc)
      * 
      * @see com.honnix.jfetion.FetionAccountControl#asyncSetUserStatus(int,
      * java.lang.String, com.honnix.jfetion.EventListener, java.lang.Object[])
      */
     public native int asyncSetUserStatus(int state, String description,
             EventListener eventListener, Object... args);
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.honnix.jfetion.FetionSessionControl#asyncTestNetwork(com.honnix.jfetion
      * .impl.data.FetionProxyInfo, com.honnix.jfetion.EventListener,
      * java.lang.Object[])
      */
     public native int asyncTestNetwork(FetionProxyInfo proxyInfo,
             EventListener eventListener, Object... args);
 
     /*
      * (non-Javadoc)
      * 
      * @see com.honnix.jfetion.FetionMessageControl#beginDialog(long)
      */
     public native boolean beginDialog(long who);
 
     /*
      * (non-Javadoc)
      * 
      * @see com.honnix.jfetion.FetionSessionControl#cancelLogin()
      */
     public native void cancelLogin();
 
     /*
      * (non-Javadoc)
      * 
      * @see com.honnix.jfetion.FetionSessionControl#closeNetwork()
      */
     public native boolean closeNetwork();
 
     /*
      * (non-Javadoc)
      * 
      * @seecom.honnix.jfetion.FetionMessageControl#
      * convertScheduledSMSReceiverListToString (java.util.List)
      */
     public native String convertScheduledSMSReceiverListToString(
             List<Integer> receiverList);
 
     /*
      * (non-Javadoc)
      * 
      * @see com.honnix.jfetion.FetionAccountControl#deleteBuddyList(int)
      */
     public native boolean deleteBuddyList(int id);
 
     /*
      * (non-Javadoc)
      * 
      * @see com.honnix.jfetion.FetionMessageControl#dialogSend(long,
      * java.lang.String)
      */
     public native boolean dialogSend(long who, String message);
 
     /*
      * (non-Javadoc)
      * 
      * @see com.honnix.jfetion.FetionMessageControl#endDialog(long)
      */
     public native void endDialog(long who);
 
     /*
      * (non-Javadoc)
      * 
      * @see com.honnix.jfetion.FetionAccountControl#getAccountById(long)
      */
     public native FetionAccount getAccountById(long id);
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.honnix.jfetion.FetionAccountControl#getAccountGroupId(com.honnix.
      * jfetion.impl.data.FetionAccount)
      */
     public native int getAccountGroupId(FetionAccount account);
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.honnix.jfetion.FetionAccountControl#getAccountShowName(com.honnix
      * .jfetion.impl.data.FetionAccount, boolean)
      */
     public native String getAccountShowName(FetionAccount account,
             boolean needImpresa);
 
     /*
      * (non-Javadoc)
      * 
      * @see com.honnix.jfetion.FetionAccountControl#getBlacklist()
      */
     public native List<FetionBlacklistItem> getBlacklist();
 
     /*
      * (non-Javadoc)
      * 
      * @see com.honnix.jfetion.FetionSessionControl#getExpireTime()
      */
     public native int getExpireTime();
 
     /*
      * (non-Javadoc)
      * 
      * @see com.honnix.jfetion.FetionAccountControl#getFirstAccount()
      */
     public native FetionAccount getFirstAccount();
 
     /*
      * (non-Javadoc)
      * 
      * @see com.honnix.jfetion.FetionAccountControl#getGangById(long)
      */
     public native FetionGang getGangById(long id);
 
     /*
      * (non-Javadoc)
      * 
      * @see com.honnix.jfetion.FetionAccountControl#getGangList()
      */
     public native List<FetionGang> getGangList();
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.honnix.jfetion.FetionAccountControl#getGangShowName(com.honnix.jfetion
      * .impl.data.FetionGang)
      */
     public native String getGangShowName(FetionGang gang);
 
     /*
      * (non-Javadoc)
      * 
      * @see com.honnix.jfetion.FetionAccountControl#getGroupList()
      */
     public native List<FetionGroup> getGroupList();
 
     /*
      * (non-Javadoc)
      * 
      * @see com.honnix.jfetion.FetionMessageControl#getMessage(long)
      */
     public native FetionMessage getMessage(long id);
 
     /*
      * (non-Javadoc)
      * 
     * @see
     * com.honnix.jfetion.FetionAccountControl#getNextAccount(com.honnix.jfetion
     * .impl.data.FetionAccount)
      */
    public native FetionAccount getNextAccount(FetionAccount currentAccount);
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.honnix.jfetion.FetionAccountControl#getOnlineStatusByAccount(com.
      * honnix.jfetion.impl.data.FetionAccount)
      */
     public native int getOnlineStatusByAccount(FetionAccount account);
 
     /*
      * (non-Javadoc)
      * 
      * @see com.honnix.jfetion.FetionAccountControl#getOnlineStatusById(long)
      */
     public native int getOnlineStatusById(long id);
 
     public String getOriginalId()
     {
         // TODO Auto-generated method stub
         return null;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.honnix.jfetion.FetionAccountControl#getOriginalId()
      */
     public native String getOriginalId(long id);
 
     /*
      * (non-Javadoc)
      * 
      * @see com.honnix.jfetion.FetionAccountControl#getPersonalInfo()
      */
     public native FetionPersonalInfo getPersonalInfo();
 
     /*
      * (non-Javadoc)
      * 
      * @see com.honnix.jfetion.FetionSessionControl#getProxy()
      */
     public native FetionProxyInfo getProxy();
 
     /*
      * (non-Javadoc)
      * 
      * @see com.honnix.jfetion.FetionMessageControl#getScheduledSMSById(int)
      */
     public native FetionScheduledSMS getScheduledSMSById(int id);
 
     /*
      * (non-Javadoc)
      * 
      * @see com.honnix.jfetion.FetionMessageControl#getScheduledSMSList()
      */
     public native List<FetionScheduledSMS> getScheduledSMSList();
 
     /*
      * (non-Javadoc)
      * 
      * @see com.honnix.jfetion.FetionSessionControl#getServerAddress()
      */
     public native String getServerAddress();
 
     /*
      * (non-Javadoc)
      * 
      * @see com.honnix.jfetion.FetionAccountControl#getUserId()
      */
     public native String getUserId();
 
     /*
      * (non-Javadoc)
      * 
      * @see com.honnix.jfetion.FetionAccountControl#getUserMobileNumber()
      */
     public native String getUserMobileNumber();
 
     /*
      * (non-Javadoc)
      * 
      * @see com.honnix.jfetion.FetionAccountControl#getUserPassword()
      */
     public native String getUserPassword();
 
     /*
      * (non-Javadoc)
      * 
      * @see com.honnix.jfetion.FetionAccountControl#getUserRefuseSmsDayCount()
      */
     public native int getUserRefuseSmsDayCount();
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.honnix.jfetion.FetionAccountControl#getUserRefuseSmsDayCountByAccount
      * (com.honnix.jfetion.impl.data.FetionAccount)
      */
     public native int getUserRefuseSmsDayCountByAccount(FetionAccount account);
 
     /*
      * (non-Javadoc)
      * 
      * @see com.honnix.jfetion.FetionAccountControl#getUserScore()
      */
     public native int getUserScore();
 
     /*
      * (non-Javadoc)
      * 
      * @see com.honnix.jfetion.FetionAccountControl#getUserShowName()
      */
     public native String getUserShowName();
 
     /*
      * (non-Javadoc)
      * 
      * @see com.honnix.jfetion.FetionAccountControl#getUserStatus()
      */
     public native int getUserStatus();
 
     /*
      * (non-Javadoc)
      * 
      * @see com.honnix.jfetion.FetionAccountControl#getUserUri(long)
      */
     public native String getUserUri();
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.honnix.jfetion.FetionAccountControl#handleContactRequest(java.lang
      * .String, int, int, java.lang.String)
      */
     public native int handleContactRequest(String uri, int isPassed,
             int groupId, String localName);
 
     /*
      * (non-Javadoc)
      * 
      * @see com.honnix.jfetion.Fetion#init()
      */
     public native boolean init();
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.honnix.jfetion.FetionAccountControl#isAuthorizedByAccount(com.honnix
      * .jfetion.impl.data.FetionAccount)
      */
     public native int isAuthorizedByAccount(FetionAccount account);
 
     /*
      * (non-Javadoc)
      * 
      * @see com.honnix.jfetion.FetionAccountControl#isAuthorizedById(long)
      */
     public native int isAuthorizedById(long id);
 
     /*
      * (non-Javadoc)
      * 
      * @see com.honnix.jfetion.FetionAccountControl#isGangById(long)
      */
     public native boolean isGangById(long id);
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.honnix.jfetion.FetionAccountControl#isInBlacklistByAccount(com.honnix
      * .jfetion.impl.data.FetionAccount)
      */
     public native boolean isInBlacklistByAccount(FetionAccount account);
 
     /*
      * (non-Javadoc)
      * 
      * @see com.honnix.jfetion.FetionAccountControl#isInBlacklistById(long)
      */
     public native boolean isInBlacklistById(long id);
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.honnix.jfetion.FetionAccountControl#isLoginByMobile(com.honnix.jfetion
      * .impl.data.FetionAccount)
      */
     public native boolean isLoginByMobile(FetionAccount account);
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.honnix.jfetion.FetionAccountControl#isOnlineByAccount(com.honnix.
      * jfetion.impl.data.FetionAccount)
      */
     public native boolean isOnlineByAccount(FetionAccount account);
 
     /*
      * (non-Javadoc)
      * 
      * @see com.honnix.jfetion.FetionAccountControl#isOnlineById(long)
      */
     public native boolean isOnlineById(long id);
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.honnix.jfetion.FetionAccountControl#isPCUserByAccount(com.honnix.
      * jfetion.impl.data.FetionAccount)
      */
     public native boolean isPCUserByAccount(FetionAccount account);
 
     /*
      * (non-Javadoc)
      * 
      * @see com.honnix.jfetion.FetionAccountControl#isPCUserById(long)
      */
     public native boolean isPCUserById(long id);
 
     /*
      * (non-Javadoc)
      * 
      * @see com.honnix.jfetion.FetionSessionControl#isProxyEnabled()
      */
     public native boolean isProxyEnabled();
 
     /*
      * (non-Javadoc)
      * 
      * @see com.honnix.jfetion.Fetion#login(java.lang.String, java.lang.String)
      */
     public native boolean login(String uid, String password);
 
     /*
      * (non-Javadoc)
      * 
      * @see com.honnix.jfetion.Fetion#logout()
      */
     public native void logout();
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.honnix.jfetion.FetionMessageControl#removeFontTag(java.lang.String)
      */
     public native String removeFontTag(String message);
 
     /*
      * (non-Javadoc)
      * 
      * @see com.honnix.jfetion.FetionAccountControl#renameBuddyList(int,
      * java.lang.String)
      */
     public native boolean renameBuddyList(int id, String name);
 
     /*
      * (non-Javadoc)
      * 
      * @see com.honnix.jfetion.FetionMessageControl#sendNudge(long)
      */
     public native boolean sendNudge(long who);
 
     /*
      * (non-Javadoc)
      * 
      * @see com.honnix.jfetion.FetionMessageControl#sendSms(long,
      * java.lang.String)
      */
     public native boolean sendSms(long who, String message);
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.honnix.jfetion.FetionMessageControl#sendSmsByMobileNumber(java.lang
      * .String, java.lang.String)
      */
     public native boolean sendSmsByMobileNumber(String mobileNumber,
             String message);
 
     /*
      * (non-Javadoc)
      * 
      * @see com.honnix.jfetion.Fetion#sendSmsToSelf(java.lang.String)
      */
     public native boolean sendSmsToSelf(String message);
 
     /*
      * (non-Javadoc)
      * 
      * @see com.honnix.jfetion.FetionAccountControl#setBuddyInfo(long,
      * java.lang.String)
      */
     public native boolean setBuddyInfo(long id, String localName);
 
     /*
      * (non-Javadoc)
      * 
      * @see com.honnix.jfetion.FetionSessionControl#setLoginStatus(int)
      */
     public native void setLoginStatus(int status);
 
     /*
      * (non-Javadoc)
      * 
      * @see com.honnix.jfetion.FetionMessageControl#setLongSmsEnabled(boolean)
      */
     public native void setLongSmsEnabled(boolean enabled);
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.honnix.jfetion.FetionSessionControl#setProxy(com.honnix.jfetion.impl
      * .data.FetionProxyInfo)
      */
     public native boolean setProxy(FetionProxyInfo proxyInfo);
 
     /*
      * (non-Javadoc)
      * 
      * @see com.honnix.jfetion.FetionSessionControl#setProxyEnabled(boolean)
      */
     public native void setProxyEnabled(boolean enabled);
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.honnix.jfetion.FetionSessionControl#setServerAddress(java.lang.String
      * )
      */
     public native boolean setServerAddress(String address);
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.honnix.jfetion.FetionMessageControl#setSystemMessageEventListener
      * (com.honnix.jfetion.EventListener, java.lang.Object[])
      */
     public native void setSystemMessageEventListener(
             EventListener eventListener, Object... args);
 
     /*
      * (non-Javadoc)
      * 
      * @see com.honnix.jfetion.FetionSessionControl#setUnknownServerAddress()
      */
     public native void setUnknownServerAddress();
 
     /*
      * (non-Javadoc)
      * 
      * @see com.honnix.jfetion.Fetion#terminate()
      */
     public native void terminate();
 
     /*
      * (non-Javadoc)
      * 
      * @see com.honnix.jfetion.FetionAccountControl#updateAccountInfoById(long)
      */
     public native void updateAccountInfoById(long id);
 
 }
