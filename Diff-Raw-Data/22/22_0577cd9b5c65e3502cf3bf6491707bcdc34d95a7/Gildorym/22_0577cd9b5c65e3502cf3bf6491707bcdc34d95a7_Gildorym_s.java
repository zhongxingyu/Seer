 package com.gildorymrp.api;
 
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.gildorymrp.api.plugin.chat.GildorymChatPlugin;
 import com.gildorymrp.api.plugin.classes.GildorymClassesPlugin;
 import com.gildorymrp.api.plugin.core.GildorymCorePlugin;
import com.gildorymrp.api.plugin.death.GildorymDeathPlugin;
 import com.gildorymrp.api.plugin.deities.GildorymDeitiesPlugin;
 import com.gildorymrp.api.plugin.donator.GildorymDonatorPlugin;
 import com.gildorymrp.api.plugin.dungeon.GildorymDungeonPlugin;
 import com.gildorymrp.api.plugin.economy.GildorymEconomyPlugin;
 import com.gildorymrp.api.plugin.equipment.GildorymEquipmentPlugin;
 import com.gildorymrp.api.plugin.essentials.GildorymEssentialsPlugin;
 import com.gildorymrp.api.plugin.lock.GildorymLockPlugin;
 import com.gildorymrp.api.plugin.moderation.GildorymModerationPlugin;
 import com.gildorymrp.api.plugin.multiworld.GildorymMultiworldPlugin;
 import com.gildorymrp.api.plugin.permissions.GildorymPermissionsPlugin;
 import com.gildorymrp.api.plugin.portals.GildorymPortalsPlugin;
 import com.gildorymrp.api.plugin.professions.GildorymProfessionsPlugin;
 import com.gildorymrp.api.plugin.regions.GildorymRegionsPlugin;
 import com.gildorymrp.api.plugin.settlements.GildorymSettlementsPlugin;
 
 /**
  * Main class, allows the obtaining of registered plugins for various services and registering of plugins
  * @author Lucariatias
  *
  */
 public class Gildorym extends JavaPlugin {
 	
 	private static GildorymChatPlugin chatPlugin;
 	private static GildorymClassesPlugin classesPlugin;
 	private static GildorymCorePlugin corePlugin;
	private static GildorymDeathPlugin deathPlugin;
 	private static GildorymDeitiesPlugin deitiesPlugin;
 	private static GildorymDonatorPlugin donatorPlugin;
 	private static GildorymDungeonPlugin dungeonPlugin;
 	private static GildorymEconomyPlugin economyPlugin;
 	private static GildorymEquipmentPlugin equipmentPlugin;
 	private static GildorymEssentialsPlugin essentialsPlugin;
 	private static GildorymLockPlugin lockPlugin;
 	private static GildorymModerationPlugin moderationPlugin;
 	private static GildorymMultiworldPlugin multiworldPlugin;
 	private static GildorymPermissionsPlugin permissionsPlugin;
 	private static GildorymPortalsPlugin portalsPlugin;
 	private static GildorymProfessionsPlugin professionsPlugin;
 	private static GildorymRegionsPlugin regionsPlugin;
 	private static GildorymSettlementsPlugin settlementsPlugin;
 
 	/**
 	 * Gets the active chat plugin on the server
 	 * 
 	 * @return the chat plugin
 	 */
 	public static GildorymChatPlugin getChatPlugin() {
 		return chatPlugin;
 	}
 
 	/**
 	 * Registers a chat plugin to the server
 	 * 
 	 * @param plugin the chat plugin to register
 	 */
 	public static void registerChatPlugin(GildorymChatPlugin plugin) {
 		Gildorym.chatPlugin = plugin;
 	}
 
 	/**
 	 * Gets the active classes plugin on the server
 	 * 
 	 * @return the classes plugin
 	 */
 	public static GildorymClassesPlugin getClassesPlugin() {
 		return classesPlugin;
 	}
 
 	/**
 	 * Registers a classes plugin to the server
 	 * 
 	 * @param plugin the classes plugin to register
 	 */
 	public static void registerClassesPlugin(GildorymClassesPlugin plugin) {
 		Gildorym.classesPlugin = plugin;
 	}
 
 	/**
 	 * Gets the active core plugin on the server
 	 * 
 	 * @return the core plugin
 	 */
 	public static GildorymCorePlugin getCorePlugin() {
 		return corePlugin;
 	}
 
 	/**
 	 * Registers a core plugin to the server
 	 * 
 	 * @param plugin the core plugin to register
 	 */
 	public static void registerCorePlugin(GildorymCorePlugin plugin) {
 		Gildorym.corePlugin = plugin;
 	}
 
 	/**
	 * Gets the active death plugin on the server
	 * 
	 * @return the death plugin
	 */
	public static GildorymDeathPlugin getDeathPlugin() {
		return deathPlugin;
	}

	/**
	 * Registers a death plugin to the server
	 * 
	 * @param plugin the death plugin to register
	 */
	public static void registerDeathPlugin(GildorymDeathPlugin plugin) {
		Gildorym.deathPlugin = plugin;
	}

	/**
 	 * Gets the active deities plugin on the server
 	 * 
 	 * @return the deities plugin
 	 */
 	public static GildorymDeitiesPlugin getDeitiesPlugin() {
 		return deitiesPlugin;
 	}
 
 	/**
 	 * Registers a deities plugin to the server
 	 * 
 	 * @param plugin the deities plugin to register
 	 */
 	public static void registerDeitiesPlugin(GildorymDeitiesPlugin plugin) {
 		Gildorym.deitiesPlugin = plugin;
 	}
 
 	/**
 	 * Gets the active donator plugin on the server
 	 * 
 	 * @return the donator plugin
 	 */
 	public static GildorymDonatorPlugin getDonatorPlugin() {
 		return donatorPlugin;
 	}
 
 	/**
 	 * Registers a donator plugin to the server
 	 * 
 	 * @param plugin the donator plugin to register
 	 */
 	public static void registerDonatorPlugin(GildorymDonatorPlugin plugin) {
 		Gildorym.donatorPlugin = plugin;
 	}
 
 	/**
 	 * Gets the active dungeon plugin on the server
 	 * 
 	 * @return the dungeon plugin
 	 */
 	public static GildorymDungeonPlugin getDungeonPlugin() {
 		return dungeonPlugin;
 	}
 
 	/**
 	 * Registers a dungeon plugin to the server
 	 * 
 	 * @param plugin the plugin to register
 	 */
 	public static void registerDungeonPlugin(GildorymDungeonPlugin plugin) {
 		Gildorym.dungeonPlugin = plugin;
 	}
 
 	/**
 	 * Gets the active economy plugin on the server
 	 * 
 	 * @return the economy plugin
 	 */
 	public static GildorymEconomyPlugin getEconomyPlugin() {
 		return economyPlugin;
 	}
 
 	/**
 	 * Registers an economy plugin to the server
 	 * 
 	 * @param plugin the economy plugin to register
 	 */
 	public static void registerEconomyPlugin(GildorymEconomyPlugin plugin) {
 		Gildorym.economyPlugin = plugin;
 	}
 	
 	/**
 	 * Gets the active equipment plugin on the server
 	 * 
 	 * @return the equipment plugin
 	 */
 	public static GildorymEquipmentPlugin getEquipmentPlugin() {
 		return equipmentPlugin;
 	}
 
 	/**
 	 * Registers an equipment plugin to the server
 	 * 
 	 * @param plugin the equipment plugin to register
 	 */
 	public static void registerEquipmentPlugin(GildorymEquipmentPlugin plugin) {
 		Gildorym.equipmentPlugin = plugin;
 	}
 
 	/**
 	 * Gets the active essentials plugin on the server
 	 * 
 	 * @return the essentials plugin
 	 */
 	public static GildorymEssentialsPlugin getEssentialsPlugin() {
 		return essentialsPlugin;
 	}
 	
 	/**
 	 * Registers an essentials plugin to the server
 	 * 
 	 * @param plugin the plugin to register
 	 */
 	public static void registerEssentialsPlugin(GildorymEssentialsPlugin plugin) {
 		Gildorym.essentialsPlugin = plugin;
 	}
 	
 	/**
 	 * Gets the active lock plugin on the server
 	 * 
 	 * @return the lock plugin
 	 */
 	public static GildorymLockPlugin getLockPlugin() {
 		return lockPlugin;
 	}
 
 	/**
 	 * Registers a lock plugin to the server
 	 * 
 	 * @param plugin the lock plugin to register
 	 */
 	public static void registerLockPlugin(GildorymLockPlugin plugin) {
 		Gildorym.lockPlugin = plugin;
 	}
 
 	/**
 	 * Gets the active moderation plugin on the server
 	 * 
 	 * @return the moderation plugin
 	 */
 	public static GildorymModerationPlugin getModerationPlugin() {
 		return moderationPlugin;
 	}
 
 	/**
 	 * Registers a moderation plugin to the server
 	 * 
 	 * @param plugin the plugin to register
 	 */
 	public static void registerModerationPlugin(GildorymModerationPlugin plugin) {
 		Gildorym.moderationPlugin = plugin;
 	}
 
 	/**
 	 * Gets the active multiworld plugin on the server
 	 * 
 	 * @return the multiworld plugin
 	 */
 	public static GildorymMultiworldPlugin getMultiworldPlugin() {
 		return multiworldPlugin;
 	}
 
 	/**
 	 * Registers a multiworld plugin to the server
 	 * 
 	 * @param plugin the multiworld plugin to register
 	 */
 	public static void registerMultiworldPlugin(GildorymMultiworldPlugin plugin) {
 		Gildorym.multiworldPlugin = plugin;
 	}
 
 	/**
 	 * Gets the active permissions plugin on the server
 	 * 
 	 * @return the permissions plugin
 	 */
 	public static GildorymPermissionsPlugin getPermissionsPlugin() {
 		return permissionsPlugin;
 	}
 	
 	/**
 	 * Registers a permissions plugin to the server
 	 * 
 	 * @param plugin the plugin to register
 	 */
 	public static void registerPermissionsPlugin(GildorymPermissionsPlugin plugin) {
 		Gildorym.permissionsPlugin = plugin;
 	}
 	
 	/**
 	 * Gets the active portals plugin on the server
 	 * 
 	 * @return the portals plugin
 	 */
 	public static GildorymPortalsPlugin getPortalsPlugin() {
 		return portalsPlugin;
 	}
 
 	/**
 	 * Registers a portals plugin to the server
 	 * 
 	 * @param plugin the portals plugin to register
 	 */
 	public static void registerPortalsPlugin(GildorymPortalsPlugin plugin) {
 		Gildorym.portalsPlugin = plugin;
 	}
 
 	/**
 	 * Gets the active professions plugin on the server
 	 * 
 	 * @return the professions plugin
 	 */
 	public static GildorymProfessionsPlugin getProfessionsPlugin() {
 		return professionsPlugin;
 	}
 
 	/**
 	 * Registers a professions plugin to the server
 	 * 
 	 * @param plugin the professions plugin to register
 	 */
 	public static void registerProfessionsPlugin(GildorymProfessionsPlugin plugin) {
 		Gildorym.professionsPlugin = plugin;
 	}
 
 	/**
 	 * Gets the active regions plugin on the server
 	 * 
 	 * @return the regions plugin
 	 */
 	public static GildorymRegionsPlugin getRegionsPlugin() {
 		return regionsPlugin;
 	}
 
 	/**
 	 * Registers a regions plugin to the server
 	 * 
 	 * @param plugin the regions plugin to register
 	 */
 	public static void registerRegionsPlugin(GildorymRegionsPlugin plugin) {
 		Gildorym.regionsPlugin = plugin;
 	}
 
 	/**
 	 * Gets the active settlements plugin on the server
 	 * 
 	 * @return the settlements plugin
 	 */
 	public static GildorymSettlementsPlugin getSettlementsPlugin() {
 		return settlementsPlugin;
 	}
 
 	/**
 	 * Registers a settlements plugin to the server
 	 * 
 	 * @param plugin the settlements plugin to register
 	 */
 	public static void registerSettlementsPlugin(GildorymSettlementsPlugin plugin) {
 		Gildorym.settlementsPlugin = plugin;
 	}
 
 }
