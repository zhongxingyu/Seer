 package info.micdm.munin_client.models;
 
 import java.util.Collection;
 import java.util.HashMap;
 
 /**
  * Сервер с мониторингом.
  * @author Mic, 2011
  *
  */
 public class Server {
 
     /**
      * Имя хоста сервера.
      */
     protected String _host;
     
     /**
      * Порт сервера.
      */
     protected int _port;
     
     /**
      * Имя пользователя.
      */
     protected String _username;
     
     /**
      * Пароль.
      */
     protected String _password;
     
     /**
      * Список ведомых серверов.
      */
     protected HashMap<String, Node> _nodes = new HashMap<String, Node>();
     
     public Server(String host) {
         this(host, 80);
     }
     
     public Server(String host, int port) {
         this(host, port, null, null);
     }
 
     public Server(String host, int port, String username, String password) {
         _host = host;
         _port = port;
         _username = username;
         _password = password;
     }
 
     public String toString() {
         return _host + ":" + _port;
     }
     
     /**
      * Возвращает хост.
      */
     public String getHost() {
         return _host;
     }
     
     /**
      * Возвращает порт.
      */
     public int getPort() {
         return _port;
     }
     
     /**
      * Возвращает логин.
      */
     public String getUsername() {
         return _username;
     }
     
     /**
      * Возвращает пароль.
      */
     public String getPassword() {
         return _password;
     }
     
     /**
      * Возвращает ноду.
      */
     public Node getNode(String name) {
         return _nodes.get(name);
     }
     
     /**
      * Возвращает список из всех нод.
      */
     public Collection<Node> getNodes() {
         return _nodes.values();
     }
 }
