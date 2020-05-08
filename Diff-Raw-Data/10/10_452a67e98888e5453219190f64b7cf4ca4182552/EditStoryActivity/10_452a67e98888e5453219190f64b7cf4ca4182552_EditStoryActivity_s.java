 /*
 * Copyright (c) 2013, TeamCMPUT301F13T02
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * 
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 * 
 * Neither the name of the {organization} nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 
 package ca.ualberta.CMPUT301F13T02.chooseyouradventure;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.webkit.WebView;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.ScrollView;
 import android.widget.TextView;
 
 /**
  * This Activity allows a story's author to edit a story by adding adding pages,
  * deleting pages, setting the first page, or deleting the story.
  * 
  * This class is part of the view of the application.
  * 
  * TODO Delete functionality not yet hooked up
  */
 
 public class EditStoryActivity extends Activity {
 	private ListView treePage;
 	private Button createNew2;
 	private Button deleteStory;
 	private ArrayList<String> pageText = new ArrayList<String>();
 	private ArrayList<Page> pageList = new ArrayList<Page>();
 	private ArrayAdapter<String> adapter;
 	private ControllerApp app;
 	private static final int HELP_INDEX = 0;
 
 	/**
 	 * This binds the buttons the the views to this activity
 	 * and sets the appropriate onclick listeners
 	 */
 	@Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.edit_story_activity);
         treePage = (ListView) findViewById(R.id.treeView);
         createNew2 = (Button) findViewById(R.id.createButton2);
         deleteStory = (Button) findViewById(R.id.deleteButton);
         createNew2.setOnClickListener(new OnClickListener() {
             public void onClick(View v) {
               try
 			{
 				createPage();
 			} catch (HandlerException e)
 			{
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
               adapter.notifyDataSetChanged();
             }
         });
         deleteStory.setOnClickListener(new OnClickListener() {
             
             public void onClick(View v) {
               //deleteCurrentStory();
             }
         });       
         app = (ControllerApp) getApplication();
         
 		pageList = app.getStory().getPages();
 		pageText = app.updateView(pageList, pageText);
 		/**
 		 * Activity to restructure Click and longClick listeners to work in a list view
 		 *  directly based on http://android.konreu.com/developer-how-to/click-long-press-event-listeners-list-activity/
 		 */
 		treePage.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 		    @Override
 		    public void onItemClick(AdapterView<?> av, View v, int pos, long listNum) {
 		        onListItemClick(v,pos,listNum);
 		    }
 		});
 		adapter = new ArrayAdapter<String>(this,
 				R.layout.list_item_base, pageText);
 		treePage.setAdapter(adapter);
     }
 	
 	@Override
 	public void onStart() {
         super.onStart();
         refresh();
     }
 	
 	/**
 	 * Inflate the options menu; this adds items to the action bar if it is present 
 	 * 
 	 *  @param menu The menu to inflate
 	 *  @return Success
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 	
 		MenuItem help = menu.add(0, HELP_INDEX, HELP_INDEX, "Help");
 		help.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
 	
 	    return true;
 	}
 
 	/**
 	 * Callback for clicking an item in the menu.
 	 * 
 	 * @param item The item that was clicked
 	 * @return Success
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) 
 	{
 		switch (item.getItemId()) {
 		case HELP_INDEX:
 	
 			ScrollView scrollView = new ScrollView(this);
 			WebView view = new WebView(this);
 	
 	    	view.loadData(getString(R.string.edit_story_help), "text/html", "UTF-8");
 	        
 	        scrollView.addView(view);
 	        scrollView.setPadding(10, 10, 10, 10);
 	        
 	        AlertDialog.Builder builder = new AlertDialog.Builder(this);
 	        builder.setTitle(R.string.help);
 	        builder.setPositiveButton(R.string.ok, null);
 	        builder.setView(scrollView);
 	        builder.show();
 	        
 			break;
 		}
 		return true;
 	}
 
 	/**
 	 * Click listener for the list of pages. It simply calls pageOptions
 	 * and passes it the position of the selected page. 
 	 * @param v
 	 * @param pos
 	 * @param id
 	 */
 	protected void onListItemClick(View v, int pos, long id) {
 		pageOptions(pos);
 	}
 	
 	/**
 	 * This creates a page
 	 * @throws HandlerException
 	 */
 	private void createPage() throws HandlerException{
     	AlertDialog.Builder builder = new AlertDialog.Builder(this);
     	builder.setTitle("Create New");	
     	final LinearLayout layout = new LinearLayout(this);
     	layout.setOrientation(LinearLayout.VERTICAL);
     	
     	final EditText alertEdit = new EditText(this);
     	layout.addView(alertEdit);
     	
     	final EditText alertEdit3 = new EditText(this);
     	final CheckBox check = new CheckBox(this);
     	final EditText alertEdit2 = new EditText(this);
     	
     	if(app.getStory().isUsesCombat()){
     		final TextView alertText = new TextView(this);
         	alertText.setText("Fighting Fragment?");
         	layout.addView(alertText);
         	
         	
         	layout.addView(check);
         	
         	final TextView alertText2 = new TextView(this);
         	alertText2.setText("Health of the Enemy on this Page?");
         	layout.addView(alertText2);
         	
         	
         	alertEdit2.setText("0");
         	layout.addView(alertEdit2);
         	
         	final TextView alertText3 = new TextView(this);
         	alertText3.setText("Name of the Enemy on this Page?");
         	layout.addView(alertText3);
         	
         	
         	alertEdit3.setText("Enemy");
         	layout.addView(alertEdit3);
     	}
     	
     	
     	builder.setView(layout);
     	builder.setMessage("Enter the title of your new page")
     	.setPositiveButton("Save", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id) {
             	String pageTitle = alertEdit.getText().toString();
             	app.updateTitle(pageTitle, check.isChecked(), alertEdit2.getText().toString(), alertEdit3.getText().toString());         	
             	refresh();
             	
             }
         })
         .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id) {
                 
             }
         });
         builder.show();
     	
     	
     }
 
 	/**
 	 * This shows the user a list of options on a story
 	 * @param Input from longclick
 	 */
 	public void pageOptions(final int pos){
		final Page currentPage = pageList.get(pos);
 		final Page FP = app.getStory().getFirstpage();
 		String[] titlesA = {"Goto/Edit","Page Properties","Cancel"};
 		String[] titlesB = {"Goto/Edit","Page Properties","Assign as First Page","Delete","Cancel"};
 		final String[] titles;
 		if(currentPage == FP){titles = titlesA;}
 		else{titles = titlesB;}
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         final AlertDialog.Builder titleEditor = new AlertDialog.Builder(this);
         final EditText alertEdit = new EditText(this);
         
         final EditText alertEdit3 = new EditText(this);
 		final CheckBox check = new CheckBox(this);
 		final EditText alertEdit2 = new EditText(this);
 		final LinearLayout layout = new LinearLayout(this);
 		final TextView alertText2 = new TextView(this);
 		final TextView alertText = new TextView(this);
 		final TextView alertText3 = new TextView(this);
 		
 		
         builder.setTitle(R.string.page_options);
         builder.setItems(titles, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int item) {
             	
             	switch(item){
             	case(0):
             		app.setEditing(true);
             		app.jump(ViewPageActivity.class,app.getStory(),app.getStory().getPages().get(pos));
             		
             	break;
             	case(1):
             		
             		
             		titleEditor.setTitle("Create New");	
             		
             		layout.setOrientation(LinearLayout.VERTICAL);
 
             		
             		layout.addView(alertEdit);
 
             		
 
             		if(app.getStory().isUsesCombat()){
           			
             			alertText.setText("Fighting Fragment?");
             			layout.addView(alertText);
             			layout.addView(check);     			
             			alertText2.setText("Health of the Enemy on this Page?");
             			layout.addView(alertText2);
            			alertEdit2.setText("0");
             			layout.addView(alertEdit2);
             			alertText3.setText("Name of the Enemy on this Page?");
             			layout.addView(alertText3);
            			alertEdit3.setText("Enemy");
             			layout.addView(alertEdit3);
             		}
 
 
             		titleEditor.setView(layout);
             		titleEditor.setMessage("Enter the title of your new page")
             		.setPositiveButton("Save", new DialogInterface.OnClickListener() {
             			public void onClick(DialogInterface dialog, int id) {
             				String pageTitle = alertEdit.getText().toString();
             				app.updateTitle(pageTitle, currentPage);         	
             				refresh();
 
             			}
             		})
             		.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
             			public void onClick(DialogInterface dialog, int id) {
 
             			}
             		});
             		titleEditor.show();
             		break;
             	case(2):
             		app.updateFP(currentPage);
             		refresh();
             		break;
             	case(3):
             		app.removePage(currentPage);
             		refresh();
             		break;
 
             	}
 
             }	
                 });
         builder.show();
     }
 	
 	/**
 	 * This rebuilds the ListView by recollecting data from the controller
 	 */
 	public void refresh(){
 		pageList = app.getStory().getPages();
 		pageText = app.updateView(pageList, pageText);
 		adapter.notifyDataSetChanged();
 	}	
 }
