 package co.zmc.projectindigo.managers;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.net.ConnectException;
 import java.net.InetSocketAddress;
 import java.net.MalformedURLException;
 import java.net.Socket;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Scanner;
 
import javax.swing.JOptionPane;
 import javax.swing.SwingWorker;
 
 import org.apache.commons.io.IOUtils;
 import org.json.simple.JSONObject;
 import org.json.simple.parser.JSONParser;
 import org.json.simple.parser.ParseException;
 
 import co.zmc.projectindigo.data.Server;
 import co.zmc.projectindigo.data.log.Logger;
 import co.zmc.projectindigo.gui.MainPanel;
 import co.zmc.projectindigo.gui.ProgressPanel;
 import co.zmc.projectindigo.gui.ServerPanel;
 import co.zmc.projectindigo.utils.DirectoryLocations;
 import co.zmc.projectindigo.utils.FileUtils;
 
 public class ServerManager extends SwingWorker<Boolean, Void> {
     private static final File _saveFile = new File(DirectoryLocations.DATA_DIR_LOCATION, "servers");
     private String            _status;
     private int               _percentComplete;
     private List<Server>      _servers  = new ArrayList<Server>();
     private MainPanel         _mainPanel;
     private JSONObject        servers   = null;
 
     public ServerManager(MainPanel mainPanel) {
         _mainPanel = mainPanel;
     }
 
     @Override
     protected Boolean doInBackground() {
         loadServers();
         return true;
     }
 
     public String getStatus() {
         return _status;
     }
 
     public int getPercentComplete() {
         return _percentComplete;
     }
 
     public void loadServers() {
         try {
             if (!_saveFile.exists()) {
                 _saveFile.createNewFile();
             }
             servers = (JSONObject) new JSONParser().parse(new Scanner(_saveFile).useDelimiter("\\A").next());
 
             for (Object key : servers.keySet()) {
                 if (key instanceof String) {
                     String name = (String) key;
                     Server s = parseServer((JSONObject) servers.get(name));
                     addServer(s);
 
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     private void addServer(Server s) {
         s.checkUpdates();
         _servers.add(s);
         ((ServerPanel) _mainPanel.getPanel(1)).addServer(s);
     }
 
     public Server parseServer(String url) throws ParseException, MalformedURLException, IOException {
         JSONObject sData = (JSONObject) new JSONParser().parse(IOUtils.toString(new URL(url)));
         return parseServer(sData);
     }
 
     public Server parseServer(JSONObject sData) throws NumberFormatException, ParseException, MalformedURLException, IOException {
         return new Server(_mainPanel, sData);
 
     }
 
     public Server getServer(String fullIp) {
         for (Server s : _servers) {
             if (s.getFullIp().equalsIgnoreCase(fullIp)) { return s; }
         }
         return null;
     }
 
     public List<Server> getServers() {
         return _servers;
     }
 
     public void removeServer(String fullIp) {
         int index = -1;
         for (int i = 0; i < _servers.size(); i++) {
             Server s = _servers.get(i);
             if (s.getFullIp().equalsIgnoreCase(fullIp)) {
                 s.getBaseDir().delete();
                 index = i;
             }
         }
         if (index != -1) {
             _servers.remove(index);
             ((ServerPanel) _mainPanel.getPanel(1)).removeServer(fullIp);
             save();
         }
     }
 
     public void save() {
         String str = "{\n";
         for (int i = 0; i < _servers.size(); i++) {
             Server s = _servers.get(i);
             if (i > 0) {
                 str += ", \n";
             }
             str += "  \"" + s.getFullIp() + "\": " + s.toString();
         }
         str += "\n}";
         FileUtils.writeStringToFile(str, _saveFile);
     }
 
    public void loadServer(String ip, int port) {
         Socket s = new Socket();
         String serverURL = "";
         try {
             String channel = "projectindigo";
             String msg = "request_modpack_url";
             Logger.logInfo("Connecting to server " + ip + ":" + port);
             ((ProgressPanel) _mainPanel.getPanel(-1)).stateChanged("Connecting to " + ip + ":" + port, 20);
 
             s.connect(new InetSocketAddress(ip, port));
             DataOutputStream out = new DataOutputStream(s.getOutputStream());
             DataInputStream in = new DataInputStream(s.getInputStream());
 
             Logger.logInfo("Requesting server information");
             ((ProgressPanel) _mainPanel.getPanel(-1)).stateChanged("Requesting server information", 40);
 
             out.writeByte(0xFA);
             out.writeShort(channel.length());
             out.writeChars(channel);
             out.writeShort(msg.length());
             out.write(msg.getBytes());
 
             if (Integer.valueOf(in.read()) == 0xFA) {
                 String readStr = "";
                 int len = in.readShort();
                 for (int i = 0; i < len; i++) {
                     readStr += in.readChar();
                 }
                 if (readStr.equalsIgnoreCase(channel)) {
                     readStr = "";
                     len = in.readShort();
                     for (int i = 0; i < len; i++) {
                         readStr += (char) in.read();
                     }
                     serverURL = readStr;
                 }
             }
             in.close();
             out.flush();
             out.close();
             s.close();
         } catch (ConnectException e) {
            JOptionPane.showMessageDialog(null, "Failed to connect to the server... Are you sure the address is right?", "Connection failed", JOptionPane.WARNING_MESSAGE);
            return;
         } catch (Exception e) {
             Logger.logError(e.getMessage(), e);
         }
         if (!serverURL.isEmpty()) {
             ((ProgressPanel) _mainPanel.getPanel(-1)).stateChanged("Reading server information", 80);
 
             Logger.logInfo("Reading server information");
             try {
                 Server server = parseServer(serverURL);
                 if (server != null) {
                     addServer(server);
                 }
                 save();
             } catch (ParseException e) {
                 Logger.logError(e.getMessage(), e);
             } catch (MalformedURLException e) {
                 e.printStackTrace();
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
     }
 }
