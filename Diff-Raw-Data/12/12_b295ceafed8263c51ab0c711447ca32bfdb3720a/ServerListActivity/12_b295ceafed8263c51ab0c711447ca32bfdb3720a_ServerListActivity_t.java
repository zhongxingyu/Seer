 package info.micdm.munin_client;
 
import info.micdm.munin_client.data.Server;
import info.micdm.munin_client.data.ServerList;

 import java.net.URI;
 import java.net.URISyntaxException;
 
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 /**
  * Диалог для добавления и редактирования сервера.
  * @author Mic, 2011
  *
  */
 class EditServerDialogBuilder {
 
     /**
      * Интерфейс для обработчика.
      * @author Mic, 2011
      *
      */
     public interface OnSaveListener {
         public void onSave(String text);
     }
     
     /**
      * Возвращает текстовое поле.
      */
     protected TextView _getTextView(Context context) {
         LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         return (TextView)inflater.inflate(R.layout.edit_server, null);
     }
     
     /**
      * Добавляет кнопку.
      */
     protected DialogInterface.OnClickListener _getClickListener(final TextView textField, final OnSaveListener listener) {
         return new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 String text = textField.getText().toString();
                 listener.onSave(text);
             }
         };
     }
     
     /**
      * Создает и показывает диалог.
      */
     public void show(Context context, OnSaveListener listener) {
         show(context, listener, null);
     }
     
     /**
      * Создает и показывает диалог.
      */
     public void show(Context context, OnSaveListener listener, String text) {
         AlertDialog.Builder builder = new AlertDialog.Builder(context);
         builder.setCancelable(true);
         TextView textField = _getTextView(context);
         if (text != null) {
             textField.setText(text);
         }
         builder.setView(textField);
         DialogInterface.OnClickListener onClick = _getClickListener(textField, listener);
         builder.setPositiveButton(R.string.server_save, onClick);
         builder.show();
     }
 }
 
 
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
         EditServerDialogBuilder builder = new EditServerDialogBuilder();
         builder.show(this, new EditServerDialogBuilder.OnSaveListener() {
             @Override
             public void onSave(String text) {
                 try {
                     URI uri = new URI(text);
                     ServerList.INSTANCE.add(new Server(uri));
                    ServerList.INSTANCE.save();
                     _fillList();
                 } catch (URISyntaxException e) {
                 
                 }
             }
         });
     }
     
     /**
      * Выполняется, когда пользователь выбирает сервер из списка.
      */
     protected void _onSelectServer(Server server) {
         Intent intent = new Intent(this, ServerActivity.class);
         intent.putExtra("server", server.getName());
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
     protected void _onEditServer(final Server server) {
         EditServerDialogBuilder builder = new EditServerDialogBuilder();
         builder.show(this, new EditServerDialogBuilder.OnSaveListener() {
             @Override
             public void onSave(String text) {
                 try {
                     URI uri = new URI(text);
                     ServerList.INSTANCE.delete(server);
                     ServerList.INSTANCE.add(new Server(uri));
                    ServerList.INSTANCE.save();
                     _fillList();
                 } catch (URISyntaxException e) {
                 
                 }
             }
         }, server.getUri().toString());
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
