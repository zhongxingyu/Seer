 package source.code;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.CheckedTextView;
 
 @SuppressWarnings("rawtypes")
 public class FriendListAdapter<T> extends ArrayAdapter {
     
     private ArrayList<Boolean> isChecked;
     private ArrayList<Friend> friends;
     private int numChecked;
     @SuppressWarnings("unchecked")
     public FriendListAdapter(Context context, int resource, ArrayList<Friend> friends)
     {
         super(context, resource, friends);
         this.friends = friends;
         this.numChecked = 0;
         this.isChecked = new ArrayList<Boolean>(friends.size());
         for (int i = 0; i < friends.size(); i++) {
             isChecked.add(i, false);
         }
     }
     
     public View getView(int position, View convertView, ViewGroup parent) {
         CheckedTextView v = (CheckedTextView)super.getView(position, convertView, parent);
         v.setChecked(isChecked.get(position));
         
         return v;
     }
     
     public void setChecked(int position, boolean checked) {
        isChecked.set(position, checked);
        System.out.println(isChecked);
         if (checked == true) {
             this.numChecked++;
         }
         else {
             this.numChecked--;
         }
     }
     
     public ArrayList<Boolean> getIsChecked()
     {
     	return isChecked;
     }
     
     public ArrayList<Friend> getSelectedFriends() {
         ArrayList<Friend> selectedFriends = new ArrayList<Friend>();
         for (int i = 0; i < this.friends.size(); i++) {
             if (this.isChecked.get(i) == true) {
                 selectedFriends.add(this.friends.get(i));
                 System.out.println("i = " + i);
                 System.out.println(friends.get(i).toString());
             }
         }
         return selectedFriends;
     }
     
     public int getNumChecked() {
         return this.numChecked;
     }
 }
