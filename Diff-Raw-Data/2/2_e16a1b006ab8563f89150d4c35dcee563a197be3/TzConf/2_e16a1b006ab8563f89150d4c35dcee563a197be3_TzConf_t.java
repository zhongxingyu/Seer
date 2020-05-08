 /* ********************************************************************
     Licensed to Jasig under one or more contributor license
     agreements. See the NOTICE file distributed with this work
     for additional information regarding copyright ownership.
     Jasig licenses this file to you under the Apache License,
     Version 2.0 (the "License"); you may not use this file
     except in compliance with the License. You may obtain a
     copy of the License at:
 
     http://www.apache.org/licenses/LICENSE-2.0
 
     Unless required by applicable law or agreed to in writing,
     software distributed under the License is distributed on
     an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
     KIND, either express or implied. See the License for the
     specific language governing permissions and limitations
     under the License.
  */
 package org.bedework.timezones.service;
 
 import org.bedework.timezones.common.Stat;
 import org.bedework.timezones.common.TzConfig;
 import org.bedework.timezones.common.TzServerUtil;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.List;
 
 import edu.rpi.cmt.jmx.ConfBase;
 import edu.rpi.cmt.jmx.ConfigHolder;
 
 /**
  * @author douglm
  *
  */
 public class TzConf extends ConfBase<TzConfig> implements TzConfMBean, ConfigHolder<TzConfig> {
   /* Name of the property holding the location of the config data */
   private static final String confuriPname = "org.bedework.tzs.confuri";
 
   /**
    */
   public TzConf() {
     super("org.bedework.timezones:service=Server");
 
     setConfigPname(confuriPname);
 
     TzServerUtil.setTzConfigHolder(this);
   }
 
   /* ========================================================================
    * Attributes
    * ======================================================================== */
 
   /** Tzdata url
    *
    * @param val
    */
   @Override
   public void setTzdataUrl(final String val) {
     getConfig().setTzdataUrl(val);
   }
 
   @Override
   public String getTzdataUrl() {
     return getConfig().getTzdataUrl();
   }
 
   @Override
   public void setLeveldbPath(final String val) {
     getConfig().setLeveldbPath(val);
   }
 
   @Override
   public String getLeveldbPath() {
     return getConfig().getLeveldbPath();
   }
 
   @Override
   public void setPrimaryUrl(final String val) {
     getConfig().setPrimaryUrl(val);
   }
 
   @Override
   public String getPrimaryUrl() {
     return getConfig().getPrimaryUrl();
   }
 
   @Override
   public void setPrimaryServer(final boolean val) {
     getConfig().setPrimaryServer(val);
   }
 
   @Override
   public boolean getPrimaryServer() {
     return getConfig().getPrimaryServer();
   }
 
   @Override
   public void setRefreshInterval(final long val) {
     getConfig().setRefreshDelay(val);
   }
 
   @Override
   public long getRefreshInterval() {
     return getConfig().getRefreshDelay();
   }
 
   /* ========================================================================
    * Operations
    * ======================================================================== */
 
   @Override
   public List<Stat> getStats() {
     try {
       return TzServerUtil.getStats();
     } catch (Throwable t) {
       error("Error getting stats");
       error(t);
       return null;
     }
   }
 
   @Override
   public String refreshData() {
     try {
       getConfig().setDtstamp(null);
       saveConfig();
       TzServerUtil.fireRefresh();
       return "Ok";
     } catch (Throwable t) {
       error(t);
       return "Refresh error: " + t.getLocalizedMessage();
     }
   }
 
   @Override
   public String checkData() {
     try {
       TzServerUtil.fireCheck();
       return "Ok";
     } catch (Throwable t) {
       error(t);
       return "Update error: " + t.getLocalizedMessage();
     }
   }
 
   @Override
   public String compareData(final String tzdataUrl) {
     StringWriter sw = new StringWriter();
 
     try {
       PrintWriter pw = new PrintWriter(sw);
 
       List<String> chgs = TzServerUtil.compareData(tzdataUrl);
 
       for (String s: chgs) {
         pw.println(s);
       }
 
     } catch (Throwable t) {
       t.printStackTrace(new PrintWriter(sw));
     }
 
     return sw.toString();
   }
 
   @Override
   public String updateData(final String tzdataUrl) {
     StringWriter sw = new StringWriter();
 
     try {
       PrintWriter pw = new PrintWriter(sw);
 
       List<String> chgs = TzServerUtil.updateData(tzdataUrl);
 
       for (String s: chgs) {
         pw.println(s);
       }
 
     } catch (Throwable t) {
       t.printStackTrace(new PrintWriter(sw));
     }
 
     return sw.toString();
   }
 
   @Override
   public String loadConfig() {
    return loadOnlyConfig(TzConfig.class);
   }
 
   /** Save the configuration.
    *
    */
   @Override
   public void putConfig() {
     saveConfig();
   }
 
   /* ====================================================================
    *                   Private methods
    * ==================================================================== */
 
   /* ========================================================================
    * Lifecycle
    * ======================================================================== */
 }
