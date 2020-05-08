 /*
  * Copyright 2012-2013 James Geboski <jgeboski@gmail.com>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.jgeboski.vindicator.event;
 
 import java.util.HashSet;
 
 import org.jgeboski.vindicator.storage.StorageException;
 import org.jgeboski.vindicator.storage.StorageLogin;
 import org.jgeboski.vindicator.storage.StoragePlayer;
 import org.jgeboski.vindicator.storage.StorageRecord;
 import org.jgeboski.vindicator.util.Log;
 import org.jgeboski.vindicator.util.Message;
 import org.jgeboski.vindicator.util.StrUtils;
 import org.jgeboski.vindicator.util.Utils;
 import org.jgeboski.vindicator.VindicatorException;
 
 import static org.jgeboski.vindicator.util.Message.hl;
 
 public class VindicatorLoginEvent extends VindicatorEvent
 {
     public StorageLogin login;
 
     public VindicatorLoginEvent(StorageLogin login)
     {
         super(true);
         this.login = login;
     }
 
     public void task()
         throws VindicatorException
     {
         login.validate(storage, vind.config.autoComplete);
 
         if (!eventContinue())
             return;
 
        checkAddresses(login);
        checkRecords(login);
     }
 
     private void checkAddresses(StorageLogin login)
         throws VindicatorException
     {
         HashSet<String> plist;
         StorageLogin    slogin;
         long            val;
 
         slogin = storage.getLogin(login);
         plist  = new HashSet<String>();
 
         if (slogin != null) {
             login = slogin;
             login.time = Utils.time();
             login.count++;
             storage.update(login);
         } else {
             storage.add(login);
         }
 
         if ((vind.config.altInfoLogins == 0) || (vind.config.altInfoTime == 0))
             return;
 
         for (StorageLogin l : storage.getLogins(login.address)) {
             if (login.player.ident.equals(l.player.ident))
                 continue;
 
             val = vind.config.altInfoLogins;
 
             if ((vind.config.altInfoLogins >= 0) && (l.count > val))
                 continue;
 
             val = vind.config.altInfoTime + l.time;
 
             if ((vind.config.altInfoTime >= 0) && (Utils.time() > val))
                 continue;
 
             plist.add(hl(l.player.alias));
         }
 
         if (plist.size() < 1)
             return;
 
         vind.broadcast("vindicator.message.notify",
                        "Player %s has %s alternates(s): %s",
                        hl(login.player.alias), hl(plist.size()),
                        StrUtils.join(plist.toArray(new String[0]), ", "));
     }
 
     private void checkRecords(StorageLogin login)
         throws VindicatorException
     {
         StorageRecord brecd;
         StorageRecord mrecd;
         String        str;
         int           notec;
 
         brecd = null;
         mrecd = null;
         notec = 0;
 
         for (StorageRecord r : storage.getRecords(login)) {
             if (r.hasFlag(StorageRecord.BAN)) {
                 brecd = r;
                 break;
             } else if (r.hasFlag(StorageRecord.MUTE)) {
                 mrecd = r;
             } else if (r.hasFlag(StorageRecord.NOTE)) {
                 notec++;
             }
         }
 
         if (brecd != null) {
             if ((brecd.timeout < 1) || (brecd.timeout > Utils.time())) {
                 if (brecd.hasFlag(StorageRecord.ADDRESS))
                     str = "Player %s attempted to join with a banned IP: %s";
                 else
                     str = "Player %s attempted to join banned: %s";
 
                 vind.broadcast("vindicator.message.notify", str,
                                hl(login.player.alias), hl(brecd.message));
 
                 str = new String();
 
                 if (brecd.timeout > 0) {
                     str += " until ";
                     str += hl(Utils.timestr(Utils.DATEF_LONG, brecd.timeout));
                 }
 
                 throw new VindicatorException("Banned%s: %s", str,
                                               hl(brecd.message));
             }
 
             brecd.issuer = new StoragePlayer(Message.plugin);
             vind.execute(new VindicatorUnbanEvent(brecd, sender));
         }
 
         if (mrecd != null) {
             if ((mrecd.timeout > 0) && (mrecd.timeout < Utils.time())) {
                 mrecd.issuer = new StoragePlayer(Message.plugin);
                 vind.execute(new VindicatorUnmuteEvent(mrecd, sender));
                 mrecd = null;
             } else {
                 mrecd.validate(storage, false);
                 vind.mutes.put(mrecd.target.ident, mrecd);
             }
         }
 
         if ((notec < 1) && (mrecd == null))
             return;
 
         str = String.format("Player %s has %s note(s)",
                             hl(login.player.alias), hl(notec));
 
         if (mrecd != null)
             str += " and is " + hl("muted");
 
         str += ".";
         vind.broadcast("vindicator.message.notify", str);
     }
 }
