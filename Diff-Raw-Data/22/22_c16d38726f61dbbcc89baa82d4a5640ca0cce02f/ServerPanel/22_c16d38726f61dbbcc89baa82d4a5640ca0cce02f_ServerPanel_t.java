 package co.zmc.projectindigo.gui;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.swing.JOptionPane;
 
 import co.zmc.projectindigo.IndigoLauncher;
 import co.zmc.projectindigo.data.Server;
 import co.zmc.projectindigo.gui.components.Button;
 import co.zmc.projectindigo.gui.components.RoundedBox;
 import co.zmc.projectindigo.gui.components.ServerInfo;
 import co.zmc.projectindigo.managers.ServerManager;
 import co.zmc.projectindigo.utils.Settings;
 
 @SuppressWarnings("serial")
 public class ServerPanel extends BasePanel {
 
     private ServerManager           _serverManager;
 
     private RoundedBox              _serverBox;
     private Map<String, ServerInfo> _servers        = new HashMap<String, ServerInfo>();
     private Button                  _addBtn;
     public String                   _selectedServer = "";
 
     public ServerPanel(MainPanel mainPanel) {
         super(mainPanel, 1);
         loadServerManager();
     }
 
     public void initComponents() {
         _serverBox = new RoundedBox(MainPanel.BORDER_COLOUR);
         _serverBox.setBounds((getWidth() - (getWidth() - 50)) / 2, (getHeight() - (getHeight() - 50)) / 2, getWidth() - 50, getHeight() - 50);
         _addBtn = new Button(this, "Add Server");
         _addBtn.setForeground(Color.WHITE);
         _addBtn.setBounds(_serverBox.getX() + (25 / 2), _serverBox.getY() + (_serverBox.getHeight() - 25 - 10), _serverBox.getWidth() - 20, 25);
         _addBtn.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 String ip = JOptionPane.showInputDialog(getParent(), "Please enter the full server IP you wish to connect to");
                 if (!ip.contains(" ")) {
                     int port = 25565;
                     try {
                         if (ip.contains(":")) {
                             int index = ip.indexOf(":");
                             port = Integer.parseInt(ip.substring(index + 1));
                             ip = ip.substring(0, index);
                         }
                        if (!getServerManager().loadServer(ip, port)) {
                            JOptionPane.showMessageDialog(getParent(), "This server is either offline or not integrated into the Indigo Launcher", "Invalid Server", JOptionPane.WARNING_MESSAGE);

                        }
                     } catch (NumberFormatException e1) {
                         JOptionPane.showMessageDialog(getParent(), "You need to include a valid port number", "Invalid Port", JOptionPane.WARNING_MESSAGE);
                     }
                 } else {
                     JOptionPane.showMessageDialog(getParent(), "You need to include a valid IP Address", "Invalid IP Address", JOptionPane.WARNING_MESSAGE);
                 }
             }
         });
         add(_serverBox);
 
     }
 
     public void loadServerManager() {
         _serverManager = new ServerManager(_mainPanel);
         _serverManager.execute();
     }
 
     public ServerManager getServerManager() {
         return _serverManager;
     }
 
     public void resetServers() {
         for (ServerInfo s : _servers.values()) {
             s.setActive(false);
         }
     }
 
     public ServerInfo getServerInfo(String fullIp) {
         if (_servers.containsKey(fullIp)) { return _servers.get(fullIp); }
         return null;
     }
 
     public void removeServer(String fullIp) {
         if (_servers.containsKey(fullIp)) {
             for (Component c : _servers.get(fullIp).getAllComponents()) {
                 remove(c);
             }
             remove(_servers.get(fullIp));
             _servers.remove(fullIp);
         }
     }
 
     public synchronized void addServer(final Server server) {
         try {
             if (!_servers.containsKey(server.getForgeVersion())) {
                 ServerInfo info = new ServerInfo(this, server);
                 info.setBounds((getWidth() - ((getWidth() - 50) - (50 * 2))) / 2, ((getHeight() - (getHeight() - 50)) / 2) + (MainPanel.PADDING + (_servers.size() * 32 + MainPanel.PADDING)),
                         (getWidth() - 50) - (50 * 2), 24);
                 _servers.put(server.getFullIp(), info);
             }
         } catch (Exception e) {
         }
     }
 
     public void launchServer(Settings settings) {
         switchPage(-1);
         try {
             IndigoLauncher._launcher.launchMinecraft(_serverManager.getServer(_selectedServer), ((LoginPanel) _mainPanel.getPanel(0)).getLoginResponse(), settings);
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
 }
