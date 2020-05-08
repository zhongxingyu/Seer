 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package aniAdd.startup;
 
 import aniAdd.Communication.ComEvent;
 import aniAdd.*;
 import gui.Mod_GUI;
 import java.awt.Font;
 import java.net.CookieHandler;
 import java.net.URI;
 import java.security.AllPermission;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Map;
 import javax.swing.JApplet;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.UIManager;
 import udpApi.Mod_UdpApi;
 /**
  *
  * @author Arokh
  */
 public class Applet extends JApplet {
     String username, session, password, autopass;
     AniAdd aniAdd;
 
     public Applet() {
         try {UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());} catch (Exception ex) { }
         
         aniAdd = new AniAdd();
     }
 
     @Override
     public void start(){
 
         try {
             AllPermission perm = new AllPermission();
             java.security.AccessController.checkPermission(perm);
         } catch (Exception exception) {
             exception.printStackTrace();
             JLabel lbl = new JLabel("Permission check failed\n");
             lbl.setFont(new Font("Tahoma", Font.BOLD, 20));
             lbl.setHorizontalAlignment(JLabel.CENTER);
             lbl.setVerticalAlignment(JLabel.CENTER);
             add(lbl);
             return;
         }
 
         username = getParameter("user");
         session = getParameter("sess");
         autopass = getParameter("autopass");
         password = "";
         if (username == null || session == null) {
             try {
                 Hashtable<String,String> cookies = new Hashtable<String,String>();
 
                 Map<String,List<String>> headers = CookieHandler.getDefault().get(new URI("http://anidb.net/"), new HashMap<String,List<String>>());
                 List<String> cookie_lists = headers.get("Cookie");
 
                 // This isn't a proper parser
                 for (String cookie_list : cookie_lists)
                     for (String cookie : cookie_list.split(";")) {
                         String attr = cookie.substring(0, cookie.indexOf('=')).trim();
                         String value = cookie.substring(cookie.indexOf('=') + 1).trim();
                         cookies.put(attr, value);
                     }
 
                 username = cookies.get("adbsessuser");
                 session = cookies.get("adbsess");
                 autopass = cookies.get("adbautopass");
                 if (username == null) username = cookies.get("adbautouser");
                 if (username == null || (session == null && autopass == null)) throw new NullPointerException();
             } catch(Exception e) {}
         }
 
        if(username==null || username.isEmpty() || password == null || password.isEmpty()){
             username = JOptionPane.showInputDialog(this, "User");
             password = JOptionPane.showInputDialog(this, "Password");
         }
         
         aniAdd.AddComListener(new Communication.ComListener() {
             public void EventHandler(ComEvent comEvent) {
                 if(comEvent.Type() == ComEvent.eType.Information){
                     if((Module.eModState)comEvent.Params(0) == Module.eModState.Initialized){
                         Initialize();
                     }
                 }
             }
         });
 
         aniAdd.Start();
     }
 
     private void Initialize(){
         Mod_GUI gui = (Mod_GUI)aniAdd.GetModule("MainGUI");
         Mod_UdpApi api = (Mod_UdpApi)aniAdd.GetModule("UdpApi");
         
         api.setPassword(password);
         api.setSession(session);
         api.setUsername(username);
 
         if(api.authenticate()) {
         } else {
         }
         
         add(gui);
     }
 
     @Override
     public void stop(){
         aniAdd.Stop();
     }
 
 }
