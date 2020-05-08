 /************************************************************************
  * This file is part of CreativePlus.
  *
  * CreativePlus is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * ExamplePlugin is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with CreativePlus.  If not, see <http://www.gnu.org/licenses/>.
  ************************************************************************/
 
 package de.Lathanael.CP.CreativePlus;
 
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.plugin.PluginDescriptionFile;
 
 /**
  * @author Lathanael
  * @author Balor
  *
  */
 public enum CPConfigEnum {
 
 	DROPITEMS("PlayersCanDropItems", false, "If set to true all players are allowed to drop any item/block."),
 	SEP_INV("SeperateInventories", true, "If set to true each player has a inventory used for CreativeMode and one for SurvivalMode."),
 	WROLDS("CreativeWorlds", Collections.list(new Enumeration<String>() {
 		private int count = 0;
 		private final String[] val = new String[] { "Creative1", "Creative2", "Creative3" };
 
 		@Override
 		public boolean hasMoreElements() {
 			return count < val.length;
 		}
 
 		@Override
 		public String nextElement() {
 			return val[count++];
 		}
 	}), "The worlds in which CreativePlus will be active"),
 	BREAK_LIST("BreakBlacklist", Collections.list(new Enumeration<Integer>() {
 		private int count = 0;
 		private final Integer[] val = new Integer[] {7, 54, 63, 64, 68, 69, 71, 77, 93, 94, 96};
 
 		@Override
 		public boolean hasMoreElements() {
 			return count < val.length;
 		}
 
 		@Override
 		public Integer nextElement() {
 			return val[count++];
 		}
 	}), "Blocks/Items which can not be destroyed by a player while beeing in creative-mode."),
 	PLACE_LIST("PlaceBlacklist", Collections.list(new Enumeration<Integer>() {
 		private int count = 0;
 		private final Integer[] val = new Integer[] {7, 8, 9, 10, 11};
 
 		@Override
 		public boolean hasMoreElements() {
 			return count < val.length;
 		}
 
 		@Override
 		public Integer nextElement() {
 			return val[count++];
 		}
 	}), "Blocks which are forbidden to be placed.");
 
 	private static ConfigurationSection pluginConfig;
 	private static String pluginVersion;
 	private static String pluginName;
 	private final String path, desc;
 	private final Object value;
 
 	private CPConfigEnum(String path, Object value, String desc) {
 		this.path = path;
 		this.desc = desc;
 		this.value = value;
 	}
 
 	public String getString() {
 		return pluginConfig.getString(path);
 	}
 
 	public int getInt() {
 		return pluginConfig.getInt(path);
 	}
 
 	public double getDouble() {
 		return pluginConfig.getDouble(path);
 	}
 
 	public boolean getBoolean() {
 		return pluginConfig.getBoolean(path);
 	}
 
 	public long getLong() {
 		return pluginConfig.getLong(path);
 	}
 
 	public float getFloat() {
 		return Float.parseFloat(pluginConfig.getString(path));
 	}
 
 	public List<String> getStringList() {
 		return pluginConfig.getStringList(path);
 	}
 
 	public List<Integer> getIntList() {
 		return pluginConfig.getIntegerList(path);
 	}
 
 	/**
 	 * @return the defaultvalues
 	 */
 	public static Map<String, Object> getDefaultvalues() {
 		Map<String, Object> values = new LinkedHashMap<String, Object>();
 		for (CPConfigEnum ce : values())
 			values.put(ce.path, ce.value);
 		return values;
 	}
 
 	public static String getHeader() {
 		StringBuffer buffer = new StringBuffer();
 		buffer.append("pluginConfiguration file of ").append(pluginName).append('\n');
 		buffer.append("Plugin Version: ").append(pluginVersion).append('\n').append('\n');
 		for (CPConfigEnum ce : values())
 			buffer.append(ce.path).append("\t:\t").append(ce.desc).append(" (Default : ")
 					.append(ce.value).append(')').append('\n');
 		return buffer.toString();
 	}
 
 	/**
 	 * @param pluginConfig
 	 *            the pluginConfig to set
 	 */
 	public static void setPluginConfig(ConfigurationSection config) {
 		CPConfigEnum.pluginConfig = config;
 	}
 
 	public static void setPluginInfos(PluginDescriptionFile pdf) {
 		CPConfigEnum.pluginVersion = pdf.getVersion();
 		CPConfigEnum.pluginName = pdf.getName();
 	}
 }
