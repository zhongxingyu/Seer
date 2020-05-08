 package iTheming;
 
 import de.itheming.app.R;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 
 public class infos extends Activity {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.infos);
     }
 	public void spenden (final View view) {
     	startActivity (new Intent (this,spenden.class));
 }
 	public void register (final View view) {
     	startActivity (new Intent (this, register.class));
 }
 	public void login (final View view) {
 		startActivity (new Intent (this,login.class));
 }
 	public void changelog (final View view) {
 		startActivity (new Intent (this,changelog.class));
 }
	public void update (final View view) {
    	startActivity (new Intent (this,update.class));
}	public void social (final View view) {
 	startActivity (new Intent (this,social.class));
 }
 }
