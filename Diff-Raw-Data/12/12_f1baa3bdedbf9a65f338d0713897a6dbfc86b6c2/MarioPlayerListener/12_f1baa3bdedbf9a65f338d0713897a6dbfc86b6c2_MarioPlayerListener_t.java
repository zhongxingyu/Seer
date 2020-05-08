 package littlegruz.marioworld.listeners;
 
 import littlegruz.marioworld.MarioMain;
 import littlegruz.marioworld.entities.MarioBlock;
 import littlegruz.marioworld.entities.MarioPlayer;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerEggThrowEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 import org.getspout.spoutapi.SpoutManager;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 /* Total music downloads required: 498KB*/
 
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
 
       if(plugin.getWorldMap().containsKey(event.getPlayer().getWorld().getName())){
          if(plugin.getPlayerMap().get(event.getPlayer().getName()).getHitBlock() != null
                && playerEye.getY() - (int) playerEye.getY() < 0.74){
             plugin.getPlayerMap().get(event.getPlayer().getName()).setHitBlock(null);
          }
          
          /* If the players eye level goes above x.74 then they will end up hitting
          their head on a block */
          if(playerEye.getY() - (int) playerEye.getY() > 0.74){
             blockLoc = playerEye;
             blockLoc.setY(blockLoc.getY() + 1);
             block = blockLoc.getBlock();
             topBlockLoc = blockLoc;
             topBlockLoc.setY(blockLoc.getY() - 1);
             
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
 
                   // Compare against all the types of special blocks
                   if(mb.getBlockType().compareToIgnoreCase("question") == 0){
                      block.setType(Material.STONE);
                      topBlockLoc.setY(blockLoc.getY() + 2);
                      topBlockLoc.getBlock().setType(Material.REDSTONE_TORCH_ON);
                      // File size 27KB
                      if(plugin.isSpoutEnabled())
                         SpoutManager.getSoundManager().playCustomMusic(plugin, SpoutManager.getPlayer(event.getPlayer()), "http://sites.google.com/site/littlegruzsplace/download/smb_powerup_appears.wav", true);
                      else
                         event.getPlayer().sendMessage(plugin.getCurrentRB().getString("PowerBlock"));
                   } else if(mb.getBlockType().compareToIgnoreCase("coin") == 0){
                      block.setType(Material.STONE);
                      coinGet(mp, event.getPlayer(), 1);
                   } else if(mb.getBlockType().compareToIgnoreCase("poison") == 0){
                      block.setType(Material.STONE);
                      topBlockLoc.getWorld().dropItem(topBlockLoc, new ItemStack(Material.BROWN_MUSHROOM, 1));
                      if(plugin.isSpoutEnabled())
                         SpoutManager.getSoundManager().playCustomMusic(plugin, SpoutManager.getPlayer(event.getPlayer()), "https://sites.google.com/site/littlegruzsplace/download/smb_powerup_appears.wav", true);
                      else
                         event.getPlayer().sendMessage(plugin.getCurrentRB().getString("PowerUpFind"));
                   } else if(mb.getBlockType().compareToIgnoreCase("super") == 0){
                      block.setType(Material.STONE);
                      topBlockLoc.getWorld().dropItem(topBlockLoc, new ItemStack(Material.RED_MUSHROOM, 1));
                      if(plugin.isSpoutEnabled())
                         SpoutManager.getSoundManager().playCustomMusic(plugin, SpoutManager.getPlayer(event.getPlayer()), "https://sites.google.com/site/littlegruzsplace/download/smb_powerup_appears.wav", true);
                      else
                         event.getPlayer().sendMessage(plugin.getCurrentRB().getString("PowerUpFind"));
                   } else if(mb.getBlockType().compareToIgnoreCase("fire") == 0){
                      block.setType(Material.STONE);
                      topBlockLoc.getWorld().dropItem(topBlockLoc, new ItemStack(Material.RED_ROSE, 1));
                      if(plugin.isSpoutEnabled())
                         SpoutManager.getSoundManager().playCustomMusic(plugin, SpoutManager.getPlayer(event.getPlayer()), "https://sites.google.com/site/littlegruzsplace/download/smb_powerup_appears.wav", true);
                      else
                         event.getPlayer().sendMessage(plugin.getCurrentRB().getString("PowerUpFind"));
                   } else if(mb.getBlockType().compareToIgnoreCase("1-up") == 0){
                      block.setType(Material.STONE);
                      topBlockLoc.getWorld().dropItem(topBlockLoc, new ItemStack(Material.CAKE, 1));
                      if(plugin.isSpoutEnabled())
                         SpoutManager.getSoundManager().playCustomMusic(plugin, SpoutManager.getPlayer(event.getPlayer()), "https://sites.google.com/site/littlegruzsplace/download/smb_powerup_appears.wav", true);
                      else
                         event.getPlayer().sendMessage(plugin.getCurrentRB().getString("PowerUpFind"));
                   } else if(mb.getBlockType().compareToIgnoreCase("inv") == 0){
                      block.setType(Material.STONE);
                      topBlockLoc.getWorld().dropItem(topBlockLoc, new ItemStack(Material.COOKIE, 1));
                      if(plugin.isSpoutEnabled())
                         SpoutManager.getSoundManager().playCustomMusic(plugin, SpoutManager.getPlayer(event.getPlayer()), "https://sites.google.com/site/littlegruzsplace/download/smb_powerup_appears.wav", true);
                      else
                         event.getPlayer().sendMessage(plugin.getCurrentRB().getString("PowerUpFind"));
                   /* Destroys the block hit if it is breakable and if the player is
                    * in a big form*/
                   } else if(mb.getBlockType().compareToIgnoreCase("break") == 0){
                      if(plugin.getPlayerMap().get(event.getPlayer().getName()).getState().compareToIgnoreCase("Large") == 0
                      || plugin.getPlayerMap().get(event.getPlayer().getName()).getState().compareToIgnoreCase("Fire") == 0){
                         block.setType(Material.AIR);
                         // File size 25KB
                         if(plugin.isSpoutEnabled())
                            SpoutManager.getSoundManager().playCustomMusic(plugin, SpoutManager.getPlayer(event.getPlayer()), "https://sites.google.com/site/littlegruzsplace/download/smb_breakblock.wav", true);
                         else
                            event.getPlayer().sendMessage("*smash*");
                      }
                      else{
                         mb.setHit(false);
                         // File size 11KB
                         if(plugin.isSpoutEnabled())
                            SpoutManager.getSoundManager().playCustomMusic(plugin, SpoutManager.getPlayer(event.getPlayer()), "https://sites.google.com/site/littlegruzsplace/download/smb_bump.wav", true);
                         else
                            event.getPlayer().sendMessage("*bump*");
                      }
                   }
                }
                else{
                   if(plugin.isMarioDamage()){
                      if(plugin.isSpoutEnabled())
                         SpoutManager.getSoundManager().playCustomMusic(plugin, SpoutManager.getPlayer(event.getPlayer()), "https://sites.google.com/site/littlegruzsplace/download/smb_bump.wav", true);
                      else
                         event.getPlayer().sendMessage("*bump*");
                   }
                }
                if(plugin.isSpoutEnabled())
                   plugin.getGui().update(event.getPlayer());
             }
          }
          
          blockLoc = event.getPlayer().getLocation().getBlock().getLocation();
          blockLoc.setY(blockLoc.getY() - 1);
          
          if(plugin.getBlockMap().get(blockLoc) != null){
             if(plugin.getBlockMap().get(blockLoc).getBlockType().compareTo("warp") == 0){
                blockLoc = plugin.getBlockMap().get(blockLoc).getLocation().clone();
                blockLoc.setY(blockLoc.getY() + 1);
                blockLoc.setX(blockLoc.getX() + 0.5);
                blockLoc.setZ(blockLoc.getZ() + 0.5);
                
                event.getPlayer().teleport(blockLoc);
             // File size 35KB
                if(plugin.isSpoutEnabled())
                   SpoutManager.getSoundManager().playCustomMusic(plugin, SpoutManager.getPlayer(event.getPlayer()), "https://sites.google.com/site/littlegruzsplace/download/smb_pipe.wav", true);
                else
                   event.getPlayer().sendMessage("*zwoop*");
             }
          }
       }
    }
 
    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event){
       if(plugin.getWorldMap().containsKey(event.getPlayer().getWorld().getName()) && plugin.isMarioDamage()){
          MarioPlayer mp = plugin.getPlayerMap().get(event.getPlayer().getName());
          // Effect given when obtaining a super mushroom
          if(event.getItem().getItemStack().getType().compareTo(Material.RED_MUSHROOM) == 0){
             event.getItem().remove();
             event.setCancelled(true);
             if(mp.getState().compareToIgnoreCase("Small") == 0){
                event.getPlayer().sendMessage(ChatColor.DARK_RED + plugin.getCurrentRB().getString("Growth"));
                mp.setState("Large");
                // File size 10KB
                if(plugin.isSpoutEnabled())
                   SpoutManager.getSoundManager().playCustomMusic(plugin, SpoutManager.getPlayer(event.getPlayer()), "https://sites.google.com/site/littlegruzsplace/download/smb3_powerup.wav", true);
             }
          }
       // Effect given when obtaining a poison mushroom
          else if(event.getItem().getItemStack().getType().compareTo(Material.BROWN_MUSHROOM) == 0){
             event.getItem().remove();
             event.setCancelled(true);
             if(!mp.isInvincible()){
                if(mp.getState().compareToIgnoreCase("Small") == 0){
                   event.getPlayer().damage(1000);
                   plugin.deathSequence(event.getPlayer());
                } else if(mp.getState().compareToIgnoreCase("Fire") == 0){
                   event.getPlayer().sendMessage(ChatColor.DARK_PURPLE + plugin.getCurrentRB().getString("Shrink"));
                   plugin.getPlayerMap().get(event.getPlayer().getName()).setState("Large");
                   event.getPlayer().getInventory().remove(Material.EGG);
                   // File size 8KB
                   if(plugin.isSpoutEnabled())
                      SpoutManager.getSoundManager().playCustomMusic(plugin, SpoutManager.getPlayer(event.getPlayer()), "https://sites.google.com/site/littlegruzsplace/download/smb3_powerdown.wav", true);
                } else{
                   event.getPlayer().sendMessage(ChatColor.DARK_PURPLE + plugin.getCurrentRB().getString("Shrink"));
                   plugin.getPlayerMap().get(event.getPlayer().getName()).setState("Small");
                   // File size 8KB
                   if(plugin.isSpoutEnabled())
                      SpoutManager.getSoundManager().playCustomMusic(plugin, SpoutManager.getPlayer(event.getPlayer()), "https://sites.google.com/site/littlegruzsplace/download/smb3_powerdown.wav", true);
                }
             }
          }
          // Effect given when obtaining a 1-up (cake) mushroom
          else if(event.getItem().getItemStack().getType().compareTo(Material.CAKE) == 0){
             event.getItem().remove();
             event.setCancelled(true);
             mp.setLives(mp.getLives() + 1);
             event.getPlayer().sendMessage(ChatColor.GREEN + plugin.getCurrentRB().getString("1UP"));
             event.getPlayer().sendMessage(ChatColor.GREEN + plugin.getCurrentRB().getString("Lives") + ": " + Integer.toString(mp.getLives()));
             // File size 38KB
             if(plugin.isSpoutEnabled())
                SpoutManager.getSoundManager().playCustomMusic(plugin, SpoutManager.getPlayer(event.getPlayer()), "https://sites.google.com/site/littlegruzsplace/download/smb_1up.wav", true);
          }
          // Effect given when obtaining a coin
          else if(event.getItem().getItemStack().getType().compareTo(Material.GOLD_INGOT) == 0){
             event.getItem().remove();
             event.setCancelled(true);
             coinGet(mp, event.getPlayer(), 1);
          }
          // Effect given when obtaining a fire flower
          else if(event.getItem().getItemStack().getType().compareTo(Material.RED_ROSE) == 0){
             event.getItem().remove();
             event.setCancelled(true);
             if(mp.getState().compareToIgnoreCase("Large") == 0){
                event.getPlayer().sendMessage(ChatColor.RED + plugin.getCurrentRB().getString("Fireball"));
                mp.setState("Fire");
                event.getPlayer().setItemInHand(new ItemStack(Material.EGG, 1));
                // File size 10KB
                if(plugin.isSpoutEnabled())
                   SpoutManager.getSoundManager().playCustomMusic(plugin, SpoutManager.getPlayer(event.getPlayer()), "https://sites.google.com/site/littlegruzsplace/download/smb3_powerup.wav", true);
             } else{
                event.getPlayer().sendMessage(ChatColor.RED + plugin.getCurrentRB().getString("Growth"));
                mp.setState("Large");
                // File size 10KB
                if(plugin.isSpoutEnabled())
                   SpoutManager.getSoundManager().playCustomMusic(plugin, SpoutManager.getPlayer(event.getPlayer()), "https://sites.google.com/site/littlegruzsplace/download/smb3_powerup.wav", true);
             }
          }
          // Effect given when obtaining a star
          else if(event.getItem().getItemStack().getType().compareTo(Material.COOKIE) == 0){
             event.getItem().remove();
             event.setCancelled(true);
             final MarioPlayer mPlayer = mp;
             // File size 441KB
             if(plugin.isSpoutEnabled())
                SpoutManager.getSoundManager().playCustomMusic(plugin, SpoutManager.getPlayer(event.getPlayer()), "https://sites.google.com/site/littlegruzsplace/download/starman.wav", true);
             mPlayer.setInvincible(true);
             event.getPlayer().sendMessage(ChatColor.YELLOW + plugin.getCurrentRB().getString("StarPower"));
             event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 280, 1));
             
             // Set invincibility to run out when the music stops (14.09s)
             plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
 
                public void run() {
                    mPlayer.setInvincible(false);
                    plugin.getServer().getPlayer(mPlayer.getPlayaName()).sendMessage(ChatColor.YELLOW + plugin.getCurrentRB().getString("StarPowerWane"));
                }
            }, 280L);
             
          }
         if(plugin.isSpoutEnabled())
            plugin.getGui().update(event.getPlayer());
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
       
       if(plugin.getWorldMap().containsKey(event.getPlayer().getWorld().getName())){
          event.getPlayer().sendMessage(plugin.getCurrentRB().getString("WelcomeP1")
                + event.getPlayer().getName() + ", " + plugin.getCurrentRB().getString("WelcomeP2"));
       }
    }
 
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event){
       if(plugin.getWorldMap().containsKey(event.getPlayer().getWorld().getName())){
          MarioPlayer mp;
          Location loc;
          mp = plugin.getPlayerMap().get(event.getPlayer().getName());
          
          if(mp.getLives() <= 0){
             mp.setLives(plugin.getDefaultLives());
             mp.setCheckpoint(event.getPlayer().getWorld().getSpawnLocation());
          }
 
          mp.setState("Small");
          loc = mp.getCheckpoint().clone();
          loc.setX(loc.getX() + 0.5);
          loc.setZ(loc.getZ() + 0.5);
          event.setRespawnLocation(loc);
          //plugin.getGui().removeGameOver(event.getPlayer());
          
          if(plugin.isSpoutEnabled())
             plugin.getGui().update(event.getPlayer());
          else
             event.getPlayer().sendMessage(ChatColor.GREEN + plugin.getCurrentRB().getString("LivesLeft") + ": " + Integer.toString(mp.getLives()));
          
          plugin.getLavaDeathMap().remove(mp.getPlayaName());
       }
    }
 
    @EventHandler
    public void onPlayerEggThrow(PlayerEggThrowEvent event){
       if(plugin.getWorldMap().containsKey(event.getPlayer().getWorld().getName())){
          if(plugin.getPlayerMap().get(event.getPlayer().getName()).getState().compareToIgnoreCase("Fire") == 0){
             event.getPlayer().setItemInHand(new ItemStack(Material.EGG, 1));
             // File size 7KB
             if(plugin.isSpoutEnabled())
                SpoutManager.getSoundManager().playCustomMusic(plugin, SpoutManager.getPlayer(event.getPlayer()), "https://sites.google.com/site/littlegruzsplace/download/smb_fireball.wav", true);
             else
                event.getPlayer().sendMessage(ChatColor.RED + "*fwoosh*");
          }
          event.setHatching(false);
       }
    }
    
    // Determines what happens when a player gets a coin
    public void coinGet(MarioPlayer mp, Player playa, int amount){
       if(mp.getCoins() + amount >= plugin.getCoinLevel()
             && !plugin.isCoinPersistence()){
          mp.setCoins((mp.getCoins() + amount) % plugin.getCoinLevel());
          mp.setLives(mp.getLives() + 1);
          
          if(plugin.isSpoutEnabled()){
             SpoutPlayer sp = SpoutManager.getPlayer(playa);
             SpoutManager.getSoundManager().playCustomMusic(plugin, sp, "https://sites.google.com/site/littlegruzsplace/download/smb_1up.wav", true);
          }
          else
             playa.sendMessage(ChatColor.GREEN + plugin.getCurrentRB().getString("Lives") + ": " + Integer.toString(mp.getLives()));
       }else{
          
          // Still level up the player when they enough coins to level up
          if((mp.getCoins() + amount) % plugin.getCoinLevel() == 0 && plugin.isCoinPersistence()){
             mp.setLives(mp.getLives() + 1);
             
             if(plugin.isSpoutEnabled()){
                SpoutPlayer sp = SpoutManager.getPlayer(playa);
                SpoutManager.getSoundManager().playCustomMusic(plugin, sp, "https://sites.google.com/site/littlegruzsplace/download/smb_1up.wav", true);
             }
             else
                playa.sendMessage(ChatColor.GREEN + plugin.getCurrentRB().getString("Lives") + ": " + Integer.toString(mp.getLives()));
          }
          mp.setCoins(mp.getCoins() + amount);
          
          if(plugin.isSpoutEnabled()){
             SpoutPlayer sp = SpoutManager.getPlayer(playa);
             SpoutManager.getSoundManager().playCustomMusic(plugin, sp, "https://sites.google.com/site/littlegruzsplace/download/smb_coin.wav", true);
          }
       }
       playa.sendMessage(ChatColor.YELLOW + plugin.getCurrentRB().getString("Coins") + ": " + Integer.toString(mp.getCoins()));
    }
 }
