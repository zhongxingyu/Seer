 package info.micdm.munin_client;
 
 import info.micdm.munin_client.models.Node;
 import info.micdm.munin_client.models.Server;
 import info.micdm.munin_client.models.ServerList;
 import android.app.ListActivity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 
 /**
  * Экран со списком доступных нод.
  * @author Mic, 2011
  *
  */
 public class ServerActivity extends ListActivity {
 
     /**
      * Отображаемый сервер.
      */
     protected Server _server;
     
     /**
      * Запоминает сервер, который надо отобразить.
      */
     protected void _setServer(Bundle bundle) {
         String serverName = bundle.getString("server");
         _server = ServerList.INSTANCE.get(serverName);
     }
     
     /**
      * Заполняет список нод.
      */
     protected void _fillList() {
         ArrayAdapter<Node> adapter = new ArrayAdapter<Node>(this, android.R.layout.simple_list_item_1);
         for (Node node: _server.getNodes()) {
             adapter.add(node);
         }
         getListView().setAdapter(adapter);
     }
     
     /**
      * Выполняется, когда пользователь выбирает ноду из списка.
      */
     protected void _onSelectNode(Node node) {
         Intent intent = new Intent(this, NodeActivity.class);
        intent.putExtra("server", _server.getHost());
         intent.putExtra("node", node.getName());
         startActivity(intent);
     }
     
     /**
      * Слушает клик по элементу списка.
      */
     protected void _listenToSelectNode() {
         getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                 _onSelectNode((Node)parent.getItemAtPosition(position));
             }
         });
     }
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.server);
         _setServer(getIntent().getExtras()); 
         _fillList();
         _listenToSelectNode();
     }
 }
