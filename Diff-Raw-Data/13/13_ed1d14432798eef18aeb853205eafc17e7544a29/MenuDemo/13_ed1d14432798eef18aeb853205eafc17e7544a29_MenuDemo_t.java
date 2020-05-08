 package com.example;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.ContextMenu;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import java.util.Random;
 
 /**
  * Provides a demo for Android Menus
  *
  * @author Kannan Chandrasekaran
  */
 public class MenuDemo extends Activity {
 
    private TextView m_context1;
    private TextView m_context2;
    private final Random rand = new Random();
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.menudemo);
      m_context1 = (TextView) findViewById(R.id.context_color);
      m_context2 = (TextView) findViewById(R.id.context_platforms);
       TextView optionsMenu = (TextView) findViewById(R.id.resetMenu);
 
       optionsMenu.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
             invalidateOptionsMenu();
          }
       });
       registerForContextMenu(m_context1);
       registerForContextMenu(m_context2);
    }
 
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
       menu.add(
               Menu.NONE, // Group ID
               Menu.FIRST + 1, // ItemId
               Menu.NONE,  // ordering
               "Option " + rand.nextInt());// title
       return super.onCreateOptionsMenu(menu);
    }
    //todo: provide a onMenuClickListener
 
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
       int size = menu.size();
       MenuItem lastItem = menu.getItem(size - 1);
       int itemId = lastItem.getItemId() + 1;
       menu.add(lastItem.getGroupId(), itemId, lastItem.getOrder(), "option " + itemId);
       return true;
    }
 
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       String text = item.getTitle() + ":" + item.getItemId();
       Toast.makeText(this, text, Toast.LENGTH_LONG).show();
       return true;
    }
 
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
       super.onCreateContextMenu(menu, v, menuInfo);
       int menuId;
       switch (v.getId()) {
         case R.id.context_color:
              createMenu(R.menu.color_menu,menu,"Choose a color");
             break;
         case R.id.context_platforms:
             createMenu(R.menu.platforms_menu,menu,"Choose a platform");
             break;
       }
    }
 
    private void createMenu(int menuID,ContextMenu menu, String title) {
      getMenuInflater().inflate(menuID, menu);
       menu.setHeaderTitle(title);
    }
 
    @Override
    public boolean onContextItemSelected(MenuItem item) {
       switch (item.getItemId()) {
          case R.id.menu_red:
             m_context1.setBackgroundResource(R.color.LightRed);
             return true;
          case R.id.menu_blue:
             m_context1.setBackgroundResource(R.color.DullBlue);
             return true;
          case R.id.menu_green:
             m_context1.setBackgroundResource(R.color.LightGreen);
             return true;
          case R.id.menu_android:
             Toast.makeText(this,"Android",Toast.LENGTH_SHORT).show();
             return true;
          case R.id.menu_windows:
             Toast.makeText(this,"Windows",Toast.LENGTH_SHORT).show();
             return true;
          case R.id.menu_ios:
             Toast.makeText(this,"IOS",Toast.LENGTH_SHORT).show();
             return true;
          default:
             return super.onContextItemSelected(item);
       }
    }
 
 
 }
