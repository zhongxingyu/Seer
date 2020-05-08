 /*
  * Copyright 2008 Pentaho Corporation.  All rights reserved. 
  * This software was developed by Pentaho Corporation and is provided under the terms 
  * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
  * this file except in compliance with the license. If you need a copy of the license, 
  * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
  * BI Platform.  The Initial Developer is Pentaho Corporation.
  *
  * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
  * the license for the specific language governing your rights and limitations.
  *
  * Created  
  * @author David Kincade
  */
 package org.pentaho.experimental.chart;
 
 import org.pentaho.reporting.libraries.base.boot.AbstractBoot;
 import org.pentaho.reporting.libraries.base.config.Configuration;
 import org.pentaho.reporting.libraries.base.versioning.ProjectInformation;
 
 @SuppressWarnings({"StaticNonFinalField"})
 public class ChartBoot extends AbstractBoot {
   private static ChartBoot instance=null;
 
   public static synchronized ChartBoot getInstance() {
     if (ChartBoot.instance == null) {
       ChartBoot.instance = new ChartBoot();
     }
     return ChartBoot.instance;
   }
 
   private ChartBoot() {
   }
 
   @SuppressWarnings("nls")
   public Configuration loadConfiguration() {
     return createDefaultHierarchicalConfiguration(
         "/org/pentaho/experimental/chart/chart.properties", // Hard coded default values that shoulnd't be changed by user
         "/chart.properties", // User changable values (these override above)
         true,
         ChartBoot.class);
   }
 
   protected void performBoot() {
     // nothing required. Just gather the configuration.
   }
 
   protected ProjectInformation getProjectInfo() {
    return ChartInfo.INSTANCE;
   }
 }
