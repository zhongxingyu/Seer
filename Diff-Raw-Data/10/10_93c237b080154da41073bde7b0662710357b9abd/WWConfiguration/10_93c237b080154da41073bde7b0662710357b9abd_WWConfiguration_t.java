 package com.github.igp.WoolWires;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.DyeColor;
 import org.bukkit.Material;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.github.igp.IGHelpers.MaterialHelper;
 
 public class WWConfiguration
 {
 	@SuppressWarnings("unused")
 	private final JavaPlugin plugin;
 	private final MaterialHelper materialHelper;
 	private final FileConfiguration config;
 	private List<WireConfiguration> wireConfigs;
 	private WireConfiguration defaultWireConfiguration;
 	private Byte inputColor;
 
 	public WWConfiguration(final JavaPlugin plugin)
 	{
 		this.plugin = plugin;
 		materialHelper = new MaterialHelper();
		
 		final File configFile = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "config.yml");
 		if ((configFile == null) || !configFile.exists())
 		{
 			plugin.getLogger().info("Configuration file not found: saving default");
			plugin.saveConfig();
 		}
		
 		config = plugin.getConfig();
		
 		load();
 	}
 
 	private void load()
 	{
 		wireConfigs = new ArrayList<WireConfiguration>();
 
 		inputColor = stringToColor(config.getString("InputColor"));
 		if (inputColor == null)
 			inputColor = stringToColor("brown");
 
 		{
 			final byte color = -1;
 			final int type = config.getInt("Wires.Default.Type");
 			final int maxSize = config.getInt("Wires.Default.MaxSize");
 			List<Material> validMechanisms = new ArrayList<Material>(8);
 			if (config.getString("Wires.Default.Allowed").equalsIgnoreCase("ALL"))
 				validMechanisms = defaultValidMechanisms();
 			else
 			{
 				for (final String s : config.getStringList("Wires.Default.Allowed"))
 				{
 					final Material material = materialHelper.getMaterialFromString(s);
 					if ((material != null) && !validMechanisms.contains(material))
 						validMechanisms.add(material);
 				}
 			}
 			if (validMechanisms.size() == 0)
 				validMechanisms = defaultValidMechanisms();
 			defaultWireConfiguration = new WireConfiguration(color, type, maxSize, validMechanisms);
 		}
 
 		for (final String s : config.getConfigurationSection("Wires").getKeys(false))
 		{
 			if (s.equalsIgnoreCase("Default"))
 				continue;
 
 			final Byte color = stringToColor(s);
 			if ((color == null) || (color == inputColor))
 				continue;
 			boolean toContinue = false;
 			for (final WireConfiguration wc : wireConfigs)
 			{
 				if (wc.getColor() == color)
 				{
 					toContinue = true;
 					continue;
 				}
 			}
 			if (toContinue)
 				continue;
 
 			Integer type = config.getInt("Wires." + s + ".Type");
 			if (type == null)
 				type = defaultWireConfiguration.getType();
 
 			Integer maxSize = config.getInt("Wires." + s + ".MaxSize");
 			if (maxSize == null)
 				maxSize = defaultWireConfiguration.getMaxSize();
 
 			List<Material> validMechanisms = new ArrayList<Material>(8);
 			if (config.getString("Wires." + s + ".Allowed").equalsIgnoreCase("ALL"))
 				validMechanisms = defaultValidMechanisms();
 			else
 			{
 				for (final String sm : config.getStringList("Wires." + s + ".Allowed"))
 				{
 
 					final Material material = materialHelper.getMaterialFromString(sm);
 					if ((material != null) && !validMechanisms.contains(material))
 						validMechanisms.add(material);
 				}
 			}
 
 			if (validMechanisms.size() == 0)
 				validMechanisms = defaultWireConfiguration.getValidMechanisms();
 
 			wireConfigs.add(new WireConfiguration(color, type, maxSize, validMechanisms));
 		}
 	}
 
 	public byte getInputColor()
 	{
 		return inputColor;
 	}
 
 	public WireConfiguration getWireConfiguration(final byte color)
 	{
 		for (final WireConfiguration wc : wireConfigs)
 		{
 			if (wc.getColor() == color)
 				return wc;
 		}
 
 		return defaultWireConfiguration;
 	}
 
 	public class WireConfiguration
 	{
 		private final byte color;
 		private final int type;
 		private final int maxSize;
 		private final List<Material> validMechanisms;
 
 		WireConfiguration(final byte color, final int type, final int maxSize, final List<Material> validMechanisms)
 		{
 			this.color = color;
 			this.type = type;
 			this.maxSize = maxSize;
 			this.validMechanisms = validMechanisms;
 		}
 
 		public final byte getColor()
 		{
 			return color;
 		}
 
 		public final byte getInputColor()
 		{
 			return inputColor;
 		}
 
 		public final List<Material> getValidMechanisms()
 		{
 			return validMechanisms;
 		}
 
 		public final int getType()
 		{
 			return type;
 		}
 
 		public final int getMaxSize()
 		{
 			return maxSize;
 		}
 	}
 
 	private final List<Material> defaultValidMechanisms()
 	{
 		final List<Material> validMechanisms = new ArrayList<Material>(8);
 
 		validMechanisms.add(Material.LEVER);
 		validMechanisms.add(Material.FENCE_GATE);
 		validMechanisms.add(Material.TRAP_DOOR);
 		validMechanisms.add(Material.GLOWSTONE);
 		validMechanisms.add(Material.GLASS);
 		validMechanisms.add(Material.NOTE_BLOCK);
 		validMechanisms.add(Material.DISPENSER);
 		validMechanisms.add(Material.WOODEN_DOOR);
 
 		return validMechanisms;
 	}
 
 	private final Byte stringToColor(final String string)
 	{
 		try
 		{
 			final DyeColor color = DyeColor.getByData((byte) Integer.parseInt(string));
 			if (color != null)
 				return color.getData();
 		}
 		catch (final NumberFormatException ex)
 		{
 		}
 
 		if (string.equalsIgnoreCase("WHITE"))
 			return DyeColor.WHITE.getData();
 
 		if (string.equalsIgnoreCase("ORANGE"))
 			return DyeColor.ORANGE.getData();
 
 		if (string.equalsIgnoreCase("MAGENTA"))
 			return DyeColor.MAGENTA.getData();
 
 		if (string.equalsIgnoreCase("LIGHT_BLUE"))
 			return DyeColor.LIGHT_BLUE.getData();
 
 		if (string.equalsIgnoreCase("YELLOW"))
 			return DyeColor.YELLOW.getData();
 
 		if (string.equalsIgnoreCase("LIME"))
 			return DyeColor.LIME.getData();
 
 		if (string.equalsIgnoreCase("PINK"))
 			return DyeColor.PINK.getData();
 
 		if (string.equalsIgnoreCase("GRAY") || string.equalsIgnoreCase("GREY"))
 			return DyeColor.GRAY.getData();
 
 		if (string.equalsIgnoreCase("LIGHT_GRAY") || string.equalsIgnoreCase("LIGHT_GREY") || string.equalsIgnoreCase("SILVER"))
 			return DyeColor.SILVER.getData();
 
 		if (string.equalsIgnoreCase("CYAN"))
 			return DyeColor.CYAN.getData();
 
 		if (string.equalsIgnoreCase("PURPLE"))
 			return DyeColor.PURPLE.getData();
 
 		if (string.equalsIgnoreCase("BLUE"))
 			return DyeColor.BLUE.getData();
 
 		if (string.equalsIgnoreCase("BROWN"))
 			return DyeColor.BROWN.getData();
 
 		if (string.equalsIgnoreCase("GREEN"))
 			return DyeColor.GREEN.getData();
 
 		if (string.equalsIgnoreCase("RED"))
 			return DyeColor.RED.getData();
 
 		if (string.equalsIgnoreCase("BLACK"))
 			return DyeColor.BLACK.getData();
 
 		return null;
 	}
 }
