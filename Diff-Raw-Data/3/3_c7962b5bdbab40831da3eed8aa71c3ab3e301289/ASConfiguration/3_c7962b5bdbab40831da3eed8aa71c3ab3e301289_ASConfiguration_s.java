 /*
  * AShops Bukkit Plugin
  * Copyright 2013 Austin Reuter (_austinho)
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package pl.austindev.ashops;
 
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.util.Collections;
 import java.util.Locale;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.event.inventory.InventoryType;
 
 import pl.austindev.mc.PluginConfiguration;
 
 public class ASConfiguration extends PluginConfiguration {
 	private final Set<Material> forbiddenItems = Collections
 			.newSetFromMap(new ConcurrentHashMap<Material, Boolean>());
 
 	public ASConfiguration(AShops plugin) {
 		super(plugin);
 	}
 
 	public Locale getLanguage() {
 		Locale locale = null;
 		String language = getString(ASConfigurationPath.LANGUAGE);
 		if (language != null) {
 			locale = new Locale(language);
 		}
		return locale != null && locale.getCountry().length() > 0 ? locale
				: Locale.ENGLISH;
 	}
 
 	public int getCapacity() {
 		int capacity = getInt(ASConfigurationPath.CAPACITY);
 		if (capacity < 1)
 			capacity = 1;
 		else if (capacity > 4)
 			capacity = 4;
 		return capacity * 64 * InventoryType.CHEST.getDefaultSize();
 	}
 
 	public BigDecimal getPrice(Player player) {
 		double price = -1;
 		double groupPrice;
 		for (String group : getPlugin().getPermissionsProvider().getGroups(
 				player)) {
 			groupPrice = getDouble(ASConfigurationPath.SHOP_PRICE, group);
 			if (groupPrice > price)
 				price = groupPrice;
 		}
 		return price >= 0.01 ? new BigDecimal(price) : BigDecimal.ZERO;
 	}
 
 	public int getShopsLimit(Player player) {
 		int limit = -1;
 		int groupLimit;
 		for (String group : getPlugin().getPermissionsProvider().getGroups(
 				player)) {
 			groupLimit = getInt(ASConfigurationPath.SHOPS_LIMIT, group);
 			if (groupLimit > limit)
 				limit = groupLimit;
 		}
 		return limit >= 0 ? limit : 0;
 	}
 
 	public String getServerAccountName() {
 		return getString(ASConfigurationPath.SERVER_ACCOUNT_NAME);
 	}
 
 	public Set<Material> getForbiddenItems() {
 		if (forbiddenItems.size() < 1) {
 			for (String typeName : getStringList(ASConfigurationPath.FORBIDDEN_ITEMS)) {
 				Material type = Material.getMaterial(typeName);
 				if (type != null)
 					forbiddenItems.add(type);
 			}
 		}
 		return forbiddenItems;
 	}
 
 	public BigDecimal getMinimalPrice(Material type) {
 		try {
 			String price = getString(ASConfigurationPath.MINIMAL_PRICE,
 					type.name());
 			return price != null ? new BigDecimal(price).setScale(2,
 					RoundingMode.HALF_EVEN) : BigDecimal.ZERO;
 		} catch (NumberFormatException e) {
 			return BigDecimal.ZERO;
 		}
 	}
 
 	public void reload() {
 		forbiddenItems.clear();
 		getPlugin().reloadConfig();
 		String currency = getPlugin().getEconomyProvider().getCurrency();
 		if (currency != null && currency.length() > 0)
 			OffersUtils.setCurrency(currency);
 	}
 
 	public int getTax(String account, String worldName) {
 		int tax = 0;
 		for (String group : getPlugin().getPermissionsProvider().getGroups(
 				account, Bukkit.getWorld(worldName))) {
 			int groupTax = getInt(ASConfigurationPath.TAX, group);
 			if (groupTax < tax || tax == 0)
 				tax = groupTax;
 		}
 		return tax;
 	}
 
 	public boolean shouldSendNotification() {
 		Boolean send = getBoolean(ASConfigurationPath.NOTIIFICATIONS);
 		return send != null ? send : true;
 	}
 
 	public int getTransactionDayLimit() {
 		int limit = getInt(ASConfigurationPath.TRANSACTIONS_DAYS_LIMIT);
 		return limit > 0 ? limit : 5;
 	}
 }
