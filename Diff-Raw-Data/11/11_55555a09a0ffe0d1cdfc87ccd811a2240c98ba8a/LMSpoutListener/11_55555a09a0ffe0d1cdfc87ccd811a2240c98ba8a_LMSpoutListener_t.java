 /*
  * This file is part of LoginMusic.
  *
  * LoginMusic is licensed under the GNU General Public License v3.
  *
  * LoginMusic is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by the
  * Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * LoginMusic is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License, If not,
  * see <http://www.gnu.org/licenses/> for the GNU General Public License.
  */
 package me.omlet.loginmusic.listener;
 
 import me.omlet.loginmusic.LoginMusic;
 
 import java.util.Collections;
 import java.util.List;
 import java.util.Random;
 
 import org.getspout.spoutapi.SpoutManager;
 import org.getspout.spoutapi.event.spout.SpoutCraftEnableEvent;
 import org.getspout.spoutapi.event.spout.SpoutListener;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 //Plays music upon player entering
 public class LMSpoutListener extends SpoutListener {
 	private LoginMusic plugin;
 	private SpoutPlayer player;
 
 	//Random number generator
 	private final Random rand = new Random();
 
 	public LMSpoutListener(LoginMusic instance) {
 		this.plugin = instance;
 	}
 
 	@Override
 	public void onSpoutCraftEnable(SpoutCraftEnableEvent event) {
 		//Grab the player associated with the event
 		this.player = SpoutManager.getPlayer(event.getPlayer());
 
 		//Lets play a global sound shall we? This is for everyone else on the server
 		if (this.plugin.getConfigInstance().getBoolean("general.play-global-music")) {
 			List temp = this.plugin.fixList(this.plugin.getConfigInstance().getList("global"));
 
 			//No reason to not try to play a sound that doesn't exist.
 			if (temp.isEmpty()) {
 				return;
 			}
 
 			//Grab a random number to choose the random sound (global).
 			int index = rand.nextInt(temp.size());
 
 			//Cycle through all the online players, check permissions, check config, play sounds based on results.
 			for (SpoutPlayer p : this.plugin.getSpoutServer().getOnlinePlayers()) {
 				if (!p.hasPermission("loginmusic.global")) {
					this.playSoundPerPerson(p, temp, -1);
                                 }
                                 
 				if (this.plugin.getConfigInstance().getBoolean("general.play-random-music")) {
 					this.playSoundPerPerson(player, temp, index);
 				} else {
 					this.playSoundPerPerson(player, temp, 0);
 				}
 			}
 		}
                 
                 //Lets play a sound for ops, shall we?
                 if (this.plugin.getConfigInstance().getBoolean("general.play-opmusic")) {
                     List temp = this.plugin.fixList(this.plugin.getConfigInstance().getList("opmusic"));
                     
                     //No reason to not try to play a sound that doesn't exist.
                     if (temp.isEmpty()) {
                         return;
                     }
                     
                     //Grab a random number to choose the random sound (opmusic).
                     int index = rand.nextInt(temp.size());
                     
                     //Cycle through all the online players, check permissions, check config, play sounds based on results.
                     for (SpoutPlayer p : this.plugin.getSpoutServer().getOnlinePlayers()) {
                         if (!p.hasPermission("loginmusic.opmusic")) {
                             continue;
                         }
                         
                         if (this.plugin.getConfigInstance().getBoolean("general.play-random-music")) {
                             if (!p.getName().equals(event.getPlayer().getName())) {
                                 SpoutManager.getSoundManager().playCustomSoundEffect(plugin, p, temp.get(index).toString(), false);
                             }
                         } else {
                             if (!p.getName().equals(event.getPlayer().getName())) {
                                 SpoutManager.getSoundManager().playCustomSoundEffect(plugin, p, temp.get(0).toString(), false);
                             }
                         }
                     }
                 }
                 
 		//Pass off a dummy list and the flag. This is a trick to just get the fallback working
 		//TODO Its hacky...lets try and do this better someday (eventhough it works fine...just not OOP).
                 if (!this.plugin.getConfigInstance().getBoolean("general.play-global-music")) {
                   this.playSoundPerPerson(player, Collections.emptyList(), -1);  
                 }
 	}
 
 	private void playSoundPerPerson(SpoutPlayer s, List list, int flag) {
 		//This essentially means that this was a fall back to the if statements above and we should calculate group-based music playing
 		if (flag == -1) {
 			for (String name : this.plugin.getGroups()) {
 				//Continue if the player has no perms for the group in question.
 				if (!this.player.hasPermission("loginmusic." + name.toLowerCase())) {
 					continue;
 				}
 
 				//Make sure the list has no nulls
 				List temp = this.plugin.fixList(this.plugin.getConfigInstance().getConfigurationSection("groups").getList(name));
 
 				//No reason to continue if we have no links.
 				if (temp.isEmpty()) {
 					continue;
 				}
 
 				if (this.plugin.getConfigInstance().getBoolean("general.universal")) {
                                     if (this.plugin.getConfigInstance().getBoolean("general.use-sound-effect")) {
                                         if (this.plugin.getConfigInstance().getBoolean("general.play-random-music")) {
                                             int num = rand.nextInt(temp.size());
                                             SpoutManager.getSoundManager().playGlobalCustomSoundEffect(plugin, temp.get(num).toString(), false);
                                         } else {
                                             SpoutManager.getSoundManager().playGlobalCustomSoundEffect(plugin, temp.get(0).toString(), false);
                                         }
                                     } else {
                                         if (this.plugin.getConfigInstance().getBoolean("general.play-random-music")) {
                                             int num = rand.nextInt(temp.size());
                                             SpoutManager.getSoundManager().playGlobalCustomMusic(plugin, temp.get(num).toString(), false);
                                         } else {
                                             SpoutManager.getSoundManager().playGlobalCustomMusic(plugin, temp.get(0).toString(), false);
                                         }
                                     }
                                 } else {
                                     if (this.plugin.getConfigInstance().getBoolean("general.use-sound-effect")) {
                                         if (this.plugin.getConfigInstance().getBoolean("general.play-random-music")) {
                                             int num = rand.nextInt(temp.size());
                                             SpoutManager.getSoundManager().playCustomSoundEffect(plugin, s, temp.get(num).toString(), false);
                                         } else {
                                             SpoutManager.getSoundManager().playCustomSoundEffect(plugin, s, temp.get(0).toString(), false);
                                         }
                                     } else {
                                         if (this.plugin.getConfigInstance().getBoolean("general.play-random-music")) {
                                             int num = rand.nextInt(temp.size());
                                             SpoutManager.getSoundManager().playCustomMusic(plugin, s, temp.get(num).toString(), false);
                                         } else {
                                             SpoutManager.getSoundManager().playCustomMusic(plugin, s, temp.get(0).toString(), false);
                                         }
                                     }
                                 }
 			}
 		} else {
                     for (String name : this.plugin.getGroups()) {
                         if (!this.player.hasPermission("loginmusic." + name.toLowerCase())
                                 || !this.player.hasPermission("loginmusic.global")) {
                             continue;
                         }
 
                         if (this.plugin.getConfigInstance().getBoolean("general.universal")) {
                             if (this.plugin.getConfigInstance().getBoolean("general.use-sound-effect")) {
                                 SpoutManager.getSoundManager().playGlobalCustomSoundEffect(plugin, list.get(flag).toString(), false);
                             } else {
                                 SpoutManager.getSoundManager().playGlobalCustomMusic(plugin, list.get(flag).toString(), false);
                             }
                         } else {
                                 if (this.plugin.getConfigInstance().getBoolean("general.use-sound-effect")) {
                                    SpoutManager.getSoundManager().playCustomSoundEffect(plugin, s, list.get(flag).toString(), false);
                                 } else {
                                    SpoutManager.getSoundManager().playCustomMusic(plugin, s, list.get(flag).toString(), false);
                                 }
                                 
                         }
                     }
             }
     }
 }
