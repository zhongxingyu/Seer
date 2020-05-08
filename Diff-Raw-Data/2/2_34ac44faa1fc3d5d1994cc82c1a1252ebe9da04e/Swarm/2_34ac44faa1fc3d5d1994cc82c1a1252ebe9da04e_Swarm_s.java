 package com.censoredsoftware.demigods.ability.ultimate;
 
 import com.censoredsoftware.demigods.Demigods;
 import com.censoredsoftware.demigods.ability.Ability;
 import com.censoredsoftware.demigods.battle.Battle;
 import com.censoredsoftware.demigods.deity.Deity;
 import com.censoredsoftware.demigods.player.DCharacter;
 import com.censoredsoftware.demigods.player.DPlayer;
 import com.censoredsoftware.demigods.player.Skill;
 import com.censoredsoftware.demigods.util.Spigots;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 import org.bukkit.Effect;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.entity.*;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 import org.bukkit.scheduler.BukkitRunnable;
 import org.bukkit.util.Vector;
 
 import java.util.List;
 import java.util.Set;
 
 public class Swarm implements Ability
 {
 	private final static String name = "Swarm", command = "swarm";
 	private final static int cost = 3700, delay = 600, repeat = 0;
 	private final static Skill.Type type = Skill.Type.ULTIMATE;
 	private final static List<String> details = Lists.newArrayList("Swarm you enemies with super powerful zombies.");
 	private String deity, permission;
 
 	public Swarm(String deity, String permission)
 	{
 		this.deity = deity;
 		this.permission = permission;
 	}
 
 	public static void swarm(Player player)
 	{
 		// Define variables
 		DCharacter character = DPlayer.Util.getPlayer(player).getCurrent();
 		Set<Entity> entitySet = Sets.newHashSet();
 		Vector playerLocation = player.getLocation().toVector();
 
 		if(!Ability.Util.doAbilityPreProcess(player, cost, type)) return;
 
 		for(Entity anEntity : player.getWorld().getEntities())
 			if(anEntity.getLocation().toVector().isInSphere(playerLocation, 50.0)) entitySet.add(anEntity);
 
 		for(Entity entity : entitySet)
 		{
 			if(entity instanceof Player)
 			{
 				Player otherPlayer = (Player) entity;
 				DCharacter otherChar = DPlayer.Util.getPlayer(otherPlayer).getCurrent();
 				if(otherPlayer.equals(player)) continue;
 				if(otherChar != null && !DCharacter.Util.areAllied(character, otherChar)) Util.spawnZombie(player, otherPlayer);
 			}
 			else if(entity instanceof LivingEntity)
 			{
 				LivingEntity livingEntity = (LivingEntity) entity;
 				Util.spawnZombie(player, livingEntity);
 			}
 		}
 	}
 
 	@Override
 	public String getDeity()
 	{
 		return deity;
 	}
 
 	@Override
 	public String getName()
 	{
 		return name;
 	}
 
 	@Override
 	public String getCommand()
 	{
 		return command;
 	}
 
 	@Override
 	public String getPermission()
 	{
 		return permission;
 	}
 
 	@Override
 	public int getCost()
 	{
 		return cost;
 	}
 
 	@Override
 	public int getDelay()
 	{
 		return delay;
 	}
 
 	@Override
 	public int getRepeat()
 	{
 		return repeat;
 	}
 
 	@Override
 	public List<String> getDetails()
 	{
 		return details;
 	}
 
 	@Override
 	public Skill.Type getType()
 	{
 		return type;
 	}
 
 	@Override
 	public Material getWeapon()
 	{
 		return null;
 	}
 
 	@Override
 	public boolean hasWeapon()
 	{
 		return getWeapon() != null;
 	}
 
 	@Override
 	public Listener getListener()
 	{
 		return new Listener()
 		{
 			@EventHandler(priority = EventPriority.HIGHEST)
 			public void onPlayerInteract(PlayerInteractEvent interactEvent)
 			{
 				if(Demigods.MiscUtil.isDisabledWorld(interactEvent.getPlayer().getWorld())) return;
 
 				if(!Ability.Util.isLeftClick(interactEvent)) return;
 
 				// Set variables
 				Player player = interactEvent.getPlayer();
 				DCharacter character = DPlayer.Util.getPlayer(player).getCurrent();
 
 				if(!Deity.Util.canUseDeitySilent(character, deity)) return;
 
 				if(player.getItemInHand() != null && character.getMeta().checkBound(name, player.getItemInHand().getType()))
 				{
 					if(!DCharacter.Util.isCooledDown(character, name, true)) return;
 
 					swarm(player);
 
 					int cooldownMultiplier = (int) (delay * ((double) character.getMeta().getAscensions() / 100));
 					DCharacter.Util.setCoolDown(character, name, System.currentTimeMillis() + cooldownMultiplier * 1000);
 				}
 			}
 
 			@EventHandler(priority = EventPriority.HIGHEST)
 			public void onEntityTargetEntity(EntityTargetLivingEntityEvent targetEvent)
 			{
				if(Demigods.MiscUtil.isDisabledWorld(targetEvent.getEntity().getWorld()) || (targetEvent.getTarget() instanceof Player)) return;
 				if(targetEvent.getEntity() instanceof Zombie && Deity.Util.canUseDeitySilent(DPlayer.Util.getPlayer((Player) targetEvent.getTarget()).getCurrent(), deity)) targetEvent.setCancelled(true);
 			}
 		};
 	}
 
 	@Override
 	public BukkitRunnable getRunnable()
 	{
 		return null;
 	}
 
 	public static class Util
 	{
 		public static boolean spawnZombie(Player player, LivingEntity target)
 		{
 			if(!player.getWorld().equals(target.getWorld())) return false;
 
 			Location toHit = target.getLocation();
 			if(Battle.Util.canTarget(target)) return false;
 			if(Demigods.MiscUtil.isRunningSpigot()) Spigots.playParticle(toHit, Effect.EXPLOSION_HUGE, 1, 1, 1, 1F, 5, 300);
 			Zombie zombie = (Zombie) toHit.getWorld().spawnEntity(toHit, EntityType.ZOMBIE);
 			zombie.addPotionEffects(Sets.newHashSet(new PotionEffect(PotionEffectType.SPEED, 999, 5, false), new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 999, 5, false), new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 999, 2, false)));
 			zombie.setTarget(target);
 
 			return true;
 		}
 	}
 }
