 /*
  * This file is part of MySalary.
  *
  * Copyright © 2011-2013 Visual Illusions Entertainment
  *
  * MySalary is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License,
  * or (at your option) any later version.
  *
  * MySalary is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
  * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along with MySalary.
  * If not, see http://www.gnu.org/licenses/gpl.html.
  */
 package net.visualillusionsent.mysalary;
 
 import net.visualillusionsent.utils.BooleanUtils;
 import net.visualillusionsent.utils.FileUtils;
 import net.visualillusionsent.utils.PropertiesFile;
 
 import java.io.File;
 import java.io.IOException;
 
 import static java.util.concurrent.TimeUnit.MINUTES;
 
 /**
  * MySalary Configuration container
  *
  * @author Jason (darkdiplomat)
  */
 public final class MySalaryConfiguration {
     private PropertiesFile myscfg;
 
     public MySalaryConfiguration(MySalary mys) throws IOException {
         loadCfg(mys);
     }
 
     private final void loadCfg(MySalary mys) throws IOException {
         File cfg = new File("config/MySalary/MySalary.cfg");
         if (!cfg.exists()) {
             if (!new File("config/MySalary/").mkdirs()) {
                 throw new IOException("Failed to create directories for MySalary.cfg");
             }
             FileUtils.cloneFileFromJar(mys.getJarPath(), "default_config.cfg", "config/MySalary/MySalary.cfg");
         }
         myscfg = new PropertiesFile("config/MySalary/MySalary.cfg");
 
         myscfg.getLong("delay", 120);
         myscfg.getDouble("pay.amount", 5.95);
         myscfg.getBoolean("require.claim", false);
         myscfg.getBoolean("group.specific.pay", false);
         myscfg.getBoolean("accumulate.checks", false);
         myscfg.getBoolean("pay.locked", false);
         myscfg.getBoolean("pay.server", false);
         myscfg.save(); // Saving will only occur if something was actually changed
     }
 
     public final long getDelay() {
         return MINUTES.toMillis(myscfg.getLong("delay")); // delay is stored in minutes but needs to be in milliseconds
     }
 
     public final double getDefaultPayAmount() {
         return myscfg.getDouble("pay.amount");
     }
 
     public boolean isRequireClaimEnabled() {
         return myscfg.getBoolean("require.claim");
     }
 
     public boolean isGroupSpecificEnabled() {
         return myscfg.getBoolean("group.specific.pay");
     }
 
     public boolean isAccumulateChecksEnabled() {
         return myscfg.getBoolean("accumulate.checks");
     }
 
     public boolean payIfLocked() {
         return myscfg.getBoolean("pay.locked");
     }
 
     public boolean payServer() {
         return myscfg.getBoolean("pay.server");
     }
 
     public double getGroupPay(String group_name) {
         if (myscfg.containsKey(group_name)) {
             return myscfg.getDouble(group_name);
         }
         return -1;
     }
 
     public final void setProperty(String key, String value) throws IllegalArgumentException {
         if (key.equals("delay")) {
             try {
                 myscfg.setLong("delay", Long.valueOf(value));
             }
             catch (NumberFormatException nfex) {
                 throw new IllegalArgumentException("Value is not of a type compatible with the key. (Expected: long [number])");
             }
         }
        else if (key.matches("[require.claim|group.specific.pay|accumulate.checks|pay.locked|pay.server]")) {
             myscfg.setBoolean(key, BooleanUtils.parseBoolean(value));
         }
         else {
             try {
                 myscfg.setDouble(key, Double.valueOf(value));
             }
             catch (NumberFormatException nfex) {
                 throw new IllegalArgumentException("Value is not of a type compatible with the key. (Expected: double [number])");
             }
         }
         myscfg.save();
     }
 }
