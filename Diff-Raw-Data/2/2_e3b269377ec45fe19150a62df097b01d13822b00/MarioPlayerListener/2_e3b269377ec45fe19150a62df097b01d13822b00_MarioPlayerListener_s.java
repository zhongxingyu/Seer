 package littlegruz.marioworld.listeners;
 
 import littlegruz.marioworld.MarioMain;
 import littlegruz.marioworld.entities.MarioBlock;
 import littlegruz.marioworld.entities.MarioPlayer;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerEggThrowEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.inventory.ItemStack;
 import org.getspout.spoutapi.SpoutManager;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 /* Total music downloads required: 463KB*/
 
 public class MarioPlayerListener implements Listener{
    private static MarioMain plugin;
    
    public MarioPlayerListener(MarioMain instance) {
            plugin = instance;
    }
 
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event){
       Location playerEye = event.getPlayer().getEyeLocation();
       Location blockLoc, topBlockLoc;
       Block block;
 
       if(plugin.getWorldMap().containsKey(event.getPlayer().getWorld().getUID().toString())){
          if(plugin.getPlayerMap().get(event.getPlayer().getName()).getHitBlock() != null
                && playerEye.getY() - (int) playerEye.getY() < 0.74){
             plugin.getPlayerMap().get(event.getPlayer().getName()).setHitBlock(null);
          }
          
          /* If the players eye level goes above x.74 then they will end up hitting
          their head on a block */
          //event.getPlayer().sendMessage(Double.toString(playerEye.getY()));
          if(playerEye.getY() - (int) playerEye.getY() > 0.74){
             blockLoc = playerEye;
             blockLoc.setY(blockLoc.getY() + 1);
             block = blockLoc.getBlock();
             topBlockLoc = blockLoc;
             topBlockLoc.setY(blockLoc.getY() + 1);
             
             MarioPlayer mp = plugin.getPlayerMap().get(event.getPlayer().getName());
             if(block.getType().compareTo(Material.AIR) != 0
                   && block.getType().compareTo(Material.SIGN) != 0
                   && block.getType().compareTo(Material.SIGN_POST) != 0
                   && block.getType().compareTo(Material.WALL_SIGN) != 0
                   && !block.isLiquid() && mp.getHitBlock() == null){
                mp.setHitBlock(block);
                
                /* Check that the hit block isn't already hit and that it is
                one of the MarioBlocks */
                MarioBlock mb = plugin.getBlockMap().get(block.getLocation());
                if(mb != null && !mb.isHit()){
                   mb.setHit(true);
                   block.setType(Material.STONE);
 
                   SpoutPlayer sp = SpoutManager.getPlayer(event.getPlayer());
                   // Compare against all the types of special blocks
                   if(mb.getBlockType().compareToIgnoreCase("question") == 0){
                      topBlockLoc.getBlock().setType(Material.REDSTONE_TORCH_ON);
                      // File size 27KB
                      SpoutManager.getSoundManager().playCustomMusic(plugin, sp, "http://sites.google.com/site/littlegruzsplace/download/smb_powerup_appears.wav", true);
                   } else if(mb.getBlockType().compareToIgnoreCase("coin") == 0){
                      coinGet(mp, sp, 1);
                   } else if(mb.getBlockType().compareToIgnoreCase("poison") == 0){
                      topBlockLoc.getWorld().dropItem(topBlockLoc, new ItemStack(Material.BROWN_MUSHROOM, 1));
                      SpoutManager.getSoundManager().playCustomMusic(plugin, SpoutManager.getPlayer(event.getPlayer()), "https://sites.google.com/site/littlegruzsplace/download/smb_powerup_appears.wav", true);
                   } else if(mb.getBlockType().compareToIgnoreCase("super") == 0){
                      topBlockLoc.getWorld().dropItem(topBlockLoc, new ItemStack(Material.RED_MUSHROOM, 1));
                      SpoutManager.getSoundManager().playCustomMusic(plugin, SpoutManager.getPlayer(event.getPlayer()), "https://sites.google.com/site/littlegruzsplace/download/smb_powerup_appears.wav", true);
                   } else if(mb.getBlockType().compareToIgnoreCase("fire") == 0){
                      topBlockLoc.getWorld().dropItem(topBlockLoc, new ItemStack(Material.RED_ROSE, 1));
                      SpoutManager.getSoundManager().playCustomMusic(plugin, SpoutManager.getPlayer(event.getPlayer()), "https://sites.google.com/site/littlegruzsplace/download/smb_powerup_appears.wav", true);
                   }
                }
                /* Destroys the block hit if it is breakable and if the player is
                 * in their big form*/
                else if(plugin.getPlayerMap().get(event.getPlayer().getName()).getState().compareToIgnoreCase("Large") == 0
                      || plugin.getPlayerMap().get(event.getPlayer().getName()).getState().compareToIgnoreCase("Fire") == 0){
                   // TODO Perhaps put in a feature which allows the creation of breakable blocks?
                   if(plugin.isMarioDamage()){
                      if(block.getType().compareTo(Material.BRICK) == 0
                            || block.getType().compareTo(Material.COBBLESTONE) == 0){
                            block.setType(Material.AIR);
                            // File size 25KB
                            SpoutManager.getSoundManager().playCustomMusic(plugin, SpoutManager.getPlayer(event.getPlayer()), "https://sites.google.com/site/littlegruzsplace/download/smb_breakblock.wav", true);
                      }else
                         // File size 11KB
                         SpoutManager.getSoundManager().playCustomMusic(plugin, SpoutManager.getPlayer(event.getPlayer()), "https://sites.google.com/site/littlegruzsplace/download/smb_bump.wav", true);
                   }
                }
                else{
                   if(plugin.isMarioDamage()){
                      SpoutManager.getSoundManager().playCustomMusic(plugin, SpoutManager.getPlayer(event.getPlayer()), "https://sites.google.com/site/littlegruzsplace/download/smb_bump.wav", true);
                   }
                }
                plugin.getGui().update(event.getPlayer());
             }
          }
       }
    }
 
    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event){
       if(plugin.getWorldMap().containsKey(event.getPlayer().getWorld().getUID().toString()) && plugin.isMarioDamage()){
          MarioPlayer mp = plugin.getPlayerMap().get(event.getPlayer().getName());
          // Effect given when obtaining a growth mushroom
          if(event.getItem().getItemStack().getType().compareTo(Material.RED_MUSHROOM) == 0){
             event.getItem().remove();
             event.setCancelled(true);
             if(mp.getState().compareToIgnoreCase("Small") == 0){
                event.getPlayer().sendMessage("You've grown!");
                mp.setState("Large");
                // File size 10KB
                SpoutManager.getSoundManager().playCustomMusic(plugin, SpoutManager.getPlayer(event.getPlayer()), "https://sites.google.com/site/littlegruzsplace/download/smb3_powerup.wav", true);
             }
          }
       // Effect given when obtaining a poison mushroom
          else if(event.getItem().getItemStack().getType().compareTo(Material.BROWN_MUSHROOM) == 0){
             event.getItem().remove();
             event.setCancelled(true);
             if(mp.getState().compareToIgnoreCase("Small") == 0){
                event.getPlayer().damage(1000);
                plugin.deathSequence(event.getPlayer());
             } else if(mp.getState().compareToIgnoreCase("Fire") == 0){
                event.getPlayer().sendMessage("You've shrunk");
                plugin.getPlayerMap().get(event.getPlayer().getName()).setState("Large");
                // File size 8KB
                SpoutManager.getSoundManager().playCustomMusic(plugin, SpoutManager.getPlayer(event.getPlayer()), "https://sites.google.com/site/littlegruzsplace/download/smb3_powerdown.wav", true);
             } else{
                event.getPlayer().sendMessage("You've shrunk");
                plugin.getPlayerMap().get(event.getPlayer().getName()).setState("Small");
                // File size 8KB
                SpoutManager.getSoundManager().playCustomMusic(plugin, SpoutManager.getPlayer(event.getPlayer()), "https://sites.google.com/site/littlegruzsplace/download/smb3_powerdown.wav", true);
             }
          }
          // Effect given when obtaining a 1-up (cake) mushroom
          else if(event.getItem().getItemStack().getType().compareTo(Material.CAKE) == 0){
             event.getItem().remove();
             event.setCancelled(true);
             mp.setLives(mp.getLives() + 1);
             // File size 38KB
             SpoutManager.getSoundManager().playCustomMusic(plugin, SpoutManager.getPlayer(event.getPlayer()), "https://sites.google.com/site/littlegruzsplace/download/smb_1up.wav", true);
          }
          // Effect given when obtaining a coin
          else if(event.getItem().getItemStack().getType().compareTo(Material.GOLD_INGOT) == 0){
             event.getItem().remove();
             event.setCancelled(true);
             SpoutPlayer sp = SpoutManager.getPlayer(event.getPlayer());
             coinGet(mp, sp, 1);
          }
          // Effect given when obtaining a fire flower
          else if(event.getItem().getItemStack().getType().compareTo(Material.RED_ROSE) == 0){
             event.getItem().remove();
             event.setCancelled(true);
             if(mp.getState().compareToIgnoreCase("Large") == 0){
                event.getPlayer().sendMessage("Fire powah!");
                mp.setState("Fire");
                event.getPlayer().setItemInHand(new ItemStack(Material.EGG, 1));
                // File size 10KB
                SpoutManager.getSoundManager().playCustomMusic(plugin, SpoutManager.getPlayer(event.getPlayer()), "https://sites.google.com/site/littlegruzsplace/download/smb3_powerup.wav", true);
             } else{
                event.getPlayer().sendMessage("You've grown!");
                mp.setState("Large");
                // File size 10KB
                SpoutManager.getSoundManager().playCustomMusic(plugin, SpoutManager.getPlayer(event.getPlayer()), "https://sites.google.com/site/littlegruzsplace/download/smb3_powerup.wav", true);
             }
          }
          // Effect given when obtaining a star
          else if(event.getItem().getItemStack().getType().compareTo(Material.COOKIE) == 0){
             final MarioPlayer mPlayer = mp;
             // File size 441KB
             SpoutManager.getSoundManager().playCustomMusic(plugin, SpoutManager.getPlayer(event.getPlayer()), "https://sites.google.com/site/littlegruzsplace/download/starman.wav", true);
             mPlayer.setInvincible(true);
             event.getPlayer().sendMessage("Star power!");
             
             // Set invincibility to run out when the music stops (14.09s)
             plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
 
                public void run() {
                    mPlayer.setInvincible(false);
                    plugin.getServer().getPlayer(mPlayer.getPlayaName()).sendMessage("Star power has worn off");
                }
            }, 182L);
             
          }
       }
       plugin.getGui().update(event.getPlayer());
    }
    
    public void coinGet(MarioPlayer mp, SpoutPlayer sp, int amount){
       if(mp.getCoins() + amount >= 100){
          mp.setCoins(0);
          mp.setLives(mp.getLives() + 1);
          SpoutManager.getSoundManager().playCustomMusic(plugin, sp, "https://sites.google.com/site/littlegruzsplace/download/smb_1up.wav", true);
       }else{
          mp.setCoins(mp.getCoins() + amount);
          SpoutManager.getSoundManager().playCustomMusic(plugin, sp, "https://sites.google.com/site/littlegruzsplace/download/smb_coin.wav", true);
       }
    }
    
    /* When a player joins, add them to the Mario Player ArrayList if they
     * aren't already in the list*/
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
       if(plugin.getPlayerMap().get(event.getPlayer().getName()) == null){
          String name = event.getPlayer().getName();
          plugin.getPlayerMap().put(name, new MarioPlayer(name, "Small", plugin.getServer().getWorld(plugin.getPlayerWorld(name)).getSpawnLocation(), 0, 3));
       }
       
       if(plugin.getWorldMap().containsKey(event.getPlayer().getWorld().getUID().toString())){
          event.getPlayer().sendMessage("Welcome " + event.getPlayer().getName() + " to Mario WorldCraft");
       }
    }
 
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event){
       if(plugin.getWorldMap().containsKey(event.getPlayer().getWorld().getUID().toString())){
          MarioPlayer mp;
          mp = plugin.getPlayerMap().get(event.getPlayer().getName());
          mp.setState("Small");
          if(mp.getLives() <= 0){
             mp.setLives(3);
          }
          event.setRespawnLocation(mp.getCheckpoint());
          //plugin.getGui().removeGameOver(event.getPlayer());
          plugin.getGui().update(event.getPlayer());
       }
    }
 
    @EventHandler
    public void onPlayerEggThrow(PlayerEggThrowEvent event){
       if(plugin.getWorldMap().containsKey(event.getPlayer().getWorld().getUID().toString())){
          if(plugin.getPlayerMap().get(event.getPlayer().getName()).getState().compareToIgnoreCase("Fire") == 0){
             event.getPlayer().setItemInHand(new ItemStack(Material.EGG, 1));
             // File size 7KB
             SpoutManager.getSoundManager().playCustomMusic(plugin, SpoutManager.getPlayer(event.getPlayer()), "https://sites.google.com/site/littlegruzsplace/download/smb_fireball.wav", true);
          }
          event.setHatching(false);
       }
    }
 }
