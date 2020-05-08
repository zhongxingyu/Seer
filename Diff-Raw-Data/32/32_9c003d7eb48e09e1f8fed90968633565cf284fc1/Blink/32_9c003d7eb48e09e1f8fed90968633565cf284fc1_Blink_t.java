 package TotalWizard.Handlers.Magic.Spells.Utility;
 
 import java.util.Random;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
 import org.bukkit.material.MaterialData;
 
 import TotalWizard.TotalWizard;
 import TotalWizard.Handlers.EffectHandlers.ParticleEffects;
 import TotalWizard.Handlers.Magic.MagicHandler.SpellType;
 import TotalWizard.Handlers.Magic.Spells.Spell;
 
 public class Blink extends Spell
 {
 
 	@Override
 	public String getName()
 	{
 		return "Blink";
 	}
 
 	@Override
 	public boolean Cast(Player Player)
 	{
 		Location Eyes = Player.getTargetBlock(null, (int) (20 + TotalWizard.MagicHandler.getBonus(SpellType.Teleport, Player.getName()))).getLocation();
 		ParticleEffects PE = ParticleEffects.LARGE_SMOKE;
 		try
 		{
			Location Tele = new Location(Player.getWorld(),Eyes.getX(),Eyes.getY() + 1,Eyes.getZ(),Player.getLocation().getPitch(),Player.getLocation().getYaw());
 			PE.sendToAll(Player.getLocation(), new Random().nextFloat(), 10 + new Random().nextInt(10));
 			Player.teleport(Tele, TeleportCause.PLUGIN);
 			PE.sendToAll(Eyes, new Random().nextFloat(), 10 + new Random().nextInt(10));
 			return true;
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	@Override
 	public int getLevelRequirement()
 	{
 		return 12;
 	}
 
 	@Override
 	public int getManaRequirement()
 	{
 		return 7;
 	}
 
 	@Override
 	public int getCastExp()
 	{
 		return 10;
 	}
 
 	@Override
 	public String[] getDescription()
 	{
 		return new String[] {ChatColor.YELLOW + "Rip the threads of space and time",ChatColor.YELLOW + "sending yourself to where you desire",ChatColor.RED + "Requires level 12 magic, costs 7 mana" };
 	}
 	
 	@Override
 	public MaterialData getMaterialData()
 	{
 		return new MaterialData(Material.INK_SACK, (byte)11);
 	}
 
 }
