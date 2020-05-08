 package loecraftpack.ponies.abilities.active;
 
 import java.io.DataInputStream;
 import java.io.IOException;
 
 import loecraftpack.enums.Race;
 import loecraftpack.packet.PacketHelper;
 import loecraftpack.packet.PacketIds;
 import loecraftpack.ponies.abilities.Ability;
 import loecraftpack.ponies.abilities.AbilityPlayerData;
 import loecraftpack.ponies.abilities.ActiveAbility;
 import loecraftpack.ponies.abilities.projectiles.Fireball;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.world.World;
 import cpw.mods.fml.common.network.PacketDispatcher;
 import cpw.mods.fml.common.network.Player;
 
 public class AbilityFireball extends ActiveAbility
 {
 	public AbilityFireball()
 	{
 		super("Fireball", Race.UNICORN, 100, 3, 1);
 	}
 
 	@Override
 	protected boolean CastSpellClient(EntityPlayer player, World world)
 	{
 		int attemptID = AbilityPlayerData.attemptUse(energyCost);
 		PacketDispatcher.sendPacketToServer(PacketHelper.Make("loecraftpack", PacketIds.useAbility, Ability.Fireball, attemptID));
 		return true;
 	}
 
 	@Override
 	public void CastSpellServer(Player player, AbilityPlayerData abilityData, DataInputStream data) throws IOException
 	{
 		EntityPlayer sender = (EntityPlayer) player;
 		int attemptID = data.readInt();
 		energyCost = (int)(this.getEnergyCost(sender));
		System.out.println("FireBall: "+energyCost+" "+abilityData.energy);
 		if(abilityData.energy>=energyCost)
 		{
 			Fireball fireball = new Fireball(sender.worldObj, sender, sender.getLookVec().xCoord/10f, sender.getLookVec().yCoord/10f, sender.getLookVec().zCoord/10f);
 			sender.worldObj.spawnEntityInWorld(fireball);
 			abilityData.addEnergy(-energyCost);
 			PacketDispatcher.sendPacketToPlayer(PacketHelper.Make("loecraftpack", PacketIds.useAbility, attemptID, energyCost), player);
 		}
 		else
 		{
 			PacketDispatcher.sendPacketToPlayer(PacketHelper.Make("loecraftpack", PacketIds.useAbility, attemptID, 0), player);
 		}
 	}
 }
