 package ualberta.g12.adventurecreator.views;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Color;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import ualberta.g12.adventurecreator.R;
 import ualberta.g12.adventurecreator.data.Choice;
 import ualberta.g12.adventurecreator.data.Fragment;
 import ualberta.g12.adventurecreator.data.FragmentPart;
 
 /**
  * Adapter for communication between the fragment class and the segments within
  * the fragment. Makes views depending on what is selected and orders the
  * segments depending on what type they are.
  */
public class FragmentPartAdapter extends ArrayAdapter {
     private final Context context;
     public final int resource;
     private final Fragment frag;
 
     private static final String TAG = "FragmentPartAdapter";
     private static final boolean DEBUG = true;
 
     /**
      * Sold constructor for the FragmentPartAdapter. Sets up the context,
      * resource and fragment.
      * 
      * @param context the context of the calling activity
      * @param resource the resource to load
      * @param frag the Fragment to display
      */
     public FragmentPartAdapter(Context context, int resource, Fragment frag) {
         super(context, resource, frag.getParts());
         this.context = context;
         this.resource = resource;
         this.frag = frag;
     }
 
     @Override
     /**
      * displays the corresponding view depending what was selected by the user
      * 
      * @param position  position of the segment that has been selected
      * @param convertView   view type that must be made
      * @param parent    fragment that the segment belongs to
      */
     public View getView(int position, View convertView, ViewGroup parent) {
         View rowView = convertView;
         if (rowView == null) {
             LayoutInflater inflater = (LayoutInflater) context
                     .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
             rowView = inflater.inflate(R.layout.listview_fragment_part_list, parent, false);
         }
         ImageView image = (ImageView) rowView.findViewById(R.id.fragmentPartIllustration);
         TextView text = (TextView) rowView.findViewById(R.id.fragmentPartTextPart);
         TextView choiceButton = (TextView) rowView.findViewById(R.id.fragmentPartChoice);
 
         // make all invisible
         image.setVisibility(View.GONE);
         text.setVisibility(View.GONE);
         choiceButton.setVisibility(View.GONE);
 
         if (DEBUG)
             Log.d(TAG, "POSITION " + position);
         FragmentPart part = frag.getParts().get(position);
         String type = part.getType();
         
         if ( type.equals("t") ) {
             if (DEBUG)
                 Log.d(TAG, "TEXT " + position);
             // Display a text segment
             String textSegment = part.getData();
             if (textSegment != null) {
                 text.setVisibility(View.VISIBLE);
                 if (text != null)
                     if (!textSegment.equals(""))
                         text.setText(textSegment);
                     else 
                         text.setText("NewText");
             }
         } else if ( type.equals("i") ) {
             if (DEBUG)
                 Log.d(TAG, "IMAGE " + position);
             // Display an illustration
 
             String picturePath = part.getData();
             Bitmap illustration = BitmapFactory.decodeFile(picturePath);
 
             if (illustration != null) {
                 image.setVisibility(View.VISIBLE);
                 if (image != null)
                     if (DEBUG)
                         Log.d(TAG, "SET IMAGE");
                 image.setImageBitmap(illustration);
             }
         } else if ( type.equals("c") ) {
             if (DEBUG)
                 Log.d(TAG, "CHOICE " + position);
             // Display a choice
 
             Choice choice = part.getChoice();
             if (choice != null) {
                 choiceButton.setVisibility(View.VISIBLE);
                 if (choiceButton != null)
                     if (DEBUG)
                         Log.d(TAG, "SET CHOICE");
                 choiceButton.setText(choice.getChoiceText());
                 choiceButton.setBackgroundColor(Color.BLACK);
                 choiceButton.setTextColor(Color.WHITE);
             }
         } else if ( type.equals("e") ) {
             if (DEBUG)
                 Log.d(TAG, "EMPTY " + position);
             // Display a DefaultPart
 
             String textSegment = "Add new text/image/choice\n(This section will be removed when done editing)";
             if (textSegment != null) {
                 text.setVisibility(View.VISIBLE);
                 if (text != null)
                     text.setText(textSegment);
             }
         }
 
         return rowView;
     }
 }
