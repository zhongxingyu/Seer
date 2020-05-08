 package info.micdm.munin_client.models;
 
 import java.util.Collection;
 import java.util.HashMap;
 
 /**
  * Список серверов.
  * @author Mic, 2011
  *
  */
 public class ServerList {
 
     /**
      * Синглтон.
      */
     public final static ServerList INSTANCE = new ServerList();
     
     /**
      * Список серверов.
      */
     protected HashMap<String, Server> _servers;
     
     /**
      * Загружает список серверов из хранилища.
      */
     protected void _load() {
         if (_servers != null) {
             return;
         }
         _servers = new HashMap<String, Server>();
         new ServerListStore().load(this);
     }
     
     /**
      * Сохраняет список серверов в хранилище.
      */
     public void save() {
         new ServerListStore().save(this);
     }
     
     /**
      * Возвращает сервер.
      */
     public Server get(String name) {
         _load();
         return _servers.get(name);
     }
     
     /**
      * Возвращает список всех серверов.
      */
     public Collection<Server> getAll() {
         _load();
         return _servers.values();
     }
     
     /**
      * Добавляет сервер в список.
      */
     public void add(Server server) {
        _servers.put(server.getHost(), server);
     }
     
     /**
      * Удаляет сервер из списка.
      */
     public void delete(Server server) {
        _servers.remove(server.getHost());
     }
 }
