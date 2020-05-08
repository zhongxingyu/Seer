 package es.ucm.jorngeren13;
 
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.widget.SimpleCursorAdapter;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.foound.widget.AmazingListView;
 
 import es.ucm.jorngeren13.util.AmazingSimpleCursorAdapter;
 
 public final class AgendaFragment extends JG13Fragment {
 
     /** List containing the agenda */
     private AmazingListView list;
 
     @Override
     public View onCreateView (final LayoutInflater inflater, final ViewGroup container, final Bundle saved) {
         final View rootView = inflater.inflate(R.layout.fragment_agenda, container, false);
         list = (AmazingListView) rootView.findViewById(R.id.list);
 
         String[] cursorFields = new String[] { "start_time", "title", "description" };
         int[] viewFields = new int[] { R.id.start_time, R.id.title, R.id.description };
 
         AmazingSimpleCursorAdapter adapter =
             new AmazingSimpleCursorAdapter(
                 rootView.getContext(), R.layout.listitem_agenda, JG13Data.getAgendaCursor(), "day", cursorFields,
                 viewFields, 0);
         adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
 
             @Override
             public boolean setViewValue (View view, Cursor cursor, int column) {
                 if (cursor.getString(column) == null) {
                     view.setVisibility(View.GONE);
                     return true;
                    
                 }
                
                view.setVisibility(View.VISIBLE);
                 return false;
             }
         });
 
         list.setPinnedHeaderView(getLayoutInflater(saved).inflate(R.layout.listitem_agenda_header, list, false));
         list.setAdapter(adapter);
         
         // Required for the menu
         setHasOptionsMenu(true);
 
         return rootView;
     }
 
     @Override
     public void onCreateOptionsMenu (Menu menu, MenuInflater inflater) {
         inflater.inflate(R.menu.menu_agenda, menu);
         super.onCreateOptionsMenu(menu, inflater);
     }
 
     @Override
     public boolean onOptionsItemSelected (MenuItem item) {
         switch(item.getItemId()){
             case R.id.menu_full_details:
                 Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.ucm.es/jornadasgerencia/programa-1"));
                 startActivity(browser);
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
         
     }
     
 }
