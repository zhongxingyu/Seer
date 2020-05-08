 package com.herocraftonline.dev.heroes.api;
 
 import org.bukkit.event.Event;
 
 import com.herocraftonline.dev.heroes.persistence.Hero;
 
 @SuppressWarnings("serial")
 public class HeroChangeLevelEvent extends Event {
 
    private final int from;
    private final int to;
    private final Hero hero;
 
     public HeroChangeLevelEvent(Hero hero, int from, int to) {
         super("HeroLevelEvent");
         this.from = from;
         this.to = to;
        this.hero = hero;
     }
 
     public final int getFrom() {
         return from;
     }
 
     public final int getTo() {
         return to;
     }
 
    /**
     * @return the hero
     */
    public Hero getHero() {
        return hero;
    }

 }
