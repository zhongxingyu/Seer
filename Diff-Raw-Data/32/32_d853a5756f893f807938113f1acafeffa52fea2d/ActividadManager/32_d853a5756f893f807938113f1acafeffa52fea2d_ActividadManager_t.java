 package org.utn.proyecto.helpful.integrart.integrar_t_android;
 
 import org.utn.proyecto.helpful.integrart.integrar_t_android.activities.hablaconcali.LaunchHablaConCaliEvent;
 import org.utn.proyecto.helpful.integrart.integrar_t_android.activities.pictogramas.LaunchPictogramEvent;
 import org.utn.proyecto.helpful.integrart.integrar_t_android.activities.calendar.LaunchCalendarEvent;
import org.utn.proyecto.helpful.integrart.integrar_t_android.activities.cantaconcali.CantaConCaliActivity;
import org.utn.proyecto.helpful.integrart.integrar_t_android.activities.cantaconcali.LaunchCantaConCaliEvent;
 import org.utn.proyecto.helpful.integrart.integrar_t_android.activities.comosehace.LaunchComoSeHaceEvent;
 import org.utn.proyecto.helpful.integrart.integrar_t_android.activities.testactivity.LaunchTestActivityEvent;
 import org.utn.proyecto.helpful.integrart.integrar_t_android.events.Event;
 import org.utn.proyecto.helpful.integrart.integrar_t_android.events.EventBus;
 import org.utn.proyecto.helpful.integrart.integrar_t_android.events.EventListener;
 import org.utn.proyecto.helpful.integrart.integrar_t_android.events.LaunchMenuEvent;
 import org.utn.proyecto.helpful.integrart.integrar_t_android.events.ShowLoginEvent;
 import android.app.Activity;
 
 import android.content.Context;
 import android.content.Intent;
 
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 
 @Singleton
 public class ActividadManager implements EventListener<Class<Activity>>{
 	@Inject
 	public ActividadManager(EventBus bus){
 		bus.addEventListener(LaunchMenuEvent.class, this);
 		bus.addEventListener(ShowLoginEvent.class, this);
 		bus.addEventListener(LaunchPictogramEvent.class, this);
 		bus.addEventListener(LaunchComoSeHaceEvent.class, this);
 		bus.addEventListener(LaunchCalendarEvent.class, this);
 		bus.addEventListener(LaunchCantaConCaliEvent.class, new LaunchCantaConCaliListener());
 		//bus.addEventListener(LaunchTestActivityEvent.class, new LaunchTestListener());
 		bus.addEventListener(LaunchTestActivityEvent.class, this);
 	}
 	
     @Override
     public void onEvent(Event<Class<Activity>> event) {
         Context context = event.getContext();
         Intent intent = new Intent(context, event.getData());
         context.startActivity(intent);
     }
 	
 	private class LaunchCantaConCaliListener implements EventListener<Void>{
 
 		@Override
 		public void onEvent(Event<Void> event) {
 			Context context = event.getContext();
 			Intent intent = new Intent(context, CantaConCaliActivity.class);
 			context.startActivity(intent);
 		}
 		
 	}
 }
