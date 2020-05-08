 /*
  * Copyright 2011-2012 Gregory P. Moyer
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.syphr.mythtv.http.backend.impl;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 import java.util.TimeZone;
 
 import javax.xml.ws.BindingProvider;
 
 import org.syphr.mythtv.data.TimeInfo;
 import org.syphr.mythtv.http.backend.MythService;
import org.syphr.mythtv.http.backend.impl._0_25.ArrayOfString;
import org.syphr.mythtv.http.backend.impl._0_25.ConnectionInfo;
import org.syphr.mythtv.http.backend.impl._0_25.LogMessageList;
import org.syphr.mythtv.http.backend.impl._0_25.Myth;
import org.syphr.mythtv.http.backend.impl._0_25.MythServices;
import org.syphr.mythtv.http.backend.impl._0_25.SettingList;
import org.syphr.mythtv.http.backend.impl._0_25.StorageGroupDirList;
import org.syphr.mythtv.http.backend.impl._0_25.TimeZoneInfo;
 import org.syphr.mythtv.http.impl.ServiceUtils;
 
 public class MythService0_25 implements MythService
 {
     private static final String URL_PATH = "/Myth";
 
     private final Myth service;
 
     public MythService0_25(String host, int port)
     {
         MythServices locator = new MythServices();
         service = locator.getBasicHttpBindingMyth();
 
         BindingProvider bindingProvider = (BindingProvider)service;
         bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                                                 "http://" + host + ":" + port + URL_PATH);
     }
 
     @Override
     public boolean addStorageGroupDir(String groupName, String dirName, String hostName)
     {
         return ServiceUtils.toPrimitive(service.addStorageGroupDir(groupName, dirName, hostName));
     }
 
     @Override
     public boolean backupDatabase()
     {
         return ServiceUtils.toPrimitive(service.backupDatabase());
     }
 
     @Override
     public boolean changePassword(String userName, String oldPassword, String newPassword)
     {
         return ServiceUtils.toPrimitive(service.changePassword(userName, oldPassword, newPassword));
     }
 
     @Override
     public boolean checkDatabase(boolean repair)
     {
         return ServiceUtils.toPrimitive(service.checkDatabase(repair));
     }
 
     @Override
     public ConnectionInfo getConnectionInfo(String pin)
     {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public String getHostName()
     {
         return service.getHostName();
     }
 
     @Override
     public List<String> getHosts()
     {
         return toList(service.getHosts());
     }
 
     @Override
     public List<String> getKeys()
     {
         return toList(service.getKeys());
     }
 
     @Override
     public LogMessageList getLogs(String hostName,
                                   String application,
                                   Integer pid,
                                   Integer tid,
                                   String thread,
                                   String filename,
                                   Integer line,
                                   String function,
                                   Calendar fromTime,
                                   Calendar toTime,
                                   String level,
                                   String msgContains)
     {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public SettingList getSetting(String hostName, String key, String _default)
     {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public StorageGroupDirList getStorageGroupDirs(String groupName, String hostName)
     {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public TimeInfo getTimeZone()
     {
         TimeZoneInfo tzInfo = service.getTimeZone();
         if (tzInfo == null)
         {
             return null;
         }
 
         return new TimeInfo(tzInfo.getCurrentDateTime().getValue().getTime(),
                             TimeZone.getTimeZone(tzInfo.getTimeZoneID()));
     }
 
     @Override
     public boolean profileDelete()
     {
         return ServiceUtils.toPrimitive(service.profileDelete());
     }
 
     @Override
     public boolean profileSubmit()
     {
         return ServiceUtils.toPrimitive(service.profileSubmit());
     }
 
     @Override
     public String profileText()
     {
         return service.profileText();
     }
 
     @Override
     public String profileURL()
     {
         return service.profileURL();
     }
 
     @Override
     public String profileUpdated()
     {
         return service.profileUpdated();
     }
 
     @Override
     public boolean putSetting(String hostName, String key, String value)
     {
         return ServiceUtils.toPrimitive(service.putSetting(hostName, key, value));
     }
 
     @Override
     public boolean removeStorageGroupDir(String groupName, String dirName, String hostName)
     {
         return ServiceUtils.toPrimitive(service.removeStorageGroupDir(groupName, dirName, hostName));
     }
 
     @Override
     public boolean sendMessage(String message, String address, Integer udpPort, Integer timeout)
     {
         return ServiceUtils.toPrimitive(service.sendMessage(message, address, udpPort, timeout));
     }
 
     @Override
     public boolean testDBSettings(String hostName,
                                   String userName,
                                   String password,
                                   String dbName,
                                   Integer dbPort)
     {
         return ServiceUtils.toPrimitive(service.testDBSettings(hostName,
                                                                userName,
                                                                password,
                                                                dbName,
                                                                dbPort));
     }
 
     protected List<String> toList(ArrayOfString stringArray)
     {
         List<String> list = new ArrayList<String>();
 
         if (stringArray != null)
         {
             list.addAll(stringArray.getString());
         }
 
         return list;
     }
 }
