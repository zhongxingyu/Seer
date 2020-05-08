 package com.herocraftonline.dev.heroes.api;
 
 import org.bukkit.event.Event;
 
 import com.herocraftonline.dev.heroes.persistence.Hero;
 
 @SuppressWarnings("serial")
 public class HeroChangeLevelEvent extends Event {
 
    protected final int from;
    protected final int to;
 
     public HeroChangeLevelEvent(Hero hero, int from, int to) {
         super("HeroLevelEvent");
         this.from = from;
         this.to = to;
     }
 
     public final int getFrom() {
         return from;
     }
 
     public final int getTo() {
         return to;
     }
 
 }
