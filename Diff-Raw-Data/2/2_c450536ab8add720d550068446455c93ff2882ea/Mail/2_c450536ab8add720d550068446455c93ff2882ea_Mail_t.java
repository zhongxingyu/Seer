 package elxris.SpiceCraft.Objects;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import org.bukkit.configuration.Configuration;
 import org.bukkit.configuration.MemoryConfiguration;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 
 import elxris.SpiceCraft.SpiceCraft;
 import elxris.SpiceCraft.Utils.Archivo;
 import elxris.SpiceCraft.Utils.Chat;
 import elxris.SpiceCraft.Utils.Fecha;
 import elxris.SpiceCraft.Utils.Strings;
 
 public class Mail{
     FileConfiguration cache;
     Configuration draft;
     Archivo archivo;
     public Mail(){
         interpreta();
     }
     public void interpreta(){
         if(!getConfig().isSet("msg")){
             sendMensajeATodos(SpiceCraft.plugin().getConfig().getString("mail.serverUserName", "Server"),
                     Strings.getString("mbox.first"));
             return;
         }
         Set<String> listacorreos = getConfig().getConfigurationSection("msg").getKeys(false);
         for(String k: listacorreos){
             int usuarios = getConfig().getConfigurationSection("msg."+k+".usuarios").getKeys(false).size();
             if(usuarios == 0 || 
                     System.currentTimeMillis()-Long.parseLong(k) >=
                     SpiceCraft.plugin().getConfig().getLong("mail.clearOnDays", 15)*24*60*60*1000){
                 // Los correos mayores a 15 das (15*24*60*60*1000) milisegundos, se eliminan.
                 getConfig().set("msg."+k, null);
             }
         }
     }
     public void eliminar(String jugador, Long mail){
         getConfig().set("msg."+mail+".usuarios."+jugador, null);
     }
     public void eliminarAll(String jugador){
         if(!getConfig().isSet("msg")){
             return;
         }
         Set<String> mensajes = getConfig().getConfigurationSection("msg").getKeys(false);
         for(String lng: mensajes){
             eliminar(jugador, Long.parseLong(lng));
         }
         save();
     }
     public String[] getMail(Long id){
         String remitente = getConfig().getString("msg."+id+".remitente");
         if(getConfig().getBoolean("msg."+id+".servidor") == true){
             remitente = "Servidor";
         }
         String[] mail = {remitente,
                 Fecha.formatoFechaDiff(id),
                 getConfig().getString("msg."+id+".mensaje"), remitente};
         return mail;
     }
     public void getMailList(String jugador){
         int mensajes = 0;
         Set<String> mail = getConfig().getConfigurationSection("msg").getKeys(false);
         for(String id: mail){
             if(getConfig().getBoolean("msg."+id+".usuarios."+jugador, false)){
                 mensajes++;
             }
         }
         Chat.mensaje(jugador, "mbox.list", mensajes);
     }
     public void getNextMail(String jugador){ //Obtiene todos los correos.
         List<String> mensajes = new ArrayList<String>();
         Set<String> mail = getConfig().getConfigurationSection("msg").getKeys(false);
         for(String id: mail){
             String path = "msg."+id+".usuarios."+jugador;
             if(getConfig().isSet(path)){
                 mensajes.add(id);
                 getConfig().set(path, false);
             }
         }
         if(mensajes.size() == 0){
             Chat.mensaje(jugador, "mbox.listEnd");
             return;
         }
         Chat.mensaje(jugador, "mbox.readStart");
         // Enviando cada uno de los mensajes.
         for(String lng: mensajes){
             String[] mensaje = getMail(Long.parseLong(lng));
             Chat.mensaje(jugador, "mbox.mail", mensaje);
         }
         Chat.mensaje(jugador, "mbox.readFinish");
         save();
     }
     public void createBorrador(String jugador, String args[]){ //Inicia el borrador.
         clearBorrador(jugador);
         List<String> destinatarios = checkDestinatarios(jugador, args);
         if(destinatarios.size() >= 1){
             getDraft().set(jugador+".destinatarios", destinatarios);
             Chat.mensaje(jugador, "mbox.created");
         }else{
             Chat.mensaje(jugador, "mbox.noPlayerAdded");
         }
     }
     public void setMensaje(String jugador, String mensaje){
         getDraft().set(jugador+".mensaje", mensaje);
     }
     public void addMensaje(String jugador, String mensaje){
         if(getDraft().getStringList(jugador+".destinatarios").size() < 1){
             Chat.mensaje(jugador, "mbox.noMessage");
             return;
         }
         String mensajeAnterior = "";
         if(getDraft().isSet(jugador+".mensaje")){
             mensajeAnterior = getDraft().getString(jugador+".mensaje");
         }
         if(mensajeAnterior.length() > SpiceCraft.plugin().getConfig().getInt("mail.maxChar")){
             if(!SpiceCraft.getOnlinePlayer(jugador).hasPermission("spicecraft.mail.noCharLimit")){
                 Chat.mensaje(jugador, "mbox.limit", SpiceCraft.plugin().getConfig().getInt("mail.maxChar"));
                 return;
             }
         }
         setMensaje(jugador, mensajeAnterior+" "+mensaje);
         Chat.mensaje(jugador, "mbox.add");
     }
     public void clearMensaje(String jugador){
         setMensaje(jugador, "");
     }
     public void clearBorrador(String jugador){
         getDraft().set(jugador, null);
     }
     public void sendMensaje(String jugador, List<String> destinatarios, String mensaje, Boolean servidor){
         destinatarios = checkDestinatarios(jugador, destinatarios.toArray(new String[0]));
         if(destinatarios.size() < 1){
             return;
         }
        if(!servidor && isFlooding(jugador)){
             Chat.mensaje(SpiceCraft.getOnlineExactPlayer(jugador), "mbox.flood");
             return;
         }
         long fecha = System.currentTimeMillis();
         String path = "msg."+fecha+".";
         getConfig().set(path+"remitente", jugador);
         getConfig().set(path+"servidor", servidor);
         getConfig().set(path+"mensaje", mensaje);
         for(String s : destinatarios){
             getConfig().set(path+"usuarios."+s, true);
         }
         for(String k: destinatarios){
             Chat.mensaje(k, "mbox.catched");
         }
         Chat.mensaje(jugador, "mbox.sended");
         clearBorrador(jugador);
         save();
     }
     public void sendMensaje(String jugador){
         if(!hasMensaje(jugador)){
             return;
         }
         List<String> destinatarios = getDraft().getStringList(jugador+".destinatarios");
         String mensaje = getDraft().getString(jugador+".mensaje");
         sendMensaje(jugador, destinatarios, mensaje, false);
     }
     public void sendMensajeATodos(String jugador){
         if(!hasMensaje(jugador)){
             return;
         }
         sendMensajeATodos(jugador, getDraft().getString(jugador+".mensaje"));
     }
     public void sendMensajeATodos(String jugador, String mensaje){
         if(!jugador.contentEquals(SpiceCraft.plugin().getConfig().getString("mail.serverUserName", "Server")) 
                 && !SpiceCraft.getOnlinePlayer(jugador).hasPermission("spicecraft.mail.massive")){
             return;
         }
         List<String> destinatarios = new ArrayList<String>();
         for(String p: SpiceCraft.getOfflinePlayerNames()){
             destinatarios.add(p);
         }
         sendMensaje(jugador, destinatarios, mensaje, true);
     }
     public String isDestinatario(String player){
         List<Player> l = SpiceCraft.plugin().getServer().matchPlayer(player);
         if(l.size() == 1){
             return l.get(0).getName();
         }else{
             if(SpiceCraft.plugin().getServer().getOfflinePlayer(player).hasPlayedBefore()){
                 return player;
             }
         }
         return null;
     }
     public boolean hasMensaje(String jugador){
         if(!getDraft().isSet(jugador+".mensaje")){
             Chat.mensaje(jugador, "mbox.noMessage");
             return false;
         }
         return true;
     }
     public boolean isFlooding(String jugador){
         long fecha = System.currentTimeMillis();
         String[] mails = getConfig().getConfigurationSection("msg").getKeys(false).toArray(new String[0]);
         int count = SpiceCraft.plugin().getConfig().getInt("mail.maxMailsIn15Minutes", 10);
         for(int i = mails.length-1; i >= 0; i--){
             if(Long.parseLong(mails[i])+15*60*1000 > fecha){
                 if(getConfig().getString("msg."+mails[i]+".remitente").contentEquals(jugador)){
                     count--;
                 }
                 if(count <= 0){
                     return true;
                 }
             }else{
                 break;
             }
         }
         return false;
     }
     public List<String> checkDestinatarios(String jugador, String[] destinatarios){
         List<String> checked = new ArrayList<String>();
         for(String s: destinatarios){
             String destinatario = isDestinatario(s);
             if(destinatario != null){
                 checked.add(destinatario);
             }else{
                 Chat.mensaje(jugador, "mbox.playerNotExist", s);
             }
         }
         return checked;
     }
     private Archivo getArchivo(){
         if(archivo == null){
             archivo = new Archivo("mail.yml");
         }
         return archivo;
     }
     private FileConfiguration getConfig(){
         if(cache == null){
             cache = getArchivo().load();
         }
         return cache;
     }
     private Configuration getDraft(){
         if(draft == null){
             draft = new MemoryConfiguration();
         }
         return draft;
     }
     private void save(){
         getArchivo().save(getConfig());
     }
 }
