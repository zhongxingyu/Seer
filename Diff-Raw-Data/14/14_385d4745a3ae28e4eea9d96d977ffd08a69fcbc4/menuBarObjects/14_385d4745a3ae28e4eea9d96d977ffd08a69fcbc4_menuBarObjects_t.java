 import java.awt.AWTException;
 import java.awt.Color;
 import java.awt.MenuItem;
 import java.awt.PopupMenu;
 import java.awt.SystemTray;
 import java.awt.TrayIcon;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 
 public class menuBarObjects {
 	public static TrayIcon trayIcon;
 	public static PopupMenu popup;
 	public static SystemTray tray;
 	
 	static MenuItem onoff;
 	static MenuItem pref;
 	static MenuItem quit;
 	
 	public static void setImage(String s, Color t){
		if (trayIcon.getImage() != null) {
			trayIcon.getImage().flush();
 		}
		trayIcon.setImage(textToImage.getImage(s,t));
	}
 	
 	public static void initTray(){
 	     popup = new PopupMenu();
 
 		 trayIcon = new TrayIcon(textToImage.getImage(" PAUSE" , Color.BLACK/*Pref.getPauseColor()*/));
          tray = SystemTray.getSystemTray();
        
          pref = new MenuItem("Preferences");
          onoff = new MenuItem("Start Pinging");
          quit = new MenuItem("Quit");
          addListeners();
 
         popup.add(onoff);
         popup.addSeparator();
         popup.add(pref);
         popup.addSeparator();
         popup.add(quit);
        
         
         
         trayIcon.setPopupMenu(popup);
        trayIcon.setImageAutoSize(true);
        
         try {
             tray.add(trayIcon);
         } catch (AWTException e) { System.out.println("TrayIcon could not be added.");
         }
         
         
         
 	}
 	
 	
 	public static void addListeners(){
 		quit.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
             	tray.remove(trayIcon);
             	System.exit(0);
             } });
         
         onoff.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
             	PingMonitorTool.togglePinger();
             } });
         
         pref.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
             	PreferenceGUIWindow.main(null);
             } });
 	}
 	
 	public static void pingLabel_isStarted(boolean rr){
 		if (rr==true){
 			onoff.setLabel("Stop Pinging");
 
 		}else{
 			onoff.setLabel("Start Pinging");
 
 		}
 	}
 }
