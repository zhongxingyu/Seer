 package my.meta.shoppinglist;
 
 import java.io.ByteArrayOutputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutput;
 import java.io.ObjectOutputStream;
 import java.io.StreamCorruptedException;
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 public class ShoppingListExampleActivity extends Activity {
     
 	private static final int LIST_EDIT = 1, LIST_ADD = 2;
 	ArrayList<String> itemList = new ArrayList<String>();
 	ArrayAdapter<String> adapter;
 	protected ListView shoppingList;
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         itemList = LoadData(getString(R.string.savefile_name));
         shoppingList = (ListView) this.findViewById(R.id.listView1);
         adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, itemList);
         shoppingList.setAdapter(adapter);
         registerForContextMenu(shoppingList);
     }
     
     // Context menu creator
     public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
         super.onCreateContextMenu(menu, v, menuInfo);
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.context_menu, menu);
     }
 
 	public boolean onContextItemSelected(MenuItem item) {
         AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
         switch (item.getItemId()) {
         case R.id.remove_item:
         	RemoveItem(info.position);
             return true;
         case R.id.edit_item:
        	Intent t = new Intent("my.meta.shoppingList.inputDialog");
         	t.putExtra("title", "Edit");
         	t.putExtra("value", itemList.get(info.position));
         	t.putExtra("item_position", info.position);
         	this.startActivityForResult(t, LIST_EDIT);
             return true;
         }
         return false;
     }
 
     // Options menu creator
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.options_menu, menu);
         return true;
     }
 	
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    switch (item.getItemId()) {
         case R.id.new_item:
         	Intent newItemIntent = new Intent("my.meta.shoppingList.inputDialog");
         	newItemIntent.putExtra("title", "Add");
         	this.startActivityForResult(newItemIntent, LIST_ADD);
         	break;
         case R.id.email:
         	SendEmail();
         	break;
 	    }
 	    return true;
 	}    
 
 	// Menu event handler
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		switch (requestCode) {
 		case LIST_EDIT:
 			String edited_text = data.getStringExtra("value");
 			if (edited_text != null && edited_text.length() > 0) {
 				int num = data.getIntExtra("item_position", 0);
 				ModifyItem(num, edited_text);
 			}
 			break;
 		case LIST_ADD:
 			String added_text = data.getStringExtra("value");
 			if (added_text != null && added_text.length() > 0) {
 				AddItem(added_text);
 			}
 			break;
 		default:
 			break;
 		}
 	}
 
 	// List item modifiers
     private void AddItem(String item) {
     	itemList.add(item);
 		adapter.notifyDataSetChanged();
     }
     private void ModifyItem(int num, String item) {
 		itemList.set(num, item);
 		adapter.notifyDataSetChanged();
     }
     private void RemoveItem(int num) {
     	itemList.remove(num);
     	adapter.notifyDataSetChanged();
     }
     
     // Save data when backgrounded.
 	protected void onPause() {
 		SaveData(getString(R.string.savefile_name), itemList);
 		super.onPause();
 	}
 	
 	@SuppressWarnings("unchecked")
 	private ArrayList<String> LoadData(String filename) {
 		ArrayList<String> al = new ArrayList<String>();
 		InputStream is;
 		try {
 			is = openFileInput(filename);
 			ObjectInputStream ois = new ObjectInputStream(is);
 			al = (ArrayList<String>) ois.readObject();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (StreamCorruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return al;
 	}
 	
 	private void SaveData(String filename, ArrayList<String> data) {
 		try {
 			ByteArrayOutputStream baos = new ByteArrayOutputStream();
 			ObjectOutput out = new ObjectOutputStream(baos);
 			out.writeObject(itemList);
 			FileOutputStream fo = openFileOutput(filename, Context.MODE_PRIVATE);
 			fo.write(baos.toByteArray());
 			fo.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
     
 	// Starts a new email pre-filled with the shopping list.
     private void SendEmail() {
     	Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
 
     	emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Saved shopping list");
     	
     	emailIntent.setType("plain/text");
     	String emailMessage = "Saved shopping list:" + System.getProperty("line.separator") + System.getProperty("line.separator");
     	for(String item : itemList) {
     		emailMessage += item + System.getProperty("line.separator");
     	}
     	emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, emailMessage);
     	
     	startActivity(emailIntent);
 	}
 }
