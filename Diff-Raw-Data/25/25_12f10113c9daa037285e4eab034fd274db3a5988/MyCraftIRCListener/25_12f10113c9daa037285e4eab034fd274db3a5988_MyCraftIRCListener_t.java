 package org.Bukkitdev.Xon;
 
 import org.bukkit.event.Event;
 import org.bukkit.event.Listener;
 import org.bukkit.event.CustomEventListener;
 
 
import com.alecgorge.minecraft.jsonapi.api.*;
import com.alecgorge.minecraft.jsonapi.JSONAPI;
 import com.herocraftonline.squallseed31.heroicdeath.HeroicDeathEvent;
 //
 
 public class MyCraftIRCListener extends CustomEventListener implements Listener {
     private HeroicDeathToJSONAPI HeroicDeathToJSONAPI;
     private JSONAPIStream stream = new DeathStream();
 
     public MyCraftIRCListener(HeroicDeathToJSONAPI HeroicDeathToJSONAPI) {
         this.HeroicDeathToJSONAPI = HeroicDeathToJSONAPI;
         
         JSONAPI JSONAPI = HeroicDeathToJSONAPI.getJSONAPI();
         JSONAPI.getStreamManager().registerStream(HeroicDeathToJSONAPI.getStreamName(), stream);
     }
 
     public void onCustomEvent(Event event) 
     {
         if (!(event instanceof HeroicDeathEvent))
             return;    	
         else 
         {
         	HeroicDeathEvent heroicDeathEvent = (HeroicDeathEvent) event;
         	stream.addMessage(new DeathMessage(HeroicDeathToJSONAPI.getStreamName(),heroicDeathEvent.getDeathCertificate()));
         }      
     }
 	
 		
 }
