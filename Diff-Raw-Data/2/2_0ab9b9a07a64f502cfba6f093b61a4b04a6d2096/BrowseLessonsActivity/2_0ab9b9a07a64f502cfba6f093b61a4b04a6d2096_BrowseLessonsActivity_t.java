 package edu.upenn.cis350.Trace2Learn;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import edu.upenn.cis350.Trace2Learn.Database.DbAdapter;
 import edu.upenn.cis350.Trace2Learn.Database.Lesson;
 import edu.upenn.cis350.Trace2Learn.Database.LessonCharacter;
 import edu.upenn.cis350.Trace2Learn.Database.LessonItem;
 import edu.upenn.cis350.Trace2Learn.Database.LessonWord;
 import android.app.Activity;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.os.Bundle;
 import android.text.Editable;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.Display;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.Gallery;
 import android.widget.ListView;
 import android.widget.PopupWindow;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class BrowseLessonsActivity extends ListActivity {
 	private View layout;
 	private PopupWindow window;
 	private ListView list, lessonList; //list of words to display in listview
 
 	private Lesson le;
 	private DbAdapter dba; 
 	private ArrayList<Lesson> items;
 	ArrayAdapter<String> arrAdapter;
 
 	final Context c = this;
 	
 	Intent i;
 
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.browse_lessons);
         dba = new DbAdapter(this);
         dba.open(); //opening the connection to database        
         
         list = (ListView)findViewById(R.id.list);
         
         items = new ArrayList<Lesson>(); //items to show in ListView to choose from 
         List<Long> ids = dba.getAllLessonIds();
         for(long id : ids){
         	Lesson le = dba.getLessonById(id);
         	le.setTagList(dba.getLessonTags(id));
         	items.add(le);
         }
         LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         LessonListAdapter la = new LessonListAdapter(this,items,vi);
         setListAdapter(la);
 
         //when a char is clicked, it is added to the new word and added to the gallery
         i = new Intent(this, BrowseWordsActivity.class);
         /*list.setOnItemClickListener(new OnItemClickListener() {
         	
             public void onItemClick(AdapterView<?> parent, View view, int position,long id) { 
             	Lesson le = ((Lesson)list.getItemAtPosition(position));
        		Intent i = new Intent(c, BrowseWordsActivity.class);
         		i.putExtra("ID", le.getId());
         		startActivity(i);
             }
         });*/
     }
 	
 	@Override  
 	protected void onListItemClick(ListView l, View v, int position, long id) {  
 	  super.onListItemClick(l, v, position, id);  
 	  
 	  clickOnItem(items.get(position));
 	} 
 	
 	//when character is clicked, it starts the display mode for that char
 		public void clickOnItem(LessonItem li){
 			Lesson le = ((Lesson)li);
     		
     		i.putExtra("ID", le.getId());
     		startActivity(i);
 			
 			Intent intent = new Intent();
 			Bundle bun = new Bundle();
 
 			bun.putString("mode", "display");
 			bun.putLong("wordId", li.getId());
 
 			intent.setClass(this, PhrasePracticeActivity.class);
 			intent.putExtras(bun);
 			startActivity(intent);
 		}
 	
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 	    ContextMenuInfo menuInfo) {
 	    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
 	    menu.setHeaderTitle("Options");
 	    String[] menuItems = {"Edit Tags", "Delete"};
 	    for (int i = 0; i<menuItems.length; i++) {
 	      menu.add(Menu.NONE, i, i, menuItems[i]);
 	    }
 	}
 	
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 	  AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
 	  int menuItemIndex = item.getItemId();
 	  le = (Lesson)items.get(info.position);
 	  Log.e("MenuIndex",Integer.toString(menuItemIndex));
 	  Log.e("ListIndex",Integer.toString(info.position));
 	  
 	  //edit tags
 	  if(menuItemIndex==0){
 		  Intent i = new Intent(c, TagActivity.class);
 		  startActivity(i);
 		  return true;
 	  }
 	  
 	  //edit words
 	  /*else if(menuItemIndex==1){
 		  Intent i = new Intent(c, EditWordsActivity.class);
 		  startActivity(i);
 	  }*/
 	  
 	  //delete lesson
 	  else if(menuItemIndex==1){
 		  long id = le.getId();
 		  long result = dba.deleteLesson(id);
 		  Log.e("Result",Long.toString(result));
 		  if(result < 0){
 			  showToast("Could not delete the lesson");
 			  return false;
 		  }
 		  else{
 			  showToast("Successfully deleted");
 			  startActivity(getIntent()); 
 			  finish();
 			  return true;
 		  }
 	  }
 	  return false;
 	}
 	
 	public void showToast(String msg){
 		Context context = getApplicationContext();
 		CharSequence text = msg;
 		int duration = Toast.LENGTH_SHORT;
 
 		Toast toast = Toast.makeText(context, text, duration);
 		toast.show();
 	}
 	
 	private void initiatePopupWindow(){
 		try {
 			Display display = getWindowManager().getDefaultDisplay(); 
 			int width = display.getWidth();  // deprecated
 			int height = display.getHeight();  // deprecated
 	        //We need to get the instance of the LayoutInflater, use the context of this activity
 	        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 	        //Inflate the view from a predefined XML layout
 	        layout = inflater.inflate(R.layout.add_to_collection_popup,(ViewGroup) findViewById(R.id.popup_layout));
 	        layout.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
 	        // create a 300px width and 470px height PopupWindow
 	        List<String> allLessons = dba.getAllLessonNames();
 	        Log.e("numLessons",Integer.toString(allLessons.size()));
 	        lessonList = (ListView)layout.findViewById(R.id.collectionlist);
 	        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,allLessons); 
 	        lessonList.setAdapter(adapter);
 	        window = new PopupWindow(layout, layout.getMeasuredWidth(), (int)(height*.8), true);
 	        // display the popup in the center
 	        window.showAtLocation(layout, Gravity.CENTER, 0, 0);
 	
 	        lessonList.setOnItemClickListener(new OnItemClickListener() {
 	            
 	            public void onItemClick(AdapterView<?> parent, View view, int position,long id) {     
 	               String name = ((String)lessonList.getItemAtPosition(position));
 	               Log.e("name",name);
 	               long success = dba.addWordToLesson(name, le.getId());
 	               Log.e("adding word",Long.toString(success));
 	               showToast("Successfully Added");
 	               window.dismiss();
 	            }
 	        });
 	 
 	    } catch (Exception e) {
 	        e.printStackTrace();
 	    }
 	}
 	
 	public void onSkipButtonClick(View view){
 		window.dismiss();
 	}
 }
