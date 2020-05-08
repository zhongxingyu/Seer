 package com.iut.ptut;
 
 import java.sql.Date;
 
 import android.app.Activity;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Toast;
 
 import com.iut.ptut.view.ActivityNotification;
 import com.iut.ptut.view.TableJavaFragment;
 import com.iut.ptut.view.TableLayoutFragment;
 
 public class MainActivity extends Activity {
 
 	
 
 	// definition de l'id  correspondant  la notification 
 	public static final int ID_NOTIFICATION = 1337;
 
 
 	//permettra simplement de recuperer la date l'heure ....
 	long theDate;
 	Date actual = new Date(theDate);
 
 
 	/*
 	 * le code ci-dessous permet d'afficher une aute fenetre a partir d'un bouton, pour cela
 	 * 1) creer le bouton
 	 * 2)dans le code xml, rajouter la ligne android:onClick""
 	 * 3) mettre le nom de la methode entre les parentheses
 	 * 
 	 * /!\ pour afficher une fenetre c'est setContentView /!\
 	 * et vous pouvez rajouter du code aussi dans la methode
 	 */
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 	}
 
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		//possibilit d'ajouter des items au menu avec la mthode menu.add( String pS);
 		   
 		// Mthode java (NE PAS UTILISER). Le XML est beaucoup plus simple
 		// menu.add("testMenu");
 		// menu.add("testMenu2");
 
 		return true;
 	}
 	
 	/**
 	 * mthode appele si on clique sur une option du menu
 	 */
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// juste un test pour afficher une vue en cliquant
 		if(item.getItemId()== R.id.menu_settings)
 			setContentView(R.layout.informations);
 		else if (item.getItemId()==R.id.menu_param)
 			setContentView(R.layout.parametres);
 		//On regarde quel item a t cliqu grce  son id et on dclenche une action
 		return false;
 	}
 	
 	
 	//
 	// NOTIF
 	//
 
 
 	@SuppressWarnings("deprecation")
 	public void createNotif(){
 
 		/**
 		 * avant tout , il faut bien integerer ces notions, qui sont cruciales 
 		 * 1) pour les notifications
 		 * 2) mais surtout pour la prog. android en general :
 		 * http://developer.android.com/reference/android/content/Context.html
 		 * http://developer.android.com/reference/android/content/Intent.html
 		 * http://developer.android.com/reference/android/app/PendingIntent.html
 		 * 
 		 * 
 		 * voila maintenant de quoi comprendre comment marche une notification :
 		 * http://developer.android.com/reference/android/app/NotificationManager.html
 		 * http://developer.android.com/reference/android/app/Notification.html
 		 * http://developer.android.com/guide/topics/ui/notifiers/notifications.html
 		 */
 
 		
 		// creation d'un gestionnaire de notifications
 		final NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);        
 
 
 		// creation d'une notification
 		Notification notification = new Notification(R.drawable.notification,"notif",System.currentTimeMillis()); // les parametres sont obligatoires pour que cela fonctionne
 		Toast.makeText(getBaseContext(), "debug1 "+ notification.toString(), Toast.LENGTH_SHORT).show();
 		
 		String titreNotification = "en cours !";//definition du titre de la notif
 		String textNotification = "c'est l'heure";  //definition du texte qui caractrise la notif
 		
 		/*
 		 * definition d'une vibration lors de la notification
 		 * Ici les chiffres correspondent  0sec de pause, 0.2sec de vibration, 0.1sec de pause, 0.2sec de vibration, 0.1sec de pause, 0.2sec de vibration
 		 */
 		notification.vibrate = new long[] {0,200,100,200,100,200};
 		
 		
 		Toast.makeText(getBaseContext(), "debug2 "+ notification.toString(), Toast.LENGTH_SHORT).show();
 
 		/**
 		 * Les intents sont lune des pierres angulaires de la plateforme (ndlr. android).
 		 * Nous pouvons les comparer  des actions qui permettent de dialoguer  travers le systme
 		 *   partir de canaux qui leurs sont ddis.
 		 *  
 		 * exemple : Quand votre mobile reoit un appel, la plateforme lance un Intent signalant larrive de l'appel.
 		 */
 		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,new Intent(this,ActivityNotification.class), 0);
 
 
 
 		//On configure notre notification avec tous les paramtres que l'on vient de crer
 		notification.setLatestEventInfo(getApplicationContext(), titreNotification, textNotification, pendingIntent);
 
 		//Enfin on ajoute notre notification et son ID  notre gestionnaire de notification
 
 		notificationManager.notify(ID_NOTIFICATION, notification);
 	}
 
 
	public void displayToday(View v){
		setContentView(R.layout.activity_today2);
	}
 	public void displayInfos (View v){
 
 
 		try {
 			createNotif();
 			Toast.makeText(getBaseContext(), "createNotif execut ", Toast.LENGTH_SHORT).show();
 		}catch(NullPointerException e) {
 			// le Toast est un message "notification" qui permet d'afficher quelque chose a l'utilisateur sans changer de fentre
 			Toast.makeText(getBaseContext(), "sa marche pas la : " + e.getMessage(), Toast.LENGTH_SHORT).show();
 			System.out.println("sa marche pas la : " + e.getMessage());	
 		}
 
 
 		setContentView(R.layout.informations);
 
 	}
 
 	public void displayMain (View v){
 		setContentView(R.layout.activity_main);
 	}
 
 }
