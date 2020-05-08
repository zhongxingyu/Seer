 package com.herocraftonline.dev.heroes.party;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.CustomEventListener;
 import org.bukkit.event.Event;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.api.ExperienceGainEvent;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 
 public class PartyCustomListener extends CustomEventListener{
     Heroes plugin;
 
     public PartyCustomListener(Heroes plugin) {
         this.plugin = plugin;
     }
 
     public void onCustomEvent(Event event) {
         if(event instanceof ExperienceGainEvent) {
             ExperienceGainEvent subEvent = (ExperienceGainEvent) event;
             if(subEvent.getHero().getParty() == null) {
                 return;
             }
             
             if(!subEvent.getHero().getParty().getExp()) {
                 return;
             }
 
             if(subEvent.getHero().getParty().getMembers().size() > 0) {
                 return;
             }
             
             Hero hero = subEvent.getHero();
             Integer expGain = Math.round(subEvent.getExpGain() / hero.getParty().getMembers().size());
 
             for(Player p : hero.getParty().getMembers()) {
                 if(p != subEvent.getHero().getPlayer()) {
                     Hero pHero = plugin.getHeroManager().getHero(p);
                     pHero.quietExpGain(expGain, subEvent.getSource());
                 }
             }
            subEvent.setExpGain(expGain);
         }
     }
     
 
 }
