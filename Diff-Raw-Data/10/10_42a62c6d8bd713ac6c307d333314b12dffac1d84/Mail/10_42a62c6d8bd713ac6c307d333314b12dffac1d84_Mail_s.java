 package elxris.Useless.Objects;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.OfflinePlayer;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 
 import elxris.Useless.Useless;
 import elxris.Useless.Utils.Archivo;
 import elxris.Useless.Utils.Chat;
 import elxris.Useless.Utils.Fecha;
 import elxris.Useless.Utils.Strings;
 
 public class Mail {
     FileConfiguration cache;
     Archivo archivo;
     
     public Mail(){
         archivo = new Archivo("mail.yml");
         load();
     }
     public void load(){
         cache = archivo.load();
         interpreta();
     }
     public void save(){
         cache.set("usuarios", null);
         archivo.save(cache);
         load();
     }
     public void interpreta(){
         List<Long> listacorreos = cache.getLongList("correos.mensajes");
         List<Long> remover = cache.getLongList("correos.mensajes");
         //Quitar los mensajes anteriores.
         for(OfflinePlayer p: Useless.plugin().getServer().getOfflinePlayers()){
             cache.set("usuarios."+p.getName()+".mensajes", null);
         }
         for(Long k: listacorreos){
             List<String> usuarios = cache.getStringList("correos."+k+".usuarios");
             if(usuarios.size() == 0 || System.currentTimeMillis()-k >= 1296000000){
                 // Los correos mayores a 15 das (15*24*60*60*1000) milisegundos, se eliminan.
                 cache.set("correos."+k, null);
                 remover.remove(k);
             }
             for(String k2: usuarios){
                 List<Long> listaAnterior = new ArrayList<Long>();
                 if(cache.isSet("usuarios."+k2+".mensajes")){
                     listaAnterior = cache.getLongList("usuarios."+k2+".mensajes");
                     listaAnterior.add(k);
                 }else{
                     listaAnterior.add(k);
                 }
                 cache.set("usuarios."+k2+".mensajes", listaAnterior);
             }
         }
         cache.set("correos.mensajes", remover);
     }
     public void eliminar(String jugador, Long mail){
         List<String> usuarios = cache.getStringList("correos."+mail+".usuarios");
         usuarios.remove(jugador);
         cache.set("correos."+mail+".usuarios", usuarios);
     }
     public void eliminarAll(String jugador){
         List<Long> mensajes = cache.getLongList("usuarios."+jugador+".mensajes");
         for(Long lng: mensajes){
             List<String> usuarios = cache.getStringList("correos."+lng+".usuarios");
             usuarios.remove(jugador);
             cache.set("correos."+lng+".usuarios", usuarios);
         }
     }
     public String[] getMail(Long id){
         String remitente = cache.getString("correos."+id+".remitente");
         if(cache.getBoolean("correos."+id+".servidor") == true){
             remitente = "Servidor";
         }
         String[] mail = {remitente,
                 Fecha.formatoFecha(id),
                 cache.getString("correos."+id+".mensaje"), remitente};
         return mail;
     }
     public void getMailList(String jugador){
         List<Long> mensajes = cache.getLongList("usuarios."+jugador+".mensajes");
         Chat.mensaje(jugador, "mbox.list", mensajes.size());
     }
     public void getNextMail(String jugador, Boolean eliminar){ //Obtiene todos los corroes.
         List<Long> mensajes = cache.getLongList("usuarios."+jugador+".mensajes");
         if(mensajes.size() == 0){
             Chat.mensaje(jugador, "mbox.listEnd");
             return;
         }
         //
         Chat.mensaje(jugador, "mbox.readStart");
         // Enviando cada uno de los mensajes.
         for(long lng: mensajes){
             String[] mensaje = getMail(lng);
             Chat.mensaje(jugador, "mbox.mail", mensaje);
             if(eliminar){
                 eliminar(jugador, lng);
             }
         }
         Chat.mensaje(jugador, "mbox.readFinish");
     }
     public void createBorrador(String jugador, String args[]){ //Inicia el borrador.
         clearBorrador(jugador);
         List<String> destinatarios = new ArrayList<String>();
         for(String k: args){
             List<Player> l =Useless.plugin().getServer().matchPlayer(k);
             if(l.size() == 1){
                 destinatarios.add(l.get(0).getName());
             }else{
                 if(Useless.plugin().getServer().getOfflinePlayer(k).getFirstPlayed() != 0){
                     destinatarios.add(k);
                     continue;
                 }
                 Chat.mensaje(jugador, "mboxc.playerNotExist", k);
             }
         }
         if(destinatarios.size() >= 1){
             cache.set("usuarios."+jugador+".borrador.destinatarios", destinatarios);
             Chat.mensaje(jugador, "mboxc.created");
         }else{
             Chat.mensaje(jugador, "mboxc.noPlayerAdded");
         }
     }
     public void setMensaje(String jugador, String mensaje){
         cache.set("usuarios."+jugador+".borrador.mensaje", mensaje);
     }
     public void addMensaje(String jugador, String mensaje){
         if(cache.getStringList("usuarios."+jugador+".borrador.destinatarios").size() < 1){
             Chat.mensaje(jugador, "mboxc.noMessage");
             return;
         }
         String mensajeAnterior = "";
         if(cache.isSet("usuarios."+jugador+".borrador.mensaje")){
             mensajeAnterior = cache.getString("usuarios."+jugador+".borrador.mensaje");
         }
         setMensaje(jugador, mensajeAnterior+" "+mensaje);
         if(mensaje.length() > Strings.getInt("mboxc.v.maxChar")){
             if(!Useless.getPlayer(jugador).hasPermission("useless.mail.noCharLimit")){
                 Chat.mensaje(jugador, "mboxc.limit");
                 return;
             }
         }
         Chat.mensaje(jugador, "mboxc.add");
     }
     public void clearMensaje(String jugador){
         setMensaje(jugador, "");
     }
     public void clearBorrador(String jugador){
         cache.set("usuarios."+jugador+".borrador", null);
         save();
     }
     public void sendMensaje(String jugador, List<String> destinatarios, String mensaje, Boolean servidor){
         if(!cache.isSet("usuarios."+jugador+".borrador.mensaje")){
             Chat.mensaje(jugador, "mboxc.noMessage");
             return;
         }
         long fecha = System.currentTimeMillis();
         List<Long> mensajes = cache.getLongList("correos.mensajes");
         mensajes.add(fecha);
         cache.set("correos.mensajes", mensajes);
         String path = "correos."+fecha+".";
         cache.set(path+"remitente", jugador);
         cache.set(path+"servidor", servidor);
         cache.set(path+"mensaje", mensaje);
         cache.set(path+"usuarios", destinatarios);
         for(String k: destinatarios){
             Chat.mensaje(k, "mboxc.catched");
         }
         Chat.mensaje(jugador, "mboxc.sended");
         clearBorrador(jugador);
     }
     public void sendMensaje(String jugador){
         List<String> destinatarios = cache.getStringList("usuarios."+jugador+".borrador.destinatarios");
         String mensaje = cache.getString("usuarios."+jugador+".borrador.mensaje");
         sendMensaje(jugador, destinatarios, mensaje, false);
     }
     public void sendMensajeATodos(String jugador){
         if(!Useless.getPlayer(jugador).hasPermission("useless.mail.massive")){
             return;
         }
         List<String> destinatarios = new ArrayList<>();
         for(OfflinePlayer p: Useless.plugin().getServer().getOfflinePlayers()){
             destinatarios.add(p.getName());
         }
         sendMensaje(jugador, destinatarios, cache.getString("usuarios."+jugador+".borrador.mensaje"), true);
     }
 }
