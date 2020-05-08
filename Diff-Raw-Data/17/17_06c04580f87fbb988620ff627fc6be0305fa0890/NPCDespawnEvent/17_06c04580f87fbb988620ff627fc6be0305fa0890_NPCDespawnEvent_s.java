 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.crimsonrpg.personas.personasapi.event.npc;
 
 import com.crimsonrpg.personas.personasapi.event.EventType;
 import com.crimsonrpg.personas.personasapi.npc.NPC;
import org.bukkit.Location;
 
 /**
  * Called when an NPC is despawned.
  */
 public class NPCDespawnEvent extends NPCEvent {
    private Location location;
 
    public NPCDespawnEvent(NPC npc, Location location) {
         super(EventType.NPC_DESPAWN, npc);
        this.location = location;
    }
    
    /**
     * Gets the location that the NPC was despawned at.
     * 
     * @return 
     */
    public Location getLocation() {
        return location;
     }
 }
