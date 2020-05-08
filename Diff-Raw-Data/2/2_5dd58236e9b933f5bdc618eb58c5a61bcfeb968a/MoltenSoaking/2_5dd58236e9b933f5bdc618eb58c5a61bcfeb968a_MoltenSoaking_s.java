 package no.runsafe.itemflangerorimega.tools.enchants;
 
 import no.runsafe.framework.api.ILocation;
 import no.runsafe.framework.api.block.IBlock;
 import no.runsafe.framework.api.player.IPlayer;
 import no.runsafe.framework.minecraft.Item;
 import no.runsafe.framework.minecraft.Sound;
 import no.runsafe.framework.minecraft.WorldEffect;
 import no.runsafe.framework.minecraft.item.meta.RunsafeMeta;
 import no.runsafe.itemflangerorimega.tools.CustomToolEnchant;
 
 public class MoltenSoaking extends CustomToolEnchant
 {
 	@Override
 	public String getEnchantText()
 	{
 		return "Molten Soaking I";
 	}
 
 	@Override
 	public String getSimpleName()
 	{
 		return "molten_soaking";
 	}
 
 	@Override
 	public boolean onUse(IPlayer player, RunsafeMeta item, IBlock rightClicked)
 	{
 		ILocation location = rightClicked == null ? player.getLocation() : rightClicked.getLocation();
 
 		if (location != null)
 		{
			item.remove(1);
 			location.playEffect(WorldEffect.PORTAL, 0.5F, 100, 10F);
 			location.playSound(Sound.Portal.Trigger, 5F, 2F);
 
 			removeBlock(location, 0, 0);
 
 			removeBlock(location, 1, 0);
 			removeBlock(location, 1, 1);
 			removeBlock(location, 0, 1);
 
 			removeBlock(location, -1, 0);
 			removeBlock(location, -1, -1);
 			removeBlock(location, 0, -1);
 
 			removeBlock(location, -1, 1);
 			removeBlock(location, 1, -1);
 
 		}
 		return true;
 	}
 
 	private void removeBlock(ILocation location, int offsetX, int offsetZ)
 	{
 		removeLava(location, offsetX, 0, offsetZ);
 		removeLava(location, offsetX, 1, offsetZ);
 		removeLava(location, offsetX, 2, offsetZ);
 		removeLava(location, offsetX, -1, offsetZ);
 		removeLava(location, offsetX, -2, offsetZ);
 	}
 
 	private void removeLava(ILocation location, int offsetX, int offsetY, int offsetZ)
 	{
 		ILocation newLocation = location.clone();
 		newLocation.offset(offsetX, offsetY, offsetZ);
 
 		IBlock block = newLocation.getBlock();
 		if (block != null && (block.is(Item.Unavailable.Lava) || block.is(Item.Unavailable.StationaryLava)))
 			block.set(Item.Unavailable.Air);
 	}
 }
