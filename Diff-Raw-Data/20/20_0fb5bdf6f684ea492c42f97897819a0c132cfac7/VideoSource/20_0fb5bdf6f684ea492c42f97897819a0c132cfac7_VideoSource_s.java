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
 package org.syphr.mythtv.data;
 
 public class VideoSource
 {
    protected long id;
    protected String sourceName;
    protected String grabber;
    protected String userId;
    protected String freqTable;
    protected String lineupId;
    protected String password;
    protected boolean useEIT;
    protected String configPath;
    protected Integer nitId;
 
     public long getId()
     {
         return id;
     }
 
     public void setId(long id)
     {
         this.id = id;
     }
 
     public String getSourceName()
     {
         return sourceName;
     }
 
     public void setSourceName(String sourceName)
     {
         this.sourceName = sourceName;
     }
 
     public String getGrabber()
     {
         return grabber;
     }
 
     public void setGrabber(String grabber)
     {
         this.grabber = grabber;
     }
 
     public String getUserId()
     {
         return userId;
     }
 
     public void setUserId(String userId)
     {
         this.userId = userId;
     }
 
     public String getFreqTable()
     {
         return freqTable;
     }
 
     public void setFreqTable(String freqTable)
     {
         this.freqTable = freqTable;
     }
 
     public String getLineupId()
     {
         return lineupId;
     }
 
     public void setLineupId(String lineupId)
     {
         this.lineupId = lineupId;
     }
 
     public String getPassword()
     {
         return password;
     }
 
     public void setPassword(String password)
     {
         this.password = password;
     }
 
     public boolean isUseEIT()
     {
         return useEIT;
     }
 
     public void setUseEIT(boolean useEIT)
     {
         this.useEIT = useEIT;
     }
 
     public String getConfigPath()
     {
         return configPath;
     }
 
     public void setConfigPath(String configPath)
     {
         this.configPath = configPath;
     }
 
     public Integer getNitId()
     {
         return nitId;
     }
 
     public void setNitId(Integer nitId)
     {
         this.nitId = nitId;
     }
 
     @Override
     public String toString()
     {
         StringBuilder builder = new StringBuilder();
         builder.append("VideoSource [id=");
         builder.append(id);
         builder.append(", sourceName=");
         builder.append(sourceName);
         builder.append(", grabber=");
         builder.append(grabber);
         builder.append(", userId=");
         builder.append(userId);
         builder.append(", freqTable=");
         builder.append(freqTable);
         builder.append(", lineupId=");
         builder.append(lineupId);
         builder.append(", password=");
         builder.append(password);
         builder.append(", useEIT=");
         builder.append(useEIT);
         builder.append(", configPath=");
         builder.append(configPath);
         builder.append(", nitId=");
         builder.append(nitId);
         builder.append("]");
         return builder.toString();
     }
 
     @Override
     public int hashCode()
     {
         final int prime = 31;
         int result = 1;
         result = prime * result + (int)(id ^ (id >>> 32));
         return result;
     }
 
     @Override
     public boolean equals(Object obj)
     {
         if (this == obj)
         {
             return true;
         }
         if (obj == null)
         {
             return false;
         }
         if (!(obj instanceof VideoSource))
         {
             return false;
         }
         VideoSource other = (VideoSource)obj;
         if (id != other.id)
         {
             return false;
         }
         return true;
     }
 }
