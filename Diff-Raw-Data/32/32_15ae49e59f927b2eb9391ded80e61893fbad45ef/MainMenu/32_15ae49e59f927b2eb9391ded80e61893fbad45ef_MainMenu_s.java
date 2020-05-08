 package edu.nku.cs.csc440.team2.UIMenus;
 
 import edu.nku.cs.csc440.team2.SMILCloud;
 import edu.nku.cs.csc440.team2.composer.Composer;
 import edu.nku.cs.csc440.team2.inbox.CreatedMessages;
 import edu.nku.cs.csc440.team2.inbox.Inbox;
 import edu.nku.cs.csc440.team2.service.SmilService;
 import edu.nku.cs.csc440.team2.uploader.Uploader;
 import edu.nku.cs.csc460.team2.R;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 
 /**
  * Display the main menu for the application (post-authentication)
  * 
  * @author John Murray
  * @version 1.0 4/24/11
  * 
  */
 public class MainMenu extends Activity {
 
 	/**
 	 * Define and display the interface that launches all of the activites in
 	 * the application (inbox, created messages, compose, edit, watch, etc.) and
 	 * offer them the abiliity to logout of their current user.
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main_menu_screen);
 
 		startService(new Intent(MainMenu.this, SmilService.class));
 
 		Button inbox = (Button) findViewById(R.id.main_menu_inbox);
 		Button createdMessage = (Button) findViewById(R.id.main_menu_created_messages);
 		Button composeNew = (Button) findViewById(R.id.main_menu_compose_messages);
 		Button uploadMedia = (Button) findViewById(R.id.main_menu_upload_media);
 		Button logout = (Button) findViewById(R.id.main_menu_logout);
 		Button exit = (Button) findViewById(R.id.main_menu_exit);
 
 		inbox.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				MainMenu.this.startActivity(new Intent(MainMenu.this,
 						Inbox.class));
 			}
 		});
 
 		createdMessage.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				MainMenu.this.startActivity(new Intent(MainMenu.this,
 						CreatedMessages.class));
 			}
 		});
 
 		composeNew.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				((SMILCloud) MainMenu.this.getApplication())
 						.queueDocumentToEdit(null);
 				MainMenu.this.startActivity(new Intent(MainMenu.this,
 						Composer.class));
 			}
 		});
 
 		uploadMedia.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Intent i = new Intent(MainMenu.this, Uploader.class);
 				MainMenu.this.startActivity(i);
 			}
 		});
 
 		logout.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				((SMILCloud) MainMenu.this.getApplication())
 						.setUserId(SMILCloud.NO_USER);
 				Intent i = new Intent(MainMenu.this, WelcomeScreen.class);
 				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 				MainMenu.this.startActivity(i);
 			}
 		});
 
 		exit.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				MainMenu.this.finish();
 			}
 		});
 
 	}
 
 }
