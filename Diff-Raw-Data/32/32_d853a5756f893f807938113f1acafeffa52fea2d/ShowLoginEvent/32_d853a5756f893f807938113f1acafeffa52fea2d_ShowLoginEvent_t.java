 package org.utn.proyecto.helpful.integrart.integrar_t_android.events;
 
import org.utn.proyecto.helpful.integrart.integrar_t_android.login.LoginActivity;

 import android.content.Context;
 
public class ShowLoginEvent extends LaunchActivityEvent {
 
 	public ShowLoginEvent(Context context) {
		super(context, LoginActivity.class);
 	}
 
 }
