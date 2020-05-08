 package il.ac.huji.chores;
 
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageButton;
 import android.widget.TextView;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Collections2;
 import com.parse.ParseUser;
 
 import javax.annotation.Nullable;
 import java.text.DateFormat;
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 
 
 public class MyChoresListAdapter extends ArrayAdapter<Chore> {
 
     public MyChoresListAdapter(Context context, List<Chore> chores) {
        super(context, R.layout.my_chores_list_row, chores);
         Log.d("MyChoresListAdapter", "Created with " + chores.size() + " chores");
     }
 
     @Override
     public void addAll(Collection<? extends Chore> collection) {
         if (collection == null)
             return;
         Collection<? extends Chore> filtered = Collections2.filter(collection, new Predicate<Chore>() {
             @Override
             public boolean apply(@Nullable Chore chore) {
                 if (chore.getStatus() == Chore.CHORE_STATUS.STATUS_DONE)
                     return false;
                 return true;
             }
         });
         super.addAll(filtered);    //To change body of overridden methods use File | Settings | File Templates.
     }
 
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
         final Date current = new Date();
         final Chore chore = getItem(position);
         Log.d("MyChoresListAdapter", "chore " + chore);
         LayoutInflater inflater = LayoutInflater.from(getContext());
         View view;
         switch (chore.getStatus()) {
             case STATUS_FUTURE:
                 view = inflater.inflate(R.layout.my_chores_list_row_future, null);
                 break;
             case STATUS_MISS:
                 view = inflater.inflate(R.layout.my_chores_list_row_miss, null);
                 break;
             default:
                 view = inflater.inflate(R.layout.my_chores_list_row_future, null);
                 Log.w("MyChoresListAdapter", "Chore status was not FUTURE or MISS but " + chore.getStatus());
         }
         TextView choreTitle = (TextView) view.findViewById(R.id.myChoresRowTitle);
         TextView choreDueDate = (TextView) view.findViewById(R.id.myChoresRowDueDate);
 
         choreTitle.setText(chore.getName());
         String choreDueString = DateFormat.getDateInstance(DateFormat.SHORT).format(chore.getDeadline());
         choreDueDate.setText(choreDueString);
 //        ImageButton editButton = (ImageButton) view.findViewById(R.id.myChoresRowEditButton);
 //        editButton.setOnClickListener(new OnClickListener() {
 //            @Override
 //            public void onClick(View v) {
 //                Context context = v.getContext();
 //                Intent intent = new Intent(context, ChoreCardActivity.class);
 //                intent.putExtra(context.getResources().getString(R.string.card_activity_extra1_name), chore);
 //                intent.putExtra(context.getResources().getString(R.string.card_activity_extra2_name), getCurUsername(context));
 //                context.startActivity(intent);
 //            }
 //        });
         return view;
     }
 
     public String getCurUsername(Context context){
       	 ParseUser user = ParseUser.getCurrentUser();
    		 if(user == null){
       		//user isn't logged in
    			LoginActivity.OpenLoginScreen(context, false);
    			user = ParseUser.getCurrentUser();
       	}
    		return user.getUsername();
     }
 }
