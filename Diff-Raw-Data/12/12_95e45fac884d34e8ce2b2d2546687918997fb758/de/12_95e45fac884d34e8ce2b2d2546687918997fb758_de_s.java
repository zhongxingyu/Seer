 package iTheming;
 
 import de.itheming.app.R;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 
 public class de extends Activity {
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         }
         
     public void newposts1 (final View view) {
     	startActivity (new Intent (this, newposts.class));
     }
 
 	public void register (final View view) {
 	    	startActivity (new Intent (this, register.class));
 	}
 	
 	public void login (final View view) {
     	startActivity (new Intent (this,login.class));
 }
 
 	public void news (final View view) {
     	startActivity (new Intent (this,news.class));
 }
 	public void startseite (final View view) {
     	startActivity (new Intent (this,startseite.class));
 }
 	public void infos (final View view) {
     	startActivity (new Intent (this,infos.class));
 }
	public void update (final View view) {
    	startActivity (new Intent (this,update.class));
}
 	public void forum (final View view) {
     	startActivity (new Intent (this,forum.class));
 }
 	public void pn (final View view) {
     	startActivity (new Intent (this,pn.class));
 }
 }
 
