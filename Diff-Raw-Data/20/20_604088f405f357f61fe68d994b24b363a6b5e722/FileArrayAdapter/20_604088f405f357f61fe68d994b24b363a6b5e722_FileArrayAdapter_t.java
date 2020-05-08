 package cryptocast.client.filechooser;
 
import java.util.List;
import android.content.Context;
import android.widget.ArrayAdapter;
import android.view.View;

 /**
  * An adapter between the ListElements and the view showing them.
  */
 public class FileArrayAdapter extends ArrayAdapter<ListElement> {
     /**
      * Constructs a new instance with the given attributes.
      * @param context The context in which this adapter is used.
      * @param textViewResourceId The view showing the data.
      * @param objects List with all elements which will be shown by the view.
      */
     public FileArrayAdapter(Context context, int textViewResourceId, List<ListElement> objects) {
     }
 
     /**
      * @return the list element at the given position in the list.
      * @param place the position of the item is in the list.
      */
     public ListElement getItem(int position) {
     }
 
     /**
      * @return a custom view of a list element at the given position in the list.
      * @param position Position of the element in the list.
      * @param convertView View which should be converted.
      * @param parent The parent view group.
      */
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
     }
 }
