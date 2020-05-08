 package info.micdm.munin_client;
 
 import info.micdm.munin_client.models.Server;
 import info.micdm.munin_client.models.ServerList;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.TextView;
 
 /**
  * Экран для добавления нового сервера.
  * @author Mic, 2011
  *
  */
 public class EditServerActivity extends Activity {
 
     /**
      * Сервер для редактирования.
      * Равно null, если добавляется новый сервер. 
      */
     protected Server _server;
     
     /**
      * Задает текст.
      */
     protected void _setInputText(int id, String text) {
         ((TextView)findViewById(id)).setText(text);
     }
     
     /**
      * Возвращает введенный текст.
      */
     protected String _getInputText(int id) {
         return ((TextView)findViewById(id)).getText().toString();
     }
     
     /**
      * Запоминает сервер для редактирования.
      */
     protected void _setServer(Bundle bundle) {
         if (bundle == null || !bundle.containsKey("server")) {
             return;
         }
         String serverName = bundle.getString("server");
         _server = ServerList.INSTANCE.get(serverName);
         _setInputText(R.id.server_host, _server.getHost());
         _setInputText(R.id.server_port, String.valueOf(_server.getPort()));
         _setInputText(R.id.server_username, _server.getUsername());
         _setInputText(R.id.server_password, _server.getPassword());
     }
     
     /**
      * Вызывается при переключении флажка.
      */
     protected void _onNeedAuthChanged(boolean isChecked) {
         int visibility = isChecked ? View.VISIBLE : View.GONE;
         findViewById(R.id.server_username).setVisibility(visibility);
         findViewById(R.id.server_password).setVisibility(visibility);
     }
     
     /**
      * Слушает клик по чекбоксу "Авторизовать".
      */
     protected void _listenToNeedAuthChanged() {
         CheckBox needAuth = (CheckBox)findViewById(R.id.server_need_auth);
         needAuth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
             @Override
             public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                 _onNeedAuthChanged(isChecked);
             }
         });
     }
     
     /**
      * Формирует объект нового сервера по введенным данным.
      */
     protected Server _getNewServer() {
         String host = _getInputText(R.id.server_host);
         String port = _getInputText(R.id.server_port);
         if (host.length() == 0 || port.length() == 0) {
             return null;
         }
         String username = null;
         String password = null;
         CheckBox needAuth = (CheckBox)findViewById(R.id.server_need_auth);
         if (needAuth.isChecked()) {
             username = _getInputText(R.id.server_username);
             password = _getInputText(R.id.server_password);
             if (username.length() == 0 || password.length() == 0) {
                 return null;
             }
         }
         return new Server(host, Integer.parseInt(port), username, password);
     }
     
     /**
      * Возвращает к списку серверов.
      */
     protected void _goToServerList() {
         Intent intent = new Intent(this, ServerListActivity.class);
         startActivity(intent);
     }
     
     /**
      * Вызывается для добавления нового сервера.
      */
     protected void _onAddServer() {
         Server server = _getNewServer();
         if (server != null) {
             if (_server != null) {
                 ServerList.INSTANCE.delete(_server);
                 _server = null;
             }
             ServerList.INSTANCE.add(server);
             ServerList.INSTANCE.save();
             _goToServerList();
         }
     }
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.edit_server);
         _setServer(getIntent().getExtras());
         _listenToNeedAuthChanged();
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.edit_server_options, menu);
         return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
        case R.id.server_add:
             _onAddServer();
             return true;
         default:
             return super.onOptionsItemSelected(item);
         }
     }
 }
