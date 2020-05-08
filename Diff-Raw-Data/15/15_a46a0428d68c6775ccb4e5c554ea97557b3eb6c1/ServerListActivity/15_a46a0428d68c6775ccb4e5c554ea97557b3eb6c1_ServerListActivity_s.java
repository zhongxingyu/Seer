 package info.micdm.munin_client;
 
 import info.micdm.munin_client.models.Server;
 import info.micdm.munin_client.models.ServerList;
 import android.app.ListActivity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.ArrayAdapter;
 
 /**
  * Экран со списком доступных серверов.
  * @author Mic, 2011
  *
  */
 public class ServerListActivity extends ListActivity {
 
     /**
      * Заполняет список серверов.
      */
     protected void _fillList() {
         ArrayAdapter<Server> adapter = new ArrayAdapter<Server>(this, android.R.layout.simple_list_item_1);
         for (Server server: ServerList.INSTANCE.getAll()) {
             adapter.add(server);
         }
         setListAdapter(adapter);
     }
     
     /**
      * Выполняется, когда пользователь хочет добавить новый сервер.
      */
     protected void _onAddNewServer() {
         Intent intent = new Intent(this, EditServerActivity.class);
         startActivity(intent);
     }
     
     /**
      * Выполняется, когда пользователь выбирает сервер из списка.
      */
     protected void _onSelectServer(Server server) {
         Intent intent = new Intent(this, ServerActivity.class);
        intent.putExtra("server", server.getHost());
         startActivity(intent);
     }
     
     /**
      * Слушает клик по элементу списка.
      */
     protected void _listenToSelectServer() {
         getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                 _onSelectServer((Server)parent.getItemAtPosition(position));
             }
         });
     }
     
     /**
      * Выполняется, когда пользователь хочет отредактировать сервер.
      */
     protected void _onEditServer(Server server) {
         Intent intent = new Intent(this, EditServerActivity.class);
         intent.putExtra("server", server.getName());
         startActivity(intent);
     }
     
     /**
      * Выполняется, когда пользователь хочет удалить сервер.
      */
     protected void _onDeleteServer(Server server) {
         ServerList.INSTANCE.delete(server);
         ServerList.INSTANCE.save();
         _fillList();
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.server_list);
         registerForContextMenu(getListView());
         _listenToSelectServer();
     }
     
     @Override
     public void onResume() {
         super.onResume();
         _fillList();
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.server_list_options, menu);
         return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case R.id.server_add:
             _onAddNewServer();
             return true;
         default:
             return super.onOptionsItemSelected(item);
         }
     }
     
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
         super.onCreateContextMenu(menu, v, menuInfo);
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.server_list_item_options, menu);
     }
     
     @Override
     public boolean onContextItemSelected(MenuItem item) {
         AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
         Server server = (Server)getListAdapter().getItem(info.position);
         switch (item.getItemId()) {
         case R.id.server_edit:
             _onEditServer(server);
             return true;
         case R.id.server_delete:
             _onDeleteServer(server);
             return true;
         default:
             return super.onContextItemSelected(item);
         }
     }
 }
