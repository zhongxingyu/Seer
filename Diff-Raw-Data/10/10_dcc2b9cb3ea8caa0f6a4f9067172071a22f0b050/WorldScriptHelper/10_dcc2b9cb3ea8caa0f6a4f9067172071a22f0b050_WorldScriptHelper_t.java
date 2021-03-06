 package net.aufdemrand.denizen.scripts.containers.core;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ExecutionException;
 
 import net.aufdemrand.denizen.Settings;
 import net.aufdemrand.denizen.objects.Duration;
 import net.aufdemrand.denizen.objects.Element;
 import net.aufdemrand.denizen.objects.aH;
 import net.aufdemrand.denizen.objects.aH.Argument;
 import net.aufdemrand.denizen.objects.dEntity;
 import net.aufdemrand.denizen.objects.dInventory;
 import net.aufdemrand.denizen.objects.dItem;
 import net.aufdemrand.denizen.objects.dList;
 import net.aufdemrand.denizen.objects.dLocation;
 import net.aufdemrand.denizen.objects.dMaterial;
 import net.aufdemrand.denizen.objects.dNPC;
 import net.aufdemrand.denizen.objects.dObject;
 import net.aufdemrand.denizen.objects.dPlayer;
 import net.aufdemrand.denizen.objects.dWorld;
 import net.aufdemrand.denizen.scripts.ScriptBuilder;
 import net.aufdemrand.denizen.scripts.ScriptEntry;
 import net.aufdemrand.denizen.scripts.commands.core.DetermineCommand;
 import net.aufdemrand.denizen.scripts.queues.ScriptQueue;
 import net.aufdemrand.denizen.scripts.queues.core.InstantQueue;
 import net.aufdemrand.denizen.tags.TagManager;
 import net.aufdemrand.denizen.utilities.Conversion;
 import net.aufdemrand.denizen.utilities.DenizenAPI;
 import net.aufdemrand.denizen.utilities.debugging.dB;
 import net.aufdemrand.denizen.utilities.entity.Position;
 
 import org.bukkit.Bukkit;
 import org.bukkit.DyeColor;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.ItemFrame;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Sheep;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockBurnEvent;
 import org.bukkit.event.block.BlockCanBuildEvent;
 import org.bukkit.event.block.BlockDamageEvent;
 import org.bukkit.event.block.BlockDispenseEvent;
 import org.bukkit.event.block.BlockFadeEvent;
 import org.bukkit.event.block.BlockFormEvent;
 import org.bukkit.event.block.BlockFromToEvent;
 import org.bukkit.event.block.BlockGrowEvent;
 import org.bukkit.event.block.BlockIgniteEvent;
 import org.bukkit.event.block.BlockPhysicsEvent;
 import org.bukkit.event.block.BlockPistonExtendEvent;
 import org.bukkit.event.block.BlockPistonRetractEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.block.BlockRedstoneEvent;
 import org.bukkit.event.block.BlockSpreadEvent;
 import org.bukkit.event.block.EntityBlockFormEvent;
 import org.bukkit.event.block.LeavesDecayEvent;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.event.enchantment.EnchantItemEvent;
 import org.bukkit.event.entity.CreatureSpawnEvent;
 import org.bukkit.event.entity.CreeperPowerEvent;
 import org.bukkit.event.entity.EntityChangeBlockEvent;
 import org.bukkit.event.entity.EntityCombustEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityExplodeEvent;
 import org.bukkit.event.entity.EntityPortalEnterEvent;
 import org.bukkit.event.entity.EntityPortalExitEvent;
 import org.bukkit.event.entity.EntityRegainHealthEvent;
 import org.bukkit.event.entity.EntityShootBowEvent;
 import org.bukkit.event.entity.EntityTameEvent;
 import org.bukkit.event.entity.EntityTargetEvent;
 import org.bukkit.event.entity.EntityTeleportEvent;
 import org.bukkit.event.entity.EntityUnleashEvent;
 import org.bukkit.event.entity.ExplosionPrimeEvent;
 import org.bukkit.event.entity.FoodLevelChangeEvent;
 import org.bukkit.event.entity.HorseJumpEvent;
 import org.bukkit.event.entity.ItemDespawnEvent;
 import org.bukkit.event.entity.ItemSpawnEvent;
 import org.bukkit.event.entity.PigZapEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.entity.PlayerLeashEntityEvent;
 import org.bukkit.event.entity.ProjectileHitEvent;
 import org.bukkit.event.entity.SheepDyeWoolEvent;
 import org.bukkit.event.entity.SheepRegrowWoolEvent;
 import org.bukkit.event.entity.SlimeSplitEvent;
 import org.bukkit.event.hanging.HangingBreakByEntityEvent;
 import org.bukkit.event.hanging.HangingBreakEvent;
 import org.bukkit.event.hanging.HangingPlaceEvent;
 import org.bukkit.event.inventory.BrewEvent;
 import org.bukkit.event.inventory.FurnaceBurnEvent;
 import org.bukkit.event.inventory.FurnaceExtractEvent;
 import org.bukkit.event.inventory.FurnaceSmeltEvent;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.inventory.InventoryCloseEvent;
 import org.bukkit.event.inventory.InventoryDragEvent;
 import org.bukkit.event.inventory.InventoryMoveItemEvent;
 import org.bukkit.event.inventory.InventoryOpenEvent;
 import org.bukkit.event.inventory.InventoryPickupItemEvent;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerAnimationEvent;
 import org.bukkit.event.player.PlayerBedEnterEvent;
 import org.bukkit.event.player.PlayerBedLeaveEvent;
 import org.bukkit.event.player.PlayerBucketEmptyEvent;
 import org.bukkit.event.player.PlayerBucketFillEvent;
 import org.bukkit.event.player.PlayerChangedWorldEvent;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerDropItemEvent;
 import org.bukkit.event.player.PlayerEggThrowEvent;
 import org.bukkit.event.player.PlayerFishEvent;
 import org.bukkit.event.player.PlayerGameModeChangeEvent;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerItemConsumeEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerKickEvent;
 import org.bukkit.event.player.PlayerLevelChangeEvent;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.event.player.PlayerShearEntityEvent;
 import org.bukkit.event.player.PlayerToggleFlightEvent;
 import org.bukkit.event.player.PlayerToggleSneakEvent;
 import org.bukkit.event.player.PlayerToggleSprintEvent;
 import org.bukkit.event.server.ServerCommandEvent;
 import org.bukkit.event.vehicle.VehicleBlockCollisionEvent;
 import org.bukkit.event.vehicle.VehicleCreateEvent;
 import org.bukkit.event.vehicle.VehicleDamageEvent;
 import org.bukkit.event.vehicle.VehicleDestroyEvent;
 import org.bukkit.event.vehicle.VehicleEnterEvent;
 import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
 import org.bukkit.event.vehicle.VehicleExitEvent;
 import org.bukkit.event.weather.LightningStrikeEvent;
 import org.bukkit.event.weather.WeatherChangeEvent;
 import org.bukkit.event.world.PortalCreateEvent;
 import org.bukkit.event.world.SpawnChangeEvent;
 import org.bukkit.event.world.StructureGrowEvent;
 import org.bukkit.event.world.WorldInitEvent;
 import org.bukkit.event.world.WorldLoadEvent;
 import org.bukkit.event.world.WorldSaveEvent;
 import org.bukkit.event.world.WorldUnloadEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.util.BlockIterator;
 
 @SuppressWarnings("deprecation")
 public class WorldScriptHelper implements Listener {
 
     public static Map<String, WorldScriptContainer> world_scripts = new ConcurrentHashMap<String, WorldScriptContainer>(8, 0.9f, 1);
 
     public WorldScriptHelper() {
         DenizenAPI.getCurrentInstance().getServer().getPluginManager()
                 .registerEvents(this, DenizenAPI.getCurrentInstance());
     }
 
 
     /////////////////////
     //   EVENT HANDLER
     /////////////////
 
     public static String doEvents(List<String> eventNames, dNPC npc, Player player, Map<String, dObject> context) {
 
         String determination = "none";
 
         if (dB.showEventsFiring) dB.log("Fired for '" + eventNames.toString() + "'");
 
         for (WorldScriptContainer script : world_scripts.values()) {
 
             if (script == null) continue;
 
             for (String eventName : eventNames) {
 
                 // Check for event's name with and without dObject prefixes
                 if (!script.contains("EVENTS.ON " + eventName.toUpperCase()))
                     continue;
 
                 // Fetch script from Event
                 //
                 // Note: a "new dPlayer(null)" will not be null itself,
                 //       so keep a ternary operator here
                 List<ScriptEntry> entries = script.getEntries
                         (player != null ? new dPlayer(player) : null,
                                 npc, "events.on " + eventName);
 
                 if (entries.isEmpty()) continue;
 
                 dB.report("Event",
                         aH.debugObj("Type", "On " + eventName)
                                 + script.getAsScriptArg().debug()
                                 + (npc != null ? aH.debugObj("NPC", npc.toString()) : "")
                                 + (player != null ? aH.debugObj("Player", player.getName()) : "")
                                 + (context != null ? aH.debugObj("Context", context.toString()) : ""));
 
                 dB.echoDebug(dB.DebugElement.Header, "Building event 'On " + eventName.toUpperCase() + "' for " + script.getName());
 
                 // Create new ID -- this is what we will look for when determining an outcome
                 long id = DetermineCommand.getNewId();
 
                 // Add the reqId to each of the entries for the determine command (this may be slightly outdated, add to TODO)
                 ScriptBuilder.addObjectToEntries(entries, "ReqId", id);
 
                 // Add entries and context to the queue
                 ScriptQueue queue = InstantQueue.getQueue(null).addEntries(entries);
 
                 if (context != null) {
                     for (Map.Entry<String, dObject> entry : context.entrySet()) {
                         queue.addContext(entry.getKey(), entry.getValue());
                     }
                 }
 
                 // Start the queue!
                 queue.start();
 
                 // Check the determination
                 if (DetermineCommand.hasOutcome(id))
                     determination =  DetermineCommand.getOutcome(id);
             }
         }
 
         return determination;
     }
 
 
     /////////////////////
     //   BLOCK EVENTS
     /////////////////
 
     // <--[event]
     // @Events
     // player breaks block
     // player breaks <material>
     // player breaks block with <item>
     // player breaks <material> with <item>
     // player breaks block with <material>
     // player breaks <material> with <material>
     //
     // @Triggers when a player breaks a block.
     // @Context
     // <context.location> returns the dLocation the block was broken at.
     // <context.material> returns the dMaterial of the block that was broken.
     //
     // @Determine
     // "CANCELLED" to stop the block from breaking.
     // "NOTHING" to make the block drop no items.
     // dList(dItem) to make the block drop a specified list of items.
     //
     // -->
     @EventHandler
     public void blockBreak(BlockBreakEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         Block block = event.getBlock();
         dMaterial material = new dMaterial(event.getBlock().getType());
 
         context.put("location", new dLocation(block.getLocation()));
         context.put("material", material);
 
         dItem item = new dItem(event.getPlayer().getItemInHand());
         dMaterial itemMaterial = item.getMaterial();
 
         List<String> events = new ArrayList<String>();
         events.add("player breaks block");
         events.add("player breaks " + material.identify());
         events.add("player breaks block with " + item.identify());
         events.add("player breaks " + material.identify() + " with " + item.identify());
         events.add("player breaks block with " + itemMaterial.identify());
         events.add("player breaks " + material.identify() + " with " + itemMaterial.identify());
 
         String determination = doEvents(events, null, event.getPlayer(), context);
 
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setCancelled(true);
 
             // Make nothing drop, usually used as "drop:nothing"
         else if (determination.toUpperCase().startsWith("NOTHING")) {
             event.setCancelled(true);
             block.setType(Material.AIR);
         }
 
         // Get a dList of dItems to drop
         else if (Argument.valueOf(determination).matchesArgumentList(dItem.class)) {
 
             // Cancel the event
             event.setCancelled(true);
             block.setType(Material.AIR);
 
             // Get the list of items
             Object list = dList.valueOf(determination).filter(dItem.class);
 
             @SuppressWarnings("unchecked")
             List<dItem> newItems = (List<dItem>) list;
 
             for (dItem newItem : newItems) {
 
                 block.getWorld().dropItemNaturally(block.getLocation(),
                         newItem.getItemStack()); // Drop each item
             }
         }
     }
 
     // <--[event]
     // @Events
     // block burns
     // <block> burns
     //
     // @Triggers when a block is destroyed by fire.
     // @Context
     // <context.location> returns the dLocation the block was burned at.
     // <context.material> returns the dMaterial of the block that was burned.
     //
     // @Determine
     // "CANCELLED" to stop the block from being destroyed.
     //
     // -->
     @EventHandler
     public void blockBurn(BlockBurnEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
 
         context.put("location", new dLocation(event.getBlock().getLocation()));
         dMaterial material = new dMaterial(event.getBlock().getType());
 
         String determination = doEvents(Arrays.asList
                 ("block burns",
                  material.identify() + " burns"),
                  null, null, context);
 
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setCancelled(true);
     }
 
     // <--[event]
     // @Events
     // block being built
     // block being built on <material>
     // <material> being built
     // <material> being built on <material>
     //
     // @Triggers when an attempt is made to build a block on another block. Not necessarily caused by players.
     // @Context
     // <context.location> returns the dLocation of the block the player is trying to build on.
     // <context.old_material> returns the dMaterial of the block the player is trying to build on.
     // <context.new_material> returns the dMaterial of the block the player is trying to build.
     //
     // @Determine
     // "BUILDABLE" to allow the building.
     // "CANCELLED" to cancel the building.
     //
     // -->
     @EventHandler
     public void blockCanBuild(BlockCanBuildEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         dMaterial oldMaterial = new dMaterial(event.getBlock().getType());
         dMaterial newMaterial = new dMaterial(event.getMaterial());
 
         context.put("location", new dLocation(event.getBlock().getLocation()));
         context.put("old_material", oldMaterial);
         context.put("new_material", newMaterial);
 
         String determination = doEvents(Arrays.asList
                 ("block being built",
                  "block being built on " + oldMaterial.identify(),
                         newMaterial.identify() + " being built",
                  newMaterial.identify() + " being built on " +
                         oldMaterial.identify()),
                  null, null, context);
 
         if (determination.toUpperCase().startsWith("BUILDABLE"))
             event.setBuildable(true);
 
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setBuildable(false);
     }
 
     // <--[event]
     // @Events
     // player damages block
     // player damages <material>
     //
     // @Triggers when a block is damaged by a player.
     // @Context
     // <context.location> returns the dLocation the block that was damaged.
     // <context.material> returns the dMaterial of the block that was damaged.
     //
     // @Determine
     // "CANCELLED" to stop the block from being damaged.
     // "INSTABREAK" to make the block get broken instantly.
     //
     // -->
     @EventHandler
     public void blockDamage(BlockDamageEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         dMaterial material = new dMaterial(event.getBlock().getType());
 
         context.put("location", new dLocation(event.getBlock().getLocation()));
         context.put("material", material);
 
         String determination = doEvents(Arrays.asList
                 ("player damages block",
                  "player damages " + material.identify()),
                  null, event.getPlayer(), context);
 
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setCancelled(true);
 
         if (determination.toUpperCase().startsWith("INSTABREAK"))
             event.setInstaBreak(true);
     }
 
     // <--[event]
     // @Events
     // block dispenses item
     // block dispenses <item>
     // <block> dispenses item
     // <block> dispenses <item>
     //
     // @Triggers when a block dispenses an item.
     // @Context
     // <context.location> returns the dLocation of the dispenser.
     // <context.item> returns the dItem of the item being dispensed.
     //
     // @Determine
     // "CANCELLED" to stop the block from dispensing.
     // Element(Double) to set the power with which the item is shot.
     //
     // -->
     @EventHandler
     public void blockDispense(BlockDispenseEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         dItem item = new dItem(event.getItem());
         dMaterial material = new dMaterial(event.getBlock().getType());
 
         context.put("location", new dLocation(event.getBlock().getLocation()));
         context.put("item", item);
 
         String determination = doEvents(Arrays.asList
                 ("block dispenses item",
                  "block dispenses " + item.identify(),
                  material.identify() + " dispenses item",
                  material.identify() + " dispenses " + item.identify()),
                  null, null, context);
 
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setCancelled(true);
 
         else if (Argument.valueOf(determination)
                          .matchesPrimitive(aH.PrimitiveType.Double)) {
             event.setVelocity(event.getVelocity().normalize()
                  .multiply(aH.getDoubleFrom(determination)));
         }
     }
 
     // <--[event]
     // @Events
     // block fades
     // <block> fades
     //
     // @Triggers when a block fades, melts or disappears based on world conditions.
     // @Context
     // <context.location> returns the dLocation the block faded at.
     // <context.material> returns the dMaterial of the block that faded.
     //
     // @Determine
     // "CANCELLED" to stop the block from fading.
     //
     // -->
     @EventHandler
     public void blockFade(BlockFadeEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         dMaterial material = new dMaterial(event.getBlock().getType());
 
         context.put("location", new dLocation(event.getBlock().getLocation()));
         context.put("material", material);
 
         String determination = doEvents(Arrays.asList
                 ("block fades",
                  material.identify() + " fades"),
                  null, null, context);
 
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setCancelled(true);
     }
 
     // <--[event]
     // @Events
     // block forms
     // <block> forms
     //
     // @Triggers when a block is formed based on world conditions,
     //           e.g. when snow forms in a snow storm or ice forms
     //           in a snowy biome
     // @Context
     // <context.location> returns the dLocation the block.
     // <context.material> returns the dMaterial of the block.
     //
     // @Determine
     // "CANCELLED" to stop the block from forming.
     //
     // -->
     @EventHandler
     public void blockForm(BlockFormEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         dMaterial material = new dMaterial(event.getBlock().getType());
 
         context.put("location", new dLocation(event.getBlock().getLocation()));
         context.put("material", material);
 
         String determination = doEvents(Arrays.asList
                 ("block forms",
                  material.identify() + " forms"),
                  null, null, context);
 
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setCancelled(true);
     }
 
     // <--[event]
     // @Events
     // liquid spreads
     // <liquid block> spreads
     //
     // @Triggers when a liquid block spreads.
     // @Context
     // <context.destination> returns the dLocation the block spread to.
     // <context.location> returns the dLocation the block spread from.
     // <context.material> returns the dMaterial of the block that spread.
     //
     // @Determine
     // "CANCELLED" to stop the block from spreading.
     //
     // -->
     @EventHandler
     public void blockFromTo(BlockFromToEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         dMaterial material = new dMaterial(event.getBlock().getType());
 
         context.put("location", new dLocation(event.getBlock().getLocation()));
         context.put("destination", new dLocation(event.getToBlock().getLocation()));
         context.put("material", material);
 
         String determination = doEvents(Arrays.asList
                 ("liquid spreads",
                  material.identify() + " spreads"),
                  null, null, context);
 
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setCancelled(true);
     }
 
     // <--[event]
     // @Events
     // block grows
     // <block> grows
     //
     // @Triggers when a block grows naturally in the world,
     //           e.g. when wheat, sugar canes, cactuses,
     //           watermelons or pumpkins grow
     // @Context
     // <context.location> returns the dLocation the block.
     // <context.material> returns the dMaterial of the block.
     //
     // @Determine
     // "CANCELLED" to stop the block from growing.
     //
     // -->
     @EventHandler
     public void blockGrow(BlockGrowEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         dMaterial material = new dMaterial(event.getBlock().getType());
 
         context.put("location", new dLocation(event.getBlock().getLocation()));
         context.put("material", material);
 
         String determination = doEvents(Arrays.asList
                 ("block grows",
                  material.identify() + " grows"),
                  null, null, context);
 
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setCancelled(true);
     }
 
     // <--[event]
     // @Events
     // block ignites
     // <block> ignites
     //
     // @Triggers when a block is set on fire.
     // @Context
     // <context.location> returns the dLocation the block was set on fire at.
     // <context.material> returns the dMaterial of the block that was set on fire.
     //
     // @Determine
     // "CANCELLED" to stop the block from being ignited.
     //
     // -->
     @EventHandler
     public void blockIgnite(BlockIgniteEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         dMaterial material = new dMaterial(event.getBlock().getType());
 
         context.put("location", new dLocation(event.getBlock().getLocation()));
         context.put("material", material);
 
         String determination = doEvents(Arrays.asList
                 ("block ignites",
                  material.identify() + " ignites"),
                  null, event.getPlayer(), context);
 
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setCancelled(true);
     }
 
     // <--[event]
     // @Events
     // block moves
     // <block> moves
     //
     // @Triggers when a block moves.
     // @Context
     // <context.location> returns the dLocation the block moved to.
     // <context.material> returns the dMaterial of the block that moved.
     //
     // @Determine
     // "CANCELLED" to stop the block from being moved.
     //
     // -->
     @EventHandler
     public void blockPhysics(BlockPhysicsEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         dMaterial material = new dMaterial(event.getBlock().getType());
 
         context.put("location", new dLocation(event.getBlock().getLocation()));
         context.put("material", material);
 
         String determination = doEvents(Arrays.asList
                 ("block moves",
                  material.identify() + " moves"),
                  null, null, context);
 
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setCancelled(true);
     }
 
     // <--[event]
     // @Events
     // piston extends
     // <block> extends
     //
     // @Triggers when a piston extends.
     // @Context
     // <context.location> returns the dLocation of the piston.
     // <context.material> returns the dMaterial of the piston.
     // <context.length> returns the number of blocks that will be moved by the piston.
     //
     // @Determine
     // "CANCELLED" to stop the piston from extending.
     //
     // -->
     @EventHandler
     public void blockPistonExtend(BlockPistonExtendEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         dMaterial material = new dMaterial(event.getBlock().getType());
 
         context.put("location", new dLocation(event.getBlock().getLocation()));
         context.put("material", material);
         context.put("length", new Element(event.getLength()));
 
         String determination = doEvents(Arrays.asList
                 ("piston extends",
                  material.identify() + " extends"),
                  null, null, context);
 
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setCancelled(true);
     }
 
     // <--[event]
     // @Events
     // piston retracts
     // <block> retracts
     //
     // @Triggers when a piston retracts.
     // @Context
     // <context.location> returns the dLocation of the piston.
     // <context.retract_location> returns the new dLocation of the block that
     //                            will be moved by the piston if it is sticky.
     // <context.material> returns the dMaterial of the piston.
     //
     // @Determine
     // "CANCELLED" to stop the piston from retracting.
     //
     // -->
     @EventHandler
     public void blockPistonRetract(BlockPistonRetractEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         dMaterial material = new dMaterial(event.getBlock().getType());
 
         context.put("location", new dLocation(event.getBlock().getLocation()));
         context.put("retract_location", new dLocation(event.getRetractLocation()));
         context.put("material", material);
 
         String determination = doEvents(Arrays.asList
                 ("piston retracts",
                  material.identify() + " retracts"),
                  null, null, context);
 
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setCancelled(true);
     }
 
     // <--[event]
     // @Events
     // player places block
     // player places <block>
     //
     // @Triggers when a player places a block.
     // @Context
     // <context.location> returns the dLocation of the block that was placed.
     // <context.material> returns the dMaterial of the block that was placed.
     //
     // @Determine
     // "CANCELLED" to stop the block from being placed.
     //
     // -->
     @EventHandler
     public void blockPlace(BlockPlaceEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         dMaterial material = new dMaterial(event.getBlock().getType());
 
         context.put("location", new dLocation(event.getBlock().getLocation()));
         context.put("material", material);
 
         String determination = doEvents(Arrays.asList
                 ("player places block",
                  "player places " + material.identify()),
                  null, event.getPlayer(), context);
 
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setCancelled(true);
     }
 
     // <--[event]
     // @Events
     // block powers
     // <block> powers
     // block unpowers
     // <block> unpowers
     //
     // @Triggers when a block is (un)powered.
     // @Context
     // <context.location> returns the dLocation of the block that was (un)powered.
     // <context.material> returns the dMaterial of the block that was (un)powered.
     //
     // @Determine
     // "CANCELLED" to stop the block from being (un)powered.
     //
     // -->
     @EventHandler
     public void blockRedstone(BlockRedstoneEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         dMaterial material = new dMaterial(event.getBlock().getType());
 
         context.put("location", new dLocation(event.getBlock().getLocation()));
         context.put("material", material);
 
         List<String> events = new ArrayList<String>();
 
         if (event.getNewCurrent() > 0) {
             events.add("block powers");
             events.add(material.identify() + " powers");
             events.add("block powered");
             events.add(material.identify() + " powered");
         }
         else {
             events.add("block unpowers");
             events.add(material.identify() + " unpowers");
             events.add("block unpowered");
             events.add(material.identify() + " unpowered");
         }
 
         String determination = doEvents(events, null, null, context);
 
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setNewCurrent(event.getOldCurrent());
     }
 
     // <--[event]
     // @Events
     // block spreads
     // <block> spreads
     //
     // @Triggers when a block spreads based on world conditions,
     //           e.g. when fire spreads, when mushrooms spread
     // @Context
     // <context.location> returns the dLocation the block.
     // <context.material> returns the dMaterial of the block.
     //
     // @Determine
     // "CANCELLED" to stop the block from growing.
     //
     // -->
     @EventHandler
     public void blockSpread(BlockSpreadEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         dMaterial material = new dMaterial(event.getBlock().getType());
 
         context.put("location", new dLocation(event.getBlock().getLocation()));
         context.put("material", material);
 
         String determination = doEvents(Arrays.asList
                 ("block spreads",
                  material.identify() + " spreads"),
                  null, null, context);
 
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setCancelled(true);
     }
 
     // <--[event]
     // @Events
     // brewing stand brews
     //
     // @Triggers when a brewing stand brews a potion.
     // @Context
     // <context.location> returns the dLocation of the brewing stand.
     // <context.inventory> returns the dInventory of the brewing stand's contents.
     //
     // @Determine
     // "CANCELLED" to stop the brewing stand from brewing.
     //
     // -->
     @EventHandler
     public void brew(BrewEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
 
         context.put("location", new dLocation(event.getBlock().getLocation()));
         context.put("inventory", new dInventory(event.getContents()));
 
         String determination = doEvents(Arrays.asList
                 ("brewing stand brews"),
                  null, null, context);
 
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setCancelled(true);
     }
 
     // <--[event]
     // @Events
     // entity forms block
     // entity forms <block>
     // <entity> forms block
     // <entity> forms <block>
     //
     // @Triggers when a block is formed by an entity,
     //           e.g. when a snowman forms snow
     // @Context
     // <context.location> returns the dLocation the block.
     // <context.material> returns the dMaterial of the block.
     // <context.entity> returns the dEntity that formed the block.
     //
     // @Determine
     // "CANCELLED" to stop the block from forming.
     //
     // -->
     @EventHandler
     public void entityBlockForm(EntityBlockFormEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         dMaterial material = new dMaterial(event.getBlock().getType());
         dEntity entity = new dEntity(event.getEntity());
         String entityType = entity.getEntityType().name();
 
         context.put("location", new dLocation(event.getBlock().getLocation()));
         context.put("material", material);
         context.put("entity", entity.getDenizenObject());
 
         String determination = doEvents(Arrays.asList
                 ("entity forms block",
                  "entity forms " + material.identify(),
                  entityType + " forms block",
                  entityType + " forms " + material.identify()),
                  null, null, context);
 
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setCancelled(true);
     }
 
     // <--[event]
     // @Events
     // furnace burns item
     // furnace burns <item>
     //
     // @Triggers when a furnace burns an item used as fuel.
     // @Context
     // <context.location> returns the dLocation of the furnace.
     // <context.item> returns the dItem burnt.
     //
     // @Determine
     // "CANCELLED" to stop the furnace from burning the item.
     // Element(Integer) to set the burn time for this fuel.
     //
     // -->
     @EventHandler
     public void furnaceBurn(FurnaceBurnEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         dItem item = new dItem(event.getFuel());
 
         context.put("location", new dLocation(event.getBlock().getLocation()));
         context.put("item", item);
 
         String determination = doEvents(Arrays.asList
                 ("furnace burns item",
                  "furnace burns " + item.identify()),
                  null, null, context);
 
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setCancelled(true);
         else if (Argument.valueOf(determination)
                          .matchesPrimitive(aH.PrimitiveType.Integer)) {
             event.setBurnTime(aH.getIntegerFrom(determination));
         }
     }
 
     // <--[event]
     // @Events
     // player takes item from furnace
     // player takes <item> from furnace
     // player takes <material> from furnace
     //
     // @Triggers when a player takes an item from a furnace.
     // @Context
     // <context.location> returns the dLocation of the furnace.
     // <context.item> returns the dItem taken out of the furnace.
     //
     // @Determine
     // Element(Integer) to set the amount of experience the player will get.
     //
     // -->
     @EventHandler
     public void furnaceExtract(FurnaceExtractEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         dMaterial itemMaterial = new dMaterial(event.getItemType());
         dItem item = new dItem(itemMaterial, event.getItemAmount());
 
         context.put("location", new dLocation(event.getBlock().getLocation()));
         context.put("item", item);
 
         String determination = doEvents(Arrays.asList
                 ("player takes item from furnace",
                  "player takes " + item.identify() + " from furnace",
                  "player takes " + itemMaterial.identify() + " from furnace"),
                  null, event.getPlayer(), context);
 
         if (Argument.valueOf(determination)
                 .matchesPrimitive(aH.PrimitiveType.Integer)) {
             event.setExpToDrop(aH.getIntegerFrom(determination));
         }
     }
 
     // <--[event]
     // @Events
     // furnace smelts item (into <item>)
     // furnace smelts <item> (into <item>)
     //
     // @Triggers when a furnace smelts an item.
     // @Context
     // <context.location> returns the dLocation of the furnace.
     // <context.source> returns the dItem that is smelted.
     // <context.result> returns the dItem that is the result of the smelting.
     //
     // @Determine
     // "CANCELLED" to stop the furnace from smelting the item.
     // dItem to set the item that is the result of the smelting.
     //
     // -->
     @EventHandler
     public void furnaceSmelt(FurnaceSmeltEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         dItem source = new dItem(event.getSource());
         dItem result = new dItem(event.getResult());
 
         context.put("location", new dLocation(event.getBlock().getLocation()));
         context.put("source_item", source);
         context.put("result_item", result);
 
         String determination = doEvents(Arrays.asList
                 ("furnace smelts item",
                  "furnace smelts " + source.identify(),
                  "furnace smelts item into " + result.identify(),
                  "furnace smelts " + source.identify() + " into " + result.identify()),
                  null, null, context);
 
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setCancelled(true);
         else if (dItem.matches(determination)) {
             event.setResult(dItem.valueOf(determination).getItemStack());
         }
     }
 
     // <--[event]
     // @Events
     // leaves decay
     // <block> decay
     //
     // @Triggers when leaves decay.
     // @Context
     // <context.location> returns the dLocation of the leaves.
     // <context.material> returns the dMaterial of the leaves.
     //
     // @Determine
     // "CANCELLED" to stop the leaves from decaying.
     //
     // -->
     @EventHandler
     public void leavesDecay(LeavesDecayEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         dMaterial material = new dMaterial(event.getBlock().getType());
 
         context.put("location", new dLocation(event.getBlock().getLocation()));
         context.put("material", material);
 
         String determination = doEvents(Arrays.asList
                 ("leaves decay",
                  material.identify() + " decay"),
                  null, null, context);
 
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setCancelled(true);
     }
 
     // <--[event]
     // @Events
     // player changes sign
     // player changes wall_sign
     // player changes sign_post
     //
     // @Triggers when a player changes a sign.
     // @Context
     // <context.location> returns the dLocation of the sign.
     // <context.new> returns the new sign text as a dList.
     // <context.old> returns the old sign text as a dList.
     // <context.material> returns the dMaterial of the sign.
     //
     // @Determine
     // "CANCELLED" to stop the sign from being changed.
     //
     // -->
     @EventHandler
     public void signChange(final SignChangeEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
 
         Player player = event.getPlayer();
         Block block = event.getBlock();
         Sign sign = (Sign) block.getState();
         dMaterial material = new dMaterial(block.getType());
 
         context.put("old", new dList(Arrays.asList(sign.getLines())));
         context.put("new", new dList(Arrays.asList(event.getLines())));
         context.put("location", new dLocation(block.getLocation()));
         context.put("material", material);
 
         String determination = doEvents(Arrays.asList
                 ("player changes sign",
                  "player changes " + material.identify()),
                  null, player, context);
 
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setCancelled(true);
     }
 
 
     /////////////////////
     //   CUSTOM EVENTS
     /////////////////
 
     // <--[event]
     // @Events
     // server start
     //
     // @Triggers when the server starts
     //
     // @Determine "CANCELLED" to save all plugins and cancel server startup.
     //
     // -->
     public void serverStartEvent() {
         // Start the 'timeEvent'
         Bukkit.getScheduler().scheduleSyncRepeatingTask(DenizenAPI.getCurrentInstance(),
                 new Runnable() {
                     @Override
                     public void run() {
                         timeEvent();
                     }
                 }, Settings.WorldScriptTimeEventFrequency().getTicks(), Settings.WorldScriptTimeEventFrequency().getTicks());
 
         // Fire the 'Server Start' event
         String determination = doEvents(Arrays.asList("server start"),
                 null, null, null);
 
         if (determination.toUpperCase().startsWith("CANCELLED"))
             Bukkit.getServer().shutdown();
     }
 
     private final Map<String, Integer> current_time = new HashMap<String, Integer>();
 
     // <--[event]
     // @Events
     // time changes in <world>
     // <0-23>:00 in <world>
     // time <0-23> in <world>
     //
     // @Triggers when a block is set on fire.
     // @Context
     // <context.time> returns the current time.
     // <context.world> returns the world.
     //
     // -->
     public void timeEvent() {
         for (World world : Bukkit.getWorlds()) {
             int hour = Double.valueOf(world.getTime() / 1000).intValue();
             hour = hour + 6;
             // Get the hour
             if (hour >= 24) hour = hour - 24;
 
             dWorld currentWorld = new dWorld(world);
 
             if (!current_time.containsKey(currentWorld.identify())
                     || current_time.get(currentWorld.identify()) != hour) {
                 Map<String, dObject> context = new HashMap<String, dObject>();
 
                 context.put("time", new Element(hour));
                 context.put("world", currentWorld);
 
                 doEvents(Arrays.asList
                         ("time changes in " + currentWorld.identify(),
                          String.valueOf(hour) + ":00 in " + currentWorld.identify(),
                          "time " + String.valueOf(hour) + " in " + currentWorld.identify()),
                          null, null, context);
 
                 current_time.put(currentWorld.identify(), hour);
             }
         }
     }
 
 
     /////////////////////
     //   HANGING EVENTS
     /////////////////
 
     // <--[event]
     // @Events
     // hanging breaks
     // hanging breaks because <cause>
     // <hanging> breaks
     // <hanging> breaks because <cause>
     //
     // @Triggers when a hanging entity (painting or itemframe) is broken.
     // @Context
     // <context.cause> returns the cause of the entity breaking.
     // <context.entity> returns the dEntity that broke the hanging entity, if any.
     // <context.hanging> returns the dEntity of the hanging.
     //
     // @Determine
     // "CANCELLED" to stop the hanging from being broken.
     //
     // -->
     @EventHandler
     public void hangingBreak(HangingBreakEvent event) {
 
         Player player = null;
         dNPC npc = null;
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         dEntity hanging = new dEntity(event.getEntity());
         String hangingType = hanging.getEntityType().name();
         String cause =  event.getCause().name();
 
         context.put("hanging", hanging);
         context.put("cause", new Element(cause));
 
         List<String> events = new ArrayList<String>();
         events.add("hanging breaks");
         events.add("hanging breaks because " + cause);
         events.add(hangingType + " breaks");
         events.add(hangingType +
                 " breaks because " + cause);
 
         if (event instanceof HangingBreakByEntityEvent) {
 
             // <--[event]
             // @Events
             // <entity> breaks hanging
             // <entity> breaks hanging because <cause>
             // <entity> breaks <hanging>
             // <entity> breaks <hanging> because <cause>
             //
             // @Triggers when a hanging entity is broken by an entity.
             // @Context
             // <context.cause> returns the cause of the entity breaking.
             // <context.entity> returns the dEntity that broke the hanging entity.
             // <context.hanging> returns the hanging entity as a dEntity.
             //
             // @Determine
             // "CANCELLED" to stop the hanging entity from being broken.
             //
             // -->
 
             HangingBreakByEntityEvent subEvent = (HangingBreakByEntityEvent) event;
 
             dEntity entity = new dEntity(subEvent.getRemover());
             String entityType = entity.getEntityType().name();
             context.put("entity", entity.getDenizenObject());
 
             if (entity.isNPC()) { npc = entity.getDenizenNPC(); entityType = "npc"; }
             else if (entity.isPlayer()) player = entity.getPlayer();
 
             events.add("entity breaks hanging");
             events.add("entity breaks hanging because " + cause);
             events.add("entity breaks " + hangingType);
             events.add("entity breaks " + hangingType + " because " + cause);
             events.add(entityType + " breaks hanging");
             events.add(entityType + " breaks hanging because " + cause);
             events.add(entityType + " breaks " + hangingType);
             events.add(entityType + " breaks " + hangingType + " because " + cause);
         }
 
         String determination = doEvents(events, npc, player, context);
 
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setCancelled(true);
     }
 
     // <--[event]
     // @Events
     // player places hanging
     // player places <hanging>
     //
     // @Triggers when a hanging entity (painting or itemframe) is placed.
     // @Context
     // <context.hanging> returns the dEntity of the hanging.
     // <context.location> returns the dLocation of the block the hanging was placed on.
     //
     // @Determine
     // "CANCELLED" to stop the hanging from being placed.
     //
     // -->
     @EventHandler
     public void hangingPlace(HangingPlaceEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         dEntity hanging = new dEntity(event.getEntity());
         String hangingType = hanging.getEntityType().name();
 
         context.put("hanging", hanging);
         context.put("location", new dLocation(event.getBlock().getLocation()));
 
         String determination = doEvents(Arrays.asList
                 ("player places hanging",
                  "player places " + hangingType),
                  null, event.getPlayer(), context);
 
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setCancelled(true);
     }
 
 
     /////////////////////
     //   ENTITY EVENTS
     /////////////////
 
     // <--[event]
     // @Events
     // entity spawns
     // entity spawns because <cause>
     // <entity> spawns
     // <entity> spawns because <cause>
     //
     // @Triggers when an entity spawns.
     // @Context
     // <context.entity> returns the dEntity that spawned.
     // <context.reason> returns the reason the entity spawned.
     //
     // @Determine
     // "CANCELLED" to stop the entity from spawning.
     //
     // -->
     @EventHandler
     public void creatureSpawn(CreatureSpawnEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         dEntity entity = new dEntity(event.getEntity());
         String entityType = entity.getEntityType().name();
         String reason = event.getSpawnReason().name();
 
         context.put("entity", entity);
         context.put("reason", new Element(reason));
 
         String determination = doEvents(Arrays.asList
                 ("entity spawns",
                  "entity spawns because " + event.getSpawnReason().name(),
                  entityType + " spawns",
                  entityType + " spawns because " + reason),
                  null, null, context);
 
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setCancelled(true);
     }
 
     // <--[event]
     // @Events
     // creeper powered (because <cause>)
     //
     // @Triggers when a creeper is struck by lightning and turned into a powered creeper.
     // @Context
     // <context.entity> returns the dEntity of the creeper.
     // <context.lightning> returns the dEntity of the lightning.
     // <context.cause> returns an Element of the cause for the creeper being powered.
     //
     // @Determine
     // "CANCELLED" to stop the creeper from being powered.
     //
     // -->
     @EventHandler
     public void creeperPower(CreeperPowerEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         dEntity entity = new dEntity(event.getEntity());
         dEntity lightning = new dEntity(event.getLightning());
         String cause = event.getCause().name();
 
         context.put("entity", entity);
         context.put("lightning", lightning);
         context.put("cause", new Element(cause));
 
         String determination = doEvents(Arrays.asList
                 ("creeper powered",
                  "creeper powered because " + cause),
                  null, null, context);
 
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setCancelled(true);
     }
 
     // <--[event]
     // @Events
     // entity changes block
     // <entity> changes block
     // <entity> changes block
     //
     // @Triggers when an entity changes the material of a block.
     // @Context
     // <context.entity> returns the dEntity that changed the block.
     // <context.location> returns the dLocation of the changed block.
     // <context.old_material> returns the old material of the block.
     // <context.new_material> returns the new material of the block.
     //
     // @Determine
     // "CANCELLED" to stop the entity from changing the block.
     //
     // -->
     @EventHandler
     public void entityChangeBlock(EntityChangeBlockEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         dEntity entity = new dEntity(event.getEntity());
         String entityType = entity.getEntityType().name();
         dMaterial oldMaterial = new dMaterial(event.getBlock().getType());
         dMaterial newMaterial = new dMaterial(event.getTo());
 
         context.put("entity", entity.getDenizenObject());
         context.put("location", new dLocation(event.getBlock().getLocation()));
         context.put("old_material", oldMaterial);
         context.put("new_material", newMaterial);
 
         String determination = doEvents(Arrays.asList
                 ("entity changes block",
                  "entity changes " + oldMaterial.identify(),
                  "entity changes block into " + newMaterial.identify(),
                  "entity changes " + oldMaterial.identify() +
                          " into " + newMaterial.identify(),
                  entityType + " changes block",
                  entityType + " changes " + oldMaterial.identify(),
                  entityType + " changes block into " + newMaterial.identify(),
                  entityType + " changes " + oldMaterial.identify() +
                          " into " + newMaterial.identify()),
                  null, null, context);
 
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setCancelled(true);
     }
 
     // <--[event]
     // @Events
     // entity combusts
     // <entity> combusts
     //
     // @Triggers when an entity combusts.
     // @Context
     // <context.duration> returns how long the entity takes to combust.
     // <context.entity> returns the dEntity that combusted.
     //
     // @Determine
     // "CANCELLED" to stop the entity from combusting.
     //
     // -->
     @EventHandler
     public void entityCombust(EntityCombustEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         Entity entity = event.getEntity();
 
         context.put("entity", new dEntity(entity).getDenizenObject());
         context.put("duration", new Duration((long) event.getDuration()));
 
         String determination = doEvents(Arrays.asList
                 ("entity combusts",
                  entity.getType().name() + " combusts"),
                  null, null, context);
 
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setCancelled(true);
     }
 
     // <--[event]
     // @Events
     // entity damaged
     // entity damaged by <cause>
     // <entity> damaged
     // <entity> damaged by <cause>
     //
     // @Triggers when an entity is damaged.
     // @Context
     // <context.cause> returns the reason the entity was damaged.
     // <context.damage> returns the amount of damage dealt.
     // <context.entity> returns the dEntity that was damaged.
     //
     // @Determine
     // "CANCELLED" to stop the entity from being damaged.
     // Element(Double) to set the amount of damage the entity receives.
     //
     // -->
     @EventHandler
     public void entityDamage(EntityDamageEvent event) {
 
         Player player = null;
         dNPC npc = null;
         String determination;
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         dEntity entity = new dEntity(event.getEntity());
         String entityType = entity.getEntityType().name();
         String cause = event.getCause().name();
 
         context.put("entity", entity.getDenizenObject());
         context.put("damage", new Element(event.getDamage()));
         context.put("cause", new Element(event.getCause().name()));
 
         if (entity.isNPC()) { npc = entity.getDenizenNPC(); entityType = "npc"; }
         else if (entity.isPlayer()) player = entity.getPlayer();
 
         boolean isFatal = false;
 
         if (entity.isValid() && entity.isLivingEntity()) {
             if (event.getDamage() >= entity.getLivingEntity().getHealth()) {
                 isFatal = true;
             }
         }
 
         List<String> events = new ArrayList<String>();
         events.add("entity damaged");
         events.add("entity damaged by " + cause);
         events.add(entityType + " damaged");
         events.add(entityType + " damaged by " + cause);
 
         if (isFatal) {
 
             // <--[event]
             // @Events
             // entity killed
             // entity killed by <cause>
             // <entity> killed
             // <entity> killed by <cause>
             //
             // @Triggers when an entity is killed.
             // @Context
             // <context.cause> returns the reason the entity was killed.
             // <context.entity> returns the dEntity that was killed.
             // <context.damage> returns the amount of damage dealt.
             // <context.shooter> returns the shooter of the entity, if any.
             //
             // @Determine
             // "CANCELLED" to stop the entity from being killed.
             // Element(Double) to set the amount of damage the entity receives, instead of dying.
             //
             // -->
 
             events.add("entity killed");
             events.add("entity killed by " + cause);
             events.add(entityType + " killed");
             events.add(entityType + " killed by " + cause);
         }
 
         if (event instanceof EntityDamageByEntityEvent) {
 
             // <--[event]
             // @Events
             // entity damages entity
             // entity damages <entity>
             // <entity> damages entity
             // <entity> damages <entity>
             //
             // @Triggers when an entity damages another entity.
             // @Context
             // <context.cause> returns the reason the entity was damaged.
             // <context.entity> returns the dEntity that was damaged.
             // <context.damage> returns the amount of damage dealt.
             // <context.damager> returns the dEntity damaging the other entity.
             // <context.shooter> returns the shooter of the entity, if any.
             //
             // @Determine
             // "CANCELLED" to stop the entity from being damaged.
             // Element(Double) to set the amount of damage the entity receives.
             //
             // -->
 
             EntityDamageByEntityEvent subEvent = (EntityDamageByEntityEvent) event;
 
             // Have a different set of player and NPC contexts for events
             // like "player damages player" from the one we have for
             // "player damaged by player"
 
             Player subPlayer = null;
             dNPC subNPC = null;
 
             dEntity damager = new dEntity(subEvent.getDamager());
             String damagerType = damager.getEntityType().name();
             context.put("damager", damager.getDenizenObject());
 
             if (damager.isNPC()) {
                 subNPC = damager.getDenizenNPC();
                 damagerType = "npc";
 
                 // If we had no NPC in our regular context, use this one
                 if (npc == null) npc = subNPC;
             }
 
             else if (damager.isPlayer()) {
                 subPlayer = damager.getPlayer();
 
                 // If we had no player in our regular context, use this one
                 if (player == null) player = subPlayer;
             }
 
             // If the damager is a projectile, add its shooter (which can be null)
             // to the context
             else if (damager.hasShooter()) {
                 dEntity shooter = damager.getShooter();
                 context.put("shooter", shooter.getDenizenObject());
             }
 
             events.add("entity damaged by entity");
             events.add("entity damaged by " + damagerType);
             events.add(entityType + " damaged by entity");
             events.add(entityType + " damaged by " + damagerType);
 
             // Have a new list of events for the subContextPlayer
             // and subContextNPC
 
             List<String> subEvents = new ArrayList<String>();
 
             subEvents.add("entity damages entity");
             subEvents.add("entity damages " + entityType);
             subEvents.add(damagerType + " damages entity");
             subEvents.add(damagerType + " damages " + entityType);
 
             if (isFatal) {
                 events.add("entity killed by entity");
                 events.add("entity killed by " + damagerType);
                 events.add(entityType + " killed by entity");
                 events.add(entityType + " killed by " + damagerType);
 
                 // <--[event]
                 // @Events
                 // entity kills entity
                 // entity kills <entity>
                 // <entity> kills entity
                 // <entity> kills <entity>
                 //
                 // @Triggers when an entity kills another entity.
                 // @Context
                 // <context.cause> returns the reason the entity was killed.
                 // <context.entity> returns the dEntity that was killed.
                 // <context.damager> returns the dEntity killing the other entity.
                 // <context.shooter> returns the shooter of the entity, if any.
                 //
                 // @Determine
                 // "CANCELLED" to stop the entity from being killed.
                 // Element(Number) to set the amount of damage the entity receives, instead of dying.
                 //
                 // -->
 
                 subEvents.add("entity kills entity");
                 subEvents.add("entity kills " + entityType);
                 subEvents.add(damagerType + " kills entity");
                 subEvents.add(damagerType + " kills " + entityType);
             }
 
             determination = doEvents(subEvents, subNPC, subPlayer, context);
 
             if (determination.toUpperCase().startsWith("CANCELLED"))
                 event.setCancelled(true);
 
             else if (Argument.valueOf(determination)
                     .matchesPrimitive(aH.PrimitiveType.Double)) {
                 event.setDamage(aH.getDoubleFrom(determination));
             }
         }
 
         determination = doEvents(events, npc, player, context);
 
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setCancelled(true);
         else if (Argument.valueOf(determination)
                 .matchesPrimitive(aH.PrimitiveType.Double)) {
             event.setDamage(aH.getDoubleFrom(determination));
         }
     }
 
     // <--[event]
     // @Events
     // entity explodes
     // <entity> explodes
     //
     // @Triggers when an entity explodes.
     // @Context
     // <context.blocks> returns a dList of blocks that the entity blew up.
     // <context.entity> returns the dEntity that exploded.
     // <context.location> returns the dLocation the entity blew up at.
     //
     // @Determine
     // "CANCELLED" to stop the entity from exploding.
     //
     // -->
     @EventHandler
     public void entityExplode(EntityExplodeEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         dEntity entity = new dEntity(event.getEntity());
         String entityType = entity.getEntityType().name();
 
         context.put("entity", entity.getDenizenObject());
         context.put("location", new dLocation(event.getLocation()));
 
         String blocks = "";
         for (Block block : event.blockList()) {
             blocks = blocks + new dLocation(block.getLocation()) + "|";
         }
         context.put("blocks", blocks.length() > 0 ? new dList(blocks) : null);
 
         String determination = doEvents(Arrays.asList
                 ("entity explodes",
                  entityType + " explodes"),
                  null, null, context);
 
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setCancelled(true);
     }
 
     // <--[event]
     // @Events
     // entity heals (because <cause>)
     // <entity> heals (because <cause>)
     //
     // @Triggers when an entity heals.
     // @Context
     // <context.amount> returns the amount the entity healed.
     // <context.entity> returns the dEntity that healed.
     // <context.reason> returns the cause of the entity healing.
     //
     // @Determine
     // "CANCELLED" to stop the entity from healing.
     // Element(Double) to set the amount of health the entity receives.
     //
     // -->
     @EventHandler
     public void entityRegainHealth(EntityRegainHealthEvent event) {
 
         Player player = null;
         dNPC npc = null;
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         dEntity entity = new dEntity(event.getEntity());
         String entityType = entity.getEntityType().name();
         String reason = event.getRegainReason().name();
 
         context.put("reason", new Element(event.getRegainReason().name()));
         context.put("amount", new Element(event.getAmount()));
         context.put("entity", entity.getDenizenObject());
 
         if (entity.isNPC()) { npc = entity.getDenizenNPC(); entityType = "npc"; }
         else if (entity.isPlayer()) player = entity.getPlayer();
 
         String determination = doEvents(Arrays.asList
                 ("entity heals",
                  "entity heals because " + reason,
                  entityType + " heals",
                  entityType + " heals because " + reason),
                  npc, player, context);
 
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setCancelled(true);
 
         else if (Argument.valueOf(determination)
                 .matchesPrimitive(aH.PrimitiveType.Double)) {
             event.setAmount(aH.getDoubleFrom(determination));
         }
     }
 
     // <--[event]
     // @Events
     // entity enters portal
     // <entity> enters portal
     //
     // @Triggers when an entity enters a portal.
     // @Context
     // <context.entity> returns the dEntity.
     // <context.location> returns the dLocation of the portal block touched by the entity.
     //
     // -->
     @EventHandler
     public void entityPortalEnter(EntityPortalEnterEvent event) {
 
         Player player = null;
         dNPC npc = null;
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         dEntity entity = new dEntity(event.getEntity());
         String entityType = entity.getEntityType().name();
 
         context.put("location", new dLocation(event.getLocation()));
         context.put("entity", entity.getDenizenObject());
 
         if (entity.isNPC()) { npc = entity.getDenizenNPC(); entityType = "npc"; }
         else if (entity.isPlayer()) player = entity.getPlayer();
 
         doEvents(Arrays.asList
                 ("entity enters portal",
                  entityType + " enters portal"),
                  npc, player, context);
     }
 
     // <--[event]
     // @Events
     // entity exits portal
     // <entity> exits portal
     //
     // @Triggers when an entity exits a portal.
     // @Context
     // <context.entity> returns the dEntity.
     // <context.location> returns the dLocation of the portal block touched by the entity.
     //
     // -->
     @EventHandler
     public void entityPortalExit(EntityPortalExitEvent event) {
 
         Player player = null;
         dNPC npc = null;
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         dEntity entity = new dEntity(event.getEntity());
         String entityType = entity.getEntityType().name();
 
         context.put("location", new dLocation(event.getTo()));
         context.put("entity", entity.getDenizenObject());
 
         if (entity.isNPC()) { npc = entity.getDenizenNPC(); entityType = "npc"; }
         else if (entity.isPlayer()) player = entity.getPlayer();
 
         doEvents(Arrays.asList
                 ("entity exits portal",
                  entityType + " exits portal"),
                  npc, player, context);
     }
 
     // <--[event]
     // @Events
     // entity shoots bow
     // <entity> shoots bow
     //
     // @Triggers when an entity shoots something out of a bow.
     // @Context
     // <context.entity> returns the dEntity that shot the bow.
     // <context.projectile> returns a dEntity of the projectile.
     //
     // @Determine
     // "CANCELLED" to stop the entity from shooting the bow.
     // dList(dEntity) to change the projectile(s) being shot.
     //
     // -->
     @EventHandler
     public void entityShootBow(EntityShootBowEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         Player player = null;
         dNPC npc = null;
 
         dItem bow = new dItem(event.getBow());
         dEntity projectile = new dEntity(event.getProjectile());
         dEntity entity = new dEntity(event.getEntity());
         String entityType = entity.getEntityType().name();
 
         context.put("bow", bow);
         context.put("projectile", projectile);
         context.put("entity", entity.getDenizenObject());
 
         if (entity.isNPC()) { npc = entity.getDenizenNPC(); entityType = "npc"; }
         else if (entity.isPlayer()) player = entity.getPlayer();
 
         String determination = doEvents(Arrays.asList
                 ("entity shoots bow",
                  "entity shoots " + bow.identify(),
                  entityType + " shoots bow",
                  entityType + " shoots " + bow.identify()),
                  npc, player, context);
 
         if (determination.toUpperCase().startsWith("CANCELLED")) {
             event.setCancelled(true);
         }
 
         // Don't use event.setProjectile() because it doesn't work
         else if (Argument.valueOf(determination).matchesArgumentList(dEntity.class)) {
 
             event.setCancelled(true);
 
             // Get the list of entities
             Object list = dList.valueOf(determination).filter(dEntity.class);
 
             @SuppressWarnings("unchecked")
             List<dEntity> newProjectiles = (List<dEntity>) list;
 
             // Go through all the entities, spawning/teleporting them
             for (dEntity newProjectile : newProjectiles) {
                 newProjectile.spawnAt(projectile.getLocation());
 
                 // Set the entity as the shooter of the projectile,
                 // where applicable
                 if (newProjectile.isProjectile()) {
                     newProjectile.setShooter(entity);
                 }
             }
 
             // Mount the projectiles on top of each other
             Position.mount(Conversion.convert(newProjectiles));
 
             // Get the last entity on the list, i.e. the one at the bottom
             // if there are many mounted on top of each other
             Entity lastProjectile = newProjectiles.get
                     (newProjectiles.size() - 1).getBukkitEntity();
 
             // Give it the same velocity as the arrow that would
             // have been shot by the bow
             lastProjectile.setVelocity(projectile.getVelocity());
         }
     }
 
     // <--[event]
     // @Events
     // entity tamed
     // <entity> tamed
     // player tames entity
     // player tames <entity>
     //
     // @Triggers when an entity is tamed.
     // @Context
     // <context.entity> returns a dEntity of the tamed entity.
     //
     // @Determine
     // "CANCELLED" to stop the entity from being tamed.
     //
     // -->
     @EventHandler
     public void entityTame(EntityTameEvent event) {
 
         Player player = null;
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         dEntity entity = new dEntity(event.getEntity());
         String entityType = entity.getEntityType().name();
         context.put("entity", entity);
 
         List<String> events = new ArrayList<String>();
         events.add("entity tamed");
         events.add(entityType + " tamed");
 
         if (event.getOwner() instanceof Player) {
             player = (Player) event.getOwner();
             events.add("player tames entity");
             events.add("player tames " + entityType);
         }
 
         String determination = doEvents(events, null, player, context);
 
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setCancelled(true);
     }
 
     // <--[event]
     // @Events
     // entity targets (<entity>)
     // entity targets (<entity>) because <cause>
     // <entity> targets (<entity>)
     // <entity> targets (<entity>) because <cause>
     //
     // @Triggers when an entity targets a new entity.
     // @Context
     // <context.entity> returns the targeting entity.
     // <context.reason> returns the reason the entity changed targets.
     // <context.target> returns the targeted entity.
     //
     // @Determine
     // "CANCELLED" to stop the entity from being targeted.
     // dEntity to make the entity target a different entity instead.
     //
     // -->
     @EventHandler
     public void entityTarget(EntityTargetEvent event) {
 
         Player player = null;
         dNPC npc = null;
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         final dEntity entity = new dEntity(event.getEntity());
 
         String entityType = entity.getEntityType().name();
         String reason = event.getReason().name();
 
         context.put("entity", entity);
         context.put("reason", new Element(reason));
 
         List<String> events = new ArrayList<String>();
         events.add("entity targets");
         events.add("entity targets because " + reason);
         events.add(entityType + " targets");
         events.add(entityType + " targets because " + reason);
 
         if (event.getTarget() != null) {
 
             dEntity target = new dEntity(event.getTarget());
             String targetType = target.getEntityType().name();
             context.put("target", target.getDenizenObject());
 
             if (target.isNPC()) { npc = target.getDenizenNPC(); targetType = "npc"; }
             else if (target.isPlayer()) player = target.getPlayer();
 
             events.add("entity targets entity");
             events.add("entity targets entity because " + reason);
             events.add("entity targets " + targetType);
             events.add("entity targets " + targetType + " because " + reason);
             events.add(entityType + " targets entity");
             events.add(entityType + " targets entity because " + reason);
             events.add(entityType + " targets " + targetType);
             events.add(entityType + " targets " + targetType + " because " + reason);
         }
 
         String determination = doEvents(events, npc, player, context);
 
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setCancelled(true);
 
             // If the determination matches a dEntity, change the event's target
             // using a scheduled task (otherwise, the target will not be changed)
             //
             // Note: this does not work with all monster types
 
             else if (dEntity.matches(determination)) {
 
                 final dEntity newTarget = dEntity.valueOf(determination);
 
                 Bukkit.getScheduler().scheduleSyncDelayedTask(DenizenAPI.getCurrentInstance(), new Runnable() {
                     @Override
                     public void run() {
                         entity.target(newTarget.getLivingEntity());
                     }
                 }, 1);
         }
     }
 
     // <--[event]
     // @Events
     // entity teleports
     // <entity> teleports
     //
     // @Triggers when an entity teleports.
     // @Context
     // <context.entity> returns the dEntity.
     // <context.origin> returns the dLocation the entity teleported from.
     // <context.entity> returns the dLocation the entity teleported to.
     //
     // @Determine
     // "CANCELLED" to stop the entity from teleporting.
     //
     // -->
     @EventHandler
     public void entityTeleport(EntityTeleportEvent event) {
 
         Player player = null;
         dNPC npc = null;
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         dEntity entity = new dEntity(event.getEntity());
         String entityType = entity.getEntityType().name();
 
         context.put("origin", new dLocation(event.getFrom()));
         context.put("destination", new dLocation(event.getTo()));
         context.put("entity", entity.getDenizenObject());
 
         if (entity.isNPC()) { npc = entity.getDenizenNPC(); entityType = "npc"; }
         else if (entity.isPlayer()) player = entity.getPlayer();
 
         String determination = doEvents(Arrays.asList
                 ("entity teleports",
                  entityType + " teleports"),
                  npc, player, context);
 
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setCancelled(true);
     }
 
     // <--[event]
     // @Events
     // entity unleashed (because <reason>)
     // <entity> unleashed (because <reason>)
     //
     // @Triggers when an entity is unleashed.
     // @Context
     // <context.entity> returns the dEntity.
     // <context.reason> returns an Element of the reason for the unleashing.
     //
     // -->
     @EventHandler
     public void entityUnleash(EntityUnleashEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         dEntity entity = new dEntity(event.getEntity());
         String entityType = entity.getEntityType().name();
         String reason = event.getReason().name();
 
         context.put("entity", entity.getDenizenObject());
         context.put("reason", new Element(reason));
 
         doEvents(Arrays.asList
                 ("entity unleashed",
                  "entity unleashed because " + reason,
                  entityType + " unleashed",
                  entityType + " unleashed because " + reason),
                  null, null, context);
     }
 
     // <--[event]
     // @Events
     // entity explosion primes
     // <entity> explosion primes
     //
     // @Triggers when an entity decides to explode.
     // @Context
     // <context.entity> returns the dEntity.
     // <context.origin> returns an Element of the explosion's radius.
     // <context.fire> returns an Element with a value of "true" if the explosion will create fire and "false" otherwise.
     //
     // @Determine
     // "CANCELLED" to stop the entity from deciding to explode.
     //
     // -->
     @EventHandler
     public void explosionPrimeEvent(ExplosionPrimeEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         Entity entity = event.getEntity();
 
         context.put("entity", new dEntity(entity));
         context.put("radius", new Element(event.getRadius()));
         context.put("fire", new Element(event.getFire()));
 
         String determination = doEvents(Arrays.asList
                 ("entity explosion primes",
                  entity.getType().name() + " explosion primes"),
                  null, null, context);
 
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setCancelled(true);
     }
 
     // <--[event]
     // @Events
     // entity changes food level
     // <entity> changes food level
     //
     // @Triggers when an entity's food level changes.
     // @Context
     // <context.entity> returns the dEntity.
     // <context.food> returns an Element(Integer) of the entity's new food level.
     //
     // @Determine
     // "CANCELLED" to stop the entity's food level from changing.
     // Element(Double) to set the entity's new food level.
     //
     // -->
     @EventHandler
     public void foodLevelChange(FoodLevelChangeEvent event) {
 
         Player player = null;
         dNPC npc = null;
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         dEntity entity = new dEntity(event.getEntity());
         String entityType = entity.getEntityType().name();
 
         context.put("food", new Element(event.getFoodLevel()));
         context.put("entity", entity.getDenizenObject());
 
         if (entity.isNPC()) { npc = entity.getDenizenNPC(); entityType = "npc"; }
         else if (entity.isPlayer()) player = entity.getPlayer();
 
         String determination = doEvents(Arrays.asList
                 ("entity changes food level",
                  entityType + " changes food level"),
                  npc, player, context);
 
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setCancelled(true);
         else if (Argument.valueOf(determination)
                 .matchesPrimitive(aH.PrimitiveType.Integer)) {
             event.setFoodLevel(aH.getIntegerFrom(determination));
         }
     }
 
     // <--[event]
     // @Events
     // horse jumps
     // (<color>) (<type>) jumps
     //
     // @Triggers when a horse jumps.
     // @Context
     // <context.entity> returns the dEntity of the horse.
     // <context.color> returns an Element of the horse's color.
     // <context.variant> returns an Element of the horse's variant.
     // <context.food> returns an Element(Float) of the jump's power.
     //
     // @Determine
     // "CANCELLED" to stop the horse from jumping.
     // Element(Double) to set the power of the jump.
     //
     // -->
     @EventHandler
     public void horseJump(HorseJumpEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         String variant = event.getEntity().getVariant().name();
         String color = event.getEntity().getColor().name();
         dEntity entity = new dEntity(event.getEntity());
 
         context.put("variant", new Element(variant));
         context.put("color", new Element(color));
         context.put("power", new Element(event.getPower()));
         context.put("entity", entity);
 
         String determination = doEvents(Arrays.asList
                 ("horse jumps",
                  variant + " jumps",
                  color + " jumps",
                  color + " " + variant + " jumps"),
                  null, null, context);
 
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setCancelled(true);
         else if (Argument.valueOf(determination)
                 .matchesPrimitive(aH.PrimitiveType.Float)) {
             event.setPower(aH.getFloatFrom(determination));
         }
     }
 
     // <--[event]
     // @Events
     // item despawns
     // <item> despawns
     // <material> despawns
     //
     // @Triggers when an item entity despawns.
     // @Context
     // <context.item> returns the dItem of the entity.
     // <context.entity> returns the dEntity.
     //
     // @Determine
     // "CANCELLED" to stop the item entity from despawning.
     //
     // -->
     @EventHandler
     public void itemDespawn(ItemDespawnEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         dItem item = new dItem(event.getEntity().getItemStack());
         dMaterial itemMaterial = item.getMaterial();
 
         context.put("item", item);
         context.put("entity", new dEntity(event.getEntity()));
 
         List<String> events = new ArrayList<String>();
         events.add("item despawns");
         events.add(item.identify() + " despawns");
         events.add(itemMaterial.identify() + " despawns");
 
         String determination = doEvents(events, null, null, context);
 
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setCancelled(true);
     }
 
     // <--[event]
     // @Events
     // item spawns
     // <item> spawns
     // <material> spawns
     //
     // @Triggers when an item entity spawns.
     // @Context
     // <context.item> returns the dItem of the entity.
     // <context.entity> returns the dEntity.
     //
     // @Determine
     // "CANCELLED" to stop the item entity from spawning.
     //
     // -->
     @EventHandler
     public void itemSpawn(ItemSpawnEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         dItem item = new dItem(event.getEntity().getItemStack());
         dMaterial itemMaterial = item.getMaterial();
 
         context.put("item", item);
         context.put("entity", new dEntity(event.getEntity()));
 
         List<String> events = new ArrayList<String>();
         events.add("item spawns");
         events.add(item.identify() + " spawns");
         events.add(itemMaterial.identify() + " spawns");
 
         if (!item.identify().equals(item.identify().split(":")[0])) {
             events.add(item.identify().split(":")[0] + " spawns");
         }
 
         String determination = doEvents(events, null, null, context);
 
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setCancelled(true);
     }
 
     // <--[event]
     // @Events
     // pig zapped
     //
     // @Triggers when a pig is zapped by lightning and turned into a pig zombie.
     // @Context
     // <context.pig> returns the dEntity of the pig.
     // <context.pig_zombie> returns the dEntity of the pig zombie.
     // <context.lightning> returns the dEntity of the lightning.
     //
     // @Determine
     // "CANCELLED" to stop the pig from being zapped.
     //
     // -->
     @EventHandler
     public void pigZap(PigZapEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         dEntity pig = new dEntity(event.getEntity());
         dEntity pigZombie = new dEntity(event.getPigZombie());
         dEntity lightning = new dEntity(event.getLightning());
 
         context.put("pig", pig);
         context.put("pig_zombie", pigZombie);
         context.put("lightning", lightning);
 
         String determination = doEvents(Arrays.asList
                 ("pig zapped"), null, null, context);
 
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setCancelled(true);
     }
 
     // <--[event]
     // @Events
     // projectile hits block
     // projectile hits <material>
     // <projectile> hits block
     // <projectile> hits <material>
     //
     // @Triggers when a projectile hits a block.
     // @Context
     // <context.projectile> returns the dEntity of the projectile.
     // <context.shooter> returns the dEntity of the shooter, if there is one.
     // <context.location> returns the dLocation of the block that was hit.
     //
     // -->
     @EventHandler
     public void projectileHit(ProjectileHitEvent event) {
 
         Player player = null;
         dNPC npc = null;
 
         dEntity projectile = new dEntity(event.getEntity());
 
         Block block = null;
         BlockIterator bi = new BlockIterator(projectile.getLocation().getWorld(), projectile.getLocation().toVector(), projectile.getLocation().getDirection().normalize(), 0, 4);
         while(bi.hasNext()) {
             block = bi.next();
             if(block.getTypeId() != 0) {
                 break;
             }
         }
 
         String entityType = projectile.getEntityType().name();
         dEntity shooter = projectile.getShooter();
         dMaterial material = new dMaterial(block.getType());
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         context.put("projectile", projectile);
         context.put("location", new dLocation(block.getLocation()));
 
         List<String> events = new ArrayList<String>();
         events.add("projectile hits block");
         events.add("projectile hits " + material.identify());
         events.add(entityType + " hits block");
         events.add(entityType + " hits " + material.identify());
 
         if (shooter != null) {
             context.put("shooter", shooter.getDenizenObject());
 
             // <--[event]
             // @Events
             // entity shoots block
             // entity shoots <material>
             // entity shoots <material> with <projectile>
             // <entity> shoots block
             // <entity> shoots <material>
             // <entity> shoots <material> with <projectile>
             //
             // @Triggers when a projectile shot by an entity hits a block.
             // @Context
             // <context.projectile> returns the dEntity of the projectile.
             // <context.shooter> returns the dEntity of the shooter, if there is one.
             // <context.location> returns the dLocation of the block that was hit.
             //
             // -->
             String shooterType = shooter.getEntityType().name();
 
             if (shooter.isNPC()) { npc = shooter.getDenizenNPC(); shooterType = "npc"; }
             else if (shooter.isPlayer()) player = shooter.getPlayer();
 
             events.add("entity shoots block");
             events.add("entity shoots block with " + entityType);
             events.add("entity shoots " + material.identify() + " with " + entityType);
             events.add(shooterType + " shoots block");
             events.add(shooterType + " shoots block with " + entityType);
             events.add(shooterType + " shoots " + material.identify() + " with " + entityType);
         }
 
         doEvents(events, npc, player, context);
     }
 
     // <--[event]
     // @Events
     // player dyes sheep (<color>)
     // sheep dyed (<color>)
     //
     // @Triggers when a sheep is dyed by a player.
     // @Context
     // <context.entity> returns the dEntity of the sheep.
     // <context.color> returns an Element of the color the sheep is being dyed.
     //
     // @Determine
     // "CANCELLED" to stop it from being dyed.
     // Element(String) that matches DyeColor to dye it a different color.
     //
     // -->
     @EventHandler
     public void sheepDyeWool(SheepDyeWoolEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         dEntity entity = new dEntity(event.getEntity());
         String color = event.getColor().name();
 
         context.put("entity", entity);
         context.put("color", new Element(color));
 
         String determination = doEvents(Arrays.asList
                 ("player dyes sheep",
                  "player dyes sheep " + color,
                  "sheep dyed",
                  "sheep dyed " + color), null, null, context);
 
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setCancelled(true);
        else if (determination.equals(determination.toUpperCase())) {
            try {
                event.setColor(DyeColor.valueOf(determination));
            }
            catch (IllegalArgumentException e) {
                dB.echoError("Unknown dye color " + determination);
            }
        }
     }
 
     // <--[event]
     // @Events
     // sheep regrows wool
     //
     // @Triggers when a sheep regrows wool.
     // @Context
     // <context.entity> returns the dEntity of the sheep.
     //
     // @Determine
     // "CANCELLED" to stop it from regrowing wool.
     //
     // -->
     @EventHandler
     public void sheepRegrowWool(SheepRegrowWoolEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         dEntity entity = new dEntity(event.getEntity());
 
         context.put("entity", entity);
 
         String determination = doEvents(Arrays.asList
                 ("sheep regrows wool"), null, null, context);
 
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setCancelled(true);
     }
 
     // <--[event]
     // @Events
     // slime splits (into <#>)
     //
     // @Triggers when a slime splits into smaller slimes.
     // @Context
     // <context.entity> returns the dEntity of the slime.
     // <context.count> returns an Element(Integer) of the number of smaller slimes it will split into.
     //
     // @Determine
     // "CANCELLED" to stop it from splitting.
     // Element(Integer) to set the number of smaller slimes it will split into.
     //
     // -->
     @EventHandler
     public void slimeSplit(SlimeSplitEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         dEntity entity = new dEntity(event.getEntity());
         int count = event.getCount();
 
         context.put("entity", entity);
         context.put("count", new Element(count));
 
         String determination = doEvents(Arrays.asList
                 ("slime splits",
                  "slime splits into " + count),
                  null, null, context);
 
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setCancelled(true);
         else if (Argument.valueOf(determination)
                 .matchesPrimitive(aH.PrimitiveType.Integer)) {
             event.setCount(aH.getIntegerFrom(determination));
         }
     }
 
 
     /////////////////////
     //   INVENTORY EVENTS
     /////////////////
 
     // <--[event]
     // @Events
     // item enchanted
     // <item> enchanted
     //
     // @Triggers when an item is enchanted.
     // @Context
     // <context.location> returns the dLocation of the enchanting table.
     // <context.inventory> returns the dInventory of the enchanting table.
     // <context.item> returns the dItem to be enchanted.
     //
     // @Determine
     // "CANCELLED" to stop the item from being enchanted.
     //
     // -->
     @EventHandler
     public void enchantItemEvent(EnchantItemEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
 
         Player player = event.getEnchanter();
         dItem item = new dItem(event.getItem());
 
         context.put("location", new dLocation(event.getEnchantBlock().getLocation()));
         context.put("inventory", new dInventory(event.getInventory()));
         context.put("item", item);
 
         String determination = doEvents(Arrays.asList
                 ("item enchanted",
                  item.identify() + " enchanted"),
                  null, player, context);
 
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setCancelled(true);
     }
 
     // <--[event]
     // @Events
     // player (<click type>) clicks (<item>) (in <inventory>)
     //
     // @Triggers when a player clicks in an inventory.
     // @Context
     // <context.item> returns the dItem the player has clicked on.
     // <context.item> returns the dInventory.
     // <context.click> returns an Element with the name of the click type.
     // <context.slot_type> returns an Element with the name of the slot type that was clicked.
     //
     // @Determine
     // "CANCELLED" to stop the player from clicking.
     //
     // -->
     @EventHandler
     public void inventoryClickEvent(InventoryClickEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         dItem item = null;
 
         Player player = (Player) event.getWhoClicked();
         String type = event.getInventory().getType().name();
         String click = event.getClick().name();
         String slotType = event.getSlotType().name();
 
         context.put("inventory", new dInventory(event.getInventory()));
         context.put("click", new Element(click));
         context.put("slot_type", new Element(slotType));
 
         List<String> events = new ArrayList<String>();
         events.add("player clicks in inventory");
         events.add("player clicks in " + type);
 
         String interaction = "player " + click + " clicks";
 
         events.add(interaction + " in inventory");
         events.add(interaction + " in " + type);
 
         if (event.getCurrentItem() != null) {
 
             item = new dItem(event.getCurrentItem());
             dMaterial itemMaterial = item.getMaterial();
 
             events.add("player clicks " +
                     item.identify() + " in inventory");
             events.add(interaction +
                     item.identify() + " in inventory");
             events.add(interaction +
                     item.identify() + " in " + type);
             events.add("player clicks " +
                     itemMaterial.identify() + " in inventory");
             events.add(interaction +
                     itemMaterial.identify() + " in inventory");
             events.add(interaction +
                     itemMaterial.identify() + " in " + type);
         }
 
         context.put("item", item);
 
         String determination = doEvents(events, null, player, context);
 
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setCancelled(true);
     }
 
     // <--[event]
     // @Events
     // player closes <inventory>
     //
     // @Triggers when a player closes an inventory.
     // @Context
     // <context.inventory> returns the dInventory.
     //
     // -->
     @EventHandler
     public void inventoryCloseEvent(InventoryCloseEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
 
         Player player = (Player) event.getPlayer();
         String type = event.getInventory().getType().name();
 
         context.put("inventory", new dInventory(event.getInventory()));
 
         doEvents(Arrays.asList
                 ("player closes inventory",
                  "player closes " + type),
                  null, player, context);
     }
 
     // <--[event]
     // @Events
     // player drags (<item>) (in <inventory>)
     //
     // @Triggers when a player drags in an inventory.
     // @Context
     // <context.item> returns the dItem the player has dragged.
     // <context.item> returns the dInventory.
     //
     // @Determine
     // "CANCELLED" to stop the player from dragging.
     //
     // -->
     @EventHandler
     public void inventoryDragEvent(InventoryDragEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         dItem item = null;
 
         Player player = (Player) event.getWhoClicked();
         String type = event.getInventory().getType().name();
 
         context.put("inventory", new dInventory(event.getInventory()));
 
         List<String> events = new ArrayList<String>();
         events.add("player drags");
         events.add("player drags in inventory");
         events.add("player drags in " + type);
 
         if (event.getOldCursor() != null) {
 
             item = new dItem(event.getOldCursor());
             dMaterial itemMaterial = item.getMaterial();
 
             events.add("player drags " +
                     item.identify());
             events.add("player drags " +
                     item.identify() + " in inventory");
             events.add("player drags " +
                     item.identify() + " in " + type);
             events.add("player drags " +
                     itemMaterial.identify());
             events.add("player drags " +
                     itemMaterial.identify() + " in inventory");
             events.add("player drags " +
                     itemMaterial.identify() + " in " + type);
         }
 
         context.put("item", item);
 
         String determination = doEvents(events, null, player, context);
 
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setCancelled(true);
     }
 
     // <--[event]
     // @Events
     // item moves from inventory (to <inventory>)
     // item moves from <inventory> (to <inventory>)
     //
     // @Triggers when an entity or block moves an item from one inventory to another.
     // @Context
     // <context.origin> returns the origin dInventory.
     // <context.destination> returns the destination dInventory.
     // <context.initiator> returns the dInventory that initiatied the item's transfer.
     // <context.item> returns the dItem that was moved.
     //
     // @Determine
     // "CANCELLED" to stop the item from being moved.
     // dItem to set a different item to be moved.
     //
     // -->
     @EventHandler
     public void inventoryMoveItemEvent(InventoryMoveItemEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
 
         dItem item = new dItem(event.getItem());
         String originType = event.getSource().getType().name();
         String destinationType = event.getDestination().getType().name();
 
         context.put("origin", new dInventory(event.getSource()));
         context.put("destination", new dInventory(event.getDestination()));
         context.put("initiator", new dInventory(event.getInitiator()));
         context.put("item", item);
 
         String determination = doEvents(Arrays.asList
                 ("item moves from inventory",
                  "item moves from " + originType,
                  "item moves from " + originType
                              + " to " + destinationType,
                  item.identify() + " moves from inventory",
                  item.identify() + " moves from " + originType,
                  item.identify() + " moves from " + originType
                              + " to " + destinationType),
                  null, null, context);
 
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setCancelled(true);
         if (dItem.matches(determination))
             event.setItem(dItem.valueOf(determination).getItemStack());
     }
 
     // <--[event]
     // @Events
     // player opens <inventory>
     //
     // @Triggers when a player opens an inventory.
     // @Context
     // <context.inventory> returns the dInventory.
     //
     // @Determine
     // "CANCELLED" to stop the player from opening the inventory.
     //
     // -->
     @EventHandler
     public void inventoryOpenEvent(InventoryOpenEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
 
         Player player = (Player) event.getPlayer();
         String type = event.getInventory().getType().name();
 
         context.put("inventory", new dInventory(event.getInventory()));
 
         String determination = doEvents(Arrays.asList
                 ("player opens inventory",
                  "player opens " + type),
                  null, player, context);
 
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setCancelled(true);
     }
 
     // <--[event]
     // @Events
     // inventory picks up item
     // inventory picks up <item>
     // <inventory> picks up item
     // <inventory> picks up <item>
     //
     // @Triggers when a hopper or hopper minecart picks up an item.
     // @Context
     // <context.inventory> returns the dInventory that picked up the item.
     // <context.item> returns the dItem.
     //
     // @Determine
     // "CANCELLED" to stop the item from being moved.
     //
     // -->
     @EventHandler
     public void inventoryPickupItemEvent(InventoryPickupItemEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
 
         dItem item = new dItem(event.getItem());
         dInventory inventory = new dInventory(event.getInventory());
         String type = inventory.getInventoryType().name();
 
         context.put("inventory", inventory);
         context.put("item", item);
 
         String determination = doEvents(Arrays.asList
                 ("inventory picks up item",
                  "inventory picks up " + item.identify(),
                  type + " picks up item",
                  type + " picks up " + item.identify()),
                  null, null, context);
 
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setCancelled(true);
     }
 
 
     /////////////////////
     //   PLAYER EVENTS
     /////////////////
 
     // <--[event]
     // @Events
     // player chats
     //
     // @Triggers when a player chats.
     // @Context
     // <context.message> returns the player's message as an Element.
     //
     // @Determine
     // "CANCELLED" to stop the player from chatting.
     // Element(String) to change the message.
     //
     // -->
     @EventHandler(priority = EventPriority.LOWEST)
     public void asyncPlayerChat(final AsyncPlayerChatEvent event) {
 
         // Return if "Use asynchronous event" is false in config file
         if (!Settings.WorldScriptChatEventAsynchronous()) return;
 
         final Map<String, dObject> context = new HashMap<String, dObject>();
         context.put("message", new Element(event.getMessage()));
 
         Callable<String> call = new Callable<String>() {
             @Override
             public String call() {
                 return doEvents(Arrays.asList("player chats"),
                         null, event.getPlayer(), context);
             }
         };
         String determination = null;
         try {
             determination = event.isAsynchronous() ? Bukkit.getScheduler().callSyncMethod(DenizenAPI.getCurrentInstance(), call).get() : call.call();
         } catch (InterruptedException e) {
             // TODO: Need to find a way to fix this eventually
             // e.printStackTrace();
         } catch (ExecutionException e) {
             e.printStackTrace();
         } catch (Exception e) {
             e.printStackTrace();
         }
 
         if (determination == null)
             return;
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setCancelled(true);
         else if (!determination.equals("none")) {
             event.setMessage(determination);
         }
     }
 
     // <--[event]
     // @Events
     // player animates (<animation>)
     //
     // @Triggers when a player performs an animation.
     // @Context
     // <context.animation> returns the name of the animation.
     //
     // @Determine
     // "CANCELLED" to stop the player from animating.
     //
     // -->
     @EventHandler
     public void playerAnimation(PlayerAnimationEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         String animation = event.getAnimationType().name();
         context.put("animation", new Element(animation));
 
         String determination = doEvents(Arrays.asList
                 ("player animates",
                  "player animates " + animation),
                  null, event.getPlayer(), context);
 
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setCancelled(true);
     }
 
     // <--[event]
     // @Events
     // player enters bed
     //
     // @Triggers when a player enters a bed.
     // @Context
     // <context.location> returns the dLocation of the bed.
     //
     // @Determine
     // "CANCELLED" to stop the player from entering the bed.
     //
     // -->
     @EventHandler
     public void playerBedEnter(PlayerBedEnterEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         context.put("location", new dLocation(event.getBed().getLocation()));
 
         String determination = doEvents
                 (Arrays.asList("player enters bed"),
                         null, event.getPlayer(), context);
 
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setCancelled(true);
     }
 
     // <--[event]
     // @Events
     // player leaves bed
     //
     // @Triggers when a player leaves a bed.
     // @Context
     // <context.location> returns the dLocation of the bed.
     //
     // -->
     @EventHandler
     public void playerBedLeave(PlayerBedLeaveEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         context.put("location", new dLocation(event.getBed().getLocation()));
 
         doEvents(Arrays.asList
                 ("player leaves bed"),
                 null, event.getPlayer(), context);
     }
 
     // <--[event]
     // @Events
     // player empties bucket
     //
     // @Triggers when a player empties a bucket.
     // @Context
     // <context.item> returns the dItem of the bucket.
     // <context.location> returns the dLocation of the block clicked with the bucket.
     //
     // @Determine
     // "CANCELLED" to stop the player from emptying the bucket.
     // dItem to set the item in the player's hand after the event.
     //
     // -->
     @EventHandler
     public void playerBucketEmpty(PlayerBucketEmptyEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         context.put("item", new dItem(event.getBucket()));
         context.put("location", new dLocation(event.getBlockClicked().getLocation()));
 
         String determination = doEvents(Arrays.asList
                 ("player empties bucket"),
                  null, event.getPlayer(), context);
 
         // Handle message
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setCancelled(true);
         if (dItem.matches(determination)) {
             ItemStack is = dItem.valueOf(determination).getItemStack();
             event.setItemStack( is != null ? is : new ItemStack(Material.AIR));
         }
 
     }
 
     // <--[event]
     // @Events
     // player fills bucket
     //
     // @Triggers when a player fills a bucket.
     // @Context
     // <context.item> returns the dItem of the bucket.
     // <context.location> returns the dLocation of the block clicked with the bucket.
     //
     // @Determine
     // "CANCELLED" to stop the player from filling the bucket.
     // dItem to set the item in the player's hand after the event.
     //
     // -->
     @EventHandler
     public void playerBucketFill(PlayerBucketFillEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         context.put("item", new dItem(event.getBucket()));
         context.put("location", new dLocation(event.getBlockClicked().getLocation()));
 
         String determination = doEvents(Arrays.asList
                 ("player fills bucket"),
                  null, event.getPlayer(), context);
 
         // Handle message
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setCancelled(true);
         if (dItem.matches(determination)) {
             ItemStack is = dItem.valueOf(determination).getItemStack();
             event.setItemStack( is != null ? is : new ItemStack(Material.AIR));
         }
     }
 
     // <--[event]
     // @Events
     // player changes world
     // player changes world from <world>
     // player changes world to <world>
     // player changes world from <world> to <world>
     //
     // @Triggers when a player moves to a different world.
     // @Context
     // <context.origin_world> returns the dWorld that the player was previously on.
     //
     // -->
     @EventHandler
     public void playerChangedWorld(PlayerChangedWorldEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         dWorld originWorld = new dWorld(event.getFrom());
         dWorld destinationWorld = new dWorld(event.getPlayer().getWorld());
         context.put("origin_world", originWorld);
 
         doEvents(Arrays.asList
                 ("player changes world",
                  "player changes world from " + originWorld.identify(),
                  "player changes world to " + destinationWorld.identify(),
                  "player changes world from " + originWorld.identify() +
                         " to " + destinationWorld.identify()),
                  null, event.getPlayer(), context);
     }
 
     // Shares description with asyncPlayerChat
     @EventHandler
     public void playerChat(final PlayerChatEvent event) {
 
         // Return if "Use asynchronous event" is true in config file
         if (Settings.WorldScriptChatEventAsynchronous()) return;
 
         final Map<String, dObject> context = new HashMap<String, dObject>();
         context.put("message", new Element(event.getMessage()));
 
         String determination = doEvents(Arrays.asList("player chats"),
                 null, event.getPlayer(), context);
 
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setCancelled(true);
         else if (!determination.equals("none")) {
             event.setMessage(determination);
         }
     }
 
     // <--[example]
     // @Title On Command Event tutorial
     // @Description
     // Denizen contains the ability to run script entries in the form
     // of a bukkit /command. Here's an example script that shows basic usage.
     //
     // @Code
     // # +--------------------
     // # | On Command Event tutorial
     // # |
     // # | Denizen contains the ability to run script entries in the form
     // # | of a Bukkit /command. Here's an example script that shows basic usage.
     //
     // On Command Event Tutorial:
     //  type: world
     //
     // # +-- EVENTS: Node --+
     // # To 'hook' into the on command event, just create a 'on <command_name> command'
     // # node as a child of the events node in any world script. Change out <command_name>
     // # with the desired name of the command. This can only be one word.
     //
     //   events:
     //
     //     # The following example will trigger on the use of '/testcommand'
     //     on testcommand command:
     //
     //     # Why not state the obvious? Just to be sure!
     //     - narrate 'You just used the /testcommand command!'
     //
     //     # You can utilize any arguments that come along with the command, too!
     //     # <c.args> returns a list of the arguments, run through the Denizen argument
     //     # interpreter. Using quotes will allow the use of multiple word arguments,
     //     # just like Denizen!
     //     # Just need what was typed after the command? Use <c.raw_args> for a String
     //     # Element containing the uninterpreted arguments.
     //     - define arg_size <c.args.size>
     //     - narrate "'%arg_size%' arguments were used."
     //     - if %arg_size% > 0 {
     //       - narrate "'<c.args.get[1]>' was the first argument."
     //       - narrate "Here's a list of all the arguments<&co> <c.args.as_cslist>"
     //       }
     //
     //     # When a command isn't found, Bukkit reports an error. To let bukkit know
     //     # that the command was handled, use the 'determine fulfilled' command/arg.
     //     - determine fulfilled
     //
     // -->
 
     // <--[event]
     // @Events
     // command
     // <command_name> command
     //
     // @Triggers when a player or console runs a Bukkit command. This happens before
     // any code of established commands allowing scripters to 'override' existing commands.
     // @Context
     // <context.command> returns the command name as an Element.
     // <context.raw_args> returns any args used as an Element.
     // <context.args> returns a dList of the arguments, parsed with Denizen's
     //   argument parser. Just like any Denizen Command, quotes and tags can be used.
     // <context.server> returns true if the command was run from the console.
     //
     // @Determine
     // "FULFILLED" to tell Bukkit the command was handled.
     //
     // -->
     @EventHandler
     public void playerCommandPreprocess(PlayerCommandPreprocessEvent event) {
         Map<String, dObject> context = new HashMap<String, dObject>();
 
         dPlayer player = dPlayer.valueOf(event.getPlayer().getName());
 
         // Well, this is ugly :(
         // Fill tags in any arguments
         List<String> args = Arrays.asList(
                 aH.buildArgs(
                         TagManager.tag(player, null,
                                 (event.getMessage().split(" ").length > 1 ? event.getMessage().split(" ", 2)[1] : ""))));
 
         dList args_list = new dList(args);
 
         String command = event.getMessage().split(" ")[0].replace("/", "").toUpperCase();
 
         // Fill context
         context.put("args", args_list);
         context.put("command", new Element(command));
         context.put("raw_args", new Element((event.getMessage().split(" ").length > 1
                 ? event.getMessage().split(" ", 2)[1] : "")));
         context.put("server", Element.FALSE);
         String determination;
 
         // Run any event scripts and get the determination.
         determination = doEvents(Arrays.asList
                 ("command",
                  command + " command"),
                  null, event.getPlayer(), context).toUpperCase();
 
         // If a script has determined fulfilled, cancel this event so the player doesn't
         // receive the default 'Invalid command' gibberish from bukkit.
         if (determination.equals("FULFILLED") || determination.equals("CANCELLED"))
             event.setCancelled(true);
     }
 
     // <--[event]
     // @Events
     // player dies
     // player death
     //
     // @Triggers when a player dies.
     // @Context
     // <context.message> returns an Element of the death message.
     //
     // @Determine
     // Element(String) to change the death message.
     //
     // -->
     @EventHandler
     public void playerDeath(PlayerDeathEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         context.put("message", new Element(event.getDeathMessage()));
 
         String determination = doEvents(Arrays.asList
                 ("player dies",
                  "player death"),
                  null, event.getEntity(), context);
 
         // Handle message
         if (!determination.equals("none")) {
             event.setDeathMessage(determination);
         }
     }
 
     // <--[event]
     // @Events
     // player drops item
     // player drops <item>
     //
     // @Triggers when a player drops an item.
     // @Context
     // <context.item> returns the dItem.
     // <context.entity> returns a dEntity of the item.
     // <context.location> returns a dLocation of the item's location.
     //
     // @Determine
     // "CANCELLED" to stop the item from being dropped.
     //
     // -->
     @EventHandler
     public void playerDropItem(PlayerDropItemEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         dItem item = new dItem(event.getItemDrop().getItemStack());
         context.put("item", item);
         context.put("entity", new dEntity(event.getItemDrop()));
         context.put("location", new dLocation(event.getItemDrop().getLocation()));
 
         List<String> events = new ArrayList<String>();
 
         events.add("player drops item");
         events.add("player drops " + item.identify());
 
         String determination = doEvents(events, null, event.getPlayer(), context);
 
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setCancelled(true);
     }
 
     // <--[event]
     // @Events
     // player throws (hatching/non-hatching) egg
     //
     // @Triggers when a player throws an egg.
     // @Context
     // <context.item> returns the dEntity of the egg.
     // <context.is_hatching> returns an Element with a value of "true" if the egg will hatch and "false" otherwise.
     //
     // @Determine
     // dEntity to set the type of the hatching entity.
     //
     // -->
     @EventHandler
     public void playerEggThrow(PlayerEggThrowEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         dEntity egg = new dEntity(event.getEgg());
         context.put("egg", egg);
         context.put("is_hatching", new Element(event.isHatching()));
 
         List<String> events = new ArrayList<String>();
         events.add("player throws egg");
 
         if (event.isHatching()) events.add("player throws hatching egg");
         else                    events.add("player throws non-hatching egg");
 
         String determination = doEvents(events, null, event.getPlayer(), context);
 
         if (dEntity.matches(determination)) {
             event.setHatching(true);
             event.setHatchingType(dEntity.valueOf(determination).getEntityType());
         }
     }
 
     // <--[event]
     // @Events
     // player fishes (while <state>)
     //
     // @Triggers when a player uses a fishing rod.
     // @Context
     // <context.hook> returns a dItem of the hook.
     // <context.state> returns an Element of the fishing state.
     // <context.entity> returns a dEntity, dPlayer or dNPC of the entity being fished.
     //
     // @Determine
     // "CANCELLED" to stop the player from fishing.
     //
     // -->
     @EventHandler
     public void playerFish(PlayerFishEvent event) {
 
 
         dNPC npc = null;
 
         String state = event.getState().name();
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         context.put("hook", new dEntity(event.getHook()));
         context.put("state", new Element(state));
 
         List<String> events = new ArrayList<String>();
         events.add("player fishes");
         events.add("player fishes while " + state);
 
         if (event.getCaught() != null) {
 
             dEntity entity = new dEntity(event.getCaught());
             String entityType = entity.getEntityType().name();
             context.put("entity", entity.getDenizenObject());
 
             if (entity.isNPC()) { npc = entity.getDenizenNPC(); entityType = "npc"; }
 
             events.add("player fishes " + entityType);
             events.add("player fishes " + entityType + " while " + state);
         }
 
         String determination = doEvents(events, npc, event.getPlayer(), context);
 
         // Handle message
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setCancelled(true);
     }
 
     // <--[event]
     // @Events
     // player changes gamemode (to <gamemode>)
     //
     // @Triggers when a player's gamemode is changed.
     // @Context
     // <context.gamemode> returns an Element of the gamemode.
     //
     // @Determine
     // "CANCELLED" to stop the gamemode from being changed.
     //
     // -->
     @EventHandler
     public void playerGameModeChange(PlayerGameModeChangeEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         context.put("gamemode", new Element(event.getNewGameMode().name()));
 
         String determination = doEvents(Arrays.asList
                 ("player changes gamemode",
                  "player changes gamemode to " + event.getNewGameMode().name()),
                  null, event.getPlayer(), context);
 
         // Handle message
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setCancelled(true);
     }
 
     // <--[event]
     // @Events
     // player (<click type>) clicks (<material>) (with <item>)
     // player stands on <pressure plate>
     //
     // @Triggers when a player clicks on a block or stands on a pressure plate.
     // @Context
     // <context.item> returns the dItem the player is clicking with.
     // <context.location> returns the dLocation the player is clicking on.
     //
     // @Determine
     // "CANCELLED" to stop the click from happening.
     //
     // -->
     @EventHandler
     public void playerInteract(PlayerInteractEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         Action action = event.getAction();
         dItem item = null;
         dMaterial itemMaterial = null;
 
         List<String> events = new ArrayList<String>();
 
         String interaction;
 
         if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK)
             interaction = "player left clicks";
         else if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)
             interaction = "player right clicks";
         // The only other action is PHYSICAL, which is triggered when a player
         // stands on a pressure plate
         else interaction = "player stands on";
 
         events.add(interaction);
 
         if (event.hasItem()) {
             item = new dItem(event.getItem());
             itemMaterial = item.getMaterial();
             context.put("item", item);
 
             events.add(interaction + " with item");
             events.add(interaction + " with " + item.identify());
             events.add(interaction + " with " + itemMaterial.identify());
         }
 
         if (event.hasBlock()) {
             Block block = event.getClickedBlock();
             context.put("location", new dLocation(block.getLocation()));
 
             interaction = interaction + " " + block.getType().name();
             events.add(interaction);
 
             if (event.hasItem()) {
                 events.add(interaction + " with item");
                 events.add(interaction + " with " + item.identify());
                 events.add(interaction + " with " + itemMaterial.identify());
             }
         }
 
         String determination = doEvents(events, null, event.getPlayer(), context);
 
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setCancelled(true);
     }
 
     // <--[event]
     // @Events
     // player right clicks entity (with item)
     //
     // @Triggers when a player right clicks on an entity.
     // @Context
     // <context.entity> returns the dEntity the player is clicking on.
     // <context.item> returns the dItem the player is clicking with.
     //
     // @Determine
     // "CANCELLED" to stop the click from happening.
     //
     // -->
     @EventHandler
     public void playerInteractEntity(PlayerInteractEntityEvent event) {
 
         String determination;
         dNPC npc = null;
 
         dItem item = new dItem(event.getPlayer().getItemInHand());
         dMaterial itemMaterial = item.getMaterial();
         dEntity entity = new dEntity(event.getRightClicked());
         String entityType = entity.getEntityType().name();
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         context.put("location", new dLocation(event.getRightClicked().getLocation()));
         context.put("entity", entity.getDenizenObject());
 
         if (entity.isNPC()) { npc = entity.getDenizenNPC(); entityType = "npc"; }
 
         List<String> events = new ArrayList<String>();
         events.add("player right clicks entity");
         events.add("player right clicks " + entityType);
         events.add("player right clicks entity with " +
                 item.identify());
         events.add("player right clicks " + entityType + " with " +
                 item.identify());
         events.add("player right clicks entity with " +
                 itemMaterial.identify());
         events.add("player right clicks " + entityType + " with " +
                 itemMaterial.identify());
 
         if (entity.getBukkitEntity() instanceof ItemFrame) {
             dItem itemFrame = new dItem(((ItemFrame) entity.getBukkitEntity()).getItem());
             dMaterial itemFrameMaterial = itemFrame.getMaterial();
             context.put("itemframe", itemFrame);
 
             events.add("player right clicks " + entityType + " " +
                     itemFrame.identify());
             events.add("player right clicks " + entityType + " " +
                     itemFrameMaterial.identify());
         }
 
         determination = doEvents(events, npc, event.getPlayer(), context);
 
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setCancelled(true);
     }
 
     // <--[event]
     // @Events
     // player consumes item
     // player consumes <item>
     //
     // @Triggers when a player consumes an item.
     // @Context
     // <context.item> returns the dItem.
     //
     // @Determine
     // "CANCELLED" to stop the item from being consumed.
     //
     // -->
     @EventHandler
     public void playerItemConsume(PlayerItemConsumeEvent event) {
 
         dItem item = new dItem(event.getItem());
         dMaterial itemMaterial = item.getMaterial();
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         context.put("item", item);
 
         List<String> events = new ArrayList<String>();
         events.add("player consumes " + item.identify());
         events.add("player consumes " + itemMaterial.identify());
 
         String determination = doEvents(events, null, event.getPlayer(), context);
 
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setCancelled(true);
     }
 
     // <--[event]
     // @Events
     // player joins
     // player join
     //
     // @Triggers when a player joins the server.
     // @Context
     // <context.message> returns an Element of the join message.
     //
     // @Determine
     // Element(String) to change the join message.
     //
     // -->
     @EventHandler
     public void playerJoinEvent(PlayerJoinEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         context.put("message", new Element(event.getJoinMessage()));
 
         String determination = doEvents(Arrays.asList
                 ("player joins",
                  "player join"),
                  null, event.getPlayer(), context);
 
         // Handle message
         if (!determination.equals("none")) {
             event.setJoinMessage(determination);
         }
     }
 
     // <--[event]
     // @Events
     // player kicked
     //
     // @Triggers when a player is kicked from the server.
     // @Context
     // <context.message> returns an Element of the kick message.
     //
     // @Determine
     // Element(String) to change the kick message.
     //
     // -->
     @EventHandler
     public void playerKick(PlayerKickEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         context.put("message", new Element(event.getLeaveMessage()));
 
         String determination = doEvents(Arrays.asList
                 ("player kicked"),
                  null, event.getPlayer(), context);
 
         if (!determination.equals("none")) {
             event.setLeaveMessage(determination);
         }
     }
 
     // <--[event]
     // @Events
     // player leashes entity
     // player leashes <entity>
     //
     // @Triggers when a player leashes an entity.
     // @Context
     // <context.entity> returns the dEntity of the leashed entity.
     // <context.holder> returns the dEntity that is holding the leash.
     //
     // -->
     @EventHandler
     public void playerLeashEntity(PlayerLeashEntityEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         dEntity entity = new dEntity(event.getEntity());
         String entityType = entity.getEntityType().name();
 
         context.put("entity", entity);
         context.put("holder", new dEntity(event.getLeashHolder()));
 
         doEvents(Arrays.asList
                 ("player leashes entity",
                  "entity leashes " + entityType),
                  null, event.getPlayer(), context);
     }
 
     // <--[event]
     // @Events
     // player levels up (from <level>/to <level>)
     //
     // @Triggers when a player levels up.
     // @Context
     // <context.level> returns an Element of the player's new level.
     //
     // -->
     @EventHandler
     public void playerLevelChange(PlayerLevelChangeEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         context.put("level", new Element(event.getNewLevel()));
 
         doEvents(Arrays.asList
                 ("player levels up",
                  "player levels up to " + event.getNewLevel(),
                  "player levels up from " + event.getOldLevel()),
                  null, event.getPlayer(), context);
     }
 
     // <--[event]
     // @Events
     // player logs in
     // player login
     //
     // @Triggers when a player logs in to the server.
     // @Context
     // <context.hostname> returns an Element of the player's hostname.
     //
     // @Determine
     // "KICKED" to kick the player from the server.
     //
     // -->
     @EventHandler
     public void playerLogin(PlayerLoginEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         context.put("hostname", new Element(event.getHostname()));
 
         String determination = doEvents(Arrays.asList
                 ("player logs in",
                  "player login"),
                  null, event.getPlayer(), context);
 
         if (determination.toUpperCase().startsWith("KICKED"))
             event.disallow(PlayerLoginEvent.Result.KICK_OTHER, determination);
     }
 
     // <--[event]
     // @Events
     // player walks over notable
     // player walks over <notable>
     //
     // @Triggers when a player walks over a notable location.
     // @Context
     // <context.notable> returns an Element of the notable location's name.
     //
     // @Determine
     // "CANCELLED" to stop the player from moving to the notable location.
     //
     // -->
     @EventHandler
     public void playerMove(PlayerMoveEvent event) {
         if (event.getFrom().getBlock().equals(event.getTo().getBlock())) return;
 
         String name = dLocation.getSaved(event.getPlayer().getLocation());
 
         if (name != null) {
             Map<String, dObject> context = new HashMap<String, dObject>();
             context.put("notable", new Element(name));
 
             String determination = doEvents(Arrays.asList
                     ("player walks over notable",
                      "player walks over " + name,
                      "walked over notable",
                      "walked over " + name),
                      null, event.getPlayer(), context);
 
             if (determination.toUpperCase().startsWith("CANCELLED") ||
                     determination.toUpperCase().startsWith("FROZEN"))
                 event.setCancelled(true);
         }
     }
 
     // <--[event]
     // @Events
     // player picks up item
     // player picks up <item>
     // player takes item
     // player takes <item>
     //
     // @Triggers when a player picks up an item.
     // @Context
     // <context.item> returns the dItem.
     // <context.entity> returns a dEntity of the item.
     // <context.location> returns a dLocation of the item's location.
     //
     // @Determine
     // "CANCELLED" to stop the item from picked up.
     //
     // -->
     @EventHandler
     public void playerPickupItem(PlayerPickupItemEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         dItem item = new dItem(event.getItem().getItemStack());
         context.put("item", item);
         context.put("entity", new dEntity(event.getItem()));
         context.put("location", new dLocation(event.getItem().getLocation()));
 
         List<String> events = new ArrayList<String>();
 
         events.add("player picks up item");
         events.add("player picks up " + item.identify());
         events.add("player takes item");
         events.add("player takes " + item.identify());
 
         String determination = doEvents(events, null, event.getPlayer(), context);
 
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setCancelled(true);
     }
 
     // <--[event]
     // @Events
     // player quits
     // player quit
     //
     // @Triggers when a player quit the server.
     // @Context
     // <context.message> returns an Element of the quit message.
     //
     // @Determine
     // Element(String) to change the quit message.
     //
     // -->
     @EventHandler
     public void playerQuit(PlayerQuitEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         context.put("message", new Element(event.getQuitMessage()));
 
         String determination = doEvents(Arrays.asList
                 ("player quits",
                  "player quit"),
                  null, event.getPlayer(), context);
 
         if (!determination.equals("none")) {
             event.setQuitMessage(determination);
         }
     }
 
     // <--[event]
     // @Events
     // player respawns (at bed/elsewhere)
     //
     // @Triggers when a player respawns.
     // @Context
     // <context.location> returns a dLocation of the respawn location.
     //
     // @Determine
     // dLocation to change the respawn location.
     //
     // -->
     @EventHandler
     public void playerRespawn(PlayerRespawnEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         context.put("location", new dLocation(event.getRespawnLocation()));
 
         List<String> events = new ArrayList<String>();
         events.add("player respawns");
 
         if (event.isBedSpawn()) events.add("player respawns at bed");
         else                    events.add("player respawns elsewhere");
 
         String determination = doEvents(events, null, event.getPlayer(), context);
 
         if (dLocation.matches(determination)) {
             dLocation location = dLocation.valueOf(determination);
 
             if (location != null) event.setRespawnLocation(location);
         }
     }
 
     // <--[event]
     // @Events
     // player shears entity
     // player shears <entity>
     // player shears <color> sheep
     //
     // @Triggers when a player shears an entity.
     // @Context
     // <context.state> returns the dEntity.
     //
     // @Determine
     // "CANCELLED" to stop the player from shearing the entity.
     //
     // -->
     @EventHandler
     public void playerShearEntity(PlayerShearEntityEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         dEntity entity = new dEntity(event.getEntity());
         String entityType = entity.getEntityType().name();
 
         context.put("entity", entity);
 
         List<String> events = new ArrayList<String>();
         events.add("player shears entity");
         events.add("player shears " + entityType);
 
         if (entity.getEntityType().equals(EntityType.SHEEP)) {
             String color = ((Sheep) entity.getBukkitEntity()).getColor().name();
             events.add("player shears " + color + " sheep");
         }
 
         String determination = doEvents(events, null, event.getPlayer(), context);
 
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setCancelled(true);
     }
 
     // <--[event]
     // @Events
     // player toggles flight
     // player starts/stops flying
     //
     // @Triggers when a player starts or stops flying.
     // @Context
     // <context.state> returns an Element with a value of "true" if the player is now flying and "false" otherwise.
     //
     // @Determine
     // "CANCELLED" to stop the player from toggling flying.
     //
     // -->
     @EventHandler
     public void playerToggleFlight(PlayerToggleFlightEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         context.put("state", new Element(event.isFlying()));
 
         String determination = doEvents(Arrays.asList
                 ("player toggles flight",
                  "player " + (event.isFlying() ? "starts" : "stops") + " flying"),
                  null, event.getPlayer(), context);
 
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setCancelled(true);
     }
 
     // <--[event]
     // @Events
     // player toggles sneak
     // player starts/stops sneaking
     //
     // @Triggers when a player starts or stops sneaking.
     // @Context
     // <context.state> returns an Element with a value of "true" if the player is now sneaking and "false" otherwise.
     //
     // @Determine
     // "CANCELLED" to stop the player from toggling sneaking.
     //
     // -->
     @EventHandler
     public void playerToggleSneak(PlayerToggleSneakEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         context.put("state", new Element(event.isSneaking()));
 
         String determination = doEvents(Arrays.asList
                 ("player toggles sneak",
                  "player " + (event.isSneaking() ? "starts" : "stops") + " sneaking"),
                  null, event.getPlayer(), context);
 
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setCancelled(true);
     }
 
     // <--[event]
     // @Events
     // player toggles sprint
     // player starts/stops sprinting
     //
     // @Triggers when a player starts or stops sprinting.
     // @Context
     // <context.state> returns an Element with a value of "true" if the player is now sprinting and "false" otherwise.
     //
     // @Determine
     // "CANCELLED" to stop the player from toggling sprinting.
     //
     // -->
     @EventHandler
     public void playerToggleSprint(PlayerToggleSprintEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         context.put("state", new Element(event.isSprinting()));
 
         String determination = doEvents(Arrays.asList
                 ("player toggles sprint",
                  "player " + (event.isSprinting() ? "starts" : "stops") + " sprinting"),
                  null, event.getPlayer(), context);
 
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setCancelled(true);
     }
 
 
     /////////////////////
     //   SERVER EVENTS
     /////////////////
 
     // Shares description with playerCommandPreprocess
     @EventHandler
     public void serverCommand(ServerCommandEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
 
         List<String> args = Arrays.asList(
                 aH.buildArgs(
                         TagManager.tag(null, null,
                                 (event.getCommand().split(" ").length > 1 ? event.getCommand().split(" ", 2)[1] : ""))));
 
         dList args_list = new dList(args);
 
         String command = event.getCommand().split(" ")[0].replace("/", "").toUpperCase();
 
         // Fill context
         context.put("args", args_list);
         context.put("command", new Element(command));
         context.put("raw_args", new Element((event.getCommand().split(" ").length > 1 ? event.getCommand().split(" ", 2)[1] : "")));
         context.put("server", Element.TRUE);
 
         doEvents(Arrays.asList("command",
                 command + " command"),
                 null, null, context);
     }
 
 
     /////////////////////
     //   VEHICLE EVENTS
     /////////////////
 
     // <--[event]
     // @Events
     // vehicle collides with block
     // vehicle collides with <material>
     // <vehicle> collides with block
     // <vehicle> collides with <material>
     //
     // @Triggers when a vehicle collides with a block.
     // @Context
     // <context.vehicle> returns the dEntity of the vehicle.
     // <context.location> returns the dLocation of the block.
     //
     // -->
     @EventHandler
     public void vehicleBlockCollision(VehicleBlockCollisionEvent event) {
 
         Player player = null;
         dNPC npc = null;
 
         dEntity vehicle = new dEntity(event.getVehicle());
         String vehicleType = vehicle.getEntityType().name();
         dMaterial material = new dMaterial(event.getBlock().getType());
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         context.put("vehicle", vehicle);
         context.put("location", new dLocation(event.getBlock().getLocation()));
 
         List<String> events = new ArrayList<String>();
         events.add("vehicle collides with block");
         events.add("vehicle collides with " + material.identify());
         events.add(vehicleType + " collides with block");
         events.add(vehicleType + " collides with " + material.identify());
 
         doEvents(events, npc, player, context);
     }
 
     // <--[event]
     // @Events
     // vehicle collides with entity
     // vehicle collides with <entity>
     // <vehicle> collides with entity
     // <vehicle> collides with <entity>
     //
     // @Triggers when a vehicle collides with an entity.
     // @Context
     // <context.vehicle> returns the dEntity of the vehicle.
     // <context.entity> returns the dEntity of the entity the vehicle has collided with.
     //
     // @Determine
     // "CANCELLED" to stop the collision from happening.
     // "NOPICKUP" to stop the vehicle from picking up the entity.
     //
     // -->
     @EventHandler
     public void vehicleEntityCollision(VehicleEntityCollisionEvent event) {
 
         Player player = null;
         dNPC npc = null;
 
         dEntity vehicle = new dEntity(event.getVehicle());
         String vehicleType = vehicle.getEntityType().name();
         dEntity entity = new dEntity(event.getEntity());
         String entityType = entity.getEntityType().name();
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         context.put("vehicle", vehicle);
         context.put("entity", entity.getDenizenObject());
 
         if (entity.isNPC()) { npc = entity.getDenizenNPC(); entityType = "npc"; }
         else if (entity.isPlayer()) player = entity.getPlayer();
 
         List<String> events = new ArrayList<String>();
         events.add("vehicle collides with entity");
         events.add("vehicle collides with " + entityType);
         events.add(vehicleType + " collides with entity");
         events.add(vehicleType + " collides with " + entityType);
 
         String determination = doEvents(events, npc, player, context);
 
         if (determination.toUpperCase().startsWith("CANCELLED"))
                 event.setCancelled(true);
         if (determination.toUpperCase().startsWith("NOPICKUP"))
                 event.setPickupCancelled(true);
     }
 
     // <--[event]
     // @Events
     // vehicle created
     // <vehicle> created
     //
     // @Triggers when a vehicle is created.
     // @Context
     // <context.vehicle> returns the dEntity of the vehicle.
     //
     // @Determine
     // "CANCELLED" to stop the entity from entering the vehicle.
     //
     // -->
     @EventHandler
     public void vehicleCreate(VehicleCreateEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
 
         dEntity vehicle = new dEntity(event.getVehicle());
         String vehicleType = vehicle.getEntityType().name();
 
         context.put("vehicle", vehicle);
 
         doEvents(Arrays.asList
                 ("vehicle created",
                  vehicleType + " created"),
                  null, null, context);
     }
 
     // <--[event]
     // @Events
     // vehicle damaged
     // <vehicle> damaged
     // entity damages vehicle
     // <entity> damages vehicle
     // entity damages <vehicle>
     // <entity> damages <vehicle>
     //
     // @Triggers when a vehicle is damaged.
     // @Context
     // <context.vehicle> returns the dEntity of the vehicle.
     // <context.entity> returns the dEntity of the attacking entity.
     //
     // @Determine
     // "CANCELLED" to stop the entity from damaging the vehicle.
     // Element(Double) to set the value of the damage received by the vehicle.
     //
     // -->
     @EventHandler
     public void vehicleDamage(VehicleDamageEvent event) {
 
         Player player = null;
         dNPC npc = null;
 
         dEntity vehicle = new dEntity(event.getVehicle());
         String vehicleType = vehicle.getEntityType().name();
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         context.put("damage", new Element(event.getDamage()));
         context.put("vehicle", vehicle);
 
         List<String> events = new ArrayList<String>();
         events.add("vehicle damaged");
         events.add(vehicleType + " damaged");
 
         if (event.getAttacker() != null) {
 
             dEntity entity = new dEntity(event.getAttacker());
             String entityType = entity.getEntityType().name();
             context.put("entity", entity.getDenizenObject());
 
             if (entity.isPlayer()) {
                 player = entity.getPlayer();
             }
             else {
                 npc = entity.getDenizenNPC();
                 entityType = "npc";
             }
 
             events.add("entity damages vehicle");
             events.add("entity damages " + vehicleType);
             events.add(entityType + " damages vehicle");
             events.add(entityType + " damages " + vehicleType);
         }
 
         String determination = doEvents(events, npc, player, context);
 
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setCancelled(true);
 
         else if (Argument.valueOf(determination)
                 .matchesPrimitive(aH.PrimitiveType.Double)) {
             event.setDamage(aH.getDoubleFrom(determination));
         }
     }
 
     @EventHandler
     public void vehicleDestroy(VehicleDestroyEvent event) {
 
         // <--[event]
         // @Events
         // vehicle destroyed
         // <vehicle> destroyed
         // entity destroys vehicle
         // <entity> destroys vehicle
         // entity destroys <vehicle>
         // <entity> destroys <vehicle>
         //
         // @Triggers when a vehicle is destroyed.
         // @Context
         // <context.vehicle> returns the dEntity of the vehicle.
         // <context.entity> returns the dEntity of the attacking entity.
         //
         // @Determine
         // "CANCELLED" to stop the entity from destroying the vehicle.
         //
         // -->
 
         Player player = null;
         dNPC npc = null;
 
         dEntity vehicle = new dEntity(event.getVehicle());
         String vehicleType = vehicle.getEntityType().name();
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         context.put("vehicle", vehicle);
 
         List<String> events = new ArrayList<String>();
         events.add("vehicle destroyed");
         events.add(vehicleType + " destroyed");
 
         if (event.getAttacker() != null) {
 
             dEntity entity = new dEntity(event.getAttacker());
             String entityType = entity.getEntityType().name();
             context.put("entity", entity.getDenizenObject());
 
             if (entity.isPlayer()) {
                 player = entity.getPlayer();
             }
             else {
                 npc = entity.getDenizenNPC();
                 entityType = "npc";
             }
 
             events.add("entity destroys vehicle");
             events.add("entity destroys " + vehicleType);
             events.add(entityType + " destroys vehicle");
             events.add(entityType + " destroys " + vehicleType);
         }
 
         String determination = doEvents(events, npc, player, context);
 
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setCancelled(true);
     }
 
     // <--[event]
     // @Events
     // entity enters vehicle
     // <entity> enters vehicle
     // entity enters <vehicle>
     // <entity> enters <vehicle>
     //
     // @Triggers when an entity enters a vehicle.
     // @Context
     // <context.vehicle> returns the dEntity of the vehicle.
     // <context.entity> returns the dEntity of the entering entity.
     //
     // @Determine
     // "CANCELLED" to stop the entity from entering the vehicle.
     //
     // -->
     @EventHandler
     public void vehicleEnter(VehicleEnterEvent event) {
 
         Player player = null;
         dNPC npc = null;
 
         Map<String, dObject> context = new HashMap<String, dObject>();
 
         dEntity vehicle = new dEntity(event.getVehicle());
         String vehicleType = vehicle.getEntityType().name();
 
         dEntity entity = new dEntity(event.getEntered());
         String entityType = entity.getEntityType().name();
 
         context.put("vehicle", vehicle);
         context.put("entity", entity.getDenizenObject());
 
         if (entity.isNPC()) { npc = entity.getDenizenNPC(); entityType = "npc"; }
         else if (entity.isPlayer()) player = entity.getPlayer();
 
         String determination = doEvents(Arrays.asList
                 ("entity enters vehicle",
                  entityType + " enters vehicle",
                  "entity enters " + vehicleType,
                  entityType + " enters " + vehicleType),
                  npc, player, context);
 
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setCancelled(true);
     }
 
     // <--[event]
     // @Events
     // entity exits vehicle
     // entity exits <vehicle>
     // <entity> exits vehicle
     // <entity> exits <vehicle>
     //
     // @Triggers when an entity exits a vehicle.
     // @Context
     // <context.vehicle> returns the dEntity of the vehicle.
     // <context.entity> returns the dEntity of the exiting entity.
     //
     // @Determine
     // "CANCELLED" to stop the entity from exiting the vehicle.
     //
     // -->
     @EventHandler
     public void vehicleExit(VehicleExitEvent event) {
 
         Player player = null;
         dNPC npc = null;
 
         Map<String, dObject> context = new HashMap<String, dObject>();
 
         dEntity vehicle = new dEntity(event.getVehicle());
         String vehicleType = vehicle.getEntityType().name();
 
         dEntity entity = new dEntity(event.getExited());
         String entityType = entity.getEntityType().name();
 
         context.put("vehicle", vehicle);
         context.put("entity", entity.getDenizenObject());
 
         if (entity.isNPC()) { npc = entity.getDenizenNPC(); entityType = "npc"; }
         else if (entity.isPlayer()) player = entity.getPlayer();
 
         String determination = doEvents(Arrays.asList
                 ("entity exits vehicle",
                  "entity exits " + vehicleType,
                  entityType + " exits vehicle",
                  entityType + " exits " + vehicleType),
                  npc, player, context);
 
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setCancelled(true);
     }
 
 
     /////////////////////
     //   WEATHER EVENTS
     /////////////////
 
     // <--[event]
     // @Events
     // lightning strikes (in <world>)
     //
     // @Triggers when lightning strikes in a world.
     // @Context
     // <context.world> returns the dWorld the lightning struck in.
     // <context.reason> returns the dLocation where the lightning struck.
     //
     // @Determine
     // "CANCELLED" to stop the lightning from striking.
     //
     // -->
     @EventHandler
     public void lightningStrike(LightningStrikeEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         dWorld world = new dWorld(event.getWorld());
         context.put("world", world);
         context.put("location", new dLocation(event.getLightning().getLocation()));
 
         String determination = doEvents(Arrays.asList
                 ("lightning strikes",
                  "lightning strikes in " + world.identify()),
                  null, null, context);
 
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setCancelled(true);
     }
 
     // <--[event]
     // @Events
     // weather changes/rains/clears (in <world>)
     //
     // @Triggers when weather changes in a world.
     // @Context
     // <context.world> returns the dWorld the weather changed in.
     // <context.weather> returns an Element with the name of the new weather.
     //
     // @Determine
     // "CANCELLED" to stop the weather from changing.
     //
     // -->
     @EventHandler
     public void weatherChange(WeatherChangeEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         dWorld world = new dWorld(event.getWorld());
         context.put("world", world);
 
         List<String> events = new ArrayList<String>();
         events.add("weather changes");
         events.add("weather changes in " + world.identify());
 
         if (event.toWeatherState()) {
             context.put("weather", new Element("rain"));
             events.add("weather rains");
             events.add("weather rains in " + world.identify());
         }
         else {
             context.put("weather", new Element("clear"));
             events.add("weather clears");
             events.add("weather clears in " + world.identify());
         }
 
         String determination = doEvents(events, null, null, context);
 
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setCancelled(true);
     }
 
 
     /////////////////////
     //   WORLD EVENTS
     /////////////////
 
     // <--[event]
     // @Events
     // portal created (in <world>) (because <reason>)
     //
     // @Triggers when a portal is created in a world.
     // @Context
     // <context.world> returns the dWorld the portal was created in.
     // <context.reason> returns an Element of the reason the portal was created.
     //
     // @Determine
     // "CANCELLED" to stop the portal from being created.
     //
     // -->
     @EventHandler
     public void portalCreate(PortalCreateEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         dWorld world = new dWorld(event.getWorld());
         String reason = event.getReason().name();
 
         context.put("world", world);
         context.put("reason", new Element(reason));
 
         String determination = doEvents(Arrays.asList
                 ("portal created",
                  "portal created because " + reason,
                  "portal created in " + world.identify(),
                  "portal created in " + world.identify() + " because " + reason),
                  null, null, context);
 
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setCancelled(true);
     }
 
     // <--[event]
     // @Events
     // spawn changes (in <world>)
     //
     // @Triggers when the world's spawn point changes.
     // @Context
     // <context.world> returns the dWorld that the spawn point changed in.
     // <context.old_location> returns the dLocation of the old spawn point.
     // <context.new_location> returns the dLocation of the new spawn point.
     //
     // -->
     @EventHandler
     public void spawnChange(SpawnChangeEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         dWorld world = new dWorld(event.getWorld());
 
         context.put("world", world);
         context.put("old_location", new dLocation(event.getPreviousLocation()));
         context.put("new_location", new dLocation(world.getWorld().getSpawnLocation()));
 
         doEvents(Arrays.asList
                 ("spawn changes",
                  "spawn changes in " + world.identify()),
                  null, null, context);
     }
 
     // <--[event]
     // @Events
     // structure grows (naturally/from bonemeal) (in <world>)
     // <structure> grows (naturally/from bonemeal) (in <world>)
     //
     // @Triggers when a structure (a tree or a mushroom) grows in a world.
     // @Context
     // <context.world> returns the dWorld the structure grew in.
     // <context.location> returns the dLocation the structure grew at.
     // <context.structure> returns an Element of the structure's type.
     //
     // @Determine
     // "CANCELLED" to stop the structure from growing.
     //
     // -->
     @EventHandler
     public void structureGrow(StructureGrowEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         dWorld world = new dWorld(event.getWorld());
         String treeType = event.getSpecies().name();
 
         context.put("world", world);
         context.put("location", new dLocation(event.getLocation()));
         context.put("structure", new Element(treeType));
 
         List<String> events = new ArrayList<String>();
         events.add("structure grows");
         events.add("structure grows in " + world.identify());
         events.add(treeType + " grows");
         events.add(treeType + " grows in " + world.identify());
 
         if (event.isFromBonemeal()) {
             events.add("structure grows from bonemeal");
             events.add("structure grows from bonemeal in " + world.identify());
             events.add(treeType + " grows from bonemeal");
             events.add(treeType + " grows from bonemeal in " + world.identify());
         }
         else {
             events.add("structure grows naturally");
             events.add("structure grows naturally in " + world.identify());
             events.add(treeType + " grows naturally");
             events.add(treeType + " grows naturally in " + world.identify());
         }
 
         String determination = doEvents(events, null, null, context);
 
         if (determination.toUpperCase().startsWith("CANCELLED"))
             event.setCancelled(true);
     }
 
     // <--[event]
     // @Events
     // world initializes
     // <world> initializes
     //
     // @Triggers when a world is initialized.
     // @Context
     // <context.world> returns the dWorld that was initialized.
     //
     // -->
     @EventHandler
     public void worldInit(WorldInitEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         dWorld world = new dWorld(event.getWorld());
 
         context.put("world", world);
 
         doEvents(Arrays.asList
                 ("world initializes",
                  world.identify() + " initializes"),
                  null, null, context);
     }
 
     // <--[event]
     // @Events
     // world loads
     // <world> loads
     //
     // @Triggers when a world is loaded.
     // @Context
     // <context.world> returns the dWorld that was loaded.
     //
     // -->
     @EventHandler
     public void worldLoad(WorldLoadEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         dWorld world = new dWorld(event.getWorld());
 
         context.put("world", world);
 
         doEvents(Arrays.asList
                 ("world loads",
                  world.identify() + " loads"),
                  null, null, context);
     }
 
     // <--[event]
     // @Events
     // world saves
     // <world> saves
     //
     // @Triggers when a world is saved.
     // @Context
     // <context.world> returns the dWorld that was saved.
     //
     // -->
     @EventHandler
     public void worldSave(WorldSaveEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         dWorld world = new dWorld(event.getWorld());
 
         context.put("world", world);
 
         doEvents(Arrays.asList
                 ("world saves",
                  world.identify() + " saves"),
                  null, null, context);
     }
 
     // <--[event]
     // @Events
     // world unloads
     // <world> unloads
     //
     // @Triggers when a world is unloaded.
     // @Context
     // <context.world> returns the dWorld that was unloaded.
     //
     // -->
     @EventHandler
     public void worldUnload(WorldUnloadEvent event) {
 
         Map<String, dObject> context = new HashMap<String, dObject>();
         dWorld world = new dWorld(event.getWorld());
 
         context.put("world", world);
 
         doEvents(Arrays.asList
                 ("world unloads",
                  world.identify() + " unloads"),
                  null, null, context);
     }
 }
