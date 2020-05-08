 /**************************************************************************************************
  * Copyright (c) 2013, Björn Heinrichs <manf@derpymail.org>                                       *
  * Permission to use, copy, modify, and/or distribute this software                               *
  * for any purpose with or without fee is hereby granted, provided                                *
  * that the above copyright notice and this permission notice appear in all copies.               *
  *                                                                                                *
  * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD           *
  * TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS.              *
  * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL     *
  * DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,                 *
  * WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING                 *
  * OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.                          *
  **************************************************************************************************/
 
 package tk.manf.mcbb.listener;
 
 import static tk.manf.mcbb.api.MCbb._;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.player.PlayerLoginEvent.Result;
 
 import tk.manf.mcbb.api.MCbb;
 
 public class WhitelistListener implements Listener {
     private MCbb mcbb;
 
     public WhitelistListener(MCbb mcbb){
         this.mcbb = mcbb;
     }
     /**
      * Detect if player is allowed to join
      * @param ev
      */
     @EventHandler(priority = EventPriority.NORMAL)
     public void onPlayerConnect(final PlayerLoginEvent ev){
         Player player = ev.getPlayer();
         String username = player.getName();
         if(!mcbb.isRegistered(username)){
             ev.setKickMessage(_(mcbb.getLocale(username), "error.whitelist"));
             ev.setResult(Result.KICK_WHITELIST);
         }
     }
 }
