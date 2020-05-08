 package com.censoredsoftware.demigods.greek.ability.passive;
 
 import com.censoredsoftware.demigods.engine.data.DCharacter;
 import com.censoredsoftware.demigods.engine.util.Zones;
 import com.censoredsoftware.demigods.greek.ability.GreekAbility;
 import com.censoredsoftware.demigods.greek.ability.ultimate.Discoball;
 import com.google.common.collect.Lists;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 
 import java.util.List;
 
 public class RainbowWalking extends GreekAbility.Passive
 {
 	private final static String name = "Rainbow Walking";
 	private final static int repeat = 5;
 	private final static List<String> details = Lists.newArrayList("Spread the disco while sneaking.");
 
 	public RainbowWalking(final String deity)
 	{
 		super(name, deity, repeat, details, null, new Runnable()
 		{
 			@Override
 			public void run()
 			{
				for(DCharacter online : DCharacter.Util.getOnlineCharactersWithDeity("DrD1sco"))
 				{
 					Player player = online.getOfflinePlayer().getPlayer();
 					if(Zones.inNoDemigodsZone(player.getLocation()) || !player.isSneaking() || player.isFlying() || Zones.inNoPvpZone(player.getLocation()) || Zones.inNoBuildZone(player, player.getLocation())) continue;
 					doEffect(player);
 				}
 			}
 
 			private void doEffect(Player player)
 			{
 				for(Entity entity : player.getNearbyEntities(30, 30, 30))
 					if(entity instanceof Player) Discoball.rainbow(player, (Player) entity);
 				Discoball.rainbow(player, player);
 				Discoball.playRandomNote(player.getLocation(), 0.5F);
 			}
 		});
 	}
 }
