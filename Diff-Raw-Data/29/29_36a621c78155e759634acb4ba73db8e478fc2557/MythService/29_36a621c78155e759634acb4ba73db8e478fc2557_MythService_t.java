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
 package org.syphr.mythtv.http.backend;
 
 import java.util.Calendar;
 import java.util.List;
 
 import org.syphr.mythtv.data.TimeInfo;
import org.syphr.mythtv.http.backend.impl._0_25.myth.ConnectionInfo;
import org.syphr.mythtv.http.backend.impl._0_25.myth.LogMessageList;
import org.syphr.mythtv.http.backend.impl._0_25.myth.SettingList;
import org.syphr.mythtv.http.backend.impl._0_25.myth.StorageGroupDirList;
 
 public interface MythService
 {
     public boolean addStorageGroupDir(String groupName, String dirName, String hostName);
 
     public boolean backupDatabase();
 
     public boolean changePassword(String userName, String oldPassword, String newPassword);
 
     public boolean checkDatabase(boolean repair);
 
     public ConnectionInfo getConnectionInfo(String pin);
 
     public String getHostName();
 
     public List<String> getHosts();
 
     public List<String> getKeys();
 
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
                                   String msgContains);
 
     public SettingList getSetting(String hostName, String key, String _default);
 
     public StorageGroupDirList getStorageGroupDirs(String groupName, String hostName);
 
     public TimeInfo getTimeZone();
 
     public boolean profileDelete();
 
     public boolean profileSubmit();
 
     public String profileText();
 
     public String profileURL();
 
     public String profileUpdated();
 
     public boolean putSetting(String hostName, String key, String value);
 
     public boolean removeStorageGroupDir(String groupName, String dirName, String hostName);
 
     public boolean sendMessage(String message, String address, Integer udpPort, Integer timeout);
 
     public boolean testDBSettings(String hostName,
                                   String userName,
                                   String password,
                                   String dbName,
                                   Integer dbPort);
 }
