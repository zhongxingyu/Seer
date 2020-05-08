 /*
  * This file is part of dBankLite.
  *
  * Copyright © 2013 Visual Illusions Entertainment
  *
  * dBankLite is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License,
  * or (at your option) any later version.
  *
  * dBankLite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
  * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along with dBankLite.
  * If not, see http://www.gnu.org/licenses/gpl.html.
  */
 package net.visualillusionsent.dconomy.addon.bank;
 
 import net.visualillusionsent.dconomy.MessageTranslator;
 import net.visualillusionsent.dconomy.dCoBase;
 import net.visualillusionsent.dconomy.io.logging.dCoLevel;
 import net.visualillusionsent.utils.PropertiesFile;
 import net.visualillusionsent.utils.UtilityException;
 
 import java.util.Timer;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public final class dBankLiteBase {
 
     private final float dCoVersion = 3.0F;
     private final Logger logger;
     private final Timer timer;
 
     private static dBankLiteBase $;
 
     public dBankLiteBase(dBankLite dbanklite) {
         $ = this;
         this.logger = dbanklite.getPluginLogger();
         if (dCoBase.getVersion() > dCoVersion) {
            warning("dConomy appears to be a newer version. Incompatibility could result.");
         }
         testdBankLiteProps();
         dbanklite.check();
         installBankMessages();
         if (getInterestInterval() > 0) { // interest enabled?
             timer = new Timer();
             timer.scheduleAtFixedRate(new InterestPayer(this), getInitialStart(), getInterestInterval());
         } else {
             timer = null;
         }
     }
 
     private final void testdBankLiteProps() {
         PropertiesFile dCoProps = dCoBase.getProperties().getPropertiesFile();
         if (!dCoProps.containsKey("interest.rate")) {
             dCoProps.setFloat("interest.rate", 2.0F, "dBankLite: Interest Rate (in percentage) (Default: 2%)");
         }
         if (!dCoProps.containsKey("interest.pay.interval")) {
             dCoProps.setInt("interest.pay.interval", 360, "dBankLite: Interest Pay Interval (in minutes) (Default: 360 [6 Hours]) Set to 0 or less to disable");
         }
         if (!dCoProps.containsKey("interest.max.payout")) {
             dCoProps.setInt("interest.max.payout", 10000, "dBankLite: Max Interest Payout (Default: 10000)");
         }
         if (!dCoProps.containsKey("sql.bank.table")) {
             dCoProps.setString("sql.bank.table", "dBankLite", "dBankLite: SQL Bank table");
         }
     }
 
     public final static void info(String msg) {
         $.logger.info(msg);
     }
 
     public final static void info(String msg, Throwable thrown) {
         $.logger.log(Level.INFO, msg, thrown);
     }
 
     public final static void warning(String msg) {
         $.logger.warning(msg);
     }
 
     public final static void warning(String msg, Throwable thrown) {
         $.logger.log(Level.WARNING, msg, thrown);
     }
 
     public final static void severe(String msg) {
         $.logger.severe(msg);
     }
 
     public final static void severe(String msg, Throwable thrown) {
         $.logger.log(Level.SEVERE, msg, thrown);
     }
 
     public final static void stacktrace(Throwable thrown) {
         if (dCoBase.getProperties().getBooleanValue("debug.enabled")) {
             $.logger.log(dCoLevel.STACKTRACE, "Stacktrace: ", thrown);
         }
     }
 
     public final static void debug(String msg) {
         if (dCoBase.getProperties().getBooleanValue("debug.enabled")) {
             $.logger.log(dCoLevel.GENERAL, msg);
         }
     }
 
     public final static void cleanUp() {
         if ($.timer != null) {
             $.timer.cancel();
             $.timer.purge();
         }
         dCoBase.getProperties().getPropertiesFile().save();
     }
 
     private final long getInitialStart() {
         if (dCoBase.getProperties().getPropertiesFile().containsKey("bank.timer.reset")) {
             long reset = dCoBase.getProperties().getPropertiesFile().getLong("bank.timer.reset") - System.currentTimeMillis();
             if (reset < 0) {
                 return 0;
             } else {
                 dCoBase.getProperties().getPropertiesFile().setLong("bank.timer.reset", System.currentTimeMillis() + reset);
                 return reset;
             }
         } else {
             setResetTime();
             return getInterestInterval();
         }
     }
 
     private final long getInterestInterval() {
         return dCoBase.getProperties().getPropertiesFile().getLong("interest.pay.interval") * 60000;
     }
 
     final void setResetTime() {
         dCoBase.getProperties().getPropertiesFile().setLong("bank.timer.reset", System.currentTimeMillis() + getInterestInterval());
     }
 
     private final void installBankMessages() {
         try {
             PropertiesFile lang = new PropertiesFile("config/dConomy3/lang/en_US.lang");
             if (!lang.containsKey("bank.deposit")) {
                 lang.setString("bank.deposit", "$cAYou have deposited $cE{0, number, 0.00} $m$cA into your $c3Bank Account$cA.", ";dBankLite Message");
             }
             if (!lang.containsKey("bank.withdraw")) {
                 lang.setString("bank.withdraw", "$cAYou have withdrawn $cE{0, number, 0.00} $m$cA from your $c3Bank Account$cA.", ";dBankLite Message");
             }
             lang.save();
             MessageTranslator.reloadMessages();
         } catch (UtilityException uex) {
             warning("Failed to install dBankLite messages into dConomy English file (en_US.lang)");
         }
     }
 }
