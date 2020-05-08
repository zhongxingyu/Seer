 package directi.androidteam.training.chatclient.Authentication;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.*;
 import directi.androidteam.training.chatclient.R;
 
 import java.util.ArrayList;
 
 /**
  * Created with IntelliJ IDEA.
  * User: vinayak
  * Date: 8/10/12
  * Time: 1:58 PM
  * To change this template use File | Settings | File Templates.
  */
 public class DisplayAccounts extends Activity implements Subscriber{
     ArrayList<String> loginList;
     ArrayList<String> logoutList;
     AccountListAdaptor adaptor;
     @Override
     public void onCreate(Bundle savedInstancestate){
         super.onCreate(savedInstancestate);
         setContentView(R.layout.accounts);
         ListView lv = (ListView)findViewById(R.id.accountScreen_list);
         adaptor = new AccountListAdaptor(this);
         AccountManager.getInstance().addSubscribers(this);
         lv.setAdapter(adaptor);
         setLoginList();
         setLogoutList();
 
     }
     public void setLoginList(){
         loginList = new ArrayList<String>();
         loginList.add("Login");
         loginList.add("Edit Password");
         loginList.add("Remove Account");
     }
     public void setLogoutList(){
         logoutList = new ArrayList<String>();
         logoutList.add("Logout");
         logoutList.add("Edit Password");
         logoutList.add("Remove Account");
     }
 
     public void  accountSettings(View view){
         //Spinner spinner = (Spinner)findViewById(R.id.accountScreen_spinner);
         Account account = AccountManager.getInstance().getAccount((String)view.getTag());
         /*ArrayAdapter<String> adapter = null;
         if(account.isLoginStatus()){
             adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,logoutList);
         }
         else
             adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,loginList);*/
         /*//spinner.setAdapter(adapter);
         //spinner.setVisibility(0);
         //spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
             @Override
             public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                 view.setVisibility(2);
                 Log.d("spinner",adapterView.getItemAtPosition(i).toString());
             }
 
             @Override
             public void onNothingSelected(AdapterView<?> adapterView) {
                 //To change body of implemented methods use File | Settings | File Templates.
             }
         });
 */
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setTitle("Settings");
         final ArrayList<String> temp;
         if(account.isLoginStatus().equals(LoginStatus.ONLINE)){
             temp = logoutList;
         }
         else
             temp = loginList;
         builder.setItems(temp.toArray(new CharSequence[temp.size()]),new DialogListener(temp,account,adaptor));
         AlertDialog dialog = builder.create();
         dialog.show();
 
     }
 
     public void goToaddAccount(View view){
         Intent intent = new Intent(this,LoginActivity.class);
         startActivity(intent);
     }
 
     @Override
     public void onResume(){
         super.onResume();
         Log.d("displayaccount","hey");
         adaptor.notifyDataSetChanged();
     }
 
     @Override
     public void onDestroy(){
         super.onDestroy();
         AccountManager.getInstance().saveAccountState();
         AccountManager.getInstance().removeSubscribers(this);
     }
 
     @Override
    public void receivedNotification(Publisher s) {
         runOnUiThread(new Runnable() {
             @Override
             public void run() {
                 adaptor.notifyDataSetChanged();
                 //To change body of implemented methods use File | Settings | File Templates.
             }
         });
 
     }
 }
