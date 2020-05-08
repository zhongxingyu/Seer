 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.crimsonrpg.personas.personas;
 
 import com.crimsonrpg.personas.personasapi.event.npc.NPCDestroyEvent;
 import com.crimsonrpg.personas.personasapi.event.npc.NPCSpawnEvent;
 import java.util.List;
 
 import org.bukkit.Location;
 import org.bukkit.entity.LivingEntity;
 
 import com.crimsonrpg.flaggables.api.Flag;
 import com.crimsonrpg.flaggables.api.GenericFlaggableManager;
 import com.crimsonrpg.personas.personasapi.event.npc.NPCCreateEvent;
 import com.crimsonrpg.personas.personasapi.event.npc.NPCDespawnEvent;
 import com.crimsonrpg.personas.personasapi.npc.NPC;
 import com.crimsonrpg.personas.personasapi.npc.NPCManager;
 import com.crimsonrpg.personas.personasapi.persona.GenericPersona;
 import com.crimsonrpg.personas.personasapi.persona.Persona;
 import java.util.HashMap;
 import java.util.Map;
 import org.martin.bukkit.npclib.NPCEntity;
 
 /**
  * The default NPC manager implementation.
  */
 public class SimpleNPCManager extends GenericFlaggableManager<NPC> implements NPCManager {
 
     private Map<LivingEntity, NPC> bukkitMappings = new HashMap<LivingEntity, NPC>();
     private org.martin.bukkit.npclib.NPCManager handle;
 
     SimpleNPCManager() {
     }
 
     void load(PersonasPlugin plugin) {
         handle = new org.martin.bukkit.npclib.NPCManager(plugin);
     }
 
     public NPC create(String id) {
         return new SimpleHumanNPC(id);
     }
 
     public NPC createNPC(String name, List<Flag> flags, Persona persona) {
        persona = (persona == null ? new GenericPersona("null") : persona);
        
         //Create an ID
         StringBuilder idBuilder = new StringBuilder();
         idBuilder.append(name).append('-').append(persona.getName());
 
         //Append a different number to the string if the id is taken
         for (int i = 0; idExists(idBuilder.toString()); i++) {
             idBuilder = (new StringBuilder()).append(idBuilder.toString()).append(i);
         }
 
         //Spawn it
         return createNPC(idBuilder.toString(), name, flags, persona);
     }
 
     public NPC createNPC(String id, String name, List<Flag> flags, Persona persona) {
         if (idExists(id)) {
             PersonasPlugin.LOGGER.warning("[Personas] An NPC with the id '" + id + "' already exists; returning the existing NPC.");
             return get(id);
         }
 
         NPC npc = create(id);
         npc.setName(name).setPersona((persona == null ? new GenericPersona("null") : persona)).addFlags(flags);
 
         //Call the event
         NPCCreateEvent event = PersonasEventFactory.callNPCCreateEvent(npc);
         //TODO: make this not cancellable
 
         //TODO: spout support?
         //SpoutManager.getAppearanceManager().setGlobalTitle((LivingEntity) theNpc.getHandle().getBukkitEntity(), title);
 
         npc.getPersona().onNPCCreate(event);
         return npc;
     }
 
     @Override
     public NPC destroy(String id) {
         return destroy(get(id));
     }
 
     @Override
     public NPC destroy(NPC npc) {
         //Call the event
         NPCDestroyEvent event = PersonasEventFactory.callNPCDestroyEvent(npc);
         if (event.isCancelled()) {
             return null;
         }
 
         npc.getPersona().onNPCDestroy(event);
         despawnNPC(npc);
         return super.destroy(npc.getId());
     }
 
     public void spawnNPC(String id, Location location) {
         spawnNPC(get(id), location);
     }
 
     public void spawnNPC(NPC npc, Location location) {
         //Call the event
         NPCSpawnEvent event = PersonasEventFactory.callNPCSpawnEvent(npc, location);
         if (event.isCancelled()) {
             return;
         }
 
         npc.getPersona().onNPCSpawn(event);
         NPCEntity handel = handle.spawnNPC(npc.getName(true), event.getLocation(), npc.getId());
         ((SimpleHumanNPC) npc).setHandle(handel); //As in the composer
         bukkitMappings.put((LivingEntity) handel.getBukkitEntity(), npc);
     }
 
     public void despawnNPC(String id) {
         despawnNPC(get(id));
     }
 
     public void despawnNPC(NPC npc) {
         //Call the event
         NPCDespawnEvent event = PersonasEventFactory.callNPCDespawnEvent(npc);
         if (event.isCancelled()) {
             return;
         }
 
         npc.getPersona().onNPCDespawn(event);
         handle.despawnById(npc.getId());
     }
 
     public NPC fromBukkitEntity(LivingEntity le) {
         return bukkitMappings.get(le);
     }
 }
