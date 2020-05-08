 package directi.androidteam.training.chatclient.Authentication;
 
 import android.app.Activity;
 import android.content.Context;
 import android.text.AndroidCharacter;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 import directi.androidteam.training.chatclient.R;
 
 /**
  * Created with IntelliJ IDEA.
  * User: vinayak
  * Date: 8/10/12
  * Time: 2:09 PM
  * To change this template use File | Settings | File Templates.
  */
 public class AccountListAdaptor extends BaseAdapter {
     public static int resourceId = R.layout.accountitem;
     Context context;
     public AccountListAdaptor(Context context){
         super();
         this.context = context;
 
     }
 
     public Context getContext(){
         return this.context;
     }
 
     @Override
     public int getCount() {
        return AccountManager.getInstance().getnumAccount();
     }
 
     @Override
     public Account getItem(int i) {
         return AccountManager.getInstance().getAccountfromList(i);
     }
 
     @Override
     public long getItemId(int i) {
         return i;
     }
 
     @Override
     public boolean isEnabled(int position) {
         return true;
     }
 
     @Override
     public int getViewTypeCount() {
         return 1;
     }
 
     @Override
     public View getView(int position,View view,ViewGroup viewg){
         View row = view;
 
         AccountListHolder holder = null;
         Account cli = getItem(position);
 
 
         /* if(row == null)
         {*/
        // Log.d("isSenderCli", cli.isSender() + "" + cli.getMessage() + " " + (new Integer(position)).toString());
         LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();
         row = inflater.inflate(resourceId, viewg, false);
 
         holder = new AccountListHolder();
 
 
         holder.username = (TextView)row.findViewById(R.id.accountitem_jid);
         holder.icon = (ImageView)row.findViewById(R.id.accountitem_icon);
         holder.status = (TextView)row.findViewById(R.id.accountitem_status);
         holder.presence = (ImageView)row.findViewById(R.id.accountitem_presence);
 
         row.setTag(holder);
 
         row.findViewById(R.id.accountitem_group).setTag(cli.getAccountUid());
         /*}
         else
         {
             Log.d("isSenderView","View "+cli.getMessage());
 
             holder = (ChatListHolder)row.getTag();
         }*/
 
         holder.username.setText(cli.accountUid);
         //holder.settings.setImageResource(R.drawable.settings);
         //holder.settings.setTag(cli.getAccountUid());
         String show = cli.getShow();
        if(show!=null){
         if(show.equals("dnd"))
             holder.presence.setImageResource(R.color.Red);
         else if(show.equals("available"))
             holder.presence.setImageResource(R.color.Green);
         else if(show.equals("away"))
             holder.presence.setImageResource(R.color.Yellow);
         }
         if(cli.isLoginStatus().equals(LoginStatus.ONLINE)){
             if(cli.getStatus()==null || cli.getStatus().equals(""))
                 holder.status.setText("online");
             else
                 holder.status.setText(cli.getStatus());
 
             holder.status.setTextColor(R.color.Black);
         }
         else if(cli.isLoginStatus().equals(LoginStatus.OFFLINE)){
             holder.status.setText("offline");
             holder.status.setTextColor(R.color.Gray);
            holder.presence.setImageResource(R.color.Gray);
         }
         else if(cli.isLoginStatus().equals(LoginStatus.CONNECTING)){
             holder.status.setText("connecting");
             holder.status.setTextColor(R.color.Gray);
         }
         else if(cli.isLoginStatus().equals(LoginStatus.INVALIDCREDENTIALS)){
             holder.status.setText("wrong password");
             holder.status.setTextColor(R.color.Red);
         }
         holder.icon.setImageResource(cli.getServiceIcon());
 
 
         return row;
     }
     static class AccountListHolder{
         TextView username;
         ImageView icon;
         TextView status;
         ImageView presence;
     }
 }
