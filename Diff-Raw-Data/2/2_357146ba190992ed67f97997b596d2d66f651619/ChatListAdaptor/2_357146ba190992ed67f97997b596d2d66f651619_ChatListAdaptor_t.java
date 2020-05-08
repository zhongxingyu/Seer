 package directi.androidteam.training.chatclient.Chat;
 
 import android.app.Activity;
 import android.content.Context;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 import directi.androidteam.training.chatclient.R;
 
 import java.util.Vector;
 
 /**
  * Created with IntelliJ IDEA.
  * User: vinayak
  * Date: 13/9/12
  * Time: 2:51 PM
  * To change this template use File | Settings | File Templates.
  */
 public class ChatListAdaptor extends ArrayAdapter<ChatListItem> {
     Context context;
     public ChatListAdaptor(Context context, Vector<ChatListItem> objects) {
         super(context, R.layout.chatlistitem, objects);
         this.context=context;
 
     }
 
    @Override
     public View getView(int position,View view,ViewGroup viewg){
        Log.d("isSenderView","hey "+(new Integer(position)).toString());
        View row = view;
        if(row==null) {
            LayoutInflater vi = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = vi.inflate(R.layout.chatlistitem,null);
        }
        ChatListItem cli = getItem(position);
        getItem(position);
        if(cli!=null) {
            int layoutResourceId = cli.getResourceID();
            Log.d("isSenderCli",cli.isSender()+""+cli.getMessage()+" "+(new Integer(position)).toString());
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, viewg, false);
            ChatListHolder holder = new ChatListHolder();
            if(cli.isSender()){
                holder.status = (TextView)row.findViewById(R.id.chatlistitem_status);
                holder.username = (TextView)row.findViewById(R.id.send_mess_name);
                holder.message = (TextView)row.findViewById(R.id.send_mess_body);
                holder.time = (TextView)row.findViewById(R.id.chatlistitem_time);
            }
            else{
                holder.username = (TextView)row.findViewById(R.id.receive_mess_name);
                holder.message = (TextView)row.findViewById(R.id.receive_mess_body);
                holder.time = (TextView)row.findViewById(R.id.chatlistitemR_time);
            }
            row.setTag(holder);
            if(!cli.isSender()){
                holder.username.setText(cli.getUsername().split("@")[0]);
            }
 
            holder.message.setText(cli.getMessage());
            holder.time.setText(cli.getTime());
 
           if(!cli.isStatus() && holder.status!=null)
                holder.status.setVisibility(0);
        }
         return row;
    }
    static class ChatListHolder{
        TextView username;
        TextView message;
        TextView time;
        TextView status;
    }
 }
