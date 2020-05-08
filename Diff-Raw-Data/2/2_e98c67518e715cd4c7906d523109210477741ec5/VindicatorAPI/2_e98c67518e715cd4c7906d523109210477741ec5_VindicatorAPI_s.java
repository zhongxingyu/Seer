 /*
  * Copyright 2012 James Geboski <jgeboski@gmail.com>
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
 
 package org.jgeboski.vindicator.api;
 
 import java.util.ArrayList;
 
 import org.bukkit.entity.Player;
 
 import org.jgeboski.vindicator.exception.APIException;
 import org.jgeboski.vindicator.exception.StorageException;
 import org.jgeboski.vindicator.storage.Storage;
 import org.jgeboski.vindicator.storage.StorageSQL;
 import org.jgeboski.vindicator.util.IPUtils;
 import org.jgeboski.vindicator.util.Utils;
 import org.jgeboski.vindicator.Vindicator;
 
 public class VindicatorAPI
 {
     public Vindicator vind;
     public Storage    storage;
 
     public VindicatorAPI(Vindicator vind)
         throws APIException
     {
         this.vind = vind;
 
         /* For now, SQL only */
         storage = new StorageSQL(
             vind.config.storeURL,  vind.config.storeUser,
             vind.config.storePass, vind.config.storePrefix);
     }
 
     public void close()
     {
         storage.close();
     }
 
     public void ban(String issuer, String target, String reason, long timeout)
         throws APIException
     {
         TargetObject to;
 
         for(TargetObject o : storage.getTargets(target)) {
             if(o.hasFlag(TargetObject.BAN))
                 throw new APIException("Ban already exists for %s", target);
         }
 
         to = new TargetObject(issuer, target, reason);
 
         to.addFlag(getTypeFlag(target, TargetObject.PLAYER, TargetObject.IP));
         to.addFlag(TargetObject.BAN);
 
         if(timeout > 0)
             to.setTimeout(Utils.time() * timeout);
 
         storage.add(to);
 
         if(to.hasFlag(TargetObject.IP))
             kickIP(target, "Banned: " + reason);
         else
             kick(target, "Banned: " + reason);
 
         vind.broadcast("vindicator.message.ban",
                        "Banned placed for %s by %s: %s",
                        target, issuer, reason);
 
         if(timeout > 0) {
             vind.broadcast("vindicator.message.ban",
                            "Temporary ban will be removed: %s",
                            to.getTimeoutStr());
         }
     }
 
     public void ban(String issuer, String target, String reason)
         throws APIException
     {
         ban(issuer, target, reason, 0);
     }
 
     public void kick(String issuer, String target, String reason)
         throws APIException
     {
         if(IPUtils.isAddress(target)) {
             if(!kickIP(target, reason))
                 throw new APIException("Player(s) for %s not found", target);
         } else {
             if(!kick(target, reason))
                 throw new APIException("Player %s not found", target);
         }
 
         vind.broadcast("vindicator.message.kick",
                        "Kick placed for %s by %s: %s",
                        target, issuer, reason);
     }
 
     public TargetObject[] lookup(String target)
         throws APIException
     {
         ArrayList<TargetObject> ret;
         int b;
         int n;
 
         ret = new ArrayList<TargetObject>();
         b = n = 0;
 
         for(TargetObject to : storage.getTargets(target)) {
             if(to.hasFlag(TargetObject.BAN)) {
                 ret.add(b, to);
                 b++;
             } else if(to.hasFlag(TargetObject.NOTE)) {
                 to.setId(n + 1);
                 ret.add(b + n, to);
                 n++;
             } else {
                 ret.add(to);
             }
         }
 
         return ret.toArray(new TargetObject[0]);
     }
 
     public void noteAdd(String issuer, String target, String note, boolean pub)
         throws APIException
     {
         TargetObject to;
         String       perm;
 
         to = new TargetObject(issuer, target, note);
 
         to.addFlag(getTypeFlag(target, TargetObject.PLAYER, TargetObject.IP));
         to.addFlag(TargetObject.NOTE);
 
         perm = "vindicator.message.noteadd";
 
         if(pub) {
             to.addFlag(TargetObject.PUBLIC);
             perm += ".public";
         }
 
         storage.add(to);
 
         vind.broadcast(perm, "Note added for %s by %s: %s",
                        target, issuer, note);
     }
 
     public void noteRem(String issuer, String target, int index)
         throws APIException
     {
         TargetObject[] tos;
         String         perm;
 
         int i;
         int n;
 
         tos = storage.getTargets(target);
 
         for(i = n = 0; (n < index) && (i < tos.length); i++) {
             if(!tos[i].hasFlag(TargetObject.NOTE))
                 n++;
         }
 
         if(n != index)
             throw new APIException("Note index %d not found", index);
 
         perm = "vindicator.message.noterem";
 
         if(tos[i].hasFlag(TargetObject.PUBLIC))
             perm += ".public";
 
         storage.remove(tos[i]);
 
         vind.broadcast(perm, "Note removed for %s by %s: %s",
                        tos[i].getTarget(), issuer, tos[i].getMessage());
     }
 
     public void unban(String issuer, String target)
         throws APIException
     {
         TargetObject bt;
         String       reason;
 
         bt = null;
 
         for(TargetObject to : storage.getTargets(target)) {
             if(!to.hasFlag(TargetObject.BAN))
                 continue;
             
             bt = to;
             break;
         }
 
         if(bt == null)
            throw new APIException("Ban for %d not found", target);
 
         target = bt.getTarget();
         reason = bt.getMessage();
 
         storage.remove(bt);
 
         if(vind.config.unbanNote)
             noteAdd(issuer, target, "Unbanned: " + reason, false);
 
         vind.broadcast("vindicator.message.unban",
                        "Ban removed for %s by %s: %s",
                        target, issuer, reason);
     }
 
     private int getTypeFlag(String target, int ifname, int ifaddress)
         throws APIException
     {
         if(Utils.isMinecraftName(target))
             return ifname;
         else if(IPUtils.isAddress(target))
             return ifaddress;
         else
             throw new APIException("Invalid player/IP: %s", target);
     }
 
     private boolean kick(String target, String message)
     {
         Player p;
 
         p = vind.getServer().getPlayerExact(target);
 
         if(p == null)
             return false;
 
         p.kickPlayer(message);
         return true;
     }
 
     private boolean kickIP(String target, String message)
     {
         String ip;
         int    i;
 
         i = 0;
 
         for(Player p : vind.getServer().getOnlinePlayers()) {
             ip = p.getAddress().getAddress().getHostAddress();
 
             if(ip.equals(target))
                 continue;
             
             p.kickPlayer(message);
             i++;
         }
 
         return (i > 0);
     }
 }
