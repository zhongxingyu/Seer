 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.crimsonrpg.personas.personas.npc;
 
 import com.crimsonrpg.personas.personas.PersonasPlugin;
 import com.crimsonrpg.personas.personasapi.npc.NPC;
 import com.crimsonrpg.personas.personasapi.npc.NPCManager;
 import com.crimsonrpg.personas.personasapi.npc.Trait;
 import com.crimsonrpg.personas.personasapi.persona.Persona;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import org.bukkit.Location;
 import org.martin.bukkit.npclib.NPCEntity;
 
 /**
  * The default NPC manager implementation.
  */
 public class SimpleNPCManager implements NPCManager {
     private Map<String, NPC> npcs = new HashMap<String, NPC>();
     private Map<String, Class<? extends Trait>> registeredTraits = new HashMap<String, Class<? extends Trait>>();
     private org.martin.bukkit.npclib.NPCManager handle;
     
     public SimpleNPCManager() {
         
     }
     
     public void load(PersonasPlugin plugin) {
         handle = new org.martin.bukkit.npclib.NPCManager(plugin);
         //TODO: this
     }
     
     public <T extends Trait> void registerTrait(String string, Class<T> type) {
         //Check if the trait is already registered
         if (registeredTraits.containsKey(string) || registeredTraits.containsValue(type)) {
             PersonasPlugin.LOGGER.warning("[Personas] The trait '" + type.getName() + "' was attempted to be registered under '" + string + "' but already exists; cancelling registration...");
             return;
         }
         
         registeredTraits.put(string, type);
     }
 
     public NPC spawnNPC(String name, Location location, List<Trait> traits, Persona persona) {
         //Create an ID
         StringBuilder idBuilder = new StringBuilder();
         idBuilder.append(name).append('-').append(persona.getName());
         
         //Append a different number to the string if the id is taken
         for (int i = 0; npcs.containsKey(idBuilder.toString()); i++) {
             idBuilder = (new StringBuilder()).append(idBuilder.toString()).append(i);
         }
         
         //Spawn it
         return spawnNPC(idBuilder.toString(), name, location, traits, persona);
     }
 
     public NPC spawnNPC(String id, String name, Location location, List<Trait> traits, Persona persona) {
         if (npcs.containsKey(id)) {
             PersonasPlugin.LOGGER.warning("[Personas] An NPC with the id '" + id + "' already exists; returning the existing NPC.");
             return npcs.get(id);
         }
         
         NPCEntity handleNPC = handle.spawnNPC(name, location, id);
         NPC theNpc = new SimpleNPC(id, name, traits, persona, handleNPC);
 
 //        String title = theNpc.getName() + "\n"
 //                + ChatColor.AQUA + "<" + theNpc.getType().name() + ">";
         
         //TODO: spout support?
         //SpoutManager.getAppearanceManager().setGlobalTitle((LivingEntity) theNpc.getHandle().getBukkitEntity(), title);
 
         npcs.put(id, theNpc);
         return theNpc;
     }
 
     public boolean despawnNPC(String id) {
        //Check if the NPC exists
        if (!npcs.containsKey(id)) return false;
        
        //Despawn
        handle.despawnById(id);
         npcs.remove(id);
         return true;
     }
 
     public void despawnNPC(NPC npc) {
        this.despawnNPC(npc.getId());
     }
 
     public NPC getNPC(String id) {
         return npcs.get(id);
     }
 
     public List<NPC> getNPCs() {
         return new ArrayList(npcs.values());
     }
     
 }
