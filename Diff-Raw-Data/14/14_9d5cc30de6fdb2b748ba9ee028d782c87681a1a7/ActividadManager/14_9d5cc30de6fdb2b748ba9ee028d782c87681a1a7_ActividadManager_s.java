 package org.utn.proyecto.helpful.integrart.integrar_t_android;
 
 import org.utn.proyecto.helpful.integrart.integrar_t_android.activities.pictogramas.LaunchPictogramEvent;
 import org.utn.proyecto.helpful.integrart.integrar_t_android.activities.pictogramas.PictogramActivity;
 import org.utn.proyecto.helpful.integrart.integrar_t_android.activities.testactivity.LaunchTestActivityEvent;
 import org.utn.proyecto.helpful.integrart.integrar_t_android.activities.testactivity.TestActivity;
 
 import org.utn.proyecto.helpful.integrart.integrar_t_android.events.Event;
 import org.utn.proyecto.helpful.integrart.integrar_t_android.events.EventBus;
 import org.utn.proyecto.helpful.integrart.integrar_t_android.events.EventListener;
 import org.utn.proyecto.helpful.integrart.integrar_t_android.events.LaunchMenuEvent;
 import org.utn.proyecto.helpful.integrart.integrar_t_android.events.ShowLoginEvent;
 import org.utn.proyecto.helpful.integrart.integrar_t_android.login.LoginActivity;
 
 import org.utn.proyecto.helpful.integrart.integrar_t_android.menu.ItemListActivity;
 import android.content.Context;
 import android.content.Intent;
 
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 
 @Singleton
 public class ActividadManager{
 	@Inject
 	public ActividadManager(EventBus bus){
 		bus.addEventListener(LaunchMenuEvent.class, new LaunchMenuListener());
 		bus.addEventListener(ShowLoginEvent.class, new LaunchLoginListener());
 		bus.addEventListener(LaunchPictogramEvent.class, new LaunchPictogramListener());
		bus.addEventListener(LaunchTestActivityEvent.class, new LaunchTestListener());
 		//bus.addEventListener(LaunchTestActivityEvent.class, new LaunchTestListener());
 	}
 	
 	private class LaunchMenuListener implements EventListener<Void>{
 		@Override
 		public void onEvent(Event<Void> event) {
 			Context context = event.getContext();
 			Intent intent = new Intent(context, ItemListActivity.class);
 			context.startActivity(intent);
 		}
 	}
 	
 	private class LaunchLoginListener implements EventListener<Void>{
 		@Override
 		public void onEvent(Event<Void> event) {
 			Context context = event.getContext();
 			Intent intent = new Intent(context, LoginActivity.class);
 			context.startActivity(intent);
 		}
 	}
 	
 	/*private class LaunchTestListener implements EventListener<Void>{
 		@Override
 		public void onEvent(Event<Void> event) {
 			Context context = event.getContext();
 			Intent intent = new Intent(context, TestActivity.class);
 			context.startActivity(intent);
 		}
 	}
	
 	private class LaunchPictogramListener implements EventListener<Void>{
 
 		@Override
 		public void onEvent(Event<Void> event) {
 			Context context = event.getContext();
 			Intent intent = new Intent(context, PictogramActivity.class);
 			context.startActivity(intent);
 		}
 		
 	}
	*/
 }
