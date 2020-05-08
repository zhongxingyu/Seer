 package no.runsafe.framework.server.player;
 
 import no.runsafe.framework.hook.IPlayerDataProvider;
 import no.runsafe.framework.hook.IPlayerNameDecorator;
 import no.runsafe.framework.hook.IPlayerPermissions;
 import no.runsafe.framework.hook.IPlayerVisibility;
 import no.runsafe.framework.output.ChatColour;
 import no.runsafe.framework.server.RunsafeLocation;
 import no.runsafe.framework.server.RunsafeWorld;
 import no.runsafe.framework.server.block.RunsafeBlock;
 import no.runsafe.framework.server.entity.RunsafeLivingEntity;
 import no.runsafe.framework.server.event.player.RunsafeOperatorEvent;
 import no.runsafe.framework.server.inventory.IInventoryHolder;
 import no.runsafe.framework.server.inventory.RunsafeInventory;
 import no.runsafe.framework.server.item.RunsafeItemStack;
 import org.bukkit.GameMode;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.entity.Player;
 import org.bukkit.potion.PotionEffect;
 import org.joda.time.DateTime;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 
 public class RunsafePlayer extends RunsafeLivingEntity implements IInventoryHolder
 {
 	public RunsafePlayer(Player toWrap)
 	{
 		super(toWrap);
 		player = toWrap;
 		basePlayer = toWrap;
 	}
 
 	public RunsafePlayer(OfflinePlayer toWrap)
 	{
 		super((toWrap instanceof Player) ? (Player) toWrap : null);
 		if (toWrap instanceof Player)
 			player = (Player) toWrap;
 		else
 			player = null;
 		basePlayer = toWrap;
 	}
 
 	public String getName()
 	{
 		return basePlayer.getName();
 	}
 
 	public String getPrettyName()
 	{
 		String name = getName();
 		if (!decoratorHooks.isEmpty())
 			for (IPlayerNameDecorator decorator : decoratorHooks)
 				name = decorator.DecorateName(this, name);
 		return name;
 	}
 
 	public boolean hasPlayedBefore()
 	{
 		return basePlayer.hasPlayedBefore();
 	}
 
 	public boolean isOnline()
 	{
 		return basePlayer.isOnline();
 	}
 
 	public void setPlayerListName(String playerName)
 	{
 		if (playerName.length() > 16)
 			this.player.setPlayerListName(playerName.substring(0, 16));
 		else
			this.player.setPlayerListName(playerName.substring(0, 16));
 	}
 
 	public boolean isOP()
 	{
 		return basePlayer.isOp();
 	}
 
 	public void OP()
 	{
 		basePlayer.setOp(true);
 		new RunsafeOperatorEvent(this, true).Fire();
 	}
 
 	public void deOP()
 	{
 		basePlayer.setOp(false);
 		new RunsafeOperatorEvent(this, false).Fire();
 	}
 
 	public boolean isSurvivalist()
 	{
 		return player.getGameMode().equals(GameMode.SURVIVAL);
 	}
 
 	public boolean isCreative()
 	{
 		return player.getGameMode().equals(GameMode.CREATIVE);
 	}
 
 	public boolean isAdventurer()
 	{
 		return player.getGameMode().equals(GameMode.ADVENTURE);
 	}
 
 	public boolean isWhitelisted()
 	{
 		return basePlayer.isWhitelisted();
 	}
 
 	public boolean isBanned()
 	{
 		return basePlayer.isBanned();
 	}
 
 	public void setBanned(boolean banned)
 	{
 		basePlayer.setBanned(banned);
 	}
 
 	public void kick(String reason)
 	{
 		player.kickPlayer(reason);
 	}
 
 	public float getXP()
 	{
 		if (player == null)
 			return 0;
 
 		return player.getExp();
 	}
 
 	public void setXP(float points)
 	{
 		if (player != null)
 			player.setExp(points);
 	}
 
 	public int getLevel()
 	{
 		if (player == null)
 			return 0;
 
 		return player.getLevel();
 	}
 
 	public void setLevel(int level)
 	{
 		if (player != null)
 			player.setLevel(level);
 	}
 
 	public void sendBlockChange(RunsafeBlock block, byte data)
 	{
 		sendBlockChange(block.getLocation(), block.getTypeId(), data);
 	}
 
 	public void sendBlockChange(RunsafeLocation location, int itemId, byte data)
 	{
 		if (player != null)
 			player.sendBlockChange(location.getRaw(), itemId, data);
 	}
 
 	public void teleport(RunsafeWorld world, double x, double y, double z)
 	{
 		teleport(new RunsafeLocation(world, x, y, z));
 	}
 
 	public RunsafeItemStack getItemInHand()
 	{
 		if (player != null)
 			return new RunsafeItemStack(player.getItemInHand());
 
 		return null;
 	}
 
 	public void sendMessage(String message)
 	{
 		if (player != null)
 			player.sendMessage(message);
 	}
 
 	public void sendColouredMessage(String message)
 	{
 		sendMessage(ChatColour.ToMinecraft(message));
 	}
 
 	public void sendColouredMessage(String format, Object... params)
 	{
 		sendColouredMessage(String.format(format, params));
 	}
 
 	public Player getRawPlayer()
 	{
 		return this.player;
 	}
 
 	public RunsafeInventory getInventory()
 	{
 		if (player != null)
 			return new RunsafeInventory(player.getInventory());
 
 		return null;
 	}
 
 	@SuppressWarnings("deprecation")
 	public void updateInventory()
 	{
 		if (player != null)
 			player.updateInventory();
 	}
 
 	public void removeBuffs()
 	{
 		for (PotionEffect effect : player.getActivePotionEffects())
 			player.removePotionEffect(effect.getType());
 	}
 
 	public boolean hasPermission(String permission)
 	{
 		return player != null && player.hasPermission(permission);
 	}
 
 	public HashMap<String, String> getData()
 	{
 		HashMap<String, String> results = getBasicData();
 		for (IPlayerDataProvider provider : dataHooks)
 		{
 			HashMap<String, String> data = provider.GetPlayerData(this);
 			if (data != null)
 				results.putAll(data);
 		}
 		return results;
 	}
 
 	public HashMap<String, String> getBasicData()
 	{
 		LinkedHashMap<String, String> data = new LinkedHashMap<String, String>();
 		if (isOnline())
 		{
 			data.put("game.ip",
 				String.format("%s [%s]",
 					player.getAddress().getAddress().getHostAddress(),
 					player.getAddress().getHostName()
 				)
 			);
 			data.put("game.mode", player.getGameMode().name());
 			data.put("game.flying", player.isFlying() ? "true" : "false");
 			data.put("game.health", String.format("%d/%d", getHealth(), getMaxHealth()));
 		}
 		data.put("game.experience", String.format("%.1f", getXP()));
 		data.put("game.level", String.format("%d", getLevel()));
 		data.put("game.op", isOP() ? "true" : "false");
 		return data;
 	}
 
 	public DateTime lastLogout()
 	{
 		if (this.isOnline() || dataHooks.isEmpty())
 			return null;
 		DateTime logout = null;
 		for (IPlayerDataProvider provider : dataHooks)
 		{
 			DateTime value = provider.GetPlayerLogout(this);
 			if (value != null && (logout == null || value.isAfter(logout)))
 				logout = value;
 		}
 		return logout;
 	}
 
 	public String getBanReason()
 	{
 		if (!this.isBanned() || dataHooks.isEmpty())
 			return null;
 		for (IPlayerDataProvider provider : dataHooks)
 		{
 			String reason = provider.GetPlayerBanReason(this);
 			if (reason != null)
 				return reason;
 		}
 		return null;
 	}
 
 	public String getDataValue(String key)
 	{
 		HashMap<String, String> data = getData();
 		if (data.containsKey(key))
 			return data.get(key);
 		return null;
 	}
 
 	public boolean canSee(RunsafePlayer target)
 	{
 		if (visibilityHooks.isEmpty())
 			return true;
 		for (IPlayerVisibility check : visibilityHooks)
 			if (!check.canPlayerASeeB(this, target))
 				return false;
 		return true;
 	}
 
 	public boolean isVanished()
 	{
 		if (visibilityHooks.isEmpty())
 			return false;
 		for (IPlayerVisibility check : visibilityHooks)
 			if (check.isPlayerVanished(this))
 				return true;
 		return false;
 	}
 
 	public List<String> getGroups()
 	{
 		ArrayList<String> result = new ArrayList<String>();
 		for (IPlayerPermissions hook : permissionHooks)
 		{
 			List<String> groups = hook.getUserGroups(this);
 			if (groups != null)
 				result.addAll(groups);
 		}
 		return result;
 	}
 
 	public static final ArrayList<IPlayerDataProvider> dataHooks = new ArrayList<IPlayerDataProvider>();
 	public static final ArrayList<IPlayerVisibility> visibilityHooks = new ArrayList<IPlayerVisibility>();
 	public static final ArrayList<IPlayerPermissions> permissionHooks = new ArrayList<IPlayerPermissions>();
 	public static final ArrayList<IPlayerNameDecorator> decoratorHooks = new ArrayList<IPlayerNameDecorator>();
 	private final Player player;
 	private final OfflinePlayer basePlayer;
 }
