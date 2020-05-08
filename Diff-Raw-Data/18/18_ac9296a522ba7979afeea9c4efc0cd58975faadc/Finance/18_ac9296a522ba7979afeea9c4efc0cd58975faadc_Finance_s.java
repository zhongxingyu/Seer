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
 
 import net.visualillusionsent.dconomy.accounting.wallet.Wallet;
 import net.visualillusionsent.dconomy.accounting.wallet.WalletHandler;
 import net.visualillusionsent.dconomy.accounting.wallet.WalletTransaction;
 import net.visualillusionsent.dconomy.dCoBase;
 import net.visualillusionsent.dconomy.modinterface.ModUser;
 import net.visualillusionsent.utils.DateUtils;
 import net.visualillusionsent.utils.PropertiesFile;
 
 import java.text.MessageFormat;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import static net.visualillusionsent.dconomy.accounting.wallet.WalletTransaction.ActionType.PLUGIN_DEPOSIT;
 
 /**
  * Finance Department
  *
  * @author Jason (darkdiplomat)
  */
 public final class Finance {
     private final PropertiesFile _pending, _reset;
     private final MySalary mys;
     private Timer timer;
 
     public Finance(MySalary mys) {
         this.mys = mys;
         _reset = new PropertiesFile("config/MySalary/.reset.mys");
         _reset.getLong("timer.reset", -1);
         _pending = new PropertiesFile("config/MySalary/.pending.mys");
         reset(true);
     }
 
     public final void payout() {
         mys.broadcastPayDay();
         for (Wallet wallet : WalletHandler.getWallets().values()) {
             // check wallet lock status
             if (wallet.isLocked() && !mys.getCfg().payIfLocked())
                 continue;
             double pay = mys.getCfg().getDefaultPayAmount();
             // check if group amount is needed
             if (mys.getCfg().isGroupSpecificEnabled()) {
                 String group = mys.getGroupNameForUser(wallet.getOwner());
                 // if no group, ignore wallet and continue
                 if (group == null)
                     continue;
                 pay = mys.getCfg().getGroupPay(group);
                 // if group doesn't have an amount, ignore it and continue
                 if (pay <= 0)
                     continue;
             }
 
             // Check if player's are required to claim their pay
             if (mys.getCfg().isRequireClaimEnabled()) {
                 // Check if checks can be accumulated
                 if (mys.getCfg().isAccumulateChecksEnabled()) {
                     // if an owner has a pending check, add to it
                     if (_pending.containsKey(wallet.getOwner())) {
                         _pending.setDouble(wallet.getOwner(), _pending.getDouble(wallet.getOwner()) + pay);
                     }
                     else { // just add them to the list
                         _pending.setDouble(wallet.getOwner(), pay);
                     }
                 }
                 else { // accumulating is disabled, so the current pay is set and the old check is burned
                     _pending.setDouble(wallet.getOwner(), pay);
                 }
             }
             else {
                 wallet.deposit(pay);
                 dCoBase.getServer().newTransaction(new WalletTransaction(mys, dCoBase.getServer().getUser(wallet.getOwner()), PLUGIN_DEPOSIT, pay));
                 mys.messageUser(wallet.getOwner(), MessageFormat.format("\u00A7AYou have received\u00A76 {0,number,#.##} {1}", pay, dCoBase.getProperties().getString("money.name")));
             }
         }
         if (mys.getCfg().payServer()) {
             double serv_pay = mys.getCfg().getDefaultPayAmount();
             if (mys.getCfg().isGroupSpecificEnabled()) {
                 serv_pay = mys.getCfg().getGroupPay("SERVER");
                 if (serv_pay <= 0)
                     return;
             }
             WalletHandler.getWalletByName("SERVER").deposit(serv_pay);
             dCoBase.getServer().newTransaction(new WalletTransaction(mys, (ModUser) dCoBase.getServer(), PLUGIN_DEPOSIT, serv_pay));
         }
     }
 
     public final void reset(boolean booting) {
         if (timer != null) {
             timer.cancel();
         }
         timer = new Timer();
         long period = mys.getCfg().getDelay();
         if (_reset.getLong("timer.reset") > -1 && booting) {
             period = _reset.getLong("timer.reset") - System.currentTimeMillis();
             if (period < 0) {
                 period = 60000;
             }
         }
        _reset.setLong("timer.reset", System.currentTimeMillis() + mys.getCfg().getDelay());
         timer.schedule(new SalaryTask(), period, mys.getCfg().getDelay());
     }
 
     public final double checkPendingAndPay(String user_name) {
         if (_pending.containsKey(user_name)) {
             double pay = _pending.getDouble(user_name);
             _pending.removeKey(user_name);
             WalletHandler.getWalletByName(user_name).deposit(pay);
             dCoBase.getServer().newTransaction(new WalletTransaction(mys, dCoBase.getServer().getUser(user_name), PLUGIN_DEPOSIT, pay));
             return pay;
         }
         return -1;
     }
 
     public final String getTimeUntil() {
         return DateUtils.getTimeUntil((_reset.getLong("timer.reset") - System.currentTimeMillis()) / 1000);
     }
 
     public final void close() {
         _pending.save();
         _reset.save();
         timer.cancel();
     }
 
     private final class SalaryTask extends TimerTask {
 
         @Override
         public void run() {
             payout();
             _reset.setLong("timer.reset", System.currentTimeMillis() + mys.getCfg().getDelay());
         }
     }
 }
